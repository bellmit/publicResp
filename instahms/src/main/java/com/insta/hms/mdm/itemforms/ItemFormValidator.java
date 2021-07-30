package com.insta.hms.mdm.itemforms;

import com.insta.hms.common.validation.NotNullRule;
import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.mdm.MasterValidator;

import org.springframework.stereotype.Component;

/**
 * The Class ItemFormValidator.
 *
 * @author irshadmohammed
 */
@Component
public class ItemFormValidator extends MasterValidator {

  /** The Constant NOT_NULL_FIELDS. */
  private static final String[] NOT_NULL_FIELDS = new String[] { "item_form_name" };

  /** The Constant NOT_NULL_RULE. */
  private static final ValidationRule NOT_NULL_RULE = new NotNullRule();

  /**
   * Instantiates a new item form validator.
   */
  public ItemFormValidator() {
    super();
    addDefaultRule(NOT_NULL_RULE, NOT_NULL_FIELDS);
  }
}
