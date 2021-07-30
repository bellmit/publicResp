package com.insta.hms.insurance;

import com.insta.hms.billing.BillDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.master.PlanMaster.PlanDetailsDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Calculator to calculate claim amount for Advance Insurance Case.

/**
 * The Class AdvanceInsuranceCalculator.
 */
public class AdvanceInsuranceCalculator implements InsuranceCalculator {
  // TODO : Need to remove this method once we complete the new calculate claim method coding.
  /**
   * Calculate.
   *
   * @param amount          the amount
   * @param discount        the discount
   * @param billNo          the bill no
   * @param planId          the plan id
   * @param firstOfCategory the first of category
   * @param visitType       the visit type
   * @param categoryId      the category id
   * @return claimamt the claimamount
   * @throws SQLException the SQL exception
   */
  public BigDecimal calculateClaim(BigDecimal amount, BigDecimal discount, String billNo,
      int planId, Boolean firstOfCategory, String visitType, int categoryId) throws SQLException {

    if (visitType == null || visitType.equals("")) {
      if (billNo != null && !billNo.equals("")) {
        visitType = BillDAO.getBillTypeAndVisitType(billNo).getVisitType();
      }
    }

    BasicDynaBean planDetails = PlanDetailsDAO.getChargeAmtForPlan(planId, categoryId, visitType);

    // this means that there is no row in the plan details for this category.
    // Assume full amount paid by insurance
    if (null == planDetails) {
      return amount;
    }

    BigDecimal patAmt = (BigDecimal) planDetails.get("patient_amount");
    BigDecimal catPatAmt = (BigDecimal) planDetails.get("patient_amount_per_category");
    BigDecimal patPer = (BigDecimal) planDetails.get("patient_percent");

    BigDecimal chargeAmount = amount;
    boolean isNegativeAmt = false;
    if (chargeAmount.compareTo(BigDecimal.ZERO) < 0) {
      isNegativeAmt = true;
    }

    if (isNegativeAmt) {
      chargeAmount = chargeAmount.negate();
    }
    BigDecimal percentChargeAmount = ((String) planDetails.get("is_copay_pc_on_post_discnt_amt"))
        .equals("Y") ? chargeAmount : amount.add(discount);

    // Calculate patient co-pay
    BigDecimal coPay = ConversionUtils.setScale(patPer.multiply(percentChargeAmount)
        .divide(new BigDecimal("100"), BigDecimal.ROUND_HALF_UP));

    // add fixed patient amount, cap it to charge amount
    BigDecimal patientAmount = coPay;

    if (firstOfCategory == true) {
      patientAmount = coPay.add(patAmt).add(catPatAmt).min(chargeAmount);
    } else {
      patientAmount = patientAmount.add(patAmt).min(chargeAmount);
    }
    BigDecimal patCap = (BigDecimal) planDetails.get("patient_amount_cap");
    // cap it to max patient amount
    if (patCap != null) {
      patientAmount = patientAmount.min(patCap);
    }
    BigDecimal claimAmount = BigDecimal.ZERO;
    // this.insuranceClaimAmount = chargeAmount.subtract(patientAmount);
    claimAmount = chargeAmount.subtract(patientAmount);
    if (isNegativeAmt) {
      claimAmount = claimAmount.negate();
    }

    return claimAmount;
  }

  // New calculate claim method

