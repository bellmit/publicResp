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
public class ServiceItemSubGroupRepository extends BulkDataRepository<String> {

  public ServiceItemSubGroupRepository() {
    super("service_item_sub_groups", "service_id", "item_subgroup_id");
  }

  private static final String QUERY = "SELECT  s.service_id , s.service_name,"
      + "ig.item_group_name, isg.item_subgroup_name " + " FROM services s "
      + " LEFT JOIN service_item_sub_groups sisg USING(service_id) "
      + " LEFT JOIN item_sub_groups isg using(item_subgroup_id) "
      + " LEFT JOIN item_groups ig using(item_group_id) "
      + "WHERE s.status='A' ORDER BY service_id";

  // TODO : query based csv import and export.
  public Map<String, List<String[]>> exportData(CsVBulkDataEntity csvEntity) {
    return DatabaseHelper.queryWithCustomMapper(QUERY, new CsvEntityMapper());

  }

  final class CsvEntityMapper implements ResultSetExtractor<Map<String, List<String[]>>> {
    @Override
    public Map<String, List<String[]>> extractData(ResultSet resultSet) throws SQLException {
      ResultSetMetaData meta = resultSet.getMetaData();
      Integer columnsCount = meta.getColumnCount();
      String[] headers = new String[columnsCount];
      List<String[]> rows = new ArrayList<String[]>();

      HashMap<String, String> headersMap = new HashMap<String, String>();
      headersMap.put("service_id", "service_id");
      headersMap.put("service_name", "service_name");
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
   * Gets the services map.
   *
   * @return the services map
   */
  public Map<String, String> getServicesMap() {
    Map<String, String> itemSubGrpMaps = new HashMap<String, String>();
    List<BasicDynaBean> list = getServiceMap();
    for (BasicDynaBean bean : list) {
      itemSubGrpMaps.put((String) bean.get("service_id"), (String) bean.get("service_id"));
    }
    return itemSubGrpMaps;
  }

  public List<BasicDynaBean> getServiceMap() {
    return DatabaseHelper.queryToDynaList(
        "select service_id, service_id  from" + " services where status='A' order by service_id");
  }

}
