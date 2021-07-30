package com.insta.hms.diagnosticsmasters.outhousemaster;

import com.bob.hms.common.AutoIncrementId;
import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.DynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class OutHouseMasterDAO.
 */
public class OutHouseMasterDAO extends GenericDAO {

  /** The out house fields. */
  private String outHouseFields = "SELECT * ";

  /** The out house test fields. */
  private String outHouseTestFields = "SELECT test_id, test_name ";

  /** The get count. */
  private String getCount = " SELECT COUNT (DISTINCT test_id) ";

  /** The out house tables. */
  private String outHouseTables = "FROM( "
      + " SELECT dom.outsource_dest_id,outsource_dest_type, "
      + " case when dom.outsource_dest_type IN ('O', 'IO') then oh_name "
      + " when outsource_dest_type='C' "
      + " then hcm.center_name end as outsource_name,charge,dg.test_name,ohd.test_id,"
      + " ohd.status AS test_status,oh.oh_id, ohd.source_center_id, hcm1.center_name "
      + " FROM diag_outsource_master dom "
      + " JOIN diag_outsource_detail ohd on ohd.outsource_dest_id = dom.outsource_dest_id "
      + " JOIN diagnostics dg on dg.test_id = ohd.test_id "
      + " LEFT JOIN outhouse_master oh on (oh.oh_id = dom.outsource_dest) "
      + " LEFT JOIN hospital_center_master hcm on (hcm.center_id::text = dom.outsource_dest) "
      + " LEFT JOIN hospital_center_master hcm1 on (hcm1.center_id = ohd.source_center_id) )"
      + " AS foo";

  /**
   * Instantiates a new out house master DAO.
   */
  public OutHouseMasterDAO() {
    super("outhouse_master");
  }

  /** The get all outsources for center. */
  private static String GET_ALL_OUTSOURCES_FOR_CENTER = "SELECT dod.outsource_dest_id, dod.test_id,"
      + " dod.charge, dod.status, "
      + " dod.source_center_id, dod.default_outsource, dod.diag_outsource_detail_id, "
      + " d.test_name, dom.outsource_dest_type,dom.outsource_dest,"
      + " case when dom.outsource_dest_type IN ('O', 'IO') then om.oh_name ELSE hcm.center_name "
      + "end as outsource_name, hcm.center_id "
      + " FROM diag_outsource_detail dod "
      + " JOIN diagnostics d  ON (d.test_id=dod.test_id) "
      + " JOIN tests_prescribed tp ON (tp.test_id = dod.test_id) "
      + " LEFT JOIN diag_outsource_master dom ON (dom.outsource_dest_id = dod.outsource_dest_id) "
      + " LEFT JOIN outhouse_master om ON(om.oh_id = dom.outsource_dest) AND om.status='A' "
      + " LEFT JOIN hospital_center_master hcm ON(hcm.center_id::text = dom.outsource_dest) "
      + " AND hcm.status='A' "
      + " WHERE dod.status='A' AND dod.source_center_id=? AND tp.pat_id = ? "
      + " GROUP BY dod.outsource_dest_id, dod.test_id, dod.charge, dod.status, "
      + " dod.source_center_id, dod.default_outsource, dod.diag_outsource_detail_id, "
      + " d.test_name, dom.outsource_dest_type,dom.outsource_dest, "
      + " dom.outsource_dest_type, om.oh_name, hcm.center_name, hcm.center_name, hcm.center_id "
      + " order by outsource_name ";

  // when center concept is not enabled, no records will be inserted into the center_outhouses,
  /** The get all outsources. */
  // hence no need to join with that in this case
  private static String GET_ALL_OUTSOURCES = "SELECT dod.outsource_dest_id, dod.test_id,"
      + " dod.charge, dod.status, "
      + " dod.source_center_id, dod.default_outsource, dod.diag_outsource_detail_id, "
      + " d.test_name, dom.outsource_dest_type,dom.outsource_dest, "
      + " case when dom.outsource_dest_type IN ('O', 'IO') then om.oh_name "
      + "ELSE hcm.center_name end as outsource_name, hcm.center_id "
      + " FROM diag_outsource_detail dod "
      + " JOIN diagnostics d  ON (d.test_id=dod.test_id) "
      + " JOIN tests_prescribed tp ON (tp.test_id = dod.test_id) "
      + " LEFT JOIN diag_outsource_master dom ON (dom.outsource_dest_id = dod.outsource_dest_id) "
      + " LEFT JOIN outhouse_master om ON(om.oh_id = dom.outsource_dest) AND om.status='A' "
      + " LEFT JOIN hospital_center_master hcm "
      + " ON(hcm.center_id::text = dom.outsource_dest) AND hcm.status='A' "
      + " WHERE dod.status='A' AND tp.pat_id = ? "
      + " GROUP BY dod.outsource_dest_id, dod.test_id, dod.charge, dod.status, "
      + " dod.source_center_id, dod.default_outsource, dod.diag_outsource_detail_id, "
      + " d.test_name, dom.outsource_dest_type,dom.outsource_dest, "
      + " dom.outsource_dest_type, om.oh_name, hcm.center_name, hcm.center_name, hcm.center_id "
      + " order by outsource_name ";

