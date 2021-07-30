package com.insta.hms.core.scheduler;

import com.bob.hms.common.DateUtil;
import com.insta.hms.batchjob.pushevent.Events;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.security.SecurityService;
import com.insta.hms.core.clinical.order.operationitems.OperationOrderItemService;
import com.insta.hms.core.clinical.order.serviceitems.ServiceOrderItemService;
import com.insta.hms.core.clinical.order.testitems.TestOrderItemService;
import com.insta.hms.core.patient.outpatientlist.PatientSearchService;
import com.insta.hms.integration.book.BookIntegrationService;
import com.insta.hms.integration.book.BookSDKUtil;
import com.insta.hms.integration.book.PatientDTO;
import com.insta.hms.mdm.doctors.DoctorService;
import com.insta.hms.mdm.resourceavailability.ResourceAvailabilityService;
import com.insta.hms.mdm.salutations.SalutationService;
import com.insta.hms.redis.RedisMessagePublisher;
import com.insta.hms.resourcescheduler.ResourceBO.AppointMentResource;
import com.insta.hms.resourcescheduler.ResourceBO.Appointments;
import com.insta.hms.resourcescheduler.ResourceDTO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class ResourceService.
 */
@Service
public class ResourceService {

  /** The Constant logger. */
  private static final Logger logger = LoggerFactory.getLogger(ResourceService.class);

  /** The resource repository. */
  @LazyAutowired
  private ResourceRepository resourceRepository;

  /** The generic preferences service. */
  @LazyAutowired
  private GenericPreferencesService genericPreferencesService;

  /** The salutation service. */
  @LazyAutowired
  private SalutationService salutationService;

  /** The test order item service. */
  @LazyAutowired
  private TestOrderItemService testOrderItemService;

  /** The service order item service. */
  @LazyAutowired
  private ServiceOrderItemService serviceOrderItemService;

  /** The operation order item service. */
  @LazyAutowired
  private OperationOrderItemService operationOrderItemService;

  /** The security service. */
  @LazyAutowired
  private SecurityService securityService;

  /** The book integration service. */
  @LazyAutowired
  private BookIntegrationService bookIntegrationService;

  /** The doctor service. */
  @LazyAutowired
  private DoctorService doctorService;

  /** The patient search service. */
  @LazyAutowired
  private PatientSearchService patientSearchService;
  
  /** The appointment service. */
  @LazyAutowired
  private AppointmentService appointmentService;
  
  /** The resource availability service. */
  @LazyAutowired
  private ResourceAvailabilityService resourceAvailabilityService;
  
  /** The appointment category factory. */
  @LazyAutowired
  private AppointmentCategoryFactory appointmentCategoryFactory;
  
  /** The redis message publisher. */
  @LazyAutowired
  RedisMessagePublisher redisMessagePublisher;

  /** The Constant PRACTO_APPOINTMENT_SOURCE. */
  private static final String PRACTO_APPOINTMENT_SOURCE = "Practo";

  /**
   * Gets the default resource availabilities.
   *
   * @param resourceId the resource id
   * @param dayOfWeek the day of week
   * @param resourceType the resource type
   * @param availabilityStatus the availability status
   * @param centerId the center id
   * @return the default resource availabilities
   */
  public List<BasicDynaBean> getDefaultResourceAvailabilities(String resourceId, int dayOfWeek,
      String resourceType, String availabilityStatus, Integer centerId) {

    int centersIncDefault = (Integer) genericPreferencesService.getAllPreferences().get(
        "max_centers_inc_default");
    return resourceRepository.getDefaultResourceAvailabilities(resourceId, dayOfWeek, resourceType,
        availabilityStatus, centerId, centersIncDefault);

  }

  /**
   * Gets the default attributes of resource.
   *
   * @param category the category
   * @param resourceId the resource id
   * @return the default attributes of resource
   */
  public BasicDynaBean getDefaultAttributesOfResource(String category, String resourceId) {
    return resourceRepository.getDefaultAttributesOfResource(category, resourceId);
  }

  /**
   * Gets the resource appointments.
   *
   * @param resourceId the resource id
   * @param resourceType the resource type
   * @param centerId the center id
   * @param startTime the start time
   * @param endTime the end time
   * @param incPatientDetails the inc patient details
   * @return the resource appointments
   */
  public List<BasicDynaBean> getResourceAppointments(String resourceId, String resourceType,
      Integer centerId, Timestamp startTime, Timestamp endTime, boolean incPatientDetails) {
    return resourceRepository.getResourceAppointments(resourceId, resourceType, centerId,
        startTime, endTime, incPatientDetails);
  }

