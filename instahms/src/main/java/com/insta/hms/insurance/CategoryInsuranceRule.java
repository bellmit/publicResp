package com.insta.hms.insurance;

import org.apache.commons.beanutils.BasicDynaBean;

import java.util.List;
import java.util.Map;

/**
 * The Interface CategoryInsuranceRule.
 */
public interface CategoryInsuranceRule {

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
      Map<Integer, BasicDynaBean> visitInsPlanCategoryMap, Map adjStatusMap);

}
