package com.insta.hms.core.diagnostics.incomingsampleregistration;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.validation.NotNullRule;
import com.insta.hms.common.validation.PhoneNumberRule;
import com.insta.hms.common.validation.RuleSetValidator;
import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class IncomingSampleRegistrationValidation extends RuleSetValidator {
  
  /** The Constant INSERT_RULESET_NAME. */
  public static final String UPDATE_RULESET_NAME = "insert";
  
  @Autowired
  private PhoneNumberRule phoneNumberRule;
  
  private static final NotNullRule notNullRule = new NotNullRule();
  private static final String[] NOT_NULL_FIELDS_INSERT = new String[]{"patient_name", 
      "patient_gender"};
  
  @LazyAutowired
  private GenericPreferencesService genericPreferencesService;
  

  /**
   * Constructor.
   */
  public IncomingSampleRegistrationValidation() {
    super();
    /* INSERT VALIDATION RULES. */
    addRule(UPDATE_RULESET_NAME, notNullRule, NOT_NULL_FIELDS_INSERT);
  }
  
  /**
   * Validate update.
   *
   * @param bean
   *          the bean
   * @return true, if successful
   */
  public boolean validateUpdate(BasicDynaBean bean) {
    setRuleSetMap(bean);
    boolean result = applyRuleSet(UPDATE_RULESET_NAME, bean);
    result = result && applyRuleSet(DEFAULT_RULESET_NAME, bean);
    if (!result) {
      throwErrors();
    }
    return true;
  }
  
  /**
   * This method sets rule set map of validator.
   *
   * @param bean bean to validate
   */
  public void setRuleSetMap(BasicDynaBean bean) {
    Map<ValidationRule, String[]> value = new HashMap<ValidationRule, String[]>();
    
    BasicDynaBean genericPreferences = genericPreferencesService.getAllPreferences();
    boolean hasMobile = bean.get("phone_no") != null && !((String) bean.get("phone_no")).isEmpty();
    value.put(notNullRule, new String[]{"phone_no"});
    if (hasMobile && ((String)genericPreferences.get("mobile_number_validation")).equals("Y")) {
      String [] fields = {"phone_no"};
      value.put(phoneNumberRule, fields);
    }
    
    this.ruleSetMap.put(DEFAULT_RULESET_NAME, value);
  }
  
  /**
   * Throw errors.
   *
   * @return true, if successful
   */
  protected boolean throwErrors() {
    ValidationErrorMap errorMap = getErrors();
    throw new ValidationException(errorMap);
  }
}
