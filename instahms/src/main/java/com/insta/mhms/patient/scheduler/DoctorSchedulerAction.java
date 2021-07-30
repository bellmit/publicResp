package com.insta.mhms.patient.scheduler;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.messaging.MessageManager;
import com.insta.hms.messaging.MessageUtil;
import com.insta.hms.resourcescheduler.CategoryMasterDAO;
import com.insta.hms.resourcescheduler.ResourceBO;
import com.insta.hms.resourcescheduler.ResourceBO.AppointMentResource;
import com.insta.hms.resourcescheduler.ResourceBO.Appointments;
import com.insta.hms.resourcescheduler.ResourceDAO;
import com.insta.hms.resourcescheduler.ResourceDTO;
import com.insta.instaapi.common.JsonProcessor;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author mithun.saha
 */
public class DoctorSchedulerAction extends DispatchAction {
  static Logger logger = LoggerFactory.getLogger(DoctorSchedulerAction.class);

  /**
   * Get Scheduler master Data.
   *
   * @param mapping mapping parameter
   * @param form form parameter
   * @param request request object
   * @param response response object
   * @return returns action forward
   * @throws SQLException may throw Sql Exception
   * @throws IOException Signals that an I/O exception has occurred
   * @throws NoSuchAlgorithmException may throw NoSuchAlgorithmException
   * @throws ParseException may throw parsing exception
   * @throws ServletException may throw Servlet Exception
   */
  public ActionForward getSchedulerMasterData(
      ActionMapping mapping,
      ActionForm form,
      HttpServletRequest request,
      HttpServletResponse response)
      throws IOException, ServletException, SQLException, NoSuchAlgorithmException, ParseException {

    logger.info("getting scheduler realted parameters getSchedulerMasterData");
    JSONSerializer js = JsonProcessor.getJSONParser();
    Map<String, Object> schedulerMasterDataMap = new HashMap<String, Object>();
    HttpSession session = request.getSession(false);
    String sesHospitalId = (String) session.getAttribute("sesHospitalId");
    String status = request.getParameter("status");
    String successMsg = "";

    if (sesHospitalId == null || "".equals(sesHospitalId)) {
      successMsg = "Session is expired, please login again";
      logger.info("Session is expired, please login again");
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      schedulerMasterDataMap.put("return_code", "1001");
      schedulerMasterDataMap.put("return_message", successMsg);

    } else {
      if (status != null && status.trim().length() > 0 && !status.equalsIgnoreCase("all")) {
        successMsg = "Invalid status parameter";
        logger.info("Invalid status parameter");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        schedulerMasterDataMap.put("return_code", "1021");
        schedulerMasterDataMap.put("return_message", successMsg);
        response.setContentType("application/json");
        response.setHeader("Cache-Control", "no-cache");
        // response.setHeader("Access-Control-Allow-Origin", "*");
        response.getWriter().write(js.deepSerialize(schedulerMasterDataMap));
        response.flushBuffer();
        return null;
      }
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
      List<String> departmentslist = new ArrayList<String>();
      departmentslist.add("dept_id");
      departmentslist.add("dept_name");
      departmentslist.add("cost_center_code");
      departmentslist.add("dept_type_id");
      departmentslist.add("status");

      List<BasicDynaBean> hospitalCenters = null;
      if (status == null || status.trim().length() == 0) {
        hospitalCenters =
            new GenericDAO("hospital_center_master")
                .listAll(hospitalCenterslist, "status", "A", "center_name");
      } else {
        hospitalCenters =
            new GenericDAO("hospital_center_master")
                .listAll(hospitalCenterslist, null, null, "center_name");
      }
      List hospitalList = new ArrayList();
      if (hospitalCenters != null) {
        for (int i = 0; i < hospitalCenters.size(); i++) {
          Map hospitalMap = new HashMap();
          hospitalMap.putAll((Map) ConversionUtils.listBeanToListMap(hospitalCenters).get(i));
          if (hospitalMap.get("created_timestamp") != null) {
            java.sql.Timestamp timestamp =
                (java.sql.Timestamp) hospitalMap.get("created_timestamp");
            hospitalMap.remove("created_timestamp");
            hospitalMap.put("created_timestamp", DateUtil.formatIso8601Timestamp(timestamp));
          }

          if (hospitalMap.get("updated_timestamp") != null) {
            java.sql.Timestamp timestamp =
                (java.sql.Timestamp) hospitalMap.get("updated_timestamp");
            hospitalMap.remove("updated_timestamp");
            hospitalMap.put("updated_timestamp", DateUtil.formatIso8601Timestamp(timestamp));
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
        departments =
            new GenericDAO("department").listAll(departmentslist, "status", "A", "dept_name");
      } else {
        departments =
            new GenericDAO("department").listAll(departmentslist, null, null, "dept_name");
      }
      List<BasicDynaBean> doctors =
          com.insta.mhms.patient.scheduler.ResourceDAO.getCenterWiseAllSchedulableDoctors(
              "doctor_name", status);

      List doctorList = new ArrayList();
      for (int i = 0; i < doctors.size(); i++) {
        Map doctorMap = new HashMap();
        doctorMap.putAll((Map) ConversionUtils.listBeanToListMap(doctors).get(i));
        /*if (doctorMap.get("created_timestamp") != null) {
          java.sql.Timestamp timestamp = (java.sql.Timestamp) doctorMap.get("created_timestamp");
          doctorMap.remove("created_timestamp");
          doctorMap.put("created_timestamp", DateUtil.formatISO8601Timestamp(timestamp));
        }

        if (doctorMap.get("updated_timestamp") != null) {
          java.sql.Timestamp timestamp = (java.sql.Timestamp) doctorMap.get("updated_timestamp");
          doctorMap.remove("updated_timestamp");
          doctorMap.put("updated_timestamp", DateUtil.formatISO8601Timestamp(timestamp));
        }*/
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
      schedulerMasterDataMap.put(
          "hospital_departments", ConversionUtils.listBeanToListMap(departments));
      schedulerMasterDataMap.put("hospital_doctors", doctorList);
      successMsg = "Success";
      schedulerMasterDataMap.put("return_message", successMsg);
      schedulerMasterDataMap.put("return_code", "2001");
      logger.info("getting all scheduler realted data....");
    }

    response.setContentType("application/json");
    response.setHeader("Cache-Control", "no-cache");
    // response.setHeader("Access-Control-Allow-Origin", "*");
    response.getWriter().write(js.deepSerialize(schedulerMasterDataMap));
    response.flushBuffer();
    return null;
  }

  /**
   * Get doctor availability slots.
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
   * @throws ParseException may throw parsing exception
   * @throws Exception throws Generic Exception
   */
  public ActionForward getDoctorAvailabilitySlots(
      ActionMapping mapping,
      ActionForm form,
      HttpServletRequest request,
      HttpServletResponse response)
      throws IOException, ServletException, SQLException, NoSuchAlgorithmException, ParseException,
          Exception {

    Map<String, Object> doctorAvailabilityMap = new HashMap<String, Object>();
    Date date1 = null;
    String successMsg = "";
    response.setContentType("application/json");
    response.setHeader("Cache-Control", "no-cache");
    HttpSession session = request.getSession(false);
    String sesHospitalId = (String) session.getAttribute("sesHospitalId");
    JSONSerializer js = JsonProcessor.getJSONParser();
    if (sesHospitalId == null || "".equals(sesHospitalId)) {
      successMsg = "Session is expired, please login again";
      logger.info("Session is expired, please login again");
      doctorAvailabilityMap.put("return_code", "1001");
      doctorAvailabilityMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.getWriter().write(js.deepSerialize(doctorAvailabilityMap));
      response.flushBuffer();
      return null;
    }
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
    String appointmentDate = request.getParameter("appointment_date");
    if (appointmentDate != null && !appointmentDate.isEmpty()) {
      try {
        date1 = DateUtil.parseIso8601Date(appointmentDate);
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
      date1 = DateUtil.getCurrentDate();
    }
    Date d1 = date1;
    Calendar cal = Calendar.getInstance();
    cal.setTime(date1);
    int dayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) - 1);
    String schedulerType = "DOC";
    List<BasicDynaBean> list =
        ResourceDAO.getResourceListByResourceType(schedulerType, scheduleName);
    List<Time> timeList = new ArrayList<Time>();
    BasicDynaBean doctor = new GenericDAO("doctors").findByKey("doctor_id", scheduleName);
    List<BasicDynaBean> timingList = null;
    int counter = 0;
    Integer centerIdInt = null;
    List<String> timeStrList = new ArrayList<String>();
    String centerId = request.getParameter("center_id");
    if (doctor != null) {
      if (centerId != null && !centerId.equals("")) {
        centerIdInt = Integer.parseInt(centerId);
      }
      while (timeList == null || timeList.size() < 1) {
        timingList =
            getDoctorAvailableTimings(schedulerType, d1, dayOfWeek, scheduleName, centerIdInt);
        if (timingList != null) {
          timeList = getAvailabilitySlots(list, timingList, d1, doctor, centerIdInt);
          if (timeList != null && timeList.size() > 0) {
            Calendar cal1 = Calendar.getInstance();
            cal1.setTime(d1);
            cal1.add(Calendar.DAY_OF_MONTH, -1);
            Date date = new Date(cal1.getTimeInMillis());
            for (int i = 0; i < timeList.size(); i++) {
              Time utc = DateUtil.parseIso8601Time("00:00:00Z");
              Time slot = timeList.get(i);
              if (slot.before(utc)) {
                timeStrList.add(
                    DateUtil.formatIso8601Date(date)
                        + "T"
                        + DateUtil.formatIso8601Time((timeList.get(i))));
              } else {
                timeStrList.add(
                    DateUtil.formatIso8601Date(d1)
                        + "T"
                        + DateUtil.formatIso8601Time((timeList.get(i))));
              }
            }
            break;
          }
        }
        cal.add(Calendar.DATE, 1);
        d1 = new Date(cal.getTimeInMillis());
        dayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) - 1);
        counter++;
        if (counter > 30) {
          successMsg =
              "Doctor is not available at least for a month from "
                  + DateUtil.formatIso8601Date(date1);
          doctorAvailabilityMap.put("return_code", "2001");
          doctorAvailabilityMap.put("return_message", successMsg);
          response.getWriter().write(js.deepSerialize(doctorAvailabilityMap));
          response.flushBuffer();
          return null;
        }
      }
    }
    logger.info("getting doctor availabilities....");
    doctorAvailabilityMap.put("doctor_first_available_date", DateUtil.formatIso8601Date(d1));
    doctorAvailabilityMap.put("doctor_available_times", timeStrList);
    List<Map<String, Object>> timeCenterList = new ArrayList<Map<String, Object>>();
    if (timingList != null) {
      timeCenterList = getAvailabilitySlotsWithCenter(list, timingList, d1, doctor, centerIdInt);
      if (timeCenterList != null && timeCenterList.size() > 0) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(d1);
        cal1.add(Calendar.DAY_OF_MONTH, -1);
        Date date = new Date(cal1.getTimeInMillis());
        if (timeCenterList != null && timeCenterList.size() > 0) {
          for (int i = 0; i < timeCenterList.size(); i++) {
            Time utc = DateUtil.parseIso8601Time("00:00:00Z");
            Map<String, Object> temp = (HashMap) timeCenterList.get(i);
            Time slot = (Time) temp.get("timeslot");
            if (slot.before(utc)) {
              temp.put(
                  "timeslot",
                  DateUtil.formatIso8601Date(date) + "T" + DateUtil.formatIso8601Time(slot));
            } else {
              temp.put(
                  "timeslot",
                  DateUtil.formatIso8601Date(d1) + "T" + DateUtil.formatIso8601Time(slot));
            }
          }
        }
      }
    }
    doctorAvailabilityMap.put("doctor_available_times_with_centers", timeCenterList);
    doctorAvailabilityMap.put("return_code", "2001");
    doctorAvailabilityMap.put("return_message", "Success");
    response.getWriter().write(js.deepSerialize(doctorAvailabilityMap));
    response.flushBuffer();
    return null;
  }

  /**
   * Get doctor availability slots for range.
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
   * @throws ParseException may throw parsing exception
   * @throws Exception throws Generic Exception
   */
  public ActionForward getDoctorAvailabilitySlotsForRange(
      ActionMapping mapping,
      ActionForm form,
      HttpServletRequest request,
      HttpServletResponse response)
      throws IOException, ServletException, SQLException, NoSuchAlgorithmException, ParseException,
          Exception {

    JSONSerializer js = JsonProcessor.getJSONParser();
    Map<String, Object> doctorAvailabilityMap = new HashMap<String, Object>();
    Date dto = null;
    String successMsg = "";
    response.setContentType("application/json");
    response.setHeader("Cache-Control", "no-cache");
    // response.setHeader("Access-Control-Allow-Origin", "*");
    HttpSession session = request.getSession(false);
    String sesHospitalId = (String) session.getAttribute("sesHospitalId");

    if (sesHospitalId == null || "".equals(sesHospitalId)) {
      successMsg = "Session is expired, please login again";
      logger.info("Session is expired, please login again");
      doctorAvailabilityMap.put("return_code", "1001");
      doctorAvailabilityMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.getWriter().write(js.deepSerialize(doctorAvailabilityMap));
      response.flushBuffer();
      return null;
    }
    String fromDate = request.getParameter("from_date");
    String toDate = request.getParameter("to_date");
    String scheduleName = request.getParameter("resource_id");
    String centerId = request.getParameter("center_id");
    if ((scheduleName == null || scheduleName.isEmpty())
        || (fromDate == null || fromDate.isEmpty())) {
      successMsg = "Mandatory fields are not supplied";
      doctorAvailabilityMap.put("return_code", "1002");
      doctorAvailabilityMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(doctorAvailabilityMap));
      response.flushBuffer();
      return null;
    }
    Date date1 = null;
    if (fromDate != null && !fromDate.isEmpty()) {
      try {
        date1 = DateUtil.parseIso8601Date(fromDate);
      } catch (ParseException pe) {
        successMsg = "Invalid from date";
        doctorAvailabilityMap.put("return_code", "1021");
        doctorAvailabilityMap.put("return_message", successMsg);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write(js.deepSerialize(doctorAvailabilityMap));
        response.flushBuffer();
        return null;
      }
    } else {
      date1 = DateUtil.getCurrentDate();
    }
    if (toDate != null && !toDate.isEmpty()) {
      try {
        dto = DateUtil.parseIso8601Date(toDate);
      } catch (ParseException pe) {
        successMsg = "Invalid to date";
        doctorAvailabilityMap.put("return_code", "1021");
        doctorAvailabilityMap.put("return_message", successMsg);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write(js.deepSerialize(doctorAvailabilityMap));
        response.flushBuffer();
        return null;
      }
    } else {
      dto = DateUtil.parseIso8601Date(fromDate);
    }
    long diff = 0;
    long dateTo = 0;
    long dateFrom = 0;
    dateTo = dto.getTime();
    dateFrom = date1.getTime();
    diff = dateTo - dateFrom;
    long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
    if (days < 0 || diff < 0) {
      successMsg = "To date can not be earlier than From date";
      doctorAvailabilityMap.put("return_code", "1022");
      doctorAvailabilityMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(doctorAvailabilityMap));
      response.flushBuffer();
      return null;
    }
    if (days > 29) {
      successMsg = "Duration can not be more than 30 days";
      doctorAvailabilityMap.put("return_code", "1023");
      doctorAvailabilityMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(doctorAvailabilityMap));
      response.flushBuffer();
      return null;
    }
    Map<String, Object> resultMap = new HashMap<String, Object>();
    Date d1 = date1;
    Calendar cal = Calendar.getInstance();
    cal.setTime(date1);
    int dayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) - 1);
    String schedulerType = "DOC";
    List<BasicDynaBean> list =
        ResourceDAO.getResourceListByResourceType(schedulerType, scheduleName);
    List<Time> timeList = new ArrayList<Time>();
    BasicDynaBean doctor = new GenericDAO("doctors").findByKey("doctor_id", scheduleName);
    List<BasicDynaBean> timingList = null;
    int counter = 0;
    if (doctor != null) {
      Integer centerIdInt = null;
      if (centerId != null && !centerId.equals("")) {
        centerIdInt = Integer.parseInt(centerId);
      }
      do {
        timingList =
            getDoctorAvailableTimings(schedulerType, d1, dayOfWeek, scheduleName, centerIdInt);
        if (timingList != null) {
          timeList = getAvailabilitySlots(list, timingList, d1, doctor, centerIdInt);
          if (timeList != null && timeList.size() > 0) {
            Calendar cal1 = Calendar.getInstance();
            cal1.setTime(d1);
            cal1.add(Calendar.DAY_OF_MONTH, -1);
            Date date = new Date(cal1.getTimeInMillis());
            List<String> timeStrList = new ArrayList<String>();
            if (timeList != null && timeList.size() > 0) {
              for (int i = 0; i < timeList.size(); i++) {
                Time utc = DateUtil.parseIso8601Time("00:00:00Z");
                Time slot = timeList.get(i);
                if (slot.before(utc)) {
                  timeStrList.add(
                      DateUtil.formatIso8601Date(date)
                          + "T"
                          + DateUtil.formatIso8601Time((timeList.get(i))));
                } else {
                  timeStrList.add(
                      DateUtil.formatIso8601Date(d1)
                          + "T"
                          + DateUtil.formatIso8601Time((timeList.get(i))));
                }
              }
            }
            resultMap.put(DateUtil.formatIso8601Date(d1), timeStrList);
          } else {
            resultMap.put(DateUtil.formatIso8601Date(d1), "No available slots");
          }
        } else {
          resultMap.put(DateUtil.formatIso8601Date(d1), "No available slots");
        }
        cal.add(Calendar.DATE, 1);
        d1 = new Date(cal.getTimeInMillis());
        dayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) - 1);
      } while (d1.compareTo(dto) <= 0);
    }
    logger.info("getting doctor availabilities....");
    doctorAvailabilityMap.put("doctor_availability_slots", resultMap);
    doctorAvailabilityMap.put("return_code", "2001");
    doctorAvailabilityMap.put("return_message", "Success");
    response.getWriter().write(js.deepSerialize(doctorAvailabilityMap));
    response.flushBuffer();
    return null;
  }

  /**
   * Get availability slots.
   *
   * @param list list parameter
   * @param timingList timing list parameter
   * @param d1 d1 parameter
   * @param doctor doctor parameter
   * @param centerId center id parameter
   * @return returns list
   * @throws Exception may throw Exception
   */
  private List getAvailabilitySlots(
      List<BasicDynaBean> list,
      List<BasicDynaBean> timingList,
      Date d1,
      BasicDynaBean doctor,
      Integer centerId)
      throws Exception {
    List<Time> timeList = new ArrayList<Time>();
    Integer overbookLimit = (Integer) doctor.get("overbook_limit");
    if (list != null && list.size() > 0) {
      BasicDynaBean bean = list.get(0);
      int defaultDuration = (Integer) bean.get("default_duration");
      long slotDuration = (defaultDuration * 60 * 1000);

      if (timingList != null && timingList.size() > 0) {
        for (int i = 0; i < timingList.size(); i++) {
          BasicDynaBean resourceAvailableBean = timingList.get(i);
          Long resourceFromTime = (Long) ((Time) resourceAvailableBean.get("from_time")).getTime();
          Long resourceToTime = (Long) ((Time) resourceAvailableBean.get("to_time")).getTime();
          int resCenterId = (Integer) resourceAvailableBean.get("center_id");
          // filtering the timlist with primary resources max and min time.
          if (centerId == null || centerId == -1 || centerId == 0 || centerId == resCenterId) {
            if (resourceFromTime != null
                && resourceToTime != null
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
    if (doctor != null && overbookLimit != null && overbookLimit == 0) {
      String doctorId = doctor.get("doctor_id").toString();
      BasicDynaBean doctorCategorybean = getResourceCategoryDetails("DOC", doctorId);
      BigDecimal duration = new BigDecimal((Integer) doctorCategorybean.get("default_duration"));
      List<Time> removalList = new ArrayList<Time>();
      List<BasicDynaBean> appointmentList = ResourceDAO.getAppointmentDetails(doctorId, d1);
      for (BasicDynaBean appBean : appointmentList) {
        String status = (String) appBean.get("appointment_status");
        if (status.equalsIgnoreCase("Cancel") || status.equalsIgnoreCase("Noshow")) {
          continue;
        }
        BigDecimal apptDuration = new BigDecimal((Integer) appBean.get("duration"));
        Integer count = 0;
        String apptDateTimeStr = ((Timestamp) appBean.get("appointment_time")).toString();
        String[] apptDateTimeArr = apptDateTimeStr.split(" ");
        Time apptTime = DateUtil.parseTime(apptDateTimeArr[1]);

        if (apptDuration.compareTo(duration) == 1) {
          count = apptDuration.divide(duration).ROUND_CEILING;
        }

        if (count > 0) {
          for (long l = apptTime.getTime();
              l < apptTime.getTime() + (apptDuration.intValue() * 60 * 1000);
              l = l + (duration.intValue() * 60 * 1000)) {
            removalList.add(new java.sql.Time(l));
          }
        } else {
          removalList.add(apptTime);
        }
      }

      if (removalList != null && removalList.size() > 0) {
        timeList.removeAll(removalList);
      }
    }
    return timeList;
  }

  /**
   * Get availability slots woth center.
   *
   * @param list list parameter
   * @param timingList timing list parameter
   * @param d1 d1 parameter
   * @param doctor doctor parameter
   * @param centerId center id parameter
   * @return returns list
   * @throws Exception may throw Exception
   */
  private List getAvailabilitySlotsWithCenter(
      List<BasicDynaBean> list,
      List<BasicDynaBean> timingList,
      Date d1,
      BasicDynaBean doctor,
      Integer centerId)
      throws Exception {
    List<Map<String, Object>> timeSlotsList = new ArrayList<Map<String, Object>>();
    Integer overbookLimit = (Integer) doctor.get("overbook_limit");
    if (list != null && list.size() > 0) {
      BasicDynaBean bean = list.get(0);
      int defaultDuration = (Integer) bean.get("default_duration");
      long slotDuration = (defaultDuration * 60 * 1000);

      if (timingList != null && timingList.size() > 0) {
        for (int i = 0; i < timingList.size(); i++) {
          BasicDynaBean resourceAvailableBean = timingList.get(i);
          Long resourceFromTime = (Long) ((Time) resourceAvailableBean.get("from_time")).getTime();
          Long resourceToTime = (Long) ((Time) resourceAvailableBean.get("to_time")).getTime();
          int resCenterId = (Integer) resourceAvailableBean.get("center_id");
          // filtering the timlist with primary resources max and min time.
          if (centerId == null || centerId == -1 || centerId == 0 || centerId == resCenterId) {
            if (resourceFromTime != null
                && resourceToTime != null
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
    if (doctor != null && overbookLimit != null && overbookLimit == 0) {
      String doctorId = doctor.get("doctor_id").toString();
      BasicDynaBean doctorCategorybean = getResourceCategoryDetails("DOC", doctorId);
      BigDecimal duration = new BigDecimal((Integer) doctorCategorybean.get("default_duration"));
      List<Map<String, Object>> removalList = new ArrayList<Map<String, Object>>();
      List<BasicDynaBean> appointmentList = ResourceDAO.getAppointmentDetails(doctorId, d1);
      for (BasicDynaBean appBean : appointmentList) {
        String status = (String) appBean.get("appointment_status");
        if (status.equalsIgnoreCase("Cancel") || status.equalsIgnoreCase("Noshow")) {
          continue;
        }
        BigDecimal apptDuration = new BigDecimal((Integer) appBean.get("duration"));
        Integer count = 0;
        String apptDateTimeStr = ((Timestamp) appBean.get("appointment_time")).toString();
        String[] apptDateTimeArr = apptDateTimeStr.split(" ");
        Integer appCenterId = (Integer) appBean.get("center_id");
        Time apptTime = DateUtil.parseTime(apptDateTimeArr[1]);

        if (apptDuration.compareTo(duration) == 1) {
          count = apptDuration.divide(duration).ROUND_CEILING;
        }

        if (count > 0) {
          for (long l = apptTime.getTime();
              l < apptTime.getTime() + (apptDuration.intValue() * 60 * 1000);
              l = l + (duration.intValue() * 60 * 1000)) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("timeslot", new java.sql.Time(l));
            map.put("center_id", appCenterId);
            removalList.add(map);
          }
        } else {
          Map<String, Object> map = new HashMap<String, Object>();
          map.put("timeslot", apptTime);
          map.put("center_id", appCenterId);
          removalList.add(map);
        }
      }

      if (removalList != null && removalList.size() > 0) {
        timeSlotsList.removeAll(removalList);
      }
    }
    return timeSlotsList;
  }

  /**
   * Get doctor available slot.
   *
   * @param schedulerType scheduler type parameter
   * @param date date parameter
   * @param dayOfWeek day of week parameter
   * @param scheduleName schedule name parameter
   * @param centerId center id parameter
   * @return returns list
   * @throws Exception may throw Exception
   */
  private List<BasicDynaBean> getDoctorAvailableTimings(
      String schedulerType, Date date, int dayOfWeek, String scheduleName, Integer centerId)
      throws Exception {
    List<BasicDynaBean> timingList = null;
    /*timingList =
        new ResourceDAO().getResourceAvailabilities(schedulerType, date, scheduleName, "A");

    if (timingList != null && timingList.size() < 1) {
      timingList =
          new ResourceDAO()
              .getResourceDefaultAvailabilities(scheduleName, dayOfWeek, schedulerType, "A");
    }

    if (timingList != null && timingList.size() < 1) {
      timingList =
          new ResourceDAO().getResourceDefaultAvailabilities("*", dayOfWeek, schedulerType, "A");
    }*/
    timingList =
        new ResourceDAO()
            .getResourceAvailabilities(schedulerType, date, scheduleName, "A", centerId);
    if (timingList == null || (timingList != null && timingList.size() < 1)) {
      timingList =
          new ResourceDAO()
              .getResourceAvailabilities(schedulerType, date, scheduleName, "N", centerId);
      if (timingList != null && timingList.size() == 1) {
        return null;
      }
    }

    if (timingList == null || (timingList != null && timingList.size() < 1)) {
      timingList =
          new ResourceDAO()
              .getResourceDefaultAvailabilities(
                  scheduleName, dayOfWeek, schedulerType, "A", centerId);
      if (timingList == null || (timingList != null && timingList.size() < 1)) {
        timingList =
            new ResourceDAO()
                .getResourceDefaultAvailabilities(
                    scheduleName, dayOfWeek, schedulerType, "N", centerId);
        if (timingList != null && timingList.size() == 1) {
          return null;
        }
      }
    }

    if (timingList == null || (timingList != null && timingList.size() < 1)) {
      timingList =
          new ResourceDAO()
              .getResourceDefaultAvailabilities("*", dayOfWeek, schedulerType, "A", centerId);
      if (timingList == null || (timingList != null && timingList.size() < 1)) {
        timingList =
            new ResourceDAO()
                .getResourceDefaultAvailabilities("*", dayOfWeek, schedulerType, "N", centerId);
        if (timingList != null && timingList.size() == 1) {
          return null;
        }
      }
    }
    return timingList;
  }

  /**
   * Get Resource category details.
   *
   * @param resourceType resource type parameter
   * @param resourceId resource id parameter
   * @return returns BasicDynaBean
   * @throws Exception may throw Exception
   */
  private BasicDynaBean getResourceCategoryDetails(String resourceType, String resourceId)
      throws Exception {
    BasicDynaBean resourceDetails =
        CategoryMasterDAO.getCategoryDetailsByResourceType(resourceId, resourceType);
    if (resourceDetails == null) {
      resourceDetails = CategoryMasterDAO.getCategoryDetailsByResourceType("*", resourceType);
    }

    return resourceDetails;
  }

  /**
   * Save appointment.
   *
   * @param mapping mapping parameter
   * @param form form parameter
   * @param request request object
   * @param response response object
   * @return returns action forward
   * @throws ServletException may throw Servlet Exception
   * @throws Exception may throw Servlet Exception
   */
  public ActionForward saveAppointment(
      ActionMapping mapping,
      ActionForm form,
      HttpServletRequest request,
      HttpServletResponse response)
      throws ServletException, Exception {

    ArrayList<AppointMentResource> scheduleAppointItemBean = new ArrayList<AppointMentResource>();
    ArrayList<Appointments> scheduleAppointBeanList = new ArrayList<Appointments>();
    ArrayList<AppointMentResource> scheduleAppointItemBeanRecuured =
        new ArrayList<AppointMentResource>();
    JSONSerializer js = JsonProcessor.getJSONParser();
    boolean success = true;
    String msg = "";
    String successMsg = "success";
    String appointmentDateTimeStr = request.getParameter("appointment_date");
    String resourceId = request.getParameter("resource_id");
    String centerId = request.getParameter("center_id");
    String patientName = "";
    String patientId = null;
    String phoneNo = "";
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
    if (resourceId == null
        || resourceId.isEmpty()
        || appointmentDateTimeStr == null
        || appointmentDateTimeStr.isEmpty()
        || mrNo == null
        || mrNo.isEmpty()
        || centerId == null
        || centerId.isEmpty()) {
      successMsg = "Mandatory fields are not supplied";
      responseMap.put("return_code", "1002");
      responseMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(responseMap));
      response.flushBuffer();
      return null;
    }
    Connection con = null;
    con = DataBaseUtil.getConnection();
    List listColumn1 = new ArrayList();
    listColumn1.add("doctor_id");
    List<BasicDynaBean> doctorBean = new GenericDAO("doctors").listAll(listColumn1);
    boolean flag1 = false;
    for (BasicDynaBean bean : doctorBean) {
      String str = (String) bean.get("doctor_id");
      if (resourceId.equals(str)) {
        flag1 = true;
      }
    }
    List listColumn2 = new ArrayList();
    listColumn2.add("center_id");
    List<BasicDynaBean> centerBean = new GenericDAO("hospital_center_master").listAll(listColumn2);
    boolean flag2 = false;

    if (centerId.matches("[0-9]*")) {
      int id = Integer.parseInt(centerId);
      for (BasicDynaBean bean : centerBean) {
        int centId = (Integer) bean.get("center_id");
        if (id == centId) {
          flag2 = true;
        }
      }
    }

    if (!flag1 || !flag2) {
      successMsg = "failed to book the Appointment.";
      responseMap.put("return_code", "1025");
      responseMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      response.getWriter().write(js.deepSerialize(responseMap));
      response.flushBuffer();
      return null;
    }

    Timestamp appointmentTime = null;
    String returnCode = "";
    Integer appointmentId =
        Integer.parseInt(com.insta.hms.resourcescheduler.ResourceDAO.getNextAppointMentId());
    Appointments ap = new Appointments(appointmentId);
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

      BasicDynaBean patDetailsBean = new PatientDetailsDAO().getBean();
      List<AppointMentResource> scheduleAppointItemList = new ArrayList<AppointMentResource>();
      new GenericDAO("patient_details").loadByteaRecords(patDetailsBean, "mr_no", mrNo);
      if (patDetailsBean != null) {
        phoneNo = (String) patDetailsBean.get("patient_phone");
        if (phoneNo == null || phoneNo.isEmpty()) {
          successMsg = "Please update the mobile number before booking an appointment.";
          responseMap.put("return_code", "1028");
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

      BasicDynaBean doctorCategorybean = getResourceCategoryDetails("DOC", resourceId);
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
      // check for duplication on appointment for the same patient.
      int duration = (Integer) doctorCategorybean.get("default_duration");
      long time = appointmentTime.getTime();
      time = time + (duration * 60 * 1000);
      Timestamp endTimestamp = new java.sql.Timestamp(time);
      List<BasicDynaBean> beans =
          ResourceDAO.IsExitsAppointment(appointmentTime, endTimestamp, -1, mrNo, null, null, null);
      if (beans != null && beans.size() > 0) {
        successMsg = "This time slot is already booked for this patient";
        responseMap.put("return_code", "1022");
        responseMap.put("return_message", successMsg);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write(js.deepSerialize(responseMap));
        response.flushBuffer();
        return null;
      }

      List<BasicDynaBean> list =
          ResourceDAO.isSlotBooked(
              appointmentTime, endTimestamp, resourceId, null, resourceId, "OPDOC");
      Integer overbookLimit;
      if (list != null && list.size() > 0) {
        // slot is booked and need to check whether the overbook enabled for the resource.
        overbookLimit = isResourceOverbooked(resourceId);
        int overbookCount = ResourceDAO.getOverbookCount(resourceId, appointmentTime);
        boolean overbook = overbookLimit != null && overbookLimit != 0;
        if (overbook == true && overbookCount > overbookLimit) {
          successMsg =
              "The number of appointments booked for this slot has hit the overbook limit.";
          responseMap.put("return_code", "1029");
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
      // check if resource is available for this time slot
      String[] split = (DateUtil.formatTimestamp(appointmentTime)).split(" ");
      Date date = DateUtil.parseDate(split[0]);
      int centId = Integer.parseInt(centerId);
      if (centId == 0) {
        int maxCenters =
            (Integer) GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default");
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
      boolean available =
          isResourceAvailable(resourceId, appointmentTime, endTimestamp, date, centId);
      if (!available) {
        if (centId != 0) {
          available = isResourceAvailable(resourceId, appointmentTime, endTimestamp, date, 0);
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
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      Integer resOverbooklimit = isResourceOverbooked(resourceId);
      boolean overBookAllowed = resOverbooklimit == null || resOverbooklimit > 0;
      if (overBookAllowed) {
        ap.setUnique_appt_ind(ResourceDAO.getNextUniqueAppointMentInd());
      } else {
        ap.setUnique_appt_ind(0);
      }
      ap.setPrim_res_id(resourceId);
      ap.setAppointmentId(appointmentId);
      ap.setAppointmentDuration((Integer) doctorCategorybean.get("default_duration"));
      ap.setMrNo(mrNo);
      ap.setPatientName(patientName);
      ap.setVisitId(patientId);
      ap.setAppointmentTime(appointmentTime);
      ap.setScheduleName("-1");
      ap.setAppointStatus("Booked");
      ap.setSchedulerVisitType("M");
      ap.setCenterId(Integer.parseInt(centerId));
      ap.setBookedBy(mrNo);
      ap.setBookedTime(DateUtil.getCurrentTimestamp());
      ap.setScheduleId(1);
      ap.setPhoneNo(phoneNo);
      ap.setChangedBy("InstaAPI");
      scheduleAppointBeanList.add(ap);
      AppointMentResource res = null;
      res = new AppointMentResource(appointmentId, "OPDOC", resourceId);
      res.setAppointment_item_id(new GenericDAO("scheduler_appointment_items").getNextSequence());
      res.setUser_name("InstaAPI");
      java.sql.Timestamp modTime = DataBaseUtil.getDateandTime();
      res.setMod_time(modTime);
      scheduleAppointItemBean.add(res);
      success =
          com.insta.hms.resourcescheduler.ResourceBO.saveAppointmentAndresources(
              con,
              scheduleAppointBeanList,
              scheduleAppointItemBean,
              scheduleAppointItemBeanRecuured);
      logger.info("appointment booked.");
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
    } catch (Exception exception) {
      success = false;
    } finally {
      DataBaseUtil.commitClose(con, success);
    }

    if (success) {
      if (null != ap.getAppointStatus()
          && ap.getAppointStatus().equalsIgnoreCase("Booked")
          && MessageUtil.allowMessageNotification(request, "scheduler_message_send")) {
        MessageManager mgr = new MessageManager();
        Map appointmentData = new HashMap();
        appointmentData.put("appointment_id", ap.getAppointmentId());
        appointmentData.put("status", ap.getAppointStatus());
        mgr.processEvent("appointment_booked", appointmentData, false);
      }
      msg = "Success";
      returnCode = "2001";
      responseMap.put("appointment_id", ap.getAppointmentId());
    }

    if (!success) {
      successMsg = "failure";
      msg = "failed to book the Appointment.";
      returnCode = "1025";
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
    logger.info("saving appointments");

    responseMap.put("return_code", returnCode);
    responseMap.put("return_message", msg);
    // responseMap.put("result", successMsg);
    response.setContentType("application/json");
    response.setHeader("Cache-Control", "no-cache");
    // response.setHeader("Access-Control-Allow-Origin", "*");
    response.getWriter().write(js.deepSerialize(responseMap));
    response.flushBuffer();
    return null;
  }

  /**
   * Check if resource is schedulable.
   *
   * @param resourceId resource id parameter
   * @return returns true or false
   * @throws SQLException may throw Sql Exception
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
   * Check if resource is overbooked.
   *
   * @param resourceId resource id parameter
   * @return returns true or false
   * @throws SQLException may throw Sql Exception
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
   * check if resource is available.
   *
   * @param resourceId resource id parameter
   * @param startAppointmentTime appointment start time
   * @param endAppointmentTime appointment end time
   * @param date date
   * @param centerId center id
   * @return returns true or false
   * @throws Exception may throw Generic exception
   */
  private boolean isResourceAvailable(
      String resourceId,
      Timestamp startAppointmentTime,
      Timestamp endAppointmentTime,
      Date date,
      Integer centerId)
      throws Exception {

    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    int dayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) - 1);
    List resourceAvailList =
        getDoctorAvailableTimings("DOC", date, dayOfWeek, resourceId, centerId);
    BasicDynaBean resourceBaen = null;
    Timestamp startAvailTime = null;
    Timestamp endAvailTime = null;
    boolean resAvailable = false;
    if (resourceAvailList != null) {
      for (int j = 0; j < resourceAvailList.size(); j++) {
        resourceBaen = (BasicDynaBean) resourceAvailList.get(j);
        int resCenterId = (Integer) resourceBaen.get("center_id");
        if (resourceBaen.get("availability_status").equals("A") && resCenterId == centerId) {
          Time fromTime = (java.sql.Time) resourceBaen.get("from_time");
          SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
          String fromTime1 = sdf.format(fromTime);
          startAvailTime = DateUtil.parseTimestamp(DateUtil.formatDate(date), fromTime1);
          Time toTime = (java.sql.Time) resourceBaen.get("to_time");
          String toTime1 = sdf.format(toTime);
          endAvailTime = DateUtil.parseTimestamp(DateUtil.formatDate(date), toTime1);
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
   * Get appointment success message.
   *
   * @param mapping mapping parameter
   * @param form form parameter
   * @param request request object
   * @param response response object
   * @return returns action forward
   * @throws ServletException may throw Servlet Exception
   * @throws Exception may throw Exception
   */
  public ActionForward getAppointmentSuccessMsg(
      ActionMapping mapping,
      ActionForm form,
      HttpServletRequest request,
      HttpServletResponse response)
      throws ServletException, Exception {
    JSONSerializer js = JsonProcessor.getJSONParser();
    HttpSession session = request.getSession(false);
    String msg = (String) session.getAttribute("appointment_success_message");
    response.setContentType("application/json");
    response.setHeader("Cache-Control", "no-cache");
    // response.setHeader("Access-Control-Allow-Origin", "*");
    response.getWriter().write(js.serialize(msg));
    response.flushBuffer();
    return null;
  }

  /**
   * Get duplicate appointment details.
   *
   * @param mapping mapping parameter
   * @param form form parameter
   * @param request request object
   * @param response response object
   * @return returns action forward
   * @throws ServletException may throw Servlet Exception
   * @throws SQLException may throw Sql Exception
   * @throws IOException Signals that an I/O exception has occurred
   * @throws Exception may throw Exception
   */
  public ActionForward getDuplicateAppointmentDetails(
      ActionMapping mapping,
      ActionForm form,
      HttpServletRequest request,
      HttpServletResponse response)
      throws ServletException, SQLException, IOException, Exception {

    Map patAppInfo = new HashMap();
    String mrno = (String) RequestContext.getSession().getAttribute("mobile_user_id");
    String resSchName = null;
    Integer resSchId = null;
    logger.info("getting duplicate appointmnets");
    String successMsg = "";
    String returnCode = "";
    JSONSerializer js = JsonProcessor.getJSONParser();
    Map<String, String> responseMap = new HashMap<String, String>();
    // response.setHeader("Access-Control-Allow-Origin", "*");
    String appointmentDate = request.getParameter("appointment_date");
    // String appointmentTime = request.getParameter("appointment_time");
    Timestamp startAppointmentDateTime = null;
    try {
      startAppointmentDateTime = DateUtil.parseIso8601Timestamp(appointmentDate);
      if (startAppointmentDateTime == null) {
        successMsg = "Invalid appointment date";
        logger.info("Invalid appointment date");
        logger.info("sending the response back to the requesting server");
        responseMap.put("return_code", "1001");
        responseMap.put("return_message", successMsg);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write(js.deepSerialize(responseMap));
        response.flushBuffer();
        return null;
      }
    } catch (ParseException pe) {
      successMsg = "Invalid appointment date";
      logger.info("Invalid appointment date");
      logger.info("sending the response back to the requesting server");
      responseMap.put("return_code", "1001");
      responseMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(responseMap));
      response.flushBuffer();
      return null;
    }
    String resourceId = request.getParameter("resource_id");
    String appointmentDateTimeStr = null;

    Timestamp endAppointmentDateTime = null;
    BasicDynaBean doctorCategorybean = getResourceCategoryDetails("DOC", resourceId);
    if (doctorCategorybean != null) {
      Integer apptDuration = (Integer) doctorCategorybean.get("default_duration");
      endAppointmentDateTime =
          new java.sql.Timestamp(startAppointmentDateTime.getTime() + apptDuration * 60 * 1000);
    }
    BasicDynaBean bean = null;
    String responseContent = "false";
    int apptId = -1;
    Map appInfo = null;
    if (startAppointmentDateTime != null && endAppointmentDateTime != null) {
      List<BasicDynaBean> appsList =
          com.insta.hms.resourcescheduler.ResourceDAO.IsExitsAppointment(
              startAppointmentDateTime, endAppointmentDateTime, apptId, mrno, null, null, null);
      bean = (appsList != null && appsList.size() > 0) ? appsList.get(0) : null;
    } else {
      bean = null;
    }

    if (bean != null) {
      appInfo = new HashMap(bean.getMap());
      resSchName = (String) bean.get("res_sch_name");
      resSchId = (Integer) bean.get("res_sch_id");
      String resourceName =
          com.insta.hms.resourcescheduler.ResourceDAO.getResourceName(resSchId, resSchName);
      patAppInfo.put("is_duplicate_appointment", "true");
      patAppInfo.put("resource_name", resourceName);
    } else {
      patAppInfo.put("is_duplicate_appointment", "false");
      patAppInfo.put("resource_name", null);
    }

    response.setContentType("application/x-json");
    response.setHeader("Cache-Control", "no-cache");
    // response.setHeader("Access-Control-Allow-Origin", "*");
    response.getWriter().write(js.deepSerialize(patAppInfo));
    response.flushBuffer();

    return null;
  }

  /**
   * cancel the patient Appointment.
   *
   * @param mapping mapping parameter
   * @param form form parameter
   * @param request request object
   * @param response response object
   * @return returns action forward
   * @throws ServletException may throw Servlet Exception
   * @throws Exception may throw Exception
   */
  public ActionForward cancelAppointment(
      ActionMapping mapping,
      ActionForm form,
      HttpServletRequest request,
      HttpServletResponse response)
      throws ServletException, Exception {
    logger.info("getting scheduler realted parameter cancelAppointment");
    // String requestHandalerKey = request.getParameter("request_handler_key");
    HttpSession session = request.getSession(false);
    String mrNo = (String) session.getAttribute("mobile_user_id");
    String sesHospitalId = (String) session.getAttribute("sesHospitalId");
    String successMsg = "success";
    Connection con = null;
    JSONSerializer js = JsonProcessor.getJSONParser();
    Map<String, Object> responseMap = new HashMap<String, Object>();

    logger.info("getting session related data from conetxt" + responseMap);
    boolean isAValidRequest = false;
    if (mrNo != null && !mrNo.equals("")) {
      isAValidRequest = true;
    }
    response.setContentType("application/json");
    response.setHeader("Cache-Control", "no-cache");
    // response.setHeader("Access-Control-Allow-Origin", "*");
    String returnCode = "";

    if (!isAValidRequest) {
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
    String appointmentIdStr = request.getParameter("appointment_id");
    String cancelReason = request.getParameter("cancel_reason");
    if (appointmentIdStr == null
        || appointmentIdStr.isEmpty()
        || cancelReason == null
        || cancelReason.isEmpty()
        || mrNo == null
        || mrNo.isEmpty()) {
      successMsg = "Mandatory fields are not supplied";
      responseMap.put("return_code", "1002");
      responseMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(responseMap));
      response.flushBuffer();
      return null;
    }
    // HttpSession session= SessionUtil.getSession(requestHandalerKey, ctx);
    boolean success = true;

    // Check the rights for Doctor Scheduler screen
    /* boolean isScreenRights =
        ScreenRights.getScreenRights(requestHandalerKey, ctx, "doctorScheduler");
    if (!isScreenRights) {
      successMsg = "Permission Denied. Please check with Administrator.";
      responseMap.put("return_code", "1003");
      responseMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(responseMap));
      response.flushBuffer();
      return null;
    } */

    /* if ((appointmentIdStr == null || appointmentIdStr.isEmpty())
        || (cancelReason == null || cancelReason.isEmpty())) {
      successMsg = "Mandatory fields are not supplied";
      responseMap.put("return_code", "1002");
      responseMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(responseMap));
      response.flushBuffer();
      return null;
    }*/

    Integer appointmentId = 0;

    try {
      appointmentId =
          (appointmentIdStr != null && !appointmentIdStr.isEmpty())
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
        // con = com.insta.instaapi.common.DataBaseUtil.getConnection(
        // (String)sessionParameters.get("hospital_name"));
        // logger.info("getting connection
        // object"+con+"----"+(String)session.getAttribute("hospital_name"));
        con = com.insta.instaapi.common.DbUtil.getConnection(sesHospitalId);
        con.setAutoCommit(false);
        List<BasicDynaBean> apptBean =
            new GenericDAO("scheduler_appointments")
                .listAll(con, null, "appointment_id", appointmentId, null);
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
        /*Map<String,Object> columndata = new HashMap<String, Object>();
        columndata.put("appointment_status", "Cancel");
        columndata.put("changed_by", mr_no);
        columndata.put("changed_time", DateUtil.getCurrentTimestamp());
        columndata.put("cancel_reason", cancelReason);
        columndata.put("appointment_id", appointmentId);*/
        BasicDynaBean bean = apptBean.get(0);
        Integer uniqueApptInd = null;
        if (((Integer) bean.get("unique_appt_ind")) == 0) {
          uniqueApptInd = ResourceDAO.getNextUniqueAppointMentInd();
        } else {
          uniqueApptInd = (Integer) bean.get("unique_appt_ind");
        }
        java.sql.Timestamp modTime = DataBaseUtil.getDateandTime();
        success =
            com.insta.hms.resourcescheduler.ResourceBO.updateAppointments(
                con, appointmentId, "Cancel", cancelReason, mrNo, uniqueApptInd, modTime);
      } finally {
        com.bob.hms.common.DataBaseUtil.commitClose(con, success);
      }
    }

    if (!success) {
      successMsg = "failed to cancel the Appointment.";
      returnCode = "1021";
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    } else {
      successMsg = "Success";
      returnCode = "2001";
    }
    responseMap.put("return_code", returnCode);
    responseMap.put("return_message", successMsg);
    logger.info("cancelling appointments");
    logger.info("sending the response back to the requesting server");
    response.getWriter().write(js.deepSerialize(responseMap));
    response.flushBuffer();
    return null;
  }

  /**
   * reschedule or update the Appointment.
   *
   * @param mapping mapping parameter
   * @param form form parameter
   * @param request request object
   * @param response response object
   * @return returns action forward
   * @throws ServletException may throw Servlet Exception
   * @throws Exception may throw Exception
   */
  public ActionForward updateAppointment(
      ActionMapping mapping,
      ActionForm form,
      HttpServletRequest request,
      HttpServletResponse response)
      throws ServletException, Exception {
    logger.info("getting scheduler realted parameters updateAppointment");
    HttpSession session = request.getSession(false);
    String mobileUserId = (String) session.getAttribute("mobile_user_id");
    Connection con = null;
    JSONSerializer js = JsonProcessor.getJSONParser();
    Map<String, Object> responseMap = new HashMap<String, Object>();

    boolean isAValidRequest = false;
    String returnCode = "";
    response.setContentType("application/json");
    response.setHeader("Cache-Control", "no-cache");
    if (mobileUserId != null && !mobileUserId.equals("")) {
      isAValidRequest = true;
    }

    boolean success = true;
    String successMsg = "Success";
    String appointmentDateTimeStr = request.getParameter("appointment_date");
    String appointmentIdStr = request.getParameter("appointment_id");
    String resourceId = request.getParameter("resource_id");
    String centerId = request.getParameter("center_id");
    String sesHospitalId = (String) session.getAttribute("sesHospitalId");
    String primaryResId = null;

    if (!isAValidRequest) {
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
      logger.info("failed to update the Appointment.");
      responseMap.put("return_code", "1025");
      responseMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      response.getWriter().write(js.deepSerialize(responseMap));
      response.flushBuffer();
      return null;
    }

    if (isAValidRequest) {
      con = com.insta.instaapi.common.DbUtil.getConnection(sesHospitalId);
      con.setAutoCommit(false);
      Map filterMap = new HashMap();
      filterMap.put("appointment_id", Integer.parseInt(appointmentIdStr));
      List<BasicDynaBean> schdulerBean =
          new GenericDAO("scheduler_appointments")
              .listAll(
                  con,
                  Arrays.asList(
                      new String[] {
                        "mr_no",
                        "duration",
                        "appointment_status",
                        "center_id",
                        "prim_res_id",
                        "appointment_time"
                      }),
                  filterMap,
                  null);
      BasicDynaBean apptBean = null;
      if (schdulerBean == null || schdulerBean.size() == 0) {
        successMsg = "failed to update the Appointment.";
        logger.info("failed to update the Appointment.");
        responseMap.put("return_code", "1025");
        responseMap.put("return_message", successMsg);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.getWriter().write(js.deepSerialize(responseMap));
        response.flushBuffer();
        return null;
      } else {
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
      }

      Timestamp appointmentTime = null;
      Map fields = new HashMap();
      Map keys = new HashMap();
      try {
        if (appointmentDateTimeStr == null || appointmentDateTimeStr.isEmpty()) {
          appointmentTime = (java.sql.Timestamp) apptBean.get("appointment_time");
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
        con.setAutoCommit(false);
        String mrNo = (String) apptBean.get("mr_no");
        int duration = (Integer) apptBean.get("duration");
        long count = 0;
        java.sql.Timestamp modTime = com.insta.instaapi.common.DbUtil.getDateandTime(con);
        if (schedule) {
          count = appointmentTime.getTime();

          java.sql.Timestamp existingTime = (java.sql.Timestamp) apptBean.get("appointment_time");
          boolean duplicate = false;

          int centerId1 = 0;
          if (centerId == null || centerId.isEmpty()) {
            centerId1 = (Integer) apptBean.get("center_id");
          } else {
            if (!centerId.matches("[0-9]*")) {
              successMsg = "failed to update the Appointment.";
              logger.info("failed to update the Appointment.");
              responseMap.put("return_code", "1025");
              responseMap.put("return_message", successMsg);
              response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
              response.getWriter().write(js.deepSerialize(responseMap));
              response.flushBuffer();
              return null;
            }
            centerId1 = Integer.parseInt(centerId);
            if (centerId1 == 0) {
              int maxCenters =
                  (Integer) GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default");
              if (maxCenters > 0) {
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
          if (count == existingTime.getTime()) {
            timeChanged = false;
            int existid = (Integer) apptBean.get("center_id");

            if (centerId1 == existid && resourceId.equals((String) apptBean.get("prim_res_id"))) {
              duplicate = true;
            }
          }

          if (duplicate) {
            responseMap.put("return_code", "2001");
            responseMap.put("return_message", "Success");
            logger.info("updating appointments");
            logger.info("sending the response back to the requesting server");
            response.getWriter().write(js.deepSerialize(responseMap));
            response.flushBuffer();
            return null;
          }
          long ll = count + (duration * 60 * 1000);
          Timestamp endTimestamp = new java.sql.Timestamp(ll);

          // check for duplication on appointment for the same patient.
          if (timeChanged) {

            List<BasicDynaBean> beans =
                com.insta.hms.resourcescheduler.ResourceDAO.IsExitsAppointment(
                    appointmentTime, endTimestamp, -1, mrNo, null, null, null);
            if (beans != null && beans.size() > 0) {
              successMsg = "This time slot is already booked for this patient";
              responseMap.put("return_code", "1022");
              responseMap.put("return_message", successMsg);
              response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
              response.getWriter().write(js.deepSerialize(responseMap));
              response.flushBuffer();
              return null;
            }
            List<BasicDynaBean> list =
                com.insta.hms.resourcescheduler.ResourceDAO.isSlotBooked(
                    appointmentTime,
                    endTimestamp,
                    resourceId,
                    appointmentIdStr,
                    resourceId,
                    "OPDOC");
            Integer overbookLimit;
            if (list != null && list.size() > 0) {
              // slot is booked and need to check whether the overbook enabled for the resource.
              overbookLimit = isResourceOverbooked(resourceId);
              int overbookCount = ResourceDAO.getOverbookCount(resourceId, appointmentTime);
              boolean overbook = overbookLimit != null && overbookLimit != 0;
              if (overbook == true && overbookCount > overbookLimit) {
                successMsg =
                    "The number of appointments booked for this slot has hit the overbook limit.";
                responseMap.put("return_code", "1030");
                responseMap.put("return_message", successMsg);
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(js.deepSerialize(responseMap));
                response.flushBuffer();
                return null;
              }
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
          String[] split = (DateUtil.formatTimestamp(appointmentTime)).split(" ");
          Date parseDate = DateUtil.parseDate(split[0]);
          boolean available =
              isResourceAvailable(
                  resourceId, appointmentTime, endTimestamp, parseDate, new Integer(centerId1));
          if (!available) {
            if (centerId1 != 0) {
              available = isResourceAvailable(resourceId, appointmentTime, endTimestamp,
                  parseDate, 0);
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
          Integer resOverbooklimit = isResourceOverbooked(resourceId);
          boolean overBookAllowed = resOverbooklimit == null || resOverbooklimit > 0;
          if (overBookAllowed) {
            fields.put("unique_appt_ind", ResourceDAO.getNextUniqueAppointMentInd());
          } else {
            fields.put("unique_appt_ind", 0);
          }
          fields.put("prim_res_id", resourceId);
          fields.put("appointment_time", appointmentTime);
          fields.put("res_sch_name", "-1");
          fields.put("center_id", centerId1);
          fields.put("res_sch_id", 1);
          fields.put("duration", duration);
          fields.put("changed_by", "InstaAPI");
          fields.put("changed_time", modTime);
          fields.put("booked_by", "InstaAPI");
          keys.put("appointment_id", Integer.parseInt(appointmentIdStr));
        } else {
          successMsg = "The resource is not schedulable";
          responseMap.put("return_code", "1021");
          responseMap.put("return_message", successMsg);
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          response.getWriter().write(js.deepSerialize(responseMap));
          response.flushBuffer();
          return null;
        }
        success = com.insta.hms.resourcescheduler.ResourceBO.updateAppointments(con, fields, keys);
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
          List<ResourceDTO> scheduleAppointItemInsertList = new ArrayList<ResourceDTO>();
          List<ResourceDTO> scheduleAppointItemUpdateList = new ArrayList<ResourceDTO>();
          List<ResourceDTO> scheduleAppointItemDeleteList = new ArrayList<ResourceDTO>();
          if (bean != null) {
            rdto.setAppointment_item_id((Integer) bean.get("appointment_item_id"));
          }
          scheduleAppointItemUpdateList.add(rdto);

          success =
              new ResourceBO()
                  .updateSchedulerResourceDetails(
                      con,
                      scheduleAppointItemInsertList,
                      scheduleAppointItemUpdateList,
                      scheduleAppointItemDeleteList);
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
      } catch (Exception exception) {
        success = false;
      } finally {
        com.bob.hms.common.DataBaseUtil.commitClose(con, success);
      }
    }

    if (success) {
      successMsg = "Success";
      returnCode = "2001";
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
   * Get patient appointment details.
   *
   * @param mapping mapping parameter
   * @param form form parameter
   * @param request request object
   * @param response response object
   * @return returns action forward
   * @throws ServletException may throw Servlet Exception
   * @throws SQLException may throw Sql Exception
   * @throws IOException Signals that an I/O exception has occurred
   */
  public ActionForm getPatientAppointmentDetails(
      ActionMapping mapping,
      ActionForm form,
      HttpServletRequest request,
      HttpServletResponse response)
      throws IOException, ServletException, SQLException {
    logger.info("getting patient appointment related  data");
    HttpSession session = request.getSession(false);
    String successMsg = "";
    String returnCode = "";
    Map<String, Object> patientDetailssuccesssMap = new HashMap<String, Object>();
    JSONSerializer js = JsonProcessor.getJSONParser();
    String mrNo = (String) session.getAttribute("userid");
    Connection con = null;

    if (mrNo == null || "".equals(mrNo)) {
      successMsg = "Session is expired, please login again";
      logger.info("Session is expired, please login again");
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      patientDetailssuccesssMap.put("return_code", "1001");
      patientDetailssuccesssMap.put("return_message", successMsg);
      response.setContentType("application/json");
      response.setHeader("Cache-Control", "no-cache");
      // response.setHeader("Access-Control-Allow-Origin", "*");
      response.getWriter().write(js.deepSerialize(patientDetailssuccesssMap));
      response.flushBuffer();
      return null;
    } else {

      try {
        Map appointmentTypeMap = new HashMap();
        appointmentTypeMap.put(1, "Consultation");
        appointmentTypeMap.put(2, "Surgery");
        appointmentTypeMap.put(3, "Service");
        appointmentTypeMap.put(4, "Test");
        con = DataBaseUtil.getConnection();
        logger.info(
            "getting connection object"
                + con
                + "----"
                + (String) session.getAttribute("sesHospitalId"));
        GenericDAO dao = new GenericDAO("scheduler_appointments");
        List<BasicDynaBean> patientappoientmentdetails = dao.findAllByKey(con, "mr_no", mrNo);

        List listMap = new ArrayList();
        if (patientappoientmentdetails != null && patientappoientmentdetails.size() > 0) {
          List list = ConversionUtils.listBeanToListMap(patientappoientmentdetails);
          for (int k = 0; k < list.size(); k++) {

            Map<String, Object> patientDetailsSuccessMap = new HashMap<String, Object>();
            patientDetailsSuccessMap.putAll((Map) list.get(k));

            if (patientDetailsSuccessMap.get("appointment_time") != null) {
              java.sql.Timestamp timestamp =
                  (java.sql.Timestamp) patientDetailsSuccessMap.get("appointment_time");
              patientDetailsSuccessMap.remove("appointment_time");
              patientDetailsSuccessMap.put("appointment_time", DateUtil
                  .formatIso8601Timestamp(timestamp));
            }
            if (patientDetailsSuccessMap.get("arrival_time") != null) {
              java.sql.Timestamp timestamp =
                  (java.sql.Timestamp) patientDetailsSuccessMap.get("arrival_time");
              patientDetailsSuccessMap.remove("arrival_time");
              patientDetailsSuccessMap.put("arrival_time", DateUtil
                  .formatIso8601Timestamp(timestamp));
            }
            if (patientDetailsSuccessMap.get("completed_time") != null) {
              java.sql.Timestamp timestamp =
                  (java.sql.Timestamp) patientDetailsSuccessMap.get("completed_time");
              patientDetailsSuccessMap.remove("completed_time");
              patientDetailsSuccessMap.put("completed_time", DateUtil
                  .formatIso8601Timestamp(timestamp));
            }
            if (patientDetailsSuccessMap.get("booked_time") != null) {
              java.sql.Timestamp timestamp =
                  (java.sql.Timestamp) patientDetailsSuccessMap.get("booked_time");
              patientDetailsSuccessMap.remove("booked_time");
              patientDetailsSuccessMap.put("booked_time", DateUtil
                  .formatIso8601Timestamp(timestamp));
            }
            if (patientDetailsSuccessMap.get("changed_time") != null) {
              java.sql.Timestamp timestamp =
                  (java.sql.Timestamp) patientDetailsSuccessMap.get("changed_time");
              patientDetailsSuccessMap.remove("changed_time");
              patientDetailsSuccessMap.put("changed_time", DateUtil
                  .formatIso8601Timestamp(timestamp));
            }
            if (patientDetailsSuccessMap.get("orig_appt_time") != null) {
              java.sql.Timestamp timestamp =
                  (java.sql.Timestamp) patientDetailsSuccessMap.get("orig_appt_time");
              patientDetailsSuccessMap.remove("orig_appt_time");
              patientDetailsSuccessMap.put("orig_appt_time", DateUtil
                  .formatIso8601Timestamp(timestamp));
            }
            List<BasicDynaBean> docName =
                new GenericDAO("doctors")
                    .listAll(
                        null,
                        Arrays.asList(new String[] {"doctor_name"}),
                        "doctor_id",
                        (String) patientDetailsSuccessMap.get("prim_res_id"),
                        null);
            if (docName != null && !docName.isEmpty()) {
              patientDetailsSuccessMap.put(
                  "doctor_name",
                  ((Map) ConversionUtils.listBeanToListMap(docName).get(0)).get("doctor_name"));
            } else {
              patientDetailsSuccessMap.put("doctor_name", "");
            }
            List<BasicDynaBean> centerName =
                new GenericDAO("hospital_center_master")
                    .listAll(
                        null,
                        Arrays.asList(new String[] {"center_name"}),
                        "center_id",
                        (Integer) patientDetailsSuccessMap.get("center_id"),
                        null);
            if (centerName != null && !centerName.isEmpty()) {
              patientDetailsSuccessMap.put(
                  "center_name",
                  ((Map) ConversionUtils.listBeanToListMap(centerName).get(0)).get("center_name"));
            } else {
              patientDetailsSuccessMap.put("center_name", "");
            }

            patientDetailsSuccessMap.put(
                "appointment_type",
                appointmentTypeMap.get(patientDetailsSuccessMap.get("res_sch_id")));

            listMap.add(patientDetailsSuccessMap);
          }
        }
        patientDetailssuccesssMap.put("patient_appointment_details", listMap);
      } finally {
        DataBaseUtil.closeConnections(con, null);
      }
    }

    response.setContentType("application/json");
    response.setHeader("Cache-Control", "no-cache");
    // response.setHeader("Access-Control-Allow-Origin", "*");
    logger.info("sending the response back to the requesting session");
    patientDetailssuccesssMap.put("return_code", "2001");
    patientDetailssuccesssMap.put("return_message", "Success");
    response.getWriter().write(js.deepSerialize(patientDetailssuccesssMap));
    response.flushBuffer();
    return null;
  }
}
