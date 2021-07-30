package com.insta.hms.common.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpResponse {

  static final Logger log = LoggerFactory.getLogger(HttpResponse.class);
  // -100 status is used to represent that it is thrown by HTTPClient
  public static final HttpResponse INVALID_URL = new HttpResponse(-100,
      "The server URL is invalid.");
  public static final HttpResponse CONNECTION_TIMEOUT = new HttpResponse(-100,
      "The server did not respond in time.");
  public static final HttpResponse HTTP_SERVER_ERROR = new HttpResponse(-100,
      "The server reported an internal error.");
  public static final int SUCCESS_STATUS_CODE = 200;
  public static final int ERROR_STATUS_CODE = -200;

  private int statusCode = 0;
  private String statusMessage = "";

  public HttpResponse(int status, String statusMessage) {
    this.statusCode = status;
    this.statusMessage = statusMessage;
  }

  public String getMessage() {
    return statusMessage;
  }

  public int getCode() {
    return statusCode;
  }

}
