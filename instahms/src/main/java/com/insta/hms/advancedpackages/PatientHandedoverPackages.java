package com.insta.hms.advancedpackages;

import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.diagnosticmodule.common.DiagReportGenerator;
import com.insta.hms.emr.OperationProviderBOImpl;
import com.insta.hms.emr.ServiceProviderBOImpl;
import com.insta.hms.imageretriever.DiagImageRetriever;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.lowagie.text.pdf.PdfCopyFields;
import com.lowagie.text.pdf.PdfReader;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO: Auto-generated Javadoc
/**
 * The Class PatientHandedoverPackages.
 */
public class PatientHandedoverPackages extends BaseAction {
  
  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(PatientHandedoverPackages.class);

  /**
   * List.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws IOException, ServletException, Exception {

    PatientPackagesDAO dao = new PatientPackagesDAO();
    Map params = getParameterMap(req);
    Map listing = ConversionUtils.getListingParameter(params);
    int centerId = (Integer) req.getSession().getAttribute("centerId");
    String tpaId = (String[]) params.get("_tpa_id") != null ? ((String[]) params.get("_tpa_id"))[0]
        : null;
    req.setAttribute("pagedList", dao.getPatientPackages(params, listing, centerId, "Y", tpaId));

    return mapping.findForward("list");
  }

  /**
   * Download documents.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws Exception the exception
   */
  public ActionForward downloadDocuments(ActionMapping mapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws SQLException, Exception {
    FlashScope flash = FlashScope.getScope(req);
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    res.setContentType("application/pdf");

    int prescId = Integer.parseInt(req.getParameter("prescription_id"));

    ServiceProviderBOImpl serviceReportProvider = new ServiceProviderBOImpl();
    OperationProviderBOImpl otReportProvider = new OperationProviderBOImpl();

    List<BasicDynaBean> packageTests = PackageTestsDAO.getSignedOffPackageTestReports(prescId);

    BasicDynaBean packageDetails = new PatientPackagesDAO().getPackageDetails(prescId);

    res.setContentType("application/pdf");

    byte[] pdfBytes = null;
    HtmlConverter hc = new HtmlConverter(new DiagImageRetriever());
    BasicDynaBean genericPrefs = GenericPreferencesDAO.getPrefsBean();

    logger.debug("Test Report of Package: " + packageDetails.get("package_name"));
    String reportString = "";
    int centerId = -1;
    for (BasicDynaBean test : packageTests) {

      reportString = reportString.concat(DiagReportGenerator.getReport(
          (Integer) test.get("report_id"), "patient", req.getContextPath(), false, false));
      // Use collection center always for print preferences and logo\header info for
      // collection of test reports.
      if (centerId == -1) {
        centerId = DiagReportGenerator.getReportPrintCenter((Integer) test.get("report_id"), "col");
      }

    }

    BasicDynaBean printPref = PrintConfigurationsDAO
        .getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_DIAG, 0, centerId);


    PdfReader reader = null;
    ByteArrayOutputStream testOs = new ByteArrayOutputStream();
    hc.writePdf(testOs, reportString, "Investigation Report", printPref, false, false, true, true,
        true, false, centerId);
    reader = new PdfReader(new ByteArrayInputStream(testOs.toByteArray()));
    ;
    OutputStream stream = res.getOutputStream();
    PdfCopyFields copy = new PdfCopyFields(stream);
    copy.addDocument(reader);// test reports to the pdf
    List<BasicDynaBean> packageServices = PackageServicesDAO
        .getSignedOffPackageServiceReports(prescId);
    logger.debug("Service Report of Package: " + packageDetails.get("package_name"));
    for (BasicDynaBean service : packageServices) {
      pdfBytes = serviceReportProvider.getPDFBytes(service.get("doc_id").toString(), 0);
      reader = new PdfReader(new ByteArrayInputStream(pdfBytes));
      copy.addDocument(reader);
    }
    List<BasicDynaBean> packageOperations = PackageOperationsDAO
        .getSignedOffPackageOTReports(prescId);
    logger.debug("Operation Report of Package: " + packageDetails.get("package_name"));
    for (BasicDynaBean operation : packageOperations) {
      pdfBytes = otReportProvider.getPDFBytes(operation.get("doc_id").toString(), 0);
      reader = new PdfReader(new ByteArrayInputStream(pdfBytes));
      copy.addDocument(reader);
    }

    copy.close();
    stream.flush();
    stream.close();
    return null;
  }

}
