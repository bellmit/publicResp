package com.insta.hms.core.prints;

import com.bob.hms.common.APIUtility;
import com.bob.hms.common.Constants;
import com.bob.hms.common.Preferences;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DynaBeanBuilder;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.ftl.FtlReportGenerator;
import com.insta.hms.common.preferences.clinicalpreferences.ClinicalPreferencesService;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.clinical.allergies.AllergiesService;
import com.insta.hms.core.clinical.antenatal.AntenatalService;
import com.insta.hms.core.clinical.consultation.SecondaryComplaintService;
import com.insta.hms.core.clinical.consultation.prescriptions.PrescriptionsService;
import com.insta.hms.core.clinical.diagnosisdetails.DiagnosisDetailsService;
import com.insta.hms.core.clinical.forms.SectionDetailsService;
import com.insta.hms.core.clinical.healthmaintenance.HealthMaintenanceService;
import com.insta.hms.core.clinical.notes.NotesService;
import com.insta.hms.core.clinical.obstetric.ObstetricHistoryService;
import com.insta.hms.core.clinical.outpatient.DoctorConsultationImageService;
import com.insta.hms.core.clinical.outpatient.DoctorConsultationService;
import com.insta.hms.core.clinical.pac.PreAnaesthestheticService;
import com.insta.hms.core.clinical.vital.GenericVitalFormService;
import com.insta.hms.core.patient.followupdetails.FollowUpService;
import com.insta.hms.core.patient.registration.RegistrationService;
import com.insta.hms.customreports.CustomReportsAction;
import com.insta.hms.dischargemedication.DischargeMedicationDAO;
import com.insta.hms.documents.DocPatientHeaderTemplatesRepository;
import com.insta.hms.documents.DocPrintConfigurationRepository;
import com.insta.hms.documents.GenericDocumentsUtil;
import com.insta.hms.documents.PrintConfigurationRepository;
import com.insta.hms.genericdocuments.GenericDocumentsFields;
import com.insta.hms.imageretriever.DoctorConsultImageRetriever;
import com.insta.hms.integration.insurance.erxprescription.ERxService;
import com.insta.hms.integration.insurance.pbm.PBMPrescriptionsService;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.mdm.centers.CenterService;
import com.insta.hms.mdm.dischargesummarytemplates.DischargeSummaryTemplateService;
import com.insta.hms.mdm.doctors.DoctorRepository;
import com.insta.hms.mdm.notetypes.NoteTypesRepository;
import com.insta.hms.mdm.prescriptionsprinttemplates.PrescriptionsTemplateService;
import com.insta.hms.mdm.printerdefinition.PrinterDefinitionService;
import com.insta.hms.mdm.printtemplates.PrintTemplate;
import com.insta.hms.mdm.printtemplates.PrintTemplateService;
import com.insta.hms.mdm.vitalparameters.VitalParameterService;
import com.insta.hms.security.usermanager.UserService;
import com.lowagie.text.DocumentException;

import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

/**
 * The Class PrintService.
 *
 * @author sonam
 */
@Service
public class PrintService {

  /** The log. */
  static Logger log = LoggerFactory.getLogger(PrintService.class);

  /** The printer definition service. */
  @LazyAutowired
  private PrinterDefinitionService printerDefinitionService;

  /** The prescriptions template service. */
  @LazyAutowired
  PrescriptionsTemplateService prescriptionsTemplateService;

  /** The session service. */
  @LazyAutowired
  private SessionService sessionService;

  /** The center service. */
  @LazyAutowired
  private CenterService centerService;

  /** The doctor consultation service. */
  @LazyAutowired
  private DoctorConsultationService doctorConsultationService;

  /** The generic documents util. */
  @LazyAutowired
  private GenericDocumentsUtil genericDocumentsUtil;

  /** The gen pref service. */
  @LazyAutowired
  private GenericPreferencesService genPrefService;

  /** The prescriptions service. */
  @LazyAutowired
  private PrescriptionsService prescriptionsService;

  /** The e rx service. */
  @LazyAutowired
  private ERxService erxService;

  /** The follow up service. */
  @LazyAutowired
  private FollowUpService followUpService;

  /** The secondary complaint service. */
  @LazyAutowired
  private SecondaryComplaintService secondaryComplaintService;

  /** The stn dtls service. */
  @LazyAutowired
  private SectionDetailsService stnDtlsService;

  /** The diagnosis details service. */
  @LazyAutowired
  private DiagnosisDetailsService diagnosisDetailsService;

  /** The allergies service. */
  @LazyAutowired
  private AllergiesService allergiesService;

  /** The vital parameter service. */
  @LazyAutowired
  private VitalParameterService vitalParameterService;

  /** The generic vital form service. */
  @LazyAutowired
  private GenericVitalFormService genericVitalFormService;

  /** The user service. */
  @LazyAutowired
  private UserService userService;

  /** The doctor consultation image service. */
  @LazyAutowired
  private DoctorConsultationImageService doctorConsultationImageService;

  /** The pre anaesthesthetic service. */
  @LazyAutowired
  private PreAnaesthestheticService preAnaesthestheticService;

  /** The health maintenance service. */
  @LazyAutowired
  private HealthMaintenanceService healthMaintenanceService;

  /** The obstetric history service. */
  @LazyAutowired
  private ObstetricHistoryService obstetricHistoryService;

  /** The antenatal service. */
  @LazyAutowired
  private AntenatalService antenatalService;

  /** The registration service. */
  @LazyAutowired
  private RegistrationService registrationService;

  /** The print template service. */
  @LazyAutowired
  private PrintTemplateService printTemplateService;

  /** The ph template repo. */
  @LazyAutowired
  private DocPatientHeaderTemplatesRepository phTemplateRepo;

  /** The pbm prescriptions service. */
  @LazyAutowired
  private PBMPrescriptionsService pbmPrescriptionsService;

  /** The notes service. */
  @LazyAutowired
  private NotesService notesService;

  /** The discharge summary template service. */
  @LazyAutowired
  private DischargeSummaryTemplateService dischargeSummaryTemplateService;

  /** The clinical pref service. */
  @LazyAutowired
  private ClinicalPreferencesService clinicalPrefService;

  /** The Constant PRINT_TYPE_PATIENT. */
  public static final String PRINT_TYPE_PATIENT = "Discharge";
  
  @LazyAutowired
  private NoteTypesRepository noteTypesRepository;

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

  /** The Constant FORM_ID. */
  private static final String FORM_ID = "form_id";

  /** The Constant SECTIONS. */
  private static final String SECTIONS = "sections";

  /** The Constant OP_PRESCRIPTION. */
  private static final String OP_PRESCRIPTION = "OP Prescription";

  /** The Constant PRINT_MODE. */
  private static final String PRINT_MODE = "print_mode";

  /** The Constant LOGO_HEADER. */
  private static final String LOGO_HEADER = "logo_header";

  /** The Constant TEXT_COLUMS. */
  private static final String TEXT_COLUMS = "textColumns";

  /** The Constant TEXT_REPORT. */
  private static final String TEXT_REPORT = "textReport";

  /** The Constant TEXT_MODE_COLUMNS. */
  private static final String TEXT_MODE_COLUMNS = "text_mode_column";

  /** The Constant PRINTER_TYPE. */
  private static final String PRINTER_TYPE = "printerType";

  /** The Constant IPEMR_SUMMARY_RECORD. */
  private static final String IPEMR_SUMMARY_RECORD = "IP EMR Summary Record";

  /** The Constant PATIENT_NOTES. */
  private static final String PATIENT_NOTES = "Patient Notes";

  /** The Constant PHYSICIAN_ORDER. */
  private static final String PHYSICIAN_ORDER = "Physician Order";

  /** The Constant VITAL_CHARTS. */
  private static final String VITAL_CHARTS = "Vitals Charts";

  /** The cfg. */
  private Configuration cfg = null;
  
  @LazyAutowired
  private DoctorRepository doctorRepository;

  /**
   * Instantiates a new prints the service.
   */
  public PrintService() {
    cfg = AppInit.getFmConfig();
  }
  
  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(CustomReportsAction.class);


  /**
   * Gets the printer definitions.
   *
   * @return the printer definitions
   * @throws SQLException the SQL exception
   */
  public Map<String, Object> getPrinterDefinitions() throws SQLException {
    Map<String, Object> data = new HashMap<>();
    data.put("printerDefinition", ConversionUtils
        .listBeanToListMap(printerDefinitionService.lookup(false)));
    BasicDynaBean genericPrefs = genPrefService.getPreferences();
    BasicDynaBean clinicalPrefs = clinicalPrefService.getClinicalPreferences();
    Integer printerId = (Integer) DocPrintConfigurationRepository
        .getPrescriptionPrintPreferences(
            (String) genericPrefs.get("default_consultation_print_template"))
        .get("printer_settings");
    Integer dischargePrinterId = (Integer) DocPrintConfigurationRepository
        .getDischargeSummaryConfiguration().get("printer_settings");
    data.put("defaultPrinter", printerId);
    data.put("dischargeDefaultPrinter", dischargePrinterId);
    data.put("ip_cases_across_doctors",
        clinicalPrefs.get("ip_cases_across_doctors"));
    return data;
  }

  /**
   * Gets the templates.
   *
   * @return the templates
   */
  public Map<String, String> getTemplates() {
    return getConsultationTemplateName();
  }

  /**
   * Gets the consultation list.
   *
   * @param mrNo
   *          the mr no
   * @return the consultation list
   */
  public Map<String, Object> getConsultationList(String mrNo) {
    Map<String, Object> response = new HashMap<>();
    Map<String, Object> sessionAttributes = sessionService
        .getSessionAttributes();
    String loggedInDoctorId = (String) (userService
        .findByKey("emp_username", (String) sessionAttributes.get("userId"))
        .get("doctor_id"));
    boolean isDoctorLogin = loggedInDoctorId != null
        && !loggedInDoctorId.equals("");
    List<BasicDynaBean> conslistBeans = doctorConsultationService
        .getConsultationListForPrint(mrNo, isDoctorLogin);
    response.put("consultations",
        ConversionUtils.listBeanToListMap(conslistBeans));
    return response;
  }

