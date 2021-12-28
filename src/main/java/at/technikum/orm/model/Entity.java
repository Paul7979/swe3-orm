package at.technikum.orm.model;

import at.technikum.orm.annotations.*;
import at.technikum.orm.exceptions.MissingAnnotationException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Getter
@Setter
@Slf4j
public class Entity {

  private static final Map<Class<?>, Entity> entityCache = new HashMap<>();

  private final List<EntityField> entityFields = new ArrayList<>();
  private final List<EntityField> foreignKeys = new ArrayList<>();
  private final Class<?> type;
  private final String tableName;
  private EntityField primaryKey;

  private Entity(Class<?> type) {
    this.type = type;
    var entityAnnotation = type.getAnnotation(at.technikum.orm.annotations.Entity.class);
    if (entityAnnotation == null) {
      throw new MissingAnnotationException(type.getSimpleName() + " is not an @Entity");
    }
    if (isNotBlank(entityAnnotation.tableName())) {
      tableName = entityAnnotation.tableName();
    } else {
      tableName = type.getSimpleName();
    }
    allFieldsFor(type).forEach(field -> {
      var ignore = field.getAnnotation(Ignore.class);
      if (ignore != null || Modifier.isStatic(field.getModifiers())) {
        return;
      }

      var primaryKeyAnnotation = field.getAnnotation(PrimaryKey.class);
      var foreignKey = field.getAnnotation(ForeignKey.class);
      var manyToMany = field.getAnnotation(ManyToMany.class);
      var columnAnnotation = field.getAnnotation(Column.class);
      var newEntity = new EntityField(field, columnAnnotation);
      if (primaryKeyAnnotation != null) {
        newEntity.setPK(true);
        if (isNotBlank(primaryKeyAnnotation.columnName())) {
          newEntity.setColumnName(primaryKeyAnnotation.columnName());
        }
        primaryKey = newEntity;
      }
      if (foreignKey != null) {
        newEntity.setFK(true);
        if (isNotBlank(foreignKey.columnName())) {
          newEntity.setColumnName(foreignKey.columnName());
        }
        foreignKeys.add(newEntity);
      }
      if (manyToMany != null) {
        newEntity.setManyToMany(true);
        ManyToManyRelation relation = new ManyToManyRelation();
        relation.setReferenceTableName(defaultIfBlank(manyToMany.referenceTableName(), type.getSimpleName() + "_" + field.getName()));
        relation.setReferencedColumnName(defaultIfBlank(manyToMany.referencedColumnName(), "fk_" + type.getSimpleName().toLowerCase()));
        newEntity.setManyToManyRelation(relation);
      }
      entityFields.add(newEntity);
    });
  }

  public static Entity ofClass(Class<?> clazz) {
    if (entityCache.containsKey(clazz)) {
      log.info("Entity Cache hit on class {}", clazz.getSimpleName());
      return entityCache.get(clazz);
    }
    var entity = new Entity(clazz);
    entityCache.put(clazz, entity);
    return entity;
  }

  public static Entity ofObject(Object o) {
    return ofClass(o.getClass());
  }

  private Stream<Field> allFieldsFor(Class<?> c) {
    return walkInheritanceTreeFor(c).flatMap(k -> Arrays.stream(k.getDeclaredFields()));
  }

  private Stream<Class<?>> walkInheritanceTreeFor(Class<?> c) {
    return iterate(c, k -> Optional.ofNullable(k.getSuperclass()));
  }

  private <T> Stream<T> iterate(T seed, Function<T, Optional<T>> fetchNextFunction) {
    Objects.requireNonNull(fetchNextFunction);

    Iterator<T> iterator = new Iterator<T>() {
      private Optional<T> t = Optional.ofNullable(seed);

      public boolean hasNext() {
        return t.isPresent();
      }

      public T next() {
        T v = t.get();

        t = fetchNextFunction.apply(v);

        return v;
      }
    };

    return StreamSupport.stream(
      Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED | Spliterator.IMMUTABLE),
      false
    );
  }
}
