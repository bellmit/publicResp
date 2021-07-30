package com.insta.hms.medicalrecorddepartment;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.Preferences;
import com.bob.hms.common.PreferencesDao;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.Registration.PatientInsurancePlanDAO;
import com.insta.hms.Registration.PatientPolicyDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.billing.Bill;
import com.insta.hms.billing.BillBO;
import com.insta.hms.billing.BillDAO;
import com.insta.hms.ceed.CeedDAO;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.core.fa.AccountingJobScheduler;
import com.insta.hms.dischargesummary.DischargeSummaryDAOImpl;
import com.insta.hms.ipservices.PrescriptionsDAO;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.HealthAuthorityPreferences.HealthAuthorityPreferencesDAO;
import com.insta.hms.master.PrescriptionsPrintTemplates.PrescriptionsTemplateDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDAO;
import com.insta.hms.mdm.practitionerconsultationmapping.PractitionerConsultationMappingService;
import com.insta.hms.orders.OrderDAO;
import com.insta.hms.outpatient.DoctorConsultationDAO;
import com.insta.hms.vitalForm.genericVitalFormDAO;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * The Class MRDUpdateScreenAction.
 */
public class MRDUpdateScreenAction extends DispatchAction {

  static MRDUpdateScreenDAO dao = new MRDUpdateScreenDAO();
  static GenericDAO hospitalClaimDiagDao = new GenericDAO("hospital_claim_diagnosis");
  static MRDDiagnosisDAO mrdDiagnosisDao = new MRDDiagnosisDAO();
  static VisitDetailsDAO regDao = new VisitDetailsDAO();
  static JSONSerializer js = new JSONSerializer().exclude("class");
  static GenericDAO diagCodeDAO = new GenericDAO("mrd_diagnosis");
  static GenericDAO drConsulDAO = new GenericDAO("doctor_consultation");
  static GenericDAO trtCodeDAO = new GenericDAO("bill_charge");
  static GenericDAO encCodeDAO = new GenericDAO("patient_registration");
  static GenericDAO regDAO = new GenericDAO("patient_registration");
  static GenericDAO patientDAO = new GenericDAO("patient_details");
  static GenericDAO loincDAO = new GenericDAO("test_details");
  static GenericDAO obserDAO = new GenericDAO("mrd_observations");
  static GenericDAO billChgClaimDAO = new GenericDAO("bill_charge_claim");
  final GenericDAO userDao = new GenericDAO("u_user");
  static MRDUpdateScreenBO mrdupdateScreenBo = new MRDUpdateScreenBO();
  AccountingJobScheduler accountingJobScheduler = ApplicationContextProvider
      .getBean(AccountingJobScheduler.class);
  static PatientInsurancePlanDAO patientInsurancePlanDAO = new PatientInsurancePlanDAO();
  static PatientPolicyDAO patientPolicyDao = new PatientPolicyDAO();
  static GenericDAO transferHospitalsDao = new GenericDAO("transfer_hospitals");
  
  private static PractitionerConsultationMappingService practitionerConsultationMappingService = 
      (PractitionerConsultationMappingService) ApplicationContextProvider.getApplicationContext()
      .getBean("practitionerConsultationMappingService");
  
  private static MessageUtil messageUtil = 
      (MessageUtil) ApplicationContextProvider.getApplicationContext()
      .getBean("messageUtil");

  /**
   * Gets the MRD update screen.
   *
   * @param am the am
   * @param af the af
   * @param req the req
   * @param res the res
   * @return the MRD update screen
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getMRDUpdateScreen(ActionMapping am, ActionForm af, HttpServletRequest req,
      HttpServletResponse res) throws Exception {

    String patientId = req.getParameter("patient_id") != null ? req.getParameter("patient_id")
        : (String) req.getAttribute("patient_id");

    HttpSession session = (HttpSession) req.getSession(false);
    Integer centerId = (Integer) session.getAttribute("centerId");
    Integer patientCenterId = null;
    String mrNo = null;
    Integer reviewsCount = 0;
    if (patientId != null && !patientId.equals("")) {
      VisitDetailsDAO regDao = new VisitDetailsDAO();
      BasicDynaBean bean = regDao.getVisitByCenterIdWithConfidentialityCheck(patientId, centerId);
      if (bean == null) {
        // user entered visit id is not a valid patient id.
        FlashScope flash = FlashScope.getScope(req);
        flash.put("error", "No Patient with Id:" + patientId);
        ActionRedirect redirect = new ActionRedirect(am.findForward("getMRDUpdateDetails"));
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
        return redirect;
      } else {
        patientCenterId = (Integer) bean.get("center_id");
        mrNo = (String) bean.get("mr_no");
      }
    }

    /* At the start of codification flow. We copy codes into hospital_claim_diagnosis
     * to mark that claim xml diagnosis codes are to contain coder modified codes.
     * Coder modified values  will be present in hospital_claim_diagnosis table.
     */
    insertRecordsIntoHospDiagnosis(patientId);
    
    // Fetch the supported code type for Consultations category
    String[] consCodeTypes = HealthAuthorityPreferencesDAO
        .getHealthAuthorityPreferences(CenterMasterDAO.getHealthAuthorityForCenter(patientCenterId))
        .getConsultation_code_types();

    String consultationCodeTypes = "";
    if (consCodeTypes != null) {
      boolean first = true;
      for (String codeType : consCodeTypes) {
        if (!first) {
          consultationCodeTypes += ",";
        }
        consultationCodeTypes += codeType;
        first = false;
      }
    }

