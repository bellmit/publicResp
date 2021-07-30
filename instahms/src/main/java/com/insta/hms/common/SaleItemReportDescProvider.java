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
 * The Class SaleItemReportDescProvider.
 */
public class SaleItemReportDescProvider extends StdReportDescJsonProvider {

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
    qu.setDynamicFieldsPartitionedOn("ssd.sale_item_id");
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
    jt1.setAlias("sstd");
    jt1.setName("store_sales_tax_details");
    jt1.setExpression("ON (sstd.sale_item_id = ssd.sale_item_id)");
    jt1.setDependsOn("ssd");
    jt1.setType("left");
    List<JoinTable> jts = new ArrayList<JoinTable>();
    jts.add(jt1);

    JoinTable jt2 = new JoinTable();
    jt2.setAlias("psctd");
    jt2.setName("sales_claim_tax_details");
    jt2.setExpression("ON (psctd.sale_item_id = ssd.sale_item_id AND "
        + " psctd.claim_id = pbcl.claim_id AND pbcl.priority =1 AND "
        + " sstd.item_subgroup_id = psctd.item_subgroup_id)");
    jt2.setDependsOn("ssd,pbcl,bc,sstd");
    jt2.setType("left");
    jts.add(jt2);

    JoinTable jt3 = new JoinTable();
    jt3.setAlias("ssctd");
    jt3.setName("sales_claim_tax_details");
    jt3.setExpression("ON (ssctd.sale_item_id = ssd.sale_item_id AND "
        + " ssctd.claim_id = sbcl.claim_id AND sbcl.priority =2 AND "
        + " sstd.item_subgroup_id = ssctd.item_subgroup_id)");
    jt3.setDependsOn("ssd,sbcl,bc,sstd");
    jt3.setType("left");
    jts.add(jt3);

    JoinTable jt = new JoinTable();
    jt.setAlias("isg");
    jt.setName("item_sub_groups");
    jt.setExpression("ON (isg.item_subgroup_id = sstd.item_subgroup_id)");
    jt.setDependsOn("sstd");
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
    fields.putAll(addItemTaxAmtCustomFields());
    fields.putAll(addPatientTaxAmtCustomFields());
    fields.putAll(addPriSponsorTaxAmtCustomFields());
    fields.putAll(addSecSponsorTaxAmtCustomFields());

    return fields;
  }

  /**
   * Adds the item tax amt custom fields.
   *
   * @return the map
   * @throws SQLException the SQL exception
   */
  private Map<String, Field> addItemTaxAmtCustomFields() throws SQLException {
    CommonReportDAO commonReportDAO = new CommonReportDAO();
    List<BasicDynaBean> list = commonReportDAO.getItemGroupCodesName();
    Map<String, Field> newfilds = new HashMap<String, Field>();
    for (BasicDynaBean b : list) {
      String taxGroupId = String.valueOf(b.get("item_group_id"));
      String taxGroupName = (String) b.get("item_group_name");
      Field field = new Field();
      field.setDisplayName("Item " + taxGroupName + " Tax");
      field.setExpression("SUM(CASE " + " WHEN isg.item_group_id = " + taxGroupId
          + " THEN (sstd.tax_amt)" + " ELSE 0 END) OVER (PARTITION BY ssd.sale_item_id)");
      field.setTable("isg,ssm");
      field.setDataType("numeric");
      field.setDecimalType("amount");
      field.setAggFunction("sum");
      field.setFilterable(true);
      field.setFieldUsesPartition(true);
      newfilds.put("item_tax_amt_" + taxGroupId, field);
    }
    return newfilds;
  }

  /**
   * Adds the patient tax amt custom fields.
   *
   * @return the map
   * @throws SQLException the SQL exception
   */
  public static Map<String, Field> addPatientTaxAmtCustomFields() throws SQLException {
    CommonReportDAO commonReportDAO = new CommonReportDAO();
    List<BasicDynaBean> list = commonReportDAO.getItemGroupCodesName();
    Map<String, Field> newfilds = new HashMap<String, Field>();
    for (BasicDynaBean b : list) {
      Field field = new Field();
      String taxGroupId = String.valueOf(b.get("item_group_id"));
      String taxGroupName = (String) b.get("item_group_name");
      field.setDisplayName("Patient " + taxGroupName + " Tax Amt");
      field.setExpression("SUM(CASE " + " WHEN isg.item_group_id = " + taxGroupId
          + " THEN (coalesce(sstd.tax_amt,0) - (coalesce(psctd.tax_amt,0) + "
          + " coalesce(ssctd.tax_amt,0))) ELSE 0 END) OVER (PARTITION BY ssd.sale_item_id)");
      field.setTable("isg,ssd,ssm,pscd,sscd,sstd,psctd,ssctd");
      field.setDataType("numeric");
      field.setDecimalType("amount");
      field.setAggFunction("sum");
      field.setFilterable(true);
      field.setFieldUsesPartition(true);
      newfilds.put("pat_tax_amt_" + taxGroupId, field);
    }
    return newfilds;
  }

  /**
   * Adds the pri sponsor tax amt custom fields.
   *
   * @return the map
   * @throws SQLException the SQL exception
   */
  private Map<String, Field> addPriSponsorTaxAmtCustomFields() throws SQLException {
    CommonReportDAO commonReportDAO = new CommonReportDAO();
    List<BasicDynaBean> list = commonReportDAO.getItemGroupCodesName();
    Map<String, Field> newfilds = new HashMap<String, Field>();
    for (BasicDynaBean b : list) {
      String taxGroupId = String.valueOf(b.get("item_group_id"));
      String taxGroupName = (String) b.get("item_group_name");
      Field field = new Field();
      field.setDisplayName("Pri. Sponsor " + taxGroupName + " Tax Amount");
      field.setExpression("SUM(CASE WHEN isg.item_group_id = " + taxGroupId
          + " THEN (psctd.tax_amt) ELSE 0 END) OVER (PARTITION BY ssd.sale_item_id)");
      field.setTable("isg,psctd,ssd");
      field.setDataType("numeric");
      field.setDecimalType("amount");
      field.setAggFunction("sum");
      field.setFilterable(true);
      field.setFieldUsesPartition(true);
      newfilds.put("pri_total_tax_amt_" + taxGroupId, field);
    }
    return newfilds;
  }

  /**
   * Adds the sec sponsor tax amt custom fields.
   *
   * @return the map
   * @throws SQLException the SQL exception
   */
  private Map<String, Field> addSecSponsorTaxAmtCustomFields() throws SQLException {
    CommonReportDAO commonReportDAO = new CommonReportDAO();
    List<BasicDynaBean> list = commonReportDAO.getItemGroupCodesName();
    Map<String, Field> newfilds = new HashMap<String, Field>();
    for (BasicDynaBean b : list) {
      String taxGroupId = String.valueOf(b.get("item_group_id"));
      String taxGroupName = (String) b.get("item_group_name");
      Field field = new Field();
      field.setDisplayName("Sec. Sponsor " + taxGroupName + " Tax Amount");
      field.setExpression("SUM(CASE WHEN isg.item_group_id = " + taxGroupId
          + " THEN (ssctd.tax_amt) ELSE 0 END) OVER (PARTITION BY ssd.sale_item_id)");
      field.setTable("isg,ssctd,ssd");
      field.setDataType("numeric");
      field.setDecimalType("amount");
      field.setAggFunction("sum");
      field.setFilterable(true);
      field.setFieldUsesPartition(true);
      newfilds.put("sec_total_tax_amt_" + taxGroupId, field);
    }
    return newfilds;
  }

}
