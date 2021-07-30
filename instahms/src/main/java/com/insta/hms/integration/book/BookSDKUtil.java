package com.insta.hms.integration.book;

import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.DateHelper;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.messaging.InstaIntegrationDao;
import com.practo.integration.sdk.AppointmentStatus;
import com.practo.integration.sdk.AppointmentVisitEvent;
import com.practo.integration.sdk.AvailabilityType;
import com.practo.integration.sdk.DoctorRegistrationDetails;
import com.practo.integration.sdk.DoctorStatus;
import com.practo.integration.sdk.PractoEstablishmentIntegrationSDK;
import com.practo.integration.sdk.PractoIntegrationSDKFactory;
import com.practo.integration.sdk.SDKErrorCode;
import com.practo.integration.sdk.SDKException;
import com.practo.integration.sdk.WeeklyAvailability;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
// TODO: Auto-generated Javadoc

/**
 * Wrapper for BookSDK calls.
 *
 * @author Sairam
 */
public class BookSDKUtil {

  /** The Constant logger. */
  private static final Logger logger = LoggerFactory.getLogger(BookSDKUtil.class);

  /** The Constant APPOINTMENT_CONFIRMED. */
  public static final String APPOINTMENT_CONFIRMED = "Confirmed";

  /** The Constant APPOINTMENT_CANCELLED. */
  public static final String APPOINTMENT_CANCELLED = "Cancel";

  /** The Constant CALENDAR_SYNC_CALLBACK_RECEIVED. */
  public static final String CALENDAR_SYNC_CALLBACK_RECEIVED = "CALENDAR_SYNC_CALLBACK_RECEIVED";

  /** The Constant PRACTO_BOOK_ENABLED. */
  public static final String PRACTO_BOOK_ENABLED = DoctorStatus.DOCTOR_APPOINTMENT_ENABLED.name();

  /** The Constant PRACTO_DOCTOR_LISTED. */
  public static final String PRACTO_DOCTOR_LISTED = DoctorStatus.DOCTOR_LISTED.name();

  /** The Constant CONNECTION_SCHEME. */
  private static final String CONNECTION_SCHEME = "http";

  /** The Constant PRACTO_BOOK_INTEGRATION. */
  public static final String PRACTO_BOOK_INTEGRATION = "practo_book";

  /** The Constant MODULE_ID. */
  public static final String MODULE_ID = "mod_practo_advantage";

  /**
   * Instantiates a new book SDK util.
   */
  private BookSDKUtil() {
  }

  /**
   * Handshake with Practo.
   *
   * @param applicationID
   *          the application ID
   * @param applicationSecret
   *          the application secret
   * @param establishmentKey
   *          the establishment key
   * @param sdkCallback
   *          the sdk callback
   * @param agentHost
   *          the agent host
   * @param agentPort
   *          the agent port
   * @throws SDKException
   *           the SDK exception
   */
  public static void init(String applicationID, String applicationSecret, String establishmentKey,
      BookSDKCallback sdkCallback, String agentHost, int agentPort) throws SDKException {
    PractoEstablishmentIntegrationSDK practoSDK = PractoIntegrationSDKFactory
        .getSDKInstance(establishmentKey);
    practoSDK.init(applicationID, applicationSecret, establishmentKey, sdkCallback,
        CONNECTION_SCHEME, agentHost, agentPort);

  }

  /**
   * Checks if is null or empty.
   *
   * @param value
   *          the value
   * @return true, if is null or empty
   */
  public static boolean isNullOrEmpty(String value) {
    return value == null || value.isEmpty();
  }

