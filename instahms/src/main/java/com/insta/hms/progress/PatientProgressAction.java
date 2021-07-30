package com.insta.hms.progress;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO: Auto-generated Javadoc
/**
 * The Class PatientProgressAction.
 */
public class PatientProgressAction extends DispatchAction {

  /**
   * Show.
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
   * @throws Exception
   *           the exception
   */
  public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, Exception {

    String mrNo = request.getParameter("mr_no");
    if ((mrNo != null) && !mrNo.equals("")) {
      Map patmap = PatientDetailsDAO.getPatientGeneralDetailsMap(mrNo);
      if (patmap == null) {
        FlashScope flash = FlashScope.getScope(request);
        flash.put("error", mrNo + " doesn't exists.");
        ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
        return redirect;
      }
    }
    String visitId = request.getParameter("filtervisitId");
    String filterType = request.getParameter("filterType");
    request.setAttribute("mrNo", mrNo);
    List<BasicDynaBean> list = new ArrayList<BasicDynaBean>();
    list = PatientProgressDAO.getProgressNotesList(mrNo, filterType, visitId);

    request.setAttribute("progressNtsList", ConversionUtils.listBeanToListMap(list));
    request.setAttribute("visitsList", PatientProgressDAO.getCenterVisits(mrNo));
    request.setAttribute("filterType", filterType);
    request.setAttribute("filtervisitId", visitId);

    return mapping.findForward("show");
  }

  /**
   * Save progress notes.
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
   * @throws Exception
   *           the exception
   */
  public ActionForward saveProgressNotes(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, Exception {

    FlashScope flash = FlashScope.getScope(request);
    String mrNo = request.getParameter("mr_no");
    String progressNtsTo = request.getParameter("progressNotesTO");
    String visitId = request.getParameter("visitId");
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    redirect.addParameter("mr_no", mrNo);

    GenericDAO dao = new GenericDAO("progress_notes");
    BasicDynaBean bean = dao.getBean();
    String username = (String) request.getSession(false).getAttribute("userid");
    Connection con = null;
    Map map = request.getParameterMap();
    ArrayList errorFields = new ArrayList();
    String progressId = request.getParameter("progress_notes_id");
    boolean success = false;

    Map<String, Object> keys = new HashMap<String, Object>();

    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      ConversionUtils.copyToDynaBean(map, bean, errorFields);
      bean.set("username", username);
      bean.set("mod_time", new java.sql.Timestamp(new java.util.Date().getTime()));
      if (progressNtsTo.equals("visit")) {
        bean.set("visit_id", visitId);
      } else {
        bean.set("visit_id", "");
      }
      if (progressId == null || progressId.equals("")) {
        bean.set("progress_notes_id", dao.getNextSequence());
        if (progressNtsTo.equals("visit")) {
          bean.set("visit_id", visitId);
        }
        success = dao.insert(con, bean);
      } else {
        keys.put("progress_notes_id", Integer.parseInt(progressId));
        success = dao.update(con, bean.getMap(), keys) > 0;
      }

    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    return redirect;
  }

  // Remove the bellow code and this code will putting new java class i.e. PatientProgressPrint.java
  // and related bug HMS-20608

  // public ActionForward getPrint(ActionMapping mapping, ActionForm form, HttpServletRequest
  // request,
  // HttpServletResponse response) throws SQLException, IOException, TemplateException,
  // TransformerException, XPathExpressionException, DocumentException {
  //
  // String mrNo = request.getParameter("mr_no");
  // String visitId = request.getParameter("patientId");
  // String filterVisitId = request.getParameter("filtervisitId");
  // String filterType = request.getParameter("filterType");
  // String fromScreen = request.getParameter("fromScreen");
  // String progressId = request.getParameter("progressID");
  //
  // BasicDynaBean printPref = null;
  //
  // int printerId = 0;
  // if ( (null != request.getParameter("printDefType")) && !
  // ("").equals(request.getParameter("printDefType"))) {
  // printerId = Integer.parseInt(request.getParameter("printDefType"));
  // }
  // printPref =
  // PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT,printerId);
  // String textContent = getTextContent(mrNo, visitId, filterType, filterVisitId, progressId,
  // fromScreen);
  // HtmlConverter hc = new HtmlConverter();
  //
  // PrintPageOptions opts = new PrintPageOptions(printPref);
  // OutputStream os = null;
  // try {
  // if (printPref.get("print_mode").equals("T")) {
  // String textReport =new String(hc.getText(textContent, "Progress Notes Details", printPref,
  // true, true));
  // request.setAttribute("textReport", textReport);
  // request.setAttribute("textColumns", printPref.get("text_mode_column"));
  // return mapping.findForward("textPrintApplet");
  // } else {
  // os = response.getOutputStream();
  // response.setContentType("application/pdf");
  // hc.writePdf(os, textContent, "Progress Notes Details", printPref, false, false, true,
  // false, true, false);
  // return null;
  // }
  // } finally {
  // if (os != null)
  // os.close();
  // }
  //
  // }
  //
  // public static String getTextContent(String mrNo, String visitId, String filterType,
  // String filterVisitId, String progressId, String fromScreen)throws SQLException, IOException,
  // TemplateException {
  //
  // Map<String, Object> params = new HashMap<String, Object>();
  // Template t = null;
  // if (mrNo == null)
  // mrNo = (String)new GenericDAO("progress_notes").findByKey("progress_notes_id",
  // progressId).get("mr_no");
  // params.put("patient", PatientDetailsDAO.getPatientGeneralDetailsMap(mrNo));
  // if (fromScreen == null)
  // params.put("progressNotesList", PatientProgressDAO.getProgressNotesForEMR(visitId, mrNo));
  // else
  // params.put("progressNotesList", PatientProgressDAO.getProgressNotesList(mrNo, filterType,
  // filterVisitId));
  // PrintTemplatesDAO templateDAO = new PrintTemplatesDAO();
  // String templateContent = templateDAO.getCustomizedTemplate(PrintTemplate.Progress_Notes);
  // if (templateContent == null || templateContent.equals(""))
  // t = AppInit.getFmConfig().getTemplate(PrintTemplate.Progress_Notes.getFtlName()+ ".ftl");
  // else {
  // StringReader reader = new StringReader(templateContent);
  // t = new Template("PatientProgressNotes.ftl", reader, AppInit.getFmConfig());
  // }
  // StringWriter writer = new StringWriter();
  // t.process(params, writer);
  // String textContent = writer.toString();
  // return textContent;
  // }
  //
  // public static byte[] getBytes(String docId, int printId)throws Exception {
  // HtmlConverter hc = new HtmlConverter();
  // String[] parts = docId.split(":");
  // String mrNO = parts[0].split("=")[1];
  // String visitID = parts[1].split("=")[1];
  // visitID = visitID.equals("noVisit") ? "" : visitID;
  // String textContent = getTextContent(mrNO, visitID, null, null, null, null);
  // BasicDynaBean printPref =
  // PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT, printId);
  // return hc.getPdfBytes(textContent, "Progress Notes Details", printPref, false, true, true,
  // true, false);
  //
  // }
}