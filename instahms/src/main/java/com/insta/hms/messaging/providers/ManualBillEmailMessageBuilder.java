package com.insta.hms.messaging.providers;

import com.insta.hms.messaging.MessageBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManualBillEmailMessageBuilder extends MessageBuilder {
  static Logger logger = LoggerFactory
      .getLogger(ManualBillEmailMessageBuilder.class);

  public ManualBillEmailMessageBuilder() {
    this.addDataProvider(new ManualBillEmailDataProvider());
  }
}
