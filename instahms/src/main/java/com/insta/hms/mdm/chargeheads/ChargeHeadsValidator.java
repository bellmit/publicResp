package com.insta.hms.mdm.chargeheads;

import com.insta.hms.common.validation.NotNullRule;
import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.mdm.MasterValidator;

import org.springframework.stereotype.Component;

/**
 * The Class ChargeHeadsValidator.
 */
@Component
public class ChargeHeadsValidator extends MasterValidator {

  /** The Constant NOT_NULL_FIELDS_INSERT. */
  private static final String[] NOT_NULL_FIELDS_INSERT = new String[] { "chargehead_id",
      "chargehead_name", "display_order", "account_head_id" };
  
  /** The Constant NOT_NULL_RULE_INSERT. */
  private static final ValidationRule NOT_NULL_RULE_INSERT = new NotNullRule();

  /** The Constant NOT_NULL_FIELDS_UPDATE. */
  private static final String[] NOT_NULL_FIELDS_UPDATE = new String[] { "chargehead_id",
      "chargehead_name", "display_order", "account_head_id" };
  
  /** The Constant NOT_NULL_RULE_UPDATE. */
  private static final ValidationRule NOT_NULL_RULE_UPDATE = new NotNullRule();

  /**
   * Instantiates a new charge heads validator.
   */
  public ChargeHeadsValidator() {
    super();
    addInsertRule(NOT_NULL_RULE_INSERT, NOT_NULL_FIELDS_INSERT);
    addUpdateRule(NOT_NULL_RULE_UPDATE, NOT_NULL_FIELDS_UPDATE);
  }

}
