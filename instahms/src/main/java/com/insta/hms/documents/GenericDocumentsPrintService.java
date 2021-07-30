package com.insta.hms.documents;

import com.bob.hms.common.APIUtility;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.BusinessService;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.PdfUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.clinical.vitalforms.VitalReadingRepository;
import com.insta.hms.core.medicalrecords.MRDDiagnosisRepository;
import com.insta.hms.imageretriever.ImageRetriever;
import com.insta.hms.imageretriever.PatientImageRetriever;
import com.insta.hms.imageretriever.VisitWiseImageRetriever;
import com.insta.hms.master.PatientHeaderTemplate.PatientHeaderTemplate;

import freemarker.template.Template;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

// TODO: Auto-generated Javadoc
/**
 * The Class GenericDocumentsPrintService.
 */
public abstract class GenericDocumentsPrintService extends BusinessService {

  /** The doc store. */
  private AbstractDocumentStore docStore;
  
  /** The generic documents util. */
  @LazyAutowired
  private GenericDocumentsUtil genericDocumentsUtil;

  /**
   * Instantiates a new generic documents print service.
   *
   * @param store the store
   */
  public GenericDocumentsPrintService(AbstractDocumentStore store) {
    this.docStore = store;
  }

  /** The log. */
  static Logger log = LoggerFactory.getLogger(PatientGeneralDocsRepository.class);

  /** The pdftemplaterepo. */
  @LazyAutowired
  private DocPdfFormTemplateRepository pdftemplaterepo;
  
  /** The ph template repo. */
  @LazyAutowired
  private DocPatientHeaderTemplatesRepository phTemplateRepo;
  
  /** The pdfvaluesdocrepo. */
  @LazyAutowired
  private PatientPdfFormValuesRepository pdfvaluesdocrepo;
  
  /** The patientdocrepo. */
  @LazyAutowired
  private PatientDocumentRepository patientdocrepo;
  
  /** The patienthvfdocimagesrepo. */
  @LazyAutowired
  private PatientHvfDocImagesRepository patienthvfdocimagesrepo;
  
  /** The hvfprinttemplaterepo. */
  @LazyAutowired
  private HvfPrintTemplateRepository hvfprinttemplaterepo;
  
  /** The patientgendocrepo. */
  @LazyAutowired
  private PatientGeneralDocsRepository patientgendocrepo;
  
  /** The mrd diagnosis repository. */
  @LazyAutowired
  private MRDDiagnosisRepository mrdDiagnosisRepository;
  
  /** The vital reading repository. */
  @LazyAutowired
  private VitalReadingRepository vitalReadingRepository;

