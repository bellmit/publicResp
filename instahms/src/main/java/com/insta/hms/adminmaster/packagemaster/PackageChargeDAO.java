package com.insta.hms.adminmaster.packagemaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.master.rateplan.ItemChargeDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


// TODO: Auto-generated Javadoc
/*
 * Rate plan related changes. The PackageDAO does not inherit from GenericDAO, hence
 * created a new class which can inherit from ItemChargeDAO without any side-effects
 * for the existing code.
 */

/**
 * The Class PackageChargeDAO.
 */
public class PackageChargeDAO extends ItemChargeDAO {

  /**
   * Instantiates a new package charge DAO.
   */
  public PackageChargeDAO() {
    super("package_charges");
  }

  /**
   * Adds the org codes for items.
   *
   * @param con the con
   * @param newOrgId the new org id
   * @param baseOrgId the base org id
   * @param userName the user name
   * @return true, if successful
   * @throws Exception the exception
   */
  /* Rate Plan Changes -- begin */
  public static boolean addOrgCodesForItems(Connection con, String newOrgId, String baseOrgId,
      String userName) throws Exception {
    return PackageDAO.addOrgCodesForPackages(con, newOrgId, null, null, null, false, baseOrgId,
        null);
  }

  /** The init org details. */
  private static String INIT_ORG_DETAILS = "INSERT INTO pack_org_details "
      + "(package_id, org_id, applicable, item_code, code_type, base_rate_sheet_id, is_override)"
      + " SELECT package_id, ?, false, null, null, null, 'N'" + " FROM packages";

  /** The init charges. */
  private static String INIT_CHARGES = "INSERT INTO package_charges(package_id,org_id,bed_type,"
      + "charge)" + "(SELECT package_id, ?, abov.bed_type, 0.0"
      + "FROM packages CROSS JOIN all_beds_orgs_view abov WHERE abov.org_id =? ) ";

  /** The Constant INSERT_CHARGES. */
  private static final String INSERT_CHARGES =
      "INSERT INTO package_charges(package_id,org_id,bed_type,"
      + " charge, is_override)" + " SELECT pc.package_id, ?, pc.bed_type, "
      + " doroundvarying(pc.charge,?,?), " + " 'N' "
      + " FROM package_charges pc, pack_org_details pod, pack_org_details podtarget  "
      + " where pc.org_id = pod.org_id and pc.package_id = pod.package_id "
      + " and podtarget.org_id = ? and podtarget.package_id = pod.package_id "
      + " and podtarget.base_rate_sheet_id = ? and pod.applicable = true " + " and pc.org_id = ? ";

  /** The Constant UPDATE_CHARGES. */
  private static final String UPDATE_CHARGES = "UPDATE package_charges AS target SET "
      + " charge = doroundvarying(pc.charge,?,?), "
      + " discount = doroundvarying(pc.discount,?,?), " + " is_override = 'N' "
      + " FROM package_charges pc, pack_org_details pod  "
      + " where pod.org_id = ? and pc.package_id = pod.package_id "
      + " and pod.base_rate_sheet_id = ? and target.package_id = pc.package_id "
      + " and target.bed_type = pc.bed_type and pod.applicable = true and target.is_override != 'Y'"
      + " and pc.org_id = ? and target.org_id = ?";

  /** The Constant UPDATE_EXCLUSIONS. */
  private static final String UPDATE_EXCLUSIONS = "UPDATE pack_org_details AS target "
      + " SET item_code = pod.item_code, code_type = pod.code_type,"
      + " applicable = true, base_rate_sheet_id = pod.org_id, is_override = 'N' "
      + " FROM pack_org_details pod WHERE pod.package_id = target.package_id and "
      + " pod.org_id = ? and pod.applicable = true and target.org_id = ? "
      + " and target.applicable = false and target.is_override != 'Y'";

