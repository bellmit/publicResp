package com.insta.hms.progress;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class PatientProgressDAO.
 */
public class PatientProgressDAO {

  /** The Constant ALL_PROGRESS_LIST. */
  public static final String ALL_PROGRESS_LIST = "SELECT d.doctor_name,* FROM progress_notes pn "
      + " JOIN doctors d ON(d.doctor_id = pn.doctor)" + " WHERE mr_no = ? ORDER BY date_time DESC";

  /** The Constant PATIENT_PROGRESS_LIST. */
  public static final String PATIENT_PROGRESS_LIST = "SELECT d.doctor_name,"
      + " * FROM progress_notes pn"
      + " JOIN doctors d ON(d.doctor_id = pn.doctor)"
      + " WHERE mr_no = ? AND (visit_id = '' OR visit_id is null) ORDER BY date_time DESC";

  /** The Constant VISIT_PROGRESS_LIST. */
  public static final String VISIT_PROGRESS_LIST = "SELECT d.doctor_name,* FROM progress_notes pn"
      + " JOIN doctors d ON(d.doctor_id = pn.doctor)"
      + " WHERE visit_id = ? ORDER BY date_time DESC";

  /**
   * Gets the progress notes list.
   *
   * @param mrNo
   *          the mr no
   * @param filterType
   *          the filter type
   * @param visitId
   *          the visit id
   * @return the progress notes list
   * @throws SQLException
   *           the SQL exception
   */
  public static List<BasicDynaBean> getProgressNotesList(String mrNo, String filterType,
      String visitId) throws SQLException {

    Connection con = null;
    PreparedStatement pstmt = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      if (filterType == null || filterType.equals("all")) {
        pstmt = con.prepareStatement(ALL_PROGRESS_LIST);
        pstmt.setString(1, mrNo);
      } else if (filterType.equals("patient")) {
        pstmt = con.prepareStatement(PATIENT_PROGRESS_LIST);
        pstmt.setString(1, mrNo);
      } else {
        pstmt = con.prepareStatement(VISIT_PROGRESS_LIST);
        pstmt.setString(1, visitId);
      }
      return DataBaseUtil.queryToDynaList(pstmt);
    } finally {
      DataBaseUtil.closeConnections(con, pstmt);
    }

  }

  /** The progress list for emr. */
  private static final String PROGRESS_LIST_FOR_EMR = "SELECT MAX(date_time) as date_time, "
      + " pgn.visit_id, pgn.mr_no,"
      + " (SELECT username from progress_notes pn WHERE"
      + " pn.date_time=MAX(pgn.date_time) and pgn.mr_no=pn.mr_no and "
      + " pgn.visit_id=pn.visit_id LIMIT 1)AS username,pr.reg_date"
      + " FROM progress_notes pgn "
      + " LEFT JOIN patient_registration pr ON(pgn.visit_id = pr.patient_id)"
      + " WHERE pgn.mr_no=? ";

  /**
   * Gets the progress nts list for emr.
   *
   * @param visitId
   *          the visit id
   * @param mrNO
   *          the mr NO
   * @return the progress nts list for emr
   * @throws SQLException
   *           the SQL exception
   */
  public List<BasicDynaBean> getProgressNtsListForEmr(String visitId, String mrNO)
      throws SQLException {

    if (visitId != null) {
      return DataBaseUtil.queryToDynaList(PROGRESS_LIST_FOR_EMR
          + " AND COALESCE(visit_id, '')!=''GROUP BY pgn.mr_no, visit_id, pr.reg_date", mrNO);
    } else {
      return DataBaseUtil.queryToDynaList(PROGRESS_LIST_FOR_EMR
          + " AND COALESCE(visit_id, '')=''GROUP BY pgn.mr_no, visit_id, pr.reg_date", mrNO);
    }  
  }

  /** The Constant PROGRESS_NOTES. */
  private static final String PROGRESS_NOTES = "SELECT d.doctor_name, * FROM progress_notes pn"
      + " LEFT JOIN doctors d ON(d.doctor_id = pn.doctor) "
      + " WHERE mr_no = ? AND visit_id = ? ORDER BY date_time";

  /**
   * Gets the progress notes for EMR.
   *
   * @param visitID
   *          the visit ID
   * @param mrNO
   *          the mr NO
   * @return the progress notes for EMR
   * @throws SQLException
   *           the SQL exception
   */
  public static List<BasicDynaBean> getProgressNotesForEMR(String visitID, String mrNO)
      throws SQLException {
    Connection con = null;
    PreparedStatement pstmt = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      pstmt = con.prepareStatement(PROGRESS_NOTES);
      pstmt.setString(1, mrNO);
      pstmt.setString(2, visitID);
      return DataBaseUtil.queryToDynaList(pstmt);
    } finally {
      DataBaseUtil.closeConnections(con, pstmt);
    }
  }

  /** The Constant VISITS_AND_DOCTORS. */
  private static final String VISITS_AND_DOCTORS = " SELECT patient_id, reg_date, status "
      + " FROM patient_registration "
      + " WHERE mr_no=? ORDER BY reg_date,reg_time DESC";

  /**
   * Gets the all visits and doctors.
   *
   * @param mrNo
   *          the mr no
   * @return the all visits and doctors
   * @throws SQLException
   *           the SQL exception
   */
  public static List getAllVisitsAndDoctors(String mrNo) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(VISITS_AND_DOCTORS);
      ps.setString(1, mrNo);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant CENTER_VISITS. */
  private static final String CENTER_VISITS = " SELECT patient_id, reg_date, "
      + " status FROM patient_registration "
      + " WHERE mr_no=? ";

  /**
   * Gets the center visits.
   *
   * @param mrNo
   *          the mr no
   * @return the center visits
   * @throws SQLException
   *           the SQL exception
   */
  public static List getCenterVisits(String mrNo) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    String centerClause = " and center_id =? ";
    String orderBy = " ORDER BY reg_date,reg_time DESC ";
    StringBuilder query = new StringBuilder(CENTER_VISITS);
    int centerID = RequestContext.getCenterId();
    if (centerID != 0) {
      query.append(centerClause);
    }
    query.append(orderBy);
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(query.toString());
      ps.setString(1, mrNo);
      if (centerID != 0) {
        ps.setInt(2, centerID);
      }
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_DOCTOR_NAME. */
  private static final String GET_DOCTOR_NAME = "SELECT d.doctor_name FROM "
      + " patient_registration pr" + " JOIN doctors d ON (pr.doctor=d.doctor_id)"
      + " WHERE mr_no = ?";

  /**
   * Gets the doctor name.
   *
   * @param mrNO
   *          the mr NO
   * @return the doctor name
   * @throws SQLException
   *           the SQL exception
   */
  public static String getDoctorName(String mrNO) throws SQLException {
    Connection con = null;
    PreparedStatement pstmt = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      pstmt = con.prepareStatement(GET_DOCTOR_NAME);
      pstmt.setString(1, mrNO);
      return DataBaseUtil.getStringValueFromDb(pstmt);
    } finally {
      DataBaseUtil.closeConnections(con, pstmt);
    }
  }

  /** The progress list for visit emr. */
  private static final String PROGRESS_LIST_FOR_VISIT_EMR = "SELECT MAX(date_time) as date_time, "
      + " pr.patient_id as visit_id, MAX(pgn.mr_no) as mr_no ,"
      + " (SELECT username from progress_notes pn WHERE"
      + " pn.date_time=MAX(pgn.date_time) and pr.mr_no=pn.mr_no and "
      + " pr.patient_id=pn.visit_id LIMIT 1) AS username,pr.reg_date"
      + " FROM progress_notes pgn "
      + " JOIN patient_registration pr ON(pgn.visit_id = pr.patient_id)" 
      + " WHERE pgn.visit_id=? ";

  /**
   * Gets the progress nts list for visit emr.
   *
   * @param visitId
   *          the visit id
   * @return the progress nts list for visit emr
   * @throws SQLException
   *           the SQL exception
   */
  public List<BasicDynaBean> getProgressNtsListForVisitEmr(String visitId) throws SQLException {

    return DataBaseUtil.queryToDynaList(PROGRESS_LIST_FOR_VISIT_EMR + " GROUP BY  pr.patient_id ",
        visitId);
  }

}