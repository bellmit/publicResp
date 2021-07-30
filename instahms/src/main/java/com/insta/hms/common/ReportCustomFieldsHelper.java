package com.insta.hms.common;

import com.insta.hms.billing.BillChargeTaxDAO;
import com.insta.hms.common.StdReportDesc.Field;
import com.insta.hms.common.StdReportDesc.JoinTable;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class ReportCustomFieldsHelper.
 */
public class ReportCustomFieldsHelper {

  /** The bill charge tax DAO. */
  private static BillChargeTaxDAO billChargeTaxDAO = new BillChargeTaxDAO();

  /**
   * Adds the bill tax amt custom fields by bill.
   *
   * @return the map
   * @throws SQLException the SQL exception
   */
  public static Map<String, Field> addBillTaxAmtCustomFieldsByBill() throws SQLException {
    List<BasicDynaBean> list = billChargeTaxDAO.getItemGroupCodesName();
    Map<String, Field> newfilds = new HashMap<String, Field>();
    for (BasicDynaBean b : list) {
      Field field = new Field();
      String taxGroupId = String.valueOf(b.get("item_group_id"));
      String taxGroupName = (String) b.get("item_group_name");
      field.setDisplayName("Bill " + taxGroupName + " Tax Amt");
      field.setExpression("SUM(CASE WHEN tsgm.item_group_id = " + taxGroupId
          + " THEN bct.tax_amount ELSE 0 END) OVER (PARTITION BY b.bill_no)");
      field.setTable("tsgm");
      field.setDataType("numeric");
      field.setDecimalType("amount");
      field.setAggFunction("sum");
      field.setFilterable(true);
      field.setFieldUsesPartition(true);
      newfilds.put("bill_tax_" + taxGroupId, field);
    }
    return newfilds;
  }

  /**
   * Adds the bill tax amt custom fields by charge.
   *
   * @return the map
   * @throws SQLException the SQL exception
   */
  public static Map<String, Field> addBillTaxAmtCustomFieldsByCharge() throws SQLException {

    List<BasicDynaBean> list = billChargeTaxDAO.getItemGroupCodesName();
    Map<String, Field> newfilds = new HashMap<String, Field>();
    for (BasicDynaBean b : list) {
      Field field = new Field();
      String taxGroupId = String.valueOf(b.get("item_group_id"));
      String taxGroupName = (String) b.get("item_group_name");
      field.setDisplayName("Item " + taxGroupName + " Tax Amt");
      field.setExpression("SUM(CASE WHEN tsgm.item_group_id = " + taxGroupId
          + " THEN coalesce(bct.tax_amount,0) ELSE 0 END) OVER (PARTITION BY bc.charge_id)");
      field.setTable("tsgm");
      field.setDataType("numeric");
      field.setDecimalType("amount");
      field.setAggFunction("sum");
      field.setFilterable(true);
      field.setFieldUsesPartition(true);
      newfilds.put("item_tax_" + taxGroupId, field);
    }
    return newfilds;
  }

  /**
   * Adds the pri sponsor tax amt custom fields by bill.
   *
   * @return the map
   * @throws SQLException the SQL exception
   */
  public static Map<String, Field> addPriSponsorTaxAmtCustomFieldsByBill() throws SQLException {

    List<BasicDynaBean> list = billChargeTaxDAO.getItemGroupCodesName();
    Map<String, Field> newfilds = new HashMap<String, Field>();
    for (BasicDynaBean b : list) {
      Field field = new Field();
      String taxGroupId = String.valueOf(b.get("item_group_id"));
      String taxGroupName = (String) b.get("item_group_name");
      field.setDisplayName("Pri. Sponsor " + taxGroupName + " Tax Amt");
      field.setExpression("SUM(CASE WHEN tsgm.item_group_id = " + taxGroupId
          + " THEN pbcct.sponsor_tax_amount ELSE 0 END) OVER (PARTITION BY b.bill_no)");
      field.setTable("tsgm,bct,pbcct,pbcc");
      field.setDataType("numeric");
      field.setDecimalType("amount");
      field.setAggFunction("sum");
      field.setFilterable(true);
      field.setFieldUsesPartition(true);
      newfilds.put("pri_bill_tax_" + taxGroupId, field);
    }
    return newfilds;
  }

