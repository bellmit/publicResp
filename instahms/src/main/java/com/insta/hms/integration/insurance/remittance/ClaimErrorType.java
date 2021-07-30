package com.insta.hms.integration.insurance.remittance;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * The Enum ClaimErrorType used to store error messages and codes at claim level for remittance
 * validation.
 */
public enum ClaimErrorType {

  /** The no id claim found. */
  NO_ID_CLAIM_FOUND(1, "XML parsing failed: ID not found for Claim"),

  /** The invalid claim id. */
  INVALID_CLAIM_ID(2, "XML parsing failed: Invalid Claim ID (or) No Claim exists with Claim ID"),

  /** The invalid batch id. */
  INVALID_BATCH_ID(4, "XML parsing failed: Invalid Claim ID. "
      + "No Submission Batch ID found for the Claim..."),

  /** The invalid batch not sent. */
  INVALID_BATCH_NOT_SENT(8, "XML parsing failed: Submission Batch is not marked as Sent."
      + " Please mark it as Sent and Upload again"),

  /** The id payer not found. */
  ID_PAYER_NOT_FOUND(16, "XML parsing failed: IDPayer not found for Claim"),

  /** The id provider not found. */
  ID_PROVIDER_NOT_FOUND(32, "XML parsing failed: ProviderID not found for Claim "),

  /** The payment ref not found. */
  PAYMENT_REF_NOT_FOUND(64, "XML parsing failed: Payment Reference not found for Claim "),

  /** The activity not found. */
  ACTIVITY_NOT_FOUND(128, "XML parsing failed: No Activity found for Claim"),

  /** The duplicate payer ref. */
  DUPLICATE_PAYMENT_REF(256, "XML parsing failed: Duplicate payment reference found."),

  /** The invalid provider id. */
  INVALID_PROVIDER_ID(512,
      "XML parsing failed: Provider ID not same as the account service registration no"),

  /** The duplicate remittance for claim. */
  DUPLICATE_REMITTANCE_FOR_CLAIM(1024,
      "XML parsing failed: Duplicate remittance file upload for Claim");

  /** The code. */
  private final Integer code;

  /** The message. */
  private final String message;

  /**
   * Instantiates a new claim error type.
   *
   * @param code the code
   * @param message the message
   */
  private ClaimErrorType(int code, String message) {
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
  private static final Map<Integer, ClaimErrorType> lookup =
      new HashMap<Integer, ClaimErrorType>();
  static {
    for (ClaimErrorType errorType : EnumSet.allOf(ClaimErrorType.class)) {
      lookup.put(errorType.getCode(), errorType);
    }
  }

  /**
   * Gets the error code.
   *
   * @param code the code
   * @return the error code
   */
  public static ClaimErrorType getErrorCode(int code) {
    return lookup.get(code);
  }

}

