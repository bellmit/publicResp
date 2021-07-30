package com.insta.hms.common;

import org.apache.struts.Globals;
import org.apache.struts.config.ActionConfig;
import org.apache.struts.config.ModuleConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

// TODO: Auto-generated Javadoc
/**
 * The Class AccessControlFilter.
 *
 * @author kalpana.muvvala
 */
public class AccessControlFilter implements Filter {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(AccessControlFilter.class);

  /** The config. */
  private FilterConfig config = null;

  /*
   * (non-Javadoc)
   * 
   * @see javax.servlet.Filter#destroy()
   */
  public void destroy() {
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse,
   * javax.servlet.FilterChain)
   */
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest req = (HttpServletRequest) request;
    boolean failure = false;
    request.setAttribute("screenId", "UNKNOWN");

    ModuleConfig mc = (ModuleConfig) config.getServletContext().getAttribute(Globals.MODULE_KEY);

    int len = req.getContextPath().length();
    String actionPath = req.getRequestURI().substring(len, req.getRequestURI().length());
    // todo: This is really not very clean. Need to find another way
    if (actionPath.endsWith(".do")) {
      actionPath = actionPath.replace(".do", "");
    }

    // remove all leading / from the actionPath: struts seems to be doing the same.
    while (actionPath.startsWith("//")) {
      actionPath = actionPath.substring(1);
    }

    ActionConfig ac = mc.findActionConfig(actionPath);
    HttpSession session = ((HttpServletRequest) request).getSession(false);
    HashMap actionRightsMap = (HashMap) session.getAttribute("urlRightsMap");

    if (session.getAttribute("userid") != null) {
      if (("/").equals(actionPath)) {
        // "/" is a special URL: it doesn't have an action config, but we still need to pass it
        chain.doFilter(request, response);
        return;
      }

      if ((null == actionRightsMap) || (null == ac)) {
        if (null == actionRightsMap) {
          logger.warn("Action rights is null: " + actionPath + "; url: " + req.getRequestURI());
        } else if (null == ac) {
          logger.warn("Action config not found: " + actionPath + "; url: " + req.getRequestURI());
          // don't chain. Send the user to the access denied page.
        }
        ((HttpServletResponse) response)
            .sendRedirect(req.getContextPath() + "/AccessControlForward.do");
        return;
      }

      // By default the access check is always done unless explicitly specified in the
      // struts-config as such. So if no-auth property is not set or if it is set to false
      // we need to check the rights of the user to access the action
      if (null != ac.getProperty("auth_mode") && ac.getProperty("auth_mode").equals("passthru")) {
        // pass it through.
        chain.doFilter(request, response);
        return;
      }

      String actionId = ac.getProperty("action_id");
      String screenId = ac.getProperty("screen_id");
      if (screenId == null) {
        screenId = actionId;
      }
      if (screenId != null) {
        request.setAttribute("screenId", screenId);
      }
      if (actionId != null) {
        request.setAttribute("actionId", actionId);
      }

      logger.debug(actionPath + " Action ID: " + actionId);

      if ((actionId != null) && actionRightsMap.containsKey(actionId)
          && actionRightsMap.get(actionId).equals("A")) {
        // user has access: chain the request forward.
        chain.doFilter(request, response);
        return;
      } else {
        int roleId = (Integer) session.getAttribute("roleId");
        logger.warn("No rights for role " + roleId + "; url: " + req.getRequestURI());
        logger.warn("actionId: " + actionId + "; rights: " + actionRightsMap.get(actionId));
        // don't chain. Send the user to the access denied page.
        ((HttpServletResponse) response)
            .sendRedirect(req.getContextPath() + "/AccessControlForward.do");
      }
    } else {
      // the user is yet to login (there is no session). The SessionCheckFilter will allow the
      // request
      // if a session is not required, otherwise it will show the login page. Chain it through
      // some other error: show access denied, but log it differently.
      logger.debug("No session: Access Control check disabled; url: " + req.getRequestURI());
      chain.doFilter(request, response);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
   */
  public void init(FilterConfig filterConfig) throws ServletException {
    this.config = filterConfig;
  }

}
