package com.insta.hms.core.clinical.eauthorization;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.core.billing.BillChargeService;
import com.insta.hms.core.clinical.consultation.prescriptions.PendingPrescriptionsRepository;
import com.insta.hms.core.clinical.consultation.prescriptions.PendingPrescriptionsService;
import com.insta.hms.core.clinical.consultation.prescriptions.PrescriptionsService;
import com.insta.hms.core.patient.registration.PatientInsurancePlansService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class EAuthorizationService.
 *
 * @author teja
 */
@Service
public class EAuthorizationService {

  /** The e auth prescription repo. */
  @LazyAutowired
  private EAuthPrescriptionRepository eauthPrescriptionRepo;

  /** The e auth prescription activities repo. */
  @LazyAutowired
  private EAuthPrescriptionActivitiesRepository eauthPrescriptionActivitiesRepo;

  /** The e auth activities observations repo. */
  @LazyAutowired
  private EAuthActivitiesObservationsRepository eauthActivitiesObservationsRepo;

  /** The generic preference service. */
  @LazyAutowired
  private GenericPreferencesService genericPreferenceService;
  
  @LazyAutowired
  private BillChargeService billChargeService;
  
  /** The Pending Prescriptions Repository. */
  @LazyAutowired
  private PendingPrescriptionsService pendingPrescriptionsService;
  
  @LazyAutowired
  private PatientInsurancePlansService patientInsurancePlansService;

  /**
   * Gets the latest E auth presc id.
   *
   * @param consId
   *          the cons id
   * @param insuranceCompanyId
   *          the insurance company id
   * @return the latest E auth presc id
   */
  public Integer getLatestEAuthPrescId(String visitId,Integer consId, String insuranceCompanyId) {
    return eauthPrescriptionRepo.getLatestEAuthPrescId(visitId, consId, insuranceCompanyId);
  }

  /**
   * Gets the activities.
   *
   * @param insuranceCompanyId
   *          the insurance company id
   * @param consId
   *          the cons id
   * @return the activities
   */
  public List<BasicDynaBean> getActivities(String insuranceCompanyId, Integer consId) {
    return eauthPrescriptionActivitiesRepo.getActivities(insuranceCompanyId, consId);
  }

  /**
   * Gets the e auth presc sequence id.
   *
   * @param userName
   *          the user name
   * @param consId
   *          the cons id
   * @param insuranceCompanyId
   *          the insurance company id
   * @return the e auth presc sequence id
   */
  public Integer getEAuthPrescSequenceId(String userName, Integer consId,
      String insuranceCompanyId) {
    Integer preauthPrescId = eauthPrescriptionRepo.getNextSequence();
    BasicDynaBean bean = eauthPrescriptionRepo.getBean();
    bean.set("preauth_presc_id", preauthPrescId);
    bean.set("preauth_cons_id", consId);
    bean.set("username", userName);
    bean.set("preauth_status", "O");
    bean.set("preauth_payer_id", insuranceCompanyId);
    if (eauthPrescriptionRepo.insert(bean) > 0) {
      return preauthPrescId;
    }
    return 0;
  }

