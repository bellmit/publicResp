/**
 *
 */

package com.insta.hms.genericdocuments;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Preferences;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.PdfUtils;
import com.insta.hms.diagnosticmodule.common.DiagnosticsDAO;
import com.insta.hms.documentpersitence.AbstractDocumentPersistence;
import com.insta.hms.imageretriever.PatientImageRetriever;
import com.insta.hms.master.PatientHeaderTemplate.PatientHeaderTemplateDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.stores.MedicineSalesDAO;
import com.insta.hms.vitalForm.genericVitalFormDAO;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfCopyFields;
import com.lowagie.text.pdf.PdfReader;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import net.sf.jasperreports.engine.JRException;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

// TODO: Auto-generated Javadoc
/**
 * The Class CaseFormDocumentsPrintAction.
 *
 * @author krishna.t
 */
public class CaseFormDocumentsPrintAction extends DispatchAction {

  /** The pdftemplatedao. */
  private static final GenericDAO pdftemplatedao = new GenericDAO("doc_pdf_form_templates");

  /** The ph template dao. */
  private static final PatientHeaderTemplateDAO phTemplateDao = new PatientHeaderTemplateDAO();

  /** The pdfvaluesdocdao. */
  private static final GenericDAO pdfvaluesdocdao = new GenericDAO("patient_pdf_form_doc_values");

  /** The patientdocdao. */
  private static final PatientDocumentsDAO patientdocdao = new PatientDocumentsDAO();

