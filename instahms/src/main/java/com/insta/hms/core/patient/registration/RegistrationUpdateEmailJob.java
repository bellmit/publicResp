package com.insta.hms.core.patient.registration;

import com.insta.hms.batchjob.MessagingJob;
import com.insta.hms.common.MailService;

import jlibs.core.util.regex.TemplateMatcher;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;


/**
 * The Class RegistrationUpdateEmailJob.
 */
public class RegistrationUpdateEmailJob extends MessagingJob {

  /**
   * Instantiates a new registration update email job.
   */
  public RegistrationUpdateEmailJob() {
    super(null);
  }

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(RegistrationUpdateEmailJob.class);

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
   * @see com.insta.hms.batchjob.MessagingJob#executeInternal(org.quartz.JobExecutionContext)
   */
  @Override
  public void executeInternal(JobExecutionContext jobContext) throws JobExecutionException {
    // TODO Auto-generated method stub

    TemplateMatcher matcher = new TemplateMatcher("${", "}");
    Map<String, Object> eventData = getMessagingData();

    MailService ms;
    try {
      ms = new MailService();

      ms.sendMail(
          (String) eventData.get("from"),
          (String[]) eventData.get("recipients"),
          matcher.replace(getMessagingEvent(), (Map<String, String>) eventData.get("vars")),
          matcher.replace((String) eventData.get("message"),
              (Map<String, String>) eventData.get("vars")));
    } catch (SQLException exc) {
      logger.error("SQLException in RegistrationUpdateEmailJob" + exc.getMessage());
      throw new JobExecutionException(exc.getMessage());
    } catch (AddressException exc) {
      logger.error("AddressException in RegistrationUpdateEmailJob" + exc.getMessage());
      throw new JobExecutionException(exc.getMessage());
    } catch (MessagingException exc) {
      logger.error("MessagingException in RegistrationUpdateEmailJob" + exc.getMessage());
      throw new JobExecutionException(exc.getMessage());
    }

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

}
