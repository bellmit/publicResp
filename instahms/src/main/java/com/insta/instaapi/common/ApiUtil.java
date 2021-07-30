package com.insta.instaapi.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

public class ApiUtil {
  static Logger logger = LoggerFactory.getLogger(ApiUtil.class);

  /**
   * Get request handler key.
   * @param request   Request object
   * @return          Request handler key
   */
  public static String getRequestKey(HttpServletRequest request) {
    String requestHandalerKey = "";
    if (request.getParameter("request_handler_key") != null) {
      requestHandalerKey = request.getParameter("request_handler_key");
    } else {
      requestHandalerKey = request.getHeader("request_handler_key");
    }
    return requestHandalerKey;

  }

  /**
   * Get request handler key.
   * @param request   Request object
   * @return          Request handler key
   */
  public static String getRequestHandlerKey(HttpServletRequest request) {
    String requestHandlerKey = "";
    String requestUrl = request.getRequestURI();
    if ((requestUrl.contains("/api/print/") || requestUrl.contains("/api/message")) 
        && request.getParameter("request_handler_key") != null) {
      requestHandlerKey = request.getParameter("request_handler_key");
    } else if (request.getHeader("request_handler_key") != null) {
      requestHandlerKey = request.getHeader("request_handler_key");
    }
    return requestHandlerKey;

  }

  /**
   * Get crendetials from request.
   * @param request   Request object
   * @return          Request handler key
   */
  public static String getCredentials(HttpServletRequest request) {
    String userCredential = "";
    if (request.getParameter("customer_user_id") != null
        || request.getParameter("customer_user_password") != null) {
      userCredential = request.getParameter("customer_user_id") + ":"
          + request.getParameter("customer_user_password");
    } else {
      userCredential = request.getHeader("x-insta-auth");
    }
    return userCredential;

  }

}
