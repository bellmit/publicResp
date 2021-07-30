package com.insta.hms.addtohomepagemaster;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.ImpersonationAction;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * The Class AddToHomePageMasterDAO.
 */
public class AddToHomePageMasterDAO {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(AddToHomePageMasterDAO.class);

  /**
   * Gets the all home page tabs.
   *
   * @param username
   *          the username
   * @return the all home page tabs
   */
  // To get all homePage Header Tabs.
  public static List<BasicDynaBean> getAllHomePageTabs(String username) {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con
          .prepareStatement("SELECT * FROM user_home_screens "
              + " WHERE user_id=? ORDER BY home_screen_id ASC");
      ps.setString(1, username);
      return DataBaseUtil.queryToDynaList(ps);
    } catch (SQLException sq) {
      logger.error("Sql Exception while fetching Header Tabs List", sq);
    } catch (Exception exception) {
      logger.error("Exception while fetching Header Tabs List", exception);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return null;
  }

  /**
   * Save home page tab.
   *
   * @param screenName
   *          the screen name
   * @param userid
   *          the userid
   * @param searchId
   *          the search id
   */
  // To save the saved search for fav screen.
  public static void saveHomePageTab(String screenName, String userid, int searchId) {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con
          .prepareStatement("insert into user_home_screens "
              + " (screen_name,user_id,search_id) values (?,?,?)");
      ps.setString(1, screenName);
      ps.setString(2, userid);
      ps.setInt(3, searchId);
      ps.execute();
      logger.info("Sucessfully saved the tab screen_name: " + screenName);

    } catch (SQLException sq) {
      logger.error("Sql Exception while saving the header Tab ", sq);
    } catch (Exception exception) {
      logger.error("Exception while saving Header Tab ", exception);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Save home page tab.
   *
   * @param screenName
   *          the screen name
   * @param userid
   *          the userid
   * @param actionId
   *          the action id
   * @param query
   *          the query
   */
  // To save the worklist , fav reports and reports for fav screen.
  public static void saveHomePageTab(String screenName, String userid, String actionId,
      String query) {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con
          .prepareStatement("insert into user_home_screens "
              + " (screen_name,user_id,action_id,query_params) values (?,?,?,?)");
      ps.setString(1, screenName);
      ps.setString(2, userid);
      ps.setString(3, actionId);
      ps.setString(4, query);
      ps.execute();
      logger.info("Sucessfully saved the tab screen_name: " + screenName);

    } catch (SQLException sq) {
      logger.error("Sql Exception while saving the header Tab ", sq);
    } catch (Exception exception) {
      logger.error("Exception while saving Header Tab ", exception);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Save fav reports home page tab.
   *
   * @param screenName
   *          the screen name
   * @param userid
   *          the userid
   * @param reportId
   *          the report id
   */
  // To save the fav reports for fav screen.
  public static void saveFavReportsHomePageTab(String screenName, String userid, int reportId) {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con
          .prepareStatement("insert into user_home_screens "
              + " (screen_name,user_id,report_id) values (?,?,?)");
      ps.setString(1, screenName);
      ps.setString(2, userid);
      ps.setInt(3, reportId);
      ps.execute();
      logger.info("Sucessfully saved the tab screen_name: " + screenName);

    } catch (SQLException sq) {
      logger.error("Sql Exception while saving the header Tab ", sq);
    } catch (Exception exception) {
      logger.error("Exception while saving Header Tab ", exception);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Delete tab.
   *
   * @param homeScreenId
   *          the home screen id
   */
  // To delete the homePage Tabs.
  public static void deleteTab(Integer homeScreenId) {
    Connection con = null;
    PreparedStatement ps = null;

    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement("delete from user_home_screens where home_screen_id = ?");
      ps.setInt(1, homeScreenId);
      ps.execute();
      logger.info("Sucessfully deleted the tab home_screen_id:" + homeScreenId);

    } catch (SQLException sq) {
      logger.error("Sql Exception while deleting Header Tab home_screen_id:" + homeScreenId, sq);
    } catch (Exception exception) {
      logger.error("Exception while deleting Header Tab home_screen_id:" 
            + homeScreenId, exception);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }
}
