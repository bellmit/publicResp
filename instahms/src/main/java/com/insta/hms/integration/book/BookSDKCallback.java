package com.insta.hms.integration.book;

import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.DateHelper;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.scheduler.ResourceService;
import com.insta.hms.mdm.centerpreferences.CenterPreferencesService;
import com.insta.hms.mdm.doctors.DoctorService;
import com.insta.hms.redis.RedisMessagePublisher;
import com.practo.integration.sdk.AppointmentAddUpdateResponse;
import com.practo.integration.sdk.AppointmentStatus;
import com.practo.integration.sdk.CalendarSyncResponse;
import com.practo.integration.sdk.DoctorAddUpdateResponse;
import com.practo.integration.sdk.DoctorStatus;
import com.practo.integration.sdk.PatientDetails;
import com.practo.integration.sdk.PractoAppointmentResponse;
import com.practo.integration.sdk.PractoEstablishmentIntegrationSDKCallback;
import com.practo.integration.sdk.SDKException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * We pass the Callback instance to Practo SDK for each center of a hospital. Any activity/callback
 * that happens for a particular center is invoked using this instance. The thread that invokes the
 * members of this function will not be from Tomcat, so we have to set connection details to
 * RequestContext
 */

@Component
@Scope("prototype")
public class BookSDKCallback implements PractoEstablishmentIntegrationSDKCallback {
  
  /** The Constant logger. */
  private static final Logger logger = LoggerFactory.getLogger(BookSDKCallback.class);

  /** The resource service. */
  @LazyAutowired
  private ResourceService resourceService;

  /** The doctor service. */
  @LazyAutowired
  private DoctorService doctorService;

  /** The book integration service. */
  @LazyAutowired
  private BookIntegrationService bookIntegrationService;

  /** The center preferences service. */
  @LazyAutowired
  private CenterPreferencesService centerPreferencesService;

  /** The redis message publisher. */
  @LazyAutowired
  RedisMessagePublisher redisMessagePublisher;

  /** The schema. */
  private String schema;

  /** The center id. */
  private int centerId;

  /** The Constant ERROR_PREFIX. */
  private static final String ERROR_PREFIX = "Error:";

  /** The Constant REGISTRATION_SUCCESSFUL. */
  private static final String REGISTRATION_SUCCESSFUL = "REGISTRATION_SUCCESSFUL";

  /** The Constant MOBILE_NUMBER_CANNOT_BE_EMPTY. */
  private static final String MOBILE_NUMBER_CANNOT_BE_EMPTY = "Mobile number cannot be empty";

  /** The Constant DOCTOR_NOT_AVAILABLE. */
  private static final String DOCTOR_NOT_AVAILABLE = "Doctor is not available for this appointment";

  /**
   * Instantiates a new book SDK callback.
   *
   * @param schema the schema
   * @param centerId the center id
   */
  public BookSDKCallback(String schema, int centerId) {
    this.schema = schema;
    this.centerId = centerId;
  }

  @Override
  public void handleDoctorStatusChange(String establishmentKey, String doctorId,
      DoctorStatus doctorStatus) throws SDKException {
    RequestContext.setConnectionDetails(new String[] { null, null, schema });
    logger.info(String.format(
        "handleDoctorStatusChange Invoked: Doctor : [%s] status for centerId: [%s]"
        + " got changed to [%s]", doctorId, centerId, doctorStatus));
    doctorService.updateDoctorStatusOnPracto(doctorId, centerId, doctorStatus.name());
  }

  @Override
  public PractoAppointmentResponse handlePractoAppointmentCreated(String establishmentKey,
      String practoAppointmentId, String doctorId, String startTime, String endTime,
      PatientDetails patientDetails, AppointmentStatus status, String appointmentCreationTime)
      throws SDKException {
    RequestContext.setConnectionDetails(new String[] { null, null, schema });
    logger.info(String.format(
        "handlePractoAppointmentCreated Invoked: New appointment received from Practo. "
        + "Practo Appointment Id : [%s] , centerId: [%s] "
        + "startTime [%s] , endTime [%s] for Doctor : [%s], status: [%s]",
        practoAppointmentId, centerId, startTime, endTime, doctorId, status));

    if (patientDetails.getMobileNumbers() == null || patientDetails.getMobileNumbers().isEmpty()) {
      // Patient mobile number is mandatory at Insta, SO, cancel the appointment
      logger.error(
          String.format("Practo Appointment : [%s] centerId: [%s] was cancelled because [%s] ",
              practoAppointmentId, centerId, MOBILE_NUMBER_CANNOT_BE_EMPTY));
      throw new SDKException(MOBILE_NUMBER_CANNOT_BE_EMPTY);
    }

    try {
      Timestamp appointmentStartTime = DateUtil.parseIso8601Timestamp(startTime);
      Timestamp appointmentEndTime = DateUtil.parseIso8601Timestamp(endTime);
      /*
       * if (resourceService.isResourceAvailable(doctorId, "DOC", appointmentStartTime,
       * appointmentEndTime, centerId)) {
       */
      String name = getPatientName(patientDetails);
      PatientDTO patient = new PatientDTO(name,
          patientDetails.getEmailIds() != null ? patientDetails.getEmailIds().get(0) : null,
          patientDetails.getMobileNumbers() != null
              ? patientDetails.getMobileNumbers().get(0)
              : null,
          null);
      /*
       * int instaAppointmentId = resourceService.addPractoAppointment(practoAppointmentId,
       * doctorId, appointmentStartTime, appointmentEndTime, patient,
       * BookSDKUtil.getAppointmentStatus(status), centerId);
       */
      int instaAppointmentId = bookIntegrationService.savePractoAppointment(practoAppointmentId,
          doctorId, appointmentStartTime, appointmentEndTime, patient,
          BookSDKUtil.getAppointmentStatus(status), centerId);
      logger.info(String.format(
          "Practo Appointment : [%s] centerId: [%s] was successfully registered on Insta with"
          + " insta AppointmentId [%d]",
          practoAppointmentId, centerId, instaAppointmentId));
      return new PractoAppointmentResponse(String.valueOf(instaAppointmentId), null, null);
      // }
    } catch (ParseException ex) {
      logger.error("", ex);
    } catch (Exception ex) {
      logger.error("", ex);
    }
    // Doctor is not available for this appointment.So, Cancel the appointment at Practo
    logger
        .info(String.format("Practo Appointment : [%s] centerId: [%s] was cancelled because [%s] ",
            practoAppointmentId, centerId, DOCTOR_NOT_AVAILABLE));
    throw new SDKException(DOCTOR_NOT_AVAILABLE);

  }

