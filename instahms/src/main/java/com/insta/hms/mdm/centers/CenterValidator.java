package com.insta.hms.mdm.centers;

import com.insta.hms.common.validation.MaximumLengthRule;
import com.insta.hms.common.validation.NotNullRule;
import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.mdm.MasterValidator;

import org.springframework.stereotype.Component;

/**
 * The Class CenterValidator.
 *
 * @author yashwant
 */
@Component
class CenterValidator extends MasterValidator {

  /** The Constant NOT_NULL_RULE. */
  private static final ValidationRule NOT_NULL_RULE = new NotNullRule();

  /** The Constant MAXIMUM_LENGTH_RULE. */
  private static final ValidationRule MAXIMUM_LENGTH_RULE = new MaximumLengthRule(200);

  /** The Constant MAXIMUM_LENGTH_RULE_100. */
  private static final ValidationRule MAXIMUM_LENGTH_RULE_100 = new MaximumLengthRule(100);

  /** The Constant NOT_NULL_FIELDS_INSERT. */
  private static final String[] NOT_NULL_FIELDS_INSERT = new String[] { "center_name",
      "center_code", "status", "city_id", "state_id", "country_id" };

  /** The Constant NOT_NULL_FIELDS_UPDATE. */
  private static final String[] NOT_NULL_FIELDS_UPDATE = new String[] { "center_name",
      "center_code", "status", "center_id", "city_id", "state_id", "country_id" };

  /** The Constant MAXIMUM_LENGTH_FIELDS. */
  private static final String[] MAXIMUM_LENGTH_FIELDS = new String[] { "center_address",
      "accounting_company_name", "hospital_center_service_reg_no" };

  /** The Constant MAXIMUM_LENGTH_FIELDS_100. */
  private static final String[] MAXIMUM_LENGTH_FIELDS_100 = new String[] { "center_name" };

  /**
   * Instantiates a new center validator.
   */
  CenterValidator() {
    addInsertRule(NOT_NULL_RULE, NOT_NULL_FIELDS_INSERT);
    addInsertRule(MAXIMUM_LENGTH_RULE, MAXIMUM_LENGTH_FIELDS);
    addInsertRule(MAXIMUM_LENGTH_RULE_100, MAXIMUM_LENGTH_FIELDS_100);

    addUpdateRule(NOT_NULL_RULE, NOT_NULL_FIELDS_UPDATE);
    addUpdateRule(MAXIMUM_LENGTH_RULE, MAXIMUM_LENGTH_FIELDS);
    addUpdateRule(MAXIMUM_LENGTH_RULE_100, MAXIMUM_LENGTH_FIELDS_100);
  }

}
