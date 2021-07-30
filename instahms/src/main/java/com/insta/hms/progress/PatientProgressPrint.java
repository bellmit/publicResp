package com.insta.hms.progress;
/**
 * prasanna.kumar
 */

import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.PrintPageOptions;
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
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;


// TODO: Auto-generated Javadoc
/**
 * The Class PatientProgressPrint.
 */
public class PatientProgressPrint extends DispatchAction {

  // HMS-20608 :if user does not have access to patient Progress notes screen, but User should be
  // able to see progress notes.

  /**
   * Gets the prints the.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the prints the
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws TemplateException
   *           the template exception
   * @throws TransformerException
   *           the transformer exception
   * @throws XPathExpressionException
   *           the x path expression exception
   * @throws DocumentException
   *           the document exception
   */
  public ActionForward getPrint(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, IOException, TemplateException,
          TransformerException, XPathExpressionException, DocumentException {

    String mrNo = request.getParameter("mr_no");
    String visitId = request.getParameter("patientId");
    String filterVisitId = request.getParameter("filtervisitId");
    String filterType = request.getParameter("filterType");
    String fromScreen = request.getParameter("fromScreen");
    String progressId = request.getParameter("progressID");

    BasicDynaBean printPref = null;

    int printerId = 4;
    if ((null != request.getParameter("printDefType"))
        && !("").equals(request.getParameter("printDefType"))) {
      printerId = Integer.parseInt(request.getParameter("printDefType"));
    }
    printPref = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT,
        printerId);
    String textContent = getTextContent(mrNo, visitId, filterType, filterVisitId, progressId,
        fromScreen);
    HtmlConverter hc = new HtmlConverter();

    OutputStream os = null;
    try {
      if (printPref.get("print_mode").equals("T")) {
        String textReport = new String(
            hc.getText(textContent, "Progress Notes Details", printPref, true, true));
        request.setAttribute("textReport", textReport);
        request.setAttribute("textColumns", printPref.get("text_mode_column"));
        return mapping.findForward("textPrintApplet");
      } else {
        os = response.getOutputStream();
        response.setContentType("application/pdf");
        boolean repeatPatientHeader = ((String) printPref.get("repeat_patient_info"))
            .equalsIgnoreCase("Y");
        hc.writePdf(os, textContent, "Progress Notes Details", printPref, false, 
            repeatPatientHeader, true, false, true, false);
        return null;
      }
    } finally {
      if (os != null) {
        os.close();
      } 
    }

  }

  /**
   * Gets the text content.
   *
   * @param mrNo
   *          the mr no
   * @param visitId
   *          the visit id
   * @param filterType
   *          the filter type
   * @param filterVisitId
   *          the filter visit id
   * @param progressId
   *          the progress id
   * @param fromScreen
   *          the from screen
   * @return the text content
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws TemplateException
   *           the template exception
   */
  public static String getTextContent(String mrNo, String visitId, String filterType,
      String filterVisitId, String progressId, String fromScreen)
          throws SQLException, IOException, TemplateException {

    Map<String, Object> params = new HashMap<String, Object>();
    Template template = null;
    if (mrNo == null) {
      mrNo = (String) new GenericDAO("progress_notes").findByKey("progress_notes_id", progressId)
          .get("mr_no");
    }  
    params.put("patient", PatientDetailsDAO.getPatientGeneralDetailsMap(mrNo));
    if (fromScreen == null) {
      params.put("progressNotesList", PatientProgressDAO.getProgressNotesForEMR(visitId, mrNo));
    } else {
      params.put("progressNotesList",
          PatientProgressDAO.getProgressNotesList(mrNo, filterType, filterVisitId));
    }  
    PrintTemplatesDAO templateDAO = new PrintTemplatesDAO();
    String templateContent = templateDAO.getCustomizedTemplate(PrintTemplate.Progress_Notes);
    if (templateContent == null || templateContent.equals("")) {
      template = AppInit.getFmConfig().getTemplate(PrintTemplate.Progress_Notes.getFtlName() 
          + ".ftl");
    } else {
      StringReader reader = new StringReader(templateContent);
      template = new Template("PatientProgressNotes.ftl", reader, AppInit.getFmConfig());
    }
    StringWriter writer = new StringWriter();
    template.process(params, writer);
    String textContent = writer.toString();
    return textContent;
  }

}
