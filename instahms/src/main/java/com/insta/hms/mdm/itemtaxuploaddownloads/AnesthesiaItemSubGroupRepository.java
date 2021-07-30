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
public class AnesthesiaItemSubGroupRepository extends BulkDataRepository<String> {

  public AnesthesiaItemSubGroupRepository() {
    super("anesthesia_item_sub_groups", "anesthesia_type_id", "item_subgroup_id");
  }

  private static final String QUERY = "SELECT  atm.anesthesia_type_id , "
      + "atm.anesthesia_type_name,ig.item_group_name, isg.item_subgroup_name "
      + " FROM anesthesia_type_master atm "
      + " LEFT JOIN anesthesia_item_sub_groups aisg USING(anesthesia_type_id) "
      + " LEFT JOIN item_sub_groups isg using(item_subgroup_id) "
      + " LEFT JOIN item_groups ig using(item_group_id) "
      + "WHERE atm.status='A' ORDER BY anesthesia_type_id";

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
      headersMap.put("anesthesia_type_id", "anesthesia_type_id");
      headersMap.put("anesthesia_type_name", "anesthesia_type_name");
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
   * Gets the anesth map.
   *
   * @return the anesth map
   */
  public Map<String, String> getAnesthMap() {
    Map<String, String> itemMaps = new HashMap<String, String>();
    List<BasicDynaBean> list = getDrgCodesMap();
    for (BasicDynaBean bean : list) {
      itemMaps.put((String) bean.get("anesthesia_type_id"),
          (String) bean.get("anesthesia_type_id"));
    }
    return itemMaps;
  }

  public List<BasicDynaBean> getDrgCodesMap() {
    return DatabaseHelper.queryToDynaList("select anesthesia_type_id, anesthesia_type_id  from"
        + " anesthesia_type_master where status='A' order by anesthesia_type_id");
  }

}
