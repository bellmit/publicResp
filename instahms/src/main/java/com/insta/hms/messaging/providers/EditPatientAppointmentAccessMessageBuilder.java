package com.insta.hms.messaging.providers;

import com.insta.hms.messaging.MessageBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EditPatientAppointmentAccessMessageBuilder extends MessageBuilder {

  static Logger logger = LoggerFactory
      .getLogger(PatientAppointmentMessageBuilder.class);

  public EditPatientAppointmentAccessMessageBuilder() {
    this.addDataProvider(new EditPatientAppointmentAccessDataProvider());
  }
}
