package com.insta.hms.mdm.itemgroups;

import com.insta.hms.common.validation.NotNullRule;
import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.mdm.MasterValidator;

import org.springframework.stereotype.Component;

// TODO: Auto-generated Javadoc
/** The Class ItemGroupsValidator. */
@Component
public class ItemGroupsValidator extends MasterValidator {

  /** The Constant NOT_NULL_RULE. */
  private static final ValidationRule NOT_NULL_RULE = new NotNullRule();

  /** The Constant NOT_NULL_FIELDS_INSERT. */
  private static final String[] NOT_NULL_FIELDS_INSERT =
      new String[] {"item_group_name", "group_code", "item_group_type_id"};

  /** The Constant NOT_NULL_FIELDS_UPDATE. */
  private static final String[] NOT_NULL_FIELDS_UPDATE =
      new String[] {"item_group_name", "group_code", "item_group_type_id"};

  /**
   * Instantiates a new item groups validator.
   */
  public ItemGroupsValidator() {
    super();
    /* INSERT VALIDATION RULES. */
    addInsertRule(NOT_NULL_RULE, NOT_NULL_FIELDS_INSERT);

    /* UPDATE VALIDATION RULES */
    addUpdateRule(NOT_NULL_RULE, NOT_NULL_FIELDS_UPDATE);
  }
}
