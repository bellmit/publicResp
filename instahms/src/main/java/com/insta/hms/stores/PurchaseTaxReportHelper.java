package com.insta.hms.stores;

import com.insta.hms.common.StdReportDesc;
import com.insta.hms.common.StdReportDescProvider;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PurchaseTaxReportHelper implements StdReportDescProvider {

  public PurchaseTaxReportHelper() {

  }

  public StdReportDesc getReportDesc(String reportName) throws Exception {

    StringBuilder query = new StringBuilder();
    ArrayList params = new ArrayList();
    makeQuery(query, params);
    ArrayList<String> queries = new ArrayList<String>();
    queries.add(query.toString());
    BasicDynaBean genPrefs = GenericPreferencesDAO.getAllPrefs();
    ArrayList<String> dateFieldsList = new ArrayList<String>();
    dateFieldsList.add("invoice_date");
    StdReportDesc taxReportDesc = new StdReportDesc("Purchase Invoice Tax Report", queries, params,
        dateFieldsList, "Stores Reports", false, null, null);
    taxReportDesc.setDescription(
        "This report builder shows the pharmacy purchases tax-wise.\n\t\tIt reports one line for every invoice between the chosen date range\n\t\tIt includes debit notes, but does not include returns and replacements.");
    taxReportDesc.addField("supplier_name", "Supplier Name", 180, "string", true, true, true);
    taxReportDesc.addField("cust_supplier_code", "Supplier Code", 180, "string", true, true, true);
    taxReportDesc.addField("supplier_tin_no", "Tin No", 120, "string", false, false, true);
    taxReportDesc.addField("drug_license_no", "Drug License No", 120, "string", false, false, true);
    taxReportDesc.addField("pan_no", "PAN No.", 120, "string", false, false, true);
    taxReportDesc.addField("cin_no", "CIN No.", 120, "string", false, false, true);
    taxReportDesc.addField("invoice_no", "Invoice No", 50, " string", false, false, true);
    taxReportDesc.addField("invoice_date", "Invoice Date", 60, "date", true, false, true); // ask
                                                                                           // vasan
                                                                                           // is't
                                                                                           // necessary
                                                                                           // for
                                                                                           // filterable
                                                                                           // ...
    taxReportDesc.addField("tax_name", "Tax", 25, "string", true, true, true);
    taxReportDesc.addField("store", "Store", 120, "String", true, true, true);
    taxReportDesc.addField("store_type_name", "Store Type", 100, "String", true, true, true);
    taxReportDesc.addField("grn_nos", "GRN Nos", 60, "String", false, false, true);
    taxReportDesc.addField("grn_date", "GRN Date", 60, "date", false, false, true);
    taxReportDesc.addField("grn_tax", "Total Item Tax", 50, "numeric", "sum", "amount");
    taxReportDesc.addField("grn_total_tax", "Total Tax", 50, "numeric", "sum", "amount");
    taxReportDesc.addField("cess_tax_amt", "Cess", 50, "numeric", "sum", "amount");
    taxReportDesc.addField("grn_amt", "Pre Tax Amt", 50, "numeric", "sum", "amount");
    taxReportDesc.addField("grn_total_amt", "Total Amt", 50, "numeric", "sum", "amount");
    taxReportDesc.addField("exempt_amt", "Exempt Amt", 50, "numeric", "sum", "amount");
    taxReportDesc.addField("ced_tax", "CED Amt", 50, "numeric", "sum", "amount");
    taxReportDesc.addField("purch_or_debit", "Purchase/Debit", 70, "string", true, true, true);
    taxReportDesc.addField("invoice_discount", "Discount", 50, "numeric", "sum", "amount");
    taxReportDesc.addField("tcs_amount", "TCS Amt", 50, "numeric", "sum", "amount");
    taxReportDesc.addField("invoice_roundoff", "Round Off", 50, "numeric", "sum", "amount");
    taxReportDesc.addField("other_charges", "Other Charges", 50, "numeric", "sum", "amount");
    taxReportDesc.addField("form_8h", "Form 8H", 50, "string", false, true, true);

    taxReportDesc.addDefaultShowField("supplier_name");
    taxReportDesc.addDefaultShowField("cust_supplier_code");
    taxReportDesc.addDefaultShowField("supplier_tin_no");
    taxReportDesc.addDefaultShowField("invoice_no");
    taxReportDesc.addDefaultShowField("invoice_date");
    taxReportDesc.addDefaultShowField("tax_name");
    taxReportDesc.addDefaultShowField("grn_date");
    taxReportDesc.addDefaultShowField("grn_tax");
    taxReportDesc.addDefaultShowField("grn_amt");
    taxReportDesc.addDefaultShowField("grn_total_amt");
    taxReportDesc.addDefaultShowField("exempt_amt");
    taxReportDesc.addDefaultShowField("purch_or_debit");

    if (((Integer) genPrefs.get("max_centers_inc_default") > 1)) {
      taxReportDesc.addField("center_name", "Center Name", 70, "string", true, true, true);

      taxReportDesc.setFieldAllowedValuesQuery("center_name",
          "SELECT center_name FROM hospital_center_master order by center_name");

    }

    taxReportDesc.setFieldAllowedValues("tax_name",
        Arrays.asList(genPrefs.get("procurement_tax_label").equals("V")
            ? new String[] { "VAT", "CST" }
            : new String[] { "GST", "iGST" }));
    taxReportDesc.setFieldAllowedValues("purch_or_debit",
        Arrays.asList(new String[] { "Purchase", "Debit" }));
    taxReportDesc.setFieldAllowedValuesQuery("store",
        "SELECT dept_name FROM stores order by dept_name");
    taxReportDesc.setFieldAllowedValuesQuery("store_type_name",
        "SELECT store_type_name FROM store_type_master order by store_type_name");
    taxReportDesc.setFieldAllowedValuesQuery("supplier_name",
        "SELECT supplier_name FROM supplier_master order by supplier_name");
    taxReportDesc.setFieldAllowedValues("form_8h", Arrays.asList(new String[] { "Yes", "No" }));
    taxReportDesc.setReportGroup("Stores Reports");

    List<BasicDynaBean> l = DirectStockEntryDAO.getTaxTypesBean();
    int i = 0;
    for (BasicDynaBean b : l) {
      BigDecimal rate = (BigDecimal) b.get("tax_rate");
      String name = (String) b.get("tax_name");
      String amtStr = name + " " + rate + "% " + "Amt"; // eg VAT 4% Amt
      String taxStr = name + " @" + rate + "%"; // eg VAT @4%
      taxReportDesc.addField("tax_" + i + "_amt", amtStr, 50, "numeric", "sum", "amount");
      taxReportDesc.addField("tax_" + i + "_tax", taxStr, 50, "numeric", "sum", "percent");
      i++;
    }
    return taxReportDesc;
  }

  private static String SELECT_ALL = "SELECT *  FROM (  ";

  private static String SELECT_ALL_END = ") AS ";

  private static final String STATIC_FIELDS = "SELECT "
      + " 'Purchase'::text AS purch_or_debit, s.supplier_name,s.cust_supplier_code, coalesce(supplier_tin_no,'') as supplier_tin_no, "
      + "  s.drug_license_no,s.pan_no,s.cin_no,i.invoice_no, i.invoice_date, "
      + " i.tax_name, t.store, hcm.center_name, sty.store_type_name, t.grn_nos, date(t.grn_date) as grn_date, t.grn_tax,CASE WHEN t.form_8h THEN 'Yes' ELSE 'No' END as form_8h, "
      + " (t.grn_tax*i.cess_tax_rate)/100 as cess_tax_amt, t.grn_amt,"
      + " (t.grn_tax+t.grn_amt+(t.grn_tax*i.cess_tax_rate)/100 +t.ced_tax+i.round_off+i.other_charges-i.discount + i.tcs_amount) as grn_total_amt,"
      + " t.ced_tax, i.discount as invoice_discount, i.tcs_amount,t.grn_tax+i.tcs_amount as grn_total_tax, i.round_off as invoice_roundoff,i.other_charges, "
      + " coalesce((SELECT sum(cost_price*(billed_qty/grn_pkg_size)-(discount+scheme_discount)) "
      + "  FROM store_grn_details " + "   JOIN store_grn_main USING (grn_no) "
      + "  WHERE supplier_invoice_id=i.supplier_invoice_id AND tax_rate = 0),0) AS exempt_amt ";

  private static final String DEBIT_STATIC_FIELDS = " SELECT   'Debit'::text AS purch_or_debit, s.supplier_name,s.cust_supplier_code,coalesce(supplier_tin_no,'') as supplier_tin_no, "
      + " s.drug_license_no,s.pan_no,s.cin_no,"
      + " d.debit_note_no AS invoice_no, d.debit_note_date AS invoice_date, d.tax_name, "
      + " t.store,hcm.center_name,sty.store_type_name,t.grn_nos, date(t.grn_date) as grn_date, t.grn_tax,CASE WHEN t.form_8h THEN 'Yes' ELSE 'No' END as form_8h, 0 as cess_tax_amt, t.grn_amt, "
      + " (t.grn_amt+t.grn_tax+t.ced_tax-d.round_off-d.other_charges+d.discount) as grn_total_amt, t.ced_tax, "
      + " d.discount as invoice_discount, 0 as tcs_amount,t.grn_tax as grn_total_tax, d.round_off as invoice_roundoff,d.other_charges, "
      + " coalesce((SELECT sum(cost_price*(billed_qty/grn_pkg_size)-(discount+scheme_discount)) "
      + " FROM store_grn_details  " + " JOIN store_grn_main USING (grn_no)  "
      + " WHERE debit_note_no =d.debit_note_no AND tax_rate = 0),0) AS exempt_amt ";

  private static final String DEBIT_FROM_TABLES = " FROM    " + " store_debit_note d   "
      + " JOIN supplier_master s ON (s.supplier_code = d.supplier_id)  " + " JOIN   "
      + " (	SELECT d.debit_note_no,   "
      + " 		sum(g.cost_price*g.billed_qty/g.grn_pkg_size - (g.discount+g.scheme_discount))::numeric(18,2) AS grn_amt,  "
      + " 		sum(g.tax) AS grn_tax,  " + " 		sum(g.item_ced) as ced_tax,  "
      + " 		max(gm.grn_date) as grn_date, textcat_commacat(DISTINCT gm.grn_no) AS grn_nos,gm.form_8h,  "
      + " 		textcat_commacat(DISTINCT gd.dept_name) AS store  " + " 	FROM store_debit_note d  "
      + " 	JOIN store_grn_main gm ON gm.debit_note_no = d.debit_note_no  "
      + " 	JOIN store_grn_details g USING (grn_no)  "
      + " 	JOIN stores gd ON (gm.store_id= gd.dept_id)  " + " 	GROUP BY d.debit_note_no,gm.form_8h"
      + " ) AS  t USING (debit_note_no)  " + " LEFT JOIN stores st ON (t.store = st.dept_name)  "
      + " LEFT JOIN hospital_center_master hcm ON(hcm.center_id=st.center_id)"
      + " LEFT JOIN store_type_master sty ON (st.store_type_id = sty.store_type_id)";

  private static final String FROM_TABLES = " FROM store_invoice i "
      + "  JOIN supplier_master s ON (s.supplier_code = i.supplier_id) "
      + "  JOIN store_invoice_totals_view t USING (supplier_invoice_id) "
      + "  LEFT JOIN stores st ON (t.store = st.dept_name)"
      + "  LEFT JOIN hospital_center_master hcm ON(hcm.center_id=st.center_id)"
      + "  LEFT JOIN store_type_master sty ON (st.store_type_id = sty.store_type_id)";

  public void makeQuery(StringBuilder q, ArrayList params) throws Exception {
    q.append(SELECT_ALL);
    q.append(STATIC_FIELDS);

    // append the dynamic fields: 2 fields for each tax_type/rate combination
    List<BasicDynaBean> l = DirectStockEntryDAO.getTaxTypesBean();
    int i = 0;
    for (BasicDynaBean b : l) {
      BigDecimal rate = (BigDecimal) b.get("tax_rate");
      String name = (String) b.get("tax_name");
      String amtStr = name + " " + rate + "% " + "Amt"; // eg VAT 4% Amt
      String taxStr = name + " @" + rate + "%"; // eg VAT@4%

      q.append(", coalesce((SELECT sum(tax) FROM store_grn_details ");
      q.append("JOIN store_grn_main USING (grn_no) ");
      q.append("WHERE supplier_invoice_id=i.supplier_invoice_id ");
      q.append("AND i.tax_name = ? AND outgoing_tax_rate = ?");
      q.append("),0) AS ");
      q.append("tax_" + i + "_tax");

      params.add(name);
      params.add(rate);

      q.append(
          ", coalesce((SELECT sum(cost_price*(billed_qty/grn_pkg_size)-(discount+scheme_discount)) FROM store_grn_details ");
      q.append("JOIN store_grn_main USING (grn_no) ");
      q.append("WHERE supplier_invoice_id=i.supplier_invoice_id ");
      q.append("AND i.tax_name = ? AND outgoing_tax_rate = ?");
      q.append("),0) AS ");
      q.append("tax_" + i + "_amt");

      params.add(name);
      params.add(rate);
      i++;
    }
    q.append(FROM_TABLES);
    q.append(SELECT_ALL_END + " query1");

    // Debit note entries start here
    q.append(" UNION ALL ");
    q.append(SELECT_ALL);
    q.append(DEBIT_STATIC_FIELDS);
    List<BasicDynaBean> dl = DirectStockEntryDAO.getTaxTypesBean();
    i = 0;
    for (BasicDynaBean b : dl) {
      BigDecimal rate = (BigDecimal) b.get("tax_rate");
      String name = (String) b.get("tax_name");
      String amtStr = name + " " + rate + "% " + "Amt"; // eg VAT 4% Amt
      String taxStr = name + " @" + rate + "%"; // eg VAT@4%

      q.append(", coalesce((SELECT sum(tax) FROM store_grn_details ");
      q.append("JOIN store_grn_main USING (grn_no) ");
      q.append("WHERE debit_note_no = d.debit_note_no ");
      q.append("AND d.tax_name = ? AND outgoing_tax_rate = ?");
      q.append("),0) AS ");
      q.append("tax_" + i + "_tax");

      params.add(name);
      params.add(rate);

      q.append(
          ", coalesce((SELECT sum(cost_price*(billed_qty/grn_pkg_size)-(discount+scheme_discount)) FROM store_grn_details ");
      q.append("JOIN store_grn_main USING (grn_no) ");
      q.append("WHERE debit_note_no = d.debit_note_no ");
      q.append("AND d.tax_name = ? AND outgoing_tax_rate = ?");
      q.append("),0) AS ");
      q.append("tax_" + i + "_amt");

      params.add(name);
      params.add(rate);
      i++;
    }
    q.append(DEBIT_FROM_TABLES);
    q.append(SELECT_ALL_END + " query2");

  }

}
