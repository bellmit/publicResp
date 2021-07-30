package com.insta.hms.insurance;

import com.insta.hms.common.ConversionUtils;

import org.apache.commons.beanutils.BasicDynaBean;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class ItemInsuranceCalculator.
 */
public class ItemInsuranceCalculator {

  /** The insurance rules. */
  private List<InsuranceRule> insuranceRules;

  /** The doctor exclusion rule. */
  private DoctorExclusionRule doctorExclusionRule;

  /** The item copay deductible rule. */
  private ItemCopayDeductibleRule itemCopayDeductibleRule;

  /**
   * Instantiates a new item insurance calculator.
   */
  public ItemInsuranceCalculator() {
    insuranceRules = new ArrayList<InsuranceRule>();
    insuranceRules.add(new ChargeHeadRule());
    insuranceRules.add(new ItemCategoryRule());
    insuranceRules.add(new PlanCategoryPayableRule());
    doctorExclusionRule = new DoctorExclusionRule();
    itemCopayDeductibleRule = new ItemCopayDeductibleRule();
    // insuranceRules.add(new ItemCopayPercentRule());
  }

  /**
   * Calculate.
   * <p>
   * Inputs: 1) BillCharge bean: rate, quantity, amount (post discount), discount,
   * insurance_claim_amount
   *
   * 2) visitInsurance: insurance details for the item for the visit such as - insurance payable
   * flag - item deductible - isCopayPostDiscount - copay % - copay limit - sponsor Limit
   *
   * The method will calculate the claim amount for the item after the insurance rules are applied.
   *
   * Returns: updated BillCharge bean which has the claim amount set after insurance rules are
   * applied.
   * </p>
   * 
   * @param billCharge          the bill charge
   * @param billChargeClaimList the bill charge claim list
   * @param visitInsuranceList  the visit insurance list
   */
  public void calculate(BasicDynaBean billCharge, List<BasicDynaBean> billChargeClaimList,
      List<BasicDynaBean> visitInsuranceList) {

    int index = 0;
    Map<Integer, List<BasicDynaBean>> visitInsuranceMap = ConversionUtils
        .listBeanToMapListBean(visitInsuranceList, "plan_id");

    for (int planId : visitInsuranceMap.keySet()) {
      BasicDynaBean billChgClaimBean = billChargeClaimList.get(index);

      Map<Integer, BasicDynaBean> insuranceCategoryMap = ConversionUtils
          .listBeanToMapBean(visitInsuranceMap.get(planId), "insurance_category_id");

      for (InsuranceRule rule : insuranceRules) {
        // Stop if the rule returns false
        if (!rule.apply(billCharge, billChgClaimBean, insuranceCategoryMap)) {
          break;
        }
      }

      doctorExclusionRule.apply(billCharge, billChgClaimBean, insuranceCategoryMap);
      if (BigDecimal.ZERO.compareTo((BigDecimal) billChgClaimBean.get("insurance_claim_amt")) < 0) {
        itemCopayDeductibleRule.apply(billCharge, billChgClaimBean, insuranceCategoryMap);
      }

      index++;
    }
  }

}
