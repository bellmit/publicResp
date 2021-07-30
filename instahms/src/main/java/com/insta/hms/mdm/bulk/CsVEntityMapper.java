package com.insta.hms.mdm.bulk;

import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class CsVEntityMapper.
 */
final class CsVEntityMapper implements ResultSetExtractor<Map<String, List<String[]>>> {

  /* (non-Javadoc)
   * @see org.springframework.jdbc.core.ResultSetExtractor#extractData(java.sql.ResultSet)
   */
  @Override
  public Map<String, List<String[]>> extractData(ResultSet resultSet) throws SQLException {
    ResultSetMetaData meta = resultSet.getMetaData();
    Integer columnsCount = meta.getColumnCount();

    Map<String, List<String[]>> resultData = new HashMap<String, List<String[]>>();
    String[] headers = new String[columnsCount];
    List<String[]> headersList = new ArrayList<String[]>();
    List<String[]> rows = new ArrayList<String[]>();

    boolean isFirstRow = true;

    while (resultSet.next()) {
      String[] row = new String[columnsCount];
      for (Integer columnIndex = 1; columnIndex <= columnsCount; columnIndex++) {
        String header = meta.getColumnName(columnIndex);
        Object rowValue = resultSet.getObject(header);

        if (isFirstRow) {
          headers[columnIndex - 1] = header;
        }
        row[columnIndex - 1] = null != rowValue ? String.valueOf(rowValue) : "";
      }
      rows.add(row);
      isFirstRow = false;
    }
    headersList.add(headers);
    resultData.put("headers", headersList);
    resultData.put("rows", rows);
    return resultData;
  }
}
