package com.insta.hms.usermanager;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Logger;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * The Class UserDashBoardDAO.
 */
public class UserDashBoardDAO {

  /** The Constant GET_USER_ROLES_FIELDS. */
  private static final String GET_USER_ROLES_FIELDS = "SELECT foo.* FROM ";

  /** The Constant GET_USER_ROLES_COUNT. */
  private static final String GET_USER_ROLES_COUNT = "SELECT count(foo.role_id) FROM ";

  /** The Constant GET_USER_ROLES_TABLES. */
  private static final String GET_USER_ROLES_TABLES = " ( select * from (SELECT ur.role_id, "
      + " ur.role_name,uu.emp_username,ur.portal_id,coalesce(uu.emp_status,'A') "
      + " As emp_status,hosp_roles,hosp_role_id, "
      + " last_login, total_login,hcm.center_name,uu.center_id,uu.sample_collection_center "
      + " FROM u_user uu " + " RIGHT JOIN u_role ur ON(ur.role_id=uu.role_id AND hosp_user='Y') "
      + " LEFT JOIN hospital_center_master hcm ON(uu.center_id = hcm.center_id) "
      + " left JOIN (select textcat_commacat(hosp_role_name) as hosp_roles, "
      + " textcat_commacat(hr.hosp_role_id::text) as hosp_role_id,u_user "
      + " from user_hosp_role_master uhr "
      + " JOIN hospital_roles_master hr ON(uhr.hosp_role_id=hr.hosp_role_id) "
      + " group by u_user) as doo ON(doo.u_user=uu.emp_username)) as noo ) as foo ";

  /**
   * Gets the user dash board.
   *
   * @param roleName the role name
   * @param pageNum the page num
   * @param userName the user name
   * @param formSort the form sort
   * @param sortReverse the sort reverse
   * @param statusList the status list
   * @param userTypeList the user type list
   * @param centerId the center id
   * @param collectionCenterId the collection center id
   * @param hospRoleId the hosp role id
   * @return the user dash board
   * @throws SQLException the SQL exception
   */
  public static PagedList getUserDashBoard(String roleName, int pageNum, String userName,
      String formSort, boolean sortReverse, List statusList, List userTypeList, String centerId,
      String collectionCenterId, String hospRoleId) throws SQLException {

    Connection con = null;
    List userRolesList = null;
    boolean treatmentAll = false;
    int totalCount = 0;

    try {

      con = DataBaseUtil.getReadOnlyConnection();
      if (formSort == null) {
        formSort = "role_name";
        sortReverse = true;
      }
      SearchQueryBuilder qb = null;
      qb = new SearchQueryBuilder(con, GET_USER_ROLES_FIELDS, GET_USER_ROLES_COUNT,
          GET_USER_ROLES_TABLES,
          "WHERE foo.role_id != 1 AND foo.role_name not ilike ('AddonsAdmin') ", formSort,
          sortReverse, 20, pageNum);

      // add the value for the initial where clause

      qb.addFilter(qb.STRING, "foo.role_name", "ilike", roleName);
      qb.addFilter(qb.STRING, "foo.emp_username", "ilike", userName);
      qb.addFilter(qb.STRING, "foo.emp_status", "IN", statusList);
      qb.addFilter(qb.STRING, "foo.portal_id", "IN", userTypeList);

      if (null != centerId && !centerId.equals("")) {
        qb.addFilter(qb.INTEGER, "foo.center_id", "=", Integer.parseInt(centerId));
      }

      if (null != collectionCenterId && !collectionCenterId.equals("")) {
        qb.addFilter(qb.INTEGER, "foo.sample_collection_center", "=",
            Integer.parseInt(collectionCenterId));
      }

      // this is for hospital roles filter. becareful with spaces and % symbols in the qry
      // expression
      if (null != hospRoleId && !hospRoleId.equals("")) {
        qb.appendToQuery("hosp_role_id ilike '" + hospRoleId + ", %' or hosp_role_id ilike '% "
            + hospRoleId + "' or hosp_role_id ilike '% " + hospRoleId + ",%' "
            + " or hosp_role_id ilike '" + hospRoleId + "' ");
      }

      qb.addSecondarySort("role_id");
      qb.build();
      PreparedStatement psData = qb.getDataStatement();
      PreparedStatement psCount = qb.getCountStatement();
      userRolesList = DataBaseUtil.queryToDynaList(psData);

      totalCount = Integer.parseInt(DataBaseUtil.getStringValueFromDb(psCount));
      psData.close();
      psCount.close();

    } finally {
      if (con != null) {
        con.close();
      }
    }

    return new PagedList(userRolesList, totalCount, 20, pageNum);
  }

  /** The get all roles. */
  private static String GET_ALL_ROLES = " select role_name from u_role order by role_name ";

  /**
   * Gets the all role names.
   *
   * @return the all role names
   */
  public static List getAllRoleNames() {
    PreparedStatement ps = null;
    ArrayList roleNameList = null;
    Connection con = null;

    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_ALL_ROLES);
      roleNameList = DataBaseUtil.queryToArrayList1(ps);
    } catch (SQLException sqlException) {
      Logger.log(sqlException);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }

    return roleNameList;
  }

  /** The get center wise users. */
  private static String GET_CENTER_WISE_USERS = " select emp_username from u_user where "
      + " hosp_user='Y' and center_id=?  order by emp_username ";
  
  /** The get all users. */
  private static String GET_ALL_USERS = " select emp_username from u_user where "
      + " hosp_user='Y' order by emp_username ";

  /**
   * Gets the all user names.
   *
   * @return the all user names
   */
  public static List getAllUserNames() {
    PreparedStatement ps = null;
    ArrayList userNameList = null;
    Connection con = null;
    Integer centerID = RequestContext.getCenterId();

    try {
      con = DataBaseUtil.getConnection();
      if (centerID != 0
          && GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1) {
        ps = con.prepareStatement(GET_CENTER_WISE_USERS);
        ps.setInt(1, centerID);
      } else {
        ps = con.prepareStatement(GET_ALL_USERS);
      }
      userNameList = DataBaseUtil.queryToArrayList1(ps);
    } catch (SQLException sqlException) {
      Logger.log(sqlException);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }

    return userNameList;
  }

}
