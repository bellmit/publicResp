package com.insta.hms.insurance;

import com.insta.hms.common.ConversionUtils;

import org.apache.commons.beanutils.BasicDynaBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class CategoryInsuranceCalculator.
 */
public class CategoryInsuranceCalculator {

  /** The insurance rules. */
  private List<CategoryInsuranceRule> insuranceRules;

  /**
   * Instantiates a new category insurance calculator.
   */
  public CategoryInsuranceCalculator() {
    insuranceRules = new ArrayList<CategoryInsuranceRule>();
    insuranceRules.add(new CategoryCopayDeductibleRule());
    insuranceRules.add(new CategoryCopayPercentRule());
    insuranceRules.add(new CategoryMaxCopayRule());
    insuranceRules.add(new CategorySponsorLimitRule());
  }

  /**
   * Calculate.
   *
   * @param billCharges         the bill charges
   * @param billChargeClaims    the bill charge claims
   * @param visitInsurancePlans the visit insurance plans
   * @return the map
   */
  public Map<Integer, Integer> calculate(List<BasicDynaBean> billCharges,
      List<BasicDynaBean> billChargeClaims, List<BasicDynaBean> visitInsurancePlans) {

    Map<Integer, Integer> ruleAdjustmentMap = new HashMap<Integer, Integer>();

    Map<Integer, List<BasicDynaBean>> billChargeClaimCategoryMap = ConversionUtils
        .listBeanToMapListBean(billChargeClaims, "insurance_category_id");
    Map<String, BasicDynaBean> billChargeMap = ConversionUtils.listBeanToMapBean(billCharges,
        "charge_id");

    for (Integer key : billChargeClaimCategoryMap.keySet()) {
      List<BasicDynaBean> categoryBillCharges = new ArrayList<>();
      List<BasicDynaBean> categoryBillChargeClaims = new ArrayList<>();

      for (BasicDynaBean chargeClaim : billChargeClaimCategoryMap.get(key)) {
        String chargeId = (String) chargeClaim.get("charge_id");
        categoryBillCharges.add(billChargeMap.get(chargeId));
        categoryBillChargeClaims.add(chargeClaim);
      }

      Map<String, List<BasicDynaBean>> billChargeClaimMap = ConversionUtils
          .listBeanToMapListBean(categoryBillChargeClaims, "charge_id");

      Map<String, Integer> adjStatusMap = new HashMap<String, Integer>();

      calculate(key, categoryBillCharges, billChargeClaimMap, visitInsurancePlans, adjStatusMap);
      if (null != adjStatusMap.get("adjStatus")
          && (adjStatusMap.get("adjStatus")).compareTo(0) != 0) {
        ruleAdjustmentMap.put(key, adjStatusMap.get("adjStatus"));
      }
    }

    return ruleAdjustmentMap;

  }

  /**
   * Calculate.
   *
   * @param categoryId          the category id
   * @param billCharges         the bill charges
   * @param billChargeClaims    the bill charge claims
   * @param visitInsurancePlans the visit insurance plans
   * @param adjStatusMap        the adj status map
   */
  private void calculate(int categoryId, List<BasicDynaBean> billCharges,
      Map<String, List<BasicDynaBean>> billChargeClaims, List<BasicDynaBean> visitInsurancePlans,
      Map adjStatusMap) {

    Map<Integer, List<BasicDynaBean>> visitInsPlansMap = 
        new HashMap<Integer, List<BasicDynaBean>>();
    Map<String, BasicDynaBean> claims = new HashMap<String, BasicDynaBean>();
    int index = 0;

    visitInsPlansMap = ConversionUtils.listBeanToMapListBean(visitInsurancePlans, "plan_id");

    for (Integer key : visitInsPlansMap.keySet()) {
      List<BasicDynaBean> visitInsList = visitInsPlansMap.get(key);

      Map<Integer, BasicDynaBean> visitInsPlanCategoryMap = new HashMap<Integer, BasicDynaBean>();
      visitInsPlanCategoryMap = ConversionUtils.listBeanToMapBean(visitInsList,
          "insurance_category_id");

      for (String billChargeKey : billChargeClaims.keySet()) {
        claims.put(billChargeKey, billChargeClaims.get(billChargeKey).get(index));
      }

      for (CategoryInsuranceRule rule : insuranceRules) {
        if ( visitInsPlanCategoryMap.get(categoryId) == null ) { 
          continue;
          // Master has new category updated after adding plan to a 
          // visit and hence can not consider this item for claim calculations.
        } 
        if (!rule.apply(categoryId, billCharges, claims, visitInsPlanCategoryMap, adjStatusMap)) {
          break;
        }
      }

      index++;
    }

  }

}