  /**
   * Register the Doctor on Practo.com . We can register the same doctor from multiple centers
   *
   * @param doctorDetails
   *          the doctor details
   * @param departmentName
   *          - Department of Doctor
   * @param slotDurationInMinutes
   *          - Default duration for an appointment
   * @param doctorTimings
   *          - The list containing all the weekly availabilities for the given centerId
   * @param establishmentKey
   *          - The key corresponding to center for which doctor registration is to be done.
   * @param centerId
   *          the center id
   * @throws SDKException
   *           the SDK exception
   */
  public static void registerDoctor(Map<String, Object> doctorDetails, String departmentName,
      int slotDurationInMinutes, List<DoctorTiming> doctorTimings, String establishmentKey,
      Integer centerId) throws SDKException {
    List<String> phoneNumbers = new ArrayList<String>();
    String mobileNumber = (String) doctorDetails.get("doctor_mobile");
    if (!isNullOrEmpty(mobileNumber)) {
      phoneNumbers.add(mobileNumber);
    }
    String residentialPhone = (String) doctorDetails.get("res_phone");
    if (!isNullOrEmpty(residentialPhone)) {
      phoneNumbers.add(residentialPhone);
    }
    String clinicNumber = (String) doctorDetails.get("clinic_phone");
    if (!isNullOrEmpty(clinicNumber)) {
      phoneNumbers.add(clinicNumber);
    }
    List<String> emailIds = new ArrayList<String>();
    String emailId = (String) doctorDetails.get("doctor_mail_id");
    if (!isNullOrEmpty(emailId)) {
      emailIds.add(emailId);
    }
    List<String> specialities = new ArrayList<String>();
    String speciality = (String) doctorDetails.get("specialization");
    if (!isNullOrEmpty(speciality)) {
      specialities.add(speciality);
    }
    String doctorId = (String) doctorDetails.get("doctor_id");
    WeeklyAvailability weeklyTimings = new WeeklyAvailability();
    List<DoctorRegistrationDetails> registrationsList = new ArrayList<DoctorRegistrationDetails>();
    String registrationNum = (String) doctorDetails.get("registration_no");
    if (!isNullOrEmpty(registrationNum)) {
      DoctorRegistrationDetails doctorRegistrationDetails = new DoctorRegistrationDetails();
      doctorRegistrationDetails.setRegistrationNumber(registrationNum);
      registrationsList.add(doctorRegistrationDetails);
    }
    logger.info(String.format(
        "Registering the Doctor on Practo. Doctor  details: centerId : [%s] DoctorId : [%s]"
        + " Slot duration : [%d] Department : [%s] Specialities: [%s] Doctor Weekly Timings :"
        + " [%s] Registrations: [%s]",
        centerId, doctorId, slotDurationInMinutes, departmentName, specialities, doctorTimings,
        registrationsList));

    for (DoctorTiming doctorTiming : doctorTimings) {
      weeklyTimings.addDoctorTimings(doctorTiming.getDayOfWeek(), doctorTiming.getStartTime(),
          doctorTiming.getEndTime());
    }
    PractoEstablishmentIntegrationSDK practoSDK = PractoIntegrationSDKFactory
        .getSDKInstance(establishmentKey);
    // call SDK method to register the doctor
    String doctorName = (String) doctorDetails.get("doctor_name");
    practoSDK.registerDoctor(doctorName, doctorId, phoneNumbers, emailIds, specialities,
        registrationsList, departmentName, slotDurationInMinutes, weeklyTimings);
    logger.info(String.format("Doctor: [%s] centerId: [%s] registration is submitted to Practo!",
        doctorId, centerId));

  }

  /**
   * Send newly created Insta Appointment to Practo.
   *
   * @param establishmentKey
   *          the establishment key
   * @param appointmentId
   *          the appointment id
   * @param doctorId
   *          the doctor id
   * @param apptStartTime
   *          the appt start time
   * @param duration
   *          the duration
   * @param status
   *          the status
   * @param patient
   *          the patient
   */
  public static void addAppointment(String establishmentKey, String appointmentId, String doctorId,
      Timestamp apptStartTime, int duration, String status, PatientDTO patient) {
    PractoEstablishmentIntegrationSDK practoSDK = PractoIntegrationSDKFactory
        .getSDKInstance(establishmentKey);
    // set the appointment duration details
    String startTime = DateHelper.getUTCTime(apptStartTime.getTime(),
        DateHelper.UTC_YEAR_MONTH_DATE_HOUR_MINUTE);
    String endTime = DateHelper.getUTCTime(apptStartTime.getTime() + duration * 60 * 1000,
        DateHelper.UTC_YEAR_MONTH_DATE_HOUR_MINUTE);
    try {
      practoSDK.addAppointment(appointmentId, doctorId, startTime, endTime,
          patient.getPatientDetails(), getAppointmentStatus(status));
    } catch (SDKException ex) {
      initToPracto();
      try {
        practoSDK.addAppointment(appointmentId, doctorId, startTime, endTime,
            patient.getPatientDetails(), getAppointmentStatus(status));
      } catch (SDKException ex1) {
        logger.error("", ex1);
      }
      logger.error(String.format("Failed to push appointment: %s to Practo", appointmentId), ex);
    }
  }

