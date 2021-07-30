package com.insta.hms.dischargesummary;

import com.bob.hms.common.APIUtility;
import com.bob.hms.common.Preferences;
import com.insta.hms.common.FlashScope;
import com.insta.hms.genericdocuments.DocumentPrintConfigurationsDAO;
import com.lowagie.text.DocumentException;

import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

/**
 * The Class DischargeSummaryPrintAction.
 *
 * @author krishna.t
 */
public class DischargeSummaryPrintAction extends DispatchAction {
  
  /** The dao. */
  static DischargeSummaryDAOImpl dao = new DischargeSummaryDAOImpl();

  /**
   * Prints the.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws ServletException the servlet exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   * @throws XPathExpressionException the x path expression exception
   * @throws TransformerException the transformer exception
   * @throws TemplateException the template exception
   * @throws DocumentException the document exception
   */
  public ActionForward print(
      ActionMapping mapping,
      ActionForm form,
      HttpServletRequest request,
      HttpServletResponse response)
      throws ServletException, IOException, SQLException, XPathExpressionException,
          TransformerException, TemplateException, DocumentException {

    String error =
        APIUtility.setConnectionDetails(
            servlet.getServletContext(), request.getParameter("request_handler_key"));
    if (error != null) {
      APIUtility.setInvalidLoginError(response, error);
      return null;
    }

    String patientId = request.getParameter("patient_id");
    BasicDynaBean dd = dao.getDischargeDetails(patientId);
    FlashScope flash = FlashScope.getScope(request);
    if (dd == null) {
      flash.put("error", "No patient discharge report available for " + patientId);
      ActionRedirect redirect = new ActionRedirect("failureRedirect");
      redirect.addParameter("patient_id", patientId);
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      return mapping.findForward("error");
    }
    int docid = (Integer) dd.get("discharge_doc_id");
    Boolean forcePDF = new Boolean(request.getParameter("forcePdf"));
    String printerIdStr = request.getParameter("printerId");
    Integer printerId = null;
    BasicDynaBean prefs = null;

    if ((printerIdStr != null) && !printerIdStr.equals("")) {
      printerId = Integer.parseInt(printerIdStr);
    }

    prefs = DocumentPrintConfigurationsDAO.getDischargeSummaryPreferences(printerId);

    String printMode = (String) prefs.get("print_mode");
    if (forcePDF) {
      printMode = "P";
    }

    // api parameter
    String logoHeader = request.getParameter("logoHeader");
    if (logoHeader != null
        && !logoHeader.equals("")
        && (logoHeader.equalsIgnoreCase("Y")
            || logoHeader.equalsIgnoreCase("L")
            || logoHeader.equalsIgnoreCase("H")
            || logoHeader.equalsIgnoreCase("N"))) {
      prefs.set("logo_header", logoHeader.toUpperCase());
    }

    DischargeSummaryReportHelper reportHelper = new DischargeSummaryReportHelper();
    
    String format = (String) dd.get("discharge_format");

    if (format.equals("F") || format.equals("T")) {
      DischargeSummaryReportHelper.FormatType formatType =
          format.equals("F")
              ? DischargeSummaryReportHelper.FormatType.HVF
              : DischargeSummaryReportHelper.FormatType.RICH_TEXT;
      Preferences sessionPrefs = APIUtility.getPreferences();

      if (printMode.equals("P")) {
        // PDF mode: convert the html to pdf and send:
        OutputStream os = response.getOutputStream();
        response.setContentType("application/pdf");
        try {
          reportHelper.getDischargeSummaryReport(
              docid,
              formatType,
              DischargeSummaryReportHelper.ReturnType.PDF,
              prefs,
              sessionPrefs,
              os);
        } catch (TemplateException te) {
          // this cause only for hvf format discharge summary.
          response.reset();
          throw te;
        }
        os.close();
        return null;
      } else {
        // text mode
        String textReport =
            new String(
                reportHelper.getDischargeSummaryReport(
                    docid,
                    formatType,
                    DischargeSummaryReportHelper.ReturnType.TEXT_BYTES,
                    prefs,
                    sessionPrefs,
                    null));

        request.setAttribute("textReport", textReport);
        request.setAttribute("textColumns", prefs.get("text_mode_column"));
        request.setAttribute("printerType", "DMP");
        return mapping.findForward("textPrintApplet");
      }

    } else if (format.equals("U")) {
      byte[] byteData = null;
      ArrayList al = dao.getUploadImageResult(docid);
      byteData = (byte[]) al.get(0);

      response.setHeader("expires", "0");
      if (al.get(2) != null && (!al.get(2).toString().equalsIgnoreCase(""))) {
        response.setContentType(al.get(2).toString());
      }
      String fileName = al.get(3) != null ? al.get(3).toString() : "";
      Object originalExtension = al.get(4) != null ? al.get(4) : "";
      if (!fileName.equals("")) {
        if (!fileName.contains(".") && !originalExtension.equals("")) {
          fileName = fileName + "." + originalExtension;
        }
        response.setHeader("Content-disposition", "inline; filename=\"" + fileName + "\"");
      }
      OutputStream os = response.getOutputStream();
      os.write(byteData);
      os.flush();
      os.close();

    } else if (format.equals("P")) {
      response.setContentType("application/pdf");
      OutputStream os = response.getOutputStream();

      new DischargeSummaryReportHelper()
          .getDischargeSummaryReport(
              docid,
              DischargeSummaryReportHelper.FormatType.PDF,
              DischargeSummaryReportHelper.ReturnType.PDF,
              prefs,
              null,
              os);

      os.close();
    }

    return null;
  }
}
