package com.insta.instaapi.customer;

import com.bob.hms.common.LoginAction;
import com.insta.hms.common.GenericDAO;
import com.insta.instaapi.common.ApiUtil;
import com.insta.instaapi.common.DbUtil;
import com.insta.instaapi.common.JsonProcessor;
import com.insta.instaapi.common.ServletContextUtil;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.apache.struts.util.MessageResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ApiLevelAction extends DispatchAction {

  static Logger logger = LoggerFactory.getLogger(LoginAction.class);

  /**
   * Get API Level.
   * @param mapping  Action Mapping
   * @param form     Action Form
   * @param request  Servlet Request Object
   * @param response Servlet Response Object
   * @return Response
   * @throws IOException IO Exception
   */
  public ActionForward getAPILevel(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    logger.debug("getting API level from DB");
    JSONSerializer js = JsonProcessor.getJSONParser();
    Map<String, Object> dataMap = new HashMap<String, Object>();
    response.setContentType("application/json");
    response.setHeader("Cache-Control", "no-cache");
    // response.setHeader("Access-Control-Allow-Origin", "*");
    String requestHandalerKey = ApiUtil.getRequestKey(request);
    String successMsg = null;
    ServletContext ctx = servlet.getServletContext();
    Map<String, Object> sessionMap = ServletContextUtil.getContextParametersMap(ctx);
    logger.debug("getting session related data from conetxt" + sessionMap);
    Map<String, Object> sessionParameters = null;
    java.sql.Timestamp loginTime = null;
    MessageResources msgResource = getResources(request);
    String tokenValidation = msgResource.getMessage("token.validation.duration");
    int validDuration = Integer.parseInt(tokenValidation);
    // getting current time
    Calendar calendar = Calendar.getInstance();
    java.util.Date now = calendar.getTime();
    java.sql.Timestamp currentTime = new java.sql.Timestamp(now.getTime());
    boolean isAValidRequest = false;
    if (sessionMap != null && !sessionMap.isEmpty()) {
      sessionParameters = (Map<String, Object>) sessionMap.get(requestHandalerKey);
      if (sessionParameters != null && !sessionParameters.isEmpty()) {
        // getting login time
        loginTime = (java.sql.Timestamp) sessionParameters.get("login_time");
        isAValidRequest = (currentTime.getTime() - loginTime.getTime()) / 60000 <= validDuration;
      }
    }
    if (!isAValidRequest) {
      successMsg = "invalid request token,please login again";
      logger.debug("invalid request token,please login again");
      logger.debug("sending the response back to the requesting server");
      dataMap.put("return_code", "1001");
      dataMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(dataMap));
      response.flushBuffer();
      return null;
    }
    logger.debug("getting connection for " + (String) sessionParameters.get("hospital_name"));
    try (Connection con = DbUtil
        .getConnection((String) sessionParameters.get("hospital_name"))) {
      con.setAutoCommit(false);
      List<BasicDynaBean> list = null;
      list = new GenericDAO("system_data").listAll(con, null, null, null);
      if (null != list && list.size() > 0) {
        logger.debug("Loading current API level...");
        BasicDynaBean bean = (BasicDynaBean) list.get(0);
        int apiLevel = (Integer) bean.get("api_level");
        String version = (String) bean.get("version");
        dataMap.put("api_level", apiLevel);
        dataMap.put("version", version);
        dataMap.put("return_code", "2001");
        dataMap.put("return_message", "Success");
        response.getWriter().write(js.deepSerialize(dataMap));
        response.flushBuffer();
        return null;
      }
    } catch (SQLException ex) {
      // TODO Auto-generated catch block
      dataMap.put("return_code", "1021");
      dataMap.put("return_message", "Failed to retrieve the API level");
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      response.getWriter().write(js.deepSerialize(dataMap));
      response.flushBuffer();
      return null;
    }
    dataMap.put("return_code", "1021");
    dataMap.put("return_message", "Failed to retrieve the API level");
    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    response.getWriter().write(js.deepSerialize(dataMap));
    response.flushBuffer();
    return null;
  }
}
