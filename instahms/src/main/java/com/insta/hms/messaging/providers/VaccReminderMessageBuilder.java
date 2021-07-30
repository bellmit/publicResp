package com.insta.hms.messaging.providers;

import com.insta.hms.messaging.MessageBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VaccReminderMessageBuilder extends MessageBuilder {
  static Logger logger = LoggerFactory
      .getLogger(VaccReminderMessageBuilder.class);

  public VaccReminderMessageBuilder() {
    this.addDataProvider(new VaccReminderDataProvider());
  }
}
