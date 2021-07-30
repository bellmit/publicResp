package com.insta.hms.outpatient;

import com.bob.hms.common.APIUtility;
import com.bob.hms.common.Preferences;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DynaBeanBuilder;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.ftl.FtlReportGenerator;
import com.insta.hms.core.clinical.diagnosisdetails.DiagnosisDetailsService;
import com.insta.hms.core.patient.followupdetails.FollowUpService;
import com.insta.hms.erxprescription.ERxPrescriptionDAO;
import com.insta.hms.genericdocuments.GenericDocumentsFields;
import com.insta.hms.imageretriever.DoctorConsultImageRetriever;
import com.insta.hms.instaforms.AbstractInstaForms;
import com.insta.hms.instaforms.PatientSectionDetailsDAO;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.PatientHeaderTemplate.PatientHeaderTemplateDAO;
import com.insta.hms.master.PrescriptionsLabelPrintTemplates.PrescriptionsLabelPrintTemplateDAO;
import com.insta.hms.master.PrescriptionsMaster.PrescriptionsMasterDAO;
import com.insta.hms.master.PrescriptionsPrintTemplates.PrescriptionsTemplateDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.master.PrintTemplates.PrintTemplate;
import com.insta.hms.master.PrintTemplates.PrintTemplatesDAO;
import com.insta.hms.master.Sections.SectionsDAO;
import com.insta.hms.medicalrecorddepartment.MRDDiagnosisDAO;
import com.insta.hms.stores.MedicineSalesDAO;
import com.insta.hms.usermanager.UserDAO;
import com.insta.hms.vitalForm.genericVitalFormDAO;
import com.insta.hms.vitalparameter.VitalMasterDAO;
import com.lowagie.text.DocumentException;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

/**
 * The Class OPPrescriptionFtlHelper.
 *
 * @author krishna.t
 */
public class OPPrescriptionFtlHelper {

  /** The log. */
  static Logger log = LoggerFactory.getLogger(OPPrescriptionFtlHelper.class);

  /** The vm DAO. */
  VitalMasterDAO vmDAO = new VitalMasterDAO();

  /** The ph template dao. */
  private PatientHeaderTemplateDAO phTemplateDao = new PatientHeaderTemplateDAO();

  /** The physician form values DAO. */
  PhysicianFormValuesDAO physicianFormValuesDAO = new PhysicianFormValuesDAO();

  /** The follow up dao. */
  private GenericDAO followUpDao = new GenericDAO("follow_up_details");

  /** The consult image dao. */
  DoctorConsultImagesDAO consultImageDao = new DoctorConsultImagesDAO();

  /** The erxdao. */
  ERxPrescriptionDAO erxdao = new ERxPrescriptionDAO();

  /** The secondary complaint dao. */
  SecondaryComplaintDAO secondaryComplaintDao = new SecondaryComplaintDAO();

  /** The psd DAO. */
  PatientSectionDetailsDAO psdDAO = new PatientSectionDetailsDAO();

  /** The gpdao. */
  GenericPreferencesDAO gpdao = new GenericPreferencesDAO();

  /** The follow up service. */
  FollowUpService followUpService = (FollowUpService) ApplicationContextProvider
      .getApplicationContext().getBean("followUpService");

  /** The diag details service. */
  DiagnosisDetailsService diagDetailsService = ApplicationContextProvider
      .getBean(DiagnosisDetailsService.class);

  /** The cfg. */
  private Configuration cfg = null;

  /**
   * Instantiates a new OP prescription ftl helper.
   */
  public OPPrescriptionFtlHelper() {
    cfg = AppInit.getFmConfig();
  }

  /**
   * The Enum ReturnType.
   */
  public enum ReturnType {

    /** The pdf. */
    PDF,
    /** The pdf bytes. */
    PDF_BYTES,
    /** The text bytes. */
    TEXT_BYTES,
    /** The html. */
    HTML
  }

  /**
   * The Enum DefaultType.
   */
  public enum DefaultType {

    /** The prescription. */
    PRESCRIPTION,
    /** The consultation. */
    CONSULTATION,
    /** The emr. */
    EMR
  }

  /**
   * Instantiates a new OP prescription ftl helper.
   *
   * @param cfg the cfg
   */
  public OPPrescriptionFtlHelper(Configuration cfg) {
    this.cfg = cfg;
  }

  /**
   * Gets the consultation pdf bytes.
   *
   * @param docIdStr  the doc id str
   * @param printerId the printer id
   * @return the consultation pdf bytes
   * @throws SQLException             the SQL exception
   * @throws IOException              Signals that an I/O exception has occurred.
   * @throws DocumentException        the document exception
   * @throws XPathExpressionException the x path expression exception
   * @throws TransformerException     the transformer exception
   * @throws ParseException           the parse exception
   */
  public byte[] getConsultationPdfBytes(String docIdStr, int printerId)
      throws SQLException, IOException, DocumentException, XPathExpressionException,
      TransformerException, ParseException {
    return getConsultationPdfBytes(docIdStr, true, printerId);
  }

