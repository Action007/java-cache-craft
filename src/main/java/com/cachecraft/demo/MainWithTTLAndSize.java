package com.cachecraft.demo;

import com.cachecraft.cache.Cache;
import com.cachecraft.cache.TTLAwareCache;
import com.cachecraft.eviction.LruEvictionPolicy;
import com.cachecraft.eviction.SizeBasedEvictionPolicy;
import com.cachecraft.eviction.TtlEvictionPolicy;
import com.cachecraft.factory.CacheFactory;

public class MainWithTTLAndSize {

    public static void main(String[] args) throws InterruptedException {
        demonstrateTTLPolicy();
        demonstrateSizeBasedPolicy();
    }

    // ============================================================
    // TTL Policy Demonstration
    // ============================================================
    public static void demonstrateTTLPolicy() throws InterruptedException {
        System.out.println("\n=== TTL POLICY DEMONSTRATION ===");

        // Create TTL policy
        TtlEvictionPolicy<String, String> ttlPolicy = new TtlEvictionPolicy<>(3000); // 3 seconds

        // Create cache with background TTL cleanup
        TTLAwareCache<String, String> cache = new TTLAwareCache<>(new LruEvictionPolicy<>(), // For
                                                                                             // capacity-based
                                                                                             // eviction
                10, // Max size
                3000, // TTL: 3 seconds
                ttlPolicy // For background cleanup
        );

        // Add some entries
        System.out.println("\n[T=0s] Adding 3 entries...");
        cache.put("short-lived-1", "Will expire soon");
        cache.put("short-lived-2", "Also expiring");
        cache.put("short-lived-3", "Me too");

        System.out.println("Current size: " + cache.size());

        // Wait 2 seconds (before expiration)
        System.out.println("\n[T=2s] Sleeping 2 seconds...");
        Thread.sleep(2000);

        // Try to get entries (lazy expiration check)
        System.out.println("Get 'short-lived-1': "
                + (cache.get("short-lived-1").isPresent() ? "FOUND" : "EXPIRED"));
        System.out.println("Current size: " + cache.size());

        // Wait 5 more seconds (past expiration + background cleanup)
        System.out.println("\n[T=7s] Sleeping 5 seconds (background cleanup should run)...");
        Thread.sleep(5000);

        // Background thread should have removed expired entries
        System.out.println("\nAfter background cleanup:");
        System.out.println("Current size: " + cache.size() + " (should be 0)");

        // Verify all gone
        System.out.println("Get 'short-lived-1': "
                + (cache.get("short-lived-1").isPresent() ? "FOUND" : "EXPIRED"));
        System.out.println("Get 'short-lived-2': "
                + (cache.get("short-lived-2").isPresent() ? "FOUND" : "EXPIRED"));
        System.out.println("Get 'short-lived-3': "
                + (cache.get("short-lived-3").isPresent() ? "FOUND" : "EXPIRED"));

        // IMPORTANT: Shutdown background thread
        cache.shutdown();

        System.out.println("\n✓ TTL Policy Demo Complete");
    }

    // ============================================================
    // Size-Based Policy Demonstration
    // ============================================================
    public static void demonstrateSizeBasedPolicy() {
        System.out.println("\n=== SIZE-BASED POLICY DEMONSTRATION ===");

        // Create size-based policy that wraps LRU
        // Evict when total weight > 10, use LRU to choose victim
        SizeBasedEvictionPolicy<String, String> sizePolicy =
                new SizeBasedEvictionPolicy<>(10, new LruEvictionPolicy<>());

        Cache<String, String> cache = CacheFactory.createInMemoryCache(sizePolicy, 100 // High entry
                                                                                       // count
                                                                                       // (won't
                                                                                       // trigger),
                                                                                       // weight
                                                                                       // will
                                                                                       // trigger
        );

        System.out.println("\nAdding entries with different weights:");

        // Note: Your current CacheEntry always uses weight=1
        // To properly demo this, you'd need to modify CacheEntry to accept custom weights
        // For now, this demonstrates the PATTERN even if weights are all 1

        cache.put("small-1", "data"); // weight=1
        cache.put("small-2", "data"); // weight=1
        cache.put("small-3", "data"); // weight=1
        System.out.println("Added 3 entries, total weight = 3");
        System.out.println("Current size: " + cache.size());

        // Add more entries
        for (int i = 4; i <= 12; i++) {
            cache.put("small-" + i, "data");
        }
        System.out.println("\nAdded 9 more entries, total weight = 12");
        System.out.println("Current size: " + cache.size());
        System.out.println("(Size-based policy triggered evictions when weight > 10)");

        // Verify oldest accessed items were evicted (LRU)
        System.out.println("\nChecking if oldest entries were evicted:");
        System.out.println("Get 'small-1': "
                + (cache.get("small-1").isPresent() ? "FOUND (unexpected)" : "EVICTED (expected)"));
        System.out.println(
                "Get 'small-12': " + (cache.get("small-12").isPresent() ? "FOUND (expected)"
                        : "EVICTED (unexpected)"));

        System.out.println("\n✓ Size-Based Policy Demo Complete");
        System.out.println("\nNOTE: To properly test weight-based eviction, you need to:");
        System.out.println("1. Modify CacheEntry to accept custom weights in constructor");
        System.out.println("2. Pass actual byte sizes as weights");
        System.out
                .println("3. Then large entries would trigger eviction sooner than small entries");
    }
}