  /**
   * Adds the pri sponsor tax amt custom fields by charge.
   *
   * @return the map
   * @throws SQLException the SQL exception
   */
  public static Map<String, Field> addPriSponsorTaxAmtCustomFieldsByCharge() throws SQLException {

    List<BasicDynaBean> list = billChargeTaxDAO.getItemGroupCodesName();
    Map<String, Field> newfilds = new HashMap<String, Field>();
    for (BasicDynaBean b : list) {
      Field field = new Field();
      String taxGroupId = String.valueOf(b.get("item_group_id"));
      String taxGroupName = (String) b.get("item_group_name");
      field.setDisplayName("Pri. Sponsor " + taxGroupName + " Tax Amt");
      field.setExpression("SUM(CASE WHEN tsgm.item_group_id = " + taxGroupId
          + " THEN coalesce(pbcct.sponsor_tax_amount,0) ELSE 0 END) "
          + "OVER (PARTITION BY bc.charge_id)");
      field.setTable("tsgm,bct,pbcct,pbcl");
      field.setDataType("numeric");
      field.setDecimalType("amount");
      field.setAggFunction("sum");
      field.setFilterable(true);
      field.setFieldUsesPartition(true);
      newfilds.put("pri_bill_tax_" + taxGroupId, field);
    }
    return newfilds;
  }

  /**
   * Adds the sec sponsor tax amt custom fields by bill.
   *
   * @return the map
   * @throws SQLException the SQL exception
   */
  public static Map<String, Field> addSecSponsorTaxAmtCustomFieldsByBill() throws SQLException {

    List<BasicDynaBean> list = billChargeTaxDAO.getItemGroupCodesName();
    Map<String, Field> newfilds = new HashMap<String, Field>();
    for (BasicDynaBean b : list) {
      Field field = new Field();
      String taxGroupId = String.valueOf(b.get("item_group_id"));
      String taxGroupName = (String) b.get("item_group_name");
      field.setDisplayName("Sec. Sponsor " + taxGroupName + " Tax Amt");
      field.setExpression("SUM(CASE WHEN tsgm.item_group_id = " + taxGroupId
          + " THEN sbcct.sponsor_tax_amount ELSE 0 END) OVER (PARTITION BY b.bill_no)");
      field.setTable("tsgm,bct,sbcct,sbcc");
      field.setDataType("numeric");
      field.setDecimalType("amount");
      field.setAggFunction("sum");
      field.setFilterable(true);
      field.setFieldUsesPartition(true);
      newfilds.put("sec_bill_tax_" + taxGroupId, field);
    }
    return newfilds;
  }

  /**
   * Adds the sec sponsor tax amt custom fields by charge.
   *
   * @return the map
   * @throws SQLException the SQL exception
   */
  public static Map<String, Field> addSecSponsorTaxAmtCustomFieldsByCharge() throws SQLException {

    List<BasicDynaBean> list = billChargeTaxDAO.getItemGroupCodesName();
    Map<String, Field> newfilds = new HashMap<String, Field>();
    for (BasicDynaBean b : list) {
      Field field = new Field();
      String taxGroupId = String.valueOf(b.get("item_group_id"));
      String taxGroupName = (String) b.get("item_group_name");
      field.setDisplayName("Sec. Sponsor " + taxGroupName + " Tax Amt");
      field.setExpression("SUM(CASE WHEN tsgm.item_group_id = " + taxGroupId
          + " THEN coalesce(sbcct.sponsor_tax_amount,0) ELSE 0 END) "
          + "OVER (PARTITION BY bc.charge_id)");
      field.setTable("tsgm,bct,sbcct,sbcl");
      field.setDataType("numeric");
      field.setDecimalType("amount");
      field.setAggFunction("sum");
      field.setFilterable(true);
      field.setFieldUsesPartition(true);
      field.setFilterable(true);
      newfilds.put("sec_bill_tax_" + taxGroupId, field);
    }
    return newfilds;
  }

  /**
   * Adds the patient tax amt custom fields by bill.
   *
   * @return the map
   * @throws SQLException the SQL exception
   */
  public static Map<String, Field> addPatientTaxAmtCustomFieldsByBill() throws SQLException {

    List<BasicDynaBean> list = billChargeTaxDAO.getItemGroupCodesName();
    Map<String, Field> newfilds = new HashMap<String, Field>();
    for (BasicDynaBean b : list) {
      Field field = new Field();
      String taxGroupId = String.valueOf(b.get("item_group_id"));
      String taxGroupName = (String) b.get("item_group_name");
      field.setDisplayName("Patient " + taxGroupName + " Tax Amt");
      field.setExpression("SUM(CASE WHEN tsgm.item_group_id = " + taxGroupId
          + " THEN (coalesce(bct.tax_amount,0) - coalesce(pbcct.sponsor_tax_amount,0) - "
          + " coalesce(sbcct.sponsor_tax_amount,0)) ELSE 0 END) OVER (PARTITION BY b.bill_no)");
      field.setTable("tsgm,bct,sbcct,sbcc,pbcct,pbcc");
      field.setDataType("numeric");
      field.setDecimalType("amount");
      field.setAggFunction("sum");
      field.setFilterable(true);
      field.setFieldUsesPartition(true);
      newfilds.put("pat_bill_tax_" + taxGroupId, field);
    }
    return newfilds;
  }

