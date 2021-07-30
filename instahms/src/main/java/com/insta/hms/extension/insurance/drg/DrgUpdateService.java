package com.insta.hms.extension.insurance.drg;

import com.insta.hms.common.BusinessService;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.billing.BillChargeClaimService;
import com.insta.hms.core.billing.BillChargeService;
import com.insta.hms.core.billing.BillChargeTaxService;
import com.insta.hms.core.billing.BillClaimService;
import com.insta.hms.core.billing.BillService;
import com.insta.hms.core.billing.ChargeRatesService;
import com.insta.hms.core.billing.StoreSalesDetailsService;
import com.insta.hms.core.inventory.sales.SalesClaimDetailsService;
import com.insta.hms.core.patient.registration.PatientInsurancePlansService;
import com.insta.hms.core.patient.registration.RegistrationService;
import com.insta.hms.mdm.drgcodesmaster.DrgCodesMasterService;
import com.insta.hms.mdm.healthauthoritypreferences.HealthAuthorityPreferencesService;
import com.insta.hms.mdm.insuranceplans.InsurancePlanService;

import org.apache.commons.beanutils.BasicDynaBean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class DrgUpdateService.
 */
@Service
public class DrgUpdateService extends BusinessService {

  static Logger logger = LoggerFactory.getLogger(DrgUpdateService.class);

  /**
   * The registration service.
   */
  @LazyAutowired
  private RegistrationService registrationService;

  /**
   * The patient insurance plans service.
   */
  @LazyAutowired
  private PatientInsurancePlansService patientInsurancePlansService;

  /**
   * The insurance plan service.
   */
  @LazyAutowired
  private InsurancePlanService insurancePlanService;

  /**
   * The drg codes master service.
   */
  @LazyAutowired
  private DrgCodesMasterService drgCodesMasterService;

  /**
   * The bill service.
   */
  @LazyAutowired
  private BillService billService;

  /**
   * The bill charge service.
   */
  @LazyAutowired
  private BillChargeService billChargeService;

  /**
   * The bill charge claim service.
   */
  @LazyAutowired
  private BillChargeClaimService billChargeClaimService;

  /**
   * The bill claim service.
   */
  @LazyAutowired
  BillClaimService billClaimService;

  /**
   * The bill charge tax service.
   */
  @LazyAutowired
  BillChargeTaxService billChargeTaxService;

  /**
   * The health auth pref service.
   */
  @LazyAutowired
  StoreSalesDetailsService storeSalesDetailsService;

  /**
   * The sales claim details service.
   */
  @LazyAutowired
  SalesClaimDetailsService salesClaimDetailsService;

  /**
   * The health auth pref service.
   */
  @LazyAutowired
  HealthAuthorityPreferencesService healthAuthPrefService;

  /**
   * The charge rates service.
   */
  @LazyAutowired
  ChargeRatesService chargeRatesService;

  private static final String AMOUNT = "amount";
  private static final String ACT_RATE = "act_rate";
  private static final String ITEM_RATE = "item_rate";
  private static final String DISCOUNT = "discount";
  private static final String ACT_REMARKS = "act_remarks";
  private static final String ACT_DESCRIPTION = "act_description";
  private static final String ACT_DESCRIPTION_ID = "act_description_id";
  private static final String ACT_RATE_PLAN_ITEM_CODE = "act_rate_plan_item_code";
  private static final String BILL_NO = "bill_no";
  private static final String CHARGE_ID = "charge_id";
  private static final String CLAIM_ID = "claim_id";
  private static final String CHARGE_HEAD = "charge_head";
  private static final String DRG_CODE = "drg_code";
  private static final String PATIENT_ID = "patient_id";
  private static final String PRIORITY = "priority";
  private static final String PLAN_ID = "plan_id";
  private static final String RELATIVE_WEIGHT = "relative_weight";
  private static final String STATUS = "status";
  private static final String SALE_ITEM_ID = "sale_item_id";
  private static final String INSURANCE_CLAIM_AMT = "insurance_claim_amt";

  /**
   * Update DRG code.
   *
   * @param visitId the visit id
   * @param drgCode the drg code
   */
  public void updateDRGCode(String visitId, String drgCode) {
    BasicDynaBean visitBean = registrationService.findByKey(visitId);
    visitBean.set(DRG_CODE, drgCode);
    Map<String, Object> keys = new HashMap<>();
    keys.put(PATIENT_ID, visitId);
    registrationService.update(visitBean, keys);
  }

