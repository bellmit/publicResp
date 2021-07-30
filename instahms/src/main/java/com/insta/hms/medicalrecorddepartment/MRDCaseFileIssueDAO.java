package com.insta.hms.medicalrecorddepartment;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class MRDCaseFileIssueDAO.
 *
 * @author lakshmi.p
 */
public class MRDCaseFileIssueDAO extends GenericDAO {

  /** The log. */
  static Logger log = LoggerFactory.getLogger(MRDCaseFileIssueDAO.class);

  /**
   * Instantiates a new MRD case file issue DAO.
   */
  public MRDCaseFileIssueDAO() {
    super("mrd_casefile_issuelog");
  }

  /** The Constant MRD_CASE_FILE_STATUS_ON_DISCHARGE. */
  public static final String MRD_CASE_FILE_STATUS_ON_DISCHARGE = "P"; // Pending

  /** The Constant MRD_CASE_FILE_STATUS_ON_MRD_UPDATE. */
  public static final String MRD_CASE_FILE_STATUS_ON_MRD_UPDATE = "A"; // Active

  /** The mrd casefile fields. */
  public static final String MRD_CASEFILE_FIELDS = "SELECT mca.mr_no, sm.salutation, patient_name, "
      + " get_patient_name(pd.salutation, pd.patient_name, pd.middle_name, pd.last_name)"
      + " AS patient_full_name, pra.patient_id, "
      + " CASE WHEN pra.status IS NULL THEN pri.status ELSE pra.status END AS status, "
      + " CASE WHEN pra.visit_type IS NULL THEN pri.visit_type ELSE pra.visit_type END"
      + "  AS patient_type, "
      + " coalesce(pra.dept_name,pri.dept_name) as dept_id,coalesce(rdep.dept_name,odep.dept_name)"
      + " as dept_name, last_name, patient_gender, casefile_no, case_status, "
      + " file_status, issued_id, mca.issued_to_dept, mca.issued_to_user,"
      + "  mca.remarks, mca.issued_on, "
      + " indented, requested_by, coalesce(dep.dept_name,mcu.file_user_name) as casefile_with,"
      + " coalesce(mcu.file_user_id, mca.issued_to_user) as casefile_with_id, "
      + " extract(year from request_date)::text as indented_date, trim(coalesce(idep.dept_name,"
      + "  imcu.file_user_name)) as requesting_dept, mca.requesting_dept as requesting_dept_id, "
      + " idep.dept_id as req_dept_id , (select min(reg_date) as reg_date "
      + " from patient_registration pr"
      + " where pr.mr_no=pd.mr_no) as regdate, (select min(reg_time) as reg_time"
      + "  from patient_registration pr"
      + " where pr.mr_no=pd.mr_no) as regtime, purpose, request_date, "
      + " case when request_date is not null then date(request_date) ||' '|| "
      + "extract(hours from request_date)||':'||extract(minutes from request_date) "
      + " else ' ' end as indent_date, case when request_date is not null then "
      + " date(request_date)::text else ' ' end as ind_date,"
      + " case when request_date is not null then extract(hours from request_date)||':'||  "
      + " extract(minutes from request_date) else ' ' end as ind_time, "
      + " extract(year from mca.issued_on)::text as issue_date, extract(year from created_date) "
      + " as created_date,"
      + " CASE WHEN pd.death_date is null THEN 'A' ELSE 'D' END as death_status, "
      + " CASE WHEN pd.first_mlc_visitid is not null THEN 'Y' ELSE 'N' END as mlc_status, "
      + " CASE WHEN mca.issued_to_user is null and mca.issued_to_dept is null "
      + " and requesting_dept is null "
      + " THEN 'D' WHEN mca.issued_to_user is null and mca.issued_to_dept is null "
      + " and requesting_dept is not null "
      + " THEN (case when requesting_dept like 'DEP%' then 'D' else 'U' end) "
      + " WHEN mca.issued_to_user is null and mca.issued_to_dept is not null THEN 'D' "
      + " WHEN mca.issued_to_user is not null and mca.issued_to_dept is null "
      + " THEN 'U' end as dept_type,coalesce(pra.center_id,pri.center_id) as center_id ";

