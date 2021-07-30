package com.insta.hms.integration.book;

import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.security.SecurityService;
import com.insta.hms.core.patient.PatientDetailsService;
import com.insta.hms.core.scheduler.Appointment;
import com.insta.hms.core.scheduler.AppointmentCategory;
import com.insta.hms.core.scheduler.AppointmentCategoryFactory;
import com.insta.hms.core.scheduler.AppointmentResource;
import com.insta.hms.core.scheduler.AppointmentService;
import com.insta.hms.core.scheduler.ResourceDTO;
import com.insta.hms.core.scheduler.ResourceService;
import com.insta.hms.integration.InstaIntegrationService;
import com.insta.hms.integration.book.BookSDKUtil.DoctorTiming;
import com.insta.hms.mdm.appointmentsources.AppointmentSourceService;
import com.insta.hms.mdm.centerpreferences.CenterPreferencesService;
import com.insta.hms.mdm.centers.CenterService;
import com.insta.hms.mdm.departments.DepartmentService;
import com.insta.hms.mdm.doctors.DoctorService;
import com.insta.hms.redis.RedisMessagePublisher;
import com.practo.integration.sdk.AppointmentVisitEvent;
import com.practo.integration.sdk.SDKException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// TODO: Auto-generated Javadoc
/**
 * The Class BookIntegrationService.
 */
@Service
public class BookIntegrationService {
  
  /** The Constant logger. */
  private static final Logger logger = LoggerFactory.getLogger(BookIntegrationService.class);

  /** The doctor service. */
  @LazyAutowired
  private DoctorService doctorService;

  /** The department service. */
  @LazyAutowired
  private DepartmentService departmentService;

  /** The insta integration service. */
  @LazyAutowired
  private InstaIntegrationService instaIntegrationService;

  /** The resource service. */
  @LazyAutowired
  private ResourceService resourceService;

  /** The application context. */
  @LazyAutowired
  private ApplicationContext applicationContext;

  /** The generic preferences service. */
  @LazyAutowired
  private GenericPreferencesService genericPreferencesService;

  /** The center service. */
  @LazyAutowired
  private CenterService centerService;

  /** The center preferences service. */
  @LazyAutowired
  private CenterPreferencesService centerPreferencesService;

  /** The security service. */
  @LazyAutowired
  private SecurityService securityService;

  /** The patient details service. */
  @LazyAutowired
  private PatientDetailsService patientDetailsService;

  /** The appointment category factory. */
  @LazyAutowired
  private AppointmentCategoryFactory appointmentCategoryFactory;

  /** The appointment source service. */
  @LazyAutowired
  private AppointmentSourceService appointmentSourceService;

  /** The appointment service. */
  @LazyAutowired
  private AppointmentService appointmentService;

  /** The redis message publisher. */
  @LazyAutowired
  private RedisMessagePublisher redisMessagePublisher;

  /** The Constant TOTAL_PROCESSED. */
  private static final String TOTAL_PROCESSED = "total_processed";
  
  /** The Constant FAILED. */
  private static final String FAILED = "failed";

  /** The Constant WEEKLY_AVAILABILITY_NOT_FOUND. */
  private static final String WEEKLY_AVAILABILITY_NOT_FOUND =
      "Weekly availability is not found for doctor %s for selected center"
      + ". Please add it and re-try ";
  