  /**
   * Gets the base rate.
   *
   * @param visitId the visit id
   * @return the base rate
   */
  public BigDecimal getBaseRate(String visitId) {
    Map<String, Object> keys = new HashMap<>();
    keys.put(PATIENT_ID, visitId);
    keys.put(PRIORITY, 1);
    BasicDynaBean visitInsBean = patientInsurancePlansService.findByKeys(keys);
    int planId = (Integer) visitInsBean.get(PLAN_ID);
    Map<String, Object> planKey = new HashMap<>();
    planKey.put(PLAN_ID, planId);
    BasicDynaBean planBean = insurancePlanService.findByKey(planKey);
    return null != planBean.get("base_rate") ? (BigDecimal) planBean.get("base_rate")
        : BigDecimal.ZERO;
  }

  /**
   * Gets the drg code bean.
   *
   * @param drgCode the drg code
   * @return the drg code bean
   */
  public BasicDynaBean getDrgCodeBean(String drgCode) {
    Map<String, Object> keys = new HashMap<>();
    keys.put(DRG_CODE, drgCode);
    return drgCodesMasterService.findByKey(keys);
  }

  /**
   * Gets the charge bean for add on pay ment.
   *
   * @param billNo the bill no
   * @return the charge bean for add on pay ment
   */
  public BasicDynaBean getChargeBeanForAddOnPayMent(String billNo) {
    Map<String, Object> keys = new HashMap<>();
    keys.put(BILL_NO, billNo);
    keys.put(CHARGE_HEAD, "APDRG");
    return billChargeService.findByKeys(keys);
  }

  /**
   * Gets the charge bean for outlier amount.
   *
   * @param billNo the bill no
   * @return the charge bean for outlier amount
   */
  public BasicDynaBean getChargeBeanForOutlierAmount(String billNo) {
    Map<String, Object> keys = new HashMap<>();
    keys.put(BILL_NO, billNo);
    keys.put(CHARGE_HEAD, "OUTDRG");
    return billChargeService.findByKeys(keys);
  }

