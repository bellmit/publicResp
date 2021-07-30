package com.insta.hms.core.scheduler;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.security.SecurityService;
import com.insta.hms.common.validation.NotNullRule;
import com.insta.hms.common.validation.PhoneNumberRule;
import com.insta.hms.common.validation.RuleSetValidator;
import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.exception.NestableValidationException;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.mdm.resourceavailability.ResourceAvailabilityService;
import com.insta.hms.mdm.resourceoverride.ResourceOverrideService;
import com.insta.hms.mdm.sponsors.SponsorTypeService;
import com.insta.hms.mdm.tpas.TpaService;



import org.apache.commons.beanutils.BasicDynaBean;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.sql.Date;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO: Auto-generated Javadoc
/**
 * The Class AppointmentValidator.
 */
@Component
public class AppointmentValidator extends RuleSetValidator {

  /** The logger. */
  private Logger logger = LoggerFactory.getLogger(this.getClass());

  /** The appointment repository. */
  @LazyAutowired
  private AppointmentRepository appointmentRepository;

  /** The res overrides service. */
  @LazyAutowired
  private ResourceOverrideService resOverridesService;

  /** The res availability service. */
  @LazyAutowired
  private ResourceAvailabilityService resAvailabilityService;

  /** The generic preferences service. */
  @LazyAutowired
  private GenericPreferencesService genericPreferencesService;

  /** The tpa service. */
  @LazyAutowired
  private TpaService tpaService;

  /** The sponsor type service. */
  @LazyAutowired
  private SponsorTypeService sponsorTypeService;

  /** The phone number rule. */
  @Autowired
  PhoneNumberRule phoneNumberRule;

  /** The security service. */
  @Autowired
  private SecurityService securityService;

  /** The Scheduler service. */
  @LazyAutowired
  private SchedulerService schedulerService;

  /** The not null rule. */
  private NotNullRule notNullRule = new NotNullRule();

  /** The parent key. */
  private String parentKey = null;

  /**
   * Gets the parent key.
   *
   * @return the parent key
   */
  public String getParentKey() {
    return parentKey;
  }

  /**
   * Sets the parent key.
   *
   * @param parentKey
   *          the new parent key
   */
  public void setParentKey(String parentKey) {
    this.parentKey = parentKey;
  }

  /**
   * This method sets rule set map of validator.
   *
   * @param bean
   *          the new rule set map
   */
  public void setRuleSetMap(BasicDynaBean bean) {
    Map<ValidationRule, String[]> value = new HashMap<ValidationRule, String[]>();

    BasicDynaBean genericPreferences = genericPreferencesService.getAllPreferences();

    /*
     * Map<String, String> actionRightsMap = (Map<String, String>) securityService
     * .getSecurityAttributes().get("actionRightsMap");
     */

    // add patient phone rule only when preference is enabled.
    boolean hasMobile = bean.get("patient_contact") != null
        && !((String) bean.get("patient_contact")).isEmpty();
    value.put(notNullRule, new String[] { "patient_contact" });
    if (hasMobile && ((String) genericPreferences.get("mobile_number_validation")).equals("Y")) {
      String[] fields = { "patient_contact" };
      value.put(phoneNumberRule, fields);
    }

    this.ruleSetMap.put(DEFAULT_RULESET_NAME, value);
  }

  /**
   * This method validates rules.
   *
   * @param bean
   *          the bean
   * @return true, if successful
   */
  @Override
  public boolean validate(BasicDynaBean bean) {
    Map<ValidationRule, String[]> ruleSet = getDefaultRuleSet();
    boolean result = true;
    if (null != ruleSet && !ruleSet.isEmpty()) {
      result = applyRuleSet(ruleSet, bean);
    }
    if (!result) {
      throwErrors(parentKey);
    }
    return result;
  }

