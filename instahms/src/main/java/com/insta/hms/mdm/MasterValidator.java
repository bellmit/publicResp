package com.insta.hms.mdm;

import com.insta.hms.common.validation.RuleSetValidator;
import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;

import org.apache.commons.beanutils.BasicDynaBean;

/**
 * The Class MasterValidator.
 */
public class MasterValidator extends RuleSetValidator {

  /** The Constant INSERT_RULESET_NAME. */
  public static final String INSERT_RULESET_NAME = "insert";

  /** The Constant UPDATE_RULESET_NAME. */
  public static final String UPDATE_RULESET_NAME = "update";

  /**
   * Validate update.
   *
   * @param bean
   *          the bean
   * @return true, if successful
   */
  public boolean validateUpdate(BasicDynaBean bean) {
    boolean result = applyRuleSet(UPDATE_RULESET_NAME, bean);
    if (!result) {
      throwErrors();
    }
    return true;
  }

  /**
   * Validate insert.
   *
   * @param bean
   *          the bean
   * @return true, if successful
   */
  public boolean validateInsert(BasicDynaBean bean) {
    boolean result = applyRuleSet(INSERT_RULESET_NAME, bean);
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
  protected boolean throwErrors() {
    ValidationErrorMap errorMap = getErrors();
    throw new ValidationException(errorMap);
  }

  /**
   * Adds the insert rule.
   *
   * @param rule
   *          the rule
   * @param fields
   *          the fields
   */
  public final void addInsertRule(ValidationRule rule, String[] fields) {
    addRule(INSERT_RULESET_NAME, rule, fields);
  }

  /**
   * Adds the update rule.
   *
   * @param rule
   *          the rule
   * @param fields
   *          the fields
   */
  public final void addUpdateRule(ValidationRule rule, String[] fields) {
    addRule(UPDATE_RULESET_NAME, rule, fields);
  }

  /**
   * Adds the default rule.
   *
   * @param rule
   *          the rule
   * @param fields
   *          the fields
   */
  public final void addDefaultRule(ValidationRule rule, String[] fields) {
    addRule(DEFAULT_RULESET_NAME, rule, fields);
  }
}
