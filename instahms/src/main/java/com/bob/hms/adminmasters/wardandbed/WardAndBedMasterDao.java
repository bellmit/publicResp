package com.bob.hms.adminmasters.wardandbed;

import com.bob.hms.common.AutoIncrementId;
import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.adminmasters.bedmaster.BedMasterDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * The Class WardAndBedMasterDAO.
 */
public class WardAndBedMasterDao {
  
  public static final Logger logger = LoggerFactory.getLogger(WardAndBedMasterDao.class);

  /** Constant GET_ACTIVE_WARD_NAME_SORTED. */
  private static final String GET_ACTIVE_WARD_NAME_SORTED = "SELECT * FROM ward_names "
      + " where status='A' order by ward_no";
  
  /**
   * Gets the ward names.
   *
   * @return the ward names
   */
  public List getWardNames() {
    return DataBaseUtil.queryToArrayList(GET_ACTIVE_WARD_NAME_SORTED);
  }

  /** The Constant GET_ALL_WARDS. */
  private static final String GET_ALL_WARDS = "SELECT * FROM ward_names";

  /**
   * Gets the all wards.
   *
   * @return the all wards
   * @throws SQLException the SQL exception
   */
  public static List getAllWards() throws SQLException {
    return DataBaseUtil.queryToDynaList(GET_ALL_WARDS);
  }

  /**
   * Gets the bed types.
   *
   * @return the bed types
   */
  public List getBedTypes() {
    String wardAndbedQuery = "select distinct bed_type  from bed_names ";
    return DataBaseUtil.queryToArrayList(wardAndbedQuery);
  }

  /** The Constant WARD_AND_BED_COUNT. */
  private static final String WARD_AND_BED_COUNT = "SELECT wn.ward_no,wn.ward_name,(select "
      + "count(bc.bed_type) from bed_names bc " + " where bc.status='I' and bc.bed_type=bn"
      + ".bed_type and wn.ward_no=bc.ward_no ) as inactive_count," + " (select count(bc"
      + ".bed_type) from bed_names bc where bc.status='A' and bc.bed_type=bn.bed_type " + " "
      + "and wn.ward_no=bc.ward_no ) as active_count,bn.bed_type, CASE WHEN wn.status='A' "
      + "then 'ACTIVE' " + " ELSE 'INACTIVE' END AS status ,wn.description,coalesce (bd"
      + ".bed_status,ibd.bed_status) as bed_status " + " FROM  ward_names wn LEFT JOIN "
      + "bed_names bn ON bn.ward_no = wn.ward_no " + " LEFT JOIN bed_details bd  ON   bd"
      + ".bed_type = bn.bed_type " + " LEFT JOIN icu_bed_charges ibd ON ibd"
      + ".intensive_bed_type = bn.bed_type WHERE wn.status='A'" + " GROUP BY bn.bed_type,wn"
      + ".ward_no,wn.ward_name,wn.status,wn.description,bd.bed_status,ibd.bed_status " + " "
      + "ORDER BY wn.ward_name, ward_no ";

  /**
   * Gets the ward and bed type count.
   *
   * @return the ward and bed type count
   * @throws SQLException the SQL exception
   */
  public static List getWardAndBedTypeCount() throws SQLException {
    try (
        Connection con = DataBaseUtil.getReadOnlyConnection();
        PreparedStatement ps = con.prepareStatement(WARD_AND_BED_COUNT);
        ) {
      return DataBaseUtil.queryToDynaList(ps);
    }
  }

  /** The Constant SEARCH_FIELDS. */
  private static final String SEARCH_FIELDS = "SELECT * ";

  /** The Constant SEARCH_COUNT. */
  private static final String SEARCH_COUNT = "SELECT count(*) ";

  /** The Constant SEARCH_TABLES. */
  private static final String SEARCH_TABLES = "FROM  " + "(SELECT wn.ward_no,wn.ward_name,"
      + "(select count(bc.bed_type) from bed_names bc " + " where bc.status='I' and bc"
      + ".bed_type=bn.bed_type and wn.ward_no=bc.ward_no ) as inactive_count," + " (select "
      + "count(bc.bed_type) from bed_names bc where bc.status='A' and bc.bed_type=bn.bed_type" + " "
      + " and wn.ward_no=bc.ward_no ) as active_count,bn.bed_type, CASE WHEN wn"
      + ".status='A' then 'ACTIVE' " + " ELSE 'INACTIVE' END AS status ,wn.description," + " "
      + "MAX(coalesce (bd.bed_status,ibd.bed_status)) as bed_status,wn.center_id " + " FROM  "
      + "ward_names wn " + " LEFT JOIN bed_names bn ON bn.ward_no = wn.ward_no " + " LEFT "
      + "JOIN bed_details bd  ON   bd.bed_type = bn.bed_type " + " LEFT JOIN icu_bed_charges "
      + "ibd ON ibd.intensive_bed_type = bn.bed_type " + " GROUP BY bn.bed_type,wn.ward_no)"
      + " AS foo";