  /**
   * This method throws validation errors.
   *
   * @param key
   *          the key
   * @return true, if successful
   */
  private boolean throwErrors(String key) {
    ValidationErrorMap validationErrorMap = getErrors();
    ValidationException ex = new ValidationException(validationErrorMap);
    Map<String, Object> nestedException = new HashMap<String, Object>();
    nestedException.put(key, ex.getErrors());
    throw new NestableValidationException(nestedException);
  }

  /**
   * Validate within same day.
   *
   * @param apptTime
   *          the appt time
   * @param apptEndTime
   *          the appt end time
   * @param errors
   *          the errors
   * @return true, if successful
   */
  public boolean validateWithinSameDay(Timestamp apptTime, Timestamp apptEndTime,
      ValidationErrorMap errors) {
    Date startDate = new Date(apptTime.getTime());
    Date endDate = new Date(apptEndTime.getTime());
    if (!startDate.toString().equals(endDate.toString())) {
      errors.addError("duration", "exception.scheduler.appointment.mismatch.dates");
      return false;
    }
    return true;
  }

  /**
   * Validate if prim res exists.
   *
   * @param category
   *          the category
   * @param primResId
   *          the prim res id
   * @param centerId
   *          the center id
   * @return the boolean
   */
  public Boolean validateIfPrimResExists(AppointmentCategory category, String primResId,
      int centerId) {
    BasicDynaBean resBean = null;
    try {
      resBean = appointmentRepository.validateIfResExists(category.validatePrimRes(), primResId);
      if (resBean == null) {
        return false;
      }
      // this needs to be handled properly later. code needs to be moved to appCat
      if (category.getCategory().equalsIgnoreCase("SNP")) {
        if ((int) resBean.get("center_id") == centerId || (int) resBean.get("center_id") == 0) {
          return true;
        }
        return false;
      }
      return true;
    } catch (DataIntegrityViolationException exp) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("primary_resource_id", "exception.scheduler.invalid.resource");
      ValidationException ex = new ValidationException(errorMap);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      nestedException.put("appointment", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
  }

  /**
   * Validate if sec res exists.
   *
   * @param category
   *          the category
   * @param secResId
   *          the sec res id
   * @param centerId
   *          the center id
   * @return the boolean
   */
  public Boolean validateIfSecResExists(AppointmentCategory category, String secResId,
      int centerId) {
    BasicDynaBean resBean = null;
    try {
      resBean = appointmentRepository.validateIfResExists(category.validateSecRes(), secResId);
      return resBean != null;
    } catch (DataIntegrityViolationException exp) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("secondary_resource_id", "exception.scheduler.invalid.resource");
      ValidationException ex = new ValidationException(errorMap);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      nestedException.put("appointment", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
  }

  /**
   * Validate if slot overbooked.
   *
   * @param category
   *          the category
   * @param resId
   *          the res id
   * @param resType
   *          the res type
   * @param apptTime
   *          the appt time
   * @param apptEndTime
   *          the appt end time
   * @param errors
   *          the errors
   * @param key
   *          the key
   * @param appId
   *          the app id
   * @return true, if successful
   */
  public boolean validateIfSlotOverbooked(AppointmentCategory category, String resId,
      String resType, Timestamp apptTime, Timestamp apptEndTime, ValidationErrorMap errors,
      String key, String appId) {
    if (key == null) {
      key = resId;
    }

    Integer overbookLimit = category.getResourceOverbookLimit(resId, resType);
    int overbookCount = appointmentRepository.getOverbookCountForResource(apptTime, apptEndTime,
        resId, resType, appId);
    if (overbookLimit != null && overbookCount > overbookLimit) {
      // TODO: return overbook limit reached error
      String resName = category.getResourceName(resId, resType);
      errors.addError(key, "exception.scheduler.slotoverbooked", Arrays.asList(resName));
      return false;
    }
    return true;
  }

  /**
   * Validate if appointment exists for patient.
   *
   * @param apptTime          the appt time
   * @param apptEndTime          the appt end time
   * @param apptId          the appt id
   * @param mrNo          the mr no
   * @param patientName          the patient name
   * @param patientContact          the patient contact
   * @param errors          the errors
   * @param contactId the contact id
   * @return true, if successful
   */
  public boolean validateIfAppointmentExistsForPatient(Timestamp apptTime, Timestamp apptEndTime,
      String apptId, String mrNo, String patientName, String patientContact,
      ValidationErrorMap errors,Integer contactId) {
    List<BasicDynaBean> apptList = appointmentRepository.appointmentExists(apptTime, apptEndTime,
        apptId, mrNo, patientName, patientContact, contactId);
    if (apptList != null && apptList.size() > 0) {
      // TODO: return Other appointment exists for patient during this apptTime
      errors.addError("patient_name", "exception.scheduler.appointmentexistsforpatient");
      return false;
    }
    return true;
  }

  /**
   * Validate if appointment exists for patient for same package.
   *
   * @param apptTime
   *          the appt time
   * @param apptEndTime
   *          the appt end time
   * @param apptId
   *          the appt id
   * @param mrNo
   *          the mr no
   * @param patientName
   *          the patient name
   * @param patientContact
   *          the patient contact
   * @param errors
   *          the errors
   * @param packageId
   *          the package id
   * @param groupId
   *          the group id
   * @return true, if successful
   */
  // TODO : refactor for same method in the next release
  public boolean validateIfAppointmentExistsForPatientForSamePackage(Timestamp apptTime,
      Timestamp apptEndTime, String apptId, String mrNo, String patientName, String patientContact,
      ValidationErrorMap errors, int packageId, int groupId) {
    List<BasicDynaBean> apptList = appointmentRepository.appointmentExistsForSamePackage(apptTime,
        apptEndTime, apptId, mrNo, patientName, patientContact, packageId, groupId);
    if (apptList != null && apptList.size() > 0) {
      // TODO: return Other appointment exists for patient during this apptTime
      errors.addError("patient_name", "exception.scheduler.appointmentexistsforpatient");
      return false;
    }
    return true;
  }

  /**
   * Gets the resource scheduele.
   *
   * @param resId
   *          the res id
   * @param resType
   *          the res type
   * @param apptTime
   *          the appt time
   * @param apptEndTime
   *          the appt end time
   * @param category
   *          the category
   * @param centerId
   *          the center id
   * @return the resource scheduele
   */
  public List getResourceScheduele(String resId, String resType, Timestamp apptTime,
      Timestamp apptEndTime, AppointmentCategory category, int centerId) {
    List resourceAvailList = new ArrayList();
    if (resType.endsWith("DOC")) {
      resType = "DOC";
    }
    resourceAvailList = resOverridesService.getResourceOverrides(resId, resType,
        new Date(apptTime.getTime()), new Date(apptEndTime.getTime()), null, null);
    if (resourceAvailList != null && resourceAvailList.size() < 1) {
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(new Date(apptTime.getTime()));
      int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
      resourceAvailList = resAvailabilityService.getDefaultResourceAvailabilities(resId, dayOfWeek,
          resType, null, null);
      if (resourceAvailList != null && resourceAvailList.size() < 1) {
        resourceAvailList = resAvailabilityService.getDefaultResourceAvailabilities("*", dayOfWeek,
            resType, null, null);
      }
    }
    Integer centersIncDefault = (Integer) genericPreferencesService.getAllPreferences().get(
        "max_centers_inc_default");
    if (resourceAvailList != null && resourceAvailList.size() > 0) {
      if (centersIncDefault > 1) {
        resourceAvailList = category.filterVisitTimingsByCenter(resourceAvailList, centerId);
      }
    }
    return resourceAvailList;
  }

  /**
   * Validate resources availability.
   *
   * @param category the category
   * @param apptTime the appt time
   * @param apptEndTime the appt end time
   * @param apptDate the appt date
   * @param resType the res type
   * @param resId the res id
   * @param centerId the center id
   * @param errors the errors
   * @param key the key
   * @param processedMap the processed map
   * @return true, if successful
   */
  public boolean validateResourcesAvailability(AppointmentCategory category, Timestamp apptTime,
      Timestamp apptEndTime, String apptDate, String resType, String resId, int centerId,
      ValidationErrorMap errors, String key, Map processedMap) {
    return validateResourcesAvailability(category, apptTime, apptEndTime, apptDate, resType, resId,
        centerId, errors, key, processedMap, null);

  }
  
  /**
   * Validate resources availability.
   *
   * @param category          the category
   * @param apptTime          the appt time
   * @param apptEndTime          the appt end time
   * @param apptDate          the appt date
   * @param resType          the res type
   * @param resId          the res id
   * @param centerId          the center id
   * @param errors          the errors
   * @param key          the key
   * @param processedMap          the processed map
   * @param visitMode the visit mode
   * @return true, if successful
   */
  public boolean validateResourcesAvailability(AppointmentCategory category, Timestamp apptTime,
      Timestamp apptEndTime, String apptDate, String resType, String resId, int centerId,
      ValidationErrorMap errors, String key, Map processedMap, String visitMode) {
    List resourceAvailList = new ArrayList();
    if (resType.endsWith("DOC")) {
      resType = "DOC";
    }
    if (resType.equalsIgnoreCase("LABTECH")) {
      resType = "DOC";
    }

    if (processedMap != null
        && processedMap.get(resType + new Date(apptTime.getTime()).toString() + resId) != null) {
      resourceAvailList = (List) processedMap.get(resType + new Date(apptTime.getTime()).toString()
          + resId);
    } else {
      resourceAvailList = resOverridesService.getResourceOverrides(resId, resType, new Date(
          apptTime.getTime()), new Date(apptEndTime.getTime()), null, null);
      if (resourceAvailList != null && resourceAvailList.size() < 1) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(apptTime.getTime()));
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        resourceAvailList = resAvailabilityService.getDefaultResourceAvailabilities(resId,
            dayOfWeek, resType, null, null);
        if (resourceAvailList != null && resourceAvailList.size() < 1) {
          resourceAvailList = resAvailabilityService.getDefaultResourceAvailabilities("*",
              dayOfWeek, resType, null, null);
        }
      }
      if (processedMap != null) {
        processedMap.put(resType + new Date(apptTime.getTime()).toString() + resId,
            resourceAvailList);
      }
    }

    if (key == null) {
      key = resId;
    }
    BasicDynaBean resourceBaen = null;
    String err = null;
    boolean resAvailable = true;
    Integer centersIncDefault = (Integer) genericPreferencesService.getAllPreferences().get(
        "max_centers_inc_default");
    Timestamp startAvailTime = null;
    Timestamp endAvailTime = null;
    if (resourceAvailList != null && resourceAvailList.size() > 0) {
      visitMode = (StringUtils.isEmpty(visitMode) || !visitMode.equalsIgnoreCase("O")) ? "I"
          : visitMode;
      resourceAvailList = resAvailabilityService.filterVisitTimingsByVisitMode(resourceAvailList,
          visitMode);
      if (centersIncDefault > 1) {
        resourceAvailList = category.filterVisitTimingsByCenter(resourceAvailList, centerId);
      }
    }
    for (int j = 0; j < resourceAvailList.size(); j++) {
      resourceBaen = (BasicDynaBean) resourceAvailList.get(j);
      // check next available slot is same as appointment center slot
      /*
       * if(centersIncDefault > 1){ if (resourceBaen.get("availability_status").equals("A")) { Time
       * fromTime = (java.sql.Time)resourceBaen.get("from_time"); SimpleDateFormat sdf = new
       * SimpleDateFormat("HH:mm:ss"); String from_time = sdf.format(fromTime); DateUtil dateUtil =
       * new DateUtil(); try { startAvailTime = new
       * Timestamp(dateUtil.getTimeStampFormatterSecs().parse(apptDate + " " +
       * from_time).getTime()); } catch (ParseException e) { errors.addError(key,
       * "exception.scheduler.invaliddatetime"); return false; } Time toTime =
       * (java.sql.Time)resourceBaen.get("to_time"); String to_time = sdf.format(toTime); try {
       * endAvailTime = new Timestamp(dateUtil.getTimeStampFormatter().parse(apptDate + " " +
       * to_time).getTime()); } catch (ParseException e) { errors.addError(key,
       * "exception.scheduler.invaliddatetime"); return false; } Integer dbAvailCenterId = (Integer)
       * resourceBaen.get("center_id"); if(!dbAvailCenterId.equals(0)){
       * 
       * if(centerId != dbAvailCenterId){ if ((apptTime.getTime() <= startAvailTime.getTime() &&
       * apptEndTime.getTime() > startAvailTime.getTime()) || (apptTime.getTime() >=
       * startAvailTime.getTime() && apptTime.getTime() < endAvailTime.getTime())){ resAvailable =
       * false; //err = "Not enough vacant appointment slots available for " + " " + resourceName;
       * String resName = category.getResourceName(resId, resType); errors.addError(key,
       * "exception.scheduler.resourceunavailable", Arrays.asList(resName)); break; } } } } }
       */

      if (resourceBaen.get("availability_status").equals("N")) {
        Time fromTime = (java.sql.Time) resourceBaen.get("from_time");
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String fromTimeStr = sdf.format(fromTime);
        DateUtil dateUtil = new DateUtil();
        try {
          startAvailTime = new Timestamp(dateUtil.getTimeStampFormatterSecs()
              .parse(apptDate + " " + fromTimeStr).getTime());
        } catch (ParseException exp) {
          errors.addError(key, "exception.scheduler.invaliddatetime");
          return false;
        }
        Time toTime = (java.sql.Time) resourceBaen.get("to_time");
        String toTimeStr = sdf.format(toTime);
        try {
          endAvailTime = new Timestamp(dateUtil.getTimeStampFormatter()
              .parse(apptDate + " " + toTimeStr).getTime());
        } catch (ParseException exp) {
          errors.addError(key, "exception.scheduler.invaliddatetime");
          return false;
        }

        Map<String, String> actionRightsMap = (Map<String, String>) securityService
            .getSecurityAttributes().get("actionRightsMap");
        if ((apptTime.getTime() <= startAvailTime.getTime() 
            && apptEndTime.getTime() > startAvailTime.getTime())
            || (apptTime.getTime() >= startAvailTime.getTime() && apptTime.getTime() < endAvailTime
                .getTime())) {
          resAvailable = false;
          // err = "Not enough vacant appointment slots available for " + " " + resourceName;
          String resName = category.getResourceName(resId, resType);
          errors.addError(key, "exception.scheduler.resourceunavailable", Arrays.asList(resName));
          break;
        }
      }
    }
    if (resourceAvailList == null || resourceAvailList.size() == 0) {
      resAvailable = false;
    }
    return resAvailable;
  }