  /**
   * Gets the drg outlier total amount.
   *
   * @param billNo  the bill no
   * @param visitId the visit id
   * @return the drg outlier total amount
   */
  public BigDecimal getDrgOutlierTotalAmount(String billNo, String visitId) {
    // get center mapped health authority base rate plan
    String baseRateplanId = healthAuthPrefService.getCenterHealthAuthBaseRatePlan();
    BasicDynaBean bill = billService.getBill(billNo);
    BasicDynaBean visitBean = registrationService.findByKey(visitId);
    boolean isInsurance = (boolean) bill.get("is_tpa");
    String bedType = (String) visitBean.get("bed_type");
    String visitType = (String) visitBean.get("visit_type");
    int[] planIds = patientInsurancePlansService.getPlanIds(visitId);
    // get the list of hospital charges
    logger.info("DRG outlier amount calculation is started for hospital items");
    List<BasicDynaBean> chargeBeansList = billChargeService.getAllHospitalChargesForDRG(billNo);
    BigDecimal totHospItemsAmt = BigDecimal.ZERO;
    for (BasicDynaBean chargeBean : chargeBeansList) {
      String chargeId = (String) chargeBean.get(CHARGE_ID);
      String chargeGrp = (String) chargeBean.get("charge_group");
      String chargeHead = (String) chargeBean.get(CHARGE_HEAD);
      String itemId = (String) chargeBean.get(ACT_DESCRIPTION_ID);
      BigDecimal actQuantity = (BigDecimal) chargeBean.get("act_quantity");
      BigDecimal itemDisc = (BigDecimal) chargeBean.get(DISCOUNT);
      BigDecimal itemRate = (BigDecimal) chargeBean.get(ACT_RATE);
      BigDecimal itemAmount = (BigDecimal) chargeBean.get(AMOUNT);
      String actUnit = (String) chargeBean.get("act_unit");
      String actDeptId = (String) chargeBean.get("act_department_id");
      Timestamp fromDate = (Timestamp) chargeBean.get("from_date");
      Timestamp toDate = (Timestamp) chargeBean.get("to_date");
      String opId = (String) chargeBean.get("op_id");

      boolean hasactivity = chargeBean.get("hasactivity") == null ? false : (Boolean) chargeBean
          .get("hasactivity");
      String chargeType = chargeBean.get("consultation_type_id") + ""; // consultationtype

      Map<String, BigDecimal> itemRateMap = null;
      BigDecimal rate = BigDecimal.ZERO;
      BigDecimal discount = BigDecimal.ZERO;
      BigDecimal amount = BigDecimal.ZERO;
      if (baseRateplanId != null && !baseRateplanId.equals("")) {
        itemRateMap = getItemRates(baseRateplanId, bedType, itemId, chargeId, chargeGrp,
            chargeHead, actQuantity, actDeptId, chargeType, actUnit, fromDate, toDate, opId,
            visitId, visitType, isInsurance, planIds, hasactivity, false);

        if (itemRateMap == null) {
          itemRateMap = new HashMap<>();
          itemRateMap.put(ITEM_RATE, itemRate);
          itemRateMap.put(DISCOUNT, itemDisc);
        }
        // calculate amount
        rate = itemRateMap.get(ITEM_RATE) == null ? itemRate : itemRateMap.get(ITEM_RATE);
        discount = itemRateMap.get(DISCOUNT) == null ? itemDisc : itemRateMap.get(DISCOUNT);
        amount = rate.multiply(actQuantity).subtract(discount);
      } else {
        rate = itemRate;
        discount = itemDisc;
        amount = itemAmount;
      }

      totHospItemsAmt = totHospItemsAmt.add(amount);
      logger.info(chargeId
          + "\t"
          + chargeHead
          + "\t"
          + rate
          + "\t"
          + discount
          + "\t"
          + actQuantity
          + "\t"
          + amount
          + "\t"
          + ((String) chargeBean.get(ACT_DESCRIPTION)).substring(0, ((String) chargeBean
          .get(ACT_DESCRIPTION)).length() > 30 ? 30
          : ((String) chargeBean.get(ACT_DESCRIPTION)).length()));
    }

    BigDecimal invAndPharmTotalAmt = getInvAndPharmTotalAmt(billNo);

    BigDecimal totCostBill = totHospItemsAmt.add(invAndPharmTotalAmt);

    // GETAdd-on payment
    BigDecimal addOnPayment = BigDecimal.ZERO;
    BasicDynaBean addOnChgBean = getChargeBeanForAddOnPayMent(billNo);
    if (addOnChgBean != null) {
      addOnPayment = addOnChgBean.get(AMOUNT) == null ? BigDecimal.ZERO : (BigDecimal) addOnChgBean
          .get(AMOUNT);
    }

    BigDecimal marginPer = getDrgMarginPercentage(visitId, 1);
    BigDecimal gapAmount = getDrgGapAmount(visitId, 1);

    BigDecimal drgbasePaymentAmt = getDrgBasePaymentAmount(billNo);
    BigDecimal outlierRate = ((totCostBill.subtract(drgbasePaymentAmt).subtract(addOnPayment)
        .subtract(gapAmount)).multiply(marginPer)).divide(new BigDecimal(100));
    // add totalHospItemsAMt + pharamcy_inventory_items amount -> total cost of bill

    // get the drg margin perc and gap amount from plan

    // get the base payment amount in drg bill --> "BPDRG" DRG Base pay

    // (Total cost -DRG Base pay - Gap - AddOnPayment)*Margin -> X

    // If X > 0 then post outlier payment

    // outlier payment charge -> rate=X, quantity=1, amount=X

    return outlierRate;
  }

  /**
   * The Enum ChargeGroup.
   */
  private enum ChargeGroup {
    REG, DIA, DOC, BED, ICU, OPE, MED, ITE, OTC, TAX, SNP, DIS, PKG, RET, DIE, DRG, PDM;
  }

