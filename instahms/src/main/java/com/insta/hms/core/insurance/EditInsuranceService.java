package com.insta.hms.core.insurance;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.DateHelper;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.billing.BillChargeClaimService;
import com.insta.hms.core.billing.BillChargeClaimTaxService;
import com.insta.hms.core.billing.BillChargeService;
import com.insta.hms.core.billing.BillChargeTaxService;
import com.insta.hms.core.billing.BillClaimService;
import com.insta.hms.core.billing.BillHelper;
import com.insta.hms.core.billing.BillRepository;
import com.insta.hms.core.billing.BillService;
import com.insta.hms.core.billing.ChangeItemRateService;
import com.insta.hms.core.billing.DiscountService;
import com.insta.hms.core.fa.AccountingJobScheduler;
import com.insta.hms.core.inventory.sales.SalesService;
import com.insta.hms.core.patient.registration.PatientInsurancePlanDetailsService;
import com.insta.hms.core.patient.registration.PatientInsurancePlansService;
import com.insta.hms.core.patient.registration.PatientInsurancePolicyDetailsService;
import com.insta.hms.core.patient.registration.PatientRegistrationService;
import com.insta.hms.core.patient.registration.RegistrationPreferencesService;
import com.insta.hms.core.patient.registration.RegistrationService;
import com.insta.hms.core.patient.registration.VisitCaseRateDetailService;
import com.insta.hms.documents.PatientDocumentService;
import com.insta.hms.documents.PlanCardDocumentsService;
import com.insta.hms.documents.PlanDocsDetailsService;
import com.insta.hms.integration.configuration.InterfaceEventMappingService;
import com.insta.hms.integration.insurance.submission.ClaimSubmissionsService;
import com.insta.hms.mdm.centerpreferences.CenterPreferencesService;
import com.insta.hms.mdm.insurancecategory.InsuranceCategoryService;
import com.insta.hms.mdm.insurancecompanies.InsuranceCompanyService;
import com.insta.hms.mdm.insuranceitemcategories.InsuranceItemCategoryService;
import com.insta.hms.mdm.insuranceplans.InsurancePlanService;
import com.insta.hms.mdm.organization.OrganizationService;
import com.insta.hms.mdm.patientcategories.PatientCategoryService;
import com.insta.hms.mdm.sponsors.SponsorTypeService;
import com.insta.hms.mdm.tpas.TpaService;
import com.insta.hms.usermanager.Role;

import flexjson.JSONSerializer;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class EditInsuranceService.
 */

@Service
public class EditInsuranceService {

  static Logger log = LoggerFactory.getLogger(EditInsuranceService.class);

  /** The patient ins plans service. */
  @LazyAutowired
  private PatientInsurancePlansService patientInsPlansService;

  /** The patient registration service. */
  @LazyAutowired
  private PatientRegistrationService patientRegistrationService;

  /** The patient policy details service. */
  @LazyAutowired
  private PatientInsurancePolicyDetailsService patientPolicyDetailsService;

  /** The patient insurance plan details service. */
  @LazyAutowired
  private PatientInsurancePlanDetailsService patientInsurancePlanDetailsService;

  /** The visit case rate detail service. */
  @LazyAutowired
  private VisitCaseRateDetailService visitCaseRateDetailService;

  /** The bill service. */
  @LazyAutowired
  private BillService billService;

  /** The discount service. */
  @LazyAutowired
  private DiscountService discountService;

  /** The plan docs detail service. */
  @LazyAutowired
  private PlanDocsDetailsService planDocsDetailService;

  /** The tpa service. */
  @LazyAutowired
  private TpaService tpaService;

  /** The insurance plan service. */
  @LazyAutowired
  private InsurancePlanService insurancePlanService;

  /** The insurance company service. */
  @LazyAutowired
  private InsuranceCompanyService insuranceCompanyService;

  /** The patient category service. */
  @LazyAutowired
  private PatientCategoryService patientCategoryService;

  /** The session service. */
  @LazyAutowired
  private SessionService sessionService;

  /** The insurance category service. */
  @LazyAutowired
  private InsuranceCategoryService insuranceCategoryService;

  /** The organization service. */
  @LazyAutowired
  private OrganizationService organizationService;

  /** The registration pref service. */
  @LazyAutowired
  private RegistrationPreferencesService registrationPrefService;

  /** The insurance item category service. */
  @LazyAutowired
  private InsuranceItemCategoryService insuranceItemCategoryService;

  /** The sponsor type service. */
  @LazyAutowired
  private SponsorTypeService sponsorTypeService;

  /** The patient document service. */
  @LazyAutowired
  private PatientDocumentService patientDocumentService;

  /** The plan card document service. */
  @LazyAutowired
  private PlanCardDocumentsService planCardDocumentService;

  /** The bill charge service. */
  @LazyAutowired
  private BillChargeService billChargeService;

  /** The change item rate service. */
  @LazyAutowired
  private ChangeItemRateService changeItemRateService;

  /** The center pref service. */
  @LazyAutowired
  private CenterPreferencesService centerPrefService;

  /** The bill repository. */
  @LazyAutowired
  private BillRepository billRepository;

  /** The bill claim service. */
  @LazyAutowired
  private BillClaimService billClaimService;

  /** The bill helper. */
  @LazyAutowired
  private BillHelper billHelper;

  /** The bill charge claim service. */
  @LazyAutowired
  private BillChargeClaimService billChargeClaimService;

  /** The claim submission service. */
  @LazyAutowired
  private ClaimSubmissionsService claimSubmissionService;

  /** The insurance claim service. */
  @LazyAutowired
  private InsuranceClaimService insuranceClaimService;

  /** The reg service. */
  @LazyAutowired
  private RegistrationService regService;

  /** The sales service. */
  @LazyAutowired
  private SalesService salesService;

  /** The bill charge tax service. */
  @LazyAutowired
  private BillChargeTaxService billChargeTaxService;

  /** The bill charge claim tax service. */
  @LazyAutowired
  private BillChargeClaimTaxService billChargeClaimTaxService;

  /** The sponsor service. */
  @LazyAutowired
  private SponsorService sponsorService;

  /** The accounting job scheduler. */
  @LazyAutowired
  private AccountingJobScheduler accountingJobScheduler;
  
  @LazyAutowired
  private GenericPreferencesService genericPreferencesService;
  
  @LazyAutowired
  private InterfaceEventMappingService interfaceEventMappingService;
  
  /**
   * Method to update visit and bill insurance details.
   *
   * @param paramMap the param map
   * @return the map
   */
  public Map<String, Object> updateInsuranceDetails(Map<String, Map> paramMap) {
    // TODO Auto-generated method stub

    @SuppressWarnings("unchecked")
    Map<String, String[]> params = paramMap.get("params");

    Map<String, Object> resultMap = new HashMap<String, Object>();

    String visitId = params.get("visitId")[0];

    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("patient_id", visitId);
    List<BasicDynaBean> existingVisitInsuranceList = patientInsPlansService.listAll(keys);

    updateVisitInsuranceDetails(paramMap, existingVisitInsuranceList, resultMap);

    Boolean status = (Boolean) resultMap.get("status");

    if (!status) {
      return resultMap;
    }

    Boolean visitInsuranceDetailsChanged = isVisitInsuranceDetailsChanged(params,
        existingVisitInsuranceList);
    Boolean limitsChanged = isLimitsChanged(params);

    if (!visitInsuranceDetailsChanged && !limitsChanged) {
      return resultMap;
    }

    /**
     * If visit insurance details are updated successfully then will change rates of all items of a
     * insurance bills as per insurance plan default rate plan.
     * And also will change rates of all items of cash bills as per center preference cash rate
     * plan.
     */
    
    boolean shouldUpdateCharge = "Y".equals(
        genericPreferencesService.getAllPreferences().get("update_charge_on_rate_plan_change"));
    if (shouldUpdateCharge) {
      billHelper.resetInventoryCharges(visitId);
      updateItemsAsPerInsuranceRatePlan(params, (visitInsuranceDetailsChanged || limitsChanged),
          resultMap, existingVisitInsuranceList);
      billHelper.replayInventoryReturns(visitId);
    }

    if (visitInsuranceDetailsChanged) {
      updateBillClaimEntries(params, existingVisitInsuranceList);
    }
    List<BasicDynaBean> billList = billService.getVisitOpenBills(visitId);

    /**
     * reprocessVisitBills - Method to process Dyna Package, DRG and Perdiem Bills.
     */
    reprocessVisitBills(visitId, billList);

    /**
     * recalculateInsuranceAmountForVisit - Method to recalculate insurance amounts for all
     * insurance bills of a visit
     */
    recalculateInsuranceAmountForVisit(params);

    /**
     * updatePkgMarginSponsorAmount - Method to update dyna pkg margin amount based on approval
     * amounts
     */
    updatePkgMarginSponsorAmount(visitId);

    /**
     * Method to reset roundoff and service claim tax amount of a bill.
     */
    resetDirectChargesInBill(visitId, billList);
    
    interfaceEventMappingService.editInsuranceDetailsEvent(visitId);
    
    return resultMap;

  }

  /**
   * Method to update patient visit insurance details.
   *
   * @param paramsMap the params map
   * @param index     the index
   * @param resultMap the result map
   */
  private void updateInsuranceDetails(@SuppressWarnings("rawtypes") Map<String, Map> paramsMap,
      int index, Map<String, Object> resultMap) {

    @SuppressWarnings("unchecked")
    Map<String, String[]> params = paramsMap.get("params");
    @SuppressWarnings("unchecked")
    Map<String, MultipartFile> fileParams = paramsMap.get("file_params");
    updatePatientPolicyDetails(params, index);
    updatePatientInsurancePlans(params, index);
    insertPatientInsurancePlanDetails(params, index);
    try {
      insertOrUpdateInsurancePlanDocs(params, fileParams, index, resultMap);
    } catch (IOException ioException) {
      log.error("Exception occurred : " + ioException.getMessage());
    }
  }

  /**
   * Reset direct charges in bill.
   *
   * @param visitId  the visit id
   * @param billList the bill list
   */
  private void resetDirectChargesInBill(String visitId, List<BasicDynaBean> billList) {
    // TODO Auto-generated method stub
    for (BasicDynaBean billBean : billList) {
      String billNo = (String) billBean.get("bill_no");
      billService.resetDirectChargesInBill(billNo);
    }
  }

  /**
   * Reprocess visit bills.
   *
   * @param visitId  the visit id
   * @param billList the bill list
   */
  private void reprocessVisitBills(String visitId, List<BasicDynaBean> billList) {
    // TODO Auto-generated method stub
    for (BasicDynaBean billBean : billList) {
      String billNo = (String) billBean.get("bill_no");
      billService.reProcessBill(billNo, false, true, true);
    }
  }

  /**
   * Update pkg margin sponsor amount.
   *
   * @param visitId the visit id
   */
  private void updatePkgMarginSponsorAmount(String visitId) {
    billService.updatePkgMarginSponsorAmount(visitId);
  }

  /**
   * Method to insert and update visit insurance details.
   *
   * @param paramMap                   the param map
   * @param existingVisitInsuranceList the existing visit insurance list
   * @param resultMap                  the result map
   */
  private void updateVisitInsuranceDetails(Map<String, Map> paramMap,
      List<BasicDynaBean> existingVisitInsuranceList, Map<String, Object> resultMap) {

    @SuppressWarnings("unchecked")
    Map<String, String[]> params = paramMap.get("params");

    int existingInsurancePlansCount = existingVisitInsuranceList.size();
    int newInsurancePlanCount = getNewInsurancePlansCount(params);
    Boolean secondaryPlanExist = newInsurancePlanCount == 2;

    String msg = null != resultMap.get("msg") ? (String) resultMap.get("msg") : "";
    String visitId = params.get("visitId")[0];
    BasicDynaBean visitBean = patientRegistrationService.findByKey("patient_id", visitId);

    if (newInsurancePlanCount == existingInsurancePlansCount && newInsurancePlanCount == 0) {
      resultMap.put("status", false);
      return;
    }

    if (newInsurancePlanCount >= existingInsurancePlansCount) {
      if (existingInsurancePlansCount == 0) {
        insertInsuranceDetails(paramMap, 1, resultMap);
        if (secondaryPlanExist) {
          insertInsuranceDetails(paramMap, 2, resultMap);
        }
      } else {
        updateInsuranceDetails(paramMap, 1, resultMap);
        if (newInsurancePlanCount > existingInsurancePlansCount) {
          insertInsuranceDetails(paramMap, 2, resultMap);
        } else if (existingInsurancePlansCount == 2) {
          updateInsuranceDetails(paramMap, 2, resultMap);
        }
      }
      resultMap.put("msg",
          msg + " TPA added/edited for Patient " + (String) visitBean.get("mr_no") + "</br>");

    } else if (newInsurancePlanCount < existingInsurancePlansCount) {
      if (existingInsurancePlansCount == 2) {
        removeInsuranceDetails(params, 2);
      }
      if (newInsurancePlanCount == 0) {
        removeInsuranceDetails(params, 1);
        resultMap.put("msg",
            msg + " TPA removed from Patient " + (String) visitBean.get("mr_no") + "</br>");
      } else {
        updateInsuranceDetails(paramMap, 1, resultMap);
      }
    }

    visitBean = patientRegistrationService.findByKey("patient_id", visitId);
    Boolean isCaseRateExists = null != visitBean.get("primary_case_rate_id");
    if (isCaseRateExists) {
      int existingPriPlanId = existingInsurancePlansCount > 0
          ? (Integer) (existingVisitInsuranceList.get(0).get("plan_id"))
          : 0;
      int newPriPlanId = params.get("primary_plan_id")[0] == null
          || (params.get("primary_plan_id")[0]).equals("") ? 0
              : Integer.parseInt(params.get("primary_plan_id")[0]);
      if (existingPriPlanId == newPriPlanId) {
        billService.updateCaseRateLimts(visitBean);
      } else {
        visitBean.set("primary_case_rate_id", null);
        visitBean.set("secondary_case_rate_id", null);

        patientRegistrationService.update(visitBean, visitId);
        visitCaseRateDetailService.deleteCaseRateDetails(visitId);
      }
    }

    updatePatientRegInsDetails(params.get("visitId")[0]);

    resultMap.put("status", true);
  }

