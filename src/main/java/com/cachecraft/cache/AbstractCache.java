package com.cachecraft.cache;

import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import com.cachecraft.entry.CacheEntry;
import com.cachecraft.eviction.EvictionPolicy;

public abstract class AbstractCache<K, V> implements Cache<K, V> {
  private final ConcurrentHashMap<K, CacheEntry<V>> cache;
  private final EvictionPolicy<K, V> evictionPolicy;
  private final int maxSize;
  private final long ttl;

  public AbstractCache(EvictionPolicy<K, V> evictionPolicy, int maxSize, long ttl) {
    this.cache = new ConcurrentHashMap<>();
    this.ttl = ttl;
    this.evictionPolicy = evictionPolicy;
    this.maxSize = maxSize;
  }

  @Override
  public Optional<V> get(K key) {
    beforeGet(key);

    CacheEntry<V> entry = cache.get(key);
    Optional<V> result;

    if (entry == null) {
      result = Optional.empty();
    } else if (entry.isExpired(ttl)) {
      cache.remove(key);
      return Optional.empty();
    } else {
      entry.recordAccess();
      result = Optional.of(entry.getValue());
    }

    afterGet(key, result);
    return result;
  }

  @Override
  public void put(K key, V value) {
    Objects.requireNonNull(key);
    Objects.requireNonNull(value);

    beforePut(key, value);

    synchronized (this) {
      if (cache.size() > 0) {
        while (cache.size() >= maxSize && !cache.containsKey(key)) {
          K victim = evictionPolicy.selectVictim(cache);
          if (victim == null)
            break;
          cache.remove(victim);
        }
      }
    }

    cache.put(key, new CacheEntry<V>(value));
    afterPut(key, value);
  };

  @Override
  public void remove(K key) {
    cache.remove(key);
  };

  @Override
  public void clearAll() {
    cache.clear();
  }

  @Override
  public int size() {
    return cache.size();
  }

  @Override
  public boolean containsKey(K key) {
    return cache.containsKey(key);
  }

  protected void beforeGet(K key) {}

  protected void afterGet(K key, Optional<V> result) {}

  protected void beforePut(K key, V value) {}

  protected void afterPut(K key, V value) {}
}
