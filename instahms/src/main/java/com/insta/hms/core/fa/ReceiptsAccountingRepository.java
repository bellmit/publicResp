package com.insta.hms.core.fa;

import com.insta.hms.common.GenericHibernateRepository;
import com.insta.hms.core.billing.BillReceiptsModel;
import com.insta.hms.model.HmsAccountingInfoModel;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class ReceiptsAccountingRepository.
 */
@Repository
public class ReceiptsAccountingRepository extends GenericHibernateRepository {

  /** The Constant GET_LAST_JOB_TRANSACTION_ID_FOR_RECEIPT. */
  private static final String GET_LAST_JOB_TRANSACTION_ID_FOR_RECEIPT = " SELECT "
      + " MAX(jobTransaction) AS max_job_transaction FROM HmsAccountingInfoModel haim "
      + " WHERE haim.voucherNo=? AND haim.voucherType IN (select voucherDefinition from FaVoucherDefinitionsModel fvd where fvd.voucherKey in ('VOUCHER_TYPE_RECEIPT','VOUCHER_TYPE_PAYMENT')) "
      + " AND jobTransaction is not null AND jobTransaction > 0 "
      + " AND haim.billNo is not null GROUP BY haim.jobTransaction "
      + " ORDER BY jobTransaction DESC ";

  /**
   * Gets the last job transaction id for reversal posts.
   *
   * @param receiptId
   *          the bill no
   * @return the last job transaction id for reversal posts
   */
  @SuppressWarnings("unchecked")
  public Integer getLastJobTransactionIdForReversalPosts(String receiptId) {
    List<Integer> resultList = (List<Integer>) executeHqlQuery(
        GET_LAST_JOB_TRANSACTION_ID_FOR_RECEIPT, new Object[] { receiptId }, 1);
    if (resultList != null && !resultList.isEmpty()) {
      return resultList.get(0);
    }
    return null;
  }

  /** The Constant GET_LAST_JOB_TRANSACTION_ID_FOR_SETOFF. */
  private static final String GET_LAST_JOB_TRANSACTION_ID_FOR_SETOFF = " SELECT "
      + " MAX(jobTransaction) AS max_job_transaction FROM HmsAccountingInfoModel haim "
      + " WHERE haim.voucherNo=? AND haim.billNo=? AND haim.voucherType IN (select voucherDefinition from FaVoucherDefinitionsModel fvd where fvd.voucherKey in ('VOUCHER_TYPE_RECEIPT','VOUCHER_TYPE_PAYMENT')) "
      + " AND jobTransaction is not null AND jobTransaction > 0 "
      + "  GROUP BY haim.jobTransaction ORDER BY jobTransaction DESC ";

  /**
   * Gets the last job transaction id for reversal posts.
   *
   * @param receiptId
   *          the receipt id
   * @param setOffBillNo
   *          the set off bill no
   * @return the last job transaction id for reversal posts
   */
  @SuppressWarnings("unchecked")
  public Integer getLastJobTransactionIdForReversalPosts(String receiptId, String setOffBillNo) {
    List<Integer> resultList = (List<Integer>) executeHqlQuery(
        GET_LAST_JOB_TRANSACTION_ID_FOR_SETOFF, new Object[] { receiptId, setOffBillNo }, 1);
    if (resultList != null && !resultList.isEmpty()) {
      return resultList.get(0);
    }
    return null;
  }

  /** The Constant GET_LAST_ACCOUNTING_DATA_FOR_BILL. */
  private static final String GET_LAST_ACCOUNTING_DATA_FOR_BILL = " FROM "
      + " HmsAccountingInfoModel haim WHERE haim.transactionType='N' "
      + " AND haim.voucherType IN (select voucherDefinition from FaVoucherDefinitionsModel fvd where fvd.voucherKey in ('VOUCHER_TYPE_RECEIPT','VOUCHER_TYPE_PAYMENT')) "
      + " AND haim.voucherNo=? AND haim.jobTransaction=? ";

  /**
   * Gets the last accounting data for receipt.
   *
   * @param receiptId
   *          the receipt id
   * @param jobTransaction
   *          the job transaction
   * @return the last accounting data for receipt
   */
  public List<HmsAccountingInfoModel> getLastAccountingDataForReceipt(String receiptId,
      Integer jobTransaction) {
    return (List<HmsAccountingInfoModel>) executeHqlQuery(GET_LAST_ACCOUNTING_DATA_FOR_BILL,
        new Object[] { receiptId, jobTransaction });
  }

