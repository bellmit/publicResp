package com.insta.hms.core.scheduler;

import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.batchjob.pushevent.Events;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.InstaLinkedMultiValueMap;
import com.insta.hms.common.PhoneNumberUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.clinical.consultation.prescriptions.PendingPrescriptionsService;
import com.insta.hms.core.clinical.order.master.OrderService;
import com.insta.hms.core.scheduler.resourcelist.SchedulerResourceSearchService;
import com.insta.hms.exception.NestableValidationException;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.mdm.ordersets.PackagesRepository;
import com.insta.hms.redis.RedisMessagePublisher;

import org.apache.commons.beanutils.BasicDynaBean;
import org.jsoup.helper.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.ServletContext;

// TODO: Auto-generated Javadoc
/**
 * The Class SchedulerBulkAppointmentsService.
 */
@Service
@SuppressWarnings({ "unchecked", "rawtypes", "deprecation" })
public class SchedulerBulkAppointmentsService {
  
  /** The Constant logger. */
  static final Logger logger = LoggerFactory.getLogger(SchedulerBulkAppointmentsService.class);

  /** The order service. */
  @LazyAutowired
  private OrderService orderService;
  
  /** The appointment validator. */
  @LazyAutowired
  AppointmentValidator appointmentValidator;
  
  /** The appointment category factory. */
  @LazyAutowired
  private AppointmentCategoryFactory appointmentCategoryFactory;
  
  /** The scheduler resource search service. */
  @LazyAutowired
  private SchedulerResourceSearchService schedulerResourceSearchService;
  
  /** The appointment service. */
  @LazyAutowired
  private AppointmentService appointmentService;
  
  /** The packages repository. */
  @LazyAutowired
  private PackagesRepository packagesRepository;
  
  /** The scheduler service. */
  @LazyAutowired
  private SchedulerService schedulerService;
  
  /** The appointment repository. */
  @LazyAutowired
  private AppointmentRepository appointmentRepository;
  
  /** The pending prescriptions service. */
  @LazyAutowired
  private PendingPrescriptionsService pendingPrescriptionsService;
  
  /** The redis message publisher. */
  @LazyAutowired
  RedisMessagePublisher redisMessagePublisher;
  
  @LazyAutowired
  ServletContext context; 

  /**
   * Gets the orderable items for appointments.
   *
   * @param params the params
   * @return the orderable items for appointments
   */
  public Map<String, Object> getOrderableItemsForAppointments(
      MultiValueMap<String, String> params) {
    params.put("service_schedulable", Arrays.asList("A", "S"));
    List<BasicDynaBean> orderableItems = orderService
        .getOrderableItem(new InstaLinkedMultiValueMap<String, String>(params));
    Map orderableItemsData = new HashMap();
    orderableItemsData.put("orderable_items", ConversionUtils.listBeanToListMap(orderableItems));
    return orderableItemsData;
  }

  /**
   * Gets the appointments for patient.
   *
   * @param params the params
   * @return the appointments for patient
   */
  public Map getAppointmentsForPatient(Map<String, String[]> params, String requestHandalerKey) {

    int centerId = 0;
    if (params.get("center_id") != null && params.get("center_id")[0] != null) {
      try {
        centerId = Integer.parseInt(params.get("center_id")[0]);
      } catch (NumberFormatException exp) {
        ValidationErrorMap errorMap = new ValidationErrorMap();
        errorMap.addError("center_id", "exception.scheduler.override.invalid.center_id");
        ValidationException ex = new ValidationException(errorMap);
        Map<String, Object> nestedException = new HashMap<String, Object>();
        nestedException.put("appointments", ex.getErrors());
        throw new NestableValidationException(nestedException);
      }
    } else {
      centerId = RequestContext.getCenterId();
    }

    String fromTimeStr = null;
    Timestamp fromTime = null;
    if (params.get("from_date") != null && params.get("from_date")[0] != null) {
      DateUtil dateUtil = new DateUtil();
      fromTimeStr = ((String[]) params.get("from_date"))[0];
      fromTimeStr = fromTimeStr + " 00:00";
      try {
        fromTime = dateUtil.parseTheTimestamp(fromTimeStr);
      } catch (ParseException exp) {
        ValidationErrorMap errorMap = new ValidationErrorMap();
        errorMap.addError("from_date", "scheduler.validation.error.invalidDataTime");
        ValidationException ex = new ValidationException(errorMap);
        Map<String, Object> nestedException = new HashMap<String, Object>();
        nestedException.put("appointments", ex.getErrors());
        throw new NestableValidationException(nestedException);
      }
    }

    Integer appId = null;
    if (params.get("appointment_id") != null && params.get("appointment_id")[0] != null) {
      try {
        appId = Integer.parseInt(params.get("appointment_id")[0]);
      } catch (NumberFormatException exp) {
        ValidationErrorMap errorMap = new ValidationErrorMap();
        errorMap.addError("appointment_id", "exception.scheduler.edit.noAppointment");
        ValidationException ex = new ValidationException(errorMap);
        Map<String, Object> nestedException = new HashMap<String, Object>();
        nestedException.put("appointments", ex.getErrors());
        throw new NestableValidationException(nestedException);
      }
    }

    String mrNo = null;
    if (params.get("mr_no") != null && params.get("mr_no")[0] != null) {
      mrNo = ((String[]) params.get("mr_no"))[0];
    }
    
    Integer contactId = null;
    if (params.get("contact_id") != null && params.get("contact_id")[0] != null) {
      String contactIdStr = ((String[]) params.get("contact_id"))[0];
      if (contactIdStr != null && !contactIdStr.equals("")) {
        contactId = Integer.parseInt(contactIdStr);
      }
    }
    
    String patientContact = null;
    if (params.get("patient_contact") != null && params.get("patient_contact")[0] != null) {
      patientContact = (((String[]) params.get("patient_contact"))[0]).trim();
    }
    
    
    if (requestHandalerKey != null) {
      Map sessionMap = (Map) context.getAttribute("sessionMap");
      if (sessionMap != null && !sessionMap.isEmpty()) {
        Map sessionParameters = (Map) sessionMap.get(requestHandalerKey);
        if (sessionParameters != null && (Boolean) sessionParameters.get("patient_login")) {
          mrNo = (String) sessionParameters.get("customer_user_id");
        }
      }

    }

    String status = "all";
    if (params.get("app_status") != null && params.get("app_status")[0] != null) {
      status = ((String[]) params.get("app_status"))[0];
    }

    List<Map> appointmentList = appointmentService.getAppointmentsForPatient(
        Arrays.asList(new Integer[] { centerId }), fromTime, mrNo, contactId,
        patientContact, appId, status);

    List<Map> resList = new ArrayList();
    for (Map appointment : appointmentList) {
      appointment.get("add_res_name");
      Boolean found = false;
      for (Map appt : resList) {
        if ((int) appt.get("appointment_id") == (int) appointment.get("appointment_id")) {
          String appCat = (String) appointment.get("app_cat");
          if (appCat.equals("DOC")) {
            if (!appointment.get("add_res_type").equals("OPDOC")) {
              Map addResMap = new HashMap();
              addResMap.put("resource_id", appointment.get("add_res_id"));
              addResMap.put("resource_type", appointment.get("add_res_type"));
              addResMap.put("resource_name", appointment.get("add_res_name"));
              List addResList = (List) appt.get("additional_resources");
              List<Map> newList = new ArrayList<Map>(addResList);
              newList.add(addResMap);
              appt.put("additional_resources", newList);
            }
          } else if (appCat.equals("OPE")) {
            if (!appointment.get("add_res_type").equals("THID")) {
              Map addResMap = new HashMap();
              addResMap.put("resource_id", appointment.get("add_res_id"));
              addResMap.put("resource_type", appointment.get("add_res_type"));
              addResMap.put("resource_name", appointment.get("add_res_name"));
              List addResList = (List) appt.get("additional_resources");
              List<Map> newList = new ArrayList<Map>(addResList);
              newList.add(addResMap);
              appt.put("additional_resources", newList);
            }
          } else if (appCat.equals("DIA")) {
            if (!appointment.get("add_res_type").equals("EQID")) {
              Map addResMap = new HashMap();
              addResMap.put("resource_id", appointment.get("add_res_id"));
              addResMap.put("resource_type", appointment.get("add_res_type"));
              addResMap.put("resource_name", appointment.get("add_res_name"));
              List addResList = (List) appt.get("additional_resources");
              List<Map> newList = new ArrayList<Map>(addResList);
              newList.add(addResMap);
              appt.put("additional_resources", newList);
            }
          } else if (appCat.equals("SNP")) {
            if (!appointment.get("add_res_type").equals("SRID")) {
              Map addResMap = new HashMap();
              addResMap.put("resource_id", appointment.get("add_res_id"));
              addResMap.put("resource_type", appointment.get("add_res_type"));
              addResMap.put("resource_name", appointment.get("add_res_name"));
              List addResList = (List) appt.get("additional_resources");
              List<Map> newList = new ArrayList<Map>(addResList);
              newList.add(addResMap);
              appt.put("additional_resources", newList);
            }
          }

          found = true;
        }
      }
      if (!found) {
        resList.add(getDisplayRowMap(appointment));
      }
    }
    Map responseMap = new HashMap();
    responseMap.put("appointments", resList);
    return responseMap;
  }

