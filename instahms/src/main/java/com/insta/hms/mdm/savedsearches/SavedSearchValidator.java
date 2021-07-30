package com.insta.hms.mdm.savedsearches;

import com.insta.hms.common.validation.MaximumLengthRule;
import com.insta.hms.common.validation.NotNullRule;
import com.insta.hms.common.validation.RegexValidationRule;
import com.insta.hms.common.validation.RuleSetValidator;
import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;

/**
 * The Class SavedSearchValidator.
 *
 * @author krishnat
 */
@Component
public class SavedSearchValidator extends RuleSetValidator {

  /** The Constant MAXIMUM_LENGTH_RULE_INSERT. */
  private static final ValidationRule MAXIMUM_LENGTH_RULE = new MaximumLengthRule(50);

  /** The Constant MAXIMUM_LENGTH_FIELDS_INSERT. */
  private static final String[] MAXIMUM_LENGTH_FIELDS_INSERT = new String[] {"search_name"};

  /** The Constant MAXIMUM_LENGTH_FIELDS_UPDATE. */
  private static final String[] MAXIMUM_LENGTH_FIELDS_UPDATE = new String[] {"search_name"};

  /** The Constant ALPHANUMERIC_FIELDS_INSERT. */
  private static final String[] ALPHANUMERIC_FIELDS_INSERT = new String[] {"search_name"};

  /** The Constant ALPHANUMERIC_FIELDS_UPDATE. */
  private static final String[] ALPHANUMERIC_FIELDS_UPDATE = new String[] {"search_name"};

  /** The Constant NOT_NULL_FIELDS. */
  private static final String[] NOT_NULL_FIELDS = new String[] {"search_name", "flow_id"};

  /** The Constant NOT_NULL_RULE. */
  private static final ValidationRule NOT_NULL_RULE =
      new NotNullRule("exception.savedsearches.searchname.empty");

  /** The Constant REGEX_VALIDATION_RULE. */
  private static final ValidationRule REGEX_VALIDATION_RULE =
      new RegexValidationRule(
          RegexValidationRule.ALPHANUMERIC_STRING, "exception.savedsearches.searchname.invalid");

  /** Instantiates a new saved search validator. */
  public SavedSearchValidator() {
    addRule("insert", MAXIMUM_LENGTH_RULE, MAXIMUM_LENGTH_FIELDS_INSERT);
    addRule("insert", NOT_NULL_RULE, NOT_NULL_FIELDS);
    addRule("insert", REGEX_VALIDATION_RULE, ALPHANUMERIC_FIELDS_INSERT);

    addRule("update", MAXIMUM_LENGTH_RULE, MAXIMUM_LENGTH_FIELDS_UPDATE);
    addRule("update", NOT_NULL_RULE, NOT_NULL_FIELDS);
    addRule("update", REGEX_VALIDATION_RULE, ALPHANUMERIC_FIELDS_UPDATE);
  }

  /**
   * Validate update.
   *
   * @param bean the bean
   * @return true, if successful
   */
  public boolean validateUpdate(BasicDynaBean bean) {
    boolean result = applyRuleSet("update", bean);
    if (!result) {
      throwErrors();
    }
    return true;
  }

  /**
   * Validate insert.
   *
   * @param bean the bean
   * @return true, if successful
   */
  public boolean validateInsert(BasicDynaBean bean) {
    boolean result = applyRuleSet("insert", bean);
    if (!result) {
      throwErrors();
    }
    return true;
  }

  /**
   * Throw errors.
   *
   * @return true, if successful
   */
  private boolean throwErrors() {
    ValidationErrorMap errorMap = getErrors();

    ValidationException ex = new ValidationException(errorMap);
    throw ex;
  }
}
