package com.insta.hms.mdm.stockadjustmentreason;

import com.insta.hms.common.validation.NotNullRule;
import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.mdm.MasterValidator;

import org.springframework.stereotype.Component;

// TODO: Auto-generated Javadoc
/**
 * The Class StockAdjustmentReasonValidator.
 */
/*
 * Owner : Ashok Pal, 7th Aug 2017
 */
@Component
public class StockAdjustmentReasonValidator extends MasterValidator {
  
  /** The Constant NOT_NULL_FIELDS. */
  private static final String[] NOT_NULL_FIELDS = new String[] {"adjustment_reason"};
  
  /** The Constant NOT_NULL_RULE. */
  private static final ValidationRule NOT_NULL_RULE = new NotNullRule();

  /**
   * Instantiates a new stock adjustment reason validator.
   */
  public StockAdjustmentReasonValidator() {
    super();
    addDefaultRule(NOT_NULL_RULE, NOT_NULL_FIELDS);
  }
}
