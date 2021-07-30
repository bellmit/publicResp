package com.insta.hms.core.patient.billing;

import com.insta.hms.batchjob.SynchronousMessagingJob;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Map;

/**
 * The Class DepositMessagingJob.
 */
public class DepositMessagingJob extends SynchronousMessagingJob {

  /**
   * Instantiates a new deposit messaging job.
   */
  public DepositMessagingJob() {
    super(null);
  }

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(DepositMessagingJob.class);

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
   * @param eventData
   *          the event data
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
   * @param eventId
   *          the new event id
   */
  public void setEventId(String eventId) {
    this.eventId = eventId;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.batchjob.MessagingJob#getMessagingEvent()
   */
  @Override
  protected String getMessagingEvent() {
    return getEventId();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.batchjob.MessagingJob#getMessagingData()
   */
  @Override
  protected Map getMessagingData() {
    return getEventData();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.batchjob.SynchronousMessagingJob#executeInternal(org.quartz.JobExecutionContext)
   */
  @Override
  public void executeInternal(JobExecutionContext jobContext) throws JobExecutionException {
    try {
      BasicDynaBean msgBean = new GenericDAO("message_types").findByKey("message_type_id",
          "sms_deposit_paid");
      if ("A".equals(msgBean.get("status"))) {
        super.executeInternal(jobContext);
      }
    } catch (SQLException exception) {
      logger.error("SQLException in DepositMessagingJob" + exception.getMessage());
      throw new JobExecutionException(exception.getMessage());
    } catch (JobExecutionException exception) {
      exception.printStackTrace();
    }
  }

}