  /**
   * Gets the all out source name.
   *
   * @param centerId the center id
   * @param visitId the visit id
   * @return the all out source name
   * @throws SQLException the SQL exception
   */
  public static ArrayList getAllOutSourceName(int centerId, String visitId) throws SQLException {
    ArrayList outHousenames = new ArrayList();
    Connection con = null;
    con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      int maxCentersIncDefault = (Integer) GenericPreferencesDAO.getAllPrefs().get(
          "max_centers_inc_default");
      if (maxCentersIncDefault > 1) {
        ps = con.prepareStatement(GET_ALL_OUTSOURCES_FOR_CENTER);
        ps.setInt(1, centerId);
        ps.setString(2, visitId);
      } else {
        ps = con.prepareStatement(GET_ALL_OUTSOURCES);
        ps.setString(1, visitId);
      }
      outHousenames = DataBaseUtil.queryToArrayList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return outHousenames;

  }

  /** The get all outsources incoming for center. */
  private static String GET_ALL_OUTSOURCES_INCOMING_FOR_CENTER = "SELECT dod.outsource_dest_id, "
      + "dod.test_id, dod.charge, dod.status, "
      + " dod.source_center_id, dod.default_outsource, dod.diag_outsource_detail_id, "
      + " d.test_name, dom.outsource_dest_type,dom.outsource_dest,"
      + " case when dom.outsource_dest_type IN ('O', 'IO') then om.oh_name ELSE hcm.center_name "
      + "end as outsource_name, hcm.center_id "
      + " FROM diag_outsource_detail dod "
      + " JOIN diagnostics d  ON (d.test_id=dod.test_id) "
      + " LEFT JOIN diag_outsource_master dom ON (dom.outsource_dest_id = dod.outsource_dest_id) "
      + " LEFT JOIN outhouse_master om ON(om.oh_id = dom.outsource_dest) AND om.status='A' "
      + " LEFT JOIN hospital_center_master hcm ON(hcm.center_id::text = dom.outsource_dest)"
      + " AND hcm.status='A' "
      + " WHERE dod.status='A' AND dod.source_center_id=? "
      + " GROUP BY dod.outsource_dest_id, dod.test_id, dod.charge, dod.status, "
      + " dod.source_center_id, dod.default_outsource, dod.diag_outsource_detail_id, "
      + " d.test_name, dom.outsource_dest_type,dom.outsource_dest, "
      + " dom.outsource_dest_type, om.oh_name, hcm.center_name, hcm.center_name, hcm.center_id "
      + " order by outsource_name ";

  /** The get all outsources incoming. */
  private static String GET_ALL_OUTSOURCES_INCOMING = "SELECT dod.outsource_dest_id, dod.test_id,"
      + " dod.charge, dod.status, "
      + " dod.source_center_id, dod.default_outsource, dod.diag_outsource_detail_id, "
      + " d.test_name, dom.outsource_dest_type,dom.outsource_dest, "
      + " case when dom.outsource_dest_type IN ('O', 'IO') then om.oh_name ELSE hcm.center_name"
      + " end as outsource_name, hcm.center_id "
      + " FROM diag_outsource_detail dod "
      + " JOIN diagnostics d  ON (d.test_id=dod.test_id) "
      + " LEFT JOIN diag_outsource_master dom ON (dom.outsource_dest_id = dod.outsource_dest_id) "
      + " LEFT JOIN outhouse_master om ON(om.oh_id = dom.outsource_dest) AND om.status='A' "
      + " LEFT JOIN hospital_center_master hcm ON(hcm.center_id::text = dom.outsource_dest) "
      + " AND hcm.status='A' "
      + " WHERE dod.status='A' "
      + " GROUP BY dod.outsource_dest_id, dod.test_id, dod.charge, dod.status, "
      + " dod.source_center_id, dod.default_outsource, dod.diag_outsource_detail_id, "
      + " d.test_name, dom.outsource_dest_type,dom.outsource_dest, "
      + " dom.outsource_dest_type, om.oh_name, hcm.center_name, hcm.center_name, hcm.center_id "
      + " order by outsource_name ";

