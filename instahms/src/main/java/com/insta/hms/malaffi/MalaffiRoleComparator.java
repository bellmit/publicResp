package com.insta.hms.malaffi;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class MalaffiRoleComparator implements Comparator<String> {

  private static final Map<String, Integer> ROLE_PRIORITY = new HashMap<>();
  static {
    ROLE_PRIORITY.put("P", 0);
    ROLE_PRIORITY.put("S", 1);
    ROLE_PRIORITY.put("T", 2);
    ROLE_PRIORITY.put("F", 3);
    ROLE_PRIORITY.put(null, 4);
  }

  @Override
  public int compare(String o1, String o2) {
    return ROLE_PRIORITY.get(o1) - ROLE_PRIORITY.get(o2);
  }

}