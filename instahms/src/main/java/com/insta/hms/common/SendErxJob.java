package com.insta.hms.common;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.integration.insurance.erxprescription.ERxService;
import com.insta.hms.jobs.GenericJob;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

public class SendErxJob extends GenericJob {
  private static Logger logger = LoggerFactory.getLogger(SendErxJob.class);
  
  @LazyAutowired
  private ERxService erxService;

  /**
   * This method starts processing a background job based on redis key.
   * 
   * @param the jobContext
   */
  @Override
  protected void executeInternal(JobExecutionContext jobContext) throws JobExecutionException {
    try {
      JobDataMap jobDataMap = jobContext.getJobDetail().getJobDataMap();
      String schema = jobDataMap.get("schema").toString();
      String userName = jobDataMap.get("userName").toString();
      String centerId = jobDataMap.get("centerId").toString();
      String path = jobDataMap.get("path").toString();
      int pbmPrescId = (int) jobDataMap.get("pbmPrescId");
      RequestContext.setConnectionDetails(
          new String[] { null, null, schema, userName, centerId, path });
      Object id = jobDataMap.get("id");
      String patientId = jobDataMap.get("patientId").toString();
      String xml = jobDataMap.get("xmlData").toString();
      erxService.sendERxRequest(id, patientId, Integer.parseInt(centerId), 
          userName, path, pbmPrescId, xml, schema);
    } catch (Exception ex) {
      logger.error("Exception in SendErxJob ", ex);
    }
  }

}