  /**
   * This is the first call that has to be made from any center to initiate the Practo Integration.
   * We create a new callback instance for each center and pass it to Practo SDK and any events that
   * happen to that center from Practo side like Appointment is created, Doctor information is
   * changed etc. will be notified to HMS by that callback instance.
   *
   * @param establishmentKey          - The key which uniquely identifies the center
   * @param centerId          - The center for which Practo integration has to be initiated
   * @throws SDKException the SDK exception
   */
  public void connectEstablishmentToPracto(String establishmentKey, int centerId)
      throws SDKException {

    // Get the new Callback instance for each center
    BookSDKCallback bookSDKCallback = applicationContext.getBean(BookSDKCallback.class,
        RequestContext.getSchema(), centerId);

    BasicDynaBean integrationRecord = instaIntegrationService
        .getActiveRecord(BookSDKUtil.PRACTO_BOOK_INTEGRATION);
    int integrationId = (Integer) integrationRecord.get("integration_id");
    String applicationID = (String) integrationRecord.get("application_id");
    String applicationSecret = (String) integrationRecord.get("application_secret");
    String agentHost = (String) integrationRecord.get("agent_host");
    int agentPort = (Integer) integrationRecord.get("agent_port");
    BookSDKUtil.init(applicationID, applicationSecret, establishmentKey, bookSDKCallback, agentHost,
        agentPort);
    // Update the Integration details in DB
    Map<String, Object> filterMap = new HashMap<String, Object>();
    filterMap.put("integration_id", integrationId);
    filterMap.put("center_id", centerId);
    Map<String, Object> valuesMap = new HashMap<String, Object>();
    valuesMap.put("establishment_key", establishmentKey);
    valuesMap.put("integration_id", integrationId);
    valuesMap.put("center_id", centerId);

    instaIntegrationService.insertOrUpdateCenterDetials(filterMap, valuesMap);

    logger.info(String.format(
        "Connection established with Practo Book for a center: [%s]  with establishment Key"
        + " : [%s] successfully !", centerId, establishmentKey));
  }

  /**
   * Register the given list of Doctors in a given center.
   *
   * @param doctorIdList          - The list of doctorIds to be registered on Practo.com
   * @param centerId          - The centerId from which Doctors are being registered
   * @param updateDoctors the update doctors
   * @return The object having the result of bulk registrations
   */
  public Map<String, Object> registerDoctors(List<String> doctorIdList, int centerId,
      boolean updateDoctors) {
    Map<String, String> errors = new HashMap<String, String>();
    String establishmentKey = getCenterEstablishmentKey(centerId);
    for (String doctorId : doctorIdList) {

      Map<String, Object> doctorDetails = doctorService.getDoctorDetails(doctorId);

      if (doctorDetails == null) {
        errors.put(doctorId, String.format("Doctor %s does not exist", doctorId));
        continue;
      }
      String doctorName = (String) doctorDetails.get("doctor_name");
      Map<String, Object> departmentDetails = departmentService
          .getDepartmentDetails((String) doctorDetails.get("dept_id"));

      List<DoctorTiming> doctorTimings = getDoctorTimings(doctorId, centerId);
      if (doctorTimings == null || doctorTimings.isEmpty()) {
        errors.put(doctorId, String.format(WEEKLY_AVAILABILITY_NOT_FOUND, doctorName));
        continue;
      }

      int slotDurationInMinutes = getSlotDurationOfDoctor(doctorId);

      String departmentName = (departmentDetails != null)
          ? (String) departmentDetails.get("dept_name")
          : null;

      try {
        if (!updateDoctors) {
          BookSDKUtil.registerDoctor(doctorDetails, departmentName, slotDurationInMinutes,
              doctorTimings, establishmentKey, centerId);
          doctorService.updateDoctorStatusOnPracto(doctorId, centerId,
              DoctorService.REGISTRATION_PENDING);
        } else {
          BookSDKUtil.updateDoctorDetails(doctorDetails, doctorTimings, establishmentKey);
        }
      } catch (SDKException ex) {
        if (!updateDoctors) {
          logger.error(String.format("Registering DoctorId :[%s], centerId: [%d] failed ", doctorId,
              centerId), ex);
        } else {
          logger.error(
              String.format("Updating DoctorId :[%s], centerId: [%d] failed ", doctorId, centerId),
              ex);
          errors.put(doctorId, BookSDKUtil.getErrorMessage(ex.getErrorCode()));
        }
      }
    }
    Map<String, Integer> meta = new HashMap<String, Integer>();
    meta.put(TOTAL_PROCESSED, (doctorIdList == null) ? 0 : doctorIdList.size());
    meta.put(FAILED, errors.size());
    meta.put("registered", meta.get(TOTAL_PROCESSED) - meta.get(FAILED));

    Map<String, Object> result = new HashMap<String, Object>();
    result.put("result", meta);
    result.put("errors", errors);
    return result;
  }

