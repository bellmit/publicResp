package com.insta.hms.core.clinical.consultation.prescriptions;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.billing.BillService;
import com.insta.hms.core.clinical.eauthorization.EAuthorizationService;
import com.insta.hms.core.clinical.forms.FormParameter;
import com.insta.hms.core.clinical.order.master.OrderService;
import com.insta.hms.core.patient.registration.PatientInsurancePlansService;
import com.insta.hms.core.patient.registration.RegistrationService;
import com.insta.hms.insurance.AdvanceInsuranceHelper;
import com.insta.hms.mdm.services.ServicesService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author teja.
 *
 */
@Component
public class PrescriptionEAuthorization extends PrescriptionIntegratorServie {

  @LazyAutowired
  private PrescriptionsService prescriptionService;
  @LazyAutowired
  private EAuthorizationService eauthService;
  @LazyAutowired
  private ServicesService serviceService;
  @LazyAutowired
  private RegistrationService regService;
  @LazyAutowired
  private BillService billService;
  @LazyAutowired
  private PatientInsurancePlansService patientInsurancePlansService;
  @LazyAutowired
  private SessionService sessionService;
  @LazyAutowired
  private MessageUtil msgUtil;
  @LazyAutowired
  private OrderService orderService;

  Logger log = LoggerFactory.getLogger(PrescriptionEAuthorization.class);