  /** The patientgendocdao. */
  private static final GenericDocumentsDAO patientgendocdao = new GenericDocumentsDAO();


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
   * @throws FileUploadException the file upload exception
   * @throws SQLException the SQL exception
   * @throws DocumentException the document exception
   * @throws ParserConfigurationException the parser configuration exception
   * @throws JRException the JR exception
   * @throws IllegalArgumentException the illegal argument exception
   * @throws TemplateException the template exception
   * @throws XPathExpressionException the x path expression exception
   * @throws TransformerException the transformer exception
   * @throws Exception the exception
   */
  public ActionForward print(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException, FileUploadException,
      SQLException, DocumentException, ParserConfigurationException, JRException,
      IllegalArgumentException, TemplateException, XPathExpressionException, TransformerException,
      Exception {

    String docId = request.getParameter("doc_id");
    String fileName = "";
    if (docId == null) {
      throw new IllegalArgumentException("docid is null");
    }
    int parseDoccId = Integer.parseInt(docId);

    BasicDynaBean patientdocbean = patientdocdao.getBean();
    BasicDynaBean patientgendocbean = patientgendocdao.findByKey("doc_id", parseDoccId);
    patientdocdao.loadByteaRecords(patientdocbean, "doc_id", parseDoccId);

    String format = patientdocbean.get("doc_format").toString();
    String printerId = request.getParameter("printerId");
    BasicDynaBean prefs;
    if ((printerId != null) && !printerId.equals("")) {
      prefs =
          PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT,
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
    Boolean appendTreatment = new Boolean(request.getParameter("appendTreatment"));

    AbstractDocumentPersistence persistenceAPI =
        AbstractDocumentPersistence.getInstance(mapping.getProperty("documentType"), new Boolean(
            mapping.getProperty("specialized")));

    if (format.equals("doc_hvf_templates")) {

      String allFields = request.getParameter("allFields");
      allFields = (allFields == null ? "Y" : allFields);
      Map patientDetails = new HashMap();

      Map keyParams = persistenceAPI.getDocKeyParams(parseDoccId);
      GenericDocumentsFields.copyStandardFields(patientDetails, false);
      persistenceAPI.copyReplaceableFields(patientDetails, keyParams, false);
      persistenceAPI.copyDocumentDetails(parseDoccId, patientDetails);

      Map ftlParamMap = new HashMap();
      ftlParamMap.put("visitdetails", patientDetails);
      ftlParamMap.put("mr_no", patientDetails.get("mr_no"));
      ftlParamMap.put("patient_id", patientDetails.get("patient_id"));
      ftlParamMap.put("vitals",
          genericVitalFormDAO.getVitalReadings(patientDetails.get("patient_id").toString(), "V"));
      ftlParamMap.put("modules_activated",
          ((Preferences) RequestContext.getSession().getAttribute("preferences"))
              .getModulesActivatedMap());
      ftlParamMap.put("fieldvalues",
          PatientHVFDocValuesDAO.getHVFDocValues(parseDoccId, allFields.equals("Y")));

      StringWriter writer = new StringWriter();
      Template ftlTemplate = AppInit.getFmConfig().getTemplate("CaseFormHVFTemplate.ftl");
      ftlTemplate.process(ftlParamMap, writer);

      String treatmentDetails = getTreatementText((String) patientDetails.get("patient_id"));
      HtmlConverter hc = new HtmlConverter();
      if (printMode.equals("T")) {
        String textReport =
            new String(hc.getText(writer.toString(), "Case Form HVF Document Print", prefs, true,
                true));
        if (treatmentDetails != null && appendTreatment) {
          String convertedTreatmentInfo =
              new String(hc.getText(treatmentDetails, "TreatmentInfo", prefs, false, false));
          textReport += convertedTreatmentInfo;
          convertedTreatmentInfo = null; // clear the memory
        }

        request.setAttribute("textReport", textReport);
        request.setAttribute("textColumns", prefs.get("text_mode_column"));
        request.setAttribute("printerType", "DMP");
        return mapping.findForward("textPrintApplet");
      } else {
        OutputStream stream = response.getOutputStream();
        PdfCopyFields copy = new PdfCopyFields(stream);

        response.setContentType("application/pdf");
        Boolean repeatPHeader = ((String) prefs.get("repeat_patient_info")).equals("Y");
        byte[] docBytes =
            hc.getPdfBytes(writer.toString(), "Case Form Document Print", prefs, repeatPHeader,
                true, true, true, false);
        // add the document content
        PdfReader reader = new PdfReader(new ByteArrayInputStream(docBytes));
        copy.addDocument(reader);
        // add the treatment details to the case form
        if (treatmentDetails != null && appendTreatment) {
          reader =
              new PdfReader(new ByteArrayInputStream(new HtmlConverter().getPdfBytes(
                  treatmentDetails, "TreatmentInfo", prefs, false, false, false, true, false)));
          copy.addDocument(reader);
        }
        copy.close();
        return null;
      }

    } else if (format.equals("doc_rich_templates")) {

      Map patientDetails = new HashMap();

      Map keyParams = persistenceAPI.getDocKeyParams(parseDoccId);
      GenericDocumentsFields.copyStandardFields(patientDetails, false);
      persistenceAPI.copyReplaceableFields(patientDetails, keyParams, false);
      persistenceAPI.copyDocumentDetails(parseDoccId, patientDetails);
      Map ftlParamMap = new HashMap();

      ftlParamMap.put("visitdetails", patientDetails);
      ftlParamMap.put("mr_no", patientDetails.get("mr_no"));
      ftlParamMap.put("patient_id", patientDetails.get("patient_id"));
      ftlParamMap.put("modules_activated",
          ((Preferences) RequestContext.getSession().getAttribute("preferences"))
              .getModulesActivatedMap());

      StringWriter writer = new StringWriter();
      String patientHeader =
          phTemplateDao.getPatientHeader((Integer) patientdocbean.get("pheader_template_id"),
              "Documents");
      StringReader reader = new StringReader(patientHeader);
      Template headerTemplate = new Template("PatientHeader.ftl", reader, AppInit.getFmConfig());
      headerTemplate.process(ftlParamMap, writer);
      StringBuilder printContent = new StringBuilder();
      printContent.append(writer.toString());
      String content = (String) patientdocbean.get("doc_content_text");
      printContent.append(content);

      String docName = "";
      if (patientDetails.get("doc_name") != null) { // some documents doesn't have the name.
        docName = (String) patientDetails.get("doc_name");
      }

      HtmlConverter hc = new HtmlConverter(new PatientImageRetriever());
      String treatmentDetails = getTreatementText((String) patientDetails.get("patient_id"));

      if (!printMode.equals("T")) {
        OutputStream stream = response.getOutputStream();
        PdfCopyFields copy = new PdfCopyFields(stream);

        response.setContentType("application/pdf");
        Boolean repeatPHeader = ((String) prefs.get("repeat_patient_info")).equals("Y");
        byte[] docBytes =
            hc.getPdfBytes(printContent.toString(), docName, prefs, repeatPHeader, true, true,
                true, false);
        // add the document content
        PdfReader pdfreader = new PdfReader(new ByteArrayInputStream(docBytes));
        copy.addDocument(pdfreader);
        // add the treatment details to the case form
        if (treatmentDetails != null && appendTreatment) {
          pdfreader =
              new PdfReader(new ByteArrayInputStream(new HtmlConverter().getPdfBytes(
                  treatmentDetails, "TreatmentInfo", prefs, false, false, false, true, false)));
          copy.addDocument(pdfreader);
        }

        copy.close();
        return null;
      } else {
        String textReport =
            new String(hc.getText(printContent.toString(), docName, prefs, true, true));
        if (treatmentDetails != null && appendTreatment) {
          String convertedTreatmentInfo =
              new String(hc.getText(treatmentDetails, "TreatmentInfo", prefs, false, false));
          textReport += convertedTreatmentInfo;
          convertedTreatmentInfo = null; // clear the memory;
        }

        request.setAttribute("textReport", textReport);
        request.setAttribute("textColumns", prefs.get("text_mode_column"));
        request.setAttribute("printerType", "DMP");
        return mapping.findForward("textPrintApplet");
      }

    } else if (format.equals("doc_pdf_form_templates")) {
      // prefs are not applicable here, the PDF is pre-formatted.
      BasicDynaBean bean = pdftemplatedao.getBean();
      pdftemplatedao.loadByteaRecords(bean, "template_id", patientdocbean.get("template_id"));

      Map<String, String> fields = new HashMap<String, String>();

      Map keyParams = persistenceAPI.getDocKeyParams(parseDoccId);
      GenericDocumentsFields.copyStandardFields(fields, true);
      persistenceAPI.copyReplaceableFields(fields, keyParams, true);
      Map documentDetails = new HashMap();
      persistenceAPI.copyDocumentDetails(parseDoccId, documentDetails);

      fields.put("_username", (String) documentDetails.get("username"));
      List<BasicDynaBean> fieldslist = pdfvaluesdocdao.listAll(null, "doc_id", parseDoccId);

      for (BasicDynaBean fieldsBean : fieldslist) {
        fields.put(fieldsBean.get("field_name").toString(), fieldsBean.get("field_value")
            .toString());
      }
      OutputStream stream = response.getOutputStream();
      PdfCopyFields copy = new PdfCopyFields(stream);

      response.setContentType("application/pdf");
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      InputStream pdf = (InputStream) bean.get("template_content");
      PdfUtils.sendFillableForm(os, pdf, fields, true, null, null, null);
      // copy the pdf case form content
      PdfReader reader = new PdfReader(new ByteArrayInputStream(os.toByteArray()));
      copy.addDocument(reader);
      // add the treatment details to the case form.
      String treatmentDetails = getTreatementText((String) fields.get("_patient_id"));
      if (treatmentDetails != null && appendTreatment) {
        reader =
            new PdfReader(new ByteArrayInputStream(
                new HtmlConverter().getPdfBytes(treatmentDetails)));
        copy.addDocument(reader);
      }
      copy.close();

    } else {
      // RTF document: prefs are not applicable here: just dump the document as is.
      Map keyParams = persistenceAPI.getDocKeyParams(parseDoccId);
      if (keyParams.get("doc_name") != null && !(keyParams.get("doc_name").equals(""))) {
        fileName = keyParams.get("doc_name").toString();
      }

      if (patientdocbean.get("original_extension") != null && !(fileName.equals(""))) {
        fileName = fileName + "." + patientdocbean.get("original_extension").toString();
        response.setHeader("Content-disposition", "attachment; filename=\"" + fileName + "\"");
      }

      response.setContentType(patientdocbean.get("content_type").toString());
      OutputStream stream = response.getOutputStream();
      stream.write(DataBaseUtil.readInputStream((java.io.InputStream) patientdocbean
          .get("doc_content_bytea")));
      stream.flush();
      stream.close();
    }
    return null;
  }


  /**
   * Gets the treatement text.
   *
   * @param patientId the patient id
   * @return the treatement text
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws TemplateException the template exception
   */
  private String getTreatementText(String patientId) throws SQLException, IOException,
      TemplateException {
    List investigations = DiagnosticsDAO.getReportList(patientId, false);
    Map treatment = new HashMap();
    treatment.put("medicines", MedicineSalesDAO.getMedicinesPrescribedAndSold(patientId));
    // treatment.put("services", ServiceDAO.getServiceValuesForPatient(patientId));
    Template ftlTemplate = null;

    ftlTemplate = AppInit.getFmConfig().getTemplate("/treatment/medicines.ftl");
    StringWriter medicinesFtl = new StringWriter();
    ftlTemplate.process(treatment, medicinesFtl);

    ftlTemplate = AppInit.getFmConfig().getTemplate("/treatment/tests.ftl");
    StringWriter testsFtl = new StringWriter();
    ftlTemplate.process(treatment, testsFtl);

    if (medicinesFtl.toString().equals("") && testsFtl.toString().equals("")) {
      return null;
    }

    StringBuilder treatmentStr = new StringBuilder();
    // wrap treatement details with root element.(to satisfy the xml well formedness)
    treatmentStr.append("<div>");
    treatmentStr.append(medicinesFtl.toString());
    treatmentStr.append(testsFtl.toString());
    treatmentStr.append("</div>");
    return treatmentStr.toString();

  }

}
