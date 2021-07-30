package com.insta.hms.insurance;

import com.insta.hms.common.ConversionUtils;

import org.apache.commons.beanutils.BasicDynaBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class VisitInsuranceCalculator.
 */
public class VisitInsuranceCalculator {

  /** The insurance rules. */
  private List<VisitInsuranceRule> insuranceRules;

  /**
   * Instantiates a new visit insurance calculator.
   */
  public VisitInsuranceCalculator() {
    insuranceRules = new ArrayList<VisitInsuranceRule>();

    insuranceRules.add(new VisitCopayDeductibleRule());
    insuranceRules.add(new VisitCopayPercentRule());
    insuranceRules.add(new VisitMaxCopayRule());
    insuranceRules.add(new VisitPerDayLimitRule());
    insuranceRules.add(new VisitSponsorLimitRule());
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
      Map<String, List<BasicDynaBean>> billChargeClaims, List<BasicDynaBean> visitInsurancePlans,
      Map<String, Object> additionalDetails) {

    Map<Integer, List<BasicDynaBean>> visitInsPlansMap = 
        new HashMap<Integer, List<BasicDynaBean>>();
    Map<String, BasicDynaBean> claims = new HashMap<String, BasicDynaBean>();
    int index = 0;

    Map<Integer, Integer> ruleAdjustmentMap = new HashMap<Integer, Integer>();

    visitInsPlansMap = ConversionUtils.listBeanToMapListBean(visitInsurancePlans, "plan_id");

    for (Integer key : visitInsPlansMap.keySet()) {
      BasicDynaBean visitInsBean = visitInsPlansMap.get(key).get(0);

      Map<String, Integer> adjStatusMap = new HashMap<String, Integer>();

      for (String chargeClaimKey : billChargeClaims.keySet()) {
        claims.put(chargeClaimKey, billChargeClaims.get(chargeClaimKey).get(index));
      }

      for (VisitInsuranceRule rule : insuranceRules) {
        if (!rule.apply(billCharges, claims, visitInsBean, adjStatusMap)) {
          break;
        }
      }
      
      if (null != additionalDetails) {
        boolean preAuthAmountExceeded = AdvanceInsuranceHelper.isPreAuthAmountExceeded(billCharges,
            claims, visitInsBean);
        additionalDetails.put("preAuthAmountExceeded", preAuthAmountExceeded);
        additionalDetails.put("preAuthRequiredMap", AdvanceInsuranceHelper
            .getPreAuthRequiredMap(billCharges, claims, preAuthAmountExceeded, visitInsBean));
      }
      

      if (adjStatusMap.get("adjStatus") != null
          && (adjStatusMap.get("adjStatus")).compareTo(0) != 0) {
        ruleAdjustmentMap.put(-2, adjStatusMap.get("adjStatus"));
      }

      index++;

    }

    return ruleAdjustmentMap;

  }

}
