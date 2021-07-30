package com.insta.hms.common;

import org.apache.commons.lang3.ArrayUtils;

import java.util.List;
import java.util.Map;

/**
 * The Class CommonUtils.
 *
 * @author mithun.saha
 */
public class CommonUtils {

  /**
   * Gets the comma separated string.
   *
   * @param strArr the str arr
   * @return the comma separated string
   */
  public static String getCommaSeparatedString(String[] strArr) {
    String commaSepartedConductingRoles = "";
    if (strArr != null) {
      boolean first = true;
      for (String conductingRole : strArr) {
        if (!first) {
          commaSepartedConductingRoles += ",";
        }
        commaSepartedConductingRoles += conductingRole;
        first = false;
      }
    }
    return commaSepartedConductingRoles.equals("") ? null : commaSepartedConductingRoles;
  }

  /**
   * Gets the string array from comma separated string.
   *
   * @param str the str
   * @return the string array from comma separated string
   */
  public static String[] getStringArrayFromCommaSeparatedString(String str) {
    String[] strArr = null;
    if (str != null) {
      strArr = str.split(",");
    }
    return strArr;
  }

  /**
   * Gets the first non null.
   *
   * @param         <T> the generic type
   * @param objects the objects
   * @return the first non null
   */
  public static <T> T getFirstNonNull(T... objects) {
    if (!ArrayUtils.isEmpty(objects)) {
      for (T object : objects) {
        if (object != null) {
          return object;
        }
      }
    }
    return null;
  }

  /**
   * Checks if is integer.
   *
   * @param key the key
   * @return true, if is integer
   */
  public static boolean isInteger(String key) {
    if (key == null) {
      return false;
    }
    try {
      int value = Integer.parseInt(key);
    } catch (NumberFormatException nfe) {
      return false;
    }
    return true;
  }

  /**
   * Gets the value.
   *
   * @param key the key
   * @param inMap the in map
   * @return the value
   */
  public static Object getValue(String key, Map<String, Object> inMap) {
    Object obj = inMap;
    for (String token : key.split("\\.", -1)) {
      if (obj == null || token == null || token.isEmpty()) {
        obj = null;
        break;
      } else if (obj instanceof Map) {
        obj = ((Map) obj).get(token);
      } else if (obj instanceof List && isInteger(token)) {
        if (((List) obj).size() > Integer.parseInt(token)) {
          obj = ((List) obj).get(Integer.valueOf(token));
        }
      } else {
        obj = null;
        break;
      }
    }
    return obj;
  }
}
