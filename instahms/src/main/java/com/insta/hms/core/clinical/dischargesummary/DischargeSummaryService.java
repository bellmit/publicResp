package com.insta.hms.core.clinical.dischargesummary;

import com.bob.hms.common.APIUtility;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.Preferences;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DateHelper;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.ftl.FtlReportGenerator;
import com.insta.hms.common.modulesactivated.ModulesActivatedService;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.clinical.allergies.AllergiesService;
import com.insta.hms.core.clinical.careteam.CareTeamService;
import com.insta.hms.core.clinical.consultation.SecondaryComplaintService;
import com.insta.hms.core.clinical.forms.ClinicalFormHl7Adapter;
import com.insta.hms.core.clinical.healthmaintenance.HealthMaintenanceService;
import com.insta.hms.core.clinical.instaforms.ConsultationFormsService;
import com.insta.hms.core.clinical.instaforms.OTFormsService;
import com.insta.hms.core.clinical.instaforms.PatientSectionDetailsService;
import com.insta.hms.core.clinical.operationdetails.OperationAnaesthesiaService;
import com.insta.hms.core.clinical.operationdetails.OperationDetailsService;
import com.insta.hms.core.clinical.order.operationitems.OperationOrderItemService;
import com.insta.hms.core.clinical.order.serviceitems.ServiceOrderItemService;
import com.insta.hms.core.clinical.order.testitems.TestOrderItemService;
import com.insta.hms.core.clinical.outpatient.DoctorConsultationImageService;
import com.insta.hms.core.clinical.outpatient.DoctorConsultationService;
import com.insta.hms.core.clinical.prescriptions.PatientConsultationPrescriptionsService;
import com.insta.hms.core.clinical.prescriptions.PatientMedicinePrescriptionsService;
import com.insta.hms.core.clinical.prescriptions.PatientOperationPrescriptionsService;
import com.insta.hms.core.clinical.prescriptions.PatientOtherMedicinePrescriptionsService;
import com.insta.hms.core.clinical.prescriptions.PatientOtherPrescriptionsService;
import com.insta.hms.core.clinical.prescriptions.PatientServicePrescriptionsService;
import com.insta.hms.core.clinical.prescriptions.PatientTestPrescriptionsService;
import com.insta.hms.core.clinical.vitalforms.VitalReadingService;
import com.insta.hms.core.medicalrecords.MRDCaseFileIndentService;
import com.insta.hms.core.medicalrecords.MRDDiagnosisService;
import com.insta.hms.core.patient.followupdetails.FollowUpService;
import com.insta.hms.core.patient.inpatientlist.InPatientSearchService;
import com.insta.hms.core.patient.registration.RegistrationService;
import com.insta.hms.core.wardassignment.UserWardAssignmentService;
import com.insta.hms.documents.DocPrintConfigurationRepository;
import com.insta.hms.documents.GenericDocumentsUtil;
import com.insta.hms.documents.PatientDocumentService;
import com.insta.hms.exception.NestableValidationException;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.forms.PatientFormDetailsRepository;
import com.insta.hms.mdm.dischargesummarytemplates.DischargeSummaryTemplateService;
import com.insta.hms.mdm.doctors.DoctorService;
import com.insta.hms.mdm.genericimages.GenericImageService;
import com.insta.hms.mdm.hospitalcenters.HospitalCenterService;
import com.insta.hms.mdm.patientgeneralimages.PatientGeneralImageService;
import com.insta.hms.mdm.printtemplates.PrintTemplate;
import com.insta.hms.mdm.printtemplates.PrintTemplateService;
import com.insta.hms.mdm.sections.SectionsService;
import com.insta.hms.mdm.vitalparameters.VitalParameterService;
import com.insta.hms.security.usermanager.UserService;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import jlibs.core.util.regex.TemplateMatcher;
import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author anup vishwas.
 *
 */

@org.springframework.stereotype.Service
public class DischargeSummaryService {

  private static Logger logger = LoggerFactory.getLogger(DischargeSummaryService.class);

  @LazyAutowired
  private DischargeSummaryRepository dischargeSummaryRepository;
  @LazyAutowired
  private DischargeSummaryTemplateService dischargeSummaryTemplateService;
  @LazyAutowired
  private TestOrderItemService testOrderItemService;
  @LazyAutowired
  ServiceOrderItemService serviceOrderItemService;
  @LazyAutowired
  private GenericDocumentsUtil genericDocUtil;
  @LazyAutowired
  private GenericPreferencesService genPrefService;
  @LazyAutowired
  private VitalParameterService vitalParameterService;
  @LazyAutowired
  private PrintTemplateService printTemplateService;
  @LazyAutowired
  private RegistrationService registrationService;
  @LazyAutowired
  private FollowUpService followUpService;
  @LazyAutowired
  private OperationOrderItemService operationOrderItemService;
  @LazyAutowired
  private DoctorConsultationService doctorConsultationService;
  @LazyAutowired
  private PatientMedicinePrescriptionsService patientMedicinePrescService;
  @LazyAutowired
  private PatientOtherMedicinePrescriptionsService patientOtherMedicinePrescService;
  @LazyAutowired
  private PatientTestPrescriptionsService patientTestPrescService;
  @LazyAutowired
  private PatientServicePrescriptionsService patientServicePrescService;
  @LazyAutowired
  private PatientConsultationPrescriptionsService patientConsultationPrescService;
  @LazyAutowired
  private PatientOtherPrescriptionsService patientOtherPrescService;
  @LazyAutowired
  private PatientOperationPrescriptionsService patientOperationPrescService;
  @LazyAutowired
  private MRDDiagnosisService mrdDiagnosisService;
  @LazyAutowired
  private VitalReadingService vitalReadingService;
  @LazyAutowired
  private DoctorConsultationImageService doctorConsultationImageService;
  @LazyAutowired
  private HealthMaintenanceService healthMaintenanceService;
  @LazyAutowired
  private UserService userService;
  @LazyAutowired
  private AllergiesService allergiesService;
  @LazyAutowired
  private SecondaryComplaintService secondaryComplaintService;
  @LazyAutowired
  private PatientSectionDetailsService patientSectionDetailsService;
  @LazyAutowired
  private OperationDetailsService operationDetailsService;
  @LazyAutowired
  private OperationAnaesthesiaService operationAnaesthesiaService;
  @LazyAutowired
  private ConsultationFormsService consultationFormsService;
  @LazyAutowired
  private OTFormsService otFormsService;
  @LazyAutowired
  private PatientDocumentService patientDocumentService;
  @LazyAutowired
  private DischargeFormatDetailRepository dischargeFormatDetailRepo;
  @LazyAutowired
  private SessionService sessionService;
  @LazyAutowired
  private MRDCaseFileIndentService mrdCaseFileIndentService;
  @LazyAutowired
  private DischargeHeaderRepository dischargeHeaderRepo;
  @LazyAutowired
  private DischargeDetailRepository dischargeDetailRepo;
  @LazyAutowired
  private DoctorService doctorService;
  @LazyAutowired
  private SectionsService sectionsService;
  @LazyAutowired
  private HospitalCenterService hospitalCenterService;
  @LazyAutowired
  private DischargeSummaryValidator dischargeSummaryValidator;
  @LazyAutowired
  private InPatientSearchService inPatientSearchService;
  @LazyAutowired
  private CareTeamService careTeamService;
  @LazyAutowired
  private UserWardAssignmentService userWardAssignmentService;
  @LazyAutowired
  private PatientGeneralImageService patientGeneralImageService;
  @LazyAutowired
  private GenericImageService genericImageService;
  @LazyAutowired
  private PatientFormDetailsRepository patientFormDetailsRepository;
  @LazyAutowired
  private ClinicalFormHl7Adapter clinicalFormHl7Adapter;
  @LazyAutowired
  private DischargeSummaryPrintService dischargeSummaryPrintService;
  @LazyAutowired
  private ModulesActivatedService modulesActivatedService;

