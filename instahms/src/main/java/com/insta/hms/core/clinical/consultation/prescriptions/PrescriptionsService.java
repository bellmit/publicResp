package com.insta.hms.core.clinical.consultation.prescriptions;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.modulesactivated.ModulesActivatedService;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.security.SecurityService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.clinical.consultation.ValidationUtils;
import com.insta.hms.core.clinical.dischargemedication.DischargeMedicationService;
import com.insta.hms.core.clinical.forms.FormParameter;
import com.insta.hms.core.clinical.forms.SystemSectionService;
import com.insta.hms.core.clinical.mar.MarService;
import com.insta.hms.core.clinical.outpatient.DoctorConsultationService;
import com.insta.hms.core.clinical.patientactivities.PatientActivitiesRepository;
import com.insta.hms.core.clinical.patientactivities.PatientActivitiesService;
import com.insta.hms.core.patient.registration.PatientInsurancePlansService;
import com.insta.hms.core.patient.registration.RegistrationService;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.integration.clinical.ceed.CeedIntegrationService;
import com.insta.hms.integration.insurance.pbm.PBMPrescriptionsRepository;
import com.insta.hms.integration.insurance.pbm.PBMPrescriptionsService;
import com.insta.hms.integration.insurance.pbmauthorization.PBMMedicinePrescriptionsRepository;
import com.insta.hms.mdm.centers.CenterService;
import com.insta.hms.mdm.departments.DepartmentService;
import com.insta.hms.mdm.doctors.DoctorService;
import com.insta.hms.mdm.formcomponents.FormComponentsService;
import com.insta.hms.mdm.healthauthoritypreferences.HealthAuthorityPreferencesService;
import com.insta.hms.mdm.insuranceplans.InsurancePlanService;
import com.insta.hms.mdm.operations.OperationsService;
import com.insta.hms.mdm.ordersets.OrderSetsService;
import com.insta.hms.mdm.prescriptionfavourites.PrescriptionFavouritesService;
import com.insta.hms.mdm.services.ServicesService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author krishnat.
 *
 */
@Service
public class PrescriptionsService extends SystemSectionService {

  @LazyAutowired
  ModulesActivatedService modulesActivatedService;

  @LazyAutowired
  PendingPrescriptionsRemarksRepository pendingPrescriptionsRemarksRepository;

  /** The log. */
  private static Logger log = LoggerFactory.getLogger(PrescriptionsService.class);

  /** Prescription item type constants. */
  public static final String MEDICINE = "Medicine";
  public static final String INVESTIGATION = "Inv.";
  public static final String SERVICE = "Service";
  public static final String OPERATION = "Operation";
  public static final String DOCTOR = "Doctor";
  public static final String DOCTOR_TYPE = "DOC";
  public static final String DEPT_TYPE = "DEPT";
  public static final String NON_HOSPITAL = "NonHospital";
  public static final String ORDER_SETS = "Order Sets";
  public static final String NON_BILLABLE = "NonBillable";

  /** Prescription item type constants. Used for Ip screens */
  private Map<String, String> itemTypesShortForm = new HashMap<>();

  /**
   * Gets item type short form.
   * @return the map
   */
  public Map<String, String> getItemTypesShortForm() {
    if (itemTypesShortForm.isEmpty()) {
      this.itemTypesShortForm.put(MEDICINE, "M");
      this.itemTypesShortForm.put(INVESTIGATION, "I");
      this.itemTypesShortForm.put(SERVICE, "S");
      this.itemTypesShortForm.put(DOCTOR, "C");
      this.itemTypesShortForm.put(NON_BILLABLE, "O");
    }
    return itemTypesShortForm;
  }

  /** Prescription item type constants list. */
  public static final List<String> ITEM_TYPES = Arrays.asList(new String[] { MEDICINE,
      INVESTIGATION, SERVICE, OPERATION, DOCTOR, NON_HOSPITAL, NON_BILLABLE });

  /** Prescription item type constants list. */
  public static final List<String> PENDING_PRESCRIPTION_TYPES = Arrays
      .asList(new String[] { INVESTIGATION, SERVICE, DOCTOR });

  /** Constants mapping Keys. */
  public static final String PRIORITY_CONSTANT_KEY = 
      "constant.prescription.priority.";
  public static final String PRIORITY_CONSTANT_SHORT_KEY = 
      "constant.prescription.priority.shortkey.";
  public static final String ITEM_TYPE_CONSTANT_SHORT_KEY = 
      "constant.prescription.itemtype.shortkey.";
  public static final String CROSS_CODE_STATUS_CONSTANT_KEY = 
      "constant.prescription.claim.edit.rank.";
  public static final String DURATION_UNITS_CONSTANT_KEY = 
      "constant.prescription.duration.units.";

  /** List of Constants. */
  public static final List<String> DURATION_UNIT_VALUES = Arrays
      .asList(new String[] { "D", "W", "M" });
  public static final List<String> PRIORITY_VALUES = Arrays
      .asList(new String[] { "N", "P", "S", "U" });

  /** Cross code Colors. */
  private static final String COLOUR_CODE_N = "green";
  private static final String COLOUR_CODE_A = "red";
  private static final String COLOUR_CODE_R = "yellow";
  private static final String COLOUR_CODE_E = "orange";
  private static final String COLOUR_CODE_NA = "grey";
  private static final String COLOUR_CODE_NI = "black";

  /** Items lookUp limit. */
  public static final Integer ITEMS_LIMIT = 25;

  @LazyAutowired
  private PrescriptionsRepository presRepo;

  @LazyAutowired
  private PendingPrescriptionsRepository pendingPresRepo;

  /* Services */
  @LazyAutowired
  private CeedIntegrationService ceedIntegrationService;
  @LazyAutowired
  private DoctorService docService;
  @LazyAutowired
  private GenericPreferencesService genPrefService;
  @LazyAutowired
  private HealthAuthorityPreferencesService healthAuthorityPrefService;
  @LazyAutowired
  private OperationsService operService;
  @LazyAutowired
  private SecurityService securityService;
  @LazyAutowired
  private ServicesService svrService;
  @LazyAutowired
  private RegistrationService regService;
  @LazyAutowired
  private InsurancePlanService insPlanService;
  @LazyAutowired
  private PBMPrescriptionsService pbmPrescService;
  @LazyAutowired
  private PBMPrescriptionsRepository pbmPrescriptionsRepository;
  @LazyAutowired
  private PatientInsurancePlansService patInsurancePlanService;
  @LazyAutowired
  private PrescriptionFavouritesService presFavService;
  @LazyAutowired
  private DoctorConsultationService doctorConsultationService;
  @LazyAutowired
  private MedicineItemService medicineItemService;
  @LazyAutowired
  private MedicinePrescriptionsRepository medicinePrescriptionsRepository;
  @LazyAutowired
  private InvestigationItemService investigationItemService;
  @LazyAutowired
  private PrescriptionItemFactory prescriptionItemFactory;
  @LazyAutowired
  private PrescriptionEAuthorization prescriptionEAuthorization;
  @LazyAutowired
  private CenterService centerService;
  @LazyAutowired
  private SessionService sessionService;
  @LazyAutowired
  private OrderSetsService orderSetsService;
  @LazyAutowired
  private PatientActivitiesService patientActivitiesService;
  @LazyAutowired
  private PatientActivitiesRepository patientActivitiesRepository;
  @LazyAutowired
  private MarService marService;
  @LazyAutowired
  PendingPrescriptionsService pendingPrescriptionsService;
  @LazyAutowired
  PendingPrescriptionsRepository pendingPrescriptionsRepository;
  @LazyAutowired
  private DepartmentService departmentService;
  @LazyAutowired
  DischargeMedicationService disMedicationService;
  @LazyAutowired
  private PBMMedicinePrescriptionsRepository pbmMedicinePrescriptionsRepository;

