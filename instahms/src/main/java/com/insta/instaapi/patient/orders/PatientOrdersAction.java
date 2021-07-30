package com.insta.instaapi.patient.orders;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.orders.OrderDAO;
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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** PatientOrdersAction class. */
public class PatientOrdersAction extends DispatchAction {

  static Logger logger = LoggerFactory.getLogger(PatientOrdersAction.class);

  /**
   * Get Patient Orders Data.
   *
   * @param mapping action mapping paramter
   * @param form form paramter
   * @param request request object
   * @param response response object
   * @return returns Action forward
   * @throws Exception throws Generic Exception
   */
  public ActionForward getPatientOrdersData(
      ActionMapping mapping,
      ActionForm form,
      HttpServletRequest request,
      HttpServletResponse response)
      throws Exception {

    String toDate = request.getParameter("to_date");
    String centerId1 = request.getParameter("center_id");
    String requestHandalerKey = ApiUtil.getRequestKey(request);
    Object fromDateRequestedTimeStamp = null;
    Object toDateRequestedTimeStamp = null;
    java.sql.Timestamp currentTimeStamp = DateUtil.getCurrentTimestamp();
    String isoTimestamp = DateUtil.formatIso8601Timestamp(currentTimeStamp);
    JSONSerializer json = JsonProcessor.getJSONParser();
    LinkedHashMap<String, Object> patientOrdersDataMAP = new LinkedHashMap<String, Object>();
    boolean isFromDateTimeStampFormat = true;
    boolean isToDateTimeStampFormat = true;
    boolean isValidRequest = false;
    ServletContext ctx = servlet.getServletContext();
    Map<String, Object> sessionMap = ServletContextUtil.getContextParametersMap(ctx);
    logger.info("getting session related data from conetxt" + sessionMap);
    response.setContentType("application/json");
    response.setHeader("Cache-Control", "no-cache");
    // response.setHeader("Access-Control-Allow-Origin", "*");
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
    String mrNo = request.getParameter("mr_no");
    if (sessionMap != null && !sessionMap.isEmpty()) {
      sessionParameters = (Map) sessionMap.get(requestHandalerKey);
      if (sessionParameters != null && !sessionParameters.isEmpty()) {
        // getting login time
        loginTime = (java.sql.Timestamp) sessionParameters.get("login_time");
        if ((currentTime.getTime() - loginTime.getTime()) / 60000 <= validDuration) {
          isValidRequest = true;
        } else {
          isValidRequest = false;
        }
        if ((boolean) sessionParameters.get("patient_login")) {
          mrNo = (String) sessionParameters.get("customer_user_id");
        }
      }
    }

    if (!isValidRequest) {
      successMsg = "invalid request token,please login again";
      logger.info("invalid request token,please login again");
      logger.info("sending the response back to the requesting server");
      patientOrdersDataMAP.put("return_code", "1001");
      patientOrdersDataMAP.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(json.deepSerialize(patientOrdersDataMAP));
      response.flushBuffer();
      return null;
    }

    // Check the rights for Clinical Analysis Data screen
    boolean isScreenRights = ScreenRights.getScreenRights(requestHandalerKey, ctx, "patientOrders");
    if (!isScreenRights) {
      successMsg = "Permission Denied. Please check with Administrator.";
      patientOrdersDataMAP.put("return_code", "1003");
      patientOrdersDataMAP.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(json.deepSerialize(patientOrdersDataMAP));
      response.flushBuffer();
      return null;
    }
    String fromDate = request.getParameter("from_date");
    if ((fromDate == null || fromDate.isEmpty()) || (mrNo == null || mrNo.isEmpty())) {
      successMsg = "Mandatory fields are not supplied";
      patientOrdersDataMAP.put("return_code", "1002");
      patientOrdersDataMAP.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(json.deepSerialize(patientOrdersDataMAP));
      response.flushBuffer();
      return null;
    }

    try {
      fromDateRequestedTimeStamp = DateUtil.parseIso8601Timestamp(fromDate);
      if (fromDateRequestedTimeStamp == null) {
        isFromDateTimeStampFormat = false;
        fromDateRequestedTimeStamp = DateUtil.parseIso8601Timestamp(fromDate + "T00:00:00Z");
      }
      if (fromDateRequestedTimeStamp == null) {
        successMsg = "Invalid input parameters supplied";
        patientOrdersDataMAP.put("return_code", "1021");
        patientOrdersDataMAP.put("return_message", successMsg);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write(json.deepSerialize(patientOrdersDataMAP));
        response.flushBuffer();
        return null;
      }
      if (toDate != null && toDate.length() > 0) {
        toDateRequestedTimeStamp = DateUtil.parseIso8601Timestamp(toDate);
        if (toDateRequestedTimeStamp == null) {
          isToDateTimeStampFormat = false;
          toDateRequestedTimeStamp = DateUtil.parseIso8601Timestamp(toDate + "T00:00:00Z");
        }
      }
    } catch (ParseException exception) {
      successMsg = "Invalid input parameters supplied";
      patientOrdersDataMAP.put("return_code", "1021");
      patientOrdersDataMAP.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(json.deepSerialize(patientOrdersDataMAP));
      response.flushBuffer();
      return null;
    }
    long diff = 0;
    long dto = 0;
    long dfrom = 0;
    if (toDateRequestedTimeStamp != null) {
      if (toDateRequestedTimeStamp instanceof java.sql.Timestamp) {
        dto = ((java.sql.Timestamp) toDateRequestedTimeStamp).getTime();
      } else {
        dto = ((java.sql.Date) toDateRequestedTimeStamp).getTime();
      }
    } else {
      dto = ((java.sql.Timestamp) currentTimeStamp).getTime();
    }
    if (fromDateRequestedTimeStamp instanceof java.sql.Timestamp) {
      dfrom = ((java.sql.Timestamp) fromDateRequestedTimeStamp).getTime();
    } else {
      dfrom = ((java.sql.Date) fromDateRequestedTimeStamp).getTime();
    }

    diff = dto - dfrom;
    long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
    if (days < 0 || diff < 0) {
      successMsg = "To date can not be earlier than From date";
      patientOrdersDataMAP.put("return_code", "1022");
      patientOrdersDataMAP.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(json.deepSerialize(patientOrdersDataMAP));
      response.flushBuffer();
      return null;
    }
    if (days > 29) {
      successMsg = "Duration can not be more than 30 days";
      patientOrdersDataMAP.put("return_code", "1023");
      patientOrdersDataMAP.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(json.deepSerialize(patientOrdersDataMAP));
      response.flushBuffer();
      return null;
    }
    Connection con = null;
    List patientOrdersList = new ArrayList();
    try {
      con =
          com.insta.instaapi.common.DbUtil.getConnection(
              (String) sessionParameters.get("hospital_name"));
      int centerId = -1;
      if (centerId1 != null && !centerId1.isEmpty()) {
        centerId = Integer.parseInt(centerId1);
      }
      List visitsList = null;
      if (toDateRequestedTimeStamp != null) {
        visitsList =
            new VisitDetailsDAO()
                .getPatientVisits(
                    con,
                    mrNo,
                    centerId,
                    (java.sql.Timestamp) fromDateRequestedTimeStamp,
                    (java.sql.Timestamp) toDateRequestedTimeStamp);
      } else {
        visitsList =
            new VisitDetailsDAO()
                .getPatientVisits(
                    con,
                    mrNo,
                    centerId,
                    (java.sql.Timestamp) fromDateRequestedTimeStamp,
                    currentTimeStamp);
      }
      if (visitsList != null && visitsList.size() > 0) {
        for (int i = 0; i < visitsList.size(); i++) {
          Map visitMap = new HashMap();
          visitMap.putAll((Hashtable) visitsList.get(i));
          String patientId = (String) ((Hashtable) visitsList.get(i)).get("PATIENT_ID");
          List ordersList = null;
          ordersList = new OrderDAO().getPatientOrdersByType(con, patientId, "Doctor");
          if (ordersList != null) {
            visitMap.put("orders", ConversionUtils.listBeanToListMap(ordersList));
          } else {
            visitMap.put("orders", null);
          }
          patientOrdersList.add(visitMap);
        }
      }
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
    patientOrdersDataMAP.put("patient_orders_details", patientOrdersList);
    successMsg = "Success";
    patientOrdersDataMAP.put("return_code", "2001");
    patientOrdersDataMAP.put("return_message", successMsg);
    response.getWriter().write(json.deepSerialize(patientOrdersDataMAP));
    response.flushBuffer();

    return null;
  }
}
