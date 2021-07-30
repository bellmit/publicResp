package com.insta.hms.diagnosticsmasters.addtest;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class TestTATDAO.
 */
public class TestTATDAO extends GenericDAO {

  /**
   * Instantiates a new test TATDAO.
   *
   * @param tablename the tablename
   */
  public TestTATDAO(String tablename) {
    super(tablename);
    // TODO Auto-generated constructor stub
  }

  /** The tat fields. */
  private static String TAT_FIELDS = " SELECT center_name,center_id,outsource_name,tat_center_id,"
      + " logistics_tat_hours,conduction_start_time,processing_days,conduction_tat_hours ";
  
  /** The test tat details view. */
  private static String TEST_TAT_DETAILS_VIEW = " FROM test_tat_view ";
  
  /** The count. */
  private static String COUNT = " SELECT count(center_name) ";

  /**
   * Gets the TAT details.
   *
   * @param testId the test id
   * @param filterParams the filter params
   * @return the TAT details
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ParseException the parse exception
   */
  public PagedList getTATDetails(String testId, Map filterParams)
      throws SQLException, IOException, ParseException {
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      SearchQueryBuilder qb = null;
      qb = new SearchQueryBuilder(con, TAT_FIELDS, COUNT, TEST_TAT_DETAILS_VIEW);
      qb.addFilterFromParamMap(filterParams);

      qb.build();

      PagedList list = qb.getDynaPagedList();
      qb.close();
      return list;
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }
  
