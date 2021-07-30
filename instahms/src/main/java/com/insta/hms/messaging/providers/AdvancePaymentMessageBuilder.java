package com.insta.hms.messaging.providers;

import com.insta.hms.messaging.MessageBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdvancePaymentMessageBuilder extends MessageBuilder {
  static Logger logger = LoggerFactory
      .getLogger(AdvancePaymentMessageBuilder.class);

  public AdvancePaymentMessageBuilder() {
    this.addDataProvider(new AdvancePaymentDataProvider());
  }

}