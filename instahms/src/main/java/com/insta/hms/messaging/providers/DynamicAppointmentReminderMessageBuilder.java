package com.insta.hms.messaging.providers;

import com.insta.hms.messaging.MessageBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicAppointmentReminderMessageBuilder extends MessageBuilder {
  static Logger logger = LoggerFactory
      .getLogger(DynamicAppointmentReminderMessageBuilder.class);

  public DynamicAppointmentReminderMessageBuilder() {
    this.addDataProvider(new DynamicAppointmentReminderDataProvider());
  }

}
