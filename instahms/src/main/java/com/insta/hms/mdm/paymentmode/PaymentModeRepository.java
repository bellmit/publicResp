package com.insta.hms.mdm.paymentmode;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;
import com.insta.hms.mdm.SearchQuery;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * The Class PaymentModeRepository.
 */
@Repository
public class PaymentModeRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new payment mode repository.
   */
  public PaymentModeRepository() {
    super("payment_mode_master", "mode_id", "payment_mode");
  }

  /** The payment mode tables. */
  private static final String PAYMENT_MODE_TABLES = " from payment_mode_master";

  @Override
  public SearchQuery getSearchQuery() {
    return new SearchQuery(PAYMENT_MODE_TABLES);
  }

  /** The Constant PAYMENT_MODE_DETAILS. */
  private static final String PAYMENT_MODE_DETAILS = "Select mode_id, "
      + " payment_mode from payment_mode_master";

  /**
   * Gets the payment mode names and ids.
   *
   * @return the payment mode names and ids
   */
  public List<BasicDynaBean> getPaymentModeNamesAndIds() {
    return DatabaseHelper.queryToDynaList(PAYMENT_MODE_DETAILS);
  }

  /** The Constant PAYMENT_MODE_TABLE_DETAILS. */
  private static final String PAYMENT_MODE_TABLE_DETAILS = " SELECT mode_id, payment_mode, "
      + " payment_mode AS mode_name from payment_mode_master "
      + " UNION "
      + " SELECT -2 AS mode_id, '_total' as payment_mode, 'Total' AS mode_name "
      + " from payment_mode_master ORDER BY mode_id desc";

  /**
   * Gets the payment mode fields with total.
   *
   * @return the payment mode fields with total
   * @throws SQLException the SQL exception
   */
  public List getPaymentModeFieldsWithTotal() throws SQLException {
    return DataBaseUtil.queryToDynaList(PAYMENT_MODE_TABLE_DETAILS);
  }

  /** The Constant SPECIFIC_PAYMENT_MODE_DEATILS. */
  private static final String SPECIFIC_PAYMENT_MODE_DEATILS = "SELECT * from payment_mode_master "
      + "where payment_mode ilike 'cash' or payment_mode ilike 'neft' "
      + " or payment_mode ilike 'cheque' ORDER BY displayorder ASC";

  /**
   * Gets the specific payment mode details.
   *
   * @return the specific payment mode details
   * @throws SQLException the SQL exception
   */
  public List getSpecificPaymentModeDetails() throws SQLException {
    return DataBaseUtil.queryToDynaList(SPECIFIC_PAYMENT_MODE_DEATILS);
  }

}
