package com.insta.hms.mdm.storeitemcontroltypes;

import com.insta.hms.common.validation.NotNullRule;
import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.mdm.MasterValidator;

import org.springframework.stereotype.Component;

/**
 * The Class StoreItemControlTypeValidator.
 */
@Component
public class StoreItemControlTypeValidator extends MasterValidator {
  
  /** The Constant NOT_NULL_FIELDS. */
  private static final String[] NOT_NULL_FIELDS = new String[] { "control_type_name" };
  
  /** The Constant NOT_NULL_RULE. */
  private static final ValidationRule NOT_NULL_RULE = new NotNullRule();

  /**
   * Instantiates a new store item control type validator.
   */
  public StoreItemControlTypeValidator() {
    super();
    addDefaultRule(NOT_NULL_RULE, NOT_NULL_FIELDS);
  }

}