  /**
   * Gets the center establishment key.
   *
   * @param centerId the center id
   * @return the center establishment key
   */
  public String getCenterEstablishmentKey(int centerId) {
    if (instaIntegrationService.getCenterIntegrationDetails(BookSDKUtil.PRACTO_BOOK_INTEGRATION,
        centerId) != null
        && instaIntegrationService
            .getCenterIntegrationDetails(BookSDKUtil.PRACTO_BOOK_INTEGRATION, centerId)
            .get("establishment_key") != null) {
      return (String) instaIntegrationService
          .getCenterIntegrationDetails(BookSDKUtil.PRACTO_BOOK_INTEGRATION, centerId)
          .get("establishment_key");
    } else {
      return null;
    }
  }

  /**
   * Checks if is practo advantage enabled.
   *
   * @return true, if is practo advantage enabled
   */
  public boolean isPractoAdvantageEnabled() {
    return securityService.getActivatedModules().contains(BookSDKUtil.MODULE_ID);
  }

  /**
   * Get the Weekly availabilities of Doctor. Check the resource for given doctor in the given
   * center. If it is not available, then look for the ALL doctors group. For the single centered
   * schema, If the resource availability is not found for a particular doctor, We will get resource
   * availabilities for "*" resource Type.
   *
   * @param doctorId          - The doctor for which Weekly timings are to be fetched
   * @param centerId          - The center for which the details are required
   * @return the doctor timings
   */
  private List<DoctorTiming> getDoctorTimings(String doctorId, int centerId) {
    List<DoctorTiming> doctorTimings = new ArrayList<DoctorTiming>();
    for (int dayOfWeek = 0; dayOfWeek < 7; dayOfWeek++) {
      int centersIncDefault = (Integer) genericPreferencesService.getAllPreferences()
          .get("max_centers_inc_default");
      List<BasicDynaBean> resourceAvailabilities = resourceService
          .getDefaultResourceAvailabilities(doctorId, dayOfWeek, "DOC", "A", centerId);
      if ((resourceAvailabilities == null || resourceAvailabilities.isEmpty())
          && centersIncDefault == 1) {
        // Check if weekly availability is defined for this doctor
        List<BasicDynaBean> resourceUnAvailabilites = resourceService
            .getDefaultResourceAvailabilities(doctorId, dayOfWeek, "DOC", "N", null);
        if (resourceUnAvailabilites == null || resourceUnAvailabilites.isEmpty()) {
          // Weekly availability is not defined for this doctor, So
          // use the weekly availability of default Doctor consultation
          resourceAvailabilities = resourceService.getDefaultResourceAvailabilities("*", dayOfWeek,
              "DOC", "A", centerId);
        }
      }
      if (resourceAvailabilities != null) {
        for (BasicDynaBean resourceAvailability : resourceAvailabilities) {
          Time fromTime = (Time) resourceAvailability.get("from_time");
          Time endTime = (Time) resourceAvailability.get("to_time");
          endTime = new Time(endTime.getTime() - 1 * 60 * 1000);
          SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
          DoctorTiming doctorTiming = new DoctorTiming(
              (Integer) resourceAvailability.get("day_of_week"), sdf.format(fromTime),
              sdf.format(endTime));
          doctorTimings.add(doctorTiming);
        }
      }
    }
    return doctorTimings;

  }

  /**
   * Get the Default slot duration for an appointment of a doctor.
   *
   * @param doctorId the doctor id
   * @return the slot duration of doctor
   */
  private int getSlotDurationOfDoctor(String doctorId) {
    BasicDynaBean resourceBean = resourceService.getDefaultAttributesOfResource("DOC", doctorId);
    if (resourceBean == null) {
      resourceBean = resourceService.getDefaultAttributesOfResource("DOC", "*");
    }
    return (Integer) resourceBean.get("default_duration");

  }