    req.setAttribute("consultationSupportedCodeTypes", consultationCodeTypes);
    req.setAttribute("referer", req.getAttribute("referer") != null ? req.getAttribute("referer")
        : req.getHeader("Referer"));
    List<BasicDynaBean> patientTrtCodes = mrdupdateScreenBo
        .getMrdTreatmentCodesList(patientId, null);
    List chargeObservationsList = new ArrayList();
    for (BasicDynaBean b : patientTrtCodes) {
      String chargeId = (String) b.get("charge_id");
      List chargeObsSubList = mrdupdateScreenBo.getMrdObservations(chargeId);
      if (chargeObsSubList != null && !chargeObsSubList.isEmpty()) {
        chargeObservationsList.addAll(chargeObsSubList);
      }
    }

    HashMap arrMRDConsultationDetails = PrescriptionsDAO.getMRDConsultationDetails(patientId);
    req.setAttribute("ConsultationDetails", arrMRDConsultationDetails);

    req.setAttribute("allowdConsulItemCodes", js.deepSerialize(ConversionUtils.listBeanToListMap(
        mrdupdateScreenBo.getAllowedItemCodesList(patientId, consultationCodeTypes))));

    List<BasicDynaBean> patientDrConsultn = mrdupdateScreenBo
        .getMrdDrConsultationList(patientId, null);
    List vitalObservationsList = new ArrayList();
    for (BasicDynaBean b : patientDrConsultn) {
      String chargeId = (String) b.get("charge_id");
      List<BasicDynaBean> chargeObsSubList = mrdupdateScreenBo.getMrdObservations(chargeId);
      if (chargeObsSubList != null && !chargeObsSubList.isEmpty()) {
        vitalObservationsList.addAll(chargeObsSubList);
      }
    }

    List<BasicDynaBean> patientDrgCodes = mrdupdateScreenBo.getMrdDrgCodesList(patientId);
    List drgObservationsList = new ArrayList();
    if (patientDrgCodes != null) {
      for (BasicDynaBean b : patientDrgCodes) {
        String chargeId = (String) b.get("drg_charge_id");
        List chargeObsSubList = mrdupdateScreenBo.getMrdObservations(chargeId);
        if (chargeObsSubList != null && !chargeObsSubList.isEmpty()) {
          drgObservationsList.addAll(chargeObsSubList);
        }
      }
    }
    
    Map<String, Object> keys = new HashMap<>();
    keys.put("patient_id", patientId);
    keys.put("priority", 1);
    
    BasicDynaBean patInsPlanBean = patientInsurancePlanDAO.findByKey(keys);
    
    String priorAuthId = null;
    Integer patPrimaryPolicyId = null;
    
    
    if (null != patInsPlanBean) {
      priorAuthId = (String) patInsPlanBean.get("prior_auth_id");
      patPrimaryPolicyId = (Integer) patInsPlanBean.get("patient_policy_id");
    }
    Map<String, Object> filterMap = new HashMap<>();
    filterMap.put("patient_policy_id", patPrimaryPolicyId);
    BasicDynaBean patPrimaryPolicyBean = patientPolicyDao.findByKey(filterMap);

    String priEligibilityRefNum = null;
    Integer priEligibilityAuthStatus = null;
    String priEligibilityAuthRemarks = null;

    if (null != patPrimaryPolicyBean) {
      priEligibilityRefNum = (String) patPrimaryPolicyBean.get("eligibility_reference_number");
      priEligibilityAuthStatus = (Integer) patPrimaryPolicyBean
        .get("eligibility_authorization_status");
      priEligibilityAuthRemarks = (String) patPrimaryPolicyBean
        .get("eligibility_authorization_remarks");
    }

    req.setAttribute("primary_prior_auth_id", priorAuthId == null ? "" : priorAuthId);
    req.setAttribute("pri_eligibility_reference_number", priEligibilityRefNum == null 
        ? "" : priEligibilityRefNum);
    req.setAttribute("pri_eligibility_authorization_status", priEligibilityAuthStatus == null 
        ? "" : priEligibilityAuthStatus);
    req.setAttribute("pri_eligibility_authorization_remarks", priEligibilityAuthRemarks == null 
        ? "" : priEligibilityAuthRemarks);

    boolean isPrimaryInsuranceCardAvailable  = PatientDetailsDAO
        .getCurrentPatientCardImage(patientId, null) != null;

