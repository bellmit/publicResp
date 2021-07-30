package com.insta.hms.core.patient.registration;

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
 * The Class OPAdmissionDoctorMessagingJob.
 */
public class OPAdmissionDoctorMessagingJob extends SynchronousMessagingJob {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(OPAdmissionDoctorMessagingJob.class);

  /** The event id. */
  private String eventId;

  /** The event data. */
  private Map<String, String> eventData;

  /**
   * Instantiates a new OP admission doctor messaging job.
   */
  public OPAdmissionDoctorMessagingJob() {
    super(null);
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

  /**
   * Gets the event data.
   *
   * @return the event data
   */
  public Map<String, String> getEventData() {
    return eventData;
  }

  /**
   * Sets the event data.
   *
   * @param eventData
   *          the event data
   */
  public void setEventData(Map<String, String> eventData) {
    this.eventData = eventData;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.batchjob.SynchronousMessagingJob#executeInternal(
   * org.quartz.JobExecutionContext)
   */
  @Override
  public void executeInternal(JobExecutionContext jobContext) throws JobExecutionException {

    try {
      BasicDynaBean msgBean = new GenericDAO("message_types").findByKey("message_type_id",
          "sms_op_patient_admitted");
      if ("A".equals(msgBean.get("status"))) {
        super.executeInternal(jobContext);
      }
    } catch (SQLException exc) {
      logger.error("SQLException in AsyncMessagingJob" + exc.getMessage());
      throw new JobExecutionException(exc.getMessage());
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.batchjob.MessagingJob#getMessagingData()
   */
  @Override
  protected Map getMessagingData() {
    return eventData;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.batchjob.MessagingJob#getMessagingEvent()
   */
  @Override
  protected String getMessagingEvent() {
    return eventId;
  }

}
