package com.insta.hms.core.patient.header;

import com.bob.hms.common.Constants;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.PhoneNumberUtil;
import com.insta.hms.common.StringUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.validation.ActionRightsACLRule;
import com.insta.hms.common.validation.UrlRightsAclRule;
import com.insta.hms.core.patient.PatientDetailsRepository;
import com.insta.hms.core.patient.PatientDetailsService;
import com.insta.hms.core.patient.registration.PatientRegistrationRepository;
import com.insta.hms.exception.AccessDeniedException;
import com.insta.hms.exception.ConversionException;
import com.insta.hms.exception.EntityNotFoundException;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.integration.configuration.InterfaceEventMappingService;
import com.insta.hms.integration.hl7.message.v23.ADTService;
import com.insta.hms.mdm.centers.CenterService;
import com.insta.hms.mdm.patientheaderpreferences.PatientHeaderPreferencesService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This is service for patient header related end points.
 *
 * @author sainathbatthala
 */
@Service
public class PatientHeaderService {

  /** The preferences service. */
  @Autowired
  PatientHeaderPreferencesService preferencesService;

  /** The patient details service. */
  @Autowired
  PatientDetailsService patientDetailsService;

  /** The patient header validator. */
  @Autowired
  PatientHeaderValidator patientHeaderValidator;

  /** The acl rule. */
  @Autowired
  ActionRightsACLRule aclRule;

  /** The url acl rule. */
  @Autowired
  UrlRightsAclRule urlAclRule;

  /** The center service. */
  @LazyAutowired
  private CenterService centerService;
  
  /** The adt service. */
  @LazyAutowired
  private ADTService adtService;
  
  /** The patient registration repository. */
  @LazyAutowired
  private PatientRegistrationRepository patientRegistrationRepository;

  /** The patient detais repository. */
  @LazyAutowired
  private PatientDetailsRepository patientDetailsRepository ;
  
  @LazyAutowired
  private InterfaceEventMappingService interfaceEventMappingService;

