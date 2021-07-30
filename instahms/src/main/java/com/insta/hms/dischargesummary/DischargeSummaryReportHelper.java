package com.insta.hms.dischargesummary;

import com.bob.hms.common.Preferences;
import com.bob.hms.common.RequestContext;
import com.insta.hms.OTServices.OperationDetailsDAO;
import com.insta.hms.OTServices.OtRecord.OtRecordDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.PdfUtils;
import com.insta.hms.common.ftl.FtlReportGenerator;
import com.insta.hms.core.patient.followupdetails.FollowUpService;
import com.insta.hms.diagnosticmodule.common.DiagnosticsDAO;
import com.insta.hms.dischargemedication.DischargeMedicationDAO;
import com.insta.hms.genericdocuments.GenericDocumentsFields;
import com.insta.hms.genericdocuments.PatientDocumentsDAO;
import com.insta.hms.genericdocuments.PatientHVFDocValuesDAO;
import com.insta.hms.imageretriever.DoctorConsultImageRetriever;
import com.insta.hms.instaforms.AbstractInstaForms;
import com.insta.hms.instaforms.OTForms;
import com.insta.hms.instaforms.PatientSectionDetailsDAO;
import com.insta.hms.ipservices.PrescriptionViewDAO;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.PatientHeaderTemplate.PatientHeaderTemplateDAO;
import com.insta.hms.master.PrescriptionsMaster.PrescriptionsMasterDAO;
import com.insta.hms.master.PrintTemplates.PrintTemplate;
import com.insta.hms.master.PrintTemplates.PrintTemplatesDAO;
import com.insta.hms.master.Sections.SectionsDAO;
import com.insta.hms.medicalrecorddepartment.MRDDiagnosisDAO;
import com.insta.hms.outpatient.AllergiesDAO;
import com.insta.hms.outpatient.ConsultationFieldValuesDAO;
import com.insta.hms.outpatient.DoctorConsultImagesDAO;
import com.insta.hms.outpatient.DoctorConsultationDAO;
import com.insta.hms.outpatient.HealthMaintenanceDAO;
import com.insta.hms.outpatient.PhysicianFormValuesDAO;
import com.insta.hms.outpatient.SecondaryComplaintDAO;
import com.insta.hms.services.ServicesDAO;
import com.insta.hms.stores.MedicineSalesDAO;
import com.insta.hms.usermanager.UserDAO;
import com.insta.hms.vitalForm.genericVitalFormDAO;
import com.insta.hms.vitalparameter.VitalMasterDAO;
import com.lowagie.text.DocumentException;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

/**
 * The Class DischargeSummaryReportHelper.
 *
 * @author krishna.t
 */
public class DischargeSummaryReportHelper {

  /** The log. */
  static Logger log = LoggerFactory.getLogger(DischargeSummaryReportHelper.class);

  /** The dao. */
  private DischargeSummaryDAOImpl dao = new DischargeSummaryDAOImpl();

  /** The p doc dao. */
  private PatientDocumentsDAO patDocDao = new PatientDocumentsDAO();

  /** The pdftemplatedao. */
  private GenericDAO pdftemplatedao = new GenericDAO("doc_pdf_form_templates");

  /** The pdffieldsvaluesdao. */
  private GenericDAO pdffieldsvaluesdao = new GenericDAO("patient_pdf_form_doc_values");

  /** The ph template dao. */
  private PatientHeaderTemplateDAO phTemplateDao = new PatientHeaderTemplateDAO();

  /** The patientdocdao. */
  private PatientDocumentsDAO patientdocdao = new PatientDocumentsDAO();

  /** The consult image dao. */
  private DoctorConsultImagesDAO consultImageDao = new DoctorConsultImagesDAO();

  /** The follow up dao. */
  private GenericDAO followUpDao = new GenericDAO("follow_up_details");

  /** The scomplaint dao. */
  private SecondaryComplaintDAO scomplaintDao = new SecondaryComplaintDAO();

  /** The ot DAO. */
  private OtRecordDAO otDAO = new OtRecordDAO();

  /** The pfv DAO. */
  private PhysicianFormValuesDAO pfvDAO = new PhysicianFormValuesDAO();

  /** The discharge medication DAO. */
  private DischargeMedicationDAO dischargeMedicationDAO = new DischargeMedicationDAO();

  /** The follow up service. */
  FollowUpService followUpService = (FollowUpService) ApplicationContextProvider
      .getApplicationContext().getBean("followUpService");

  /** The cfg. */
  private Configuration cfg = null;

  /**
   * Instantiates a new discharge summary report helper.
   */
  public DischargeSummaryReportHelper() {
    cfg = AppInit.getFmConfig();
  }

  /**
   * Instantiates a new discharge summary report helper.
   *
   * @param cfg the cfg
   */
  public DischargeSummaryReportHelper(Configuration cfg) {
    this.cfg = cfg;
  }

  /**
   * The Enum returnType.
   */
  public enum ReturnType {

