package com.insta.hms.core.fa;

import com.insta.hms.billing.accounting.AccountingConstants;
import com.insta.hms.core.billing.ReceiptModel;
import com.insta.hms.core.billing.ReceiptRefundReferenceModel;
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
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class ReceiptsAccountingDataInsertService.
 */
@Service
public class ReceiptsAccountingDataInsertService {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(AccountingDataInsertService.class);

  /** The accounting data insert repo. */
  @Autowired
  private AccountingDataInsertRepository accountingDataInsertRepo;

  /** The receipts accounting repository. */
  @Autowired
  private ReceiptsAccountingRepository receiptsAccountingRepository;

  /** The patient receipt accounting processor. */
  @Autowired
  private PatientReceiptAccountingProcessor patientReceiptAccountingProcessor;

  /** The sponsor receipt accounting processor. */
  @Autowired
  private SponsorReceiptAccountingProcessor sponsorReceiptAccountingProcessor;

  /** The patient refund accounting processor. */
  @Autowired
  private PatientRefundAccountingProcessor patientRefundAccountingProcessor;

  /** The patient write off accounting processor. */
  @Autowired
  private PatientWriteOffAccountingProcessor patientWriteOffAccountingProcessor;

  /** The sponsor write off accounting processor. */
  @Autowired
  private SponsorWriteOffAccountingProcessor sponsorWriteOffAccountingProcessor;

  /** The reward points receipt accounting processor. */
  @Autowired
  private RewardPointsReceiptAccountingProcessor rewardPointsReceiptAccountingProcessor;

  /** The patient write off refund accounting processor. */
  @Autowired
  private PatientWriteOffRefundAccountingProcessor patientWriteOffRefundAccountingProcessor;

  /** The Sponsor write off refund accounting processor. */
  @Autowired
  private SponsorWriteOffRefundAccountingProcessor sponsorWriteOffRefundAccountingProcessor;

  /** The deposit receipt accounting processor. */
  @Autowired
  private DepositReceiptAccountingProcessor depositReceiptAccountingProcessor;

  /** The deposit refund accounting processor. */
  @Autowired
  private DepositRefundAccountingProcessor depositRefundAccountingProcessor;

  /**
   * Process accounting for receipt.
   *
   * @param receiptId
   *          the receipt id
   * @param jobTransaction
   *          the job transaction
   * @param reversalsOnly
   *          the reversals only
   */
  @Transactional
  public void processAccountingForReceipt(String receiptId, Integer jobTransaction,
      boolean reversalsOnly, Date createdAt) {
    postReversalForReceipt(receiptId, jobTransaction, createdAt);
    if (!reversalsOnly) {
      insertAccountingDataForReceipt(receiptId, jobTransaction, createdAt);
    }
  }

  /**
   * Insert accounting data for receipt.
   *
   * @param receiptId
   *          the receipt id
   * @param jobTransaction
   *          the job transaction
   */
  private void insertAccountingDataForReceipt(String receiptId, Integer jobTransaction,
      Date createdAt) {
    List<HmsAccountingInfoModel> accountingDataToInsert = getAccountingDataToInsert(receiptId,
        jobTransaction, createdAt);
    accountingDataInsertRepo.insertAccountingDataFromList(accountingDataToInsert);
  }

