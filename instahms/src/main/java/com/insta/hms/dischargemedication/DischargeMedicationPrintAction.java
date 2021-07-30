package com.insta.hms.dischargemedication;

import com.insta.hms.common.HtmlConverter;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.lowagie.text.DocumentException;

import flexjson.JSONSerializer;
import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.text.ParseException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO: Auto-generated Javadoc
/**
 * The Class DischargeMedicationPrintAction.
 *
 * @author krishna
 */
public class DischargeMedicationPrintAction extends DispatchAction {

  /** The js. */
  final JSONSerializer js = new JSONSerializer().exclude("class");
  
  /** The report helper. */
  final DischargeMedicationReportHelper reportHelper = new DischargeMedicationReportHelper();

  /**
   * Discharge medication print.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws TemplateException the template exception
   * @throws DocumentException the document exception
   * @throws ParseException the parse exception
   * @throws Exception the exception
   */
  public ActionForward dischargeMedicationPrint(
      ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res)
      throws SQLException, IOException, TemplateException, DocumentException, ParseException,
          Exception {

    String printerIdStr = req.getParameter("printerId");
    String patientId = req.getParameter("patient_id");

    Integer printerId = null;
    if ((printerIdStr != null) && !printerIdStr.equals("")) {
      printerId = Integer.parseInt(printerIdStr);
    }

    BasicDynaBean printPref =
        PrintConfigurationsDAO.getPageOptions(
            PrintConfigurationsDAO.PRINT_TYPE_DISCHARGE, printerId);
    String printMode = "P";
    if (printPref.get("print_mode") != null) {
      printMode = (String) printPref.get("print_mode");
    }

    HtmlConverter hc = new HtmlConverter();
    Boolean repeatPHeader = ((String) printPref.get("repeat_patient_info")).equals("Y");

    if (printMode.equals("P")) {
      res.setContentType("application/pdf");
      OutputStream os = res.getOutputStream();
      String pdfReport = reportHelper.getDischargeMedicationSheet(patientId);
      hc.writePdf(
          os,
          pdfReport,
          "Discharge Medication",
          printPref,
          false,
          repeatPHeader,
          true,
          true,
          true,
          false);
      os.close();

    } else {
      String textReport =
          new String(
              hc.getText(
                  reportHelper.getDischargeMedicationSheet(patientId),
                  "Discharge Medication",
                  printPref,
                  true,
                  true));
      req.setAttribute("textReport", textReport);
      req.setAttribute("textColumns", printPref.get("text_mode_column"));
      req.setAttribute("printerType", "DMP");
      return mapping.findForward("textPrintApplet");
    }
    return null;
  }
}
