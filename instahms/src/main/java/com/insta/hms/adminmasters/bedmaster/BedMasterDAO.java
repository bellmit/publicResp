package com.insta.hms.adminmasters.bedmaster;

import com.bob.hms.adminmasters.organization.OrgMasterDao;
import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.auditlog.AuditLogDao;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.ipservices.BedDTO;
import com.insta.hms.master.rateplan.ItemChargeDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class BedMasterDAO.
 */
public class BedMasterDAO extends ItemChargeDAO {

  /**
   * Instantiates a new bed master DAO.
   */
  public BedMasterDAO() {
    super("bed_details");
  }

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(BedMasterDAO.class);

  /** The Constant GET_ALL_EXISTING_BEDS. */
  private static final String GET_ALL_EXISTING_BEDS =
      "SELECT distinct(Bed_Type) FROM Bed_Details ORDER BY bed_type ";

  /** The Constant GET_ALL_EXISTING_ICU_BEDS. */
  private static final String GET_ALL_EXISTING_ICU_BEDS =
      "SELECT distinct bed_type FROM bed_details where bed_status='A' "
      + "UNION SELECT distinct  intensive_bed_type as bed_type"
      + " FROM icu_bed_charges where bed_status='A' ";

  /** The Constant GET_GENERAL_WHEN_NO_ICU. */
  private static final String GET_GENERAL_WHEN_NO_ICU = "SELECT distinct Bed_Type FROM Bed_Details";

  /**
   * Gets the existingbedtypes.
   *
   * @param isIcu
   *          the is ICU
   * @return the existingbedtypes
   * @throws SQLException
   *           the SQL exception
   */
  public List<BasicDynaBean> getexistingbedtypes(boolean isIcu) throws SQLException {

    Connection con = DataBaseUtil.getReadOnlyConnection();
    PreparedStatement ps = null;
    if (isIcu) {
      ps = con.prepareStatement(GET_ALL_EXISTING_ICU_BEDS);
    } else {
      ps = con.prepareStatement(GET_ALL_EXISTING_BEDS);
    }
    List<BasicDynaBean> lis = DataBaseUtil.queryToDynaList(ps);

    if (lis.size() == 0) {
      ps = con.prepareStatement(GET_GENERAL_WHEN_NO_ICU);
      lis = DataBaseUtil.queryToDynaList(ps);
    }
    ps.close();
    con.close();
    return lis;
  }

  /** The Constant NORMAL_BED_CHARGE. */
  private static final String NORMAL_BED_CHARGE =
      " SELECT bed_type, organization, bed_status, intensive_bed_status, child_bed_status, "
      + "  bed_charge, bed_charge_discount, nursing_charge, nursing_charge_discount, "
      + "  duty_charge, duty_charge_discount, maintainance_charge, maintainance_charge_discount, "
      + "  hourly_charge, hourly_charge_discount, "
      + "  daycare_slab_1_charge, daycare_slab_2_charge, daycare_slab_3_charge, "
      + "  daycare_slab_1_charge_discount, daycare_slab_2_charge_discount, "
      + " daycare_slab_3_charge_discount, "
      + "  initial_payment, luxary_tax, code_type, item_code, billing_group_id "
      + " FROM bed_details JOIN bed_types bt ON bed_type_name = bed_type"
      + " where bed_type=? and organization=?";

  /**
   * Gets the normal bed charge.
   *
   * @param bedtytpe
   *          the bedtytpe
   * @param orgid
   *          the orgid
   * @return the normal bed charge
   */
  public List getNormalBedCharge(String bedtytpe, String orgid) {
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    List cl = null;
    try {
      con = DataBaseUtil.getConnection();

      ps = con.prepareStatement(NORMAL_BED_CHARGE);
      ps.setString(1, bedtytpe);
      ps.setString(2, orgid);
      cl = DataBaseUtil.queryToArrayList(ps);
      logger.debug("{}", cl);
    } catch (Exception ex) {
      logger.error("Exception occured in getNormalBedCharge method", ex);
    } finally {
      try {
        if (rs != null) {
          rs.close();
        }
        if (ps != null) {
          ps.close();
        }
        if (con != null) {
          con.close();
        }
      } catch (Exception ex) {
        logger.error("Exception in getNormalBedCharge method", ex);
      }
    }
    return cl;
  }

  /**
   * Gets the normal bed charges bean.
   *
   * @param bedType
   *          the bed type
   * @param orgId
   *          the org id
   * @return the normal bed charges bean
   * @throws SQLException
   *           the SQL exception
   */
  public BasicDynaBean getNormalBedChargesBean(String bedType, String orgId) throws SQLException {
    return DataBaseUtil.queryToDynaBean(NORMAL_BED_CHARGE, new Object[] { bedType, orgId });
  }

  /** The Constant ICU_BED_CHARGE. */
  private static final String ICU_BED_CHARGE =
      " SELECT bed_type, organization, bed_status, intensive_bed_type, "
      + "  bed_charge, bed_charge_discount, nursing_charge, nursing_charge_discount, "
      + "  duty_charge, duty_charge_discount, maintainance_charge, maintainance_charge_discount, "
      + "  hourly_charge, hourly_charge_discount, "
      + "  daycare_slab_1_charge, daycare_slab_2_charge, daycare_slab_3_charge, "
      + "  daycare_slab_1_charge_discount, daycare_slab_2_charge_discount,"
      + " daycare_slab_3_charge_discount, "
      + "  luxary_tax, initial_payment, code_type, item_code, billing_group_id "
      + " FROM icu_bed_charges bd JOIN bed_types bt ON bed_type_name = bed_type"
      + " where bed_type=? and organization=? and intensive_bed_type=?";

  /**
   * Gets the ICU bedcharges.
   *
   * @param previoubedtype
   *          the previoubedtype
   * @param icubedtype
   *          the icubedtype
   * @param orgid
   *          the orgid
   * @return the ICU bedcharges
   */
  public List getIcuBedcharges(String previoubedtype, String icubedtype, String orgid) {
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    List cl = null;
    try {
      con = DataBaseUtil.getConnection();

      String generalorgid = "ORG0001";
      String generalbedtype = "GENERAL";

      ps = con.prepareStatement(ICU_BED_CHARGE);
      ps.setString(1, previoubedtype);
      ps.setString(2, orgid);
      ps.setString(3, icubedtype);

      cl = DataBaseUtil.queryToArrayList(ps);
      logger.debug("{}", cl);

      if (cl.size() == 0) {
        ps.setString(1, generalbedtype);
        ps.setString(2, generalorgid);
        ps.setString(3, icubedtype);
        cl = DataBaseUtil.queryToArrayList(ps);
      }

    } catch (Exception ex) {
      logger.error("Exception in getICUBedcharges method", ex);
    } finally {
      try {
        if (rs != null) {
          rs.close();
        }
        if (ps != null) {
          ps.close();
        }
        if (con != null) {
          con.close();
        }
      } catch (Exception ex) {
        logger.error("Exception in getICUBedcharges method", ex);
      }
    }

    return cl;
  }

  /**
   * Gets the ICU bed charges bean.
   *
   * @param icuBedType
   *          the icu bed type
   * @param prvsBedType
   *          the prvs bed type
   * @param orgId
   *          the org id
   * @return the ICU bed charges bean
   * @throws SQLException
   *           the SQL exception
   */
  public BasicDynaBean getIcuBedChargesBean(String icuBedType, String prvsBedType, String orgId)
      throws SQLException {
    if (isIcuBedType(prvsBedType)) {
      return DataBaseUtil.queryToDynaBean(ICU_BED_CHARGE,
          new Object[] { "GENERAL", orgId, icuBedType });
    } else {
      return DataBaseUtil.queryToDynaBean(ICU_BED_CHARGE,
          new Object[] { prvsBedType, orgId, icuBedType });
    }
  }

  /** The Constant ALL_ICU_BED_CHARGE. */
  private static final String ALL_ICU_BED_CHARGE =
      " SELECT bed_type, organization, bed_status, intensive_bed_type, "
      + "  bed_charge, bed_charge_discount, nursing_charge, nursing_charge_discount, "
      + "  duty_charge, duty_charge_discount, maintainance_charge, maintainance_charge_discount, "
      + "  hourly_charge, hourly_charge_discount, "
      + "  daycare_slab_1_charge, daycare_slab_2_charge, daycare_slab_3_charge, "
      + "  daycare_slab_1_charge_discount, daycare_slab_2_charge_discount,"
      + " daycare_slab_3_charge_discount, "
      + "  luxary_tax, initial_payment, code_type, item_code, billing_group_id "
      + " FROM icu_bed_charges bd JOIN bed_types bt ON bed_type_name = bed_type where bed_type=?"
      + " and organization=? ";

  /**
   * Gets the ICU bed charges list.
   *
   * @param prvsBedType
   *          the prvs bed type
   * @param orgId
   *          the org id
   * @return the ICU bed charges list
   * @throws SQLException
   *           the SQL exception
   */
  public List<BasicDynaBean> getIcuBedChargesList(String prvsBedType, String orgId)
      throws SQLException {
    if (isIcuBedType(prvsBedType)) {
      return DataBaseUtil.queryToDynaList(ALL_ICU_BED_CHARGE, new Object[] { "GENERAL", orgId });
    } else {
      return DataBaseUtil.queryToDynaList(ALL_ICU_BED_CHARGE, new Object[] { prvsBedType, orgId });
    }
  }

  /** The Constant BED_OCCUPIED. */
  /*
   * Update the status of bed to occupied, where status can be Y (occupied), C
   * (cleaning), M (Maintenance), R (Retained). To set status to N, call
   * releaseBed function. The same status will be propogated to all children and
   * parent (suite) beds.
   */
  private static final String BED_OCCUPIED = " UPDATE bed_names SET occupancy=? "
      + " WHERE ( bed_id=? " + // update self
      "  OR bed_ref_id=? " + // update children
      "  OR bed_id=(SELECT bed_ref_id FROM bed_names WHERE bed_id=?)) AND occupancy != ? ";