  /**
   * Validate member id.
   *
   * @param additionalInfo
   *          the additional info
   * @param validationErrors
   *          the validation errors
   * @return true, if successful
   */
  public boolean validateMemberId(Map<String, Object> additionalInfo,
      ValidationErrorMap validationErrors) {
    boolean success = true;
    String memberId = (String) additionalInfo.get("member_id");
    String sponsorId = (String) additionalInfo.get("primary_sponsor_id");
    if (null != sponsorId && !sponsorId.equals("")) {
      BasicDynaBean sponsor = tpaService.getDetails(sponsorId);
      Integer sponsorTypeId = (Integer) sponsor.get("sponsor_type_id");
      Map<String, Object> keys = new HashMap();
      keys.put("sponsor_type_id", sponsorTypeId);
      BasicDynaBean sponsorTypeBean = sponsorTypeService.findByPk(keys);
      String memberIdPattern = (String) sponsor.get("member_id_pattern");
      if (null != memberId && !memberId.isEmpty() && memberIdPattern != null
          && !memberIdPattern.isEmpty()) {
        String regExPattern = patternToRegEx(memberIdPattern);
        Pattern pattern = Pattern.compile(regExPattern);
        Matcher matcher = pattern.matcher(memberId);
        if (!matcher.find()) {
          validationErrors.addError("member_id",
              "ui.message.member.id.pattern.mismatch.double.placeholder",
              Arrays.asList((String) sponsorTypeBean.get("member_id_label"), memberIdPattern));
          success = false;
        }
      }
    }
    return success;
  }

