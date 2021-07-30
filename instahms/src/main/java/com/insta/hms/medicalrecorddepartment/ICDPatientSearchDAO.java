package com.insta.hms.medicalrecorddepartment;

import com.bob.hms.common.DataBaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * The Class ICDPatientSearchDAO.
 */
public class ICDPatientSearchDAO {
  protected static final String PATIENT_ID = "patient_id";

  private ICDPatientSearchDAO() {

  }

  /** The Constant UPDATE_CODIFIED_BY. */
  public static final String UPDATE_CODIFIED_BY = "UPDATE patient_registration"
      + " SET codified_by = ?";

  /**
   * Update codified by.
   *
   * @param patList
   *          the pat list
   * @param codifiedBy
   *          the codified by
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public static boolean updateCodifiedBy(List patList, String codifiedBy) throws SQLException {
    Connection con = DataBaseUtil.getReadOnlyConnection();
    con.setAutoCommit(false);
    boolean success = false;
    StringBuilder where = new StringBuilder();
    DataBaseUtil.addWhereFieldInList(where, PATIENT_ID, patList);

    StringBuilder query = new StringBuilder(UPDATE_CODIFIED_BY);
    query.append(where);
    try (PreparedStatement ps = con.prepareStatement(query.toString())) {
      ps.setString(1, codifiedBy);

      int index = 2;
      for (int i = 0; i < patList.size(); i++, index++) {
        ps.setString(index, (String) patList.get(i));
      }
      success = ps.executeUpdate() > 0;

    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    return success;
  }

  /** The Constant VERIFIED_AND_COMPLETED. */
  public static final String VERIFIED_AND_COMPLETED = "UPDATE patient_registration"
      + " SET codification_status = ?";

  /**
   * Verified and completed.
   *
   * @param patientList
   *          the patient list
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public static boolean verifiedAndCompleted(List<String> patientList) throws SQLException {
    Connection con = DataBaseUtil.getReadOnlyConnection();
    con.setAutoCommit(false);
    boolean success = false;

    StringBuilder where = new StringBuilder();
    DataBaseUtil.addWhereFieldInList(where, PATIENT_ID, patientList);

    StringBuilder query = new StringBuilder(VERIFIED_AND_COMPLETED);
    query.append(where);
    try (PreparedStatement ps = con.prepareStatement(query.toString())) {
      ps.setString(1, "V");

      int index = 2;
      for (int indexPatList = 0; indexPatList < patientList.size(); indexPatList++, index++) {
        ps.setString(index, (String) patientList.get(indexPatList));
      }
      success = ps.executeUpdate() > 0;

    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    return success;
  }

  /** The Constant REOPEN_CODIFICATION. */
  public static final String REOPEN_CODIFICATION = "UPDATE patient_registration "
      + " SET codification_status = ?";

  /**
   * Reopen for codification.
   *
   * @param patientList
   *          the patient list
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public static boolean reopenForCodification(List<String> patientList) throws SQLException {
    Connection con = DataBaseUtil.getReadOnlyConnection();
    con.setAutoCommit(false);
    boolean success = false;
    StringBuilder where = new StringBuilder();
    DataBaseUtil.addWhereFieldInList(where, PATIENT_ID, patientList);

    StringBuilder query = new StringBuilder(REOPEN_CODIFICATION);
    query.append(where);
    try (PreparedStatement ps = con.prepareStatement(query.toString())) {
      ps.setString(1, "P");

      int index = 2;
      for (int indexPatList = 0; indexPatList < patientList.size(); indexPatList++, index++) {
        ps.setString(index, (String) patientList.get(indexPatList));
      }
      success = ps.executeUpdate() > 0;

    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    return success;
  }
}