    req.setAttribute("isPrimaryInsuranceCardAvailable", isPrimaryInsuranceCardAvailable);
    req.setAttribute("patient_id", patientId);
    String success = req.getParameter("success") != null ? req.getParameter("success")
        : (String) req.getAttribute("success");
    req.setAttribute("info", success);
    List patientDiagnosis = mrdupdateScreenBo.getMrdDiagnosisList(patientId, null);
    req.setAttribute("patientDiagnosis", patientDiagnosis);
    List docEnteredDiagnosis = mrdupdateScreenBo.getMrdDiagnosis(null, patientId, null);
    req.setAttribute("docEnteredDiagnosis", docEnteredDiagnosis);
    List salesBillList = mrdupdateScreenBo.getSalesBillUrlList(patientId);
    req.setAttribute("salesBillList", salesBillList);
    List prevDiagnosesList = mrdupdateScreenBo
        .getPrevMrdDiagnosisListForCurrentVisit(patientId, null);
    req.setAttribute("prevDiagnoses", prevDiagnosesList);
    req.setAttribute("patientDrConsultn", patientDrConsultn);
    req.setAttribute("patientVitalObservations",
        ConversionUtils.copyListDynaBeansToMap(vitalObservationsList));
    String secondarycount = mrdupdateScreenBo.diagnosisCodeCount(patientId);
    req.setAttribute("secondarycount", secondarycount);
    req.setAttribute("observationCodeTypeList",
        js.deepSerialize(mrdupdateScreenBo.getObservationListCodes()));
    req.setAttribute("regPref", RegistrationPreferencesDAO.getRegistrationPreferences());
    String healthAuthority = CenterMasterDAO.getHealthAuthorityForCenter(patientCenterId);
    req.setAttribute("diagCodeType", HealthAuthorityPreferencesDAO
        .getHealthAuthorityPreferences(healthAuthority).getDiagnosis_code_type());
    req.setAttribute("healthAuthorityPrefs", HealthAuthorityPreferencesDAO
        .getHealthAuthorityPreferences(null != healthAuthority ? healthAuthority : "HAAD"));
    req.setAttribute("patientTrtObservations",
        ConversionUtils.copyListDynaBeansToMap(chargeObservationsList));
    req.setAttribute("patientTrtCodes", ConversionUtils.copyListDynaBeansToMap(patientTrtCodes));
    req.setAttribute("patientEncCodes", mrdupdateScreenBo.getEncounterCodes(patientId));
    req.setAttribute("useDRG", VisitDetailsDAO.visitUsesDRG(patientId));
    req.setAttribute("patientDrgObservations",
        ConversionUtils.copyListDynaBeansToMap(drgObservationsList));
    req.setAttribute("visitTpaBillsCount", BillDAO.getVisitTpaBillsCountExcludePrimary(patientId));
    List patientLoIncCodes = mrdupdateScreenBo.getLionIncCodes(patientId, null);
    req.setAttribute("patientLoIncCodes",
        ConversionUtils.copyListDynaBeansToMap(patientLoIncCodes));
    List testResultsData = mrdupdateScreenBo.getTestsWithLioncTestResults(patientCenterId);
    req.setAttribute("testResultsData",
        js.serialize(ConversionUtils.copyListDynaBeansToMap(testResultsData)));
    req.setAttribute("consultations", DoctorConsultationDAO.getMRDVisitConsultations(patientId));
    req.setAttribute("mrdSupportedCodeTypes", js.deepSerialize(ConversionUtils
        .copyListDynaBeansToMap(new GenericDAO("mrd_supported_code_types").listAll())));
    req.setAttribute("usePerdiem", VisitDetailsDAO.visitUsesPerdiem(patientId));
    req.setAttribute("perdiemCode", mrdupdateScreenBo.getPerdiemCode(patientId));
    String drgChargeId = req.getParameter("drg_charge_id");
    req.setAttribute("drgChargeId", drgChargeId);

    Map drgMap = mrdupdateScreenBo.getDRGCode(patientId);
    if (null != drgMap && null != drgMap.get("drg_bill_no") && null == drgMap.get("drg_code")) {
      drgMap = mrdupdateScreenBo.getMARDRGCode(patientId);
    }
    req.setAttribute("drgCode", drgMap);

