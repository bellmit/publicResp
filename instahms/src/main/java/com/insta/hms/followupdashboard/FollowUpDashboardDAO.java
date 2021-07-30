package com.insta.hms.followupdashboard;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The Class FollowUpDashboardDAO.
 */
public class FollowUpDashboardDAO {

  /**
   * The Constant FIELD_NONE.
   */
  public static final int FIELD_NONE = 0;

  /**
   * The Constant FIELD_MRNO.
   */
  public static final int FIELD_MRNO = 1;

  /**
   * The Constant QUERY_FIELD_NAMES.
   */
  private static final String[] QUERY_FIELD_NAMES = {"", "pd.mr_no"};

  /**
   * The connection.
   */
  private Connection connection = null;

  /**
   * The Constant ALL_QUERY_FIELDS.
   */
  private static final String ALL_QUERY_FIELDS = "SELECT  pd.mr_no, fud.patient_id, fud"
      + ".followup_id, "
      + " get_patient_name(pd.salutation, pd.patient_name, pd.middle_name, pd.last_name) as"
      + " patient_name, "
      + " dept.dept_id as followupdeptid, dept.dept_name as followupdeptname, "
      + " doc.doctor_id as followupdocid, doc.doctor_name as followupdocname, "
      + " fud.followup_date, fud.followup_remarks, pd.patient_phone as phone ";

  /**
   * The Constant ALL_QUERY_COUNT.
   */
  private static final String ALL_QUERY_COUNT = "SELECT COUNT(fud.followup_id)";

  /**
   * The Constant ALL_QUERY_TABLES.
   */
  private static final String ALL_QUERY_TABLES = " FROM follow_up_details fud "
      + " JOIN patient_registration pr ON fud.patient_id=pr.patient_id "
      + " LEFT OUTER JOIN patient_details pd ON (pr.mr_no=pd.mr_no AND "
      + " ( patient_confidentiality_check(pd.patient_group,pd.mr_no) )) "
      + " LEFT OUTER JOIN doctors doc ON doc.doctor_id = fud.followup_doctor_id "
      + " LEFT OUTER JOIN department dept ON (dept.dept_id = doc.dept_id)";

  /**
   * Gets the follow up details.
   *
   * @param params      the params
   * @param sortOrder   the sort order
   * @param sortReverse the sort reverse
   * @param pageSize    the page size
   * @param pageNum     the page num
   * @return the follow up details
   * @throws SQLException   the SQL exception
   * @throws ParseException the parse exception
   */
  public static PagedList getFollowUpDetails(Map params, int sortOrder, boolean sortReverse,
      int pageSize, int pageNum) throws SQLException, ParseException {

    String sortField = null;

    if ((sortOrder != FIELD_NONE) && (sortOrder < QUERY_FIELD_NAMES.length)) {
      sortField = QUERY_FIELD_NAMES[sortOrder];
    }

    List<String> validColumns = new ArrayList();
    validColumns.add("doc.doctor_id");
    validColumns.add("doc.doctor_name");
    validColumns.add("dept.dept_id");
    validColumns.add("dept.dept_name");
    validColumns.add("fud.followup_date");
    validColumns.add("pr.reg_date");
    validColumns.add("fud.followup_remarks");
    validColumns.add("pd.mr_no");
    validColumns.add("fud.followup_id");
    validColumns.add("fud.patient_id");
    validColumns.add("pd.patient_phone");
    
    Connection con = DataBaseUtil.getReadOnlyConnection();
    SearchQueryBuilder qb = new SearchQueryBuilder(con, ALL_QUERY_FIELDS, ALL_QUERY_COUNT,
          ALL_QUERY_TABLES, null, null, sortField, sortReverse, pageSize, pageNum, validColumns);

    qb.addFilterFromParamMap(params);

    qb.build();

    PreparedStatement psData = qb.getDataStatement();
    PreparedStatement psCount = qb.getCountStatement();

    ResultSet rsData = psData.executeQuery();

    ArrayList list = new ArrayList();
    while (rsData.next()) {
      FollowUpDashboardSearch fs = new FollowUpDashboardSearch();
      populateFollowUpDetails(fs, rsData);
      list.add(fs);
    }
    rsData.close();

    int totalCount = 0;
    ResultSet rsCount = psCount.executeQuery();
    if (rsCount.next()) {
      totalCount = rsCount.getInt(1);
    }
    rsCount.close();

    qb.close();
    con.close();

    return new PagedList(list, totalCount, pageSize, pageNum);
  }

  /**
   * Populate follow up details.
   *
   * @param fds the fds
   * @param rs  the rs
   * @throws SQLException   the SQL exception
   * @throws ParseException the parse exception
   */
  private static void populateFollowUpDetails(FollowUpDashboardSearch fds, ResultSet rs)
      throws SQLException, ParseException {

    fds.setMrno(rs.getString("mr_no"));
    fds.setPatientName(rs.getString("patient_name"));
    fds.setPhone(rs.getString("phone"));
    fds.setFollowUpDeptId(rs.getString("followUpDeptId"));
    fds.setFollowUpDeptName(rs.getString("followUpDeptName"));
    fds.setFollowUpDocId(rs.getString("followUpDocId"));
    fds.setFollowUpDocName(rs.getString("followUpDocName"));
    fds.setFollowUpDate(rs.getDate("followup_date"));
    fds.setFollowUpRemarks(rs.getString("followup_remarks"));
  }
}
