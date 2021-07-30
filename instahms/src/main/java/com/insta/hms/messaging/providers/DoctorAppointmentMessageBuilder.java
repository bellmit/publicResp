package com.insta.hms.messaging.providers;

import com.insta.hms.messaging.MessageBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DoctorAppointmentMessageBuilder extends MessageBuilder {
  static Logger logger = LoggerFactory
      .getLogger(DoctorAppointmentMessageBuilder.class);

  public DoctorAppointmentMessageBuilder() {
    this.addDataProvider(new DoctorAppointmentDataProvider());
  }
}
