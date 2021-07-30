package com.insta.hms.core.fa;

import com.insta.hms.billing.accounting.AccountingConstants;
import com.insta.hms.model.HmsAccountingInfoModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The Class SetOffAccountingDataInsertService.
 */
@Service
public class SetOffAccountingDataInsertService {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(AccountingDataInsertService.class);

  /** The accounting data insert repo. */
  @Autowired
  private AccountingDataInsertRepository accountingDataInsertRepo;

  /** The receipts accounting repository. */
  @Autowired
  private ReceiptsAccountingRepository receiptsAccountingRepository;

  /** The deposit set off accounting processor. */
  @Autowired
  private DepositSetOffAccountingProcessor depositSetOffAccountingProcessor;

  /**
   * Process accounting for set off.
   *
   * @param receiptId      receipt identifier from which set offs are to be processed
   * @param setOffBillNo   bill identifier for which set offs are to be processed
   * @param setOffType     set off type to process
   * @param jobTransaction job transaction identifier
   * @param reversalsOnly  process only setoff reversals ?
   * @param createdAt      creation timestamp for voucher
   */
  @Transactional
  public void processAccountingForSetOff(String receiptId, String setOffBillNo, String setOffType,
      Integer jobTransaction, boolean reversalsOnly, Date createdAt) {
    postReversalForSetOff(receiptId, setOffBillNo, jobTransaction, createdAt);
    if (!reversalsOnly) {
      insertAccountingDataForSetOff(receiptId, setOffBillNo, jobTransaction, createdAt);
    }
  }

  /**
   * Insert accounting data for set off.
   *
   * @param receiptId
   *          the receipt id
   * @param setOffBillNo
   *          the set off bill no
   * @param jobTransaction
   *          the job transaction
   */
  private void insertAccountingDataForSetOff(String receiptId, String setOffBillNo,
      Integer jobTransaction, Date createdAt) {
    List<HmsAccountingInfoModel> accountingDataToInsert = getAccountingDataToInsert(receiptId,
        setOffBillNo, jobTransaction, createdAt);
    accountingDataInsertRepo.insertAccountingDataFromList(accountingDataToInsert);
  }

  /**
   * Gets the accounting data to insert.
   *
   * @param receiptId
   *          the receipt id
   * @param setOffBillNo
   *          the set off bill no
   * @param jobTransaction
   *          the job transaction
   * @return the accounting data to insert
   */
  private List<HmsAccountingInfoModel> getAccountingDataToInsert(String receiptId,
      String setOffBillNo, Integer jobTransaction, Date createdAt) {
    List<HmsAccountingInfoModel> accountingDataList = new ArrayList<>();
    List<HmsAccountingInfoModel> setOffAccountingDataList = depositSetOffAccountingProcessor
        .processAccountingForDepositSetOff(receiptId, setOffBillNo);
    for (HmsAccountingInfoModel setOffAccountingData : setOffAccountingDataList) {
      setOffAccountingData.setJobTransaction(jobTransaction);
      setOffAccountingData.setCreatedAt(createdAt);
      setOffAccountingData.setGuid(accountingDataInsertRepo.generateAccountingNextId());
      accountingDataList.add(setOffAccountingData);
    }

    return accountingDataList;
  }

  /**
   * Post reversal for set off.
   *
   * @param receiptId
   *          the receipt id
   * @param setOffBillNo
   *          the set off bill no
   * @param currentJobTransaction
   *          the current job transaction
   */
  private void postReversalForSetOff(String receiptId, String setOffBillNo,
      Integer currentJobTransaction, Date createdAt) {
    // posting reversals has following steps
    // 1. swap debit and credit accounts
    // 2. update created_at
    // 3. Update Transaction type to 'R'
    // 4. Update current jobTransaction
    logger.info("Accounting reversals process started for setOff : {} ", receiptId);
    Integer lastJobTransaction = receiptsAccountingRepository
        .getLastJobTransactionIdForReversalPosts(receiptId, setOffBillNo);
    if (lastJobTransaction != null && receiptId != null) {
      List<HmsAccountingInfoModel> reversalsDataList = new ArrayList<>();
      List<HmsAccountingInfoModel> lastDataForReceipt = receiptsAccountingRepository
          .getLastAccountingDataForSetOff(receiptId, setOffBillNo, lastJobTransaction);
      for (HmsAccountingInfoModel accountingData : lastDataForReceipt) {
        HmsAccountingInfoModel reversalEntry = new HmsAccountingInfoModel();
        BeanUtils.copyProperties(accountingData, reversalEntry);
        reversalEntry.setDebitAccount(accountingData.getCreditAccount());
        reversalEntry.setCreditAccount(accountingData.getDebitAccount());
        reversalEntry.setGuid(accountingDataInsertRepo.generateAccountingNextId());
        reversalEntry.setCreatedAt(createdAt);
        reversalEntry.setTransactionType(AccountingConstants.TRANSACTION_TYPE_R);
        reversalEntry.setJobTransaction(currentJobTransaction);
        reversalsDataList.add(reversalEntry);
      }
      // insert the reversals
      accountingDataInsertRepo.batchInsert(reversalsDataList);
    }
    logger.info("Accounting reversals process completed for Receipt : {} ", receiptId);

  }

}