  /**
   * Copy pre auth presc attributes.
   *
   * @param visitId
   *          the visit id
   * @param username
   *          the username
   * @param item
   *          the item
   * @param bean
   *          the bean
   * @param preauthPrescId
   *          the preauth presc id
   * @param consultationId
   *          the consultation id
   * @param opt
   *          the opt
   */
  private void copyPreAuthPrescAttributes(String visitId, String username,
      Map<String, Object> item, BasicDynaBean bean, int preauthPrescId, Integer consultationId,
      String opt) {

    if (opt.equals("insert")) {
      bean.set("preauth_mode", 0);
      bean.set("preauth_act_id", eauthPrescriptionActivitiesRepo.getNextSequence());
      bean.set("consultation_id", consultationId);
      bean.set("preauth_act_item_id", item.get("item_id"));
      bean.set("prescribed_date", DateUtil.getCurrentTimestamp());
      bean.set("patient_pres_id", item.get("patient_presc_id"));
      bean.set("visit_id", visitId);
      bean.set("act_code", item.get("item_code"));
      bean.set("act_code_type", item.get("code_type"));
      bean.set("doc_cons_type", -1);
      bean.set("preauth_presc_id", preauthPrescId);
    }
    bean.set("act_qty", item.get("item_qty"));
    bean.set("rem_qty", item.get("item_qty"));
    bean.set("mod_time", DateUtil.getCurrentTimestamp());
    bean.set("username", username);
    bean.set("preauth_act_item_remarks", item.get("item_remarks"));
    bean.set("preauth_required", item.get("preauth_required"));
    bean.set("preauth_act_type", null);
    bean.set("tooth_unv_number", null);
    bean.set("tooth_fdi_number", null);

    String itemType = (String) item.get("item_type");
    if (itemType.equals(PrescriptionsService.INVESTIGATION)) {
      bean.set("preauth_act_type", "DIA");
    } else if (itemType.equals(PrescriptionsService.SERVICE)) {
      bean.set("preauth_act_type", "SER");
      bean.set("tooth_unv_number", item.get("tooth_unv_number"));
      bean.set("tooth_fdi_number", item.get("tooth_fdi_number"));

    } else if (itemType.equals(PrescriptionsService.OPERATION)) {
      bean.set("preauth_act_type", "OPE");

    } else if (itemType.equals(PrescriptionsService.DOCTOR)) {
      bean.set("preauth_act_type", "DOC");
    }
    BigDecimal itemQty = new BigDecimal((Integer) bean.get("act_qty"));
    BigDecimal amount = ((BigDecimal) item.get("charge")).subtract(
        ((BigDecimal) item.get("discount"))).multiply(itemQty);
    bean.set("amount", amount);
    bean.set("rate", item.get("charge"));
    bean.set("discount", ((BigDecimal) item.get("discount")).multiply(itemQty));
    bean.set("claim_net_amount", item.get("insurance_claim_amt"));
    bean.set("patient_share", (amount.subtract((BigDecimal) item.get("insurance_claim_amt"))));
  }
  
  private void copyPreAuthPrescAttributes(BasicDynaBean billCharge,
      BasicDynaBean preAuthPrescActivity, Map<String, Object> params) {

    final String visitId = (String) params.get("visit_id");
    final Integer preauthPrescId = (Integer) params.get("preauth_presc_id");
    final String username = (String) params.get("username");
    Integer consultationId = (Integer) params.get("consultation_id");
    Integer patientPrescId = (Integer) billCharge.get("doc_presc_id");
    if (null == patientPrescId) {
      patientPrescId = Boolean.TRUE.equals(params.get("is_autogenerated")) ? -1 : 0;
    }

    if (null == consultationId) {
      consultationId = 0;
    }
    
    String chargeGroup = (String) billCharge.get("charge_group");
    String activityType = chargeGroup;
    // For services the charge head is SNP but pre auth table expects SER to show the name.
    if ("SNP".equals(chargeGroup)) {
      activityType = "SER";
    }

    preAuthPrescActivity.set("preauth_mode", 0);
    preAuthPrescActivity.set("preauth_act_id", eauthPrescriptionActivitiesRepo.getNextSequence());
    preAuthPrescActivity.set("preauth_act_item_id", billCharge.get("act_description_id"));
    preAuthPrescActivity.set("prescribed_date", DateUtil.getCurrentTimestamp());
    preAuthPrescActivity.set("patient_pres_id", patientPrescId);
    preAuthPrescActivity.set("visit_id", visitId);
    preAuthPrescActivity.set("doc_cons_type", -1);
    preAuthPrescActivity.set("preauth_presc_id", preauthPrescId);
    preAuthPrescActivity.set("consultation_id", consultationId);
    Integer actQty = 0;
    if (billCharge.get("act_quantity") != null
        && !billCharge.get("act_quantity").toString().equals("")) {
      actQty = (int) Double.parseDouble(billCharge.get("act_quantity").toString());
    }
    preAuthPrescActivity.set("act_qty", actQty);
    preAuthPrescActivity.set("act_code", billCharge.get("act_rate_plan_item_code"));
    preAuthPrescActivity.set("act_code_type", billCharge.get("code_type"));
    preAuthPrescActivity.set("rem_qty", 0);
    preAuthPrescActivity.set("mod_time", DateUtil.getCurrentTimestamp());
    preAuthPrescActivity.set("username", username);
    preAuthPrescActivity.set("preauth_act_item_remarks", billCharge.get("act_remarks"));
    preAuthPrescActivity.set("preauth_required", "Y");
    preAuthPrescActivity.set("preauth_act_type", activityType);
    preAuthPrescActivity.set("tooth_unv_number", null);
    preAuthPrescActivity.set("tooth_fdi_number", null);
    preAuthPrescActivity.set("added_to_bill", "Y");

    BigDecimal amount = (BigDecimal) billCharge.get("amount");
    BigDecimal insuranceClaimAmount = (BigDecimal) billCharge.get("insurance_claim_amount");
    preAuthPrescActivity.set("amount", amount);
    preAuthPrescActivity.set("rate", billCharge.get("act_rate"));
    preAuthPrescActivity.set("discount", billCharge.get("discount"));
    preAuthPrescActivity.set("claim_net_amount", insuranceClaimAmount);
    preAuthPrescActivity.set("patient_share", (amount.subtract(insuranceClaimAmount)));
  }

