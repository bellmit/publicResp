package com.insta.hms.common;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.ImpersonationAction;
import com.insta.hms.common.ConversionUtils.LISTING;

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
 * The Class FavouriteReportDAO.
 */
public class FavouriteReportDAO extends GenericDAO {

  /** The Constant table. */
  private static final String table = "favourite_reports";

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(ImpersonationAction.class);

  /**
   * Instantiates a new favourite report DAO.
   */
  public FavouriteReportDAO() {
    super(table);
  }

  /** The Constant UPDATE_FREQUENT_VIEW_STATUS. */
  private static final String UPDATE_FREQUENT_VIEW_STATUS = " UPDATE favourite_reports  SET "
      + " frequently_viewed= CASE WHEN frequently_viewed='N' THEN 'Y' ELSE 'N' END "
      + " WHERE report_title = ? ";

  /**
   * Update freq status.
   *
   * @param repId the rep id
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean updateFreqStatus(String repId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    int resultCount;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(UPDATE_FREQUENT_VIEW_STATUS);
      ps.setString(1, repId);
      resultCount = ps.executeUpdate();
    } finally {
      DataBaseUtil.closeConnections(con, ps, null);
    }
    return resultCount == 1;
  }

  /** The Constant REPORT_IDS_NAMES. */
  private static final String REPORT_IDS_NAMES = "SELECT report_id, report_title AS report_name "
      + " FROM " + table;

  /**
   * Gets the report ids names.
   *
   * @return the report ids names
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getReportIdsNames() throws SQLException {
    return DataBaseUtil.queryToDynaList(REPORT_IDS_NAMES);
  }

  /** The Constant REPORT_EXISTS. */
  private static final String REPORT_EXISTS = "SELECT trim(report_title) AS report_title FROM "
      + table + " WHERE UPPER(TRIM(report_title)) = UPPER(TRIM(?))";

  /**
   * Report exists.
   *
   * @param con         the con
   * @param username    the username
   * @param actionId    the action id
   * @param reportTitle the report title
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public static boolean reportExists(Connection con, String username, String actionId,
      String reportTitle) throws SQLException {
    PreparedStatement ps = null;
    ResultSet rs = null;

    try {
      ps = con.prepareStatement(REPORT_EXISTS);
      ps.setString(1, reportTitle.trim());
      rs = ps.executeQuery();
      if (rs.next()) {
        return true;
      }
    } finally {
      DataBaseUtil.closeConnections(null, ps, rs);
    }
    return false;
  }

  /**
   * Report exists.
   *
   * @param reportTitle the report title
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public static boolean reportExists(String reportTitle) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    ResultSet rs = null;

    try {
      ps = con.prepareStatement(REPORT_EXISTS);
      ps.setString(1, reportTitle.trim());
      rs = ps.executeQuery();
      if (rs.next()) {
        return true;
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps, rs);
    }
    return false;
  }

  /** The Constant MYFAVOURITES. */
  private static final String MYFAVOURITES = "SELECT * FROM " + table
      + " WHERE  action_id=? ORDER BY report_title";

  /**
   * Gets the my favourites.
   *
   * @param actionId the action id
   * @return the my favourites
   * @throws SQLException the SQL exception
   */
  public static List getMyFavourites(String actionId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(MYFAVOURITES);
      ps.setString(1, actionId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant CUSTOM_SRXML. */
  private static final String CUSTOM_SRXML = "SELECT custom_report_name FROM " + table
      + " WHERE report_title=? ORDER BY report_title";

  /**
   * Gets the custom srxml.
   *
   * @param reportTitle the report title
   * @return the custom srxml
   * @throws SQLException the SQL exception
   */
  public static String getCustomSrxml(String reportTitle) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    ResultSet rs = null;
    String cstmName;
    try {
      ps = con.prepareStatement(CUSTOM_SRXML);
      ps.setString(1, reportTitle);
      rs = ps.executeQuery();
      if (rs.next()) {
        cstmName = (String) rs.getObject("custom_report_name");
      } else {
        cstmName = null;
      }
      return cstmName;

    } finally {
      DataBaseUtil.closeConnections(con, ps, rs);
    }
  }

  /** The Constant REPORT_SRXML. */
  private static final String REPORT_SRXML = "SELECT report_desc_srxml FROM " + table
      + " WHERE report_title=?";

  /**
   * Gets the report srxml.
   *
   * @param reportTitle the report title
   * @return the report srxml
   * @throws SQLException the SQL exception
   */
  public static String getReportSrxml(String reportTitle) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    ResultSet rs = null;
    String rptName;
    try {
      ps = con.prepareStatement(REPORT_SRXML);
      ps.setString(1, reportTitle);
      rs = ps.executeQuery();
      if (rs.next()) {
        rptName = rs.getString("report_desc_srxml");
      } else {
        rptName = null;
      }
      return rptName;
    } finally {
      DataBaseUtil.closeConnections(con, ps, rs);
    }
  }

