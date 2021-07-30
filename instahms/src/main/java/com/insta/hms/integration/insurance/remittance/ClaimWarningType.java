package com.insta.hms.integration.insurance.remittance;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * The Enum ClaimWarningType.
 */
public enum ClaimWarningType {

  /** The claim id not found skipped. */
  CLAIM_ID_NOT_FOUND_SKIPPED(1,
      "XML parsing warning: Matching Claim ID not found in system. Skipped processing.");

  /** The code. */
  private final Integer code;

  /** The message. */
  private final String message;

  /**
   * Instantiates a new claim warning type.
   *
   * @param code the code
   * @param message the message
   */
  private ClaimWarningType(int code, String message) {
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
  private static final Map<Integer, ClaimWarningType> lookup =
      new HashMap<Integer, ClaimWarningType>();
  static {
    for (ClaimWarningType warningType : EnumSet.allOf(ClaimWarningType.class)) {
      lookup.put(warningType.getCode(), warningType);
    }
  }

  /**
   * Gets the warning code.
   *
   * @param code the code
   * @return the warning code
   */
  public static ClaimWarningType getWarningCode(int code) {
    return lookup.get(code);
  }
}