  /**
   * Gets the consultation template name.
   *
   * @return the consultation template name
   */
  public Map<String, String> getConsultationTemplateName() {
    Map<String, String> templateMap = new HashMap<>();
    List<BasicDynaBean> printTemplateBeanList = prescriptionsTemplateService
        .lookup(false);
    templateMap.put("CUSTOM-BUILTIN_HTML", "Built-in Default HTML template");
    templateMap.put("CUSTOM-BUILTIN_TEXT", "Built-in Default Text template");
    for (BasicDynaBean template : printTemplateBeanList) {
      templateMap.put("CUSTOM-" + template.get("template_name").toString(),
          template.get("template_name").toString());
    }
    return templateMap;
  }

  /**
   * Gets the all templates.
   *
   * @return the all templates
   */
  public Map<String, Object> getAllTemplates() {
    Map<String, Object> dischargeTemplateMap = new HashMap<>();
    dischargeTemplateMap.put("templates", ConversionUtils.listBeanToListMap(
        dischargeSummaryTemplateService.getAllActiveTemplates()));
    return dischargeTemplateMap;
  }

  /**
   * Gets the patient visits.
   *
   * @param mrNo
   *          the mr no
   * @param visitType
   *          the visit type
   * @param activeOnly
   *          the active only
   * @return the patient visits
   */
  @SuppressWarnings("rawtypes")
  public List getPatientVisits(String mrNo, String visitType,
      boolean activeOnly) {
    return registrationService.getPatientVisits(mrNo, visitType, activeOnly,
        false);
  }

  /**
   * Gets the all visits.
   *
   * @param mrNo the mr no
   * @param activeOnly the active only
   * @return the all visits
   */
  @SuppressWarnings("rawtypes")
  public List getAllVisits(String mrNo, boolean activeOnly) {
    return registrationService.getPatientAllVisits(mrNo, activeOnly);
  }

  /**
   * Prints the consultation.
   *
   * @param consId
   *          the cons id
   * @param templateName
   *          the template name
   * @param printerId
   *          the printer id
   * @param logoHeader
   *          the logo header
   * @param requestMap
   *          the request map
   * @param response
   *          the response
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws XPathExpressionException
   *           the x path expression exception
   * @throws DocumentException
   *           the document exception
   * @throws TransformerException
   *           the transformer exception
   * @throws TemplateException
   *           the template exception
   */
  public void printConsultation(Integer consId, String templateName,
      Integer printerId, String logoHeader, Map<String, Object> requestMap,
      HttpServletResponse response)
      throws SQLException, IOException, XPathExpressionException,
      DocumentException, TransformerException, TemplateException {

    BasicDynaBean genericPrefs = genPrefService.getPreferences();
    if (templateName == null || templateName.equals("")) {
      templateName = (String) genericPrefs
          .get("default_consultation_print_template");
    }

    BasicDynaBean prefs = null;
    prefs = DocPrintConfigurationRepository
        .getPrescriptionPrintPreferences(templateName, printerId);

    String printMode = "P";
    if (prefs.get(PRINT_MODE) != null) {
      printMode = (String) prefs.get(PRINT_MODE);
    }

    if (logoHeader != null && !logoHeader.equals("")
        && (logoHeader.equalsIgnoreCase(Constants.STRING_Y) || logoHeader.equalsIgnoreCase("L")
            || logoHeader.equalsIgnoreCase("H")
            || logoHeader.equalsIgnoreCase(Constants.STRING_N))) {
      prefs.set(LOGO_HEADER, logoHeader.toUpperCase());
    }

    if (printMode.equals("P")) {
      OutputStream os = response.getOutputStream();
      response.setContentType(Constants.CONTENT_TYPE_PDF);
      getConsultationReport(consId, templateName, ReturnType.PDF, prefs, os);
      os.close();

    } else {
      String textReport = new String(getConsultationReport(consId, templateName,
          ReturnType.TEXT_BYTES, prefs, null));
      requestMap.put(TEXT_REPORT, textReport);
      requestMap.put(TEXT_COLUMS, prefs.get(TEXT_MODE_COLUMNS));
      requestMap.put(PRINTER_TYPE, "DMP");
    }
  }
  
  /**
   * prints the discharge medication.
   * @param patientId consultationId
   * @param templateName templateName to be used
   * @param printerId printerId to be used
   * @param logoHeader logo Information
   * @param requestMap requestMap
   * @param response response to be sent
   * @param consultationType IP/OP
   * @throws SQLException exception that can be thrown
   * @throws IOException exception  that can thrown
   * @throws XPathExpressionException exception when path cant be found
   * @throws DocumentException when no document is present
   * @throws TransformerException exception  that can thrown 
   * @throws TemplateException exception that can be thrown
   */
  public void printDischargeMedication(String patientId, String templateName, Integer printerId,
      String logoHeader, Map<String, Object> requestMap, HttpServletResponse response,
      String consultationType) throws SQLException, IOException, XPathExpressionException,
      DocumentException, TransformerException, TemplateException {

    BasicDynaBean genericPreference = genPrefService.getPreferences();
    if (StringUtils.isEmpty(templateName)) {
      templateName = (String) genericPreference.get("default_consultation_print_template");
    }
    boolean useStoreItems = genericPreference.get("prescription_uses_stores").equals("Y");

    BasicDynaBean prefs =
        DocPrintConfigurationRepository.getPrescriptionPrintPreferences(templateName, printerId);
    String printMode = "P";
    if (prefs.get(PRINT_MODE) != null) {
      printMode = (String) prefs.get(PRINT_MODE);
    }

    if (!StringUtils.isEmpty(logoHeader) || logoHeader.equalsIgnoreCase(Constants.STRING_Y)
        || logoHeader.equalsIgnoreCase("L") || logoHeader.equalsIgnoreCase("H")
        || logoHeader.equalsIgnoreCase(Constants.STRING_N)) {
      prefs.set(LOGO_HEADER, logoHeader.toUpperCase());
    }

    if (printMode.equals("P")) {
      OutputStream os = response.getOutputStream();
      response.setContentType(Constants.CONTENT_TYPE_PDF);
      getDischargeMedicationReport(patientId, templateName, ReturnType.PDF, prefs, os,
          useStoreItems, consultationType);
      os.close();
    } else {
      String textReport = new String(getDischargeMedicationReport(patientId, templateName,
          ReturnType.TEXT_BYTES, prefs, null, useStoreItems, consultationType));
      requestMap.put(TEXT_REPORT, textReport);
      requestMap.put(TEXT_COLUMS, prefs.get(TEXT_MODE_COLUMNS));
      requestMap.put(PRINTER_TYPE, "DMP");
    }
  }

