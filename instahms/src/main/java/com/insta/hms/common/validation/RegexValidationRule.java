package com.insta.hms.common.validation;

import com.insta.hms.common.StringUtil;
import com.insta.hms.exception.ValidationErrorMap;

import org.apache.commons.beanutils.BasicDynaBean;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * The Class RegexValidationRule.
 */
public class RegexValidationRule extends ValidationRule {

  /** The Constant ALPHANUMERIC_STRING. */
  public static final String ALPHANUMERIC_STRING = "^[0-9A-Za-z ]+$";

  /** The Constant ALPHANUMERIC_WORD. */
  public static final String ALPHANUMERIC_WORD = "^[0-9A-Za-z]+$";

  /** The pattern string. */
  private String patternString;

  /** The custom message key. */
  private String customMessageKey = null;

  /**
   * Instantiates a new regex validation rule.
   *
   * @param patternString
   *          the pattern string
   */
  public RegexValidationRule(String patternString) {
    this.patternString = patternString;
  }

  /**
   * Instantiates a new regex validation rule.
   *
   * @param patternString
   *          the pattern string
   * @param customMessageKey
   *          the custom message key
   */
  public RegexValidationRule(String patternString, String customMessageKey) {
    this.patternString = patternString;
    if (customMessageKey != null && !customMessageKey.isEmpty()) {
      this.customMessageKey = customMessageKey;
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
    if (null == bean) {
      return false;
    }
    boolean ok = true;
    for (String field : fields) {
      if (null != bean.get(field)
          && Pattern.matches(this.patternString, bean.get(field).toString())) {
        continue;
      }
      ok = false;
      if (null == errorMap.getErrorMap()) {
        continue;
      }
      if (this.patternString.equals(ALPHANUMERIC_STRING)) {
        errorMap.addError(field,
            customMessageKey != null ? customMessageKey : "exception.not.alphanumericstring",
            Arrays.asList(StringUtil.prettyName(field)));
      } else if (this.patternString.equals(ALPHANUMERIC_WORD)) {
        errorMap.addError(field,
            customMessageKey != null ? customMessageKey : "exception.not.alphanumericword",
            Arrays.asList(StringUtil.prettyName(field)));
      } else {
        errorMap.addError(field,
            customMessageKey != null ? customMessageKey : "exception.regexvalidation.failed",
            Arrays.asList(StringUtil.prettyName(field), patternString));
      }
    }
    return ok;
  }

}
