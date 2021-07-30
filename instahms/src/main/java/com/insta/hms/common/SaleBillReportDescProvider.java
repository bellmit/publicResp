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
 * The Class SaleBillReportDescProvider.
 */
public class SaleBillReportDescProvider extends StdReportDescJsonProvider {

  /** The common report DAO. */
  private static CommonReportDAO commonReportDAO = new CommonReportDAO();

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
    qu.setDynamicFieldsPartitionedOn("ssm.sale_id");
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
    jt1.setAlias("pscd");
    jt1.setName("sales_claim_details");
    jt1.setExpression("ON (pscd.sale_item_id = ssd.sale_item_id AND pscd.claim_id = pbcl.claim_id "
        + "AND pbcl.priority =1)");
    jt1.setDependsOn("ssd,pbcl,bc");
    jt1.setType("left");
    List<JoinTable> jts = new ArrayList<JoinTable>();
    jts.add(jt1);

    JoinTable jt2 = new JoinTable();
    jt2.setAlias("sscd");
    jt2.setName("sales_claim_details");
    jt2.setExpression("ON (sscd.sale_item_id = ssd.sale_item_id AND sscd.claim_id = sbcl.claim_id "
        + "AND sbcl.priority =2)");
    jt2.setDependsOn("ssd,sbcl,bc");
    jt2.setType("left");
    jts.add(jt2);

    JoinTable jt3 = new JoinTable();
    jt3.setAlias("sstd");
    jt3.setName("store_sales_tax_details");
    jt3.setExpression("ON (sstd.sale_item_id = ssd.sale_item_id)");
    jt3.setDependsOn("ssd");
    jt3.setType("left");
    jts.add(jt3);

    JoinTable jt4 = new JoinTable();
    jt4.setAlias("psctd");
    jt4.setName("sales_claim_tax_details");
    jt4.setExpression(
        "ON (psctd.sale_item_id = ssd.sale_item_id AND psctd.claim_id = pbcl.claim_id "
            + " AND pbcl.priority =1 AND sstd.item_subgroup_id = psctd.item_subgroup_id)");
    jt4.setDependsOn("ssd,pbcl,bc,sstd");
    jt4.setType("left");
    jts.add(jt4);

    JoinTable jt5 = new JoinTable();
    jt5.setAlias("ssctd");
    jt5.setName("sales_claim_tax_details");
    jt5.setExpression(
        "ON (ssctd.sale_item_id = ssd.sale_item_id AND ssctd.claim_id = sbcl.claim_id "
            + " AND sbcl.priority =2 AND sstd.item_subgroup_id = ssctd.item_subgroup_id)");
    jt5.setDependsOn("ssd,sbcl,bc,sstd");
    jt5.setType("left");
    jts.add(jt5);

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
    fields.putAll(addBillTaxAmtCustomFields());
    fields.putAll(addPatientTaxAmtCustomFields());
    fields.putAll(addPriSponsorTaxAmtCustomFields());
    fields.putAll(addSecSponsorTaxAmtCustomFields());
    fields.putAll(addPatientAmount());
    fields.putAll(addPatientAmtWithoutTax());
    fields.putAll(addPatientTaxAmount());
    fields.putAll(addPriSponsorAmount());
    fields.putAll(addSecSponsorAmount());
    fields.putAll(addPriSponsorWithoutTaxAmount());
    fields.putAll(addSecSponsorWithoutTaxAmount());
    fields.putAll(addPriSponsorTaxAmount());
    fields.putAll(addSecSponsorTaxAmount());

