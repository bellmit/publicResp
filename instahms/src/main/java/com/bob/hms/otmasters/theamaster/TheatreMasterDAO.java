package com.bob.hms.otmasters.theamaster;

import com.bob.hms.adminmasters.organization.OrgMasterDao;
import com.bob.hms.common.AutoIncrementId;
import com.bob.hms.common.Constants;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.adminmasters.bedmaster.BedMasterDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.rateplan.ItemChargeDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TheatreMasterDAO extends ItemChargeDAO {

  public TheatreMasterDAO() {
    super("theatre_charges");
  }

  Logger logger = LoggerFactory.getLogger(TheatreMasterDAO.class);

  private static final BedMasterDAO bddao = new BedMasterDAO();

  public static final String GET_THEATRES = "SELECT THEATRE_ID,THEATRE_NAME"
      + " FROM THEATRE_MASTER WHERE STATUS='A' ORDER BY THEATRE_NAME";

  public ArrayList getTheatreMastDetails() {
    return DataBaseUtil.queryToArrayList(GET_THEATRES);
  }

  /** Rate Plan ID fetch query. **/
  private static final String RP_QUERY = "select org_id from organization_details where org_name=?";

  private static final String CHARGE_QUERY = "select daily_charge as  charge from theatre_charges"
      + " where theatre_id=? and org_id = ? and bed_type = ?";

  /**
   * Get Theatre Details.
   * @param theatreId Theatre ID
   * @param orgId Rate Plan ID Rate Plan ID
   * @param bedType Bed Type
   * @return List of DynaBeans
   * @throws SQLException  Query execution exception
   */
  public List getTheatreMaster(String theatreId, String orgId, String bedType) throws SQLException {
    List cl = null;
    try (Connection con = DataBaseUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(CHARGE_QUERY)) {

      String generalorgid = DataBaseUtil.getStringValueFromDb(RP_QUERY,
          Constants.getConstantValue("ORG"));
      String generalbedtype = Constants.getConstantValue("BEDTYPE");

      ps.setString(1, theatreId);
      ps.setString(2, orgId);
      ps.setString(3, bedType);

      cl = DataBaseUtil.queryToArrayList(ps);
      logger.debug("{}", cl);

      if (!cl.isEmpty()) {
        ps.setString(1, theatreId);
        ps.setString(2, generalorgid);
        ps.setString(3, generalbedtype);
        cl = DataBaseUtil.queryToArrayList(ps);
        logger.debug("{}", cl);
      }

    } catch (Exception ex) {
      logger.error("Exception in getTheatreMaster method", ex);
    }
    return cl;
  }

  public static final String GET_THEATRE = " SELECT t.theatre_id, t.theatre_name, tc.daily_charge,"
      + " tc.min_charge, tc.incr_charge,"
      + " tm.min_duration, tm.incr_duration, tc.daily_charge_discount, "
      + " tc.min_charge_discount,tc.incr_charge_discount,"
      + " tc.slab_1_charge,tc.slab_1_charge_discount,"
      + " tm.duration_unit_minutes,tm.slab_1_threshold,tm.billing_group_id "
      + " FROM theatre_master t " + " JOIN theatre_charges tc USING (theatre_id) "
      + " LEFT JOIN theatre_master tm USING (theatre_id) " + " WHERE t.status='A' ";

  public static final String GET_THEATRE_CHARGES = GET_THEATRE
      + " AND tc.bed_type=? AND tc.org_id=? " + " UNION " + GET_THEATRE
      + " AND tc.bed_type='GENERAL' AND tc.org_id='ORG0001' "
      + " AND NOT EXISTS (SELECT theatre_id FROM theatre_charges WHERE theatre_id = t.theatre_id "
      + " AND bed_type=? AND org_id=?) ";

  /**
   * Get theatre charges.
   * @param bedType Bed Type
   * @param orgId Rate Plan ID Rate Plan ID
   * @return List of DynaBean
   * @throws SQLException  Query execution exception
   */
  public List getTheatreCharges(String bedType, String orgId) throws SQLException {
    List list = null;
    try (Connection con = DataBaseUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(GET_THEATRE_CHARGES)) {
      ps.setString(1, bedType);
      ps.setString(2, orgId);
      ps.setString(3, bedType);
      ps.setString(4, orgId);
      list = DataBaseUtil.queryToArrayList(ps);
    }
    return list;
  }

  /**
   * Get theatre charges for new operation.
   * @return Map representing Theatre Charges.
   * @throws Exception Generic exception
   */
  public Map getTheatreChargesForNewOperation() throws Exception {

    LinkedHashMap<String, ArrayList<String>> map = new LinkedHashMap<>();
    ArrayList<String> beds = new ArrayList<>();
    ArrayList<String> dailyChgarge = new ArrayList<>();
    ArrayList<String> minCharge = new ArrayList<>();
    ArrayList<String> slab1Charge = new ArrayList<>();
    ArrayList<String> incrCharge = new ArrayList<>();
    ArrayList<String> dailyChargeDiscount = new ArrayList<>();
    ArrayList<String> minChargeDiscount = new ArrayList<>();
    ArrayList<String> slab1ChargeDiscount = new ArrayList<>();
    ArrayList<String> incrChargeDiscount = new ArrayList<>();

    ArrayList<Hashtable<String, String>> bedTypes = bddao.getUnionOfAllBedTypes();
    Iterator<Hashtable<String, String>> it = bedTypes.iterator();

    while (it.hasNext()) {
      Hashtable<String, String> ht = it.next();
      beds.add(ht.get("BED_TYPE"));
      dailyChgarge.add(null);
      dailyChargeDiscount.add(null);
      minCharge.add(null);
      minChargeDiscount.add(null);
      slab1Charge.add(null);
      slab1ChargeDiscount.add(null);
      incrCharge.add(null);
      incrChargeDiscount.add(null);
    }

    map.put("CHARGES", beds);
    map.put("dailyChgarge", dailyChgarge);
    map.put("dailyChargeDiscount", dailyChargeDiscount);
    map.put("minCharge", minCharge);
    map.put("minChargeDiscount", minChargeDiscount);
    map.put("slab1Charge", slab1Charge);
    map.put("slab1ChargeDiscount", slab1ChargeDiscount);
    map.put("incrCharge", incrCharge);
    map.put("incrChargeDiscount", incrChargeDiscount);

    return map;
  }

  private static final String GET_ALL_CHARGES = "SELECT tc.daily_charge,tc.min_charge,"
      + " tc.incr_charge,tc.daily_charge_discount,tc.min_charge_discount,tc.incr_charge_discount,"
      + " tc.slab_1_charge, tc.slab_1_charge_discount "
      + " FROM theatre_charges tc WHERE tc.theatre_id=? and tc.bed_type=? and tc.org_id=?";

  /**
   * Get theatre charges for editing.
   * @param theatreId Theatre ID
   * @param orgId Rate Plan ID
   * @return map contianing theatre charges.
   * @throws Exception Generic exception
   */
  public Map getTheatreChargesForEdit(String theatreId, String orgId) throws Exception {
    LinkedHashMap<String, ArrayList<String>> map = new LinkedHashMap<>();
    ArrayList<String> beds = new ArrayList<>();
    ArrayList<String> dailyChgarge = new ArrayList<>();
    ArrayList<String> minCharge = new ArrayList<>();
    ArrayList<String> slab1Charge = new ArrayList<>();
    ArrayList<String> incrCharge = new ArrayList<>();
    ArrayList<String> dailyChargeDiscount = new ArrayList<>();
    ArrayList<String> minChargeDiscount = new ArrayList<>();
    ArrayList<String> slab1ChargeDiscount = new ArrayList<>();
    ArrayList<String> incrChargeDiscount = new ArrayList<>();

    ArrayList<Hashtable<String, String>> bedTypes = bddao.getUnionOfAllBedTypes();
    Iterator<Hashtable<String, String>> it = bedTypes.iterator();
    Connection con = DataBaseUtil.getReadOnlyConnection();
    PreparedStatement ps = con.prepareStatement(GET_ALL_CHARGES);

    while (it.hasNext()) {

      Hashtable<String, String> ht = it.next();
      String bedType = ht.get("BED_TYPE");
      beds.add(bedType);
      ps.setString(1, theatreId);
      ps.setString(2, bedType);
      ps.setString(3, orgId);

      ArrayList<Hashtable<String, String>> al = DataBaseUtil.queryToArrayList(ps);
      Iterator<Hashtable<String, String>> chargeIt = al.iterator();
      if (chargeIt.hasNext()) {
        Hashtable<String, String> chargeht = chargeIt.next();
        dailyChgarge.add(chargeht.get("DAILY_CHARGE"));
        dailyChargeDiscount.add(chargeht.get("DAILY_CHARGE_DISCOUNT"));
        minCharge.add(chargeht.get("MIN_CHARGE"));
        minChargeDiscount.add(chargeht.get("MIN_CHARGE_DISCOUNT"));
        slab1Charge.add(chargeht.get("SLAB_1_CHARGE"));
        slab1ChargeDiscount.add(chargeht.get("SLAB_1_CHARGE_DISCOUNT"));
        incrCharge.add(chargeht.get("INCR_CHARGE"));
        incrChargeDiscount.add(chargeht.get("INCR_CHARGE_DISCOUNT"));
      } else {
        dailyChgarge.add(null);
        dailyChargeDiscount.add(null);
        minCharge.add(null);
        minChargeDiscount.add(null);
        slab1Charge.add(null);
        slab1ChargeDiscount.add(null);
        incrCharge.add(null);
        incrChargeDiscount.add(null);
      }
      map.put("CHARGES", beds);
      map.put("dailyChgarge", dailyChgarge);
      map.put("dailyChargeDiscount", dailyChargeDiscount);
      map.put("minCharge", minCharge);
      map.put("minChargeDiscount", minChargeDiscount);
      map.put("slab1Charge", slab1Charge);
      map.put("slab1ChargeDiscount", slab1ChargeDiscount);
      map.put("incrCharge", incrCharge);
      map.put("incrChargeDiscount", incrChargeDiscount);
    }

    ps.close();
    con.close();

    return map;
  }

  private static final String GET_THEATRE_DEF = "SELECT tm.theatre_name,tm.status,"
      + " coalesce(tm.min_duration,0) as min_duration,"
      + " coalesce(tm.incr_duration,0) as incr_duration,schedule, center_id,"
      + " duration_unit_minutes,overbook_limit, slab_1_threshold,store_id,"
      + " allow_zero_claim_amount, "
      + " billing_group_id FROM theatre_master tm WHERE tm.theatre_id = ?";

  /**
   * Get theatre definition.
   * @param theatreId Theatre ID
   * @return Theatre definition.
   * @throws SQLException  Query execution exception
   */
  public ArrayList<Hashtable<String, String>> getTheatreDef(String theatreId) throws SQLException {
    try (Connection con = DataBaseUtil.getReadOnlyConnection();
        PreparedStatement ps = con.prepareStatement(GET_THEATRE_DEF)) {
      ps.setString(1, theatreId);
      return DataBaseUtil.queryToArrayList(ps);
    }
  }

  /**
   * Get next theatre ID.
   * @return theatre id
   * @throws SQLException  Query execution exception
   */
  public String getNextTheatreId() throws SQLException {
    String id = null;
    id = AutoIncrementId.getNewIncrUniqueId("THEATRE_ID", "THEATRE_MASTER", "THEATERID");

    return id;
  }

  private static final String INSERT_THEATRE = "INSERT INTO theatre_master(theatre_id,"
      + " theatre_name,status,min_duration,incr_duration,schedule,"
      + " center_id,duration_unit_minutes,slab_1_threshold,"
      + " overbook_limit,store_id,allow_zero_claim_amount,billing_group_id)"
      + " values (?,?,?,?,?,?,?,?,?,?,?,?,?)";
  private static final String UPDATE_THEATRE_DEF = "UPDATE theatre_master SET theatre_name=?,"
      + " status=?,min_duration=?,incr_duration=?,schedule=?,"
      + " duration_unit_minutes=?, slab_1_threshold=?,overbook_limit=?,"
      + " store_id=?,allow_zero_claim_amount=?,billing_group_id=? WHERE theatre_id=?";

  private static final String CHECK_THEATRE_PRESENT = "SELECT count(*) FROM theatre_master"
      + " WHERE theatre_id=? ";

  /**
   * Create or update theatre definition.
   * @param con Database connection
   * @param th Theatre Object
   * @return true or false representing status of operation
   * @throws SQLException  Query execution exception
   */
  public boolean addOrEditTheatreDef(Connection con, Theatre th) throws SQLException {
    boolean status = false;
    PreparedStatement ps = con.prepareStatement(INSERT_THEATRE);
    PreparedStatement ups = con.prepareStatement(UPDATE_THEATRE_DEF);
    PreparedStatement rps = con.prepareStatement(CHECK_THEATRE_PRESENT);

    rps.setString(1, th.getTheatreId());
    ;
    String count = DataBaseUtil.getStringValueFromDb(rps);
    if (count.equals("0")) {
      ps.setString(1, th.getTheatreId());
      ps.setString(2, th.getTheatreName());
      ps.setString(3, th.getStatus());
      ps.setInt(4, th.getMinDuration());
      ps.setInt(5, th.getIncrDuration());
      ps.setBoolean(6, th.getSchedule());
      ps.setInt(7, th.getCenterId());
      ps.setInt(8, th.getUnitSize());
      ps.setInt(9, th.getSlab1Threshold());
      ps.setObject(10, th.getOverbookLimit());
      ps.setInt(11, th.getStoreId());
      ps.setString(12, th.getAllowZeroClaimAmount());
      ps.setObject(13, th.getBillingGroupId());

      status = ps.executeUpdate() > 0;

    } else {
      ups.setString(1, th.getTheatreName());
      ups.setString(2, th.getStatus());
      ups.setInt(3, th.getMinDuration());
      ups.setInt(4, th.getIncrDuration());
      ups.setBoolean(5, th.getSchedule());
      ups.setInt(6, th.getUnitSize());
      ups.setInt(7, th.getSlab1Threshold());
      ups.setObject(8, th.getOverbookLimit());
      ups.setInt(9, th.getStoreId());
      ups.setString(10, th.getAllowZeroClaimAmount());
      ups.setObject(11, th.getBillingGroupId());
      ups.setString(12, th.getTheatreId());

      status = ups.executeUpdate() > 0;
    }

    ps.close();
    ups.close();
    rps.close();

    return status;
  }

  private static final String INSERT_THEATRE_CHARGES = "INSERT INTO theatre_charges("
      + "theatre_id,org_id,bed_type,"
      + "daily_charge,min_charge,incr_charge,daily_charge_discount,min_charge_discount,"
      + "incr_charge_discount,slab_1_charge,slab_1_charge_discount)VALUES(?,?,?,"
      + "?,?,?,?,?,?,?,?)";
  private static final String UPDATE_THEATRE_CHARGES = "UPDATE theatre_charges SET daily_charge=?,"
      + "min_charge=?,incr_charge=?,daily_charge_discount=?,min_charge_discount=?,"
      + "incr_charge_discount=?,slab_1_charge=?,slab_1_charge_discount=?  WHERE theatre_id=?"
      + " AND org_id=? AND bed_type=? ";

  private static final String CHEC_THEATRE_CHARGE = "SELECT count(*) FROM theatre_charges"
      + " WHERE theatre_id=? AND org_id=? AND bed_type=?";

  /**
   * Add or update theatre charges.
   * @param con Database connection
   * @param theatreList Theatre List
   * @return true or false representing status of operation.
   * @throws SQLException  Query execution exception
   */
  public boolean addOrEditTheatreCharges(Connection con, ArrayList<TheatreCharges> theatreList)
      throws SQLException {
    boolean status = false;
    PreparedStatement ps = con.prepareStatement(INSERT_THEATRE_CHARGES);
    PreparedStatement ups = con.prepareStatement(UPDATE_THEATRE_CHARGES);

    PreparedStatement rps = con.prepareStatement(CHEC_THEATRE_CHARGE);

    Iterator<TheatreCharges> it = theatreList.iterator();
    while (it.hasNext()) {
      TheatreCharges tc = it.next();
      rps.setString(1, tc.getTheatreId());
      rps.setString(2, tc.getOrgId());
      rps.setString(3, tc.getBedType());

      String count = DataBaseUtil.getStringValueFromDb(rps);
      if (count.equals("0")) {
        ps.setString(1, tc.getTheatreId());
        ps.setString(2, tc.getOrgId());
        ps.setString(3, tc.getBedType());
        ps.setBigDecimal(4, tc.getDailyCharge());
        ps.setBigDecimal(5, tc.getMinCharge());
        ps.setBigDecimal(6, tc.getIncrCharge());
        ps.setBigDecimal(7, tc.getDailyChargeDiscount());
        ps.setBigDecimal(8, tc.getMinChargeDiscount());
        ps.setBigDecimal(9, tc.getIncrChargeDiscount());
        ps.setBigDecimal(10, tc.getSlab1Charge());
        ps.setBigDecimal(11, tc.getSlab1ChargeDiscount());
        ps.addBatch();

      } else {
        ups.setBigDecimal(1, tc.getDailyCharge());
        ups.setBigDecimal(2, tc.getMinCharge());
        ups.setBigDecimal(3, tc.getIncrCharge());
        ups.setBigDecimal(4, tc.getDailyChargeDiscount());
        ups.setBigDecimal(5, tc.getMinChargeDiscount());
        ups.setBigDecimal(6, tc.getIncrChargeDiscount());
        ups.setBigDecimal(7, tc.getSlab1Charge());
        ups.setBigDecimal(8, tc.getSlab1ChargeDiscount());
        ups.setString(9, tc.getTheatreId());
        ups.setString(10, tc.getOrgId());
        ups.setString(11, tc.getBedType());

        ups.addBatch();

      }

    }
    do {
      int[] firstBatchUpdateCount = ps.executeBatch();
      status = DataBaseUtil.checkBatchUpdates(firstBatchUpdateCount);
      if (!status) {
        break;
      }

      int[] secondBatchUpdateCount = ups.executeBatch();
      status = DataBaseUtil.checkBatchUpdates(secondBatchUpdateCount);

    } while (false);

    ups.close();
    ps.close();
    rps.close();

    return status;
  }

  private static final String THEATRE_FIELDS = "SELECT OT.theatre_name,OT.theatre_id,ot.status,"
      + " OT.center_id,OT.store_id";
  private static final String THEATRE_COUNT = "SELECT count(*) ";
  private static final String THEATRE_FROM_TABLES = " FROM theatre_master OT  ";

  private static final String GET_ALL_BED_TYPES = " SELECT distinct bed_type FROM theatre_charges"
      + " WHERE org_id = ?";

  private static final String GET_DAILY_CHARGE = "SELECT OTC.daily_charge, OTC.bed_type"
      + " FROM theatre_charges OTC WHERE OTC.bed_type=? AND OTC.org_id=? AND OTC.theatre_id=?";

  private static final String GET_HOURLY_CHARGE = "SELECT OTC.min_charge,OTC.bed_type"
      + " FROM theatre_charges OTC WHERE OTC.bed_type=? AND OTC.org_id=? AND OTC.theatre_id=? ";

  private static final String GET_SLAB1_CHARGE = "SELECT OTC.slab_1_charge, OTC.bed_type"
      + " FROM theatre_charges OTC WHERE OTC.bed_type=? AND OTC.org_id=? AND OTC.theatre_id=?";

  private static final String GET_INCR_CHARGE = "SELECT OTC.incr_charge,OTC.bed_type"
      + " FROM theatre_charges OTC WHERE OTC.bed_type=? AND OTC.org_id=? AND OTC.theatre_id=?";

  /**
   * Get theatre details.
   * @param statusList Status List
   * @param orgId Rate Plan ID
   * @param chargeType Charge Type
   * @param centerId Center ID
   * @param pageNum Page number
   * @return paginated list with theatre details.
   * @throws SQLException  Query execution exception
   */
  public PagedList getTheatreDetails(ArrayList<String> statusList, String orgId, String chargeType,
      int centerId, int pageNum) throws SQLException {
    LinkedHashMap<String, ArrayList> map = new LinkedHashMap<>();
    map.put("dailyCharge", null);
    map.put("hourlyCharge", null);
    map.put("slab1Chareg", null);
    map.put("incrCharge", null);
    ArrayList<Map> al = new ArrayList<>();
    int count = 0;
    
    PreparedStatement psCount = null;
    PreparedStatement ps = null;
    PreparedStatement psData = null;
    Connection con = null;
    
    try { 

      con = DataBaseUtil.getReadOnlyConnection();
  
      SearchQueryBuilder qb = new SearchQueryBuilder(con, THEATRE_FIELDS, THEATRE_COUNT,
          THEATRE_FROM_TABLES, null, null, "OT.theatre_name", false, 25, pageNum);
      qb.addFilter(SearchQueryBuilder.STRING, "OT.status", "IN", statusList);
  
      if (centerId != 0) {
        qb.addFilter(SearchQueryBuilder.INTEGER, "OT.center_id", "=", centerId);
      }
  
      qb.build();
  
      if (chargeType.equals("DC")) {
        ps = con.prepareStatement(GET_DAILY_CHARGE);
      } else if (chargeType.equals("HC")) {
        ps = con.prepareStatement(GET_HOURLY_CHARGE);
      } else if (chargeType.equals("IC")) {
        ps = con.prepareStatement(GET_INCR_CHARGE);
      } else if (chargeType.equals("SC")) {
        ps = con.prepareStatement(GET_SLAB1_CHARGE);
      }
  
      ArrayList<List> allTheatres = new ArrayList<>();
      ArrayList<String> headers = new ArrayList<>();
      ArrayList<String> bedTypes = BedMasterDAO.getUnionOfBedTypes();
      headers.addAll(bedTypes);
      allTheatres.add(headers);
  
      psData  = qb.getDataStatement();
      ArrayList<Hashtable<String, String>> theatres = DataBaseUtil.queryToArrayList(psData);
      Iterator<Hashtable<String, String>> it = theatres.iterator();
      while (it.hasNext()) {
        Hashtable<String, String> ht = it.next();
  
        ArrayList<String> operationRecord = new ArrayList<>();
        operationRecord.add(ht.get("STATUS"));
        operationRecord.add(ht.get("THEATRE_ID"));
        operationRecord.add(ht.get("THEATRE_NAME"));
        operationRecord.add(ht.get("STORE_ID"));
        if (((int)GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")) > 1) {
          operationRecord.add(ht.get("CENTER_ID"));
        }
        Iterator<String> beds = bedTypes.iterator();
        while (beds.hasNext()) {
          String bed = beds.next();
          ps.setString(1, bed);
          ps.setString(2, orgId);
          ps.setString(3, ht.get("THEATRE_ID"));
          operationRecord.add(DataBaseUtil.getStringValueFromDb(ps));
        }
        allTheatres.add(operationRecord);
      }
      logger.debug("{}", allTheatres);
      if (chargeType.equals("DC")) {
        map.put("dailyCharge", allTheatres);
      } else if (chargeType.equals("HC")) {
        map.put("hourlyCharge", allTheatres);
      } else if (chargeType.equals("IC")) {
        map.put("incrCharge", allTheatres);
      } else if (chargeType.equals("SC")) {
        map.put("slab1Charge", allTheatres);
      }
  
      al.add(map);
  
      psCount  = qb.getCountStatement();
      count = Integer.parseInt((DataBaseUtil.getStringValueFromDb(psCount)));
      
      return new PagedList(al, count, 25, pageNum);
    } finally {
      if ( psCount != null && !psCount.isClosed() ) {
        psCount.close();
      }
      DataBaseUtil.closeConnections(con, psData);
    }
    
  }

  /**
   * Bulk update theatres.
   * @param con Database connection
   * @param orgId Rate Plan ID
   * @param bedTypes List of bed types
   * @param theatreArr Array of theatre ids
   * @param groupUpdate Part of group update
   * @param amount Amount
   * @param isPercentage is percentage ?
   * @param roundOff Roundoff for charges
   * @param updateTable Update Table
   * @throws SQLException  Query execution exception
   */
  public void groupUpdateTheatres(Connection con, String orgId, List<String> bedTypes,
      List<String> theatreArr, String groupUpdate, BigDecimal amount, boolean isPercentage,
      BigDecimal roundOff, String updateTable) throws SQLException {

    if (roundOff.compareTo(BigDecimal.ZERO) == 0) {
      groupIncreaseChargesNoRoundOff(con, orgId, bedTypes, theatreArr, groupUpdate, amount,
          isPercentage, updateTable);
    } else {
      groupIncreaseChargesWithRoundOff(con, orgId, bedTypes, theatreArr, groupUpdate, amount,
          isPercentage, roundOff, updateTable);
    }
  }

  private static final String GROUP_INCR_THEATRE_CHARGES = " UPDATE theatre_charges "
      + " SET # = GREATEST( round((# + ?)/?,0)*?, 0) "
      + " WHERE org_id=? ";

  private static final String GROUP_INCR_THEATRE_CHARGES_PERCENTAGE = " UPDATE theatre_charges "
      + " SET # = GREATEST( round(#*(100+?)/100/?,0)*?, 0) "
      + " WHERE org_id=? ";

  private static String GROUP_INCR_DISCOUNTS = " UPDATE theatre_charges"
      + " SET # = LEAST(GREATEST( round((# + ?)/?,0)*?, 0), @) "
      + " WHERE org_id=? ";

  private static String GROUP_INCR_DISCOUNTS_PERCENTAGE = " UPDATE theatre_charges"
      + " SET # = LEAST(GREATEST( round(#*(100+?)/100/?,0)*?, 0), @) "
      + " WHERE org_id=? ";

  private static String GROUP_APPLY_DISCOUNTS = " UPDATE theatre_charges"
      + " SET @ = LEAST(GREATEST( round((# + ?)/?,0)*?, 0), #) "
      + " WHERE org_id=? ";

  private static String GROUP_APPLY_DISCOUNT_PERCENTAGE = " UPDATE theatre_charges"
      + " SET @ = LEAST(GREATEST( round(#+(# * ?/100/?),0)*?, 0), #) "
      + " WHERE org_id=? ";

  /**
   * Group increase charges with round off.
   * @param con Database connection
   * @param orgId Rate Plan ID
   * @param bedTypes List of bed types
   * @param theatreArr Array of theatre ids
   * @param groupUpdate Part of group update
   * @param amount Amount
   * @param isPercentage is percentage ?
   * @param roundOff Roundoff for charges
   * @param updateTable Update Table
   * @throws SQLException  Query execution exception
   */
  public void groupIncreaseChargesWithRoundOff(Connection con, String orgId, List<String> bedTypes,
      List<String> theatreArr, String groupUpdate, BigDecimal amount, boolean isPercentage,
      BigDecimal roundOff, String updateTable) throws SQLException {

    StringBuilder query = null;
    String chargeType = null;

    if (groupUpdate.equals("DC")) {
      chargeType = "daily_charge";
    } else if (groupUpdate.equals("HC")) {
      chargeType = "min_charge";
    } else if (groupUpdate.equals("IC")) {
      chargeType = "incr_charge";
    } else if (groupUpdate.equals("SC")) {
      chargeType = "slab_1_charge";
    }

    if (updateTable != null && updateTable.equals("UPDATECHARGE")) {
      query = new StringBuilder(isPercentage
          ? GROUP_INCR_THEATRE_CHARGES_PERCENTAGE.replaceAll("#",
              DataBaseUtil.quoteIdent(chargeType))
          : GROUP_INCR_THEATRE_CHARGES.replaceAll("#", DataBaseUtil.quoteIdent(chargeType)));

    } else if (updateTable.equals("UPDATEDISCOUNT")) {

      GROUP_INCR_DISCOUNTS = GROUP_INCR_DISCOUNTS.replaceAll("@", chargeType);
      GROUP_INCR_DISCOUNTS_PERCENTAGE = GROUP_INCR_DISCOUNTS_PERCENTAGE.replaceAll("@", chargeType);

      chargeType = chargeType + "_discount";
      query = new StringBuilder(isPercentage
          ? GROUP_INCR_DISCOUNTS_PERCENTAGE.replaceAll("#", DataBaseUtil.quoteIdent(chargeType))
          : GROUP_INCR_DISCOUNTS.replaceAll("#", DataBaseUtil.quoteIdent(chargeType)));
    } else {

      String chargeTypeDiscount = chargeType + "_discount";

      if (isPercentage) {
        GROUP_APPLY_DISCOUNT_PERCENTAGE = GROUP_APPLY_DISCOUNT_PERCENTAGE.replaceAll("@",
            chargeTypeDiscount);
        GROUP_APPLY_DISCOUNT_PERCENTAGE = GROUP_APPLY_DISCOUNT_PERCENTAGE.replaceAll("#",
            chargeType);
      } else {
        GROUP_APPLY_DISCOUNTS = GROUP_APPLY_DISCOUNTS.replaceAll("@", chargeTypeDiscount);
        GROUP_APPLY_DISCOUNTS = GROUP_APPLY_DISCOUNTS.replaceAll("#", chargeType);
      }
      query = new StringBuilder(
          isPercentage ? GROUP_APPLY_DISCOUNT_PERCENTAGE : GROUP_APPLY_DISCOUNTS);
    }

    SearchQueryBuilder.addWhereFieldOpValue(true, query, "bed_type", "IN", bedTypes);
    SearchQueryBuilder.addWhereFieldOpValue(true, query, "theatre_id", "IN", theatreArr);

    PreparedStatement ps = con.prepareStatement(query.toString());

    int index = 1;
    ps.setBigDecimal(index++, amount);
    ps.setBigDecimal(index++, roundOff);
    ps.setBigDecimal(index++, roundOff);
    ps.setString(index++, orgId);

    if (bedTypes != null) {
      for (String bedType : bedTypes) {
        ps.setString(index++, bedType);
      }
    }

    if (theatreArr != null) {
      for (String theatre : theatreArr) {
        ps.setString(index++, theatre);
      }
    }

    ps.executeUpdate();
    if (ps != null) {
      ps.close();
    }
  }

  private static final String GROUP_INCR_THEATRE_CHARGES_NO_ROUNDOFF = " UPDATE theatre_charges"
      + " SET # = GREATEST( # + ?, 0) "
      + " WHERE org_id=? ";

  private static final String GROUP_INCR_THEATRE_CHARGES_PERCENTAGE_NO_ROUNDOFF = " UPDATE"
      + " theatre_charges SET # = GREATEST(# +( # * ? / 100 ) , 0) "
      + " WHERE org_id=? ";

  private static String GROUP_INCR_DISCOUNTS_NO_ROUNDOFF = " UPDATE theatre_charges"
      + " SET # = LEAST(GREATEST( # + ?, 0), @) "
      + " WHERE org_id=? ";

  private static String GROUP_INCR_DISCOUNTS_PERCENTAGE_NO_ROUNDOFF = " UPDATE theatre_charges"
      + " SET # = LEAST(GREATEST(# +( # * ? / 100 ) , 0), @) "
      + " WHERE org_id=? ";

  private static String GROUP_APPLY_DISCOUNTS_NO_ROUNDOFF = " UPDATE theatre_charges"
      + " SET @ = LEAST(GREATEST( # + ?, 0), #) "
      + " WHERE org_id=? ";

  private static String GROUP_APPLY_DISCOUNT_PERCENTAGE_NO_ROUNDOFF = " UPDATE theatre_charges"
      + " SET @ = LEAST(GREATEST(# + ( # * ? / 100) , 0), #) "
      + " WHERE org_id=? ";

  /**
   * Group increase charges without round off.
   * @param con Database connection
   * @param orgId Rate Plan ID
   * @param bedTypes List of bed types
   * @param theatreArr Array of theatre ids
   * @param groupUpdate Part of group update
   * @param amount Amount
   * @param isPercentage is percentage ?
   * @param updateTable Update Table
   * @throws SQLException  Query execution exception
   */
  public void groupIncreaseChargesNoRoundOff(Connection con, String orgId, List<String> bedTypes,
      List<String> theatreArr, String groupUpdate, BigDecimal amount, boolean isPercentage,
      String updateTable) throws SQLException {

    StringBuilder query = null;
    String chargeType = null;

    if (groupUpdate.equals("DC")) {
      chargeType = "daily_charge";
    } else if (groupUpdate.equals("HC")) {
      chargeType = "min_charge";
    } else if (groupUpdate.equals("IC")) {
      chargeType = "incr_charge";
    } else if (groupUpdate.equals("SC")) {
      chargeType = "slab_1_charge";
    }

    if (updateTable != null && updateTable.equals("UPDATECHARGE")) {
      query = new StringBuilder(isPercentage
          ? GROUP_INCR_THEATRE_CHARGES_PERCENTAGE_NO_ROUNDOFF.replaceAll("#",
              DataBaseUtil.quoteIdent(chargeType))
          : GROUP_INCR_THEATRE_CHARGES_NO_ROUNDOFF.replaceAll("#",
              DataBaseUtil.quoteIdent(chargeType)));

    } else if (updateTable.equals("UPDATEDISCOUNT")) {

      GROUP_INCR_DISCOUNTS_NO_ROUNDOFF = GROUP_INCR_DISCOUNTS_NO_ROUNDOFF.replaceAll("@",
          chargeType);
      GROUP_INCR_DISCOUNTS_PERCENTAGE_NO_ROUNDOFF = GROUP_INCR_DISCOUNTS_PERCENTAGE_NO_ROUNDOFF
          .replaceAll("@", chargeType);

      chargeType = chargeType + "_discount";
      query = new StringBuilder(isPercentage
          ? GROUP_INCR_DISCOUNTS_PERCENTAGE_NO_ROUNDOFF.replaceAll("#",
              DataBaseUtil.quoteIdent(chargeType))
          : GROUP_INCR_DISCOUNTS_NO_ROUNDOFF.replaceAll("#", DataBaseUtil.quoteIdent(chargeType)));
    } else {

      String chargeTypeDiscount = chargeType + "_discount";

      if (isPercentage) {
        GROUP_APPLY_DISCOUNT_PERCENTAGE_NO_ROUNDOFF = GROUP_APPLY_DISCOUNT_PERCENTAGE_NO_ROUNDOFF
            .replaceAll("@", chargeTypeDiscount);
        GROUP_APPLY_DISCOUNT_PERCENTAGE_NO_ROUNDOFF = GROUP_APPLY_DISCOUNT_PERCENTAGE_NO_ROUNDOFF
            .replaceAll("#", chargeType);
      } else {
        GROUP_APPLY_DISCOUNTS_NO_ROUNDOFF = GROUP_APPLY_DISCOUNTS_NO_ROUNDOFF.replaceAll("@",
            chargeTypeDiscount);
        GROUP_APPLY_DISCOUNTS_NO_ROUNDOFF = GROUP_APPLY_DISCOUNTS_NO_ROUNDOFF.replaceAll("#",
            chargeType);
      }
      query = new StringBuilder(isPercentage ? GROUP_APPLY_DISCOUNT_PERCENTAGE_NO_ROUNDOFF
          : GROUP_APPLY_DISCOUNTS_NO_ROUNDOFF);
    }

    SearchQueryBuilder.addWhereFieldOpValue(true, query, "bed_type", "IN", bedTypes);
    SearchQueryBuilder.addWhereFieldOpValue(true, query, "theatre_id", "IN", theatreArr);

    PreparedStatement ps = con.prepareStatement(query.toString());

    int index = 1;
    ps.setBigDecimal(index++, amount);
    ps.setString(index++, orgId);

    if (bedTypes != null) {
      for (String bedType : bedTypes) {
        ps.setString(index++, bedType);
      }
    }

    if (theatreArr != null) {
      for (String theatre : theatreArr) {
        ps.setString(index++, theatre);
      }
    }

    ps.executeUpdate();
    if (ps != null) {
      ps.close();
    }
  }

  private static final String CHECK_DUPLICATE = "SELECT count(*) FROM theatre_master"
      + " WHERE theatre_name=?";

  /**
   * Check if theatre name is in use.
   * @param newTheatre Theatre Name
   * @return true or false representing if theatre with given name exists.
   * @throws SQLException  Query execution exception
   */
  public static boolean checkDuplicate(String newTheatre) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(CHECK_DUPLICATE);
      ps.setString(1, newTheatre);
      String count = DataBaseUtil.getStringValueFromDb(ps);
      return !count.equals("0");
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  public static final String GET_THEATRE_CHARGES_OF_A_THEATRE = GET_THEATRE
      + "  AND tc.bed_type=? AND tc.org_id=? AND theatre_id =? ";


  /**
   * get theatre charge details.
   * @param theatreId Theatre ID
   * @param bedType Bed Type
   * @param orgid Rate Plan ID
   * @return theatre charge details
   * @throws SQLException  Query execution exception
   */
  public BasicDynaBean getTheatreChargeDetails(String theatreId, String bedType, String orgid)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    List list = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_THEATRE_CHARGES_OF_A_THEATRE);
      ps.setString(1, bedType);
      ps.setString(2, orgid);
      ps.setString(3, theatreId);
      list = DataBaseUtil.queryToDynaList(ps);
      if (ps != null) {
        ps.close();
      }
      if (list.size() == 0) {
        ps = con.prepareStatement(GET_THEATRE_CHARGES_OF_A_THEATRE);
        ps.setString(1, "GENERAL");
        ps.setString(2, "ORG0001");
        ps.setString(3, theatreId);
        list = DataBaseUtil.queryToDynaList(ps);
      }
      if (list.size() > 0) {
        return (BasicDynaBean) list.get(0);
      } else {
        return null;
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Get store for given theatre.
   * @param theatreId Theatre ID
   * @return store id.
   * @throws SQLException  Query execution exception
   */
  public static int getStoreOfOpearationTheatre(String theatreId) throws SQLException {
    BasicDynaBean bean = new GenericDAO("theatre_master").findByKey("theatre_id", theatreId);
    int storeId = (bean != null) ? (Integer) bean.get("store_id") : -3;
    return storeId;
  }

  private static final String THEATRES_NAMESAND_iDS = "select theatre_name,theatre_id"
      + " from theatre_master";

  /**
   * Get list of theatre names and ids.
   * @return List of theatre name and theatre ids
   * @throws SQLException  Query execution exception
   */
  public static List getTheatresNamesAndIds() throws SQLException {
    return ConversionUtils
        .copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(THEATRES_NAMESAND_iDS));
  }

  private static final String INSERT_INTO_THEATRES = "INSERT INTO theatre_charges("
      + " theatre_id,org_id,bed_type,daily_charge,"
      + " min_charge,incr_charge,tax,slab_1_charge) VALUES (?,?,?, GREATEST(round(?), 0),"
      + " GREATEST(round(?), 0), GREATEST(round(?),0), GREATEST(round(?),0), GREATEST(round(?),0))";

  private static final String SELECT_THEATRES = "SELECT distinct theatre_id"
      + " FROM theatre_charges order by theatre_id";

  private static final String GET_THEATRE_CHARGES_QUERY = "SELECT "
      + " coalesce(daily_charge,0) as daily_charge,coalesce(min_charge,0) as min_charge,"
      + " coalesce(incr_charge,0) as incr_charge, coalesce(tax,0)as tax,"
      + " coalesce(slab_1_charge,0)as slab_1_charge"
      + " FROM theatre_charges WHERE theatre_id=? AND  org_id=? AND bed_type=? ";

  private static final String GET_DISTINCT_BEDTYPES_FOR_THEATRE = "SELECT distinct bed_type"
      + " FROM theatre_charges WHERE theatre_id=?";

  private static final String INSERT_INTO_THEATRES_BY = "INSERT INTO theatre_charges("
      + " theatre_id,org_id,bed_type,"
      + " daily_charge,min_charge,incr_charge,tax,slab_1_charge)"
      + " (SELECT theatre_id,?,bed_type,doroundvarying(daily_charge,?,?),"
      + " doroundvarying(min_charge,?,?),doroundvarying(incr_charge,?,?),tax,"
      + " doroundvarying(slab_1_charge,?,?) FROM theatre_charges"
      + " WHERE org_id=? )";

  private static final String INSERT_INTO_THEA_WITH_DISCOUNTS_BY = "INSERT INTO theatre_charges("
      + " theatre_id,org_id,bed_type,"
      + " daily_charge,min_charge,incr_charge,tax,slab_1_charge,"
      + " daily_charge_discount,min_charge_discount,incr_charge_discount,slab_1_charge_discount) "
      + " (SELECT theatre_id,?,bed_type," + " doroundvarying(daily_charge,?,?),"
      + " doroundvarying(min_charge,?,?),doroundvarying(incr_charge,?,?), tax,"
      + " doroundvarying(slab_1_charge,?,?),"
      + " doroundvarying(daily_charge_discount,?,?),"
      + " doroundvarying(min_charge_discount,?,?),doroundvarying(incr_charge_discount,?,?),"
      + " doroundvarying(slab_1_charge_discount,?,?)"
      + " FROM theatre_charges WHERE org_id=? )";

  /**
   * Add rate plan to all theatres.
   * @param con Database connection
   * @param newOrgId New rate plan ID
   * @param varianceType Variance type
   * @param varianceBy Variance by
   * @param baseOrgId Base rate plan
   * @param nearstRoundOfValue nearest round off value
   * @param updateDiscounts Update discounts ?
   * @return true or false representing success of operation.
   * @throws Exception Generic exception
   */
  public static boolean addOrgForTheatres(Connection con, String newOrgId, String varianceType,
      Double varianceBy, String baseOrgId, Double nearstRoundOfValue, boolean updateDiscounts)
      throws Exception {
    boolean status = false;

    PreparedStatement ps = null;
    if (!varianceType.equals("Incr")) {
      varianceBy = new Double(-varianceBy);
    }
    status = insertChargesByPercent(con, newOrgId, baseOrgId, varianceBy, nearstRoundOfValue,
        updateDiscounts) > 0;
    if (null != ps) {
      ps.close();
    }

    return status;
  }

  /**
   * Add rate plan for all theatre
   * @param con Database connection
   * @param newOrgId New rate plan ID
   * @param varianceType Variance type
   * @param varianceValue Variance value
   * @param varianceBy Variance by
   * @param useValue Use value
   * @param baseOrgId Base rate plan
   * @return true or false representing success of operation.
   * @throws Exception Generic exception
   */
  public static boolean addOrgForTheatres(Connection con, String newOrgId, String varianceType,
      Double varianceValue, Double varianceBy, boolean useValue, String baseOrgId)
      throws Exception {

    /*
     * 1)Getting all Theatres present in the theatre_charges table 2)Getting all
     * bedType corresponding to this Theatres 3)Getting
     * all(daily_charge,min_charge,....) Charges corresponding to this
     * Theatre,bedtype,base Organization given by user.
     *
     */
    PreparedStatement ps = con.prepareStatement(INSERT_INTO_THEATRES);

    PreparedStatement ps1 = con.prepareStatement(SELECT_THEATRES);
    ArrayList<String> docList = DataBaseUtil.queryToOnlyArrayList(ps1);
    Iterator<String> iterator = docList.iterator();
    ps1.close();

    ps1 = con.prepareStatement(GET_THEATRE_CHARGES_QUERY);
    ArrayList<Hashtable<String, String>> chargeList = null;

    PreparedStatement ps2 = con.prepareStatement(GET_DISTINCT_BEDTYPES_FOR_THEATRE);

    while (iterator.hasNext()) {
      String theatreId = iterator.next();
      ps2.setString(1, theatreId);

      ArrayList<String> bedTypes = DataBaseUtil.queryToOnlyArrayList(ps2);
      Iterator<String> beds = bedTypes.iterator();
      while (beds.hasNext()) {
        String bedType = beds.next();
        ps1.setString(1, theatreId);
        ps1.setString(2, baseOrgId);
        ps1.setString(3, bedType);
        chargeList = DataBaseUtil.queryToArrayList(ps1);
        Iterator<Hashtable<String, String>> charIt = chargeList.iterator();
        while (charIt.hasNext()) {
          Hashtable<String, String> ht = charIt.next();
          Double dailyCharge = new Double(ht.get("DAILY_CHARGE")).doubleValue();
          Double minCharge = new Double(ht.get("MIN_CHARGE")).doubleValue();
          Double incrCharge = new Double(ht.get("INCR_CHARGE")).doubleValue();
          Double tax = new Double(ht.get("TAX")).doubleValue();
          Double slab1Chg = new Double(ht.get("SLAB_1_CHARGE"));

          if (varianceType.equals("Incr")) {
            if (useValue) {
              dailyCharge += varianceValue;
              minCharge += varianceValue;
              incrCharge += varianceValue;
              tax += varianceValue;
              slab1Chg += varianceValue;

            } else {
              dailyCharge += dailyCharge * (varianceBy.doubleValue() / 100);
              minCharge += minCharge * (varianceBy.doubleValue() / 100);
              incrCharge += incrCharge * (varianceBy.doubleValue() / 100);
              tax += tax * (varianceBy.doubleValue() / 100);
              slab1Chg += slab1Chg * (varianceBy.doubleValue() / 100);
            }
          } else {
            if (useValue) {
              dailyCharge -= varianceValue;
              minCharge -= varianceValue;
              incrCharge -= varianceValue;
              tax -= varianceValue;
              slab1Chg -= varianceValue;

            } else {
              dailyCharge -= dailyCharge * (varianceBy.doubleValue() / 100);
              minCharge -= minCharge * (varianceBy.doubleValue() / 100);
              incrCharge -= incrCharge * (varianceBy.doubleValue() / 100);
              tax += tax * (varianceBy.doubleValue() / 100);
              slab1Chg -= slab1Chg * (varianceBy.doubleValue() / 100);
            }
          }

          ps.setString(1, theatreId);
          ps.setString(2, newOrgId);
          ps.setString(3, bedType);
          ps.setDouble(4, dailyCharge);
          ps.setDouble(5, minCharge);
          ps.setDouble(6, incrCharge);
          ps.setDouble(7, tax);
          ps.setDouble(8, slab1Chg);

          ps.addBatch();

        } // charge while loop

      } // beds while loop

    } // operations loop

    int[] batchUpdateCount = ps.executeBatch();
    boolean status = DataBaseUtil.checkBatchUpdates(batchUpdateCount);
    ps.close();
    ps2.close();
    return status;
  }

  private static int insertChargesByPercent(Connection con, String newOrgId, String baseOrgId,
      Double varianceBy, Double nearstRoundOfValue, boolean updateDiscounts) throws Exception {

    int ndx = 1;
    int numCharges = 4;

    PreparedStatement pstmt = null;
    try {
      pstmt = con.prepareStatement(
          updateDiscounts ? INSERT_INTO_THEA_WITH_DISCOUNTS_BY : INSERT_INTO_THEATRES_BY);
      pstmt.setString(ndx++, newOrgId);

      for (int i = 0; i < numCharges; i++) {
        pstmt.setBigDecimal(ndx++, new BigDecimal(varianceBy));
        pstmt.setBigDecimal(ndx++, new BigDecimal(nearstRoundOfValue));
      }

      // go one more round setting the parameters
      if (updateDiscounts) { 
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


  private static final String UPDATE_THEATRE_PLUS = "UPDATE theatre_charges totab SET "
      + " daily_charge = round(fromtab.daily_charge + ?),"
      + " min_charge = round(fromtab.min_charge + ?),"
      + " incr_charge = round(fromtab.incr_charge + ?),"
      + " slab_1_charge = round(fromtab.slab_1_charge + ?),"
      + " tax = round(fromtab.tax + ?)" + " FROM theatre_charges fromtab"
      + " WHERE totab.org_id = ? AND fromtab.org_id = ?"
      + " AND totab.theatre_id = fromtab.theatre_id AND totab.bed_type = fromtab.bed_type"
      + " AND totab.is_override='N'";

  private static final String UPDATE_THEATRE_MINUS = "UPDATE theatre_charges totab SET "
      + " daily_charge = GREATEST(round(fromtab.daily_charge - ?), 0),"
      + " min_charge = GREATEST(round(fromtab.min_charge - ?), 0),"
      + " incr_charge = GREATEST(round(fromtab.incr_charge - ?), 0),"
      + " slab_1_charge = GREATEST(round(fromtab.slab_1_charge - ?), 0),"
      + " tax = GREATEST(round(fromtab.tax -?), 0)" + " FROM theatre_charges fromtab"
      + " WHERE totab.org_id = ? AND fromtab.org_id = ?"
      + " AND totab.theatre_id = fromtab.theatre_id AND totab.bed_type = fromtab.bed_type"
      + " AND totab.is_override='N'";

  private static final String UPDATE_THEATRE_BY = "UPDATE theatre_charges totab SET "
      + " daily_charge = doroundvarying(fromtab.daily_charge,?,?),"
      + " min_charge = doroundvarying(fromtab.min_charge,?,?),"
      + " incr_charge = doroundvarying(fromtab.incr_charge,?,?),"
      + " slab_1_charge = doroundvarying(fromtab.slab_1_charge,?,?),"
      + " tax = doroundvarying(fromtab.tax,?,?)" + " FROM theatre_charges fromtab"
      + " WHERE totab.org_id = ? AND fromtab.org_id = ?"
      + " AND totab.theatre_id = fromtab.theatre_id AND totab.bed_type = fromtab.bed_type"
      + " AND totab.is_override='N'";

  /**
   * Update rate plan for theatres.
   * @param con Database connection
   * @param orgId Rate Plan ID
   * @param varianceType Variance type
   * @param varianceValue Variance value
   * @param varianceBy Variance by
   * @param useValue Use value
   * @param baseOrgId Base rate plan
   * @param nearstRoundOfValue nearest round off value
   * @return true or false representing success of operation.
   * @throws SQLException  Query execution exception
   * @throws IOException I/O operation exception
   */
  public static boolean updateOrgForTheatres(Connection con, String orgId, String varianceType,
      Double varianceValue, Double varianceBy, boolean useValue, String baseOrgId,
      Double nearstRoundOfValue) throws SQLException, IOException {

    boolean status = false;
    PreparedStatement pstmt = null;

    if (useValue) {

      if (varianceType.equals("Incr")) {
        pstmt = con.prepareStatement(UPDATE_THEATRE_PLUS);
      } else {
        pstmt = con.prepareStatement(UPDATE_THEATRE_MINUS);
      }

      pstmt.setBigDecimal(1, new BigDecimal(varianceValue));
      pstmt.setBigDecimal(2, new BigDecimal(varianceValue));
      pstmt.setBigDecimal(3, new BigDecimal(varianceValue));
      pstmt.setBigDecimal(4, new BigDecimal(varianceValue));
      pstmt.setBigDecimal(5, new BigDecimal(varianceValue));
      pstmt.setString(6, orgId);
      pstmt.setString(7, baseOrgId);

      status = pstmt.executeUpdate() > 0;

    } else {

      pstmt = con.prepareStatement(UPDATE_THEATRE_BY);
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
      pstmt.setString(11, orgId);
      pstmt.setString(12, baseOrgId);

      status = pstmt.executeUpdate() > 0;
    }

    pstmt.close();

    return status;
  }

  private static final String OT_FIELDS = "SELECT *";
  private static final String OT_COUNT = "SELECT count(*)";
  private static final String OT_FROM_TABLES = " FROM (SELECT ot.theatre_name, "
      + " ot.theatre_id, ot.status, ot.center_id,hcm.center_name,tod.org_id,tod.is_override,"
      + " od.org_name "
      + " FROM theatre_master ot "
      + " JOIN theatre_org_details tod on (tod.theatre_id = ot.theatre_id) "
      + " JOIN organization_details od on (od.org_id = tod.org_id) "
      + " JOIN hospital_center_master hcm on (hcm.center_id=ot.center_id) " + " ) AS foo";

  /**
   * Get paginated list of theatres matching search criteria.
   * @param requestParams parameters sent along with request
   * @param pagingParams pagination parameters
   * @return paginated list of theatres 
   * @throws ParseException Parser exception
   * @throws SQLException  Query execution exception
   */
  public PagedList search(Map requestParams, Map pagingParams) throws ParseException, SQLException {

    Connection con = null;
    SearchQueryBuilder qb = null;
    try {
      con = DataBaseUtil.getConnection();
      qb = new SearchQueryBuilder(con, OT_FIELDS, OT_COUNT, OT_FROM_TABLES, pagingParams);
      qb.addFilterFromParamMap(requestParams);
      qb.addSecondarySort("theatre_id");
      qb.build();

      return qb.getMappedPagedList();
    } finally {
      qb.close();
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /**
   * Gte all charges for given rate plan and theatre.
   * @param orgId Rate Plan ID
   * @param id Theatre ID
   * @return List of charges
   * @throws SQLException  Query execution exception
   */
  public List<BasicDynaBean> getAllChargesForOrgOT(String orgId, String id) throws SQLException {
    List<String> ids = new ArrayList();
    ids.add(id);
    return getAllChargesForOrg(orgId, ids);
  }

  private static final String GET_ALL_CHARGES_FOR_ORG = " SELECT theatre_id, bed_type,  min_charge,"
      + " incr_charge ,slab_1_charge,"
      + " min_charge_discount, incr_charge_discount, slab_1_charge_discount, "
      + " daily_charge, daily_charge_discount " + " FROM theatre_charges " + " WHERE org_id=?";

  /**
   * Get all charges for given list of theatres for given rate plan.
   * @param orgId Rate Plan ID
   * @param ids Theatre IDs
   * @return List of charges
   * @throws SQLException  Query execution exception
   */
  public List<BasicDynaBean> getAllChargesForOrg(String orgId, List<String> ids)
      throws SQLException {

    Connection con = null;
    PreparedStatement ps = null;

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      StringBuilder query = new StringBuilder(GET_ALL_CHARGES_FOR_ORG);
      SearchQueryBuilder.addWhereFieldOpValue(true, query, "theatre_id", "IN", ids);

      ps = con.prepareStatement(query.toString());

      int index = 1;
      ps.setString(index++, orgId);
      if (ids != null) {
        for (String id : ids) {
          ps.setString(index++, id);
        }
      }
      return DataBaseUtil.queryToDynaList(ps);

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  private static final String COPY_THEATRE_DETAILS_TO_ALL_ORGS = " INSERT INTO "
      + " theatre_org_details (theatre_id, org_id, applicable,username,mod_time) "
      + " SELECT tod.theatre_id, o.org_id, tod.applicable,tod.username,tod.mod_time "
      + " FROM organization_details o "
      + " JOIN theatre_org_details tod ON (tod.theatre_id=? AND tod.org_id='ORG0001') "
      + " WHERE o.org_id != 'ORG0001'";

  /**
   * Copy theatre details to all rate plans.
   * @param con Database connection
   * @param theatreId Theatre ID
   * @throws SQLException  Query execution exception
   */
  public void copyTheatreDetailsToAllOrgs(Connection con, String theatreId) throws SQLException {
    PreparedStatement ps = con.prepareStatement(COPY_THEATRE_DETAILS_TO_ALL_ORGS);
    ps.setString(1, theatreId);
    ps.executeUpdate();
    ps.close();
  }

  private static final String INSERT_ORG_DETAILS_FOR_THEATRES = "INSERT INTO theatre_org_details "
      + " SELECT theatre_id, ?, applicable, ?, ?, ?, 'N'"
      + " FROM theatre_org_details WHERE org_id=?;";

  /**
   * Add rate plan codes for theatre.
   * @param con Database connection
   * @param newOrgId New rate plan ID
   * @param varianceType Variance type
   * @param varianceValue Variance value
   * @param varianceBy Variance by
   * @param useValue Use value
   * @param baseOrgId Base rate plan
   * @param nearstRoundOfValue nearest round off value
   * @param userName User Name
   * @return true or false representing success of operation.
   * @throws Exception Generic exception
   */
  public static boolean addOrgCodesForTheatre(Connection con, String newOrgId, String varianceType,
      Double varianceValue, Double varianceBy, boolean useValue, String baseOrgId,
      Double nearstRoundOfValue, String userName) throws Exception {
    boolean status = false;
    PreparedStatement ps = null;
    BasicDynaBean obean = new OrgMasterDao().findByKey(con, "org_id", newOrgId);
    String rateSheetId = ("N".equals((String) obean.get("is_rate_sheet")) ? baseOrgId : null);
    try {
      ps = con.prepareStatement(INSERT_ORG_DETAILS_FOR_THEATRES);
      ps.setString(1, newOrgId);
      ps.setString(2, userName);
      ps.setTimestamp(3, DateUtil.getCurrentTimestamp());
      ps.setString(4, rateSheetId);
      ps.setString(5, baseOrgId);

      status = ps.executeUpdate() > 0;
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
    return status;
  }

  /**
   * Add rate plan codes for items.
   * @param con Database connection
   * @param newOrgId New rate plan ID
   * @param baseOrgId Base rate plan
   * @param userName User Name
   * @return true or false representing success of operation.
   * @throws Exception Generic exception
   */
  public static boolean addOrgCodesForItems(Connection con, String newOrgId, String baseOrgId,
      String userName) throws Exception {
    return addOrgCodesForTheatre(con, newOrgId, null, null, null, false, baseOrgId, null, userName);
  }

  private static String INIT_ORG_DETAILS = "INSERT INTO theatre_org_details"
      + " (theatre_id, org_id, applicable, base_rate_sheet_id, is_override) "
      + "   SELECT theatre_id, ?, false, null, 'N'" + "   FROM theatre_master";

  private static String INIT_CHARGES = "INSERT INTO theatre_charges(theatre_id,org_id,bed_type,"
      + "daily_charge,min_charge,incr_charge,slab_1_charge)"
      + "(SELECT theatre_id, ?, abov.bed_type, 0.0, 0.0, 0.0, 0.0"
      + "FROM theatre_master tm CROSS JOIN all_beds_orgs_view abov WHERE abov.org_id =? ) ";

  private static final String INSERT_CHARGES = "INSERT INTO theatre_charges("
      + " theatre_id,org_id,bed_type,"
      + " daily_charge,min_charge,incr_charge,slab_1_charge, tax, is_override)"
      + " SELECT tc.theatre_id, ?, tc.bed_type, doroundvarying(tc.daily_charge, ?, ?), "
      + " doroundvarying(tc.min_charge, ?, ?), doroundvarying(tc.incr_charge, ?, ?), "
      + " doroundvarying(tc.slab_1_charge, ?, ?), tc.tax, 'N' "
      + " FROM theatre_charges tc, theatre_org_details tod, theatre_org_details todtarget "
      + " where tc.org_id = tod.org_id and tc.theatre_id = tod.theatre_id "
      + " and todtarget.org_id = ? and todtarget.theatre_id = tod.theatre_id"
      + " and todtarget.base_rate_sheet_id = ? "
      + " and tod.applicable = true " + " and tc.org_id = ? ";

  private static final String UPDATE_CHARGES = "UPDATE theatre_charges AS target SET "
      + " daily_charge = doroundvarying(tc.daily_charge, ?, ?), "
      + " min_charge = doroundvarying(tc.min_charge, ?, ?), "
      + " incr_charge = doroundvarying(tc.incr_charge, ?, ?), "
      + " slab_1_charge = doroundvarying(tc.slab_1_charge, ?, ?), "
      + " daily_charge_discount = doroundvarying(tc.daily_charge_discount, ?, ?), "
      + " min_charge_discount = doroundvarying(tc.min_charge_discount, ?, ?), "
      + " incr_charge_discount = doroundvarying(tc.incr_charge_discount, ?, ?), "
      + " slab_1_charge_discount = doroundvarying(tc.slab_1_charge_discount, ?, ?), "
      + " tax = tc.tax, " + " is_override = 'N' "
      + " FROM theatre_charges tc, theatre_org_details tod "
      + " where tod.org_id = ? and tc.theatre_id = tod.theatre_id and tod.base_rate_sheet_id = ? "
      + " and target.theatre_id = tc.theatre_id and target.bed_type = tc.bed_type and "
      + " tod.applicable = true and target.is_override != 'Y'"
      + " and tc.org_id = ? and target.org_id = ?";

  private static final String UPDATE_EXCLUSIONS = "UPDATE theatre_org_details AS target "
      + " SET applicable = true, base_rate_sheet_id = tod.org_id, is_override = 'N' "
      + " FROM theatre_org_details tod WHERE tod.theatre_id = target.theatre_id and "
      + " tod.org_id = ? and tod.applicable = true and target.org_id = ?"
      + " and target.applicable = false and target.is_override != 'Y'";

  /**
   * Update rate plan.
   * @param con Database connection
   * @param newOrgId New rate plan ID
   * @param baseOrgId Base rate plan
   * @param varianceType Variance type
   * @param variance Variance
   * @param rndOff Round Off
   * @param userName User Name
   * @param orgName Rate plan name
   * @return true or false representing success of operation.
   * @throws Exception Generic exception
   */
  public boolean updateRatePlan(Connection con, String newOrgId, String baseOrgId,
      String varianceType, Double variance, Double rndOff, String userName, String orgName)
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
        varianceBy, roundOff, newOrgId, baseOrgId, baseOrgId, newOrgId };
    Object[] insparams = { newOrgId, varianceBy, roundOff, varianceBy, roundOff, varianceBy,
        roundOff, varianceBy, roundOff, newOrgId, baseOrgId, baseOrgId };
    status = updateExclusions(con, UPDATE_EXCLUSIONS, newOrgId, baseOrgId);
    if (status) {
      status = updateCharges(con, UPDATE_CHARGES, updparams);
    }
    // postAuditEntry(con, "operation_charges_audit_log", userName, orgName);
    return status;
  }

  /**
   * Initalize rate plan.
   * @param con Database connection
   * @param newOrgId New rate plan ID
   * @param varianceType Variance type
   * @param varianceBy Variance by
   * @param baseOrgId Base rate plan
   * @param roundOff Roundoff for charges
   * @param userName User Name
   * @param orgName Rate plan name
   * @return true or false representing success of operation.
   * @throws Exception Generic exception
   */
  public boolean initRatePlan(Connection con, String newOrgId, String varianceType,
      Double varianceBy, String baseOrgId, Double roundOff, String userName, String orgName)
      throws Exception {
    boolean status = addOrgCodesForItems(con, newOrgId, baseOrgId, userName);
    if (status) {
      status = addOrgForTheatres(con, newOrgId, varianceType, varianceBy, baseOrgId, roundOff,
          true);
    }
    return status;
  }

  private static final String REINIT_EXCLUSIONS = "UPDATE theatre_org_details as target "
      + " SET applicable = tod.applicable, base_rate_sheet_id = tod.org_id, "
      + " is_override = 'N' "
      + " FROM theatre_org_details tod WHERE tod.theatre_id = target.theatre_id and "
      + " tod.org_id = ? and target.org_id = ? and target.is_override != 'Y'";

  /**
   * Reinitialize rate plan.
   * @param con Database connection
   * @param newOrgId New rate plan ID
   * @param varianceType Variance type
   * @param variance variance
   * @param baseOrgId Base rate plan
   * @param rndOff Round off
   * @param userName User Name
   * @param orgName Rate plan name
   * @return true or false representing success of operation.
   * @throws Exception Generic exception
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
        varianceBy, roundOff, varianceBy, roundOff, newOrgId, baseOrgId };
    status = updateExclusions(con, REINIT_EXCLUSIONS, newOrgId, baseOrgId);
    if (status) {
      status = updateCharges(con, UPDATE_RATEPLAN_THEATRE_CHARGES, updparams);
    }
    return status;
  }

  private static String INIT_ITEM_ORG_DETAILS = "INSERT INTO theatre_org_details "
      + "(theatre_id, org_id, applicable, base_rate_sheet_id, is_override)"
      + " ( SELECT ?, od.org_id, true, prspv.base_rate_sheet_id, 'N' FROM organization_details od"
      + " LEFT OUTER JOIN priority_rate_sheet_parameters_view prspv ON od.org_id = prspv.org_id )";

  private static String INIT_ITEM_CHARGES = "INSERT INTO theatre_charges("
      + " theatre_id ,org_id,bed_type, daily_charge, min_charge, slab_1_charge, incr_charge)"
      + "(SELECT ?, abov.org_id, abov.bed_type, 0.0, 0.0, 0.0, 0.0 FROM all_beds_orgs_view abov) ";

  /**
   * 
   * @param con Database connection
   * @param theatreId Theatre ID
   * @param userName User Name
   * @return true or false representing success of operation.
   * @throws Exception Generic exception
   */
  public boolean initItemCharges(Connection con, String theatreId, String userName)
      throws Exception {

    boolean status = false;
    status = initItemCharges(con, INIT_ITEM_ORG_DETAILS, INIT_ITEM_CHARGES, theatreId, null);

    return status;
  }

  private static final String GET_DERIVED_RATE_PALN_DETAILS = "select rp.org_id,od.org_name, "
      + " case when rate_variation_percent < 0 then 'Decrease By' else 'Increase By' end"
      + " as discormarkup, "
      + " rate_variation_percent,round_off_amount,tod.applicable,tod.theatre_id,"
      + " rp.base_rate_sheet_id, tod.is_override "
      + " from rate_plan_parameters rp join organization_details od on(od.org_id=rp.org_id) "
      + " join theatre_org_details tod on (tod.org_id = rp.org_id) "
      + " where rp.base_rate_sheet_id =?  and theatre_id=?  and tod.base_rate_sheet_id = ? ";

  /**
   * Get derived rate plan details.
   * @param baseRateSheetId base rate sheet id
   * @param theatreId Theatre ID
   * @return List of derived rate plans
   * @throws SQLException  Query execution exception
   */
  public List<BasicDynaBean> getDerivedRatePlanDetails(String baseRateSheetId, String theatreId)
      throws SQLException {
    return getDerivedRatePlanDetails(baseRateSheetId, "operationTheatre", theatreId,
        GET_DERIVED_RATE_PALN_DETAILS);
  }

  /**
   * Update charges for derived rate plans.
   * @param con Database connection
   * @param baseRateSheetId Base rate sheet ID
   * @param ratePlanIds Rate plan IDs
   * @param bedType Bed type
   * @param dailyChg Charge for daily occupancy
   * @param minChg Minimum charge
   * @param incrChg Incremental charges
   * @param slabChg Slab charges
   * @param theatreId Theatre ID
   * @param dailyDisc Daily discount
   * @param minDisc Minimum discount
   * @param incrDisc Incremental discount
   * @param slabDisc Slab discount
   * @return true or false representing success of operation. 
   * @throws Exception Generic exception
   */
  public boolean updateChargesForDerivedRatePlans(Connection con, String baseRateSheetId,
      String[] ratePlanIds, String[] bedType, Double[] dailyChg, Double[] minChg, Double[] incrChg,
      Double[] slabChg, String theatreId, Double[] dailyDisc, Double[] minDisc, Double[] incrDisc,
      Double[] slabDisc) throws Exception {
    boolean success = false;

    TheatreMasterDAO thdao = new TheatreMasterDAO();
    for (int i = 0; i < ratePlanIds.length; i++) {
      Map<String, Object> keys = new HashMap<String, Object>();
      keys.put("base_rate_sheet_id", baseRateSheetId);
      keys.put("org_id", ratePlanIds[i]);
      BasicDynaBean bean = new GenericDAO("rate_plan_parameters").findByKey(keys);
      int variation = (Integer) bean.get("rate_variation_percent");
      int roundoff = (Integer) bean.get("round_off_amount");

      List<BasicDynaBean> chargeList = new ArrayList();
      boolean overrided = isChargeOverrided(con, ratePlanIds[i], "theatre_id", theatreId,
          "operationTheatre", "theatre_org_details");
      if (!overrided) {
        for (int k = 0; k < bedType.length; k++) {
          BasicDynaBean charge = thdao.getBean();

          charge.set("theatre_id", theatreId);
          charge.set("org_id", ratePlanIds[i]);
          charge.set("bed_type", bedType[k]);

          Double dailyCharge = calculateCharge(dailyChg[k], new Double(variation), roundoff);
          Double minimumCharge = calculateCharge(minChg[k], new Double(variation), roundoff);
          Double incrementalCharge = calculateCharge(incrChg[k], new Double(variation), roundoff);
          Double slabCharge = calculateCharge(slabChg[k], new Double(variation), roundoff);

          Double dailyDiscount = calculateCharge(dailyDisc[k], new Double(variation), roundoff);
          Double minDiscount = calculateCharge(minDisc[k], new Double(variation), roundoff);
          Double incrDiscount = calculateCharge(incrDisc[k], new Double(variation), roundoff);
          Double slabDiscount = calculateCharge(slabDisc[k], new Double(variation), roundoff);

          charge.set("daily_charge", new BigDecimal(dailyCharge));
          charge.set("min_charge", new BigDecimal(minimumCharge));
          charge.set("incr_charge", new BigDecimal(incrementalCharge));
          charge.set("slab_1_charge", new BigDecimal(slabCharge));

          charge.set("daily_charge_discount", new BigDecimal(dailyDiscount));
          charge.set("min_charge_discount", new BigDecimal(minDiscount));
          charge.set("incr_charge_discount", new BigDecimal(incrDiscount));
          charge.set("slab_1_charge_discount", new BigDecimal(slabDiscount));
          chargeList.add(charge);
        }
      }
      for (BasicDynaBean charge : chargeList) {
        thdao.updateWithNames(con, charge.getMap(), 
            new String[] { "theatre_id", "org_id", "bed_type" });
      }
      success = true;
    }
    return success;
  }

  private static final String UPDATE_RATEPLAN_THEATRE_CHARGES = "UPDATE theatre_charges totab SET "
      + " daily_charge = doroundvarying(fromtab.daily_charge,?,?),"
      + " min_charge = doroundvarying(fromtab.min_charge,?,?),"
      + " incr_charge = doroundvarying(fromtab.incr_charge,?,?),"
      + " slab_1_charge = doroundvarying(fromtab.slab_1_charge,?,?),"
      + " tax = doroundvarying(fromtab.tax,?,?), "
      + " daily_charge_discount = doroundvarying(fromtab.daily_charge_discount,?,?), "
      + " min_charge_discount = doroundvarying(fromtab.min_charge_discount,?,?), "
      + " incr_charge_discount = doroundvarying(fromtab.incr_charge_discount,?,?), "
      + " slab_1_charge_discount = doroundvarying(fromtab.slab_1_charge_discount,?,?) "
      + " FROM theatre_charges fromtab" + " WHERE totab.org_id = ? AND fromtab.org_id = ?"
      + " AND totab.theatre_id = fromtab.theatre_id"
      + " AND totab.bed_type = fromtab.bed_type AND totab.is_override='N'";

  /**
   * Update theatre charges for derived rate plans.
   * @param orgId Rate Plan ID
   * @param varianceType Variance type
   * @param varianceValue Variance value
   * @param varianceBy Variance by
   * @param baseOrgId Base rate plan
   * @param nearstRoundOfValue nearest round off value
   * @return true or false representing success of operation.
   * @throws SQLException  Query execution exception
   * @throws Exception Generic exception
   */
  public boolean updateTheatreChargesForDerivedRatePlans(String orgId, String varianceType,
      Double varianceValue, Double varianceBy, String baseOrgId, Double nearstRoundOfValue)
      throws SQLException, Exception {

    boolean success = false;
    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);

      BigDecimal variance = new BigDecimal(varianceBy);
      BigDecimal roundoff = new BigDecimal(nearstRoundOfValue);
      Object[] updparams = { variance, roundoff, variance, roundoff, variance, roundoff, variance,
          roundoff, variance, roundoff, variance, roundoff, variance, roundoff, variance, roundoff,
          orgId, baseOrgId, baseOrgId, orgId };
      success = updateCharges(con, UPDATE_CHARGES, updparams);
    } finally {
      DataBaseUtil.commitClose(con, success);
    }

    return success;
  }

  /**
   * Update bed for given rate plan.
   * @param con Database connection
   * @param ratePlanId Rate plan ID
   * @param variance variance
   * @param rateSheetId Rate sheet ID
   * @param rndOff round off
   * @param bedType bed type
   * @return true or false representing success of operation
   * @throws Exception Generic exception
   */
  public boolean updateBedForRatePlan(Connection con, String ratePlanId, Double variance,
      String rateSheetId, Double rndOff, String bedType) throws Exception {

    boolean status = false;

    BigDecimal varianceBy = new BigDecimal(variance);
    BigDecimal roundOff = new BigDecimal(rndOff);

    Object[] updparams = { varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff,
        varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff,
        varianceBy, roundOff, varianceBy, roundOff, ratePlanId, rateSheetId, bedType };
    status = updateCharges(con, UPDATE_RATEPLAN_THEATRE_CHARGES + " AND totab.bed_type=? ",
        updparams);
    return status;
  }

  // changes for surgery week view
  private static final String GET_SCHEDULER_WEEKVIEW_THEATRE = " SELECT theatre_id as resource_id,"
      + " theatre_name as resource_name,status,min_duration "
      + " FROM theatre_master t  WHERE " + "theatre_id = ?"
      + " and t.status = 'A' and t.schedule=true";

  /**
   * Get default theatre bean.
   * @param theatreId Theatre ID
   * @return Default theatre bean
   * @throws SQLException  Query execution exception
   */
  public static BasicDynaBean getDefaultTheatreBean(String theatreId) throws SQLException {

    return DataBaseUtil.queryToDynaBean(GET_SCHEDULER_WEEKVIEW_THEATRE, theatreId);

  }

  private static final String GET_THEATRE_NAMES = " SELECT DISTINCT t.theatre_id, t.theatre_name "
      + "FROM theatre_master t " + "LEFT JOIN user_theatres ut ON(t.theatre_id =ut.theatre_id) "
      + "WHERE t.status = 'A' and t.schedule=true  & @ order by theatre_name";

  /**
   * Get the list of schedulable theatres.
   * @return List of schedulable theatres
   * @throws SQLException  Query execution exception
   */
  public List<BasicDynaBean> getSchedulableTheatreName(String userName, int roleId)
      throws SQLException {
    Integer centerId = RequestContext.getCenterId();
    int maxCenterIncDefault = (Integer) GenericPreferencesDAO.getAllPrefs()
        .get("max_centers_inc_default");
    String getTheatreName = GET_THEATRE_NAMES;
    List<Object> queryParams = new ArrayList<>();
    if ((roleId != 1) && (roleId != 2)) {
      queryParams.add(userName);
      getTheatreName = getTheatreName.replace("&", "and ut.emp_username = ?");
    } else {
      getTheatreName = getTheatreName.replace("&", "");
    }
    if (maxCenterIncDefault > 1 && centerId != 0) {
      queryParams.add(centerId);
      getTheatreName = getTheatreName.replaceAll("@", " AND center_id IN (0,?)");
      return DatabaseHelper.queryToDynaList(getTheatreName, queryParams.toArray());
    } else {
      getTheatreName = getTheatreName.replaceAll("@", " ");
      return DatabaseHelper.queryToDynaList(getTheatreName, queryParams.toArray());
    }
  }

  private static final String GET_THEATRE_ITEM_SUB_GROUP_TAX_DETAILS = "SELECT "
      + " isg.item_subgroup_id, isg.subgroup_code, isg.item_subgroup_name, ig.group_code "
      + " FROM theatre_item_sub_groups tisg "
      + " JOIN item_sub_groups isg ON(tisg.item_subgroup_id = isg.item_subgroup_id) "
      + " JOIN item_groups ig ON (ig.item_group_id = isg.item_group_id) "
      + " WHERE tisg.theatre_id = ? ";

  /**
   * Get tax sub group details for given theatre id.
   * @param itemId Theatre ID
   * @return List of tax sub groups
   * @throws SQLException  Query execution exception
   */
  public List<BasicDynaBean> getTheatreItemSubGroupTaxDetails(String itemId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_THEATRE_ITEM_SUB_GROUP_TAX_DETAILS);
      ps.setString(1, itemId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  private static final String GET_OT_ITEM_SUBGROUP_DETAILS = "select tisg.item_subgroup_id, "
      + " isg.item_subgroup_name,ig.item_group_id,"
      + " item_group_name,igt.item_group_type_id,igt.item_group_type_name "
      + " from theatre_item_sub_groups tisg "
      + " left join item_sub_groups isg on (isg.item_subgroup_id = tisg.item_subgroup_id) "
      + " left join theatre_master tm on (tm.theatre_id = tisg.theatre_id) "
      + " left join item_groups ig on (ig.item_group_id = isg.item_group_id)"
      + " left join item_group_type igt on (igt.item_group_type_id = ig.item_group_type_id)"
      + " where tisg.theatre_id = ? ";

  /**
   * Get theatre item sub group details.
   * @param theatreId Theatre ID
   * @return List of subgroups mapped to theatre 
   * @throws SQLException  Query execution exception
   */
  public static List<BasicDynaBean> getOtItemSubGroupDetails(String theatreId)
      throws SQLException {
    List list = null;
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_OT_ITEM_SUBGROUP_DETAILS);
      ps.setString(1, theatreId);
      list = DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return list;
  }

  /**
   * Get active insurance categories for given theatre.
   * @param theatreId Theatre ID
   * @return List of Basic Dyna Bean
   * @throws SQLException  Query execution exception
   */
  public List<BasicDynaBean> getActiveInsuranceCategories(String theatreId) throws SQLException {
    List list = null;
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(SELECT_INSURANCE_CATEGORY_IDS);
      ps.setString(1, theatreId);
      list = DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }

    return list;
  }

  private static final String SELECT_INSURANCE_CATEGORY_IDS = "SELECT insurance_category_id "
      + "FROM theatre_insurance_category_mapping WHERE theatre_id =?";
  
  private static final String THEATERS_BY_CENTER = "SELECT theatre_id,theatre_name FROM "
      + " theatre_master thm " + " where thm.status='A' and thm.schedule='t' ##centerId## ";
  
  /**
   * get theatres for a center.
   * 
   * @param centerId center ID
   * @return list of active & schedulable theatres in a center
   */
  public List<BasicDynaBean> getTheatersByCenter(int centerId) {
    String query = THEATERS_BY_CENTER;
    if (centerId != 0) {
      query = query.replace("##centerId##", " AND thm.center_id = ?");
      return DatabaseHelper.queryToDynaList(query, new Object[] { centerId });
    } else {
      query = query.replace("##centerId##", "");
      return DatabaseHelper.queryToDynaList(query);
    }
  }

  private static final String SELECT_MAPPED_THEATERS = " SELECT thm.theatre_id, thm.theatre_name "
      + " FROM theatre_master thm"
      + " JOIN operation_theatre_mapping otm ON (otm.theatre_id = thm.theatre_id)"
      + " WHERE operation_id = ? AND thm.schedule='t' ##centerId##";

  /**
   * get theatres for selected operation.
   * 
   * @param operationId operation ID
   * @param centerId center ID
   * @return mapped theatres for operation
   */
  public List<BasicDynaBean> getMappedTheater(String operationId, int centerId) {

    String queryForCenter = SELECT_MAPPED_THEATERS;
    if (centerId != 0) {
      queryForCenter = queryForCenter.replace("##centerId##", " AND thm.center_id = ?");
      return DatabaseHelper.queryToDynaList(queryForCenter, operationId, centerId);
    } else {
      queryForCenter = queryForCenter.replace("##centerId##", "");
      return DatabaseHelper.queryToDynaList(queryForCenter, new Object[] { operationId });
    }
  }

  public static final String GET_ALL_USER_THEATRE_IDS = "SELECT theatre_id FROM user_theatres "
      + "WHERE emp_username = ?";

  public static List<BasicDynaBean> getUserTheatres(String user) throws SQLException {
    List<BasicDynaBean> theatres = DataBaseUtil.queryToDynaList(GET_ALL_USER_THEATRE_IDS, user);
    return theatres;
  }

  public static final String GET_ALL_USER_THEATRES = "SELECT Distinct tm.theatre_id,"
      + "tm.theatre_name FROM theatre_master tm &";

  /**
   * Get theatres mapped to user.
   * 
   * @param user userName
   * @param roleId roleId
   * @return List of Basic Dyna Bean
   * @throws SQLException Query execution exception
   */
  public static List<BasicDynaBean> getUserTheatresList(String user, int roleId)
      throws SQLException {
    String getAllUserTheatresList = GET_ALL_USER_THEATRES;
    List<BasicDynaBean> theatres;
    String joinQuery = "join user_theatres ut ON"
        + "( tm.theatre_id = ut.theatre_id and emp_username = ?)";
    if ((roleId != 1) && (roleId != 2)) {
      getAllUserTheatresList = getAllUserTheatresList.replace("&", joinQuery);
      theatres = DataBaseUtil.queryToDynaList(getAllUserTheatresList, user);
      return theatres;
    } else {
      getAllUserTheatresList = getAllUserTheatresList.replace("&", "");
      theatres = DataBaseUtil.queryToDynaList(getAllUserTheatresList);
      return theatres;
    }
  }
  
  public static final String GET_ALL_THEATRES = "select  tm.theatre_id,tm.theatre_name, "
      + "tm.center_id,hcm.center_name from theatre_master tm "
      + "JOIN hospital_center_master hcm ON(hcm.center_id=tm.center_id)  "
      + "WHERE tm.STATUS='A' order by tm.theatre_name ";

  /**
   * Gets the theaters list.
   *
   * @return the theaters list
   * @throws SQLException the SQL exception
   */
  public List getTheatresList() throws SQLException {
    ArrayList theatres = DataBaseUtil.queryToArrayList(GET_ALL_THEATRES);
    return theatres;
  }

}
