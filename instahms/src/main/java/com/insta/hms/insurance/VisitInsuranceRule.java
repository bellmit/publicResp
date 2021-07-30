package com.insta.hms.insurance;

import org.apache.commons.beanutils.BasicDynaBean;

import java.util.List;
import java.util.Map;

/**
 * The Interface VisitInsuranceRule.
 */
public interface VisitInsuranceRule {

  /**
   * Apply.
   *
   * @param billCharges        the bill charges
   * @param claims             the claims
   * @param visitInsurancePlan the visit insurance plan
   * @param adjStatusMap       the adj status map
   * @return true, if successful
   */
  public boolean apply(List<BasicDynaBean> billCharges, Map<String, BasicDynaBean> claims,
      BasicDynaBean visitInsurancePlan, Map adjStatusMap);

}