  /** The Constant MYCUSTOMFAVOURITES. */
  private static final String MYCUSTOMFAVOURITES = "SELECT * FROM " + table
      + " WHERE  action_id=? AND custom_report_name=? ";

  /**
   * Gets the my custom favourites.
   *
   * @param actionId the action id
   * @param repName  the rep name
   * @return the my custom favourites
   * @throws SQLException the SQL exception
   */
  public static List getMyCustomFavourites(String actionId, String repName) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(MYCUSTOMFAVOURITES);
      ps.setString(1, actionId);
      ps.setString(2, repName);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant SEARCH_FIELDS. */
  private static final String SEARCH_FIELDS = " SELECT report_id, report_title, created_date, "
      + " fav.action_id, query_params, parent_report_name, custom_report_name, user_name, "
      + " report_group, frequently_viewed";

  /** The Constant SEARCH_COUNT. */
  private static final String SEARCH_COUNT = " SELECT COUNT(*) ";

  /** The Constant SEARCH_TABLES. */
  private static final String SEARCH_TABLES = " FROM favourite_reports fav";

  /** The Constant EXT_SEARCH_TABLES. */
  private static final String EXT_SEARCH_TABLES = " FROM (SELECT fr.*, role_id "
      + " FROM favourite_reports fr   " + " JOIN (  "
      + "          SELECT * FROM (   SELECT screen_id as action_id, rights, role_id FROM   (  "
      + "          SELECT screen_id, rights, role_id FROM screen_rights   UNION  "
      + "          SELECT action, rights, role_id  FROM action_rights   UNION  "
      + "          SELECT action_id, rights, role_id FROM url_action_rights   ) AS foo  "
      + "          WHERE rights = 'A' ORDER BY screen_id   ) AS foo2  "
      + "  ) AS foo3 ON (fr.action_id = foo3.action_id ) " + " UNION  "
      + " SELECT fr.*, role_id FROM favourite_reports  fr  "
      + " JOIN favourite_report_rights ON (report_id = favourite_report_id) "
      + " WHERE rights = 'A'  " + " ORDER BY role_id ) AS fav ";

  /** The Constant EXT_SEARCH_GROUP_BY. */
  private static final String EXT_SEARCH_GROUP_BY = " report_id, report_title, "
      + " created_date, fav.action_id, "
      + " query_params, parent_report_name, custom_report_name,  "
      + " user_name ,report_group, frequently_viewed, role_id ";

