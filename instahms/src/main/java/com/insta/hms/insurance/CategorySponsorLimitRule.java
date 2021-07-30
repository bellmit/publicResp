package com.insta.hms.insurance;

import org.apache.commons.beanutils.BasicDynaBean;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The Class CategorySponsorLimitRule.
 */
public class CategorySponsorLimitRule implements CategoryInsuranceRule {

  /** The rt. */
  private RuleAdjustmentType rt = RuleAdjustmentType.CATEGORY_SPONSOR_LIMIT_ADJ;

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.insurance.CategoryInsuranceRule#apply(int, java.util.List, java.util.Map,
   * java.util.Map, java.util.Map)
   */
  @Override
  public boolean apply(int itemCategoryId, List<BasicDynaBean> billCharges,
      Map<String, BasicDynaBean> billChargeClaims,
      Map<Integer, BasicDynaBean> visitInsPlanCategoryMap, Map adjStatusMap) {

    BasicDynaBean visitInsCatBean = visitInsPlanCategoryMap.get(itemCategoryId);
    List<BasicDynaBean> billChargesInReverse = billCharges;
    Collections.sort(billChargesInReverse, Collections.reverseOrder(new BillChargeComparator()));

    BigDecimal categorySponsorLimit = null != visitInsCatBean.get("per_treatment_limit")
        ? (BigDecimal) visitInsCatBean.get("per_treatment_limit")
        : BigDecimal.ZERO;

    BigDecimal totalSponsorAmt = new AdvanceInsuranceHelper().getTotalSponsorAmt(billCharges,
        billChargeClaims);

    BigDecimal sponsorAdjustment = BigDecimal.ZERO;

    if (categorySponsorLimit.compareTo(BigDecimal.ZERO) > 0
        && totalSponsorAmt.compareTo(categorySponsorLimit) > 0) {
      sponsorAdjustment = totalSponsorAmt.subtract(categorySponsorLimit);
    }

    String limitType = (String) visitInsCatBean.get("limit_type");

    if (limitType.equals("R") && sponsorAdjustment.compareTo(BigDecimal.ZERO) > 0) {
      for (BasicDynaBean billCharge : billChargesInReverse) {
        Boolean isClaimLocked = (Boolean) billCharge.get("is_claim_locked");
        String chargeId = (String) billCharge.get("charge_id");
        BasicDynaBean billChgClaimBean = billChargeClaims.get(chargeId);
        Boolean includeInClaimCalc = (Boolean) billChgClaimBean.get("include_in_claim_calc");
        if (!isClaimLocked && includeInClaimCalc) {
          BigDecimal itemClaimAmt = new AdvanceInsuranceHelper()
              .getItemClaimAmount(billChgClaimBean);
          BigDecimal sponsorLimitAdj = itemClaimAmt.min(sponsorAdjustment).negate();
          billChgClaimBean.set("sponsor_limit_adj", sponsorLimitAdj);
          sponsorAdjustment = sponsorAdjustment.add(sponsorLimitAdj);
          if (sponsorAdjustment.compareTo(BigDecimal.ZERO) == 0) {
            break;
          }

        }
      }
    }

    /*
     * for(BasicDynaBean billCharge : billChargesInReverse){ Boolean isClaimLocked =
     * (Boolean)billCharge.get("is_claim_locked"); if (!isClaimLocked) { String chargeId =
     * (String)billCharge.get("charge_id"); BasicDynaBean billChgClaimBean =
     * billChargeClaims.get(chargeId); BigDecimal itemClaimAmt = new
     * AdvanceInsuranceHelper().getItemClaimAmount(billChgClaimBean); BigDecimal sponsorLimitAdj =
     * itemClaimAmt.min(sponsorAdjustment).negate(); billChgClaimBean.set("sponsor_limit_adj",
     * sponsorLimitAdj); sponsorAdjustment = sponsorAdjustment.add(sponsorLimitAdj);
     * if(sponsorAdjustment.compareTo(BigDecimal.ZERO) == 0) break;
     * 
     * } }
     */

    if (isAdjustmentIncomplete(sponsorAdjustment)) {
      setAdjustmentStatus(adjStatusMap);
    }

    return true;
  }

  /**
   * Checks if is adjustment incomplete.
   *
   * @param sponsorAdjustment the sponsor adjustment
   * @return true, if is adjustment incomplete
   */
  private boolean isAdjustmentIncomplete(BigDecimal sponsorAdjustment) {

    return sponsorAdjustment.compareTo(BigDecimal.ZERO) > 0;

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