  /**
   * Gets the reference data.
   *
   * @return the reference data
   */
  public Map<String, List<BasicDynaBean>> getReferenceData() {
    Map<String, List<BasicDynaBean>> map = new HashMap<String, List<BasicDynaBean>>();
    map.put("centers", centerService.lookup(true));
    // get the center integration details
    Map<String, Object> filterMap = new HashMap<String, Object>();
    filterMap.put("integration_id", instaIntegrationService
        .getActiveRecord(BookSDKUtil.PRACTO_BOOK_INTEGRATION).get("integration_id"));
    List<BasicDynaBean> centerIntegrationPrefs = instaIntegrationService
        .centerDetailsLookup(filterMap);
    map.put("integration_preferences", centerIntegrationPrefs);
    return map;
  }

  /**
   * Gets the center preferences.
   *
   * @param centerId the center id
   * @return the center preferences
   */
  public Map<String, List<BasicDynaBean>> getCenterPreferences(Integer centerId) {
    Map<String, List<BasicDynaBean>> map = new HashMap<String, List<BasicDynaBean>>();
    BasicDynaBean centerPrefs = centerPreferencesService.getCenterPreferences(centerId);
    List<BasicDynaBean> centerPrefsList = new ArrayList<BasicDynaBean>();
    centerPrefsList.add(centerPrefs);
    map.put("center_preferences", centerPrefsList);
    return map;
  }

  /**
   * Checks if is book enabled.
   *
   * @param resId the res id
   * @param centerId the center id
   * @return true, if is book enabled
   */
  public boolean isBookEnabled(String resId, int centerId) {
    String doctorStatusOnPracto = null;
    BasicDynaBean doctorCenterBean = doctorService.getDoctorCenter(resId, centerId);
    if (doctorCenterBean != null && doctorCenterBean.get("status_on_practo") != null) {
      doctorStatusOnPracto = (String) doctorCenterBean.get("status_on_practo");
    }
    if (doctorStatusOnPracto != null && Arrays
        .asList(BookSDKUtil.CALENDAR_SYNC_CALLBACK_RECEIVED.toLowerCase(),
            BookSDKUtil.PRACTO_BOOK_ENABLED.toLowerCase(),
            BookSDKUtil.PRACTO_DOCTOR_LISTED.toLowerCase())
        .contains(doctorStatusOnPracto.toLowerCase())) {
      return true;
    }
    return false;
  }

  /**
   * Push appointment event to practo.
   *
   * @param appt the appt
   * @param event the event
   */
  public void pushAppointmentEventToPracto(List<BasicDynaBean> appt, String event) {
    BasicDynaBean apptBean = appt.get(0);
    Integer appointmentId = (Integer) (apptBean.get("appointment_id"));
    logger.info("updating " + appointmentId + " with status " + event);
    Integer centerId = (Integer) apptBean.get("center_id");
    if ((Integer) apptBean.get("res_sch_id") == 1) {
      if (isBookEnabled((String) apptBean.get("prim_res_id"), centerId)) {
        String establishmentKey = getCenterEstablishmentKey((Integer) apptBean.get("center_id"));
        if (event.equalsIgnoreCase("Arrived")) {
          BookSDKUtil.notifyAppointmentVisitEventToPracto(establishmentKey, centerId,
              appointmentId.toString(), AppointmentVisitEvent.PATIENT_CHECKIN);
        } else if (event.equalsIgnoreCase("Noshow")) {
          BookSDKUtil.notifyAppointmentVisitEventToPracto(establishmentKey, centerId,
              appointmentId.toString(), AppointmentVisitEvent.PATIENT_NO_SHOW);
        }
      }
    } else {
      // Secondary resources
      for (BasicDynaBean bean : appt) {
        if (((String) bean.get("resource_type")).contains("DOC")) {
          // secondary doctor
          String doctorId = (String) bean.get("resource_id");
          if (!isBookEnabled(doctorId, centerId)) {
            continue;
          }
          String establishmentKey = getCenterEstablishmentKey((Integer) apptBean.get("center_id"));
          String appointmentIdStr = String.valueOf(appointmentId) + "-" + doctorId;
          if (event.equalsIgnoreCase("Arrived")) {
            BookSDKUtil.notifyAppointmentVisitEventToPracto(establishmentKey, centerId,
                appointmentIdStr, AppointmentVisitEvent.PATIENT_CHECKIN);
          } else if (event.equalsIgnoreCase("Noshow")) {
            BookSDKUtil.notifyAppointmentVisitEventToPracto(establishmentKey, centerId,
                appointmentIdStr, AppointmentVisitEvent.PATIENT_NO_SHOW);
          }
        }
      }
    }
  }


