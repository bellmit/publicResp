/*
 * Copyright (c) 2008-2009 Insta Health Solutions Pvt Ltd All rights reserved.
 */

package com.insta.hms.usermanager;

import com.bob.hms.common.Preferences;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;

import flexjson.JSONSerializer;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * The Class RoleAction.
 */
public class RoleAction extends DispatchAction {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(RoleAction.class);

  /** The bo. */
  RoleBO bo = new RoleBO();

  /**
   * Gets the role screen.
   *
   * @param mapping
   *          the mapping
   * @param af
   *          the af
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the role screen
   * @throws SQLException
   *           the SQL exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getRoleScreen(ActionMapping mapping, ActionForm af,
      HttpServletRequest request, HttpServletResponse response) throws SQLException {
    String roleId = request.getParameter("roleId");
    String roleName = request.getParameter("roleName");
    List rolesList = bo.getAllRoles();
    Preferences prefs = (Preferences) request.getSession(false).getAttribute("preferences");
    request.setAttribute("modules_activated", prefs.getModulesActivatedMap());
    request.setAttribute("rolesList", rolesList);
    request.setAttribute("hRoleId", roleId);
    request.setAttribute("roleName", roleName);
    return mapping.findForward("getRoleScreen");
  }

  /**
   * Creates the role.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws SQLException
   *           the SQL exception
   */
  @IgnoreConfidentialFilters
  public ActionForward createRole(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException {
    String message = null;
    FlashScope flash = FlashScope.getScope(request);

    RoleForm roleForm = (RoleForm) form;
    HttpSession session = request.getSession();
    String userName = (String) session.getAttribute("userid");

    Rights rights = new Rights();
    HashMap<String, Object> screenRights = (HashMap<String, Object>) roleForm.getScreenRightsMap();
    if (screenRights.containsKey("discharge_summary")) {
      screenRights.put("new_discharge_summary", screenRights.get("discharge_summary"));
    }

    if (screenRights.containsKey("new_op_order") || screenRights.containsKey("new_ip_order")) {
      screenRights.put("order", "A");
    }

    rights.setScreenRightsMap(screenRights);
    rights.setActionRightsMap(roleForm.getActionRightsMap());

    logger.debug("Screenrights: " + roleForm.getScreenRightsMap().size());
    logger.debug("Actionrights: " + roleForm.getActionRightsMap().size());

    HashMap urlactionRights = new HashMap();
    Iterator screen = roleForm.getScreenRightsMap().entrySet().iterator();
    while (screen.hasNext()) {
      Map.Entry screenEntry = (Map.Entry) screen.next();
      Map screenActionMap = (Map) servlet.getServletContext().getAttribute("screenActionMap");
      List actions = (List) screenActionMap.get(screenEntry.getKey());
      if (null == actions) {
        // screen is not in the map, which means action id is same as the screen id
        urlactionRights.put(screenEntry.getKey(), screenEntry.getValue());
      } else {
        Iterator it = actions.iterator();
        while (it.hasNext()) {
          String actionId = (String) it.next();
          urlactionRights.put(actionId, screenEntry.getValue());
        }
      }
    }
    rights.setUrlActionRightsMap(urlactionRights);
    String result = null;
    String newRoleName = null;
    String newRoleId = null;
    boolean pageStats = false;
    if (roleForm.getOperation().equalsIgnoreCase("create")) {
      logger.debug("Creating role");
      Role role = new Role();
      role.setName(roleForm.getName().trim());
      role.setRemarks(roleForm.getRemarks());
      role.setStatus("A");
      role.setModUser(userName);

      result = bo.createRole(role, rights);

      if (result.equalsIgnoreCase("success")) {
        newRoleName = role.getName();
        newRoleId = Integer.toString(role.getRoleId());
        message = "Role" + role.getName() + " created successfully ";
        flash.put("success", message);
        pageStats = true;
      } else if (result.equalsIgnoreCase("exist")) {
        message = "This RoleName Already Exists";
        flash.put("error", message);
      } else {
        message = "Role creation failed";
        flash.put("error", message);
      }
    }
    if (roleForm.getOperation().equalsIgnoreCase("edit")) {
      logger.debug("Editing role");
      Role role = new Role();
      role.setName(roleForm.getName());
      role.setRemarks(roleForm.getRemarks());
      role.setStatus(roleForm.getStatus());
      role.setRoleId(Integer.parseInt(roleForm.getRoleId()));
      role.setModUser(userName);
      result = bo.modifyRole(role, rights);

      if (result.equalsIgnoreCase("success")) {
        message = "Role updated successfully";
        flash.put("success", message);
        newRoleName = role.getName();
        newRoleId = Integer.toString(role.getRoleId());
      } else if (result.equalsIgnoreCase("exist")) {
        message = "This RoleName Already Exists";
        flash.put("error", message);
      } else {
        message = "Role modification failed";
        flash.put("error", message);
      }
    }
    ActionRedirect redirect;
    if (pageStats) {
      redirect = new ActionRedirect(mapping.findForward("getScreenRedirect"));
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      redirect.addParameter("roleId", newRoleId);
      redirect.addParameter("roleName", newRoleName);
      redirect.addParameter("newRole", "yes");
    } else {
      redirect = new ActionRedirect(mapping.findForward("listRedirect"));
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      redirect.addParameter("filterClosed", "true");
      redirect.addParameter("sortOrder", "rolename");
      redirect.addParameter("sortReverse", "true");
      redirect.addParameter("hospital", "on");
    }
    return redirect;
  }

  /**
   * returns a JSON string depicting the role's details.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the role details JSON
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ServletException
   *           the servlet exception
   * @throws SQLException
   *           the SQL exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getRoleDetailsJSON(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException, SQLException {

    String roleIdString = request.getParameter("roleId");
    int roleId = Integer.parseInt(roleIdString);
    RoleDetails roleDetails = bo.getDetails(roleId);

    JSONSerializer js = new JSONSerializer();
    String roleJSON = js.exclude("*.class").include("rights.*").deepSerialize(roleDetails);

    response.setContentType("text/plain");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    response.getWriter().write(roleJSON);
    response.flushBuffer();
    return null;
  }
}
