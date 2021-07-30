package com.insta.hms.mdm.diagtestresults;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.diagnosticsmasters.Result;
import com.insta.hms.mdm.bulk.BulkDataRepository;
import com.insta.hms.mdm.bulk.CsVBulkDataEntity;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The Class DiagTestResultRepository.
 *
 * @author anil.n
 */
@Repository
public class DiagTestResultRepository extends BulkDataRepository<Integer> {

  /** The diag test result service. */
  @LazyAutowired
  private DiagTestResultService diagTestResultService;

  /**
   * Instantiates a new diag test result repository.
   */
  public DiagTestResultRepository() {
    super("test_results_master", "resultlabel_id", null);
  }

  /** The Constant GET_RESULTS_LIST_CENTER. */
  private static final String GET_RESULTS_LIST_CENTER = " select test_id,units,"
      + " display_order,trm.resultlabel_id,"
      + " expr_4_calc_result, case when trm.method_id is not null"
      + " then resultlabel|| '.' ||method_name "
      + " else resultlabel end as resultlabel, code_type,result_code,data_allowed,source_if_list,"
      + " resultlabel_short, hl7_export_code,trm.method_id,trc.center_id "
      + " FROM test_results_master trm "
      + " LEFT JOIN diag_methodology_master dm ON (dm.method_id = trm.method_id) "
      + " LEFT JOIN test_results_center trc ON (trc.resultlabel_id = trm.resultlabel_id) "
      + " WHERE trm.test_id=? "; // and (trc.center_id= ? OR trc.center_id = 0)

  /**
   * Gets the results list.
   *
   * @param testId
   *          the test id
   * @return the results list
   */
  public List<BasicDynaBean> getResultsList(String testId) {
    return DatabaseHelper.queryToDynaList(GET_RESULTS_LIST_CENTER, testId);
  }

  /** The Constant INSERT_RESULTS. */
  private static final String INSERT_RESULTS = "INSERT INTO test_results_master "
      + " (test_id, resultlabel, units, display_order, resultlabel_id,"
      + " expr_4_calc_result,code_type,result_code,data_allowed,source_if_list,"
      + "resultlabel_short,hl7_export_code, method_id, default_value ) "
      + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";

  /**
   * Insert results.
   *
   * @param resultMasters
   *          the result masters
   * @return true, if successful
   */
  protected boolean insertResults(List<Result> resultMasters) {

    List<Object[]> queryParamsList = new ArrayList<>();
    boolean success = true;
    String query = INSERT_RESULTS;

    Iterator<Result> it = resultMasters.iterator();
    while (it.hasNext()) {
      Result rs = it.next();
      List<Object> queryParams = new ArrayList<>();
      queryParams.add(rs.getTestId());
      queryParams.add(rs.getResultLabel());
      queryParams.add(rs.getUnits());
      if ("".equals(rs.getOrder())) {
        queryParams.add(0);
      } else {
        queryParams.add(Integer.parseInt(rs.getOrder()));
      }
      int resultLabelId = Integer.parseInt(rs.getResultlabel_id());
      queryParams.add(resultLabelId);
      queryParams.add(rs.getExpression());
      queryParams.add(rs.getCode_type());
      queryParams.add(rs.getResult_code());
      queryParams.add(rs.getDataAllowed());
      queryParams.add(rs.getSourceIfList());
      queryParams.add(rs.getResultLabelShort());
      queryParams.add((rs.getHl7_interface() != null && !rs.getHl7_interface().equals("")) ? rs
          .getHl7_interface() : null);
      queryParams.add(rs.getMethodId());
      queryParams.add(rs.getDefaultValue());
      queryParamsList.add(queryParams.toArray());
    }
    int[] results = DatabaseHelper.batchInsert(query, queryParamsList);

    for (int result : results) {
      if (result < 0) {
        success = false;
        break;
      }
    }
    return success;
  }

  /*
   * 
   * @see com.insta.hms.common.GenericRepository#getNextSequence()
   */
  @Override
  public Integer getNextSequence() {
    return DatabaseHelper.getNextSequence("resultlabel");
  }

