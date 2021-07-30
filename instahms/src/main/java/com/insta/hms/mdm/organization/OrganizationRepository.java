package com.insta.hms.mdm.organization;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * The Class OrganizationRepository.
 *
 * @author Anand Patel.
 */
@Repository
public class OrganizationRepository extends MasterRepository<String> {

  /**
   * Instantiates a new organization repository.
   */
  public OrganizationRepository() {
    super("organization_details", "org_id");
  }

  /**
   * Gets the all base rate plan list.
   *
   * @return the all base rate plan list
   */
  public List<BasicDynaBean> getAllBaseRatePlanList() {
    return DatabaseHelper.queryToDynaList(
        "SELECT org_id,org_name FROM organization_details WHERE status='A' ");
  }

  /** The Constant GET_ACTIVE_ORGID_NAMES_EXCLUDE_ORG. */
  private static final String GET_ACTIVE_ORGID_NAMES_EXCLUDE_ORG =
      " SELECT org_id,org_name "
          + "FROM organization_details  WHERE status='A' AND is_rate_sheet = 'Y' "
          + "AND org_id != ? ORDER BY org_name  ";

  /**
   * Gets the active org id names exclude org.
   *
   * @param orgId the org id
   * @return the active org id names exclude org
   */
  public List<BasicDynaBean> getActiveOrgIdNamesExcludeOrg(String orgId) {
    return DatabaseHelper.queryToDynaList(GET_ACTIVE_ORGID_NAMES_EXCLUDE_ORG, orgId);
  }

  /**
   * Gets the rate sheet for charge.
   *
   * @return List of BasicDynaBean.
   */
  public List<BasicDynaBean> getRateSheetForCharge() {
    return DatabaseHelper.queryToDynaList(
        " SELECT org_id,org_name FROM organization_details "
            + " WHERE status='A' and is_rate_sheet = 'Y' "
            + " order by org_name");
  }

  /** The Constant OP_ALLOWED_RATE_PLANS. */
  private static final String OP_ALLOWED_RATE_PLANS =
      " select org_name,org_id from "
          + " organization_details od "
          + " join (select regexp_split_to_table(op_allowed_rate_plans, E',') "
          + " as rate_plan_id_split"
          + " from patient_category_master where category_id= ? ) as foo "
          + " ON(foo.rate_plan_id_split = od.org_id "
          + " OR foo.rate_plan_id_split = '*') and od.org_id = ? ";

  /**
   * Check for op allowed rate plans.
   *
   * @param objects the objects
   * @return the basic dyna bean
   */
  public BasicDynaBean checkForOpAllowedRatePlans(Object[] objects) {
    return DatabaseHelper.queryToDynaBean(OP_ALLOWED_RATE_PLANS, objects);
  }

  /** The Constant GET_ORG_DETAILS. */
  private static final String GET_ORG_DETAILS =
      " SELECT b.bill_no, od.org_id, od.org_name, od.pharmacy_discount_percentage, "
          + " od.pharmacy_discount_type, od.store_rate_plan_id "
          + " FROM bill b "
          + " LEFT JOIN organization_details od ON (b.bill_rate_plan_id =  od.org_id) "
          + " WHERE b.bill_no = ?";

  /**
   * This method is used to get org details.
   *
   * @param billNo the billNo
   * @return the rate plan detail
   */
  public BasicDynaBean getRatePlanDetail(String billNo) {
    return DatabaseHelper.queryToDynaBean(GET_ORG_DETAILS, billNo);
  }

  /** The Constant GET_VALID_RATE_PLANS. */
  private static final String GET_VALID_RATE_PLANS =
      " SELECT org_name,org_id,status from organization_details "
          + " WHERE status='A' AND ( (has_date_validity AND "
          + " current_date BETWEEN valid_from_date AND valid_to_date ) "
          + " OR (NOT has_date_validity)) "
          + " ORDER BY org_name ";

  /**
   * Gets the valid rate plans.
   *
   * @return the valid rate plans
   */
  public List<BasicDynaBean> getValidRatePlans() {
    return DatabaseHelper.queryToDynaList(GET_VALID_RATE_PLANS);
  }

  /** The Constant RATE_SHEET_NAMES. */
  private static final String RATE_SHEET_NAMES =
      "SELECT org_id,org_name FROM organization_details where is_rate_sheet='Y'";

  /**
   * Gets the rate sheet list.
   *
   * @return the rate sheet list
   * @throws SQLException
   *           the SQL exception
   */
  public List<BasicDynaBean> getRateSheetList() {
    return DatabaseHelper.queryToDynaList(RATE_SHEET_NAMES);
  }

  /** The Constant RATE_SHEET_AND_PLAN_NAMES. */
  private static final String RATE_SHEET_AND_PLAN_NAMES =
      "SELECT org_id, org_name FROM organization_details where status='A' "
      + " order by lower(org_name) ";

  /**
   * Gets the rate sheet and plans list.
   *
   * @return the rate sheet and plans list
   * @throws SQLException
   *           the SQL exception
   */
  public List<BasicDynaBean> getRateSheetAndPlanList() {
    return DatabaseHelper.queryToDynaList(RATE_SHEET_AND_PLAN_NAMES);
  }

  /** The Constant RATE_PLAN_NAMES. */
  private static final String RATE_PLAN_NAMES =
      "SELECT od.org_id, od.org_name, rpp.base_rate_sheet_id FROM organization_details od LEFT JOIN"
          + " rate_plan_parameters rpp using (org_id) ";

  /**
   * Gets the rate plan list.
   *
   * @return the rate plan list
   * @throws SQLException
   *           the SQL exception
   */
  public List<BasicDynaBean> getAllRatePlanList() {
    return DatabaseHelper.queryToDynaList(RATE_PLAN_NAMES);
  }

  /**
   * Gets the rate plan list for the given orgIds.
   *
   * @return the rate plan list for the given orgIds
   * @throws SQLException
   *           the SQL exception
   */
  public List<BasicDynaBean> getRatePlanList(List<String> orgIds) {
    MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
    mapSqlParameterSource.addValue("orgIds", orgIds);
    String query = RATE_PLAN_NAMES + " WHERE od.org_id IN (:orgIds) ";
    return DatabaseHelper.queryToDynaList(query, mapSqlParameterSource);
  }
}