  /**
   * Update rate plan.
   *
   * @param con the con
   * @param newOrgId the new org id
   * @param baseOrgId the base org id
   * @param varianceType the variance type
   * @param variance the variance
   * @param rndOff the rnd off
   * @param userName the user name
   * @param orgName the org name
   * @return true, if successful
   * @throws Exception the exception
   */
  public boolean updateRatePlan(Connection con, String newOrgId, String baseOrgId,
      String varianceType, Double variance, Double rndOff, String userName, String orgName)
      throws Exception {

    boolean status = false;
    // No audit logs for package master as of now.

    if (!varianceType.equals("Incr")) {
      variance = new Double(-variance);
    }

    BigDecimal varianceBy = new BigDecimal(variance);
    BigDecimal roundOff = new BigDecimal(rndOff);

    Object[] updparams = {
        varianceBy, roundOff, varianceBy, roundOff, newOrgId, baseOrgId,
        baseOrgId, newOrgId };
    status = updateExclusions(con, UPDATE_EXCLUSIONS, newOrgId, baseOrgId, true);
    if (status) {
      status = updateCharges(con, UPDATE_CHARGES, updparams);
    }

    return status;
  }

  /**
   * Inits the rate plan.
   *
   * @param con the con
   * @param newOrgId the new org id
   * @param varianceType the variance type
   * @param varianceBy the variance by
   * @param baseOrgId the base org id
   * @param roundOff the round off
   * @param userName the user name
   * @param orgName the org name
   * @return true, if successful
   * @throws Exception the exception
   */
  public boolean initRatePlan(Connection con, String newOrgId, String varianceType,
      Double varianceBy, String baseOrgId, Double roundOff, String userName, String orgName)
      throws Exception {
    boolean status = addOrgCodesForItems(con, newOrgId, baseOrgId, userName);
    if (status) {
      status = PackageDAO.addOrgForPackages(con, newOrgId, varianceType, 0.0, varianceBy, false,
          baseOrgId, roundOff, true);
    }
    return status;
  }

  /** The Constant REINIT_EXCLUSIONS. */
  private static final String REINIT_EXCLUSIONS = "UPDATE pack_org_details as target "
      + " SET applicable = pod.applicable, base_rate_sheet_id = pod.org_id, "
      + " item_code = pod.item_code, code_type = pod.code_type, is_override = 'N' "
      + " FROM pack_org_details pod WHERE pod.package_id = target.package_id and "
      + " pod.org_id = ? and target.org_id = ? and target.is_override != 'Y'";

  /**
   * Reinit rate plan.
   *
   * @param con the con
   * @param newOrgId the new org id
   * @param varianceType the variance type
   * @param variance the variance
   * @param baseOrgId the base org id
   * @param rndOff the rnd off
   * @param userName the user name
   * @param orgName the org name
   * @return true, if successful
   * @throws Exception the exception
   */
  public boolean reinitRatePlan(Connection con, String newOrgId, String varianceType,
      Double variance, String baseOrgId, Double rndOff, String userName, String orgName)
      throws Exception {
    boolean status = false;
    if (!varianceType.equals("Incr")) {
      variance = new Double(-variance);
    }
    BigDecimal varianceBy = new BigDecimal(variance);
    BigDecimal roundOff = new BigDecimal(rndOff);

    Object[] updparams = { varianceBy, roundOff, varianceBy, roundOff, newOrgId, baseOrgId };
    status = updateExclusions(con, REINIT_EXCLUSIONS, newOrgId, baseOrgId, true);
    if (status) {
      status = updateCharges(con, UPDATE_RATEPLAN_PACKAGE_CHARGES, updparams);
    }
    return status;
  }

  /** The init item org details. */
  private static String INIT_ITEM_ORG_DETAILS = "INSERT INTO pack_org_details "
      + "(package_id, org_id, applicable, item_code, code_type, base_rate_sheet_id, is_override)"
      + " ( SELECT ?, od.org_id, false, null, null, prspv.base_rate_sheet_id, "
      + " 'N' FROM organization_details od "
      + " LEFT OUTER JOIN priority_rate_sheet_parameters_view prspv ON od.org_id = prspv.org_id )";

  /** The init item charges. */
  private static String INIT_ITEM_CHARGES =
      "INSERT INTO package_charges(package_id,org_id,bed_type,"
      + "charge)" + "(SELECT ?, abov.org_id, abov.bed_type, 0.0, ? FROM all_beds_orgs_view abov) ";