  /**
   * Insert E auth.
   *
   * @param prescriptions
   *          the prescriptions
   * @param patientId
   *          the patient id
   * @param consultationId
   *          the consultation id
   * @param username
   *          the username
   * @param preauthPrescId
   *          the preauth presc id
   * @return true, if successful
   */
  public boolean insertEAuth(List<Map<String, Object>> prescriptions, String patientId,
      Integer consultationId, String username, Integer preauthPrescId) {
    List<BasicDynaBean> eauthPrescriptionActivitieBeans = new ArrayList<>();
    List<BasicDynaBean> observationBeans = new ArrayList<>();
    List<Integer> pendingPrescriptionIds = new ArrayList<>();
    for (Map<String, Object> presItem : prescriptions) {
      BasicDynaBean eauthPrescriptionActivitieBean = eauthPrescriptionActivitiesRepo.getBean();
      copyPreAuthPrescAttributes(patientId, username, presItem, eauthPrescriptionActivitieBean,
          preauthPrescId, consultationId, "insert");
      pendingPrescriptionIds.add(Integer.parseInt(presItem.get("patient_presc_id").toString()));
      eauthPrescriptionActivitieBeans.add(eauthPrescriptionActivitieBean);
      if (PrescriptionsService.SERVICE.equals((String) presItem.get("item_type"))) {
        observationBeans.add(eauthPrescriptionActivitieBean);
      }
    }
    boolean success = eauthPrescriptionActivitiesRepo
        .isBatchSuccess(eauthPrescriptionActivitiesRepo
            .batchInsert(eauthPrescriptionActivitieBeans));
    if (success) {
      pendingPrescriptionsService.updatePreauthActivityId(pendingPrescriptionIds);
    }
    if (!observationBeans.isEmpty()) {
      success &= insertOrUpdateEAuthActivityObservations(observationBeans, "insert");
    }
    return success;
  }
  
  /**
   * Insert E auth of already ordered items.
   *
   * @param billCharges
   *          the list of bill charges
   * @param visitId
   *          the visit id
   * @param username
   *          the username
   * @param preauthPrescId
   *          the preauth presc id
   * @return true, if successful
   */
  public boolean insertEAuth(List<BasicDynaBean> billCharges, String visitId, String username,
      Integer preauthPrescId, Integer consultationId) {

    // can add is_autogenerated flag here

    List<BasicDynaBean> eauthPrescriptionActivitieBeans = new ArrayList<>();
    Map<String, Object> params = new HashMap<>();
    List<Integer> prescIds = new ArrayList<>();
    params.put("visit_id", visitId);
    params.put("consultation_id", consultationId);
    params.put("preauth_presc_id", preauthPrescId);
    params.put("username", username);
    params.put("is_autogenerated", true);
    for (BasicDynaBean charge : billCharges) {
      Integer prescriptionId = (Integer) charge.get("doc_presc_id");
      if (prescriptionId != null && !prescriptionId.equals(0)) {
        prescIds.add(prescriptionId);
      }
      BasicDynaBean eauthPrescriptionActivitieBean = eauthPrescriptionActivitiesRepo.getBean();
      copyPreAuthPrescAttributes(charge, eauthPrescriptionActivitieBean, params);
      eauthPrescriptionActivitieBeans.add(eauthPrescriptionActivitieBean);
      // Have to update the preauth_act_id in the bill charge table. 
      charge.set("preauth_act_id", eauthPrescriptionActivitieBean.get("preauth_act_id"));
      
    }
    
    boolean success = eauthPrescriptionActivitiesRepo.isBatchSuccess(
        eauthPrescriptionActivitiesRepo.batchInsert(eauthPrescriptionActivitieBeans));
    
    // If there are items ordered with prescription then update the prior auth id in ppd table.
    if (success && !prescIds.isEmpty()) {
      pendingPrescriptionsService.updatePreauthActivityId(prescIds);
    }
    
    // The bill charges to be updated after the pre auth activities are inserted.
    for (BasicDynaBean charge : billCharges) {
      String chargeId = (String)charge.get("charge_id");
    
      Map<String, Object> updateMap = new HashMap<>();
      updateMap.put("preauth_act_id", charge.get("preauth_act_id"));
      
      // This method is called instead of the basicDynaBeaen one as the billCharges may contain
      // additional columns like doc_presc_id which are not in bill_charge table.
      billChargeService.update(updateMap, chargeId);
    }
    
    return success;
  }

