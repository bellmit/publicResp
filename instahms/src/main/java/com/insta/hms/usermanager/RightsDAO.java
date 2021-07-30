/*
 * Copyright (c) 2007-2008 BOB Technologies. All rights reserved.
 */

package com.insta.hms.usermanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * The Class RightsDAO.
 */
public class RightsDAO {

  /** The con. */
  private Connection con = null;

  /**
   * Instantiates a new rights DAO.
   *
   * @param con
   *          the con
   */
  public RightsDAO(Connection con) {
    this.con = con;
  }

  /*
   * Get Screen/Group rights records as a hashmap (group => access rights)
   */

  /** The Constant GET_SCREEN_RIGHTS. */
  private static final String GET_SCREEN_RIGHTS =
      "SELECT screen_id, rights FROM screen_rights  WHERE role_id=?";

  /**
   * Gets the screen rights.
   *
   * @param roleId
   *          the role id
   * @return the screen rights
   * @throws SQLException
   *           the SQL exception
   */
  public HashMap getScreenRights(int roleId) throws SQLException {
    return getRightsWithQuery(roleId, GET_SCREEN_RIGHTS);
  }

  /** The Constant GET_ACTION_RIGHTS. */
  private static final String GET_ACTION_RIGHTS =
      "SELECT action, rights FROM action_rights  WHERE role_id=?";

  /**
   * Gets the action rights.
   *
   * @param roleId
   *          the role id
   * @return the action rights
   * @throws SQLException
   *           the SQL exception
   */
  public HashMap getActionRights(int roleId) throws SQLException {
    return getRightsWithQuery(roleId, GET_ACTION_RIGHTS);
  }

  /** The Constant GET_URL_ACTION_RIGHTS. */
  private static final String GET_URL_ACTION_RIGHTS =
      "SELECT action_id, rights FROM url_action_rights  WHERE role_id=?";

  /**
   * Gets the url action rights.
   *
   * @param roleId
   *          the role id
   * @return the url action rights
   * @throws SQLException
   *           the SQL exception
   */
  public HashMap getUrlActionRights(int roleId) throws SQLException {
    return getRightsWithQuery(roleId, GET_URL_ACTION_RIGHTS);
  }

  /**
   * Gets the rights.
   *
   * @param roleId
   *          the role id
   * @return the rights
   * @throws SQLException
   *           the SQL exception
   */
  public Rights getRights(int roleId) throws SQLException {
    Rights rights = new Rights();
    rights.setScreenRightsMap(getScreenRights(roleId));
    rights.setActionRightsMap(getActionRights(roleId));
    rights.setUrlActionRightsMap(getUrlActionRights(roleId));
    return rights;
  }

  /**
   * Gets the rights with query.
   *
   * @param roleId
   *          the role id
   * @param query
   *          the query
   * @return the rights with query
   * @throws SQLException
   *           the SQL exception
   */
  private HashMap getRightsWithQuery(int roleId, String query) throws SQLException {
    PreparedStatement ps = con.prepareStatement(query);
    ps.setInt(1, roleId);
    ResultSet rs = ps.executeQuery();

    HashMap rightsMap = new HashMap();

    while (rs.next()) {
      String key = rs.getString(1);
      String rights = rs.getString(2);
      rightsMap.put(key, rights);
    }
    rs.close();
    ps.close();
    return rightsMap;
  }

  /**
   * Update the screen/group/action rights. This does a delete and insert, whether or not a record
   * already exists. Maybe we can optimze later, but this is not a frequently used operation, so it
   * is OK to be a little heavy on performance.
   *    *
   * @param roleId
   *          the role id
   * @param rights
   *          the rights
   * @throws SQLException
   *           the SQL exception
   */
  public void setRights(int roleId, Rights rights) throws SQLException {
    setScreenRights(roleId, rights.getScreenRightsMap());
    setActionRights(roleId, rights.getActionRightsMap());
    setUrlActionRights(roleId, rights.getUrlActionRightsMap());
  }

  /** The Constant DELETE_SCREEN_RIGHTS. */
  private static final String DELETE_SCREEN_RIGHTS = "DELETE FROM screen_rights WHERE role_id=?";

  /** The Constant DELETE_ACTION_RIGHTS. */
  private static final String DELETE_ACTION_RIGHTS = "DELETE FROM action_rights WHERE role_id=?";

  /** The Constant DELETE_URL_ACTION_RIGHTS. */
  private static final String DELETE_URL_ACTION_RIGHTS =
      "DELETE FROM url_action_rights WHERE role_id=?";

  /** The Constant INSERT_SCREEN_RIGHTS. */
  private static final String INSERT_SCREEN_RIGHTS =
      "INSERT INTO screen_rights (role_id, screen_id, rights) VALUES (?, ?, ?)";

  /** The Constant INSERT_ACTION_RIGHTS. */
  private static final String INSERT_ACTION_RIGHTS =
      "INSERT INTO action_rights (role_id, action, rights) VALUES (?, ?, ?)";

  /** The Constant INSERT_URL_ACTION_RIGHTS. */
  private static final String INSERT_URL_ACTION_RIGHTS =
      "INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (?, ?, ?)";

  /**
   * Sets the screen rights.
   *
   * @param roleId
   *          the role id
   * @param map
   *          the a
   * @throws SQLException
   *           the SQL exception
   */
  public void setScreenRights(int roleId, HashMap map) throws SQLException {
    setRightsWithQuery(roleId, map, DELETE_SCREEN_RIGHTS, INSERT_SCREEN_RIGHTS);
  }

  /**
   * Sets the action rights.
   *
   * @param roleId
   *          the role id
   * @param map
   *          the a
   * @throws SQLException
   *           the SQL exception
   */
  public void setActionRights(int roleId, HashMap map) throws SQLException {
    setRightsWithQuery(roleId, map, DELETE_ACTION_RIGHTS, INSERT_ACTION_RIGHTS);
  }

  /**
   * Sets the url action rights.
   *
   * @param roleId
   *          the role id
   * @param map
   *          the a
   * @throws SQLException
   *           the SQL exception
   */
  public void setUrlActionRights(int roleId, HashMap map) throws SQLException {
    setRightsWithQuery(roleId, map, DELETE_URL_ACTION_RIGHTS, INSERT_URL_ACTION_RIGHTS);
  }

  /**
   * Sets the rights with query.
   *
   * @param roleId
   *          the role id
   * @param rights
   *          the rights
   * @param delQuery
   *          the del query
   * @param insQuery
   *          the ins query
   * @throws SQLException
   *           the SQL exception
   */
  private void setRightsWithQuery(int roleId, HashMap rights, String delQuery, String insQuery)
      throws SQLException {
    PreparedStatement ps = con.prepareStatement(delQuery);
    ps.setInt(1, roleId);
    ps.executeUpdate();

    // ignore the result, because this role may not have had any rows existing
    ps.close();

    ps = con.prepareStatement(insQuery);
    ps.setInt(1, roleId);
    for (Iterator i = rights.entrySet().iterator(); i.hasNext();) {
      Map.Entry mapEntry = (Map.Entry) i.next();
      ps.setString(2, (String) mapEntry.getKey());
      ps.setString(3, (String) mapEntry.getValue());
      ps.executeUpdate();
    }
    ps.close();
  }

}
