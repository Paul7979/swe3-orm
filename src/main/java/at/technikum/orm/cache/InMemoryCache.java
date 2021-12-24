package at.technikum.orm.cache;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
public class InMemoryCache<K, V> implements Cache<K,V> {

  private Map<K, V> cache;

  private Map<OffsetDateTime, K> timestampKeyMap;

  public InMemoryCache() {
    Runnable cleaner = new Runnable() {
      @Override
      public void run() {
        log.info("Starting cleanup");
        timestampKeyMap.entrySet()
          .stream()
          .filter(offsetDateTimeKEntry -> Duration.between(offsetDateTimeKEntry.getKey(), OffsetDateTime.now()).toSeconds() > 30)
          .forEach(offsetDateTimeKEntry -> cache.remove(offsetDateTimeKEntry.getValue()));
        log.info("Finished cleanup");
      }
    };
    ScheduledExecutorService executor = Executors.newScheduledThreadPool(1, r -> {
      var thread = Executors.defaultThreadFactory().newThread(r);
      thread.setDaemon(true);
      return thread;
    });
    executor.scheduleAtFixedRate(cleaner, 60, 30, TimeUnit.SECONDS);
    timestampKeyMap = new HashMap<>();
    cache = new ConcurrentHashMap<>();
  }

  @Override
  public V get(K key) {
    return cache.get(key);
  }

  @Override
  public void clear() {
    cache = new ConcurrentHashMap<>();
    timestampKeyMap = new HashMap<>();
  }

  @Override
  public void put(K key, V value) {
    cache.put(key, value);
    timestampKeyMap.put(OffsetDateTime.now(), key);
  }
}