  /**
   * Pattern to reg ex.
   *
   * @param pattern
   *          the pattern
   * @return the string
   */
  private String patternToRegEx(String pattern) {
    return "^"
        + pattern.replaceAll("x", "[A-Za-z]").replaceAll("X", "[A-Za-z]").replaceAll("9", "[0-9]")
        + "$";
  }

  /**
   * Validate update appointment status params.
   *
   * @param updateAppList
   *          the update app list
   * @param errors
   *          the errors
   * @return true, if successful
   */
  public boolean validateUpdateAppointmentStatusParams(ArrayList updateAppList,
      ValidationErrorMap errors) {

    if (updateAppList == null || updateAppList.isEmpty() || errors == null) {
      return false;
    }
    boolean success = true;
    for (int i = 0; i < updateAppList.size(); i++) {
      Map app = (Map) updateAppList.get(i);
      String appointmentId = String.valueOf(app.get("appointment_id"));
      String appointmentStatus = (String) app.get("appointment_status");
      String cancelReason = (String) app.get("cancel_reason");
      String cancelType = (String) app.get("cancel_type");
      if (appointmentId.equals("null") || "".equals(appointmentId)) {
        errors.addError("appointment_id", "scheduler.validation.error.appointmentId");
        return false;
      }
      if (appointmentStatus == null || "".equals(appointmentStatus)) {
        errors.addError("appointment_status", "scheduler.validation.error.appointmentStatus");
        return false;
      }
      BasicDynaBean appointmentBean = appointmentRepository.findByKey("appointment_id",
          Integer.parseInt(appointmentId));
      if (appointmentBean == null) {
        errors.addError("appointment_id", "scheduler.validation.error.validAppointmentId");
        success = false;
      }
      if (appointmentBean != null) {
        String previousStatus = (String) appointmentBean.get("appointment_status");
        if (appointmentStatus.equals("Completed") && previousStatus.equals(appointmentStatus)) {
          success = true;
        } else {
          if (previousStatus.equalsIgnoreCase("Cancel")
              || previousStatus.equalsIgnoreCase("Noshow")) {
            errors.addError("appointment_status", "scheduler.validation.error.appointmentcanceled");
            success = false;
          }
          if (previousStatus.equals("Arrived") && !appointmentStatus.equals("Completed")) {
            errors.addError("appointment_status", "scheduler.validation.error.Arrived");
            success = false;
          }
          if (previousStatus.equals("Completed")) {
            errors.addError("appointment_status", "scheduler.validation.error.completed");
            success = false;
          }
          if ((previousStatus.equals("Booked") || previousStatus.equals("Confirmed"))
              && appointmentStatus.equals("Completed")) {
            errors.addError("appointment_status", "scheduler.validation.error.notforcompleted");
            success = false;
          }
        }
      }
      if (appointmentStatus != null
          && !Arrays.asList("Cancel", "Confirmed", "Booked", "Noshow", "Completed", "Arrived")
              .contains(appointmentStatus)) {
        errors.addError("appointment_status", "scheduler.validation.error.invalidStatus");
        success = false;
      }
      if (appointmentStatus != null && appointmentStatus.equalsIgnoreCase("Cancel")) {
        if (cancelType == null || "".equals(cancelType.trim())) {
          errors.addError("appointment_status", "scheduler.validation.error.cancelType");
          errors.addError("cancel_type", "scheduler.validation.error.cancelType");
          success = false;
        }
        if (cancelType != null
            && !Arrays.asList("patient", "doctor", "other").contains(cancelType.toLowerCase())) {
          errors.addError("appointment_status", "scheduler.validation.error.cancelType");
          errors.addError("cancel_type", "scheduler.validation.error.cancelType");
          success = false;
        }
        if (cancelReason == null || "".equals(cancelReason.trim())) {
          errors.addError("appointment_status", "scheduler.validation.error.cancelReason");
          errors.addError("cancel_reason", "scheduler.validation.error.cancelReason");
          success = false;
        }
      }
    }
    return success;
  }

