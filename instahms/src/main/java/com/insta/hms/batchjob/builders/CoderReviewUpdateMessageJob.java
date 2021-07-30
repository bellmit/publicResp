package com.insta.hms.batchjob.builders;

import com.insta.hms.batchjob.MessagingJob;

import java.util.Map;

public class CoderReviewUpdateMessageJob extends MessagingJob {

  private String eventId;
  private Map<String,Object> eventData;

  public CoderReviewUpdateMessageJob() {
    super(null);
  }

  public String getEventId() {
    return eventId;
  }

  public void setEventId(String eventId) {
    this.eventId = eventId;
  }

  public Map<String, Object> getEventData() {
    return eventData;
  }

  public void setEventData(Map<String, Object> eventData) {
    this.eventData = eventData;
  }

  @Override
  protected String getMessagingEvent() {
    return getEventId();
  }

  @Override
  protected Map getMessagingData() {
    return getEventData();
  }
  
  
}
