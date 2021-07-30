package com.insta.hms.ceed;

/*
 * This is a custom exception created for error handling in ceed integration
 */
public class CEEDInternetConnectionException extends Exception {
  public CEEDInternetConnectionException(String errorMessage) {
    super(errorMessage);
  }
}