  /**
   * Method to update insurance details in patient_registration table.
   *
   * @param visitId the visit id
   */
  private void updatePatientRegInsDetails(String visitId) {
    // TODO Auto-generated method stub

    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("patient_id", visitId);
    List<BasicDynaBean> visitInsPlanList = patientInsPlansService.listAll(keys);
    BasicDynaBean visitDetailsBean = patientRegistrationService.findByKey("patient_id", visitId);
    for (BasicDynaBean patPlanBean : visitInsPlanList) {
      int priority = (Integer) patPlanBean.get("priority");
      if (priority == 1) {
        visitDetailsBean.set("primary_sponsor_id", patPlanBean.get("sponsor_id"));
        visitDetailsBean.set("primary_insurance_co", patPlanBean.get("insurance_co"));
        visitDetailsBean.set("primary_insurance_approval", patPlanBean.get("insurance_approval"));
        visitDetailsBean.set("prior_auth_id", patPlanBean.get("prior_auth_id"));
        visitDetailsBean.set("prior_auth_mode_id", patPlanBean.get("prior_auth_mode_id"));
        visitDetailsBean.set("use_drg",
            (null != patPlanBean.get("use_drg") && patPlanBean.get("use_drg").equals("") ? "N"
                : patPlanBean.get("use_drg")));

        visitDetailsBean.set("use_perdiem",
            (null != patPlanBean.get("use_perdiem") && patPlanBean.get("use_perdiem").equals("")
                ? "N"
                : patPlanBean.get("use_perdiem")));

        visitDetailsBean.set("plan_id", patPlanBean.get("plan_id"));
        visitDetailsBean.set("category_id", patPlanBean.get("plan_type_id"));
      } else if (priority == 2) {
        visitDetailsBean.set("secondary_sponsor_id", patPlanBean.get("sponsor_id"));
        visitDetailsBean.set("secondary_insurance_co", patPlanBean.get("insurance_co"));
        visitDetailsBean.set("secondary_insurance_approval", patPlanBean.get("insurance_approval"));
      }
    }

    patientRegistrationService.update(visitDetailsBean, visitId);
  }

  /**
   * When we remove insurance from visit then we use this method to remove insurance details from
   * patient_registration table.
   *
   * @param visitId  the visit id
   * @param priority the priority
   * @return the boolean
   */
  private Boolean removePatientRegistrationDetails(String visitId, int priority) {

    BasicDynaBean visitDetailsBean = patientRegistrationService.getBean();

    if (priority == 1) {
      visitDetailsBean.set("primary_sponsor_id", null);
      visitDetailsBean.set("primary_insurance_co", null);
      visitDetailsBean.set("plan_id", 0);
      visitDetailsBean.set("category_id", 0);
      visitDetailsBean.set("org_id", "ORG0001");
    } else if (priority == 2) {
      visitDetailsBean.set("secondary_sponsor_id", null);
      visitDetailsBean.set("secondary_insurance_co", null);
    }

    return patientRegistrationService.update(visitDetailsBean, visitId);
  }

  /**
   * Method to check is insurance details are modified or not.
   *
   * @param params             the params
   * @param visitInsuranceList the visit Insurance List
   * @return the boolean
   */
  private Boolean isVisitInsuranceDetailsChanged(Map<String, String[]> params,
      List<BasicDynaBean> visitInsuranceList) {
    // TODO Auto-generated method stub

    Boolean visitInsuranceDetailsChanged = false;

    String exisitnPriSponsor = null;
    String existingSecSponsor = null;
    int exisitngPriPlan = 0;
    int existingSecPlan = 0;

    String primarySponsorId = null;
    String secondarySponsorId = null;

    for (BasicDynaBean insBean : visitInsuranceList) {
      int priority = (Integer) insBean.get("priority");
      if (priority == 1) {
        exisitnPriSponsor = (String) insBean.get("sponsor_id");
        exisitngPriPlan = (Integer) insBean.get("plan_id");

      } else if (priority == 2) {
        existingSecSponsor = (String) insBean.get("sponsor_id");
        existingSecPlan = (Integer) insBean.get("plan_id");
      }
    }

    primarySponsorId = params.get("primary_sponsor_id")[0];
    secondarySponsorId = params.get("secondary_sponsor_id")[0];
    int priPlanId = params.get("primary_plan_id")[0] == null
        || (params.get("primary_plan_id")[0]).equals("") ? 0
            : Integer.parseInt(params.get("primary_plan_id")[0]);
    int secPlanId = params.get("secondary_plan_id")[0] == null
        || (params.get("secondary_plan_id")[0]).equals("") ? 0
            : Integer.parseInt(params.get("secondary_plan_id")[0]);

    String visitId = params.get("visitId")[0];

    BasicDynaBean visitBean = patientRegistrationService.findByKey("patient_id", visitId);
    String existingVisitRatePlan = (String) visitBean.get("org_id");
    String visitRatePlan = params.get("organization")[0];

    if (((exisitnPriSponsor == null || primarySponsorId == null
        || !exisitnPriSponsor.equals(primarySponsorId))
        || ((!StringUtils.isBlank(existingSecSponsor) || !StringUtils.isBlank(secondarySponsorId))
            && (existingSecSponsor == null || secondarySponsorId == null
                || !existingSecSponsor.equals(secondarySponsorId))))
        || ((exisitngPriPlan != priPlanId) || (existingSecPlan != secPlanId))
        || !visitRatePlan.equals(existingVisitRatePlan)) {
      visitInsuranceDetailsChanged = true;
    }

    return visitInsuranceDetailsChanged;
  }

  /**
   * Method to check is visit/category limits changed or not.
   *
   * @param params the params
   * @return the boolean
   */
  private Boolean isLimitsChanged(Map<String, String[]> params) {
    // TODO Auto-generated method stub

    String insCategoryLimitsEdited = params.get("insEdited")[0];
    String visitLimitsEdited = params.get("visitLimitsChanged")[0];

    return insCategoryLimitsEdited.equalsIgnoreCase("true") || visitLimitsEdited.equals("Y");
  }

  /**
   * Method to remove insurance details from all insurance related tables.
   *
   * @param params the params
   * @param index  the index
   */
  private void removeInsuranceDetails(Map<String, String[]> params, int index) {

    String visitId = params.get("visitId")[0];

    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("patient_id", visitId);
    keys.put("priority", index);
    BasicDynaBean patientInsPlanBean = patientInsPlansService.findByKey(keys);

    removePatientPolicyDetails(patientInsPlanBean, visitId);
    removePatientInsurancePlanDetails(patientInsPlanBean, visitId);
    removePatientInsurancePlans(patientInsPlanBean, visitId);
    removePatientRegistrationDetails(visitId, index);

  }

  /**
   * Method to delete entries in patient_insurance_plans table.
   *
   * @param patientInsPlanBean the patient ins plan bean
   * @param visitId            the visit id
   */

  private void removePatientInsurancePlans(BasicDynaBean patientInsPlanBean, String visitId) {

    int patientInsurancePlanId = (Integer) patientInsPlanBean.get("patient_insurance_plans_id");
    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("patient_insurance_plans_id", patientInsurancePlanId);
    patientInsPlansService.delete(keys);

  }

  /**
   * Method to delete entries in patient_insurance_plan_details table.
   *
   * @param patientInsPlanBean the patient ins plan bean
   * @param visitId            the visit id
   */
  private void removePatientInsurancePlanDetails(BasicDynaBean patientInsPlanBean, String visitId) {

    int patientInsurancePlanId = (Integer) patientInsPlanBean.get("patient_insurance_plans_id");
    patientInsurancePlanDetailsService.delete(patientInsurancePlanId);
  }

  /**
   * Method to delete entries in patient_policy_details table.
   *
   * @param patientInsPlanBean the patient ins plan bean
   * @param visitId            the visit id
   */

  private void removePatientPolicyDetails(BasicDynaBean patientInsPlanBean, String visitId) {

    int patientPolicyId = (Integer) patientInsPlanBean.get("patient_policy_id");
    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("patient_policy_id", patientPolicyId);
    patientPolicyDetailsService.delete(keys);
  }

  /**
   * Method to update patient_insurance_plans table.
   *
   * @param params the params
   * @param index  the index
   */

  private void updatePatientInsurancePlans(Map<String, String[]> params, int index) {

    String prefix = getPrefix(index);
    Integer patientInsurancePlanId = params.get(prefix + "_patient_insurance_plans_id")[0] == null
        || (params.get(prefix + "_patient_insurance_plans_id")[0]).equals("") ? null
            : Integer.parseInt(params.get(prefix + "_patient_insurance_plans_id")[0]);
    Map<String, Object> keys = new HashMap<>();
    keys.put("patient_insurance_plans_id", patientInsurancePlanId);
    BasicDynaBean patientInsPlansBean = patientInsPlansService.findByKey(keys);
    setPatientInsPlansBean(params, patientInsPlansBean, index);
    patientInsPlansService.update(patientInsPlansBean, keys);

  }

  /**
   * Method to update patient_policy_details table.
   *
   * @param params the params
   * @param index  the index
   * @return the int
   */

  private int updatePatientPolicyDetails(Map<String, String[]> params, int index) {

    BasicDynaBean patientPolicyBean = patientPolicyDetailsService.getBean();
    setPatientPolicyBean(params, patientPolicyBean, index);

    return patientPolicyDetailsService.update(patientPolicyBean);

  }

  /**
   * Method to insert patient visit insurance details.
   *
   * @param paramsMap the params map
   * @param index     the index
   * @param resultMap the result map
   */

  private void insertInsuranceDetails(@SuppressWarnings("rawtypes") Map<String, Map> paramsMap,
      int index, Map<String, Object> resultMap) {
    @SuppressWarnings("unchecked")
    Map<String, String[]> params = paramsMap.get("params");
    @SuppressWarnings("unchecked")
    Map<String, MultipartFile> fileParams = paramsMap.get("file_params");
    insertPatientPolicyDetails(params, index);
    insertPatientInsurancePlans(params, index);
    insertPatientInsurancePlanDetails(params, index);
    try {
      insertOrUpdateInsurancePlanDocs(params, fileParams, index, resultMap);
    } catch (IOException ioException) {
      log.error("Exception occurred : " + ioException.getMessage());
    }
  }

  /**
   * Method to insert data into patient policy details table.
   *
   * @param params the params
   * @param index  the index
   */
  private void insertPatientPolicyDetails(Map<String, String[]> params, int index) {

    BasicDynaBean patientPolicyBean = patientPolicyDetailsService.getBean();

    setPatientPolicyBean(params, patientPolicyBean, index);
    patientPolicyDetailsService.insert(patientPolicyBean);
  }