  /**
   * Gets the item rates.
   *
   * @param orgId           the org id
   * @param bedType         the bed type
   * @param itemId          the item id
   * @param chargeId        the charge id
   * @param chargeGrp       the charge grp
   * @param chargeHead      the charge head
   * @param itemQty         the item qty
   * @param actDeptId       the act dept id
   * @param chargeType      the charge type
   * @param actUnit         the act unit
   * @param fromDate        the from date
   * @param toDate          the to date
   * @param opId            the op id
   * @param visitId         the visit id
   * @param visitType       the visit type
   * @param isInsurance     the is insurance
   * @param planIds         the plan ids
   * @param hasactivity     the hasactivity
   * @param firstOfCategory the first of category
   * @return the item rates
   */
  public Map<String, BigDecimal> getItemRates(String orgId, String bedType, String itemId,
                                              String chargeId, String chargeGrp,
                                              String chargeHead, BigDecimal itemQty,
                                              String actDeptId,
                                              String chargeType, String actUnit,
                                              Timestamp fromDate, Timestamp toDate, String opId,
                                              String visitId, String visitType,
                                              boolean isInsurance, int[] planIds,
                                              boolean hasactivity,
                                              boolean firstOfCategory) {
    Map<String, BigDecimal> rateMap = new HashMap<>();
    chargeRatesService.setChargeRates(visitId, chargeType, planIds, isInsurance);
    chargeRatesService.setPatientDetailsMap(visitId);

    switch (ChargeGroup.valueOf(chargeGrp)) {
      case REG:
        rateMap = chargeRatesService.getRegistrationChargeRate(chargeHead, orgId, bedType, itemId);
        break;
      case DOC:
        rateMap = chargeRatesService.getDoctorCharge(orgId, bedType, itemId, chargeId, itemQty,
            chargeType, hasactivity);
        break;
      case DIA:
        rateMap = chargeRatesService.getDiagCharge(orgId, bedType, itemId, itemQty);
        break;
      case SNP:
        rateMap = chargeRatesService.getServiceCharge(orgId, bedType, itemId, itemQty, actDeptId);
        break;
      case DIE:
        rateMap = chargeRatesService.getDietaryCharge(orgId, bedType, itemId, itemQty);
        break;
      case OTC:
        if (chargeHead.startsWith("EQ")) {
          rateMap = chargeRatesService.getEquipmentCharge(orgId, bedType, itemId, chargeHead,
              chargeId, itemQty, actUnit, fromDate, toDate, hasactivity);
        } else if (chargeHead.startsWith("MIS")) {
          // chargeRates.getAmountFomBillCharge(con,cdto.getChargeId()); 
          // need to keep original charge rates only
        } else {
          rateMap = chargeRatesService.getOtherCharge(orgId, bedType, itemId, itemQty);
        }
        break;
      case PKG:
        if (chargeHead.equals("PKGPKG")) {
          rateMap = chargeRatesService.getPackageCharge(orgId, bedType, itemId, itemQty);
        }
        break;
      case BED:
      case ICU:
        rateMap = chargeRatesService.getBedCharge(orgId, bedType, itemId, itemQty, chargeGrp,
            chargeHead);
        break;
      case OPE:
        if (chargeHead.startsWith("EQ")) {
          rateMap = chargeRatesService.getEquipmentCharge(orgId, bedType, itemId, chargeHead,
              chargeId, itemQty, actUnit, fromDate, toDate, hasactivity);
        } else {
          rateMap = chargeRatesService.getSurgeryCharge(orgId, bedType, itemId, itemQty, chargeId,
              chargeHead, actUnit, fromDate, toDate, opId);
        }
        break;
      default:
        /*
         * Ignore medicine, inventory, tax, DRG, PDM charges and other misc charges as they are rate
         * plan independent charges. so getting value form bill_charge table directly
         */
        break;

    }
    return rateMap;
  }

  /**
   * Gets the inv and pharm total amt.
   *
   * @param billNo the bill no
   * @return the inv and pharm total amt
   */
  public BigDecimal getInvAndPharmTotalAmt(String billNo) {
    BasicDynaBean amountBean = billChargeService.getInvAndPharmTotalAmt(billNo);
    BigDecimal totalInvAndPharmamt = BigDecimal.ZERO;
    if (amountBean != null) {
      totalInvAndPharmamt = (amountBean.get("totalinvandpharmamt") == null) ? BigDecimal.ZERO
          : (BigDecimal) amountBean.get("totalinvandpharmamt");
    }
    return totalInvAndPharmamt;
  }

