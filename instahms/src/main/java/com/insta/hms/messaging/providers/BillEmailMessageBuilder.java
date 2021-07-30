package com.insta.hms.messaging.providers;

import com.insta.hms.messaging.MessageBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BillEmailMessageBuilder extends MessageBuilder {
  static Logger logger = LoggerFactory
      .getLogger(BillEmailMessageBuilder.class);

  public BillEmailMessageBuilder() {
    this.addDataProvider(new BillEmailDataProvider());
  }
}
