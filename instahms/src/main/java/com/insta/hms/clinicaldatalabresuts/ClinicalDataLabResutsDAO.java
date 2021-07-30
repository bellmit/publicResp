package com.insta.hms.clinicaldatalabresuts;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * The Class ClinicalDataLabResutsDAO.
 *
 * @author mithun.saha
 */

public class ClinicalDataLabResutsDAO {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(ClinicalDataLabResutsDAO.class);

  /** The clinical lab fields. */
  private static String CLINICAL_LAB_FIELDS = " SELECT *  ";

  /** The clinical lab count. */
  private static String CLINICAL_LAB_COUNT = " SELECT count(*) ";

  /** The clinical lab tables. */
  private static String CLINICAL_LAB_TABLES = " FROM (SELECT cl.values_as_of_date,cl.mrno,"
      + " cl.values_as_of_date::text AS text_date,clr.resultlabel_id::text,clv.test_value,"
      + " cl.clinical_lab_recorded_id::text AS clinical_lab_recorded_id_text "
      + " FROM clinical_lab_recorded cl " + " JOIN patient_details pd ON (pd.mr_no = cl.mrno "
      + " AND patient_confidentiality_check(pd.patient_group, pd.mr_no))"
      + " JOIN clinical_lab_values clv USING(clinical_lab_recorded_id) "
      + " JOIN clinical_lab_result clr ON(clr.resultlabel_id=clv.resultlabel_id) "
      + " JOIN test_results_master trm ON(trm.resultlabel_id=clr.resultlabel_id) "
      + " ORDER BY clr.display_order" + " ) AS foo";

  /** The clinical master fields. */
  private static String CLINICAL_MASTER_FIELDS = " SELECT "
      + " clr.resultlabel_id::text AS resultlabel_id,"
      + " resultlabel_short,resultlabel FROM  clinical_lab_result clr "
      + " JOIN test_results_master trm ON(trm.resultlabel_id=clr.resultlabel_id) "
      + " ORDER BY clr.display_order, resultlabel_short, resultlabel";

