package com.insta.hms.core.patient.billing;

import com.insta.hms.batchjob.MessagingJob;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * The Class BillMessagingJob.
 */
public class BillMessagingJob extends MessagingJob {

  /**
   * Instantiates a new bill messaging job.
   */
  public BillMessagingJob() {
    super(null);
  }

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(BillMessagingJob.class);
  
  /** The event data. */
  private Map<String, Object> eventData;
  
  /** The event id. */
  private String eventId;

  /**
   * Gets the event data.
   *
   * @return the event data
   */
  public Map<String, Object> getEventData() {
    return eventData;
  }

  /**
   * Sets the event data.
   *
   * @param eventData the event data
   */
  public void setEventData(Map<String, Object> eventData) {
    this.eventData = eventData;
  }

  /**
   * Gets the event id.
   *
   * @return the event id
   */
  public String getEventId() {
    return eventId;
  }

  /**
   * Sets the event id.
   *
   * @param eventId the new event id
   */
  public void setEventId(String eventId) {
    this.eventId = eventId;
  }

  /* (non-Javadoc)
   * @see com.insta.hms.batchjob.MessagingJob#getMessagingEvent()
   */
  @Override
  protected String getMessagingEvent() {
    return getEventId();
  }

  /* (non-Javadoc)
   * @see com.insta.hms.batchjob.MessagingJob#getMessagingData()
   */
  @Override
  protected Map getMessagingData() {
    return getEventData();
  }
}