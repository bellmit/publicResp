package com.insta.hms.core.clinical.dischargesummary;

import com.bob.hms.common.APIUtility;
import com.bob.hms.common.Preferences;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.Encoder;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.PdfUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.ftl.FtlReportGenerator;
import com.insta.hms.core.clinical.dischargemedication.DischargeMedicationService;
import com.insta.hms.core.patient.followupdetails.FollowUpService;
import com.insta.hms.documents.DocPatientHeaderTemplatesRepository;
import com.insta.hms.documents.DocPdfFormTemplateRepository;
import com.insta.hms.documents.DocPrintConfigurationRepository;
import com.insta.hms.documents.GenericDocumentsUtil;
import com.insta.hms.documents.PatientDocumentService;
import com.insta.hms.documents.PatientPdfFormValuesRepository;
import com.insta.hms.imageretriever.DoctorConsultImageRetriever;
import com.insta.hms.mdm.formheader.FormHeaderService;
import com.insta.hms.mdm.printerdefinition.PrinterDefinitionService;
import com.insta.hms.mdm.printtemplates.PrintTemplate;
import com.insta.hms.mdm.printtemplates.PrintTemplateService;
import com.lowagie.text.DocumentException;

import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

/**
 * @author anup vishwas.
 *
 */

@Service
public class DischargeSummaryPrintService {
  static Logger log = LoggerFactory.getLogger(DischargeSummaryPrintService.class);

  @LazyAutowired
  private PrintTemplateService printTemplateService;
  @LazyAutowired
  private FormHeaderService formHeaderService;
  @LazyAutowired
  private DischargeSummaryService dischargeSummaryService;
  @LazyAutowired
  private FollowUpService followUpService;
  @LazyAutowired
  private PrinterDefinitionService printerDefinitionService;
  @LazyAutowired
  private DocPatientHeaderTemplatesRepository phTemplateRepo;
  @LazyAutowired
  private GenericDocumentsUtil genericDocumentsUtil;
  @LazyAutowired
  private DischargeHeaderRepository dischargeHeaderRepo;
  @LazyAutowired
  private DischargeDetailRepository dischargeDetailRepo;
  @LazyAutowired
  private DischargeFormatDetailRepository dischargeFormatDetailRepo;
  @LazyAutowired
  private PatientDocumentService patientDocumentService;
  @LazyAutowired
  private DocPdfFormTemplateRepository docpdfformtemplaterepo;
  @LazyAutowired
  private PatientPdfFormValuesRepository patientPdfFormValuesRepo;
  @LazyAutowired
  private DischargeMedicationService dischargeMedicationService;

  public enum ReturnType {
    PDF, PDF_BYTES, TEXT_BYTES
  }

  /**
   * Get all printer definition.
   * 
   * @return map
   * @throws SQLException the exception
   */
  public Map<String, Object> getAllPrinterDefinition() throws SQLException {
    Map<String, Object> printerDefinitionMap = new HashMap<String, Object>();
    Integer printerId = (Integer) DocPrintConfigurationRepository.getDischargeSummaryConfiguration()
        .get("printer_settings");
    printerDefinitionMap.put("printerDefinition",
        ConversionUtils.copyListDynaBeansToMap(printerDefinitionService.lookup(false)));
    printerDefinitionMap.put("showPrinter", printerId);

    return printerDefinitionMap;
  }

