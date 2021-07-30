package com.insta.hms.common;

import com.bob.hms.common.DataBaseUtil;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * The Class CommonReportDAO.
 */
public class CommonReportDAO extends GenericDAO {

  /**
   * Instantiates a new common report DAO.
   */
  public CommonReportDAO() {
    super("item_groups");
  }

  /** The Constant GET_ITEM_GROUP_CODES_NAME. */
  private static final String GET_ITEM_GROUP_CODES_NAME =
      "select DISTINCT item_group_name, item_group_id FROM item_groups ";

  /**
   * Gets the item group codes name.
   *
   * @return the item group codes name
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getItemGroupCodesName() throws SQLException {
    PreparedStatement ps = null;
    Connection con = DataBaseUtil.getReadOnlyConnection();
    try {
      ps = con.prepareStatement(GET_ITEM_GROUP_CODES_NAME);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

}
