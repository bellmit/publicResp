package com.insta.hms.insurance;

import org.apache.commons.beanutils.BasicDynaBean;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The Class AdvanceInsuranceHelper.
 */
public class AdvanceInsuranceHelper {

  /**
   * Gets the total copay.
   *
   * @param billCharges      the bill charges
   * @param billChargeClaims the bill charge claims
   * @return the total copay
   */
  public BigDecimal getTotalCopay(List<BasicDynaBean> billCharges,
      Map<String, BasicDynaBean> billChargeClaims) {
    BigDecimal totCopay = BigDecimal.ZERO;
    for (BasicDynaBean billCharge : billCharges) {

      String chargeId = (String) billCharge.get("charge_id");
      BasicDynaBean billChgClaimBean = billChargeClaims.get(chargeId);
      if (billChgClaimBean == null) {
        return totCopay;
      }
      Boolean includeInClaimCalculation = (Boolean) billChgClaimBean.get("include_in_claim_calc");
      Boolean isClaimLocked = (Boolean) billCharge.get("is_claim_locked");
      if (Boolean.TRUE.equals(includeInClaimCalculation)) {
        if (Boolean.FALSE.equals(isClaimLocked)) {
          BigDecimal amount = ((BigDecimal) billCharge.get("amount"))
              .subtract((BigDecimal) billCharge.get("insurance_claim_amount"));
          BigDecimal insuranceClaimAmt = (null != billChgClaimBean.get("insurance_claim_amt")) 
              ? (BigDecimal) billChgClaimBean.get("insurance_claim_amt") : BigDecimal.ZERO;
          BigDecimal copayDedAdj = (null != billChgClaimBean.get("copay_ded_adj")) 
              ? (BigDecimal) billChgClaimBean.get("copay_ded_adj") : BigDecimal.ZERO;
          BigDecimal maxCopayAdj = (null != billChgClaimBean.get("max_copay_adj")) 
              ? (BigDecimal) billChgClaimBean.get("max_copay_adj") : BigDecimal.ZERO;
          BigDecimal spnrLimitAdj = (null != billChgClaimBean.get("sponsor_limit_adj")) 
              ? (BigDecimal) billChgClaimBean.get("sponsor_limit_adj") : BigDecimal.ZERO;
          BigDecimal copayPercAdj = (null != billChgClaimBean.get("copay_perc_adj")) 
              ? (BigDecimal) billChgClaimBean.get("copay_perc_adj") : BigDecimal.ZERO;

          BigDecimal totClaimAmt = insuranceClaimAmt
              .add(copayDedAdj.add(maxCopayAdj.add(spnrLimitAdj.add(copayPercAdj))));
          totCopay = totCopay.add(amount.subtract(totClaimAmt));
        } else {
          BigDecimal amount = (BigDecimal) billCharge.get("amount");
          BigDecimal insuranceClaimAmt = (BigDecimal) billChgClaimBean.get("insurance_claim_amt");
          totCopay = totCopay.add(amount.subtract(insuranceClaimAmt));
        }
      }

    }
    return totCopay;
  }

  /**
   * Gets the total sponsor amt.
   *
   * @param billCharges      the bill charges
   * @param billChargeClaims the bill charge claims
   * @return the total sponsor amt
   */
  public BigDecimal getTotalSponsorAmt(List<BasicDynaBean> billCharges,
      Map<String, BasicDynaBean> billChargeClaims) {

    BigDecimal totalSponsorAmt = BigDecimal.ZERO;

    for (BasicDynaBean billCharge : billCharges) {

      String chargeId = (String) billCharge.get("charge_id");
      BasicDynaBean billChgClaimBean = billChargeClaims.get(chargeId);
      if (billChgClaimBean == null) {
        return totalSponsorAmt;
      }
      Boolean includeInClaimCalculation = (Boolean) billChgClaimBean.get("include_in_claim_calc");
      Boolean isClaimLocked = (Boolean) billCharge.get("is_claim_locked");
      if (Boolean.TRUE.equals(includeInClaimCalculation)) {
        if (Boolean.FALSE.equals(isClaimLocked)) {
          BigDecimal insClaimAmt = (BigDecimal) billChgClaimBean.get("insurance_claim_amt");
          BigDecimal copayDedAdj = (BigDecimal) billChgClaimBean.get("copay_ded_adj");
          BigDecimal maxCopayAdj = (BigDecimal) billChgClaimBean.get("max_copay_adj");
          BigDecimal spnLimitAdj = (BigDecimal) billChgClaimBean.get("sponsor_limit_adj");
          BigDecimal copayPercAdj = (BigDecimal) billChgClaimBean.get("copay_perc_adj");
          totalSponsorAmt = totalSponsorAmt.add(
              insClaimAmt.add(copayDedAdj.add(maxCopayAdj.add(spnLimitAdj.add(copayPercAdj)))));
        } else {
          String isClaimAmtIncludesTax = billCharge.get("claim_amount_includes_tax") == null ? "N"
              : (String) billCharge.get("claim_amount_includes_tax");
          String isLimitIncludesTax = billCharge.get("limit_includes_tax") == null ? "N"
              : (String) billCharge.get("limit_includes_tax");
          if (isClaimAmtIncludesTax.equals("Y") && isLimitIncludesTax.equals("Y")) {
            BigDecimal insClaimAmt = ((BigDecimal) billChgClaimBean.get("insurance_claim_amt"))
                .add((BigDecimal) billChgClaimBean.get("tax_amt"));
            totalSponsorAmt = totalSponsorAmt.add(insClaimAmt);
          } else {
            BigDecimal insClaimAmt = ((BigDecimal) billChgClaimBean.get("insurance_claim_amt"));
            totalSponsorAmt = totalSponsorAmt.add(insClaimAmt);
          }
        }
      }
    }

    return totalSponsorAmt;

  }

