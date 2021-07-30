package com.insta.hms.common;

import org.springframework.http.HttpStatus;

/**
 * The Class HMSErrorResponse.
 */
public class HMSErrorResponse {

  /** The status. */
  private int status;

  /** The display message. */
  private String displayMessage;

  // TODO: Do we need errorCode?
  // private int errorCode;

  /** The Constant INTERNAL_SERVER_ERROR. */
  // TODO: localization here??
  private static final String INTERNAL_SERVER_ERROR = "An internal server error occurred";

  /**
   * Instantiates a new HMS error response.
   *
   * @param status         the status
   * @param displayMessage the display message
   */
  public HMSErrorResponse(HttpStatus status, String displayMessage) {
    if (status != null) {
      this.status = status.value();
    } else {
      this.status = HttpStatus.INTERNAL_SERVER_ERROR.value();// default status value
    }

    if (displayMessage != null) {
      this.displayMessage = displayMessage;
    } else {
      this.displayMessage = INTERNAL_SERVER_ERROR; // default display message
    }
  }

  /**
   * Instantiates a new HMS error response.
   *
   * @param displayMessage the display message
   */
  public HMSErrorResponse(String displayMessage) {
    this(null, displayMessage);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuffer str = new StringBuffer();
    str.append("Status: ").append(status).append(" message: ").append(displayMessage);
    return str.toString();
  }

  /**
   * Gets the status.
   *
   * @return the status
   */
  public int getStatus() {
    return this.status;
  }

  /**
   * Gets the display message.
   *
   * @return the display message
   */
  public String getDisplayMessage() {
    return this.displayMessage;
  }

}