    /** The pdf. */
    PDF,

    /** The pdf bytes. */
    PDF_BYTES,

    /** The text bytes. */
    TEXT_BYTES
  }

  /**
   * The Enum formatType.
   */
  public enum FormatType {

    /** The hvf. */
    HVF("F"),

    /** The pdf. */
    PDF("P"),

    /** The rich text. */
    RICH_TEXT("T");

    /** The format. */
    String format;

    /**
     * Instantiates a new format type.
     *
     * @param format the format
     */
    FormatType(String format) {
      this.format = format;
    }

    /**
     * Gets the format.
     *
     * @return the format
     */
    public String getFormat() {
      return format;
    }
  }

  /**
   * Gets the discharge summary report.
   *
   * @param docId the doc id
   * @param format the format
   * @param retType the ret type
   * @param pref the pref
   * @param sessionPrefs the session prefs
   * @param os the os
   * @return the discharge summary report
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws DocumentException the document exception
   * @throws TemplateException the template exception
   * @throws XPathExpressionException the x path expression exception
   * @throws TransformerException the transformer exception
   */
  public byte[] getDischargeSummaryReport(Integer docId, FormatType format, ReturnType retType,
      BasicDynaBean pref, Preferences sessionPrefs, OutputStream os)
      throws SQLException, IOException, DocumentException, TemplateException,
      XPathExpressionException, TransformerException {

    Integer centerId = RequestContext.getCenterId();
    byte[] bytes = null;
    String patientId = dao.getPatientId(docId, format);
    FtlReportGenerator ftlGen = null;
    FtlReportGenerator dischargeContentftlGen = null;

    if (format.equals(FormatType.HVF)) {
      PrintTemplatesDAO printtemplatedao = new PrintTemplatesDAO();
      String templateContent = printtemplatedao.getCustomizedTemplate(PrintTemplate.Dis);

      if (templateContent == null || templateContent.equals("")) {
        ftlGen = new FtlReportGenerator(PrintTemplate.Dis.getFtlName());
      } else {
        StringReader reader = new StringReader(templateContent);
        ftlGen = new FtlReportGenerator("DischargeHVFPrintTemplate", reader);
      }
      Map<String, String> patientDetailsMap = new HashMap<String, String>();
      GenericDocumentsFields.copyPatientDetails(patientDetailsMap, null, patientId, false);
      List dischargedetails =
          DischargeSummaryDAOImpl.getFollowupAndDischargeDetails(patientId, docId);
      List fieldValues = new DischargeSummaryDAOImpl().getFormFieldsValuesFromDatabase(docId);

      String formTitle = DischargeSummaryDAOImpl.getHVFTemplateTitle(docId, patientId);
      int patientCenterId = VisitDetailsDAO.getCenterId(patientId);
      String helathAuthority = CenterMasterDAO.getHealthAuthorityForCenter(patientCenterId);
      BasicDynaBean genericPrefs = GenericPreferencesDAO.getAllPrefs();
      Boolean useStoreItems = genericPrefs.get("prescription_uses_stores").equals("Y");
      String visitType = (String) patientDetailsMap.get("visit_type");
      String orgId = (String) patientDetailsMap.get("org_id");
      Map templateMap = new HashMap();
      templateMap.put("visitdetails", patientDetailsMap);
      templateMap.put("modules_activated", sessionPrefs.getModulesActivatedMap());
      templateMap.put("dischargedetails", dischargedetails);
      templateMap.put("form_title", formTitle);
      templateMap.put("fieldvalues", fieldValues);
      templateMap.put("dischargemedications", dischargeMedicationDAO.getDischargeMedicationDetails(
          patientId, helathAuthority, useStoreItems, visitType, orgId));
      String userName = "";
      if (fieldValues != null && !fieldValues.isEmpty()) {
        userName = (String) ((BasicDynaBean) fieldValues.get(0)).get("username");
      }
      templateMap.put("user_name", userName);

      /*
       * the following (investigations/medicines/services) are treatment given to the patient.
       */
      Pattern treatmentSheetPattern =
          Pattern.compile(".*\\$\\s*\\{\\s*treatmentSheet\\s*!?\\s*\\}.*");
      Pattern consulatationDetailsPattern =
          Pattern.compile(".*\\$\\s*\\{\\s*consultationDetails\\s*!?\\s*\\}.*");
      Pattern otDetailsPattern = Pattern.compile(".*\\$\\s*\\{\\s*otDetails\\s*!?\\s*\\}.*");

      try {
        if (treatmentSheetPattern.matcher(templateContent).find()) {
          String treatmentInfo = processTreatment(patientId);
          templateMap.put("treatmentSheet", treatmentInfo);
        }
        if (consulatationDetailsPattern.matcher(templateContent).find()) {
          String consultationDetails = getConsultationDetails(patientId, userName);
          templateMap.put("consultationDetails", consultationDetails);
        }
        if (otDetailsPattern.matcher(templateContent).find()) {
          String otDetails = getOTDetails(patientId);
          templateMap.put("otDetails", otDetails);
        }
      } catch (Exception exp) {
        log.error("", exp);
      }

      StringWriter writer = new StringWriter();
      try {
        ftlGen.setReportParams(templateMap);
        ftlGen.process(writer);
      } catch (TemplateException te) {
        log.error("", te);
        throw te;
      }
      HtmlConverter hc = new HtmlConverter(new DoctorConsultImageRetriever());
      log.debug("Discharge Summary Template Content : " + writer.toString());
      Boolean repeatPatientHeader = ((String) pref.get("repeat_patient_info")).equals("Y");
      boolean finalized = patientDetailsMap.get("discharge_finalized_date") != null;
      if (retType.equals(ReturnType.PDF)) {
        hc.writePdf(os, writer.toString(), formTitle, pref, false, repeatPatientHeader, true, true,
            finalized, false);
        os.close();
      } else if (retType.equals(ReturnType.PDF_BYTES)) {
        bytes = hc.getPdfBytes(writer.toString(), formTitle, pref, repeatPatientHeader, true, true,
            finalized, false);

      } else if (retType.equals(ReturnType.TEXT_BYTES)) {
        bytes = hc.getText(writer.toString(), formTitle, pref, true, true);
      }

    } else if (format.equals(FormatType.RICH_TEXT)) {
      BasicDynaBean document = dao.getRichTextDocument(docId);
      String reportContent = (String) document.get("report_file");
      String patientHeader =
          phTemplateDao.getPatientHeader((Integer) document.get("pheader_template_id"), "D");
      if (reportContent == null) {
        reportContent = "Report is Not Availble";
      } else {
        Map<String, String> patientDetailsMap = new HashMap<String, String>();
        GenericDocumentsFields.copyPatientDetails(patientDetailsMap, null, patientId, false);
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
            dischargeMedicationDAO.getDischargeMedicationDetails(patientId, centerId));

        StringWriter writer = new StringWriter();
        StringWriter dischargeContentwriter = new StringWriter();
        try {
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
        ArrayList arrFollowUpDetails = dao.getfollowUpDetails(patientId);
        if (arrFollowUpDetails.size() > 0) {
          html.append("<table cellspacing='0' cellpadding='2' width='100%'><tbody>");
          html.append("<tr height='10'></tr>");
          html.append("<tr><td align='left' colspan='3'><b>Follow Up Details</b></td></tr>");
          html.append("<tr height='10'></tr>");

          for (int f = 0; f < arrFollowUpDetails.size(); f++) {

            Hashtable htFollowUpDetails = (Hashtable) arrFollowUpDetails.get(f);
            html.append("<tr><td width='15%'>" + (String) htFollowUpDetails.get("FOLLOWUP_DATE")
                + "</td><td width='25%'>" + (String) htFollowUpDetails.get("DOCTOR_NAME")
                + "</td><td width='60%'>" + (String) htFollowUpDetails.get("FOLLOWUP_REMARKS")
                + "</td></tr>");
          }
          html.append("</tbody></table>\n");
        }
        reportContent = html.toString();
        HtmlConverter hc = new HtmlConverter(new DoctorConsultImageRetriever());
        Boolean repeatPatientHeader = ((String) pref.get("repeat_patient_info")).equals("Y");
        boolean finalized = patientDetailsMap.get("discharge_finalized_date") != null;
        if (retType.equals(ReturnType.PDF)) {
          hc.writePdf(os, reportContent, "Discharge Summary", pref, false, repeatPatientHeader,
              true, true, finalized, false);
          os.close();

        } else if (retType.equals(ReturnType.PDF_BYTES)) {
          bytes = hc.getPdfBytes(reportContent, "Discharge Summary", pref, repeatPatientHeader,
              true, true, finalized, false);

        } else if (retType.equals(ReturnType.TEXT_BYTES)) {
          bytes = hc.getText(reportContent, "Discharge Summary", pref, true, true);
        }
      }

    } else if (format.equals(FormatType.PDF)) {
      BasicDynaBean patientdocbean = patDocDao.getBean();

      patDocDao.loadByteaRecords(patientdocbean, "doc_id", docId);
      BasicDynaBean bean = pdftemplatedao.getBean();
      pdftemplatedao.loadByteaRecords(bean, "template_id", patientdocbean.get("template_id"));
      InputStream pdf = (InputStream) bean.get("template_content");

      Map<String, String> fields = new HashMap<String, String>();
      GenericDocumentsFields.copyStandardFields(fields, true);
      GenericDocumentsFields.copyPatientDetails(fields, null, patientId, true);

      List<BasicDynaBean> fieldslist = pdffieldsvaluesdao.listAll(null, "doc_id", docId);

      for (BasicDynaBean fieldsBean : fieldslist) {
        fields.put(fieldsBean.get("field_name").toString(),
            fieldsBean.get("field_value").toString());
      }
      DischargeSummaryReportHelper.copyFollowupDetails(fields,
          new DischargeSummaryDAOImpl().getfollowUpDetails(patientId));

      if (retType.equals(ReturnType.PDF)) {
        PdfUtils.sendFillableForm(os, pdf, fields, true, null, null, null);
        os.close();
      } else if (retType.equals(ReturnType.PDF_BYTES)) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        PdfUtils.sendFillableForm(stream, pdf, fields, true, null, null, null);
        bytes = stream.toByteArray();
        stream.close();
      }
    }
    return bytes;
  }

  /**
   * Removes the header and footer.
   *
   * @param reports the reports
   * @return the list
   */
  public static List<BasicDynaBean> removeHeaderAndFooter(List<BasicDynaBean> reports) {
    List<BasicDynaBean> list = new ArrayList<BasicDynaBean>();
    for (BasicDynaBean bean : reports) {
      try {
        String contentAfterRemove =
            HtmlConverter.deleteHeaderAndFooter((String) bean.get("report_data"));
        bean.set("report_data", contentAfterRemove);
        list.add(bean);
      } catch (Exception exp) {
        log.error("invalid report content unable to "
            + "remove patient header and footer for report id: " + bean.get("report_id"));
      }
    }
    return list;
  }

  /**
   * Copy followup details.
   *
   * @param map the map
   * @param followupList the followup list
   */
  public static void copyFollowupDetails(Map map, ArrayList<Hashtable> followupList) {
    int index = 1;
    if (followupList != null) {
      for (Hashtable table : followupList) {
        map.put("_followup_doctorName_" + index, table.get("DOCTOR_NAME"));
        map.put("_followup_remarks_" + index, table.get("FOLLOWUP_REMARKS"));
        map.put("_followup_date_" + index, table.get("FOLLOWUP_DATE"));
        index++;
      }
    }
  }

  /**
   * The Class Operation.
   */
  public class Operation {

    /** The operation name. */
    private String operationName;

    /** The notes. */
    private String notes;

    /** The rich text content. */
    private String richTextContent;

    /** The hvf values. */
    private List hvfValues = new ArrayList();

    /** The prescribed id. */
    private int prescribedId;

    /** The format. */
    private String format;

    /**
     * Instantiates a new operation.
     *
     * @param operationName the operation name
     * @param prescribedId the prescribed id
     */
    public Operation(String operationName, int prescribedId) {
      this.operationName = operationName;
      this.prescribedId = prescribedId;
    }

    /**
     * Gets the operation name.
     *
     * @return the operation name
     */
    public String getOperationName() {
      return operationName;
    }

    /**
     * Gets the prescribed id.
     *
     * @return the prescribed id
     */
    public int getPrescribedId() {
      return prescribedId;
    }

    /**
     * Sets the format.
     *
     * @param format the new format
     */
    public void setFormat(String format) {
      this.format = format;
    }

    /**
     * Gets the format.
     *
     * @return the format
     */
    public String getFormat() {
      return format;
    }

    /**
     * Sets the notes.
     *
     * @param notes the new notes
     */
    public void setNotes(String notes) {
      this.notes = notes;
    }

    /**
     * Gets the notes.
     *
     * @return the notes
     */
    public String getNotes() {
      return notes;
    }

    /**
     * Gets the rich text content.
     *
     * @return the rich text content
     */
    public String getRichTextContent() {
      return richTextContent;
    }

    /**
     * Gets the hvf values.
     *
     * @return the hvf values
     */
    public List getHvfValues() {
      return hvfValues;
    }
  }

  /**
   * Process treatment.
   *
   * @param patientId the patient id
   * @return the string
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws TemplateException the template exception
   */
  public String processTreatment(String patientId)
      throws SQLException, IOException, TemplateException {
    Map templateMap = new HashMap();
    templateMap.put("medicines", MedicineSalesDAO.getMedicinesPrescribedAndSold(patientId));
    templateMap.put("tests", getTestValuesForPatient(patientId));
    templateMap.put("services", getServicesForPatient(patientId));
    templateMap.put("operation_presc",
        PrescriptionViewDAO.getOpetaionPrescriptionsCompleted(patientId));
    List<BasicDynaBean> operations = PrescriptionViewDAO.getOpetaionsCompleted(patientId);
    templateMap.put("operations", operations);
    List operationDocuments = new ArrayList();

    for (BasicDynaBean b : operations) {
      Integer docId = (Integer) b.get("doc_id");
      String format = (String) b.get("doc_format");
      String operationName = (String) b.get("name");
      Integer prescribedId = (Integer) b.get("prescribed_id");
      Operation op = new Operation(operationName, prescribedId);
      op.setNotes((String) b.get("remarks"));
      op.setFormat(format);

      if (docId != null) {
        if (format.equals("doc_hvf_templates")) {
          List fieldvalues = PatientHVFDocValuesDAO.getHVFDocValues(docId, true);
          op.hvfValues.addAll(fieldvalues);
        } else if (format.equals("doc_rich_templates")) {
          BasicDynaBean patientdocbean = patientdocdao.getBean();
          patientdocdao.loadByteaRecords(patientdocbean, "doc_id", docId);
          String content = (String) patientdocbean.get("doc_content_text");
          op.richTextContent = content;
        }
      }
      operationDocuments.add(op);
    }
    templateMap.put("operation_documents", operationDocuments);

    List imageColumnList = new ArrayList();
    imageColumnList.add("image_id");
    imageColumnList.add("datetime");
    List patientConsultations = new ArrayList();
    Map modulesActivatedMap =
        ((Preferences) RequestContext.getSession().getAttribute("preferences"))
            .getModulesActivatedMap();
    String modPharmacy = (String) (modulesActivatedMap.get("mod_pharmacy"));
    modPharmacy = modPharmacy == null ? "" : modPharmacy;

    VitalMasterDAO vmDAO = new VitalMasterDAO();
    templateMap.put("vital_params", vmDAO.getActiveVitalParams("O"));
    List<BasicDynaBean> noOfConsultations =
        new DoctorConsultationDAO().findAllByKey("patient_id", patientId);
    for (BasicDynaBean consult : noOfConsultations) {
      if (consult.get("status").equals("U")) {
        continue;
      }

      int consultationId = (Integer) consult.get("consultation_id");
      Map consultationMap = new HashMap();
      // cw: consultation wise
      consultationMap.put("cw_medicines",
          PrescriptionsMasterDAO.getPrescribedItems("Medicine", consultationId, modPharmacy));
      consultationMap.put("cw_services",
          PrescriptionsMasterDAO.getPrescribedItems("Service", consultationId, modPharmacy));
      consultationMap.put("cw_tests",
          PrescriptionsMasterDAO.getPrescribedItems("Test", consultationId, modPharmacy));
      consultationMap.put("cw_crossConsultations",
          PrescriptionsMasterDAO.getPrescribedItems("Doctor", consultationId, modPharmacy));
      consultationMap.put("cw_vitals", Collections.EMPTY_LIST); // added for legacy support.
      consultationMap.put("cw_vital_values", genericVitalFormDAO.groupByReadingId(patientId, "V"));
      consultationMap.put("cw_images",
          consultImageDao.listAll(imageColumnList, "consultation_id", consultationId));
      consultationMap.put("consultation_details",
          DoctorConsultationDAO.getConsultDetails(consultationId));
      consultationMap.put("cw_hvf_fields", ConsultationFieldValuesDAO
          .getConsultationFieldsValuesForPrint(consultationId, true, true));
      patientConsultations.add(consultationMap);
    }
    templateMap.put("patientConsultations", patientConsultations);
    templateMap.put("diagnosis_details", MRDDiagnosisDAO.getAllDiagnosisDetails(patientId));

    List<BasicDynaBean> followupBean = followUpService.getfollowUpDetails(patientId);
    if (followupBean != null && followupBean.size() > 0) {
      templateMap.put("followup_date", followupBean.get(0).get("followup_date"));
      templateMap.put("followupDetails", followupBean);
    }
    BasicDynaBean visitBean =
        new GenericDAO("patient_registration").findByKey("patient_id", patientId);
    templateMap.put("complaint", (String) visitBean.get("complaint"));

    PrintTemplatesDAO printtemplatedao = new PrintTemplatesDAO();
    String templateContent = printtemplatedao.getCustomizedTemplate(PrintTemplate.TreatmentSheet);
    FtlReportGenerator ftlGen = null;

    if (templateContent == null || templateContent.equals("")) {
      ftlGen = new FtlReportGenerator(PrintTemplate.TreatmentSheet.getFtlName());
    } else {
      StringReader reader = new StringReader(templateContent);
      ftlGen = new FtlReportGenerator("TreatmentSheetPrintTemplate", reader);
    }
    StringWriter writer = new StringWriter();
    try {
      ftlGen.setReportParams(templateMap);
      ftlGen.process(writer);
    } catch (TemplateException te) {
      log.error("", te);
      throw te;
    }
    return writer.toString();
  }

  /**
   * The Class Test.
   */
  public class Test {

    /** The prescribed id. */
    private int prescribedId;

    /** The test name. */
    private String testName;

    /** The notes. */
    private String notes;

    /** The format. */
    private String format;

    /** The test values. */
    private List testValues = new ArrayList();

    /**
     * Instantiates a new test.
     *
     * @param prescribedId the prescribed id
     * @param testName the test name
     * @param notes the notes
     * @param format the format
     */
    public Test(int prescribedId, String testName, String notes, String format) {
      this.prescribedId = prescribedId;
      this.testName = testName;
      this.notes = notes;
      this.format = format;
    }

    /**
     * Gets the test values.
     *
     * @return the test values
     */
    public List getTestValues() {
      return testValues;
    }

    /**
     * Gets the test name.
     *
     * @return the test name
     */
    public String getTestName() {
      return testName;
    }

    /**
     * Gets the prescribed id.
     *
     * @return the prescribed id
     */
    public int getPrescribedId() {
      return prescribedId;
    }

    /**
     * Gets the notes.
     *
     * @return the notes
     */
    public String getNotes() {
      return notes;
    }

    /**
     * Gets the format.
     *
     * @return the format
     */
    public String getFormat() {
      return format;
    }
  }

  /**
   * Gets the test values for patient.
   *
   * @param patientId the patient id
   * @return the test values for patient
   * @throws SQLException the SQL exception
   */
  public List getTestValuesForPatient(String patientId) throws SQLException {
    List<BasicDynaBean> dynalist = DiagnosticsDAO.getTestValuesForPatient(patientId);

    List<Test> testsList = new ArrayList<Test>();
    int prescribedID = 0;
    Test test = null;
    for (BasicDynaBean bean : dynalist) {
      if ((Integer) bean.get("prescribed_id") != prescribedID) {
        test = new Test((Integer) bean.get("prescribed_id"), (String) bean.get("test_name"),
            (String) bean.get("remarks"), (String) bean.get("conducted_in_reportformat"));
        testsList.add(test);
      }
      if (test != null) {
        test.testValues.add(bean);
      }
      prescribedID = (Integer) bean.get("prescribed_id");
    }
    return testsList;
  }

  /**
   * The Class Service.
   */
  public class Service {

    /** The service details. */
    private BasicDynaBean serviceDetails;

    /** The service name. */
    private String serviceName;

    /** The format. */
    private String format;

    /** The notes. */
    private String notes;

    /** The hvf values. */
    private List hvfValues = new ArrayList();

    /** The rich text content. */
    private String richTextContent;

    /**
     * Instantiates a new service.
     *
     * @param serviceName the service name
     * @param notes the notes
     */
    public Service(String serviceName, String notes) {
      this.serviceName = serviceName;
      this.notes = notes;
    }

    /**
     * Gets the service name.
     *
     * @return the service name
     */
    public String getServiceName() {
      return serviceName;
    }

    /**
     * Gets the notes.
     *
     * @return the notes
     */
    public String getNotes() {
      return notes;
    }

    /**
     * Gets the format.
     *
     * @return the format
     */
    public String getFormat() {
      return format;
    }

    /**
     * Sets the format.
     *
     * @param format the new format
     */
    public void setFormat(String format) {
      this.format = format;
    }

    /**
     * Sets the rich text content.
     *
     * @param richTextContent the new rich text content
     */
    public void setRichTextContent(String richTextContent) {
      this.richTextContent = richTextContent;
    }

    /**
     * Gets the hvf values.
     *
     * @return the hvf values
     */
    public List getHvfValues() {
      return hvfValues;
    }
  }

  /**
   * Gets the services for patient.
   *
   * @param patientId the patient id
   * @return the services for patient
   * @throws SQLException the SQL exception
   */
  public List getServicesForPatient(String patientId) throws SQLException {
    List<BasicDynaBean> dynalist = ServicesDAO.getServiceDetails(patientId);
    ArrayList<Service> serviceList = new ArrayList<Service>();
    for (BasicDynaBean b : dynalist) {
      Service service = new Service((String) b.get("service_name"), (String) b.get("remarks"));
      service.serviceDetails = b;
      String format = (String) b.get("doc_format");
      service.setFormat(format);
      Integer docId = (Integer) b.get("doc_id");
      if (docId != null) {
        if (format.equals("doc_hvf_templates")) {
          List fieldvalues = PatientHVFDocValuesDAO.getHVFDocValues(docId, true);
          service.hvfValues.addAll(fieldvalues);
        } else if (format.equals("doc_rich_templates")) {
          BasicDynaBean patientdocbean = patientdocdao.getBean();
          patientdocdao.loadByteaRecords(patientdocbean, "doc_id", docId);
          String content = (String) patientdocbean.get("doc_content_text");
          service.richTextContent = content;
        }
      }

      serviceList.add(service);
    }
    return serviceList;
  }

  /**
   * Gets the consultation details.
   *
   * @param patientId the patient id
   * @param userName the user name
   * @return the consultation details
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws TemplateException the template exception
   */
  public String getConsultationDetails(String patientId, String userName)
      throws SQLException, IOException, TemplateException {
    List<BasicDynaBean> consultations = DoctorConsultationDAO.getConsultations(patientId);
    UserDAO userdao = new UserDAO();
    List<Map> consultationsList = new ArrayList<Map>();
    Map patDetails = new HashMap();
    GenericDocumentsFields.copyPatientDetails(patDetails, null, patientId, false);
    for (BasicDynaBean bean : consultations) {
      int consultId = (Integer) bean.get("consultation_id");
      BasicDynaBean genericPrefs = GenericPreferencesDAO.getAllPrefs();
      String useStoreItems = (String) genericPrefs.get("prescription_uses_stores");
      BasicDynaBean consultBean = DoctorConsultationDAO.getConsultDetails(consultId);

      Map ftlParamMap = new HashMap();
      List allPrescriptions =
          PrescriptionsMasterDAO.getAllPrescriptions(consultId, patientId, useStoreItems, null);
      ftlParamMap.put("presMedicines",
          PrescriptionsMasterDAO.getPrescribedItems(allPrescriptions, "Medicine"));
      ftlParamMap.put("presServices",
          PrescriptionsMasterDAO.getPrescribedItems(allPrescriptions, "Service"));
      ftlParamMap.put("presTests",
          PrescriptionsMasterDAO.getPrescribedItems(allPrescriptions, "Inv."));
      ftlParamMap.put("presConsultation",
          PrescriptionsMasterDAO.getPrescribedItems(allPrescriptions, "Doctor"));
      ftlParamMap.put("presInstructions",
          PrescriptionsMasterDAO.getPrescribedItems(allPrescriptions, "Instructions"));
      ftlParamMap.put("NonHospitalItems",
          PrescriptionsMasterDAO.getPrescribedItems(allPrescriptions, "NonHospital"));
      ftlParamMap.put("presOperations",
          PrescriptionsMasterDAO.getPrescribedItems(allPrescriptions, "Operation"));
      ftlParamMap.put("consultation_bean", consultBean);

      AbstractInstaForms formDAO = AbstractInstaForms.getInstance("Form_CONS");

      Map params = new HashMap();
      params.put("consultation_id", new String[] {consultId + ""});
      BasicDynaBean compBean = formDAO.getComponents(params);

      ftlParamMap.put("vitals", Collections.EMPTY_LIST); // added for legacy support.
      ftlParamMap.put("vital_values",
          genericVitalFormDAO.groupByReadingId((String) consultBean.get("patient_id"), "V"));
      String itemType = (String) formDAO.getKeys().get("item_type");
      ftlParamMap.put("health_maintenance",
          HealthMaintenanceDAO.getAllHealthMaintenance((String) consultBean.get("mr_no"), patientId,
              consultId, 0, (Integer) compBean.get("form_id"), itemType));
      List consultationFields =
          ConsultationFieldValuesDAO.getConsultationFieldsValuesForPrint(consultId, true, true);

      ftlParamMap.put("consultationFields", consultationFields);
      ftlParamMap.put("noteTakerEnabled", userdao.getPrescriptionNoteTaker(userName));
      List imageColumnList = new ArrayList();
      imageColumnList.add("image_id");
      imageColumnList.add("datetime");
      ftlParamMap.put("consultImages",
          consultImageDao.listAll(imageColumnList, "consultation_id", consultId));
      List<BasicDynaBean> followupBean = followUpService.getfollowUpDetails(patientId);
      if (followupBean != null && followupBean.size() > 0) {
        ftlParamMap.put("followup_date", followupBean.get(0).get("followup_date"));
        ftlParamMap.put("followupDetails", followupBean);
      }
      ftlParamMap.put("allergies",
          AllergiesDAO.getAllActiveAllergies((String) consultBean.get("mr_no"),
              (String) consultBean.get("patient_id"), consultId, 0,
              (Integer) compBean.get("form_id"), itemType));

      ftlParamMap.put("secondary_complaints", new SecondaryComplaintDAO()
          .getSecondaryComplaints((String) consultBean.get("patient_id")));

      PatientSectionDetailsDAO psdDAO = new PatientSectionDetailsDAO();
      List<BasicDynaBean> consValues = psdDAO.getAllSectionDetails(
          (String) consultBean.get("mr_no"), (String) consultBean.get("patient_id"), consultId, 0,
          (Integer) compBean.get("form_id"), itemType);

      Map<Object, List<List>> map =
          ConversionUtils.listBeanToMapListListBean(consValues, "section_title", "field_id");
      ftlParamMap.put("PhysicianForms", map);

      ftlParamMap.put("consultation_components", compBean.getMap());
      ftlParamMap.put("consult_phy_forms",
          SectionsDAO.getAddedSectionMasterDetails((String) consultBean.get("mr_no"),
              (String) consultBean.get("patient_id"), consultId, 0,
              (Integer) compBean.get("form_id"), itemType));
      consultationsList.add(ftlParamMap);
    }

    PrintTemplatesDAO printtemplatedao = new PrintTemplatesDAO();
    String templateContent =
        printtemplatedao.getCustomizedTemplate(PrintTemplate.ConsultationDetails);
    FtlReportGenerator ftlGen = null;

    if (templateContent == null || templateContent.equals("")) {
      ftlGen = new FtlReportGenerator(PrintTemplate.ConsultationDetails.getFtlName());
    } else {
      StringReader reader = new StringReader(templateContent);
      ftlGen = new FtlReportGenerator("ConsultationDetailsPrintTemplate", reader);
    }
    VitalMasterDAO vmDAO = new VitalMasterDAO();
    Map templateParams = new HashMap();
    templateParams.put("consultations", consultationsList);
    templateParams.put("diagnosis_details", MRDDiagnosisDAO.getAllDiagnosisDetails(patientId));
    templateParams.put("visitdetails", patDetails);
    templateParams.put("vital_params", vmDAO.getActiveVitalParams("O"));

    StringWriter writer = new StringWriter();
    try {
      ftlGen.setReportParams(templateParams);
      ftlGen.process(writer);
    } catch (TemplateException te) {
      log.error("", te);
      throw te;
    }
    return writer.toString();
  }

  /**
   * Gets the OT details.
   *
   * @param patientId the patient id
   * @return the OT details
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws TemplateException the template exception
   * @throws Exception the exception
   */
  public String getOTDetails(String patientId)
      throws SQLException, IOException, TemplateException, Exception {

    List<BasicDynaBean> operationDetailsIds = OperationDetailsDAO.getPatinetOperations(patientId);
    List<Map> operationsList = new ArrayList<Map>();
    Map patDetails = new HashMap();
    GenericDocumentsFields.copyPatientDetails(patDetails, null, patientId, false);

    for (BasicDynaBean bean : operationDetailsIds) {
      Integer opDetailsId = (Integer) bean.get("operation_details_id");

      Map ftlParamMap = new HashMap();
      ftlParamMap.put("surgery_details", OperationDetailsDAO.getSurgeryDetailsForFTL(opDetailsId));
      ftlParamMap.put("operation_team_details", OperationDetailsDAO.getOperationTeam(opDetailsId));
      ftlParamMap.put("operation_anaethesia_details",
          OperationDetailsDAO.getOperationAnaesthesiaDetails(opDetailsId));
      ftlParamMap.put("opeartionsList", OperationDetailsDAO.getSurgeryListForFTL(opDetailsId));

      List<BasicDynaBean> operationsProcIdsList = otDAO.getOperations(patientId, opDetailsId);
      List opCompDetails = new ArrayList<BasicDynaBean>();
      Map operationWiseCompValues = new HashMap<String, Map<Object, List<List>>>();
      Map<Integer, Integer> sysCompDetails = new HashMap<Integer, Integer>();
      Map<String, String> operationNames = new HashMap<String, String>();

      BasicDynaBean components = null;
      AbstractInstaForms formDAO = new OTForms();
      PatientSectionDetailsDAO psdDAO = new PatientSectionDetailsDAO();
      String itemType = (String) formDAO.getKeys().get("item_type");
      for (BasicDynaBean operationIDS : operationsProcIdsList) {
        int operationProcId = (Integer) operationIDS.get("operation_proc_id");

        Map params = new HashMap();
        params.put("operation_proc_id", new String[] {operationProcId + ""});
        components = formDAO.getComponents(params);

        opCompDetails.add(components);
        String formsIds = (String) components.get("sections");
        String[] formsIdsArray = formsIds.split(",");
        for (int i = 0; i < formsIdsArray.length; i++) {
          int formId = Integer.parseInt(formsIdsArray[i]);
          if (formId < 0) {
            sysCompDetails.put(formId, formId);
          }
        }
        List<BasicDynaBean> sectionValues = psdDAO.getAllSectionDetails(
            (String) patDetails.get("mr_no"), (String) patDetails.get("patient_id"),
            operationProcId, 0, (Integer) components.get("form_id"), itemType);

        Map<Object, List<List>> map =
            ConversionUtils.listBeanToMapListListBean(sectionValues, "section_title", "field_id");
        operationWiseCompValues.put(operationProcId + "", map);
        String operationName = (String) operationIDS.get("operation_name");
        operationNames.put((new Integer(operationProcId)).toString(), operationName);
      }

      ftlParamMap.put("ot_record_components", opCompDetails);
      ftlParamMap.put("opCompSectionValues", operationWiseCompValues);
      ftlParamMap.put("system_components", sysCompDetails);
      ftlParamMap.put("operationNames", operationNames);
      operationsList.add(ftlParamMap);
    }

    PrintTemplatesDAO printtemplatedao = new PrintTemplatesDAO();
    String templateContent = printtemplatedao.getCustomizedTemplate(PrintTemplate.OTDetails);
    FtlReportGenerator ftlGen = null;

    if (templateContent == null || templateContent.equals("")) {
      ftlGen = new FtlReportGenerator(PrintTemplate.OTDetails.getFtlName());
    } else {
      StringReader reader = new StringReader(templateContent);
      ftlGen = new FtlReportGenerator("OTDetailsPrintTemplate", reader);
    }
    Map templateParams = new HashMap();
    templateParams.put("operations", operationsList);
    templateParams.put("diagnosis_details", MRDDiagnosisDAO.getAllDiagnosisDetails(patientId));
    templateParams.put("secondary_complaints", scomplaintDao.getSecondaryComplaints(patientId));
    templateParams.put("visitdetails", patDetails);
    StringWriter writer = new StringWriter();
    try {
      ftlGen.setReportParams(templateParams);
      ftlGen.process(writer);
    } catch (TemplateException te) {
      log.error("", te);
      throw te;
    }
    return writer.toString();
  }
}
