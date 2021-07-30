package com.insta.hms.common;

import com.insta.hms.common.StdReportDesc.Field;
import com.insta.hms.common.StdReportDesc.JoinTable;
import com.insta.hms.common.StdReportDesc.QueryUnit;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class PurchaseItemsReportDescProvider.
 */
public class PurchaseItemsReportDescProvider extends StdReportDescJsonProvider {

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.common.StdReportDescJsonProvider#getReportDesc(java.lang.String)
   */
  @Override
  public StdReportDesc getReportDesc(String descName) throws Exception {
    // TODO Auto-generated method stub
    StdReportDesc reportDesc = super.getReportDesc(descName);
    QueryUnit qu = reportDesc.getQueryUnits().get(0);
    List<JoinTable> jt = qu.getJoinTables();
    jt.addAll(addTaxCustomJoinTables());
    qu.setDynamicFieldsPartitionedOn("rppiv.grn_no, rppiv.item_batch_id");
    qu.setJoinTables(jt);
    reportDesc.getFields().putAll(addTaxCustomFields());
    reportDesc.validate();
    return reportDesc;
  }

  /**
   * Adds the tax custom join tables.
   *
   * @return the list
   */
  public List<JoinTable> addTaxCustomJoinTables() {
    JoinTable jt1 = new JoinTable();
    jt1.setAlias("sgtd");
    jt1.setName("store_grn_tax_details");
    jt1.setExpression("ON (sgtd.grn_no = rppiv.grn_no AND sgtd.medicine_id = rppiv.medicine_id "
        + "AND sgtd.item_batch_id = rppiv.item_batch_id)");
    jt1.setDependsOn("rppiv");
    jt1.setType("left");
    List<JoinTable> jts = new ArrayList<JoinTable>();
    jts.add(jt1);

    JoinTable jt = new JoinTable();
    jt.setAlias("isg");
    jt.setName("item_sub_groups");
    jt.setExpression("ON (isg.item_subgroup_id = sgtd.item_subgroup_id)");
    jt.setDependsOn("sgtd");
    jt.setType("left");
    jts.add(jt);
    return jts;
  }

  /**
   * Adds the tax custom fields.
   *
   * @return the map
   * @throws SQLException the SQL exception
   */
  public Map<String, Field> addTaxCustomFields() throws SQLException {
    // TODO Auto-generated method stub
    Map<String, Field> fields = new HashMap<String, Field>();
    fields.putAll(addTaxAmtCustomFields());

    return fields;
  }

  /**
   * Adds the tax amt custom fields.
   *
   * @return the map
   * @throws SQLException the SQL exception
   */
  private Map<String, Field> addTaxAmtCustomFields() throws SQLException {
    CommonReportDAO commonReportDAO = new CommonReportDAO();
    List<BasicDynaBean> list = commonReportDAO.getItemGroupCodesName();
    Map<String, Field> newfilds = new HashMap<String, Field>();
    for (BasicDynaBean b : list) {
      String taxGroupId = String.valueOf(b.get("item_group_id"));
      String taxGroupName = (String) b.get("item_group_name");
      Field field = new Field();
      field.setDisplayName(taxGroupName + " Tax");
      field.setExpression("SUM(CASE WHEN isg.item_group_id = " + taxGroupId
          + " THEN sgtd.tax_amt ELSE 0 END) OVER (PARTITION BY rppiv.grn_no,rppiv.item_batch_id)");
      field.setTable("isg");
      field.setDataType("numeric");
      field.setDecimalType("amount");
      field.setAggFunction("sum");
      field.setFilterable(true);
      field.setFieldUsesPartition(true);
      newfilds.put("tax_amt_" + taxGroupId, field);
    }
    return newfilds;
  }

}