  /**
   * Validate additional resources params.
   *
   * @param params
   *          the params
   * @param errors
   *          the errors
   * @return true, if successful
   */
  public boolean validateAdditionalResourcesParams(Map<String, String[]> params,
      ValidationErrorMap errors) {
    if (params == null || params.isEmpty() || errors == null) {
      return false;
    }
    boolean success = true;
    String[] resTypes = ((String[]) params.get("res_types"));
    
    String[] category = params.get("res_sch_category");
    if (resTypes == null || resTypes[0].equals("")) {
      errors.addError("res_types", "scheduler.validation.error.resourType");
      success = false;
    }
    if (category == null || category[0].equals("")) {
      errors.addError("res_sch_category", "scheduler.validation.error.schCategory");
      success = false;
    }
    if (category != null && !Arrays.asList("DOC", "OPE", "SNP", "DIA").contains(category[0])) {
      errors.addError("res_sch_category", "scheduler.validation.error.validcategory");
      success = false;
    }
    String[] apptDate = (String[]) params.get("date");
    if (apptDate == null || apptDate[0].equals("")) {
      errors.addError("date", "scheduler.validation.error.appDate");
      success = false;
    }
    String[] centerId = (String[]) params.get("center_id");
    if (centerId != null) {
      try {
        Integer.parseInt(params.get("center_id")[0]);
      } catch (Exception exp) {
        errors.addError("center_id", "scheduler.validation.error.center_id.integer");
        success = false;
      }
    }
    if (apptDate != null && !apptDate[0].equals("")) {
      java.util.Date dt = null;
      try {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy");
        dt = dateFormatter.parse(apptDate[0]);
        if (!apptDate[0].equals(dateFormatter.format(dt))) {
          dt = null;
        }
        if (dt == null) {
          errors.addError("date", "js.common.message.date.incorrect.format");
          success = false;
        }
      } catch (ParseException exp) {
        errors.addError("date", "js.common.message.date.incorrect.format");
        success = false;
      }
    }
    String[] slotTime = (String[]) params.get("slot_time");
    if (slotTime == null || slotTime[0].equals("")) {
      errors.addError("slot_time", "scheduler.validation.error.appSlotTime");
      success = false;
    }
    String[] duration = (String[]) params.get("duration");
    if (duration == null || duration[0].equals("")) {
      errors.addError("duration", "scheduler.validation.error.duration");
      success = false;
    }
    if (duration != null && !duration[0].equals("")) {
      try {
        Integer.parseInt(params.get("duration")[0]);
      } catch (Exception exp) {
        errors.addError("duration", "scheduler.validation.error.duration.integer");
        success = false;
      }
    }
    return success;
  }