  /**
   * Calculate Claim.
   *
   * @param amount           the amount
   * @param discount         the discount
   * @param billNo           the bill no
   * @param planId           the plan id
   * @param firstOfCategory  the first of category
   * @param visitType        the visit type
   * @param categoryId       the category id
   * @param insurancePayable the insurance payable
   * @return claimamt the claimamount
   * @throws SQLException the SQL exception
   */
  public BigDecimal calculateClaim(BigDecimal amount, BigDecimal discount, String billNo,
      int planId, Boolean firstOfCategory, String visitType, int categoryId,
      boolean insurancePayable) throws SQLException {

    if (visitType == null || visitType.equals("")) {
      if (billNo != null && !billNo.equals("")) {
        visitType = BillDAO.getBillTypeAndVisitType(billNo).getVisitType();
      }
    }

    BasicDynaBean planDetails = PlanDetailsDAO.getChargeAmtForPlan(planId, categoryId, visitType);

    // this means that there is no row in the plan details for this category.
    // Assume full amount paid by insurance
    if (null == planDetails) {
      return amount;
    }

    BigDecimal patAmt = (BigDecimal) planDetails.get("patient_amount");
    BigDecimal catPatAmt = (BigDecimal) planDetails.get("patient_amount_per_category");
    BigDecimal patPer = (BigDecimal) planDetails.get("patient_percent");

    BigDecimal chargeAmount = amount;
    boolean isNegativeAmt = false;
    if (chargeAmount.compareTo(BigDecimal.ZERO) < 0) {
      isNegativeAmt = true;
    }

    if (isNegativeAmt) {
      chargeAmount = chargeAmount.negate();
    }
    BigDecimal percentChargeAmount = ((String) planDetails.get("is_copay_pc_on_post_discnt_amt"))
        .equals("Y") ? chargeAmount : amount.add(discount);

    // Calculate patient co-pay
    BigDecimal coPay = ConversionUtils.setScale(patPer.multiply(percentChargeAmount)
        .divide(new BigDecimal("100"), BigDecimal.ROUND_HALF_UP));

    // add fixed patient amount, cap it to charge amount
    BigDecimal patientAmount = coPay;

    if (firstOfCategory == true) {
      patientAmount = coPay.add(patAmt).add(catPatAmt).min(chargeAmount);
    } else {
      patientAmount = patientAmount.add(patAmt).min(chargeAmount);
    }
    BigDecimal claimAmount = BigDecimal.ZERO;
    claimAmount = chargeAmount.subtract(patientAmount);
    BigDecimal maxCopay = (BigDecimal) planDetails.get("patient_amount_cap");
    BigDecimal sponsorLimit = (BigDecimal) planDetails.get("per_treatment_limit");
    // cap it to max patient amount
    if (maxCopay != null && maxCopay.compareTo(BigDecimal.ZERO) > 0) {
      patientAmount = patientAmount.min(maxCopay);
      claimAmount = chargeAmount.subtract(patientAmount);
    } else if (sponsorLimit != null && sponsorLimit.compareTo(BigDecimal.ZERO) > 0) {
      claimAmount = claimAmount.min(sponsorLimit);
    }

    if (isNegativeAmt) {
      claimAmount = claimAmount.negate();
    }

    return claimAmount;
  }

  /**
   * Spring method.
   *
   * @param amount           the amount
   * @param discount         the discount
   * @param planId           the plan id
   * @param firstOfCategory  the first of category
   * @param categoryId       the category id
   * @param insurancePayable the insurance payable
   * @param planDetails      the plan details
   * @return the big decimal
   */
  public BigDecimal calculateClaim(BigDecimal amount, BigDecimal discount, int planId,
      Boolean firstOfCategory, int categoryId, boolean insurancePayable,
      BasicDynaBean planDetails) {

    // this means that there is no row in the plan details for this category.
    // Assume full amount paid by insurance
    if (null == planDetails) {
      return amount;
    }

    BigDecimal patAmt = (BigDecimal) planDetails.get("patient_amount");
    BigDecimal catPatAmt = (BigDecimal) planDetails.get("patient_amount_per_category");
    BigDecimal patPer = (BigDecimal) planDetails.get("patient_percent");

    BigDecimal chargeAmount = amount;
    boolean isNegativeAmt = chargeAmount.compareTo(BigDecimal.ZERO) < 0;

    if (isNegativeAmt) {
      chargeAmount = chargeAmount.negate();
    }
    BigDecimal percentChargeAmount = ((String) planDetails.get("is_copay_pc_on_post_discnt_amt"))
        .equals("Y") ? chargeAmount : amount.add(discount);

    // Calculate patient co-pay
    BigDecimal coPay = ConversionUtils.setScale(patPer.multiply(percentChargeAmount)
        .divide(new BigDecimal("100"), BigDecimal.ROUND_HALF_UP));

    // add fixed patient amount, cap it to charge amount
    BigDecimal patientAmount = coPay;

    if (firstOfCategory) {
      patientAmount = coPay.add(patAmt).add(catPatAmt).min(chargeAmount);
    } else {
      patientAmount = patientAmount.add(patAmt).min(chargeAmount);
    }
    BigDecimal claimAmount = BigDecimal.ZERO;
    claimAmount = chargeAmount.subtract(patientAmount);
    BigDecimal maxCopay = (BigDecimal) planDetails.get("patient_amount_cap");
    BigDecimal sponsorLimit = (BigDecimal) planDetails.get("per_treatment_limit");
    // cap it to max patient amount
    if (maxCopay != null && maxCopay.compareTo(BigDecimal.ZERO) > 0) {
      patientAmount = patientAmount.min(maxCopay);
      claimAmount = chargeAmount.subtract(patientAmount);
    } else if (sponsorLimit != null && sponsorLimit.compareTo(BigDecimal.ZERO) > 0) {
      claimAmount = claimAmount.min(sponsorLimit);
    }

    if (isNegativeAmt) {
      claimAmount = claimAmount.negate();
    }
    return claimAmount;
  }

  /*
   * billCharges - list of charges.
   *
   * billChargeClaims - map of billChargeClaims. key=chargeId, value=list of billChargeClaims for
   * the billCharge
   */
  
