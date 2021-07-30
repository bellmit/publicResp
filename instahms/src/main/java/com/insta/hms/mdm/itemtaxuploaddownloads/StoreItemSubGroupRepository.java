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
public class StoreItemSubGroupRepository extends BulkDataRepository<Integer> {

  public StoreItemSubGroupRepository() {
    super("store_item_sub_groups", "medicine_id");
  }

  private static final String query = "SELECT  sid.medicine_id , sid.medicine_name,"
      + "ig.item_group_name, isg.item_subgroup_name " + " FROM store_item_details sid "
      + " LEFT JOIN store_item_sub_groups sisg USING(medicine_id) "
      + " LEFT JOIN item_sub_groups isg using(item_subgroup_id) "
      + " LEFT JOIN item_groups ig using(item_group_id) "
      + "WHERE sid.status='A' ORDER BY medicine_id";

  // TODO : query based csv import and export.
  public Map<String, List<String[]>> exportData(CsVBulkDataEntity csvEntity) {
    return DatabaseHelper.queryWithCustomMapper(query, new CsvEntityMapper());

  }
  
  @Override
  public boolean supportsAutoId() {
    return false;
  }

  final class CsvEntityMapper implements ResultSetExtractor<Map<String, List<String[]>>> {
    @Override
    public Map<String, List<String[]>> extractData(ResultSet resultSet) throws SQLException {
      ResultSetMetaData meta = resultSet.getMetaData();
      Integer columnsCount = meta.getColumnCount();
      String[] headers = new String[columnsCount];
      List<String[]> rows = new ArrayList<String[]>();

      HashMap<String, String> headersMap = new HashMap<String, String>();
      headersMap.put("medicine_id", "medicine_id");
      headersMap.put("medicine_name", "medicine_name");
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
  
  private static final String SUB_GROUP_CODE_QUERY = "select integration_subgroup_id,tax_rate "
      + " from store_item_sub_groups sisg"
      + " join item_sub_groups isg on (sisg.item_subgroup_id = isg.item_subgroup_id)"
      + " join item_sub_groups_tax_details isgtd on "
      + " (sisg.item_subgroup_id = isgtd.item_subgroup_id)"
      + " where medicine_id =?";

  public List<BasicDynaBean> getItemSubgroupCodes(int itemId) {
    return DatabaseHelper.queryToDynaList(SUB_GROUP_CODE_QUERY, new Object[] { itemId });
  }

}
