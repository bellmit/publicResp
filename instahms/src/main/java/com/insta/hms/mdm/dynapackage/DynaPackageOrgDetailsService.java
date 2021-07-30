package com.insta.hms.mdm.dynapackage;

import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class DynaPackageOrgDetailsService.
 * 
 * @author eshwar-chandra
 */
@Service
public class DynaPackageOrgDetailsService {

  /** The dyna package org details repository. */
  @LazyAutowired
  private DynaPackageOrgDetailsRepository dynaPackageOrgDetailsRepository;

  /**
   * Copy dyna package details to all orgs.
   *
   * @param dynaPackageId the dyna package id
   * @return the integer
   */
  @Transactional(rollbackFor = Exception.class)
  public Integer copyDynaPackageDetailsToAllOrgs(int dynaPackageId) {
    return dynaPackageOrgDetailsRepository.copyDynaPackageDetailsToAllOrgs(dynaPackageId);
  }

  /**
   * Find by key.
   *
   * @param keys the keys
   * @return the basic dyna bean
   */
  public BasicDynaBean findByKey(Map<String, Object> keys) {
    return dynaPackageOrgDetailsRepository.findByKey(keys);
  }

  /**
   * Update.
   *
   * @param bean the bean
   * @param keys the keys
   * @return the integer
   */
  public Integer update(BasicDynaBean bean, Map<String, Object> keys) {
    return dynaPackageOrgDetailsRepository.update(bean, keys);
  }

  /**
   * update Org For Derived Rate Plans.
   *
   * @param ratePlanIds   ratePlanIds
   * @param applicable applicable
   * @param dynaPackId dynaPackId
   * @return true, if successful
   */
  public boolean updateOrgForDerivedRatePlans(String[] ratePlanIds, String[] applicable,
      String dynaPackId)
      throws Exception {
    return updateOrgForDerivedRatePlans(ratePlanIds, applicable, "dyna_package_org_details",
        "dynapackages",
        "dyna_package_id", dynaPackId);
  }

  /**
   * Update org for derived rate plans.
   *
   * @param ratePlanIds the rate plan ids
   * @param applicable the applicable
   * @param orgTbl the org tbl
   * @param category the category
   * @param categoryId the category id
   * @param categoryIdValue the category id value
   * @return true, if successful
   * 
   */
  public boolean updateOrgForDerivedRatePlans(String[] ratePlanIds, String[] applicable,
      String orgTbl,
      String category, String categoryId, String categoryIdValue) {
    boolean success = false;
    for (int i = 0; i < ratePlanIds.length; i++) {
      BasicDynaBean bean = dynaPackageOrgDetailsRepository.getBean();
      Map<String, Object> keys = new HashMap<String, Object>();
      keys.put("org_id", ratePlanIds[i]);
      if (category.equals("consultation") || category.equals("packages")
          || category.equals("dynapackages")) {
        keys.put(categoryId, Integer.parseInt(categoryIdValue));
      } else {
        keys.put(categoryId, categoryIdValue);
      }
      if (applicable[i].equals("true")) {
        bean.set("applicable", true);
      } else {
        bean.set("applicable", false);
      }
      boolean overrideItemCharges = checkItemStatus(ratePlanIds[i], orgTbl, category, categoryId,
          categoryIdValue, applicable[i]);
      if (overrideItemCharges) {
        bean.set("is_override", "Y");
      }

      int updatedRows = dynaPackageOrgDetailsRepository.update(bean, keys);
      success = updatedRows > 0;
    }
    return success;
  }

  /**
   * checkItemStatus.
   *
   * @param ratePlanId the rate plan id
   * @param orgDetailsTbl orgDetailsTbl
   * @param chargeCategory chargeCategory
   * @param categoryId categoryId
   * @param categoryValue categoryValue
   * @param applicable applicable
   * @return true, if successful
   */
  private boolean checkItemStatus(String ratePlanId, String orgDetailsTbl, String chargeCategory,
      String categoryId, String categoryValue, String applicable) {

    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("org_id", ratePlanId);
    if (chargeCategory.equals("consultation") || chargeCategory.equals("dynapackages")
        || chargeCategory.equals("packages")) {
      keys.put(categoryId, Integer.parseInt(categoryValue));
    } else {
      keys.put(categoryId, categoryValue);
    }
    BasicDynaBean bean = dynaPackageOrgDetailsRepository.findByKey(keys);
    String baseRateSheetId = (String) bean.get("base_rate_sheet_id");

    keys.put("org_id", baseRateSheetId);
    bean = dynaPackageOrgDetailsRepository.findByKey(keys);
    return bean.get("applicable").equals(true) != applicable.equals("true");
  }

}
