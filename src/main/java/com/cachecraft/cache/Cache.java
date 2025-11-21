package com.cachecraft.cache;

import java.util.Optional;

public interface Cache<K, V> {
  Optional<V> get(K key);

  void put(K key, V value);

  void remove(K key);

  void clearAll();

  int size();

  boolean containsKey(K key);
}
