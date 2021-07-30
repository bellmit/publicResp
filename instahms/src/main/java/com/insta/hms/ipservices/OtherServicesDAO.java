package com.insta.hms.ipservices;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

// TODO: Auto-generated Javadoc
/**
 * The Class OtherServicesDAO.
 */
public class OtherServicesDAO {

  /** The Constant OTHER_SERVICES_CANEL. */
  public static final String OTHER_SERVICES_CANEL = "UPDATE "
      + " OTHER_SERVICES_PRESCRIBED SET CANCEL_STATUS='C' "
      + " WHERE PRESCRIBED_ID = ?";

  /**
   * Cancel other services prescribed.
   *
   * @param con
   *          the con
   * @param id
   *          the id
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean cancelOtherServicesPrescribed(Connection con, int id) throws SQLException {
    boolean status = true;
    try (PreparedStatement ps = con.prepareStatement(OTHER_SERVICES_CANEL)) {
      ps.setInt(1, id);
      if (ps.executeUpdate() <= 0) {
        status = false;
      }
    }
    return status;
  }
}
