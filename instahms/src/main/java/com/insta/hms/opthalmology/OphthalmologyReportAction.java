package com.insta.hms.opthalmology;

import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
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
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.xpath.XPathExpressionException;

/**
 * The Class OphthalmologyReportAction.
 */
public class OphthalmologyReportAction extends DispatchAction {

  /**
   * Gets the report.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the report
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
   */
  public ActionForward getReport(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException, SQLException,
      TemplateException, DocumentException, XPathExpressionException {

    String patientId = request.getParameter("patientId");
    String mrNo = request.getParameter("mr_no");

    BasicDynaBean printPref = PrintConfigurationsDAO
        .getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT, 4);

    Map params = new HashMap();
    params = insertDataInMap(patientId, mrNo, params);

    Template template = AppInit.getFmConfig().getTemplate("OphthalmologyReport.ftl");
    HtmlConverter hc = new HtmlConverter();

    StringWriter writer = new StringWriter();
    template.process(params, writer);
    String textContent = writer.toString();
    OutputStream os = response.getOutputStream();
    response.setContentType("application/pdf");
    try {
      hc.writePdf(os, textContent, "Dialysis Session Report", printPref, false, false, true, true,
          true, false);
    } catch (Exception exp) {
      System.out.println(exp);

    } finally {
      os.close();
    }

    return null;

  }

  /**
   * Gets the bytes.
   *
   * @param patientId
   *          the patient id
   * @param mrNo
   *          the mr no
   * @return the bytes
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws DocumentException
   *           the document exception
   * @throws XPathExpressionException
   *           the x path expression exception
   * @throws TemplateException
   *           the template exception
   */
  public static byte[] getBytes(String patientId, String mrNo) throws SQLException, IOException,
      DocumentException, XPathExpressionException, TemplateException {

    Map params = new HashMap();

    params = insertDataInMap(patientId, mrNo, params);

    BasicDynaBean printPref = PrintConfigurationsDAO
        .getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT, 4);
    Template template = AppInit.getFmConfig().getTemplate("OphthalmologyReport.ftl");
    HtmlConverter hc = new HtmlConverter();

    StringWriter writer = new StringWriter();
    template.process(params, writer);
    String textContent = writer.toString();

    return hc.getPdfBytes(textContent, "Opthalmology Report", printPref, false, true, true, true,
        false);
  }

  private static Map insertDataInMap(String patientId, String mrNo, Map params)
      throws SQLException {

    List testValues = OptometristScreenDAO.getTestValues(patientId);
    List eyeList = OptometristScreenDAO.getEyeTestList();

    Map patientDetails = PatientDetailsDAO.getPatientGeneralDetailsMap(mrNo);
    params.put("patient", patientDetails);
    params.put("testValues", testValues);
    params.put("eyeList", eyeList);
    params.put("records", OptometristScreenDAO.getRecordsForReport(patientId));
    params.put("opthalTestMain",
        new GenericDAO("opthal_test_main").findByKey("patient_id", patientId));
    params.put("patientHistory",
        new GenericDAO("patient_history").findByKey("patient_id", patientId));
    params.put("patientEyeHistory",
        new GenericDAO("patient_eye_history").findByKey("patient_id", patientId));

    return params;
  }

}
