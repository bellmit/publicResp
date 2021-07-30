package com.insta.hms.insurance;

import com.insta.hms.common.ConversionUtils;

import org.apache.commons.beanutils.BasicDynaBean;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * The Class CategoryCopayPercentRule.
 */
public class CategoryCopayPercentRule implements CategoryInsuranceRule {

  /**
   * Apply.
   *
   * @param itemCategoryId          the item category id
   * @param billCharges             the bill charges
   * @param billChargeClaims        the bill charge claims
   * @param visitInsPlanCategoryMap the visit ins plan category map
   * @param adjStatusMap            the adj status map
   * @return true, if successful
   */
  public boolean apply(int itemCategoryId, List<BasicDynaBean> billCharges,
      Map<String, BasicDynaBean> billChargeClaims,
      Map<Integer, BasicDynaBean> visitInsPlanCategoryMap, Map adjStatusMap) {

    BasicDynaBean visitInsCatBean = visitInsPlanCategoryMap.get(itemCategoryId);

    BigDecimal categoryCopayPercent = null != visitInsCatBean.get("patient_percent")
        ? (BigDecimal) visitInsCatBean.get("patient_percent")
        : BigDecimal.ZERO;

    int priority = (Integer) visitInsCatBean.get("priority");
    Boolean isCopayApplicableOnPostDiscountedAmt = ((String) visitInsCatBean
        .get("is_copay_pc_on_post_discnt_amt")).equals("Y");

    for (BasicDynaBean charge : billCharges) {
      Boolean isClaimLocked = (Boolean) charge.get("is_claim_locked");
      String chargeId = (String) charge.get("charge_id");
      BasicDynaBean billChargeClaimBean = billChargeClaims.get(chargeId);

      Boolean includeInClaimCalc = (Boolean) billChargeClaimBean.get("include_in_claim_calc");

      if (Boolean.FALSE.equals(isClaimLocked) && Boolean.TRUE.equals(includeInClaimCalc)) {

        BigDecimal itemClaimAmt = new AdvanceInsuranceHelper()
            .getItemClaimAmount(billChargeClaimBean);

        BigDecimal discount = priority == 1 ? (BigDecimal) charge.get("discount") : BigDecimal.ZERO;

        Boolean itemClaimable = new AdvanceInsuranceHelper().isItemClaimable(charge);

        if (Boolean.FALSE.equals(isCopayApplicableOnPostDiscountedAmt) 
            && Boolean.TRUE.equals(itemClaimable) && itemClaimAmt.compareTo(BigDecimal.ZERO) != 0 
            && categoryCopayPercent.compareTo(new BigDecimal(100)) != 0) {
          itemClaimAmt = itemClaimAmt.add(discount);
        }

        itemClaimAmt = ConversionUtils.setScale(itemClaimAmt);

        BigDecimal itemCopay = ConversionUtils.setScale(categoryCopayPercent.multiply(itemClaimAmt)
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
