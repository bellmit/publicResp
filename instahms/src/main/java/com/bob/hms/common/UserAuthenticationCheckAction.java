/**
 *
 */

package com.bob.hms.common;

import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.usermanager.PasswordEncoder;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

// TODO: Auto-generated Javadoc
/**
 * The Class UserAuthenticationCheckAction.
 *
 * @author nikunj.s
 */
public class UserAuthenticationCheckAction extends DispatchAction {

  /**
   * Checks if is authenticated user and has access.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws SQLException             the SQL exception
   * @throws ServletException         the servlet exception
   * @throws IOException              Signals that an I/O exception has occurred.
   * @throws NoSuchAlgorithmException the no such algorithm exception
   */
  @IgnoreConfidentialFilters
  public ActionForward isAuthenticatedUserAndHasAccess(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws SQLException, ServletException, IOException, NoSuchAlgorithmException {

    response.setContentType("text/plain");
    response.setHeader("Cache-Control", "no-cache");
    HttpSession session = request.getSession(false);
    Integer centerId = (Integer) session.getAttribute("centerId");
    String actionId = request.getParameter("action_id");
    String password = request.getParameter("login_password");
    String user = request.getParameter("login_user");
    BasicDynaBean bean = new GenericDAO("u_user").findByKey("emp_username", user);

    BasicDynaBean userBean = null;
    if (bean != null) {
      if (!(Boolean) bean.get("is_encrypted")) {
        userBean = new LoginDao().isValidUserAndHasAccess(request.getParameter("login_user"),
            actionId, password);
      } else {
        userBean = new LoginDao().isValidUserAndHasAccess(request.getParameter("login_user"),
            actionId, null);
        if (!PasswordEncoder.matches(password, (String) userBean.get("emp_password"), userBean)) {
          userBean = null;
        }
      }
    }

    String result = "";
    if (null != userBean) {
      // The data type for role_id is numeric(5,0) instead of integer. Hence we get a BigDecimal
      // instead of an Integer in the DynaBean.

      // A -- Allowed User
      // U -- UnAuthorized User
      // S -- Shared Login User
      // N -- Non Center user
      // I -- Invalid User
      Integer roleId = ((BigDecimal) userBean.get("role_id")).intValue();
      if ((roleId == 1 || roleId == 2) && userBean.get("is_shared_login").equals("N")) {
        result = "A";
      } else {
        if (centerId != null && centerId.equals(userBean.get("center_id"))) {
          if (userBean.get("is_shared_login").equals("N")) {
            String rights = (String) userBean.get("rights");
            rights = rights == null ? "" : rights;
            result = rights.equals("A") ? "A" : "U";
          } else {
            result = "S";
          }
        } else {
          result = "N";
        }
      }
    } else {
      result = "I";
    }

    response.getWriter().write(result);
    response.flushBuffer();
    return null;
  }

}