  /**
   * Gets all visit details.
   * 
   * @return map
   */
  public Map<String, Object> getAllVisitDetails() {

    HttpServletRequest request = RequestContext.getHttpRequest();
    HttpSession session = (HttpSession) request.getSession(false);
    Integer roleId = (Integer) session.getAttribute("roleId");
    Integer centerId = (Integer) session.getAttribute("centerId");
    Map actionRightsMap = (Map) session.getAttribute("actionRightsMap");
    String mrNo = request.getParameter("mr_no");
    String rightsForInActiveVisit = "Y";
    List<BasicDynaBean> list = new ArrayList<BasicDynaBean>();
    Map<String, Object> defaultFilterMap = inPatientSearchService.getDefaultFilterData();
    boolean isDoctorLogin = (Boolean) defaultFilterMap.get("is_doctor_login");
    boolean applyNurseRules = (Boolean) defaultFilterMap.get("applyNurseRules");

    List<BasicDynaBean> careTeamOrNurseVisitList = null;
    if (isDoctorLogin) {
      String doctorId = (String) defaultFilterMap.get("doctor_id");
      careTeamOrNurseVisitList = careTeamService.careTeamVisitList(mrNo, doctorId);
    } else if (applyNurseRules) {
      String loggedUserName = (String) defaultFilterMap.get("user_name");
      careTeamOrNurseVisitList =
          userWardAssignmentService.nurseWardAssignmentVisitList(mrNo, loggedUserName);
    }
    List<BasicDynaBean> visitDetailsList =
        dischargeSummaryRepository.getAllVisitDetailsList(mrNo, centerId);
    for (int i = 0; i < visitDetailsList.size(); i++) {
      String lastUpdatedBy = "";
      String reportStatus = "Open";
      String loggedInDocOrNurseVisit = "N";
      BasicDynaBean visitDetailsBean = visitDetailsList.get(i);
      String dischargeFinalizedUser = (String) visitDetailsBean.get("discharge_finalized_user");
      String patientId = (String) visitDetailsBean.get("patient_id");
      if (dischargeFinalizedUser != null && !dischargeFinalizedUser.isEmpty()) {
        reportStatus = "Finalized";
      }
      Integer docId = (Integer) visitDetailsBean.get("discharge_doc_id");
      if (null != docId && docId != 0) {
        String format = (String) visitDetailsBean.get("discharge_format");
        if (format.equals("F")) {
          BasicDynaBean disform = dischargeHeaderRepo.getDocForm(docId);
          lastUpdatedBy = (String) disform.get("username");
        } else if (format.equals("T")) {
          BasicDynaBean report = dischargeFormatDetailRepo.getDocumentReport(docId).get(0);
          lastUpdatedBy = (String) report.get("username");
        }
      } else if (null != docId && docId == 0) {
        visitDetailsBean.set("discharge_doc_id", null);
      }
      // default the discharge doctor to Admitting Doc/Consultation Doc
      String disDocId = (String) visitDetailsBean.get("discharge_doctor_id");
      if (disDocId == null || disDocId.equals("")) {
        visitDetailsBean.set("discharge_doctor_id", visitDetailsBean.get("doctor_id"));
        visitDetailsBean.set("discharge_doctor_name", visitDetailsBean.get("doctor_name"));
      }
      // default it to current date/time, if not there in database
      if (visitDetailsBean.get("discharge_date") == null
          || visitDetailsBean.get("discharge_date").equals("")) {
        visitDetailsBean.set("discharge_date", DateUtil.getCurrentDate());
        visitDetailsBean.set("discharge_time", DateUtil.getCurrentTime());
      }
      // Used for multi visit and if a loggedin doc/nurse is not assigned with particular visit
      if (careTeamOrNurseVisitList == null || !visitDetailsBean.get("visit_type").equals("i")) {
        loggedInDocOrNurseVisit = "Y";
      } else if (careTeamOrNurseVisitList != null) {
        for (int j = 0; j < careTeamOrNurseVisitList.size(); j++) {
          BasicDynaBean bean = careTeamOrNurseVisitList.get(j);
          if (bean.get("patient_id").equals(patientId)) {
            loggedInDocOrNurseVisit = "Y";
          }
        }
      }
      visitDetailsBean.set("last_updated_by", lastUpdatedBy);
      visitDetailsBean.set("report_status", reportStatus);
      visitDetailsBean.set("loggedin_docornurse_visit", loggedInDocOrNurseVisit);
      list.add(visitDetailsBean);
    }

    if (roleId != 1 && roleId != 2
        && (actionRightsMap != null && actionRightsMap.get("dishcharge_close") != null
            && actionRightsMap.get("dishcharge_close").equals("N"))) {
      rightsForInActiveVisit = "N";
    }
    String loggedInDoctorId = (String) defaultFilterMap.get("doctor_id");
    Map<String, Object> filterMap = new HashMap<>();
    filterMap.put("doctor_id", loggedInDoctorId);
    String loggedInDoc = "";
    if (loggedInDoctorId != null && !loggedInDoctorId.equals("")) {
      loggedInDoc = (String) doctorService.findByPk(filterMap).get("doctor_name");
    }
    Map<String, Object> visitDetailsMap = new HashMap<String, Object>();
    visitDetailsMap.put("visit_details", ConversionUtils.listBeanToListMap(list));
    visitDetailsMap.put("logged_in_doctor", loggedInDoc);
    visitDetailsMap.put("logged_in_doctor_id", loggedInDoctorId);
    visitDetailsMap.put("rights_for_inactive_visit", rightsForInActiveVisit);
    int maxCenterIncDefault =
        (Integer) genPrefService.getAllPreferences().get("max_centers_inc_default");
    visitDetailsMap.put("is_multicenter_schema", maxCenterIncDefault > 1);
    return visitDetailsMap;
  }

  /**
   * Get all templates.
   * 
   * @return map
   */
  public Map<String, Object> getAllTemplates() {

    Map<String, Object> dischargeTemplateMap = new HashMap<String, Object>();
    dischargeTemplateMap.put("templates",
        ConversionUtils.listBeanToListMap(dischargeSummaryTemplateService.getAllActiveTemplates()));
    return dischargeTemplateMap;
  }

