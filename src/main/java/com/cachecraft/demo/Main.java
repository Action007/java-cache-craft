package com.cachecraft.demo;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.cachecraft.cache.Cache;
import com.cachecraft.decorator.MetricsCache;
import com.cachecraft.eviction.LfuEvictionPolicy;
import com.cachecraft.eviction.LruEvictionPolicy;
import com.cachecraft.factory.CacheFactory;
import com.cachecraft.proxy.LazyLoadingCache;
import com.cachecraft.store.CacheRegistry;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== STARTING CACHE PROJECT DEMO ===\n");

        // ============================================================
        // 1. Basic Operations & 2. LRU Eviction
        // ============================================================
        System.out.println("--- Scenario 1: LRU Eviction (Capacity 3) ---");
        // Factory: Create Memory Cache with LRU
        Cache<String, String> lruRaw =
                CacheFactory.createInMemoryCache(new LruEvictionPolicy<>(), 3);
        // Decorator: Wrap in Metrics
        MetricsCache<String, String> lruCache = new MetricsCache<>(lruRaw);
        // Registry: Register it
        CacheRegistry.INSTANCE.registerCache("LRU-Cache", lruCache);

        lruCache.put("A", "Alpha");
        lruCache.put("B", "Beta");
        lruCache.put("C", "Gamma");

        System.out.println("Filled cache with A, B, C.");
        lruCache.get("A"); // Access A. New order (Least to Most): B, C, A
        System.out.println("Accessed A. Expecting B to be evicted next.");

        lruCache.put("D", "Delta"); // Trigger eviction. B should go.

        Optional<String> resB = lruCache.get("B");
        Optional<String> resD = lruCache.get("D");

        System.out.println(
                "Get B (Should be empty): " + (resB.isPresent() ? "Found" : "Evicted (Correct)"));
        System.out.println(
                "Get D (Should be found): " + (resD.isPresent() ? "Found (Correct)" : "Missing"));
        System.out.println();

        // ============================================================
        // 3. LFU Eviction
        // ============================================================
        System.out.println("--- Scenario 2: LFU Eviction (Capacity 3) ---");
        MetricsCache<String, String> lfuCache =
                new MetricsCache<>(CacheFactory.createInMemoryCache(new LfuEvictionPolicy<>(), 3));
        CacheRegistry.INSTANCE.registerCache("LFU-Cache", lfuCache);

        lfuCache.put("A", "A-Val");
        lfuCache.put("B", "B-Val");
        lfuCache.put("C", "C-Val");

        // Access A 5 times, B 5 times, C 0 times
        for (int i = 0; i < 5; i++)
            lfuCache.get("A");
        for (int i = 0; i < 5; i++)
            lfuCache.get("B");

        System.out.println("Access Counts: A=5, B=5, C=0. Putting D...");
        lfuCache.put("D", "D-Val"); // Should evict C

        System.out.println("Get C (Should be empty): "
                + (lfuCache.get("C").isPresent() ? "Found" : "Evicted (Correct)"));
        System.out.println();

        // ============================================================
        // 4. TTL Expiration
        // ============================================================
        System.out.println("--- Scenario 3: TTL Expiration (2 Seconds) ---");
        Cache<String, String> ttlCache =
                CacheFactory.createTimedCache(new LruEvictionPolicy<>(), 2000, 5);
        ttlCache.put("Fast", "GoneSoon");

        System.out.println("Put 'Fast'. Sleeping 1 second...");
        Thread.sleep(1000);
        System.out.println("Get 'Fast' (Should exist): "
                + (ttlCache.get("Fast").isPresent() ? "Alive" : "Gone"));

        System.out.println("Sleeping 1.5 seconds...");
        Thread.sleep(1500);
        System.out.println("Get 'Fast' (Should be expired): "
                + (ttlCache.get("Fast").isPresent() ? "Alive" : "Expired (Correct)"));
        System.out.println();

        // ============================================================
        // 5. Lazy Loading (Proxy Pattern)
        // ============================================================
        System.out.println("--- Scenario 4: Lazy Loading ---");
        // Create a cache that loads from a 'database' if missing
        Cache<Integer, String> dbCache =
                CacheFactory.createInMemoryCache(new LruEvictionPolicy<>(), 10);

        LazyLoadingCache<Integer, String> lazyCache = new LazyLoadingCache<>(dbCache, (key) -> {
            System.out.println("   [DB] Fetching expensive value for ID " + key + "...");
            return "User-" + key;
        });

        System.out.println("Get ID 42 (First time): " + lazyCache.get(42).get());
        System.out.println("Get ID 42 (Second time): " + lazyCache.get(42).get()); // Should NOT
                                                                                   // print [DB]
        System.out.println();

        // ============================================================
        // 6. Concurrent Access
        // ============================================================
        System.out.println("--- Scenario 5: High Concurrency Test ---");
        MetricsCache<Integer, Integer> parallelCache =
                new MetricsCache<>(CacheFactory.createInMemoryCache(new LruEvictionPolicy<>(), 50));
        CacheRegistry.INSTANCE.registerCache("Concurrent-Cache", parallelCache);

        int threads = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);

        System.out.println("Spawning " + threads + " threads doing 1000 ops each...");
        for (int i = 0; i < threads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < 1000; j++) {
                        int key = j % 20; // High contention on 20 keys
                        parallelCache.put(key, threadId);
                        parallelCache.get(key);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();
        System.out.println("Concurrency test finished without exceptions.");
        System.out.println();

        // ============================================================
        // 7 & 8. Registry & Global Stats
        // ============================================================
        System.out.println("--- Final Scenario: Global Registry Stats ---");
        CacheRegistry.INSTANCE.displayAllStatistic();

        System.out.println("\n=== DEMO COMPLETE ===");
    }
}
