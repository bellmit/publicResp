package com.insta.hms.batchjob.builders;

import com.insta.hms.batchjob.MessagingJob;

import java.util.HashMap;
import java.util.Map;

public class DynamicAppointmentReminderJob extends MessagingJob {

  public DynamicAppointmentReminderJob() {
    super("dynamic_appointment_reminder");
  }

  @Override
  protected Map getMessagingData() {
    String appointmentId = getParams();
    Map<String, String> appointmentData = new HashMap<>();
    appointmentData.put("appointment_no", appointmentId);
    return appointmentData;
  }

}