  /**
   * Send Updated Insta Appointment to Practo.
   *
   * @param establishmentKey
   *          the establishment key
   * @param appointmentId
   *          the appointment id
   * @param apptStartTime
   *          the appt start time
   * @param duration
   *          the duration
   * @param status
   *          the status
   * @param patient
   *          the patient
   */
  public static void updateAppointment(String establishmentKey, String appointmentId,
      Timestamp apptStartTime, int duration, String status, PatientDTO patient) {
    PractoEstablishmentIntegrationSDK practoSDK = PractoIntegrationSDKFactory
        .getSDKInstance(establishmentKey);
    // Set the patient details
    // set the appointment duration details
    String startTime = DateHelper.getUTCTime(apptStartTime.getTime(),
        DateHelper.UTC_YEAR_MONTH_DATE_HOUR_MINUTE);
    String endTime = DateHelper.getUTCTime(apptStartTime.getTime() + duration * 60 * 1000,
        DateHelper.UTC_YEAR_MONTH_DATE_HOUR_MINUTE);
    try {
      practoSDK.updateAppointment(appointmentId, startTime, endTime, patient.getPatientDetails(),
          getAppointmentStatus(status));
    } catch (SDKException ex) {
      initToPracto();
      try {
        practoSDK.updateAppointment(appointmentId, startTime, endTime, patient.getPatientDetails(),
            getAppointmentStatus(status));
      } catch (SDKException ex1) {
        logger.error("", ex1);
      }
      logger.error("", ex);
    }
  }

  /**
   * Send updated Doctor details to Practo.
   *
   * @param doctorDetails
   *          the doctor details
   * @param doctorTimings
   *          the doctor timings
   * @param establishmentKey
   *          the establishment key
   */
  public static void updateDoctorDetails(Map<String, Object> doctorDetails,
      List<DoctorTiming> doctorTimings, String establishmentKey) {
    try {
      List<String> phoneNumbers = new ArrayList<String>();
      String mobileNumber = (String) doctorDetails.get("doctor_mobile");
      if (!isNullOrEmpty(mobileNumber)) {
        phoneNumbers.add(mobileNumber);
      }
      String residentialPhone = (String) doctorDetails.get("res_phone");
      if (!isNullOrEmpty(residentialPhone)) {
        phoneNumbers.add(residentialPhone);
      }
      String clinicNumber = (String) doctorDetails.get("clinic_phone");
      if (!isNullOrEmpty(clinicNumber)) {
        phoneNumbers.add(clinicNumber);
      }
      List<String> emailIds = new ArrayList<String>();
      String emailId = (String) doctorDetails.get("doctor_mail_id");
      if (!isNullOrEmpty(emailId)) {
        emailIds.add(emailId);
      }
      List<String> specialities = new ArrayList<String>();
      String speciality = (String) doctorDetails.get("specialization");
      if (!isNullOrEmpty(speciality)) {
        specialities.add(speciality);
      }
      String doctorId = (String) doctorDetails.get("doctor_id");
      List<DoctorRegistrationDetails> registrationsList =
          new ArrayList<DoctorRegistrationDetails>();
      String registrationNum = (String) doctorDetails.get("registration_no");
      if (!isNullOrEmpty(registrationNum)) {
        DoctorRegistrationDetails doctorRegistrationDetails = new DoctorRegistrationDetails();
        doctorRegistrationDetails.setRegistrationNumber(registrationNum);
        registrationsList.add(doctorRegistrationDetails);
      }
      WeeklyAvailability weeklyTimings = new WeeklyAvailability();
      for (DoctorTiming doctorTiming : doctorTimings) {
        weeklyTimings.addDoctorTimings(doctorTiming.getDayOfWeek(), doctorTiming.getStartTime(),
            doctorTiming.getEndTime());
      }
      logger.info(
          String.format("Updating the Doctor on Practo. Doctor  details \n  DoctorId : %s \n  "
              + " Doctor Weekly Timings : %s", doctorId, doctorTimings));
      PractoEstablishmentIntegrationSDK practoSDK = PractoIntegrationSDKFactory
          .getSDKInstance(establishmentKey);
      // call SDK method to update doctor details
      practoSDK.updateDoctorDetails(doctorId, phoneNumbers, emailIds, specialities,
          registrationsList, weeklyTimings);
      logger.info(String.format("Doctor: %s details are  successfully sent to Practo!", doctorId));
    } catch (SDKException ex) {
      logger.error("", ex);
    }
  }

