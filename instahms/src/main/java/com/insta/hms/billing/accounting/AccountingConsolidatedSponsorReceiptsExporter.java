package com.insta.hms.billing.accounting;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.billing.ClaimReceiptsDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * The Class AccountingConsolidatedSponsorReceiptsExporter.
 *
 * @author krishna
 */
public class AccountingConsolidatedSponsorReceiptsExporter extends GenericAccountingExporter {

  /** The Constant THIS_VOUCHER_TYPE. */
  private static final String THIS_VOUCHER_TYPE = "CONSOLIDATED_SPONSOR_RECEIPTS";

  /**
   * Instantiates a new accounting consolidated sponsor receipts exporter.
   *
   * @param format
   *          the format
   */
  public AccountingConsolidatedSponsorReceiptsExporter(String format) {
    super(THIS_VOUCHER_TYPE, "receipt_no", "display_date", "receipts", "receipt_vtype",
        "CSRECEIPT", format);
  }

  /**
   * @see com.insta.hms.billing.accounting.GenericAccountingExporter#createVoucher(java.util.Map)
   */
  @Override
  protected Voucher createVoucher(Map data) {
    return createVoucher(
        new Date(((java.sql.Timestamp) data.get(getVoucherDateField())).getTime()),
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
    List<BasicDynaBean> records = null;
    try {
      records = ClaimReceiptsDAO.getAllClaimReceipts(con, fromDate, toDate, accountGroup, centerId,
          voucherList);
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
    return records;
  }

}
