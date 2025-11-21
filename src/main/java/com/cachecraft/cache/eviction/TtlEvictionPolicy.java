package com.cachecraft.cache.eviction;

import java.util.Map;
import com.cachecraft.entry.CacheEntry;

public class TtlEvictionPolicy<K, V> implements EvictionPolicy<K, V> {

  @Override
  public K selectVictim(Map<K, CacheEntry<V>> entries) {

  }
}