  /**
   * Adds the doctor overrides to practo.
   *
   * @param establishmentKey
   *          the establishment key
   * @param centerId
   *          the center id
   * @param doctorId
   *          the doctor id
   * @param startTimestampStr
   *          the start timestamp str
   * @param endTimestampStr
   *          the end timestamp str
   * @param isAvailable
   *          the is available
   */
  public static void addDoctorOverridesToPracto(String establishmentKey, Integer centerId,
      String doctorId, String startTimestampStr, String endTimestampStr, Boolean isAvailable) {
    logger.info(String.format(
        "Adding Doctor overrides to Practo. CenterId: [%s], DoctorId: [%s] startDate: [%s],"
        + " endDate: [%s], isAvailable : [%s]",
        centerId, doctorId, startTimestampStr, endTimestampStr, isAvailable));
    try {
      PractoEstablishmentIntegrationSDK practoSDK = PractoIntegrationSDKFactory
          .getSDKInstance(establishmentKey);
      AvailabilityType availabilityType = isAvailable
          ? AvailabilityType.ALLOW
          : AvailabilityType.BLOCK;
      practoSDK.configureDoctorTimings(doctorId, startTimestampStr, endTimestampStr,
          availabilityType);
    } catch (SDKException ex) {
      logger.error("", ex);
    }
  }

  /**
   * Notify appointment visit event to practo.
   *
   * @param establishmentKey
   *          the establishment key
   * @param centerId
   *          the center id
   * @param appointmentId
   *          the appointment id
   * @param event
   *          the event
   */
  public static void notifyAppointmentVisitEventToPracto(String establishmentKey, Integer centerId,
      String appointmentId, AppointmentVisitEvent event) {
    PractoEstablishmentIntegrationSDK practoSDK = PractoIntegrationSDKFactory
        .getSDKInstance(establishmentKey);
    String eventTime = DateHelper.getUTCTime(System.currentTimeMillis(),
        DateHelper.UTC_YEAR_MONTH_DATE_HOUR_MINUTE);
    try {
      practoSDK.notifyAppointmentVisitEvent(appointmentId, event, eventTime, null);
      logger.info(String.format(
          "Notifying patient visit event to Practo. CenterId: [%s], Insta AppointmentId : [%s], "
              + " event name: [%s], event time : [%s], ",
          centerId, appointmentId, event, eventTime));
    } catch (SDKException ex) {
      initToPracto();
      try {
        practoSDK.notifyAppointmentVisitEvent(appointmentId, event, eventTime, null);
      } catch (SDKException ex1) {
        logger.error("", ex1);
      }
      logger.error("", ex);
    }
  }

  /**
   * Override doctor timings.
   *
   * @param establishmentKey
   *          the establishment key
   * @param centerId
   *          the center id
   * @param doctorId
   *          the doctor id
   * @param overridesList
   *          the overrides list
   * @throws ParseException
   *           the parse exception
   */
  public static void overrideDoctorTimings(String establishmentKey, Integer centerId,
      String doctorId, List<BasicDynaBean> overridesList) throws ParseException {
    for (BasicDynaBean override : overridesList) {
      Boolean isAvailable = (override.get("availability_status").equals("A")
          && override.get("center_id").equals(centerId));
      Date date = (Date) override.get("availability_date");

      Time startTime = (Time) override.get("from_time");
      Time endTime = (Time) override.get("to_time");
      endTime = new Time(endTime.getTime() - 1 * 60 * 1000);

      Timestamp startTimestamp = DateUtil.timestampFromDateTime(date, startTime);
      Timestamp endTimestamp = DateUtil.timestampFromDateTime(date, endTime);

      // convert timestamp to UTC
      String startTimestampUTC = DateUtil.formatIso8601TimestampNoSec(startTimestamp);
      String endTimestampUTC = DateUtil.formatIso8601TimestampNoSec(endTimestamp);
      addDoctorOverridesToPracto(establishmentKey, centerId, doctorId, startTimestampUTC,
          endTimestampUTC, isAvailable);
    }
  }

