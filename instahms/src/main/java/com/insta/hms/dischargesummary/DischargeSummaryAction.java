package com.insta.hms.dischargesummary;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PdfUtils;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.genericdocuments.DocumentPrintConfigurationsDAO;
import com.insta.hms.genericdocuments.GenericDocumentsAction;
import com.insta.hms.genericdocuments.GenericDocumentsDAO;
import com.insta.hms.genericdocuments.GenericDocumentsFields;
import com.insta.hms.genericdocuments.PatientDocumentsDAO;
import com.insta.hms.ipservices.PrescriptionViewDAO;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;
import com.insta.hms.master.PatientReport.PatientReportCommonDAO;
import com.insta.hms.master.PatientReport.TemplateReportDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.master.PrinterSettingsMaster.PrinterSettingsDAO;
import com.insta.hms.pdf2dom.PdfFormToDom;
import com.lowagie.text.DocumentException;

import flexjson.JSONSerializer;
import freemarker.template.TemplateException;
import jlibs.core.util.regex.TemplateMatcher;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.fit.pdfdom.PDFDomTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

// TODO: Auto-generated Javadoc
/** The Class DischargeSummaryAction. */
public class DischargeSummaryAction extends BaseAction {

  /** The logger. */
  static Logger logger =
      LoggerFactory.getLogger(DischargeSummaryAction.class);

  /** The bo. */
  static DischargeSummaryBOImpl bo = new DischargeSummaryBOImpl();

  /** The dao. */
  static DischargeSummaryDAOImpl dao = new DischargeSummaryDAOImpl();

  /** The p doc dao. */
  static PatientDocumentsDAO pDocDao = new PatientDocumentsDAO();

  /** The pdfvaluesdocdao. */
  static GenericDAO pdfvaluesdocdao = new GenericDAO("patient_pdf_form_doc_values");

  /** The pdftemplatedao. */
  static GenericDAO pdftemplatedao = new GenericDAO("doc_pdf_form_templates");

  /** The templatesdao. */
  static PatientReportCommonDAO templatesdao = new PatientReportCommonDAO();

  /** The doctordao. */
  static GenericDAO doctordao = new GenericDAO("doctors");

  /** The visitdao. */
  static VisitDetailsDAO visitdao = new VisitDetailsDAO();

  /*
   * write a single method to save the discharge summary of all formats.
   */

