/**
 *
 */

package com.insta.hms.genericdocuments;

import com.bob.hms.common.APIUtility;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.PdfUtils;
import com.insta.hms.documentpersitence.AbstractDocumentPersistence;
import com.insta.hms.imageretriever.ImageRetriever;
import com.insta.hms.imageretriever.PatientImageRetriever;
import com.insta.hms.imageretriever.VisitWiseImageRetriever;
import com.insta.hms.master.HVFPrintTemplate.HVFPrintTemplateDAO;
import com.insta.hms.master.PatientHeaderTemplate.PatientHeaderTemplate;
import com.insta.hms.master.PatientHeaderTemplate.PatientHeaderTemplateDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.medicalrecorddepartment.MRDDiagnosisDAO;
import com.insta.hms.vitalForm.genericVitalFormDAO;
import com.insta.instaapi.common.JsonProcessor;
import com.insta.instaapi.common.ServletContextUtil;
import com.lowagie.text.DocumentException;

import flexjson.JSONSerializer;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import net.sf.jasperreports.engine.JRException;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

// TODO: Auto-generated Javadoc
/**
 * The Class GenericDocumentsPrintAction.
 *
 * @author krishna.t
 */
public class GenericDocumentsPrintAction extends DispatchAction {

  /** The log. */
  static final Logger log = LoggerFactory.getLogger(GenericDocumentsDAO.class);

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

  private static final JSONSerializer js = JsonProcessor.getJSONParser();

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

    String reqHandlerKey = request.getParameter("request_handler_key");
    String error =
        APIUtility.setConnectionDetails(servlet.getServletContext(),
            reqHandlerKey);
    if (error != null) {
      APIUtility.setInvalidLoginError(response, error);
      return null;
    }
    ServletContext ctx = servlet.getServletContext();
    Map<String, Object> sessionMap = ServletContextUtil.getContextParametersMap(ctx);
    Map<String,Object> sessionParameters = null;
    if (sessionMap != null && !sessionMap.isEmpty()) {
      sessionParameters = (Map<String,Object>) sessionMap.get(reqHandlerKey);
    }

    String docId = request.getParameter("doc_id");
    String fileName = "";
    if (docId == null) {
      throw new IllegalArgumentException("docid is null");
    }
    int docIdInt = Integer.parseInt(docId);


    BasicDynaBean patientdocbean = patientdocdao.getBean();
   
    boolean isPatientLogin = sessionParameters != null 
        && ((boolean) sessionParameters.get("patient_login"));
    if (isPatientLogin) {
      String mrNo = (String) sessionParameters.get("customer_user_id");
      if (!validateMrNoAndDocId(mrNo, docIdInt, mapping.getProperty("documentType"))) {
        String successMsg = "Invalid input parameters supplied for doc_id";
        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("return_code", "1021");
        errorMap.put("return_message", successMsg);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write(js.deepSerialize(errorMap));
        response.flushBuffer();
        return null;
      }
    }

    patientdocdao.loadByteaRecords(patientdocbean, "doc_id", docIdInt);