  /**
   * Gets template content.
   * 
   * @return map
   * @throws NumberFormatException the exception
   * @throws IOException the exception
   * @throws TemplateException the exception
   * @throws SQLException the exception
   */
  // IN Progress
  public Map<String, Object> getTemplateContent()
      throws NumberFormatException, IOException, TemplateException, SQLException {

    HttpServletRequest request = RequestContext.getHttpRequest();
    Map<String, Object> templateContentMap = new HashMap<String, Object>();
    String userName = (String) request.getSession(false).getAttribute("userId");
    String format = request.getParameter("discharge_format");
    String docId = request.getParameter("discharge_doc_id");
    String formatId = request.getParameter("format_id");
    String patientId = request.getParameter("patient_id");
    List<BasicDynaBean> templateBeanList = null;
    String processedTemplateContent = null;

    Pattern treatmentSheetPattern =
        Pattern.compile(".*\\$\\s*\\{\\s*treatmentSheet\\s*!?\\s*\\}.*");
    Pattern consulatationDetailsPattern =
        Pattern.compile(".*\\$\\s*\\{\\s*consultationDetails\\s*!?\\s*\\}.*");
    Pattern otDetailsPattern = Pattern.compile(".*\\$\\s*\\{\\s*otDetails\\s*!?\\s*\\}.");

    if (format.equals("T")) {
      if (docId != null) {
        templateBeanList = dischargeFormatDetailRepo.getDocumentReport(Integer.parseInt(docId));
        templateContentMap.put("template_content",
            ConversionUtils.listBeanToListMap(templateBeanList));
      } else {
        templateBeanList = dischargeSummaryRepository.getDefaultReport(formatId);
        String templateContent = (String) templateBeanList.get(0).get("report_file");
        String templateTitle = (String) templateBeanList.get(0).get("template_title");
        String templateName = (String) templateBeanList.get(0).get("template_name");
        // TO DO : need to change this checker
        if (treatmentSheetPattern.matcher(templateContent).find()
            || consulatationDetailsPattern.matcher(templateContent).find()
            || otDetailsPattern.matcher(templateContent).find()) {
          processedTemplateContent =
              replaceTags(templateContent, patientId, templateTitle, userName);
          Map<String, Object> map = new HashMap<String, Object>();
          map.put("report_file", processedTemplateContent);
          map.put("template_name", templateName);
          map.put("template_title", templateTitle);
          List<Map<String, Object>> listMap = new ArrayList<Map<String, Object>>();
          listMap.add(map);
          templateContentMap.put("template_content", listMap);
        } else {
          templateContentMap.put("template_content",
              ConversionUtils.listBeanToListMap(templateBeanList));
        }
      }
    } else if (format.equals("F")) {
      if (docId != null) {
        templateBeanList = dischargeDetailRepo.getFormFieldsValues(Integer.parseInt(docId));
      } else {
        templateBeanList = dischargeSummaryRepository.getDefaultFormFieldValues(formatId);
      }
      templateContentMap.put("template_content",
          ConversionUtils.listBeanToListMap(templateBeanList));
    }

    List<BasicDynaBean> followUpDetailsList = followUpService.getfollowUpDetails(patientId);
    templateContentMap.put("followup_details",
        ConversionUtils.listBeanToListMap(followUpDetailsList));
    return templateContentMap;
  }

  /**
   * Gets dis value.
   * 
   * @return map
   */
  public Map<String, Object> getDischargeValueItems() {

    Map<String, Object> map = new HashMap<String, Object>();
    /*
     * map.put("ip_record", "Ip Record Sections"); map.put("diag_detail", "Diagnoses");
     * map.put("vital_detail", "Vitals");
     */
    map.put("inv_detail", "Investigations");
    /*
     * map.put("past_inv_detail", "Past Investigations"); map.put("doctor_note", "Doctor Notes");
     * map.put("medicine_detail", "Medicines"); map.put("service_detail", "Services");
     * map.put("operation_detail", "Operations"); map.put("ot_detail", "OT Sections");
     * map.put("dicharge_medication", "Discharge Medications"); map.put("generic_form",
     * "Patient Generic Forms");
     */

    return map;
  }

  /**
   * Gets selected item details.
   * 
   * @return the map
   */
  public Map<String, Object> getSelectedItemDetails() {

    HttpServletRequest request = RequestContext.getHttpRequest();
    Map<String, Object> map = new HashMap<String, Object>();
    String selectedItem = request.getParameter("selected_item");
    String patientId = request.getParameter("patient_id");
    if (selectedItem.equals("inv_detail")) {
      map.put("inv_detail",
          ConversionUtils.listBeanToListMap(testOrderItemService.getPrescribedTestList(patientId)));
    } else if (selectedItem.equals("service_detail")) {
      map.put("service_detail", ConversionUtils
          .listBeanToListMap(serviceOrderItemService.getPrescribedServiceList(patientId)));
    }
    return map;
  }

  /**
   * Process selected tokens.
   * 
   * @return map
   * @throws SQLException the exception
   * @throws IOException the exception
   * @throws TemplateException the exception
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public Map<String, Object> processSelectedTokens()
      throws SQLException, IOException, TemplateException {
    HttpServletRequest request = RequestContext.getHttpRequest();
    Map<String, Object> map = new HashMap<String, Object>();
    String itemKey = request.getParameter("itemKey");
    String[] itemvalue = null;
    if (itemKey.equals("inv_detail")) {
      itemvalue = request.getParameterValues("itemValue");
    }
    List<BasicDynaBean> testDetailsList = testOrderItemService.getSelectedTestDetails(itemvalue);
    Map testDetailsGroupMap =
        ConversionUtils.listBeanToMapListBean(testDetailsList, "prescribed_id");
    map.put("testDetailsGroupMap", testDetailsGroupMap);
    map.put("testDetailKeys", testDetailsGroupMap.keySet());
    String templateContent =
        printTemplateService.getCustomizedTemplate(PrintTemplate.Investigation);
    FtlReportGenerator ftlGen = null;
    if (templateContent == null || templateContent.equals("")) {
      ftlGen = new FtlReportGenerator(PrintTemplate.Investigation.getFtlName());
    } else {
      StringReader reader = new StringReader(templateContent);
      ftlGen = new FtlReportGenerator("InvestigationTemplate", reader);
    }
    StringWriter writer = new StringWriter();
    try {
      ftlGen.setReportParams(map);
      ftlGen.process(writer);
    } catch (TemplateException te) {
      logger.error("", te);
      throw te;
    }
    Map<String, Object> processedTokenMap = new HashMap<String, Object>();
    processedTokenMap.put("token_html", writer.toString());

    return processedTokenMap;
  }

  /**
   * Save and sign off.
   * 
   * @param params the param
   * @return map
   * @throws SQLException the exception
   * @throws ParseException the exception
   */
  @SuppressWarnings("rawtypes")
  @Transactional(rollbackFor = Exception.class)
  public Map<String, Object> saveAndSignoff(Map<String, Object> params)
      throws SQLException, ParseException {
    Map<String, Object> map = new HashMap<String, Object>();
    boolean success = false;
    Map<String, Object> visitDetailsParams = (Map<String, Object>) params.get("visit_details");
    Map<String, List<Map>> dischargeDetailParams =
        (Map<String, List<Map>>) params.get("document_details");
    String format = (String) getValue("discharge_format", visitDetailsParams);
    String mrNo = (String) getValue("mr_no", visitDetailsParams);

    validateDischargeSummaryDetails(params);

    if (format.equals("T")) {
      saveDischargeSummaryHtml(visitDetailsParams, dischargeDetailParams);
      if (dsExists((Integer) getValue("discharge_doc_id", visitDetailsParams), format)) {
        success = updatePatientRegistration(visitDetailsParams);
      }
      success = mrdCaseFileIndentService.setMRDCaseFileStatus(mrNo,
          MRDCaseFileIndentService.MRD_CASE_FILE_STATUS_ON_DISCHARGE);
      success = saveFollowUpDetails(visitDetailsParams, params);
    } else if (format.equals("F")) {
      saveDischargeSummaryHvf(visitDetailsParams, dischargeDetailParams);
      if (dsExists((Integer) getValue("discharge_doc_id", visitDetailsParams), format)) {
        success = updatePatientRegistration(visitDetailsParams);
      }
      success = mrdCaseFileIndentService.setMRDCaseFileStatus(mrNo,
          MRDCaseFileIndentService.MRD_CASE_FILE_STATUS_ON_DISCHARGE);
      success = saveFollowUpDetails(visitDetailsParams, params);
    }
    // returning doc id, useful for front end
    int dischargeDocId = (int) getValue("discharge_doc_id", visitDetailsParams);
    map.put("discharge_doc_id", dischargeDocId);

    // useful to change state once we save
    List<BasicDynaBean> followUpDetailsList =
        followUpService.getfollowUpDetails((String) getValue("patient_id", visitDetailsParams));
    map.put("followup_details", ConversionUtils.listBeanToListMap(followUpDetailsList));
    if (getValue("is_signoff", visitDetailsParams).equals("Y")) {
      //as format from visitDetails is of 'T' text we need 'P' PDF hence overriding
      clinicalFormHl7Adapter
          .dischargeSummarySaveAndFinaliseEvent(visitDetailsParams.get("patient_id").toString(),
              format, dischargeDocId);
    }
    return map;
  }

