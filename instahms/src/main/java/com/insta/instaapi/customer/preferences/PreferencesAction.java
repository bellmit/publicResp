package com.insta.instaapi.customer.preferences;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.instaapi.common.ApiUtil;
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
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PreferencesAction extends DispatchAction {
  static Logger logger = LoggerFactory.getLogger(PreferencesAction.class);

  /**
   * Get FAQ.
   * 
   * @param mapping  Action Mapping
   * @param form     Action Form
   * @param request  Servlet Request Object
   * @param response Servlet Response Object
   * @return Response
   * @throws IOException  IO exception
   * @throws SQLException SQL exception
   */
  public ActionForward getFaqs(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws IOException, SQLException {

    String requestHandlerKey = ApiUtil.getRequestKey(request);
    boolean isAValidRequest = false;
    ServletContext ctx = servlet.getServletContext();
    Map<String, Object> sessionMap = ServletContextUtil.getContextParametersMap(ctx);
    logger.info("getting session related data from conetxt" + sessionMap);
    Map sessionParameters = null;
    String successMsg = "";
    java.sql.Timestamp loginTime = null;
    MessageResources msgResource = getResources(request);
    String tokenValidation = msgResource.getMessage("token.validation.duration");
    int validDuration = Integer.parseInt(tokenValidation);
    // getting current time
    Calendar calendar = Calendar.getInstance();
    java.util.Date now = calendar.getTime();
    java.sql.Timestamp currentTime = new java.sql.Timestamp(now.getTime());
    if (sessionMap != null && !sessionMap.isEmpty()) {
      sessionParameters = (Map) sessionMap.get(requestHandlerKey);
      if (sessionParameters != null && !sessionParameters.isEmpty()) {
        // getting login time
        loginTime = (java.sql.Timestamp) sessionParameters.get("login_time");
        isAValidRequest = (currentTime.getTime() - loginTime.getTime()) / 60000 <= validDuration;
      }
    }
    response.setContentType("application/json");
    response.setHeader("Cache-Control", "no-cache");
    // response.setHeader("Access-Control-Allow-Origin", "*");
    Map<String, Object> faqsDataMap = new HashMap<String, Object>();

    if (!isAValidRequest) {
      successMsg = "invalid request token,please login again";
      logger.info("invalid request token,please login again");
      faqsDataMap.put("return_code", "1001");
      faqsDataMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }
    if (isAValidRequest) {
      try (Connection con = com.insta.instaapi.common.DbUtil
          .getConnection((String) sessionParameters.get("hospital_name"))) {
        logger.info("getting connection object" + con + "----"
            + (String) sessionParameters.get("hospital_name"));
        List<BasicDynaBean> faqData = new GenericDAO("customer_preferences").listAll(con,
            Arrays.asList(new String[] { "faq" }), null, null);
        faqsDataMap.put("faq", ConversionUtils.listBeanToListMap(faqData));
        response.setStatus(HttpServletResponse.SC_OK);
        faqsDataMap.put("return_code", "2001");
        faqsDataMap.put("return_message", "Success");
      }
    }

    logger.info("sending the response back to the requesting server");
    JSONSerializer js = JsonProcessor.getJSONParser();
    response.getWriter().write(js.deepSerialize(faqsDataMap));
    response.flushBuffer();
    return null;
  }

  /**
   * Get Terms and Conditions.
   * 
   * @param mapping  Action Mapping
   * @param form     Action Form
   * @param request  Servlet Request Object
   * @param response Servlet Response Object
   * @return Response
   * @throws IOException  IO exception
   * @throws SQLException SQL exception
   */
  public ActionForward getTermsAndConditions(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {

    String requestHandalerKey = ApiUtil.getRequestKey(request);
    boolean isAValidRequest = false;
    ServletContext ctx = servlet.getServletContext();
    Map<String, Object> sessionMap = ServletContextUtil.getContextParametersMap(ctx);
    logger.info("getting session related data from conetxt" + sessionMap);
    Map sessionParameters = null;
    String successMsg = "";
    java.sql.Timestamp loginTime = null;
    MessageResources msgResource = getResources(request);
    String tokenValidation = msgResource.getMessage("token.validation.duration");
    int validDuration = Integer.parseInt(tokenValidation);
    // getting current time
    Calendar calendar = Calendar.getInstance();
    java.util.Date now = calendar.getTime();
    java.sql.Timestamp currentTime = new java.sql.Timestamp(now.getTime());
    if (sessionMap != null && !sessionMap.isEmpty()) {
      sessionParameters = (Map) sessionMap.get(requestHandalerKey);
      if (sessionParameters != null && !sessionParameters.isEmpty()) {
        // getting login time
        loginTime = (java.sql.Timestamp) sessionParameters.get("login_time");
        isAValidRequest = (currentTime.getTime() - loginTime.getTime()) / 60000 <= validDuration;
      }
    }
    response.setContentType("application/json");
    response.setHeader("Cache-Control", "no-cache");
    // response.setHeader("Access-Control-Allow-Origin", "*");
    Map<String, Object> tncDataMap = new HashMap<String, Object>();
    if (!isAValidRequest) {
      successMsg = "invalid request token,please login again";
      logger.info("invalid request token,please login again");
      tncDataMap.put("return_code", "1001");
      tncDataMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }
    if (isAValidRequest) {
      try (Connection con = com.insta.instaapi.common.DbUtil
          .getConnection((String) sessionParameters.get("hospital_name"))) {
        logger.info("getting connection object" + con + "----"
            + (String) sessionParameters.get("hospital_name"));
        List<BasicDynaBean> tncData = new GenericDAO("customer_preferences")
            .listAll(con, Arrays.asList(new String[] { "t_and_c" }), null, null);
        tncDataMap.put("TermsAndConditions",
            ConversionUtils.listBeanToListMap(tncData));
        response.setStatus(HttpServletResponse.SC_OK);
        tncDataMap.put("return_code", "2001");
        tncDataMap.put("return_message", "Success");
      }
    }

    logger.info("sending the response back to the requesting server");
    JSONSerializer js = JsonProcessor.getJSONParser();
    response.getWriter().write(js.deepSerialize(tncDataMap));
    response.flushBuffer();
    return null;
  }

  /**
   * Get Privacy Statement.
   * 
   * @param mapping  Action Mapping
   * @param form     Action Form
   * @param request  Servlet Request Object
   * @param response Servlet Response Object
   * @return Response
   * @throws IOException  IO exception
   * @throws SQLException SQL exception
   */
  public ActionForward getPrivacyStatements(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {

    String requestHandalerKey = ApiUtil.getRequestKey(request);
    boolean isAValidRequest = false;
    ServletContext ctx = servlet.getServletContext();
    Map<String, Object> sessionMap = ServletContextUtil.getContextParametersMap(ctx);
    logger.info("getting session related data from conetxt" + sessionMap);
    Map sessionParameters = null;
    String successMsg = "";
    java.sql.Timestamp loginTime = null;
    MessageResources msgResource = getResources(request);
    String tokenValidation = msgResource.getMessage("token.validation.duration");
    int validDuration = Integer.parseInt(tokenValidation);
    // getting current time
    Calendar calendar = Calendar.getInstance();
    java.util.Date now = calendar.getTime();
    java.sql.Timestamp currentTime = new java.sql.Timestamp(now.getTime());
    if (sessionMap != null && !sessionMap.isEmpty()) {
      sessionParameters = (Map) sessionMap.get(requestHandalerKey);
      if (sessionParameters != null && !sessionParameters.isEmpty()) {
        // getting login time
        loginTime = (java.sql.Timestamp) sessionParameters.get("login_time");
        isAValidRequest = (currentTime.getTime() - loginTime.getTime()) / 60000 <= validDuration;
      }
    }
    response.setContentType("application/json");
    response.setHeader("Cache-Control", "no-cache");
    // response.setHeader("Access-Control-Allow-Origin", "*");
    Map<String, Object> privacyStatementsDataMap = new HashMap<String, Object>();
    if (!isAValidRequest) {
      successMsg = "invalid request token,please login again";
      logger.info("invalid request token,please login again");
      privacyStatementsDataMap.put("return_code", "1001");
      privacyStatementsDataMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }
    if (isAValidRequest) {
      try (Connection con = com.insta.instaapi.common.DbUtil
          .getConnection((String) sessionParameters.get("hospital_name"))) {
        logger.info("getting connection object" + con + "----"
            + (String) sessionParameters.get("hospital_name"));
        List<BasicDynaBean> privacyStatementsData = new GenericDAO("customer_preferences")
            .listAll(con, Arrays.asList(new String[] { "privacy_statements" }), null, null);
        privacyStatementsDataMap.put("privacyStatements",
            ConversionUtils.listBeanToListMap(privacyStatementsData));
        response.setStatus(HttpServletResponse.SC_OK);
        privacyStatementsDataMap.put("return_code", "2001");
        privacyStatementsDataMap.put("return_message", "Success");
      }
    }

    logger.info("sending the response back to the requesting server");
    JSONSerializer js = JsonProcessor.getJSONParser();
    response.getWriter().write(js.deepSerialize(privacyStatementsDataMap));
    response.flushBuffer();
    return null;
  }

  /**
   * Get Hospital Logo.
   * 
   * @param mapping  Action Mapping
   * @param form     Action Form
   * @param request  Servlet Request Object
   * @param response Servlet Response Object
   * @return Response
   * @throws IOException  IO exception
   * @throws SQLException SQL exception
   */
  public ActionForward getLogo(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws IOException, SQLException {

    JSONSerializer js = JsonProcessor.getJSONParser();
    String requestHandalerKey = ApiUtil.getRequestKey(request);
    boolean isAValidRequest = false;
    ServletContext ctx = servlet.getServletContext();
    Map<String, Object> sessionMap = ServletContextUtil.getContextParametersMap(ctx);
    logger.info("getting session related data from conetxt" + sessionMap);
    Map sessionParameters = null;
    String successMsg = "";
    java.sql.Timestamp loginTime = null;
    MessageResources msgResource = getResources(request);
    String tokenValidation = msgResource.getMessage("token.validation.duration");
    int validDuration = Integer.parseInt(tokenValidation);
    // getting current time
    Calendar calendar = Calendar.getInstance();
    java.util.Date now = calendar.getTime();
    java.sql.Timestamp currentTime = new java.sql.Timestamp(now.getTime());
    if (sessionMap != null && !sessionMap.isEmpty()) {
      sessionParameters = (Map) sessionMap.get(requestHandalerKey);
      if (sessionParameters != null && !sessionParameters.isEmpty()) {
        // getting login time
        loginTime = (java.sql.Timestamp) sessionParameters.get("login_time");
        isAValidRequest = (currentTime.getTime() - loginTime.getTime()) / 60000 <= validDuration;
      }
    }
    response.setHeader("Cache-Control", "no-cache");
    // response.setHeader("Access-Control-Allow-Origin", "*");
    Map<String, Object> logoDataMap = new HashMap<String, Object>();

    if (!isAValidRequest) {
      response.setContentType("application/json");
      successMsg = "invalid request token,please login again";
      logger.info("invalid request token,please login again");
      logoDataMap.put("return_code", "1001");
      logoDataMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(logoDataMap));
      response.flushBuffer();
      return null;
    }
    if (isAValidRequest) {
      try (Connection con = com.insta.instaapi.common.DbUtil
          .getConnection((String) sessionParameters.get("hospital_name"))) {
        logger.info("getting connection object" + con + "----"
            + (String) sessionParameters.get("hospital_name"));
        OutputStream os = response.getOutputStream();
        response.setContentType("image/gif");
        InputStream logo = getLogo(con);
        byte[] bytes = new byte[4096];
        int len = 0;
        while ((len = logo.read(bytes)) > 0) {
          os.write(bytes, 0, len);
        }

        os.flush();
        logo.close();
        response.setStatus(HttpServletResponse.SC_OK);
        logoDataMap.put("return_code", "2001");
        logoDataMap.put("return_message", "Success");

        return null;
      }
    }
    response.setContentType("application/json");
    logger.info("sending the response back to the requesting server");
    successMsg = "Not able to perform the operation";
    logger.info("Not able to perform the operation");
    logoDataMap.put("return_code", "1002");
    logoDataMap.put("return_message", successMsg);
    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    response.getWriter().write(js.deepSerialize(logoDataMap));
    response.flushBuffer();
    return null;
  }

  private static final String GET_LOGO = "SELECT logo FROM hosp_print_master_files"
      + " WHERE center_id = 0";

  /**
   * Get Logo from DB.
   * 
   * @param con Database Connection
   * @return Inputstream containing Logo
   * @throws SQLException SQL Exception
   */
  public static InputStream getLogo(Connection con) throws SQLException {
    try (PreparedStatement ps = con.prepareStatement(GET_LOGO); ResultSet rs = ps.executeQuery()) {
      if (!rs.next()) {
        return null;
      }
      return rs.getBinaryStream(1);
    }
  }

}
