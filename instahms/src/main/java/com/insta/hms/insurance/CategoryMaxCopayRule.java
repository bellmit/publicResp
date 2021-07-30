package com.insta.hms.insurance;

import org.apache.commons.beanutils.BasicDynaBean;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The Class CategoryMaxCopayRule.
 */
public class CategoryMaxCopayRule implements CategoryInsuranceRule {

  /** The rt. */
  private RuleAdjustmentType rt = RuleAdjustmentType.CATEGORY_MAX_COPAY_ADJ;

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.insurance.CategoryInsuranceRule#apply (int, java.util.List, java.util.Map,
   * java.util.Map, java.util.Map)
   */
  @Override
  public boolean apply(int itemCategoryId, List<BasicDynaBean> billCharges,
      Map<String, BasicDynaBean> billChargeClaims,
      Map<Integer, BasicDynaBean> visitInsPlanCategoryMap, Map adjStatusMap) {

    List<BasicDynaBean> billChargesInReverse = billCharges;

    Collections.sort(billChargesInReverse, Collections.reverseOrder(new BillChargeComparator()));

    BasicDynaBean visitInsCatBean = visitInsPlanCategoryMap.get(itemCategoryId);

    BigDecimal categoryMaxCopay = null != visitInsCatBean.get("patient_amount_cap")
        ? (BigDecimal) visitInsCatBean.get("patient_amount_cap")
        : BigDecimal.ZERO;

    BigDecimal totalCategoryCopay = new AdvanceInsuranceHelper().getTotalCopay(billCharges,
        billChargeClaims);

    BigDecimal copayAdjustment = BigDecimal.ZERO;

    if (categoryMaxCopay.compareTo(BigDecimal.ZERO) != 0
        && totalCategoryCopay.compareTo(categoryMaxCopay) > 0) {
      copayAdjustment = totalCategoryCopay.subtract(categoryMaxCopay);
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
          billhgClaimBean.set("max_copay_adj", maxCopayAdj);

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