  /**
   * Method to prepare patient policy details bean from request params.
   *
   * @param params            the params
   * @param patientPolicyBean the patient policy bean
   * @param index             the index
   */
  private void setPatientPolicyBean(Map<String, String[]> params, BasicDynaBean patientPolicyBean,
      int index) {

    String prefix = getPrefix(index);

    String visitId = params.get("visitId")[0];

    BasicDynaBean visitBean = patientRegistrationService.findByKey("patient_id", visitId);
    String mrNo = (String) visitBean.get("mr_no");

    String startDateStr = getParamDefault(params, prefix + "_policy_validity_start", null);
    String endDateStr = getParamDefault(params, prefix + "_policy_validity_end", null);

    final java.sql.Date policyValidityStart = startDateStr != null
        ? DateHelper.parseDatetoSqlDate(startDateStr)
        : null;
    final java.sql.Date policyValidityEnd = endDateStr != null
        ? DateHelper.parseDatetoSqlDate(endDateStr)
        : null;

    final String memberId = getParamDefault(params, prefix + "_member_id", null);
    final String policyNumber = getParamDefault(params, prefix + "_policy_number", null);
    final String policyHolderName = getParamDefault(params, prefix + "_policy_holder_name", null);
    final String patientRelationShip = getParamDefault(params, prefix + "_patient_relationship",
        null);

    int patientPolicyId = (params.get(prefix + "_policy_id")[0] != null
        && !(params.get(prefix + "_policy_id")[0]).equals(""))
            ? Integer.parseInt(params.get(prefix + "_policy_id")[0])
            : 0;

    patientPolicyId = patientPolicyId == 0
        ? DatabaseHelper.getNextSequence("patient_policy_details_patient_policy_id")
        : patientPolicyId;

    Integer planId = params.get(prefix + "_plan_id")[0] == null
        || (params.get(prefix + "_plan_id")[0]).equals("") ? null
            : Integer.parseInt(params.get(prefix + "_plan_id")[0]);

    patientPolicyBean.set("mr_no", mrNo);
    patientPolicyBean.set("visit_id", visitId);
    patientPolicyBean.set("member_id", memberId != null ? memberId.trim() : null);
    patientPolicyBean.set("policy_holder_name", policyHolderName);
    patientPolicyBean.set("patient_relationship", patientRelationShip);
    patientPolicyBean.set("policy_validity_start", policyValidityStart);
    patientPolicyBean.set("policy_validity_end", policyValidityEnd);
    patientPolicyBean.set("plan_id", planId);
    patientPolicyBean.set("patient_policy_id", patientPolicyId);
    patientPolicyBean.set("policy_number", policyNumber);
  }

  /**
   * Method to insert data into patient_insurance_plans table.
   *
   * @param params the params
   * @param index  the index
   */
  private void insertPatientInsurancePlans(Map<String, String[]> params, int index) {

    BasicDynaBean patientInsPlansBean = patientInsPlansService.getBean();
    setPatientInsPlansBean(params, patientInsPlansBean, index);
    patientInsPlansService.insert(patientInsPlansBean);
  }

  /**
   * Method to prepare patient insurance plans bean from request params.
   *
   * @param params              the params
   * @param patientInsPlansBean the patient ins plans bean
   * @param index               the index
   */

  private void setPatientInsPlansBean(Map<String, String[]> params,
      BasicDynaBean patientInsPlansBean, int index) {

    String prefix = getPrefix(index);

    String sponsorId = null;
    String insCompId = null;

    final String visitType = params.get("visitType")[0];
    String visitId = params.get("visitId")[0];

    BasicDynaBean visitBean = patientRegistrationService.findByKey("patient_id", visitId);
    final String mrNo = (String) visitBean.get("mr_no");

    sponsorId = params.get(prefix + "_sponsor_id")[0];
    insCompId = getParamDefault(params, prefix + "_insurance_co", null);
    final Integer planId = params.get(prefix + "_plan_id")[0] == null
        || (params.get(prefix + "_plan_id")[0]).equals("") ? null
            : Integer.parseInt(params.get(prefix + "_plan_id")[0]);
    final Integer categoryId = params.get(prefix + "_plan_type") == null
        || (params.get(prefix + "_plan_type")[0]).equals("") ? null
            : Integer.parseInt(params.get(prefix + "_plan_type")[0]);

    String useDRG = "N";
    useDRG = getParamDefault(params, prefix + "_use_drg", "N");
    String usePerdiem = "N";
    usePerdiem = getParamDefault(params, prefix + "_use_perdiem", "N");
    Integer patientPolicyId = 0;
    patientPolicyId = (params.get(prefix + "_policy_id")[0] != null
        && !(params.get(prefix + "_policy_id")[0]).equals(""))
            ? Integer.parseInt(params.get(prefix + "_policy_id")[0])
            : 0;

    final String priorAuthId = params.get(prefix + "_prior_auth_id")[0];
    final int priorAuthModeId = params.get(prefix + "_prior_auth_mode_id")[0] == null
        || params.get(prefix + "_prior_auth_mode_id")[0].equals("") ? 0
            : Integer.parseInt(params.get(prefix + "_prior_auth_mode_id")[0]);

    String visitLimitStr = getParamDefault(params, prefix + "_visit_limit", null);
    BigDecimal visitLimit = visitLimitStr != null ? new BigDecimal(visitLimitStr) : null;

    String visitDeductibleStr = getParamDefault(params, prefix + "_visit_deductible", null);
    BigDecimal visitDeductible = visitDeductibleStr != null ? new BigDecimal(visitDeductibleStr)
        : null;

    String visitCopayStr = getParamDefault(params, prefix + "_visit_copay", null);
    BigDecimal visitCopay = visitCopayStr != null ? new BigDecimal(visitCopayStr) : null;

    String maxCopayStr = getParamDefault(params, prefix + "_max_copay", null);
    BigDecimal maxCopay = maxCopayStr != null ? new BigDecimal(maxCopayStr) : null;

    String utilizationLimitStr = getParamDefault(params, prefix + "_plan_utilization", null);

    BigDecimal utilizationAmount = utilizationLimitStr != null ? new BigDecimal(utilizationLimitStr)
        : null;
    Integer patientInsurancePlanId = params.get(prefix + "_patient_insurance_plans_id")[0] == null
        || (params.get(prefix + "_patient_insurance_plans_id")[0]).equals("") ? null
            : Integer.parseInt(params.get(prefix + "_patient_insurance_plans_id")[0]);
    patientInsurancePlanId = (null == patientInsurancePlanId)
        ? patientInsPlansService.getNextSequence()
        : patientInsurancePlanId;
    patientPolicyId = patientPolicyId == 0 ? getPatientPolicyId(mrNo, visitId, planId)
        : patientPolicyId;
    if (utilizationAmount != null) {
      patientInsPlansBean.set("utilization_amount", utilizationAmount);
    }
    patientInsPlansBean.set("patient_id", visitId);
    patientInsPlansBean.set("mr_no", mrNo);
    patientInsPlansBean.set("patient_insurance_plans_id", patientInsurancePlanId);
    patientInsPlansBean.set("insurance_co", insCompId);
    patientInsPlansBean.set("sponsor_id", sponsorId);
    patientInsPlansBean.set("plan_id", planId);
    patientInsPlansBean.set("plan_type_id", categoryId);
    patientInsPlansBean.set("prior_auth_id", priorAuthId);
    patientInsPlansBean.set("prior_auth_mode_id", priorAuthModeId);
    patientInsPlansBean.set("use_drg", useDRG);
    patientInsPlansBean.set("use_perdiem", usePerdiem);
    patientInsPlansBean.set("patient_policy_id", patientPolicyId);
    patientInsPlansBean.set("priority", index);

    String planLimitStr = getParamDefault(params, prefix + "_plan_limit", null);
    BigDecimal planLimit = planLimitStr != null ? new BigDecimal(planLimitStr) : null;
    patientInsPlansBean.set("plan_limit", planLimit);

    String perDayLimitStr = getParamDefault(params, prefix + "_perday_limit", null);
    BigDecimal perDayLimit = perDayLimitStr != null ? new BigDecimal(perDayLimitStr) : null;
    patientInsPlansBean.set("visit_per_day_limit", perDayLimit);

    String limitIncludeFollowUp = params.get(prefix + "_limits_include_followUps")[0];
    if (null != limitIncludeFollowUp && !limitIncludeFollowUp.equals("")
        && limitIncludeFollowUp.equals("Y") && visitType.equalsIgnoreCase("o")) {
      patientInsPlansBean.set("episode_limit", visitLimit);
      patientInsPlansBean.set("episode_deductible", visitDeductible);
      patientInsPlansBean.set("episode_copay_percentage", visitCopay);
      patientInsPlansBean.set("episode_max_copay_percentage", maxCopay);
    } else {
      patientInsPlansBean.set("visit_limit", visitLimit);
      patientInsPlansBean.set("visit_deductible", visitDeductible);
      patientInsPlansBean.set("visit_copay_percentage", visitCopay);
      patientInsPlansBean.set("visit_max_copay_percentage", maxCopay);
    }
  }

  /**
   * Method to insert data into patient_insurance_plan_details table.
   *
   * @param params the params
   * @param index  the index
   */
  private void insertPatientInsurancePlanDetails(Map<String, String[]> params, int index) {

    String visitId = params.get("visitId")[0];
    String visitType = params.get("visitType")[0];
    List<BasicDynaBean> patInsPlanDetailsList = new ArrayList<BasicDynaBean>();

    setPatientInsurancePlanDetails(params, patInsPlanDetailsList, visitId, visitType, index);

    if (!patInsPlanDetailsList.isEmpty() && patInsPlanDetailsList.size() > 0) {
      patientInsurancePlanDetailsService
          .delete((Integer) patInsPlanDetailsList.get(0).get("patient_insurance_plans_id"));
      patientInsurancePlanDetailsService.insertAll(patInsPlanDetailsList);
    }

  }

  /**
   * Method to prepare patient insurance plan details bean from request params.
   *
   * @param params                the params
   * @param patInsPlanDetailsList the pat ins plan details list
   * @param visitId               the visit id
   * @param visitType             the visit type
   * @param index                 the index
   */
  private void setPatientInsurancePlanDetails(Map<String, String[]> params,
      List<BasicDynaBean> patInsPlanDetailsList, String visitId, String visitType, int index) {

    String catPrefix = index == 1 ? "P" : "S";
    String prefix = getPrefix(index);
    String[] categoryNames = (String[]) params.get(catPrefix + "_cat_name");

    if (categoryNames != null && categoryNames.length > 0) {
      String[] categoryIds = (String[]) params.get(catPrefix + "_cat_id");
      String[] sponserLimits = (String[]) params.get(catPrefix + "_sponser_limit");
      String[] catDeducts = (String[]) params.get(catPrefix + "_cat_deductible");
      String[] itemDeducts = (String[]) params.get(catPrefix + "_item_deductible");
      String[] copayPercent = (String[]) params.get(catPrefix + "_copay_percent");
      String[] maxCopayPercent = (String[]) params.get(catPrefix + "_max_copay");

      BasicDynaBean patientInsPlanDetBean;

      Integer planId = null;
      Integer patientInsurancePlanId = null;
      Integer insuranceCategoryId = null;
      BigDecimal patientAmount = null;
      BigDecimal patientPercent = null;
      BigDecimal patientAmountCap = null;
      BigDecimal perTreatmentLimit = null;
      BigDecimal patientAmountPerCategory = null;

      for (int j = 0; j < categoryNames.length; j++) {
        patientInsPlanDetBean = patientInsurancePlanDetailsService.getBean();
        planId = params.get(prefix + "_plan_id")[0] == null
            || (params.get(prefix + "_plan_id")[0]).equals("") ? null
                : Integer.parseInt(params.get(prefix + "_plan_id")[0]);

        patientInsurancePlanId = params.get(prefix + "_patient_insurance_plans_id")[0] == null
            || (params.get(prefix + "_patient_insurance_plans_id")[0]).equals("") ? null
                : Integer.parseInt(params.get(prefix + "_patient_insurance_plans_id")[0]);

        insuranceCategoryId = categoryIds[j] == null || categoryIds[j].equals("") ? 0
            : Integer.parseInt(categoryIds[j]);

        patientAmount = sponserLimits[j] == null || sponserLimits[j].equals("") ? null
            : new BigDecimal(sponserLimits[j]);

        patientPercent = catDeducts[j] == null || catDeducts[j].equals("") ? new BigDecimal(0)
            : new BigDecimal(catDeducts[j]);

        patientAmountCap = itemDeducts[j] == null || itemDeducts[j].equals("") ? new BigDecimal(0)
            : new BigDecimal(itemDeducts[j]);

        perTreatmentLimit = copayPercent[j] == null || copayPercent[j].equals("")
            ? new BigDecimal(0)
            : new BigDecimal(copayPercent[j]);

        patientAmountPerCategory = maxCopayPercent[j] == null || maxCopayPercent[j].equals("")
            ? null
            : new BigDecimal(maxCopayPercent[j]);

        Map<String, Object> keys = new HashMap<String, Object>();
        keys.put("patient_id", visitId);
        keys.put("plan_id", planId);
        BasicDynaBean patientInsPlanBean = patientInsPlansService.findByKey(keys);
        Integer patientInsPlanId = (Integer) patientInsPlanBean.get("patient_insurance_plans_id");
        patientInsurancePlanId = null == patientInsurancePlanId ? patientInsPlanId
            : patientInsurancePlanId;

        patientInsPlanDetBean.set("visit_id", visitId);
        patientInsPlanDetBean.set("plan_id", planId);
        patientInsPlanDetBean.set("insurance_category_id", insuranceCategoryId);
        patientInsPlanDetBean.set("patient_insurance_plans_id", patientInsurancePlanId);
        patientInsPlanDetBean.set("patient_amount", patientAmountCap);
        patientInsPlanDetBean.set("patient_percent", perTreatmentLimit);
        patientInsPlanDetBean.set("patient_amount_cap", patientAmountPerCategory);
        patientInsPlanDetBean.set("per_treatment_limit", patientAmount);
        patientInsPlanDetBean.set("patient_amount_per_category", patientPercent);
        patientInsPlanDetBean.set("patient_type", visitType);

        patInsPlanDetailsList.add(patientInsPlanDetBean);

      }
    }
  }

