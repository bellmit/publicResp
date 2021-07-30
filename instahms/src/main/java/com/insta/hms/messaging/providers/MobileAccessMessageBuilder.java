package com.insta.hms.messaging.providers;

import com.insta.hms.messaging.MessageBuilder;

public class MobileAccessMessageBuilder extends MessageBuilder {

  public MobileAccessMessageBuilder() {
    this.addDataProvider(new MobileAccessDataProvider());
  }
}
