package com.insta.hms.billing.accounting;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.billing.DepositsDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * The Class AccountingDepositsExporter.
 */
public class AccountingDepositsExporter extends GenericAccountingExporter {

  /** The Constant THIS_VOUCHER_TYPE. */
  private static final String THIS_VOUCHER_TYPE = "DEPOSITS";

  /**
   * Instantiates a new accounting deposits exporter.
   *
   * @param format the format
   */
  public AccountingDepositsExporter(String format) {
    super(THIS_VOUCHER_TYPE, "receipt_no", "display_date", "receipts", "receipt_vtype", "DEPOSIT",
        format);
  }

  /**
   * @see com.insta.hms.billing.accounting
   * .GenericAccountingExporter#getTransactions(int, java.util.List)
   */
  @Override
  protected List<BasicDynaBean> getTransactions(int centerId, List voucherList) 
      throws SQLException {
    Connection con = DataBaseUtil.getConnection(60);
    List<BasicDynaBean> records = null;
    try {
      records = DepositsDAO.getAllDeposits(con, fromDate, toDate, accountGroup, centerId,
          voucherList);
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
    return records;
  }

}
