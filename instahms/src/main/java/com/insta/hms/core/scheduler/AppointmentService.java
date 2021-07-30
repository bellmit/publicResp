package com.insta.hms.core.scheduler;

import static com.insta.hms.jobs.common.QuartzJobHelper.buildJob;

import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.batchjob.builders.AppointmentStatusChangeSMSJob;
import com.insta.hms.batchjob.builders.BookAppointmentSharingJob;
import com.insta.hms.batchjob.builders.DynamicAppointmentReminderJob;
import com.insta.hms.batchjob.pushevent.EventListenerJob;
import com.insta.hms.batchjob.pushevent.Events;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DynaBeanBuilder;
import com.insta.hms.common.GenericRepository;
import com.insta.hms.common.JobSchedulingService;
import com.insta.hms.common.PhoneNumberUtil;
import com.insta.hms.common.PushService;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.security.SecurityService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.billing.BillService;
import com.insta.hms.core.clinical.order.master.OrderService;
import com.insta.hms.core.clinical.outpatient.DoctorConsultationService;
import com.insta.hms.core.patient.PatientDetailsRepository;
import com.insta.hms.core.patient.communication.PatientCommunicationService;
import com.insta.hms.core.patient.outpatientlist.PatientSearchService;
import com.insta.hms.core.patient.registration.PatientInsurancePlansService;
import com.insta.hms.core.patient.registration.PatientRegistrationRepository;
import com.insta.hms.core.patient.registration.RegistrationPreferencesService;
import com.insta.hms.core.patient.registration.RegistrationService;
import com.insta.hms.exception.HMSException;
import com.insta.hms.exception.NestableValidationException;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.integration.book.BookIntegrationService;
import com.insta.hms.jobs.JobService;
import com.insta.hms.mdm.appointmentsources.AppointmentSourceRepository;
import com.insta.hms.mdm.breaktheglass.UserMrnoAssociationRepository;
import com.insta.hms.mdm.centerpreferences.CenterPreferencesService;
import com.insta.hms.mdm.centers.CenterService;
import com.insta.hms.mdm.confidentialitygrpmaster.UserConfidentialityAssociationRepository;
import com.insta.hms.mdm.consultationtypes.ConsultationTypesService;
import com.insta.hms.mdm.doctors.DoctorService;
import com.insta.hms.mdm.hospitalcenters.HospitalCenterService;
import com.insta.hms.mdm.insuranceplans.InsurancePlanService;
import com.insta.hms.mdm.practitionertypes.PractitionerTypeMappingsService;
import com.insta.hms.mdm.resourceavailability.ResourceAvailabilityService;
import com.insta.hms.mdm.resourceoverride.ResourceOverrideDetailsRepository;
import com.insta.hms.mdm.resourceoverride.ResourceOverrideService;
import com.insta.hms.mdm.salutations.SalutationService;
import com.insta.hms.messaging.MessageManager;
import com.insta.hms.redis.RedisMessagePublisher;
import com.insta.hms.security.usermanager.UserService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.codehaus.plexus.util.StringUtils;
import org.jsoup.helper.StringUtil;
import org.quartz.JobDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO: Auto-generated Javadoc
/**
 * The Class AppointmentService.
 */
@Service
public class AppointmentService {

  /** The logger. */
  private Logger logger = LoggerFactory.getLogger(this.getClass());

  /** The generic preferences service. */
  @LazyAutowired
  private GenericPreferencesService genericPreferencesService;

  /** The appointment validator. */
  @LazyAutowired
  private AppointmentValidator appointmentValidator;

  /** The resource override service. */
  @LazyAutowired
  private ResourceOverrideService resourceOverrideService;

  /** The resource details repository. */
  @LazyAutowired
  private ResourceOverrideDetailsRepository resourceDetailsRepository;

  /** The appointment repository. */
  @LazyAutowired
  private AppointmentRepository appointmentRepository;

  /** The res availability service. */
  @LazyAutowired
  private ResourceAvailabilityService resAvailabilityService;

  /** The session service. */
  @LazyAutowired
  private SessionService sessionService;

  /** The book integration service. */
  @LazyAutowired
  private BookIntegrationService bookIntegrationService;

  /** The center service. */
  @LazyAutowired
  private CenterService centerService;

  /** The job service. */
  @LazyAutowired
  private JobService jobService;

  /** The resource repository. */
  @LazyAutowired
  private ResourceRepository resourceRepository;

  /** The patient search service. */
  @LazyAutowired
  private PatientSearchService patientSearchService;

  /** The push service. */
  @LazyAutowired
  private PushService pushService;

  /** The reg pref service. */
  @LazyAutowired
  RegistrationPreferencesService regPrefService;

  /** The appointment category factory. */
  @LazyAutowired
  private AppointmentCategoryFactory appointmentCategoryFactory;

  /** The bill service. */
  @LazyAutowired
  private BillService billService;

  /** The salutation service. */
  @LazyAutowired
  private SalutationService salutationService;

  /** The order service. */
  @LazyAutowired
  private OrderService orderService;

  /** The registration service. */
  @LazyAutowired
  private RegistrationService registrationService;

  /** The patient insurance plans service. */
  @LazyAutowired
  private PatientInsurancePlansService patientInsurancePlansService;

  /** The consultation types service. */
  @LazyAutowired
  private ConsultationTypesService consultationTypesService;

  /** The insurance plan service. */
  @LazyAutowired
  private InsurancePlanService insurancePlanService;

  /** The doctor consultation service. */
  @LazyAutowired
  private DoctorConsultationService doctorConsultationService;

  /** The patient registration repository. */
  @LazyAutowired
  private PatientRegistrationRepository patientRegistrationRepository;

  /** The patient registration repository. */
  @LazyAutowired
  private PatientDetailsRepository patientDetailsRepository;

  /** The patient contact detail repository. */
  @LazyAutowired
  private PatientContactDetailsRepository patientContactDetailRepository;

  /** The scheduler service. */
  @LazyAutowired
  private SchedulerService schedulerService;

  /** The hospital center service. */
  @LazyAutowired
  private HospitalCenterService hospitalCenterService;

  /** The security service. */
  @LazyAutowired
  private SecurityService securityService;

  /** The appointment source repository. */
  @LazyAutowired
  private AppointmentSourceRepository appointmentSourceRepository;

  /** The redis message publisher. */
  @LazyAutowired
  private RedisMessagePublisher redisMessagePublisher;

  /** The user service. */
  @LazyAutowired
  private UserService userService;

  /** The user service. */
  @LazyAutowired
  private UserConfidentialityAssociationRepository userConfidentialityAssociationRepository;
  
  /** The user mrno association repository. */
  @LazyAutowired
  private UserMrnoAssociationRepository userMrnoAssociationRepository;

  /** The patient contact details service. */
  @LazyAutowired
  private PatientContactDetailsService patientContactDetailsService;
  
  /** The patient communication service. */
  @LazyAutowired
  private PatientCommunicationService patientCommunicationService;
  
  /** The practitioner type mapping service. */
  @LazyAutowired
  private PractitionerTypeMappingsService practitionerTypeMappingService;
  
  /** The doctor service. */
  @LazyAutowired
  private DoctorService doctorService;

  /** The Center preference service. */
  @LazyAutowired
  private CenterPreferencesService centerPreferencesService;
  
  /** The contact details repository. */
  @LazyAutowired
  private PatientContactDetailsRepository contactDetailsRepository;
  
  /** The Constant WEBSOCKET_PUSH_CHANNEL. */
  private static final String WEBSOCKET_PUSH_CHANNEL = "/topic/appointments";

  /** The Constant APPOINTMENT_STATUS_FOR_DYNAMIC_SMS. */
  private static final Set<String> APPOINTMENT_STATUS_FOR_DYNAMIC_SMS = new HashSet<>(
      Arrays.asList("confirmed","booked"));

  /** The register redirect url. */
  private final String registerRedirectUrl = "/patients/opregistration/index.htm#/filter/1/"
      + "appointment/#contactId#/registration/visit/new?"
      + "contact_id=#contactId#&category=#category#&registrationType=OP&retain_route_params=true";

  /** The register redirect url for mr. */
  private final String registerRedirectUrlForMr = "/patients/opregistration/index.htm#/filter/1/"
      + "patient/#mr_no#/registration/visit/new?"
      + "appointment_id=#appointmentId#&category=#category#&"
      + "registrationType=OP&retain_route_params=true&current_appointment_id=#appointmentId#&"
      + "contact_id=#contactId#";

  /** The message ordered. */
  private static final String MESSAGE_ORDERED = "Consultation is ordered";


  /**
   * Creates the bulk appointments.
   *
   * @param apptCategoryList the appt category list
   * @param params           the params
   * @return the map
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Transactional(rollbackFor = Exception.class)
  public Map<String, Object> createBulkAppointments(List<AppointmentCategory> apptCategoryList,
      Map<String, Object> params) {
    List<Map> appointmentInfoList = (List<Map>) params.get("appointments");
    Map finalResponseMap = new HashMap();
    finalResponseMap.put("patient", params.get("patient"));
    List finalResponseAppList = new ArrayList();
    for (int i = 0; i < appointmentInfoList.size(); i++) {
      Map<String, Object> appointmentInfo = appointmentInfoList.get(i);
      AppointmentCategory apptCategory = apptCategoryList.get(i);
      List<AppointmentCategory> categoryList = new ArrayList();
      categoryList.add(apptCategory);
      Map<String, Object> parameterMap = new HashMap();
      List appointmentList = new ArrayList();
      appointmentList.add(appointmentInfo);
      parameterMap.putAll(params);
      parameterMap.put("appointments", appointmentList);
      Map responseMap = createNewAppointments(categoryList, parameterMap);
      finalResponseAppList.add(((List) responseMap.get("appointments")).get(0));
    }
    finalResponseMap.put("appointments", finalResponseAppList);
    return finalResponseMap;
  }

  /**
   * Update appointments package group id.
   *
   * @param appointmentsResponseMap the appointments response map
   * @param packagesIds             the packages ids
   * @return the list
   */
  @Transactional
  public List<Map<String, Object>> updateAppointmentsPackageGroupId(
      List<Map<String, Object>> appointmentsResponseMap, Set<Integer> packagesIds) {
    for (Integer packageId : packagesIds) {
      Integer uniqueApptPackageGrpId = appointmentRepository
          .getNextUniqueAppointmentPackageGroupId();
      List<BasicDynaBean> updateBeanList = new ArrayList<>();
      Map<String, Object> keys = new HashMap<>();
      for (Map<String, Object> appointments : appointmentsResponseMap) {
        Map<String, Object> appointmentInfo = (Map<String, Object>) appointments
            .get("appointments");
        if (((Integer) appointmentInfo.get("package_id")).equals(packageId)) {
          keys.put("appointment_id", ((List) appointmentInfo.get("appointment_ids_list")).get(0));
          BasicDynaBean appointmentBean = appointmentRepository.findByKey("appointment_id",
              ((List) appointmentInfo.get("appointment_ids_list")).get(0));
          appointmentBean.set("appointment_pack_group_id", uniqueApptPackageGrpId);
          appointmentRepository.update(appointmentBean, keys);
          appointmentInfo.put("appointment_pack_group_id", uniqueApptPackageGrpId);
        }
      }
    }
    return appointmentsResponseMap;
  }

  /**
   * Creates the new appointments.
   *
   * @param apptCategoryList the appt category list
   * @param params           the params
   * @return the map
   */
  @Transactional(rollbackFor = Exception.class)
  @SuppressWarnings({ "unchecked", "rawtypes", "unused" })
  public Map<String, Object> createNewAppointments(List<AppointmentCategory> apptCategoryList,
      Map<String, Object> params) {

    Map<String, Object> patientInfo = (Map<String, Object>) params.get("patient");
    List<Map> appointmentInfoList = (List<Map>) params.get("appointments");
    Map<String, Object> recurrenceInfo = (Map<String, Object>) params.get("repeats");
    Map<String, Object> additionalInfo = (Map<String, Object>) params.get("additional_info");
    List<Appointment> appointmentsList = new ArrayList<Appointment>();
    List<AppointmentResource> appointmentResourceList = new ArrayList<AppointmentResource>();
    List<Map> additionalResourceMapList = new ArrayList<>();
    ArrayList<AppointmentResource> scheduleAppointItemBeanRecuured =
        new ArrayList<AppointmentResource>();
    List appointmentIdForResponse = new ArrayList<Integer>();
    List appointmentCatForResponse = new ArrayList<String>();
    Appointment appointmentPojo = null;
    AppointmentCategory apptCategory = null;
    BasicDynaBean genpref = genericPreferencesService.getPreferences();
    String mrNo = null;
    boolean applyPatientAttribution = (params.get("patient_attribution") != null
        && (Boolean) params.get("patient_attribution"));

    for (int i = 0; i < appointmentInfoList.size(); i++) {

      Map appointmentInfo = appointmentInfoList.get(i);
      apptCategory = apptCategoryList.get(i);
      String visitMode = (String) appointmentInfo.get("visit_mode");
      String practoApptId = appointmentInfo.get("practo_appointment_id") != null
          ? (String) appointmentInfo.get("practo_appointment_id")
          : null;
      if (params.get("appointment_plan_id") == null
          && appointmentInfoList.get(i).get("package_id") == null
          && (practoApptId == null || practoApptId.equals(""))) {
        validate(apptCategory, patientInfo, appointmentInfo, additionalInfo,visitMode);
        validateAppointmentSource(patientInfo, appointmentInfo);
      }
      Integer appointmentId = appointmentRepository.getNextSequence();
      List<Integer> apptIdsList = new ArrayList<Integer>();
      List<AppointmentCategory> apptCatsList = new ArrayList();
      apptIdsList.add(appointmentId);
      apptCatsList.add(apptCategory);
      String userName = null;
      if (practoApptId != null && !practoApptId.equalsIgnoreCase("")) {
        userName = "InstaAdmin";
      } else {
        userName = (String) sessionService.getSessionAttributes().get("userId");
      }
      String primResId = (String) appointmentInfo.get("primary_resource_id");
      Timestamp bookedTime = DateUtil.getCurrentTimestamp();
      if (appointmentInfo.get("app_source_id") != null
          && (Integer)appointmentInfo.get("app_source_id") == -1
          && appointmentInfo.get("category").equals("DOC")) {
        String secondaryRes = setSecondaryResource(primResId,
            (Integer) appointmentInfo.get("center_id"));
        appointmentInfo.put("secondary_resource_id", secondaryRes);
      }
      applyPatientAttribution = applyPatientAttribution
          || (practoApptId != null);
      String patientContact = patientInfo.get("patient_contact") != null
          ? (String) patientInfo.get("patient_contact") : null;
      String patientName = patientInfo.get("patient_name") != null
          ? (String) patientInfo.get("patient_name") : null;
      if (applyPatientAttribution && (appointmentInfo.get("mr_no") == null
          || appointmentInfo.get("mr_no").equals(""))) {
        List<BasicDynaBean> matchingPatientList =
            patientSearchService.findPatientMatch(patientName, patientContact, true,
                (Integer) genpref.get("patient_name_match_distance"));
        // updating mr_no for existing patients.
        if (matchingPatientList.size() == 1) {
          mrNo = (String) matchingPatientList.get(0).get("mr_no");
          patientInfo.put("mr_no", mrNo);
        }
      }

      // Create Appointment
      Appointment appt = new Appointment(appointmentId);
      String resType = apptCategory.getPrimaryResourceType();
      Integer overbookLimit = apptCategory.getResourceOverbookLimit(primResId, resType);
      boolean overBookAllowed = overbookLimit == null || overbookLimit > 0;
      if (overBookAllowed || (practoApptId != null && !practoApptId.equalsIgnoreCase(""))) {
        appt.setUnique_appt_ind(appointmentRepository.getNextUniqueAppointMentInd());
      } else {
        appt.setUnique_appt_ind(0);
      }
 
      params.put("appointment", appointmentInfo);
      setAppointmentPojo(apptCategory, params, appt);
      appt.setBookedTime(DateUtil.getCurrentTimestamp());
      appt.setBookedBy(userName);
      Integer parentPackObId = appointmentInfo.get("parent_pack_ob_id") != null
          ? Integer.valueOf((String) appointmentInfo.get("parent_pack_ob_id"))
          : null;
      appt.setParentPackObId(parentPackObId);
      Integer packageId = appointmentInfo.get("package_id") != null
          ? (Integer) appointmentInfo.get("package_id")
          : null;
      appt.setPackageId(packageId);

      Timestamp originalApptTime = appointmentInfo.get("orig_appt_time") != null
          ? (Timestamp) appointmentInfo.get("orig_appt_time") : null;
      appt.setOrigApptTime(originalApptTime);

      // setting waitList
      Timestamp apptm = appt.getAppointmentTime();
      long apptTimeLongt = apptm.getTime();
      apptTimeLongt = apptTimeLongt + (appt.getAppointmentDuration() * 60 * 1000);
      Timestamp apptEndTimet = new java.sql.Timestamp(apptTimeLongt);
      Integer waitlist = appointmentRepository.getOverbookCountForResource(apptm, apptEndTimet,
          primResId, resType, null);
      appt.setWaitlist(waitlist);
      
      // TODO refactor required
      if (appt.getAppointmentPackGroupId() == null && packageId != null) {
        appt.setAppointmentPackGroupId(0);
      }
      // populate category specific appointment data
      List<Appointment> appointmentsListForOneRepeatsSet = new ArrayList<Appointment>();
      appointmentsListForOneRepeatsSet.add(appt);
      apptCategory.setAppointmentData(appointmentsListForOneRepeatsSet, params);
      List<AppointmentResource> appointmentResourceListForOneRepeatsSet =
          new ArrayList<AppointmentResource>();
      apptCategory.setAppointmentAdditionalResourceData(appt,
          appointmentResourceListForOneRepeatsSet, params);

      boolean isSponsorInfoExists = false;
      // set the sponsor information
      if (appt.getPrimarySponsorId() != null) {
        isSponsorInfoExists = true;
      }
      // set the recurrence data
      if (recurrenceInfo != null) {
        RecurrencePattern recurrPattern = new RecurrencePattern();
        recurrPattern.setRecurranceOption(
            recurrenceInfo.get("repeat_by") != null ? (String) recurrenceInfo.get("repeat_by")
                : null);
        recurrPattern.setRecurrNo(recurrenceInfo.get("repeat_every") != null
            ? (Integer) recurrenceInfo.get("repeat_every")
            : null);
        recurrPattern.setWeek(
            recurrenceInfo.get("days_of_week") != null ? (List) recurrenceInfo.get("days_of_week")
                : null);
        try {
          DateUtil recurrDateUtil = new DateUtil();
          DateUtil endDateUtil = new DateUtil();
          recurrPattern.setRecurrDate(
              recurrDateUtil.parseTheTimestamp((String) recurrenceInfo.get("recurr_date")));
          recurrPattern
              .setUntilDate(endDateUtil.parseTheTimestamp((String) recurrenceInfo.get("end_date")));
        } catch (ParseException exp) {

          ValidationErrorMap errorMap = new ValidationErrorMap();
          errorMap.addError("recurrDate", "exception.scheduler.repeats.invalid.date");
          ValidationException ex = new ValidationException(errorMap);
          Map<String, Object> nestedException = new HashMap<String, Object>();
          nestedException.put("repeats", ex.getErrors());
          throw new NestableValidationException(nestedException);
        }
        recurrPattern.setOccurrNo(recurrenceInfo.get("end_occurances") != null
            ? (Integer) recurrenceInfo.get("end_occurances")
            : null);
        recurrPattern.setMonthlyRecurrType((String) recurrenceInfo.get("recurr_month_type"));
        Map recMap = new HashMap();
        recMap.put("start_time", appt.getAppointmentTime());
        recMap.put("slotDuration", appt.getAppointmentDuration());
        recMap.put("scheduleAppointBeanList", appointmentsListForOneRepeatsSet);
        recMap.put("scheduleAppointItemBean", appointmentResourceListForOneRepeatsSet);
        recMap.put("scheduleAppointItemBeanRecuured", scheduleAppointItemBeanRecuured);
        recMap.put("userName", userName);
        recMap.put("recurrPattern", recurrPattern);
        recMap.put("category", apptCategory);
        recMap.put("resourceId", appt.getPrim_res_id());
        recMap.put("apptIds", apptIdsList);
        recMap.put("apptCats", apptCatsList);
        recMap.put("isSponsorInfoExists", isSponsorInfoExists);
        recMap.put("overbookLimit", overbookLimit);
        recMap.put("visitMode", visitMode);

        getTotalRecurrences(recMap);
      }

      Map additionalResourceMap = new HashMap();
      additionalResourceMap.put("appIdList", apptIdsList);
      List<Map> insertList = appointmentInfo.get("additional_resources_insert") != null
          ? (List<Map>) appointmentInfo.get("additional_resources_insert")
          : null;
      additionalResourceMap.put("insertList", insertList);
      List<Map> deleteList = appointmentInfo.get("additional_resources_delete") != null
          ? (List<Map>) appointmentInfo.get("additional_resources_delete")
          : null;
      additionalResourceMap.put("deleteList", deleteList);
      additionalResourceMap.put("userName", userName);
      additionalResourceMap.put("appointmentsList", appointmentsListForOneRepeatsSet);
      additionalResourceMap.put("appt", appt);
      additionalResourceMap.put("appointmentResourceList", appointmentResourceListForOneRepeatsSet);
      additionalResourceMapList.add(additionalResourceMap);

      for (Integer apptId : apptIdsList) {
        appointmentIdForResponse.add(apptId);
      }

      appointmentInfo.put("appointment_ids_list", apptIdsList);

      for (AppointmentCategory apptCat : apptCatsList) {
        appointmentCatForResponse.add(apptCat.getCategory());
      }

      for (Appointment appointment : appointmentsListForOneRepeatsSet) {
        appointmentsList.add(appointment);
      }

      for (AppointmentResource item : appointmentResourceListForOneRepeatsSet) {
        appointmentResourceList.add(item);
      }

      params.remove("appointment");
      if (appointmentInfo.get("appointment_id") != null) {
        appointmentInfo.remove("appointment_id");
      }
    }

    if (appointmentsList != null && appointmentsList.size() > 0) {
      for (Appointment app : appointmentsList) {
        if (app.getMrNo() == null || app.getMrNo().isEmpty()) {
          app.setMrNo(mrNo);
        }
      }
    }

    Boolean status = true;
    // inserting appointment
    if (appointmentsList != null && appointmentsList.size() > 0) {
      status = status & insertSchedulerAppointment(appointmentsList);
    }

    // inserting primary resource as additional resource
    if (appointmentResourceList != null) {
      status = status & appointmentRepository.addSchedulerAppointmentItems(appointmentResourceList);
    }

    // inserting additional resources

    for (Map additionalResourceMap : additionalResourceMapList) {

      List insertList = (List) additionalResourceMap.get("insertList");
      List deleteList = (List) additionalResourceMap.get("deleteList");
      String userName = (String) additionalResourceMap.get("userName");
      List<Integer> appIdsList = (List) additionalResourceMap.get("appIdList");
      for (Integer appotId : appIdsList) {
        status = status & updateAdditionalResources(insertList, deleteList, appotId, userName);
      }
    }

    // inserting repeats data
    if (scheduleAppointItemBeanRecuured != null && scheduleAppointItemBeanRecuured.size() > 0) {
      status = status
          & appointmentRepository.addSchedulerAppointmentItems(scheduleAppointItemBeanRecuured);
    }

    // Push the new appointment at web service endpoint
    if (status) {
      String apptIds = appointmentIdForResponse.toString();
      apptIds = apptIds.substring(1, apptIds.length() - 1);
      String apptCategories = appointmentCatForResponse.toString();
      apptCategories = apptCategories.substring(1, apptCategories.length() - 1);
    }
    patientCommunicationService.convetAndSavePatientCommPreference(
        appointmentsList.get(0).getMrNo(), (String) patientInfo.get("send_sms"),
        (String) patientInfo.get("send_email"), (String) patientInfo.get("preferred_language"));
    boolean sendCommunication = true;
    if (params.get("send_communication") != null) {
      sendCommunication = "Y".equals((String) params.get("send_communication"));
    } else {
      sendCommunication = appointmentInfoList.get(0).get("send_communication") == null
          || (Boolean) appointmentInfoList.get(0).get("send_communication");
    }
    String[] apptIds = new String[appointmentsList.size()];
    for (int i = 0; i < appointmentsList.size(); i++) {
      apptIds[i] = appointmentsList.get(i).getAppointmentId().toString();
    }
    if (apptIds.length == 1) {
      schedulePushEvent(apptIds[0],
          "APPOINTMENT_" + appointmentsList.get(0).getAppointStatus().toUpperCase());
    } else {
      schedulePushEvent(apptIds,
          "APPOINTMENT_" + appointmentsList.get(0).getAppointStatus().toUpperCase());
    }
    
    if (sendCommunication) {
      if (params.get("appointment_plan_id") == null) {

        for (Appointment appt : appointmentsList) {
          // Send SMS for appointments if any
          if ((appt.getPractoAppointmentId() == null || appt.getPractoAppointmentId().equals(""))
              && appt.getAppointStatus() != null
              && (appt.getAppointStatus().equalsIgnoreCase("confirmed")
                  || appt.getAppointStatus().equalsIgnoreCase("booked"))) {

            sendAppointmentSMSforNewAppointment(appt.getAppointmentId(), appt.getBookedBy(),
                appt.getAppointStatus(), appt.getAppointmentTime());
            if (apptCategory.getCategory().equals("DOC")
                && appt.getAppointStatus().equalsIgnoreCase("confirmed")) {
              scheduleAppointmentStatusChangeSMSJob(appt.getAppointmentId().toString(),
                  appt.getBookedBy(), appt.getAppointStatus(), "doc_appt_confirmed");
            }
          }
        }
      }
      for (Appointment appt : appointmentsList) {
        if (checkDynamicSmsAllowedtoSend(appt.getAppointStatus())) {
          scheduleAppointmentMsg(appt.getAppointmentId(), "DynamicAppointmentReminderJob",
              appt.getAppointmentTime());
        }
      }
    }

    for (Map additionalResourceMap : additionalResourceMapList) {

      // Sharing with practo
      Appointment appt = (Appointment) additionalResourceMap.get("appt");
      List<Appointment> appointmentsListForPush = (List) additionalResourceMap
          .get("appointmentsList");
      List<AppointmentResource> appointmentsResourceForPush = 
          (List<AppointmentResource>) additionalResourceMap
          .get("appointmentResourceList");
      List<Map> insertList = (List<Map>) additionalResourceMap.get("insertList");
      if (bookIntegrationService.isPractoAdvantageEnabled()
          && appt.getPractoAppointmentId() == null) {
        // Add all the doctor appts to Practo
        // TODO: populate recurrence timing list (only recurrent appt times
        // list)
        List<Map<String, Timestamp>> recurrenceList = null;
        if (appointmentsListForPush != null && appointmentsListForPush.size() > 1) {
          recurrenceList = new ArrayList<Map<String, Timestamp>>();
          for (Appointment appointment : appointmentsListForPush) {
            Map<String, Timestamp> map = new HashMap<String, Timestamp>();
            map.put(Integer.toString(appointment.getAppointmentId()),
                appointment.getAppointmentTime());
            recurrenceList.add(map);
          }
        }
        if (insertList != null) {
          for (Map map : insertList) {
            AppointmentResource appRes = new AppointmentResource(appt.getAppointmentId(),
                (String) map.get("resource_type"), (String) map.get("resource_id"));
            appointmentsResourceForPush.add(appRes);
          }
        }
        shareAppointmentsWithPracto(appt.getBookedBy(), appt, recurrenceList,
            appointmentsResourceForPush, true);
        
      }
    }

    return params;
  }

