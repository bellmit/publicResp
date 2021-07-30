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
 * The Class ConsultationItemSubGroupsRepository.
 */
@Repository
public class ConsultationItemSubGroupsRepository extends BulkDataRepository<Integer> {

  /**
   * Instantiates a new consultation item sub groups repository.
   */
  public ConsultationItemSubGroupsRepository() {
    super("consultation_item_sub_groups", "consultation_type_id", "item_subgroup_id");

  }

  private static final String GET_ITEM_SUBGROUP_ID = " SELECT item_group_id ,"
      + "item_subgroup_id FROM item_sub_groups "
      + " WHERE item_group_id = ? AND item_subgroup_id = ? ";

  /**
   * Gets the sub group id.
   *
   * @param itemgrpId
   *          the itemgrp id
   * @param itemSubgrpId
   *          the item subgrp id
   * @return the sub group id
   */
  public BasicDynaBean getSubGroupId(Integer itemgrpId, Integer itemSubgrpId) {
    return DatabaseHelper.queryToDynaBean(GET_ITEM_SUBGROUP_ID,
        new Object[] { itemgrpId, itemSubgrpId });
  }

  private static final String query = "SELECT  ct.consultation_type_id , ct.consultation_type,"
      + "ig.item_group_name, isg.item_subgroup_name " + " FROM consultation_types ct "
      + " LEFT JOIN consultation_item_sub_groups cisg USING(consultation_type_id) "
      + " LEFT JOIN item_sub_groups isg using(item_subgroup_id) "
      + " LEFT JOIN item_groups ig using(item_group_id) "
      + "WHERE ct.status='A' ORDER BY consultation_type_id";

  // TODO : query based csv import and export.
  public Map<String, List<String[]>> exportData(CsVBulkDataEntity csvEntity) {
    return DatabaseHelper.queryWithCustomMapper(query, new CsVEntityMapper());

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
      headersMap.put("consultation_type_id", "consultation_type_id");
      headersMap.put("consultation_type", "consultation_type");
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
   * Gets the consult id map.
   *
   * @return the consult id map
   */
  public Map<String, String> getConsultIdMap() {
    Map<String, String> itemMaps = new HashMap<String, String>();
    List<BasicDynaBean> list = getConsultationIdMap();
    for (BasicDynaBean bean : list) {
      itemMaps.put(String.valueOf(bean.get("consultation_type_id")),
          String.valueOf(bean.get("consultation_type_id")));
    }
    return itemMaps;
  }

  /**
   * Gets the consultation id map.
   *
   * @return the consultation id map
   */
  public List<BasicDynaBean> getConsultationIdMap() {
    return DatabaseHelper.queryToDynaList("select consultation_type_id, consultation_type_id  from"
        + " consultation_types where status='A' order by consultation_type_id");
  }

}
