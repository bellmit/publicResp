package com.insta.hms.messaging.providers;

import com.insta.hms.messaging.MessageBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppointmentCancelMessageBuilder extends MessageBuilder {
  static Logger logger = LoggerFactory.getLogger(AppointmentCancelMessageBuilder.class);

  public AppointmentCancelMessageBuilder() {
    this.addDataProvider(new AppointmentCancelDataProvider());
  }
}
