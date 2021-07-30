package com.insta.instaapi.customer.login;

import com.bob.hms.common.DataBaseUtil;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserDAO {

  private static final String GET_USER_QUERY = "SELECT u.*, d.doctor_name FROM u_user u "
      + "left join doctors d on (u.doctor_id=d.doctor_id) WHERE emp_username= ?";

  private static final String IS_MOD_ADDONS_ENABLED = "SELECT * FROM modules_activated "
      + " WHERE module_id = ? ";

  /**
   * Get User.
   * @param con           Database connection object
   * @param userName      User name to get
   * @return              User BasicDynaBean Object
   * @throws SQLException Query related exception
   */
  public static BasicDynaBean getRecord(Connection con, String userName) throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(GET_USER_QUERY);
      ps.setString(1, userName);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  /**
   * Check if portal mod is active.
   * @param con  Connection object
   * @param moduleId     module id
   * @return boolean true or false
   * @throws SQLException SQL Exception
   */
  public static boolean isModuleActivated(Connection con, String moduleId) throws SQLException {
    PreparedStatement ps = null;
    boolean active = false;
    try {
      ps = con.prepareStatement(IS_MOD_ADDONS_ENABLED);
      ps.setString(1, moduleId);
      BasicDynaBean bean = DataBaseUtil.queryToDynaBean(ps);
      if (bean != null && bean.get("activation_status").toString().equals("Y")) {
        active = true;
      }
      return active;
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }
}
