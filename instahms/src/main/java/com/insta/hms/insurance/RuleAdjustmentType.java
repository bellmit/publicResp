package com.insta.hms.insurance;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * The Enum RuleAdjustmentType.
 */
/*
 * For Task 53548. There is a value corresponding to each Cat & Visit Insurance Rule where the
 * adjustment may not have been completed. Each enum value is associated with a code. During rule
 * processing for each such rule, a bit is set corresponding to the code if the adjustment could not
 * be completed for the rule. This will usually happen if some items in the category / visit are
 * locked.
 *
 * TODO: - Check if values are added for all rules - Replace with appropriate messages
 */
public enum RuleAdjustmentType {
  // TODO: set correct property from application.properties
  /*
   * CATEGORY_DEDUCTIBLE_ADJ(1,
   * MessageResources.getMessageResources("java.resources.application").getMessage(
   * "category.deductible_adj")), CATEGORY_MAX_COPAY_ADJ(2,
   * MessageResources.getMessageResources("java.resources.application").getMessage(
   * "category.max.copay.adj")), CATEGORY_SPONSOR_LIMIT_ADJ(4,
   * MessageResources.getMessageResources("java.resources.application").getMessage(
   * "category.sponsor.limit.adj")), VISIT_DEDUCTIBLE_ADJ(8,
   * MessageResources.getMessageResources("java.resources.application").getMessage(
   * "visit.deductible.adj")), VISIT_MAX_COPAY_ADJ(16,
   * MessageResources.getMessageResources("java.resources.application").getMessage(
   * "visit.max.copay.adj")), VISIT_PER_DAY_LIMIT_ADJ(32,
   * MessageResources.getMessageResources("java.resources.application").getMessage(
   * "viist.per.day.limit.adj")), VISIT_SPONSOR_LIMIT_ADJ(64,
   * MessageResources.getMessageResources("java.resources.application").getMessage(
   * "visit.sponsor.limit.adj"));
   */

  /** The category deductible adj. */
  CATEGORY_DEDUCTIBLE_ADJ(1, "Category Deductible"),
  /** The category max copay adj. */
  CATEGORY_MAX_COPAY_ADJ(2, "Category Max Copay"),
  /** The category sponsor limit adj. */
  CATEGORY_SPONSOR_LIMIT_ADJ(4, "Category Sponsor Limit"),
  /** The visit deductible adj. */
  VISIT_DEDUCTIBLE_ADJ(8, "Visit Deductible"),
  /** The visit max copay adj. */
  VISIT_MAX_COPAY_ADJ(16, "Visit Max Copay"),
  /** The visit per day limit adj. */
  VISIT_PER_DAY_LIMIT_ADJ(32, "Visit Per Day Limit"),
  /** The visit sponsor limit adj. */
  VISIT_SPONSOR_LIMIT_ADJ(64, "Visit Sponsor Limit");

  /** The code. */
  private final Integer code;

  /** The message. */
  private final String message;

  /**
   * Instantiates a new rule adjustment type.
   *
   * @param code    the i
   * @param message the message
   */
  private RuleAdjustmentType(int code, String message) {
    this.code = code;
    this.message = message;
  }

  /**
   * Gets the code.
   *
   * @return the code
   */
  public Integer getCode() {
    return this.code;
  }

  /**
   * Gets the message.
   *
   * @return the message
   */
  public String getMessage() {
    return this.message;
  }

  /** The Constant lookup. */
  private static final Map<Integer, RuleAdjustmentType> lookup = 
      new HashMap<Integer, RuleAdjustmentType>();
  static {
    for (RuleAdjustmentType r : EnumSet.allOf(RuleAdjustmentType.class)) {
      lookup.put(r.getCode(), r);
    }
  }

  /**
   * Gets the adjustment type.
   *
   * @param code the code
   * @return the adjustment type
   */
  public static RuleAdjustmentType getAdjustmentType(int code) {
    return lookup.get(code);
  }

}
