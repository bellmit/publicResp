package com.insta.hms.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * The Class NestableValidationException.
 *
 * @author aditya
 *     This class is to be used by transaction classes to nest the
 *     error object of Validation Exception.
 */
public class NestableValidationException extends HMSException {

  /**
   * Default SerialVersionId.
   */
  private static final long serialVersionUID = 1L;

  /**
   * The Constant defaultStatus.
   */
  private static final HttpStatus defaultStatus = HttpStatus.BAD_REQUEST;

  /**
   * The Constant defaultKey.
   */
  private static final String defaultKey = "exception.validation.failed";

  /**
   * The nested error map.
   */
  private Map<String, Object> nestedErrorMap;

  /**
   * Instantiates a new nestable validation exception.
   */
  public NestableValidationException() {
    super(defaultStatus, defaultKey, null);
  }

  /**
   * Instantiates a new nestable validation exception.
   *
   * @param nestedErrorMap the nested error map
   */
  public NestableValidationException(Map<String, Object> nestedErrorMap) {
    this();
    this.nestedErrorMap = nestedErrorMap;
  }

  /**
   * Gets the nested error map.
   *
   * @return the nested error map
   */
  public Map<String, Object> getNestedErrorMap() {
    return nestedErrorMap;
  }

  /**
   * Sets the nested error map.
   *
   * @param nestedErrorMap the nested error map
   */
  public void setNestedErrorMap(Map<String, Object> nestedErrorMap) {
    this.nestedErrorMap = nestedErrorMap;
  }

  /**
   * Gets the errors.
   *
   * @return the errors
   */
  public Map<String, Object> getErrors() {
    return this.nestedErrorMap;
  }
}
