package com.insta.hms.messaging.providers;

public enum TokensSupported {
  MESSAGE_BODY("message_body"),
  MOBILE_NUMBER("mobile_no"),
  MESSAGE_SUBJECT("message_subject"),
  ;
  private String tokenName;

  TokensSupported(String tokenName) {
    this.tokenName = tokenName;
  }

  public String getTokenName() {
    return tokenName;
  }
}
