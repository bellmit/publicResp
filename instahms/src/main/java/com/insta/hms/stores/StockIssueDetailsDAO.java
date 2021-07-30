package com.insta.hms.stores;

import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


/**
 * The Class StockIssueDetailsDAO.
 */
public class StockIssueDetailsDAO extends GenericDAO {

  /**
   * Instantiates a new stock issue details DAO.
   */
  public StockIssueDetailsDAO() {
    super("stock_issue_details");
  }

  /**
   * Gets the issue detail bean.
   * Useful to get details bean passing minimum params like userissueno(unique id of
   * issue),medicine and batch number.
   *
   * @param con the con
   * @param userIssueNo the user issue no
   * @param medicineId the medicine id
   * @param batchNo the batch no
   * @return the issue detail bean
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public BasicDynaBean getIssueDetailBean(Connection con, int userIssueNo, int medicineId,
      String batchNo) throws SQLException, IOException {

    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("user_issue_no", userIssueNo);
    keys.put("medicine_id", medicineId);
    keys.put("batch_no", batchNo);

    return findByKey(con, keys);

  }

}
