package com.insta.hms.messaging.providers;

import com.insta.hms.messaging.MessageBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PatientDischargeNokMessageBuilder extends MessageBuilder {
  Logger logger = LoggerFactory.getLogger(PatientDischargeNokMessageBuilder.class);

  public PatientDischargeNokMessageBuilder() {
    this.addDataProvider(new PatientDischargeNokDataProvider());
  }
}