    return fields;
  }

  /**
   * Adds the bill tax amt custom fields.
   *
   * @return the map
   * @throws SQLException the SQL exception
   */
  private Map<String, Field> addBillTaxAmtCustomFields() throws SQLException {

    List<BasicDynaBean> list = commonReportDAO.getItemGroupCodesName();
    Map<String, Field> newfilds = new HashMap<String, Field>();
    for (BasicDynaBean b : list) {
      String taxGroupId = String.valueOf(b.get("item_group_id"));
      String taxGroupName = (String) b.get("item_group_name");
      Field field = new Field();
      field.setDisplayName("Bill " + taxGroupName + " Tax");
      field.setExpression("SUM(CASE " + " WHEN isg.item_group_id = " + taxGroupId
          + " THEN (sstd.tax_amt)" + " ELSE 0 END) OVER (PARTITION BY ssd.sale_id)");
      field.setTable("isg,ssm");
      field.setDataType("numeric");
      field.setDecimalType("amount");
      field.setAggFunction("sum");
      field.setFilterable(true);
      field.setFieldUsesPartition(true);
      newfilds.put("bill_tax_amt_" + taxGroupId, field);
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

    List<BasicDynaBean> list = commonReportDAO.getItemGroupCodesName();
    Map<String, Field> newfilds = new HashMap<String, Field>();
    for (BasicDynaBean b : list) {
      Field field = new Field();
      String taxGroupId = String.valueOf(b.get("item_group_id"));
      String taxGroupName = (String) b.get("item_group_name");
      field.setDisplayName("Patient " + taxGroupName + " Tax Amt");
      field.setExpression("SUM(CASE " + " WHEN isg.item_group_id = " + taxGroupId
          + " THEN (coalesce(sstd.tax_amt,0) - (coalesce(psctd.tax_amt,0) + "
          + " coalesce(ssctd.tax_amt,0))) ELSE 0 END) OVER (PARTITION BY ssd.sale_id)");
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

    List<BasicDynaBean> list = commonReportDAO.getItemGroupCodesName();
    Map<String, Field> newfilds = new HashMap<String, Field>();
    for (BasicDynaBean b : list) {
      String taxGroupId = String.valueOf(b.get("item_group_id"));
      String taxGroupName = (String) b.get("item_group_name");
      Field field = new Field();
      field.setDisplayName("Pri. Sponsor " + taxGroupName + " Tax Amount");
      field.setExpression("SUM(CASE WHEN isg.item_group_id = " + taxGroupId
          + " THEN (psctd.tax_amt) ELSE 0 END) OVER (PARTITION BY ssm.sale_id)");
      field.setTable("isg,psctd,ssm");
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

    List<BasicDynaBean> list = commonReportDAO.getItemGroupCodesName();
    Map<String, Field> newfilds = new HashMap<String, Field>();
    for (BasicDynaBean b : list) {
      String taxGroupId = String.valueOf(b.get("item_group_id"));
      String taxGroupName = (String) b.get("item_group_name");
      Field field = new Field();
      field.setDisplayName("Sec. Sponsor " + taxGroupName + " Tax Amount");
      field.setExpression("SUM(CASE WHEN isg.item_group_id = " + taxGroupId
          + " THEN (ssctd.tax_amt) ELSE 0 END) OVER (PARTITION BY ssm.sale_id)");
      field.setTable("isg,ssctd,ssm");
      field.setDataType("numeric");
      field.setDecimalType("amount");
      field.setAggFunction("sum");
      field.setFilterable(true);
      field.setFieldUsesPartition(true);
      newfilds.put("sec_total_tax_amt_" + taxGroupId, field);
    }
    return newfilds;
  }

  /**
   * Adds the patient amount.
   *
   * @return the map
   * @throws SQLException the SQL exception
   */
  private Map<String, Field> addPatientAmount() throws SQLException {
    Field field = new Field();
    field.setDisplayName("Patient Amount");
    field.setExpression("SUM(ssd.amount - (coalesce(pscd.insurance_claim_amt,0) + "
        + " coalesce(sscd.insurance_claim_amt,0)) - (coalesce(pscd.tax_amt,0) + "
        + " coalesce(sscd.tax_amt,0))) OVER (PARTITION BY ssd.sale_id)");
    field.setTable("ssm,ssd,pscd,sscd,isg");
    field.setDataType("numeric");
    field.setDecimalType("amount");
    field.setAggFunction("sum");
    field.setFilterable(true);
    field.setFieldUsesPartition(true);
    Map<String, Field> newfilds = new HashMap<String, Field>();
    newfilds.put("patient_amount", field);
    return newfilds;
  }

  /**
   * Adds the patient amt without tax.
   *
   * @return the map
   * @throws SQLException the SQL exception
   */
  private Map<String, Field> addPatientAmtWithoutTax() throws SQLException {
    Field field = new Field();
    field.setDisplayName("Patient Amount w/o Tax");
    field.setExpression("SUM(ssd.amount - (coalesce(pscd.insurance_claim_amt,0) + "
        + " coalesce(sscd.insurance_claim_amt,0)) - ssd.tax) "
        + " OVER (PARTITION BY ssd.sale_id)");
    field.setTable("ssd,pscd,sscd,isg");
    field.setDataType("numeric");
    field.setDecimalType("amount");
    field.setAggFunction("sum");
    field.setFilterable(true);
    field.setFieldUsesPartition(true);
    Map<String, Field> newfilds = new HashMap<String, Field>();
    newfilds.put("patient_amount_wo_tax", field);
    return newfilds;
  }

  /**
   * Adds the patient tax amount.
   *
   * @return the map
   * @throws SQLException the SQL exception
   */
  private Map<String, Field> addPatientTaxAmount() throws SQLException {
    Field field = new Field();
    field.setDisplayName("Patient Tax amount");
    field.setExpression("SUM(ssd.tax - (coalesce(pscd.tax_amt,0) + coalesce(sscd.tax_amt,0))) "
        + " OVER (PARTITION BY ssd.sale_id)");
    field.setTable("ssd,pscd,sscd,isg");
    field.setDataType("numeric");
    field.setDecimalType("amount");
    field.setAggFunction("sum");
    field.setFilterable(true);
    field.setFieldUsesPartition(true);
    Map<String, Field> newfilds = new HashMap<String, Field>();
    newfilds.put("patient_tax_amount", field);
    return newfilds;
  }

  /**
   * Adds the pri sponsor amount.
   *
   * @return the map
   * @throws SQLException the SQL exception
   */
  private Map<String, Field> addPriSponsorAmount() throws SQLException {
    Field field = new Field();
    field.setDisplayName("Pri. Sponsor Amount");
    field.setExpression("SUM(coalesce(pscd.insurance_claim_amt,0) + coalesce(pscd.tax_amt,0)) "
        + " OVER (PARTITION BY ssd.sale_id)");
    field.setTable("ssm,pscd,ssd,isg");
    field.setDataType("numeric");
    field.setDecimalType("amount");
    field.setAggFunction("sum");
    field.setFilterable(true);
    field.setFieldUsesPartition(true);
    Map<String, Field> newfilds = new HashMap<String, Field>();
    newfilds.put("pri_sponsor", field);
    return newfilds;
  }

  /**
   * Adds the sec sponsor amount.
   *
   * @return the map
   * @throws SQLException the SQL exception
   */
  private Map<String, Field> addSecSponsorAmount() throws SQLException {
    Field field = new Field();
    field.setDisplayName("Sec. Sponsor Amount");
    field.setExpression("SUM(coalesce(sscd.insurance_claim_amt,0) + coalesce(sscd.tax_amt,0)) "
        + " OVER (PARTITION BY ssd.sale_id)");
    field.setTable("ssm,sscd,ssd,isg");
    field.setDataType("numeric");
    field.setDecimalType("amount");
    field.setAggFunction("sum");
    field.setFilterable(true);
    field.setFieldUsesPartition(true);
    Map<String, Field> newfilds = new HashMap<String, Field>();
    newfilds.put("sec_sponsor", field);
    return newfilds;
  }

  /**
   * Adds the pri sponsor without tax amount.
   *
   * @return the map
   * @throws SQLException the SQL exception
   */
  private Map<String, Field> addPriSponsorWithoutTaxAmount() throws SQLException {
    Field field = new Field();
    field.setDisplayName("Pri. Sponsor Amount w/o Tax");
    field.setExpression("SUM(coalesce(pscd.insurance_claim_amt,0)) OVER "
        + " (PARTITION BY ssd.sale_id)");
    field.setTable("ssm,pscd,ssd,isg");
    field.setDataType("numeric");
    field.setDecimalType("amount");
    field.setAggFunction("sum");
    field.setFilterable(true);
    field.setFieldUsesPartition(true);
    Map<String, Field> newfilds = new HashMap<String, Field>();
    newfilds.put("pri_sponsor_wo_tax", field);
    return newfilds;
  }

  /**
   * Adds the sec sponsor without tax amount.
   *
   * @return the map
   * @throws SQLException the SQL exception
   */
  private Map<String, Field> addSecSponsorWithoutTaxAmount() throws SQLException {
    Field field = new Field();
    field.setDisplayName("Sec. Sponsor Amount w/o Tax");
    field.setExpression("SUM(coalesce(sscd.insurance_claim_amt,0)) OVER "
        + " (PARTITION BY ssd.sale_id)");
    field.setTable("ssm,sscd,ssd,isg");
    field.setDataType("numeric");
    field.setDecimalType("amount");
    field.setAggFunction("sum");
    field.setFilterable(true);
    field.setFieldUsesPartition(true);
    Map<String, Field> newfilds = new HashMap<String, Field>();
    newfilds.put("sec_sponsor_wo_tax", field);
    return newfilds;
  }

  /**
   * Adds the pri sponsor tax amount.
   *
   * @return the map
   * @throws SQLException the SQL exception
   */
  private Map<String, Field> addPriSponsorTaxAmount() throws SQLException {
    Field field = new Field();
    field.setDisplayName("Pri. Sponsor Tax Amount");
    field.setExpression(
        "SUM(coalesce(pscd.tax_amt,0)) OVER (PARTITION BY ssd.sale_id,isg.item_group_id)");
    field.setTable("ssm,pscd,ssd,isg");
    field.setDataType("numeric");
    field.setDecimalType("amount");
    field.setAggFunction("sum");
    field.setFilterable(true);
    field.setFieldUsesPartition(true);
    Map<String, Field> newfilds = new HashMap<String, Field>();
    newfilds.put("pri_sponsor_tax", field);
    return newfilds;
  }

  /**
   * Adds the sec sponsor tax amount.
   *
   * @return the map
   * @throws SQLException the SQL exception
   */
  private Map<String, Field> addSecSponsorTaxAmount() throws SQLException {
    Field field = new Field();
    field.setDisplayName("Sec. Sponsor Tax Amount");
    field.setExpression(
        "SUM(coalesce(sscd.tax_amt,0)) OVER (PARTITION BY ssd.sale_id)");
    field.setTable("ssm,sscd,ssd,isg");
    field.setDataType("numeric");
    field.setDecimalType("amount");
    field.setAggFunction("sum");
    field.setFilterable(true);
    field.setFieldUsesPartition(true);
    Map<String, Field> newfilds = new HashMap<String, Field>();
    newfilds.put("sec_sponsor_tax", field);
    return newfilds;
  }

}