  /**
   * Gets the fav reports.
   *
   * @param filter  the filter
   * @param listing the listing
   * @param roleId  the role id
   * @return the fav reports
   * @throws SQLException   the SQL exception
   * @throws ParseException the parse exception
   */
  public static PagedList getFavReports(Map filter, Map listing, int roleId)
      throws SQLException, ParseException {
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      SearchQueryBuilder qb = new SearchQueryBuilder(con,
          roleId > 2 ? SEARCH_FIELDS + " , role_id "
              : SEARCH_FIELDS + " , " + roleId + " AS role_id ",
          SEARCH_COUNT, roleId > 2 ? EXT_SEARCH_TABLES : SEARCH_TABLES, null, EXT_SEARCH_GROUP_BY,
          (String) listing.get(LISTING.SORTCOL), (Boolean) listing.get(LISTING.SORTASC),
          (Integer) listing.get(LISTING.PAGESIZE), (Integer) listing.get(LISTING.PAGENUM));
      if (roleId > 2) {
        qb.addFilter(qb.INTEGER, "role_id", "=", roleId);
      }
      qb.addFilterFromParamMap(filter);
      qb.addSecondarySort("report_id", true);
      qb.build();
      PagedList list = qb.getMappedPagedList();
      qb.close();
      return list;
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /** The Constant COUNT_QUERY. */
  private static final String COUNT_QUERY = "SELECT COUNT(*) FROM " + table + "  ";

  /**
   * Gets the count.
   *
   * @return the count
   * @throws SQLException the SQL exception
   */
  public static int getCount() throws SQLException {
    Connection con = DataBaseUtil.getReadOnlyConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(COUNT_QUERY);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        return rs.getInt("COUNT");
      } else {
        return 0;
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the fav reports list.
   *
   * @param filter the filter
   * @return the fav reports list
   * @throws SQLException the SQL exception
   */
  public static List getFavReportsList(Map filter) throws SQLException {

    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(SEARCH_FIELDS + " " + SEARCH_TABLES);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant ALL_REPORT_TYPES. */
  private static final String ALL_REPORT_TYPES = "SELECT DISTINCT(parent_report_name) "
      + " FROM favourite_reports ";

  /**
   * Gets the all fav report types.
   *
   * @return the all fav report types
   * @throws SQLException the SQL exception
   */
  public static List getAllFavReportTypes() throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(ALL_REPORT_TYPES);
      return DataBaseUtil.queryToStringList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }

  }

  /** The Constant ALL_REPORT_CREATORS. */
  private static final String ALL_REPORT_CREATORS = "SELECT DISTINCT(user_name) "
      + " FROM favourite_reports ORDER BY user_name";

  /**
   * Gets the all fav report creators.
   *
   * @return the all fav report creators
   * @throws SQLException the SQL exception
   */
  public static List getAllFavReportCreators() throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(ALL_REPORT_CREATORS);
      return DataBaseUtil.queryToStringList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }

  }

  /** The Constant ALL_REPORT_GROUPS. */
  private static final String ALL_REPORT_GROUPS = "SELECT DISTINCT(report_group) "
      + " FROM favourite_reports ORDER BY report_group";

  /**
   * Gets the all fav report groups.
   *
   * @return the all fav report groups
   * @throws SQLException the SQL exception
   */
  public static List getAllFavReportGroups() throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(ALL_REPORT_GROUPS);
      return DataBaseUtil.queryToStringList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant ALL_REPORT_TITLES. */
  private static final String ALL_REPORT_TITLES = "SELECT report_title, user_name "
      + " FROM favourite_reports ORDER BY report_title";

  /**
   * Gets the all fav report titles.
   *
   * @return the all fav report titles
   * @throws SQLException the SQL exception
   */
  public static List getAllFavReportTitles() throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(ALL_REPORT_TITLES);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_FAVOURITE_REPORT_RIGHTS. */
  private static final String GET_FAVOURITE_REPORT_RIGHTS = " SELECT rights "
      + " FROM favourite_report_rights WHERE role_id = ? AND favourite_report_id = ? ";

  /**
   * Gets the favourite report right.
   *
   * @param roleId   the role id
   * @param reportId the report id
   * @return the favourite report right
   * @throws SQLException the SQL exception
   */
  public static boolean getFavouriteReportRight(int roleId, int reportId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_FAVOURITE_REPORT_RIGHTS);
      ps.setInt(1, roleId);
      ps.setInt(2, reportId);
      String rights = DataBaseUtil.getStringValueFromDb(ps);
      boolean hasRights = rights != null && !rights.equalsIgnoreCase("N");
      return hasRights;
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }

  }

  /** The Constant GET_ROLES_WITH_RIGHTS_FOR_REPORT. */
  private static final String GET_ROLES_WITH_RIGHTS_FOR_REPORT = " SELECT f.role_id "
      + " FROM favourite_report_rights f" + " JOIN u_role u ON (u.role_id = f.role_id) "
      + " WHERE favourite_report_id = ? ";

  /**
   * Gets the roles with rights.
   *
   * @param reportId the report id
   * @return the roles with rights
   * @throws SQLException the SQL exception
   */
  public static List getRolesWithRights(int reportId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_ROLES_WITH_RIGHTS_FOR_REPORT);
      ps.setInt(1, reportId);
      return DataBaseUtil.queryToArrayList1(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }

  }

  /** The Constant GET_FAVOUROTE_REPORTS. */
  private static final String GET_FAVOUROTE_REPORTS = "SELECT * FROM " + table
      + " WHERE user_name=?";

  /**
   * Gets the user fav reports.
   *
   * @param userId the user id
   * @return the user fav reports
   * @throws SQLException the SQL exception
   */
  public static List getUserFavReports(String userId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(GET_FAVOUROTE_REPORTS);
      ps.setString(1, userId);
      return DataBaseUtil.queryToDynaList(ps);
    } catch (SQLException se) {
      logger.error("Sql Exception occured while fetching fav reports using user_id", se);
    } catch (Exception exception) {
      logger.error("Error while fetching  fav reports using user_id", exception);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return null;
  }
}