  /**
   * Method to insert insurance plan document into plan_doc_details table.
   *
   * @param params     the params
   * @param fileParams the file params
   * @param index      the index
   * @param resultMap  the result map
   * @throws IOException Signals that an I/O exception has occurred.
   */

  private void insertOrUpdateInsurancePlanDocs(Map<String, String[]> params,
      Map<String, MultipartFile> fileParams, int index, Map<String, Object> resultMap)
      throws IOException {

    Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();

    final String userName = (String) sessionAttributes.get("userId");

    final String visitId = params.get("visitId")[0];

    String prefix = getPrefix(index);
    Object insDocBytea = null;
    MultipartFile file = null;
    if (fileParams.get(prefix + "_file_name") != null) {

      file = fileParams.get(prefix + "_file_name");
      insDocBytea = file.getInputStream();
    }

    final String insCardFileLocation = params.get(prefix + "_sponsor_cardfileLocationI")[0];
    final String docsName = params.get(prefix + "_insurance_doc_name")[0];
    final String format = params.get(prefix + "_insurance_format")[0];
    final String docType = params.get(prefix + "_insurance_doc_type")[0];
    final String docDate = params.get(prefix + "_insurance_doc_date")[0];

    if (docsName == null || docsName.equals("")) {
      return;
    }

    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("patient_id", visitId);
    keys.put("priority", index);

    BasicDynaBean visitBean = patientRegistrationService.findByKey("patient_id", visitId);
    final String mrNo = (String) visitBean.get("mr_no");

    Map<String, Object[]> newparamMap = new HashMap<String, Object[]>();
    copyStringToMap(newparamMap, "username", userName);
    copyStringToMap(newparamMap, "doc_date", docDate);
    copyStringToMap(newparamMap, "doc_name", docsName);
    copyStringToMap(newparamMap, "doc_format", format);
    copyStringToMap(newparamMap, "format", format);
    copyStringToMap(newparamMap, "doc_type", docType);
    copyStringToMap(newparamMap, "mr_no", mrNo);
    copyStringToMap(newparamMap, "patient_id", visitId);
    BasicDynaBean patientInsPlanBean = patientInsPlansService.findByKey(keys);
    Integer patientPolicyId = (Integer) patientInsPlanBean.get("patient_policy_id");
    copyStringToMap(newparamMap, "patient_policy_id", patientPolicyId.toString());
    if (insDocBytea != null && file.getSize() > 0) {
      copyObjectToMap(newparamMap, "fileName", file);

    } else if (null != insCardFileLocation && !insCardFileLocation.equals("")) {
      copyObjectToMap(newparamMap, "fileName", file);
    }
    BasicDynaBean docBean = planDocsDetailService.findByKey("patient_policy_id", patientPolicyId);
    insertOrUpdateDocument(newparamMap, docBean, resultMap);
    newparamMap.clear();
  }

  /**
   * Insert or update document.
   *
   * @param newparamMap the newparam map
   * @param docBean     the doc bean
   * @param resultsMap  the results map
   */
  private void insertOrUpdateDocument(Map<String, Object[]> newparamMap, BasicDynaBean docBean,
      Map<String, Object> resultsMap) {

    if (newparamMap.get("fileName") == null) {
      return;
    }

    try {
      if (docBean != null && docBean.get("doc_id") != null) {
        copyStringToMap(newparamMap, "doc_id", docBean.get("doc_id").toString());
        planCardDocumentService.update(newparamMap);
      } else {
        Map<String, Object> resultMap = planCardDocumentService.create(newparamMap);
        Boolean status = (Boolean) resultMap.get("status");
        if (status) {
          BasicDynaBean patDocDetailsBean = planDocsDetailService.getBean();
          int docId = (Integer) resultMap.get("docId");
          int patientPolicyId = Integer
              .parseInt(newparamMap.get("patient_policy_id")[0].toString());

          patDocDetailsBean.set("doc_id", docId);
          patDocDetailsBean.set("patient_policy_id", patientPolicyId);
          patDocDetailsBean.set("doc_name", (String) newparamMap.get("doc_name")[0]);
          patDocDetailsBean.set("username", (String) newparamMap.get("username")[0]);
          planDocsDetailService.insert(patDocDetailsBean);
        } else {
          resultsMap.put("error",
              "Incorrectly formatted values supplied. Failed to insert/update Document");
        }

      }
    } catch (IOException | ParseException | SQLException exception) {
      log.error("Exception occurred : " + exception.getMessage());
    }
  }

  /**
   * Copy object to map.
   *
   * @param params the params
   * @param key    the key
   * @param value  the value
   */
  public void copyObjectToMap(Map params, String key, Object value) {

    if (params.containsKey(key)) {
      Object[] obj = (Object[]) params.get(key);
      Object[] newArray = Arrays.copyOf(obj, obj.length + 1);
      newArray[obj.length] = value;
      params.put(key, newArray);

    } else {
      params.put(key, new Object[] { value });
    }
  }

  /**
   * Copy string to map.
   *
   * @param params the params
   * @param key    the key
   * @param value  the value
   */
  public void copyStringToMap(Map params, String key, String value) {

    if (params.containsKey(key)) {
      String[] obj = (String[]) params.get(key);
      String[] newArray = Arrays.copyOf(obj, obj.length + 1);
      newArray[obj.length] = value;
      params.put(key, newArray);

    } else {
      params.put(key, new String[] { value });
    }
  }

  /**
   * Gets the patient policy id.
   *
   * @param mrNo    the mr no
   * @param visitId the visit id
   * @param planId  the plan id
   * @return the patient policy id
   */
  private Integer getPatientPolicyId(String mrNo, String visitId, Integer planId) {

    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("mr_no", mrNo);
    keys.put("visit_id", visitId);
    keys.put("plan_id", planId);

    BasicDynaBean patientPolicyBean = patientPolicyDetailsService.findByKey(keys);
    return (Integer) patientPolicyBean.get("patient_policy_id");
  }

  /**
   * Gets the param default.
   *
   * @param params       the params
   * @param paramName    the param name
   * @param defaultValue the default value
   * @return the param default
   */
  private String getParamDefault(Map<String, String[]> params, String paramName,
      String defaultValue) {
    if (params.get(paramName) == null) {
      return defaultValue;
    }
    String value = params.get(paramName)[0];
    if ((value == null) || value.trim().equals("")) {
      value = defaultValue;
    }
    return value;
  }

  /**
   * Gets the prefix.
   *
   * @param index the index
   * @return the prefix
   */
  private String getPrefix(int index) {

    String prefix = null;
    prefix = index == 1 ? "primary" : "secondary";
    return prefix;
  }

  /**
   * Gets the new insurance plans count.
   *
   * @param params the params
   * @return the new insurance plans count
   */
  private int getNewInsurancePlansCount(Map<String, String[]> params) {

    int noOfInsurancePlanCount = 0;
    String primarySponsor = null != params.get("primary_sponsor") ? params.get("primary_sponsor")[0]
        : null;
    String secondarySponsor = null != params.get("secondary_sponsor")
        ? params.get("secondary_sponsor")[0]
        : null;

    int priPlanId = params.get("primary_plan_id")[0] == null
        || (params.get("primary_plan_id")[0]).equals("") ? 0
            : Integer.parseInt(params.get("primary_plan_id")[0]);

    int secPlanId = params.get("secondary_plan_id")[0] == null
        || ((String) params.get("secondary_plan_id")[0]).equals("") ? 0
            : Integer.parseInt(params.get("secondary_plan_id")[0]);

    if (null != primarySponsor && primarySponsor.equals("I") && priPlanId > 0) {
      noOfInsurancePlanCount++;
    }
    if (null != secondarySponsor && secondarySponsor.equals("I") && secPlanId > 0) {
      noOfInsurancePlanCount++;
    }
    return noOfInsurancePlanCount;
  }