  /**
   * This method gets patient details.
   *
   * @param advanced
   *          the advanced
   * @param mrNo
   *          the mr no
   * @param visitId
   *          the visit id
   * @param appointmentId
   *          the appointment id
   * @param headerPreferences
   *          the header preferences
   * @param visitType
   *          the visit type
   * @param dataCategory
   *          the data category
   * @return patient details map
   * @throws ParseException
   *           the parse exception
   */
  public Map<String, Object> getPatientDetails(String advanced, String mrNo, String visitId,
      String appointmentId, String headerPreferences, String[] visitType, String[] dataCategory)
      throws ParseException {

    // parameter validation check
    if (advanced.equals("Y")) {
      if ((mrNo == null || mrNo.isEmpty()) && (visitId == null || visitId.isEmpty())) {
        ValidationErrorMap errorMap = new ValidationErrorMap();
        errorMap.addError("mr_no, visit_id", "exception.both.parameters.notnull");
        ValidationException ex = new ValidationException(errorMap);
        throw ex;
      }

      if (headerPreferences == null
          || !(headerPreferences.equals("Y") || headerPreferences.equals("N"))) {
        ValidationErrorMap errorMap = new ValidationErrorMap();
        errorMap.addError("header_preferences", "exception.invalid.value",
            Arrays.asList(headerPreferences));
        ValidationException ex = new ValidationException(errorMap);
        throw ex;
      }

      if (!isValidVisitType(visitType)) {
        ValidationErrorMap errorMap = new ValidationErrorMap();
        errorMap.addError("visit_type", "exception.invalid.value",
            Arrays.asList(StringUtil.prettyName("visit_type")));
        ValidationException ex = new ValidationException(errorMap);
        throw ex;
      }

      if (!isValidDataCategory(dataCategory)) {
        ValidationErrorMap errorMap = new ValidationErrorMap();
        errorMap.addError("data_category", "exception.invalid.value",
            Arrays.asList(StringUtil.prettyName("data_category")));
        ValidationException ex = new ValidationException(errorMap);
        throw ex;
      }

    } else {
      if ((mrNo == null || mrNo.isEmpty()) && (appointmentId == null || appointmentId.isEmpty())) {
        ValidationErrorMap errorMap = new ValidationErrorMap();
        errorMap.addError("mr_no, appointment_id", "exception.both.parameters.notnull");
        ValidationException ex = new ValidationException(errorMap);
        throw ex;
      }
    }

    Map<String, Object> ret = new HashMap<String, Object>();
    List<BasicDynaBean> babyMotherDetails = new ArrayList<>();

    // if advanced is 'N', get basic details
    if (advanced.equals("N")) {

      BasicDynaBean patientBasicDetails = patientDetailsService
          .getBasicDetails(mrNo, appointmentId);
      if (patientBasicDetails == null) {
        EntityNotFoundException ex = null;
        if (mrNo != null) {
          ex = new EntityNotFoundException(new String[] { "Patient", "MR Number", mrNo });
        } else {
          ex = new EntityNotFoundException(new String[] { "Appointment", "id", appointmentId });
        }
        throw ex;
      }
      ret.put("mr_no", patientBasicDetails.get("mr_no"));
      ret.put("salutation", patientBasicDetails.get("salutation"));
      ret.put("salutation_id", patientBasicDetails.get("salutation_id"));
      ret.put("full_name", patientBasicDetails.get("full_name"));
      ret.put("patient_name", patientBasicDetails.get("patient_name"));
      ret.put("middle_name", patientBasicDetails.get("middle_name"));
      ret.put("last_name", patientBasicDetails.get("last_name"));
      ret.put("dateofbirth",
          DateUtil.formatDate((java.sql.Date) patientBasicDetails.get("dateofbirth")));
      ret.put("expected_dob",
          DateUtil.formatDate((java.sql.Date) patientBasicDetails.get("expected_dob")));
      ret.put("patient_gender", patientBasicDetails.get("patient_gender"));
      ret.put("patient_gender_text", patientBasicDetails.get("patient_gender_text"));
      ret.put("patient_contact", patientBasicDetails.get("patient_phone"));
      ret.put("age_text", patientBasicDetails.get("age_text"));

      Map<String, Object> appointmentDetails = new HashMap<String, Object>();
      appointmentDetails.put("patient_name", patientBasicDetails.get("patient_name"));
      appointmentDetails.put("patient_contact", patientBasicDetails.get("patient_phone"));
      ret.put("appointment_details", appointmentDetails);
    } else if (advanced.equals("Y")) { // if advanced is 'Y', get advanced details

      BasicDynaBean patientAdvancedDetails = patientDetailsService
          .getAdvancedDetails(mrNo, visitId);

      if (headerPreferences.equals("Y")) {
        // get patient level preferences
        List<BasicDynaBean> patientLevelPreferences = preferencesService.getPreferencesToDisplay(
            "P", visitType, dataCategory);

        if (patientAdvancedDetails == null) {
          EntityNotFoundException ex = new EntityNotFoundException(new String[] { "Patient",
              "MR Number" + " Visit Id", mrNo + " " + visitId });
          throw ex;
        }

        List<Map<String, Object>> patientLevelPreferencesMapList = 
            new ArrayList<Map<String, Object>>();
        Iterator<BasicDynaBean> itr = patientLevelPreferences.iterator();

        while (itr.hasNext()) {
          BasicDynaBean bean = itr.next();
          Map<String, Object> patientLevelPreferencesMap = new HashMap<String, Object>();
          patientLevelPreferencesMap.put("field_name", bean.get("field_name"));
          patientLevelPreferencesMap.put("display", bean.get("display"));
          patientLevelPreferencesMap.put("data_level", bean.get("data_level"));
          patientLevelPreferencesMap.put("field_desc", bean.get("field_desc"));
          patientLevelPreferencesMap.put("visit_type", bean.get("visit_type"));
          patientLevelPreferencesMap.put("data_category", bean.get("data_category"));
          patientLevelPreferencesMap.put("display_order", bean.get("display_order"));
          patientLevelPreferencesMap.put("data_type", bean.get("data_type"));
          patientLevelPreferencesMapList.add(patientLevelPreferencesMap);
          DynaProperty property = patientAdvancedDetails.getDynaClass().getDynaProperty(
              (String) bean.get("field_name"));
          if (property.getType().equals(java.sql.Date.class)) {
            ret.put((String) bean.get("field_name"), DateUtil
                .formatDate((java.sql.Date) patientAdvancedDetails.get((String) bean
                    .get("field_name"))));
          } else {
            ret.put((String) bean.get("field_name"),
                patientAdvancedDetails.get((String) bean.get("field_name")));
          }

          if (bean.get("field_name") != null && bean.get("field_name").equals("patient_gender")) {
            ret.put("patient_gender_text", patientAdvancedDetails.get("patient_gender_text"));
          }
          if (bean.get("field_name") != null
              && bean.get("field_name").equals("other_identification_doc_value")) {
            ret.put("other_identification_doc_value_text",
                patientAdvancedDetails.get("other_identification_doc_value_text"));
          }
          if (bean.get("field_name") != null && bean.get("field_name").equals("age_text")) {
            if (patientAdvancedDetails.get("dateofbirth") != null) {
              ret.put("age_text", DateUtil.getAgeTextForDate(
                  patientAdvancedDetails.get("dateofbirth").toString(), "yyyy-MM-dd"));
            }
            if (patientAdvancedDetails.get("expected_dob") != null) {
              ret.put("age_text", DateUtil.getAgeTextForDate(
                  patientAdvancedDetails.get("expected_dob").toString(), "yyyy-MM-dd"));
            }
          }
        }

        ret.put("patient_field_prefs", patientLevelPreferencesMapList);
        // get visit level preferences
        Map<String, Object> visitDetailsMap = new HashMap<String, Object>();
        List<BasicDynaBean> visitLevelPreferences = preferencesService.getPreferencesToDisplay("V",
            visitType, dataCategory);
        List<Map<String, Object>> visitLevelPreferencesMapList = 
            new ArrayList<Map<String, Object>>();
        itr = visitLevelPreferences.iterator();

        while (itr.hasNext()) {
          BasicDynaBean bean = itr.next();
          Map<String, Object> visitLevelPreferencesMap = new HashMap<String, Object>();
          String fieldName = (String) bean.get("field_name");
          if (fieldName.equals("admitted_dept")) {
            fieldName += "_name";
          }
          visitLevelPreferencesMap.put("field_name", fieldName);
          visitLevelPreferencesMap.put("display", bean.get("display"));
          visitLevelPreferencesMap.put("data_level", bean.get("data_level"));
          visitLevelPreferencesMap.put("field_desc", bean.get("field_desc"));
          visitLevelPreferencesMap.put("visit_type", bean.get("visit_type"));
          visitLevelPreferencesMap.put("data_category", bean.get("data_category"));
          visitLevelPreferencesMap.put("display_order", bean.get("display_order"));
          visitLevelPreferencesMap.put("data_type", bean.get("data_type"));
          visitLevelPreferencesMapList.add(visitLevelPreferencesMap);

          if (visitId != null) {
            DynaProperty property = patientAdvancedDetails.getDynaClass()
                .getDynaProperty(fieldName);
            if (property != null && property.getType().equals(java.sql.Date.class)) {
              visitDetailsMap.put(fieldName,
                  DateUtil.formatDate((java.sql.Date) patientAdvancedDetails.get(fieldName)));
            } else {
              visitDetailsMap.put(fieldName, patientAdvancedDetails.get(fieldName));
            }
          } else {
            visitDetailsMap.put(fieldName, null);
          }
        }

        ret.put("visit_field_prefs", visitLevelPreferencesMapList);
        ret.put("visit_details", visitDetailsMap);
      } else if (headerPreferences.equals("N")) {
        // currently we are sending only original_mr_no. feel free to send new values here.
        ret.put("original_mr_no", patientAdvancedDetails.get("original_mr_no"));
      }

      if (visitId != null) {
        babyMotherDetails = patientDetailsService.getBabyOrMotherDetails(visitId);
      }

    }
    ret.put("baby_mother_details", ConversionUtils.listBeanToListMap(babyMotherDetails));

    return ret;
  }