  /**
   * Gets the display row map.
   *
   * @param apptMap the appt map
   * @return the display row map
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public Map getDisplayRowMap(Map apptMap) {
    Map displayRowMap = new HashMap();

    Map primResMap = new HashMap();
    primResMap.put("resource_id", apptMap.get("prim_res_id"));
    primResMap.put("resource_name", apptMap.get("prim_res_name"));
    displayRowMap.put("primary_resource", primResMap);

    Map secResMap = new HashMap();
    secResMap.put("resource_id", apptMap.get("sec_res_id"));
    secResMap.put("resource_name", apptMap.get("sec_res_name"));
    displayRowMap.put("secondary_resource", secResMap);
    
    String appCat = (String) apptMap.get("app_cat");
    if (appCat.equals("DOC")) {
      if (!apptMap.get("add_res_type").equals("OPDOC")) {
        Map addResMap = new HashMap();
        addResMap.put("resource_id", apptMap.get("add_res_id"));
        addResMap.put("resource_type", apptMap.get("add_res_type"));
        addResMap.put("resource_name", apptMap.get("add_res_name"));
        displayRowMap.put("additional_resources", Arrays.asList(new Map[] { addResMap }));
      } else {
        List list = new ArrayList<Map>();
        displayRowMap.put("additional_resources", list);
      }
    } else if (appCat.equals("OPE")) {
      if (!apptMap.get("add_res_type").equals("THID")) {
        Map addResMap = new HashMap();
        addResMap.put("resource_id", apptMap.get("add_res_id"));
        addResMap.put("resource_type", apptMap.get("add_res_type"));
        addResMap.put("resource_name", apptMap.get("add_res_name"));
        displayRowMap.put("additional_resources", Arrays.asList(new Map[] { addResMap }));
      } else {
        List list = new ArrayList<Map>();
        displayRowMap.put("additional_resources", list);
      }
    } else if (appCat.equals("DIA")) {
      if (!apptMap.get("add_res_type").equals("EQID")) {
        Map addResMap = new HashMap();
        addResMap.put("resource_id", apptMap.get("add_res_id"));
        addResMap.put("resource_type", apptMap.get("add_res_type"));
        addResMap.put("resource_name", apptMap.get("add_res_name"));
        displayRowMap.put("additional_resources", Arrays.asList(new Map[] { addResMap }));
      } else {
        List list = new ArrayList<Map>();
        displayRowMap.put("additional_resources", list);
      }
    } else if (appCat.equals("SNP")) {
      if (!apptMap.get("add_res_type").equals("SRID")) {
        Map addResMap = new HashMap();
        addResMap.put("resource_id", apptMap.get("add_res_id"));
        addResMap.put("resource_type", apptMap.get("add_res_type"));
        addResMap.put("resource_name", apptMap.get("add_res_name"));
        displayRowMap.put("additional_resources", Arrays.asList(new Map[] { addResMap }));
      } else {
        List list = new ArrayList<Map>();
        displayRowMap.put("additional_resources", list);
      }
    }
    displayRowMap.put("patient_name", apptMap.get("patient_name"));
    displayRowMap.put("appointment_id", apptMap.get("appointment_id"));
    displayRowMap.put("appointment_time", apptMap.get("start_time"));
    displayRowMap.put("appointment_date", apptMap.get("date"));
    displayRowMap.put("appointment_status", apptMap.get("appointment_status"));
    displayRowMap.put("duration", apptMap.get("duration"));
    displayRowMap.put("center_id", apptMap.get("center_id"));
    displayRowMap.put("center_name", apptMap.get("center_name"));
    displayRowMap.put("package_id", apptMap.get("package_id"));
    displayRowMap.put("app_cat", apptMap.get("app_cat"));
    displayRowMap.put("package_name", apptMap.get("package_name"));
    displayRowMap.put("presc_doc_id", apptMap.get("presc_doc_id"));
    displayRowMap.put("presc_doc_name", apptMap.get("presc_doc_name"));
    displayRowMap.put("cond_doc_id", apptMap.get("cond_doc_id"));
    displayRowMap.put("cond_doc_name", apptMap.get("cond_doc_name"));
    displayRowMap.put("remarks", apptMap.get("remarks"));
    displayRowMap.put("complaint", apptMap.get("complaint"));
    displayRowMap.put("appointment_pack_group_id", apptMap.get("appointment_pack_group_id"));
    displayRowMap.put("app_source_id", apptMap.get("app_source_id"));
    displayRowMap.put("scheduler_visit_type", apptMap.get("scheduler_visit_type"));
    displayRowMap.put("department_id", apptMap.get("department_id"));
    displayRowMap.put("conducting_doc_mandatory", apptMap.get("conducting_doc_mandatory"));
    displayRowMap.put("testType", apptMap.get("type"));
    displayRowMap.put("waitlist", apptMap.get("waitlist"));
    displayRowMap.put("department_name", apptMap.get("department_name"));
    displayRowMap.put("app_source_name", apptMap.get("app_source_name"));
    displayRowMap.put("original_time", apptMap.get("original_time"));
    displayRowMap.put("original_date", apptMap.get("original_date"));
    displayRowMap.put("last_updated_time", apptMap.get("changed_time"));
    displayRowMap.put("last_updated_date_time", apptMap.get("changed_date"));
    displayRowMap.put("rescheduled", apptMap.get("rescheduled"));
    displayRowMap.put("visit_mode", apptMap.get("visit_mode"));
    return displayRowMap;
  }

  /**
   * If appointment possible.
   *
   * @param resId the res id
   * @param apptTimeStamp the appt time stamp
   * @param appointmentDuration the appointment duration
   * @param appointmentCategory the appointment category
   * @param centerId the center id
   * @param dateStr the date str
   * @param processedResourceMap the processed resource map
   * @param primeDefaultDuration the prime default duration
   * @return the boolean
   */
  public Boolean ifAppointmentPossible(String resId, Timestamp apptTimeStamp,
      int appointmentDuration, AppointmentCategory appointmentCategory, int centerId,
      String dateStr, Map processedResourceMap, int primeDefaultDuration, String apptid) {

    Timestamp endTimeStamp = addTimeToTimeStamp(apptTimeStamp, appointmentDuration);
    String resType = appointmentCategory.getPrimaryResourceType();
    String key = resId + resType + dateStr;
    // size of the availability array depends on slot duration of primary resource
    int arSize = 24 * 60 / primeDefaultDuration + 7;

    if (processedResourceMap.get(key) != null) {
      // we have already processed this resource
      return checkIfAppointmentPossible(apptTimeStamp, (Map) processedResourceMap.get(key),
          appointmentDuration, primeDefaultDuration, endTimeStamp);
    } else {
      // we hit a new resource

      // get his schedule i.e. default availability/override
      List availList = appointmentValidator.getResourceScheduele(resId, resType, apptTimeStamp,
          endTimeStamp, appointmentCategory, centerId);
      Integer overbookLimit = appointmentCategory.getResourceOverbookLimit(resId, resType);
      // appointments for the day
      List appList = appointmentRepository.getAllApptsForDayForRes(dateStr, resType, resId, apptid);
      // process the above lists to generate a single array which can be used directly
      Map availMap = getFinalAvailabilityMap(overbookLimit, appList, availList,
          primeDefaultDuration, dateStr, arSize);
      processedResourceMap.put(key, availMap);
      processedResourceMap.put("visitModeValues", availMap.get("visitModeMap"));
      return checkIfAppointmentPossible(apptTimeStamp, (Map) processedResourceMap.get(key),
          appointmentDuration, primeDefaultDuration, endTimeStamp);
    }
  }

