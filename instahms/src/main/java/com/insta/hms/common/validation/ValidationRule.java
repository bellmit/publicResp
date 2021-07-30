package com.insta.hms.common.validation;

import com.insta.hms.exception.ValidationErrorMap;

import org.apache.commons.beanutils.BasicDynaBean;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class ValidationRule {

  public abstract boolean apply(BasicDynaBean bean, String[] fields, ValidationErrorMap errorMap);

  private Map<String, String> violations = new HashMap<String, String>();

  public void addViolation(String property, String message) {
    violations.put(property, message);
  }

  public Map<String, String> getViolations() {
    return Collections.unmodifiableMap(violations);
  }

}
