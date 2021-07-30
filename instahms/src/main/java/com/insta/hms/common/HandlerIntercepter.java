package com.insta.hms.common;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.exception.AccessDeniedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class HandlerIntercepter extends HandlerInterceptorAdapter {

  static Logger logger = LoggerFactory.getLogger(HandlerIntercepter.class);

  @LazyAutowired
  private SessionService sessionService;

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.springframework.web.servlet.handler.HandlerInterceptorAdapter#preHandle(javax.servlet.http.
   * HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object) The prehandle has
   * been overridden to: 1. All requests to the lookup endpoint are pass thru(irrespective of the
   * role id or action id) 2. Set action id into the request for ACL 3. Deny access if the current
   * action id doesn't have the rights for the current url based on UrlRightsMap
   */
  @SuppressWarnings("rawtypes")
  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object method)
      throws Exception {

    logger.debug("Before handling the request");
    if (method instanceof HttpRequestHandler) {
      return super.preHandle(request, response, method);
    }
    if (method instanceof HandlerMethod) {
      HandlerMethod handlerMethod = (HandlerMethod) method;
      Object handler = handlerMethod.getBean();

      Boolean lookupCall = false;

      if (handlerMethod.hasMethodAnnotation(RequestMapping.class)) {

        RequestMapping requestMapping = handlerMethod.getMethodAnnotation(RequestMapping.class);
        String[] mappings = requestMapping.value();

        // Possible for value to be empty due to default mappings.
        for (String mapping : mappings) {
          mapping = mapping.substring(mapping.lastIndexOf("/") + 1);
          // pass thru the request if call is to the lookup endpoint
          if (mapping.equalsIgnoreCase("lookup")) {
            logger.debug("Lookup method: passing the request thru");
            lookupCall = true;
          }
        }
      }

      if (handler instanceof BaseController) {

        HttpSession session = request.getSession(false);

        BaseController controller = (BaseController) handler;
        String actionId = controller.getActionId();
        String authMode = controller.getAuthMode();
        logger.debug("Action Id : " + actionId);
        request.setAttribute("actionId", actionId);

        if (lookupCall || null != authMode && authMode.equals("passthru")) {
          // pass it through.
          return super.preHandle(request, response, handler);
        }

        HashMap actionRightsMap = (HashMap) sessionService
            .getSessionAttributes(new String[] { "urlRightsMap" }).get("urlRightsMap");
        if ((actionId != null) && actionRightsMap.containsKey(actionId)
            && actionRightsMap.get(actionId).equals("A")) {
          // user has access: chain the request forward.
          // chain.doFilter(request, response);
          // return;
          return super.preHandle(request, response, handler);
        } else {
          int roleId = (Integer) sessionService.getSessionAttributes(new String[] { "roleId" })
              .get("roleId");
          logger.warn("No rights for role " + roleId + "; url: " + request.getRequestURI());
          logger.warn("actionId: " + actionId + "; rights: " + actionRightsMap.get(actionId));
          // don't chain. Send the user to the access denied page.
          throw new AccessDeniedException("exception.access.denied");
          // return false;
        }
      }
    }
    return false;
  }

  @Override
  public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
      ModelAndView modelAndView) throws Exception {

    logger.debug("After handling the request");
    super.postHandle(request, response, handler, modelAndView);
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
      Object handler, Exception ex) throws Exception {

    logger.debug("After rendering the view");
    super.afterCompletion(request, response, handler, ex);
  }
}
