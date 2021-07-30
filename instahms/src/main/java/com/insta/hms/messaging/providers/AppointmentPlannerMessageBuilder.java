package com.insta.hms.messaging.providers;

import com.insta.hms.messaging.MessageBuilder;

public class AppointmentPlannerMessageBuilder extends MessageBuilder {

  public AppointmentPlannerMessageBuilder() {
    this.addDataProvider(new AppointmentPlannerDataProvider());
  }

}
