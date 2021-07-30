package com.insta.hms.messaging.providers;

import com.insta.hms.messaging.MessageBuilder;

public class RefundMessageBuilder extends MessageBuilder {
  public RefundMessageBuilder() {
    this.addDataProvider(new RefundDataProvider());
  }
}
