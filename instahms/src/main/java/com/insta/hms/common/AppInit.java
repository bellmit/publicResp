package com.insta.hms.common;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.bob.hms.common.ScreenConfig;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;

import freemarker.template.Configuration;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.BigDecimalConverter;
import org.apache.commons.beanutils.converters.IntegerConverter;
import org.apache.commons.beanutils.converters.SqlDateConverter;
import org.apache.commons.beanutils.converters.SqlTimeConverter;
import org.apache.commons.beanutils.converters.SqlTimestampConverter;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.action.PlugIn;
import org.apache.struts.config.ActionConfig;
import org.apache.struts.config.ModuleConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * The Class AppInit. An application initializer: this is called when the servlet is initialized. We
 * do initialization like storing of application wide properties in a a convenient format.
 * 
 * @author deepak_kk
 */
public class AppInit implements PlugIn {

  /** The log. */
  static Logger log = LoggerFactory.getLogger(AppInit.class);

  /** The fm config. */
  private static Configuration fmConfig = null;

  /** The context. */
  public static ServletContext context = null;

  /** The root real path. */
  public static String rootRealPath = null;

  /** The action url map. */
  private static Map actionUrlMap = new HashMap(); // actionId => URL

  /** The screen action map. */
  private static Map screenActionMap = new HashMap(); // screenId =>
  // [actionId,...] (only
  /** The action screen map. */
  // for mult)
  private static Map actionScreenMap = new HashMap(); // actionId => screenId

  /** The privileged actions. */
  // (only for mult)
  private static List privilegedActions = new ArrayList(); // instaOnly

  /** The confidential filter white list. */
  private static Set<String> confidentialFilterWhiteList = new HashSet<>();

  /** The confidential filter regex patterns. */
  private static Set<Pattern> confidentialFilterRegexPatterns = new HashSet<>();

