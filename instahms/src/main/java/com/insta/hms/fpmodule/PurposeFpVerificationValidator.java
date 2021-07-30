package com.insta.hms.fpmodule;

import com.insta.hms.common.validation.NotNullRule;
import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.mdm.MasterValidator;

import org.springframework.stereotype.Component;

@Component
public class PurposeFpVerificationValidator extends MasterValidator {

  private static final String[] NOT_NULL_FIELDS_INSERT = new String[] { "purpose" };
  private static final ValidationRule NOT_NULL_RULE_INSERT = new NotNullRule();

  private static final String[] NOT_NULL_FIELDS_UPDATE = new String[] { "purpose_id", "purpose" };
  private static final ValidationRule NOT_NULL_RULE_UPDATE = new NotNullRule();

  /**
   * Instantiates a new purpose FP verification validator.
   */
  public PurposeFpVerificationValidator() {
    super();
    addInsertRule(NOT_NULL_RULE_INSERT, NOT_NULL_FIELDS_INSERT);
    addUpdateRule(NOT_NULL_RULE_UPDATE, NOT_NULL_FIELDS_UPDATE);
  }

}