  /**
   * Add Doctor overrides to Practo.
   *
   * @param establishmentKey
   *          the establishment key
   * @param centerId
   *          - The center for which overrides are to be fetched
   * @param doctorId
   *          the doctor id
   * @param startDate
   *          - This is used as availability_date if present. Used when overridesList does
   *          not have {@code availability_date}
   * @param endDate
   *          - The overrides that are present in overridesList are recurrent till endDate.
   *          This parameter needs to be used when overridesList contains overrides for
   *          single date.
   * @param overridesList
   *          - List of overrides including non-availabilities
   * @throws ParseException
   *           the parse exception
   */
  public static void overrideDoctorTimings(String establishmentKey, Integer centerId,
      String doctorId, Date startDate, Date endDate, List<BasicDynaBean> overridesList)
      throws ParseException {
    Calendar start = Calendar.getInstance();
    start.setTime(startDate);
    Calendar end = Calendar.getInstance();
    end.setTime(endDate);
    for (java.util.Date dateIterator = start.getTime(); end.after(start) || end.equals(start); start
        .add(Calendar.DATE, 1), dateIterator = start.getTime()) {
      for (BasicDynaBean override : overridesList) {
        Boolean isAvailable;
        if (override.get("center_id") != null) {
          isAvailable = (override.get("availability_status").equals("A")
              && override.get("center_id").equals(centerId));
        } else {
          isAvailable = (override.get("availability_status").equals("A") && centerId == 0);
        }
        Time startTime = (Time) override.get("from_time");
        Time endTime = (Time) override.get("to_time");
        endTime = new Time(endTime.getTime() - 1 * 60 * 1000);

        Timestamp startTimestamp = DateUtil.timestampFromDateTime(dateIterator, startTime);
        Timestamp endTimestamp = DateUtil.timestampFromDateTime(dateIterator, endTime);

        // convert timestamp to UTC
        String startTimestampUTC = DateUtil.formatIso8601TimestampNoSec(startTimestamp);
        String endTimestampUTC = DateUtil.formatIso8601TimestampNoSec(endTimestamp);
        addDoctorOverridesToPracto(establishmentKey, centerId, doctorId, startTimestampUTC,
            endTimestampUTC, isAvailable);
      }
    }

  }

  /**
   * Close all.
   */
  public static void closeAll() {
    PractoIntegrationSDKFactory.closeAll();
  }

  /**
   * Close.
   *
   * @param establishmentKey
   *          the establishment key
   */
  public static void close(String establishmentKey) {
    PractoIntegrationSDKFactory.closeSDKInstance(establishmentKey);
  }

  /**
   * Maps the Insta dayOfWeek to Book SDK dayOfWeek. In Book SDK, The DayOfWeek starts from MONDAY
   * with value 0. In Insta , The dayOfWeek starts from SUNDAY with value 0
   * 
   * @param dayOfWeek
   *          - Insta representation of day.
   * @return The Book ENUM representation of Insta dayOfWeek
   */
  public static WeeklyAvailability.DayOfWeek getDayOfWeekEnum(int dayOfWeek) {
    return WeeklyAvailability.DayOfWeek.values()[(dayOfWeek + 6) % 7];

  }

  /**
   * The Util class to represent Doctor Timing.
   */
  public static class DoctorTiming {

    /** The day of week. */
    private WeeklyAvailability.DayOfWeek dayOfWeek;

    /** The start time. */
    private String startTime;

    /** The end time. */
    private String endTime;

    /**
     * Instantiates a new doctor timing.
     *
     * @param dayOfWeek
     *          the day of week
     * @param startTime
     *          the start time
     * @param endTime
     *          the end time
     */
    public DoctorTiming(WeeklyAvailability.DayOfWeek dayOfWeek, String startTime, String endTime) {
      this.dayOfWeek = dayOfWeek;
      this.startTime = startTime;
      this.endTime = endTime;
    }

    /**
     * Instantiates a new doctor timing.
     *
     * @param dayOfWeek
     *          the day of week
     * @param startTime
     *          the start time
     * @param endTime
     *          the end time
     */
    public DoctorTiming(int dayOfWeek, String startTime, String endTime) {
      this.dayOfWeek = getDayOfWeekEnum(dayOfWeek);
      this.startTime = startTime;
      this.endTime = endTime;
    }

