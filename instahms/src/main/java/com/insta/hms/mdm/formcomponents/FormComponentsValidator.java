package com.insta.hms.mdm.formcomponents;

import com.insta.hms.common.validation.NotNullRule;
import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.mdm.MasterValidator;

import org.springframework.stereotype.Component;

/**
 * The Class FormComponentsValidator.
 */
@Component
public class FormComponentsValidator extends MasterValidator {

  /** The Constant NOT_NULL_FIELDS_INSERT. */
  private static final String[] NOT_NULL_FIELDS_INSERT = new String[] { "form_type", "form_name" };

  /** The Constant NOT_NULL_FIELDS_UPDATE. */
  private static final String[] NOT_NULL_FIELDS_UPDATE = new String[] { "id", "form_type",
      "form_name" };

  /** The Constant NOT_NULL_RULE_INSERT. */
  private static final ValidationRule NOT_NULL_RULE_INSERT = new NotNullRule();

  /** The Constant NOT_NULL_RULE_UPDATE. */
  private static final ValidationRule NOT_NULL_RULE_UPDATE = new NotNullRule();

  /**
   * Instantiates a new form components validator.
   */
  public FormComponentsValidator() {
    addInsertRule(NOT_NULL_RULE_INSERT, NOT_NULL_FIELDS_INSERT);
    addUpdateRule(NOT_NULL_RULE_UPDATE, NOT_NULL_FIELDS_UPDATE);
  }
}