  /**
   * Save discharge summary.
   * 
   * @param visitDetailsParams the map
   * @param dischargeDetailParams the map
   * @return map
   */
  @SuppressWarnings("rawtypes")
  public Map<String, Object> saveDischargeSummaryHtml(Map<String, Object> visitDetailsParams,
      Map<String, List<Map>> dischargeDetailParams) {
    boolean success = false;
    List<Map> orderedItemList = dischargeDetailParams.get("richtext");
    BasicDynaBean bean = dischargeFormatDetailRepo.getBean();
    int docId = (Integer) getValue("discharge_doc_id", visitDetailsParams);
    Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
    String userName = (String) sessionAttributes.get("userId");
    for (int i = 0; i < orderedItemList.size(); i++) {
      Map map = orderedItemList.get(i);
      bean.set("report_file", map.get("report_file"));
      bean.set("username", userName);
      if (docId != 0) {
        Map<String, Object> keys = new HashMap<String, Object>();
        keys.put("docid", docId);
        success = dischargeFormatDetailRepo.update(bean, keys) > 0;
      } else {
        String formatId = (String) getValue("format_id", visitDetailsParams);
        docId = dischargeFormatDetailRepo.getNextSequence();
        BasicDynaBean disFormatBean = dischargeSummaryTemplateService.getDischargeFormat(formatId);
        bean.set("mr_no", getValue("mr_no", visitDetailsParams));
        bean.set("patient_id", getValue("patient_id", visitDetailsParams));
        bean.set("format_id", formatId);
        bean.set("pheader_template_id", disFormatBean.get("pheader_template_id"));
        bean.set("docid", docId);
        success = dischargeFormatDetailRepo.insert(bean) > 0;
      }

    }
    // putting newly generated doc id to use further for updating other related tables
    visitDetailsParams.put("discharge_doc_id", docId);
    // TO DO
    return null;
  }

  /**
   * Save discharge summary.
   * 
   * @param visitDetailsParams the map
   * @param dischargeDetailParams the map
   * @return map
   */
  public Map<String, Object> saveDischargeSummaryHvf(Map<String, Object> visitDetailsParams,
      Map<String, List<Map>> dischargeDetailParams) {
    boolean success = false;
    List<Map> hvfDetailsList = dischargeDetailParams.get("hvf");
    int docId = (Integer) getValue("discharge_doc_id", visitDetailsParams);
    BasicDynaBean disHeaderbean = dischargeHeaderRepo.getBean();
    Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
    String userName = (String) sessionAttributes.get("userId");
    disHeaderbean.set("username", userName);
    if (docId != 0) {
      Map<String, Object> keys = new HashMap<String, Object>();
      keys.put("docid", docId);
      success = dischargeHeaderRepo.update(disHeaderbean, keys) > 0;
      List<String> disDetailColumnList = new ArrayList<String>();
      disDetailColumnList.add("docdetail_id");
      disDetailColumnList.add("field_id");
      List<BasicDynaBean> disDetailList =
          dischargeDetailRepo.listAll(disDetailColumnList, "doc_id", docId);

      for (int i = 0; i < hvfDetailsList.size(); i++) {
        BasicDynaBean insertDisDetailBean = dischargeDetailRepo.getBean();
        boolean isDisDetailExist = false;
        Map map = hvfDetailsList.get(i);
        String fieldId = (String) map.get("field_id");
        String fieldValue = (String) map.get("field_value");
        for (int j = 0; j < disDetailList.size(); j++) {
          if (null != fieldId && fieldId.equals(disDetailList.get(j).get("field_id"))) {
            isDisDetailExist = true;
            break;
          }
        }
        // update or insert dis detail, possible if any new field is added in master and
        // it is been used for already saved discharge summary
        if (isDisDetailExist) {
          BasicDynaBean updateDisDetailBean = dischargeDetailRepo.getBean();
          Map<String, Object> disDetailkeys = new HashMap<String, Object>();
          disDetailkeys.put("doc_id", docId);
          disDetailkeys.put("field_id", fieldId);
          updateDisDetailBean.set("field_value", fieldValue);

          dischargeDetailRepo.update(updateDisDetailBean, disDetailkeys);
        } else {
          insertDisDetailBean.set("docdetail_id", 'D' + dischargeDetailRepo.getNextSequence());
          insertDisDetailBean.set("doc_id", docId);
          insertDisDetailBean.set("field_id", fieldId);
          insertDisDetailBean.set("field_value", fieldValue);

          dischargeDetailRepo.insert(insertDisDetailBean);
        }
      }

    } else {
      docId = dischargeHeaderRepo.getNextSequence();
      disHeaderbean.set("docid", docId);
      disHeaderbean.set("mr_no", getValue("mr_no", visitDetailsParams));
      disHeaderbean.set("patient_id", getValue("patient_id", visitDetailsParams));
      disHeaderbean.set("form_id", getValue("format_id", visitDetailsParams));
      // disHeaderbean.set("dis_date", ""); // TO DO : need to put current date
      success = dischargeHeaderRepo.insert(disHeaderbean) > 0;
      for (int i = 0; i < hvfDetailsList.size(); i++) {
        Map map = hvfDetailsList.get(i);
        BasicDynaBean disDetaisBean = dischargeDetailRepo.getBean();
        disDetaisBean.set("docdetail_id", "D" + dischargeDetailRepo.getNextSequence());
        disDetaisBean.set("doc_id", docId);
        disDetaisBean.set("field_id", map.get("field_id"));
        disDetaisBean.set("field_value", map.get("field_value"));

        dischargeDetailRepo.insert(disDetaisBean);
      }
      // putting newly generated doc id to use further for updating other related tables
      visitDetailsParams.put("discharge_doc_id", docId);
    }

    return null;
  }

  /**
   * Update patient reg.
   * 
   * @param visitDetailsParams the map
   * @return boolean value
   * @throws ParseException the exception
   */
  public boolean updatePatientRegistration(Map<String, Object> visitDetailsParams)
      throws ParseException {
    Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
    String userName = (String) sessionAttributes.get("userId");
    String doctorId = (String) getValue("discharge_doctor_id", visitDetailsParams);
    String allowSigUsageByOthers = "";
    String loggedInDoctorId =
        (String) userService.findByKey("emp_username", userName).get("doctor_id");
    BasicDynaBean regBean = registrationService.getBean();
    String format = (String) getValue("discharge_format", visitDetailsParams);
    regBean.set("discharge_format", format);
    regBean.set("discharge_doctor_id", doctorId);
    regBean.set("discharge_doc_id", getValue("discharge_doc_id", visitDetailsParams));
    regBean.set("user_name", userName);
    regBean.set("disch_date_for_disch_summary",
        new DateUtil().parseDate((String) getValue("discharge_date", visitDetailsParams)));
    regBean.set("disch_time_for_disch_summary",
        new DateUtil().parseTime((String) getValue("discharge_time", visitDetailsParams)));
    if (getValue("is_signoff", visitDetailsParams).equals("Y")) {
      regBean.set("discharge_finalized_user", userName);
      // TO DO : need to change to datehelper
      regBean.set("discharge_finalized_date", DateUtil.getCurrentDate());
      // TO DO : need to change to datehelper
      regBean.set("discharge_finalized_time", DateUtil.getCurrentTime());
    }
    // signatory user name applicable only for hvf template.
    if (format.equals("F")) {
      if (doctorId != null && !doctorId.equals("")) {
        allowSigUsageByOthers =
            (String) doctorService.getDoctorDetails(doctorId).get("allow_sig_usage_by_others");
        if (loggedInDoctorId != null && allowSigUsageByOthers.equals("Y")) {
          regBean.set("signatory_username", doctorId);
        }
      }
    }
    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("patient_id", getValue("patient_id", visitDetailsParams));
    boolean success = false;
    success = registrationService.update(regBean, keys) > 0;

    return success;
  }

