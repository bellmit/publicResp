package com.insta.hms.mdm.dynapackage;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.rateplan.RatePlanParametersService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/*
 * @author eshwar-chandra
 */
@Service
public class DynaPackageCategoryLimitsService {

  @LazyAutowired
  private DynaPackageCategoryLimitsRepository dynaPackageCategoryLimitsRepository;

  @LazyAutowired
  private RatePlanParametersService ratePlanParametersService;

  /*
   * Copy the GENERAL rate plan charges to all other rate plans. Assumes that the other
   * rate-plan charges are non-existent, ie, no updates only inserts new charges.
   */
  public Integer copyGeneralChargesToAllOrgs(int dynaPackageId, String userName) {
    return dynaPackageCategoryLimitsRepository.copyGeneralChargesToAllOrgs(dynaPackageId, userName);
  }

  public boolean updateChargesBasedOnNewRateSheet(String orgId, Double varianceBy,
      String baseOrgId, Double nearstRoundOfValue, String dynaPackageId) {
    return dynaPackageCategoryLimitsRepository.updateChargesBasedOnNewRateSheet(orgId, varianceBy,
        baseOrgId, nearstRoundOfValue, dynaPackageId);
  }

  /**
   * Update category limits for derived rate plans.
   *
   * @param rateplanIds the rateplan ids
   * @param orgId the org id
   * @param dynaPackageId the dyna package id
   * @param categoryIds the category ids
   * @return true, if successful
   */
  public boolean updateCategoryLimitsForDerivedRatePlans(String[] rateplanIds, String orgId,
      String dynaPackageId, String[] categoryIds) {

    boolean success = false;
    Map<String, Object> keys = new HashMap<String, Object>();

    for (int i = 0; i < rateplanIds.length; i++) {
      keys.put("base_rate_sheet_id", orgId);
      keys.put("org_id", rateplanIds[i]);
      BasicDynaBean bean = ratePlanParametersService.findByKey(keys);
      Double varianceBy = new Double((Integer) bean.get("rate_variation_percent"));
      Double nearstRoundOfValue = new Double((Integer) bean.get("round_off_amount"));

      for (int k = 0; k < categoryIds.length; k++) {
        int updatedRows =
            dynaPackageCategoryLimitsRepository.updateCategoryLimitsForDerivedRatePlans(
                new Object[] {new BigDecimal(varianceBy), new BigDecimal(nearstRoundOfValue),
                    rateplanIds[i], orgId, Integer.parseInt(dynaPackageId),
                    Integer.parseInt(categoryIds[k])});
        if (updatedRows >= 0) {
          success = true;
        }
      }
    }
    return success;
  }
}
