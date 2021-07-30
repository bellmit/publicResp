package com.insta.hms.dentalconsultation;

import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.ftl.FtlReportGenerator;
import com.insta.hms.imageretriever.DoctorConsultImageRetriever;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.master.PrintTemplates.PrintTemplate;
import com.insta.hms.master.PrintTemplates.PrintTemplatesDAO;
import com.lowagie.text.DocumentException;

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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

public class PatientDentalSuppliesPrintAction extends DispatchAction {

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
   * @throws TemplateException the template exception
   * @throws DocumentException the document exception
   * @throws XPathExpressionException the x path expression exception
   * @throws TransformerException the transformer exception
   * @throws ParseException the parse exception
   */
  public ActionForward print(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response)
      throws SQLException, ServletException, IOException, TemplateException, DocumentException,
      XPathExpressionException, TransformerException, ParseException {

    FtlReportGenerator frg = null;
    String templateContent = new PrintTemplatesDAO()
        .getCustomizedTemplate(PrintTemplate.DENTAL_SUPPLIER_PRINT);
    if (templateContent != null && !templateContent.equals("")) {
      frg = new FtlReportGenerator("DentalSupplier.ftl", new StringReader(templateContent));
    } else {
      frg = new FtlReportGenerator(PrintTemplate.DENTAL_SUPPLIER_PRINT.getFtlName());
    }
    StringWriter writer = new StringWriter();
    frg.setParamsFromParamMap(request.getParameterMap());
    frg.process(writer);

    String printerIdStr = request.getParameter("printerId");
    int printerId = 0;
    if ((printerIdStr != null) && !printerIdStr.equals("")) {
      printerId = Integer.parseInt(printerIdStr);
    }
    BasicDynaBean prefs = PrintConfigurationsDAO
        .getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT, printerId);

    HtmlConverter hc = new HtmlConverter(new DoctorConsultImageRetriever());
    String printMode = "P";
    if (prefs.get("print_mode") != null) {
      printMode = (String) prefs.get("print_mode");
    }
    Boolean repeatPHeader = ((String) prefs.get("repeat_patient_info")).equals("Y");
    if (printMode.equals("P")) {
      response.setContentType("application/pdf");
      OutputStream os = response.getOutputStream();
      hc.writePdf(os, writer.toString(), "Supplies Order Details", prefs, false, repeatPHeader,
          true, true, false, false);
      os.close();
    } else {
      String textReport = new String(
          hc.getText(writer.toString(), "Supplies Order Details", prefs, true, true));

      request.setAttribute("textReport", textReport);
      request.setAttribute("textColumns", prefs.get("text_mode_column"));
      request.setAttribute("printerType", "DMP");
      return mapping.findForward("textPrintApplet");
    }
    return null;
  }

}