  /*
   * protected void addDoctorAppointmentsToPracto(BasicDynaBean appointment, boolean
   * isAppointmentCreated, List<ResourceDTO> resourceDeleteList) { int appointmentId = (Integer)
   * appointment.get("appointment_id"); int centerId = (Integer) appointment.get("center_id");
   * String establishmentKey = getCenterEstablishmentKey(centerId); if (establishmentKey == null) {
   * return; } Timestamp apptStartTime = (Timestamp) appointment.get("appointment_time"); int
   * apptDuration = (Integer) appointment.get("duration"); String mrNo = (String)
   * appointment.get("mr_no"); String patientName = (String) appointment.get("patient_name"); String
   * phoneNo = (String) appointment.get("patient_contact"); String apptStatus = (String)
   * appointment.get("appointment_status"); String emailId = null; BasicDynaBean centerPreferences =
   * centerPreferencesService.getCenterPreferences(centerId); PatientDTO patient = null; boolean
   * sharePatientDetails = (centerPreferences.get("share_pat_details_to_practo") != null) ?
   * (Boolean) centerPreferences.get("share_pat_details_to_practo") : false; if
   * (sharePatientDetails) { // Share the Patient details to Practo if (mrNo != null &&
   * !mrNo.isEmpty()) { BasicDynaBean patDetails = patientDetailsService.getPatientMailId(mrNo);
   * emailId = (String) patDetails.get("email_id"); }patient = new PatientDTO(patientName, emailId,
   * phoneNo, mrNo); } else { patient = new PatientDTO(); } }
   */

  /**
   * Save practo appointment.
   *
   * @param practoAppointmentId the practo appointment id
   * @param resourceId the resource id
   * @param appointmentStartTime the appointment start time
   * @param appointmentEndTime the appointment end time
   * @param patient the patient
   * @param appointmentStatus the appointment status
   * @param centerId the center id
   * @return the integer
   */
  public Integer savePractoAppointment(String practoAppointmentId, String resourceId,
      Timestamp appointmentStartTime, Timestamp appointmentEndTime, PatientDTO patient,
      String appointmentStatus, int centerId) {
    // Create the doctor appointment category object as till now only doctor appointments can be
    // booked from practo.
    // Populate the Map to structure the appointment data into a way AppointmentService needs it.
    Map<String, Object> patientMap = new HashMap<String, Object>();
    patientMap.put("patient_name", patient.getName());
    patientMap.put("patient_contact", patient.getPhoneNo());
    patientMap.put("scheduler_visit_type", "M");
    Map<String, Object> apptMap = new HashMap<String, Object>();
    apptMap.put("patient", patientMap);
    AppointmentCategory apptCategory = appointmentCategoryFactory.getInstance("DOC");
    String category = apptCategory.getCategory();
    Map<String, Object> appointmentMap = new HashMap<String, Object>();
    appointmentMap.put("category", category);
    appointmentMap.put("primary_resource_id", resourceId);
    appointmentMap.put("secondary_resource_id", "-1");
    appointmentMap.put("center_id", centerId);
    appointmentMap.put("duration",
        (int) ((appointmentEndTime.getTime() - appointmentStartTime.getTime()) / (1000 * 60)));
    appointmentMap.put("date", DateUtil.formatDate(appointmentStartTime));
    appointmentMap.put("slot_time", DateUtil.formatTime(appointmentStartTime));
    appointmentMap.put("status", appointmentStatus);
    List<BasicDynaBean> list = appointmentSourceService
        .getAppointmentSourceIdForSourceName("Practo");
    int apptSourceId = -1;
    if (list != null && list.size() > 0) {
      apptSourceId = (Integer) ((BasicDynaBean) list.get(0)).get("appointment_source_id");
    }
    appointmentMap.put("app_source_id", apptSourceId);
    appointmentMap.put("practo_appointment_id", practoAppointmentId);
    Map<String, Object> additionalInfoMap = new HashMap<String, Object>();
    additionalInfoMap.put("email_id", patient.getEmailId());
    apptMap.put("additional_info", additionalInfoMap);

    List<Map> appointmentsData = new ArrayList<Map>();
    appointmentsData.add(appointmentMap);
    apptMap.put("appointments", appointmentsData);

    List appCategoryList = new ArrayList<AppointmentCategory>();
    appCategoryList.add(apptCategory);

    Map<String, Object> appt = appointmentService.createNewAppointments(appCategoryList, apptMap);
    if (appt != null) {
      List<Map<String, Object>> appointmentsInfo = (List<Map<String, Object>>) appt
          .get("appointments");
      List<Integer> apptIds = (List<Integer>) ((Map<String, Object>) appointmentsInfo.get(0))
          .get("appointment_ids_list");
      redisMessagePublisher.publishMsgForSchema(
          RedisMessagePublisher.REDIS_APPOINTMENT_PUSH_CHANNEL, apptIds.get(0) + ";" + category);
      return apptIds.get(0);
    }
    return 0;
  }

