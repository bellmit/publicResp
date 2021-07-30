package com.insta.hms.usermanager;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * The Class UserSignatureDAO.
 *
 * @author krishna
 */
public class UserSignatureDAO extends GenericDAO {

  /**
   * Instantiates a new user signature DAO.
   */
  public UserSignatureDAO() {
    super("user_images");
  }

  private static final String DOCTOR_SIGNATURE = " SELECT signature  FROM user_images "
      + "WHERE doctor_id=?";

  /**
   * Get Doctor Signature.
   * @param doctorId the doctor Id
   * @return doctor signature.
   */

  public static InputStream getDoctorSignature(String doctorId) throws SQLException {

    try (Connection con = DataBaseUtil.getReadOnlyConnection();
      PreparedStatement ps = con.prepareStatement(DOCTOR_SIGNATURE);) {
      ps.setString(1, doctorId);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return rs.getBinaryStream(1);
        } else {
          return null;
        }
      }
    }
  }



  private static final String USER_SIGNATURE = " SELECT signature  FROM user_images "
      + "WHERE emp_username=?";

  /**
   * Get User Signature.
   * @param userName the user Id
   * @return user signature.
   */

  public static InputStream getUserSignature(String userName) throws SQLException {

    try (Connection con = DataBaseUtil.getReadOnlyConnection();
      PreparedStatement ps = con.prepareStatement(USER_SIGNATURE);) {
      ps.setString(1, userName);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return rs.getBinaryStream(1);
        } else {
          return null;
        }
      }
    }
  }

}