  /**
   * Validat secondary resources schedule params.
   *
   * @param params
   *          the params
   * @param errors
   *          the errors
   * @return true, if successful
   */
  public boolean validatSecondaryResourcesScheduleParams(Map<String, String[]> params,
      ValidationErrorMap errors) {
    if (params == null || params.isEmpty() || errors == null) {
      return false;
    }
    boolean success = true;
    String[] resSchCategory = params.get("res_sch_category");
    String[] resources = params.get("resources");    
    if (resSchCategory == null || resSchCategory[0].equals("")) {
      errors.addError("res_sch_category", "scheduler.validation.error.schCategory");
      success = false;
    }
    if (resSchCategory != null
        && !Arrays.asList("DOC", "OPE", "SNP", "DIA").contains(resSchCategory[0])) {
      errors.addError("res_sch_category", "scheduler.validation.error.validcategory");
      success = false;
    }
    if (resources == null || resources[0].equals("")) {
      errors.addError("resources", "scheduler.validation.error.resources");
      success = false;
    }
    String[] startDate = params.get("start_date");
    if (startDate == null || startDate[0].equals("")) {
      errors.addError("start_date", "scheduler.validation.error.start_date");
      success = false;
    }
    if (startDate != null && !startDate[0].equals("")) {
      java.util.Date dt = null;
      try {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy");
        dt = dateFormatter.parse(startDate[0]);
        if (!startDate[0].equals(dateFormatter.format(dt))) {
          dt = null;
        }
        if (dt == null) {
          errors.addError("start_date", "js.common.message.date.incorrect.format");
          success = false;
        }
      } catch (ParseException exp) {
        errors.addError("start_date", "js.common.message.date.incorrect.format");
        success = false;
      }
    }
    String[] endDate = params.get("end_date");
    if (endDate == null || endDate[0].equals("")) {
      errors.addError("end_date", "scheduler.validation.error.end_date");
      success = false;
    }
    if (endDate != null && !endDate[0].equals("")) {
      java.util.Date edt = null;
      try {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy");
        edt = dateFormatter.parse(endDate[0]);
        if (!endDate[0].equals(dateFormatter.format(edt))) {
          edt = null;
        }
        if (edt == null) {
          errors.addError("end_date", "js.common.message.date.incorrect.format");
          success = false;
        }
      } catch (ParseException exp) {
        errors.addError("end_date", "js.common.message.date.incorrect.format");
        success = false;
      }
    }
    return success;
  }

