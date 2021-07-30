package com.insta.hms.mdm.testorganization;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.clinical.order.master.OrgDetailsRepository;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class TestOrganizationRepository.
 *
 * @author anil.n
 */
@Repository
public class TestOrganizationRepository extends MasterRepository<String> implements
    OrgDetailsRepository {

  /** The test organization service. */
  @LazyAutowired
  private TestOrganizationService testOrganizationService;

  /**
   * Instantiates a new test organization repository.
   */
  public TestOrganizationRepository() {
    // TODO : composite primary key
    super("test_org_details", "test_id");
  }

  /** The Constant GET_ORG_ITEM_CODE. */
  private static final String GET_ORG_ITEM_CODE = 
      "SELECT * FROM test_org_details where test_id = ? AND org_id = ?";

  /**
   * Gets the org item code.
   *
   * @param orgId the org id
   * @param testId the test id
   * @return the org item code
   */
  public List<BasicDynaBean> getOrgItemCode(String orgId, String testId) {
    return DatabaseHelper.queryToDynaList(GET_ORG_ITEM_CODE, testId, orgId);
  }

  /** The Constant GET_TEST_NOT_APPLICABLE. */
  private static final String GET_TEST_NOT_APPLICABLE = 
      " SELECT org_id FROM test_org_details where test_id = ? AND applicable = false "
      + " AND org_id != ? ";

  /**
   * Gets the test not applicable rate plans.
   *
   * @param testId the test id
   * @param orgId the org id
   * @return the test not applicable rate plans
   */
  public List<BasicDynaBean> getTestNotApplicableRatePlans(String testId, String orgId) {
    return DatabaseHelper.queryToDynaList(GET_TEST_NOT_APPLICABLE, testId, orgId);
  }

  /** The Constant UPDATE_ORG_DETAILS. */
  private static final String UPDATE_ORG_DETAILS = 
      "UPDATE test_org_details SET applicable = true WHERE test_id = ? AND org_id = ?";

  /**
   * Update test organization details.
   *
   * @param testId the test id
   * @param orgId the org id
   * @return true, if successful
   */
  public boolean updateTestOrganizationDetails(String testId, String orgId) {
    return (DatabaseHelper.update(UPDATE_ORG_DETAILS, testId, orgId)) > 0;
  }

  /** The Constant UPDATE_FLAG. */
  private static final String UPDATE_FLAG = 
      "UPDATE test_org_details SET applicable = ?, is_override = ? WHERE test_id = ? "
      + " AND org_id = ?";

  /**
   * Update org for derived rate plans.
   *
   * @param ratePlanIds the rate plan ids
   * @param applicable the applicable
   * @param repository the repository
   * @param category the category
   * @param categoryId the category id
   * @param categoryIdValue the category id value
   * @return true, if successful
   */
  public boolean updateOrgForDerivedRatePlans(String[] ratePlanIds, String[] applicable,
      MasterRepository repository, String category, String categoryId, String categoryIdValue) {

    // TODO : this is implemented only for diag tests , todo same for operations, services and other
    // masters.
    boolean success = true;
    for (int i = 0; i < ratePlanIds.length; i++) {
      boolean applicableFlag = true;
      String override = "N";
      BasicDynaBean bean = super.getBean();
      Map<String, Object> keys = new HashMap<>();
      keys.put("org_id", ratePlanIds[i]);
      if (category.equals("consultation") || category.equals("packages")
          || category.equals("dynapackages")) {
        keys.put(categoryId, Integer.parseInt(categoryIdValue));
      } else {
        keys.put(categoryId, categoryIdValue);
      }
      if (applicable[i].equals("true")) {
        bean.set("applicable", true);
        applicableFlag = true;
      } else {
        bean.set("applicable", false);
        applicableFlag = false;
      }
      boolean overrideItemCharges = checkItemStatus(ratePlanIds[i], repository, category,
          categoryId, categoryIdValue, applicable[i]);
      if (overrideItemCharges) {
        override = "Y";
      }
      
      int updated = DatabaseHelper.update(UPDATE_FLAG,
          new Object[] { applicableFlag, override, categoryIdValue, ratePlanIds[i] }); 
     
      success = updated > 0;
    }
    return success;
  }

  /** The Constant CHECK_TEST_EXIST. */
  private static final String CHECK_TEST_EXIST = 
      "SELECT * FROM test_org_details WHERE org_id = ? AND test_id = ?";

  /**
   * Check item status.
   *
   * @param ratePlanId the rate plan id
   * @param repository the repository
   * @param chargeCategory the charge category
   * @param categoryId the category id
   * @param categoryValue the category value
   * @param applicable the applicable
   * @return true, if successful
   */
  public boolean checkItemStatus(String ratePlanId, MasterRepository repository,
      String chargeCategory, String categoryId, String categoryValue, String applicable) {

    // TODO : for consultation, dynapackages and packages. This is only for diagnostics.
    
    BasicDynaBean bean = DatabaseHelper.queryToDynaBean(CHECK_TEST_EXIST, ratePlanId,
        categoryValue);
    String baseRateSheetId = (String) bean.get("base_rate_sheet_id");
    bean = DatabaseHelper.queryToDynaBean(CHECK_TEST_EXIST, baseRateSheetId, categoryValue);
    return bean.get("applicable").equals(true) != applicable.equals("true");
  }

  /** The Constant CHECK_EXIST_TEST. */
  private static final String CHECK_EXIST_TEST = 
      " SELECT * FROM test_org_details WHERE org_id = ? AND test_id = ? ";

  // TODO : this is common method for consultation, diagnostics, dynapackages and packages, now
  /**
   * Update applicableflag for derived rate plans.
   *
   * @param derivedRatePlanIds the derived rate plan ids
   * @param chargeCategory the charge category
   * @param categoryIdName the category id name
   * @param categoryIdValue the category id value
   * @param orgDetailTblName the org detail tbl name
   * @param orgId the org id
   * @return true, if successful
   */
  // taking diagnostics only later will change to common
  public boolean updateApplicableflagForDerivedRatePlans(List<BasicDynaBean> derivedRatePlanIds,
      String chargeCategory, String categoryIdName, String categoryIdValue, String orgDetailTblName,
      String orgId) {

    boolean success = true;

    for (int k = 0; k < derivedRatePlanIds.size(); k++) {
      BasicDynaBean drBean = derivedRatePlanIds.get(k);
      String ratePlanId = (String) drBean.get("org_id");
      BasicDynaBean rpBean = DatabaseHelper.queryToDynaBean(CHECK_EXIST_TEST, ratePlanId,
          categoryIdValue);
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

  /** The Constant UODATE_APLLICATION_FLAG. */
  private static final String UODATE_APLLICATION_FLAG = 
      "UPDATE test_org_details SET applicable = ? ,base_rate_sheet_id = ?"
      + " WHERE org_id = ? AND test_id = ? ";

  /**
   * Update applicable flag.
   *
   * @param ratePlanId the rate plan id
   * @param orgDetailTblName the org detail tbl name
   * @param categoryIdName the category id name
   * @param categoryId the category id
   * @param chargeCategory the charge category
   * @param rateSheetId the rate sheet id
   * @return true, if successful
   */
  public boolean updateApplicableFlag(String ratePlanId, String orgDetailTblName,
      String categoryIdName, String categoryId, String chargeCategory, String rateSheetId) {

    boolean success = true;
    String rateSheet = null;
    boolean applicable = true;

    rateSheet = getOtherRatesheetId(ratePlanId, rateSheetId, orgDetailTblName, categoryIdName,
        categoryId, chargeCategory);
    if (rateSheet == null) {
      List<BasicDynaBean> raetSheetList = getRateSheetsByPriority(ratePlanId);
      BasicDynaBean prBean = raetSheetList.get(0);
      applicable = false;
      rateSheet = (String) prBean.get("base_rate_sheet_id");
    }
    int updated = DatabaseHelper.update(UODATE_APLLICATION_FLAG, applicable, rateSheet, ratePlanId,
        categoryId);
    if (updated < 1) {
      success = false;
    }
    recalculateCharges(ratePlanId, rateSheet, categoryId, chargeCategory);
    return success;

  }

  /**
   * Gets the other ratesheet id.
   *
   * @param ratePlanId the rate plan id
   * @param rateSheetId the rate sheet id
   * @param orgDetailTblName the org detail tbl name
   * @param categoryIdName the category id name
   * @param categoryId the category id
   * @param chargeCategory the charge category
   * @return the other ratesheet id
   */
  public String getOtherRatesheetId(String ratePlanId, String rateSheetId, String orgDetailTblName,
      String categoryIdName, String categoryId, String chargeCategory) {

    String newRateSheet = null;
    List<BasicDynaBean> rateSheetList = getRateSheetsByPriority(ratePlanId); 
    // TODO : make this method as common, now its only rate_plan_parametrs
    for (int i = 0; i < rateSheetList.size(); i++) {
      BasicDynaBean rateSheetBean = rateSheetList.get(i);
      String baseRateSheetId = (String) rateSheetBean.get("base_rate_sheet_id");
      if (checkItemExistence(baseRateSheetId, orgDetailTblName, categoryIdName, categoryId,
          chargeCategory)) {
        newRateSheet = baseRateSheetId;
        break;
      }
    }
    return newRateSheet;
  }

  /** The Constant GET_RATE_PLAN_PARAMETERS. */
  private static final String GET_RATE_PLAN_PARAMETERS = 
      "SELECT * FROM rate_plan_parameters WHERE org_id = ? ORDER BY priority";

  /**
   * Gets the rate sheets by priority.
   *
   * @param orgId the org id
   * @return the rate sheets by priority
   */
  public List<BasicDynaBean> getRateSheetsByPriority(String orgId) {
    return DatabaseHelper.queryToDynaList(GET_RATE_PLAN_PARAMETERS, orgId);
  }

  /**
   * Check item existence.
   *
   * @param rateSheetId the rate sheet id
   * @param orgDetailTblName the org detail tbl name
   * @param categoryIdName the category id name
   * @param categoryId the category id
   * @param chargeCategory the charge category
   * @return true, if successful
   */
  // This is only for diagnostics
  public boolean checkItemExistence(String rateSheetId, String orgDetailTblName,
      String categoryIdName, String categoryId, String chargeCategory) {

    boolean success = false;
    BasicDynaBean bean = DatabaseHelper.queryToDynaBean(CHECK_TEST_EXIST, rateSheetId, categoryId);
    if (bean.get("applicable").equals(true)) {
      success = true;
    }
    return success;
  }

  /** The Constant GET_RATE_PLAN_DETAILS. */
  private static final String GET_RATE_PLAN_DETAILS = 
      "SELECT * FROM rate_plan_parameters WHERE org_id = ? AND base_rate_sheet_id = ?";

  /**
   * Recalculate charges.
   *
   * @param ratePlanId the rate plan id
   * @param rateSheet the rate sheet
   * @param categoryId the category id
   * @param chargeCategory the charge category
   * @return true, if successful
   */
  public boolean recalculateCharges(String ratePlanId, String rateSheet, String categoryId,
      String chargeCategory) {

    boolean success = true;
    BasicDynaBean rpBean = DatabaseHelper.queryToDynaBean(GET_RATE_PLAN_DETAILS, ratePlanId,
        rateSheet);
    Double varianceBy = ((Integer) rpBean.get("rate_variation_percent")).doubleValue();
    Double nearstRoundOfValue = ((Integer) rpBean.get("round_off_amount")).doubleValue();
    success = updateChargesBasedOnNewRateSheet(ratePlanId, varianceBy, rateSheet,
        nearstRoundOfValue, categoryId, chargeCategory);
    return success;
  }

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

    return updateChargesBasedOnNewRateSheet(ratePlanId, varianceBy, rateSheetId, nearstRoundOfValue,
        categoryId);
  }

  /** The Constant UPDATE_RATEPLAN_DIAG_CHARGES. */
  private static final String UPDATE_RATEPLAN_DIAG_CHARGES = "UPDATE diagnostic_charges totab SET "
      + " charge = doroundvarying(fromtab.charge,?,?), "
      + " discount = doroundvarying(fromtab.discount,?,?) "
      + " FROM diagnostic_charges fromtab" + " WHERE totab.org_name = ? AND fromtab.org_name = ?"
      + " AND totab.test_id = fromtab.test_id AND totab.bed_type = fromtab.bed_type "
      + " AND totab.is_override='N' ";

  /**
   * Update charges based on new rate sheet.
   *
   * @param orgId the org id
   * @param varianceBy the variance by
   * @param baseOrgId the base org id
   * @param nearstRoundOfValue the nearst round of value
   * @param testId the test id
   * @return true, if successful
   */
  public boolean updateChargesBasedOnNewRateSheet(String orgId, Double varianceBy, String baseOrgId,
      Double nearstRoundOfValue, String testId) {

    String query = UPDATE_RATEPLAN_DIAG_CHARGES + "AND totab.test_id = ? ";
    int updated = DatabaseHelper.update(query, new BigDecimal(varianceBy),
        new BigDecimal(nearstRoundOfValue), new BigDecimal(varianceBy),
        new BigDecimal(nearstRoundOfValue), orgId, baseOrgId, testId);
    return updated > 0;
  }

  /** The Constant REINIT_EXCLUSIONS. */
  private static final String REINIT_EXCLUSIONS = "UPDATE test_org_details as target "
      + " SET applicable = tod.applicable, base_rate_sheet_id = tod.org_id, "
      + " item_code = tod.item_code, code_type = tod.code_type, is_override = 'N' "
      + " FROM test_org_details tod WHERE tod.test_id = target.test_id and "
      + " tod.org_id = ? and target.org_id = ? and target.is_override != 'Y'";

  /**
   * Reinit rate plan.
   *
   * @param newOrgId the new org id
   * @param varianceType the variance type
   * @param variance the variance
   * @param baseOrgId the base org id
   * @param rndOff the rnd off
   * @param userName the user name
   * @param orgName the org name
   * @return true, if successful
   */
  public boolean reinitRatePlan(String newOrgId, String varianceType, Double variance,
      String baseOrgId, Double rndOff, String userName, String orgName) {

    boolean status = true;
    if (!varianceType.equals("Incr")) {
      variance = -variance.doubleValue();
    }
    BigDecimal varianceBy = new BigDecimal(variance);
    BigDecimal roundOff = new BigDecimal(rndOff);

    Object[] updparams = { varianceBy, roundOff, varianceBy, roundOff, newOrgId, baseOrgId };
    status = updateExclusions(REINIT_EXCLUSIONS, newOrgId, baseOrgId, true);
    if (status) {
      status = updateCharges(UPDATE_RATEPLAN_DIAG_CHARGES, updparams);
    }
    return status;
  }

  /** The Constant UPDATE_TIME_STAMP. */
  private static final String UPDATE_TIME_STAMP = 
      "UPDATE master_timestamp set master_count = master_count + ? ";

  /**
   * Update exclusions.
   *
   * @param updateExclusionsQuery the update exclusions query
   * @param orgId the org id
   * @param baseOrgId the base org id
   * @param updateMasterTimestamp the update master timestamp
   * @return true, if successful
   */
  public boolean updateExclusions(String updateExclusionsQuery, String orgId, String baseOrgId,
      boolean updateMasterTimestamp) {

    boolean status = true;
    if (null != updateExclusionsQuery) {
      Integer updated = DatabaseHelper.update(updateExclusionsQuery, baseOrgId, orgId);
      if (updated < 1) {
        status = false;
      }
      if (updated > 0 && updateMasterTimestamp) {
        DatabaseHelper.update(UPDATE_TIME_STAMP, new Object[] { updated });
      }
    }
    return status;
  }

  /**
   * Update charges.
   *
   * @param updateChargesQuery the update charges query
   * @param params the params
   * @return true, if successful
   */
  public boolean updateCharges(String updateChargesQuery, Object[] params) {
    return DatabaseHelper.update(updateChargesQuery, params) > 0;
  }

  /** The Constant UPDATE_EXCLUSIONS. */
  private static final String UPDATE_EXCLUSIONS = "UPDATE test_org_details AS target "
      + " SET item_code = tod.item_code, code_type = tod.code_type,"
      + " applicable = true, base_rate_sheet_id = tod.org_id, is_override = 'N' "
      + " FROM test_org_details tod WHERE tod.test_id = target.test_id and "
      + " tod.org_id = ? and tod.applicable = true and target.org_id = ? and "
      + " target.applicable = false and target.is_override != 'Y'";

  /** The Constant UPDATE_CHARGES. */
  private static final String UPDATE_CHARGES = "UPDATE diagnostic_charges AS target SET "
      + " charge = doroundvarying(tc.charge, ?, ?), "
      + " discount = doroundvarying(tc.discount, ?, ?), " + " priority = tc.priority, "
      + " username = ?, is_override = 'N' " + " FROM diagnostic_charges tc, test_org_details tod "
      + " where tod.org_id = ? and tc.test_id = tod.test_id and tod.base_rate_sheet_id = ? and "
      + " target.test_id = tc.test_id and target.bed_type = tc.bed_type and "
      + " tod.applicable = true and target.is_override != 'Y'"
      + " and tc.org_name = ? and target.org_name = ?";

  /** The Constant INSERT_AUDIT_LOG. */
  private static final String INSERT_AUDIT_LOG = 
      " INSERT INTO diagnostic_charges_audit_log (user_name, operation, field_name, old_value, "
      + " new_value) "
      + " VALUES (?, ?, ?, ?, ?) ";

  /**
   * Update rate plan.
   *
   * @param newOrgId the new org id
   * @param baseOrgId the base org id
   * @param varianceType the variance type
   * @param variance the variance
   * @param rndOff the rnd off
   * @param userName the user name
   * @param orgName the org name
   * @return true, if successful
   */
  public boolean updateRatePlan(String newOrgId, String baseOrgId, String varianceType,
      Double variance, Double rndOff, String userName, String orgName) {

    boolean status = true;
    if (!varianceType.equals("Incr")) {
      variance = -variance.doubleValue();
    }

    BigDecimal varianceBy = new BigDecimal(variance);
    BigDecimal roundOff = new BigDecimal(rndOff);

    Object[] updparams = { varianceBy, roundOff, varianceBy, roundOff, userName, newOrgId,
        baseOrgId, baseOrgId, newOrgId };
    
    status = updateExclusions(UPDATE_EXCLUSIONS, newOrgId, baseOrgId, true);
    if (status) {
      status = updateCharges(UPDATE_CHARGES, updparams);
    }

    DatabaseHelper.insert(INSERT_AUDIT_LOG, userName, "BULK INSERT", "org_id", "", orgName);
    return status;

  }

  @Override
  public BasicDynaBean getCodeDetails(String itemId, String orgId) {
    return DatabaseHelper.queryToDynaBean(GET_ORG_ITEM_CODE, itemId, orgId);
  }
}