  /**
   * Method to update item rates of selected bills as per insurance rate plan. And also to update
   * item rates of cash bills as per cash rate plan.
   *
   * @param params                       the params
   * @param visitInsuranceDetailsChanged the visit insurance details changed
   * @param resultMap                    the result map
   * @param existingVisitInsuranceList   the existing visit insurance list
   */
  private void updateItemsAsPerInsuranceRatePlan(Map<String, String[]> params,
      Boolean visitInsuranceDetailsChanged, Map<String, Object> resultMap,
      List<BasicDynaBean> existingVisitInsuranceList) {
    // TODO Auto-generated method stub

    String visitId = params.get("visitId")[0];
    BasicDynaBean visitBean = patientRegistrationService.findByKey("patient_id", visitId);

    Integer prevDefaultDiscPlanId = 0;
    if (null != existingVisitInsuranceList && existingVisitInsuranceList.size() > 0) {
      prevDefaultDiscPlanId = getDefaultDiscountPlanId(existingVisitInsuranceList);
    }

    String billsRequired = params.get("bills_to_change_sponsor_amounts")[0];
    String visitType = (String) visitBean.get("visit_type");

    if (visitInsuranceDetailsChanged && !billsRequired.equals("open_bills")) {
      reopenBills(visitId, resultMap);
      cancelCreditNotes(visitId, resultMap);
    }

    String insurancePlanDefaultRatePlan = params.get("organization")[0];
    String visitRatePlan = (String) visitBean.get("org_id");
    String bedType = (String) visitBean.get("bed_type");
    int priPlanId = params.get("primary_plan_id")[0] == null
        || (params.get("primary_plan_id")[0]).equals("") ? 0
            : Integer.parseInt(params.get("primary_plan_id")[0]);
    int defaultDiscPlanId = 0;
    String multiVisitPkgBillNos = null;
    List<BasicDynaBean> multiVisitPkgBills = changeItemRateService
        .getMultiVisitPackageTPABills("open_bills", visitId);
    if (null != multiVisitPkgBills && multiVisitPkgBills.size() > 0) {
      multiVisitPkgBillNos = changeItemRateService.getMultiVisitPackageBillNos(multiVisitPkgBills);
    }

    BasicDynaBean planBean = insurancePlanService.findByKey(priPlanId);

    defaultDiscPlanId = null != planBean && null != planBean.get("discount_plan_id")
        ? (Integer) planBean.get("discount_plan_id")
        : 0;

    String billNos = changeItemRateService.getBillNos(visitId);

    List<BasicDynaBean> insBillBeans = changeItemRateService.getBillList(visitId, "open_bills");

    if ((!insurancePlanDefaultRatePlan.equals(visitRatePlan))
        || (!prevDefaultDiscPlanId.equals(defaultDiscPlanId))) {
      changeItemRateService.changeBillChargeItemRates(visitBean, billNos,
          insurancePlanDefaultRatePlan, bedType,multiVisitPkgBills);

      if (null != billNos) {
        if (defaultDiscPlanId != 0) {
          /**
           * When there is default discount plan mapped for a insurance plan then will apply that
           * default discount plan to all insurance bills.
           */
          applyDiscountPlanForInsuranceBills(defaultDiscPlanId, billNos, visitType);
        } else {
          /**
           * When there is no default discount plan mapped in plan then remove discount plan from
           * bills.
           */
          if (prevDefaultDiscPlanId != 0) {
            removeDiscountPlanFromBills(insBillBeans, billNos);
          } else {
            applyDiscountPlanForBills(insBillBeans, visitType);
          }
        }

        billService.updateBillRatePlan(insurancePlanDefaultRatePlan, billNos);

        String msg = null != resultMap.get("msg") ? (String) resultMap.get("msg") : "";
        billNos = billNos.replace("'", "");
        resultMap.put("msg",
            msg + " Updated insurance rate plan rates for following insurance bills : " + billNos
                + "</br>");
      }

      if (null != multiVisitPkgBills && multiVisitPkgBills.size() > 0) {
        multiVisitPkgBillNos = changeItemRateService
            .getMultiVisitPackageBillNos(multiVisitPkgBills);
        billService.updateBillRatePlan(insurancePlanDefaultRatePlan, multiVisitPkgBillNos);

        //ignoring the Charges Update for multi-visit package entries
        /* changeItemRateService.updateMultiVisitPackageCharges(multiVisitPkgBills,
            insurancePlanDefaultRatePlan, bedType); */

        if (defaultDiscPlanId != 0) {
          /**
           * When there is default discount plan mapped for a insurance plan then will apply that
           * default discount plan to all insurance bills.
           */
          applyDiscountPlanForInsuranceBills(defaultDiscPlanId, multiVisitPkgBillNos, visitType);
        } else {
          /**
           * When there is no default discount plan mapped in plan then will apply discount plan
           * which is applied in a bill.
           */
          applyDiscountPlanForBills(multiVisitPkgBills, visitType);
        }

        String msg = null != resultMap.get("msg") ? (String) resultMap.get("msg") : "";
        multiVisitPkgBillNos = multiVisitPkgBillNos.replace("'", "");
        resultMap.put("msg",
            msg + " Updated  Discount Plan for following MVP insurance bills : "
                + multiVisitPkgBillNos + "</br>");
      }

      BasicDynaBean visitDetailsBean = patientRegistrationService.getBean();
      visitDetailsBean.set("org_id", insurancePlanDefaultRatePlan);
      patientRegistrationService.update(visitDetailsBean, visitId);
    } else if (defaultDiscPlanId != 0) {
      applyDiscountPlanForInsuranceBills(defaultDiscPlanId, billNos, visitType);
      applyDiscountPlanForInsuranceBills(defaultDiscPlanId, multiVisitPkgBillNos, visitType);
    } else if (prevDefaultDiscPlanId != 0 && defaultDiscPlanId == 0) {
      removeDiscountPlanFromBills(insBillBeans, billNos);
    }

    String nonInsRatePlan = getNonInsuranceRatePlan(visitBean);
    String nonInsbillNos = getNonInsuranceBills(visitBean, nonInsRatePlan);
    List<BasicDynaBean> multiVisitPkgCashBills = changeItemRateService
        .getMultiVisitPackageCashBills(visitId, nonInsRatePlan);

    if (null != nonInsbillNos) {
      changeItemRateService.changeBillChargeItemRates(visitBean, nonInsbillNos, nonInsRatePlan,
          bedType, multiVisitPkgCashBills);
      billService.updateBillRatePlan(nonInsRatePlan, nonInsbillNos);

      List<BasicDynaBean> nonInsBillBeans = billService
          .getNonInsuranceOpenBills((String) visitBean.get("patient_id"));
      applyDiscountPlanForBills(nonInsBillBeans, visitType);

      String msg = null != resultMap.get("msg") ? (String) resultMap.get("msg") : "";
      nonInsbillNos = nonInsbillNos.replace("'", "");
      resultMap.put("msg",
          msg + " Updated non insurance rate plan rates for following cash bills : " + nonInsbillNos
              + "</br>");
    }
    if (null != multiVisitPkgCashBills && multiVisitPkgCashBills.size() > 0) {
      String multiVisitPkgCashBillNos = changeItemRateService
          .getMultiVisitPackageBillNos(multiVisitPkgCashBills);

      billService.updateBillRatePlan(nonInsRatePlan, multiVisitPkgCashBillNos);

      //ignoring the Charges Update for multi-visit package entries

      /* changeItemRateService.updateMultiVisitPackageCharges(multiVisitPkgCashBills,
          nonInsRatePlan, bedType); */

      applyDiscountPlanForBills(multiVisitPkgCashBills, visitType);

      String msg = null != resultMap.get("msg") ? (String) resultMap.get("msg") : "";
      multiVisitPkgCashBillNos = multiVisitPkgCashBillNos.replace("'", "");
      resultMap.put("msg",
          msg + " Updated Discount Plan for following MVP cash bills : "
              + multiVisitPkgCashBillNos + "</br>");
    }

    List<BasicDynaBean> billChargesToUpdateTax = billChargeService.getVisitBillCharges(visitId);
    if (!billChargesToUpdateTax.isEmpty()) {
      billChargeTaxService.calculateAndUpdateBillChargeTaxes(billChargesToUpdateTax, visitId);
    }
  }

  /**
   * Gets the default discount plan id.
   *
   * @param existingVisitInsuranceList the existing visit insurance list
   * @return the default discount plan id
   */
  private Integer getDefaultDiscountPlanId(List<BasicDynaBean> existingVisitInsuranceList) {
    // TODO Auto-generated method stub
    Integer defaultDiscPlanId = 0;
    for (BasicDynaBean bean : existingVisitInsuranceList) {
      int priority = (Integer) bean.get("priority");
      if (priority == 1) {
        int planId = (Integer) bean.get("plan_id");
        BasicDynaBean planBean = insurancePlanService.findByKey(planId);
        if (null != planBean.get("discount_plan_id")) {
          defaultDiscPlanId = (Integer) planBean.get("discount_plan_id");
        }
      }
    }
    return defaultDiscPlanId;
  }

  /**
   * Removes the discount plan from bills.
   *
   * @param insBillBeans the ins bill beans
   * @param billNos      the bill nos
   */
  private void removeDiscountPlanFromBills(List<BasicDynaBean> insBillBeans, String billNos) {
    // TODO Auto-generated method stub
    for (BasicDynaBean billBean : insBillBeans) {
      String billNo = (String) billBean.get("bill_no");
      billBean.set("discount_category_id", 0);
      Map<String, Object> keys = new HashMap<String, Object>();
      keys.put("bill_no", billNo);
      billService.update(billBean, keys);
    }
  }

  /**
   * Apply discount plan for bills.
   *
   * @param billBeans the bill beans
   * @param visitType the visit type
   */
  private void applyDiscountPlanForBills(List<BasicDynaBean> billBeans, String visitType) {
    // TODO Auto-generated method stub
    for (BasicDynaBean billBean : billBeans) {
      String billNo = (String) billBean.get("bill_no");
      int discountPlanId = null != billBean.get("discount_category_id")
          ? (Integer) billBean.get("discount_category_id")
          : 0;
      if (discountPlanId != 0) {
        List<BasicDynaBean> billCharges = billChargeService
            .getBillChargesExcludingPharmacy("'".concat(billNo).concat("'"));
        if (null != billCharges && billCharges.size() > 0) {
          updateBillCharges(discountPlanId, billCharges, visitType);
        }
      }
    }
  }

  /**
   * Apply insurance plan mapped default discount plan for insurance bills. Discount plan should not
   * apply for pharmacy and inventory items as mentioned in bug HMS-22082
   *
   * @param defaultDiscPlanId the default disc plan id
   * @param billNos           the bill nos
   * @param visitType         the visit type
   */
  private void applyDiscountPlanForInsuranceBills(int defaultDiscPlanId, String billNos,
      String visitType) {
    if (defaultDiscPlanId != 0 && null != billNos) {
      billService.updateInsurancePlanDefaultDiscountPlan(defaultDiscPlanId, billNos);
      List<BasicDynaBean> billCharges = billChargeService.getBillChargesExcludingPharmacy(billNos);
      updateBillCharges(defaultDiscPlanId, billCharges, visitType);
    }
  }

  /**
   * Doing batch update to update amount and discount for all charges.
   *
   * @param discountPlanId the discount plan id
   * @param billCharges    the bill charges
   * @param visitType      the visit type
   */
  private void updateBillCharges(int discountPlanId, List<BasicDynaBean> billCharges,
      String visitType) {
    // TODO Auto-generated method stub
    Map<String, Object> updateKeysMap = new HashMap<String, Object>();
    List<String> updateChgIdKeys = new ArrayList<String>();
    List<BasicDynaBean> billChargeToUpdateDisc = new ArrayList<BasicDynaBean>();
    discountService.applyDiscountPlan(discountPlanId, billCharges, visitType, updateChgIdKeys,
        billChargeToUpdateDisc);
    updateKeysMap.put("charge_id", updateChgIdKeys);
    billChargeService.batchUpdate(billChargeToUpdateDisc, updateKeysMap);
  }

  /**
   * On editing visit insurance details, We use this method to cancel all credit notes of a visit.
   *
   * @param visitId   the visit id
   * @param resultMap the result map
   */

  private void cancelCreditNotes(String visitId, Map<String, Object> resultMap) {
    // TODO Auto-generated method stub

    List<BasicDynaBean> creditNoteList = billService.getVisitCreditNote(visitId);
    String creditNoteNos = "";
    for (BasicDynaBean billBean : creditNoteList) {
      final String creditNoteNo = (String) billBean.get("bill_no");

      BasicDynaBean chargeBean = billChargeService.getBean();
      chargeBean.set("amount", BigDecimal.ZERO);
      chargeBean.set("act_quantity", BigDecimal.ZERO);
      chargeBean.set("insurance_claim_amount", BigDecimal.ZERO);
      chargeBean.set("discount", BigDecimal.ZERO);
      chargeBean.set("status", "X");
      Map<String, Object> keys = new HashMap<String, Object>();
      keys.put("bill_no", creditNoteNo);
      billChargeService.update(chargeBean, keys);

      billBean.set("total_amount", BigDecimal.ZERO);
      billBean.set("total_discount", BigDecimal.ZERO);
      billBean.set("total_claim", BigDecimal.ZERO);
      billBean.set("claim_recd_amount", BigDecimal.ZERO);
      billBean.set("status", "X");
      billService.update(billBean, keys);

      creditNoteNos = creditNoteNos.equals("") ? creditNoteNo
          : creditNoteNos.concat("," + creditNoteNo);
    }
    if (!creditNoteNos.equals("")) {
      String msg = null != resultMap.get("msg") ? (String) resultMap.get("msg") : "";
      resultMap.put("msg",
          msg.concat("Following credit notes are cancelled : " + creditNoteNos + "</br>"));
    }
  }

  /**
   * When we edit insurance details using "all open/closed/finalized bills" option then we are
   * reopening finalized and closed bills to recalculate insurance amounts as per new insurance
   * plan.
   *
   * @param visitId   the visit id
   * @param resultMap the result map
   */
  private void reopenBills(String visitId, Map<String, Object> resultMap) {
    // TODO Auto-generated method stub

    List<String> unreopenedBills = new ArrayList<String>();

    Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
    Map<String, Object> actionRightsMap = (Map<String, Object>) sessionService
        .getSessionAttributes(new String[] { "actionRightsMap" }).get("actionRightsMap");
    Integer roleID = (Integer) sessionAttributes.get("roleId");

    String actionRightStatus = (String) actionRightsMap.get(Role.BILL_REOPEN);
    String userId = (String) sessionAttributes.get("userId");

    if (!roleID.equals(1) && !roleID.equals(2) && !actionRightStatus.equalsIgnoreCase("A")) {
      resultMap.put("status", true);
      return;
    }

    List<BasicDynaBean> billsToReopen = billService.getClosedAndFinalizedTpaBills(visitId);

    if (null == billsToReopen || billsToReopen.size() == 0) {
      resultMap.put("status", true);
      return;
    }

    for (BasicDynaBean bill : billsToReopen) {

      String billNo = (String) bill.get("bill_no");
      boolean isClaimsent = false;
      List<BasicDynaBean> billClaimList = billClaimService.listAll(billNo);

      for (BasicDynaBean billClaim : billClaimList) {
        String claimId = (String) billClaim.get("claim_id");
        if (claimId != null && !claimId.equals("")) {
          BasicDynaBean claimbean = insuranceClaimService.getClaimById(claimId);
          String submissionBatchID = null;
          if (claimbean != null) {
            submissionBatchID = (String) claimbean.get("last_submission_batch_id");
          }

          Map<String, Object> identifiers = new HashMap<String, Object>();
          identifiers.put("claim_id", claimId);
          identifiers.put("submission_batch_id", submissionBatchID);
          BasicDynaBean claimSubmissionBean = claimSubmissionService.findByKey(identifiers);

          if (claimSubmissionBean != null) {
            String claimStatus = (claimSubmissionBean.get("status") != null)
                ? (String) claimSubmissionBean.get("status")
                : null;
            if (claimStatus != null && claimStatus.equals("S")) {
              unreopenedBills.add(billNo);
              isClaimsent = true;
            }
          }
          /**
           * Bills which are already a part of submission batch cannot be reopened.
           */
          if (unreopenedBills != null && unreopenedBills.size() > 0) {
            String billNos = "";
            for (String billNumber : unreopenedBills) {
              billNos = billNos.equals("") ? billNumber : billNos + ", " + billNumber;
            }
            String msg = (String) resultMap.get("msg");
            StringBuilder msgBuilder = new StringBuilder();
            if (null != msg) {
              msgBuilder.append(msg);
            }
            msgBuilder
                .append(" The following bills are not reopened as claim is marked as sent : ");
            msgBuilder.append(billNos);
            msgBuilder.append("</br>");
            msg = msgBuilder.toString();
            resultMap.put("msg", msg);
          }
        }
        if (isClaimsent) {
          continue;
        }
        billService.reopenBill(billNo, "Reopening bill for updating Insurance details.", userId);
      }
    }
    resultMap.put("status", true);
  }