  /** The mrd casefile count. */
  public static final String MRD_CASEFILE_COUNT = "SELECT count(mca.mr_no) ";

  /** The mrd casefile tables. */
  public static final String MRD_CASEFILE_TABLES = " FROM mrd_casefile_attributes mca "
      + "  JOIN patient_details pd ON (pd.mr_no = mca.mr_no AND "
                    + " (patient_confidentiality_check(pd.patient_group,pd.mr_no))) "
      + " LEFT JOIN salutation_master sm ON(sm.salutation_id = pd.salutation) "
      + " LEFT JOIN patient_registration pra ON (pd.mr_no=pra.mr_no"
      + "  AND pra.patient_id = pd.visit_id) "
      + " LEFT JOIN patient_registration pri ON (pd.mr_no=pri.mr_no "
      + " AND pri.patient_id = pd.previous_visit_id) "
      + " LEFT JOIN department dep ON (dep.dept_id = mca.issued_to_dept) "
      + " LEFT JOIN department rdep ON (rdep.dept_id = pra.dept_name) "
      + " LEFT JOIN department odep ON (odep.dept_id = pri.dept_name) "
      + " LEFT JOIN department idep ON (idep.dept_id = mca.requesting_dept) "
      + " LEFT JOIN mrd_casefile_users imcu ON (imcu.file_user_id::character varying"
      + "  = mca.requesting_dept) "
      + " LEFT JOIN mrd_casefile_users mcu ON (mcu.file_user_id=mca.issued_to_user) "
      + " LEFT JOIN mrd_casefile_issuelog mci on (mci.issue_id= mca.issued_id) ";

  private static final String PATIENT_CONFIDENTIALITY_CLAUSE = " WHERE "
      + "( patient_confidentiality_check(pd.patient_group,pd.mr_no) )";

  /**
   * Inits the where.
   *
   * @param action
   *          the action
   * @return the string
   */
  public static String initWhere(String action) {
    String where = PATIENT_CONFIDENTIALITY_CLAUSE;
    if (action.equals("indent")) {
      where += " AND file_status='A' and indented='N' and case_status!='I' ";
    } else if (action.equals("issue")) {
      where += " AND file_status='A' and case_status!='I' ";
    } else if (action.equals("return")) {
      where += " AND file_status='U' and case_status!='I' ";
    } else if (action.equals("close")) {
      where += " AND indented='Y' and case_status!='I' ";
    } else if (action.equals("mulitple")) {
      where += " AND indented = 'Y' and case_status!='I' ";
    }
    return where;
  }

