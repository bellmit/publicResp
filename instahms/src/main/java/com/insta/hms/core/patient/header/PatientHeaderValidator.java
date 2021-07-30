package com.insta.hms.core.patient.header;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.security.SecurityService;
import com.insta.hms.common.validation.EmailIdRule;
import com.insta.hms.common.validation.MrNumberRule;
import com.insta.hms.common.validation.NotNullRule;
import com.insta.hms.common.validation.PhoneNumberRule;
import com.insta.hms.common.validation.RuleSetValidator;
import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.core.patient.registration.RegistrationPreferencesService;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class PatientHeaderValidator.
 */
@Component
public class PatientHeaderValidator extends RuleSetValidator {

  /** The phone number rule. */
  @Autowired
  PhoneNumberRule phoneNumberRule;

  /** The email id rule. */
  @Autowired
  EmailIdRule emailIdRule;

  /** The mr number rule. */
  @Autowired
  MrNumberRule mrNumberRule;

  /** The generic preferences service. */
  @Autowired
  GenericPreferencesService genericPreferencesService;

  /** The security service. */
  @LazyAutowired
  private SecurityService securityService;

  /** The reg pref service. */
  @LazyAutowired
  private RegistrationPreferencesService regPrefService;

  /** The not null rule. */
  private NotNullRule notNullRule = new NotNullRule();

  /**
   * This method sets rule set map of validator.
   *
   * @param bean
   *          the new rule set map
   */
  public void setRuleSetMap(BasicDynaBean bean) {
    Map<ValidationRule, String[]> value = new HashMap<ValidationRule, String[]>();

    BasicDynaBean genericPreferences = genericPreferencesService.getAllPreferences();

    Map<String, String> actionRightsMap = (Map<String, String>) securityService
        .getSecurityAttributes().get("actionRightsMap");
    if (securityService.isAdministrator() 
        || "A".equals(actionRightsMap.get("edit_patient_header"))) {
      // add patient phone rule only when preference is enabled.
      Map regPref = regPrefService.getRegistrationPreferences().getMap();
      boolean mobileRequired = regPref.get("patientphone_field_validate") != null
          && !((String) regPref.get("patientphone_field_validate")).equals("N");
      boolean emailRequired = regPref.get("validate_email_id") != null
          && !((String) regPref.get("validate_email_id")).equals("N");
      boolean hasMobile = bean.get("patient_phone") != null
          && !((String) bean.get("patient_phone")).isEmpty();
      if (mobileRequired) {
        value.put(notNullRule, new String[] { "patient_phone" });
      }
      if (hasMobile 
          && ((String) genericPreferences.get("mobile_number_validation")).equals("Y")) {
        String[] fields = { "patient_phone" };
        value.put(phoneNumberRule, fields);
      }
      if (emailRequired) {
        value.put(notNullRule, new String[] { "email_id" });
      }
      boolean hasEmail = bean.get("email_id") != null 
          && !((String) bean.get("email_id")).isEmpty();
      if (hasEmail) {
        value.put(emailIdRule, new String[] { "email_id" });
      }
    }
    value.put(mrNumberRule, new String[] { "original_mr_no" });

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
      throwErrors();
    }
    return result;
  }

  /**
   * This method throws validation errors.
   *
   * @return true, if successful
   */
  private boolean throwErrors() {
    ValidationErrorMap errorMap = getErrors();
    ValidationException ex = new ValidationException(errorMap);
    throw ex;
  }
}
