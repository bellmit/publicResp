package com.insta.hms.core.scheduler;

import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.exception.HMSException;
import com.insta.hms.exception.NestableValidationException;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.master.ServiceMaster.ServiceMasterDAO;
import com.insta.hms.mdm.resourceoverride.ResourceOverrideService;
import com.insta.hms.redis.RedisMessagePublisher;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class SchedulerService.
 */
@Service
public class SchedulerService {

  /** The logger. */
  private Logger logger = LoggerFactory.getLogger(this.getClass());

  /** The generic preferences service. */
  @LazyAutowired
  private GenericPreferencesService genericPreferencesService;
  
  /** The appointment category factory. */
  @LazyAutowired
  private AppointmentCategoryFactory appointmentCategoryFactory;
  
  /** The appointment service. */
  @LazyAutowired
  private AppointmentService appointmentService;
  
  /** The resource override service. */
  @LazyAutowired
  private ResourceOverrideService resourceOverrideService;
  
  /** The appointment validator. */
  @LazyAutowired
  private AppointmentValidator appointmentValidator;
  
  /** The scheduler ACL. */
  @LazyAutowired
  private SchedulerACL schedulerACL;
  
  /** The redis message publisher. */
  @LazyAutowired
  private RedisMessagePublisher redisMessagePublisher;

  /** Scheduler Bulk appointment service. **/
  @LazyAutowired
  private SchedulerBulkAppointmentsService schedulerBulkAppointmentsService;

  /** Appointment Repository. **/
  @LazyAutowired
  private AppointmentRepository appointmentRepository;