  /**
   * Adds the or edit.
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
   * @throws ServletException
   *           the servlet exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   */
  @IgnoreConfidentialFilters
  public ActionForward addOrEdit(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws ServletException,
      IOException, SQLException {

    HttpSession session = request.getSession(false);
    String patientId = request.getParameter("patient_id");

    Integer roleId = (Integer) session.getAttribute("roleId");
    Map actionRightsMap = (Map) session.getAttribute("actionRightsMap");
    BasicDynaBean visitbean = visitdao.getVisitDetailsWithConfCheck(patientId);
    if ((visitbean != null && visitbean.get("status").equals("I"))
        && roleId != 1
        && roleId != 2
        && (actionRightsMap != null 
        && actionRightsMap.get("dishcharge_close") != null 
        && actionRightsMap.get("dishcharge_close").equals("N"))) {
      logger.warn("No action rights to add/edit discharge summary");
      FlashScope flash = FlashScope.getScope(request);
      ActionRedirect redirect = new ActionRedirect(mapping.findForward("noActionRight"));
      flash.info("User has no action rights to add/edit discharge summary.");
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      return redirect;
    }

    String format = request.getParameter("format");
    BasicDynaBean dd = dao.getDischargeDetails(patientId);
    Integer docid = null;
    if (dd != null) {
      docid = (Integer) dd.get("discharge_doc_id");
    }
    ActionRedirect redirect = null;
    if (docid == null || docid == 0) {
      redirect = new ActionRedirect(mapping.findForward("addRedirect"));
      redirect.addParameter("chooseTemplate", true);
      redirect.addParameter("format", format);
    } else {
      redirect = new ActionRedirect(mapping.findForward("showRedirect"));
      redirect.addParameter("format", dd.get("discharge_format"));
    }
    redirect.addParameter("patient_id", patientId);
    redirect.addParameter("ps_status", request.getParameter("ps_status"));

    return redirect;
  }

  /**
   * Gets the discharge summary.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the discharge summary
   * @throws Exception
   *           the exception
   */
  public ActionForward getDischargeSummary(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {
    request.setAttribute("info",
        request.getAttribute("info") != null ? request.getAttribute("info") : "");

    return mapping.findForward("addshow");
  }

  /**
   * Adds the.
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
   * @throws ServletException
   *           the servlet exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   * @throws TemplateException
   *           the template exception
   * @throws Exception
   *           the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward add(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException, SQLException,
      TemplateException, Exception {

    ActionForward forward = mapping.findForward("addshow");
    Boolean chooseTemplate = new Boolean(request.getParameter("chooseTemplate"));
    String patientId = request.getParameter("patient_id");
    if (chooseTemplate) {
      if (patientId != null && !patientId.equals("")) {
        VisitDetailsDAO regDao = new VisitDetailsDAO();
        BasicDynaBean bean = regDao.getVisitDetailsWithConfCheck(patientId);
        if (bean == null) {
          String format = request.getParameter("format");
          // user entered visit id is not a valid patient id.
          FlashScope flash = FlashScope.getScope(request);
          flash.put("error", "No Patient with Id:" + patientId);
          ActionRedirect redirect = new ActionRedirect(mapping.findForward("addRedirect"));
          redirect.addParameter("chooseTemplate", true);
          redirect.addParameter("format", format);
          redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
          return redirect;
        }
      }
      List templates = templatesdao.listAllActive();
      request.setAttribute("templates", ConversionUtils.copyListDynaBeansToMap(templates));
      return forward;
    }

    String format = request.getParameter("format");
    String formId = request.getParameter("form_id");
    String userName = (String) request.getSession(false).getAttribute("userId");
    String templateCaption = "";

    if (format.equals("F")) {
      ArrayList arrFormFieldsFromDB = bo.getFormFieldsFromDatabase(formId);
      request.setAttribute("FormFieldsFromDB", arrFormFieldsFromDB);
      BasicDynaBean formHeader = new GenericDAO("form_header").findByKey("form_id", formId);
      templateCaption = (String) formHeader.get("form_caption");

    } else if (format.equals("T")) {
      // create a brand new one from the template given
      String templateTitle = dao.getTemplateTitleForHTMLPrint(formId);

      TemplateReportDAO tempDao = new TemplateReportDAO();
      BasicDynaBean report = (BasicDynaBean) tempDao.getTemplateReport(formId);
      String templateContent = (String) report.get("report_file");
      templateCaption = (String) report.get("template_caption");
      request.setAttribute("templateContent",
          replaceTags(templateContent, patientId, templateTitle, userName));

    } else if (format.equals("P")) {
      BasicDynaBean pdfTemplate = GenericDocumentsDAO.getPdfTemplateDetails(
          Integer.parseInt(formId), false);
      templateCaption = (String) pdfTemplate.get("template_name");
    }

    BasicDynaBean dischargeDetails = dao.getDischargeDetails(patientId);
    JSONSerializer js = new JSONSerializer();

    if (dischargeDetails != null) {
      request.setAttribute("dis", dischargeDetails.getMap());
    }
    request.setAttribute("templateCaption", templateCaption);
    request.setAttribute("allDoctorList",
        js.serialize(ConversionUtils.listBeanToListMap(DoctorMasterDAO.getAllActiveDoctors())));
    ArrayList arrFollowUpDetails = dao.getfollowUpDetails(patientId);
    request.setAttribute("disFollowUpDetails", js.exclude("class").serialize(arrFollowUpDetails));

    Integer printerId = (Integer) DocumentPrintConfigurationsDAO.getDischargeSummaryConfiguration()
        .get("printer_settings");
    Map printSettings = getPrintSettingsMap(printerId);

    String showPrinter = request.getParameter("showPrinter");
    if (showPrinter == null) {
      // show default printer.
      request.setAttribute("showPrinter", printerId);
    } else {
      request.setAttribute("showPrinter", showPrinter);
    }
    /*
     * these print preferences are used only for yui editor(Rich Text Templates) to populate on page
     * load. for print user selected printer definitions will be used.
     */
    request.setAttribute("printPrefs", printSettings);

    return mapping.findForward("addshow");
  }

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
   * @throws ServletException
   *           the servlet exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   * @throws Exception
   *           the exception
   */
  public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException, SQLException, Exception {
    String patientId = request.getParameter("patient_id");

    BasicDynaBean dd = dao.getDischargeDetails(patientId);
    int docid = (Integer) dd.get("discharge_doc_id");
    String format = (String) dd.get("discharge_format");
    String templateCaption = "";
    String lastUpdatedBy = "";

    if (format.equals("F")) {
      List arrFormFieldsValuesFromDB = bo.getFormFieldsValuesFromDatabase(docid);
      request.setAttribute("FormFieldsValuesFromDB", arrFormFieldsValuesFromDB);
      BasicDynaBean disform = dao.getDocForm(docid);
      templateCaption = (String) disform.get("form_caption");
      lastUpdatedBy = (String) disform.get("username");

    } else if (format.equals("T")) {
      BasicDynaBean report = dao.getDocumentReport(docid);
      templateCaption = (String) report.get("template_caption");
      request.setAttribute("templateContent", (String) report.get("report_file"));
      lastUpdatedBy = (String) report.get("username");

    } else if (format.equals("U")) {
      Map uploadedFileDetails = dao.getUploadedFileDetails(docid);
      request.setAttribute("uploadedFileDetails", uploadedFileDetails);

    } else if (format.equals("P")) {
      BasicDynaBean pdfTemplate = GenericDocumentsDAO.getPdfTemplateDetails(docid, true);
      templateCaption = (String) pdfTemplate.get("template_name");
    }
    request.setAttribute("lastUpdatedBy", lastUpdatedBy);
    BasicDynaBean dischargeDetails = dao.getDischargeDetails(patientId);
    JSONSerializer js = new JSONSerializer();

    if (dischargeDetails != null) {
      request.setAttribute("dis", dischargeDetails.getMap());
    }
    request.setAttribute("templateCaption", templateCaption);
    request.setAttribute("allDoctorList",
        js.serialize(ConversionUtils.listBeanToListMap(DoctorMasterDAO.getAllActiveDoctors())));
    ArrayList arrFollowUpDetails = dao.getfollowUpDetails(patientId);
    request.setAttribute("disFollowUpDetails", js.exclude("class").serialize(arrFollowUpDetails));

    Integer printerId = (Integer) PrintConfigurationsDAO.getDischargeDefaultPrintPrefs().get(
        "printer_id");
    Map printSettings = PrintConfigurationsDAO.getPageOptions(
        PrintConfigurationsDAO.PRINT_TYPE_PATIENT).getMap();
    //

    // overriding the printer definition if it is hvf or richetxt.
    if (format.equals("F") || format.equals("T")) {
      printerId = (Integer) DocumentPrintConfigurationsDAO.getDischargeSummaryConfiguration().get(
          "printer_settings");
      printSettings = getPrintSettingsMap(printerId);
      ;
    }

    String showPrinter = request.getParameter("showPrinter");
    if (showPrinter == null) {
      // show default printer.
      request.setAttribute("showPrinter", printerId);
    } else {
      request.setAttribute("showPrinter", showPrinter);
    }
    /*
     * these print preferences are used only for yui editor(Rich Text Templates) to populate on page
     * load. for print user selected printer definitions will be used.
     */
    request.setAttribute("printPrefs", printSettings);

    request.setAttribute("templateCaption", templateCaption);
    request.setAttribute("docid", docid);
    return mapping.findForward("addshow");
  }

  /*
   * This method is used to save the DischargeSummary Details of the patient
   */

  /**
   * Save discharge summary form.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the action forward
   * @throws ServletException
   *           the servlet exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   */
  public ActionForward saveDischargeSummaryForm(ActionMapping mapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException,
      SQLException, ParseException {

    String mrNo = null;
    String patientID = null;
    String disDate = null;
    String disTime = null;
    String doctorId = null;

    boolean resValue = false;

    String dischargeStatus = "";

    Map mapValuesFromDisForm = req.getParameterMap();

    Set setFromMap = mapValuesFromDisForm.keySet();
    Iterator itrFromSet = setFromMap.iterator();
    itrFromSet.next();
    int mapsize = mapValuesFromDisForm.size();
    Object[] keyValuePairs2 = mapValuesFromDisForm.entrySet().toArray();

    for (int i = 0; i < mapsize; i++) {
      Map.Entry entry = (Map.Entry) keyValuePairs2[i];
      Object key = entry.getKey();
      String[] values = (String[]) entry.getValue();
      if (key.equals("scat")) {
        mrNo = values[0];
      }
      if (key.equals("patient_id")) {
        patientID = values[0];
      }
      if (key.equals("dischargeDate")) {
        disDate = values[0];
      }
      if (key.equals("dischargeTime")) {
        disTime = values[0];
      }
      if (key.equals("doctorId")) {
        doctorId = values[0];
      }
    }

    String formId = req.getParameter("form_id");
    String docidStr = req.getParameter("docid");
    int docid = 0;
    if ((docidStr != null) && !docidStr.equals("")) {
      docid = Integer.parseInt(docidStr);
    }

    if (formId.contains(",")) {
      String[] array = formId.split(",");
      formId = array[0];
    }

    DischargeSummaryDTO dto = new DischargeSummaryDTO();
    DischargeSummaryForm af = (DischargeSummaryForm) form;

    dto.setFormat("F");
    dto.setDocid(docid);
    dto.setMrno(mrNo);
    dto.setPatId(patientID);
    dto.setDisDate(disDate);
    dto.setDisTime(disTime);
    dto.setDoctorId(doctorId);
    HttpSession session = req.getSession(false);
    dto.setUserName((String) session.getAttribute("userid"));

    // set the followup details
    dto.setFollowUpDate(af.getFollowUpDate());
    dto.setFollowUpDoctorId(af.getFollowUpDoctorId());
    dto.setFollowUpRemarks(af.getFollowUpRemarks());
    dto.setFollowUpId(af.getFollowUpId());
    dto.setDeleteFollowUpIds(af.getDeleteFollowUpIds());
    dto.setPatId(patientID);
    dto.setDisch_date_for_disch_summary(af.getDischDateForDischSummary());
    dto.setDisch_time_for_disch_summary(af.getDischTimeForDischSummary());

    if (af.isFinalized()) {
      if (af.getFinalizedUser().equals("")) {
        dto.setFinalizedUser((String) session.getAttribute("userid"));
        dto.setFinalizedDate(new java.sql.Date(new java.util.Date().getTime()));
        dto.setFinalizedTime(new java.sql.Time(new java.util.Date().getTime()));
      } else {
        dto.setFinalizedUser(af.getFinalizedUser());
        dto.setFinalizedDate(DateUtil.parseDate(af.getFinalizedDate()));
        dto.setFinalizedTime(DateUtil.parseTime(af.getFinalizedTime()));
      }
      GenericDAO userDAO = new GenericDAO("u_user");
      BasicDynaBean user = userDAO.findByKey("emp_username", dto.getFinalizedUser());
      String loggedInDoctorId = (String) user.get("doctor_id");
      loggedInDoctorId = loggedInDoctorId == null ? "" : loggedInDoctorId;
      String allowSigUsageByOthers = "";
      if (!doctorId.equals("")) {
        allowSigUsageByOthers = (String) ((BasicDynaBean) new DoctorMasterDAO().findByKey(
            "doctor_id", doctorId)).get("allow_sig_usage_by_others");
      }

      if ((!doctorId.equals("") && loggedInDoctorId.equals(doctorId))
          || allowSigUsageByOthers.equals("Y")) {
        dto.setSignatory_username(doctorId); 
      }
    }

    Connection con = DataBaseUtil.getConnection();
    con.setAutoCommit(false);
    String msg = null;
    try {
      msg = dao.saveDischargeSummary(con, mapValuesFromDisForm, docid, formId, mrNo, patientID,
          session.getAttribute("userid").toString());

      if (docid == 0) {
        docid = (Integer) mapValuesFromDisForm.get("docid");
        dto.setDocid(docid);
      }

      if (msg.equals("Discharge Summary Details are Saved")) {
        resValue = dao.saveDischargeDetails(dto, con);
      }

      if (resValue) {
        if (dto.getFollowUpDate() != null || dto.getFollowUpDoctorId() != null
            || dto.getFollowUpRemarks() != null || dto.getDeleteFollowUpIds() != null) {
          resValue = dao.saveFollowUpDetails(dto, con);
        }
      }
    } finally {
      DataBaseUtil.commitClose(con, resValue);
    }

    req.setAttribute("patientID", patientID);

    req.setAttribute("docid", docid);

    if (resValue) {
      logger.debug("Successfully Inserted DischargeSummary Details");
      dischargeStatus = "Successfully Inserted Discharge Summary Details";
      req.setAttribute("msg", dischargeStatus);
      req.setAttribute("printerId", req.getParameter("printerId"));
      req.setAttribute("patient_id", patientID);
      return mapping.findForward("printDisReport");

    } else {
      logger.warn("Error Occured in Inserting Discharge Summary Details");
      FlashScope flash = FlashScope.getScope(req);
      flash.put("error", "Transaction failure..");
      ActionRedirect redirect = new ActionRedirect(mapping.findForward("failureRedirect"));
      redirect.addParameter("patient_id", patientID);
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      return redirect;
    }
  }

  /**
   * Replace tags.
   *
   * @param templateContent
   *          the template content
   * @param patientId
   *          the patient id
   * @param templateTitle
   *          the template title
   * @param userName
   *          the user name
   * @return the string
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws TemplateException
   *           the template exception
   * @throws SQLException
   *           the SQL exception
   */
  private String replaceTags(String templateContent, String patientId, String templateTitle,
      String userName) throws IOException, TemplateException, SQLException {

    Map<String, String> patDet = new HashMap<>();
    GenericDocumentsFields.copyPatientDetails(patDet, null, patientId, false);

    if (patDet.get("discharge_date") == null) {
      patDet.put("discharge_date", 
          new DateUtil().getTimeStampFormatter().format(new java.util.Date()));
    }

    /*
     * the following (investigations/mediicnes/services) are treatment given to the patient.
     */
    List operations = PrescriptionViewDAO.getOpetaionsCompleted(patientId);

    Map replaceFields = new HashMap(patDet);

    String operationStartDate = null;
    String operationEndDate = null;
    String operationStartTime = null;
    String operationEndTime = null;

    if (operations != null && !operations.isEmpty()) {
      operationStartDate = (String) ((BasicDynaBean) operations.get(0)).get("operation_date");
      operationEndDate = (String) ((BasicDynaBean) operations.get(0)).get("operation_end_date");
      operationStartTime = (String) ((BasicDynaBean) operations.get(0)).get("starttime");
      operationEndTime = (String) ((BasicDynaBean) operations.get(0)).get("endtime");
    }
    replaceFields.put("operation_start_date", operationStartDate);
    replaceFields.put("operation_end_date", operationEndDate);
    replaceFields.put("operation_start_time", operationStartTime);
    replaceFields.put("operation_end_time", operationEndTime);

    try {
      String treatmentInfo = new DischargeSummaryReportHelper().processTreatment(patientId);
      replaceFields.put("treatmentSheet", treatmentInfo);

      String consultationDetails = new DischargeSummaryReportHelper().getConsultationDetails(
          patientId, userName);
      replaceFields.put("consultationDetails", consultationDetails);

      String otDetails = new DischargeSummaryReportHelper().getOTDetails(patientId);
      replaceFields.put("otDetails", otDetails);
    } catch (Exception exp) {
      logger.error("", exp);
    }

    TemplateMatcher matcher = new TemplateMatcher("${", "}");
    templateContent = matcher.replace(templateContent, replaceFields);

    /*
     * Section 3: Report Data
     */
    StringBuilder html = new StringBuilder("");
    html.append("<div >");
    html.append("<table cellspacing='0' cellpadding='1' width='100%'><tbody>");
    html.append("<tr><td align='center'>" + templateTitle + "</td></tr>");
    html.append("<tr height='20'></tr>");
    html.append("</tbody></table>\n");

    html.append("<table cellspacing='0' cellpadding='2'>").append("<tbody><tr><td>")
        .append(templateContent).append("</td></tr></tbody></table>").append("</div>\n");

    return html.toString();
  }

  /**
   * Save discharge summary html.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the action forward
   * @throws ServletException
   *           the servlet exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   * @throws DocumentException
   *           the document exception
   */
  public ActionForward saveDischargeSummaryHtml(ActionMapping mapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException,
      SQLException, ParseException, DocumentException {
    String mrno = "";
    String patId = "";
    String disDate = "";
    String disTime = "";
    String disType = "";
    String doctorId = "";
    String deathDate = null;
    String deathTime = null;
    String deathReason = null;

    Map mapValuesFromDisForm = req.getParameterMap();

    Set setFromMap = mapValuesFromDisForm.keySet();
    Iterator itrFromSet = setFromMap.iterator();
    itrFromSet.next();
    int mapsize = mapValuesFromDisForm.size();
    Object[] keyValuePairs2 = mapValuesFromDisForm.entrySet().toArray();

    for (int i = 0; i < mapsize; i++) {
      Map.Entry entry = (Map.Entry) keyValuePairs2[i];
      Object key = entry.getKey();
      String[] values = (String[]) entry.getValue();
      if (key.equals("scat")) {
        mrno = values[0];
      }
      if (key.equals("patient_id")) {
        patId = values[0];
      }
      if (key.equals("dischargeDate")) {
        disDate = values[0];
      }
      if (key.equals("dischargeTime")) {
        disTime = values[0];
      }
      if (key.equals("disType")) {
        disType = values[0];
      }
      if (key.equals("doctorId")) {
        doctorId = values[0];
      }
      if (key.equals("deathDate")) {
        deathDate = values[0];
      }
      if (key.equals("deathTime")) {
        deathTime = values[0];
      }
      if (key.equals("deathReason")) {
        deathReason = values[0];
      }
    }

    DischargeSummaryDTO dto = new DischargeSummaryDTO();

    int docid = 0;
    String docidStr = req.getParameter("docid");
    if ((docidStr != null) && !docidStr.equals("")) {
      docid = Integer.parseInt(docidStr);
    }

    dto.setMrno(mrno);
    dto.setPatId(patId);
    String formId = req.getParameter("form_id") != null ? req.getParameter("form_id") : "";
    dto.setTemplateId(formId);
    DischargeSummaryForm af = (DischargeSummaryForm) form;
    dto.setTemplateContent(af.getTemplateContent());

    dto.setFormat("T");
    dto.setDocid(docid);
    dto.setDisDate(disDate);
    dto.setDisTime(disTime);
    dto.setDisType(disType);
    dto.setDoctorId(doctorId);
    HttpSession session = req.getSession(false);
    dto.setUserName((String) session.getAttribute("userid"));

    if (disType != null && disType.equals("Expiry")) {
      dto.setDeathDate(deathDate);
      dto.setDeathTime(deathTime);
      dto.setDeathReason(deathReason);
    }

    dto.setFollowUpDate(af.getFollowUpDate());
    dto.setFollowUpDoctorId(af.getFollowUpDoctorId());
    dto.setFollowUpRemarks(af.getFollowUpRemarks());
    dto.setFollowUpId(af.getFollowUpId());
    dto.setDeleteFollowUpIds(af.getDeleteFollowUpIds());
    dto.setDisch_date_for_disch_summary(af.getDischDateForDischSummary());
    dto.setDisch_time_for_disch_summary(af.getDischTimeForDischSummary());

    if (af.isFinalized()) {
      if (af.getFinalizedUser().equals("")) {
        dto.setFinalizedUser((String) session.getAttribute("userid"));
        dto.setFinalizedDate(new java.sql.Date(new java.util.Date().getTime()));
        dto.setFinalizedTime(new java.sql.Time(new java.util.Date().getTime()));
      } else {
        dto.setFinalizedUser(af.getFinalizedUser());
        dto.setFinalizedDate(DateUtil.parseDate(af.getFinalizedDate()));
        dto.setFinalizedTime(DateUtil.parseTime(af.getFinalizedTime()));
      }
    }
    if (disType != null && disType.equals("Referred To")) {
      dto.setReferredTo(af.getReferredTo());
    }
    boolean resValue = false;
    Connection con = DataBaseUtil.getConnection();
    con.setAutoCommit(false);
    try {
      resValue = dao.saveDisHtml(dto, con);
      if (resValue) {
        resValue = dao.saveDischargeDetails(dto, con);
      }
      if (resValue) {
        if (dto.getFollowUpDate() != null || dto.getFollowUpDoctorId() != null
            || dto.getFollowUpRemarks() != null || dto.getDeleteFollowUpIds() != null) {
          resValue = dao.saveFollowUpDetails(dto, con);
        }
      }
    } finally {
      if (resValue) {
        con.commit();
      } else {
        con.rollback();
      }
      DataBaseUtil.closeConnections(con, null);
    }

    req.setAttribute("mrNo", mrno);
    req.setAttribute("patientID", patId);
    req.setAttribute("templateId", formId);
    req.setAttribute("docid", dto.getDocid());

    if (resValue) {
      req.setAttribute("printerId", req.getParameter("printerId"));
      return mapping.findForward("printDisHtmlReport");
    } else {
      FlashScope flash = FlashScope.getScope(req);
      flash.put("error", "Transaction failure..");
      ActionRedirect redirect = new ActionRedirect(mapping.findForward("failureRedirect"));
      redirect.addParameter("patient_id", patId);
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      return redirect;
    }
  }

  /**
   * Adds the patient row.
   *
   * @param html
   *          the html
   * @param label1
   *          the label 1
   * @param value1
   *          the value 1
   */
  private static void addPatientRow(StringBuilder html, String label1, Object value1) {

    if (empty(value1)) {
      value1 = "";
    }
    if (empty(label1) && empty(value1)) {
      return;
    }

    html.append("<tr>");
    html.append("<td width='15%'>").append(label1).append("</td>");
    html.append("<td width='43%'>").append(value1).append("</td>");
    html.append("</tr>");
  }

  /**
   * Empty.
   *
   * @param str
   *          the str
   * @return true, if successful
   */
  public static boolean empty(Object str) {
    return (str == null || str.toString().equals(""));
  }

  /**
   * Not empty.
   *
   * @param str
   *          the str
   * @return true, if successful
   */
  public static boolean notEmpty(Object str) {
    return !empty(str);
  }

  /**
   * Delete.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param req
   *          the req
   * @param resp
   *          the resp
   * @return the action forward
   * @throws SQLException
   *           the SQL exception
   * @throws UnsupportedEncodingException
   *           the unsupported encoding exception
   */
  /**
   * Deletes a report of the patient. Parameters: patient_id: the patient id for whom to delete the
   * document
   */
  public ActionForward delete(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse resp) throws SQLException, UnsupportedEncodingException {

    String patientId = req.getParameter("patient_id");

    int docid = 0;
    String format = "";
    String error = "";
    String mrNo = "";

    /*
     * Get discharge details of the current patient
     */
    BasicDynaBean dd = dao.getDischargeDetails(patientId);

    if (dd != null) {
      docid = (Integer) dd.get("discharge_doc_id");
      format = (String) dd.get("discharge_format");
      mrNo = (String) dd.get("mr_no");

      Connection con = null;
      boolean success = false;
      try {
        con = DataBaseUtil.getConnection();
        con.setAutoCommit(false);

        do {
          boolean tempSuccess = false;
          if (format.equals("F")) {
            tempSuccess = dao.deleteFormHeader(con, docid);
          } else if (format.equals("T")) {
            tempSuccess = dao.deleteHtmlReport(con, docid);
          } else if (format.equals("U")) {
            tempSuccess = dao.deleteUploadedFile(con, docid);
          } else if (format.equals("P")) {
            /**
             * discharge pdf form template is handled in two ways. Case 1) saves only the discharge
             * details. ---------------------------------------- no patient document is generated in
             * this case. but document id is generated and saved against to that patient
             *
             * <p>
             * Hence, no deleting required here. but we have to update the docid to '0' against to
             * that patient(because docid is inserted).
             *
             * <p>
             * Case 2) saves discharge details along with pdf form template.
             * --------------------------------------------------------------- patient document is
             * generated and document id is saved against to that patient.
             *
             * <p>
             * In this case we have to delete the document from patient documents. and update docid
             * to '0' against to that patient.
             */
            if (new GenericDocumentsDAO().exist("doc_id", docid)) {
              tempSuccess = GenericDocumentsDAO.deletePatientDocuments(con,
                  "doc_pdf_form_templates", docid);
            } else {
              tempSuccess = true;
            }
          }
          if (!tempSuccess) {
            break;
          }

          tempSuccess = dao.updateDischargeDocid(con, patientId, 0);
          if (!tempSuccess) {
            break;
          }

          success = true;

        } while (false);
      } finally {
        if (!success) {
          error = "Transaction failed, unable to delete report";
        }
        DataBaseUtil.commitClose(con, success);
      }

    } else {
      error = "No patient discharge report available for " + patientId;
    }

    FlashScope flash = FlashScope.getScope(req);
    flash.put("error", error);
    if (error != null) {
      flash.put("success", "Discharge Summary deleted succesfully.");
    }
    // ideally this should come from the referer header instead of a mapping
    String redirectPath = "/inpatients/dischargesummary/index.htm#/filter/default/patient/"
        + URLEncoder.encode(mrNo, "UTF-8") + "/dischargesummary?retain_route_params=true";
    ActionRedirect redirect = new ActionRedirect(redirectPath);

    return redirect;
  }

  /**
   * Save discharge summary upload files.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the action forward
   * @throws ServletException
   *           the servlet exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   */
  public ActionForward saveDischargeSummaryUploadFiles(ActionMapping mapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException,
      SQLException, ParseException {

    String docidStr = req.getParameter("docid");
    int docid = 0;
    if ((docidStr != null) && !docidStr.equals("")) {
      docid = Integer.parseInt(docidStr);
    }

    DischargeSummaryDTO dto = new DischargeSummaryDTO();
    DischargeSummaryForm af = (DischargeSummaryForm) form;

    dto.setFormat("U");
    dto.setDocid(docid);
    dto.setDisDate(af.getDischargeDate());
    dto.setDisTime(af.getDischargeTime());

    String distype = req.getParameter("disType");
    if (distype != null && distype.equals("Expiry")) {
      dto.setDeathDate(af.getDeathDate());
      dto.setDeathTime(af.getDeathTime());
      dto.setDeathReason(af.getDeathReason());
    }

    dto.setDisType(req.getParameter("disType"));
    dto.setDoctorId(req.getParameter("doctorId"));
    HttpSession session = req.getSession(false);
    dto.setUserName((String) session.getAttribute("userid"));

    dto.setTheFile(af.getTheFile());
    dto.setMrno(af.getScat());
    dto.setPatId(af.getPatient_id());

    dto.setContentfilename(af.getContentfilename());
    dto.setContenttype(af.getContenttype());
    dto.setFollowUpDate(af.getFollowUpDate());
    dto.setFollowUpDoctorId(af.getFollowUpDoctorId());
    dto.setFollowUpRemarks(af.getFollowUpRemarks());
    dto.setFollowUpId(af.getFollowUpId());
    dto.setDeleteFollowUpIds(af.getDeleteFollowUpIds());
    dto.setDisch_date_for_disch_summary(af.getDischDateForDischSummary());
    dto.setDisch_time_for_disch_summary(af.getDischTimeForDischSummary());

    if (af.isFinalized()) {
      if (af.getFinalizedUser().equals("")) {
        dto.setFinalizedUser((String) session.getAttribute("userid"));
        dto.setFinalizedDate(new java.sql.Date(new java.util.Date().getTime()));
        dto.setFinalizedTime(new java.sql.Time(new java.util.Date().getTime()));
      } else {
        dto.setFinalizedUser(af.getFinalizedUser());
        dto.setFinalizedDate(DateUtil.parseDate(af.getFinalizedDate()));
        dto.setFinalizedTime(DateUtil.parseTime(af.getFinalizedTime()));
      }
    }
    if (req.getParameter("disType") != null && req.getParameter("disType").equals("Referred To")) {
      dto.setReferredTo(af.getReferredTo());
    }

    FlashScope flash = FlashScope.getScope(req);

    String uploadStatus = null;
    Connection con = DataBaseUtil.getConnection();
    con.setAutoCommit(false);
    boolean allSuccess = false;
    ActionRedirect redirect = null;

    try {
      do {
        if (af.getTheFile().getFileSize() > 0) {
          uploadStatus = dao.insertDischargeUploadFiles(dto, con);
          if (!uploadStatus.equals("Successfully Uploaded Discharge Summary Files")) {
            flash.put("error", uploadStatus);
            redirect = new ActionRedirect(mapping.findForward("failureRedirect"));
            break;
          }
        }

        boolean result = dao.saveDischargeDetails(dto, con);
        if (!result) {
          flash.put("error", "Unable to save discharge details");
          redirect = new ActionRedirect(mapping.findForward("failureRedirect"));
          break;
        }

        if (dto.getFollowUpDate() != null || dto.getFollowUpDoctorId() != null
            || dto.getFollowUpRemarks() != null || dto.getDeleteFollowUpIds() != null) {
          result = dao.saveFollowUpDetails(dto, con);
          if (!result) {
            flash.put("error", "Unable to save follow up details");
            redirect = new ActionRedirect(mapping.findForward("failureRedirect"));
            break;
          }
        }
        allSuccess = true;
        flash.put("success", "Discharge details saved successfully");
        redirect = new ActionRedirect(mapping.findForward("successRedirect"));
      } while (false);

    } finally {
      DataBaseUtil.commitClose(con, allSuccess);
    }
    redirect.addParameter("format", "U");
    redirect.addParameter("docid", dto.getDocid());
    redirect.addParameter("patient_id", af.getPatient_id());
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

    return redirect;
  }

  /**
   * Open editable pdf form.
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
   * @throws ServletException
   *           the servlet exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   * @throws DocumentException
   *           the document exception
   * @throws TransformerException
   *           the transformer exception
   * @throws ParserConfigurationException
   *           the parser configuration exception
   */
  public ActionForward openEditablePdfForm(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws ServletException,
      IOException, SQLException, ParseException, DocumentException, TransformerException,
      ParserConfigurationException {

    DischargeSummaryDTO dto = new DischargeSummaryDTO();
    DischargeSummaryForm af = (DischargeSummaryForm) form;

    String docidStr = af.getDocid();
    String formId = af.getFormId();

    int docid = 0;
    if ((docidStr != null) && !docidStr.equals("")) {
      docid = Integer.parseInt(docidStr);
    }
    if (formId.contains(",")) {
      String[] array = formId.split(",");
      formId = array[0];
    }
    boolean resValue = false;

    dto.setFormat("P");
    dto.setDocid(docid);
    dto.setMrno(af.getScat());
    dto.setPatId(af.getPatient_id());
    dto.setDisDate(af.getDischargeDate());
    dto.setDisTime(af.getDischargeTime());
    dto.setDisType(af.getDisType());

    if (af.getDisType() != null && af.getDisType().equals("Expiry")) {
      dto.setDeathDate(af.getDeathDate());
      dto.setDeathTime(af.getDeathTime());
      dto.setDeathReason(af.getDeathReason());
    }
    dto.setDoctorId(af.getDoctorId());
    HttpSession session = request.getSession(false);
    dto.setUserName((String) session.getAttribute("userid"));

    // set the followup details
    dto.setFollowUpDate(af.getFollowUpDate());
    dto.setFollowUpDoctorId(af.getFollowUpDoctorId());
    dto.setFollowUpRemarks(af.getFollowUpRemarks());
    dto.setFollowUpId(af.getFollowUpId());
    dto.setDeleteFollowUpIds(af.getDeleteFollowUpIds());
    dto.setPatId(af.getPatient_id());
    dto.setDisch_date_for_disch_summary(af.getDischDateForDischSummary());
    dto.setDisch_time_for_disch_summary(af.getDischTimeForDischSummary());

    if (af.isFinalized()) {
      if (af.getFinalizedUser().equals("")) {
        dto.setFinalizedUser((String) session.getAttribute("userid"));
        dto.setFinalizedDate(new java.sql.Date(new java.util.Date().getTime()));
        dto.setFinalizedTime(new java.sql.Time(new java.util.Date().getTime()));
      } else {
        dto.setFinalizedUser(af.getFinalizedUser());
        dto.setFinalizedDate(DateUtil.parseDate(af.getFinalizedDate()));
        dto.setFinalizedTime(DateUtil.parseTime(af.getFinalizedTime()));
      }
    }
    if (af.getDisType() != null && af.getDisType().equals("Referred To")) {
      dto.setReferredTo(af.getReferredTo());
    }

    /*
     * handle transaction correctly
     */
    Connection con = DataBaseUtil.getConnection();
    con.setAutoCommit(false);
    String msg = null;
    try {
      if (docid == 0) {
        docid = pDocDao.getNextSequence();
        BasicDynaBean bean = pDocDao.getBean();
        bean.set("doc_id", docid);
        bean.set("template_id", Integer.parseInt(formId));
        bean.set("doc_format", "doc_pdf_form_templates");
        bean.set("doc_type", "SYS_DS");
        resValue = pDocDao.insert(con, bean);
        dto.setDocid(docid);

      } else {
        // if doc id exists no need to update the patient documents
        BasicDynaBean bean = pDocDao.getBean();
        pDocDao.loadByteaRecords(bean, "doc_id", docid);
        formId = (Integer) bean.get("template_id") + "";
        resValue = true;
      }
      if (resValue) {
        resValue = false;
        resValue = dao.saveDischargeDetails(dto, con);
      }
      if (resValue) {
        if (resValue) {
          if (dto.getFollowUpDate() != null || dto.getFollowUpDoctorId() != null
              || dto.getFollowUpRemarks() != null || dto.getDeleteFollowUpIds() != null) {
            resValue = false;
            resValue = dao.saveFollowUpDetails(dto, con);
          }
        }
      }
    } finally {
      if (resValue) {
        con.commit();
        msg = "Discharge Details Saved Successfully..";
      } else {
        con.rollback();
        msg = "Transaction Failure";
      }
      DataBaseUtil.closeConnections(con, null);
    }
    request.setAttribute("patientID", af.getPatient_id());
    request.setAttribute("docid", docid);
    String openPdf = request.getParameter("openPdf");
    if (msg.equals("Transaction Failure") || openPdf.equals("N")) {
      FlashScope flash = FlashScope.getScope(request);
      flash.put("error", "Transaction failure..");
      ActionRedirect redirect = new ActionRedirect(mapping.findForward("failureRedirect"));
      redirect.addParameter("patient_id", request.getParameter("patient_id"));
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      return redirect;

    } else {
      Map pdfParams = new HashMap();
      GenericDocumentsFields.copyStandardFields(pdfParams, true);
      GenericDocumentsFields.copyPatientDetails(pdfParams, af.getScat(), af.getPatient_id(), true);
      copyFollowupDetails(pdfParams, af);

      BasicDynaBean bean = pdftemplatedao.getBean();
      int tempformId = Integer.parseInt(formId);
      pdftemplatedao.loadByteaRecords(bean, "template_id", tempformId);
      InputStream pdf = (InputStream) bean.get("template_content");
      InputStream html = (InputStream) bean.get("html_template");
      if (null == html && null != pdf) {
        PDDocument document = null;
        document = PDDocument.load(pdf);
        PDFDomTree domTree = new PdfFormToDom();
        Document dom = domTree.createDOM(document);
        byte[] htmlBytes = null;
        htmlBytes = GenericDocumentsAction.documentToByteArray(dom);
        Map<String, byte[]> dataMap = new HashMap<>();
        dataMap.put("html_template", htmlBytes);
        String[] updateColumns = { "html_template" };
        Map keyMap = new HashMap();
        keyMap.put("template_id", tempformId);
        con = DataBaseUtil.getConnection();
        pdftemplatedao.update(con, updateColumns, dataMap, keyMap);
        DataBaseUtil.closeConnections(con, null);

        html = new ByteArrayInputStream(htmlBytes);
      }

      Map hiddenParams = new HashMap();

      if (pdfvaluesdocdao.exist("doc_id", docid)) {
        List<BasicDynaBean> fieldslist = pdfvaluesdocdao.listAll(null, "doc_id", docid);
        for (BasicDynaBean fieldsBean : fieldslist) {
          pdfParams.put(fieldsBean.get("field_name").toString(), fieldsBean.get("field_value")
              .toString());
        }
        hiddenParams.put("details", "update");
      } else {
        hiddenParams.put("details", "insert");
      }
      hiddenParams.put("template_id", formId);
      hiddenParams.put("doc_id", "" + docid);
      hiddenParams.put("patient_id", af.getPatient_id());
      hiddenParams.put("_method", "saveDischargePdfForm");

      String submitUrl = request.getContextPath() + "/dischargesummary/discharge.do";

      OutputStream os = response.getOutputStream();
      PdfUtils.sendFillableForm(os, html, pdfParams, false, submitUrl, hiddenParams, null);

      return null;
    }
  }

  /**
   * Save discharge pdf form.
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
   * @throws ServletException
   *           the servlet exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   */
  public ActionForward saveDischargePdfForm(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws ServletException,
      IOException, SQLException {
    Map params = request.getParameterMap();
    String action = ((String[]) params.get("details"))[0];
    Connection con = DataBaseUtil.getConnection();
    con.setAutoCommit(false);
    String msg = null;
    String error = null;
    ActionRedirect redirect = null;
    FlashScope flash = FlashScope.getScope(request);
    String username = (String) request.getSession(false).getAttribute("userid");
    try {
      int docid = Integer.parseInt(((String[]) params.get("doc_id"))[0]);
      if (action.equals("insert")) {
        if (PatientDocumentsDAO.insertPDFFormFieldValues(con, params, docid, username)) {
          msg = "Discharge Pdf Form Report Template saved successfully..";
        } else {
          error = "Failed to save the Discharge Pdf Form Report Template..";
        }
      } else {
        if (PatientDocumentsDAO.updatePDFFormFieldValues(con, params, docid)) {
          msg = "Discharge Pdf Form Report Template updated successfully..";
        } else {
          error = "Failed to update the Discharge Pdf Form Report Template..";
        }
      }
    } finally {
      if (msg != null) {
        con.commit();
        flash.put("success", msg);
        redirect = new ActionRedirect(mapping.findForward("successRedirect"));
        redirect.addParameter("patient_id", ((String[]) params.get("patient_id"))[0]);
      } else {
        con.rollback();
        flash.put("error", error);
        redirect = new ActionRedirect(mapping.findForward("failureRedirect"));
        redirect.addParameter("patient_id", ((String[]) params.get("patient_id"))[0]);
        redirect.addParameter("form_id", ((String[]) params.get("template_id"))[0]);
        redirect.addParameter("format", "P");
      }
      DataBaseUtil.closeConnections(con, null);
    }
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    return redirect;
  }

  /**
   * Copy followup details.
   *
   * @param map
   *          the map
   * @param form
   *          the form
   */
  private static void copyFollowupDetails(Map map, DischargeSummaryForm form) {
    if (form.getFollowUpId() != null) {
      int index = 1;
      for (String id : form.getFollowUpId()) {
        map.put("_followup_doctorName_" + index, form.getFollowUpDoctorName()[index - 1]);
        map.put("_followup_remarks_" + index, form.getFollowUpRemarks()[index - 1]);
        map.put("_followup_date_" + index, form.getFollowUpDate()[index - 1]);
        index++;
      }
    }
  }

  /**
   * Gets the image content.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the image content
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   */
  public ActionForward getImageContent(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {

    String strDocId = request.getParameter("docid");
    byte[] byteData = null;
    ArrayList al = dao.getUploadImageResult(Integer.parseInt(strDocId));
    byteData = (byte[]) al.get(0);

    response.setHeader("expires", "0");
    if (al.get(2) != null && (!al.get(2).toString().equalsIgnoreCase(""))) {
      response.setContentType(al.get(2).toString());
    }
    String fileName = al.get(3) != null ? al.get(3).toString() : "";
    Object originalExtension = al.get(4) != null ? al.get(4) : "";
    if (!fileName.equals("")) {
      if (!fileName.contains(".") && !originalExtension.equals("")) {
        fileName = fileName + "." + originalExtension;
      }
      response.setHeader("Content-disposition", "inline; filename=\"" + fileName + "\"");
    }
    OutputStream os = response.getOutputStream();
    os.write(byteData);
    os.flush();
    os.close();

    return null;
  }

  /**
   * Delete image.
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
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ServletException
   *           the servlet exception
   * @throws SQLException
   *           the SQL exception
   */
  public ActionForward deleteImage(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException, SQLException {
    String strDocId = request.getParameter("docId");
    String mrno = request.getParameter("mrNo");
    String patId = request.getParameter("patient_id");
    boolean deleteStatus = dao.deleteImage(Integer.parseInt(strDocId));

    request.setAttribute("mrNo", mrno);
    request.setAttribute("patientID", patId);

    return mapping.findForward("onInsertSuccessOrFailure");
  }

  /**
   * Gets the prints the settings map.
   *
   * @param printerId
   *          the printer id
   * @return the prints the settings map
   * @throws SQLException
   *           the SQL exception
   */
  private Map getPrintSettingsMap(Integer printerId) throws SQLException {
    PrinterSettingsDAO psDao = new PrinterSettingsDAO();
    BasicDynaBean printSettings = psDao.findByKey("printer_id", printerId);
    return printSettings.getMap();
  }
}