  /**
   * Search MRD case list.
   *
   * @param map
   *          the map
   * @param listing
   *          the listing
   * @param allRecords
   *          the all records
   * @param action
   *          the action
   * @return the paged list
   * @throws ParseException
   *           the parse exception
   * @throws SQLException
   *           the SQL exception
   */
  public static PagedList searchMRDCaseList(Map map, Map<LISTING, Object> listing,
      boolean allRecords, String action) throws ParseException, SQLException {
    Connection con = null;
    PagedList list = null;
    PreparedStatement ps = null;
    String sortField = (String) listing.get(LISTING.SORTCOL);
    boolean sortReverse = (Boolean) listing.get(LISTING.SORTASC);
    int pageSize = 20;
    int pageNum = (Integer) listing.get(LISTING.PAGENUM);
    String[] centerId = (String[]) map.get("_center_id");
    if (allRecords) {
      pageSize = 0;
      pageNum = 0;
    }
    try {

      con = DataBaseUtil.getReadOnlyConnection();
      SearchQueryBuilder qb = null;

      String initWhere = initWhere(action);
      qb = new SearchQueryBuilder(con, MRD_CASEFILE_FIELDS, MRD_CASEFILE_COUNT,
          MRD_CASEFILE_TABLES, initWhere, sortField, sortReverse, pageSize, pageNum);

      qb.addFilterFromParamMap(map);
      // this is for filtering center specific results
      if (RequestContext.getCenterId().intValue() > 0) {
        if (((Integer) GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default"))
            .intValue() > 0) {
          qb.addFilter(qb.INTEGER, "coalesce(pra.center_id,pri.center_id)", "=",
              RequestContext.getCenterId());
        }
      }

      qb.addFilter(qb.STRING, "mca.mr_no", "=", getValue("_mr_no", map));
      qb.addFilter(qb.STRING, "coalesce(pra.dept_name,pri.dept_name)", "=",
          getValue("_dept_id", map));
      qb.addFilter(qb.STRING, "idep.dept_id", "=", getValue("_req_dept_id", map));
      qb.addFilter(qb.STRING, "mca.issued_to_dept", "=", getValue("_issued_to_dept", map));

      if (!getValue("_issued_to_user", map).equals("")) {
        qb.addFilter(qb.INTEGER, "mca.issued_to_user", "=",
            Integer.parseInt(getValue("_issued_to_user", map)));
      }

      List<String> statusList = Arrays.asList((String[]) map.get("_visit_status") == null
          ? new String[0] : (String[]) map.get("_visit_status"));
      if (statusList != null && !statusList.isEmpty() && !statusList.get(0).equals("")) {
        qb.addFilter(qb.STRING, "CASE WHEN COALESCE(pd.visit_id, '') != '' THEN 'A' WHEN"
            + "  COALESCE(pd.previous_visit_id, '') != '' THEN 'I' END", "in", statusList);
      }

      List<String> visitTypeList = Arrays.asList((String[]) map.get("_visit_type") == null
          ? new String[0] : (String[]) map.get("_visit_type"));
      if (visitTypeList != null && !visitTypeList.isEmpty() && !visitTypeList.get(0).equals("")) {
        qb.addFilter(qb.STRING,
            "CASE WHEN COALESCE(pd.visit_id, '') != '' THEN pra.visit_type "
                + "WHEN COALESCE(pd.previous_visit_id, '') != '' THEN pri.visit_type ELSE 'n' END",
            "in", visitTypeList);
      }

      qb.addSecondarySort("mca.mr_no", false);
      qb.build();
      if (action.equals("search")) {
        if (allRecords) {
          list = qb.getDynaPagedList();
        } else {
          list = qb.getMappedPagedList();
        }
      } else {
        list = qb.getMappedPagedList();
      }
      qb.close();
    } finally {
      con.close();
    }
    return list;
  }

  /**
   * Gets the value.
   *
   * @param key
   *          the key
   * @param params
   *          the params
   * @return the value
   */
  private static String getValue(String key, Map params) {
    Object[] obj = (Object[]) params.get(key);
    if (obj != null && obj[0] != null) {
      return obj[0].toString();
    }
    return "";
  }

  /** The Constant GET_MRD_DETAILS. */
  private static final String GET_MRD_DETAILS = "SELECT mca.file_status,"
      + "  mca.issued_id, mci.issued_on, "
      + " coalesce(dep.dept_name,mcu.file_user_name) as issued_to, mci.purpose, mci.issue_user, "
      + " mca.case_status FROM mrd_casefile_attributes mca LEFT JOIN mrd_casefile_issuelog mci ON "
      + " (mci.issue_id = mca.issued_id) LEFT JOIN department dep"
      + "  ON (mci.issued_to_dept = dep.dept_id) "
      + " LEFT JOIN mrd_casefile_users mcu ON (mci.issued_to_user = mcu.file_user_id)"
      + "  WHERE mca.mr_no = ? ";

  /**
   * Gets the issue details.
   *
   * @param mrNo
   *          the mr no
   * @return the issue details
   * @throws SQLException
   *           the SQL exception
   */
  public static BasicDynaBean getIssueDetails(String mrNo) throws SQLException {
    BasicDynaBean bean = null;
    List list = DataBaseUtil.queryToDynaList(GET_MRD_DETAILS, mrNo);

    if (list != null && list.size() > 0) {
      bean = (BasicDynaBean) list.get(0);
    }
    return bean;
  }

  /** The Constant GET_ALL_ISSUE_DETAILS. */
  private static final String GET_ALL_ISSUE_DETAILS = "SELECT issue_id, mca.mr_no, mci.issued_on,"
      + " coalesce(dep.dept_name,mcu.file_user_name) as issued_to, "
      + " purpose, issue_user, mci.returned_on,return_user FROM mrd_casefile_issuelog mci "
      + " JOIN mrd_casefile_attributes mca on (mca.mr_no = mci.mr_no) "
      + " LEFT JOIN department dep ON (mci.issued_to_dept = dep.dept_id) "
      + " LEFT JOIN mrd_casefile_users mcu ON (mci.issued_to_user = mcu.file_user_id) "
      + " WHERE mca.mr_no = ? ORDER BY mci.issued_on ";

  /**
   * Gets the all issue details.
   *
   * @param mrNo
   *          the mr no
   * @return the all issue details
   * @throws SQLException
   *           the SQL exception
   */
  public static List<BasicDynaBean> getAllIssueDetails(String mrNo) throws SQLException {
    return DataBaseUtil.queryToDynaList(GET_ALL_ISSUE_DETAILS, mrNo);
  }

  /**
   * Sets the MRD case file status.
   *
   * @param con
   *          the con
   * @param mrno
   *          the mrno
   * @param status
   *          the status
   * @return true, if successful
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   */
  public static boolean setMRDCaseFileStatus(Connection con, String mrno, String status)
      throws IOException, SQLException {
    if (new GenericDAO("mrd_casefile_attributes").findByKey("mr_no", mrno) != null) {
      Map<String, String> fields = new HashMap<>();
      Map<String, String> keys = new HashMap<>();
      fields.put("case_status", status);
      keys.put("mr_no", mrno);
      return new GenericDAO("mrd_casefile_attributes").update(con, fields, keys) > 0;
    }
    return true;
  }

  /** The Constant DEPT_USER_QUERY. */
  public static final String DEPT_USER_QUERY = " SELECT * from ("
      + " SELECT file_user_id::character varying AS issue_to_id, "
      + " file_user_name AS issued_to_name, "
      + " 'U' as type FROM mrd_casefile_users WHERE status ='A') as foo  ";

  /**
   * Gets the issuedto dep user list.
   *
   * @param findString
   *          the find string
   * @param limit
   *          the limit
   * @return the issuedto dep user list
   * @throws SQLException
   *           the SQL exception
   */
  public static List getIssuedtoDepUserList(String findString, int limit) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    if ((findString == null) || (findString.length() < 2)) {
      return null;
    }
    StringBuilder filter = new StringBuilder();
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      filter.append("issued_to_name ILIKE ").append("'%" + findString + "%'");
      String finalQuery = DEPT_USER_QUERY + "WHERE " + filter.toString();
      ps = con.prepareStatement(finalQuery);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the indent casefiles.
   *
   * @param filter
   *          the filter
   * @param listing
   *          the listing
   * @return the indent casefiles
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   */
  public static List getIndentCasefiles(Map filter, Map listing)
      throws SQLException, ParseException {
    Connection con = null;
    PreparedStatement ps = null;
    List list = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      String[] screen = (String[]) filter.get("_screen");
      String initWhere = PATIENT_CONFIDENTIALITY_CLAUSE;
      if (screen[0].equals("issue")) {
        initWhere = " AND indented = 'Y' ";
      } else if (screen[0].equals("return")) {
        initWhere = " AND file_status = 'U' AND case_status != 'I'";
      }
      SearchQueryBuilder qb = new SearchQueryBuilder(con, MRD_CASEFILE_FIELDS, MRD_CASEFILE_COUNT,
          MRD_CASEFILE_TABLES, initWhere, null, false, 0, 0);

      qb.addFilterFromParamMap(filter);
      qb.build();
      ps = qb.getDataStatement();

      list = DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }

    return list;
  }
}