    /**
     * Gets the day of week.
     *
     * @return the day of week
     */
    public WeeklyAvailability.DayOfWeek getDayOfWeek() {
      return dayOfWeek;
    }

    /**
     * Gets the start time.
     *
     * @return the start time
     */
    public String getStartTime() {
      return startTime;
    }

    /**
     * Gets the end time.
     *
     * @return the end time
     */
    public String getEndTime() {
      return endTime;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
      return "DoctorTiming [dayOfWeek=" + dayOfWeek + ", startTime=" + startTime + ", endTime="
          + endTime + "]";
    }
  }

  /**
   * Maps the SDK error to the error to show to user.
   *
   * @param sdkErrorCode
   *          the sdk error code
   * @return the error message
   */
  public static String getErrorMessage(SDKErrorCode sdkErrorCode) {
    String errorKey = null;
    if (sdkErrorCode == SDKErrorCode.INTEGRATION_INIT_FAILED) {
      errorKey = "exception.integration.book.init.failed";

    } else if (sdkErrorCode == SDKErrorCode.INTEGRATION_NOT_INITIALIZED) {
      errorKey = "exception.integration.book.not.initialized";

    } else {
      errorKey = "exception.internal.error";
    }
    MessageUtil messageutil = ApplicationContextProvider.getBean(MessageUtil.class);
    return messageutil.getMessage(errorKey, null);
  }

  /**
   * Maps Insta Equivalent of the Appointment status to Practo.
   *
   * @param appointmentStatus
   *          the appointment status
   * @return the appointment status
   */
  public static AppointmentStatus getAppointmentStatus(String appointmentStatus) {
    if (appointmentStatus.equalsIgnoreCase(APPOINTMENT_CANCELLED)) {
      return AppointmentStatus.CANCELLED;
    } else {
      return AppointmentStatus.CONFIRMED;
    }
  }

  /**
   * Maps Practo Appointment status to Insta.
   *
   * @param appointmentStatus
   *          the appointment status
   * @return the appointment status
   */
  public static String getAppointmentStatus(AppointmentStatus appointmentStatus) {
    if (appointmentStatus == AppointmentStatus.CONFIRMED) {
      return APPOINTMENT_CONFIRMED;
    } else {
      return APPOINTMENT_CANCELLED;
    }
  }

  /**
   * Inits the to practo.
   */
  private static void initToPracto() {
    try {
      InstaIntegrationDao integrationDao = new InstaIntegrationDao();
      BasicDynaBean integrationRecord = integrationDao
          .getActiveBean(BookSDKUtil.PRACTO_BOOK_INTEGRATION);
      Integer integrationId = (Integer) integrationRecord.get("integration_id");
      String applicationID = (String) integrationRecord.get("application_id");
      String applicationSecret = (String) integrationRecord.get("application_secret");
      String agentHost = (String) integrationRecord.get("agent_host");
      Integer agentPort = (Integer) integrationRecord.get("agent_port");
      Map<String, Object> filterMap = new HashMap<>();
      filterMap.put("integration_id", integrationId);
      List<BasicDynaBean> centerIntegrationPrefs = integrationDao
          .getIntegrationDetailForAllCenter(integrationId);
      for (BasicDynaBean centerBean : centerIntegrationPrefs) {
        String establishmentKey = (String) centerBean.get("establishment_key");
        if (establishmentKey == null || establishmentKey.trim().isEmpty()) {
          continue;
        }
        Integer centerId = (Integer) centerBean.get("center_id");
        BookSDKCallback bookSDKCallback = ApplicationContextProvider.getApplicationContext()
            .getBean(BookSDKCallback.class, RequestContext.getSchema(), centerId);
        try {
          BookSDKUtil.init(applicationID, applicationSecret, establishmentKey, bookSDKCallback,
              agentHost, agentPort);
          logger.info(String.format("Connect to Practo for center: [%s] is successfull", centerId));
        } catch (SDKException ex) {
          logger.error(String.format("Failed to connect to Practo for center:[%s]", centerId), ex);
        }
      }
    } catch (SQLException exception) {
      logger.error("SQL Exception while making connection", exception);
    }
  }

}