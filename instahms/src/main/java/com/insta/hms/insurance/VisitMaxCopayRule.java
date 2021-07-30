package com.insta.hms.insurance;

import org.apache.commons.beanutils.BasicDynaBean;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The Class VisitMaxCopayRule.
 */
public class VisitMaxCopayRule implements VisitInsuranceRule {

  /** The rt. */
  private RuleAdjustmentType rt = RuleAdjustmentType.VISIT_MAX_COPAY_ADJ;

  /**
   * Apply.
   *
   * @param billCharges        the bill charges
   * @param billChargeClaims   the bill charge claims
   * @param visitInsurancePlan the visit insurance plan
   * @param adjStatusMap       the adj status map
   * @return true, if successful
   */
  public boolean apply(List<BasicDynaBean> billCharges, Map<String, BasicDynaBean> billChargeClaims,
      BasicDynaBean visitInsurancePlan, Map adjStatusMap) {

    BigDecimal visitMaxCopay = visitInsurancePlan.get("visit_max_copay_percentage") == null
        ? BigDecimal.ZERO
        : (BigDecimal) visitInsurancePlan.get("visit_max_copay_percentage");
    BigDecimal totalCopay = new AdvanceInsuranceHelper().getTotalCopay(billCharges,
        billChargeClaims);

    List<BasicDynaBean> billChargesInReverse = billCharges;
    Collections.sort(billChargesInReverse, Collections.reverseOrder(new BillChargeComparator()));

    BigDecimal copayAdjustment = BigDecimal.ZERO;

    if (visitMaxCopay.compareTo(BigDecimal.ZERO) > 0 && totalCopay.compareTo(visitMaxCopay) > 0) {
      copayAdjustment = totalCopay.subtract(visitMaxCopay);
    }

    if (copayAdjustment.compareTo(BigDecimal.ZERO) > 0) {
      for (BasicDynaBean billCharge : billChargesInReverse) {
        Boolean isClaimLocked = (Boolean) billCharge.get("is_claim_locked");
        String chargeId = (String) billCharge.get("charge_id");
        BasicDynaBean billhgClaimBean = billChargeClaims.get(chargeId);
        Boolean includeInClaimCalc = (Boolean) billhgClaimBean.get("include_in_claim_calc");

        if (!isClaimLocked && includeInClaimCalc) {

          BigDecimal itemClaimAmt = new AdvanceInsuranceHelper()
              .getItemClaimAmount(billhgClaimBean);
          BigDecimal itemAmt = ((BigDecimal) billCharge.get("amount"))
              .subtract((BigDecimal) billCharge.get("insurance_claim_amount"));

          BigDecimal itemCopay = itemAmt.subtract(itemClaimAmt);

          BigDecimal maxCopayAdj = copayAdjustment.min(itemCopay);
          billhgClaimBean.set("max_copay_adj",
              ((BigDecimal) billhgClaimBean.get("max_copay_adj")).add(maxCopayAdj));

          BigDecimal billChgMaxCopayAdj = ((BigDecimal) billCharge.get("max_copay_adj"))
              .add((BigDecimal) billhgClaimBean.get("max_copay_adj"));
          billCharge.set("max_copay_adj", billChgMaxCopayAdj);

          copayAdjustment = copayAdjustment.subtract(maxCopayAdj);

          if (copayAdjustment.compareTo(BigDecimal.ZERO) == 0) {
            break;
          }
        }
      }

      if (isAdjustmentIncomplete(copayAdjustment)) {
        setAdjustmentStatus(adjStatusMap);
      }
    }

    return true;
  }

  /**
   * Checks if is adjustment incomplete.
   *
   * @param copayAdjustment the copay adjustment
   * @return true, if is adjustment incomplete
   */
  private boolean isAdjustmentIncomplete(BigDecimal copayAdjustment) {

    return copayAdjustment.compareTo(BigDecimal.ZERO) > 0;

  }

  /**
   * Sets the adjustment status.
   *
   * @param adjStatusMap the new adjustment status
   */
  private void setAdjustmentStatus(Map adjStatusMap) {
    int code = null != adjStatusMap.get("adjStatus") ? (Integer) adjStatusMap.get("adjStatus") : 0;
    code = code + this.rt.getCode();
    adjStatusMap.put("adjStatus", code);
  }

}
