package com.insta.hms.messaging.providers;

import com.insta.hms.messaging.MessageBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiscountSmsMessageBuilder extends MessageBuilder {
  static Logger logger = LoggerFactory
      .getLogger(DiscountSmsMessageBuilder.class);

  public DiscountSmsMessageBuilder() {
    this.addDataProvider(new DiscountSmsDataProvider());
  }

}