  /**
   * Sets the bed occupied.
   *
   * @param con
   *          the con
   * @param bedId
   *          the bed id
   * @param occupancyStatus
   *          the occupancy status
   * @return the int
   * @throws SQLException
   *           the SQL exception
   */
  public static int setBedOccupied(Connection con, int bedId, String occupancyStatus)
      throws SQLException {

    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(BED_OCCUPIED);
      ps.setString(1, occupancyStatus);
      ps.setInt(2, bedId);
      ps.setInt(3, bedId);
      ps.setInt(4, bedId);
      ps.setString(5, occupancyStatus);

      return ps.executeUpdate();
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  /** The Constant BED_RELEASED. */
  /*
   * Release the bed, setting occupancy status to N. This will propogate to all
   * children in suite bed case, but to parent only if all siblings are also
   * unoccupied.
   */
  private static final String BED_RELEASED = " UPDATE bed_names bn SET occupancy='N' "
      + " WHERE bed_id=? " + // update self
      "  OR bed_ref_id=? " + // update children
      "  OR (bed_id=(SELECT bed_ref_id FROM bed_names WHERE bed_id=?) " + // update parent
      "      AND NOT EXISTS ( " + // if no children of parent other than self are occupied
      "  SELECT bed_id FROM bed_names WHERE bed_ref_id=bn.bed_id "
      + "AND occupancy = 'Y' AND bed_id!=?)) ";

  /**
   * Release bed.
   *
   * @param con
   *          the con
   * @param bedId
   *          the bed id
   * @throws SQLException
   *           the SQL exception
   */
  public static void releaseBed(Connection con, int bedId) throws SQLException {
    PreparedStatement ps = con.prepareStatement(BED_RELEASED);
    ps.setInt(1, bedId);
    ps.setInt(2, bedId);
    ps.setInt(3, bedId);
    ps.setInt(4, bedId);

    ps.executeUpdate();
    ps.close();
  }

  /** The Constant GET_ALL_BED_TYPE. */
  private static final String GET_ALL_BED_TYPE =
      " SELECT bed_type_name,is_icu,billing_bed_type,status FROM bed_types WHERE status='A'  ";

  /**
   * Gets the all bed type.
   *
   * @return the all bed type
   * @throws SQLException
   *           the SQL exception
   */
  public static List getAllBedType() throws SQLException {
    return DataBaseUtil.queryToDynaList(GET_ALL_BED_TYPE + " ORDER BY UPPER(bed_type_name) ");
  }

  /** The Constant GET_BED_WARD_QUERY. */
  /*
   * Retrieve a list of normal beds with their wards, and all related charges
   */
  private static final String GET_BED_WARD_QUERY = "SELECT "
      + " bn.bed_id, bn.bed_name, bn.ward_no, bn.bed_type, bd.bed_charge,"
      + " bd.hourly_charge, bd.nursing_charge, bd.duty_charge, bd.maintainance_charge, "
      + " bd.luxary_tax, bd.initial_payment, bd.bed_charge_discount, bd.nursing_charge_discount,"
      + " bd.duty_charge_discount, bd.maintainance_charge_discount, bd.hourly_charge_discount, "
      + " bd.daycare_slab_1_charge,bd.daycare_slab_2_charge,bd.daycare_slab_3_charge,"
      + "bd.daycare_slab_1_charge_discount, "
      + " bd.daycare_slab_2_charge_discount,bd.daycare_slab_3_charge_discount,bt.is_icu "
      + "FROM bed_names bn " + " JOIN bed_types bt on(bt.bed_type_name = bn.bed_type) "
      + " JOIN bed_details bd ON (bn.bed_type=bd.bed_type) "
      + " JOIN ward_names w ON (bn.ward_no=w.ward_no) "
      + "WHERE bd.bed_status='A' AND w.status='A' ";

  /** The Constant GET_BED_WARD_CHARGES. */
  private static final String GET_BED_WARD_CHARGES = GET_BED_WARD_QUERY + " AND organization=? "
      + " UNION " + GET_BED_WARD_QUERY + " AND organization='ORG0001' "
      + " AND NOT EXISTS (SELECT bed_type FROM bed_details "
      + " WHERE bed_type=bn.bed_type AND organization=?)";

  /**
   * Gets the bed ward charges.
   *
   * @param orgid
   *          the orgid
   * @return the bed ward charges
   * @throws SQLException
   *           the SQL exception
   */
  public List getBedWardCharges(String orgid) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    List list = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_BED_WARD_CHARGES);
      ps.setString(1, orgid);
      ps.setString(2, orgid);
      list = DataBaseUtil.queryToArrayList(ps);
    } finally {
      if (ps != null) {
        ps.close();
      }
      if (con != null) {
        con.close();
      }
    }
    return list;
  }

  /** The Constant bed_charges_list. */
  private static final String bed_charges_list = "SELECT "
      + " bd.bed_charge,bd.hourly_charge, bd.nursing_charge,"
      + " bd.duty_charge, bd.maintainance_charge, "
      + " bd.luxary_tax, bd.initial_payment, bd.bed_charge_discount, bd.nursing_charge_discount,"
      + " bd.duty_charge_discount, bd.maintainance_charge_discount, bd.hourly_charge_discount, "
      + " bd.daycare_slab_1_charge,bd.daycare_slab_2_charge,bd.daycare_slab_3_charge,"
      + " bd.daycare_slab_1_charge_discount, "
      + " bd.daycare_slab_2_charge_discount,bd.daycare_slab_3_charge_discount,bt.is_icu,"
      + " bd.bed_type, bd.code_type, bd.item_code,bt.billing_group_id " + "FROM bed_details bd "
      + " JOIN bed_types bt ON ( bed_type_name = bed_type ) ";

  /** The Constant bed_charges_list_unit. */
  private static final String bed_charges_list_unit = bed_charges_list + " AND organization=? "
      + " UNION " + bed_charges_list + " AND organization='ORG0001' "
      + " AND NOT EXISTS (SELECT bed_type FROM bed_details "
      + " WHERE bed_type=bd.bed_type AND organization=?)";

  /**
   * Gets the normal bed charges list.
   *
   * @param orgid
   *          the orgid
   * @return the normal bed charges list
   * @throws SQLException
   *           the SQL exception
   */
  public List<BasicDynaBean> getNormalBedChargesList(String orgid) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    List list = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(bed_charges_list_unit);
      ps.setString(1, orgid);
      ps.setString(2, orgid);
      list = DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return list;
  }

  /** The Constant GET_BED_CHARGES_QUERY. */
  /*
   * Return the bed details and associated different type of charges given a bed
   * ID
   */
  private static final String GET_BED_CHARGES_QUERY = GET_BED_WARD_QUERY
      + " AND bn.bed_id=? AND organization=?";

  /**
   * Gets the bed charges.
   *
   * @param bedId
   *          the bed id
   * @param orgId
   *          the org id
   * @return the bed charges
   * @throws SQLException
   *           the SQL exception
   */
  public BasicDynaBean getBedCharges(int bedId, String orgId) throws SQLException {
    return DataBaseUtil.queryToDynaBean(GET_BED_CHARGES_QUERY, new Object[] { bedId, orgId });
  }

  /** The Constant GET_BED_WARD_QUERYWOBN. */
  private static final String GET_BED_WARD_QUERYWOBN =
      "SELECT bn.ward_no, bn.bed_type, bd.bed_charge, bd.hourly_charge, bd.nursing_charge, "
      + "  bd.duty_charge, bd.maintainance_charge, bd.luxary_tax " + "FROM bed_names bn "
      + " JOIN bed_details bd ON (bn.bed_type=bd.bed_type) "
      + " JOIN ward_names w ON (bn.ward_no=w.ward_no) "
      + "WHERE bd.bed_status='A' AND w.status='A' AND bn.status='A' ";

  /** The Constant GET_BED_WARD_CHARGESWOBN. */
  private static final String GET_BED_WARD_CHARGESWOBN = GET_BED_WARD_QUERYWOBN
      + " AND organization=? " + " UNION " + GET_BED_WARD_QUERYWOBN + " AND organization='ORG0001' "
      + " AND NOT EXISTS (SELECT bed_type FROM bed_details"
      + " WHERE bed_type=bn.bed_type AND organization=?)";

  /**
   * Gets the bed ward charges WOBN.
   *
   * @param orgid
   *          the orgid
   * @return the bed ward charges WOBN
   * @throws SQLException
   *           the SQL exception
   */
  public List getBedWardChargesWobn(String orgid) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    List list = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_BED_WARD_CHARGESWOBN);
      ps.setString(1, orgid);
      ps.setString(2, orgid);
      list = DataBaseUtil.queryToArrayList(ps);
    } finally {
      if (ps != null) {
        ps.close();
      }
      if (con != null) {
        con.close();
      }
    }
    return list;
  }

  /** The Constant GET_ICU_WARD_QUERY. */
  /*
   * Retrieve a list of ICU beds with wards, charges
   */
  private static final String GET_ICU_WARD_QUERY =
      "SELECT bn.bed_type,bn.bed_id, bn.bed_name, bn.ward_no, ic.intensive_bed_type,"
      + " ic.bed_charge, ic.hourly_charge, "
      + " ic.nursing_charge, ic.duty_charge, ic.maintainance_charge,"
      + " ic.luxary_tax,ic.initial_payment,ic.bed_charge_discount, "
      + " ic.nursing_charge_discount, ic.duty_charge_discount, "
      + " ic.maintainance_charge_discount, ic.hourly_charge_discount,"
      + " ic.daycare_slab_1_charge, ic.daycare_slab_2_charge, "
      + " ic.daycare_slab_3_charge, ic.daycare_slab_1_charge_discount,"
      + " ic.daycare_slab_2_charge_discount, ic.daycare_slab_3_charge_discount "
      + " FROM bed_names bn "
      + "  JOIN icu_bed_charges ic ON (bn.bed_type = ic.intensive_bed_type) "
      + "  JOIN ward_names w ON (bn.ward_no=w.ward_no) "
      + " WHERE ic.bed_status='A' AND w.status='A'  and bn.status='A'";

  /** The Constant GET_ICU_WARD_CHARGES. */
  private static final String GET_ICU_WARD_CHARGES = GET_ICU_WARD_QUERY
      + " AND ic.bed_type=? AND ic.organization=? ";

  /**
   * Gets the ICU ward charges.
   *
   * @param bedType
   *          the bed type
   * @param orgid
   *          the orgid
   * @return the ICU ward charges
   * @throws SQLException
   *           the SQL exception
   */
  public List getIcuWardCharges(String bedType, String orgid) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    List list = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_ICU_WARD_CHARGES);
      ps.setString(1, bedType);
      ps.setString(2, orgid);
      list = DataBaseUtil.queryToArrayList(ps);
    } finally {
      if (ps != null) {
        ps.close();
      }
      if (con != null) {
        con.close();
      }
    }
    return list;
  }

  /** The Constant GET_ICU_CHARGES_QUERY. */
  /*
   * Return the ICU Bed details and associated different type of charges given a
   * bed ID If the billing bed_type is an ICU bed type, then, we use GENERAL as
   * the bed_type, as we don't have rates defined for ICU charges where the
   * billing bed_type itself is an ICU bed.
   */
  private static final String GET_ICU_CHARGES_QUERY = GET_ICU_WARD_QUERY
      + " AND bn.bed_id=? AND organization=? AND ic.bed_type=? ";

  /**
   * Gets the ICU bed charges.
   *
   * @param bedId
   *          the bed id
   * @param orgId
   *          the org id
   * @param bedType
   *          the bed type
   * @return the ICU bed charges
   * @throws SQLException
   *           the SQL exception
   */
  public BasicDynaBean getIcuBedCharges(int bedId, String orgId, String bedType)
      throws SQLException {
    if (isIcuBedType(bedType)) {
      return DataBaseUtil.queryToDynaBean(GET_ICU_CHARGES_QUERY,
          new Object[] { bedId, orgId, "GENERAL" });
    } else {
      return DataBaseUtil.queryToDynaBean(GET_ICU_CHARGES_QUERY,
          new Object[] { bedId, orgId, bedType });
    }
  }

  /** The Constant GET_ICU_WARD_QUERYWOBN. */
  private static final String GET_ICU_WARD_QUERYWOBN =
      "SELECT  bn.ward_no, ic.intensive_bed_type, ic.bed_charge, ic.hourly_charge, "
      + "  ic.nursing_charge, ic.duty_charge, ic.maintainance_charge,"
      + " ic.luxary_tax, ic.code_type, ic.item_code " + " FROM bed_names bn "
      + "  JOIN icu_bed_charges ic ON (bn.bed_type = ic.intensive_bed_type) "
      + "  JOIN ward_names w ON (bn.ward_no=w.ward_no) "
      + " WHERE ic.bed_status='A' AND w.status='A' ";

  /** The Constant GET_ICU_WARD_CHARGESWOBN. */
  private static final String GET_ICU_WARD_CHARGESWOBN = GET_ICU_WARD_QUERYWOBN
      + " AND ic.bed_type=? AND ic.organization=? " + " UNION " + GET_ICU_WARD_QUERYWOBN
      + " AND organization='ORG0001' AND ic.bed_type='GENERAL' "
      + " AND NOT EXISTS (SELECT bed_type FROM icu_bed_charges "
      + " WHERE intensive_bed_type=bn.bed_type AND bed_type=? AND organization=?)";

  /**
   * Gets the ICU ward charges WOBN.
   *
   * @param bedType
   *          the bed type
   * @param orgid
   *          the orgid
   * @return the ICU ward charges WOBN
   * @throws SQLException
   *           the SQL exception
   */
  public List getIcuWardChargesWobn(String bedType, String orgid) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    List list = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_ICU_WARD_CHARGESWOBN);
      ps.setString(1, bedType);
      ps.setString(2, orgid);
      ps.setString(3, bedType);
      ps.setString(4, orgid);
      list = DataBaseUtil.queryToArrayList(ps);
    } finally {
      if (ps != null) {
        ps.close();
      }
      if (con != null) {
        con.close();
      }
    }
    return list;
  }

  /** The Constant BED_FIELDS. */
  private static final String BED_FIELDS =
      "SELECT r.bedtype,r.bed_charge,r.nursing_charge,r.initial_payment,r.duty_charge,"
      + "r.maintainance_charge,r.luxary_tax,r.bed_status,r.hourly_charge,is_override ";

  /** The Constant BED_COUNT. */
  private static final String BED_COUNT = " SELECT count(*) ";

  /** The Constant BED_TABLES. */
  private static final String BED_TABLES = " FROM bedcharges_view r";

  /**
   * Gets the bed details.
   *
   * @param chargeHead
   *          the charge head
   * @param orgId
   *          the org id
   * @param pageNum
   *          the page num
   * @param isOverride
   *          the is override
   * @return the bed details
   * @throws SQLException
   *           the SQL exception
   */
  public PagedList getBedDetails(String chargeHead, String orgId, int pageNum, String isOverride)
      throws SQLException {

    Connection con = DataBaseUtil.getConnection();
    SearchQueryBuilder qb = new SearchQueryBuilder(con, BED_FIELDS, BED_COUNT, BED_TABLES, null,
        null, false, 25, pageNum);
    qb.addFilter(SearchQueryBuilder.STRING, "r.organization", "=", orgId);

    if (null != isOverride) {
      qb.addFilter(SearchQueryBuilder.STRING, "r.is_override", "=", isOverride);
    }

    qb.build();

    PreparedStatement psCount = qb.getCountStatement();

    int totalCount = 0;
    ResultSet rsCount = psCount.executeQuery();
    if (rsCount.next()) {
      totalCount = rsCount.getInt(1);
    }
    rsCount.close();
    psCount.close();
    PreparedStatement psData = qb.getDataStatement();
    ArrayList<Hashtable<String, String>> al = DataBaseUtil.queryToArrayList(psData);
    psData.close();
    con.close();
    // list,totalrecords,pageSize,pageNo
    return new PagedList(al, totalCount, 25, pageNum);
  }

  /** The Constant checkBedType. */
  private static final String checkBedType = "SELECT count(*) from bed_details  where bed_type = ?";

  /** The Constant GET_NORMAL_BED_CHARGES. */
  private static final String GET_NORMAL_BED_CHARGES =
      "SELECT bd.bed_type,bd.bed_charge,bd.nursing_charge,bd.duty_charge,"
      + " bd.maintainance_charge,bd.hourly_charge,bd.luxary_tax,bd.hourly_charge"
      + " ,bd.bed_status,bd.initial_payment,org.org_name,bd.bed_charge_discount,"
      + " bd.nursing_charge_discount,bd.duty_charge_discount,"
      + " bd.maintainance_charge_discount,bd.hourly_charge_discount,"
      + " bd.daycare_slab_1_charge,bd.daycare_slab_2_charge,bd.daycare_slab_3_charge, "
      + " bd.daycare_slab_1_charge_discount,bd.daycare_slab_2_charge_discount,"
      + " bd.daycare_slab_3_charge_discount," + " item_code, code_type, billing_group_id "
      + " FROM bed_details bd JOIN bed_types bt ON bt.bed_type_name = bd.bed_type"
      + " JOIN organization_details org ON org.org_id = bd.organization WHERE bd.bed_type=? and "
      + " bd.organization=?";

  /** The Constant GET_ICU_BED_CHARGES. */
  private static final String GET_ICU_BED_CHARGES =
      "SELECT icu.bed_type, icu.bed_charge,icu.nursing_charge,icu.initial_payment,"
      + " icu.duty_charge,icu.maintainance_charge,icu.organization,"
      + " icu.bed_status,icu.luxary_tax,icu.hourly_charge"
      + " ,icu.bed_charge_discount,icu.nursing_charge_discount,icu.duty_charge_discount,"
      + " icu.maintainance_charge_discount"
      + " ,icu.hourly_charge_discount,icu.daycare_slab_1_charge,icu.daycare_slab_2_charge,"
      + " icu.daycare_slab_3_charge, "
      + " icu.daycare_slab_1_charge_discount, icu.daycare_slab_2_charge_discount,"
      + " icu.daycare_slab_3_charge_discount, item_code, code_type, billing_group_id "
      + " FROM icu_bed_charges icu JOIN bed_types bt ON bt.bed_type_name = icu.bed_type"
      + " WHERE intensive_bed_type=? and organization =?";

  /**
   * Gets the edits the charges screen.
   *
   * @param bedType
   *          the bed type
   * @param orgId
   *          the org id
   * @return the edits the charges screen
   * @throws Exception
   *           the exception
   */
  public Map getEditChargesScreen(String bedType, String orgId) throws Exception {

    LinkedHashMap<String, List> output = new LinkedHashMap<String, List>();

    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;

    ArrayList<String> bedTypes = new ArrayList<String>();
    ArrayList<String> bedCharge = new ArrayList<String>();
    ArrayList<String> bedChargeDiscount = new ArrayList<String>();
    ArrayList<String> nursingCharge = new ArrayList<String>();
    ArrayList<String> nursingChargeDiscount = new ArrayList<String>();
    ArrayList<String> dutyCharge = new ArrayList<String>();
    ArrayList<String> dutyChargeDiscount = new ArrayList<String>();
    ArrayList<String> profCharge = new ArrayList<String>();
    ArrayList<String> profChargeDiscount = new ArrayList<String>();
    ArrayList<String> hourlyCharge = new ArrayList<String>();
    ArrayList<String> hourlyChargeDiscount = new ArrayList<String>();
    ArrayList<String> luxCharge = new ArrayList<String>();
    ArrayList<String> intialCharge = new ArrayList<String>();
    ArrayList<String> daycareSlab1Charge = new ArrayList<String>();
    ArrayList<String> daycareSlab2Charge = new ArrayList<String>();
    ArrayList<String> daycareSlab3Charge = new ArrayList<String>();
    ArrayList<String> daycareSlab1ChargeDiscount = new ArrayList<String>();
    ArrayList<String> daycareSlab2ChargeDiscount = new ArrayList<String>();
    ArrayList<String> daycareSlab3ChargeDiscount = new ArrayList<String>();
    String codeType;
    String orgItemCode;

    if (!isIcuBedType(bedType)) {
      // normal bed
      ps = con.prepareStatement(GET_NORMAL_BED_CHARGES);
      ps.setString(1, bedType);
      ps.setString(2, orgId);

      ArrayList<Hashtable<String, String>> al = DataBaseUtil.queryToArrayList(ps);
      Iterator<Hashtable<String, String>> it = al.iterator();

      while (it.hasNext()) {
        Hashtable<String, String> ht = it.next();
        bedTypes.add(ht.get("BED_TYPE"));
        bedCharge.add(ht.get("BED_CHARGE"));
        bedChargeDiscount.add(ht.get("BED_CHARGE_DISCOUNT"));
        nursingCharge.add(ht.get("NURSING_CHARGE"));
        nursingChargeDiscount.add(ht.get("NURSING_CHARGE_DISCOUNT"));
        dutyCharge.add(ht.get("DUTY_CHARGE"));
        dutyChargeDiscount.add(ht.get("DUTY_CHARGE_DISCOUNT"));
        profCharge.add(ht.get("MAINTAINANCE_CHARGE"));
        profChargeDiscount.add(ht.get("MAINTAINANCE_CHARGE_DISCOUNT"));
        hourlyCharge.add(ht.get("HOURLY_CHARGE"));
        hourlyChargeDiscount.add(ht.get("HOURLY_CHARGE_DISCOUNT"));
        intialCharge.add(ht.get("INITIAL_PAYMENT"));
        daycareSlab1Charge.add(ht.get("DAYCARE_SLAB_1_CHARGE"));
        daycareSlab2Charge.add(ht.get("DAYCARE_SLAB_2_CHARGE"));
        daycareSlab3Charge.add(ht.get("DAYCARE_SLAB_3_CHARGE"));
        daycareSlab1ChargeDiscount.add(ht.get("DAYCARE_SLAB_1_CHARGE_DISCOUNT"));
        daycareSlab2ChargeDiscount.add(ht.get("DAYCARE_SLAB_2_CHARGE_DISCOUNT"));
        daycareSlab3ChargeDiscount.add(ht.get("DAYCARE_SLAB_3_CHARGE_DISCOUNT"));
        luxCharge.add(ht.get("LUXARY_TAX"));
      }
    } else {
      // icu bed
      ps = con.prepareStatement(GET_ICU_BED_CHARGES);
      ps.setString(1, bedType);
      ps.setString(2, orgId);

      ArrayList<Hashtable<String, String>> al = DataBaseUtil.queryToArrayList(ps);
      Iterator<Hashtable<String, String>> it = al.iterator();

      while (it.hasNext()) {
        Hashtable<String, String> ht = it.next();
        bedTypes.add(ht.get("BED_TYPE"));
        bedCharge.add(ht.get("BED_CHARGE"));
        bedChargeDiscount.add(ht.get("BED_CHARGE_DISCOUNT"));
        nursingCharge.add(ht.get("NURSING_CHARGE"));
        nursingChargeDiscount.add(ht.get("NURSING_CHARGE_DISCOUNT"));
        dutyCharge.add(ht.get("DUTY_CHARGE"));
        dutyChargeDiscount.add(ht.get("DUTY_CHARGE_DISCOUNT"));
        profCharge.add(ht.get("MAINTAINANCE_CHARGE"));
        profChargeDiscount.add(ht.get("MAINTAINANCE_CHARGE_DISCOUNT"));
        hourlyCharge.add(ht.get("HOURLY_CHARGE"));
        hourlyChargeDiscount.add(ht.get("HOURLY_CHARGE_DISCOUNT"));
        intialCharge.add(ht.get("INITIAL_PAYMENT"));
        daycareSlab1Charge.add(ht.get("DAYCARE_SLAB_1_CHARGE"));
        daycareSlab2Charge.add(ht.get("DAYCARE_SLAB_2_CHARGE"));
        daycareSlab3Charge.add(ht.get("DAYCARE_SLAB_3_CHARGE"));
        daycareSlab1ChargeDiscount.add(ht.get("DAYCARE_SLAB_1_CHARGE_DISCOUNT"));
        daycareSlab2ChargeDiscount.add(ht.get("DAYCARE_SLAB_2_CHARGE_DISCOUNT"));
        daycareSlab3ChargeDiscount.add(ht.get("DAYCARE_SLAB_3_CHARGE_DISCOUNT"));
        luxCharge.add(ht.get("LUXARY_TAX"));
      }
    }

    output.put("BEDTYPES", bedTypes);
    output.put("BED CHARGE", bedCharge);
    output.put("BED CHARGE DISCOUNT", bedChargeDiscount);
    output.put("NURSING CHARGE", nursingCharge);
    output.put("NURSING CHARGE DISCOUNT", nursingChargeDiscount);
    output.put("DUTY DOCTOR CHARGE", dutyCharge);
    output.put("DUTY DOCTOR CHARGE DISCOUNT", dutyChargeDiscount);
    output.put("PROFESSIONAL CHARGE", profCharge);
    output.put("PROFESSIONAL CHARGE DISCOUNT", profChargeDiscount);
    output.put("HOURLY CHARGE", hourlyCharge);
    output.put("HOURLY CHARGE DISCOUNT", hourlyChargeDiscount);
    output.put("INTIAL CHARGE", intialCharge);
    output.put("DAYCARE SLAB 1 CHARGE", daycareSlab1Charge);
    output.put("DAYCARE SLAB 2 CHARGE", daycareSlab2Charge);
    output.put("DAYCARE SLAB 3 CHARGE", daycareSlab3Charge);
    output.put("DAYCARE SLAB 1 CHARGE DISCOUNT", daycareSlab1ChargeDiscount);
    output.put("DAYCARE SLAB 2 CHARGE DISCOUNT", daycareSlab2ChargeDiscount);
    output.put("DAYCARE SLAB 3 CHARGE DISCOUNT", daycareSlab3ChargeDiscount);
    output.put("LUXARY CHARGE", luxCharge);

    logger.debug("{}", output);

    DataBaseUtil.closeConnections(con, ps);
    return output;
  }

  /** The Constant GET_DISTINCT_BEDS. */
  private static final String GET_DISTINCT_BEDS = "select distinct bed_type_name from bed_types";

  /**
   * Gets the new screenfor adding new bed.
   *
   * @param isIcu
   *          the is ICU
   * @return the new screenfor adding new bed
   * @throws SQLException
   *           the SQL exception
   */
  public Map getNewScreenforAddingNewBed(boolean isIcu) throws SQLException {

    LinkedHashMap<String, List> output = new LinkedHashMap<String, List>();
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = con.prepareStatement(GET_DISTINCT_BEDS);

    ArrayList<String> bedTypes = new ArrayList<String>();
    ArrayList<String> bedCharge = new ArrayList<String>();
    ArrayList<String> bedChargeDiscount = new ArrayList<String>();
    ArrayList<String> nursingCharge = new ArrayList<String>();
    ArrayList<String> nursingChargeDiscount = new ArrayList<String>();
    ArrayList<String> dutyCharge = new ArrayList<String>();
    ArrayList<String> dutyChargeDiscount = new ArrayList<String>();
    ArrayList<String> profCharge = new ArrayList<String>();
    ArrayList<String> profChargeDiscount = new ArrayList<String>();
    ArrayList<String> hourlyCharge = new ArrayList<String>();
    ArrayList<String> hourlyChargeDiscount = new ArrayList<String>();
    ArrayList<String> intialCharge = new ArrayList<String>();
    ArrayList<String> luxCharge = new ArrayList<String>();
    ArrayList<String> daycareSlab1Charge = new ArrayList<String>();
    ArrayList<String> daycareSlab1ChargeDiscount = new ArrayList<String>();
    ArrayList<String> daycareSlab2Charge = new ArrayList<String>();
    ArrayList<String> daycareSlab2ChargeDiscount = new ArrayList<String>();
    ArrayList<String> daycareSlab3Charge = new ArrayList<String>();
    ArrayList<String> daycareSlab3ChargeDiscount = new ArrayList<String>();

    if (!isIcu) {
      // normal bed
      bedTypes.add("");
      bedCharge.add("");
      bedChargeDiscount.add("");
      nursingCharge.add("");
      nursingChargeDiscount.add("");
      dutyCharge.add("");
      dutyChargeDiscount.add("");
      profCharge.add("");
      profChargeDiscount.add("");
      hourlyCharge.add("");
      hourlyChargeDiscount.add("");
      intialCharge.add("");
      luxCharge.add("");
      daycareSlab1Charge.add("");
      daycareSlab1ChargeDiscount.add("");
      daycareSlab2Charge.add("");
      daycareSlab2ChargeDiscount.add("");
      daycareSlab3Charge.add("");
      daycareSlab3ChargeDiscount.add("");
    } else {
      ;
      // icu type
      ArrayList al = DataBaseUtil.queryToOnlyArrayList(ps);
      Iterator<String> it = al.iterator();
      while (it.hasNext()) {
        bedTypes.add(it.next());
        bedCharge.add("");
        bedChargeDiscount.add("");
        nursingCharge.add("");
        nursingChargeDiscount.add("");
        dutyCharge.add("");
        dutyChargeDiscount.add("");
        profCharge.add("");
        profChargeDiscount.add("");
        hourlyCharge.add("");
        hourlyChargeDiscount.add("");
        intialCharge.add("");
        luxCharge.add("");
        daycareSlab1Charge.add("");
        daycareSlab1ChargeDiscount.add("");
        daycareSlab2Charge.add("");
        daycareSlab2ChargeDiscount.add("");
        daycareSlab3Charge.add("");
        daycareSlab3ChargeDiscount.add("");
      }
    }
    output.put("BEDTYPES", bedTypes);
    output.put("BED CHARGE", bedCharge);
    output.put("BED CHARGE DISCOUNT", bedChargeDiscount);
    output.put("NURSING CHARGE", nursingCharge);
    output.put("NURSING CHARGE DISCOUNT", nursingChargeDiscount);
    output.put("DUTY DOCTOR CHARGE", dutyCharge);
    output.put("DUTY DOCTOR CHARGE DISCOUNT", dutyChargeDiscount);
    output.put("PROFESSIONAL CHARGE", profCharge);
    output.put("PROFESSIONAL CHARGE DISCOUNT", profChargeDiscount);
    output.put("HOURLY CHARGE", hourlyCharge);
    output.put("HOURLY CHARGE DISCOUNT", hourlyChargeDiscount);
    output.put("INTIAL CHARGE", intialCharge);
    output.put("LUXARY CHARGE", luxCharge);
    output.put("DAYCARE SLAB 1 CHARGE", daycareSlab1Charge);
    output.put("DAYCARE SLAB 1 CHARGE DISCOUNT", daycareSlab1ChargeDiscount);
    output.put("DAYCARE SLAB 2 CHARGE", daycareSlab2Charge);
    output.put("DAYCARE SLAB 2 CHARGE DISCOUNT", daycareSlab2ChargeDiscount);
    output.put("DAYCARE SLAB 3 CHARGE", daycareSlab3Charge);
    output.put("DAYCARE SLAB 3 CHARGE DISCOUNT", daycareSlab3ChargeDiscount);
    logger.debug("{}", output);
    DataBaseUtil.closeConnections(con, ps);
    return output;
  }

  /** The Constant GET_BED_STATUS. */
  private static final String GET_BED_STATUS =
      "SELECT bed_status FROM bed_details WHERE bed_type = ?";

  /** The Constant GET_ICU_BED_STATUS. */
  private static final String GET_ICU_BED_STATUS =
      "SELECT bed_status FROM icu_bed_charges WHERE intensive_bed_type = ? ";

  /**
   * Gets the bed status.
   *
   * @param bedType
   *          the bed type
   * @return the bed status
   * @throws SQLException
   *           the SQL exception
   */
  public String getBedStatus(String bedType) throws SQLException {
    String status = null;
    boolean isIcu = isIcuBedType(bedType);
    Connection con = null;
    PreparedStatement ps = null;
    if (!isIcu) {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_BED_STATUS);
      ps.setString(1, bedType);
      status = DataBaseUtil.getStringValueFromDb(ps);

    } else {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_ICU_BED_STATUS);
      ps.setString(1, bedType);
      status = DataBaseUtil.getStringValueFromDb(ps);

    }

    ps.close();
    con.close();

    return status;
  }

  /** The Constant INSERT_INTO_BED_DETAILS. */
  private static final String INSERT_INTO_BED_DETAILS =
      "INSERT INTO bed_details(bed_type,bed_charge,nursing_charge,initial_payment,"
      + "duty_charge,maintainance_charge,organization,bed_status,luxary_tax,hourly_charge"
      + ",bed_charge_discount,nursing_charge_discount,duty_charge_discount,"
      + " maintainance_charge_discount"
      + ",hourly_charge_discount,daycare_slab_1_charge,daycare_slab_2_charge,"
      + " daycare_slab_3_charge,daycare_slab_1_charge_discount,daycare_slab_2_charge_discount, "
      + " daycare_slab_3_charge_discount, item_code, code_type) "
      + " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";

  /** The Constant INSERT_INTO_ICU_CHARGES. */
  private static final String INSERT_INTO_ICU_CHARGES =
      "INSERT INTO icu_bed_charges(bed_type,bed_charge,nursing_charge,initial_payment,"
      + "duty_charge,maintainance_charge,organization,bed_status,luxary_tax,intensive_bed_type"
      + ",hourly_charge,bed_charge_discount,nursing_charge_discount,duty_charge_discount,"
      + " maintainance_charge_discount"
      + ",hourly_charge_discount,daycare_slab_1_charge,daycare_slab_2_charge,"
      + " daycare_slab_3_charge,daycare_slab_1_charge_discount,daycare_slab_2_charge_discount,"
      + " daycare_slab_3_charge_discount, item_code, code_type) VALUES(?,?,?,?,?,?,"
      + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

  /** The Constant UPDATE_BED_CHARGE. */
  private static final String UPDATE_BED_CHARGE =
      "UPDATE bed_details SET bed_charge=?,nursing_charge=?, initial_payment=?,"
      + "duty_charge=?,maintainance_charge=?,bed_status=?,luxary_tax=?,hourly_charge=?"
      + ",bed_charge_discount=?,nursing_charge_discount=?,duty_charge_discount=?,"
      + " maintainance_charge_discount=?"
      + ",hourly_charge_discount=?,daycare_slab_1_charge=?,daycare_slab_2_charge=?,"
      + " daycare_slab_3_charge=?,daycare_slab_1_charge_discount=?,"
      + " daycare_slab_2_charge_discount=?, daycare_slab_3_charge_discount=?, "
      + " item_code=?, code_type=?  WHERE bed_type=? and organization=?";

  /** The Constant UPDATE_ICU_CHARGE. */
  private static final String UPDATE_ICU_CHARGE =
      "UPDATE icu_bed_charges SET bed_charge=?,nursing_charge=?,initial_payment=?,"
      + " duty_charge=?,maintainance_charge=?,bed_status=?,luxary_tax=?,hourly_charge=?"
      + ",bed_charge_discount=?,nursing_charge_discount=?,duty_charge_discount=?,"
      + " maintainance_charge_discount=?"
      + ",hourly_charge_discount=?,daycare_slab_1_charge=?,daycare_slab_2_charge=?,"
      + " daycare_slab_3_charge=?,daycare_slab_1_charge_discount=?,"
      + " daycare_slab_2_charge_discount=?, "
      + " daycare_slab_3_charge_discount=?, item_code=?, code_type=? WHERE bed_type=? AND "
      + " organization=? AND intensive_bed_type=?";

  /** The Constant isBedisThereForOrg. */
  private static final String isBedisThereForOrg =
      "SELECT count(*) FROM bed_details WHERE bed_type=? and organization=?";

  /** The Constant isIcuisThereForOrg. */
  private static final String isIcuisThereForOrg =
      "SELECT count(*) FROM icu_bed_charges WHERE bed_type=? AND organization=? "
      + " AND intensive_bed_type=? ";

  /**
   * Adds the or update bed charge.
   *
   * @param con
   *          the con
   * @param al
   *          the al
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean addOrUpdateBedCharge(Connection con, ArrayList<BedDetails> al)
      throws SQLException {

    boolean status = false;
    PreparedStatement ps = con.prepareStatement(checkBedType);
    PreparedStatement ps1 = con.prepareStatement(UPDATE_BED_CHARGE);
    PreparedStatement ps2 = con.prepareStatement(INSERT_INTO_BED_DETAILS);
    PreparedStatement ps3 = con.prepareStatement(UPDATE_ICU_CHARGE);
    PreparedStatement ps4 = con.prepareStatement(INSERT_INTO_ICU_CHARGES);

    PreparedStatement rps5 = con.prepareStatement(isBedisThereForOrg);
    PreparedStatement rps6 = con.prepareStatement(isIcuisThereForOrg);

    boolean icubed = false;
    Iterator<BedDetails> it = al.iterator();
    while (it.hasNext()) {
      BedDetails bd = it.next();
      ps.setString(1, bd.getBedType());
      String count = DataBaseUtil.getStringValueFromDb(ps);

      if (count.equals("0")) {
        icubed = true;
      }

      ps.close();
      break;
    }

    Iterator<BedDetails> it1 = al.iterator();
    while (it1.hasNext()) {
      BedDetails bd = it1.next();
      if (!icubed) {
        // bed_details block
        rps5.setString(1, bd.getBedType());
        rps5.setString(2, bd.getOrgId());

        String count = DataBaseUtil.getStringValueFromDb(rps5);
        if (count.equals("0")) {
          // insert block for bed_details
          ps2.setString(1, bd.getBedType());
          ps2.setDouble(2, bd.getBedCharge());
          ps2.setDouble(3, bd.getNursingCharge());
          ps2.setDouble(4, bd.getIntialCharge());
          ps2.setDouble(5, bd.getDutyCharge());
          ps2.setDouble(6, bd.getProfCharge());
          ps2.setString(7, bd.getOrgId());
          ps2.setString(8, bd.getBedStatus());
          ps2.setDouble(9, bd.getLuxaryCharge());
          ps2.setDouble(10, bd.getHourlyCharge());
          ps2.setDouble(11, bd.getBedChargeDiscount());
          ps2.setDouble(12, bd.getNursingChargeDiscount());
          ps2.setDouble(13, bd.getDutyChargeDiscount());
          ps2.setDouble(14, bd.getProfChargeDiscount());
          ps2.setDouble(15, bd.getHourlyChargeDiscount());
          ps2.setDouble(16, bd.getDaycareSlab1Charge());
          ps2.setDouble(17, bd.getDaycareSlab2Charge());
          ps2.setDouble(18, bd.getDaycareSlab3Charge());
          ps2.setDouble(19, bd.getDaycareSlab1ChargeDiscount());
          ps2.setDouble(20, bd.getDaycareSlab2ChargeDiscount());
          ps2.setDouble(21, bd.getDaycareSlab3ChargeDiscount());
          ps2.setString(22, bd.getOrgItemCode());
          ps2.setString(23, bd.getCodeType());

          ps2.addBatch();

        } else {
          // update block for bed_details
          ps1.setDouble(1, bd.getBedCharge());
          ps1.setDouble(2, bd.getNursingCharge());
          ps1.setDouble(3, bd.getIntialCharge());
          ps1.setDouble(4, bd.getDutyCharge());
          ps1.setDouble(5, bd.getProfCharge());
          ps1.setString(6, bd.getBedStatus());
          ps1.setDouble(7, bd.getLuxaryCharge());
          ps1.setDouble(8, bd.getHourlyCharge());
          ps1.setDouble(9, bd.getBedChargeDiscount());
          ps1.setDouble(10, bd.getNursingChargeDiscount());
          ps1.setDouble(11, bd.getDutyChargeDiscount());
          ps1.setDouble(12, bd.getProfChargeDiscount());
          ps1.setDouble(13, bd.getHourlyChargeDiscount());
          ps1.setDouble(14, bd.getDaycareSlab1Charge());
          ps1.setDouble(15, bd.getDaycareSlab2Charge());
          ps1.setDouble(16, bd.getDaycareSlab3Charge());
          ps1.setDouble(17, bd.getDaycareSlab1ChargeDiscount());
          ps1.setDouble(18, bd.getDaycareSlab2ChargeDiscount());
          ps1.setDouble(19, bd.getDaycareSlab3ChargeDiscount());
          ps1.setString(20, bd.getOrgItemCode());
          ps1.setString(21, bd.getCodeType());
          ps1.setString(22, bd.getBedType());
          ps1.setString(23, bd.getOrgId());

          ps1.addBatch();
        }
      } else {
        // icu_bed_charges block
        rps6.setString(1, bd.getBaseBed());
        rps6.setString(2, bd.getOrgId());
        rps6.setString(3, bd.getBedType());

        String count = DataBaseUtil.getStringValueFromDb(rps6);
        if (count.equals("0")) {
          // insert block for icu_bed_charges
          ps4.setString(1, bd.getBaseBed());
          ps4.setDouble(2, bd.getBedCharge());
          ps4.setDouble(3, bd.getNursingCharge());
          ps4.setDouble(4, bd.getIntialCharge());
          ps4.setDouble(5, bd.getDutyCharge());
          ps4.setDouble(6, bd.getProfCharge());
          ps4.setString(7, bd.getOrgId());
          ps4.setString(8, bd.getBedStatus());
          ps4.setDouble(9, bd.getLuxaryCharge());
          ps4.setString(10, bd.getBedType());
          ps4.setDouble(11, bd.getHourlyCharge());
          ps4.setDouble(12, bd.getBedChargeDiscount());
          ps4.setDouble(13, bd.getNursingChargeDiscount());
          ps4.setDouble(14, bd.getDutyChargeDiscount());
          ps4.setDouble(15, bd.getProfChargeDiscount());
          ps4.setDouble(16, bd.getHourlyChargeDiscount());
          ps4.setDouble(17, bd.getDaycareSlab1Charge());
          ps4.setDouble(18, bd.getDaycareSlab2Charge());
          ps4.setDouble(19, bd.getDaycareSlab3Charge());
          ps4.setDouble(20, bd.getDaycareSlab1ChargeDiscount());
          ps4.setDouble(21, bd.getDaycareSlab2ChargeDiscount());
          ps4.setDouble(22, bd.getDaycareSlab3ChargeDiscount());
          ps4.setString(23, bd.getOrgItemCode());
          ps4.setString(24, bd.getCodeType());

          ps4.addBatch();

        } else {
          // update block for icu_bed_charges
          ps3.setDouble(1, bd.getBedCharge());
          ps3.setDouble(2, bd.getNursingCharge());
          ps3.setDouble(3, bd.getIntialCharge());
          ps3.setDouble(4, bd.getDutyCharge());
          ps3.setDouble(5, bd.getProfCharge());
          ps3.setString(6, bd.getBedStatus());
          ps3.setDouble(7, bd.getLuxaryCharge());
          ps3.setDouble(8, bd.getHourlyCharge());
          ps3.setDouble(9, bd.getBedChargeDiscount());
          ps3.setDouble(10, bd.getNursingChargeDiscount());
          ps3.setDouble(11, bd.getDutyChargeDiscount());
          ps3.setDouble(12, bd.getProfChargeDiscount());
          ps3.setDouble(13, bd.getHourlyChargeDiscount());
          ps3.setDouble(14, bd.getDaycareSlab1Charge());
          ps3.setDouble(15, bd.getDaycareSlab2Charge());
          ps3.setDouble(16, bd.getDaycareSlab3Charge());
          ps3.setDouble(17, bd.getDaycareSlab1ChargeDiscount());
          ps3.setDouble(18, bd.getDaycareSlab2ChargeDiscount());
          ps3.setDouble(19, bd.getDaycareSlab3ChargeDiscount());
          ps3.setString(20, bd.getOrgItemCode());
          ps3.setString(21, bd.getCodeType());
          ps3.setString(22, bd.getBaseBed());
          ps3.setString(23, bd.getOrgId());
          ps3.setString(24, bd.getBedType());

          ps3.addBatch();

        }
      }

    }

    int[] aval = ps1.executeBatch();
    int[] bval = ps2.executeBatch();
    int[] cval = ps3.executeBatch();
    int[] dval = ps4.executeBatch();

    do {
      status = DataBaseUtil.checkBatchUpdates(aval);
      if (!status) {
        break;
      }
      status = DataBaseUtil.checkBatchUpdates(bval);
      if (!status) {
        break;
      }
      status = DataBaseUtil.checkBatchUpdates(cval);
      if (!status) {
        break;
      }
      status = DataBaseUtil.checkBatchUpdates(dval);
      if (!status) {
        break;
      }

    } while (false);

    return status;
  }

  /**
   * Adds the new normal bed.
   *
   * @param con
   *          the con
   * @param al
   *          the al
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean addNewNormalBed(Connection con, ArrayList<BedDetails> al) throws SQLException {
    boolean status = false;
    PreparedStatement ps = con.prepareStatement(INSERT_INTO_BED_DETAILS);

    OrgMasterDao orgDao = new OrgMasterDao();
    ArrayList<Hashtable<String, String>> lis = (ArrayList) orgDao.getAllOrgs();
    Iterator<Hashtable<String, String>> it1 = lis.iterator();
    String selectedOrgId = al.get(0).getOrgId();

    while (it1.hasNext()) {
      Hashtable<String, String> ht1 = it1.next();
      String orgId = ht1.get("ORG_ID");
      Iterator<BedDetails> it = al.iterator();
      while (it.hasNext()) {
        BedDetails bd = it.next();

        ps.setString(1, bd.getBedType());
        ps.setDouble(2, bd.getBedCharge());
        ps.setDouble(3, bd.getNursingCharge());
        ps.setDouble(4, bd.getIntialCharge());
        ps.setDouble(5, bd.getDutyCharge());
        ps.setDouble(6, bd.getProfCharge());
        ps.setString(7, orgId);
        ps.setString(8, bd.getBedStatus());
        ps.setDouble(9, bd.getLuxaryCharge());
        ps.setDouble(10, bd.getHourlyCharge());
        ps.setDouble(11, bd.getBedChargeDiscount());
        ps.setDouble(12, bd.getNursingChargeDiscount());
        ps.setDouble(13, bd.getDutyChargeDiscount());
        ps.setDouble(14, bd.getProfChargeDiscount());
        ps.setDouble(15, bd.getHourlyChargeDiscount());
        ps.setDouble(16, bd.getDaycareSlab1Charge());
        ps.setDouble(17, bd.getDaycareSlab2Charge());
        ps.setDouble(18, bd.getDaycareSlab3Charge());
        ps.setDouble(19, bd.getDaycareSlab1ChargeDiscount());
        ps.setDouble(20, bd.getDaycareSlab2ChargeDiscount());
        ps.setDouble(21, bd.getDaycareSlab3ChargeDiscount());
        if (orgId.equals(selectedOrgId)) {
          ps.setString(22, bd.getOrgItemCode());
          ps.setString(23, bd.getCodeType());
        } else {
          ps.setString(22, null);
          ps.setString(23, null);
        }
        ps.addBatch();

      }
    }

    int[] aval = ps.executeBatch();
    status = DataBaseUtil.checkBatchUpdates(aval);

    ps.close();
    return status;
  }

  /**
   * Addd new icu bed.
   *
   * @param con
   *          the con
   * @param al
   *          the al
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean adddNewIcuBed(Connection con, ArrayList<BedDetails> al) throws SQLException {
    boolean status = false;
    PreparedStatement ps = con.prepareStatement(INSERT_INTO_ICU_CHARGES);

    OrgMasterDao orgDao = new OrgMasterDao();
    ArrayList<Hashtable<String, String>> lis = (ArrayList) orgDao.getAllOrgs();
    Iterator<Hashtable<String, String>> it1 = lis.iterator();
    String selectedOrgId = al.get(0).getOrgId();

    while (it1.hasNext()) {
      Hashtable<String, String> ht1 = it1.next();
      String orgId = ht1.get("ORG_ID");
      BedDetails generalBedDetails = null;

      Iterator<BedDetails> it = al.iterator();

      while (it.hasNext()) {
        BedDetails bd = it.next();
        // addIcuBedOwnCharges(bd,generalBedDetails,al);
        ps.setString(1, bd.getBaseBed());
        ps.setDouble(2, bd.getBedCharge());
        ps.setDouble(3, bd.getNursingCharge());
        ps.setDouble(4, bd.getIntialCharge());
        ps.setDouble(5, bd.getDutyCharge());
        ps.setDouble(6, bd.getProfCharge());
        ps.setString(7, orgId);
        ps.setString(8, bd.getBedStatus());
        ps.setDouble(9, bd.getLuxaryCharge());
        ps.setString(10, bd.getBedType());
        ps.setDouble(11, bd.getHourlyCharge());
        ps.setDouble(12, bd.getBedChargeDiscount());
        ps.setDouble(13, bd.getNursingChargeDiscount());
        ps.setDouble(14, bd.getDutyChargeDiscount());
        ps.setDouble(15, bd.getProfChargeDiscount());
        ps.setDouble(16, bd.getHourlyChargeDiscount());
        ps.setDouble(17, bd.getDaycareSlab1Charge());
        ps.setDouble(18, bd.getDaycareSlab2Charge());
        ps.setDouble(19, bd.getDaycareSlab3Charge());
        ps.setDouble(20, bd.getDaycareSlab1ChargeDiscount());
        ps.setDouble(21, bd.getDaycareSlab2ChargeDiscount());
        ps.setDouble(22, bd.getDaycareSlab3ChargeDiscount());
        if (orgId.equals(selectedOrgId)) {
          ps.setString(23, bd.getOrgItemCode());
          ps.setString(24, bd.getCodeType());
        } else {
          ps.setString(23, null);
          ps.setString(24, null);
        }

        ps.addBatch();

      }

    }

    int[] aval = ps.executeBatch();
    status = DataBaseUtil.checkBatchUpdates(aval);

    ps.close();

    return status;
  }

  /**
   * Adds the ICU bed own charges.
   *
   * @param generalBedDetails
   *          the general bed details
   * @param al
   *          the al
   * @throws Exception
   *           the exception
   */
  public void addIcuBedOwnCharges(BedDetails generalBedDetails, List al) throws Exception {
    for (int i = 0; i < al.size(); i++) {
      BedDetails bd = (BedDetails) al.get(i);
      BeanUtils.copyProperties(generalBedDetails, bd);
      if (bd.getBaseBed().equals("GENERAL")) {
        generalBedDetails.setBaseBed(bd.getBedType());
        al.add(al.size(), generalBedDetails);
        break;
      }
    }
  }

  /** The Constant INSERT_INTO_REGISTRATION_PLUS. */
  private static final String INSERT_INTO_REGISTRATION_PLUS =
      "INSERT INTO registration_charges(org_id, bed_type, ip_reg_charge,"
      + " op_reg_charge, gen_reg_charge, reg_renewal_charge, mrcharge, ip_mlccharge,"
      + " op_mlccharge)(SELECT org_id, ?, round(ip_reg_charge + ?),"
      + " round(op_reg_charge + ?), round(gen_reg_charge + ?), round(reg_renewal_charge + ?),"
      + " round(mrcharge + ?), round(ip_mlccharge + ?)," + " round(op_mlccharge + ?) FROM "
      + " registration_charges WHERE bed_type = ?)";

  /** The Constant INSERT_INTO_REGISTRATION_MINUS. */
  private static final String INSERT_INTO_REGISTRATION_MINUS =
      "INSERT INTO registration_charges(org_id, bed_type, ip_reg_charge,"
      + " op_reg_charge, gen_reg_charge, reg_renewal_charge, mrcharge, ip_mlccharge,"
      + " op_mlccharge)(SELECT org_id, ?, round(ip_reg_charge - ?),"
      + " round(op_reg_charge - ?), round(gen_reg_charge - ?), round(reg_renewal_charge - ?),"
      + " round(mrcharge - ?)," + " round(ip_mlccharge - ?), round(op_mlccharge - ?) FROM "
      + " registration_charges WHERE bed_type = ?)";

  /** The Constant INSERT_INTO_REGISTRATION_BY. */
  private static final String INSERT_INTO_REGISTRATION_BY =
      "INSERT INTO registration_charges(org_id, bed_type, ip_reg_charge,"
      + " op_reg_charge, gen_reg_charge, reg_renewal_charge, mrcharge, ip_mlccharge,"
      + " op_mlccharge)(SELECT org_id, ?, doroundvarying(ip_reg_charge,?,?),"
      + " doroundvarying(op_reg_charge,?,?), doroundvarying(gen_reg_charge,?,?),"
      + " doroundvarying(reg_renewal_charge,?,?), doroundvarying(mrcharge,?,?),"
      + " doroundvarying(ip_mlccharge,?,?), doroundvarying(op_mlccharge,?,?)"
      + " FROM  registration_charges WHERE bed_type = ?)";

  /**
   * Adds the bed for registration charges.
   *
   * @param con
   *          the con
   * @param newBedType
   *          the new bed type
   * @param baseBedForCharges
   *          the base bed for charges
   * @param varianceType
   *          the variance type
   * @param varianceBy
   *          the variance by
   * @param varianceValue
   *          the variance value
   * @param useValue
   *          the use value
   * @param nearstRoundOfValue
   *          the nearst round of value
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean addBedForRegistrationCharges(Connection con, String newBedType,
      String baseBedForCharges, String varianceType, Double varianceBy, Double varianceValue,
      boolean useValue, Double nearstRoundOfValue) throws SQLException {

    boolean status = false;
    PreparedStatement pstmt = null;
    if (useValue) {
      pstmt = varianceType.equals("Incr") ? con.prepareStatement(INSERT_INTO_REGISTRATION_PLUS)
          : con.prepareStatement(INSERT_INTO_REGISTRATION_MINUS);

      pstmt.setString(1, newBedType);
      pstmt.setDouble(2, varianceValue);
      pstmt.setDouble(3, varianceValue);
      pstmt.setDouble(4, varianceValue);
      pstmt.setDouble(5, varianceValue);
      pstmt.setDouble(6, varianceValue);
      pstmt.setDouble(7, varianceValue);
      pstmt.setDouble(8, varianceValue);
      pstmt.setString(9, baseBedForCharges);

      status = pstmt.executeUpdate() >= 0;

    } else {

      pstmt = con.prepareStatement(INSERT_INTO_REGISTRATION_BY);
      if (!varianceType.equals("Incr")) {
        varianceBy = new Double(-varianceBy);
      }

      pstmt.setString(1, newBedType);

      pstmt.setBigDecimal(2, new BigDecimal(varianceBy));
      pstmt.setBigDecimal(3, new BigDecimal(nearstRoundOfValue));

      pstmt.setBigDecimal(4, new BigDecimal(varianceBy));
      pstmt.setBigDecimal(5, new BigDecimal(nearstRoundOfValue));

      pstmt.setBigDecimal(6, new BigDecimal(varianceBy));
      pstmt.setBigDecimal(7, new BigDecimal(nearstRoundOfValue));

      pstmt.setBigDecimal(8, new BigDecimal(varianceBy));
      pstmt.setBigDecimal(9, new BigDecimal(nearstRoundOfValue));

      pstmt.setBigDecimal(10, new BigDecimal(varianceBy));
      pstmt.setBigDecimal(11, new BigDecimal(nearstRoundOfValue));

      pstmt.setBigDecimal(12, new BigDecimal(varianceBy));
      pstmt.setBigDecimal(13, new BigDecimal(nearstRoundOfValue));

      pstmt.setBigDecimal(14, new BigDecimal(varianceBy));
      pstmt.setBigDecimal(15, new BigDecimal(nearstRoundOfValue));

      pstmt.setString(16, baseBedForCharges);

      status = pstmt.executeUpdate() >= 0;

    }
    if (pstmt != null) {
      pstmt.close();
    }

    return status;
  }

  /** The Constant INSERT_INTO_DOCTORS_PLUS. */
  private static final String INSERT_INTO_DOCTORS_PLUS =
      "INSERT INTO doctor_consultation_charge(doctor_name,bed_type,doctor_ip_charge,"
      + " night_ip_charge,ward_ip_charge,"
      + "organization,ot_charge,co_surgeon_charge,assnt_surgeon_charge)(SELECT doctor_name,?,"
      + " round(doctor_ip_charge+?),round(night_ip_charge + ?),round(ward_ip_charge + ?),"
      + "organization,round(ot_charge + ?),round(co_surgeon_charge + ?),"
      + " round(assnt_surgeon_charge + ?) FROM"
      + " doctor_consultation_charge WHERE  bed_type = ?)";

  /** The Constant INSERT_INTO_DOCTORS_MINUS. */
  private static final String INSERT_INTO_DOCTORS_MINUS =
      "INSERT INTO doctor_consultation_charge(doctor_name,bed_type,doctor_ip_charge,"
      + " night_ip_charge,ward_ip_charge,"
      + "organization,ot_charge,co_surgeon_charge,assnt_surgeon_charge)(SELECT doctor_name,?,"
      + " round(doctor_ip_charge - ?),round(night_ip_charge - ?),round(ward_ip_charge - ?),"
      + "organization,round(ot_charge - ?),round(co_surgeon_charge - ?),"
      + " round(assnt_surgeon_charge - ?) FROM"
      + " doctor_consultation_charge WHERE  bed_type = ?)";

  /** The Constant INSER_INTO_DOCTORS_BY. */
  private static final String INSER_INTO_DOCTORS_BY =
      "INSERT INTO doctor_consultation_charge(doctor_name,bed_type,doctor_ip_charge,"
      + " night_ip_charge,ward_ip_charge,"
      + "organization,ot_charge,co_surgeon_charge,assnt_surgeon_charge)(SELECT doctor_name,?,"
      + " doroundvarying(doctor_ip_charge,?,?),doroundvarying(night_ip_charge,?,?),"
      + " doroundvarying(ward_ip_charge,?,?),"
      + "organization,doroundvarying(ot_charge,?,?),doroundvarying(co_surgeon_charge,?,?),"
      + " doroundvarying(assnt_surgeon_charge,?,?) FROM"
      + " doctor_consultation_charge WHERE  bed_type = ?)";

  /**
   * Adds the bedfor doctors.
   *
   * @param con
   *          the con
   * @param newBedType
   *          the new bed type
   * @param baseBedForCharges
   *          the base bed for charges
   * @param varianceType
   *          the variance type
   * @param varianceBy
   *          the variance by
   * @param varianceValue
   *          the variance value
   * @param useValue
   *          the use value
   * @param nearstRoundOfValue
   *          the nearst round of value
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean addBedforDoctors(Connection con, String newBedType, String baseBedForCharges,
      String varianceType, Double varianceBy, Double varianceValue, boolean useValue,
      Double nearstRoundOfValue) throws SQLException {
    boolean status = false;
    PreparedStatement ps = null;
    if (useValue) {
      if (varianceType.equals("Incr")) {
        ps = con.prepareStatement(INSERT_INTO_DOCTORS_PLUS);
      } else {
        ps = con.prepareStatement(INSERT_INTO_DOCTORS_MINUS);
      }

      ps.setString(1, newBedType);
      ps.setDouble(2, varianceValue);
      ps.setDouble(3, varianceValue);
      ps.setDouble(4, varianceValue);
      ps.setDouble(5, varianceValue);
      ps.setDouble(6, varianceValue);
      ps.setDouble(7, varianceValue);
      ps.setString(8, baseBedForCharges);

      int ival = ps.executeUpdate();
      logger.debug(Integer.toString(ival));
      if (ival >= 0) {
        status = true;
      }

    } else {
      ps = con.prepareStatement(INSER_INTO_DOCTORS_BY);
      if (!varianceType.equals("Incr")) {
        varianceBy = new Double(-varianceBy);
      }

      ps.setString(1, newBedType);

      ps.setBigDecimal(2, new BigDecimal(varianceBy));
      ps.setBigDecimal(3, new BigDecimal(nearstRoundOfValue));

      ps.setBigDecimal(4, new BigDecimal(varianceBy));
      ps.setBigDecimal(5, new BigDecimal(nearstRoundOfValue));

      ps.setBigDecimal(6, new BigDecimal(varianceBy));
      ps.setBigDecimal(7, new BigDecimal(nearstRoundOfValue));

      ps.setBigDecimal(8, new BigDecimal(varianceBy));
      ps.setBigDecimal(9, new BigDecimal(nearstRoundOfValue));

      ps.setBigDecimal(10, new BigDecimal(varianceBy));
      ps.setBigDecimal(11, new BigDecimal(nearstRoundOfValue));

      ps.setBigDecimal(12, new BigDecimal(varianceBy));
      ps.setBigDecimal(13, new BigDecimal(nearstRoundOfValue));

      ps.setString(14, baseBedForCharges);

      int ival = ps.executeUpdate();
      logger.debug(Integer.toString(ival));
      if (ival >= 0) {
        status = true;
      }
    }
    ps.close();

    return status;
  }

  /** The Constant INSERT_INTO_OPERATIONS_PLUS. */
  private static final String INSERT_INTO_OPERATIONS_PLUS =
      "INSERT INTO operation_charges(op_id,org_id,bed_type,"
      + "surg_asstance_charge,surgeon_charge,anesthetist_charge)(SELECT op_id,org_id,?,"
      + "round(surg_asstance_charge+?),round(surgeon_charge+?),round(anesthetist_charge+?) "
      + "FROM operation_charges WHERE bed_type=? and org_id = ?)";

  /** The Constant INSERT_INTO_OPERATIONS_MINUS. */
  private static final String INSERT_INTO_OPERATIONS_MINUS =
      "INSERT INTO operation_charges(op_id,org_id,bed_type,"
      + "surg_asstance_charge,surgeon_charge,anesthetist_charge)(SELECT op_id,org_id,?,"
      + "round(surg_asstance_charge-?),round(surgeon_charge-?),round(anesthetist_charge-?) "
      + "FROM operation_charges WHERE bed_type=? and org_id=?)";

  /** The Constant INSERT_INTO_OPERATIONS_BY. */
  private static final String INSERT_INTO_OPERATIONS_BY =
      "INSERT INTO operation_charges(op_id,org_id,bed_type,"
      + "surg_asstance_charge,surgeon_charge,anesthetist_charge)(SELECT op_id,org_id,?,"
      + " doroundvarying(surg_asstance_charge,?,?), " + " doroundvarying(surgeon_charge,?,?), "
      + " doroundvarying(anesthetist_charge,?,?) "
      + " FROM  operation_charges WHERE  bed_type=? and org_id=?)";

  /**
   * Adds the bedfor operations.
   *
   * @param newBedType
   *          the new bed type
   * @param baseBedForCharges
   *          the base bed for charges
   * @param varianceType
   *          the variance type
   * @param varianceBy
   *          the variance by
   * @param varianceValue
   *          the variance value
   * @param useValue
   *          the use value
   * @param nearstRoundOfValue
   *          the nearst round of value
   * @param userName
   *          the user name
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws Exception
   *           the exception
   */
  public boolean addBedforOperations(String newBedType, String baseBedForCharges,
      String varianceType, Double varianceBy, Double varianceValue, boolean useValue,
      Double nearstRoundOfValue, String userName) throws SQLException, IOException, Exception {
    GenericDAO.alterTrigger("DISABLE", "operation_charges", "z_operation_charges_audit_trigger");

    Connection con = DataBaseUtil.getConnection(60);
    con.setAutoCommit(false);
    PreparedStatement ps = null;

    OrgMasterDao orgDao = new OrgMasterDao();
    List<BasicDynaBean> orgList = orgDao.listAll();
    boolean flag = true;
    try {
      /*
       * inserting for all organizations at a time causing performance issue, so
       * because of this, at one time we are inserting only one organization charges.
       */
      for (BasicDynaBean orgbean : orgList) {
        flag = false;
        if (useValue) {
          if (varianceType.equals("Incr")) {
            ps = con.prepareStatement(INSERT_INTO_OPERATIONS_PLUS);
          } else {
            ps = con.prepareStatement(INSERT_INTO_OPERATIONS_MINUS);
          }

          ps.setString(1, newBedType);
          ps.setBigDecimal(2, new BigDecimal(varianceValue));
          ps.setBigDecimal(3, new BigDecimal(varianceValue));
          ps.setBigDecimal(4, new BigDecimal(varianceValue));
          ps.setString(5, baseBedForCharges);
          ps.setString(6, (String) orgbean.get("org_id"));

          flag = ps.executeUpdate() >= 0;
          if (!flag) {
            logger.debug(
                "failed to insert operation charges for organization: " + orgbean.get("org_name"));
            break;
          }

        } else {
          ps = con.prepareStatement(INSERT_INTO_OPERATIONS_BY);
          if (!varianceType.equals("Incr")) {
            varianceBy = new Double(-varianceBy);
          }
          ps.setString(1, newBedType);
          ps.setBigDecimal(2, new BigDecimal(varianceBy));
          ps.setBigDecimal(3, new BigDecimal(nearstRoundOfValue));
          ps.setBigDecimal(4, new BigDecimal(varianceBy));
          ps.setBigDecimal(5, new BigDecimal(nearstRoundOfValue));
          ps.setBigDecimal(6, new BigDecimal(varianceBy));
          ps.setBigDecimal(7, new BigDecimal(nearstRoundOfValue));
          ps.setString(8, baseBedForCharges);
          ps.setString(9, (String) orgbean.get("org_id"));

          flag = ps.executeUpdate() >= 0;
          if (!flag) {
            logger.debug(
                "failed to insert operation charges for organization: " + orgbean.get("org_name"));
            break;
          }
        }
      }

      flag &= new AuditLogDao("Master", "operation_charges_audit_log").logMasterChange(con,
          userName, "INSERT", "bed_type", newBedType);
    } catch (Exception ex) {
      logger.error("", ex);
      flag = false;
      throw ex;
    } finally {
      if (flag) {
        con.commit();
      } else {
        con.rollback();
      }

      DataBaseUtil.closeConnections(con, ps);
    }
    return flag;
  }

  /** The Constant INSERT_INTO_THEATRES_PLUS. */
  private static final String INSERT_INTO_THEATRES_PLUS =
      "INSERT INTO theatre_charges(theatre_id,org_id,bed_type,"
      + "daily_charge,min_charge,incr_charge,slab_1_charge,tax)(SELECT theatre_id,org_id,?,"
      + "round(daily_charge+?),round(min_charge + ?),round(incr_charge + ?),"
      + " round(slab_1_charge + ?),tax FROM theatre_charges WHERE bed_type=? )";

  /** The Constant INSERT_INTO_THEATRES_MINUS. */
  private static final String INSERT_INTO_THEATRES_MINUS =
      "INSERT INTO theatre_charges(theatre_id,org_id,bed_type,"
      + "daily_charge,min_charge,incr_charge,slab_1_charge, tax)(SELECT theatre_id,org_id,?,"
      + "round(daily_charge - ?),round(min_charge - ?),round(incr_charge - ?),"
      + " round(slab_1_charge - ?),tax FROM theatre_charges WHERE bed_type=? )";

  /** The Constant INSERT_INTO_THEATRES_BY. */
  private static final String INSERT_INTO_THEATRES_BY =
      "INSERT INTO theatre_charges(theatre_id,org_id,bed_type,"
      + "daily_charge,min_charge,incr_charge,slab_1_charge, tax)(SELECT theatre_id,org_id,?,"
      + "doroundvarying(daily_charge,?,?),doroundvarying(min_charge,?,?),"
      + " doroundvarying(incr_charge,?,?),doroundvarying(slab_1_charge,?,?), tax "
      + " FROM theatre_charges WHERE bed_type=? )";

  /**
   * Adds the bed for theatres.
   *
   * @param con
   *          the con
   * @param newBedType
   *          the new bed type
   * @param baseBedForCharges
   *          the base bed for charges
   * @param varianceType
   *          the variance type
   * @param varianceBy
   *          the variance by
   * @param varianceValue
   *          the variance value
   * @param useValue
   *          the use value
   * @param nearstRoundOfValue
   *          the nearst round of value
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean addBedForTheatres(Connection con, String newBedType, String baseBedForCharges,
      String varianceType, Double varianceBy, Double varianceValue, boolean useValue,
      Double nearstRoundOfValue) throws SQLException {

    boolean status = false;
    PreparedStatement ps = null;
    if (useValue) {
      if (varianceType.equals("Incr")) {
        ps = con.prepareStatement(INSERT_INTO_THEATRES_PLUS);
      } else {
        ps = con.prepareStatement(INSERT_INTO_THEATRES_MINUS);
      }
      ps.setString(1, newBedType);
      ps.setBigDecimal(2, new BigDecimal(varianceValue));
      ps.setBigDecimal(3, new BigDecimal(varianceValue));
      ps.setBigDecimal(4, new BigDecimal(varianceValue));
      ps.setBigDecimal(5, new BigDecimal(varianceValue));
      ps.setString(6, baseBedForCharges);

      int ival = ps.executeUpdate();
      logger.debug(Integer.toString(ival));
      if (ival >= 0) {
        status = true;
      }

    } else {
      ps = con.prepareStatement(INSERT_INTO_THEATRES_BY);
      ps.setString(1, newBedType);
      if (!varianceType.equals("Incr")) {
        varianceBy = new Double(-varianceBy);
      }

      ps.setBigDecimal(2, new BigDecimal(varianceBy));
      ps.setBigDecimal(3, new BigDecimal(nearstRoundOfValue));
      ps.setBigDecimal(4, new BigDecimal(varianceBy));
      ps.setBigDecimal(5, new BigDecimal(nearstRoundOfValue));
      ps.setBigDecimal(6, new BigDecimal(varianceBy));
      ps.setBigDecimal(7, new BigDecimal(nearstRoundOfValue));
      ps.setBigDecimal(8, new BigDecimal(varianceBy));
      ps.setBigDecimal(9, new BigDecimal(nearstRoundOfValue));
      ps.setString(10, baseBedForCharges);

      int ival = ps.executeUpdate();
      logger.debug(Integer.toString(ival));
      if (ival >= 0) {
        status = true;
      }
    }

    ps.close();
    return status;
  }

  /** The Constant INSERT_INTO_EQUIPMENTS_PLUS. */
  private static final String INSERT_INTO_EQUIPMENTS_PLUS =
      "INSERT INTO equipement_charges(equip_id,org_id,bed_type,"
      + "daily_charge,min_charge,incr_charge,slab_1_charge,tax)(SELECT equip_id,org_id,?,"
      + " round(daily_charge + ?),round(min_charge + ?),"
      + "round(incr_charge+?),round(slab_1_charge+?),tax "
      + " FROM equipement_charges where bed_type=? )";

  /** The Constant INSERT_INTO_EQUIPMENTS_MINUS. */
  private static final String INSERT_INTO_EQUIPMENTS_MINUS =
      "INSERT INTO equipement_charges(equip_id,org_id,bed_type,"
      + "daily_charge,min_charge,incr_charge,slab_1_charge,tax)(SELECT equip_id,org_id,?,"
      + " round(daily_charge - ?),round(min_charge - ?),"
      + "round(incr_charge - ?),round(slab_1_charge - ?),tax"
      + " FROM equipement_charges where bed_type=? )";

  /** The Constant INSERT_INTO_EQUIPMENTS_BY. */
  private static final String INSERT_INTO_EQUIPMENTS_BY =
      "INSERT INTO equipement_charges(equip_id,org_id,bed_type,"
      + "daily_charge,min_charge,incr_charge,slab_1_charge,tax)(SELECT equip_id,org_id,?,"
      + " doroundvarying(daily_charge,?,?),"
      + "doroundvarying(min_charge,?,?),doroundvarying(incr_charge,?,?),"
      + " doroundvarying(slab_1_charge,?,?),tax FROM equipement_charges WHERE bed_type=?)";

  /**
   * Adds the bed for equipments.
   *
   * @param con
   *          the con
   * @param newBedType
   *          the new bed type
   * @param baseBedForCharges
   *          the base bed for charges
   * @param varianceType
   *          the variance type
   * @param varianceBy
   *          the variance by
   * @param varianceValue
   *          the variance value
   * @param useValue
   *          the use value
   * @param nearstRoundOfValue
   *          the nearst round of value
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean addBedForEquipments(Connection con, String newBedType, String baseBedForCharges,
      String varianceType, Double varianceBy, Double varianceValue, boolean useValue,
      Double nearstRoundOfValue) throws SQLException {
    boolean status = false;

    PreparedStatement ps = null;
    if (useValue) {
      if (varianceType.equals("Incr")) {
        ps = con.prepareStatement(INSERT_INTO_EQUIPMENTS_PLUS);
      } else {
        ps = con.prepareStatement(INSERT_INTO_EQUIPMENTS_MINUS);
      }

      ps.setString(1, newBedType);
      ps.setBigDecimal(2, new BigDecimal(varianceValue));
      ps.setBigDecimal(3, new BigDecimal(varianceValue));
      ps.setBigDecimal(4, new BigDecimal(varianceValue));
      ps.setBigDecimal(5, new BigDecimal(varianceValue));
      ps.setString(6, baseBedForCharges);

      int ival = ps.executeUpdate();
      logger.debug(Integer.toString(ival));
      if (ival >= 0) {
        status = true;
      }
    } else {
      ps = con.prepareStatement(INSERT_INTO_EQUIPMENTS_BY);
      ps.setString(1, newBedType);
      if (!varianceType.equals("Incr")) {
        varianceBy = new Double(-varianceBy);
      }
      ps.setBigDecimal(2, new BigDecimal(varianceBy));
      ps.setBigDecimal(3, new BigDecimal(nearstRoundOfValue));
      ps.setBigDecimal(4, new BigDecimal(varianceBy));
      ps.setBigDecimal(5, new BigDecimal(nearstRoundOfValue));
      ps.setBigDecimal(6, new BigDecimal(varianceBy));
      ps.setBigDecimal(7, new BigDecimal(nearstRoundOfValue));
      ps.setBigDecimal(8, new BigDecimal(varianceBy));
      ps.setBigDecimal(9, new BigDecimal(nearstRoundOfValue));
      ps.setString(10, baseBedForCharges);

      int ival = ps.executeUpdate();
      logger.debug(Integer.toString(ival));
      if (ival >= 0) {
        status = true;
      }

    }
    ps.close();

    return status;
  }

  /** The Constant INSERT_INTO_ANESTHESIA_TYPE__PLUS. */
  private static final String INSERT_INTO_ANESTHESIA_TYPE__PLUS =
      "INSERT INTO anesthesia_type_charges(anesthesia_type_id,org_id,bed_type,"
      + " min_charge,incr_charge,slab_1_charge)(SELECT anesthesia_type_id,org_id,?,"
      + " round(min_charge + ?),"
      + " round(incr_charge+?),round(slab_1_charge+?) FROM anesthesia_type_charges "
      + " where bed_type=? )";

  /** The Constant INSERT_INTO_ANESTHESIA_TYPE_MINUS. */
  private static final String INSERT_INTO_ANESTHESIA_TYPE_MINUS =
      "INSERT INTO anesthesia_type_charges(anesthesia_type_id,org_id,bed_type,"
      + "min_charge,incr_charge,slab_1_charge)(SELECT anesthesia_type_id,org_id,?,"
      + " round(min_charge - ?),"
      + "round(incr_charge - ?),round(slab_1_charge - ?) FROM anesthesia_type_charges"
      + " where bed_type=? )";

  /** The Constant INSERT_INTO_ANESTHESIA_TYPE_BY. */
  private static final String INSERT_INTO_ANESTHESIA_TYPE_BY =
      "INSERT INTO anesthesia_type_charges(anesthesia_type_id,org_id,bed_type,"
      + "min_charge,incr_charge,slab_1_charge)(SELECT anesthesia_type_id,org_id,?, "
      + "doroundvarying(min_charge,?,?),doroundvarying(incr_charge,?,?),"
      + " doroundvarying(slab_1_charge,?,?) FROM anesthesia_type_charges WHERE bed_type=?)";

  /**
   * Adds the bed for anestesia types.
   *
   * @param con
   *          the con
   * @param newBedType
   *          the new bed type
   * @param baseBedForCharges
   *          the base bed for charges
   * @param varianceType
   *          the variance type
   * @param varianceBy
   *          the variance by
   * @param varianceValue
   *          the variance value
   * @param useValue
   *          the use value
   * @param nearstRoundOfValue
   *          the nearst round of value
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean addBedForAnestesiaTypes(Connection con, String newBedType,
      String baseBedForCharges, String varianceType, Double varianceBy, Double varianceValue,
      boolean useValue, Double nearstRoundOfValue) throws SQLException {
    boolean status = false;

    PreparedStatement ps = null;
    if (useValue) {
      if (varianceType.equals("Incr")) {
        ps = con.prepareStatement(INSERT_INTO_ANESTHESIA_TYPE__PLUS);
      } else {
        ps = con.prepareStatement(INSERT_INTO_ANESTHESIA_TYPE_MINUS);
      }

      ps.setString(1, newBedType);
      ps.setBigDecimal(2, new BigDecimal(varianceValue));
      ps.setBigDecimal(3, new BigDecimal(varianceValue));
      ps.setBigDecimal(4, new BigDecimal(varianceValue));
      ps.setString(5, baseBedForCharges);

      int ival = ps.executeUpdate();
      logger.debug(Integer.toString(ival));
      if (ival >= 0) {
        status = true;
      }
    } else {
      ps = con.prepareStatement(INSERT_INTO_ANESTHESIA_TYPE_BY);
      ps.setString(1, newBedType);
      if (!varianceType.equals("Incr")) {
        varianceBy = new Double(-varianceBy);
      }
      ps.setBigDecimal(2, new BigDecimal(varianceBy));
      ps.setBigDecimal(3, new BigDecimal(nearstRoundOfValue));
      ps.setBigDecimal(4, new BigDecimal(varianceBy));
      ps.setBigDecimal(5, new BigDecimal(nearstRoundOfValue));
      ps.setBigDecimal(6, new BigDecimal(varianceBy));
      ps.setBigDecimal(7, new BigDecimal(nearstRoundOfValue));
      ps.setString(8, baseBedForCharges);

      int ival = ps.executeUpdate();
      logger.debug(Integer.toString(ival));
      if (ival >= 0) {
        status = true;
      }

    }
    ps.close();

    return status;
  }

  /** The Constant INSERT_INTO_SERVICES_PLUS. */
  private static final String INSERT_INTO_SERVICES_PLUS =
      "INSERT INTO service_master_charges(service_id,"
      + "bed_type,org_id,unit_charge)(SELECT service_id,?,org_id,"
      + " round(unit_charge+?) FROM service_master_charges" + " WHERE bed_type=? )";

  /** The Constant INSERT_INTO_SERVICES_MINUS. */
  private static final String INSERT_INTO_SERVICES_MINUS =
      "INSERT INTO service_master_charges(service_id,"
      + "bed_type,org_id,unit_charge)(SELECT service_id,?,org_id,"
      + " round(unit_charge - ?) FROM service_master_charges" + " WHERE bed_type=? )";

  /** The Constant INSERT_INTO_SERVICES_BY. */
  private static final String INSERT_INTO_SERVICES_BY =
      "INSERT INTO service_master_charges(service_id,"
      + " bed_type,org_id,unit_charge)(SELECT service_id,?,org_id, "
      + " doroundvarying(unit_charge,?,?) " + " FROM service_master_charges"
      + " WHERE bed_type=? )";

  /**
   * Adds the bed for services.
   *
   * @param newBedType
   *          the new bed type
   * @param baseBedForCharges
   *          the base bed for charges
   * @param varianceType
   *          the variance type
   * @param varianceBy
   *          the variance by
   * @param varianceValue
   *          the variance value
   * @param useValue
   *          the use value
   * @param nearstRoundOfValue
   *          the nearst round of value
   * @param userName
   *          the user name
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public boolean addBedForServices(String newBedType, String baseBedForCharges, String varianceType,
      Double varianceBy, Double varianceValue, boolean useValue, Double nearstRoundOfValue,
      String userName) throws SQLException, IOException {
    boolean status = false;
    Connection con = DataBaseUtil.getConnection(60);
    con.setAutoCommit(false);
    PreparedStatement ps = null;

    GenericDAO.alterTrigger("DISABLE", "service_master_charges",
        "z_services_charges_audit_trigger");
    if (useValue) {
      if (varianceType.equals("Incr")) {
        ps = con.prepareStatement(INSERT_INTO_SERVICES_PLUS);
        ps.setString(1, newBedType);
        ps.setDouble(2, varianceValue);
        ps.setString(3, baseBedForCharges);
        int ival = ps.executeUpdate();
        logger.debug(Integer.toString(ival));
        if (ival >= 0) {
          status = true;
        }
      } else {
        ps = con.prepareStatement(INSERT_INTO_SERVICES_MINUS);
        ps.setString(1, newBedType);
        ps.setDouble(2, varianceValue);
        ps.setString(3, baseBedForCharges);
        int ival = ps.executeUpdate();
        logger.debug(Integer.toString(ival));
        if (ival >= 0) {
          status = true;
        }
      }
    } else {
      ps = con.prepareStatement(INSERT_INTO_SERVICES_BY);
      ps.setString(1, newBedType);
      if (!varianceType.equals("Incr")) {
        varianceBy = new Double(-varianceBy);
      }
      ps.setBigDecimal(2, new BigDecimal(varianceBy));
      ps.setBigDecimal(3, new BigDecimal(nearstRoundOfValue));
      ps.setString(4, baseBedForCharges);

      int ival = ps.executeUpdate();
      logger.debug(Integer.toString(ival));
      if (ival >= 0) {
        status = true;
      }
    }

    if (status) {
      con.commit();
    } else {
      con.rollback();
    }

    status &= new AuditLogDao("Master", "service_master_charges_audit_log").logMasterChange(con,
        userName, "INSERT", "bed_type", newBedType);
    DataBaseUtil.closeConnections(con, ps);

    return status;
  }

  /** The Constant INSERT_INTO_DYNA_PLUS. */
  private static final String INSERT_INTO_DYNA_PLUS =
      "INSERT INTO dyna_package_charges(dyna_package_id,"
      + "bed_type,org_id,charge)(SELECT dyna_package_id,?,"
      + " org_id, round(charge+?) FROM dyna_package_charges" + " WHERE bed_type=? )";

  /** The Constant INSERT_INTO_DYNA_MINUS. */
  private static final String INSERT_INTO_DYNA_MINUS =
      "INSERT INTO dyna_package_charges(dyna_package_id,"
      + "bed_type,org_id,charge)(SELECT dyna_package_id,?,"
      + " org_id, round(charge - ?) FROM dyna_package_charges" + " WHERE bed_type=? )";

  /** The Constant INSERT_INTO_DYNA_BY. */
  private static final String INSERT_INTO_DYNA_BY =
      "INSERT INTO dyna_package_charges(dyna_package_id,"
      + "bed_type,org_id,charge)(SELECT dyna_package_id,?,org_id, "
      + " doroundvarying(charge,?,?) FROM dyna_package_charges" + " WHERE bed_type=? )";

  /**
   * Adds the bed for dyna packages.
   *
   * @param con
   *          the con
   * @param newBedType
   *          the new bed type
   * @param baseBedForCharges
   *          the base bed for charges
   * @param varianceType
   *          the variance type
   * @param varianceBy
   *          the variance by
   * @param varianceValue
   *          the variance value
   * @param useValue
   *          the use value
   * @param nearstRoundOfValue
   *          the nearst round of value
   * @param userName
   *          the user name
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public boolean addBedForDynaPackages(Connection con, String newBedType, String baseBedForCharges,
      String varianceType, Double varianceBy, Double varianceValue, boolean useValue,
      Double nearstRoundOfValue, String userName) throws SQLException, IOException {
    boolean status = false;
    PreparedStatement ps = null;

    if (useValue) {
      if (varianceType.equals("Incr")) {
        ps = con.prepareStatement(INSERT_INTO_DYNA_PLUS);
        ps.setString(1, newBedType);
        ps.setDouble(2, varianceValue);
        ps.setString(3, baseBedForCharges);
        int ival = ps.executeUpdate();
        logger.debug(Integer.toString(ival));
        if (ival >= 0) {
          status = true;
        }
      } else {
        ps = con.prepareStatement(INSERT_INTO_DYNA_MINUS);
        ps.setString(1, newBedType);
        ps.setDouble(2, varianceValue);
        ps.setString(3, baseBedForCharges);
        int ival = ps.executeUpdate();
        logger.debug(Integer.toString(ival));
        if (ival >= 0) {
          status = true;
        }
      }
    } else {
      ps = con.prepareStatement(INSERT_INTO_DYNA_BY);
      ps.setString(1, newBedType);
      if (!varianceType.equals("Incr")) {
        varianceBy = new Double(-varianceBy);
      }
      ps.setBigDecimal(2, new BigDecimal(varianceBy));
      ps.setBigDecimal(3, new BigDecimal(nearstRoundOfValue));
      ps.setString(4, baseBedForCharges);

      int ival = ps.executeUpdate();
      logger.debug(Integer.toString(ival));
      if (ival >= 0) {
        status = true;
      }
    }

    ps.close();

    return status;
  }

  /** The Constant INSERT_INTO_DYNA_CATEGORY_PLUS. */
  private static final String INSERT_INTO_DYNA_CATEGORY_PLUS =
      "INSERT INTO dyna_package_category_limits(dyna_package_id, "
      + " dyna_pkg_cat_id, bed_type, org_id, pkg_included, amount_limit, qty_limit)"
      + "(SELECT dyna_package_id,dyna_pkg_cat_id,?,org_id,"
      + " pkg_included,round(amount_limit + ?),qty_limit "
      + " FROM dyna_package_category_limits  WHERE bed_type = ? ) ";

  /** The Constant INSERT_INTO_DYNA_CATEGORY_MINUS. */
  private static final String INSERT_INTO_DYNA_CATEGORY_MINUS =
      "INSERT INTO dyna_package_category_limits(dyna_package_id,"
      + " dyna_pkg_cat_id, bed_type, org_id, pkg_included, amount_limit, qty_limit)"
      + "(SELECT dyna_package_id,dyna_pkg_cat_id,?,org_id,pkg_included,"
      + "GREATEST(round(amount_limit - ?), 0), qty_limit "
      + " FROM dyna_package_category_limits  WHERE bed_type = ? ) ";

  /** The Constant INSERT_INTO_DYNA_CATEGORY_BY. */
  private static final String INSERT_INTO_DYNA_CATEGORY_BY =
      "INSERT INTO dyna_package_category_limits(dyna_package_id,"
      + " dyna_pkg_cat_id, bed_type, org_id, pkg_included, amount_limit, qty_limit)"
      + "(SELECT dyna_package_id,dyna_pkg_cat_id,?,org_id,pkg_included,"
      + " doroundvarying(amount_limit,?,?), qty_limit "
      + " FROM dyna_package_category_limits  WHERE bed_type = ? ) ";

  /**
   * Adds the bed for dyna package category limits.
   *
   * @param con
   *          the con
   * @param newBedType
   *          the new bed type
   * @param baseBedForCharges
   *          the base bed for charges
   * @param varianceType
   *          the variance type
   * @param varianceBy
   *          the variance by
   * @param varianceValue
   *          the variance value
   * @param useValue
   *          the use value
   * @param nearstRoundOfValue
   *          the nearst round of value
   * @param userName
   *          the user name
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public boolean addBedForDynaPackageCategoryLimits(Connection con, String newBedType,
      String baseBedForCharges, String varianceType, Double varianceBy, Double varianceValue,
      boolean useValue, Double nearstRoundOfValue, String userName)
      throws SQLException, IOException {
    boolean status = false;
    PreparedStatement ps = null;

    if (useValue) {
      if (varianceType.equals("Incr")) {
        ps = con.prepareStatement(INSERT_INTO_DYNA_CATEGORY_PLUS);
        ps.setString(1, newBedType);
        ps.setDouble(2, varianceValue);
        ps.setString(3, baseBedForCharges);
        int ival = ps.executeUpdate();
        logger.debug(Integer.toString(ival));
        if (ival >= 0) {
          status = true;
        }
      } else {
        ps = con.prepareStatement(INSERT_INTO_DYNA_CATEGORY_MINUS);
        ps.setString(1, newBedType);
        ps.setDouble(2, varianceValue);
        ps.setString(3, baseBedForCharges);
        int ival = ps.executeUpdate();
        logger.debug(Integer.toString(ival));
        if (ival >= 0) {
          status = true;
        }
      }
    } else {
      ps = con.prepareStatement(INSERT_INTO_DYNA_CATEGORY_BY);
      ps.setString(1, newBedType);
      if (!varianceType.equals("Incr")) {
        varianceBy = new Double(-varianceBy);
      }
      ps.setBigDecimal(2, new BigDecimal(varianceBy));
      ps.setBigDecimal(3, new BigDecimal(nearstRoundOfValue));
      ps.setString(4, baseBedForCharges);

      int ival = ps.executeUpdate();
      logger.debug(Integer.toString(ival));
      if (ival >= 0) {
        status = true;
      }
    }

    ps.close();

    return status;
  }

  /*
   * doroundcharge sql function: doroundcharge(unitcharge numeric,varianceby
   * numeric,varianceType varchar,nearstround numeric);
   *
   */

  /** The Constant INSERT_TEST_CHARGE_PLUS. */
  private static final String INSERT_TEST_CHARGE_PLUS =
      "INSERT INTO diagnostic_charges(test_id,"
      + "org_name,charge,bed_type,priority)(SELECT test_id,org_name,round(charge+?),?, "
      + " priority FROM diagnostic_charges  where bed_type=?)";

  /** The Constant INSERT_TEST_CHARGE_MINUS. */
  private static final String INSERT_TEST_CHARGE_MINUS = "INSERT INTO diagnostic_charges(test_id,"
      + "org_name,charge,bed_type,priority)(SELECT test_id,org_name,round(charge-?),?,"
      + " priority FROM  diagnostic_charges where bed_type=?)";

  /** The Constant INSERT_TEST_CHARGE_BY. */
  private static final String INSERT_TEST_CHARGE_BY = "INSERT INTO diagnostic_charges(test_id,"
      + " org_name,charge,bed_type,priority)(SELECT test_id,org_name,"
      + " doroundvarying(charge,?,?) " + " ,?,priority FROM "
      + " diagnostic_charges where bed_type=?)";

  /**
   * Adds the bed for test.
   *
   * @param newBedType
   *          the new bed type
   * @param baseBedForCharges
   *          the base bed for charges
   * @param varianceType
   *          the variance type
   * @param varianceBy
   *          the variance by
   * @param varianceValue
   *          the variance value
   * @param useValue
   *          the use value
   * @param nearstRoundOfValue
   *          the nearst round of value
   * @param userName
   *          the user name
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public boolean addBedForTest(String newBedType, String baseBedForCharges, String varianceType,
      Double varianceBy, Double varianceValue, boolean useValue, Double nearstRoundOfValue,
      String userName) throws SQLException, IOException {
    boolean status = false;
    Connection con = DataBaseUtil.getConnection(60);
    con.setAutoCommit(false);
    PreparedStatement ps = null;

    GenericDAO.alterTrigger("DISABLE", "diagnostic_charges",
        "z_diagnostictest_charges_audit_trigger");
    if (useValue) {
      if (varianceType.equals("Incr")) {
        ps = con.prepareStatement(INSERT_TEST_CHARGE_PLUS);
        ps.setDouble(1, varianceValue);
        ps.setString(2, newBedType);
        ps.setString(3, baseBedForCharges);
        int ival = ps.executeUpdate();
        logger.debug(Integer.toString(ival));
        if (ival >= 0) {
          status = true;
        }
        logger.debug(Integer.toString(ival));
        if (ival >= 0) {
          status = true;
        }
      } else {
        ps = con.prepareStatement(INSERT_TEST_CHARGE_MINUS);
        ps.setDouble(1, varianceValue);
        ps.setString(2, newBedType);
        ps.setString(3, baseBedForCharges);

        int ival = ps.executeUpdate();
        logger.debug(Integer.toString(ival));
        if (ival >= 0) {
          status = true;
        }
        logger.debug(Integer.toString(ival));
        if (ival >= 0) {
          status = true;
        }

      }
    } else {
      ps = con.prepareStatement(INSERT_TEST_CHARGE_BY);
      if (!varianceType.equals("Incr")) {
        varianceBy = new Double(-varianceBy);
      }
      ps.setBigDecimal(1, new BigDecimal(varianceBy));
      ps.setBigDecimal(2, new BigDecimal(nearstRoundOfValue));
      ps.setString(3, newBedType);
      ps.setString(4, baseBedForCharges);

      int ival = ps.executeUpdate();
      logger.debug(Integer.toString(ival));
      if (ival >= 0) {
        status = true;
      }
    }
    if (status) {
      con.commit();
    } else {
      con.rollback();
    }

    status &= new AuditLogDao("Master", "diagnostic_charges_audit_log").logMasterChange(con,
        userName, "INSERT", "bed_type", newBedType);
    DataBaseUtil.closeConnections(con, ps);
    return status;
  }

  /** The Constant INSERT_PACKAGE_CHARGES_PLUS. */
  private static final String INSERT_PACKAGE_CHARGES_PLUS =
      "INSERT INTO package_charges(package_id,"
      + "org_id,charge,bed_type)(SELECT package_id,org_id,round(charge+?),? FROM "
      + " package_charges  where bed_type=?)";

  /** The Constant INSERT_PACKAGE_CHARGES_MINUS. */
  private static final String INSERT_PACKAGE_CHARGES_MINUS =
      "INSERT INTO package_charges(package_id,"
      + "org_id,charge,bed_type)(SELECT package_id,org_id,round(charge-?),? FROM "
      + " package_charges where bed_type=?)";

  /** The Constant INSERT_PACKAGE_CHARGES_BY. */
  private static final String INSERT_PACKAGE_CHARGES_BY = "INSERT INTO package_charges(package_id,"
      + "org_id,charge,bed_type)(SELECT package_id,org_id,doroundvarying(charge,?,?),? FROM "
      + " package_charges where bed_type=?)";

  /**
   * Adds the bed for packages.
   *
   * @param con
   *          the con
   * @param newBedType
   *          the new bed type
   * @param baseBedForCharges
   *          the base bed for charges
   * @param varianceType
   *          the variance type
   * @param varianceBy
   *          the variance by
   * @param varianceValue
   *          the variance value
   * @param useValue
   *          the use value
   * @param nearstRoundOfValue
   *          the nearst round of value
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean addBedForPackages(Connection con, String newBedType, String baseBedForCharges,
      String varianceType, Double varianceBy, Double varianceValue, boolean useValue,
      Double nearstRoundOfValue) throws SQLException {
    boolean status = false;
    PreparedStatement ps = null;
    if (useValue) {
      if (varianceType.equals("Incr")) {
        ps = con.prepareStatement(INSERT_PACKAGE_CHARGES_PLUS);
        ps.setDouble(1, varianceValue);
        ps.setString(2, newBedType);
        ps.setString(3, baseBedForCharges);
        int ival = ps.executeUpdate();
        logger.debug(Integer.toString(ival));
        if (ival >= 0) {
          status = true;
        }
      } else {
        ps = con.prepareStatement(INSERT_PACKAGE_CHARGES_MINUS);
        ps.setDouble(1, varianceValue);
        ps.setString(2, newBedType);
        ps.setString(3, baseBedForCharges);

        int ival = ps.executeUpdate();
        logger.debug(Integer.toString(ival));
        if (ival >= 0) {
          status = true;
        }
        logger.debug(Integer.toString(ival));
        if (ival >= 0) {
          status = true;
        }

      }
    } else {
      ps = con.prepareStatement(INSERT_PACKAGE_CHARGES_BY);
      if (!varianceType.equals("Incr")) {
        varianceBy = new Double(-varianceBy);
      }
      ps.setBigDecimal(1, new BigDecimal(varianceBy));
      ps.setBigDecimal(2, new BigDecimal(nearstRoundOfValue));
      ps.setString(3, newBedType);
      ps.setString(4, baseBedForCharges);

      int ival = ps.executeUpdate();
      logger.debug(Integer.toString(ival));
      if (ival >= 0) {
        status = true;
      }
    }
    ps.close();
    return status;
  }

  // This is for dietary

  /** The Constant INSERT_MEAL_CHARGE_PLUS. */
  private static final String INSERT_MEAL_CHARGE_PLUS = "INSERT INTO diet_charges(diet_id,"
      + "org_id,charge,bed_type)(SELECT diet_id,org_id,round(charge+?),? FROM "
      + " diet_charges  where bed_type=?)";

  /** The Constant INSERT_MEAL_CHARGE_MINUS. */
  private static final String INSERT_MEAL_CHARGE_MINUS = "INSERT INTO diet_charges(diet_id,"
      + "org_id,charge,bed_type)(SELECT diet_id,org_id,round(charge-?),? FROM "
      + " diet_charges where bed_type=?)";

  /** The Constant INSERT_MEAL_CHARGE_BY. */
  private static final String INSERT_MEAL_CHARGE_BY = "INSERT INTO diet_charges(diet_id,"
      + "org_id,charge,bed_type)(SELECT diet_id,org_id,doroundvarying(charge,?,?),? FROM "
      + " diet_charges where bed_type=?)";

  /**
   * Adds the bed for dietry.
   *
   * @param con
   *          the con
   * @param newBedType
   *          the new bed type
   * @param baseBedForCharges
   *          the base bed for charges
   * @param varianceType
   *          the variance type
   * @param varianceBy
   *          the variance by
   * @param varianceValue
   *          the variance value
   * @param useValue
   *          the use value
   * @param nearstRoundOfValue
   *          the nearst round of value
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean addBedForDietry(Connection con, String newBedType, String baseBedForCharges,
      String varianceType, Double varianceBy, Double varianceValue, boolean useValue,
      Double nearstRoundOfValue) throws SQLException {
    boolean status = false;
    PreparedStatement ps = null;
    if (useValue) {
      if (varianceType.equals("Incr")) {
        ps = con.prepareStatement(INSERT_MEAL_CHARGE_PLUS);
        ps.setDouble(1, varianceValue);
        ps.setString(2, newBedType);
        ps.setString(3, baseBedForCharges);
        int ival = ps.executeUpdate();
        logger.debug(Integer.toString(ival));
        if (ival >= 0) {
          status = true;
        }
        logger.debug(Integer.toString(ival));
        if (ival >= 0) {
          status = true;
        }
      } else {
        ps = con.prepareStatement(INSERT_MEAL_CHARGE_MINUS);
        ps.setDouble(1, varianceValue);
        ps.setString(2, newBedType);
        ps.setString(3, baseBedForCharges);

        int ival = ps.executeUpdate();
        logger.debug(Integer.toString(ival));
        if (ival >= 0) {
          status = true;
        }
        logger.debug(Integer.toString(ival));
        if (ival >= 0) {
          status = true;
        }

      }
    } else {
      ps = con.prepareStatement(INSERT_MEAL_CHARGE_BY);
      if (!varianceType.equals("Incr")) {
        varianceBy = new Double(-varianceBy);
      }
      ps.setBigDecimal(1, new BigDecimal(varianceBy));
      ps.setBigDecimal(2, new BigDecimal(nearstRoundOfValue));
      ps.setString(3, newBedType);
      ps.setString(4, baseBedForCharges);

      int ival = ps.executeUpdate();
      logger.debug(Integer.toString(ival));
      if (ival >= 0) {
        status = true;
      }
    }
    ps.close();
    return status;
  }

  /** The Constant GET_DISTINCT_ICUBEDS. */
  private static final String GET_DISTINCT_ICUBEDS =
      "SELECT distinct intensive_bed_type FROM icu_bed_charges";

  /** The Constant SELECT_CHARGES. */
  private static final String SELECT_CHARGES = "SELECT bed_charge,nursing_charge,initial_payment,"
      + " duty_charge,maintainance_charge,hourly_charge,daycare_slab_1_charge,"
      + " daycare_slab_2_charge, daycare_slab_3_charge FROM  icu_bed_charges "
      + " WHERE bed_type=? AND organization=? AND intensive_bed_type=?";

  /** The Constant SELECT_ORG_LIST. */
  private static final String SELECT_ORG_LIST = "SELECT distinct organization"
      + " FROM icu_bed_charges WHERE "
      + "intensive_bed_type =?";

  /** The Constant INSERT_INTO_ICUS. */
  private static final String INSERT_INTO_ICUS = "INSERT INTO icu_bed_charges(bed_type,"
      + " bed_charge,nursing_charge,initial_payment,"
      + " duty_charge,maintainance_charge,organization,charge_type,bed_status,luxary_tax,"
      + " intensive_bed_type,hourly_charge,daycare_slab_1_charge, "
      + " daycare_slab_2_charge, daycare_slab_3_charge) "
      + "(SELECT ?,round(?),round(?),round(?),"
      + " round(?),round(?),organization,charge_type,bed_status,luxary_tax,"
      + " intensive_bed_type,round(?),round(?),round(?),round(?) "
      + " FROM icu_bed_charges WHERE bed_type = ? AND organization =? AND intensive_bed_type =? ) ";

  /**
   * Adds the normal bed for icu charges.
   *
   * @param con
   *          the con
   * @param newBedType
   *          the new bed type
   * @param baseBedForCharges
   *          the base bed for charges
   * @param varianceType
   *          the variance type
   * @param varianceBy
   *          the variance by
   * @param varianceValue
   *          the variance value
   * @param useValue
   *          the use value
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean addNormalBedForIcuCharges(Connection con, String newBedType,
      String baseBedForCharges, String varianceType, Double varianceBy, Double varianceValue,
      boolean useValue) throws SQLException {

    PreparedStatement ps = con.prepareStatement(GET_DISTINCT_ICUBEDS);
    PreparedStatement ps1 = con.prepareStatement(INSERT_INTO_ICUS);
    PreparedStatement ps2 = con.prepareStatement(SELECT_CHARGES);
    ArrayList<String> icubedlist = DataBaseUtil.queryToOnlyArrayList(ps);
    Iterator<String> it = icubedlist.iterator();
    ps.close();

    ps = con.prepareStatement(SELECT_ORG_LIST);

    while (it.hasNext()) {
      String icubed = it.next();
      ps.setString(1, icubed);
      ArrayList<String> orgList = DataBaseUtil.queryToOnlyArrayList(ps);
      Iterator<String> orgIt = orgList.iterator();
      while (orgIt.hasNext()) {
        String orgId = orgIt.next();
        ps2.setString(1, baseBedForCharges);
        ps2.setString(2, orgId);
        ps2.setString(3, icubed);

        ArrayList<Hashtable<String, String>> chargeList = DataBaseUtil.queryToArrayList(ps2);
        Iterator<Hashtable<String, String>> chargeIt = chargeList.iterator();
        while (chargeIt.hasNext()) {
          Hashtable<String, String> ht = chargeIt.next();
          Double bedCharge = new Double(ht.get("BED_CHARGE"));
          Double nursingCharge = new Double(ht.get("NURSING_CHARGE"));
          Double intialPayment = new Double(ht.get("INITIAL_PAYMENT"));
          Double dutyCharge = new Double(ht.get("DUTY_CHARGE"));
          Double maintainceCharge = new Double(ht.get("MAINTAINANCE_CHARGE"));
          Double hourlyCharge = new Double(ht.get("HOURLY_CHARGE"));
          Double daycareSlab1Charge = new Double(ht.get("DAYCARE_SLAB_1_CHARGE"));
          Double daycareSlab2Charge = new Double(ht.get("DAYCARE_SLAB_2_CHARGE"));
          Double daycareSlab3Charge = new Double(ht.get("DAYCARE_SLAB_3_CHARGE"));

          if (varianceType.equals("Incr")) {
            if (useValue) {
              bedCharge += varianceValue;
              nursingCharge += varianceValue;
              intialPayment += varianceValue;
              dutyCharge += varianceValue;
              maintainceCharge += varianceValue;
              hourlyCharge += varianceValue;
            } else {
              bedCharge += bedCharge * (varianceBy.doubleValue() / 100);
              nursingCharge += nursingCharge * (varianceBy.doubleValue() / 100);
              intialPayment += intialPayment * (varianceBy.doubleValue() / 100);
              dutyCharge += dutyCharge * (varianceBy.doubleValue() / 100);
              maintainceCharge += maintainceCharge * (varianceBy.doubleValue() / 100);
              hourlyCharge += hourlyCharge * (varianceBy.doubleValue() / 100);
              daycareSlab1Charge += daycareSlab1Charge * (varianceBy.doubleValue() / 100);
              daycareSlab2Charge += daycareSlab2Charge * (varianceBy.doubleValue() / 100);
              daycareSlab3Charge += daycareSlab3Charge * (varianceBy.doubleValue() / 100);
            }
          } else {
            if (useValue) {
              bedCharge -= varianceValue;
              nursingCharge -= varianceValue;
              intialPayment -= varianceValue;
              dutyCharge -= varianceValue;
              maintainceCharge -= varianceValue;
              hourlyCharge -= varianceValue;
              daycareSlab1Charge -= varianceValue;
              daycareSlab2Charge -= varianceValue;
              daycareSlab3Charge -= varianceValue;

            } else {
              bedCharge -= bedCharge * (varianceBy.doubleValue() / 100);
              nursingCharge -= nursingCharge * (varianceBy.doubleValue() / 100);
              intialPayment -= intialPayment * (varianceBy.doubleValue() / 100);
              dutyCharge -= dutyCharge * (varianceBy.doubleValue() / 100);
              maintainceCharge -= maintainceCharge * (varianceBy.doubleValue() / 100);
              hourlyCharge -= hourlyCharge * (varianceBy.doubleValue() / 100);
              daycareSlab1Charge -= daycareSlab1Charge * (varianceBy.doubleValue() / 100);
              daycareSlab2Charge -= daycareSlab2Charge * (varianceBy.doubleValue() / 100);
              daycareSlab3Charge -= daycareSlab3Charge * (varianceBy.doubleValue() / 100);

            }

          }

          ps1.setString(1, newBedType);
          ps1.setDouble(2, bedCharge);
          ps1.setDouble(3, nursingCharge);
          ps1.setDouble(4, intialPayment);
          ps1.setDouble(5, dutyCharge);
          ps1.setDouble(6, maintainceCharge);
          ps1.setDouble(7, hourlyCharge);
          ps1.setDouble(8, daycareSlab1Charge);
          ps1.setDouble(9, daycareSlab2Charge);
          ps1.setDouble(10, daycareSlab3Charge);
          ps1.setString(11, baseBedForCharges);
          ps1.setString(12, orgId);
          ps1.setString(13, icubed);

          ps1.addBatch();

        }
      }
    }
    boolean status = false;
    int[] aval = ps1.executeBatch();
    status = DataBaseUtil.checkBatchUpdates(aval);

    if (ps != null) {
      ps.close();
    }
    if (ps2 != null) {
      ps2.close();
    }
    if (ps1 != null) {
      ps.close();
    }

    return status;
  }

  /** The Constant isIcuBedType. */
  private static final String isIcuBedType =
      "SELECT is_icu FROM  bed_types WHERE  bed_type_name = ?";

  /**
   * Checks if is icu bed type.
   *
   * @param bedType
   *          the bed type
   * @return true, if is icu bed type
   * @throws SQLException
   *           the SQL exception
   */
  public static boolean isIcuBedType(String bedType) throws SQLException {
    boolean icubedType = false;
    Connection con = DataBaseUtil.getReadOnlyConnection();
    PreparedStatement ps = con.prepareStatement(isIcuBedType);
    ps.setString(1, bedType);
    String icuStatus = DataBaseUtil.getStringValueFromDb(ps);
    if (icuStatus.equals("Y")) {
      icubedType = true;
    }
    ps.close();
    con.close();
    return icubedType;
  }

  /**
   * Checks if is icu.
   *
   * @param bedId
   *          the bed id
   * @return true, if is icu
   * @throws SQLException
   *           the SQL exception
   */
  public boolean isIcu(int bedId) throws SQLException {
    return isIcuBedType(getBedDeails(bedId).getBedtype());
  }

  /** The Constant GET_ALL_ACTIVE_BEDTYPES. */
  private static final String GET_ALL_ACTIVE_BEDTYPES =
      " SELECT bed_type_name as bed_type from bed_types WHERE billing_bed_type='Y'"
      + " AND status = 'A' ORDER BY display_order ";

  /**
   * Gets the union of all bed types.
   *
   * @return the union of all bed types
   * @throws SQLException
   *           the SQL exception
   */
  public ArrayList<Hashtable<String, String>> getUnionOfAllBedTypes() throws SQLException {
    ArrayList<Hashtable<String, String>> al = null;
    Connection con = null;
    PreparedStatement ps = null;
    try {

      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_ALL_ACTIVE_BEDTYPES);

      al = DataBaseUtil.queryToArrayList(ps);
      ArrayList<Hashtable<String, String>> duplicate = new ArrayList<Hashtable<String, String>>();

      Hashtable<String, String> ght = new Hashtable<String, String>();
      ght.put("BED_TYPE", "GENERAL");
      duplicate.add(ght);

      Iterator<Hashtable<String, String>> it = al.iterator();
      while (it.hasNext()) {
        Hashtable<String, String> ht = it.next();
        if (!ht.get("BED_TYPE").equals("GENERAL")) {
          duplicate.add(ht);
        }
      }
      logger.debug("{}", al);
      logger.debug("{}", duplicate);
      return duplicate;
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the union of bed types.
   *
   * @return the union of bed types
   * @throws SQLException
   *           the SQL exception
   */
  public static ArrayList<String> getUnionOfBedTypes() throws SQLException {
    ArrayList<String> al = null;
    Connection con = null;
    ArrayList<String> duplicate = new ArrayList<String>();
    PreparedStatement ps = null;

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_ALL_ACTIVE_BEDTYPES);
      al = DataBaseUtil.queryToOnlyArrayList(ps);

      duplicate.add("GENERAL");

      Iterator<String> it = al.iterator();
      while (it.hasNext()) {
        String bed = it.next();
        if (!bed.equals("GENERAL")) {
          duplicate.add(bed);
        }
      }
      logger.debug("{}", duplicate);
    } finally {
      if (null != ps) {
        ps.close();
      }
      if (null != con) {
        con.close();
      }
    }
    return duplicate;
  }

  /** The Constant GET_ALL_BEDTYPES. */
  private static final String GET_ALL_BEDTYPES =
      "SELECT distinct bed_type FROM bed_details   UNION "
      + " SELECT distinct  intensive_bed_type as bed_type FROM icu_bed_charges  ";

  /**
   * Chek duplicate bed type.
   *
   * @param newBedType
   *          the new bed type
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public static boolean chekDuplicateBedType(String newBedType) throws SQLException {
    boolean status = false;
    ArrayList<Hashtable<String, String>> al = null;
    Connection con = DataBaseUtil.getReadOnlyConnection();
    PreparedStatement ps = con.prepareStatement(GET_ALL_BEDTYPES);
    al = DataBaseUtil.queryToArrayList(ps);
    Iterator<Hashtable<String, String>> it = al.iterator();
    while (it.hasNext()) {
      Hashtable<String, String> ht = it.next();
      if (newBedType.equals(ht.get("BED_TYPE"))) {
        // duplicate bedTyoe
        status = true;
      }
    }

    ps.close();
    con.close();

    return status;
  }

  /** The Constant UPDATE_BEDTYPE. */
  private static final String UPDATE_BEDTYPE =
      "UPDATE bed_details SET bed_status = ?  WHERE bed_type = ? ";

  /** The Constant UPDATE_BEDNAMES. */
  private static final String UPDATE_BEDNAMES =
      " UPDATE bed_names SET status = ? WHERE bed_type = ? ";

  /** The Constant UPDATE_ICUBED. */
  private static final String UPDATE_ICUBED =
      "UPDATE icu_bed_charges  SET bed_status = ? WHERE intensive_bed_type  = ?  ";

  /**
   * Update bed status.
   *
   * @param con
   *          the con
   * @param bedType
   *          the bed type
   * @param bedStatus
   *          the bed status
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean updateBedStatus(Connection con, String bedType, String bedStatus)
      throws SQLException {
    boolean status = false;
    boolean isicu = false;
    int ival = 0;
    int jval = 0;

    isicu = isIcuBedType(bedType);
    PreparedStatement ps = null;
    PreparedStatement ps1 = null;
    if (!isicu) {
      ps = con.prepareStatement(UPDATE_BEDTYPE);
      ps.setString(1, bedStatus);
      ps.setString(2, bedType);

      ival = ps.executeUpdate();

      if (ival > 0) {
        status = true;
      }
      ps.close();

    } else {

      ps1 = con.prepareStatement(UPDATE_ICUBED);
      ps1.setString(1, bedStatus);
      ps1.setString(2, bedType);

      jval = ps1.executeUpdate();
      if (jval > 0) {
        status = true;
      }
      ps1.close();
    }
    if (ival > 0 || jval > 0) {
      ps = con.prepareStatement(UPDATE_BEDNAMES);
      ps.setString(1, bedStatus);
      ps.setString(2, bedType);

      int kval = ps.executeUpdate();

      if (kval > 0) {
        status = true;
      }
      ps.close();
    }
    return status;
  }

  /**
   * Gets the bed deails.
   *
   * @param bedid
   *          the bedid
   * @return the bed deails
   * @throws SQLException
   *           the SQL exception
   */
  public static BedDTO getBedDeails(int bedid) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    BedDTO bed = new BedDTO();
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement("SELECT B.BED_NAME,B.BED_ID,W.WARD_NAME,B.BED_TYPE,"
          + " B.OCCUPANCY,B.WARD_NO,B.BED_REF_ID,B.avilable_date,B.REMARKS,B.BED_STATUS "
          + " FROM BED_NAMES B JOIN WARD_NAMES W ON B.WARD_NO=W.WARD_NO  " + "WHERE B.BED_ID=?");
      ps.setInt(1, bedid);
      rs = ps.executeQuery();
      while (rs.next()) {
        bed.setBedname(rs.getString(1));
        bed.setBed_id(rs.getInt(2));
        bed.setWardname(rs.getString(3));
        bed.setBedtype(rs.getString(4));
        bed.setOccupancy(rs.getString("OCCUPANCY"));
        bed.setWardNo(rs.getString("WARD_NO"));
        bed.setBed_ref_id(rs.getInt("bed_ref_id"));
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return bed;
  }

  /** The Constant bed_names_query. */
  private static final String bed_names_query =
      "SELECT b.ward_no, b.bed_type, b.bed_name, w.ward_name,"
      + " b.bed_id, b.bed_ref_id, b.avilable_date, b.remarks, b.occupancy, b.status, b.bed_status, "
      + " bt.is_icu, bt.is_child_bed, bt.billing_bed_type, "
      + " bt.insurance_category_id,bt.billing_group_id "
      + " FROM bed_names b " + "  JOIN ward_names w USING(ward_no) "
      + "  JOIN bed_types bt ON (bt.bed_type_name = b.bed_type) ";

  /**
   * Gets the bed details bean.
   *
   * @param con
   *          the con
   * @param bedid
   *          the bedid
   * @return the bed details bean
   * @throws SQLException
   *           the SQL exception
   */
  public static BasicDynaBean getBedDetailsBean(Connection con, int bedid) throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(bed_names_query + " WHERE bed_id = ? ");
      ps.setInt(1, bedid);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  /**
   * Gets the bed details bean.
   *
   * @param bedid
   *          the bedid
   * @return the bed details bean
   * @throws SQLException
   *           the SQL exception
   */
  public static BasicDynaBean getBedDetailsBean(int bedid) throws SQLException {
    PreparedStatement ps = null;
    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(bed_names_query + " WHERE bed_id = ? ");
      ps.setInt(1, bedid);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the bed details bean.
   *
   * @param actDescriptionId
   *          the act description id
   * @return the bed details bean
   * @throws SQLException
   *           the SQL exception
   */
  public static BasicDynaBean getBedDetailsBean(String actDescriptionId) throws SQLException {
    PreparedStatement ps = null;
    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con
          .prepareStatement(bed_names_query + " WHERE b.bed_id::varchar =? OR bt.bed_type_name=? ");
      ;
      ps.setString(1, actDescriptionId);
      ps.setString(2, actDescriptionId);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_ALL_BED_DETAILS. */
  private static final String GET_ALL_BED_DETAILS =
      " SELECT * from bedcharges_view WHERE bed_status='A' ";

  /**
   * Gets the all bed details.
   *
   * @return the all bed details
   * @throws SQLException
   *           the SQL exception
   */
  public static List getAllBedDetails() throws SQLException {
    return DataBaseUtil.queryToDynaList(GET_ALL_BED_DETAILS + " ORDER BY UPPER(bedtype) ");
  }
  
  /**
   * Gets the all bed details.
   *
   * @param organization
   *          the organization
   * @param bedtype
   *          the bedtype
   * @return the all bed details
   * @throws SQLException
   *           the SQL exception
   */
  public List<BasicDynaBean> getAllBedDetails(String organization, String bedtype)
      throws SQLException {
    return DataBaseUtil.queryToDynaList(GET_ALL_BED_DETAILS_BY_ORG_AND_BEDTYPE, organization,
        bedtype);
  }
  
  /**
   * Gets the billing bed details.
   *
   * @return the billing bed details
   * @throws SQLException
   *           the SQL exception
   */
  public static List getBillingBedDetails() throws SQLException {
    return DataBaseUtil.queryToDynaList(
        GET_ALL_BED_DETAILS + " AND billing_bed_type='Y' ORDER BY UPPER(bedtype) ");
  }
  
  /**
   * Gets the billing bed details.
   *
   * @param organization
   *          the organization
   * @param bedtype
   *          the bedtype
   * @return the billing bed details
   * @throws SQLException
   *           the SQL exception
   */
  public List<BasicDynaBean> getBillingBedDetails(String organization, String bedtype)
      throws SQLException {
    return DataBaseUtil.queryToDynaList(
        GET_ALL_BED_DETAILS_BY_ORG_AND_BEDTYPE + " AND billing_bed_type='Y' ", organization,
        bedtype);
  }

  /** The Constant GET_ALL_ACTIVE_BED_TYPES. */
  private static final String GET_ALL_ACTIVE_BED_TYPES =
      " SELECT bed_type_name as bedtype, billing_bed_type, status as bed_status "
      + " from bed_types WHERE status='A' ";

  /**
   * Gets the all active bed types.
   *
   * @return the all active bed types
   * @throws SQLException
   *           the SQL exception
   */
  public static List getAllActiveBedTypes() throws SQLException {
    return DataBaseUtil
        .queryToDynaList(GET_ALL_ACTIVE_BED_TYPES + " ORDER BY UPPER(bed_type_name) ");
  }

  /**
   * Gets the all active billable bed types.
   *
   * @return the all active billable bed types
   * @throws SQLException
   *           the SQL exception
   */
  public static List getAllActiveBillableBedTypes() throws SQLException {
    return DataBaseUtil.queryToDynaList(
        GET_ALL_ACTIVE_BED_TYPES + " AND billing_bed_type='Y' ORDER BY UPPER(bed_type_name) ");
  }

  /** The Constant GET_ALL_BED_DETAILS_BY_ORG_AND_BEDTYPE. */
  private static final String GET_ALL_BED_DETAILS_BY_ORG_AND_BEDTYPE =
      " SELECT * FROM bedcharges_view WHERE bed_status='A' AND organization=? AND bedtype=? ";

  /**
   * Group update for charges.
   *
   * @param con
   *          the con
   * @return the list
   * @throws Exception
   *           the exception
   */
  public List groupUpdateForCharges(Connection con) throws Exception {
    con = DataBaseUtil.getConnection();
    List list = null;
    PreparedStatement ps = null;
    try {
      ps = con
          .prepareStatement(" SELECT column_name FROM information_schema.columns "
              + " WHERE table_schema = (SELECT current_schema()) AND table_name = 'bed_details' ");
      list = DataBaseUtil.queryToArrayList(ps);
    } finally {
      if (ps != null) {
        ps.close();
      }
    }

    return list;
  }

  /** The Constant GROUP_INCR_BED_CHARGES. */
  private static final String GROUP_INCR_BED_CHARGES =
      " UPDATE bed_details SET # = GREATEST( round((# + ?)/?,0)*?, 0) "
      + " WHERE organization=? ";

  /** The Constant GROUP_INCR_BED_CHARGES_PERCENTAGE. */
  private static final String GROUP_INCR_BED_CHARGES_PERCENTAGE =
      " UPDATE bed_details SET # = GREATEST( round(#*(100+?)/100/?,0)*?, 0) "
      + " WHERE organization=? ";

  /** The Constant GROUP_INCR_BED_DISCOUNTS. */
  private static final String GROUP_INCR_BED_DISCOUNTS =
      " UPDATE bed_details SET # = LEAST(GREATEST( round((# + ?)/?,0)*?, 0), @) "
      + " WHERE organization=? ";

  /** The Constant GROUP_INCR_BED_DISCOUNTS_PERCENTAGE. */
  private static final String GROUP_INCR_BED_DISCOUNTS_PERCENTAGE =
      " UPDATE bed_details SET # = LEAST(GREATEST( round(#*(100+?)/100/?,0)*?, 0), @) "
      + " WHERE organization=? ";

  /** The Constant GROUP_APPLY_DISCOUNTS_BED. */
  private static final String GROUP_APPLY_DISCOUNTS_BED =
      " UPDATE bed_details SET @ = LEAST(GREATEST( round((# + ?)/?,0)*?, 0), #) "
      + " WHERE organization=? ";

  /** The Constant GROUP_APPLY_DISCOUNT_PERCENTAGE_BED. */
  private static final String GROUP_APPLY_DISCOUNT_PERCENTAGE_BED =
      " UPDATE bed_details SET @ = GREATEST( round(# * ?/100/?,0)*?, 0) "
      + " WHERE organization=? ";

  /**
   * Group update charges normal beds.
   *
   * @param con
   *          the con
   * @param orgId
   *          the org id
   * @param bedTypes
   *          the bed types
   * @param groupUpdate
   *          the group update
   * @param amount
   *          the amount
   * @param isPercentage
   *          the is percentage
   * @param roundOff
   *          the round off
   * @param updateTable
   *          the update table
   * @throws SQLException
   *           the SQL exception
   */
  public void groupUpdateChargesNormalBeds(Connection con, String orgId, List<String> bedTypes,
      String groupUpdate, BigDecimal amount, boolean isPercentage, BigDecimal roundOff,
      String updateTable) throws SQLException {

    StringBuilder query = null;
    String chargeType = null;

    if (groupUpdate.equals("BEDCHARGE")) {
      chargeType = "bed_charge";
    } else if (groupUpdate.equals("NURSING")) {
      chargeType = "nursing_charge";
    } else if (groupUpdate.equals("INITIAL")) {
      chargeType = "initial_payment";
    } else if (groupUpdate.equals("DUTY")) {
      chargeType = "duty_charge";
    } else if (groupUpdate.equals("MAINTAINANCE")) {
      chargeType = "maintainance_charge";
    } else if (groupUpdate.equals("HOURLY")) {
      chargeType = "hourly_charge";
    } else if (groupUpdate.equals("LUXARY")) {
      chargeType = "luxary_tax";
    }

    if (updateTable != null && updateTable.equals("UPDATECHARGE")) {
      query = new StringBuilder(isPercentage
          ? GROUP_INCR_BED_CHARGES_PERCENTAGE.replaceAll("#", DataBaseUtil.quoteIdent(chargeType))
          : GROUP_INCR_BED_CHARGES.replaceAll("#", DataBaseUtil.quoteIdent(chargeType)));

    } else if (updateTable.equals("UPDATEDISCOUNT")) {

      String updateDisAmount = GROUP_INCR_BED_DISCOUNTS.replaceAll("@", chargeType);
      String updateDisPercent = GROUP_INCR_BED_DISCOUNTS_PERCENTAGE.replaceAll("@", chargeType);

      chargeType = chargeType + "_discount";

      query = new StringBuilder(
          isPercentage ? updateDisPercent.replaceAll("#", DataBaseUtil.quoteIdent(chargeType))
              : updateDisAmount.replaceAll("#", DataBaseUtil.quoteIdent(chargeType)));
    } else {

      String chargeTypeDiscount = chargeType + "_discount";
      String discountOnCharge = null;

      if (isPercentage) {
        discountOnCharge = GROUP_APPLY_DISCOUNT_PERCENTAGE_BED.replaceAll("@", chargeTypeDiscount);
        discountOnCharge = discountOnCharge.replaceAll("#", chargeType);
      } else {
        discountOnCharge = GROUP_APPLY_DISCOUNTS_BED.replaceAll("@", chargeTypeDiscount);
        discountOnCharge = discountOnCharge.replaceAll("#", chargeType);
      }
      query = new StringBuilder(discountOnCharge);
    }

    SearchQueryBuilder.addWhereFieldOpValue(true, query, "bed_type", "IN", bedTypes);

    PreparedStatement ps = con.prepareStatement(query.toString());

    int ival = 1;
    ps.setBigDecimal(ival++, amount);
    ps.setBigDecimal(ival++, roundOff);
    ps.setBigDecimal(ival++, roundOff);
    ps.setString(ival++, orgId);

    if (bedTypes != null) {
      for (String bedType : bedTypes) {
        ps.setString(ival++, bedType);
      }
    }

    ps.executeUpdate();
    if (ps != null) {
      ps.close();
    }
  }

  /** The Constant GROUP_INCR_ICUBED_CHARGES. */
  private static final String GROUP_INCR_ICUBED_CHARGES =
      " UPDATE icu_bed_charges SET # = GREATEST( round((#+?)/?,0)*?, 0) "
      + " WHERE organization=? ";

  /** The Constant GROUP_INCR_ICUBED_CHARGES_PERCENTAGE. */
  private static final String GROUP_INCR_ICUBED_CHARGES_PERCENTAGE =
      " UPDATE icu_bed_charges SET # = GREATEST( round(#*(100+?)/100/?,0)*?, 0) "
      + " WHERE organization=? ";

  /** The Constant GROUP_INCR_ICUBED_DISCOUNTS. */
  private static final String GROUP_INCR_ICUBED_DISCOUNTS =
      " UPDATE icu_bed_charges SET # = LEAST(GREATEST( round((# + ?)/?,0)*?, 0), @) "
      + " WHERE organization=? ";

  /** The Constant GROUP_INCR_ICUBED_DISCOUNTS_PERCENTAGE. */
  private static final String GROUP_INCR_ICUBED_DISCOUNTS_PERCENTAGE =
      " UPDATE icu_bed_charges SET # = LEAST(GREATEST( round(#*(100+?)/100/?,0)*?, 0), @) "
      + " WHERE organization=? ";

  /** The Constant GROUP_APPLY_DISCOUNTS_ICUBED. */
  private static final String GROUP_APPLY_DISCOUNTS_ICUBED =
      " UPDATE icu_bed_charges SET @ = LEAST(GREATEST( round((#+?)/?,0)*?, 0), #) "
      + " WHERE organization=? ";

  /** The Constant GROUP_APPLY_DISCOUNT_PERCENTAGE_ICUBED. */
  private static final String GROUP_APPLY_DISCOUNT_PERCENTAGE_ICUBED =
      " UPDATE icu_bed_charges SET @ = GREATEST( round(# * ?/100/?,0)*?, 0) "
      + " WHERE organization=? ";

  /**
   * Group update charges ICU beds.
   *
   * @param con
   *          the con
   * @param orgId
   *          the org id
   * @param icubedTypes
   *          the icubed types
   * @param groupUpdate
   *          the group update
   * @param amount
   *          the amount
   * @param isPercentage
   *          the is percentage
   * @param roundOff
   *          the round off
   * @param updateTable
   *          the update table
   * @throws SQLException
   *           the SQL exception
   */
  public void groupUpdateChargesIcuBeds(Connection con, String orgId, List<String> icubedTypes,
      String groupUpdate, BigDecimal amount, boolean isPercentage, BigDecimal roundOff,
      String updateTable) throws SQLException {

    StringBuilder query = null;
    String chargeType = null;

    if (groupUpdate.equals("BEDCHARGE")) {
      chargeType = "bed_charge";
    } else if (groupUpdate.equals("NURSING")) {
      chargeType = "nursing_charge";
    } else if (groupUpdate.equals("INITIAL")) {
      chargeType = "initial_payment";
    } else if (groupUpdate.equals("DUTY")) {
      chargeType = "duty_charge";
    } else if (groupUpdate.equals("MAINTAINANCE")) {
      chargeType = "maintainance_charge";
    } else if (groupUpdate.equals("HOURLY")) {
      chargeType = "hourly_charge";
    } else if (groupUpdate.equals("LUXARY")) {
      chargeType = "luxary_tax";
    }

    if (updateTable != null && updateTable.equals("UPDATECHARGE")) {
      query = new StringBuilder(isPercentage
          ? GROUP_INCR_ICUBED_CHARGES_PERCENTAGE.replaceAll("#",
              DataBaseUtil.quoteIdent(chargeType))
          : GROUP_INCR_ICUBED_CHARGES.replaceAll("#", DataBaseUtil.quoteIdent(chargeType)));

    } else if (updateTable.equals("UPDATEDISCOUNT")) {

      String updateDisAmount = GROUP_INCR_ICUBED_DISCOUNTS.replaceAll("@", chargeType);
      String updateDisPercent = GROUP_INCR_ICUBED_DISCOUNTS_PERCENTAGE.replaceAll("@", chargeType);

      chargeType = chargeType + "_discount";

      query = new StringBuilder(
          isPercentage ? updateDisPercent.replaceAll("#", DataBaseUtil.quoteIdent(chargeType))
              : updateDisAmount.replaceAll("#", DataBaseUtil.quoteIdent(chargeType)));
    } else {

      String chargeTypeDiscount = chargeType + "_discount";
      String discountOnCharge = null;
      if (isPercentage) {
        discountOnCharge = GROUP_APPLY_DISCOUNT_PERCENTAGE_ICUBED.replaceAll("@",
            chargeTypeDiscount);
        discountOnCharge = discountOnCharge.replaceAll("#", chargeType);
      } else {
        discountOnCharge = GROUP_APPLY_DISCOUNTS_ICUBED.replaceAll("@", chargeTypeDiscount);
        discountOnCharge = discountOnCharge.replaceAll("#", chargeType);
      }

      query = new StringBuilder(discountOnCharge);
    }

    List<String> bedTypes = getBedTypes(con, icubedTypes, orgId);

    SearchQueryBuilder.addWhereFieldOpValue(true, query, "bed_type", "IN", bedTypes);
    SearchQueryBuilder.addWhereFieldOpValue(true, query, "intensive_bed_type", "IN", icubedTypes);

    PreparedStatement ps = con.prepareStatement(query.toString());

    int ival = 1;
    ps.setBigDecimal(ival++, amount);
    ps.setBigDecimal(ival++, roundOff);
    ps.setBigDecimal(ival++, roundOff);
    ps.setString(ival++, orgId);

    if (bedTypes != null) {
      for (String bedType : bedTypes) {
        ps.setString(ival++, bedType);
      }
    }
    if (icubedTypes != null) {
      for (String icuBedType : icubedTypes) {
        ps.setString(ival++, icuBedType);
      }
    }
    ps.executeUpdate();
    if (ps != null) {
      ps.close();
    }
  }

  /**
   * Gets the bed types.
   *
   * @param con
   *          the con
   * @param icubedTypes
   *          the icubed types
   * @param orgId
   *          the org id
   * @return the bed types
   * @throws SQLException
   *           the SQL exception
   */
  private List<String> getBedTypes(Connection con, List<String> icubedTypes, String orgId)
      throws SQLException {
    List<String> bedTypes = new ArrayList<String>();
    StringBuilder query = new StringBuilder(
        "SELECT distinct(bed_type) FROM icu_bed_charges WHERE organization = ? ");
    SearchQueryBuilder.addWhereFieldOpValue(true, query, "intensive_bed_type", "IN", icubedTypes);
    PreparedStatement ps = con.prepareStatement(query.toString());
    int ival = 1;
    ps.setString(ival++, orgId);
    if (icubedTypes != null) {
      for (String bedType : icubedTypes) {
        ps.setString(ival++, bedType);
      }
    }
    ResultSet rs = ps.executeQuery();
    while (rs.next()) {
      bedTypes.add(rs.getString(1));
    }
    ps.close();
    return bedTypes;
  }

  /** The Constant BED_NAMES. */
  private static final String BED_NAMES =
      "SELECT bn.*, wn.ward_name, wn.allowed_gender, case when substring(upper(bed_name) "
      + "from '([0-9]+)') is null then 1 else substring(upper(bed_name) from '([0-9]+)')::BIGINT "
      + " end as bed_order FROM bed_names bn "
      + "  JOIN ward_names wn using(ward_no)";

  /**
   * Gets the all bed names.
   *
   * @param centerId
   *          the center id
   * @param multiCentered
   *          the multi centered
   * @return the all bed names
   * @throws SQLException
   *           the SQL exception
   */
  public static List getAllBedNames(int centerId, boolean multiCentered) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(
          BED_NAMES + (multiCentered && centerId != 0 ? " WHERE center_id = ? " : "")
              + " ORDER BY bed_order, ward_name ASC ");
      if (multiCentered && centerId != 0) {
        ps.setInt(1, centerId);
      }
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the all free bed names.
   *
   * @param multiCentered
   *          the multi centered
   * @param centerId
   *          the center id
   * @return the all free bed names
   * @throws SQLException
   *           the SQL exception
   */
  public static List getAllFreeBedNames(boolean multiCentered, int centerId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(BED_NAMES + " WHERE occupancy = 'N' "
          + "AND wn.status = 'A' AND bn.status = 'A' AND bn.bed_status = 'A' "
          + (multiCentered && centerId > 0 ? " AND wn.center_id = ?" : "")
          + " ORDER BY bed_order ASC ");
      if (multiCentered && centerId > 0) {
        ps.setInt(1, centerId);
      }
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant WARD_NAMES. */
  private static final String WARD_NAMES = "SELECT * FROM ward_names WHERE status='A' ";

  /**
   * Gets the all ward names.
   *
   * @param centerId
   *          the center id
   * @param multiCentered
   *          the multi centered
   * @return the all ward names
   * @throws SQLException
   *           the SQL exception
   */
  public static List getAllWardNames(int centerId, boolean multiCentered) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(WARD_NAMES
          + (multiCentered && centerId != 0 ? " AND center_id = ?" : "") + " ORDER BY ward_name ");
      if (multiCentered && centerId != 0) {
        ps.setInt(1, centerId);
      }
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant CHILD_BEDS. */
  private static final String CHILD_BEDS =
      "SELECT * FROM bed_names where bed_ref_id = ? AND status ='A' ";

  /**
   * Gets the child beds.
   *
   * @param con
   *          the con
   * @param bedId
   *          the bed id
   * @return the child beds
   * @throws SQLException
   *           the SQL exception
   */
  public static List<BasicDynaBean> getChildBeds(Connection con, int bedId) throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(CHILD_BEDS);
      ps.setInt(1, bedId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  /** Brings only non icu beds which are use ful for bystander bed allocation. */
  private static final String NON_ICU_BEDS =
      " SELECT distinct bed_type FROM bed_details where bed_status = 'A' ";

  /**
   * Gets the non ICU bedtypes.
   *
   * @return the non ICU bedtypes
   * @throws SQLException
   *           the SQL exception
   */
  public static List<BasicDynaBean> getNonIcuBedtypes() throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(NON_ICU_BEDS);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant BED_NAMES_WITH_TYPES. */
  private static final String BED_NAMES_WITH_TYPES =
      "SELECT bed_type, bed_name, ward_no, ward_name "
      + "    FROM bed_names bn " + "   JOIN ward_names USING (ward_no) "
      + "   WHERE bn.status ='A'";

  /**
   * Gets the bed names and types.
   *
   * @param centerId
   *          the center id
   * @param multicenterd
   *          the multicenterd
   * @return the bed names and types
   * @throws SQLException
   *           the SQL exception
   */
  public static List<BasicDynaBean> getBedNamesAndTypes(int centerId, boolean multicenterd)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(
          BED_NAMES_WITH_TYPES + (multicenterd && centerId != 0 ? " AND center_id = ?" : ""));

      if (multicenterd && centerId != 0) {
        ps.setInt(1, centerId);
      }
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant INSERT_CONSULTATIONS_CHARGE_PLUS. */
  private static final String INSERT_CONSULTATIONS_CHARGE_PLUS =
      "INSERT INTO consultation_charges(consultation_type_id,"
      + "org_id,charge,bed_type,discount)(SELECT consultation_type_id,org_id,"
      + " round(charge+?),?,discount FROM "
      + " consultation_charges    where bed_type=?)";

  /** The Constant INSERT_CONSULTATIONS_CHARGE_MINUS. */
  private static final String INSERT_CONSULTATIONS_CHARGE_MINUS =
      "INSERT INTO consultation_charges(consultation_type_id,"
      + "org_id,charge,bed_type,discount)(SELECT consultation_type_id,org_id,"
      + " round(charge-?),?,discount FROM "
      + " consultation_charges   where bed_type=?)";

  /** The Constant INSERT_CONSULTATIONS_CHARGE_BY. */
  private static final String INSERT_CONSULTATIONS_CHARGE_BY =
      "INSERT INTO consultation_charges(consultation_type_id,"
      + "org_id,charge,bed_type,discount)(SELECT consultation_type_id,org_id,"
      + " doroundvarying(charge,?,?),?,discount FROM "
      + " consultation_charges   where bed_type=?)";

  /**
   * Adds the bed for consultation type.
   *
   * @param con
   *          the con
   * @param newBedType
   *          the new bed type
   * @param baseBedForCharges
   *          the base bed for charges
   * @param varianceType
   *          the variance type
   * @param varianceBy
   *          the variance by
   * @param varianceValue
   *          the variance value
   * @param useValue
   *          the use value
   * @param nearstRoundOfValue
   *          the nearst round of value
   * @param userName
   *          the user name
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public boolean addBedForConsultationType(Connection con, String newBedType,
      String baseBedForCharges, String varianceType, Double varianceBy, Double varianceValue,
      boolean useValue, Double nearstRoundOfValue, String userName)
      throws SQLException, IOException {
    boolean status = false;
    PreparedStatement ps = null;

    if (useValue) {
      if (varianceType.equals("Incr")) {
        ps = con.prepareStatement(INSERT_CONSULTATIONS_CHARGE_PLUS);
        ps.setDouble(1, varianceValue);
        ps.setString(2, newBedType);
        ps.setString(3, baseBedForCharges);
        int ival = ps.executeUpdate();
        logger.debug(Integer.toString(ival));
        if (ival >= 0) {
          status = true;
        }
        logger.debug(Integer.toString(ival));
        if (ival >= 0) {
          status = true;
        }
      } else {
        ps = con.prepareStatement(INSERT_CONSULTATIONS_CHARGE_MINUS);
        ps.setDouble(1, varianceValue);
        ps.setString(2, newBedType);
        ps.setString(3, baseBedForCharges);

        int ival = ps.executeUpdate();
        logger.debug(Integer.toString(ival));
        if (ival >= 0) {
          status = true;
        }
        logger.debug(Integer.toString(ival));
        if (ival >= 0) {
          status = true;
        }

      }
    } else {
      ps = con.prepareStatement(INSERT_CONSULTATIONS_CHARGE_BY);
      if (!varianceType.equals("Incr")) {
        varianceBy = new Double(-varianceBy);
      }
      ps.setBigDecimal(1, new BigDecimal(varianceBy));
      ps.setBigDecimal(2, new BigDecimal(nearstRoundOfValue));
      ps.setString(3, newBedType);
      ps.setString(4, baseBedForCharges);

      int ival = ps.executeUpdate();
      logger.debug(Integer.toString(ival));
      if (ival >= 0) {
        status = true;
      }
    }
    ps.close();

    return status;
  }

  /**
   * Gets the bed type.
   *
   * @param bedType
   *          the bed type
   * @return the bed type
   * @throws SQLException
   *           the SQL exception
   */
  public BasicDynaBean getBedType(String bedType) throws SQLException {
    return new GenericDAO("bed_types").findByKey("bed_type_name", bedType);
  }

  /**
   * Update bed type.
   *
   * @param con
   *          the con
   * @param bedDetails
   *          the bed details
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public boolean updateBedType(Connection con, BedDetails bedDetails)
      throws SQLException, IOException {
    Map keys = new HashMap();
    GenericDAO dao = new GenericDAO("bed_types");
    keys.put("bed_type_name", bedDetails.getBedType());
    BasicDynaBean bedtypeBean = dao.getBean();
    bedtypeBean.set("status", bedDetails.getBedStatus());
    bedtypeBean.set("display_order", bedDetails.getDisplayOrder());
    bedtypeBean.set("billing_bed_type", bedDetails.getBillBedType());
    bedtypeBean.set("allow_zero_claim_amount", bedDetails.getAllowZeroClaimAmount());
    bedtypeBean.set("billing_group_id", bedDetails.getBillingGroupId());
    return dao.update(con, bedtypeBean.getMap(), keys) > 0;
  }

  /**
   * Insert bed type.
   *
   * @param con
   *          the con
   * @param bedDetails
   *          the bed details
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public boolean insertBedType(Connection con, BedDetails bedDetails)
      throws SQLException, IOException {
    GenericDAO dao = new GenericDAO("bed_types");
    BasicDynaBean bedtypeBean = dao.getBean();
    bedtypeBean.set("bed_type_name", bedDetails.getBedType());
    bedtypeBean.set("status", bedDetails.getBedStatus());
    bedtypeBean.set("display_order", bedDetails.getDisplayOrder());
    bedtypeBean.set("billing_bed_type", bedDetails.getBillBedType());
    bedtypeBean.set("is_icu", bedDetails.getIsIcu());
    bedtypeBean.set("is_child_bed", "N");
    bedtypeBean.set("allow_zero_claim_amount", bedDetails.getAllowZeroClaimAmount());
    bedtypeBean.set("billing_group_id", bedDetails.getBillingGroupId());
    GenericDAO.lockTable(con, "bed_types");// for applying table lock.
    return dao.insert(con, bedtypeBean);
  }

  /** The Constant ALL_BED_TYPES. */
  private static final String ALL_BED_TYPES = "SELECT * FROM bed_types WHERE status = 'A' ";

  /**
   * Gets the all bed types.
   *
   * @return the all bed types
   * @throws SQLException
   *           the SQL exception
   */
  public static List getAllBedTypes() throws SQLException {
    return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(ALL_BED_TYPES));
  }

  /**
   * Gets the all bed types list.
   *
   * @return the all bed types list
   * @throws SQLException
   *           the SQL exception
   */
  public static List getAllBedTypesList() throws SQLException {
    return DataBaseUtil.queryToDynaList(ALL_BED_TYPES + " ORDER BY bed_type_name");
  }

  /**
   * Gets the all billing bed types.
   *
   * @return the all billing bed types
   * @throws SQLException
   *           the SQL exception
   */
  public static List getAllBillingBedTypes() throws SQLException {
    return DataBaseUtil
        .queryToDynaList(ALL_BED_TYPES + " AND billing_bed_type = 'Y' ORDER BY bed_type_name");
  }

  /** The Constant INSERT_INTO_BED_DETAILS_PLUS. */
  private static final String INSERT_INTO_BED_DETAILS_PLUS =
      "INSERT INTO bed_details(bed_type, bed_charge, nursing_charge, initial_payment, duty_charge,"
      + "maintainance_charge, organization, charge_type, bed_status,"
      + "luxary_tax, intensive_bed_status, child_bed_status,"
      + "hourly_charge, daycare_slab_1_charge, daycare_slab_2_charge, daycare_slab_3_charge) "
      + "(SELECT bed_type, round(bed_charge + ?), round(nursing_charge + ?), "
      + " round(initial_payment + ?), round(duty_charge + ?),"
      + "round(maintainance_charge + ?), ?, charge_type, bed_status,"
      + "luxary_tax, intensive_bed_status, child_bed_status,"
      + "round(hourly_charge + ?), round(daycare_slab_1_charge + ?),"
      + " round(daycare_slab_2_charge + ?), round(daycare_slab_3_charge + ?)"
      + " FROM bed_details WHERE  organization=?);";

  /** The Constant INSERT_INTO_BED_DETAILS_MINUS. */
  private static final String INSERT_INTO_BED_DETAILS_MINUS =
      "INSERT INTO bed_details(bed_type, bed_charge, nursing_charge, initial_payment, duty_charge,"
      + "maintainance_charge, organization, charge_type, bed_status,"
      + "luxary_tax, intensive_bed_status, child_bed_status,"
      + "hourly_charge, daycare_slab_1_charge, daycare_slab_2_charge, daycare_slab_3_charge) "
      + " (SELECT bed_type, GREATEST(round(bed_charge - ?), 0), "
      + " GREATEST(round(nursing_charge - ?), 0), GREATEST(round(initial_payment - ?), 0),"
      + " GREATEST(round(duty_charge - ?), 0), GREATEST(round(maintainance_charge - ?), 0), ?,"
      + " charge_type, bed_status,"
      + "luxary_tax, intensive_bed_status, child_bed_status,"
      + "GREATEST(round(hourly_charge - ?), 0), GREATEST(round(daycare_slab_1_charge - ?), 0),"
      + " GREATEST(round(daycare_slab_2_charge - ?), 0), "
      + "GREATEST(round(daycare_slab_3_charge - ?), 0)"
      + " FROM bed_details WHERE  organization=?);";

  /** The Constant INSERT_INTO_BED_DETAILS_BY. */
  private static final String INSERT_INTO_BED_DETAILS_BY =
      "INSERT INTO bed_details(bed_type, organization, "
      + "bed_charge, nursing_charge, initial_payment, duty_charge,"
      + "maintainance_charge, charge_type, bed_status,"
      + "luxary_tax, intensive_bed_status, child_bed_status,"
      + "hourly_charge, daycare_slab_1_charge, daycare_slab_2_charge, daycare_slab_3_charge)"
      + " (SELECT bed_type, ?, doroundvarying(bed_charge,?,?), doroundvarying(nursing_charge,?,?), "
      + "doroundvarying(initial_payment,?,?), doroundvarying(duty_charge,?,?),"
      + "doroundvarying(maintainance_charge ,?, ?), charge_type, bed_status,"
      + "luxary_tax, intensive_bed_status, child_bed_status,"
      + "doroundvarying(hourly_charge,?,?), doroundvarying(daycare_slab_1_charge,?,?),"
      + " doroundvarying(daycare_slab_2_charge,?,?),"
      + "doroundvarying(daycare_slab_3_charge,?,?)  " + "FROM bed_details WHERE  organization=?);";

  /** The Constant INSERT_INTO_BED_DETAILS_WITH_DISCOUNTS_BY. */
  private static final String INSERT_INTO_BED_DETAILS_WITH_DISCOUNTS_BY =
      "INSERT INTO bed_details(bed_type, organization, "
      + "bed_charge, nursing_charge, initial_payment, duty_charge,"
      + "maintainance_charge, charge_type, bed_status,"
      + "luxary_tax, intensive_bed_status, child_bed_status,"
      + "hourly_charge, daycare_slab_1_charge, daycare_slab_2_charge, daycare_slab_3_charge,"
      + "bed_charge_discount, nursing_charge_discount,"
      + " initial_payment_discount, duty_charge_discount,"
      + "maintainance_charge_discount, "
      + "hourly_charge_discount, daycare_slab_1_charge_discount,"
      + " daycare_slab_2_charge_discount, daycare_slab_3_charge_discount)"
      + " (SELECT bed_type, ?, "
      + "doroundvarying(bed_charge,?,?), doroundvarying(nursing_charge,?,?), "
      + "doroundvarying(initial_payment,?,?), doroundvarying(duty_charge,?,?),"
      + "doroundvarying(maintainance_charge ,?, ?), charge_type, bed_status,"
      + "luxary_tax, intensive_bed_status, child_bed_status,"
      + "doroundvarying(hourly_charge,?,?), doroundvarying(daycare_slab_1_charge,?,?),"
      + " doroundvarying(daycare_slab_2_charge,?,?),"
      + "doroundvarying(daycare_slab_3_charge,?,?),"
      + "doroundvarying(bed_charge_discount,?,?), doroundvarying(nursing_charge_discount,?,?), "
      + "doroundvarying(initial_payment_discount,?,?), doroundvarying(duty_charge_discount,?,?),"
      + "doroundvarying(maintainance_charge_discount ,?, ?), "
      + "doroundvarying(hourly_charge_discount,?,?),"
      + " doroundvarying(daycare_slab_1_charge_discount,?,?),"
      + " doroundvarying(daycare_slab_2_charge_discount,?,?),"
      + "doroundvarying(daycare_slab_3_charge_discount,?,?)"
      + "FROM bed_details WHERE  organization=?);";

  /**
   * Adds the org for bed types.
   *
   * @param con
   *          the con
   * @param newOrgId
   *          the new org id
   * @param varianceType
   *          the variance type
   * @param varianceValue
   *          the variance value
   * @param varianceBy
   *          the variance by
   * @param useValue
   *          the use value
   * @param baseOrgId
   *          the base org id
   * @param nearstRoundOfValue
   *          the nearst round of value
   * @return true, if successful
   * @throws Exception
   *           the exception
   */
  public static boolean addOrgForBedTypes(Connection con, String newOrgId, String varianceType,
      Double varianceValue, Double varianceBy, boolean useValue, String baseOrgId,
      Double nearstRoundOfValue) throws Exception {

    return addOrgForBedTypes(con, newOrgId, varianceType, varianceValue, varianceBy, useValue,
        baseOrgId, nearstRoundOfValue, false);

  }

  /**
   * Adds the org for bed types.
   *
   * @param con
   *          the con
   * @param newOrgId
   *          the new org id
   * @param varianceType
   *          the variance type
   * @param varianceValue
   *          the variance value
   * @param varianceBy
   *          the variance by
   * @param useValue
   *          the use value
   * @param baseOrgId
   *          the base org id
   * @param nearstRoundOfValue
   *          the nearst round of value
   * @param updateDiscounts
   *          the update discounts
   * @return true, if successful
   * @throws Exception
   *           the exception
   */
  public static boolean addOrgForBedTypes(Connection con, String newOrgId, String varianceType,
      Double varianceValue, Double varianceBy, boolean useValue, String baseOrgId,
      Double nearstRoundOfValue, boolean updateDiscounts) throws Exception {

    boolean status = false;
    PreparedStatement ps = null;
    if (useValue) {
      if (varianceType.equals("Incr")) {
        ps = con.prepareStatement(INSERT_INTO_BED_DETAILS_PLUS);
      } else {
        ps = con.prepareStatement(INSERT_INTO_BED_DETAILS_MINUS);
      }

      ps.setBigDecimal(1, new BigDecimal(varianceValue));
      ps.setBigDecimal(2, new BigDecimal(varianceValue));
      ps.setBigDecimal(3, new BigDecimal(varianceValue));
      ps.setBigDecimal(4, new BigDecimal(varianceValue));
      ps.setBigDecimal(5, new BigDecimal(varianceValue));
      ps.setString(6, newOrgId);
      ps.setBigDecimal(7, new BigDecimal(varianceValue));
      ps.setBigDecimal(8, new BigDecimal(varianceValue));
      ps.setBigDecimal(9, new BigDecimal(varianceValue));
      ps.setBigDecimal(10, new BigDecimal(varianceValue));
      ps.setString(11, baseOrgId);

      int ival = ps.executeUpdate();
      logger.debug(Integer.toString(ival));
      if (ival >= 0) {
        status = true;
      }
    } else {

      /*
       * ps = con.prepareStatement(INSERT_INTO_BED_DETAILS_BY);
       * if(!varianceType.equals("Incr")){ varianceBy = new Double(-varianceBy); }
       * 
       * ps.setBigDecimal(1, new BigDecimal(varianceBy)); ps.setString(2, newOrgId);
       * ps.setBigDecimal(3, new BigDecimal(varianceBy)); ps.setBigDecimal(4, new
       * BigDecimal(nearstRoundOfValue)); ps.setBigDecimal(5, new
       * BigDecimal(varianceBy)); ps.setBigDecimal(6, new
       * BigDecimal(nearstRoundOfValue)); ps.setBigDecimal(7, new
       * BigDecimal(varianceBy)); ps.setBigDecimal(8, new
       * BigDecimal(nearstRoundOfValue)); ps.setBigDecimal(9, new
       * BigDecimal(varianceBy)); ps.setBigDecimal(10, new
       * BigDecimal(nearstRoundOfValue)); ps.setBigDecimal(11, new
       * BigDecimal(nearstRoundOfValue)); ps.setBigDecimal(12, new
       * BigDecimal(varianceBy)); ps.setBigDecimal(13, new
       * BigDecimal(nearstRoundOfValue)); ps.setBigDecimal(14, new
       * BigDecimal(varianceBy)); ps.setBigDecimal(15, new
       * BigDecimal(nearstRoundOfValue)); ps.setBigDecimal(16, new
       * BigDecimal(varianceBy)); ps.setBigDecimal(17, new
       * BigDecimal(nearstRoundOfValue)); ps.setBigDecimal(18, new
       * BigDecimal(varianceBy)); ps.setBigDecimal(19, new
       * BigDecimal(nearstRoundOfValue)); ps.setString(20, baseOrgId);
       * 
       * int ival = ps.executeUpdate();        logger.debug(Integer.toString(ival));
        if (ival >= 0) {
          status = true;
        }
       */
      int ival = insertChargesByPercent(con, newOrgId, baseOrgId, varianceBy, nearstRoundOfValue,
          updateDiscounts);
      logger.debug(Integer.toString(ival));
      if (ival >= 0) {
        status = true;
      }
    }

    if (null != ps) {
      ps.close();
    }

    return status;
  }

  /**
   * Insert charges by percent.
   *
   * @param con
   *          the con
   * @param newOrgId
   *          the new org id
   * @param baseOrgId
   *          the base org id
   * @param varianceBy
   *          the variance by
   * @param nearstRoundOfValue
   *          the nearst round of value
   * @param updateDiscounts
   *          the update discounts
   * @return the int
   * @throws Exception
   *           the exception
   */
  private static int insertChargesByPercent(Connection con, String newOrgId, String baseOrgId,
      Double varianceBy, Double nearstRoundOfValue, boolean updateDiscounts) throws Exception {

    int ndx = 1;
    int numCharges = 9;

    PreparedStatement pstmt = null;
    try {
      pstmt = con.prepareStatement(
          updateDiscounts ? INSERT_INTO_BED_DETAILS_WITH_DISCOUNTS_BY : INSERT_INTO_BED_DETAILS_BY);
      pstmt.setString(ndx++, newOrgId);

      for (int i = 0; i < numCharges; i++) {
        pstmt.setBigDecimal(ndx++, new BigDecimal(varianceBy));
        pstmt.setBigDecimal(ndx++, new BigDecimal(nearstRoundOfValue));
      }

      if (updateDiscounts) { // go one more round setting the parameters
        for (int i = 0; i < numCharges; i++) {
          pstmt.setBigDecimal(ndx++, new BigDecimal(varianceBy));
          pstmt.setBigDecimal(ndx++, new BigDecimal(nearstRoundOfValue));
        }
      }

      pstmt.setString(ndx++, baseOrgId);

      return pstmt.executeUpdate();

    } finally {

      if (null != pstmt) {
        pstmt.close();
      }
    }
  }

  /** The Constant INSERT_INTO_ICU. */
  private static final String INSERT_INTO_ICU =
      "INSERT INTO icu_bed_charges(bed_type,bed_charge,nursing_charge,initial_payment,duty_charge,"
      + "maintainance_charge,organization,charge_type,bed_status,"
      + "luxary_tax,intensive_bed_type, hourly_charge,daycare_slab_1_charge,daycare_slab_2_charge,"
      + "daycare_slab_3_charge)(SELECT bed_type,round(?),round(?),round(?),round(?),"
      + "round(?),?,charge_type,bed_status,"
      + "luxary_tax,intensive_bed_type,GREATEST(round(?), 0), GREATEST(round(?), 0),"
      + " GREATEST(round(?), 0), GREATEST(round(?), 0)"
      + " FROM icu_bed_charges WHERE intensive_bed_type = ? AND "
      + "organization=? AND bed_type=? )";

  /** The Constant INSERT_INTO_ICU_WITH_DISCOUNT. */
  private static final String INSERT_INTO_ICU_WITH_DISCOUNT =
      "INSERT INTO icu_bed_charges(bed_type,"
      + "bed_charge,nursing_charge,initial_payment,duty_charge,maintainance_charge,"
      + "organization,charge_type,bed_status," + "luxary_tax,intensive_bed_type, "
      + "hourly_charge,daycare_slab_1_charge,daycare_slab_2_charge,daycare_slab_3_charge, "
      + "bed_charge_discount,nursing_charge_discount,initial_payment_discount,duty_charge_discount,"
      + "maintainance_charge_discount, hourly_charge_discount,"
      + "daycare_slab_1_charge_discount,daycare_slab_2_charge_discount,"
      + " daycare_slab_3_charge_discount)"
      + "(SELECT bed_type," + "round(?),round(?),round(?),round(?),round(?),"
      + "?,charge_type,bed_status," + "luxary_tax,intensive_bed_type,"
      + "GREATEST(round(?), 0), GREATEST(round(?), 0), GREATEST(round(?), 0),"
      + " GREATEST(round(?), 0), "
      + "round(?),round(?),round(?),round(?)," + "round(?), GREATEST(round(?), 0),"
      + "GREATEST(round(?), 0),GREATEST(round(?), 0),GREATEST(round(?), 0)"
      + " FROM icu_bed_charges WHERE intensive_bed_type = ? AND "
      + "organization=? AND bed_type=? )";

  /** The Constant GET_ICU_DETAILS. */
  private static final String GET_ICU_DETAILS =
      "SELECT bed_type,bed_charge,nursing_charge,initial_payment,duty_charge,"
      + "maintainance_charge,luxary_tax,hourly_charge,daycare_slab_1_charge,"
      + " daycare_slab_2_charge,daycare_slab_3_charge,"
      + "bed_charge_discount,nursing_charge_discount,initial_payment_discount,duty_charge_discount,"
      + "maintainance_charge_discount,hourly_charge_discount,daycare_slab_1_charge_discount,"
      + " daycare_slab_2_charge_discount,daycare_slab_3_charge_discount,code_type,item_code"
      + " FROM icu_bed_charges WHERE intensive_bed_type=? AND organization=?";

  /** The Constant GET_DISTINCT_ICU_BEDS. */
  private static final String GET_DISTINCT_ICU_BEDS =
      "SELECT  distinct intensive_bed_type FROM icu_bed_charges;";

  /**
   * Adds the org for icu bed types.
   *
   * @param con
   *          the con
   * @param newOrgId
   *          the new org id
   * @param varianceType
   *          the variance type
   * @param varianceValue
   *          the variance value
   * @param varianceBy
   *          the variance by
   * @param useValue
   *          the use value
   * @param baseOrgId
   *          the base org id
   * @return true, if successful
   * @throws Exception
   *           the exception
   */
  public static boolean addOrgForIcuBedTypes(Connection con, String newOrgId, String varianceType,
      Double varianceValue, Double varianceBy, boolean useValue, String baseOrgId)
      throws Exception {
    return addOrgForIcuBedTypes(con, newOrgId, varianceType, varianceValue, varianceBy, useValue,
        baseOrgId, false);
  }

  /**
   * Adds the org for icu bed types.
   *
   * @param con
   *          the con
   * @param newOrgId
   *          the new org id
   * @param varianceType
   *          the variance type
   * @param varianceValue
   *          the variance value
   * @param varianceBy
   *          the variance by
   * @param useValue
   *          the use value
   * @param baseOrgId
   *          the base org id
   * @param updateDiscounts
   *          the update discounts
   * @return true, if successful
   * @throws Exception
   *           the exception
   */
  public static boolean addOrgForIcuBedTypes(Connection con, String newOrgId, String varianceType,
      Double varianceValue, Double varianceBy, boolean useValue, String baseOrgId,
      boolean updateDiscounts) throws Exception {

    boolean status = false;
    PreparedStatement ps = con.prepareStatement(GET_DISTINCT_ICU_BEDS);
    ArrayList<String> bedTypes = DataBaseUtil.queryToOnlyArrayList(ps);
    Iterator<String> it = bedTypes.iterator();
    ps.close();

    PreparedStatement ps1 = con.prepareStatement(GET_ICU_DETAILS);
    PreparedStatement ps2 = con
        .prepareStatement((updateDiscounts ? INSERT_INTO_ICU_WITH_DISCOUNT : INSERT_INTO_ICU));

    while (it.hasNext()) {
      String bedType = it.next();
      ps1.setString(1, bedType);
      ps1.setString(2, baseOrgId);

      ArrayList chargeList = DataBaseUtil.queryToArrayList(ps1);
      Iterator<Hashtable<String, String>> it1 = chargeList.iterator();
      while (it1.hasNext()) {
        Hashtable<String, String> ht = it1.next();

        Double bedCharge = new Double(ht.get("BED_CHARGE"));
        Double nursingCharge = new Double(ht.get("NURSING_CHARGE"));
        Double intialPayment = new Double(ht.get("INITIAL_PAYMENT"));
        Double dutyCharge = new Double(ht.get("DUTY_CHARGE"));
        Double maintainceCharge = new Double(ht.get("MAINTAINANCE_CHARGE"));
        Double hourlyCharge = new Double(ht.get("HOURLY_CHARGE"));
        Double daycareSlab1Chg = new Double(ht.get("DAYCARE_SLAB_1_CHARGE"));
        Double daycareSlab2Chg = new Double(ht.get("DAYCARE_SLAB_2_CHARGE"));
        Double daycareSlab3Chg = new Double(ht.get("DAYCARE_SLAB_3_CHARGE"));

        Double bedChargeDiscount = new Double(ht.get("BED_CHARGE_DISCOUNT"));
        Double nursingChargeDiscount = new Double(ht.get("NURSING_CHARGE_DISCOUNT"));
        Double intialPaymentDiscount = new Double(ht.get("INITIAL_PAYMENT_DISCOUNT"));
        Double dutyChargeDiscount = new Double(ht.get("DUTY_CHARGE_DISCOUNT"));
        Double maintainceChargeDiscount = new Double(ht.get("MAINTAINANCE_CHARGE_DISCOUNT"));
        Double hourlyChargeDiscount = new Double(ht.get("HOURLY_CHARGE_DISCOUNT"));
        Double daycareSlab1ChgDiscount = new Double(ht.get("DAYCARE_SLAB_1_CHARGE_DISCOUNT"));
        Double daycareSlab2ChgDiscount = new Double(ht.get("DAYCARE_SLAB_2_CHARGE_DISCOUNT"));
        Double daycareSlab3ChgDiscount = new Double(ht.get("DAYCARE_SLAB_3_CHARGE_DISCOUNT"));

        if (varianceType.equals("Incr")) {
          if (useValue) {
            bedCharge += varianceValue;
            nursingCharge += varianceValue;
            intialPayment += varianceValue;
            dutyCharge += varianceValue;
            maintainceCharge += varianceValue;
            hourlyCharge += varianceValue;
            daycareSlab1Chg += varianceValue;
            daycareSlab2Chg += varianceValue;
            daycareSlab3Chg += varianceValue;
            bedChargeDiscount += varianceValue;
            nursingChargeDiscount += varianceValue;
            intialPaymentDiscount += varianceValue;
            dutyChargeDiscount += varianceValue;
            maintainceChargeDiscount += varianceValue;
            hourlyChargeDiscount += varianceValue;
            daycareSlab1ChgDiscount += varianceValue;
            daycareSlab2ChgDiscount += varianceValue;
            daycareSlab3ChgDiscount += varianceValue;
          } else {
            bedCharge += bedCharge * (varianceBy.doubleValue() / 100);
            nursingCharge += nursingCharge * (varianceBy.doubleValue() / 100);
            intialPayment += intialPayment * (varianceBy.doubleValue() / 100);
            dutyCharge += dutyCharge * (varianceBy.doubleValue() / 100);
            maintainceCharge += maintainceCharge * (varianceBy.doubleValue() / 100);
            hourlyCharge += hourlyCharge * (varianceBy.doubleValue() / 100);
            daycareSlab1Chg += daycareSlab1Chg * (varianceBy.doubleValue() / 100);
            daycareSlab2Chg += daycareSlab2Chg * (varianceBy.doubleValue() / 100);
            daycareSlab3Chg += daycareSlab3Chg * (varianceBy.doubleValue() / 100);
            bedChargeDiscount += bedChargeDiscount * (varianceBy.doubleValue() / 100);
            nursingChargeDiscount += nursingChargeDiscount * (varianceBy.doubleValue() / 100);
            intialPaymentDiscount += intialPaymentDiscount * (varianceBy.doubleValue() / 100);
            dutyChargeDiscount += dutyChargeDiscount * (varianceBy.doubleValue() / 100);
            maintainceChargeDiscount += maintainceChargeDiscount * (varianceBy.doubleValue() / 100);
            hourlyChargeDiscount += hourlyChargeDiscount * (varianceBy.doubleValue() / 100);
            daycareSlab1ChgDiscount += daycareSlab1ChgDiscount * (varianceBy.doubleValue() / 100);
            daycareSlab2ChgDiscount += daycareSlab2ChgDiscount * (varianceBy.doubleValue() / 100);
            daycareSlab3ChgDiscount += daycareSlab3ChgDiscount * (varianceBy.doubleValue() / 100);
          }
        } else {
          if (useValue) {
            bedCharge -= varianceValue;
            nursingCharge -= varianceValue;
            intialPayment -= varianceValue;
            dutyCharge -= varianceValue;
            maintainceCharge -= varianceValue;
            hourlyCharge -= varianceValue;
            daycareSlab1Chg -= varianceValue;
            daycareSlab2Chg -= varianceValue;
            daycareSlab3Chg -= varianceValue;
            bedChargeDiscount -= varianceValue;
            nursingChargeDiscount -= varianceValue;
            intialPaymentDiscount -= varianceValue;
            dutyChargeDiscount -= varianceValue;
            maintainceChargeDiscount -= varianceValue;
            hourlyChargeDiscount -= varianceValue;
            daycareSlab1ChgDiscount -= varianceValue;
            daycareSlab2ChgDiscount -= varianceValue;
            daycareSlab3ChgDiscount -= varianceValue;
          } else {
            bedCharge -= bedCharge * (varianceBy.doubleValue() / 100);
            nursingCharge -= nursingCharge * (varianceBy.doubleValue() / 100);
            intialPayment -= intialPayment * (varianceBy.doubleValue() / 100);
            dutyCharge -= dutyCharge * (varianceBy.doubleValue() / 100);
            maintainceCharge -= maintainceCharge * (varianceBy.doubleValue() / 100);
            hourlyCharge -= hourlyCharge * (varianceBy.doubleValue() / 100);
            daycareSlab1Chg -= daycareSlab1Chg * (varianceBy.doubleValue() / 100);
            daycareSlab2Chg -= daycareSlab2Chg * (varianceBy.doubleValue() / 100);
            daycareSlab3Chg -= daycareSlab3Chg * (varianceBy.doubleValue() / 100);
            bedChargeDiscount -= bedChargeDiscount * (varianceBy.doubleValue() / 100);
            nursingChargeDiscount -= nursingChargeDiscount * (varianceBy.doubleValue() / 100);
            intialPaymentDiscount -= intialPaymentDiscount * (varianceBy.doubleValue() / 100);
            dutyChargeDiscount -= dutyChargeDiscount * (varianceBy.doubleValue() / 100);
            maintainceChargeDiscount -= maintainceChargeDiscount * (varianceBy.doubleValue() / 100);
            hourlyChargeDiscount -= hourlyChargeDiscount * (varianceBy.doubleValue() / 100);
            daycareSlab1ChgDiscount -= daycareSlab1ChgDiscount * (varianceBy.doubleValue() / 100);
            daycareSlab2ChgDiscount -= daycareSlab2ChgDiscount * (varianceBy.doubleValue() / 100);
            daycareSlab3ChgDiscount -= daycareSlab3ChgDiscount * (varianceBy.doubleValue() / 100);
          }

        }
        int ndx = 1;
        ps2.setDouble(ndx++, bedCharge);
        ps2.setDouble(ndx++, nursingCharge);
        ps2.setDouble(ndx++, intialPayment);
        ps2.setDouble(ndx++, dutyCharge);
        ps2.setDouble(ndx++, maintainceCharge);
        ps2.setString(ndx++, newOrgId);
        ps2.setDouble(ndx++, hourlyCharge);
        ps2.setDouble(ndx++, daycareSlab1Chg);
        ps2.setDouble(ndx++, daycareSlab2Chg);
        ps2.setDouble(ndx++, daycareSlab3Chg);
        if (updateDiscounts) {
          ps2.setDouble(ndx++, bedChargeDiscount);
          ps2.setDouble(ndx++, nursingChargeDiscount);
          ps2.setDouble(ndx++, intialPaymentDiscount);
          ps2.setDouble(ndx++, dutyChargeDiscount);
          ps2.setDouble(ndx++, maintainceChargeDiscount);
          ps2.setDouble(ndx++, hourlyChargeDiscount);
          ps2.setDouble(ndx++, daycareSlab1ChgDiscount);
          ps2.setDouble(ndx++, daycareSlab2ChgDiscount);
          ps2.setDouble(ndx++, daycareSlab3ChgDiscount);
        }
        String normalBed = ht.get("BED_TYPE");
        ps2.setString(ndx++, bedType);
        ps2.setString(ndx++, baseOrgId);
        ps2.setString(ndx++, normalBed);

        ps2.addBatch();

      }

    }

    int[] aval = ps2.executeBatch();
    status = DataBaseUtil.checkBatchUpdates(aval);
    if (ps1 != null) {
      ps1.close();
    }
    if (ps2 != null) {
      ps2.close();
    }

    return status;
  }

  /**
   * Inits the rate plan.
   *
   * @param con
   *          the con
   * @param newOrgId
   *          the new org id
   * @param varianceType
   *          the variance type
   * @param varianceBy
   *          the variance by
   * @param baseOrgId
   *          the base org id
   * @param roundOff
   *          the round off
   * @param userName
   *          the user name
   * @param orgName
   *          the org name
   * @return true, if successful
   * @throws Exception
   *           the exception
   */
  public boolean initRatePlan(Connection con, String newOrgId, String varianceType,
      Double varianceBy, String baseOrgId, Double roundOff, String userName, String orgName)
      throws Exception {
    // no org_details for registration
    boolean status = addOrgForBedTypes(con, newOrgId, varianceType, 0.0, varianceBy, false,
        baseOrgId, roundOff, true);
    if (status) {
      status = addOrgForIcuBedTypes(con, newOrgId, varianceType, 0.0, varianceBy, false, baseOrgId,
          /* roundOff, */true);
    }
    return status;
  }

  /**
   * Reinit rate plan.
   *
   * @param con
   *          the con
   * @param newOrgId
   *          the new org id
   * @param varianceType
   *          the variance type
   * @param variance
   *          the variance
   * @param baseOrgId
   *          the base org id
   * @param rndOff
   *          the rnd off
   * @param userName
   *          the user name
   * @param orgName
   *          the org name
   * @return true, if successful
   * @throws Exception
   *           the exception
   */
  public boolean reinitRatePlan(Connection con, String newOrgId, String varianceType,
      Double variance, String baseOrgId, Double rndOff, String userName, String orgName)
      throws Exception {
    boolean status = false;
    // disableAuditTriggers("operation_charges",
    // "z_operation_charges_audit_trigger");

    if (!varianceType.equals("Incr")) {
      variance = new Double(-variance);
    }

    BigDecimal varianceBy = new BigDecimal(variance);
    BigDecimal roundOff = new BigDecimal(rndOff);

    Object[] updparams = { varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff,
        varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff,
        varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff,
        varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff,
        varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff, newOrgId, baseOrgId };
    status = updateCharges(con, UPDATE_BEDCHARGES, updparams);
    if (status) {
      status = updateCharges(con, UPDATE_ICU_BEDCHARGES, updparams);
    }
    return status;
  }

  /** The Constant UPDATE__BEDDETAILS_PLUS. */
  private static final String UPDATE__BEDDETAILS_PLUS = "UPDATE bed_details totab SET "
      + " bed_charge = round(fromtab.bed_charge + ?), "
      + " nursing_charge = round(fromtab.nursing_charge + ?),"
      + " initial_payment = round(fromtab.initial_payment + ?), "
      + " duty_charge = round(fromtab.duty_charge + ?),"
      + " maintainance_charge = round(fromtab.maintainance_charge + ?), "
      + " hourly_charge = round(fromtab.hourly_charge + ?),"
      + " daycare_slab_1_charge = round(fromtab.daycare_slab_1_charge + ?), "
      + " daycare_slab_2_charge = round(fromtab.daycare_slab_2_charge + ?),"
      + " daycare_slab_3_charge = round(fromtab.daycare_slab_3_charge + ?)"
      + " FROM bed_details fromtab" + " WHERE totab.organization = ? AND fromtab.organization = ?"
      + " AND totab.bed_type = fromtab.bed_type";

  /** The Constant UPDATE_BEDDETAILS_MINUS. */
  private static final String UPDATE_BEDDETAILS_MINUS = "UPDATE bed_details totab SET "
      + " bed_charge = GREATEST(round(fromtab.bed_charge - ?), 0), "
      + " nursing_charge = GREATEST(round(fromtab.nursing_charge - ?), 0),"
      + " initial_payment = GREATEST(round(fromtab.initial_payment - ?), 0), "
      + " duty_charge = GREATEST(round(fromtab.duty_charge - ?), 0),"
      + " maintainance_charge = GREATEST(round(fromtab.maintainance_charge - ?), 0), "
      + " hourly_charge = GREATEST(round(fromtab.hourly_charge - ?), 0),"
      + " daycare_slab_1_charge = GREATEST(round(fromtab.daycare_slab_1_charge - ?), 0), "
      + " daycare_slab_2_charge = GREATEST(round(fromtab.daycare_slab_2_charge - ?), 0),"
      + " daycare_slab_3_charge = GREATEST(round(fromtab.daycare_slab_3_charge - ?), 0)"
      + " FROM bed_details fromtab" + " WHERE totab.organization = ? AND fromtab.organization = ?"
      + " AND totab.bed_type = fromtab.bed_type";

  /** The Constant UPDATE_BEDDETAILS_BY. */
  private static final String UPDATE_BEDDETAILS_BY = "UPDATE bed_details totab SET "
      + " bed_charge = doroundvarying(fromtab.bed_charge,?,?), "
      + " nursing_charge = doroundvarying(fromtab.nursing_charge,?,?),"
      + " initial_payment = doroundvarying(fromtab.initial_payment,?,?), "
      + " duty_charge = doroundvarying(fromtab.duty_charge,?,?),"
      + " maintainance_charge = doroundvarying(fromtab.maintainance_charge,?,?), "
      + " hourly_charge = doroundvarying(fromtab.hourly_charge,?,?),"
      + " daycare_slab_1_charge = doroundvarying(fromtab.daycare_slab_1_charge,?,?), "
      + " daycare_slab_2_charge = doroundvarying(fromtab.daycare_slab_2_charge,?,?),"
      + " daycare_slab_3_charge = doroundvarying(fromtab.daycare_slab_3_charge,?,?)"
      + " FROM bed_details fromtab" + " WHERE totab.organization = ? AND fromtab.organization = ?"
      + " AND totab.bed_type = fromtab.bed_type";

  /**
   * Update org for beddetailscharge.
   *
   * @param con
   *          the con
   * @param orgId
   *          the org id
   * @param varianceType
   *          the variance type
   * @param varianceValue
   *          the variance value
   * @param varianceBy
   *          the variance by
   * @param useValue
   *          the use value
   * @param baseOrgId
   *          the base org id
   * @param nearstRoundOfValue
   *          the nearst round of value
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public static boolean updateOrgForBeddetailscharge(Connection con, String orgId,
      String varianceType, Double varianceValue, Double varianceBy, boolean useValue,
      String baseOrgId, Double nearstRoundOfValue) throws SQLException, IOException {

    boolean status = false;
    PreparedStatement pstmt = null;

    if (useValue) {

      if (varianceType.equals("Incr")) {
        pstmt = con.prepareStatement(UPDATE__BEDDETAILS_PLUS);
      } else {
        pstmt = con.prepareStatement(UPDATE_BEDDETAILS_MINUS);
      }

      pstmt.setBigDecimal(1, new BigDecimal(varianceValue));
      pstmt.setBigDecimal(2, new BigDecimal(varianceValue));
      pstmt.setBigDecimal(3, new BigDecimal(varianceValue));
      pstmt.setBigDecimal(4, new BigDecimal(varianceValue));
      pstmt.setBigDecimal(5, new BigDecimal(varianceValue));
      pstmt.setBigDecimal(6, new BigDecimal(varianceValue));
      pstmt.setBigDecimal(7, new BigDecimal(varianceValue));
      pstmt.setBigDecimal(8, new BigDecimal(varianceValue));
      pstmt.setBigDecimal(9, new BigDecimal(varianceValue));
      pstmt.setString(10, orgId);
      pstmt.setString(11, baseOrgId);

      int ival = pstmt.executeUpdate();
      if (ival >= 0) {
        status = true;
      }

    } else {

      pstmt = con.prepareStatement(UPDATE_BEDDETAILS_BY);
      if (!varianceType.equals("Incr")) {
        varianceBy = new Double(-varianceBy);
      }

      pstmt.setBigDecimal(1, new BigDecimal(varianceBy));
      pstmt.setBigDecimal(2, new BigDecimal(nearstRoundOfValue));
      pstmt.setBigDecimal(3, new BigDecimal(varianceBy));
      pstmt.setBigDecimal(4, new BigDecimal(nearstRoundOfValue));
      pstmt.setBigDecimal(5, new BigDecimal(varianceBy));
      pstmt.setBigDecimal(6, new BigDecimal(nearstRoundOfValue));
      pstmt.setBigDecimal(7, new BigDecimal(varianceBy));
      pstmt.setBigDecimal(8, new BigDecimal(nearstRoundOfValue));
      pstmt.setBigDecimal(9, new BigDecimal(varianceBy));
      pstmt.setBigDecimal(10, new BigDecimal(nearstRoundOfValue));
      pstmt.setBigDecimal(11, new BigDecimal(varianceBy));
      pstmt.setBigDecimal(12, new BigDecimal(nearstRoundOfValue));
      pstmt.setBigDecimal(13, new BigDecimal(varianceBy));
      pstmt.setBigDecimal(14, new BigDecimal(nearstRoundOfValue));
      pstmt.setBigDecimal(15, new BigDecimal(varianceBy));
      pstmt.setBigDecimal(16, new BigDecimal(nearstRoundOfValue));
      pstmt.setBigDecimal(17, new BigDecimal(varianceBy));
      pstmt.setBigDecimal(18, new BigDecimal(nearstRoundOfValue));
      pstmt.setString(19, orgId);
      pstmt.setString(20, baseOrgId);

      int ival = pstmt.executeUpdate();
      if (ival >= 0) {
        status = true;
      }

    }
    pstmt.close();

    return status;
  }

  /** The Constant UPDATE__ICU_BEDDETAILS_PLUS. */
  private static final String UPDATE__ICU_BEDDETAILS_PLUS = "UPDATE icu_bed_charges totab SET "
      + " bed_charge = round(fromtab.bed_charge + ?), "
      + " nursing_charge = round(fromtab.nursing_charge + ?),"
      + " initial_payment = round(fromtab.initial_payment + ?), "
      + " duty_charge = round(fromtab.duty_charge + ?),"
      + " maintainance_charge = round(fromtab.maintainance_charge + ?), "
      + " hourly_charge = round(fromtab.hourly_charge + ?),"
      + " daycare_slab_1_charge = round(fromtab.daycare_slab_1_charge + ?), "
      + " daycare_slab_2_charge = round(fromtab.daycare_slab_2_charge + ?),"
      + " daycare_slab_3_charge = round(fromtab.daycare_slab_3_charge + ?)"
      + " FROM icu_bed_charges fromtab"
      + " WHERE totab.organization = ? AND fromtab.organization = ?"
      + " AND totab.bed_type = fromtab.bed_type "
      + " AND totab.intensive_bed_type = fromtab.intensive_bed_type";

  /** The Constant UPDATE_ICU_BEDDETAILS_MINUS. */
  private static final String UPDATE_ICU_BEDDETAILS_MINUS = "UPDATE icu_bed_charges totab SET "
      + " bed_charge = GREATEST(round(fromtab.bed_charge - ?), 0), "
      + " nursing_charge = GREATEST(round(fromtab.nursing_charge - ?), 0),"
      + " initial_payment = GREATEST(round(fromtab.initial_payment - ?), 0), "
      + " duty_charge = GREATEST(round(fromtab.duty_charge - ?), 0),"
      + " maintainance_charge = GREATEST(round(fromtab.maintainance_charge - ?), 0), "
      + " hourly_charge = GREATEST(round(fromtab.hourly_charge - ?), 0),"
      + " daycare_slab_1_charge = GREATEST(round(fromtab.daycare_slab_1_charge - ?), 0), "
      + " daycare_slab_2_charge = GREATEST(round(fromtab.daycare_slab_2_charge - ?), 0),"
      + " daycare_slab_3_charge = GREATEST(round(fromtab.daycare_slab_3_charge - ?), 0)"
      + " FROM icu_bed_charges fromtab"
      + " WHERE totab.organization = ? AND fromtab.organization = ?"
      + " AND totab.bed_type = fromtab.bed_type "
      + " AND totab.intensive_bed_type = fromtab.intensive_bed_type";

  /** The Constant UPDATE_ICU_BEDDETAILS_BY. */
  private static final String UPDATE_ICU_BEDDETAILS_BY = "UPDATE icu_bed_charges totab SET "
      + " bed_charge = doroundvarying(fromtab.bed_charge,?,?), "
      + " nursing_charge = doroundvarying(fromtab.nursing_charge,?,?),"
      + " initial_payment = doroundvarying(fromtab.initial_payment,?,?), "
      + " duty_charge = doroundvarying(fromtab.duty_charge,?,?),"
      + " maintainance_charge = doroundvarying(fromtab.maintainance_charge,?,?), "
      + " hourly_charge = doroundvarying(fromtab.hourly_charge,?,?),"
      + " daycare_slab_1_charge = doroundvarying(fromtab.daycare_slab_1_charge,?,?), "
      + " daycare_slab_2_charge = doroundvarying(fromtab.daycare_slab_2_charge,?,?),"
      + " daycare_slab_3_charge = doroundvarying(fromtab.daycare_slab_3_charge,?,?)"
      + " FROM icu_bed_charges fromtab"
      + " WHERE totab.organization = ? AND fromtab.organization = ?"
      + " AND totab.bed_type = fromtab.bed_type "
      + " AND totab.intensive_bed_type = fromtab.intensive_bed_type";

  /**
   * Update org for icu bedcharge.
   *
   * @param con
   *          the con
   * @param orgId
   *          the org id
   * @param varianceType
   *          the variance type
   * @param varianceValue
   *          the variance value
   * @param varianceBy
   *          the variance by
   * @param useValue
   *          the use value
   * @param baseOrgId
   *          the base org id
   * @param nearstRoundOfValue
   *          the nearst round of value
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public static boolean updateOrgForIcuBedcharge(Connection con, String orgId, String varianceType,
      Double varianceValue, Double varianceBy, boolean useValue, String baseOrgId,
      Double nearstRoundOfValue) throws SQLException, IOException {

    boolean status = false;
    PreparedStatement pstmt = null;

    if (useValue) {

      if (varianceType.equals("Incr")) {
        pstmt = con.prepareStatement(UPDATE__ICU_BEDDETAILS_PLUS);
      } else {
        pstmt = con.prepareStatement(UPDATE_ICU_BEDDETAILS_MINUS);
      }

      pstmt.setBigDecimal(1, new BigDecimal(varianceValue));
      pstmt.setBigDecimal(2, new BigDecimal(varianceValue));
      pstmt.setBigDecimal(3, new BigDecimal(varianceValue));
      pstmt.setBigDecimal(4, new BigDecimal(varianceValue));
      pstmt.setBigDecimal(5, new BigDecimal(varianceValue));
      pstmt.setBigDecimal(6, new BigDecimal(varianceValue));
      pstmt.setBigDecimal(7, new BigDecimal(varianceValue));
      pstmt.setBigDecimal(8, new BigDecimal(varianceValue));
      pstmt.setBigDecimal(9, new BigDecimal(varianceValue));
      pstmt.setString(10, orgId);
      pstmt.setString(11, baseOrgId);

      int ival = pstmt.executeUpdate();
      if (ival >= 0) {
        status = true;
      }

    } else {

      pstmt = con.prepareStatement(UPDATE_ICU_BEDDETAILS_BY);
      if (!varianceType.equals("Incr")) {
        varianceBy = new Double(-varianceBy);
      }

      pstmt.setBigDecimal(1, new BigDecimal(varianceBy));
      pstmt.setBigDecimal(2, new BigDecimal(nearstRoundOfValue));
      pstmt.setBigDecimal(3, new BigDecimal(varianceBy));
      pstmt.setBigDecimal(4, new BigDecimal(nearstRoundOfValue));
      pstmt.setBigDecimal(5, new BigDecimal(varianceBy));
      pstmt.setBigDecimal(6, new BigDecimal(nearstRoundOfValue));
      pstmt.setBigDecimal(7, new BigDecimal(varianceBy));
      pstmt.setBigDecimal(8, new BigDecimal(nearstRoundOfValue));
      pstmt.setBigDecimal(9, new BigDecimal(varianceBy));
      pstmt.setBigDecimal(10, new BigDecimal(nearstRoundOfValue));
      pstmt.setBigDecimal(11, new BigDecimal(varianceBy));
      pstmt.setBigDecimal(12, new BigDecimal(nearstRoundOfValue));
      pstmt.setBigDecimal(13, new BigDecimal(varianceBy));
      pstmt.setBigDecimal(14, new BigDecimal(nearstRoundOfValue));
      pstmt.setBigDecimal(15, new BigDecimal(varianceBy));
      pstmt.setBigDecimal(16, new BigDecimal(nearstRoundOfValue));
      pstmt.setBigDecimal(17, new BigDecimal(varianceBy));
      pstmt.setBigDecimal(18, new BigDecimal(nearstRoundOfValue));
      pstmt.setString(19, orgId);
      pstmt.setString(20, baseOrgId);

      int ival = pstmt.executeUpdate();
      if (ival >= 0) {
        status = true;
      }

    }
    pstmt.close();

    return status;
  }

  /** The Constant GET_BED_NAMES. */
  private static final String GET_BED_NAMES =
      " SELECT wn.ward_name,wn.ward_no , bn.bed_type,bn.bed_name,bn.bed_id,case "
      + " when substring(upper(bed_name) from '([0-9]+)') is null then 1 "
      + " else substring(upper(bed_name) from '([0-9]+)')::BIGINT end as bed_order "
      + " FROM bed_names bn "
      + " JOIN ward_names wn ON bn.ward_no=wn.ward_no WHERE bn.occupancy='N' "
      + " AND bn.bed_status = 'A' "
      + " AND bn.ward_no=? AND bn.bed_type = ? AND bn.status = 'A' ORDER BY bed_order ASC";

  /**
   * Gets the bednames for ward.
   *
   * @param wards
   *          the wards
   * @param bedType
   *          the bed type
   * @return the bednames for ward
   * @throws SQLException
   *           the SQL exception
   */
  public static List<BasicDynaBean> getBednamesForWard(String wards, String bedType)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_BED_NAMES);
      ps.setString(1, wards);
      ps.setString(2, bedType);
      logger.info(GET_BED_NAMES + " *** " + wards + " bedtype " + bedType);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_WARD_NAMES. */
  private static final String GET_WARD_NAMES =
      " SELECT wn.ward_name, wn.ward_no, wn.allowed_gender, bn.bed_type,"
      + " count(bn.bed_type) AS totalbeds,"
      + " (select count(bn1.bed_type)  " + " FROM bed_names bn1,ward_names wn1 "
      + " WHERE bn1.ward_no = wn1.ward_no AND "
      + " wn1.status = 'A' and bn1.occupancy='N'  AND  bn.bed_type = bn1.bed_type   AND "
      + " wn.ward_no=wn1.ward_no and bn1.status='A' AND bn1.bed_status = 'A' "
      + "   GROUP BY wn1.ward_name,bn1.bed_type,bn1.bed_type,wn1.ward_no ) as freebeds "
      + " FROM bed_names bn" + " JOIN ward_names wn ON (wn.ward_no = bn.ward_no) "
      + " JOIN bed_types bt on(bt.bed_type_name = bn.bed_type) " + " WHERE bn.ward_no = wn.ward_no "
      + " AND wn.status = 'A' AND bn.status='A' AND bn.bed_status = 'A' "
      + " AND bt.status='A' AND bed_type = ? #"
      + "   GROUP BY wn.ward_name,bn.bed_type,bn.bed_type,wn.ward_no ORDER BY wn.ward_name ";

  /**
   * Gets the wards for bed type.
   *
   * @param bedType
   *          the bed type
   * @param centerId
   *          the center id
   * @param multiCentered
   *          the multi centered
   * @return the wards for bed type
   * @throws SQLException
   *           the SQL exception
   */
  public List<BasicDynaBean> getWardsForBedType(String bedType, int centerId, boolean multiCentered)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_WARD_NAMES.replace("#",
          multiCentered && centerId != 0 ? " AND wn.center_id =? " : ""));
      ps.setString(1, bedType);
      if (multiCentered && centerId != 0) {
        ps.setInt(2, centerId);
      }
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_BED_TYPES. */
  private static final String GET_BED_TYPES =
      " SELECT wn.ward_name,wn.ward_no , bn.bed_type,count(bn.bed_type) AS totalbeds,"
      + " (select count(bn1.bed_type)  " + " FROM bed_names bn1,ward_names wn1 "
      + " WHERE bn1.ward_no = wn1.ward_no AND "
      + " wn1.status = 'A' and bn1.occupancy='N'  AND  bn.bed_type = bn1.bed_type   AND "
      + " wn.ward_no=wn1.ward_no and bn1.status='A' AND bn1.bed_status = 'A' "
      + "   GROUP BY wn1.ward_name,bn1.bed_type,bn1.bed_type,wn1.ward_no ) as freebeds "
      + " FROM bed_names bn" + " JOIN ward_names wn ON (wn.ward_no = bn.ward_no)"
      + " JOIN bed_types bt on(bt.bed_type_name = bn.bed_type) " + " WHERE bn.ward_no = wn.ward_no "
      + " AND wn.status = 'A' AND bn.status='A' AND bn.bed_status = 'A' "
      + " AND bt.status='A' AND wn.ward_no = ?"
      + "   GROUP BY wn.ward_name,bn.bed_type,bn.bed_type,wn.ward_no ORDER BY UPPER(bn.bed_type) ";

  /**
   * Gets the bed types for ward.
   *
   * @param ward
   *          the ward
   * @return the bed types for ward
   * @throws SQLException
   *           the SQL exception
   */
  public List<BasicDynaBean> getBedTypesForWard(String ward) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_BED_TYPES);
      ps.setString(1, ward);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The bed details query. */
  private String bedDetailsQuery = " SELECT * from bed_names "
      + " WHERE bed_name = ? AND ward_no = ? AND bed_id != ? ";

  /**
   * Checks if is duplicate bed.
   *
   * @param con
   *          the con
   * @param bedName
   *          the bed name
   * @param wardNo
   *          the ward no
   * @param bedId
   *          the bed id
   * @return true, if is duplicate bed
   * @throws SQLException
   *           the SQL exception
   */
  public boolean isDuplicateBed(Connection con, String bedName, String wardNo, int bedId)
      throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(bedDetailsQuery);
      ps.setString(1, bedName);
      ps.setString(2, wardNo);
      ps.setInt(3, bedId);

      return (DataBaseUtil.queryToDynaBean(ps) != null);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  /** The Constant UPDATE_RATEPLAN_BED_CHARGE. */
  private static final String UPDATE_RATEPLAN_BED_CHARGE =
      "UPDATE bed_details SET bed_charge=doroundvarying(bed_charge,?,?),"
      + " nursing_charge=doroundvarying(nursing_charge,?,?),"
      + " initial_payment=doroundvarying(initial_payment,?,?), "
      + " duty_charge=doroundvarying(duty_charge,?,?),"
      + " maintainance_charge=doroundvarying(maintainance_charge,?,?),"
      + " hourly_charge=doroundvarying(hourly_charge,?,?), "
      + " bed_charge_discount=doroundvarying(bed_charge_discount,?,?),"
      + " nursing_charge_discount=doroundvarying(nursing_charge_discount,?,?),"
      + " duty_charge_discount=doroundvarying(duty_charge_discount,?,?),"
      + " maintainance_charge_discount=doroundvarying(maintainance_charge_discount,?,?), "
      + " hourly_charge_discount=doroundvarying(hourly_charge_discount,?,?),"
      + " daycare_slab_1_charge=doroundvarying(daycare_slab_1_charge,?,?),"
      + " daycare_slab_2_charge=doroundvarying(daycare_slab_2_charge,?,?),"
      + " daycare_slab_3_charge=doroundvarying(daycare_slab_3_charge,?,?),"
      + " daycare_slab_1_charge_discount=doroundvarying(daycare_slab_1_charge_discount,?,?), "
      + " daycare_slab_2_charge_discount=doroundvarying(daycare_slab_2_charge_discount,?,?),"
      + " daycare_slab_3_charge_discount=doroundvarying(daycare_slab_3_charge_discount,?,?) "
      + " WHERE bed_type=? and organization=?";

  /**
   * Update bed charges for rate plan.
   *
   * @param ratePlanId
   *          the rate plan id
   * @param bedType
   *          the bed type
   * @param noOfcharges
   *          the no ofcharges
   * @param varianceBy
   *          the variance by
   * @param roundOff
   *          the round off
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean updateBedChargesForRatePlan(String ratePlanId, String bedType, int noOfcharges,
      Double varianceBy, Double roundOff) throws SQLException {

    Connection con = null;
    PreparedStatement ps = null;
    boolean success = false;

    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      ps = con.prepareStatement(UPDATE_RATEPLAN_BED_CHARGE);
      int index = 1;
      for (int i = 0; i < noOfcharges; i++) {
        ps.setBigDecimal(index++, new BigDecimal(varianceBy));
        ps.setBigDecimal(index++, new BigDecimal(roundOff));
      }
      ps.setString(index++, bedType);
      ps.setString(index++, ratePlanId);

      int kval = ps.executeUpdate();
      if (kval >= 0) {
        success = true;
      }

    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    return success;
  }

  /** The Constant UPDATE_BEDCHARGES. */
  private static final String UPDATE_BEDCHARGES = "UPDATE bed_details totab SET "
      + " bed_charge = doroundvarying(fromtab.bed_charge,?,?), "
      + " nursing_charge = doroundvarying(fromtab.nursing_charge,?,?),"
      + " initial_payment = doroundvarying(fromtab.initial_payment,?,?), "
      + " duty_charge = doroundvarying(fromtab.duty_charge,?,?),"
      + " maintainance_charge = doroundvarying(fromtab.maintainance_charge,?,?), "
      + " hourly_charge = doroundvarying(fromtab.hourly_charge,?,?),"
      + " daycare_slab_1_charge = doroundvarying(fromtab.daycare_slab_1_charge,?,?), "
      + " daycare_slab_2_charge = doroundvarying(fromtab.daycare_slab_2_charge,?,?),"
      + " daycare_slab_3_charge = doroundvarying(fromtab.daycare_slab_3_charge,?,?),"
      + " bed_charge_discount = doroundvarying(fromtab.bed_charge_discount,?,?), "
      + " nursing_charge_discount = doroundvarying(fromtab.nursing_charge_discount,?,?),"
      + " initial_payment_discount = doroundvarying(fromtab.initial_payment_discount,?,?), "
      + " duty_charge_discount = doroundvarying(fromtab.duty_charge_discount,?,?),"
      + " maintainance_charge_discount = doroundvarying(fromtab.maintainance_charge_discount,?,?), "
      + " hourly_charge_discount = doroundvarying(fromtab.hourly_charge_discount,?,?),"
      + " daycare_slab_1_charge_discount = doroundvarying("
      + " fromtab.daycare_slab_1_charge_discount,?,?), "
      + " daycare_slab_2_charge_discount = doroundvarying("
      + " fromtab.daycare_slab_2_charge_discount,?,?),"
      + " daycare_slab_3_charge_discount = doroundvarying("
      + " fromtab.daycare_slab_3_charge_discount,?,?), "
      + " luxary_tax = fromtab.luxary_tax " + " FROM bed_details fromtab"
      + " WHERE totab.organization = ? AND fromtab.organization = ? "
      + " AND totab.bed_type = fromtab.bed_type AND totab.is_override != 'Y'";

  /**
   * Update charges for derived rateplans.
   *
   * @param rateplanId
   *          the rateplan id
   * @param rateSheetId
   *          the rate sheet id
   * @param bedtype
   *          the bedtype
   * @param noOfcharges
   *          the no ofcharges
   * @param varianceBy
   *          the variance by
   * @param roundOff
   *          the round off
   * @param upload
   *          the upload
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean updateChargesForDerivedRateplans(String rateplanId, String rateSheetId,
      String bedtype, int noOfcharges, Double varianceBy, Double roundOff, boolean upload)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    boolean success = false;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      String query = upload ? UPDATE_BEDCHARGES : UPDATE_BEDCHARGES + " AND totab.bed_type=? ";
      ps = con.prepareStatement(query);
      int index = 1;
      for (int i = 0; i < noOfcharges; i++) {
        ps.setBigDecimal(index++, new BigDecimal(varianceBy));
        ps.setBigDecimal(index++, new BigDecimal(roundOff));
      }
      ps.setString(index++, rateplanId);
      ps.setString(index++, rateSheetId);
      if (!upload) {
        ps.setString(index++, bedtype);
      }

      int kval = ps.executeUpdate();
      if (kval >= 0) {
        success = true;
      }

    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    return success;
  }

  /** The Constant UPDATE_ICU_BEDCHARGES. */
  private static final String UPDATE_ICU_BEDCHARGES = "UPDATE icu_bed_charges totab SET "
      + " bed_charge = doroundvarying(fromtab.bed_charge,?,?),"
      + "nursing_charge = doroundvarying(fromtab.nursing_charge,?,?),"
      + " initial_payment = doroundvarying(fromtab.initial_payment,?,?), "
      + " duty_charge = doroundvarying(fromtab.duty_charge,?,?),"
      + " maintainance_charge = doroundvarying(fromtab.maintainance_charge,?,?), "
      + " hourly_charge = doroundvarying(fromtab.hourly_charge,?,?),"
      + " daycare_slab_1_charge = doroundvarying(fromtab.daycare_slab_1_charge,?,?), "
      + " daycare_slab_2_charge = doroundvarying(fromtab.daycare_slab_2_charge,?,?),"
      + " daycare_slab_3_charge = doroundvarying(fromtab.daycare_slab_3_charge,?,?),"
      + " bed_charge_discount = doroundvarying(fromtab.bed_charge_discount,?,?), "
      + " nursing_charge_discount = doroundvarying(fromtab.nursing_charge_discount,?,?), "
      + " duty_charge_discount = doroundvarying(fromtab.duty_charge_discount,?,?), "
      + " maintainance_charge_discount = doroundvarying(fromtab.maintainance_charge_discount,?,?), "
      + " hourly_charge_discount = doroundvarying(fromtab.hourly_charge_discount,?,?), "
      + " initial_payment_discount = doroundvarying(fromtab.initial_payment_discount,?,?), "
      + " daycare_slab_1_charge_discount = doroundvarying("
      + " fromtab.daycare_slab_1_charge_discount,?,?), "
      + " daycare_slab_2_charge_discount = doroundvarying("
      + " fromtab.daycare_slab_2_charge_discount,?,?), "
      + " daycare_slab_3_charge_discount = doroundvarying("
      + " fromtab.daycare_slab_3_charge_discount,?,?), "
      + " luxary_tax = fromtab.luxary_tax " + " FROM icu_bed_charges fromtab"
      + " WHERE totab.bed_type = fromtab.bed_type "
      + " AND totab.intensive_bed_type = fromtab.intensive_bed_type "
      + " AND totab.organization = ? AND fromtab.organization = ?  AND totab.is_override != 'Y' ";

  /**
   * Update ICU bed charges for derived rate plans.
   *
   * @param rateplanId
   *          the rateplan id
   * @param ratesheetId
   *          the ratesheet id
   * @param bedtype
   *          the bedtype
   * @param noofcharges
   *          the noofcharges
   * @param varianceBy
   *          the variance by
   * @param roundOff
   *          the round off
   * @param upload
   *          the upload
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean updateIcuBedChargesForDerivedRatePlans(String rateplanId, String ratesheetId,
      String bedtype, int noofcharges, Double varianceBy, Double roundOff, boolean upload)
      throws SQLException {

    Connection con = null;
    PreparedStatement ps = null;
    boolean success = false;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      String query = upload ? UPDATE_ICU_BEDCHARGES
          : UPDATE_ICU_BEDCHARGES + " AND totab.intensive_bed_type=? ";
      ps = con.prepareStatement(query);
      int index = 1;
      for (int i = 0; i < noofcharges; i++) {
        ps.setBigDecimal(index++, new BigDecimal(varianceBy));
        ps.setBigDecimal(index++, new BigDecimal(roundOff));
      }
      ps.setString(index++, rateplanId);
      ps.setString(index++, ratesheetId);
      if (!upload) {
        ps.setString(index++, bedtype);
      }

      int kval = ps.executeUpdate();
      if (kval >= 0) {
        success = true;
      }

    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    return success;
  }

  /** The Constant GET_DERIVED_RATE_PLAN_DETAILS. */
  private static final String GET_DERIVED_RATE_PLAN_DETAILS =
      " SELECT ps.org_id, ps.base_rate_sheet_id, "
      + " od.org_name, case when rate_variation_percent<0 then "
      + " 'Decrease By' else 'Increase By' end "
      + " as discormarkup,  ps.round_off_amount, ps.rate_variation_percent,bd.is_override "
      + " FROM bed_details bd "
      + " JOIN priority_rate_sheet_parameters_view ps on(bd.organization = ps.org_id) "
      + " JOIN organization_details od on (od.org_id = ps.org_id) "
      + " where ps.base_rate_sheet_id = ? and bd.bed_type=? ";

  /** The Constant GET_DERIVED_RATE_PLAN_DETAILS_FOR_ICUBEDTYPE. */
  private static final String GET_DERIVED_RATE_PLAN_DETAILS_FOR_ICUBEDTYPE =
      " SELECT ps.org_id, ps.base_rate_sheet_id,"
      + " od.org_name, case when rate_variation_percent<0 then"
      + " 'Decrease By' else 'Increase By' end "
      + " as discormarkup,  ps.round_off_amount, ps.rate_variation_percent,ic.is_override "
      + " FROM icu_bed_charges ic "
      + " JOIN priority_rate_sheet_parameters_view ps on(ic.organization = ps.org_id) "
      + " JOIN organization_details od on (od.org_id = ps.org_id) "
      + " where ps.base_rate_sheet_id = ? and ic.intensive_bed_type=? and bed_type='GENERAL' ";

  /**
   * Gets the derived rate plan details.
   *
   * @param baseRateSheetId
   *          the base rate sheet id
   * @param bedType
   *          the bed type
   * @param isIcu
   *          the is ICU
   * @return the derived rate plan details
   * @throws SQLException
   *           the SQL exception
   */
  public List<BasicDynaBean> getDerivedRatePlanDetails(String baseRateSheetId, String bedType,
      boolean isIcu) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      if (!isIcu) {
        ps = con.prepareStatement(GET_DERIVED_RATE_PLAN_DETAILS);
      } else {
        ps = con.prepareStatement(GET_DERIVED_RATE_PLAN_DETAILS_FOR_ICUBEDTYPE);
      }
      ps.setString(1, baseRateSheetId);
      ps.setString(2, bedType);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Update bed for rate plan.
   *
   * @param con
   *          the con
   * @param ratePlanId
   *          the rate plan id
   * @param variance
   *          the variance
   * @param rateSheetId
   *          the rate sheet id
   * @param rndOff
   *          the rnd off
   * @param bedType
   *          the bed type
   * @return true, if successful
   * @throws Exception
   *           the exception
   */
  public boolean updateBedForRatePlan(Connection con, String ratePlanId, Double variance,
      String rateSheetId, Double rndOff, String bedType) throws Exception {

    boolean status = false;

    BigDecimal varianceBy = new BigDecimal(variance);
    BigDecimal roundOff = new BigDecimal(rndOff);

    Object[] updparams = { varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff,
        varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff,
        varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff,
        varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff,
        varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff, ratePlanId, rateSheetId,
        bedType };
    status = updateCharges(con, UPDATE_ICU_BEDCHARGES + " AND totab.bed_type=? ", updparams);
    return status;
  }

  /** The Constant GET_BED_ITEM_SUB_GROUP_TAX_DETAILS. */
  private static final String GET_BED_ITEM_SUB_GROUP_TAX_DETAILS =
      " SELECT isg.item_subgroup_id, isg.subgroup_code, isg.item_subgroup_name, ig.group_code "
      + " FROM bed_item_sub_groups bisg "
      + " JOIN item_sub_groups isg ON(isg.item_subgroup_id = bisg.item_subgroup_id) "
      + " JOIN item_groups ig ON (ig.item_group_id = isg.item_group_id) "
      + " JOIN bed_names bn ON (bn.bed_type = bisg.bed_type_name) " + " WHERE bn.bed_id = ? ";

  /**
   * Gets the bed item sub group tax details.
   *
   * @param itemId
   *          the item id
   * @return the bed item sub group tax details
   * @throws SQLException
   *           the SQL exception
   */
  public List<BasicDynaBean> getBedItemSubGroupTaxDetails(String itemId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_BED_ITEM_SUB_GROUP_TAX_DETAILS);
      ps.setInt(1, Integer.parseInt(itemId));
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_BED_TYPE_ITEM_SUB_GROUP_TAX_DETAILS. */
  private static final String GET_BED_TYPE_ITEM_SUB_GROUP_TAX_DETAILS =
      "SELECT isg.item_subgroup_id, isg.subgroup_code, isg.item_subgroup_name, ig.group_code "
      + " FROM bed_item_sub_groups bisg "
      + " JOIN item_sub_groups isg ON(isg.item_subgroup_id = bisg.item_subgroup_id) "
      + " JOIN item_groups ig ON (ig.item_group_id = isg.item_group_id) "
      + " WHERE bisg.bed_type_name = ? ";

  /**
   * Gets the bed type item sub group tax details.
   *
   * @param itemId
   *          the item id
   * @return the bed type item sub group tax details
   * @throws SQLException
   *           the SQL exception
   */
  public List<BasicDynaBean> getBedTypeItemSubGroupTaxDetails(String itemId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_BED_TYPE_ITEM_SUB_GROUP_TAX_DETAILS);
      ps.setString(1, itemId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_BED_TYPE_SUBGROUP_DETAILS. */
  private static final String GET_BED_TYPE_SUBGROUP_DETAILS =
      "select bisg.item_subgroup_id,isg.item_subgroup_name,ig.item_group_id,"
      + " item_group_name,igt.item_group_type_id,igt.item_group_type_name "
      + " from bed_item_sub_groups bisg "
      + " left join item_sub_groups isg on (isg.item_subgroup_id = bisg.item_subgroup_id) "
      + " left join bed_types b on (b.bed_type_name = bisg.bed_type_name) "
      + " left join item_groups ig on (ig.item_group_id = isg.item_group_id)"
      + " left join item_group_type igt on (igt.item_group_type_id = ig.item_group_type_id)"
      + " where bisg.bed_type_name = ? ";

  /**
   * Gets the bed item sub group details.
   *
   * @param bedType
   *          the bed type
   * @return the bed item sub group details
   * @throws SQLException
   *           the SQL exception
   */
  public static List<BasicDynaBean> getBedItemSubGroupDetails(String bedType) throws SQLException {
    List list = null;
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_BED_TYPE_SUBGROUP_DETAILS);
      ps.setString(1, bedType);
      list = DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return list;
  }

  /**
   * Gets the active insurance categories.
   *
   * @param bedType
   *          the bed type
   * @return the active insurance categories
   * @throws SQLException
   *           the SQL exception
   */
  public List<BasicDynaBean> getActiveInsuranceCategories(String bedType) throws SQLException {
    PreparedStatement ps = null;
    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(SELECT_INSURANCE_CATEGORY_IDS);
      ps.setString(1, bedType);

      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant SELECT_INSURANCE_CATEGORY_IDS. */
  private static final String SELECT_INSURANCE_CATEGORY_IDS = "SELECT insurance_category_id "
      + "FROM bed_types_insurance_category_mapping WHERE bed_type_name =?";

  /** The Constant GET_BED_TYPE. */
  private static final String GET_BED_TYPE = "SELECT bed_type"
      + " FROM bed_names bn WHERE bn.bed_id = ? ";

  /**
   * Method to get list of bedtypes using bed ids.
   *
   * @param listItemIds
   *          the list item ids
   * @return the bed type list
   * @throws SQLException
   *           the SQL exception
   */
  public List<String> getBedTypeList(List<String> listItemIds) throws SQLException {
    List<String> bedTypeIds = new ArrayList<>();
    for (String bedId : listItemIds) {
      if (!bedId.isEmpty()) {
        String bedType = DataBaseUtil.getStringValueFromDb(GET_BED_TYPE, Integer.parseInt(bedId));
        bedTypeIds.add(bedType);
      }
    }
    return bedTypeIds;
  }
}
