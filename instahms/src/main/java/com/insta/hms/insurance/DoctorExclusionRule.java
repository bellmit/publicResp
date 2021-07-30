package com.insta.hms.insurance;

import org.apache.commons.beanutils.BasicDynaBean;

import java.math.BigDecimal;
import java.util.Map;

/**
 * The Class DoctorExclusionRule.
 */
public class DoctorExclusionRule implements InsuranceRule {

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.insurance.InsuranceRule#apply(org.apache.commons.beanutils.BasicDynaBean,
   * org.apache.commons.beanutils.BasicDynaBean, java.util.Map)
   */
  @Override
  public boolean apply(BasicDynaBean billCharge, BasicDynaBean billChargeClaimBean,
      Map<Integer, BasicDynaBean> insuranceCategoryMap) {
    if (billCharge.getMap().containsKey("item_excluded_from_doctor")
        && null != billCharge.get("item_excluded_from_doctor")) {

      boolean isExcluded = (boolean) billCharge.get("item_excluded_from_doctor");
      if (!isExcluded) {
        BigDecimal amt = (BigDecimal) billCharge.get("amount");
        BigDecimal claimAmt = (BigDecimal) billCharge.get("insurance_claim_amount");
        BigDecimal chargeAmt = amt.subtract(claimAmt);
        billChargeClaimBean.set("insurance_claim_amt", chargeAmt);
      } else {
        billChargeClaimBean.set("include_in_claim_calc", false);
        billChargeClaimBean.set("insurance_claim_amt", BigDecimal.ZERO);
      }

      return !isExcluded;
    }
    return true;
  }

}
