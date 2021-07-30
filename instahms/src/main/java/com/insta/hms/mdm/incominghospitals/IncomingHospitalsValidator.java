package com.insta.hms.mdm.incominghospitals;

import com.insta.hms.common.validation.NotNullRule;
import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.mdm.MasterValidator;

import org.springframework.stereotype.Component;

/**
 * The Class IncomingHospitalsValidator.
 */
@Component
public class IncomingHospitalsValidator extends MasterValidator {
  
  /** The Constant NOT_NULL_FIELDS_INSERT. */
  private static final String[] NOT_NULL_FIELDS_INSERT = new String[] {"hospital_name"};
  
  /** The Constant NOT_NULL_RULE. */
  private static final ValidationRule NOT_NULL_RULE = new NotNullRule();

  /** The Constant NOT_NULL_FIELDS_UPDATE. */
  private static final String[] NOT_NULL_FIELDS_UPDATE =
      new String[] {"hospital_id", "hospital_name"};

  /**
   * Instantiates a new incoming hospitals validator.
   */
  public IncomingHospitalsValidator() {
    super();
    addInsertRule(NOT_NULL_RULE, NOT_NULL_FIELDS_INSERT);
    addUpdateRule(NOT_NULL_RULE, NOT_NULL_FIELDS_UPDATE);
  }
}
