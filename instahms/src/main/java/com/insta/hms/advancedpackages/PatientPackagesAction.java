package com.insta.hms.advancedpackages;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.diag.ohsampleregistration.OhSampleRegistrationDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO: Auto-generated Javadoc
/**
 * The Class PatientPackagesAction.
 */
public class PatientPackagesAction extends BaseAction {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(PatientPackagesAction.class);
  
  private static final GenericDAO packagePrescribed = new GenericDAO("package_prescribed");
  
  /** The Constant patient_package_details. */
  private static final String patient_package_details = "SELECT * FROM ("
      + " SELECT pp.package_id,CASE WHEN pcd.panel_id is not null THEN "
      + " concat(d.test_name, ': ', pap.package_name) WHEN pcd.activity_type ='Doctor' "
      + " THEN 'Doctor' "
      + " ELSE coalesce(d.test_name, s.service_name) END as activity_description ,"
      + " pp.presc_date "
      + " as presc_timestamp,pp.presc_date,"
      + " (CASE WHEN pcd.activity_type IN ('Laboratory','Radiology') THEN tp.conducted"
      + "      WHEN pcd.activity_type = 'Service' THEN sp.conducted"
      + "      WHEN pcd.activity_type = 'Doctor' THEN dp.status END ) as activity_status,"
      + " (CASE WHEN pcd.activity_type IN ('Laboratory','Radiology') THEN tc.conducted_date"
      + "      WHEN pcd.activity_type = 'Service' THEN sp.conducteddate"
      + "  WHEN pcd.activity_type = 'Doctor' THEN dp.consultation_complete_time END ) "
      + " as activity_conducted_date,"
      + " tvr.handed_over as test_report_handed_over,pcd.activity_type,d.results_entry_applicable "
      + " FROM package_prescribed pp " + " JOIN packages pm USING(package_id) "
      + " JOIN package_contents pcd USING(package_id) "
      + " LEFT JOIN tests_prescribed tp ON (tp.package_ref = pp.prescription_id"
      + " AND tp.test_id = pcd.activity_id AND pcd.activity_type IN ('Laboratory','Radiology')) "
      + " LEFT JOIN packages pap ON (pap.package_id=pcd.panel_id) "
      + " LEFT JOIN diagnostics d ON(d.test_id = tp.test_id) "
      + " LEFT JOIN tests_conducted tc ON ( tc.prescribed_id = tp.prescribed_id)"
      + " LEFT JOIN test_visit_reports tvr USING(report_id) "
      + " LEFT JOIN services_prescribed sp ON (sp.package_ref = pp.prescription_id"
      + " AND sp.service_id = pcd.activity_id AND pcd.activity_type = 'Service') "
      + " LEFT JOIN doctor_consultation dp ON (dp.package_ref = pp.prescription_id "
      + " AND pcd.activity_type = 'Doctor')  "
      + " LEFT JOIN services s ON(s.service_id = sp.service_id)"
      + "WHERE pp.prescription_id = ? " + " UNION "
      + " SELECT pp.package_id,opm.operation_name as activity_description,pp.presc_date "
      + " as presc_timestamp,pp.presc_date,opp.status as activity_status,"
      + "   opp.end_datetime as activity_conducted_date,null as test_report_handed_over,"
      + " 'OT' as activity_type,true as results_entry_applicable "
      + " FROM package_prescribed pp " + " JOIN packages pm USING(package_id) "
      + " JOIN package_contents pc ON pc.package_id=pm.package_id "
      + " JOIN operation_master opm ON( pc.operation_id = opm.op_id )"
      + " JOIN bed_operation_schedule opp ON (opp.package_ref = pp.prescription_id) "
      + "WHERE pp.prescription_id = ?" + ") AS foo WHERE foo.activity_status NOT IN ('RBS','RAS') ";

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
    req.setAttribute("pagedList", dao.getPatientPackages(params, listing, centerId));