  /**
   * Gets the consultation report.
   *
   * @param consultId
   *          the consult id
   * @param templateName
   *          the template name
   * @param enumType
   *          the enum type
   * @param prefs
   *          the prefs
   * @param os
   *          the os
   * @return the consultation report
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws TemplateException
   *           the template exception
   * @throws XPathExpressionException
   *           the x path expression exception
   * @throws DocumentException
   *           the document exception
   * @throws TransformerException
   *           the transformer exception
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public byte[] getConsultationReport(int consultId, String templateName,
      ReturnType enumType, BasicDynaBean prefs, OutputStream os)
      throws SQLException, IOException, TemplateException,
      XPathExpressionException, DocumentException, TransformerException {
    byte[] bytes = null;
    BasicDynaBean consultBean = doctorConsultationService
        .getDoctorConsultDetails(consultId);
    Map patientDetailsMap = new HashMap();
    Preferences sessionPrefs = APIUtility.getPreferences();
    genericDocumentsUtil.copyPatientDetails(patientDetailsMap, null,
        (String) consultBean.get(Constants.PATIENT_ID), false);
    Map<String, Object> templateMap = new HashMap();
    templateMap.put("visitdetails", patientDetailsMap);
    templateMap.put("modules_activated", sessionPrefs.getModulesActivatedMap());
    String bedType = (String) patientDetailsMap.get("alloc_bed_type");
    bedType = bedType == null || bedType.equals("")
        ? (String) patientDetailsMap.get("bill_bed_type") : bedType;
    bedType = bedType == null || bedType.equals("") ? "GENERAL" : bedType;

    String center = (String) patientDetailsMap.get(Constants.CENTER_ID);
    int centerId = Integer.parseInt(center);

    List<BasicDynaBean> allPrescriptions = prescriptionsService
        .getPrescriptionsForPrints(consultId, bedType,
            (String) patientDetailsMap.get(Constants.ORG_ID), centerId);
    
    List<BasicDynaBean> prescMedicineList =
        PrescriptionsService.getPrescribedItems(allPrescriptions, "Medicine");
    
    templateMap.put("presMedicines", prescMedicineList);
    templateMap.put("presServices",
        PrescriptionsService.getPrescribedItems(allPrescriptions, "Service"));
    templateMap.put("presTests",
        PrescriptionsService.getPrescribedItems(allPrescriptions, "Inv."));
    templateMap.put("presConsultation",
        PrescriptionsService.getPrescribedItems(allPrescriptions, "Doctor"));
    templateMap.put("presInstructions", PrescriptionsService
        .getPrescribedItems(allPrescriptions, "Instructions"));
    templateMap.put("NonHospitalItems", PrescriptionsService
        .getPrescribedItems(allPrescriptions, "NonHospital"));
    templateMap.put("presOperations",
        PrescriptionsService.getPrescribedItems(allPrescriptions, "Operation"));
    templateMap.put(Constants.VITALS, Collections.emptyList());
    templateMap.put("standing_instructions", Collections.emptyList());
    templateMap.put("printDoctorNotes", true);
    
    List<String> doctorIds = new ArrayList<>();
    HashMap<String, List<BasicDynaBean>> medicationDetailsMap = new HashMap<>();
    if (prescMedicineList != null && !prescMedicineList.isEmpty()) {
      for (BasicDynaBean prescMedicine : prescMedicineList) {
        String doctorId = (String) prescMedicine.get("prescribed_doctor_id");
        if (StringUtils.isEmpty(doctorId)) {
          doctorId = "--";
        }
        if (!medicationDetailsMap.containsKey(doctorId)) {
          doctorIds.add(doctorId);
          List<BasicDynaBean> prescMedicines = new ArrayList<>();
          prescMedicines.add(prescMedicine);
          medicationDetailsMap.put(doctorId, prescMedicines);
        } else {
          medicationDetailsMap.get(doctorId).add(prescMedicine);
        }
      }
    }

    templateMap.put("presMedicinesMap", medicationDetailsMap);

    HashMap<String, BasicDynaBean> doctorMap = new HashMap<>();
    if (medicationDetailsMap != null && !medicationDetailsMap.isEmpty()) {
      List<BasicDynaBean> doctorsList = doctorRepository.getDoctorDepartmentInfo(doctorIds);
      if (doctorsList != null && !doctorsList.isEmpty()) {
        for (BasicDynaBean doctor : doctorsList) {
          doctorMap.put((String) doctor.get("doctor_id"), doctor);
        }
      }
    }

    templateMap.put("doctorMap", doctorMap);


    Integer pbmPrescId = prescriptionsService.getErxConsPBMId(consultId);
    BasicDynaBean erxConsBean = null;
    List erxActivities = Collections.emptyList();
    if (pbmPrescId != null && pbmPrescId > 0) {
      // Erx Activity Ids
      erxActivities = prescriptionsService.getErxPrescribedActivities(
          pbmPrescId,
          (String) centerService.findByKey(centerId).get("health_authority"));
      erxConsBean = pbmPrescriptionsService.getConsErxDetails(pbmPrescId);
    }
    List<BasicDynaBean> followupBean = followUpService
        .getfollowUpDetails((String) consultBean
            .get(Constants.PATIENT_ID));
    if (!followupBean.isEmpty()) {
      templateMap.put("followup_date",
          (String) followupBean.get(0).get("followup_date"));
      templateMap.put("followupDetails", followupBean);
    }
    List packageComponents = Collections.emptyList();
    List tests = PrescriptionsService.getPrescribedItems(allPrescriptions,
        "Inv.");
    if (!tests.isEmpty()) {
      packageComponents = prescriptionsService
          .getAllPackageComponents(consultId);
    }
    String userName = RequestContext.getUserName();
    templateMap.put(Constants.SECONDARY_COMPLAINTS, secondaryComplaintService
        .getSecondaryComplaints((String) consultBean.get(Constants.PATIENT_ID)));
    templateMap.put("erx_cons_bean", erxConsBean);
    templateMap.put("erxActivities", erxActivities);
    templateMap.put("packageComponents", packageComponents);
    templateMap.put("consultation_bean", consultBean);
    templateMap.put("userName", userName);
    String visitType = (String) patientDetailsMap.get("op_type");
    String formType = null;
    if (visitType.equals("F")) {
      formType = "Form_OP_FOLLOW_UP_CONS";
    } else {
      formType = "Form_CONS";
    }
    BasicDynaBean compBean = stnDtlsService.getComponentDetails(formType,
        consultId, consultBean);
    Integer formId = (Integer) compBean.get(FORM_ID);
    List<BasicDynaBean> consValues = stnDtlsService.getAllSectionDetails(
        (String) consultBean.get(Constants.MR_NO),
        (String) consultBean.get(Constants.PATIENT_ID), consultId, 0, formId, "CONS");

    Map<Object, List<List>> map = ConversionUtils.listBeanToMapListListBean(
        consValues, "str_section_detail_id", Constants.FIELD_ID);
    templateMap.put("PhysicianForms", map);
    templateMap.put("diagnosis_details", diagnosisDetailsService
        .getAllDiagnosisDetails((String) consultBean.get(Constants.PATIENT_ID)));
    templateMap.put("coder_diagnosis_details", diagnosisDetailsService
        .getAllCoderDiagnosisDetails((String) consultBean.get(Constants.PATIENT_ID)));
    String noteTakerEnabled = Constants.STRING_N;
    List allergies = Collections.emptyList();
    List pacDetails = Collections.emptyList();
    List healthMaintenance = Collections.emptyList();
    List pregnancyhistories = Collections.emptyList();
    List pregnancyhistoriesBean = Collections.emptyList();
    // Legacy support for customize template
    List antenatalinfo = Collections.emptyList(); 
    Set antenatalKeyCounts = null;
    Map antenatalinfoMap = null;
    List consultationFields = Collections.emptyList();
    Map consultationFieldsMap = null;
    List imagesList = Collections.emptyList(); // note taker images.
    List vitalParams = Collections.emptyList();
    List vitalReadings = Collections.emptyList();
    Map<String, Object> consultCompMap = new HashMap();

    for (String comp : ((String) compBean.get(SECTIONS)).split(",")) {
      int sectionId = 0;
      if (!StringUtils.isEmpty(comp)) {
        sectionId = Integer.parseInt(comp);
      }
      if (sectionId == -1) {
        consultCompMap.put("complaint", Constants.STRING_Y);
      } else if (sectionId == -2) {
        consultCompMap.put("allergies", Constants.STRING_Y);
        allergies = allergiesService.getAllActiveAllergies(
            (String) consultBean.get(Constants.MR_NO),
            (String) consultBean.get(Constants.PATIENT_ID), consultId, 0, formId,
            "CONS");

      } else if (sectionId == -4) {
        consultCompMap.put(Constants.VITALS, Constants.STRING_Y);
        vitalParams =
            vitalParameterService.getUniqueVitalsforPatient((String) consultBean.get("patient_id"));
        if (vitalParams == null || vitalParams.isEmpty()) {
          vitalParams = vitalParameterService.getActiveVitalParams("O");
        }
        vitalReadings =
            genericVitalFormService.groupByReadingId((String) consultBean.get("patient_id"), "V");
      } else if (sectionId == -5) {
        consultCompMap.put("consultation_notes", Constants.STRING_Y);
        consultationFields = doctorConsultationService
            .getConsultationFieldsValues(consultId, true, true);
        consultationFieldsMap = ConversionUtils
            .listBeanToMapBean(consultationFields, "field_name");
        BasicDynaBean userBean = userService.findByKey("emp_username", userName);
        if (userBean != null) {
          noteTakerEnabled = (String) userBean.get("prescription_note_taker");
        }
        List<String> imageColumnList = new ArrayList();
        imageColumnList.add("image_id");
        imageColumnList.add("datetime");
        imagesList = doctorConsultationImageService.listAll(imageColumnList,
            "consultation_id", consultId);
      } else if (sectionId == -16) {
        pacDetails = preAnaesthestheticService.getAllPACRecords(
            (String) consultBean.get(Constants.MR_NO),
            (String) consultBean.get(Constants.PATIENT_ID), consultId, 0, formId,
            "CONS");
      } else if (sectionId == -15) {
        healthMaintenance = healthMaintenanceService.getAllHealthMaintenance(
            (String) consultBean.get(Constants.MR_NO),
            (String) consultBean.get(Constants.PATIENT_ID), consultId, 0, formId,
            "CONS");
      } else if (sectionId == -13) {
        pregnancyhistories = obstetricHistoryService.getAllPregnancyDetails(
            (String) consultBean.get(Constants.MR_NO),
            (String) consultBean.get(Constants.PATIENT_ID), consultId, 0, "CONS",
            formId);
        pregnancyhistoriesBean = obstetricHistoryService
            .getAllObstetricHeadDetails((String) consultBean.get(Constants.MR_NO),
                (String) consultBean.get(Constants.PATIENT_ID), consultId, 0, "CONS",
                formId);

      } else if (sectionId == -14) {

        antenatalinfo = antenatalService.getAllAntenatalDetails(
            (String) consultBean.get(Constants.MR_NO),
            (String) consultBean.get(Constants.PATIENT_ID), consultId, 0, "CONS",
            formId);
        antenatalinfoMap = ConversionUtils.listBeanToMapListBean(antenatalinfo,
            "pregnancy_count_key");
        antenatalKeyCounts = antenatalinfoMap.keySet();
      } else if (sectionId == -17) {
        consultCompMap.put("immunization", Constants.STRING_Y);
      }
    }
    consultCompMap.putAll(compBean.getMap());
    templateMap.put("allergies", allergies);
    templateMap.put("consultationFieldsMap", consultationFieldsMap);
    templateMap.put("consultationFields", consultationFields);
    templateMap.put("consultImages", imagesList);
    templateMap.put("noteTakerEnabled", noteTakerEnabled);
    templateMap.put("pac_details", pacDetails);
    templateMap.put("health_maintenance", healthMaintenance);
    templateMap.put("pregnancyhistories", pregnancyhistories);
    templateMap.put("pregnancyhistoriesBean", pregnancyhistoriesBean);
    templateMap.put("antenatalinfo", antenatalinfo);
    templateMap.put("antenatalKeyCounts", antenatalKeyCounts);
    templateMap.put("antenatalinfoMap", antenatalinfoMap);
    templateMap.put(Constants.VITAL_PARAMS, vitalParams);
    templateMap.put("vital_values", vitalReadings);
    templateMap.put("consultation_components", consultCompMap);
    templateMap.put("insta_sections",
        stnDtlsService.getAddedSectionMasterDetails(
            (String) consultBean.get(Constants.MR_NO),
            (String) consultBean.get(Constants.PATIENT_ID), consultId, 0, formId,
            "CONS"));
    String templateContent = null;
    String templateMode = null;
    boolean isClosed = consultBean.get("status").equals("C");
    FtlReportGenerator ftlGen = null;
    if (templateName.equals("BUILTIN_HTML")
        || templateName.equals("CUSTOM-BUILTIN_HTML")) {
      ftlGen = new FtlReportGenerator("ConsultationPrint");
      templateMode = "H";
    } else if (templateName.equals("BUILTIN_TEXT")
        || templateName.equals("CUSTOM-BUILTIN_TEXT")) {
      ftlGen = new FtlReportGenerator("ConsultationTextPrint");
      templateMode = "T";
    } else {

      BasicDynaBean presTemplateBean = prescriptionsTemplateService
          .getTemplateContent(templateName);
      if (presTemplateBean == null) {
        return bytes; //Returning empty array
      }
      templateContent = (String) presTemplateBean
          .get("prescription_template_content");
      templateMode = (String) presTemplateBean.get("template_mode");
      StringReader reader = new StringReader(templateContent);
      ftlGen = new FtlReportGenerator("ConsultationSheetPrint.ftl", reader);
    }
    StringWriter writer = new StringWriter();
    try {
      ftlGen.setReportParams(templateMap);
      ftlGen.process(writer);
    } catch (TemplateException te) {
      log.error(te.getMessage());
      throw te;
    }

    HtmlConverter hc = new HtmlConverter(new DoctorConsultImageRetriever());
    Boolean repeatPHeader = ((String) prefs.get(Constants.REPEAT_PATIENT_INFO))
        .equals(Constants.STRING_Y);
    if (enumType.equals(ReturnType.PDF)) {
      if (templateMode != null && templateMode.equals("T")) {
        hc.textToPDF(writer.toString(), os, prefs);
      } else {
        hc.writePdf(os, writer.toString(), OP_PRESCRIPTION, prefs, false,
            repeatPHeader, true, true, isClosed, false);
      }
      os.close();

    } else if (enumType.equals(ReturnType.PDF_BYTES)) {
      ByteArrayOutputStream stream = new ByteArrayOutputStream();
      if (templateMode != null && templateMode.equals("T")) {
        hc.textToPDF(writer.toString(), stream, prefs);
      } else {
        hc.writePdf(stream, writer.toString(), OP_PRESCRIPTION, prefs, false,
            repeatPHeader, true, true, isClosed, false);
      }
      bytes = stream.toByteArray();
      stream.close();

    } else if (enumType.equals(ReturnType.TEXT_BYTES)) {
      if (templateMode != null && templateMode.equals("T")) {
        bytes = writer.toString().getBytes();
      } else {
        bytes = hc.getText(writer.toString(), OP_PRESCRIPTION, prefs, true,
            true);
      }
    }
    return bytes;
  }
  
  /**
   * gets the discharge medication that has been prescribed.
   * @param consultationId the consultationId
   * @param templateName templateName to be used
   * @param enumType return type
   * @param prefs preferences
   * @param os output stream
   * @param useStoreItems boolean to check
   * @param consultationType IP/OP 
   * @return list of discharge medication
   * @throws SQLException exception that can be thrown
   * @throws IOException exception that can be thrown
   * @throws TemplateException exception that can be thrown
   * @throws XPathExpressionException exception that can be thrown
   * @throws DocumentException exception that can be thrown
   * @throws TransformerException exception that can be thrown
   */
  public byte[] getDischargeMedicationReport(String consultationId, String templateName,
      ReturnType enumType, BasicDynaBean prefs, OutputStream os, boolean useStoreItems,
      String consultationType) throws SQLException, IOException, TemplateException,
      XPathExpressionException, DocumentException, TransformerException {
    byte[] bytes = null;
    Map<String, String> patientDetailsMap = new HashMap<>();
    Preferences sessionPrefs = APIUtility.getPreferences();
    Map<String, Object> templateMap = new HashMap<>();
    BasicDynaBean consultBean = null;
    if (consultationType.equals("IP")) {
      GenericDocumentsFields.copyPatientDetails(patientDetailsMap, null, consultationId, false);
    } else {
      consultBean =
          doctorConsultationService.getDoctorConsultDetails(Integer.parseInt(consultationId));
      genericDocumentsUtil.copyPatientDetails(patientDetailsMap, null,
          (String) consultBean.get(Constants.PATIENT_ID), false);
    }

    templateMap.put("visitdetails", patientDetailsMap);


    templateMap.put("modules_activated", sessionPrefs.getModulesActivatedMap());
    String bedType = (String) patientDetailsMap.get("alloc_bed_type");
    bedType =
        bedType == null || bedType.equals("") ? (String) patientDetailsMap.get("bill_bed_type")
            : bedType;
    bedType = bedType == null || bedType.equals("") ? "GENERAL" : bedType;

    String center = (String) patientDetailsMap.get(Constants.CENTER_ID);
    int centerId = Integer.parseInt(center);

    String helathAuthority = CenterMasterDAO.getHealthAuthorityForCenter(centerId);
    String visitType = (String) patientDetailsMap.get("visit_type");;
    String orgId = (String) patientDetailsMap.get("org_id");;
    String patientId = (String) patientDetailsMap.get("patient_id");
    List<BasicDynaBean> dischargeMedicationBeanList = DischargeMedicationDAO
        .getDischargeMedicationDetails(patientId, helathAuthority, useStoreItems, visitType, orgId);
    
    List<String> doctorIds = new ArrayList<>();
    HashMap<String, List<BasicDynaBean>> medicationDetailsMap = new HashMap<>();
    if (dischargeMedicationBeanList != null && !dischargeMedicationBeanList.isEmpty()) {
      for (BasicDynaBean dischargeMedication : dischargeMedicationBeanList) {
        String doctorId = (String) dischargeMedication.get("doctor_id");
        if (StringUtils.isEmpty(doctorId)) {
          doctorId = "--";
        }
        if (!medicationDetailsMap.containsKey(doctorId)) {
          doctorIds.add(doctorId);
          List<BasicDynaBean> dischargeMedications = new ArrayList<>();
          dischargeMedications.add(dischargeMedication);
          medicationDetailsMap.put(doctorId, dischargeMedications);
        } else {
          medicationDetailsMap.get(doctorId).add(dischargeMedication);
        }
      }
    }

    templateMap.put("medicationDetailsMap", medicationDetailsMap);

    HashMap<String, BasicDynaBean> doctorMap = new HashMap<>();
    if (medicationDetailsMap != null && !medicationDetailsMap.isEmpty()) {
      List<BasicDynaBean> doctorsList = doctorRepository.getDoctorDepartmentInfo(doctorIds);
      if (doctorsList != null && !doctorsList.isEmpty()) {
        for (BasicDynaBean doctor : doctorsList) {
          doctorMap.put((String) doctor.get("doctor_id"), doctor);
        }
      }
    }

    templateMap.put("doctorMap", doctorMap);

    String templateContent = null;
    String templateMode = null;
    boolean isClosed = consultBean != null ? consultBean.get("status").equals("C") : false;
    FtlReportGenerator ftlGen = null;
    if (templateName.equals("BUILTIN_HTML") || templateName.equals("CUSTOM-BUILTIN_HTML")) {
      ftlGen = new FtlReportGenerator("DischargeMedication");
      templateMode = "H";
    } else if (templateName.equals("BUILTIN_TEXT") || templateName.equals("CUSTOM-BUILTIN_TEXT")) {
      ftlGen = new FtlReportGenerator("ConsultationTextPrint");
      templateMode = "T";
    } else {

      BasicDynaBean presTemplateBean =
          prescriptionsTemplateService.getTemplateContent(templateName);
      if (presTemplateBean == null) {
        return bytes; // Returning empty array
      }
      templateContent = (String) presTemplateBean.get("prescription_template_content");
      templateMode = (String) presTemplateBean.get("template_mode");
      StringReader reader = new StringReader(templateContent);
      ftlGen = new FtlReportGenerator("ConsultationSheetPrint.ftl", reader);
    }
    StringWriter writer = new StringWriter();
    try {
      ftlGen.setReportParams(templateMap);
      ftlGen.process(writer);
    } catch (TemplateException te) {
      log.error(te.getMessage());
      throw te;
    }

    HtmlConverter hc = new HtmlConverter(new DoctorConsultImageRetriever());
    Boolean repeatPHeader =
        ((String) prefs.get(Constants.REPEAT_PATIENT_INFO)).equals(Constants.STRING_Y);
    if (enumType.equals(ReturnType.PDF)) {
      if (templateMode != null && templateMode.equals("T")) {
        hc.textToPDF(writer.toString(), os, prefs);
      } else {
        hc.writePdf(os, writer.toString(), OP_PRESCRIPTION, prefs, false, repeatPHeader, true, true,
            isClosed, false);
      }
      os.close();

    } else if (enumType.equals(ReturnType.PDF_BYTES)) {
      ByteArrayOutputStream stream = new ByteArrayOutputStream();
      if (templateMode != null && templateMode.equals("T")) {
        hc.textToPDF(writer.toString(), stream, prefs);
      } else {
        hc.writePdf(stream, writer.toString(), OP_PRESCRIPTION, prefs, false, repeatPHeader, true,
            true, isClosed, false);
      }
      bytes = stream.toByteArray();
      stream.close();

    } else if (enumType.equals(ReturnType.TEXT_BYTES)) {
      if (templateMode != null && templateMode.equals("T")) {
        bytes = writer.toString().getBytes();
      } else {
        bytes = hc.getText(writer.toString(), OP_PRESCRIPTION, prefs, true, true);
      }
    }
    return bytes;
  }

  
  /**
   * Gets the triage print.
   *
   * @param consId the cons id
   * @param reqPrinterId the req printer id
   * @param logoHeader the logo header
   * @param requestMap the request map
   * @param response the response
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws XPathExpressionException the x path expression exception
   * @throws DocumentException the document exception
   * @throws TransformerException the transformer exception
   * @throws TemplateException the template exception
   */
  public void getTriagePrint(Integer consId, Integer reqPrinterId,
      String logoHeader, Map<String, Object> requestMap,
      HttpServletResponse response)
      throws SQLException, IOException, XPathExpressionException,
      DocumentException, TransformerException, TemplateException {

    // TODO: for API for all print

    Map<String, Object> sessionAttributes = sessionService
        .getSessionAttributes();
    Integer centerId = (Integer) sessionAttributes.get("centerId");
    BasicDynaBean prefs = null;
    int printerId = 0;
    if ((reqPrinterId != null)) {
      printerId = reqPrinterId;
    }
    prefs = PrintConfigurationRepository.getPageOptions(PRINT_TYPE_PATIENT,
        printerId, centerId);
    String printMode = "P";
    if (prefs.get(PRINT_MODE) != null) {
      printMode = (String) prefs.get(PRINT_MODE);
    }
    // api parameter
    if (logoHeader != null && !logoHeader.equals("")
        && (logoHeader.equalsIgnoreCase(Constants.STRING_Y) || logoHeader.equalsIgnoreCase("L")
            || logoHeader.equalsIgnoreCase("H")
            || logoHeader.equalsIgnoreCase(Constants.STRING_N))) {
      prefs.set(LOGO_HEADER, logoHeader.toUpperCase());
    }

    if (printMode.equals("P")) {
      OutputStream os = response.getOutputStream();
      response.setContentType(Constants.CONTENT_TYPE_PDF);
      getTriageAndClinicalInfoReport(consId, ReturnType.PDF, prefs, os, true);
      os.close();

    } else {
      String textReport = new String(getTriageAndClinicalInfoReport(consId,
          ReturnType.TEXT_BYTES, prefs, null, true));
      requestMap.put(TEXT_REPORT, textReport);
      requestMap.put(TEXT_COLUMS, prefs.get(TEXT_MODE_COLUMNS));
      requestMap.put(PRINTER_TYPE, "DMP");
    }

  }