  /**
   * Gets the drg margin percentage.
   *
   * @param visitId  the visit id
   * @param priority the priority
   * @return the drg margin percentage
   */
  public BigDecimal getDrgMarginPercentage(String visitId, int priority) {
    Map<String, Object> keys = new HashMap<>();
    keys.put(PATIENT_ID, visitId);
    keys.put(PRIORITY, priority);
    BasicDynaBean visitInsBean = patientInsurancePlansService.findByKey(keys);
    int planId = (Integer) visitInsBean.get(PLAN_ID);
    Map<String, Object> planKey = new HashMap<>();
    planKey.put(PLAN_ID, planId);
    BasicDynaBean planBean = insurancePlanService.findByKey(planKey);
    return null != planBean.get("marginal_percent") ? (BigDecimal) planBean.get("marginal_percent")
        : BigDecimal.ZERO;
  }

  /**
   * Gets the drg gap amount.
   *
   * @param visitId  the visit id
   * @param priority the priority
   * @return the drg gap amount
   */
  public BigDecimal getDrgGapAmount(String visitId, int priority) {
    Map<String, Object> keys = new HashMap<>();
    keys.put(PATIENT_ID, visitId);
    keys.put(PRIORITY, priority);
    BasicDynaBean visitInsBean = patientInsurancePlansService.findByKey(keys);
    int planId = (Integer) visitInsBean.get(PLAN_ID);
    Map<String, Object> planKey = new HashMap<>();
    planKey.put(PLAN_ID, planId);
    BasicDynaBean planBean = insurancePlanService.findByKey(planKey);
    return null != planBean.get("gap_amount") ? (BigDecimal) planBean.get("gap_amount")
        : BigDecimal.ZERO;

  }

  /**
   * Gets the drg base payment amount.
   *
   * @param billNo the bill no
   * @return the drg base payment amount
   */
  private BigDecimal getDrgBasePaymentAmount(String billNo) {
    Map keys = new HashMap();
    keys.put(BILL_NO, billNo);
    keys.put(CHARGE_HEAD, "BPDRG");
    BasicDynaBean chargeBean = billChargeService.findByKeys(keys);
    BigDecimal basePaymentAmt = BigDecimal.ZERO;
    if (chargeBean != null) {
      basePaymentAmt = chargeBean.get(AMOUNT) == null ? BigDecimal.ZERO : (BigDecimal) chargeBean
          .get(AMOUNT);
    }
    return basePaymentAmt;
  }

  /**
   * Gets the relative weight.
   *
   * @param drgCode the drg code
   * @return the relative weight
   */
  public BigDecimal getRelativeWeight(String drgCode) {
    Map<String, Object> keys = new HashMap<>();
    keys.put(DRG_CODE, drgCode);
    BasicDynaBean drgCodeBean = drgCodesMasterService.findByKey(keys);
    return null != drgCodeBean.get(RELATIVE_WEIGHT) ? (BigDecimal) drgCodeBean.get(RELATIVE_WEIGHT)
        : BigDecimal.ZERO;
  }

  /**
   * Gets the adds the on pyament factor.
   *
   * @param visitId the visit id
   * @return the adds the on pyament factor
   */
  public BigDecimal getAddOnPyamentFactor(String visitId) {
    Map<String, Object> keys = new HashMap<>();
    keys.put(PATIENT_ID, visitId);
    keys.put(PRIORITY, 1);
    BasicDynaBean visitInsBean = patientInsurancePlansService.findByKeys(keys);
    int planId = (Integer) visitInsBean.get(PLAN_ID);
    Map<String, Object> planKey = new HashMap<>();
    planKey.put(PLAN_ID, planId);
    BasicDynaBean planBean = insurancePlanService.findByKey(planKey);
    return null != planBean.get("add_on_payment_factor") ? (BigDecimal) planBean
        .get("add_on_payment_factor") : new BigDecimal(75);
  }

  /**
   * Gets the HCPCS factor.
   *
   * @param drgCode the drg code
   * @return the HCPCS factor
   */
  public BigDecimal getHCPCSFactor(String drgCode) {
    Map<String, Object> keys = new HashMap<>();
    keys.put(DRG_CODE, drgCode);
    BasicDynaBean drgCodeBean = drgCodesMasterService.findByKey(keys);
    return null != drgCodeBean.get("hcpcs_portion_per") ? (BigDecimal) drgCodeBean
        .get("hcpcs_portion_per") : BigDecimal.ZERO;
  }

