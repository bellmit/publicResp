package com.insta.hms.mdm.coderclaimreview;

import com.insta.hms.common.validation.MaximumLengthRule;
import com.insta.hms.common.validation.NotNullRule;
import com.insta.hms.mdm.MasterDetailsValidator;

import org.springframework.stereotype.Component;

/**
 * The Class ReviewTypesValidator.
 */
@Component
public class ReviewTypesValidator extends MasterDetailsValidator {
  
  /** The Constant NOT_NULL_RULE. */
  private static final NotNullRule NOT_NULL_RULE = new NotNullRule();
  
  /** The Constant REVIEW_TYPE_LENGTH. */
  private static final MaximumLengthRule REVIEW_TYPE_LENGTH = new MaximumLengthRule(
      95);
  
  /** The Constant REVIEW_TITLE_LENGTH. */
  private static final MaximumLengthRule REVIEW_TITLE_LENGTH = new MaximumLengthRule(
      200);
  
  /** The Constant NOT_NULL_FIELDS_INSERT. */
  private static final String[] NOT_NULL_FIELDS_INSERT = { "review_type",
      "review_title", "review_content", "status", "review_category_id" };
  
  /** The Constant REVIEW_TYPE. */
  private static final String[] REVIEW_TYPE = new String[] { "review_type" };
  
  /** The Constant REVIEW_TITLE. */
  private static final String[] REVIEW_TITLE = new String[] { "review_title" };

  /**
   * Instantiates a new review types validator.
   *
   * @param reviewCategoryRule the review category rule
   */
  public ReviewTypesValidator(ReviewCategoryRule reviewCategoryRule) {
    super();
    addInsertRule(reviewCategoryRule, new String[] { "review_category_id" });
    addInsertRule(NOT_NULL_RULE, NOT_NULL_FIELDS_INSERT);
    addInsertRule(REVIEW_TITLE_LENGTH, REVIEW_TITLE);
    addInsertRule(REVIEW_TYPE_LENGTH, REVIEW_TYPE);

    addUpdateRule(REVIEW_TITLE_LENGTH, REVIEW_TITLE);
    addUpdateRule(REVIEW_TYPE_LENGTH, REVIEW_TYPE);
    addUpdateRule(reviewCategoryRule, new String[] { "review_category_id" });
    addUpdateRule(NOT_NULL_RULE, NOT_NULL_FIELDS_INSERT);
  }

}