  /**
   * Gets the triage and clinical info report.
   *
   * @param consultId
   *          the consult id
   * @param enumType
   *          the enum type
   * @param prefs
   *          the prefs
   * @param os
   *          the os
   * @param isTriage
   *          the is triage
   * @return the triage and clinical info report
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws TemplateException
   *           the template exception
   * @throws XPathExpressionException
   *           the x path expression exception
   * @throws DocumentException
   *           the document exception
   * @throws TransformerException
   *           the transformer exception
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public byte[] getTriageAndClinicalInfoReport(int consultId,
      ReturnType enumType, BasicDynaBean prefs, OutputStream os,
      boolean isTriage) throws SQLException, IOException, TemplateException,
      XPathExpressionException, DocumentException, TransformerException {
    byte[] bytes = null;
    BasicDynaBean consultBean = doctorConsultationService
        .getDoctorConsultDetails(consultId);
    Map patientDetailsMap = new HashMap();
    genericDocumentsUtil.copyPatientDetails(patientDetailsMap, null,
        (String) consultBean.get(Constants.PATIENT_ID), false);

    Map ftlParamMap = new HashMap();
    ftlParamMap.put("visitdetails", patientDetailsMap);
    String triFormType = "Form_TRI";
    String deptId = (String) patientDetailsMap.get("dept_id");
    BasicDynaBean triageCompBean = stnDtlsService
        .getTriageComponentDetails(deptId, triFormType, consultId);
    Integer triFormId = (Integer) triageCompBean.get("form_id");

    ftlParamMap.put("diagnosis_details", diagnosisDetailsService
        .getAllDiagnosisDetails((String) consultBean.get(Constants.PATIENT_ID)));

    allergiesService.getAllActiveAllergies((String) consultBean.get(Constants.MR_NO),
        (String) consultBean.get(Constants.PATIENT_ID), consultId, 0, triFormId,
        "CONS");
    ftlParamMap.put("allergies",
        allergiesService.getAllActiveAllergies(
            (String) consultBean.get(Constants.MR_NO),
            (String) consultBean.get(Constants.PATIENT_ID), consultId, 0, triFormId,
            "CONS"));
    ftlParamMap.put("pregnancyhistories",
        obstetricHistoryService.getAllPregnancyDetails(
            (String) consultBean.get(Constants.MR_NO),
            (String) consultBean.get(Constants.PATIENT_ID), consultId, 0, "CONS",
            triFormId));

    ftlParamMap.put("pregnancyhistoriesBean",
        obstetricHistoryService.getAllObstetricHeadDetails(
            (String) consultBean.get(Constants.MR_NO),
            (String) consultBean.get(Constants.PATIENT_ID), consultId, 0, "CONS",
            triFormId));

    Set antenatalKeyCounts = null;
    Map antenatalinfoMap = null;
    List antenatalinfo = antenatalService.getAllAntenatalDetails(
        (String) consultBean.get(Constants.MR_NO),
        (String) consultBean.get(Constants.PATIENT_ID), consultId, 0, "CONS",
        triFormId);
    antenatalinfoMap = ConversionUtils.listBeanToMapListBean(antenatalinfo,
        "pregnancy_count_key");
    antenatalKeyCounts = antenatalinfoMap.keySet();
    ftlParamMap.put("antenatalKeyCounts", antenatalKeyCounts);
    ftlParamMap.put("antenatalinfoMap", antenatalinfoMap);
    ftlParamMap.put("antenatalinfo", antenatalinfo);
    Map triageCompMap = new HashMap();
    if (triageCompBean.get(SECTIONS) != null
        && !(((String) triageCompBean.get(SECTIONS)).isEmpty())) {
      for (String comp : ((String) triageCompBean.get(SECTIONS)).split(",")) {
        Integer sectionId = Integer.parseInt(comp);
        if (sectionId == -2) {
          triageCompMap.put("allergies", Constants.STRING_Y);
        }
        if (sectionId == -4) {
          triageCompMap.put(Constants.VITALS, Constants.STRING_Y);
        }
        if (sectionId == -17) {
          triageCompMap.put("immunization", Constants.STRING_Y);
        }
      }
    }
    triageCompMap.putAll(triageCompBean.getMap());
    ftlParamMap.put("triage_components", triageCompMap);
    String bedType = (String) patientDetailsMap.get("alloc_bed_type");
    bedType = bedType == null || bedType.equals("")
        ? (String) patientDetailsMap.get("bill_bed_type") : bedType;
    bedType = bedType == null || bedType.equals("") ? "GENERAL" : bedType;

    String center = (String) patientDetailsMap.get(Constants.CENTER_ID);
    int centerId = Integer.parseInt(center);
    List<BasicDynaBean> allPrescriptions = prescriptionsService
        .getPrescriptionsForPrints(consultId, bedType,
            (String) patientDetailsMap.get(Constants.ORG_ID), centerId);

    List<BasicDynaBean> consValues = stnDtlsService.getAllSectionDetails(
        (String) consultBean.get(Constants.MR_NO),
        (String) consultBean.get(Constants.PATIENT_ID), consultId, 0, triFormId,
        "CONS");

    Map<Object, List<List>> map = ConversionUtils.listBeanToMapListListBean(
        consValues, "str_section_detail_id", Constants.FIELD_ID);
    ftlParamMap.put("HPI", null);
    ftlParamMap.put("ROS", null);
    ftlParamMap.put("PE", null);
    ftlParamMap.put("PhysicianForms", map);
    ftlParamMap.put("presMedicines",
        PrescriptionsService.getPrescribedItems(allPrescriptions, "Medicine"));
    ftlParamMap.put("presServices",
        PrescriptionsService.getPrescribedItems(allPrescriptions, "Service"));
    ftlParamMap.put("presTests",
        PrescriptionsService.getPrescribedItems(allPrescriptions, "Inv."));
    ftlParamMap.put("presConsultation",
        PrescriptionsService.getPrescribedItems(allPrescriptions, "Doctor"));
    ftlParamMap.put("presInstructions", PrescriptionsService
        .getPrescribedItems(allPrescriptions, "Instructions"));
    ftlParamMap.put("NonHospitalItems", PrescriptionsService
        .getPrescribedItems(allPrescriptions, "NonHospital"));
    ftlParamMap.put("presOperations",
        PrescriptionsService.getPrescribedItems(allPrescriptions, "Operation"));

    List<BasicDynaBean> vitalParams =
        vitalParameterService.getUniqueVitalsforPatient(
            (String) consultBean.get(Constants.PATIENT_ID));
    if (vitalParams == null || vitalParams.isEmpty()) {
      vitalParams = vitalParameterService.getActiveVitalParams("O");
    }
    ftlParamMap.put(Constants.VITAL_PARAMS, vitalParams);
    ftlParamMap.put(Constants.VITALS,
        genericVitalFormService.groupByReadingId(
            (String) consultBean.get(Constants.PATIENT_ID), "V"));
    ftlParamMap.put("consultationFields",
        doctorConsultationService.getConsultationFieldsValues(consultId, true, true));
    String visitType = (String) patientDetailsMap.get("op_type");
    String consFormType = null;
    if (visitType.equals("F")) {
      consFormType = "Form_OP_FOLLOW_UP_CONS";
    } else {
      consFormType = "Form_CONS";
    }
    BasicDynaBean consCompBean = stnDtlsService
        .getComponentDetails(consFormType, consultId, consultBean);
    Map consultCompMap = new HashMap();
    if (consCompBean.get(SECTIONS) != null
        && !(((String) consCompBean.get(SECTIONS)).isEmpty())) {
      for (String comp : ((String) consCompBean.get(SECTIONS)).split(",")) {
        Integer formid = Integer.parseInt(comp);
        if (formid == -1) {
          consultCompMap.put("complaint", Constants.STRING_Y);
        }
        if (formid == -2) {
          consultCompMap.put("allergies", Constants.STRING_Y);
        }
        if (formid == -4) {
          consultCompMap.put(Constants.VITALS, Constants.STRING_Y);
        }
        if (formid == -5) {
          consultCompMap.put("consultation_notes", Constants.STRING_Y);
        }

      }
    }
    consultCompMap.putAll(consCompBean.getMap());
    ftlParamMap.put("consultation_components", consultCompMap);
    ftlParamMap.put("triage_insta_sections",
        stnDtlsService.getAddedSectionMasterDetails(
            (String) consultBean.get(Constants.MR_NO),
            (String) consultBean.get(Constants.PATIENT_ID), consultId, 0, triFormId,
            "CONS"));

    ftlParamMap.put("insta_sections",
        stnDtlsService.getAddedSectionMasterDetails(
            (String) consultBean.get(Constants.MR_NO),
            (String) consultBean.get(Constants.PATIENT_ID), consultId, 0,
            (Integer) consCompBean.get("form_id"), "CONS"));
    // for legacy support
    DynaBeanBuilder builder = new DynaBeanBuilder();
    builder.add("past_medical_history");
    builder.add("family_history");
    builder.add("social_history");
    String userName = (String) sessionService.getSessionAttributes()
        .get("userId");
    BasicDynaBean pfshBean = builder.build();
    Preferences sessionPrefs = APIUtility.getPreferences();
    ftlParamMap.put("pfshBean", pfshBean);
    ftlParamMap.put("modules_activated", sessionPrefs.getModulesActivatedMap());
    ftlParamMap.put("consultation_bean", consultBean);
    ftlParamMap.put("userName", userName);
    ftlParamMap.put(Constants.SECONDARY_COMPLAINTS, secondaryComplaintService
        .getSecondaryComplaints((String) consultBean.get(Constants.PATIENT_ID)));
    PrintTemplate printTemplate = null;
    if (isTriage) {
      printTemplate = PrintTemplate.Triage;
    } else {
      printTemplate = PrintTemplate.ClinicalInfo;
    }
    String templateContent = printTemplateService
        .getCustomizedTemplate(printTemplate);
    Template temp = null;
    if (templateContent == null || templateContent.equals("")) {
      temp = cfg.getTemplate(printTemplate.getFtlName() + ".ftl");
    } else {
      StringReader reader = new StringReader(templateContent);
      temp = new Template("ClinicalInformationPrintTemplate.ftl", reader,
          AppInit.getFmConfig());
    }

    boolean isClosed = consultBean.get("status").equals("C");
    String patientHeader = phTemplateRepo.getPatientHeader(
        printTemplateService.getPatientHeaderTemplateId(printTemplate),
        isTriage ? "Triage" : "CI");
    StringReader reader = new StringReader(patientHeader);
    Template pt = new Template("PatientHeader.ftl", reader,
        AppInit.getFmConfig());
    Map templateMap = new HashMap();
    templateMap.put("visitdetails", patientDetailsMap);
    templateMap.put("modules_activated", sessionPrefs.getModulesActivatedMap());
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
    Boolean repeatPHeader = ((String) prefs.get(Constants.REPEAT_PATIENT_INFO))
        .equals(Constants.STRING_Y);
    StringBuilder documentContent = new StringBuilder();
    documentContent.append(pwriter.toString());
    if (enumType.equals(ReturnType.HTML)) {
      String content = writer.toString();
      // which is required to display the images in the jsp, when we render this
      // content in browser
      content = content.replaceAll(
          "PhysicianFieldsImageMarkers.do\\?_method=view",
          "../master/ImageMarkers/ViewImage.do\\?_method=view");
      content = content.replaceAll(
          "PhysicianFieldsImage.do\\?_method=viewImage",
          "../master/SectionFields/ViewImage.do\\?_method=viewImage");
      documentContent.append(content);
    } else {
      documentContent.append(writer.toString());
    }

    if (enumType.equals(ReturnType.PDF)) {
      hc.writePdf(os, documentContent.toString(), OP_PRESCRIPTION, prefs, false,
          repeatPHeader, true, true, isClosed, false);
      os.close();

    } else if (enumType.equals(ReturnType.PDF_BYTES)) {
      ByteArrayOutputStream stream = new ByteArrayOutputStream();
      hc.writePdf(stream, documentContent.toString(), OP_PRESCRIPTION, prefs,
          false, repeatPHeader, true, true, isClosed, false);
      bytes = stream.toByteArray();
      stream.close();

    } else if (enumType.equals(ReturnType.TEXT_BYTES)) {
      bytes = hc.getText(documentContent.toString(), OP_PRESCRIPTION, prefs,
          true, true);
    } else if (enumType.equals(ReturnType.HTML)) {
      return documentContent.toString().getBytes();
    }
    return bytes;
  }

  /**
   * Prints the clinical info.
   *
   * @param consId
   *          the cons id
   * @param reqPrinterId
   *          the req printer id
   * @param logoHeader
   *          the logo header
   * @param requestMap
   *          the request map
   * @param response
   *          the response
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws XPathExpressionException
   *           the x path expression exception
   * @throws DocumentException
   *           the document exception
   * @throws TransformerException
   *           the transformer exception
   * @throws TemplateException
   *           the template exception
   */
  public void printClinicalInfo(Integer consId, Integer reqPrinterId,
      String logoHeader, Map<String, Object> requestMap,
      HttpServletResponse response)
      throws SQLException, IOException, XPathExpressionException,
      DocumentException, TransformerException, TemplateException {
    Map<String, Object> sessionAttributes = sessionService
        .getSessionAttributes();
    Integer centerId = (Integer) sessionAttributes.get("centerId");
    BasicDynaBean prefs = null;
    int printerId = 0;
    if ((reqPrinterId != null)) {
      printerId = reqPrinterId;
    }
    prefs = PrintConfigurationRepository.getPageOptions(PRINT_TYPE_PATIENT,
        printerId, centerId);
    String printMode = "P";
    if (prefs.get(PRINT_MODE) != null) {
      printMode = (String) prefs.get(PRINT_MODE);
    }
    // api parameter
    if (logoHeader != null && !logoHeader.equals("")
        && (logoHeader.equalsIgnoreCase(Constants.STRING_Y) || logoHeader.equalsIgnoreCase("L")
            || logoHeader.equalsIgnoreCase("H")
            || logoHeader.equalsIgnoreCase(Constants.STRING_N))) {
      prefs.set(LOGO_HEADER, logoHeader.toUpperCase());
    }

    if (printMode.equals("P")) {
      OutputStream os = response.getOutputStream();
      response.setContentType(Constants.CONTENT_TYPE_PDF);
      getTriageAndClinicalInfoReport(consId, ReturnType.PDF, prefs, os, false);
      os.close();

    } else {
      String textReport = new String(getTriageAndClinicalInfoReport(consId,
          ReturnType.TEXT_BYTES, prefs, null, true));
      requestMap.put(TEXT_REPORT, textReport);
      requestMap.put(TEXT_COLUMS, prefs.get(TEXT_MODE_COLUMNS));
      requestMap.put(PRINTER_TYPE, "DMP");
    }

  }

