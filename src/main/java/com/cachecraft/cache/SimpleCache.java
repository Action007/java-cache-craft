package com.cachecraft.cache;

import java.util.HashMap;
import java.util.Optional;
import com.cachecraft.entry.CacheEntry;

public class SimpleCache<K, V> implements Cache<K, V> {
  HashMap<K, CacheEntry<V>> cache = new HashMap<>();

  @Override
  public Optional<V> get(K key) {
    CacheEntry<V> cacheEntry = cache.get(key);
    if (cacheEntry != null) {
      cacheEntry.recordAccess();
      return Optional.of(cacheEntry.getValue());
    }
    return Optional.empty();
  }

  @Override
  public void put(K key, V value) {
    cache.put(key, new CacheEntry<V>(value));
  }

  @Override
  public void remove(K key) {
    cache.remove(key);
  }

  @Override
  public void clearAll() {
    cache.clear();
  }

  @Override
  public int size() {
    return cache.size();
  }

  @Override
  public boolean containsKey(K key) {
    return cache.containsKey(key);
  }

}
