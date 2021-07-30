package com.insta.hms.mdm.sequences;

import com.insta.hms.common.validation.NotNullRule;
import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.mdm.MasterValidator;

import org.apache.commons.beanutils.BasicDynaBean;

import java.util.Arrays;
import java.util.List;

/**
 * The Class SequenceMasterValidator.
 */
public abstract class SequenceMasterValidator extends MasterValidator {

  /** The rule list. */
  protected List<BasicDynaBean> ruleList = null;
  
  /** The errors key. */
  protected String errorsKey = null;

  /** The failed validation message key. */
  protected final String conficltRuleValidationMessageKey =
      "exception.sequences.conflict.rule.validation.failed";

  /** The Constant NOT_NULL_RULE_INSERT. */
  protected static final ValidationRule NOT_NULL_RULE_INSERT = new NotNullRule();
  
  /** The Constant NOT_NULL_RULE_UPDATE. */
  protected static final ValidationRule NOT_NULL_RULE_UPDATE = new NotNullRule();

  /**
   * Instantiates a new sequence master validator.
   */
  public SequenceMasterValidator() {
    super();
  }

  /**
   * Gets the rule list.
   *
   * @param bean the bean
   * @return the rule list
   */
  public List<BasicDynaBean> getRuleList(BasicDynaBean bean) {
    return ruleList;
  }

  /**
   * Gets the validateUpdate.
   *
   * @param bean the bean
   * @return the rule boolean
   */
  @Override
  public boolean validateUpdate(BasicDynaBean bean) {
    super.validateUpdate(bean);
    return validateRule(bean, getRuleList(bean), errorsKey);
  }

  /**
   * Gets the validateInsert.
   *
   * @param bean the bean
   * @return the rule boolean
   */
  @Override
  public boolean validateInsert(BasicDynaBean bean) {
    super.validateInsert(bean);
    return validateRule(bean, getRuleList(bean), errorsKey);
  }

  /**
   * Validate rule.
   *
   * @param bean the bean
   * @param ruleList the rule list
   * @param errorsKey the errors key
   * @return true, if successful
   */
  public boolean validateRule(BasicDynaBean bean, List<BasicDynaBean> ruleList, String errorsKey) {
    ValidationErrorMap errorsMap = new ValidationErrorMap();
    if (ruleList != null && ruleList.size() > 0) {
      errorsMap.addError(
          errorsKey + " Conficting Rule ",
          conficltRuleValidationMessageKey,
          Arrays.asList(new String[] {""}));
    }

    if (!errorsMap.getErrorMap().isEmpty()) {
      throw new ValidationException(errorsMap);
    }

    return true;
  }
}
