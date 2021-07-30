package com.insta.hms.wardactivities.nursenotes;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * The Class NurseNotesDAO.
 */
public class NurseNotesDAO extends GenericDAO {

  /**
   * Instantiates a new nurse notes DAO.
   */
  public NurseNotesDAO() {
    super("ip_nurse_notes");
  }

  /** The Constant GET_NOTE_NUM. */
  public static final String GET_NOTE_NUM = "select max(note_num)"
      + " from ip_nurse_notes where patient_id=?";

  /**
   * Gets the note num.
   *
   * @param patientId the patient id
   * @return the note num
   * @throws SQLException the SQL exception
   */
  public int getNoteNum(String patientId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(GET_NOTE_NUM);
      ps.setString(1, patientId);
      return DataBaseUtil.getIntValueFromDb(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }

  }

  /** The Constant GET_NURSE_NOTES. */
  public static final String GET_NURSE_NOTES = " SELECT * FROM ip_nurse_notes"
      + " WHERE patient_id = ? ORDER BY creation_datetime ";

  /**
   * Gets the nurse notes.
   *
   * @param patientId the patient id
   * @return the nurse notes
   * @throws SQLException the SQL exception
   */
  @SuppressWarnings("unchecked")
  public static List<BasicDynaBean> getNurseNotes(String patientId) throws SQLException {

    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_NURSE_NOTES);
      ps.setString(1, patientId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_NURSE_NOTES_FIELDS. */
  public static final String GET_NURSE_NOTES_FIELDS = " SELECT inn.note_id,"
      + " inn.patient_id, inn.note_num, regexp_replace(notes, E'[\\r\\n]+', ' ', 'g') as notes,"
      + " inn.creation_datetime, inn.mod_time, inn.mod_user, inn.finalized ";
  
  /** The Constant COUNT. */
  private static final String COUNT = "SELECT count(*) ";
  
  /** The Constant TABLES. */
  private static final String TABLES = " From ip_nurse_notes inn";

  /**
   * Gets the nurse notes.
   *
   * @param patientId the patient id
   * @param pageNumParam the page num param
   * @param pageSizeParam the page size param
   * @return the nurse notes
   * @throws SQLException the SQL exception
   */
  public static PagedList getNurseNotes(String patientId, String pageNumParam, String pageSizeParam)
      throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    ResultSet rs = null;
    SearchQueryBuilder qb = null;
    try {
      int pageNum = 0; 
      int noOfRecord = 0;
      int pageSize = 20;
      if (pageSizeParam != null && !pageSizeParam.equals("")) {
        pageSize = Integer.parseInt(pageSizeParam);
      }
      if (pageNumParam != null && !pageNumParam.equals("")) {
        pageNum = Integer.parseInt(pageNumParam);
      } else {
        ps = con.prepareStatement(COUNT + TABLES + " WHERE (inn.patient_id  = ?)");
        ps.setString(1, patientId);
        rs = ps.executeQuery();
        if (rs.next()) {
          noOfRecord = rs.getInt(1);
        }
        int mod = noOfRecord % pageSize;
        if (mod == 0) {
          pageNum = noOfRecord / pageSize;
        } else {
          pageNum = noOfRecord / pageSize + 1;
        }
      }
      qb = new SearchQueryBuilder(con, GET_NURSE_NOTES_FIELDS, COUNT, TABLES, null,
          "inn.creation_datetime", false, pageSize, pageNum);
      qb.addFilter(SearchQueryBuilder.STRING, "inn.patient_id", "=", patientId);
      qb.build();

      return qb.getDynaPagedList();
    } finally {
      DataBaseUtil.closeConnections(con, ps, rs);
    }
  }

}
