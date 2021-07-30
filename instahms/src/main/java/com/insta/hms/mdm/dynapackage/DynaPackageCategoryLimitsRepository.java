package com.insta.hms.mdm.dynapackage;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;
import com.insta.hms.common.utils.EnvironmentUtil;

import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

// TODO: Auto-generated Javadoc

/**
 * The Class DynaPackageCategoryLimitsRepository.
 *
 * @author eshwar-chandra
 */
@Repository
public class DynaPackageCategoryLimitsRepository extends GenericRepository {


  /*
   * Copy the GENERAL rate plan charges to all other rate plans. Assumes that the other
   * rate-plan charges are non-existent, ie, no updates only inserts new charges.
   */
  private static final String COPY_GENERAL_CHARGES_TO_ALL_ORGS =
      "INSERT INTO dyna_package_category_limits (org_id, bed_type, dyna_package_id,"
          + " dyna_pkg_cat_id, username,pkg_included, amount_limit, qty_limit) "
          + " SELECT abo.org_id, abo.bed_type, dcl.dyna_package_id, dcl.dyna_pkg_cat_id,"
          + " dcl.username, dcl.pkg_included, dcl.amount_limit, dcl.qty_limit "
          + " FROM all_beds_orgs_view abo "
          + " JOIN dyna_package_category_limits dcl ON (dcl.dyna_package_id=?"
          + " AND dcl.bed_type = abo.bed_type "
          + " AND dcl.org_id = 'ORG0001') "
          + " WHERE abo.org_id != 'ORG0001' ";

  /**
   * The Constant UPDATE_RATEPALN_DYNAPKG_CHARGES_BY.
   */
  private static final String UPDATE_RATEPALN_DYNAPKG_CHARGES_BY =
      "UPDATE dyna_package_category_limits totab SET "
          + " amount_limit = doroundvarying(fromtab.amount_limit,?,?), "
          + " qty_limit = fromtab.qty_limit, "
          + " pkg_included = fromtab.pkg_included "
          + " FROM dyna_package_category_limits fromtab"
          + " WHERE totab.org_id = ? AND fromtab.org_id = ?"
          + " AND totab.dyna_package_id = fromtab.dyna_package_id"
          + " AND totab.dyna_pkg_cat_id = fromtab.dyna_pkg_cat_id "
          + " AND totab.bed_type = fromtab.bed_type AND totab.is_override='N' ";

  /**
   * The Constant UPDATE_RATEPLAN_DYNAPKG_CHARGES_BY.
   */
  private static final String UPDATE_RATEPLAN_DYNAPKG_CHARGES_BY =
      "UPDATE dyna_package_category_limits totab SET "
          + " amount_limit = doroundvarying(fromtab.amount_limit,?,?),"
          + " qty_limit = fromtab.qty_limit, "
          + " pkg_included = fromtab.pkg_included "
          + " FROM dyna_package_category_limits fromtab"
          + " WHERE totab.org_id = ? AND fromtab.org_id = ?"
          + " AND totab.dyna_package_id = fromtab.dyna_package_id"
          + " AND totab.bed_type = fromtab.bed_type "
          + " AND totab.dyna_pkg_cat_id = fromtab.dyna_pkg_cat_id "
          + " AND totab.dyna_package_id=? AND totab.dyna_pkg_cat_id=? "
          + " AND totab.is_override='N' ";

  private static final String INSERT_AUDIT_LOG =
      " INSERT INTO dyna_package_category_limits_audit_log (user_name, operation, field_name,"
          + " dyna_package_id) "
          + " VALUES (?, ?, ?, ?) ";

  /**
   * Instantiates a new dyna package category limits repository.
   */
  public DynaPackageCategoryLimitsRepository() {
    super("dyna_package_category_limits");
  }

  /**
   * Copy general charges to all orgs.
   *
   * @param dynaPackageId the dyna package id
   * @param userName      userName
   * @return the integer
   */
  public Integer copyGeneralChargesToAllOrgs(int dynaPackageId, String userName) {

    DatabaseHelper.disableTrigger("z_dyna_package_category_limits_audit_trigger",
        "dyna_package_category_limits");
    int rows = DatabaseHelper
        .insert(COPY_GENERAL_CHARGES_TO_ALL_ORGS, EnvironmentUtil.getDatabaseQueryTimeout() * 3,
            dynaPackageId);
    DatabaseHelper.enableTrigger("z_dyna_package_category_limits_audit_trigger",
        "dyna_package_category_limits");
    DatabaseHelper.insert(INSERT_AUDIT_LOG,
        userName, "BULK INSERT", "ALL LIMITS", dynaPackageId);
    return rows;

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
    boolean success = false;
    success =
        DatabaseHelper.update(UPDATE_RATEPALN_DYNAPKG_CHARGES_BY
                + " AND totab.dyna_package_id = ? ",
            new BigDecimal(varianceBy), new BigDecimal(nearstRoundOfValue), orgId,
            baseOrgId, Integer.parseInt(dynaPackageId)) > 0;
    return success;
  }

  /**
   * Update category limits for derived rate plans.
   *
   * @param values the values
   * @return the integer
   */
  public Integer updateCategoryLimitsForDerivedRatePlans(Object[] values) {
    return DatabaseHelper.update(UPDATE_RATEPLAN_DYNAPKG_CHARGES_BY, values);
  }

}
