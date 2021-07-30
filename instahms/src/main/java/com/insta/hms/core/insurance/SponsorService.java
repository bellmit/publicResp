package com.insta.hms.core.insurance;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.taxation.ItemTaxDetails;
import com.insta.hms.common.taxation.TaxContext;
import com.insta.hms.core.billing.BillChargeClaimService;
import com.insta.hms.core.billing.BillChargeClaimTaxService;
import com.insta.hms.core.billing.BillChargeService;
import com.insta.hms.core.billing.BillChargeTaxService;
import com.insta.hms.core.billing.BillHelper;
import com.insta.hms.core.billing.BillRepository;
import com.insta.hms.core.billing.BillService;
import com.insta.hms.core.billing.BillTaxCalculator;
import com.insta.hms.core.billing.InsurancePayableRepository;
import com.insta.hms.core.clinical.eauthorization.EAuthPrescriptionActivitiesRepository;
import com.insta.hms.core.inventory.sales.SalesService;
import com.insta.hms.core.inventory.stocks.StoreCategoryRepository;
import com.insta.hms.core.patient.PatientDetailsService;
import com.insta.hms.core.patient.registration.PatientInsurancePlanDetailsService;
import com.insta.hms.core.patient.registration.PatientInsurancePlansService;
import com.insta.hms.core.patient.registration.RegistrationService;
import com.insta.hms.insurance.AdvanceInsuranceCalculator;
import com.insta.hms.mdm.hospitalcenters.HospitalCenterService;
import com.insta.hms.mdm.item.StoreItemDetailsService;
import com.insta.hms.mdm.taxgroups.TaxGroupService;
import com.insta.hms.mdm.tpas.TpaService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * The Class SponsorService.
 */
@Service
public class SponsorService {
  // TODO : we may not need any prefix,
  // since _ comes
  // after all uppercase letters. We
  // will change this if required.
  // Otherwise, we will have to deal
  // with unpacking it when
  // sending the results back
  /** The Constant NEW_CHARGE_ID_PREFIX. */
  private static final String NEW_CHARGE_ID_PREFIX = "";

  /** The pat ins plan service. */
  @LazyAutowired
  private PatientInsurancePlansService patInsPlanService;

  /** The reg service. */
  @LazyAutowired
  private RegistrationService regService;

  /** The pat insu plan details service. */
  @LazyAutowired
  private PatientInsurancePlanDetailsService patInsuPlanDetailsService;

  /** The bill service. */
  @LazyAutowired
  private BillService billService;

  /** The bill charge claim service. */
  @LazyAutowired
  private BillChargeClaimService billChargeClaimService;

  /** The bill charge service. */
  @LazyAutowired
  private BillChargeService billChargeService;

  /** The ins payable repository. */
  @LazyAutowired
  private InsurancePayableRepository insPayableRepository;

  /** The bill charge tax service. */
  @LazyAutowired
  private BillChargeTaxService billChargeTaxService;

  /** The bill helper. */
  @LazyAutowired
  private BillHelper billHelper;

  /** The bill tax calculator. */
  @LazyAutowired
  private BillTaxCalculator billTaxCalculator;

  /** The tpa service. */
  @LazyAutowired
  private TpaService tpaService;

  /** The bill charge claim tax service. */
  @LazyAutowired
  private BillChargeClaimTaxService billChargeClaimTaxService;

  /** The patient details service. */
  @LazyAutowired
  private PatientDetailsService patientDetailsService;

  /** The hosp center service. */
  @LazyAutowired
  private HospitalCenterService hospCenterService;

  /** The sales service. */
  @LazyAutowired
  private SalesService salesService;

  /** The store item details service. */
  @LazyAutowired
  private StoreItemDetailsService storeItemDetailsService;

  /** The tax group service. */
  @LazyAutowired
  private TaxGroupService taxGroupService;

  /** The store category repository. */
  @LazyAutowired
  private StoreCategoryRepository storeCategoryRepository;
  
  @LazyAutowired
  private EAuthPrescriptionActivitiesRepository eauthPrescActRepo;

  /** The bill repository. */
  @LazyAutowired
  BillRepository billRepository;
  
  @LazyAutowired
  private GenericPreferencesService genericPreferenceService;

  /** The log. */
  private static Logger log = LoggerFactory.getLogger(SponsorService.class);

  /**
   * Recalculate sponsor amount.
   *
   * @param visitId the visit id
   */
  @SuppressWarnings("unchecked")
  public void recalculateSponsorAmount(String visitId) {

    String isClaimAmtIncludesTax = "N";
    String isLimitIncludesTax = "N";
    Boolean includeFollowUpVisits = false;

    List<BasicDynaBean> planList = patInsPlanService.getPlanDetails(visitId);

    for (BasicDynaBean planBean : planList) {
      String limitsIncludeFollowUp = (String) planBean.get("limits_include_followup");
      includeFollowUpVisits = includeFollowUpVisits
          || (null != limitsIncludeFollowUp && limitsIncludeFollowUp.equals("Y"));
      if ((Integer) planBean.get("priority") == 1) {
        isClaimAmtIncludesTax = (String) planBean.get("claim_amount_includes_tax");
        isLimitIncludesTax = (String) planBean.get("limit_includes_tax");
      }
    }

    String mainVisitID = regService.getMainVisitId(visitId);

    String followUpVisitIds = "'".concat(visitId).concat("'");
    Boolean insuranceDetailsFromMainVisit = false;

    if (includeFollowUpVisits) {
      Boolean isMainVisit = visitId.equals(mainVisitID);
      if (isMainVisit) {
        followUpVisitIds = getFollowUpVisitIds(followUpVisitIds, mainVisitID);
      } else {
        if (isMainVisitPlanSameAsFollowupPlan(mainVisitID, visitId)) {
          followUpVisitIds = getFollowUpVisitIds(followUpVisitIds, mainVisitID);
          insuranceDetailsFromMainVisit = true;
        }
      }
    }

    // visitId = (null != mainVisitID && "" != mainVisitID && includeFollowUpVisits &&
    // isInsurancePlansSame) ? mainVisitID : visitId;

    List<BasicDynaBean> visitInsuranceList = patInsuPlanDetailsService
        .getVisitInsDetails(insuranceDetailsFromMainVisit ? mainVisitID : visitId);

    Map<String, BasicDynaBean> additionalDetails = getAdditionalDetails(visitId);

    List<String> chargeTaxToBeAdjusted = new ArrayList<>();

    List<BasicDynaBean> billCharges = billService.getVisitBillCharges(visitId,
        includeFollowUpVisits, followUpVisitIds);

    initializeBillChargeAdjustments(billCharges);

    Map<Integer, List<BasicDynaBean>> visitInsurancePlanMap = ConversionUtils
        .listBeanToMapListBean(visitInsuranceList, "plan_id");

    Map<Integer, Map<Integer, Integer>> adjMap = new HashMap<>();

    for (Entry<Integer, List<BasicDynaBean>> visitInsurancePlan : visitInsurancePlanMap
        .entrySet()) {
      int key = visitInsurancePlan.getKey();
      List<BasicDynaBean> billChargeClaims = billService.getVisitBillChargeClaims(visitId, key,
          includeFollowUpVisits, followUpVisitIds);

      Map<String, List<BasicDynaBean>> billChargeClaimMap = ConversionUtils
          .listBeanToMapListBean(billChargeClaims, "charge_id");

      initializeBillChargeClaimAdjustments(billChargeClaimMap, billCharges);

      Map<Integer, Integer> planAdjMap = new AdvanceInsuranceCalculator().calculate(billCharges,
          billChargeClaimMap, visitInsurancePlan.getValue(), billChargeClaims);

      adjMap.put(key, planAdjMap);

      // tax related calculations
      Map<String, Object> chargeAndSponsorTax = calculateSponsorAmountAndTaxSplit(billCharges,
          billChargeClaimMap, isClaimAmtIncludesTax, isLimitIncludesTax, null, additionalDetails,
          visitId);
      updateBillChargeClaimTaxEntries(billCharges, billChargeClaimMap, chargeAndSponsorTax,
          chargeTaxToBeAdjusted);

      billChargeClaimService.updateBillChargeClaims(billCharges, billChargeClaimMap);

      updateBillChargeTaxAmountsForExempts(chargeTaxToBeAdjusted);
      setBillChargeInsuranceClaimAmt(billCharges, billChargeClaimMap, isClaimAmtIncludesTax,
          isLimitIncludesTax);
    }
    billService.insertBillAdjustmentAlerts(adjMap, visitId);
  }

  /**
   * Gets the follow up visit ids.
   *
   * @param followUpVisitIds the follow up visit ids
   * @param mainVisitID      the main visit ID
   * @return the follow up visit ids
   */
  private String getFollowUpVisitIds(String followUpVisitIds, String mainVisitID) {
    // TODO Auto-generated method stub
    String visitIds = followUpVisitIds;
    Map<String, Object> keys = new HashMap<>();
    keys.put("main_visit_id", mainVisitID);
    List<BasicDynaBean> followUpVisits = regService.listAll(keys);
    for (BasicDynaBean followUpVisitBean : followUpVisits) {
      String followUpVisitId = (String) followUpVisitBean.get("patient_id");
      if (isMainVisitPlanSameAsFollowupPlan(mainVisitID, followUpVisitId)) {
        visitIds = visitIds == null ? "'".concat(followUpVisitId).concat("'")
            : visitIds.concat(",").concat("'".concat(followUpVisitId).concat("'"));
      }
    }
    return visitIds;
  }

