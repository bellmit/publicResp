package com.insta.hms.mdm.storeitemrates.taxsubgroup;

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

/**
 * The Class StoreTariffItemSubgroupRepository.
 */
@Repository
public class StoreTariffItemSubgroupRepository extends BulkDataRepository<Integer> {

  /**
   * Instantiates a new store item subgroup repository.
   */
  public StoreTariffItemSubgroupRepository() {
    super("store_tariff_item_sub_groups", "item_id");
  }

  private static final String QUERY = "SELECT  sid.medicine_id , sid.medicine_name,"
      + " srp.store_rate_plan_id, srp.store_rate_plan_name, "
      + " ig.item_group_name, isg.item_subgroup_name " 
      + " FROM store_item_details sid "
      + " LEFT JOIN store_rate_plans srp ON store_rate_plan_id = ? "
      + " LEFT JOIN store_tariff_item_sub_groups stisg on (sid.medicine_id = stisg.item_id "
      + " AND srp.store_rate_plan_id = stisg.store_rate_plan_id) "
      + " LEFT JOIN item_sub_groups isg using(item_subgroup_id) "
      + " LEFT JOIN item_groups ig using(item_group_id) "
      + " WHERE sid.status='A' AND srp.status = 'A'"
      + " ORDER BY medicine_id, store_rate_plan_name";

  // TODO : query based csv import and export.
  public Map<String, List<String[]>> exportData(CsVBulkDataEntity csvEntity, Object[] values) {
    return DatabaseHelper.queryWithCustomMapper(QUERY,values, new CsvEntityMapper());

  }

  final class CsvEntityMapper implements ResultSetExtractor<Map<String, List<String[]>>> {
    @Override
    public Map<String, List<String[]>> extractData(ResultSet resultSet) throws SQLException {
      ResultSetMetaData meta = resultSet.getMetaData();
      Integer columnsCount = meta.getColumnCount();
      String[] headers = new String[columnsCount];
      List<String[]> rows = new ArrayList<>();

      HashMap<String, String> headersMap = new HashMap<>();
      headersMap.put("medicine_id", "medicine_id");
      headersMap.put("medicine_name", "medicine_name");
      headersMap.put("store_rate_plan_id", "store_rate_plan_id");
      headersMap.put("store_rate_plan_name", "store_rate_plan_name");
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
      Map<String, List<String[]>> resultData = new HashMap<>();
      List<String[]> headersList = new ArrayList<>();
      headersList.add(headers);
      resultData.put("headers", headersList);
      resultData.put("rows", rows);
      return resultData;
    }
  }
  
  /**
   * Gets the store item map.
   *
   * @return the store item map
   */
  public Map<String, String> getStoreItemMap() {
    Map<String, String> itemMaps = new HashMap<String, String>();
    List<BasicDynaBean> list = getMedicineMap();
    for (BasicDynaBean bean : list) {
      itemMaps.put(String.valueOf(bean.get("medicine_id")),
          String.valueOf(bean.get("medicine_id")));
    }
    return itemMaps;
  }

  public List<BasicDynaBean> getMedicineMap() {
    return DatabaseHelper.queryToDynaList("select medicine_id, medicine_id  from"
        + " store_item_details where status='A' order by medicine_id");
  }
}