  /**
   * Adds the patient tax amt custom fields by charge.
   *
   * @return the map
   * @throws SQLException the SQL exception
   */
  public static Map<String, Field> addPatientTaxAmtCustomFieldsByCharge() throws SQLException {

    List<BasicDynaBean> list = billChargeTaxDAO.getItemGroupCodesName();
    Map<String, Field> newfilds = new HashMap<String, Field>();
    for (BasicDynaBean b : list) {
      Field field = new Field();
      String taxGroupId = String.valueOf(b.get("item_group_id"));
      String taxGroupName = (String) b.get("item_group_name");
      field.setDisplayName("Patient " + taxGroupName + " Tax Amt");
      field.setExpression("SUM(CASE WHEN tsgm.item_group_id = " + taxGroupId
          + " THEN (coalesce(bct.tax_amount,0) - coalesce(pbcct.sponsor_tax_amount,0) - "
          + " coalesce(sbcct.sponsor_tax_amount,0)) ELSE 0 END) OVER (PARTITION BY bc.charge_id)");
      field.setTable("tsgm,bct,sbcct,sbcl,pbcct,pbcl");
      field.setDataType("numeric");
      field.setDecimalType("amount");
      field.setAggFunction("sum");
      field.setFilterable(true);
      field.setFieldUsesPartition(true);
      newfilds.put("pat_bill_tax_" + taxGroupId, field);
    }
    return newfilds;
  }

  /**
   * Adds the bill tax sub group custom fields by charge.
   *
   * @return the map
   * @throws SQLException the SQL exception
   */
  public static Map<String, Field> addBillTaxSubGroupCustomFieldsByCharge() throws SQLException {
    BillChargeTaxDAO billChargeTaxDAO = new BillChargeTaxDAO();
    List<BasicDynaBean> list = billChargeTaxDAO.getItemGroupCodesName();
    Map<String, Field> newfilds = new HashMap<>();
    for (BasicDynaBean b : list) {
      Field field = new Field();
      String taxGroupId = String.valueOf(b.get("item_group_id"));
      String taxGroupName = (String) b.get("item_group_name");
      field.setDisplayName("Tax Sub Group (" + taxGroupName + ")");
      field.setExpression("MAX(CASE WHEN tsgm.item_group_id = " + taxGroupId
          + " AND bc.charge_group != 'MED' AND bc.charge_group != 'RET' THEN "
          + "tsgm.item_subgroup_name ELSE '' END) OVER (PARTITION BY bc.charge_id)");
      field.setTable("tsgm");
      field.setFilterable(true);
      field.setFieldUsesPartition(true);
      newfilds.put("item_tax_sub_group" + taxGroupId, field);
    }
    return newfilds;
  }

  /**
   * Adds the bill item tax rate custom fields by charge.
   *
   * @return the map
   */
  public static Map<String, Field> addBillItemTaxRateCustomFieldsByCharge() {
    Field field = new Field();
    field.setDisplayName("Total Tax Rate %");
    field.setExpression("SUM(CASE WHEN bc.charge_group != 'MED' AND bc.charge_group != 'RET' THEN "
        + "coalesce(bct.tax_rate,0) END) OVER (PARTITION BY bc.charge_id)");
    field.setTable("bct");
    field.setDataType("numeric");
    field.setDecimalType("amount");
    field.setFilterable(true);
    field.setFieldUsesPartition(true);
    Field fid = new Field();
    fid.setDisplayName("Total Tax Rate %");
    fid.setExpression(
        "SUM(CASE WHEN bc.charge_group != 'MED' AND bc.charge_group != 'RET' "
        + "THEN coalesce(bct.tax_rate,0) END) OVER (PARTITION BY bc.charge_id)");
    fid.setTable("bct");
    fid.setDataType("numeric");
    fid.setDecimalType("amount");
    fid.setFilterable(true);
    fid.setGroupable(true);
    fid.setFieldUsesPartition(true);
    Map<String, Field> newfilds = new HashMap<>();
    newfilds.put("total_tax_rate", fid);
    return newfilds;
  }