  /**
   * Gets the consultation pdf bytes.
   *
   * @param docIdStr  the doc id str
   * @param allFields the all fields
   * @param printerId the printer id
   * @return the consultation pdf bytes
   * @throws SQLException             the SQL exception
   * @throws IOException              Signals that an I/O exception has occurred.
   * @throws DocumentException        the document exception
   * @throws XPathExpressionException the x path expression exception
   * @throws TransformerException     the transformer exception
   * @throws ParseException           the parse exception
   */
  public byte[] getConsultationPdfBytes(String docIdStr, boolean allFields, int printerId)
      throws SQLException, IOException, DocumentException, XPathExpressionException,
      TransformerException, ParseException {

    if (docIdStr == null || docIdStr.equals("")) {
      throw new IllegalArgumentException("docIdStr is null");
    }
    int consultationId = 0;
    try {
      consultationId = Integer.parseInt(docIdStr);
    } catch (NumberFormatException nfe) {
      throw new IllegalArgumentException("Prescription not found for : " + docIdStr);
    }
    try {
      BasicDynaBean prefs = PrintConfigurationsDAO
          .getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT, printerId);
      return getConsultationFtlReport(consultationId, null, ReturnType.PDF_BYTES, prefs, allFields,
          null, null, DefaultType.CONSULTATION);
    } catch (TemplateException te) {
      throw new IllegalArgumentException(te);
    }
  }

  /**
   * Gets the consultation ftl report.
   *
   * @param consultId    the consult id
   * @param templateName the template name
   * @param enumType     the enum type
   * @param prefs        the prefs
   * @param allFields    the all fields
   * @param os           the os
   * @param userName     the user name
   * @param templateType the template type
   * @return the consultation ftl report
   * @throws SQLException             the SQL exception
   * @throws DocumentException        the document exception
   * @throws TemplateException        the template exception
   * @throws IOException              Signals that an I/O exception has occurred.
   * @throws XPathExpressionException the x path expression exception
   * @throws TransformerException     the transformer exception
   */
  @SuppressWarnings("rawtypes")
  public byte[] getConsultationFtlReport(int consultId, String templateName, ReturnType enumType,
      BasicDynaBean prefs, boolean allFields, OutputStream os, String userName,
      DefaultType templateType) throws SQLException, DocumentException, TemplateException,
      IOException, XPathExpressionException, TransformerException {

    byte[] bytes = null;
    CenterMasterDAO centerDao = new CenterMasterDAO();
    UserDAO userdao = new UserDAO();
    BasicDynaBean consultBean = DoctorConsultationDAO.getConsultDetails(consultId);
    Map patientDetails = new HashMap();
    GenericDocumentsFields.copyPatientDetails(patientDetails, null,
        (String) consultBean.get("patient_id"), false);
    Map<String, Object> ftlParamMap = new HashMap();
    ftlParamMap.put("visitdetails", patientDetails);
    Integer centerId = RequestContext.getCenterId();

    Map modulesActivatedMap = APIUtility.getPreferences().getModulesActivatedMap();
    BasicDynaBean genericPrefs = gpdao.getAllPrefs();
    String useStoreItems = (String) genericPrefs.get("prescription_uses_stores");
    List<BasicDynaBean> allPrescriptions = PrescriptionsMasterDAO.getAllPrescriptions(consultId,
        (String) consultBean.get("patient_id"), useStoreItems, null);
    List tests = PrescriptionsMasterDAO.getPrescribedItems(allPrescriptions, "Inv.");
    List packageComponents = Collections.emptyList();
    List erxActivities = Collections.emptyList();

    ftlParamMap.put("modules_activated", modulesActivatedMap);
    ftlParamMap.put("presMedicines",
        PrescriptionsMasterDAO.getPrescribedItems(allPrescriptions, "Medicine"));
    ftlParamMap.put("presServices",
        PrescriptionsMasterDAO.getPrescribedItems(allPrescriptions, "Service"));
    ftlParamMap.put("presTests", tests);
    ftlParamMap.put("presConsultation",
        PrescriptionsMasterDAO.getPrescribedItems(allPrescriptions, "Doctor"));
    ftlParamMap.put("presInstructions",
        PrescriptionsMasterDAO.getPrescribedItems(allPrescriptions, "Instructions"));
    ftlParamMap.put("NonHospitalItems",
        PrescriptionsMasterDAO.getPrescribedItems(allPrescriptions, "NonHospital"));
    ftlParamMap.put("presOperations",
        PrescriptionsMasterDAO.getPrescribedItems(allPrescriptions, "Operation"));
    ftlParamMap.put("vitals", Collections.emptyList()); // added for legacy support.
    ftlParamMap.put("standing_instructions", Collections.emptyList());
    // is added for legacy support.(need to remove once after 7.0 first release)
    ftlParamMap.put("printDoctorNotes", true);

    if (!tests.isEmpty()) {
      packageComponents = PrescriptionsMasterDAO.getAllPackageComponents(consultId);
    }

    int pbmPrescId = erxdao.getErxConsPBMId(consultId);

    BasicDynaBean erxConsBean = null;
    if (pbmPrescId > 0) {
      // Erx cons bean.
      erxConsBean = erxdao.getConsErxDetails(pbmPrescId);
      // Erx Activity Ids
      erxActivities = erxdao.getErxPrescribedActivities(pbmPrescId,
          centerDao.getHealthAuthorityForCenter(centerId));
    }

    List<BasicDynaBean> followupBean = followUpService
        .getfollowUpDetails((String) consultBean.get("patient_id"));
    if (followupBean != null && followupBean.size() > 0) {
      ftlParamMap.put("followup_date", followupBean.get(0).get("followup_date"));
      ftlParamMap.put("followupDetails", followupBean);
    }

    ftlParamMap.put("secondary_complaints",
        secondaryComplaintDao.getSecondaryComplaints((String) consultBean.get("patient_id")));
    ftlParamMap.put("erx_cons_bean", erxConsBean);
    ftlParamMap.put("erxActivities", erxActivities);
    ftlParamMap.put("packageComponents", packageComponents);
    ftlParamMap.put("consultation_bean", consultBean);
    ftlParamMap.put("userName", userName);
    String visitType = (String) patientDetails.get("op_type");
    String formType = null;
    if (visitType.equals("F")) {
      formType = "Form_OP_FOLLOW_UP_CONS";
    } else {
      formType = "Form_CONS";
    }
    AbstractInstaForms formDAO = AbstractInstaForms.getInstance(formType);
    String itemType = (String) formDAO.getKeys().get("item_type");
    Map params = new HashMap();
    params.put("consultation_id", new String[] { consultId + "" });
    params.put("form_type", formType);
    BasicDynaBean compBean = formDAO.getComponents(params);

    List<BasicDynaBean> consValues = psdDAO.getAllSectionDetails((String) consultBean.get("mr_no"),
        (String) consultBean.get("patient_id"), consultId, 0, (Integer) compBean.get("form_id"),
        itemType);
    Map<Object, List<List>> map = ConversionUtils.listBeanToMapListListBean(consValues,
        "str_section_detail_id", "field_id");
    ftlParamMap.put("PhysicianForms", map);
    ftlParamMap.put("diagnosis_details",
        MRDDiagnosisDAO.getAllDiagnosisDetails((String) consultBean.get("patient_id")));
    ftlParamMap.put("coder_diagnosis_details",
        diagDetailsService.getAllCoderDiagnosisDetails((String) consultBean.get("patient_id")));
    String compAllergies = "N";
    String compVitals = "N";
    String consultationNotes = "N";
    String compComplaint = "N";
    String noteTakerEnabled = "N";

    List allergies = Collections.emptyList();
    List pacDetails = Collections.emptyList();
    List healthMaintenance = Collections.emptyList();
    List pregnancyhistories = Collections.emptyList();
    List pregnancyhistoriesBean = Collections.emptyList();
    List antenatalinfo = Collections.emptyList(); // Legacy support for customize template
    List consultationFields = Collections.emptyList();
    Map consultationFieldsMap = null;
    List imagesList = Collections.emptyList(); // note taker images.
    List vitalParams = Collections.emptyList();
    List vitalReadings = Collections.emptyList();

    Map<String, Object> consultCompMap = new HashMap();
    Set antenatalKeyCounts = null;
    Map antenatalinfoMap = null;
    for (String comp : ((String) compBean.get("sections")).split(",")) {
      Integer sectionid = Integer.parseInt(comp);
      if (sectionid == -1) {
        compComplaint = "Y";
        consultCompMap.put("complaint", compComplaint);

      } else if (sectionid == -2) {
        compAllergies = "Y";
        consultCompMap.put("allergies", compAllergies);
        allergies = AllergiesDAO.getAllActiveAllergies((String) consultBean.get("mr_no"),
            (String) consultBean.get("patient_id"), consultId, 0, (Integer) compBean.get("form_id"),
            itemType);

      } else if (sectionid == -4) {
        compVitals = "Y";
        consultCompMap.put("vitals", compVitals);

        vitalParams = vmDAO.getActiveVitalParams("O");
        vitalReadings = genericVitalFormDAO.groupByReadingId((String) consultBean.get("patient_id"),
            "V");

      } else if (sectionid == -5) {
        consultationNotes = "Y";
        consultCompMap.put("consultation_notes", consultationNotes);

        consultationFields = ConsultationFieldValuesDAO
            .getConsultationFieldsValuesForPrint(consultId, allFields, true);
        consultationFieldsMap = ConversionUtils.listBeanToMapBean(consultationFields, "field_name");

        BasicDynaBean doctorNotesBean = (BasicDynaBean) consultationFieldsMap.get("Doctor Notes");
        BasicDynaBean diagnosisBean = (BasicDynaBean) consultationFieldsMap.get("Diagnosis");
        BasicDynaBean remarksBean = (BasicDynaBean) consultationFieldsMap.get("Other Instructions");
        BasicDynaBean descriptionBean = (BasicDynaBean) consultationFieldsMap.get("Description");

        consultBean.set("doctor_notes",
            doctorNotesBean == null ? "" : doctorNotesBean.get("field_value"));
        consultBean.set("diagnosis", diagnosisBean == null ? "" : diagnosisBean.get("field_value"));
        consultBean.set("remarks", remarksBean == null ? "" : remarksBean.get("field_value"));
        consultBean.set("description",
            descriptionBean == null ? "" : descriptionBean.get("field_value"));

        noteTakerEnabled = userdao.getPrescriptionNoteTaker(userName);
        List<String> imageColumnList = new ArrayList();
        imageColumnList.add("image_id");
        imageColumnList.add("datetime");
        imagesList = consultImageDao.listAll(imageColumnList, "consultation_id", consultId);

      } else if (sectionid == -16) {
        pacDetails = PreAnaesthestheticDAO.getAllPACRecords((String) consultBean.get("mr_no"),
            (String) consultBean.get("patient_id"), consultId, 0, (Integer) compBean.get("form_id"),
            itemType);

      } else if (sectionid == -15) {
        healthMaintenance = HealthMaintenanceDAO.getAllHealthMaintenance(
            (String) consultBean.get("mr_no"), (String) consultBean.get("patient_id"), consultId, 0,
            (Integer) compBean.get("form_id"), itemType);

      } else if (sectionid == -13) {
        pregnancyhistories = PregnancyHistoryDAO.getAllPregnancyDetails(
            (String) consultBean.get("mr_no"), (String) consultBean.get("patient_id"), consultId, 0,
            (Integer) compBean.get("form_id"), itemType);
        pregnancyhistoriesBean = ObstetricRecordDAO.getAllObstetricHeadDetails(
            (String) consultBean.get("mr_no"), (String) consultBean.get("patient_id"), consultId, 0,
            (Integer) compBean.get("form_id"), itemType);

      } else if (sectionid == -14) {
        antenatalinfo = AntenatalDAO.getAllAntenatalDetails((String) consultBean.get("mr_no"),
            (String) consultBean.get("patient_id"), consultId, 0, (Integer) compBean.get("form_id"),
            itemType);
        antenatalinfoMap = ConversionUtils.listBeanToMapListBean(antenatalinfo,
            "pregnancy_count_key");
        antenatalKeyCounts = antenatalinfoMap.keySet();
      }

    }
    consultCompMap.putAll(compBean.getMap());

    ftlParamMap.put("allergies", allergies);
    ftlParamMap.put("consultationFieldsMap", consultationFieldsMap);
    ftlParamMap.put("consultationFields", consultationFields);
    ftlParamMap.put("consultImages", imagesList);
    ftlParamMap.put("noteTakerEnabled", noteTakerEnabled);
    ftlParamMap.put("pac_details", pacDetails);
    ftlParamMap.put("health_maintenance", healthMaintenance);
    ftlParamMap.put("pregnancyhistories", pregnancyhistories);
    ftlParamMap.put("pregnancyhistoriesBean", pregnancyhistoriesBean);
    ftlParamMap.put("antenatalinfo", antenatalinfo);
    ftlParamMap.put("antenatalKeyCounts", antenatalKeyCounts);
    ftlParamMap.put("antenatalinfoMap", antenatalinfoMap);
    ftlParamMap.put("vital_params", vitalParams);
    ftlParamMap.put("vital_values", vitalReadings);

    ftlParamMap.put("consultation_components", consultCompMap);
    ftlParamMap.put("insta_sections",
        SectionsDAO.getAddedSectionMasterDetails((String) consultBean.get("mr_no"),
            (String) consultBean.get("patient_id"), consultId, 0, (Integer) compBean.get("form_id"),
            itemType));
    String templateContent = null;
    String templateMode = null;
    if (templateName == null || templateName.equals("")) {
      if (templateType.equals(DefaultType.PRESCRIPTION)) {
        templateName = (String) genericPrefs.get("default_prescription_print_template");
      } else if (templateType.equals(DefaultType.CONSULTATION)) {
        templateName = (String) genericPrefs.get("default_consultation_print_template");
      } else {
        templateName = (String) genericPrefs.get("default_emr_print_template");
      }
    }

    boolean isClosed = consultBean.get("status").equals("C");
    FtlReportGenerator ftlGen = null;
    if (templateName.equals("BUILTIN_HTML")) {
      ftlGen = new FtlReportGenerator("ConsultationPrint");
      templateMode = "H";
    } else if (templateName.equals("BUILTIN_TEXT")) {
      ftlGen = new FtlReportGenerator("ConsultationTextPrint");
      templateMode = "T";
    } else {
      BasicDynaBean pbean = PrescriptionsTemplateDAO.getTemplateContent(templateName);

      if (pbean == null) {
        return null;
      }
      templateContent = (String) pbean.get("prescription_template_content");
      templateMode = (String) pbean.get("template_mode");
      StringReader reader = new StringReader(templateContent);
      ftlGen = new FtlReportGenerator("ConsultationSheetPrint.ftl", reader);
    }
    StringWriter writer = new StringWriter();
    ftlGen.setReportParams(ftlParamMap);
    ftlGen.process(writer);

    HtmlConverter hc = new HtmlConverter(new DoctorConsultImageRetriever());
    Boolean repeatPHeader = ((String) prefs.get("repeat_patient_info")).equals("Y");
    if (enumType.equals(ReturnType.PDF)) {
      if (templateMode != null && templateMode.equals("T")) {
        hc.textToPDF(writer.toString(), os, prefs);
      } else {
        hc.writePdf(os, writer.toString(), "OP Prescription", prefs, false, repeatPHeader, true,
            true, isClosed, false);
      }
      os.close();

    } else if (enumType.equals(ReturnType.PDF_BYTES)) {
      ByteArrayOutputStream stream = new ByteArrayOutputStream();
      if (templateMode != null && templateMode.equals("T")) {
        hc.textToPDF(writer.toString(), stream, prefs);
      } else {
        hc.writePdf(stream, writer.toString(), "OP Prescription", prefs, false, repeatPHeader, true,
            true, isClosed, false);
      }
      bytes = stream.toByteArray();
      stream.close();

    } else if (enumType.equals(ReturnType.TEXT_BYTES)) {
      if (templateMode != null && templateMode.equals("T")) {
        bytes = writer.toString().getBytes();
      } else {
        bytes = hc.getText(writer.toString(), "OP Prescription", prefs, true, true);
      }
    }
    return bytes;
  }

  /**
   * Gets the prescription label ftl report.
   *
   * @param saleId       the sale id
   * @param templateName the template name
   * @param enumType     the enum type
   * @param prefs        the prefs
   * @param os           the os
   * @param printType    the print type
   * @param userName     the user name
   * @return the prescription label ftl report
   * @throws SQLException             the SQL exception
   * @throws DocumentException        the document exception
   * @throws TemplateException        the template exception
   * @throws IOException              Signals that an I/O exception has occurred.
   * @throws XPathExpressionException the x path expression exception
   * @throws TransformerException     the transformer exception
   */
  public byte[] getPrescriptionLabelFtlReport(String saleId, String templateName,
      ReturnType enumType, BasicDynaBean prefs, OutputStream os, String printType, String userName)
      throws SQLException, DocumentException, TemplateException, IOException,
      XPathExpressionException, TransformerException {

    byte[] bytes = null;
    Map ftlParamMap = new HashMap();

    Map modulesActivatedMap = ((Preferences) RequestContext.getSession()
        .getAttribute("preferences")).getModulesActivatedMap();
    BasicDynaBean genericPrefs = gpdao.getAllPrefs();

    List<BasicDynaBean> saleDetails = MedicineSalesDAO.getPrescList(saleId);

    for (BasicDynaBean pbean : saleDetails) {
      Object saleUnit = pbean.get("sale_unit");
      int lblCount = (Integer) pbean.get("lblcount");

      if (saleUnit.equals("P")) {

        int issueBase = ((BigDecimal) pbean.get("issue_base_unit")).intValue();
        int soldQty = ((BigDecimal) pbean.get("quantity")).intValue();
        lblCount = soldQty % issueBase == 0 ? soldQty / issueBase : (soldQty / issueBase) + 1;

      } else {
        lblCount = 1;
      }
      pbean.set("lblcount", lblCount);
    }

    ftlParamMap.put("modules_activated", modulesActivatedMap);
    ftlParamMap.put("userName", userName);
    ftlParamMap.put("items", saleDetails);

    PrintTemplatesDAO printtemplatedao = new PrintTemplatesDAO();
    Template temp = null;
    String templateContent = null;
    String templateMode = null;

    if (templateName == null || templateName.equals("") || templateName.equals("BUILTIN_HTML")) {
      temp = AppInit.getFmConfig().getTemplate("PrescriptionLabelTemplate.ftl");
      templateMode = "H";
    } else if (templateName.equals("BUILTIN_TEXT")) {
      temp = AppInit.getFmConfig().getTemplate("PrescriptionLabelPrintTextTemplate.ftl");
      templateMode = "T";
    } else {
      BasicDynaBean pbean = PrescriptionsLabelPrintTemplateDAO.getTemplateContent(templateName);

      if (pbean == null) {
        return null;
      }
      templateContent = (String) pbean.get("prescription_lbl_template_content");
      templateMode = (String) pbean.get("template_mode");
      StringReader reader = new StringReader(templateContent);
      temp = new Template("PrescriptionLabelPrintTextTemplate.ftl", reader, AppInit.getFmConfig());
    }

    StringWriter writer = new StringWriter();
    temp.process(ftlParamMap, writer);
    HtmlConverter hc = new HtmlConverter(new DoctorConsultImageRetriever());
    Boolean repeatPHeader = ((String) prefs.get("repeat_patient_info")).equals("Y");
    if (enumType.equals(ReturnType.PDF)) {
      if (templateMode != null && templateMode.equals("T")) {
        hc.textToPDF(writer.toString(), os, prefs);
      } else {
        hc.writePdf(os, writer.toString(), "Prescription Label", prefs, false, repeatPHeader, true,
            true, true, false);
      }
      os.close();

    } else if (enumType.equals(ReturnType.PDF_BYTES)) {
      ByteArrayOutputStream stream = new ByteArrayOutputStream();
      if (templateMode != null && templateMode.equals("T")) {
        hc.textToPDF(writer.toString(), stream, prefs);
      } else {
        hc.writePdf(stream, writer.toString(), "Prescription Label", prefs, false, repeatPHeader,
            true, true, true, false);
      }
      bytes = stream.toByteArray();
      stream.close();

    } else if (enumType.equals(ReturnType.TEXT_BYTES)) {
      if (templateMode != null && templateMode.equals("T")) {
        bytes = writer.toString().getBytes();
      } else {
        bytes = hc.getText(writer.toString(), "Prescription Label", prefs, true, true);
      }
    }
    return bytes;
  }

  /**
   * Gets the triage and clinical info ftl report.
   *
   * @param consultId the consult id
   * @param enumType  the enum type
   * @param prefs     the prefs
   * @param os        the os
   * @param userName  the user name
   * @param isTriage  the is triage
   * @return the triage and clinical info ftl report
   * @throws SQLException             the SQL exception
   * @throws DocumentException        the document exception
   * @throws TemplateException        the template exception
   * @throws IOException              Signals that an I/O exception has occurred.
   * @throws XPathExpressionException the x path expression exception
   * @throws TransformerException     the transformer exception
   * @throws ParseException           the parse exception
   */
  public byte[] getTriageAndClinicalInfoFtlReport(int consultId, ReturnType enumType,
      BasicDynaBean prefs, OutputStream os, String userName, boolean isTriage)
      throws SQLException, DocumentException, TemplateException, IOException,
      XPathExpressionException, TransformerException, ParseException {
    return getTriageAndClinicalInfoFtlReport(consultId, enumType, prefs, os, userName, isTriage,
        null);
  }

  /**
   * Gets the triage and clinical info ftl report.
   *
   * @param consultId     the consult id
   * @param enumType      the enum type
   * @param prefs         the prefs
   * @param os            the os
   * @param userName      the user name
   * @param isTriage      the is triage
   * @param patientDetMap the patient det map
   * @return the triage and clinical info ftl report
   * @throws SQLException             the SQL exception
   * @throws DocumentException        the document exception
   * @throws TemplateException        the template exception
   * @throws IOException              Signals that an I/O exception has occurred.
   * @throws XPathExpressionException the x path expression exception
   * @throws TransformerException     the transformer exception
   * @throws ParseException           the parse exception
   */
  public byte[] getTriageAndClinicalInfoFtlReport(int consultId, ReturnType enumType,
      BasicDynaBean prefs, OutputStream os, String userName, boolean isTriage, Map patientDetMap)
      throws SQLException, DocumentException, TemplateException, IOException,
      XPathExpressionException, TransformerException, ParseException {

    byte[] bytes = null;

    BasicDynaBean consultBean = DoctorConsultationDAO.getConsultDetails(consultId);
    Map patientDetails = new HashMap();
    if (patientDetMap == null || patientDetMap.isEmpty()) {
      GenericDocumentsFields.copyPatientDetails(patientDetails, null,
          (String) consultBean.get("patient_id"), false);
    } else {
      GenericDocumentsFields.convertAndCopy(patientDetMap, patientDetails, false);
    }

    Map ftlParamMap = new HashMap();
    ftlParamMap.put("visitdetails", patientDetails);

    String visitType = (String) patientDetails.get("op_type");
    String formType = null;
    if (visitType.equals("F")) {
      formType = "Form_OP_FOLLOW_UP_CONS";
    } else {
      formType = "Form_CONS";
    }
    AbstractInstaForms triageFormDAO = AbstractInstaForms.getInstance("Form_TRI");

    Map params = new HashMap();
    params.put("consultation_id", new String[] { consultId + "" });
    BasicDynaBean triageCompBean = triageFormDAO.getComponents(params);

    String triageItemType = (String) triageFormDAO.getKeys().get("item_type");
    ftlParamMap.put("diagnosis_details",
        MRDDiagnosisDAO.getAllDiagnosisDetails((String) consultBean.get("patient_id")));
    ftlParamMap.put("allergies",
        AllergiesDAO.getAllActiveAllergies((String) consultBean.get("mr_no"),
            (String) consultBean.get("patient_id"), consultId, 0,
            (Integer) triageCompBean.get("form_id"), triageItemType));
    ftlParamMap.put("pregnancyhistories",
        PregnancyHistoryDAO.getAllPregnancyDetails((String) consultBean.get("mr_no"),
            (String) consultBean.get("patient_id"), consultId, 0,
            (Integer) triageCompBean.get("form_id"), triageItemType));
    ftlParamMap.put("pregnancyhistoriesBean",
        ObstetricRecordDAO.getAllObstetricHeadDetails((String) consultBean.get("mr_no"),
            (String) consultBean.get("patient_id"), consultId, 0,
            (Integer) triageCompBean.get("form_id"), triageItemType));

    Set antenatalKeyCounts = null;
    Map antenatalinfoMap = null;
    List antenatalinfo = AntenatalDAO.getAllAntenatalDetails((String) consultBean.get("mr_no"),
        (String) consultBean.get("patient_id"), consultId, 0,
        (Integer) triageCompBean.get("form_id"), triageItemType);
    antenatalinfoMap = ConversionUtils.listBeanToMapListBean(antenatalinfo, "pregnancy_count_key");
    antenatalKeyCounts = antenatalinfoMap.keySet();
    ftlParamMap.put("antenatalinfo", antenatalinfo);
    ftlParamMap.put("antenatalKeyCounts", antenatalKeyCounts);
    ftlParamMap.put("antenatalinfoMap", antenatalinfoMap);

    String compAllergies = "N";
    String compVitals = "N";
    String compImmunization = "N";
    Map triageCompMap = triageCompBean.getMap();
    for (String comp : ((String) triageCompBean.get("sections")).split(",")) {
      Integer formid = Integer.parseInt(comp);
      if (formid == -2) {
        compAllergies = "Y";
        triageCompMap.put("allergies", compAllergies);
      }
      if (formid == -4) {
        compVitals = "Y";
        triageCompMap.put("vitals", compVitals);
      }
      if (formid == -17) {
        compImmunization = "Y";
        triageCompMap.put("immunization", compImmunization);
      }
    }
    ftlParamMap.put("triage_components", triageCompMap);
    params.put("form_type", formType);

    BasicDynaBean genericPrefs = gpdao.getAllPrefs();
    String useStoreItems = (String) genericPrefs.get("prescription_uses_stores");
    List allPrescriptions = PrescriptionsMasterDAO.getAllPrescriptions(consultId,
        (String) consultBean.get("patient_id"), useStoreItems, null);

    List<BasicDynaBean> consValues = psdDAO.getAllSectionDetails((String) consultBean.get("mr_no"),
        (String) consultBean.get("patient_id"), consultId, 0,
        (Integer) triageCompBean.get("form_id"), triageItemType);
    Map<Object, List<List>> map = ConversionUtils.listBeanToMapListListBean(consValues,
        "str_section_detail_id", "field_id");
    ftlParamMap.put("HPI", null);
    ftlParamMap.put("ROS", null);
    ftlParamMap.put("PE", null);
    ftlParamMap.put("PhysicianForms", map);

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

    ftlParamMap.put("vital_params", vmDAO.getActiveVitalParams("O"));
    ftlParamMap.put("vitals",
        genericVitalFormDAO.groupByReadingId((String) consultBean.get("patient_id"), "V"));
    List consultationFields = ConsultationFieldValuesDAO
        .getConsultationFieldsValuesForPrint(consultId, true, true);
    ftlParamMap.put("consultationFields", consultationFields);
    String deptId = (String) patientDetails.get("dept_id");

    compAllergies = "N";
    compVitals = "N";

    String consultationNotes = "N";
    String compComplaint = "N";
    Map consultCompMap = new HashMap();
    AbstractInstaForms consFormDAO = AbstractInstaForms.getInstance(formType);
    BasicDynaBean consCompBean = consFormDAO.getComponents(params);
    for (String comp : ((String) consCompBean.get("sections")).split(",")) {
      Integer formid = Integer.parseInt(comp);
      if (formid == -1) {
        compComplaint = "Y";
        consultCompMap.put("complaint", compComplaint);
      }
      if (formid == -2) {
        compAllergies = "Y";
        consultCompMap.put("allergies", compAllergies);
      }
      if (formid == -4) {
        compVitals = "Y";
        consultCompMap.put("vitals", compVitals);
      }
      if (formid == -5) {
        consultationNotes = "Y";
        consultCompMap.put("consultation_notes", consultationNotes);
      }

    }
    consultCompMap.putAll(consCompBean.getMap());
    ftlParamMap.put("consultation_components", consultCompMap);

    ftlParamMap.put("triage_insta_sections",
        SectionsDAO.getAddedSectionMasterDetails((String) consultBean.get("mr_no"),
            (String) consultBean.get("patient_id"), consultId, 0,
            (Integer) triageCompBean.get("form_id"), triageItemType));

    String consItemType = (String) consFormDAO.getKeys().get("item_type");
    ftlParamMap.put("insta_sections",
        SectionsDAO.getAddedSectionMasterDetails((String) consultBean.get("mr_no"),
            (String) consultBean.get("patient_id"), consultId, 0,
            (Integer) consCompBean.get("form_id"), consItemType));

    // for legacy support
    DynaBeanBuilder builder = new DynaBeanBuilder();
    builder.add("past_medical_history");
    builder.add("family_history");
    builder.add("social_history");
    BasicDynaBean pfshBean = builder.build();

    Map modulesActivatedMap = APIUtility.getPreferences().getModulesActivatedMap();
    ftlParamMap.put("pfshBean", null);
    ftlParamMap.put("modules_activated", modulesActivatedMap);
    ftlParamMap.put("consultation_bean", consultBean);
    ftlParamMap.put("userName", userName);

    ftlParamMap.put("secondary_complaints",
        secondaryComplaintDao.getSecondaryComplaints((String) consultBean.get("patient_id")));
    PrintTemplatesDAO printtemplatedao = new PrintTemplatesDAO();
    PrintTemplate printTemplate = null;
    if (isTriage) {
      printTemplate = PrintTemplate.Triage;
    } else {
      printTemplate = PrintTemplate.ClinicalInfo;
    }

    String templateContent = printtemplatedao.getCustomizedTemplate(printTemplate);
    Template temp = null;

    if (templateContent == null || templateContent.equals("")) {
      temp = cfg.getTemplate(printTemplate.getFtlName() + ".ftl");
    } else {
      StringReader reader = new StringReader(templateContent);
      temp = new Template("ClinicalInformationPrintTemplate.ftl", reader, AppInit.getFmConfig());
    }

    boolean isClosed = consultBean.get("status").equals("C");
    String patientHeader = phTemplateDao.getPatientHeader(
        printtemplatedao.getPatientHeaderTemplateId(printTemplate), isTriage ? "Triage" : "CI");
    StringReader reader = new StringReader(patientHeader);
    Template pt = new Template("PatientHeader.ftl", reader, AppInit.getFmConfig());
    Map templateMap = new HashMap();
    templateMap.put("visitdetails", patientDetails);
    templateMap.put("modules_activated", modulesActivatedMap);
    StringWriter pwriter = new StringWriter();

    try {
      pt.process(templateMap, pwriter);
    } catch (TemplateException te) {
      log.error("", te);
      throw te;
    }
    StringWriter writer = new StringWriter();
    temp.process(ftlParamMap, writer);

    HtmlConverter hc = new HtmlConverter(new DoctorConsultImageRetriever());
    Boolean repeatPHeader = ((String) prefs.get("repeat_patient_info")).equals("Y");

    StringBuilder documentContent = new StringBuilder();
    documentContent.append(pwriter.toString());
    if (enumType.equals(ReturnType.HTML)) {
      String content = writer.toString();
      // which is required to display the images in the jsp, when we render this content in browser
      content = content.replaceAll("PhysicianFieldsImageMarkers.do\\?_method=view",
          "../master/ImageMarkers/ViewImage.do\\?_method=view");
      content = content.replaceAll("PhysicianFieldsImage.do\\?_method=viewImage",
          "../master/SectionFields/ViewImage.do\\?_method=viewImage");
      documentContent.append(content);
    } else {
      documentContent.append(writer.toString());
    }

    if (enumType.equals(ReturnType.PDF)) {
      hc.writePdf(os, documentContent.toString(), "OP Prescription", prefs, false, repeatPHeader,
          true, true, isClosed, false);
      os.close();

    } else if (enumType.equals(ReturnType.PDF_BYTES)) {
      ByteArrayOutputStream stream = new ByteArrayOutputStream();
      hc.writePdf(stream, documentContent.toString(), "OP Prescription", prefs, false,
          repeatPHeader, true, true, isClosed, false);
      bytes = stream.toByteArray();
      stream.close();

    } else if (enumType.equals(ReturnType.TEXT_BYTES)) {
      bytes = hc.getText(documentContent.toString(), "OP Prescription", prefs, true, true);
    } else if (enumType.equals(ReturnType.HTML)) {
      return documentContent.toString().getBytes();
    }
    return bytes;
  }
}
