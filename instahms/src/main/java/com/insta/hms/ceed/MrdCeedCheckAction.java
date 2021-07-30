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

/*
 * This is action class for ceed integration on mrd codification screen.
 */
public class MrdCeedCheckAction extends BaseAction {

  Logger logger = LoggerFactory.getLogger(CeedCheckAction.class);

  /**
   * Send CEED Request.
   * 
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return view post CEED request
   * @throws SQLException the SQL exception
   * @throws Exception    the exception
   */
  public ActionRedirect sendCeedRequest(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, Exception {

    ActionRedirect redirect = new ActionRedirect(mapping.findForward("getMRDUpdateDetails"));

    // get request parameters
    String drgChargeId = request.getParameter("drg_charge_id");
    String patientId = request.getParameter("patient_id");
    String ceedCheckType = request.getParameter("ceed_check_type");

    // add parameters to redirect
    redirect.addParameter("drg_charge_id", drgChargeId);
    redirect.addParameter("patient_id", patientId);
    Connection con = DataBaseUtil.getConnection();
    con.setAutoCommit(false);
    boolean status = false;
    String error = null;
    try {
      AbstractCeedCheck mrdceedcheck = new MRDCeedCheck();
      error = mrdceedcheck.performCeedCheck(con, null, patientId, ceedCheckType);
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
