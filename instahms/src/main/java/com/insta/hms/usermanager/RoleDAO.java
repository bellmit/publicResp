package com.insta.hms.usermanager;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class RoleDAO.
 */
public class RoleDAO {

  /** The con. */
  private Connection con = null;

  /**
   * Instantiates a new role DAO.
   *
   * @param con
   *          the con
   */
  public RoleDAO(Connection con) {
    this.con = con;
  }

  /** The Constant GET_ALL_ROLES. */
  /*
   * Get all roles: returns a list of Role DTOs (except InstaAdmin)
   */
  private static final String GET_ALL_ROLES = " SELECT role_id, role_name, role_status, role_remk,"
      + " portal_id,mod_user, to_char(u_role.mod_date, 'dd-mm-yyyy HH24:mi') as modDate "
      + " FROM u_role WHERE role_id!=1 AND role_name!='AddonsAdmin' order by role_name ";

  /**
   * Gets the all roles.
   *
   * @return the all roles
   * @throws SQLException
   *           the SQL exception
   */
  public List getAllRoles() throws SQLException {
    PreparedStatement ps = con.prepareStatement(GET_ALL_ROLES);
    ResultSet rs = ps.executeQuery();
    ArrayList allRoles = new ArrayList();

    while (rs.next()) {
      Role role = new Role();
      populateRole(role, rs);
      allRoles.add(role);
    }
    rs.close();
    ps.close();
    return allRoles;
  }

  /** The Constant ROLES_FOR_ACTION_RIGHTS. */
  private static final String ROLES_FOR_ACTION_RIGHTS =
      "SELECT action, role_id, rights FROM action_rights WHERE action=?";

  /**
   * Gets the roles for action rights.
   *
   * @param actionRights
   *          the action rights
   * @return the roles for action rights
   * @throws SQLException
   *           the SQL exception
   */
  public static List getRolesForActionRights(String actionRights) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(ROLES_FOR_ACTION_RIGHTS);
      ps.setString(1, actionRights);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_ROLE_ID. */
  /*
   * Get a role from the ID. Returns null if a role with the given id is not found.
   */
  private static final String GET_ROLE_ID = " SELECT role_id, role_name, role_status, role_remk, "
      + " portal_id, mod_user, to_char(u_role.mod_date, 'dd-mm-yyyy HH24:mi') as modDate "
      + " FROM u_role WHERE role_id=?";

  /**
   * Gets the role from id.
   *
   * @param roleId
   *          the role id
   * @return the role from id
   * @throws SQLException
   *           the SQL exception
   */
  public Role getRoleFromId(int roleId) throws SQLException {
    PreparedStatement ps = con.prepareStatement(GET_ROLE_ID);
    ps.setInt(1, roleId);
    ResultSet rs = ps.executeQuery();

    Role role = null;
    if (rs.next()) {
      role = new Role();
      populateRole(role, rs);
    }
    rs.close();
    ps.close();
    return role;
  }

  /** The Constant GET_ROLE_NAME. */
  /*
   * Get a role from the role name. Returns null if a role is not found with the given name.
   */
  private static final String GET_ROLE_NAME = " SELECT role_id, role_name, role_status, "
      + " role_remk,portal_id, mod_user, to_char(u_role.mod_date, 'dd-mm-yyyy HH24:mi') as "
      + " modDate FROM u_role WHERE LOWER(role_name) = LOWER(?)";

  /**
   * Gets the role from name.
   *
   * @param roleName
   *          the role name
   * @return the role from name
   * @throws SQLException
   *           the SQL exception
   */
  public Role getRoleFromName(String roleName) throws SQLException {
    PreparedStatement ps = con.prepareStatement(GET_ROLE_NAME);
    ps.setString(1, roleName);
    ResultSet rs = ps.executeQuery();

    Role role = null;
    if (rs.next()) {
      role = new Role();
      populateRole(role, rs);
    }
    rs.close();
    ps.close();
    return role;
  }