  /**
   * Inits the item charges.
   *
   * @param con the con
   * @param packageId the package id
   * @param userName the user name
   * @return true, if successful
   * @throws Exception the exception
   */
  public boolean initItemCharges(Connection con, String packageId, String userName)
      throws Exception {

    boolean status = false;
    status = initItemCharges(con, INIT_ITEM_ORG_DETAILS, INIT_ITEM_CHARGES, packageId, null);
    // postAuditEntry(con, "service_master_charges_audit_log", userName, orgName);

    return status;
  }

  /**
   * Update org for derived rate plans.
   *
   * @param con the con
   * @param ratePlanIds the rate plan ids
   * @param applicable the applicable
   * @param packageId the package id
   * @return true, if successful
   * @throws Exception the exception
   */
  public boolean updateOrgForDerivedRatePlans(Connection con, String[] ratePlanIds,
      String[] applicable, String packageId) throws Exception {
    return updateOrgForDerivedRatePlans(con, ratePlanIds, applicable, "pack_org_details",
        "packages", "package_id", packageId);
  }

  /**
   * Update charges for derived rate plans.
   *
   * @param con the con
   * @param baseRateSheetId the base rate sheet id
   * @param ratePlanIds the rate plan ids
   * @param bedType the bed type
   * @param regularcharges the regularcharges
   * @param packageId the package id
   * @param discounts the discounts
   * @param applicable the applicable
   * @return true, if successful
   * @throws Exception the exception
   */
  public boolean updateChargesForDerivedRatePlans(Connection con, String baseRateSheetId,
      String[] ratePlanIds, String[] bedType, Double[] regularcharges, String packageId,
      Double[] discounts, String[] applicable) throws Exception {

    return updateChargesForDerivedRatePlans(con, baseRateSheetId, ratePlanIds, bedType,
        regularcharges, "package_charges", "pack_org_details", "packages", "package_id", packageId,
        discounts, applicable);
  }

  /** The Constant GET_DERIVED_RATE_PALN_DETAILS. */
  private static final String GET_DERIVED_RATE_PALN_DETAILS = "select rp.org_id,od.org_name, "
      + " case when rate_variation_percent<0 then 'Decrease By' "
      + " else 'Increase By' end as discormarkup, rate_variation_percent,"
      + " round_off_amount,pod.applicable,pod.package_id,rp.base_rate_sheet_id, pod.is_override "
      + " from rate_plan_parameters rp " + " join organization_details od on(od.org_id=rp.org_id) "
      + " join pack_org_details pod on (pod.org_id = rp.org_id) "
      + " where rp.base_rate_sheet_id =?  and package_id=?  and pod.base_rate_sheet_id=? ";

  /**
   * Gets the derived rate plan details.
   *
   * @param baseRateSheetId the base rate sheet id
   * @param packageId the package id
   * @return the derived rate plan details
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getDerivedRatePlanDetails(String baseRateSheetId, String packageId)
      throws SQLException {
    return getDerivedRatePlanDetails(baseRateSheetId, "packages", packageId,
        GET_DERIVED_RATE_PALN_DETAILS);
  }

  /** The Constant UPDATE_RATEPLAN_PACKAGE_CHARGES. */
  private static final String UPDATE_RATEPLAN_PACKAGE_CHARGES = "UPDATE package_charges totab SET "
      + " charge = doroundvarying(fromtab.charge,?,?), "
      + " discount = doroundvarying(fromtab.discount,?,?) " + " FROM package_charges fromtab"
      + " WHERE totab.org_id = ? AND fromtab.org_id = ?"
      + " AND totab.package_id = fromtab.package_id "
      + " AND totab.bed_type = fromtab.bed_type AND totab.is_override='N'";

