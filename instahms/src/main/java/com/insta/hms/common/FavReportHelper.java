package com.insta.hms.common;

import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

/**
 * The Class FavReportHelper.
 */
/*
 * For creating a map directly from a query string, try jlibs.core.net.URLUtil(String queryParams,
 * String encoding)
 */
public class FavReportHelper {

  /**
   * Gets the report params.
   *
   * @param params the params
   * @return the report params
   */
  public static String getReportParams(Map params) {
    StringBuilder queryParams = new StringBuilder();
    if (!(params == null || params.isEmpty() || params.entrySet() == null
        || params.keySet() == null)) {
      String methodName = ((Object[]) params.get("_searchMethod"))[0].toString();
      queryParams.append("?method=" + methodName);
      Iterator it = params.entrySet().iterator();

      while (it.hasNext()) {
        Map.Entry pairs = (Map.Entry) it.next();
        String key = (String) pairs.getKey();
        Object[] vals = (Object[]) pairs.getValue();
        if (key.startsWith("_") || key.contains("prgkey")) {
          continue;
        }

        if (vals != null) {
          for (int i = 0; i < vals.length; i++) {
            String val = (String) vals[i];
            if (!val.isEmpty()) {
              try {
                queryParams.append("&" + key + "=" + URLEncoder.encode(val, "UTF-8"));
              } catch (java.io.UnsupportedEncodingException exception) {
                // ignore the param, continue with the rest
              }
            }
          }
        }
      }
    }
    return queryParams.toString();
  }
}