  /**
   * Adds the doctor appointments to practo.
   *
   * @param appointment the appointment
   * @param appointmentResourceList the appointment resource list
   * @param recurrenceList the recurrence list
   * @param isAppointmentCreated the is appointment created
   * @param resourceDeleteList the resource delete list
   */
  public void addDoctorAppointmentsToPracto(Appointment appointment,
      List<AppointmentResource> appointmentResourceList,
      List<Map<String, Timestamp>> recurrenceList, boolean isAppointmentCreated,
      List<ResourceDTO> resourceDeleteList) {
    int appointmentId = (Integer) appointment.getAppointmentId();
    int centerId = (Integer) appointment.getCenterId();
    String establishmentKey = getCenterEstablishmentKey(centerId);
    if (establishmentKey == null) {
      return;
    }
    Timestamp apptStartTime = (Timestamp) appointment.getAppointmentTime();
    int apptDuration = (Integer) appointment.getAppointmentDuration();
    String mrNo = (String) appointment.getMrNo();
    String patientName = (String) appointment.getPatientName();
    String phoneNo = (String) appointment.getPhoneNo();
    String apptStatus = (String) appointment.getAppointStatus();
    String emailId = null;
    BasicDynaBean centerPreferences = centerPreferencesService.getCenterPreferences(centerId);
    PatientDTO patient = null;
    boolean sharePatientDetails = (centerPreferences.get("share_pat_details_to_practo") != null)
        ? (Boolean) centerPreferences.get("share_pat_details_to_practo")
        : false;
    if (sharePatientDetails) {
      // Share the Patient details to Practo
      if (mrNo != null && !mrNo.isEmpty()) {
        BasicDynaBean patDetails = patientDetailsService.getPatientMailId(mrNo);
        emailId = (String) patDetails.get("email_id");
      }
      patient = new PatientDTO(patientName, emailId, phoneNo, mrNo);
    } else {
      patient = new PatientDTO();
    }
    for (AppointmentResource apptResource : appointmentResourceList) {
      if (((String) apptResource.getResourceType()).contains("DOC")) {
        // resource is a doctor
        String doctorId = (String) apptResource.getResourceId();
        if (!isBookEnabled(doctorId, centerId)) {
          continue;
        }
        String appointmentIdStr = null;
        if (recurrenceList != null && recurrenceList.size() > 0) {
          for (Map<String, Timestamp> recurrenceInfo : recurrenceList) {
            Set<String> key = recurrenceInfo.keySet();
            appointmentIdStr = key.iterator().next();
            if (!((String) apptResource.getResourceType()).equalsIgnoreCase("OPDOC")) {
              appointmentIdStr = appointmentIdStr + "-" + doctorId;
            }
            apptStartTime = recurrenceInfo.get(appointmentIdStr);
            addAppointmentToPracto(centerId, establishmentKey, appointmentIdStr, doctorId,
                apptStartTime, apptDuration, apptStatus, patient, isAppointmentCreated);
          }
        } else {
          appointmentIdStr = String.valueOf(appointmentId);
          if (!((String) apptResource.getResourceType()).equalsIgnoreCase("OPDOC")) {
            appointmentIdStr = appointmentIdStr + "-" + doctorId;
          }
          addAppointmentToPracto(centerId, establishmentKey, appointmentIdStr, doctorId,
              apptStartTime, apptDuration, apptStatus, patient, isAppointmentCreated);
        }
      }
    }
    if (resourceDeleteList == null) {
      return;
    }
    // No recurrence in such case as this list will be available only in case of appointment update
    // and recurrent appointments update is not
    // implemented in scheduler as of now.
    for (ResourceDTO resourceDTO : resourceDeleteList) {
      if (resourceDTO.getResourceType().contains("DOC")) {
        String doctorId = resourceDTO.getResourceId();
        if (!isBookEnabled(doctorId, centerId)) {
          continue;
        }
        String appointmentIdStr = String.valueOf(appointmentId) + "-" + doctorId;

        addAppointmentToPracto(centerId, establishmentKey, appointmentIdStr, doctorId,
            apptStartTime, apptDuration, BookSDKUtil.APPOINTMENT_CANCELLED, patient,
            isAppointmentCreated);

      }

    }
  }

