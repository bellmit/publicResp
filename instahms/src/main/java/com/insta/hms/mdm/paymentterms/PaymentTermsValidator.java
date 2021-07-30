package com.insta.hms.mdm.paymentterms;

import com.insta.hms.common.validation.NotNullRule;
import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.mdm.MasterValidator;

import org.springframework.stereotype.Component;

/**
 * The Class PaymentTermsValidator.
 *
 * @author ashokkumar
 */
@Component
public class PaymentTermsValidator extends MasterValidator {

  /** The Constant NOT_NULL_FIELDS. */
  private static final String[] NOT_NULL_FIELDS = new String[] { "template_name",
      "terms_conditions" };

  /** The Constant NOT_NULL_RULE. */
  private static final ValidationRule NOT_NULL_RULE = new NotNullRule();

  /**
   * Instantiates a new payment terms validator.
   */
  public PaymentTermsValidator() {
    super();
    addDefaultRule(NOT_NULL_RULE, NOT_NULL_FIELDS);
  }

}
