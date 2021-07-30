package com.insta.hms.common.phonenumber;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.validation.PhoneNumberRule;
import com.insta.hms.core.patient.PatientDetailsService;
import com.insta.hms.core.patient.registration.PatientRegistrationRepository;
import com.insta.hms.core.patient.registration.RegistrationPreferencesService;
import com.insta.hms.core.patient.registration.RegistrationValidator;
import com.insta.hms.exception.NestableValidationException;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Phone Number validation service class. */
@Service
public class PhoneNumberValidationService {

  /** Patient Details Service. * */
  @LazyAutowired
  private PatientDetailsService patientDetailsService;

  /** Patient Registration Repository. * */
  @LazyAutowired
  private PatientRegistrationRepository patientRegistrationRepository;

  /** Phone number Validation Service. * */
  @LazyAutowired
  private PhoneNumberValidationService phoneNumberValidationService;

  /** Registration Preference service. * */
  @LazyAutowired
  private RegistrationPreferencesService regPrefService;

  /** Generic Preferences service. * */
  @LazyAutowired
  private GenericPreferencesService genPrefService;

  /** Phone number validator class. * */
  @LazyAutowired
  private PhoneNumberRule phoneNumberRule;

  /** Registration validator. * */
  @LazyAutowired
  private RegistrationValidator registrationValidator;

  /**
   * Validate mobile number pattern.
   *
   * @param params parameters map.
   */
  public void validateMobileNumber(Map<String, Object> params) throws Exception {
    BasicDynaBean patientBean = patientDetailsService.getBean();
    BasicDynaBean visitBean = patientRegistrationRepository.getBean();

    List<String> errors = new ArrayList<String>();
    Map<String, Object> patientParams = (Map<String, Object>) params.get("patient");
    ConversionUtils.copyJsonToDynaBean(patientParams, patientBean, errors, true);

    Map<String, Object> visitParams = (Map<String, Object>) params.get("visit");
    ConversionUtils.copyJsonToDynaBean(visitParams, visitBean, errors, true);

    ValidationErrorMap validationErrorMap = new ValidationErrorMap();
    Map<String, Object> nestedException = new HashMap();

    ValidationException ex = null;
    if (!validate(patientBean, validationErrorMap, (Boolean) visitBean.get("is_er_visit"))) {
      ex = new ValidationException(validationErrorMap);
      nestedException.put("patient", ex.getErrors());
    }

    if (!nestedException.isEmpty()) {
      throw new NestableValidationException(nestedException);
    }
  }

  /**
   * phone number validate method.
   *
   * @param bean             BasicDynaBean
   * @param validationErrors error map
   * @param isErVisit      emergency patient
   * @return return false if validation fails otherwise true
   */
  public boolean validate(BasicDynaBean bean, ValidationErrorMap validationErrors,
      Boolean isErVisit) {

    boolean success = true;

    Map regPref = regPrefService.getRegistrationPreferences().getMap();
    Boolean isUnidentifiedPatient = (Boolean) bean.get("is_unidentified_patient");
    Boolean mobileAccess = bean.get("mobile_access") != null ? (Boolean) bean.get("mobile_access")
        : false;

    Boolean phoneValidationEnabled = genPrefService.getAllPreferences()
        .get("mobile_number_validation").equals("Y");
    String patientPhoneValidate = ((String) regPref.get("patientphone_field_validate"));

    if (((patientPhoneValidate != null
        && (patientPhoneValidate.equals("O") || patientPhoneValidate.equals("A"))
        && phoneValidationEnabled) || mobileAccess) && RequestContext.getHttpRequest() != null
        && bean.get("patient_phone") == null && !(isUnidentifiedPatient || isErVisit)) {
      validationErrors.addError("patient_phone", "js.registration.patient.phone.no.required");
      success = false;
    }
    if (phoneValidationEnabled != null && phoneValidationEnabled) {
      if (bean.get("patient_phone") != null
          && !phoneNumberRule.apply(bean, new String[] { "patient_phone" }, validationErrors)) {
        success = false;
      }
      if (bean.get("patient_care_oftext") != null
          && !phoneNumberRule.apply(bean,
              new String[] { "patient_care_oftext" }, validationErrors)) {
        success = false;
      }
    }

    // Format Patient - Phone number
    String phoneNumber = (String) bean.get("patient_phone");
    if (phoneNumber != null && !phoneNumber.isEmpty()) {
      registrationValidator.formatPhoneNumber(bean, "patient_phone", "patient_phone_country_code");
      if (bean.get("patient_phone").toString().length() > 16) {
        validationErrors.addError("patient_phone", "js.registration.patient.invalid.phoneno.long");
        success = false;
      }
    }
    return success;
  }
}
