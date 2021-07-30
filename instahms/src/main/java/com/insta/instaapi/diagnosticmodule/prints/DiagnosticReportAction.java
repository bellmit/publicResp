package com.insta.instaapi.diagnosticmodule.prints;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.diagnosticmodule.laboratory.DiagReportHelper;
import com.insta.instaapi.common.ApiUtil;
import com.insta.instaapi.common.JsonProcessor;
import com.insta.instaapi.common.ScreenRights;
import com.insta.instaapi.common.ServletContextUtil;

import flexjson.JSONSerializer;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.apache.struts.util.MessageResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * DiagnosticReportAction class.
 * 
 * @author krishna
 *
 */
public class DiagnosticReportAction extends DispatchAction {

  static Logger logger = LoggerFactory.getLogger(DiagnosticReportAction.class);

  /**
   * Get Diagnostics Reports by Visit.
   * 
   * @param mapping  Action mapping
   * @param form     Action form
   * @param request  request object
   * @param response response object
   * @return the action forward
   * @throws IOException              Signals that an I/O exception has occurred.
   * @throws ServletException         Servlet Exception
   * @throws SQLException             SQL Exception
   * @throws NoSuchAlgorithmException No such algorithm exception
   * @throws ParseException           parsing exception
   * @throws Exception                Generic Exception
   */
  public ActionForward getDiagReportsForVisit(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException, SQLException, NoSuchAlgorithmException, ParseException, Exception {
    logger.info("getting diagnostics reports visit wise");
    Map<String, Object> filterMap = new HashMap<String, Object>();
    String requestHandalerKey = ApiUtil.getRequestKey(request);
    boolean isAValidRequest = false;
    Connection con = null;
    JSONSerializer js = JsonProcessor.getJSONParser();
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
        if ((currentTime.getTime() - loginTime.getTime()) / 60000 <= validDuration) {
          isAValidRequest = true;
        } else {
          isAValidRequest = false;
        }
      }
    }
    Map<String, Object> visitDetailsDataMap = new HashMap<String, Object>();
    if (!isAValidRequest) {
      response.setContentType("application/json");
      response.setHeader("Cache-Control", "no-cache");
      // response.setHeader("Access-Control-Allow-Origin", "*");
      successMsg = "invalid request token,please login again";
      logger.info("invalid request token,please login again");
      logger.info("sending the response back to the requesting server");
      visitDetailsDataMap.put("return_code", "1001");
      visitDetailsDataMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(visitDetailsDataMap));
      response.flushBuffer();
      return null;
    }
    // Check the rights for Laboratory Reports List screen
    boolean isScreenRights = ScreenRights.getScreenRights(requestHandalerKey, ctx,
        "labReportsData");
    if (!isScreenRights) {
      Map labReportsData = new HashMap();
      successMsg = "Permission Denied. Please check with Administrator.";
      labReportsData.put("return_code", "1003");
      labReportsData.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(labReportsData));
      response.flushBuffer();
      return null;
    }
    String patientId = request.getParameter("visitId");
    if (patientId == null || patientId.isEmpty()) {
      successMsg = "Mandatory fields are not supplied";
      visitDetailsDataMap.put("return_code", "1002");
      visitDetailsDataMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(visitDetailsDataMap));
      response.flushBuffer();
      return null;
    }

    RequestContext.setConnectionDetails(
        new String[] { "", "", (String) sessionParameters.get("hospital_name"), "", "0" });
    response.setContentType("application/pdf");
    response.setHeader("Cache-Control", "no-cache");
    // response.setHeader("Access-Control-Allow-Origin", "*");
    GenericDAO userDAO = new GenericDAO("u_user");
    // String userId = (String) userDAO.findByKey("request_handler_key",
    // requestHandalerKey).get("emp_username");
    String userId = (String) sessionParameters.get("customer_user_id");
    OutputStream os = response.getOutputStream();
    DiagReportHelper helper = new DiagReportHelper();
    String cpath = request.getContextPath();
    helper.generateReport(os, patientId, userId, cpath, request.getParameter("logoHeader"));
    os.flush();
    os.close();
    logger.info("sent all diagnostics reports....");
    return null;
  }
}
