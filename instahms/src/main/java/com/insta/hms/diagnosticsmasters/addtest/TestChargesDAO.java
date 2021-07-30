package com.insta.hms.diagnosticsmasters.addtest;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.auditlog.AuditLogDao;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.master.rateplan.ItemChargeDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class TestChargesDAO.
 */
public class TestChargesDAO extends ItemChargeDAO {
  
  /**
   * Instantiates a new test charges DAO.
   */
  public TestChargesDAO() {
    super("diagnostic_charges");
  }

  // Rate Plan Changes - Begin

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
  public static boolean addOrgCodesForItems(Connection con, String newOrgId, String baseOrgId,
      String userName) throws Exception {
    return AddTestDAOImpl.addOrgCodesForTests(con, newOrgId, null, null, null, false, baseOrgId,
        null, userName);
  }

  /**
   * Adds the org for tests.
   *
   * @param con the con
   * @param newOrgId the new org id
   * @param varianceType the variance type
   * @param varianceValue the variance value
   * @param varianceBy the variance by
   * @param useValue the use value
   * @param baseOrgId the base org id
   * @param nearstRoundOfValue the nearst round of value
   * @param userName the user name
   * @param orgName the org name
   * @param updateDiscounts the update discounts
   * @return true, if successful
   * @throws Exception the exception
   */
  public static boolean addOrgForTests(Connection con, String newOrgId, String varianceType,
      Double varianceValue, Double varianceBy, boolean useValue, String baseOrgId,
      Double nearstRoundOfValue, String userName, String orgName, boolean updateDiscounts)
      throws Exception {
    return AddTestDAOImpl.addOrgForTests(con, newOrgId, varianceType, varianceValue, varianceBy,
        useValue, baseOrgId, nearstRoundOfValue, userName, orgName, updateDiscounts);
  }

  /** The init org details. */
  private static String INIT_ORG_DETAILS = "INSERT INTO test_org_details "
      + " (test_id, org_id, applicable, item_code, code_type, base_rate_sheet_id, is_override) "
      + " SELECT test_id, ?, false, null, null, null, 'N'" + " FROM diagnostics";

  /** The init charges. */
  private static String INIT_CHARGES = "INSERT INTO diagnostic_charges(test_id,org_name,bed_type,"
      + "charge,priority,username)" + "(SELECT test_id, ?, abov.bed_type, 0.0, 'R', ? "
      + "FROM diagnostics d CROSS JOIN all_beds_orgs_view abov WHERE abov.org_id =? ) ";

  /** The Constant INSERT_CHARGES. */
  private static final String INSERT_CHARGES = "INSERT INTO diagnostic_charges"
      + " (test_id,org_name,bed_type,"
      + " charge,priority,username, is_override) " + " SELECT tc.test_id, ?, tc.bed_type, "
      + " doroundvarying(tc.charge, ?, ?), " + " tc.priority, " + " ?, 'N' "
      + " FROM diagnostic_charges tc, test_org_details tod, test_org_details todtarget "
      + " where tc.org_name = tod.org_id and tc.test_id = tod.test_id "
      + " and todtarget.org_id = ? and todtarget.test_id = tod.test_id and"
      + "  todtarget.base_rate_sheet_id = ? "
      + " and tod.applicable = true " + " and tc.org_name = ? ";

  /** The Constant UPDATE_CHARGES. */
  private static final String UPDATE_CHARGES = "UPDATE diagnostic_charges AS target SET "
      + " charge = doroundvarying(tc.charge, ?, ?), "
      + " discount = doroundvarying(tc.discount, ?, ?), " + " priority = tc.priority, "
      + " username = ?, is_override = 'N' " + " FROM diagnostic_charges tc, test_org_details tod "
      + " where tod.org_id = ? and tc.test_id = tod.test_id and tod.base_rate_sheet_id = ? and "
      + " target.test_id = tc.test_id and target.bed_type = tc.bed_type and "
      + " tod.applicable = true and target.is_override != 'Y'"
      + " and tc.org_name = ? and target.org_name = ?";

  /** The Constant UPDATE_EXCLUSIONS. */
  private static final String UPDATE_EXCLUSIONS = "UPDATE test_org_details AS target "
      + " SET item_code = tod.item_code, code_type = tod.code_type,"
      + " applicable = true, base_rate_sheet_id = tod.org_id, is_override = 'N' "
      + " FROM test_org_details tod WHERE tod.test_id = target.test_id and "
      + " tod.org_id = ? and tod.applicable = true and target.org_id = ? and target.applicable ="
      + "  false and target.is_override != 'Y'";

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
    if (!varianceType.equals("Incr")) {
      variance = new Double(-variance);
    }