  /**
   * Prints the ip emr.
   *
   * @param patientId
   *          the patient id
   * @param reqPrinterId
   *          the req printer id
   * @param logoHeader
   *          the logo header
   * @param requestMap
   *          the request map
   * @param response
   *          the response
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   * @throws XPathExpressionException
   *           the x path expression exception
   * @throws DocumentException
   *           the document exception
   * @throws TransformerException
   *           the transformer exception
   */
  public void printIpEmr(String patientId, Integer reqPrinterId,
      String logoHeader, Map<String, Object> requestMap,
      HttpServletResponse response) throws IOException, SQLException,
      XPathExpressionException, DocumentException, TransformerException {
    Map<String, Object> sessionAttributes = sessionService
        .getSessionAttributes();
    Integer centerId = (Integer) sessionAttributes.get("centerId");
    BasicDynaBean prefs = null;
    int printerId = 0;
    if ((reqPrinterId != null)) {
      printerId = reqPrinterId;
    }
    prefs = PrintConfigurationRepository.getPageOptions(PRINT_TYPE_PATIENT,
        printerId, centerId);
    String printMode = "P";
    if (prefs.get(PRINT_MODE) != null) {
      printMode = (String) prefs.get(PRINT_MODE);
    }

    if (printMode.equals("P")) {
      OutputStream os = response.getOutputStream();
      response.setContentType(Constants.CONTENT_TYPE_PDF);
      getIpEmrReport(patientId, ReturnType.PDF, prefs, os, false);
      os.close();

    } else {
      String textReport = new String(
          getIpEmrReport(patientId, ReturnType.TEXT_BYTES, prefs, null, true));
      requestMap.put(TEXT_REPORT, textReport);
      requestMap.put(TEXT_COLUMS, prefs.get(TEXT_MODE_COLUMNS));
      requestMap.put(PRINTER_TYPE, "DMP");
    }

  }

