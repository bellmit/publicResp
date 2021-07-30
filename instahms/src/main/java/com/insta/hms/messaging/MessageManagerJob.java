package com.insta.hms.messaging;

import com.bob.hms.common.RequestContext;
import com.insta.hms.jobs.GenericJob;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.List;
import java.util.Map;

public class MessageManagerJob extends GenericJob {

  private static Logger logger = LoggerFactory.getLogger(MessageManagerJob.class);

  @Override
  public void executeInternal(JobExecutionContext jobContext) throws JobExecutionException {

    MessageManager mgr = new MessageManager();
    try {
      JobDataMap jobDataMap = jobContext.getJobDetail().getJobDataMap();
      RequestContext
          .setConnectionDetails(new String[] { null, null, jobDataMap.get("schema").toString(),
              jobDataMap.get("userName").toString(), jobDataMap.get("centerId").toString(), null,
              null, jobDataMap.get("userLocale").toString() });
      MDC.put("schema", jobDataMap.get("schema").toString());
      MDC.put("username", jobDataMap.get("userName").toString());
      MessageContext ctx = MessageContext.fromType(jobDataMap.get("messageTypeId").toString());
      ctx.setEventData((Map) jobDataMap.get("eventData"));

      // Initialize the builder with the input context
      MessageBuilder builder = MessageBuilderFactory
          .getBuilder(jobDataMap.get("messageTypeId").toString());

      // Build the messages
      builder.build(ctx);
      List<Message> msgs = builder.getMessageList();
      if (null != msgs && !msgs.isEmpty()) {
        mgr.sendMessages(jobDataMap.get("messageTypeId").toString(),
            jobDataMap.get("messageMode").toString(), msgs);
      }
    } catch (Exception ex) {
      logger.error("Exception in MessageManagerJob : " + ex.getMessage());
    } finally {
      MDC.clear();
    }
  }

}