  /**
   * Save follow up details.
   * 
   * @param visitDetailsParams the map
   * @param params the map
   * @return boolean value
   * @throws SQLException the exception
   * @throws ParseException the exception
   */
  // TO DO : need to check any condition needed to call this method
  public boolean saveFollowUpDetails(Map<String, Object> visitDetailsParams,
      Map<String, Object> params) throws SQLException, ParseException {
    boolean success = false;
    String followUpId = null;
    List<Map> followUpDetailsList = (List<Map>) params.get("followupdetails");
    for (int i = 0; i < followUpDetailsList.size(); i++) {
      Map map = followUpDetailsList.get(i);
      String isAddedDeletedModified = (String) map.get("followup_status");
      if (isAddedDeletedModified.equals("newadded")) {
        BasicDynaBean insertBean = followUpService.getBean();
        followUpId = followUpService.getNextFollowUpId();
        insertBean.set("followup_id", followUpId);
        insertBean.set("followup_doctor_id", map.get("followup_doctor_id"));
        insertBean.set("followup_date",
            new DateUtil().parseDate((String) map.get("followup_date")));
        insertBean.set("followup_remarks", map.get("followup_remarks"));
        insertBean.set("patient_id", visitDetailsParams.get("patient_id"));

        success = followUpService.insert(insertBean) > 0;
      } else if (isAddedDeletedModified.equals("deleted")) {
        followUpId = (String) map.get("followup_id");

        success = followUpService.delete("followup_id", followUpId) > 0;
      } else if (isAddedDeletedModified.equals("exists")) {
        Map<String, Object> keys = new HashMap<String, Object>();
        BasicDynaBean updateBean = followUpService.getBean();
        followUpId = (String) map.get("followup_id");
        keys.put("followup_id", followUpId);
        updateBean.set("followup_doctor_id", map.get("followup_doctor_id"));
        updateBean.set("followup_date",
            new DateUtil().parseDate((String) map.get("followup_date")));
        updateBean.set("followup_remarks", map.get("followup_remarks"));
        updateBean.set("patient_id", visitDetailsParams.get("patient_id"));

        success = followUpService.update(updateBean, keys) > 0;
      }
    }
    // need to check return type
    return success;
  }

  private void validateDischargeSummaryDetails(Map<String, Object> params) {
    Map<String, Object> nestedException = new HashMap<String, Object>();
    List<Map> followUpDetailsList = (List<Map>) params.get("followupdetails");
    // validate follow up deatails
    if (followUpDetailsList.size() > 0) {
      ValidationErrorMap validationErrors = new ValidationErrorMap();
      if (!dischargeSummaryValidator.validateFollowUpDetails(followUpDetailsList,
          validationErrors)) {
        ValidationException ex = new ValidationException(validationErrors);
        nestedException.put("followupDetails", ex.getErrors());
      }
    }
    if (!nestedException.isEmpty()) {
      throw new NestableValidationException(nestedException);
    }

  }

  /**
   * Do exists.
   * 
   * @param docId the int
   * @param format the string
   * @return boolean value
   */
  // TO DO : need to complete
  public boolean dsExists(int docId, String format) {
    if (format.equals("T")) {
      return dischargeFormatDetailRepo.findByKey("docid", docId) != null;
    } else if (format.equals("F")) {
      return dischargeHeaderRepo.findByKey("docid", docId) != null;
    } else if (format.equals("P")) {
      // TO DO: ned to implement
    } else if (format.equals("U")) {
      // TO DO: need to implement
    }
    return false;
  }

  // TO DO : need to check
  @SuppressWarnings("rawtypes")
  private Object getValue(String key, Map params, boolean sendNull) {
    Object obj = params.get(key);
    if (sendNull && obj == null) {
      return null;
    } else if (obj != null) {
      return obj;
    }
    return "";
  }

  @SuppressWarnings("rawtypes")
  private Object getValue(String key, Map params) {
    return getValue(key, params, false);
  }

  /**
   * Delete discharge summary.
   * 
   * @param params the param
   * @return map
   */
  @Transactional(rollbackFor = Exception.class)
  public Map<String, Object> deleteDischargeSummary(Map<String, Object> params) {
    Map<String, Object> disDocDetailParams =
        (Map<String, Object>) params.get("discharge_doc_details");
    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("patient_id", disDocDetailParams.get("patient_id"));
    BasicDynaBean regBean = registrationService.getBean();
    int docId = (Integer) disDocDetailParams.get("discharge_doc_id");
    String format = (String) disDocDetailParams.get("discharge_format");
    if (format.equals("F")) {
      dischargeHeaderRepo.delete("docid", docId);
      dischargeDetailRepo.delete("doc_id", docId);
    } else if (format.equals("T")) {
      dischargeFormatDetailRepo.delete("docid", docId);
    } else if (format.equals("U")) {
      // TO DO : need to implement

    } else if (format.equals("P")) {
      // TO DO : need to implement

    }
    regBean.set("discharge_doc_id", 0);
    regBean.set("disch_date_for_disch_summary", null);
    regBean.set("disch_time_for_disch_summary", null);
    registrationService.update(regBean, keys);


    // TO DO : need to check
    return null;
  }

  /**
   * Revert finalize discharge summary.
   * 
   * @param params the param
   * @return the map
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> revertFinalizedDischargeSummary(Map<String, Object> params) {
    Map<String, Object> keys = new HashMap<String, Object>();
    Map<String, Object> disDocDetailParams =
        (Map<String, Object>) params.get("discharge_doc_details");
    keys.put("patient_id", disDocDetailParams.get("patient_id"));
    BasicDynaBean regBean = registrationService.getBean();
    regBean.set("discharge_finalized_user", null);
    regBean.set("discharge_finalized_date", null);
    regBean.set("discharge_finalized_time", null);
    regBean.set("discharge_summary_reopened", true);
    boolean success = false;
    success = registrationService.update(regBean, keys) > 0;
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("discharge_doc_id", disDocDetailParams.get("discharge_doc_id"));
    return map;
  }

  /**
   * Retainig all existing logic to support existing tokens (treatmentSheet, consultationDetails,
   * otDetails).
   * 
   * @param templateContent the string
   * @param patientId the string
   * @param templateTitle the string
   * @param userName the string
   * @return string
   */
  @SuppressWarnings({"unchecked", "rawtypes", "unused"})
  public String replaceTags(String templateContent, String patientId, String templateTitle,
      String userName) {

    Map<String, String> map = new HashMap<String, String>();
    genericDocUtil.copyPatientDetails(map, null, patientId, false);
    if (map.get("discharge_date") == null) {
      map.put("discharge_date", DateHelper.parseTimeStamp("today"));
    }

    List operations = operationOrderItemService.getCompletedOperation(patientId);
    Template template = null;

    Map replaceFields = new HashMap(map);

    String operationStartDate = null;
    String operationEndDate = null;
    String operationStartTime = null;
    String operationEndTime = null;
    Pattern treatmentSheetPattern =
        Pattern.compile(".*\\$\\s*\\{\\s*treatmentSheet\\s*!?\\s*\\}.*");
    Pattern consulatationDetailsPattern =
        Pattern.compile(".*\\$\\s*\\{\\s*consultationDetails\\s*!?\\s*\\}.*");
    Pattern otDetailsPattern = Pattern.compile(".*\\$\\s*\\{\\s*otDetails\\s*!?\\s*\\}.*");

    if (operations != null && !operations.isEmpty()) {
      operationStartDate = (String) ((BasicDynaBean) operations.get(0)).get("operation_date");
      operationEndDate = (String) ((BasicDynaBean) operations.get(0)).get("operation_end_date");
      operationStartTime = (String) ((BasicDynaBean) operations.get(0)).get("starttime");
      operationEndTime = (String) ((BasicDynaBean) operations.get(0)).get("endtime");
    }
    replaceFields.put("operation_start_date", operationStartDate);
    replaceFields.put("operation_end_date", operationEndDate);
    replaceFields.put("operation_start_time", operationStartTime);
    replaceFields.put("operation_end_time", operationEndTime);

    try {
      if (treatmentSheetPattern.matcher(templateContent).find()) {
        String treatmentInfo = processTreatment(patientId);
        replaceFields.put("treatmentSheet", treatmentInfo);
      }
      if (consulatationDetailsPattern.matcher(templateContent).find()) {
        String consultationDetails = getConsultationDetails(patientId, userName);
        replaceFields.put("consultationDetails", consultationDetails);
      }
      if (otDetailsPattern.matcher(templateContent).find()) {
        String otDetails = getOTDetails(patientId);
        replaceFields.put("otDetails", otDetails);
      }

    } catch (Exception exception) {
      logger.error("", exception);
    }

    TemplateMatcher matcher = new TemplateMatcher("${", "}");
    templateContent = matcher.replace(templateContent, replaceFields);

    /*
     * Section 3: Report Data
     */
    StringBuilder html = new StringBuilder("");
    html.append("<div >");
    html.append("<table cellspacing='0' cellpadding='1' width='100%'><tbody>");
    html.append("<tr><td align='center'>" + templateTitle + "</td></tr>");
    html.append("<tr height='20'><td>&nbsp;</td></tr>");
    html.append("</tbody></table>\n");

    html.append("<table cellspacing='0' cellpadding='2'>").append("<tbody><tr><td>")
        .append(templateContent).append("</td></tr></tbody></table>").append("</div>\n");

    return html.toString();
  }

