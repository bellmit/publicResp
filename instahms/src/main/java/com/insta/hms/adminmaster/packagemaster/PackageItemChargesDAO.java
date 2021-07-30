package com.insta.hms.adminmaster.packagemaster;

import com.insta.hms.master.rateplan.ItemChargeDAO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;

// TODO: Auto-generated Javadoc
/**
 * The Class PackageItemChargesDAO.
 */
public class PackageItemChargesDAO extends ItemChargeDAO {
  
  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(PackageItemChargesDAO.class);

  /**
   * Instantiates a new package item charges DAO.
   */
  public PackageItemChargesDAO() {
    super("package_item_charges");
  }

  /** The Constant INSERT_INTO_PACK_ITMES_PLUS. */
  private static final String INSERT_INTO_PACK_ITMES_PLUS =
      "INSERT INTO package_item_charges(package_id, "
      + " pack_ob_id, bed_type, org_id, charge)"
      + "(SELECT package_id,pack_ob_id,bed_type,?,round(charge + ?) "
      + " FROM package_item_charges  WHERE org_id = ? ) ";

  /** The Constant INSERT_INTO_PACK_ITMES_MINUS. */
  private static final String INSERT_INTO_PACK_ITMES_MINUS =
      "INSERT INTO package_item_charges(package_id,"
      + " pack_ob_id, bed_type, org_id, charge)"
      + "(SELECT package_id,pack_ob_id,bed_type,?,GREATEST(round(charge - ?), 0)"
      + " FROM package_item_charges  WHERE org_id = ? ) ";

  /** The Constant INSER_INTO_PACK_ITMES_BY. */
  private static final String INSER_INTO_PACK_ITMES_BY =
      "INSERT INTO package_item_charges(package_id,"
      + " pack_ob_id, bed_type, org_id, charge)"
      + "(SELECT package_id,pack_ob_id,bed_type,?,doroundvarying(charge,?,?)"
      + " FROM package_item_charges  WHERE org_id = ? ) ";

  /**
   * Adds the org for package items.
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
   * @return true, if successful
   * @throws Exception the exception
   */
  public static boolean addOrgForPackageItems(Connection con, String newOrgId, String varianceType,
      Double varianceValue, Double varianceBy, boolean useValue, String baseOrgId,
      Double nearstRoundOfValue, String userName, String orgName) throws Exception {
    boolean status = false;
    PreparedStatement ps = null;

    if (useValue) {
      if (varianceType.equals("Incr")) {
        ps = con.prepareStatement(INSERT_INTO_PACK_ITMES_PLUS);

      } else {
        ps = con.prepareStatement(INSERT_INTO_PACK_ITMES_MINUS);
      }
      ps.setString(1, newOrgId);
      ps.setDouble(2, varianceValue);
      ps.setString(3, baseOrgId);

      int val = ps.executeUpdate();
      logger.debug(Integer.toString(val));
      if (val >= 0) {
        status = true;
      }
    } else {
      ps = con.prepareStatement(INSER_INTO_PACK_ITMES_BY);
      ps.setString(1, newOrgId);
      if (!varianceType.equals("Incr")) {
        varianceBy = new Double(-varianceBy);
      }
      ps.setBigDecimal(2, new BigDecimal(varianceBy));
      ps.setBigDecimal(3, new BigDecimal(nearstRoundOfValue));
      ps.setString(4, baseOrgId);

      int val = ps.executeUpdate();
      logger.debug(Integer.toString(val));
      if (val >= 0) {
        status = true;
      }

    }
    ps.close();
    return status;
  }

  /** The Constant REINIT_CHARGES. */
  private static final String REINIT_CHARGES = "UPDATE package_item_charges AS target SET "
      + " charge = doroundvarying(pic.charge,?,?), " + " pack_ob_id = pic.pack_ob_id, "
      + " is_override = 'N' " + " FROM package_item_charges pic"
      + " where target.package_id = pic.package_id and target.bed_type = pic.bed_type "
      + " and target.pack_ob_id = pic.pack_ob_id and "
      + " target.org_id = ? and pic.org_id = ? and target.is_override != 'Y'";

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

    Object[] updparams = { varianceBy, roundOff, newOrgId, baseOrgId };
    status = updateCharges(con, REINIT_CHARGES, updparams);
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
    boolean status = addOrgForPackageItems(con, newOrgId, varianceType, 0.0, varianceBy, false,
        baseOrgId, roundOff, userName, orgName);
    return status;
  }

  /** The Constant UPDATE_CHARGES. */
  private static final String UPDATE_CHARGES = "UPDATE package_item_charges AS target SET "
      + " charge = doroundvarying(pic.charge,?,?), " + " pack_ob_id = pic.pack_ob_id, "
      + " is_override = 'N' " + " FROM package_item_charges pic, pack_org_details pod  "
      + " where pod.org_id = ? and pic.package_id = pod.package_id "
      + " and pod.base_rate_sheet_id = ? and "
      + " target.package_id = pic.package_id and target.bed_type = pic.bed_type "
      + " and target.pack_ob_id = pic.pack_ob_id and "
      + " pod.applicable = true and target.is_override != 'Y'"
      + " and pic.org_id = ? and target.org_id = ?";

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

    Object[] updparams = { varianceBy, roundOff, newOrgId, baseOrgId, baseOrgId, newOrgId };
    // No update exclusion as that would already be done as part of package charges.
    status = updateCharges(con, UPDATE_CHARGES, updparams);

    return status;
  }

}
