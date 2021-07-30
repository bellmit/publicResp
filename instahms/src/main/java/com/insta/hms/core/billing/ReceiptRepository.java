package com.insta.hms.core.billing;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public class ReceiptRepository extends GenericRepository{

	public ReceiptRepository() {
		super("receipts");	
	}

	/*
	 * Receipt number generation methods: this is based on a set of preferences
	 * on how the type controls the prefix and sequence to use.
	 */
	private static final String RECEIPT_SEQ_PREFS = " SELECT pattern_id FROM hosp_receipt_seq_prefs "
			+ "  WHERE (bill_type = ? or bill_type ='*') AND "
			+ "  (visit_type = ? or visit_type = '*') AND "
			+ "  (restriction_type = ? or restriction_type = '*') AND "
			+ "  (payment_type = ?) AND "
			+ "  (center_id = ? OR center_id = 0) order by center_id desc, priority limit 1;" ;
	
	public String getNextReceiptNo(String billType, String visitType,
			String restrictionType, String paymentType, int centerId) {	
		return DatabaseHelper.getNextPatternId(DatabaseHelper.getString(RECEIPT_SEQ_PREFS,
				new Object[]{billType,visitType,restrictionType,paymentType,centerId}));

	}

	/*
	 * Get the Cash Payments at MRNO Level on Day Wise and Visit Level
	 */
	private static final String GET_CASH_PAYMENTS = " SELECT COALESCE(SUM(day_cash_payment),0) AS day_cash_payment, "
			+ " COALESCE(SUM(visit_cash_amount),0) AS visit_cash_amount,COALESCE(SUM(day_refund_cash_amt),0) AS day_refund_cash_amt, "
			+ " COALESCE(SUM(visit_refund_cash_amt),0) AS visit_refund_cash_amt,"
			+ " COALESCE(transaction_limit-SUM(visit_cash_amount),0) AS avbl_cash_limit,"
			+ " transaction_limit " + " FROM( "
			+ " SELECT COALESCE(SUM(r.amount),0) AS day_cash_payment,"
			+ " COALESCE(SUM(CASE WHEN r.receipt_type='F' THEN -(r.amount)  ELSE 0 END),0) AS day_refund_cash_amt,"
			+ " 0 AS visit_cash_amount,0 as visit_refund_cash_amt,pm.transaction_limit " + " FROM receipts r "
			+ " JOIN payment_mode_master pm ON pm.mode_id=r.payment_mode_id "
			+ " WHERE r.mr_no=? AND COALESCE(created_at::date, display_date::date)=now()::date "
			+ " AND  r.payment_mode_id = -1 GROUP BY pm.transaction_limit" + " UNION ALL "
			+ " SELECT 0 AS day_cash_payment," + " 0 AS day_refund_cash_amt,"
			+ " COALESCE(SUM(CASE WHEN r.is_deposit=true THEN 0 ELSE r.amount END),0) + COALESCE(SUM(rc.allocated_amount),0) AS visit_cash_amount, "
			+ " COALESCE(SUM(CASE WHEN r.receipt_type='F' THEN -(r.amount)  ELSE 0 END),0) AS visit_refund_cash_amt,pm.transaction_limit"
			+ " FROM receipts r "
			+ " LEFT JOIN bill_receipts br ON br.receipt_no=r.receipt_id "
			+ " LEFT JOIN LATERAL (SELECT receipt_no , SUM(allocated_amount) AS allocated_amount "
			+ " FROM bill_receipts WHERE  r.receipt_id = receipt_no AND br.bill_no=bill_no AND r.is_deposit = true "
			+ " GROUP BY receipt_no) rc ON r.receipt_id = rc.receipt_no "
			+ " LEFT JOIN bill b ON b.bill_no=br.bill_no "
			+ " JOIN payment_mode_master pm ON pm.mode_id=r.payment_mode_id "
			+ " WHERE b.visit_id=? AND r.payment_mode_id=-1 GROUP BY pm.transaction_limit)as foo GROUP BY transaction_limit ";

	/**
	 * Gets the cash payments.
	 *
	 * @param mrNo,visitId
	 * @return the details
	 */

	public BasicDynaBean getCashPayments(String mrNo, String visitId) {
		return DatabaseHelper.queryToDynaBean(GET_CASH_PAYMENTS, new Object[] { mrNo, visitId });
	}

	/*
	 * Get the Deposit Cash Payments and Available Cash Deposit at MRNO Level
	 */
	private static final String GET_DEPOSIT_CASH_PAYMENTS = "SELECT COALESCE(SUM(day_cash_collection),0) AS day_cash_collection, "
			+ " COALESCE(SUM(day_refund_cash),0) AS day_refund_cash,COALESCE(SUM(unallocated_amount),0) AS unallocated_amount, "
			+ " transaction_limit FROM " + " (SELECT COALESCE(SUM(r.amount),0) AS day_cash_collection, "
			+ " COALESCE(SUM(CASE WHEN r.receipt_type='F' THEN -(r.amount)  ELSE 0 END),0) AS day_refund_cash, "
			+ " 0 AS unallocated_amount, " + " pm.transaction_limit " + " FROM receipts r "
			+ " JOIN payment_mode_master pm ON pm.mode_id=r.payment_mode_id "
			+ " WHERE COALESCE(r.mr_no,'')=? AND created_at::date=now()::date "
			+ " AND  r.payment_mode_id = -1 GROUP BY pm.transaction_limit " + " UNION ALL "
			+ " SELECT 0 AS day_cash_collection,0 AS day_refund_cash, "
			+ " COALESCE(SUM(r.unallocated_amount),0) AS unallocated_amount, " + " pm.transaction_limit "
			+ " FROM receipts r " + " JOIN payment_mode_master pm ON pm.mode_id=r.payment_mode_id "
			+ " WHERE COALESCE(r.mr_no,'')=? AND  r.payment_mode_id = -1 "
			+ " AND is_deposit=true AND receipt_type!='F' GROUP BY pm.transaction_limit)As foo GROUP BY transaction_limit ";

	/**
	 * Gets the Deposit cash payments.
	 *
	 * @param mrNo
	 * @return the details
	 */

	public BasicDynaBean getDepositCashPayments(String mrNo) {
		return DatabaseHelper.queryToDynaBean(GET_DEPOSIT_CASH_PAYMENTS, new Object[] { mrNo, mrNo });
	}

  private static final String RECEIPTS_NOT_IN_HMS_ACCOUNTING_INFO = "SELECT r.receipt_id"
      + " FROM receipts r"
      + " LEFT JOIN (SELECT voucher_no as receipt_id, max(mod_time) as modified_at"
      + "   FROM hms_accounting_info"
      + "   WHERE mod_time BETWEEN ? AND ? AND voucher_type in (select voucher_definition from fa_voucher_definitions where voucher_key in ('VOUCHER_TYPE_RECEIPT','VOUCHER_TYPE_PAYMENT'))"
      + "   GROUP BY voucher_no) ac ON ac.receipt_id = r.receipt_id"
      + " WHERE r.modified_at BETWEEN ? AND ?"
      + "   AND (ac.modified_at IS NULL OR ac.modified_at != r.modified_at)";

  public List<BasicDynaBean> getReceiptsNotInHmsAccountingInfo(
      int relFromHour, int relToHour) {
    Timestamp now = DateUtil.getCurrentTimestamp();

    Timestamp startTime = DateUtil.addHours(now, relFromHour * -1);
    Timestamp endTime = DateUtil.addHours(now, relToHour * -1);
    
    return DatabaseHelper.queryToDynaList(RECEIPTS_NOT_IN_HMS_ACCOUNTING_INFO, 
        new Object[] {startTime, endTime, startTime, endTime});
  }
}

