package com.cachecraft.cache;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import com.cachecraft.entry.CacheEntry;
import com.cachecraft.eviction.EvictionPolicy;

public class TTLAwareCache<K, V> extends AbstractCache<K, V> {
  private final ScheduledExecutorService expirationScheduler;
  private final EvictionPolicy<K, V> ttlPolicy;

  public TTLAwareCache(EvictionPolicy<K, V> evictionPolicy, int maxSize, long ttl,
      EvictionPolicy<K, V> ttlPolicy) {
    super(evictionPolicy, maxSize, ttl);
    this.ttlPolicy = ttlPolicy;

    // Start background expiration thread
    this.expirationScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
      Thread t = new Thread(r, "TTL-Expiration-Thread");
      t.setDaemon(true); // Don't prevent JVM shutdown
      return t;
    });

    // Run every 5 seconds
    expirationScheduler.scheduleAtFixedRate(this::cleanupExpiredEntries, 5, 5, TimeUnit.SECONDS);
  }

  /**
   * Background cleanup of expired entries. Called periodically by scheduler.
   */
  private void cleanupExpiredEntries() {
    try {
      int removedCount = 0;
      K victim;

      // Keep removing expired entries until none left
      while ((victim = ttlPolicy.selectVictim(getInternalCache())) != null) {
        remove(victim);
        removedCount++;
        System.out.println("[TTL-CLEANUP] Expired: " + victim);
      }

      if (removedCount > 0) {
        System.out.println("[TTL-CLEANUP] Removed " + removedCount + " expired entries");
      }
    } catch (Exception e) {
      System.err.println("[TTL-CLEANUP] Error during cleanup: " + e.getMessage());
    }
  }

  /**
   * Expose internal cache for TTL policy to scan. This is a bit of a hack - better design would be
   * to have AbstractCache expose this.
   */
  private Map<K, CacheEntry<V>> getInternalCache() {
    // You need to add a protected method in AbstractCache:
    // protected Map<K, CacheEntry<V>> getEntries() { return cache; }
    throw new UnsupportedOperationException(
        "Need to add protected getEntries() method in AbstractCache");
  }

  /**
   * Shutdown the background thread. MUST be called when done with cache to prevent resource leak.
   */
  public void shutdown() {
    System.out.println("[TTL-CLEANUP] Shutting down background thread...");
    expirationScheduler.shutdown();
    try {
      if (!expirationScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
        expirationScheduler.shutdownNow();
      }
    } catch (InterruptedException e) {
      expirationScheduler.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }
}