  public Map<Integer, Integer> calculate(List<BasicDynaBean> billCharges,
      Map<String, List<BasicDynaBean>> billChargeClaimsMap, List<BasicDynaBean> visitInsuranceList,
      List<BasicDynaBean> billChargeClaims) {
    return calculate(billCharges, billChargeClaimsMap, visitInsuranceList, billChargeClaims, null);
  }

  /**
   * Calculate.
   *
   * @param billCharges         the bill charges
   * @param billChargeClaimsMap the bill charge claims map
   * @param visitInsuranceList  the visit insurance list
   * @param billChargeClaims    the bill charge claims
   * @return the map
   */
  public Map<Integer, Integer> calculate(List<BasicDynaBean> billCharges,
      Map<String, List<BasicDynaBean>> billChargeClaimsMap, List<BasicDynaBean> visitInsuranceList,
      List<BasicDynaBean> billChargeClaims, Map<String, Object> additionalDetails) {

    ItemInsuranceCalculator itemCalculator = new ItemInsuranceCalculator();

    Map<Boolean, List<BasicDynaBean>> billChargeMap = new HashMap<Boolean, List<BasicDynaBean>>(
        ConversionUtils.listBeanToMapListBean(billCharges, "is_claim_locked"));

    List<BasicDynaBean> editableBillCharges = billChargeMap.get(false);

    if (null != editableBillCharges) {
      for (BasicDynaBean billCharge : editableBillCharges) {
        String chargeId = (String) billCharge.get("charge_id");
        List<BasicDynaBean> billChargeClaimList = billChargeClaimsMap.get(chargeId);

        itemCalculator.calculate(billCharge, billChargeClaimList, visitInsuranceList);
      }
    }

    CategoryInsuranceCalculator categoryCalculator = new CategoryInsuranceCalculator();

    Map<Integer, Integer> catAdjMap = new HashMap<Integer, Integer>();

    catAdjMap = categoryCalculator.calculate(billCharges, billChargeClaims, visitInsuranceList);

    Map<Integer, Integer> visitAdjMap = new HashMap<Integer, Integer>();

    VisitInsuranceCalculator visitCalculator = new VisitInsuranceCalculator();

    Map<Boolean, List<BasicDynaBean>> billChgIncluedInCalMap = ConversionUtils
        .listBeanToMapListBean(billCharges, "include_in_claim_calc");

    List<BasicDynaBean> billChgListIncludedInCalc = billChgIncluedInCalMap.get(true);

    Map<String, List<BasicDynaBean>> billChgClaimsIncludedInCalc = 
        new HashMap<String, List<BasicDynaBean>>();

    if (null != billChgListIncludedInCalc) {
      for (BasicDynaBean chgBean : billChgListIncludedInCalc) {
        String chargeId = (String) chgBean.get("charge_id");
        billChgClaimsIncludedInCalc.put(chargeId, billChargeClaimsMap.get(chargeId));
      }

      visitAdjMap = visitCalculator.calculate(billChgListIncludedInCalc,
          billChgClaimsIncludedInCalc, visitInsuranceList, additionalDetails);
    }

    Map<Integer, Integer> adjMap = new HashMap<Integer, Integer>();

    adjMap.putAll(catAdjMap);
    adjMap.putAll(visitAdjMap);

    for (BasicDynaBean charge : billCharges) {
      String chargeId = (String) charge.get("charge_id");
      Boolean isClaimLocked = (Boolean) charge.get("is_claim_locked");
      if (!isClaimLocked) {
        List<BasicDynaBean> billChgClaimList = billChargeClaimsMap.get(chargeId);
        for (BasicDynaBean billChgClaim : billChgClaimList) {
          BigDecimal insClaimAmt = (null != billChgClaim.get("insurance_claim_amt")) 
              ? (BigDecimal) billChgClaim.get("insurance_claim_amt") : BigDecimal.ZERO;
          BigDecimal copayDedAdj = (null != billChgClaim.get("copay_ded_adj")) 
              ? (BigDecimal) billChgClaim.get("copay_ded_adj") : BigDecimal.ZERO;
          BigDecimal maxCopayAdj = (null != billChgClaim.get("max_copay_adj")) 
              ? (BigDecimal) billChgClaim.get("max_copay_adj") : BigDecimal.ZERO;
          BigDecimal spnLimitAdj = (null != billChgClaim.get("sponsor_limit_adj")) 
              ? (BigDecimal) billChgClaim.get("sponsor_limit_adj") : BigDecimal.ZERO;
          BigDecimal copayPercAdj = (null != billChgClaim.get("copay_perc_adj")) 
              ? (BigDecimal) billChgClaim.get("copay_perc_adj") : BigDecimal.ZERO;

          billChgClaim.set("insurance_claim_amt",
              insClaimAmt.add(copayDedAdj.add(maxCopayAdj.add(spnLimitAdj.add(copayPercAdj)))));
        }
      }
    }

    return adjMap;

  }

}
