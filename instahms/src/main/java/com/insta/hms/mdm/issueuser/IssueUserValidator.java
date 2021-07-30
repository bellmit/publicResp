package com.insta.hms.mdm.issueuser;

/*
 * Owner : Ashok Pal, 7th Aug 2017
 */
import com.insta.hms.common.validation.NotNullRule;
import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.mdm.MasterValidator;

import org.springframework.stereotype.Component;

/**
 * The Class IssueUserValidator.
 */
@Component
public class IssueUserValidator extends MasterValidator {

  /** The Constant NOT_NULL_RULE. */
  private static final ValidationRule NOT_NULL_RULE = new NotNullRule();

  /**
   * Instantiates a new issue user validator.
   */
  public IssueUserValidator() {
    super();
    // addDefaultRule(NOT_NULL_RULE,null);
  }

}
