package com.insta.instaapi.customer.scheduler;

import static com.insta.hms.jobs.common.QuartzJobHelper.buildJob;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.batchjob.pushevent.EventListenerJob;
import com.insta.hms.batchjob.pushevent.Events;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.JobSchedulingService;
import com.insta.hms.jobs.JobService;
import com.insta.hms.master.AppointmentSource.AppointmentSourceDAO;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.resourcescheduler.CategoryMasterDAO;
import com.insta.hms.resourcescheduler.ContactsDAO;
import com.insta.hms.resourcescheduler.PractoBookHelper;
import com.insta.hms.resourcescheduler.ResourceBO;
import com.insta.hms.resourcescheduler.ResourceBO.AppointMentResource;
import com.insta.hms.resourcescheduler.ResourceBO.Appointments;
import com.insta.hms.resourcescheduler.ResourceDAO;
import com.insta.hms.resourcescheduler.ResourceDTO;
import com.insta.instaapi.common.ApiUtil;
import com.insta.instaapi.common.DbUtil;
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
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author mithun.saha
 *
 */
public class DoctorSchedulerAction extends DispatchAction {
  static Logger logger = LoggerFactory.getLogger(DoctorSchedulerAction.class);
  private static final CenterMasterDAO centerDao = new CenterMasterDAO();
  static GenericDAO modulesActivated = new GenericDAO("modules_activated");
  private static ContactsDAO contactsDao = new ContactsDAO();
  JobService jobService = JobSchedulingService.getJobService();

  /**
   * Retrieve Master data required for scheduler.
   * 
   * @param mapping  Action Mapping
   * @param form     Action Form
   * @param request  Servlet Request Object
   * @param response Servlet Response Object
   * @return Response
   * @throws IOException  IO Exception
   * @throws SQLException SQL Exception
   */
  @SuppressWarnings("deprecation")
  public ActionForward getSchedulerMasterData(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {

    logger.info("getting scheduler realted parameters getSchedulerMasterData");
    String requestHandalerKey = ApiUtil.getRequestKey(request);
    String status = request.getParameter("status");
    boolean isAValidRequest = false;
    JSONSerializer js = JsonProcessor.getJSONParser();
    ServletContext ctx = servlet.getServletContext();
    Map<String, Object> sessionMap = ServletContextUtil.getContextParametersMap(ctx);
    logger.info("getting session related data from conetxt" + sessionMap);
    Map sessionParameters = null;
    String successMsg = "";
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
        isAValidRequest = (currentTime.getTime() - loginTime.getTime()) / 60000 <= validDuration;
      }
    }
    response.setContentType("application/json");
    response.setHeader("Cache-Control", "no-cache");
    // response.setHeader("Access-Control-Allow-Origin", "*");
    Map<String, Object> schedulerMasterDataMap = new HashMap<String, Object>();

