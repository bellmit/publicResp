package com.insta.hms.mdm.dialyzertypes;

import com.insta.hms.common.validation.NotNullRule;
import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.mdm.MasterValidator;

import org.springframework.stereotype.Component;

/**
 * The Class DialyzerTypeValidator.
 */
@Component
public class DialyzerTypeValidator extends MasterValidator {

  /** The Constant NOT_NULL_FIELDS_UPDATE. */
  private static final String[] NOT_NULL_FIELDS_UPDATE = new String[] { "dialyzer_type_id",
      "dialyzer_type_name" };

  /** The Constant NOT_NULL_FIELDS_INSERT. */
  private static final String[] NOT_NULL_FIELDS_INSERT = new String[] { "dialyzer_type_name" };

  /** The Constant NOT_NULL_RULE. */
  private static final ValidationRule NOT_NULL_RULE = new NotNullRule();

  /**
   * Instantiates a new dialyzer type validator.
   */
  public DialyzerTypeValidator() {
    addUpdateRule(NOT_NULL_RULE, NOT_NULL_FIELDS_UPDATE);
    addInsertRule(NOT_NULL_RULE, NOT_NULL_FIELDS_INSERT);
  }
}
