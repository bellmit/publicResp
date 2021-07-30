package com.insta.hms.core.medicalrecords.codification.coderclaimreview;

import com.insta.hms.common.validation.NotNullRule;
import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.mdm.MasterValidator;

/**
 * The Class CodificationMessageTypesValidator.
 */
import org.springframework.stereotype.Component;

@Component
public class CodificationMessageTypesValidator extends MasterValidator {

  /** The Constant NOT_NULL_FIELDS_INSERT. */
  private static final String[] NOT_NULL_FIELDS_INSERT = new String[] { "review_type",
      "review_type", "review_type" };

  /** The Constant NOT_NULL_RULE_INSERT. */
  private static final ValidationRule NOT_NULL_RULE_INSERT = new NotNullRule();

  /** The Constant NOT_NULL_FIELDS_UPDATE. */
  private static final String[] NOT_NULL_FIELDS_UPDATE = new String[] { "review_type_id",
      "review_type", "review_type", "review_type" };

  /** The Constant NOT_NULL_RULE_UPDATE. */
  private static final ValidationRule NOT_NULL_RULE_UPDATE = new NotNullRule();

  /**
   * Instantiates a new codification message types validator.
   */
  public CodificationMessageTypesValidator() {
    super();
    addInsertRule(NOT_NULL_RULE_INSERT, NOT_NULL_FIELDS_INSERT);
    addUpdateRule(NOT_NULL_RULE_UPDATE, NOT_NULL_FIELDS_UPDATE);
  }

}
