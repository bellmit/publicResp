package com.insta.hms.messaging.providers;

import com.insta.hms.messaging.MessageBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PatientDepositMessageBuilder extends MessageBuilder {
  static Logger logger = LoggerFactory
      .getLogger(PatientDepositMessageBuilder.class);

  public PatientDepositMessageBuilder() {
    addDataProvider(new PatientDepositDataProvider());
  }
}