  /**
   * Gets the HCPCS payment amt.
   *
   * @param billNo  the bill no
   * @param visitId the visit id
   * @param drgCode the drg code
   * @return the HCPCS payment amt
   */
  public BigDecimal getHCPCSPaymentAmt(String billNo, String visitId, String drgCode) {
    BigDecimal totalHCPCsCost = billChargeService.getAddOnPaymentAmt(billNo);
    BigDecimal hcpcsPayment = BigDecimal.ZERO;
    BigDecimal baseRate = getBaseRate(visitId);
    BigDecimal relativeWeight = getRelativeWeight(drgCode);
    BigDecimal addOnPaymentFactor = getAddOnPyamentFactor(visitId);
    BigDecimal hcpcsFactor = getHCPCSFactor(drgCode);

    BigDecimal drgbasePaymentAmt = baseRate.multiply(relativeWeight);
    BigDecimal accountedCostofHCPCs = drgbasePaymentAmt.multiply(hcpcsFactor.divide(new BigDecimal(
        100)));

    if (totalHCPCsCost != null && totalHCPCsCost.compareTo(accountedCostofHCPCs) > 0) {
      hcpcsPayment = addOnPaymentFactor.divide(new BigDecimal(100)).multiply(
          totalHCPCsCost.subtract(accountedCostofHCPCs));
    }

    return hcpcsPayment;
  }

  /**
   * Gets the adjustment amt.
   *
   * @param billNo the bill no
   * @return the adjustment amt
   */
  public BasicDynaBean getAdjustmentAmt(String billNo) {
    return billChargeService.getAdjustmentAmt(billNo);
  }

  /**
   * Post bill charge.
   *
   * @param bean the bean
   */
  public void postBillCharge(BasicDynaBean bean) {
    billChargeService.insert(bean);
  }

  /**
   * Post bill charge claims.
   *
   * @param chargeBean the charge bean
   * @param visitId    the visit id
   * @param billNo     the bill no
   */
  public void postBillChargeClaims(BasicDynaBean chargeBean, String visitId, String billNo) {
    int[] planIds = patientInsurancePlansService.getPlanIds(visitId);
    List<BasicDynaBean> chargesList = new ArrayList<>();
    chargesList.add(chargeBean);
    BasicDynaBean bill = billService.findByKey(billNo);
    billChargeClaimService.insertBillChargeClaims(chargesList, planIds, visitId, bill, null, null);
  }

  /**
   * Update DRG base payment.
   *
   * @param visitId        the visit id
   * @param billNo         the bill no
   * @param drgCode        the drg code
   * @param basePyamentChg the base pyament chg
   * @throws SQLException the SQL exception
   */
  public void updateDRGBasePayment(String visitId, String billNo, String drgCode,
                                   String basePyamentChg) {
    BigDecimal baseRate = getBaseRate(visitId);
    BasicDynaBean drgCodeBean = getDrgCodeBean(drgCode);
    BigDecimal relativeWeight = null != drgCodeBean.get(RELATIVE_WEIGHT) ? (BigDecimal) drgCodeBean
        .get(RELATIVE_WEIGHT) : BigDecimal.ZERO;
    BigDecimal drgBasePayAmt = baseRate.multiply(relativeWeight);
    BasicDynaBean chgBean = billChargeService.findByKey(CHARGE_ID, basePyamentChg);
    chgBean.set(ACT_RATE, drgBasePayAmt);
    chgBean.set(AMOUNT, drgBasePayAmt);
    chgBean.set(STATUS, "A");
    chgBean.set(ACT_REMARKS, "Base Payment.     BaseRate : " + baseRate + "  RelativeWeight : "
        + relativeWeight);
    chgBean.set(ACT_RATE_PLAN_ITEM_CODE, drgCode);
    chgBean.set(ACT_DESCRIPTION_ID, drgCode);
    Map<String, Object> key = new HashMap<>();
    key.put(CHARGE_ID, basePyamentChg);
    billChargeService.update(chgBean, key);
    Boolean isDrgCodeChanged = !drgCode.equals(chgBean.get(ACT_DESCRIPTION_ID));
    billChargeTaxService.updateBillChargeTaxes(chgBean, isDrgCodeChanged);
  }