  /**
   * Gets the ip emr report.
   *
   * @param patientId
   *          the patient id
   * @param enumType
   *          the enum type
   * @param prefs
   *          the prefs
   * @param os
   *          the os
   * @param bool
   *          the bool
   * @return the ip emr report
   * @throws SQLException
   *           the SQL exception
   * @throws TemplateNotFoundException
   *           the template not found exception
   * @throws MalformedTemplateNameException
   *           the malformed template name exception
   * @throws ParseException
   *           the parse exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws XPathExpressionException
   *           the x path expression exception
   * @throws DocumentException
   *           the document exception
   * @throws TransformerException
   *           the transformer exception
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public byte[] getIpEmrReport(String patientId, ReturnType enumType,
      BasicDynaBean prefs, OutputStream os, boolean bool)
      throws SQLException, TemplateNotFoundException,
      MalformedTemplateNameException, ParseException, IOException,
      XPathExpressionException, DocumentException, TransformerException {
    byte[] bytes = null;
    Map patientDetailsMap = new HashMap();
    genericDocumentsUtil.copyPatientDetails(patientDetailsMap, null, patientId,
        false);

    Map<String, Object> ftlParamMap = new HashMap<>();
    ftlParamMap.put("visitdetails", patientDetailsMap);
    Preferences sessionPrefs = APIUtility.getPreferences();
    ftlParamMap.put("modules_activated", sessionPrefs.getModulesActivatedMap());
    ftlParamMap.put(Constants.SECONDARY_COMPLAINTS,
        secondaryComplaintService.getSecondaryComplaints(patientId));
    ftlParamMap.put("diagnosis_details",
        diagnosisDetailsService.getAllDiagnosisDetails(patientId));
    String mrNo = (String) patientDetailsMap.get(Constants.MR_NO);
    BasicDynaBean ipEmrCompBean = stnDtlsService.getIpEmrComponentDetails(mrNo,
        "Form_IP", patientId);
    Integer ipEmrFormId = (Integer) ipEmrCompBean.get("form_id");
    String bedType = (String) patientDetailsMap.get("alloc_bed_type");
    bedType = bedType == null || bedType.equals("")
        ? (String) patientDetailsMap.get("bill_bed_type") : bedType;
    bedType = bedType == null || bedType.equals("") ? "GENERAL" : bedType;

    ftlParamMap.put("allergies", allergiesService.getAllActiveAllergies(mrNo,
        patientId, 0, 0, ipEmrFormId, ""));
    ftlParamMap.put("pac_details", preAnaesthestheticService
        .getAllPACRecords(mrNo, patientId, 0, 0, ipEmrFormId, ""));
    ftlParamMap.put("pregnancyhistories", obstetricHistoryService
        .getAllPregnancyDetails(mrNo, patientId, 0, 0, "", ipEmrFormId));
    ftlParamMap.put("pregnancyhistoriesBean", obstetricHistoryService
        .getAllObstetricHeadDetails(mrNo, patientId, 0, 0, "", ipEmrFormId));
    ftlParamMap.put("antenatalinfo", antenatalService
        .getAllAntenatalDetails(mrNo, patientId, 0, 0, "", ipEmrFormId));
    List<BasicDynaBean> instaFormValues = stnDtlsService
        .getAllSectionDetails(mrNo, patientId, 0, 0, ipEmrFormId, "");

    Map<Object, List<List>> map = ConversionUtils.listBeanToMapListListBean(
        instaFormValues, "str_section_detail_id", Constants.FIELD_ID);
    ftlParamMap.put("PhysicianForms", map);
    ftlParamMap.put("ip_record_components", ipEmrCompBean.getMap());
    ftlParamMap.put("insta_sections", stnDtlsService
        .getAddedSectionMasterDetails(mrNo, patientId, 0, 0, ipEmrFormId, ""));
    PrintTemplate printTemplate = PrintTemplate.IpEmrSummaryRecord;

    String templateContent = printTemplateService
        .getCustomizedTemplate(printTemplate);
    Template temp = null;
    if (templateContent != null && !templateContent.equals("")) {
      StringReader reader = new StringReader(templateContent);
      temp = new Template("IpEmrSummaryRecordReportTemplate.ftl", reader,
          AppInit.getFmConfig());
    } else {
      temp = cfg.getTemplate("IpEmrSummaryRecordReport.ftl");
    }
    StringWriter writer = new StringWriter();
    try {
      temp.process(ftlParamMap, writer);
    } catch (TemplateException exp) {
      log.info(exp.getMessage());
    }
    HtmlConverter hc = new HtmlConverter(new DoctorConsultImageRetriever());
    Boolean repeatPHeader = ((String) prefs.get(Constants.REPEAT_PATIENT_INFO))
        .equals(Constants.STRING_Y);
    if (enumType.equals(ReturnType.PDF)) {
      hc.writePdf(os, writer.toString(), IPEMR_SUMMARY_RECORD, prefs, false,
          repeatPHeader, true, true, true, false);
      os.close();

    } else if (enumType.equals(ReturnType.PDF_BYTES)) {
      ByteArrayOutputStream stream = new ByteArrayOutputStream();
      hc.writePdf(stream, writer.toString(), IPEMR_SUMMARY_RECORD, prefs, false,
          repeatPHeader, true, true, true, false);
      bytes = stream.toByteArray();
      stream.close();
    } else if (enumType.equals(ReturnType.TEXT_BYTES)) {
      bytes = hc.getText(writer.toString(), IPEMR_SUMMARY_RECORD, prefs, true,
          true);
    }
    return bytes;
  }

  /**
   * Prints the emr consultation.
   *
   * @param consultationId the consultation id
   * @param templateName the template name
   * @param printerId the printer id
   * @param logoHeader the logo header
   * @param requestMap the request map
   * @param response the response
   * @throws XPathExpressionException the x path expression exception
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws DocumentException the document exception
   * @throws TransformerException the transformer exception
   * @throws TemplateException the template exception
   */
  public void printEmrConsultation(Integer consultationId, String templateName,
      Integer printerId, String logoHeader, Map<String, Object> requestMap,
      HttpServletResponse response)
      throws XPathExpressionException, SQLException, IOException,
      DocumentException, TransformerException, TemplateException {
    BasicDynaBean genericPrefs = genPrefService.getPreferences();
    if (templateName == null || templateName.equals("")) {
      templateName = (String) genericPrefs.get("default_emr_print_template");
    }
    printConsultation(consultationId, templateName, printerId, logoHeader,
        requestMap, response);
  }

