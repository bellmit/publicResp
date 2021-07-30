package com.insta.hms.clinicaldatalabresuts;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.TagFunctions;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.diagnosticmodule.common.DiagnosticsDAO;
import com.insta.hms.diagnosticmodule.laboratory.LaboratoryDAO;
import com.insta.hms.diagnosticsmasters.ResultRangesDAO;
import com.insta.hms.dialysisadequacy.DialysisAdequacyDAO;
import com.insta.hms.genericdocuments.GenericDocumentsFields;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDTO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.master.PrintTemplates.PrintTemplate;
import com.insta.hms.master.PrintTemplates.PrintTemplatesDAO;

import flexjson.JSONSerializer;
import freemarker.template.Template;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * The Class ClinicalDataLabResutsAction.
 */
public class ClinicalDataLabResutsAction extends DispatchAction {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(ClinicalDataLabResutsAction.class);

  /** The json. */
  JSONSerializer json = new JSONSerializer().exclude("class");

  /** The clinical data DAO. */
  ClinicalDataLabResutsDAO clinicalDataDAO = new ClinicalDataLabResutsDAO();

  private static final GenericDAO clinicalLabRecordedDAO = new GenericDAO("clinical_lab_recorded");
  private static final GenericDAO clinicalLabValuesDAO = new GenericDAO("clinical_lab_values");
  private static final GenericDAO dialysisPrescriptionsDAO =
      new GenericDAO("dialysis_prescriptions");
  
