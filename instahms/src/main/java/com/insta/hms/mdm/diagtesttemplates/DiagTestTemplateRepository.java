package com.insta.hms.mdm.diagtesttemplates;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.bulk.BulkDataRepository;
import com.insta.hms.mdm.bulk.CsVBulkDataEntity;

import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class DiagTestTemplateRepository.
 */
@Repository
public class DiagTestTemplateRepository extends BulkDataRepository<String> {

  /**
   * Instantiates a new diag test template repository.
   */
  public DiagTestTemplateRepository() {
    super("test_template_master", "test_id", null);
  }

  /* (non-Javadoc)
   * @see com.insta.hms.mdm.bulk.BulkDataRepository
   * #exportData(com.insta.hms.mdm.bulk.CsVBulkDataEntity)
   */
  public Map<String, List<String[]>> exportData(CsVBulkDataEntity csvEntity) {
    return DatabaseHelper.queryWithCustomMapper(query, new CsvEntityMapper());

  }

  /** The Constant query. */
  private static final String query = "SELECT d.test_name, dd.ddept_name, tf.format_name "
      + "FROM test_template_master trm " 
      + "JOIN diagnostics d using(test_id) "
      + "JOIN diagnostics_departments dd using(ddept_id) "
      + "JOIN test_format tf on(trm.format_name = tf.testformat_id) " 
      + "order by test_id";

  /** The Constant INSERT_TEST_TEMPLATES. */
  private static final String INSERT_TEST_TEMPLATES = " INSERT INTO test_template_master "
      + " values(?, ?)";

  /**
   * Insert template.
   *
   * @param testId
   *          the test id
   * @param formatId
   *          the format id
   * @return the int
   */
  public int insertTemplate(String testId, String formatId) {
    return DatabaseHelper.insert(INSERT_TEST_TEMPLATES, testId, formatId);
  }

  /** The Constant DELETE_TEMPLATE. */
  private static final String DELETE_TEMPLATE = "DELETE FROM test_template_master "
      + " WHERE test_id = ? ";

  /**
   * Delete template.
   *
   * @param testId
   *          the test id
   * @return the int
   */
  public int deleteTemplate(String testId) {
    return DatabaseHelper.delete(DELETE_TEMPLATE, testId);
  }

  /**
   * The Class CsvEntityMapper.
   */
  final class CsvEntityMapper implements ResultSetExtractor<Map<String, List<String[]>>> {

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.jdbc.core.ResultSetExtractor#extractData(java.sql.ResultSet)
     */
    @Override
    public Map<String, List<String[]>> extractData(ResultSet resultSet) throws SQLException {
      ResultSetMetaData meta = resultSet.getMetaData();
      Integer columnsCount = meta.getColumnCount();

      Map<String, String> headersMap = new HashMap<String, String>();
      headersMap.put("test_name", "Test Name");
      headersMap.put("ddept_name", "Dept Name");
      headersMap.put("format_name", "Format Name");

      boolean isFirstRow = true;
      String[] headers = new String[columnsCount];
      List<String[]> rows = new ArrayList<String[]>();
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
      List<String[]> headersList = new ArrayList<String[]>();
      headersList.add(headers);
      Map<String, List<String[]>> resultData = new HashMap<String, List<String[]>>();
      resultData.put("headers", headersList);
      resultData.put("rows", rows);
      return resultData;
    }
  }

}
