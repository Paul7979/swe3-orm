package at.technikum.orm.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ManyToMany {
  String columnName() default "";
  String referenceTableName() default "";
  String referencedColumnName() default "";
}