  /** The Constant ROLE_EXISTS. */
  private static final String ROLE_EXISTS = " SELECT role_id, role_name, role_status, "
      + " role_remk,portal_id, mod_user, to_char(u_role.mod_date, 'dd-mm-yyyy HH24:mi') as "
      + " modDate FROM u_role WHERE LOWER(role_name) = LOWER(?) AND role_id!=?";

  /**
   * Role exists.
   *
   * @param roleName
   *          the role name
   * @param roleId
   *          the role id
   * @return the role
   * @throws SQLException
   *           the SQL exception
   */
  public Role roleExists(String roleName, int roleId) throws SQLException {
    PreparedStatement ps = con.prepareStatement(ROLE_EXISTS);
    ps.setString(1, roleName);
    ps.setInt(2, roleId);
    ResultSet rs = ps.executeQuery();

    Role role = null;
    if (rs.next()) {
      role = new Role();
      populateRole(role, rs);
    }
    rs.close();
    ps.close();
    return role;
  }

  /** The Constant NEXT_ROLE_ID. */
  /*
   * Get next available role_id number TODO: convert this to a sequence.
   */
  public static final String NEXT_ROLE_ID = "SELECT ( COALESCE(MAX(role_id),0) + 1 ) FROM u_role";

  /**
   * Gets the next role id.
   *
   * @return the next role id
   * @throws SQLException
   *           the SQL exception
   */
  public int getNextRoleId() throws SQLException {
    PreparedStatement ps = con.prepareStatement(NEXT_ROLE_ID);
    // todo: change to this: int retval = DataBaseUtil.getIntValueFromDB(ps);
    String stringRoleId = DataBaseUtil.getStringValueFromDb(NEXT_ROLE_ID);
    int intRoleId = Integer.parseInt(stringRoleId);
    ps.close();
    return intRoleId;
  }

  /** The Constant CREATE_ROLE. */
  /*
   * Insert a role into the role table. Role ID must be generated and supplied to this method, it
   * will not be auto-generated. Use getNextRoleId for that purpose.
   */
  private static final String CREATE_ROLE = "INSERT INTO u_role (role_id, role_name, "
      + " role_status, role_remk, mod_user, mod_date) VALUES (?, ?, ?, ?, ?, ?)";

  /**
   * Creates the role.
   *
   * @param userRole
   *          the user role
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean createRole(Role userRole) throws SQLException {
    PreparedStatement ps = con.prepareStatement(CREATE_ROLE);

    ps.setInt(1, userRole.getRoleId());
    ps.setString(2, userRole.getName());
    ps.setString(3, userRole.getStatus());
    ps.setString(4, userRole.getRemarks());
    ps.setString(5, userRole.getModUser());
    ps.setTimestamp(6, DateUtil.getCurrentTimestamp());

    int resultCount = ps.executeUpdate();
    ps.close();

    return resultCount == 1;
  }

  /**
   * Populate role.
   *
   * @param role
   *          the r
   * @param rs
   *          the rs
   * @throws SQLException
   *           the SQL exception
   */
  private void populateRole(Role role, ResultSet rs) throws SQLException {
    role.setRoleId(rs.getInt("role_id"));
    role.setName(rs.getString("role_name"));
    role.setStatus(rs.getString("role_status"));
    role.setRemarks(rs.getString("role_remk"));
    role.setPortalId(rs.getString("portal_id"));
    role.setModUser(rs.getString("mod_user"));
    role.setModDate(rs.getString("modDate"));
  }

  /** The Constant UPDATE_ROLE. */
  /*
   * Update a role with new values
   */
  private static final String UPDATE_ROLE = "UPDATE u_role set role_name=?, role_status=?, "
      + " role_remk=? , mod_user=?, mod_date=? WHERE role_id=?";