  /**
   * Generate avail map for res.
   *
   * @return the map
   */
  public Map generateAvailMapForRes() {
    return null;
  }

  /**
   * Check if appointment possible.
   *
   * @param apptTimeStamp the appt time stamp
   * @param availResMap the avail res map
   * @param duration the duration
   * @param primeDefaultDuration the prime default duration
   * @param endTimeStamp the end time stamp
   * @return the boolean
   */
  public Boolean checkIfAppointmentPossible(Timestamp apptTimeStamp, Map availResMap, int duration,
      int primeDefaultDuration, Timestamp endTimeStamp) {

    ValidationErrorMap map = new ValidationErrorMap();
    if (!appointmentValidator.validateWithinSameDay(apptTimeStamp, endTimeStamp, map)) {
      return false;
    }
    int minutes1 = getMinutes(apptTimeStamp) / primeDefaultDuration;
    int minutes2 = getMinutes(endTimeStamp) / primeDefaultDuration;
    if (getMinutes(endTimeStamp) % primeDefaultDuration == 0) {
      minutes2--;
    }
    int totalAvailDuration = 0;
    Boolean[] availArray = (Boolean[]) availResMap.get("availArray");
    // as explained this logic is to process cases where res is available for duration less than his
    // slot
    int[] durationLimitArray = (int[]) availResMap.get("durationLimit");

    while (minutes1 <= minutes2) {
      if (availArray[minutes1]) {
        totalAvailDuration += (durationLimitArray[minutes1]);
      }
      minutes1++;
    }
    if (totalAvailDuration >= duration) {
      return true;
    }
    return false;
  }

  /**
   * Gets the appt array.
   *
   * @param overbookLimit the overbook limit
   * @param appList the app list
   * @param primDefaultDuration the prim default duration
   * @param arSize the ar size
   * @return the appt array
   */
  public Boolean[] getApptArray(Integer overbookLimit, List<BasicDynaBean> appList,
      int primDefaultDuration, int arSize,  Map[] overBookInfArr) {

    int[] ar = new int[arSize];
    Boolean[] result = new Boolean[arSize];

    // initialize the array
    for (int i = 0; i < arSize; i++) {
      result[i] = false;
    }

    int roleId = RequestContext.getSession() != null ? RequestContext.getRoleId() : 1;
    Map actionRightsMap = RequestContext.getSession() != null ? (Map)
        RequestContext.getSession().getAttribute("actionRightsMap") : null;
    boolean allowApptOverbooking = actionRightsMap != null
        ? "A".equals(actionRightsMap.get("allow_appt_overbooking")) : true;

    // process the appointments to mark the timings on the array
    for (BasicDynaBean bean : appList) {

      Timestamp startAvailTime = (Timestamp) bean.get("appointment_time");
      ar[getMinutes(startAvailTime) / primDefaultDuration]++;

      Timestamp endAvailTime = (Timestamp) bean.get("end_appointment_time");
      int minutesInEndTime = getMinutes(endAvailTime);
      if (minutesInEndTime % primDefaultDuration == 0) {
        ar[minutesInEndTime / primDefaultDuration]--;
      } else {
        ar[minutesInEndTime / primDefaultDuration + 1]--;
      }

    }
    int count = 0;
    for (int i = 0; i < arSize; i++) {
      // we get the count of the appointments and check over book reached simultaneously
      count += ar[i];

      // check if this role has acl to overbook appointments
      if ((roleId != 1 && roleId != 2) && !allowApptOverbooking
          && count >= 1 && (overbookLimit != 0 || overbookLimit != null)) {
        result[i] = false;
      } else if (overbookLimit == null || count <= overbookLimit) {
        // resource can be booked
        result[i] = true;
        Map overBookInfoForSlot = new HashMap<>();
        overBookInfoForSlot.put("overbook_limit", overbookLimit);
        overBookInfoForSlot.put("booked", count);
        overBookInfArr[i] = overBookInfoForSlot;
      }
    }
    return result;
  }

  /**
   * Gets the avail map.
   *
   * @param availList the avail list
   * @param primDefaultDuration the prim default duration
   * @param apptDate the appt date
   * @param arSize the ar size
   * @return the avail map
   */
  public Map getAvailMap(List<BasicDynaBean> availList, int primDefaultDuration, String apptDate,
      int arSize) {

    Map resultMap = new HashMap();
    Boolean[] availArray = new Boolean[arSize];
    List timingsList = new ArrayList();
    for (int i = 0; i < arSize; i++) {
      availArray[i] = false;
    }

    // durationLimit is for checking availabilities less than primDefaultDuration like slot is of 15
    // min but resource is available for 5 min only
    int[] durationLimit = new int[arSize];
    for (BasicDynaBean resourceBaen : availList) {
      if (resourceBaen.get("availability_status").equals("A")) {
        // if res is available , we go to each slot falling in the interval to mark available

        Time fromTime = (java.sql.Time) resourceBaen.get("from_time");
        int fromMinute = getMinutes(fromTime) / primDefaultDuration;
        if ((getMinutes(fromTime)) % primDefaultDuration != 0) {
          fromMinute++;
        }

        Time toTime = (java.sql.Time) resourceBaen.get("to_time");
        int toMinute = getMinutes(toTime) / primDefaultDuration;

        while (fromMinute < toMinute) {
          durationLimit[fromMinute] = primDefaultDuration;
          availArray[fromMinute] = true;
          fromMinute++;
        }
        Map availVisitModeMap = new HashMap();
        availVisitModeMap.put("from_time", fromTime);
        availVisitModeMap.put("to_time", toTime);
        availVisitModeMap.put("visit_mode", resourceBaen.get("visit_mode"));
        timingsList.add(availVisitModeMap);

        if ((getMinutes(toTime) % primDefaultDuration) != 0) {
          availArray[toMinute] = true;
          durationLimit[toMinute] = getMinutes(toTime) % primDefaultDuration;
        }
      }
    }
    resultMap.put("availArray", availArray);
    resultMap.put("durationLimit", durationLimit);
    resultMap.put("visitModeMap", timingsList);
    return resultMap;
  }

