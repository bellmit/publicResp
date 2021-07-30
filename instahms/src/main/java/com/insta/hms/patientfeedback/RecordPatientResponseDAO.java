package com.insta.hms.patientfeedback;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class RecordPatientResponseDAO.
 *
 * @author mithun.saha
 */
public class RecordPatientResponseDAO extends GenericDAO {
  
  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(RecordPatientResponseDAO.class);

  /**
   * Instantiates a new record patient response DAO.
   */
  public RecordPatientResponseDAO() {
    super("survey_form");
  }

  /** The Constant GET_ALL_ACTIVE_SECTION_DETAILS. */
  private static final String GET_ALL_ACTIVE_SECTION_DETAILS = "SELECT * "
      + " FROM survey_form_section "
      + " WHERE status = 'A' AND form_id = ? "
      + " order by section_order";

  /**
   * Gets the all active section details.
   *
   * @param formId the form id
   * @return the all active section details
   * @throws Exception the exception
   */
  public List<BasicDynaBean> getAllActiveSectionDetails(int formId) throws Exception {
    Connection con = null;
    PreparedStatement ps = null;
    List<BasicDynaBean> sectionDetails = null;

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_ALL_ACTIVE_SECTION_DETAILS);
      ps.setInt(1, formId);
      sectionDetails = DataBaseUtil.queryToDynaList(ps);

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return sectionDetails;
  }

  /** The Constant GET_ALL_ACTIVE_QUESTION_DETAILS. */
  private static final String GET_ALL_ACTIVE_QUESTION_DETAILS = "SELECT *  "
      + " FROM survey_form_section sfs "
      + " JOIN  survey_section_question ssq ON(ssq.section_id=sfs.section_id)"
      + " WHERE form_id = ? AND ssq.status = 'A' "
      + " AND sfs.status = 'A'  order by ssq.section_id,question_order";

  /**
   * Gets the all active survey question details.
   *
   * @param formId the form id
   * @return the all active survey question details
   * @throws Exception the exception
   */
  public static List<BasicDynaBean> getAllActiveSurveyQuestionDetails(int formId) throws Exception {
    Connection con = null;
    PreparedStatement ps = null;
    List<BasicDynaBean> questionDetails = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_ALL_ACTIVE_QUESTION_DETAILS);
      ps.setInt(1, formId);
      questionDetails = DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return questionDetails;
  }
}