    if (!isAValidRequest) {
      successMsg = "invalid request token,please login again";
      logger.info("invalid request token,please login again");
      logger.info("sending the response back to the requesting server");
      schedulerMasterDataMap.put("return_code", "1001");
      schedulerMasterDataMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(schedulerMasterDataMap));
      response.flushBuffer();
      return null;
    }

    // Check the rights for Doctor Scheduler screen
    boolean isScreenRights = ScreenRights.getScreenRights(requestHandalerKey, ctx,
        "schedulerMasterData");
    if (!isScreenRights) {
      successMsg = "Permission Denied. Please check with Administrator.";
      schedulerMasterDataMap.put("return_code", "1003");
      schedulerMasterDataMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(schedulerMasterDataMap));
      response.flushBuffer();
      return null;
    }

    if (isAValidRequest) {
      if (status != null && status.trim().length() > 0 && !status.equalsIgnoreCase("all")) {
        successMsg = "Invalid status parameter";
        logger.info("Invalid status parameter");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        schedulerMasterDataMap.put("return_code", "1021");
        schedulerMasterDataMap.put("return_message", successMsg);
        response.getWriter().write(js.deepSerialize(schedulerMasterDataMap));
        response.flushBuffer();
        return null;
      }
      try (Connection con = DbUtil.getConnection((String) sessionParameters.get("hospital_name"))) {
        logger.info("getting connection object" + con + "----"
            + sessionParameters.get("hospital_name"));
        List<String> hospitalCenterslist = new ArrayList<String>();
        hospitalCenterslist.add("center_id");
        hospitalCenterslist.add("center_name");
        hospitalCenterslist.add("center_code");
        hospitalCenterslist.add("city_id");
        hospitalCenterslist.add("state_id");
        hospitalCenterslist.add("country_id");
        hospitalCenterslist.add("center_address");
        hospitalCenterslist.add("accounting_company_name");
        hospitalCenterslist.add("hospital_center_service_reg_no");
        hospitalCenterslist.add("center_contact_phone");
        hospitalCenterslist.add("region_id");
        hospitalCenterslist.add("health_authority");
        hospitalCenterslist.add("created_timestamp");
        hospitalCenterslist.add("updated_timestamp");
        hospitalCenterslist.add("status");
        hospitalCenterslist.add("center_timezone");

        List<String> departmentslist = new ArrayList<String>();
        departmentslist.add("dept_id");
        departmentslist.add("dept_name");
        departmentslist.add("cost_center_code");
        departmentslist.add("dept_type_id");
        departmentslist.add("status");

        List<BasicDynaBean> hospitalCenters = null;
        if (status == null || status.trim().length() == 0) {
          hospitalCenters = new GenericDAO("hospital_center_master").listAll(con,
              hospitalCenterslist, "status", "A", "center_name");
        } else {
          hospitalCenters = new GenericDAO("hospital_center_master").listAll(con,
              hospitalCenterslist, null, null, "center_name");
        }
        List hospitalList = new ArrayList();
        if (hospitalCenters != null) {
          for (int i = 0; i < hospitalCenters.size(); i++) {
            Map hospitalMap = new HashMap();
            hospitalMap.putAll((Map) ConversionUtils.listBeanToListMap(hospitalCenters).get(i));
            if (hospitalMap.get("created_timestamp") != null) {
              Timestamp createdTimestamp = (Timestamp) hospitalMap
                  .get("created_timestamp");
              hospitalMap.remove("created_timestamp");
              hospitalMap.put("created_timestamp",
                  DateUtil.formatIso8601Timestamp(createdTimestamp));
            }

            if (hospitalMap.get("updated_timestamp") != null) {
              Timestamp updatedTimestamp = (Timestamp) hospitalMap
                  .get("updated_timestamp");
              hospitalMap.remove("updated_timestamp");
              hospitalMap.put("updated_timestamp",
                  DateUtil.formatIso8601Timestamp(updatedTimestamp));
            }
            if (hospitalMap.get("center_address") != null
                && !"".equals(hospitalMap.get("center_address"))) {
              String str = (String) hospitalMap.get("center_address");
              hospitalMap.remove("center_address");
              str = str.trim().replace("\\r", "").replace("\\n", "");
              hospitalMap.put("center_address", str.trim());
            }

            hospitalList.add(hospitalMap);
          }
        }
        List<BasicDynaBean> departments = null;
        if (status == null || status.trim().length() == 0) {
          departments = new GenericDAO("department").listAll(con, departmentslist, "status", "A",
              "dept_name");
        } else {
          departments = new GenericDAO("department").listAll(con, departmentslist, null, null,
              "dept_name");
        }
        List<BasicDynaBean> doctors = getAllSchedulableDoctors(con, "doctor_name", status);
        List doctorList = new ArrayList();
        for (int i = 0; i < doctors.size(); i++) {
          Map doctorMap = new HashMap();
          doctorMap.putAll((Map) ConversionUtils.listBeanToListMap(doctors).get(i));
          if (doctorMap.get("doctor_address") != null
              && !"".equals(doctorMap.get("doctor_address"))) {
            String str = (String) doctorMap.get("doctor_address");
            str = str.trim().replace("\\r", "").replace("\\n", "");
            doctorMap.remove("doctor_address");
            doctorMap.put("doctor_address", str.trim());
          }
          doctorList.add(doctorMap);
        }

        schedulerMasterDataMap.put("hospital_center_master", hospitalList);
        schedulerMasterDataMap.put("hospital_departments",
            ConversionUtils.listBeanToListMap(departments));
        schedulerMasterDataMap.put("hospital_doctors", doctorList);
        schedulerMasterDataMap.put("return_code", "2001");
        schedulerMasterDataMap.put("return_message", "Success");

      }
      logger.info("getting all scheduler realted data....");
    }

    logger.info("sending the response back to the requesting server");
    response.getWriter().write(js.deepSerialize(schedulerMasterDataMap));
    response.flushBuffer();
    return null;
  }

  /**
   * Retrieve slots available for doctor.
   * 
   * @param mapping  Action Mapping
   * @param form     Action Form
   * @param request  Servlet Request Object
   * @param response Servlet Response Object
   * @return Response
   * @throws IOException  IO Exception
   * @throws SQLException SQL Exception
   */
  public ActionForward getDoctorAvailabilitySlots(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException, SQLException, NoSuchAlgorithmException, ParseException, Exception {
    logger.info("getting scheduler realted parameters getDoctorAvailabilitySlots");
    String requestHandalerKey = ApiUtil.getRequestKey(request);
    ServletContext ctx = servlet.getServletContext();
    boolean isAValidRequest = false;
    Map<String, Object> sessionMap = ServletContextUtil.getContextParametersMap(ctx);
    Map sessionParameters = null;
    String successMsg = "";
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
        isAValidRequest = (currentTime.getTime() - loginTime.getTime()) / 60000 <= validDuration;
      }
    }

    response.setContentType("application/json");
    response.setHeader("Cache-Control", "no-cache");
    // response.setHeader("Access-Control-Allow-Origin", "*");
    Connection con = null;
    String schedulerType = "DOC";
    String centerId = request.getParameter("center_id");
    Map<String, Object> doctorAvailabilityMap = new HashMap<String, Object>();
    JSONSerializer js = JsonProcessor.getJSONParser();
    logger.info("getting session related data from conetxt" + sessionMap);

    if (!isAValidRequest) {
      successMsg = "invalid request token,please login again";
      logger.info("invalid request token,please login again");
      logger.info("sending the response back to the requesting server");
      doctorAvailabilityMap.put("return_code", "1001");
      doctorAvailabilityMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(doctorAvailabilityMap));
      response.flushBuffer();
      return null;
    }

    // Check the rights for Resource Default Availability,Resource Availability Overrides screen
    boolean isScreenRights = ScreenRights.getScreenRights(requestHandalerKey, ctx,
        "doctorAvailabilitySlots");
    if (!isScreenRights) {
      successMsg = "Permission Denied. Please check with Administrator.";
      doctorAvailabilityMap.put("return_code", "1003");
      doctorAvailabilityMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(doctorAvailabilityMap));
      response.flushBuffer();
      return null;
    }

    String appointmentDate = request.getParameter("appointment_date");
    String scheduleName = request.getParameter("resource_id");
    if (scheduleName == null || scheduleName.isEmpty()) {
      successMsg = "Mandatory fields are not supplied";
      doctorAvailabilityMap.put("return_code", "1002");
      doctorAvailabilityMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(doctorAvailabilityMap));
      response.flushBuffer();
      return null;
    }

    Date apptDate = DateUtil.getCurrentDate();
    if (appointmentDate != null && !appointmentDate.isEmpty()) {
      try {
        apptDate = DateUtil.parseIso8601Date(appointmentDate);
      } catch (ParseException pe) {
        successMsg = "Invalid appointment date";
        doctorAvailabilityMap.put("return_code", "1021");
        doctorAvailabilityMap.put("return_message", successMsg);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write(js.deepSerialize(doctorAvailabilityMap));
        response.flushBuffer();
        return null;
      }
    }

    Date forDate = apptDate;
    Calendar cal = Calendar.getInstance();
    cal.setTime(apptDate);
    int dayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) - 1);
    List<Time> timeList = new ArrayList<Time>();
    if (isAValidRequest) {
      Integer centerIdInt = null;
      if (centerId != null && !centerId.equals("")) {
        centerIdInt = Integer.parseInt(centerId);
      }
      // set connection details with default values.
      String schema = (String) sessionParameters.get("hospital_name");
      RequestContext.setConnectionDetails(
          new String[] { "", "", schema, "", "0" });
      
      BasicDynaBean centerBean = null;
      String userName = (String) sessionParameters.get("customer_user_id");
      String roleId = null;
      if ((boolean) sessionParameters.get("patient_login")) {
        centerBean = centerDao.findByKey("center_id", 0);
        roleId = "-1"; //Hack to skip role checks for patient in 
      } else {
        GenericDAO userDAO = new GenericDAO("u_user");
        BasicDynaBean userBean = userDAO.findByKey("emp_username", userName);
        roleId = String.valueOf(((BigDecimal) userBean.get("role_id")));
        centerBean = centerDao.findByKey("center_id", userBean.get("center_id"));
      }
      RequestContext.setConnectionDetails(new String[] { "", "", schema, userName,
          String.valueOf((Integer) centerBean.get("center_id")), 
          (String) centerBean.get("center_name"), roleId });
      
      
      logger.info("getting connection object" + con + "----"
          + sessionParameters.get("hospital_name"));
      boolean isSharingAlreadyBookedSlots = true;
      String bookedSlot = request.getParameter("booked_slot");
      if (bookedSlot != null && bookedSlot.equalsIgnoreCase("I")) {
        isSharingAlreadyBookedSlots = false;
      }
      List<BasicDynaBean> resourceList = ResourceDAO.getResourceListByResourceType(schedulerType,
          scheduleName);
      BasicDynaBean doctor = new GenericDAO("doctors").findByKey("doctor_id", scheduleName);
      List<BasicDynaBean> avialableSlotsList = null;
      int counter = 0;
      List<String> timeStrList = new ArrayList<String>();
      if (doctor != null) {
        while (timeList == null || timeList.size() < 1) {
          avialableSlotsList = getDoctorAvailableTimings(schedulerType, forDate, dayOfWeek,
              scheduleName, centerIdInt);
          
          if (avialableSlotsList != null && !avialableSlotsList.isEmpty()) {
            avialableSlotsList = filterVisitTimingsByVisitMode(avialableSlotsList);
          }

          if (avialableSlotsList != null) {
            timeList = getAvailabilitySlots(resourceList, avialableSlotsList, forDate, doctor,
                centerIdInt, isSharingAlreadyBookedSlots);
            if (timeList != null && timeList.size() > 0) {
              Calendar cal2 = Calendar.getInstance();
              cal2.setTime(forDate);
              cal2.add(Calendar.DAY_OF_MONTH, -1);
              Date date = new Date(cal2.getTimeInMillis());
              for (int i = 0; i < timeList.size(); i++) {
                Timestamp utc = (DateUtil.parseTimestamp8601("00:00:00Z"));
                Time slot = timeList.get(i);
                if (slot.before(utc)) {
                  timeStrList.add(DateUtil.formatIso8601Date(date) + "T"
                      + DateUtil.formatIso8601Time((timeList.get(i))));
                } else {
                  timeStrList.add(DateUtil.formatIso8601Date(forDate) + "T"
                      + DateUtil.formatIso8601Time((timeList.get(i))));
                }
              }
              break;
            }
          }
          cal.add(Calendar.DATE, 1);
          forDate = new Date(cal.getTimeInMillis());
          dayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) - 1);
          counter++;
          if (counter > 30) {
            successMsg = "Doctor is not available at least for a month from "
                + DateUtil.formatIso8601Date(apptDate);
            doctorAvailabilityMap.put("return_code", "2000");
            doctorAvailabilityMap.put("return_message", successMsg);
            response.getWriter().write(js.deepSerialize(doctorAvailabilityMap));
            response.flushBuffer();
            return null;
          }
        }
      }
      // BasicDynaBean resourceBean = getResourceCategoryDetails(schedulerType, scheduleName);
      logger.info("getting doctor availabilities....");
      doctorAvailabilityMap.put("doctor_first_available_date", DateUtil.formatIso8601Date(forDate));
      doctorAvailabilityMap.put("doctor_available_times", timeStrList);
      List<String> timeStrList2 = new ArrayList<String>();
      List<Map<String, Object>> timeCenterList = new ArrayList<Map<String, Object>>();
      if (avialableSlotsList != null) {
        timeCenterList = getAvailabilitySlotsWithCenter(resourceList, avialableSlotsList, apptDate,
            doctor, centerIdInt, isSharingAlreadyBookedSlots);
        if (timeCenterList != null && timeCenterList.size() > 0) {
          Calendar cal3 = Calendar.getInstance();
          cal3.setTime(forDate);
          cal3.add(Calendar.DAY_OF_MONTH, -1);
          Date date2 = new Date(cal3.getTimeInMillis());
          if (timeCenterList != null && timeCenterList.size() > 0) {
            for (int j = 0; j < timeCenterList.size(); j++) {
              Time utc2 = DateUtil.parseIso8601Time("00:00:00Z");
              Map<String, Object> temp = (HashMap) timeCenterList.get(j);
              Time slot2 = (Time) temp.get("timeslot");
              if (slot2.before(utc2)) {
                temp.put("timeslot",
                    DateUtil.formatIso8601Date(date2) + "T" + DateUtil.formatIso8601Time(slot2));
              } else {
                temp.put("timeslot",
                    DateUtil.formatIso8601Date(forDate) + "T" + DateUtil.formatIso8601Time(slot2));
              }
            }
          }
        }
      }
      doctorAvailabilityMap.put("doctor_available_times_with_centers", timeCenterList);
    }
    doctorAvailabilityMap.put("return_code", "2001");
    doctorAvailabilityMap.put("return_message", "Success");
    logger.info("sending the response back to the requesting server");
    response.getWriter().write(js.deepSerialize(doctorAvailabilityMap));
    response.flushBuffer();
    return null;
  }

  /**
   * Get doctor available timings.
   * 
   * @param schedulerType Scheduler Type
   * @param forDate       Date for which timings is required
   * @param dayOfWeek     which day of the week
   * @param resourceName  Resource Name
   * @param centerId      Center ID
   * @return List of timings
   * @throws Exception exception
   */
  private List<BasicDynaBean> getDoctorAvailableTimings(String schedulerType, Date forDate,
      int dayOfWeek, String resourceName, Integer centerId) throws Exception {
    List<BasicDynaBean> timingList = null;
    ResourceDAO resourceDAO = new ResourceDAO();
    timingList = resourceDAO.getResourceAvailabilities(schedulerType, forDate, resourceName, "A",
        centerId);
    if (timingList == null || (timingList != null && timingList.size() < 1)) {
      timingList = resourceDAO.getResourceAvailabilities(schedulerType, forDate, resourceName, "N",
          centerId);
      if (timingList != null && timingList.size() == 1) {
        return null;
      }
    }

    if (timingList == null || (timingList != null && timingList.size() < 1)) {
      timingList = resourceDAO.getResourceDefaultAvailabilities(resourceName, dayOfWeek,
          schedulerType, "A", centerId);
      if (timingList == null || (timingList != null && timingList.size() < 1)) {
        timingList = resourceDAO.getResourceDefaultAvailabilities(resourceName, dayOfWeek,
            schedulerType, "N", centerId);
        if (timingList != null && timingList.size() == 1) {
          return null;
        }
      }
    }

    if (timingList == null || (timingList != null && timingList.size() < 1)) {
      timingList = resourceDAO.getResourceDefaultAvailabilities("*", dayOfWeek, schedulerType, "A",
          0);
      if (timingList == null || (timingList != null && timingList.size() < 1)) {
        timingList = resourceDAO.getResourceDefaultAvailabilities("*", dayOfWeek, schedulerType,
            "N", 0);
        if (timingList != null && timingList.size() == 1) {
          return null;
        }
      } else if (centerId != null) {
        for (BasicDynaBean timing : timingList) {
          timing.set("center_id", centerId);
        }
      }
    }
    return timingList;
  }

  /**
   * Save an appointment.
   * 
   * @param mapping  Action Mapping
   * @param form     Action Form
   * @param request  Servlet Request Object
   * @param response Servlet Response Object
   * @return Response
   * @throws IOException  IO Exception
   * @throws SQLException SQL Exception
   */
  public ActionForward saveAppointment(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws ServletException, Exception {
    logger.info("getting scheduler realted parameters saveAppointmnt");
    String requestHandalerKey = ApiUtil.getRequestKey(request);
    List<Appointments> appList = new ArrayList<>();
    JSONSerializer js = JsonProcessor.getJSONParser();
    boolean success = false;
    String successMsg = "success";
    Map<String, Object> responseMap = new HashMap<String, Object>();
    ServletContext ctx = servlet.getServletContext();
    Map<String, Object> sessionMap = ServletContextUtil.getContextParametersMap(ctx);
    logger.info("getting session related data from conetxt" + sessionMap);
    response.setContentType("application/json");
    response.setHeader("Cache-Control", "no-cache");
    // response.setHeader("Access-Control-Allow-Origin", "*");
    boolean isAValidRequest = false;
    String mrNo = request.getParameter("mr_no");
    String patientEmailParam = request.getParameter("patient_email");
    String appointmentSource = request.getParameter("appointment_source");
    String sendCommunication = request.getParameter("send_communication");
    String appointmentStatus = request.getParameter("appointment_status");
    if (appointmentStatus != null && appointmentStatus.equalsIgnoreCase("confirmed")) {
      appointmentStatus = "Confirmed";
    } else {
      appointmentStatus = "Booked";
    }
    String returnCode = "";
    Map sessionParameters = null;
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
        isAValidRequest = (currentTime.getTime() - loginTime.getTime()) / 60000 <= validDuration;
        if ((boolean) sessionParameters.get("patient_login")) {
          mrNo = (String) sessionParameters.get("customer_user_id");
        }
      }
    }

    if (!isAValidRequest) {
      successMsg = "invalid request token,please login again";
      logger.info("invalid request token,please login again");
      logger.info("sending the response back to the requesting server");
      responseMap.put("return_code", "1001");
      responseMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(responseMap));
      response.flushBuffer();
      return null;
    }
    logger.info("valid request");
    // Check the rights for Doctor Scheduler screen
    boolean isScreenRights = ScreenRights.getScreenRights(requestHandalerKey, ctx,
        "doctorScheduler");
    if (!isScreenRights) {
      successMsg = "Permission Denied. Please check with Administrator.";
      responseMap.put("return_code", "1003");
      responseMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(responseMap));
      response.flushBuffer();
      return null;
    }
    logger.info("got screen rights");
    String appointmentDateTimeStr = request.getParameter("appointment_date");
    String resourceId = request.getParameter("resource_id");
    String centerIdStr = request.getParameter("center_id");
    String patientName = request.getParameter("patient_name");
    String phoneNo = request.getParameter("patient_phone");
    boolean isValidMobileNumber = com.insta.hms.common.PhoneNumberUtil.isValidNumberMobile(phoneNo);
    if (((resourceId == null || resourceId.isEmpty())
        || (appointmentDateTimeStr == null || appointmentDateTimeStr.isEmpty())
        || (centerIdStr == null || centerIdStr.isEmpty()))
        || ((mrNo == null || mrNo.isEmpty()) && (phoneNo == null || phoneNo.isEmpty()))
        || ((mrNo == null || mrNo.isEmpty()) && (patientName == null || patientName.isEmpty()))) {
      successMsg = "Mandatory fields are not supplied";
      responseMap.put("return_code", "1002");
      responseMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(responseMap));
      response.flushBuffer();
      return null;
    }

    logger.info("mandatory fields supplied");

    if (mrNo == null && !isValidMobileNumber) {
      successMsg = "Invalid mobile number";
      responseMap.put("return_code", "1004");
      responseMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(responseMap));
      response.flushBuffer();
      return null;
    }
    logger.info("valid number");

    String patientId = null;
    ArrayList<AppointMentResource> scheduleAppointItemBean = new ArrayList<>();
    ArrayList<Appointments> scheduleAppointBeanList = new ArrayList<>();
    ArrayList<AppointMentResource> scheduleAppointItemBeanRecuured = new ArrayList<>();
    Appointments appointment = null;
    String countryCode = com.insta.hms.common.PhoneNumberUtil.getCountryCode(phoneNo);
    Connection con = null;
    if (isAValidRequest) {
      // set connection details with default values.
      String schema = (String) sessionParameters.get("hospital_name");
      RequestContext.setConnectionDetails(
          new String[] { "", "", schema, "", "0" });

      BasicDynaBean centerBean = null;
      String userName = (String) sessionParameters.get("customer_user_id");
      String roleId = null;
      if ((boolean) sessionParameters.get("patient_login")) {
        centerBean = centerDao.findByKey("center_id", 0);
        roleId = "-1"; //Hack to skip role checks for patient in 
      } else {
        GenericDAO userDAO = new GenericDAO("u_user");
        BasicDynaBean userBean = userDAO.findByKey("emp_username", userName);
        roleId = String.valueOf(((BigDecimal) userBean.get("role_id")));
        centerBean = centerDao.findByKey("center_id", userBean.get("center_id"));
      }
      RequestContext.setConnectionDetails(new String[] { "", "", schema, userName,
          String.valueOf((Integer) centerBean.get("center_id")), 
          (String) centerBean.get("center_name"), roleId });
      logger.info("set request context");
      boolean schedule = isResourceSchedulable(resourceId);
      if (!schedule) {
        successMsg = "Resource is not schedulable";
        responseMap.put("return_code", "1021");
        responseMap.put("return_message", successMsg);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write(js.deepSerialize(responseMap));
        response.flushBuffer();
        return null;
      }
      logger.info("resource schedulable");
      if (mrNo != null && !mrNo.isEmpty()) {
        List<BasicDynaBean> patientMrNo = new GenericDAO("patient_details")
            .listAll(Arrays.asList(new String[] { "mr_no" }), "mr_no", mrNo, null);
        if (patientMrNo == null || patientMrNo.size() == 0) {
          successMsg = "failed to book the Appointment.";
          responseMap.put("return_code", "1025");
          responseMap.put("return_message", successMsg);
          response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
          response.getWriter().write(js.deepSerialize(responseMap));
          response.flushBuffer();
          return null;
        }
      }
      logger.info("got patient");
      BasicDynaBean doctorCategorybean = getResourceCategoryDetails("DOC", resourceId);
      int duration = (Integer) doctorCategorybean.get("default_duration");
      Timestamp appointmentTime = null;
      Integer appointmentId = null;
      try {
        appointmentTime = DateUtil.parseIso8601Timestamp(appointmentDateTimeStr);
        if (appointmentTime == null) {
          successMsg = "Invalid appointment date";
          logger.info("Invalid appointment date");
          logger.info("sending the response back to the requesting server");
          responseMap.put("return_code", "1026");
          responseMap.put("return_message", successMsg);
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          response.getWriter().write(js.deepSerialize(responseMap));
          response.flushBuffer();
          return null;
        }
        logger.info("valid appointment date");
        long apptTimeEpochMili = appointmentTime.getTime();
        apptTimeEpochMili = apptTimeEpochMili + (duration * 60 * 1000);
        Timestamp endTimestamp = new Timestamp(apptTimeEpochMili);
        // check for duplication on appointment for the same patient.
        List<BasicDynaBean> beans = null;
        if (mrNo != null && !mrNo.isEmpty()) {
          beans = ResourceDAO.IsExitsAppointment(appointmentTime, endTimestamp, -1, mrNo, null,
              null, null);
        } else {
          beans = ResourceDAO.IsExitsAppointment(appointmentTime, endTimestamp, -1, null,
              patientName, phoneNo, null);
        }
        if (beans != null && beans.size() > 0) {
          successMsg = "This time slot is already booked for this patient";
          responseMap.put("return_code", "1022");
          responseMap.put("return_message", successMsg);
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          response.getWriter().write(js.deepSerialize(responseMap));
          response.flushBuffer();
          return null;
        }
        // get appointment source
        Integer appSource = 0;
        BasicDynaBean appointmentSourceBean = AppointmentSourceDAO
            .getAppointmentSourceDetailsByName(appointmentSource);
        if (appointmentSourceBean != null) {
          appSource = (Integer) appointmentSourceBean.get("appointment_source_id");
          if (appSource != 0) {
            Integer limit = (Integer) appointmentSourceBean.get("patient_day_appt_limit");
            if (ResourceDAO.isAppointmentLimitReached(appointmentTime, mrNo, phoneNo, limit)) {
              successMsg = "The number of appointments booked per day"
                  + " for this patient has hit the limit.";
              responseMap.put("return_code", "1031");
              responseMap.put("return_message", successMsg);
              response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
              response.getWriter().write(js.deepSerialize(responseMap));
              response.flushBuffer();
              return null;
            }
          }
        }
        logger.info("time slot not booked for patient");
        List<BasicDynaBean> list = ResourceDAO.isSlotBooked(appointmentTime, endTimestamp,
            resourceId, null, resourceId, "OPDOC");
        if (list != null && list.size() > 0) {
          // slot is booked and need to check whether the overbook enabled for the resource.
          Integer overbookLimit = isResourceOverbooked(resourceId);
          int overbookCount = ResourceDAO.getOverbookCount(resourceId, appointmentTime);
          boolean overbook = overbookLimit != null && overbookLimit != 0;
          if (overbook == true && overbookCount > overbookLimit) {
            successMsg = "The number of appointments booked for this slot"
                + " has hit the overbook limit.";
            responseMap.put("return_code", "1028");
            responseMap.put("return_message", successMsg);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(js.deepSerialize(responseMap));
            response.flushBuffer();
            return null;
          }
          if (overbookLimit != null && overbook == false) {
            successMsg = "The time slot for this resource is already booked";
            responseMap.put("return_code", "1023");
            responseMap.put("return_message", successMsg);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(js.deepSerialize(responseMap));
            response.flushBuffer();
            return null;
          }
        }
        logger.info("time slot available for resource");
        // check if resource is available for this time slot
        String[] split = (DateUtil.formatTimestamp(appointmentTime)).split(" ");
        Date forDate = DateUtil.parseDate(split[0]);
        int centerId = 0;
        if (centerIdStr != null && !centerIdStr.equals("")) {
          centerId = Integer.parseInt(centerIdStr);
        }
        if (centerId == 0) {
          int maxCenters = (Integer) GenericPreferencesDAO.getAllPrefs()
              .get("max_centers_inc_default");
          if (maxCenters > 1) {
            successMsg = "Appointment can not be booked for default center";
            responseMap.put("return_code", "1027");
            responseMap.put("return_message", successMsg);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(js.deepSerialize(responseMap));
            response.flushBuffer();
            return null;
          }
        }
        boolean available = isResourceAvailable(resourceId, appointmentTime, endTimestamp, forDate,
            centerId);
        if (!available) {
          if (centerId != 0) {
            available = isResourceAvailable(resourceId, appointmentTime, endTimestamp, forDate, 0);
          }
          if (!available) {
            successMsg = "The resource is not available";
            responseMap.put("return_code", "1024");
            responseMap.put("return_message", successMsg);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(js.deepSerialize(responseMap));
            response.flushBuffer();
            return null;
          }
        }
        logger.info("resource is available");
        con = DataBaseUtil.getConnection();
        Timestamp modTime = DataBaseUtil.getDateandTime();
        con.setAutoCommit(false);
        Integer contactId = null;
        if (mrNo != null && !mrNo.isEmpty()) {
          BasicDynaBean patDetailsBean = new PatientDetailsDAO().getBean();
          new GenericDAO("patient_details").loadByteaRecords(patDetailsBean, "mr_no", mrNo);
          if (patDetailsBean != null) {
            phoneNo = (String) patDetailsBean.get("patient_phone");
            if (phoneNo == null || phoneNo.isEmpty()) {
              successMsg = "Please update the patient's mobile number"
                  + " before booking an appointment.";
              responseMap.put("return_code", "1029");
              responseMap.put("return_message", successMsg);
              response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
              response.getWriter().write(js.deepSerialize(responseMap));
              response.flushBuffer();
              return null;
            }
            String patFirstName = (String) patDetailsBean.get("patient_name");
            patFirstName = (patFirstName == null) ? "" : patFirstName;
            String patMiddleName = (String) patDetailsBean.get("middle_name");
            patMiddleName = (patMiddleName == null) ? "" : patMiddleName;
            String patLastName = (String) patDetailsBean.get("last_name");
            patLastName = (patLastName == null) ? "" : patLastName;
            patientName = patFirstName + " " + patMiddleName + " " + patLastName;
          }
          logger.info("got patient info");
        } else { //check if contact already exist
          BasicDynaBean contactBean = contactsDao.getBean();
          contactBean.set("patient_name",patientName);
          contactBean.set("patient_contact",phoneNo);
          contactId = ContactsDAO.getContactIdIfContactExists(contactBean);
          if (contactId == null) {
            // generate a new contact
            contactId = contactsDao.getNextSequence();
            contactBean.set("contact_id",contactId);
            contactBean.set("patient_contact_country_code",countryCode);
            contactBean.set("preferred_language", (String) GenericPreferencesDAO.getAllPrefs().get(
                "contact_pref_lang_code"));
            contactBean.set("vip_status", "N");
            contactBean.set("send_sms", "N");
            contactBean.set("send_email", "N");
            contactBean.set("create_time", modTime);
            contactBean.set("mod_user", "InstaAPI");
            contactsDao.insert(con, contactBean);
          }          
        }
        
        appointmentId = Integer.parseInt(ResourceDAO.getNextAppointMentId());
        appointment = new Appointments(appointmentId);

        Integer resOverbooklimit = isResourceOverbooked(resourceId);
        boolean overBookAllowed = resOverbooklimit == null || resOverbooklimit > 0;
        if (overBookAllowed) {
          appointment.setUnique_appt_ind(ResourceDAO.getNextUniqueAppointMentInd());
        } else {
          appointment.setUnique_appt_ind(0);
        }
        int waitList = ResourceDAO.getOverbookCount(resourceId, appointmentTime);
        appointment.setPrim_res_id(resourceId);
        appointment.setAppointmentId(appointmentId);
        appointment.setContactId(contactId);
        appointment.setAppointmentDuration(duration);
        appointment.setMrNo(mrNo);
        appointment.setPatientName(patientName);
        appointment.setVisitId(patientId);
        appointment.setAppointmentTime(appointmentTime);
        appointment.setScheduleName("-1");
        appointment.setAppointStatus(appointmentStatus);
        appointment.setSchedulerVisitType("M");
        appointment.setCenterId(Integer.parseInt(centerIdStr));
        appointment.setBookedBy("InstaAPI");
        appointment.setBookedTime(DateUtil.getCurrentTimestamp());
        appointment.setScheduleId(1);
        appointment.setPhoneNo(phoneNo);
        appointment.setChangedBy("InstaAPI");
        appointment.setApp_source_id(appSource);
        appointment.setPhoneCountryCode("+" + countryCode);
        appointment.setWaitlist(waitList);
        appointment.setVisitMode("I");
        scheduleAppointBeanList.add(appointment);
        logger.info("appointment bean set");
        AppointMentResource res = null;
        res = new AppointMentResource(appointmentId, "OPDOC", resourceId);
        res.setAppointment_item_id(new GenericDAO("scheduler_appointment_items").getNextSequence());
        res.setAppointment_item_id(res.getAppointment_item_id());
        res.setUser_name("InstaAPI");
        res.setMod_time(modTime);
        scheduleAppointItemBean.add(res);
        logger.info("appointment resource bean set");
        logger.info("booking an appointment");
        success = new ResourceBO().saveAppointmentAndresources(con, scheduleAppointBeanList,
            scheduleAppointItemBean, scheduleAppointItemBeanRecuured);
        logger.info("returned from bo call");
      } catch (ParseException pe) {
        successMsg = "Invalid appointment date";
        logger.info("Invalid appointment date");
        logger.info("sending the response back to the requesting server");
        responseMap.put("return_code", "1026");
        responseMap.put("return_message", successMsg);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write(js.deepSerialize(responseMap));
        response.flushBuffer();
        return null;
      } catch (Exception ex) {
        success = false;
        throw ex;
      } finally {
        DataBaseUtil.commitClose(con, success);
      }
    }

    if (success) {
      Integer appId = appointment.getAppointmentId();
      if (sendCommunication != null && sendCommunication.equals("true")) {
        ResourceBO.sendSMS(appId, appointmentStatus,
            "appointment_" + appointmentStatus.toLowerCase(), false);
        if (appointmentStatus.equalsIgnoreCase("confirmed")) {
          ResourceBO.sendSMS(appId, appointmentStatus, "doc_appt_confirmed", false);
        }
      }
      // Add the doctor appt to Practo
      BasicDynaBean modBean = modulesActivated.findByKey("module_id", "mod_practo_advantage");
      if (modBean != null && modBean.get("activation_status") != null
          && modBean.get("activation_status").equals("Y")) {
        PractoBookHelper.addDoctorAppointmentsToPracto(appId, true);
      }
      schedulePushEvent(appId.toString(), "APPOINTMENT_" + appointmentStatus.toUpperCase());

      responseMap.put("appointment_id", appId);
      successMsg = "Success";
      returnCode = "2001";
      logger.info("appointment booked.");
    }

    if (!success) {
      successMsg = "failed to book the Appointment.";
      returnCode = "1025";
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      logger.info("failed to book an appointment.");
    }
    responseMap.put("return_code", returnCode);
    responseMap.put("return_message", successMsg);
    logger.info("sending the response back to the requesting server");
    response.getWriter().write(js.deepSerialize(responseMap));
    response.flushBuffer();
    return null;
  }

  /**
   * Returns boolean indicating if resource is schedulable.
   * 
   * @param resourceId resource id
   * @return Returns boolean indicating if resource is schedulable
   * @throws SQLException SQL Exception
   */
  private boolean isResourceSchedulable(String resourceId) throws SQLException {
    // see if the resource is schedulable
    GenericDAO docDao = new GenericDAO("doctors");
    List columns = new ArrayList();
    columns.add("schedule");
    Map<String, Object> identifiers = new HashMap<String, Object>();
    identifiers.put("doctor_id", resourceId);
    BasicDynaBean docBean = docDao.findByKey(columns, identifiers);
    boolean schedule = (Boolean) docBean.get("schedule");
    return schedule;
  }

  /**
   * Get overbook limit for a resource .
   * 
   * @param resourceId resource id
   * @return Returns overbook limit
   * @throws SQLException SQL Exception
   */
  private Integer isResourceOverbooked(String resourceId) throws SQLException {
    GenericDAO docDao = new GenericDAO("doctors");
    List columns = new ArrayList();
    columns.add("overbook_limit");
    Map<String, Object> identifiers = new HashMap<String, Object>();
    identifiers.put("doctor_id", resourceId);
    BasicDynaBean docBean = docDao.findByKey(columns, identifiers);
    Integer overbookLimit = (Integer) docBean.get("overbook_limit");
    return overbookLimit;
  }

  /**
   * Returns boolean indicating if resource is avialable during given time.
   * 
   * @param resourceId           resource id
   * @param startAppointmentTime appointment start time
   * @param endAppointmentTime   appointment start time
   * @param forDate              for date
   * @param centerId             center id
   * @return Returns boolean indicating if resource is available
   * @throws Exception exception
   */
  private boolean isResourceAvailable(String resourceId, Timestamp startAppointmentTime,
      Timestamp endAppointmentTime, Date forDate, int centerId) throws Exception {

    Calendar cal = Calendar.getInstance();
    cal.setTime(forDate);
    int dayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) - 1);
    List resourceAvailList = getDoctorAvailableTimings("DOC", forDate, dayOfWeek, resourceId,
        centerId);
    BasicDynaBean resourceBean = null;
    Timestamp startAvailTime = null;
    Timestamp endAvailTime = null;
    boolean resAvailable = false;
    if (resourceAvailList != null) {
      for (int j = 0; j < resourceAvailList.size(); j++) {
        resourceBean = (BasicDynaBean) resourceAvailList.get(j);
        int resCenterId = (Integer) resourceBean.get("center_id");
        if (resourceBean.get("availability_status").equals("A") && resCenterId == centerId) {
          Time fromTime = (java.sql.Time) resourceBean.get("from_time");
          SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
          String fromTimeStr = sdf.format(fromTime);
          startAvailTime = DateUtil.parseTimestamp(DateUtil.formatDate(forDate), fromTimeStr);
          Time toTime = (java.sql.Time) resourceBean.get("to_time");
          String toTimeStr = sdf.format(toTime);
          endAvailTime = DateUtil.parseTimestamp(DateUtil.formatDate(forDate), toTimeStr);
          if ((startAppointmentTime.getTime() <= startAvailTime.getTime()
              && endAppointmentTime.getTime() > startAvailTime.getTime())
              || (startAppointmentTime.getTime() >= startAvailTime.getTime()
                  && startAppointmentTime.getTime() < endAvailTime.getTime())) {
            resAvailable = true;

            break;
          }
        }
      }
    }
    return resAvailable;
  }

  /**
   * Update an appointment.
   * 
   * @param mapping  Action Mapping
   * @param form     Action Form
   * @param request  Servlet Request Object
   * @param response Servlet Response Object
   * @return Response
   * @throws IOException  IO Exception
   * @throws SQLException SQL Exception
   */
  public ActionForward updateAppointment(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws ServletException, Exception {
    logger.info("getting scheduler realted parameters updateAppointment");
    String requestHandalerKey = ApiUtil.getRequestKey(request);
    Connection con = null;
    JSONSerializer js = JsonProcessor.getJSONParser();
    Map<String, Object> responseMap = new HashMap<String, Object>();
    ServletContext ctx = servlet.getServletContext();
    Map<String, Object> sessionMap = ServletContextUtil.getContextParametersMap(ctx);
    logger.info("getting session related data from conetxt" + sessionMap);
    boolean isAValidRequest = false;
    String returnCode = "";
    response.setContentType("application/json");
    response.setHeader("Cache-Control", "no-cache");
    Map sessionParameters = null;
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
        isAValidRequest = (currentTime.getTime() - loginTime.getTime()) / 60000 <= validDuration;
      }
    }

    boolean success = true;
    String successMsg = "Success";
    String appointmentDateTimeStr = request.getParameter("appointment_date");
    String patientName = request.getParameter("patient_name");
    String phoneNo = request.getParameter("patient_phone");
    String resourceId = request.getParameter("resource_id");
    String appointmentStatus = request.getParameter("appointment_status");
    String appointmentDuration = request.getParameter("appointment_duration");
    String sendCommunication = request.getParameter("send_communication");
    String primaryResId = null;
    String finalStatus = null;
   
    if (!isAValidRequest) {
      successMsg = "invalid request token,please login again";
      logger.info("invalid request token,please login again");
      logger.info("sending the response back to the requesting server");
      responseMap.put("return_code", "1001");
      responseMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(responseMap));
      response.flushBuffer();
      return null;
    }
    if ("".equals(appointmentDuration) || appointmentDuration != null) {
      successMsg = "appointment_duration parameter is not required";
      logger.info("appointment_duration parameter is not required");
      responseMap.put("return_code", "1023");
      responseMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(responseMap));
      response.flushBuffer();
      return null;

    }

    // Check the rights for Doctor Scheduler screen
    boolean isScreenRights = ScreenRights.getScreenRights(requestHandalerKey, ctx,
        "doctorScheduler");
    if (!isScreenRights) {
      successMsg = "Permission Denied. Please check with Administrator.";
      responseMap.put("return_code", "1003");
      responseMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(responseMap));
      response.flushBuffer();
      return null;
    }

    String appointmentIdStr = request.getParameter("appointment_id");
    if (appointmentIdStr == null || appointmentIdStr.isEmpty()) {
      successMsg = "Mandatory fields are not supplied";
      responseMap.put("return_code", "1002");
      responseMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(responseMap));
      response.flushBuffer();
      return null;
    }

    if (!appointmentIdStr.matches("[0-9]*")) {

      successMsg = "failed to update the Appointment.";
      logger.info("failed to update the Appointment. Appointment id is not all numbers");
      responseMap.put("return_code", "1025");
      responseMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      response.getWriter().write(js.deepSerialize(responseMap));
      response.flushBuffer();
      return null;
    }
    boolean timeOrResChanged = true;
    boolean duplicate = false;
    if (isAValidRequest) {
      // set connection details with default values.
      String schema = (String) sessionParameters.get("hospital_name");
      RequestContext.setConnectionDetails(
          new String[] { "", "", schema, "", "0" });

      BasicDynaBean centerBean = null;
      String userName = (String) sessionParameters.get("customer_user_id");
      String roleId = null;
      if ((boolean) sessionParameters.get("patient_login")) {
        centerBean = centerDao.findByKey("center_id", 0);
        roleId = "-1"; //Hack to skip role checks for patient in 
      } else {
        GenericDAO userDAO = new GenericDAO("u_user");
        BasicDynaBean userBean = userDAO.findByKey("emp_username", userName);
        roleId = String.valueOf(((BigDecimal) userBean.get("role_id")));
        centerBean = centerDao.findByKey("center_id", userBean.get("center_id"));
      }
      RequestContext.setConnectionDetails(new String[] { "", "", schema, userName,
          String.valueOf((Integer) centerBean.get("center_id")), 
          (String) centerBean.get("center_name"), roleId });

      Map appointmentMap = new HashMap();
      appointmentMap.put("confirmed", Arrays.asList(new String[] { "Booked", "Confirmed" }));
      appointmentMap.put("booked", Arrays.asList(new String[] { "Confirmed", "Booked" }));

      Map filterMap = new HashMap();
      filterMap.put("appointment_id", Integer.parseInt(appointmentIdStr));
      List<BasicDynaBean> schdulerBean = new GenericDAO("scheduler_appointments").listAll(
          Arrays.asList(
              new String[] { "mr_no", "duration", "appointment_status", "center_id", "prim_res_id",
                  "appointment_time", "patient_name", "patient_contact", "app_source_id",
                  "waitlist" }),
          filterMap, null);
      BasicDynaBean apptBean = null;
      if (schdulerBean == null || schdulerBean.size() == 0) {
        successMsg = "failed to update the Appointment.";
        logger.info("failed to update the Appointment.No bean found");
        responseMap.put("return_code", "1025");
        responseMap.put("return_message", successMsg);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.getWriter().write(js.deepSerialize(responseMap));
        response.flushBuffer();
        return null;
      }
      logger.info("Bean found");
      apptBean = (BasicDynaBean) schdulerBean.get(0);
      primaryResId = (String) apptBean.get("prim_res_id");
      if (((String) apptBean.get("appointment_status")).equalsIgnoreCase("Cancel")
          || ((String) apptBean.get("appointment_status")).equalsIgnoreCase("noshow")) {
        successMsg = "Appointments in 'Cancel' or 'No Show' status can not be updated.";
        returnCode = "1028";
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        responseMap.put("return_code", returnCode);
        responseMap.put("return_message", successMsg);
        logger.info("Appointments in 'Cancel' or 'No Show' status can not be updated.");
        logger.info("sending the response back to the requesting server");
        response.getWriter().write(js.deepSerialize(responseMap));
        response.flushBuffer();
        return null;
      }
      if (appointmentStatus != null && !appointmentStatus.isEmpty()) {

        List<String> arrayList = (List<String>) appointmentMap
            .get(appointmentStatus.toLowerCase());
        boolean flag = false;
        if (null != arrayList) {
          for (String str : arrayList) {
            if (str.equalsIgnoreCase(appointmentStatus)) {
              flag = true;
            }
          }

        }
        logger.info("flag" + flag);
        if (!flag) {
          successMsg = "failed to update the Appointment.";
          logger.info("failed to update the Appointment.");
          responseMap.put("return_code", "1025");
          responseMap.put("return_message", successMsg);
          response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
          response.getWriter().write(js.deepSerialize(responseMap));
          response.flushBuffer();
          return null;
        }

      }

      Timestamp appointmentTime = null;
      Map fields = new HashMap();
      Map keys = new HashMap();
      try {
        if (appointmentDateTimeStr == null || appointmentDateTimeStr.isEmpty()) {
          appointmentTime = (Timestamp) apptBean.get("appointment_time");
        } else {
          appointmentTime = DateUtil.parseIso8601Timestamp(appointmentDateTimeStr);
        }
        if (appointmentTime == null) {
          successMsg = "Invalid appointment date";
          logger.info("Invalid appointment date");
          logger.info("sending the response back to the requesting server");
          responseMap.put("return_code", "1026");
          responseMap.put("return_message", successMsg);
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          response.getWriter().write(js.deepSerialize(responseMap));
          response.flushBuffer();
          return null;
        }
        if (resourceId == null || resourceId.isEmpty()) {
          resourceId = (String) apptBean.get("prim_res_id");
        }
        boolean schedule = isResourceSchedulable(resourceId);
        con = DataBaseUtil.getConnection();
        con.setAutoCommit(false);
        String mrNo = (String) apptBean.get("mr_no");
        if (!schedule) {
          successMsg = "The resource is not schedulable";
          responseMap.put("return_code", "1021");
          responseMap.put("return_message", successMsg);
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          response.getWriter().write(js.deepSerialize(responseMap));
          response.flushBuffer();
          return null;
        }
        long apptStartEpochMili = appointmentTime.getTime();

        Timestamp existingTime = (Timestamp) apptBean.get("appointment_time");
        int centerId = 0;
        String centerIdStr = request.getParameter("center_id");
        if (centerIdStr == null || centerIdStr.isEmpty()) {
          centerId = (Integer) apptBean.get("center_id");
        } else {
          if (!centerIdStr.matches("[0-9]*")) {
            successMsg = "failed to update the Appointment.";
            logger.info("failed to update the Appointment. Center id is not all numbers");
            responseMap.put("return_code", "1025");
            responseMap.put("return_message", successMsg);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(js.deepSerialize(responseMap));
            response.flushBuffer();
            return null;
          }
          logger.info("center_id" + centerId);
          centerId = Integer.parseInt(centerIdStr);
          if (centerId == 0) {
            int maxCenters = (Integer) GenericPreferencesDAO.getAllPrefs()
                .get("max_centers_inc_default");
            if (maxCenters > 1) {
              successMsg = "Appointment can not be booked for default center";
              responseMap.put("return_code", "1029");
              responseMap.put("return_message", successMsg);
              response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
              response.getWriter().write(js.deepSerialize(responseMap));
              response.flushBuffer();
              return null;
            }
          }
        }
        boolean timeChanged = true;
        if (apptStartEpochMili == existingTime.getTime()) {
          timeChanged = false;
          int existid = (Integer) apptBean.get("center_id");

          if (centerId == existid && resourceId.equals((String) apptBean.get("prim_res_id"))) {
            String status = (String) apptBean.get("appointment_status");
            timeOrResChanged = false;
            if (status != null && !status.isEmpty()) {
              if (status.equalsIgnoreCase(appointmentStatus)) {
                duplicate = true;
              }
            }
          }
        }
        logger.info("duplicate check");
        if (duplicate) {
          responseMap.put("return_code", "2001");
          responseMap.put("return_message", "Success");
          logger.info("updating appointments");
          logger.info("sending the response back to the requesting server");
          response.getWriter().write(js.deepSerialize(responseMap));
          response.flushBuffer();
          return null;
        }
        int duration = (Integer) apptBean.get("duration");
        Timestamp endTimestamp = new Timestamp(
            apptStartEpochMili + (duration * 60 * 1000));

        // check for duplication on appointment for the same patient.
        if (timeChanged) {
          String phone = null;
          List<BasicDynaBean> beans = null;
          if (mrNo != null) {
            beans = ResourceDAO.IsExitsAppointment(appointmentTime, endTimestamp, -1, mrNo, null,
                null, null);
          } else {
            phone = apptBean.get("patient_contact") != null
                ? (String) apptBean.get("patient_contact")
                : "";
            String name = apptBean.get("patient_name") != null
                ? (String) apptBean.get("patient_name")
                : "";
            beans = ResourceDAO.IsExitsAppointment(appointmentTime, endTimestamp, -1, null, name,
                phone, null);
          }
          if (beans != null && beans.size() > 0) {
            successMsg = "This time slot is already booked for this patient";
            responseMap.put("return_code", "1022");
            responseMap.put("return_message", successMsg);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(js.deepSerialize(responseMap));
            response.flushBuffer();
            return null;
          }
          Object appSource = apptBean.get("app_source_id");
          if (appSource != null && (Integer) appSource != 0) {
            Integer appSourceInt = (Integer) appSource;
            BasicDynaBean appointmentSourceBean = AppointmentSourceDAO
                .getAppointmentSourceDetails(appSourceInt);
            Integer limit = (Integer) appointmentSourceBean.get("patient_day_appt_limit");
            if (ResourceDAO.isAppointmentLimitReached(appointmentTime, mrNo, phone, limit)) {
              successMsg = "The number of appointments booked per day for this"
                  + " patient has hit the limit.";
              responseMap.put("return_code", "1031");
              responseMap.put("return_message", successMsg);
              response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
              response.getWriter().write(js.deepSerialize(responseMap));
              response.flushBuffer();
              return null;
            }
          }
          List<BasicDynaBean> list = ResourceDAO.isSlotBooked(appointmentTime, endTimestamp,
              resourceId, appointmentIdStr, resourceId, "OPDOC");
          if (list != null && list.size() > 0) {
            // slot is booked and need to check whether the overbook enabled for the resource.
            Integer overbookLimit = isResourceOverbooked(resourceId);
            int overbookCount = ResourceDAO.getOverbookCount(resourceId, appointmentTime);
            boolean overbook = overbookLimit != null && overbookLimit != 0;
            if (overbook == true && overbookCount > overbookLimit) {
              successMsg = "The number of appointments booked for this slot"
                  + " has hit the overbook limit.";
              responseMap.put("return_code", "1030");
              responseMap.put("return_message", successMsg);
              response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
              response.getWriter().write(js.deepSerialize(responseMap));
              response.flushBuffer();
              return null;
            }
            logger.info("overbook" + overbook);
            if (overbook == false) {
              successMsg = "The time slot for this resource is already booked";
              responseMap.put("return_code", "1027");
              responseMap.put("return_message", successMsg);
              response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
              response.getWriter().write(js.deepSerialize(responseMap));
              response.flushBuffer();
              return null;
            }

          }
        }
        // check if resource is available for this time slot
        logger.info("check if resource is available");
        String[] split = (DateUtil.formatTimestamp(appointmentTime)).split(" ");
        Date apptDate = DateUtil.parseDate(split[0]);
        boolean available = isResourceAvailable(resourceId, appointmentTime, endTimestamp,
            apptDate, centerId);
        if (!available) {
          if (centerId != 0) {
            available = isResourceAvailable(resourceId, appointmentTime, endTimestamp, apptDate,
                0);
          }
          if (!available) {
            successMsg = "The resource is not available";
            responseMap.put("return_code", "1024");
            responseMap.put("return_message", successMsg);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(js.deepSerialize(responseMap));
            response.flushBuffer();
            return null;
          }
        }
        Integer resOverbooklimit = ResourceDAO.isResourceOverbooked(con, resourceId, "DOC");
        boolean overBookAllowed = resOverbooklimit == null || resOverbooklimit > 0;
        logger.info("overBookAllowed" + overBookAllowed);
        if (overBookAllowed) {
          fields.put("unique_appt_ind", ResourceDAO.getNextUniqueAppointMentInd());
        } else {
          fields.put("unique_appt_ind", 0);
        }
        fields.put("prim_res_id", resourceId);
        fields.put("appointment_time", appointmentTime);
        fields.put("res_sch_name", "-1");
        fields.put("center_id", centerId);
        fields.put("res_sch_id", 1);
        fields.put("duration", duration);
        fields.put("changed_by", "InstaAPI");
        Timestamp modTime = DbUtil.getDateandTime(con);
        fields.put("changed_time", modTime);
        fields.put("booked_by", "InstaAPI");
        int waitList = ResourceDAO.getOverbookCount(resourceId, appointmentTime);
        fields.put("waitlist",waitList);
        keys.put("appointment_id", Integer.parseInt(appointmentIdStr));
        if (appointmentStatus != null && !appointmentStatus.isEmpty()) {
          fields.put("appointment_status", appointmentStatus);
          finalStatus = appointmentStatus;
        } else {
          finalStatus = (String) apptBean.get("appointment_status");
          fields.put("appointment_status", finalStatus);
        }

        success = ResourceBO.updateAppointments(con, fields, keys);
        logger.info("updateAppointments" + success);
        if (success) {
          int appointmentId = Integer.parseInt(appointmentIdStr);
          ResourceDTO rdto = new ResourceDTO();
          rdto.setAppointmentId(appointmentId);
          rdto.setResourceId(resourceId);
          rdto.setResourceType("OPDOC");
          rdto.setUser_name("InstaAPI");
          rdto.setMod_time(modTime);
          Map<String, Object> identifiers = new HashMap<String, Object>();
          identifiers.put("appointment_id", appointmentId);
          identifiers.put("resource_id", primaryResId);
          BasicDynaBean bean = new GenericDAO("scheduler_appointment_items").findByKey(identifiers);
          if (bean != null) {
            rdto.setAppointment_item_id((Integer) bean.get("appointment_item_id"));
          }
          List<ResourceDTO> scheduleAppointItemInsertList = new ArrayList<>();
          List<ResourceDTO> scheduleAppointItemUpdateList = new ArrayList<>();
          List<ResourceDTO> scheduleAppointItemDeleteList = new ArrayList<>();
          scheduleAppointItemUpdateList.add(rdto);
          success = new ResourceBO().updateSchedulerResourceDetails(con,
              scheduleAppointItemInsertList, scheduleAppointItemUpdateList,
              scheduleAppointItemDeleteList);
          logger.info("updateSchedulerResourceDetails" + success);
          if (apptBean.get("waitlist") != null) {
            Timestamp apptTime = (Timestamp)apptBean.get("appointment_time");
            upgradeAppointmentsWaitlist(apptTime, (Integer) apptBean.get("duration"),
                (String) apptBean.get("prim_res_id"), "OPDOC", (Integer) apptBean.get("waitlist"),
                sendCommunication);
          }

        }
      } catch (ParseException pe) {
        successMsg = "Invalid appointment date";
        logger.info("Invalid appointment date");
        logger.info("sending the response back to the requesting server");
        responseMap.put("return_code", "1026");
        responseMap.put("return_message", successMsg);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write(js.deepSerialize(responseMap));
        response.flushBuffer();
        return null;
      } catch (Exception ex) {
        success = false;
      } finally {
        DataBaseUtil.commitClose(con, success);
      }
    }
    if (success) {
      successMsg = "Success";
      returnCode = "2001";
      schedulePushEvent(appointmentIdStr,
          timeOrResChanged ? Events.APPOINTMENT_RESCHEDULED : Events.APPOINTMENT_UPDATED);
      int appointmentId = Integer.parseInt(appointmentIdStr);
      if (!duplicate && sendCommunication != null && sendCommunication.equals("true")) {
        if (timeOrResChanged) {
          ResourceBO.sendSMS(appointmentId, appointmentStatus, "appointment_details_changed",
              false);
        } else if (finalStatus.equalsIgnoreCase("confirmed")) {
          ResourceBO.sendSMS(appointmentId, "Confirmed", "appointment_confirmed", false);
          ResourceBO.sendSMS(appointmentId, "Confirmed", "doc_appt_confirmed", false);
        }
      }
      // Add the doctor appt to Practo
      BasicDynaBean modBean = modulesActivated.findByKey("module_id", "mod_practo_advantage");
      if (modBean != null && modBean.get("activation_status") != null
          && modBean.get("activation_status").equals("Y")) {
        PractoBookHelper.addDoctorAppointmentsToPracto(appointmentId, false);
      }
    }
    if (!success) {
      successMsg = "failed to update the Appointment.";
      returnCode = "1025";
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
    responseMap.put("return_code", returnCode);
    responseMap.put("return_message", successMsg);
    logger.info("updating appointments");
    logger.info("sending the response back to the requesting server");
    response.getWriter().write(js.deepSerialize(responseMap));
    response.flushBuffer();
    return null;
  }
  
  /**
   * Upgrade appointments waitlist.
   *
   * @param startApptTime the start appt time
   * @param duration the duration
   * @param primaryResource the primary resource
   * @param primaryResourceType the primary resource type
   * @param waitlist the waitlist
   */
  public void upgradeAppointmentsWaitlist(Timestamp startApptTime, Integer duration,
      String primaryResource, String primaryResourceType, Integer waitlist,
      String sendCommunication) {
    long apptTimeLong = startApptTime.getTime();
    apptTimeLong = apptTimeLong + (duration * 60 * 1000);
    Timestamp endTime = new java.sql.Timestamp(apptTimeLong);

    List<BasicDynaBean> appointmentsForUpgrade = null;
    try {
      appointmentsForUpgrade = ResourceDAO.isSlotBooked(startApptTime, endTime, "-1",
          primaryResource, primaryResourceType, waitlist);
    } catch (SQLException exp) {
      logger.error("Exception while getting appointments for which waitlist should be changed",
          exp);
    }
    if (!appointmentsForUpgrade.isEmpty()) {
      String[] apptIds = new String[appointmentsForUpgrade.size()];
      int count = 0;
      List<Object[]> updateParamsList = new ArrayList<>();
      for (BasicDynaBean apptBean : appointmentsForUpgrade) {
        Integer appointmentId = (Integer) apptBean.get("appointment_id");
        Integer waitlsitNumber = (Integer) apptBean.get("waitlist");
        apptIds[count++] = String.valueOf(appointmentId);
        updateParamsList.add(new Object[] { waitlsitNumber - 1, appointmentId });
      }
      ResourceDAO.batchUpgradeWaitlist(updateParamsList);
      
      if (sendCommunication != null && sendCommunication.equals("true")) {
        for (BasicDynaBean apptBean : appointmentsForUpgrade) {
          try {
            ResourceBO.sendSMS((Integer) apptBean.get("appointment_id"),
                apptBean.get("appointment_status").toString(), "waitlist", false);
          } catch (Exception exp) {
            logger.error("Exception while sending waitlist sms",exp);
          }
         
        }
      }
    }
  }

  /**
   * Cancel an appointment.
   * 
   * @param mapping  Action Mapping
   * @param form     Action Form
   * @param request  Servlet Request Object
   * @param response Servlet Response Object
   * @return Response
   * @throws IOException  IO Exception
   * @throws SQLException SQL Exception
   */
  public ActionForward cancelAppointment(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws ServletException, Exception {
    logger.info("getting scheduler realted parameter cancelAppointment");
    String requestHandalerKey = ApiUtil.getRequestKey(request);
    Connection con = null;
    JSONSerializer js = JsonProcessor.getJSONParser();
    Map<String, Object> responseMap = new HashMap<String, Object>();
    ServletContext ctx = servlet.getServletContext();
    Map<String, Object> sessionMap = ServletContextUtil.getContextParametersMap(ctx);
    logger.info("getting session related data from conetxt" + sessionMap);
    boolean isAValidRequest = false;
    response.setContentType("application/json");
    response.setHeader("Cache-Control", "no-cache");
    // response.setHeader("Access-Control-Allow-Origin", "*");
    String returnCode = "";
    Map sessionParameters = null;
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
        isAValidRequest = (currentTime.getTime() - loginTime.getTime()) / 60000 <= validDuration;
      }
    }
    // HttpSession session= SessionUtil.getSession(requestHandalerKey, ctx);
    boolean success = true;
    String successMsg = "success";
    String appointmentIdStr = request.getParameter("appointment_id");
    String cancelReason = request.getParameter("cancel_reason");
    if (!isAValidRequest) {
      successMsg = "invalid request token,please login again";
      logger.info("invalid request token,please login again");
      logger.info("sending the response back to the requesting server");
      responseMap.put("return_code", "1001");
      responseMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(responseMap));
      response.flushBuffer();
      return null;
    }

    // Check the rights for Doctor Scheduler screen
    boolean isScreenRights = ScreenRights.getScreenRights(requestHandalerKey, ctx,
        "doctorScheduler");
    if (!isScreenRights) {
      successMsg = "Permission Denied. Please check with Administrator.";
      responseMap.put("return_code", "1003");
      responseMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(responseMap));
      response.flushBuffer();
      return null;
    }

    if ((appointmentIdStr == null || appointmentIdStr.isEmpty())
        || (cancelReason == null || cancelReason.isEmpty())) {
      successMsg = "Mandatory fields are not supplied";
      responseMap.put("return_code", "1002");
      responseMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(responseMap));
      response.flushBuffer();
      return null;
    }

    Integer appointmentId = 0;
    try {
      appointmentId = (appointmentIdStr != null && !appointmentIdStr.isEmpty())
          ? Integer.parseInt(appointmentIdStr)
          : 0;
    } catch (NumberFormatException nfe) {
      successMsg = "failed to cancel the Appointment.";
      returnCode = "1021";
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      responseMap.put("return_code", returnCode);
      responseMap.put("return_message", successMsg);
      logger.info("failed to cancel the Appointment.");
      logger.info("sending the response back to the requesting server");
      response.getWriter().write(js.deepSerialize(responseMap));
      response.flushBuffer();
      return null;
    }

    if (isAValidRequest) {
      try {
        // set connection details with default values.
        String schema = (String) sessionParameters.get("hospital_name");
        RequestContext.setConnectionDetails(
            new String[] { "", "", schema, "", "0" });

        BasicDynaBean centerBean = null;
        String userName = (String) sessionParameters.get("customer_user_id");
        String roleId = null;
        if ((boolean) sessionParameters.get("patient_login")) {
          centerBean = centerDao.findByKey("center_id", 0);
          roleId = "-1"; //Hack to skip role checks for patient in 
        } else {
          GenericDAO userDAO = new GenericDAO("u_user");
          BasicDynaBean userBean = userDAO.findByKey("emp_username", userName);
          roleId = String.valueOf(((BigDecimal) userBean.get("role_id")));
          centerBean = centerDao.findByKey("center_id", userBean.get("center_id"));
        }
        RequestContext.setConnectionDetails(new String[] { "", "", schema, userName,
            String.valueOf((Integer) centerBean.get("center_id")), 
            (String) centerBean.get("center_name"), roleId });
        
        con = DataBaseUtil.getConnection();
        con.setAutoCommit(false);
        List<BasicDynaBean> apptBean = new GenericDAO("scheduler_appointments").listAll(con, null,
            "appointment_id", appointmentId, null);
        if (apptBean == null || apptBean.size() == 0) {
          successMsg = "failed to cancel the Appointment.";
          returnCode = "1021";
          response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
          responseMap.put("return_code", returnCode);
          responseMap.put("return_message", successMsg);
          logger.info("failed to cancel the Appointment.");
          logger.info("sending the response back to the requesting server");
          response.getWriter().write(js.deepSerialize(responseMap));
          response.flushBuffer();
          return null;
        }
        Map<String, Object> columndata = new HashMap<String, Object>();
        columndata.put("appointment_status", "Cancel");
        columndata.put("changed_by", "InstaAPI");
        columndata.put("changed_time", DateUtil.getCurrentTimestamp());
        columndata.put("cancel_reason", cancelReason);
        columndata.put("appointment_id", appointmentId);
        BasicDynaBean bean = apptBean.get(0);
        Integer uniqueApptInd = null;
        if (((Integer) bean.get("unique_appt_ind")) == 0) {
          uniqueApptInd = ResourceDAO.getNextUniqueAppointMentInd(con);
        } else {
          uniqueApptInd = (Integer) bean.get("unique_appt_ind");
        }
        Timestamp modTime = DataBaseUtil.getDateandTime();
        success = new ResourceBO().updateAppointments(con, appointmentId, "Cancel", cancelReason,
            "InstaAPI", uniqueApptInd, modTime);
      } finally {
        DataBaseUtil.commitClose(con, success);
      }
    }

    if (!success) {
      successMsg = "failed to cancel the Appointment.";
      returnCode = "1021";
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
    String sendCommunication = request.getParameter("send_communication");
    if (sendCommunication != null && sendCommunication.equals("true")) {
      ResourceBO.sendSMS(appointmentId, "Cancel", "appointment_cancelled", false);
    }
    List columnList = new ArrayList();
    columnList.add("waitlist");
    columnList.add("appointment_time");
    columnList.add("duration");
    columnList.add("prim_res_id");
    Map<String, Object> identifiers = new HashMap<>();
    identifiers.put("appointment_id", appointmentId);
    BasicDynaBean apptBean = new GenericDAO("scheduler_appointments").findByKey(columnList,
        identifiers);
    if (apptBean.get("waitlist") != null) {
      Timestamp apptTime = (Timestamp) apptBean.get("appointment_time");
      upgradeAppointmentsWaitlist(apptTime, (Integer) apptBean.get("duration"),
          apptBean.get("prim_res_id").toString(), "OPDOC", (Integer) apptBean.get("waitlist"),
          sendCommunication);
    }
    // Add the doctor appt to Practo
    BasicDynaBean modBean = modulesActivated.findByKey("module_id", "mod_practo_advantage");
    if (modBean != null && modBean.get("activation_status") != null
        && modBean.get("activation_status").equals("Y")) {
      PractoBookHelper.addDoctorAppointmentsToPracto(appointmentId, false);
    }
    schedulePushEvent(appointmentId.toString(), Events.APPOINTMENT_CANCEL);
    successMsg = "Success";
    returnCode = "2001";
    responseMap.put("return_code", returnCode);
    responseMap.put("return_message", successMsg);
    logger.info("cancelling appointments");
    logger.info("sending the response back to the requesting server");
    response.getWriter().write(js.deepSerialize(responseMap));
    response.flushBuffer();
    return null;
  }

  private static final String GET_CENTERWISE_SCHEDULE_LIST_DOCTER = "SELECT d.doctor_id ,"
      + " d.doctor_name, d.specialization, d.doctor_type, d.doctor_address, d.doctor_mobile,"
      + " d.doctor_mail_id, d.op_consultation_validity, d.dept_id, d.ot_doctor_flag,"
      + " d.consulting_doctor_flag, d.schedule, d.qualification, d.registration_no,"
      + " d.res_phone, d.clinic_phone, d.payment_category, d.payment_eligible,"
      + " d.doctor_license_number, d.allowed_revisit_count, d.custom_field1_value,"
      + " d.custom_field2_value, d.custom_field3_value, d.custom_field4_value,"
      + " d.custom_field5_value, d.ip_discharge_consultation_validity,hcm.center_name,"
      + " d.ip_discharge_consultation_count, d.ip_template_id, d.overbook_limit,"
      + " to_char(d.created_timestamp AT TIME ZONE (SELECT  current_setting('TIMEZONE'))"
      + " AT TIME ZONE 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') AS created_timestamp,"
      + " to_char(d.updated_timestamp AT TIME ZONE (SELECT  current_setting('TIMEZONE'))"
      + " AT TIME ZONE 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') AS updated_timestamp,"
      + " d.practition_type, d.status as doctor_status, dcm.status as doctor_center_status,"
      + " dcm.doc_center_id, dcm.center_id ,dept.dept_name FROM doctors d"
      + " LEFT JOIN doctor_center_master dcm ON(dcm.doctor_id=d.doctor_id)"
      + " LEFT JOIN department dept ON(d.dept_id=dept.dept_id)"
      + " JOIN hospital_center_master hcm on(dcm.center_id=hcm.center_id)"
      + " WHERE @ d.scheduleable_by = 'A' order by ?";

  /**
   * Get list of all schedulable doctors.
   * 
   * @param con        Database connection
   * @param sortcolumn Column to sort response list on
   * @param status     resource status
   * @return list of all schedulable doctors
   * @throws SQLException SQL Exception
   */
  public static List<BasicDynaBean> getAllSchedulableDoctors(Connection con, String sortcolumn,
      String status) throws SQLException {
    PreparedStatement ps = null;

    try {
      String query = new String(GET_CENTERWISE_SCHEDULE_LIST_DOCTER);
      if (status == null || status.trim().length() == 0) {
        query = query.replace("@", "d.status = 'A' AND dcm.status = 'A' AND hcm.status='A' AND");
      } else {
        query = query.replace("@", "");
      }
      ps = con.prepareStatement(query);
      ps.setString(1, sortcolumn);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Get list of availability slots.
   * 
   * @param resourceList                List of resources
   * @param availableSlotsList          List of slots
   * @param forDate                     For Date
   * @param doctor                      Doctor Bean Object
   * @param centerId                    Center ID
   * @param isSharingAlreadyBookedSlots is sharing already booked slots
   * @return List of availability slots with center
   * @throws Exception exception
   */
  private List getAvailabilitySlots(List<BasicDynaBean> resourceList,
      List<BasicDynaBean> availableSlotsList, Date forDate, BasicDynaBean doctor, Integer centerId,
      boolean isSharingAlreadyBookedSlots) throws Exception {
    List<Time> timeList = new ArrayList<>();
    Integer overbook = (Integer) doctor.get("overbook_limit");
    Connection con = null;
    con = DataBaseUtil.getConnection();
    String doctorId = doctor.get("doctor_id").toString();
    overbook = new ResourceDAO().isResourceOverbooked(con, doctorId, "DOC");
    con.close();
    if (centerId == null) {
      centerId = 0;
    }
    if (!isSharingAlreadyBookedSlots) {
      overbook = 0;
    }
    if (resourceList != null && !resourceList.isEmpty()) {
      BasicDynaBean bean = resourceList.get(0);
      int defaultDuration = (Integer) bean.get("default_duration");
      long slotDuration = (defaultDuration * 60 * 1000);

      if (availableSlotsList != null && !availableSlotsList.isEmpty()) {
        for (int i = 0; i < availableSlotsList.size(); i++) {
          BasicDynaBean resourceAvailableBean = availableSlotsList.get(i);
          Time fromTime = (Time) resourceAvailableBean.get("from_time");
          Long resourceFromTime = (Long) fromTime.getTime();
          int startMinute = fromTime.getMinutes() + (fromTime.getHours() * 60);
          int remainderFromTime = startMinute % defaultDuration;
          if (remainderFromTime != 0) {
            int minToadd = defaultDuration - remainderFromTime;
            resourceFromTime = resourceFromTime + (minToadd * 60 * 1000);
          }

          Time toTime = (Time) resourceAvailableBean.get("to_time");
          Long resourceToTime = (Long) toTime.getTime();
          int endMinute = toTime.getMinutes() + (toTime.getHours() * 60);
          // handling 23:59 end time case
          if (endMinute == 1439) {
            endMinute = 1440;
          }
          int remainderToTime = endMinute % defaultDuration;
          if (remainderToTime != 0) {
            int minToSubtract = remainderToTime;
            resourceToTime = resourceToTime - (minToSubtract * 60 * 1000);
          }
          Integer resCenterId = null;
          if (resourceAvailableBean.get("center_id") != null) {
            resCenterId = (Integer) resourceAvailableBean.get("center_id");
          }
          // filtering the timlist with primary resources max and min time.
          if (centerId == null || centerId == -1 || centerId == 0 || centerId.equals(resCenterId)) {
            if (resourceFromTime != null && resourceToTime != null
                && !resourceFromTime.equals(resourceToTime)) {
              for (long j = resourceFromTime; j < resourceToTime; j = j + slotDuration) {
                // exclude non available time
                timeList.add(new java.sql.Time(j));
              }
            }
          }
        }
      }
    }
    List<Time> removalList = new ArrayList<Time>();
    List<Time> removeList = new ArrayList<Time>();
    Map<String, Object> appointmentCount = new HashMap<String, Object>();
    if (doctor != null && overbook != null) {
      BasicDynaBean doctorCategorybean = getResourceCategoryDetails("DOC", doctorId);
      BigDecimal duration = new BigDecimal((Integer) doctorCategorybean.get("default_duration"));
      List<BasicDynaBean> appointmentList = ResourceDAO
          .getAppointmentWithSecondaryResource(doctorId, forDate);
      for (BasicDynaBean appBean : appointmentList) {
        String status = (String) appBean.get("appointment_status");
        BigDecimal apptDuration = new BigDecimal((Integer) appBean.get("duration"));
        String apptDateTimeStr = ((Timestamp) appBean.get("appointment_time")).toString();
        String[] apptDateTimeArr = apptDateTimeStr.split(" ");
        Time apptTime = DateUtil.parseTime(apptDateTimeArr[1]);

      
        for (long l = apptTime.getTime(); l < apptTime.getTime()
            + (apptDuration.intValue() * 60 * 1000); l = l + (5 * 60 * 1000)) {
          Map<String, Object> map = new HashMap<String, Object>();
          java.sql.Time time = new java.sql.Time(l);
          Integer value = (Integer) appointmentCount.get(time.toString());
          if (value == null) {
            appointmentCount.put(time.toString(), 1);
          } else {
            appointmentCount.put(time.toString(), value + 1);
          }
          removalList.add(new java.sql.Time(l));
        }
        for (long l = apptTime.getTime(); l > apptTime.getTime()
            - (duration.longValue() * 60 * 1000); l = l - (5 * 60 * 1000)) {
          removalList.add(new java.sql.Time(l));

        }
      }

      for (Entry<String, Object> entry : appointmentCount.entrySet()) {
        Map<String, Object> map = new HashMap<String, Object>();
        if ((Integer) entry.getValue() > overbook) {
          removeList.add(java.sql.Time.valueOf(entry.getKey()));
        }
      }
      if (removeList != null && removeList.size() > 0) {
        timeList.removeAll(removeList);
      }
    }
    return timeList;
  }

  /**
   * Get list of availability slots with center.
   * 
   * @param resourceList                List of resources
   * @param availableSlotsList          List of slots
   * @param forDate                     For Date
   * @param doctor                      Doctor Bean Object
   * @param centerId                    Center ID
   * @param isSharingAlreadyBookedSlots is sharing already booked slots
   * @return List of availability slots with center
   * @throws Exception exception
   */
  private List getAvailabilitySlotsWithCenter(List<BasicDynaBean> resourceList,
      List<BasicDynaBean> availableSlotsList, Date forDate, BasicDynaBean doctor, Integer centerId,
      boolean isSharingAlreadyBookedSlots) throws Exception {
    List<Map<String, Object>> timeSlotsList = new ArrayList<Map<String, Object>>();
    Integer overbook = (Integer) doctor.get("overbook_limit");
    Connection con = null;
    con = DataBaseUtil.getConnection();
    String doctorId = doctor.get("doctor_id").toString();
    overbook = new ResourceDAO().isResourceOverbooked(con, doctorId, "DOC");
    con.close();
    if (centerId == null) {
      centerId = 0;
    }
    if (!isSharingAlreadyBookedSlots) {
      overbook = 0;
    }
    if (resourceList != null && resourceList.size() > 0) {
      BasicDynaBean bean = resourceList.get(0);
      int defaultDuration = (Integer) bean.get("default_duration");
      long slotDuration = (defaultDuration * 60 * 1000);

      if (availableSlotsList != null && availableSlotsList.size() > 0) {
        for (int i = 0; i < availableSlotsList.size(); i++) {
          BasicDynaBean resourceAvailableBean = availableSlotsList.get(i);
          Time fromTime = (Time) resourceAvailableBean.get("from_time");
          Long resourceFromTime = (Long) fromTime.getTime();
          int startMinute = fromTime.getMinutes() + (fromTime.getHours() * 60);
          int remainderFromTime = startMinute % defaultDuration;
          if (remainderFromTime != 0) {
            int minToadd = defaultDuration - remainderFromTime;
            resourceFromTime = resourceFromTime + (minToadd * 60 * 1000);
          }

          Time toTime = (Time) resourceAvailableBean.get("to_time");
          Long resourceToTime = (Long) toTime.getTime();
          int endMinute = toTime.getMinutes() + (toTime.getHours() * 60);
          // handling 23:59 end time case
          if (endMinute == 1439) {
            endMinute = 1440;
          }
          int remainderToTime = endMinute % defaultDuration;
          if (remainderToTime != 0) {
            int minToSubtract = remainderToTime;
            resourceToTime = resourceToTime - (minToSubtract * 60 * 1000);
          }
          Integer resCenterId = null;
          if (resourceAvailableBean.get("center_id") != null) {
            resCenterId = (Integer) resourceAvailableBean.get("center_id");
          }
          // filtering the timlist with primary resources max and min time.
          if (centerId == null || centerId == -1 || centerId == 0 || centerId.equals(resCenterId)) {
            if (resourceFromTime != null && resourceToTime != null
                && !resourceFromTime.equals(resourceToTime)) {
              for (long j = resourceFromTime; j < resourceToTime; j = j + slotDuration) {
                // exclude non available time
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("timeslot", new java.sql.Time(j));
                map.put("center_id", (Integer) resourceAvailableBean.get("center_id"));
                timeSlotsList.add(map);
              }
            }
          }
        }
      }
    }
    List<Map<String, Object>> removalList = new ArrayList<Map<String, Object>>();
    List<Map<String, Object>> removeList = new ArrayList<Map<String, Object>>();
    Map<String, Object> appointmentCount =  new HashMap<String, Object>();
    if (doctor != null && overbook != null) {
      BasicDynaBean doctorCategorybean = getResourceCategoryDetails("DOC", doctorId);
      BigDecimal duration = new BigDecimal((Integer) doctorCategorybean.get("default_duration"));
      List<BasicDynaBean> appointmentList = ResourceDAO
          .getAppointmentWithSecondaryResource(doctorId, forDate);
      for (BasicDynaBean appBean : appointmentList) {
        String status = (String) appBean.get("appointment_status");
        if (status.equalsIgnoreCase("Cancel") || status.equalsIgnoreCase("Noshow")) {
          continue;
        }
        BigDecimal apptDuration = new BigDecimal((Integer) appBean.get("duration"));
        String apptDateTimeStr = ((Timestamp) appBean.get("appointment_time")).toString();
        String[] apptDateTimeArr = apptDateTimeStr.split(" ");
        Integer appCenterId = (Integer) appBean.get("center_id");
        Time apptTime = DateUtil.parseTime(apptDateTimeArr[1]);

        for (long l = apptTime.getTime(); l < apptTime.getTime()
            + (apptDuration.intValue() * 60 * 1000); l = l + (5 * 60 * 1000)) {
          Map<String, Object> map = new HashMap<String, Object>();
          map.put("timeslot", new java.sql.Time(l));
          java.sql.Time time = new java.sql.Time(l);
          Integer value = (Integer) appointmentCount.get(time.toString());
          if (value == null) {
            appointmentCount.put(time.toString(), 1);
          } else {
            appointmentCount.put(time.toString(), value + 1);
          }
          map.put("center_id", appCenterId);
          removalList.add(map);
        }
        for (long l = apptTime.getTime(); l > apptTime.getTime()
            - (duration.longValue() * 60 * 1000); l = l - (5 * 60 * 1000)) {
          Map<String, Object> map = new HashMap<String, Object>();
          map.put("timeslot", new java.sql.Time(l));
          map.put("center_id", appCenterId);
          removalList.add(map);
        }
      }
    }
    for (Entry<String, Object> entry : appointmentCount.entrySet()) {
      Map<String, Object> map = new HashMap<String, Object>();
      if ((Integer) entry.getValue() > overbook) {
        map.put("timeslot", java.sql.Time.valueOf(entry.getKey()));
        map.put("center_id", centerId);
        removeList.add(map);
      }
    }
    if (removeList != null && removeList.size() > 0) {
      timeSlotsList.removeAll(removeList);
    }
    return timeSlotsList;
  }

  private List getFirstAvailableSlotWithCenter(List<BasicDynaBean> list,
      List<BasicDynaBean> timingList, Date forDate, BasicDynaBean doctor, Integer centerId,
      boolean isSharingAlreadyBookedSlots) throws Exception {
    List<Map<String, Object>> timeSlotsList = new ArrayList<Map<String, Object>>();
    Map<String, Object> timeSlotsMap = new HashMap<String, Object>();
    Integer overbook = (Integer) doctor.get("overbook_limit");
    String docId = (String) doctor.get("doctor_id");
    if (list != null && list.size() > 0) {
      BasicDynaBean bean = list.get(0);
      int defaultDuration = (Integer) bean.get("default_duration");
      long slotDuration = (defaultDuration * 60 * 1000);

      if (timingList != null && timingList.size() > 0) {
        for (int i = 0; i < timingList.size(); i++) {
          BasicDynaBean resourceAvailableBean = timingList.get(i);
          Time fromTime = (Time) resourceAvailableBean.get("from_time");
          Long resourceFromTime = (Long) fromTime.getTime();
          int startMinute = fromTime.getMinutes() + (fromTime.getHours() * 60);
          int remainderFromTime = startMinute % defaultDuration;
          if (remainderFromTime != 0) {
            int minToadd = defaultDuration - remainderFromTime;
            resourceFromTime = resourceFromTime + (minToadd * 60 * 1000);
          }

          Time toTime = (Time) resourceAvailableBean.get("to_time");
          Long resourceToTime = (Long) toTime.getTime();
          int endMinute = toTime.getMinutes() + (toTime.getHours() * 60);
          // handling 23:59 end time case
          if (endMinute == 1439) {
            endMinute = 1440;
          }
          int remainderToTime = endMinute % defaultDuration;
          if (remainderToTime != 0) {
            int minToSubtract = remainderToTime;
            resourceToTime = resourceToTime - (minToSubtract * 60 * 1000);
          }
          SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
          Time time = DateUtil.getCurrentTime();
          Long currentTime = sdf.parse(time.toString()).getTime();
          Date currentDate = DateUtil.getCurrentDate();
          boolean notToday = forDate.getTime() != currentDate.getTime();

          int resCenterId = (Integer) resourceAvailableBean.get("center_id");
          if ((centerId == null || centerId == -1 || centerId == 0 || centerId.equals(resCenterId))
              && resourceFromTime != null && resourceToTime != null
              && !resourceFromTime.equals(resourceToTime)
              && (notToday || (currentTime > resourceFromTime && currentTime < resourceToTime)
                  || (currentTime < resourceFromTime && currentTime < resourceToTime))) {
            for (long j = resourceFromTime; j < resourceToTime; j = j + slotDuration) {
              if (notToday || j > currentTime) {
                Time startTime = new java.sql.Time(j);
                Time endTime = new java.sql.Time(startTime.getTime() + slotDuration);
                Timestamp startTimestamp = DateUtil.timestampFromDateTime(forDate,
                    startTime);
                Timestamp endTimestamp = DateUtil.timestampFromDateTime(forDate, endTime);
                BasicDynaBean appBean = new ResourceDAO().isResourceBooked(startTimestamp,
                    endTimestamp, docId, "DOC", -1, "DOC", isSharingAlreadyBookedSlots);
                if (appBean == null) {
                  Time utc = DateUtil.parseIso8601Time("00:00:00Z");
                  if (startTime.before(utc)) {
                    Calendar cal1 = Calendar.getInstance();
                    cal1.setTime(forDate);
                    cal1.add(Calendar.DAY_OF_MONTH, -1);
                    Date date = new Date(cal1.getTimeInMillis());
                    timeSlotsMap.put("timeslot", DateUtil.formatIso8601Date(date) + "T"
                        + DateUtil.formatIso8601Time(startTime));
                  } else {
                    timeSlotsMap.put("timeslot", DateUtil.formatIso8601Date(forDate) + "T"
                        + DateUtil.formatIso8601Time(startTime));
                  }
                  timeSlotsMap.put("center_id", (Integer) resourceAvailableBean.get("center_id"));
                  timeSlotsList.add(timeSlotsMap);
                  return timeSlotsList;
                }
              }
            }
          }
        }
      }
    }
    return timeSlotsList;
  }

  private BasicDynaBean getResourceCategoryDetails(String resourceType, String resourceId)
      throws Exception {
    BasicDynaBean resourceDetails = CategoryMasterDAO.getCategoryDetailsByResourceType(resourceId,
        resourceType);
    if (resourceDetails == null) {
      resourceDetails = CategoryMasterDAO.getCategoryDetailsByResourceType("*", resourceType);
    }

    return resourceDetails;
  }

  /**
   * Get patient appointments.
   * 
   * @param mapping  Action Mapping
   * @param form     Action Form
   * @param request  Servlet Request Object
   * @param response Servlet Response Object
   * @return Response
   * @throws Exception exception
   */
  public ActionForward getPatientAppointments(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {
    String fromDate = request.getParameter("from_date");
    String toDate = request.getParameter("to_date");
    String patientPhone = request.getParameter("patient_phone");
    String mrno = request.getParameter("mr_no");
    String requestHandalerKey = ApiUtil.getRequestKey(request);
    JSONSerializer json = JsonProcessor.getJSONParser();
    LinkedHashMap<String, Object> patientAppointmentMap = new LinkedHashMap<String, Object>();
    boolean isValidRequest = false;
    ServletContext ctx = servlet.getServletContext();
    Map<String, Object> sessionMap = ServletContextUtil.getContextParametersMap(ctx);
    logger.info("getting session related data from conetxt" + sessionMap);
    response.setContentType("application/json");
    response.setHeader("Cache-Control", "no-cache");
    Map sessionParameters = null;
    String successMsg = "";
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
        isValidRequest = (currentTime.getTime() - loginTime.getTime()) / 60000 <= validDuration;
        if ((boolean) sessionParameters.get("patient_login")) {
          mrno = (String) sessionParameters.get("customer_user_id");
        }
      }
    }

    if (!isValidRequest) {
      successMsg = "invalid request token,please login again";
      logger.info("invalid request token,please login again");
      logger.info("sending the response back to the requesting server");
      patientAppointmentMap.put("return_code", "1001");
      patientAppointmentMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(json.deepSerialize(patientAppointmentMap));
      response.flushBuffer();
      return null;
    }

    // Check the rights for Scheduler
    boolean isScreenRights = ScreenRights.getScreenRights(requestHandalerKey, ctx,
        "doctorScheduler");
    if (!isScreenRights) {
      successMsg = "Permission Denied. Please check with Administrator.";
      patientAppointmentMap.put("return_code", "1003");
      patientAppointmentMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(json.deepSerialize(patientAppointmentMap));
      response.flushBuffer();
      return null;
    }

    String searchByPatient = request.getParameter("search_by_patient");
    if (searchByPatient == null || searchByPatient.equalsIgnoreCase("Y")) {
      if ((patientPhone == null || patientPhone.isEmpty()) && (mrno == null || mrno.isEmpty())) {
        successMsg = "Mandatory fields are not supplied";
        patientAppointmentMap.put("return_code", "1002");
        patientAppointmentMap.put("return_message", successMsg);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write(json.deepSerialize(patientAppointmentMap));
        response.flushBuffer();
        return null;
      }
    }

    Object fromDateRequestedTimeStamp = null;
    Object toDateRequestedTimeStamp = null;
    Object formattedTo = null;
    Object formattedFrom = null;
    Timestamp currentTimeStamp = DateUtil.getCurrentTimestamp();
    long toEpochMili = 0;
    long fromEpochMili = 0;
    try {
      if (fromDate != null && fromDate.length() > 0) {
        fromDateRequestedTimeStamp = DateUtil.parseDate8601(fromDate);
        formattedFrom = DateUtil.formatIso8601Date((java.sql.Date) fromDateRequestedTimeStamp);
        fromEpochMili = ((java.sql.Date) fromDateRequestedTimeStamp).getTime();
      } else {
        Timestamp sixmonthBack = DateUtil.addSubtractMonths(currentTimeStamp, -6);
        fromDateRequestedTimeStamp = sixmonthBack;
        formattedFrom = DateUtil.formatIso8601Timestamp(sixmonthBack);
        fromEpochMili = ((Timestamp) sixmonthBack).getTime();
      }

      if (toDate != null && toDate.length() > 0) {
        toDateRequestedTimeStamp = DateUtil.parseDate8601(toDate);
        formattedTo = DateUtil.formatIso8601Date((java.sql.Date) toDateRequestedTimeStamp);
        toEpochMili = ((java.sql.Date) toDateRequestedTimeStamp).getTime();
      } else {
        toDateRequestedTimeStamp = currentTimeStamp;
        formattedTo = DateUtil.formatIso8601Timestamp(currentTimeStamp);
        toEpochMili = ((Timestamp) currentTimeStamp).getTime();
      }

    } catch (Exception ex) {
      successMsg = "Invalid input parameters supplied";
      patientAppointmentMap.put("return_code", "1021");
      patientAppointmentMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(json.deepSerialize(patientAppointmentMap));
      response.flushBuffer();
      return null;
    }
    patientAppointmentMap.put("start_date", formattedFrom);
    patientAppointmentMap.put("end_date", formattedTo);

    long diff = toEpochMili - fromEpochMili;
    long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
    if (days < 0 || diff < 0) {
      successMsg = "To date can not be earlier than From date";
      patientAppointmentMap.put("return_code", "1022");
      patientAppointmentMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(json.deepSerialize(patientAppointmentMap));
      response.flushBuffer();
      return null;
    }

    logger.info("Starting to retrieve data!!!");
    Connection con = DbUtil.getConnection((String) sessionParameters.get("hospital_name"));
    String section = "APPOINTMENTS";
    String centerIdStr = request.getParameter("center_id");
    String filterByApptDate = request.getParameter("filter_by_appointment_date");
    String phoneNumberNationalPart = com.insta.hms.common.PhoneNumberUtil
        .getNationalNumber(patientPhone);
    patientAppointmentMap.put("appointments",
        ResourceDAO.getAppointmentsQueryResult(con, section, sessionParameters,
            fromDateRequestedTimeStamp, toDateRequestedTimeStamp, patientPhone,
            phoneNumberNationalPart, mrno, centerIdStr,
            filterByApptDate != null && filterByApptDate.equalsIgnoreCase("Y"),
            searchByPatient == null || searchByPatient.equalsIgnoreCase("Y")));
    logger.info("Data retrieval ends, sending response back to clinet");
    successMsg = "Success";
    patientAppointmentMap.put("return_code", "2001");
    patientAppointmentMap.put("return_message", successMsg);
    response.getWriter().write(json.deepSerialize(patientAppointmentMap));
    response.flushBuffer();
    DbUtil.closeConnections(con, null);
    return null;
  }

  /**
   * Get first available slot for doctor.
   * 
   * @param mapping  Action Mapping
   * @param form     Action Form
   * @param request  Servlet Request Object
   * @param response Servlet Response Object
   * @return Response
   * @throws Exception exception
   */
  public ActionForward getDoctorFirstAvailabilitySlot(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {
    logger.info("getting scheduler realted parameters getDoctorAvailabilitySlots");
    String requestHandalerKey = ApiUtil.getRequestKey(request);
    ServletContext ctx = servlet.getServletContext();
    Map<String, Object> doctorAvailabilityMap = new HashMap<String, Object>();
    boolean isAValidRequest = false;
    Map<String, Object> sessionMap = ServletContextUtil.getContextParametersMap(ctx);
    Map sessionParameters = null;
    String successMsg = "";
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
        isAValidRequest = (currentTime.getTime() - loginTime.getTime()) / 60000 <= validDuration;
      }
    }

    response.setContentType("application/json");
    response.setHeader("Cache-Control", "no-cache");
    // response.setHeader("Access-Control-Allow-Origin", "*");
    Connection con = null;
    String schedulerType = "DOC";
    Date apptDate = null;
    String centerIdStr = request.getParameter("center_id");
    Map<String, Object> firstSlotMap = new HashMap<String, Object>();
    JSONSerializer js = JsonProcessor.getJSONParser();
    logger.info("getting session related data from conetxt" + sessionMap);

    if (!isAValidRequest) {
      successMsg = "invalid request token,please login again";
      logger.info("invalid request token,please login again");
      logger.info("sending the response back to the requesting server");
      doctorAvailabilityMap.put("return_code", "1001");
      doctorAvailabilityMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(doctorAvailabilityMap));
      response.flushBuffer();
      return null;
    }

    // Check the rights for Resource Default Availability,Resource Availability Overrides screen
    boolean isScreenRights = ScreenRights.getScreenRights(requestHandalerKey, ctx,
        "doctorAvailabilitySlots");
    if (!isScreenRights) {
      successMsg = "Permission Denied. Please check with Administrator.";
      doctorAvailabilityMap.put("return_code", "1003");
      doctorAvailabilityMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(doctorAvailabilityMap));
      response.flushBuffer();
      return null;
    }

    String resourceIds = request.getParameter("resource_id");
    if (resourceIds == null || resourceIds.equals("")) {
      successMsg = "Mandatory fields are not supplied";
      doctorAvailabilityMap.put("return_code", "1002");
      doctorAvailabilityMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(doctorAvailabilityMap));
      response.flushBuffer();
      return null;
    }
    String[] scheduleNames = resourceIds.split(",");
    boolean getForDate = false;
    String appointmentDate = request.getParameter("appointment_date");
    if (appointmentDate != null && !appointmentDate.isEmpty()) {
      try {
        apptDate = DateUtil.parseIso8601Date(appointmentDate);
        getForDate = true;
      } catch (ParseException pe) {
        successMsg = "Invalid appointment date";
        doctorAvailabilityMap.put("return_code", "1021");
        doctorAvailabilityMap.put("return_message", successMsg);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write(js.deepSerialize(doctorAvailabilityMap));
        response.flushBuffer();
        return null;
      }
    } else {
      apptDate = DateUtil.getCurrentDate();
    }

    boolean isSharingAlreadyBookedSlots = true;
    String bookedSlot = request.getParameter("booked_slot");
    if (bookedSlot != null && bookedSlot.equalsIgnoreCase("I")) {
      isSharingAlreadyBookedSlots = false;
    }
    if (isAValidRequest) {
      Integer centerIdInt = null;
      if (centerIdStr != null && !centerIdStr.equals("")) {
        centerIdInt = Integer.parseInt(centerIdStr);
      }
      // set connection details with default values.
      String schema = (String) sessionParameters.get("hospital_name");
      RequestContext.setConnectionDetails(
          new String[] { "", "", schema, "", "0" });

      BasicDynaBean centerBean = null;
      String userName = (String) sessionParameters.get("customer_user_id");
      String roleId = null;
      if ((boolean) sessionParameters.get("patient_login")) {
        centerBean = centerDao.findByKey("center_id", 0);
        roleId = "-1"; //Hack to skip role checks for patient in 
      } else {
        GenericDAO userDAO = new GenericDAO("u_user");
        BasicDynaBean userBean = userDAO.findByKey("emp_username", userName);
        roleId = String.valueOf(((BigDecimal) userBean.get("role_id")));
        centerBean = centerDao.findByKey("center_id", userBean.get("center_id"));
      }
      RequestContext.setConnectionDetails(new String[] { "", "", schema, userName,
          String.valueOf((Integer) centerBean.get("center_id")), 
          (String) centerBean.get("center_name"), roleId });
      
      logger.info("getting connection object" + con + "----"
          + sessionParameters.get("hospital_name"));

      for (String scheduleName : scheduleNames) {
        Date froDate = apptDate;
        Calendar cal = Calendar.getInstance();
        cal.setTime(apptDate);
        int dayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) - 1);
        List<Map<String, Object>> timeCenterList = new ArrayList<Map<String, Object>>();
        List<BasicDynaBean> list = ResourceDAO.getResourceListByResourceType(schedulerType,
            scheduleName);
        BasicDynaBean doctor = new GenericDAO("doctors").findByKey("doctor_id", scheduleName);
        List<BasicDynaBean> timingList = null;
        int counter = 0;
        List<String> timeStrList = new ArrayList<String>();
        if (doctor != null) {
          while (timeCenterList == null || timeCenterList.size() < 1) {
            timingList = getDoctorAvailableTimings(schedulerType, froDate, dayOfWeek, scheduleName,
                centerIdInt);
            if (timingList != null && !timingList.isEmpty()) {
              timingList = filterVisitTimingsByVisitMode(timingList);
            }
            if (timingList != null) {
              timeCenterList = getFirstAvailableSlotWithCenter(list, timingList, froDate, doctor,
                  centerIdInt, isSharingAlreadyBookedSlots);
              if (timeCenterList != null && timeCenterList.size() > 0) {
                firstSlotMap.put(scheduleName, timeCenterList);
                break;
              }
            }
            cal.add(Calendar.DATE, 1);
            froDate = new Date(cal.getTimeInMillis());
            dayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) - 1);
            counter++;
            if (counter > 10 || getForDate) {
              firstSlotMap.put(scheduleName, timeCenterList);
              break;
            }
          }
        }
      }
    }

    doctorAvailabilityMap.put("first_slots", firstSlotMap);
    doctorAvailabilityMap.put("return_code", "2001");
    doctorAvailabilityMap.put("return_message", "Success");
    logger.info("sending the response back to the requesting server");
    response.getWriter().write(js.deepSerialize(doctorAvailabilityMap));
    response.flushBuffer();
    return null;
  }
  
  /**
   * Schedule push event.
   *
   * @param appointmentId the appointment id
   * @param eventId the event id
   */
  public void schedulePushEvent(String appointmentId, String eventId) {
    String schema = RequestContext.getSchema();
    Map<String, Object> eventData = new HashMap<>();
    eventData.put("appointment_id", appointmentId);
    eventData.put("schema", schema);
    eventData.put("eventId", eventId);

    Map<String, Object> jobData = new HashMap<>();
    jobData.put("schema", schema);
    jobData.put("eventId", eventId);
    jobData.put("eventData", eventData);
    jobService.scheduleImmediate(
        buildJob("PushEventJob_" + appointmentId, EventListenerJob.class, jobData));
  }
  
  /**
   * Filter visit timings by visit mode.
   *
   * @param resourceAvailability the resource availability
   * @return the list
   */
  public List<BasicDynaBean> filterVisitTimingsByVisitMode(
      List<BasicDynaBean> resourceAvailability) {
    for (BasicDynaBean visitTiming : resourceAvailability) {
      if (visitTiming.get("availability_status").equals("A")) {
        String availabilityMode = (String) visitTiming.get("visit_mode");
        if (availabilityMode.equals("I") || availabilityMode.equals("B")) {
          continue;
        }
        visitTiming.set("availability_status", "N");
      }
    }
    return resourceAvailability;
  }
}
