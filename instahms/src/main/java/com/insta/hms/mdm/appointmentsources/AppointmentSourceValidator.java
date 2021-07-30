package com.insta.hms.mdm.appointmentsources;

import com.insta.hms.common.validation.NotNullRule;
import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.mdm.MasterValidator;

import org.springframework.stereotype.Component;

// TODO: Auto-generated Javadoc
/**
 * The Class AppointmentSourceValidator.
 */
@Component
public class AppointmentSourceValidator extends MasterValidator {
  
  /** The Constant NOT_NULL_FIELDS_UPDATE. */
  private static final String[] NOT_NULL_FIELDS_UPDATE = new String[] { "appointment_source_id",
      "appointment_source_name", "status", "paid_at_source" };

  /** The Constant NOT_NULL_FIELDS_INSERT. */
  private static final String[] NOT_NULL_FIELDS_INSERT = new String[] { "appointment_source_name",
      "status", "paid_at_source" };

  /** The Constant NOT_NULL_RULE. */
  private static final ValidationRule NOT_NULL_RULE = new NotNullRule();

  /**
   * Instantiates a new appointment source validator.
   */
  public AppointmentSourceValidator() {
    addInsertRule(NOT_NULL_RULE, NOT_NULL_FIELDS_INSERT);
    addUpdateRule(NOT_NULL_RULE, NOT_NULL_FIELDS_UPDATE);
  }
}