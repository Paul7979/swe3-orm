package at.technikum.orm;

import at.technikum.orm.model.Entity;
import at.technikum.orm.model.EntityField;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.function.Predicate.not;

@Slf4j
public class FluentSelect {

  private final ConnectionFactory connectionFactory;
  private String sql;
  private Entity entity;
  private final List<Object> objects = new ArrayList<>();
  private boolean whereSet = false;

  FluentSelect(Entity entity, ConnectionFactory connectionFactory) {
    sql = Orm.simpleSelectAllInternalFields(entity);
    this.entity = entity;
    this.connectionFactory = connectionFactory;
  }


  /**
   * Starts the chain of the fluent Select
   * @param fieldName the name of the field as it is in the DB
   * @param operation the desired {@link Operation operation}
   * @param value the value that should be checked for
   * @return the {@link FluentSelect fluentSelect} object to keep chaining
   */
  public FluentSelect where(String fieldName, Operation operation, Object value) {
    sql+= " where " + fieldName + operationToString(operation) + "?";
    objects.add(value);
    whereSet = true;
    return this;
  }

  /**
   * Concatenates using "AND", throws runtime exception when {@link #where(String, Operation, Object) where} was not called before
   * @param fieldName the name of the field as it is in the DB
   * @param operation the desired {@link Operation operation}
   * @param value the value that should be checked for
   * @return the {@link FluentSelect fluentSelect} object to keep chaining
   */
  public FluentSelect and(String fieldName, Operation operation, Object value) {
    if (!whereSet) {
      throw new RuntimeException("Start by using where");
    }
    sql+= " AND " + fieldName + operationToString(operation) + "?";
    objects.add(value);
    return this;
  }

  /**
   * Concatenates using "OR", throws runtime exception when {@link #where(String, Operation, Object) where} was not called before
   * @param fieldName the name of the field as it is in the DB
   * @param operation the desired {@link Operation operation}
   * @param value the value that should be checked for
   * @return the {@link FluentSelect fluentSelect} object to keep chaining
   */
  public FluentSelect or(String fieldName, Operation operation, Object value) {
    if (!whereSet) {
      throw new RuntimeException("Start by using where");
    }
    sql+= " OR " + fieldName + operationToString(operation) + "?";
    objects.add(value);
    return this;
  }

  /**
   *
   * @param clazz the Raw Type of the collection in order to use in a type safe way
   * @return the Collection of results
   */
  public <T> Collection<T> get(Class<T> clazz) {
    if (!whereSet) {
      throw new RuntimeException("Start by using where");
    }
    try (
      var connection = connectionFactory.get();
      var preparedStatement = connection.prepareStatement(sql)
    ) {
      log.debug(sql);
      for (int i = 0; i < objects.size(); i++) {
        var x = toDbObject(objects.get(i));
        preparedStatement.setObject(i + 1, x);
      }

      var resultSet = preparedStatement.executeQuery();


      return resultSetToCollection(resultSet, clazz);
    } catch (Exception e) {
      log.error("Error calling constructor", e);
      throw new RuntimeException("Could fetch db object");
    }
  }

  private <T> Collection<T> resultSetToCollection(ResultSet resultSet, Class<T> clazz) throws NoSuchMethodException, SQLException, InvocationTargetException, InstantiationException, IllegalAccessException {
    Collection<T> collection = new ArrayList<>();
    var entityFields = entity.getEntityFields()
      .stream()
      .filter(not(EntityField::isFK))
      .filter(not(EntityField::isManyToMany))
      .toList();
    var constructor = clazz.getConstructor();

    while (resultSet.next()) {
      var o = constructor.newInstance();
      for (int i = 0; i < entityFields.size(); i++) {
        var entityField = entityFields.get(i);
        var dbObject = entityField.fromDbObject(resultSet.getObject(i + 1));
        entityField.getField().set(o, dbObject);
      }
      collection.add(o);
    }
    return collection;

  }

  private Object toDbObject(Object value) {
    if (value == null) {
      return null;
    }
    Class<?> type = value.getClass();
    if (type.isEnum()) {
      return value.toString();
    }
    if (type.equals(LocalDate.class)) {
      var dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
      return dateTimeFormatter.format((LocalDate) value);
    }
    return value;
  }

  private String operationToString(Operation operation) {
    return switch (operation) {
      case IS -> " = ";
      case LESS -> " < ";
      case GREATER -> " > ";
      case NOT -> " != ";
    };
  }
}