  /**
   * Print discharge summary.
   * 
   * @param params the param
   * @param requestMap the request map
   * @param response the response
   * @throws SQLException the exception
   * @throws XPathExpressionException the exception
   * @throws IOException the exception
   * @throws DocumentException the exception
   * @throws TransformerException the exception
   * @throws TemplateException the exception
   */
  public void printDischargeSummary(Map<String, String[]> params, Map<String, Object> requestMap,
      HttpServletResponse response) throws SQLException, XPathExpressionException, IOException,
      DocumentException, TransformerException, TemplateException {

    String docId = params.get("discharge_doc_id")[0];
    if (docId == null) {
      throw new IllegalArgumentException("docid is null");
    }
    int docid = Integer.parseInt(docId);
    String patientId = params.get("patient_id")[0];
    Boolean forcePDF = new Boolean(params.get("forcePdf")[0]);
    String printerIdStr = params.get("printerId")[0];
    Integer printerId = null;
    BasicDynaBean prefs = null;
    if ((printerIdStr != null) && !printerIdStr.equals("")) {
      printerId = Integer.parseInt(printerIdStr);
    }

    prefs = DocPrintConfigurationRepository.getDischargeSummaryPreferences(printerId);
    String printMode = (String) prefs.get("print_mode");
    if (forcePDF) {
      printMode = "P";
    }
    String logoHeader = params.get("logoHeader")[0];
    if (logoHeader != null && !logoHeader.equals("")
        && (logoHeader.equalsIgnoreCase("Y") || logoHeader.equalsIgnoreCase("L")
            || logoHeader.equalsIgnoreCase("H") || logoHeader.equalsIgnoreCase("N"))) {
      prefs.set("logo_header", logoHeader.toUpperCase());
    }

    String format = params.get("discharge_format")[0];
    if (format.equals("F") || format.equals("T")) {
      Preferences sessionPrefs = APIUtility.getPreferences();
      if (printMode.equals("P")) {
        // PDF mode: convert the html to pdf and send:
        OutputStream os = response.getOutputStream();
        response.setContentType("application/pdf");
        try {
          getDischargeSummaryReport(docid, patientId, format, ReturnType.PDF, prefs, sessionPrefs,
              os);
        } catch (TemplateException te) {
          // this cause only for hvf format discharge summary.
          response.reset();
          throw te;
        }
        os.close();
        // return null;
      } else {
        String textReport = new String(getDischargeSummaryReport(docid, patientId, format,
            ReturnType.TEXT_BYTES, prefs, sessionPrefs, null));
        requestMap.put("textReport", textReport);
        requestMap.put("textColumns", prefs.get("text_mode_column"));
      }
    } else if (format.equals("P")) {
      response.setContentType("application/pdf");
      OutputStream os = response.getOutputStream();
      getDischargeSummaryReport(docid, patientId, format, ReturnType.PDF, prefs, null, os);
      os.close();

    }
  }

