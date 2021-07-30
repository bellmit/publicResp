package com.insta.hms.diagnostics.diagnosticreportdocket;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.ftl.FtlReportGenerator;
import com.insta.hms.diagnosticmodule.common.DiagReportGenerator;
import com.insta.hms.diagnosticmodule.common.DiagnosticsDAO;
import com.insta.hms.imageretriever.DiagImageRetriever;
import com.insta.hms.master.PatientHeaderTemplate.PatientHeaderTemplate;
import com.insta.hms.master.PatientHeaderTemplate.PatientHeaderTemplateDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.master.PrintTemplates.PrintTemplate;
import com.insta.hms.master.PrintTemplates.PrintTemplatesDAO;
import com.insta.hms.modules.ModulesDAO;

import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DiagnosticReportDocketAction extends Action {
  static Logger logger = LoggerFactory
      .getLogger(DiagnosticReportDocketAction.class);

  private static final String REPORT_ID = "report_id";

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping,
   * org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest,
   * javax.servlet.http.HttpServletResponse)
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Override
  public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws Exception {

    String visitId = request.getParameter("visit_id");
    String thisGivenErrorUrl = request.getParameter("error_url");// can be null
    String scheema = request.getParameter("schema");
    // this should be a hidden parameter in the form or request parameter directly given in url
    // setting db details,centerid
    String[] dbSchema = new String[] { null, null, scheema, "", "0", "" };
    RequestContext.setConnectionDetails(dbSchema);// setting scheema
    boolean valid = true;
    Map<String, Object> identifiers = new HashMap<>();
    identifiers.put("patient_id", visitId);
    List<String> columns = new ArrayList<>();
    columns.add("docs_download_passcode");
    Map visitDetails = new GenericDAO("patient_registration").findByKey(columns, identifiers)
        .getMap();

    valid &= (visitDetails != null);// check visitid
    String passcode = request.getParameter("passcode");
    valid &= (passcode != null);// check passcode
    if (visitDetails != null && valid) {
      valid &= ((Integer) visitDetails.get("docs_download_passcode") == Integer.parseInt(passcode));
    }
    BasicDynaBean modDownloadReportBean = new ModulesDAO().findByKey("module_id",
        "mod_download_reports");
    boolean moduleActiveStatus = (modDownloadReportBean != null
        && modDownloadReportBean.get("activation_status").equals("Y"));
    // check passcode
    if (!valid || !moduleActiveStatus) {
      response.sendRedirect(thisGivenErrorUrl == null
          ? request.getContextPath() + "/patientReportAccessFailure.do" : thisGivenErrorUrl);
      return null;// redirecting to an error page
    }
    // string of all signed-off reports of the visit
    List<BasicDynaBean> signedOffReports = DiagnosticsDAO.getReportList(visitId, true);

    String reportString = "";
    boolean isFirstReport = true;
    int collCenterID = -1;
    int reportId = 0;
    for (BasicDynaBean signedOffReport : signedOffReports) {
      reportId = (Integer) signedOffReport.get(REPORT_ID);
      if (!isFirstReport) {
        // start each report in new page.
        reportString = reportString.concat("<p class='pagebreak'/>");
      }
      reportString = reportString
          .concat(DiagReportGenerator.getReport((Integer) signedOffReport.get(REPORT_ID), "patient",
              request.getContextPath(), true, false));
      isFirstReport = false;
      if (collCenterID == -1) {
        collCenterID = DiagReportGenerator
            .getReportPrintCenter((Integer) signedOffReport.get(REPORT_ID), "col");
      }
    }
    
    String pendingTests = "";
    String skipPendingTests = request.getParameter("skip_pending_tests");
    Boolean skipPending = skipPendingTests != null && skipPendingTests.equalsIgnoreCase("true");
    if (!skipPending) {
      pendingTests = new PendingTestsReportGenerator().getPendingTestsReport(visitId);
      // display the pending tests in next page.
      if (!reportString.isEmpty() && pendingTests != null && !pendingTests.isEmpty()) {
        reportString = reportString.concat("<p class='pagebreak'/>");
      }
    }

    if (collCenterID == -1) {
      collCenterID = DiagnosticsDAO.getPendingTestsCollectioncenterid(visitId);
    }

    reportString = reportString.concat(pendingTests);
    PatientHeaderTemplateDAO phTemplateDAO = new PatientHeaderTemplateDAO();
    PrintTemplate template = PrintTemplate.WebLab;
    PrintTemplatesDAO printTemplateDao = new PrintTemplatesDAO();
    FtlReportGenerator ftlGen = null;

    String patientHeader = phTemplateDAO.getPatientHeader(
        (Integer) printTemplateDao.getPatientHeaderTemplateId(template),
        PatientHeaderTemplate.WebBased.getType());
    ftlGen = new FtlReportGenerator("PatientHeader", new StringReader(patientHeader));
    Map ftlParams = new HashMap();
    ftlParams.put("visitDetails", DiagReportGenerator.getPatientDetailsMap(visitId, reportId));
    StringWriter writer = new StringWriter();
    try {
      ftlGen.setReportParams(ftlParams);
      ftlGen.process(writer);
    } catch (TemplateException te) {
      logger.debug("Exception raised while processing the patient header for report Id : " + 0);
      throw te;
    }
    StringBuilder printContent = new StringBuilder();
    printContent.append(writer.toString());

    reportString = reportString.replace(request.getContextPath() + "/images/strikeoff.png",
        AppInit.getServletContext().getRealPath("/images/strikeoff.png"));
    // for background image of amended report
    if (reportString.isEmpty()) {
      reportString = "<div style='text-align: center; font-weight:bold; font-size:25px;'>"
          + "Some tests are still in progress. "
          + "Report will be available once all tests are completed.</div>";
    }
    printContent.append(reportString);
    response.setContentType("application/pdf");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    response.setDateHeader("Expires", 0);
    BasicDynaBean printPrefs = PrintConfigurationsDAO
        .getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_WEB_DIAG, 0, collCenterID);
    OutputStream os = response.getOutputStream();
    HtmlConverter hc = new HtmlConverter(new DiagImageRetriever());
    hc.writePdf(os, printContent.toString(), "Investigation Report", printPrefs, false,
        ((String) printPrefs.get("repeat_patient_info")).equals("Y"), true, true, true, false,
        collCenterID);

    os.close();
    return null;

  }

}
