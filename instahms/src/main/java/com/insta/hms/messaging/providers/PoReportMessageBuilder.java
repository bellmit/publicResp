package com.insta.hms.messaging.providers;

import com.insta.hms.messaging.MessageBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PoReportMessageBuilder extends MessageBuilder {

  static Logger logger = LoggerFactory
      .getLogger(PoReportMessageBuilder.class);

  public PoReportMessageBuilder() {
    this.addDataProvider(new PoReportDataProvider());
  }
}