  /**
   * Checks if is main visit plan same as followup plan.
   *
   * @param mainVisitID the main visit ID
   * @param visitId     the visit id
   * @return the boolean
   */
  private Boolean isMainVisitPlanSameAsFollowupPlan(String mainVisitID, String visitId) {
    Boolean isPlansSame = false;
    Map<String, Object> keys = new HashMap<>();
    keys.put("patient_id", mainVisitID);

    List<BasicDynaBean> mainVisitPlanBeans = patInsPlanService.listAll(keys);
    keys.put("patient_id", visitId);
    List<BasicDynaBean> followupVisitPlanBeans = patInsPlanService.listAll(keys);
    int mainVisitPriPlanId = null != mainVisitPlanBeans && !mainVisitPlanBeans.isEmpty()
        && null != mainVisitPlanBeans.get(0) ? (Integer) mainVisitPlanBeans.get(0).get("plan_id")
            : 0;
    int mainVisitSecPlanId = (null != mainVisitPlanBeans && mainVisitPlanBeans.size() > 1
        && null != mainVisitPlanBeans.get(1)) ? (Integer) mainVisitPlanBeans.get(1).get("plan_id")
            : 0;

    int visitPriPlanId = null != followupVisitPlanBeans && !followupVisitPlanBeans.isEmpty()
        && null != followupVisitPlanBeans.get(0)
            ? (Integer) followupVisitPlanBeans.get(0).get("plan_id")
            : 0;
    int visitSecPlanId = (null != followupVisitPlanBeans && followupVisitPlanBeans.size() > 1
        && null != followupVisitPlanBeans.get(1))
            ? (Integer) followupVisitPlanBeans.get(1).get("plan_id")
            : 0;
    if (mainVisitPriPlanId == visitPriPlanId && mainVisitSecPlanId == visitSecPlanId) {
      isPlansSame = true;
    }
    return isPlansSame;
  }

  /**
   * Gets the additional details.
   *
   * @param visitId the visit id
   * @return the additional details
   */
  private Map<String, BasicDynaBean> getAdditionalDetails(String visitId) {
    Map<String, BasicDynaBean> details = new HashMap<>();
    BasicDynaBean visiBean = regService.findByKey(visitId);
    if (null != visiBean && !visiBean.getMap().isEmpty()) {
      BasicDynaBean patientBean = patientDetailsService.findByKey((String) visiBean.get("mr_no"));
      BasicDynaBean centerBean = hospCenterService.findByKey((Integer) visiBean.get("center_id"));

      details.put("patient", patientBean);
      details.put("center", centerBean);
      details.put("visit", visiBean);
    }
    return details;
  }

  /**
   * Update bill charge tax amounts for exempts.
   *
   * @param chargeTaxToBeAdjusted the charge tax to be adjusted
   */
  private void updateBillChargeTaxAmountsForExempts(List<String> chargeTaxToBeAdjusted) {
    billChargeTaxService.updateBillChargeTaxAmountsForExempts(chargeTaxToBeAdjusted);
  }

  /**
   * Sets the bill charge insurance claim amt.
   *
   * @param billCharges           the bill charges
   * @param billChargeClaimMap    the bill charge claim map
   * @param isClaimAmtIncludesTax the is claim amt includes tax
   * @param isLimitIncludesTax    the is limit includes tax
   */
  private void setBillChargeInsuranceClaimAmt(List<BasicDynaBean> billCharges,
      Map<String, List<BasicDynaBean>> billChargeClaimMap, String isClaimAmtIncludesTax,
      String isLimitIncludesTax) {
    for (BasicDynaBean charge : billCharges) {
      Boolean isClaimLocked = (Boolean) charge.get("is_claim_locked");
      String chargeId = (String) charge.get("charge_id");
      BasicDynaBean billChgClaimBean = billChargeClaimMap.get(chargeId).get(0);
      if (Boolean.FALSE.equals(isClaimLocked)) {
        BigDecimal claimAmt = (BigDecimal) billChgClaimBean.get("insurance_claim_amt");
        if (isClaimAmtIncludesTax.equals("Y") && isLimitIncludesTax.equals("Y")) {
          claimAmt = claimAmt.add((BigDecimal) billChgClaimBean.get("tax_amt"));
        }

        BigDecimal billChgInsClaimAmt = ((BigDecimal) charge.get("insurance_claim_amount"))
            .add(claimAmt);
        charge.set("insurance_claim_amount", billChgInsClaimAmt);
      } else {
        BigDecimal amount = (BigDecimal) charge.get("amount");
        BigDecimal claimAmt = (BigDecimal) billChgClaimBean.get("insurance_claim_amt");
        if (isClaimAmtIncludesTax.equals("Y") && isLimitIncludesTax.equals("Y")) {
          claimAmt = claimAmt.add((BigDecimal) billChgClaimBean.get("tax_amt"));
        }
        charge.set("amount", amount.subtract(claimAmt));
      }
    }

  }

  /**
   * Initialize bill charge adjustments.
   *
   * @param billCharges the bill charges
   */
  private void initializeBillChargeAdjustments(List<BasicDynaBean> billCharges) {
    for (BasicDynaBean billCharge : billCharges) {
      Boolean isClaimLocked = (Boolean) billCharge.get("is_claim_locked");

      if (!isClaimLocked) {
        billCharge.set("insurance_claim_amount", BigDecimal.ZERO);
        billCharge.set("copay_ded_adj", BigDecimal.ZERO);
        billCharge.set("max_copay_adj", BigDecimal.ZERO);
        billCharge.set("sponsor_limit_adj", BigDecimal.ZERO);
        billCharge.set("copay_perc_adj", BigDecimal.ZERO);
      }
    }
  }

  /**
   * Initialize bill charge claim adjustments.
   *
   * @param billChargeClaimMap the bill charge claim map
   * @param billCharges        the bill charges
   */
  @SuppressWarnings("unchecked")
  private void initializeBillChargeClaimAdjustments(
      Map<String, List<BasicDynaBean>> billChargeClaimMap, List<BasicDynaBean> billCharges) {

    Map<String, BasicDynaBean> billChgMap = ConversionUtils.listBeanToMapBean(billCharges,
        "charge_id");
    for (Entry<String, List<BasicDynaBean>> billChargeClaim : billChargeClaimMap.entrySet()) {
      String key = billChargeClaim.getKey();
      Boolean isClaimLocked = (Boolean) (billChgMap.get(key).get("is_claim_locked"));

      if (!isClaimLocked) {
        List<BasicDynaBean> billChgClaimList = billChargeClaim.getValue();
        // RC Anupama : get(0) is
        // incorrect
        BasicDynaBean billChgClaimBean = billChgClaimList.get(0);
        billChgClaimBean.set("copay_ded_adj", BigDecimal.ZERO);
        billChgClaimBean.set("max_copay_adj", BigDecimal.ZERO);
        billChgClaimBean.set("sponsor_limit_adj", BigDecimal.ZERO);
        billChgClaimBean.set("copay_perc_adj", BigDecimal.ZERO);
      }
    }

  }

  /**
   * Gets the reg screen order items sponosor amount.
   *
   * @param newCharges      the new charges
   * @param visitInsDetails the visit ins details
   * @param adjMap          the adj map
   * @param sponsorTaxMap   the sponsor tax map
   * @param details         the details
   * @param visitId         the visit id
   * @return the reg screen order items sponosor amount
   * @throws SQLException the SQL exception
   */
  public Map<Integer, List<BasicDynaBean>> getRegScreenOrderItemsSponosorAmount(
      List<Map<String, Object>> newCharges, List<BasicDynaBean> visitInsDetails,
      Map<Integer, Map<Integer, Integer>> adjMap, Map<Integer, Object> sponsorTaxMap,
      Map<String, BasicDynaBean> details, String visitId) throws SQLException {
    List<BasicDynaBean> newBeanList = mapToChargeBean(newCharges);
    return calculateRegScreenOrderItemsSponsorAmount(newBeanList, visitInsDetails, adjMap,
        sponsorTaxMap, details, visitId);
  }

  /**
   * Calculate reg screen order items sponsor amount.
   *
   * @param newCharges      the new charges
   * @param visitInsDetails the visit ins details
   * @param adjMap          the adj map
   * @param sponsorTaxMap   the sponsor tax map
   * @param details         the details
   * @param visitId         the visit id
   * @return the map
   * @throws SQLException the SQL exception
   */
  private Map<Integer, List<BasicDynaBean>> calculateRegScreenOrderItemsSponsorAmount(
      List<BasicDynaBean> newCharges, List<BasicDynaBean> visitInsDetails,
      Map<Integer, Map<Integer, Integer>> adjMap, Map<Integer, Object> sponsorTaxMap,
      Map<String, BasicDynaBean> details, String visitId) throws SQLException {
    Map<Integer, List<BasicDynaBean>> retMap = new HashMap<>();

    List<BasicDynaBean> billCharges = new ArrayList<>();

    String isClaimAmtIncludesTax = "N";
    String isLimitIncludesTax = "N";
    BasicDynaBean tpaBean = null;

    if (null != visitInsDetails && visitInsDetails.size() > 0 && null != visitInsDetails.get(0)) {
      String sponsorId = (String) visitInsDetails.get(0).get("sponsor_id");
      Map<String, String> params = new HashMap<String, String>();
      params.put("tpa_id", sponsorId);
      tpaBean = tpaService.findByPk(params);
    }

    if (tpaBean != null) {
      isClaimAmtIncludesTax = (String) tpaBean.get("claim_amount_includes_tax");
      isLimitIncludesTax = (String) tpaBean.get("limit_includes_tax");
    }
    
    String shouldConsiderPreauths = (String) genericPreferenceService.getAllPreferences()
        .get("set_preauth_approved_amt_as_claim_amt");
    if ("Y".equals(shouldConsiderPreauths)) {
      // For charges with preauth id update the approved amount as sponsor amount.
      copyPreauthApprovedAmountAsSponsorAmount(newCharges);
    }

    if (null != newCharges) {
      billCharges.addAll(newCharges);
    }
    initializeBillChargeAdjustments(billCharges);
    if (visitInsDetails == null) {
      throw new RuntimeException("Visit Insurance details is not found");
    }
    Map<Integer, List<BasicDynaBean>> visitInsurancePlanMap = ConversionUtils
        .listBeanToMapListBean(visitInsDetails, "plan_id");

    setCatIdBasedOnPlanIds(newCharges, visitInsurancePlanMap.keySet(), details);

    for (Map.Entry<Integer, List<BasicDynaBean>> visitMapEntry : visitInsurancePlanMap.entrySet()) {
      int planId = visitMapEntry.getKey();
      List<BasicDynaBean> visitInsBeanList = visitMapEntry.getValue();
      BasicDynaBean visitInsBean = visitInsBeanList.get(0);
      int priority = (Integer) visitInsBean.get("priority");

      List<BasicDynaBean> billChargeClaims = new ArrayList<>();

      List<BasicDynaBean> chargeClaimList = getNewChargeClaimList(newCharges, planId, priority,
          visitInsDetails, details);

      if (chargeClaimList != null) {
        billChargeClaims.addAll(chargeClaimList);
      }

      Map<String, List<BasicDynaBean>> billChargeClaimMap = ConversionUtils
          .listBeanToMapListBean(billChargeClaims, "charge_id");
      initializeBillChargeClaimAdjustments(billChargeClaimMap, billCharges);

      Map<Integer, Integer> planAdjMap = new HashMap<>();
      Map<String, Object> chargeAndSponsorTax = new HashMap<>();

      if (null != billCharges) {
        planAdjMap = new AdvanceInsuranceCalculator().calculate(billCharges, billChargeClaimMap,
            visitMapEntry.getValue(), billChargeClaims);

        chargeAndSponsorTax = calculateSponsorAmountAndTaxSplit(billCharges, billChargeClaimMap,
            isClaimAmtIncludesTax, isLimitIncludesTax, tpaBean, details, visitId);
      }

      setBillChargeInsuranceClaimAmt(billCharges, billChargeClaimMap, "N", "N");
      List<BasicDynaBean> resultList = new ArrayList<>();
      for (String chargeId : billChargeClaimMap.keySet()) {
        resultList.addAll(billChargeClaimMap.get(chargeId));
      }
      retMap.put(planId, resultList);
      adjMap.put(planId, planAdjMap);

      if (sponsorTaxMap != null) {
        sponsorTaxMap.put(planId, chargeAndSponsorTax);
      }
    }

    return retMap;
  }