  /**
   * Prints the.
   *
   * @param params the params
   * @param requestMap the request map
   * @param response the response
   * @throws Exception the exception
   */
  public void print(Map<String, String[]> params, Map<String, Object> requestMap,
      HttpServletResponse response) throws Exception {
    // String error =
    // APIUtility.setConnectionDetails(servlet.getServletContext(),
    // params.get("request_handler_key")[0]);
    // if (error != null) {
    // APIUtility.setInvalidLoginError(response, error);
    // return null;
    // }

    String docId = params.get("doc_id")[0];
    String fileName = "";
    if (docId == null) {
      throw new IllegalArgumentException("docid is null");
    }
    int docIdInt = Integer.parseInt(docId);

    String printerId = null;
    if (null != params.get("printerId")) {
      printerId = params.get("printerId")[0];
    }
    String template = null;
    if (null != params.get("templateName")) {
      template = params.get("templateName")[0];
    }

    Boolean specialized = docStore.isSpecialized();
    int centerId = 0;
    Map keyParams = docStore.getDocKeyParams(docIdInt);
    if (!specialized) {
      centerId = docStore.getCenterId(params);
    } else {
      centerId = RequestContext.getCenterId();
    }
    BasicDynaBean prefs;
    if ((printerId != null) && !printerId.equals("")) {
      prefs = PrintConfigurationRepository.getPageOptions(
          PrintConfigurationRepository.PRINT_TYPE_PATIENT, Integer.parseInt(printerId), centerId);
    } else {
      // use the default printer
      prefs = PrintConfigurationRepository.getPatientDefaultPrintPrefs(centerId);
    }
    BasicDynaBean patientdocbean = patientdocrepo.findByKey("doc_id", docIdInt);
    String format = patientdocbean.get("doc_format").toString();
    BasicDynaBean overridePrefs = getSpecificPrinterPrefs(format, template, docIdInt, printerId);
    if (null != overridePrefs) {
      prefs = overridePrefs;
    }

    String printMode = "P";
    String forcePdf = null;
    if (null != params.get("forcePdf")) {
      forcePdf = params.get("forcePdf")[0];
    }
    if ((forcePdf == null) || !forcePdf.equals("true")) {
      // use the print mode selected.
      printMode = (String) prefs.get("print_mode");
    }

    // api parameter
    String logoHeader = null;
    if (null != params.get("logoHeader")) {
      logoHeader = params.get("logoHeader")[0];
    }
    if (logoHeader != null && !logoHeader.equals("")
        && (logoHeader.equalsIgnoreCase("Y") || logoHeader.equalsIgnoreCase("L")
            || logoHeader.equalsIgnoreCase("H") || logoHeader.equalsIgnoreCase("N"))) {
      prefs.set("logo_header", logoHeader.toUpperCase());
    }

    if (format.equals("doc_hvf_templates")) {

      String allFields = null;
      if (null != params.get("allFields")) {
        allFields = params.get("allFields")[0];
      }
      allFields = (allFields == null ? "Y" : allFields);
      Map patientDetails = new HashMap();

      genericDocumentsUtil.copyStandardFields(patientDetails, false);
      docStore.copyReplaceableFields(patientDetails, keyParams, false);
      docStore.copyDocumentDetails(docIdInt, patientDetails);
      Map ftlParamMap = new HashMap();
      ftlParamMap.put("visitdetails", patientDetails);
      ftlParamMap.put("mr_no", patientDetails.get("mr_no"));
      ftlParamMap.put("mr_no_barcode", "*" + patientDetails.get("mr_no") + "*");
      ftlParamMap.put("patient_id", patientDetails.get("patient_id"));
      ftlParamMap.put("modules_activated", APIUtility.getPreferences().getModulesActivatedMap());
      List fieldvalues = PatientHvfDocValuesRepository.getHVFDocValues(docIdInt,
          allFields.equals("Y"));
      ftlParamMap.put("fieldvalues", fieldvalues);
      List imageFieldvalues = PatientHvfDocValuesRepository.getHVFDocImageValues(docIdInt,
          allFields.equals("Y"));
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
          List<String> columns = new ArrayList<>();
          columns.add("field_image");
          Object docImgIdObj = docImgeId;
          List<BasicDynaBean> beans = patienthvfdocimagesrepo.listAll(columns, "doc_image_id",
              docImgIdObj);
          // InputStream is =
          // patientgendocrepo.getHVFDocImage(docImgeId);

          /*
           * ServletContext context = request.getSession().getServletContext(); String virtualPath =
           * "/images/InstaLogo.jpg"; String realPath = context.getRealPath(virtualPath);
           * 
           * File imgFile = new File(realPath); InputStream is = new FileInputStream(imgFile);
           */

          if (beans != null) {
            // FileUtils.writeByteArrayToFile(imgFile, (byte[])
            // beans.get(0).get(columns.get(0)));
            patientgendocrepo.inputStreamToFile((InputStream) beans.get(0).get(columns.get(0)),
                imgFile);
            // is.close();
            imageMap.put("doc_image_id", imgbean.get("doc_image_id"));
            imageMap.put("image_url", imgFile.getAbsolutePath());
            imageMap.put("field_image_content_type", imgbean.get("field_image_content_type"));
            imageMap.put("device_ip", imgbean.get("device_ip"));
            imageMap.put("device_info", imgbean.get("device_info"));
            imageMap.put("capture_time", imgbean.get("capture_time"));
            imageMap.put("field_id", imgbean.get("field_id"));
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
        ftlParamMap.put("vitals", vitalReadingRepository
            .getVitalReadings(patientDetails.get("patient_id").toString(), null));
        ftlParamMap.put("diagnosis_details", mrdDiagnosisRepository
            .getAllDiagnosisDetails(patientDetails.get("patient_id").toString()));
      } else {
        ftlParamMap.put("vitals", null);
        ftlParamMap.put("diagnosis_details", null);
      }
      String templateName = PatientHvfDocValuesRepository.getPrintTemplateName(docIdInt);
      Template templ = null;
      String templateMode = null;

      StringWriter writer = new StringWriter();

      if (templateName == null || templateName.equals("")) {
        templ = AppInit.getFmConfig().getTemplate("PatientHVFDocumentPrint.ftl");
        templateMode = "H";
      } else {
        BasicDynaBean tmpBean = hvfprinttemplaterepo.getTemplateContent(templateName);

        if (tmpBean == null) {
          // couldn't find the template in the db, bail out with
          // error.
          return;
        }
        templateMode = (String) tmpBean.get("template_mode");
        String templateContent = (String) tmpBean.get("hvf_template_content");

        StringReader reader = new StringReader(templateContent);
        templ = new Template("PatientHVFDocumentPrint.ftl", reader, AppInit.getFmConfig());
      }

      templ.process(ftlParamMap, writer);

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
          textContent = new String(
              hc.getText(hvfContent, "Patient HVF Document Print", prefs, true, true));
        }
        requestMap.put("textReport", textContent);
        requestMap.put("textColumns", prefs.get("text_mode_column"));
        requestMap.put("printerType", "DMP");
        return;
        // return mapping.findForward("textPrintApplet");

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
        } catch (Exception exp) {
          log.error("Generated HTML content:");
          log.error(hvfContent);
          throw (exp);
        } finally {
          os.close();
        }
        return;
      }

    } else if (format.equals("doc_rich_templates")) {

      String patHeaderTemplateType = PatientHeaderTemplate.Documents.getType();
      if (new Boolean(docStore.isSpecialized()) && docStore.getDocumentType().equals("service")) {
        patHeaderTemplateType = PatientHeaderTemplate.Ser.getType();
      }

      Map patientDetails = new HashMap();

      genericDocumentsUtil.copyStandardFields(patientDetails, false);
      docStore.copyReplaceableFields(patientDetails, keyParams, false);
      docStore.copyDocumentDetails(docIdInt, patientDetails);
      Map ftlParamMap = new HashMap();
      ftlParamMap.put("visitdetails", patientDetails);
      ftlParamMap.put("mr_no", patientDetails.get("mr_no"));
      ftlParamMap.put("mr_no_barcode", "*" + patientDetails.get("mr_no") + "*");
      ftlParamMap.put("patient_id", patientDetails.get("patient_id"));
      ftlParamMap.put("modules_activated", APIUtility.getPreferences().getModulesActivatedMap());
      StringWriter writer = new StringWriter();
      String patientHeader = phTemplateRepo.getPatientHeader(
          (Integer) patientdocbean.get("pheader_template_id"), patHeaderTemplateType);
      StringReader reader = new StringReader(patientHeader);
      Template templ = new Template("PatientHeader.ftl", reader, AppInit.getFmConfig());
      templ.process(ftlParamMap, writer);
      StringBuilder printContent = new StringBuilder();
      printContent.append(writer.toString());
      String content = (String) patientdocbean.get("doc_content_text");
      // replace the control characters.
      content = content.replaceAll("[\\x00-\\x09\\x0b-\\x1f]", " ");
      StringWriter genericContentWriter = new StringWriter();
      StringReader genericContentReader = new StringReader(content);
      Template genericContentTemplate = new Template("GenericTemplate", genericContentReader,
          AppInit.getFmConfig());
      Map genericMap = new HashMap();
      genericMap.put("visitdetails", patientDetails);
      genericContentTemplate.process(genericMap, genericContentWriter);
      printContent.append(genericContentWriter.toString());
      String docName = "";
      if (patientDetails.get("doc_name") != null) {
        // doesn't have the
        // name.
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
        return;
      } else {
        String textReport = new String(
            hc.getText(printContent.toString(), docName, prefs, true, true, centerId));
        requestMap.put("textReport", textReport);
        requestMap.put("textColumns", prefs.get("text_mode_column"));
        requestMap.put("printerType", "DMP");
        return;
        // return mapping.findForward("textPrintApplet");
      }

    } else if (format.equals("doc_pdf_form_templates")) {
      // prefs are not applicable here, the PDF is pre-formatted.
      // pdftemplaterepo.loadByteaRecords(bean, "template_id",
      // patientdocbean.get("template_id"));

      Map<String, String> fields = new HashMap<>();

      genericDocumentsUtil.copyStandardFields(fields, true);
      docStore.copyReplaceableFields(fields, keyParams, true);

      Map documentDetails = new HashMap();
      docStore.copyDocumentDetails(docIdInt, documentDetails);
      fields.put("_username", (String) documentDetails.get("username"));

      List<BasicDynaBean> fieldslist = pdfvaluesdocrepo.listAll(null, "doc_id", docIdInt);

      for (BasicDynaBean fieldsBean : fieldslist) {
        fields.put(fieldsBean.get("field_name").toString(),
            fieldsBean.get("field_value").toString());
      }

      HashMap<String, String> hiddenParams = new HashMap<>();
      hiddenParams.put("doc_id", params.get("doc_id")[0]);

      response.setContentType("application/pdf");
      OutputStream os = response.getOutputStream();
      BasicDynaBean bean = pdftemplaterepo.findByKey("template_id",
          patientdocbean.get("template_id"));
      InputStream pdf = (InputStream) bean.get("template_content");
      PdfUtils.sendFillableForm(os, pdf, fields, true, null, hiddenParams, null, centerId);

    } else {

      Map docParams = new HashMap();
      docStore.copyDocumentDetails(docIdInt, docParams);

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
      stream.write(
          IOUtils.toByteArray((java.io.InputStream) patientdocbean.get("doc_content_bytea")));
      stream.flush();
      stream.close();
    }
    return;
  }

  /**
   * Gets the specific printer prefs.
   *
   * @param format the format
   * @param template the template
   * @param docId the doc id
   * @param printerId the printer id
   * @return the specific printer prefs
   */
  private BasicDynaBean getSpecificPrinterPrefs(String format, String template, Integer docId,
      String printerId) {
    Integer printIdInt = null;
    BasicDynaBean prefs = null;
    if (null != printerId && !printerId.equals("")) {
      printIdInt = Integer.parseInt(printerId);
    }

    if (template == null || template.isEmpty()) {
      template = PatientHvfDocValuesRepository.getTemplateName(docId, format);
    }

    if ("reg".equals(docStore.getDocumentType())
        && (format.equals("doc_rich_templates") || format.equals("doc_hvf_templates"))) {
      prefs = DocPrintConfigurationRepository
          .getRegistrationPrintConfiguration(template, printIdInt);
    }
    return prefs;
  }

}