  /**
   * Gets the TAT details.
   *
   * @param offset the offset
   * @param limit the limit
   * @return the TAT details
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getTATDetails(int offset, int limit) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      int maxCentersIncDefault = (Integer) GenericPreferencesDAO.getAllPrefs()
          .get("max_centers_inc_default");
      if (maxCentersIncDefault > 1) {
        ps = con.prepareStatement(DIAG_TAT_CENTER_MASTER_FOR_MULTICENTER);
        ps.setInt(1, offset);
        ps.setInt(2, limit);
      } else {
        ps = con.prepareStatement(DIAG_TAT_CENTER_MASTER_FOR_SINGLECENTER);
      }
      ps.setFetchSize(1000);
      return DataBaseUtil.queryToDynaList(ps);

    } finally {
      DataBaseUtil.closeConnections(con, ps);

    }

  }


  /**
   * Gets the TAT hours.
   *
   * @param testId the test id
   * @return the TAT hours
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public List<BasicDynaBean> getTATHours(String testId) throws SQLException, IOException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      int maxCentersIncDefault = (Integer) GenericPreferencesDAO.getAllPrefs()
          .get("max_centers_inc_default");
      if (maxCentersIncDefault > 1) {
        ps = con.prepareStatement("SELECT distinct hcm.center_id,hcm.center_name,"
            + "om.outsource_name AS outsource_name ,dtcm.* FROM hospital_center_master hcm "
            + "LEFT JOIN  diag_tat_center_master dtcm ON (dtcm.center_id = hcm.center_id) "
            + "LEFT JOIN diag_outsource_detail dod ON dod.source_center_id = dtcm.center_id "
            + " AND dod.test_id = dtcm.test_id AND dod.status = 'A' "
            + "LEFT JOIN diag_outsource_master dom "
            + " ON dod.outsource_dest_id = dom.outsource_dest_id "
            + "LEFT JOIN outsource_names om ON dom.outsource_dest_id = om.outsource_dest_id "
            + "where hcm.status='A' and hcm.center_id != 0 and "
            + " (dtcm.test_id= ? or dtcm.test_id is null) "
            + "ORDER BY hcm.center_name");
      } else {
        ps = con.prepareStatement("SELECT distinct hcm.center_id,hcm.center_name,"
            + "om.outsource_name AS outsource_name,dtcm.* FROM hospital_center_master hcm "
            + "LEFT JOIN  diag_tat_center_master dtcm ON (dtcm.center_id = hcm.center_id) "
            + "LEFT JOIN diag_outsource_detail dod ON dod.source_center_id = dtcm.center_id "
            + " AND dod.test_id = dtcm.test_id AND dod.status = 'A' "
            + "LEFT JOIN diag_outsource_master dom "
            + " ON dod.outsource_dest_id = dom.outsource_dest_id "
            + "LEFT JOIN outsource_names om ON dom.outsource_dest_id = om.outsource_dest_id "
            + "where hcm.status='A' and (dtcm.test_id= ? or dtcm.test_id is null) "
            + "ORDER BY hcm.center_name");
      }
      ps.setString(1, testId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the TAT property.
   *
   * @param propertyName the property name
   * @param testId the test id
   * @param centerId the center id
   * @return the TAT property
   * @throws SQLException the SQL exception
   */
  public Object getTATProperty(String propertyName, String testId, int centerId)
      throws SQLException {
    Map<String, Object> filterMap = new HashMap<>();
    filterMap.put("center_id", centerId);
    filterMap.put("test_id", testId);
    Connection con = null;
    BasicDynaBean bean = null;
    try {
      con = DataBaseUtil.getConnection();
      bean = findByKey(con, new ArrayList<>(Arrays.asList(propertyName)), filterMap);
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
    if (bean != null) {
      return bean.get(propertyName);
    } else {
      return null;
    }

  }

  /**
   * Adds the test TAT centers.
   *
   * @param con the con
   * @param testId the test id
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public boolean addTestTATCenters(Connection con, String testId)
      throws SQLException, IOException {
    boolean success = true;
    List<BasicDynaBean> centerIdList = Collections.EMPTY_LIST;
    int maxCentersIncDefault = (Integer) GenericPreferencesDAO.getAllPrefs()
        .get("max_centers_inc_default");
    if (maxCentersIncDefault > 1) {
      centerIdList = CenterMasterDAO.getCentersList();
    } else {
      centerIdList = new CenterMasterDAO().listAll();
    }
    BasicDynaBean diagTatCenterBean = getBean();

    if (centerIdList != null && !centerIdList.isEmpty()) {
      for (Iterator iterator = centerIdList.iterator(); iterator.hasNext();) {
        Integer centerId = (Integer) ((BasicDynaBean) iterator.next()).get("center_id");
        diagTatCenterBean.set("tat_center_id", Integer.toString(getNextSequence()));
        diagTatCenterBean.set("test_id", testId);
        diagTatCenterBean.set("center_id", centerId);
        diagTatCenterBean.set("processing_days", "XXXXXXX");
        success &= insert(con, diagTatCenterBean);
      }
    }
    return success;

  }

  /** The get outsource dest id. */
  private static final String GET_OUTSOURCE_DEST_ID = "select dom.outsource_dest,"
      + "dom.outsource_dest_id , dom.outsource_dest_type"
      + " from diag_outsource_master dom "
      + "join diag_outsource_detail dod on (dod.outsource_dest_id =  dom.outsource_dest_id) "
      + "where test_id = ? and source_center_id = ?";

  /**
   * Gets the outsource dest ids.
   *
   * @param sourceCenterId the source center id
   * @param testId the test id
   * @return the outsource dest ids
   * @throws SQLException the SQL exception
   */
  public List<String> getOutsourceDestIds(Integer sourceCenterId, String testId)
      throws SQLException {
    List<String> list = new ArrayList<>();
    list.add(sourceCenterId.toString());
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_OUTSOURCE_DEST_ID, ResultSet.TYPE_SCROLL_SENSITIVE,
          ResultSet.CONCUR_READ_ONLY);
      ps.setString(1, testId);
      while (true) {
        ps.setInt(2, sourceCenterId);
        ResultSet rs = ps.executeQuery();
        if (rs != null && rs.next()) {
          rs.last();
          if (rs.getRow() == 1) {
            if ((Integer) rs.getInt(2) != null && rs.getString(3).charAt(0) == 'C') {
              list.add(rs.getString(1));
              sourceCenterId = rs.getInt(1);
            } else if (((Integer) rs.getInt(2) != null && rs.getString(3).charAt(0) == 'O')) {
              break;
            } else {
              list = Collections.EMPTY_LIST;
              break;
            }
          } else {
            list = Collections.EMPTY_LIST;
            break;
          }
        } else {
          break;
        }
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return list;
  }

  // {"Test Name", "Center Name","Logistics TAT","Processing Days","Conduction Start
  /** The Constant DIAG_TAT_CENTER_MASTER_FOR_MULTICENTER. */
  // Time","Conduction TAT"}
  private static final String DIAG_TAT_CENTER_MASTER_FOR_MULTICENTER = "SELECT dtcm.tat_center_id,"
      + " d.test_name,hcm.center_id,hcm.center_name,dtcm.logistics_tat_hours,"
      + "dtcm.processing_days,dtcm.conduction_start_time ,dtcm.conduction_tat_hours "
      + "FROM diag_outsource_detail dod "
      + "JOIN  diag_tat_center_master dtcm ON (dtcm.test_id = dod.test_id "
      + " AND dtcm.center_id = dod.source_center_id) "
      + "JOIN hospital_center_master hcm ON(hcm.center_id = dod.source_center_id) "
      + "JOIN diagnostics d ON(dtcm.test_id=d.test_id) "
      + "WHERE hcm.status='A' and hcm.center_id != 0 "
      + "GROUP BY dtcm.tat_center_id,d.test_name,hcm.center_id,hcm.center_name,"
      + " dtcm.logistics_tat_hours, "
      + "dtcm.processing_days,dtcm.conduction_start_time ,dtcm.conduction_tat_hours,d.test_id "
      + "order by d.test_id offset ? limit ?";
  
  /** The Constant DIAG_TAT_CENTER_MASTER_FOR_SINGLECENTER. */
  private static final String DIAG_TAT_CENTER_MASTER_FOR_SINGLECENTER = "SELECT dtcm.tat_center_id,"
      + " d.test_name,hcm.center_id,hcm.center_name,dtcm.logistics_tat_hours,dtcm.processing_days,"
      + "dtcm.conduction_start_time ,dtcm.conduction_tat_hours  FROM hospital_center_master hcm "
      + "LEFT OUTER JOIN  diag_tat_center_master dtcm ON (dtcm.center_id = hcm.center_id) "
      + "JOIN diagnostics d ON(dtcm.test_id=d.test_id) "
      + "WHERE hcm.status='A'  ORDER BY dtcm.test_id";

  /** The Constant TAT_RECORDS_FOR_MULTICENTER. */
  private static final String TAT_RECORDS_FOR_MULTICENTER = "SELECT dtcm.tat_center_id "
      + " FROM hospital_center_master hcm "
      + "LEFT OUTER JOIN  diag_tat_center_master dtcm ON (dtcm.center_id = hcm.center_id) "
      + "JOIN diagnostics d ON(dtcm.test_id=d.test_id) "
      + "WHERE hcm.status='A' and hcm.center_id != 0  and d.test_name=? and hcm.center_name=?";
  
  /** The Constant TAT_RECORDS_FOR_SINGLECENTER. */
  private static final String TAT_RECORDS_FOR_SINGLECENTER = "SELECT dtcm.tat_center_id "
      + " FROM hospital_center_master hcm "
      + "LEFT OUTER JOIN  diag_tat_center_master dtcm ON (dtcm.center_id = hcm.center_id) "
      + "JOIN diagnostics d ON(dtcm.test_id=d.test_id) "
      + "WHERE hcm.status='A' and d.test_name=? and hcm.center_name=?";

  /**
   * Test TAT exists.
   *
   * @param testName the test name
   * @param centerName the center name
   * @return the integer
   * @throws SQLException the SQL exception
   */
  public static Integer testTATExists(String testName, String centerName) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    ResultSet rs = null;
    Integer tatCenterId = null;

    try {
      int maxCentersIncDefault = (Integer) GenericPreferencesDAO.getAllPrefs()
          .get("max_centers_inc_default");
      if (maxCentersIncDefault > 1) {
        ps = con.prepareStatement(TAT_RECORDS_FOR_MULTICENTER);
      } else {
        ps = con.prepareStatement(TAT_RECORDS_FOR_SINGLECENTER);
      }
      ps.setString(1, testName);
      ps.setString(2, centerName);
      rs = ps.executeQuery();
      if (rs.next()) {
        tatCenterId = rs.getInt(1);
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return tatCenterId;
  }

  /** The Constant OUTSOURCE_NAME. */
  public static final String OUTSOURCE_NAME = "select CASE WHEN outsource_dest_type ='C' "
      + " THEN hcm.center_name "
      + "ELSE om.oh_name END AS outsource_name " + "from diag_outsource_master dom "
      + "LEFT JOIN hospital_center_master hcm ON (hcm.center_id::text = dom.outsource_dest) "
      + "join diag_outsource_detail dod on (dod.outsource_dest_id =  dom.outsource_dest_id) "
      + "LEFT JOIN outhouse_master om ON dom.outsource_dest::text = om.oh_id::text "
      + "where test_id = ? and source_center_id =? ";

  /**
   * Gets the outsuorce name.
   *
   * @param testId the test id
   * @param centerId the center id
   * @return the outsuorce name
   * @throws SQLException the SQL exception
   */
  public static String getOutsuorceName(String testId, int centerId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    ResultSet rs = null;
    String outSourceName = "";

    try {
      ps = con.prepareStatement(OUTSOURCE_NAME);
      ps.setString(1, testId);
      ps.setInt(2, centerId);
      rs = ps.executeQuery();
      if (rs.next()) {
        outSourceName = rs.getString(1);
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return outSourceName;
  }

  /** The Constant TAT_DETAIL_FOR_CENTERS. */
  public static final String TAT_DETAIL_FOR_CENTERS = "select * from diag_tat_center_master"
      + " where test_id=? and center_id in(";

  /**
   * Gets the TAT details for centers.
   *
   * @param centerList the center list
   * @param testId the test id
   * @return the TAT details for centers
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getTATDetailsForCenters(List<String> centerList, String testId)
      throws SQLException {
    int index = 1;
    StringBuilder query = new StringBuilder(TAT_DETAIL_FOR_CENTERS);
    query.append(centerList.toString().replace("[", "").replace("]", ""));
    query.append(")");
    query.append(" ORDER BY ");
    for (int i = 0; i < centerList.size(); i++) {
      query.append(" center_id = ? DESC ");
      if (i != centerList.size() - 1) {
        query.append(",");
      }
    }
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();

      ps = con.prepareStatement(query.toString());
      ps.setString(index++, testId);
      for (int i = 0; i < centerList.size(); i++) {
        ps.setInt(index++, Integer.parseInt(centerList.get(i)));
      }
      return DataBaseUtil.queryToDynaList(ps);

    } finally {
      DataBaseUtil.closeConnections(con, ps);

    }

  }

  /** The Constant TAT_DETAIL_COUNT_ACTIVE_CENTERS. */
  public static final String TAT_DETAIL_COUNT_ACTIVE_CENTERS = "select count(dtcm.center_id)"
      + "  FROM diag_outsource_detail dod "
      + "JOIN  diag_tat_center_master dtcm ON (dtcm.test_id = dod.test_id"
      + "  AND dtcm.center_id = dod.source_center_id) "
      + "JOIN hospital_center_master hcm ON (hcm.center_id = dod.source_center_id) "
      + "WHERE hcm.status='A' and hcm.center_id != 0";

  /**
   * Gets the TAT details count.
   *
   * @return the TAT details count
   * @throws SQLException the SQL exception
   */
  public static int getTATDetailsCount() throws SQLException {
    StringBuilder query = new StringBuilder(TAT_DETAIL_COUNT_ACTIVE_CENTERS);
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(query.toString());
      ResultSet rs = ps.executeQuery();
      rs.next();
      return rs.getInt(1);

    } finally {
      DataBaseUtil.closeConnections(con, ps);

    }

  }

  /** The Constant GET_TAT_DETAILS_CHAIN. */
  public static final String GET_TAT_DETAILS_CHAIN = " select dtcm.test_id,dtcm.center_id "
      + " as source_center_id,dtcm.center_id as outsource_dest_id, "
      + " cast(center_id as text),conduction_tat_hours,logistics_tat_hours,"
      + "  conduction_start_time, processing_days "
      + " FROM diag_tat_center_master dtcm "
      + " LEFT JOIN  diag_outsource_detail dod ON (dod.test_id = dtcm.test_id "
      + " AND dod.source_center_id = dtcm.center_id) "
      + " where dtcm.test_id = ? and dtcm.center_id = ? and coalesce(dod.status, 'A') = 'A' "
      + " union all "
      + " select test_id,source_center_id, outsource_dest_id,outsource_dest,conduction_tat_hours,"
      + " logistics_tat_hours, "
      + " conduction_start_time, processing_days " + " from (with recursive "
      + " outsource_chain(test_id,source_center_id, outsource_dest_id, "
      + " outsource_dest,conduction_tat_hours,logistics_tat_hours,conduction_start_time,"
      + "  processing_days) as "
      + " (select dod.test_id,source_center_id,dod.outsource_dest_id,outsource_dest,"
      + " conduction_tat_hours, "
      + " logistics_tat_hours,conduction_start_time, processing_days "
      + " from diag_outsource_detail dod "
      + " join diag_outsource_master dom on (dom.outsource_dest_id = dod.outsource_dest_id) "
      + " join diag_tat_center_master dtcm on (cast (dom.outsource_dest as int) = dtcm.center_id "
      + " and dod.test_id = dtcm.test_id) and (dom.outsource_dest_type = 'C') "
      + " where dod.test_id = ? and dod.source_center_id = ? and dod.status = 'A' "
      + "   union all "
      + " select foo.test_id, foo.source_center_id, foo.outsource_dest_id,foo.outsource_dest,"
      + " foo.conduction_tat_hours, "
      + " foo.logistics_tat_hours,foo.conduction_start_time, foo.processing_days "
      + " from (select dod.test_id,source_center_id, dod.outsource_dest_id,outsource_dest,"
      + " conduction_tat_hours, "
      + " logistics_tat_hours,conduction_start_time, processing_days "
      + " from diag_outsource_detail dod "
      + " join diag_outsource_master dom on (dom.outsource_dest_id = dod.outsource_dest_id) "
      + " join diag_tat_center_master dtcm on (cast (dom.outsource_dest as int) = dtcm.center_id "
      + " and dod.test_id = dtcm.test_id) "
      + " and (dom.outsource_dest_type = 'C') and (dod.status = 'A')) as foo, "
      + " outsource_chain oc, diag_outsource_master dom1 "
      + " where (oc.outsource_dest_id = dom1.outsource_dest_id) and (oc.test_id = foo.test_id) "
      + " and (foo.source_center_id = cast (oc.outsource_dest as int)) "
      + " and (dom1.outsource_dest_type = 'C')) "
      + " select * from outsource_chain oc1) as foo1 ";

  /**
   * Gets the TAT details chain.
   *
   * @param testId the test id
   * @param centerId the center id
   * @return the TAT details chain
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getTATDetailsChain(String testId, int centerId)
      throws SQLException {
    StringBuilder query = new StringBuilder(GET_TAT_DETAILS_CHAIN);
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();

      ps = con.prepareStatement(query.toString());
      ps.setString(1, testId);
      ps.setInt(2, centerId);
      ps.setString(3, testId);
      ps.setInt(4, centerId);
      return DataBaseUtil.queryToDynaList(ps);

    } finally {
      DataBaseUtil.closeConnections(con, ps);

    }

  }

}
