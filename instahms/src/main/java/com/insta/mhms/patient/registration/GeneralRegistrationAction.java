package com.insta.mhms.patient.registration;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.RandomGeneration;
import com.insta.hms.master.AreaMaster.AreaMasterDAO;
import com.insta.hms.master.StateMaster.StateMasterDAO;
import com.insta.instaapi.common.JsonProcessor;
import com.insta.instaapi.patient.registration.GeneralRegistrationDAO;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/** @author
 * mithun.saha
 */
public class GeneralRegistrationAction extends DispatchAction {
  static Logger logger = LoggerFactory.getLogger(GeneralRegistrationAction.class);

  /**
   * Get master Data.
   *
   * @param mapping mapping parameter
   * @param form form parameter
   * @param request request object
   * @param response response object
   * @return returns action forward
   * @throws IOException Signals that an I/O exception has occurred
   * @throws ServletException may throw Servlet Exception
   * @throws SQLException may throw Sql Exception
   * @throws NoSuchAlgorithmException may throw NoSuchAlgorithmException
   * @throws ParseException may throw parse exception
   */
  public ActionForward getRegMasterData(
      ActionMapping mapping,
      ActionForm form,
      HttpServletRequest request,
      HttpServletResponse response)
      throws IOException, ServletException, SQLException, NoSuchAlgorithmException, ParseException {
    logger.info("getting registration related master data");
    Map<String, Object> schedulerMasterDataMap = new HashMap<String, Object>();
    response.setContentType("application/json");
    response.setHeader("Cache-Control", "no-cache");
    // response.setHeader("Access-Control-Allow-Origin", "*");
    HttpSession session = request.getSession(false);
    String sesHospitalId = (String) session.getAttribute("sesHospitalId");
    String successMsg = "";
    String returnCode = "";
    if (sesHospitalId == null || "".equals(sesHospitalId)) {
      successMsg = "Session is expired, please login again";
      logger.info("Session is expired, please login again");
      schedulerMasterDataMap.put("return_code", "1001");
      schedulerMasterDataMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    } else {
      Connection con = null;
      try {
        con = com.insta.instaapi.common.DbUtil.getConnection(sesHospitalId);
        Map<String, Object> filterMap = new HashMap<String, Object>();
        filterMap.put("status", "A");
        List<String> salutationColumns = new ArrayList<String>();
        salutationColumns.add("salutation_id");
        salutationColumns.add("salutation");
        salutationColumns.add("gender");

        /*List<String> patientCategoriesColums=new ArrayList<String>();
        patientCategoriesColums.add("category_id");
        patientCategoriesColums.add("category_name");
        patientCategoriesColums.add("center_id");   */

        List<BasicDynaBean> allCities = GeneralRegistrationDAO.getCityDetails(con);
        List<BasicDynaBean> allStates = GeneralRegistrationDAO.getStateDetails(con);
        List<BasicDynaBean> allSalutaions =
            new GenericDAO("salutation_master")
                .listAll(con, salutationColumns, filterMap, "salutation");
        // List<BasicDynaBean> allPatientCategories = new
        // GenericDAO("patient_category_master").listAll(con,patientCategoriesColums, filterMap,
        // "category_name");
        List<BasicDynaBean> allPatientCategories =
            GeneralRegistrationDAO.getPatientCategoryDetails(con);
        schedulerMasterDataMap.put("allCities", ConversionUtils.listBeanToListMap(allCities));
        schedulerMasterDataMap.put("allStates", ConversionUtils.listBeanToListMap(allStates));
        schedulerMasterDataMap.put(
            "allPatientCategories", ConversionUtils.listBeanToListMap(allPatientCategories));
        schedulerMasterDataMap.put(
            "allSalutations", ConversionUtils.listBeanToListMap(allSalutaions));
        successMsg = "Success";
        schedulerMasterDataMap.put("return_message", successMsg);
        schedulerMasterDataMap.put("return_code", "2001");
      } finally {
        com.insta.instaapi.common.DbUtil.closeConnections(con, null);
      }
    }
    JSONSerializer js = JsonProcessor.getJSONParser();
    response.getWriter().write(js.deepSerialize(schedulerMasterDataMap));
    response.flushBuffer();
    return null;
  }

