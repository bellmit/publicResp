package com.insta.hms.core.clinical.dischargemedication;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.security.SecurityService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.clinical.consultation.prescriptions.MedicineItemService;
import com.insta.hms.core.clinical.consultation.prescriptions.PrescriptionsRepository;
import com.insta.hms.core.clinical.consultation.prescriptions.PrescriptionsService;
import com.insta.hms.core.clinical.forms.FormParameter;
import com.insta.hms.core.clinical.forms.SystemSectionService;
import com.insta.hms.core.patient.registration.RegistrationService;
import com.insta.hms.integration.clinical.ceed.CeedIntegrationService;
import com.insta.hms.integration.insurance.pbm.PBMPrescriptionsService;
import com.insta.hms.mdm.centers.CenterService;
import com.insta.hms.mdm.healthauthoritypreferences.HealthAuthorityPreferencesService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class DischargeMedicationService.
 */
@Service
public class DischargeMedicationService extends SystemSectionService {

  public DischargeMedicationService() {
    this.sectionId = -22;
  }

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

  public static final String PRIORITY_CONSTANT_KEY = "constant.prescription.priority.";
  public static final String PRIORITY_CONSTANT_SHORT_KEY =
      "constant.prescription.priority.shortkey.";
  public static final String ITEM_TYPE_CONSTANT_SHORT_KEY =
      "constant.prescription.itemtype.shortkey.";
  public static final String CROSS_CODE_STATUS_CONSTANT_KEY =
      "constant.prescription.claim.edit.rank.";
  public static final String DURATION_UNITS_CONSTANT_KEY = "constant.prescription.duration.units.";
  
  private final String pbmPrescIdKey = "pbm_presc_id";

  @LazyAutowired
  private DischargeMedicationRepository repo;
  @LazyAutowired
  private SessionService sessionService;
  @LazyAutowired
  private HealthAuthorityPreferencesService healthAuthorityPreferencesService;
  @LazyAutowired
  private GenericPreferencesService genPrefService;
  @LazyAutowired
  private RegistrationService regService;
  @LazyAutowired
  private CenterService centerService;
  @LazyAutowired
  private PrescriptionsRepository presRepo;
  @LazyAutowired
  private SecurityService securityService;
  @LazyAutowired
  private CeedIntegrationService ceedIntegrationService;
  @LazyAutowired
  private PrescriptionsService prescService;
  @LazyAutowired
  private HealthAuthorityPreferencesService healthAuthorityPrefService;
  @LazyAutowired
  private MedicineItemService medicineItemService;
  @LazyAutowired
  private PBMPrescriptionsService pbmPrescService;
  @LazyAutowired
  private MessageUtil messageUtil;

