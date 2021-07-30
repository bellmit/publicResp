package com.insta.hms.core.medicalrecords.codification.coderclaimreview;

import com.insta.hms.common.validation.NotNullRule;
import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.mdm.MasterValidator;

import org.springframework.stereotype.Component;

/**
 * The Class CoderClaimReviewValidator.
 */
@Component
public class CoderClaimReviewValidator extends MasterValidator {

  /** The Constant NOT_NULL_FIELDS_INSERT. */
  private static final String[] NOT_NULL_INSERT = new String[] { "patient_id",
      "title", "body", "created_by", "status" };

  /** The Constant NOT_NULL_RULE_INSERT. */
  private static final ValidationRule NOT_NULL_RULE_INSERT = new NotNullRule();

  /** The Constant NOT_NULL_FIELDS_UPDATE. */
  private static final String[] NOT_NULL_UPDATE = new String[] { "updated_by" };

  /** The Constant NOT_NULL_RULE_UPDATE. */
  private static final ValidationRule NOT_NULL_RULE_UPDATE = new NotNullRule();

  /** The Constant INSERT_RULESET_NAME. */
  public static final String INSERT_RULESET_NAME = "insert";

  /** The Constant UPDATE_RULESET_NAME. */
  public static final String UPDATE_RULESET_NAME = "update";

  /**
   * Instantiates a new coder claim review validator.
   */
  public CoderClaimReviewValidator() {
    addInsertRule(NOT_NULL_RULE_INSERT, NOT_NULL_INSERT);
    addUpdateRule(NOT_NULL_RULE_UPDATE, NOT_NULL_UPDATE);
  }
}