  /**
   * List.
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
   * @throws Exception
   *           the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws Exception {

    String mrno = (String) req.getParameter("mr_no");
    if (mrno != null && !mrno.equals("")) {
      Map patmap = PatientDetailsDAO.getPatientGeneralDetailsMap(mrno);
      if (patmap == null) {
        FlashScope flash = FlashScope.getScope(req);
        flash.put("error", mrno + " doesn't exists.");
        ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
        return redirect;
      }
    }

    if ((mrno != null && !mrno.equals(""))) {
      Map requestParams = req.getParameterMap();
      PagedList dateList = null;
      dateList = clinicalDataDAO.getClinicalDates(requestParams,
          ConversionUtils.getListingParameter(requestParams), mrno);
      req.setAttribute("masterList", clinicalDataDAO.getClinicalMasterRecords());
      List dtoList = dateList.getDtoList();
      List list = new ArrayList();
      for (int i = 0; i < dtoList.size(); i++) {
        list.add(((BasicDynaBean) dtoList.get(i)).get("values_as_of_date"));
      }
      req.setAttribute("test_result_values",
          ConversionUtils.listBeanToMapMapBean(
              (clinicalDataDAO.getClinicalLabDetails(list, mrno)).getDtoList(),
              "clinical_lab_recorded_id_text", "resultlabel_id"));
      req.setAttribute("pagedList", dateList);
      req.setAttribute("dateList", clinicalDataDAO.getClinicalLabDates(requestParams,
          ConversionUtils.getListingParameter(requestParams), mrno));
    }
    return mapping.findForward("list");
  }

  /**
   * Adds the.
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
   * @throws Exception
   *           the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward add(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws Exception {
    req.setAttribute("clinicalLabDetails", ClinicalDataLabResutsDAO.getClinicalLabDetails());
    req.setAttribute("jsonHtmlColorCodes", GenericPreferencesDAO.getAllPrefs());
    return mapping.findForward("addshow");
  }

  /**
   * Show.
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
   * @throws Exception
   *           the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws Exception {
    String mrno = (String) req.getParameter("mr_no");
    String valueDate = req.getParameter("values_as_of_date");
    String clinicalLabReordedId = req.getParameter("clinical_lab_recorded_id");
    req.setAttribute("clinicalLabDetails",
        ClinicalDataLabResutsDAO.getClinicalLabRecordsList(Integer.parseInt(clinicalLabReordedId)));
    req.setAttribute("clinicalLabValues", ClinicalDataLabResutsDAO.getClinicalLabValuesBean(mrno,
        valueDate, Integer.parseInt(clinicalLabReordedId)));
    req.setAttribute("jsonHtmlColorCodes", GenericPreferencesDAO.getAllPrefs());
    return mapping.findForward("addshow");
  }

  /**
   * Creates the.
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
   * @throws Exception
   *           the exception
   */
  public ActionForward create(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws Exception {
    Connection con = null;
    boolean success = false;
    int clinicalLabRecordedId = clinicalLabRecordedDAO.getNextSequence();
    String userName = (String) req.getSession(false).getAttribute("userid");
    Timestamp currentDateTime = DataBaseUtil.getDateandTime();
    String[] resultlabelId = req.getParameterValues("resultlabel_id");
    String[] testValue = req.getParameterValues("test_value");
    String[] remarks = req.getParameterValues("remarks");
    String[] valueDate = req.getParameterValues("value_date");
    // String[] severity = req.getParameterValues("withinNormal");
    BasicDynaBean clinicalLabRecordBean = clinicalLabRecordedDAO.getBean();
    BasicDynaBean clinicalLabValuesBean = null;
    Map<String, Map<String, Object>> valuesMap = new HashMap<String, Map<String, Object>>();
    DialysisAdequacyDAO adequacyDao = new DialysisAdequacyDAO();

    LaboratoryDAO laboratoryDao = new LaboratoryDAO();
    BasicDynaBean referenceResultRangeBean = null;
    boolean isCalculatedValue = false;
    Map sessionMap = null;
    List errors = new ArrayList();
    FlashScope flash = FlashScope.getScope(req);
    ConversionUtils.copyToDynaBean(req.getParameterMap(), clinicalLabRecordBean, errors);
    clinicalLabRecordBean.set("clinical_lab_recorded_id", clinicalLabRecordedId);
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      if (errors != null) {
        success = clinicalLabRecordedDAO.insert(con, clinicalLabRecordBean);
        HashMap resMap = new HashMap();
        String mrno = (String) req.getParameter("mr_no");
        List severityValue = new ArrayList<String>();
        // severityValue.clear();
        String severity = null;
        String resLabel = null;
        Map patmap = PatientDetailsDAO.getPatientGeneralDetailsMap(mrno);
        if (resultlabelId != null) {
          for (int i = 0; i < resultlabelId.length; i++) {
            int valuesId = clinicalLabValuesDAO.getNextSequence();
            clinicalLabValuesBean = clinicalLabValuesDAO.getBean();
            clinicalLabValuesBean.set("values_id", valuesId);
            clinicalLabValuesBean.set("clinical_lab_recorded_id", clinicalLabRecordedId);
            clinicalLabValuesBean.set("resultlabel_id", Integer.parseInt(resultlabelId[i]));
            clinicalLabValuesBean.set("test_value", testValue[i]);
            clinicalLabValuesBean.set("remarks", remarks[i]);
            Date thisValueDate = (valueDate != null && !valueDate[i].equals(""))
                ? DateUtil.parseDate(valueDate[i]) : null;
            clinicalLabValuesBean.set("value_date", thisValueDate);
            resMap.put("resultlabel_id", Integer.parseInt(resultlabelId[i]));
            if (thisValueDate != null) {
              resMap.put("sample_date", valueDate[i]);
            } else {
              resMap.put("sample_date", DateUtil.currentDate("dd-MM-yyyy"));

            }

            if (resultlabelId != null && !resultlabelId[i].isEmpty()) {
              isCalculatedValue = DiagnosticsDAO
                  .isCalculateResult(Integer.parseInt(resultlabelId[i]));
            }

            referenceResultRangeBean = ResultRangesDAO.getResultRange(resMap, patmap);
            if (referenceResultRangeBean != null && testValue != null && testValue[i] != null
                && !testValue[i].equals("") && TagFunctions.isNumeric(testValue[i])) {
              severity = (laboratoryDao.checkSeviarity(referenceResultRangeBean,
                  new BigDecimal(testValue[i])));
              if (severity.equals("###") || severity.equals("***")) {
                resLabel = LaboratoryDAO.getResultLabel(con, Integer.parseInt(resultlabelId[i]));
                severityValue.add(resLabel);
                clinicalLabValuesBean.set("test_value", "");
                clinicalLabValuesBean.set("value_date", null);
              }
            }
            if (testValue[i] != null && !testValue[i].isEmpty()) {
              clinicalLabValuesBean.set("user_name", userName);
              clinicalLabValuesBean.set("mod_time", DateUtil.getCurrentTimestamp());
            } else {
              clinicalLabValuesBean.set("user_name", null);
              clinicalLabValuesBean.set("mod_time", null);
            }
            // clinicalLabValuesBean.set("withinnormal", severity[i]);
            success = clinicalLabValuesDAO.insert(con, clinicalLabValuesBean);
            if (!success) {
              break;
            }
          }
        }
        String mrNo = (String) clinicalLabRecordBean.get("mrno");
        java.sql.Date valuesAsOfDate = (java.sql.Date) clinicalLabRecordBean
            .get("values_as_of_date");
        int prescriptionId = 0;
        BasicDynaBean prescriptionBean = dialysisPrescriptionsDAO.findByKey("mr_no", mrNo);

        BasicDynaBean sessionBean = null;
        if (prescriptionBean != null) {
          prescriptionId = (Integer) prescriptionBean.get("dialysis_presc_id");
        }
        if (prescriptionId != 0) {
          sessionBean = ClinicalDataLabResutsDAO.getSessionBean(con, prescriptionId,
              valuesAsOfDate);
        }
        if (sessionBean != null) {
          sessionMap = sessionBean.getMap();
        } else {
          sessionMap = new HashMap();
          sessionMap.put("start_time", new java.sql.Timestamp(valuesAsOfDate.getTime()));
        }

        valuesMap = adequacyDao.getCalculatedKtvandUrr(con, sessionMap, req.getParameter("mr_no"),
            userName, clinicalLabRecordedId);
        success = adequacyDao.saveKtvAndUrrValues(con, valuesMap, mrNo, userName);
        String formatedResults = severityValue.toString().replace("[", "").replace("]", "").trim();
        if (severityValue.size() > 0) {
          flash.info("The following Result labels are having Improbable High/Low values..."
              + "These values are not allowed.\n" + formatedResults + " ");
        }
      }
    } finally {
      DataBaseUtil.commitClose(con, success);
    }

    boolean saveAndPrint = req.getParameter("saveAndPrint") != null
        && req.getParameter("saveAndPrint").equals("Y");
    if (saveAndPrint) {
      String printURL = req.getContextPath();
      printURL = printURL + "/dialysis/ClinicalDataLabResults.do?_method=generatePrint";
      printURL = printURL + "&reportId=" + clinicalLabRecordedId;
      printURL = printURL + "&printerId=" + req.getParameter("printerId");

      List<String> printURLs = new ArrayList<String>();
      printURLs.add(printURL);
      HttpSession session = req.getSession();
      session.setAttribute("printURLs", printURLs);
    }

    ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
    redirect.addParameter("clinical_lab_recorded_id",
        clinicalLabRecordBean.get("clinical_lab_recorded_id"));
    redirect.addParameter("mr_no", clinicalLabRecordBean.get("mrno"));
    redirect.addParameter("values_as_of_date", clinicalLabRecordBean.get("values_as_of_date"));
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    return redirect;
  }