  /** The Constant TEST_RESULT_LIST. */
  private static final String TEST_RESULT_LIST = " SELECT "
      + " array_to_string(array_agg(hcm.center_name),',') as centers,dmm.method_name,"
      + " trc.status,array_to_string(array_agg(hcm.center_id),',') as numcenter,trm.*"
      + " FROM TEST_RESULTS_MASTER trm "
      + " LEFT JOIN diag_methodology_master as dmm USING (method_id) "
      + " LEFT JOIN test_results_center as trc USING (resultlabel_id) "
      + " LEFT JOIN hospital_center_master hcm on hcm.center_id=trc.center_id"
      + " WHERE TEST_ID = ?"
      + " GROUP BY trm.resultlabel_id ,trc.status,dmm.method_name,trm.test_id,"
      + " trm.resultlabel,trm.units,trm.display_order,trm.expr_4_calc_result,trm.code_type,"
      + " trm.result_code,trm.data_allowed,trm.source_if_list,trm.resultlabel_short,"
      + " trm.hl7_export_code,trm.method_id, trm.default_value" + " ORDER BY trm.display_order ";

  /**
   * Gets the test results list.
   *
   * @param testId
   *          the test id
   * @return the test results list
   */
  public List<BasicDynaBean> getTestResultsList(String testId) {
    return DatabaseHelper.queryToDynaList(TEST_RESULT_LIST, testId);
  }

  /** The Constant ALL_RESULTS_WITH_RANGES. */
  private static final String ALL_RESULTS_WITH_RANGES = "   SELECT DISTINCT(tr.resultlabel_id)"
      + " FROM test_result_ranges tr    "
      + "   JOIN test_results_master using(resultlabel_id)WHERE test_id = ?  ";

  /**
   * List all test result references.
   *
   * @param testId
   *          the test id
   * @return the list
   */
  public List<BasicDynaBean> listAllTestResultReferences(String testId) {
    return DatabaseHelper.queryToDynaList(ALL_RESULTS_WITH_RANGES, testId);
  }

  /** The Constant GET_RESULTS_LIST_FOR_TEST. */
  private static final String GET_RESULTS_LIST_FOR_TEST = " select test_id,units,"
      + " display_order,trm.resultlabel_id,"
      + " expr_4_calc_result, case when trm.method_id is not null"
      + " then resultlabel|| '.' ||method_name "
      + " else resultlabel end as resultlabel, code_type,result_code,data_allowed,source_if_list,"
      + " resultlabel_short, hl7_export_code,trm.method_id "
      + " FROM test_results_master trm "
      + " LEFT JOIN diag_methodology_master dm ON (dm.method_id = trm.method_id) "
      + " LEFT JOIN test_results_center trc ON (trc.resultlabel_id = trm.resultlabel_id) "
      + " WHERE trm.test_id=?";

  /**
   * Gets the results list for json.
   *
   * @param testId
   *          the test id
   * @return the results list for json
   */
  public List<BasicDynaBean> getResultsListForJson(String testId) {
    return DatabaseHelper.queryToDynaList(GET_RESULTS_LIST_FOR_TEST, testId);
  }

  /** The Constant UPDATE_TEST_RESULTS. */
  private static final String UPDATE_TEST_RESULTS = " UPDATE test_results_master"
      + " SET resultlabel=?, units=?, display_order=?, "
      + " expr_4_calc_result = ? , code_type = ?,"
      + " result_code = ? ,data_allowed = ?, source_if_list =?,"
      + " resultlabel_short=?,hl7_export_code=?, method_id=?, default_value = ?"
      + " WHERE test_id=? AND resultlabel_id=? ";

