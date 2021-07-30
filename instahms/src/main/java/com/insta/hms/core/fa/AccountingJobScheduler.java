package com.insta.hms.core.fa;

import static com.insta.hms.jobs.common.QuartzJobHelper.buildJob;

import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.JobSchedulingService;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.modulesactivated.ModulesActivatedService;
import com.insta.hms.jobs.JobService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class AccountingJobScheduler.
 */
@Component
public class AccountingJobScheduler {

  /** The accounting job scheduler service. */
  @Autowired
  private AccountingJobSchedulerService accountingJobSchedulerService;
  
  @LazyAutowired
  ModulesActivatedService modulesActivatedService;  
  
  public boolean isAccountingEnabled() {
    return modulesActivatedService
            .isModuleActivated("mod_accounting");
  }

  /**
   * Schedule accounting for bill.
   *
   * @param visitId the visit id
   * @param billNo  the bill no
   */
  public void scheduleAccountingForBill(String visitId, String billNo) {
    
    if (!isAccountingEnabled()) {
      return;
    }
    
    Map<String, Object> accountingJobData = new HashMap<String, Object>();
    Map<String, Object> dataMap = new HashMap<String, Object>();
    dataMap.put("bill_no", billNo);
    dataMap.put("phBills",
        accountingJobSchedulerService.getAllPharmacyBillsOfBill(billNo, Boolean.FALSE));

    // schedule accounting job for bill
    accountingJobData.put("visitId", visitId);
    accountingJobData.put("billNo", billNo);
    accountingJobData.put("schema", RequestContext.getSchema());
    accountingJobData.put("dataMap", dataMap);
    JobService accountingJobService = JobSchedulingService.getJobService();
    String uniqueName = DateUtil.getCurrentISO8601TimestampMillis() + "_"
        + RequestContext.getUserName();
    accountingJobService.scheduleImmediate(buildJob("AccountingJob_" + billNo + "_" + uniqueName,
        AccountingDataInsertJob.class, accountingJobData));
  }

  /**
   * Schedule accounting for bill.
   *
   * @param visitId the visit id
   * @param billNo  the bill no
   * @param dataMap the data map
   */

  public void scheduleAccountingForBill(String visitId, String billNo,
      Map<String, Object> dataMap) {
    
    if (!isAccountingEnabled()) {
      return;
    }
    
    Map<String, Object> accountingJobData = new HashMap<String, Object>();
    dataMap.put("bill_no", billNo);

    // schedule accounting job for bill
    accountingJobData.put("visitId", visitId);
    accountingJobData.put("billNo", billNo);
    accountingJobData.put("schema", RequestContext.getSchema());
    accountingJobData.put("dataMap", dataMap);
    JobService accountingJobService = JobSchedulingService.getJobService();
    String uniqueName = DateUtil.getCurrentISO8601TimestampMillis() + "_"
        + RequestContext.getUserName();
    accountingJobService.scheduleImmediate(buildJob("AccountingJob_" + billNo + "_" + uniqueName,
        AccountingDataInsertJob.class, accountingJobData));
  }

  /**
   * Schedule accounting for bills.
   *
   * @param billsList the bills list
   */
  public void scheduleAccountingForBills(List<BasicDynaBean> billsList) {
    for (BasicDynaBean billBean : billsList) {
      if (((String) billBean.get("status")).equals("F")
          || ((String) billBean.get("status")).equals("C")) {
        scheduleAccountingForBill((String) billBean.get("visit_id"),
            (String) billBean.get("bill_no"));
      }
    }
  }

  /**
   * Schedule accounting for bill now to bill later change.
   *
   * @param visitId       the visit id
   * @param billNo        the bill no
   * @param reversalsOnly the reversals only
   */
  public void scheduleAccountingForBillNowToBillLaterChange(String visitId, String billNo,
      boolean reversalsOnly) {
    Map<String, Object> dataMap = new HashMap<String, Object>();
    dataMap.put("reversalsOnly", reversalsOnly);
    scheduleAccountingForBill(visitId, billNo, dataMap);
  }

