package at.technikum.orm.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(value = RetentionPolicy.RUNTIME)
public @interface ForeignKey {
  String referencesTable() default "";
  String columnName() default "";
}