  /**
   * Validatget appointment details params.
   *
   * @param params
   *          the params
   * @param errors
   *          the errors
   * @return true, if successful
   */
  public boolean validatgetAppointmentDetailsParams(Map<String, String[]> params,
      ValidationErrorMap errors) {
    if (params == null || params.isEmpty() || errors == null) {
      return false;
    }
    boolean success = true;
    String[] appointmentId = params.get("appointment_id");
    String[] resSchCategory = params.get("res_sch_category");
    if (appointmentId == null || "".equals(appointmentId[0])) {
      errors.addError("appointment_id", "scheduler.validation.error.appointmentId");
      success = false;
    }
    if (resSchCategory == null || resSchCategory[0].equals("")) {
      errors.addError("res_sch_category", "scheduler.validation.error.schCategory");
      success = false;
    }
    if (resSchCategory != null
        && !Arrays.asList("DOC", "OPE", "SNP", "DIA").contains(resSchCategory[0])) {
      errors.addError("res_sch_category", "scheduler.validation.error.validcategory");
      success = false;
    }
    return success;
  }

  /**
   * Validate get previous appt.
   *
   * @param params
   *          the params
   * @param errors
   *          the errors
   * @return true, if successful
   */
  public boolean validateGetPreviousAppt(Map<String, String[]> params, ValidationErrorMap errors) {
    if (params == null || params.isEmpty() || errors == null) {
      return false;
    }
    boolean success = true;
    String[] mrNo = params.get("mr_no");
    if (mrNo == null || mrNo[0].equals("")) {
      errors.addError("mr_no", "scheduler.validation.error.mr_no");
      success = false;
    }
    return success;
  }

  /**
   * Validate if user has acl to overbook an appointment.
   *
   * @param resourceId resource id
   * @param appointmentTime appointmentTime
   * @return boolean value true or false
   */
  public boolean isUSerAllowedToOverBookAppt(String resourceId, Timestamp appointmentTime,
      String category) {
    return schedulerService.isUserAllowedToBookAppts(resourceId, appointmentTime, category);
  }
 
}
