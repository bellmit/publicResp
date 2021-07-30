package com.insta.hms.mdm.dynapackage;

import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.master.rateplan.UpdateChargesHelper;
import com.insta.hms.mdm.rateplan.RatePlanParametersService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc

/**
 * The Class DynaPackageChargesService.
 */
/*
 * @author eshwar-chandra
 */
@Service
public class DynaPackageChargesService {

  /**
   * The logger.
   */
  static Logger logger = LoggerFactory.getLogger(DynaPackageChargesService.class);

  /**
   * The dyna package charges repository.
   */
  @LazyAutowired
  private DynaPackageChargesRepository dynaPackageChargesRepository;
  /**
   * The dyna package category limits service.
   */
  @LazyAutowired
  private DynaPackageCategoryLimitsService dynaPackageCategoryLimitsService;
  /**
   * The dyna package org details service.
   */
  @LazyAutowired
  private DynaPackageOrgDetailsService dynaPackageOrgDetailsService;
  /**
   * The rate plan parameters service.
   */
  @LazyAutowired
  private RatePlanParametersService ratePlanParametersService;

  /**
   * Update charges based on new rate sheet.
   *
   * @param ratePlanId the rate plan id
   * @param varianceBy the variance by
   * @param rateSheetId the rate sheet id
   * @param nearstRoundOfValue the nearst round of value
   * @param categoryId the category id
   * @param category the category
   * @return true, if successful
   */
  public boolean updateChargesBasedOnNewRateSheet(String ratePlanId, Double varianceBy,
      String rateSheetId, Double nearstRoundOfValue, String categoryId, String category) {

    boolean success = false;

    if (category.equals("dynapackages")) {
      success = updateChargesBasedOnNewRateSheet(ratePlanId, varianceBy,
          rateSheetId, nearstRoundOfValue, categoryId);
      success =
          dynaPackageCategoryLimitsService.updateChargesBasedOnNewRateSheet(ratePlanId, varianceBy,
              rateSheetId, nearstRoundOfValue, categoryId);
    }
    return success;
  }

  /**
   * Update charges based on new rate sheet.
   *
   * @param orgId              the org id
   * @param varianceBy         the variance by
   * @param baseOrgId          the base org id
   * @param nearstRoundOfValue the nearst round of value
   * @param dynaPackageId      the dyna package id
   * @return true, if successful
   */
  public boolean updateChargesBasedOnNewRateSheet(String orgId, Double varianceBy,
      String baseOrgId, Double nearstRoundOfValue, String dynaPackageId) {
    return dynaPackageChargesRepository
        .updateChargesBasedOnNewRateSheet(orgId, varianceBy, baseOrgId, nearstRoundOfValue,
            dynaPackageId);
  }

  /**
   * Copy general charges to all orgs.
   *
   * @param dynaPackageId the dyna package id
   * @return the object
   */
  public Object copyGeneralChargesToAllOrgs(int dynaPackageId) {
    return dynaPackageChargesRepository.copyGeneralChargesToAllOrgs(dynaPackageId);
  }

  /**
   * Update applicableflag for derived rate plans.
   *
   * @param derivedRatePlanIds the derived rate plan ids
   * @param chargeCategory     the charge category
   * @param categoryIdName     the category id name
   * @param categoryIdValue    the category id value
   * @param orgDetailTblName   the org detail tbl name
   * @param orgId              the org id
   * @return true, if successful
   * @throws Exception the exception
   */
  @Transactional(rollbackFor = Exception.class)
  public boolean updateApplicableflagForDerivedRatePlans(List<BasicDynaBean> derivedRatePlanIds,
      String chargeCategory, String categoryIdName, String categoryIdValue,
      String orgDetailTblName, String orgId) throws Exception {
    boolean success = true;
    Map<String, Object> keys = new HashMap<>();
    if (chargeCategory.equals("consultation") || chargeCategory.equals("dynapackages")
        || chargeCategory.equals("packages")) {
      keys.put(categoryIdName, Integer.parseInt(categoryIdValue));
    } else {
      keys.put(categoryIdName, categoryIdValue);
    }

    for (int k = 0; k < derivedRatePlanIds.size(); k++) {
      BasicDynaBean rbean = derivedRatePlanIds.get(k);
      String ratePlanId = (String) rbean.get("org_id");
      keys.put("org_id", ratePlanId);
      BasicDynaBean rpBean = dynaPackageOrgDetailsService.findByKey(keys);
      String isOverrided = (String) rpBean.get("is_override");
      if (!isOverrided.equals("Y")) {
        success = updateApplicableFlag(ratePlanId, orgDetailTblName, categoryIdName,
            categoryIdValue, chargeCategory, orgId);
        if (!success) {
          break;
        }
      }
    }
    return success;
  }

