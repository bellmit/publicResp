package com.insta.hms.insurance;

import org.apache.commons.beanutils.BasicDynaBean;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * The Class VisitCopayDeductibleRule.
 */
public class VisitCopayDeductibleRule implements VisitInsuranceRule {

  /** The rt. */
  private RuleAdjustmentType rt = RuleAdjustmentType.VISIT_DEDUCTIBLE_ADJ;

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.insurance.VisitInsuranceRule#apply(java.util.List, java.util.List,
   * java.util.List) billCharges - all billCharges that are to be included in the calculation
   * billChargeClaims - all billChargeClaims for the billCharges
   */
  @Override
  public boolean apply(List<BasicDynaBean> billCharges, Map<String, BasicDynaBean> billChargeClaims,
      BasicDynaBean visitInsurancePlan, Map adjStatusMap) {

    BigDecimal totCopay = new AdvanceInsuranceHelper().getTotalCopay(billCharges, billChargeClaims);

    BigDecimal visitCopayDeductible = (BigDecimal) visitInsurancePlan.get("visit_deductible");
    BigDecimal copayAdjustment = BigDecimal.ZERO;

    if (null != visitCopayDeductible && totCopay.compareTo(visitCopayDeductible) < 0) {
      copayAdjustment = visitCopayDeductible.subtract(totCopay);
    }

    if (copayAdjustment.compareTo(BigDecimal.ZERO) > 0) {

      for (BasicDynaBean charge : billCharges) {
        Boolean isClaimLocked = (Boolean) charge.get("is_claim_locked");
        BigDecimal copayDedAdj = BigDecimal.ZERO;

        String chargeId = (String) charge.get("charge_id");
        BasicDynaBean billhgClaimBean = billChargeClaims.get(chargeId);

        Boolean includeInClaimCalc = (Boolean) billhgClaimBean.get("include_in_claim_calc");
        if (!isClaimLocked && includeInClaimCalc) {

          BigDecimal itemClaimAmt = new AdvanceInsuranceHelper()
              .getItemClaimAmount(billhgClaimBean);

          copayDedAdj = copayAdjustment.min(itemClaimAmt).negate();
          copayAdjustment = copayAdjustment.add(copayDedAdj);

          billhgClaimBean.set("copay_ded_adj",
              ((BigDecimal) billhgClaimBean.get("copay_ded_adj")).add(copayDedAdj));

          BigDecimal billChgCopayDedAdj = ((BigDecimal) charge.get("copay_ded_adj"))
              .add((BigDecimal) billhgClaimBean.get("copay_ded_adj"));
          charge.set("copay_ded_adj", billChgCopayDedAdj);

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
