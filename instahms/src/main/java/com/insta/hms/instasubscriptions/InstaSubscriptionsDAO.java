package com.insta.hms.instasubscriptions;

import com.bob.hms.common.DataBaseUtil;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * The Class InstaSubscriptionsDAO.
 */
public class InstaSubscriptionsDAO {

  /** The integration name. */
  private static final String INTEGRATION_NAME = "chargebee";

  /** The get chargebee details. */
  private static final String GET_CHARGEBEE_DETAILS = "SELECT ii.chargebee_customer_id, ii.aeskey"
      + " FROM insta_integration ii" + " WHERE ii.status=? and ii.integration_name=?";

  /**
   * Gets the chargebee details.
   *
   * @return the chargebee details
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getChargebeeDetails() throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      StringBuilder query = new StringBuilder(GET_CHARGEBEE_DETAILS);
      ps = con.prepareStatement(query.toString());
      ps.setString(1, "A");
      ps.setString(2, INTEGRATION_NAME);
      BasicDynaBean bean = DataBaseUtil.queryToDynaBean(ps);
      return bean;
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }
}
