package com.insta.instaapi.patient.mobileaccess;

import com.insta.hms.common.RandomGeneration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * PatientMobileAccessDao.
 */
public class PatientMobileAccessDAO {

  public static final String CREATE_PASSWORD = "update patient_details set mobile_access=?,"
      + "mobile_password=? where mr_no=?";

  /**
   * updates the password.
   * @param con connection paramter
   * @param mrno mrno parameter
   * @return returns updated password
   * @throws SQLException throws sql exception
   */
  public static int updatePassword(Connection con,String mrno) throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(CREATE_PASSWORD);
      ps.setBoolean(1, true);
      ps.setString(2,RandomGeneration.randomGeneratedPassword(6));
      ps.setString(3, mrno);
      return ps.executeUpdate();
    } finally {
      com.insta.instaapi.common.DbUtil.closeConnections(null, ps);
    }
  }

}
