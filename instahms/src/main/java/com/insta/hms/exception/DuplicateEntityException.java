package com.insta.hms.exception;

import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class DuplicateEntityException.
 */
public class DuplicateEntityException extends HMSException {

  /**
   * The Constant serialVersionUID.
   */
  private static final long serialVersionUID = 1L;

  /**
   * The default status.
   */
  private static HttpStatus defaultStatus = HttpStatus.BAD_REQUEST;

  /**
   * The Constant defaultKey.
   */
  private static final String defaultKey = "exception.duplicate.entity";

  /**
   * The duplicate object.
   */
  private Map<String, Object> duplicateObject = new HashMap<String, Object>();

  /**
   * Instantiates a new duplicate entity exception.
   *
   * @param status     the status
   * @param messageKey the message key
   * @param params     the params
   */
  public DuplicateEntityException(HttpStatus status, String messageKey, String[] params) {
    super(status == null ? defaultStatus : status, messageKey == null ? defaultKey : messageKey,
        params);
  }

  /**
   * Instantiates a new duplicate entity exception.
   *
   * @param status          the status
   * @param messageKey      the message key
   * @param params          the params
   * @param duplicateObject the duplicate object
   */
  public DuplicateEntityException(HttpStatus status, String messageKey, String[] params,
         Map<String, Object> duplicateObject) {
    this(status, messageKey, params);
    if (null != duplicateObject && !duplicateObject.isEmpty()) {
      this.setDuplicateObject(duplicateObject);
    }
  }

  /**
   * Instantiates a new duplicate entity exception.
   *
   * @param messageKey the message key
   * @param params     the params
   */
  public DuplicateEntityException(String messageKey, String[] params) {
    this(defaultStatus, messageKey, params);
  }

  /**
   * Instantiates a new duplicate entity exception.
   *
   * @param status          the status
   * @param params          the params
   * @param duplicateObject the duplicate object
   */
  public DuplicateEntityException(HttpStatus status, String[] params,
          Map<String, Object> duplicateObject) {
    this(status, defaultKey, params, duplicateObject);
  }

  /**
   * Instantiates a new duplicate entity exception.
   *
   * @param params the params
   */
  public DuplicateEntityException(String[] params) {
    this(defaultKey, params);
  }

  /**
   * Instantiates a new duplicate entity exception.
   *
   * @param messageKey the message key
   */
  public DuplicateEntityException(String messageKey) {
    this(messageKey, null);
  }

  /**
   * Instantiates a new duplicate entity exception.
   *
   * @param cause the cause
   */
  public DuplicateEntityException(Throwable cause) {
    super(cause);
  }

  /**
   * Instantiates a new duplicate entity exception.
   */
  public DuplicateEntityException() {
    this(defaultKey, null);
  }

  /**
   * Gets the duplicate object.
   *
   * @return the duplicate object
   */
  public Map<String, Object> getDuplicateObject() {
    return duplicateObject;
  }

  /**
   * Sets the duplicate object.
   *
   * @param duplicateObject the duplicate object
   */
  public void setDuplicateObject(Map<String, Object> duplicateObject) {
    this.duplicateObject = duplicateObject;
  }

}
