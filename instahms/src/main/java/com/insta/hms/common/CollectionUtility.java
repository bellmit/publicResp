package com.insta.hms.common;

import java.util.Map;

/**
 * The Class CollectionUtility.
 */
public class CollectionUtility {

  /**
   * Instantiates a new collection utility.
   */
  private CollectionUtility() {}

  /**
   * Gets the value from map.
   *
   * @param <K> the key type
   * @param <V> the value type
   * @param map the map
   * @param key the key
   * @return the value from map
   */
  public static <K, V> V getValueFromMap(Map<K, Object> map, K key) {
    return (V) map.get(key);
  }

  /**
   * Gets the value from map or default.
   *
   * @param <K> the key type
   * @param <V> the value type
   * @param map the map
   * @param key the key
   * @param defaultValue the default value
   * @return the value from map or default
   */
  public static <K, V> V getValueFromMapOrDefault(Map<K, Object> map, K key, V defaultValue) {
    V value = (V) map.get(key);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }

  /**
   * Put if absent.
   *
   * @param <K> the key type
   * @param <V> the value type
   * @param map the map
   * @param keyToInsert the key to insert
   * @param valueToInsert the value to insert
   * @return true, if successful
   */
  public static <K, V> boolean putIfAbsent(Map<K, V> map, K keyToInsert, V valueToInsert) {
    boolean isInsertSuccess = false;
    if (!map.containsKey(keyToInsert)) {
      map.put(keyToInsert, valueToInsert);
      isInsertSuccess = true;
    }
    return isInsertSuccess;
  }
}
