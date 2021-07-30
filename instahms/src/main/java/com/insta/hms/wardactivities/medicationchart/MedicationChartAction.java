package com.insta.hms.wardactivities.medicationchart;

import com.bob.hms.common.Preferences;
import com.bob.hms.common.RequestContext;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.genericdocuments.GenericDocumentsFields;
import com.insta.hms.master.PatientHeaderTemplate.PatientHeaderTemplate;
import com.insta.hms.master.PatientHeaderTemplate.PatientHeaderTemplateDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.master.PrintTemplates.PrintTemplate;
import com.insta.hms.master.PrintTemplates.PrintTemplatesDAO;
import com.lowagie.text.DocumentException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

/**
 * The Class MedicationChartAction.
 *
 * @author nikunj.s
 */
public class MedicationChartAction extends DispatchAction {

  /** The log. */
  static Logger log = LoggerFactory
      .getLogger(MedicationChartAction.class);

  /**
   * The Enum ReturnType.
   */
  public enum ReturnType {
    
    /** The pdf. */
    PDF, 
 /** The pdf bytes. */
 PDF_BYTES, 
 /** The text bytes. */
 TEXT_BYTES
  }

  /** The ph template dao. */
  private PatientHeaderTemplateDAO phTemplateDao = new PatientHeaderTemplateDAO();
  
  /** The print template dao. */
  private PrintTemplatesDAO printTemplateDao = new PrintTemplatesDAO();

  /**
   * List.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws Exception the exception
   */
  public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws Exception {

    Map map = request.getParameterMap();
    String patientId = request.getParameter("visit_id");
    if (patientId != null && !patientId.equals("")) {
      VisitDetailsDAO regDao = new VisitDetailsDAO();
      BasicDynaBean bean = regDao.findByKey("patient_id", patientId);
      if (bean == null) {
        // user entered visit id is not a valid patient id.
        FlashScope flash = FlashScope.getScope(request);
        flash.put("error", "No Patient with Id:" + patientId);
        ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
        return redirect;
      }
      request.setAttribute(
          "medicationpagedList",
          MedicationChartDAO.getMedicationChartDetails(map,
              ConversionUtils.getListingParameter(map)));
    }
    return mapping.findForward("list");
  }

  /**
   * Prints the medication chart.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws Exception the exception
   */
  public ActionForward printMedicationChart(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {

    String patientId = request.getParameter("patientId");
    String printerId = request.getParameter("printerId");

    BasicDynaBean prefs = null;
    int printerIdInt = 0;
    if ((printerId != null) && !printerId.equals("")) {
      printerIdInt = Integer.parseInt(printerId);
    }
    prefs = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT,
        printerIdInt);

    String printMode = "P";
    if (prefs.get("print_mode") != null) {
      printMode = (String) prefs.get("print_mode");
    }
    String userName = (String) request.getSession(false).getAttribute("userid");

    if (printMode.equals("P")) {
      response.setContentType("application/pdf");
      OutputStream os = response.getOutputStream();
      getMedicationChartReport(patientId, ReturnType.PDF, prefs, os, userName);
      os.close();

    } else {
      String textReport = new String(getMedicationChartReport(patientId, ReturnType.TEXT_BYTES,
          prefs, null, userName));
      request.setAttribute("textReport", textReport);
      request.setAttribute("textColumns", prefs.get("text_mode_column"));
      request.setAttribute("printerType", "DMP");
      return mapping.findForward("textPrintApplet");
    }

    return null;
  }

  /**
   * Gets the medication chart report.
   *
   * @param patientId the patient id
   * @param enumType the enum type
   * @param prefs the prefs
   * @param os the os
   * @param userName the user name
   * @return the medication chart report
   * @throws SQLException the SQL exception
   * @throws DocumentException the document exception
   * @throws TemplateException the template exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws XPathExpressionException the x path expression exception
   * @throws TransformerException the transformer exception
   */
  public byte[] getMedicationChartReport(String patientId, ReturnType enumType,
      BasicDynaBean prefs, OutputStream os, String userName) throws SQLException,
      DocumentException, TemplateException, IOException, XPathExpressionException,
      TransformerException {

    byte[] bytes = null;
    Map patientDetails = new HashMap();
    GenericDocumentsFields.copyPatientDetails(patientDetails, null, patientId, false);
    PrintTemplate template = PrintTemplate.Medication_Chart;
    String templateContent = printTemplateDao.getCustomizedTemplate(template);

    Template temp = null;
    if (templateContent == null || templateContent.equals("")) {
      temp = AppInit.getFmConfig().getTemplate(template.getFtlName() + ".ftl");
    } else {
      StringReader reader = new StringReader(templateContent);
      temp = new Template(null, reader, AppInit.getFmConfig());
    }

    Map ftlParamMap = new HashMap();
    ftlParamMap.put("visitdetails", patientDetails);
    ftlParamMap.put("modules_activated",
        ((Preferences) RequestContext.getSession().getAttribute("preferences"))
            .getModulesActivatedMap());

    List activitiesList = MedicationChartDAO.getMedicationChartReport(patientId);
    ftlParamMap.put("medication_chart", activitiesList);
    StringWriter writer = new StringWriter();
    try {
      temp.process(ftlParamMap, writer);
    } finally {
      log.debug("Exception raised while processing the patient header for patient Id : "
          + patientId);
    }
    HtmlConverter hc = new HtmlConverter();
    String patientHeader = (String) phTemplateDao.getPatientHeader(
        (Integer) printTemplateDao.getPatientHeaderTemplateId(template),
        PatientHeaderTemplate.Medication_Chart.getType());

    Template patientHeaderTemplate = new Template("PatientHeader.ftl", new StringReader(
        patientHeader), AppInit.getFmConfig());
    Map ftlParams = new HashMap();
    ftlParams.put("visitdetails", patientDetails);
    ftlParams.put("modules_activated",
        ((Preferences) RequestContext.getSession().getAttribute("preferences"))
            .getModulesActivatedMap());
    StringWriter patientHeaderWriter = new StringWriter();
    try {
      patientHeaderTemplate.process(ftlParams, patientHeaderWriter);
    } catch (TemplateException te) {
      log.debug("Exception raised while processing the patient header for report Id : "
            + patientId);
      throw te;
    }
    StringBuilder printContent = new StringBuilder();
    printContent.append(patientHeaderWriter.toString());
    printContent.append(writer.toString());

    Boolean repeatPHeader = ((String) prefs.get("repeat_patient_info")).equals("Y");
    if (enumType.equals(ReturnType.PDF)) {
      hc.writePdf(os, printContent.toString(), "Patient Medication Chart", prefs, false,
          repeatPHeader, true, true, true, false);
      os.close();

    } else if (enumType.equals(ReturnType.PDF_BYTES)) {
      ByteArrayOutputStream stream = new ByteArrayOutputStream();
      hc.writePdf(stream, printContent.toString(), "Patient Medication Chart", prefs, false,
          repeatPHeader, true, true, true, false);
      bytes = stream.toByteArray();
      stream.close();

    } else if (enumType.equals(ReturnType.TEXT_BYTES)) {
      bytes = hc.getText(printContent.toString(), "Patient Medication Chart", prefs, true, true);

    }
    return bytes;

  }
}
