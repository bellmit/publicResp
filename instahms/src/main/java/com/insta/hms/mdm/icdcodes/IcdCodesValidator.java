package com.insta.hms.mdm.icdcodes;

import com.insta.hms.common.validation.MaximumLengthRule;
import com.insta.hms.common.validation.NotNullRule;
import com.insta.hms.mdm.MasterValidator;

import org.springframework.stereotype.Component;

/**
 * The Class IcdCodesValidator.
 */
@Component
public class IcdCodesValidator extends MasterValidator {

  /** The not null rule. */
  NotNullRule notNullRule = new NotNullRule();

  /** The max length rule. */
  MaximumLengthRule maxLengthRule = new MaximumLengthRule(100);

  /**
   * Instantiates a new ICD codes validator.
   */
  public IcdCodesValidator() {
    addInsertRule(notNullRule, new String[] {"code", "code_type", "code_desc"});
    addUpdateRule(notNullRule, new String[] {"code", "code_type", "code_desc"});
    addInsertRule(maxLengthRule, new String[] {"code"});
    addUpdateRule(maxLengthRule, new String[] {"code"});
  }
}
