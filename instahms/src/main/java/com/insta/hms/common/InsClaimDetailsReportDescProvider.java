package com.insta.hms.common;

import com.insta.hms.billing.BillChargeTaxDAO;
import com.insta.hms.common.StdReportDesc.Field;
import com.insta.hms.common.StdReportDesc.FieldExpression;
import com.insta.hms.common.StdReportDesc.JoinTable;
import com.insta.hms.common.StdReportDesc.QueryUnit;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class InsClaimDetailsReportDescProvider.
 */
public class InsClaimDetailsReportDescProvider extends StdReportDescJsonProvider {

  /**
   * Adds the tax custom fields.
   *
   * @return the map
   * @throws SQLException the SQL exception
   */
  public Map<String, Field> addTaxCustomFields() throws SQLException {
    Map<String, Field> fields = new HashMap<String, Field>();
    fields.putAll(addBillTaxAmtCustomFieldsByCharge());
    fields.putAll(addPriSponsorTaxAmtCustomFieldsByCharge());
    fields.putAll(addSecSponsorTaxAmtCustomFieldsByCharge());
    fields.putAll(addPatientTaxAmtCustomFieldsByCharge());
    return fields;
  }

  /**
   * Tax custom flds expression for store.
   *
   * @return the map
   * @throws SQLException the SQL exception
   */
  public Map<String, FieldExpression> taxCustomFldsExpressionForStore() throws SQLException {
    Map<String, FieldExpression> fields = new HashMap<String, FieldExpression>();
    fields.putAll(billTaxAmtCustomFldsForStoreItem());
    fields.putAll(priSponsorTaxAmtCustomFldsForStoreItem());
    fields.putAll(secSponsorTaxAmtCustomFldsForStoreItem());
    fields.putAll(patientTaxAmtCustomFldsForStoreItem());
    return fields;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.common.StdReportDescJsonProvider#getReportDesc(java.lang.String)
   */
  @Override
  public StdReportDesc getReportDesc(String descName) throws Exception {
    StdReportDesc reportDesc = super.getReportDesc(descName);
    QueryUnit qu = reportDesc.getQueryUnits().get(0);
    String mainTableName1 = qu.getMainTableName();
    List<JoinTable> jt = qu.getJoinTables();
    // mainTableName: "store_sales_details" & mainTableName: "bill_charge"
    if ("store_sales_details".equals(mainTableName1)) {
      jt.addAll(ReportCustomFieldsHelper.addTaxCustomJoinTblsForInsStore());
      qu.setDynamicFieldsPartitionedOn("s.sale_item_id");
      qu.setJoinTables(jt);
      Map<String, FieldExpression> fe = qu.getFieldExpressions();
      fe.putAll(taxCustomFldsExpressionForStore());
    } else {
      jt.addAll(ReportCustomFieldsHelper.addTaxCustomJoinTablesForIns());
      qu.setDynamicFieldsPartitionedOn("bcc.charge_id");
      qu.setJoinTables(jt);
      reportDesc.getFields().putAll(addTaxCustomFields());
    }
    if (reportDesc.getQueryUnits().size() > 1) {
      QueryUnit qu1 = reportDesc.getQueryUnits().get(1);
      String mainTableName2 = qu1.getMainTableName();
      List<JoinTable> jt1 = qu1.getJoinTables();
      if ("store_sales_details".equals(mainTableName2)) {
        jt1.addAll(ReportCustomFieldsHelper.addTaxCustomJoinTblsForInsStore());
        qu1.setDynamicFieldsPartitionedOn("s.sale_item_id");
        qu1.setJoinTables(jt1);
        Map<String, FieldExpression> fe = qu1.getFieldExpressions();
        fe.putAll(taxCustomFldsExpressionForStore());
      } else {
        jt1.addAll(ReportCustomFieldsHelper.addTaxCustomJoinTablesForIns());
        qu1.setDynamicFieldsPartitionedOn("bcc.charge_id");
        qu1.setJoinTables(jt1);
        reportDesc.getFields().putAll(addTaxCustomFields());
      }
    }

    reportDesc.validate();
    return reportDesc;
  }

  /**
   * Adds the bill tax amt custom fields by charge.
   *
   * @return the map
   * @throws SQLException the SQL exception
   */
  public static Map<String, Field> addBillTaxAmtCustomFieldsByCharge() throws SQLException {
    BillChargeTaxDAO billChargeTaxDAO = new BillChargeTaxDAO();
    List<BasicDynaBean> list = billChargeTaxDAO.getItemGroupCodesName();
    Map<String, Field> newfilds = new HashMap<String, Field>();
    for (BasicDynaBean b : list) {
      Field field = new Field();
      String taxGroupId = String.valueOf(b.get("item_group_id"));
      String taxGroupName = (String) b.get("item_group_name");
      field.setDisplayName("Item " + taxGroupName + " Tax Amt");
      field.setExpression("SUM(CASE WHEN tsgm.item_group_id = " + taxGroupId
          + " THEN bct.tax_amount ELSE 0 END) OVER (PARTITION BY bcc.charge_id)");
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
   * Adds the pri sponsor tax amt custom fields by charge.
   *
   * @return the map
   * @throws SQLException the SQL exception
   */
  public static Map<String, Field> addPriSponsorTaxAmtCustomFieldsByCharge() throws SQLException {
    BillChargeTaxDAO billChargeTaxDAO = new BillChargeTaxDAO();
    List<BasicDynaBean> list = billChargeTaxDAO.getItemGroupCodesName();
    Map<String, Field> newfilds = new HashMap<String, Field>();
    for (BasicDynaBean b : list) {
      Field field = new Field();
      String taxGroupId = String.valueOf(b.get("item_group_id"));
      String taxGroupName = (String) b.get("item_group_name");
      field.setDisplayName("Pri. Sponsor " + taxGroupName + " Tax Amt");
      field.setExpression("SUM(CASE WHEN tsgm.item_group_id = " + taxGroupId
          + " THEN pbcct.sponsor_tax_amount ELSE 0 END) OVER (PARTITION BY bcc.charge_id)");
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
   * Adds the sec sponsor tax amt custom fields by charge.
   *
   * @return the map
   * @throws SQLException the SQL exception
   */
  public static Map<String, Field> addSecSponsorTaxAmtCustomFieldsByCharge() throws SQLException {
    BillChargeTaxDAO billChargeTaxDAO = new BillChargeTaxDAO();
    List<BasicDynaBean> list = billChargeTaxDAO.getItemGroupCodesName();
    Map<String, Field> newfilds = new HashMap<String, Field>();
    for (BasicDynaBean b : list) {
      Field field = new Field();
      String taxGroupId = String.valueOf(b.get("item_group_id"));
      String taxGroupName = (String) b.get("item_group_name");
      field.setDisplayName("Sec. Sponsor " + taxGroupName + " Tax Amt");
      field.setExpression("SUM(CASE WHEN tsgm.item_group_id = " + taxGroupId
          + " THEN sbcct.sponsor_tax_amount ELSE 0 END) OVER (PARTITION BY bcc.charge_id)");
      field.setTable("tsgm,bct,sbcct,sbcl");
      field.setDataType("numeric");
      field.setDecimalType("amount");
      field.setAggFunction("sum");
      field.setFieldUsesPartition(true);
      field.setFilterable(true);
      newfilds.put("sec_bill_tax_" + taxGroupId, field);
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
    BillChargeTaxDAO billChargeTaxDAO = new BillChargeTaxDAO();
    List<BasicDynaBean> list = billChargeTaxDAO.getItemGroupCodesName();
    Map<String, Field> newfilds = new HashMap<String, Field>();
    for (BasicDynaBean b : list) {
      Field field = new Field();
      String taxGroupId = String.valueOf(b.get("item_group_id"));
      String taxGroupName = (String) b.get("item_group_name");
      field.setDisplayName("Patient " + taxGroupName + " Tax Amt");
      field.setExpression("SUM(CASE WHEN tsgm.item_group_id = " + taxGroupId
          + " THEN (coalesce(bct.tax_amount,0) - coalesce(pbcct.sponsor_tax_amount,0) - "
          + "coalesce(sbcct.sponsor_tax_amount,0)) ELSE 0 END) OVER (PARTITION BY bcc.charge_id)");
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
   * Bill tax amt custom flds for store item.
   *
   * @return the map
   * @throws SQLException the SQL exception
   */
  public static Map<String, FieldExpression> billTaxAmtCustomFldsForStoreItem()
      throws SQLException {
    BillChargeTaxDAO billChargeTaxDAO = new BillChargeTaxDAO();
    List<BasicDynaBean> list = billChargeTaxDAO.getItemGroupCodesName();
    Map<String, FieldExpression> newfilds = new HashMap<String, FieldExpression>();
    for (BasicDynaBean b : list) {
      FieldExpression field = new FieldExpression();
      String taxGroupId = String.valueOf(b.get("item_group_id"));
      String taxGroupName = (String) b.get("item_group_name");
      // f.setDisplayName("Item "+taxGroupName + " Tax Amt");
      field.setExpression("SUM(CASE WHEN tsgm.item_group_id = " + taxGroupId
          + " AND ssm.type = 'R' THEN -sstd.tax_amt WHEN tsgm.item_group_id = " + taxGroupId
          + " THEN sstd.tax_amt ELSE 0 END) OVER (PARTITION BY s.sale_item_id)");
      field.setTable("tsgm");
      // f.setDataType("numeric");
      // f.setDecimalType("amount");
      // f.setAggFunction("sum");
      // f.setFieldUsesPartition(true);
      newfilds.put("item_tax_" + taxGroupId, field);
    }
    return newfilds;
  }

  /**
   * Pri sponsor tax amt custom flds for store item.
   *
   * @return the map
   * @throws SQLException the SQL exception
   */
  public static Map<String, FieldExpression> priSponsorTaxAmtCustomFldsForStoreItem()
      throws SQLException {
    BillChargeTaxDAO billChargeTaxDAO = new BillChargeTaxDAO();
    List<BasicDynaBean> list = billChargeTaxDAO.getItemGroupCodesName();
    Map<String, FieldExpression> newfilds = new HashMap<String, FieldExpression>();
    for (BasicDynaBean b : list) {
      FieldExpression field = new FieldExpression();
      String taxGroupId = String.valueOf(b.get("item_group_id"));
      String taxGroupName = (String) b.get("item_group_name");
      // f.setDisplayName("Pri. Sponsor "+taxGroupName + " Tax Amt");
      field.setExpression("SUM(CASE WHEN tsgm.item_group_id = " + taxGroupId
          + " THEN psctd.tax_amt ELSE 0 END) OVER (PARTITION BY s.sale_item_id)");
      field.setTable("tsgm,pbcl,psctd");
      // f.setDataType("numeric");
      // f.setDecimalType("amount");
      // f.setAggFunction("sum");
      // f.setFieldUsesPartition(true);
      newfilds.put("pri_bill_tax_" + taxGroupId, field);
    }
    return newfilds;
  }

  /**
   * Sec sponsor tax amt custom flds for store item.
   *
   * @return the map
   * @throws SQLException the SQL exception
   */
  public static Map<String, FieldExpression> secSponsorTaxAmtCustomFldsForStoreItem()
      throws SQLException {
    BillChargeTaxDAO billChargeTaxDAO = new BillChargeTaxDAO();
    List<BasicDynaBean> list = billChargeTaxDAO.getItemGroupCodesName();
    Map<String, FieldExpression> newfilds = new HashMap<String, FieldExpression>();
    for (BasicDynaBean b : list) {
      FieldExpression field = new FieldExpression();
      String taxGroupId = String.valueOf(b.get("item_group_id"));
      String taxGroupName = (String) b.get("item_group_name");
      // f.setDisplayName("Sec. Sponsor "+taxGroupName + " Tax Amt");
      field.setExpression("SUM(CASE WHEN tsgm.item_group_id = " + taxGroupId
          + " THEN ssctd.tax_amt ELSE 0 END) OVER (PARTITION BY s.sale_item_id)");
      field.setTable("tsgm,sbcl,ssctd");
      // f.setDataType("numeric");
      // f.setDecimalType("amount");
      // f.setAggFunction("sum");
      // f.setFieldUsesPartition(true);
      newfilds.put("sec_bill_tax_" + taxGroupId, field);
    }
    return newfilds;
  }

  /**
   * Patient tax amt custom flds for store item.
   *
   * @return the map
   * @throws SQLException the SQL exception
   */
  public static Map<String, FieldExpression> patientTaxAmtCustomFldsForStoreItem()
      throws SQLException {
    BillChargeTaxDAO billChargeTaxDAO = new BillChargeTaxDAO();
    List<BasicDynaBean> list = billChargeTaxDAO.getItemGroupCodesName();
    Map<String, FieldExpression> newfilds = new HashMap<String, FieldExpression>();
    for (BasicDynaBean b : list) {
      FieldExpression field = new FieldExpression();
      String taxGroupId = String.valueOf(b.get("item_group_id"));
      String taxGroupName = (String) b.get("item_group_name");
      // f.setDisplayName("Sec. Sponsor "+taxGroupName + " Tax Amt");
      field.setExpression("SUM(CASE WHEN tsgm.item_group_id = " + taxGroupId
          + " AND ssm.type = 'R' THEN -(coalesce(sstd.tax_amt,0) - coalesce(psctd.tax_amt,0) - "
          + " coalesce(ssctd.tax_amt,0)) WHEN tsgm.item_group_id = " + taxGroupId
          + " THEN (coalesce(sstd.tax_amt,0) - coalesce(psctd.tax_amt,0) - "
          + " coalesce(ssctd.tax_amt,0)) ELSE 0 END) OVER (PARTITION BY s.sale_item_id)");
      field.setTable("tsgm,sstd,psctd,sbcl,ssctd,pbcl");
      // f.setDataType("numeric");
      // f.setDecimalType("amount");
      // f.setAggFunction("sum");
      // f.setFieldUsesPartition(true);
      newfilds.put("pat_bill_tax_" + taxGroupId, field);
    }
    return newfilds;
  }

}
