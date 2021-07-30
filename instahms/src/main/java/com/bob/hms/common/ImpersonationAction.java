/**
 *
 */

package com.bob.hms.common;

import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.mdm.usercentercounters.UserBillingCenterCounterMappingService;
import com.insta.hms.stores.StoreDAO;
import com.insta.hms.stores.StoresDashBoardsDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

// TODO: Auto-generated Javadoc
/**
 * The Class ImpersonationAction.
 *
 * @author krishna.t
 */
public class ImpersonationAction extends DispatchAction {

  static Logger logger = LoggerFactory.getLogger(ImpersonationAction.class);
  
  private static UserBillingCenterCounterMappingService counterMappingService = 
      ApplicationContextProvider.getBean(UserBillingCenterCounterMappingService.class);

  /**
   * Change role.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws ServletException the servlet exception
   * @throws IOException      Signals that an I/O exception has occurred.
   * @throws SQLException     the SQL exception
   */
  @IgnoreConfidentialFilters
  public ActionForward changeRole(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException, SQLException {
    HttpSession session = request.getSession(false);

    int roleId = (Integer) session.getAttribute("roleId");
    String roleName = (String) session.getAttribute("roleName");

    String modifiedRoleId = request.getParameter("userRoleId");
    String modifiedRoleName = request.getParameter("roleName");

    Object loggedInRoleIdObj = session.getAttribute("loggedInRoleId");
    Object loggedInRoleNameObj = session.getAttribute("loggedInRoleName");
    int loggedInRoleId = 0;
    String loggedInRoleName = null;

    if (loggedInRoleIdObj == null) {
      loggedInRoleId = roleId;
      loggedInRoleName = roleName;
    } else {
      loggedInRoleId = (Integer) loggedInRoleIdObj;
      loggedInRoleName = (String) loggedInRoleNameObj;
    }

    if (roleId == 1 || roleId == 2 || loggedInRoleId == 1 || loggedInRoleId == 2) {
      session.setAttribute("roleId", Integer.parseInt(modifiedRoleId));
      session.setAttribute("roleName", modifiedRoleName);
    }
    Connection con = DataBaseUtil.getConnection();
    try {
      ScreenRightsHelper.setScreenRights(con, Integer.parseInt(modifiedRoleId));
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
    session.setAttribute("loggedInRoleId", loggedInRoleId);
    session.setAttribute("loggedInRoleName", loggedInRoleName);

    return mapping.findForward("success");
  }

  /**
   * Change center.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   */
  @IgnoreConfidentialFilters
  public ActionForward changeCenter(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException {
    HttpSession session = request.getSession(false);

    String userId = (String) session.getAttribute("userId");
    int centerId = (Integer) session.getAttribute("centerId");
    String centerName = (String) session.getAttribute("centerName");

    int modifiedCenterId = Integer.parseInt(request.getParameter("modifiedCenterId"));
    String modifiedCenterName = request.getParameter("modifiedCenterName");

    Object loggedInCenterIdObj = session.getAttribute("loggedInCenterId");
    Object loggedInCenterNameObj = session.getAttribute("loggedInCenterName");
    int loggedInCenterId = 0;
    String loggedInCenterName = null;

    if (loggedInCenterIdObj == null) {
      loggedInCenterId = centerId;
      loggedInCenterName = centerName;
    } else {
      loggedInCenterId = (Integer) loggedInCenterIdObj;
      loggedInCenterName = (String) loggedInCenterNameObj;
    }

    FlashScope flash = FlashScope.getScope(request);
    if (centerId == 0 || loggedInCenterId == 0) {
      session.setAttribute("centerId", modifiedCenterId);
      session.setAttribute("centerName", modifiedCenterName);
      GenericDAO userdao = new GenericDAO("u_user");
      BasicDynaBean userBean = userdao.findByKey("emp_username", userId);
      String defaultPharmStoreId = (String) userBean.get("pharmacy_store_id");
      // check default pharmacy store also existing in the modified center if modified center is not
      // a super center.
      if (defaultPharmStoreId != null && !defaultPharmStoreId.equals("") && modifiedCenterId != 0
          && !StoreDAO.existsInCenter(Integer.parseInt(defaultPharmStoreId), modifiedCenterId)) {
        // Check schema is multi center and user center_id is 0
        int maxCentersIncDefault = (Integer) GenericPreferencesDAO.getAllPrefs()
            .get("max_centers_inc_default");

        /*
         * default center user may not have a particular default store. But user screen mandates to
         * pick one default store(that can be of another center) Hence on impersonation we pick one
         * random default store which belongs to this center. And henceforth this store acts as his
         * default store.
         */

        if (maxCentersIncDefault > 1 && (Integer) userBean.get("center_id") == 0) {
          String multiStore = (String) userBean.get("multi_store");
          String defaultStoreOfAssignedStores = multiStore == null ? null
              : new StoresDashBoardsDAO().getAccessableStoresOfCenter(modifiedCenterId, multiStore);

          if (defaultStoreOfAssignedStores != null) {
            session.setAttribute("pharmacyStoreId", defaultStoreOfAssignedStores);
          } else {
            flash.put("error",
                "Default Pharamcy Store notassignedstoresOfThisCenter exists in this center.");
            session.setAttribute("pharmacyStoreId", null);
          }
        } else {
          flash.put("error", "Default Pharamcy Store not exists in this center.");
          session.setAttribute("pharmacyStoreId", null);
        }

      } else {
        // restore back the original pharmacy store id
        session.setAttribute("pharmacyStoreId", defaultPharmStoreId);
      }

      String multiStoreStr = (String) userBean.get("multi_store");
      String multiStoreAccess;
      if (multiStoreStr == null || multiStoreStr.trim().equals("")) {
        multiStoreAccess = "N";
      } else {
        if (multiStoreStr.contains(",")) {
          multiStoreAccess = "A";
        } else {
          multiStoreAccess = "N";
        }
      }
      session.setAttribute("multiStoreAccess", multiStoreAccess);

      Connection con = null;
      try {
        con = DataBaseUtil.getConnection();
        int roleId = (Integer) session.getAttribute("roleId");
        LoginAction.setPreferences(request, con);
        ScreenRightsHelper.setScreenRights(con, roleId);
        BasicDynaBean centerBean = new CenterMasterDAO().findByKey("center_id", modifiedCenterId);
        LoginAction.setCenterCredentialsInSession(session, centerBean);
      } catch (SQLException exception) {
        logger.error("Exception Raised in Impersonation Action", exception);
        session.removeAttribute("sesHospitalId");
        session.removeAttribute("userid");
        session.removeAttribute("userId");
        session.removeAttribute("loginCenterHealthAuthority");
        session.removeAttribute("shafafiya_user");
        session.removeAttribute("shafafiya_preauth_user");
        session.removeAttribute("dhpo_facility_user");
        throw (exception);

      } finally {
        DataBaseUtil.closeConnections(con, null);
      }
    }
    BasicDynaBean mappedCounterBean = counterMappingService.getMappedCounterForCenter(userId,
        modifiedCenterId);
    if (null != mappedCounterBean) {
      session.setAttribute("billingcounterId", (String) mappedCounterBean.get("counter_id"));
      session.setAttribute("billingcounterName", (String) mappedCounterBean.get("counter_no"));
    } else {
      session.setAttribute("billingcounterId", null);
      session.setAttribute("billingcounterName", null);
    }
    session.setAttribute("loggedInCenterId", loggedInCenterId);
    session.setAttribute("loggedInCenterName", loggedInCenterName);
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("success"));
    String previousUrl = request.getParameter("currentURL");
    if (previousUrl != null && (previousUrl.contains("/resources/scheduler/index.htm")
        || previousUrl.contains("/resourcescheduler/calendarview/index.htm"))) {
      redirect = new ActionRedirect(previousUrl);
    }
    return redirect;
  }

}
