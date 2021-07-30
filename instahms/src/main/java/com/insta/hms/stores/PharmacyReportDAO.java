package com.insta.hms.stores;

import au.com.bytecode.opencsv.CSVWriter;
import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.SearchQueryBuilder;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class PharmacyReportDAO {

  /*
   * Purchase Details Report
   */

  private static final String PURDET_EXT_QUERY_FIELDS = "SELECT gd.dept_name as \"Store Name\",supplier_name as \"Suppier Name\", pi.invoice_no as \"Invoice No\", gm.grn_date as \"GRN Date\","
      + "m.medicine_name as \"Medicine Name\", g.batch_no as  \"Batch No\", g.billed_qty as \"Billed Qty\", "
      + " g.bonus_qty as  \"Bonus Qty\", g.mrp as \"MRP\", g.cost_price as  \"Cost Price\","
      + " g.discount as \"Discount\",g.tax_rate as  \"VAT Rate\", g.tax as \"VAT\", "
      + " pi.cess_tax_rate as  \"CESS Rate\", pi.cess_tax_amt as  \"CESS\","
      + "(g.billed_qty*g.cost_price - g.discount + g.tax + pi.cess_tax_amt) as \"Total\"";

  private static final String PURDET_EXT_QUERY_COUNT = "SELECT count(m.medicine_name) ";

  private static final String PURDET_EXT_QUERY_TABLES = " FROM store_grn_details g"
      + " JOIN store_grn_main gm USING(grn_no)" + " JOIN store_item_details m USING(medicine_id)"
      + " JOIN store_invoice pi ON ( pi.supplier_invoice_id = gm.supplier_invoice_id )"
      + " JOIN supplier_master s ON(s.supplier_code = pi.supplier_id) "
      + " JOIN stores gd ON (gd.dept_id = gm.store_id) ";

  public static void purchaseDetailsExportCSV(CSVWriter writer, java.util.Date from,
      java.util.Date to, String suppId, String store) throws SQLException, IOException {

    Connection con = DataBaseUtil.getConnection();
    SearchQueryBuilder qb = new SearchQueryBuilder(con, PURDET_EXT_QUERY_FIELDS,
        PURDET_EXT_QUERY_COUNT, PURDET_EXT_QUERY_TABLES, null, null, false, 0, 0);

    if (!suppId.equals("*"))
      qb.addFilter(qb.STRING, "pi.supplier_id", "=", suppId);
    if (!store.equals("*"))
      qb.addFilter(qb.STRING, "gm.store_id", "=", store);
    qb.addFilter(qb.DATE, "date_trunc('day', gm.grn_date)", ">=", from);
    qb.addFilter(qb.DATE, "date_trunc('day', gm.grn_date)", "<=", to);

    qb.build();

    PreparedStatement ps = qb.getDataStatement();
    ResultSet rs = ps.executeQuery();

    // write as CSV
    writer.writeAll(rs, true);

    // cleanup
    qb.close();
    con.close();
  }

  /*
   * Purchase Summary Report
   */

  private static final String PURSUM_EXT_QUERY_FIELDS = " SELECT gd.dept_name as \"Store Name\",s.supplier_name as \"Suppier Name\", pi.invoice_no as \"Invoice No\", gm.grn_date AS \"GRN Date\", gm.grn_no AS \"GRN No\","
      + " SUM(g.cost_price*g.billed_qty) AS \"Amount\", g.tax_rate AS \"VAT Rate\", sum(g.tax) AS \"VAT\",  SUM(g.discount) AS \"Discount\" ,"
      + " ROUND (SUM(g.tax) * pi.cess_tax_rate / 100) AS \"Cess Amt\" , "
      + " ((sum(g.cost_price*g.billed_qty))+(sum(g.tax))-(sum(g.discount)) + ROUND (SUM(g.tax) * pi.cess_tax_rate / 100) ) as \"Total\" ";

  private static final String PURSUM_EXT_QUERY_COUNT = "SELECT count(pi.invoice_no) ";

  private static final String PURSUM_EXT_QUERY_TABLES = " FROM store_grn_details g"
      + " JOIN store_grn_main gm using (grn_no)"
      + " JOIN store_invoice pi using (supplier_invoice_id)  "
      + " JOIN supplier_master s on (s.supplier_code = pi.supplier_id)"
      + " JOIN stores gd ON (gd.dept_id = gm.store_id) ";

  private static final String PURSUM_EXT_QUERY_GROUP_BY = " dept_name,s.supplier_name, pi.invoice_no, pi.cess_tax_rate, g.tax_rate, gm.grn_date, gm.grn_no";

  public static void purchaseSummaryExportCSV(CSVWriter writer, java.util.Date from,
      java.util.Date to, String suppId, String store) throws SQLException, IOException {

    Connection con = DataBaseUtil.getConnection();
    SearchQueryBuilder qb = new SearchQueryBuilder(con, PURSUM_EXT_QUERY_FIELDS,
        PURSUM_EXT_QUERY_COUNT, PURSUM_EXT_QUERY_TABLES, null, PURSUM_EXT_QUERY_GROUP_BY, null,
        false, 0, 0);

    if (!suppId.equals("*"))
      qb.addFilter(qb.STRING, "pi.supplier_id", "=", suppId);
    if (!store.equals("*"))
      qb.addFilter(qb.STRING, "gm.store_id", "=", store);
    qb.addFilter(qb.DATE, "date_trunc('day', gm.grn_date)", ">=", from);
    qb.addFilter(qb.DATE, "date_trunc('day', gm.grn_date)", "<=", to);

    qb.build();

    PreparedStatement ps = qb.getDataStatement();
    ResultSet rs = ps.executeQuery();

    // write as CSV
    writer.writeAll(rs, true);

    // cleanup
    qb.close();
    con.close();
  }

  /*
   * Pharmacy Sales Details Report
   */

  private static final String SALEDET_EXT_QUERY_FIELDS = " select gd.dept_name as store,to_char(pmsm.sale_date,'DD-MM-YYYY') as Date,pmsm.sale_id as Bill,"
      + " pmd.medicine_name,mm.manf_name,pms.batch_no,to_char(pmsd.exp_dt,'DD-MM-YYYY')as exp_date,pms.quantity as qty,"
      + "sic.control_type_name, pms.amount ";

  private static final String SALEDET_EXT_QUERY_COUNT = "SELECT count(pmd.medicine_name) ";

  private static final String SALEDET_EXT_QUERY_TABLES = " from store_sales_main  pmsm"
      + " JOIN store_sales_details pms on (pms.sale_id=pmsm.sale_id)"
      + " JOIN stores gd on (pmsm.store_id=gd.dept_id)"
      + " JOIN store_item_details pmd on (pmd.medicine_id=pms.medicine_id)"
      + " JOIN manf_master mm on (pmd.manf_name=mm.manf_code)"
      + " JOIN store_stock_details  pmsd on (pmsd.batch_no=pms.batch_no and "
      + " pmsd.dept_id=pmsm.store_id and pms.medicine_id=pmsd.medicine_id)"
      + " JOIN bill b on (b.bill_no=pmsm.bill_no)  "
      + " LEFT JOIN store_item_controltype sic ON (sic.control_type_id = pmd.control_type_id";

  public static void PharmacySalesDetailExportCSV(CSVWriter writer, java.util.Date from,
      java.util.Date to, String type, String medicineName) throws SQLException, IOException {

    Connection con = DataBaseUtil.getConnection();
    SearchQueryBuilder qb = new SearchQueryBuilder(con, SALEDET_EXT_QUERY_FIELDS,
        SALEDET_EXT_QUERY_COUNT, SALEDET_EXT_QUERY_TABLES, null, null, false, 0, 0);

    qb.addFilter(qb.DATE, "date_trunc('day', pmsm.sale_date)", ">=", from);
    qb.addFilter(qb.DATE, "date_trunc('day', pmsm.sale_date)", "<=", to);
    qb.addFilter(qb.STRING, "pmd.medicine_name", "=", medicineName);
    // chking report contains sales r returns
    if (type.equalsIgnoreCase("S"))
      qb.addFilter(qb.STRING, "type", "=", "S"); // only sales
    if (type.equalsIgnoreCase("R"))
      qb.addFilter(qb.STRING, "type", "=", "R"); // only returns

    qb.build();

    PreparedStatement ps = qb.getDataStatement();
    ResultSet rs = ps.executeQuery();

    // write as CSV
    writer.writeAll(rs, true);

    // cleanup
    qb.close();
    con.close();
  }

  public static void PharmacySalesDetailExportCSV(CSVWriter writer, java.util.Date from,
      java.util.Date to, Map extraParams) throws SQLException, IOException {
    String medicineName = (String) extraParams.get("medicineName");
    String type = (String) extraParams.get("type");

    PharmacySalesDetailExportCSV(writer, from, to, type, medicineName);
  }

  /*
   * Pharmacy Sales Summary Report
   */

  private static final String SALESUM_EXT_QUERY_FIELDS = " select "
      + " case when b.bill_type='C' then 'Bill Later' else 'Bill Now' end as bill_type,d.dept_name as Store,s.username,"
      + " to_char(s.sale_date,'DD-MM-YYYY') as sale_date,to_char(s.date_time,'hh:mi') as time,s.bill_no, s.sale_id,"
      + " case when b.visit_type='i' then 'In Patient' when b.visit_type='o' then 'Out Patient' "
      + " when b.visit_type='d' then 'Diagnostics' else 'Retail' end as patient_type,"
      + " case when s.type='S' then 'Normal' else 'Return' end as sale_type,"
      + " ((case when s.type='R' then (sum(pms.amount)-sum(pms.tax)) else 0.00 end)+(case when s.type!='R' then (sum(pms.amount)-sum(pms.tax)) else 0.00 end)) as amount,"
      + " ((case when s.type='R' then sum(si.tax) else 0.00 end)+(case when s.type!='R' then sum(si.tax) else 0.00 end)) as vat,"
      + " s.discount,((case when s.type='R' then (sum(pms.amount)-sum(pms.tax)) else 0.00 end)+(case when s.type!='R' then (sum(pms.amount)-sum(pms.tax)) else 0.00 end)+"
      + " (case when s.type='R' then sum(si.tax) else 0.00 end)+(case when s.type!='R' then sum(si.tax) else 0.00 end)-s.discount) as total";

  private static final String SALESUM_EXT_QUERY_COUNT = " SELECT count(s.bill_no) ";

  private static final String SALESUM_EXT_QUERY_TABLES = " FROM store_sales_details si"
      + " JOIN store_sales_main s using (sale_id)" + " JOIN stores d on (s.store_id = d.dept_id)"
      + " JOIN store_sales_details pms on (pms.sale_item_id = si.sale_item_id)"
      + " JOIN bill b on (s.bill_no = b.bill_no)";

  private static final String SALESUM_EXT_QUERY_GROUP_BY = " d.dept_name, s.username,s.sale_id,s.bill_no,"
      + " s.date_time, b.bill_type, b.visit_type,s.discount,s.type,s.sale_date";

  public static void PharmacySalesSummaryExportCSV(CSVWriter writer, java.util.Date from,
      java.util.Date to, String type) throws SQLException, IOException {

    Connection con = DataBaseUtil.getConnection();
    SearchQueryBuilder qb = new SearchQueryBuilder(con, SALESUM_EXT_QUERY_FIELDS,
        SALESUM_EXT_QUERY_COUNT, SALESUM_EXT_QUERY_TABLES, null, SALESUM_EXT_QUERY_GROUP_BY, null,
        false, 0, 0);

    qb.addFilter(qb.DATE, "date_trunc('day', s.sale_date)", ">=", from);
    qb.addFilter(qb.DATE, "date_trunc('day', s.sale_date)", "<=", to);
    // chking report contains sales r returns
    if (type.equalsIgnoreCase("S"))
      qb.addFilter(qb.STRING, "type", "=", "S"); // only sales
    if (type.equalsIgnoreCase("R"))
      qb.addFilter(qb.STRING, "type", "=", "R"); // only returns

    qb.build();

    PreparedStatement ps = qb.getDataStatement();
    ResultSet rs = ps.executeQuery();

    // write as CSV
    writer.writeAll(rs, true);

    // cleanup
    qb.close();
    con.close();
  }

  /*
   * Doctor Wise Sales Report
   */

  // normal sales table

  private static final String DOCTOR_EXT_QUERY_FIELDS = "select distinct(pmsm.doctor_name) as DoctorName,ptd.patient_name as PatientName,to_char(pmsm.sale_date,'DD-MM-YYYY')as SaleDate,"
      + " b.bill_no as BillNo,sum(bc.amount) as Amount";

  private static final String DOCTOR_EXT_QUERY_COUNT = " SELECT count(b.bill_no) ";

  private static final String DOCTOR_EXT_QUERY_TABLES = " from doctors d, bill b, bill_charge bc,"
      + " patient_registration pd,patient_details ptd,store_sales_main pmsm";

  private static final String DOCTOR_EXT_QUERY_INITWHERE = " where pd.doctor=d.doctor_id and b.bill_no=bc.bill_no and b.visit_id=pd.patient_id and ptd.mr_no=pd.mr_no "
      + " and pmsm.bill_no=b.bill_no and bc.act_department_id='DEP_PHA'";

  private static final String DOCTOR_EXT_QUERY_GROUP_BY = " pmsm.doctor_name,ptd.patient_name,"
      + " b.bill_no,pmsm.sale_date order by pmsm.doctor_name,b.bill_no ";

  // retails customer table
  private static final String DOCTORRET_EXT_QUERY_FIELDS = " select distinct(pmsm.doctor_name) as DoctorName,prc.customer_name as PatientName,to_char(pmsm.sale_date,'DD-MM-YYYY')as SaleDate,"
      + " b.bill_no as BillNo,sum(bc.amount) as Amount";

  private static final String DOCTORRET_EXT_QUERY_COUNT = " SELECT count(b.bill_no) ";

  private static final String DOCTORRET_EXT_QUERY_TABLES = " from store_retail_customers prc,bill b, bill_charge bc,"
      + " store_sales_main pmsm";

  private static final String DOCTORRET_EXT_QUERY_INITWHERE = " where b.visit_id=prc.customer_id and pmsm.bill_no=b.bill_no"
      + " and b.bill_no=bc.bill_no and bc.act_department_id='DEP_PHA' ";

  private static final String DOCTORRET_EXT_QUERY_GROUP_BY = " pmsm.doctor_name,prc.customer_name,pmsm.sale_date,"
      + " b.bill_no order by pmsm.doctor_name,b.bill_no  ";

  public static void doctorSalesExportCSV(CSVWriter writer, java.util.Date from, java.util.Date to)
      throws SQLException, IOException {

    Connection con = DataBaseUtil.getConnection();
    // normal sales table
    SearchQueryBuilder qb = new SearchQueryBuilder(con, DOCTOR_EXT_QUERY_FIELDS,
        DOCTOR_EXT_QUERY_COUNT, DOCTOR_EXT_QUERY_TABLES, DOCTOR_EXT_QUERY_INITWHERE,
        DOCTOR_EXT_QUERY_GROUP_BY, null, false, 0, 0);

    qb.addFilter(qb.DATE, "date_trunc('day', pmsm.sale_date)", ">=", from);
    qb.addFilter(qb.DATE, "date_trunc('day', pmsm.sale_date)", "<=", to);

    qb.build();
    PreparedStatement ps = null;
    PreparedStatement ps1 = null;
    ResultSet rs = null;
    ResultSet rs1 = null;
    try {
      ps = qb.getDataStatement();
      rs = ps.executeQuery();
  
      // retails customer table
      SearchQueryBuilder qb1 = new SearchQueryBuilder(con, DOCTORRET_EXT_QUERY_FIELDS,
          DOCTORRET_EXT_QUERY_COUNT, DOCTORRET_EXT_QUERY_TABLES, DOCTORRET_EXT_QUERY_INITWHERE,
          DOCTORRET_EXT_QUERY_GROUP_BY, null, false, 0, 0);
  
      qb1.addFilter(qb.DATE, "date_trunc('day', pmsm.sale_date)", ">=", from);
      qb1.addFilter(qb.DATE, "date_trunc('day', pmsm.sale_date)", "<=", to);
  
      qb1.build();
  
      ps1 = qb1.getDataStatement();
      rs1 = ps1.executeQuery();
  
      // write as CSV
      writer.writeAll(rs, true);
      writer.writeAll(rs1, false);

      // cleanup
      qb.close();
      qb1.close();
      con.close();
    } finally {
      if (rs != null) {rs.close();}
      if (rs1 != null) {rs1.close();}
      ps.close();
      ps1.close();
    }
  }

  /*
   * Supplier Returns Report
   */

  private static final String SUPP_RETURNS_EXT_QUERY_FIELDS = " SELECT s.supplier_name,sm.cust_supplier_code,to_char(prm.date_time,'DD-MM-YYYY') as date,medicine_name,pr.batch_no,pr.qty,"
      + " round((pmsd.package_cp/pmd.issue_base_unit)*pr.qty,2) as value,"
      + " case when return_type='D' then 'Damage' when return_type='E' then 'Expiry' else 'Others' end as returntype,"
      + " user_name,prm.remarks,return_no";

  private static final String SUPP_RETURNS_EXT_QUERY_COUNT = " SELECT count(medicine_name) ";

  private static final String SUPP_RETURNS_EXT_QUERY_TABLES = " FROM store_supplier_returns pr"
      + " JOIN store_supplier_returns_main prm using (return_no)"
      + " JOIN store_item_details pmd using(medicine_id)"
      + " JOIN store_stock_details pmsd using(medicine_id)"
      + " JOIN supplier_master s on (s.supplier_code = supplier_id)";

  private static final String SUPP_RETURNS_EXT_QUERY_INITWHERE = " where pr.batch_no=pmsd.batch_no";

  private static final String SUPP_RETURNS_EXT_QUERY_GROUP_BY = " s.supplier_name,sm.cust_supplier_code,medicine_name,pr.batch_no,pr.qty,user_name,prm.remarks,returntype,"
      + " return_no,date,pmsd.package_cp,pmd.issue_base_unit";

  public static void supplierReturnsExportCSV(CSVWriter writer, java.util.Date from,
      java.util.Date to, String suppId) throws SQLException, IOException {

    Connection con = DataBaseUtil.getConnection();
    SearchQueryBuilder qb = new SearchQueryBuilder(con, SUPP_RETURNS_EXT_QUERY_FIELDS,
        SUPP_RETURNS_EXT_QUERY_COUNT, SUPP_RETURNS_EXT_QUERY_TABLES,
        SUPP_RETURNS_EXT_QUERY_INITWHERE, SUPP_RETURNS_EXT_QUERY_GROUP_BY, null, false, 0, 0);

    if (!suppId.equals("*"))
      qb.addFilter(qb.STRING, "pi.supplier_id", "=", suppId);
    qb.addFilter(qb.DATE, "date_trunc('day', prm.date_time)", ">=", from);
    qb.addFilter(qb.DATE, "date_trunc('day', prm.date_time)", "<=", to);

    qb.build();

    PreparedStatement ps = qb.getDataStatement();
    ResultSet rs = ps.executeQuery();

    // write as CSV
    writer.writeAll(rs, true);

    // cleanup
    qb.close();
    con.close();
  }

  /*
   * Stock Issue Report
   */

  private static final String STOCK_ISSUE_EXT_QUERY_FIELDS = " select gdf.dept_name as from_store,gdt.dept_name as to_store,psi.issue_no,pmd.medicine_name,"
      + " psi.batch_no,to_char(psim.date_time, 'DD-MM-YYYY HH:MI AM')as date,psim.username,psi.qty,"
      + " (round((pmsd.package_cp/pmd.issue_base_unit),2)*psi.qty) as value,"
      + " (round((pmsd.tax/pmd.issue_base_unit),2)*psi.qty) as vat,psim.reason";

  private static final String STOCK_ISSUE_EXT_QUERY_COUNT = " SELECT count(pmd.medicine_name) ";

  private static final String STOCK_ISSUE_EXT_QUERY_TABLES = " from store_item_details  pmd , stock_issue_main psim,"
      + " stock_issue_details psi,store_stock_details pmsd," + " stores gdf, stores gdt ";

  private static final String STOCK_ISSUE_EXT_QUERY_INITWHERE = " where psi.issue_no=psim.issue_no and psi.medicine_id=pmd.medicine_id and "
      + " psim.store_from= gdf.dept_id and psim.store_to=gdt.dept_id and psi.medicine_id=pmsd.medicine_id"
      + " and gdt.dept_id=pmsd.dept_id and psi.batch_no=pmsd.batch_no";

  public static void stockIssueExportCSV(CSVWriter writer, java.util.Date from, java.util.Date to)
      throws SQLException, IOException {

    Connection con = DataBaseUtil.getConnection();
    SearchQueryBuilder qb = new SearchQueryBuilder(con, STOCK_ISSUE_EXT_QUERY_FIELDS,
        STOCK_ISSUE_EXT_QUERY_COUNT, STOCK_ISSUE_EXT_QUERY_TABLES, STOCK_ISSUE_EXT_QUERY_INITWHERE,
        null, null, false, 0, 0);

    qb.addFilter(qb.DATE, "date_trunc('day', psim.date_time)", ">=", from);
    qb.addFilter(qb.DATE, "date_trunc('day', psim.date_time)", "<=", to);

    qb.build();

    PreparedStatement ps = qb.getDataStatement();
    ResultSet rs = ps.executeQuery();

    // write as CSV
    writer.writeAll(rs, true);

    // cleanup
    qb.close();
    con.close();
  }

  /*
   * Stock Adjustment Report
   */

  private static final String STOCK_ADJ_EXT_QUERY_FIELDS = "select distinct(gd.dept_name) as store,pmd.medicine_name,psa.batch_no,"
      + " to_char(psam.date_time, 'DD-MM-YYYY HH:MI AM')as adjdate,psam.username,psa.qty as adjqty,"
      + " ( CASE WHEN PSA.TYPE='A' THEN 'Increment' WHEN PSA.TYPE='R' THEN 'Decrement' END )AS adjtype, "
      + " psam.reason as adjReason,psa.qty*pmsd.package_cp/pmd.issue_base_unit as value";

  private static final String STOCK_ADJ_EXT_QUERY_COUNT = " SELECT count(pmd.medicine_name) ";

  private static final String STOCK_ADJ_EXT_QUERY_TABLES = " from store_item_details  pmd , store_adj_main psam,"
      + " store_adj_details psa,stores gd, store_stock_details pmsd  ";

  private static final String STOCK_ADJ_EXT_QUERY_INITWHERE = " where psa.adj_no=psam.adj_no and psa.medicine_id=pmd.medicine_id and psam.store_id= gd.dept_id "
      + " and psa.medicine_id=pmsd.medicine_id and psa.batch_no=pmsd.batch_no and pmsd.dept_id=psam.store_id";

  public static void stockAdjustmentExportCSV(CSVWriter writer, java.util.Date from,
      java.util.Date to) throws SQLException, IOException {

    Connection con = DataBaseUtil.getConnection();
    SearchQueryBuilder qb = new SearchQueryBuilder(con, STOCK_ADJ_EXT_QUERY_FIELDS,
        STOCK_ADJ_EXT_QUERY_COUNT, STOCK_ADJ_EXT_QUERY_TABLES, STOCK_ADJ_EXT_QUERY_INITWHERE, null,
        null, false, 0, 0);

    qb.addFilter(qb.DATE, "date_trunc('day', psam.date_time)", ">=", from);
    qb.addFilter(qb.DATE, "date_trunc('day', psam.date_time)", "<=", to);

    qb.build();

    PreparedStatement ps = qb.getDataStatement();
    ResultSet rs = ps.executeQuery();

    // write as CSV
    writer.writeAll(rs, true);

    // cleanup
    qb.close();
    con.close();
  }

}
