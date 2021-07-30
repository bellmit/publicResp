package com.insta.hms.billing.accounting;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.billing.ChargeDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * The Class AccountingPaymentsExporter.
 */
public class AccountingPaymentsExporter extends GenericAccountingExporter {

  /** The export voucher type. */
  private static String EXPORT_VOUCHER_TYPE = "PAYMENT_VOUCHERS";

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(AccountingPaymentsExporter.class);

  /**
   * Instantiates a new accounting payments exporter.
   *
   * @param format
   *          the format
   */
  public AccountingPaymentsExporter(String format) {
    super(EXPORT_VOUCHER_TYPE, "voucher_no", "voucher_date", "payments", "payment_voucher_vtype",
        null, format);
  }

  /**
   * @see com.insta.hms.billing.accounting
   * .GenericAccountingExporter#getTransactionType(java.util.Map)
   */
  protected String getTransactionType(Map voucher) {
    String category = (String) voucher.get("voucher_category");
    if ("P".equalsIgnoreCase(category)) {
      return "PP"; // Paid Payments
    } else {
      return "PR"; // Payment Refunds
    }
  }

  /**
   * @see com.insta.hms.billing.accounting.GenericAccountingExporter#getTransactions(int,
   *      java.util.List)
   */
  protected List<BasicDynaBean> getTransactions(int centerId, List voucherList) 
      throws SQLException {
    Connection con = DataBaseUtil.getConnection(60);
    List<BasicDynaBean> records = null;
    try {
      records = ChargeDAO.getAllPaymentVouchers(con, fromDate, toDate, accountGroup, centerId,
          voucherList);
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
    return records;
  }

}