  /**
   * Gets the new charge claim list.
   *
   * @param newCharges       the new charges
   * @param planId           the plan id
   * @param priority         the priority
   * @param visitInsBeanList the visit ins bean list
   * @param details          the details
   * @return the new charge claim list
   */
  private List<BasicDynaBean> getNewChargeClaimList(List<BasicDynaBean> newCharges, int planId,
      int priority, List<BasicDynaBean> visitInsBeanList, Map<String, BasicDynaBean> details) {
    List<BasicDynaBean> list = new ArrayList<BasicDynaBean>();
    for (BasicDynaBean charge : newCharges) {
      BasicDynaBean chargeClaimBean = billChargeClaimService.getBean();
      chargeClaimBean.set("charge_id", charge.get("charge_id"));
      if (charge.get("charge_head") != null) {
        chargeClaimBean.set("charge_head", charge.get("charge_head"));
      }
      Boolean isClaimLocked = (Boolean) charge.get("is_claim_locked");
      chargeClaimBean.set("include_in_claim_calc", true);

      chargeClaimBean.set("insurance_category_id", (Integer) charge.get("insurance_category_id"));
      if (isClaimLocked) {
        if (priority == 1) {
          chargeClaimBean.set("insurance_claim_amt", charge.get("pri_insurance_claim_amount"));
          chargeClaimBean.set("include_in_claim_calc", charge.get("pri_include_in_claim_calc"));
        } else {
          chargeClaimBean.set("insurance_claim_amt", charge.get("sec_insurance_claim_amount"));
          chargeClaimBean.set("include_in_claim_calc", charge.get("sec_include_in_claim_calc"));
        }
      }

      String chargeGroup = (charge.get("charge_group") != null)
          ? charge.get("charge_group").toString() : "";
      // set values for insurance_category_id
      if ("REG".equals(chargeGroup)) {
        setRegistrationChargeCategoryId(charge, chargeClaimBean, planId, visitInsBeanList);
      } else {
        if (details != null && !details.isEmpty()) {
          BasicDynaBean visitBean = details.get("visit");
          billHelper.getInsCatIdFromTemplate(charge, planId, chargeClaimBean,
              (String) visitBean.get("visit_type"));
        }
      }

      // TODO : we need to set the claim id here
      list.add(chargeClaimBean);
    }
    return list;
  }

  /**
   * Sets the registration charge category id.
   *
   * @param charge           the charge
   * @param chargeClaimBean  the charge claim bean
   * @param planId           the plan id
   * @param visitInsBeanList the visit ins bean list
   */
  private void setRegistrationChargeCategoryId(BasicDynaBean charge, BasicDynaBean chargeClaimBean,
      int planId, List<BasicDynaBean> visitInsBeanList) {
    Map<Integer, BasicDynaBean> insuranceCategoryMap = ConversionUtils
        .listBeanToMapBean(visitInsBeanList, "insurance_category_id");
    if (insuranceCategoryMap.containsKey(-1)) {
      chargeClaimBean.set("insurance_category_id", -1);
    }
  }

  private List<BasicDynaBean> mapToChargeBean(List<Map<String, Object>> newCharges) {
    List<BasicDynaBean> list = new ArrayList<BasicDynaBean>();
    // Kludge !! - the view is created only so that we will be able to create a bean with these
    // fields.
    // We will have to get rid of this, with the next revision of this code.

    for (Map<String, Object> newCharge : newCharges) {
      BasicDynaBean chargeBean = insPayableRepository.getBean();
      String chargeId = (String) newCharge.get("charge_id");
      if (null != chargeId) {
        chargeBean.set("charge_id", chargeId);
        Boolean isClaimLocked = (Boolean) newCharge.get("is_claim_locked");
        chargeBean.set("is_claim_locked", isClaimLocked);
        chargeBean.set("insurance_claim_amount", BigDecimal.ZERO);
        chargeBean.set("pri_insurance_claim_amount",
            new BigDecimal((String) newCharge.get("primclaimAmt")));

        chargeBean.set("pri_include_in_claim_calc",
            newCharge.get("pri_include_in_claim") != null
                && (newCharge.get("pri_include_in_claim").equals("Y")
                    || newCharge.get("pri_include_in_claim").equals("")));

        if (null != newCharge.get("secclaimAmt")) {
          chargeBean.set("sec_insurance_claim_amount",
              new BigDecimal((String) newCharge.get("secclaimAmt")));
        }

        if (null != newCharge.get("sec_include_in_claim")) {
          chargeBean.set(
              "sec_include_in_claim_calc",
              newCharge.get("sec_include_in_claim") != null
                  && (newCharge.get("sec_include_in_claim").equals("Y") || newCharge.get(
                      "sec_include_in_claim").equals("")));
        }
        if (null != newCharge.get("package_id")
            && !"".equals(newCharge.get("package_id"))) {
          Integer packageId = newCharge.get("package_id") instanceof String
              ? Integer.valueOf((String) newCharge.get("package_id"))
                  : (Integer) newCharge.get("package_id");
          chargeBean.set("package_id", packageId);
        }
        chargeBean.set("include_in_claim_calc", Boolean.TRUE);
        chargeBean.set("copay_ded_adj", BigDecimal.ZERO);
        chargeBean.set("max_copay_adj", BigDecimal.ZERO);
        chargeBean.set("sponsor_limit_adj", BigDecimal.ZERO);
        chargeBean.set("copay_perc_adj", BigDecimal.ZERO);
        chargeBean.set("amount", newCharge.get("amount"));
        chargeBean.set("discount", newCharge.get("discount"));
        Integer insCatId = newCharge.get("insurance_category_id") instanceof String
            ? Integer.valueOf((String) newCharge.get("insurance_category_id"))
                : (int) newCharge.get("insurance_category_id");
        chargeBean.set("insurance_category_id", insCatId);
        chargeBean.set("is_charge_head_payable", newCharge.get("is_insurance_payable"));
        chargeBean.set("charge_head", newCharge.get("charge_head_id"));
        chargeBean.set("store_item_category_payable", true);
        chargeBean.set("act_description_id", newCharge.get("act_description_id"));
        chargeBean.set("charge_group", newCharge.get("charge_group"));
        chargeBean.set("consultation_type_id", newCharge.get("consultation_type_id"));
        chargeBean.set("op_id", newCharge.get("op_id"));
        if (newCharge.get("item_excluded_from_doctor") != null) {
          if (newCharge.get("item_excluded_from_doctor").equals("Y")
              || newCharge.get("item_excluded_from_doctor").equals(true)) {
            chargeBean.set("item_excluded_from_doctor", true);
          } else if (newCharge.get("item_excluded_from_doctor").equals("N")
              || newCharge.get("item_excluded_from_doctor").equals(false)) {
            chargeBean.set("item_excluded_from_doctor", false);
          }
        }
        chargeBean.set("item_excluded_from_doctor_remarks",
            newCharge.get("item_excluded_from_doctor_remarks"));
        if (null != newCharge.get("preauth_act_id")) {
          chargeBean.set("preauth_act_id", newCharge.get("preauth_act_id"));
        }
        list.add(chargeBean);
      }
    }
    return list;
  }

  /**
   * Gets the ordered item sponsor amount.
   *
   * @param newCharges    the new charges
   * @param editedCharges the edited charges
   * @param visitId       the visit id
   * @param adjMap        the adj map
   * @param sponsorTaxMap the sponsor tax map
   * @return the ordered item sponsor amount
   * @throws SQLException the SQL exception
   */
  public Map<Integer, List<BasicDynaBean>> getOrderedItemSponsorAmount(
      ArrayList<Map<String, Object>> newCharges, List<Map<String, Object>> editedCharges,
      String visitId, Map<Integer, Map<Integer, Integer>> adjMap,
      Map<Integer, Object> sponsorTaxMap, Map<String, Object> additionalInfo) throws SQLException {
    List<BasicDynaBean> newBeanList = mapToChargeBean(newCharges);
    List<BasicDynaBean> editedBeanList = mapToChargeBean(editedCharges);
    // Common Method used when Items ordered from Bill Screen
    
    return calculateSponosorAmount(newBeanList, editedBeanList, visitId, adjMap, sponsorTaxMap,
        null, additionalInfo);
  }

