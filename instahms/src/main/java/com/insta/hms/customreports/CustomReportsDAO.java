package com.insta.hms.customreports;

import com.bob.hms.common.Constants;
import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.DynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class CustomReportsDAO.
 */
public class CustomReportsDAO extends GenericDAO {

  /**
   * Instantiates a new custom reports DAO.
   */
  public CustomReportsDAO() {
    super("custom_reports");
  }

  /** The Constant SEQ. */
  private static final String SEQ = "SELECT nextval('cust_rpt_seq');";

  /**
   * Generated id.
   *
   * @return the int
   * @throws SQLException the SQL exception
   */
  public int generatedId() throws SQLException {
    return DataBaseUtil.getIntValueFromDb(SEQ);
  }

  /** The Constant SELECT_QUERY. */
  private static final String SELECT_QUERY = "SELECT report_id, "
      + "report_name, report_desc, report_type, "
      + " (SELECT count(*) FROM custom_report_variables "
      + "  WHERE report_id = cr.report_id) AS num_vars ";

  /** The Constant COUNT_QUERY. */
  private static final String COUNT_QUERY = "SELECT count(*)";

  /** The Constant TABLES. */
  private static final String TABLES = " FROM custom_reports cr ";

  /** The Constant RITES_TABLES. */
  private static final String RITES_TABLES = " LEFT JOIN "
      + "custom_report_rights crr ON (cr.report_id = crr.custom_report_id) ";

  /** The Constant INIT_FILTER. */
  private static final String INIT_FILTER = " WHERE parent_id IS NULL";

  /**
   * List reports.
   *
   * @param params the params
   * @param roleId the role id
   * @return the paged list
   * @throws SQLException the SQL exception
   */
  public PagedList listReports(Map<LISTING, Object> params, int roleId) throws SQLException {
    String sortField = (String) params.get(LISTING.SORTCOL);
    boolean sortReverse = (Boolean) params.get(LISTING.SORTASC);
    int pageSize = (Integer) params.get(LISTING.PAGESIZE);
    int pageNum = (Integer) params.get(LISTING.PAGENUM);
    pageSize = Constants.DEFAULT_PAGE_SIZE_100;
    if (pageNum < 0) {
      pageNum = 0;
    }

    Connection con = DataBaseUtil.getReadOnlyConnection();
    SearchQueryBuilder qb = new SearchQueryBuilder(con, SELECT_QUERY, COUNT_QUERY,
        TABLES + (roleId > 2 ? RITES_TABLES : ""), INIT_FILTER, sortField, sortReverse, pageSize,
        pageNum);

    if (roleId > 2) {
      qb.addFilter(qb.STRING, "crr.rights", "=", "A");
      qb.addFilter(qb.INTEGER, "role_id", "=", roleId);
    }
    qb.addSecondarySort("report_id");

    try {
      qb.build();
      return qb.getDynaPagedList();
    } finally {
      qb.close();
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /** The Constant LIST_NON_SRXML_REPORTS. */
  private static final String LIST_NON_SRXML_REPORTS = "SELECT report_id,"
      + " report_name, report_desc, report_type FROM"
      + " custom_reports WHERE parent_id IS NULL AND report_type != 'srjs'";

  /**
   * List non srjs reports.
   *
   * @return the list
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> listNonSrjsReports() throws SQLException {
    return DataBaseUtil.queryToDynaList(LIST_NON_SRXML_REPORTS);
  }

  /** The Constant SUB_REPORTS. */
  private static final String SUB_REPORTS = "SELECT * FROM custom_reports WHERE parent_id = ?;";

  /**
   * Gets the sub reports.
   *
   * @param keyid the keyid
   * @return the sub reports
   * @throws SQLException the SQL exception
   */
  public List<DynaBean> getSubReports(int keyid) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(SUB_REPORTS);
      ps.setObject(1, keyid);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant CUST_REPORT_NAMES. */
  private static final String CUST_REPORT_NAMES = "SELECT"
      + " report_name, report_id FROM custom_reports";

  /**
   * Gets the custom report name and I ds.
   *
   * @return the custom report name and I ds
   * @throws SQLException the SQL exception
   */
  public static List<DynaBean> getCustomReportNameAndIDs() throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(CUST_REPORT_NAMES);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant DEL_REPORT. */
  private static final String DEL_REPORT = "DELETE FROM custom_reports "
      + "WHERE report_id = ? OR parent_id = ?;";

  /**
   * Delete report.
   *
   * @param keyid the keyid
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean deleteReport(int keyid) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(DEL_REPORT);
      ps.setObject(1, keyid);
      ps.setObject(2, keyid);
      return ps.execute();
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant IS_DUPLICATE. */
  private static final String IS_DUPLICATE = "SELECT report_id FROM custom_reports "
      + "WHERE UPPER(TRIM(report_name)) = UPPER(TRIM(?))";

  /**
   * Check if duplicate.
   *
   * @param reportName the report name
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean checkIfDuplicate(String reportName) throws SQLException {
    Boolean response = false;
    try (Connection con = DataBaseUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(IS_DUPLICATE)) {
      ps.setObject(1, reportName);
      try (ResultSet rs = ps.executeQuery()) {
        response = rs.next();
      }
    }
    return response;
  }

  /** The Constant ALL_REPORT_TITLES. */
  private static final String ALL_REPORT_TITLES = "SELECT" + " report_name FROM custom_reports";

  /**
   * Gets the all report names.
   *
   * @return the all report names
   * @throws SQLException the SQL exception
   */
  public static List getAllReportNames() throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(ALL_REPORT_TITLES);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_ROLES_WITH_RIGHTS_FOR_REPORT. */
  private static final String GET_ROLES_WITH_RIGHTS_FOR_REPORT = " SELECT "
      + "c.role_id FROM custom_report_rights c" + " JOIN u_role u ON (u.role_id = c.role_id) "
      + " WHERE custom_report_id = ? ";

  /**
   * Gets the roles with rights.
   *
   * @param reportId the report id
   * @return the roles with rights
   * @throws SQLException the SQL exception
   */
  public static List getRolesWithRights(int reportId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_ROLES_WITH_RIGHTS_FOR_REPORT);
      ps.setInt(1, reportId);
      return DataBaseUtil.queryToArrayList1(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }

  }

}
