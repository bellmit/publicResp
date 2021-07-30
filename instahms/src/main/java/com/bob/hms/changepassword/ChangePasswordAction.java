
package com.bob.hms.changepassword;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.usermanager.PasswordEncoder;
import com.insta.hms.usermanager.UserAction;
import com.insta.hms.usermanager.UserDAO;

import flexjson.JSONSerializer;


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
import java.util.HashMap;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class ChangePasswordAction extends DispatchAction {
  JSONSerializer js = new JSONSerializer().exclude("class");
  GenericDAO passwordRulesDao = new GenericDAO("password_rule");
  private static GenericDAO uUserDao = new GenericDAO("u_user");

  Logger logger = LoggerFactory.getLogger(ChangePasswordAction.class);

  /**
   * Change password.
   *
   * @param am the am
   * @param af the af
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws ServletException the servlet exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   */
  @IgnoreConfidentialFilters
  public ActionForward changePassword(ActionMapping am, ActionForm af, HttpServletRequest req,
      HttpServletResponse res) throws ServletException, IOException, SQLException {
    logger.debug("In action-----------------------------------");
    HttpSession session = req.getSession(false);
    Object userid = session.getAttribute("userid");
    if (userid == null) {
      return am.findForward("login");
    } else {
      req.setAttribute("passwordRules",
          js.serialize(ConversionUtils.copyListDynaBeansToMap(passwordRulesDao.listAll())));
      return am.findForward("gotoScreen");
    }
  }

  /**
   * Update password.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws ServletException the servlet exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @IgnoreConfidentialFilters
  public ActionForward updatePassword(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    HttpSession session = request.getSession(false);
    Object userid = session.getAttribute("userid");
    Object tempUserId = session.getAttribute("tempUserId");
    Object userID = userid == null ? tempUserId : userid;
    String newPassword = request.getParameter("pwd");
    String encryptedNewPassword = null;
    String success = "success";
    FlashScope flash = FlashScope.getScope(request);
    boolean succ = false;
    ActionRedirect redirect = null;

    if (userid == null && tempUserId == null) {
      return mapping.findForward("login");
    } else {
      String msg = "";
      try {
        /* Populating the form bean */
        ChangePasswordForm seform = (ChangePasswordForm) form;
        BasicDynaBean userBean = UserDAO.getRecord((String) userID);
        String encryptedOldPassword = (String) userBean.get("emp_password");
        boolean isEncrypted = (Boolean) userBean.get("is_encrypted");
        String plainOldPassword = seform.getOldpwd();
        if (isEncrypted) {
          encryptedNewPassword = PasswordEncoder.encode(newPassword);
        } else {
          encryptedNewPassword = newPassword;
        }

        if (!PasswordEncoder.matches(plainOldPassword, encryptedOldPassword, userBean)) {
          if (userid == null) {
            flash.put("login_status", "errorInUpdatePassword");
            flash.put("msg", "Incorrect old password.");
            session.setAttribute("passwordRules",
                    js.serialize(ConversionUtils
                            .copyListDynaBeansToMap(passwordRulesDao.listAll())));
            redirect = new ActionRedirect(mapping.findForward("loginScreen"));
            redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
            return redirect;
          }
          request.setAttribute("msg", "Incorrect old password.");
          request.setAttribute("passwordRules",
                  js.serialize(ConversionUtils.copyListDynaBeansToMap(passwordRulesDao.listAll())));
          return mapping.findForward( "gotoScreen");
        }

        if (userid == null) {
          seform.setPwd(encryptedNewPassword);
          seform.setUid((String) userID);
          seform.setOldpwd(encryptedOldPassword);
        }
        
        String password = seform.getPwd();
        UserAction user = new UserAction();

        msg = user.checkPasswordStrength(newPassword);
        if (msg == null) {
          msg = user.checkPasswordFrequency(newPassword, (String) userID);
        }

        if (msg == null) {
          ChangePasswordDaoInterface intf = ChangePasswordDaoFactory.getChangePasswordDao();
          seform.setOldpwd(encryptedOldPassword);
          seform.setPwd(encryptedNewPassword);
          String result = intf.changePassword(seform);
          if (result.equalsIgnoreCase("success")) {
            msg = "Password successfully changed";
            user.updatePasswordHistory((String) userID, encryptedOldPassword);
            succ = true;
          } else {
            logger.debug("In else block");
            msg = "Failed to change Password";
          }
        } else {
          msg = "Failed to change Password. " + msg;
        }
        success = userid == null ? "loginScreen" : "gotoScreen";
        if (userid == null) {
          if (succ) {
            Connection con = DataBaseUtil.getConnection();
            BasicDynaBean ubean = uUserDao.findByKey("emp_username", userID);
            ubean.set("force_password_change", false);
            HashMap<String, Object> keys = new HashMap<String, Object>();
            keys.put("emp_username", userID);
            uUserDao.update(con,ubean.getMap(), keys);
            DataBaseUtil.closeConnections(con, null);
            flash.put("login_status", msg + ". Please login with your new password.");
          } else {
            flash.put("login_status", "errorInUpdatePassword");
          }
          flash.put("msg", msg);
          redirect = new ActionRedirect(mapping.findForward("loginScreen"));
          redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
        } else {
          request.setAttribute("passwordRules",
              js.serialize(ConversionUtils.copyListDynaBeansToMap(passwordRulesDao.listAll())));
          request.setAttribute("msg", msg);
        }
      } catch (Exception ex) {
        logger.error("Exception Occured in PartPaymentsAction", ex);
      }
      /* forwarding control to the master jsp */
      if (userid == null) {
        return redirect;
      } else {
        return mapping.findForward(success);
      }
    }
  }

}