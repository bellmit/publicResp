package com.insta.hms.core.billing;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.jobs.GenericJob;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class BillChargeAllocationJob extends GenericJob {

  @LazyAutowired
  AllocationService allocationService;

  static Logger logger = LoggerFactory
      .getLogger(BillChargeAllocationJob.class);

  @Override
  protected void executeInternal(JobExecutionContext context) throws JobExecutionException {

    String billNo = (String) context.getJobDetail().getJobDataMap().get("bill_no");
    Integer centerId = (Integer) context.getJobDetail().getJobDataMap().get("centerId");

    logger.debug("Starting bill charge allocation for bill number: " + billNo);
    allocationService.allocate(billNo, centerId);
    logger.debug("Finished bill charge allocation for bill number:" + billNo);

  }

}
