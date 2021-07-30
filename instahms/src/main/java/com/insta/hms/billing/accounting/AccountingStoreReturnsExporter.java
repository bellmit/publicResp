package com.insta.hms.billing.accounting;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.stores.StoresSupplierReturnsDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * The Class AccountingStoreReturnsExporter.
 */
public class AccountingStoreReturnsExporter extends GenericAccountingExporter {

  /** The Constant THIS_VOUCHER_TYPE. */
  private static final String THIS_VOUCHER_TYPE = "STORES_RETURNS_WITH_DEBIT";

  /**
   * Instantiates a new accounting store returns exporter.
   *
   * @param format
   *          the format
   */
  public AccountingStoreReturnsExporter(String format) {
    super(THIS_VOUCHER_TYPE, "debit_note_no", "debit_note_date", "returns",
        "purchase_return_vtype", "SRWDEBITNOTE", format);
  }

  /**
   * @see com.insta.hms.billing.accounting.GenericAccountingExporter#createVoucher(java.util.Map)
   */
  @Override
  protected Voucher createVoucher(Map data) {
    return createVoucher((java.sql.Date) data.get(getVoucherDateField()), data.get("supplier_id")
        + "-" + data.get(getVoucherNumberField()), getAccountingVoucherType(data));
  }

  /**
   * @see com.insta.hms.billing.accounting.GenericAccountingExporter#getTransactions(int,
   *      java.util.List)
   */
  @Override
  protected List<BasicDynaBean> getTransactions(int centerId, 
      List voucherList) throws SQLException {
    Connection con = DataBaseUtil.getConnection(60);
    List<BasicDynaBean> invoices = null;
    try {
      invoices = StoresSupplierReturnsDAO.getReturnsWithDebit(con, fromDate, toDate, accountGroup,
          centerId, voucherList);
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
    return invoices;
  }
}