  /**
   * Process treatment.
   * 
   * @param patientId the visit id
   * @return string
   * @throws SQLException the exception
   * @throws IOException the exception
   * @throws TemplateException the exception
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  public String processTreatment(String patientId)
      throws SQLException, IOException, TemplateException {
    Map templateMap = new HashMap();
    templateMap.put("medicines",
        dischargeSummaryRepository.getMedicinesPrescribedAndSold(patientId));
    templateMap.put("tests", getTestValuesForPatient(patientId));
    templateMap.put("services", getServicesForPatient(patientId));
    templateMap.put("operation_presc",
        dischargeSummaryRepository.getOpetaionPrescriptionsCompleted(patientId));
    List<BasicDynaBean> operations = operationOrderItemService.getCompletedOperation(patientId);
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
          // TO DO : need to change for HVF
          // List fieldvalues = PatientHVFDocValuesDAO.getHVFDocValues(docId, true);
          // op.hvfValues.addAll(fieldvalues);
        } else if (format.equals("doc_rich_templates")) {
          BasicDynaBean patientdocbean = patientDocumentService.findByKey(docId);
          String content = (String) patientdocbean.get("doc_content_text");
          op.setRichTextContent(content);
        }
      }
      operationDocuments.add(op);
    }
    templateMap.put("operation_documents", operationDocuments);

    List imageColumnList = new ArrayList();
    imageColumnList.add("image_id");
    imageColumnList.add("datetime");
    List patientConsultations = new ArrayList();
    boolean isModPharmacyActivated =  modulesActivatedService.isModuleActivated("mod_pharmacy");
    String modPharmacy = isModPharmacyActivated ? "Y" : "N";
    templateMap.put("vital_params", vitalParameterService.getActiveVitalParams("O"));
    List<BasicDynaBean> noOfConsultations = doctorConsultationService.listAll(patientId);
    for (BasicDynaBean consult : noOfConsultations) {
      if (consult.get("status").equals("U")) {
        continue;
      }
      int consultationId = (Integer) consult.get("consultation_id");
      Map consultationMap = new HashMap();
      if (modPharmacy.equals("Y")) {
        consultationMap.put("cw_medicines",
            patientMedicinePrescService.getPresMedForTreatmentSheet(consultationId));
      } else {
        consultationMap.put("cw_medicines",
            patientOtherMedicinePrescService.getOtherPrescribedMedicines(consultationId));
      }
      consultationMap.put("cw_tests",
          patientTestPrescService.getPrescTestsForTreatmentSheet(consultationId));
      consultationMap.put("cw_services",
          patientServicePrescService.getPresServicesForTreatmentSheet(consultationId));
      consultationMap.put("cw_crossConsultations",
          patientConsultationPrescService.getPrescConsultationForTreatmentSheet(consultationId));
      consultationMap.put("cw_vitals", Collections.EMPTY_LIST); // added for legacy support.
      consultationMap.put("cw_vital_values", groupByReadingId(patientId, "V"));
      consultationMap.put("cw_images", doctorConsultationImageService.listAll(imageColumnList,
          "consultation_id", consultationId));
      consultationMap.put("consultation_details",
          doctorConsultationService.getDoctorConsultDetails(consultationId));
      consultationMap.put("cw_hvf_fields",
          doctorConsultationService.getConsultationFieldsValues(consultationId, true, true));
      patientConsultations.add(consultationMap);
    }
    templateMap.put("patientConsultations", patientConsultations);
    templateMap.put("diagnosis_details", mrdDiagnosisService.getAllDiagnosisDetails(patientId));
    BasicDynaBean followupBean = followUpService.findByKey(patientId);
    if (followupBean != null) {
      templateMap.put("followup_date", followupBean.get("followup_date"));
    }

    BasicDynaBean visitBean = registrationService.findByKey(patientId);
    templateMap.put("complaint", (String) visitBean.get("complaint"));

    String templateContent =
        printTemplateService.getCustomizedTemplate(PrintTemplate.TreatmentSheet);
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
      logger.error("", te);
      throw te;
    }
    return writer.toString();

  }

  /**
   * Get cons details.
   * 
   * @param patientId the visit id
   * @param userName the username
   * @return string
   * @throws SQLException the exception
   * @throws IOException the exception
   * @throws TemplateException the exception
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  public String getConsultationDetails(String patientId, String userName)
      throws SQLException, IOException, TemplateException {

    List<Map> consultationsList = new ArrayList<Map>();
    Map pdetails = new HashMap();
    genericDocUtil.copyPatientDetails(pdetails, null, patientId, false);
    HttpServletRequest request = RequestContext.getHttpRequest();
    HttpSession session = (HttpSession) request.getSession(false);
    Integer centerId = (Integer) session.getAttribute("centerId");
    String helathAuthority =
        (String) hospitalCenterService.findByKey(centerId).get("health_authority");
    String orgId = (String) pdetails.get("org_id");
    orgId = orgId == null || orgId.equals("") ? "GENERAL" : orgId;
    String bedType = (String) pdetails.get("alloc_bed_type");
    bedType =
        bedType == null || bedType.equals("") ? (String) pdetails.get("bill_bed_type") : bedType;
    bedType = bedType == null || bedType.equals("") ? "GENERAL" : bedType;

    List<BasicDynaBean> consultations = doctorConsultationService.listVisitConsultations(patientId);
    for (BasicDynaBean bean : consultations) {
      int consultId = (Integer) bean.get("consultation_id");
      BasicDynaBean genericPrefs = genPrefService.getPreferences();
      String useStoreItems = (String) genericPrefs.get("prescription_uses_stores");

      Map ftlParamMap = new HashMap();
      if (useStoreItems.equals("Y")) {
        ftlParamMap.put("presMedicines", patientMedicinePrescService
            .getPrescMedicinesForConsultation(consultId, helathAuthority));
      } else {
        ftlParamMap.put("presMedicines",
            patientOtherPrescService.getPrescOtherMedicinesForConsultation(consultId));
      }
      ftlParamMap.put("presServices",
          patientServicePrescService.getPresServicesForConsultation(orgId, bedType, consultId));
      ftlParamMap.put("presTests",
          patientTestPrescService.getPrescTestsForConsultation(orgId, bedType, consultId));
      ftlParamMap.put("presConsultation",
          patientConsultationPrescService.getPrescConsultationsForConsultation(consultId));
      ftlParamMap.put("NonHospitalItems",
          patientOtherPrescService.getPrescOthersForConsultation(consultId));
      ftlParamMap.put("presOperations", patientOperationPrescService
          .getPrescOperationsForConsultation(consultId, orgId, bedType));
      BasicDynaBean consultBean = doctorConsultationService.getDoctorConsultDetails(consultId);
      ftlParamMap.put("consultation_bean", consultBean);

      Map params = new HashMap();
      params.put("consultation_id", new String[] {consultId + ""});
      BasicDynaBean compBean = consultationFormsService.getComponents(params);

      ftlParamMap.put("vitals", Collections.EMPTY_LIST);
      ftlParamMap.put("vital_values",
          groupByReadingId((String) consultBean.get("patient_id"), "V"));
      String itemType = (String) consultationFormsService.getKeys().get("item_type");
      ftlParamMap.put("health_maintenance",
          healthMaintenanceService.getAllHealthMaintenance((String) consultBean.get("mr_no"),
              patientId, consultId, 0, (Integer) compBean.get("form_id"), itemType));
      List consultationFields =
          doctorConsultationService.getConsultationFieldsValues(consultId, true, true);
      Map consultationFieldsMap =
          ConversionUtils.listBeanToMapBean(consultationFields, "field_name");
      ftlParamMap.put("consultationFields", consultationFields);
      ftlParamMap.put("noteTakerEnabled",
          userService.findByKey("emp_username", userName).get("prescription_note_taker"));
      List imageColumnList = new ArrayList();
      imageColumnList.add("image_id");
      imageColumnList.add("datetime");
      ftlParamMap.put("consultImages",
          doctorConsultationImageService.listAll(imageColumnList, "consultation_id", consultId));
      BasicDynaBean followupBean = followUpService.findByKey(patientId);
      if (followupBean != null) {
        ftlParamMap.put("followup_date", followupBean.get("followup_date"));
      }
      ftlParamMap.put("allergies",
          allergiesService.getAllActiveAllergies((String) consultBean.get("mr_no"),
              (String) consultBean.get("patient_id"), consultId, 0,
              (Integer) compBean.get("form_id"), itemType));
      ftlParamMap.put("secondary_complaints",
          secondaryComplaintService.getSecondaryComplaints(patientId));
      List<BasicDynaBean> consValues = patientSectionDetailsService.getAllSectionDetails(
          (String) consultBean.get("mr_no"), (String) consultBean.get("patient_id"), consultId, 0,
          (Integer) compBean.get("form_id"), itemType);
      Map<Object, List<List>> map =
          ConversionUtils.listBeanToMapListListBean(consValues, "section_title", "field_id");
      ftlParamMap.put("PhysicianForms", map);
      ftlParamMap.put("consultation_components", compBean.getMap());
      ftlParamMap.put("consult_phy_forms",
          sectionsService.getAddedSectionMasterDetails((String) consultBean.get("mr_no"),
              (String) consultBean.get("patient_id"), consultId, 0,
              (Integer) compBean.get("form_id"), itemType));

      consultationsList.add(ftlParamMap);

    }
    String templateContent =
        printTemplateService.getCustomizedTemplate(PrintTemplate.ConsultationDetails);
    FtlReportGenerator ftlGen = null;

    if (templateContent == null || templateContent.equals("")) {
      ftlGen = new FtlReportGenerator(PrintTemplate.ConsultationDetails.getFtlName());
    } else {
      StringReader reader = new StringReader(templateContent);
      ftlGen = new FtlReportGenerator("ConsultationDetailsPrintTemplate", reader);
    }
    Map templateParams = new HashMap();
    templateParams.put("consultations", consultationsList);
    templateParams.put("diagnosis_details", mrdDiagnosisService.getAllDiagnosisDetails(patientId));
    templateParams.put("visitdetails", pdetails);
    templateParams.put("vital_params", vitalParameterService.getActiveVitalParams("O"));

    StringWriter writer = new StringWriter();
    try {
      ftlGen.setReportParams(templateParams);
      ftlGen.process(writer);
    } catch (TemplateException te) {
      logger.error("", te);
      throw te;
    }
    return writer.toString();
  }

  /**
   * Gets OT Detaials.
   * 
   * @param patientId the visit id
   * @return string
   * @throws SQLException the exception
   * @throws IOException the exception
   * @throws TemplateException the exception
   * @throws Exception the exception
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  public String getOTDetails(String patientId)
      throws SQLException, IOException, TemplateException, Exception {

    List operationDetailsList = new ArrayList();
    operationDetailsList.add("operation_details_id");
    List<Map> operationsList = new ArrayList<Map>();
    List<BasicDynaBean> operationDetailsIds =
        operationDetailsService.listAll(operationDetailsList, "patient_id", patientId);
    Map pdetails = new HashMap();
    genericDocUtil.copyPatientDetails(pdetails, null, patientId, false);
    for (BasicDynaBean bean : operationDetailsIds) {
      Integer opDetailsId = (Integer) bean.get("operation_details_id");
      Map ftlParamMap = new HashMap();
      BasicDynaBean operationDetailsBean =
          operationDetailsService.getOperationDetailsForFTL(opDetailsId);
      ftlParamMap.put("surgery_details", operationDetailsBean);
      ftlParamMap.put("operation_team_details",
          operationDetailsService.getOperationTeam(opDetailsId));
      ftlParamMap.put("operation_anaethesia_details",
          operationAnaesthesiaService.getOperationAnaesthesiaDetails(opDetailsId));
      ftlParamMap.put("opeartionsList", operationDetailsService.getSurgeryListForFTL(opDetailsId));
      List opCompDetails = new ArrayList<BasicDynaBean>();
      List<BasicDynaBean> operationsProcIdsList =
          operationDetailsService.getOperations(patientId, opDetailsId);

      Map operationWiseCompValues = new HashMap<String, Map<Object, List<List>>>();
      Map<Integer, Integer> sysCompDetails = new HashMap<Integer, Integer>();
      Map<String, String> operationNames = new HashMap<String, String>();

      BasicDynaBean components = null;
      String itemType = (String) otFormsService.getKeys().get("item_type");
      for (BasicDynaBean operationIDS : operationsProcIdsList) {
        int operationProcId = (Integer) operationIDS.get("operation_proc_id");
        Map params = new HashMap();
        params.put("operation_proc_id", new String[] {operationProcId + ""});
        components = otFormsService.getComponents(params);

        opCompDetails.add(components);
        String formsIds = (String) components.get("sections");
        String[] formsIdsArray = formsIds.split(",");
        for (int i = 0; i < formsIdsArray.length; i++) {
          int formId = Integer.parseInt(formsIdsArray[i]);
          if (formId < 0) {
            sysCompDetails.put(formId, formId);
          }
        }
        List<BasicDynaBean> sectionValues = patientSectionDetailsService.getAllSectionDetails(
            (String) pdetails.get("mr_no"), (String) pdetails.get("patient_id"), operationProcId, 0,
            (Integer) components.get("form_id"), itemType);

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
    String templateContent = printTemplateService.getCustomizedTemplate(PrintTemplate.OTDetails);
    FtlReportGenerator ftlGen = null;

    if (templateContent == null || templateContent.equals("")) {
      ftlGen = new FtlReportGenerator(PrintTemplate.OTDetails.getFtlName());
    } else {
      StringReader reader = new StringReader(templateContent);
      ftlGen = new FtlReportGenerator("OTDetailsPrintTemplate", reader);
    }
    Map templateParams = new HashMap();
    templateParams.put("operations", operationsList);
    templateParams.put("diagnosis_details", mrdDiagnosisService.getAllDiagnosisDetails(patientId));
    templateParams.put("secondary_complaints",
        secondaryComplaintService.getSecondaryComplaints(patientId));
    templateParams.put("visitdetails", pdetails);
    StringWriter writer = new StringWriter();
    try {
      ftlGen.setReportParams(templateParams);
      ftlGen.process(writer);
    } catch (TemplateException te) {
      logger.error("", te);
      throw te;
    }
    return writer.toString();

  }

  /**
   * Gets test values for patient.
   * 
   * @param patientId the visit id
   * @return the list
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public List getTestValuesForPatient(String patientId) {
    List<BasicDynaBean> dynalist = dischargeSummaryRepository.getTestValuesForPatient(patientId);

    List<Test> testsList = new ArrayList<Test>();
    int prescribedID = 0;
    Test test = null;
    for (BasicDynaBean bean : dynalist) {
      if ((Integer) bean.get("prescribed_id") != prescribedID) {
        test = new Test((Integer) bean.get("prescribed_id"), (String) bean.get("test_name"),
            (String) bean.get("remarks"), (String) bean.get("conducted_in_reportformat"));
        testsList.add(test);
      }
      test.getTestValues().add(bean);
      prescribedID = (Integer) bean.get("prescribed_id");
    }
    return testsList;
  }

  /**
   * Gets services for patient.
   * 
   * @param patientId the visit id
   * @return list
   * @throws SQLException the exception
   */
  @SuppressWarnings("rawtypes")
  public List getServicesForPatient(String patientId) throws SQLException {
    List<BasicDynaBean> dynalist = serviceOrderItemService.getConductedServiceDetails(patientId);
    ArrayList<Service> serviceList = new ArrayList<Service>();
    for (BasicDynaBean b : dynalist) {
      Service service = new Service((String) b.get("service_name"), (String) b.get("remarks"));
      service.setServiceDetails(b);
      String format = (String) b.get("doc_format");
      service.setFormat(format);
      Integer docId = (Integer) b.get("doc_id");
      if (docId != null) {
        if (format.equals("doc_hvf_templates")) {
          // TO DO: need to check in details
          // List fieldvalues = PatientHVFDocValuesDAO.getHVFDocValues(doc_id, true);
          // service.getHvfValues().addAll(fieldvalues);
        } else if (format.equals("doc_rich_templates")) {
          BasicDynaBean patientdocbean = patientDocumentService.findByKey(docId);
          String content = (String) patientdocbean.get("doc_content_text");
          service.setRichTextContent(content);
        }
      }

      serviceList.add(service);
    }
    return serviceList;
  }