  /**
   * Adds the appointment to practo.
   *
   * @param centerId the center id
   * @param establishmentKey the establishment key
   * @param appointmentId the appointment id
   * @param doctorId the doctor id
   * @param apptStartTime the appt start time
   * @param apptDuration the appt duration
   * @param apptStatus the appt status
   * @param patient the patient
   * @param isAppointmentCreated the is appointment created
   */
  private static void addAppointmentToPracto(int centerId, String establishmentKey,
      String appointmentId, String doctorId, Timestamp apptStartTime, int apptDuration,
      String apptStatus, PatientDTO patient, boolean isAppointmentCreated) {
    if (apptStatus.equalsIgnoreCase("Noshow")) {
      BookSDKUtil.notifyAppointmentVisitEventToPracto(establishmentKey, centerId, appointmentId,
          AppointmentVisitEvent.PATIENT_NO_SHOW);
      return;
    }
    if (apptStatus.equalsIgnoreCase("Arrived")) {
      BookSDKUtil.notifyAppointmentVisitEventToPracto(establishmentKey, centerId, appointmentId,
          AppointmentVisitEvent.PATIENT_CHECKIN);
      return;
    }
    if (apptStatus.equalsIgnoreCase("Cancel")) {
      apptStatus = BookSDKUtil.APPOINTMENT_CANCELLED;
    } else {
      apptStatus = BookSDKUtil.APPOINTMENT_CONFIRMED;
    }
    logger.info(String.format(
        "Pushing [%s] appointment to Practo. Details: [%n] centerId: [%d], appointmentId: [%s],"
        + " doctorId:[%s], startTime: [%s], Duration: [%d], Appt status: [%s],"
        + " Patient Details: [%s] ", isAppointmentCreated ? "Created" : "Updated", centerId,
            appointmentId, doctorId, apptStartTime, apptDuration, apptStatus, patient));
    if (isAppointmentCreated) {
      BookSDKUtil.addAppointment(establishmentKey, appointmentId, doctorId, apptStartTime,
          apptDuration, apptStatus, patient);
    } else {
      BookSDKUtil.updateAppointment(establishmentKey, appointmentId, apptStartTime, apptDuration,
          apptStatus, patient);
    }
  }
}