    BasicDynaBean printpref = PrintConfigurationsDAO
        .getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT);
    int printerId = (Integer) printpref.get("printer_id");
    req.setAttribute("consPrinterId", printerId);
    req.setAttribute("consPrintTemplates", PrescriptionsTemplateDAO.getTemplateNames());

    BasicDynaBean babyDetails = PatientDetailsDAO.getBabyDateOfBirtsAndSalutationDetails(mrNo,
        patientId);

    Boolean isBaby = false;
    if (babyDetails != null) {
      List<BasicDynaBean> genericVitalFormDetails = genericVitalFormDAO.getVitalReadings(patientId,
          null);
      for (BasicDynaBean vitalDetails : genericVitalFormDetails) {

        String paramLabel = (String) vitalDetails.get("param_label");
        String paramvalue = (String) vitalDetails.get("param_value");
        String paramuom = (String) vitalDetails.get("param_uom");
        // if weight value is
        // there in vital
        // form then setting
        // it as default
        if (paramLabel.equalsIgnoreCase("Weight") && !paramvalue.equals("")) {
          isBaby = true;
          req.setAttribute("weightValue", paramvalue);
          req.setAttribute("weightUom", paramuom);
          break;
        }
      }
    }
    req.setAttribute("isBaby", isBaby);

    req.setAttribute("isCustomFieldsExist",
        BillBO.isMandatoryCustomFieldsExistForPatient(patientId));
    req.setAttribute("isChildPatient", babyDetails != null);

    BasicDynaBean patientBean = regDAO.findByKey("patient_id",
        patientId);
    req.setAttribute("orgID",
        patientBean != null
            ? patientBean.get("org_id") == null || patientBean.get("org_id").equals("") ? "ORG0001"
                : patientBean.get("org_id")
            : "ORG0001");
    req.setAttribute("codification_status",
        patientBean != null ? patientBean.get("codification_status") : "");
    req.setAttribute("codification_remarks",
        patientBean == null || patientBean.get("codification_remarks") == null ? ""
            : patientBean.get("codification_remarks"));

    Preferences prefs = null;
    String modCeedIntegrationEnabled = null;
    try (Connection ceedcon = DataBaseUtil.getConnection()) {
      reviewsCount = dao.getOpenInprogressReviewsCount(ceedcon, patientId);
      List<BasicDynaBean> openBills = BillDAO.getVisitAllOpenBills(ceedcon, patientId);
      req.setAttribute("finalizeAll", openBills == null || openBills.isEmpty() ? "Y" : "N");

      PreferencesDao dao = new PreferencesDao(ceedcon);
      prefs = dao.getPreferences();
      Map modules = prefs.getModulesActivatedMap();
      modCeedIntegrationEnabled = (String) modules.get("mod_ceed_integration");
    }

    Map ceedResponse = null;
    Boolean ceedstatus = null;
    BasicDynaBean ceedbean = null;
    if (modCeedIntegrationEnabled != null && modCeedIntegrationEnabled.equals("Y")) {
      ceedResponse = ConversionUtils.listBeanToMapListMap(CeedDAO.getMrdResponseDetails(patientId),
          "charge_id");
      ceedbean = CeedDAO.checkIfMrdCeedCheckDone(patientId);
      ceedstatus = ceedbean != null;
    }

    req.setAttribute("ceedResponseMap", ceedResponse);
    req.setAttribute("ceedResponseMapJson", js.deepSerialize(ceedResponse));
    req.setAttribute("ceedstatus", js.deepSerialize(ceedstatus));
    req.setAttribute("ceedbean", ceedbean);
    req.setAttribute("reviewsCount", reviewsCount);
    
    BasicDynaBean dischargeDetails = new DischargeSummaryDAOImpl().getDischargeDetails(patientId);
    if (dischargeDetails != null) {
      req.setAttribute("dis", dischargeDetails.getMap());
    }
    if (patientBean != null) {
      req.setAttribute("opType", patientBean.get("op_type"));
      req.setAttribute("visitType", patientBean.get("visit_type"));
    }
    return am.findForward("getMRDUpdateScreen");
  }

  /**
   * Days between.
   *
   * @param d1 the d 1
   * @param d2 the d 2
   * @return the int
   */
  public int daysBetween(java.util.Date d1, java.util.Date d2) {
    return (int) ((d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24));
  }
  
  /**
   * Insert records into hospital_claim_diagnosis that
   * haven't been copied from mrd_diagnosis before.
   * This method ensures that when claim is generated only coder
   * modified diagnoses are sent via claim.
   *
   * @param patientId the patient id
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void insertRecordsIntoHospDiagnosis(String patientId) throws SQLException, IOException {
    try (Connection con = DataBaseUtil.getConnection()) {
      Map<String, String> filterMap = new HashMap<>();
      filterMap.put("visit_id", patientId);
      // first check if records have already been inserted
      if ((hospitalClaimDiagDao.listAll(null, filterMap, null).isEmpty())) {
        List<BasicDynaBean> diagnosisList = mrdDiagnosisDao.listAll(null, filterMap, null);
        List<BasicDynaBean> hospitalClaimDiagList = new ArrayList<>();
        for (BasicDynaBean diagBean : diagnosisList) {
          BasicDynaBean hospitalClaimDiagBean = hospitalClaimDiagDao.getBean(con);
          hospitalClaimDiagBean.set("visit_id", diagBean.get("visit_id"));
          hospitalClaimDiagBean.set("id", ((BigDecimal) diagBean.get("id")).intValue());
          hospitalClaimDiagBean.set("description", diagBean.get("description"));
          hospitalClaimDiagBean.set("icd_code", diagBean.get("icd_code"));
          hospitalClaimDiagBean.set("code_type", diagBean.get("code_type"));
          hospitalClaimDiagBean.set("diag_type", diagBean.get("diag_type"));
          hospitalClaimDiagBean.set("username", diagBean.get("username"));
          hospitalClaimDiagBean.set("mod_time", diagBean.get("mod_time"));
          hospitalClaimDiagBean.set("remarks", diagBean.get("remarks"));
          hospitalClaimDiagBean.set("year_of_onset", diagBean.get("year_of_onset"));
          hospitalClaimDiagBean.set("present_on_admission", diagBean.get("present_on_admission"));
          // to identify that the record has been automatically copied and not edited yet
          hospitalClaimDiagBean.set("modified_by", "SYSTEM");

          hospitalClaimDiagList.add(hospitalClaimDiagBean);
        }
        hospitalClaimDiagDao.insertAll(con, hospitalClaimDiagList);
      }
    }    
  }
  
  /**
   * Save mrd.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws Exception the exception
   */
  public ActionForward saveMrd(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws Exception {
    Map<String, String[]> params = new HashMap<>(req.getParameterMap());
    
    String failedDoctorName = validateConsultationTypes(params.get("doctor_id"),
        params.get("consultation_type_id"));
    if (null != failedDoctorName) {
      ActionRedirect redirect = new ActionRedirect(mapping.findForward("getMRDUpdateDetails"));
      String visitId = req.getParameter("patId");
      redirect.addParameter("patient_id", visitId);
      FlashScope fs = FlashScope.getScope(req);
      fs.put("error", messageUtil.getMessage("mrd.update.consultation.check",
          new Object[] { failedDoctorName }));
      redirect.addParameter(FlashScope.FLASH_KEY, fs.key());
      return redirect;
    }
    
    
    List diagInsertList = new ArrayList();
    List diagUpdateList = new ArrayList();
    List diagDeleteList = new ArrayList();
    List consulUpdateList = new ArrayList();
    List trtUpdateList = new ArrayList();
    List loincUpdateList = new ArrayList();
    List consulCodeTypesList = new ArrayList();
    List<BasicDynaBean> billChgClaimList = new ArrayList<BasicDynaBean>();

    HttpSession session = req.getSession();
    String userid = (String) session.getAttribute("userid");
    if (null != req.getParameter("finalizeAll")) {
      List<String> listColumns = new ArrayList<>();
      listColumns.add("allow_bill_fnlz_with_pat_due");

      Map<String, Object> filtermap = new HashMap<>();
      filtermap.put("emp_username", userid);
      BasicDynaBean userBean = userDao.findByKey(listColumns, filtermap);
      if ("N".equals((String) userBean.get("allow_bill_fnlz_with_pat_due"))) {
        String visitId = req.getParameter("patId");
        BigDecimal visitPatientDue = BillDAO.getVisitPatientDue(visitId);
        if (null != visitPatientDue && !(visitPatientDue.compareTo(BigDecimal.ZERO) == 0)) {
          ActionRedirect redirect = new ActionRedirect(mapping.findForward("getMRDUpdateDetails"));
          redirect.addParameter("patient_id", visitId);
          FlashScope fs = FlashScope.getScope(req);
          fs.put("error",
              messageUtil.getMessage("js.billing.billlist.bill.finalization.notallowed"));
          redirect.addParameter(FlashScope.FLASH_KEY, fs.key());
          return redirect;
        }
      }
    }
    String[] diagCodeIds = params.get("id");
    String[] deleted = params.get("deleted");
    String[] presentOnArrival = params.get("present_on_admission");
    String[] yearOfOnset = params.get("year_of_onset");
    String[] consulCodeIds = params.get("consultation_id");
    String[] chargeIds = params.get("charge_id");
    String visitId = req.getParameter("patId");

    String[] primaryAuthIds = params.get("primary_auth_id");
    String[] primaryAuthModeIds = params.get("primary_auth_mode_id");
    String[] primaryChargeIds = params.get("primary_charge_id");
    String[] secondaryAuthIds = params.get("secondary_auth_id");
    String[] secondaryAuthModeIds = params.get("secondary_auth_mode_id");
    String[] secondaryChargeIds = params.get("secondary_charge_id");
    String[] diagCodeTypes = params.get("diag_code_type");
    String[] consulDescriptions = params.get("consul_code_desc");
    String[] consulCodeTypes = params.get("consul_code_type");
    String[] consulCodes = params.get("item_code");

    String drgBillNo = req.getParameter("drg_bill_no");
    String drgDescription = req.getParameter("drg_description");
    String primaryPriorAuthId = req.getParameter("primary_prior_auth_id");

    BasicDynaBean visitBean = VisitDetailsDAO.getVisitDetails(visitId);
    String[] loincCodeTypes = params.get("loinc_code_type");
    if (diagCodeIds != null) {
      for (int i = 0; i < diagCodeIds.length; i++) {
        if (diagCodeIds[i] == null || diagCodeIds[i].equals("")) {
          continue;
        }
        BasicDynaBean bean = hospitalClaimDiagDao.getBean();
        ConversionUtils.copyIndexToDynaBean(params, i, bean, null);
        bean.set("visit_id", visitId);
        bean.set("modified_by", userid);
        bean.set("present_on_admission", presentOnArrival[i]);
        if (StringUtils.isNotEmpty(yearOfOnset[i])) {
          bean.set("year_of_onset", Integer.parseInt(yearOfOnset[i]));
        }
        if (diagCodeTypes != null && diagCodeTypes[i] != null) {
          bean.set("code_type", diagCodeTypes[i]);
        }
        if (diagCodeIds[i].equals("-999") && !deleted[i].equals("true")) {
          // newly added diagnosis
          diagInsertList.add(bean);
        } else if (deleted[i].equals("true")) {
          diagDeleteList.add(bean);
        } else {
          diagUpdateList.add(bean);
        }
      }
    }

    if (consulCodeIds != null) {
      for (int i = 0; i < consulCodeIds.length; i++) {
        if (consulCodeIds[i] == null || consulCodeIds[i].equals("")) {
          continue;
        }
        BasicDynaBean bean = drConsulDAO.getBean();
        ConversionUtils.copyIndexToDynaBean(params, i, bean, null);
        bean.set("description", consulDescriptions[i]);
        consulUpdateList.add(bean);
        Map codeMap = new HashMap();
        codeMap.put("item_code", consulCodes[i]);
        codeMap.put("code_type", consulCodeTypes[i]);
        codeMap.put("user_name", userid);
        consulCodeTypesList.add(codeMap);
      }
    }

    if (chargeIds != null) {
      String[] trtCodeTypes = params.get("trt_code_type");
      for (int i = 0; i < chargeIds.length; i++) {
        BasicDynaBean trtBean = trtCodeDAO.getBean();
        ConversionUtils.copyIndexToDynaBean(params, i, trtBean, null);
        trtBean.set("code_type", trtCodeTypes[i]);
        trtUpdateList.add(trtBean);
      }
    }

    String[] primaryClaimIds = params.get("primary_claim_id");
    if (primaryClaimIds != null) {
      for (int i = 0; i < primaryClaimIds.length; i++) {
        if (null != primaryClaimIds[i] && !primaryClaimIds[i].equals("")) {
          Map<String, Object[]> primaryInsParams = new HashMap<String, Object[]>();
          primaryInsParams.put("claim_id", new Object[] { primaryClaimIds[i] });
          primaryInsParams.put("charge_id", new Object[] { primaryChargeIds[i] });
          primaryInsParams.put("prior_auth_id", new Object[] { primaryAuthIds[i] });
          primaryInsParams.put("prior_auth_mode_id", new Object[] { primaryAuthModeIds[i] });
          BasicDynaBean billChgClaimBeanForPrimary = billChgClaimDAO.getBean();
          ConversionUtils.copyToDynaBean(primaryInsParams, billChgClaimBeanForPrimary);

          billChgClaimList.add(billChgClaimBeanForPrimary);
        }
      }
    }

    String[] secondaryClaimIds = params.get("secondary_claim_id");
    if (secondaryClaimIds != null) {
      for (int i = 0; i < secondaryClaimIds.length; i++) {
        if (null != secondaryClaimIds[i] && !secondaryClaimIds[i].equals("")) {
          Map<String, Object[]> secondaryInsParams = new HashMap<String, Object[]>();
          secondaryInsParams.put("claim_id", new Object[] { secondaryClaimIds[i] });
          secondaryInsParams.put("charge_id", new Object[] { secondaryChargeIds[i] });
          secondaryInsParams.put("prior_auth_id", new Object[] { secondaryAuthIds[i] });
          secondaryInsParams.put("prior_auth_mode_id", new Object[] { secondaryAuthModeIds[i] });
          BasicDynaBean billChgClaimBeanForSecondary = billChgClaimDAO.getBean();
          ConversionUtils.copyToDynaBean(secondaryInsParams, billChgClaimBeanForSecondary);

          billChgClaimList.add(billChgClaimBeanForSecondary);
        }
      }
    }

    if (!req.getParameter("is_enc_end_overridden").equals("Y")) {
      params.remove("encounter_end_date");
      params.remove("encounter_end_time");
    }
    BasicDynaBean encBean = null;
    String[] insuranceIds = params.get("insurance_id");
    if (insuranceIds != null) {
      for (int i = 0; i < insuranceIds.length; i++) {
        encBean = regDao.getBean();
        ConversionUtils.copyIndexToDynaBean(params, i, encBean, null);
        encBean.set("prior_auth_id", primaryPriorAuthId);
        encBean.set("prior_auth_mode_id", visitBean.get("prior_auth_mode_id"));
        encBean.set("user_name", userid);
      }
    }

    BasicDynaBean loincBean = null;
    String[] testResultLblIds = params.get("loinc_id");
    if (testResultLblIds != null) {
      for (int i = 0; i < testResultLblIds.length; i++) {
        loincBean = loincDAO.getBean();
        ConversionUtils.copyIndexToDynaBean(params, i, loincBean, null);
        loincBean.set("code_type", loincCodeTypes[i]);
        loincBean.set("id", Integer.parseInt(testResultLblIds[i]));
        loincUpdateList.add(loincBean);
      }
    }

    BasicDynaBean regBean = null;
    regBean = regDao.getBean();
    ConversionUtils.copyIndexToDynaBean(params, 0, regBean, null, true);
    regBean.set("patient_id", visitId);
    regBean.set("codified_by", userid);
    regBean.set("user_name", userid);

    regBean.set("prior_auth_id", primaryPriorAuthId);
    regBean.set("prior_auth_mode_id", visitBean.get("prior_auth_mode_id"));

    String codificationStatus = req.getParameter("codification_status");
    if (codificationStatus != null) {
      regBean.set("codification_status", codificationStatus);
    }

    regBean.set("transfer_source", req.getParameter("encounter_start_source_id"));
    regBean.set("transfer_destination", req.getParameter("encounter_end_destination_id"));

    BasicDynaBean patBean = null;
    patBean = patientDAO.getBean();
    String mrNo = req.getParameter("mrNo");
    ConversionUtils.copyIndexToDynaBean(params, 0, patBean, null, true);
    patBean.set("mr_no", mrNo);

    boolean hasDRGCode = false;
    String drgCode = req.getParameter("drg_code");
    String drgChargeId = req.getParameter("drg_charge_id");
    if ((drgCode != null && !drgCode.trim().equals(""))
        || (drgChargeId != null && !drgChargeId.trim().equals(""))) {
      hasDRGCode = true;
    }

    Map<String, String> drgMap = null;
    if (hasDRGCode) {
      drgMap = new HashMap<String, String>();
      drgMap.put("act_rate_plan_item_code", drgCode);
      drgMap.put("charge_id", drgChargeId);
      drgMap.put("bill_no", drgBillNo);
      drgMap.put("username", userid);
    }
    BasicDynaBean patInsPlanBean = null;
    if (primaryPriorAuthId != null && StringUtils.isNotEmpty(primaryPriorAuthId)) {
      Map<String, Object> keys = new HashMap<>();
      keys.put("patient_id", visitId);
      keys.put("priority", 1);
      patInsPlanBean = patientInsurancePlanDAO.findByKey(keys);
      if (null != patInsPlanBean) {
        patInsPlanBean.set("prior_auth_id", primaryPriorAuthId);
      }
    }

    String msg = "MRD details save failed";
    String finalizPendingMsg = "";
    Map successMap = null;
    List drgUpdateList = new ArrayList();
    successMap = MRDUpdateScreenBO.saveMrd(diagInsertList, diagUpdateList, diagDeleteList,
        trtUpdateList, drgUpdateList, loincUpdateList, consulUpdateList, consulCodeTypesList,
        encBean, regBean, patBean, drgMap, billChgClaimList, patInsPlanBean);

    // Save consultation observations after consultation is saved
    // As the observation trigger is
    boolean success = (Boolean) successMap.get("success");
    if (!success && (successMap.get("error") != null || !successMap.get("error").equals(""))) {
      msg = (String) successMap.get("error");
    }

    String[] vitalChargeIds = params.get("vitalCharge_id");
    if (vitalChargeIds != null && vitalChargeIds.length > 0) {
      try (Connection con = DataBaseUtil.getConnection()) {
        for (int i = 0; i < vitalChargeIds.length; i++) {
          String[] observationCode = params.get("vitalObserCode." + vitalChargeIds[i]);
          String[] observationType = params.get("vitalObserType." + vitalChargeIds[i]);
          String[] observationValue = params.get("vitalObserValue." + vitalChargeIds[i]);
          String[] observationValueType = params.get("vitalObserValueType." + vitalChargeIds[i]);
          String[] observationValueEditable = params
              .get("vitalObserValueEditable." + vitalChargeIds[i]);
          obserDAO.delete(con, "charge_id", vitalChargeIds[i]);
          if (observationCode != null && observationCode.length > 0) {
            for (int j = 0; j < observationCode.length; j++) {
              BasicDynaBean obserBean = obserDAO.getBean();
              obserBean.set("charge_id", vitalChargeIds[i]);
              obserBean.set("code", observationCode[j]);
              obserBean.set("observation_type", observationType[j]);
              obserBean.set("value", observationValue[j]);
              obserBean.set("value_type", observationValueType[j]);
              obserBean.set("value_editable",
                  observationValueEditable[j] == null || observationValueEditable[j].equals("")
                      ? "Y"
                      : observationValueEditable[j]);
              obserDAO.insert(con, obserBean);
            }
          }
        }
      }
    }

    String[] physicianChargeIds = params.get("physicianCharge_id");
    if (physicianChargeIds != null && physicianChargeIds.length > 0) {
      try (Connection con = DataBaseUtil.getConnection()) {
        for (int i = 0; i < physicianChargeIds.length; i++) {
          String[] observationCode = params.get("physicianObserCode." + physicianChargeIds[i]);
          String[] observationType = params.get("physicianObserType." + physicianChargeIds[i]);
          String[] observationValue = params.get("physicianObserValue." + physicianChargeIds[i]);
          String[] observationValueType = params
              .get("physicianObserValueType." + physicianChargeIds[i]);
          String[] observationValueEditable = params
              .get("physicianObserValueEditable." + physicianChargeIds[i]);
          if (observationCode != null && observationCode.length > 0) {
            for (int j = 0; j < observationCode.length; j++) {
              BasicDynaBean obserBean = obserDAO.getBean();
              obserBean.set("charge_id", physicianChargeIds[i]);
              obserBean.set("code", observationCode[j]);
              obserBean.set("observation_type", observationType[j]);
              obserBean.set("value", observationValue[j]);
              obserBean.set("value_type", observationValueType[j]);
              obserBean.set("value_editable",
                  observationValueEditable[j] == null || observationValueEditable[j].equals("")
                      ? "Y"
                      : observationValueEditable[j]);
              obserDAO.insert(con, obserBean);
            }
          }
        }
      }
    }

    BillBO billBO = new BillBO();
    Set<String> billsFinalized = new HashSet<>();
    try (Connection con = DataBaseUtil.getConnection()) { 
      List<BasicDynaBean> openBills = BillDAO.getVisitAllOpenBills(con, visitId);
      StringBuilder billBuff = new StringBuilder();
      String sep = "";
      String finalizeBills = req.getParameter("finalizeAll");
      if (finalizeBills != null) {
        for (BasicDynaBean bill : openBills) {
          String billNo = (String) bill.get("bill_no");
          Bill billObj = billBO.getBill(billNo);

          String equipFinalized = OrderDAO.isBillEquipmentDetailsFinalized(billObj.getVisitId(),
              billNo);
          String bedFinalized = OrderDAO.isBillBedDetailsFinalized(billObj.getVisitId(), billNo);
          String error = null;
          if (equipFinalized.equals("Finalized") && bedFinalized.equals("Finalized")) {
            error = new BillBO().updateBillStatus(billObj, Bill.BILL_STATUS_FINALIZED,
                billObj.getPaymentStatus(), "Y", DateUtil.getCurrentTimestamp(), userid, false,
                false);
            if (error == null || error.equals("")) {
              billsFinalized.add(billNo);
            }
          } else {
            billBuff.append(sep);
            billBuff.append(billNo);
            sep = ",";
          }
          if (error == null || error.equals("")) {
            success = success && true;
          } else {
            success = false;
          }
        }
        if (billBuff.length() > 0) {
          finalizPendingMsg = "<br/> The following bills were not finalized because "
              + "bed/equipment finalization is pending for these bills: " + billBuff.toString();
        }
      }
    }

    if (success) {
      if (finalizPendingMsg.equals("")) {
        msg = "MRD details saved successfully";
      } else {
        msg = "MRD details saved successfully" + finalizPendingMsg;
      }
    }

    String ceedcheck = req.getParameter("ceedcheck");
    if (ceedcheck != null && ceedcheck.equals("Y")) { // redirect for sending ceed request
      ActionRedirect redirect = new ActionRedirect(mapping.findForward("sendCeedRequestRedirect"));
      String ceedCheckType = req.getParameter("ceedchecktype");
      redirect.addParameter("ceed_check_type", ceedCheckType);
      redirect.addParameter("patient_id", visitId);
      redirect.addParameter("drg_charge_id", drgChargeId);
      return redirect;
    }

    // schedule accounting for the bills finalized
    List<BasicDynaBean> billsList = billBO.getBillBeans(billsFinalized);
    if (billsList != null && billsList.size() > 0) {
      accountingJobScheduler.scheduleAccountingForBills(billsList);
    }

    ActionRedirect redirect = new ActionRedirect(mapping.findForward("getMRDUpdateDetails"));
    redirect.addParameter("patient_id", visitId);
    redirect.addParameter("drg_charge_id", drgChargeId);
    FlashScope fs = FlashScope.getScope(req);
    fs.put("success", msg);
    fs.put("referer", req.getParameter("referer"));
    redirect.addParameter(FlashScope.FLASH_KEY, fs.key());
    return redirect;
  }
  
  private String validateConsultationTypes(String[] doctorIds, String[] consultationTypeIds)
      throws SQLException {

    GenericDAO doctorDao = new GenericDAO("doctors");
    if (null != doctorIds && doctorIds.length > 0) {
      for (int i = 0; i < doctorIds.length; i++) {
        String doctorId = doctorIds[i];
        String consultationType = consultationTypeIds[i];
        BasicDynaBean doctorBean = doctorDao.findByKey("doctor_id", doctorId);
        if (doctorBean == null || doctorBean.get("practitioner_id") == null) {
          continue;
        }
        int practitionerTypeId = (int) doctorBean.get("practitioner_id");
        List<BasicDynaBean> consultationTypes = practitionerConsultationMappingService
            .getConsultationTypes(practitionerTypeId,null);
        boolean isConsultationExist = false;
        if (!consultationTypes.isEmpty()) {
          for (BasicDynaBean consultaitonBean : consultationTypes) {
            if (consultationType.equals(String.valueOf(
                consultaitonBean.get("consultation_type_id")))) {
              isConsultationExist = true;
              break;
            }
          }
          if (!isConsultationExist) {
            return (String) doctorBean.get("doctor_name");
          }
        }
      }
    }

    return null;
  }

  /**
   * Gets the codes list of code type.
   *
   * @param am the am
   * @param af the af
   * @param req the req
   * @param res the res
   * @return the codes list of code type
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getCodesListOfCodeType(ActionMapping am, ActionForm af,
      HttpServletRequest req, HttpServletResponse res) throws Exception {
    String searchInput = req.getParameter("query");
    String codeType = req.getParameter("codeType");
    String patientType = (req.getParameter("patientType") != null
        && !req.getParameter("patientType").trim().equals("")) ? req.getParameter("patientType")
            : "";
    String dialogType = req.getParameter("dialog_type");
    List list = null;
    list = mrdupdateScreenBo.getCodesListOfCodeType(searchInput, codeType, patientType,
        dialogType);
    Map icdmap = new HashMap();
    icdmap.put("result", ConversionUtils.listBeanToListMap(list));
    String responseContent = new JSONSerializer().deepSerialize(icdmap);

    res.setContentType("application/json");
    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    res.getWriter().write(responseContent);
    res.flushBuffer();
    return null;

  }

  /**
   * Gets the diagnosis history.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the diagnosis history
   * @throws Exception the exception
   */
  public ActionForward getDiagnosisHistory(ActionMapping mapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws Exception {
    String patientId = req.getParameter("visitId");
    if (patientId != null && !patientId.equals("")) {
      List list = MRDDiagnosisDAO.getDiagnosisHistory(patientId);
      res.setContentType("application/json");
      res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
      res.getWriter().write(js.serialize(ConversionUtils.copyListDynaBeansToMap(list)));
      res.flushBuffer();
    }
    return null;
  }

  /**
   * Gets the open review count.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the open review count
   * @throws Exception the exception
   */
  // Ajax function, it will return open+inprogress reviews count.
  public ActionForward getOpenReviewCount(ActionMapping mapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws Exception {
    String patientId = req.getParameter("visitId");
    Integer reviewsCount = 0;
    res.setContentType("application/json");
    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    if (patientId != null && !patientId.equals("")) {
      try (Connection con = DataBaseUtil.getConnection()) {
        reviewsCount = dao.getOpenInprogressReviewsCount(con, patientId);
      }
    }
    res.getWriter().write(js.serialize(reviewsCount));
    res.flushBuffer();
    return null;
  }

  /**
   * View.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws Exception the exception
   */
  public ActionForward view(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws Exception {
    req.setAttribute("patient_id", req.getParameter("patient_id"));
    return mapping.findForward("getReportScreen");
  }

  /**
   * Prints the.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws Exception the exception
   */
  public ActionForward print(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws Exception {

    String patientId = req.getParameter("patient_id") != null
        ? (String) req.getParameter("patient_id")
        : (String) req.getAttribute("patient_id");
    String format = req.getParameter("format");
    Map map = req.getParameterMap();
    if ((format == null) || (format.equals(""))) {
      format = "pdf";
    }
    HtmlConverter converter = new HtmlConverter();
    StringWriter writer = MRDUpdateScreenBO.getReportContentStringWriter(patientId, format, map);
    if (format.equals("pdf")) {
      OutputStream os = res.getOutputStream();
      res.setContentType("application/pdf");
      converter.writePdf(os, writer.toString());
      os.close();
    } else {
      res.setContentType("text/html");
      res.getWriter().write(writer.toString());
    }
    return null;
  }

  /**
   * Validate code.
   *
   * @param am the am
   * @param af the af
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws Exception the exception
   */
  public ActionForward validateCode(ActionMapping am, ActionForm af, HttpServletRequest req,
      HttpServletResponse res) throws Exception {

    String codeType = req.getParameter("codeType");
    String code = req.getParameter("code");
    String category = req.getParameter("codeCategory");
    String visitType = req.getParameter("visit_type");
    boolean valid = MRDUpdateScreenBO.validateCode(category, codeType, code, visitType);

    String result = valid ? "valid" : "invalid";
    res.setContentType("text/plain");
    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    res.getWriter().write(result);
    res.flushBuffer();
    return null;
  }

  /**
   * Reopen codifiaction.
   *
   * @param am the am
   * @param af the af
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws Exception the exception
   */
  public ActionForward reopenCodifiaction(ActionMapping am, ActionForm af, HttpServletRequest req,
      HttpServletResponse res) throws Exception {

    String msg = "Codification reopened";
    String visitId = req.getParameter("patId");
    BasicDynaBean bean = regDAO.getBean();
    bean.set("patient_id", visitId);
    bean.set("codification_status", "P");
    int count = MRDUpdateScreenBO.reopenCodifiaction(bean);
    if (count == 0) {
      msg = "Failed to reopen";
    }
    FlashScope fs = FlashScope.getScope(req);
    ActionRedirect redirect = new ActionRedirect(am.findForward("getMRDUpdateDetails"));
    redirect.addParameter("patient_id", visitId);
    fs.put("success", msg);
    redirect.addParameter(FlashScope.FLASH_KEY, fs.key());
    return redirect;
  }

  /**
   * Gets the Transfer Hospitals.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the transfer hospitals
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getTransferHospitalSearch(ActionMapping mapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws Exception {

    String searchInput = req.getParameter("query");
    List list = null;
    Integer limit = Integer.parseInt(req.getParameter("limit"));
    list = mrdupdateScreenBo.getTransferHospitals(searchInput, limit);
    Map result = new HashMap();
    result.put("results", ConversionUtils.listBeanToListMap(list));
    String responseContent = new JSONSerializer().deepSerialize(result);
    res.setContentType("application/json");
    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    res.getWriter().write(responseContent);
    res.flushBuffer();
    return null;
  }

}
