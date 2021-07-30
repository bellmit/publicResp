package com.insta.hms.wardactivities.nursenotes;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.lowagie.text.DocumentException;

import flexjson.JSONSerializer;
import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
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
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

/**
 * The Class NurseNotesAction.
 *
 * @author Nikunj
 */

public class NurseNotesAction extends DispatchAction {

  /** The log. */
  static Logger log = LoggerFactory.getLogger(NurseNotesAction.class);
  
  /** The nurse notes DAO. */
  NurseNotesDAO nurseNotesDAO = new NurseNotesDAO();

  /**
   * Nursenotescreen.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public ActionForward nursenotescreen(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {

    String patientId = request.getParameter("visit_id");
    JSONSerializer js = new JSONSerializer().exclude(".class");
    GenericDAO userDAO = new GenericDAO("u_user");
    if (patientId != null) {
      request.setAttribute("nursenotes", NurseNotesDAO.getNurseNotes(patientId));
    }
    BasicDynaBean userbean = userDAO.findByKey("emp_username", request.getSession(false)
        .getAttribute("userId"));
    if (userbean != null) {
      request.setAttribute("isSharedLogIn", userbean.get("is_shared_login"));
      request.setAttribute("roleId", userbean.get("role_id"));
      request.setAttribute("userBean", js.deepSerialize(userbean.getMap()));
    }
    request.setAttribute("actionId", mapping.getProperty("action_id"));
    return mapping.findForward("nursenotescreen");
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
    String htover = request.getParameter("htover");
    Map params = request.getParameterMap();
    String patientId = request.getParameter("patient_id");
    String authorUser = request.getParameter("authUser");
    Timestamp creationDateTime = DateUtil.parseTimestamp(request.getParameter("entered_date"),
        request.getParameter("entered_time"));
    Boolean success = true;
    Connection con = DataBaseUtil.getConnection();
    con.setAutoCommit(false);
    String error = null;
    List errorFields = new ArrayList();
    try {
      BasicDynaBean bean = nurseNotesDAO.getBean();
      ConversionUtils.copyToDynaBean(params, bean, errorFields);
      if (authorUser != null && !authorUser.isEmpty()) {
        bean.set("mod_user", authorUser);
      }
      bean.set("note_id", nurseNotesDAO.getNextSequence());
      bean.set("mod_time", DateUtil.getCurrentTimestamp());
      bean.set("patient_id", patientId);
      bean.set("creation_datetime", creationDateTime);
      bean.set("note_type", htover);
      if (errorFields.isEmpty()) {
        success = false;
        int noteNum = nurseNotesDAO.getNoteNum(patientId);
        if (noteNum == 0) {
          bean.set("note_num", 1);
        } else {
          bean.set("note_num", noteNum + 1);
        }
        if (!nurseNotesDAO.insert(con, bean)) {
          error = "Failed to insert Nurse's Notes..";
        }
      }
      success = true;
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
    FlashScope flash = FlashScope.getScope(request);
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
    
    Map params = request.getParameterMap();
    String[] noteids = request.getParameterValues("h_note_id");
    Boolean success = true;
    Connection con = DataBaseUtil.getConnection();
    con.setAutoCommit(false);
    String error = null;
    FlashScope flash = FlashScope.getScope(request);
    List errorFields = new ArrayList();
    try {
      if (noteids != null) {
        success = false;
        for (int i = 0; i < noteids.length - 1; i++) {
          BasicDynaBean bean = nurseNotesDAO.getBean();
          ConversionUtils.copyIndexToDynaBeanPrefixed(params, i, bean, errorFields, "h_");
          String delete = (request.getParameterValues("h_delete_note")[i]).toString();
          if (delete.equals("Y")) {
            if (!nurseNotesDAO.delete(con, "note_id", bean.get("note_id"))) {
              error = "Failed to delete Nurse's Notes..";
            }
          }
        }
      }
      success = true;
    } catch (Exception exe) {
      exe.printStackTrace();
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
    flash.put("error", error);
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

    Map params = request.getParameterMap();
    String noteid = request.getParameter("ed_note_id");
    String authorUser = request.getParameter("authUser");
    String htover = request.getParameter("ed_htover");
    FlashScope flash = FlashScope.getScope(request);
    Boolean success = false;
    Connection con = DataBaseUtil.getConnection();
    con.setAutoCommit(false);
    String error = null;
    Timestamp creationDateTimeEdit = DateUtil.parseTimestamp(
        request.getParameter("ed_entered_date"), request.getParameter("ed_entered_time"));
    try {
      if (noteid != null && !noteid.isEmpty()) {
        BasicDynaBean bean = nurseNotesDAO.getBean();
        bean.set("mod_time", DateUtil.getCurrentTimestamp());
        bean.set("creation_datetime", creationDateTimeEdit);
        bean.set("note_type", htover);

        ConversionUtils.copyToDynaBean(params, bean, "ed_");
        if (authorUser != null && !authorUser.isEmpty()) {
          bean.set("mod_user", authorUser);
        }
        if (nurseNotesDAO.update(con, bean.getMap(), "note_id", Integer.parseInt(noteid)) < 1) {
          error = "Failed to delete Nurse's Notes..";
        }
      }
      success = true;
    } catch (Exception exe) {
      exe.printStackTrace();
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
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

    GenericNurseNotesFtlHelper ftlHelper = new GenericNurseNotesFtlHelper(AppInit.getFmConfig());

    if (printMode.equals("P")) {
      response.setContentType("application/pdf");
      OutputStream os = response.getOutputStream();
      ftlHelper.getNureseNotesReport(visitId, GenericNurseNotesFtlHelper.ReturnType.PDF, pref, os);
      os.close();
    } else {
      String textReport = new String(ftlHelper.getNureseNotesReport(visitId,
          GenericNurseNotesFtlHelper.ReturnType.TEXT_BYTES, pref, null));
      request.setAttribute("textReport", textReport);
      request.setAttribute("textColumns", pref.get("text_mode_column"));
      request.setAttribute("printerType", "DMP");
      return mapping.findForward("textPrintApplet");
    }
    return null;
  }

}