  /**
   * Gets the non insurance rate plan.
   *
   * @param visitBean the visit bean
   * @return the non insurance rate plan
   */
  private String getNonInsuranceRatePlan(BasicDynaBean visitBean) {
    // TODO Auto-generated method stub
    int centerId = (Integer) visitBean.get("center_id");
    BasicDynaBean cenetrPrefBean = centerPrefService.getCenterPreferences(centerId);
    String nonInsuranceRatePlan = (String) cenetrPrefBean
        .get("pref_rate_plan_for_non_insured_bill");
    nonInsuranceRatePlan = (nonInsuranceRatePlan == null || nonInsuranceRatePlan.equals(""))
        ? "ORG0001"
        : nonInsuranceRatePlan;
    return nonInsuranceRatePlan;
  }

  /**
   * Gets the non insurance bills.
   *
   * @param visitBean            the visit bean
   * @param nonInsuranceRatePlan the non insurance rate plan
   * @return the non insurance bills
   */
  private String getNonInsuranceBills(BasicDynaBean visitBean, String nonInsuranceRatePlan) {
    // TODO Auto-generated method stub

    String nonInsbillNos = null;
    List<BasicDynaBean> nonInsuranceBills = billService
        .getNonInsuranceOpenBills((String) visitBean.get("patient_id"));
    for (BasicDynaBean billBean : nonInsuranceBills) {
      String billRatePlan = (String) billBean.get("bill_rate_plan_id");
      String billNo = (String) billBean.get("bill_no");
      if (!nonInsuranceRatePlan.equals(billRatePlan)) {
        Boolean multiVisitPackageBill = billService.isMultiVisitPackageBill(billNo);
        if (!multiVisitPackageBill) {
          nonInsbillNos = null == nonInsbillNos ? "'".concat(billNo).concat("'")
              : nonInsbillNos.concat(",".concat("'").concat(billNo).concat("'"));
        }
      }
    }

    return nonInsbillNos;
  }

  /**
   * Method to update bill insurance details. Updating insurance related fields in bill_claim,
   * bill_charge_claim and insurance_claim table.
   *
   * @param params                     the params
   * @param existingVisitInsuranceList the existing visit insurance list
   */
  private void updateBillClaimEntries(Map<String, String[]> params,
      List<BasicDynaBean> existingVisitInsuranceList) {
    String visitId = params.get("visitId")[0];
    String billsRequired = params.get("bills_to_change_sponsor_amounts")[0];

    List<BasicDynaBean> billList;
    if (billsRequired.equals("open_bills")) {
      billList = billService.getOpenTpaBills(visitId);
    } else {
      billList = billService.getAllTpaBills(visitId);
    }

    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("patient_id", visitId);
    List<BasicDynaBean> visitInsuranceList = patientInsPlansService.listAll(keys, "priority");

    if (null != billList) {
      for (BasicDynaBean billBean : billList) {
        changesToBillClaimOnEditIns(visitId, billBean, visitInsuranceList,
            existingVisitInsuranceList);
      }
    }
    if (!billsRequired.equals("open_bills")) {
      return;
    }
    List<BasicDynaBean> allInsBills = billService.getAllTpaBillsAndCreditNotes(visitId);
    for (BasicDynaBean insbillBean : allInsBills) {
      for (BasicDynaBean planBean : visitInsuranceList) {
        int priority = (Integer) planBean.get("priority");
        int pnId = (Integer) planBean.get("plan_id");
        String sponsorId = (String) planBean.get("sponsor_id");
        updateInsuranceBillClaim((String) insbillBean.get("bill_no"), pnId, sponsorId, priority);
      }
    }
  }

  /**
   * Method to update bill_claim table.
   *
   * @param billNo    the bill no
   * @param planId    the plan id
   * @param sponsorId the sponsor id
   * @param priority  the priority
   * @return the boolean
   */
  private Boolean updateInsuranceBillClaim(String billNo, int planId, String sponsorId,
      int priority) {
    // TODO Auto-generated method stub

    BasicDynaBean billClaimBean = billClaimService.getBean();

    billClaimBean.set("plan_id", planId);
    billClaimBean.set("sponsor_id", sponsorId);

    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("bill_no", billNo);
    keys.put("priority", priority);
    boolean success = true;
    success = success && billClaimService.update(billClaimBean, keys) >= 0;

    Map<String, Object> chgKeys = new HashMap<String, Object>();
    chgKeys.put("bill_no", billNo);

    billClaimBean = billClaimService.findByKey(keys);
    String claimId = (String) billClaimBean.get("claim_id");

    Map<String, Object> bcKeys = new HashMap<String, Object>();
    bcKeys.put("bill_no", billNo);
    bcKeys.put("claim_id", claimId);
    BasicDynaBean billChgClaimBean = billChargeClaimService.getBean();
    billChgClaimBean.set("sponsor_id", sponsorId);
    success = success && billChargeClaimService.update(billChgClaimBean, bcKeys) >= 0;

    BasicDynaBean claimBean = insuranceClaimService.getBean();
    Map<String, Object> insClaimKeys = new HashMap<String, Object>();
    insClaimKeys.put("claim_id", claimId);
    claimBean.set("plan_id", planId);
    success = success && insuranceClaimService.update(claimBean, insClaimKeys);

    return success;
  }

  /**
   * Method to update bill claim insurance details. When we add new insurance plan then will insert
   * entries into bill_claim, bill_charge_claim, sales_claim_details and insurance_claim tables.
   * When we edit existing insurance details then will update plan_id, sponsor_id and claim_id in
   * bill_claim, bill_charge_claim, sales_claim_details and insurance_claim tables. When we remove
   * insurance plan from visit then will delete entries from bill_claim, bill_charge_claim and from
   * sales_claim_details tables.
   *
   * @param visitId                    the visit id
   * @param billBean                   the bill bean
   * @param visitInsuranceList         the visit insurance list
   * @param existingVisitInsuranceList the existing visit insurance list
   */

  private void changesToBillClaimOnEditIns(String visitId, BasicDynaBean billBean,
      List<BasicDynaBean> visitInsuranceList, List<BasicDynaBean> existingVisitInsuranceList) {
    // TODO Auto-generated method stub

    String billNo = (String) billBean.get("bill_no");
    Boolean isTpa = (Boolean) billBean.get("is_tpa");
    String billStatus = (String) billBean.get("status");

    int existingPriPlan = 0;
    int existingSecPlan = 0;

    List<BasicDynaBean> chargeList = billChargeService.listAll(billNo);

    for (int k = 0; k < existingVisitInsuranceList.size(); k++) {
      BasicDynaBean existingVisitInsBean = existingVisitInsuranceList.get(k);
      int priority = (Integer) existingVisitInsBean.get("priority");

      if (priority == 1) {
        existingPriPlan = (Integer) existingVisitInsBean.get("plan_id");
      }
      if (priority == 2) {
        existingSecPlan = (Integer) existingVisitInsBean.get("plan_id");
      }
    }

    if (isTpa && null != visitInsuranceList && !visitInsuranceList.isEmpty()) {
      for (int i = 0; i < visitInsuranceList.size(); i++) {
        BasicDynaBean visitInsBean = visitInsuranceList.get(i);

        int existingPlan = i == 0 ? existingPriPlan : existingSecPlan;

        Integer planId = (Integer) visitInsBean.get("plan_id");

        String claimId = billClaimService.getClaimId(existingPlan, billNo, visitId);

        boolean submissionBatchExistis = false;
        BasicDynaBean insClaimBean = insuranceClaimService.findByKey("claim_id", claimId);
        String submissionBatchId = null;
        if (null != insClaimBean && null != insClaimBean.get("last_submission_batch_id")) {
          submissionBatchId = (String) insClaimBean.get("last_submission_batch_id");
        }
        if (null != submissionBatchId && !submissionBatchId.equals("")) {
          submissionBatchExistis = true;
        }
        if (submissionBatchExistis) {
          updateBillClaimOnEditIns(billNo, visitInsBean, claimId, visitId, null);
          insClaimBean.set("plan_id", planId);
          Map<String, Object> insClaimKeys = new HashMap<String, Object>();
          insClaimKeys.put("claim_id", claimId);
          insuranceClaimService.update(insClaimBean, insClaimKeys);
        } else {
          String newClaimId = getNewClaimId(billBean, planId, visitId);
          if (null != claimId && !claimId.equals("")) {
            updateBillClaimOnEditIns(billNo, visitInsBean, claimId, visitId, newClaimId);
            
            /**
             * Update new claim id in sales claim details table
             */
            if (billStatus.equals("F")) {
              updateSalesClaimOnEditIns(billNo, visitInsBean, claimId, newClaimId);
            }
          } else {
            insertbillClaimOnEditIns(billNo, chargeList, visitInsBean, visitId, newClaimId);

            /**
             * Insert sales claim details entries for finalized pharamcy bills.
             */
            if (billStatus.equals("F")) {
              insertSalesClaimOnEditIns(billNo, chargeList, visitInsBean, visitId, newClaimId);
            }
          }
        }
      }

      if (billStatus.equals("A")) {

        for (BasicDynaBean chargeBean : chargeList) {
          String chargeGroup = (String) chargeBean.get("charge_group");
          String chargeHead = (String) chargeBean.get("charge_head");

          if (chargeGroup.equals("MED") || chargeHead.equals("PHRET")
              || chargeHead.equals("PHCRET")) {
            setMedicineInsuranceAmtForPlan(visitInsuranceList, billBean, chargeBean);
          }
        }

        // On Edit Insurance Updating bill charge tax amount to zero

        billChargeTaxService.updateBillChargeClaimTaxAmtToZero(billNo);

        billChargeClaimService.updateBillChargeClaimsAmtToZero(billNo);
      }

      if (visitInsuranceList.size() == 1 && existingSecPlan != 0) {
        String secClaimId = billClaimService.getClaimId(existingSecPlan, billNo, visitId);
        Map<String, Object> deleteKeys = new HashMap<String, Object>();
        deleteKeys.put("bill_no", billNo);
        deleteKeys.put("claim_id", secClaimId);

        billClaimService.delete(deleteKeys);
        billChargeClaimService.delete(deleteKeys);
      }

    } else if (visitInsuranceList.size() == 0 && (existingPriPlan != 0 || existingSecPlan != 0)) {
      billClaimService.delete("bill_no", billNo);
      billChargeClaimService.delete("bill_no", billNo);
      billBean.set("is_tpa", false);
      billBean.set("discount_category_id", 0);
      Map<String, Object> keys = new HashMap<String, Object>();
      keys.put("bill_no", billNo);
      billRepository.update(billBean, keys);

      BasicDynaBean visitBean = patientRegistrationService.findByKey("patient_id", visitId);
      String ratePlanId = (String) visitBean.get("org_id");
      String bedType = (String) visitBean.get("bed_type");
      String billNoToChangeRate = "'".concat(billNo).concat("'");
      Boolean multiVisitPackageBill = billService.isMultiVisitPackageBill(billNo);
      if (!multiVisitPackageBill) {
        changeItemRateService.changeBillChargeItemRates(visitBean, billNoToChangeRate, ratePlanId,
            bedType, null);
        //On Removing the Insurance Re-Calcuate the tax amount 
        List<BasicDynaBean> billChargesToUpdateTax = billChargeService.getVisitBillCharges(visitId);
        if (!billChargesToUpdateTax.isEmpty()) {
          billChargeTaxService.calculateAndUpdateBillChargeTaxes(billChargesToUpdateTax, visitId);
        }
      }
    }

  }
  
  private Boolean updateSalesClaimOnEditIns(String billNo, BasicDynaBean visitInsBean, 
      String claimId, String newClaimId) {
    String sponsorId = (String) visitInsBean.get("sponsor_id");
    return salesService.updateSalesClaimOnEditIns(billNo, sponsorId, claimId, newClaimId);
  }