  /**
   * Gets the clinical master records.
   *
   * @return the clinical master records
   * @throws SQLException
   *           the SQL exception
   */
  public List getClinicalMasterRecords() throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(CLINICAL_MASTER_FIELDS);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /**
   * Gets the clinical lab details.
   *
   * @param dates
   *          the dates
   * @param mrno
   *          the mrno
   * @return the clinical lab details
   * @throws SQLException
   *           the SQL exception
   */
  public PagedList getClinicalLabDetails(List dates, String mrno) throws SQLException {
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();

      SearchQueryBuilder qb = new SearchQueryBuilder(con, CLINICAL_LAB_FIELDS, CLINICAL_LAB_COUNT,
          CLINICAL_LAB_TABLES, null, null, false, 0, 0);
      qb.addFilter(SearchQueryBuilder.STRING, "mrno", "=", mrno);
      qb.addFilter(SearchQueryBuilder.DATE, "values_as_of_date", "IN", dates);
      qb.addSecondarySort("values_as_of_date", true);
      qb.build();
      return qb.getDynaPagedList();
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /** The Constant CLINICAL_LAB_DETAILS. */
  private static final String CLINICAL_LAB_DETAILS = " SELECT trm.*,trr.* "
      + " FROM clinical_lab_result clr"
      + " JOIN test_results_master trm ON(trm.resultlabel_id = clr.resultlabel_id)"
      + " LEFT JOIN test_result_ranges trr ON(trm.resultlabel_id = trr.resultlabel_id)"
      + " WHERE clr.status = 'A' ORDER BY clr.display_order";

  /**
   * Gets the clinical lab details.
   *
   * @return the clinical lab details
   * @throws SQLException
   *           the SQL exception
   */
  public static List<BasicDynaBean> getClinicalLabDetails() throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(CLINICAL_LAB_DETAILS);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The clinical dates fields. */
  private static String CLINICAL_DATES_FIELDS = " SELECT *  ";

  /** The clinical dates count. */
  private static String CLINICAL_DATES_COUNT = " SELECT count(*) ";

  /** The clinical dates tables. */
  private static String CLINICAL_DATES_TABLES = " FROM (SELECT clr.*, "
      + " clr.values_as_of_date::text AS text_date FROM clinical_lab_recorded clr "
      + " JOIN patient_details pd ON (pd.mr_no = clr.mrno "
      + " AND patient_confidentiality_check(pd.patient_group, pd.mr_no))"
      + " order by values_as_of_date desc" + " ) AS foo";

  /**
   * Gets the clinical dates.
   *
   * @param map
   *          the map
   * @param pagingParams
   *          the paging params
   * @param mrno
   *          the mrno
   * @return the clinical dates
   * @throws SQLException
   *           the SQL exception
   */
  public PagedList getClinicalDates(Map map, Map pagingParams, String mrno) throws SQLException {
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();

      SearchQueryBuilder qb = new SearchQueryBuilder(con, CLINICAL_DATES_FIELDS,
          CLINICAL_DATES_COUNT, CLINICAL_DATES_TABLES, pagingParams);
      qb.addFilter(SearchQueryBuilder.STRING, "mrno", "=", mrno);
      qb.build();

      return qb.getDynaPagedList();
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /** The Constant CLINICAL_DATES. */
  private static final String CLINICAL_DATES = "SELECT values_as_of_date::text as text_date "
      + " FROM clinical_lab_recorded " + " ORDER BY values_as_of_date ";

  /**
   * Gets the clinical dates.
   *
   * @return the clinical dates
   * @throws SQLException
   *           the SQL exception
   */
  public static List getClinicalDates() throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(CLINICAL_DATES);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the clinical lab recorded main.
   *
   * @param recordedId
   *          the recorded id
   * @return the clinical lab recorded main
   * @throws SQLException
   *           the SQL exception
   */
  public static BasicDynaBean getClinicalLabRecordedMain(int recordedId) throws SQLException {
    String query = CLINICAL_DATES_FIELDS + CLINICAL_DATES_TABLES
        + " WHERE clinical_lab_recorded_id = ? ";
    return DataBaseUtil.queryToDynaBean(query, recordedId);
  }

  /** The Constant CLINICAL_LAB_RECORDS. */
  private static final String CLINICAL_LAB_RECORDS = "SELECT trm.*,trr.*,clv.*,clr.*,cle.*  "
      + " FROM clinical_lab_recorded cle JOIN clinical_lab_values clv "
      + " ON(cle.clinical_lab_recorded_id = clv.clinical_lab_recorded_id) "
      + " JOIN patient_details pd ON (pd.mr_no = cle.mrno "
      + " AND patient_confidentiality_check(pd.patient_group, pd.mr_no))"
      + " LEFT JOIN clinical_lab_result clr ON(clr.resultlabel_id = clv.resultlabel_id)"
      + " LEFT JOIN test_results_master trm ON(trm.resultlabel_id = clr.resultlabel_id)"
      + " LEFT JOIN test_result_ranges trr ON(trm.resultlabel_id = trr.resultlabel_id)"
      + " WHERE clr.status = 'A' AND clv.clinical_lab_recorded_id = ? ORDER BY clr.display_order";

  /**
   * Gets the clinical lab records list.
   *
   * @param recordId
   *          the record id
   * @return the clinical lab records list
   * @throws SQLException
   *           the SQL exception
   */
  public static List<BasicDynaBean> getClinicalLabRecordsList(int recordId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(CLINICAL_LAB_RECORDS);
      ps.setInt(1, recordId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant CLINICAL_LAB_VALUES. */
  private static final String CLINICAL_LAB_VALUES = "SELECT clr.* "
      + " FROM clinical_lab_recorded clr " + " JOIN patient_details pd ON (pd.mr_no = clr.mrno "
      + " AND patient_confidentiality_check(pd.patient_group, pd.mr_no))"
      + " WHERE mrno=? AND values_as_of_date::text=? and clinical_lab_recorded_id = ?";

  /**
   * Gets the clinical lab values bean.
   *
   * @param mrNo
   *          the mr no
   * @param date
   *          the date
   * @param clinLabId
   *          the clin lab id
   * @return the clinical lab values bean
   * @throws SQLException
   *           the SQL exception
   */
  public static BasicDynaBean getClinicalLabValuesBean(String mrNo, String date, int clinLabId)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(CLINICAL_LAB_VALUES);
      ps.setString(1, mrNo);
      ps.setString(2, date);
      ps.setInt(3, clinLabId);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The clinical lab dates fields. */
  private static String CLINICAL_LAB_DATES_FIELDS = " SELECT *  ";

  /** The clinical lab dates count. */
  private static String CLINICAL_LAB_DATES_COUNT = " SELECT count(*) ";

  /** The clinical lab dates tables. */
  private static String CLINICAL_LAB_DATES_TABLES = " FROM "
      + " (SELECT clr.values_as_of_date::text as text_date, "
      + " clr.clinical_lab_recorded_id::text AS clinical_lab_recorded_id_text,clr.*  "
      + " FROM clinical_lab_recorded clr JOIN patient_details pd ON (pd.mr_no = clr.mrno "
      + " AND patient_confidentiality_check(pd.patient_group, pd.mr_no))"
      + " ORDER BY values_as_of_date desc) AS foo";

  /**
   * Gets the clinical lab dates.
   *
   * @param map
   *          the map
   * @param pagingParams
   *          the paging params
   * @param mrno
   *          the mrno
   * @return the clinical lab dates
   * @throws SQLException
   *           the SQL exception
   */
  public PagedList getClinicalLabDates(Map map, Map pagingParams, String mrno) throws SQLException {
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();

      SearchQueryBuilder qb = new SearchQueryBuilder(con, CLINICAL_LAB_DATES_FIELDS,
          CLINICAL_LAB_DATES_COUNT, CLINICAL_LAB_DATES_TABLES, pagingParams);
      qb.addFilter(SearchQueryBuilder.STRING, "mrno", "=", mrno);
      qb.build();

      return qb.getMappedPagedList();
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /** The Constant DIALYSIS_SESSION_BEAN. */
  private static final String DIALYSIS_SESSION_BEAN = "SELECT start_time, fin_real_wt, order_id "
      + "FROM dialysis_session "
      + "WHERE prescription_id = ? AND start_time::date = ? ORDER BY order_id DESC LIMIT 1";

  /**
   * Gets the session bean.
   *
   * @param con
   *          the con
   * @param prescriptionId
   *          the prescription id
   * @param date
   *          the date
   * @return the session bean
   * @throws SQLException
   *           the SQL exception
   */
  public static BasicDynaBean getSessionBean(Connection con, Integer prescriptionId,
      java.sql.Date date) throws SQLException {

    try (PreparedStatement pstmt = con.prepareStatement(DIALYSIS_SESSION_BEAN)) {
      pstmt.setInt(1, prescriptionId);
      pstmt.setDate(2, date);
      return DataBaseUtil.queryToDynaBean(pstmt);
    }
  }
}
