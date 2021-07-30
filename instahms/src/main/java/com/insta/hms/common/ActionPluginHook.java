package com.insta.hms.common;

import org.apache.struts.chain.commands.ActionCommandBase;
import org.apache.struts.chain.contexts.ActionContext;
import org.apache.struts.chain.contexts.ServletActionContext;
import org.apache.struts.config.ActionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class ActionPluginHook extends ActionCommandBase {

  static Logger logger = LoggerFactory.getLogger(ActionPluginHook.class);

  @Override
  public boolean execute(ActionContext actionContext)
      throws InstantiationException, IllegalAccessException, SecurityException,
      NoSuchMethodException, IllegalArgumentException, InvocationTargetException {

    ServletActionContext serverActionContext = (ServletActionContext) actionContext;
    if (serverActionContext == null) {
      logger.debug("ServletActionContext is null");
      return false;
    }
    HttpServletRequest req = serverActionContext.getRequest();
    HttpSession session = req.getSession(false);
    if (session == null) {
      logger.debug("Session is null. Logged out?");
      return false;
    }
    ActionConfigMain actionConfig = (ActionConfigMain) session.getServletContext()
        .getAttribute("actionHookConfig");
    List<ActionHookConfig> actionHookConfigList = actionConfig.getActionHookConfigs();

    for (ActionHookConfig actionHookConfig : actionHookConfigList) {
      // Check if action_id in request is present in actionHookConfig ie stored in app context
      String actionHookId = actionHookConfig.getId();
      String actionHookMethod = actionHookConfig.getMethod();
      ActionConfig ac = serverActionContext.getActionConfig();
      String actionId = ac.getProperty("action_id");
      String methodParam = ac.getParameter();
      String method = req.getParameter(methodParam);
      String primaryKey = actionHookConfig.getPrimaryKey();
      String schemaName = (String) session.getAttribute("sesHospitalId");
      String userId = (String) session.getAttribute("userId");

      if (actionId != null && actionId.equals(actionHookId) && method.equals(actionHookMethod)) {
        logger.info("Executing " + actionHookConfig.getPluginsCount() + "plugins for actionId"
            + actionHookId);
        List<ActionPluginConfig> actionPluginList = actionHookConfig.getPlugins();
        for (ActionPluginConfig actionPluginListItem : actionPluginList) {
          // instantiate plugin class and invoke execute method
          try {
            Map<String, String[]> map = new HashMap(req.getParameterMap());
            map.put("action_id", new String[] { actionId });
            map.put("primary_key", new String[] { primaryKey });
            map.put("schema_name", new String[] { schemaName });
            map.put("user_id", new String[] { userId });

            Class cls = Class.forName(actionPluginListItem.getName());
            Object obj = cls.newInstance();
            Method pluginMethod = cls.getDeclaredMethod("execute", Map.class);
            pluginMethod.invoke(obj, map);
          } catch (ClassNotFoundException classNotFoundException) {
            // failed to invoke execute
            classNotFoundException.printStackTrace();
            logger.error("", classNotFoundException);
            logger.info("Invalid class declared in actions.xml");
          }
        }
      }
    }

    return false;

  }

}
