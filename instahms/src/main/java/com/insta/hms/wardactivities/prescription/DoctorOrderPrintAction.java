package com.insta.hms.wardactivities.prescription;

import com.bob.hms.common.Preferences;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.genericdocuments.GenericDocumentsFields;
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
 * The Class DoctorOrderPrintAction.
 *
 * @author krishna
 */
public class DoctorOrderPrintAction extends DispatchAction {

  /** The doc order DAO. */
  IPPrescriptionDAO docOrderDAO = new IPPrescriptionDAO();

  /**
   * Prints the.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws TemplateException the template exception
   * @throws XPathExpressionException the x path expression exception
   * @throws DocumentException the document exception
   * @throws TransformerException the transformer exception
   */
  public ActionForward print(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, IOException, TemplateException,
      XPathExpressionException, DocumentException, TransformerException {
    String patientId = request.getParameter("patientId");

    Map modulesActivatedMap = ((Preferences) request.getSession(false).getAttribute("preferences"))
        .getModulesActivatedMap();
    Map patientDetails = new HashMap();
    GenericDocumentsFields.copyPatientDetails(patientDetails, null, patientId, false);
    Map ftlParamMap = new HashMap();
    ftlParamMap.put("visitdetails", patientDetails);
    ftlParamMap.put("modules_activated", modulesActivatedMap);
    List list = docOrderDAO.getPrescriptions(patientId);
    ftlParamMap.put("orders", list);

    String templateContent = new PrintTemplatesDAO()
        .getCustomizedTemplate(PrintTemplate.DoctorOrder);

    Template temp = null;
    if (templateContent != null && !templateContent.equals("")) {
      StringReader reader = new StringReader(templateContent);
      temp = new Template("DoctorOrderPrintTemplate.ftl", reader, AppInit.getFmConfig());
    } else {
      temp = AppInit.getFmConfig().getTemplate("DoctorOrderPrint.ftl");
    }

    StringWriter writer = new StringWriter();
    temp.process(ftlParamMap, writer);

    String printerIdStr = request.getParameter("printerId");
    int printerId = 0;
    if ((printerIdStr != null) && !printerIdStr.equals("")) {
      printerId = Integer.parseInt(printerIdStr);
    }
    BasicDynaBean prefs = PrintConfigurationsDAO.getPageOptions(
        PrintConfigurationsDAO.PRINT_TYPE_PATIENT, printerId);

    String printMode = "P";
    if (prefs.get("print_mode") != null) {
      printMode = (String) prefs.get("print_mode");
    }

    HtmlConverter hc = new HtmlConverter();
    Boolean repeatPHeader = ((String) prefs.get("repeat_patient_info")).equals("Y");
    if (printMode.equals("P")) {
      response.setContentType("application/pdf");
      OutputStream os = response.getOutputStream();
      hc.writePdf(os, writer.toString(), "Doctor Order Print", prefs, false, repeatPHeader, true,
          true, false, false);
      os.close();

    } else {
      String textReport = new String(hc.getText(writer.toString(), "Doctor Order Print", prefs,
          true, true));
      request.setAttribute("textReport", textReport);
      request.setAttribute("textColumns", prefs.get("text_mode_column"));
      request.setAttribute("printerType", "DMP");
      return mapping.findForward("textPrintApplet");

    }
    return null;
  }
}
