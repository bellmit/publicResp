package com.insta.hms.insurance;

import org.apache.commons.beanutils.BasicDynaBean;

import java.util.Map;

/**
 * The Interface InsuranceRule.
 */
public interface InsuranceRule {

  /**
   * Apply.
   *
   * @param billCharge           the bill charge
   * @param billChargeClaimBean  the bill charge claim bean
   * @param insuranceCategoryMap the insurance category map
   * @return true, if successful
   */
  /*
   * Inputs: 1) BillCharge bean: rate, quantity, amount (post discount), discount,
   * insurance_claim_amount
   *
   * 2) visitInsurance: insurance details for the item for the visit such as - insurance payable
   * flag - item deductible - isCopayPostDiscount - copay % - copay limit - sponsor Limit
   *
   * The method will calculate the claim amount for the item after the rule is applied.
   *
   * Returns: BillCharge bean updated with the claim amount after applying the rule.
   *
   */
  public boolean apply(BasicDynaBean billCharge, BasicDynaBean billChargeClaimBean,
      Map<Integer, BasicDynaBean> insuranceCategoryMap);

}
