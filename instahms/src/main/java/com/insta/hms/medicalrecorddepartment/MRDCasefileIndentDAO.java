package com.insta.hms.medicalrecorddepartment;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

public class MRDCasefileIndentDAO extends GenericDAO {

  static Logger log = LoggerFactory
      .getLogger(MRDCasefileIndentDAO.class);

  public MRDCasefileIndentDAO() {
    super("mrd_casefile_attributes");
  }

  public static final String MRD_MRNOS = "SELECT mca.mr_no ,patient_name  "
      + " FROM  mrd_casefile_attributes mca join patient_details pd using(mr_no) ";

  public static List getMRNos() throws SQLException {
    return DataBaseUtil.queryToDynaList(MRD_MRNOS);
  }

  public static final String DEPT_UNIT_NAME = "SELECT  *  FROM "
      + " department  WHERE  status='A' ";

  public static List getDepartmentUnits() {
    return DataBaseUtil.queryToArrayList1(DEPT_UNIT_NAME);
  }

  public static final String DEPT_LIST = "select dept_id,dept_name from department"
      + " where status='A' order by dept_name";

  /**
   * Gets the dept names.
   *
   * @return the dept names
   * @throws SQLException
   *           the SQL exception
   */
  public static List<Hashtable> getDeptNames() throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(DEPT_LIST);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  public static final String IS_CASEFILE_EXISTS = "SELECT status,remarks FROM mrd_casefile_indent "
      + " WHERE mr_no = ? ";

  /**
   * Check casefile.
   *
   * @param mrNo
   *          the mr no
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public static boolean checkCasefile(String mrNo) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    List list = null;
    String status = null;
    String remarks = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(IS_CASEFILE_EXISTS);
      ps.setString(1, mrNo);
      list = DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    Iterator it = list.iterator();
    while (it.hasNext()) {
      BasicDynaBean indentBean = (BasicDynaBean) it.next();
      status = (String) indentBean.get("status");
      remarks = (String) indentBean.get("remarks");
    }

    return status == null || !status.equals("A");
  }

  /**
   * Gets the MRD casefile details.
   *
   * @param mrno
   *          the mrno
   * @return the MRD casefile details
   * @throws SQLException
   *           the SQL exception
   */
  public static List getMRDCasefileDetails(String mrno) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    String getMrdCasefiles = MRDCaseFileIssueDAO.MRD_CASEFILE_FIELDS
        + MRDCaseFileIssueDAO.MRD_CASEFILE_TABLES + " WHERE mca.mr_no = ? ";
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(getMrdCasefiles);
      ps.setString(1, mrno);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  public static final String UPDATE_MRD_CASEFILE = "UPDATE mrd_casefile_attributes"
      + "  SET requested_by = ?, " + " request_date=?, indented='Y' WHERE mr_no =? ";

  /**
   * Raise casefile indent.
   *
   * @param mrNo
   *          the mr no
   * @param userName
   *          the user name
   * @return the string
   * @throws SQLException
   *           the SQL exception
   */
  public static String raiseCasefileIndent(String[] mrNo, String userName) throws SQLException {

    Connection con = null;
    String msg = null;
    con = DataBaseUtil.getReadOnlyConnection();
    try (PreparedStatement ps = con.prepareStatement(UPDATE_MRD_CASEFILE)) {
      for (int j = 0; j < mrNo.length; j++) {
        if (!mrNo[j].equals("")) {
          ps.setString(1, userName);
          ps.setTimestamp(2, DateUtil.getCurrentTimestamp());
          ps.setString(3, mrNo[j]);
          ps.addBatch();
        }
      }
      int[] results = ps.executeBatch();

      for (int m = 0; m < results.length; m++) {
        if (results[m] <= 0) {
          msg = " Indent are not raised ";
        } else {
          msg = "Indent raised successfully ";
        }
      }

    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
    return msg;
  }

  public static final String MRD_CASEFILES = " SELECT mca.mr_no, foo.reg_date, foo.reg_time, "
      + " get_patient_name(pd.salutation, pd.patient_name, pd.middle_name, pd.last_name)"
      + "  as patient_name , "
      + " dep.dept_name, casefile_no, file_status, mca.issued_to, mca.remarks, indented, "
      + " requested_by FROM mrd_casefile_attributes mca  JOIN patient_details pd "
      + " ON (pd.mr_no = mca.mr_no)"
      + " LEFT JOIN patient_registration pra ON (pra.patient_id = pd.visit_id) "
      + " LEFT JOIN (SELECT min(reg_date) as reg_date ,min(reg_time) as reg_time,mca.mr_no "
      + " FROM patient_registration pr JOIN mrd_casefile_attributes mca ON pr.mr_no=mca.mr_no "
      + " GROUP BY mca.mr_no) AS foo ON foo.mr_no=mca.mr_no "
      + " LEFT JOIN department dep ON (dep.dept_id = pra.dept_name) ";

  /**
   * Gets the all MRD casefiles.
   *
   * @param action
   *          the action
   * @return the all MRD casefiles
   * @throws SQLException
   *           the SQL exception
   */
  public static List getAllMRDCasefiles(String action) throws SQLException {
    String allMrdCaseFiles = null;
    if (action.equals("indent")) {
      allMrdCaseFiles = MRD_CASEFILES + " WHERE file_status='A' and indented='N' ";
    } else if (action.equals("issue")) {
      allMrdCaseFiles = MRD_CASEFILES + " WHERE file_status='A' ";
    } else if (action.equals("return")) {
      allMrdCaseFiles = MRD_CASEFILES + " WHERE file_status='U' ";
    } else if (action.equals("close")) {
      allMrdCaseFiles = MRD_CASEFILES + " WHERE indented='Y' ";
    }
    return DataBaseUtil.queryToDynaList(allMrdCaseFiles);
  }
}