  /**
   * This method updates patient details.
   * @param params the params
   * @return updated patient details map
   */
  public Map<String, Object> updatePatientDetails(Map<String, Object> params) {
    
    String mrNo = (String) params.get("mr_no");
    
    // parameter validation check
    if ((mrNo == null || mrNo.isEmpty())) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("mr_no", "exception.notnull.value",
          Arrays.asList(StringUtil.prettyName("mr_no")));
      ValidationException ex = new ValidationException(errorMap);
      throw ex;
    }

    Map<String, Object> ret = new HashMap<String, Object>();
    List<String> errors = new ArrayList<String>();
    
    BasicDynaBean bean = patientDetailsService.getBean();  
    ConversionUtils.copyJsonToDynaBean(params, bean, errors, true);
    
    BasicDynaBean visitBean = patientRegistrationRepository.getBean(); 
    ConversionUtils.copyJsonToDynaBean(params, visitBean, errors, true);

    if (mrNo != null && patientDetailsService.isMrNumberValid(mrNo)
        && params.containsKey("visit_id") && params.get("visit_id").equals("new")) {
      BasicDynaBean visitIdAssoWithMrBean = patientDetailsRepository.getPatientVisit(mrNo);
      String visitId = (String) visitIdAssoWithMrBean.get("visit_id");
      if (!visitId.equals(params.get("visit_id"))) {
        bean.set("visit_id", visitId);
      }
    }