  /**
   * Gets the accounting data to insert.
   *
   * @param receiptId
   *          the receipt id
   * @param jobTransaction
   *          the job transaction
   * @return the accounting data to insert
   */
  private List<HmsAccountingInfoModel> getAccountingDataToInsert(String receiptId,
      Integer jobTransaction, Date createdAt) {
    List<HmsAccountingInfoModel> accountingDataList = new ArrayList<>();

    ReceiptModel receipt = (ReceiptModel) accountingDataInsertRepo.load(ReceiptModel.class,
        receiptId);
    ReceiptsAccounting receiptAccounting = getReceiptAccountingProcessor(receipt);
    if (receipt == null || receiptAccounting == null) {
      return accountingDataList;
    }
    
    List<HmsAccountingInfoModel> receiptAccountingDataList = receiptAccounting
        .processAccountingForReceipt(receipt);
    for (HmsAccountingInfoModel receiptAccountingData : receiptAccountingDataList) {
      receiptAccountingData.setJobTransaction(jobTransaction);
      receiptAccountingData.setCreatedAt(createdAt);
      receiptAccountingData.setModTime(receipt.getModifiedAt());
      receiptAccountingData.setGuid(accountingDataInsertRepo.generateAccountingNextId());
      accountingDataList.add(receiptAccountingData);
    }

    List<HmsAccountingInfoModel> receiptTaxAccountingDataList = receiptAccounting
        .processAccountingForReceiptTaxes(receipt);

    for (HmsAccountingInfoModel receiptTaxAccountingData : receiptTaxAccountingDataList) {
      receiptTaxAccountingData.setJobTransaction(jobTransaction);
      receiptTaxAccountingData.setCreatedAt(createdAt);
      receiptTaxAccountingData.setModTime(receipt.getModifiedAt());
      receiptTaxAccountingData.setGuid(accountingDataInsertRepo.generateAccountingNextId());
      accountingDataList.add(receiptTaxAccountingData);
    }

    return accountingDataList;
  }

  /**
   * Gets the receipt accounting processor.
   *
   * @param receipt
   *          the receipt
   * @return the receipt accounting processor
   */
  private ReceiptsAccounting getReceiptAccountingProcessor(ReceiptModel receipt) {
    boolean isPatientReceipt = receipt != null && receipt.getReceiptType().equals("R")
        && !receipt.getIsDeposit()
        && (receipt.getPointsRedeemed() == null || receipt.getPointsRedeemed().intValue() == 0)
        && receipt.getTpaId() == null;
    boolean isSponsorReceipt = receipt != null && receipt.getReceiptType().equals("R")
        && !receipt.getIsDeposit()
        && (receipt.getPointsRedeemed() == null || receipt.getPointsRedeemed().intValue() == 0)
        && receipt.getTpaId() != null;
    boolean isPatientRefund = receipt != null && receipt.getReceiptType().equals("F")
        && !receipt.getIsDeposit()
        && !receiptsAccountingRepository.isWriteOffRefundReceipt(receipt.getReceiptId())
        && (receipt.getPointsRedeemed() == null || receipt.getPointsRedeemed().intValue() == 0)
        && receipt.getTpaId() == null;
    boolean isPatientWriteOff = receipt != null && receipt.getReceiptType().equals("W")
        && !receipt.getIsDeposit()
        && (receipt.getPointsRedeemed() == null || receipt.getPointsRedeemed().intValue() == 0)
        && receipt.getTpaId() == null;
    boolean isSponsorWriteOff = receipt != null && receipt.getReceiptType().equals("W")
        && !receipt.getIsDeposit()
        && (receipt.getPointsRedeemed() == null || receipt.getPointsRedeemed().intValue() == 0)
        && receipt.getTpaId() != null;
    boolean isPatientWriteOffRefund = receipt != null && receipt.getReceiptType().equals("F")
        && !receipt.getIsDeposit()
        && receiptsAccountingRepository.isWriteOffRefundReceipt(receipt.getReceiptId())
        && (receipt.getPointsRedeemed() == null || receipt.getPointsRedeemed().intValue() == 0)
        && receipt.getTpaId() == null;
    boolean isSponsorWriteOffRefund = receipt != null && receipt.getReceiptType().equals("F")
        && !receipt.getIsDeposit()
        && receiptsAccountingRepository.isWriteOffRefundReceipt(receipt.getReceiptId())
        && (receipt.getPointsRedeemed() == null || receipt.getPointsRedeemed().intValue() == 0)
        && receipt.getTpaId() != null;
    boolean isRewardPointReceipt = receipt != null && receipt.getReceiptType().equals("R")
        && !receipt.getIsDeposit()
        && (receipt.getPointsRedeemed() != null && receipt.getPointsRedeemed().intValue() > 0)
        && receipt.getTpaId() == null;
    boolean isDepositReceipt = receipt != null && receipt.getReceiptType().equals("R")
        && receipt.getIsDeposit();
    boolean isDepositRefund = receipt != null && receipt.getReceiptType().equals("F")
        && receipt.getIsDeposit();

    if (isPatientReceipt) {
      return patientReceiptAccountingProcessor;
    } else if (isSponsorReceipt) {
      return sponsorReceiptAccountingProcessor;
    } else if (isPatientRefund) {
      return patientRefundAccountingProcessor;
    } else if (isPatientWriteOff) {
      return patientWriteOffAccountingProcessor;
    } else if (isSponsorWriteOff) {
      return sponsorWriteOffAccountingProcessor;
    } else if (isPatientWriteOffRefund) {
      return patientWriteOffRefundAccountingProcessor;
    } else if (isSponsorWriteOffRefund) {
      return sponsorWriteOffRefundAccountingProcessor;
    } else if (isRewardPointReceipt) {
      return rewardPointsReceiptAccountingProcessor;
    } else if (isDepositReceipt) {
      return depositReceiptAccountingProcessor;
    } else if (isDepositRefund) {
      return depositRefundAccountingProcessor;
    }
    return null;
  }

