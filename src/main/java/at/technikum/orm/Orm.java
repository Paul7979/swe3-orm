package at.technikum.orm;

import at.technikum.orm.cache.Cache;
import at.technikum.orm.cache.CacheKeyWrapper;
import at.technikum.orm.cache.ForeignCacheKeyWrapper;
import at.technikum.orm.cache.InMemoryCache;
import at.technikum.orm.model.Entity;
import at.technikum.orm.model.EntityField;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;

import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

@Slf4j
public class Orm {

  private static final String INSERT_TEMPLATE = """
    INSERT INTO ${tableName} (${columNames})
    VALUES (${valuePlaceholder})
    ON CONFLICT (${pkColumnName}) DO UPDATE SET ${columnsWithoutPKWithPlaceholder}""";

  private static final String SELECT_TEMPLATE = """
    SELECT ${columNames} FROM ${tableName}
    WHERE (${pkColumnName}) = ?""";

  private static final String SELECT_FK_TEMPLATE = """
    SELECT ${columNames} FROM ${tableName}
    WHERE (${fkColumnName}) = ?""";

  private static final String SIMPLE_SELECT_INTERNAL = """
    SELECT ${columNames} FROM ${tableName}""";

  private final ConnectionFactory connectionFactory;
  private final Cache<CacheKeyWrapper, Object> cache;
  private final Cache<ForeignCacheKeyWrapper, Object> foreigKeyCache;

  public Orm(String url) throws SQLException {
    connectionFactory =  ConnectionFactory.of(url);
    cache = new InMemoryCache<>();
    foreigKeyCache = new InMemoryCache<>();
  }

  public <T>T get(Class<T> clazz, Object ID) throws SQLException {
    var cacheKeyWrapper = new CacheKeyWrapper(clazz, ID);
    var cachedObject = cache.get(cacheKeyWrapper);
    if (cachedObject != null) {
      log.info("Hit Cache on class {} with id {}", clazz.getSimpleName(), ID);
      return (T) cachedObject;
    }
    var entity = Entity.ofClass(clazz);
    var entityFields = entity.getEntityFields().stream()
      .filter(not(EntityField::isFK))
      .toList();
    var columnNames = entityFields.stream()
      .map(EntityField::getName).collect(Collectors.joining(", "));
    var columnTypes = entityFields.stream()
      .map(EntityField::getType)
      .toList();

    Map<String, String> substituteValues = new HashMap<>();
    substituteValues.put("tableName", entity.getTableName());
    substituteValues.put("columNames", columnNames);
    substituteValues.put("pkColumnName", entity.getPrimaryKey().getName());

    StringSubstitutor stringSubstitutor = new StringSubstitutor(substituteValues);
    var selectStatement = stringSubstitutor.replace(SELECT_TEMPLATE);
    log.info(selectStatement);
    Object o = null;
    try (
      var connection = connectionFactory.get();
      var preparedStatement = connection.prepareStatement(selectStatement)
    ) {
      preparedStatement.setObject(1, ID);
      var resultSet = preparedStatement.executeQuery();
      List<Object> values = new ArrayList<>(columnTypes.size());
      if (resultSet.next()) {
        for (int i = 0; i < entityFields.size(); i++) {
          var entityField = entityFields.get(i);
          var value = entityField.fromDbObject(resultSet.getObject(i+1));
          values.add(value);
        }
      }
      //try to get a matching constructor - else use no args const and reflection
      try {
        Constructor<T> allArgsConstructor = tryToGetAllArgsConstructor(entity, columnTypes);
        if (allArgsConstructor != null) {
          allArgsConstructor.setAccessible(true);
          o = allArgsConstructor.newInstance(values.toArray());
        } else {
          Constructor<?> noArgsConstructor = tryToGetNoArgsConstructor(entity);
          if (noArgsConstructor != null) {
            noArgsConstructor.setAccessible(true);
            T obj = (T) noArgsConstructor.newInstance();
            for (int i = 0; i < values.size(); i++) {
              entityFields.get(i).setValue(obj, values.get(i));
            }
            o = obj;
          }
        }
        if (o == null) {
          throw new RuntimeException("Error creating object from DB, try declaring a no Args constructor in class " + clazz.getSimpleName());
        }
      } catch (Exception e) {
        log.error("Error calling constructor", e);
        throw new RuntimeException("Could fetch db object");
      }
    }
    cache.put(cacheKeyWrapper, (T) o);
    fillForeignFields(o, entity);
    return (T) o;
  }

  private void fillForeignFields(Object o, Entity entity) {
    entity.getForeignKeys().forEach(foreignField -> {
      if (foreignField.isCollection()) {
        Object fields = getCollectionByFk(foreignField.getRawType(), entity.getType(), entity.getPrimaryKey().getValue(o));
        foreignField.setValue(o, fields);
      }
    });
  }

  private Object getCollectionByFk(Class<?> rawType, Class<?> entityType, Object primaryKey) {

    var cacheKey = new ForeignCacheKeyWrapper(rawType, entityType, primaryKey);
    var cachedObj = foreigKeyCache.get(cacheKey);
    if (cachedObj != null) {
      return cachedObj;
    }

    Entity entity = Entity.ofClass(rawType);
    var fkTableName = entity.getEntityFields()
      .stream()
      .filter(entityField -> entityField.getType().equals(entityType))
      .map(EntityField::getName)
      .findFirst().orElseThrow();

    var selectStatement = simpleSelectAllInternalFields(entity) + " where " + fkTableName + " = ?";
    var retrieveCollection = retrieveCollection(selectStatement, entity, primaryKey);
    foreigKeyCache.put(cacheKey, retrieveCollection);
    return retrieveCollection;
  }