  /**
   * Set center default secondary resource for portal appointments.
   * @param primResId primary resource id
   * @param centerId cnter id
   * @return consultation type id
   */
  public String setSecondaryResource(String primResId, Integer centerId) {
    List consultationTypesForPractioner = findDoctorPractiontionerType(primResId, centerId);
    if (consultationTypesForPractioner.size() > 0) {
      Map<String,Object> res = (Map) consultationTypesForPractioner.get(0);
      return Integer.toString((Integer) res.get("consultation_type_id"));
    }
    return "-1";
  }

  /**
   * Validate appointment source.
   *
   * @param patientInfo     the patient info
   * @param appointmentInfo the appointment info
   */
  public void validateAppointmentSource(Map<String, Object> patientInfo, Map appointmentInfo) {
    Map<String, Object> nestedException = new HashMap<>();
    ValidationErrorMap validationErrors = new ValidationErrorMap();
    Integer appointmentSource = appointmentInfo.get("app_source_id") != null
        ? (Integer) appointmentInfo.get("app_source_id")
        : null;
    if (appointmentSource == null || appointmentSource == 0) {
      return;
    }
    BasicDynaBean appointmentSourceBean = appointmentSourceRepository
        .findByPk(Collections.singletonMap("appointment_source_id", appointmentSource));
    if (appointmentSourceBean == null) {
      validationErrors.addError("app_source_id", "exception.scheduler.appointment.source.invalid");
      ValidationException ex = new ValidationException(validationErrors);
      nestedException.put("appointment", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    Integer apptLimit = (Integer) appointmentSourceBean.get("patient_day_appt_limit");
    if (apptLimit.equals(-1)) {
      return;
    }
    String apptDateStr = (String) appointmentInfo.get("date");
    Date apptDate = null;
    try {
      apptDate = DateUtil.parseDate(apptDateStr);
    } catch (ParseException exp) {
      logger.error("", exp);
    }
    String mrNo = patientInfo.get("mr_no") != null ? (String) patientInfo.get("mr_no") : null;
    String patientContact = patientInfo.get("patient_contact") != null
        ? (String) patientInfo.get("patient_contact")
        : null;
    if (appointmentRepository.isAppointmentLimitReached(apptDate, mrNo, patientContact,
        apptLimit)) {
      validationErrors.addError("app_source_id",
          "exception.scheduler.appointment.source.limit.reached");
      ValidationException ex = new ValidationException(validationErrors);
      nestedException.put("appointment", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }

  }

  /**
   * Save additional details.
   *
   * @param additionalInfo the additional info
   * @param appt           the appt
   * @param contactInfo    the contact info
   */
  public void saveAdditionalDetails(Map<String, Object> additionalInfo, Appointment appt,
      Map<String, Object> contactInfo) {

    String patientDob = additionalInfo.get("patient_dob") != null
        ? (String) additionalInfo.get("patient_dob")
        : null;
    if (patientDob != null) {
      try {
        appt.setPatientDob(DateUtil.parseDate(patientDob));
      } catch (ParseException e1) {
        ValidationErrorMap errorMap = new ValidationErrorMap();
        errorMap.addError("additional_info", "exception.scheduler.patientDob.invalid.date");
        ValidationException ex = new ValidationException(errorMap);
        Map<String, Object> nestedException = new HashMap<String, Object>();
        nestedException.put("additional_info", ex.getErrors());
        throw new NestableValidationException(nestedException);
      }
    }
    Integer patientAge = additionalInfo.get("age") != null
        ? Integer.parseInt((String) additionalInfo.get("age"))
        : null;

    appt.setPatientAge(patientAge);
    String patientAgeUnits = additionalInfo.get("patient_age_units") != null
        ? (String) additionalInfo.get("patient_age_units")
        : null;
    appt.setPatientAgeUnits(patientAgeUnits);
    String patientGender = additionalInfo.get("patient_gender") != null
        ? (String) additionalInfo.get("patient_gender")
        : null;
    appt.setPatientGender(patientGender);
    Integer patientCategory = additionalInfo.get("patient_category_id") != null
        ? (Integer) additionalInfo.get("patient_category_id")
        : null;
    appt.setPatientCategory(patientCategory);
    String patientAddress = additionalInfo.get("patient_address") != null
        ? (String) additionalInfo.get("patient_address")
        : null;
    appt.setPatientAddress(patientAddress);
    String patientArea = additionalInfo.get("patient_area") != null
        ? (String) additionalInfo.get("patient_area")
        : null;
    appt.setPatientArea(patientArea);
    String patientState = additionalInfo.get("patient_state") != null
        ? (String) additionalInfo.get("patient_state")
        : null;
    appt.setPatientState(patientState);
    String patientCity = additionalInfo.get("patient_city") != null
        ? (String) additionalInfo.get("patient_city")
        : null;
    appt.setPatientCity(patientCity);
    String patientCountry = additionalInfo.get("patient_country") != null
        ? (String) additionalInfo.get("patient_country")
        : null;
    appt.setPatientCountry(patientCountry);
    String patientNationality = additionalInfo.get("patient_nationality") != null
        ? (String) additionalInfo.get("patient_nationality")
        : null;
    appt.setPatientNationality(patientNationality);
    String patientEmailId = additionalInfo.get("patient_email_id") != null
        ? (String) additionalInfo.get("patient_email_id")
        : null;
    appt.setPatientEmailId(patientEmailId);
    String patientCitizenId = additionalInfo.get("citizen_id") != null
        ? (String) additionalInfo.get("citizen_id")
        : null;
    appt.setPatientCitizenId(patientCitizenId);

  }

  /**
   * Share appointments with practo.
   *
   * @param userName                the user name
   * @param appt                    the appt
   * @param recurrenceList          the recurrence list
   * @param appointmentResourceList the appointment resource list
   * @param isCreated               the is created
   */
  public void shareAppointmentsWithPracto(String userName, Appointment appt,
      List<Map<String, Timestamp>> recurrenceList,
      List<AppointmentResource> appointmentResourceList, boolean isCreated) {
    Map<String, Object> jobData = new HashMap<>();
    jobData.put("schema", RequestContext.getSchema());
    jobData.put("userName", userName);
    jobData.put("appointment", appt);
    jobData.put("recurrenceList", recurrenceList);
    jobData.put("appointmentResourceList", appointmentResourceList);
    jobData.put("isAppointmentCreated", isCreated);
    jobService
        .scheduleImmediate(buildJob("BookApptSharingJob_" + isCreated + appt.getAppointmentId(),
            BookAppointmentSharingJob.class, jobData));
  }
  
  /**
   * Schedule push event.
   *
   * @param appointmentId the appointment id
   * @param eventId the event id
   */
  public void schedulePushEvent( String appointmentId, String eventId) {
    String schema =  RequestContext.getSchema();
    Map<String, Object> eventData = new HashMap<>();
    eventData.put("appointment_id", appointmentId);
    eventData.put("schema", schema);
    eventData.put("eventId", eventId);
    
    Map<String, Object> jobData = new HashMap<>();
    jobData.put("schema", schema);
    jobData.put("eventId", eventId);
    jobData.put("eventData", eventData);
    jobService
        .scheduleImmediate(buildJob("PushEventJob_" + appointmentId,
            EventListenerJob.class, jobData));
  }
  
  /**
   * Schedule push event.
   *
   * @param appointmentId the appointment id
   * @param eventId the event id
   */
  public void schedulePushEvent( String[] appointmentId, String eventId) {
    String schema =  RequestContext.getSchema();
    Map<String, Object> eventData = new HashMap<>();
    eventData.put("appointment_ids", appointmentId);
    eventData.put("schema", schema);
    eventData.put("eventId", eventId);
    
    Map<String, Object> jobData = new HashMap<>();
    jobData.put("schema", schema);
    jobData.put("eventId", eventId);
    jobData.put("eventData", eventData);
    jobService
        .scheduleImmediate(buildJob("PushEventJob_" + appointmentId,
            EventListenerJob.class, jobData));
  }

  /**
   * Push appointments to web socket.
   *
   * @param appointmentIdArray the appointment id array
   * @param appointmentCategoriesArray the appointment categories array
   */
  @SuppressWarnings("unchecked")
  public void pushAppointmentsToWebSocket(String[] appointmentIdArray,
      String[] appointmentCategoriesArray) {
    List<BasicDynaBean> users = userService.listAll();
    for (BasicDynaBean user : users) {
      Object loginHandle = user.get("login_handle");
      if (null != loginHandle && !"".equals(loginHandle)) {
        boolean mask = false;
        Map<String, Object> pushMap = new HashMap<String, Object>();
        for (int i = 0; i < appointmentIdArray.length; i++) {

          Integer apptId = Integer.parseInt(appointmentIdArray[i]);
          AppointmentCategory apptCategory = appointmentCategoryFactory
              .getInstance(appointmentCategoriesArray[i]);
          Map apptMap = new HashMap<String, Object>();
          BasicDynaBean appointmentBean = appointmentRepository
              .getAppointmentsByAppointmentId(apptCategory, apptId, null);
          if (appointmentBean != null) {
            BasicDynaBean userConfidentialityAssocBean = null;
            BasicDynaBean userMrnoAssociationBean = null;
            if (appointmentBean.get("patient_group") != null
                && (Integer) appointmentBean.get("patient_group") != 0) {
              Map<String, Object> filterMap = new HashMap<>();
              filterMap.put("emp_username", user.get("emp_username"));
              filterMap.put("confidentiality_grp_id", appointmentBean.get("patient_group"));
              filterMap.put("status", "A");
              userConfidentialityAssocBean = userConfidentialityAssociationRepository
                  .findByKey(filterMap);
              Map<String, Object> userMrnoFilter = new HashMap<>();
              userMrnoFilter.put("emp_username", user.get("emp_username"));
              userMrnoFilter.put("mr_no", appointmentBean.get("mr_no"));
              userMrnoAssociationBean = userMrnoAssociationRepository.findByKey(userMrnoFilter);
              if (userConfidentialityAssocBean == null && userMrnoAssociationBean == null) {
                mask = true;
              }
            }
            apptMap = new HashMap<String, Object>();
            apptMap.put("appointment_id", appointmentBean.get("appointment_id"));
            apptMap.put("mr_no", appointmentBean.get("mr_no"));
            apptMap.put("visit_id", appointmentBean.get("visit_id"));
            apptMap.put("center_id", appointmentBean.get("center_id"));
            apptMap.put("center_code", appointmentBean.get("center_code"));
            apptMap.put("center_name", appointmentBean.get("center_name"));
            apptMap.put("booked_by", appointmentBean.get("booked_by"));
            apptMap.put("booked_time", appointmentBean.get("booked_time"));
            apptMap.put("changed_by", appointmentBean.get("changed_by"));
            apptMap.put("changed_time", appointmentBean.get("changed_time"));
            apptMap.put("resource_type", appointmentBean.get("resource_type"));
            apptMap.put("resource_id", appointmentBean.get("resource_id"));
            apptMap.put("res_sch_category", appointmentBean.get("res_sch_category"));
            apptMap.put("res_sch_type", appointmentBean.get("res_sch_type"));
            apptMap.put("presc_doc_id", appointmentBean.get("presc_doc_id"));
            apptMap.put("presc_doctor", appointmentBean.get("presc_doctor"));
            apptMap.put("resource_name", appointmentBean.get("resource_name"));
            apptMap.put("dept_name", appointmentBean.get("dept_name"));
            apptMap.put("patient_name", appointmentBean.get("patient_name"));
            apptMap.put("appointment_status", appointmentBean.get("appointment_status"));
            apptMap.put("appointment_visit_type", appointmentBean.get("appointment_visit_type"));
            apptMap.put("patient_contact", appointmentBean.get("patient_contact"));
            apptMap.put("appointment_time", new DateUtil().getTimeStampFormatterSecs()
                .format(appointmentBean.get("appointment_time")));
            apptMap.put("appointment_source_name", appointmentBean.get("appointment_source_name"));
            apptMap.put("duration", appointmentBean.get("duration"));
            apptMap.put("cancel_reason", appointmentBean.get("cancel_reason"));
            apptMap.put("cancel_type", appointmentBean.get("cancel_type"));
            apptMap.put("complaint", appointmentBean.get("complaint"));
            apptMap.put("remarks", appointmentBean.get("remarks"));
            apptMap.put("vip_status", appointmentBean.get("vip_status"));
            apptMap.put("res_sch_name", appointmentBean.get("res_sch_name"));
            apptMap.put("abbreviation", appointmentBean.get("abbreviation"));
            apptMap.put("appointment_reason_detail",
                appointmentBean.get("appointment_reason_detail"));
            apptMap.put("package_id", appointmentBean.get("package_id"));
            // apptMap.put("contact_id", appointmentBean.get("contact_id"));
            apptMap.put("patient_gender_text", appointmentBean.get("patient_gender_text"));
            apptMap.put("age", appointmentBean.get("age"));
            apptMap.put("age_in", appointmentBean.get("age_in"));
            apptMap.put("send_sms", appointmentBean.get("send_sms"));
            apptMap.put("send_email", appointmentBean.get("send_email"));
            apptMap.put("preferred_language", appointmentBean.get("lang_code"));
            apptMap.put("waitlist", appointmentBean.get("waitlist"));
            apptMap.put("visit_mode", appointmentBean.get("visit_mode"));

            if (mask) {
              apptMap.put("mr_no", "Xxxxxxxxxxxxxxxxx");
              apptMap.put("visit_id", "Xxxxxxxxxxxxxxxxx");
              apptMap.put("patient_name", "Xxxxxxxxxxxxxxxxx");
              apptMap.put("patient_contact", "Xxxxxxxxxxxxxxxxx");
              apptMap.put("is_patient_group_accessible", "N");
            }
            if (pushMap.containsKey(appointmentBean.get("prim_res_id"))) {

              ((List<Map>) pushMap.get((String) appointmentBean.get("prim_res_id"))).add(apptMap);

            } else {

              List<Map> apptMapsList = new ArrayList<Map>();
              apptMapsList.add(apptMap);
              pushMap.put((String) appointmentBean.get("prim_res_id"), apptMapsList);
            }
          }
        }
        this.pushService.pushToUser((String) user.get("emp_username"), WEBSOCKET_PUSH_CHANNEL,
            pushMap);
      }
    }
  }

  /**
   * Validate.
   *
   * @param category the category
   * @param patientInfo the patient info
   * @param appointmentInfo the appointment info
   * @param additionalInfo the additional info
   * @return true, if successful
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public boolean validate(AppointmentCategory category, Map<String, Object> patientInfo,
      Map<String, Object> appointmentInfo, Map<String, Object> additionalInfo, String visitMode) {
    ValidationErrorMap validationErrors;
    Map<String, Object> nestedException = new HashMap<>();
    Integer duration = 0;
    if (appointmentInfo.get("duration") != null) {
      duration = (Integer)appointmentInfo.get("duration");
    }
    // TODO: get scheduler_master bean and get the info
    /*
     * else if (scheduleBean != null) { duration =
     * ((Integer)scheduleBean.get("default_duration")).intValue(); }
     */
    if (duration > 999) {
      validationErrors = new ValidationErrorMap();
      validationErrors.addError("duration", "exception.scheduler.invalid.duration");
      ValidationException ex = new ValidationException(validationErrors);
      nestedException.put("appointment", ex.getErrors());
    }
    String apptDate = (String) appointmentInfo.get("date");
    String slotTime = (String) appointmentInfo.get("slot_time");
    String secondaryResId = String.valueOf(appointmentInfo.get("secondary_resource_id"));
    if (apptDate == null || apptDate.equals("")) {
      validationErrors = new ValidationErrorMap();
      validationErrors.addError("date", "exception.scheduler.appointment.date");
      ValidationException ex = new ValidationException(validationErrors);
      nestedException.put("appointment", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    if (slotTime == null || slotTime.equals("")) {
      validationErrors = new ValidationErrorMap();
      validationErrors.addError("slot_time", "exception.scheduler.appointment.slot_time");
      ValidationException ex = new ValidationException(validationErrors);
      nestedException.put("appointment", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    if (secondaryResId == null || secondaryResId.equals("")) {
      validationErrors = new ValidationErrorMap();
      if (category.getCategory().equals("DOC")) {
        validationErrors.addError("secondary_resource_id",
            "exception.scheduler.appointment.cty.mandatory");
      } else {
        validationErrors.addError("secondary_resource_id",
            "exception.scheduler.appointment.secondary.resource");
      }
      ValidationException ex = new ValidationException(validationErrors);
      nestedException.put("appointment", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    String primResId = (String) appointmentInfo.get("primary_resource_id");
    if (primResId == null || primResId.equals("")) {
      validationErrors = new ValidationErrorMap();
      validationErrors.addError("primary_resource_id",
          "exception.scheduler.appointment.primary.resource");
      ValidationException ex = new ValidationException(validationErrors);
      nestedException.put("appointment", ex.getErrors());
    }
    String appointmentStatus = (String) appointmentInfo.get("status");
    if (appointmentStatus == null || "".equals(appointmentStatus)) {
      validationErrors = new ValidationErrorMap();
      validationErrors.addError("status", "scheduler.validation.error.appointmentStatus");
      ValidationException ex = new ValidationException(validationErrors);
      nestedException.put("appointment", ex.getErrors());
    } else if (!Arrays.asList("Cancel", "Confirmed", "Booked", "Noshow", "Completed", "Arrived")
        .contains(appointmentStatus)) {
      validationErrors = new ValidationErrorMap();
      validationErrors.addError("status", "scheduler.validation.error.invalidStatus");
      ValidationException ex = new ValidationException(validationErrors);
      nestedException.put("appointment", ex.getErrors());
    }
    String timestampStr = apptDate + " " + slotTime;
    Timestamp apptTime = null;
    try {
      DateUtil dateUtil = new DateUtil();
      apptTime = dateUtil.parseTheTimestamp(timestampStr);
    } catch (ParseException pe) {
      validationErrors = new ValidationErrorMap();
      ValidationException ex = new ValidationException(validationErrors);
      nestedException.put("exception.scheduler.appointment.invalid.date", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    long apptTimeLong = apptTime.getTime();
    apptTimeLong = apptTimeLong + (duration * 60 * 1000);
    Timestamp endTimestamp = new java.sql.Timestamp(apptTimeLong);
    validationErrors = new ValidationErrorMap();
    String practoAppointmentId = (String) appointmentInfo.get("practo_appointment_id");
    if (!appointmentValidator.isUSerAllowedToOverBookAppt(primResId, apptTime,
        category.getCategory())) {
      validationErrors.addError("slot_time", "ui.error.overbook.not.allowed");
      ValidationException ex = new ValidationException(validationErrors);
      nestedException.put("appointment", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    if ((practoAppointmentId == null || practoAppointmentId.equals(""))
        && !appointmentValidator.validateWithinSameDay(apptTime, endTimestamp, validationErrors)) {
      ValidationException ex = new ValidationException(validationErrors);
      nestedException.put("appointment", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    Map<String, String> actionRightsMap = (Map<String, String>) securityService
        .getSecurityAttributes().get("actionRightsMap");
    if (apptTime.before(DateUtil.getCurrentTimestamp())
        && !("A".equals(actionRightsMap.get("allow_backdated_app"))
            || String.valueOf(actionRightsMap.get("all_rights")).equals("true"))) {
      validationErrors = new ValidationErrorMap();
      validationErrors.addError("date", "exception.scheduler.appointment.past.date.time");
      ValidationException ex = new ValidationException(validationErrors);
      nestedException.put("appointment", ex.getErrors());
    }
    /* Validate if appointment slot is available */
    String resType = category.getPrimaryResourceType();
    Integer appointmentId = appointmentInfo.get("appointment_id") != null
        ? (Integer) appointmentInfo.get("appointment_id")
        : null;

    String appId = null;
    if (appointmentId != null) {
      appId = appointmentId.toString();
    }
    if ((practoAppointmentId == null || practoAppointmentId.equals(""))
        && !appointmentValidator.validateIfSlotOverbooked(category, primResId, resType, apptTime,
            endTimestamp, validationErrors, "slot_time", appId)) {
      ValidationException ex = new ValidationException(validationErrors);
      nestedException.put("appointment", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }

    validationErrors = new ValidationErrorMap();
    String mrNo = patientInfo.get("mr_no") != null ? (String) patientInfo.get("mr_no") : null;
    Integer contactId = (Integer) patientInfo.get("contact_id");
    String patientName = patientInfo.get("patient_name") != null
        ? (String) patientInfo.get("patient_name")
        : null;
    String patientContact = patientInfo.get("patient_contact") != null
        ? (String) patientInfo.get("patient_contact")
        : null;
    Integer centerId = appointmentInfo.get("center_id") != null
        ? (Integer) appointmentInfo.get("center_id")
        : (RequestContext.getCenterId() != null ? RequestContext.getCenterId() : 0);

    BasicDynaBean bean = appointmentRepository.findByKey("appointment_id", appointmentId);
    if (!(bean != null && bean.get("package_id") != null)) {
      if ((practoAppointmentId == null
          || (practoAppointmentId != null && practoAppointmentId.isEmpty()))
          && !appointmentValidator.validateIfAppointmentExistsForPatient(apptTime, endTimestamp,
              appId, mrNo, patientName, patientContact, validationErrors, contactId)) {

        ValidationException ex = new ValidationException(validationErrors);
        nestedException.put("patient", ex.getErrors());
      }
    } else {
      if ((practoAppointmentId == null
          || (practoAppointmentId != null && practoAppointmentId.isEmpty()))
          && !appointmentValidator.validateIfAppointmentExistsForPatientForSamePackage(apptTime,
              endTimestamp, appId, mrNo, patientName, patientContact, validationErrors,
              (int) bean.get("package_id"), (int) bean.get("appointment_pack_group_id"))) {

        ValidationException ex = new ValidationException(validationErrors);
        nestedException.put("patient", ex.getErrors());
      }
    }

    validationErrors = new ValidationErrorMap();

    if (!appointmentValidator.validateResourcesAvailability(category, apptTime, endTimestamp,
        apptDate, resType.equals("OPDOC") ? "DOC" : resType, primResId, centerId, validationErrors,
        "secondary_resource_id_key", null,visitMode)) {
      ValidationException ex = new ValidationException(validationErrors);
      nestedException.put("appointment", ex.getErrors());
    }

    if (additionalInfo != null) {
      validationErrors = new ValidationErrorMap();
      if (!appointmentValidator.validateMemberId(additionalInfo, validationErrors)) {
        ValidationException ex = new ValidationException(validationErrors);
        nestedException.put("additional_info", ex.getErrors());
      }
    }

    // check additional resources availability
    validationErrors = new ValidationErrorMap();
    List resourceTypes = new ArrayList<>();
    List resourceValues = new ArrayList<>();
    List<Map> insertList = (appointmentInfo.get("additional_resources_insert") != null
        ? (List<Map>) appointmentInfo.get("additional_resources_insert")
        : null);
    if (insertList != null) {
      for (Map row : insertList) {
        resourceTypes.add(row.get("resource_type"));
        resourceValues.add(row.get("resource_id"));
      }
    }

    if (resourceTypes != null) {
      for (int i = 0; i < resourceTypes.size(); i++) {
        if (!resourceTypes.get(i).equals("") && !resourceValues.get(i).equals("")) {
          if (!appointmentValidator.validateResourcesAvailability(category, apptTime, endTimestamp,
              apptDate, (String) resourceTypes.get(i), (String) resourceValues.get(i), centerId,
              validationErrors, null, null,visitMode)) {
            ValidationException ex = new ValidationException(validationErrors);
            Map<String, Object> resourceIdsMap = new HashMap<String, Object>();
            if (resourceIdsMap.get("resources_id") != null) {
              Map<String, Object> temp = (Map<String, Object>) resourceIdsMap.get("resources_id");
              temp.putAll(ex.getErrors());
            } else {
              resourceIdsMap.put("additional_resources_insert", ex.getErrors());
            }
            if ((Map<String, Object>) nestedException.get("appointment") != null) {
              Map<String, Object> temp = (Map<String, Object>) nestedException.get("appointment");
              temp.putAll(resourceIdsMap);
            } else {
              nestedException.put("appointment", resourceIdsMap);
            }
          }
        }
      }
    }
    validationErrors = new ValidationErrorMap();
    if (resourceTypes != null) {
      for (int i = 0; i < resourceTypes.size(); i++) {
        if (!resourceTypes.get(i).equals("") && !resourceValues.get(i).equals("")) {
          if (!appointmentValidator.validateIfSlotOverbooked(category,
              (String) resourceValues.get(i), (String) resourceTypes.get(i), apptTime, endTimestamp,
              validationErrors, null, appId)) {
            ValidationException ex = new ValidationException(validationErrors);
            Map<String, Object> resourceIdsMap = new HashMap<String, Object>();
            if (resourceIdsMap.get("resources_id") != null) {
              Map<String, Object> temp = (Map<String, Object>) resourceIdsMap.get("resources_id");
              temp.putAll(ex.getErrors());
            } else {
              resourceIdsMap.put("additional_resources_insert", ex.getErrors());
            }
            if ((Map<String, Object>) nestedException.get("appointment") != null) {
              Map<String, Object> temp = (Map<String, Object>) nestedException.get("appointment");
              temp.putAll(resourceIdsMap);
            } else {
              nestedException.put("appointment", resourceIdsMap);
            }
          }
        }
      }
    }

    if (!nestedException.isEmpty()) {
      throw new NestableValidationException(nestedException);
    }
    return true;
  }

  /**
   * Gets the appointments.
   *
   * @param appointmentCategory the appointment category
   * @param params              the params
   * @return the appointments
   */
  public List<BasicDynaBean> getAppointments(AppointmentCategory appointmentCategory,
      Map<String, String[]> params) {
    Date startDate = null;
    Date endDate = null;
    Timestamp modifiedAfter = null;
    ValidationErrorMap validationErrors;
    String[] resourceIds = null;
    String[] skipApptsWithStatus = null;
    Map<String, Object> nestedException = new HashMap<String, Object>();
    String userName = (String) sessionService.getSessionAttributes().get("userId");
    try {
      startDate = DateUtil.parseDate(params.get("start_date")[0]);
      endDate = DateUtil.parseDate(params.get("end_date")[0]);
      resourceIds = params.get("resources");
      skipApptsWithStatus = params.get("skip_status");
      DateUtil dateUtil = new DateUtil();
      modifiedAfter = params.get("modified_after") != null
          ? dateUtil.parseTheTimestamp(params.get("modified_after")[0])
          : null;
    } catch (ParseException pe) {
      validationErrors = new ValidationErrorMap();
      ValidationException ex = new ValidationException(validationErrors);
      nestedException.put("invalid_appointmentDateTime", ex.getErrors());
    }
    if (!nestedException.isEmpty()) {
      throw new NestableValidationException(nestedException);
    }
    Integer centerId = Integer.parseInt(params.get("center_id")[0]);
    Integer centersIncDefault = Integer.parseInt(params.get("max_centers_inc_default")[0]);
    return appointmentRepository.getAppointments(appointmentCategory, resourceIds, centerId,
        centersIncDefault, startDate, endDate, modifiedAfter, skipApptsWithStatus, userName);
  }

  /**
   * Schedule appointment msg.
   *
   * @param uniqueId        the unique id
   * @param jobName         the job name
   * @param appointmentTime the appointment time
   */
  private void scheduleAppointmentMsg(Integer uniqueId, String jobName, Timestamp appointmentTime) {
    String schema = RequestContext.getSchema();
    String uniString = String.valueOf(uniqueId);
    Timestamp msgSendinTimeStamp = msgSendingTime(appointmentTime);

    if (msgSendinTimeStamp.getTime() > System.currentTimeMillis()) {
      String msgSendingTime = msgSendinTimeStamp.toString();
      Map<String, Object> jobData = new HashMap<String, Object>();
      jobData.put("params", uniString);
      jobData.put("schema", RequestContext.getSchema());
      if (jobService != null) {
        jobService.scheduleAt(buildJob("DynamicAppointmentReminderJob" + uniString,
            DynamicAppointmentReminderJob.class, jobData), msgSendinTimeStamp);
      }
    }
  }

  /**
   * Msg sending time.
   *
   * @param jobTime the job time
   * @return the timestamp
   */
  /*
   * Adjust time at which message need to be send.
   */
  private Timestamp msgSendingTime(Timestamp jobTime) {
    Map<String,Object> findMap  = new HashMap<>();
    findMap.put("message_type_id", "sms_dynamic_appointment_reminder");
    findMap.put("param_name", "buffer_hours");
    BasicDynaBean configBean;
    Integer hourInMilliSec = 60 * 60 * 1000;
    int bufferHour = 2;
    configBean = new GenericRepository("message_config").findByKey(findMap);
    bufferHour = Integer.parseInt(configBean.get("param_value").toString());
    Timestamp msgTime = new Timestamp(jobTime.getTime() - bufferHour * hourInMilliSec);
    return msgTime;
  }
  
  /**
   * Check Dynamic Sms Allowed to Send.
   *
   * @param appointmentStatus the appointment status
   * @return the boolean
   */
  private Boolean checkDynamicSmsAllowedtoSend(String appointmentStatus) {
    Map<String, Object> findMapSMS = new HashMap<>();
    findMapSMS.put("message_type_id", "sms_dynamic_appointment_reminder");
    findMapSMS.put("param_name", "status");
    Map<String, Object> findMapEMAIL = new HashMap<>();
    findMapEMAIL.put("param_name", "status");
    findMapEMAIL.put("message_type_id", "email_dynamic_appointment_reminder");
    BasicDynaBean configBeanSMS;
    BasicDynaBean configBeanEMAIL;
    configBeanSMS = new GenericRepository("message_config").findByKey(findMapSMS);
    configBeanEMAIL = new GenericRepository("message_config").findByKey(findMapEMAIL);
    if (APPOINTMENT_STATUS_FOR_DYNAMIC_SMS.contains(appointmentStatus.toLowerCase())) {
      String configStatusSMS = configBeanSMS.get("param_value").toString().toLowerCase();
      String configStatusEMAIL = configBeanEMAIL.get("param_value").toString().toLowerCase();
      return (configStatusSMS.equals(appointmentStatus.toLowerCase())
          || configStatusSMS.equals("both"))
          || configStatusEMAIL.equals(appointmentStatus.toLowerCase())
          || configStatusEMAIL.equals("both");
    }
    return false;
  }

  /**
   * Gets the resource appointments.
   *
   * @param resourceId   the resource id
   * @param resourceType the resource type
   * @param startTime    the start time
   * @param endTime      the end time
   * @return the resource appointments
   */
  public List<BasicDynaBean> getResourceAppointments(String resourceId, String resourceType,
      Timestamp startTime, Timestamp endTime) {
    return appointmentRepository.getResourceAppointments(resourceId, resourceType, startTime,
        endTime);
  }

  /**
   * Online appointments exists.
   *
   * @param resourceId the resource id
   * @param currentDateTime the current date time
   * @param visitMode the visit mode
   * @return the boolean
   */
  public Boolean onlineAppointmentsExists(String resourceId, 
      Timestamp currentDateTime, String visitMode) {
    return appointmentRepository.onlineAppointmentsExists(resourceId, currentDateTime, visitMode);
  }

  /**
   * Gets the appointment source.
   *
   * @param appointmentId the appointment id
   * @return the appointment source
   */
  public String getAppointmentSource(Integer appointmentId) {
    return appointmentRepository.getAppointmentSource(appointmentId);
  }

  /**
   * Gets the appointment status list.
   *
   * @param category the category
   * @return the appointment status list
   */
  public List<BasicDynaBean> getAppointmentStatusList(String category) {
    List<String> columns = new ArrayList();
    columns.add("status_name");
    columns.add("status_description");
    List<BasicDynaBean> statusList = new GenericRepository("scheduler_status").listAll(columns,
        "category", category);
    return statusList;
  }

  /**
   * Gets the appointment details.
   *
   * @param category      the category
   * @param appointmentId the appointment id
   * @return the appointment details
   */
  public Map<String, Object> getAppointmentDetails(AppointmentCategory category,
      Integer appointmentId) {
    logger.debug("staring appointment details method" + DateUtil.getCurrentTimestamp());
    List<BasicDynaBean> appointmentDetails = appointmentRepository.getAppointmentDetails(category,
        appointmentId);
    Map<String, Object> apptMap = new HashMap<String, Object>();
    logger.debug("size of appt details list : " + appointmentDetails.size());
    if (appointmentDetails != null && appointmentDetails.size() > 0) {
      Map<String, Object> patientMap = null;
      Map<String, Object> appointmentMap = null;
      Map<String, Object> additionalInfoMap = null;
      List<Map> resourcesList = new ArrayList<Map>();
      Map<String, Object> resourceMap = null;
      for (BasicDynaBean appt : appointmentDetails) {
        if (apptMap != null && apptMap.size() > 0) {
          patientMap = (Map<String, Object>) apptMap.get("patient");
          appointmentMap = (Map<String, Object>) apptMap.get("appointment");
          additionalInfoMap = (Map<String, Object>) apptMap.get("additional_info");
        } else {
          apptMap = new HashMap<String, Object>();
          patientMap = new HashMap<String, Object>();
          appointmentMap = new HashMap<String, Object>();
          additionalInfoMap = new HashMap<String, Object>();
          appointmentMap.put("additional_resources", resourcesList);
          apptMap.put("appointment", appointmentMap);
          apptMap.put("patient", patientMap);
          apptMap.put("additional_info", additionalInfoMap);
        }
        if (appt.get("primary_resource") != null
            && (Boolean) appt.get("primary_resource") == true) {
          patientMap.put("mr_no", appt.get("mr_no"));
          patientMap.put("salutation_name", appt.get("salutation_name"));
          patientMap.put("patient_name", appt.get("patient_name"));
          patientMap.put("last_name", appt.get("last_name"));
          patientMap.put("patient_dob", appt.get("patient_dob"));
          patientMap.put("patient_age", appt.get("patient_age"));
          patientMap.put("patient_age_units", appt.get("patient_age_units"));
          if (appt.get("patient_age") == null) {
            if (appt.get("patient_dob") != null || appt.get("expected_dob") != null) {
              String ageStr = (String) appt.get("age_text");
              patientMap.put("patient_age", ageStr.equals("00:00:00") 
                  ? 0 : Integer.parseInt(ageStr.split(" ")[0]));
              patientMap.put("patient_age_units",
                  ageStr.equals("00:00:00") 
                  ? "D" : ageStr.split(" ")[1].toUpperCase().substring(0,1));
            }
          }
          patientMap.put("patient_gender", appt.get("patient_gender"));
          patientMap.put("patient_email_id", appt.get("patient_email_id"));
          patientMap.put("send_sms", appt.get("send_sms"));
          patientMap.put("send_email", appt.get("send_email"));
          patientMap.put("preferred_language", appt.get("lang_code"));

          patientMap.put("patient_contact", appt.get("patient_contact"));
          patientMap.put("vip_status", appt.get("vip_status"));
          patientMap.put("scheduler_visit_type", appt.get("scheduler_visit_type"));
          apptMap.put("patient", patientMap);

          appointmentMap.put("appointment_id", appt.get("appointment_id"));
          appointmentMap.put("category", category.getCategory());
          appointmentMap.put("secondary_resource_id", appt.get("res_sch_name"));
          appointmentMap.put("primary_resource_id", appt.get("prim_res_id"));
          appointmentMap.put("center_id", appt.get("center_id"));
          appointmentMap.put("date", appt.get("text_appointment_date"));
          appointmentMap.put("slot_time", appt.get("appointment_date_time"));
          appointmentMap.put("duration", appt.get("duration"));
          appointmentMap.put("complaint", appt.get("complaint"));
          appointmentMap.put("remarks", appt.get("remarks"));
          appointmentMap.put("visit_mode",  appt.get("visit_mode"));
          appointmentMap.put("status", appt.get("appointment_status"));
          appointmentMap.put("presc_doc_id", appt.get("presc_doc_id"));
          appointmentMap.put("presc_doc_name", appt.get("presc_doc_name"));
          appointmentMap.put("app_source_id", appt.get("appointment_source_id"));
          appointmentMap.put("practo_appointment_id", appt.get("practo_appointment_id"));
          appointmentMap.put("teleconsult_url", appt.get("teleconsult_url"));

          additionalInfoMap.put("patient_category_id", appt.get("patient_category"));
          additionalInfoMap.put("patient_address", appt.get("patient_address"));
          additionalInfoMap.put("patient_area", appt.get("patient_area"));
          additionalInfoMap.put("patient_city", appt.get("patient_city"));
          additionalInfoMap.put("patient_state", appt.get("patient_state"));
          additionalInfoMap.put("patient_country", appt.get("patient_country"));
          additionalInfoMap.put("patient_nationality", appt.get("patient_nationality"));
          additionalInfoMap.put("patient_citizen_id", appt.get("patient_citizen_id"));
          additionalInfoMap.put("primary_sponsor_id", appt.get("primary_sponsor_id"));
          additionalInfoMap.put("primary_sponsor_name", appt.get("tpa_name"));
          additionalInfoMap.put("primary_insurance_co", appt.get("primary_sponsor_co"));
          additionalInfoMap.put("plan_type_id", appt.get("plan_type_id"));
          additionalInfoMap.put("plan_id", appt.get("plan_id"));
          additionalInfoMap.put("member_id", appt.get("member_id"));
          apptMap.put("additional_info", additionalInfoMap);
        } else if (appt.get("primary_resource") != null
            && (Boolean) appt.get("primary_resource") == false) {
          if (appointmentMap.get("additional_resources") == null
              || (appointmentMap.get("additional_resources") != null
                  && ((List<Map>) appointmentMap.get("additional_resources")).size() <= 0)) {
            resourcesList = new ArrayList<Map>();
            appointmentMap.put("additional_resources", resourcesList);
          }
          resourcesList = (List<Map>) appointmentMap.get("additional_resources");
          resourceMap = new HashMap<String, Object>();
          resourceMap.put("resource_type", appt.get("resource_type"));
          resourceMap.put("resource_id", appt.get("resource_id"));
          resourcesList.add(resourceMap);
        }
      }
    }
    logger.debug("ending appointment details method" + DateUtil.getCurrentTimestamp());
    return apptMap;
  }

  /**
   * Gets the total recurrences.
   *
   * @param recMap the rec map
   */
  public void getTotalRecurrences(Map recMap) {
    ArrayList scheduleAppointBeanList = (ArrayList) recMap.get("scheduleAppointBeanList");
    Appointment appt = (Appointment) scheduleAppointBeanList.get(0);
    Timestamp appointmentTime = appt.getAppointmentTime();
    ArrayList scheduleAppointItemBean = (ArrayList) recMap.get("scheduleAppointItemBean");
    ArrayList scheduleAppointItemBeanRecuured = (ArrayList) recMap
        .get("scheduleAppointItemBeanRecuured");
    RecurrencePattern recurrenceBean = (RecurrencePattern) recMap.get("recurrPattern");
    List<Integer> apptIdsList = (List<Integer>) recMap.get("apptIds");
    AppointmentCategory apptCategory = (AppointmentCategory) recMap.get("category");
    List<AppointmentCategory> apptCatsList = (List<AppointmentCategory>) recMap.get("apptCats");
    ValidationErrorMap errorsMap = new ValidationErrorMap();
    List<Appointment> repeatAppointments = new ArrayList<Appointment>();
    List<Timestamp> startTimeSlots = new ArrayList<Timestamp>();
    List<Timestamp> endTimeSlots = new ArrayList<Timestamp>();

    recMap.put("appointmentDetail", appt);
    recMap.put("errorsMap", errorsMap);

    Integer recurranceNo = recurrenceBean.getRecurrNo();
    Integer occuranceNo = 0;
    String recurrenceOption = recurrenceBean.getRecurranceOption();
    String recurrMonthOption = recurrenceBean.getMonthlyRecurrType();
    java.util.Date recurrDate = recurrenceBean.getRecurrDate();
    java.util.Date untilDate = null;
    java.util.Date apptDate = null;
    int daysDiff = 0;

    Calendar cal = Calendar.getInstance();

    if (recurrDate == null) {
      cal.setTime(appointmentTime);
      apptDate = new java.util.Date(appointmentTime.getTime());
    } else {
      cal.setTime(recurrDate);
      apptDate = recurrDate;
    }

    if (recurrenceBean.getOccurrNo() != null && recurrenceBean.getOccurrNo() > 0) {
      occuranceNo = recurrenceBean.getOccurrNo();
      occuranceNo--;

    } else if (recurrenceBean.getUntilDate() != null) {
      untilDate = recurrenceBean.getUntilDate();

      Calendar cal1 = Calendar.getInstance();
      cal1.setTime(apptDate);

      Calendar cal2 = Calendar.getInstance();
      cal2.setTime(untilDate);

      if (recurranceNo > 0) {
        if (recurrenceOption.equals("D")) {
          occuranceNo = daysBetween(cal1, cal2);
          occuranceNo = occuranceNo / recurranceNo;
        } else if (recurrenceOption.equals("W")) {
          if (recurrenceBean.getUntilDate() != null) {
            Calendar startDate = Calendar.getInstance();
            startDate.setTime(apptDate);
            daysDiff = daysBetween(startDate, cal2);
          }
          occuranceNo = weeksBetween(cal1, cal2);
          occuranceNo = occuranceNo / recurranceNo;
        } else if (recurrenceOption.equals("M")) {
          if (recurrMonthOption.equals("D_M") || recurrMonthOption.equals("D_W")) {
            occuranceNo = monthsBetween(cal1, cal2);
            occuranceNo = occuranceNo / recurranceNo;
          }
        } else if (recurrenceOption.equals("Y")) {
          occuranceNo = yearsBetween(cal1, cal2);
          occuranceNo = occuranceNo / recurranceNo;
        }
      }
    }
    logger.debug("{}", recurranceNo);
    logger.debug("{}", occuranceNo);
    logger.debug(recurrenceOption);
    if (recurranceNo != null && occuranceNo != null) {
      if (recurranceNo > 0 && occuranceNo > 0) {

        if (recurrenceOption.equals("D")) {

          for (int i = 0; i < occuranceNo; i++) {

            Integer appointmentId = appointmentRepository.getNextSequence();
            apptIdsList.add(appointmentId);
            apptCatsList.add(apptCategory);
            cal.add(Calendar.DATE, recurranceNo);
            Appointment recurAppt = new Appointment(appointmentId);
            Timestamp nextAppointmenttime = new java.sql.Timestamp(cal.getTime().getTime());
            recurAppt.setAppointmentId(appointmentId);
            recurAppt.setAppointmentTime(nextAppointmenttime);
            setEndTimeStamp(recMap, nextAppointmenttime, startTimeSlots, endTimeSlots);
            recMap.put("startTime", startTimeSlots);
            recMap.put("endTime", endTimeSlots);
            repeatAppointments.add(recurAppt);
          }
        } else if (recurrenceOption.equals("W")) {
          Calendar appointDay = Calendar.getInstance();
          appointDay.setTime(appointmentTime);
          int firstOccur = appointDay.get(Calendar.DAY_OF_WEEK);
          int daysCount = 1;
          int weekCount = 0;
          int repetition = 1;
          int endsOnRecur = recurranceNo;
          List week = new ArrayList();
          week = recurrenceBean.getWeek();
          int count = 0;
          int iterate = 0;
          for (int l = 0; l < week.size(); l++) {
            iterate++;
            if (firstOccur == (int) week.get(l)) {
              break;
            }
          }
          if (week.size() > 1) {
            if (week.size() == iterate) {
              count = 0;
            } else {
              count = iterate;
            }
          } else {
            count = 0;
          }
          boolean endsOn = recurrenceBean.getUntilDate() != null;
          Calendar cl = Calendar.getInstance();
          if (endsOn) {
            occuranceNo++;
          }
          int daysForRec = (recurranceNo * 7) * occuranceNo;
          if (recurranceNo > 1) {
            if (count == 0) {
              daysCount = daysCount + (endsOnRecur - 1) * 7;
              if (endsOn) {
                daysDiff = daysDiff - (endsOnRecur - 1) * 7;
              }
            }
          }
          for (int j = 0; j < occuranceNo; j++) {
            for (int k = count; k < week.size(); k++) {
              if (endsOn) {
                if (weekCount > occuranceNo) {
                  break;
                }
              } else {
                if (repetition > occuranceNo) {
                  break;
                }
              }
              int weekDay = Integer.parseInt(week.get(k).toString());
              for (int i = daysCount; i <= daysForRec; i++) {
                cl.setTime(appointmentTime);
                cl.add(Calendar.DATE, i);
                daysCount++;
                count = 0;
                if (endsOn) {
                  daysDiff--;
                }
                int calDay = cl.get(Calendar.DAY_OF_WEEK);
                if (calDay == weekDay && daysDiff >= 0) {
                  Integer appointmentId = appointmentRepository.getNextSequence();
                  apptIdsList.add(appointmentId);
                  apptCatsList.add(apptCategory);
                  Appointment recurAppt = new Appointment(appointmentId);
                  Timestamp nextAppointmenttime = new java.sql.Timestamp(cl.getTime().getTime());
                  recurAppt.setAppointmentId(appointmentId);
                  recurAppt.setAppointmentTime(nextAppointmenttime);
                  setEndTimeStamp(recMap, nextAppointmenttime, startTimeSlots, endTimeSlots);
                  recMap.put("startTime", startTimeSlots);
                  recMap.put("endTime", endTimeSlots);
                  repeatAppointments.add(recurAppt);
                  repetition++;
                  break;
                }
              }
            }
            if (recurranceNo > 1) {
              endsOnRecur = recurranceNo;
              daysCount = daysCount + (endsOnRecur - 1) * 7;
              if (endsOn) {
                daysDiff = daysDiff - (endsOnRecur - 1) * 7;
              }
            }
            if (endsOn) {
              weekCount++;
            }
          }
        } else if (recurrenceOption.equals("M")) {
          if (recurrMonthOption.equals("D_M")) {
            java.util.Date recurrDateAndTime = null;
            recurrDateAndTime = recurrenceBean.getRecurrDate();
            java.util.Date endsOnDate = recurrenceBean.getUntilDate();
            Calendar endsRecurr = null;
            int endDay = 0;
            int estimatedDays = 0;
            if (endsOnDate != null) {
              endsRecurr = Calendar.getInstance();
              endsRecurr.setTime(endsOnDate);
              endDay = endsRecurr.get(Calendar.DAY_OF_MONTH);
            }
            Calendar recMon = Calendar.getInstance();
            Calendar nextRecurDay = Calendar.getInstance();
            recMon.setTime(recurrDateAndTime);
            int monthDate = recMon.get(Calendar.DAY_OF_MONTH);
            int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
            int maxDays = recMon.getActualMaximum(Calendar.DAY_OF_MONTH);
            boolean recurCheck = true;
            boolean isLastRecurrence = false;
            if (dayOfMonth == monthDate) {
              cal.add(Calendar.MONTH, recurranceNo);
            } else {
              cal = recMon;
            }

            for (int i = 1; i <= occuranceNo; i++) {
              nextRecurDay.setTime(recurrDateAndTime);
              nextRecurDay.add(Calendar.MONTH, recurranceNo * i);

              if (i == occuranceNo && endsOnDate != null) {
                boolean isLeap = nextRecurDay.get(Calendar.YEAR) % 4 == 0;
                int month = nextRecurDay.get(Calendar.MONTH);
                if (month == 7 || month == 12) {
                  estimatedDays = 31;
                }
                if (month == 2) {
                  if (isLeap) {
                    estimatedDays = 29;
                  } else {
                    estimatedDays = 28;
                  }
                } else {
                  estimatedDays = 30;
                }

                if ((endsRecurr.getTimeInMillis() - nextRecurDay.getTimeInMillis())
                    / (24 * 60 * 60 * 1000) < 0) {
                  recurCheck = false;
                  break;
                }
              }

              Integer appointmentId = appointmentRepository.getNextSequence();
              apptIdsList.add(appointmentId);
              apptCatsList.add(apptCategory);
              if (monthDate == maxDays) {
                dayOfMonth = nextRecurDay.getActualMaximum(Calendar.DAY_OF_MONTH);
                while (nextRecurDay.get(Calendar.DAY_OF_MONTH) != dayOfMonth) {
                  if (!isLastRecurrence) {
                    nextRecurDay.add(Calendar.DATE, 1);
                  } else {
                    recurCheck = false;
                    break;
                  }
                }
              } else {
                dayOfMonth = nextRecurDay.get(Calendar.DAY_OF_MONTH);
              }
              int lastMonthDay = monthDate;
              if (dayOfMonth < monthDate) {
                while (nextRecurDay.get(Calendar.DAY_OF_MONTH) != monthDate) {
                  lastMonthDay--;
                  if (dayOfMonth == lastMonthDay) {
                    break;
                  }
                }
              }
              if (recurCheck) {
                Appointment recurAppt = new Appointment(appointmentId);
                Timestamp nextAppointmenttime = new java.sql.Timestamp(
                    nextRecurDay.getTime().getTime());
                recurAppt.setAppointmentId(appointmentId);
                recurAppt.setAppointmentTime(nextAppointmenttime);
                setEndTimeStamp(recMap, nextAppointmenttime, startTimeSlots, endTimeSlots);
                recMap.put("startTime", startTimeSlots);
                recMap.put("endTime", endTimeSlots);
                repeatAppointments.add(recurAppt);

                cal.add(Calendar.MONTH, recurranceNo);
              }
            }
          } else {
            int daysForRec = 0;
            Calendar cal1 = null;
            java.util.Date endDate = null;
            Calendar cal2 = null;
            if (recurrenceBean.getUntilDate() != null) {
              cal1 = Calendar.getInstance();
              cal1.setTime(appointmentTime);
              cal2 = Calendar.getInstance();
              endDate = recurrenceBean.getUntilDate();
              cal2.setTime(new Timestamp(endDate.getTime()));
              daysForRec = daysBetween(cal1, cal2);
            } else {
              daysForRec = (recurranceNo * 31) * occuranceNo;
            }
            daysForRec = daysForRec + 1;
            int start = 0;
            int end = 0;
            int counter1 = 1;
            int counter2 = 1;
            int count = 1;
            int occurenceCount = 1;
            int calRecurrenceNo = recurranceNo;
            boolean check = false;
            Calendar cl1 = Calendar.getInstance();
            Calendar cl2 = Calendar.getInstance();
            int weekDay = cal.get(Calendar.DAY_OF_WEEK);
            int monthNo = cal.get(Calendar.MONTH);
            int weekNo = cal.get(Calendar.DAY_OF_WEEK_IN_MONTH);
            int dayInMonth = cal.get(Calendar.DAY_OF_MONTH);
            boolean isLast = isLastWeek(weekDay, dayInMonth, cal, monthNo);
            if (isLast) {
              weekNo = 5;
            }
            int calculateweekNo = weekNo;
            for (counter1 = 0; counter1 <= daysForRec; counter1++) {
              if (occurenceCount <= occuranceNo) {
                if (calculateweekNo == 1) {
                  counter2 = calRecurrenceNo * 30;
                  counter2++;
                  start = 1;
                  end = 7;
                } else if (calculateweekNo == 2) {
                  counter2 = calRecurrenceNo * 30 + 7;
                  counter2++;
                  start = 8;
                  end = 14;
                } else if (calculateweekNo == 3) {
                  counter2 = calRecurrenceNo * 30 + 14;
                  counter2++;
                  start = 15;
                  end = 21;
                } else if (calculateweekNo == 4) {
                  counter2 = calRecurrenceNo * 30 + 21;
                  counter2++;
                  start = 22;
                  end = 28;
                } else {
                  counter2 = calRecurrenceNo * 30 + 28;
                  counter2++;
                  start = 29;
                  end = 31;
                }
              }
              check = false;
              if (occurenceCount <= occuranceNo) {
                while (start <= end && start < daysForRec) {
                  if (daysForRec > 0) {
                    cl1.setTime(apptDate);
                    cl1.add(Calendar.MONTH, calRecurrenceNo);
                    cl1.set(Calendar.DAY_OF_MONTH, 0);
                    cl1.add(Calendar.DATE, start);

                    if (recurrenceBean.getUntilDate() != null) {
                      if ((cal2.getTimeInMillis() - cl1.getTimeInMillis())
                          / (24 * 60 * 60 * 1000) < 0) {
                        break;
                      }
                    }

                    cl2.setTime(apptDate);
                    cl2.set(Calendar.DAY_OF_MONTH, 0);
                    cl2.add(Calendar.DATE, counter2);

                    while ((cl2.getTimeInMillis() - cl1.getTimeInMillis())
                        / (24 * 60 * 60 * 1000) < 0) {
                      cl2.add(Calendar.DATE, 1);
                    }

                    if (cl1.get(Calendar.MONTH) == cl2.get(Calendar.MONTH)) {
                      int monthDay = cl1.get(Calendar.DAY_OF_MONTH);
                      int nextAppointDay = cl2.get(Calendar.DAY_OF_MONTH);

                      if (nextAppointDay > monthDay) {
                        while (cl2.get(Calendar.DAY_OF_MONTH) != monthDay) {
                          cl2.add(Calendar.DATE, -1);
                          nextAppointDay = cl2.get(Calendar.DATE);
                        }
                      } else if (nextAppointDay < monthDay) {
                        while (cl2.get(Calendar.DAY_OF_MONTH) != monthDay) {
                          cl2.add(Calendar.DATE, 1);
                          nextAppointDay = cl2.get(Calendar.DATE);
                        }
                      }

                      if (monthDay == nextAppointDay) {
                        if (cl1.get(Calendar.DAY_OF_WEEK) == weekDay
                            && cl1.get(Calendar.DAY_OF_WEEK_IN_MONTH) == calculateweekNo) {
                          check = true;
                          if (cal2 != null) {
                            cal1.setTime(cl1.getTime());
                            daysForRec = daysBetween(cal1, cal2);
                            daysForRec++;
                          }
                          break;
                        } else {
                          start++;
                          check = false;
                        }
                      } else {
                        start++;
                        check = false;
                      }
                    } else {
                      start++;
                      check = false;
                    }
                  }
                }
                if (!check && calculateweekNo == 5) {
                  calculateweekNo = 4;
                  continue;
                }
              }
              if (check && daysForRec > 0) {
                Integer appointmentId = appointmentRepository.getNextSequence();
                apptIdsList.add(appointmentId);
                apptCatsList.add(apptCategory);
                Appointment recurAppt = new Appointment(appointmentId);
                Timestamp nextAppointmenttime = new java.sql.Timestamp(cl1.getTime().getTime());
                recurAppt.setAppointmentId(appointmentId);
                recurAppt.setAppointmentTime(nextAppointmenttime);
                setEndTimeStamp(recMap, nextAppointmenttime, startTimeSlots, endTimeSlots);
                recMap.put("startTime", startTimeSlots);
                recMap.put("endTime", endTimeSlots);
                repeatAppointments.add(recurAppt);
                if (count == 1) {
                  count++;
                  count = count * recurranceNo;
                } else {
                  count = count + recurranceNo;
                }
                calRecurrenceNo = count;
                calculateweekNo = weekNo;
                occurenceCount++;
              }
            }
          }

        } else if (recurrenceOption.equals("Y")) {

          for (int i = 0; i < occuranceNo; i++) {
            Appointment appointment = (Appointment) recMap.get("appointmentDetail");
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(appointmentTime);
            java.util.Date endsOnDate = recurrenceBean.getUntilDate();
            Calendar endsRecurr = null;
            int endDay = 0;
            if (endsOnDate != null) {
              endsRecurr = Calendar.getInstance();
              endsRecurr.setTime(endsOnDate);
              endDay = endsRecurr.get(Calendar.DAY_OF_MONTH);
            }
            int leapAppointDay = 0;
            if (calendar.get(Calendar.YEAR) % 4 == 0) {
              leapAppointDay = Integer.parseInt((new SimpleDateFormat("dd-MM-yyyy")
                  .format(appointment.getAppointmentTime()).substring(0, 2)));
            }
            int appointmentId = appointmentRepository.getNextSequence();
            apptIdsList.add(appointmentId);
            apptCatsList.add(apptCategory);
            cal.add(Calendar.YEAR, recurranceNo);
            int appointDay = Integer.parseInt((new SimpleDateFormat("dd-MM-yyyy")
                .format(appointment.getAppointmentTime()).substring(0, 2)));
            int monthNo = cal.get(Calendar.MONTH);
            if (monthNo == 1) {
              if (cal.get(Calendar.YEAR) % 4 == 0) {
                if (leapAppointDay == 28) {
                  cal.add(Calendar.DATE, 0);
                } else if (endDay == 28 && i == occuranceNo - 1) {
                  continue;
                } else if (appointDay == 28 || appointDay == 29) {
                  cal.add(Calendar.DATE, 1);
                }
              }
            }
            Appointment recurAppt = new Appointment(appointmentId);
            Timestamp nextAppointmenttime = new java.sql.Timestamp(cal.getTime().getTime());
            recurAppt.setAppointmentId(appointmentId);
            recurAppt.setAppointmentTime(nextAppointmenttime);
            setEndTimeStamp(recMap, nextAppointmenttime, startTimeSlots, endTimeSlots);
            recMap.put("startTime", startTimeSlots);
            recMap.put("endTime", endTimeSlots);
            repeatAppointments.add(recurAppt);
          }
        }
      }
    }
    if (repeatAppointments.size() > 0) {
      recMap.put("repeatAppointments", repeatAppointments);
      setBeanList(recMap);
    }
  }

  /**
   * Sets the bean list.
   *
   * @param recordsMap the records map
   */
  private void setBeanList(Map<String, Object> recordsMap) {
    List<Appointment> appointments = (List) recordsMap.get("repeatAppointments");
    Appointment appt = (Appointment) recordsMap.get("appointmentDetail");
    boolean isSponsorInfoExists = (boolean) recordsMap.get("isSponsorInfoExists");
    AppointmentCategory category = (AppointmentCategory)recordsMap.get("category");
    List scheduleAppointBeanList = (List) recordsMap.get("scheduleAppointBeanList");
    List scheduleAppointItemBean = (List) recordsMap.get("scheduleAppointItemBean");
    List scheduleAppointItemBeanRecuured = (List) recordsMap.get("scheduleAppointItemBeanRecuured");
    List scheduleAppointSponsorList = (List) recordsMap.get("scheduleAppointSponsorList");
    String userName = (String) recordsMap.get("userName");
    Integer overbookLimit = (Integer) recordsMap.get("overbookLimit");

    Integer errorCount = validateRecurrenceAppointments(recordsMap);

    if (errorCount == 0) {
      for (int j = 0; j < appointments.size(); j++) {
        Appointment app = appointments.get(j);
        Integer appointmentId = app.getAppointmentId();
        Timestamp nextAppointmenttime = app.getAppointmentTime();
        app.setMrNo(appt.getMrNo());
        app.setContactId(appt.getContactId());
        app.setPatientName(appt.getPatientName());
        app.setPhoneNo(appt.getPhoneNo());
        app.setComplaint(appt.getComplaint());
        app.setScheduleId(appt.getScheduleId());
        app.setScheduleName(appt.getScheduleName());
        app.setAppointStatus(appt.getAppointStatus());
        app.setBookedBy(appt.getBookedBy());
        app.setBookedTime(appt.getBookedTime());
        app.setAppointmentDuration(appt.getAppointmentDuration());
        app.setAppointmentTime(nextAppointmenttime);
        app.setCenterId(appt.getCenterId());
        app.setChangedBy(appt.getChangedBy());
        app.setRemarks(appt.getRemarks());
        app.setPrescDocId(appt.getPrescDocId());
        app.setSchedulerVisitType(appt.getSchedulerVisitType());
        app.setVipStatus(appt.getVipStatus().equals("Y") ? "Y" : "N");
        boolean overBookAllowed = overbookLimit == null || overbookLimit > 0;
        if (overBookAllowed) {
          app.setUnique_appt_ind(appointmentRepository.getNextUniqueAppointMentInd());
        } else {
          app.setUnique_appt_ind(0);
        }

        app.setConsultationTypeId(appt.getConsultationTypeId());
        app.setSchPriorAuthId(appt.getSchPriorAuthId());
        app.setSchPriorAuthModeId(appt.getSchPriorAuthModeId());
        app.setPrim_res_id(appt.getPrim_res_id());
        app.setContactId(appt.getContactId());
        app.setPatientPrescId(appt.getPatientPrescId());
        
        //set waitList
        Timestamp apptTime = app.getAppointmentTime();
        long apptTimeLong = apptTime.getTime();
        apptTimeLong = apptTimeLong + (app.getAppointmentDuration() * 60 * 1000);
        Timestamp apptEndTime = new java.sql.Timestamp(apptTimeLong);
        Integer waitlist = appointmentRepository.getOverbookCountForResource(apptTime, apptEndTime,
            app.getPrim_res_id(), category.getPrimaryResourceType(), null);
        app.setWaitlist(waitlist);
        
        app.setVisitMode(appt.getVisitMode());
        app.setTeleconsultURL(appt.getTeleconsultURL());
        if (isSponsorInfoExists) {
          app.setPrimarySponsorId(appt.getPrimarySponsorId());
          app.setPrimarySponsorCo(appt.getPrimarySponsorCo());
          app.setPlanId(appt.getPlanId());
          app.setPlanTypeId(appt.getPlanTypeId());
          app.setMemberId(appt.getMemberId());
        }
        scheduleAppointBeanList.add(app);
        if (scheduleAppointItemBean.size() > 0) {
          Iterator itr = scheduleAppointItemBean.iterator();
          AppointmentResource apptres = null;
          java.sql.Timestamp modTime = DateUtil.getCurrentTimestamp();
          while (itr.hasNext()) {
            AppointmentResource res = (AppointmentResource) itr.next();
            apptres = new AppointmentResource(appointmentId, res.getResourceType(),
                res.getResourceId());
            apptres.setAppointment_item_id(
                new GenericRepository("scheduler_appointment_items").getNextSequence());
            apptres.setMod_time(modTime);
            apptres.setUser_name(userName);
            scheduleAppointItemBeanRecuured.add(apptres);
          }
        }
      }
    }
  }

  /**
   * Days between.
   *
   * @param c1 the c 1
   * @param c2 the c 2
   * @return the int
   */
  public static int daysBetween(Calendar c1, Calendar c2) {
    // return (int)( (d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24));
    int dayCount = 0;
    while (c1.before(c2)) {
      c1.add(Calendar.DATE, 1);
      dayCount++;
    }
    return dayCount;
  }

  /**
   * Weeks between.
   *
   * @param c1 the c 1
   * @param c2 the c 2
   * @return the int
   */
  public static int weeksBetween(Calendar c1, Calendar c2) {
    // return (int)( (d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24 * 7));
    int weekCount = 0;
    while (c1.before(c2)) {
      c1.add(Calendar.WEEK_OF_YEAR, 1);
      weekCount++;
    }
    return weekCount;
  }

  /**
   * Months between.
   *
   * @param c1 the c 1
   * @param c2 the c 2
   * @return the int
   */
  public static int monthsBetween(Calendar c1, Calendar c2) {
    int monthCount = 0;
    while (c1.before(c2)) {
      c1.add(Calendar.MONTH, 1);
      monthCount++;
    }
    return monthCount;
  }

  /**
   * Years between.
   *
   * @param c1 the c 1
   * @param c2 the c 2
   * @return the int
   */
  public static int yearsBetween(Calendar c1, Calendar c2) {
    int yearCount = 0;
    int estimatedDays = 364;
    while (c1.before(c2)) {
      if (c1.get(Calendar.YEAR) % 4 == 0) {
        estimatedDays = 363;
      }
      if ((c2.getTimeInMillis() - c1.getTimeInMillis()) / (24 * 60 * 60 * 1000) >= estimatedDays) {
        c1.add(Calendar.YEAR, 1);
        yearCount++;
      } else {
        break;
      }
    }
    return yearCount;
  }

  /**
   * Sets the end time stamp.
   *
   * @param recMap              the rec map
   * @param recurAppoinmentTime the recur appoinment time
   * @param startTimeSlots      the start time slots
   * @param endTimeSlots        the end time slots
   */
  private void setEndTimeStamp(Map recMap, Timestamp recurAppoinmentTime,
      List<Timestamp> startTimeSlots, List<Timestamp> endTimeSlots) {
    Timestamp startTimeStamp = (Timestamp) recMap.get("start_time");
    startTimeStamp = recurAppoinmentTime;
    int slotDuration = (int) recMap.get("slotDuration");
    long startTimeLong = startTimeStamp.getTime();
    startTimeLong = startTimeLong + (slotDuration * 60 * 1000);
    Timestamp endTimestamp = new java.sql.Timestamp(startTimeLong);
    startTimeSlots.add(startTimeStamp);
    endTimeSlots.add(endTimestamp);
  }

  /**
   * Validate recurrence appointments.
   *
   * @param recordsMap the records map
   * @return the int
   */
  private int validateRecurrenceAppointments(Map recordsMap) {

    List<Appointment> appointments = (List) recordsMap.get("repeatAppointments");
    List<Timestamp> startTimeSlots = (List) recordsMap.get("startTime");
    List<Timestamp> endTimeSlots = (List) recordsMap.get("endTime");
    Appointment appt = (Appointment) recordsMap.get("appointmentDetail");
    RecurrencePattern recurrenceBean = (RecurrencePattern) recordsMap.get("recurrPattern");
    java.util.Date untilDate = recurrenceBean.getUntilDate();
    AppointmentCategory category = (AppointmentCategory) recordsMap.get("category");
    String resType = category.getPrimaryResourceType();
    String resId = (String) recordsMap.get("resourceId");

    int errorCount = 0;
    for (int i = 0; i < appointments.size(); i++) {
      Appointment app = appointments.get(i);
      Timestamp startTime = startTimeSlots.get(i);
      Timestamp endTime = endTimeSlots.get(i);
      Timestamp nextAppointmenttime = app.getAppointmentTime();
      ValidationErrorMap errorsMap = new ValidationErrorMap();
      ValidationErrorMap appErrorMap = new ValidationErrorMap();
      Map<String, Object> nestedException = new HashMap<String, Object>();

      Date convertDate = new java.sql.Date(nextAppointmenttime.getTime());
      String apptDate = new SimpleDateFormat("dd-MM-yyyy").format(convertDate);

      boolean isSlotOverbooked = appointmentValidator.validateIfSlotOverbooked(category, resId,
          resType, startTime, endTime, errorsMap, null, null);
      boolean isResavailable = appointmentValidator.validateResourcesAvailability(category,
          startTime, endTime, apptDate, resType, resId, appt.getCenterId(), errorsMap, null, null,
          (String) recordsMap.get("visitMode"));

      if (!isSlotOverbooked || !isResavailable) {
        errorCount++;
        if (untilDate != null) {
          appErrorMap.addError("end_date", "exception.scheduler.appointment.invalid");
          ValidationException ex = new ValidationException(appErrorMap);
          nestedException.put("repeats", ex.getErrors());
          if (!nestedException.isEmpty()) {
            throw new NestableValidationException(nestedException);
          }
        } else {
          appErrorMap.addError("end_occurances", "exception.scheduler.appointment.invalid");
          ValidationException ex = new ValidationException(appErrorMap);
          nestedException.put("repeats", ex.getErrors());
          if (!nestedException.isEmpty()) {
            throw new NestableValidationException(nestedException);
          }
        }
      }
    }
    return errorCount;
  }

  /**
   * Checks if is last week.
   *
   * @param weekDay the week day
   * @param dayNo   the day no
   * @param cal     the cal
   * @param monthNo the month no
   * @return true, if is last week
   */
  private boolean isLastWeek(int weekDay, int dayNo, Calendar cal, int monthNo) {
    boolean isLast = true;
    for (int i = dayNo + 1; i < 31; i++) {
      if (cal.get(Calendar.MONTH) == monthNo) {
        cal.add(Calendar.DATE, 1);
        if (cal.get(Calendar.DAY_OF_WEEK) == weekDay && cal.get(Calendar.MONTH) == monthNo) {
          isLast = false;
        }
      }
    }
    return isLast;
  }

  /**
   * Gets the patient active visits.
   *
   * @param mrNo the mr no
   * @return the patient active visits
   */
  private List getPatientActiveVisits(String mrNo) {
    return registrationService.getPatientVisits(mrNo);
  }

  /**
   * Arrival meta data.
   *
   * @param mrNo  the mr no
   * @param docId the doc id
   * @return the map
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public Map<String, Object> arrivalMetaData(String mrNo, String docId) {

    Map params = new HashMap();
    List<Map> visitsList = getPatientActiveVisits(mrNo);
    List<Map> visitsWithConsultationList = new ArrayList();
    for (Map visit : visitsList) {
      Map visitMap = new HashMap();
      BasicDynaBean bean = patientRegistrationRepository.findByKey("patient_id",
          visit.get("visit_id"));
      List<Map> consultationTypes = orderService.getConsultationTypes(
          Arrays.asList(new String[] { (String) bean.get("org_id") }),
          (String) bean.get("visit_type"));
      List finalconsultationTypes = new ArrayList();
      if (docId != null) {
        List<BasicDynaBean> applicableConsultationTypes = appointmentRepository
            .getFilteredConsultationTypes(docId);
        for (Map consultationType : consultationTypes) {
          for (BasicDynaBean applicableConsultationType : applicableConsultationTypes) {
            if ((int) consultationType
                .get("consultation_type_id") == (int) applicableConsultationType
                    .get("consultation_type_id")) {
              finalconsultationTypes.add(consultationType);
              break;
            }
          }
        }
      }
      visitMap.putAll(visit);
      visitMap.put("consultation_types",
          finalconsultationTypes.size() > 0 ? finalconsultationTypes : consultationTypes);
      visitMap.put("cty_mapped_found", finalconsultationTypes.size() > 0 ? "yes" : "no");
      visitsWithConsultationList.add(visitMap);
    }
    params.put("visits", visitsWithConsultationList);
    return params;
  }

  /**
   * Update appointments status.
   *
   * @param params the params
   * @return the map
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public Map<String, Object> updateAppointmentsStatus(Map<String, Object> params) {
    ArrayList updateAppList = (ArrayList) params.get("update_app_status");
    ValidationErrorMap validationErrors = new ValidationErrorMap();
    List<Map> validationErrorsList = new ArrayList<Map>();
    Map<String, Object> nestedException = new HashMap<String, Object>();
    if (!appointmentValidator.validateUpdateAppointmentStatusParams(updateAppList,
        validationErrors)) {
      ValidationException ex = new ValidationException(validationErrors);
      validationErrorsList.add(ex.getErrors());
      nestedException.put("update_app_status", validationErrorsList);
      throw new NestableValidationException(nestedException);
    }
    for (int i = 0; i < updateAppList.size(); i++) {
      boolean statusUpdated = false;
      Map app = (Map) updateAppList.get(i);
      String appointmentId = String.valueOf(app.get("appointment_id"));
      String appointmentStatus = (String) app.get("appointment_status");
      boolean sendCommunication = app.get("send_communication") == null
          || ((Boolean) app.get("send_communication"));
      String userName = (String) sessionService.getSessionAttributes().get("userId");
      Appointment appt;
      List<AppointmentResource> apptResourceList;
      String redirectUrl = "";
      BasicDynaBean appointmentBean = null;
      if (appointmentId != null && !appointmentId.isEmpty()) {
        appointmentBean = appointmentRepository.findByKey("appointment_id",
            Integer.parseInt(appointmentId));
        Map keys = new HashMap<>();
        if (appointmentBean != null && appointmentStatus != null) {
          String category = appointmentRepository
              .getAppointmentCategory((Integer) appointmentBean.get("res_sch_id"));
          app.put("category", category);
          AppointmentCategory apptCategory = appointmentCategoryFactory
              .getInstance(category.toUpperCase(Locale.ENGLISH));
          keys.put("appointment_id", Integer.parseInt(appointmentId));
          if (sendCommunication && checkDynamicSmsAllowedtoSend(appointmentStatus)) {
            scheduleAppointmentMsg(Integer.parseInt(appointmentId),"DynamicAppointmentReminderJob", 
                (Timestamp) appointmentBean.get("appointment_time"));
          }
          switch (appointmentStatus) {
            case "Cancel":
              int uniqueApptInd = (Integer) appointmentBean.get("unique_appt_ind");
              if (uniqueApptInd == 0) {
                uniqueApptInd = appointmentRepository.getNextUniqueAppointMentInd();
              }
              String cancelType = app.get("cancel_type") != null ? (String) app.get("cancel_type")
                  : "Other";
              appointmentBean.set("cancel_type", cancelType);
              appointmentBean.set("cancel_reason", ((String) app.get("cancel_reason")).trim());
              appointmentBean.set("unique_appt_ind", uniqueApptInd);
              updateAppointment(appointmentBean, appointmentStatus, userName, keys);
              if (sendCommunication) {
                Boolean statusChangeSms = (Boolean) app.get("statusChangeSMS");
                if (statusChangeSms == null || statusChangeSms) {
                  scheduleAppointmentStatusChangeSMSJob(appointmentId, userName, appointmentStatus,
                      "appointment_cancelled");
                }
                unscheduleAppointmentMsg(Integer.parseInt(appointmentId),
                    "DynamicAppointmentReminderJob");
              }
              appt = getAppointmentObject(Integer.parseInt(appointmentId));
              apptResourceList = getapptResourceListObject(appt);
              shareAppointmentsWithPracto(userName, appt, null, apptResourceList, false);
              if (appointmentBean.get("waitlist") != null) {
                upgradeAppointmentsWaitlist(appt.getAppointmentTime(), 
                     appt.getAppointmentDuration(),(String) appointmentBean.get("prim_res_id"),
                     apptCategory.getPrimaryResourceType(), 
                     (Integer) appointmentBean.get("waitlist"));
              }
              statusUpdated = true;
              break;
            case "Confirmed":
              updateAppointment(appointmentBean, appointmentStatus, userName, keys);
              scheduleAppointmentStatusChangeSMSJob(appointmentId, userName, appointmentStatus,
                  "appointment_confirmed");
              if (sendCommunication && category.equals("DOC")) {
                scheduleAppointmentStatusChangeSMSJob(appointmentId, userName, appointmentStatus, 
                    "doc_appt_confirmed");
              }
              statusUpdated = true;
              break;
            case "Booked":
              updateAppointment(appointmentBean, appointmentStatus, userName, keys);
              scheduleAppointmentStatusChangeSMSJob(appointmentId, userName, appointmentStatus,
                  "appointment_booked");
              statusUpdated = true;
              break;
            case "Noshow":
              if (!((String) appointmentBean.get("appointment_status"))
                  .equalsIgnoreCase("arrived")) {
                int uniqueApptIndx = (Integer) appointmentBean.get("unique_appt_ind");
                if (uniqueApptIndx == 0) {
                  uniqueApptIndx = appointmentRepository.getNextUniqueAppointMentInd();
                }
                appointmentBean.set("unique_appt_ind", uniqueApptIndx);
                updateAppointment(appointmentBean, appointmentStatus, userName, keys);
                appt = getAppointmentObject(Integer.parseInt(appointmentId));
                apptResourceList = getapptResourceListObject(appt);
                shareAppointmentsWithPracto(userName, appt, null, apptResourceList, false);
              }
              statusUpdated = true;
              break;
            case "Completed":
              appointmentBean.set("completed_time", DateUtil.getCurrentTimestamp());
              updateAppointment(appointmentBean, appointmentStatus, userName, keys);
              statusUpdated = true;
              break;
            case "Arrived":
              String previousStatus = (String) appointmentBean.get("appointment_status");
              String contactId = String.valueOf(appointmentBean.get("contact_id"));
              String replaceregisterRedirectUrl = registerRedirectUrlForMr;
              if (previousStatus.equalsIgnoreCase("Booked")
                  || previousStatus.equalsIgnoreCase("Confirmed")) {
                String allowMultipleActiveVisits = (String) regPrefService
                    .getRegistrationPreferences().get("allow_multiple_active_visits");
                allowMultipleActiveVisits = (allowMultipleActiveVisits != null
                    && !allowMultipleActiveVisits.equals("")) ? allowMultipleActiveVisits : "N";
                String schedulerGenerateOrder = (String) genericPreferencesService
                    .getAllPreferences().get("scheduler_generate_order");
                schedulerGenerateOrder = schedulerGenerateOrder == null ? "N"
                    : schedulerGenerateOrder;
                List<String> modules = securityService.getActivatedModules();
                String modEclaim = (modules.contains("mod_eclaim")) ? "Y" : "N";
                String visitId;
                if (app.get("visit_id") instanceof Map) {
                  Map map = (Map) app.get("visit_id");
                  visitId = (String) map.get("newVisit");
                } else {
                  visitId = app.get("visit_id") != null ? (String) app.get("visit_id") : null;
                }
                String doctorCharge = (String) app.get("doctor_charge");
                String mrNo = (String) appointmentBean.get("mr_no");
                String replaceregisterRedirectUrlMrNo = null;
                if (mrNo != null) {
                  replaceregisterRedirectUrlMrNo = registerRedirectUrlForMr.replace("#mr_no#",
                      mrNo);
                }
                String primResId = null;
                if (apptCategory.getCategory().equalsIgnoreCase("DOC")) {
                  primResId = (String) appointmentBean.get("prim_res_id");
                }
                if (mrNo == null || mrNo.equals("")) {
                  // new patient
                  handleRightsForRegistrationScreen();
                  replaceregisterRedirectUrl = replaceregisterRedirectUrl.replace("#appointmentId#",
                      appointmentId);
                  replaceregisterRedirectUrl = replaceregisterRedirectUrl
                      .replace("/patient/#mr_no#", "/contact/" + contactId);
                  replaceregisterRedirectUrl = replaceregisterRedirectUrl.replace("#category#",
                      apptCategory.getCategory());
                  params.put("arrival_status", "redirect");
                  redirectUrl = replaceregisterRedirectUrl;
                } else {
                  if (modEclaim.equalsIgnoreCase("N")) {
                    if (allowMultipleActiveVisits.equalsIgnoreCase("N")) {
                      if (visitId != null) {
                        handleBill(visitId, doctorCharge, RequestContext.getUserName(),
                            appointmentBean);
                        setArrivedStatus(appointmentId, visitId, doctorCharge);
                        statusUpdated = true;
                        params.put("arrival_status", "completed");
                        params.put("message", MESSAGE_ORDERED);
                      } else {
                        Map arrivalData = arrivalMetaData(mrNo, primResId);
                        List<Map> visitsList = (List) arrivalData.get("visits");
                        if (visitsList != null && visitsList.size() > 0) {
                          if (schedulerGenerateOrder.equalsIgnoreCase("N")) {
                            setArrivedStatus(appointmentId, null, null);
                            statusUpdated = true;
                            params.put("arrival_status", "completed");
                          } else {
                            params.put("arrival_data", arrivalMetaData(mrNo, primResId));
                            params.put("arrival_status", "in_progress");
                          }
                        } else {
                          handleRightsForRegistrationScreen();
                          replaceregisterRedirectUrlMrNo = replaceregisterRedirectUrlMrNo
                              .replace("#appointmentId#", appointmentId);
                          replaceregisterRedirectUrlMrNo = replaceregisterRedirectUrlMrNo
                              .replace("#contactId#", contactId);
                          replaceregisterRedirectUrlMrNo = replaceregisterRedirectUrlMrNo
                              .replace("#category#", apptCategory.getCategory());
                          params.put("arrival_status", "redirect");
                          redirectUrl = replaceregisterRedirectUrlMrNo;
                        }
                      }
                    } else {
                      if (visitId != null) {
                        handleBill(visitId, doctorCharge, RequestContext.getUserName(),
                            appointmentBean);
                        setArrivedStatus(appointmentId, visitId, doctorCharge);
                        statusUpdated = true;
                        params.put("arrival_status", "completed");
                        params.put("message", MESSAGE_ORDERED);
                      } else {
                        Map arrivalData = arrivalMetaData(mrNo, primResId);
                        List<Map> visitsList = (List) arrivalData.get("visits");
                        if (visitsList != null && visitsList.size() > 0) {
                          if (schedulerGenerateOrder.equalsIgnoreCase("N")) {
                            setArrivedStatus(appointmentId, null, null);
                            statusUpdated = true;
                            params.put("arrival_status", "completed");
                          } else {
                            params.put("arrival_data", arrivalMetaData(mrNo, primResId));
                            params.put("arrival_status", "in_progress");
                            if (checkScreenRightsForRegistrationScreen()) {
                              params.put("new_visit", "Y");
                              replaceregisterRedirectUrlMrNo = replaceregisterRedirectUrlMrNo
                                  .replace("#appointmentId#", appointmentId);
                              replaceregisterRedirectUrlMrNo = replaceregisterRedirectUrlMrNo
                                  .replace("#contactId#", contactId);
                              replaceregisterRedirectUrlMrNo = replaceregisterRedirectUrlMrNo
                                  .replace("#category#", apptCategory.getCategory());
                              redirectUrl = replaceregisterRedirectUrlMrNo;
                            }
                          }
                        } else {
                          handleRightsForRegistrationScreen();
                          replaceregisterRedirectUrlMrNo = replaceregisterRedirectUrlMrNo
                              .replace("#appointmentId#", appointmentId);
                          replaceregisterRedirectUrlMrNo = replaceregisterRedirectUrlMrNo
                              .replace("#contactId#", contactId);
                          replaceregisterRedirectUrlMrNo = replaceregisterRedirectUrlMrNo
                              .replace("#category#", apptCategory.getCategory());
                          params.put("arrival_status", "redirect");
                          redirectUrl = replaceregisterRedirectUrlMrNo;
                        }
                      }
                    }
                  } else {
                    if (visitId != null) {
                      handleBill(visitId, doctorCharge, RequestContext.getUserName(),
                          appointmentBean);
                      setArrivedStatus(appointmentId, visitId, doctorCharge);
                      statusUpdated = true;
                      params.put("arrival_status", "completed");
                      params.put("message", MESSAGE_ORDERED);
                    } else {
                      Map arrivalData = arrivalMetaData(mrNo, primResId);
                      List<Map> visitsList = (List) arrivalData.get("visits");
                      List filteredVisitList = new ArrayList();
                      if (!apptCategory.getCategory().equals("DOC")) {
                        filteredVisitList.addAll(visitsList);
                      } else {
                        for (Map visit : visitsList) {
                          String visitIdM = (String) visit.get("visit_id");
                          List consultationList = doctorConsultationService
                              .listVisitConsultations(visitIdM);
                          if (consultationList != null && consultationList.size() == 0) {
                            filteredVisitList.add(visit);
                          }
                        }
                      }
                      if (filteredVisitList != null && filteredVisitList.size() > 0) {
                        if (schedulerGenerateOrder.equalsIgnoreCase("N")) {
                          setArrivedStatus(appointmentId, null, null);
                          statusUpdated = true;
                          params.put("arrival_status", "completed");
                        } else {
                          arrivalData.put("visits", filteredVisitList);
                          params.put("arrival_data", arrivalData);
                          params.put("arrival_status", "in_progress");
                          if (allowMultipleActiveVisits.equalsIgnoreCase("Y")
                              && checkScreenRightsForRegistrationScreen()) {
                            params.put("new_visit", "Y");
                            replaceregisterRedirectUrlMrNo = replaceregisterRedirectUrlMrNo
                                .replace("#appointmentId#", appointmentId);
                            replaceregisterRedirectUrlMrNo = replaceregisterRedirectUrlMrNo
                                .replace("#contactId#", contactId);
                            replaceregisterRedirectUrlMrNo = replaceregisterRedirectUrlMrNo
                                .replace("#category#", apptCategory.getCategory());
                            redirectUrl = replaceregisterRedirectUrlMrNo;
                          }
                        }
                      } else {
                        if (allowMultipleActiveVisits.equalsIgnoreCase("N")) {
                          if (filteredVisitList.size() != visitsList.size()) {
                            if (schedulerGenerateOrder.equalsIgnoreCase("N")) {
                              setArrivedStatus(appointmentId, null, null);
                              statusUpdated = true;
                              params.put("arrival_status", "completed");
                            } else {
                              List<Map> errors = new ArrayList<Map>();
                              ValidationErrorMap appErrorMap = new ValidationErrorMap();
                              appErrorMap.addError("appointment_status",
                                  "exception.scheduler.status.no.consultationfree.visits");
                              ValidationException ex = new ValidationException(appErrorMap);
                              errors.add(ex.getErrors());
                              nestedException.put("update_app_status", errors);
                              throw new NestableValidationException(nestedException);
                            }
                          } else {
                            handleRightsForRegistrationScreen();
                            replaceregisterRedirectUrlMrNo = replaceregisterRedirectUrlMrNo
                                .replace("#appointmentId#", appointmentId);
                            replaceregisterRedirectUrlMrNo = replaceregisterRedirectUrlMrNo
                                .replace("#contactId#", contactId);
                            replaceregisterRedirectUrlMrNo = replaceregisterRedirectUrlMrNo
                                .replace("#category#", apptCategory.getCategory());
                            params.put("arrival_status", "redirect");
                            redirectUrl = replaceregisterRedirectUrlMrNo;
                          }
                        } else {
                          if (filteredVisitList.size() != visitsList.size()) {
                            if (schedulerGenerateOrder.equalsIgnoreCase("N")) {
                              setArrivedStatus(appointmentId, null, null);
                              statusUpdated = true;
                              params.put("arrival_status", "completed");
                            } else {
                              handleRightsForRegistrationScreen();
                              replaceregisterRedirectUrlMrNo = replaceregisterRedirectUrlMrNo
                                  .replace("#appointmentId#", appointmentId);
                              replaceregisterRedirectUrlMrNo = replaceregisterRedirectUrlMrNo
                                  .replace("#contactId#", contactId);
                              replaceregisterRedirectUrlMrNo = replaceregisterRedirectUrlMrNo
                                  .replace("#category#", apptCategory.getCategory());
                              params.put("arrival_status", "redirect");
                              redirectUrl = replaceregisterRedirectUrlMrNo;
                            }
                          } else {
                            handleRightsForRegistrationScreen();
                            replaceregisterRedirectUrlMrNo = replaceregisterRedirectUrlMrNo
                                .replace("#appointmentId#", appointmentId);
                            replaceregisterRedirectUrlMrNo = replaceregisterRedirectUrlMrNo
                                .replace("#contactId#", contactId);
                            replaceregisterRedirectUrlMrNo = replaceregisterRedirectUrlMrNo
                                .replace("#category#", apptCategory.getCategory());
                            params.put("arrival_status", "redirect");
                            redirectUrl = replaceregisterRedirectUrlMrNo;
                          }
                        }
                      }
                    }
                  }
                }
              }
              break;

            default:
              break;
          }
          if (statusUpdated) {
            schedulePushEvent(appointmentId.toString(),
                "APPOINTMENT_" + appointmentStatus.toUpperCase());
          }
        }
      } else {
        List<Map> errors = new ArrayList<Map>();
        ValidationErrorMap appErrorMap = new ValidationErrorMap();
        appErrorMap.addError("appointment_id", "exception.scheduler.edit.noAppointment");
        ValidationException ex = new ValidationException(appErrorMap);
        errors.add(ex.getErrors());
        nestedException.put("update_app_status", errors);
        throw new NestableValidationException(nestedException);
      }
      params.put("redirect", redirectUrl);
    }
    return params;
  }

  /**
   * Handle rights for registration screen.
   */
  private void handleRightsForRegistrationScreen() {
    Map<String, Object> nestedException = new HashMap<String, Object>();
    List<Map> screenRightsErrors = new ArrayList<Map>();
    ValidationErrorMap appErrorMap = new ValidationErrorMap();
    if (!checkScreenRightsForRegistrationScreen()) {
      appErrorMap.addError("appointment_status", "exception.scheduler.register.opPatient");
      ValidationException ex = new ValidationException(appErrorMap);
      screenRightsErrors.add(ex.getErrors());
      nestedException.put("update_app_status", screenRightsErrors);
      throw new NestableValidationException(nestedException);
    }
  }

  /**
   * Gets the appt resource list object.
   *
   * @param appt the appt
   * @return the appt resource list object
   */
  public List<AppointmentResource> getapptResourceListObject(Appointment appt) {
    List<AppointmentResource> apptResList = new ArrayList<AppointmentResource>();
    List<BasicDynaBean> resList = appointmentRepository.getSchedulerAppts(appt.getAppointmentId());
    for (BasicDynaBean res : resList) {
      AppointmentResource apptres = new AppointmentResource(appt.getAppointmentId(),
          (String) res.get("resource_type"), (String) res.get("resource_id"));
      apptres.setAppointment_item_id((int) res.get("appointment_item_id"));
      apptres.setMod_time((Timestamp) res.get("mod_time"));
      apptres.setUser_name((String) res.get("user_name"));
      apptResList.add(apptres);
    }
    return apptResList;
  }

  /**
   * Gets the appointment object.
   *
   * @param appointmentId the appointment id
   * @return the appointment object
   */
  public Appointment getAppointmentObject(int appointmentId) {
    Appointment apptOb = new Appointment(appointmentId);
    BasicDynaBean appBean = appointmentRepository.findByKey("appointment_id", appointmentId);
    apptOb.setAppointmentId(appointmentId);
    apptOb.setCenterId((Integer) appBean.get("center_id"));
    apptOb.setAppointmentTime((Timestamp) appBean.get("appointment_time"));
    apptOb.setAppointmentDuration((Integer) appBean.get("duration"));
    apptOb.setMrNo((String) appBean.get("mr_no"));
    apptOb.setPatientName((String) appBean.get("patient_name"));
    apptOb.setPhoneNo((String) appBean.get("patient_contact"));
    apptOb.setAppointStatus((String) appBean.get("appointment_status"));
    apptOb.setPractoAppointmentId((String) appBean.get("practo_appointment_id"));
    apptOb.setScheduleName((String) appBean.get("res_sch_name"));
    return apptOb;
  }

  /**
   * Check screen rights for registration screen.
   *
   * @return the boolean
   */
  private Boolean checkScreenRightsForRegistrationScreen() {

    HashMap urlRightsMap = (HashMap) sessionService
        .getSessionAttributes(new String[] { "urlRightsMap" }).get("urlRightsMap");
    String opRegScreenRights = (String) (urlRightsMap.get("new_op_registration") != null
        ? urlRightsMap.get("new_op_registration")
        : "N");
    return opRegScreenRights.equals("A");

  }

  /**
   * Handle bill.
   *
   * @param visitId         the visit id
   * @param doctorCharge    the doctor charge
   * @param userName        the user name
   * @param appointmentBean the appointment bean
   */
  @SuppressWarnings({ "rawtypes", "unused", "unchecked" })
  private void handleBill(String visitId, String doctorCharge, String userName,
      BasicDynaBean appointmentBean) {
    Map ordersMap = new HashMap();
    Map tempMap = new HashMap();
    String apptTime = new SimpleDateFormat("dd-MM-yyyy HH:mm")
        .format(appointmentBean.get("appointment_time"));
    List docList = new ArrayList();
    List servList = new ArrayList();
    if ((int) appointmentBean.get("res_sch_id") == 1) {
      tempMap.put("doctors_item_id", appointmentBean.get("prim_res_id"));
      tempMap.put("doctors_multi_visit_package", false);
      tempMap.put("doctors_start_date", apptTime);
      tempMap.put("doctors_head", doctorCharge);
      tempMap.put("doctors_prescribed_doctor_id", appointmentBean.get("presc_doc_id"));
      tempMap.put("appointment_id", String.valueOf(appointmentBean.get("appointment_id")));
      tempMap.put("visit_mode", appointmentBean.get("visit_mode"));
      docList.add(new HashMap(tempMap));
    } else if ((int) appointmentBean.get("res_sch_id") == 3) {
      tempMap.put("services_item_id", appointmentBean.get("res_sch_name"));
      tempMap.put("services_multi_visit_package", false);
      tempMap.put("services_start_date", apptTime);
      tempMap.put("services_prescribed_doctor_id", appointmentBean.get("presc_doc_id"));
      tempMap.put("services_quantity", 1);
      servList.add(new HashMap(tempMap));
    }
    ordersMap.put("doctors", docList);
    ordersMap.put("services", servList);
    ordersMap.put("others", new ArrayList());
    ordersMap.put("tests", new ArrayList());
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
  }

  /**
   * Schedule appointment status change SMS job.
   *
   * @param appointmentId     the appointment id
   * @param userName          the user name
   * @param appointmentStatus the appointment status
   * @param eventId           the event id
   */
  private void scheduleAppointmentStatusChangeSMSJob(String appointmentId, String userName,
      String appointmentStatus, String eventId) {
    String[] apptArr = new String[1];
    apptArr[0] = String.valueOf(appointmentId);
    Map<String, Object> jobData = new HashMap<String, Object>();
    jobData.put("schema", RequestContext.getSchema());
    jobData.put("eventId", eventId);
    jobData.put("eventData", apptArr);
    jobData.put("userName", userName);
    jobData.put("newStatus", appointmentStatus);
    jobService
        .scheduleImmediate(buildJob("AppointmentStatusChangeSMSJob_" + eventId + "" + appointmentId,
            AppointmentStatusChangeSMSJob.class, jobData));

  }

  /**
   * Sets the arrived status.
   *
   * @param appointmentId      the appointment id
   * @param visitId            the visit id
   * @param consultationTypeId the consultation type id
   */
  public void setArrivedStatus(String appointmentId, String visitId, String consultationTypeId) {
    BasicDynaBean appointmentBean = appointmentRepository.findByKey("appointment_id",
        Integer.parseInt(appointmentId));
    if (appointmentBean != null) {
      Map keys = new HashMap();
      keys.put("appointment_id", Integer.parseInt(appointmentId));
      appointmentBean.set("arrival_time", DateUtil.getCurrentTimestamp());
      appointmentBean.set("visit_id", visitId);
      if (consultationTypeId != null) {
        appointmentBean.set("consultation_type_id", Integer.parseInt(consultationTypeId));
      }
      String userName = (String) sessionService.getSessionAttributes().get("userId");
      updateAppointment(appointmentBean, "Arrived", userName, keys);
      Appointment appt = getAppointmentObject(Integer.parseInt(appointmentId));
      List<AppointmentResource> apptResourceList = getapptResourceListObject(appt);
      shareAppointmentsWithPracto(userName, appt, null, apptResourceList, false);
    }
  }

  /**
   * Update appointment.
   *
   * @param appointmentBean   the appointment bean
   * @param appointmentStatus the appointment status
   * @param userName          the user name
   * @param keys              the keys
   */
  private void updateAppointment(BasicDynaBean appointmentBean, String appointmentStatus,
      String userName, Map keys) {
    appointmentBean.set("appointment_status", appointmentStatus);
    appointmentBean.set("changed_by", userName);
    appointmentBean.set("changed_time", DateUtil.getCurrentTimestamp());
    int updateCount = appointmentRepository.update(appointmentBean, keys);
    if (updateCount == 0) {
      logger.debug("Failed to update Appointment Status");
    }
    return;
  }

  /**
   * Unschedule appointment msg.
   *
   * @param uniqueId the unique id
   * @param jobName  the job name
   */
  public void unscheduleAppointmentMsg(Integer uniqueId, String jobName) {

    String uniString = String.valueOf(uniqueId);
    Map<String, Object> jobData = new HashMap<>();
    jobData.put("params", uniString);
    jobData.put("schema", RequestContext.getSchema());
    JobDetail jobDetail = buildJob("DynamicAppointmentReminderJob" + uniString,
        DynamicAppointmentReminderJob.class, jobData);
    if (jobService != null) {
      jobService.deleteJob(jobDetail);
    }
  }

  /**
   * Update appointments.
   *
   * @param appointmentId the appointment id
   * @return true, if successful
   */
  public boolean updateAppointments(int appointmentId) {
    return appointmentRepository.updateAppointments(appointmentId);
  }

  /**
   * Edits the appointment.
   *
   * @param apptCategory the appt category
   * @param params       the params
   * @return the map
   */
  @Transactional(rollbackFor = Exception.class)
  @SuppressWarnings({ "unchecked", "unused", "rawtypes" })
  public Map<String, Object> editAppointment(AppointmentCategory apptCategory,
      Map<String, Object> params) {

    Map<String, Object> appointmentInfo = (Map<String, Object>) params.get("appointment");
    Map<String, Object> additionalInfo = (Map<String, Object>) params.get("additional_info");
    String visitMode = (String)appointmentInfo.get("visit_mode");
    Map errMap = new HashMap();

    String secResId = String.valueOf(appointmentInfo.get("secondary_resource_id"));
    Integer appointmentId = (Integer) appointmentInfo.get("appointment_id");
    boolean sendCommunication = appointmentInfo.get("send_communication") == null
        || (Boolean) appointmentInfo.get("send_communication");
    String practoApptId = (appointmentInfo.get("practo_appointment_id") != null
        && !("").equals(appointmentInfo.get("practo_appointment_id")))
            ? (String) appointmentInfo.get("practo_appointment_id") : null;
    String userName = null;
    if (practoApptId != null && !practoApptId.equalsIgnoreCase("")) {
      userName = "InstaAdmin";
    } else {
      userName = (String) sessionService.getSessionAttributes().get("userId");
    }

    BasicDynaBean schItems = appointmentRepository.findByKey("appointment_id", appointmentId);
    if (schItems == null) {
      throw new ValidationException("exception.scheduler.edit.noAppointment");
    }
    Timestamp originalApptTime = null;
    originalApptTime = (Timestamp) schItems.get("appointment_time");
    Integer packageId = (Integer) schItems.get("package_id");
    appointmentInfo.put("appointment_pack_group_id", schItems.get("appointment_pack_group_id"));
    Map<String, Object> patientInfo = (Map<String, Object>) params.get("patient");
    if (patientInfo != null) {
      patientInfo.put("contact_id", schItems.get("contact_id"));
    }
    appointmentInfo.put("patient_presc_id", schItems.get("patient_presc_id"));
    String category = appointmentRepository
        .getAppointmentCategory((Integer) schItems.get("res_sch_id"));
    AppointmentCategory appointmentCategory = appointmentCategoryFactory
        .getInstance(category.toUpperCase(Locale.ENGLISH));
    String oldApptStatus = (String) schItems.get("appointment_status");
    if (!(oldApptStatus.equalsIgnoreCase("Confirmed")
        || oldApptStatus.equalsIgnoreCase("Booked"))) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("status", "exception.scheduler.edit.noeditallowed.status");
      ValidationException ex = new ValidationException(errorMap);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      nestedException.put("appointment", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }

    String primResId = (String) (appointmentInfo.get("primary_resource_id") != null
        ? appointmentInfo.get("primary_resource_id") : schItems.get("prim_res_id"));

    if (patientInfo != null && schItems != null && schItems.get("prim_res_id") != null
        && !schItems.get("prim_res_id").equals(primResId)) {
      if (packageId == null) {
        validate(appointmentCategory, patientInfo, appointmentInfo, additionalInfo,visitMode);
      }
      // Since the resource is changed, we cancel the existing appointment and
      // create new one
      params.put("orig_appt_time", originalApptTime);
      return appointmentCancelAndCreate(appointmentCategory, params, userName);
    }
    // data for waitList 
    String apptDate = appointmentInfo.get("date") != null ? (String) appointmentInfo.get("date")
        : null;
    String slotTime = appointmentInfo.get("slot_time") != null
        ? (String) appointmentInfo.get("slot_time") : null;
    String timestampStr = apptDate + " " + slotTime;
    Timestamp apptTime = null;
    try {
      DateUtil dateUtil = new DateUtil();
      apptTime = dateUtil.parseTheTimestamp(timestampStr);
    } catch (ParseException pe) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("date", "exception.scheduler.appointment.invalid.date");
      ValidationException ex = new ValidationException(errorMap);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      nestedException.put("appointment", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    Integer duration = (Integer) schItems.get("duration");
    long apptTimeLong = apptTime.getTime();
    apptTimeLong = apptTimeLong + (duration * 60 * 1000);
    Timestamp apptEndTime = new java.sql.Timestamp(apptTimeLong);
    Integer oldWaitlistNumber = (Integer) schItems.get("waitlist");

    if (patientInfo == null) {

      patientInfo = new HashMap();
      patientInfo.put("mr_no", schItems.get("mr_no"));
      patientInfo.put("contact_id", schItems.get("contact_id"));
      patientInfo.put("patient_name", schItems.get("patient_name"));
      patientInfo.put("patient_contact", schItems.get("patient_contact"));
      patientInfo.put("contact_id", schItems.get("contact_id"));
      appointmentInfo.put("secondary_resource_id", schItems.get("res_sch_name"));
      appointmentInfo.put("primary_resource_id", schItems.get("prim_res_id"));
      appointmentInfo.put("duration", duration);
      appointmentInfo.put("status", oldApptStatus);
      List<Map> validateAddResourcesList = new ArrayList();
      copyAdditionalResources(appointmentId, validateAddResourcesList, null, appointmentCategory);
      appointmentInfo.put("additional_resources_insert", validateAddResourcesList);

      validate(appointmentCategory, patientInfo, appointmentInfo, additionalInfo,visitMode);
      Map keys = new HashMap();
      keys.put("appointment_id", schItems.get("appointment_id"));

      schItems.set("appointment_time", apptTime);
      schItems.set("changed_time", DateUtil.getCurrentTimestamp());
      schItems.set("rescheduled", "Y");
      schItems.set("orig_appt_time", originalApptTime);
      if (visitMode.equalsIgnoreCase("I")) {
        schItems.set("teleconsult_url", null);
      }
      String resType = appointmentCategory.getPrimaryResourceType();

      Integer overbookLimit = appointmentCategory.getResourceOverbookLimit(primResId, resType);
      boolean overBookAllowed = overbookLimit == null || overbookLimit > 0;
      if (overBookAllowed || (practoApptId != null && !practoApptId.equalsIgnoreCase(""))) {
        schItems.set("unique_appt_ind", appointmentRepository.getNextUniqueAppointMentInd());
      } else {
        schItems.set("unique_appt_ind", 0);
      }

      if (!originalApptTime.equals(apptTime)) {
        Integer newWaitlist = appointmentRepository.getOverbookCountForResource(apptTime,
            apptEndTime, primResId, resType, null);
        schItems.set("waitlist", newWaitlist);
      }
      appointmentRepository.update(schItems, keys);
      if (!originalApptTime.equals(apptTime) && oldWaitlistNumber != null) {
        upgradeAppointmentsWaitlist(originalApptTime, duration, primResId, resType,
            oldWaitlistNumber);
      }
      // share with practo
      Appointment appt = getAppointmentObject(appointmentId);
      if (bookIntegrationService.isPractoAdvantageEnabled()) {
        List<AppointmentResource> apptResourceList = getapptResourceListObject(appt);
        shareAppointmentsWithPracto(userName, appt, null, apptResourceList, false);
      }
      schedulePushEvent( appointmentId.toString(), Events.APPOINTMENT_RESCHEDULED);

      if (schItems != null && schItems.get("practo_appointment_id") == null
          && schItems.get("appointment_status") != null
          && (((String) schItems.get("appointment_status")).equalsIgnoreCase("confirmed")
              || ((String) schItems.get("appointment_status")).equalsIgnoreCase("booked"))) {

        sendAppointmentSMSforTimeChange(appointmentId);

        unscheduleAppointmentMsg(appointmentId, "DynamicAppointmentReminderJob");
        if (checkDynamicSmsAllowedtoSend(schItems.get("appointment_status").toString())) {
          scheduleAppointmentMsg(appointmentId, "DynamicAppointmentReminderJob", apptTime);
        }
      }

    } else {

      // Create NewAppointment Pojo
      Appointment appt = new Appointment(appointmentId);
      boolean status = false;
      String newStatus = (String) appointmentInfo.get("status");

      // Check status change
      if (!oldApptStatus.equalsIgnoreCase(newStatus)) {
        Map requestMap = new HashMap();
        requestMap.put("appointment_id", appointmentId.toString());
        requestMap.put("appointment_status", newStatus);
        requestMap.put("category", appointmentCategory.getCategory());
        List<Map> cancelParamsList = new ArrayList();
        cancelParamsList.add(requestMap);
        Map updateStatusMap = new HashMap();
        updateStatusMap.put("update_app_status", cancelParamsList);
        updateAppointmentsStatus(updateStatusMap);
      }
      String resType = appointmentCategory.getPrimaryResourceType();

      Integer overbookLimit = appointmentCategory.getResourceOverbookLimit(primResId, resType);
      boolean overBookAllowed = overbookLimit == null || overbookLimit > 0;
      if (overBookAllowed || (practoApptId != null && !practoApptId.equalsIgnoreCase(""))) {
        appt.setUnique_appt_ind(appointmentRepository.getNextUniqueAppointMentInd());
      } else {
        appt.setUnique_appt_ind(0);
      }
      setAppointmentPojo(appointmentCategory, params, appt);
      appt.setBookedTime((Timestamp) schItems.get("booked_time"));
      appt.setChangedTime(DateUtil.getCurrentTimestamp());

      if (appt.getAppointStatus() != null && appt.getAppointStatus().equals("Arrived")
          && schItems.get("appointment_status").equals("Arrived")) {
        errMap.put("error", "patient is already arrived");
        return errMap;
      }

      // Check if validation required
      Boolean validationRequired = checkIfValidationRequired(params, appt, schItems);
      boolean appRescheduled = false;
      boolean appDurationChanged = false;
      Timestamp oldAppointmentTime = (Timestamp) schItems.get("appointment_time");
      Integer oldDuration = (Integer) schItems.get("duration");
      if (validationRequired && packageId == null) {
        validate(appointmentCategory, patientInfo, appointmentInfo, additionalInfo,visitMode);
      }

      // set waitList and appReschedule
      if (appt.getAppointmentTime().getTime() != oldAppointmentTime.getTime()) {
        Integer newWaitlist = appointmentRepository.getOverbookCountForResource(apptTime,
            apptEndTime, primResId, resType, null);
        appt.setWaitlist(newWaitlist);
        appRescheduled = true;
      } else {
        appt.setWaitlist(oldWaitlistNumber);
      }
      if (!appt.getAppointmentDuration().equals(oldDuration)) {
        appDurationChanged = true;
      }

      if (appRescheduled || schItems.get("rescheduled").equals("Y")) {
        appt.setRescheduled("Y");
        appt.setOrigApptTime(originalApptTime);
      } else {
        appt.setRescheduled("N");
      }
      if (schItems.get("booked_by") != null) {
        appt.setBookedBy((String) schItems.get("booked_by"));
      }

      // Handling AdditionalInfo
      String oldMemberId = (String) schItems.get("member_id");
      String newMemberId = appt.getMemberId();
      boolean isMemberIdChanged = false;

      if (newMemberId != null && oldMemberId != null && !newMemberId.equals(oldMemberId)) {
        isMemberIdChanged = true;
      }

      // Handling additional resources list

      List<Map> deleteList = appointmentInfo.get("additional_resources_delete") != null
          ? (List<Map>) appointmentInfo.get("additional_resources_delete") : null;

      boolean isAdditionalResourceChanged = true;

      // validate
      boolean successfulValidate = true;
      List<Map> addionalInsertList = (List<Map>) appointmentInfo.get("additional_resources_insert");
      List<Map> tempList = new ArrayList<>();

      if (addionalInsertList != null) {
        for (Map map : addionalInsertList) {
          tempList.add(map);
        }
      }
      copyAdditionalResources(appointmentId, addionalInsertList, deleteList, appointmentCategory);
      List<Map> additionalResources = addionalInsertList;
      if (validationRequired && packageId == null) {
        successfulValidate = validate(appointmentCategory, patientInfo, appointmentInfo,
            additionalInfo,visitMode);
      }
      appointmentInfo.put("additional_resources_insert", tempList);
      List<Map> insertList = appointmentInfo.get("additional_resources_insert") != null
          ? (List<Map>) appointmentInfo.get("additional_resources_insert") : null;
      insertList = (List<Map>) appointmentInfo.get("additional_resources_insert");
      if (!successfulValidate) {
        errMap.put("error", "Validation failed....");
        return errMap;
      }

      do {

        if (appt.getAppointStatus() != null) {
          status = updateSchedulerAppointment(appt);
          if (!status) {
            break;
          }

          status = updateAdditionalResources(insertList, deleteList, appointmentId, userName);

          if (!status) {
            break;
          }

        }

      } while (false);
      patientCommunicationService.convetAndSavePatientCommPreference(
          (String) patientInfo.get("mr_no"), (String) patientInfo.get("send_sms"),
          (String) patientInfo.get("send_email"), (String) patientInfo.get("preferred_language"));
      boolean isConfirmedOrBooked = appt != null && appt.getAppointStatus() != null
          && appt.getAppointStatus().equalsIgnoreCase("confirmed")
          || appt.getAppointStatus().equalsIgnoreCase("booked");
      schedulePushEvent(appointmentId.toString(),
          appRescheduled ? Events.APPOINTMENT_RESCHEDULED : Events.APPOINTMENT_UPDATED);
      if (isConfirmedOrBooked && (appDurationChanged || appRescheduled)
          && bookIntegrationService.isPractoAdvantageEnabled()) {
        List<AppointmentResource> appointmentResourceList = new ArrayList<AppointmentResource>();
        AppointmentResource res = new AppointmentResource(appt.getAppointmentId(), resType,
            primResId);
        appointmentResourceList.add(res);
        if (additionalResources != null) {
          for (Map map : additionalResources) {
            AppointmentResource appRes = new AppointmentResource(appt.getAppointmentId(),
                (String) map.get("resource_type"), (String) map.get("resource_id"));
            appointmentResourceList.add(appRes);
          }
        }
        shareAppointmentsWithPracto(appt.getBookedBy(), appt, null, appointmentResourceList, false);
      }

      if (appt != null
          && (appt.getPractoAppointmentId() == null || appt.getPractoAppointmentId().equals(""))
          && isConfirmedOrBooked && sendCommunication) {
        if (appRescheduled && params.get("appointment_plan_id") == null) {
          sendAppointmentSMSforTimeChange(appointmentId);
        }

        if (checkDynamicSmsAllowedtoSend(schItems.get("appointment_status").toString())) {
          unscheduleAppointmentMsg(appointmentId, "DynamicAppointmentReminderJob");
        }

        if (checkDynamicSmsAllowedtoSend(appt.getAppointStatus())) {
          scheduleAppointmentMsg(appointmentId, "DynamicAppointmentReminderJob",
              appt.getAppointmentTime());
        }

      }
      if (appRescheduled && oldWaitlistNumber != null) {
        upgradeAppointmentsWaitlist(oldAppointmentTime, oldDuration, primResId, resType,
            oldWaitlistNumber);
      }
    }

    return params;

  }

  /**
   * Appointment cancel and create.
   *
   * @param apptCategory the appt category
   * @param params       the params
   * @param userName     the user name
   * @return the map
   */
  @SuppressWarnings("unchecked")
  @Transactional(rollbackFor = Exception.class)
  public Map<String, Object> appointmentCancelAndCreate(AppointmentCategory apptCategory,
      Map<String, Object> params, String userName) {
    Map<String, Object> appointmentInfo = (Map<String, Object>) params.get("appointment");

    Integer appointmentId = (Integer) appointmentInfo.get("appointment_id");
    Map cancelParams = new HashMap();
    cancelParams.put("appointment_id", appointmentId.toString());
    cancelParams.put("appointment_status", "Cancel");
    cancelParams.put("cancel_reason", "Cancelling as primary resource rescheduled");
    cancelParams.put("cancel_type", "Other");
    cancelParams.put("category", apptCategory.getCategory());
    cancelParams.put("statusChangeSMS", false);
    appointmentInfo.put("orig_appt_time", params.get("orig_appt_time"));
    List<Map> cancelParamsList = new ArrayList();
    cancelParamsList.add(cancelParams);
    Map updateStatusMap = new HashMap();
    updateStatusMap.put("update_app_status", cancelParamsList);
    try {
      updateAppointmentsStatus(updateStatusMap);
    } catch (Exception exp) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("appointment_status", "exception.scheduler.status.updation.failed");
      ValidationException ex = new ValidationException(errorMap);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      nestedException.put("repeats", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    // copying additional resources from old appointment into new

    copyAdditionalResources(appointmentId,
        (List<Map>) appointmentInfo.get("additional_resources_insert"),
        (List<Map>) appointmentInfo.get("additional_resources_delete"), apptCategory);
    // booking new appointment
    List appCategoryList = new ArrayList<AppointmentCategory>();
    appCategoryList.add(apptCategory);
    params.put("appointments", new ArrayList<Map>(Arrays.asList(appointmentInfo)));
    Map<String, Object> newAppointments = createNewAppointments(appCategoryList, params);

    return newAppointments;
  }

  /**
   * Check if validation required.
   *
   * @param params the params
   * @param appt the appt
   * @param apptBean the appt bean
   * @return the boolean
   */
  public Boolean checkIfValidationRequired(Map params, Appointment appt, BasicDynaBean apptBean) {

    Map apptMap = (Map) params.get("appointment");
    List insertList = (List) apptMap.get("additional_resources_insert");
    if (!appt.getPatientName().equals(apptBean.get("patient_name"))
        || !appt.getSchedulerVisitType().equals(apptBean.get("scheduler_visit_type"))
        || !appt.getAppointmentTime().equals(apptBean.get("appointment_time"))
        || !appt.getAppointmentDuration().equals(apptBean.get("duration"))
        || !appt.getAppointStatus().equals(apptBean.get("appointment_status"))
        || !appt.getScheduleName().equals(apptBean.get("res_sch_name"))
        || !appt.getPrim_res_id().equals(apptBean.get("prim_res_id"))
        || !appt.getVipStatus().equals(apptBean.get("vip_status"))
        || !appt.getVisitMode().equals(apptBean.get("visit_mode"))
        || (insertList != null && insertList.size() > 0)) {
      return true;
    }
    return false;
  }

  /**
   * Sets the appointment pojo.
   *
   * @param apptCategory the appt category
   * @param params the params
   * @param appt the appt
   */
  @SuppressWarnings("unchecked")
  private void setAppointmentPojo(AppointmentCategory apptCategory, Map<String, Object> params,
      Appointment appt) {

    Map<String, Object> patientInfo = (Map<String, Object>) params.get("patient");
    Map<String, Object> appointmentInfo = (Map<String, Object>) params.get("appointment");

    String updatedPhoneNo = null;
    String patientContact = patientInfo.get("patient_contact") != null
        && !patientInfo.get("patient_contact").equals("")
        ? (String) patientInfo.get("patient_contact") : null;
    String mrNum = patientInfo.get("mr_no") != null ? (String) patientInfo.get("mr_no") : null;
    if (patientContact == null && mrNum != null) {
      BasicDynaBean patientBean = mrNum != null
          ? patientDetailsRepository.findByKey("mr_no", patientInfo.get("mr_no"))
          : null;
      patientContact = patientBean.get("patient_phone") != null
          ? (String) patientBean.get("patient_phone") : null;
    }
    String practoApptId = (appointmentInfo.get("practo_appointment_id") != null
        && !("").equals(appointmentInfo.get("practo_appointment_id")))
            ? (String) appointmentInfo.get("practo_appointment_id") : null;
    if (practoApptId == null || (practoApptId != null && practoApptId.equalsIgnoreCase(""))) {

      DynaBeanBuilder builder = new DynaBeanBuilder();
      builder.add("patient_contact");
      BasicDynaBean bean = builder.build();
      bean.set("patient_contact", patientContact);
      appointmentValidator.setRuleSetMap(bean);
      appointmentValidator.setParentKey("patient");
      appointmentValidator.validate(bean);
      updatedPhoneNo = (String) bean.get("patient_contact");
    }
    String secResId = String.valueOf(appointmentInfo.get("secondary_resource_id"));
    String primResId = (String) appointmentInfo.get("primary_resource_id");
    int centerId = appointmentInfo.get("center_id") != null
        ? (Integer) appointmentInfo.get("center_id") : 0;
    if (practoApptId == null || (practoApptId != null && practoApptId.equalsIgnoreCase(""))) {
      if (!appointmentValidator.validateIfPrimResExists(apptCategory, primResId, centerId)
          || !appointmentValidator.validateIfSecResExists(apptCategory, secResId, centerId)) {

        ValidationErrorMap errorMap = new ValidationErrorMap();
        errorMap.addError("resource_id", "exception.scheduler.invalid.resource");
        ValidationException ex = new ValidationException(errorMap);
        Map<String, Object> nestedException = new HashMap<String, Object>();
        nestedException.put("appointment", ex.getErrors());
        throw new NestableValidationException(nestedException);
      }
    }

    int scheduleIdForAppointment = 0;
    // Getting schedule details always for '*' category just to denote the same
    // category record for
    // the scheduler type.
    String resType = apptCategory.getPrimaryResourceType();
    if (resType.endsWith("DOC")) {
      resType = "DOC";
    } else {
      // TODO this will go in apptCategory as it wont work beyond
      if (resType.equals("EQID")) {
        resType = "TST";
      } else {
        resType = "SER";
      }
    }
    BasicDynaBean scheduleBeanForRes = resAvailabilityService.getResourceDetails(resType,
        primResId);
    BasicDynaBean scheduleBean = resAvailabilityService.getResourceDetails(resType, "*");
    if (scheduleBean != null) {
      scheduleIdForAppointment = (Integer) scheduleBean.get("res_sch_id");
    }
    Integer duration = 0;
    if (appointmentInfo.get("duration") != null) {
      duration = (Integer) appointmentInfo.get("duration");
    } else {
      if (scheduleBeanForRes != null) {
        duration = ((Integer) scheduleBeanForRes.get("default_duration"));
      } else {
        duration = ((Integer) scheduleBean.get("default_duration"));
      }
    }

    if (duration <= 0) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("duration", "exception.scheduler.appointment.invalid.duration");
      ValidationException ex = new ValidationException(errorMap);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      nestedException.put("appointment", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    String apptDate = appointmentInfo.get("date") != null ? (String) appointmentInfo.get("date")
        : null;
    String slotTime = appointmentInfo.get("slot_time") != null
        ? (String) appointmentInfo.get("slot_time") : null;
    String timestampStr = apptDate + " " + slotTime;
    Timestamp apptTime = null;
    try {
      DateUtil dateUtil = new DateUtil();
      apptTime = dateUtil.parseTheTimestamp(timestampStr);
    } catch (ParseException pe) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("date", "exception.scheduler.appointment.invalid.date");
      ValidationException ex = new ValidationException(errorMap);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      nestedException.put("appointment", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    if (updatedPhoneNo != null) {
      patientContact = updatedPhoneNo;
    }

    String userName = null;
    if (practoApptId != null && !practoApptId.equalsIgnoreCase("")) {
      userName = "InstaAdmin";
    } else {
      userName = (String) sessionService.getSessionAttributes().get("userId");
    }
    Timestamp bookedTime = DateUtil.getCurrentTimestamp();
    // parse boolean check needed for 'Y' or 'N'
    Integer apptSourceId = appointmentInfo.get("app_source_id") != null
        ? (Integer) appointmentInfo.get("app_source_id") : 0;
    appt.setApp_source_id(apptSourceId);
    appt.setAppointmentDuration(duration);
    appt.setAppointmentTime(apptTime);
    String appStatus = appointmentInfo.get("status") != null
        ? (String) appointmentInfo.get("status") : null;
    appt.setAppointStatus(appStatus);
    appt.setCancelReason("");
    appt.setCenterId(centerId);
    appt.setChangedBy(userName);
    String complaint = appointmentInfo.get("complaint") != null
        ? (String) appointmentInfo.get("complaint") : null;
    appt.setComplaint(complaint);
    String mrNo = patientInfo.get("mr_no") != null ? (String) patientInfo.get("mr_no") : null;
    appt.setMrNo(mrNo);
    Map salutationMap = new HashMap<>();
    String salutation = "";
    String patientName = "";
    String lastName = "";
    String fullName = "";
    if (patientInfo.get("salutation_name") != null) {
      salutationMap.put("salutation_id", patientInfo.get("salutation_name"));
      BasicDynaBean salutationBean = salutationService.getSalutationId(salutationMap);
      if (salutationBean != null) {
        salutation = (String) salutationBean.get("salutation");
        fullName = fullName + salutation;
      }
    }
    if (patientInfo.get("patient_name") != null) {
      patientName = (String) patientInfo.get("patient_name");
      fullName = fullName + patientName;
    }
    if (patientInfo.get("last_name") != null) {
      lastName = (String) patientInfo.get("last_name");
      if (!lastName.equals("")) {
        fullName = fullName + " " + lastName;
      }
    }
    String vipStatus = patientInfo.get("vip_status") != null
        && !((String) patientInfo.get("vip_status")).equals("")
            ? (String) patientInfo.get("vip_status") : "N";
    appt.setPatientName(fullName);
    appt.setPhoneNo(patientContact);
    appt.setPrim_res_id(primResId);
    appt.setScheduleName(secResId);
    appt.setVipStatus(vipStatus);
    appt.setRescheduled("N");
    Integer parentPackObId = (appointmentInfo.get("parent_pack_ob_id") != null
        && !appointmentInfo.get("parent_pack_ob_id").equals(""))
            ? Integer.parseInt((String) appointmentInfo.get("parent_pack_ob_id")) : null;
    appt.setParentPackObId(parentPackObId);
    Integer packageId = (appointmentInfo.get("package_id") != null
        && !appointmentInfo.get("package_id").equals(""))
            ? ((Integer) appointmentInfo.get("package_id")) : null;
    appt.setPackageId(packageId);
    Integer appointmentPackGroupId = (appointmentInfo.get("appointment_pack_group_id") != null
        && !appointmentInfo.get("appointment_pack_group_id").equals(""))
            ? ((Integer) appointmentInfo.get("appointment_pack_group_id")) : null;
    appt.setAppointmentPackGroupId(appointmentPackGroupId);
    List<Appointment> appointmentsList = new ArrayList<Appointment>();
    appointmentsList.add(appt);
    apptCategory.setAppointmentData(appointmentsList, params);

    // Get the country code for the center and populate in Appointment object
    String defaultCode = null;
    if (patientContact != null && !patientContact.isEmpty()) {
      try {
        defaultCode = centerService.getCountryCode(centerId);
      } catch (NullPointerException exp) {
        ValidationErrorMap errorMap = new ValidationErrorMap();
        errorMap.addError("center_id", "exception.scheduler.override.invalid.center_id");
        ValidationException ex = new ValidationException(errorMap);
        Map<String, Object> nestedException = new HashMap<String, Object>();
        nestedException.put("appointment", ex.getErrors());
        throw new NestableValidationException(nestedException);
      }
      if (defaultCode == null) {
        defaultCode = centerService.getCountryCode(0);
      }
      List<String> parts = PhoneNumberUtil.getCountryCodeAndNationalPart(patientContact, null);
      if (parts != null && !parts.isEmpty() && !parts.get(0).isEmpty()) {
        appt.setPhoneCountryCode("+" + parts.get(0));
      } else if (defaultCode != null) {
        appt.setPhoneCountryCode("+" + defaultCode);
        if (!patientContact.startsWith("+")) {
          appt.setPhoneNo("+" + defaultCode + patientContact);
        }
      }
      if (appt.getPhoneNo().length() > 16) {
        ValidationErrorMap errorMap = new ValidationErrorMap();
        errorMap.addError("patient_contact", "exception.scheduler.patient.invalid.phoneno.long");
        ValidationException ex = new ValidationException(errorMap);
        Map<String, Object> nestedException = new HashMap<String, Object>();
        nestedException.put("patient", ex.getErrors());
        throw new NestableValidationException(nestedException);
      }

      Pattern pattern = Pattern.compile("^[0-9\\+]*$");
      Matcher matcher = pattern.matcher(appt.getPhoneNo());
      if (!matcher.matches()) {
        ValidationErrorMap errorMap = new ValidationErrorMap();
        errorMap.addError("patient_contact", "exception.scheduler.patient.invalid.phoneno.long");
        ValidationException ex = new ValidationException(errorMap);
        Map<String, Object> nestedException = new HashMap<String, Object>();
        nestedException.put("patient", ex.getErrors());
        throw new NestableValidationException(nestedException);
      }
    }
    appt.setPractoAppointmentId(practoApptId);

    String prescDocId = appointmentInfo.get("presc_doc_id") != null
        ? (String) appointmentInfo.get("presc_doc_id") : null;
    appt.setPrescDocId(prescDocId);

    String condDocId = appointmentInfo.get("cond_doc_id") != null
        ? (String) appointmentInfo.get("cond_doc_id") : null;
    appt.setCondDocId(condDocId);

    String remarks = appointmentInfo.get("remarks") != null
        ? (String) appointmentInfo.get("remarks") : null;
    appt.setRemarks(remarks);
    appt.setScheduleId(scheduleIdForAppointment);
    String visitType = patientInfo.get("scheduler_visit_type") != null
        ? (String) patientInfo.get("scheduler_visit_type") : "M";
    appt.setSchedulerVisitType(visitType);
    appt.setVisitId(null);
    String visitMode = (String) appointmentInfo.get("visit_mode");
    if (StringUtils.isEmpty(visitMode) || !visitMode.toUpperCase().equals("O")) {
      appt.setVisitMode("I");
    } else {
      appt.setVisitMode(visitMode);
    }
    String teleURL = null;
    if (visitMode != null && !visitMode.equalsIgnoreCase("I")) {
      teleURL = (String) appointmentInfo.get("teleconsult_url") != null
             ? (String) appointmentInfo.get("teleconsult_url") : null;
    }
    appt.setTeleconsultURL(teleURL);
    Integer contactId = (Integer) patientInfo.get("contact_id");
    Map contactInfo = new HashMap();
    contactInfo.put("salutation_name", patientInfo.get("salutation_name"));
    contactInfo.put("patient_name", patientInfo.get("patient_name"));
    contactInfo.put("last_name", patientInfo.get("last_name"));
    contactInfo.put("patient_contact", appt.getPhoneNo());
    contactInfo.put("center_id", centerId);
    contactInfo.put("patient_contact", patientInfo.get("patient_contact"));
    contactInfo.put("preferred_language",
        (String) genericPreferencesService.getAllPreferences().get("contact_pref_lang_code"));
    contactInfo.put("send_sms", "N");
    contactInfo.put("send_email", "N");

    contactInfo.put("patient_dob", patientInfo.get("patient_dob"));
    if (patientInfo.get("patient_age") == null && patientInfo.get("patient_dob") != null) {
      try {
        Map map = DateUtil.getAgeForDate(patientInfo.get("patient_dob").toString(), "dd-MM-yyyy");
        String age = (map.get("age") != null) ? map.get("age").toString() : null;
        String ageIn = (map.get("ageIn") != null) ? map.get("ageIn").toString() : null;
        contactInfo.put("patient_age", age);
        contactInfo.put("patient_age_units", ageIn);
      } catch (ParseException exp) {
        ValidationErrorMap errorMap = new ValidationErrorMap();
        errorMap.addError("patientDob", "exception.scheduler.patientDob.invalid.date");
        ValidationException ex = new ValidationException(errorMap);
        Map<String, Object> nestedException = new HashMap<String, Object>();
        nestedException.put("patient", ex.getErrors());
        throw new NestableValidationException(nestedException);
      }
    } else {
      contactInfo.put("patient_age", patientInfo.get("patient_age"));
      contactInfo.put("patient_age_units", patientInfo.get("patient_age_units"));
    }
    contactInfo.put("patient_gender", patientInfo.get("patient_gender"));
    contactInfo.put("practo_appointment_id", practoApptId);
    contactInfo.put("patient_email_id", patientInfo.get("patient_email_id"));
    contactInfo.put("vip_status", vipStatus);
    if (mrNo == null || mrNo.equals("")) {
      if (contactId == null) {
        contactId = Integer
            .parseInt((String) (patientContactDetailsService.insertContactDetails(contactInfo))
                .get("contact_id"));
      }
    }
    appt.setContactId(contactId);
    // Handling AdditionalInfo
    Map<String, Object> additionalInfo = (Map<String, Object>) params.get("additional_info");
    String memberId = null;
    if (additionalInfo != null) {
      String primarySponsorId = additionalInfo.get("primary_sponsor_id") != null
          ? (String) additionalInfo.get("primary_sponsor_id") : null;
      String primaryInsuranceCo = additionalInfo.get("primary_insurance_co") != null
          ? (String) additionalInfo.get("primary_insurance_co") : null;
      Integer planId = additionalInfo.get("plan_id") != null
          ? (Integer) additionalInfo.get("plan_id") : null;
      Integer planTypeId = additionalInfo.get("plan_type_id") != null
          ? (Integer) additionalInfo.get("plan_type_id") : null;
      memberId = additionalInfo.get("member_id") != null ? (String) additionalInfo.get("member_id")
          : null;
      if ((primarySponsorId != null && !primarySponsorId.equals(""))
          || (primaryInsuranceCo != null && !primaryInsuranceCo.equals(""))
          || (planId != null && planId > 0) || (planTypeId != null && planTypeId > 0)
          || (memberId != null && !memberId.equals(""))) {

        if (ifInactiveSponsor(primarySponsorId)) {
          ValidationErrorMap errorMap = new ValidationErrorMap();
          errorMap.addError("primary_sponsor_name", "exception.scheduler.error.invalid.sponsor");
          ValidationException ex = new ValidationException(errorMap);
          Map<String, Object> nestedException = new HashMap<String, Object>();
          nestedException.put("additional_info", ex.getErrors());
          throw new NestableValidationException(nestedException);
        }
        appt.setPrimarySponsorId(primarySponsorId);
        appt.setPrimarySponsorCo(primaryInsuranceCo);
        appt.setPlanId(planId);
        appt.setPlanTypeId(planTypeId);
        appt.setMemberId(memberId);
      }
    }
    // adding pending prescriptionId
    if (appointmentInfo.get("patient_presc_id") != null) {
      appt.setPatientPrescId(Long.parseLong(appointmentInfo.get("patient_presc_id").toString()));
    }
  }

  /**
   * If inactive sponsor.
   *
   * @param primarySponsorId the primary sponsor id
   * @return true, if successful
   */
  private boolean ifInactiveSponsor(String primarySponsorId) {

    BasicDynaBean sponsorBean = appointmentRepository.getSponsorBean(primarySponsorId);
    if (sponsorBean == null || ((String) sponsorBean.get("status")).equalsIgnoreCase("I")) {
      return true;
    }
    return false;
  }

  /**
   * Insert scheduler appointment.
   *
   * @param appointmentsList the appointments list
   * @return true, if successful
   */
  private boolean insertSchedulerAppointment(List<Appointment> appointmentsList) {

    List<BasicDynaBean> dynaList = new ArrayList<>();
    boolean result = true;
    for (Appointment appointment : appointmentsList) {
      dynaList.add(convertAppointmentPojoToAppoinmentBean(appointment));
    }

    int[] successes = appointmentRepository.batchInsert(dynaList);
    for (int success : successes) {
      if (success < 0) {
        result = false;
        break;
      }
    }

    return result;
  }

  /**
   * Update scheduler appointment.
   *
   * @param appointment the appointment
   * @return true, if successful
   */
  private boolean updateSchedulerAppointment(Appointment appointment) {

    BasicDynaBean bean = convertAppointmentPojoToAppoinmentBean(appointment);
    Map<String, Object> keys = new HashMap();
    keys.put("appointment_id", appointment.getAppointmentId());
    return appointmentRepository.update(bean, keys) == 1;

  }

  /**
   * Convert appointment pojo to appoinment bean.
   *
   * @param appointment the appointment
   * @return the basic dyna bean
   */
  private BasicDynaBean convertAppointmentPojoToAppoinmentBean(Appointment appointment) {

    BasicDynaBean bean = appointmentRepository.getBean();
    bean.set("appointment_id", appointment.getAppointmentId());
    bean.set("mr_no", appointment.getMrNo());
    bean.set("patient_name", appointment.getPatientName());
    bean.set("patient_contact", appointment.getPhoneNo());
    bean.set("complaint", appointment.getComplaint());
    bean.set("res_sch_id", appointment.getScheduleId());
    bean.set("res_sch_name", appointment.getScheduleName());
    bean.set("appointment_time", appointment.getAppointmentTime());
    bean.set("duration", appointment.getAppointmentDuration());
    bean.set("appointment_status", appointment.getAppointStatus());
    bean.set("booked_by", appointment.getBookedBy());
    bean.set("booked_time", appointment.getBookedTime());
    bean.set("cancel_reason", appointment.getCancelReason());
    bean.set("visit_id", appointment.getVisitId());
    bean.set("consultation_type_id", appointment.getConsultationTypeId());
    bean.set("remarks", appointment.getRemarks());
    bean.set("changed_time", appointment.getChangedTime());
    bean.set("changed_by", appointment.getChangedBy());
    bean.set("scheduler_visit_type", appointment.getSchedulerVisitType());
    bean.set("scheduler_prior_auth_no", appointment.getSchPriorAuthId());
    bean.set("scheduler_prior_auth_mode_id", appointment.getSchPriorAuthModeId());
    bean.set("center_id", appointment.getCenterId());
    bean.set("presc_doc_id", appointment.getPrescDocId());
    bean.set("cond_doc_id", appointment.getCondDocId());
    bean.set("salutation_name", appointment.getSalutationName());
    bean.set("unique_appt_ind", appointment.getUnique_appt_ind());
    bean.set("prim_res_id", appointment.getPrim_res_id());
    bean.set("patient_contact_country_code", appointment.getPhoneCountryCode());
    bean.set("practo_appointment_id", appointment.getPractoAppointmentId());
    bean.set("app_source_id", appointment.getApp_source_id());
    bean.set("primary_sponsor_id", appointment.getPrimarySponsorId());
    bean.set("primary_sponsor_co", appointment.getPrimarySponsorCo());
    bean.set("plan_id", appointment.getPlanId());
    bean.set("plan_type_id", appointment.getPlanTypeId());
    bean.set("member_id", appointment.getMemberId());
    bean.set("patient_dob", appointment.getPatientDob());
    bean.set("patient_age", appointment.getPatientAge());
    bean.set("patient_age_units", appointment.getPatientAgeUnits());
    bean.set("patient_gender", appointment.getPatientGender());
    bean.set("patient_category", appointment.getPatientCategory());
    bean.set("patient_address", appointment.getPatientAddress());
    bean.set("patient_area", appointment.getPatientArea());
    bean.set("patient_state", appointment.getPatientState());
    bean.set("patient_city", appointment.getPatientCity());
    bean.set("patient_country", appointment.getPatientCountry());
    bean.set("patient_nationality", appointment.getPatientNationality());
    bean.set("patient_email_id", appointment.getPatientEmailId());
    bean.set("patient_citizen_id", appointment.getPatientCitizenId());
    bean.set("vip_status", appointment.getVipStatus());
    bean.set("rescheduled",
        appointment.getRescheduled() != null ? appointment.getRescheduled() : "N");
    bean.set("contact_id", appointment.getContactId());
    bean.set("parent_pack_ob_id", appointment.getParentPackObId());
    bean.set("package_id", appointment.getPackageId());
    bean.set("appointment_pack_group_id", appointment.getAppointmentPackGroupId());
    bean.set("patient_presc_id", appointment.getPatientPrescId());
    bean.set("orig_appt_time", appointment.getOrigApptTime());
    bean.set("waitlist", appointment.getWaitlist());
    bean.set("visit_mode", appointment.getVisitMode());
    bean.set("teleconsult_url", appointment.getTeleconsultURL());
    return bean;
  }

  /**
   * Update additional resources.
   *
   * @param insertList the insert list
   * @param deleteList the delete list
   * @param apptId     the appt id
   * @param userName   the user name
   * @return true, if successful
   */
  private boolean updateAdditionalResources(List<Map> insertList, List<Map> deleteList,
      Integer apptId, String userName) {

    boolean insertResult = true;
    boolean deleteResult = true;

    if (insertList != null && insertList.size() > 0) {
      insertResult = appointmentRepository.addSchedulerAppointmentItems(insertList, userName,
          apptId);
    }

    if (deleteList != null && deleteList.size() > 0) {
      deleteResult = appointmentRepository.removeSchedulerAppointmentItems(deleteList, apptId);
    }

    return insertResult & deleteResult;
  }

  /**
   * Copy additional resources.
   *
   * @param apptId     the appt id
   * @param finalList  the final list
   * @param deleteList the delete list
   * @param apptCat    the appt cat
   */
  public void copyAdditionalResources(Integer apptId, List<Map> finalList, List<Map> deleteList,
      AppointmentCategory apptCat) {

    List<BasicDynaBean> oldList = appointmentRepository.getSchedulerItemsByItemId(apptId);
    if (finalList == null) {
      finalList = new ArrayList<Map>();
    }
    if (oldList != null) {
      for (BasicDynaBean row : oldList) {
        if (row.get("resource_type") != null
            && row.get("resource_type").equals(apptCat.getPrimaryResourceType())) {
          continue;
        } else {
          Boolean present = false;
          if (deleteList != null) {
            for (Map map : deleteList) {
              if (map.get("resource_type").equals(row.get("resource_type"))
                  && map.get("resource_id").equals(row.get("resource_id"))) {
                present = true;
                break;
              }
            }
          }
          if (!present) {
            finalList.add(row.getMap());
          }
        }
      }
    }
  }

  /**
   * Send appointment SM sfor new appointment.
   *
   * @param appointmentId the appointment id
   * @param userName      the user name
   * @param apptStatus    the appt status
   * @param apptTime      the appt time
   */
  public void sendAppointmentSMSforNewAppointment(Integer appointmentId, String userName,
      String apptStatus, Timestamp apptTime) {

    String[] appointmentIds = new String[1];
    appointmentIds[0] = String.valueOf(appointmentId);
    Map<String, Object> jobData = new HashMap<String, Object>();
    jobData.put("schema", RequestContext.getSchema());
    if (apptStatus.equalsIgnoreCase("confirmed")) {
      jobData.put("eventId", "appointment_confirmed");
    } else {
      jobData.put("eventId", "appointment_booked");
    }
    jobData.put("eventData", appointmentIds);
    jobData.put("userName", userName);
    jobData.put("newStatus", apptStatus);
    jobService.scheduleImmediate(
        buildJob("AppointmentStatusChangeSMSJob_" + apptStatus + "" + appointmentId,
            AppointmentStatusChangeSMSJob.class, jobData));
  }

  /**
   * Send appointment SM sfor time change.
   *
   * @param appId the app id
   */
  public void sendAppointmentSMSforTimeChange(Integer appId) {
    List<String> modules = securityService.getActivatedModules();
    if (modules.contains("mod_messaging")) {
      MessageManager mgr = new MessageManager();
      Map<String, Object> jobData = new HashMap<>();
      jobData.put("appointment_id", appId);
      try {
        mgr.processEvent("appointment_details_changed", jobData);
      } catch (SQLException | ParseException | IOException exp) {
        logger.error("Exception caused while triggering appointment_details_changed ", exp);
        throw new HMSException("exception.unable.send.message");
      }
    }
  }

  /**
   * Gets the days appointments for resource.
   *
   * @param resId       the res id
   * @param date        the date
   * @param appCategory the app category
   * @param centerId    the center id
   * @return the days appointments for resource
   */
  public List<BasicDynaBean> getDaysAppointmentsForResource(String resId, Date date,
      AppointmentCategory appCategory, Integer centerId) {

    Integer centerIncDefault = (Integer) genericPreferencesService.getAllPreferences()
        .get("max_centers_inc_default");
    String userName = (String) sessionService.getSessionAttributes().get("userId");
    return appointmentRepository.getAppointments(appCategory, new String[] { resId }, centerId,
        centerIncDefault, date, date, null, new String[] { "Cancel", "Noshow" }, userName);
  }

  /**
   * Gets the appointments for patient.
   *
   * @param centerIds  the center ids
   * @param fromTime   the from time
   * @param mrNo       the mr no
   * @param contactId  the contact id
   * @param patientContact the patient contact
   * @param appId      the app id
   * @param apptStatus the appt status
   * @return the appointments for patient
   */
  public List<Map> getAppointmentsForPatient(List<Integer> centerIds, Timestamp fromTime,
      String mrNo, Integer contactId, String patientContact, Integer appId, String apptStatus) {

    List<BasicDynaBean> beanList = appointmentRepository.getAppointmentsForPatient(centerIds,
        fromTime, mrNo, contactId, patientContact, appId, apptStatus);
    return ConversionUtils.copyListDynaBeansToMap(beanList);
  }

  /**
   * Gets the secondary resources.
   *
   * @param params the params
   * @return the secondary resources
   */
  public Map getSecondaryResources(Map<String, String[]> params) {
    String category = params.get("appt_cat")[0];
    String primRes =
        params.get("prim_res_id") != null ? params.get("prim_res_id")[0] : null;
    AppointmentCategory apptCat = appointmentCategoryFactory
        .getInstance(category.toUpperCase(Locale.ENGLISH));
    Map response = new HashMap();
    response.put("consultation_types",
        primRes != null && !primRes.equals("") ? apptCat
            .getSecondaryResources(primRes) : apptCat
            .getSecondaryResources());
    List defaultList = null;
    Integer centerId = RequestContext.getCenterId();
    if (category.equalsIgnoreCase("DOC") && primRes != null) {
      defaultList = findDoctorPractiontionerType(primRes, centerId);
    }
    response.put("default_consultation", defaultList);
    return response;
  }

  /**
   * Get the doctor practiontier type.
   * @param doctorId doctor id
   * @param centerId center id
   * @return list of consultation types for practioner type
   */
  public List findDoctorPractiontionerType(String doctorId, Integer centerId) {
    Map<String, String> filterMap = new HashMap<>();
    List defaultList = new ArrayList();
    filterMap.put("doctor_id", doctorId);
    BasicDynaBean doctorBean = doctorService.findByPk(filterMap);
    if (doctorBean != null && doctorBean.get("practitioner_id") != null) {
      Integer practitionerTypeId = (Integer) doctorBean.get("practitioner_id");
      defaultList = ConversionUtils.listBeanToListMap(practitionerTypeMappingService
          .getPractitionerMappings(centerId, practitionerTypeId));
    }
    return defaultList;
  }

  /**
   * Gets the appointment count and time.
   *
   * @param resourceId the resource id
   * @param date       the date
   * @return the appointment count and time
   */
  public List<BasicDynaBean> getAppointmentCountAndTime(String resourceId, Date date) {
    return appointmentRepository.getAppointmentCountAndTime(resourceId, date);
  }

  /**
   * Gets the previous appt.
   *
   * @param params the params
   * @return the previous appt
   */
  public Map<String, Object> getPreviousAppt(Map<String, String[]> params) {
    ValidationErrorMap validationErrors = new ValidationErrorMap();
    ;
    Map<String, Object> nestedException = new HashMap<String, Object>();
    if (!appointmentValidator.validateGetPreviousAppt(params, validationErrors)) {
      ValidationException ex = new ValidationException(validationErrors);
      nestedException.put("previousAppt", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    String mrNo = params.get("mr_no") != null ? params.get("mr_no")[0] : "";
    Integer appointmentId = 0;
    if (params.get("appointment_id") != null && !params.get("appointment_id")[0].equals("")) {
      appointmentId = Integer.parseInt(params.get("appointment_id")[0]);
    }
    Map<String, Object> previousApp = new HashMap<String, Object>();
    if (!mrNo.equals("")) {
      BasicDynaBean recentApptBean = appointmentRepository.getPreviousApptDetails(mrNo,
          appointmentId);
      if (recentApptBean != null) {
        previousApp = recentApptBean.getMap();
      }
    }
    return previousApp;
  }

  /**
   * Gets the associated mr no for appointment.
   *
   * @param appointmentId the appointment id
   * @return the associated mr no for appointment
   */
  public BasicDynaBean getAssociatedMrNoForAppointment(Integer appointmentId) {
    return appointmentRepository.getMrNoForAppointment(appointmentId);
  }

  /**
   * Checks if is appointment id valid.
   *
   * @param parameter the parameter
   * @return true, if is appointment id valid
   */
  public boolean isAppointmentIdValid(String parameter) {
    return appointmentRepository.exist("appointment_id", Integer.parseInt(parameter));
  }

  /**
   * Push update appointment status to redis.
   *
   * @param params the params
   */
  public void pushUpdateAppointmentStatusToRedis(Map<String, Object> params) {
    List<Integer> appointmentIdForResponse = new ArrayList();
    List<String> appointmentCatForResponse = new ArrayList();
    ArrayList updateAppList = (ArrayList) params.get("update_app_status");
    for (int i = 0; i < updateAppList.size(); i++) {
      Map app = (Map) updateAppList.get(i);
      appointmentIdForResponse.add((Integer) app.get("appointment_id"));
      appointmentCatForResponse.add((String) app.get("category"));
    }
    String apptCategories = StringUtil.join(appointmentCatForResponse, ",");
    String apptIds = StringUtil.join(appointmentIdForResponse, ",");
    redisMessagePublisher.publishMsgForSchema(RedisMessagePublisher.REDIS_APPOINTMENT_PUSH_CHANNEL,
        apptIds + ";" + apptCategories);
  }

  /**
   * Push consultation appointment to redis.
   *
   * @param id the id
   */
  public void pushConsultationAppointmentToRedis(Integer id) {
    BasicDynaBean consultationBean = doctorConsultationService.findByKey(id);
    if (consultationBean != null && consultationBean.get("appointment_id") != null) {
      int appointmentId = (int) consultationBean.get("appointment_id");
      redisMessagePublisher.publishMsgForSchema(
          RedisMessagePublisher.REDIS_APPOINTMENT_PUSH_CHANNEL, appointmentId + ";" + "DOC");
    }
  }

  /**
   * Save bulk appointments.
   *
   * @param params the params
   * @return the list
   */
  @Transactional(rollbackFor = Exception.class)
  public List<Map<String, Object>> saveBulkAppointments(Map params) {
    List<Map<String, Object>> appointmentsArray = (List<Map<String, Object>>) params.get("values");
    List<Map<String, Object>> finalResponseMapList = new ArrayList<>();
    Set<Integer> packageIds = new HashSet();
    validateBulkAppointments(appointmentsArray);
    for (Map<String, Object> appointmentData : appointmentsArray) {
      Map<String, Object> appointment = (Map<String, Object>) appointmentData.get("appointments");
      AppointmentCategory apptCategory = schedulerService
          .getAppCategory((String) appointment.get("category"));
      Map<String, Object> patientInfo = (Map<String, Object>) appointmentData
          .get("patient");
      contactDetailsRepository.updateContact(patientInfo);
      appointmentData.put("appointments", Arrays.asList(appointment));
      Map responseMap = createNewAppointments(Arrays.asList(apptCategory), appointmentData);
      Map finalResponseMap = new HashMap();
      finalResponseMap.put("patient", appointmentData.get("patient"));
      finalResponseMap.put("appointments", ((List) responseMap.get("appointments")).get(0));
      finalResponseMapList.add(finalResponseMap);
      if (appointment.get("package_id") != null) {
        packageIds.add((Integer) appointment.get("package_id"));
      }
    }
    updateAppointmentsPackageGroupId(finalResponseMapList, packageIds);

    return finalResponseMapList;
  }

  /**
   * Edits the bulk appointment.
   *
   * @param params the params
   * @return the list
   */
  @Transactional(rollbackFor = Exception.class)
  public List editBulkAppointment(Map params) {

    List<Map<String, Object>> appointmentsArray = (List<Map<String, Object>>) params.get("values");
    validateBulkAppointments(appointmentsArray);
    List resList = new ArrayList();

    for (Map appointmentData : appointmentsArray) {
      Map<String, Object> appointment = (Map<String, Object>) appointmentData.get("appointments");
      Map reqAppMap = new HashMap(appointment);
      reqAppMap.put("presc_doc_name", appointment.get("presc_doc_id_name"));

      Map requestMap = new HashMap();
      Map patientMap = (Map) appointmentData.get("patient");
      requestMap.put("patient", patientMap);
      if (patientMap.get("patient_contact") == null
          || patientMap.get("patient_contact").equals("")) {
        ValidationErrorMap errorMap = new ValidationErrorMap();
        errorMap.addError("patient_contact", "exception.scheduler.appointment.phone.contact");
        ValidationException ex = new ValidationException(errorMap);
        Map<String, Object> nestedException = new HashMap<String, Object>();
        nestedException.put("patient", ex.getErrors());
        throw new NestableValidationException(nestedException);
      }
      requestMap.put("appointment", reqAppMap);
      Map<String, Object> additionalInfo = (Map<String, Object>) appointmentData
          .get("additional_info");
      Map<String, Object> patientInfo = (Map<String, Object>) appointmentData
          .get("patient");
      contactDetailsRepository.updateContact(patientInfo);
      requestMap.put("additional_info", additionalInfo);
      AppointmentCategory apptCategory = schedulerService
          .getAppCategory((String) appointment.get("category"));
      resList.add(editAppointment(apptCategory, requestMap));
    }
    return resList;
  }

  /**
   * Validate bulk appointments.
   *
   * @param appointmentsArray the appointments array
   * @return true, if successful
   */
  public boolean validateBulkAppointments(List<Map<String, Object>> appointmentsArray) {
    Map<String, Object> nestedException = new HashMap<>();
    Map patientErrorMap = new HashMap();
    for (int i = 0; i < appointmentsArray.size(); i++) {
      Map<String, Object> appointment = (Map) appointmentsArray.get(i).get("appointments");
      AppointmentCategory apptCategory = schedulerService
          .getAppCategory((String) appointment.get("category"));
      try {
        validate(apptCategory, (Map<String, Object>) appointmentsArray.get(i).get("patient"),
            appointment, null,(String)appointment.get("visit_mode"));
      } catch (NestableValidationException exp) {
        Map errorMap = (Map) exp.getNestedErrorMap();
        List<Map> customizedErrorMaps = new ArrayList();
        Map appointmentsErrorMap = new HashMap<>();
        if (errorMap.get("appointment") != null) {
          appointmentsErrorMap.put("appointments", (Map) errorMap.get("appointment"));
          customizedErrorMaps.add(appointmentsErrorMap);
        }
        if (errorMap.get("patient") != null) {
          if (patientErrorMap.get("patient") == null) {
            patientErrorMap.put("patient", (Map) errorMap.get("patient"));
            customizedErrorMaps.add(patientErrorMap);
          }
        }
        nestedException.put(String.valueOf(i), customizedErrorMaps);
      }
      try {
        validateAppointmentSource((Map<String, Object>) appointmentsArray.get(i).get("patient"),
            appointment);
      } catch (NestableValidationException exp) {
        Map errorMap = (Map) exp.getNestedErrorMap();
        List<Map> customizedErrorMaps = new ArrayList();
        Map appointmentsErrorMap = new HashMap<>();
        if (errorMap.get("appointment") != null) {
          appointmentsErrorMap.put("appointments", (Map) errorMap.get("appointment"));
          customizedErrorMaps.add(appointmentsErrorMap);
        }
        nestedException.put(String.valueOf(i), customizedErrorMaps);
        // if appointment limit is reached throw immediately
        throw new NestableValidationException(nestedException);
      }
    }
    if (!nestedException.isEmpty()) {
      throw new NestableValidationException(nestedException);
    }
    return true;
  }

  /**
   * Generate phone number.
   *
   * @param patientContact the patient contact
   * @return the string
   */
  public String generatePhoneNumber(String patientContact) {
    DynaBeanBuilder builder = new DynaBeanBuilder();
    builder.add("patient_contact");
    BasicDynaBean bean = builder.build();
    bean.set("patient_contact", patientContact);
    appointmentValidator.setRuleSetMap(bean);
    appointmentValidator.setParentKey("patient");
    appointmentValidator.validate(bean);
    return (String) bean.get("patient_contact");
  }

  /**
   * Find by key.
   *
   * @param appointmentId the appointment id
   * @return the basic dyna bean
   */
  public BasicDynaBean findByKey(Integer appointmentId) {
    return appointmentRepository.findByKey("appointment_id", appointmentId);
  }

  /**
   * Updates appointment status using received sms.
   * 
   * @param paramMap     the map
   * @param appointments the list of beans
   */
  public void updateAppointmentStatus(Map<String, Object> paramMap,
      List<BasicDynaBean> appointments) {
    String appointmentStatus = paramMap.get("status").toString();
    appointmentRepository.updateAppointmentStatus(paramMap, appointments);
    String eventId = appointmentStatus.equalsIgnoreCase("confirmed") ? "appointment_confirmed"
        : "appointment_cancelled";
    for (BasicDynaBean bean : appointments) {
      scheduleAppointmentStatusChangeSMSJob(bean.get("appointment_id").toString(), "_system",
          appointmentStatus, eventId);
    }
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
      String primaryResource, String primaryResourceType, Integer waitlist) {
    long apptTimeLong = startApptTime.getTime();
    apptTimeLong = apptTimeLong + (duration * 60 * 1000);
    Timestamp endTime = new java.sql.Timestamp(apptTimeLong);

    List<BasicDynaBean> appointmentsForUpgrade = appointmentRepository.isSlotBooked(startApptTime,
        endTime, "-1", primaryResource, primaryResourceType, waitlist);
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
      appointmentRepository.batchUpgradeWaitlist(updateParamsList);
      HashMap<String, Object> messageData = new HashMap<String, Object>();
      messageData.put("appointment_ids", apptIds);
      MessageManager mgr = new MessageManager();
      try {
        mgr.processEvent("waitlist", messageData);
      } catch (Exception exp) {
        logger.error("", exp);
      }

    }
  }
  
  public BasicDynaBean getPatientDetailsForAppointment(Integer apptId) {
    return appointmentRepository.getPatientDetailsForAppointment(apptId);
  }
  
  public List<BasicDynaBean> getClientDetails(String eventId) {
    return appointmentRepository.getClientDetails(eventId);
  }
  
  public Map<String,Object> getSubscriberDetailsForEvent(String eventId, int clientId) {
    return appointmentRepository.getSubscriberDetailsForEvent(eventId,clientId);
  }
}
