package com.insta.hms.integration.regulatory.ohsrsdohgovph;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.jobs.GenericJob;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

public class OhsrsdohgovphReportGenerationJob extends GenericJob {

  private static Logger logger = LoggerFactory.getLogger(OhsrsdohgovphReportGenerationJob.class);

  @LazyAutowired
  RedisTemplate<String, Object> redisTemplate;

  @LazyAutowired
  OhsrsdohgovphService service;
  
  @Override
  protected void executeInternal(JobExecutionContext jobContext) throws JobExecutionException {
    JobDataMap jobDataMap = jobContext.getJobDetail().getJobDataMap();
    String schema = jobDataMap.get("schema").toString();
    String userName = jobDataMap.get("userName").toString();
    String centerId = jobDataMap.get("centerId").toString();
    int year = jobDataMap.getInt("reportingYear");
    RequestContext.setConnectionDetails(new String[] { null, null, schema, userName, centerId });
    logger.info("generating report for center : " + centerId + ", year : " + String.valueOf(year));
    if (service.generateReport(year)) {
      logger.info("Report generated for center : " + centerId + ", year : " + String.valueOf(year));
    } else {
      logger.error("Report generation failed for center : " + centerId + ", year : "
            + String.valueOf(year));      
    }
  }

}