  /**
   * Method to insert/update entries in sales_claim_details for pharmacy items.
   *
   * @param visitInsuranceList the visit insurance list
   * @param billBean           the bill bean
   * @param chargeBean         the charge bean
   */
  private void setMedicineInsuranceAmtForPlan(List<BasicDynaBean> visitInsuranceList,
      BasicDynaBean billBean, BasicDynaBean chargeBean) {
    // TODO Auto-generated method stub

    String chargeId = (String) chargeBean.get("charge_id");
    BasicDynaBean salebean = salesService.getSaleItems(chargeId);
    if (salebean == null) {
      return;
    }

    String saleId = (String) salebean.get("sale_id");
    List<BasicDynaBean> saleItems = salesService.getSaleItemsDetails(saleId);

    String visitId = (String) billBean.get("visit_id");
    String billNo = (String) billBean.get("bill_no");

    salesService.deleteSalesClaimDetails(saleId);

    List<BasicDynaBean> salesClaimBeanList = new ArrayList<BasicDynaBean>();

    for (int j = 0; j < saleItems.size(); j++) {

      BasicDynaBean saleItem = saleItems.get(j);

      for (int i = 0; i < visitInsuranceList.size(); i++) {

        BasicDynaBean visitInsBean = visitInsuranceList.get(i);
        final String sponsorId = (String) visitInsBean.get("sponsor_id");
        int planId = (Integer) visitInsBean.get("plan_id");
        String claimId = billClaimService.getClaimId(planId, billNo, visitId);

        // insert new sale claim details
        BasicDynaBean salesClaimBean = salesService.getSalesClaimBean();

        salesClaimBean.set("claim_id", claimId);
        salesClaimBean.set("sale_item_id", (Integer) saleItem.get("sale_item_id"));
        salesClaimBean.set("claim_status", (String) saleItem.get("claim_status"));
        salesClaimBean.set("insurance_claim_amt", BigDecimal.ZERO);
        salesClaimBean.set("ref_insurance_claim_amount", BigDecimal.ZERO);
        billHelper.checkSaleItemsForInsCatInRedis(chargeBean, saleItem, planId, visitId);
        salesClaimBean.set("insurance_category_id",
            (Integer) saleItem.get("insurance_category_id"));
        salesClaimBean.set("return_insurance_claim_amt", BigDecimal.ZERO);
        salesClaimBean.set("prior_auth_id", (String) saleItem.get("prior_auth_id"));
        salesClaimBean.set("prior_auth_mode_id", (Integer) saleItem.get("prior_auth_mode_id"));
        salesClaimBean.set("sponsor_id", sponsorId);

        salesClaimBeanList.add(salesClaimBean);

      }
    }

    salesService.insertSalesClaimDetails(salesClaimBeanList);
  }

  /**
   * Method to insert entries into bill_claim table.
   *
   * @param billNo       the bill no
   * @param chargeList   the charge list
   * @param visitInsBean the visit ins bean
   * @param visitId      the visit id
   * @param newClaimId   the new claim id
   * @return the boolean
   */

  private Boolean insertbillClaimOnEditIns(String billNo, List<BasicDynaBean> chargeList,
      BasicDynaBean visitInsBean, String visitId, String newClaimId) {
    // TODO Auto-generated method stub

    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("patient_id", visitId);
    keys.put("plan_id", (Integer) visitInsBean.get("plan_id"));

    int priority = (Integer) visitInsBean.get("priority");
    String sponsorId = (String) visitInsBean.get("sponsor_id");
    int planId = (Integer) visitInsBean.get("plan_id");

    BasicDynaBean billClaimBean = billClaimService.getBean();

    billClaimBean.set("claim_id", newClaimId);
    billClaimBean.set("bill_no", billNo);
    billClaimBean.set("plan_id", planId);
    billClaimBean.set("visit_id", visitId);
    billClaimBean.set("sponsor_id", sponsorId);
    billClaimBean.set("priority", priority);

    billClaimService.insert(billClaimBean);

    List<BasicDynaBean> billChargeClaimList = new ArrayList<BasicDynaBean>();
    for (BasicDynaBean chg : chargeList) {
      BasicDynaBean chgClmBean = billChargeClaimService.getBean();
      chgClmBean.set("charge_id", (String) chg.get("charge_id"));
      chgClmBean.set("claim_id", newClaimId);
      chgClmBean.set("bill_no", billNo);
      chgClmBean.set("sponsor_id", sponsorId);

      String chargeGroup = (String) chg.get("charge_group");
      if (!chargeGroup.equals("MED") && !chargeGroup.equals("RET")) {
        billHelper.checkForInsCatInRedis(chg, planId, visitId);
      }

      chgClmBean.set("insurance_category_id", (Integer) chg.get("insurance_category_id"));

      billChargeClaimList.add(chgClmBean);
    }
    boolean success = true;
    success = success && billChargeClaimService.insertAll(billChargeClaimList);

    return success;
  }

  private void insertSalesClaimOnEditIns(String billNo, List<BasicDynaBean> chargeList,
      BasicDynaBean visitInsBean, String visitId, String newClaimId) {

    List<BasicDynaBean> salesClaimBeanList = new ArrayList<BasicDynaBean>();

    for (BasicDynaBean chg : chargeList) {
      String chargeGroup = (String) chg.get("charge_group");
      String chargeHead = (String) chg.get("charge_head");

      if (chargeGroup.equals("MED") || chargeHead.equals("PHRET") || chargeHead.equals("PHCRET")) {
        String chargeId = (String) chg.get("charge_id");
        BasicDynaBean saleBill = salesService.getSaleItems(chargeId);

        String sponsorId = (String) visitInsBean.get("sponsor_id");
        int planId = (Integer) visitInsBean.get("plan_id");

        if (null != saleBill) {
          String saleId = (String) saleBill.get("sale_id");
          List<BasicDynaBean> saleItems = salesService.getSaleItemsDetails(saleId);

          for (BasicDynaBean saleItem : saleItems) {
            // insert new sale claim details
            BasicDynaBean salesClaimBean = salesService.getSalesClaimBean();

            salesClaimBean.set("claim_id", newClaimId);
            salesClaimBean.set("sale_item_id", (Integer) saleItem.get("sale_item_id"));
            salesClaimBean.set("claim_status", (String) saleItem.get("claim_status"));
            salesClaimBean.set("insurance_claim_amt", BigDecimal.ZERO);
            salesClaimBean.set("ref_insurance_claim_amount", BigDecimal.ZERO);
            billHelper.checkSaleItemsForInsCatInRedis(chg, saleItem, planId, visitId);
            salesClaimBean.set("insurance_category_id",
                (Integer) saleItem.get("insurance_category_id"));
            salesClaimBean.set("return_insurance_claim_amt", BigDecimal.ZERO);
            salesClaimBean.set("prior_auth_id", (String) saleItem.get("prior_auth_id"));
            salesClaimBean.set("prior_auth_mode_id", (Integer) saleItem.get("prior_auth_mode_id"));
            salesClaimBean.set("sponsor_id", sponsorId);

            salesClaimBeanList.add(salesClaimBean);
          }
        }
      }
    }

    salesService.insertSalesClaimDetails(salesClaimBeanList);

  }

  /**
   * Gets the new claim id.
   *
   * @param billBean the bill bean
   * @param planId   the plan id
   * @param visitId  the visit id
   * @return the new claim id
   */
  private String getNewClaimId(BasicDynaBean billBean, int planId, String visitId) {
    // TODO Auto-generated method stub

    int accountGroup = (Integer) billBean.get("account_group");

    String claimId = billClaimService.isVisitClaimExists(planId, visitId, accountGroup);

    if (claimId == null || claimId.isEmpty()) {
      int centerId = regService.getCenterId(visitId);
      claimId = insuranceClaimService.getNextPrefixedId(centerId, accountGroup);
      billChargeClaimService.insertInsuranceClaimId(visitId, planId, claimId,
          (String) billBean.get("bill_no"));
    }
    return claimId;
  }

  /**
   * Method to update bill_claim and bill_charge_claim tables.
   *
   * @param billNo       the bill no
   * @param visitInsBean the visit ins bean
   * @param oldClaimId   the old claim id
   * @param visitId      the visit id
   * @param newClaimId   the new claim id
   * @return the boolean
   */
  private Boolean updateBillClaimOnEditIns(String billNo, BasicDynaBean visitInsBean,
      String oldClaimId, String visitId, String newClaimId) {
    // TODO Auto-generated method stub

    BasicDynaBean billClaimBean = billClaimService.getBean();

    int priority = (Integer) visitInsBean.get("priority");
    String sponsorId = (String) visitInsBean.get("sponsor_id");
    int planId = (Integer) visitInsBean.get("plan_id");

    billClaimBean.set("plan_id", planId);
    billClaimBean.set("visit_id", visitId);
    billClaimBean.set("sponsor_id", sponsorId);
    billClaimBean.set("priority", priority);
    if (null != newClaimId) {
      billClaimBean.set("claim_id", newClaimId);
    }

    Map<String, Object> billClmkeys = new HashMap<String, Object>();
    billClmkeys.put("bill_no", billNo);
    billClmkeys.put("claim_id", oldClaimId);
    boolean success = true;
    success = success && billClaimService.update(billClaimBean, billClmkeys) >= 0;

    if (success) {
      success = success && updateBillCharegClaimOnEditIns(billNo, planId, oldClaimId, visitId,
          sponsorId, newClaimId);

      List<BasicDynaBean> chargeList = billChargeService.listAll(billNo);
      for (BasicDynaBean chg : chargeList) {

        Map<String, Object> keys = new HashMap<>();
        keys.put("charge_id", (String) chg.get("charge_id"));
        if (null != newClaimId) {
          keys.put("claim_id", newClaimId);
        } else {
          keys.put("claim_id", oldClaimId);
        }
        String chargeGroup = (String) chg.get("charge_group");
        if (!chargeGroup.equals("MED") && !chargeGroup.equals("RET")) {
          billHelper.checkForInsCatInRedis(chg, planId, visitId);
        }
        BasicDynaBean chgClmBean = billChargeClaimService.getBean();
        chgClmBean.set("insurance_category_id", (Integer) chg.get("insurance_category_id"));
        success = success && billChargeClaimService.update(chgClmBean, keys) >= 0;
      }
    }

    return success;

  }

  /**
   * Method to update bill_charge_claim table.
   *
   * @param billNo     the bill no
   * @param planId     the plan id
   * @param oldClaimId the old claim id
   * @param visitId    the visit id
   * @param sponsorId  the sponsor id
   * @param newClaimId the new claim id
   * @return true, if successful
   */
  private boolean updateBillCharegClaimOnEditIns(String billNo, int planId, String oldClaimId,
      String visitId, String sponsorId, String newClaimId) {

    BasicDynaBean bean = billChargeClaimService.getBean();

    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("bill_no", billNo);
    keys.put("claim_id", oldClaimId);

    bean.set("sponsor_id", sponsorId);
    if (null != newClaimId) {
      bean.set("claim_id", newClaimId);
    }
    boolean success = true;
    success = billChargeClaimService.update(bean, keys) >= 0;

    return success;
  }

  /**
   * Method to recalculate sponsor amount of all insurance bills as per new insurance plan.
   *
   * @param params the params
   */
  private void recalculateInsuranceAmountForVisit(Map<String, String[]> params) {
    // TODO Auto-generated method stub
    String visitId = params.get("visitId")[0];

    billChargeService.unlockVisitBillsCharges(visitId, "open");
    salesService.unlockVisitSaleItems(visitId, "open");
    billChargeClaimService.includeBillChargesInClaimCalc(visitId, "open");
    
    // Check if we should set the preauth approval amount as claim amount and set it.
    boolean shouldUpdateClaimAmountBasedOnPreAuth = "Y".equals(
        genericPreferencesService.getAllPreferences().get("set_preauth_approved_amt_as_claim_amt"));
    if (shouldUpdateClaimAmountBasedOnPreAuth) {
      billChargeClaimService.setClaimAmountAndExclusionBasedOnPreAuth(visitId);
      billChargeService.setClaimAmountAndExclusionBasedOnPreAuth(visitId);
    }
    
    sponsorService.recalculateSponsorAmount(visitId);
    billChargeService.setIssueReturnsClaimAmountTOZero(visitId, "open");
    insertOrUpdateBillChargeTaxesForSales(visitId);
    salesService.lockVisitSaleItems(visitId, "open");
    billChargeClaimService.updateSalesBillCharges(visitId, "open");
    salesService.updateTaxDetails(visitId);
  }

