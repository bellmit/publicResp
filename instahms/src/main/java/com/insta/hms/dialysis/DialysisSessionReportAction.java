package com.insta.hms.dialysis;

import static com.insta.hms.dialysis.DialysisMedicationsDAO.getPrescriptinForSession;

import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.common.AppInit;
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
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;


// TODO: Auto-generated Javadoc
/**
 * The Class DialysisSessionReportAction.
 *
 * @author pragna.p
 */

public class DialysisSessionReportAction extends DispatchAction {

  /**
   * Gets the session report.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param response the response
   * @return the session report
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws TemplateException the template exception
   * @throws XPathExpressionException the x path expression exception
   * @throws DocumentException the document exception
   * @throws TransformerException the transformer exception
   */
  public ActionForward getSessionReport(ActionMapping mapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse response)
      throws SQLException, ParseException, IOException, TemplateException, XPathExpressionException,
      DocumentException, TransformerException {

    String orderId = req.getParameter("order_id");
    DialysisSessionsDao dao = new DialysisSessionsDao();

    BasicDynaBean printPref = PrintConfigurationsDAO
        .getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT, 4);

    // params to be passed to the template processor
    Map params = new HashMap();

    List prepParamsValues = new ArrayList();
    prepParamsValues = dao.getPrepValues(Integer.parseInt(orderId));
    params.put("prepParamValues", prepParamsValues);

    List prepParamMasterValues = new ArrayList();
    prepParamMasterValues = dao.getPrepMasterValues();
    params.put("prepParamMasterValues", prepParamMasterValues);
    String mrNo = req.getParameter("mr_no");
    Map patientDetails = PatientDetailsDAO.getPatientGeneralDetailsMap(mrNo);
    params.put("patient", patientDetails);

    BasicDynaBean sessionDetails = dao.getSessionDetailsForReport(Integer.parseInt(orderId));
    params.put("SessionDetails", sessionDetails.getMap());

    if (sessionDetails.get("in_real_wt") != null) {
      BigDecimal inrealWeight = (BigDecimal) sessionDetails.get("in_real_wt");

      if (sessionDetails.get("target_weight") != null) {
        BigDecimal targetWeight = (BigDecimal) sessionDetails.get("target_weight");

        BigDecimal ufrate = inrealWeight.subtract(targetWeight);
        params.put("uf_target", ufrate);
      }
      if (sessionDetails.get("fin_real_wt") != null) {
        BigDecimal finrealWeight = (BigDecimal) sessionDetails.get("fin_real_wt");

        BigDecimal weightLoss = inrealWeight.subtract(finrealWeight);
        params.put("wt_loss", weightLoss);
      }
      if (sessionDetails.get("target_wt") != null) {
        BigDecimal targetWt = (BigDecimal) sessionDetails.get("target_wt");

        params.put("excessWt", inrealWeight.subtract(targetWt));
      }
    }

    // BasicDynaBean prevSessionDetails = dao.getPrevSessionDetails(Integer.parseInt(order_id));
    // params.put("PrevSessionDetails", prevSessionDetails.getMap());
    params.put("drugs", getDrugBuilder(Integer.parseInt(orderId)));

    List intraDetails = dao.getIntraDialysisDetails(Integer.parseInt(orderId));
    params.put("IntraDetails", intraDetails);
    params.put("treatmentList", getPrescriptinForSession(mrNo, Integer.parseInt(orderId)));
    /*
     * Get the template for the print
     */
    Template template = AppInit.getFmConfig().getTemplate("DialysisSessionReport.ftl");
    HtmlConverter hc = new HtmlConverter();

    /*
     * Process the template and get the pdf
     */
    StringWriter writer = new StringWriter();
    template.process(params, writer);
    String textContent = writer.toString();
    OutputStream os = response.getOutputStream();
    response.setContentType("application/pdf");
    try {
      hc.writePdf(os, textContent, "Dialysis Session Report", printPref, false, false, true, true,
          true, false);
    } finally {
      os.close();
    }
    return null;
  }

  /**
   * Dialysis sessions bytes.
   *
   * @param mrNo the mr no
   * @param orderId the order id
   * @return the byte[]
   * @throws Exception the exception
   */
  public static byte[] dialysisSessionsBytes(String mrNo, String orderId) throws Exception {

    DialysisSessionsDao dao = new DialysisSessionsDao();

    // params to be passed to the template processor
    Map params = new HashMap();

    Map patientDetails = PatientDetailsDAO.getPatientGeneralDetailsMap(mrNo);
    params.put("patient", patientDetails);

    BasicDynaBean sessionDetails = dao.getSessionDetailsForReport(Integer.parseInt(orderId));
    params.put("SessionDetails", sessionDetails.getMap());

    if (sessionDetails.get("in_real_wt") != null && sessionDetails.get("target_weight") != null) {
      BigDecimal inrealWeight = (BigDecimal) sessionDetails.get("in_real_wt");
      BigDecimal targetWeight = (BigDecimal) sessionDetails.get("target_weight");

      BigDecimal ufrate = inrealWeight.subtract(targetWeight);
      params.put("uf_target", ufrate);
    }
    if (sessionDetails.get("in_real_wt") != null && sessionDetails.get("fin_real_wt") != null) {
      BigDecimal inrealWeight = (BigDecimal) sessionDetails.get("in_real_wt");
      BigDecimal finrealWeight = (BigDecimal) sessionDetails.get("fin_real_wt");

      BigDecimal weightLoss = inrealWeight.subtract(finrealWeight);
      params.put("wt_loss", weightLoss);
    }

    params.put("drugs", getDrugBuilder(Integer.parseInt(orderId)));
    params.put("treatmentList", getPrescriptinForSession(mrNo, Integer.parseInt(orderId)));

    // BasicDynaBean prevSessionDetails = dao.getPrevSessionDetails(Integer.parseInt(order_id));
    // params.put("PrevSessionDetails", prevSessionDetails.getMap());

    List intraDetails = dao.getIntraDialysisDetails(Integer.parseInt(orderId));
    params.put("IntraDetails", intraDetails);

    Template template = AppInit.getFmConfig().getTemplate("DialysisSessionReport.ftl");
    HtmlConverter hc = new HtmlConverter();

    StringWriter writer = new StringWriter();
    template.process(params, writer);
    String textContent = writer.toString();
    BasicDynaBean printPref = PrintConfigurationsDAO
        .getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT, 4);
    return hc.getPdfBytes(textContent, "Dialysis Session Report", printPref, false, true, true,
        true, false);

  }

  /**
   * Gets the drug builder.
   *
   * @param orderId the order id
   * @return the drug builder
   * @throws SQLException the SQL exception
   */
  private static StringBuilder getDrugBuilder(int orderId) throws SQLException {
    List<BasicDynaBean> list = DialysisSessionsDao.getDrugList(orderId);
    StringBuilder builder = DialysisSessionsDao.builder(list, "medicine_name");
    return builder;
  }

}