  @Override
  public PractoAppointmentResponse handlePractoAppointmentUpdated(String establishmentKey,
      String practoAppointmentId, String appointmentId, String startTime, String endTime,
      PatientDetails patientDetails, AppointmentStatus status, String appointmentUpdatedTime)
      throws SDKException {

    RequestContext.setConnectionDetails(new String[] { null, null, schema });

    BasicDynaBean appointmentBean = resourceService.getAppointmentBean("appointment_id",
        Integer.parseInt(appointmentId));
    String doctorId = (String) appointmentBean.get("prim_res_id");

    logger.info(String.format(
        "handlePractoAppointmentUpdated Invoked: Practo Appointment: [%s], Insta Appointment Id:"
        + " [%s], updated at Practo, startTime [%s] , endTime [%s] for Doctor : [%s],"
        + "status : [%s]",
        practoAppointmentId, appointmentId, startTime, endTime, doctorId, status));
    try {
      Timestamp appointmentStartTime = DateUtil.parseIso8601Timestamp(startTime);
      Timestamp appointmentEndTime = DateUtil.parseIso8601Timestamp(endTime);
      boolean isResheduled = (appointmentStartTime
          .getTime() != ((Timestamp) appointmentBean.get("appointment_time")).getTime())
          || (((appointmentEndTime.getTime() - appointmentStartTime.getTime())
              / (1000 * 60)) != (Integer) appointmentBean.get("duration"));

      if (isResheduled && appointmentBean.get("appointment_status") != null
          && appointmentBean.get("appointment_status").equals("Arrived")) {
        // Cannot reschedule appointment as patient arrived
        throw new SDKException("Patient already arrived. So cannot reschedule");
      }
      boolean isResourceAvailable = resourceService.isResourceAvailable(doctorId, "DOC",
              appointmentStartTime, appointmentEndTime, centerId);
      logger.info("doctor availablity is " + isResourceAvailable);
      if (!isResheduled || isResourceAvailable) {
        String phoneNo = (patientDetails.getMobileNumbers() == null)
            ? null
            : patientDetails.getMobileNumbers().get(0);
        String emailId = (patientDetails.getEmailIds() == null)
            ? null
            : patientDetails.getEmailIds().get(0);
        resourceService.updatePractoAppointment(appointmentBean, appointmentStartTime,
            appointmentEndTime, getPatientName(patientDetails), phoneNo,
            BookSDKUtil.getAppointmentStatus(status), emailId);
        logger.info(String.format(
            "Practo Appointment Id: [%s] , Insta appt : [%s] is successfully updated on Insta",
            practoAppointmentId, appointmentId));
        redisMessagePublisher.publishMsgForSchema(
            RedisMessagePublisher.REDIS_APPOINTMENT_PUSH_CHANNEL, appointmentId + ";" + "DOC");
      }
      return new PractoAppointmentResponse(appointmentId, null, null);
    } catch (ParseException ex) {
      logger.error("", ex);
    }
    logger.info(String.format(
        "Failed to update Practo Appointment. Insta appointment Id: [%s], centerId: [%s],"
        + " because [%s] ", appointmentId, centerId, DOCTOR_NOT_AVAILABLE));
    // notify SDK that failed to update
    throw new SDKException(DOCTOR_NOT_AVAILABLE);
  }


