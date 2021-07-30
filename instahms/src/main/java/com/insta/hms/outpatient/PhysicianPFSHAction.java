package com.insta.hms.outpatient;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class PhysicianPFSHAction.
 */
public class PhysicianPFSHAction extends BaseAction {

  /** The pfsh DAO. */
  static GenericDAO pfshDAO = new GenericDAO("patient_pfsh");

  /**
   * Show.
   *
   * @param mapping the mapping
   * @param form    the form
   * @param req     the req
   * @param res     the res
   * @return the action forward
   * @throws SQLException the SQL exception
   */
  public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws SQLException {

    // key parameter: consultation ID
    String consIdStr = req.getParameter("consultation_id");
    int consId = Integer.parseInt(consIdStr);
    BasicDynaBean cons = new DoctorConsultationDAO().findConsultationExt(consId);
    req.setAttribute("cons", cons.getMap());

    return mapping.findForward("show");
  }

  /*
   * click Save in the physician form screen, any of HPI, ROS and Examination
   */
  /**
   * Update.
   *
   * @param mapping the mapping
   * @param form    the form
   * @param req     the req
   * @param res     the res
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws SQLException, IOException {

    String consIdStr = req.getParameter("consultation_id");

    Connection con = DataBaseUtil.getConnection();
    boolean success = false;
    try {
      con.setAutoCommit(false);
    } finally {
      DataBaseUtil.commitClose(con, success);
    }

    FlashScope flash = FlashScope.getScope(req);
    flash.put("info", "Information saved");
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
    redirect.addParameter("consultation_id", consIdStr);
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

    return redirect;
  }
}