  /**
   * Gets the item claim amount.
   *
   * @param billChargeClaim the bill charge claim
   * @return the item claim amount
   */
  public BigDecimal getItemClaimAmount(BasicDynaBean billChargeClaim) {

    BigDecimal insClaimAmt = (null != billChargeClaim.get("insurance_claim_amt")) 
        ? (BigDecimal) billChargeClaim.get("insurance_claim_amt") : BigDecimal.ZERO;
    BigDecimal copayDedAdj = (null != billChargeClaim.get("insurance_claim_amt")) 
        ? (BigDecimal) billChargeClaim.get("copay_ded_adj") : BigDecimal.ZERO;
    BigDecimal maxCopayAdj = (null != billChargeClaim.get("max_copay_adj")) 
        ? (BigDecimal) billChargeClaim.get("max_copay_adj") : BigDecimal.ZERO;
    BigDecimal spnLimitAdj = (null != billChargeClaim.get("sponsor_limit_adj")) 
        ? (BigDecimal) billChargeClaim.get("sponsor_limit_adj") : BigDecimal.ZERO;
    BigDecimal copayPercAdj = (null != billChargeClaim.get("copay_perc_adj")) 
        ? (BigDecimal) billChargeClaim.get("copay_perc_adj") : BigDecimal.ZERO;
    return insClaimAmt.add(copayDedAdj.add(maxCopayAdj.add(spnLimitAdj.add(copayPercAdj))));
  }

  /**
   * Checks if is item claimable.
   *
   * @param billCharge the bill charge
   * @return the boolean
   */
  public Boolean isItemClaimable(BasicDynaBean billCharge) {
    Boolean isChargeHeadPayable = (Boolean) billCharge.get("is_charge_head_payable");
    String chargeHead = (String) billCharge.get("charge_head");
    Boolean itemClaimable = isChargeHeadPayable;

    if (chargeHead.equals("INVITE")) {
      Boolean isStoreItemCatgeoryPayable = (Boolean) billCharge.get("store_item_category_payable");
      itemClaimable = isChargeHeadPayable && isStoreItemCatgeoryPayable;
    }

    return itemClaimable;
  }
  
