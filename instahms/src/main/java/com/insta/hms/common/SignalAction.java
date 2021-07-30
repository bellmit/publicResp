package com.insta.hms.common;

import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.SystemMessageMaster.SystemMessagesDAO;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class SignalAction. Action that is allowed for internal system signals that can be sent from
 * other programs to us. Session check is not done for this action, so you can call these without
 * having to log in. But SessionCheckFilter ensured that this is allowed only from 127.0.0.1, so
 * external clients cannot really use this.
 * One of the current uses is to refresh the system messages (if they have been modified by cron
 * jobs etc., this needs to be done).
 */
public class SignalAction extends DispatchAction {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(SignalAction.class);

  /**
   * Refresh system messages.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @IgnoreConfidentialFilters
  public ActionForward refreshSystemMessages(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws IOException {

    response.setContentType("text/plain");

    String schema = request.getParameter("schema");
    if ((schema == null) || schema.equals("")) {
      response.getWriter().write("Missing parameter: schema\n");
      return null;
    }
    request.getSession().setAttribute("sesHospitalId", schema);
    SystemMessagesDAO dao = new SystemMessagesDAO();
    dao.clearCache();
    request.getSession().setAttribute("sesHospitalId", null);

    response.getWriter().write("Success\n");
    return null;
  }
}