    if (!errors.isEmpty()) {
      throw new ConversionException(errors);
    }
    Map<String, Object> keys = new HashMap<String, Object>();
    String visitId = params.containsKey("visit_id") && !params.get("visit_id").equals("new")
        ? (String) bean.get("visit_id")
        : params.containsKey("visit_id") ?  (String)params.get("visit_id") : null;
    boolean visitUpdated = false;
    if (visitId != null) {
      keys.put(Constants.PATIENT_ID, visitId);
      patientRegistrationRepository.update(visitBean, keys);
      visitUpdated = true;
    }

    List<String> violations = new ArrayList<String>();
    List<String> fieldsList = Arrays.asList("patient_phone", "email_id");

    BasicDynaBean existingBean = patientDetailsService.getAdvancedDetails(mrNo, null);
    if (!urlAclRule.apply(bean, existingBean, Arrays.asList("original_mr_no"), "reg_general",
        violations)) {
      throw new AccessDeniedException("exception.access.denied.markmrduplicate", new String[] {});
    }

    if (!aclRule.apply(bean, existingBean, fieldsList, "edit_patient_header", violations)) {
      throw new AccessDeniedException("exception.access.denied.patientheader", new String[] {});
    }

    patientHeaderValidator.setRuleSetMap(bean);
    patientHeaderValidator.validate(bean);

    Map<String, Object> key = new HashMap<String, Object>();
    key.put("mr_no", mrNo);
    String patientContact = (String) params.get("patient_phone");
    if (patientContact != null && !patientContact.isEmpty()) {
      int centerId = RequestContext.getCenterId();
      String defaultCode = centerService.getCountryCode(centerId);
      if (defaultCode == null) {
        defaultCode = centerService.getCountryCode(0);
      }
      List<String> parts = PhoneNumberUtil.getCountryCodeAndNationalPart(patientContact, null);
      if (parts != null && !parts.isEmpty() && !parts.get(0).isEmpty()) {
        bean.set("patient_phone_country_code", "+" + parts.get(0));
      } else if (defaultCode != null) {
        bean.set("patient_phone_country_code", "+" + defaultCode);
        if (!patientContact.startsWith("+")) {
          bean.set("patient_phone", "+" + defaultCode + patientContact);
        }
      }
      if (bean.get("patient_phone").toString().length() > 16) {
        ValidationErrorMap errorMap = new ValidationErrorMap();
        errorMap.addError("patient_phone", "js.registration.patient.invalid.phoneno.long");
        ValidationException ex = new ValidationException(errorMap);
        throw ex;
      }
    }
    Integer updatedRows = patientDetailsService.update(bean, key);
    if (visitUpdated) {
      interfaceEventMappingService.editVisitEvent((String) visitBean.get(Constants.PATIENT_ID));
    }
    String originalMrNo = (String) params.get("original_mr_no");
    if (!StringUtils.isEmpty(originalMrNo)) {
      interfaceEventMappingService.mergePatientsEvent(originalMrNo, mrNo);
    } else {
      interfaceEventMappingService.editPatientEvent(mrNo);
    }
    if (updatedRows > 0) {
      if (originalMrNo != null && !originalMrNo.isEmpty()) {
        Map<String, Object> adtData = new HashMap<>();
        adtData.put(Constants.MR_NO, originalMrNo);
        
        adtData.put("old_mr_no", mrNo);
        adtService.createAndSendADTMessage("ADT_18", adtData);
      }
      BasicDynaBean patientAdvancedDetails = patientDetailsService.getAdvancedDetails(mrNo, null);
      ret.put("mr_no", (String) patientAdvancedDetails.get("mr_no"));
      ret.put("patient_phone", (String) patientAdvancedDetails.get("patient_phone"));
      ret.put("email_id", (String) patientAdvancedDetails.get("email_id"));
      ret.put("original_mr_no", (String) patientAdvancedDetails.get("original_mr_no"));
    } else {
      EntityNotFoundException ex = new EntityNotFoundException(new String[] { "Patient",
          "MR Number", mrNo });
      throw ex;
    }