  /**
   * Gets the pharmacy sponsor amount.
   *
   * @param newCharges     the new charges
   * @param visitId        the visit id
   * @param adjMap         the adj map
   * @param sponsorTaxMap  the sponsor tax map
   * @param subGrpCodesMap the sub grp codes map
   * @return the pharmacy sponsor amount
   */
  public Map<Integer, List<BasicDynaBean>> getPharmacySponsorAmount(
      List<Map<String, Object>> newCharges, String visitId,
      Map<Integer, Map<Integer, Integer>> adjMap, Map<Integer, Object> sponsorTaxMap,
      Map<String, List<BasicDynaBean>> subGrpCodesMap) {
    List<BasicDynaBean> newBeanList = mapToSaleBean(newCharges);
    return calculateSponosorAmount(newBeanList, null, visitId, adjMap, sponsorTaxMap,
        subGrpCodesMap);
  }

  /**
   * Map to sale bean.
   *
   * @param newCharges the new charges
   * @return the list
   */
  private List<BasicDynaBean> mapToSaleBean(List<Map<String, Object>> newCharges) {
    List<BasicDynaBean> list = new ArrayList<>();
    for (Map<String, Object> newCharge : newCharges) {
      BasicDynaBean chargeBean = insPayableRepository.getBean();
      String chargeId = (String) newCharge.get("charge_id");
      String chargeHead = (String) newCharge.get("charge_head_id");
      if (null != chargeId) {
        if (chargeId.startsWith("_")) {
          chargeBean.set("charge_id", NEW_CHARGE_ID_PREFIX + chargeId);
        } else {
          chargeBean.set("charge_id", chargeId);
        }
        chargeBean.set("is_claim_locked", newCharge.get("is_claim_locked"));
        chargeBean.set("insurance_claim_amount", BigDecimal.ZERO);
        chargeBean.set("pri_insurance_claim_amount",
            new BigDecimal((String) newCharge.get("primclaimAmt")));

        chargeBean.set("pri_include_in_claim_calc",
            newCharge.get("pri_include_in_claim") != null
                && (newCharge.get("pri_include_in_claim").equals("Y")
                    || newCharge.get("pri_include_in_claim").equals("")));

        if (null != newCharge.get("secclaimAmt")) {
          chargeBean.set("sec_insurance_claim_amount",
              new BigDecimal((String) newCharge.get("secclaimAmt")));
        }
        if (null != newCharge.get("sec_include_in_claim")) {
          chargeBean.set("sec_include_in_claim_calc",
              newCharge.get("sec_include_in_claim") != null
                  && (newCharge.get("sec_include_in_claim").equals("Y")
                      || newCharge.get("sec_include_in_claim").equals("")));
        }

        chargeBean.set("include_in_claim_calc", Boolean.TRUE);
        chargeBean.set("copay_ded_adj", BigDecimal.ZERO);
        chargeBean.set("max_copay_adj", BigDecimal.ZERO);
        chargeBean.set("sponsor_limit_adj", BigDecimal.ZERO);
        chargeBean.set("copay_perc_adj", BigDecimal.ZERO);
        chargeBean.set("amount", new BigDecimal((String) newCharge.get("amount")));
        chargeBean.set("discount", new BigDecimal((String) newCharge.get("discount")));
        chargeBean.set("insurance_category_id",
            Integer.valueOf(((String) newCharge.get("insurance_category_id"))));
        chargeBean.set("is_charge_head_payable", newCharge.get("is_insurance_payable"));
        chargeBean.set("charge_head", chargeHead);
        chargeBean.set("charge_group", newCharge.get("charge_group_id"));
        chargeBean.set("store_item_category_payable", newCharge.get("store_item_category_payable"));
        if (newCharge.get("package_id") != null) {
          chargeBean.set("package_id", newCharge.get("package_id"));
        } else {
          chargeBean.set("act_description_id", newCharge.get("descriptionId"));
        }
        chargeBean.set("consultation_type_id", 0);
        chargeBean.set("claim_amount_includes_tax",
            newCharge.get("claim_amount_includes_tax") != null
                ? (String) newCharge.get("claim_amount_includes_tax")
                : "N");
        setSponsorTaxAmount(chargeBean, newCharge);
        list.add(chargeBean);
      }
    }
    return list;
  }

  /**
   * Sets the sponsor tax amount.
   *
   * @param chargeBean the charge bean
   * @param newCharge  the new charge
   */
  private void setSponsorTaxAmount(BasicDynaBean chargeBean, Map<String, Object> newCharge) {
    Boolean isClaimLocked = (Boolean) newCharge.get("is_claim_locked");
    if (!isClaimLocked) {
      chargeBean.set("pri_claim_tax_amt",
          newCharge.get("priInsClaimTaxAmt") != null
              ? (BigDecimal) newCharge.get("priInsClaimTaxAmt")
              : BigDecimal.ZERO);
      chargeBean.set("sec_claim_tax_amt",
          newCharge.get("secInsClaimTaxAmt") != null
              ? (BigDecimal) newCharge.get("secInsClaimTaxAmt")
              : BigDecimal.ZERO);
    } else {
      // get sponsor tax amount by inputting edited claim amount
      BigDecimal priClaimTaxAmt = BigDecimal.ZERO;
      BigDecimal secClaimTaxAmt = BigDecimal.ZERO;
      String isClaimAmtIncludesTax = newCharge.get("claim_amount_includes_tax") != null
          ? (String) newCharge.get("claim_amount_includes_tax")
          : "N";
      if (isClaimAmtIncludesTax.equals("Y")) {
        final BigDecimal priClaimAmt = (BigDecimal) chargeBean.get("pri_insurance_claim_amount");
        final BigDecimal secClaimAmt = (BigDecimal) chargeBean.get("sec_insurance_claim_amount");
        Map<String, Object> chargeDetMap = chargeBean.getMap();
        chargeDetMap.put("amount", priClaimAmt);
        priClaimTaxAmt = getTaxAmountForCharge(chargeDetMap);
        chargeDetMap.put("amount", secClaimAmt);
        secClaimTaxAmt = getTaxAmountForCharge(chargeDetMap);
      }
      chargeBean.set("pri_claim_tax_amt", priClaimTaxAmt);
      chargeBean.set("sec_claim_tax_amt", secClaimTaxAmt);
    }
  }

  /**
   * Gets the tax amount for charge.
   *
   * @param chargeMap the charge map
   * @return the tax amount for charge
   */
  public BigDecimal getTaxAmountForCharge(Map<String, Object> chargeMap) {

    Map<Integer, Object> itemSubGrpTaxMap = new HashMap<Integer, Object>();
    BigDecimal taxAmt = BigDecimal.ZERO;
    String chargeId = (String) chargeMap.get("charge_id");
    String itemId = (String) chargeMap.get("act_description_id");
    String chargeGroup = (String) chargeMap.get("charge_group");
    String chargeHead = (String) chargeMap.get("charge_head");
    Integer consId = (Integer) chargeMap.get("consultation_type_id");
    String opId = (String) chargeMap.get("op_id");

    boolean isTotalTaxExist = billChargeTaxService.exist("charge_id",
        (String) chargeMap.get("charge_id"), false) || (chargeId).startsWith("_");

    ItemTaxDetails itemTaxDetails = new ItemTaxDetails();
    itemTaxDetails.setAmount((BigDecimal) chargeMap.get("amount"));

    TaxContext taxContext = new TaxContext();

    if (null != itemId && null != chargeGroup && isTotalTaxExist) {
      List<BasicDynaBean> subGroupCodes = new ArrayList<BasicDynaBean>();
      if (!chargeId.startsWith("_")) {
        subGroupCodes = billHelper.getItemSubgroupCodes(chargeId);
      }

      if (subGroupCodes.isEmpty()) {
        subGroupCodes = billHelper.getItemSubgroupCodes(itemId, chargeGroup, chargeHead, consId,
            opId);
      } else {
        taxContext.setTransactionId(chargeId);
      }

      if (subGroupCodes != null && subGroupCodes.size() > 0) {
        itemSubGrpTaxMap = billHelper.getTaxChargesMap(itemTaxDetails, taxContext, subGroupCodes);
      }
    }

    for (Map.Entry<Integer, Object> subGrpMap : itemSubGrpTaxMap.entrySet()) {
      Map<String, String> taxMap = (Map<String, String>) subGrpMap.getValue();
      taxAmt = taxAmt.add(new BigDecimal(taxMap.get("amount")));
    }
    return taxAmt;
  }

  /**
   * Calculate sponosor amount.
   *
   * @param newCharges    the new charges
   * @param editedCharges the edited charges
   * @param visitId       the visit id
   * @param adjMap        the adj map
   * @param sponsorTaxMap the sponsor tax map
   * @return the map
   */
  private Map<Integer, List<BasicDynaBean>> calculateSponosorAmount(List<BasicDynaBean> newCharges,
      List<BasicDynaBean> editedCharges, String visitId, Map<Integer, Map<Integer, Integer>> adjMap,
      Map<Integer, Object> sponsorTaxMap) {
    return calculateSponosorAmount(newCharges, editedCharges, visitId, adjMap, sponsorTaxMap, null);
  }

  /**
   * Calculate sponosor amount.
   *
   * @param newCharges     the new charges
   * @param editedCharges  the edited charges
   * @param visitId        the visit id
   * @param adjMap         the adj map
   * @param sponsorTaxMap  the sponsor tax map
   * @param subGrpCodesMap the sub grp codes map
   * @return the map
   */
  private Map<Integer, List<BasicDynaBean>> calculateSponosorAmount(List<BasicDynaBean> newCharges,
      List<BasicDynaBean> editedCharges, String visitId, Map<Integer, Map<Integer, Integer>> adjMap,
      Map<Integer, Object> sponsorTaxMap, Map<String, List<BasicDynaBean>> subGrpCodesMap) {
    return calculateSponosorAmount(newCharges, editedCharges, visitId, adjMap, sponsorTaxMap,
        subGrpCodesMap, null);
  }
    
