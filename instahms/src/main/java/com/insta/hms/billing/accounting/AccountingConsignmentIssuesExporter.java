package com.insta.hms.billing.accounting;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.stores.StoresTallyDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * The Class AccountingConsignmentIssuesExporter.
 */
public class AccountingConsignmentIssuesExporter extends GenericAccountingExporter {

  /** The Constant EXPORT_VOUCHER_TYPE. */
  private static final String EXPORT_VOUCHER_TYPE = "CONSINGMENT_STOCK_ISSUED";

  /**
   * Instantiates a new accounting consignment issues exporter.
   *
   * @param format
   *          the format
   */
  public AccountingConsignmentIssuesExporter(String format) {
    super(EXPORT_VOUCHER_TYPE, "consignment_invoice_no", "con_invoice_date", "purchases",
        "purchase_vtype", "CSISSUED", format);
  }

  /**
   * @see com.insta.hms.billing.accounting.GenericAccountingExporter#createVoucher(java.util.Map)
   */
  @Override
  protected Voucher createVoucher(Map data) {
    return createVoucher((java.sql.Date) data.get(getVoucherDateField()),
        "SCI" + data.get(getVoucherNumberField()), getAccountingVoucherType(data));
  }

  /**
   * @see com.insta.hms.billing.accounting.GenericAccountingExporter#getTransactions(int,
   *      java.util.List)
   */
  @Override
  protected List<BasicDynaBean> getTransactions(int centerId, List voucherList) 
      throws SQLException {
    Connection con = DataBaseUtil.getConnection(60);
    List<BasicDynaBean> records = null;
    try {
      records = StoresTallyDAO.getConsignmentStockIssued(con, fromDate, toDate, accountGroup,
          centerId, voucherList);
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
    return records;
  }

}