  /** The action mapping pattern. */
  private static String ACTION_MAPPING_PATTERN = ".do";

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.struts.action.PlugIn#init(org.apache.struts.action.ActionServlet,
   * org.apache.struts.config.ModuleConfig)
   */
  @Override
  public void init(ActionServlet servlet, ModuleConfig config) throws ServletException {
    // Used to maintain list of migrated actions
    initializeConverters();
    context = servlet.getServletContext();
    fmConfig = new Configuration();
    fmConfig.setServletContextForTemplateLoading(context, "WEB-INF/templates");
    rootRealPath = context.getRealPath("");
    log.info("Initializing application, root real path is: " + rootRealPath);

    boolean configError = false;

    for (ActionConfig ac : config.findActionConfigs()) {
      String migrated = ac.getProperty("migrated");
      addStrutsUrlToConfidentialFilterWhiteList(ac, confidentialFilterWhiteList);
      if (null != migrated && migrated.equalsIgnoreCase("true")) {
        continue;
      }

      String url = ac.getPath().startsWith("/")
          ? (ac.getPath().substring(1) + ACTION_MAPPING_PATTERN)
          : (ac.getPath() + ACTION_MAPPING_PATTERN);

      String actionId = ac.getProperty("action_id");
      String authMode = ac.getProperty("auth_mode");

      if ((authMode == null) || !authMode.equals("passthru")) {
        // Sanity checks: ensure that action_id is present for every
        // action
        // unless it is a passthru: whereby actionId is not required.
        if (actionId == null) {
          configError = true;
          log.error("Missing action_id for: " + ac.getPath() + " in struts-config.xml");
        } else if (actionUrlMap.get(actionId) != null) {
          configError = true;
          log.error("Duplicate action_id " + actionId + "(" + url + ", "
              + actionUrlMap.get(actionId) + ")" + " in struts-config.xml");
        }
      }

      if (actionId != null) {
        actionUrlMap.put(actionId, url);
      }

      String screenId = ac.getProperty("screen_id");
      if (null != screenId) {
        // we have a screen id, which means it has multiple actions for
        // the screen
        if (screenActionMap.containsKey(screenId)) {
          List actionList = (List) screenActionMap.get(screenId);
          actionList.add(actionId);
          screenActionMap.put(screenId, actionList);
        } else {
          List newActionList = new ArrayList();
          newActionList.add(actionId);
          screenActionMap.put(screenId, newActionList);
        }
        actionScreenMap.put(actionId, screenId);
      }

      if (null != authMode && authMode.equals("instaonly")) {
        // This is really a small list right now, we dont need a hash
        // map.
        // We can just use list.contains() to check if an action is
        // privileged
        privilegedActions.add(actionId);
        log.debug("Adding privileged action : " + actionId);
      }

      if (null == authMode || (!authMode.equals("instaonly")) && (!authMode.equals("passthru"))) {
        // If it is not a passthru/privileged, it should have been
        // configured in the screens.xml
        ScreenConfig sc = (ScreenConfig) servlet.getServletContext().getAttribute("screenConfig");
        if (null == screenId) {
          screenId = actionId;
        }
        if (null != screenId && null == sc.getScreen(screenId)) {
          configError = true;
          log.error("Missing screen_id for: " + screenId + " in screens.xml");
        }
        if (screenId != null && screenId.length() > 40) {
          log.error("The length of Screen ID: " + screenId + " is greater than 40...\n");
          configError = true;
        }
      } else {
        // collect all passthru actions under one screen: passthru. This
        // is for easy
        // mapping of the action to a module: these are not associated
        // to any module
        actionScreenMap.put(actionId, "passthru");
      }
    }

    List<String> migratedActions = new ArrayList<String>();
    boolean springConfigurationError = addSpringPathsToActionURLMap(migratedActions);
    springConfigurationError = springConfigurationError
        || createSpringWhiteList(confidentialFilterWhiteList, confidentialFilterRegexPatterns);
    if (configError != true && springConfigurationError != true) {
      // We need to save the map in the app context - will be used for
      // menu generation
      servlet.getServletContext().setAttribute("actionUrlMap", actionUrlMap);
      // This will be used for persisting the permissions when the user
      // saves data for a screen
      servlet.getServletContext().setAttribute("screenActionMap", screenActionMap);
      servlet.getServletContext().setAttribute("actionScreenMap", actionScreenMap);
      // It is ok right now, if we end up maintaining a lot of such
      // structures related to access control,
      // it may be better to keep a action_id => action_config map.
      servlet.getServletContext().setAttribute("privilegedActions", privilegedActions);
      servlet.getServletContext().setAttribute("migratedActions", migratedActions);
      servlet.getServletContext().setAttribute("confidentialityWhiteListedUrlList",
          confidentialFilterWhiteList);
    }
    // else, the above will not be set and we won't get a menu.

    // to reset insurance_submission_batch job when tomcat is restarted
    resetClaimSubmissionBatchStatus();
    // to reset the selfpayXMLjob while restarting tomcat.
    resetSelfpayBatchStatus();

  }

  /**
   * Adds the struts url to confidential filter white list.
   *
   * @param actionConfig        the action config
   * @param confidentialUrlList the confidential url list
   */
  private void addStrutsUrlToConfidentialFilterWhiteList(ActionConfig actionConfig,
      Set<String> confidentialUrlList) {
    if (actionConfig.getType() == null) {
      return;
    }
    try {
      Class<?> clazz = Class.forName(actionConfig.getType());
      String superClassName = clazz.getSuperclass().getName();
      Method[] methods = clazz.getDeclaredMethods();
      for (Method method : methods) {
        if (method.isAnnotationPresent(IgnoreConfidentialFilters.class)) {
          String urlWithMethod = null;
          if (superClassName.endsWith("BaseAction") || superClassName.endsWith("DispatchAction")
              || superClassName.endsWith("IpBedAction")) {
            StringBuilder url = new StringBuilder(actionConfig.getPath() + ACTION_MAPPING_PATTERN);
            urlWithMethod = url.append("?" + actionConfig.getParameter() + "=" + method.getName())
                .toString();
          } else if (method.getName().equals("execute") && superClassName.endsWith("Action")) {
            urlWithMethod = actionConfig.getPath() + ACTION_MAPPING_PATTERN;
          }
          if (urlWithMethod != null) {
            confidentialUrlList.add(urlWithMethod);
          }
        }
      }
    } catch (ClassNotFoundException exception) {
      log.warn("Unable to find class for actionConfig:" + actionConfig.toString());
      return;
    }
  }

  /**
   * Creates the spring white list.
   *
   * @param confidentialFilterSpringWhiteList the confidential filter spring white list
   * @param regexPatterns                     the regex patterns
   * @return the boolean
   */
  private Boolean createSpringWhiteList(Set<String> confidentialFilterSpringWhiteList,
      Set<Pattern> regexPatterns) {
    ConfigurableApplicationContext applicationContext = getSpringApplicationContext();
    ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();
    for (String beanName : applicationContext.getBeanDefinitionNames()) {
      if (beanName.endsWith("Controller")) {
        Class<?> controllerClass = beanFactory.getType(beanName);
        RequestMapping classRequestMappingAnnotation = AnnotationUtils
            .findAnnotation(controllerClass, RequestMapping.class);
        String[] classRequestMappings = null;
        if (classRequestMappingAnnotation != null) {
          classRequestMappings = classRequestMappingAnnotation.value();
        } else {
          log.warn("Unable to find request mapping for bean:" + beanName);
        }
        Method[] methods = controllerClass.getMethods();
        RequestMapping methodRequestMappingAnnotation = null;
        for (Method method : methods) {
          if (method.isAnnotationPresent(IgnoreConfidentialFilters.class)) {
            if (method.isAnnotationPresent(RequestMapping.class)) {
              methodRequestMappingAnnotation = AnnotationUtils.findAnnotation(method,
                  RequestMapping.class);
            } else if (method.isAnnotationPresent(GetMapping.class)) {
              GetMapping getMapping = AnnotationUtils.findAnnotation(method, GetMapping.class);
              String[] mappings = getMapping.value();
              methodRequestMappingAnnotation = synthesizeRequestMappingAnnotation(mappings);
            } else if (method.isAnnotationPresent(DeleteMapping.class)) {
              DeleteMapping deleteMapping = AnnotationUtils.findAnnotation(method,
                  DeleteMapping.class);
              String[] mappings = deleteMapping.value();
              methodRequestMappingAnnotation = synthesizeRequestMappingAnnotation(mappings);
            } else if (method.isAnnotationPresent(PostMapping.class)) {
              PostMapping postMapping = AnnotationUtils.findAnnotation(method, PostMapping.class);
              String[] mappings = postMapping.value();
              methodRequestMappingAnnotation = synthesizeRequestMappingAnnotation(mappings);
            } else if (method.isAnnotationPresent(PutMapping.class)) {
              PutMapping putMapping = AnnotationUtils.findAnnotation(method, PutMapping.class);
              String[] mappings = putMapping.value();
              methodRequestMappingAnnotation = synthesizeRequestMappingAnnotation(mappings);
            } else {
              log.error("Unable to find request mapping for:" + method.toString() + "in bean:"
                  + beanName);
              return true;
            }
            if (methodRequestMappingAnnotation != null) {
              String[] methodRequestMappings = methodRequestMappingAnnotation.value();
              for (String classRequestMapping : classRequestMappings) {
                if (methodRequestMappings.length == 0) {
                  confidentialFilterSpringWhiteList.add("/" + classRequestMapping);
                }
                for (String methodRequestMapping : methodRequestMappings) {
                  if (StringUtil.isNullOrEmpty(methodRequestMapping)) {
                    confidentialFilterSpringWhiteList.add(classRequestMapping);
                    continue;
                  }
                  if (!methodRequestMapping.startsWith("/")) {
                    methodRequestMapping = "/" + methodRequestMapping;
                  }
                  confidentialFilterSpringWhiteList.add(classRequestMapping + methodRequestMapping);
                }
              }
            }
          }
        }
      }
    }
    return false;
  }

  /**
   * Synthesize request mapping annotation.
   *
   * @param mapping the mapping
   * @return the request mapping
   */
  private RequestMapping synthesizeRequestMappingAnnotation(String[] mapping) {
    Map<String, Object> valueMap = new HashMap<>();
    valueMap.put("value", mapping);
    return AnnotationUtils.synthesizeAnnotation(valueMap, RequestMapping.class, null);
  }

  /**
   * Adds the spring paths to action URL map.
   *
   * @param migratedActionIDs the migrated action I ds
   * @return true, if successful
   */
  /*
   * Adds paths for Migrated Actions by adding a mapping from action IDs to paths. The steps for
   * getting the path for a migrated screen are : 1) Get bean factory from Application Context. 2)
   * If bean ends with controller, bean needs a path. 3) Get Action ID and Authentication mode from
   * Bean Definition. 4) IF ACTION ID IS NOT PROVIDED OR ACTION ID FOR A MIGRATED SCREEN HAS NOT
   * BEEN REMOVED HOME ACTION WILL THROW A NPE SINCE THE MAPPING PROCESS WILL FAIL AND ANY OF THE
   * URL MAPS WILL NOT BE PRESENT IN SERVLET CONTEXT. This is not applicable for passthru. 5) Get
   * class level Request Mapping Annotation and add to action url map.
   *
   */
  private boolean addSpringPathsToActionURLMap(List<String> migratedActionIDs) {
    boolean error = false;
    ConfigurableApplicationContext applicationContext = getSpringApplicationContext();
    ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();
    for (String beanName : applicationContext.getBeanDefinitionNames()) {
      if (beanName.endsWith("Controller")) {
        BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
        MutablePropertyValues properties = beanDefinition.getPropertyValues();
        PropertyValue actionIDProperty = properties.getPropertyValue("actionId");
        PropertyValue screenIDProperty = properties.getPropertyValue("screenId");
        PropertyValue authModeProperty = properties.getPropertyValue("authMode");

        String actionID = extractValueFromPropertyValue(actionIDProperty);
        String screenID = extractValueFromPropertyValue(screenIDProperty);
        String authMode = extractValueFromPropertyValue(authModeProperty);

        if ((authMode == null) || !authMode.equals("passthru")) {
          if (actionID == null) {
            error = true;
            log.error(
                "Missing action_id for Spring controller class :" + beanName + "configuration xml");
          } else if (actionUrlMap.containsKey(actionID)) {
            error = true;
            log.error("Duplicate action_id " + actionID + "(" + beanName + ", "
                + actionUrlMap.get(actionID) + ")" + " in Spring XML configuration");
          }
        }

        RequestMapping annotation = beanFactory.findAnnotationOnBean(beanName,
            RequestMapping.class);
        String relativePath = null;
        if (null != annotation && null != annotation.path() && annotation.path().length > 0) {
          relativePath = annotation.path()[0];
          String url = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
          // url = url.concat(".htm");

          if (actionID != null) {
            migratedActionIDs.add(actionID);
            actionUrlMap.put(actionID, url);
          }

          if (authMode != null && authMode.equals("instaonly")) {
            privilegedActions.add(actionID);
            log.debug("Adding privileged action : " + actionID);
          }

          if (authMode != null && (authMode.equals("instaonly") || authMode.equals("passthru"))) {
            actionScreenMap.put(actionID, "passthru");
          }
          if (null != screenID) {
            // we have a screen id, which means it has multiple actions for
            // the screen
            if (screenActionMap.containsKey(screenID)) {
              List actionList = (List) screenActionMap.get(screenID);
              actionList.add(actionID);
              screenActionMap.put(screenID, actionList);
            } else {
              List newActionList = new ArrayList();
              newActionList.add(actionID);
              screenActionMap.put(screenID, newActionList);
            }
            actionScreenMap.put(actionID, screenID);
          }
        }
      }
    }
    return error;
  }

  /**
   * Gets the spring application context.
   *
   * @return the spring application context
   */
  private ConfigurableApplicationContext getSpringApplicationContext() {
    ConfigurableApplicationContext applicationContext =
        (ConfigurableApplicationContext) ApplicationContextProvider.getApplicationContext();
    while (applicationContext == null) {
      long starttime = System.currentTimeMillis();
      while (System.currentTimeMillis() - starttime < 10000) {
      }
      log.info("waiting for application context to startup");
      applicationContext = (ConfigurableApplicationContext) ApplicationContextProvider
          .getApplicationContext();
    }
    return applicationContext;
  }

  /**
   * Extract value from property value.
   *
   * @param pair the pair
   * @return the string
   */
  public String extractValueFromPropertyValue(PropertyValue pair) {
    if (pair == null) {
      return null;
    } else {
      String value = ((TypedStringValue) pair.getValue()).getValue();
      if (value.isEmpty()) {
        return null;
      } else {
        return value;
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.struts.action.PlugIn#destroy()
   */
  public void destroy() {
  }

  /**
   * Gets the fm config.
   *
   * @return the fm config
   */
  public static Configuration getFmConfig() {
    if (fmConfig == null) {
      // App has not been initialized, this must be for email reports.
      fmConfig = new Configuration();
      try {
        fmConfig.setDirectoryForTemplateLoading(new File(rootRealPath + "/WEB-INF/templates"));
      } catch (Exception exception) {
        // todo: this is not good. We should be throwing FileNotFound
        // and IO Exceptions, but
        // it affects too many callers who are not interested in getting
        // it this way.
        exception.printStackTrace();
        return null;
      }
    }

    fmConfig.setTagSyntax(Configuration.AUTO_DETECT_TAG_SYNTAX);
    int numDecimals = 2;
    try {
      numDecimals = GenericPreferencesDAO.getGenericPreferences().getDecimalDigits();
    } catch (Exception exception) {
      /* ignore */
    }
    if (numDecimals == 2) {
      fmConfig.setNumberFormat("#,##0.00");
    } else if (numDecimals == 3) {
      fmConfig.setNumberFormat("#,##0.000");
    }
    return fmConfig;
  }

  /**
   * Gets the servlet context.
   *
   * @return the servlet context
   */
  public static ServletContext getServletContext() {
    return context;
  }

  /**
   * Gets the root real path.
   *
   * @return the root real path
   */
  public static String getRootRealPath() {

    /* Taking context from spring because struts loads after spring */
    if (null == rootRealPath) {
      return ((WebApplicationContext) ApplicationContextProvider.getApplicationContext())
          .getServletContext().getRealPath("");
    }
    return rootRealPath;
  }

  /**
   * Sets the root real path.
   *
   * @param realPath the new root real path
   */
  public static void setRootRealPath(String realPath) {
    rootRealPath = realPath;
  }

  /**
   * Gets the real path.
   *
   * @param relativePath the relative path
   * @return the real path
   */
  public static String getRealPath(String relativePath) {
    return rootRealPath + "/" + relativePath;
  }

  /**
   * Gets the action url map.
   *
   * @return the action url map
   */
  public static Map getActionUrlMap() {
    return actionUrlMap;
  }

  /**
   * Initialize our default date/time/timestamp converters using our common input date patterns.
   */
  public static void initializeConverters() {
    // plain date converter
    SqlDateConverter dateConverter = new SqlDateConverter();
    dateConverter.setPatterns(new String[] { "dd-MM-yy", "dd-MM-yyyy", "dd/MM/yy", "dd/MM/yyyy" });
    ConvertUtils.register(dateConverter, java.sql.Date.class);

    // timestamp converter: rarely used, since user input will be split into
    // date and time
    SqlTimestampConverter tsConverter = new SqlTimestampConverter();
    tsConverter.setPatterns(new String[] { "dd-MM-yyyy HH:mm", "dd-MM-yyyy HH:mm:ss" });
    ConvertUtils.register(tsConverter, java.sql.Timestamp.class);

    // time converter
    SqlTimeConverter timeConverter = new SqlTimeConverter();
    timeConverter.setPatterns(new String[] { "HH:mm", "HH:mm:ss" });
    ConvertUtils.register(timeConverter, java.sql.Time.class);

    /*
     * return null when value is null. throw exception when unable to convert to integer.
     */
    CommonTypeConverter integerConverter = new CommonTypeConverter(new IntegerConverter());
    ConvertUtils.register(integerConverter, java.lang.Integer.class);

    /*
     * return null when value is null. throw exception when unable to convert to BigDecimal.
     */
    CommonTypeConverter bdConverter = new CommonTypeConverter(new BigDecimalConverter());
    ConvertUtils.register(bdConverter, java.math.BigDecimal.class);
  }

  /**
   * Update in all schemas.
   *
   * @param tableName  the table name
   * @param columnData the column data
   * @param keys       the keys
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  private static void updateInAllSchemas(String tableName, Map columnData, Map keys)
      throws SQLException, IOException {
    if (DatabaseHelper.getJBDCTemplate() != null) {
      Boolean success = true;
      List<BasicDynaBean> schemas = DatabaseHelper.getAllSchemas();
      for (BasicDynaBean schema : schemas) {
        String sch = (String) schema.get("schema");
        String[] dbSchema = new String[] { null, "", sch, "_system", "", "" };
        RequestContext.setConnectionDetails(dbSchema);
        // reset the following tables
        GenericDAO dao = new GenericDAO(tableName);
        Connection con = null;
        try {
          con = DataBaseUtil.getConnection();
          con.setAutoCommit(false);
          dao.update(con, columnData, keys);
        } finally {
          DataBaseUtil.commitClose(con, success);
        }
      }
    }
  }

  /**
   * method used to reset the insurance_submission_batch table processing_status fields to Not
   * Scheduled & processing_type to null.
   */
  private void resetClaimSubmissionBatchStatus() {
    Map columnData = new HashMap<>();
    columnData.put("processing_status", "N");
    columnData.put("processing_type", " ");
    Map keys = new HashMap<>();
    keys.put("processing_status", "P");
    try {
      updateInAllSchemas("insurance_submission_batch", columnData, keys);
    } catch (SQLException | IOException exception) {
      log.error("", exception);
    }
  }

  /**
   * Reset selfpay batch status of jobs that are in progress to Not-Scheduled.
   */
  private void resetSelfpayBatchStatus() {
    Map columnData = new HashMap<>();
    columnData.put("processing_status", "N");
    Map keys = new HashMap<>();
    keys.put("processing_status", "P");
    try {
      updateInAllSchemas("selfpay_submission_batch", columnData, keys);
    } catch (SQLException | IOException exception) {
      log.error("", exception);
    }
  }

}
