package com.insta.hms.core.fa;

import com.insta.hms.jobs.GenericJob;
import com.insta.hms.model.AccountingFailedExportsModel;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class AccountingFailedExportRetryJob extends GenericJob {

  @Autowired
  private AccountingDataInsertService accountingDataInsertService;

  @Autowired
  private AccountingJobScheduler scheduler;
  
  @Override
  protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
    List<AccountingFailedExportsModel> failedExports = 
        accountingDataInsertService.getFailedExports();
    for (AccountingFailedExportsModel failedExport: failedExports) {
      if (failedExport.getBillNo() != null && failedExport.getVisitId() != null) {
        scheduler.scheduleAccountingForBill(failedExport.getVisitId(), failedExport.getBillNo());
      } else if (failedExport.getReceiptId() != null) {
        scheduler.scheduleAccountingForReceipt(failedExport.getReceiptId());
      }
    }
  }
}
