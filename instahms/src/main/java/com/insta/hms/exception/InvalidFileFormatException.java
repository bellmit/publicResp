package com.insta.hms.exception;

import org.springframework.http.HttpStatus;

/**
 * The Class InvalidFileFormatException.
 *
 * @author aditya
 *     file format exception to be thrown when IO exception occurs
 */
public class InvalidFileFormatException extends HMSException {

  /**
   * default serial version UID.
   */
  private static final long serialVersionUID = 1L;

  /**
   * The Constant defaultStatus.
   */
  private static final HttpStatus defaultStatus = HttpStatus.BAD_REQUEST;

  /**
   * The Constant defaultKey.
   */
  private static final String defaultKey = "exception.invalid.file.format";

  /**
   * Instantiates a new invalid file format exception.
   *
   * @param messageKey the message key
   * @param params     the params
   */
  public InvalidFileFormatException(String messageKey, String[] params) {
    super(defaultStatus, messageKey, params);
  }

  /**
   * Instantiates a new invalid file format exception.
   *
   * @param params the params
   */
  public InvalidFileFormatException(String[] params) {
    this(defaultKey, params);
  }

  /**
   * Instantiates a new invalid file format exception.
   *
   * @param messageKey the message key
   */
  public InvalidFileFormatException(String messageKey) {
    this(messageKey, null);
  }

  /**
   * Instantiates a new invalid file format exception.
   *
   * @param cause the cause
   */
  public InvalidFileFormatException(Throwable cause) {
    super(cause);
  }

  /**
   * Instantiates a new invalid file format exception.
   */
  public InvalidFileFormatException() {
    this(defaultKey, null);
  }

}
