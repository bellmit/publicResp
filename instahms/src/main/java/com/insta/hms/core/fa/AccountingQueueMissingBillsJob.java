package com.insta.hms.core.fa;

import com.insta.hms.core.billing.BillService;
import com.insta.hms.core.billing.ReceiptService;
import com.insta.hms.jobs.GenericJob;
import com.insta.hms.model.AccountingFailedExportsModel;

import org.apache.commons.beanutils.BasicDynaBean;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class AccountingQueueMissingBillsJob extends GenericJob {

  @Autowired
  private AccountingDataInsertService accountingDataInsertService;

  @Autowired
  private BillService billService;
  
  @Autowired
  private ReceiptService receiptService;
  
  @Autowired
  private AccountingJobScheduler scheduler;
  
  @Override
  protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
    
    if (!scheduler.isAccountingEnabled()) {
      return;
    }
    
    List<BasicDynaBean> missingBills = 
        billService.getBillsNotInHmsAccountingInfo();
    for (BasicDynaBean missingBill: missingBills) {
      accountingDataInsertService.logFailedExport((String) missingBill.get("bill_no"),
          (String) missingBill.get("visit_id"));
    }
    List<BasicDynaBean> missingReceipts = 
        receiptService.getReceiptsNotInHmsAccountingInfo();
    for (BasicDynaBean missingReceipt: missingReceipts) {
      accountingDataInsertService.logFailedExport((String) missingReceipt.get("receipt_id"));
    }
  }
}
