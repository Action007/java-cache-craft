package com.cachecraft.eviction;

import java.util.Map;
import com.cachecraft.entry.CacheEntry;

public class SizeBasedEvictionPolicy<K, V> implements EvictionPolicy<K, V> {
  private final int maxWeight;
  private final EvictionPolicy<K, V> delegate;

  public SizeBasedEvictionPolicy(int maxWeight, EvictionPolicy<K, V> delegate) {
    this.maxWeight = maxWeight;
    this.delegate = delegate;
  }

  @Override
  public K selectVictim(Map<K, CacheEntry<V>> entries) {
    int totalWeight = entries.values().stream().mapToInt(CacheEntry::getSizeWeight).sum();

    if (totalWeight > maxWeight) {
      return delegate.selectVictim(entries);
    }
    return null;
  }
}
