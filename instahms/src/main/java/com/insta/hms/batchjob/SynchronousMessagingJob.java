package com.insta.hms.batchjob;

import com.bob.hms.common.RequestContext;
import com.insta.hms.messaging.MessageManager;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;

public class SynchronousMessagingJob extends MessagingJob {

  public SynchronousMessagingJob(String event) {
    super(event);
  }

  @Override
  public void executeInternal(JobExecutionContext jobContext) throws JobExecutionException {

    Logger logger = LoggerFactory.getLogger(SynchronousMessagingJob.class);

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
        mgr.processEvent(getMessagingEvent(), getMessagingData(), false);
      }
    } catch (SQLException exception) {
      logger.error("SQLException in SynchronousMessagingJob" + exception.getMessage());
      throw new JobExecutionException(exception.getMessage());
    } catch (ParseException exception) {
      logger.error("ParseException in SynchronousMessagingJob" + exception.getMessage());
      throw new JobExecutionException(exception.getMessage());
    } catch (IOException exception) {
      logger.error("IOException in SynchronousMessagingJob" + exception.getMessage());
      throw new JobExecutionException(exception.getMessage());
    }
  }

}
