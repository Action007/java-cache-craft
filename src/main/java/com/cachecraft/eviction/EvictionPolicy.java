package com.cachecraft.eviction;

import java.util.Map;
import com.cachecraft.entry.CacheEntry;

public interface EvictionPolicy<K, V> {
  K selectVictim(Map<K, CacheEntry<V>> entries);
}
