package com.insta.hms.billing.accounting;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.stores.StoresTallyDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * The Class AccountingStoreInvoicesExporter.
 */
public class AccountingStoreInvoicesExporter extends GenericAccountingExporter {

  /** The Constant THIS_VOUCHER_TYPE. */
  private static final String THIS_VOUCHER_TYPE = "STORES_INVOICES";

  /**
   * Instantiates a new accounting store invoices exporter.
   *
   * @param format
   *          the format
   */
  public AccountingStoreInvoicesExporter(String format) {
    super(THIS_VOUCHER_TYPE, "voucher_number", "grn_date", "purchases", "purchase_vtype",
        "PURCHASE", format);
  }

  /**
   * @see com.insta.hms.billing.accounting.GenericAccountingExporter#createVoucher(java.util.Map)
   */
  @Override
  protected Voucher createVoucher(Map data) {
    return createVoucher((java.sql.Date) data.get(getVoucherDateField()),
        data.get(getVoucherNumberField()).toString(), getAccountingVoucherType(data));
  }

  /**
   * @see com.insta.hms.billing.accounting.GenericAccountingExporter#getTransactions(int,
   *      java.util.List)
   */
  @Override
  protected List<BasicDynaBean> getTransactions(int centerId, List voucherList)
      throws SQLException {
    Connection con = DataBaseUtil.getConnection(60);
    List<BasicDynaBean> invoices = null;
    try {
      invoices = StoresTallyDAO.getInvoiceAmounts(con, fromDate, toDate, accountGroup, centerId,
          voucherList);
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
    return invoices;
  }
}
