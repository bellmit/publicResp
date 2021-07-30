package com.insta.hms.stores;

import com.insta.hms.common.StdReportDesc;
import com.insta.hms.common.StdReportDescProvider;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The Class PurchaseItmesTaxReportHelper.
 */
public class PurchaseItmesTaxReportHelper implements StdReportDescProvider {

  /**
   * Instantiates a new purchase itmes tax report helper.
   */
  public PurchaseItmesTaxReportHelper() {

  }

  /**
   * getReportDesc method.
   * String descName the descName
   * 
   * @return StdReportDesc StdReportDesc
   */
  public StdReportDesc getReportDesc(String descName) throws Exception {

    StringBuilder query = new StringBuilder();
    ArrayList params = new ArrayList();
    makeQuery(query, params);
    ArrayList<String> queries = new ArrayList<String>();
    queries.add(query.toString());
    ArrayList<String> dateFieldsList = new ArrayList<String>();
    dateFieldsList.add("invoice_date");
    dateFieldsList.add("grn_date");
    StdReportDesc taxReportDesc = new StdReportDesc("Purchase Items Tax Report", queries, params,
        dateFieldsList, "Stores Reports", false, null, null);
    taxReportDesc.setDescription(
        "This report builder shows the pharmacy purchases tax-wise.\n\t\tIt reports one line for"
        + " every item in an invoice between the chosen date range\n\t\tIt includes debit notes,"
        + " but does not include returns and replacements.");
    taxReportDesc.addField("dept_name", "Store Name", 150, "string", true, true, true);
    taxReportDesc.addField("supplier_name", "Supplier Name", 150, "string", true, true, true);
    taxReportDesc.addField("cust_supplier_code", "Supplier Code", 150, "string", true, true, true);
    taxReportDesc.addField("supplier_tin_no", "Supplier GSTIN No", 120, "string", false, false,
        true);
    taxReportDesc.addField("drug_license_no", "Drug License No", 50, " string", false, false, true);
    taxReportDesc.addField("pan_no", "PAN No.", 50, " string", false, false, true);
    taxReportDesc.addField("cin_no", "CIN No.", 50, " string", false, false, true);
    taxReportDesc.addField("invoice_no", "Invoice No", 50, " string", false, false, true);
    taxReportDesc.addField("grn_date", "Grn Date", 70, " string", true, false, true);
    taxReportDesc.addField("invoice_date", "Invoice Date", 60, "String", true, false, true);
    taxReportDesc.addField("invoice_no_date", "Invoice No./Date", 120, "String", true, false, true);
    taxReportDesc.addField("tax_name", "Tax", 30, "string", true, true, true);
    taxReportDesc.addField("medicine_name", "Item Name", 120, "string", true, false, true);
    taxReportDesc.addField("cust_item_code", "Custom Item Code", 120, "string", true, false, true);
    taxReportDesc.addField("exempted", "Exempt Amt", 100, "numeric", false, false, true);
    taxReportDesc.addField("bill_value", "Total Bill Value", 100, "numeric", false, false, true);
    taxReportDesc.addField("store_type", "Store Type", 100, "String", true, true, true);
    taxReportDesc.addField("purchase_type", "Purchase Type", 100, "String", true, true, true);
    taxReportDesc.addField("form_8h", "Form 8H", 50, "String", false, true, true);
    taxReportDesc.addField("item_code", "Drug Code", 100, "string", false, true, true);
    taxReportDesc.addField("supplier_state", "Supplier State", 150, "string", false, true, true);
    taxReportDesc.addField("pharmacy_tin_no", "Store GSTIN No", 120, "string", false, true, true);

    taxReportDesc.addDefaultShowField("supplier_name");
    taxReportDesc.addDefaultShowField("cust_supplier_code");
    taxReportDesc.addDefaultShowField("invoice_no");
    taxReportDesc.addDefaultShowField("grn_date");
    taxReportDesc.addDefaultShowField("invoice_no_date");
    taxReportDesc.addDefaultShowField("medicine_name");
    taxReportDesc.addDefaultShowField("cust_item_code");
    taxReportDesc.addDefaultShowField("exempted");
    taxReportDesc.addDefaultShowField("bill_value");

    BasicDynaBean genPrefs = GenericPreferencesDAO.getAllPrefs();

    if (((Integer) genPrefs.get("max_centers_inc_default") > 1)) {
      taxReportDesc.addField("center_name", "Center Name", 100, "string", true, true, true);

      taxReportDesc.setFieldAllowedValuesQuery("center_name",
          "SELECT center_name FROM hospital_center_master order by center_name");

    }

    taxReportDesc.setFieldAllowedValuesQuery("supplier_name",
        "SELECT supplier_name FROM supplier_master order by supplier_name");
    taxReportDesc.setFieldAllowedValuesQuery("dept_name",
        "SELECT dept_name FROM stores order by dept_name");
    taxReportDesc.setFieldAllowedValues("purchase_type",
        Arrays.asList(new String[] { "Purchase", "Debit Note" }));
    taxReportDesc.setFieldAllowedValuesQuery("store_type",
        "SELECT distinct store_type_name FROM store_type_master order by store_type_name");
    taxReportDesc.setFieldAllowedValues("tax_name",
        Arrays.asList(genPrefs.get("procurement_tax_label").equals("V")
            ? new String[] { "VAT", "CST" }
            : new String[] { "GST", "iGST" }));
    taxReportDesc.setFieldAllowedValues("form_8h", Arrays.asList(new String[] { "Yes", "No" }));
    taxReportDesc.setReportGroup("Stores Reports");

    List<BasicDynaBean> list = DirectStockEntryDAO.getTaxTypesBean();
    int i1 = 0;
    for (BasicDynaBean bean : list) {
      BigDecimal rate = (BigDecimal) bean.get("tax_rate");
      String name = (String) bean.get("tax_name");
      String amtStr = name + " " + rate + "% " + "Amt"; // eg VAT 4% Amt
      String taxStr = name + " @" + rate + "%"; // eg VAT @4%
      taxReportDesc.addField("tax_" + i1 + "_amt", amtStr, 50, "numeric", "sum", "amount");
      taxReportDesc.addField("tax_" + i1 + "_tax", taxStr, 50, "numeric", "sum", "percent");
      i1++;
    }
    return taxReportDesc;
  }