    String format = patientdocbean.get("doc_format").toString();
    String printerId = request.getParameter("printerId");
    String template = request.getParameter("templateName");
    AbstractDocumentPersistence persistenceAPI =
        AbstractDocumentPersistence.getInstance(mapping.getProperty("documentType"), new Boolean(
            mapping.getProperty("specialized")));
    Boolean specialized = new Boolean(mapping.getProperty("specialized"));
    int centerId = 0;
    Map keyParams = persistenceAPI.getDocKeyParams(docIdInt);
    if (!specialized) {
      centerId = persistenceAPI.getCenterId(request.getParameterMap());
    } else {
      centerId = RequestContext.getCenterId();
    }
    BasicDynaBean prefs;
    if ((printerId != null) && !printerId.equals("")) {
      prefs =
          PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT,
              Integer.parseInt(request.getParameter("printerId")), centerId);
    } else {
      // use the default printer
      prefs = PrintConfigurationsDAO.getPatientDefaultPrintPrefs(centerId);
    }

    BasicDynaBean overridePrefs =
        getSpecificPrinterPrefs(mapping, format, template, docIdInt, printerId);
    if (null != overridePrefs) {
      prefs = overridePrefs;
    }

    String printMode = "P";
    String forcePdf = request.getParameter("forcePdf");
    if ((forcePdf == null) || !forcePdf.equals("true")) {
      // use the print mode selected.
      printMode = (String) prefs.get("print_mode");
    }

    // api parameter
    String logoHeader = request.getParameter("logoHeader");
    if (logoHeader != null
        && !logoHeader.equals("")
        && (logoHeader.equalsIgnoreCase("Y") || logoHeader.equalsIgnoreCase("L")
            || logoHeader.equalsIgnoreCase("H") || logoHeader.equalsIgnoreCase("N"))) {
      prefs.set("logo_header", logoHeader.toUpperCase());
    }

    if (format.equals("doc_hvf_templates")) {

      String allFields = request.getParameter("allFields");
      allFields = (allFields == null ? "Y" : allFields);
      Map patientDetails = new HashMap();

      GenericDocumentsFields.copyStandardFields(patientDetails, false);
      persistenceAPI.copyReplaceableFields(patientDetails, keyParams, false);
      persistenceAPI.copyDocumentDetails(docIdInt, patientDetails);

      Map ftlParamMap = new HashMap();
      ftlParamMap.put("visitdetails", patientDetails);
      ftlParamMap.put("mr_no", patientDetails.get("mr_no"));
      ftlParamMap.put("mr_no_barcode", "*" + patientDetails.get("mr_no") + "*");
      ftlParamMap.put("patient_id", patientDetails.get("patient_id"));
      ftlParamMap.put("modules_activated", APIUtility.getPreferences().getModulesActivatedMap());
      List fieldvalues = PatientHVFDocValuesDAO.getHVFDocValues(docIdInt, allFields.equals("Y"));
      ftlParamMap.put("fieldvalues", fieldvalues);
      List imageFieldvalues =
          PatientHVFDocValuesDAO.getHVFDocImageValues(docIdInt, allFields.equals("Y"));
      List imgFieldsList = new ArrayList();

      if (imageFieldvalues != null && imageFieldvalues.size() > 0) {

        for (Object imgObj : imageFieldvalues) {
          Map imageMap = new HashMap();
          BasicDynaBean imgbean = (BasicDynaBean) imgObj;
          if (imgbean == null || imgbean.get("doc_image_id") == null
              || imgbean.get("field_name") == null) {
            continue;
          }
          int docImgeId = (Integer) imgbean.get("doc_image_id");
          String imageFileName = new String(docImgeId + (String) imgbean.get("field_name"));
          File imgFile = File.createTempFile(imageFileName, "");
          InputStream is = patientgendocdao.getHVFDocImage(docImgeId);

          /*
           * ServletContext context = request.getSession().getServletContext(); String virtualPath =
           * "/images/InstaLogo.jpg"; String realPath = context.getRealPath(virtualPath);
           * 
           * File imgFile = new File(realPath); InputStream is = new FileInputStream(imgFile);
           */

          if (is != null) {
            patientgendocdao.inputStreamToFile(is, imgFile);
            is.close();
            imageMap.put("doc_image_id", (Integer) imgbean.get("doc_image_id"));
            imageMap.put("image_url", imgFile.getAbsolutePath());
            imageMap.put("field_image_content_type",
                (String) imgbean.get("field_image_content_type"));
            imageMap.put("device_ip", (String) imgbean.get("device_ip"));
            imageMap.put("device_info", (String) imgbean.get("device_info"));
            imageMap.put("capture_time", (Timestamp) imgbean.get("capture_time"));
            imageMap.put("field_id", (Integer) imgbean.get("field_id"));
            imgFieldsList.add(imageMap);
          }
        }
      }
      if (imgFieldsList != null && imgFieldsList.size() > 0) {
        ftlParamMap.put("imgFieldsList", imgFieldsList);
      }
      ftlParamMap.put("patientDocDetails", patientdocbean);
      if (patientDetails.get("patient_id") != null 
          && !patientDetails.get("patient_id").equals("")) {
        ftlParamMap
            .put("vitals", genericVitalFormDAO.getVitalReadings(patientDetails.get("patient_id")
                .toString(), null));
        ftlParamMap.put("diagnosis_details",
            MRDDiagnosisDAO.getAllDiagnosisDetails(patientDetails.get("patient_id").toString()));
      } else {
        ftlParamMap.put("vitals", null);
        ftlParamMap.put("diagnosis_details", null);
      }
      String templateName = PatientHVFDocValuesDAO.getPrintTemplateName(docIdInt);
      Template ftlTemplate = null;
      String templateMode = null;

      StringWriter writer = new StringWriter();

      if (templateName == null || templateName.equals("")) {
        ftlTemplate = AppInit.getFmConfig().getTemplate("PatientHVFDocumentPrint.ftl");
        templateMode = "H";
      } else {
        HVFPrintTemplateDAO templateDao = new HVFPrintTemplateDAO();
        BasicDynaBean tmpBean = templateDao.getTemplateContent(templateName);

        if (tmpBean == null) {
          // couldn't find the template in the db, bail out with error.
          return null;
        }
        templateMode = (String) tmpBean.get("template_mode");
        String templateContent = (String) tmpBean.get("hvf_template_content");

        StringReader reader = new StringReader(templateContent);
        ftlTemplate = new Template("PatientHVFDocumentPrint.ftl", reader, AppInit.getFmConfig());
      }

      ftlTemplate.process(ftlParamMap, writer);

      ImageRetriever imgretriever = null;
      String patientId = (String) patientDetails.get("patient_id");
      patientId = patientId == null ? "" : patientId;
      if (patientId.equals("")) {
        imgretriever = new PatientImageRetriever();
      } else {
        imgretriever = new VisitWiseImageRetriever();
      }
      HtmlConverter hc = new HtmlConverter(imgretriever);
      String hvfContent = writer.toString();

      if (printMode.equals("T")) {
        String textContent = null;
        if (templateMode.equals("T")) {
          // write the output as is.
          textContent = hvfContent;
        } else {
          // convert from HTML to text
          textContent =
              new String(hc.getText(hvfContent, "Patient HVF Document Print", prefs, true, true));
        }
        request.setAttribute("textReport", textContent);
        request.setAttribute("textColumns", prefs.get("text_mode_column"));
        request.setAttribute("printerType", "DMP");
        return mapping.findForward("textPrintApplet");

      } else {
        OutputStream os = response.getOutputStream();
        response.setContentType("application/pdf");
        Boolean repeatPHeader = ((String) prefs.get("repeat_patient_info")).equals("Y");
        try {
          if (templateMode.equals("T")) {
            // convert text to PDF
            hc.textToPDF(hvfContent, os, prefs);
          } else {
            // convert html to PDF
            hc.writePdf(os, hvfContent, "Patient HVF Document Print", prefs, false, repeatPHeader,
                true, true, true, false, centerId);
          }
        } catch (Exception ex) {
          log.error("Generated HTML content:");
          log.error(hvfContent);
          throw (ex);
        } finally {
          os.close();
        }
        return null;
      }


    } else if (format.equals("doc_rich_templates")) {

      String patientHeaderTemplateType = PatientHeaderTemplate.Documents.getType();
      if (new Boolean(mapping.getProperty("specialized"))
          && mapping.getProperty("documentType").equals("service")) {
        patientHeaderTemplateType = PatientHeaderTemplate.Ser.getType();
      }
      Map patientDetails = new HashMap();

      GenericDocumentsFields.copyStandardFields(patientDetails, false);
      persistenceAPI.copyReplaceableFields(patientDetails, keyParams, false);
      persistenceAPI.copyDocumentDetails(docIdInt, patientDetails);

      Map ftlParamMap = new HashMap();
      ftlParamMap.put("visitdetails", patientDetails);
      ftlParamMap.put("mr_no", patientDetails.get("mr_no"));
      ftlParamMap.put("mr_no_barcode", "*" + patientDetails.get("mr_no") + "*");
      ftlParamMap.put("patient_id", patientDetails.get("patient_id"));
      ftlParamMap.put("modules_activated", APIUtility.getPreferences().getModulesActivatedMap());
      StringWriter writer = new StringWriter();
      String patientHeader =
          phTemplateDao.getPatientHeader((Integer) patientdocbean.get("pheader_template_id"),
              patientHeaderTemplateType);
      StringReader reader = new StringReader(patientHeader);
      Template headerTemplate = new Template("PatientHeader.ftl", reader, AppInit.getFmConfig());
      headerTemplate.process(ftlParamMap, writer);
      StringBuilder printContent = new StringBuilder();
      printContent.append(writer.toString());
      String content = (String) patientdocbean.get("doc_content_text");
      // replace the control characters.
      content = content.replaceAll("[\\x00-\\x09\\x0b-\\x1f]", " ");
      StringWriter genericContentWriter = new StringWriter();
      StringReader genericContentReader = new StringReader(content);
      Template genericContentTemplate =
          new Template("GenericTemplate", genericContentReader, AppInit.getFmConfig());
      Map genericMap = new HashMap();
      genericMap.put("visitdetails", patientDetails);
      genericContentTemplate.process(genericMap, genericContentWriter);
      printContent.append(genericContentWriter.toString());
      String docName = "";
      if (patientDetails.get("doc_name") != null) { // some documents doesn't have the name.
        docName = (String) patientDetails.get("doc_name");
      }
      ImageRetriever imgretriever = null;
      String patientId = (String) patientDetails.get("patient_id");
      patientId = patientId == null ? "" : patientId;
      if (patientId.equals("")) {
        imgretriever = new PatientImageRetriever();
      } else {
        imgretriever = new VisitWiseImageRetriever();
      }
      HtmlConverter hc = new HtmlConverter(imgretriever);

      if (!printMode.equals("T")) {
        OutputStream os = response.getOutputStream();
        response.setContentType("application/pdf");
        Boolean repeatPHeader = ((String) prefs.get("repeat_patient_info")).equals("Y");
        hc.writePdf(os, printContent.toString(), docName, prefs, false, repeatPHeader, true, true,
            true, false, centerId);
        os.close();
        return null;
      } else {
        String textReport =
            new String(hc.getText(printContent.toString(), docName, prefs, true, true, centerId));
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

      GenericDocumentsFields.copyStandardFields(fields, true);
      persistenceAPI.copyReplaceableFields(fields, keyParams, true);

      Map documentDetails = new HashMap();
      persistenceAPI.copyDocumentDetails(docIdInt, documentDetails);
      fields.put("_username", (String) documentDetails.get("username"));
      SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
      String docDate = "";
      if (!"".equals(documentDetails.get("doc_date"))) {
        docDate = dateFormat.format(documentDetails.get("doc_date"));
      }
      fields.put("_doc_date", docDate);
      fields.put("_doc_name", (String) documentDetails.get("doc_name"));


      List<BasicDynaBean> fieldslist = pdfvaluesdocdao.listAll(null, "doc_id", docIdInt);

      for (BasicDynaBean fieldsBean : fieldslist) {
        fields.put(fieldsBean.get("field_name").toString(), fieldsBean.get("field_value")
            .toString());
      }

      HashMap<String, String> hiddenParams = new HashMap<String, String>();
      hiddenParams.put("doc_id", request.getParameter("doc_id"));

      response.setContentType("application/pdf");
      OutputStream os = response.getOutputStream();
      InputStream pdf = (InputStream) bean.get("template_content");
      PdfUtils.sendFillableForm(os, pdf, fields, true, null, hiddenParams, null, centerId);

    } else {

      Map docParams = new HashMap();
      persistenceAPI.copyDocumentDetails(docIdInt, docParams);

      if (docParams.get("doc_name") != null && !(docParams.get("doc_name").equals(""))) {
        fileName = docParams.get("doc_name").toString();
        log.debug("Setting file name to: " + fileName);
      } else {
        log.debug("No file name");
      }

      if (patientdocbean.get("original_extension") != null && !(fileName.equals(""))) {
        fileName = fileName + "." + patientdocbean.get("original_extension").toString();
        response.setHeader("Content-disposition", "inline; filename=\"" + fileName + "\"");
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

  private boolean validateMrNoAndDocId(String mrNo, Integer docIdInt, String docType)
      throws SQLException {
    String mrNoResult = null;
    if (docType != null && (docType.equalsIgnoreCase("lab_test_doc")
        || docType.equalsIgnoreCase("rad_test_doc"))) {
      mrNoResult = patientgendocdao.getMrForTestDocument(docIdInt);
    } else if (docType != null && docType.equalsIgnoreCase("service")) {
      mrNoResult = patientgendocdao.getMrForServiceDocument(docIdInt);
    } else {
      BasicDynaBean patientgendocbean = patientgendocdao.findByKey("doc_id", docIdInt);
      mrNoResult = (String) patientgendocbean.get("mr_no");
    }
    if (mrNoResult != null && !mrNoResult.equals(mrNo)) {
      return false;
    }
    return true;
  }

  /**
   * Gets the specific printer prefs.
   *
   * @param mapping the mapping
   * @param format the format
   * @param template the template
   * @param docId the doc id
   * @param printerId the printer id
   * @return the specific printer prefs
   * @throws SQLException the SQL exception
   */
  private BasicDynaBean getSpecificPrinterPrefs(ActionMapping mapping, String format,
      String template, Integer docId, String printerId) throws SQLException {
    Integer printerIdInt = null;
    BasicDynaBean prefs = null;
    if (null != printerId && !printerId.equals("")) {
      printerIdInt = Integer.parseInt(printerId);
    }

    if (template == null || template.isEmpty()) {
      template = PatientHVFDocValuesDAO.getTemplateName(docId, format);
    }

    if ("reg".equals(mapping.getProperty("documentType"))
        && (format.equals("doc_rich_templates") || format.equals("doc_hvf_templates"))) {
      prefs = DocumentPrintConfigurationsDAO
          .getRegistrationPrintConfiguration(template, printerIdInt);
    }
    return prefs;
  }
}
