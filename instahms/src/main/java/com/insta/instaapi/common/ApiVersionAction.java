package com.insta.instaapi.common;

import com.bob.hms.common.LoginAction;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;

import flexjson.JSONSerializer;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.apache.struts.util.MessageResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ApiVersionAction extends DispatchAction {

  static Logger logger = LoggerFactory.getLogger(LoginAction.class);

  /**
   * Get API Version.
   * @param mapping  Action Mapping
   * @param form     Action Form
   * @param request  Servlet Request Object
   * @param response Servlet Response Object
   * @return Response
   * @throws IOException IO Exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getAPIVersion(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    logger.info("getting API version from properties");
    JSONSerializer js = JsonProcessor.getJSONParser();
    ServletContext ctx = servlet.getServletContext();
    MessageResources msgResource = getResources(request);
    Map<String, Object> dataMap = new HashMap<String, Object>();
    response.setContentType("application/json");
    response.setHeader("Cache-Control", "no-cache");
    // response.setHeader("Access-Control-Allow-Origin", "*");
    if (null != msgResource) {
      logger.info("Loading current instaapps version...");
      String version = msgResource.getMessage("insta.software.version");
      if (null != version) {
        dataMap.put("version", version);
        dataMap.put("return_code", "2001");
        dataMap.put("return_message", "Success");
        response.getWriter().write(js.deepSerialize(dataMap));
        response.flushBuffer();
        return null;
      }
    }
    dataMap.put("return_code", "1021");
    dataMap.put("return_message", "Failed to retrieve the version");
    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    response.getWriter().write(js.deepSerialize(dataMap));
    response.flushBuffer();
    return null;
  }
}
