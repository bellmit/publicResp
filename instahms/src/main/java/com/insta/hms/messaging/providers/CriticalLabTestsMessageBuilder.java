package com.insta.hms.messaging.providers;

import com.insta.hms.messaging.MessageBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CriticalLabTestsMessageBuilder extends MessageBuilder {
  Logger logger = LoggerFactory.getLogger(CriticalLabTestsMessageBuilder.class);

  public CriticalLabTestsMessageBuilder() {
    // TODO Auto-generated constructor stub
    this.addDataProvider(new CriticalLabTestsDataProvider());
  }
}
