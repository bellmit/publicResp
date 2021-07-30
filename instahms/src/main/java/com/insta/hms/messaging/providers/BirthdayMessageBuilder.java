package com.insta.hms.messaging.providers;

import com.insta.hms.messaging.MessageBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BirthdayMessageBuilder extends MessageBuilder {
  static Logger logger = LoggerFactory
      .getLogger(BirthdayMessageBuilder.class);

  public BirthdayMessageBuilder() {
    this.addDataProvider(new BirthdayMessageDataProvider());
  }
}
