package com.insta.hms.messaging.providers;

import com.insta.hms.messaging.MessageBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdmissionMessageBuilder extends MessageBuilder {
  static Logger logger = LoggerFactory
      .getLogger(AdmissionMessageBuilder.class);

  public AdmissionMessageBuilder() {
    this.addDataProvider(new AdmissionDataProvider());
  }
}
