package com.insta.hms.advancedpackages;

import com.bob.hms.common.DataBaseUtil;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class PackageTestsDAO.
 */
public class PackageTestsDAO {

  /** The Constant PACKAGE_TEST_REPORTS_LIST. */
  public static final String PACKAGE_TEST_REPORTS_LIST =
      "SELECT distinct tvr.report_id,report_addendum  "
      + " FROM package_prescribed pp "
      + " JOIN tests_prescribed tp ON(tp.package_ref = pp.prescription_id) "
      + " JOIN test_visit_reports tvr USING(report_id) "
      + " WHERE tvr.report_id not in (SELECT report_id FROM tests_prescribed "
      + " WHERE conducted in ('RAS','RBS')"
      + " AND report_id is not null ) " + " AND package_ref=?  ";

  /**
   * Gets the signed off package test reports.
   *
   * @param packageRef the package ref
   * @return the signed off package test reports
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static List<BasicDynaBean> getSignedOffPackageTestReports(int packageRef)
      throws SQLException, IOException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(PACKAGE_TEST_REPORTS_LIST + " AND signed_off = 'Y' ");
      ps.setInt(1, packageRef);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

}