  /** The select fields query. */
  private static String SELECT_FIELDS_QUERY = "SELECT date(grn_date) AS grn_date,"
      + " coalesce(smi.supplier_name, smd.supplier_name) AS supplier_name, "
      + " coalesce(smi.cust_supplier_code, smd.cust_supplier_code) AS cust_supplier_code,"
      + " coalesce(smi.drug_license_no, smd.drug_license_no) AS drug_license_no,"
      + " coalesce(smi.pan_no, smd.pan_no) AS pan_no,coalesce(smi.cin_no, smd.cin_no) AS cin_no,"
      + " coalesce(smi.supplier_tin_no, smd.supplier_tin_no) AS supplier_tin_no,"
      + " coalesce(i.tax_name,d.tax_name) AS tax_name, medicine_name,cust_item_code,"
      + " coalesce(i.invoice_no, d.debit_note_no) AS invoice_no,s.dept_name,hcm.center_name, "
      + " coalesce(i.invoice_date, d.debit_note_date)  AS invoice_date,stm.store_type_name"
      + " AS store_type, coalesce(i.invoice_no, d.debit_note_no) || ' /  ' || "
      + " to_char(coalesce(i.invoice_date, d.debit_note_date),'dd-MM-yyyy') AS invoice_no_date,"
      + " CASE WHEN (gm.supplier_invoice_id IS NULL) THEN 'Debit' ELSE 'Purchase' END AS"
      + " purchase_type, (g.cost_price*g.billed_qty/g.grn_pkg_size - (g.discount+"
      + " g.scheme_discount) + tax)::numeric(18,2) AS bill_value,"
      + " CASE WHEN gm.form_8h THEN 'Yes' ELSE 'No' END as form_8h, "
      + " CASE WHEN g.tax_rate=0 THEN (g.cost_price*g.billed_qty/g.grn_pkg_size - (g.discount+"
      + " g.scheme_discount))::numeric(18,2) ELSE 0 END AS exempted, "
      + " sic.item_code,smi.supplier_state,s.pharmacy_tin_no ";

  /** The from tables. */
  private static String FROM_TABLES = " FROM store_grn_details g "
      + " JOIN store_grn_main gm USING (grn_no) "
      + " JOIN store_item_details im USING (medicine_id) "
      + " LEFT JOIN ha_item_code_type hict ON (hict.medicine_id = im.medicine_id) "
      + " LEFT JOIN store_item_codes sic ON (sic.medicine_id = hict.medicine_id AND"
      + " sic.code_type = hict.code_type) "
      + " LEFT JOIN store_invoice i USING (supplier_invoice_id) "
      + " LEFT JOIN store_debit_note d USING (debit_note_no) "
      + " LEFT JOIN supplier_master smi ON (i.supplier_id = smi.supplier_code) "
      + " LEFT JOIN supplier_master smd ON (d.supplier_id = smd.supplier_code)"
      + " LEFT JOIN stores s ON(gm.store_id = s.dept_id) "
      + " LEFT JOIN store_type_master stm ON(s.store_type_id=stm.store_type_id)"
      + " LEFT JOIN hospital_center_master hcm ON(s.center_id = hcm.center_id)";

  /**
   * Make query.
   *
   * @param query the query
   * @param params the params
   * @throws SQLException the SQL exception
   */
  private void makeQuery(StringBuilder query, ArrayList params) throws SQLException {
    query.append(SELECT_FIELDS_QUERY);

    List<BasicDynaBean> list = DirectStockEntryDAO.getTaxTypesBean();
    int i1 = 0;
    for (BasicDynaBean bean : list) {
      BigDecimal rate = (BigDecimal) bean.get("tax_rate");
      String name = (String) bean.get("tax_name");
      String amtStr = name + " " + rate + "% " + "Amt";
      String taxStr = name + " @" + rate + "%";
      String amtName = "tax_" + i1 + "_amt";
      String taxName = "tax_" + i1 + "_tax";

      query.append(",CASE WHEN '" + name
          + "'='CST' THEN (CASE WHEN coalesce(i.cst_rate,d.cst_rate)=" + rate
          + " THEN (g.cost_price*g.billed_qty/g.grn_pkg_size- (g.discount+"
          + " g.scheme_discount))::numeric(18,2) ELSE 0 END) ELSE (CASE WHEN g.tax_rate="
          + rate + " AND coalesce(i.tax_name,d.tax_name)!='CST' THEN (g.cost_price*g.billed_qty"
          + "/g.grn_pkg_size- (g.discount+g.scheme_discount))::numeric(18,2) ELSE 0 END) END AS "
          + amtName);
      query.append(",CASE WHEN '" + name
          + "'='CST' THEN (CASE WHEN coalesce(i.cst_rate,d.cst_rate)=" + rate
          + " THEN (g.tax)::numeric(18,2) ELSE 0 END) ELSE (CASE WHEN g.tax_rate=" + rate
          + " AND coalesce(i.tax_name,d.tax_name)!='CST' THEN (g.tax)::numeric(18,2) ELSE 0 END )"
          + " END AS "
          + taxName);
      i1++;
    }
    query.append(FROM_TABLES);
  }

}
