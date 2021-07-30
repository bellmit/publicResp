package com.insta.hms.messaging.providers;

import com.insta.hms.messaging.MessageBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PatientDischargeMessageBuilder extends MessageBuilder {
  Logger logger = LoggerFactory.getLogger(PatientDischargeMessageBuilder.class);

  public PatientDischargeMessageBuilder() {
    this.addDataProvider(new PatientDischargeDataProvider());
  }
}
