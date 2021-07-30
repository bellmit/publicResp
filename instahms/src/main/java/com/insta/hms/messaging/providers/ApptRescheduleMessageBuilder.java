package com.insta.hms.messaging.providers;

import com.insta.hms.messaging.MessageBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApptRescheduleMessageBuilder extends MessageBuilder {
  static Logger logger = LoggerFactory
      .getLogger(ApptRescheduleMessageBuilder.class);

  public ApptRescheduleMessageBuilder() {
    this.addDataProvider(new ApptRescheduleDataProvider());
  }
}