  /**
   * Post reversal for receipt.
   *
   * @param receiptId
   *          the receipt id
   * @param currentJobTransaction
   *          the current job transaction
   */
  private void postReversalForReceipt(String receiptId, Integer currentJobTransaction,
      Date createdAt) {
    // posting reversals has following steps
    // 1. swap debit and credit accounts
    // 2. update created_at
    // 3. Update Transaction type to 'R'
    // 4. Update current jobTransaction
    logger.info("Accounting reversals process started for Receipt : {} ", receiptId);
    ReceiptModel receipt = (ReceiptModel) accountingDataInsertRepo.load(ReceiptModel.class,
        receiptId);
    Integer lastJobTransaction = null;
    if (receipt != null && receipt.getIsDeposit()) {
      lastJobTransaction = receiptsAccountingRepository
          .getLastJobTransactionIdForReversalPostsDeposits(receiptId);
    } else {
      lastJobTransaction = receiptsAccountingRepository
          .getLastJobTransactionIdForReversalPosts(receiptId);
    }

    if (lastJobTransaction != null && receiptId != null) {
      List<HmsAccountingInfoModel> reversalsDataList = new ArrayList<>();
      List<HmsAccountingInfoModel> lastDataForReceipt = receiptsAccountingRepository
          .getLastAccountingDataForReceipt(receiptId, lastJobTransaction);
      for (HmsAccountingInfoModel accountingData : lastDataForReceipt) {
        HmsAccountingInfoModel reversalEntry = new HmsAccountingInfoModel();
        BeanUtils.copyProperties(accountingData, reversalEntry);
        reversalEntry.setDebitAccount(accountingData.getCreditAccount());
        reversalEntry.setCreditAccount(accountingData.getDebitAccount());
        reversalEntry.setGuid(accountingDataInsertRepo.generateAccountingNextId());
        reversalEntry.setCreatedAt(createdAt);
        reversalEntry.setTransactionType(AccountingConstants.TRANSACTION_TYPE_R);
        reversalEntry.setJobTransaction(currentJobTransaction);
        reversalEntry.setPatientFullName(accountingData.getPatientFullName());
        reversalsDataList.add(reversalEntry);
      }
      // insert the reversals
      accountingDataInsertRepo.batchInsert(reversalsDataList);
    }
    logger.info("Accounting reversals process completed for Receipt : {} ", receiptId);

  }
}