  /**
   * Update applicable flag.
   *
   * @param ratePlanId       the rate plan id
   * @param orgDetailTblName the org detail tbl name
   * @param categoryIdName   the category id name
   * @param categoryId       the category id
   * @param chargeCategory   the charge category
   * @param rateSheetId      the rate sheet id
   * @return true, if successful
   * @throws Exception the exception
   */
  public boolean updateApplicableFlag(String ratePlanId, String orgDetailTblName,
      String categoryIdName, String categoryId, String chargeCategory, String rateSheetId)
      throws Exception {

    GenericDAO dao = new GenericDAO(orgDetailTblName);
    BasicDynaBean bean = dao.getBean();

    bean.set("org_id", ratePlanId);
    String rateSheet = null;

    rateSheet =
        getOtherRatesheetId(ratePlanId, rateSheetId, orgDetailTblName, categoryIdName, categoryId,
            chargeCategory);
    if (rateSheet != null) {
      bean.set("applicable", true);
      bean.set("base_rate_sheet_id", rateSheet);
    } else {
      List<BasicDynaBean> raetSheetList = getRateSheetsByPriority(ratePlanId);
      BasicDynaBean prBean = raetSheetList.get(0);
      rateSheet = (String) prBean.get("base_rate_sheet_id");
      bean.set("applicable", false);
      bean.set("base_rate_sheet_id", rateSheet);
    }

    boolean success = false;
    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("org_id", ratePlanId);
    if (chargeCategory.equals("consultation") || chargeCategory.equals("dynapackages")
        || chargeCategory.equals("packages")) {
      keys.put(categoryIdName, Integer.parseInt(categoryId));
    } else {
      keys.put(categoryIdName, categoryId);
    }

    success = dynaPackageOrgDetailsService.update(bean, keys) > 0;
    recalculateCharges(ratePlanId, rateSheet, categoryId, chargeCategory);
    return success;

  }

  /**
   * Recalculate charges.
   *
   * @param ratePlanId     the rate plan id
   * @param rateSheet      the rate sheet
   * @param categoryId     the category id
   * @param chargeCategory the charge category
   * @return true, if successful
   */
  public boolean recalculateCharges(String ratePlanId, String rateSheet,
      String categoryId, String chargeCategory) {

    boolean success = false;
    Map<String, Object> rkeys = new HashMap<String, Object>();
    rkeys.put("org_id", ratePlanId);
    rkeys.put("base_rate_sheet_id", rateSheet);
    BasicDynaBean rbean = ratePlanParametersService.findByKey(rkeys);
    Double varianceBy = new Double((Integer) rbean.get("rate_variation_percent"));
    Double nearstRoundOfValue = new Double((Integer) rbean.get("round_off_amount"));
    success = updateChargesBasedOnNewRateSheet(ratePlanId, varianceBy,
        rateSheet, nearstRoundOfValue, categoryId, chargeCategory);
    return success;
  }

  /**
   * Gets the other ratesheet id.
   *
   * @param ratePlanId       the rate plan id
   * @param rateSheetId      the rate sheet id
   * @param orgDetailTblName the org detail tbl name
   * @param categoryIdName   the category id name
   * @param categoryId       the category id
   * @param chargeCategory   the charge category
   * @return the other ratesheet id
   */
  public String getOtherRatesheetId(String ratePlanId, String rateSheetId,
      String orgDetailTblName, String categoryIdName, String categoryId, String chargeCategory) {
    String newRateSheet = null;
    List<BasicDynaBean> rateSheetList = getRateSheetsByPriority(ratePlanId);
    for (int i = 0; i < rateSheetList.size(); i++) {
      BasicDynaBean rbean = rateSheetList.get(i);
      String baseRateSheetId = (String) rbean.get("base_rate_sheet_id");
      if (checkItemExistence(baseRateSheetId, orgDetailTblName, categoryIdName, categoryId,
          chargeCategory)) {
        newRateSheet = baseRateSheetId;
        break;
      }
    }
    return newRateSheet;
  }

  /**
   * Check item existence.
   *
   * @param rateSheetId      the rate sheet id
   * @param orgDetailTblName the org detail tbl name
   * @param categoryIdName   the category id name
   * @param categoryId       the category id
   * @param chargeCategory   the charge category
   * @return true, if successful
   */
  public boolean checkItemExistence(String rateSheetId, String orgDetailTblName,
      String categoryIdName,
      String categoryId, String chargeCategory) {
    boolean success = false;
    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("org_id", rateSheetId);
    if (chargeCategory.equals("consultation") || chargeCategory.equals("dynapackages")
        || chargeCategory.equals("packages")) {
      keys.put(categoryIdName, Integer.parseInt(categoryId));
    } else {
      keys.put(categoryIdName, categoryId);
    }
    BasicDynaBean bean = dynaPackageOrgDetailsService.findByKey(keys);

    if (bean.get("applicable").equals(true)) {
      success = true;
    }

    return success;
  }

