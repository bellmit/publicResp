package com.insta.hms.common;

/**
 * Used to define URL and jsp page paths in controller.
 */
public class URLRoute {

  /*
   * Access Control
   */
  public static final String INSUFFICIENT_PERMISSIONS_PAGE = "/pages/AccessControlForward";
  public static final String SESSION_URL = "session";
  public static final String SECURITY_URL = "security";

  /**
   * System Preferences (Generic Preferences).
   */
  public static final String SYSTEM_PREFERENCES = "/systempreferences";

  /**
   * Generic Preferences.
   */
  public static final String GENERIC_PREFERENCES = "/genericpreferences";

  /**
   * User Preferences.
   */
  public static final String USER_PREFERENCES = "/userpreferences";

  /**
   * Public APIs.
   */
  public static final String API_CUSTOMER = "/api/customer";

  /**
   * React template.
   */
  public static final String REACT_TEMPLATE = "/pages/index";

  /**
   * Documents type filter controller.
   */
  public static final String DOCUMENTS_TYPE_FILTER = "/filterdocs";

}
