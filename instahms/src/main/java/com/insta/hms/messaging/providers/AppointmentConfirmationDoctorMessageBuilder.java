package com.insta.hms.messaging.providers;

import com.insta.hms.messaging.MessageBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppointmentConfirmationDoctorMessageBuilder extends MessageBuilder {
  static Logger logger = LoggerFactory
      .getLogger(AppointmentConfirmationDoctorDataProvider.class);

  public AppointmentConfirmationDoctorMessageBuilder() {
    this.addDataProvider(new AppointmentConfirmationDoctorDataProvider());
  }
}
