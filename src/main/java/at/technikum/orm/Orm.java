package at.technikum.orm;

import at.technikum.orm.cache.Cache;
import at.technikum.orm.cache.CacheKeyWrapper;
import at.technikum.orm.cache.InMemoryCache;
import at.technikum.orm.model.Entity;
import at.technikum.orm.model.EntityField;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;
import org.postgresql.core.Tuple;

import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
public class Orm {

  private static final String INSERT_TEMPLATE = """
    INSERT INTO ${tableName} (${columNames})
    VALUES (${valuePlaceholder})
    ON CONFLICT (${pkColumnName}) DO UPDATE SET ${columnsWithoutPKWithPlaceholder}""";

  private static final String SELECT_TEMPLATE = """
    SELECT ${columNames} FROM ${tableName}
    WHERE (${pkColumnName}) = ?""";


  private final ConnectionFactory connectionFactory;
  private final Cache<CacheKeyWrapper, Object> cache;

  public Orm(String url) throws SQLException {
    connectionFactory =  ConnectionFactory.of(url);
    cache = new InMemoryCache<>();
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
      .filter(Predicate.not(EntityField::isFK))
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
          var newInstance = allArgsConstructor.newInstance(values.toArray());
          cache.put(cacheKeyWrapper, newInstance);
          return newInstance;
        }

        Constructor<?> noArgsConstructor = tryToGetNoArgsConstructor(entity);
        if (noArgsConstructor != null) {
          noArgsConstructor.setAccessible(true);
          T o = (T) noArgsConstructor.newInstance();
          for (int i = 0; i < values.size(); i++) {
            entityFields.get(i).setValue(o, values.get(i));
          }
          cache.put(cacheKeyWrapper, o);
          return o;
        }

        throw new RuntimeException("Could not unwrap db object");
      } catch (Exception e) {
        log.error("Error calling constructor", e);
        throw new RuntimeException("Could fetch db object");
      }
    }
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