  /**
   * Gets the all out source name for incoming.
   *
   * @param centerId the center id
   * @return the all out source name for incoming
   * @throws SQLException the SQL exception
   */
  public static ArrayList getAllOutSourceNameForIncoming(int centerId) throws SQLException {
    ArrayList outHousenames = new ArrayList();
    Connection con = null;
    con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      int maxCentersIncDefault = (Integer) GenericPreferencesDAO.getAllPrefs().get(
          "max_centers_inc_default");
      if (maxCentersIncDefault > 1) {
        ps = con.prepareStatement(GET_ALL_OUTSOURCES_INCOMING_FOR_CENTER);
        ps.setInt(1, centerId);
      } else {
        ps = con.prepareStatement(GET_ALL_OUTSOURCES_INCOMING);
      }
      outHousenames = DataBaseUtil.queryToArrayList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return outHousenames;

  }

  /** The get all active outsources. */
  private static String GET_ALL_ACTIVE_OUTSOURCES = "SELECT oh_name as outsource_name,oh_id "
      + " as outsource_dest FROM outhouse_master where status = 'A' "
      + " UNION ALL "
      + " SELECT center_name as outsource_name, center_id::text as outsource_dest "
      + " FROM hospital_center_master hcm "
      + " JOIN diag_outsource_master dom ON (dom.outsource_dest = hcm.center_id::text) "
      + " WHERE dom.status='A' ";

