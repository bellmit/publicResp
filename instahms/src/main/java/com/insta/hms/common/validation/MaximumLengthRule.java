package com.insta.hms.common.validation;

import com.insta.hms.common.StringUtil;
import com.insta.hms.exception.ValidationErrorMap;

import org.apache.commons.beanutils.BasicDynaBean;

import java.util.Arrays;

/**
 * The MaximumLength Rule.
 * 
 * @author tanmay.k
 */
public class MaximumLengthRule extends PropertyValidationRule {

  /** The maximum length. */
  protected Integer maximumLength = 70;

  /**
   * Instantiates a new maximum length rule.
   */
  public MaximumLengthRule() {
    super();
  }

  /**
   * Instantiates a new maximum length rule.
   *
   * @param maximumLength
   *          the maximum length
   */
  public MaximumLengthRule(Integer maximumLength) {
    super();
    this.maximumLength = maximumLength;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.common.validation.ValidationRule#apply(org.apache.commons.beanutils.
   * BasicDynaBean, java.lang.String[], java.util.Map)
   */
  @Override
  public boolean apply(BasicDynaBean bean, String[] fields, ValidationErrorMap errorMap) {
    boolean ok = false;
    if (bean != null) {
      ok = true;
      for (String field : fields) {
        Object value = bean.get(field);
        if (value != null && value.toString().length() > maximumLength) {
          if (null != errorMap.getErrorMap()) {
            errorMap.addError(field, "exception.max.length", Arrays
                .asList(new String[] { StringUtil.prettyName(field), maximumLength.toString() }));
          }
          ok = false;
        }
      }
    }
    return ok;
  }
}