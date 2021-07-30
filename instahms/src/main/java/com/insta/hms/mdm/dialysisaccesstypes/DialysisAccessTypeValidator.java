package com.insta.hms.mdm.dialysisaccesstypes;

import com.insta.hms.common.validation.NotNullRule;
import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.mdm.MasterValidator;

import org.springframework.stereotype.Component;

/**
 * The Class DialysisAccessTypeValidator.
 */
@Component
public class DialysisAccessTypeValidator extends MasterValidator {
  
  /** The Constant NOT_NULL_FIELDS_UPDATE. */
  private static final String[] NOT_NULL_FIELDS_UPDATE = new String[] { "access_type_id",
      "access_type", "access_category" };

  /** The Constant NOT_NULL_FIELDS_INSERT. */
  private static final String[] NOT_NULL_FIELDS_INSERT = new String[] { "access_type",
      "access_category" };

  /** The Constant NOT_NULL_RULE. */
  private static final ValidationRule NOT_NULL_RULE = new NotNullRule();

  /**
   * Instantiates a new dialysis access type validator.
   */
  public DialysisAccessTypeValidator() {
    addUpdateRule(NOT_NULL_RULE, NOT_NULL_FIELDS_UPDATE);
    addInsertRule(NOT_NULL_RULE, NOT_NULL_FIELDS_INSERT);
  }
}