  /**
   * Update.
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
   * @throws Exception
   *           the exception
   */
  @SuppressWarnings("rawtypes")
  public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws Exception {
    Connection con = null;
    boolean success = false;
    String userName = (String) req.getSession(false).getAttribute("userid");
    Timestamp currentDateTime = DataBaseUtil.getDateandTime();
    String[] resultlabelId = req.getParameterValues("resultlabel_id");
    String[] testValue = req.getParameterValues("test_value");
    String[] remarks = req.getParameterValues("remarks");
    String[] valuesId = req.getParameterValues("values_id");
    String[] valueDate = req.getParameterValues("value_date");
    String[] edited = req.getParameterValues("edited");
    // String[] severity = req.getParameterValues("withinNormal");
    BasicDynaBean clinicalLabRecordBean = clinicalLabRecordedDAO.getBean();
    BasicDynaBean clinicalLabValuesBean = null;
    DialysisAdequacyDAO adequacyDao = new DialysisAdequacyDAO();
    Map<String, Map<String, Object>> valuesMap = new HashMap<String, Map<String, Object>>();
    BasicDynaBean referenceResultRangeBean = null;
    LaboratoryDAO laboratoryDao = new LaboratoryDAO();
    FlashScope flash = FlashScope.getScope(req);
    Map sessionMap = null;
    List errors = new ArrayList();
    Map keys = new HashMap();
    String clinicalLabRecordedId = req.getParameter("clinical_lab_recorded_id");
    keys.put("clinical_lab_recorded_id", Integer.parseInt(clinicalLabRecordedId));
    ConversionUtils.copyToDynaBean(req.getParameterMap(), clinicalLabRecordBean, errors);
    clinicalLabRecordBean.set("clinical_lab_recorded_id", Integer.parseInt(clinicalLabRecordedId));
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      HashMap resMap = new HashMap();
      String mrno = (String) req.getParameter("mr_no");
      List severityValue = new ArrayList<String>();
      severityValue.clear();
      String severity;
      String resLabel;
      Map patmap = PatientDetailsDAO.getPatientGeneralDetailsMap(mrno);
      boolean isCalculatedValue = false;

      if (errors != null) {
        clinicalLabRecordedDAO.update(con, clinicalLabRecordBean.getMap(), keys);

        if (valuesId != null) {
          for (int i = 0; i < valuesId.length; i++) {
            Map keys1 = new HashMap();
            keys1.put("values_id", Integer.parseInt(valuesId[i]));
            clinicalLabValuesBean = clinicalLabValuesDAO.getBean();
            clinicalLabValuesBean.set("values_id", Integer.parseInt(valuesId[i]));
            clinicalLabValuesBean.set("clinical_lab_recorded_id",
                Integer.parseInt(clinicalLabRecordedId));
            clinicalLabValuesBean.set("resultlabel_id", Integer.parseInt(resultlabelId[i]));
            clinicalLabValuesBean.set("test_value", testValue[i]);
            clinicalLabValuesBean.set("remarks", remarks[i]);
            Date thisValueDate = (valueDate != null && !valueDate[i].equals(""))
                ? DateUtil.parseDate(valueDate[i]) : null;
            clinicalLabValuesBean.set("value_date", thisValueDate);
            resMap.put("resultlabel_id", Integer.parseInt(resultlabelId[i]));
            if (thisValueDate != null) {
              resMap.put("sample_date", valueDate[i]);
            } else {
              resMap.put("sample_date", DateUtil.currentDate("dd-MM-yyyy"));

            }

            if (resultlabelId != null && !resultlabelId[i].isEmpty()) {
              isCalculatedValue = DiagnosticsDAO.isCalculateResult(new Integer(resultlabelId[i]));
            }

            referenceResultRangeBean = ResultRangesDAO.getResultRange(resMap, patmap);
            boolean severityCheck = false;
            if (referenceResultRangeBean != null && testValue != null && testValue[i] != null
                && !testValue[i].equals("") && TagFunctions.isNumeric(testValue[i])) {
              severity = (laboratoryDao.checkSeviarity(referenceResultRangeBean,
                  new BigDecimal(testValue[i])));
              if (severity.equals("###") || severity.equals("***")) {
                resLabel = LaboratoryDAO.getResultLabel(con, Integer.parseInt(resultlabelId[i]));
                severityValue.add(resLabel);
                severityCheck = true;
              }
            }

            if (edited != null && edited[i].equals("Y")) {
              clinicalLabValuesBean.set("user_name", userName);
              clinicalLabValuesBean.set("mod_time", DateUtil.getCurrentTimestamp());
            }
            if (!severityCheck) {
              clinicalLabValuesDAO.update(con, clinicalLabValuesBean.getMap(), keys1);
              success = true;
            }
          }
        }

        String mrNo = (String) clinicalLabRecordBean.get("mrno");
        java.sql.Date valuesAsOfDate = (java.sql.Date) clinicalLabRecordBean
            .get("values_as_of_date");
        int prescriptionId = 0;
        BasicDynaBean prescriptionBean = dialysisPrescriptionsDAO.findByKey("mr_no", mrNo);

        BasicDynaBean sessionBean = null;
        if (prescriptionBean != null) {
          prescriptionId = (Integer) prescriptionBean.get("dialysis_presc_id");
        }
        if (prescriptionId != 0) {
          sessionBean = ClinicalDataLabResutsDAO.getSessionBean(con, prescriptionId,
              valuesAsOfDate);
        }
        if (sessionBean != null) {
          sessionMap = sessionBean.getMap();
        } else {
          sessionMap = new HashMap();
          sessionMap.put("start_time", new java.sql.Timestamp(valuesAsOfDate.getTime()));
        }

        valuesMap = adequacyDao.getCalculatedKtvandUrr(con, sessionMap, req.getParameter("mrno"),
            userName, Integer.parseInt(clinicalLabRecordedId));
        success = adequacyDao.saveKtvAndUrrValues(con, valuesMap, mrNo, userName);
        String formatedResults = severityValue.toString().replace("[", "").replace("]", "").trim();
        if (severityValue.size() > 0) {
          flash.info("The following Result labels are having Improbable High/Low values..."
              + "These values are not allowed.\n" + formatedResults + " ");
        }
      }
    } finally {
      DataBaseUtil.commitClose(con, success);
    }

