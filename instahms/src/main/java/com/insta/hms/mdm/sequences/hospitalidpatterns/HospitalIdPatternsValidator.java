package com.insta.hms.mdm.sequences.hospitalidpatterns;

import com.insta.hms.common.validation.NotNullRule;
import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.mdm.MasterValidator;

import org.springframework.stereotype.Component;

/**
 * The Class HospitalIdPatternsValidator.
 */
@Component
public class HospitalIdPatternsValidator extends MasterValidator {

  /** The Constant NOT_NULL_FIELDS_INSERT. */
  private static final String[] NOT_NULL_FIELDS_INSERT =
      new String[] {"pattern_id", "sequence_name"};
  
  /** The Constant NOT_NULL_RULE_INSERT. */
  private static final ValidationRule NOT_NULL_RULE_INSERT = new NotNullRule();

  /** The Constant NOT_NULL_FIELDS_UPDATE. */
  private static final String[] NOT_NULL_FIELDS_UPDATE =
      new String[] {"pattern_id", "sequence_name"};
  
  /** The Constant NOT_NULL_RULE_UPDATE. */
  private static final ValidationRule NOT_NULL_RULE_UPDATE = new NotNullRule();

  /**
   * Instantiates a new hospital id patterns validator.
   */
  public HospitalIdPatternsValidator() {
    super();
    addInsertRule(NOT_NULL_RULE_INSERT, NOT_NULL_FIELDS_INSERT);
    addUpdateRule(NOT_NULL_RULE_UPDATE, NOT_NULL_FIELDS_UPDATE);
  }
}
