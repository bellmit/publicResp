package com.bob.hms.common;

import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.core.clinical.ipemr.IpEmrFormService;
import com.insta.hms.mdm.breaktheglass.UserMrnoAssociationService;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * The Class logoutAction.
 */
public class LogoutAction extends Action {

  static Logger logger = LoggerFactory.getLogger(LogoutAction.class);
  static UserMrnoAssociationService userMrnoAssociationService = ApplicationContextProvider
      .getBean(UserMrnoAssociationService.class);
  static IpEmrFormService ipEmrService =
      ApplicationContextProvider.getBean(IpEmrFormService.class);

  @Override
  public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws IOException, ServletException, SQLException {

    HttpSession session = request.getSession(false);

    if (session != null) {
      String username = (String) session.getAttribute("userid");
      String hospital = (String) session.getAttribute("sesHospitalId");
      if (username != null) {
        userMrnoAssociationService.deleteUserMrNoAssociations(username);
        ipEmrService.deleteSectionLock(username);
      }
      session.invalidate();

      if (username != null) {
        logger.info("Logout success, user=" + username + "@" + hospital);
      }
    }

    return mapping.findForward("logout");
  }
}
