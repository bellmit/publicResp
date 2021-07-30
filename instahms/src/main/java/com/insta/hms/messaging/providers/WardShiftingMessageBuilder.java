package com.insta.hms.messaging.providers;

import com.insta.hms.messaging.MessageBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WardShiftingMessageBuilder extends MessageBuilder {
  static Logger logger = LoggerFactory
      .getLogger(WardShiftingMessageBuilder.class);

  public WardShiftingMessageBuilder() {
    this.addDataProvider(new WardShiftingDataProvider());
  }
}
