package com.insta.hms.messaging.providers;

import com.insta.hms.messaging.MessageBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WaitlistMessageBuilder extends MessageBuilder {
  Logger logger = LoggerFactory.getLogger(WaitlistMessageBuilder.class);

  public WaitlistMessageBuilder() {
    this.addDataProvider(new WaitlistDataProvider());
  }
}
