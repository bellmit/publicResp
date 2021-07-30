package com.insta.hms.mdm.insuranceplandetails;

import com.insta.hms.common.validation.NotNullRule;
import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.mdm.MasterValidator;

import org.springframework.stereotype.Component;

/**
 * The Class InsurancePlanDetailsValidator.
 */
@Component
public class InsurancePlanDetailsValidator extends MasterValidator {

  /** The Constant NOT_NULL_FIELDS_INSERT. */
  private static final  String[] NOT_NULL_FIELDS_INSERT = new String[] { "insurance_category_id",
      "patient_type", "plan_id", "username", "category_payable" };

  /** The Constant NOT_NULL_RULE_INSERT. */
  private static final  ValidationRule NOT_NULL_RULE_INSERT = new NotNullRule();

  /** The Constant NOT_NULL_FIELDS_UPDATE. */
  private static final  String[] NOT_NULL_FIELDS_UPDATE = new String[] {
      "insurance_plan_details_id", "insurance_category_id", "patient_type", "plan_id", "username",
      "category_payable" };

  /** The Constant NOT_NULL_RULE_UPDATE. */
  private static final  ValidationRule NOT_NULL_RULE_UPDATE = new NotNullRule();

  /**
   * Instantiates a new insurance plan details validator.
   */
  public InsurancePlanDetailsValidator() {
    super();
    addInsertRule(NOT_NULL_RULE_INSERT, NOT_NULL_FIELDS_INSERT);
    addUpdateRule(NOT_NULL_RULE_UPDATE, NOT_NULL_FIELDS_UPDATE);
  }

}