  /**
   * Update adjustment entry.
   *
   * @param billNo  the bill no
   * @param visitId the visit id
   * @return the boolean
   */
  public Boolean updateAdjustmentEntry(String billNo, String visitId) {
    Map<String, Object> keys = new HashMap<>();
    keys.put(BILL_NO, billNo);
    keys.put(CHARGE_HEAD, "ADJDRG");
    BasicDynaBean adjChargeBean = billChargeService.findByKeys(keys);
    BasicDynaBean bean = getAdjustmentAmt(billNo);
    BigDecimal adjAmt = BigDecimal.ZERO;
    BigDecimal taxAmt = BigDecimal.ZERO;
    if (null != bean && null != bean.get(AMOUNT)) {
      adjAmt = (BigDecimal) bean.get(AMOUNT);
    }
    if (null != bean && null != bean.get("tax")) {
      taxAmt = (BigDecimal) bean.get("tax");
    }
    adjChargeBean.set(ACT_RATE, adjAmt.negate());
    adjChargeBean.set(AMOUNT, adjAmt.negate());
    adjChargeBean.set("tax_amt", taxAmt.negate());
    adjChargeBean.set(STATUS, "A");
    return billChargeService.update(adjChargeBean, keys) > 0;
  }

  /**
   * Update add on payment.
   *
   * @param visitId the visit id
   * @param billNo  the bill no
   * @param drgCode the drg code
   * @throws SQLException the SQL exception
   */
  public void updateAddOnPayment(String visitId, String billNo, String drgCode) {
    BigDecimal hcpcsPyamentAmt = getHCPCSPaymentAmt(billNo, visitId, drgCode);
    if (hcpcsPyamentAmt != null && hcpcsPyamentAmt.compareTo(BigDecimal.ZERO) == 0) {
      billChargeService.cancelAddOnPaymentDRGItems(billNo);
      billChargeClaimService.cancelAddOnPaymentDRGItems(billNo);
    } else {
      BasicDynaBean chgBean = getChargeBeanForAddOnPayMent(billNo);
      String addOnPaymentRemarks = "HCPCs Add On Payment.   HCPCs Portion Per : "
          + getHCPCSFactor(drgCode) + "  Add On Payment Factor Per : "
          + getAddOnPyamentFactor(visitId);

      String status = (String) chgBean.get(STATUS);

      String itemCode = status.equals("X") ? "98" : (String) chgBean.get(ACT_RATE_PLAN_ITEM_CODE);
      chgBean.set(ACT_RATE, hcpcsPyamentAmt);
      chgBean.set(AMOUNT, hcpcsPyamentAmt);
      chgBean.set(STATUS, "A");
      chgBean.set(ACT_RATE_PLAN_ITEM_CODE, itemCode);
      chgBean.set(ACT_DESCRIPTION_ID, drgCode);
      chgBean.set(ACT_REMARKS, addOnPaymentRemarks);

      Map<String, Object> key = new HashMap<>();
      key.put(CHARGE_ID, chgBean.get(CHARGE_ID));
      billChargeService.update(chgBean, key);
      Boolean isDrgCodeChanged = !drgCode.equals(chgBean.get(ACT_DESCRIPTION_ID));
      billChargeTaxService.updateBillChargeTaxes(chgBean, isDrgCodeChanged);
    }
  }

  /**
   * Update DRG outlier entry.
   *
   * @param visitId the visit id
   * @param billNo  the bill no
   * @param drgCode the drg code
   * @throws SQLException the SQL exception
   */
  public void updateDRGOutlierEntry(String visitId, String billNo, String drgCode) {
    BigDecimal outlierAmt = getDrgOutlierTotalAmount(billNo, visitId);
    if (outlierAmt != null && outlierAmt.compareTo(BigDecimal.ZERO) <= 0) {
      billChargeService.cancelDRGOutlierAmountEntry(billNo);
      billChargeClaimService.cancelDRGOutlierAmountEntry(billNo);
    } else {
      BasicDynaBean chgBean = getChargeBeanForOutlierAmount(billNo);
      String addOnPaymentRemarks = "DRG Outlier Amount update";

      String status = (String) chgBean.get(STATUS);

      String itemCode = status.equals("X") ? "99" : (String) chgBean.get(ACT_RATE_PLAN_ITEM_CODE);
      chgBean.set(ACT_RATE, outlierAmt);
      chgBean.set(AMOUNT, outlierAmt);
      chgBean.set(STATUS, "A");
      chgBean.set(ACT_RATE_PLAN_ITEM_CODE, itemCode);
      chgBean.set(ACT_REMARKS, addOnPaymentRemarks);
      chgBean.set(ACT_DESCRIPTION_ID, drgCode);
      Map<String, Object> key = new HashMap<>();
      key.put(CHARGE_ID, chgBean.get(CHARGE_ID));
      billChargeService.update(chgBean, key);
      Boolean isDrgCodeChanged = !drgCode.equals(chgBean.get(ACT_DESCRIPTION_ID));
      billChargeTaxService.updateBillChargeTaxes(chgBean, isDrgCodeChanged);
    }
  }

