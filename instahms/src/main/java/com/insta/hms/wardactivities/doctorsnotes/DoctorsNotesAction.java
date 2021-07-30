package com.insta.hms.wardactivities.doctorsnotes;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.billing.BillDAO;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.insurance.SponsorBO;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.IPPreferences.IPPreferencesDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.orders.ConsultationTypesDAO;
import com.lowagie.text.DocumentException;

import flexjson.JSONSerializer;
import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

/**
 * The Class DoctorsNotesAction.
 *
 * @author Nikunj
 */

public class DoctorsNotesAction extends DispatchAction {

  /** The log. */
  static Logger log = LoggerFactory.getLogger(DoctorsNotesAction.class);
  
  /** The dr notes DAO. */
  private final DoctorsNotesDAO drNotesDAO = new DoctorsNotesDAO();

  /**
   * Doctorsnotescreen.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ParseException the parse exception
   */
  public ActionForward doctorsnotescreen(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException,
      ParseException {
    String patientId = request.getParameter("visit_id");
    JSONSerializer js = new JSONSerializer().exclude(".class");
    GenericDAO userDAO = new GenericDAO("u_user");
    List doctors = Collections.EMPTY_LIST;

    if (patientId != null) {
      List<BasicDynaBean> doctorsNotes = DoctorsNotesDAO.getDoctorsNotes(patientId);
      request.setAttribute("doctornotes", doctorsNotes);
      doctors = ConversionUtils.copyListDynaBeansToMap(DoctorMasterDAO.getAllActiveDoctors());
      BasicDynaBean visitBean = new GenericDAO("patient_registration").findByKey("patient_id",
          patientId);
      int centerId = (Integer) visitBean.get("center_id");
      String healthAuthority = CenterMasterDAO.getHealthAuthorityForCenter(centerId);
      request.setAttribute("consultationTypes", ConsultationTypesDAO.getConsultationTypes("i",
          (String) visitBean.get("org_id"), healthAuthority));
    }
    BasicDynaBean userbean = userDAO.findByKey("emp_username", request.getSession(false)
        .getAttribute("userId"));
    if (userbean != null) {
      request.setAttribute("isSharedLogIn", userbean.get("is_shared_login"));
      request.setAttribute("roleId", userbean.get("role_id"));
      request.setAttribute("userBean", js.deepSerialize(userbean.getMap()));
    }
    // Preferences
    Map prefs = new IPPreferencesDAO().getPreferences().getMap();
    request.setAttribute("ip_preferences", prefs);
    request.setAttribute("ipPrefsJSON", js.serialize(prefs));
    BasicDynaBean genericPrefs = GenericPreferencesDAO.getAllPrefs();
    request.setAttribute("doctors", js.deepSerialize(doctors));
    request.setAttribute("actionId", mapping.getProperty("action_id"));
    return mapping.findForward("doctorsnotescreen");
  }

  /**
   * Save.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws Exception the exception
   */
  public ActionForward save(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
    FlashScope flash = FlashScope.getScope(request);
    HttpSession session = request.getSession(false);
    String userName = (String) session.getAttribute("userId");
    String remoteAddr = request.getRemoteAddr();
    String actionId = (String) mapping.getProperty("action_id");
    Map params = request.getParameterMap();
    String patientId = request.getParameter("patient_id");
    String doctorId = request.getParameter("doctor_id");
    String authorUser = request.getParameter("authUser");
    Timestamp creationDateTime = DateUtil.parseTimestamp(request.getParameter("entered_date"),
        request.getParameter("entered_time"));
    Boolean success = true;
    Connection con = DataBaseUtil.getConnection();
    con.setAutoCommit(false);
    String error = null;
    List errorFields = new ArrayList();
    String billNo = null;
    try {
      BasicDynaBean bean = drNotesDAO.getBean();
      ConversionUtils.copyToDynaBean(params, bean, errorFields);
      if (authorUser != null && !authorUser.isEmpty()) {
        bean.set("mod_user", authorUser);
      }
      bean.set("note_id", drNotesDAO.getNextSequence());
      bean.set("mod_time", DateUtil.getCurrentTimestamp());
      bean.set("patient_id", patientId);
      bean.set("creation_datetime", creationDateTime);
      if (errorFields.isEmpty()) {
        success = false;
        if (bean.get("billable_consultation").equals("Y")) {
          // insert in bill_charge
          int consultationtypeid = Integer.parseInt(request.getParameter("consultation_type_id"));
          Map map = drNotesDAO.setDoctorsCharge(con, patientId, doctorId, userName,
              consultationtypeid);
          String checkForInactive = (String) map.get("checkForInactive");
          if (checkForInactive != null) {
            error = checkForInactive;
            flash.put("error", error);
            redirect.addParameter("visit_id", request.getParameter("patient_id"));
            redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
            return redirect;
          }
          if (!(Boolean) map.get("status")) {
            error = "Failed to insert Doctors Charges";
          }
          String chargeId = (String) map.get("charge_id");
          billNo = (String) map.get("bill_no");
          bean.set("charge_id", chargeId);
        } else {
          bean.set("consultation_type_id", null);
        }
        int noteNum = drNotesDAO.getNoteNum(patientId);
        if (noteNum == 0) {
          bean.set("note_num", 1);
        } else {
          bean.set("note_num", noteNum + 1);
        }
        if (!drNotesDAO.insert(con, bean)) {
          error = "Failed to insert Doctors Notes..";
        }
      }
      success = true;
    } finally {
      DataBaseUtil.commitClose(con, success);
    }

    if (success && null != billNo && "Y".equals(params.get("billable_consultation"))) {
      BasicDynaBean bill = BillDAO.getBillBean(billNo);
      if ((Boolean) bill.get("is_tpa")) {
        new SponsorBO().recalculateSponsorAmount(patientId);
        BillDAO.resetSponsorTotals(billNo);
      }
    }

    flash.put("error", error);
    redirect.addParameter("visit_id", request.getParameter("patient_id"));
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    return redirect;
  }

  /**
   * Delete.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws ParseException the parse exception
   */
  public ActionForward delete(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, IOException, ServletException,
      ParseException {

    HttpSession session = request.getSession(false);
    String userName = (String) session.getAttribute("userId");
    String remoteAddr = request.getRemoteAddr();
    String actionId = (String) mapping.getProperty("action_id");
    Map params = request.getParameterMap();
    String[] noteids = request.getParameterValues("h_note_id");
    FlashScope flash = FlashScope.getScope(request);
    Boolean success = true;
    Connection con = DataBaseUtil.getConnection();
    con.setAutoCommit(false);
    String error = null;
    List errorFields = new ArrayList();
    try {
      if (noteids != null) {
        success = false;
        for (int i = 0; i < noteids.length - 1; i++) {
          BasicDynaBean bean = drNotesDAO.getBean();
          ConversionUtils.copyIndexToDynaBeanPrefixed(params, i, bean, errorFields, "h_");
          String delete = (request.getParameterValues("h_delete_note")[i]).toString();
          if (delete.equals("Y")) {
            if (!drNotesDAO.delete(con, "note_id", bean.get("note_id"))) {
              error = "Failed to delete the prescriptions..";
            }
          }
        }
      }
      success = true;
    } catch (Exception exe) {
      log.error(exe.toString());
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    flash.put("error", error);
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
    redirect.addParameter("visit_id", request.getParameter("patient_id"));
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    return redirect;
  }

  /**
   * Update.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws ParseException the parse exception
   */
  public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, IOException, ServletException,
      ParseException {
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));

    Map params = request.getParameterMap();
    String noteid = request.getParameter("ed_note_id");
    String patientId = request.getParameter("patient_id");
    HttpSession session = request.getSession(false);
    String userName = (String) session.getAttribute("userId");
    String remoteAddr = request.getRemoteAddr();
    String actionId = (String) mapping.getProperty("action_id");
    String authorUser = request.getParameter("authUser");
    FlashScope flash = FlashScope.getScope(request);
    Boolean success = false;
    Connection con = DataBaseUtil.getConnection();
    con.setAutoCommit(false);
    String error = null;
    Timestamp creationDateTimeEdit = DateUtil.parseTimestamp(
        request.getParameter("ed_entered_date"), request.getParameter("ed_entered_time"));
    try {
      if (noteid != null && !noteid.isEmpty()) {
        BasicDynaBean bean = drNotesDAO.getBean();
        bean.set("mod_time", DateUtil.getCurrentTimestamp());
        bean.set("creation_datetime", creationDateTimeEdit);
        ConversionUtils.copyToDynaBean(params, bean, "ed_");
        if (authorUser != null && !authorUser.isEmpty()) {
          bean.set("mod_user", authorUser);
        }
        if (bean.get("billable_consultation").equals("Y")
            && bean.get("consultation_type_id") != null) {
          // insert in bill_charge
          String doctorId = (String) bean.get("doctor_id");
          int consultationtypeid = (Integer) bean.get("consultation_type_id");
          Map map = drNotesDAO.setDoctorsCharge(con, patientId, doctorId, userName,
              consultationtypeid);
          String checkForInactive = (String) map.get("checkForInactive");
          if (checkForInactive != null) {
            error = checkForInactive;
            flash.put("error", error);
            redirect.addParameter("visit_id", request.getParameter("patient_id"));
            redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
            return redirect;
          }
          if (!(Boolean) map.get("status")) {
            error = "Failed to insert Doctors Charges";
          }
          String chargeId = (String) map.get("charge_id");
          bean.set("charge_id", chargeId);
        } else {
          String consultationtypeid = request.getParameter("consultationTypeId");
          if (consultationtypeid != null && !consultationtypeid.isEmpty()) {
            bean.set("consultation_type_id", Integer.valueOf(consultationtypeid));
          } else {
            bean.set("consultation_type_id", null);
          }
        }
        if (drNotesDAO.update(con, bean.getMap(), "note_id", Integer.parseInt(noteid)) < 1) {
          error = "Failed to delete the prescriptions..";
        }
      }
      success = true;
    } catch (Exception exe) {
      log.error(exe.toString());
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    flash.put("error", error);
    redirect.addParameter("visit_id", request.getParameter("patient_id"));
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    return redirect;
  }

  /**
   * Generate report.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws DocumentException the document exception
   * @throws TemplateException the template exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws XPathExpressionException the x path expression exception
   * @throws TransformerException the transformer exception
   */
  public ActionForward generateReport(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException,
      DocumentException, TemplateException, IOException, XPathExpressionException,
      TransformerException {

    String visitId = request.getParameter("patient_id");
    BasicDynaBean pref = PrintConfigurationsDAO.getPageOptions(
        PrintConfigurationsDAO.PRINT_TYPE_PATIENT, 0);
    String printMode = "P";
    if (pref.get("print_mode") != null) {
      printMode = (String) pref.get("print_mode");
    }

    GenericDoctorNotesFtlHelper ftlHelper = new GenericDoctorNotesFtlHelper(AppInit.getFmConfig());

    if (printMode.equals("P")) {
      response.setContentType("application/pdf");
      OutputStream os = response.getOutputStream();
      ftlHelper.getDoctorsNotesReport(visitId, GenericDoctorNotesFtlHelper.ReturnType.PDF, pref,
          os);
      os.close();
    } else {
      String textReport = new String(ftlHelper.getDoctorsNotesReport(visitId,
          GenericDoctorNotesFtlHelper.ReturnType.TEXT_BYTES, pref, null));
      request.setAttribute("textReport", textReport);
      request.setAttribute("textColumns", pref.get("text_mode_column"));
      request.setAttribute("printerType", "DMP");
      return mapping.findForward("textPrintApplet");
    }
    return null;
  }

  /**
   * Gets the billable consultation count for day.
   *
   * @param am the am
   * @param af the af
   * @param req the req
   * @param res the res
   * @return the billable consultation count for day
   * @throws Exception the exception
   */
  public ActionForward getBillableConsultationCountForDay(ActionMapping am, ActionForm af,
      HttpServletRequest req, HttpServletResponse res) throws Exception {

    String patientId = req.getParameter("patient_id");
    String date = req.getParameter("creation_datetime");
    String doctorId = req.getParameter("doctor_id");
    String responseContent = new String(DoctorsNotesDAO.getBillableNotesForDay(patientId, doctorId,
        new Date(DateUtil.parseDate(date).getTime())).toString());
    res.setContentType("application/json");
    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    res.getWriter().write(responseContent);
    res.flushBuffer();
    return null;
  }

}
