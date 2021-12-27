package at.technikum.orm.cache;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

@AllArgsConstructor
@EqualsAndHashCode
public class ForeignCacheKeyWrapper {

  private Class<?> fkType;
  private Class<?> entityType;
  private Object objectID;

}
