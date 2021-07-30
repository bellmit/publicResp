package com.insta.hms.billing.accounting;

/**
 * The Class AccountingException.
 *
 * @author krishna.t
 * 
 *         used to wrap all the tally application exceptions.
 */
public class AccountingException extends Exception {

  /**
   * Instantiates a new accounting exception.
   *
   * @param exception
   *          the e
   */
  public AccountingException(Exception exception) {
    super(exception);
  }

  /**
   * Instantiates a new accounting exception.
   *
   * @param msg
   *          the msg
   * @param exception
   *          the e
   */
  public AccountingException(String msg, Exception exception) {
    super(msg, exception);
  }

  /**
   * Instantiates a new accounting exception.
   *
   * @param msg
   *          the msg
   */
  public AccountingException(String msg) {
    super(msg);
  }

}
