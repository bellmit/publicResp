package com.insta.hms.diagnosticsmasters.addtest;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class TestResultsDAO.
 */
public class TestResultsDAO extends GenericDAO {

  /**
   * Instantiates a new test results DAO.
   */
  public TestResultsDAO() {
    super("test_results_master");
  }

  /** The Constant RESULTS_FOR_EUIPMENT. */
  private static final String RESULTS_FOR_EUIPMENT = " SELECT d.test_name,resultlabel_id as id,"
      + " resultlabel as name,units as units "
      + "    FROM test_results_master trm "
      + "    JOIN diagnostics d  USING(test_id)  WHERE status = 'A' ";

  /* (non-Javadoc)
   * @see com.insta.hms.common.GenericDAO#listAll()
   */
  @Override
  public List<BasicDynaBean> listAll() throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(RESULTS_FOR_EUIPMENT);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Search results.
   *
   * @param resultNameQuery the result name query
   * @return the list
   * @throws SQLException the SQL exception
   */
  public static List searchResults(String resultNameQuery) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(RESULTS_FOR_EUIPMENT + " AND resultlabel ilike ? ");
      ps.setString(1, resultNameQuery);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_RESULTS_LIST. */
  private static final String GET_RESULTS_LIST = " select test_id,units,"
      + " display_order,resultlabel_id,"
      + " expr_4_calc_result, case when trm.method_id is not null then"
      + "  resultlabel|| '.' ||method_name "
      + " else resultlabel end as resultlabel, code_type,result_code,data_allowed,source_if_list,"
      + " resultlabel_short, hl7_export_code,trm.method_id " + " FROM test_results_master trm "
      + " LEFT JOIN diag_methodology_master dm ON (dm.method_id = trm.method_id) "
      + " WHERE trm.test_id=? ";

  /** The Constant GET_RESULTS_LIST_CENTER. */
  private static final String GET_RESULTS_LIST_CENTER = " select test_id,units,display_order,"
      + " trm.resultlabel_id,"
      + " expr_4_calc_result, case when trm.method_id is not null then"
      + "  resultlabel|| '.' ||method_name "
      + " else resultlabel end as resultlabel, code_type,result_code,data_allowed,source_if_list,"
      + " resultlabel_short, hl7_export_code,trm.method_id,trc.center_id "
      + " FROM test_results_master trm "
      + " LEFT JOIN diag_methodology_master dm ON (dm.method_id = trm.method_id) "
      + " LEFT JOIN test_results_center trc ON (trc.resultlabel_id = trm.resultlabel_id) "
      + " WHERE trm.test_id=? "; // and (trc.center_id= ? OR trc.center_id = 0)

  /**
   * Gets the results list.
   *
   * @param con the con
   * @param testId the test id
   * @return the results list
   * @throws SQLException the SQL exception
   * @throws Exception the exception
   */
  public List<BasicDynaBean> getResultsList(Connection con, String testId)
      throws SQLException, Exception {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(GET_RESULTS_LIST_CENTER);
      ps.setString(1, testId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
  }

  /**
   * Gets the result bean.
   *
   * @param testId the test id
   * @param resultLabel the result label
   * @param methodId the method id
   * @return the result bean
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getResultBean(String testId, String resultLabel, String methodId)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_RESULTS_LIST + " AND trm.resultlabel=? AND trm.method_id =? ");
      ps.setString(1, testId);
      ps.setString(2, resultLabel);
      ps.setInt(3, Integer.parseInt(methodId));
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_EXISTING_RESULTS_LIST. */
  private static final String GET_EXISTING_RESULTS_LIST = "SELECT * FROM test_results_master"
      + "  WHERE test_id = ? AND resultlabel = ? ";

  /**
   * Gets the existing results list.
   *
   * @param testId the test id
   * @param resultlabel the resultlabel
   * @param methodId the method id
   * @return the existing results list
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getExistingResultsList(String testId, String resultlabel,
      Object methodId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      if (null == methodId) {
        ps = con.prepareStatement(GET_EXISTING_RESULTS_LIST + " AND method_id is null");
        ps.setString(1, testId);
        ps.setString(2, resultlabel);
      } else {
        ps = con.prepareStatement(GET_EXISTING_RESULTS_LIST + " AND method_id=? ");
        ps.setString(1, testId);
        ps.setString(2, resultlabel);
        ps.setObject(3, methodId);
      }

      return DataBaseUtil.queryToDynaList(ps);

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }

  }

  /** The Constant GET_RESULTS_LIST_FOR_EXPRESSION. */
  private static final String GET_RESULTS_LIST_FOR_EXPRESSION = " select test_id,units,"
      + " display_order,trm.resultlabel_id,"
      + " expr_4_calc_result, case when trm.method_id is not null then"
      + "  resultlabel|| '.' ||method_name "
      + " else resultlabel end as resultlabel, code_type,result_code,data_allowed,source_if_list,"
      + " resultlabel_short, hl7_export_code,trm.method_id,trc.center_id "
      + " FROM test_results_master trm "
      + " LEFT JOIN diag_methodology_master dm ON (dm.method_id = trm.method_id) "
      + " LEFT JOIN test_results_center trc ON (trc.resultlabel_id = trm.resultlabel_id) "
      + " WHERE trm.test_id=? AND (trc.center_id= ? OR trc.center_id = 0)";

  /**
   * Gets the results list for expr.
   *
   * @param con the con
   * @param testId the test id
   * @param centerID the center ID
   * @return the results list for expr
   * @throws SQLException the SQL exception
   * @throws Exception the exception
   */
  public List<BasicDynaBean> getResultsListForExpr(Connection con, String testId, int centerID)
      throws SQLException, Exception {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(GET_RESULTS_LIST_FOR_EXPRESSION);
      ps.setString(1, testId);
      ps.setInt(2, centerID);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
  }

}
