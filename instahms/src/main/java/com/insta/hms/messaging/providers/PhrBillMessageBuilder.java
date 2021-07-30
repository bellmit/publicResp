package com.insta.hms.messaging.providers;

import com.insta.hms.messaging.MessageBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PhrBillMessageBuilder extends MessageBuilder {
  static Logger logger = LoggerFactory
      .getLogger(PhrBillMessageBuilder.class);

  public PhrBillMessageBuilder() {
    this.addDataProvider(new PhrBillDataProvider());
  }

}