  /** The Constant GET_LAST_ACCOUNTING_DATA_FOR_SETOFF. */
  private static final String GET_LAST_ACCOUNTING_DATA_FOR_SETOFF = " FROM "
      + " HmsAccountingInfoModel haim WHERE haim.transactionType='N' "
      + " AND haim.voucherType IN (select voucherDefinition from FaVoucherDefinitionsModel fvd where fvd.voucherKey in ('VOUCHER_TYPE_RECEIPT','VOUCHER_TYPE_PAYMENT')) "
      + " AND haim.voucherNo=? AND haim.billNo=? AND haim.jobTransaction=? ";

  /**
   * Gets the last accounting data for set off.
   *
   * @param receiptId
   *          the receipt id
   * @param setOffBillNo
   *          the set off bill no
   * @param jobTransaction
   *          the job transaction
   * @return the last accounting data for set off
   */
  @SuppressWarnings("unchecked")
  public List<HmsAccountingInfoModel> getLastAccountingDataForSetOff(String receiptId,
      String setOffBillNo, Integer jobTransaction) {
    return (List<HmsAccountingInfoModel>) executeHqlQuery(GET_LAST_ACCOUNTING_DATA_FOR_SETOFF,
        new Object[] { receiptId, setOffBillNo, jobTransaction });
  }

  /** The Constant GET_IS_WRITEOFF_REFUND_RECEIPT. */
  private static final String GET_IS_WRITEOFF_REFUND_RECEIPT = " SELECT count(1) FROM "
      + " ReceiptRefundReferenceModel rrm JOIN  rrm.receipt r ON ( r.receiptType='W' ) "
      + " WHERE rrm.refundReceipt=:receiptId ";

  /**
   * Checks if is write off refund receipt.
   *
   * @param receiptId
   *          the receipt id
   * @return true, if is write off refund receipt
   */
  public boolean isWriteOffRefundReceipt(String receiptId) {
    Session session = getSession();
    Query query = session.createQuery(GET_IS_WRITEOFF_REFUND_RECEIPT);
    query.setString("receiptId", receiptId);
    return ((long) query.uniqueResult()) > 0;
  }

  /** The Constant GET_WRITEOFF_REFUND_BILLRECEIPTS_MODEL. */
  private static final String GET_WRITEOFF_REFUND_BILLRECEIPTS_MODEL = " SELECT br FROM "
      + " ReceiptRefundReferenceModel rrm JOIN  rrm.receipt r ON ( r.receiptType='W' ) "
      + " JOIN r.billReceipts br WHERE rrm.refundReceipt=:receiptId ";

  /**
   * Gets the bill receipt model for write off refund receipt.
   *
   * @param receiptId
   *          the receipt id
   * @return the bill receipt model for write off refund receipt
   */
  public BillReceiptsModel getBillReceiptModelForWriteOffRefundReceipt(String receiptId) {
    Session session = getSession();
    Query query = session.createQuery(GET_WRITEOFF_REFUND_BILLRECEIPTS_MODEL);
    query.setString("receiptId", receiptId);
    List resultList = query.list();
    return (resultList != null && !resultList.isEmpty()) ? (BillReceiptsModel) resultList.get(0)
        : null;
  }

  /** The Constant GET_RECEIPT_TOTAL_TAX. */
  private static final String GET_RECEIPT_TOTAL_TAX = "SELECT COALESCE(SUM(taxAmount), 0) "
      + " FROM ReceiptTaxModel WHERE receiptId = :receiptId";

  /**
   * Gets the receipt total tax.
   *
   * @param receiptId
   *          the receipt id
   * @return the receipt total tax
   */
  public BigDecimal getReceiptTotalTax(String receiptId) {
    Session session = getSession();
    Query query = session.createQuery(GET_RECEIPT_TOTAL_TAX);
    query.setString("receiptId", receiptId);
    return (BigDecimal) query.uniqueResult();
  }

