package com.insta.hms.batchjob.builders;

import com.bob.hms.common.RequestContext;
import com.insta.hms.batchjob.MessagingJob;
import com.insta.hms.messaging.MessageManager;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

public class AppointmentRescheduleJob extends MessagingJob {
  public AppointmentRescheduleJob() {
    // TODO Auto-generated constructor stub
    super("doctor_unavailable");
  }

  private static Logger logger = LoggerFactory.getLogger(AppointmentRescheduleJob.class);
  private String[] eventData;
  private String eventId;
  private String[] status;

  public String[] getEventData() {
    return eventData;
  }

  public void setEventData(String[] eventData) {
    this.eventData = eventData;
  }

  public String getEventId() {
    return eventId;
  }

  public void setEventId(String eventId) {
    this.eventId = eventId;
  }

  public String[] getStatus() {
    return status;
  }

  public void setStatus(String[] status) {
    this.status = status;
  }

  @Override
  public void executeInternal(JobExecutionContext jobContext) throws JobExecutionException {

    Logger logger = LoggerFactory.getLogger(AppointmentRescheduleJob.class);

    String schema = getSchema();
    String userName = getUserName() == null ? "" : getUserName().toString();
    if (userName.equals("")) {
      userName = "_system";
    }
    RequestContext.setConnectionDetails(new String[] { null, null, schema, userName, "0" });
    MessageManager mgr = new MessageManager();
    String[] modules = getModuleDependencies();
    boolean moduleOk = true;
    try {
      for (String module : modules) {
        moduleOk = moduleOk && isModuleEnable(module);
      }
      if (moduleOk) {
        String[] appointStatus = getStatus();
        String[] appointmentIds = getEventData();
        for (int i = 0; i < appointmentIds.length; i++) {
          int appointmentIdInt = Integer.parseInt(appointmentIds[i]);
          String appointmentStatus = appointStatus[i];
          Map appointmentData = new HashMap();
          appointmentData.put("appointment_id", appointmentIdInt);
          appointmentData.put("status", appointmentStatus);
          mgr.processEvent("doctor_unavailable", appointmentData, false);
        }
      }
    } catch (SQLException exception) {
      logger.error("SQLException in MessagingJob" + exception.getMessage());
      throw new JobExecutionException(exception.getMessage());
    } catch (ParseException exception) {
      logger.error("ParseException in MessagingJob" + exception.getMessage());
      throw new JobExecutionException(exception.getMessage());
    } catch (IOException exception) {
      logger.error("IOException in MessagingJob" + exception.getMessage());
      throw new JobExecutionException(exception.getMessage());
    }
  }

  @Override
  protected String getMessagingEvent() {
    return getEventId();
  }

}
