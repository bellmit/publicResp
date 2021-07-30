package com.insta.hms.messaging.providers;

import com.insta.hms.messaging.MessageBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpAdmissionMessageBuilder extends MessageBuilder {
  static Logger logger = LoggerFactory
      .getLogger(OpAdmissionMessageBuilder.class);

  public OpAdmissionMessageBuilder() {
    this.addDataProvider(new OpAdmissionDataProvider());
  }
}
