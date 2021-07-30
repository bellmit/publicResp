package com.insta.hms.integration.insurance.remittance;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * The Enum ActivityErrorType used to store error messages and codes at activity level for
 * remittance validation.
 */
public enum ActivityErrorType {

  /** The invalid activity. */
  INVALID_ACTIVITY(1, "XML parsing failed: Invalid activity found. Activity Id is "
      + "not prefixed with A or P or not a valid activity id."),

  /** The invalid resub batch id. */
  INVALID_RESUB_BATCH_ID(2, "XML parsing failed: Invalid activity found. Resubmitted "
      + "Activity Id is not suffixed a valid resubmission batch id."),

  /** The invalid activity id. */
  INVALID_ACTIVITY_ID(4, "XML parsing failed: ID for Activity not valid in Claim"),

  /** The invalid sale item id. */
  INVALID_SALE_ITEM_ID(16, "XML parsing failed: Invalid activity found, Activity Id prefixed "
      + "with P does not have sale item id."),

  /** The invalid denial code. */
  INVALID_DENIAL_CODE(32, "XML parsing failed: Denial Code value not valid for "
      + "Activity in Claim"),

  /** The activity id not found. */
  ACTIVITY_ID_NOT_FOUND(64, "XML parsing failed: ID not found for Activity in Claim"),

  /** The invalid start date. */
  INVALID_START_DATE(128, "XML parsing failed: Start date not found for Activity in Claim"),

  /** The type value not found. */
  TYPE_VALUE_NOT_FOUND(256, "XML parsing failed: Type value not found for Activity in Claim"),

  /** The code value not found. */
  CODE_VALUE_NOT_FOUND(512, "XML parsing failed: Code value not valid for Claim"),

  /** The quantity value not found. */
  QUANTITY_VALUE_NOT_FOUND(1024, "XML parsing failed: Quantity value not found for Claim"),

  /** The net value not found. */
  NET_VALUE_NOT_FOUND(2048, "XML parsing failed: Net value not found for Activity"),

  /** The clinician value not found. */
  CLINICIAN_VALUE_NOT_FOUND(4096, "XML parsing failed: Clinician value not found for Claim"),

  /** The payment value not found. */
  PAYMENT_VALUE_NOT_FOUND(8192, "XML parsing failed: Payment value not found for Activity");

  /** The code. */
  private final Integer code;

  /** The message. */
  private final String message;

  /**
   * Instantiates a new activity error type.
   *
   * @param code the code
   * @param message the message
   */
  private ActivityErrorType(int code, String message) {
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
  private static final Map<Integer, ActivityErrorType> lookup =
      new HashMap<Integer, ActivityErrorType>();
  static {
    for (ActivityErrorType errorType : EnumSet.allOf(ActivityErrorType.class)) {
      lookup.put(errorType.getCode(), errorType);
    }
  }

  /**
   * Gets the error code.
   *
   * @param code the code
   * @return the error code
   */
  public static ActivityErrorType getErrorCode(int code) {
    return lookup.get(code);
  }

}

