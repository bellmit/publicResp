package com.insta.hms.common;

import java.util.Map;

/**
 * The Class TypeAwareMap. Currently supports only get and put operations. Extend if used
 * extensively with ForwardingClass.
 * Thread safety depends on the passed Map instance.
 * Inferring type is referred as I 
 * 
 * @param <K> the key type
 * @param <V> the value type
 */
public class TypeAwareMap<K, V> {

  /** The map. */
  private Map<K, V> map;

  /**
   * Instantiates a new type aware map. Mutates the underlying map with its useful wrappers.
   * 
   * @param map the map
   */
  public TypeAwareMap(Map<K, V> map) {
    this.map = map;
  }

  /**
   * Gets the.
   *
   * @param <I> the generic type
   * @param key the key
   * @return the i
   */
  public <I> I get(K key) {
    return (I) map.get(key);
  }

  /**
   * Gets the or default.
   *
   * @param <I> the generic type
   * @param key the key
   * @param defaultValue the default value
   * @return the or default
   */
  public <I> I getOrDefault(K key, I defaultValue) {
    I value = (I) map.get(key);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }

  /**
   * Put.
   *
   * @param key the key
   * @param value the value
   * @return the v
   */
  public V put(K key, V value) {
    return map.put(key, value);
  }

  /**
   * Put if absent.
   *
   * @param key the key
   * @param value the value
   * @return the v
   */
  public V putIfAbsent(K key, V value) {
    if (!map.containsKey(key)) {
      return map.put(key, value);
    }
    return null;
  }

  public <I> I remove(K key) {
    return (I) map.remove(key);
  }
}
