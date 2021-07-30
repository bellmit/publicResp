package com.insta.hms.batchjob.builders;

import com.bob.hms.common.RequestContext;
import com.insta.hms.batchjob.MessagingJob;
import com.insta.hms.core.patient.registration.RegistrationSmsJob;
import com.insta.hms.messaging.MessageManager;
import com.insta.hms.resourcescheduler.ResourceDAO;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

public class AppointmentStatusChangeSMSJob extends MessagingJob {

  public AppointmentStatusChangeSMSJob() {
    super(null);
  }

  private static Logger logger = LoggerFactory.getLogger(RegistrationSmsJob.class);
  private String[] eventData;
  private String eventId;
  private String newStatus;

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

  public String getNewStatus() {
    return newStatus;
  }

  public void setNewStatus(String newStatus) {
    this.newStatus = newStatus;
  }

  @Override
  public void executeInternal(JobExecutionContext jobContext) throws JobExecutionException {

    String schema = getSchema();
    String userName = getUserName() == null ? "" : getUserName();
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
        String appointmentStatus = getNewStatus();
        String[] appointmentIds = getEventData();
        for (String appointmentIdStr : appointmentIds) {
          int appointmentIdInt = Integer.parseInt(appointmentIdStr);
          if (!(ResourceDAO.getAppointmentSource(appointmentIdInt) != null
              && ResourceDAO.getAppointmentSource(appointmentIdInt).equalsIgnoreCase("practo"))) {
            Map appointmentData = new HashMap();
            appointmentData.put("appointment_id", appointmentIdInt);
            appointmentData.put("status", appointmentStatus);
            mgr.processEvent(getEventId(), appointmentData, false);
          }
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
