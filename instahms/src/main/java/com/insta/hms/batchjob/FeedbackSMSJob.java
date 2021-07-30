package com.insta.hms.batchjob;

import java.util.HashMap;
import java.util.Map;

public class FeedbackSMSJob extends MessagingJob {

  public FeedbackSMSJob() {
    super("feedback_reminder");
  }

  @Override
  protected Map getMessagingData() {
    Map<String, String> dataMap = new HashMap<>();
    dataMap.put("config_number", getParams());
    return dataMap;
  }
}
