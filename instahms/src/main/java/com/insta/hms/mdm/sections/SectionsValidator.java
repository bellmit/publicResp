package com.insta.hms.mdm.sections;

import com.insta.hms.common.validation.NotNullRule;
import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.mdm.MasterValidator;

import org.springframework.stereotype.Component;

/**
 * The Class SectionsValidator.
 */
@Component
public class SectionsValidator extends MasterValidator {
  
  /** The Constant NOT_NULL_FIELDS. */
  private static final String[] NOT_NULL_FIELDS = new String[] {"section_title"};
  
  /** The Constant NOT_NULL_RULE. */
  private static final ValidationRule NOT_NULL_RULE = new NotNullRule();

  /**
   * Instantiates a new sections validator.
   */
  public SectionsValidator() {
    super();
    addDefaultRule(NOT_NULL_RULE, NOT_NULL_FIELDS);
  }
}