  /**
   * Initiates.
   * @param data the data
   * @param params the param
   * @param errMap the error map
   * @return map
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> initiate(Map<String, Object> data, FormParameter params,
      Map<String, Object> errMap) {

    int[] insurancePlans = patientInsurancePlansService.getPlanIds(params.getPatientId());
    
    int primaryPlanId = 0;
    if (null != insurancePlans && insurancePlans.length > 0) {
      primaryPlanId = insurancePlans[0];
    }
    boolean success = true;
    Map<String, Object> returnData = new HashMap<String, Object>();
    if (insurancePlans == null) {
      returnData.put("success", success);
      return returnData;
    }
    BasicDynaBean patientBean = regService.findByKey(params.getPatientId());
    List<Map<String, Object>> prescriptions = prescriptionService.getPresriptions(
        (Integer) params.getId(), (String) patientBean.get("bed_type"),
        (String) patientBean.get("org_id"));
    Map<String, Object> estimateAmountReqBody = new HashMap<>();
    Map<String, Object> visit = new HashMap<>();
    visit.put("visit_id", params.getPatientId());
    estimateAmountReqBody.put("visit", visit);
    List<Map<String, Object>> orderedItems = new ArrayList<>();
    List<Map<String, Object>> priorAuthPrescriptions = new ArrayList<>();

    for (Map<String, Object> presItem : prescriptions) {

      if (!presItem.get("status").equals("P")) {
        continue;
      }

      priorAuthPrescriptions.add(presItem);
      Map<String, Object> orderedItem = new HashMap<String, Object>();
      orderedItem.put("amount",
          ((BigDecimal) presItem.get("charge")).subtract((BigDecimal) presItem.get("discount"))
              .toString());
      orderedItem.put("insurance_category_id",
          ((Integer) presItem.get("insurance_category_id")).toString());
      orderedItem.put("discount", ((BigDecimal) presItem.get("discount")).toString());
      orderedItem.put("new", "Y");
      orderedItem.put("act_description_id", presItem.get("item_id"));
      orderedItem.put("op_id", presItem.get("item_id"));
      orderedItem.put("tax_amt", "0.00");
      orderedItem.put("consultation_type_id", -1);
      String itemType = (String) presItem.get("item_type");
      if (itemType.equals(PrescriptionsService.SERVICE)) {
        BigDecimal itemQty = new BigDecimal((Integer) presItem.get("item_qty"));
        orderedItem.put("amount",
            ((BigDecimal) presItem.get("charge"))
                .subtract((BigDecimal) presItem.get("discount")).multiply(itemQty).toString());
        orderedItem.put("discount",
            ((BigDecimal) ((BigDecimal) presItem.get("discount")).multiply(itemQty)).toString());
        orderedItem.put("type", "Service");
        orderedItem.put("charge_head", "SERSNP");
        orderedItem.put("charge_group", "SNP");
      } else if (itemType.equals(PrescriptionsService.OPERATION)) {
        orderedItem.put("type", "Operation");
        orderedItem.put("charge_head", "SACOPE");
        orderedItem.put("charge_group", "OPE");
      } else if (itemType.equals(PrescriptionsService.INVESTIGATION)) {
        orderedItem.put("type",
            presItem.get("category").equals("DEP_LAB") ? "Labaratory" : "Radiology");
        orderedItem.put("charge_head",
            presItem.get("category").equals("DEP_LAB") ? "LTDIA" : "RTDIA");
        orderedItem.put("charge_group", "DIA");
      } else if (itemType.equals(PrescriptionsService.DOCTOR)) {
        orderedItem.put("type", "Doctor");
        orderedItem.put("charge_head", "OPDOC");
        orderedItem.put("charge_group", "DOC");
      }
      orderedItems.add(orderedItem);
    }

    estimateAmountReqBody.put("ordered_items", orderedItems);
    Map<String, Object> claimEstimatedAmount = null;
    try {
      claimEstimatedAmount = billService.estimateAmount(estimateAmountReqBody);
    } catch (Exception exception) {
      log.error("Prescriptions Insurance Calculation", exception);
      errMap.put("preAuth", msgUtil.getMessage("exception.prescription.preauth.initiation.failed"));
      returnData.put("success", false);
      return returnData;
    }
    List<Integer> newPrescriptionIds = (List<Integer>) data.get("insert_ids");
    String username = (String) sessionService.getSessionAttributes().get("userId");
    int priorAuthPrescriptionsl = priorAuthPrescriptions.size();
    List<Map<String, Object>> insertItems;
    List<Map<String, Object>> updateItems;
    if (primaryPlanId > 0) {
      insertItems = new ArrayList<>();
      updateItems = new ArrayList<>();
      Map<String, Object> claimAmount = 
          (Map<String, Object>) ((Map<String, Object>) claimEstimatedAmount
          .get("estimate_amount")).get(primaryPlanId);
      String insuranceCompanyId = (insurancePlans[0] == primaryPlanId)
          ? (String) patientBean.get("primary_insurance_co")
          : (String) patientBean.get("secondary_insurance_co");
      Integer preauthPrescId = eauthService.getLatestEAuthPrescId((String) params.getPatientId(),
          (Integer) params.getId(), insuranceCompanyId);
      if (preauthPrescId == null && !priorAuthPrescriptions.isEmpty()) {
        preauthPrescId = eauthService.getEAuthPrescSequenceId(username, (Integer) params.getId(),
            insuranceCompanyId);
      }
      Map<String, BasicDynaBean> preAuthPrescriptionActivitiesMap = ConversionUtils
          .listBeanToMapBean(
              eauthService.getActivities(insuranceCompanyId, (Integer) params.getId()),
              "patient_pres_id");
      for (Integer i = 1; i <= priorAuthPrescriptionsl; i++) {
        Map<String, Object> prescriptionItem = priorAuthPrescriptions.get(i - 1);
        Map<String, Object> itemclaimAmount = (Map<String, Object>) claimAmount
            .get("_" + i.toString());
        prescriptionItem.put("insurance_claim_amt", itemclaimAmount.get("insurance_claim_amt"));
        BasicDynaBean preAuthPrescriptionActivitiesBean = preAuthPrescriptionActivitiesMap
            .get(prescriptionItem.get("patient_presc_id"));
        if (newPrescriptionIds.contains(prescriptionItem.get("patient_presc_id"))
            || preAuthPrescriptionActivitiesBean == null) {
          insertItems.add(prescriptionItem);
        } else if (preAuthPrescriptionActivitiesBean.get("preauth_act_status").equals("O")) {
          updateItems.add(prescriptionItem);
        }
      }
      List<Integer> deletePrescriptionIds = (List<Integer>) data.get("delete_ids");
      if (!deletePrescriptionIds.isEmpty()) {
        success &= eauthService.deleteEAuth(deletePrescriptionIds, username,
            (Integer) params.getId());
      }
      if (!insertItems.isEmpty()) {
        success &= eauthService.insertEAuth(insertItems, params.getPatientId(),
            (Integer) params.getId(), username, preauthPrescId);
      }
      if (!updateItems.isEmpty()) {
        success &= eauthService.updateEAuth(updateItems, params.getPatientId(),
            (Integer) params.getId(), username, preauthPrescId, preAuthPrescriptionActivitiesMap);
      }
    }

    Boolean preAuthLimitExceeded = Boolean.FALSE;
    if (null != claimEstimatedAmount) {
      preAuthLimitExceeded = (Boolean) claimEstimatedAmount
          .get("pre_authorized_amount_limit_reached");
    }

    if (null != preAuthLimitExceeded && preAuthLimitExceeded) {
      orderService.createPreAuthPrescriptionForOrderedItemsOfVisit(params.getPatientId(),
          (Integer) params.getId());
    } else if (null != claimEstimatedAmount) {
      // If preauth limit has come down then update send for preauth as N
      orderService.deletePriorAuthForOrderedItemsOfVisit(params.getPatientId(),
          (Integer) params.getId());
      eauthService.uncheckPreviouslyCheckedPrescriptionsForPriorAuth(params.getPatientId(),
          (Integer) params.getId());
    }

    returnData.put("success", success);
    return returnData;
  }

  /**
   * Initiates.
   * @param params the param
   * @param errMap the error map
   * @return map
   */
  public Map<String, Object> initiate(Map<String, Object> params, Map<String, Object> errMap) {

    String visitId = (String) params.get("patient_id");
    Integer consultationId = (Integer) params.get("consultation_id");
    int[] insurancePlans = patientInsurancePlansService.getPlanIds(visitId);
    boolean success = true;
        
    int primaryPlanId = 0;
    if (null != insurancePlans && insurancePlans.length > 0) {
      primaryPlanId = insurancePlans[0];
    }
    
    Map<String, Object> returnData = new HashMap<>();
    if (insurancePlans == null) {
      returnData.put("success", success);
      return returnData;
    }
    BasicDynaBean patientBean = regService.findByKey(visitId);
    String username = (String) sessionService.getSessionAttributes().get("userId");
    List<Map<String, Object>> prescriptions = prescriptionService.getAllPresriptions(consultationId,
        (String) patientBean.get("bed_type"), (String) patientBean.get("org_id"));
    Map<String, Object> estimateAmountReqBody = new HashMap<>();
    Map<String, Object> visit = new HashMap<>();
    visit.put("visit_id", visitId);
    estimateAmountReqBody.put("visit", visit);
    List<Map<String, Object>> orderedItems = new ArrayList<>();
    List<Map<String, Object>> priorAuthPrescriptions = new ArrayList<>();

    for (Map<String, Object> presItem : prescriptions) {

      if (!presItem.get("status").equals("P")) {
        continue;
      }

      priorAuthPrescriptions.add(presItem);
      Map<String, Object> orderedItem = new HashMap<>();
      orderedItem.put("amount", ((BigDecimal) presItem.get("charge")).toString());
      orderedItem.put("insurance_category_id",
          ((Integer) presItem.get("insurance_category_id")).toString());
      orderedItem.put("discount", ((BigDecimal) presItem.get("discount")).toString());
      orderedItem.put("new", "Y");
      orderedItem.put("act_description_id", presItem.get("item_id"));
      orderedItem.put("op_id", presItem.get("item_id"));
      orderedItem.put("tax_amt", "0.00");
      orderedItem.put("consultation_type_id", -1);
      String itemType = (String) presItem.get("item_type");
      if (itemType.equals(PrescriptionsService.SERVICE)) {
        BigDecimal itemQty = new BigDecimal((Integer) presItem.get("item_qty"));
        orderedItem.put("amount",
            (((BigDecimal) presItem.get("charge")).multiply(itemQty)).toString());
        orderedItem.put("discount",
            (((BigDecimal) presItem.get("discount")).multiply(itemQty)).toString());
        orderedItem.put("type", "Service");
        orderedItem.put("charge_head", "SERSNP");
        orderedItem.put("charge_group", "SNP");
      } else if (itemType.equals(PrescriptionsService.OPERATION)) {
        orderedItem.put("type", "Operation");
        orderedItem.put("charge_head", "SACOPE");
        orderedItem.put("charge_group", "OPE");
      } else if (itemType.equals(PrescriptionsService.INVESTIGATION)) {
        orderedItem.put("type",
            presItem.get("category").equals("DEP_LAB") ? "Labaratory" : "Radiology");
        orderedItem.put("charge_head",
            presItem.get("category").equals("DEP_LAB") ? "LTDIA" : "RTDIA");
        orderedItem.put("charge_group", "DIA");
      } else if (itemType.equals(PrescriptionsService.DOCTOR)) {
        orderedItem.put("type", "Doctor");
        orderedItem.put("charge_head", "OPDOC");
        orderedItem.put("charge_group", "DOC");
      }
      orderedItems.add(orderedItem);
    }

    estimateAmountReqBody.put("ordered_items", orderedItems);
    Map<String, Object> claimEstimatedAmount = null;
    try {
      claimEstimatedAmount = billService.estimateAmount(estimateAmountReqBody);
    } catch (Exception exception) {
      log.error("Prescriptions Insurance Calculation", exception);
      errMap.put("preAuth", msgUtil.getMessage("exception.prescription.preauth.initiation.failed"));
      returnData.put("success", false);
      return returnData;
    }
    Boolean preAuthLimitExceeded = Boolean.FALSE;
    if (null != claimEstimatedAmount) {
      preAuthLimitExceeded = (Boolean) claimEstimatedAmount
          .get("pre_authorized_amount_limit_reached");
    }
    if (null != preAuthLimitExceeded && preAuthLimitExceeded) {
      if (primaryPlanId > 0) {
        List<Map<String, Object>> insertItems = new ArrayList<>();
        List<Map<String, Object>> updateItems = new ArrayList<>();
        Map<String, Object> claimAmount = 
            (Map<String, Object>) ((Map<String, Object>) claimEstimatedAmount
            .get("estimate_amount")).get(primaryPlanId);
        String insuranceCompanyId = (insurancePlans[0] == primaryPlanId)
            ? (String) patientBean.get("primary_insurance_co")
            : (String) patientBean.get("secondary_insurance_co");
        Integer preauthPrescId = eauthService.getLatestEAuthPrescId(visitId, consultationId,
            insuranceCompanyId);
        if (preauthPrescId == null && !priorAuthPrescriptions.isEmpty()) {
          preauthPrescId = eauthService.getEAuthPrescSequenceId(username, consultationId,
              insuranceCompanyId);
        }
        Map<String, BasicDynaBean> preAuthPrescriptionActivitiesMap = ConversionUtils
            .listBeanToMapBean(eauthService.getActivities(insuranceCompanyId, consultationId),
                "patient_pres_id");
        for (Integer i = 1; i <= priorAuthPrescriptions.size(); i++) {
          Map<String, Object> prescriptionItem = priorAuthPrescriptions.get(i - 1);
          Map<String, Object> itemclaimAmount = (Map<String, Object>) claimAmount
              .get("_" + i.toString());
          prescriptionItem.put("insurance_claim_amt", itemclaimAmount.get("insurance_claim_amt"));
          BasicDynaBean preAuthPrescriptionActivitiesBean = preAuthPrescriptionActivitiesMap
              .get(prescriptionItem.get("patient_presc_id"));
          enablePreAuthForValidPrescription(prescriptionItem, visitId);
          if (preAuthPrescriptionActivitiesBean == null) {
            insertItems.add(prescriptionItem);
          } else if (preAuthPrescriptionActivitiesBean.get("preauth_act_status").equals("O")) {
            updateItems.add(prescriptionItem);
          }
        }

        if (!insertItems.isEmpty()) {
          success &= eauthService.insertEAuth(insertItems, visitId, consultationId, username,
              preauthPrescId);
        }
        if (!updateItems.isEmpty()) {
          success &= eauthService.updateEAuth(updateItems, visitId, consultationId, username,
              preauthPrescId, preAuthPrescriptionActivitiesMap);
        }
      }
      success = orderService.createPreAuthPrescriptionForOrderedItemsOfVisit(visitId,
          consultationId);
    } else if (null != claimEstimatedAmount) {
      // If preauth limit has come down then update send for preauth as N
      orderService.deletePriorAuthForOrderedItemsOfVisit(visitId, consultationId);
      eauthService.uncheckPreviouslyCheckedPrescriptionsForPriorAuth(visitId, consultationId);
    }
    returnData.put("success", success);
    return returnData;
  }

