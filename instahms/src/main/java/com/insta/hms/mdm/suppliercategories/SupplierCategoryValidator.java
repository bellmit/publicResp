package com.insta.hms.mdm.suppliercategories;

import com.insta.hms.common.validation.NotNullRule;
import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.mdm.MasterValidator;

import org.springframework.stereotype.Component;

/**
 * The Class SupplierCategoryValidator.
 */
@Component
public class SupplierCategoryValidator extends MasterValidator {

  /** The notnullfields. */
  private final String[] notnullfields = new String[] { "supp_category_name" };

  /** The notnullrule. */
  private final ValidationRule notnullrule = new NotNullRule();

  /**
   * Instantiates a new supplier category validator.
   */
  public SupplierCategoryValidator() {
    super();
    addDefaultRule(notnullrule, notnullfields);
  }
}