  /**
   * Update E auth.
   *
   * @param prescriptions
   *          the prescriptions
   * @param patientId
   *          the patient id
   * @param consultationId
   *          the consultation id
   * @param username
   *          the username
   * @param preauthPrescId
   *          the preauth presc id
   * @param eauthPrescriptionActivitieBeansMap
   *          the e auth prescription activitie beans map
   * @return true, if successful
   */
  public boolean updateEAuth(List<Map<String, Object>> prescriptions, String patientId,
      Integer consultationId, String username, Integer preauthPrescId,
      Map<String, BasicDynaBean> eauthPrescriptionActivitieBeansMap) {

    List<BasicDynaBean> eauthPrescriptionActivitieBeans = new ArrayList<BasicDynaBean>();
    List<BasicDynaBean> observationBeans = new ArrayList<BasicDynaBean>();
    List<Integer> preauthActIds = new ArrayList<Integer>();
    List<Integer> pendingPrescriptionIds = new ArrayList<>();
    for (Map<String, Object> presItem : prescriptions) {
      BasicDynaBean eauthPrescriptionActivitieBean = eauthPrescriptionActivitieBeansMap
          .get(presItem.get("patient_presc_id"));
      copyPreAuthPrescAttributes(patientId, username, presItem, eauthPrescriptionActivitieBean,
          preauthPrescId, consultationId, "update");
      pendingPrescriptionIds.add((Integer)presItem.get("patient_presc_id"));
      eauthPrescriptionActivitieBeans.add(eauthPrescriptionActivitieBean);
      preauthActIds.add((Integer) eauthPrescriptionActivitieBean.get("preauth_act_id"));
      if (PrescriptionsService.SERVICE.equals((String) presItem.get("item_type"))) {
        observationBeans.add(eauthPrescriptionActivitieBean);
      }
    }
    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("preauth_act_id", preauthActIds);
    boolean success = eauthPrescriptionActivitiesRepo
        .isBatchSuccess(eauthPrescriptionActivitiesRepo.batchUpdate(
            eauthPrescriptionActivitieBeans, keys));
    if (success) {
      pendingPrescriptionsService.updatePreauthActivityId(pendingPrescriptionIds);
    }
    if (!observationBeans.isEmpty()) {
      success &= insertOrUpdateEAuthActivityObservations(observationBeans, "update");
    }
    return success;
  }

  /**
   * Delete E auth.
   *
   * @param deletePrescriptionIds
   *          the delete prescription ids
   * @param userName
   *          the user name
   * @param consId
   *          the cons id
   * @return true, if successful
   */
  @SuppressWarnings("unchecked")
  public boolean deleteEAuth(List<Integer> deletePrescriptionIds, String userName, Integer consId) {
    Map<String, Object> filter = new HashMap<>();
    filter.put("consultation_id", consId);
    Map<Integer, BasicDynaBean> eauthPrescriptionActivitieBeansMap = ConversionUtils
        .listBeanToMapListBean(
            eauthPrescriptionActivitiesRepo.listAll(new ArrayList<String>(), filter, null),
            "patient_pres_id");
    List<BasicDynaBean> beans = new ArrayList<>();
    List<Integer> preauthActIds = new ArrayList<>();
    for (Integer presId : deletePrescriptionIds) {
      List<BasicDynaBean> temp = (List<BasicDynaBean>) eauthPrescriptionActivitieBeansMap
          .get(presId);
      if (temp == null) {
        continue;
      }
      int tempLen = temp.size();
      for (int i = 0; i < tempLen; i++) {
        BasicDynaBean bean = temp.get(i);
        bean.set("username", userName);
        bean.set("mod_time", DateUtil.getCurrentTimestamp());
        bean.set("status", "X");
        bean.set("preauth_required", "N");
        bean.set("patient_pres_id", presId);
        beans.add(bean);
        preauthActIds.add((Integer) bean.get("preauth_act_id"));

      }
    }
    Map<String, Object> keys = new HashMap<>();
    keys.put("preauth_act_id", preauthActIds);
    if (!beans.isEmpty()) {
      return eauthPrescriptionActivitiesRepo.isBatchSuccess(eauthPrescriptionActivitiesRepo
          .batchUpdate(beans, keys));
    }
    return true;
  }
  
