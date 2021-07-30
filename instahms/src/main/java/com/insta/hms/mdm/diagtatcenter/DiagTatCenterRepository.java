package com.insta.hms.mdm.diagtatcenter;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.annotations.LazyAutowired;
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
 * The Class DiagTatCenterRepository.
 */
@Repository
public class DiagTatCenterRepository extends BulkDataRepository<String> {

  /** The diag tat center service. */
  @LazyAutowired
  private DiagTatCenterService diagTatCenterService;

  /**
   * Instantiates a new diag tat center repository.
   */
  public DiagTatCenterRepository() {
    super("diag_tat_center_master", "tat_center_id");
  }

  /** The Constant DIAG_TAT_CENTER_MASTER_FOR_MULTICENTER. */
  private static final String DIAG_TAT_CENTER_MASTER_FOR_MULTICENTER = 
      " SELECT dtcm.tat_center_id, "
      + "d.test_name,hcm.center_id,hcm.center_name,dtcm.logistics_tat_hours,"
      + "dtcm.processing_days,dtcm.conduction_start_time ,dtcm.conduction_tat_hours "
      + "FROM diag_outsource_detail dod " + "JOIN  diag_tat_center_master dtcm "
      + " ON (dtcm.test_id = dod.test_id AND dtcm.center_id = dod.source_center_id) "
      + "JOIN hospital_center_master hcm ON(hcm.center_id = dod.source_center_id) "
      + "JOIN diagnostics d ON(dtcm.test_id=d.test_id) "
      + "WHERE hcm.status='A' and hcm.center_id != 0 "
      + "GROUP BY dtcm.tat_center_id,d.test_name,hcm.center_id, "
      + "hcm.center_name,dtcm.logistics_tat_hours, "
      + "dtcm.processing_days,dtcm.conduction_start_time ,dtcm.conduction_tat_hours,d.test_id "
      + "order by d.test_id";

  /** The Constant DIAG_TAT_CENTER_MASTER_FOR_SINGLECENTER. */
  private static final String DIAG_TAT_CENTER_MASTER_FOR_SINGLECENTER = 
      "SELECT dtcm.tat_center_id,d.test_name,hcm.center_id,hcm.center_name, "
      + "dtcm.logistics_tat_hours,dtcm.processing_days, "
      + "dtcm.conduction_start_time ,dtcm.conduction_tat_hours  FROM hospital_center_master hcm "
      + "LEFT OUTER JOIN  diag_tat_center_master dtcm ON (dtcm.center_id = hcm.center_id) "
      + "JOIN diagnostics d ON(dtcm.test_id=d.test_id) "
      + "WHERE hcm.status='A'  ORDER BY dtcm.test_id";

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.mdm.bulk.BulkDataRepository#exportData(com.insta.hms.mdm.bulk.CSVBulkDataEntity)
   */
  @Override
  public Map<String, List<String[]>> exportData(CsVBulkDataEntity csvEntity) {
    int centers = diagTatCenterService.getMaxCentersDefault();
    if (centers > 1) {
      return DatabaseHelper.queryWithCustomMapper(DIAG_TAT_CENTER_MASTER_FOR_MULTICENTER,
          new CsvEntityMapper());
    } else {
      return DatabaseHelper.queryWithCustomMapper(DIAG_TAT_CENTER_MASTER_FOR_SINGLECENTER,
          new CsvEntityMapper());
    }
  }

  /**
   * The Class CSVEntityMapper.
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

      String[] headers = new String[columnsCount];
      List<String[]> rows = new ArrayList<>();
      Map<String, String> aliasUnmsToDBnmsMap = new HashMap<>();
      aliasUnmsToDBnmsMap.put("tat_center_id", "tat center id");
      aliasUnmsToDBnmsMap.put("center_id", "Center Id");
      aliasUnmsToDBnmsMap.put("test_name", "Test Name");
      aliasUnmsToDBnmsMap.put("center_name", "Center Name");
      aliasUnmsToDBnmsMap.put("logistics_tat_hours", "Logistics TAT");
      aliasUnmsToDBnmsMap.put("processing_days", "Processing Days");
      aliasUnmsToDBnmsMap.put("conduction_start_time", "Conduction Start Time");
      aliasUnmsToDBnmsMap.put("conduction_tat_hours", "Conduction TAT");

      boolean isFirstRow = true;

      while (resultSet.next()) {
        String[] row = new String[columnsCount];
        for (Integer columnIndex = 1; columnIndex <= columnsCount; columnIndex++) {
          String header = meta.getColumnName(columnIndex);
          Object rowValue = resultSet.getObject(header);

          if (isFirstRow) {
            headers[columnIndex - 1] = aliasUnmsToDBnmsMap.get(header);
          }
          row[columnIndex - 1] = null != rowValue ? String.valueOf(rowValue) : "";
        }
        rows.add(row);
        isFirstRow = false;
      }
      if (isFirstRow) {
        for (Integer columnIndex = 1; columnIndex <= columnsCount; columnIndex++) {
          String header = meta.getColumnName(columnIndex);
          headers[columnIndex - 1] = aliasUnmsToDBnmsMap.get(header);
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
}