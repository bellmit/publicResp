package com.insta.instaapi.patient.mobileaccess;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.GenericDAO;
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

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** PatientMobileAccessAction class. */
public class PatientMobileAccessAction extends DispatchAction {
  static Logger logger = LoggerFactory.getLogger(PatientMobileAccessAction.class);

  /**
   * enables patient Mobile access.
   *
   * @param mapping mapping paramter.
   * @param form form parameter.
   * @param request request object.
   * @param response response object.
   * @return reyurns action forward.
   * @throws IOException Signals that an I/O exception has occurred
   * @throws ServletException may throw servlet exception
   * @throws SQLException may throw sql exception
   */
  public ActionForward enablePatientMobileAccess(
      ActionMapping mapping,
      ActionForm form,
      HttpServletRequest request,
      HttpServletResponse response)
      throws IOException, ServletException, SQLException {

    String requestHandalerKey = ApiUtil.getRequestKey(request);
    boolean isAValidRequest = false;
    Connection con = null;
    JSONSerializer js = JsonProcessor.getJSONParser();
    ServletContext ctx = servlet.getServletContext();
    Map<String, Object> sessionMap = ServletContextUtil.getContextParametersMap(ctx);
    logger.info("getting session related data from conetxt" + sessionMap);
    Map sessionParameters = null;
    String successMsg = "";
    boolean success = true;
    GenericDAO patDao = new GenericDAO("patient_details");
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
          isAValidRequest = true;
        } else {
          isAValidRequest = false;
        }
        if ((boolean) sessionParameters.get("patient_login")) {
          mrNo = (String) sessionParameters.get("customer_user_id");
        }
      }
    }
    response.setContentType("application/json");
    response.setHeader("Cache-Control", "no-cache");
    // response.setHeader("Access-Control-Allow-Origin", "*");
    Map<String, Object> patientMobileAceessDataMap = new HashMap<String, Object>();
    if (!isAValidRequest) {
      successMsg = "invalid request token,please login again";
      logger.info("invalid request token,please login again");
      logger.info("sending the response back to the requesting server");
      patientMobileAceessDataMap.put("return_code", "1001");
      patientMobileAceessDataMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(patientMobileAceessDataMap));
      response.flushBuffer();
      return null;
    }
    // Check the rights for Edit Visit Details screen
    boolean isScreenRights =
        ScreenRights.getScreenRights(requestHandalerKey, ctx, "PatientMobileAccess");
    if (!isScreenRights) {
      successMsg = "Permission Denied. Please check with Administrator.";
      patientMobileAceessDataMap.put("return_code", "1003");
      patientMobileAceessDataMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(patientMobileAceessDataMap));
      response.flushBuffer();
      return null;
    }
    String dateOfBirth = request.getParameter("date_of_birth");
    String mobileNo = request.getParameter("mobile_no");
    String gender = request.getParameter("gender");
    if (mrNo == null
        || "".equals(mrNo)
        || dateOfBirth == null
        || "".equals(dateOfBirth)
        || mobileNo == null
        || "".equals(mobileNo)
        || gender == null
        || "".equals(gender)) {
      successMsg = "Mandatory fields are not supplied";
      logger.info("Mandatory fields are not supplied");
      patientMobileAceessDataMap.put("return_code", "1002");
      patientMobileAceessDataMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(patientMobileAceessDataMap));
      response.flushBuffer();
      return null;
    }
    if (!dateOfBirth.matches("[0-9]{4}[-]{1}[0-9]{2}[-]{1}[0-9]{2}")) {
      successMsg = "date_of_birth format is not valid, please enter yyyy-MM-dd format";
      logger.info("date_of_birth format is not valid, please enter yyyy-MM-dd format");
      patientMobileAceessDataMap.put("return_code", "1021");
      patientMobileAceessDataMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(patientMobileAceessDataMap));
      response.flushBuffer();
      return null;
    }
    if (isAValidRequest) {
      try {
        con =
            com.insta.instaapi.common.DbUtil.getConnection(
                (String) sessionParameters.get("hospital_name"));
        con.setAutoCommit(false);
        logger.info(
            "getting connection object"
                + con
                + "----"
                + (String) sessionParameters.get("hospital_name"));
        Map<String, Object> filterMap = new HashMap<String, Object>();
        filterMap.put("mr_no", mrNo);
        filterMap.put("patient_gender", gender.trim());
        List<String> patientDetailsColumns = new ArrayList<String>();
        patientDetailsColumns.add("mr_no");
        patientDetailsColumns.add("patient_phone");
        patientDetailsColumns.add("dateofbirth");
        patientDetailsColumns.add("patient_gender");
        patientDetailsColumns.add("mobile_access");
        patientDetailsColumns.add("mobile_password");
        List<BasicDynaBean> patientDetailsBeans =
            patDao.listAll(con, patientDetailsColumns, filterMap, null);
        if (patientDetailsBeans != null && patientDetailsBeans.size() > 0) {
          BasicDynaBean patientDetails = patientDetailsBeans.get(0);
          String mrno = (String)patientDetails.get("mr_no");
          String patientPhone = (String) patientDetails.get("patient_phone");
          String patientGender = (String) patientDetails.get("patient_gender");
          Boolean mobileAccess = (Boolean) patientDetails.get("mobile_access");
          String dateofbirth = null;
          if (patientDetails.get("dateofbirth") != null) {
            java.sql.Date date = (java.sql.Date) patientDetails.get("dateofbirth");
            dateofbirth = DateUtil.formatIso8601Date(date);
          }
          if (!mobileAccess) {
            if (mrNo.equals(mrno)
                && mobileNo.equals(patientPhone)
                && ((dateofbirth != null && dateofbirth.equals(dateOfBirth))
                    || dateofbirth == null)) {
              success = PatientMobileAccessDAO.updatePassword(con, mrno) > 0;
              if (success) {
                BasicDynaBean patientDetailsBean = patDao.getBean(con);
                patDao.loadByteaRecords(con, patientDetailsBean, "mr_no", mrNo);
                patientMobileAceessDataMap.put(
                    "patient_mobile_password", (String) patientDetailsBean.get("mobile_password"));
                patientMobileAceessDataMap.put("return_code", "2001");
                patientMobileAceessDataMap.put("return_message", "Success");
              } else {
                successMsg = "Failed to create the password";
                logger.info("Failed to create the password");
                patientMobileAceessDataMap.put("return_code", "1022");
                patientMobileAceessDataMap.put("return_message", successMsg);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                success = false;
              }
            } else {
              successMsg = "Given values are invalid";
              logger.info("Given values are invalid");
              patientMobileAceessDataMap.put("return_code", "1023");
              patientMobileAceessDataMap.put("return_message", successMsg);
              response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
              success = false;
            }
          } else {
            // successMsg ="Patient has already been given the mobile access";
            logger.info("Patient has already been given the mobile access");
            patientMobileAceessDataMap.put(
                "patient_mobile_password", (String) patientDetails.get("mobile_password"));
            patientMobileAceessDataMap.put("return_code", "2001");
            patientMobileAceessDataMap.put("return_message", "Success");
          }

        } else {
          successMsg = "no patient found with given parameter";
          logger.info("no patient found with given parameter");
          patientMobileAceessDataMap.put("return_code", "1024");
          patientMobileAceessDataMap.put("return_message", successMsg);
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          success = false;
        }

      } finally {
        com.insta.instaapi.common.DbUtil.commitClose(con, success);
      }
    }

    logger.info("sending the response back to the requesting server");
    response.getWriter().write(js.deepSerialize(patientMobileAceessDataMap));
    response.flushBuffer();
    return null;
  }
}