  /**
   * Delete E auth of ordered item.
   *
   * @param preauthActIds the preauth act ids
   * @param userName the user name
   * @return true, if successful
   */
  public boolean deleteEAuthOfOrderedItem(List<Integer> preauthActIds, String userName) {
    Map<Integer, BasicDynaBean> eauthPrescriptionActivitieBeansMap = ConversionUtils
        .listBeanToMapBean(eauthPrescriptionActivitiesRepo.getActivities(preauthActIds),
            "preauth_act_id");
    List<BasicDynaBean> beans = new ArrayList<>();
    for (Integer preauthActId : preauthActIds) {
      BasicDynaBean bean = eauthPrescriptionActivitieBeansMap.get(preauthActId);
      bean.set("username", userName);
      bean.set("mod_time", DateUtil.getCurrentTimestamp());
      bean.set("status", "X");
      bean.set("preauth_required", "N");
      beans.add(bean);
    }
    Map<String, Object> keys = new HashMap<>();
    keys.put("preauth_act_id", preauthActIds);
    if (!preauthActIds.isEmpty()) {
      return eauthPrescriptionActivitiesRepo
          .isBatchSuccess(eauthPrescriptionActivitiesRepo.batchUpdate(beans, keys));
    }
    return true;
  }

  /**
   * Insert or update E auth activity observations.
   *
   * @param preAuthBeans
   *          the pre auth beans
   * @param opt
   *          the opt
   * @return true, if successful
   */
  public boolean insertOrUpdateEAuthActivityObservations(List<BasicDynaBean> preAuthBeans,
      String opt) {
    String toothNumberingSystem = (String) genericPreferenceService.getAllPreferences().get(
        "tooth_numbering_system");
    List<BasicDynaBean> beans = new ArrayList<BasicDynaBean>();
    for (BasicDynaBean preAuthBean : preAuthBeans) {
      if (opt.equals("update")) {
        eauthActivitiesObservationsRepo.delete("preauth_act_id", preAuthBean.get("preauth_act_id"));
      }
      String toothNumbers = null;
      if (toothNumberingSystem.equals("U")) {
        toothNumbers = (String) preAuthBean.get("tooth_unv_number");
      } else {
        toothNumbers = (String) preAuthBean.get("tooth_fdi_number");
      }
      if (toothNumbers != null && !"".equals(toothNumbers)) {
        String[] toothNumbersList = toothNumbers.split(",");
        for (String toothNumber : toothNumbersList) {
          BasicDynaBean bean = eauthActivitiesObservationsRepo.getBean();
          bean.set("preauth_act_id", (Integer) preAuthBean.get("preauth_act_id"));
          bean.set("obs_type", "Universal Dental");
          bean.set("code", toothNumber);
          bean.set("value", "");
          bean.set("value_type", "");
          beans.add(bean);
        }
      }
    }
    if (!beans.isEmpty()) {
      return eauthActivitiesObservationsRepo.isBatchSuccess(eauthActivitiesObservationsRepo
          .batchInsert(beans));
    }
    return true;
  }
  
  /**
   * Uncheck previously checked prescriptions for prior auth. This method updates prior auth
   * required to N for all prior auth activities of that consultation. This is done only if the
   * preference insurance plans enable_pre_authorized_limit is Y.
   *
   * @param consultationId
   *          the consultation id
   */
  public void uncheckPreviouslyCheckedPrescriptionsForPriorAuth(String visitId,
      int consultationId) {
    BasicDynaBean insurancePlanInfo = patientInsurancePlansService.getPatInsuranceInfo(visitId);
    if (!"Y".equals(insurancePlanInfo.get("enable_pre_authorized_limit"))) {
      return;
    }
    if (consultationId != 0) {
      eauthPrescriptionActivitiesRepo
          .uncheckPreviouslyCheckedPrescriptionsForPriorAuth(consultationId);
    }
  }

  /**
   * Gets the preauth act ids of ordered items.
   *
   * @param visitId the visit id
   * @return the preauth act ids of ordered items
   */
  public List<Integer> getPreauthActIdsOfOrderedItems(String visitId) {
    List<Integer> preauthIds = new ArrayList<>();
    for (BasicDynaBean bean : eauthPrescriptionActivitiesRepo.getPreauthActIds(visitId)) {
      preauthIds.add((Integer) bean.get("preauth_act_id"));
    }
    return preauthIds;
  }
}