  /**
   * Update results.
   *
   * @param modifiedResults
   *          the modified results
   * @return true, if successful
   */
  public boolean updateResults(ArrayList<Result> modifiedResults) {

    List<Object[]> queryParamsList = new ArrayList<>();
    boolean success = true;
    String query = UPDATE_TEST_RESULTS;

    Iterator<Result> it = modifiedResults.iterator();
    while (it.hasNext()) {
      Result rs = it.next();
      List<Object> queryParams = new ArrayList<>();
      queryParams.add(rs.getResultLabel());
      queryParams.add(rs.getUnits());
      int dispOrder = Integer.parseInt(rs.getOrder());
      if ("".equals(rs.getOrder())) {
        queryParams.add(null);
      } else {
        queryParams.add(dispOrder);
      }
      queryParams.add(rs.getExpression());
      queryParams.add(rs.getCode_type());
      queryParams.add(rs.getResult_code());
      queryParams.add(rs.getDataAllowed());
      queryParams.add(rs.getSourceIfList());
      queryParams.add(rs.getResultLabelShort());
      queryParams.add((rs.getHl7_interface() != null && !rs.getHl7_interface().equals("")) ? rs
          .getHl7_interface() : null);
      if (rs.getMethodId() != null && !rs.getMethodId().equals("")) {
        queryParams.add(rs.getMethodId());
      } else {
        queryParams.add(null);
      }
      queryParams.add(rs.getDefaultValue());
      queryParams.add(rs.getTestId());
      int resultLableId = Integer.parseInt(rs.getResultlabel_id());
      queryParams.add(resultLableId);
      queryParamsList.add(queryParams.toArray());
    }
    int[] results = DatabaseHelper.batchUpdate(query, queryParamsList);

    for (int result : results) {
      if (result < 0) {
        success = false;
        break;
      }
    }
    return success;
  }

  /** The Constant DELETE_TEST_RESULTS. */
  public static final String DELETE_TEST_RESULTS = "DELETE FROM test_results_master "
      + " WHERE test_id=? AND resultlabel_id=?";

  /**
   * Delete results.
   *
   * @param deleteResults
   *          the delete results
   * @return true, if successful
   */
  public boolean deleteResults(ArrayList<Result> deleteResults) {
    List<Object[]> queryParamsList = new ArrayList<>();
    boolean success = true;
    String query = DELETE_TEST_RESULTS;

    Iterator<Result> it = deleteResults.iterator();
    while (it.hasNext()) {
      Result rs = it.next();
      List<Object> queryParams = new ArrayList<>();
      queryParams.add(rs.getTestId());
      int resultId = Integer.parseInt(rs.getResultlabel_id());
      queryParams.add(resultId);

      queryParamsList.add(queryParams.toArray());
    }
    int[] results = DatabaseHelper.batchDelete(query, queryParamsList);

    for (int result : results) {
      if (result < 0) {
        success = false;
        break;
      }
    }
    return success;
  }

  /** The Constant CENTERS_FOR_RESULT_LABELS. */
  public static final String CENTERS_FOR_RESULT_LABELS = " SELECT center_id"
      + " from test_results_master trm"
      + " LEFT JOIN test_results_center trc on trc.resultlabel_id = trm.resultlabel_id"
      + " WHERE trm.test_id =? and trm.resultlabel_id=? ";

  /**
   * Centers for results.
   *
   * @param testId
   *          the test id
   * @param resultLabelId
   *          the result label id
   * @return the list
   */
  public List centersForResults(String testId, int resultLabelId) {
    return DatabaseHelper.queryToDynaList(CENTERS_FOR_RESULT_LABELS, testId, resultLabelId);
  }

  /** The Constant GET_TEST_RESULT_LABEL_DETAILS_FOR_CENTER. */
  private static final String GET_TEST_RESULT_LABEL_DETAILS_FOR_CENTER = ""
      + "SELECT trm.resultlabel_id,d.test_name,dd.ddept_name, trm.resultlabel,"
      + "TEXTCAT_COMMACAT(COALESCE(hcm.center_name,'') ) as center_name, "
      + "dmm.method_name,trm.units,trm.display_order " + "FROM test_results_master trm "
      + "JOIN test_results_center trc ON (trc.resultlabel_id = trm.resultlabel_id) "
      + "JOIN hospital_center_master hcm ON (hcm.center_id = trc.center_id) "
      + "JOIN diagnostics d USING(test_id) " + "JOIN diagnostics_departments dd USING(ddept_id)"
      + "LEFT JOIN diag_methodology_master dmm ON(dmm.method_id = trm.method_id) "
      + "WHERE trc.status = 'A' "
      + "GROUP BY trm.test_id,trm.resultlabel_id, d.test_name, dd.ddept_name, trm.resultlabel, "
      + "dmm.method_name, trm.units, trm.display_order ORDER BY test_id";

