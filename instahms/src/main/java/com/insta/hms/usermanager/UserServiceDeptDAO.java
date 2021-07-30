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
 * The Class UserServiceDeptDAO.
 *
 * @author prasanna
 */

public class UserServiceDeptDAO extends GenericDAO {

  /**
   * Instantiates a new user service dept DAO.
   */
  public UserServiceDeptDAO() {
    super("user_services_depts");
  }

  /** The Constant GET_USER_SERV_DEPT. */
  private static final String GET_USER_SERV_DEPT = "SELECT serv_dept_id FROM user_services_depts "
      + " WHERE emp_username = ?";

  /**
   * Gets the user service department.
   *
   * @param userid the userid
   * @return the user service department
   * @throws SQLException the SQL exception
   */
  public List<Hashtable<String, Object>> getUserServiceDepartment(String userid)
      throws SQLException {

    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_USER_SERV_DEPT);
      ps.setString(1, userid);
      return DataBaseUtil.queryToArrayList(ps);
    } finally {
      ps.close();
      con.close();
    }
  }

  /** The Constant GET_SEARCH_USER_SERV_DEPT. */
  private static final String GET_SEARCH_USER_SERV_DEPT = "SELECT serv_dept_id FROM "
      + " user_services_depts WHERE emp_username = ? and serv_dept_id = ?";

  /**
   * Gets the user search service department.
   *
   * @param userid the userid
   * @param servdeptid the servdeptid
   * @return the user search service department
   * @throws SQLException the SQL exception
   */
  public List<Hashtable<String, Object>> getUserSearchServiceDepartment(String userid,
      int servdeptid) throws SQLException {

    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_SEARCH_USER_SERV_DEPT);
      ps.setString(1, userid);
      ps.setInt(2, servdeptid);
      return DataBaseUtil.queryToArrayList(ps);
    } finally {
      ps.close();
      con.close();
    }
  }

  /** The Constant GET_SEARCH_USER_SERV_NAme. */
  private static final String GET_SEARCH_USER_SERV_NAme = "SELECT usd.serv_dept_id FROM services s"
      + " LEFT JOIN user_services_depts usd on (s.serv_dept_id=usd.serv_dept_id) "
      + " where usd.emp_username = ?  AND (service_name ilike ? or service_name ilike ?)";

  /**
   * Gets the user search service name.
   *
   * @param userid the userid
   * @param servname the servname
   * @return the user search service name
   * @throws SQLException the SQL exception
   */
  public List<Hashtable<String, Object>> getUserSearchServiceName(String userid, String servname)
      throws SQLException {

    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_SEARCH_USER_SERV_NAme);
      ps.setString(1, userid);
      ps.setString(2, servname + "%");
      ps.setString(3, "% " + servname + "%");
      return DataBaseUtil.queryToArrayList(ps);
    } finally {
      ps.close();
      con.close();
    }
  }

  /** The Constant GET_SEARCH_SERV_VISIT_TYPE. */
  private static final String GET_SEARCH_SERV_VISIT_TYPE = 
      " SELECT usd.serv_dept_id from services_prescribed sp"
      + " LEFT JOIN patient_registration pr on(pr.patient_id = sp.patient_id)"
      + " LEFT JOIN services s on(s.service_id=sp.service_id) "
      + " LEFT JOIN user_services_depts usd on (s.serv_dept_id=usd.serv_dept_id)"
      + " Where usd.emp_username = ?  AND pr.visit_type=? ";

  /**
   * Gets the user search service visit type.
   *
   * @param userid the userid
   * @param visittype the visittype
   * @return the user search service visit type
   * @throws SQLException the SQL exception
   */
  public List<Hashtable<String, Object>> getUserSearchServiceVisitType(String userid,
      String visittype) throws SQLException {

    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_SEARCH_SERV_VISIT_TYPE);
      ps.setString(1, userid);
      ps.setString(2, visittype);
      return DataBaseUtil.queryToArrayList(ps);
    } finally {
      ps.close();
      con.close();
    }
  }

  /** The Constant GET_SERV_DEPT. */
  private static final String GET_SERV_DEPT = "SELECT department,serv_dept_id"
      + " from  services_departments where status='A'  order by department";

  /**
   * Gets the user serv dept.
   *
   * @return the user serv dept
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getUserServDept() throws SQLException {
    Connection con = DataBaseUtil.getReadOnlyConnection();
    PreparedStatement ps = null;
    List<BasicDynaBean> userservdeptList = null;
    try {

      ps = con.prepareStatement(GET_SERV_DEPT);
      userservdeptList = DataBaseUtil.queryToDynaList(ps);

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }

    return userservdeptList;

  }

  /** The Constant GET_SELECT_SERV_DEPT. */
  private static final String GET_SELECT_SERV_DEPT = "SELECT sd.department,sd.serv_dept_id"
      + " from  services_departments sd"
      + " LEFT JOIN user_services_depts usd  ON(usd.serv_dept_id=sd.serv_dept_id)"
      + " WHERE usd.emp_username=?  order by department";

  /**
   * Gets the user select serv dept.
   *
   * @param username the username
   * @return the user select serv dept
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getUserSelectServDept(String username) throws SQLException {
    Connection con = DataBaseUtil.getReadOnlyConnection();
    PreparedStatement ps = null;
    List<BasicDynaBean> userservdeptList = null;
    try {

      ps = con.prepareStatement(GET_SELECT_SERV_DEPT);
      ps.setString(1, username);
      userservdeptList = DataBaseUtil.queryToDynaList(ps);

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }

    return userservdeptList;

  }

  /** The Constant GET_SERV_NAME. */
  private static final String GET_SERV_NAME = "SELECT s.service_name from services s "
      + " LEFT JOIN services_departments sd ON (sd.serv_dept_id = s.serv_dept_id) "
      + " LEFT JOIN user_services_depts usd ON (s.serv_dept_id = usd.serv_dept_id) "
      + " WHERE usd.emp_username=? order by service_name ";

  /**
   * Gets the services name.
   *
   * @param username the username
   * @return the services name
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getServicesName(String username) throws SQLException {
    Connection con = DataBaseUtil.getReadOnlyConnection();
    PreparedStatement ps = null;
    List<BasicDynaBean> userservnameList = null;
    try {

      ps = con.prepareStatement(GET_SERV_NAME);
      ps.setString(1, username);
      userservnameList = DataBaseUtil.queryToDynaList(ps);

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }

    return userservnameList;

  }

}
