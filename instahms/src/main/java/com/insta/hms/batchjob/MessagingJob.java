package com.insta.hms.batchjob;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.jobs.GenericJob;
import com.insta.hms.messaging.MessageManager;

import org.apache.commons.beanutils.BasicDynaBean;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Map;

public class MessagingJob extends GenericJob {

  private String params;
  private String userName;
  private String forceResend;

  public String getForceResend() {
    return forceResend;
  }

  public void setForceResend(String forceResend) {
    this.forceResend = forceResend;
  }

  public String getParams() {
    return params;
  }

  public void setParams(String params) {
    this.params = params;
  }

  private String event = "";

  public String getEvent() {
    return event;
  }

  public void setEvent(String event) {
    this.event = event;
  }

  /**
   * Instantiates a new messaging job.
   *
   * @param event the event
   */
  public MessagingJob(String event) {
    if (event != null) {
      this.event = event;
    }
  }

  @Override
  public void executeInternal(JobExecutionContext jobContext) throws JobExecutionException {

    Logger logger = LoggerFactory.getLogger(MessagingJob.class);

    String schema = getSchema();
    String userName = getUserName() == null ? "_system" : getUserName();

    RequestContext.setConnectionDetails(new String[] { null, null, schema, userName, "0" });
    MessageManager mgr = new MessageManager();
    String[] modules = getModuleDependencies();
    boolean moduleOk = true;
    try {
      for (String module : modules) {
        moduleOk = moduleOk && isModuleEnable(module);
      }
      if (moduleOk) {
        mgr.processEvent(getMessagingEvent(), getMessagingData(), false);
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

  protected String[] getModuleDependencies() {
    return new String[] { "mod_messaging" };
  }

  protected Map getMessagingData() {
    return null;
  }

  protected String getMessagingEvent() {
    return event;
  }

  protected boolean isModuleEnable(String modeId) throws SQLException {
    BasicDynaBean modBean = new GenericDAO("modules_activated").findByKey("module_id", modeId);
    return (modBean != null && modBean.get("activation_status") != null
        && modBean.get("activation_status").equals("Y"));
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

}
