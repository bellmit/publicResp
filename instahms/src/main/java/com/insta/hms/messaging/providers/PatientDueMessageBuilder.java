package com.insta.hms.messaging.providers;

import com.insta.hms.messaging.MessageBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PatientDueMessageBuilder extends MessageBuilder {
  static Logger logger = LoggerFactory
      .getLogger(PatientDueMessageBuilder.class);

  public PatientDueMessageBuilder() {
    this.addDataProvider(new PatientDueDataProvider());
  }

}