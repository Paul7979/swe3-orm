package at.technikum.orm.cache;

public interface Cache <K,V> {

  V get (K key);

  void clear();

  void put(K key, V value);

}