  /**
   * Gets the ward and bed type list.
   *
   * @param requestParams the request params
   * @param pagingParams the paging params
   * @return the ward and bed type list
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   */
  public static PagedList getWardAndBedTypeList(Map requestParams, Map pagingParams)
      throws SQLException, ParseException {
    Connection con = null;
    SearchQueryBuilder qb = null;

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      qb = new SearchQueryBuilder(con, SEARCH_FIELDS, SEARCH_COUNT, SEARCH_TABLES, pagingParams);
      qb.addFilterFromParamMap(requestParams);
      qb.addSecondarySort("ward_no");
      qb.build();

      return qb.getMappedPagedList();
    } finally {
      if (qb != null) {
        qb.close();
      }
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /** The Constant Inactive_WARD_AND_BED_COUNT. */
  private static final String INACTIVE_WARD_AND_BED_COUNT = "SELECT wn.ward_no,wn.ward_name,"
      + "count(bn.bed_type)," + " bn.bed_type,CASE WHEN wn.status='A' then 'ACTIVE' ELSE "
      + "'INACTIVE' END AS status ,wn.description" + " FROM  ward_names wn " + " LEFT JOIN "
      + "bed_names bn on bn.ward_no = wn.ward_no and bn.status ='I'  " + " GROUP BY bn"
      + ".bed_type,wn.ward_no,wn.ward_name,wn.status,wn.description " + " ORDER BY ward_no ";

  /**
   * Gets the in ward and bed type count.
   *
   * @return the in ward and bed type count
   * @throws SQLException the SQL exception
   */
  public static List getInWardAndBedTypeCount() throws SQLException {
    try (
        Connection con = DataBaseUtil.getReadOnlyConnection();
        PreparedStatement ps = con.prepareStatement(INACTIVE_WARD_AND_BED_COUNT);
        ) {
      return DataBaseUtil.queryToDynaList(ps);
    }
  }

  /** The Constant GET_WARD_DETAILS. */
  private static final String GET_WARD_DETAILS = "SELECT wn.ward_no,wn.ward_name,count(bn"
      + ".bed_type), bn.bed_type,wn.status ,wn.description, wn.store_id,wn.center_id,"
      + " wn.allowed_gender FROM  ward_names wn LEFT JOIN "
      + " bed_names bn USING (ward_no) WHERE "
      + " wn.ward_no=? GROUP BY bn.bed_type,wn.ward_no,wn.ward_name,wn.status, "
      + " wn.description,wn.store_id,wn.center_id " + " ORDER BY ward_no";

  /**
   * Gets the war details.
   *
   * @param wardId the ward id
   * @return the war details
   * @throws SQLException the SQL exception
   */
  public static List getWarDetails(String wardId) throws SQLException {
    try (
        Connection con = DataBaseUtil.getReadOnlyConnection();
        PreparedStatement ps = con.prepareStatement(GET_WARD_DETAILS);
        ) {
      ps.setString(1, wardId);
      return DataBaseUtil.queryToDynaList(ps);
    }
  }

  /** The Constant UPDATE_WARD_DETAILS. */
  private static final String UPDATE_WARD_DETAILS = "UPDATE ward_names SET status=?,"
      + "description=?, store_id = ?, allowed_gender = ? WHERE" + " ward_no = ? ";

  /**
   * Update ward details.
   *
   * @param con the con
   * @param wn the wn
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public static boolean updateWardDetails(Connection con, WardNames wn) throws SQLException {
    boolean status = false;
    try (PreparedStatement ps = con.prepareStatement(UPDATE_WARD_DETAILS)) {
      ps.setString(1, wn.getStatus());
      ps.setString(2, wn.getDescription());
      ps.setInt(3, wn.getLinenStore());
      ps.setString(4, wn.getAllowedGender());
      ps.setString(5, wn.getWardNo());
  
      int result = ps.executeUpdate();
      if (result > 0) {
        status = true;
      } 
    }
    return status;
  }

  /** The Constant INSERT_BED_NAMES. */
  private static final String INSERT_BED_NAMES = "INSERT INTO bed_names(ward_no,bed_type,"
      + "bed_name,occupancy,bed_id)VALUES" + "(?,?,?,?,?)";

  /** The Constant GET_BED_COUNT. */
  private static final String GET_BED_COUNT = "SELECT  count(*) FROM Bed_Names where Ward_No "
      + "like ?  and Bed_Type like ?";

  /**
   * Insert bed names.
   *
   * @param con the con
   * @param bn the bn
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public static boolean insertBedNames(Connection con, BedNames bn) throws SQLException {
    boolean status = false;
    try (PreparedStatement ps = con.prepareStatement(GET_BED_COUNT)) {
      ps.setString(1, bn.getWardNo());
      ps.setString(2, bn.getBedType());

      String count = DataBaseUtil.getStringValueFromDb(ps);      
      int totalBeds = 0;
      if (count != null) {
        totalBeds = Integer.parseInt(count);
      }
      String newBedName = bn.getBedType() + (totalBeds + 1);
      bn.setBedName(newBedName);
    }

    try (PreparedStatement ps1 = con.prepareStatement(INSERT_BED_NAMES)) {
      ps1.setString(1, bn.getWardNo());
      ps1.setString(2, bn.getBedType());
      ps1.setString(3, bn.getBedName());
      ps1.setString(4, bn.getOccupancy());
      ps1.setInt(5, bn.getBedId());
  
      int result = ps1.executeUpdate();
      if (result > 0) {
        status = true;
      }
    }

    return status;
  }

  /**
   * Gets the next ward id.
   *
   * @return the next ward id
   * @throws SQLException the SQL exception
   */
  public static String getNextWardId() throws SQLException {
    String id = null;
    id = AutoIncrementId.getNewIncrId("WARD_NO", "WARD_NAMES", "WARDID");

    return id;
  }

  /** The Constant DUPLICATE_WARD_NAMES. */
  private static final String DUPLICATE_WARD_NAMES = "select wn.ward_no,wn.ward_name,bn"
      + ".bed_type from ward_names wn " + " LEFT JOIN bed_names bn ON bn.ward_no = wn.ward_no" + " "
      + " where wn.ward_name=? and bn.bed_type=? " + " GROUP BY bn.bed_type,wn.ward_no,"
      + "wn.ward_name ";

  /** The Constant INSERT_WARD_DETAILS. */
  private static final String INSERT_WARD_DETAILS = "INSERT INTO ward_names(ward_no,ward_name,"
      + "status,description,store_id,center_id,allowed_gender)" + "VALUES(?,?,?,?,?,?,?)";

  /**
   * Insert ward.
   *
   * @param con the con
   * @param wn the wn
   * @param bedType the bed type
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public static boolean insertWard(Connection con, WardNames wn, String bedType)
      throws SQLException {
    boolean status = false;
    boolean duplicate = false;

    try (PreparedStatement ps = con.prepareStatement(DUPLICATE_WARD_NAMES)) {
      ps.setString(1, wn.getWardName());
      ps.setString(2, bedType);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          duplicate = true;
        } 
      }
    }

    if (!duplicate) {
      try (PreparedStatement ps1 = con.prepareStatement(INSERT_WARD_DETAILS)) {
        ps1.setString(1, wn.getWardNo());
        ps1.setString(2, wn.getWardName());
        ps1.setString(3, wn.getStatus());
        ps1.setString(4, wn.getDescription());
        ps1.setInt(5, wn.getLinenStore());
        ps1.setInt(6, wn.getCenter_id());
        ps1.setString(7, wn.getAllowedGender());
  
        int result = ps1.executeUpdate();
        if (result > 0) {
          status = true;
        }
      }
    }
    
    return status;
  }

  /** The Constant GET_BED_NAMES_FIELDS. */
  private static final String GET_BED_NAMES_FIELDS = " SELECT ward_no,bed_type,bed_name,"
      + "occupancy,status,bed_id";
  
  /** The Constant GET_BED_NAMES_COUNT. */
  private static final String GET_BED_NAMES_COUNT = " SELECT count(*) ";
  
  /** The Constant GET_BED_NAMES_TABLES. */
  private static final String GET_BED_NAMES_TABLES = " FROM bed_names";

  /**
   * Gets the bed names.
   *
   * @param wardId the ward id
   * @param bedType the bed type
   * @param pageNum the page num
   * @return the bed names
   * @throws SQLException the SQL exception
   */
  public static PagedList getBedNames(String wardId, String bedType, int pageNum)
      throws SQLException {
    Connection con = DataBaseUtil.getReadOnlyConnection();

    SearchQueryBuilder qb = new SearchQueryBuilder(con, GET_BED_NAMES_FIELDS, GET_BED_NAMES_COUNT,
        GET_BED_NAMES_TABLES, null, null, "bed_id", false, 0, pageNum);

    qb.addFilter(SearchQueryBuilder.STRING, "ward_no", "=", wardId);
    qb.addFilter(SearchQueryBuilder.STRING, "bed_type", "=", bedType);
    qb.build();
    PreparedStatement psData = qb.getDataStatement();
    PreparedStatement psCount = qb.getCountStatement();

    List<BasicDynaBean> al = DataBaseUtil.queryToDynaList(psData);
    int count = Integer.parseInt((DataBaseUtil.getStringValueFromDb(psCount)));

    final PagedList pl = new PagedList(al, count, 0, pageNum);
    psData.close();
    psCount.close();
    con.close();

    return pl;
  }

  /** The Constant UPDATE_BED_NAMES. */
  private static final String UPDATE_BED_NAMES = "UPDATE bed_names SET status=?,bed_name=?  "
      + "WHERE " + " ward_no=? AND bed_type=? AND bed_id = ?  ";

  /**
   * Update bed names details.
   *
   * @param con the con
   * @param bn the bn
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public static boolean updateBedNamesDetails(Connection con, BedNames bn) throws SQLException {
    boolean status = false;

    try (PreparedStatement ps = con.prepareStatement(UPDATE_BED_NAMES)) {
      ps.setString(1, bn.getStatus());
      ps.setString(2, bn.getBedName());
      ps.setString(3, bn.getWardNo());
      ps.setString(4, bn.getBedType());
      ps.setInt(5, bn.getBedId());
  
      int result = ps.executeUpdate();
  
      if (result > 0) {
        status = true;
      }
    }

    return status;
  }

  /** The Constant GET_NEXT_BEDID. */
  private static final String GET_NEXT_BEDID = "SELECT nextval('bedid_sequence')";

  /**
   * Gets the next bed id.
   *
   * @return the next bed id
   * @throws SQLException the SQL exception
   */
  public static int getNextBedId() throws SQLException {
    int id = 0;
    Connection con = DataBaseUtil.getReadOnlyConnection();
    PreparedStatement ps = con.prepareStatement(GET_NEXT_BEDID);
    id = Integer.parseInt(DataBaseUtil.getStringValueFromDb(ps));
    ps.close();
    con.close();
    return id;
  }

  /** The Constant GET_BEDTYPE_STATUS. */
  private static final String GET_BEDTYPE_STATUS = " SELECT bed_status FROM bed_details WHERE "
      + "bed_type = ? ";

  /** The Constant GET_ICU_BEDTYPE_STATUS. */
  private static final String GET_ICU_BEDTYPE_STATUS = " SELECT bed_status FROM icu_bed_charges"
      + " WHERE intensive_bed_type = ? ";

  /**
   * Gets the bed type status.
   *
   * @param bedType the bed type
   * @return the bed type status
   * @throws SQLException the SQL exception
   */
  public static String getBedTypeStatus(String bedType) throws SQLException {
    String status = null;
    boolean isIcu = false;
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      isIcu = BedMasterDAO.isIcuBedType(bedType);
      if (!isIcu) {
        ps = con.prepareStatement(GET_BEDTYPE_STATUS);
        ps.setString(1, bedType);
        status = DataBaseUtil.getStringValueFromDb(ps);
      } else {
        ps = con.prepareStatement(GET_ICU_BEDTYPE_STATUS);
        ps.setString(1, bedType);
        status = DataBaseUtil.getStringValueFromDb(ps);
      }

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return status;
  }

  /** The Constant GET_WARD_NAMES. */
  private static final String GET_WARD_NAMES = "select wn.ward_name,wn.ward_no , bn.bed_type,"
      + "count(bn.bed_type) as totalbeds," + "(select count(bn1.bed_type)  from bed_names "
      + "bn1,ward_names wn1 where bn1.ward_no = wn1.ward_no and " + " wn1.status = 'A' and "
      + "bn1.occupancy='N'  and  bn.bed_type = bn1.bed_type   and " + " wn"
      + ".ward_no=wn1.ward_no and bn1.status='A'  group by wn1.ward_name,bn1.bed_type,"
      + "bn1.bed_type,wn1.ward_no   ) as freebeds " + " from bed_names bn,ward_names wn  "
      + "where bn.ward_no = wn.ward_no and wn.status = 'A' " + " group by wn.ward_name,bn"
      + ".bed_type,bn.bed_type,wn.ward_no";

  /**
   * Gets the wardnamesforselectedbedtype.
   *
   * @param bedtype the bedtype
   * @return the wardnamesforselectedbedtype
   * @throws SQLException the SQL exception
   */
  public static String getwardnamesforselectedbedtype(String bedtype) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    String wardDetails = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_WARD_NAMES);
      wardDetails = DataBaseUtil.getXmlContentWithNoChild(ps, "wardnames");
    } catch (Exception ex) {
      logger.error("", ex);
    } finally {
      try {
        if (ps != null) {
          ps.close();
        }
        if (con != null) {
          con.close();
        }
      } catch (Exception ex) {
        logger.error("", ex);
      }
    }
    return wardDetails;
  }

  /** The Constant GET_WARD_NAMES_FOR_BED_TYPE. */
  private static final String GET_WARD_NAMES_FOR_BED_TYPE = "SELECT ward_name,ward_no,bed_type,"
      + "" + " coalesce(totalbeds,0) AS totalbeds,coalesce(freebeds,0) AS freebeds FROM ("
      + " SELECT wn.ward_name,wn.ward_no , bn.bed_type,count(bn.bed_type) AS totalbeds," + " "
      + "(SELECT count(bn1.bed_type) FROM bed_names bn1,ward_names wn1 " + " WHERE "
      + "bn1.ward_no = wn1.ward_no AND wn1.status = 'A' AND bn1.occupancy='N' AND bn.bed_type"
      + " = bn1.bed_type " + " AND wn.ward_no=wn1.ward_no AND bn1.status='A' " + " GROUP BY "
      + "wn1.ward_name,bn1.bed_type,bn1.bed_type,wn1.ward_no) AS freebeds FROM bed_names"
      + " bn,ward_names wn  WHERE bn.ward_no = wn.ward_no AND wn.status = 'A' AND bn"
      + ".bed_type=? " + " GROUP BY wn.ward_name,bn.bed_type,bn.bed_type,wn.ward_no) AS foo";

  /**
   * Gets the wardnamesforbedtype.
   *
   * @param bedtype the bedtype
   * @return the wardnamesforbedtype
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getwardnamesforbedtype(String bedtype) throws SQLException {
    return DataBaseUtil.queryToDynaList(GET_WARD_NAMES_FOR_BED_TYPE, bedtype);
  }

  /** The Constant GET_OCCUPANCY. */
  private static final String GET_OCCUPANCY = "SELECT occupancy, bed_ref_id FROM bed_names" + ""
      + " WHERE bed_id=?";

  /**
   * Gets the occupancy.
   *
   * @param bedId the bed id
   * @return the occupancy
   * @throws SQLException the SQL exception
   */
  public static String getOccupancy(int bedId) throws SQLException {
    try (
        Connection con = DataBaseUtil.getReadOnlyConnection();
        PreparedStatement pstmt = con.prepareStatement(GET_OCCUPANCY);
        ) {
      String childOccupancy = null;
      String parentOccupancy = null;
      Integer parentBedId = null;
      
      
      pstmt.setInt(1, bedId);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          childOccupancy = rs.getString("occupancy");
          parentBedId = rs.getInt("bed_ref_id");
        }
        if (childOccupancy != null && childOccupancy.equals("Y")) {
          return "occupied";
        } else if (parentBedId != null && parentBedId != 0) {
          try ( PreparedStatement pstmt1 = con.prepareStatement(GET_OCCUPANCY)) {
            pstmt1.setInt(1, parentBedId);
            try (ResultSet rs1 = pstmt1.executeQuery()) {
              if (rs1.next()) {
                parentOccupancy = rs1.getString("occupancy");
              }
              if (parentOccupancy != null && parentOccupancy.equals("Y")) {
                return "occupied";
              } else {
                return "not occupied";
              }
            }
          }
        } else {
          return "not occupied";
        }        
      }
    }
  }

  /** The Constant GET_ALL_WARD_BEDS. */
  private static final String GET_ALL_WARD_BEDS = "SELECT bed_id FROM bed_names WHERE ward_no=?";

  /**
   * Gets the all bed ids.
   *
   * @param wardNo the ward no
   * @return the all bed ids
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getAllBedIds(String wardNo) throws SQLException {
    Connection con = null;
    PreparedStatement pstmt = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      pstmt = con.prepareStatement(GET_ALL_WARD_BEDS);
      pstmt.setString(1, wardNo);
      return DataBaseUtil.queryToDynaList(pstmt);
    } finally {
      DataBaseUtil.closeConnections(con, pstmt);
    }
  }

  /**
   * Update childs.
   *
   * @param con the con
   * @param bedRefId the bed ref id
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public static boolean updateChilds(Connection con, int bedRefId) throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement("UPDATE bed_names set status = 'I' WHERE bed_ref_id = ? ");
      ps.setInt(1, bedRefId);
      return ps.executeUpdate() > 0;
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

}
