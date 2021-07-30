package com.insta.hms.exception;

import org.springframework.http.HttpStatus;

/**
 * The Class ConfigFileNotFoundException.
 *
 * @author - tanmay.k
 */
public class ConfigFileNotFoundException extends HMSException {

  /**
   * Eclipse Generated SerialVersionUID.
   */
  private static final long serialVersionUID = -3582788306029667380L;

  /**
   * The Constant defaultStatus.
   */
  private static final HttpStatus defaultStatus = HttpStatus.INTERNAL_SERVER_ERROR;

  /**
   * The Constant defaultKey.
   */
  private static final String defaultKey = "exception.config.file.not.found";

  /**
   * Instantiates a new config file not found exception.
   *
   * @param messageKey the message key
   * @param params     the params
   */
  public ConfigFileNotFoundException(String messageKey, String[] params) {
    super(defaultStatus, messageKey, params);
  }

  /**
   * Instantiates a new config file not found exception.
   *
   * @param params the params
   */
  public ConfigFileNotFoundException(String[] params) {
    this(defaultKey, params);
  }

  /**
   * Instantiates a new config file not found exception.
   *
   * @param messageKey the message key
   */
  public ConfigFileNotFoundException(String messageKey) {
    this(messageKey, null);
  }

  /**
   * Instantiates a new config file not found exception.
   *
   * @param cause the cause
   */
  public ConfigFileNotFoundException(Throwable cause) {
    super(cause);
  }

  /**
   * Instantiates a new config file not found exception.
   */
  public ConfigFileNotFoundException() {
    this(defaultKey, null);
  }

}