  /**
   * Group bys reading id.
   * 
   * @param patientId the visit id
   * @param paramContainer the param
   * @return list
   */
  @SuppressWarnings({"unchecked", "rawtypes", "unused"})
  public List groupByReadingId(String patientId, String paramContainer) {
    // multiple rows(single row for each parameter) per single single reading id
    String visit = null;
    List<BasicDynaBean> paramWiseReadingList =
        vitalReadingService.getVitalReadings(patientId, paramContainer);
    BasicDynaBean patBean = registrationService.findByKey(patientId);
    if (patBean != null) {
      visit = ((String) patBean.get("visit_type")).toUpperCase();
    }
    List<BasicDynaBean> paramList = vitalParameterService.getAllParams(paramContainer, visit);
    ArrayList readingWiseList = new ArrayList();
    ArrayList records = new ArrayList();
    int readingId = 0;
    Map labelmap = null;
    for (BasicDynaBean bean : paramWiseReadingList) {
      if (readingId != (Integer) bean.get("vital_reading_id")) {
        labelmap = new LinkedHashMap();
        for (BasicDynaBean param : paramList) {
          labelmap.put(param.get("param_label"), "");
        }
        readingWiseList.add(labelmap);
      }

      labelmap.put(bean.get("param_label"), bean.get("param_value"));
      labelmap.putAll(bean.getMap());
      readingId = (Integer) bean.get("vital_reading_id");

    }
    return readingWiseList;

  }

