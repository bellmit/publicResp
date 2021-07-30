package com.insta.hms.billing.accounting;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.billing.ReceiptRelatedDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * The Class AccountingReceiptsExporter.
 */
public class AccountingReceiptsExporter extends GenericAccountingExporter {

  /** The Constant THIS_VOUCHER_TYPE. */
  private static final String THIS_VOUCHER_TYPE = "RECEIPTS";

  /**
   * Instantiates a new accounting receipts exporter.
   *
   * @param format
   *          the format
   */
  public AccountingReceiptsExporter(String format) {
    super(THIS_VOUCHER_TYPE, "receipt_no", "voucher_date", "receipts", "receipt_vtype", "RECEIPT",
        format);
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
      records = ReceiptRelatedDAO.getAllReceipts(con, fromDate, toDate, accountGroup, centerId,
          voucherList);
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
    return records;
  }

  /**
   * @see com.insta.hms.billing.accounting
   * .GenericAccountingExporter#getAccountingVoucherType(java.util.Map)
   */
  protected String getAccountingVoucherType(Map data) {
    String paymentType = (String) data.get("payment_type");
    String voucherType = "refund_vtype";
    if (paymentType.equals("R") || paymentType.equals("S")) {
      voucherType = "receipt_vtype";
    }
    return voucherTypes.get(voucherType).toString();
  }

}