  private Object retrieveCollection(String selectStatement, Entity entity, Object primaryKey) {
    try (
      var connection = connectionFactory.get();
      var preparedStatement = connection.prepareStatement(selectStatement)
    ) {
      List<Object> objects = new ArrayList<>();
      var entityFields = entity.getEntityFields().stream().filter(not(EntityField::isFK)).toList();
      preparedStatement.setObject(1, primaryKey);
      var resultSet = preparedStatement.executeQuery();
      Constructor<?> noArgsConstructor = tryToGetNoArgsConstructor(entity);
      Objects.requireNonNull(noArgsConstructor).setAccessible(true);
      while (resultSet.next()) {
        Object o = noArgsConstructor.newInstance();
        for (int i = 0; i < entityFields.size(); i++) {
          entityFields.get(i).setValue(o, resultSet.getObject(i+1));
        }
        objects.add(o);
      }
      return objects;
    } catch (Exception e) {
      throw new RuntimeException("Error getting Collection ", e);
    }
  }

  private static String simpleSelectAllInternalFields(Entity entity) {
    var columnNames = entity.getEntityFields()
      .stream()
      .filter(not(EntityField::isFK))
      .map(EntityField::getName)
      .collect(Collectors.joining(", "));
    Map<String, String> substituteValues = new HashMap<>();
    substituteValues.put("tableName", entity.getTableName());
    substituteValues.put("columNames", columnNames);
    StringSubstitutor stringSubstitutor = new StringSubstitutor(substituteValues);
    return stringSubstitutor.replace(SIMPLE_SELECT_INTERNAL);
  }

  private void saveIgnoringFK(Object o, List<Class<?>> classes) {

    Entity entity = Entity.ofClass(o.getClass());

    List<String> columnNames = new ArrayList<>(entity.getEntityFields().size());
    List<String> columnNamesWithoutPK = new ArrayList<>(entity.getEntityFields().size());
    List<Object> values = new ArrayList<>();
    List<Object> valuesWithoutPK = new ArrayList<>();

    entity.getEntityFields().forEach(entityField -> {
      if (entityField.isFK()) {
        var fkToStore = entityField.getValue(o);
        if (fkToStore == null) {
          return;
        }
        if (!classes.contains(entityField.getType()) && !classes.contains(entityField.getRawType())) {
          classes.add(o.getClass());
          if (entityField.isCollection() && fkToStore instanceof Collection<?> collection) {
            collection.forEach(objToStore -> saveIgnoringFK(objToStore, classes));
            return;
          }
          saveIgnoringFK(fkToStore, classes);
        }
        if (entityField.isJoining()){
          return;
        }
        columnNames.add(entityField.getName());
        columnNamesWithoutPK.add(entityField.getName());
        var value = Entity.ofClass(entityField.getType()).getPrimaryKey().getValue(fkToStore);
        valuesWithoutPK.add(value);
        values.add(value);
        return;
      }
      columnNames.add(entityField.getName());
      var object = entityField.toDbObject(entityField.getValue(o));
      if (!entityField.isPK()) {
        columnNamesWithoutPK.add(entityField.getName());
        valuesWithoutPK.add(object);
      }
      values.add(object);
    });

    Map<String, String> substituteValues = new HashMap<>();
    substituteValues.put("tableName", entity.getTableName());
    substituteValues.put("pkColumnName", entity.getPrimaryKey().getName());
    substituteValues.put("columNames", String.join(", ", columnNames));
    substituteValues.put("valuePlaceholder", createPlaceholders(columnNames));
    substituteValues.put("columnsWithoutPKWithPlaceholder", createColumnNamesWithPlaceholders(columnNamesWithoutPK));

    StringSubstitutor stringSubstitutor = new StringSubstitutor(substituteValues);

    values.addAll(valuesWithoutPK);
    var insertStatement = stringSubstitutor.replace(INSERT_TEMPLATE);
    log.info(insertStatement);
    try (
      var connection = connectionFactory.get();
      var preparedStatement = connection.prepareStatement(insertStatement)
    ) {
      for (int i = 0; i < values.size(); i++) {
        preparedStatement.setObject(i+1, values.get(i));
      }
      preparedStatement.execute();
      var primaryKey = entity.getPrimaryKey().getValue(o);
      var cacheKeyWrapper = new CacheKeyWrapper(o.getClass(), primaryKey);
      cache.put(cacheKeyWrapper, o);
    } catch (SQLException e) {
      throw new RuntimeException("Failed to store", e);
    }
  }

  public void save(Object o) throws SQLException {
    saveIgnoringFK(o, new ArrayList<>());
  }

  private <T> Constructor<T> tryToGetNoArgsConstructor(Entity entity) {
    try {
      return (Constructor<T>) entity.getType().getConstructor();
    } catch (NoSuchMethodException e) {
      return null;
    }
  }

  private <T> Constructor<T> tryToGetAllArgsConstructor(Entity entity, List<? extends Class<?>> types) {
    try {
      return (Constructor<T>) entity.getType().getConstructor(types.toArray(new Class<?>[0]));
    } catch (NoSuchMethodException e) {
      return null;
    }
  }

  private String createColumnNamesWithPlaceholders(List<String> columnNamesWithoutPK) {
    return String.join(" = ?, ", columnNamesWithoutPK) + " = ?";
  }

  private String createPlaceholders(List<String> columnNames) {
    return String.join(", ", Collections.nCopies(columnNames.size(), "?"));
  }
}
