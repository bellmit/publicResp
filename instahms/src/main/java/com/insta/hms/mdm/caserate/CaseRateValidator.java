package com.insta.hms.mdm.caserate;

import com.insta.hms.common.validation.NotNullRule;
import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.mdm.MasterDetailsValidator;
import org.springframework.stereotype.Component;

@Component
public class CaseRateValidator extends MasterDetailsValidator {

  /**
   * The Constant NOT_NULL_FIELDS_INSERT.
   */
  private static final String[] CASE_RATE_MAIN_NOT_NULL_FIELDS_INSERT = new String[] {
      "insurance_company_id", "network_type_id", "plan_id", "code_type", "code",
      "code_description", "case_rate_number",
      "status"};

  /**
   * The Constant NOT_NULL_FIELDS_INSERT.
   */
  private static final String[] CASE_RATE_DETAIL_NOT_NULL_FIELDS_INSERT = new String[] {
      "case_rate_id", "insurance_category_id", "amount"};

  /**
   * The Constant NOT_NULL_RULE_INSERT.
   */
  private static final ValidationRule NOT_NULL_RULE_INSERT = new NotNullRule();

  /**
   * The Constant NOT_NULL_FIELDS_UPDATE.
   */
  private static final String[] CASE_RATE_MAIN_NOT_NULL_FIELDS_UPDATE = new String[] {
      "case_rate_id"};

  /**
   * The Constant NOT_NULL_FIELDS_UPDATE.
   */
  private static final String[] CASE_RATE_DETAIL_NOT_NULL_FIELDS_UPDATE = new String[] {
      "case_rate_detail_id", "case_rate_id", "insurance_category_id", "amount"};

  /**
   * The Constant NOT_NULL_RULE_UPDATE.
   */
  private static final ValidationRule NOT_NULL_RULE_UPDATE = new NotNullRule();

  /**
   * Instantiates new case rate validator.
   */
  public CaseRateValidator() {
    super();
    addInsertRule(NOT_NULL_RULE_INSERT, CASE_RATE_MAIN_NOT_NULL_FIELDS_INSERT);
    addInsertRule("case_rate_detail", NOT_NULL_RULE_INSERT,
        CASE_RATE_DETAIL_NOT_NULL_FIELDS_INSERT);
    addUpdateRule(NOT_NULL_RULE_UPDATE, CASE_RATE_MAIN_NOT_NULL_FIELDS_UPDATE);
    addUpdateRule("case_rate_main", NOT_NULL_RULE_UPDATE, CASE_RATE_DETAIL_NOT_NULL_FIELDS_UPDATE);
  }
}
