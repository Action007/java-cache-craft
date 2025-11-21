package com.cachecraft.entry;

import java.time.Instant;

public class CacheEntry<V> {
  private V value;
  private Instant creationTime;
  private Instant lastAccessTime;
  private int accessCount = 0;
  private int sizeWeight;


  public CacheEntry(V value) {
    this.value = value;
    this.creationTime = Instant.now();
    this.lastAccessTime = this.creationTime;
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

  public int getAccessCount() {
    return accessCount;
  }

  public void recordAccess() {
    this.lastAccessTime = Instant.now();
    this.accessCount++;
  }
}
