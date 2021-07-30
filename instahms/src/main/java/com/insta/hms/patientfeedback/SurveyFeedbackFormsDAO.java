package com.insta.hms.patientfeedback;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class SurveyFeedbackFormsDAO.
 */
public class SurveyFeedbackFormsDAO extends GenericDAO {
  
  /**
   * Instantiates a new survey feedback forms DAO.
   */
  public SurveyFeedbackFormsDAO() {
    super("survey_form");
  }

  /** The survey feedback forms fields. */
  private static String SURVEY_FEEDBACK_FORMS_FIELDS = " SELECT *  ";

  /** The survey feedback forms count. */
  private static String SURVEY_FEEDBACK_FORMS_COUNT = " SELECT count(*) ";

  /** The survey feedback forms tables. */
  private static String SURVEY_FEEDBACK_FORMS_TABLES = " FROM survey_form";

  /**
   * Gets the feedback forms.
   *
   * @param map the map
   * @param pagingParams the paging params
   * @return the feedback forms
   * @throws Exception the exception
   * @throws ParseException the parse exception
   */
  public PagedList getFeedbackForms(Map map, Map pagingParams) throws Exception, ParseException {
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();

      SearchQueryBuilder qb = new SearchQueryBuilder(con, SURVEY_FEEDBACK_FORMS_FIELDS,
          SURVEY_FEEDBACK_FORMS_COUNT, SURVEY_FEEDBACK_FORMS_TABLES, pagingParams);

      qb.addFilterFromParamMap(map);
      qb.addSecondarySort("form_name", false);
      qb.build();

      PagedList surFeeList = qb.getMappedPagedList();
      return surFeeList;
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /** The Constant GET_ALL_FEEDBACK_FORMS. */
  public static final String GET_ALL_FEEDBACK_FORMS = " SELECT form_id,form_name FROM survey_form ";

  /**
   * Gets the all feedback form details.
   *
   * @return the all feedback form details
   * @throws Exception the exception
   */
  public static List getAllFeedbackFormDetails() throws Exception {
    Connection con = null;
    PreparedStatement ps = null;
    ArrayList formList = null;

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_ALL_FEEDBACK_FORMS);
      formList = DataBaseUtil.queryToArrayList(ps);

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return formList;
  }

  /** The Constant GET_FEEDBACK_SECTION_DETAILS. */
  public static final String GET_FEEDBACK_SECTION_DETAILS = " SELECT "
      + " form_id,section_id,section_title,section_order "
      + " FROM survey_form_section WHERE form_id = ?";

  /**
   * Gets the feedback section details.
   *
   * @param formId the form id
   * @return the feedback section details
   * @throws Exception the exception
   */
  public static List getFeedbackSectionDetails(int formId) throws Exception {
    Connection con = null;
    PreparedStatement ps = null;
    ArrayList sectionList = null;

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_FEEDBACK_SECTION_DETAILS);
      ps.setInt(1, formId);
      sectionList = DataBaseUtil.queryToArrayList(ps);

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return sectionList;
  }

  /** The Constant GET_ALL_SECTION_DETAILS. */
  public static final String GET_ALL_SECTION_DETAILS = " SELECT * "
      + " from survey_form_section "
      + " where form_id = ? order by section_order";

  /**
   * Gets the all form section details.
   *
   * @param formId the form id
   * @return the all form section details
   * @throws Exception the exception
   */
  public static List<BasicDynaBean> getAllFormSectionDetails(int formId) throws Exception {
    Connection con = null;
    PreparedStatement ps = null;
    List<BasicDynaBean> sectionList = null;

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_ALL_SECTION_DETAILS);
      ps.setInt(1, formId);
      sectionList = DataBaseUtil.queryToDynaList(ps);

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return sectionList;
  }

  /** The Constant GET_ALL_QUESTION_DETAILS. */
  public static final String GET_ALL_QUESTION_DETAILS = " SELECT "
      + " scq.*,sqcm.*,srtm.rating_type_id,srtm.rating_type "
      + " from survey_section_question scq"
      + " JOIN survey_question_category_master sqcm  "
      + " ON(sqcm.category_id = scq.category_id) "
      + " LEFT JOIN survey_rating_type_master srtm "
      + " ON(srtm.rating_type_id = scq.rating_type_id)"
      + " WHERE section_id = ? order by question_order";

  /**
   * Gets the all section question details.
   *
   * @param sectionId the section id
   * @return the all section question details
   * @throws Exception the exception
   */
  public static List<BasicDynaBean> getAllSectionQuestionDetails(int sectionId) throws Exception {
    Connection con = null;
    PreparedStatement ps = null;
    List<BasicDynaBean> questionList = null;

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_ALL_QUESTION_DETAILS);
      ps.setInt(1, sectionId);
      questionList = DataBaseUtil.queryToDynaList(ps);

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return questionList;
  }

  /** The Constant GET_ALL_QUESTION_DETAILS_BY_SECTION. */
  public static final String GET_ALL_QUESTION_DETAILS_BY_SECTION = " SELECT "
      + " scq.*,sqcm.*,srtm.rating_type_id,srtm.rating_type "
      + " from survey_section_question scq"
      + " JOIN survey_question_category_master sqcm "
      + " ON(sqcm.category_id = scq.category_id) "
      + " LEFT JOIN survey_rating_type_master srtm "
      + " ON(srtm.rating_type_id = scq.rating_type_id)"
      + " WHERE section_id = ? AND scq.status='A' order by question_order";

  /**
   * Gets the all question details by section.
   *
   * @param sectionId the section id
   * @return the all question details by section
   * @throws Exception the exception
   */
  public static List<BasicDynaBean> getAllQuestionDetailsBySection(int sectionId) throws Exception {
    Connection con = null;
    PreparedStatement ps = null;
    List<BasicDynaBean> questionList = null;

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_ALL_QUESTION_DETAILS_BY_SECTION);
      ps.setInt(1, sectionId);
      questionList = DataBaseUtil.queryToDynaList(ps);

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return questionList;
  }

}
