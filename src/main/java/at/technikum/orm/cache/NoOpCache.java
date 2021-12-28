package at.technikum.orm.cache;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class NoOpCache<K, V> implements Cache<K, V> {

  public NoOpCache() {
  }

  @Override
  public V get(K key) {
    return null;
  }

  @Override
  public void clear() {
  }

  @Override
  public void put(K key, V value) {
  }
}
