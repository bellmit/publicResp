package com.insta.hms.mdm.prescriptionsdeclinedreasonmaster;

import com.insta.hms.mdm.MasterValidator;
import com.insta.hms.mdm.coderclaimreview.ReviewCategoryRule;

import org.springframework.stereotype.Component;

/**
 * The Class ReviewTypesValidator.
 */
@Component
public class PrescDeclinedReasonValidator extends MasterValidator {
 
  /**
   * Instantiates a new review types validator.
   *
   * @param reviewCategoryRule the review category rule
   */
  public PrescDeclinedReasonValidator(ReviewCategoryRule reviewCategoryRule) {
    super(); 
  }

}