    return mapping.findForward("list");
  }

  /**
   * Gets the patient package details.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the patient package details
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws Exception the exception
   */
  public ActionForward getPatientPackageDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res)
      throws IOException, ServletException, Exception {

    String prescriptionId = req.getParameter("prescription_id");
    String patientId = req.getParameter("patient_id");
    Connection con = null;
    PreparedStatement ps = null;
    try {

      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(patient_package_details);
      ps.setInt(1, Integer.parseInt(prescriptionId));
      ps.setInt(2, Integer.parseInt(prescriptionId));
      req.setAttribute("package_conduction_details", DataBaseUtil.queryToDynaList(ps));

      BasicDynaBean patientDetails = VisitDetailsDAO.getPatientVisitDetailsBean(patientId);
      if (patientDetails != null) {
        req.setAttribute("patient", patientDetails);
      } else {
        req.setAttribute("incoming_patient",
            OhSampleRegistrationDAO.getIncomingCustomer(patientId));
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return mapping.findForward("patientpackagedetails");
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
      if (centerId == -1) {
        centerId = DiagReportGenerator.getReportPrintCenter((Integer) test.get("report_id"), "col");
      }
    }

    BasicDynaBean printPref = PrintConfigurationsDAO
        .getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_DIAG, 0, centerId);
    ByteArrayOutputStream testOs = new ByteArrayOutputStream();
    PdfReader reader = null;
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

  /**
   * Complete package.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward completePackage(ActionMapping mapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws SQLException, Exception {
    FlashScope flash = FlashScope.getScope(req);
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    redirect.addParameter("mr_no", req.getParameter("mr_no"));

    Map paramMap = getParameterMap(req);

    String[] prescriptionId = (String[]) paramMap.get("_prescription_id");
    BasicDynaBean packagePrescBean = null;
    Connection con = null;
    boolean success = true;
    try {

      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);

      if (prescriptionId != null) {
        for (int i = 0; i < prescriptionId.length; i++) {

          packagePrescBean = packagePrescribed.findByKey("prescription_id",
              Integer.parseInt(prescriptionId[i]));
          packagePrescBean.set("status", "C");
          packagePrescBean.set("completion_time", DataBaseUtil.getDateandTime());
          packagePrescBean.set("completion_by",
              (String) req.getSession(false).getAttribute("userid"));

          success &= (packagePrescribed.update(con, packagePrescBean.getMap(), "prescription_id",
              Integer.parseInt(prescriptionId[i])) > 0);
        }
      }
    } finally {
      DataBaseUtil.commitClose(con, success);
    }

    return redirect;
  }

  /**
   * Gets the patient package handover screen.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the patient package handover screen
   * @throws SQLException the SQL exception
   * @throws Exception the exception
   */
  public ActionForward getPatientPackageHandoverScreen(ActionMapping mapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws SQLException, Exception {

    req.setAttribute("packageDetails", new PatientPackagesDAO()
        .getPackageDetails(Integer.parseInt(req.getParameter("prescription_id"))));
    BasicDynaBean patientDetails = VisitDetailsDAO
        .getPatientVisitDetailsBean(req.getParameter("patient_id"));
    if (patientDetails != null) {
      req.setAttribute("patient", patientDetails);
    } else {
      req.setAttribute("incoming_patient",
          OhSampleRegistrationDAO.getIncomingCustomer(req.getParameter("patient_id")));
    }
    return mapping.findForward("packagehandover");
  }

  /**
   * Hand over package.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws Exception the exception
   */
  public ActionForward handOverPackage(ActionMapping mapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws SQLException, Exception {
    FlashScope flash = FlashScope.getScope(req);
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    redirect.addParameter("mr_no", req.getParameter("mr_no"));

    GenericDAO testsDao = new GenericDAO("tests_prescribed");
    GenericDAO testReportDao = new GenericDAO("test_visit_reports");

    Connection con = null;
    boolean success = true;

    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      BasicDynaBean packagePrescBean = packagePrescribed.getBean();
      ConversionUtils.copyToDynaBean(req.getParameterMap(), packagePrescBean);

      packagePrescBean.set("completion_time", packagePrescBean.get("handover_time"));
      packagePrescBean.set("status", "C");
      packagePrescBean.set("completion_by", (String) req.getSession(false).getAttribute("userid"));

      success = (packagePrescribed.update(con, packagePrescBean.getMap(), "prescription_id",
          (Integer) packagePrescBean.get("prescription_id")) > 0);

      // handover test report (if any) too.

      List<BasicDynaBean> tests = testsDao.findAllByKey("package_ref",
          (Integer) packagePrescBean.get("prescription_id"));

      for (BasicDynaBean testBean : tests) {

        if (testBean.get("report_id") == null) {
          continue;
        }
        BasicDynaBean testReportBean = testReportDao.findByKey("report_id",
            (Integer) testBean.get("report_id"));
        testReportBean.set("handed_over", "Y");
        testReportBean.set("handed_over_to", packagePrescBean.get("handover_to"));
        testReportBean.set("hand_over_time", packagePrescBean.get("handover_time"));
        success &= (testReportDao.update(con, testReportBean.getMap(), "report_id",
            (Integer) testBean.get("report_id")) > 0);
      }
    } finally {
      DataBaseUtil.commitClose(con, success);
    }

    return redirect;
  }

}
