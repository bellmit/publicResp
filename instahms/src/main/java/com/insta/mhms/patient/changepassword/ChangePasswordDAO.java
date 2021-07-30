package com.insta.mhms.patient.changepassword;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author mohammed.r
 */
public class ChangePasswordDAO extends GenericDAO {

  public ChangePasswordDAO() {
    // TODO Auto-generated constructor stub
    super("patient_details");
  }

  private static final String GET_PATIENT_OLD_PASSWORD =
      " SELECT mobile_password FROM patient_details WHERE mr_no= ? ";

  /**
   * Get patient password details.
   *
   * @param con connection object
   * @param mrNo mrno of the patient
   * @return returns String
   * @throws SQLException may throw Sql Exception
   */
  public static String getPatientPasswordDetails(Connection con, String mrNo) throws SQLException {
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_PATIENT_OLD_PASSWORD);
      ps.setString(1, mrNo);
      return DataBaseUtil.getStringValueFromDb(ps);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  private static final String GET_PASSWORD_UPDATE =
      " Update patient_details set mobile_password = ? where mr_no = ? ";

  /**
   * update patient password.
   *
   * @param con connection object
   * @param mrNo patient mr number
   * @param mobilePassword mobile password
   * @return returns boolean
   * @throws SQLException may throw Sql Exception
   */
  public static boolean updatePatientPassword(Connection con, String mrNo, String mobilePassword)
      throws SQLException {
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_PASSWORD_UPDATE);
      ps.setString(1, mobilePassword);
      ps.setString(2, mrNo);
      return ps.executeUpdate() > 0;
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }
}
