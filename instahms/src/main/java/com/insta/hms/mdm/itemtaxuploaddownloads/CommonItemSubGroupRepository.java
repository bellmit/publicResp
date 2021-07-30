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

// TODO: Auto-generated Javadoc
/**
 * The Class CommonItemSubGroupRepository.
 */
@Repository
public class CommonItemSubGroupRepository extends BulkDataRepository<String> {

  /**
   * Instantiates a new common item sub group repository.
   */
  public CommonItemSubGroupRepository() {
    super("common_item_sub_groups", "charge_name", "item_subgroup_id");

  }

  private static final String QUERY = "SELECT  ccm.charge_name , "
      + "ccm.charge_type,ig.item_group_name," + " isg.item_subgroup_name "
      + " FROM common_charges_master ccm "
      + " LEFT JOIN common_item_sub_groups cisg USING(charge_name) "
      + " LEFT JOIN item_sub_groups isg using(item_subgroup_id) "
      + " LEFT JOIN item_groups ig using(item_group_id) "
      + "WHERE ccm.status='A' ORDER BY charge_name";

  public Map<String, List<String[]>> exportData(CsVBulkDataEntity csvEntity) {
    return DatabaseHelper.queryWithCustomMapper(QUERY, new CsVEntityMapper());

  }

  /**
   * The Class CsVEntityMapper.
   */
  final class CsVEntityMapper implements ResultSetExtractor<Map<String, List<String[]>>> {

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
      List<String[]> rows = new ArrayList<String[]>();

      HashMap<String, String> headersMap = new HashMap<String, String>();
      headersMap.put("charge_name", "charge_name");
      headersMap.put("charge_type", "charge_type");
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
   * Gets the common map.
   *
   * @return the common map
   */
  public Map<String, String> getCommonMap() {
    Map<String, String> itemMaps = new HashMap<String, String>();
    List<BasicDynaBean> list = getOtherMap();
    for (BasicDynaBean bean : list) {
      itemMaps.put((String) bean.get("charge_name"), (String) bean.get("charge_name"));
    }
    return itemMaps;
  }

  /**
   * Gets the other map.
   *
   * @return the other map
   */
  public List<BasicDynaBean> getOtherMap() {
    return DatabaseHelper.queryToDynaList("select charge_name, charge_name  from"
        + " common_charges_master where status='A' order by charge_name");
  }

}
