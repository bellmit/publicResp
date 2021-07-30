package com.insta.hms.billing.accounting;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.billing.ChargeDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * The Class AccountingPaymentDuesExporter.
 */
public class AccountingPaymentDuesExporter extends GenericAccountingExporter {

  /** The Constant THIS_VOUCHER_TYPE. */
  private static final String THIS_VOUCHER_TYPE = "PAYMENT_DUES";

  /**
   * Instantiates a new accounting payment dues exporter.
   *
   * @param format
   *          the format
   */
  public AccountingPaymentDuesExporter(String format) {
    super(THIS_VOUCHER_TYPE, "voucher_no", "voucher_date", "receipts", "payment_vtype",
        "PAYMENTDUES", format);
  }

  /**
   * @see com.insta.hms.billing.accounting.GenericAccountingExporter#getTransactions(int,
   *      java.util.List)
   */
  @Override
  protected List<BasicDynaBean> getTransactions(int centerId, 
      List voucherList) throws SQLException {
    Connection con = DataBaseUtil.getConnection(60);
    List<BasicDynaBean> records = null;
    try {
      records = ChargeDAO.getAllPaymentsDue(con, fromDate, toDate, accountGroup, centerId,
          voucherList);
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
    return records;
  }

}