  /**
   * Update role.
   *
   * @param userRole
   *          the user role
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean updateRole(Role userRole) throws SQLException {
    PreparedStatement ps = con.prepareStatement(UPDATE_ROLE);

    ps.setString(1, userRole.getName());
    ps.setString(2, userRole.getStatus());
    ps.setString(3, userRole.getRemarks());
    ps.setString(4, userRole.getModUser());
    ps.setTimestamp(5, DateUtil.getCurrentTimestamp());
    ps.setInt(6, userRole.getRoleId());

    int resultCount = ps.executeUpdate();
    ps.close();

    return resultCount == 1;
  }

  /** The Constant GET_ACTION_RIGHTS. */
  private static final String GET_ACTION_RIGHTS =
      "SELECT rights FROM action_rights WHERE role_id=? AND action=? ";

  /**
   * Gets the action rights.
   *
   * @param roleId
   *          the role id
   * @param action
   *          the action
   * @return the action rights
   * @throws SQLException
   *           the SQL exception
   */
  public String getActionRights(int roleId, String action) throws SQLException {
    PreparedStatement ps = null;
    Connection con = null;
    String actionRight = null;
    con = DataBaseUtil.getReadOnlyConnection();
    Role role = new Role();
    ps = con.prepareStatement(GET_ACTION_RIGHTS);
    ps.setInt(1, roleId);
    ps.setString(2, action);
    ResultSet rs = ps.executeQuery();
    if (rs.next()) {
      actionRight = rs.getString("rights");

    }
    rs.close();
    ps.close();
    con.close();

    return actionRight;

  }

  /** The Constant ROLES. */
  private static final String ROLES = "SELECT role_id, role_name FROM u_role WHERE role_status='A'";

  /** The Constant ORDER_BY_ROLE. */
  private static final String ORDER_BY_ROLE = "order by role_name asc";

  /** The Constant EXCLUDE_INSTA_ADMIN. */
  private static final String EXCLUDE_INSTA_ADMIN = " AND role_id != 1 ";

  /**
   * Gets the roles.
   *
   * @param excludeInstaAdmin
   *          the exclude insta admin
   * @return the roles
   * @throws SQLException
   *           the SQL exception
   */
  public static List getRoles(boolean excludeInstaAdmin) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(
          ROLES + (excludeInstaAdmin ? EXCLUDE_INSTA_ADMIN + ORDER_BY_ROLE : ORDER_BY_ROLE));
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_PAGE_STAT_VALS. */
  /*
   * get the page stat values stored in the DB
   */
  private static final String GET_PAGE_STAT_VALS = "SELECT " + "  stats_item1 " + " ,stats_item2 "
      + " ,stats_item3 " + " ,stats_item4 " + " ,stats_item5 " + " ,stats_item6 " + " ,stats_item7 "
      + " ,stats_item8 " + " ,stats_item9 " + " ,stats_item10 " + " ,stats_item11 "
      + " ,stats_item12 " + " FROM u_role WHERE role_id = ? ";

