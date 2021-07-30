package com.insta.hms.messaging.providers;

import com.insta.hms.messaging.MessageBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PhrPrescriptionMessageBuilder extends MessageBuilder {
  static Logger logger = LoggerFactory
      .getLogger(PhrPrescriptionMessageBuilder.class);

  public PhrPrescriptionMessageBuilder() {
    this.addDataProvider(new PhrPrescriptionDataProvider());
  }

}