  /**
   * Prints the pres consultation.
   *
   * @param consultationId the consultation id
   * @param templateName the template name
   * @param printerId the printer id
   * @param logoHeader the logo header
   * @param requestMap the request map
   * @param response the response
   * @throws XPathExpressionException the x path expression exception
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws DocumentException the document exception
   * @throws TransformerException the transformer exception
   * @throws TemplateException the template exception
   */
  public void printPresConsultation(Integer consultationId,
      String templateName, Integer printerId, String logoHeader,
      Map<String, Object> requestMap, HttpServletResponse response)
      throws XPathExpressionException, SQLException, IOException,
      DocumentException, TransformerException, TemplateException {
    BasicDynaBean genericPrefs = genPrefService.getPreferences();
    if (templateName == null || templateName.equals("")) {
      templateName = (String) genericPrefs
          .get("default_prescription_print_template");
    }
    printConsultation(consultationId, templateName, printerId, logoHeader,
        requestMap, response);
  }

  /**
   * Prints the patient notes.
   *
   * @param patientId the patient id
   * @param reqPrinterId the req printer id
   * @param logoHeader the logo header
   * @param requestMap the request map
   * @param response the response
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws XPathExpressionException the x path expression exception
   * @throws SQLException the SQL exception
   * @throws DocumentException the document exception
   * @throws TransformerException the transformer exception
   */
  public void printPatientNotes(String patientId, Integer reqPrinterId,
      String logoHeader, Map<String, Object> requestMap, String noteTypeId,
      String hospitalRoleId, HttpServletResponse response)
      throws IOException, XPathExpressionException, SQLException,
      DocumentException, TransformerException {
    Map<String, Object> sessionAttributes = sessionService
        .getSessionAttributes();
    Integer centerId = (Integer) sessionAttributes.get("centerId");
    BasicDynaBean prefs = null;
    int printerId = 0;
    if ((reqPrinterId != null)) {
      printerId = reqPrinterId;
    }
    prefs = PrintConfigurationRepository.getPageOptions(PRINT_TYPE_PATIENT,
        printerId, centerId);
    String printMode = "P";
    if (prefs.get(PRINT_MODE) != null) {
      printMode = (String) prefs.get(PRINT_MODE);
    }

    if (printMode.equals("P")) {
      OutputStream os = response.getOutputStream();
      response.setContentType(Constants.CONTENT_TYPE_PDF);
      getPatientNotesReport(patientId, ReturnType.PDF, prefs, os, false,
          noteTypeId, hospitalRoleId);
      os.close();

    } else {
      String textReport = new String(getPatientNotesReport(patientId,
          ReturnType.TEXT_BYTES, prefs, null, true, noteTypeId, hospitalRoleId));
      requestMap.put(TEXT_REPORT, textReport);
      requestMap.put(TEXT_COLUMS, prefs.get(TEXT_MODE_COLUMNS));
      requestMap.put(PRINTER_TYPE, "DMP");
    }

  }

  /**
   * Gets the patient notes report.
   *
   * @param patientId the patient id
   * @param enumType the enum type
   * @param prefs the prefs
   * @param os the os
   * @param bool the bool
   * @return the patient notes report
   * @throws SQLException the SQL exception
   * @throws TemplateNotFoundException the template not found exception
   * @throws MalformedTemplateNameException the malformed template name exception
   * @throws ParseException the parse exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws XPathExpressionException the x path expression exception
   * @throws DocumentException the document exception
   * @throws TransformerException the transformer exception
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public byte[] getPatientNotesReport(String patientId, ReturnType enumType,
      BasicDynaBean prefs, OutputStream os, boolean bool, String noteTypeId, String hospitalRoleId)
      throws SQLException, TemplateNotFoundException,
      MalformedTemplateNameException, ParseException, IOException,
      XPathExpressionException, DocumentException, TransformerException {
    byte[] bytes = null;
    Map patientDetailsMap = new HashMap();
    genericDocumentsUtil.copyPatientDetails(patientDetailsMap, null, patientId,
        false);

    Map<String, Object> ftlParamMap = new HashMap<>();
    ftlParamMap.put("visitdetails", patientDetailsMap);
    Preferences sessionPrefs = APIUtility.getPreferences();
    ftlParamMap.put("modules_activated", sessionPrefs.getModulesActivatedMap());
    List<BasicDynaBean> patientNotes = null;
    if (!StringUtils.isEmpty(noteTypeId)) {
      patientNotes = notesService.getPatientFinalNotes(patientId,
        Collections.singletonList(Integer.parseInt(noteTypeId)));
    }
    if (!StringUtils.isEmpty(hospitalRoleId)) {
      List<BasicDynaBean> noteTypeMasterByHospitalRole = noteTypesRepository
          .getNoteTypeMasterByRoleId(Collections.singletonList(
              Integer.parseInt(hospitalRoleId)));
      if (!noteTypeMasterByHospitalRole.isEmpty()) {
        List<Integer> noteTypeIds = new ArrayList<>();
        for (BasicDynaBean noteTypeMaster: noteTypeMasterByHospitalRole) {
          noteTypeIds.add((Integer) noteTypeMaster.get("note_type_id"));
        }
        patientNotes = notesService.getPatientFinalNotes(patientId, noteTypeIds);
      }
    }
    if (patientNotes == null) {
      patientNotes = notesService.getPatientFinalNotes(
          patientId, Collections.<Integer>emptyList());
    }
    
    ftlParamMap.put("patientNotes", patientNotes);
    PrintTemplate printTemplate = PrintTemplate.PatientNotes;
    String templateContent = printTemplateService
        .getCustomizedTemplate(printTemplate);
    Template temp = null;
    if (templateContent != null && !templateContent.equals("")) {
      StringReader reader = new StringReader(templateContent);
      temp = new Template("PatientNotesReportTemplate.ftl", reader,
          AppInit.getFmConfig());
    } else {
      temp = cfg.getTemplate("PatientNotesReport.ftl");
    }
    StringWriter writer = new StringWriter();
    try {
      temp.process(ftlParamMap, writer);
    } catch (TemplateException exp) {
      log.info(exp.getMessage());
    }
    HtmlConverter hc = new HtmlConverter(new DoctorConsultImageRetriever());
    Boolean repeatPHeader = ((String) prefs.get(Constants.REPEAT_PATIENT_INFO))
        .equals(Constants.STRING_Y);
    if (enumType.equals(ReturnType.PDF)) {
      hc.writePdf(os, writer.toString(), PATIENT_NOTES, prefs, false,
          repeatPHeader, true, true, true, false);
      os.close();

    } else if (enumType.equals(ReturnType.PDF_BYTES)) {
      ByteArrayOutputStream stream = new ByteArrayOutputStream();
      hc.writePdf(stream, writer.toString(), PATIENT_NOTES, prefs, false,
          repeatPHeader, true, true, true, false);
      bytes = stream.toByteArray();
      stream.close();
    } else if (enumType.equals(ReturnType.TEXT_BYTES)) {
      bytes = hc.getText(writer.toString(), PATIENT_NOTES, prefs, true, true);
    }
    return bytes;
  }

  /**
   * Prints the physician orders.
   *
   * @param patientId the patient id
   * @param reqPrinterId the req printer id
   * @param logoHeader the logo header
   * @param requestMap the request map
   * @param response the response
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws XPathExpressionException the x path expression exception
   * @throws SQLException the SQL exception
   * @throws DocumentException the document exception
   * @throws TransformerException the transformer exception
   */
  public void printPhysicianOrders(String patientId, Integer reqPrinterId,
      String logoHeader, Map<String, Object> requestMap,
      HttpServletResponse response)
      throws IOException, XPathExpressionException, SQLException,
      DocumentException, TransformerException {
    Map<String, Object> sessionAttributes = sessionService
        .getSessionAttributes();
    Integer centerId = (Integer) sessionAttributes.get("centerId");
    BasicDynaBean prefs = null;
    int printerId = 0;
    if ((reqPrinterId != null)) {
      printerId = reqPrinterId;
    }
    prefs = PrintConfigurationRepository.getPageOptions(PRINT_TYPE_PATIENT,
        printerId, centerId);
    String printMode = "P";
    if (prefs.get(PRINT_MODE) != null) {
      printMode = (String) prefs.get(PRINT_MODE);
    }

    if (printMode.equals("P")) {
      OutputStream os = response.getOutputStream();
      response.setContentType(Constants.CONTENT_TYPE_PDF);
      getPhysicianOrders(patientId, ReturnType.PDF, prefs, os, false);
      os.close();

    } else {
      String textReport = new String(getPhysicianOrders(patientId,
          ReturnType.TEXT_BYTES, prefs, null, true));
      requestMap.put(TEXT_REPORT, textReport);
      requestMap.put(TEXT_COLUMS, prefs.get(TEXT_MODE_COLUMNS));
      requestMap.put(PRINTER_TYPE, "DMP");
    }

  }