  /**
   * Gets generic image list.
   * 
   * @return the map
   */
  public Map<String, Object> getGenericImageList() {
    HttpServletRequest request = RequestContext.getHttpRequest();
    String mrNo = request.getParameter("mr_no");
    if (mrNo == null || mrNo.equals("")) {
      String patientId = request.getParameter("patient_id");
      if (patientId == null || patientId.equals("")) {
        return null;
      } else {
        BasicDynaBean patientRegBean = registrationService.findByKey(patientId);
        if (patientRegBean == null) {
          return null;
        } else {
          mrNo = patientRegBean.get("mr_no").toString();
        }
      }
    }

    List<String> pcolumns = new ArrayList<String>();
    pcolumns.add("image_id");
    pcolumns.add("mr_no");
    pcolumns.add("image_name");
    pcolumns.add("content_type");
    BasicDynaBean genericPref = genPrefService.getAllPreferences();
    String diaImgPref = (String) genericPref.get("diag_images");
    List<String> diagImages = new ArrayList<String>(Arrays.asList(diaImgPref.split("\\s*,\\s*")));
    List<BasicDynaBean> patientImagesList = null;
    if (diagImages.contains("P")) {
      patientImagesList = patientGeneralImageService.listAll(pcolumns, "mr_no", mrNo);
    }

    List<String> gcolumns = new ArrayList<String>();
    gcolumns.add("image_id");
    gcolumns.add("image_name");
    gcolumns.add("content_type");
    List<BasicDynaBean> genericImagesList = null;
    if (diagImages.contains("G")) {
      genericImagesList = genericImageService.listAll(gcolumns);
    }

    List<Map> allImages = new ArrayList<Map>();
    if (null != patientImagesList) {
      for (BasicDynaBean bean : patientImagesList) {
        Map map = new HashMap(bean.getMap());
        map.put("viewUrl",
            "/pages/GenericDocuments/PatientGeneralImageAction.do?_method=view&image_id="
                + bean.get("image_id"));
        allImages.add(map);
      }
    }
    if (null != genericImagesList) {
      for (BasicDynaBean bean : genericImagesList) {
        Map map = new HashMap(bean.getMap());
        map.put("viewUrl",
            "/master/GenericImageMaster.do?_method=view&image_id=" + bean.get("image_id"));

        allImages.add(map);
      }
    }
    Map<String, Object> imageMap = new HashMap<String, Object>();
    imageMap.put("generic_images", allImages);

    return imageMap;
  }

  /**
   * Get discharge summary form data byte array.
   * @param patientId the patient id.
   * @param format the discharge summary format.
   * @param dischargeDocId discharge document id.
   * @return Base64 encoded pdf byte array.
   */
  public String getFormDataEncodedByteArray(Object patientId, Object format,
      Object dischargeDocId) {
    String encodedBase64String = null;
    try {
      BasicDynaBean dischargeSummaryPrintPrefs =
          DocPrintConfigurationRepository.getDischargeSummaryPreferences(6);
      Preferences sessionPrefs = APIUtility.getPreferences();
      OutputStream os = new ByteArrayOutputStream();
      byte[] dischargeSummaryReport = dischargeSummaryPrintService
          .getDischargeSummaryReport((int) dischargeDocId, (String) patientId, (String) format,
              DischargeSummaryPrintService.ReturnType.PDF_BYTES, dischargeSummaryPrintPrefs,
              sessionPrefs, os);

      encodedBase64String = Base64.getEncoder().encodeToString(dischargeSummaryReport);
      os.close();
    } catch (Exception ex) {
      logger.error("Error while encoding Discharge Summary data: ", ex);
    }
    return encodedBase64String;
  }

  /**
   * get discharge summary form segment data for save and finalise event.
   * @param patientId patient id
   * @return the segment data map
   */
  public Map getFormSegmentInformation(Object patientId) {
    return dischargeSummaryRepository
        .getDischargeSummarySaveFinaliseEventData((String) patientId).getMap();
  }
}
