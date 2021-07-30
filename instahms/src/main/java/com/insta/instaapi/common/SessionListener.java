package com.insta.instaapi.common;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

public class SessionListener implements HttpSessionListener {
  
  /**
   * Session listener initalization.
   * @param config    servlet configuration
   */
  public void init(ServletConfig config) {
  }

  /**
   * Session created event listener.
   * @param event session event
   */
  public void sessionCreated(HttpSessionEvent event) {
    HttpSession session = event.getSession();
    ServletContext context = session.getServletContext();
    Map<String, Object> sessionMap = (Map<String, Object>) context.getAttribute("sessionMap");
    if (sessionMap == null || sessionMap.isEmpty()) {
      context.setAttribute("sessionMap", new HashMap<String, Object>());
      sessionMap = (Map<String, Object>) context.getAttribute("sessionMap");
    }
  }

  /**
   * Session destroyed event listener.
   * @param event session event
   */
  public void sessionDestroyed(HttpSessionEvent event) {
    HttpSession session = event.getSession();
    ServletContext context = session.getServletContext();
    Map<String, Object> sessionMap = (Map<String, Object>) context.getAttribute("sessionMap");
  }
}