  /**
   * Method to insert/update bill charge tax entries for sale bills.
   *
   * @param visitId the visit id
   * @return the boolean
   */
  private Boolean insertOrUpdateBillChargeTaxesForSales(String visitId) {
    // TODO Auto-generated method stub
    boolean success = true;
    List<BasicDynaBean> billChargeTaxesToInsert = new ArrayList<BasicDynaBean>();
    List<BasicDynaBean> billChargeClaimTaxesToInsert = new ArrayList<BasicDynaBean>();
    List<BasicDynaBean> billChargeTaxesToUpdate = new ArrayList<BasicDynaBean>();
    List<BasicDynaBean> billChargeclaimTaxesToUpdate = new ArrayList<BasicDynaBean>();

    List<BasicDynaBean> salesTaxList = salesService.getSalesTaxDetails(visitId);

    Map<String, Object> updateKeysMap = new HashMap<String, Object>();
    List<Integer> updateChgTaxIdKeys = new ArrayList<Integer>();

    // insert or update bill_charge_tax entries
    for (BasicDynaBean salesTaxBean : salesTaxList) {
      BasicDynaBean billChargeTaxBean = billChargeTaxService.getBean();
      String chargeId = (String) salesTaxBean.get("charge_id");
      Integer taxSubGroupId = (Integer) salesTaxBean.get("tax_sub_group_id");
      BigDecimal taxRate = ((salesTaxBean.get("tax_rate") != null)
          ? (BigDecimal) salesTaxBean.get("tax_rate")
          : new BigDecimal("-1"));
      if (taxRate.compareTo(BigDecimal.ZERO) >= 0) {
        billChargeTaxBean.set("charge_id", chargeId);
        billChargeTaxBean.set("tax_sub_group_id", taxSubGroupId);
        billChargeTaxBean.set("tax_rate", taxRate);
        billChargeTaxBean.set("tax_amount", salesTaxBean.get("tax_amount"));
        if (!billChargeTaxService.isBillChargeTaxExist(chargeId, taxSubGroupId)) {
          billChargeTaxesToInsert.add(billChargeTaxBean);
        } else {
          BasicDynaBean chgTaxBean = billChargeTaxService.getBillChargeTaxBean(chargeId,
              taxSubGroupId);
          Integer chgTaxId = (Integer) chgTaxBean.get("charge_tax_id");
          updateChgTaxIdKeys.add(chgTaxId);
          billChargeTaxesToUpdate.add(billChargeTaxBean);
        }
      }

    }

    if (!billChargeTaxesToInsert.isEmpty()) {
      success = success && billChargeTaxService.insertBillChargeTaxes(billChargeTaxesToInsert);
    }

    if (!billChargeTaxesToUpdate.isEmpty()) {
      updateKeysMap.put("charge_tax_id", updateChgTaxIdKeys);
      success = success && billChargeTaxService.batchUpdate(billChargeTaxesToUpdate, updateKeysMap);
    }

    Map<String, Object> updateChgClaimTaxKeysMap = new HashMap<String, Object>();
    List<Object> chgIdKeys = new ArrayList<Object>();
    List<Object> taxSubGrpIdKeys = new ArrayList<Object>();
    List<Object> claimIdKeys = new ArrayList<Object>();
    List<BasicDynaBean> salesClaimTaxList = salesService.getSalesClaimTaxDetails(visitId);
    // insert or update bill_charge_claim_tax entries
    for (BasicDynaBean salesClaimTaxBean : salesClaimTaxList) {
      String chargeId = (String) salesClaimTaxBean.get("charge_id");
      String claimId = (String) salesClaimTaxBean.get("claim_id");
      Integer taxSubGroupId = (Integer) salesClaimTaxBean.get("tax_sub_group_id");

      Map<String, Object> chargeTaxMap = new HashMap<String, Object>();
      chargeTaxMap.put("charge_id", chargeId);
      chargeTaxMap.put("tax_sub_group_id", taxSubGroupId);
      BasicDynaBean bcTaxBean = billChargeTaxService.findByKey(chargeTaxMap);

      int chargeTaxId = 0;
      if (null != bcTaxBean && null != bcTaxBean.get("charge_tax_id")) {
        chargeTaxId = (Integer) bcTaxBean.get("charge_tax_id");
        salesClaimTaxBean.set("charge_tax_id", chargeTaxId);
        BigDecimal taxRate = ((salesClaimTaxBean.get("tax_rate") != null)
            ? (BigDecimal) salesClaimTaxBean.get("tax_rate")
            : new BigDecimal("-1"));
        if (taxRate.compareTo(BigDecimal.ZERO) >= 0) {
          BasicDynaBean billChargeClaimTaxBean = billChargeClaimTaxService.getBean();
          billChargeClaimTaxBean.set("charge_id", chargeId);
          billChargeClaimTaxBean.set("claim_id", claimId);
          billChargeClaimTaxBean.set("tax_sub_group_id", taxSubGroupId);
          billChargeClaimTaxBean.set("tax_rate", taxRate);
          billChargeClaimTaxBean.set("sponsor_tax_amount",
              salesClaimTaxBean.get("sponsor_tax_amount"));
          billChargeClaimTaxBean.set("charge_tax_id", chargeTaxId);

          if (!billChargeClaimTaxService.isBillChargeClaimTaxExist(chargeId, claimId,
              taxSubGroupId)) {
            billChargeClaimTaxesToInsert.add(billChargeClaimTaxBean);
          } else {
            chgIdKeys.add(chargeId);
            taxSubGrpIdKeys.add(taxSubGroupId);
            claimIdKeys.add(claimId);
            billChargeclaimTaxesToUpdate.add(billChargeClaimTaxBean);
          }
        }

      }
    }

    if (!billChargeClaimTaxesToInsert.isEmpty()) {
      success = success && billChargeClaimTaxService.batchInsert(billChargeClaimTaxesToInsert);
    }

    if (!billChargeclaimTaxesToUpdate.isEmpty()) {
      updateChgClaimTaxKeysMap.put("charge_id", chgIdKeys);
      updateChgClaimTaxKeysMap.put("tax_sub_group_id", taxSubGrpIdKeys);
      updateChgClaimTaxKeysMap.put("claim_id", claimIdKeys);
      success = success && billChargeClaimTaxService.batchUpdate(billChargeclaimTaxesToUpdate,
          updateChgClaimTaxKeysMap);
    }

    return success;
  }

  /**
   * Parses the date.
   *
   * @param dateStr the date str
   * @return the java.sql. date
   * @throws ParseException the parse exception
   */
  public static java.sql.Date parseDate(String dateStr) throws java.text.ParseException {
    SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy");
    if ((dateStr != null) && !dateStr.equals("")) {
      java.util.Date dt = dateFormatter.parse(dateStr);
      return new java.sql.Date(dt.getTime());
    }
    return null;
  }

  /**
   * Sets the insurance details.
   *
   * @param params the params
   * @param mav    the mav
   */
  public void setInsuranceDetails(Map<String, String[]> params, ModelAndView mav) {

    String visitId = params.get("visitId")[0];
    BasicDynaBean visitbean = patientRegistrationService.findByKey("patient_id", visitId);

    final String mrno = visitbean != null ? (String) visitbean.get("mr_no") : null;

    // If credit notes exist throw confirmation that all credit notes for current visit will be
    // cancelled
    ArrayList<String> billNoList = billService.getCreditNoteList(visitId);
    mav.addObject("creditNoteList", billNoList);

    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("patient_id", visitId);
    keys.put("priority", 1);
    BasicDynaBean priSponsorBean = patientInsPlansService.findByKey(keys);
    keys.put("priority", 2);
    BasicDynaBean secSponsorBean = patientInsPlansService.findByKey(keys);

    boolean isPrimaryInsuranceCardAvailable = false;
    if (priSponsorBean != null) {
      isPrimaryInsuranceCardAvailable = planDocsDetailService
          .isPlanDocumentExist((Integer) priSponsorBean.get("patient_policy_id"));
    }

    boolean isSecondaryInsuranceCardAvailable = false;
    if (secSponsorBean != null) {
      isSecondaryInsuranceCardAvailable = planDocsDetailService
          .isPlanDocumentExist((Integer) secSponsorBean.get("patient_policy_id"));
    }

    mav.addObject("isPrimaryInsuranceCardAvailable", isPrimaryInsuranceCardAvailable);
    mav.addObject("isSecondaryInsuranceCardAvailable", isSecondaryInsuranceCardAvailable);

    Map patientDetails = patientRegistrationService.getPatientVisitInsuranceDetailsMap(visitId);
    mav.addObject("billNo", null != params.get("billNo") ? params.get("billNo")[0] : null);
    mav.addObject("patient", patientDetails);
    mav.addObject("isNewUX", params.get("isNewUX"));

    Map<String, Object> tpaKeys = new HashMap<String, Object>();
    tpaKeys.put("status", "A");
    List<BasicDynaBean> tpaList = tpaService.listAll(tpaKeys);
    mav.addObject("tpaList", ConversionUtils.copyListDynaBeansToMap(tpaList));

    Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
    Integer centerId = (Integer) sessionAttributes.get("centerId");

    centerId = null != visitbean ? (Integer) visitbean.get("center_id") : centerId;

    List<BasicDynaBean> planTypeList = insuranceCategoryService.getInsurancePlanTypes(visitId,
        centerId);
    Map<Integer, List<String>> networkTypeSponsorIdListMap = new HashMap<>();
    
    List<Integer> planCategories = new ArrayList<Integer>();
    for (BasicDynaBean planType : planTypeList) {
      planCategories.add((Integer) planType.get("category_id"));
    }

    Map<String, Object> filterMap = new HashMap<>();
    filterMap.put("category_id", planCategories);
    filterMap.put("status", "A");

    List<BasicDynaBean> planBeans = insurancePlanService.findByCriteria(filterMap);

    for (BasicDynaBean planBean : planBeans) {
      if (networkTypeSponsorIdListMap.get(planBean.get("category_id")) == null) {
        networkTypeSponsorIdListMap.put((Integer) planBean.get("category_id"),
            new ArrayList<String>());
      }
      networkTypeSponsorIdListMap.get(planBean.get("category_id"))
          .add((String) planBean.get("sponsor_id"));
    }

    mav.addObject("categoryLists", ConversionUtils.listBeanToListMap(planTypeList));
    mav.addObject("networkTypeSponsorIdListMap",
            new JSONSerializer().deepSerialize(networkTypeSponsorIdListMap));
    int patientCategoryId = patientDetails.get("patient_category") == null ? 0
        : (Integer) patientDetails.get("patient_category");
    String visitType = null != visitbean ? (String) visitbean.get("visit_type") : null;
    List<BasicDynaBean> categoryWiseTpaNames = tpaService.getAllowedSponsors(patientCategoryId,
        visitType);

    mav.addObject("categoryWiseTpaNames", ConversionUtils.listBeanToListMap(categoryWiseTpaNames));

    List<BasicDynaBean> categoryWiseRateplans = patientCategoryService
        .getAllowedRatePlans(patientCategoryId, visitType);
    mav.addObject("categoryWiseRateplans",
        ConversionUtils.listBeanToListMap(categoryWiseRateplans));

    List<BasicDynaBean> categoryWiseInsuranceComapnies = patientCategoryService
        .getAllowedInsCompanies(patientCategoryId, visitType);

    mav.addObject("categoryWiseInsComps", categoryWiseInsuranceComapnies);

    List<Integer> centerIds = new ArrayList<Integer>();
    centerIds.add(RequestContext.getCenterId());
    List<BasicDynaBean> categoryJSON = patientCategoryService.listByCenter(centerIds, true);

    mav.addObject("categoryJSON", ConversionUtils.listBeanToListMap(categoryJSON));

    List<BasicDynaBean> ratePlanList = organizationService.getValidRatePlans();
    mav.addObject("ratePlanList", ConversionUtils.listBeanToListMap(ratePlanList));

    List<BasicDynaBean> patientPlanDetails = patientInsPlansService
        .getPatientInsuranceDetails(visitId);
    mav.addObject("policyNos", ConversionUtils.listBeanToListMap(patientPlanDetails));

    List<BasicDynaBean> allPatientPlanDetails = patientInsPlansService
        .getAllPatientPlanDetailsUsingMrNo(mrno);

    mav.addObject("allPolicyNos", ConversionUtils.listBeanToListMap(allPatientPlanDetails));

    List<BasicDynaBean> billList = billService.listAll(null, "visit_id", visitId);

    mav.addObject("allTpaBills", ConversionUtils.listBeanToListMap(billList));

    BasicDynaBean regPrefs = registrationPrefService.getRegistrationPreferences();
    mav.addObject("regPref", regPrefs.getMap());

    List<BasicDynaBean> insuranceItemCategories = insuranceItemCategoryService
        .getItemInsuranceCategory();

    mav.addObject("itemCatlist", ConversionUtils.listBeanToListMap(insuranceItemCategories));
    List<BasicDynaBean> sponsorTypeList = sponsorTypeService.listAll();

    mav.addObject("sponsorTypelist", ConversionUtils.listBeanToListMap(sponsorTypeList));

    mav.addObject("tpanames", ConversionUtils.listBeanToListMap(tpaList));

    mav.addObject("oldEditIns", false);

  }

  public void scheduleAccountingForVisitBills(String visitId) {
    List<BasicDynaBean> finalizedBillsList = billService.getVisitFinalizedAndClosedBills(visitId);
    accountingJobScheduler.scheduleAccountingForBills(finalizedBillsList);
  }
}
