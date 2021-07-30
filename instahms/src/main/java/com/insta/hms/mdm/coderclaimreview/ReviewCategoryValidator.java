package com.insta.hms.mdm.coderclaimreview;

import com.insta.hms.common.validation.MaximumLengthRule;
import com.insta.hms.common.validation.NotNullRule;
import com.insta.hms.mdm.MasterDetailsValidator;

import org.springframework.stereotype.Component;

/**
 * The Class ReviewCategoryValidator.
 */
@Component
public class ReviewCategoryValidator extends MasterDetailsValidator {

  /** The Constant NOT_NULL_RULE. */
  private static final NotNullRule NOT_NULL_RULE = new NotNullRule();

  /** The Constant CATEGORY_NAME_LENGTH. */
  private static final MaximumLengthRule CATEGORY_NAME_LENGTH = new MaximumLengthRule(
      50);

  /** The Constant NOT_NULL_FIELDS_INSERT. */
  private static final String[] NOT_NULL_FIELDS_INSERT = { "category_name",
      "category_type", "send_email", "status" };

  /** The Constant CATEGORY_NAME. */
  private static final String[] CATEGORY_NAME = new String[] {
      "category_name" };

  /**
   * Instantiates a new review category validator.
   */
  public ReviewCategoryValidator() {
    super();
    addInsertRule(NOT_NULL_RULE, NOT_NULL_FIELDS_INSERT);
    addInsertRule(CATEGORY_NAME_LENGTH, CATEGORY_NAME);

    addUpdateRule(NOT_NULL_RULE, NOT_NULL_FIELDS_INSERT);
    addUpdateRule(CATEGORY_NAME_LENGTH, CATEGORY_NAME);

  }
}