  /**
   * Gets the page stat vals.
   *
   * @param roleId
   *          the role id
   * @return the page stat vals
   * @throws SQLException
   *           the SQL exception
   */
  public static Map getPageStatVals(String roleId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = con.prepareStatement(GET_PAGE_STAT_VALS);
      ps.setInt(1, Integer.parseInt(roleId));
      rs = ps.executeQuery();
      Map<String, String> statsMap = new HashMap<String, String>();
      if (rs.next()) {
        statsMap.put("stats_item1", rs.getString("stats_item1"));
        statsMap.put("stats_item2", rs.getString("stats_item2"));
        statsMap.put("stats_item3", rs.getString("stats_item3"));
        statsMap.put("stats_item4", rs.getString("stats_item4"));
        statsMap.put("stats_item5", rs.getString("stats_item5"));
        statsMap.put("stats_item6", rs.getString("stats_item6"));
        statsMap.put("stats_item7", rs.getString("stats_item7"));
        statsMap.put("stats_item8", rs.getString("stats_item8"));
        statsMap.put("stats_item9", rs.getString("stats_item9"));
        statsMap.put("stats_item10", rs.getString("stats_item10"));
        statsMap.put("stats_item11", rs.getString("stats_item11"));
        statsMap.put("stats_item12", rs.getString("stats_item12"));
      }
      return statsMap;

    } finally {
      DataBaseUtil.closeConnections(con, ps, rs);
    }
  }

  /**
   * Gets the individual page stat value.
   *
   * @param roleId
   *          the role id
   * @param statNo
   *          the stat no
   * @return the individual page stat value
   * @throws SQLException
   *           the SQL exception
   */
  public String getIndividualPageStatValue(int roleId, String statNo) throws SQLException {
    PreparedStatement ps = null;
    ResultSet rs = null;
    String res = null;
    try {
      String field = "stats_item" + statNo;
      ps = con.prepareStatement(
          "SELECT " + DataBaseUtil.quoteIdent(field) + " FROM  u_role WHERE role_id=? ");
      ps.setInt(1, roleId);
      rs = ps.executeQuery();
      if (rs.next()) {
        res = rs.getString(field);
      }
    } finally {
      rs.close();
      ps.close();
    }
    return res;
  }

  /** The Constant UPDATE_PAGE_STAT_DETAILS. */
  /*
   * Update a role with new Page Stat values
   */
  private static final String UPDATE_PAGE_STAT_DETAILS = "UPDATE u_role set " + " stats_item1 = ? "
      + " ,stats_item2 = ? " + " ,stats_item3 = ? " + " ,stats_item4 = ? " + " ,stats_item5 = ? "
      + " ,stats_item6 = ? " + " ,stats_item7 = ? " + " ,stats_item8 = ? " + " ,stats_item9 = ? "
      + " ,stats_item10 = ? " + " ,stats_item11 = ? " + " ,stats_item12 = ? " + "WHERE role_id=? ";

  /**
   * Update page stat details.
   *
   * @param roleId
   *          the role id
   * @param statsItem
   *          the stats item
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean updatePageStatDetails(String roleId, String[] statsItem) throws SQLException {
    PreparedStatement ps = con.prepareStatement(UPDATE_PAGE_STAT_DETAILS);

    ps.setString(1, statsItem[1]);
    ps.setString(2, statsItem[2]);
    ps.setString(3, statsItem[3]);
    ps.setString(4, statsItem[4]);
    ps.setString(5, statsItem[5]);
    ps.setString(6, statsItem[6]);
    ps.setString(7, statsItem[7]);
    ps.setString(8, statsItem[8]);
    ps.setString(9, statsItem[9]);
    ps.setString(10, statsItem[10]);
    ps.setString(11, statsItem[11]);
    ps.setString(12, statsItem[12]);
    ps.setInt(13, Integer.parseInt(roleId));

    int resultCount = ps.executeUpdate();
    ps.close();

    return resultCount == 1;
  }

  /** The has screen rights. */
  private static String HAS_SCREEN_RIGHTS = " SELECT * FROM ( SELECT screen_id, rights, role_id "
      + " FROM screen_rights UNION SELECT action, rights, role_id  FROM action_rights UNION "
      + " SELECT action_id, rights, role_id FROM url_action_rights "
      + " ) as foo where rights='A' AND screen_id=? and role_id=? ";

  /**
   * Checks for rights.
   *
   * @param screenId
   *          the screen id
   * @param roleId
   *          the role id
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean hasRights(String screenId, int roleId) throws SQLException {
    if (roleId == 1 || roleId == 2) {
      return true;
    }

    PreparedStatement ps = con.prepareStatement(HAS_SCREEN_RIGHTS);
    ps.setString(1, screenId);
    ps.setInt(2, roleId);

    ResultSet rs = ps.executeQuery();

    Boolean hasRights;
    if (rs.next()) {
      hasRights = true;
    } else {
      hasRights = false;
    }
    rs.close();
    ps.close();
    return hasRights;
  }

}
