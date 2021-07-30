package com.insta.hms.mdm.taxsubgroups;

import com.insta.hms.common.validation.NotNullRule;
import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.mdm.MasterValidator;

import org.springframework.stereotype.Component;

/**
 * The Class TaxSubGroupValidator.
 */
@Component
public class TaxSubGroupValidator extends MasterValidator {

  /** The not null fields insert. */
  private static final  String[] notnullfieldsinsert = new String[] { "item_subgroup_name",
      "item_group_id", "status" };
  
  /** The not null rule insert. */
  private static final  ValidationRule notnullruleinsert = new NotNullRule();

  /** The not null fields update. */
  private static final  String[] notnullfieldsupdate = new String[] { "item_subgroup_id",
      "item_subgroup_name", "item_group_id", "status" };
  
  /** The not null rule update. */
  private static final  ValidationRule notnullruleupdate = new NotNullRule();

  /**
   * Instantiates a new tax sub group validator.
   */
  public TaxSubGroupValidator() {
    super();
    addInsertRule(notnullruleinsert, notnullfieldsinsert);
    addUpdateRule(notnullruleupdate, notnullfieldsupdate);
  }

}
