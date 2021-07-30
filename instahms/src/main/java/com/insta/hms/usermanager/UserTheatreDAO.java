package com.insta.hms.usermanager;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.List;

/**
 * The Class UserTheatreDAO.
 *
 * @author yashwanth
 */

public class UserTheatreDAO extends GenericDAO {

  /**
   * Instantiates a new user theater DAO.
   */
  public UserTheatreDAO() {
    super("user_theatres");
  }

  public static final String GET_DEFAULT_USER_THEATRE_ID = "SELECT theatre_id FROM user_theatres"
      + "" + " WHERE emp_username = ? AND default_theatre = true";

  public static List<BasicDynaBean> getUserDefaultTheatre(String user) throws SQLException {
    List<BasicDynaBean> theatre = DataBaseUtil.queryToDynaList(GET_DEFAULT_USER_THEATRE_ID, user);
    return theatre;
  }
}
