package com.insta.hms.patientcustomfields;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDAO;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * The Class PatientCustomFieldsAction.
 */
public class PatientCustomFieldsAction extends DispatchAction {

  /**
   * Show custom fields.
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
  public ActionForward showCustomFields(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException {
    JSONSerializer js = new JSONSerializer().exclude("class");

    String mrno = request.getParameter("mr_no");
    String patientId = request.getParameter("patient_id");
    String visitType = request.getParameter("visit_type");
    request.setAttribute("regPref", RegistrationPreferencesDAO.getRegistrationPreferences());
    request.setAttribute("regPrefJSON",
        js.serialize(RegistrationPreferencesDAO.getRegistrationPreferences()));
    request.setAttribute("patientCustomFields",
        PatientCustomFieldsDAO.getPatientVisitDetailsBean(mrno, patientId));
    request.setAttribute("patient_id", patientId);
    request.setAttribute("mr_no", mrno);
    request.setAttribute("visit_type", visitType);
    return mapping.findForward("showcustomfields");
  }

  /**
   * Save custom fields.
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
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public ActionForward saveCustomFields(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {

    GenericDAO patientDetailsDAO = new GenericDAO("patient_details");
    GenericDAO patientRegistrationDAO = new GenericDAO("patient_registration");
    BasicDynaBean patientDetailsBean = patientDetailsDAO.getBean();
    BasicDynaBean patientRegistrationBean = patientRegistrationDAO.getBean();
    String mrno = request.getParameter("mr_no");
    ActionRedirect redirect = 
        new ActionRedirect(mapping.findForward("patientcustomfieldsRedirect"));

    ConversionUtils.copyToDynaBean(request.getParameterMap(), patientDetailsBean);
    ConversionUtils.copyToDynaBean(request.getParameterMap(), patientRegistrationBean);
    patientDetailsBean.set("mr_no", mrno);
    String patientId = request.getParameter("patient_id");
    patientRegistrationBean.set("patient_id", patientId);
    HttpSession session = request.getSession();
    String userid = (String) session.getAttribute("userid");
    patientRegistrationBean.set("user_name", userid);
    boolean success = false;
    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      success = patientDetailsDAO.updateWithName(con, patientDetailsBean.getMap(), "mr_no") > 0;
      if (patientId != null) {
        success &= patientRegistrationDAO.updateWithName(con, patientRegistrationBean.getMap(),
            "patient_id") > 0;
      }
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    if (patientId != null) {
      redirect.addParameter("patient_id", patientId);
    }
    redirect.addParameter("mr_no", mrno);
    String visitType = request.getParameter("visit_type");
    redirect.addParameter("visit_type", visitType);
    return redirect;
  }
}
