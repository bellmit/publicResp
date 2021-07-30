package com.insta.hms.exception;

import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class ResourceLockedException.
 */
public class ResourceLockedException extends HMSException {
  
  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The default status. */
  private static HttpStatus defaultStatus = HttpStatus.BAD_REQUEST;
  
  /** The Constant defaultKey. */
  private static final String defaultKey = "exception.resource.locked";

  /** The resorce locked object. */
  private Map<String, Object> resorceLockedObject = new HashMap<String, Object>();

  /**
   * Instantiates a new resource locked exception.
   *
   * @param status the status
   * @param messageKey the message key
   * @param params the params
   */
  public ResourceLockedException(HttpStatus status, String messageKey,
      String[] params) {
    super(status == null ? defaultStatus : status,
        messageKey == null ? defaultKey : messageKey, params);
  }
  
  /**
   * Instantiates a new resource locked exception.
   *
   * @param status the status
   * @param messageKey the message key
   * @param params the params
   * @param resorceLockedObject the resorce locked object
   */
  public ResourceLockedException(HttpStatus status, String messageKey,
      String[] params, Map<String, Object> resorceLockedObject) {
    this(status, messageKey, params);
    if (null != resorceLockedObject && !resorceLockedObject.isEmpty()) {
      this.setResorceLockedObject(resorceLockedObject);
    }
  }

  /**
   * Instantiates a new resource locked exception.
   *
   * @param messageKey the message key
   * @param params the params
   */
  public ResourceLockedException(String messageKey, String[] params) {
    this(defaultStatus, messageKey, params);
  }

  /**
   * Instantiates a new resource locked exception.
   *
   * @param status the status
   * @param params the params
   * @param resorceLockedObject the resorce locked object
   */
  public ResourceLockedException(HttpStatus status, String[] params,
      Map<String, Object> resorceLockedObject) {
    this(status, defaultKey, params, resorceLockedObject);
  }

  /**
   * Instantiates a new resource locked exception.
   *
   * @param params the params
   */
  public ResourceLockedException(String[] params) {
    this(defaultKey, params);
  }

  /**
   * Instantiates a new resource locked exception.
   *
   * @param messageKey the message key
   */
  public ResourceLockedException(String messageKey) {
    this(messageKey, null);
  }

  /**
   * Instantiates a new resource locked exception.
   *
   * @param cause the cause
   */
  public ResourceLockedException(Throwable cause) {
    super(cause);
  }

  /**
   * Instantiates a new resource locked exception.
   */
  public ResourceLockedException() {
    this(defaultKey, null);
  }

  /**
   * Gets the resorce locked object.
   *
   * @return the resorce locked object
   */
  public Map<String, Object> getResorceLockedObject() {
    return resorceLockedObject;
  }

  /**
   * Sets the resorce locked object.
   *
   * @param resorceLockedObject the resorce locked object
   */
  public void setResorceLockedObject(Map<String, Object> resorceLockedObject) {
    this.resorceLockedObject = resorceLockedObject;
  }

}
