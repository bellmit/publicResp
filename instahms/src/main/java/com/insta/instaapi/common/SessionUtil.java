package com.insta.instaapi.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

/**
 * @author mithun.saha
 *
 */
public class SessionUtil {
  static Logger logger = LoggerFactory.getLogger(SessionUtil.class);

  public static HttpSession getSession(String sessionId, ServletContext ctx) {
    Map<String, Object> sessionMap = (Map<String, Object>) ctx.getAttribute("sessionMap");
    return (HttpSession) sessionMap.get(sessionId);
  }
}