  private Map<Integer, List<BasicDynaBean>> calculateSponosorAmount(List<BasicDynaBean> newCharges,
      List<BasicDynaBean> editedCharges, String visitId, Map<Integer, Map<Integer, Integer>> adjMap,
      Map<Integer, Object> sponsorTaxMap, Map<String, List<BasicDynaBean>> subGrpCodesMap,
      Map<String, Object> additionalInformation) {

    // get tax related tpa level details
    String isClaimAmtIncludesTax = "N";
    String isLimitIncludesTax = "N";

    List<BasicDynaBean> planList = patInsPlanService.getPlanDetails(visitId);

    BasicDynaBean tpaBean = null;

    for (BasicDynaBean planBean : planList) {
      if ((Integer) planBean.get("priority") == 1) {
        tpaBean = planBean;
        isClaimAmtIncludesTax = (String) planBean.get("claim_amount_includes_tax");
        isLimitIncludesTax = (String) planBean.get("limit_includes_tax");
      }
    }

    Map<Integer, List<BasicDynaBean>> retMap = new HashMap<Integer, List<BasicDynaBean>>();
    Boolean includeFollowUpVisits = isFollowupVisitsIncluded(visitId);
    String refVisitId = getReferenceVisitId(visitId, includeFollowUpVisits);

    String mainVisitID = refVisitId;

    String followUpVisitIds = "'".concat(visitId).concat("'");
    Boolean insuranceDetailsFromMainVisit = false;

    if (includeFollowUpVisits) {
      Boolean isMainVisit = visitId.equals(mainVisitID);
      if (isMainVisit) {
        followUpVisitIds = getFollowUpVisitIds(followUpVisitIds, mainVisitID);
      } else {
        if (isMainVisitPlanSameAsFollowupPlan(mainVisitID, visitId)) {
          followUpVisitIds = getFollowUpVisitIds(followUpVisitIds, mainVisitID);
          insuranceDetailsFromMainVisit = true;
        }
      }
    }

    Map<Integer, List<BasicDynaBean>> visitInsurancePlanMap = getVisitInsuranceDetails(
        insuranceDetailsFromMainVisit ? refVisitId : visitId);

    BasicDynaBean billBean = billRepository.getBean();
    billBean.set("is_tpa", visitInsurancePlanMap.keySet().size() > 0);
    Map<String, BasicDynaBean> details = getAdditionalDetails(visitId);
    details.put("bill", billBean);

    List<BasicDynaBean> billCharges = getBillChargesList(visitId, includeFollowUpVisits,
        followUpVisitIds);

    if (null != editedCharges && editedCharges.size() > 0 && !planList.isEmpty()) {
      billCharges = setEditedBillCharges(editedCharges, billCharges);
    }
    
    String shouldConsiderPreauths = (String) genericPreferenceService.getAllPreferences()
        .get("set_preauth_approved_amt_as_claim_amt");
    if ("Y".equals(shouldConsiderPreauths)) {
      // For charges with preauth id update the approved amount as sponsor amount.
      copyPreauthApprovedAmountAsSponsorAmount(newCharges);
    }


    initializeBillChargeAdjustments(billCharges);

    setCatIdBasedOnPlanIds(newCharges, visitInsurancePlanMap.keySet(), details);

    if (null != newCharges) {
      billCharges.addAll(newCharges);
    }

    for (Map.Entry<Integer, List<BasicDynaBean>> visitInsurancePlan : visitInsurancePlanMap
        .entrySet()) {
      int planId = visitInsurancePlan.getKey();
      List<BasicDynaBean> visitInsBeanList = visitInsurancePlan.getValue();
      BasicDynaBean visitInsBean = visitInsBeanList.get(0);
      int priority = (Integer) visitInsBean.get("priority");

      List<BasicDynaBean> billChargeClaims = getBillChargeClaimList(visitId, planId,
          includeFollowUpVisits, followUpVisitIds);

      if (null != editedCharges && editedCharges.size() > 0) {
        billChargeClaims = setEditedBillChargeClaims(editedCharges, billChargeClaims, priority);
      }

      List<BasicDynaBean> chargeClaimList = getNewChargeClaimList(newCharges, planId, priority,
          visitInsBeanList, details);

      if (chargeClaimList != null) {
        billChargeClaims.addAll(chargeClaimList);
      }

      Map<String, List<BasicDynaBean>> billChargeClaimMap = ConversionUtils
          .listBeanToMapListBean(billChargeClaims, "charge_id");
      initializeBillChargeClaimAdjustments(billChargeClaimMap, billCharges);
      
      Map<Integer, Integer> planAdjMap = new HashMap<>();
      Map<String, Object> chargeAndSponsorTax = new HashMap<>();

      if (null != billCharges) {
        planAdjMap = new AdvanceInsuranceCalculator().calculate(billCharges, billChargeClaimMap,
            visitInsurancePlan.getValue(), billChargeClaims, additionalInformation);

        chargeAndSponsorTax = calculateSponsorAmountAndTaxSplit(billCharges, billChargeClaimMap,
            isClaimAmtIncludesTax, isLimitIncludesTax, tpaBean, details, visitId, subGrpCodesMap);
      }

      setBillChargeInsuranceClaimAmt(billCharges, billChargeClaimMap, "N", "N");
      List<BasicDynaBean> resultList = new ArrayList<>();
      for (String chargeId : billChargeClaimMap.keySet()) {
        resultList.addAll(billChargeClaimMap.get(chargeId));
      }
      retMap.put(planId, resultList);
      adjMap.put(planId, planAdjMap);
      if (sponsorTaxMap != null) {
        sponsorTaxMap.put(planId, chargeAndSponsorTax);
      }
    }

    return retMap;
  }
  
  private void copyPreauthApprovedAmountAsSponsorAmount(List<BasicDynaBean> chargeList) {
    for (BasicDynaBean charge : chargeList) {
      Integer preAuthActId = (Integer) charge.get("preauth_act_id");
      if (null == preAuthActId || (Boolean)charge.get("is_claim_locked")) {
        continue;
      }
      BasicDynaBean preAuthActBean = eauthPrescActRepo.findByKey("preauth_act_id", preAuthActId);
      if (null == preAuthActBean) {
        continue;
      }

      BigDecimal approvedAmt = (BigDecimal) preAuthActBean.get("claim_net_approved_amount");
      String preauthActStatus = (String) preAuthActBean.get("preauth_act_status");
      boolean preauthApprovedOrDenied = "C".equals(preauthActStatus)
          || "D".equals(preauthActStatus);
      if (preauthApprovedOrDenied && approvedAmt != null) {
        // Assume quantity will be one when ordering.
        BigDecimal quantity = BigDecimal.ONE;
        Integer approvedQuantity = (Integer) preAuthActBean.get("approved_qty");
        if (quantity != null && approvedQuantity != null && approvedQuantity > 0
            && quantity.compareTo(BigDecimal.ZERO) > 0
            && approvedAmt.compareTo(BigDecimal.ZERO) > 0) {
          BigDecimal approvedPerItemAmt = approvedAmt.divide(new BigDecimal(approvedQuantity),
              RoundingMode.HALF_EVEN);
          approvedAmt = approvedPerItemAmt
              .multiply(new BigDecimal(Math.min(quantity.intValue(), approvedQuantity.intValue())));
        } else {
          approvedAmt = BigDecimal.ZERO;
        }
        // Always update only primary insurance amount if updating from preauth.
        charge.set("pri_insurance_claim_amount", approvedAmt);
        charge.set("is_claim_locked", true);
      }
    }
  }

  /**
   * Sets the cat id based on plan ids.
   *
   * @param newCharges the new charges
   * @param keySet     the key set
   * @param details    the details
   */
  private void setCatIdBasedOnPlanIds(List<BasicDynaBean> newCharges, Set<Integer> keySet,
      Map<String, BasicDynaBean> details) {
    if (keySet.toArray().length == 0) {
      return;
    }

    Map<String, Object> itemMap = billHelper.getItemMapForInsuranceCategories(newCharges);

    billHelper.getCatIdBasedOnPlanIds(newCharges, itemMap, keySet, details);

  }

  /**
   * Checks if is followup visits included.
   *
   * @param visitId the visit id
   * @return the boolean
   */
  private Boolean isFollowupVisitsIncluded(String visitId) {
    Boolean includeFollowUpVisits = false;
    List<BasicDynaBean> planList = patInsPlanService.getPlanDetails(visitId);

    for (BasicDynaBean planBean : planList) {
      String limitsIncludeFollowUp = (String) planBean.get("limits_include_followup");
      includeFollowUpVisits = includeFollowUpVisits
          || (null != limitsIncludeFollowUp && limitsIncludeFollowUp.equals("Y"));
    }
    return includeFollowUpVisits;

  }

  /**
   * Gets the reference visit id.
   *
   * @param visitId               the visit id
   * @param includeFollowUpVisits the include follow up visits
   * @return the reference visit id
   */
  private String getReferenceVisitId(String visitId, Boolean includeFollowUpVisits) {
    String mainVisitID = regService.getMainVisitId(visitId);
    String refVisitId = (null != mainVisitID && includeFollowUpVisits) ? mainVisitID : visitId;
    return refVisitId;
  }

  /**
   * Gets the visit insurance details.
   *
   * @param visitId the visit id
   * @return the visit insurance details
   */
  @SuppressWarnings("unchecked")
  private Map<Integer, List<BasicDynaBean>> getVisitInsuranceDetails(String visitId) {
    List<BasicDynaBean> visitInsuranceList = patInsuPlanDetailsService.getVisitInsDetails(visitId);
    Map<Integer, List<BasicDynaBean>> visitInsurancePlanMap = ConversionUtils
        .listBeanToMapListBean(visitInsuranceList, "plan_id");
    return visitInsurancePlanMap;
  }

  /**
   * Gets the bill charges list.
   *
   * @param visitId               the visit id
   * @param includeFollowUpVisits the include follow up visits
   * @param followUpVisitIds      the follow up visit ids
   * @return the bill charges list
   */
  protected List<BasicDynaBean> getBillChargesList(String visitId, Boolean includeFollowUpVisits,
      String followUpVisitIds) {
    List<BasicDynaBean> billCharges = billService.getVisitBillCharges(visitId,
        includeFollowUpVisits, followUpVisitIds);
    return billCharges;
  }

