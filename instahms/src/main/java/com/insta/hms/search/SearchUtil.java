package com.insta.hms.search;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * The Class SearchUtil.
 *
 * @author aditya get query parameters which are used to filter
 * @returns string of queryParams
 */
public class SearchUtil {

  /**
   * Gets the search criteria.
   *
   * @param params the params
   * @return the search criteria
   */
  @SuppressWarnings("rawtypes")
  public static String getSearchCriteria(Map<String, String[]> params) {

    StringBuilder queryParams = new StringBuilder();
    if (params != null && !params.isEmpty() && params.entrySet() != null
        && params.keySet() != null) {

      queryParams.append("?");
      Iterator<Entry<String, String[]>> it = params.entrySet().iterator();

      while (it.hasNext()) {

        Map.Entry pairs = it.next();

        String key = (String) pairs.getKey();
        Object[] val = (Object[]) pairs.getValue();

        if (key.contains("prgkey")) {
          continue;
        }

        if (val != null) {
          for (int i = 0; i < val.length; i++) {
            queryParams.append("&" + key + "=" + val[i].toString());
          }
        }

      }
    }
    return queryParams.toString();
  }
}