  /**
   * Gets the final availability map.
   *
   * @param overbookLimit the overbook limit
   * @param appList the app list
   * @param availList the avail list
   * @param primDefaultDuration the prim default duration
   * @param apptDate the appt date
   * @param arSize the ar size
   * @return the final availability map
   */
  public Map getFinalAvailabilityMap(Integer overbookLimit, List<BasicDynaBean> appList,
      List<BasicDynaBean> availList, int primDefaultDuration, String apptDate, int arSize) {

    Map finalAvailMap = new HashMap();
    // process appointments list to generate appointments array
    Map[] overBookInfArr = new HashMap[arSize];
    Boolean[] apptArray = getApptArray(overbookLimit, appList, primDefaultDuration,
        arSize, overBookInfArr);
    Map availMap = getAvailMap(availList, primDefaultDuration, apptDate, arSize);
    Boolean[] availArray = (Boolean[]) availMap.get("availArray");
    Boolean[] finalAvailabilityArray = new Boolean[arSize];
    for (int i = 0; i < arSize; i++) {
      finalAvailabilityArray[i] = availArray[i] & apptArray[i];
    }
    finalAvailMap.put("overBookinfo", overBookInfArr);
    finalAvailMap.put("availArray", finalAvailabilityArray);
    finalAvailMap.put("durationLimit", availMap.get("durationLimit"));
    finalAvailMap.put("visitModeMap", availMap.get("visitModeMap"));
    return finalAvailMap;
  }

  /**
   * Gets the minutes.
   *
   * @param time the d
   * @return the minutes
   */
  public int getMinutes(Time time) {
    return time.getHours() * 60 + time.getMinutes();
  }

  /**
   * Gets the minutes.
   *
   * @param time the d
   * @return the minutes
   */
  public int getMinutes(Timestamp time) {
    return time.getHours() * 60 + time.getMinutes();
  }

  /**
   * Gets the minutes.
   *
   * @param time the d
   * @return the minutes
   */
  public int getMinutes(Date time) {
    return time.getHours() * 60 + time.getMinutes();
  }

  /**
   * Adds the time to time stamp.
   *
   * @param timeStamp the t
   * @param durationInMin the duration in min
   * @return the timestamp
   */
  public Timestamp addTimeToTimeStamp(Timestamp timeStamp, int durationInMin) {
    Long timeStampLong = timeStamp.getTime();
    timeStampLong = timeStampLong + (durationInMin * 60 * 1000);
    return new java.sql.Timestamp(timeStampLong);
  }

