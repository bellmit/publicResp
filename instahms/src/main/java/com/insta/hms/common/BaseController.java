package com.insta.hms.common;

import com.insta.hms.common.annotations.LazyAutowired;
import flexjson.JSONSerializer;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * The Class BaseController which is should be one of the parent classes for all controllers
 * implementing access control using actionId and authModes.
 */
public class BaseController {

  /** The action id. */
  private String actionId;

  /** The auth mode. */
  private String authMode;

  @LazyAutowired
  private SentryUiConfig sentryUiConfig;

  private static JSONSerializer jsonSerializer = new JSONSerializer().exclude("class");

  /**
   * Gets the parameter map.
   *
   * @param req the req
   * @return the parameter map
   */
  protected Map<String, Object> getParameterMap(HttpServletRequest req) {

    Map<String, String[]> params = req.getParameterMap();
    Map<String, String[]> arrValues = new HashMap<String, String[]>();
    Map<String, Object> scalars = new HashMap<String, Object>();
    Map<String, Object> out = new HashMap<String, Object>();
    if (null != params && params.size() > 0) {
      for (Map.Entry<String, String[]> entry : params.entrySet()) {
        String[] value = entry.getValue();
        if (null != value && value.length > 1) {
          arrValues.put(entry.getKey(), entry.getValue());
        } else {
          scalars.put(entry.getKey(), value[0]);
        }
      }
    }

    out.putAll(arrValues);
    out.putAll(scalars);
    return out;
  }

  /**
   * Gets the parameter.
   *
   * @param params the params
   * @param key    the key
   * @return the parameter
   */
  public String getParameter(Map params, String key) {
    Object[] obj = (Object[]) params.get(key);
    if (obj == null || obj[0] == null) {
      return null;
    }
    return obj[0].toString();
  }

  /**
   * Gets the files present in the form data of the request.
   *
   * @param request the request
   * @return the map of MultiPartFiles
   */
  protected Map<String, MultipartFile> getFiles(HttpServletRequest request) {
    Map<String, MultipartFile> fileMap = null;
    if (request instanceof DefaultMultipartHttpServletRequest) {
      fileMap = ((DefaultMultipartHttpServletRequest) request).getFileMap();
    }
    return fileMap;
  }

  /**
   * Gets the all file present in the form data of the request.
   *
   * @param request the request
   * @return the map of MultiPartFiles
   */
  protected MultiValueMap<String, MultipartFile> getAllFiles(HttpServletRequest request) {
    MultiValueMap<String, MultipartFile> fileMap = null;
    if (request instanceof DefaultMultipartHttpServletRequest) {
      fileMap = ((DefaultMultipartHttpServletRequest) request).getMultiFileMap();
    }
    return fileMap;
  }

  /**
   * Sets the action id.
   *
   * @param actionId the new action id
   */
  public void setActionId(String actionId) {
    this.actionId = actionId;
  }

  /**
   * Gets the action id.
   *
   * @return the action id
   */
  public String getActionId() {
    return actionId;
  }

  /**
   * Sets the auth mode.
   *
   * @param authMode the new auth mode
   */
  public void setAuthMode(String authMode) {
    this.authMode = authMode;
  }

  /**
   * Gets the auth mode.
   *
   * @return the auth mode
   */
  public String getAuthMode() {
    return authMode;
  }

  /** The screen id. */
  private String screenId;

  /**
   * Gets the screen id.
   *
   * @return the screen id
   */
  public String getScreenId() {
    return screenId;
  }

  /**
   * Sets the screen id.
   *
   * @param screenId the new screen id
   */
  public void setScreenId(String screenId) {
    this.screenId = screenId;
  }

  protected ModelAndView renderUi(String title, String bundle, String decorator, String referrer,
      Map<String, String> globalVars, boolean hasFroala, boolean isSettingsPage) {
    ModelAndView mav = new ModelAndView(URLRoute.REACT_TEMPLATE);
    mav.addObject("title", title);
    mav.addObject("bundle", bundle);
    mav.addObject("decorator", decorator);
    mav.addObject("referrer", referrer);
    mav.addObject("globalVars", globalVars);
    mav.addObject("hasFroala", hasFroala);
    mav.addObject("isSettingsPage", isSettingsPage);
    Map<String, String> sentryConfig = new HashMap<>();
    sentryConfig.put("dsn", sentryUiConfig.getDsn());
    sentryConfig.put("environment", sentryUiConfig.getEnvironment());
    sentryConfig.put("release", sentryUiConfig.getRelease());
    Map<String, Object> sentryOptions = new HashMap<>();
    sentryOptions.put("config", sentryConfig);
    Map<String, Object> sentryTags = new HashMap<>();
    sentryTags.put("servername", sentryUiConfig.getServername());
    sentryOptions.put("tags", sentryTags);
    if (sentryUiConfig.hasDsn()) {
      sentryOptions.put("enabled", true);
    } else {
      sentryOptions.put("enabled", false);
    }
    mav.addObject("sentryOptions", jsonSerializer.serialize(sentryOptions));
    return mav;
  }

  protected ModelAndView renderMasterUi(String title, String bundle, String decorator,
      boolean hasFroala) {
    return renderUi(title, bundle, decorator, null, new HashMap<String, String>(), hasFroala, true);
  }

  protected ModelAndView renderMasterUi(String title, String bundle, boolean hasFroala) {
    return renderUi(title, bundle, "reactDecorator", null, new HashMap<String, String>(), hasFroala,
        true);
  }

  protected ModelAndView renderMasterUi(String title, String bundle, String decorator) {
    return renderUi(title, bundle, decorator, null, new HashMap<String, String>(), false, true);
  }

  protected ModelAndView renderMasterUi(String title, String bundle) {
    return renderMasterUi(title, bundle, false);
  }

  protected ModelAndView renderMasterUi(String title, String bundle,
      Map<String, String> globalVars) {
    return renderUi(title, bundle, "reactDecorator", null, globalVars, false, true);
  }

  protected ModelAndView renderFlowUi(String title, String bundle, String activityPaneLayout,
      String flowType, String activity, boolean hasFroala, String referrer) {
    Map<String, String> globalVars = new HashMap<>();
    globalVars.put("activity", activity);
    globalVars.put("activityPaneLayout", activityPaneLayout);
    globalVars.put("flowType", flowType);
    return renderUi(title, bundle, "reactDecorator", referrer, globalVars, hasFroala, false);
  }

  protected ModelAndView renderFlowUi(String title, String bundle, String activityPaneLayout,
      String flowType, String activity, boolean hasFroala) {
    return renderFlowUi(title, bundle, activityPaneLayout, flowType, activity, hasFroala, null);
  }

}
