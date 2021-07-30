package com.insta.hms.messaging.providers;

import com.insta.hms.messaging.MessageBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PhysicalDischargeMessageBuilder extends MessageBuilder {
  Logger logger = LoggerFactory.getLogger(PhysicalDischargeMessageBuilder.class);

  public PhysicalDischargeMessageBuilder() {
    this.addDataProvider(new PhysicalDischargeDataProvider());
  }
}