  /**
   * Checks if the resource is available in a given center for the given time [startTime to
   * endTime]. We get list of all the available resources for the date of startTime or endTime (as
   * both of them will have same date value) If for any available resource , If <i> startTime </i>
   * lies in the range of [resource's startTime, resoure's endTime ), true is returned. If none of
   * available resources meet above condition, false is returned.
   * NOTE : startTime and endTime are assumed to belong to same date
   *
   * @param resourceId          - The unique identifier of resource
   * @param resourceType          - The type of resource . Eg: Doctor, Operation theaters
   * @param startTime          - The start Time from which the resource is needed
   * @param endTime          - The end Time upto which the resource is needed
   * @param centerId          - The center for which resource availability is to be checked
   * @return true, if is resource available
   * @throws ParseException the parse exception
   */
  public boolean isResourceAvailable(String resourceId, String resourceType, Timestamp startTime,
      Timestamp endTime, int centerId) throws ParseException {
    // As the startTime and endTime belong to same date, use either of them.
    Date date = new Date(startTime.getTime());

    List<BasicDynaBean> resourceAvailabilities = getResourceAvailableTimings(resourceId,
        resourceType, date, centerId);
    if (resourceAvailabilities != null) {
      for (BasicDynaBean resourceAvailability : resourceAvailabilities) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        String fromTime = dateFormat.format((Time) resourceAvailability.get("from_time"));
        String toTime = dateFormat.format((Time) resourceAvailability.get("to_time"));
        Timestamp availabilityStartTime = DateUtil.parseTimestamp(DateUtil.formatDate(date),
            fromTime);
        Timestamp availabilityEndTime = DateUtil.parseTimestamp(DateUtil.formatDate(date), toTime);
        // Check if the startTime lies in the range [availabilityStartTime, availabilityEndTime)
        if (startTime.getTime() >= availabilityStartTime.getTime()
            && startTime.getTime() < availabilityEndTime.getTime()) {
          return true;
        }

      }
    }
    return false;
  }

  /**
   * Get the Availability of a resource for a given date. We first check if any overrides exists for
   * given date. If no overrides exist then we look for Weekly Availability for the date's
   * dayOfWeek. If the weekly availability for a a particular resource is not found and if it a
   * single center schema, Then we also consider the Weekly availability for ANY group for the given
   * resourceType
   *
   * @param resourceId          - Unique identifier of the resource
   * @param resourceType          - The type of resource . Eg: Doctors
   * @param date          - The date for which availabilities are to be fetched
   * @param centerId          - The center for which resource are to be fetched
   * @return the resource available timings
   */
  public List<BasicDynaBean> getResourceAvailableTimings(String resourceId, String resourceType,
      Date date, Integer centerId) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    int centersIncDefault = (Integer) genericPreferencesService.getAllPreferences().get(
        "max_centers_inc_default");
    List<BasicDynaBean> list = resourceRepository.getResourceOverrides(resourceType, date, date,
        resourceId, null, null, centersIncDefault);
    int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
    if (list == null || list.isEmpty()) {
      list = resourceRepository.getDefaultResourceAvailabilities(resourceId, dayOfWeek,
          resourceType, null, null, centersIncDefault);
    }
    if (list == null || list.isEmpty()) {
      list = resourceRepository.getDefaultResourceAvailabilities("*", dayOfWeek, resourceType,
          null, null, centersIncDefault);
    }
    List<BasicDynaBean> availabilities = new ArrayList<BasicDynaBean>();
    for (BasicDynaBean resBean : list) {
      if (resBean.get("center_id") != null) {
        if (resBean.get("availability_status").equals("A")
            && (centerId == null || resBean.get("center_id").equals(centerId))) {
          availabilities.add(resBean);
        }
      } else {
        if ((resBean.get("availability_status").equals("A")) 
            && (centerId == null || centerId == 0)) {
          availabilities.add(resBean);
        }

      }
    }
    return availabilities;

  }

  /**
   * Gets the resource overrides.
   *
   * @param resourceId the resource id
   * @param resourceType the resource type
   * @param fromDate the from date
   * @param endDate the end date
   * @param availabilityStatus the availability status
   * @param centerId the center id
   * @return the resource overrides
   */
  public List<BasicDynaBean> getResourceOverrides(String resourceId, String resourceType,
      Date fromDate, Date endDate, String availabilityStatus, Integer centerId) {
    int centersIncDefault = (Integer) genericPreferencesService.getAllPreferences().get(
        "max_centers_inc_default");
    return resourceRepository.getResourceOverrides(resourceType, fromDate, endDate, resourceId,
        availabilityStatus, centerId, centersIncDefault);
  }

  /**
   * Adds the practo appointment.
   *
   * @param practoAppointmentId the practo appointment id
   * @param doctorId the doctor id
   * @param appointmentStartTime the appointment start time
   * @param appointmentEndTime the appointment end time
   * @param patientDetails the patient details
   * @param appointmentStatus the appointment status
   * @param centerId the center id
   * @return the int
   */
  // TODO:: Where should the patientId of Practo patient, email, appt status be stored
  @Transactional(rollbackFor = Exception.class)
  public int addPractoAppointment(String practoAppointmentId, String doctorId,
      Timestamp appointmentStartTime, Timestamp appointmentEndTime, PatientDTO patientDetails,
      String appointmentStatus, Integer centerId) {

    int appointmentId = resourceRepository.getNextAppointmentId();
    Appointments appointment = new Appointments(appointmentId);
    appointment.setAppointmentDuration((int) ((appointmentEndTime.getTime() - appointmentStartTime
        .getTime()) / (1000 * 60)));
    appointment.setAppointmentTime(appointmentStartTime);
    appointment.setUnique_appt_ind(resourceRepository.getNextUniqueAppointment());
    appointment.setPrim_res_id(doctorId);
    appointment.setAppointmentId(appointmentId);
    appointment.setPatientName(patientDetails.getName());
    appointment.setScheduleName(doctorId);
    appointment.setAppointStatus("Confirmed");
    appointment.setSchedulerVisitType("M");
    appointment.setCenterId(centerId);
    appointment.setBookedTime(DateUtil.getCurrentTimestamp());
    appointment.setScheduleId(1);
    appointment.setPractoAppointmentId(practoAppointmentId);
    appointment.setPhoneNo(patientDetails.getPhoneNo());
    appointment.setBookedBy("InstaAdmin");
    appointment.setApp_source_id((Integer) resourceRepository.getAppointmentSource(
        PRACTO_APPOINTMENT_SOURCE).get("appointment_source_id"));

    BasicDynaBean genpref = genericPreferencesService.getPreferences();
    if (appointment.getMrNo() == null || appointment.getMrNo().isEmpty()) {
      List<BasicDynaBean> matchingPatientList = patientSearchService.findPatientMatch(
          patientDetails.getName(), patientDetails.getPhoneNo(), true,
          (Integer) genpref.get("patient_name_match_distance"));
      // updating mr_no for existing patients.
      if (matchingPatientList.size() == 1) {
        appointment.setMrNo((String) matchingPatientList.get(0).get("mr_no"));
      }
    }

    List<Appointments> scheduledAppointments = new ArrayList<Appointments>();
    scheduledAppointments.add(appointment);
    AppointMentResource appointmentResource = new AppointMentResource(appointmentId, "OPDOC",
        doctorId);
    appointmentResource.setAppointment_item_id(resourceRepository.getNextAppointmentItemId());
    appointmentResource.setMod_time(DateUtil.getCurrentTimestamp());
    appointmentResource.setUser_name("InstaAdmin");
    List<AppointMentResource> scheduledAppointmentItems = new ArrayList<AppointMentResource>();
    scheduledAppointmentItems.add(appointmentResource);

    saveAppointmentAndresources(scheduledAppointments, scheduledAppointmentItems, null);
    return appointmentId;

  }

  /**
   * Gets the appointment bean.
   *
   * @param field the field
   * @param value the value
   * @return the appointment bean
   */
  public BasicDynaBean getAppointmentBean(String field, Object value) {
    return resourceRepository.findByKey(field, value);
  }

  /**
   * Update practo appointment.
   *
   * @param apptBean the appt bean
   * @param startTime the start time
   * @param endTime the end time
   * @param patientName the patient name
   * @param patientPhone the patient phone
   * @param status the status
   * @param emailId the email id
   */
  @Transactional(rollbackFor = Exception.class)
  public void updatePractoAppointment(BasicDynaBean apptBean, Timestamp startTime,
      Timestamp endTime, String patientName, String patientPhone, String status, String emailId) {
    Timestamp currTime = new java.sql.Timestamp(new java.util.Date().getTime());

    apptBean.set("patient_name", patientName);
    apptBean.set("patient_contact", patientPhone);
    apptBean.set("appointment_status", status);
    apptBean.set("appointment_time", startTime);
    Long duration = (endTime.getTime() - startTime.getTime()) / (1000 * 60);
    apptBean.set("duration", duration.intValue());
    apptBean.set("changed_by", "InstaAdmin");
    apptBean.set("changed_time", currTime);
    apptBean.set("patient_email_id", emailId);
    if (status.equalsIgnoreCase(BookSDKUtil.APPOINTMENT_CANCELLED)) {
      apptBean.set("cancel_reason", "Practo cancellation");
      apptBean.set("cancel_type", "Patient");
    }
    Map<String, Object> keys = new HashMap<>();
    Integer appointmentId = (Integer) apptBean.get("appointment_id");
    keys.put("appointment_id", appointmentId);
    resourceRepository.update(apptBean, keys);
    List<BasicDynaBean> resourceItemList = resourceRepository.getAppointmentItems(appointmentId);
    List<ResourceDTO> resourceUpdateList = new ArrayList<ResourceDTO>();
    // update all the resources corresponding to appointment
    for (BasicDynaBean resourceItem : resourceItemList) {
      ResourceDTO resourceDTO = new ResourceDTO();
      resourceDTO.setAppointmentId(appointmentId);
      resourceDTO.setUser_name("InstaAdmin");
      resourceDTO.setMod_time(currTime);
      resourceDTO.setResourceId((String) resourceItem.get("resource_id"));
      resourceDTO.setResourceType((String) resourceItem.get("resource_type"));
      resourceUpdateList.add(resourceDTO);

    }
    updateSchedulerResourceDetails(null, resourceUpdateList, null);
  }

  /**
   * Save appointment andresources.
   *
   * @param scheduledAppointments the scheduled appointments
   * @param scheduledAppointmentItems the scheduled appointment items
   * @param scheduledAppointmentItemsRecurred the scheduled appointment items recurred
   */
  private void saveAppointmentAndresources(List<Appointments> scheduledAppointments,
      List<AppointMentResource> scheduledAppointmentItems,
      List<AppointMentResource> scheduledAppointmentItemsRecurred) {

    if (scheduledAppointments != null && !scheduledAppointments.isEmpty()) {
      resourceRepository.insertAppointments(scheduledAppointments);
    }
    if (scheduledAppointmentItems != null && !scheduledAppointmentItems.isEmpty()) {
      resourceRepository.insertAppointmentItems(scheduledAppointmentItems);
    }
    if (scheduledAppointmentItemsRecurred != null && !scheduledAppointmentItemsRecurred.isEmpty()) {
      resourceRepository.insertAppointmentItems(scheduledAppointmentItemsRecurred);
    }

  }

  /**
   * Update scheduler resource details.
   *
   * @param resourceInsertList the resource insert list
   * @param resourceUpdateList the resource update list
   * @param resourceDeleteList the resource delete list
   */
  private void updateSchedulerResourceDetails(List<ResourceDTO> resourceInsertList,
      List<ResourceDTO> resourceUpdateList, List<ResourceDTO> resourceDeleteList) {

    if (resourceDeleteList != null && !resourceDeleteList.isEmpty()) {
      resourceRepository.deleteResources(resourceDeleteList);
    }
    if (resourceInsertList != null && !resourceInsertList.isEmpty()) {
      resourceRepository.insertResources(resourceInsertList);
    }
    if (resourceUpdateList != null && !resourceUpdateList.isEmpty()) {
      resourceRepository.updateResources(resourceUpdateList);
    }
  }

  /**
   * Gets the todays appointments for mrno.
   *
   * @param mrNo the mr no
   * @return the todays appointments for mrno
   */
  public List<BasicDynaBean> getTodaysAppointmentsForMrno(String mrNo) {
    return resourceRepository.getTodaysAppointmentsForMrno(mrNo);
  }

  /**
   * Find by key.
   *
   * @param appointmentId the appointment id
   * @return the basic dyna bean
   */
  public BasicDynaBean findByKey(Integer appointmentId) {
    return resourceRepository.findByKey("appointment_id", appointmentId);
  }

  /**
   * Gets the appointment details.
   *
   * @param appointmentId the appointment id
   * @param appointmentPackGroupId the appointment pack group id
   * @return the appointment details
   */
  public List<BasicDynaBean> getAppointmentDetails(Integer appointmentId, 
      Integer appointmentPackGroupId) {
    return resourceRepository.getAppointmentDetails(appointmentId, appointmentPackGroupId);
  }

  /**
   * Gets the todays OP appointments.
   *
   * @param mrNo the mr no
   * @param centerId the center id
   * @return the todays OP appointments
   */
  public List<BasicDynaBean> getTodaysOPAppointments(String mrNo, int centerId) {
    return resourceRepository.getTodaysOPAppointments(mrNo, centerId);
  }

  /**
   * Gets the todays OP appointments.
   *
   * @param contactId the contact id
   * @param centerId the center id
   * @return the todays OP appointments
   */
  public List<BasicDynaBean> getTodaysOPAppointments(Integer contactId, int centerId) {
    return resourceRepository.getTodaysOPAppointments(contactId, centerId);
  }
  
  /**
   * Update status.
   *
   * @param appointmentId the appointment id
   * @param appointmentStatus the appointment status
   * @param userName the user name
   * @return true, if successful
   */
  public boolean updateStatus(int appointmentId, String appointmentStatus, String userName) {
    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("appointment_id", appointmentId);
    BasicDynaBean apptBean = resourceRepository.getBean();
    apptBean.set("appointment_status", appointmentStatus);
    apptBean.set("arrival_time", DateUtil.getCurrentTimestamp());
    apptBean.set("changed_by", userName);
    boolean status = resourceRepository.update(apptBean, keys) > 0;
    try {
      if (status
          && (appointmentStatus.equalsIgnoreCase("Arrived") || appointmentStatus
              .equalsIgnoreCase("Noshow"))) {
        boolean modPractoAdvantage = (securityService.getActivatedModules())
            .contains("mod_practo_advantage");
        if (modPractoAdvantage) {
          bookIntegrationService.pushAppointmentEventToPracto(
              getAppointmentDetails(appointmentId, null),
              appointmentStatus);
        }
        appointmentService.schedulePushEvent(String.valueOf(appointmentId),
            "APPOINTMENT_" + appointmentStatus.toUpperCase());
      }
    } catch (Exception ex) {
      logger.error("", ex);
    }
    return status;
  }

  /**
   * Update scheduler.
   *
   * @param appointmentId the appointment id
   * @param mrno the mrno
   * @param patientId the patient id
   * @param patientbean the patientbean
   * @param ailment the ailment
   * @param userName the user name
   * @param consultationTypeId the consultation type id
   * @param remarks the remarks
   * @param prescDocId the presc doc id
   * @param screenName the screen name
   * @return true, if successful
   */
  public boolean updateScheduler(int appointmentId, String mrno, String patientId,
      BasicDynaBean patientbean, String ailment, String userName, int consultationTypeId,
      String remarks, String prescDocId, String screenName) {
    String salutationName = "";
    BasicDynaBean salutationBean = null;

    Map<String, Object> saluKeys = new HashMap<String, Object>();
    saluKeys.put("salutation_id", patientbean.get("salutation"));
    salutationBean = salutationService.findByPk(saluKeys);
    if (salutationBean != null) {
      salutationName = (String) salutationBean.get("salutation");
    }
    
    BasicDynaBean apptBean = resourceRepository.getBean();
    apptBean.set("mr_no", mrno);
    apptBean.set("visit_id", patientId);
    String patientName = ((String) patientbean.get("patient_name")) + " "
        + (patientbean.get("middle_name") != null ? (String) patientbean.get("middle_name") : "")
        + " " + (patientbean.get("last_name") != null ? (String) patientbean.get("last_name") : "");
    apptBean.set("patient_name", patientName);
    String patientPhone = (String) patientbean.get("patient_phone");
    BasicDynaBean appbean = findByKey(appointmentId);
    patientPhone = (patientPhone == null || "".equals(patientPhone)) ? (String) appbean
        .get("patient_contact") : patientPhone;
    apptBean.set("patient_contact", patientPhone);
    apptBean.set("arrival_time", DateUtil.getCurrentTimestamp());
    if (screenName.equals("Reg")) {
      apptBean.set("appointment_status", "Arrived");
      apptBean.set("arrival_time", DateUtil.getCurrentTimestamp());
    }
    apptBean.set("complaint", ailment);
    apptBean.set("consultation_type_id", consultationTypeId);
    apptBean.set("changed_by", userName);
    apptBean.set("changed_time", DateUtil.getCurrentTimestamp());
    apptBean.set("presc_doc_id", (prescDocId != null && !prescDocId.equals("")) ? prescDocId
        : appbean.get("presc_doc_id"));
    apptBean.set("salutation_name", salutationName);
    if (remarks != null) {
      apptBean.set("remarks", remarks);
    }
    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("appointment_id", appointmentId);
    boolean status = resourceRepository.update(apptBean, keys) > 0;
    BasicDynaBean resAvailablebean = resourceAvailabilityService.getResourceBean((Integer) appbean
        .get("res_sch_id"));
    String category = resAvailablebean.get("res_sch_category") != null ? (String) resAvailablebean
        .get("res_sch_category") : "DOC";
    Appointment appt = appointmentService.getAppointmentObject(appointmentId);
    List<AppointmentResource> apptResourceList = appointmentService.getapptResourceListObject(appt);
    appointmentService.shareAppointmentsWithPracto(userName, appt, null, apptResourceList, false);
    redisMessagePublisher.publishMsgForSchema(RedisMessagePublisher.REDIS_APPOINTMENT_PUSH_CHANNEL,
        appointmentId + ";" + category);
    return status;
  }

  /**
   * Gets the conduction for test or service or operation.
   *
   * @param category the category
   * @param scheduleId the schedule id
   * @return the conduction for test or service or operation
   */
  public boolean getConductionForTestOrServiceOrOperation(String category, String scheduleId) {
    boolean conduction = false;
    BasicDynaBean bean = null;
    if (category == null) {
      return conduction;
    }
    if (category.equals("SNP")) {
      bean = getServiceConduction(scheduleId);
    } else if (category.equals("DIA")) {
      bean = getTestConduction(scheduleId);
    } else if (category.equals("OPE")) {
      bean = getOperationConduction(scheduleId);
    }
    if (bean != null && bean.get("conduction_applicable") != null
        && bean.get("conduction_applicable").equals(true)) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Gets the operation conduction.
   *
   * @param scheduleId the schedule id
   * @return the operation conduction
   */
  private BasicDynaBean getOperationConduction(String scheduleId) {
    return resourceRepository.getOperationConduction(scheduleId);
  }

  /**
   * Gets the test conduction.
   *
   * @param scheduleId the schedule id
   * @return the test conduction
   */
  private BasicDynaBean getTestConduction(String scheduleId) {
    return resourceRepository.getTestConduction(scheduleId);
  }

  /**
   * Gets the service conduction.
   *
   * @param scheduleId the schedule id
   * @return the service conduction
   */
  private BasicDynaBean getServiceConduction(String scheduleId) {
    return resourceRepository.getServiceConduction(scheduleId);
  }

  /**
   * Update test or service or operation status.
   *
   * @param appointmentId the appointment id
   * @param category the category
   * @return true, if successful
   */
  public boolean updateTestOrServiceOrOperationStatus(int appointmentId, String category) {
    BasicDynaBean bean = null;
    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("appointment_id", appointmentId);
    if (category == null) {
      return false;
    }
    if (category.equals("SNP")) {
      bean = serviceOrderItemService.getBean();
      bean.set("conducted", "C");
      return serviceOrderItemService.update(bean, keys) > 0;
    } else if (category.equals("DIA")) {
      bean = testOrderItemService.getBean();
      bean.set("conducted", "C");
      return testOrderItemService.update(bean, keys) > 0;
    } else if (category.equals("OPE")) {
      bean = operationOrderItemService.getBean();
      bean.set("conducted", "C");
      return operationOrderItemService.update(bean, keys) > 0;
    }
    return false;
  }

  /**
   * Checks if is appointment completed.
   *
   * @param appointmentId the appointment id
   * @param category the category
   * @return true, if is appointment completed
   */
  public boolean isAppointmentCompleted(int appointmentId, String category) {
    return resourceRepository.isAppointmentCompleted(appointmentId, category);
  }

  /**
   * Convert contact to mr no.
   *
   * @param mrNo the mr no
   * @param contactId the contact id
   */
  public void convertContactToMrNo(String mrNo, Integer contactId) {
    resourceRepository.flushContact(mrNo, contactId);
  }
}