  /**
   * Gets the all active out source name.
   *
   * @return the all active out source name
   * @throws SQLException the SQL exception
   */
  public static ArrayList getAllActiveOutSourceName() throws SQLException {
    ArrayList outHousenames = new ArrayList();
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_ALL_ACTIVE_OUTSOURCES);
      outHousenames = DataBaseUtil.queryToArrayList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return outHousenames;
  }

  /** The get all outsources names. */
  private static String GET_ALL_OUTSOURCES_NAMES = "SELECT CASE WHEN outsource_dest_type ='C' "
      + "THEN center_name ELSE oh_name END AS outsource_name "
      + " FROM diag_outsource_master dom "
      + " LEFT JOIN outhouse_master om ON (dom.outsource_dest = om.oh_id) "
      + " LEFT JOIN hospital_center_master hcm ON (hcm.center_id::text = dom.outsource_dest)";
  
  /** The get all outsources list. */
  private static String GET_ALL_OUTSOURCES_LIST = "SELECT CASE WHEN outsource_dest_type ='C'"
      + " THEN center_name  ELSE oh_name END AS outsource_name, dom.outsource_dest_id"
      + " FROM diag_outsource_master dom "
      + " LEFT JOIN outhouse_master om ON (dom.outsource_dest = om.oh_id) "
      + " LEFT JOIN hospital_center_master hcm ON (hcm.center_id::text = dom.outsource_dest)";

  /** The get all multi outsources list. */
  private static String GET_ALL_MULTI_OUTSOURCES_LIST = "SELECT CASE WHEN outsource_dest_type ='C'"
      + " THEN center_name ELSE oh_name END AS outsource_name, dom.outsource_dest_id"
      + " FROM diag_outsource_master dom "
      + " LEFT JOIN outhouse_master om ON (dom.outsource_dest = om.oh_id) "
      + " LEFT JOIN center_outsources co ON (dom.outsource_dest = co.outsource_id) "
      + " LEFT JOIN hospital_center_master hcm ON (hcm.center_id::text = dom.outsource_dest) "
      + "where co.center_id = ?";

  /**
   * Gets the all out sources.
   *
   * @return the all out sources
   * @throws SQLException the SQL exception
   */
  public static ArrayList getAllOutSources() throws SQLException {
    ArrayList outHouses = new ArrayList();
    Connection con = null;
    con = DataBaseUtil.getConnection();
    PreparedStatement ps = con.prepareStatement(GET_ALL_OUTSOURCES_NAMES);
    outHouses = DataBaseUtil.queryToArrayList(ps);
    ps.close();
    con.close();
    return outHouses;

  }

  /**
   * Gets the all out sources list.
   *
   * @param centerId the center id
   * @return the all out sources list
   * @throws SQLException the SQL exception
   */
  public static ArrayList getAllOutSourcesList(int centerId) throws SQLException {
    ArrayList outHouselist = new ArrayList();
    Connection con = null;
    con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      if (centerId == 0) {
        ps = con.prepareStatement(GET_ALL_OUTSOURCES_LIST);
      } else {
        ps = con.prepareStatement(GET_ALL_MULTI_OUTSOURCES_LIST);
        ps.setInt(1, centerId);
      }
      outHouselist = DataBaseUtil.queryToArrayList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return outHouselist;
  }

  /** The get all departments. */
  private static String GET_ALL_DEPARTMENTS = " SELECT ddept_id,ddept_name "
      + "FROM diagnostics_departments  WHERE status='A' ";

  /**
   * Gets the all departments.
   *
   * @return the all departments
   * @throws SQLException the SQL exception
   */
  public ArrayList getAllDepartments() throws SQLException {
    ArrayList deptnames = new ArrayList();
    Connection con = null;
    con = DataBaseUtil.getConnection();
    PreparedStatement ps = con.prepareStatement(GET_ALL_DEPARTMENTS);
    deptnames = DataBaseUtil.queryToArrayList(ps);
    ps.close();
    con.close();
    return deptnames;
  }

  /** The get all tests. */
  private static String GET_ALL_TESTS = " SELECT test_id,test_name FROM diagnostics d "
      + " WHERE  d.status='A' "
      + " AND NOT EXISTS (SELECT test_id FROM diag_outsource_detail dod"
      + " WHERE dod.test_id = d.test_id) "
      + " order by d.test_name ";

  /**
   * Gets the all test names.
   *
   * @return the all test names
   * @throws SQLException the SQL exception
   */
  public ArrayList getAllTestNames() throws SQLException {
    ArrayList deptnames = new ArrayList();
    Connection con = null;
    con = DataBaseUtil.getConnection();
    PreparedStatement ps = con.prepareStatement(GET_ALL_TESTS);
    deptnames = DataBaseUtil.queryToArrayList(ps);
    ps.close();
    con.close();
    return deptnames;
  }

  /** The get all template names. */
  private static String GET_ALL_TEMPLATE_NAMES = "SELECT template_name FROM "
      + "diagnostic_outhouse_print_template ";

  /**
   * Gets the all template names.
   *
   * @return the all template names
   * @throws SQLException the SQL exception
   */
  public ArrayList getAllTemplateNames() throws SQLException {
    ArrayList templateNames = new ArrayList();
    Connection con = null;
    con = DataBaseUtil.getConnection();
    PreparedStatement ps = con.prepareStatement(GET_ALL_TEMPLATE_NAMES);
    templateNames = DataBaseUtil.queryToArrayList(ps);
    ps.close();
    con.close();
    return templateNames;
  }

  /** The get all out house tests. */
  private static String GET_ALL_OUT_HOUSE_TESTS = " SELECT test_id,test_name "
      + "FROM diagnostics ";

  /**
   * Gets the all tests.
   *
   * @return the all tests
   * @throws SQLException the SQL exception
   */
  public ArrayList getAllTests() throws SQLException {
    ArrayList testnames = new ArrayList();
    Connection con = null;
    con = DataBaseUtil.getConnection();
    PreparedStatement ps = con.prepareStatement(GET_ALL_OUT_HOUSE_TESTS);
    testnames = DataBaseUtil.queryToArrayList(ps);
    ps.close();
    con.close();
    return testnames;

  }

  /** The query field length. */
  public static final String[] QUERY_FIELD_LENGTH = { "", "dg.test_name", "oh_name", "oh_charge" };

  /** The field none. */
  public static final int FIELD_NONE = 0;

  /** The field test name. */
  public static final int FIELD_TEST_NAME = 1;

  /** The field out house. */
  public static final int FIELD_OUT_HOUSE = 2;

  /** The field charge. */
  public static final int FIELD_CHARGE = 3;

  /** The Constant GROUP_BY. */
  private static final String GROUP_BY = " test_id, test_name ";

  /**
   * Gets the out house details.
   *
   * @param filters the filters
   * @param pagingParams the paging params
   * @return the out house details
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   */
  public PagedList getOutHouseDetails(Map filters, Map<LISTING, Object> pagingParams)
      throws SQLException, ParseException {
    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      SearchQueryBuilder qb = new SearchQueryBuilder(con, outHouseTestFields, getCount,
          outHouseTables, null, GROUP_BY, pagingParams);
      qb.addFilterFromParamMap(filters);
      qb.addSecondarySort("test_id");
      qb.build();

      PagedList pagedList = qb.getMappedPagedList();
      return pagedList;
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }

  }
  
  /**
   * Gets the out house details.
   *
   * @param ohID the oh ID
   * @return the out house details
   * @throws SQLException the SQL exception
   */
  public OutHouseMaster getOutHouseDetails(String ohID) throws SQLException {
    ResultSet rs = null;
    PreparedStatement ps = null;
    Connection con = DataBaseUtil.getReadOnlyConnection();
    OutHouseMaster dto = new OutHouseMaster();
    try {
      ps = con.prepareStatement(GET_OUT_HOUSE_DETAILS);
      ps.setString(1, ohID);
      rs = ps.executeQuery();
      while (rs.next()) {
        dto.setOhName(rs.getString("OH_NAME"));
        dto.setOhId(rs.getString("OH_ID"));
        dto.setStatus(rs.getString("STATUS"));
        dto.setTemplate_name(rs.getString("TEMPLATE_NAME"));
        dto.setCliaNo(rs.getString("CLIA_NO"));
        dto.setOhAddress(rs.getString("OH_ADDRESS"));
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps, rs);
    }
    return dto;
  }


  /**
   * Gets the out house test details.
   *
   * @param filters the filters
   * @param testIDs the test I ds
   * @return the out house test details
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   */
  public List<BasicDynaBean> getOutHouseTestDetails(Map filters, List<String> testIDs)
      throws SQLException, ParseException {
    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      SearchQueryBuilder qb = new SearchQueryBuilder(con, outHouseFields, null, outHouseTables,
          null, null, false, 0, 0);
      qb.addFilterFromParamMap(filters);
      qb.addFilter(SearchQueryBuilder.STRING, "test_id", "IN", testIDs);
      qb.addSecondarySort("test_id");
      qb.build();

      List list = DataBaseUtil.queryToDynaList(qb.getDataStatement());
      qb.close();
      return list;
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }

  }

  /** The get outsource name. */
  private static String GET_OUTSOURCE_NAME = " SELECT DISTINCT  "
      + " CASE WHEN outsource_dest_type = 'O' THEN oh_name ELSE center_name END AS outsource_name,"
      + " test_name,charge,osd.test_id,dc.ddept_id,dom.status,osd.status as teststatus  "
      + " FROM diag_outsource_master dom "
      + " JOIN outhouse_master om ON (om.oh_id = dom.outsource_dest) "
      + " LEFT JOIN hospital_center_master hcm ON (hcm.center_id::text = dom.outsource_dest) "
      + " JOIN diag_outsource_detail  osd ON (osd.outsource_dest_id = dom.outsource_dest_id) "
      + " JOIN diagnostics  dc ON (dc.test_id = osd.test_id) "
      + " WHERE dom.outsource_dest_id = ? and osd.test_id = ? ";

  /**
   * Gets the out source name.
   *
   * @param outSourceDestId the out source dest id
   * @param testId the test id
   * @return the out source name
   * @throws SQLException the SQL exception
   */
  public ArrayList<Hashtable<String, String>> getOutSourceName(int outSourceDestId, String testId)
      throws SQLException {
    ArrayList<Hashtable<String, String>> outSourceName = new ArrayList<Hashtable<String, String>>();
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_OUTSOURCE_NAME);
      ps.setInt(1, outSourceDestId);
      ps.setString(2, testId);
      outSourceName = DataBaseUtil.queryToArrayList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return outSourceName;
  }

  /** The get test details. */
  private static String GET_TEST_DETAILS = " SELECT outsource_dest_id, dod.test_id, "
      + "dod.status, dod.charge,  CASE WHEN dom.outsource_dest_type IN ('O', 'IO') THEN om.oh_name"
      + " WHEN dom.outsource_dest_type='C' THEN hcm.center_name END AS "
      + " outsource_name, d.test_name, "
      + " h.center_name AS source_center_name, dod.source_center_id, dod.default_outsource, "
      + "dod.diag_outsource_detail_id"
      + " FROM diag_outsource_detail dod "
      + " LEFT JOIN diag_outsource_master dom USING(outsource_dest_id) "
      + " LEFT JOIN outhouse_master om ON (om.oh_id=dom.outsource_dest) "
      + " LEFT JOIN hospital_center_master h ON (h.center_id = dod.source_center_id) "
      + " LEFT JOIN hospital_center_master hcm ON (hcm.center_id::text=dom.outsource_dest) "
      + " JOIN diagnostics d ON (d.test_id = dod.test_id) "
      + " WHERE dod.test_id=? ORDER BY h.center_name ";

  /**
   * Gets the outhouse test details.
   *
   * @param testId the test id
   * @return the outhouse test details
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getOuthouseTestDetails(String testId) throws SQLException {

    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_TEST_DETAILS);
      ps.setString(1, testId);
      return DataBaseUtil.queryToDynaListDates(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The is test exist. */
  private static final String IS_TEST_EXIST = "SELECT COUNT(*) FROM diag_outsource_detail "
      + " WHERE test_id=? ";

  /**
   * Checks if is outhouse test exist.
   *
   * @param testId the test id
   * @return true, if is outhouse test exist
   * @throws SQLException the SQL exception
   */
  public boolean isOuthouseTestExist(String testId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    boolean exist = false;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(IS_TEST_EXIST);
      ps.setString(1, testId);
      int res = DataBaseUtil.getIntValueFromDb(ps);
      if (res > 0) {
        exist = true;
      } 
    } finally {
      DataBaseUtil.closeConnections(con, ps, rs); 
    }
    return exist;
  }

  /** The chek for existence out house. */
  private static final String CHEK_FOR_EXISTENCE_OUT_HOUSE = "select count(*) from "
      + "outhouse_master where oh_name = ? ";

  /** The insert new out house details. */
  private static final String INSERT_NEW_OUT_HOUSE_DETAILS = "  INSERT INTO outhouse_master"
      + "( oh_id,oh_name,template_name,status,clia_no,oh_address ) "
      + "  values ( ?,?,?,?,?,? ) ";

  /** The out source master details. */
  private static final String OUT_SOURCE_MASTER_DETAILS = "SELECT * FROM diag_outsource_detail ";

  /**
   * Gets the out source master details.
   *
   * @return the out source master details
   * @throws SQLException the SQL exception
   */
  public ArrayList getOutSourceMasterDetails() throws SQLException {
    ArrayList outHouseMaster = new ArrayList();
    Connection con = null;
    con = DataBaseUtil.getConnection();
    PreparedStatement ps = con.prepareStatement(OUT_SOURCE_MASTER_DETAILS);
    outHouseMaster = DataBaseUtil.queryToArrayList(ps);
    ps.close();
    con.close();
    return outHouseMaster;
  }

  /**
   * Gets the next out house ID.
   *
   * @return the next out house ID
   * @throws Exception the exception
   */
  public String getNextOutHouseID() throws Exception {
    String id = null;
    // id = AutoIncrementId.getNewIncrId("OH_ID", "OUTHOUSE_MASTER", "Out House Hospital");
    id = AutoIncrementId.getSequenceId("outhouseid_sequence", "Out House Hospital");
    return id;
  }

  /** The get outhouse id for name. */
  private static String GET_OUTHOUSE_ID_FOR_NAME = "SELECT oh_id FROM outhouse_master "
      + "WHERE oh_name =?";

  /**
   * Gets the out house id for name.
   *
   * @param outhouseName the outhouse name
   * @return the out house id for name
   * @throws SQLException the SQL exception
   */
  public static String getOutHouseIdForName(String outhouseName) throws SQLException {
    String id = null;
    outhouseName = outhouseName.trim();
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_OUTHOUSE_ID_FOR_NAME);
      ps.setString(1, outhouseName);
      id = DataBaseUtil.getStringValueFromDb(ps);
      if (id == null) {
        id = AutoIncrementId.getSequenceId("outhouseid_sequence", "Out House Hospital");
      } else if (id.equals("")) {
        id = AutoIncrementId.getSequenceId("outhouseid_sequence", "Out House Hospital");
      }

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }

    return id;
  }

  /**
   * Adds the new outhouse.
   *
   * @param dto the dto
   * @return true, if successful
   * @throws Exception the exception
   */
  public static boolean addNewOuthouse(OutHouseMaster dto) throws Exception {
    Connection con = DataBaseUtil.getConnection();
    con.setAutoCommit(false);
    PreparedStatement ps = null;
    PreparedStatement cps = null;
    try {
      ps = con.prepareStatement(INSERT_NEW_OUT_HOUSE_DETAILS);
      cps = con.prepareStatement(CHEK_FOR_EXISTENCE_OUT_HOUSE);

      cps.setString(1, dto.getOhName());
      String count = DataBaseUtil.getStringValueFromDb(cps);
      if (count.equals("0")) {
        ps.setString(1, dto.getOhId());
        ps.setString(2, dto.getOhName());
        ps.setString(3, "A");
        int res = ps.executeUpdate();
      }
      con.commit();
    } finally {
      DataBaseUtil.closeConnections(con, ps);
      DataBaseUtil.closeConnections(null, cps);
    }
    return false;
  }

  /** The get out house details. */
  private static String GET_OUT_HOUSE_DETAILS = " SELECT * FROM outhouse_master where oh_id = ? ";

  /** The get out house name. */
  private static String GET_OUT_HOUSE_NAME = "SELECT count(*) FROM outhouse_master "
      + "where oh_name = ? ";
  
  /** The update out house. */
  private static String UPDATE_OUT_HOUSE = "  UPDATE outhouse_master SET oh_name = ?,"
      + "template_name = ?, status = ? ,clia_no = ?,oh_address = ?"
      + "  WHERE oh_id = ?   ";
  
  /** The update out house test status. */
  private static String UPDATE_OUT_HOUSE_TEST_STATUS = "UPDATE ohmaster_detail SET "
      + "status = ? WHERE outsource_dest_id = ? ";

  /**
   * Update out house details.
   *
   * @param con the con
   * @param ohid the ohid
   * @param outsourceDestId the outsource dest id
   * @param ohname the ohname
   * @param outhousename the outhousename
   * @param ohstatus the ohstatus
   * @param templateName the template name
   * @param cliaNo the clia no
   * @param address the address
   * @return the int
   * @throws SQLException the SQL exception
   */
  public int updateOutHouseDetails(Connection con, String ohid, String outsourceDestId,
      String ohname, String outhousename, String ohstatus, String templateName, String cliaNo,
      String address) throws SQLException {
    int inc = 0;
    String count = "0";
    PreparedStatement ps = null;
    PreparedStatement ps1 = null;
    try {
      if (!outhousename.equals(ohname)) {

        ps1 = con.prepareStatement(GET_OUT_HOUSE_NAME);
        ps1.setString(1, ohname);
        count = DataBaseUtil.getStringValueFromDb(ps1);
      }
      if (count.equals("0")) {
        ps = con.prepareStatement(UPDATE_OUT_HOUSE);
        ps.setString(1, ohname);
        ps.setString(2, templateName);
        ps.setString(3, ohstatus);
        ps.setString(4, cliaNo);
        ps.setString(5, address);
        ps.setString(6, ohid);
        inc = ps.executeUpdate();
        ps.close();
        if (inc > 0) {
          ps = con.prepareStatement(UPDATE_OUT_HOUSE_TEST_STATUS);
          ps.setString(1, ohstatus);
          ps.setInt(2, Integer.parseInt(outsourceDestId));
          inc = ps.executeUpdate();
        }

      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
      DataBaseUtil.closeConnections(null, ps1);
    }

    return inc;

  }

  /** The Constant GET_NEWLY_ADDED_OUTHOUSEID. */
  private static final String GET_NEWLY_ADDED_OUTHOUSEID = "SELECT oh_id "
      + "FROM outhouse_master WHERE oh_id NOT IN( "
      + " SELECT outsource_dest FROM diag_outsource_master WHERE outsource_dest_type='O')";

  /**
   * Gets the newly added outhouse id.
   *
   * @return the newly added outhouse id
   * @throws Exception the exception
   * @throws SQLException the SQL exception
   */
  public String getNewlyAddedOuthouseId() throws Exception, SQLException {
    String outhouseId = null;
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_NEWLY_ADDED_OUTHOUSEID);
      outhouseId = DataBaseUtil.getStringValueFromDb(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return outhouseId;
  }

  /** The Constant GET_OUTSOURCES_WITH_RESPECT_TO_CENTERS. */
  private static final String GET_OUTSOURCES_WITH_RESPECT_TO_CENTERS = " SELECT  co.center_id, "
      + "CASE WHEN dom.outsource_dest_type IN ('O', 'IO') THEN om.oh_id "
      + "WHEN dom.outsource_dest_type = 'C' THEN hcm.center_id::varchar END AS outsource_id,"
      + "CASE WHEN dom.outsource_dest_type IN ('O', 'IO') THEN om.oh_name "
      + "WHEN dom.outsource_dest_type = 'C' THEN hcm.center_name END AS outsource_name,"
      + "dom.outsource_dest_id "
      + " FROM diag_outsource_master  dom "
      + "  JOIN center_outsources co ON (co.outsource_id = dom.outsource_dest)"
      + "  LEFT JOIN outhouse_master om ON (om.oh_id = co.outsource_id)"
      + "  LEFT JOIN hospital_center_master hcm ON (hcm.center_id::varchar = co.outsource_id)"
      + " WHERE dom.status = 'A'"
      + " GROUP BY co.center_id, dom.outsource_dest_type, om.oh_id, hcm.center_id, "
      + "dom.outsource_dest_id, om.oh_name, hcm.center_name";

  /**
   * Gets the outsources respect to center.
   *
   * @return the outsources respect to center
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getOutsourcesRespectToCenter() throws SQLException {
    Connection con = null;
    PreparedStatement pstmt = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      pstmt = con.prepareStatement(GET_OUTSOURCES_WITH_RESPECT_TO_CENTERS);
      return DataBaseUtil.queryToDynaList(pstmt);
    } finally {
      DataBaseUtil.closeConnections(con, pstmt);
    }
  }

  /** The out house list tables. */
  private static final String OUT_HOUSE_LIST_TABLES = "    FROM( "
      + " SELECT distinct dom.outsource_dest_id,outsource_dest_type, "
      + " case when outsource_dest_type='O' then oh_name when outsource_dest_type='IO' then"
      + " oh_name when outsource_dest_type='C' "
      + " then center_name end as outsource_name,"
      + " dom.status as status,oh.oh_id "
      + " FROM diag_outsource_master dom "
      + " LEFT JOIN outhouse_master oh on (oh.oh_id = dom.outsource_dest) "
      + " LEFT JOIN hospital_center_master hcm on (hcm.center_id::text = dom.outsource_dest))"
      + "AS foo";

  /** The out house list count. */
  private static final String OUT_HOUSE_LIST_COUNT = "SELECT COUNT (*)";

  /**
   * Gets the out house L ist.
   *
   * @param filters the filters
   * @param pagingParams the paging params
   * @return the out house L ist
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   */
  public PagedList getOutHouseLIst(Map filters, Map<LISTING, Object> pagingParams)
      throws SQLException, ParseException {
    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      SearchQueryBuilder qb = new SearchQueryBuilder(con, outHouseFields, OUT_HOUSE_LIST_COUNT,
          OUT_HOUSE_LIST_TABLES, pagingParams);
      qb.addFilterFromParamMap(filters);
      qb.addSecondarySort("outsource_dest_id");
      qb.build();

      PagedList list = qb.getMappedPagedList();
      return list;
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /** The Constant GET_OUTSOURCES_ID_WITH_RESPECT_TO_CENTERS. */
  private static final String GET_OUTSOURCES_ID_WITH_RESPECT_TO_CENTERS = " SELECT  co.center_id "
      + "as source_center_id, dom.outsource_dest_id "
      + " FROM diag_outsource_master  dom "
      + "  JOIN center_outsources co ON (co.outsource_id = dom.outsource_dest)"
      + " WHERE dom.status = 'A'";

  /**
   * Gets the outsource I ds respect to center.
   *
   * @return the outsource I ds respect to center
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getOutsourceIDsRespectToCenter() throws SQLException {
    Connection con = null;
    PreparedStatement pstmt = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      pstmt = con.prepareStatement(GET_OUTSOURCES_ID_WITH_RESPECT_TO_CENTERS);
      return DataBaseUtil.queryToDynaList(pstmt);
    } finally {
      DataBaseUtil.closeConnections(con, pstmt);
    }
  }

  /**
   * List bean to map list val.
   *
   * @param beans the beans
   * @param columnName the column name
   * @param colValue the col value
   * @return the hash map
   */
  public static HashMap listBeanToMapListVal(List beans, String columnName, String colValue) {
    HashMap rowMap = new LinkedHashMap();
    Iterator it = beans.iterator();
    while (it.hasNext()) {
      DynaBean row = (DynaBean) it.next();
      Object colName = row.get(columnName);
      List list = (List) rowMap.get(colName);
      if (list == null) {
        list = new ArrayList();
        rowMap.put(colName, list);
      }
      list.add(row.get(colValue));
    }
    return rowMap;
  }

  /** The get center outsource list. */
  private static String GET_CENTER_OUTSOURCE_LIST = " SELECT d.test_id, hcm.center_id, "
      + "dod.outsource_dest_id FROM diag_outsource_detail dod "
      + " JOIN diagnostics d  ON (d.test_id=dod.test_id) "
      + " JOIN tests_prescribed tp ON (tp.test_id = dod.test_id) "
      + " JOIN diag_outsource_master dom ON (dom.outsource_dest_id = dod.outsource_dest_id) "
      + " JOIN hospital_center_master hcm ON(hcm.center_id::text = dom.outsource_dest) "
      + "AND hcm.status='A' "
      + " WHERE dod.status='A' AND dod.source_center_id=? AND dom.outsource_dest_type = 'C' "
      + " AND tp.pat_id = ? ";

  /**
   * Gets the center out source list.
   *
   * @param centerId the center id
   * @param visitId the visit id
   * @return the center out source list
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getCenterOutSourceList(int centerId, String visitId)
      throws SQLException {

    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_CENTER_OUTSOURCE_LIST);
      ps.setInt(1, centerId);
      ps.setString(2, visitId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_OUTSOURCE_CHAIN_AGAINST_TEST. */
  private static final String GET_OUTSOURCE_CHAIN_AGAINST_TEST = " with recursive "
      + " outsource_chain(test_id, source_center_id, outsource_dest_id) as "
      + " (select dod.test_id, dod.source_center_id, dod.outsource_dest_id "
      + "from diag_outsource_detail dod "
      + " where dod.test_id = ? and dod.source_center_id = ? and dod.status = 'A' "
      + " union all "
      + " select foo.test_id, foo.source_center_id, foo.outsource_dest_id "
      + " from (select dod.test_id, dod.source_center_id, dod.outsource_dest_id "
      + " from diag_outsource_detail dod "
      + " where dod.status = 'A') as foo, outsource_chain oc, diag_outsource_master dom "
      + " where (oc.outsource_dest_id = dom.outsource_dest_id) and (oc.test_id = foo.test_id) "
      + " and (foo.source_center_id = cast (dom.outsource_dest as int)) "
      + " and (dom.outsource_dest_type = 'C')) "
      + " select string_agg(outsource_dest_id||'', ',') as outsource_dest_id "
      + " from outsource_chain group by test_id ";

  /**
   * Gets the outsource chain against test.
   *
   * @param testId the test id
   * @param sourceCenterId the source center id
   * @return the outsource chain against test
   * @throws SQLException the SQL exception
   */
  public static String getOutsourceChainAgainstTest(String testId, int sourceCenterId)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    String outSourceChain = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_OUTSOURCE_CHAIN_AGAINST_TEST);
      ps.setString(1, testId);
      ps.setInt(2, sourceCenterId);
      outSourceChain = DataBaseUtil.getStringValueFromDb(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return outSourceChain;
  }

}
