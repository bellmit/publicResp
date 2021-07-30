package com.insta.hms.core.fa;

import com.insta.hms.jobs.GenericJob;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class AccountingDataInsertJob extends GenericJob {
  @Autowired
  private AccountingDataInsertService accountingDataInsertService;

  @Autowired
  private PharmacyAccountingDataInsertService pharmacyAccountingDataInsertService;

  @Autowired
  private ReceiptsAccountingDataInsertService receiptsAccountingDataInsertService;

  @Autowired
  private SetOffAccountingDataInsertService setOffAccountingDataInsertService;

  private static Logger logger = LoggerFactory.getLogger(AccountingDataInsertJob.class);

  private String billNo;
  private String visitId;
  private Map<String, Object> dataMap;

  @Override
  public void executeInternal(JobExecutionContext jobContext) throws JobExecutionException {
    setJobConnectionDetails();
    Integer jobTransaction = accountingDataInsertService.generateNextJobTransaction();
    Date createdAt = new Date();
    try {
      logger.info("Accounting processing initialized " + billNo);
      boolean reversalsOnly = (dataMap.containsKey("reversalsOnly") && (Boolean) dataMap
          .get("reversalsOnly") != null) ? (Boolean) dataMap.get("reversalsOnly") : Boolean.FALSE;
      if (billNo != null) {
        accountingDataInsertService.processAccountingForBill(billNo, jobTransaction, reversalsOnly,
            createdAt);
      }
  
      if (dataMap.containsKey("phBills")) {
        processAccountingForPharmacyBills(dataMap, jobTransaction, createdAt);
      }

      logger.info("Accounting processing completed " + billNo);
      accountingDataInsertService.removeLogForFailedExport(billNo, visitId);
    } catch (Exception ex) {
      accountingDataInsertService.logFailedExport(billNo, visitId);
      logger.error("{}", ex);
    }

    if (dataMap.containsKey("receipt")) {
      Map<String, Object> receiptDataMap = (Map<String, Object>) dataMap.get("receipt");
      if (receiptDataMap != null && !receiptDataMap.isEmpty()) {
        processAccountingForReceipt(receiptDataMap, jobTransaction, createdAt);
      }
    }

    if (dataMap.containsKey("ReceiptsList")) {
      processAccountingForReceiptsList(dataMap, jobTransaction, createdAt);
    }
  }

  private void processAccountingForPharmacyBills(Map<String, Object> dataMap,
      Integer jobTransaction, Date createdAt) {
    List<Map<String, Object>> phBillsList = (List<Map<String, Object>>) dataMap.get("phBills");
    if (phBillsList != null && !phBillsList.isEmpty()) {
      for (Map<String, Object> phBillDetails : phBillsList) {
        String phBillNo = phBillDetails.get("phBillNo") != null ? (String) phBillDetails
            .get("phBillNo") : null;
        Boolean reversalsOnly = phBillDetails.get("reversalsOnly") != null ? (Boolean) phBillDetails
            .get("reversalsOnly") : Boolean.FALSE;
        if (phBillNo != null) {
          pharmacyAccountingDataInsertService.processAccountingForPharmacyBill(phBillNo,
              jobTransaction, reversalsOnly, createdAt);
        }
      }
    }
  }

  private void processAccountingForReceipt(Map<String, Object> receiptDataMap,
      Integer jobTransaction, Date createdAt) {
    String receiptId = null;
    try {
      receiptId = receiptDataMap.get("receiptId") != null ? (String) receiptDataMap
        .get("receiptId") : null;
      Boolean reversalsOnly = receiptDataMap.get("reversalsOnly") != null ? (Boolean) receiptDataMap
          .get("reversalsOnly") : Boolean.FALSE;
      String setOffBillNo = receiptDataMap.get("setOffBillNo") != null ? (String) receiptDataMap
          .get("setOffBillNo") : null;
      String setOffType = receiptDataMap.get("setOffType") != null ? (String) receiptDataMap
          .get("setOffType") : null;
  
      if (receiptId != null && setOffBillNo != null) {
        setOffAccountingDataInsertService.processAccountingForSetOff(receiptId, setOffBillNo,
            setOffType, jobTransaction, reversalsOnly, createdAt);
      } else if (receiptId != null) {
        receiptsAccountingDataInsertService.processAccountingForReceipt(receiptId, jobTransaction,
            reversalsOnly, createdAt);
      }
      accountingDataInsertService.removeLogForFailedExport(receiptId);
    } catch (Exception ex) {
      accountingDataInsertService.logFailedExport(receiptId);
      logger.error("{}", ex);
    }
  }

  private void processAccountingForReceiptsList(Map<String, Object> dataMap,
      Integer jobTransaction, Date createdAt) {
    List<Map<String, Object>> receiptsList = (List<Map<String, Object>>) dataMap
        .get("ReceiptsList");
    if (receiptsList != null && !receiptsList.isEmpty()) {
      for (Map<String, Object> receiptDataMap : receiptsList) {
        processAccountingForReceipt(receiptDataMap, jobTransaction, createdAt);
      }
    }
  }

  public String getBillNo() {
    return billNo;
  }

  public void setBillNo(String billNo) {
    this.billNo = billNo;
  }

  public Map<String, Object> getDataMap() {
    return dataMap;
  }

  public void setDataMap(Map<String, Object> dataMap) {
    this.dataMap = dataMap;
  }

  public String getVisitId() {
    return visitId;
  }

  public void setVisitId(String visitId) {
    this.visitId = visitId;
  }

}