  /* Utils */
  @LazyAutowired
  private MessageUtil messageUtil;

  /* Validators */
  @LazyAutowired
  private PrescriptionsValidator presValidator;
  
  @LazyAutowired
  private PrescriptionsMainRepository patientPrescriptionsMainRepository;

  private final String pbmPrescIdKey = "pbm_presc_id";

  public PrescriptionsService() {
    this.sectionId = -7;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Map<String, Object> saveSection(Map<String, Object> requestBody, BasicDynaBean sdbean,
      FormParameter parameter, Map<String, Object> errorMap) {
    BasicDynaBean genPref = genPrefService.getAllPreferences();
    Map<String, Object> sessionnAttributes = sessionService
        .getSessionAttributes(new String[] { "userId", "mod_eclaim_pbm", "mod_eclaim_erx" });
    boolean modEclaimPbm = (Boolean) sessionnAttributes.get("mod_eclaim_pbm");
    boolean modEclaimErx = (Boolean) sessionnAttributes.get("mod_eclaim_erx");

    BasicDynaBean patientBean = regService.findByKey(parameter.getPatientId());
    String healthAuthority = (String) centerService.findByKey((int) patientBean.get("center_id"))
        .get("health_authority");
    boolean generics = false;
    boolean usesStores = true;
    if (!parameter.getFormType().equals(FormComponentsService.FormType.Form_IP.toString())) {
      Map<String, Object> filterMap = new HashMap<>();
      filterMap.put("health_authority", healthAuthority);
      generics = ((String) healthAuthorityPrefService.findByPk(filterMap)
          .get("prescriptions_by_generics")).equalsIgnoreCase("Y");
      usesStores = genPref.get("prescription_uses_stores").equals("Y");
    }
    
    boolean sendForErx = requestBody.get("send_for_erx") == null ? false
        : (boolean) requestBody.get("send_for_erx");
    
    BasicDynaBean pbmPrescBean = getPbmPrescBean(parameter, modEclaimPbm, modEclaimErx,
        (Integer) patientBean.get("plan_id"), false, sendForErx);
    Integer pbmPresId = pbmPrescBean != null ? (Integer) pbmPrescBean.get(pbmPrescIdKey) : 0;
    boolean success = true;
    boolean isPrescriptionChanged = false;
    Map<String, Object> response = new HashMap<>();
    if (requestBody.get("insert") != null) {
      success &= insert(requestBody, errorMap, parameter,
          generics, usesStores, (String) sessionnAttributes.get("userId"), pbmPresId,
          (Integer) requestBody.get("section_id"), response, healthAuthority);
      isPrescriptionChanged = success;
    }

    if (requestBody.get("update") != null) {
      success &= update(requestBody, errorMap, parameter,
          generics, usesStores, (String) sessionnAttributes.get("userId"), pbmPresId,
          (Integer) requestBody.get("section_id"), response, healthAuthority);
      isPrescriptionChanged = success;
    }

    if (requestBody.get("delete") != null) {
      success &= delete(requestBody, errorMap, parameter, generics, usesStores,
          (String) sessionnAttributes.get("userId"), response);
      isPrescriptionChanged = success;
    }

    if (pbmPresId != 0) {
      Map<String, Object> pbmKeysMap = new HashMap<>();
      pbmKeysMap.put(pbmPrescIdKey, pbmPresId);
      int drugCount = (medicineItemService.listAll(pbmKeysMap)).size();
      medicineItemService.attachPbmToERX((Integer) parameter.getId(), pbmPresId);
      // Update drug count in PBM Prescription.
      pbmPrescBean.set("drug_count", drugCount);
      success &= pbmPrescriptionsRepository.update(pbmPrescBean, pbmKeysMap) > 0;
      success &= success
          ? updatePbmMedicinePrescriptions(pbmPrescBean, parameter, healthAuthority, drugCount) :
          false;
    }

    if (success) {
      return response;
    }
    return null;
  }

  /**
   * to update PBMMedicinePrescriptions.
   * @param pbmPrescBean pbmPrescBean
   * @param parameter formParameter
   * @param healthAuthority the healthAuthority
   * @param drugCount drugCount
   * @return true if updateSuccessful
   */
  public boolean updatePbmMedicinePrescriptions(BasicDynaBean pbmPrescBean,
      FormParameter parameter, String healthAuthority, int drugCount) {
    boolean erxEnabledForCenter = false;
    boolean pbmEnabledForCenter = false;
    if (!StringUtils.isEmpty(healthAuthority)) {
      String healthAuthLower = healthAuthority.toLowerCase();
      if (healthAuthLower.equals("haad")) {
        pbmEnabledForCenter = true;
      } else if (healthAuthLower.equals("dha")) {
        erxEnabledForCenter = true;
      }
    }
    /* Get patient medicine prescriptions for pbm prescribed id, if records in pbm_medicine_presc
     *  exists, update would be run*/
    List<BasicDynaBean> patientPbmPrescMedicines = medicinePrescriptionsRepository
        .getPbmPrescItemsWithDrugCount((int) pbmPrescBean.get(pbmPrescIdKey), drugCount);
    boolean success = true;
    for (BasicDynaBean patientMedicinePrescBean : patientPbmPrescMedicines) {
      BasicDynaBean pbmMedicinePrescBean = pbmMedicinePrescriptionsRepository
          .findByKey("op_medicine_pres_id", patientMedicinePrescBean.get("op_medicine_pres_id"));
      if (pbmMedicinePrescBean != null) {
        if (pbmMedicinePrescBean.get("updated_in_pbm").equals("N")) {
          copyPatientMedToPbmBean(patientMedicinePrescBean, pbmMedicinePrescBean, parameter);
          Map<String, Object> updateKeys = new HashMap<>();
          updateKeys
              .put("op_medicine_pres_id", patientMedicinePrescBean.get("op_medicine_pres_id"));
          success &=
              pbmMedicinePrescriptionsRepository.update(pbmMedicinePrescBean, updateKeys) > 0;
        }
      } else {
        pbmMedicinePrescBean = pbmMedicinePrescriptionsRepository.getBean();
        if (pbmEnabledForCenter || (erxEnabledForCenter && patientMedicinePrescBean
            .get("send_for_erx").equals("Y"))) {
          copyPatientMedToPbmBean(patientMedicinePrescBean, pbmMedicinePrescBean, parameter);
          pbmMedicinePrescBean.set("updated_in_pbm", "N");
          success &= pbmMedicinePrescriptionsRepository.insert(pbmMedicinePrescBean) == 1;
        }
      }
      if (!success) {
        break;
      }
    }
    return success;
  }

  private void copyPatientMedToPbmBean(BasicDynaBean patientMedicinePrescBean,
      BasicDynaBean pbmMedicinePrescBean, FormParameter parameter) {
    Map patientMedicinePrescBeanMap = new HashMap<>();
    patientMedicinePrescBeanMap.putAll(patientMedicinePrescBean.getMap());
    int medicineQuantity = (int) patientMedicinePrescBeanMap.get("medicine_quantity");
    patientMedicinePrescBeanMap.remove("medicine_quantity");
    pbmMedicinePrescBean.set("medicine_quantity", new BigDecimal(medicineQuantity));
    ConversionUtils.copyToDynaBean(patientMedicinePrescBeanMap, pbmMedicinePrescBean);
    if (!parameter.getFormType().equals(FormComponentsService.FormType.Form_IP.toString())) {
      pbmMedicinePrescBean.set("consultation_id", (int) parameter.getId());
    }
    pbmMedicinePrescBean.set("visit_id", parameter.getPatientId());
  }

  /**
   * Gets pbm id.
   * 
   * @param parameter the form param
   * @param modEclaimPbm the boolean
   * @param modEclaimErx the boolean
   * @param planId the integer
   * @return the integer
   */
  public BasicDynaBean getPbmPrescBean(FormParameter parameter, boolean modEclaimPbm,
      boolean modEclaimErx, Integer planId, boolean isDisMedication, boolean sendForErx) {
    BasicDynaBean pbmPrescBean = null;
    
    if (!sendForErx) {
      return null;
    }
    
    if (parameter.getFormType().equals(FormComponentsService.FormType.Form_IP.toString())
        && !isDisMedication) {
      return null;
    }

    /*
     * PBM Prior Auth is required only when mod_eclaim_pbm module is enabled
     * (mod_eclaim_erx is disabled) and visit type is 'o' (or) mod_eclaim_erx module
     * is enabled and visit type is 'o'
     */
    if (planId == 0) {
      BasicDynaBean patientPlan = patInsurancePlanService
          .getVisitPrimaryPlan(parameter.getPatientId());
      planId = (null != patientPlan) ? (Integer) patientPlan.get("plan_id") : 0;
    }

    boolean pbmPriorAuthRequired = false;
    /*
     * When mod_eclaim_erx is enabled, all medicines prescribed in the consultation
     * will be with in a single prescription.
     */
    if (modEclaimErx) {

      pbmPriorAuthRequired = true;
      int pbmPrescId = medicineItemService.getPrescriptions(parameter.getId());
      pbmPrescBean = pbmPrescriptionsRepository.getPbmPrescriptionWithOpenStatus(pbmPrescId);
      /*
       * When mod_eclaim_erx is not enabled and mod_eclaim_pbm is enabled, medicines
       * prescribed in the consultation can fall into multiple prescriptions. So, get
       * the latest Open pbm_presc_id if exists to save medicines prescriptions. There
       * may be a pbm prescription in Sent mode. In such case, a new Id is generated
       * for later entries. Also, new prescription(s) can be created by Pharmacist in
       * Pharmacy for the same consultation.
       */
    } else if (modEclaimPbm) {
      Map<String, Object> plamFilterMap = new HashMap<>();
      plamFilterMap.put("plan_id", planId);
      BasicDynaBean planBean = insPlanService.findByPk(plamFilterMap);
      if (planBean != null && planBean.get("require_pbm_authorization").equals("Y")) {
        pbmPriorAuthRequired = true;
        pbmPrescBean = medicineItemService.getLatestPBMPresId(parameter.getId());
      }
    }

    if (pbmPriorAuthRequired && null == pbmPrescBean) {
      pbmPrescBean = pbmPrescService.insert(parameter.getId());
    }
    return pbmPrescBean;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Map<String, Object> getSectionDetailsFromCurrentForm(FormParameter parameters) {

    Boolean presFromStores = true;
    boolean isIpForm =
        parameters.getFormType().equals(FormComponentsService.FormType.Form_IP.toString());
    if (!isIpForm) {
      presFromStores = ((String) genPrefService.getAllPreferences().get("prescription_uses_stores"))
          .equalsIgnoreCase("Y");
    }

    BasicDynaBean patientBean = regService.findByKey(parameters.getPatientId());
    String healthAuthority = (String) centerService.findByKey((int) patientBean.get("center_id"))
        .get("health_authority");
    List<Map<String, Object>> prescriptions = ConversionUtils
        .copyListDynaBeansToLinkedMap(presRepo.getPrescriptions(parameters.getId(),
            (String) patientBean.get("bed_type"), (String) patientBean.get("org_id"),
            (Integer) patientBean.get("center_id"), healthAuthority, presFromStores));
    List<String> activatedModules = securityService.getActivatedModules();
    int prescriptionsL = prescriptions.size();
    BasicDynaBean ceedBean = null;
    Map<String, Object> ceedResponses = null;
    boolean isceedModuleEnabled = activatedModules.contains("mod_ceed_integration");
    if (isceedModuleEnabled && isIpForm) {
      ceedBean = ceedIntegrationService.checkIfCeedCheckDone((int) parameters.getId());
      ceedResponses = ConversionUtils.listBeanToMapListMap(
          ceedIntegrationService.getResponseDetails((int) parameters.getId()), "activity_id");
    }
    for (int i = 0; i < prescriptionsL; i++) {
      Map<String, Object> prescription = prescriptions.get(i);
      if (isIpForm) {
        updateItemIfAdministered(prescription);
      }
      String itemType = (String) prescription.get("item_type");
      if (isceedModuleEnabled) {
        if (ceedBean == null) {
          prescription.put("cross_code_status",
              messageUtil.getMessage(CROSS_CODE_STATUS_CONSTANT_KEY + "NI", null));
          prescription.put("cross_code_colour", COLOUR_CODE_NI);
        } else if (DOCTOR.equals(itemType) || NON_HOSPITAL.equals(itemType)
            || (MEDICINE.equals(itemType) && (((boolean) prescription.get("non_hosp_medicine"))
                || (prescription.get("item_id") == null || prescription.get("item_id").equals("")
                    || prescription.get("master").equals("op"))))) {
          prescription.put("cross_code_status",
              messageUtil.getMessage(CROSS_CODE_STATUS_CONSTANT_KEY + "NA", null));
          prescription.put("cross_code_colour", COLOUR_CODE_NA);
        } else if (ceedResponses.containsKey(prescription.get("item_prescribed_id"))) {
          List<Map<String, Object>> presCeedResponses = (List<Map<String, Object>>) ceedResponses
              .get(prescription.get("item_prescribed_id"));
          String rank = "N";
          String color = COLOUR_CODE_N;
          for (Map<String, Object> response : presCeedResponses) {
            if (response.get("claim_edit_rank").equals("A")) {
              rank = "A";
              color = COLOUR_CODE_A;
            } else if (response.get("claim_edit_rank").equals("R") && !rank.equals("A")) {
              rank = "R";
              color = COLOUR_CODE_R;
            } else if (response.get("claim_edit_rank").equals("E")) {
              rank = "E";
              color = COLOUR_CODE_E;
            } else if (response.get("claim_edit_rank").equals("NA")) {
              rank = "NA";
              color = COLOUR_CODE_NA;
            }
          }
          prescription.put("cross_code_status",
              messageUtil.getMessage(CROSS_CODE_STATUS_CONSTANT_KEY + rank, null));
          prescription.put("cross_code_colour", color);
        } else {
          prescription.put("cross_code_status",
              messageUtil.getMessage(CROSS_CODE_STATUS_CONSTANT_KEY + "NI", null));
          prescription.put("cross_code_colour", COLOUR_CODE_NI);
        }
      } else {
        prescription.put("cross_code_status", null);
        prescription.put("cross_code_colour", null);
      }
      if (MEDICINE.equals(itemType) || NON_HOSPITAL.equals(itemType)) {
        prescription.put("priority_text",
            messageUtil.getMessage(PRIORITY_CONSTANT_KEY + prescription.get("priority"), null));
      } else {
        prescription.put("priority_text", null);
      }
    }
    Map<String, Object> data = new HashMap<>();
    data.put("records", prescriptions);
    return data;
  }

  /**
   * Check if any activity existing for the prescription item has been administered.
   * Update Prescription with isAdministered: 'false', if an activity exists.
   * Same flag used in UI for enabling Prescription edit in Physician Order (IPEMR).
   * @param prescription - prescription bean.
   */
  private void updateItemIfAdministered(Map<String, Object> prescription) {
    Map<String, Object> filterMap = new HashMap<>();
    filterMap.put("prescription_id", prescription.get("item_prescribed_id"));
    filterMap.put("prescription_type", "M"); //Medicine
    filterMap.put("activity_status", "D"); //Administered
    prescription.put("is_administered", null != patientActivitiesRepository.findByKey(filterMap));
  }

  @Override
  public Map<String, Object> getSectionDetailsFromLastSavedForm(FormParameter parameter) {
    if (FormComponentsService.FormType.Form_IP.name().equals(parameter.getFormType())
            && presRepo.isAdmReqPresentInPresc(parameter.getMrNo(),parameter.getPatientId())) {
      return getSectionDetailsFromCurrentForm(parameter);
    }
    Map<String, Object> data = new HashMap<>();
    data.put("records", new ArrayList<Map<String, Object>>());
    return data;
  }

  /**
   * Gets prescription items.
   *
   * @param bedType the string
   * @param orgId the string
   * @param patientType the string
   * @param insPlanId the integer
   * @param presType the string
   * @param isDischargeMedication indicator for discharge medication section
   * @return the list of map
   */
  @SuppressWarnings("unchecked")
  public List<Map<String, Object>> getPrescriptionItems(String mrNo, String bedType, String orgId,
      String patientType, String gender, Integer insPlanId, String presType, String tpaId,
      Integer centerId, String deptId, Integer age, String ageIn, Boolean invIsPrescribalble,
      String searchQuery, boolean isDischargeMedication) {

    if (presType.equals(MEDICINE)) {
      String healthAuthority = (String) centerService.findByKey(centerId).get("health_authority");
      Boolean generics = false;
      Boolean presFromStores = true;
      boolean isPhysicianOrder = patientType.equals("i") && !isDischargeMedication;
      if (!isPhysicianOrder) {
        Map<String, Object> filterMap = new HashMap<>();
        filterMap.put("health_authority", healthAuthority);
        generics = ((String) healthAuthorityPrefService.findByPk(filterMap)
            .get("prescriptions_by_generics")).equalsIgnoreCase("Y");

        presFromStores =
            ((String) genPrefService.getAllPreferences().get("prescription_uses_stores"))
                .equalsIgnoreCase("Y");
      }
      return ConversionUtils.listBeanToListMap(
          medicineItemService.getMedicinesForPrescription(generics, orgId, patientType, insPlanId,
              presFromStores, healthAuthority, centerId, searchQuery, ITEMS_LIMIT));

    } else if (presType.equals(INVESTIGATION)) {
      List<BasicDynaBean> invItems = investigationItemService.getTestsForPrescription(mrNo, bedType,
          orgId, patientType, insPlanId, centerId, tpaId, searchQuery, deptId, age, ageIn, gender,
          invIsPrescribalble, ITEMS_LIMIT);
      int itemsL = invItems.size();
      for (int i = 0; i < itemsL; i++) {
        BasicDynaBean invItem = invItems.get(i);
        if (invItem.get("last_conduction_date_time") != null
            && invItem.get("result_validity_period") != null
            && invItem.get("result_validity_period_units") != null
            && !(Boolean) invItems.get(i).get("is_package")) {
          Integer period = (Integer) invItem.get("result_validity_period");
          String unit = (String) invItem.get("result_validity_period_units");
          Timestamp conductedDate = (Timestamp) invItem.get("last_conduction_date_time");
          Calendar cal = Calendar.getInstance();
          cal.setTimeInMillis(conductedDate.getTime());
          if (unit.equals("D")) {
            cal.add(Calendar.DATE, period);
            invItem.set("order_auth_required",
                cal.getTimeInMillis() > new Date().getTime() ? "Y" : "N");
          } else if (unit.equals("M")) {
            cal.add(Calendar.MONTH, period);
            invItem.set("order_auth_required",
                cal.getTimeInMillis() > new Date().getTime() ? "Y" : "N");
          } else {
            cal.add(Calendar.YEAR, period);
            invItem.set("order_auth_required",
                cal.getTimeInMillis() > new Date().getTime() ? "Y" : "N");
          }
        }
      }
      return ConversionUtils.listBeanToListMap(invItems);
    } else if (presType.equals(SERVICE)) {
      return svrService.getServicesForPrescription(bedType, orgId, patientType, insPlanId,
          searchQuery, ITEMS_LIMIT);
    } else if (presType.equals(DOCTOR_TYPE)) {
      return docService.getDoctorsForPrescription(bedType, orgId, patientType, insPlanId,
          searchQuery, ITEMS_LIMIT);
    } else if (presType.equals(DEPT_TYPE)) {
      return departmentService.getDepartmentsForPrescription(searchQuery, ITEMS_LIMIT);
    } else if (presType.equals(OPERATION)) {
      return operService.getOperationsForPrescription(bedType, orgId, patientType, insPlanId,
          searchQuery, ITEMS_LIMIT);
    } else if (presType.equals(ORDER_SETS)) {
      return orderSetsService.getOrderSetsForPrescription(patientType, gender, centerId, deptId,
          DateUtil.getCurrentDate(), DateUtil.getCurrentDate(), searchQuery);
    }
    return null;
  }

  /**
   * Gets investigation conduction date.
   *
   * @param mrNo the string
   * @param idList the list of string
   * @return the list of map
   */
  @SuppressWarnings("unchecked")
  public List<Map<String, Object>> invConductionDateByIds(String mrNo, List<String> idList) {
    List<BasicDynaBean> invItems = investigationItemService.invConductionDateForIds(mrNo, idList);
    int itemsL = invItems.size();
    for (int i = 0; i < itemsL; i++) {
      BasicDynaBean invItem = invItems.get(i);
      if (invItem.get("last_conduction_date_time") != null
          && invItem.get("result_validity_period") != null
          && invItem.get("result_validity_period_units") != null
          && !(Boolean) invItems.get(i).get("is_package")) {
        Integer period = (Integer) invItem.get("result_validity_period");
        String unit = (String) invItem.get("result_validity_period_units");
        Timestamp conductedDate = (Timestamp) invItem.get("last_conduction_date_time");
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(conductedDate.getTime());
        if (unit.equals("D")) {
          cal.add(Calendar.DATE, period);
          invItem.set("order_auth_required",
              cal.getTimeInMillis() > new Date().getTime() ? "Y" : "N");
        } else if (unit.equals("M")) {
          cal.add(Calendar.MONTH, period);
          invItem.set("order_auth_required",
              cal.getTimeInMillis() > new Date().getTime() ? "Y" : "N");
        } else {
          cal.add(Calendar.YEAR, period);
          invItem.set("order_auth_required",
              cal.getTimeInMillis() > new Date().getTime() ? "Y" : "N");
        }
      }
    }
    return ConversionUtils.listBeanToListMap(invItems);
  }

  /**
   * Gets all prescription items.
   *
   * @param medicine (Ex: true if required)
   * @param test the boolean
   * @param dentalService the boolean
   * @param nonDentalService the boolean
   * @param operation the boolean
   * @param doctor the boolean
   * @param department the boolean
   * @param visitType the string
   * @param gender the string
   * @param orgId the string
   * @param centerId the string
   * @param deptId the string
   * @param searchQuery the string
   * @param isDischargeMedication indicator for discharge medication
   * @return the list of map
   */
  @SuppressWarnings("unchecked")
  public List<Map<String, Object>> getALLPrescriptionItems(Boolean medicine, Boolean test,
      Boolean dentalService, Boolean nonDentalService, Boolean operation, Boolean doctor,
      Boolean department, String visitType, String gender, String orgId, Integer centerId,
      String deptId,Integer age, String ageIn,Integer planId,
      String tpaId, String searchQuery, boolean isDischargeMedication) {

    String healthAuthority = (String) centerService.findByKey(centerId).get("health_authority");

    Boolean generics = false;
    Boolean presFromStores = true;

    if (!visitType.equals("i") || (visitType.equals("i") && isDischargeMedication)) {
      Map<String, Object> filterMap = new HashMap<>();
      filterMap.put("health_authority", healthAuthority);
      generics =
          ((String) healthAuthorityPrefService.findByPk(filterMap).get("prescriptions_by_generics"))
              .equalsIgnoreCase("Y");
      presFromStores = ((String) genPrefService.getAllPreferences().get("prescription_uses_stores"))
          .equalsIgnoreCase("Y");
    }
    if (isDischargeMedication) {
      return ConversionUtils.listBeanToListMap(presRepo.getAllMedicinePrescriptionItems(medicine,
          generics, presFromStores, searchQuery, healthAuthority, centerId, visitType));
    }
    return ConversionUtils.listBeanToListMap(
        presRepo.getAllPrescriptionItems(medicine, test, dentalService, nonDentalService, operation,
            doctor, generics, presFromStores, department, visitType, gender, orgId, centerId,
            deptId, age, ageIn, planId, tpaId, searchQuery, healthAuthority));
  }

  /**
   * Gets prescriptions.
   * @param consId the integer
   * @param bedType the string
   * @param orgId the string
   * @param tpaId the string
   * @return the list of map
   */
  @SuppressWarnings("unchecked")
  public List<Map<String, Object>> getPresriptions(Integer consId, String bedType, String orgId,
      String tpaId, Integer centerId, Boolean isDischargeMedication) {
    String healthAuthority = (String) centerService.findByKey(centerId).get("health_authority");
    Boolean presFromStores =
        ((String) genPrefService.getAllPreferences().get("prescription_uses_stores"))
            .equalsIgnoreCase("Y");
    if (isDischargeMedication) {
      return ConversionUtils.listBeanToListMap(presRepo
          .getDischargeMedicationWithCharges(consId, presFromStores, orgId, centerId,
              healthAuthority));
    }
    return ConversionUtils.listBeanToListMap(presRepo
        .getPrescriptionsWithCharges(consId, presFromStores, bedType, orgId, tpaId, centerId,
            healthAuthority));
  }

  /**
   * This is used by prior Authorization.
   * @param consId the integer
   * @param bedType the string
   * @param orgId the string
   * @return the list of map
   */
  @SuppressWarnings("unchecked")
  public List<Map<String, Object>> getPresriptions(Integer consId, String bedType, String orgId) {
    String showPriorAuthPresc = ((String) genPrefService.getAllPreferences()
        .get("show_prior_auth_presc"));
    return ConversionUtils.copyListDynaBeansToLinkedMap(
        presRepo.getPrescriptions(consId, bedType, orgId, showPriorAuthPresc));
  }
  
  /**
   * Gets prescriptions.
   * @param presIds the list of integer
   * @param bedType the string
   * @param orgId the string
   * @param tpaId the string
   * @param centerId the integer
   * @return list of map
   */
  @SuppressWarnings("unchecked")
  public List<Map<String, Object>> getPresriptions(List<Integer> presIds, String bedType,
      String orgId, String tpaId, Integer centerId, Boolean isDischargeMedication) {
    String healthAuthority = (String) centerService.findByKey(centerId).get("health_authority");
    Boolean presFromStores =
        ((String) genPrefService.getAllPreferences().get("prescription_uses_stores"))
            .equalsIgnoreCase("Y");
    if (isDischargeMedication) {
      return ConversionUtils.listBeanToListMap(presRepo.getDischargeMediPrescriptions(presIds,
          presFromStores, orgId, centerId, healthAuthority));
    }
    return ConversionUtils.listBeanToListMap(presRepo.getPrescriptions(presIds, presFromStores,
        bedType, orgId, tpaId, centerId, healthAuthority));
  }

  /**
   * Gets the all presriptions irrespective of the show_prior_auth_presc
   * preference.
   *
   * @param consId  the cons id
   * @param bedType the bed type
   * @param orgId   the org id
   * @return the all presriptions
   */
  @SuppressWarnings("unchecked")
  public List<Map<String, Object>> getAllPresriptions(Integer consId, String bedType,
      String orgId) {
    return ConversionUtils
        .copyListDynaBeansToLinkedMap(presRepo.getPrescriptions(consId, bedType, orgId, "A"));
  }

  /**
   * Gets prescriptions for the visit.
   * 
   * @param visitId the visitId
   * @param bedType the bedType
   * @param orgId the orgId
   * @param tpaId the tpaId
   * @return the list of prescriptions for the visit
   */
  @SuppressWarnings("unchecked")
  public List<Map<String, Object>> getPresriptionsForVisit(String visitId, String bedType,
      String orgId, String tpaId, Integer centerId, boolean isDischargeMedication) {
    String healthAuthority = (String) centerService.findByKey(centerId).get("health_authority");
    Boolean presFromStores =
        ((String) genPrefService.getAllPreferences().get("prescription_uses_stores"))
            .equalsIgnoreCase("Y");

    if (isDischargeMedication) {
      return ConversionUtils.listBeanToListMap(presRepo.getDischargeMedicationWithCharges(visitId,
          presFromStores, orgId, centerId, healthAuthority));
    }
    return Collections.emptyList();
  }

  /**
   * Inserts prescriptions.
   * @param requestBody the requestBody
   * @param errorMap the map
   * @param params the form param
   * @param generics the boolean 
   * @param usesStores the boolean
   * @param user the string
   * @param pbmPresId the integer
   * @param response the map
   * @param healthAuthority the string
   * @return the boolean value
   */
  @SuppressWarnings("unchecked")
  public boolean insert(Map<String, Object> requestBody, Map<String, Object> errorMap,
      FormParameter params, boolean generics, boolean usesStores, String user, Integer pbmPresId,
      Integer sectionId, Map<String, Object> response, String healthAuthority) {
    response.put("insert", new HashMap<String, Object>());
    List<BasicDynaBean> insertBeans = new ArrayList<>();
    Integer recordIndex = 0;
    String doctorId = null;
    
    // Adds a new record in patient_prescription_main table to maintain the doctor presc id
    BasicDynaBean patPrescMainRepoBean = patientPrescriptionsMainRepository.getBean();
    
    if (!params.getFormType().equals(FormComponentsService.FormType.Form_IP.toString())) {
      BasicDynaBean consBean = doctorConsultationService.getConsultation((Integer) params.getId());
      doctorId = (String) consBean.get("doctor_id");
      patPrescMainRepoBean.set("consultation_id", params.getId());
    }
    boolean isDischargeMedication = false;
    // if discharge medication then set is_discharge_medication to true
    if (sectionId == -22) {
      isDischargeMedication = true;
    }

    int patPrescMainId = patientPrescriptionsMainRepository.getNextSequence();
    patPrescMainRepoBean.set("doc_presc_id", patPrescMainId);
    patPrescMainRepoBean.set("visit_id", params.getPatientId());
    patPrescMainRepoBean.set("prescribing_doc_id", doctorId);
    patPrescMainRepoBean.set("created_by", user);
    patPrescMainRepoBean.set("created_at", requestBody.get("transaction_start_date"));
    
    ValidationErrorMap errMap = getValidationErrorMap();
    List<Map<String, Object>> prescriptions =
        (List<Map<String, Object>>) requestBody.get("insert");
    for (Map<String, Object> prescription : prescriptions) {
      String itemType = (String) prescription.get("item_type");
      if (!prescription.containsKey("adm_request_id")
              || prescription.get("adm_request_id") == null) {
        BasicDynaBean mainBean = presRepo.getBean();
        Boolean nonHospitalMedicine = (Boolean) prescription.get("non_hosp_medicine");

        List<String> conversionErrorList = new ArrayList<>();
        if (presValidator.validateInsert(prescription, generics, usesStores, params, sectionId,
                 errMap, healthAuthority)) {
          ConversionUtils.copyJsonToDynaBean(prescription, mainBean, conversionErrorList, true);
          Integer patientPresId = presRepo.getNextSequence();
          mainBean.set("patient_presc_id", patientPresId);
          mainBean.set(params.getFormType()
                  .equals(FormComponentsService.FormType.Form_IP.toString())
                  ? "visit_id"
                  : "consultation_id", params.getId());
          mainBean.set("status", "P");
          mainBean.set("presc_type", itemType);
          mainBean.set("store_item", usesStores
                  && itemType.equals(MEDICINE) && !nonHospitalMedicine);
          mainBean.set("username", user);
          mainBean.set("doc_presc_id", patPrescMainId);
          mainBean.set("created_by", user);
          mainBean.set("created_at", requestBody.get("transaction_start_date"));
          insertBeans.add(mainBean);
          prescription.put("visit_id", params.getPatientId());
          prescription.put("doctor_id", doctorId);
          prescription.put("generics", generics);
          prescription.put("pbm_id", pbmPresId);
          prescription.put("is_discharge_medication", isDischargeMedication);
          prescriptionItemFactory.getItemService(itemType, usesStores, nonHospitalMedicine)
                  .insert(prescription, mainBean, errMap);
          /*
           * patient_pending_prescription table is inserted only when
           * mod_pat_pending_prescription module is enabled
           */
          boolean modPatPendingPres = modulesActivatedService
                  .isModuleActivated("mod_pat_pending_prescription");
          if (modPatPendingPres && PENDING_PRESCRIPTION_TYPES.contains(itemType)) {
            pendingPrescriptionsService.insertPrescriptions(mainBean, prescription);
          }
        }
        if (!errMap.getErrorMap().isEmpty() || !conversionErrorList.isEmpty()) {
          if (!errorMap.containsKey("insert")) {
            errorMap.put("insert", new HashMap<String, Object>());
          }
          ((Map<String, Object>) errorMap.get("insert")).put(recordIndex.toString(),
                  (new ValidationException(
                          ValidationUtils.copyCoversionErrors(errMap, conversionErrorList)))
                          .getErrors());
          errMap.setErrorMap(new HashMap<String, Map<String, List<String>>>());
        } else if (params.getFormType().equals(FormComponentsService.FormType.Form_IP.toString())) {
          // activity related stuff
          try {
            if (MEDICINE.equals(itemType)) {
              try {
                if (prescription.get("priority").equals("P")
                    || prescription.get("priority").equals("S")) {
                  marService.marSetup(mainBean, params);
                }
              } catch (ValidationException exe) {
                log.error(exe.toString());
              }
            } else {
              patientActivitiesService.insert(mainBean, params.getPatientId(), user);
            }
          } catch (ParseException exe) {
            log.error(exe.toString());
          }
        }
        Map<String, Object> record = new HashMap<>();
        record.put("item_prescribed_id", mainBean.get("patient_presc_id"));
        record.put("prescribed_date", mainBean.get("prescribed_date"));
        record.put("erx_status", "O");
        ((Map<String, Object>) response.get("insert")).put(recordIndex.toString(), record);
        recordIndex++;
      } else if (params != null && !StringUtils.isEmpty(params.getMrNo())
              && !StringUtils.isEmpty(params.getPatientId())) {
        Map<String, Object> filterMap = new HashMap<>();
        filterMap.put("mr_no", params.getMrNo());
        filterMap.put("visit_id",params.getPatientId());
        filterMap.put("presc_type",itemType);
        BasicDynaBean mainBean = presRepo.findByKey(filterMap);
        if (mainBean != null) {
          updateForAdmReqPresc(mainBean,itemType,prescription,errorMap,params,
                  generics,usesStores,user,pbmPresId,sectionId,healthAuthority,requestBody);
          Map<String, Object> record = new HashMap<>();
          record.put("item_prescribed_id", mainBean.get("patient_presc_id"));
          record.put("prescribed_date", mainBean.get("prescribed_date"));
          record.put("erx_status", "O");
          ((Map<String, Object>) response.get("insert")).put(recordIndex.toString(), record);
          recordIndex++;
        }
      }
    }
    if (!insertBeans.isEmpty() && errorMap.isEmpty()) {
      patientPrescriptionsMainRepository.insert(patPrescMainRepoBean);
      presRepo.batchInsert(insertBeans);
      presRepo.batchInsertAuditLogs(insertBeans);
    }
    return true;
  }

  /**
   * Handling admission request updation seperately
   * Since adm req presc is already created in the db from
   * Admission Request Screen, here we are updating the
   * data in it.
   * @param mainBean the mainBean
   * @param itemType the string
   * @param prescription the map
   * @param errorMap the map
   * @param params the map
   * @param generics the boolean
   * @param usesStores the boolean
   * @param user the string
   * @param pbmPresId the int
   * @param healthAuthority the string
   * @param requestBody the requestbody
   */
  private void updateForAdmReqPresc(BasicDynaBean mainBean, String itemType,
                                    Map<String, Object> prescription, Map<String, Object> errorMap,
                                    FormParameter params,
                                    boolean generics, boolean usesStores, String user,
                                    Integer pbmPresId, Integer sectionId, String healthAuthority,
                                    Map<String, Object> requestBody) {
    if (MEDICINE.equals(itemType)) {
      patientActivitiesService
              .cancelActivity(Integer.parseInt(mainBean.get("patient_presc_id").toString()));
    }
    if (itemType != null && (MEDICINE.equals(itemType) || INVESTIGATION.equals(itemType)
            || SERVICE.equals(itemType))) {
      Map<String, Object> updateRequestBody = requestBody;
      List<Map<String, Object>> mapList = new ArrayList<>();
      mapList.add(prescription);
      updateRequestBody.put("update", mapList);
      updateRequestBody.remove("insert");
      update(updateRequestBody, errorMap, params, generics, usesStores, user, pbmPresId, sectionId,
              new HashMap<String, Object>(), healthAuthority);
    }
  }

  /**
   * Updates the prescription.
   * @param requestBody the requestBody
   * @param errorMap the map
   * @param params the map
   * @param generics the boolean
   * @param usesStores the boolean
   * @param user the string
   * @param pbmPresId the integer
   * @param response the map
   * @param healthAuthority the string
   * @return boolean value
   */
  @SuppressWarnings("unchecked")
  public boolean update(Map<String, Object> requestBody, Map<String, Object> errorMap,
      FormParameter params, boolean generics, boolean usesStores, String user, Integer pbmPresId,
      Integer sectionId, Map<String, Object> response, String healthAuthority) {

    List<Map<String, Object>> prescriptions = (List<Map<String, Object>>) requestBody.get("update");
    response.put("update", new HashMap<String, Object>());
    List<BasicDynaBean> updateBeans = new ArrayList<>();
    Map<String, Object> updateKeysMap = new HashMap<>();
    List<Integer> updateKeys = new ArrayList<>();
    ValidationErrorMap errMap = getValidationErrorMap();
    Integer recordIndex = 0;

    for (Map<String, Object> prescription : prescriptions) {
      BasicDynaBean mainBean = presRepo.getBean();
      Integer itemPrescribedId = (Integer) prescription.get("item_prescribed_id");
      String itemType = (String) prescription.get("item_type");
      Boolean nonHospitalMedicine = (Boolean) prescription.get("non_hosp_medicine");
      List<String> conversionErrorList = new ArrayList<>();
      if (presValidator.validateUpdate(prescription, generics, usesStores, params, sectionId,
          errMap, healthAuthority)) {
        ConversionUtils.copyJsonToDynaBean(prescription, mainBean, conversionErrorList, false);
        mainBean.set("username", user);
        mainBean.set("modified_by", user);
        mainBean.set("modified_at", getCurrentTimestamp());
        updateBeans.add(mainBean);
        updateKeys.add(itemPrescribedId);
        prescription.put("generics", generics);
        prescription.put("pbm_id", pbmPresId);

        prescriptionItemFactory.getItemService(itemType, usesStores, nonHospitalMedicine)
            .update(prescription, mainBean, errMap);
        // Pending prescriptions are updated only when mod_pat_pending_prescription
        // module is enabled
        boolean modPatPendingPres = modulesActivatedService
            .isModuleActivated("mod_pat_pending_prescription");
        if (modPatPendingPres) {
          pendingPrescriptionsService.updatePrescriptions(prescription);
        }
      }
      if (!errMap.getErrorMap().isEmpty() || !conversionErrorList.isEmpty()) {
        if (!errorMap.containsKey("update")) {
          errorMap.put("update", new HashMap<String, Object>());
        }
        ((Map<String, Object>) errorMap.get("update")).put(recordIndex.toString(),
            (new ValidationException(
                ValidationUtils.copyCoversionErrors(errMap, conversionErrorList))).getErrors());
        errMap.setErrorMap(new HashMap<String, Map<String, List<String>>>());
      } else if (params.getFormType().equals(FormComponentsService.FormType.Form_IP.toString())) {
        Map<String, Object> filterMap = new HashMap<>();
        filterMap.put("patient_presc_id", itemPrescribedId);
        mainBean.set("patient_presc_id", itemPrescribedId);
        mainBean.set("presc_type", itemType);
        if ("Y".equals(mainBean.get("discontinued"))) {
          patientActivitiesService.cancelActivity(itemPrescribedId,
              getItemTypesShortForm().get(itemType));
        } else if (isFreqNDurationModified(presRepo.findByKey(filterMap), mainBean)) {
          patientActivitiesService.cancelActivity(itemPrescribedId);
          try {
            if (!MEDICINE.equals(itemType)) {
              patientActivitiesService.insert(mainBean, params.getPatientId(), user);
            }
          } catch (Exception exe) {
            log.error("Unable to insert medicine type prescriptions. " + exe.getMessage());
          }
        }
      }
      Map<String, Object> record = new HashMap<>();
      record.put("item_prescribed_id", prescription.get("item_prescribed_id"));
      ((Map<String, Object>) response.get("update")).put(recordIndex.toString(), record);
      recordIndex++;
    }
    updateKeysMap.put("patient_presc_id", updateKeys);
    if (!updateBeans.isEmpty()) {
      presRepo.batchUpdate(updateBeans, updateKeysMap);
      presRepo.batchUpdateAuditLog(updateBeans, updateKeysMap);
    }
    return true;
  }

  /**
   * Deletes the prescription.
   * @param requestBody the requestBody
   * @param errorMap the map
   * @param params the form param
   * @param generics the boolean
   * @param usesStores the boolean
   * @param user the string
   * @param response the map
   * @return boolean value
   */
  @SuppressWarnings("unchecked")
  public boolean delete(Map<String, Object> requestBody, Map<String, Object> errorMap,
      FormParameter params, boolean generics, boolean usesStores, String user,
      Map<String, Object> response) {

    List<Map<String, Object>> prescriptions = (List<Map<String, Object>>) requestBody.get("delete");
    response.put("delete", new HashMap<String, Object>());
    ValidationErrorMap errMap = getValidationErrorMap();
    List<Object> deleteKeys = new ArrayList<>();
    Integer recordIndex = 0;

    for (Map<String, Object> prescription : prescriptions) {
      Integer itemPrescribedId = (Integer) prescription.get("item_prescribed_id");
      String itemType = (String) prescription.get("item_type");
      Boolean nonHospitalMedicine = (Boolean) prescription.get("non_hosp_medicine");
      if (presValidator.validateDelete(prescription, generics, usesStores, errMap)) {
        deleteKeys.add((Integer) prescription.get("item_prescribed_id"));

        prescriptionItemFactory.getItemService(itemType, usesStores, nonHospitalMedicine)
            .delete((Integer) prescription.get("item_prescribed_id"));
        if (params.getFormType().equals(FormComponentsService.FormType.Form_IP.toString())) {
          patientActivitiesService.deleteIncompleteActivity(itemPrescribedId,
              getItemTypesShortForm().get(itemType));
        }
      } else {
        if (!errorMap.containsKey("delete")) {
          errorMap.put("delete", new HashMap<String, Object>());
        }
        ((Map<String, Object>) errorMap.get("delete")).put(recordIndex.toString(),
            (new ValidationException(errMap)).getErrors());
        errMap.setErrorMap(new HashMap<String, Map<String, List<String>>>());
      }
      Map<String, Object> record = new HashMap<>();
      record.put("item_prescribed_id", prescription.get("item_prescribed_id"));
      ((Map<String, Object>) response.get("delete")).put(recordIndex.toString(), record);
      recordIndex++;
    }
    if (!deleteKeys.isEmpty()) {
      presRepo.batchDeleteAuditLog("patient_presc_id", deleteKeys);
      presRepo.batchDelete("patient_presc_id", deleteKeys);
      // Pending prescriptions are deleted only when mod_pat_pending_prescription
      // module is enabled
      boolean modPatPendingPres = modulesActivatedService
          .isModuleActivated("mod_pat_pending_prescription");
      if (modPatPendingPres) {
        List<Object> deletePendingPrescKeys = new ArrayList<>();
        for (Object key : deleteKeys) {
          deletePendingPrescKeys
              .addAll(pendingPrescriptionsRepository.getPendingPrescIds((int) key));
        }
        pendingPrescriptionsRemarksRepository.batchDelete("pending_prescription_id",
            deletePendingPrescKeys);
        pendingPrescriptionsRepository.batchDelete("pat_pending_presc_id", deletePendingPrescKeys);
      }
    }
    return true;
  }

  public Integer getErxConsPBMId(Object consId ) {
    return presRepo.getErxConsPBMId(consId);
  }

  public List<BasicDynaBean> getErxPrescribedActivities(int pbmPrescId, String healthAuthority) {
    return presRepo.getErxPrescribedActivities(pbmPrescId, healthAuthority);
  }

  /**
   * Gets recent prescription ids.
   * @param doctorId the string
   * @return the list of integer
   */
  public List<Integer> getRecentPrescriptionIds(String doctorId) {
    List<BasicDynaBean> records = presRepo.getRecentPrescriptionIds(doctorId);
    if (records != null) {
      List<Integer> ids = new ArrayList<>();
      for (BasicDynaBean bean : records) {
        ids.add((Integer) bean.get("patient_presc_id"));
      }
      return ids;
    }
    return new ArrayList<>();
  }

  private ValidationErrorMap getValidationErrorMap() {
    return new ValidationErrorMap();
  }

  @Override
  public Boolean deleteSection(Integer sectiondetailId, FormParameter parameter,
      Map<String, Object> errorMap) {
    return true;
  }

  /**
   * Gets prescriptions for prints.
   * @param consId the integer
   * @param bedType the string
   * @param orgId the string
   * @param centerId the integer
   * @return the list of basic dyna beans
   */
  public List<BasicDynaBean> getPrescriptionsForPrints(Integer consId, String bedType, String orgId,
      Integer centerId) {
    String healthAuthority = (String) centerService.findByKey(centerId).get("health_authority");
    Boolean presFromStores = ((String) genPrefService.getAllPreferences()
        .get("prescription_uses_stores")).equalsIgnoreCase("Y");
    return presRepo.getPrescriptionsForPrints(consId, bedType, orgId, centerId, healthAuthority,
        presFromStores);
  }

  public List<BasicDynaBean> getPhysicianOrdersForPrint(Object patientId, String bedType,
      String orgId, Integer centerId) {
    String healthAuthority = (String) centerService.findByKey(centerId).get("health_authority");
    return presRepo.getPrescriptions(patientId, bedType, orgId, centerId, healthAuthority, true);
  }

  /**
   * Gets prescribed items.
   * @param presList the list of basic dyna bean
   * @param itemType the string
   * @return the list
   */
  public static List getPrescribedItems(List<BasicDynaBean> presList, String itemType) {
    List itemTypeList = new ArrayList();
    if (presList != null && !presList.isEmpty()) {
      for (BasicDynaBean b : presList) {
        if (b.get("item_type").equals(itemType)) {
          itemTypeList.add(b);
        }
      }
    }
    return itemTypeList;
  }

  public List getAllPackageComponents(int consultationId) {
    return presRepo.getAllPackageComponents(consultationId);
  }

  private static final String[] TEMPLATE_EXCLUDE_FIELDS = { "erx_denial_code", "denial_desc",
      "example", "erx_denial_remarks", "erx_status", "cross_code_status", "cross_code_colour",
      "item_prescribed_id", "prescribed_date" };

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public void processTemplateData(FormParameter parameters, Map<String, Object> templateData,
      Map<String, Object> responseData, Integer formId) {
    if (((List<Map<String, Object>>) responseData.get("records")).isEmpty() && (templateData != null
        && !((List<Map<String, Object>>) templateData.get("records")).isEmpty())) {
      BasicDynaBean patientBean = regService.findByKey(parameters.getPatientId());
      String healthAuthority = (String) centerService.findByKey((int) patientBean.get("center_id"))
          .get("health_authority");
      Boolean presFromStores = true;
      boolean generics = false;
      if (!parameters.getFormType().equals(FormComponentsService.FormType.Form_IP.toString())) {
        presFromStores = ((String) genPrefService.getAllPreferences()
            .get("prescription_uses_stores")).equalsIgnoreCase("Y");
        Map<String, Object> filterMap = new HashMap<>();
        filterMap.put("health_authority", healthAuthority);
        generics = ((String) healthAuthorityPrefService.findByPk(filterMap)
            .get("prescriptions_by_generics")).equalsIgnoreCase("Y");

      }
      List<Map> templateRecords = (List<Map>) templateData.get("records");
      List<String> itemIds = new ArrayList<>();
      List<Map<String, Object>> responseRecords = new ArrayList<>();
      for (Map<String, Object> record : templateRecords) {
        if (MEDICINE.equals(record.get("item_type"))) {
          if (generics) {
            itemIds.add((String) record.get("generic_code"));
          } else if (presFromStores) {
            itemIds.add((String) record.get("item_id"));
          } else {
            itemIds.add((String) record.get("item_name"));
          }
        } else if (NON_HOSPITAL.equals(record.get("item_type"))) {
          responseRecords.add(record);
        } else {
          itemIds.add((String) record.get("item_id"));
        }
      }

      List<BasicDynaBean> prescriptions = presRepo.getTemplatePrescriptionsByIds(
          (String) patientBean.get("org_id"), (String) patientBean.get("bed_type"),
          (Integer) patientBean.get("center_id"), (String) patientBean.get("primary_sponsor_id"),
          healthAuthority, itemIds, presFromStores, generics);
      for (Map record : templateRecords) {
        Map<String, Object> temp = new HashMap<>();
        for (BasicDynaBean bean : prescriptions) {
          boolean isMatch = false;
          if (MEDICINE.equals(record.get("item_type"))) {
            if ((presFromStores || generics) && bean.get("item_id").equals(record.get("item_id"))) {
              isMatch = true;
            } else if (bean.get("item_name").equals(record.get("item_name"))
                && MEDICINE.equals(bean.get("item_type"))) {
              isMatch = true;
            }
          } else if (!NON_HOSPITAL.equals(record.get("item_type"))
              && bean.get("item_id").equals(record.get("item_id"))) {
            isMatch = true;
          }
          if (isMatch) {
            temp.putAll(record);
            temp.putAll(bean.getMap());
            responseRecords.add(temp);
            break;
          }
        }
      }

      for (int i = 0; i < responseRecords.size(); i++) {
        for (String key : TEMPLATE_EXCLUDE_FIELDS) {
          responseRecords.get(i).remove(key);
        }
      }

      responseData.put("records", responseRecords);
      responseData.put("isTemplateRecords", true);
    }
  }

  /**
   * Checks is freq and duration is modified.
   * @param dbean is retrieved from the database.
   * @param ubean is populated from the user input
   * @return booleans value
   */
  private boolean isFreqNDurationModified(BasicDynaBean dbean, BasicDynaBean ubean) {
    if (isNotEqual(ubean.get("freq_type"), dbean.get("freq_type"))) {
      return true;
    }
    if (isNotEqual(ubean.get("recurrence_daily_id"), dbean.get("recurrence_daily_id"))) {
      return true;
    }
    if (isNotEqual(ubean.get("repeat_interval"), dbean.get("repeat_interval"))) {
      return true;
    }
    if (ubean.get("freq_type").equals("R")
        && isNotEqual(ubean.get("repeat_interval_units"), dbean.get("repeat_interval_units"))) {
      return true;
    }
    if (isNotEqual(ubean.get("start_datetime"), dbean.get("start_datetime"))) {
      return true;
    }
    if (isNotEqual(ubean.get("end_datetime"), dbean.get("end_datetime"))) {
      return true;
    }
    if (isNotEqual(ubean.get("no_of_occurrences"), dbean.get("no_of_occurrences"))) {
      return true;
    }
    if (isNotEqual(ubean.get("end_on_discontinue"), dbean.get("end_on_discontinue"))) {
      return true;
    }

    return false;
  }

  /**
   * Checks two objects equal or not.
   * @param val1 the object1
   * @param val2 the object2
   * @return the boolean value
   */
  public boolean isNotEqual(Object val1, Object val2) {
    if (val1 == null && val2 != null) {
      return true;
    } else if (val1 != null && val2 == null) {
      return true;
    } else if (val1 == null && val2 == null) {
      return false;
    } else {
      if (val1 instanceof String) {
        return !val1.equals(val2);
      } else if (val1 instanceof Integer) {
        return ((Integer) val1).intValue() != ((Integer) val2).intValue();
      } else if (val1 instanceof java.sql.Timestamp) {
        return !val1.equals(val2);
      } else if (val1 instanceof BigDecimal) {
        return !val1.equals(val2);
      } else if (val1 instanceof java.sql.Date) {
        return !val1.equals(val2);
      }
      return false;
    }
  }

  public List<BasicDynaBean> getMedications(String patientId) {
    return presRepo.getMedications(patientId);
  }

  /**
   * Find by id.
   *
   * @param prescriptionId the prescription id
   * @return the basic dyna bean
   */
  public BasicDynaBean findById(Integer prescriptionId) {
    return presRepo.findByKey("patient_presc_id", prescriptionId);
  }
  
  private java.sql.Timestamp getCurrentTimestamp() {
    Calendar calendar = Calendar.getInstance();
    java.util.Date now = calendar.getTime();
    return new java.sql.Timestamp(now.getTime());
  }
}
