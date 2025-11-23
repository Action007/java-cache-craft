package com.cachecraft.cache;

import com.cachecraft.eviction.EvictionPolicy;

public class StandardCache<K, V> extends AbstractCache<K, V> {
  public StandardCache(EvictionPolicy<K, V> evictionPolicy, int maxSize, long ttl) {
    super(evictionPolicy, maxSize, ttl);
  }
}
