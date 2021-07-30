package com.insta.instaapi.customer.patientclinicaldata;

import com.bob.hms.common.DateUtil;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.diagnosticmodule.laboratory.LaboratoryBO;
import com.insta.instaapi.common.ApiUtil;
import com.insta.instaapi.common.JsonProcessor;
import com.insta.instaapi.common.ScreenRights;
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PatientClinicalDataAction extends DispatchAction {
  static Logger logger = LoggerFactory
      .getLogger(com.insta.instaapi.customer.patientclinicaldata.PatientClinicalDataAction.class);

  private static String[] PATIENT_DATA_SECTIONS = { "LAB", "MEDICATIONS", "ALLERGIES",
      "APPOINTMENTS", "PATIENT_DEMOGRAPHICS", "MEDICAL_PROFESSIONAL_DEMOGRAPHICS" };

  /**
   * Get Patient Clinical Data.
   * 
   * @param mapping  Action Mapping
   * @param form     Action Form
   * @param request  Servlet Request Object
   * @param response Servlet Response Object
   * @return Response
   * @throws Exception  exception
   */
  public ActionForward getPatientClinicalData(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {

    String requestHandalerKey = ApiUtil.getRequestKey(request);
    Object fromDateRequestedTimeStamp = null;
    Object toDateRequestedTimeStamp = null;
    java.sql.Timestamp currentTimeStamp = DateUtil.getCurrentTimestamp();
    String isoTimestamp = DateUtil.formatIso8601Timestamp(currentTimeStamp);
    JSONSerializer json = JsonProcessor.getJSONParser();
    LinkedHashMap<String, Object> patientClinicalDataMAP = new LinkedHashMap<String, Object>();
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
    if (sessionMap != null && !sessionMap.isEmpty()) {
      sessionParameters = (Map) sessionMap.get(requestHandalerKey);
      if (sessionParameters != null && !sessionParameters.isEmpty()) {
        // getting login time
        loginTime = (java.sql.Timestamp) sessionParameters.get("login_time");
        isValidRequest = (currentTime.getTime() - loginTime.getTime()) / 60000 <= validDuration;
      }
    }

    if (!isValidRequest) {
      successMsg = "invalid request token,please login again";
      logger.info("invalid request token,please login again");
      logger.info("sending the response back to the requesting server");
      patientClinicalDataMAP.put("return_code", "1001");
      patientClinicalDataMAP.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(json.deepSerialize(patientClinicalDataMAP));
      response.flushBuffer();
      return null;
    }

    // Check the rights for Clinical Analysis Data screen
    boolean isScreenRights = ScreenRights.getScreenRights(requestHandalerKey, ctx,
        "patientClinicalData");
    if (!isScreenRights) {
      successMsg = "Permission Denied. Please check with Administrator.";
      patientClinicalDataMAP.put("return_code", "1003");
      patientClinicalDataMAP.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(json.deepSerialize(patientClinicalDataMAP));
      response.flushBuffer();
      return null;
    }

    String fromDate = request.getParameter("from_date");
    if (fromDate == null || fromDate.isEmpty()) {
      successMsg = "Mandatory fields are not supplied";
      patientClinicalDataMAP.put("return_code", "1002");
      patientClinicalDataMAP.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(json.deepSerialize(patientClinicalDataMAP));
      response.flushBuffer();
      return null;
    }

    try {
      fromDateRequestedTimeStamp = DateUtil.parseIso8601Timestamp(fromDate);
      if (fromDateRequestedTimeStamp == null) {
        isFromDateTimeStampFormat = false;
        fromDateRequestedTimeStamp = DateUtil.parseIso8601Date(fromDate);
      }
      if (fromDateRequestedTimeStamp == null) {
        successMsg = "Invalid input parameters supplied";
        patientClinicalDataMAP.put("return_code", "1021");
        patientClinicalDataMAP.put("return_message", successMsg);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write(json.deepSerialize(patientClinicalDataMAP));
        response.flushBuffer();
        return null;
      }
      String toDate = request.getParameter("to_date");
      if (toDate != null && toDate.length() > 0) {
        toDateRequestedTimeStamp = DateUtil.parseIso8601Timestamp(toDate);
        if (toDateRequestedTimeStamp == null) {
          isToDateTimeStampFormat = false;
          toDateRequestedTimeStamp = DateUtil.parseIso8601Date(toDate);
        }
      }
    } catch (ParseException ex) {
      successMsg = "Invalid input parameters supplied";
      patientClinicalDataMAP.put("return_code", "1021");
      patientClinicalDataMAP.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(json.deepSerialize(patientClinicalDataMAP));
      response.flushBuffer();
      return null;
    }
    if (isFromDateTimeStampFormat) {
      patientClinicalDataMAP.put("START_DATE",
          DateUtil.formatIso8601Timestamp((java.sql.Timestamp) fromDateRequestedTimeStamp));
    } else {
      patientClinicalDataMAP.put("START_DATE",
          DateUtil.formatIso8601Date((java.sql.Date) fromDateRequestedTimeStamp));
    }

    if (toDateRequestedTimeStamp != null) {
      if (isToDateTimeStampFormat) {
        patientClinicalDataMAP.put("END_DATE",
            DateUtil.formatIso8601Timestamp((java.sql.Timestamp) toDateRequestedTimeStamp));
      } else {
        patientClinicalDataMAP.put("END_DATE",
            DateUtil.formatIso8601Date((java.sql.Date) toDateRequestedTimeStamp));
      }
    } else {
      patientClinicalDataMAP.put("END_DATE", isoTimestamp);
    }
    long diff = 0;
    long toEpochMili = 0;
    long fromEpochMili = 0;
    if (toDateRequestedTimeStamp != null) {
      if (toDateRequestedTimeStamp instanceof java.sql.Timestamp) {
        toEpochMili = ((java.sql.Timestamp) toDateRequestedTimeStamp).getTime();
      } else {
        toEpochMili = ((java.sql.Date) toDateRequestedTimeStamp).getTime();
      }
    } else {
      toEpochMili = ((java.sql.Timestamp) currentTimeStamp).getTime();
    }
    if (fromDateRequestedTimeStamp instanceof java.sql.Timestamp) {
      fromEpochMili = ((java.sql.Timestamp) fromDateRequestedTimeStamp).getTime();
    } else {
      fromEpochMili = ((java.sql.Date) fromDateRequestedTimeStamp).getTime();
    }

    diff = toEpochMili - fromEpochMili;
    long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
    if (days < 0 || diff < 0) {
      successMsg = "To date can not be earlier than From date";
      patientClinicalDataMAP.put("return_code", "1022");
      patientClinicalDataMAP.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(json.deepSerialize(patientClinicalDataMAP));
      response.flushBuffer();
      return null;
    }
    if (days > 29) {
      successMsg = "Duration can not be more than 30 days";
      patientClinicalDataMAP.put("return_code", "1023");
      patientClinicalDataMAP.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(json.deepSerialize(patientClinicalDataMAP));
      response.flushBuffer();
      return null;
    }
    logger.info("Starting to retrieve data!!!");
    for (String section : PATIENT_DATA_SECTIONS) {
      if (toDateRequestedTimeStamp != null) {
        patientClinicalDataMAP.put(section, PatientClinicalDataDAO.getQueryResult(section,
            sessionParameters, fromDateRequestedTimeStamp, toDateRequestedTimeStamp));
      } else {
        patientClinicalDataMAP.put(section, PatientClinicalDataDAO.getQueryResult(section,
            sessionParameters, fromDateRequestedTimeStamp, currentTimeStamp));
      }
    }
    logger.info("Data retrieval ends, sending response back to clinet");
    successMsg = "Success";
    patientClinicalDataMAP.put("return_code", "2001");
    patientClinicalDataMAP.put("return_message", successMsg);
    response.getWriter().write(json.deepSerialize(patientClinicalDataMAP));
    response.flushBuffer();

    return null;
  }

  /**
   * Get Patient Clinical Data Check.
   * 
   * @param mapping  Action Mapping
   * @param form     Action Form
   * @param request  Servlet Request Object
   * @param response Servlet Response Object
   * @return Response
   * @throws Exception  exception
   */
  public ActionForward getPatientClinicalDataCheck(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {

    String requestHandalerKey = ApiUtil.getRequestKey(request);
    Object fromDateRequestedTimeStamp = null;
    Object toDateRequestedTimeStamp = null;
    java.sql.Timestamp currentTimeStamp = DateUtil.getCurrentTimestamp();
    String isoTimestamp = DateUtil.formatIso8601Timestamp(currentTimeStamp);
    JSONSerializer json = JsonProcessor.getJSONParser();
    LinkedHashMap<String, Object> patientClinicalDataMAP = new LinkedHashMap<String, Object>();
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
    if (sessionMap != null && !sessionMap.isEmpty()) {
      sessionParameters = (Map) sessionMap.get(requestHandalerKey);
      if (sessionParameters != null && !sessionParameters.isEmpty()) {
        // getting login time
        loginTime = (java.sql.Timestamp) sessionParameters.get("login_time");
        isValidRequest = (currentTime.getTime() - loginTime.getTime()) / 60000 <= validDuration;
      }
    }

    if (!isValidRequest) {
      successMsg = "invalid request token,please login again";
      logger.info("invalid request token,please login again");
      logger.info("sending the response back to the requesting server");
      patientClinicalDataMAP.put("return_code", "1001");
      patientClinicalDataMAP.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(json.deepSerialize(patientClinicalDataMAP));
      response.flushBuffer();
      return null;
    }

    // Check the rights for Clinical Analysis Data screen
    boolean isScreenRights = ScreenRights.getScreenRights(requestHandalerKey, ctx,
        "patientClinicalData");
    if (!isScreenRights) {
      successMsg = "Permission Denied. Please check with Administrator.";
      patientClinicalDataMAP.put("return_code", "1003");
      patientClinicalDataMAP.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(json.deepSerialize(patientClinicalDataMAP));
      response.flushBuffer();
      return null;
    }

    String fromDate = request.getParameter("from_date");
    if (fromDate == null || fromDate.isEmpty()) {
      successMsg = "Mandatory fields are not supplied";
      patientClinicalDataMAP.put("return_code", "1002");
      patientClinicalDataMAP.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(json.deepSerialize(patientClinicalDataMAP));
      response.flushBuffer();
      return null;
    }

    try {
      fromDateRequestedTimeStamp = DateUtil.parseIso8601Timestamp(fromDate);
      if (fromDateRequestedTimeStamp == null) {
        isFromDateTimeStampFormat = false;
        fromDateRequestedTimeStamp = DateUtil.parseIso8601Date(fromDate);
      }
      if (fromDateRequestedTimeStamp == null) {
        successMsg = "Invalid input parameters supplied";
        patientClinicalDataMAP.put("return_code", "1021");
        patientClinicalDataMAP.put("return_message", successMsg);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write(json.deepSerialize(patientClinicalDataMAP));
        response.flushBuffer();
        return null;
      }
      String toDate = request.getParameter("to_date");
      if (toDate != null && toDate.length() > 0) {
        toDateRequestedTimeStamp = DateUtil.parseIso8601Timestamp(toDate);
        if (toDateRequestedTimeStamp == null) {
          isToDateTimeStampFormat = false;
          toDateRequestedTimeStamp = DateUtil.parseIso8601Date(toDate);
        }
      }
    } catch (ParseException ex) {
      successMsg = "Invalid input parameters supplied";
      patientClinicalDataMAP.put("return_code", "1021");
      patientClinicalDataMAP.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(json.deepSerialize(patientClinicalDataMAP));
      response.flushBuffer();
      return null;
    }
    if (isFromDateTimeStampFormat) {
      patientClinicalDataMAP.put("START_DATE",
          DateUtil.formatIso8601Timestamp((java.sql.Timestamp) fromDateRequestedTimeStamp));
    } else {
      patientClinicalDataMAP.put("START_DATE",
          DateUtil.formatIso8601Date((java.sql.Date) fromDateRequestedTimeStamp));
    }

    if (toDateRequestedTimeStamp != null) {
      if (isToDateTimeStampFormat) {
        patientClinicalDataMAP.put("END_DATE",
            DateUtil.formatIso8601Timestamp((java.sql.Timestamp) toDateRequestedTimeStamp));
      } else {
        patientClinicalDataMAP.put("END_DATE",
            DateUtil.formatIso8601Date((java.sql.Date) toDateRequestedTimeStamp));
      }
    } else {
      patientClinicalDataMAP.put("END_DATE", isoTimestamp);
    }
    long diff = 0;
    long toEpochMili = 0;
    long fromEpochMili = 0;
    if (toDateRequestedTimeStamp != null) {
      if (toDateRequestedTimeStamp instanceof java.sql.Timestamp) {
        toEpochMili = ((java.sql.Timestamp) toDateRequestedTimeStamp).getTime();
      } else {
        toEpochMili = ((java.sql.Date) toDateRequestedTimeStamp).getTime();
      }
    } else {
      toEpochMili = ((java.sql.Timestamp) currentTimeStamp).getTime();
    }
    if (fromDateRequestedTimeStamp instanceof java.sql.Timestamp) {
      fromEpochMili = ((java.sql.Timestamp) fromDateRequestedTimeStamp).getTime();
    } else {
      fromEpochMili = ((java.sql.Date) fromDateRequestedTimeStamp).getTime();
    }

    diff = toEpochMili - fromEpochMili;
    long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
    if (days < 0 || diff < 0) {
      successMsg = "To date can not be earlier than From date";
      patientClinicalDataMAP.put("return_code", "1022");
      patientClinicalDataMAP.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(json.deepSerialize(patientClinicalDataMAP));
      response.flushBuffer();
      return null;
    }
    if (days > 29) {
      successMsg = "Duration can not be more than 30 days";
      patientClinicalDataMAP.put("return_code", "1023");
      patientClinicalDataMAP.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(json.deepSerialize(patientClinicalDataMAP));
      response.flushBuffer();
      return null;
    }
    logger.info("Starting to retrieve data!!!");
    for (String section : PATIENT_DATA_SECTIONS) {
      if (section.equalsIgnoreCase("LAB")) {
        continue;
      }
      if (toDateRequestedTimeStamp != null) {
        patientClinicalDataMAP.put(section, PatientClinicalDataDAO.getQueryResult(section,
            sessionParameters, fromDateRequestedTimeStamp, toDateRequestedTimeStamp));
      } else {
        patientClinicalDataMAP.put(section, PatientClinicalDataDAO.getQueryResult(section,
            sessionParameters, fromDateRequestedTimeStamp, currentTimeStamp));
      }
    }
    logger.info("Data retrieval ends, sending response back to clinet");
    successMsg = "Success";
    patientClinicalDataMAP.put("return_code", "2001");
    patientClinicalDataMAP.put("return_message", successMsg);
    response.getWriter().write(json.deepSerialize(patientClinicalDataMAP));
    response.flushBuffer();

    return null;
  }

  /**
   * Get Patient Clinical Data test.
   * 
   * @param mapping  Action Mapping
   * @param form     Action Form
   * @param request  Servlet Request Object
   * @param response Servlet Response Object
   * @return Response
   * @throws Exception  exception
   */
  public ActionForward getPatientClinicalDataTest(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {

    String requestHandalerKey = ApiUtil.getRequestKey(request);
    Object requestedTimeStamp = null;
    Object toTimestamp = null;
    java.sql.Timestamp currentTimeStamp = DateUtil.getCurrentTimestamp();
    String isoTimestamp = DateUtil.formatIso8601Timestamp(currentTimeStamp);
    JSONSerializer json = JsonProcessor.getJSONParser();
    LinkedHashMap<String, Object> patientClinicalDataMAP = new LinkedHashMap<String, Object>();
    boolean isTimeStampFormat = true;
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
    if (sessionMap != null && !sessionMap.isEmpty()) {
      sessionParameters = (Map) sessionMap.get(requestHandalerKey);
      if (sessionParameters != null && !sessionParameters.isEmpty()) {
        // getting login time
        loginTime = (java.sql.Timestamp) sessionParameters.get("login_time");
        isValidRequest = (currentTime.getTime() - loginTime.getTime()) / 60000 <= validDuration;
      }
    }

    if (!isValidRequest) {
      successMsg = "invalid request token,please login again";
      logger.info("invalid request token,please login again");
      logger.info("sending the response back to the requesting server");
      patientClinicalDataMAP.put("return_code", "1001");
      patientClinicalDataMAP.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(json.deepSerialize(patientClinicalDataMAP));
      response.flushBuffer();
      return null;
    }

    // Check the rights for Clinical Analysis Data screen
    boolean isScreenRights = ScreenRights.getScreenRights(requestHandalerKey, ctx,
        "patientClinicalData");
    if (!isScreenRights) {
      successMsg = "Permission Denied. Please check with Administrator.";
      patientClinicalDataMAP.put("return_code", "1003");
      patientClinicalDataMAP.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(json.deepSerialize(patientClinicalDataMAP));
      response.flushBuffer();
      return null;
    }

    String fromDate = request.getParameter("from_date");
    if (fromDate == null || fromDate.isEmpty()) {
      successMsg = "Mandatory fields are not supplied";
      patientClinicalDataMAP.put("return_code", "1002");
      patientClinicalDataMAP.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(json.deepSerialize(patientClinicalDataMAP));
      response.flushBuffer();
      return null;
    }

    try {
      requestedTimeStamp = DateUtil.parseIso8601Timestamp(fromDate);
      if (requestedTimeStamp == null) {
        isTimeStampFormat = false;
        requestedTimeStamp = DateUtil.parseIso8601Date(fromDate);
      }
      if (requestedTimeStamp == null) {
        successMsg = "Invalid input parameters supplied";
        patientClinicalDataMAP.put("return_code", "1021");
        patientClinicalDataMAP.put("return_message", successMsg);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write(json.deepSerialize(patientClinicalDataMAP));
        response.flushBuffer();
        return null;
      }
      String toDate = request.getParameter("to_date");
      toTimestamp = DateUtil.parseIso8601Timestamp(toDate);
      if (toTimestamp == null) {
        isToDateTimeStampFormat = false;
        toTimestamp = DateUtil.parseIso8601Date(toDate);
      }
      if (toTimestamp == null) {
        successMsg = "Invalid input parameters supplied";
        patientClinicalDataMAP.put("return_code", "1021");
        patientClinicalDataMAP.put("return_message", successMsg);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write(json.deepSerialize(patientClinicalDataMAP));
        response.flushBuffer();
        return null;
      }
    } catch (ParseException ex) {
      successMsg = "Invalid input parameters supplied";
      patientClinicalDataMAP.put("return_code", "1021");
      patientClinicalDataMAP.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(json.deepSerialize(patientClinicalDataMAP));
      response.flushBuffer();
      return null;
    }
    if (isTimeStampFormat) {
      patientClinicalDataMAP.put("START_DATE",
          DateUtil.formatIso8601Timestamp((java.sql.Timestamp) requestedTimeStamp));
    } else {
      patientClinicalDataMAP.put("START_DATE",
          DateUtil.formatIso8601Date((java.sql.Date) requestedTimeStamp));
    }
    if (isToDateTimeStampFormat) {
      patientClinicalDataMAP.put("END_DATE",
          DateUtil.formatIso8601Timestamp((java.sql.Timestamp) toTimestamp));
    } else {
      patientClinicalDataMAP.put("END_DATE",
          DateUtil.formatIso8601Date((java.sql.Date) toTimestamp));
    }
    // patientClinicalDataMAP.put("END_DATE", isoTimestamp);

    for (String section : PATIENT_DATA_SECTIONS) {
      if (!section.equalsIgnoreCase("LAB")) {
        patientClinicalDataMAP.put(section, PatientClinicalDataDAO.getQueryResult(section,
            sessionParameters, requestedTimeStamp, toTimestamp));
      } else {
        Connection con = com.insta.instaapi.common.DbUtil
            .getConnection((String) sessionParameters.get("hospital_name"));
        List labResults = PatientClinicalDataDAO.getQueryResult(section, sessionParameters,
            requestedTimeStamp, toTimestamp);
        List newLabResults = new ArrayList();
        for (int i = 0; i < labResults.size(); i++) {
          Map beanMap = (Map) labResults.get(i);
          int resultlabelId = (Integer) beanMap.get("resultlabel_id");
          HashMap map = new HashMap();
          map.put("resultlabel_id", resultlabelId);
          map.put("sample_date", new java.sql.Timestamp(new java.util.Date().getTime()));
          /*
           * Map<String, Object> pd = new HashMap<String, Object>(); pd.put("mr_no",
           * bean.get("mr_no"));
           */
          String patientId = (String) beanMap.get("patient_id");
          // pd.put("patient_id", patient_id);
          BasicDynaBean patientDetails = VisitDetailsDAO.getPatientVisitDetailsBean(con,
              patientId);
          boolean isInpatient = (patientDetails == null);
          if (patientDetails == null) {
            patientDetails = PatientDetailsDAO.getIncomingPatientDetails(patientId);
          }
          BasicDynaBean referenceResultRangeBean = LaboratoryBO.getResultRange(con, map,
              patientDetails.getMap());
          Map temp = new HashMap();
          temp.putAll(beanMap);
          if (referenceResultRangeBean != null 
              && referenceResultRangeBean.get("min_normal_value") != null) {
            temp.put("min_normal_value", referenceResultRangeBean.get("min_normal_value"));
          } else {
            temp.put("min_normal_value", "");
          }
          if (referenceResultRangeBean != null
              && referenceResultRangeBean.get("max_normal_value") != null) {
            temp.put("max_normal_value", referenceResultRangeBean.get("max_normal_value"));
          } else {
            temp.put("max_normal_value", "");
          }
          newLabResults.add(temp);
          beanMap = null;
        }
        labResults = null;
        patientClinicalDataMAP.put(section, newLabResults);
      }
    }
    successMsg = "Success";
    patientClinicalDataMAP.put("return_code", "2001");
    patientClinicalDataMAP.put("return_message", successMsg);
    writeToTextFile("temp.txt", json.deepSerialize(patientClinicalDataMAP));
    /*
     * response.getWriter().write(json.deepSerialize(patientClinicalDataMAP));
     * response.flushBuffer();
     */

    return null;
  }

  /**
   * Write to text file.
   * @param fileName      file name to write to
   * @param content       content to write
   * @throws IOException  IO exception
   */
  public static void writeToTextFile(String fileName, String content) throws IOException {
    File file = new File(fileName);
    // if file doesnt exists, then create it
    if (!file.exists()) {
      file.createNewFile();
    }
    FileWriter fw = new FileWriter(file.getAbsoluteFile());
    BufferedWriter bw = new BufferedWriter(fw);
    bw.write(content);
    bw.close();
  }

  /**
   * Get Patient demographics.
   * 
   * @param mapping  Action Mapping
   * @param form     Action Form
   * @param request  Servlet Request Object
   * @param response Servlet Response Object
   * @return Response
   * @throws Exception  exception
   */
  public ActionForward getPatientDemographics(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {

    String requestHandalerKey = ApiUtil.getRequestKey(request);
    Object fromDateRequestedTimeStamp = null;
    Object toDateRequestedTimeStamp = null;
    java.sql.Timestamp currentTimeStamp = DateUtil.getCurrentTimestamp();
    String isoTimestamp = DateUtil.formatIso8601Timestamp(currentTimeStamp);
    JSONSerializer json = JsonProcessor.getJSONParser();
    LinkedHashMap<String, Object> patientClinicalDataMAP = new LinkedHashMap<String, Object>();
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
    if (sessionMap != null && !sessionMap.isEmpty()) {
      sessionParameters = (Map) sessionMap.get(requestHandalerKey);
      if (sessionParameters != null && !sessionParameters.isEmpty()) {
        // getting login time
        loginTime = (java.sql.Timestamp) sessionParameters.get("login_time");
        isValidRequest = (currentTime.getTime() - loginTime.getTime()) / 60000 <= validDuration;
      }
    }

    if (!isValidRequest) {
      successMsg = "invalid request token,please login again";
      logger.info("invalid request token,please login again");
      logger.info("sending the response back to the requesting server");
      patientClinicalDataMAP.put("return_code", "1001");
      patientClinicalDataMAP.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(json.deepSerialize(patientClinicalDataMAP));
      response.flushBuffer();
      return null;
    }

    // Check the rights for Clinical Analysis Data screen
    boolean isScreenRights = ScreenRights.getScreenRights(requestHandalerKey, ctx,
        "patientDetails");
    if (!isScreenRights) {
      successMsg = "Permission Denied. Please check with Administrator.";
      patientClinicalDataMAP.put("return_code", "1003");
      patientClinicalDataMAP.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(json.deepSerialize(patientClinicalDataMAP));
      response.flushBuffer();
      return null;
    }

    String fromDate = request.getParameter("from_date");
    if (fromDate == null || fromDate.isEmpty()) {
      successMsg = "Mandatory fields are not supplied";
      patientClinicalDataMAP.put("return_code", "1002");
      patientClinicalDataMAP.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(json.deepSerialize(patientClinicalDataMAP));
      response.flushBuffer();
      return null;
    }

    try {
      fromDateRequestedTimeStamp = DateUtil.parseIso8601Timestamp(fromDate);
      if (fromDateRequestedTimeStamp == null) {
        isFromDateTimeStampFormat = false;
        fromDateRequestedTimeStamp = DateUtil.parseIso8601Date(fromDate);
      }
      if (fromDateRequestedTimeStamp == null) {
        successMsg = "Invalid input parameters supplied";
        patientClinicalDataMAP.put("return_code", "1021");
        patientClinicalDataMAP.put("return_message", successMsg);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write(json.deepSerialize(patientClinicalDataMAP));
        response.flushBuffer();
        return null;
      }
      String toDate = request.getParameter("to_date");
      if (toDate != null && toDate.length() > 0) {
        toDateRequestedTimeStamp = DateUtil.parseIso8601Timestamp(toDate);
        if (toDateRequestedTimeStamp == null) {
          isToDateTimeStampFormat = false;
          toDateRequestedTimeStamp = DateUtil.parseIso8601Date(toDate);
        }
      }
    } catch (ParseException ex) {
      successMsg = "Invalid input parameters supplied";
      patientClinicalDataMAP.put("return_code", "1021");
      patientClinicalDataMAP.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(json.deepSerialize(patientClinicalDataMAP));
      response.flushBuffer();
      return null;
    }
    if (isFromDateTimeStampFormat) {
      patientClinicalDataMAP.put("START_DATE",
          DateUtil.formatIso8601Timestamp((java.sql.Timestamp) fromDateRequestedTimeStamp));
    } else {
      patientClinicalDataMAP.put("START_DATE",
          DateUtil.formatIso8601Date((java.sql.Date) fromDateRequestedTimeStamp));
    }
    if (toDateRequestedTimeStamp != null) {
      if (isToDateTimeStampFormat) {
        patientClinicalDataMAP.put("END_DATE",
            DateUtil.formatIso8601Timestamp((java.sql.Timestamp) toDateRequestedTimeStamp));
      } else {
        patientClinicalDataMAP.put("END_DATE",
            DateUtil.formatIso8601Date((java.sql.Date) toDateRequestedTimeStamp));
      }
    } else {
      patientClinicalDataMAP.put("END_DATE", isoTimestamp);
    }
    long diff = 0;
    long toEpochMili = 0;
    long fromEpochMili = 0;
    if (toDateRequestedTimeStamp != null) {
      if (toDateRequestedTimeStamp instanceof java.sql.Timestamp) {
        toEpochMili = ((java.sql.Timestamp) toDateRequestedTimeStamp).getTime();
      } else {
        toEpochMili = ((java.sql.Date) toDateRequestedTimeStamp).getTime();
      }
    } else {
      toEpochMili = ((java.sql.Timestamp) currentTimeStamp).getTime();
    }
    if (fromDateRequestedTimeStamp instanceof java.sql.Timestamp) {
      fromEpochMili = ((java.sql.Timestamp) fromDateRequestedTimeStamp).getTime();
    } else {
      fromEpochMili = ((java.sql.Date) fromDateRequestedTimeStamp).getTime();
    }

    diff = toEpochMili - fromEpochMili;
    long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
    if (days < 0 || diff < 0) {
      successMsg = "To date can not be earlier than From date";
      patientClinicalDataMAP.put("return_code", "1022");
      patientClinicalDataMAP.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(json.deepSerialize(patientClinicalDataMAP));
      response.flushBuffer();
      return null;
    }
    if (days > 29) {
      successMsg = "Duration can not be more than 30 days";
      patientClinicalDataMAP.put("return_code", "1023");
      patientClinicalDataMAP.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(json.deepSerialize(patientClinicalDataMAP));
      response.flushBuffer();
      return null;
    }
    if (toDateRequestedTimeStamp != null) {
      patientClinicalDataMAP.put("PATIENT_DETAILS",
          PatientClinicalDataDAO.getQueryResult("PATIENT_DETAILS", sessionParameters,
              fromDateRequestedTimeStamp, toDateRequestedTimeStamp));
    } else {
      patientClinicalDataMAP.put("PATIENT_DETAILS", PatientClinicalDataDAO.getQueryResult(
          "PATIENT_DETAILS", sessionParameters, fromDateRequestedTimeStamp, currentTimeStamp));
    }
    successMsg = "Success";
    patientClinicalDataMAP.put("return_code", "2001");
    patientClinicalDataMAP.put("return_message", successMsg);
    response.getWriter().write(json.deepSerialize(patientClinicalDataMAP));
    response.flushBuffer();
    
    return null;
  }
}