  /**
   * Gets the rate sheets by priority.
   *
   * @param orgId the org id
   * @return the rate sheets by priority
   */
  public List<BasicDynaBean> getRateSheetsByPriority(String orgId) {
    Map<String, Object> filterMap = new HashMap<String, Object>();
    filterMap.put("org_id", orgId);
    List<BasicDynaBean> sheets = ratePlanParametersService.listAll(null, filterMap, "priority");
    logger.info("base rate sheet count :" + ((null != sheets) ? sheets.size() : "null"));
    return sheets;
  }

  /**
   * Updatedyna pack charges for derived rate plans.
   *
   * @param rateplanIds   the rateplan ids
   * @param orgId         the org id
   * @param dynaPackageId the dyna package id
   * @param applicable    the applicable
   * @return true, if successful
   */
  public boolean updatedynaPackChargesForDerivedRatePlans(String[] rateplanIds, String orgId,
      String dynaPackageId, String[] applicable) {
    boolean success = false;

    for (int i = 0; i < rateplanIds.length; i++) {

      Map<String, Object> keys = new HashMap<String, Object>();
      keys.put("base_rate_sheet_id", orgId);
      keys.put("org_id", rateplanIds[i]);
      BasicDynaBean bean = ratePlanParametersService.findByKey(keys);
      Double varianceBy = new Double((Integer) bean.get("rate_variation_percent"));
      Double nearstRoundOfValue = new Double((Integer) bean.get("round_off_amount"));
      Object[] values =
          new Object[] {new BigDecimal(varianceBy), new BigDecimal(nearstRoundOfValue),
              rateplanIds[i], orgId, Integer.parseInt(dynaPackageId)};

      success = dynaPackageChargesRepository.updateDynaPackageCharges(values) > 0;
      boolean overrideItemCharges = checkItemStatus(rateplanIds[i], "dyna_package_org_details",
          "dynapackages", "dyna_package_id", dynaPackageId, applicable[i]);
      if (overrideItemCharges) {
        overrideItemCharges("dyna_package_charges", "dynapackages", rateplanIds[i],
            "dyna_package_id", dynaPackageId);
      }
    }
    return success;
  }

  /**
   * Check item status.
   *
   * @param ratePlanId     the rate plan id
   * @param orgDetailsTbl  the org details tbl
   * @param chargeCategory the charge category
   * @param categoryId     the category id
   * @param categoryValue  the category value
   * @param applicable     the applicable
   * @return true, if successful
   */
  public boolean checkItemStatus(String ratePlanId, String orgDetailsTbl, String chargeCategory,
      String categoryId, String categoryValue, String applicable) {

    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("org_id", ratePlanId);
    if (chargeCategory.equals("consultation") || chargeCategory.equals("dynapackages")
        || chargeCategory.equals("packages")) {
      keys.put(categoryId, Integer.parseInt(categoryValue));
    } else {
      keys.put(categoryId, categoryValue);
    }
    BasicDynaBean bean = dynaPackageOrgDetailsService.findByKey(keys);
    String baseRateSheetId = (String) bean.get("base_rate_sheet_id");

    keys.put("org_id", baseRateSheetId);
    bean = dynaPackageOrgDetailsService.findByKey(keys);
    return bean.get("applicable").equals(true) != applicable.equals("true");
  }

  /**
   * Override item charges.
   *
   * @param chargeTblName   the charge tbl name
   * @param chargeCategory  the charge category
   * @param orgId           the org id
   * @param categoryIdName  the category id name
   * @param categoryIdValue the category id value
   * @return true, if successful
   */
  public boolean overrideItemCharges(String chargeTblName, String chargeCategory, String orgId,
      String categoryIdName, String categoryIdValue) {

    BasicDynaBean chargeBean = dynaPackageChargesRepository.getBean();
    chargeBean.set("is_override", "Y");
    Map<String, Object> chKeys = new HashMap<String, Object>();
    boolean success = false;
    if (chargeCategory.equals("diagnostics")) {
      chKeys.put("org_name", orgId);
    } else {
      chKeys.put("org_id", orgId);
    }
    if (chargeCategory.equals("operations")) {
      chKeys.put("op_id", categoryIdValue);
    } else {
      if (chargeCategory.equals("consultation") || chargeCategory.equals("dynapackages")
          || chargeCategory.equals("packages")) {
        chKeys.put(categoryIdName, Integer.parseInt(categoryIdValue));
      } else {
        chKeys.put(categoryIdName, categoryIdValue);
      }
    }
    success = dynaPackageChargesRepository.update(chargeBean, chKeys) > 0;
    return success;
  }
}
