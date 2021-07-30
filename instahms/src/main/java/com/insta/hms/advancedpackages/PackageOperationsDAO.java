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
 * The Class PackageOperationsDAO.
 */
public class PackageOperationsDAO {

  /** The package conduct operations. */
  private static String PACKAGE_CONDUCT_OPERATIONS =
      " SELECT doc_id, doc_name, doc_date, doc_format, od.username, bos.patient_id, "
      + "(SELECT doc.doctor_name FROM bed_operation_schedule bps "
      + "left join doctors doc on doc.doctor_id = bps.consultant_doctor"
      + " WHERE bps.prescribed_id=od.prescription_id AND bps.consultant_doctor IS NOT NULL "
      + " AND bps.consultant_doctor!= '' LIMIT 1) AS pres_doctor"
      + " FROM operation_documents od JOIN patient_documents pd using (doc_id) "
      + " JOIN bed_operation_schedule bos on (bos.prescribed_id=od.prescription_id) "
      + "WHERE package_ref = ?";

  /**
   * Gets the signed off package OT reports.
   *
   * @param packageRef the package ref
   * @return the signed off package OT reports
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getSignedOffPackageOTReports(int packageRef)
      throws IOException, SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(PACKAGE_CONDUCT_OPERATIONS);
      ps.setInt(1, packageRef);

      return DataBaseUtil.queryToDynaList(ps);

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }
}
