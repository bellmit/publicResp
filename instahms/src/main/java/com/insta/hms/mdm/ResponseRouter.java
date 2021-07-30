package com.insta.hms.mdm;

import com.insta.hms.common.StringUtil;

/**
 * 
 * @author yashwant
 * 
 *         This classes is used for defining a path for jsp resources and used it in controller for
 *         sending response.
 *
 */

public class ResponseRouter {

  private static final String BASE_VIEW_PATH = "/pages/";
  private static final String REDIRECT_VIEW_PREFIX = "redirect:";
  // private static final String VIEW_PAGE_SUFFIX = ".htm";

  private String pagePath = "";
  private String pathElement = "";
  private String searchUrl = "";
  private String searchPage = "";
  private String showUrl = "";
  private String showPage = "";
  private String addUrl = "";
  private String addPage = "";
  private String createUrl = "";
  private String createOkRedirect = "";
  private String createErrorRedirect = "";
  private String updateUrl = "";
  private String updateOkRedirect = "";
  private String updateErrorRedirect = "";
  private String deleteUrl = "";
  private String deleteOkRedirect = "";
  private String deleteErrorRedirect = "";

  private ResponseRouter(String pagePath, String pathElement, String searchUrl, String searchPage,
      String showUrl, String showPage, String addUrl, String addPage, String createUrl,
      String createOkRedirect, String createErrorRedirect, String updateUrl,
      String updateOkRedirect, String updateErrorRedirect, String deleteUrl,
      String deleteOkRedirect, String deleteErrorRedirect) {
    this.pagePath = pagePath;
    this.pathElement = pathElement;

    this.searchUrl = searchUrl;
    this.searchPage = searchPage;

    this.showUrl = showUrl;
    this.showPage = showPage;

    this.addUrl = addUrl;
    this.addPage = addPage;

    this.createUrl = createUrl;
    this.createOkRedirect = createOkRedirect;
    this.createErrorRedirect = createErrorRedirect;

    this.updateUrl = updateUrl;
    this.updateOkRedirect = updateOkRedirect;
    this.updateErrorRedirect = updateErrorRedirect;

    this.deleteUrl = deleteUrl;
    this.deleteOkRedirect = deleteOkRedirect;
    this.deleteErrorRedirect = deleteErrorRedirect;

  }

  private ResponseRouter(String pagePath, String pathElement, String searchUrl, String showUrl,
      String addUrl, String createUrl, String updateUrl, String deleteUrl) {
    this(pagePath, pathElement, searchUrl, searchUrl, showUrl, showUrl, addUrl, addUrl, createUrl,
        showUrl, searchUrl, updateUrl, showUrl, showUrl, deleteUrl, searchUrl, showUrl);
  }

  private ResponseRouter(String pagePath, String pathElement, String searchUrl, String showUrl,
      String addUrl) {
    this(pagePath, pathElement, searchUrl, showUrl, addUrl, "create", "update", "delete");
  }

  protected ResponseRouter(String pagePath, String pathElement) {
    this(pagePath, pathElement, "list", "show", "add");
  }

  protected String routeSuccess(String action) {

    if ("list".equalsIgnoreCase(action)) {
      return routePage(searchPage);
    }

    if ("show".equalsIgnoreCase(action)) {
      return routePage(showPage);
    }

    if ("add".equalsIgnoreCase(action)) {
      return routePage(addPage);
    }

    if ("create".equalsIgnoreCase(action)) {
      return routeRedirect(createOkRedirect);
    }

    if ("update".equalsIgnoreCase(action)) {
      return routeRedirect(updateOkRedirect);
    }

    if ("delete".equalsIgnoreCase(action)) {
      return routeRedirect(deleteOkRedirect);
    }

    return "";
  }

  protected String routeError(String action) {

    if ("search".equalsIgnoreCase(action)) {
      return routePage(searchPage);
    }

    if ("show".equalsIgnoreCase(action)) {
      return routePage(showPage);
    }

    if ("add".equalsIgnoreCase(action)) {
      return routePage(showPage);
    }

    if ("create".equalsIgnoreCase(action)) {
      return routeRedirect(createErrorRedirect);
    }

    if ("update".equalsIgnoreCase(action)) {
      return routeRedirect(updateErrorRedirect);
    }

    if ("delete".equalsIgnoreCase(action)) {
      return routeRedirect(deleteErrorRedirect);
    }

    return "";
  }

  protected String routeRedirect(String route) {
    return REDIRECT_VIEW_PREFIX + route;
  }

  protected String routePage(String route) {
    if (null != pagePath && !pagePath.isEmpty()) {
      String jspPath = BASE_VIEW_PATH + pagePath;
      return StringUtil.makeURLPath(new String[] { jspPath, this.pathElement, route });
    }

    return StringUtil.makeURLPath(new String[] { BASE_VIEW_PATH, this.pathElement, route });
  }

  // TODO : consider making the return value a ResponseView so that
  // it scales better. In that case, parameter should
  // include a Model or its derivative so that the model data can be put in the view
  // For now, we leave it as a string so that the caller can use it as a
  // viewName

  /**
   * Route.
   *
   * @param action the action
   * @param success the success
   * @return the string
   */
  public String route(String action, boolean success) {
    if (success) {
      return routeSuccess(action);
    } else {
      return routeError(action);
    }
  }

  // TODO : Same as route(action, success)
  public String route(String action) {
    return route(action, true);
  }

}
