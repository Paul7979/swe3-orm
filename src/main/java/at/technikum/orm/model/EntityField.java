package at.technikum.orm.model;

import at.technikum.orm.annotations.Column;
import at.technikum.orm.annotations.ForeignKey;
import at.technikum.orm.annotations.PrimaryKey;
import at.technikum.orm.exceptions.ReflectiveAccesException;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Getter
@Setter
public class EntityField {

  private String name;

  private Class<?> type;

  private Field field;

  private boolean isPK;

  private boolean isFK;

  public EntityField(Field field, Column columnAnnotation) {
    this.field = field;
    if (columnAnnotation != null && isNotBlank(columnAnnotation.name())) {
      name = columnAnnotation.name();
    } else {
      name = field.getName();
    }
    if (field.getGenericType() instanceof ParameterizedType parameterizedType) {
      type = (Class<?>) parameterizedType.getActualTypeArguments()[0]; // extracts eg. String from List<String>
    } else {
      this.type = field.getType();
    }
  }

  public EntityField(Field field, Column columnAnnotation, PrimaryKey primaryKey) {
    this(field, columnAnnotation);
    if (primaryKey != null) {
      isPK = true;
    }
  }

  public EntityField(Field field, Column columnAnnotation, ForeignKey foreignKey) {
    this(field, columnAnnotation);
    if (foreignKey != null) {
      isPK = true;
    }
  }

  public Object toDbObject(Object value) {
    if (value == null) {
      return null;
    }
    if (type.isEnum()) {
      return value.toString();
    }
    if (isFK) {
      var fkType = Entity.ofClass(type).getPrimaryKey();
      return fkType.toDbObject(value);
    }

    if (type.equals(LocalDate.class)) {
      var dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
      return dateTimeFormatter.format((LocalDate) value);
    }
    return value;
  }

  public Object getValue(Object o) {
    field.setAccessible(true);
    try {
      return field.get(o);
    } catch (IllegalAccessException e) {
      throw new ReflectiveAccesException("Error accessing", e);
    }
  }
}