    BigDecimal varianceBy = new BigDecimal(variance);
    BigDecimal roundOff = new BigDecimal(rndOff);

    Object[] updparams = { varianceBy, roundOff, varianceBy, roundOff, userName, newOrgId,
        baseOrgId, baseOrgId, newOrgId };
    Object[] insparams = { newOrgId, varianceBy, roundOff, userName, newOrgId, baseOrgId,
        baseOrgId };
    status = updateExclusions(con, UPDATE_EXCLUSIONS, newOrgId, baseOrgId, true);
    if (status) {
      status = updateCharges(con, UPDATE_CHARGES, updparams);
    }

    postAuditEntry(con, "diagnostic_charges_audit_log", userName, orgName);

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
      status = addOrgForTests(con, newOrgId, varianceType, 0.0, varianceBy, false, baseOrgId,
          roundOff, userName, orgName, true);
    }
    postAuditEntry(con, "diagnostic_charges_audit_log", userName, orgName);
    return status;
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
      status = updateCharges(con, UPDATE_RATEPLAN_DIAG_CHARGES, updparams);
    }
    return status;
  }

  /** The init item org details. */
  private static String INIT_ITEM_ORG_DETAILS = "INSERT INTO test_org_details "
      + "(test_id, org_id, applicable, item_code, code_type, base_rate_sheet_id, is_override)"
      + "(SELECT ?, od.org_id, false, null, null, prspv.base_rate_sheet_id, 'N'"
      + "  FROM organization_details od "
      + " LEFT OUTER JOIN priority_rate_sheet_parameters_view prspv ON od.org_id = prspv.org_id )";

  /** The init item charges. */
  private static String INIT_ITEM_CHARGES = "INSERT INTO diagnostic_charges"
      + " (test_id,org_name,bed_type, charge, username, priority)"
      + "(SELECT ?, abov.org_id, abov.bed_type, 0.0, ?, 'R' FROM all_beds_orgs_view abov) ";

  /**
   * Inits the item charges.
   *
   * @param con the con
   * @param serviceId the service id
   * @param userName the user name
   * @return true, if successful
   * @throws Exception the exception
   */
  public boolean initItemCharges(Connection con, String serviceId, String userName)
      throws Exception {

    boolean status = false;
    // disableAuditTriggers("service_master_charges", "z_services_charges_audit_trigger");
    status = initItemCharges(con, INIT_ITEM_ORG_DETAILS, INIT_ITEM_CHARGES, serviceId, userName);
    // postAuditEntry(con, "service_master_charges_audit_log", userName, orgName);
    return status;
  }

  /**
   * Update org for derived rate plans.
   *
   * @param con the con
   * @param ratePlanIds the rate plan ids
   * @param applicable the applicable
   * @param testId the test id
   * @return true, if successful
   * @throws Exception the exception
   */
  public boolean updateOrgForDerivedRatePlans(Connection con, String[] ratePlanIds,
      String[] applicable, String testId) throws Exception {
    return updateOrgForDerivedRatePlans(con, ratePlanIds, applicable, "test_org_details",
        "diagnostics", "test_id", testId);
  }

  /**
   * Update charges for derived rate plans.
   *
   * @param con the con
   * @param baseRateSheetId the base rate sheet id
   * @param ratePlanIds the rate plan ids
   * @param bedType the bed type
   * @param regularcharges the regularcharges
   * @param testId the test id
   * @param discounts the discounts
   * @param applicable the applicable
   * @return true, if successful
   * @throws Exception the exception
   */
  public boolean updateChargesForDerivedRatePlans(Connection con, String baseRateSheetId,
      String[] ratePlanIds, String[] bedType, Double[] regularcharges, String testId,
      Double[] discounts, String[] applicable) throws Exception {

    return updateChargesForDerivedRatePlans(con, baseRateSheetId, ratePlanIds, bedType,
        regularcharges, "diagnostic_charges", "test_org_details", "diagnostics", "test_id", testId,
        discounts, applicable);
  }

  /** The Constant GET_DERIVED_RATE_PALN_DETAILS. */
  private static final String GET_DERIVED_RATE_PALN_DETAILS = "select rp.org_id,od.org_name, "
      + " case when rate_variation_percent<0 then 'Decrease By' else 'Increase By' "
      + " end as discormarkup, "
      + " rate_variation_percent,round_off_amount,tod.applicable,tod.test_id,"
      + " rp.base_rate_sheet_id,tod.is_override "
      + " from rate_plan_parameters rp " + " join organization_details od on(od.org_id=rp.org_id) "
      + " join test_org_details tod on (tod.org_id = rp.org_id) "
      + " where rp.base_rate_sheet_id =?  and test_id=?  and tod.base_rate_sheet_id=? ";

  /**
   * Gets the derived rate plan details.
   *
   * @param baseRateSheetId the base rate sheet id
   * @param testId the test id
   * @return the derived rate plan details
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getDerivedRatePlanDetails(String baseRateSheetId, String testId)
      throws SQLException {
    return getDerivedRatePlanDetails(baseRateSheetId, "diagnostics", testId,
        GET_DERIVED_RATE_PALN_DETAILS);
  }

  /** The Constant UPDATE_RATEPLAN_DIAG_CHARGES. */
  private static final String UPDATE_RATEPLAN_DIAG_CHARGES = "UPDATE diagnostic_charges totab SET "
      + " charge = doroundvarying(fromtab.charge,?,?),"
      + "  discount = doroundvarying(fromtab.discount,?,?) "
      + " FROM diagnostic_charges fromtab" + " WHERE totab.org_name = ? AND fromtab.org_name = ?"
      + " AND totab.test_id = fromtab.test_id AND totab.bed_type = fromtab.bed_type"
      + "  AND totab.is_override='N'";

  /**
   * Update test charges for derived rate plans.
   *
   * @param orgId the org id
   * @param varianceType the variance type
   * @param varianceValue the variance value
   * @param varianceBy the variance by
   * @param baseOrgId the base org id
   * @param nearstRoundOfValue the nearst round of value
   * @param userName the user name
   * @param orgName the org name
   * @param upload the upload
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws Exception the exception
   */
  public boolean updateTestChargesForDerivedRatePlans(String orgId, String varianceType,
      Double varianceValue, Double varianceBy, String baseOrgId, Double nearstRoundOfValue,
      String userName, String orgName, boolean upload) throws SQLException, Exception {

    boolean success = false;
    Connection con = null;
    GenericDAO.alterTrigger("DISABLE", "diagnostic_charges",
        "z_diagnostictest_charges_audit_trigger");

    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);

      if (upload) {
        GenericDAO rateParameterDao = new GenericDAO("rate_plan_parameters");
        List<BasicDynaBean> rateSheetList = getRateSheetsByPriority(con, orgId, rateParameterDao);
        for (int i = 0; i < rateSheetList.size(); i++) {
          success = false;
          BasicDynaBean currentSheet = rateSheetList.get(i);
          Integer variation = (Integer) currentSheet.get("rate_variation_percent");
          String varType = (variation >= 0) ? "Incr" : "Decr";
          Double varBy = new Double((variation >= 0 ? variation : -variation));
          Double roundOff = new Double((Integer) currentSheet.get("round_off_amount"));
          if (i == 0) {
            success = reinitRatePlan(con, orgId, varType, varBy,
                (String) currentSheet.get("base_rate_sheet_id"), roundOff, userName, orgName);
          } else {
            success = updateRatePlan(con, orgId, (String) currentSheet.get("base_rate_sheet_id"),
                varType, varBy, roundOff, userName, orgName);
          }
        }
      } else {
        BigDecimal variance = new BigDecimal(varianceBy);
        BigDecimal roundoff = new BigDecimal(nearstRoundOfValue);
        Object[] updparams = { variance, roundoff, variance, roundoff, userName, orgId, baseOrgId,
            baseOrgId, orgId };
        success = updateCharges(con, UPDATE_CHARGES, updparams);
      }

      success &= new AuditLogDao("Master", "diagnostic_charges_audit_log").logMasterChange(con,
          userName, "UPDATE", "org_id", orgName);

    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    return success;
  }

  /**
   * Update charges based on new rate sheet.
   *
   * @param con the con
   * @param orgId the org id
   * @param varianceBy the variance by
   * @param baseOrgId the base org id
   * @param nearstRoundOfValue the nearst round of value
   * @param testId the test id
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws Exception the exception
   */
  public static boolean updateChargesBasedOnNewRateSheet(Connection con, String orgId,
      Double varianceBy, String baseOrgId, Double nearstRoundOfValue, String testId)
      throws SQLException, Exception {
    boolean success = false;
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(UPDATE_RATEPLAN_DIAG_CHARGES + " AND totab.test_id = ? ");
      ps.setBigDecimal(1, new BigDecimal(varianceBy));
      ps.setBigDecimal(2, new BigDecimal(nearstRoundOfValue));
      ps.setBigDecimal(3, new BigDecimal(varianceBy));
      ps.setBigDecimal(4, new BigDecimal(nearstRoundOfValue));
      ps.setString(5, orgId);
      ps.setString(6, baseOrgId);
      ps.setString(7, testId);

      int inc = ps.executeUpdate();
      if (inc >= 0) {
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
    status = updateCharges(con, UPDATE_RATEPLAN_DIAG_CHARGES + " AND totab.bed_type=? ", updparams);
    return status;
  }

}
