package com.cachecraft.store;

import java.util.concurrent.ConcurrentHashMap;
import com.cachecraft.cache.Cache;
import com.cachecraft.decorator.MetricsCache;

public enum CacheRegistry {
  INSTANCE;

  private final ConcurrentHashMap<String, Cache<?, ?>> registry = new ConcurrentHashMap<>();

  public void registerCache(String name, Cache<?, ?> cache) {
    registry.put(name, cache);
  }

  public Cache<?, ?> getCache(String name) {
    return registry.get(name);
  }

  public void displayAllStatistic() {
    System.out.println("=== Global Cache Statistics ===");
    registry.forEach((name, cache) -> {
      if (cache instanceof MetricsCache) {
        System.out.printf("Cache [%s]: %s%n", name, ((MetricsCache<?, ?>) cache).getStatistics());
      } else {
        System.out.printf("Cache [%s]: No metrics available (Not a MetricsCache)%n", name);
      }
    });
    System.out.println("===============================");
  }
}