  /**
   * Gets the physician orders.
   *
   * @param patientId the patient id
   * @param enumType the enum type
   * @param prefs the prefs
   * @param os the os
   * @param bool the bool
   * @return the physician orders
   * @throws SQLException the SQL exception
   * @throws TemplateNotFoundException the template not found exception
   * @throws MalformedTemplateNameException the malformed template name exception
   * @throws ParseException the parse exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws XPathExpressionException the x path expression exception
   * @throws DocumentException the document exception
   * @throws TransformerException the transformer exception
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public byte[] getPhysicianOrders(String patientId, ReturnType enumType,
      BasicDynaBean prefs, OutputStream os, boolean bool)
      throws SQLException, TemplateNotFoundException,
      MalformedTemplateNameException, ParseException, IOException,
      XPathExpressionException, DocumentException, TransformerException {
    byte[] bytes = null;
    Map patientDetailsMap = new HashMap();
    genericDocumentsUtil.copyPatientDetails(patientDetailsMap, null, patientId,
        false);

    Map<String, Object> ftlParamMap = new HashMap<>();
    ftlParamMap.put("visitdetails", patientDetailsMap);
    Preferences sessionPrefs = APIUtility.getPreferences();
    ftlParamMap.put("modules_activated", sessionPrefs.getModulesActivatedMap());
    String bedType = (String) patientDetailsMap.get("alloc_bed_type");
    bedType = bedType == null || bedType.equals("")
        ? (String) patientDetailsMap.get("bill_bed_type") : bedType;
    bedType = bedType == null || bedType.equals("") ? "GENERAL" : bedType;

    String center = (String) patientDetailsMap.get(Constants.CENTER_ID);
    int centerId = Integer.parseInt(center);
    List<BasicDynaBean> allPrescriptions = prescriptionsService
        .getPhysicianOrdersForPrint(patientId, bedType,
            (String) patientDetailsMap.get(Constants.ORG_ID), centerId);
    ftlParamMap.put("orders", allPrescriptions);
    PrintTemplate printTemplate = PrintTemplate.DoctorOrder;
    String templateContent = printTemplateService
        .getCustomizedTemplate(printTemplate);
    Template temp = null;
    if (templateContent != null && !templateContent.equals("")) {
      StringReader reader = new StringReader(templateContent);
      temp = new Template("DoctorOrderPrintTemplate.ftl", reader,
          AppInit.getFmConfig());
    } else {
      temp = cfg.getTemplate("DoctorOrderPrint.ftl");
    }
    StringWriter writer = new StringWriter();
    try {
      temp.process(ftlParamMap, writer);
    } catch (TemplateException exp) {
      log.info(exp.getMessage());
    }
    HtmlConverter hc = new HtmlConverter(new DoctorConsultImageRetriever());
    Boolean repeatPHeader = ((String) prefs.get(Constants.REPEAT_PATIENT_INFO))
        .equals(Constants.STRING_Y);
    if (enumType.equals(ReturnType.PDF)) {
      hc.writePdf(os, writer.toString(), PHYSICIAN_ORDER, prefs, false,
          repeatPHeader, true, true, true, false);
      os.close();

    } else if (enumType.equals(ReturnType.PDF_BYTES)) {
      ByteArrayOutputStream stream = new ByteArrayOutputStream();
      hc.writePdf(stream, writer.toString(), PHYSICIAN_ORDER, prefs, false,
          repeatPHeader, true, true, true, false);
      bytes = stream.toByteArray();
      stream.close();
    } else if (enumType.equals(ReturnType.TEXT_BYTES)) {
      bytes = hc.getText(writer.toString(), PHYSICIAN_ORDER, prefs, true, true);
    }
    return bytes;
  }

  /**
   * Prints the vitals chart.
   *
   * @param patientId the patient id
   * @param reqPrinterId the req printer id
   * @param logoHeader the logo header
   * @param requestMap the request map
   * @param response the response
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws XPathExpressionException the x path expression exception
   * @throws SQLException the SQL exception
   * @throws DocumentException the document exception
   * @throws TransformerException the transformer exception
   */
  public void printVitalsChart(String patientId, Integer reqPrinterId,
      String logoHeader, Map<String, Object> requestMap,
      HttpServletResponse response)
      throws IOException, XPathExpressionException, SQLException,
      DocumentException, TransformerException {
    Map<String, Object> sessionAttributes = sessionService
        .getSessionAttributes();
    Integer centerId = (Integer) sessionAttributes.get("centerId");
    BasicDynaBean prefs = null;
    int printerId = 0;
    if ((reqPrinterId != null)) {
      printerId = reqPrinterId;
    }
    prefs = PrintConfigurationRepository.getPageOptions(PRINT_TYPE_PATIENT,
        printerId, centerId);
    String printMode = "P";
    if (prefs.get(PRINT_MODE) != null) {
      printMode = (String) prefs.get(PRINT_MODE);
    }

    if (printMode.equals("P")) {
      OutputStream os = response.getOutputStream();
      response.setContentType(Constants.CONTENT_TYPE_PDF);
      getVitalsChartReport(patientId, ReturnType.PDF, prefs, os, false);
      os.close();

    } else {
      String textReport = new String(getVitalsChartReport(patientId,
          ReturnType.TEXT_BYTES, prefs, null, true));
      requestMap.put(TEXT_REPORT, textReport);
      requestMap.put(TEXT_COLUMS, prefs.get(TEXT_MODE_COLUMNS));
      requestMap.put(PRINTER_TYPE, "DMP");
    }

  }

  /**
   * Gets the vitals chart report.
   *
   * @param patientId the patient id
   * @param enumType the enum type
   * @param prefs the prefs
   * @param os the os
   * @param bool the bool
   * @return the vitals chart report
   * @throws SQLException the SQL exception
   * @throws TemplateNotFoundException the template not found exception
   * @throws MalformedTemplateNameException the malformed template name exception
   * @throws ParseException the parse exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws XPathExpressionException the x path expression exception
   * @throws DocumentException the document exception
   * @throws TransformerException the transformer exception
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public byte[] getVitalsChartReport(String patientId, ReturnType enumType,
      BasicDynaBean prefs, OutputStream os, boolean bool)
      throws SQLException, TemplateNotFoundException,
      MalformedTemplateNameException, ParseException, IOException,
      XPathExpressionException, DocumentException, TransformerException {
    byte[] bytes = null;
    Map patientDetailsMap = new HashMap();
    genericDocumentsUtil.copyPatientDetails(patientDetailsMap, null, patientId,
        false);

    Map<String, Object> ftlParamMap = new HashMap<>();
    ftlParamMap.put("visitdetails", patientDetailsMap);
    Preferences sessionPrefs = APIUtility.getPreferences();
    ftlParamMap.put("modules_activated", sessionPrefs.getModulesActivatedMap());
    List<BasicDynaBean> vitalParams =
        vitalParameterService.getUniqueVitalsforPatient(patientId);
    if (vitalParams == null || vitalParams.isEmpty()) {
      vitalParams = vitalParameterService.getActiveVitalParams("I");
    }
    ftlParamMap.put(Constants.VITAL_PARAMS, vitalParams);
    ftlParamMap.put(Constants.VITALS, genericVitalFormService.groupByReadingId(patientId, "V"));
    PrintTemplate printTemplate = PrintTemplate.VitalsChart;
    String templateContent = printTemplateService.getCustomizedTemplate(printTemplate);
    Template temp = null;
    if (templateContent != null && !templateContent.equals("")) {
      StringReader reader = new StringReader(templateContent);
      temp = new Template("VitalsChartReportTemplate.ftl", reader,
          AppInit.getFmConfig());
    } else {
      temp = cfg.getTemplate("VitalsChartReport.ftl");
    }
    StringWriter writer = new StringWriter();
    try {
      temp.process(ftlParamMap, writer);
    } catch (TemplateException exp) {
      log.info(exp.getMessage());
    }
    HtmlConverter hc = new HtmlConverter(new DoctorConsultImageRetriever());
    Boolean repeatPHeader = ((String) prefs.get(Constants.REPEAT_PATIENT_INFO))
        .equals(Constants.STRING_Y);
    if (enumType.equals(ReturnType.PDF)) {
      hc.writePdf(os, writer.toString(), VITAL_CHARTS, prefs, false,
          repeatPHeader, true, true, true, false);
      os.close();

    } else if (enumType.equals(ReturnType.PDF_BYTES)) {
      ByteArrayOutputStream stream = new ByteArrayOutputStream();
      hc.writePdf(stream, writer.toString(), VITAL_CHARTS, prefs, false,
          repeatPHeader, true, true, true, false);
      bytes = stream.toByteArray();
      stream.close();
    } else if (enumType.equals(ReturnType.TEXT_BYTES)) {
      bytes = hc.getText(writer.toString(), PATIENT_NOTES, prefs, true, true);
    }
    return bytes;
  }
}
