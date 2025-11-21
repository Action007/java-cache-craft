package com.cachecraft.cache.eviction;

import java.util.Comparator;
import java.util.Map;
import com.cachecraft.entry.CacheEntry;

public class LfuEvictionPolicy<K, V> implements EvictionPolicy<K, V> {

  @Override
  public K selectVictim(Map<K, CacheEntry<V>> entries) {
    return entries.entrySet().stream().min(Comparator
        .comparing((Map.Entry<K, CacheEntry<V>> entry) -> entry.getValue().getAccessCount())
        .thenComparing((Map.Entry<K, CacheEntry<V>> entry) -> entry.getValue().getLastAccessTime()))
        .map(Map.Entry::getKey)
        .orElseThrow(() -> new IllegalStateException("Cannot select victim from empty entries"));
  }
}