  /**
   * Adds the tax custom join tables for bill.
   *
   * @return the list
   */
  public static List<JoinTable> addTaxCustomJoinTablesForBill() {
    JoinTable jt = new JoinTable();
    jt.setAlias("bc");
    jt.setName("bill_charge");
    jt.setExpression("ON (bc.bill_no = b.bill_no)");
    jt.setDependsOn("b");
    jt.setType("LEFT");
    List<JoinTable> jts = new ArrayList<JoinTable>();
    jts.add(jt);
    JoinTable jt1 = new JoinTable();
    jt1.setAlias("bct");
    jt1.setName("bill_charge_tax");
    jt1.setExpression("ON (bc.charge_id = bct.charge_id)");
    jt1.setDependsOn("bc");
    jt1.setType("LEFT");
    jts.add(jt1);
    JoinTable jt2 = new JoinTable();
    jt2.setAlias("tsgm");
    jt2.setName("item_sub_groups");
    jt2.setExpression("ON (tsgm.item_subgroup_id = bct.tax_sub_group_id)");
    jt2.setDependsOn("bct");
    jt2.setType("LEFT");
    jts.add(jt2);
    JoinTable jt4 = new JoinTable();
    jt4.setAlias("pbcc");
    jt4.setName("bill_claim");
    jt4.setExpression("ON (pbcc.bill_no = b.bill_no AND pbcc.priority = 1)");
    jt4.setDependsOn("pbcct");
    jt4.setType("LEFT");
    jts.add(jt4);
    JoinTable jt3 = new JoinTable();
    jt3.setAlias("pbcct");
    jt3.setName("bill_charge_claim_tax");
    jt3.setExpression("ON (pbcc.claim_id = pbcct.claim_id AND bc.charge_id = pbcct.charge_id AND "
        + "pbcct.tax_sub_group_id = bct.tax_sub_group_id)");
    jt3.setDependsOn("bc,bct,pbcc");
    jt3.setType("LEFT");
    jts.add(jt3);
    JoinTable jt6 = new JoinTable();
    jt6.setAlias("sbcc");
    jt6.setName("bill_claim");
    jt6.setExpression("ON (sbcc.bill_no = b.bill_no AND sbcc.priority = 2)");
    jt6.setDependsOn("sbcct");
    jt6.setType("LEFT");
    jts.add(jt6);
    JoinTable jt5 = new JoinTable();
    jt5.setAlias("sbcct");
    jt5.setName("bill_charge_claim_tax");
    jt5.setExpression("ON (sbcc.claim_id = sbcct.claim_id AND bc.charge_id = sbcct.charge_id AND "
        + "sbcct.tax_sub_group_id = bct.tax_sub_group_id)");
    jt5.setDependsOn("bc,bct,sbcc");
    jt5.setType("LEFT");
    jts.add(jt5);
    return jts;
  }

  /**
   * Adds the tax custom join tables for charge.
   *
   * @return the list
   */
  public static List<JoinTable> addTaxCustomJoinTablesForCharge() {
    JoinTable jt1 = new JoinTable();
    jt1.setAlias("bct");
    jt1.setName("bill_charge_tax");
    jt1.setExpression("ON (bc.charge_id = bct.charge_id)");
    jt1.setDependsOn("bc");
    jt1.setType("LEFT");
    List<JoinTable> jts = new ArrayList<JoinTable>();
    jts.add(jt1);
    JoinTable jt2 = new JoinTable();
    jt2.setAlias("tsgm");
    jt2.setName("item_sub_groups");
    jt2.setExpression("ON (tsgm.item_subgroup_id = bct.tax_sub_group_id)");
    jt2.setDependsOn("bct");
    jt2.setType("LEFT");
    jts.add(jt2);
    /*
     * JoinTable jt4=new JoinTable(); jt4.setAlias("pbcc"); jt4.setName("bill_claim");
     * jt4.setExpression("ON (pbcc.bill_no = b.bill_no AND pbcc.priority = 1)");
     * jt4.setDependsOn("pbcct"); jt4.setType("LEFT"); jts.add(jt4);
     */
    JoinTable jt3 = new JoinTable();
    jt3.setAlias("pbcct");
    jt3.setName("bill_charge_claim_tax");
    jt3.setExpression("ON (pbcl.claim_id = pbcct.claim_id AND bc.charge_id = pbcct.charge_id AND "
        + "pbcct.tax_sub_group_id = bct.tax_sub_group_id)");
    jt3.setDependsOn("bc,bct,pbcl");
    jt3.setType("LEFT");
    jts.add(jt3);
    /*
     * JoinTable jt6=new JoinTable(); jt6.setAlias("sbcc"); jt6.setName("bill_claim");
     * jt6.setExpression("ON (sbcc.bill_no = b.bill_no AND sbcc.priority = 2)");
     * jt6.setDependsOn("sbcct"); jt6.setType("LEFT"); jts.add(jt6);
     */
    JoinTable jt5 = new JoinTable();
    jt5.setAlias("sbcct");
    jt5.setName("bill_charge_claim_tax");
    jt5.setExpression("ON (sbcl.claim_id = sbcct.claim_id AND bc.charge_id = sbcct.charge_id AND "
        + "sbcct.tax_sub_group_id = bct.tax_sub_group_id)");
    jt5.setDependsOn("bc,bct,sbcl");
    jt5.setType("LEFT");
    jts.add(jt5);
    return jts;
  }