    String contactPrefLangCode = (String) params.get("contact_pref_lang_code");
    if (contactPrefLangCode == null || contactPrefLangCode.isEmpty()) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("contact_pref_lang_code",
          "js.registration.patient.required.preferred.language");
      ValidationException ex = new ValidationException(errorMap);
      throw ex;
    }
    updatedRows = patientDetailsService.updateContactPrefLangCode(mrNo, contactPrefLangCode);
    if (updatedRows > 0) {
      BasicDynaBean patientAdvancedDetails = patientDetailsService.getAdvancedDetails(mrNo, null);
      ret.put("contact_pref_lang_code", patientAdvancedDetails.get("contact_pref_lang_code"));
    }

    return ret;
  }

  /**
   * This method uploads photo.
   *
   * @param mrNo
   *          the mr no
   * @param patientPhoto
   *          the patient photo
   * @return the boolean
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public Boolean uploadPhoto(String mrNo, byte[] patientPhoto) throws IOException {

    // parameter validation check
    if ((mrNo == null || mrNo.isEmpty())) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("mr_no", "exception.notnull.value",
          Arrays.asList(StringUtil.prettyName("mr_no")));
      ValidationException ex = new ValidationException(errorMap);
      throw ex;
    }

    if (patientPhoto == null) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("patient_photo", "exception.notnull.value",
          Arrays.asList(StringUtil.prettyName("patient_photo")));
      ValidationException ex = new ValidationException(errorMap);
      throw ex;
    }

    Boolean status = patientDetailsService.uploadPhoto(mrNo, patientPhoto);

    if (!status) {
      EntityNotFoundException ex = new EntityNotFoundException(new String[] { "Patient",
          "MR Number", mrNo });
      throw ex;
    }
    return status;
  }

  /**
   * This method gets photo.
   *
   * @param mrNo
   *          the mr no
   * @param dimensions
   *          the dimensions
   * @return the photo
   */
  public InputStream getPhoto(String mrNo, String dimensions) {

    // parameter validation check
    if ((mrNo == null || mrNo.isEmpty())) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("mr_no", "exception.notnull.value",
          Arrays.asList(StringUtil.prettyName("mr_no")));
      ValidationException ex = new ValidationException(errorMap);
      throw ex;
    }

    if (!(dimensions == null || dimensions.equals("36x36") || dimensions.equals("46x46")
        || dimensions.equals("66x66") || dimensions.equals("84x84"))) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("dimensions", "exception.notnull.value",
          Arrays.asList(StringUtil.prettyName("dimensions")));
      ValidationException ex = new ValidationException(errorMap);
      throw ex;
    }

    String columnName = null;
    if (dimensions == null) {
      columnName = "patient_photo";
    } else {
      columnName = "patient_photo_" + dimensions;
    }

    BasicDynaBean patientPhotoBean = patientDetailsService.getPhoto(mrNo, columnName);
    if (patientPhotoBean != null && patientPhotoBean.get(columnName) != null) {
      return (InputStream) patientPhotoBean.get(columnName);
    } else if (patientPhotoBean == null) {
      EntityNotFoundException ex = new EntityNotFoundException(new String[] { "Patient",
          "given ","MR Number" });
      throw ex;
    } else {
      EntityNotFoundException ex = new EntityNotFoundException(new String[] { "Photo", "MR Number",
          mrNo });
      throw ex;
    }
  }

  /**
   * Checks if is valid visit type.
   *
   * @param visitType
   *          the visit type
   * @return true, if is valid visit type
   */
  public boolean isValidVisitType(String[] visitType) {
    if (visitType == null) {
      return false;
    }
    List<String> valid = Arrays.asList(new String[] { "b", "i", "o" });
    for (int i = 0; i < visitType.length; i++) {
      if (!valid.contains(visitType[i])) {
        return false;
      }
    }
    return true;
  }

  /**
   * Checks if is valid data category.
   *
   * @param dataCategory
   *          the data category
   * @return true, if is valid data category
   */
  public boolean isValidDataCategory(String[] dataCategory) {
    if (dataCategory == null) {
      return false;
    }
    List<String> valid = Arrays.asList(new String[] { "Both", "None", "O", "C" });
    for (int i = 0; i < dataCategory.length; i++) {
      if (!valid.contains(dataCategory[i])) {
        return false;
      }
    }

    return true;
  }

  /**
   * Gets the preferred languages.
   *
   * @return the preferred languages
   */
  public List<Map<String, String>> getPreferredLanguages(String userLangCode) {
    return patientDetailsService.getPreferredLanguages(userLangCode);
  }
}
