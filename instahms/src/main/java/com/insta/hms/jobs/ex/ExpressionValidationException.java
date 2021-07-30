package com.insta.hms.jobs.ex;

import com.insta.hms.exception.HMSException;

import org.springframework.http.HttpStatus;

/**
 * Cron Expression Exception.
 *
 * @author yashwant
 */
public class ExpressionValidationException extends HMSException {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** The default status. */
  private static final HttpStatus DEFAULTSTATUS = HttpStatus.BAD_REQUEST;

  /**
   * Instantiates a new expression validation exception.
   *
   * @param exMsg the ex msg
   */
  public ExpressionValidationException(String exMsg) {
    super(exMsg);
  }

  /**
   * Instantiates a new expression validation exception.
   *
   * @param messageKey the message key
   * @param params the params
   */
  public ExpressionValidationException(String messageKey, String[] params) {
    super(DEFAULTSTATUS, messageKey, params);
  }

  /**
   * Instantiates a new expression validation exception.
   *
   * @param status the status
   * @param messageKey the message key
   * @param params the params
   */
  public ExpressionValidationException(HttpStatus status, String messageKey, String[] params) {
    super(status, messageKey, params);
  }
}