  /**
   * Schedule accounting for bill reversals only.
   *
   * @param visitId the visit id
   * @param billNo  the bill no
   */
  public void scheduleAccountingForBillReversalsOnly(String visitId, String billNo) {
    Map<String, Object> dataMap = new HashMap<String, Object>();
    dataMap.put("reversalsOnly", Boolean.TRUE);
    dataMap.put("phBills",
        accountingJobSchedulerService.getAllPharmacyBillsOfBill(billNo, Boolean.TRUE));
    scheduleAccountingForBill(visitId, billNo, dataMap);
  }

  /**
   * Schedule accounting for sales. This method is useful when sales is done with raise bill option
   * and bill got closed/finalized from sales screen itself. for such bills it will post the
   * accounting entries.
   *
   * @param bills the bills
   */
  public void scheduleAccountingForSales(List<BasicDynaBean> bills) {
    Map<String, Object> dataMap = new HashMap<>();
    for (BasicDynaBean billBean : bills) {
      String billNo = (String) billBean.get("bill_no");
      String status = (String) billBean.get("status");
      if ("F".equals(status) || "C".equals(status)) {
        dataMap.put("phBills",
            accountingJobSchedulerService.getAllPharmacyBillsOfBill(billNo, Boolean.FALSE));
        scheduleAccountingForBill((String) billBean.get("visit_id"), billNo, dataMap);
      }
    }
  }

  /**
   * Schedule accounting for receipt.
   *
   * @param receiptId the receipt id
   */
  public void scheduleAccountingForReceipt(String receiptId) {
    Map<String, Object> receiptData = new HashMap<>();
    receiptData.put("receiptId", receiptId);
    receiptData.put("reversalsOnly", Boolean.FALSE);
    scheduleAccountingForReceipt(receiptData);
  }
  
  /**
   * Schedule accounting for receipt.
   *
   * @param receiptData the receipt data
   */
  public void scheduleAccountingForReceipt(Map<String, Object> receiptData) {

    if (!isAccountingEnabled()) {
      return;
    }

    Map<String, Object> accountingJobData = new HashMap<>();
    Map<String, Object> dataMap = new HashMap<>();
    dataMap.put("receipt", receiptData);
    // schedule accounting job for bill
    accountingJobData.put("schema", RequestContext.getSchema());
    accountingJobData.put("dataMap", dataMap);
    JobService accountingJobService = JobSchedulingService.getJobService();
    String uniqueName = DateUtil.getCurrentISO8601TimestampMillis() + "_"
        + RequestContext.getUserName();
    accountingJobService.scheduleImmediate(buildJob(
        "AccountingJob_" + (String) receiptData.get("receiptId") + "_" + uniqueName,
        AccountingDataInsertJob.class, accountingJobData));
  }

  /**
   * Schedule accounting for receipts list.
   *
   * @param receiptsDataList
   *          the receipts data list
   */
  public void scheduleAccountingForReceiptsList(List<Map<String, Object>> receiptsDataList) {
    if (!isAccountingEnabled() || receiptsDataList == null || receiptsDataList.isEmpty()) {
      return;
    }
    Map<String, Object> accountingJobData = new HashMap<>();
    Map<String, Object> dataMap = new HashMap<>();
    dataMap.put("ReceiptsList", receiptsDataList);
    // schedule accounting job for bill
    accountingJobData.put("schema", RequestContext.getSchema());
    accountingJobData.put("dataMap", dataMap);
    JobService accountingJobService = JobSchedulingService.getJobService();
    String uniqueName = DateUtil.getCurrentISO8601TimestampMillis() + "_"
        + RequestContext.getUserName();
    accountingJobService.scheduleImmediate(buildJob("AccountingJob_" + "ReceiptsList" + "_"
        + uniqueName, AccountingDataInsertJob.class, accountingJobData));
  }
}
