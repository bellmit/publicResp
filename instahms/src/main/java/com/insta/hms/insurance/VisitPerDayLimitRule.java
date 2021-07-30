package com.insta.hms.insurance;

import com.bob.hms.common.DateUtil;

import org.apache.commons.beanutils.BasicDynaBean;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * The Class VisitPerDayLimitRule.
 */
public class VisitPerDayLimitRule implements VisitInsuranceRule {

  /** The rt. */
  private RuleAdjustmentType rt = RuleAdjustmentType.VISIT_PER_DAY_LIMIT_ADJ;

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

    BigDecimal visitPerDayLimit = null == visitInsurancePlan.get("visit_per_day_limit")
        ? BigDecimal.ZERO
        : (BigDecimal) visitInsurancePlan.get("visit_per_day_limit");

    Date regDate = visitInsurancePlan.get("reg_date") != null
        ? (Date) visitInsurancePlan.get("reg_date")
        : DateUtil.getCurrentDate();

    Date dischargeDate = visitInsurancePlan.get("discharge_date") != null
        ? (Date) visitInsurancePlan.get("discharge_date")
        : DateUtil.getCurrentDate();

    Timestamp admissionDateTime = new Timestamp(regDate.getTime());
    Timestamp dischargeDateTime = new Timestamp(dischargeDate.getTime());

    int[] noOfDaysHours = DateUtil.getDaysHours(admissionDateTime, dischargeDateTime, true);
    int noOfDays = noOfDaysHours[0] + 1;

    BigDecimal visitSponsorLimit = visitPerDayLimit.multiply(new BigDecimal(noOfDays));

    List<BasicDynaBean> billChargesInReverse = billCharges;
    Collections.sort(billChargesInReverse, Collections.reverseOrder(new BillChargeComparator()));

    if (visitSponsorLimit.compareTo(BigDecimal.ZERO) > 0) {
      BigDecimal totalSponsorAmt = new AdvanceInsuranceHelper().getTotalSponsorAmt(billCharges,
          billChargeClaims);

      BigDecimal sponsorAdj = BigDecimal.ZERO;

      if (totalSponsorAmt.compareTo(visitSponsorLimit) > 0) {
        sponsorAdj = totalSponsorAmt.subtract(visitSponsorLimit);
      }

      if (sponsorAdj.compareTo(BigDecimal.ZERO) > 0) {
        for (BasicDynaBean billCharge : billChargesInReverse) {
          Boolean isClaimLocked = (Boolean) billCharge.get("is_claim_locked");
          String chargeId = (String) billCharge.get("charge_id");
          BasicDynaBean billChgClaimBean = billChargeClaims.get(chargeId);
          Boolean includeInClaimCalc = (Boolean) billChgClaimBean.get("include_in_claim_calc");

          if (!isClaimLocked && includeInClaimCalc) {

            BigDecimal itemClaimAmt = new AdvanceInsuranceHelper()
                .getItemClaimAmount(billChgClaimBean);

            BigDecimal sponsorLimitAdj = itemClaimAmt.min(sponsorAdj).negate();
            billChgClaimBean.set("sponsor_limit_adj",
                sponsorLimitAdj.add((BigDecimal) billChgClaimBean.get("sponsor_limit_adj")));
            sponsorAdj = sponsorAdj.add(sponsorLimitAdj);
            if (sponsorAdj.compareTo(BigDecimal.ZERO) == 0) {
              break;
            }
          }
        }
        if (isAdjustmentIncomplete(sponsorAdj)) {
          setAdjustmentStatus(adjStatusMap);
        }

      }

      return false;
    }

    if (visitPerDayLimit.compareTo(BigDecimal.ZERO) > 0) {
      return false;
    }

    return true;
  }

  /**
   * Checks if is adjustment incomplete.
   *
   * @param sponsorAdj the sponsor adj
   * @return true, if is adjustment incomplete
   */
  private boolean isAdjustmentIncomplete(BigDecimal sponsorAdj) {

    return sponsorAdj.compareTo(BigDecimal.ZERO) > 0;

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
