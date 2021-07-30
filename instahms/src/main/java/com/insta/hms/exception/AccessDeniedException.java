package com.insta.hms.exception;

import org.springframework.http.HttpStatus;

/**
 * The Class AccessDeniedException.
 */
public class AccessDeniedException extends HMSException {

  /**
   * Eclipse generated serialVersionUID.
   */
  private static final long serialVersionUID = 3280090588986747618L;

  /**
   * The Constant defaultStatus.
   */
  private static final HttpStatus defaultStatus = HttpStatus.FORBIDDEN;

  /**
   * The Constant defaultKey.
   */
  private static final String defaultKey = "exception.access.denied";

  /**
   * Instantiates a new access denied exception.
   *
   * @param messageKey the message key
   * @param params     the params
   */
  public AccessDeniedException(String messageKey, String[] params) {
    super(defaultStatus, messageKey, params);
  }

  /**
   * Instantiates a new access denied exception.
   *
   * @param params the params
   */
  public AccessDeniedException(String[] params) {
    this(defaultKey, params);
  }

  /**
   * Instantiates a new access denied exception.
   *
   * @param messageKey the message key
   */
  public AccessDeniedException(String messageKey) {
    this(messageKey, null);
  }

  /**
   * Instantiates a new access denied exception.
   *
   * @param cause the cause
   */
  public AccessDeniedException(Throwable cause) {
    super(cause);
  }

  /**
   * Instantiates a new access denied exception.
   */
  public AccessDeniedException() {
    super(defaultStatus, defaultKey, null);
  }

}