  /** The Constant GET_REFUND_RECEIPT_TOTAL_TAX. */
  private static final String GET_REFUND_RECEIPT_TOTAL_TAX = "SELECT COALESCE(SUM(taxAmount), 0) "
      + " FROM ReceiptRefundReferenceModel WHERE refundReceipt = :refundReceiptId";

  /**
   * Gets the refund receipt total tax.
   *
   * @param refundReceiptId
   *          the refund receipt id
   * @return the refund receipt total tax
   */
  public BigDecimal getRefundReceiptTotalTax(String refundReceiptId) {
    Session session = getSession();
    Query query = session.createQuery(GET_REFUND_RECEIPT_TOTAL_TAX);
    query.setString("refundReceiptId", refundReceiptId);
    return (BigDecimal) query.uniqueResult();
  }

  /** The Constant GET_REFUND_RECEIPT_TAX_DETAILS. */
  private static final String GET_REFUND_RECEIPT_TAX_DETAILS = " SELECT new map(rrrm as rrm, "
      + " fromReceipt as fromReceipt, fromReceiptTaxes as fromReceiptTaxes) FROM "
      + " ReceiptRefundReferenceModel rrrm " + " JOIN rrrm.receipt fromReceipt "
      + " JOIN fromReceipt.receiptTaxes fromReceiptTaxes "
      + " WHERE rrrm.refundReceipt = :refundReceiptId ";

  /**
   * Gets the refund receipt tax details.
   *
   * @param refundReceiptId
   *          the refund receipt id
   * @return the refund receipt tax details
   */
  public List<Map<String, Object>> getRefundReceiptTaxDetails(String refundReceiptId) {
    Session session = getSession();
    Query query = session.createQuery(GET_REFUND_RECEIPT_TAX_DETAILS);
    query.setString("refundReceiptId", refundReceiptId);
    List<Map<String, Object>> result = (List<Map<String, Object>>) query.list();
    return result;
  }
  
  /** The Constant GET_REFUND_RECEIPT_ALLOCATION_DETAILS. */
  private static final String GET_REFUND_RECEIPT_ALLOCATION_DETAILS = " SELECT new map(rrrm as rrm, "
      + " fromReceipt as fromReceipt) FROM "
      + " ReceiptRefundReferenceModel rrrm " + " JOIN rrrm.receipt fromReceipt "
      + " WHERE rrrm.refundReceipt = :refundReceiptId ";

  /**
   * Gets the refund receipt allocation details.
   *
   * @param refundReceiptId the refund receipt id
   * @return the refund receipt allocation details
   */
  public List<Map<String,Object>> getRefundReceiptAllocationDetails(String refundReceiptId) {
    Session session = getSession();
    Query query = session.createQuery(GET_REFUND_RECEIPT_ALLOCATION_DETAILS);
    query.setString("refundReceiptId", refundReceiptId);
    List<Map<String, Object>> result = (List<Map<String, Object>>) query.list();
    return result;
  }

  /** The Constant GET_LAST_JOB_TRANSACTION_ID_FOR_DEPOSIT_RECEIPT. */
  private static final String GET_LAST_JOB_TRANSACTION_ID_FOR_DEPOSIT_RECEIPT = " SELECT "
      + " MAX(jobTransaction) AS max_job_transaction FROM HmsAccountingInfoModel haim "
      + " WHERE haim.voucherNo=? AND haim.voucherType IN (select voucherDefinition from FaVoucherDefinitionsModel fvd where fvd.voucherKey in ('VOUCHER_TYPE_RECEIPT','VOUCHER_TYPE_PAYMENT')) "
      + " AND jobTransaction is not null AND jobTransaction > 0 "
      + " AND haim.billNo is null GROUP BY haim.jobTransaction ORDER BY jobTransaction DESC ";

  /**
   * Gets the last job transaction id for reversal posts deposits.
   *
   * @param receiptId
   *          the receipt id
   * @return the last job transaction id for reversal posts deposits
   */
  public Integer getLastJobTransactionIdForReversalPostsDeposits(String receiptId) {
    List<Integer> resultList = (List<Integer>) executeHqlQuery(
        GET_LAST_JOB_TRANSACTION_ID_FOR_DEPOSIT_RECEIPT, new Object[] { receiptId }, 1);
    if (resultList != null && !resultList.isEmpty()) {
      return resultList.get(0);
    }
    return null;
  }

}