  /**
   * Do Pre registartion method.
   *
   * @param mapping mapping parameter
   * @param form form parameter
   * @param request request object
   * @param response response object
   * @return returns action forward
   * @throws IOException Signals that an I/O exception has occurred
   * @throws ServletException may throw Servlet Exception
   * @throws SQLException may throw Sql Exception
   * @throws NoSuchAlgorithmException may throw NoSuchAlgorithmException
   * @throws ParseException may throw parse exception
   */
  public ActionForward doPreRegistration(
      ActionMapping mapping,
      ActionForm form,
      HttpServletRequest request,
      HttpServletResponse response)
      throws IOException, ServletException, SQLException, NoSuchAlgorithmException, ParseException {
    logger.info("getting pre registration related parameters");
    JSONSerializer js = JsonProcessor.getJSONParser();
    BasicDynaBean patientBean = null;
    Map<String, Object> patientPreRegistrationSuccessMap = new HashMap<String, Object>();
    boolean success = false;
    String successMsg = "success";
    response.setContentType("application/json");
    response.setHeader("Cache-Control", "no-cache");
    // response.setHeader("Access-Control-Allow-Origin", "*");
    String returnCode = "";
    HttpSession session = request.getSession(false);
    String sesHospitalId = (String) session.getAttribute("sesHospitalId");
    if (sesHospitalId == null || "".equals(sesHospitalId)) {
      successMsg = "Session is expired, please login again";
      logger.info("Session is expired, please login again");
      patientPreRegistrationSuccessMap.put("return_code", "1001");
      patientPreRegistrationSuccessMap.put("return_message", successMsg);
      logger.info("sending the response back to the requesting server");
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.getWriter().write(js.deepSerialize(patientPreRegistrationSuccessMap));
      response.flushBuffer();
      return null;
    }
    PatientDetailsDAO dao = new PatientDetailsDAO();
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
    if (sesHospitalId != null && !sesHospitalId.isEmpty()) {
      try {
        con = com.insta.instaapi.common.DbUtil.getConnection(sesHospitalId);
        logger.info("getting connection from database" + con);
        //con = com.insta.instaapi.common.DataBaseUtil.getConnection((String)session.
        //getAttribute("hospital_name"));
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
        patientBean.set("mobile_password", RandomGeneration.randomGeneratedPassword(6));
        patientBean.set("mobile_access", true);
        int result = registerPatient(con, patientBean);
        if (result == 0) {
          success = true;
        } else if (result == 1) {
          success = false;
        } else if (result == 2) {
          successMsg = "Patient is already registered.";
          returnCode = "1028";
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          // patientPreRegistrationSuccessMap.put("success", false);
          patientPreRegistrationSuccessMap.put("return_message", successMsg);
          patientPreRegistrationSuccessMap.put("return_code", returnCode);
          logger.info("sending the response back to the requesting server");
          response.getWriter().write(js.deepSerialize(patientPreRegistrationSuccessMap));
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
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      // patientPreRegistrationSuccessMap.put("success", false);
    } else {
      returnCode = "2001";
      successMsg = "Success";
      // patientPreRegistrationSuccessMap.put("success", true);
      patientPreRegistrationSuccessMap.put("mr_no", (String) patientBean.get("mr_no"));
      response.setStatus(HttpServletResponse.SC_OK);
    }
    patientPreRegistrationSuccessMap.put("return_message", successMsg);
    patientPreRegistrationSuccessMap.put("return_code", returnCode);
    logger.info("sending the response back to the requesting server");
    response.getWriter().write(js.deepSerialize(patientPreRegistrationSuccessMap));
    response.flushBuffer();
    return null;
  }

  /**
   * Get patient details.
   *
   * @param mapping mapping parameter
   * @param form form parameter
   * @param request request object
   * @param response response object
   * @return returns action forward
   * @throws ServletException may throw Servlet Exception
   * @throws Exception Generic Exception
   */
  public ActionForward getPatientDetails(
      ActionMapping mapping,
      ActionForm form,
      HttpServletRequest request,
      HttpServletResponse response)
      throws ServletException, Exception {

    JSONSerializer js = JsonProcessor.getJSONParser();
    boolean success = true;
    String msg = "";
    String successMsg = "success";
    response.setContentType("application/json");
    // response.setHeader("Access-Control-Allow-Origin", "*");
    HttpSession session = request.getSession(false);
    String mrNo = (String) session.getAttribute("mobile_user_id");
    Map<String, Object> responseMap = new HashMap<String, Object>();
    if (mrNo == null || mrNo.equals("")) {
      successMsg = "Session is expired, please login again";
      logger.info("Session is expired, please login again");
      logger.info("sending the response back to the requesting server");
      responseMap.put("return_code", "1001");
      responseMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.getWriter().write(js.deepSerialize(responseMap));
      response.flushBuffer();
      return null;
    }
    Connection con = DataBaseUtil.getConnection();
    PatientDetailsDAO dao = new PatientDetailsDAO();
    try {
      logger.info("getting all patient realted data....");
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
          java.sql.Date date = (java.sql.Date) patientDetailsSuccessMap.get("category_expiry_date");
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
          java.sql.Date date = (java.sql.Date) patientDetailsSuccessMap.get("first_visit_reg_date");
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
          java.sql.Timestamp time = (java.sql.Timestamp) patientDetailsSuccessMap.get("mod_time");
          patientDetailsSuccessMap.remove("mod_time");
          patientDetailsSuccessMap.put("mod_time", DateUtil.formatIso8601Timestamp(time));
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

        responseMap.put("patient_details", patientDetailsSuccessMap);
        successMsg = "Success";
        logger.info("Success");
        logger.info("sending the response back to the requesting server");
        responseMap.put("return_code", "2001");
        responseMap.put("return_message", successMsg);
        response.getWriter().write(js.deepSerialize(responseMap));
        response.flushBuffer();
        return null;
      } else {
        successMsg = "Failed to get patient details";
        logger.info("Failed to get patient details");
        logger.info("sending the response back to the requesting server");
        responseMap.put("return_code", "1021");
        responseMap.put("return_message", successMsg);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.getWriter().write(js.deepSerialize(responseMap));
        response.flushBuffer();
        return null;
      }
    } finally {
      com.insta.instaapi.common.DbUtil.closeConnections(con, null);
    }
  }

  /**
   * method to validate string.
   *
   * @param str String parameter
   * @return returns true or false
   */
  private boolean validateString(String str) {
    Pattern pattern = Pattern.compile("[^a-z ]", Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(str);
    boolean result = matcher.find();
    return result;
  }

  /**
   * method to validate numbers.
   *
   * @param str String parameter
   * @return returns true or false
   */
  private boolean validateNumbers(String str) {
    Pattern pattern = Pattern.compile("[^0-9]", Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(str);
    boolean result = matcher.find();
    return result;
  }

  private static final String GET_CATEGORY_DETAILS =
      "SELECT code,seperate_num_seq FROM patient_category_master " + "WHERE category_id=?";

  /**
   * register patient.
   * @param con connection object
   * @param bean BasicDynaBean
   * @return returns 0 if success
   * @throws SQLException may throw Sql Exception
   * @throws IOException Signals that an I/O exception has occurred
   * @throws NoSuchAlgorithmException may throw NoSuchAlgorithmException
   * @throws ParseException may throw parsing exception
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
    } else {
      return 1;
    }
  }

  /**
   * validates Area and insert.
   *
   * @param con connection object
   * @param cityid city id
   * @param areaName area name
   * @return returns boolean value
   * @throws SQLException may throw Sql Exception
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
   * Find Area by city id.
   *
   * @param con connection object
   * @param cityid city id
   * @param area area name
   * @return returns BasicDynaBean
   * @throws SQLException may throw Sql Exception
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
   * Get next formatted id.
   * @param con connection object
   * @return returns String
   * @throws SQLException may throw Sql Exception
   */
  public String getNextFormattedId(Connection con) throws SQLException {
    return com.insta.instaapi.common.DbUtil.getSequenceId(
        con, "area_master" + "_seq", "area_master");
  }
}
