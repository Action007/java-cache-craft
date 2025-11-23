package com.cachecraft.decorator;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import com.cachecraft.cache.Cache;

public class MetricsCache<K, V> implements Cache<K, V> {
  private final Cache<K, V> delegate;
  private final AtomicLong hits;
  private final AtomicLong misses;
  private final AtomicLong totalTimeNs;

  public MetricsCache(Cache<K, V> delegate) {
    this.delegate = delegate;
    this.hits = new AtomicLong(0);
    this.misses = new AtomicLong(0);
    this.totalTimeNs = new AtomicLong(0);
  }

  @Override
  public Optional<V> get(K key) {
    long start = System.nanoTime();

    // Delegate the actual work
    Optional<V> result = delegate.get(key);

    long end = System.nanoTime();
    long duration = end - start;

    // Update metrics
    totalTimeNs.addAndGet(duration);

    if (result.isPresent()) {
      hits.incrementAndGet();
    } else {
      misses.incrementAndGet();
    }

    return result;
  }

  @Override
  public void put(K key, V value) {
    delegate.put(key, value);
  }

  @Override
  public void remove(K key) {
    delegate.remove(key);
  }

  @Override
  public void clearAll() {
    delegate.clearAll();
  }

  @Override
  public int size() {
    return delegate.size();
  }

  @Override
  public boolean containsKey(K key) {
    return delegate.containsKey(key);
  }

  // Custom method to view stats
  public String getStatistics() {
    long h = hits.get();
    long m = misses.get();
    long totalOps = h + m;
    long time = totalTimeNs.get();

    double hitRate = totalOps == 0 ? 0.0 : (double) h / totalOps * 100.0;
    double avgTime = totalOps == 0 ? 0.0 : (double) time / totalOps;

    return String.format("Cache Stats: [Hits: %d, Misses: %d, HitRate: %.2f%%, AvgTime: %.2f ns]",
        h, m, hitRate, avgTime);
  }
}
