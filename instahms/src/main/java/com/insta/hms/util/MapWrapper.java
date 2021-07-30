package com.insta.hms.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MapWrapper {

  protected Map dataMap = new HashMap();

  public Map getMap() {
    return Collections.unmodifiableMap(dataMap);
  }

  public void clear() {
    dataMap.clear();
  }

  public boolean containsKey(Object key) {
    return dataMap.containsKey(key);
  }

  public boolean containsValue(Object value) {
    return dataMap.containsValue(value);
  }

  public Set entrySet() {
    return dataMap.entrySet();
  }

  public Object get(Object key) {
    return dataMap.get(key);
  }

  public boolean isEmpty() {
    return dataMap.isEmpty();
  }

  public Set keySet() {
    return dataMap.keySet();
  }

  public Object put(Object key, Object value) {
    return dataMap.put(key, value);
  }

  public void putAll(Map map) {
    dataMap.putAll(map);
  }

  public Object remove(Object key) {
    return dataMap.remove(key);
  }

  public int size() {
    return dataMap.size();
  }

  public Collection values() {
    return dataMap.values();
  }

}
