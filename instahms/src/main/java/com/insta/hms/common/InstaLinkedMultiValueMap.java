package com.insta.hms.common;

import org.springframework.util.LinkedMultiValueMap;

import java.util.List;
import java.util.Map;

/**
 * The Class InstaLinkedMultiValueMap.
 *
 * @author ritolia
 * @param <K> the key type
 * @param <V> the value type
 */
@SuppressWarnings({ "unchecked", "serial" })
public class InstaLinkedMultiValueMap<K, V> extends LinkedMultiValueMap<K, V> {

  /**
   * Instantiates a new insta linked multi value map.
   *
   * @param otherMap the other map
   */
  public InstaLinkedMultiValueMap(Map<K, List<V>> otherMap) {
    super(otherMap);
  }

  /**
   * The method getFirstOrDefault. This method returns default value if key doesn't exits.
   *
   * @param key          the key
   * @param defaultValue the default value
   * @return the first or default
   */
  public V getFirstOrDefault(K key, Object defaultValue) {
    if (getFirst(key) != null && !getFirst(key).equals("")) {
      return getFirst(key);
    } else {
      return (V) defaultValue;
    }
  }
}