  /**
   * Update charges based on new rate sheet.
   *
   * @param con the con
   * @param orgId the org id
   * @param varianceBy the variance by
   * @param baseOrgId the base org id
   * @param nearstRoundOfValue the nearst round of value
   * @param packageId the package id
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws Exception the exception
   */
  public static boolean updateChargesBasedOnNewRateSheet(Connection con, String orgId,
      Double varianceBy, String baseOrgId, Double nearstRoundOfValue, String packageId)
      throws SQLException, Exception {
    boolean success = false;
    PreparedStatement ps = null;
    try {
      String updateRatePlanPack = UPDATE_RATEPLAN_PACKAGE_CHARGES + " AND totab.package_id = ? ";
      ps = con.prepareStatement(updateRatePlanPack);
      ps.setBigDecimal(1, new BigDecimal(varianceBy));
      ps.setBigDecimal(2, new BigDecimal(nearstRoundOfValue));
      ps.setBigDecimal(3, new BigDecimal(varianceBy));
      ps.setBigDecimal(4, new BigDecimal(nearstRoundOfValue));
      ps.setString(5, orgId);
      ps.setString(6, baseOrgId);
      ps.setInt(7, Integer.parseInt(packageId));

      int intValue = ps.executeUpdate();
      if (intValue >= 0) {
        success = true;
      }
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
    return success;
  }

  /**
   * Update bed for rate plan.
   *
   * @param con the con
   * @param ratePlanId the rate plan id
   * @param variance the variance
   * @param rateSheetId the rate sheet id
   * @param rndOff the rnd off
   * @param bedType the bed type
   * @return true, if successful
   * @throws Exception the exception
   */
  public boolean updateBedForRatePlan(Connection con, String ratePlanId, Double variance,
      String rateSheetId, Double rndOff, String bedType) throws Exception {

    boolean status = false;

    BigDecimal varianceBy = new BigDecimal(variance);
    BigDecimal roundOff = new BigDecimal(rndOff);

    Object[] updparams = { varianceBy, roundOff, varianceBy, roundOff, ratePlanId, rateSheetId,
        bedType };
    status = updateCharges(con, UPDATE_RATEPLAN_PACKAGE_CHARGES + " AND totab.bed_type=? ",
        updparams);
    return status;
  }

  /** The package item charges. */
  private static String PACKAGE_ITEM_CHARGES =
      "INSERT INTO package_item_charges(package_id,pack_ob_id,org_id,bed_type,"
      + "charge) (SELECT ?, ?, abov.org_id, abov.bed_type, ? FROM all_active_beds_orgs_view abov) ";

  /**
   * Insert package item charges.
   *
   * @param con the con
   * @param packageId the package id
   * @param packObId the pack ob id
   * @param charge the charge
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws Exception the exception
   */
  public boolean insertPackageItemCharges(Connection con, int packageId, String packObId,
      BigDecimal charge) throws SQLException, Exception {
    boolean success = false;
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(PACKAGE_ITEM_CHARGES);
      ps.setInt(1, packageId);
      ps.setString(2, packObId);
      ps.setBigDecimal(3, charge);
      int intValue = ps.executeUpdate();
      if (intValue >= 0) {
        success = true;
      }
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
    return success;
  }

  /** The Constant UPDATE_RATEPLAN_PKG_ITEM_CHARGES. */
  private static final String UPDATE_RATEPLAN_PKG_ITEM_CHARGES =
      "UPDATE package_item_charges totab SET "
      + " charge = doroundvarying(fromtab.charge,?,?) " + " FROM package_item_charges fromtab"
      + " WHERE totab.org_id = ? AND fromtab.org_id = ?"
      + " AND totab.package_id = fromtab.package_id AND totab.pack_ob_id = fromtab.pack_ob_id "
      + " AND totab.bed_type = fromtab.bed_type "
      + " AND totab.is_override='N'";

  /**
   * Update package item charges for rateplans.
   *
   * @param con the con
   * @param packageId the package id
   * @param packObId the pack ob id
   * @param baseRateSheetId the base rate sheet id
   * @param ratePlanId the rate plan id
   * @param varianceBy the variance by
   * @param nearstRoundOfValue the nearst round of value
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws Exception the exception
   */
  public boolean updatePackageItemChargesForRateplans(Connection con, int packageId,
      String packObId, String baseRateSheetId, String ratePlanId, Double varianceBy,
      Double nearstRoundOfValue) throws SQLException, Exception {
    boolean success = false;
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(
          UPDATE_RATEPLAN_PKG_ITEM_CHARGES + " AND totab.package_id = ?  AND totab.pack_ob_id = ?");
      ps.setBigDecimal(1, new BigDecimal(varianceBy));
      ps.setBigDecimal(2, new BigDecimal(nearstRoundOfValue));
      ps.setString(3, ratePlanId);
      ps.setString(4, baseRateSheetId);
      ps.setInt(5, packageId);
      ps.setString(6, packObId);
      int intValue = ps.executeUpdate();
      if (intValue >= 0) {
        success = true;
      }
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
    return success;
  }

  /** The Constant GET_PKG_ITEM_CHARGES. */
  private static final String GET_PKG_ITEM_CHARGES =
      " SELECT pic.package_id, pic.pack_ob_id, bed_type,charge,activity_description,"
      + " activity_qty " + " FROM package_item_charges pic "
      + " JOIN package_contents pcd ON(pic.pack_ob_id = pcd.pack_ob_id) "
      + " WHERE pic.package_id=? and pic.org_id=? ";

  /**
   * Gets the package item charges for org.
   *
   * @param packageId the package id
   * @param ratePlanId the rate plan id
   * @return the package item charges for org
   * @throws SQLException the SQL exception
   * @throws Exception the exception
   */
  public List<BasicDynaBean> getPackageItemChargesForOrg(String packageId, String ratePlanId)
      throws SQLException, Exception {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_PKG_ITEM_CHARGES);
      ps.setInt(1, Integer.parseInt(packageId));
      ps.setString(2, ratePlanId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_PACKAGE_ITEM_LIST. */
  private static final String GET_PACKAGE_ITEM_LIST =
      "SELECT pack_ob_id,activity_description,package_id,activity_qty "
      + " FROM package_contents WHERE package_id=?";

  /**
   * Gets the package items list.
   *
   * @param packageId the package id
   * @return the package items list
   * @throws SQLException the SQL exception
   * @throws Exception the exception
   */
  public List<BasicDynaBean> getPackageItemsList(String packageId) throws SQLException, Exception {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_PACKAGE_ITEM_LIST);
      ps.setInt(1, Integer.parseInt(packageId));
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Update pkg item charges for derived rate plans.
   *
   * @param con the con
   * @param baseRateSheetId the base rate sheet id
   * @param ratePlanIds the rate plan ids
   * @param bedType the bed type
   * @param regularcharges the regularcharges
   * @param packageId the package id
   * @param applicable the applicable
   * @param packObIds the pack ob ids
   * @return true, if successful
   * @throws Exception the exception
   */
  public boolean updatePkgItemChargesForDerivedRatePlans(Connection con, String baseRateSheetId,
      String[] ratePlanIds, String[] bedType, Double[] regularcharges, String packageId,
      String[] applicable, String[] packObIds) throws Exception {
    boolean success = false;
    GenericDAO cdao = new GenericDAO("package_item_charges");
    for (int i = 0; i < ratePlanIds.length; i++) {
      Map<String, Object> keys = new HashMap<String, Object>();
      keys.put("base_rate_sheet_id", baseRateSheetId);
      keys.put("org_id", ratePlanIds[i]);
      BasicDynaBean bean = new GenericDAO("rate_plan_parameters").findByKey(keys);
      int variation = (Integer) bean.get("rate_variation_percent");
      int roundoff = (Integer) bean.get("round_off_amount");
      List<BasicDynaBean> chargeList = new ArrayList();
      boolean overrided = isChargeOverrided(con, ratePlanIds[i], "package_id", packageId,
          "packages", "pack_org_details");

      if (!overrided) {
        for (int k = 0; k < bedType.length; k++) {
          BasicDynaBean charge = cdao.getBean();
          charge.set("package_id", Integer.parseInt(packageId));
          charge.set("pack_ob_id", packObIds[k]);
          charge.set("org_id", ratePlanIds[i]);
          charge.set("bed_type", bedType[k]);
          Double rpCharge = calculateCharge(regularcharges[k], new Double(variation), roundoff);
          charge.set("charge", new BigDecimal(rpCharge));
          chargeList.add(charge);
        }
      }

      for (BasicDynaBean c : chargeList) {
        cdao.updateWithNames(con, c.getMap(),
            new String[] { "package_id", "pack_ob_id", "org_id", "bed_type" });
      }
      success = true;

      boolean overrideItemCharges = checkItemStatus(con, ratePlanIds[i], "pack_org_details",
          "packages", "package_id", packageId, applicable[i]);
      if (overrideItemCharges) {
        BasicDynaBean chargeBean = cdao.getBean();
        chargeBean.set("is_override", "Y");
        Map<String, Object> chKeys = new HashMap<String, Object>();
        chKeys.put("org_id", ratePlanIds[i]);
        chKeys.put("package_id", Integer.parseInt(packageId));
        success = cdao.update(con, chargeBean.getMap(), chKeys) > 0;
      }
    }
    return success;
  }

  /**
   * Update pkg item charges.
   *
   * @param con the con
   * @param ratePlanId the rate plan id
   * @param orgId the org id
   * @param packageId the package id
   * @param rdao the rdao
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws Exception the exception
   */
  public boolean updatePkgItemCharges(Connection con, String ratePlanId, String orgId,
      String packageId, GenericDAO rdao) throws SQLException, Exception {
    String rateSheet = null;
    rateSheet = getOtherRatesheetId(con, ratePlanId, orgId, "pack_org_details", "package_id",
        packageId, "packages");
    if (rateSheet == null) {
      List<BasicDynaBean> raetSheetList = getRateSheetsByPriority(con, ratePlanId, rdao);
      BasicDynaBean prBean = raetSheetList.get(0);
      rateSheet = (String) prBean.get("base_rate_sheet_id");
    }
    Map<String, Object> rateKeys = new HashMap<String, Object>();
    rateKeys.put("org_id", ratePlanId);
    rateKeys.put("base_rate_sheet_id", rateSheet);
    BasicDynaBean beanRP = rdao.findByKey(con, rateKeys);
    boolean success = true;
    Double varianceBy = new Double((Integer) beanRP.get("rate_variation_percent"));
    Double nearstRoundOfValue = new Double((Integer) beanRP.get("round_off_amount"));
    success = new PackageChargeDAO().updatePkgItemChargesBasedOnNewRateSheet(con, ratePlanId,
        varianceBy, rateSheet, nearstRoundOfValue, packageId);

    return success;
  }

  /** The Constant UPDATE_RATEPLAN_PACKAGE_ITEM_CHARGES. */
  private static final String UPDATE_RATEPLAN_PACKAGE_ITEM_CHARGES =
      "UPDATE package_item_charges totab SET "
      + " charge = doroundvarying(fromtab.charge,?,?) " + " FROM package_item_charges fromtab"
      + " WHERE totab.org_id = ? AND fromtab.org_id = ?"
      + " AND totab.package_id = fromtab.package_id AND totab.bed_type = fromtab.bed_type AND "
      + " totab.pack_ob_id = fromtab.pack_ob_id AND totab.is_override='N'";

  /**
   * Update pkg item charges based on new rate sheet.
   *
   * @param con the con
   * @param orgId the org id
   * @param varianceBy the variance by
   * @param baseOrgId the base org id
   * @param nearstRoundOfValue the nearst round of value
   * @param packageId the package id
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws Exception the exception
   */
  public boolean updatePkgItemChargesBasedOnNewRateSheet(Connection con, String orgId,
      Double varianceBy, String baseOrgId, Double nearstRoundOfValue, String packageId)
      throws SQLException, Exception {

    boolean success = false;
    PreparedStatement ps = null;
    try {
      ps = con
          .prepareStatement(UPDATE_RATEPLAN_PACKAGE_ITEM_CHARGES + " AND totab.package_id = ? ");
      ps.setBigDecimal(1, new BigDecimal(varianceBy));
      ps.setBigDecimal(2, new BigDecimal(nearstRoundOfValue));
      ps.setString(3, orgId);
      ps.setString(4, baseOrgId);
      ps.setInt(5, Integer.parseInt(packageId));

      int intValue = ps.executeUpdate();
      if (intValue >= 0) {
        success = true;
      }
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
    return success;

  }

  /* Rate Plan Changes - End */

}