  /**
   * Gets the secondary resources schedule.
   *
   * @param params the params
   * @return the secondary resources schedule
   */
  public Map<String, List<Object>> getSecondaryResourcesSchedule(Map<String, String[]> params) {
    // get the category object associated with resource category
    ValidationErrorMap validationErrors = new ValidationErrorMap();
    Map<String, Object> nestedException = new HashMap<String, Object>();
    if (!appointmentValidator.validatSecondaryResourcesScheduleParams(params, validationErrors)) {
      ValidationException ex = new ValidationException(validationErrors);
      nestedException.put("resources", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }

    AppointmentCategory appointmentCategory = getAppCategory(params.get("res_sch_category")[0]);
    Map<String, String[]> newParams = new HashMap<String, String[]>(params);
    Integer centerId = RequestContext.getCenterId();
    Integer centersIncDefault = (Integer) genericPreferencesService.getAllPreferences()
        .get("max_centers_inc_default");
    newParams.put("center_id", new String[] { centerId.toString() });
    newParams.putAll(params);
    newParams.put("max_centers_inc_default", new String[] { centersIncDefault.toString() });
    // fetch appointments
    List<BasicDynaBean> appointments = appointmentService.getAppointments(appointmentCategory,
        newParams);
    // fetch overrides
    String[] resourceIds = newParams.get("resources");
    Date startDate = null;
    Date endDate = null;
    try {
      startDate = DateUtil.parseDate(newParams.get("start_date")[0]);
      endDate = DateUtil.parseDate(newParams.get("end_date")[0]);
    } catch (ParseException pe) {
      logger.error("start date or end date is invalid!");
    }
    List<BasicDynaBean> overrides = resourceOverrideService.getResourceOverrides(
        Arrays.asList(resourceIds), appointmentCategory.getCategory(), startDate, endDate, null,
        null);
    // center applicability for overrides
    if (overrides != null && overrides.size() > 0 && centersIncDefault > 1) {
      overrides = appointmentCategory.filterVisitTimingsByCenter(overrides, centerId);
    }
    /*
     * for (BasicDynaBean override: overrides) { if (override.get("availability_status").equals("A")
     * && centersIncDefault != 1 && !Arrays.asList(0, centerId).contains(override.get("center_id")))
     * { // The available slot is considered as unavailable for the // given center, so we mark as
     * 'N' override.set("availability_status", "N"); } }
     */
    Map<String, List<Object>> appointmentsMap = formatOverridesAppointmentsListBean(appointments);
    Map<String, List<Object>> overridesMap = formatOverridesAppointmentsListBean(overrides);
    List<Object> list = new ArrayList<Object>();
    for (String resourceId : resourceIds) {
      Map<String, Object> resourceCalender = new HashMap<String, Object>();
      resourceCalender.put("resource_id", resourceId);
      resourceCalender.put("appointments",
          appointmentsMap.get(resourceId) != null ? appointmentsMap.get(resourceId)
              : Collections.emptyList());
      resourceCalender.put("overrides",
          overridesMap.get(resourceId) != null ? overridesMap.get(resourceId)
              : Collections.emptyList());
      list.add(resourceCalender);

    }
    return Collections.singletonMap("resources_calender", list);
  }

  /**
   * Format overrides appointments list bean.
   *
   * @param list the list
   * @return the map
   */
  private Map<String, List<Object>> formatOverridesAppointmentsListBean(List<BasicDynaBean> list) {
    Map<String, List<Object>> map = new HashMap<String, List<Object>>();
    for (BasicDynaBean bean : list) {
      String resourceId = (String) bean.get("resource_id");
      if (map.containsKey(resourceId)) {
        map.get(resourceId).add(bean.getMap());
      } else {
        map.put(resourceId, new ArrayList<Object>(Arrays.asList(bean.getMap())));
      }
    }
    return map;

  }

  /**
   * Save appointment with utc time.
   *
   * @param params the params
   * @return the map
   */
  public Map<String, Object> saveAppointmentWithUtcTime(Map<String, Object> params) {
    return saveAppointment(vaidateAndChangeUtcAppointmentTime(params));
  }

  /**
   * create new service appointments.
   * @param appointmentObj appointment data
   * @return map
   */
  public Map<String, Object> setPrimaryResAndSaveAppointment(Map<String, Object> appointmentObj) {
    Map<String, String[]> params = new HashMap<>();
    Map<String, Object> appointmentData = (Map<String, Object>) appointmentObj.get("appointment");
    String secRes = (String) appointmentData.get("secondary_resource_id");
    String categoryStr = (String) appointmentData.get("category");
    String centerId = String.valueOf(appointmentData.get("center_id"));
    String dept = String.valueOf(appointmentData.get("dept"));
    params.put("sec_res", new String[] { secRes });
    params.put("app_cat", new String[] { categoryStr});
    params.put("center_id", new String[] { centerId });
    params.put("dept", new String[] { dept });
    String status = appointmentData.get("status") != null
        ? (String) appointmentData.get("status") : "Booked";
    appointmentObj.put("status", status);
    Map<String, Object> newParamsMap = vaidateAndChangeUtcAppointmentTime(appointmentObj);
    Map<String, Object> appointmentMap = (Map<String, Object>) newParamsMap.get("appointment");
    params.put("date", new String[] {(String) appointmentMap.get("date")});
    params.put("slot_time", new String[] {(String) appointmentMap.get("slot_time")});
    String resourceId = setPrimaryRes(params, false);
    Map apptMap = (Map) newParamsMap.get("appointment");
    apptMap.put("primary_resource_id", resourceId);
    return saveAppointment(newParamsMap);
  }

  /**
   * Edit service appointment.
   * @param params parameters
   * @return Map
   */
  public Map<String,Object> setPrimaryResAndUpdateAppointment(Map<String, Object> params) {
    Map<String, Object> newParams =  vaidateAndChangeUtcAppointmentTime(params);
    Map<String, Object> appointmentData = (Map<String, Object>) newParams.get("appointment");
    BasicDynaBean schItems = appointmentService.findByKey(
        (Integer) appointmentData.get("appointment_id"));
    Map<String, String[]> paramsMap = new HashMap<>();
    paramsMap.put("sec_res", new String[] {(String) schItems.get("res_sch_name")});
    paramsMap.put("slot_time", new String[] {(String) appointmentData.get("slot_time")});
    paramsMap.put("app_cat", new String[] {(String) appointmentData.get("category")});
    paramsMap.put("center_id", new String[] { (String.valueOf(schItems.get("center_id"))) });
    paramsMap.put("date", new String[] { (String) appointmentData.get("date") });
    paramsMap.put("primary_resource", new String[] { (String) schItems.get("prim_res_id") });
    ServiceMasterDAO serviceMasterDAO = new ServiceMasterDAO();
    BasicDynaBean serviceBean = null;
    try {
      serviceBean = serviceMasterDAO.findByKey("service_id", schItems.get("res_sch_name"));
    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }
    paramsMap.put("dept", new String[] { String.valueOf(serviceBean.get("serv_dept_id"))} );
    String resourceId = setPrimaryRes(paramsMap, true);
    Map appointmentsParams = new HashMap();
    appointmentsParams.put("appointment", appointmentData);
    if (!resourceId.equals(schItems.get("prim_res_id"))) {
      Map patientAttributesMap = new HashMap();
      patientAttributesMap.put("patient_name", schItems.get("patient_name"));
      patientAttributesMap.put("patient_contact", schItems.get("patient_contact"));
      appointmentsParams.put("patient_attribution", true);
      appointmentsParams.put("patient", patientAttributesMap);
      Map appointmentAttributes = new HashMap();
      appointmentAttributes.put("primary_resource_id", resourceId);
      appointmentAttributes.put("secondary_resource_id", schItems.get("res_sch_name"));
      appointmentAttributes.put("center_id", schItems.get("center_id"));
      appointmentAttributes.put("appointment_slot", appointmentData.get("appointment_slot"));
      appointmentAttributes.put("category", appointmentData.get("category"));
      appointmentAttributes.put("app_source_id", -1);
      appointmentAttributes.put("status","Booked");
      appointmentAttributes.put("dept", serviceBean.get("serv_dept_id"));
      appointmentAttributes.put("appointment_id", appointmentData.get("appointment_id"));
      appointmentAttributes.put("date", appointmentData.get("date"));
      appointmentAttributes.put("slot_time", appointmentData.get("slot_time"));
      appointmentsParams.put("appointment", appointmentAttributes);
    }
    return editAppointmentDetails(appointmentsParams);
  }

  /**
   * Set primary resource for service appointments from widget.
   * @param params parameters
   * @param isReschedule is being rescheduled
   * @return resouce id
   */
  private String setPrimaryRes(Map<String, String[]> params, boolean isReschedule) {
    Map<String,Object> resources = schedulerBulkAppointmentsService
        .getAvailableSlotsForASingleApp(params);
    String slotTime = params.get("slot_time")[0];
    List<Map<String, Object>> listRes = (List<Map<String, Object>>) resources.get(slotTime);
    String resourceId = null;
    Collections.sort(listRes, sortResourceMap());
    if (isReschedule) {
      String prevResourceId = params.get("primary_resource")[0];
      for (Map m : listRes) {
        if (m.get("resource_id").equals(prevResourceId)) {
          resourceId = (String) m.get("resource_id");
          return resourceId;
        }
      }
    }
    resourceId = (String) listRes.get(0).get("resource_id");
    return resourceId;
  }

  /**
   * Sort resources by overbook limit reached.
   * @return Comparator
   */
  private Comparator<Map<String, Object>> sortResourceMap() {
    Comparator<Map<String, Object>> sortByOverBookCount = new Comparator<Map<String, Object>>() {
      @Override
      public int compare(Map<String, Object> o1, Map<String, Object> o2) {
        if ((Integer) o1.get("booked") > (Integer) o2.get("booked")) {
          return 1;
        }
        return -1;
      }
    };
    return sortByOverBookCount;
  }

  /**
   * Edits the appointment with utc time.
   *
   * @param params the params
   * @return the map
   */
  public Map<String, Object> editAppointmentWithUtcTime(Map<String, Object> params) {
    return editAppointmentDetails(vaidateAndChangeUtcAppointmentTime(params));
  }

  /**
   * Vaidate and change utc appointment time.
   *
   * @param params the params
   * @return the map
   */
  public Map<String, Object> vaidateAndChangeUtcAppointmentTime(Map<String, Object> params) {
    if (params.get("appointment") == null) {
      throw new HMSException(HttpStatus.BAD_REQUEST, "exception.bad.request", null);
    }
    Map<String, Object> appointmentData = (Map<String, Object>) params.get("appointment");
    if (appointmentData.get("appointment_slot") == null) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("date", "exception.scheduler.appointment.invalid.date");
      ValidationException ex = new ValidationException(errorMap);
      Map<String, Object> nestedException = new HashMap<>();
      nestedException.put("appointment", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    String appointmentDateTimeStr = (String) appointmentData.get("appointment_slot");
    Timestamp appointmentTimestamp;
    try {
      appointmentTimestamp = DateUtil.parseIso8601Timestamp(appointmentDateTimeStr);
    } catch (ParseException exp) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("date", "exception.scheduler.appointment.invalid.date");
      ValidationException ex = new ValidationException(errorMap);
      Map<String, Object> nestedException = new HashMap<>();
      nestedException.put("appointment", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    if (appointmentData.get("center_id") == null) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("center_id", "exception.scheduler.appointment.center.required");
      ValidationException ex = new ValidationException(errorMap);
      Map<String, Object> nestedException = new HashMap<>();
      nestedException.put("appointment", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    if (!isValidApptStatus(appointmentData)) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("status", "exception.scheduler.appointment.invalid.status");
      ValidationException ex = new ValidationException(errorMap);
      Map<String, Object> nestedException = new HashMap<>();
      nestedException.put("appointment", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    appointmentData.put("date", DateUtil.formatDate(DateUtil.getDatePart(appointmentTimestamp)));
    appointmentData.put("slot_time",
        DateUtil.formatSQlTime(DateUtil.getTimePart(appointmentTimestamp)));
    return params;
  }

  private boolean isValidApptStatus(Map<String, Object> appointmentData) {
    if (((String)appointmentData.get("status")).equalsIgnoreCase("booked")
        || ((String)appointmentData.get("status")).equalsIgnoreCase("confirmed")) {
      return true;
    }
    return false;
  }

  /**
   * Save appointment.
   *
   * @param params the params
   * @return the map
   */
  public Map<String, Object> saveAppointment(Map<String, Object> params) {
    // get the calender associated with resource category
    /*
     * ResourceCalendar resourceCalendar = schedulerResourceCalendarFactory
     * .getInstance(params.get("res_sch_category")[0]); resourceCalendar.createAppointment(params);
     */
    Map<String, Object> appointmentData = (Map<String, Object>) params.get("appointment");

    List<Map> appointmentsData = new ArrayList<Map>();
    appointmentsData.add(appointmentData);
    params.remove("appointment");
    params.put("appointments", appointmentsData);

    List<AppointmentCategory> appCategoryList = new ArrayList();
    String categoryStr = (String) appointmentData.get("category");
    AppointmentCategory apptCategory = getAppCategory(categoryStr);
    appCategoryList.add(apptCategory);

    Map<String, Object> responseMap = appointmentService.createNewAppointments(appCategoryList,
        params);
    List<Map<String, Object>> appointmentsInfo = (List<Map<String, Object>>) responseMap
        .get("appointments");
    List<Integer> apptIds = (List<Integer>) ((Map<String, Object>) appointmentsInfo.get(0))
        .get("appointment_ids_list");

    StringBuilder appIdString = new StringBuilder();
    StringBuilder appCategoryString = new StringBuilder();
    for (int i = 0;i < apptIds.size();i++) {
      if (i != apptIds.size() - 1) {
        appIdString.append(apptIds.get(i) + ",");
        appCategoryString.append(categoryStr + ",");
      } else {
        appIdString.append(apptIds.get(i));
        appCategoryString.append(categoryStr);
      }
    }
    redisMessagePublisher.publishMsgForSchema(RedisMessagePublisher.REDIS_APPOINTMENT_PUSH_CHANNEL,
        appIdString + ";" + appCategoryString);
    return responseMap;
  }

  /**
   * Gets the appointment details.
   *
   * @param params the params
   * @return the appointment details
   */
  public Map<String, Object> getAppointmentDetails(Map<String, String[]> params) {

    ValidationErrorMap validationErrors = new ValidationErrorMap();
    Map<String, Object> nestedException = new HashMap<String, Object>();
    if (!appointmentValidator.validatgetAppointmentDetailsParams(params, validationErrors)) {
      ValidationException ex = new ValidationException(validationErrors);
      nestedException.put("viewappt", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    // get the category object associated with resource category
    AppointmentCategory appointmentCategory = getAppCategory(params.get("res_sch_category")[0]);
    Integer appointmentId = Integer.parseInt(params.get("appointment_id")[0]);
    return appointmentService.getAppointmentDetails(appointmentCategory, appointmentId);
  }

  /**
   * Edits the appointment details.
   *
   * @param params the params
   * @return the map
   */
  public Map<String, Object> editAppointmentDetails(Map<String, Object> params) {

    Map<String, Object> appointmentData = (Map<String, Object>) params.get("appointment");
    Integer oldAppId = (Integer) appointmentData.get("appointment_id");
    Map<String, Object> response = appointmentService.editAppointment(null, params);
    Map<String, Object> responseAppointmentData = new HashMap<String, Object>();
    Integer appId;
    if (response.get("appointment") != null) {
      responseAppointmentData = (Map<String, Object>) response.get("appointment");
      appId = (Integer) responseAppointmentData.get("appointment_id");

    } else {
      responseAppointmentData = ((List<Map<String, Object>>) params.get("appointments")).get(0);
      appId = ((List<Integer>) responseAppointmentData.get("appointment_ids_list")).get(0);
    }

    String category = responseAppointmentData.get("category") != null
        ? (String)responseAppointmentData.get("category") : "DOC";
    redisMessagePublisher.publishMsgForSchema(RedisMessagePublisher.REDIS_APPOINTMENT_PUSH_CHANNEL,
        appId + ";" + category);

    if (!appId.equals(oldAppId)) {
      // case when new appointment is created because of resource change
      redisMessagePublisher.publishMsgForSchema(
          RedisMessagePublisher.REDIS_APPOINTMENT_PUSH_CHANNEL,
          oldAppId + ";" + category);
    }
    return response;
  }

  /**
   * Update appointments status.
   *
   * @param params the params
   * @return the map
   */
  public Map<String, Object> updateAppointmentsStatus(Map<String, Object> params) {
    Map<String, Object> response = appointmentService.updateAppointmentsStatus(params);
    appointmentService.pushUpdateAppointmentStatusToRedis(response);
    return response;
  }

  /**
   * Adds the overrides.
   *
   * @param params the params
   * @return the list
   */
  public List<Map> addOverrides(Map params) {
    return resourceOverrideService.saveBulkOverrideDetails(params);
  }

  /**
   * Gets the app category.
   *
   * @param category the category
   * @return the app category
   */
  public AppointmentCategory getAppCategory(String category) {
    AppointmentCategory apptCategory = null;
    try {
      apptCategory = appointmentCategoryFactory.getInstance(category.toUpperCase(Locale.ENGLISH));
    } catch (Exception exp) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("category", "exception.invalid.category");
      ValidationException ex = new ValidationException(errorMap);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      nestedException.put("repeats", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    return apptCategory;
  }

  /**
   * check if user has rights to overbook appointment.
   * @param resourceId resource id
   * @param apptTime appointment time in String
   * @returns boolean true or false
   */
  public boolean isUserAllowedToBookAppts(String resourceId, String apptTime, String category) {
    Timestamp appointmentTimeStamp = null;
    try {
      appointmentTimeStamp = DateUtil.stringTosqlTimeStampFormatter(apptTime);
    } catch (ParseException exp) {
      logger.error("Cannot convert appointment time string to timestamp format "
          + exp.getStackTrace());
    }
    return isUserAllowedToBookAppts(resourceId, appointmentTimeStamp, category);
  }

  /**
   * check if user has rights to overbook appointment.
   * @param resourceId resource id
   * @param apptTime appointment time in timestamp
   * @param category resource category
   * @return boolean true or false
   */
  public boolean isUserAllowedToBookAppts(String resourceId, Timestamp apptTime, String category) {
    int overbookLimit = appointmentRepository.getResourceOverBookLimit(resourceId, category);
    int numberOfAppts = appointmentRepository.getOverBookCountForSlot(resourceId, apptTime);
    int roleId = RequestContext.getSession() != null ? RequestContext.getRoleId() : 1;
    Map actionRightsMap = RequestContext.getSession() != null
        ? (Map) RequestContext.getSession().getAttribute("actionRightsMap") : null;
    boolean allowApptOverbooking = actionRightsMap == null
        || "A".equals(actionRightsMap.get("allow_appt_overbooking"));
    if ((roleId != 1 && roleId != 2) && !allowApptOverbooking && numberOfAppts >= 1
        && overbookLimit != 0) {
      return false;
    }
    return true;
  }
}
