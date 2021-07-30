package com.insta.hms.mdm.storetypes;

import com.insta.hms.common.validation.MaximumLengthRule;
import com.insta.hms.common.validation.NotNullRule;
import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.mdm.MasterValidator;

import org.springframework.stereotype.Component;

@Component
class StoreTypeValidator extends MasterValidator {

  private final ValidationRule notnullrule = new NotNullRule();

  private final ValidationRule maximumlengthrule = new MaximumLengthRule(100);

  private final String[] notnullfieldsinsert = new String[] { "store_type_name" };

  private final String[] notnullfieldsupdate = new String[] { "store_type_name", "store_type_id" };

  private final String[] maximumlengthfields = new String[] { "store_type_name" };

  /**
   * Instantiates a new store validator.
   */

  public StoreTypeValidator() {
    super();
    addInsertRule(notnullrule, notnullfieldsinsert);
    addInsertRule(maximumlengthrule, maximumlengthfields);

    addUpdateRule(notnullrule, notnullfieldsupdate);
    addUpdateRule(maximumlengthrule, maximumlengthfields);

  }
}
