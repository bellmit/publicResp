package com.insta.hms.usermanager;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.SQLException;
import java.util.List;

/**
 * The Class UserHospitalRoleMasterDAO.
 *
 * @author Anil N
 */

public class UserHospitalRoleMasterDAO extends GenericDAO {

  /**
   * Instantiates a new user hospital role master DAO.
   */
  public UserHospitalRoleMasterDAO() {
    super("user_hosp_role_master");
  }

  /** The Constant GET_ALL_HOSP_ROLE_IDS. */
  public static final String GET_ALL_HOSP_ROLE_IDS = "SELECT * FROM user_hosp_role_master "
      + " WHERE u_user = ?";

  /**
   * Gets the user hosp role ids.
   *
   * @param user
   *          the user
   * @return the user hosp role ids
   * @throws SQLException
   *           the SQL exception
   */
  public static List<BasicDynaBean> getUserHospRoleIds(String user) throws SQLException {
    return DataBaseUtil.queryToDynaList(GET_ALL_HOSP_ROLE_IDS, user);
  }
}