    ActionRedirect redirect = null;
    redirect = new ActionRedirect(mapping.findForward("showRedirect"));
    boolean saveAndPrint = req.getParameter("saveAndPrint") != null
        && req.getParameter("saveAndPrint").equals("Y");
    if (saveAndPrint) {
      String printURL = req.getContextPath();
      printURL = printURL + "/dialysis/ClinicalDataLabResults.do?_method=generatePrint";
      printURL = printURL + "&reportId=" + clinicalLabRecordedId;
      printURL = printURL + "&printerId=" + req.getParameter("printerId");

      List<String> printURLs = new ArrayList<String>();
      printURLs.add(printURL);
      HttpSession session = req.getSession();
      session.setAttribute("printURLs", printURLs);
    }

    redirect.addParameter("clinical_lab_recorded_id", clinicalLabRecordedId);
    redirect.addParameter("mr_no", clinicalLabRecordBean.get("mrno"));
    redirect.addParameter("values_as_of_date", clinicalLabRecordBean.get("values_as_of_date"));
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    return redirect;
  }

  /**
   * Generate print.
   *
   * @param am
   *          the am
   * @param af
   *          the af
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the action forward
   * @throws Exception
   *           the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward generatePrint(ActionMapping am, ActionForm af, HttpServletRequest req,
      HttpServletResponse res) throws Exception {

    Connection con = null;
    Map params = new HashMap();
    String reportIdStr = req.getParameter("reportId");
    int reportId = reportIdStr == null ? 0 : Integer.parseInt(reportIdStr);
    if (reportId != 0) {
      GenericPreferencesDTO dto = GenericPreferencesDAO.getGenericPreferences();
      BasicDynaBean mainList = ClinicalDataLabResutsDAO.getClinicalLabRecordedMain(reportId);
      String printerId = req.getParameter("printerId");

      BasicDynaBean printprefs = null;
      if (printerId != null) {
        printprefs = PrintConfigurationsDAO
            .getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT, Integer.parseInt(printerId));
      }
      if (printprefs == null) {
        printprefs = PrintConfigurationsDAO
            .getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT);
      }

      Map patDetails = new HashMap();
      GenericDocumentsFields.copyPatientDetails(patDetails, (String) mainList.get("mrno"), null,
          false);
      params.put("patient", patDetails);
      List<BasicDynaBean> resultList = ClinicalDataLabResutsDAO.getClinicalLabRecordsList(reportId);
      params.put("result", ConversionUtils.listBeanToListMap(resultList));
      params.put("main", mainList.getMap());
      PrintTemplatesDAO printtemplatedao = new PrintTemplatesDAO();
      String templateContent = printtemplatedao.getCustomizedTemplate(PrintTemplate.CLab);

      Template t1 = null;
      if (templateContent == null || templateContent.equals("")) {
        t1 = AppInit.getFmConfig().getTemplate(PrintTemplate.CLab.getFtlName() + ".ftl");
      } else {
        StringReader reader = new StringReader(templateContent);
        t1 = new Template("ClinicalLabPrint.ftl", reader, AppInit.getFmConfig());
      }
      StringWriter writer = new StringWriter();
      t1.process(params, writer);
      String printContent = writer.toString();
      HtmlConverter hc = new HtmlConverter();
      if (printprefs.get("print_mode").equals("P")) {
        OutputStream os = res.getOutputStream();
        res.setContentType("application/pdf");
        hc.writePdf(os, printContent, "ClinicalLabPrint", printprefs, false, false, true, true,
            true, false);
        return null;
      } else {
        String textReport = null;
        textReport = new String(
            hc.getText(printContent, "ClinicalLabPrint", printprefs, true, true));
        req.setAttribute("textReport", textReport);
        req.setAttribute("textColumns", printprefs.get("text_mode_column"));
        req.setAttribute("printerType", "DMP");
        return am.findForward("textPrintApplet");
      }
    }
    return null;
  }

  /**
   * Prints the from ID.
   *
   * @param reportId
   *          the report id
   * @param printerId
   *          the printer id
   * @return the byte[]
   * @throws Exception
   *           the exception
   */
  public static byte[] printFromID(int reportId, int printerId) throws Exception {

    GenericPreferencesDTO dto = GenericPreferencesDAO.getGenericPreferences();
    BasicDynaBean mainList = ClinicalDataLabResutsDAO.getClinicalLabRecordedMain(reportId);

    BasicDynaBean printprefs = null;
    printprefs = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT,
        printerId);
    if (printprefs == null) {
      printprefs = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT);
    }

    Map patientDetails = new HashMap();
    GenericDocumentsFields.copyPatientDetails(patientDetails, (String) mainList.get("mrno"), null,
        false);
    Map params = new HashMap();
    params.put("patient", patientDetails);
    List<BasicDynaBean> resultList = ClinicalDataLabResutsDAO.getClinicalLabRecordsList(reportId);
    params.put("result", ConversionUtils.listBeanToListMap(resultList));
    params.put("main", mainList.getMap());
    PrintTemplatesDAO printtemplatedao = new PrintTemplatesDAO();
    String templateContent = printtemplatedao.getCustomizedTemplate(PrintTemplate.CLab);

    Template t1 = null;
    if (templateContent == null || templateContent.equals("")) {
      t1 = AppInit.getFmConfig().getTemplate(PrintTemplate.CLab.getFtlName() + ".ftl");
    } else {
      StringReader reader = new StringReader(templateContent);
      t1 = new Template("ClinicalLabPrint.ftl", reader, AppInit.getFmConfig());
    }
    StringWriter writer = new StringWriter();
    t1.process(params, writer);
    String printContent = writer.toString();
    HtmlConverter hc = new HtmlConverter();
    return hc.getPdfBytes(printContent, "ClinicalLabPrint", printprefs, false, false, true, true,
        false);
  }

}
