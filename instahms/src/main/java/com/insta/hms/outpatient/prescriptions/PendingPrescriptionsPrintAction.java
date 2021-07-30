package com.insta.hms.outpatient.prescriptions;

import com.bob.hms.common.Preferences;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.ftl.FtlReportGenerator;
import com.insta.hms.genericdocuments.GenericDocumentsFields;
import com.insta.hms.imageretriever.DoctorConsultImageRetriever;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.master.PrintTemplates.PrintTemplate;
import com.insta.hms.master.PrintTemplates.PrintTemplatesDAO;
import com.lowagie.text.DocumentException;

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
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.xpath.XPathExpressionException;

/**
 * The Class PendingPrescriptionsPrintAction.
 *
 * @author krishna.t
 */
public class PendingPrescriptionsPrintAction extends DispatchAction {

  /** The log. */
  static final Logger log = LoggerFactory.getLogger(PendingPrescriptionsPrintAction.class);

  /** The dao. */
  static final PatientPendingPrescriptionsDAO dao = new PatientPendingPrescriptionsDAO();

  /**
   * Prints the.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws Exception the exception
   */
  public ActionForward print(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    String patientId = request.getParameter("patient_id");
    String consIdStr = request.getParameter("consultation_id");
    int consultationId = 0;
    if (consIdStr != null && !consIdStr.equals("")) {
      consultationId = Integer.parseInt(consIdStr);
    }

    Map patientDetails = new HashMap();
    GenericDocumentsFields.copyPatientDetails(patientDetails, null, patientId, false);
    Map ftlParamMap = new HashMap();

    ftlParamMap.put("visitdetails", patientDetails);
    Map modulesActivatedMap = ((Preferences) RequestContext.getSession()
        .getAttribute("preferences")).getModulesActivatedMap();
    ftlParamMap.put("modules_activated", modulesActivatedMap);
    String prescType = request.getParameter("prescType");
    String category = request.getParameter("category");
    List prescList = dao.getPrescriptions(consultationId, patientId, prescType, category);
    ftlParamMap.put("prescriptions", prescList);

    HtmlConverter hc = new HtmlConverter(new DoctorConsultImageRetriever());
    BasicDynaBean prefs;
    String printerId = request.getParameter("printerId");
    if ((printerId != null) && !printerId.equals("")) {
      prefs = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT,
          Integer.parseInt(request.getParameter("printerId")));
    } else {
      // use the default printer
      prefs = PrintConfigurationsDAO.getPatientDefaultPrintPrefs();
    }

    String printMode = "P";
    String forcePdf = request.getParameter("forcePdf");
    if ((forcePdf == null) || !forcePdf.equals("true")) {
      // use the print mode selected.
      printMode = (String) prefs.get("print_mode");
    }
    PrintTemplate template = PrintTemplate.PENDING_PRESCRIPTION;

    String templateContent = new PrintTemplatesDAO().getCustomizedTemplate(template);
    FtlReportGenerator ftlGen = null;
    if (templateContent == null || templateContent.equals("")) {
      ftlGen = new FtlReportGenerator(template.getFtlName());
    } else {
      StringReader reader = new StringReader(templateContent);
      ftlGen = new FtlReportGenerator(template.getFtlName(), reader);
    }
    StringWriter writer = new StringWriter();
    ftlGen.setReportParams(ftlParamMap);
    ftlGen.process(writer);

    Boolean repeatPHeader = ((String) prefs.get("repeat_patient_info")).equals("Y");
    if (printMode.equals("T")) {

      // convert from HTML to text
      String textContent = new String(
          hc.getText(writer.toString(), "Pending Patient Prescriptions Print", prefs, true, true));

      request.setAttribute("textReport", textContent);
      request.setAttribute("textColumns", prefs.get("text_mode_column"));
      request.setAttribute("printerType", "DMP");
      return mapping.findForward("textPrintApplet");

    } else {
      OutputStream os = response.getOutputStream();
      response.setContentType("application/pdf");
      try {
        // convert html to PDF
        hc.writePdf(os, writer.toString(), "Pending Patient Prescriptions Print", prefs, false,
            repeatPHeader, true, true, true, false);
      } catch (IOException | SQLException | DocumentException | XPathExpressionException exe) {
        log.error("Generated HTML content:");
        log.error(writer.toString());
        throw (exe);
      } finally {
        os.close();
      }
      return null;
    }
  }
}