  /**
   * Adds the tax custom join tables for ins.
   *
   * @return the list
   */
  public static List<JoinTable> addTaxCustomJoinTablesForIns() {
    JoinTable jt1 = new JoinTable();
    jt1.setAlias("bct");
    jt1.setName("bill_charge_tax");
    jt1.setExpression("ON (bcc.charge_id = bct.charge_id)");
    jt1.setDependsOn("bcc");
    jt1.setType("LEFT");
    List<JoinTable> jts = new ArrayList<JoinTable>();
    jts.add(jt1);

    JoinTable jt2 = new JoinTable();
    jt2.setAlias("tsgm");
    jt2.setName("item_sub_groups");
    jt2.setExpression("ON (tsgm.item_subgroup_id = bct.tax_sub_group_id)");
    jt2.setDependsOn("bct");
    jt2.setType("LEFT");
    jts.add(jt2);

    JoinTable jt3 = new JoinTable();
    jt3.setAlias("pbcct");
    jt3.setName("bill_charge_claim_tax");
    jt3.setExpression("ON (pbcl.claim_id = pbcct.claim_id AND bcc.charge_id = pbcct.charge_id AND "
        + "pbcct.tax_sub_group_id = bct.tax_sub_group_id)");
    jt3.setDependsOn("bcc,bct,pbcl");
    jt3.setType("LEFT");
    jts.add(jt3);

    JoinTable jt5 = new JoinTable();
    jt5.setAlias("sbcct");
    jt5.setName("bill_charge_claim_tax");
    jt5.setExpression("ON (sbcl.claim_id = sbcct.claim_id AND bcc.charge_id = sbcct.charge_id AND "
        + "sbcct.tax_sub_group_id = bct.tax_sub_group_id)");
    jt5.setDependsOn("bcc,bct,sbcl");
    jt5.setType("LEFT");
    jts.add(jt5);
    return jts;
  }

  /**
   * Adds the tax custom join tbls for ins store.
   *
   * @return the list
   */
  public static List<JoinTable> addTaxCustomJoinTblsForInsStore() {
    JoinTable jt1 = new JoinTable();
    jt1.setAlias("sstd");
    jt1.setName("store_sales_tax_details");
    jt1.setExpression("ON (sstd.sale_item_id = s.sale_item_id)");
    jt1.setDependsOn("s");
    jt1.setType("LEFT");
    List<JoinTable> jts = new ArrayList<JoinTable>();
    jts.add(jt1);

    JoinTable jt2 = new JoinTable();
    jt2.setAlias("tsgm");
    jt2.setName("item_sub_groups");
    jt2.setExpression("ON (tsgm.item_subgroup_id = sstd.item_subgroup_id)");
    jt2.setDependsOn("sstd");
    jt2.setType("LEFT");
    jts.add(jt2);

    JoinTable jt3 = new JoinTable();
    jt3.setAlias("psctd");
    jt3.setName("sales_claim_tax_details");
    jt3.setExpression("ON (psctd.sale_item_id = s.sale_item_id AND psctd.claim_id = pbcl.claim_id "
        + " AND psctd.item_subgroup_id = sstd.item_subgroup_id)");
    jt3.setDependsOn("s,sstd,pbcl");
    jt3.setType("LEFT");
    jts.add(jt3);

    JoinTable jt5 = new JoinTable();
    jt5.setAlias("ssctd");
    jt5.setName("sales_claim_tax_details");
    jt5.setExpression("ON (ssctd.sale_item_id = s.sale_item_id AND ssctd.claim_id = sbcl.claim_id "
        + " AND ssctd.item_subgroup_id = sstd.item_subgroup_id)");
    jt5.setDependsOn("s,sstd,sbcl");
    jt5.setType("LEFT");
    jts.add(jt5);
    return jts;
  }

}