  /** The Constant GET_TEST_RESULT_LABEL_DETAILS. */
  private static final String GET_TEST_RESULT_LABEL_DETAILS = ""
      + "SELECT trm.resultlabel_id,d.test_name,dd.ddept_name, trm.resultlabel,"
      + "dmm.method_name,trm.units,trm.display_order " + "FROM test_results_master trm "
      + "JOIN diagnostics d USING(test_id) " + "JOIN diagnostics_departments dd USING(ddept_id)"
      + "LEFT JOIN diag_methodology_master dmm ON(dmm.method_id = trm.method_id) "
      + "GROUP BY trm.test_id,trm.resultlabel_id, d.test_name, dd.ddept_name, trm.resultlabel, "
      + "dmm.method_name, trm.units, trm.display_order ORDER BY test_id";


  /* 
   * @see com.insta.hms.mdm.bulk.BulkDataRepository
   * #exportData(com.insta.hms.mdm.bulk.CSVBulkDataEntity)
   */
  @Override
  public Map<String, List<String[]>> exportData(CsVBulkDataEntity csvEntity) {
    int centers = diagTestResultService.getMaxCentersDefault();
    if (centers > 1) {
      return DatabaseHelper.queryWithCustomMapper(GET_TEST_RESULT_LABEL_DETAILS_FOR_CENTER,
          new CsvEntityMapper());
    } else {
      return DatabaseHelper.queryWithCustomMapper(GET_TEST_RESULT_LABEL_DETAILS,
          new CsvEntityMapper());
    }
  }

  /**
   * The Class CsvEntityMapper.
   */
  final class CsvEntityMapper implements ResultSetExtractor<Map<String, List<String[]>>> {

    /*
     * 
     * @see org.springframework.jdbc.core.ResultSetExtractor#extractData(java.sql.ResultSet)
     */
    @Override
    public Map<String, List<String[]>> extractData(ResultSet resultSet) throws SQLException {
      ResultSetMetaData meta = resultSet.getMetaData();
      Integer columnsCount = meta.getColumnCount();

      String[] headers = new String[columnsCount];
      List<String[]> rows = new ArrayList<>();

      Map<String, String> headersMap = new HashMap<>();
      headersMap.put("resultlabel_id", "resultlabel_id");
      headersMap.put("test_name", "Test Name");
      headersMap.put("ddept_name", "Dept Name");
      headersMap.put("resultlabel", "Result label");
      headersMap.put("center_name", "Center");
      headersMap.put("method_name", "Methodology");
      headersMap.put("units", "Units");
      headersMap.put("display_order", "Display Order");

      boolean isFirstRow = true;

      while (resultSet.next()) {
        String[] row = new String[columnsCount];
        for (Integer columnIndex = 1; columnIndex <= columnsCount; columnIndex++) {
          String header = meta.getColumnName(columnIndex);
          Object rowValue = resultSet.getObject(header);

          if (isFirstRow) {
            headers[columnIndex - 1] = headersMap.get(header);
          }
          row[columnIndex - 1] = null != rowValue ? String.valueOf(rowValue) : "";
        }
        rows.add(row);
        isFirstRow = false;
      }
      if (isFirstRow) {
        for (Integer columnIndex = 1; columnIndex <= columnsCount; columnIndex++) {
          String header = meta.getColumnName(columnIndex);
          headers[columnIndex - 1] = headersMap.get(header);
        }
      }
      List<String[]> headersList = new ArrayList<>();
      headersList.add(headers);
      Map<String, List<String[]>> resultData = new HashMap<>();
      resultData.put("headers", headersList);
      resultData.put("rows", rows);
      return resultData;
    }
  }

