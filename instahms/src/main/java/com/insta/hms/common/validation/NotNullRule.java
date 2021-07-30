package com.insta.hms.common.validation;

import com.insta.hms.common.StringUtil;
import com.insta.hms.exception.ValidationErrorMap;

import org.apache.commons.beanutils.BasicDynaBean;

import java.util.Arrays;

/**
 * The Class NotNullRule.
 */
public class NotNullRule extends PropertyValidationRule {

  /** The message key. */
  private String messageKey = "exception.notnull.value";

  /**
   * Instantiates a new not null rule.
   */
  public NotNullRule() {

  }

  /**
   * Instantiates a new not null rule.
   *
   * @param messageKey
   *          the message key
   */
  public NotNullRule(String messageKey) {
    if (messageKey != null && !messageKey.isEmpty()) {
      this.messageKey = messageKey;
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.common.validation.ValidationRule#apply(org.apache.commons.beanutils.
   * BasicDynaBean, java.lang.String[], com.insta.hms.exception.ValidationErrorMap)
   */
  @Override
  public boolean apply(BasicDynaBean bean, String[] fields, ValidationErrorMap errorMap) {
    boolean ok = false;
    if (null != bean) {
      ok = true;
      for (String field : fields) {
        if (null == bean.get(field) || bean.get(field).toString().trim().isEmpty()) {
          if (null != errorMap.getErrorMap()) {
            errorMap.addError(field, this.messageKey, Arrays.asList(StringUtil.prettyName(field)));
          }
          ok = false;
        }
      }
    }
    return ok;
  }

}