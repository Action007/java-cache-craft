package com.cachecraft.factory;

import com.cachecraft.cache.Cache;
import com.cachecraft.cache.StandardCache;
import com.cachecraft.eviction.EvictionPolicy;

public class CacheFactory {
  public static <K, V> Cache<K, V> createInMemoryCache(EvictionPolicy<K, V> policy, int maxSize) {
    return new StandardCache<>(policy, maxSize, -1);
  }

  public static <K, V> Cache<K, V> createTimedCache(EvictionPolicy<K, V> policy, long ttlMillis,
      int maxSize) {
    return new StandardCache<>(policy, maxSize, ttlMillis);
  }
}
