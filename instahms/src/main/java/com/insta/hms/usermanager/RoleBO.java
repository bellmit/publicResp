/*
 * Copyright (c) 2007-2008 BOB Technologies, all rights reserved.
 *
 */

package com.insta.hms.usermanager;

import com.bob.hms.common.DataBaseUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * The Class RoleBO.
 */
public class RoleBO {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(RoleBO.class);

  /**
   * Create a role with the given role attributes as well as rights for the role.
   *
   * @param role
   *          the role
   * @param rights
   *          the rights
   * @return the string
   * @throws SQLException
   *           the SQL exception
   */
  public String createRole(Role role, Rights rights) throws SQLException {
    Connection con = null;
    boolean success = true;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false); // start a transaction
      RoleDAO roleDAO = new RoleDAO(con);

      Role exist = roleDAO.getRoleFromName(role.getName());
      if (exist != null) {
        success = false;
        return "exist";
      }

      // Create the role object
      int roleId = roleDAO.getNextRoleId();
      role.setRoleId(roleId);
      success = roleDAO.createRole(role);
      if (!success) {
        logger.error("RoleDAO returned error while creating role");
        return "failed";
      }

      // Set access rights objects for this role
      RightsDAO rightsDAO = new RightsDAO(con);
      rightsDAO.setRights(roleId, rights);
      return "success";

    } catch (SQLException sqlException) {
      success = false;
      logger.error("Exception during createRole:", sqlException);
      return "failed";

    } finally {
      // check for success and close the connection after a commit/rollback
      if (con != null) {
        if (success) {
          con.commit();
        } else {
          con.rollback();
        }
        con.close();
      }
    }
  }

  /**
   * Modify a role, identified by role.roleId, change access rights in addition.
   *
   * @param role
   *          the role
   * @param rights
   *          the rights
   * @return the string
   * @throws SQLException
   *           the SQL exception
   */
  public String modifyRole(Role role, Rights rights) throws SQLException {
    Connection con = null;
    boolean success = true;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false); // start a transaction
      RoleDAO roleDAO = new RoleDAO(con);

      Role exists = roleDAO.roleExists(role.getName(), role.getRoleId());

      if (exists != null) {
        success = false;
        return "exist";
      }
      // Update the role object
      success = roleDAO.updateRole(role);
      if (!success) {
        return "failed";
      }

      // Set access rights objects for this role
      RightsDAO rightsDAO = new RightsDAO(con);
      rightsDAO.setRights(role.getRoleId(), rights);

      return "success";

    } catch (SQLException sqlException) {
      logger.error("Exception during modifyRole:", sqlException);
      success = false;
      return "failed";

    } finally {
      // check for success and close the connection after a commit/rollback
      if (null != con) {
        if (success) {
          con.commit();
        } else {
          con.rollback();
        }
        con.close();
      }
    }
  }

  /**
   * Retrieve a role DTO given the roleId.
   *
   * @param roleId
   *          the role id
   * @return the role from id
   * @throws SQLException
   *           the SQL exception
   */
  public Role getRoleFromId(int roleId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    RoleDAO dao = new RoleDAO(con);
    Role role = dao.getRoleFromId(roleId);
    con.close();
    return role;
  }

  /**
   * Retrieve a role DTO given the role Name (which is also unique).
   *
   * @param name
   *          the name
   * @return the role from name
   * @throws SQLException
   *           the SQL exception
   */
  public Role getRoleFromName(String name) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    RoleDAO dao = new RoleDAO(con);
    Role role = dao.getRoleFromName(name);
    con.close();
    return role;
  }

  /**
   * Retrieve the rights for a given role identified by the role ID.
   *
   * @param roleId
   *          the role id
   * @return the rights
   * @throws SQLException
   *           the SQL exception
   */
  public Rights getRights(int roleId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    RightsDAO dao = new RightsDAO(con);
    return dao.getRights(roleId);
  }

  /**
   * Return a list of Role DTOs.
   *
   * @return the all roles
   * @throws SQLException
   *           the SQL exception
   */
  public List getAllRoles() throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    RoleDAO dao = new RoleDAO(con);
    List roleList = dao.getAllRoles();
    con.close();
    return roleList;
  }

  /**
   * Return the role details: role attributes + rights for the role.
   *
   * @param roleId
   *          the role id
   * @return the details
   * @throws SQLException
   *           the SQL exception
   */
  public RoleDetails getDetails(int roleId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    RoleDAO roleDao = new RoleDAO(con);
    RightsDAO rightsDao = new RightsDAO(con);

    Role role = roleDao.getRoleFromId(roleId);
    Rights rights = rightsDao.getRights(roleId);
    con.close();

    RoleDetails rd = new RoleDetails();
    rd.setRole(role);
    rd.setRights(rights);
    return rd;
  }

}
