package com.insta.hms.common.validation;

import com.insta.hms.common.Validator;
import com.insta.hms.exception.ValidationErrorMap;

import org.apache.commons.beanutils.BasicDynaBean;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class RuleSetValidator implements Validator {

  public static final String DEFAULT_RULESET_NAME = "default";
  protected final Map<String, Map<ValidationRule, String[]>> ruleSetMap =
      new HashMap<String, Map<ValidationRule, String[]>>();

  protected ValidationErrorMap errorMap = new ValidationErrorMap();

  protected ValidationErrorMap getErrors() {
    return errorMap;
  }

  protected Map<ValidationRule, String[]> getRuleSet(String ruleSetName) {
    return (null != ruleSetName && ruleSetMap.containsKey(ruleSetName))
        ? ruleSetMap.get(ruleSetName)
        : Collections.unmodifiableMap(new HashMap<ValidationRule, String[]>());
  }

  protected Map<ValidationRule, String[]> getDefaultRuleSet() {
    return getRuleSet(getDefaultRuleSetName());
  }

  protected String getDefaultRuleSetName() {
    return (DEFAULT_RULESET_NAME);
  }

  protected boolean applyRuleSet(Map<ValidationRule, String[]> ruleSet, BasicDynaBean bean) {
    boolean ok = true;
    // clear errorMap for each new request
    errorMap.getErrorMap().clear();
    if (null != ruleSet) {
      for (Entry<ValidationRule, String[]> entry : ruleSet.entrySet()) {
        ValidationRule rule = entry.getKey();
        ok = ok && rule.apply(bean, entry.getValue(), errorMap);
      }
    }
    return ok;
  }

  protected boolean applyRuleSet(String ruleSetName, BasicDynaBean bean) {
    Map<ValidationRule, String[]> ruleSet = getRuleSet(ruleSetName);
    if (null == ruleSet || ruleSet.isEmpty()) {
      ruleSet = getDefaultRuleSet();
    }
    return applyRuleSet(ruleSet, bean);
  }

  protected void addRule(String ruleSetName, ValidationRule rule, String[] fields) {
    if (!ruleSetMap.containsKey(ruleSetName)) {
      ruleSetMap.put(ruleSetName, new HashMap<ValidationRule, String[]>());
    }
    Map<ValidationRule, String[]> ruleSet = ruleSetMap.get(ruleSetName);
    ruleSet.put(rule, fields);
  }

  @Override
  public boolean validate(BasicDynaBean bean) {
    Map<ValidationRule, String[]> ruleSet = getDefaultRuleSet();
    if (null != ruleSet && !ruleSet.isEmpty()) {
      return applyRuleSet(ruleSet, bean);
    }
    return true;
  }

}
