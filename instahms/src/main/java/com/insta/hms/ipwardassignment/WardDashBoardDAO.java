package com.insta.hms.ipwardassignment;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * The Class WardDashBoardDAO.
 */
public class WardDashBoardDAO extends GenericDAO {
  
  /**
   * Instantiates a new ward dash board DAO.
   */
  public WardDashBoardDAO() {
    super("nurse_ward_assignments");
  }

  /** The Constant GET_USER_WARD_DETAILS. */
  public static final String GET_USER_WARD_DETAILS = " Select nwa.emp_username, nwa.ward_id,"
      + " nwa.username,nwa.mod_time,uu.role_id,ur.role_name,wn.ward_name,nwa.ward_id as ward_no "
      + " From nurse_ward_assignments nwa "
      + " LEFT JOIN u_user uu ON(nwa.emp_username = uu.emp_username) "
      + " RIGHT JOIN u_role ur ON(ur.role_id=uu.role_id AND hosp_user='Y') "
      + " JOIN ward_names wn ON (nwa.ward_id = wn.ward_no) " + " where nwa.emp_username=? ";

  /**
   * Gets the user ward details.
   *
   * @param empUserName the emp user name
   * @param centerId the center id
   * @return the user ward details
   * @throws SQLException the SQL exception
   */
  public List getUserWardDetails(String empUserName, int centerId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    String query = GET_USER_WARD_DETAILS;
    try {
      if (centerId != 0) {
        query = query.concat("AND wn.center_id=?");
      }
      ps = con.prepareStatement(query);
      ps.setString(1, empUserName);
      if (centerId != 0) {
        ps.setInt(2, centerId);
      }

      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }

  }

}
