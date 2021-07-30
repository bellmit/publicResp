package com.insta.hms.insurance;

import org.apache.commons.beanutils.BasicDynaBean;

import java.math.BigDecimal;
import java.util.Map;

/**
 * The Class ChargeHeadRule.
 */
public class ChargeHeadRule implements InsuranceRule {

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.insurance.InsuranceRule#apply(org.apache.commons.beanutils.BasicDynaBean,
   * org.apache.commons.beanutils.BasicDynaBean, java.util.Map)
   */
  @Override
  public boolean apply(BasicDynaBean billCharge, BasicDynaBean billChargeClaimBean,
      Map<Integer, BasicDynaBean> insuranceCategoryMap) {

    Boolean isChargeHeadPayable = (Boolean) billCharge.get("is_charge_head_payable");
    String chargeHead = (billCharge.get("charge_head") != null) 
        ? billCharge.get("charge_head").toString() : "";
    BigDecimal amt = (BigDecimal) billCharge.get("amount");
    BigDecimal claimAmt = (BigDecimal) billCharge.get("insurance_claim_amount");
    BigDecimal chargeAmt = amt.subtract(claimAmt);

    Boolean itemClaimable = isChargeHeadPayable;

    if ("INVITE".equals(chargeHead)) {
      if (billCharge.get("package_id") != null) {
        itemClaimable = true;
      } else {
        Boolean isStoreItemCatgeoryPayable = (Boolean) billCharge
            .get("store_item_category_payable");
        itemClaimable = isStoreItemCatgeoryPayable;
      }
    }

    if ("PHMED".equals(chargeHead) || "PHCMED".equals(chargeHead)) {
      itemClaimable = true;
    }

    billChargeClaimBean.set("insurance_claim_amt", 
        Boolean.TRUE.equals(itemClaimable) ? chargeAmt : BigDecimal.ZERO);

    return itemClaimable;
  }

}
