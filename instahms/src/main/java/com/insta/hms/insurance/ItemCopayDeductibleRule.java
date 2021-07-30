package com.insta.hms.insurance;

import org.apache.commons.beanutils.BasicDynaBean;

import java.math.BigDecimal;
import java.util.Map;

/**
 * The Class ItemCopayDeductibleRule.
 */
public class ItemCopayDeductibleRule implements InsuranceRule {

  /**
   * Apply.
   *
   * @param billCharge           the bill charge
   * @param billChargeClaimBean  the bill charge claim bean
   * @param insuranceCategoryMap the insurance category map
   * @return true, if successful
   */
  @Override
  public boolean apply(BasicDynaBean billCharge, BasicDynaBean billChargeClaimBean,
      Map<Integer, BasicDynaBean> insuranceCategoryMap) {

    int catgeoryId = (Integer) billChargeClaimBean.get("insurance_category_id");
    BasicDynaBean insuranceCategoryBean = insuranceCategoryMap.get(catgeoryId);

    BigDecimal deductible = (BigDecimal) insuranceCategoryBean.get("patient_amount");
    BigDecimal amt = (BigDecimal) billCharge.get("amount");
    BigDecimal claimAmt = (BigDecimal) billCharge.get("insurance_claim_amount");
    BigDecimal chargeAmt = amt.subtract(claimAmt);

    BigDecimal copay = chargeAmt.min(deductible);

    billChargeClaimBean.set("insurance_claim_amt", chargeAmt.subtract(copay));
    return true;
  }

}