  /**
   * Sets preauth_required='Y' for valid prescription.
   *
   * @param prescriptionItem the prescription item
   */
  private void enablePreAuthForValidPrescription(Map<String, Object> prescriptionItem,
      String visitId) {
    BasicDynaBean insurancePlan = patientInsurancePlansService.getVisitPrimaryPlan(visitId);
    Set<String> excludedGroups = AdvanceInsuranceHelper
        .getPreAuthExcludedChargeGroups(insurancePlan);
    String itemType = (String) prescriptionItem.get("item_type");
    String chargeGroup = "";
    switch (itemType) {
      case PrescriptionsService.SERVICE:
        chargeGroup = "SNP";
        break;
      case PrescriptionsService.OPERATION:
        chargeGroup = "OPE";
        break;
      case PrescriptionsService.INVESTIGATION:
        chargeGroup = "DIA";
        break;
      case PrescriptionsService.DOCTOR:
      default:
        chargeGroup = "DOC";
    }
    if (!excludedGroups.contains(chargeGroup)) {
      prescriptionItem.put("preauth_required", "Y");
    }
  }

  /**
   * Initiate the pre auth for ordered items.
   *
   * @param params      the Map containing insurance_co_id, visit_id.
   * @param billCharges the bill charges dynabean.
   * @return true, if successful
   */
  public boolean initiateForOrderedItems(Map<String, Object> params,
      List<BasicDynaBean> billCharges) {
    String insuranceCoId = (String) params.get("insurance_co_id");
    String visitId = (String) params.get("visit_id");
    Integer consultationId = (Integer) params.get("consultation_id");
    if (null == consultationId) {
      consultationId = 0;
    }
    String username = (String) sessionService.getSessionAttributes().get("userId");

    Integer preAuthPrescId = eauthService.getLatestEAuthPrescId(visitId, consultationId,
        insuranceCoId);
    if (preAuthPrescId == null) {
      preAuthPrescId = eauthService.getEAuthPrescSequenceId(username, consultationId,
          insuranceCoId);
    }
    return eauthService.insertEAuth(billCharges, visitId, username, preAuthPrescId, consultationId);
  }

}
