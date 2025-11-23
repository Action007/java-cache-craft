package com.cachecraft.proxy;

import java.util.Optional;
import java.util.function.Function;
import com.cachecraft.cache.Cache;

public class LazyLoadingCache<K, V> implements Cache<K, V> {
  private final Cache<K, V> delegate;
  private final Function<K, V> loader;

  public LazyLoadingCache(Cache<K, V> delegate, Function<K, V> loader) {
    this.delegate = delegate;
    this.loader = loader;
  }

  @Override
  public Optional<V> get(K key) {
    Optional<V> existing = delegate.get(key);
    if (existing.isPresent()) {
      return existing;
    }

    synchronized (this) {
      existing = delegate.get(key);
      if (existing.isPresent()) {
        return existing;
      }

      V value = loader.apply(key);
      if (value != null) {
        delegate.put(key, value);
        return Optional.of(value);
      }
      return Optional.empty();
    }
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
}
