package com.cachecraft.eviction;

import java.time.Instant;
import java.util.Map;
import com.cachecraft.entry.CacheEntry;

public class TtlEvictionPolicy<K, V> implements EvictionPolicy<K, V> {
  private final long ttlMillis;

  public TtlEvictionPolicy(long ttlMillis) {
    this.ttlMillis = ttlMillis;
  }

  @Override
  public K selectVictim(Map<K, CacheEntry<V>> entries) {
    Instant now = Instant.now();
    return entries.entrySet().stream()
        .filter((entry) -> now.toEpochMilli()
            - entry.getValue().getCreationTime().toEpochMilli() > ttlMillis)
        .findFirst().map(Map.Entry::getKey).orElse(null);
  }
}
