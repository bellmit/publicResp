package com.insta.instaapi.common;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

public class ServletContextUtil {
  /**
   * Get session parameter map.
   * @param ctx   Servlet context
   * @return      Session Map
   */
  public static Map<String, Object> getContextParametersMap(ServletContext ctx) {
    Map<String, Object> sessionMap = (Map<String, Object>) ctx.getAttribute("sessionMap");
    if (sessionMap == null || sessionMap.isEmpty()) {
      ctx.setAttribute("sessionMap", new HashMap<String, Object>());
      sessionMap = (Map<String, Object>) ctx.getAttribute("sessionMap");
    }
    return sessionMap;
  }
}