  /** The Constant GET_RESULTS_LIST_FOR_EXPRESSION. */
  private static final String GET_RESULTS_LIST_FOR_EXPRESSION = " select test_id,"
      + " units,display_order,trm.resultlabel_id,"
      + " expr_4_calc_result, case when trm.method_id is not null"
      + " then resultlabel|| '.' ||method_name "
      + " else resultlabel end as resultlabel, code_type,result_code,data_allowed,source_if_list,"
      + " resultlabel_short, hl7_export_code,trm.method_id,trc.center_id "
      + " FROM test_results_master trm "
      + " LEFT JOIN diag_methodology_master dm ON (dm.method_id = trm.method_id) "
      + " LEFT JOIN test_results_center trc ON (trc.resultlabel_id = trm.resultlabel_id) "
      + " WHERE trm.test_id=? AND (trc.center_id= ? OR trc.center_id = 0)";

  /**
   * Gets the results list for expr.
   *
   * @param testId
   *          the test id
   * @param centerId
   *          the center ID
   * @return the results list for expr
   */
  public List<BasicDynaBean> getResultsListForExpr(String testId, int centerId) {
    return DatabaseHelper.queryToDynaList(GET_RESULTS_LIST_FOR_EXPRESSION, new Object[] { testId,
        centerId });
  }

  /** The Constant INSERT_RESULTS_CSV. */
  private static final String INSERT_RESULTS_CSV = "INSERT INTO test_results_master "
      + " (resultlabel, test_id, method_id, display_order, units, resultlabel_id) "
      + " VALUES (?,?,?,?,?,?) ";

  /**
   * Insert test results CSV.
   *
   * @param bean
   *          the bean
   * @return the int
   */
  public int insertTestResultsCsv(BasicDynaBean bean) {

    Integer labelId = (Integer) bean.get("resultlabel_id");
    String resultLabel = (String) bean.get("resultlabel");
    String testId = (String) bean.get("test_id");
    Integer methodId = bean.get("method_id") != null ? (Integer) bean.get("method_id") : null;
    Integer displayOrder = bean.get("display_order") != null ? (Integer) bean.get("display_order")
        : null;
    String units = (String) bean.get("units");
    Object[] parameters = new Object[] { resultLabel, testId, methodId, displayOrder, units,
        labelId };
    return DatabaseHelper.insert(INSERT_RESULTS_CSV, parameters);
  }

  /** The check existing result label. */
  private static final String CHECK_EXISTING_RESULT_LABEL = " SELECT resultlabel "
      + " FROM test_results_master WHERE test_id = ? AND resultlabel = ? ";

  /**
   * Gets the existing result label.
   *
   * @param testId
   *          the test id
   * @param resultlabel
   *          the resultlabel
   * @param methodId
   *          the method id
   * @return the existing result label
   */
  public List<BasicDynaBean> getExistingResultLabel(String testId, String resultlabel,
      Object methodId) {
    if (methodId == null) {
      return DatabaseHelper.queryToDynaList(CHECK_EXISTING_RESULT_LABEL, new Object[] { testId,
          resultlabel });
    } else {
      return DatabaseHelper.queryToDynaList(CHECK_EXISTING_RESULT_LABEL + " AND method_id = ? ",
          new Object[] { testId, resultlabel, methodId });
    }
  }

  /** The Constant RESULTS_FOR_EQUIPMENT. */
  private static final String RESULTS_FOR_EQUIPMENT = " SELECT d.test_name,"
      + " resultlabel_id as id,resultlabel as name,"
      + " units as units  FROM test_results_master trm  "
      + " JOIN diagnostics d  USING(test_id)  WHERE status = 'A'";

  /**
   * Gets the results for equipment.
   *
   * @return the results for equipment
   */
  public List<BasicDynaBean> getResultsForEquipment() {

    return DatabaseHelper.queryToDynaList(RESULTS_FOR_EQUIPMENT);
  }
}