  /**
   * Gets the total sponsor amt without excluded charges.
   *
   * @param billCharges
   *          the bill charges
   * @param billChargeClaims
   *          the bill charge claims
   * @return the total sponsor amt without doctor charges
   */
  public static BigDecimal getTotalSponsorAmtWithoutExcludedChargeGroups(
      List<BasicDynaBean> billCharges, Map<String, BasicDynaBean> billChargeClaims,
      Set<String> excludedChargeGroups) {

    BigDecimal totalSponsorAmt = BigDecimal.ZERO;

    for (BasicDynaBean billCharge : billCharges) {

      // Do not consider doctor charges for the total sponsor amount calculation.
      if (excludedChargeGroups.contains((billCharge.get("charge_group")))) {
        continue;
      }

      String chargeId = (String) billCharge.get("charge_id");
      BasicDynaBean billChgClaimBean = billChargeClaims.get(chargeId);

      Boolean includeInClaimCalculation = (Boolean) billChgClaimBean.get("include_in_claim_calc");
      Boolean isClaimLocked = (Boolean) billCharge.get("is_claim_locked");
      if (includeInClaimCalculation) {
        if (!isClaimLocked) {
          BigDecimal insClaimAmt = (BigDecimal) billChgClaimBean.get("insurance_claim_amt");
          BigDecimal copayDedAdj = (BigDecimal) billChgClaimBean.get("copay_ded_adj");
          BigDecimal maxCopayAdj = (BigDecimal) billChgClaimBean.get("max_copay_adj");
          BigDecimal spnLimitAdj = (BigDecimal) billChgClaimBean.get("sponsor_limit_adj");
          BigDecimal copayPercAdj = (BigDecimal) billChgClaimBean.get("copay_perc_adj");
          totalSponsorAmt = totalSponsorAmt.add(
              insClaimAmt.add(copayDedAdj.add(maxCopayAdj.add(spnLimitAdj.add(copayPercAdj)))));
        } else {
          String isClaimAmtIncludesTax = billCharge.get("claim_amount_includes_tax") == null ? "N"
              : (String) billCharge.get("claim_amount_includes_tax");
          String isLimitIncludesTax = billCharge.get("limit_includes_tax") == null ? "N"
              : (String) billCharge.get("limit_includes_tax");
          if (isClaimAmtIncludesTax.equals("Y") && isLimitIncludesTax.equals("Y")) {
            BigDecimal insClaimAmt = ((BigDecimal) billChgClaimBean.get("insurance_claim_amt"))
                .add((BigDecimal) billChgClaimBean.get("tax_amt"));
            totalSponsorAmt = totalSponsorAmt.add(insClaimAmt);
          } else {
            BigDecimal insClaimAmt = ((BigDecimal) billChgClaimBean.get("insurance_claim_amt"));
            totalSponsorAmt = totalSponsorAmt.add(insClaimAmt);
          }
        }
      }
    }

    return totalSponsorAmt;

  }

  /**
   * Checks if is pre auth amount exceeded beyond the limit.
   *
   * @param billCharges
   *          the bill charges
   * @param billChargeClaims
   *          the bill charge claims
   * @param visitInsBean
   *          the visit ins bean
   * @return true, if pre auth amount has exceeded
   */
  public static boolean isPreAuthAmountExceeded(List<BasicDynaBean> billCharges,
      Map<String, BasicDynaBean> billChargeClaims, BasicDynaBean visitInsBean) {

    if (!"Y".contentEquals((String) visitInsBean.get("enable_pre_authorized_limit"))) {
      // returning false so that pre auth does not happen automatically.
      return false;
    }

    Set<String> excludedGroups = getPreAuthExcludedChargeGroups(visitInsBean);

    BigDecimal preAuthorizedAmount = (BigDecimal) visitInsBean.get("op_pre_authorized_amount");
    if (null == preAuthorizedAmount) {
      return false;
    }
    BigDecimal totalSponsorAmount = getTotalSponsorAmtWithoutExcludedChargeGroups(billCharges,
        billChargeClaims, excludedGroups);
    return (preAuthorizedAmount.compareTo(totalSponsorAmount) < 0);

  }

  /**
   * Gets a map of charge_id and if pre-auth is required(boolean) for that charge.
   *
   * @param billCharges
   *          the bill charges
   * @param preAuthAmountExceeded
   *          the boolean (ideally the result of isPreAuthAmountExceeded method)
   * @param visitInsBean
   *          the visit ins bean
   * @return the map of charge_id and its pre_auth required flag.
   */
  public static Map<String, Boolean> getPreAuthRequiredMap(List<BasicDynaBean> billCharges,
      Map<String, BasicDynaBean> claims, boolean preAuthAmountExceeded,
      BasicDynaBean visitInsBean) {
    Map<String, Boolean> preAuthRequiredMap = new HashMap<>();
    Set<String> excludedGroups = getPreAuthExcludedChargeGroups(visitInsBean);
    for (BasicDynaBean billCharge : billCharges) {
      boolean isPreAuthRequired = false;
      String chargeId = (String) billCharge.get("charge_id");
      BasicDynaBean claim = claims.get(chargeId);
      isPreAuthRequired = ((boolean) claim.get("include_in_claim_calc")) && preAuthAmountExceeded
          && !excludedGroups.contains(billCharge.get("charge_group"));
      preAuthRequiredMap.put(chargeId, isPreAuthRequired);
    }
    return preAuthRequiredMap;
  }

  /**
   * Gets the pre auth excluded charge groups.
   *
   * @param insurancePlanBean
   *          the insurance plan bean
   * @return the pre auth excluded charge groups
   */
  public static Set<String> getPreAuthExcludedChargeGroups(BasicDynaBean insurancePlanBean) {
    Set<String> excludedGroups = new HashSet<>();
    String excludedGroupsStr = (String) insurancePlanBean.get("excluded_charge_groups");
    if (null != excludedGroupsStr) {
      excludedGroups.addAll(Arrays.asList(excludedGroupsStr.split(",")));
    }
    return excludedGroups;
  }

}
