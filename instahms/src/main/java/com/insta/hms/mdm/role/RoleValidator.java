package com.insta.hms.mdm.role;

import com.insta.hms.common.validation.MaximumLengthRule;
import com.insta.hms.common.validation.NotNullRule;
import com.insta.hms.mdm.MasterValidator;

import org.springframework.stereotype.Component;

/**
 * The Class RoleValidator.
 */
@Component
public class RoleValidator extends MasterValidator {

  /** The Constant NOT_NULL_RULE. */
  private static final NotNullRule NOT_NULL_RULE = new NotNullRule();

  /** The Constant ROLE_NAME_MAX_LENGTH. */
  private static final MaximumLengthRule ROLE_NAME_MAX_LENGTH = new MaximumLengthRule(
      25);

  /** The Constant ROLE_STATUS_MAX_LENGTH. */
  private static final MaximumLengthRule ROLE_STATUS_MAX_LENGTH = new MaximumLengthRule(
      1);

  /** The Constant STATS_ITEM_MAX_LENGTH. */
  private static final MaximumLengthRule STATS_ITEM_MAX_LENGTH = new MaximumLengthRule(
      80);

  /** The Constant NOT_NULL_FIELDS. */
  private static final String[] NOT_NULL_FIELDS = new String[] { "role_name",
      "role_status" };

  /** The Constant ROLE_NAME. */
  private static final String[] ROLE_NAME = new String[] { "role_name" };

  /** The Constant ROLE_STATUS. */
  private static final String[] ROLE_STATUS = new String[] { "role_status" };

  /** The Constant STATS_FIELDS. */
  private static final String[] STATS_FIELDS = new String[] { "stats_item1",
      "stats_item2", "stats_item3", "stats_item4", "stats_item5", "stats_item6",
      "stats_item7", "stats_item8", "stats_item9", "stats_item10",
      "stats_item11", "stats_item12" };

  /**
   * Instantiates a new role validator.
   */
  public RoleValidator() {
    super();
    addInsertRule(NOT_NULL_RULE, NOT_NULL_FIELDS);
    addInsertRule(ROLE_NAME_MAX_LENGTH, ROLE_NAME);
    addInsertRule(ROLE_STATUS_MAX_LENGTH, ROLE_STATUS);
    addInsertRule(STATS_ITEM_MAX_LENGTH, STATS_FIELDS);

    addUpdateRule(NOT_NULL_RULE, NOT_NULL_FIELDS);
    addUpdateRule(ROLE_NAME_MAX_LENGTH, ROLE_NAME);
    addUpdateRule(ROLE_STATUS_MAX_LENGTH, ROLE_STATUS);
    addUpdateRule(STATS_ITEM_MAX_LENGTH, STATS_FIELDS);
  }
}
