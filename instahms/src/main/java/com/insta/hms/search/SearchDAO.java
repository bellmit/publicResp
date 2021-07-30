package com.insta.hms.search;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.ImpersonationAction;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

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
 * The Class SearchDAO.
 *
 * @author krishna.t
 */
public class SearchDAO extends GenericDAO {

  private static Logger logger = LoggerFactory
      .getLogger(ImpersonationAction.class);
  private static final String table = "search_parameters";

  /**
   * Instantiates a new search DAO.
   */
  public SearchDAO() {
    super(table);
  }

  private static final String SEARCH_EXISTS = "SELECT search_name FROM " + table
      + " WHERE user_name=? AND action_id=? AND UPPER(search_name)=UPPER(?) AND search_id !=?";

  /**
   * Search exsits.
   *
   * @param con the con
   * @param username the username
   * @param actionId the action id
   * @param searchName the search name
   * @param searchId the search id
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean searchExsits(Connection con, String username, String actionId, String searchName,
      Integer searchId) throws SQLException {
    PreparedStatement ps = null;
    ResultSet rs = null;

    try {
      ps = con.prepareStatement(SEARCH_EXISTS);
      ps.setString(1, username);
      ps.setString(2, actionId);
      ps.setString(3, searchName);
      ps.setInt(4, searchId);
      rs = ps.executeQuery();
      if (rs.next()) {
        return true;
      }
    } finally {
      DataBaseUtil.closeConnections(null, ps, rs);
    }
    return false;
  }

  private static String SEARCH_FIELDS = " SELECT search_name, user_name, screen_name, search_id  ";

  private static String SEARCH_COUNT = " SELECT count(*) ";

  private static String SEARCH_TABLES = " FROM search_parameters ";

  /**
   * Gets the saved searches.
   *
   * @param map the map
   * @param pagingParams the paging params
   * @return the saved searches
   * @throws ParseException the parse exception
   * @throws SQLException the SQL exception
   */
  public PagedList getSavedSearches(Map map, Map pagingParams) throws ParseException, SQLException {
    Connection con = DataBaseUtil.getReadOnlyConnection();
    PagedList list = null;
    try {
      SearchQueryBuilder qb = new SearchQueryBuilder(con, SEARCH_FIELDS, SEARCH_COUNT,
          SEARCH_TABLES, pagingParams);

      qb.addFilterFromParamMap(map);
      qb.addSecondarySort("search_id", false);
      qb.build();

      list = qb.getMappedPagedList();
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
    return list;
  }

  private static final String MYSEARCHES = "SELECT * FROM " + table + " WHERE action_id=?";

  /**
   * Gets the my searches.
   *
   * @param actionId the action id
   * @return the my searches
   * @throws SQLException the SQL exception
   */
  public static List getMySearches(String actionId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(MYSEARCHES);
      ps.setString(1, actionId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  // Returns the user all saved search based on user_id.
  private static final String USERSAVEDSEARCH = "SELECT * FROM " + table + " WHERE user_name=?";

  /**
   * Gets the user saved search.
   *
   * @param userId the user id
   * @return the user saved search
   * @throws SQLException the SQL exception
   */
  public static List getUserSavedSearch(String userId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(USERSAVEDSEARCH);
      ps.setString(1, userId);
      return DataBaseUtil.queryToDynaList(ps);
    } catch (SQLException se) {
      logger.error("Sql Exception occured while fetching saved search using user_id", se);
    } catch (Exception exception) {
      logger.error("Error while fetching saved search using user_id", exception);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return null;
  }

}