  /**
   * Sets the edited bill charges.
   *
   * @param editedCharges the edited charges
   * @param billCharges   the bill charges
   * @return the list
   */
  @SuppressWarnings("unchecked")
  private List<BasicDynaBean> setEditedBillCharges(List<BasicDynaBean> editedCharges,
      List<BasicDynaBean> billCharges) {

    Map<String, BasicDynaBean> billChgMap = ConversionUtils.listBeanToMapBean(billCharges,
        "charge_id");

    for (BasicDynaBean bean : editedCharges) {
      String chargeId = (String) bean.get("charge_id");
      BigDecimal amount = (BigDecimal) bean.get("amount");
      BigDecimal discount = (BigDecimal) bean.get("discount");
      Boolean isClaimLocked = (Boolean) bean.get("is_claim_locked");
      if (billChgMap.get(chargeId) != null) {
        billChgMap.get(chargeId).set("amount", amount);
        billChgMap.get(chargeId).set("discount", discount);
        billChgMap.get(chargeId).set("is_claim_locked", isClaimLocked);
      }
    }

    List<BasicDynaBean> retList = new ArrayList<BasicDynaBean>();

    for (Map.Entry<String, BasicDynaBean> entry : billChgMap.entrySet()) {
      retList.add(entry.getValue());
    }

    return retList;

  }

  /**
   * Gets the bill charge claim list.
   *
   * @param visitId               the visit id
   * @param planId                the plan id
   * @param includeFollowUpVisits the include follow up visits
   * @param followUpVisitIds      the follow up visit ids
   * @return the bill charge claim list
   */
  protected List<BasicDynaBean> getBillChargeClaimList(String visitId, Integer planId,
      Boolean includeFollowUpVisits, String followUpVisitIds) {
    List<BasicDynaBean> billChargeClaims = billService.getVisitBillChargeClaims(visitId, planId,
        includeFollowUpVisits, followUpVisitIds);
    return billChargeClaims;
  }

  /**
   * Sets the edited bill charge claims.
   *
   * @param editedCharges    the edited charges
   * @param billChargeClaims the bill charge claims
   * @param priroity         the priroity
   * @return the list
   */
  @SuppressWarnings("unchecked")
  private List<BasicDynaBean> setEditedBillChargeClaims(List<BasicDynaBean> editedCharges,
      List<BasicDynaBean> billChargeClaims, int priroity) {
    Map<String, BasicDynaBean> billChgClaimMap = ConversionUtils.listBeanToMapBean(billChargeClaims,
        "charge_id");
    for (BasicDynaBean bean : editedCharges) {
      String chargeId = (String) bean.get("charge_id");
      if (billChgClaimMap.get(chargeId) != null) {
        BigDecimal claimAmt = priroity == 1 ? (BigDecimal) bean.get("pri_insurance_claim_amount")
            : (BigDecimal) bean.get("sec_insurance_claim_amount");
        billChgClaimMap.get(chargeId).set("insurance_claim_amt", claimAmt);
        Boolean includeInClaimCalc = priroity == 1 ? (Boolean) bean.get("pri_include_in_claim_calc")
            : (Boolean) bean.get("sec_include_in_claim_calc");
        billChgClaimMap.get(chargeId).set("include_in_claim_calc", includeInClaimCalc);
      }
    }

    List<BasicDynaBean> retList = new ArrayList<BasicDynaBean>();

    for (Map.Entry<String, BasicDynaBean> entry : billChgClaimMap.entrySet()) {
      retList.add(entry.getValue());
    }

    return retList;
  }

  /**
   * Calculate sponsor amount and tax split.
   *
   * @param billCharges           the bill charges
   * @param billChargeClaimMap    the bill charge claim map
   * @param isClaimAmtIncludesTax the is claim amt includes tax
   * @param isLimitIncludesTax    the is limit includes tax
   * @param tpaBean               the tpa bean
   * @param details               the details
   * @param visitId               the visit id
   * @return the map
   */
  private Map<String, Object> calculateSponsorAmountAndTaxSplit(List<BasicDynaBean> billCharges,
      Map<String, List<BasicDynaBean>> billChargeClaimMap, String isClaimAmtIncludesTax,
      String isLimitIncludesTax, BasicDynaBean tpaBean, Map<String, BasicDynaBean> details,
      String visitId) {
    return calculateSponsorAmountAndTaxSplit(billCharges, billChargeClaimMap, isClaimAmtIncludesTax,
        isLimitIncludesTax, tpaBean, details, visitId, null);
  }

  /**
   * Calculate sponsor amount and tax split.
   *
   * @param billCharges           the bill charges
   * @param billChargeClaimMap    the bill charge claim map
   * @param isClaimAmtIncludesTax the is claim amt includes tax
   * @param isLimitIncludesTax    the is limit includes tax
   * @param tpaBean               the tpa bean
   * @param details               the details
   * @param visitId               the visit id
   * @param subGrpCodesMap        the sub grp codes map
   * @return the map
   */
  private Map<String, Object> calculateSponsorAmountAndTaxSplit(List<BasicDynaBean> billCharges,
      Map<String, List<BasicDynaBean>> billChargeClaimMap, String isClaimAmtIncludesTax,
      String isLimitIncludesTax, BasicDynaBean tpaBean, Map<String, BasicDynaBean> details,
      String visitId, Map<String, List<BasicDynaBean>> subGrpCodesMap) {

    String itemId = null;
    String chargeGroup = null;
    String chargeHead = null;
    int consId = 0;
    String opId = null;
    BigDecimal amount = BigDecimal.ZERO;
    BigDecimal insClaimAmt = BigDecimal.ZERO;
    // It will hold
    // charge and its
    // sponsor tax
    // details
    Map<String, Object> chargeAndSponsorTax = new HashMap<>();
    Map<String, List<BasicDynaBean>> chargeTaxBeansMap = new HashMap<>();
    if (null != visitId && !visitId.equals("")) {
      chargeTaxBeansMap = ConversionUtils.listBeanToMapListBean(
          billChargeTaxService.getAllHospitalItemsContainingTotalTax(visitId), "charge_id");
    }
    for (BasicDynaBean chargeBean : billCharges) {
      Boolean isClaimLocked = (Boolean) chargeBean.get("is_claim_locked");
      final String chargeId = (String) chargeBean.get("charge_id");

      // if(!isClaimLocked){
      Map<String, Object> itemSponsorTaxMap = new HashMap<>();
      chargeGroup = (String) chargeBean.get("charge_group");
      chargeHead = (String) chargeBean.get("charge_head");
      itemId = "PKG".equals(chargeGroup) && chargeBean.get("package_id") != null
          ? String.valueOf(chargeBean.get("package_id"))
              : (String) chargeBean.get("act_description_id");
      amount = (BigDecimal) chargeBean.get("amount");
      consId = (Integer) chargeBean.get("consultation_type_id");
      opId = (String) chargeBean.get("op_id");

      insClaimAmt = (BigDecimal) ((billChargeClaimMap.get(chargeId)).get(0))
          .get("insurance_claim_amt");
      boolean isTotalTaxExist = false;
      if ((chargeId).startsWith("_")) {
        isTotalTaxExist = true;
      } else if (chargeGroup.equals("MED")) {
        isTotalTaxExist = true;
      } else if (null != chargeTaxBeansMap && !chargeTaxBeansMap.isEmpty()) {
        isTotalTaxExist = chargeTaxBeansMap.containsKey((String) chargeBean.get("charge_id"));
      }

      ItemTaxDetails itemTaxDetails = new ItemTaxDetails();
      itemTaxDetails.setAmount(insClaimAmt);
      // itemTaxDetails.setInsClaimAmt(insClaimAmt);

      TaxContext taxContext = new TaxContext();
      if (null == tpaBean) {
        taxContext.setItemBean(chargeBean);
      } else {
        taxContext.setItemBean(tpaBean);
      }

      taxContext.setPatientBean(details.get("patient"));
      taxContext.setCenterBean(details.get("center"));
      taxContext.setBillBean(details.get("bill"));
      taxContext.setVisitBean(details.get("visit"));

      if (null != itemId && null != chargeGroup && isTotalTaxExist) {
        // Used to get the Item sub group codes.
        // List<BasicDynaBean> subGroupCodes = billingHelper.getItemSubgroupCodes(itemId,
        // chargeGroup, chargeHead, consId);

        List<BasicDynaBean> subGroupCodes = new ArrayList<BasicDynaBean>();

        if (subGrpCodesMap != null && subGrpCodesMap.get(chargeId) != null) {
          subGroupCodes = subGrpCodesMap.get(chargeId);
        } else if (chargeGroup.equals("MED")) {

          if (!chargeId.startsWith("_")) {
            subGroupCodes = salesService.getItemSubgroupCodes(chargeId);
          }

          if (subGroupCodes == null || subGroupCodes.isEmpty()) {
            subGroupCodes = storeItemDetailsService.getMedicineSubgroups(Integer.parseInt(itemId));
          }
        } else {
          if (!chargeId.startsWith("_")) {
            subGroupCodes = billHelper.getItemSubgroupCodes(chargeId);
          }

          if (subGroupCodes.isEmpty()) {
            subGroupCodes = billHelper.getItemSubgroupCodes(itemId, chargeGroup, chargeHead, consId,
                opId);
          } else {
            taxContext.setTransactionId(chargeId);
          }
        }

        if (subGroupCodes != null && subGroupCodes.size() > 0) {
          if (isClaimAmtIncludesTax.equals("Y") && isLimitIncludesTax.equals("Y")
              && !isClaimLocked) {
            taxContext.setSubgroups(subGroupCodes);
            itemTaxDetails.setTaxBasis("A");
            itemSponsorTaxMap = billHelper.calculateSponsorTaxes(itemTaxDetails, taxContext,
                subGroupCodes);
          } else if (isClaimAmtIncludesTax.equals("Y")) {
            itemSponsorTaxMap = billHelper.calculateSponsorTaxes(itemTaxDetails, taxContext,
                subGroupCodes);
          }

          // getTaxDetails(taxMap, subGroupCodes, itemSponsorTaxMap);
          chargeAndSponsorTax.put((String) chargeBean.get("charge_id"), itemSponsorTaxMap);
        }
      }
      // }
    }
    return chargeAndSponsorTax;
  }

