package at.technikum.orm.cache;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;

@AllArgsConstructor
@EqualsAndHashCode
public class CacheKeyWrapper {

  private Class<?> clazz;
  private Object objectID;

}
