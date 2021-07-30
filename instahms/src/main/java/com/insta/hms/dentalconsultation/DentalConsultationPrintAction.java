package com.insta.hms.dentalconsultation;

import com.bob.hms.common.Preferences;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.genericdocuments.GenericDocumentsFields;
import com.insta.hms.imageretriever.DoctorConsultImageRetriever;
import com.insta.hms.master.CommonPrintTemplates.PrintTemplatesDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.PrescriptionsMaster.PrescriptionsMasterDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.outpatient.DentalChartHelperDAO;
import com.insta.hms.outpatient.ToothImageDetails;
import com.lowagie.text.DocumentException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

public class DentalConsultationPrintAction extends DispatchAction {

  /**
   * Prints the.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws ServletException the servlet exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ParseException the parse exception
   * @throws DocumentException the document exception
   * @throws XPathExpressionException the x path expression exception
   * @throws TransformerException the transformer exception
   * @throws TemplateException the template exception
   */
  public ActionForward print(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response)
      throws SQLException, ServletException, IOException, ParseException, DocumentException,
      XPathExpressionException, TransformerException, TemplateException {
    final String mrNo = request.getParameter("mr_no");
    String printerIdStr = request.getParameter("printerId");
    BasicDynaBean prefs = null;
    int printerId = 0;
    if ((printerIdStr != null) && !printerIdStr.equals("")) {
      printerId = Integer.parseInt(printerIdStr);
    }
    prefs = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT,
        printerId);

    String printMode = "P";
    if (prefs.get("print_mode") != null) {
      printMode = (String) prefs.get("print_mode");
    }
    Map patientDetails = new HashMap();
    GenericDocumentsFields.copyPatientDetails(patientDetails, mrNo, null, false);
    Map ftlParamMap = new HashMap();
    ftlParamMap.put("patientdetails", patientDetails);

    String orgId = "ORG0001";
    String bedType = "GENERAL";
    ToothTreatmentDetailsDao trtmtDao = new ToothTreatmentDetailsDao();
    List<BasicDynaBean> treatments = trtmtDao.getTreatmentDetails(mrNo, orgId, bedType);

    BasicDynaBean genericPrefs = GenericPreferencesDAO.getAllPrefs();
    ftlParamMap.put("treatments", treatments);

    HashMap serviceSubTasks = ConversionUtils
        .listBeanToMapListBean(trtmtDao.getSavedServiceSubTasksForPatient(mrNo), "unique_id");
    ftlParamMap.put("service_sub_tasks", serviceSubTasks);

    ftlParamMap.put("dentalChartPref", (String) genericPrefs.get("dental_chart"));
    PatientDentalConditionDao patDentlDao = new PatientDentalConditionDao();
    ftlParamMap.put("dental_chart_markers", patDentlDao.getMarkerImageDetails(mrNo));
    ToothImageDetails toothImageDetails = DentalChartHelperDAO.getToothImageDetails(true);
    ftlParamMap.put("tooth_image_details", toothImageDetails);

    Map modulesActivatedMap = ((Preferences) RequestContext.getSession()
        .getAttribute("preferences")).getModulesActivatedMap();
    String useStoreItems = (String) genericPrefs.get("prescription_uses_stores");
    List allPrescriptions = PrescriptionsMasterDAO.getPrescriptionsForPatient(mrNo,
        useStoreItems);
    ftlParamMap.put("presMedicines",
        PrescriptionsMasterDAO.getPrescribedItems(allPrescriptions, "Medicine"));
    ftlParamMap.put("presTests",
        PrescriptionsMasterDAO.getPrescribedItems(allPrescriptions, "Inv."));
    ftlParamMap.put("presServices",
        PrescriptionsMasterDAO.getPrescribedItems(allPrescriptions, "Service"));

    String templateName = request.getParameter("printTemplate");
    if (templateName == null || templateName.equals("")) {
      templateName = (String) genericPrefs.get("default_dental_cons_print_template");
    }
    Template template = null;
    String templateMode = null;
    if (templateName.equals("BUILTIN_HTML")) {
      template = AppInit.getFmConfig().getTemplate("DentalTreatmentDetails.ftl");
      templateMode = "H";
    } else if (templateName.equals("BUILTIN_TEXT")) {
      template = AppInit.getFmConfig().getTemplate("DentalTreatmentDetailsText.ftl");
      templateMode = "T";
    } else {
      BasicDynaBean pbean = PrintTemplatesDAO.getTemplateContent(templateName);

      if (pbean == null) {
        return null;
      }
      String templateContent = (String) pbean.get("template_content");
      templateMode = (String) pbean.get("template_mode");
      StringReader reader = new StringReader(templateContent);
      template = new Template("DentalConsultationPrint.ftl", reader, AppInit.getFmConfig());
    }
    StringWriter writer = new StringWriter();
    template.process(ftlParamMap, writer);

    HtmlConverter hc = new HtmlConverter(new DoctorConsultImageRetriever());
    Boolean repeatPHeader = ((String) prefs.get("repeat_patient_info")).equals("Y");

    if (printMode.equals("P")) {
      response.setContentType("application/pdf");
      OutputStream os = response.getOutputStream();
      if (templateMode != null && templateMode.equals("T")) {
        hc.textToPDF(writer.toString(), os, prefs);
      } else {
        hc.writePdf(os, writer.toString(), "Dental Treatment Details", prefs, false, repeatPHeader,
            true, true, false, false);
      }
      os.close();

    } else {
      String textReport = null;
      if (templateMode != null && templateMode.equals("T")) {
        textReport = new String(writer.toString().getBytes());
      } else {
        textReport = new String(
            hc.getText(writer.toString(), "Dental Treatment Details", prefs, true, true));
      }
      request.setAttribute("textReport", textReport);
      request.setAttribute("textColumns", prefs.get("text_mode_column"));
      request.setAttribute("printerType", "DMP");
      return mapping.findForward("textPrintApplet");
    }
    return null;
  }

}
