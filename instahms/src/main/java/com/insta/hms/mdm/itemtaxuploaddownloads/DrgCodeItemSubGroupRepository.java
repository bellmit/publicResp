package com.insta.hms.mdm.itemtaxuploaddownloads;

import com.insta.hms.common.DatabaseHelper;
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
import java.util.List;
import java.util.Map;

@Repository
public class DrgCodeItemSubGroupRepository extends BulkDataRepository<String> {

  public DrgCodeItemSubGroupRepository() {
    super("drg_code_item_sub_groups", "drg_code", "item_subgroup_id");
  }

  private static final String query = " SELECT  dcm.drg_code, dcm.drg_description ,"
      + "ig.item_group_name, isg.item_subgroup_name " + " FROM drg_codes_master dcm "
      + " LEFT JOIN drg_code_item_sub_groups disg USING(drg_code) "
      + " LEFT JOIN item_sub_groups isg using(item_subgroup_id) "
      + " LEFT JOIN item_groups ig using(item_group_id) "
      + " WHERE dcm.status='A' ORDER BY drg_code";

  // TODO : query based csv import and export.
  public Map<String, List<String[]>> exportData(CsVBulkDataEntity csvEntity) {
    return DatabaseHelper.queryWithCustomMapper(query, new CsVEntityMapper());

  }

  final class CsVEntityMapper implements ResultSetExtractor<Map<String, List<String[]>>> {
    @Override
    public Map<String, List<String[]>> extractData(ResultSet resultSet) throws SQLException {
      ResultSetMetaData meta = resultSet.getMetaData();
      Integer columnsCount = meta.getColumnCount();
      String[] headers = new String[columnsCount];
      List<String[]> rows = new ArrayList<String[]>();

      HashMap<String, String> headersMap = new HashMap<String, String>();
      headersMap.put("drg_code", "drg_code");
      headersMap.put("drg_description", "drg_description");
      headersMap.put("item_group_name", "item_group_name");
      headersMap.put("item_subgroup_name", "item_subgroup_name");

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
      Map<String, List<String[]>> resultData = new HashMap<String, List<String[]>>();
      List<String[]> headersList = new ArrayList<String[]>();
      headersList.add(headers);
      resultData.put("headers", headersList);
      resultData.put("rows", rows);
      return resultData;
    }
  }

  /**
   * Gets the drg code map.
   *
   * @return the drg code map
   */
  public Map<String, String> getDrgCodeMap() {
    Map<String, String> itemMaps = new HashMap<String, String>();
    List<BasicDynaBean> list = getDrgCodesMap();
    for (BasicDynaBean bean : list) {
      itemMaps.put((String) bean.get("drg_code"), (String) bean.get("drg_code"));
    }
    return itemMaps;
  }

  public List<BasicDynaBean> getDrgCodesMap() {
    return DatabaseHelper.queryToDynaList(
        "select drg_code, drg_code  from" + " drg_codes_master where status='A' order by drg_code");
  }

}
