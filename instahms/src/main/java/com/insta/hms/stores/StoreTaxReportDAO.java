package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class StoreTaxReportDAO {

  private static String GET_INVOICE_XLS = "select nextval('tn_tax_report_seq') as serial_no,supplier_name as name_of_seller, supplier_tin_no as seller_tin, "
      + " case when (vat_rate = 4 OR vat_rate = 5) then 2044 " + " when vat_rate =0 then 752 "
      + " when (vat_rate = 12.5 OR  vat_rate = 14 OR  vat_rate = 14.50) then 301 "
      + " else 0 end as commodity_code,"
      + " invoice_no, invoice_date,grn_amt as purchase_value,vat_rate as tax_rate,grn_tax as vat_cst_paid,"
      + " case when vat_rate =0 then 'E' else 'R' end as category,store_id "
      + " from purchase_details_view_by_vatrate "
      + " where (date_trunc('day', grn_date) >= ? AND date_trunc('day', grn_date) <=?) # ";

  private static String GET_SALES_XLS = "select nextval('tn_tax_report_seq') as serial_no,* from( "
      + " select 'CONSUMER' as name_of_buyer,0 as buyer_tin,"
      + " case when (tax_rate = 4 OR tax_rate = 5) then 2044 " + " when tax_rate =0 then 752 "
      + " when (tax_rate = 12.5 OR tax_rate = 14 OR tax_rate = 14.50) then 301 "
      + " else 0 end as commodity_code,"
      + " '' as invoice_no,'' as invoice_date,sum(bill_amount) as sales_value,tax_rate,"
      + " sum(tax_amount) as vat_cst_paid,"
      + " case when type='S' and tax_rate=0 then 'Exempt (E)' when type='S' and tax_rate !=0 then 'First Schedule (F)' else 'Sales return (R)' end as category,"
      + " store_id " + " from sale_details_view_by_vatrate"
      + " WHERE (date_trunc('day', SALE_DATE) >= ? AND date_trunc('day', SALE_DATE) <=?) # "
      + " group by tax_rate,type,store_id " + " order by type desc,tax_rate) as foo";

  private static String GET_RETURNS_XLS = "select nextval('tn_tax_report_seq') as serial_no,14 as sec_code,0 as seller_tin,"
      + " case when (vat_rate = 4 OR vat_rate = 5) then 2044 " + " when vat_rate =0 then 752 "
      + " when (vat_rate = 12.5 OR  vat_rate = 14 OR  vat_rate = 14.50) then 301 "
      + " else 0 end as commodity_code,"
      + " sum(grn_amt) as inputtax_value,vat_rate as tax_rate,0 as reversal_credit"
      + " from purchase_returns_view_by_vatrate"
      + " WHERE (date_trunc('day', debit_note_date) >= ? AND date_trunc('day', debit_note_date) <=?) # "
      + " group by vat_rate";

  public static List<BasicDynaBean> getChargesXLS(String query, String fromDate, String toDate,
      String deptId, String store_type_id) throws Exception {
    Connection con = null;
    PreparedStatement ps = null;
    List<BasicDynaBean> list = null;
    List<Object> params = new ArrayList<Object>();
    try {
      String queryName = query.equalsIgnoreCase("ANNEX I")
          ? GET_INVOICE_XLS
          : query.equalsIgnoreCase("ANNEX II") ? GET_SALES_XLS : GET_RETURNS_XLS;
      String queryFilters = "";
      if (!deptId.equals("")) {
        queryFilters = queryFilters + " AND (store_id = ?) ";
        params.add(Integer.parseInt(deptId));
      }
      if (!store_type_id.equals("")) {
        queryFilters = queryFilters + " AND (store_type_id = ?) ";
        params.add(Integer.parseInt(store_type_id));
      }
      String finalquery = (!deptId.equals("") || !store_type_id.equals(""))
          ? queryName.replace("#", queryFilters)
          : queryName.replace("#", "");
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(finalquery);
      ps.setDate(1, DataBaseUtil.parseDate(fromDate));
      ps.setDate(2, DataBaseUtil.parseDate(toDate));
      ListIterator<Object> paramsIterator = params.listIterator();
      while (paramsIterator.hasNext()) {
        Object param = paramsIterator.next();
        int idx = paramsIterator.nextIndex();
        ps.setObject(idx + 2, param);
      }
      list = DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return list;
  }

  public static void resetDummySequence() throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement("ALTER SEQUENCE tn_tax_report_seq  RESTART WITH 1");
      ps.executeUpdate();
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }

  }

}
