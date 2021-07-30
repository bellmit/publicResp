package com.insta.hms.mdm.samplesources;

import com.insta.hms.common.validation.NotNullRule;
import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.mdm.MasterValidator;

import org.springframework.stereotype.Component;

/**
 * The Class SampleSourceValidator.
 */
@Component
public class SampleSourceValidator extends MasterValidator {
  
  /** The Constant NOT_NULL_FIELDS_INSERT. */
  private static final String[] NOT_NULL_FIELDS_INSERT = new String[] { "source_name" };
  
  /** The Constant NOT_NULL_FIELDS_UPDATE. */
  private static final String[] NOT_NULL_FIELDS_UPDATE = new String[] { "source_id", 
    "source_name" };
  
  /** The Constant NOT_NULL_RULE. */
  private static final ValidationRule NOT_NULL_RULE = new NotNullRule();

  /**
   * Instantiates a new sample source validator.
   */
  public SampleSourceValidator() {
    super();
    addInsertRule(NOT_NULL_RULE, NOT_NULL_FIELDS_INSERT);
    addUpdateRule(NOT_NULL_RULE, NOT_NULL_FIELDS_UPDATE);
  }
}
