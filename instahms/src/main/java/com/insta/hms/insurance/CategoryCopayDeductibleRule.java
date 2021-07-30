package com.insta.hms.insurance;

import org.apache.commons.beanutils.BasicDynaBean;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * The Class CategoryCopayDeductibleRule.
 */
public class CategoryCopayDeductibleRule implements CategoryInsuranceRule {

  /** The rt. */
  private RuleAdjustmentType rt = RuleAdjustmentType.CATEGORY_DEDUCTIBLE_ADJ;

  /**
   * Apply Rule.
   *
   * @param itemCategoryId          the item category id
   * @param billCharges             the bill charges
   * @param billChargeClaims        the bill charge claims
   * @param visitInsPlanCategoryMap the visit ins plan category map
   * @param adjStatusMap            the adj status map
   * @return true, if successful
   */
  @Override
  public boolean apply(int itemCategoryId, List<BasicDynaBean> billCharges,
      Map<String, BasicDynaBean> billChargeClaims,
      Map<Integer, BasicDynaBean> visitInsPlanCategoryMap, Map adjStatusMap) {

    BasicDynaBean visitInsCatBean = visitInsPlanCategoryMap.get(itemCategoryId);

    BigDecimal categoryCopayDeductible = (BigDecimal) visitInsCatBean
        .get("patient_amount_per_category");

    BigDecimal totalCategoryCopay = new AdvanceInsuranceHelper().getTotalCopay(billCharges,
        billChargeClaims);

    BigDecimal copayAdjustment = BigDecimal.ZERO;

    if (totalCategoryCopay.compareTo(categoryCopayDeductible) < 0) {
      copayAdjustment = categoryCopayDeductible.subtract(totalCategoryCopay);
    }

    if (copayAdjustment.compareTo(BigDecimal.ZERO) > 0) {

      for (BasicDynaBean billCharge : billCharges) {

        Boolean isClaimLocked = (Boolean) billCharge.get("is_claim_locked");
        BigDecimal copayDedAdj = BigDecimal.ZERO;

        String chargeId = (String) billCharge.get("charge_id");
        BasicDynaBean billhgClaimBean = billChargeClaims.get(chargeId);

        Boolean includeInClaimCalc = (Boolean) billhgClaimBean.get("include_in_claim_calc");

        if (!isClaimLocked && includeInClaimCalc) {

          BigDecimal itemClaimAmt = new AdvanceInsuranceHelper()
              .getItemClaimAmount(billhgClaimBean);

          copayDedAdj = copayAdjustment.min(itemClaimAmt).negate();
          copayAdjustment = copayAdjustment.add(copayDedAdj);

          billhgClaimBean.set("copay_ded_adj", copayDedAdj);

          BigDecimal billChgCopayDedAdj = ((BigDecimal) billCharge.get("copay_ded_adj"))
              .add((BigDecimal) billhgClaimBean.get("copay_ded_adj"));
          billCharge.set("copay_ded_adj", billChgCopayDedAdj);

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
