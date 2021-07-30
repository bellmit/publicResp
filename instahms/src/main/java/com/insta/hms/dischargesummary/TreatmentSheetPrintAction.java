package com.insta.hms.dischargesummary;

import com.bob.hms.common.Preferences;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.genericdocuments.GenericDocumentsFields;
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
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

// TODO: Auto-generated Javadoc
/**
 * The Class TreatmentSheetPrintAction.
 *
 * @author krishna
 */
public class TreatmentSheetPrintAction extends DispatchAction {

  /** The log. */
  static Logger log = LoggerFactory.getLogger(TreatmentSheetPrintAction.class);
  
  /** The ph template dao. */
  static PatientHeaderTemplateDAO phTemplateDao = new PatientHeaderTemplateDAO();

  /** The print template dao. */
  static PrintTemplatesDAO printTemplateDao = new PrintTemplatesDAO();

  /**
   * Prints the.
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
   * @throws DocumentException
   *           the document exception
   * @throws XPathExpressionException
   *           the x path expression exception
   * @throws TransformerException
   *           the transformer exception
   */
  public ActionForward print(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException, SQLException,
      TemplateException, DocumentException, XPathExpressionException, TransformerException {
    String printerIdStr = request.getParameter("printerId");
    String patientId = request.getParameter("patient_id");
    int printerId = 0;
    BasicDynaBean prefs = null;

    if ((printerIdStr != null) && !printerIdStr.equals("")) {
      printerId = Integer.parseInt(printerIdStr);
    }
    prefs = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_DISCHARGE,
        printerId);
    Boolean repeatPatientHeader = ((String) prefs.get("repeat_patient_info")).equals("Y");

    String patientHeader = phTemplateDao.getPatientHeader(
        printTemplateDao.getPatientHeaderTemplateId(PrintTemplate.TreatmentSheet), "TSheet");
    Map<String, String> patientDetailsMap = new HashMap<>();
    GenericDocumentsFields.copyPatientDetails(patientDetailsMap, null, patientId, false);
    StringReader reader = new StringReader(patientHeader);
    Template temp = new Template("PatientHeader.ftl", reader, AppInit.getFmConfig());
    Map templateMap = new HashMap();
    templateMap.put("visitdetails", patientDetailsMap);
    templateMap.put("modules_activated",
        ((Preferences) RequestContext.getSession().getAttribute("preferences"))
            .getModulesActivatedMap());
    StringWriter writer = new StringWriter();

    try {
      temp.process(templateMap, writer);
    } catch (TemplateException te) {
      log.error("", te);
      throw te;
    }
    String treatmentContent = new DischargeSummaryReportHelper().processTreatment(patientId);
    StringBuilder reportContent = new StringBuilder();
    reportContent.append(writer.toString());
    reportContent.append(treatmentContent);
    HtmlConverter hc = new HtmlConverter();
    String printMode = (String) prefs.get("print_mode");
    if (printMode.equals("P")) {
      OutputStream os = response.getOutputStream();
      response.setContentType("application/pdf");
      hc.writePdf(os, reportContent.toString(), "Treatment Sheet", prefs, false,
          repeatPatientHeader, true, true, false, false);
      os.close();
      return null;
    } else {
      String textReport = new String(hc.getText(reportContent.toString(), "Treatment Sheet", prefs,
          true, true));
      request.setAttribute("textReport", textReport);
      request.setAttribute("textColumns", prefs.get("text_mode_column"));
      request.setAttribute("printerType", "DMP");
      return mapping.findForward("textPrintApplet");
    }
  }
}