  /**
   * Update bill charge claim tax entries.
   *
   * @param billCharges           the bill charges
   * @param billChargeClaimMap    the bill charge claim map
   * @param chargeAndSponsorTax   the charge and sponsor tax
   * @param chargeTaxToBeAdjusted the charge tax to be adjusted
   */
  private void updateBillChargeClaimTaxEntries(List<BasicDynaBean> billCharges,
      Map<String, List<BasicDynaBean>> billChargeClaimMap, Map<String, Object> chargeAndSponsorTax,
      List<String> chargeTaxToBeAdjusted) {

    boolean success = true;
    for (BasicDynaBean chargeBean : billCharges) {

      String isClaimAmtIncludesTax = "N";
      // String isLimitIncludesTax = "N";

      String chargeId = (String) chargeBean.get("charge_id");
      String chargeType = (String) chargeBean.get("charge_type");

      Map<String, Object> sponsorTaxDetailsMap = (Map<String, Object>) chargeAndSponsorTax
          .get((String) chargeBean.get("charge_id"));

      if (sponsorTaxDetailsMap == null || sponsorTaxDetailsMap.size() <= 0) {
        continue;
      }

      String claimId = (String) ((billChargeClaimMap.get((String) chargeBean.get("charge_id")))
          .get(0)).get("claim_id");
      String sponsorId = (String) ((billChargeClaimMap.get((String) chargeBean.get("charge_id")))
          .get(0)).get("sponsor_id");
      Map<String, String> params = new HashMap<String, String>();
      params.put("tpa_id", sponsorId);
      BasicDynaBean tpaBean = tpaService.findByPk(params);

      if (tpaBean != null) {
        isClaimAmtIncludesTax = (String) tpaBean.get("claim_amount_includes_tax");
        // isLimitIncludesTax = (String)tpaBean.get("limit_includes_tax");
      }
      // insClaimAmt =
      // (BigDecimal)((billChargeClaimMap.get((String)
      // chargeBean.get("charge_id"))).get(0)).get("insurance_claim_amt");
      BigDecimal totSponsorTax = BigDecimal.ZERO; // total tax amount per claim
      BigDecimal sponsorAmt = (BigDecimal) sponsorTaxDetailsMap.get("sponsorAmount");
      if (sponsorAmt != null) {
        ((billChargeClaimMap.get((String) chargeBean.get("charge_id"))).get(0))
            .set("insurance_claim_amt", sponsorAmt);
      }
      // key:
      // subgroupid,
      // value:
      // map<str,map<str,str>>
      Map<Integer, Object> subGrpSponTaxDetailsMap = (Map<Integer, Object>) sponsorTaxDetailsMap
          .get("subGrpSponTaxDetailsMap");
      if (subGrpSponTaxDetailsMap != null && isClaimAmtIncludesTax.equals("Y")) {
        for (Map.Entry<Integer, Object> subgroupTaxDetMap : subGrpSponTaxDetailsMap.entrySet()) {

          Integer taxSubGroupId = subgroupTaxDetMap.getKey();
          Map<String, String> subGroupTaxSplit = (Map<String, String>) subgroupTaxDetMap.getValue();

          if (chargeType.equals("pharmacy")) {
            int saleItemId = Integer.parseInt(chargeId.split("-")[1]);
            Map<String, Object> saleClaimTaxKeyMap = new HashMap<String, Object>();
            saleClaimTaxKeyMap.put("sale_item_id", saleItemId);
            saleClaimTaxKeyMap.put("claim_id", claimId);
            saleClaimTaxKeyMap.put("item_subgroup_id", taxSubGroupId);
            BasicDynaBean salesClaimTaxBean = salesService.getSalesClaimTaxBean(saleClaimTaxKeyMap);

            if (salesClaimTaxBean != null) {
              salesClaimTaxBean.set("tax_rate",
                  new BigDecimal((String) subGroupTaxSplit.get("rate")));
              salesClaimTaxBean.set("tax_amt",
                  new BigDecimal((String) subGroupTaxSplit.get("amount")));
              if (null != subGroupTaxSplit.get("adjTaxAmt")
                  && !subGroupTaxSplit.get("adjTaxAmt").equals("")) {
                salesClaimTaxBean.set("adj_amt", (String) subGroupTaxSplit.get("adjTaxAmt"));
              }
              salesService.updateSalesClaimTaxBean(salesClaimTaxBean, saleClaimTaxKeyMap);
            } else {
              salesClaimTaxBean = salesService.getSalesClaimTaxBean();
              salesClaimTaxBean.set("sale_item_id", saleItemId);
              salesClaimTaxBean.set("claim_id", claimId);
              salesClaimTaxBean.set("item_subgroup_id", taxSubGroupId);
              salesClaimTaxBean.set("tax_rate",
                  new BigDecimal((String) subGroupTaxSplit.get("rate")));
              salesClaimTaxBean.set("tax_amt",
                  new BigDecimal((String) subGroupTaxSplit.get("amount")));
              if (null != subGroupTaxSplit.get("adjTaxAmt")
                  && !subGroupTaxSplit.get("adjTaxAmt").equals("")) {
                salesClaimTaxBean.set("adj_amt", (String) subGroupTaxSplit.get("adjTaxAmt"));
              }
              salesService.insertSalesClaimTaxBean(salesClaimTaxBean);
            }
            totSponsorTax = totSponsorTax.add((BigDecimal) salesClaimTaxBean.get("tax_amt"));

          } else {

            Map<String, Object> chargeTaxMap = new HashMap<String, Object>();
            chargeTaxMap.put("charge_id", chargeId);
            chargeTaxMap.put("tax_sub_group_id", taxSubGroupId);
            BasicDynaBean chargeTaxBean = billChargeTaxService.findByKey(chargeTaxMap);

            int chargeTaxId = 0;
            if (null != chargeTaxBean && null != chargeTaxBean.get("charge_tax_id")) {
              chargeTaxId = (Integer) chargeTaxBean.get("charge_tax_id");
            }
            Map<String, Object> claimTaxKeyMap = new HashMap<String, Object>();
            claimTaxKeyMap.put("charge_id", chargeId);
            claimTaxKeyMap.put("claim_id", claimId);
            claimTaxKeyMap.put("charge_tax_id", chargeTaxId);

            BasicDynaBean claimTaxBean = billChargeClaimTaxService.findByKey(claimTaxKeyMap);
            if (claimTaxBean != null) {
              claimTaxBean.set("tax_rate", new BigDecimal((String) subGroupTaxSplit.get("rate")));
              claimTaxBean.set("sponsor_tax_amount",
                  new BigDecimal((String) subGroupTaxSplit.get("amount")));
              claimTaxBean.set("tax_sub_group_id", taxSubGroupId);
              claimTaxBean.set("charge_tax_id", chargeTaxId);
              if (null != subGroupTaxSplit.get("adjTaxAmt")
                  && !subGroupTaxSplit.get("adjTaxAmt").equals("")) {
                claimTaxBean.set("adj_amt", (String) subGroupTaxSplit.get("adjTaxAmt"));
                chargeTaxToBeAdjusted.add(chargeId);
              }
              billChargeClaimTaxService.update(claimTaxBean, claimTaxKeyMap);
            } else {
              claimTaxBean = billChargeClaimTaxService.getBean();
              claimTaxBean.set("charge_id", chargeId);
              claimTaxBean.set("claim_id", claimId);
              claimTaxBean.set("sponsor_id", sponsorId);
              claimTaxBean.set("tax_sub_group_id", taxSubGroupId);
              claimTaxBean.set("tax_rate", new BigDecimal((String) subGroupTaxSplit.get("rate")));
              claimTaxBean.set("sponsor_tax_amount",
                  new BigDecimal((String) subGroupTaxSplit.get("amount")));
              if (null != subGroupTaxSplit.get("adjTaxAmt")
                  && !subGroupTaxSplit.get("adjTaxAmt").equals("")) {
                claimTaxBean.set("adj_amt", (String) subGroupTaxSplit.get("adjTaxAmt"));
                chargeTaxToBeAdjusted.add(chargeId);
              }
              claimTaxBean.set("charge_tax_id", chargeTaxId);
              billChargeClaimTaxService.insert(claimTaxBean);
            }
            totSponsorTax = totSponsorTax.add((BigDecimal) claimTaxBean.get("sponsor_tax_amount"));
          }
        }
      }
      ((billChargeClaimMap.get((String) chargeBean.get("charge_id"))).get(0)).set("tax_amt",
          totSponsorTax);
    }
  }

