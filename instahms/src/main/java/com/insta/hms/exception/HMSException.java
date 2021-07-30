package com.insta.hms.exception;

import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.HMSErrorResponse;
import com.insta.hms.common.MessageUtil;

import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The Class HMSException.
 */
public class HMSException extends RuntimeException {

  /**
   * The Constant serialVersionUID.
   */
  private static final long serialVersionUID = 1L;

  /**
   * The Constant defaultStatus.
   */
  private static final HttpStatus defaultStatus = HttpStatus.INTERNAL_SERVER_ERROR;

  /**
   * The Constant defaultKey.
   */
  private static final String defaultKey = "exception.internal.error";

  /**
   * The Constant messageUtil.
   */
  private static final MessageUtil messageUtil =
          ApplicationContextProvider.getBean(MessageUtil.class);

  /**
   * The status.
   */
  private HttpStatus status;

  /**
   * The message key.
   */
  private String messageKey;

  /**
   * The params.
   */
  private String[] params;
  
  /**
   * The errors.
   */
  private List<String> errors;

  /**
   * Instantiates a new HMS exception.
   */
  public HMSException() {
    this.status = defaultStatus;
    this.messageKey = defaultKey;
  }

  /**
   * Instantiates a new HMS exception.
   *
   * @param cause the cause
   */
  public HMSException(Throwable cause) {
    super(cause);
  }

  /**
   * Instantiates a new HMS exception.
   *
   * @param status     the status
   * @param messageKey the message key
   * @param params     the params
   */
  public HMSException(HttpStatus status, String messageKey, String[] params) {
    if (messageKey != null) {
      this.messageKey = messageKey;
    } else {
      this.messageKey = defaultKey;
    }

    if (status != null) {
      this.status = status;
    } else {
      this.status = defaultStatus;
    }

    if (params != null) {
      this.params = params;
    }
  }

  /**
   * Instantiates a new HMS exception.
   *
   * @param messageKey the message key
   * @param params     the params
   */
  public HMSException(String messageKey, String[] params) {
    this(null, messageKey, params);
  }

  /**
   * Instantiates a new HMS exception.
   * @param errors the list of string
   */
  public HMSException(List<String> errors) {
    this.errors = errors;
  }
  
  /**
   * Instantiates a new HMS exception.
   *
   * @param messageKey the message key
   */
  public HMSException(String messageKey) {
    this(messageKey, null);
  }

  /**
   * Gets the status.
   *
   * @return the status
   */
  public HttpStatus getStatus() {
    return this.status;
  }

  /**
   * Gets the message key.
   *
   * @return the message key
   */
  public String getMessageKey() {
    return this.messageKey;
  }

  /**
   * Gets the params.
   *
   * @return the params
   */
  public String[] getParams() {
    return this.params;
  }

  /* (non-Javadoc)
   * @see java.lang.Throwable#getMessage()
   */
  @Override
  public String getMessage() {
    return messageUtil.getMessage(getMessageKey(), getParams());
  }

  /**
   * Gets the error response.
   *
   * @return the error response
   */
  public HMSErrorResponse getErrorResponse() {
    return new HMSErrorResponse(getStatus(), getMessage());
  }

  /**
   * gets the list of string as errors.
   * @return the list of string
   */
  public List<String> getErrorsList() {
    if (this.errors.isEmpty()) {
      return Arrays.asList(getMessage());
    } else {
      return this.errors;
    }
  }
}
