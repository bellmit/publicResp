package com.insta.instaapi.customer.bill;

import com.bob.hms.common.DateUtil;
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

import java.sql.Connection;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BillAction extends DispatchAction {
  static Logger logger = LoggerFactory.getLogger(BillAction.class);

  /**
   * Get Bills.
   * 
   * @param mapping  Action Mapping
   * @param form     Action Form
   * @param request  Servlet Request Object
   * @param response Servlet Response Object
   * @return Response
   * @throws Exception exception
   */
  public ActionForward getBills(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    String mrNo = request.getParameter("mr_no");
    Integer centerId = null;
    long page = 1;
    String successMsg = "";
    LinkedHashMap<String, Object> responseMap = new LinkedHashMap<String, Object>();
    JSONSerializer json = JsonProcessor.getJSONParser();
    String requestHandlerKey = ApiUtil.getRequestKey(request);
    boolean isValidRequest = false;
    ServletContext ctx = servlet.getServletContext();
    Map<String, Object> sessionMap = ServletContextUtil.getContextParametersMap(ctx);
    logger.info("getting session related data from conetxt" + sessionMap);
    response.setContentType("application/json");
    response.setHeader("Cache-Control", "no-cache");
    Map sessionParameters = null;
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
        isValidRequest = (currentTime.getTime() - loginTime.getTime()) / 60000 <= validDuration;
        if ((boolean) sessionParameters.get("patient_login")) {
          mrNo = (String) sessionParameters.get("customer_user_id");
        }
      }
    }

    if (!isValidRequest) {
      successMsg = "invalid request token,please login again";
      logger.info("invalid request token,please login again");
      logger.info("sending the response back to the requesting server");
      responseMap.put("return_code", "1001");
      responseMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(json.deepSerialize(responseMap));
      response.flushBuffer();
      return null;
    }
    try {
      page = Long.parseLong(request.getParameter("page"));
    } catch (NumberFormatException ex) {
      page = 1;
    }
    if (request.getParameter("center_id") != null) {
      try {
        centerId = Integer.parseInt(request.getParameter("center_id").trim());
      } catch (NumberFormatException ex) {
        successMsg = "Not a valid center ID";
        responseMap.put("return_code", "1024");
        responseMap.put("return_message", successMsg);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write(json.deepSerialize(responseMap));
        response.flushBuffer();
        return null;
      }
    }
    if (centerId != null && centerId == 0) {
      // Remove center id filter if filter is applied for default center
      centerId = null;
    }

    // Check the rights for Bill Screen
    boolean isScreenRights = ScreenRights.getScreenRights(requestHandlerKey, ctx, "billing");
    if (!isScreenRights) {
      successMsg = "Permission Denied. Please check with Administrator.";
      responseMap.put("return_code", "1003");
      responseMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(json.deepSerialize(responseMap));
      response.flushBuffer();
      return null;
    }

    Object fromDateRequestedTimeStamp = null;
    Object toDateRequestedTimeStamp = null;
    Object formattedTo = null;
    Object formattedFrom = null;
    java.sql.Timestamp currentTimeStamp = DateUtil.getCurrentTimestamp();
    long toEpochMili = 0;
    long fromEpochMili = 0;
    String fromDate = request.getParameter("from_date");
    String toDate = request.getParameter("to_date");
    if (fromDate != null && fromDate.length() > 0) {
      fromDateRequestedTimeStamp = DateUtil.parseTimestamp8601(fromDate);
      formattedFrom = DateUtil
          .formatIso8601Timestamp((java.sql.Timestamp) fromDateRequestedTimeStamp);
      fromEpochMili = ((java.sql.Timestamp) fromDateRequestedTimeStamp).getTime();
    } else {
      java.sql.Timestamp oneWeekBack = DateUtil.addDays(currentTimeStamp, -7);
      fromDateRequestedTimeStamp = oneWeekBack;
      formattedFrom = DateUtil.formatIso8601Timestamp(oneWeekBack);
      fromEpochMili = ((java.sql.Timestamp) oneWeekBack).getTime();
    }

    if (toDate != null && toDate.length() > 0) {
      toDateRequestedTimeStamp = DateUtil.parseTimestamp8601(toDate);
      formattedTo = DateUtil
          .formatIso8601Timestamp((java.sql.Timestamp) toDateRequestedTimeStamp);
      toEpochMili = ((java.sql.Timestamp) toDateRequestedTimeStamp).getTime();
    } else {
      toDateRequestedTimeStamp = currentTimeStamp;
      formattedTo = DateUtil.formatIso8601Timestamp(currentTimeStamp);
      toEpochMili = ((java.sql.Timestamp) currentTimeStamp).getTime();
    }
    responseMap.put("start_date", formattedFrom);
    responseMap.put("end_date", formattedTo);

    long diff = toEpochMili - fromEpochMili;
    long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
    if (days < 0 || diff < 0) {
      successMsg = "To date can not be earlier than From date";
      responseMap.put("return_code", "1022");
      responseMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(json.deepSerialize(responseMap));
      response.flushBuffer();
      return null;
    }
    if (days > 7) {
      successMsg = "Duration can not be more than 7 days";
      responseMap.put("return_code", "1023");
      responseMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(json.deepSerialize(responseMap));
      response.flushBuffer();
      return null;
    }

    logger.info("Starting to retrieve data!!!");
    Connection con = com.insta.instaapi.common.DbUtil
        .getConnection((String) sessionParameters.get("hospital_name"));
    String filterOnFinalizedDate = request.getParameter("filter_by_finalized_date");
    responseMap.put("bills",
        BillDao.getBills(con, fromDateRequestedTimeStamp, toDateRequestedTimeStamp, mrNo, centerId,
            page, filterOnFinalizedDate != null && filterOnFinalizedDate.equalsIgnoreCase("Y")));
    logger.info("Data retrieval ends, sending response back to clinet");
    successMsg = "Success";
    responseMap.put("return_code", "2001");
    responseMap.put("return_message", successMsg);
    response.getWriter().write(json.deepSerialize(responseMap));
    response.flushBuffer();
    com.insta.instaapi.common.DbUtil.closeConnections(con, null);
    return null;
  }

}
