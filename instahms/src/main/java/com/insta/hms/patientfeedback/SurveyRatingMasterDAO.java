package com.insta.hms.patientfeedback;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Logger;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class SurveyRatingMasterDAO.
 *
 * @author mithun.saha
 */

public class SurveyRatingMasterDAO extends GenericDAO {
  
  /**
   * Instantiates a new survey rating master DAO.
   */
  public SurveyRatingMasterDAO() {
    super("survey_rating_type_master");
  }

  /** The survey rating fields. */
  private static String SURVEY_RATING_FIELDS = " SELECT *  ";

  /** The survey rating count. */
  private static String SURVEY_RATING_COUNT = " SELECT count(*) ";

  /** The survey rating tables. */
  private static String SURVEY_RATING_TABLES = " FROM survey_rating_type_master";

  /**
   * Gets the ratings.
   *
   * @param map the map
   * @param pagingParams the paging params
   * @return the ratings
   * @throws Exception the exception
   * @throws ParseException the parse exception
   */
  public PagedList getRatings(Map map, Map pagingParams) throws Exception, ParseException {
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();

      SearchQueryBuilder qb = new SearchQueryBuilder(con, SURVEY_RATING_FIELDS,
          SURVEY_RATING_COUNT, SURVEY_RATING_TABLES, pagingParams);

      qb.addFilterFromParamMap(map);
      qb.addSecondarySort("rating_type", false);
      qb.build();

      PagedList surRatList = qb.getMappedPagedList();
      return surRatList;
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /** The Constant GET_ALL_SURVEY_RATING. */
  public static final String GET_ALL_SURVEY_RATING = " SELECT "
      + " rating_type_id,rating_type "
      + " FROM survey_rating_type_master ";

  /**
   * Gets the all ratings.
   *
   * @return the all ratings
   */
  public static List getAllRatings() {
    Connection con = null;
    PreparedStatement ps = null;
    ArrayList ratingList = null;

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_ALL_SURVEY_RATING);
      ratingList = DataBaseUtil.queryToArrayList(ps);

    } catch (SQLException exp) {
      Logger.log(exp);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return ratingList;
  }

  /** The Constant GET_ALL_RATING_DETAILS. */
  public static final String GET_ALL_RATING_DETAILS = " SELECT * "
      + " FROM survey_rating_details_master "
      + " where rating_type_id = ? ";

  /**
   * Gets the all rating details.
   *
   * @param ratingTypeId the rating type id
   * @return the all rating details
   * @throws SQLException the SQL exception
   */
  public static List getAllRatingDetails(int ratingTypeId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_ALL_RATING_DETAILS);
      ps.setInt(1, ratingTypeId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

}
