package com.insta.instaapi.patient.registration;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.bob.hms.diag.ohsampleregistration.IncomingPatientDAO;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.master.AreaMaster.AreaMasterDAO;
import com.insta.hms.master.StateMaster.StateMasterDAO;
import com.insta.hms.patientsdetailssearch.PatientsDetailsSearchDAO;
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
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author mithun.saha
 */
public class GeneralRegistrationAction extends DispatchAction {
  static Logger logger = LoggerFactory.getLogger(GeneralRegistrationAction.class);

  /**
   * do pre registration.
   *
   * @param mapping mapping paramter
   * @param form form parameter
   * @param request request object
   * @param response response object
   * @return returns action forward.
   * @throws IOException Signals that an I/O exception has occurred
   * @throws ServletException may throw Servlet Exception
   * @throws SQLException may throw SQL Exception
   * @throws NoSuchAlgorithmException may throw NoSuchAlgorithmException
   * @throws ParseException may throw ParseException
   */
  public ActionForward doPreRegistration(
      ActionMapping mapping,
      ActionForm form,
      HttpServletRequest request,
      HttpServletResponse response)
      throws IOException, ServletException, SQLException, NoSuchAlgorithmException, ParseException {
    logger.info("getting pre registration related parameters");
    String requestHandalerKey = ApiUtil.getRequestKey(request);
    boolean isAValidRequest = false;
    ServletContext ctx = servlet.getServletContext();
    Map<String, Object> sessionMap = ServletContextUtil.getContextParametersMap(ctx);
    logger.info("getting session related data from conetxt" + sessionMap);
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
    // HttpSession session= SessionUtil.getSession(requestHandalerKey, ctx);
    JSONSerializer js = JsonProcessor.getJSONParser();
    BasicDynaBean patientBean = null;
    Map<String, Object> patientPreRegistrationSuccessMap = new HashMap<String, Object>();
    boolean success = false;
    String successMsg = "success";
    response.setContentType("application/json");
    response.setHeader("Cache-Control", "no-cache");
    // response.setHeader("Access-Control-Allow-Origin", "*");
    String returnCode = "";

    if (!isAValidRequest) {
      successMsg = "invalid request token,please login again";
      logger.info("invalid request token,please login again");
      logger.info("sending the response back to the requesting server");
      // patientPreRegistrationSuccessMap.put("success", false);
      patientPreRegistrationSuccessMap.put("return_code", "1001");
      patientPreRegistrationSuccessMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(patientPreRegistrationSuccessMap));
      response.flushBuffer();
      return null;
    }

