package com.insta.hms.ipservices;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

// TODO: Auto-generated Javadoc
/**
 * The Class TestsDAO.
 */
public class TestsDAO {
  
  /** The Constant TEST_CANCEL_QUERY. */
  public static final String TEST_CANCEL_QUERY = "UPDATE "
      + " Tests_Prescribed "
      + " SET Conducted='Cancel' "
      + " WHERE prescribed_id=?";

  /**
   * Cancel tests prescribed.
   *
   * @param con the con
   * @param prescribedId the prescribed id
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean cancelTestsPrescribed(Connection con, int prescribedId) throws SQLException {
    boolean teststatus = true;
    try (PreparedStatement ps = con.prepareStatement(TEST_CANCEL_QUERY)) {
      ps.setInt(1, prescribedId);
      if (ps.executeUpdate() <= 0) {
        teststatus = false;
      }
    }
    return teststatus;
  }

  /** The Constant UPDATE_TEST_PRESCRIPTION. */
  private static final String UPDATE_TEST_PRESCRIPTION = "UPDATE "
      + " tests_prescribed "
      + " SET remarks = ?, pres_doctor = ? "
      + " WHERE prescribed_id = ?";

  /**
   * Update test prescription.
   *
   * @param con the con
   * @param prescriptionId the prescription id
   * @param remarks the remarks
   * @param docId the doc id
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public static boolean updateTestPrescription(Connection con, int prescriptionId, String remarks,
      String docId) throws SQLException {
    boolean updateStatus = true;
    try (PreparedStatement ps = con.prepareStatement(UPDATE_TEST_PRESCRIPTION);) {
      ps.setString(1, remarks);
      ps.setString(2, docId);
      ps.setInt(3, prescriptionId);
      updateStatus = ps.executeUpdate() == 1;
    }
    return updateStatus;
  }
}
