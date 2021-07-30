package com.insta.hms.search;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The Class SearchHelper.
 *
 * @author krishna.t
 */
public class SearchHelper {

  /**
   * Gets the search criteria.
   *
   * @param params the params
   * @return the search criteria
   */
  public static String getSearchCriteria(Map params) {
    List<String> excludeParams = Arrays.asList(new String[] {
        "_mysearch", "_search_name", "_actionId", "_method","_searchMethod", "prgkey"});
    StringBuilder queryParams = new StringBuilder();
    if (params != null && !params.isEmpty() && params.entrySet() != null
        && params.keySet() != null) {

      String methodName = ((Object[]) params.get("_searchMethod"))[0].toString();
      queryParams.append("?_method=" + methodName);
      Iterator it = params.entrySet().iterator();

      while (it.hasNext()) {
        Map.Entry pairs = (Map.Entry) it.next();
        String key = (String) pairs.getKey();
        Object[] val = (Object[]) pairs.getValue();
        if (excludeParams.contains(key)) {
          continue;
        }

        if (val != null) {
          for (int i = 0; i < val.length; i++) {
            String value = (String) val[i];
            try {
              value = URLEncoder.encode(value.toString(), StandardCharsets.UTF_8.toString());
            } catch (Exception ex) {
              value = (String) val[i];
            }
            queryParams.append("&" + key + "=" + value);
          }
        }

      }

    }
    return queryParams.toString();
  }

}