    // Check the rights for Pre-Registration screen
    boolean isScreenRights =
        ScreenRights.getScreenRights(requestHandalerKey, ctx, "preRegistration");
    if (!isScreenRights) {
      successMsg = "Permission Denied. Please check with Administrator.";
      patientPreRegistrationSuccessMap.put("return_code", "1003");
      patientPreRegistrationSuccessMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(patientPreRegistrationSuccessMap));
      response.flushBuffer();
      return null;
    }

    if (isAValidRequest) {
      String middleName = request.getParameter("middle_name");
      middleName = middleName == null ? "" : middleName;
      String lastName = request.getParameter("last_name");
      lastName = lastName == null ? "" : lastName;
      String dateOfBirth = request.getParameter("dateOfBirth");
      String salutation = request.getParameter("salutation");
      String gender = request.getParameter("patient_gender");
      String mobileNo = request.getParameter("patient_phone");
      String patientCategoryId = request.getParameter("patient_category_id");
      String patientArea = request.getParameter("patient_area");
      patientArea = (patientArea == null || patientArea.equals("")) ? null : patientArea;
      String patientAddress = request.getParameter("patient_address");
      patientAddress = (patientAddress == null || patientAddress.isEmpty()) ? "" : patientAddress;
      String patientCity = request.getParameter("patient_city");
      String patientState = request.getParameter("patient_state");
      String firstName = request.getParameter("patient_name");
      if ((firstName == null || firstName.trim().equals(""))
          || (dateOfBirth == null || dateOfBirth.trim().equals(""))
          || (salutation == null || salutation.trim().isEmpty())
          || (gender == null || gender.trim().isEmpty())
          || (patientCity == null || patientCity.trim().isEmpty())
          || (mobileNo == null || mobileNo.trim().isEmpty())
          || (patientState == null || patientState.trim().isEmpty())
          || (patientCategoryId == null || patientCategoryId.trim().isEmpty())) {
        successMsg = "Mandatory fields are not supplied";
        patientPreRegistrationSuccessMap.put("return_code", "1002");
        // patientPreRegistrationSuccessMap.put("success", false);
        patientPreRegistrationSuccessMap.put("return_message", successMsg);
        logger.info(successMsg);
        logger.info("sending the response back to the requesting server");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write(js.deepSerialize(patientPreRegistrationSuccessMap));
        response.flushBuffer();
        return null;
      }

      if (mobileNo.indexOf("+") == -1 || mobileNo.indexOf("+") > 0) {
        successMsg = "Phone number does not contain the country code";
        patientPreRegistrationSuccessMap.put("return_code", "1021");
        // patientPreRegistrationSuccessMap.put("success", false);
        patientPreRegistrationSuccessMap.put("return_message", successMsg);
        logger.info(successMsg);
        logger.info("sending the response back to the requesting server");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write(js.deepSerialize(patientPreRegistrationSuccessMap));
        response.flushBuffer();
        return null;
      }
      if (validateNumbers(mobileNo.substring(1))) {
        successMsg = "Phone number contains invalid characters";
        patientPreRegistrationSuccessMap.put("return_code", "1022");
        // patientPreRegistrationSuccessMap.put("success", false);
        patientPreRegistrationSuccessMap.put("return_message", successMsg);
        logger.info(successMsg);
        logger.info("sending the response back to the requesting server");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write(js.deepSerialize(patientPreRegistrationSuccessMap));
        response.flushBuffer();
        return null;
      }
      if (validateString(firstName)) {
        successMsg = "First name contains invalid characters";
        patientPreRegistrationSuccessMap.put("return_code", "1023");
        // patientPreRegistrationSuccessMap.put("success", false);
        patientPreRegistrationSuccessMap.put("return_message", successMsg);
        logger.info(successMsg);
        logger.info("sending the response back to the requesting server");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write(js.deepSerialize(patientPreRegistrationSuccessMap));
        response.flushBuffer();
        return null;
      }
      if (validateString(middleName)) {
        successMsg = "Middle name contains invalid characters";
        patientPreRegistrationSuccessMap.put("return_code", "1024");
        // patientPreRegistrationSuccessMap.put("success", false);
        patientPreRegistrationSuccessMap.put("return_message", successMsg);
        logger.info(successMsg);
        logger.info("sending the response back to the requesting server");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write(js.deepSerialize(patientPreRegistrationSuccessMap));
        response.flushBuffer();
        return null;
      }
      if (validateString(lastName)) {
        successMsg = "Last name contains invalid characters";
        patientPreRegistrationSuccessMap.put("return_code", "1025");
        // patientPreRegistrationSuccessMap.put("success", false);
        patientPreRegistrationSuccessMap.put("return_message", successMsg);
        logger.info(successMsg);
        logger.info("sending the response back to the requesting server");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write(js.deepSerialize(patientPreRegistrationSuccessMap));
        response.flushBuffer();
        return null;
      }
      if (gender.length() != 1
          || (!gender.equalsIgnoreCase("m")
              && !gender.equalsIgnoreCase("f")
              && !gender.equalsIgnoreCase("o"))) {
        successMsg = "Gender is invalid";
        patientPreRegistrationSuccessMap.put("return_code", "1026");
        // patientPreRegistrationSuccessMap.put("success", false);
        patientPreRegistrationSuccessMap.put("return_message", successMsg);
        logger.info(successMsg);
        logger.info("sending the response back to the requesting server");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write(js.deepSerialize(patientPreRegistrationSuccessMap));
        response.flushBuffer();
        return null;
      }

      Connection con = null;
      PatientDetailsDAO dao = new PatientDetailsDAO();
      try {
        con =
            com.insta.instaapi.common.DbUtil.getConnection(
                (String) sessionParameters.get("hospital_name"));
        logger.info("getting connection from database" + con);
        con.setAutoCommit(false);
        patientBean = dao.getBean(con);
        patientBean.set("patient_name", firstName);
        patientBean.set("middle_name", middleName);
        patientBean.set("last_name", lastName);
        patientBean.set("salutation", salutation);
        patientBean.set("patient_phone", mobileNo);
        java.sql.Date date = null;
        try {
          date = DateUtil.parseIso8601Date(dateOfBirth);
        } catch (ParseException pe) {
          successMsg = "Date of birth is invalid";
          patientPreRegistrationSuccessMap.put("return_code", "1027");
          // patientPreRegistrationSuccessMap.put("success", false);
          patientPreRegistrationSuccessMap.put("return_message", successMsg);
          logger.info(successMsg);
          logger.info("sending the response back to the requesting server");
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          response.getWriter().write(js.deepSerialize(patientPreRegistrationSuccessMap));
          response.flushBuffer();
          return null;
        }
        Calendar yrsLaterCal = Calendar.getInstance();
        yrsLaterCal.add(Calendar.YEAR, 120);
        Calendar yrsBeforeCal = Calendar.getInstance();
        yrsBeforeCal.add(Calendar.YEAR, -120);
        yrsBeforeCal.add(Calendar.DAY_OF_MONTH, -1);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        if (cal.compareTo(yrsBeforeCal) < 0 || cal.compareTo(yrsLaterCal) > 0) {
          successMsg = "Date of birth is invalid";
          patientPreRegistrationSuccessMap.put("return_code", "1027");
          // patientPreRegistrationSuccessMap.put("success", false);
          patientPreRegistrationSuccessMap.put("return_message", successMsg);
          logger.info(successMsg);
          logger.info("sending the response back to the requesting server");
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          response.getWriter().write(js.deepSerialize(patientPreRegistrationSuccessMap));
          response.flushBuffer();
          return null;
        }
        patientBean.set("dateofbirth", date);
        patientBean.set("resource_captured_from", "register");
        patientBean.set("patient_gender", gender);
        patientBean.set("patient_state", patientState);
        patientBean.set("patient_city", patientCity);
        patientBean.set(
            "patient_category_id",
            (patientCategoryId != null && !patientCategoryId.isEmpty())
                ? Integer.parseInt(patientCategoryId)
                : 1);
        BasicDynaBean countryBean =
            new StateMasterDAO().findByKey(con, "state_id", request.getParameter("patient_state"));
        patientBean.set("country", countryBean != null ? countryBean.get("country_id") : null);
        patientBean.set("patient_address", patientAddress);
        patientBean.set("patient_area", patientArea);
        patientBean.set("country", countryBean != null ? countryBean.get("country_id") : null);
        patientBean.set("user_name", "InstaAPI");
        patientBean.set("mod_time", DateUtil.getCurrentTimestamp());
        int result = registerPatient(con, patientBean);
        if (result == 0) {
          success = true;
          returnCode = "2001";
          successMsg = "Success";
          // patientPreRegistrationSuccessMap.put("success", true);
          patientPreRegistrationSuccessMap.put("mr_no", (String) patientBean.get("mr_no"));
        } else if (result == 1) {
          success = false;
        } else if (result == 2) {
          successMsg = "Patient is already registered.";
          returnCode = "1028";
          patientPreRegistrationSuccessMap.put("return_message", successMsg);
          patientPreRegistrationSuccessMap.put("return_code", returnCode);
          logger.info("sending the response back to the requesting server");
          response.getWriter().write(js.deepSerialize(patientPreRegistrationSuccessMap));
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          response.flushBuffer();
          return null;
        }
      } finally {
        com.insta.instaapi.common.DbUtil.commitClose(con, success);
      }
    }
    if (!success) {
      successMsg = "Failed to Pre Register the Patient.";
      returnCode = "1029";
      // patientPreRegistrationSuccessMap.put("success", false);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
    patientPreRegistrationSuccessMap.put("return_message", successMsg);
    patientPreRegistrationSuccessMap.put("return_code", returnCode);
    logger.info("sending the response back to the requesting server");
    response.getWriter().write(js.deepSerialize(patientPreRegistrationSuccessMap));
    response.flushBuffer();
    return null;
  }

  private boolean validateString(String str) {
    Pattern pattern = Pattern.compile("[^a-z ]", Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(str);
    boolean result = matcher.find();
    return result;
  }

  private boolean validateNumbers(String str) {
    Pattern pattern = Pattern.compile("[^0-9]", Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(str);
    boolean result = matcher.find();
    return result;
  }

  /**
   * Get master data.
   *
   * @param mapping mapping paramter
   * @param form form paramter
   * @param request request object
   * @param response response object
   * @return returns action forward
   * @throws IOException Signals that an I/O exception has occurred
   * @throws ServletException may throw Servlet Exception
   * @throws SQLException may throw Sql Exception
   * @throws NoSuchAlgorithmException may throw NoSuchAlgorithmException
   * @throws ParseException may throw parse Exception
   */
  public ActionForward getRegMasterData(
      ActionMapping mapping,
      ActionForm form,
      HttpServletRequest request,
      HttpServletResponse response)
      throws IOException, ServletException, SQLException, NoSuchAlgorithmException, ParseException {
    logger.info("getting master related  data");

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
    response.setContentType("application/json");
    response.setHeader("Cache-Control", "no-cache");
    // response.setHeader("Access-Control-Allow-Origin", "*");
    Map<String, Object> masterDataMap = new HashMap<String, Object>();

    if (!isAValidRequest) {
      successMsg = "invalid request token,please login again";
      logger.info("invalid request token,please login again");
      logger.info("sending the response back to the requesting server");
      masterDataMap.put("return_code", "1001");
      masterDataMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(masterDataMap));
      response.flushBuffer();
      return null;
    }

    // Check the rights for States, Cities, Salutations and Patient Category Master screen
    boolean isScreenRights = ScreenRights.getScreenRights(requestHandalerKey, ctx, "regMasterData");
    if (!isScreenRights) {
      successMsg = "Permission Denied. Please check with Administrator.";
      masterDataMap.put("return_code", "1003");
      masterDataMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(masterDataMap));
      response.flushBuffer();
      return null;
    }

    if (isAValidRequest) {
      try {
        filterMap.put("status", "A");
        con =
            com.insta.instaapi.common.DbUtil.getConnection(
                (String) sessionParameters.get("hospital_name"));
        logger.info(
            "getting connection object"
                + con
                + "----"
                + (String) sessionParameters.get("hospital_name"));

        List<String> salutationColumns = new ArrayList<String>();
        salutationColumns.add("salutation_id");
        salutationColumns.add("salutation");
        salutationColumns.add("gender");

        /*List<String> patientCategoriesColums = new ArrayList<String>();
        patientCategoriesColums.add("category_id");
        patientCategoriesColums.add("category_name");
        patientCategoriesColums.add("center_id");   */

        List<BasicDynaBean> allCities = GeneralRegistrationDAO.getCityDetails(con);
        List<BasicDynaBean> allStates = GeneralRegistrationDAO.getStateDetails(con);
        List<BasicDynaBean> allCountries = GeneralRegistrationDAO.getCountryDetails(con);
        List<BasicDynaBean> allGovtIdentifiers =
            GeneralRegistrationDAO.getGovtIdentifierDetails(con);
        List<BasicDynaBean> allSalutaions =
            new GenericDAO("salutation_master")
                .listAll(con, salutationColumns, filterMap, "salutation");
        // List<BasicDynaBean> allPatientCategories = new
        // GenericDAO("patient_category_master").listAll(con,patientCategoriesColums, filterMap,
        // "category_name");
        List<BasicDynaBean> allPatientCategories =
            GeneralRegistrationDAO.getPatientCategoryDetails(con);
        masterDataMap.put("allCities", ConversionUtils.listBeanToListMap(allCities));
        masterDataMap.put("allStates", ConversionUtils.listBeanToListMap(allStates));
        masterDataMap.put("allCountries", ConversionUtils.listBeanToListMap(allCountries));
        masterDataMap.put(
            "allGovtIdentifiers", ConversionUtils.listBeanToListMap(allGovtIdentifiers));
        masterDataMap.put(
            "allPatientCategories", ConversionUtils.listBeanToListMap(allPatientCategories));
        masterDataMap.put("allSalutations", ConversionUtils.listBeanToListMap(allSalutaions));
        successMsg = "Success";
        masterDataMap.put("return_message", successMsg);
        masterDataMap.put("return_code", "2001");

      } finally {

        com.insta.instaapi.common.DbUtil.closeConnections(con, null);
      }
      logger.info("getting master realted data....");
    }

    logger.info("sending the response back to the requesting server");
    response.getWriter().write(js.deepSerialize(masterDataMap));
    response.flushBuffer();
    return null;
  }

  /**
   * Get patient Details.
   *
   * @param mapping mapping parameter
   * @param form form paramter
   * @param request request object
   * @param response response object
   * @return returns patient details.
   * @throws IOException Signals that an I/O exception has occurred
   * @throws ServletException may throw Servlet Exception
   * @throws SQLException may throw SQL Exception
   */
  public ActionForm getPatientDetails(
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
    PatientDetailsDAO dao = new PatientDetailsDAO();
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
    Map<String, Object> patientDetailsDataMap = new HashMap<String, Object>();
    if (mrNo == null || "".equals(mrNo)) {
      successMsg = "Mandatory fields are not supplied";
      logger.info("Mandatory fields are not supplied");
      patientDetailsDataMap.put("return_code", "1002");
      patientDetailsDataMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(patientDetailsDataMap));
      response.flushBuffer();
      return null;
    }

    if (!isAValidRequest) {
      successMsg = "invalid request token,please login again";
      logger.info("invalid request token,please login again");
      logger.info("sending the response back to the requesting server");
      patientDetailsDataMap.put("return_code", "1001");
      patientDetailsDataMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(patientDetailsDataMap));
      response.flushBuffer();
      return null;
    }

    // Check the rights for Patient Search screen
    boolean isScreenRights =
        ScreenRights.getScreenRights(requestHandalerKey, ctx, "patientDetails");
    if (!isScreenRights) {
      successMsg = "Permission Denied. Please check with Administrator.";
      patientDetailsDataMap.put("return_code", "1003");
      patientDetailsDataMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(patientDetailsDataMap));
      response.flushBuffer();
      return null;
    }

    if (isAValidRequest) {
      try {
        con =
            com.insta.instaapi.common.DbUtil.getConnection(
                (String) sessionParameters.get("hospital_name"));
        logger.info(
            "getting connection object"
                + con
                + "----"
                + (String) sessionParameters.get("hospital_name"));

        List<BasicDynaBean> patientdetails = dao.findPatientByMrno(con, mrNo);
        Map<String, Object> patientDetailsSuccessMap = new HashMap<String, Object>();
        if (patientdetails != null && patientdetails.size() > 0) {
          patientDetailsSuccessMap.putAll(
              (Map) (ConversionUtils.listBeanToListMap(patientdetails).get(0)));

          if (patientDetailsSuccessMap.get("dateofbirth") != null) {
            java.sql.Date date = (java.sql.Date) patientDetailsSuccessMap.get("dateofbirth");
            patientDetailsSuccessMap.remove("dateofbirth");
            patientDetailsSuccessMap.put("dateofbirth", DateUtil.formatIso8601Date(date));
          }
          if (patientDetailsSuccessMap.get("expected_dob") != null) {
            java.sql.Date date = (java.sql.Date) patientDetailsSuccessMap.get("expected_dob");
            patientDetailsSuccessMap.remove("expected_dob");
            patientDetailsSuccessMap.put("expected_dob", DateUtil.formatIso8601Date(date));
          }
          if (patientDetailsSuccessMap.get("category_expiry_date") != null) {
            java.sql.Date date =
                (java.sql.Date) patientDetailsSuccessMap.get("category_expiry_date");
            patientDetailsSuccessMap.remove("category_expiry_date");
            patientDetailsSuccessMap.put("category_expiry_date", DateUtil.formatIso8601Date(date));
          }
          if (patientDetailsSuccessMap.get("death_date") != null) {
            java.sql.Date date = (java.sql.Date) patientDetailsSuccessMap.get("death_date");
            patientDetailsSuccessMap.remove("death_date");
            patientDetailsSuccessMap.put("death_date", DateUtil.formatIso8601Date(date));
          }
          if (patientDetailsSuccessMap.get("death_time") != null) {
            java.sql.Time time = (java.sql.Time) patientDetailsSuccessMap.get("death_time");
            patientDetailsSuccessMap.remove("death_time");
            patientDetailsSuccessMap.put("death_time", DateUtil.formatIso8601Time(time));
          }
          if (patientDetailsSuccessMap.get("first_visit_reg_date") != null) {
            java.sql.Date date =
                (java.sql.Date) patientDetailsSuccessMap.get("first_visit_reg_date");
            patientDetailsSuccessMap.remove("first_visit_reg_date");
            patientDetailsSuccessMap.put("first_visit_reg_date", DateUtil.formatIso8601Date(date));
          }
          if (patientDetailsSuccessMap.get("passport_validity") != null) {
            java.sql.Date date = (java.sql.Date) patientDetailsSuccessMap.get("passport_validity");
            patientDetailsSuccessMap.remove("passport_validity");
            patientDetailsSuccessMap.put("passport_validity", DateUtil.formatIso8601Date(date));
          }
          if (patientDetailsSuccessMap.get("visa_validity") != null) {
            java.sql.Date date = (java.sql.Date) patientDetailsSuccessMap.get("visa_validity");
            patientDetailsSuccessMap.remove("visa_validity");
            patientDetailsSuccessMap.put("visa_validity", DateUtil.formatIso8601Date(date));
          }
          if (patientDetailsSuccessMap.get("mod_time") != null) {
            java.sql.Timestamp timestamp =
                (java.sql.Timestamp) patientDetailsSuccessMap.get("mod_time");
            patientDetailsSuccessMap.remove("mod_time");
            patientDetailsSuccessMap.put("mod_time", DateUtil.formatIso8601Timestamp(timestamp));
          }
          if (patientDetailsSuccessMap.get("custom_field14") != null) {
            java.sql.Date date = (java.sql.Date) patientDetailsSuccessMap.get("custom_field14");
            patientDetailsSuccessMap.remove("custom_field14");
            patientDetailsSuccessMap.put("custom_field14", DateUtil.formatIso8601Date(date));
          }
          if (patientDetailsSuccessMap.get("custom_field15") != null) {
            java.sql.Date date = (java.sql.Date) patientDetailsSuccessMap.get("custom_field15");
            patientDetailsSuccessMap.remove("custom_field15");
            patientDetailsSuccessMap.put("custom_field15", DateUtil.formatIso8601Date(date));
          }
          if (patientDetailsSuccessMap.get("custom_field16") != null) {
            java.sql.Date date = (java.sql.Date) patientDetailsSuccessMap.get("custom_field16");
            patientDetailsSuccessMap.remove("custom_field16");
            patientDetailsSuccessMap.put("custom_field16", DateUtil.formatIso8601Date(date));
          }
          patientDetailsDataMap.put("patient_details", patientDetailsSuccessMap);
        } else {
          successMsg = "invalid MRNO";
          logger.info("invalid MRNO");
          logger.info("sending the response back to the requesting server");
          patientDetailsDataMap.put("return_code", "1021");
          patientDetailsDataMap.put("return_message", successMsg);
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          response.getWriter().write(js.deepSerialize(patientDetailsDataMap));
          response.flushBuffer();
          return null;
        }

      } finally {
        com.insta.instaapi.common.DbUtil.closeConnections(con, null);
      }
      logger.info("getting all patient realted data....");
    }

    logger.info("sending the response back to the requesting server");
    patientDetailsDataMap.put("return_code", "2001");
    patientDetailsDataMap.put("return_message", "Success");
    response.getWriter().write(js.deepSerialize(patientDetailsDataMap));
    response.flushBuffer();

    return null;
  }

  /**
   * Find the patient.
   *
   * @param mapping mapping paramter
   * @param form form paramter
   * @param request request object
   * @param response response object
   * @return returns action forward
   * @throws IOException Signals that an I/O exception has occurred
   * @throws ServletException may throw Servlet Exception
   * @throws SQLException may throw SQL Exception.
   */
  public ActionForm findPatient(
      ActionMapping mapping,
      ActionForm form,
      HttpServletRequest request,
      HttpServletResponse response)
      throws IOException, ServletException, SQLException {

    String requestHandalerKey = ApiUtil.getRequestKey(request);
    String[] mrNos = request.getParameterValues("mr_no");
    List<String> mrNosSanitized = new ArrayList<String>();
    if (mrNos != null) {
      for (String s : mrNos) {
        if (s != null && s.trim().length() > 0) {
          mrNosSanitized.add(s);
        }
      }
    }
    boolean isAValidRequest = false;
    Connection con = null;
    JSONSerializer js = JsonProcessor.getJSONParser();
    ServletContext ctx = servlet.getServletContext();
    Map<String, Object> sessionMap = ServletContextUtil.getContextParametersMap(ctx);
    logger.info("getting session related data from conetxt" + sessionMap);
    Map sessionParameters = null;
    String successMsg = "";
    // PatientDetailsDAO dao = new PatientDetailsSearchDAO();
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
        if ((boolean) sessionParameters.get("patient_login")) {
          mrNosSanitized.clear();
          mrNosSanitized.add((String) sessionParameters.get("customer_user_id"));
        }
      }
    }
    response.setContentType("application/json");
    response.setHeader("Cache-Control", "no-cache");
    // response.setHeader("Access-Control-Allow-Origin", "*");
    Map<String, Object> patientDetailsDataMap = new HashMap<String, Object>();

    if (!isAValidRequest) {
      successMsg = "invalid request token,please login again";
      logger.info("invalid request token,please login again");
      logger.info("sending the response back to the requesting server");
      patientDetailsDataMap.put("return_code", "1001");
      patientDetailsDataMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(patientDetailsDataMap));
      response.flushBuffer();
      return null;
    }
    String firstName = request.getParameter("first_name");
    String lastName = request.getParameter("last_name");
    String gender = request.getParameter("gender");
    String phoneNo = request.getParameter("phone_no");
    String email = request.getParameter("email");
    String lastVisitedDate = request.getParameter("last_visited_date");
    String birthDate = request.getParameter("date_of_birth");
    String govtId = request.getParameter("govt_id");
    String oldmrno = request.getParameter("old_mrno");
    if ((mrNosSanitized.size() == 0)
        && (firstName == null || "".equals(firstName))
        && (lastName == null || "".equals(lastName))
        && (gender == null || "".equals(gender))
        && (phoneNo == null || "".equals(phoneNo))
        && (email == null || "".equals(email))
        && (lastVisitedDate == null || "".equals(lastVisitedDate))
        && (govtId == null || "".equals(govtId))
        && (birthDate == null || "".equals(birthDate))
        && (oldmrno == null || "".equals(oldmrno))) {
      successMsg = "Please specify at least one search criteria";
      logger.info("Please specify at least one search criteria");
      patientDetailsDataMap.put("return_code", "1021");
      patientDetailsDataMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(patientDetailsDataMap));
      response.flushBuffer();
      return null;
    }

    // Check the rights for Patient Search screen
    boolean isScreenRights =
        ScreenRights.getScreenRights(requestHandalerKey, ctx, "patientDetails");
    if (!isScreenRights) {
      successMsg = "Permission Denied. Please check with Administrator.";
      patientDetailsDataMap.put("return_code", "1003");
      patientDetailsDataMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(patientDetailsDataMap));
      response.flushBuffer();
      return null;
    }

    if (isAValidRequest) {
      try {
        con =
            com.insta.instaapi.common.DbUtil.getConnection(
                (String) sessionParameters.get("hospital_name"));
        logger.info(
            "getting connection object"
                + con
                + "----"
                + (String) sessionParameters.get("hospital_name"));

        Map<LISTING, Object> listingParams = new HashMap<LISTING, Object>();
        listingParams.put(LISTING.SORTCOL, "mr_no");
        listingParams.put(LISTING.SORTASC, false);
        listingParams.put(LISTING.PAGENUM, 0);
        listingParams.put(LISTING.PAGESIZE, 0);
        Map<String, Object> params = new HashMap<String, Object>();
        if (mrNosSanitized.size() > 0) {
          params.put("mr_no", mrNosSanitized.toArray(new String[mrNosSanitized.size()]));
          params.put("mr_no@type", new String[] {"String"});
          params.put("mr_no@op", new String[] {"in"});
        }
        if (firstName != null && !firstName.equals("")) {
          params.put("patient_name", new String[] {firstName});
          params.put("patient_name@type", new String[] {"String"});
          params.put("patient_name@op", new String[] {"ilike"});
        }
        if (lastName != null && !lastName.equals("")) {
          params.put("last_name", new String[] {lastName});
          params.put("last_name@type", new String[] {"String"});
          params.put("last_name@op", new String[] {"ilike"});
        }
        if (gender != null && !gender.equals("")) {
          params.put("patient_gender", new String[] {gender});
          params.put("patient_gender@type", new String[] {"String"});
          params.put("patient_gender@op", new String[] {"in"});
        }
        if (phoneNo != null && !phoneNo.equals("")) {
          if (phoneNo.charAt(0) == '+') {
            phoneNo = phoneNo.substring(1);
          }
          params.put("patient_phone", new String[] {phoneNo});
          params.put("patient_phone@type", new String[] {"String"});
          params.put("patient_phone@op", new String[] {"in"});
        }
        if (email != null && !email.equals("")) {
          params.put("email_id", new String[] {email});
          params.put("email_id@type", new String[] {"String"});
          params.put("email_id@op", new String[] {"in"});
        }
        if (lastVisitedDate != null && !lastVisitedDate.equals("")) {
          params.put("last_visited_date", new String[] {lastVisitedDate});
          params.put("last_visited_date@type", new String[] {"string"});
          params.put("last_visited_date@op", new String[] {"in"});
          params.put("last_visited_date@cast", new String[] {"Y"});
        }
        if (birthDate != null && !birthDate.equals("")) {
          params.put("dateofbirth", new String[] {birthDate});
          params.put("dateofbirth@type", new String[] {"string"});
          params.put("dateofbirth@op", new String[] {"in"});
          params.put("dateofbirth@cast", new String[] {"Y"});
        }
        if (govtId != null && !govtId.equals("")) {
          params.put("government_identifier", new String[] {govtId});
          params.put("government_identifier@type", new String[] {"String"});
          params.put("government_identifier@op", new String[] {"in"});
        }
        if (oldmrno != null && !oldmrno.equals("")) {
          params.put("oldmrno", new String[] { oldmrno });
          params.put("oldmrno@type", new String[] { "String" });
          params.put("oldmrno@op", new String[] { "in" });
        }
        List patientdetails =
            PatientsDetailsSearchDAO.findPatientDetail(con, params, listingParams);
        if (patientdetails != null && patientdetails.size() > 0) {
          patientDetailsDataMap.put("patient_details", patientdetails);
          logger.info("sending the response back to the requesting server");
          patientDetailsDataMap.put("return_code", "2001");
          patientDetailsDataMap.put("return_message", "Success");
          response.getWriter().write(js.deepSerialize(patientDetailsDataMap));
          response.flushBuffer();
          return null;
        } else {
          successMsg = "No patient found for the given search criteria";
          logger.info("No patient found for the given search criteria");
          logger.info("sending the response back to the requesting server");
          patientDetailsDataMap.put("return_code", "1022");
          patientDetailsDataMap.put("return_message", successMsg);
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          response.getWriter().write(js.deepSerialize(patientDetailsDataMap));
          response.flushBuffer();
          return null;
        }
      } catch (ParseException exception) {
        // TODO Auto-generated catch block
        exception.printStackTrace();
      } finally {
        com.insta.instaapi.common.DbUtil.closeConnections(con, null);
      }
      logger.info("getting all patient realted data....");
    }

    logger.info("sending the response back to the requesting server");
    patientDetailsDataMap.put("return_code", "2001");
    patientDetailsDataMap.put("return_message", "Success");
    response.getWriter().write(js.deepSerialize(patientDetailsDataMap));
    response.flushBuffer();

    return null;
  }

  /**
   * Get patient Visits.
   *
   * @param mapping mapping paramter
   * @param form form paramter
   * @param request request object
   * @param response response object
   * @return returns patient visits
   * @throws IOException Signals that an I/O exception has occurred
   * @throws ServletException may throw Servlet Exception
   * @throws SQLException may throw Sql Exception
   */
  public ActionForm getPatientVisits(
      ActionMapping mapping,
      ActionForm form,
      HttpServletRequest request,
      HttpServletResponse response)
      throws IOException, ServletException, SQLException {

    String requestHandalerKey = ApiUtil.getRequestKey(request);
    String mrNo = request.getParameter("mr_no");
    String fromDate = request.getParameter("from_date");
    String toDate = request.getParameter("to_date");
    String primarySponsorId = request.getParameter("primary_sponsor_id");
    String secondarySponsorId = request.getParameter("secondary_sponsor_id");
    Object fromDateRequestedTimeStamp = null;
    Object toDateRequestedTimeStamp = null;
    Timestamp currentTimeStamp = DateUtil.getCurrentTimestamp();
    String isoTimestamp = DateUtil.formatIso8601Timestamp(currentTimeStamp);
    boolean isFromDateTimeStampFormat = true;
    boolean isToDateTimeStampFormat = true;
    boolean isAValidRequest = false;
    Connection con = null;
    JSONSerializer js = JsonProcessor.getJSONParser();
    ServletContext ctx = servlet.getServletContext();
    Map<String, Object> sessionMap = ServletContextUtil.getContextParametersMap(ctx);
    logger.info("getting session related data from conetxt" + sessionMap);
    Map sessionParameters = null;
    String successMsg = "";
    // PatientDetailsDAO dao = new PatientDetailsSearchDAO();
    Timestamp loginTime = null;
    MessageResources msgResource = getResources(request);
    String tokenValidation = msgResource.getMessage("token.validation.duration");
    int validDuration = Integer.parseInt(tokenValidation);
    // getting current time
    Calendar calendar = Calendar.getInstance();
    java.util.Date now = calendar.getTime();
    Timestamp currentTime = new Timestamp(now.getTime());
    if (sessionMap != null && !sessionMap.isEmpty()) {
      sessionParameters = (Map) sessionMap.get(requestHandalerKey);
      if (sessionParameters != null && !sessionParameters.isEmpty()) {
        // getting login time
        loginTime = (Timestamp) sessionParameters.get("login_time");
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
    Map<String, Object> visitDetailsDataMap = new HashMap<String, Object>();

    if (!isAValidRequest) {
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

    // Check the rights for Patient Search screen
    boolean isScreenRights = ScreenRights.getScreenRights(requestHandalerKey, ctx, "visitDetails");
    if (!isScreenRights) {
      successMsg = "Permission Denied. Please check with Administrator.";
      visitDetailsDataMap.put("return_code", "1003");
      visitDetailsDataMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(visitDetailsDataMap));
      response.flushBuffer();
      return null;
    }

    try {
      if (toDate != null && toDate.length() > 0) {
        toDateRequestedTimeStamp = DateUtil.parseIso8601Timestamp(toDate);
        if (toDateRequestedTimeStamp == null) {
          isToDateTimeStampFormat = false;
          toDateRequestedTimeStamp = DateUtil.parseIso8601Date(toDate);
        }
      }

      if (fromDate != null && fromDate.length() > 0) {
        fromDateRequestedTimeStamp = DateUtil.parseIso8601Timestamp(fromDate);
        if (fromDateRequestedTimeStamp == null) {
          isFromDateTimeStampFormat = false;
          fromDateRequestedTimeStamp = DateUtil.parseIso8601Date(fromDate);
        }
      }

      if (fromDate == null || fromDate.length() <= 0) {
        if (toDateRequestedTimeStamp == null) {
          toDateRequestedTimeStamp = currentTimeStamp;
          fromDateRequestedTimeStamp = DateUtil.addSubtractMonths(currentTimeStamp, -6);
        } else {
          if (isToDateTimeStampFormat) {
            fromDateRequestedTimeStamp =
                DateUtil.addSubtractMonths((Timestamp) toDateRequestedTimeStamp, -6);
          } else {
            fromDateRequestedTimeStamp =
                DateUtil.addSubtractMonths((Date) toDateRequestedTimeStamp, -6);
          }
        }
      }

      if (toDateRequestedTimeStamp == null) {
        if (isFromDateTimeStampFormat) {
          toDateRequestedTimeStamp =
              DateUtil.addSubtractMonths((Timestamp) fromDateRequestedTimeStamp, 6);
        } else {
          toDateRequestedTimeStamp =
              DateUtil.addSubtractMonths((Date) fromDateRequestedTimeStamp, 6);
        }
      }

    } catch (ParseException exception) {
      successMsg = "Invalid input parameters supplied";
      visitDetailsDataMap.put("return_code", "1021");
      visitDetailsDataMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(visitDetailsDataMap));
      response.flushBuffer();
      return null;
    }
    if (isFromDateTimeStampFormat) {
      visitDetailsDataMap.put(
          "START_DATE", DateUtil.formatIso8601Timestamp((Timestamp) fromDateRequestedTimeStamp));
    } else {
      visitDetailsDataMap.put(
          "START_DATE", DateUtil.formatIso8601Date((Date) fromDateRequestedTimeStamp));
    }

    if (isToDateTimeStampFormat) {
      visitDetailsDataMap.put(
          "END_DATE", DateUtil.formatIso8601Timestamp((Timestamp) toDateRequestedTimeStamp));
    } else {
      visitDetailsDataMap.put(
          "END_DATE", DateUtil.formatIso8601Date((Date) toDateRequestedTimeStamp));
    }
    if (fromDate != null && toDate != null) {
      boolean isDatesValid = true;

      Timestamp actualToDate;
      if (isToDateTimeStampFormat) {
        actualToDate = (Timestamp) toDateRequestedTimeStamp;
      } else {
        long dto = ((Date) toDateRequestedTimeStamp).getTime();
        actualToDate = new Timestamp(dto);
      }

      Timestamp expectedtoDate;
      if (isFromDateTimeStampFormat) {
        expectedtoDate = DateUtil.addSubtractMonths((Timestamp) fromDateRequestedTimeStamp, 6);
        isDatesValid = actualToDate.compareTo((Timestamp) fromDateRequestedTimeStamp) > 0;
      } else {
        expectedtoDate = DateUtil.addSubtractMonths((Date) fromDateRequestedTimeStamp, 6);
      }

      if ((mrNo == null || "".equals(mrNo)) && actualToDate.compareTo(expectedtoDate) > 0) {
        successMsg = "Duration can not be more than 6 months";
        visitDetailsDataMap.put("return_code", "1023");
        visitDetailsDataMap.put("return_message", successMsg);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write(js.deepSerialize(visitDetailsDataMap));
        response.flushBuffer();
        return null;
      }

      if (!isDatesValid) {
        successMsg = "To date can not be earlier than From date";
        visitDetailsDataMap.put("return_code", "1022");
        visitDetailsDataMap.put("return_message", successMsg);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write(js.deepSerialize(visitDetailsDataMap));
        response.flushBuffer();
        return null;
      }
    }
    String useDischargeDateForIpStr = request.getParameter("use_discharge_date_for_ip");
    if (useDischargeDateForIpStr == null) {
      useDischargeDateForIpStr = "n";
    }
    if (!useDischargeDateForIpStr.equalsIgnoreCase("y")
        && !useDischargeDateForIpStr.equalsIgnoreCase("n")) {
      successMsg = "Invalid value for Use Discharge Date For IP";
      visitDetailsDataMap.put("return_code", "1026");
      visitDetailsDataMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(visitDetailsDataMap));
      response.flushBuffer();
      return null;
    }
    if (isAValidRequest) {
      try {
        logger.info("getting all visits related data....");
        RequestContext.setConnectionDetails(
            new String[] {"", "", (String) sessionParameters.get("hospital_name"), "", "0"});
        logger.info(
            "getting connection object"
                + con
                + "----"
                + (String) sessionParameters.get("hospital_name"));
        List visitDetails = null;
        visitDetails =
            new VisitDetailsDAO()
                .getPatientVisitsDetails(
                    fromDateRequestedTimeStamp,
                    toDateRequestedTimeStamp,
                    mrNo,
                    primarySponsorId,
                    secondarySponsorId,
                    useDischargeDateForIpStr.equalsIgnoreCase("y"));
        if (visitDetails != null && visitDetails.size() > 0) {
          visitDetailsDataMap.put("patient_visits_details", visitDetails);
          logger.info("sending the response back to the requesting server");
          visitDetailsDataMap.put("return_code", "2001");
          visitDetailsDataMap.put("return_message", "Success");
          response.getWriter().write(js.deepSerialize(visitDetailsDataMap));
          response.flushBuffer();
          return null;
        } else {
          successMsg = "No visits found for the given search criteria";
          logger.info("No visits found for the given search criteria");
          logger.info("sending the response back to the requesting server");
          visitDetailsDataMap.put("return_code", "1024");
          visitDetailsDataMap.put("return_message", successMsg);
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          response.getWriter().write(js.deepSerialize(visitDetailsDataMap));
          response.flushBuffer();
          return null;
        }
      } catch (Exception exception) {
        // TODO Auto-generated catch block
        successMsg = "Failed to retrieve the visits";
        logger.info("Failed to retrieve the visits");
        logger.info("sending the response back to the requesting server");
        visitDetailsDataMap.put("return_code", "1025");
        visitDetailsDataMap.put("return_message", successMsg);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.getWriter().write(js.deepSerialize(visitDetailsDataMap));
        response.flushBuffer();
        return null;
      } finally {
        com.insta.instaapi.common.DbUtil.closeConnections(con, null);
      }
    }

    logger.info("sending the response back to the requesting server");
    visitDetailsDataMap.put("return_code", "2001");
    visitDetailsDataMap.put("return_message", "Success");
    response.getWriter().write(js.deepSerialize(visitDetailsDataMap));
    response.flushBuffer();

    return null;
  }

  /**
   * Get Incoming patient visits.
   *
   * @param mapping mapping parameter
   * @param form form paramter
   * @param request request object
   * @param response response object
   * @return returns action forward
   * @throws IOException Signals that an I/O exception has occurred
   * @throws ServletException may throw Servlet Exception
   * @throws SQLException may throw SQL Exception
   */
  public ActionForm getIncomingPatientVisits(
      ActionMapping mapping,
      ActionForm form,
      HttpServletRequest request,
      HttpServletResponse response)
      throws IOException, ServletException, SQLException {

    String requestHandalerKey = ApiUtil.getRequestKey(request);
    String toDate = request.getParameter("to_date");
    String incomingHospId = request.getParameter("incoming_hospital_id");
    Object fromDateRequestedTimeStamp = null;
    Object toDateRequestedTimeStamp = null;
    java.sql.Timestamp currentTimeStamp = DateUtil.getCurrentTimestamp();
    String isoTimestamp = DateUtil.formatIso8601Timestamp(currentTimeStamp);
    boolean isFromDateTimeStampFormat = true;
    boolean isToDateTimeStampFormat = true;
    boolean isAValidRequest = false;
    Connection con = null;
    JSONSerializer js = JsonProcessor.getJSONParser();
    ServletContext ctx = servlet.getServletContext();
    Map<String, Object> sessionMap = ServletContextUtil.getContextParametersMap(ctx);
    logger.info("getting session related data from conetxt" + sessionMap);
    Map sessionParameters = null;
    String successMsg = "";
    // PatientDetailsDAO dao = new PatientDetailsSearchDAO();
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
    response.setContentType("application/json");
    response.setHeader("Cache-Control", "no-cache");
    // response.setHeader("Access-Control-Allow-Origin", "*");
    Map<String, Object> visitDetailsDataMap = new HashMap<String, Object>();

    if (!isAValidRequest) {
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

    // Check the rights for Patient Search screen
    boolean isScreenRights = ScreenRights.getScreenRights(requestHandalerKey, ctx, "visitDetails");
    if (!isScreenRights) {
      successMsg = "Permission Denied. Please check with Administrator.";
      visitDetailsDataMap.put("return_code", "1003");
      visitDetailsDataMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(visitDetailsDataMap));
      response.flushBuffer();
      return null;
    }
    String fromDate = request.getParameter("from_date");
    if (fromDate == null || fromDate.isEmpty()) {
      successMsg = "Mandatory fields are not supplied";
      visitDetailsDataMap.put("return_code", "1002");
      visitDetailsDataMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(visitDetailsDataMap));
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
        visitDetailsDataMap.put("return_code", "1021");
        visitDetailsDataMap.put("return_message", successMsg);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write(js.deepSerialize(visitDetailsDataMap));
        response.flushBuffer();
        return null;
      }
      if (toDate != null && toDate.length() > 0) {
        toDateRequestedTimeStamp = DateUtil.parseIso8601Timestamp(toDate);
        if (toDateRequestedTimeStamp == null) {
          isToDateTimeStampFormat = false;
          toDateRequestedTimeStamp = DateUtil.parseIso8601Date(toDate);
        }
      }
    } catch (ParseException exception) {
      successMsg = "Invalid input parameters supplied";
      visitDetailsDataMap.put("return_code", "1021");
      visitDetailsDataMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(visitDetailsDataMap));
      response.flushBuffer();
      return null;
    }
    if (isFromDateTimeStampFormat) {
      visitDetailsDataMap.put(
          "START_DATE",
          DateUtil.formatIso8601Timestamp((java.sql.Timestamp) fromDateRequestedTimeStamp));
    } else {
      visitDetailsDataMap.put(
          "START_DATE", DateUtil.formatIso8601Date((java.sql.Date) fromDateRequestedTimeStamp));
    }
    if (toDateRequestedTimeStamp != null) {
      if (isToDateTimeStampFormat) {
        visitDetailsDataMap.put(
            "END_DATE",
            DateUtil.formatIso8601Timestamp((java.sql.Timestamp) toDateRequestedTimeStamp));
      } else {
        visitDetailsDataMap.put(
            "END_DATE", DateUtil.formatIso8601Date((java.sql.Date) toDateRequestedTimeStamp));
      }
    } else {
      visitDetailsDataMap.put("END_DATE", isoTimestamp);
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
      visitDetailsDataMap.put("return_code", "1022");
      visitDetailsDataMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(visitDetailsDataMap));
      response.flushBuffer();
      return null;
    }
    if (days > 29) {
      successMsg = "Duration can not be more than 30 days";
      visitDetailsDataMap.put("return_code", "1023");
      visitDetailsDataMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(visitDetailsDataMap));
      response.flushBuffer();
      return null;
    }
    if (isAValidRequest) {
      try {
        logger.info("getting all incoming visits related data....");
        RequestContext.setConnectionDetails(
            new String[] {"", "", (String) sessionParameters.get("hospital_name"), "", "0"});
        logger.info(
            "getting connection object"
                + con
                + "----"
                + (String) sessionParameters.get("hospital_name"));
        List visitDetails = null;
        if (toDateRequestedTimeStamp != null) {
          visitDetails =
              IncomingPatientDAO.getIncomingPatientVisits(
                  fromDateRequestedTimeStamp, toDateRequestedTimeStamp, incomingHospId);
        } else {
          visitDetails =
              IncomingPatientDAO.getIncomingPatientVisits(
                  fromDateRequestedTimeStamp, currentTimeStamp, incomingHospId);
        }
        if (visitDetails != null && visitDetails.size() > 0) {
          visitDetailsDataMap.put("incoming_patient_visits_details", visitDetails);
          logger.info("sending the response back to the requesting server");
          visitDetailsDataMap.put("return_code", "2001");
          visitDetailsDataMap.put("return_message", "Success");
          response.getWriter().write(js.deepSerialize(visitDetailsDataMap));
          response.flushBuffer();
          return null;
        } else {
          successMsg = "No visits found for the given search criteria";
          logger.info("No visits found for the given search criteria");
          logger.info("sending the response back to the requesting server");
          visitDetailsDataMap.put("return_code", "1024");
          visitDetailsDataMap.put("return_message", successMsg);
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          response.getWriter().write(js.deepSerialize(visitDetailsDataMap));
          response.flushBuffer();
          return null;
        }
      } catch (Exception exception) {
        // TODO Auto-generated catch block
        successMsg = "Failed to retrieve the visits";
        logger.info("Failed to retrieve the visits");
        logger.info("sending the response back to the requesting server");
        visitDetailsDataMap.put("return_code", "1025");
        visitDetailsDataMap.put("return_message", successMsg);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.getWriter().write(js.deepSerialize(visitDetailsDataMap));
        response.flushBuffer();
        return null;
      } finally {
        com.insta.instaapi.common.DbUtil.closeConnections(con, null);
      }
    }

    logger.info("sending the response back to the requesting server");
    visitDetailsDataMap.put("return_code", "2001");
    visitDetailsDataMap.put("return_message", "Success");
    response.getWriter().write(js.deepSerialize(visitDetailsDataMap));
    response.flushBuffer();

    return null;
  }

  private static final String GET_CATEGORY_DETAILS =
      "SELECT code,seperate_num_seq FROM patient_category_master " + "WHERE category_id=?";

  /**
   * method to register patient.
   *
   * @param con connection object
   * @param bean patient bean
   * @return returns 0 if patient registration is success
   * @throws SQLException may throw Sql Exception.
   * @throws IOException Signals that an I/O exception has occurred
   * @throws NoSuchAlgorithmException may throw NoSuchAlgorithmException
   * @throws ParseException may throw Parsing exception
   */
  public int registerPatient(Connection con, BasicDynaBean bean)
      throws SQLException, IOException, NoSuchAlgorithmException, ParseException {
    if (bean.get("middle_name") == null) {
      bean.set("middle_name", "");
    }
    if (bean.get("last_name") == null) {
      bean.set("last_name", "");
    }
    bean.set("remarks", DateUtil.formatIso8601Date((java.sql.Date) bean.get("dateofbirth")));
    boolean patientExists = GeneralRegistrationDAO.checkDetailsExist(con, bean);
    if (patientExists) {
      return 2;
    }
    PreparedStatement ps = null;
    ResultSet rs = null;
    String newMrno = null;

    int patientCategoryId = (Integer) bean.get("patient_category_id");
    int defalutCatId = 1;

    if (patientCategoryId != defalutCatId) {
      String categoryCode = null;
      String isSeperateSeqRequire = null;
      ps = con.prepareStatement(GET_CATEGORY_DETAILS);
      ps.setInt(1, patientCategoryId);
      rs = ps.executeQuery();
      if (rs.next()) {
        categoryCode = rs.getString("code");
        isSeperateSeqRequire = rs.getString("seperate_num_seq");
      }
      if ("Y".equals(isSeperateSeqRequire) && !"".equals(categoryCode)) {
        newMrno = com.insta.instaapi.common.DbUtil.getNextPatternId(con, categoryCode);

      } else {
        newMrno = com.insta.instaapi.common.DbUtil.getNextPatternId(con, "MRNO");
      }
    } else {
      newMrno = com.insta.instaapi.common.DbUtil.getNextPatternId(con, "MRNO");
    }
    bean.set("mr_no", newMrno);
    PatientDetailsDAO patientDAO = new PatientDetailsDAO();
    boolean success = patientDAO.insert(con, bean);
    if (success) {
      if (success) {
        String cityid = (String) bean.get("patient_city");
        String area = (String) bean.get("patient_area");
        if (area != null && !area.equals("")) {
          success = checkAndInsertArea(con, cityid, area);
        }
      }
    }
    com.insta.instaapi.common.DbUtil.closeConnections(null, ps, rs);
    if (success) {
      return 0;
    }
    return 1;
  }

  /**
   * method to insert area details.
   *
   * @param con connection object
   * @param cityid city id
   * @param areaName area name
   * @return returns true if success
   * @throws SQLException may throw SQL exception
   * @throws IOException Signals that an I/O exception has occurred
   */
  private boolean checkAndInsertArea(Connection con, String cityid, String areaName)
      throws SQLException, IOException {
    boolean success = true;

    AreaMasterDAO areadao = new AreaMasterDAO();
    BasicDynaBean areaByCity = getAreaByCity(con, cityid, areaName);
    if (areaByCity == null) {
      BasicDynaBean bean = areadao.getBean(con);
      bean.set("area_id", getNextFormattedId(con));
      bean.set("area_name", areaName);
      bean.set("city_id", cityid);

      success = areadao.insert(con, bean);
    }
    return success;
  }

  private static String GET_AREA_FOR_CITY =
      "SELECT * FROM area_master WHERE city_id = ? AND area_name = ?";

  /**
   * Get Area by city id.
   *
   * @param con connection object
   * @param cityid city id.
   * @param area area name
   * @return returns BasicDynaBean of area
   * @throws SQLException may throw SQL Exception
   */
  public BasicDynaBean getAreaByCity(Connection con, String cityid, String area)
      throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(GET_AREA_FOR_CITY);
      ps.setObject(1, cityid);
      ps.setObject(2, area);
      List list = DataBaseUtil.queryToDynaList(ps);
      if (list.size() > 0) {
        return (BasicDynaBean) list.get(0);
      } else {
        return null;
      }
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  /**
   * Get next Formatted id.
   *
   * @param con connection Object
   * @return returns next auto genetated id
   * @throws SQLException may throw Sql Exception
   */
  public String getNextFormattedId(Connection con) throws SQLException {
    return com.insta.instaapi.common.DbUtil.getSequenceId(
        con, "area_master" + "_seq", "area_master");
  }
}
