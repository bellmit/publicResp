/**
 *
 */

package com.insta.hms.exception;

import org.springframework.http.HttpStatus;

/**
 * The Class EntityNotFoundException.
 *
 * @author aditya
 *     Custom Exception class to be used to throw missing/not found
 *     exception
 */
public class EntityNotFoundException extends HMSException {

  /**
   * Eclipse generated serialVerisonId.
   */
  private static final long serialVersionUID = -8706065339663645895L;

  /**
   * The Constant defaultStatus.
   */
  private static final HttpStatus defaultStatus = HttpStatus.NOT_FOUND;

  /**
   * The Constant defaultKey.
   */
  private static final String defaultKey = "exception.entity.not.found";

  /**
   * Instantiates a new entity not found exception.
   *
   * @param messageKey the message key
   * @param params     the params
   */
  public EntityNotFoundException(String messageKey, String[] params) {
    super(defaultStatus, messageKey, params);
  }

  /**
   * Instantiates a new entity not found exception.
   *
   * @param params the params
   */
  public EntityNotFoundException(String[] params) {
    this(defaultKey, params);
  }

  /**
   * Instantiates a new entity not found exception.
   *
   * @param messageKey the message key
   */
  public EntityNotFoundException(String messageKey) {
    this(messageKey, null);
  }

  /**
   * Instantiates a new entity not found exception.
   *
   * @param cause the cause
   */
  public EntityNotFoundException(Throwable cause) {
    super(cause);
  }

}
