package com.insta.hms.messaging.providers;

import com.insta.hms.messaging.MessageBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PaymentReceivedMessageBuilder extends MessageBuilder {
  static Logger logger = LoggerFactory
      .getLogger(PaymentReceivedMessageBuilder.class);

  public PaymentReceivedMessageBuilder() {
    this.addDataProvider(new PaymentReceivedDataProvider());
  }

}
