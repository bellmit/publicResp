package com.insta.hms.billing.accounting;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.stores.StoresTallyDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * The Class AccountingConsignmentReturnsExporter.
 */
public class AccountingConsignmentReturnsExporter extends GenericAccountingExporter {

  /** The Constant THIS_VOUCHER_TYPE. */
  private static final String THIS_VOUCHER_TYPE = "CONSINGMENT_STOCK_RETURNED";

  /**
   * Instantiates a new accounting consignment returns exporter.
   *
   * @param format
   *          the format
   */
  public AccountingConsignmentReturnsExporter(String format) {
    super(THIS_VOUCHER_TYPE, "consignment_invoice_no", "con_return_date", "purchases",
        "purchase_return_vtype", "CSRETURNED", format);
  }

  /**
   * @see com.insta.hms.billing.accounting.GenericAccountingExporter#createVoucher(java.util.Map)
   */
  @Override
  protected Voucher createVoucher(Map data) {
    return createVoucher((java.sql.Date) data.get(this.getVoucherDateField()),
        "SCR" + data.get(this.getVoucherNumberField()).toString(), getAccountingVoucherType(data));
  }

  /**
   * @see com.insta.hms.billing.accounting .GenericAccountingExporter#getTransactions(int,
   *      java.util.List)
   */
  @Override
  protected List<BasicDynaBean> getTransactions(int centerId, List voucherList) 
      throws SQLException {
    Connection con = DataBaseUtil.getConnection(60);
    List<BasicDynaBean> records = null;
    try {
      records = StoresTallyDAO.getConsignmentStockReturnedAmounts(con, fromDate, toDate,
          accountGroup, centerId, voucherList);
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
    return records;
  }

}