  /**
   * Update sales claim amount.
   *
   * @param saleId the sale id
   * @return true, if successful
   */
  public boolean updateSalesClaimAmount(String saleId) {
    boolean success = false;
    List<BasicDynaBean> saleItemlist = storeSalesDetailsService.findAllByKey(Collections
        .singletonMap("sale_id", saleId));
    int count = 0;
    if (null != saleItemlist) {
      for (BasicDynaBean bean : saleItemlist) {
        BasicDynaBean saleItemClaimBean = salesClaimDetailsService.findByKey(Collections
            .singletonMap(SALE_ITEM_ID, bean.get(SALE_ITEM_ID)));
        saleItemClaimBean.set(INSURANCE_CLAIM_AMT, BigDecimal.ZERO);
        saleItemClaimBean.set("return_insurance_claim_amt", BigDecimal.ZERO);
        count = salesClaimDetailsService.update(saleItemClaimBean,
            Collections.singletonMap(SALE_ITEM_ID, bean.get(SALE_ITEM_ID)));
      }
    }
    if (count > 0) {
      success = true;
    }
    return success;

  }

  /**
   * Update claim amount.
   *
   * @param chrg the chrg
   * @return true, if successful
   */
  public boolean updateClaimAmount(BasicDynaBean chrg) {
    String billNo = (String) chrg.get(BILL_NO);
    int cnt = 0;
    boolean success = false;
    if (null != billNo && !billNo.equals("")) {
      BasicDynaBean bcdao = billClaimService.getPrimaryBillClaim(billNo);
      Map<String, Object> keyMap = new HashMap<>();
      keyMap.put(BILL_NO, billNo);
      keyMap.put(CLAIM_ID, bcdao.get(CLAIM_ID));
      keyMap.put(CHARGE_ID, chrg.get(CHARGE_ID));
      BasicDynaBean bccbean = billChargeClaimService.findByKey(keyMap);
      if (null == bccbean) {
        insertChargeClaim(billNo, (String) chrg.get(CHARGE_ID), (String) chrg.get(CHARGE_HEAD));
        bccbean = billChargeClaimService.findByKey(keyMap);
      }
      bccbean.set(INSURANCE_CLAIM_AMT, chrg.get("insurance_claim_amount"));
      cnt = billChargeClaimService.update(bccbean, keyMap);
    }
    if (cnt > 0) {
      success = true;
    }
    return success;
  }

  /**
   * Insert charge claim.
   *
   * @param billNo     the bill no
   * @param chargeId   the charge id
   * @param chargeHead the charge head
   */
  private void insertChargeClaim(String billNo, String chargeId, String chargeHead) {

    BasicDynaBean bcbean = billClaimService.getPrimaryBillClaim(billNo);
    String sponsorId = null;
    String claimId = null;
    if (null != bcbean) {
      sponsorId = (String) bcbean.get("sponsor_id");
      claimId = (String) bcbean.get(CLAIM_ID);
    }
    BasicDynaBean bccbean = billChargeClaimService.getBean();
    bccbean.set(BILL_NO, billNo);
    bccbean.set(CHARGE_ID, chargeId);
    bccbean.set(INSURANCE_CLAIM_AMT, BigDecimal.ZERO);
    bccbean.set("claim_status", "O");
    bccbean.set("claim_recd_total", BigDecimal.ZERO);
    bccbean.set("return_insurance_claim_amt", BigDecimal.ZERO);
    bccbean.set(CHARGE_HEAD, chargeHead);
    bccbean.set(CLAIM_ID, claimId);
    bccbean.set("sponsor_id", sponsorId);

    billChargeClaimService.insert(bccbean);
  }

}
