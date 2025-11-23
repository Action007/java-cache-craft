package com.cachecraft.entry;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

public class CacheEntry<V> {
  private V value;
  private Instant creationTime;
  private volatile Instant lastAccessTime;
  private AtomicInteger accessCount;
  private int sizeWeight;

  public CacheEntry(V value) {
    this.value = value;
    this.creationTime = Instant.now();
    this.lastAccessTime = this.creationTime;
    this.accessCount = new AtomicInteger(0);
    this.sizeWeight = 1;
  }

  public V getValue() {
    return this.value;
  }

  public Instant getCreationTime() {
    return this.creationTime;
  }

  public Instant getLastAccessTime() {
    return this.lastAccessTime;
  }

  public int getSizeWeight() {
    return this.sizeWeight;
  }

  public AtomicInteger getAccessCount() {
    return accessCount;
  }

  public void recordAccess() {
    this.lastAccessTime = Instant.now();
    this.accessCount.incrementAndGet();
  }

  public boolean isExpired(long ttlMillis) {
    Instant now = Instant.now();
    if (ttlMillis == -1 || ttlMillis == 0) {
      return false;
    }
    if (now.toEpochMilli() - creationTime.toEpochMilli() > ttlMillis) {
      return true;
    }
    return false;
  }
}