  /**
   * Gets the available slots.
   *
   * @param params the params
   * @return the available slots
   */
  public Map getAvailableSlots(Map<String, String[]> params) {

    int centerId = 0;
    if (params.get("center_id") != null && params.get("center_id")[0] != null) {
      try {
        centerId = Integer.parseInt(params.get("center_id")[0]);
      } catch (NumberFormatException exp) {
        ValidationErrorMap errorMap = new ValidationErrorMap();
        errorMap.addError("center_id", "exception.scheduler.override.invalid.center_id");
        ValidationException ex = new ValidationException(errorMap);
        Map<String, Object> nestedException = new HashMap<String, Object>();
        nestedException.put("combinations", ex.getErrors());
        throw new NestableValidationException(nestedException);
      }
    } else {
      centerId = RequestContext.getCenterId();
    }

    String secResStr = null;
    if (params.get("secondary_resources_id_list") != null
        && params.get("secondary_resources_id_list")[0] != null) {
      secResStr = params.get("secondary_resources_id_list")[0];
    }
    List<String> secResList = null;
    if (secResStr != null) {
      secResList = Arrays.asList(secResStr.split(","));
    } else {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("secondary_resources_id_list",
          "exception.scheduler.invalidormissing.params");
      ValidationException ex = new ValidationException(errorMap);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      nestedException.put("combinations", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    int noOfItems = secResList.size();

    String appCatStr = null;
    if (params.get("appointment_category_list") != null
        && params.get("appointment_category_list")[0] != null) {
      appCatStr = params.get("appointment_category_list")[0];
    }
    List<String> appCatList = null;
    if (appCatStr != null) {
      appCatList = Arrays.asList(appCatStr.split(","));
    } else {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("appointment_category_list", "exception.scheduler.invalidormissing.params");
      ValidationException ex = new ValidationException(errorMap);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      nestedException.put("combinations", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    if (appCatList.size() != noOfItems) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("appointment_category_list", "exception.scheduler.invalid.size");
      ValidationException ex = new ValidationException(errorMap);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      nestedException.put("combinations", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }

    String dependencyStr = null;
    if (params.get("interdependency_list") != null
        && params.get("interdependency_list")[0] != null) {
      dependencyStr = params.get("interdependency_list")[0];
    }
    List<String> dependencyList = null;
    if (dependencyStr != null) {
      dependencyList = Arrays.asList(dependencyStr.split(","));
    } else {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("interdependency_list", "exception.scheduler.invalidormissing.params");
      ValidationException ex = new ValidationException(errorMap);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      nestedException.put("combinations", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    if (dependencyList.size() != noOfItems) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("interdependency_list", "exception.scheduler.invalid.size");
      ValidationException ex = new ValidationException(errorMap);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      nestedException.put("combinations", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }

    String deptStr = null;
    if (params.get("dept_list") != null && params.get("dept_list")[0] != null) {
      deptStr = params.get("dept_list")[0];
    }
    List<String> deptList = null;
    if (deptStr != null) {
      deptList = Arrays.asList(deptStr.split(","));
    } else {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("dept_list", "exception.scheduler.invalidormissing.params");
      ValidationException ex = new ValidationException(errorMap);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      nestedException.put("combinations", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    if (deptList.size() != noOfItems) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("dept_list", "exception.scheduler.invalid.size");
      ValidationException ex = new ValidationException(errorMap);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      nestedException.put("combinations", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }

    String timeStr = null;
    String dateStr = null;
    Timestamp apptTime = null;
    if (params.get("time") != null && params.get("time")[0] != null) {
      timeStr = params.get("time")[0];
    }

    if (params.get("date") != null && params.get("date")[0] != null) {
      dateStr = params.get("date")[0];
    }
    if (timeStr != null && dateStr != null) {
      DateUtil dateUtil = new DateUtil();
      try {
        apptTime = dateUtil.parseTheTimestamp(dateStr + " " + timeStr);
        Timestamp currTime = DateUtil.getCurrentTimestamp();
        if (apptTime.before(currTime)) {
          ValidationErrorMap errorMap = new ValidationErrorMap();
          errorMap.addError("dateTime", "exception.scheduler.override.old.dates");
          ValidationException ex = new ValidationException(errorMap);
          Map<String, Object> nestedException = new HashMap<String, Object>();
          nestedException.put("combinations", ex.getErrors());
          throw new NestableValidationException(nestedException);
        }
      } catch (ParseException exp) {
        ValidationErrorMap errorMap = new ValidationErrorMap();
        errorMap.addError("dateTime", "scheduler.validation.error.invalidDataTime");
        ValidationException ex = new ValidationException(errorMap);
        Map<String, Object> nestedException = new HashMap<String, Object>();
        nestedException.put("combinations", ex.getErrors());
        throw new NestableValidationException(nestedException);
      }
    } else {
      apptTime = DateUtil.getCurrentTimestamp();
    }
    boolean isUtc = false;
    if (params.get("is_utc") != null && params.get("is_utc")[0] != null) {
      isUtc = "true".equals(params.get("is_utc")[0]);
    }
    Map responseMap = new HashMap();
    responseMap.put("combinations", getSlots(secResList, apptTime, appCatList, deptList, centerId,
        dependencyList, dateStr, isUtc));
    return responseMap;
  }

  /**
   * Gets the prim res list.
   *
   * @param secResId the sec res id
   * @param apptCat the appt cat
   * @param deptId the dept id
   * @param centerId the center id
   * @return the prim res list
   */
  public List getPrimResList(String secResId, AppointmentCategory apptCat, String deptId,
      int centerId) {
    return apptCat.getPrimResApplicableForSecRes(secResId, centerId, deptId);
  }

  /**
   * Gets the appointment duration.
   *
   * @param apptCat the appt cat
   * @param secResId the sec res id
   * @param primResId the prim res id
   * @return the appointment duration
   */
  public int getAppointmentDuration(AppointmentCategory apptCat, String secResId,
      String primResId) {
    int duration = apptCat.getAppointmentDuration(secResId, primResId);
    if (duration == -1) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("consultation type",
          "exception.scheduler.no.duration.in.consultation.type");
      ValidationException ex = new ValidationException(errorMap);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      nestedException.put("combinations", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    return duration;
  }

  /**
   * Save all bulk appointments.
   *
   * @param params the params
   * @return the list
   */
  public List<Map<String, Object>> saveAllBulkAppointments(Map params) {
    List<Map<String, Object>> response = appointmentService.saveBulkAppointments(params);
    List<Integer> appointmentIdForResponse = new ArrayList();
    List<String> appointmentCatForResponse = new ArrayList();
    for (Map<String, Object> appointmentObject : response) {
      Map<String, Object> appointmentsInfo = (Map<String, Object>) appointmentObject
          .get("appointments");
      List<Integer> apptIds = (List<Integer>) appointmentsInfo.get("appointment_ids_list");
      appointmentIdForResponse.add(apptIds.get(0));
      appointmentCatForResponse.add((String) appointmentsInfo.get("category"));
    }
    String apptCategories = StringUtil.join(appointmentCatForResponse, ",");
    String apptIds = StringUtil.join(appointmentIdForResponse, ",");
    redisMessagePublisher.publishMsgForSchema(RedisMessagePublisher.REDIS_APPOINTMENT_PUSH_CHANNEL,
        apptIds + ";" + apptCategories);
    return response;
  }

  /**
   * Recurfn.
   *
   * @param responseList the response list
   * @param secResList the sec res list
   * @param recurLvl the recur lvl
   * @param apptTimeStamp the appt time stamp
   * @param apptCatList the appt cat list
   * @param deptList the dept list
   * @param centerId the center id
   * @param combinationMap the combination map
   * @param numberOfItemsInOrderSet the number of items in order set
   * @param dependencyList the dependency list
   * @param listLength the list length
   * @param allPrimResList the all prim res list
   * @param appointmentDurationList the appointment duration list
   * @param processedResourceMap the processed resource map
   * @param isUtc the is utc
   */
  public void recurfn(List<Map> responseList, List<String> secResList, int recurLvl,
      Timestamp apptTimeStamp, List<AppointmentCategory> apptCatList, List<String> deptList,
      int centerId, Map combinationMap, int numberOfItemsInOrderSet, List<String> dependencyList,
      int listLength, List<List<Map>> allPrimResList, List appointmentDurationList,
      Map processedResourceMap, boolean isUtc) {

    // return size of list shall not exceed 15/3
    if (responseList.size() >= listLength) {
      return;
    }

    // we found a combination
    if (recurLvl >= numberOfItemsInOrderSet) {
      Map tempMap = new HashMap(combinationMap);
      if (listLength != 15) {
        for (Map map : responseList) {
          if (sameCombination(map, tempMap)) {
            return;
          }
        }
      }
      responseList.add(tempMap);
      return;
    }

    // get values for the items
    AppointmentCategory apptCat = apptCatList.get(recurLvl);
    int duration = (int) appointmentDurationList.get(recurLvl);
    String dependency = dependencyList.get(recurLvl);

    List<Map> primResList = allPrimResList.get(recurLvl);

    for (Map primRes : primResList) {

      // time calculation for the item-slot
      String primResId = (String) primRes.get("resource_id");
      SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
      SimpleDateFormat sdf2 = new SimpleDateFormat("dd-MM-yyyy");
      int defaultDuration = (int) primRes.get("defaultDuration");
      final Timestamp originalTimeStamp = new Timestamp(apptTimeStamp.getTime());
      Time tempTime = schedulerResourceSearchService.getNextSlotTime(sdf.format(apptTimeStamp),
          defaultDuration);
      if (tempTime == null) {
        return;
      }
      String date = sdf2.format(apptTimeStamp);
      String apptTimeStr = date + " " + sdf.format(tempTime);
      try {
        apptTimeStamp = DateUtil.parseTimestamp(apptTimeStr);
      } catch (ParseException exp) {
        logger.debug("exception",exp);
      }
      // check if appt is possible at the time
      if (ifAppointmentPossible(primResId, apptTimeStamp, duration, apptCat, centerId, date,
          processedResourceMap, defaultDuration,null)) {
        // if possible put it in map and recur
        if (isUtc) {
          primRes.put("time_slot", DateUtil.formatIso8601TimestampNoSec(apptTimeStamp));
        } else {
          primRes.put("time_slot", apptTimeStamp);
        }
        combinationMap.put("slot" + recurLvl, primRes);
        apptTimeStamp = addTimeToTimeStamp(apptTimeStamp, duration);
        if (dependency != null && !dependency.equals("") && !dependency.equals("0")) {
          apptTimeStamp = addTimeToTimeStamp(apptTimeStamp, Integer.parseInt(dependency));
        }

        recurfn(responseList, secResList, recurLvl + 1, apptTimeStamp, apptCatList, deptList,
            centerId, combinationMap, numberOfItemsInOrderSet, dependencyList, listLength,
            allPrimResList, appointmentDurationList, processedResourceMap, isUtc);
      }
      apptTimeStamp = new Timestamp(originalTimeStamp.getTime());
    }

  }

  /**
   * Same combination.
   *
   * @param m1 the m 1
   * @param m2 the m 2
   * @return the boolean
   */
  Boolean sameCombination(Map<String, Object> m1, Map<String, Object> m2) {
    // iterate over all the keys in map and check for equality (singly nested map)
    for (String key : m1.keySet()) {
      Map x1 = (Map) m1.get(key);
      Map x2 = (Map) m2.get(key);
      if (!x1.equals(x2)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Gets the slots.
   *
   * @param secResList the sec res list
   * @param apptTimeStamp the appt time stamp
   * @param apptCatList the appt cat list
   * @param deptList the dept list
   * @param centerId the center id
   * @param dependencyList the dependency list
   * @param dateStr the date str
   * @param isUtc the is utc
   * @return the slots
   */
  public List getSlots(List<String> secResList, Timestamp apptTimeStamp, List<String> apptCatList,
      List<String> deptList, int centerId, List<String> dependencyList, String dateStr,
      boolean isUtc) {

    Timestamp endTimeStamp = null;
    String endTimeStr = dateStr + " " + "23:59";
    try {
      endTimeStamp = DateUtil.parseTimestamp(endTimeStr);
    } catch (ParseException exp) {
      logger.debug("ParseException",exp);
    }
    List appCatList = prepareAppCatList(apptCatList);
    int numberOfItemsInOrderSet = secResList.size();
    List<List<Map>> allPrimResList = prepareResDataList(secResList, appCatList, deptList, centerId,
        numberOfItemsInOrderSet);
    List appDurationList = prepareAppointmentDurationList(secResList, appCatList);

    List<Map> responseList = new ArrayList();
    Map combinationMap = new HashMap();
    Map processedResourceMap = new HashMap();
    Timestamp origAppTime = new Timestamp(apptTimeStamp.getTime());
    recurfn(responseList, secResList, 0, apptTimeStamp, appCatList, deptList, centerId,
        combinationMap, numberOfItemsInOrderSet, dependencyList, 15, allPrimResList,
        appDurationList, processedResourceMap, isUtc);

    if (responseList.size() == 0) {
      // get skip value

      int skipValue = 15;
      if (allPrimResList.get(0).size() != 0) {
        skipValue = getSkipTime(allPrimResList.get(0));
      }
      // start iteration till we reach availability
      while (responseList.size() < 3 && origAppTime.before(endTimeStamp)) {
        origAppTime = addTimeToTimeStamp(origAppTime, skipValue);
        apptTimeStamp = new Timestamp(origAppTime.getTime());
        if (apptTimeStamp.after(endTimeStamp)) {
          break;
        }
        List<Map> tempResponseList = new ArrayList();
        Map combinationMap1 = new HashMap();
        recurfn(tempResponseList, secResList, 0, apptTimeStamp, appCatList, deptList, centerId,
            combinationMap1, numberOfItemsInOrderSet, dependencyList, 3 - responseList.size(),
            allPrimResList, appDurationList, processedResourceMap, isUtc);
        for (Map map : tempResponseList) {
          Map tempMap = copySlotMap(map);
          if (responseList.size() == 0) {
            responseList.add(tempMap);
          } else {
            boolean found = false;
            for (Map x : responseList) {
              if (sameCombination(x, tempMap)) {
                found = true;
              }
            }
            if (!found) {
              responseList.add(tempMap);
            }
          }
        }
      }
    }
    return responseList;
  }

  /**
   * Copy slot map.
   *
   * @param m1 the m 1
   * @return the map
   */
  public Map copySlotMap(Map<String, Map> m1) {

    Map responseMap = new HashMap();
    Map slot0 = m1.get("slot0");
    responseMap.put("slot0", new HashMap(slot0));
    Map slot1 = m1.get("slot1");
    responseMap.put("slot1", new HashMap(slot1));
    return responseMap;
  }

  /**
   * Prepare app cat list.
   *
   * @param apptCatList the appt cat list
   * @return the list
   */
  public List prepareAppCatList(List<String> apptCatList) {
    // appointment category list
    List<AppointmentCategory> resultList = new ArrayList();
    for (int i = 0; i < apptCatList.size(); i++) {
      AppointmentCategory apptCat = appointmentCategoryFactory
          .getInstance(apptCatList.get(i).toUpperCase(Locale.ENGLISH));
      resultList.add(apptCat);
    }
    return resultList;
  }

  /**
   * Prepare res data list.
   *
   * @param secResList the sec res list
   * @param apptCatList the appt cat list
   * @param deptList the dept list
   * @param centerId the center id
   * @param numberOfItemsInOrderSet the number of items in order set
   * @return the list
   */
  public List<List<Map>> prepareResDataList(List<String> secResList,
      List<AppointmentCategory> apptCatList, List<String> deptList, int centerId,
      int numberOfItemsInOrderSet) {

    // this map contains all the primary resources with their default duration for every sec Res
    List resultList = new ArrayList();
    for (int i = 0; i < numberOfItemsInOrderSet; i++) {
      resultList.add(prepareResourceList(secResList.get(i), 
          apptCatList.get(i), deptList.get(i),centerId));
    }
    return resultList;
  }
  
  /**
   * Prepare resource list.
   *
   * @param secRes the sec res
   * @param apptCat the appt cat
   * @param dept the dept
   * @param centerId the center id
   * @return the list
   */
  public List prepareResourceList(String secRes, AppointmentCategory apptCat, 
      String dept,int centerId) {
    List<Map> resList = getPrimResList(secRes, apptCat, dept,centerId);
    for (Map primRes : resList) {
      String primResId = (String) primRes.get("resource_id");
      int defaultDuration = apptCat.getSlotDurationOfPrimRes(primResId);
      primRes.put("defaultDuration", defaultDuration);
    }
    return resList;
  }

  /**
   * Gets the skip time.
   *
   * @param resList the res list
   * @return the skip time
   */
  public int getSkipTime(List<Map> resList) {
    // get hcf for jumping time
    int gcd = (int) (resList.get(0)).get("defaultDuration");
    BigInteger gcdB = BigInteger.valueOf(gcd);
    for (Map map : resList) {
      int duration = (int) map.get("defaultDuration");
      BigInteger b1 = BigInteger.valueOf(duration);
      gcdB = b1.gcd(gcdB);
    }
    return gcdB.intValue();
  }

  /**
   * Prepare appointment duration list.
   *
   * @param secResList the sec res list
   * @param apptCatList the appt cat list
   * @return the list
   */
  public List prepareAppointmentDurationList(List<String> secResList,
      List<AppointmentCategory> apptCatList) {
    // get appDurationList for a sec Res
    List resultList = new ArrayList();
    for (int i = 0; i < apptCatList.size(); i++) {
      resultList.add(getAppointmentDuration(apptCatList.get(i), secResList.get(i), null));
    }
    return resultList;
  }

  /**
   * Cancel bulk appointments.
   *
   * @param params the params
   * @return the map
   */
  public Map<String, Object> cancelBulkAppointments(Map<String, Object> params) {
    Map<String, Object> response = appointmentService.updateAppointmentsStatus(params);
    appointmentService.pushUpdateAppointmentStatusToRedis(response);
    return response;
  }

  /**
   * Edits the all bulk appointment.
   *
   * @param params the params
   * @return the list
   */
  public List editAllBulkAppointment(Map params) {
    List<Map<String, Object>> response = appointmentService.editBulkAppointment(params);
    List<Integer> appointmentIdForResponse = new ArrayList();
    List<String> appointmentCatForResponse = new ArrayList();
    boolean resourceChanged = false;
    for (Map<String, Object> appointmentObject : response) {
      Map<String, Object> appointmentsInfo = (Map<String, Object>) appointmentObject
          .get("appointment");
      if (appointmentsInfo != null) {
        appointmentIdForResponse.add((Integer) appointmentsInfo.get("appointment_id"));
        appointmentCatForResponse.add((String) appointmentsInfo.get("category"));
      } else {
        // this will be response format if the resource is changed
        List<Map<String, Object>> appointmentsList = (List<Map<String, Object>>) appointmentObject
            .get("appointments");
        for (Map<String, Object> appt : appointmentsList) {
          appointmentIdForResponse.add(((List<Integer>) appt.get("appointment_ids_list")).get(0));
          appointmentCatForResponse.add((String) appt.get("category"));
        }
        resourceChanged = true;
      }
    }
    if (resourceChanged) {
      List<Map<String, Object>> requestMapList = (List<Map<String, Object>>) params.get("values");
      for (Map<String, Object> request : requestMapList) {
        Map<String, Object> apptObj = (Map<String, Object>) request.get("appointments");
        Integer appointmentId = (Integer) apptObj.get("appointment_id");
        if (!appointmentIdForResponse.contains(appointmentId)) {
          appointmentIdForResponse.add(appointmentId);
          appointmentCatForResponse.add((String) apptObj.get("category"));
        }
      }
    }
    String apptCategories = StringUtil.join(appointmentCatForResponse, ",");
    String apptIds = StringUtil.join(appointmentIdForResponse, ",");
    redisMessagePublisher.publishMsgForSchema(RedisMessagePublisher.REDIS_APPOINTMENT_PUSH_CHANNEL,
        apptIds + ";" + apptCategories);
    return response;
  }

  /**
   * Arrive bulk appointment.
   *
   * @param params the params
   * @return the map
   */
  public Map<String, Object> arriveBulkAppointment(Map params) {
    List<Map> appointments = (List) params.get("appointments");
    String visitId = (String) params.get("visit_id");
    List<Integer> appointmentIdForResponse = new ArrayList();
    List<String> appointmentCatString = new ArrayList();
    List<AppointmentCategory> appointmentCatForResponse = new ArrayList();
    String[] apptIdArray = new String[appointments.size()]; 
    if (visitId == null) {
      for (Map appoitment : appointments) {
        appointmentService.setArrivedStatus(appoitment.get("appointment_id").toString(), visitId,
            appoitment.get("category").equals("DOC") ? appoitment.get("sec_res_id").toString()
                : null);
        appointmentIdForResponse.add((Integer) appoitment.get("appointment_id"));
        String category = (String) appoitment.get("category");
        appointmentCatForResponse
            .add(appointmentCategoryFactory.getInstance(category.toUpperCase(Locale.ENGLISH)));
      }
    } else {
      Map ordersMap = new HashMap();
      List docList = new ArrayList();
      List servList = new ArrayList();
      List testList = new ArrayList();
      Map appt = new HashMap();
      for (Map appointment : appointments) {
        String appointmentId = ((Integer)appointment.get("appointment_id")).toString();
        AppointmentCategory apptCat = appointmentCategoryFactory
            .getInstance(((String) appointment.get("category")).toUpperCase(Locale.ENGLISH));
        if (appointment.get("category").equals("DOC")) {
          appt = appointmentService.getAppointmentDetails(apptCat, Integer.parseInt(appointmentId));
        }
        Map prescriptionDetails = pendingPrescriptionsService
            .getPrescIdMapOfAppointment(appointmentId);
        Map tempMap = new HashMap();
        Long docPrescId = null;
        Integer preauthActId = null;
        Integer patPendingPrescId = null;
        String  preauthActStatus = null;
        // Added to make the pending prescriptions as ordered in the order service.
        if (prescriptionDetails != null && !prescriptionDetails.isEmpty()) {
          docPrescId = (Long)prescriptionDetails.get("patient_presc_id");
          preauthActId = (Long) prescriptionDetails.get("preauth_act_id") != null
            ? Integer.parseInt((prescriptionDetails.get("preauth_act_id")
            .toString())) : null;
          patPendingPrescId = (Long) prescriptionDetails.get("pat_pending_presc_id") != null
            ? Integer.parseInt(
            prescriptionDetails.get("pat_pending_presc_id").toString()) : null;
          preauthActStatus = prescriptionDetails.get("preauth_activity_status") != null
            ? prescriptionDetails.get("preauth_activity_status").toString() : null;
        }
        tempMap.put("pat_pending_presc_id", patPendingPrescId);
        tempMap.put("preauth_act_id", preauthActId);
        tempMap.put("preauth_activity_status", preauthActStatus);
        if (appointment.get("category").equals("DOC")) {
          tempMap.put("doctors_doc_presc_id", docPrescId);
          tempMap.put("doctors_prior_auth_item_id", preauthActId);
          tempMap.put("doctors_preauth_act_status", preauthActStatus);
          tempMap.put("doctors_pat_pending_presc_id", patPendingPrescId);
          tempMap.put("doctors_item_id", appointment.get("prim_res_id"));
          tempMap.put("doctors_multi_visit_package", false);
          String timeStr = (String) appointment.get("appointment_time");
          tempMap.put("doctors_start_date", (String) appointment.get("appointment_date") + " "
              + timeStr.substring(0, timeStr.length() - 3));
          tempMap.put("doctors_head", appointment.get("sec_res_id"));
          tempMap.put("doctors_remarks", "Scheduler Consultation");
          tempMap.put("doctors_prescribed_doctor_id", appointment.get("presc_doc"));
          tempMap.put("appointment_id", appointmentId);
          Map appment = (Map) appt.get("appointment"); 
          tempMap.put("visit_mode", appment.get("visit_mode"));
          docList.add(new HashMap(tempMap));
        } else if (appointment.get("category").equals("SNP")) {
          tempMap.put("services_doc_presc_id", docPrescId);
          tempMap.put("services_prior_auth_item_id", preauthActId);
          tempMap.put("services_preauth_act_status", preauthActStatus);
          tempMap.put("services_pat_pending_presc_id", patPendingPrescId);
          tempMap.put("services_item_id", appointment.get("sec_res_id"));
          tempMap.put("services_multi_visit_package", false);
          String timeStr = (String) appointment.get("appointment_time");
          tempMap.put("services_start_date", (String) appointment.get("appointment_date") + " "
              + timeStr.substring(0, timeStr.length() - 3));
          tempMap.put("services_prescribed_doctor_id", appointment.get("presc_doc"));
          tempMap.put("services_quantity", 1);
          tempMap.put("services_remarks", "Scheduler Service");
          servList.add(new HashMap(tempMap));
        } else if (appointment.get("category").equals("DIA")) {
          tempMap.put("tests_doc_presc_id", docPrescId);
          tempMap.put("tests_prior_auth_item_id", preauthActId);
          tempMap.put("tests_preauth_act_status", preauthActStatus);
          tempMap.put("tests_pat_pending_presc_id", patPendingPrescId);
          tempMap.put("tests_item_id", appointment.get("sec_res_id"));
          tempMap.put("tests_multi_visit_package", false);
          String timeStr = (String) appointment.get("appointment_time");
          tempMap.put("tests_start_date", (String) appointment.get("appointment_date") + " "
              + timeStr.substring(0, timeStr.length() - 3));
          tempMap.put("tests_prescribed_doctor_id", appointment.get("presc_doc"));
          tempMap.put("tests_quantity", 1);
          tempMap.put("tests_remarks", "Scheduler Test");
          testList.add(new HashMap(tempMap));
        }
      }
      ordersMap.put("doctors", docList);
      ordersMap.put("services", servList);
      ordersMap.put("others", new ArrayList());
      ordersMap.put("tests", testList);
      ordersMap.put("packages", new ArrayList());
      Map finalMap = new HashMap();
      finalMap.put("ordered_items", ordersMap);
      finalMap.put("visit_id", visitId);
      try {
        orderService.createOrdersByVisitIdAndItems(finalMap);
      } catch (ParseException exp) {
        logger.error("", exp);
      } catch (SQLException exp) {
        logger.error("", exp);
      } catch (IOException exp) {
        logger.error("", exp);
      } catch (NoSuchMethodException exp) {
        logger.error("", exp);
      } catch (IllegalAccessException exp) {
        logger.error("", exp);
      } catch (InvocationTargetException exp) {
        logger.error("", exp);
      }

      int inc = 0;
      for (Map appoitment : appointments) {
        appointmentService.setArrivedStatus(appoitment.get("appointment_id").toString(), visitId,
            appoitment.get("category").equals("DOC") ? appoitment.get("sec_res_id").toString()
                : null);
        appointmentIdForResponse.add((Integer) appoitment.get("appointment_id"));
        String category = (String) appoitment.get("category");
        appointmentCatString.add(category);
        appointmentCatForResponse
            .add(appointmentCategoryFactory.getInstance(category.toUpperCase(Locale.ENGLISH)));
        apptIdArray[inc++] = ((Integer) appoitment.get("appointment_id")).toString();
      }
      appointmentService.schedulePushEvent(apptIdArray, Events.APPOINTMENT_ARRIVED);
    }
    String apptCategories = StringUtil.join(appointmentCatString, ",");
    String apptIds = StringUtil.join(appointmentIdForResponse, ",");
    redisMessagePublisher.publishMsgForSchema(RedisMessagePublisher.REDIS_APPOINTMENT_PUSH_CHANNEL,
        apptIds + ";" + apptCategories);
    return params;

  }

  /**
   * Gets the available slots UTC.
   *
   * @param requestMap the request map
   * @return the available slots UTC
   */
  public Map<String, List<Object>> getAvailableSlotsUTC(Map<String, String[]> requestMap) {
    if (requestMap.get("appointment_slot") != null && requestMap.get("appointment_slot")[0] != null
        && !"".equals(requestMap.get("appointment_slot")[0])) {
      String appointmentDateTimeStr = (String) requestMap.get("appointment_slot")[0];
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
      requestMap.put("date",
          new String[] { DateUtil.formatDate(DateUtil.getDatePart(appointmentTimestamp)) });
      requestMap.put("time",
          new String[] { DateUtil.formatSQlTime(DateUtil.getTimePart(appointmentTimestamp)) });
    }
    return getAvailableSlots(requestMap);
  }

  /**
   * Save bulk appointments UTC.
   *
   * @param requestBody the request body
   * @return the list
   */
  public List<Map<String, Object>> saveBulkAppointmentsUTC(ModelMap requestBody) {
    List<Map<String, Object>> appointmentsArray = (List<Map<String, Object>>) requestBody
        .get("values");
    for (Map<String, Object> appointmentData : appointmentsArray) {
      Map<String, Object> appointment = (Map<String, Object>) appointmentData.get("appointments");
      if (appointment.get("appointment_slot") == null) {
        ValidationErrorMap errorMap = new ValidationErrorMap();
        errorMap.addError("appointment_slot", "exception.scheduler.appointment.invalid.date");
        ValidationException ex = new ValidationException(errorMap);
        Map<String, Object> nestedException = new HashMap<>();
        nestedException.put("appointment", ex.getErrors());
        throw new NestableValidationException(nestedException);
      }
      String appointmentDateTimeStr = (String) appointment.get("appointment_slot");
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
      appointment.put("date", DateUtil.formatDate(DateUtil.getDatePart(appointmentTimestamp)));
      appointment.put("slot_time",
          DateUtil.formatSQlTime(DateUtil.getTimePart(appointmentTimestamp)));
    }
    return saveAllBulkAppointments(requestBody);
  }

  /**
   * Gets the available slots for A single app.
   *
   * @param apptCatStr
   *          the appt cat str
   * @param secResId
   *          the sec res id
   * @param primResId
   *          the prim res id
   * @param deptId
   *          the dept id
   * @param centerId
   *          the center id
   * @param dateStr
   *          the date str
   * @return the available slots for A single app
   * @throws ParseException
   *           the parse exception
   */
  public Map getAvailableSlotsForASingleApp(String apptCatStr, String secResId, String primResId,
      String deptId, int centerId, String dateStr, Integer duration, String apptid, String isUtc,
      String slotTime) throws ParseException {

    AppointmentCategory apptCat = appointmentCategoryFactory
        .getInstance(apptCatStr.toUpperCase(Locale.ENGLISH));
    Integer appointmentDuration = null;
    if (duration != null) {
      appointmentDuration = duration;
    } else {
      appointmentDuration = getAppointmentDuration(apptCat, secResId, null);
    }
    DateUtil dateUtil = new DateUtil();
    Map resultMap = new HashMap();
    // resources list
    List<Map> resList = new ArrayList();
    int skipTime = 0;

    if (primResId != null) {
      // prepare the resMap and add to res list
      Map resMap = new HashMap();
      int defaultDuration = apptCat.getSlotDurationOfPrimRes(primResId);
      resMap.put("defaultDuration", defaultDuration);
      resMap.put("resource_id", primResId);
      resList.add(resMap);
      skipTime = defaultDuration;

    } else {
      // getting all the resources list
      resList = prepareResourceList(secResId, apptCat, deptId, centerId);
      // getting skipTime for jump
      if (resList.size() > 0) {
        skipTime = getSkipTime(resList);
      }
    }

    // this map contains all processed availabilities array
    Map processedResourceMap = new HashMap();

    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

    // iteration constants
    Timestamp startTime = dateUtil.parseTheTimestamp(dateStr + " " + "00:00");
    Timestamp stopTime = dateUtil.parseTheTimestamp(dateStr + " " + "23:59");
    Timestamp apptTime = new Timestamp(startTime.getTime());

    if (slotTime != null && !slotTime.equals("")) {
      startTime = dateUtil.parseTheTimestamp(dateStr + " " + slotTime);
      apptTime = new Timestamp(startTime.getTime());
    }
    while (apptTime.before(stopTime)) {
      String timeStr = sdf.format(apptTime);;
      if (isUtc != null && isUtc.equals("true")) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        java.util.Date date = df.parse(dateStr + " " + timeStr);
        timeStr = DateUtil.formatIso8601TimestampNoSec(date);
      }
      resultMap.put(timeStr, new ArrayList());
      for (Map primRes : resList) {

        Integer defaultDuration = (Integer) primRes.get("defaultDuration");
        primResId = (String) primRes.get("resource_id");

        Time tempTime = schedulerResourceSearchService.getNextSlotTime(sdf.format(apptTime),
            defaultDuration);
        if (tempTime == null) {
          continue;
        }
        String apptTimeStr = dateStr + " " + sdf.format(tempTime);
        if (dateUtil.parseTheTimestamp(apptTimeStr).equals(apptTime)) {
          Boolean appPossible = ifAppointmentPossible(primResId, apptTime, appointmentDuration,
              apptCat, centerId, dateStr, processedResourceMap, defaultDuration, apptid);
          List visitModeArray = (List)processedResourceMap.get("visitModeValues");
          if (appPossible) {
            int minutesIndex = getMinutes(apptTime) / defaultDuration;
            Map mapForResource = (Map) processedResourceMap.get(primResId
                + apptCat.getPrimaryResourceType() + dateStr);
            HashMap[] overBookInfoArr = (HashMap[]) mapForResource.get("overBookinfo");
            Map<String,Object> overBookInfoForSlot = overBookInfoArr[minutesIndex];
            Map newResmap = new HashMap();
            newResmap.putAll(primRes);
            newResmap.put("overbook_limit", overBookInfoForSlot.get("overbook_limit"));
            newResmap.put("booked",  overBookInfoForSlot.get("booked"));
            List listOfRes = (List) resultMap.get(timeStr);
            if (apptCatStr.equals("DOC")) {
              long timeMlSeconds = sdf.parse(timeStr).getTime();
              for (int i = 0; i < visitModeArray.size(); i++) {
                Map visitMap = (Map) visitModeArray.get(i);
                Time fromTime = (Time) visitMap.get("from_time");
                Time toTime = (Time) visitMap.get("to_time");
                long fromMlSeconds = fromTime.getTime();
                long toMlSeconds = toTime.getTime();
                if (timeMlSeconds >= fromMlSeconds && timeMlSeconds < toMlSeconds) {
                  newResmap.put("visit_mode", visitMap.get("visit_mode"));
                  break;
                }
              }
            }
            listOfRes.add(newResmap);
            resultMap.put(timeStr, listOfRes);
          }
        }
      }

      if (slotTime != null && !slotTime.equals("")) {
        break;
      }
      apptTime = addTimeToTimeStamp(apptTime, skipTime);
    }
    return resultMap;
  }

  /**
   * Gets the available slots for A single app.
   *
   * @param params
   *          the params
   * @return the available slots for A single app
   */
  public Map getAvailableSlotsForASingleApp(Map<String, String[]> params) {
    String apptCatStr = params.get("app_cat") != null ? params.get("app_cat")[0] : null;
    String secResId = params.get("sec_res") != null ? params.get("sec_res")[0] : null;
    String primResId = params.get("prim_res") != null ? params.get("prim_res")[0] : null;
    String deptId = params.get("dept") != null ? params.get("dept")[0] : null;
    int centerId = params.get("center_id") != null && !(params.get("center_id")).equals("") 
        ? Integer.parseInt(params.get("center_id")[0])
        : RequestContext.getCenterId();
    String dateStr = params.get("date") != null ? params.get("date")[0] : null;
    String apptid = params.get("appt_id") != null ? params.get("appt_id")[0] : null;
    String isUtc = params.get("is_utc") != null ? params.get("is_utc")[0] : null;
    Integer duration = params.get("duration") != null && !(params.get("duration")[0]).equals("") 
        ? Integer.parseInt(params.get("duration")[0])
        : null;
    String slotTime = params.get("slot_time") != null ? params.get("slot_time")[0] : null;
    try {
      return getAvailableSlotsForASingleApp(apptCatStr, secResId, primResId, deptId, centerId,
          dateStr,duration,apptid, isUtc, slotTime);
    } catch (Exception exp) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("params", "exception.missing.or.wrong.mandatory.fields");
      ValidationException ex = new ValidationException(errorMap);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      nestedException.put("appointments", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
  }

}
