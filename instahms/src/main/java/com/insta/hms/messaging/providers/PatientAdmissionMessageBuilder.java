package com.insta.hms.messaging.providers;

import com.insta.hms.messaging.MessageBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PatientAdmissionMessageBuilder extends MessageBuilder {
  static Logger logger = LoggerFactory
      .getLogger(PatientAdmissionMessageBuilder.class);

  public PatientAdmissionMessageBuilder() {
    this.addDataProvider(new PatientAdmissionDataProvider());
  }
}
