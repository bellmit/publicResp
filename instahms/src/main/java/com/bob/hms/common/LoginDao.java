/**
 *
 */

package com.bob.hms.common;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * The Class LoginDAO.
 *
 * @author nikunj.s
 */
public class LoginDao {

  private String userAuthQuery = " SELECT emp_username, u.role_id, role_name, is_shared_login,"
      + "  center_id, (SELECT rights FROM screen_rights sr WHERE (sr.role_id = u.role_id) "
      + " AND screen_id = ?) AS rights " + " FROM u_user u " + " JOIN u_role r USING (role_id) "
      + " WHERE u.role_id = r.role_id AND emp_status = 'A' " + " AND emp_username=? ";

  /**
   * Checks if is valid user and has access.
   *
   * @param userId   the user id
   * @param actionId the action id
   * @return the basic dyna bean
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean isValidUserAndHasAccess(String userId, String actionId) throws SQLException {
    return isValidUserAndHasAccess(userId, actionId, null);
  }

  /**
   * Checks if is valid user and has access.
   *
   * @param userId   the user id
   * @param actionId the action id
   * @param password the password
   * @return the basic dyna bean
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean isValidUserAndHasAccess(String userId, String actionId, String password)
      throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    Object[] params = null;
    try {
      if (null != password) {
        userAuthQuery = userAuthQuery + " AND emp_password=? ";
        params = new Object[] { actionId, userId, password };
      } else {
        params = new Object[] { actionId, userId };
      }
      return DataBaseUtil.queryToDynaBean(userAuthQuery, params);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }
}
