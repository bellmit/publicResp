package com.insta.hms.patientfeedback;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Logger;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class SurveyPatientResponsesDAO.
 *
 * @author mithun.saha
 */
public class SurveyPatientResponsesDAO extends GenericDAO {

  /**
   * Instantiates a new survey patient responses DAO.
   */
  public SurveyPatientResponsesDAO() {
    super("survey_visit_feedback");
  }

  /** The survey patient responses fields. */
  private static String SURVEY_PATIENT_RESPONSES_FIELDS = " SELECT *  ";

  /** The survey patient responses count. */
  private static String SURVEY_PATIENT_RESPONSES_COUNT = " SELECT count(*) ";

  /** The survey patient responses tables. */
  private static String SURVEY_PATIENT_RESPONSES_TABLES = " FROM survey_visit_feedback svf "
      + " JOIN survey_form sf ON(svf.form_id = sf.form_id)"
      + " JOIN patient_details pd ON(pd.mr_no = svf.mr_no AND "
      + " (patient_confidentiality_check(pd.patient_group,pd.mr_no)))";

  /**
   * Gets the all survey patient responses.
   *
   * @param map          the map
   * @param pagingParams the paging params
   * @return the all survey patient responses
   * @throws Exception      the exception
   * @throws ParseException the parse exception
   */
  public PagedList getAllSurveyPatientResponses(Map map, Map pagingParams)
      throws Exception, ParseException {
    Connection con = null;

    Map modifiedMap = new HashMap();
    modifiedMap.putAll(map);
    try {
      con = DataBaseUtil.getReadOnlyConnection();

      SearchQueryBuilder qb = new SearchQueryBuilder(con, SURVEY_PATIENT_RESPONSES_FIELDS,
          SURVEY_PATIENT_RESPONSES_COUNT, SURVEY_PATIENT_RESPONSES_TABLES, pagingParams);
      String[] mrNo = (String[]) modifiedMap.get("mr_no");
      if (null != modifiedMap.get("mr_no")) {
        qb.addFilter(SearchQueryBuilder.STRING, "pd.mr_no", "ILIKE", mrNo[0].toString());
        modifiedMap.remove("mr_no");
      }
      qb.addFilterFromParamMap(modifiedMap);
      qb.addSecondarySort("svf.mr_no");
      qb.addSecondarySort("survey_response_id", false);
      qb.build();

      PagedList surPatList = qb.getMappedPagedList();
      return surPatList;
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /** The Constant GET_ALL_QUESTION_CATEGORIES. */
  public static final String GET_ALL_QUESTION_CATEGORIES = " SELECT " + " category_id,category "
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

  /** The Constant GET_SURVEY_VISIT_GENERAL_INFO. */
  private static final String GET_SURVEY_VISIT_GENERAL_INFO = "SELECT * "
      + " FROM survey_visit_feedback svf " + " JOIN patient_details pd ON (pd.mr_no = svf.mr_no "
      + " AND (patient_confidentiality_check(pd.patient_group,pd.mr_no))) "
      + " JOIN survey_form  sf ON(svf.form_id = sf.form_id) " + " WHERE survey_response_id = ? ";

  /**
   * Gets the all survey general info.
   *
   * @param surveyResponseId the survey response id
   * @return the all survey general info
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean getAllSurveyGeneralInfo(int surveyResponseId) throws SQLException {
    PreparedStatement ps = null;
    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_SURVEY_VISIT_GENERAL_INFO);
      ps.setInt(1, surveyResponseId);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_SURVEY_FORM_SECTION_DETAILS. */
  private static final String GET_SURVEY_FORM_SECTION_DETAILS = "SELECT * "
      + " FROM  survey_form_section " + " WHERE form_id = ? "
      + " ORDER BY section_order,section_id";

  /**
   * Gets the survey form section details.
   *
   * @param formId the form id
   * @return the survey form section details
   * @throws Exception the exception
   */
  public static List<BasicDynaBean> getSurveyFormSectionDetails(int formId) throws Exception {
    PreparedStatement ps = null;
    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_SURVEY_FORM_SECTION_DETAILS);
      ps.setInt(1, formId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_SURVEY_FORM__SECTION_QUESTION_DETAILS. */
  private static final String GET_SURVEY_FORM__SECTION_QUESTION_DETAILS = "SELECT * "
      + " FROM  survey_section_question " + " WHERE section_id = ? " + " ORDER BY question_order";

  /**
   * Gets the survey form section question details.
   *
   * @param sectionId the section id
   * @return the survey form section question details
   * @throws Exception the exception
   */
  public static List<BasicDynaBean> getSurveyFormSectionQuestionDetails(int sectionId)
      throws Exception {
    PreparedStatement ps = null;
    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_SURVEY_FORM__SECTION_QUESTION_DETAILS);
      ps.setInt(1, sectionId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_SURVEY_RESPONSE_DETAILS. */
  private static final String GET_SURVEY_RESPONSE_DETAILS = "SELECT * "
      + " FROM  survey_visit_feedback_details svfd "
      + " JOIN survey_section_question scq ON(svfd.question_id = scq.question_id) "
      + " JOIN survey_form_section sfc ON(scq.section_id = sfc.section_id)"
      + " LEFT JOIN survey_rating_details_master srdm ON(srdm.rating_id = svfd.rating_id) "
      + " WHERE survey_response_id = ? ORDER BY response_detail_id, scq.section_id";

  /**
   * Gets the survey response details.
   *
   * @param responseId the response id
   * @return the survey response details
   * @throws Exception the exception
   */
  public static List<BasicDynaBean> getSurveyResponseDetails(int responseId) throws Exception {
    PreparedStatement ps = null;
    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_SURVEY_RESPONSE_DETAILS);
      ps.setInt(1, responseId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The get survey form question details. */
  private static final String GET_SURVEY_FORM_QUESTION_DETAILS = "SELECT * "
      + " FROM survey_section_question " + " where section_id IN ( ";

  /**
   * Gets the survey form question details.
   *
   * @param sectionIds   the section ids
   * @param responseList the response list
   * @return the survey form question details
   * @throws Exception the exception
   */
  public List<BasicDynaBean> getSurveyFormQuestionDetails(List<BasicDynaBean> sectionIds,
      List<BasicDynaBean> responseList) throws Exception {
    PreparedStatement ps = null;
    Connection con = null;
    int index = 1;
    StringBuilder sb = new StringBuilder(GET_SURVEY_FORM_QUESTION_DETAILS);
    try {
      con = DataBaseUtil.getConnection();

      for (int i = 0; i < sectionIds.size(); i++) {
        sb.append("?");
        if (i == sectionIds.size() - 1) {
          sb.append(")");
        } else {
          sb.append(",");
        }
      }

      sb.append(" AND question_id NOT IN (");

      for (int i = 0; i < responseList.size(); i++) {
        sb.append("?");
        if (i == responseList.size() - 1) {
          sb.append(")");
        } else {
          sb.append(",");
        }
      }

      sb.append(" order by section_id,question_id");

      ps = con.prepareStatement(sb.toString());

      for (int i = 0; i < sectionIds.size(); i++) {
        ps.setInt(index++, (Integer) sectionIds.get(i).get("section_id"));
      }

      for (int i = 0; i < responseList.size(); i++) {
        ps.setInt(index++, (Integer) responseList.get(i).get("question_id"));
      }

      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

}
