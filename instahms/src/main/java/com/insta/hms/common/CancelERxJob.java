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


public class CancelERxJob extends GenericJob {
  
  private static Logger logger = LoggerFactory.getLogger(CancelERxJob.class);
  
  @LazyAutowired
  private ERxService erxService;
  
  @Override
  public void executeInternal(JobExecutionContext jobExecutionContext) 
      throws JobExecutionException {
    try {
      JobDataMap jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();
      String schema = jobDataMap.get("schema").toString();
      String centerId = jobDataMap.get("centerId").toString();
      Object id = jobDataMap.get("id"); // consultationId
      String patientId = jobDataMap.get("patientId").toString();
      int pbmPrescId = (int) jobDataMap.get("pbmPrescId");
      String path = jobDataMap.get("path").toString();
      String xml = jobDataMap.get("xml").toString();
      String userId = jobDataMap.get("userId").toString();
      
      RequestContext.setConnectionDetails(
          new String[] { null, null, schema, userId, centerId, path });
      erxService.cancelERxRequest(patientId, id, pbmPrescId, path, xml, userId, schema,
          Integer.parseInt(centerId));        
    } catch (Exception ex) {
      logger.error("Exception in CancelERxJob ", ex);
    }
  }

}