  @SuppressWarnings("unchecked")
  @Override
  public Map<String, Object> getSectionDetailsFromCurrentForm(FormParameter parameters) {

    Boolean presFromStores =
        ((String) genPrefService.getAllPreferences().get("prescription_uses_stores"))
            .equalsIgnoreCase("Y");


    BasicDynaBean patientBean = regService.findByKey(parameters.getPatientId());
    String healthAuthority = (String) centerService.findByKey((int) patientBean.get("center_id"))
        .get("health_authority");
    List<Map<String, Object>> prescriptions = ConversionUtils
        .copyListDynaBeansToLinkedMap(presRepo.getDischargeMedications(parameters.getId(),
            (String) patientBean.get("bed_type"), (String) patientBean.get("org_id"),
            (Integer) patientBean.get("center_id"), healthAuthority, presFromStores));

    int prescriptionsL = prescriptions.size();
    for (int i = 0; i < prescriptionsL; i++) {
      Map<String, Object> prescription = prescriptions.get(i);
      String itemType = (String) prescription.get("item_type");
      prescription.put("cross_code_status", null);
      prescription.put("cross_code_colour", null);

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
   * Gets the discharge medication details.
   *
   * @param visitId the visit id
   * @return the discharge medication details
   */
  public List<BasicDynaBean> getDischargeMedicationDetails(String visitId, String visitType,
      String orgId) { // USED IN DISCHARGE SUMMARY
    Boolean presFromStores =
        ((String) genPrefService.getAllPreferences().get("prescription_uses_stores"))
            .equalsIgnoreCase("Y");
    return repo.getDischargeMedicationDetails(visitId,
        (String) healthAuthorityPreferencesService
            .listBycenterId(RequestContext.getCenterId())
            .get("health_authority"),
        presFromStores, visitType, orgId);
  }

  @Override
  public Map<String, Object> saveSection(Map<String, Object> requestBody, BasicDynaBean sdbean,
      FormParameter parameter, Map<String, Object> errorMap) {

    BasicDynaBean genPref = genPrefService.getAllPreferences();
    Map<String, Object> sessionnAttributes = sessionService
        .getSessionAttributes(new String[] {"userId", "mod_eclaim_pbm", "mod_eclaim_erx"});
    boolean modEclaimPbm = (Boolean) sessionnAttributes.get("mod_eclaim_pbm");
    boolean modEclaimErx = (Boolean) sessionnAttributes.get("mod_eclaim_erx");

    BasicDynaBean patientBean = regService.findByKey(parameter.getPatientId());
    String healthAuthority = (String) centerService.findByKey((int) patientBean.get("center_id"))
        .get("health_authority");

    Map<String, Object> filterMap = new HashMap<>();
    filterMap.put("health_authority", healthAuthority);
    boolean generics =
        ((String) healthAuthorityPrefService.findByPk(filterMap).get("prescriptions_by_generics"))
            .equalsIgnoreCase("Y");
    boolean usesStores = genPref.get("prescription_uses_stores").equals("Y");
    
    boolean sendForErx =
        requestBody.get("send_for_erx") == null ? false : (boolean) requestBody.get("send_for_erx");

    BasicDynaBean pbmPrescBean = prescService.getPbmPrescBean(parameter, modEclaimPbm, modEclaimErx,
        (Integer) patientBean.get("plan_id"), true, sendForErx);
    Integer pbmPresId = pbmPrescBean != null ? (Integer) pbmPrescBean.get(pbmPrescIdKey) : 0;
    boolean success = true;
    Map<String, Object> response = new HashMap<>();
    if (requestBody.get("insert") != null) {
      success &= prescService.insert(requestBody,
          errorMap, parameter, generics, usesStores, (String) sessionnAttributes.get("userId"),
          pbmPresId, (Integer) requestBody.get("section_id"), response, healthAuthority);
    }
    if (requestBody.get("update") != null) {
      success &= prescService.update(requestBody,
          errorMap, parameter, generics, usesStores, (String) sessionnAttributes.get("userId"),
          pbmPresId, (Integer) requestBody.get("section_id"), response, healthAuthority);
    }
    if (requestBody.get("delete") != null) {
      success &=
          prescService.delete(requestBody, errorMap,
              parameter, generics, usesStores, (String) sessionnAttributes.get("userId"), response);
    }
    // Update drug count in PBM Prescription.
    if (pbmPresId != 0) {
      //In case of IP it is visitId and Op it is consultationId
      medicineItemService.attachPbmToERxDischargeMedication(parameter.getId(), pbmPresId);
      Map<String, Object> pbmKeysMap = new HashMap<>();
      pbmKeysMap.put("pbm_presc_id", pbmPresId);
      Map<String, Object> pbmFieldsMap = new HashMap<>();
      int drugCount = (medicineItemService.listAll(pbmKeysMap)).size();
      pbmFieldsMap.put("drug_count", drugCount);
      success &= pbmPrescService.update(pbmFieldsMap, pbmKeysMap);
      success &= success
          ? prescService.updatePbmMedicinePrescriptions(pbmPrescBean, parameter, healthAuthority,
              drugCount)
          : false;
    }

    if (success) {
      return response;
    }
    return null;
  }

  @Override
  public Boolean deleteSection(Integer sectiondetailId, FormParameter parameter,
      Map<String, Object> errorMap) {
    return true;
  }


  @Override
  public Map<String, Object> getSectionDetailsFromLastSavedForm(FormParameter parameter) {
    Map<String, Object> data = new HashMap<>();
    data.put("records", new ArrayList<Map<String, Object>>());
    return data;
  }
}
