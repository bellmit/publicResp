package com.insta.hms.adminmaster.packagemaster;

import com.bob.hms.common.DataBaseUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class PackageBO.
 */
public class PackageBO {

  /**
   * Gets the package names and beds.
   *
   * @param orgid the orgid
   * @return the package names and beds
   * @throws SQLException the SQL exception
   */
  public List getPackageNamesAndBeds(String orgid) throws SQLException {
    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      return new PackageDAO(con).getPackageNamesAndBeds(orgid);
    } finally {
      if (con != null) {
        con.close();
      }
    }
  }

}