  @Override
  public List<CalendarSyncResponse> handleCalendarSync(String establishmentKey, String doctorId,
      String syncStartTime, String syncEndTime) throws SDKException {
    RequestContext.setConnectionDetails(new String[] { null, null, schema });
    doctorService.updateDoctorStatusOnPracto(doctorId, centerId,
        BookSDKUtil.CALENDAR_SYNC_CALLBACK_RECEIVED);
    logger.info(String.format(
        "Entered handleCalendarSync with params :establishmentKey [%s] , doctorId: [%s] ,"
        + "centerId: [%s], syncStartTime : [%s] , syncEndTime : [%s] ",
        establishmentKey, doctorId, centerId, syncStartTime, syncEndTime));
    // share the overrides of Doctor to Practo
    List<BasicDynaBean> overridesList = resourceService.getResourceOverrides(doctorId, "DOC",
        DateUtil.getCurrentDate(), null, null, null);

    List<CalendarSyncResponse> calenderList = null;
    try {
      BookSDKUtil.overrideDoctorTimings(establishmentKey, centerId, doctorId, overridesList);
      Timestamp startTime = DateUtil.parseIso8601Timestamp(syncStartTime);
      Timestamp endTime = DateUtil.parseIso8601Timestamp(syncEndTime);
      BasicDynaBean centerPrefs = centerPreferencesService.getCenterPreferences(centerId);
      boolean sharePatientDetails = (centerPrefs.get("share_pat_details_to_practo") != null)
          ? (Boolean) centerPrefs.get("share_pat_details_to_practo")
          : false;
      List<BasicDynaBean> list = resourceService.getResourceAppointments(doctorId, "DOC", centerId,
          startTime, endTime, sharePatientDetails);
      calenderList = new ArrayList<CalendarSyncResponse>();
      for (BasicDynaBean bean : list) {
        PatientDTO patientDTO = new PatientDTO();
        if (sharePatientDetails) {
          patientDTO.setMrNo((String) bean.get("mr_no"));
          patientDTO.setName((String) bean.get("patient_name"));
          patientDTO.setEmailId((String) bean.get("email_id"));
          patientDTO.setPhoneNo((String) bean.get("patient_contact"));
        }
        Timestamp apptStartTime = (Timestamp) bean.get("appointment_time");
        Timestamp apptEndTime = (Timestamp) bean.get("end_appointment_time");
        CalendarSyncResponse calender = new CalendarSyncResponse(
            String.valueOf(bean.get("appointment_id")),
            DateHelper.getUTCTime(apptStartTime.getTime(),
                DateHelper.UTC_YEAR_MONTH_DATE_HOUR_MINUTE),
            DateHelper.getUTCTime(apptEndTime.getTime(),
                DateHelper.UTC_YEAR_MONTH_DATE_HOUR_MINUTE),
            patientDTO.getPatientDetails(), AppointmentStatus.CONFIRMED);
        calenderList.add(calender);
      }
    } catch (ParseException ex) {
      logger.error("", ex);
    }
    logger.info(String.format(
        "handleCalenderSync Invoked: Appointments of Doctor : [%s] , centerId: [%s] ,"
        + " startTime : [%s] , endTime : [%s] , Calender: [%s] ",
        doctorId, centerId, syncStartTime, syncEndTime, calenderList));

    return calenderList;
  }

  @Override
  public void handleDoctorAddUpdateResponse(String establishmentKey, String doctorId,
      DoctorAddUpdateResponse responseStatus, String responseMessage) throws SDKException {
    RequestContext.setConnectionDetails(new String[] { null, null, schema });
    logger.info(String.format(
        "handleDoctorAddUpdateResponse Invoked: Doctor : [%s], centerId: [%s],"
        + " Response status : [%s] Response Message : [%s]",
        doctorId, centerId, responseStatus, responseMessage));
    if (responseStatus == DoctorAddUpdateResponse.DOCTOR_REGISTRATION_SUCCEEDED) {
      doctorService.updateDoctorStatusOnPracto(doctorId, centerId, REGISTRATION_SUCCESSFUL);
    } else if (responseStatus == DoctorAddUpdateResponse.DOCTOR_REGISTRATION_FAILED) {
      doctorService.updateDoctorStatusOnPracto(doctorId, centerId, ERROR_PREFIX + responseMessage);
    }
  }

  /**
   * Gets the patient name.
   *
   * @param patientDetails the patient details
   * @return the patient name
   */
  private String getPatientName(PatientDetails patientDetails) {
    StringBuilder name = new StringBuilder();
    if (patientDetails.getFirstName() != null) {
      name.append(patientDetails.getFirstName()).append(" ");
    }
    if (patientDetails.getMiddleName() != null) {
      name.append(patientDetails.getMiddleName()).append(" ");
    }
    if (patientDetails.getLastName() != null) {
      name.append(patientDetails.getLastName());
    }
    return name.toString().trim();
  }


  @Override
  public void handleAppointmentAddUpdateResponse(String establishmentKey, String appointmentId,
      AppointmentAddUpdateResponse responseStatus, String responseMessage) throws SDKException {
    logger.info(String.format(
        "handleAppointmentAddUpdateResponse Invoked: CenterId: [%s], Insta appointmentId [%s], "
            + "Response status: [%s], Response Message: [%s]",
        centerId, appointmentId, responseStatus, responseMessage));

  }

}