  /**
   * Get discharge summary report.
   * 
   * @param docId the doc id
   * @param patientId the visit id
   * @param format the format
   * @param rtype the rtype
   * @param pref the pref
   * @param sessionPrefs the session preference
   * @param os the output stream
   * @return byte[]
   * @throws SQLException the exception
   * @throws IOException the exception
   * @throws DocumentException the exception
   * @throws TemplateException the exception
   * @throws XPathExpressionException the exception
   * @throws TransformerException the exception
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public byte[] getDischargeSummaryReport(Integer docId, String patientId, String format,
      ReturnType rtype, BasicDynaBean pref, Preferences sessionPrefs, OutputStream os)
      throws SQLException, IOException, DocumentException, TemplateException,
      XPathExpressionException, TransformerException {

    byte[] bytes = null;
    FtlReportGenerator ftlGen = null;
    FtlReportGenerator dischargeContentftlGen = null;
    
    Pattern treatmentSheetPattern =
        Pattern.compile(".*\\$\\s*\\{\\s*treatmentSheet\\s*!?\\s*\\}.*");
    Pattern consulatationDetailsPattern =
        Pattern.compile(".*\\$\\s*\\{\\s*consultationDetails\\s*!?\\s*\\}.*");
    Pattern otDetailsPattern = Pattern.compile(".*\\$\\s*\\{\\s*otDetails\\s*!?\\s*\\}.*");
    
    String templateContent = printTemplateService.getCustomizedTemplate(PrintTemplate.Dis);
    if (format.equals("F")) {
      if (templateContent == null || templateContent.equals("")) {
        ftlGen = new FtlReportGenerator(PrintTemplate.Dis.getFtlName());
      } else {
        StringReader reader = new StringReader(templateContent);
        ftlGen = new FtlReportGenerator("DischargeHVFPrintTemplate", reader);
      }
      Map<String, String> patientDetailsMap = new HashMap<String, String>();
      genericDocumentsUtil.copyPatientDetails(patientDetailsMap, null, patientId, false);
      List dischargedetails = dischargeHeaderRepo.getFollowupAndDischargeDetails(patientId, docId);
      List fieldValues = dischargeDetailRepo.getFormFieldsValues(docId);
      BasicDynaBean formHeaderBean = formHeaderService.getFormHeaderDetail(docId, patientId);
      String formTitle = (String) formHeaderBean.get("form_title");
      Map templateMap = new HashMap();
      templateMap.put("visitdetails", patientDetailsMap);
      templateMap.put("modules_activated", sessionPrefs.getModulesActivatedMap());
      templateMap.put("dischargedetails", dischargedetails);
      templateMap.put("form_title", formTitle);
      templateMap.put("fieldvalues", fieldValues);
      templateMap.put("dischargemedications",
          dischargeMedicationService.getDischargeMedicationDetails(patientId,
              patientDetailsMap.get("visit_type"), patientDetailsMap.get("org_id")));
      String userName = "";
      if (fieldValues != null && !fieldValues.isEmpty()) {
        userName = (String) ((BasicDynaBean) fieldValues.get(0)).get("username");
      }
      templateMap.put("user_name", userName);

      try {
        if (treatmentSheetPattern.matcher(templateContent).find()) {
          String treatmentInfo = dischargeSummaryService.processTreatment(patientId);
          templateMap.put("treatmentSheet", treatmentInfo);
        }
        if (consulatationDetailsPattern.matcher(templateContent).find()) {
          String consultationDetails =
              dischargeSummaryService.getConsultationDetails(patientId, userName);
          templateMap.put("consultationDetails", consultationDetails);
        }
        if (otDetailsPattern.matcher(templateContent).find()) {
          String otDetails = dischargeSummaryService.getOTDetails(patientId);
          templateMap.put("otDetails", otDetails);
        }
      } catch (Exception exception) {
        log.error("", exception);
      }

      StringWriter writer = new StringWriter();
      try {
        // t.process(templateMap,writer);
        ftlGen.setReportParams(templateMap);
        ftlGen.process(writer);
      } catch (TemplateException te) {
        log.error("", te);
        throw te;
      }

      // TO DO : need to check DoctorConsultImageRetriever
      HtmlConverter hc = new HtmlConverter(new DoctorConsultImageRetriever());
      log.debug("Discharge Summary Template Content : " + writer.toString());
      Boolean repeatPatientHeader = ((String) pref.get("repeat_patient_info")).equals("Y");
      boolean finalized = patientDetailsMap.get("discharge_finalized_date") != null;
      if (rtype.equals(ReturnType.PDF)) {
        hc.writePdf(os, writer.toString(), formTitle, pref, false, repeatPatientHeader, true, true,
            finalized, false);
        os.close();
      } else if (rtype.equals(ReturnType.PDF_BYTES)) {
        bytes = hc.getPdfBytes(writer.toString(), formTitle, pref, repeatPatientHeader, true, true,
            finalized, false);

      } else if (rtype.equals(ReturnType.TEXT_BYTES)) {
        bytes = hc.getText(writer.toString(), formTitle, pref, true, true);
      }

    } else if (format.equals("T")) {
      BasicDynaBean documentBean = dischargeFormatDetailRepo.findByKey("docid", docId);
      String reportContent = (String) documentBean.get("report_file");
      String patientHeader =
          phTemplateRepo.getPatientHeader((Integer) documentBean.get("pheader_template_id"), "D");
      if (reportContent == null) {
        reportContent = "Report is Not Availble";
      } else {
        Map<String, String> patientDetailsMap = new HashMap<String, String>();
        genericDocumentsUtil.copyPatientDetails(patientDetailsMap, null, patientId, false);
        StringReader reader = new StringReader(patientHeader);
        ftlGen = new FtlReportGenerator("PatientHeader", reader);
        Map templateMap = new HashMap();
        templateMap.put("visitdetails", patientDetailsMap);
        templateMap.put("modules_activated", sessionPrefs.getModulesActivatedMap());
        StringReader dischargecontentreader = new StringReader(reportContent);
        dischargeContentftlGen =
            new FtlReportGenerator("DischargeTemplateHeader", dischargecontentreader);
        Map disTemplateMap = new HashMap();
        disTemplateMap.put("visitdetails", patientDetailsMap);
        String dischargedoctorid = patientDetailsMap.get("discharge_doctor_id");
        disTemplateMap.put("discharge_doctor_id", dischargedoctorid);
        disTemplateMap.put("dischargemedications",
            dischargeMedicationService.getDischargeMedicationDetails(patientId,
                patientDetailsMap.get("visit_type"), patientDetailsMap.get("org_id")));

        if (treatmentSheetPattern.matcher(templateContent).find()) {
          String treatmentInfo = dischargeSummaryService.processTreatment(patientId);
          disTemplateMap.put("treatmentSheet", treatmentInfo);
          //for module activation info 
          disTemplateMap.put("modules_activated", sessionPrefs.getModulesActivatedMap());
          templateMap.put("treatmentSheet", treatmentInfo);
        }
        
        StringWriter writer = new StringWriter();
        StringWriter dischargeContentwriter = new StringWriter();
        try {
          // t.process(templateMap, writer);
          ftlGen.setReportParams(templateMap);
          ftlGen.process(writer);
          dischargeContentftlGen.setReportParams(disTemplateMap);
          dischargeContentftlGen.process(dischargeContentwriter);
        } catch (TemplateException te) {
          log.error("", te);
          throw te;
        }
        log.debug("Discharge Rich Text(Editor) Template Patient Header:" + writer.toString());
        StringBuilder html = new StringBuilder("");
        html.append(writer.toString());
        html.append(dischargeContentwriter.toString());
        List<BasicDynaBean> followUpDetailsList = followUpService.getfollowUpDetails(patientId);
        if (followUpDetailsList.size() > 0) {
          html.append("<table cellspacing='0' cellpadding='2' width='100%'><tbody>");
          html.append("<tr height='10'></tr>");
          html.append("<tr><td align='left' colspan='3'><b>Follow Up Details</b></td></tr>");
          html.append("<tr height='10'></tr>");
          for (int f = 0; f < followUpDetailsList.size(); f++) {
            BasicDynaBean bean = followUpDetailsList.get(f);
            String followUpDate = Encoder.cleanHtml((String) bean.get("followup_date"));
            String doctorName = Encoder.cleanHtml((String) bean.get("doctor_name"));
            String followUpRemarks = Encoder.cleanHtml((String) bean.get("followup_remarks"));
            html.append("<tr><td width='15%'>" + followUpDate
                + "</td><td width='25%'>" + doctorName
                + "</td><td width='60%'>" + followUpRemarks + "</td></tr>");
          }
          html.append("</tbody></table>\n");
        }
        reportContent = html.toString();
        HtmlConverter hc = new HtmlConverter(new DoctorConsultImageRetriever());
        Boolean repeatPatientHeader = ((String) pref.get("repeat_patient_info")).equals("Y");
        boolean finalized = patientDetailsMap.get("discharge_finalized_date") != null;
        if (rtype.equals(ReturnType.PDF)) {
          hc.writePdf(os, reportContent, "Discharge Summary", pref, false, repeatPatientHeader,
              true, true, finalized, false);
          os.close();
        } else if (rtype.equals(ReturnType.TEXT_BYTES)) {
          bytes = hc.getText(reportContent, "Discharge Summary", pref, true, true);
        }
      }
    } else if (format.equals("P")) {
      int templateId = (Integer) patientDocumentService.findByKey(docId).get("template_id");
      BasicDynaBean docPdfFormBean = docpdfformtemplaterepo.findByKey("template_id", templateId);
      InputStream pdf = (InputStream) docPdfFormBean.get("template_content");
      Map<String, String> fields = new HashMap<String, String>();
      genericDocumentsUtil.copyStandardFields(fields, true);
      genericDocumentsUtil.copyPatientDetails(fields, null, patientId, true);
      List<BasicDynaBean> fieldslist = patientPdfFormValuesRepo.listAll(null, "doc_id", docId);
      for (BasicDynaBean fieldsBean : fieldslist) {
        fields.put(fieldsBean.get("field_name").toString(),
            fieldsBean.get("field_value").toString());
      }
      if (rtype.equals(ReturnType.PDF)) {
        PdfUtils.sendFillableForm(os, pdf, fields, true, null, null, null);
        os.close();
      } else if (rtype.equals(ReturnType.PDF_BYTES)) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        PdfUtils.sendFillableForm(stream, pdf, fields, true, null, null, null);
        bytes = stream.toByteArray();
        stream.close();
      }
    }

    return bytes;
  }

}
