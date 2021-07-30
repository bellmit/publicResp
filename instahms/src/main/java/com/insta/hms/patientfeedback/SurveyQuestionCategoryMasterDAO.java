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
 * The Class SurveyQuestionCategoryMasterDAO.
 *
 * @author mithun.saha
 */

public class SurveyQuestionCategoryMasterDAO extends GenericDAO {
  
  /**
   * Instantiates a new survey question category master DAO.
   */
  public SurveyQuestionCategoryMasterDAO() {
    super("survey_question_category_master");
  }

  /** The survey question category fields. */
  private static String SURVEY_QUESTION_CATEGORY_FIELDS = " SELECT *  ";

  /** The survey question category count. */
  private static String SURVEY_QUESTION_CATEGORY_COUNT = " SELECT count(*) ";

  /** The survey question category tables. */
  private static String SURVEY_QUESTION_CATEGORY_TABLES = " FROM survey_question_category_master";

  /**
   * Gets the all question categories.
   *
   * @param map the map
   * @param pagingParams the paging params
   * @return the all question categories
   * @throws Exception the exception
   * @throws ParseException the parse exception
   */
  public PagedList getAllQuestionCategories(Map map, Map pagingParams) throws Exception,
      ParseException {
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();

      SearchQueryBuilder qb = new SearchQueryBuilder(con, SURVEY_QUESTION_CATEGORY_FIELDS,
          SURVEY_QUESTION_CATEGORY_COUNT, SURVEY_QUESTION_CATEGORY_TABLES, pagingParams);

      qb.addFilterFromParamMap(map);
      qb.addSecondarySort("category", false);
      qb.build();

      PagedList quesList = qb.getMappedPagedList();
      return quesList;
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /** The Constant GET_ALL_QUESTION_CATEGORIES. */
  public static final String GET_ALL_QUESTION_CATEGORIES = " SELECT "
      + " category_id,category "
      + " FROM survey_question_category_master ";

  /**
   * Gets the all question category.
   *
   * @return the all question category
   */
  public static List getAllQuestionCategory() {
    Connection con = null;
    PreparedStatement ps = null;
    ArrayList categoryList = null;

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_ALL_QUESTION_CATEGORIES);
      categoryList = DataBaseUtil.queryToArrayList(ps);

    } catch (SQLException exp) {
      Logger.log(exp);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return categoryList;
  }

}
