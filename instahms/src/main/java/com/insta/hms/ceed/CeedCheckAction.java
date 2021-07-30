package com.insta.hms.ceed;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.FlashScope;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CeedCheckAction extends BaseAction {

  Logger logger = LoggerFactory.getLogger(CeedCheckAction.class);

  /**
   * Send ceed request, gets response and redirect to consultation and management page.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the package list screen
   * @throws SQLException the SQL exception
   * @throws Exception    the exception
   */
  public ActionRedirect sendCeedRequest(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, Exception {

    ActionRedirect redirect = new ActionRedirect(mapping.findForward("OpPrescribeListRedirect"));

    // get request parameters
    String conId = request.getParameter("consultation_id");
    String patientId = request.getParameter("visit_id");

    // add parameters to redirect
    redirect.addParameter("consultation_id", conId);
    redirect.addParameter("visit_id", patientId);
    Connection con = DataBaseUtil.getConnection();
    con.setAutoCommit(false);
    boolean status = false;
    String error = null;
    try {
      AbstractCeedCheck consceedcheck = new ConsultationCeedCheck();
      error = consceedcheck.performCeedCheck(con, conId, patientId, "2");
      status = error == null;
    } finally {
      DataBaseUtil.commitClose(con, status);
      if (error != null) {
        FlashScope flash = FlashScope.getScope(request);
        flash.error(error);
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      }
    }
    return redirect;
  }
}