  /**
   * Gets the issues charge claims.
   *
   * @param params the params
   * @return the issues charge claims
   */
  public Map<Integer, Map> getIssuesChargeClaims(Map<String, String[]> params) {

    String visitId = params.get("visitId")[0];
    // get tpa credentials
    String isClaimAmtIncludesTax = "N";
    String isLimitIncludesTax = "N";
    String packageId = params.get("package_id") != null ? params.get("package_id")[0]
        : null;

    Integer pkgId = null;

    if (StringUtils.isNotBlank(packageId) && NumberUtils.isParsable(packageId)) {
      pkgId = Integer.parseInt(packageId);
    }

    BasicDynaBean sponsorDetailsBean = patInsPlanService.getPlanDetails(visitId).get(0);

    if (sponsorDetailsBean != null) {
      isClaimAmtIncludesTax = (String) sponsorDetailsBean.get("claim_amount_includes_tax");
      isLimitIncludesTax = (String) sponsorDetailsBean.get("limit_includes_tax");
    }

    String[] chargeIds = params.get("temp_charge_id");
    String[] medDisc = params.get("discountAmtHid");
    String[] orgTaxAmt = params.get("original_tax");
    String[] taxAmt = params.get("tax_amt");
    String[] amt = params.get("amt");
    String[] primarypatIncClaimAmt = params.get("pri_ins_amt");
    String[] secondarypatIncClaimAmt = params.get("sec_ins_amt");
    String[] insurancecategory = params.get("insurancecategory");
    String[] hdeleted = params.get("hdeleted");
    String[] storeItemCategories = params.get("category");
    String[] priInsClaimTaxAmt = params.get("pri_ins_tax");
    String[] secInsClaimTaxAmt = params.get("sec_ins_tax");
    String[] medicineId = params.get("medicine_id");
    // String[] itemSubgroupIds =
    // ((String)params.get("tax_sub_group_ids")[0]).trim().split("\\,");

    List<BasicDynaBean> groupList = taxGroupService.getTaxItemGroups();
    Map<String, List<BasicDynaBean>> subGrpCodesMap = new HashMap<>();

    int deletedIndex = 0;
    List<Map<String, Object>> newCharges = new ArrayList<>();

    for (String chargeId : chargeIds) {

      if (chargeId == null || chargeId.equals("") || hdeleted[deletedIndex].equals("true")) {
        deletedIndex++;
        continue;
      }
      List<BasicDynaBean> subGroupsList = new ArrayList<>();
      List<String> subGroupIds = new ArrayList<>();
      for (int j = 0; j < groupList.size(); j++) {
        BasicDynaBean groupBean = (BasicDynaBean) groupList.get(j);
        int groupId = groupBean.get("item_group_id") != null
            ? (Integer) groupBean.get("item_group_id")
            : 0;
        if (params.get("taxsubgroupid" + groupId) != null
            && ((String[]) params.get("taxsubgroupid" + groupId))[deletedIndex] != null
            && !((String[]) params.get("taxsubgroupid" + groupId))[deletedIndex].isEmpty()) {
          String taxSubgroupId = ((String[]) params.get("taxsubgroupid" + groupId))[deletedIndex];
          subGroupIds.add(taxSubgroupId);
        }
      }
      Iterator<String> subGroupsIdIterator = subGroupIds.iterator();
      while (subGroupsIdIterator.hasNext()) {
        String subGroupId = subGroupsIdIterator.next();
        if (null != subGroupId && !subGroupId.trim().isEmpty()) {
          BasicDynaBean subGrpBean = billChargeTaxService
              .getMasterSubGroupDetails(Integer.parseInt(subGroupId));
          subGroupsList.add(subGrpBean);
        }
      }
      subGrpCodesMap.put(chargeId, subGroupsList);
      BigDecimal amount = BigDecimal.ZERO;
      BigDecimal taxAmount = BigDecimal.ZERO;
      BigDecimal amountWithTax = BigDecimal.ZERO;
      // amount = new BigDecimal(amt[i]).subtract(new BigDecimal(taxAmt[i]));
      // amountWithTax = amount.add(new BigDecimal(orgTaxAmt[i]));
      //
      amount = new BigDecimal(amt[deletedIndex]);
      amountWithTax = amount.add(new BigDecimal(orgTaxAmt[deletedIndex]));

      taxAmount = new BigDecimal(taxAmt[deletedIndex]);

      Map<String, Object> issueMap = new HashMap<>();

      issueMap.put("claim_amount_includes_tax", isClaimAmtIncludesTax);
      issueMap.put("limit_includes_tax", isLimitIncludesTax);
      issueMap.put("charge_id", chargeIds[deletedIndex]);
      if (pkgId != null) {
        issueMap.put("charge_group_id", "PKG");
      } else {
        issueMap.put("charge_group_id", "ITE");
      }
      issueMap.put("charge_head_id", "INVITE");
      issueMap.put("primclaimAmt", primarypatIncClaimAmt[deletedIndex].equals("") ? "0"
          : primarypatIncClaimAmt[deletedIndex]);
      issueMap.put("secclaimAmt", secondarypatIncClaimAmt[deletedIndex].equals("") ? "0"
          : secondarypatIncClaimAmt[deletedIndex]);
      issueMap.put("discount", medDisc[deletedIndex].equals("") ? "0" : medDisc[deletedIndex]);
      issueMap.put("insurance_category_id", insurancecategory[deletedIndex]);
      // TODO : this should come from the
      // page
      issueMap.put("is_insurance_payable", Boolean.TRUE);
      issueMap.put("is_claim_locked", Boolean.FALSE);
      if (pkgId != null) {
        issueMap.put("store_item_category_payable", Boolean.TRUE);
      } else {
        Boolean storeItemCategoryPayable = isStoreItemCatgeoryPayable(
            storeItemCategories[deletedIndex]);
        issueMap.put("store_item_category_payable", storeItemCategoryPayable);
      }
      issueMap.put("pri_include_in_claim", true);
      issueMap.put("sec_include_in_claim", true);
      if (null != priInsClaimTaxAmt && !priInsClaimTaxAmt[deletedIndex].equals("")) {
        issueMap.put("priInsClaimTaxAmt", new BigDecimal(priInsClaimTaxAmt[deletedIndex]));
      } else {
        issueMap.put("priInsClaimTaxAmt", BigDecimal.ZERO);
      }

      if (null != secInsClaimTaxAmt && !secInsClaimTaxAmt[deletedIndex].equals("")) {
        issueMap.put("secInsClaimTaxAmt", new BigDecimal(secInsClaimTaxAmt[deletedIndex]));
      } else {
        issueMap.put("secInsClaimTaxAmt", BigDecimal.ZERO);
      }

      if (isClaimAmtIncludesTax.equals("Y") && isLimitIncludesTax.equals("Y")) {
        issueMap.put("amount", String.valueOf(amountWithTax));
      } else {
        issueMap.put("amount", String.valueOf(amount));
      }

      if (pkgId != null) {
        issueMap.put("package_id", pkgId);
      } else {
        issueMap.put("descriptionId", medicineId[deletedIndex]);
      }

      issueMap.put("tax_amt", taxAmount);

      newCharges.add(issueMap);
      deletedIndex++;
    }
    Map<Integer, Map<Integer, Integer>> adjMap = new HashMap<>();
    Map<Integer, Object> sponsorTaxMap = new HashMap<>();
    Map<Integer, List<BasicDynaBean>> issueItemClaimsMap = getPharmacySponsorAmount(newCharges,
        visitId, adjMap, sponsorTaxMap, subGrpCodesMap);
    Map<String, String> adjTaxMap = new HashMap<>();
    for (Entry<Integer, List<BasicDynaBean>> issueItemClaims : issueItemClaimsMap.entrySet()) {
      // process the tax amount
      Integer key = issueItemClaims.getKey();
      Map<String, Object> chargeAndSponsorTaxMap = (Map<String, Object>) sponsorTaxMap.get(key);
      List<BasicDynaBean> saleChgList = issueItemClaims.getValue();
      for (BasicDynaBean saleChgBean : saleChgList) {
        String chargeIdStr = (String) saleChgBean.get("charge_id");
        BigDecimal totSponsorTax = BigDecimal.ZERO;
        BigDecimal sponsorAmount = (BigDecimal) saleChgBean.get("insurance_claim_amt");
        totSponsorTax = (BigDecimal) saleChgBean.get("tax_amt") == null ? BigDecimal.ZERO
            : (BigDecimal) saleChgBean.get("tax_amt");
        Map<String, Object> sponsorTaxAndSplitMap = (Map<String, Object>) chargeAndSponsorTaxMap
            .get(chargeIdStr);
        String adjTaxAmt = "N";
        if (sponsorTaxAndSplitMap != null && sponsorTaxAndSplitMap.size() > 0) {
          totSponsorTax = BigDecimal.ZERO;
          sponsorAmount = (sponsorTaxAndSplitMap.get("sponsorAmount") != null)
              ? (BigDecimal) sponsorTaxAndSplitMap.get("sponsorAmount")
              : (BigDecimal) saleChgBean.get("insurance_claim_amt");

          Map<Integer, Object> subGrpCodesTaxMap = (Map<Integer, Object>) sponsorTaxAndSplitMap
              .get("subGrpSponTaxDetailsMap");
          for (Map.Entry<Integer, Object> subGrpTaxAmountsMap : subGrpCodesTaxMap.entrySet()) {
            Integer subGrpCodeId = subGrpTaxAmountsMap.getKey();
            Map<String, String> subgrpTaxDetails = (Map<String, String>) subGrpTaxAmountsMap
                .getValue();
            totSponsorTax = totSponsorTax
                .add(new BigDecimal((String) subgrpTaxDetails.get("amount")));
            if (null != subgrpTaxDetails.get("adjTaxAmt")
                && subgrpTaxDetails.get("adjTaxAmt").equals("Y")) {
              adjTaxAmt = "Y";
            }
          }
        }
        saleChgBean.set("insurance_claim_amt", sponsorAmount);
        saleChgBean.set("tax_amt", totSponsorTax);
        adjTaxMap.put(chargeIdStr, adjTaxAmt);
      }
    }
    Map<Integer, Map> issueChargesMap = new HashMap<>();

    for (Entry<Integer, List<BasicDynaBean>> issueItemClaim : issueItemClaimsMap.entrySet()) {
      Integer key = issueItemClaim.getKey();
      List<BasicDynaBean> saleChgList = issueItemClaim.getValue();

      List listmap = ConversionUtils.listBeanToListMap(saleChgList);

      issueChargesMap.put(key, ConversionUtils.listMapToMapMap(listmap, "charge_id"));
      for (BasicDynaBean bean : saleChgList) {
        log.info(bean.get("charge_id") + "  " + bean.get("insurance_claim_amt") + "  "
            + bean.get("insurance_category_id"));
      }
    }
    issueChargesMap.put(-2, adjTaxMap);

    return issueChargesMap;
  }

  /**
   * Checks if is store item catgeory payable.
   *
   * @param storeItemCategory the store item category
   * @return the boolean
   */
  private Boolean isStoreItemCatgeoryPayable(String storeItemCategory) {
    BasicDynaBean storeCatBean = storeCategoryRepository.findByKey("category_id",
        Integer.parseInt(storeItemCategory));
    return (Boolean) storeCatBean.get("claimable");
  }

}
