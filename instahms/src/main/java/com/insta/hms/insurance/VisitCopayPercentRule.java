package com.insta.hms.insurance;

import com.insta.hms.common.ConversionUtils;

import org.apache.commons.beanutils.BasicDynaBean;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * The Class VisitCopayPercentRule.
 */
public class VisitCopayPercentRule implements VisitInsuranceRule {

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

    BigDecimal visitCopayPerc = null == visitInsurancePlan.get("visit_copay_percentage")
        ? BigDecimal.ZERO
        : (BigDecimal) visitInsurancePlan.get("visit_copay_percentage");

    for (BasicDynaBean charge : billCharges) {
      Boolean isClaimLocked = (Boolean) charge.get("is_claim_locked");
      String chargeId = (String) charge.get("charge_id");
      BasicDynaBean billChargeClaimBean = billChargeClaims.get(chargeId);
      Boolean includeInClaimCalc = (Boolean) billChargeClaimBean.get("include_in_claim_calc");
      if (!isClaimLocked && includeInClaimCalc) {

        BigDecimal itemClaimAmt = new AdvanceInsuranceHelper()
            .getItemClaimAmount(billChargeClaimBean);

        BigDecimal itemCopay = ConversionUtils.setScale(visitCopayPerc.multiply(itemClaimAmt)
            .divide(new BigDecimal("100"), BigDecimal.ROUND_HALF_UP));

        itemCopay = itemCopay.negate();

        billChargeClaimBean.set("copay_perc_adj",
            ((BigDecimal) billChargeClaimBean.get("copay_perc_adj")).add(itemCopay));

        charge.set("copay_perc_adj", ((BigDecimal) billChargeClaimBean.get("copay_perc_adj"))
            .add((BigDecimal) billChargeClaimBean.get("copay_perc_adj")));
      }
    }

    return true;
  }

}
