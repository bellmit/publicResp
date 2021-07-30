/**
 *
 */
package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author krishna.t
 *
 */
public class StoresTallyDAO {

  /*
   * Returns the invoice amounts suitable for export to tally: all closed/finalized invoices, whose
   * modification time is within the given range.
   */

  private static final String GET_ALL_INVOICES_AMOUNTS = " SELECT i.supplier_id, i.invoice_no, sup.supplier_name,sup.cust_supplier_code, i.due_date, i.invoice_date, i.po_no, "
      + "	i.po_reference, i.status, i.discount as inv_level_discount, i.round_off, i.other_charges, i.cess_tax_amt, i.tax_name, "
      + "	i.supplier_invoice_id, g.cost_price, g.billed_qty, g.grn_pkg_size, "
      + "	(g.cost_price*g.billed_qty/g.grn_pkg_size) as grn_amt, g.discount, g.tax, g.item_ced as ced_tax, "
      + "	hcm.center_code, s.center_id, "
      + "	i.cst_rate, g.tax_rate, s.purchases_store_cst_account_prefix, s.purchases_store_vat_account_prefix, "
      + "	icm.purchases_cat_cst_account_prefix, icm.purchases_cat_vat_account_prefix, debit_amt, gm.grn_date::date as grn_date,i.invoice_no||'/'||gm.grn_no as voucher_number "
      + " FROM store_invoice i " + "	JOIN store_grn_main gm USING (supplier_invoice_id) "
      + "	JOIN store_grn_details g USING (grn_no) "
      + "	JOIN store_item_details pmd using (medicine_id) "
      + "	JOIN stores s on s.dept_id=gm.store_id "
      + "	JOIN hospital_center_master hcm ON s.center_id=hcm.center_id "
      + " 	JOIN supplier_master sup ON (i.supplier_id = sup.supplier_code) "
      + "	JOIN store_category_master icm ON (pmd.med_category_id=icm.category_id) "
      + " WHERE i.consignment_stock=false and i.status!='O' ";

  public static final List getInvoiceAmounts(Connection con, java.sql.Timestamp fromDate,
      java.sql.Timestamp toDate, Integer accountGroup, int centerId, List supplierInvoiceNos)
      throws SQLException {
    PreparedStatement ps = null;
    try {
      StringBuilder where = new StringBuilder();
      if (fromDate != null && toDate != null) {
        where.append(" AND grn_date BETWEEN ? AND ? AND i.account_group=? ");
        if (centerId != 0)
          where.append(" AND s.center_id=?");
      } else {
        if (supplierInvoiceNos == null || supplierInvoiceNos.isEmpty())
          return Collections.EMPTY_LIST;

        DataBaseUtil.addWhereFieldInList(where, "i.supplier_invoice_id", supplierInvoiceNos, true);
      }
      ps = con.prepareStatement(GET_ALL_INVOICES_AMOUNTS + where.toString()
          + " ORDER BY supplier_invoice_id, icm.category_id");
      if (fromDate != null && toDate != null) {
        ps.setTimestamp(1, fromDate);
        ps.setTimestamp(2, toDate);
        ps.setInt(3, accountGroup);
        if (centerId != 0)
          ps.setInt(4, centerId);
      } else {
        Iterator it = supplierInvoiceNos.iterator();
        int i = 1;
        while (it.hasNext()) {
          ps.setInt(i++, (Integer) ((Map) it.next()).get("voucher_no"));
        }
      }
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  public static final String CONSIGNMENT_INVOICES = " SELECT t.issue_id, t.supplier_id, t.grn_amt, t.supplier_invoice_id, "
      + "	t.con_invoice_date, t.grn_tax as final_tax, t.vat_rate, sm.supplier_name,sm.cust_supplier_code, "
      + "	scm.purchases_cat_vat_account_prefix, scm.purchases_cat_cst_account_prefix, si.invoice_no, "
      + "	si.discount, si.round_off, si.tax_name, si.other_charges, si.po_no, si.po_reference, "
      + "	si.cess_tax_amt, t.center_code, t.dept_center_code, t.consignment_invoice_no, "
      + "	t.purchase_store_account_prefix  " + " FROM store_cons_invoice_totals_view_by_vatrate t "
      + "	JOIN store_invoice si USING (supplier_invoice_id) "
      + "	JOIN supplier_master sm ON (t.supplier_id=sm.supplier_code) "
      + "	JOIN store_category_master scm ON (t.med_category_id=scm.category_id) "
      + " WHERE si.consignment_stock=true";

  public static final List getConsignmentStockIssued(Connection con, java.sql.Timestamp fromDate,
      java.sql.Timestamp toDate, Integer accountGroup, int centerId, List issueIds)
      throws SQLException {
    PreparedStatement ps = null;
    try {
      StringBuilder where = new StringBuilder();
      if (fromDate != null && toDate != null) {
        where.append(" AND con_invoice_date BETWEEN ? AND ? AND si.account_group=? ");
        if (centerId != 0)
          where.append(" AND t.center_id=? ");
      } else {
        if (issueIds == null || issueIds.isEmpty())
          return Collections.EMPTY_LIST;

        DataBaseUtil.addWhereFieldInList(where, "t.issue_id", issueIds, true);
      }
      ps = con.prepareStatement(CONSIGNMENT_INVOICES + where.toString());

      if (fromDate != null && toDate != null) {
        ps.setTimestamp(1, fromDate);
        ps.setTimestamp(2, toDate);
        ps.setInt(3, accountGroup);
        if (centerId != 0)
          ps.setInt(4, centerId);
      } else {
        Iterator it = issueIds.iterator();
        int i = 1;
        while (it.hasNext()) {
          ps.setInt(i++, (Integer) ((Map) it.next()).get("voucher_no"));
        }
      }
      return DataBaseUtil.queryToDynaList(ps);

    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  private final static String INVENTORY_CONSIGNMENT_STOCK_RETRUNS_AMOUNTS = " SELECT m.user_return_no, SUM((sgd.cost_price/sgd.grn_pkg_size)*r.qty - (sgd.discount/sgd.total_qty*r.qty)) AS final_amt, "
      + "	i.supplier_id, DATE( m.date_time) AS con_return_date, s.supplier_name,"
      + " 	hcm.center_code, coalesce(pdept.cost_center_code, dept.cost_center_code) as dept_center_code, "
      + " 	CASE WHEN i.tax_name = 'CST' THEN st.purchases_store_cst_account_prefix ELSE st.purchases_store_vat_account_prefix "
      + "	END AS purchase_store_account_prefix,  scm.purchases_cat_vat_account_prefix, scm.purchases_cat_cst_account_prefix, "
      + " 	sum((r.qty * sgd.tax)/sgd.billed_qty) AS final_tax,	CASE WHEN i.tax_name = 'CST' THEN i.cst_rate ELSE sgd.tax_rate END AS vat_rate, "
      + " 	ic.consignment_invoice_no, i.tax_name " + " FROM store_issue_returns_main m "
      + " JOIN store_issue_returns_details r ON r.user_return_no = m.user_return_no "
      + " JOIN store_consignment_invoice ic on ic.issue_id = m.user_issue_no AND "
      + " 	ic.medicine_id = r.medicine_id AND ic.batch_no = r.batch_no "
      + " JOIN store_invoice i ON i.supplier_invoice_id=ic.supplier_invoice_id "
      + " JOIN store_grn_main sgm USING (grn_no) "
      + " JOIN store_grn_details sgd ON (sgd.grn_no=sgm.grn_no and sgd.medicine_id=r.medicine_id and sgd.batch_no=r.batch_no)"
      + " JOIN store_item_details sid ON (sid.medicine_id=r.medicine_id) "
      + " JOIN store_category_master scm ON (sid.med_category_id=scm.category_id) "
      + " JOIN supplier_master s ON (i.supplier_id = s.supplier_code) "
      + " JOIN stores st ON (st.dept_id=m.dept_to) "
      + " JOIN hospital_center_master hcm ON (hcm.center_id=st.center_id) "
      + " LEFT JOIN patient_registration pr ON (pr.patient_id=m.returned_by) "
      + " LEFT JOIN department pdept ON (pdept.dept_id=pr.dept_name) "
      + " LEFT JOIN department dept ON (dept.dept_id=m.returned_by) "
      + " LEFT JOIN hospital_center_master dhcm ON (dhcm.center_id=pr.center_id) ";

  private final static String INVENTORY_CONSIGNMENT_STOCK_RETRUNS_AMOUNTS_GROUP_BY = " GROUP BY m.user_return_no, date(m.date_time), i.supplier_id, s.supplier_name ,ic.issue_id, hcm.center_code,"
      + " 	pdept.cost_center_code, dept.cost_center_code,  scm.purchases_cat_vat_account_prefix, scm.purchases_cat_cst_account_prefix, "
      + "	(CASE WHEN i.tax_name = 'CST' THEN st.purchases_store_cst_account_prefix else st.purchases_store_vat_account_prefix end), "
      + "	CASE WHEN i.tax_name = 'CST' THEN i.cst_rate ELSE sgd.tax_rate END, ic.consignment_invoice_no, i.tax_name ";

  public static List getConsignmentStockReturnedAmounts(Connection con, java.sql.Timestamp fromDate,
      java.sql.Timestamp toDate, Integer accountGroup, int centerId, List returnNos)
      throws SQLException {
    PreparedStatement ps = null;
    try {
      StringBuilder where = new StringBuilder(" WHERE ");
      if (fromDate != null && toDate != null) {
        where.append(" m.date_time between ? AND ? AND i.account_group=? ");
        if (centerId != 0)
          where.append(" AND pr.center_id=? ");
      } else {
        if (returnNos == null || returnNos.isEmpty())
          return Collections.EMPTY_LIST;

        DataBaseUtil.addWhereFieldInList(where, "m.user_return_no", returnNos, false);
      }
      ps = con.prepareStatement(INVENTORY_CONSIGNMENT_STOCK_RETRUNS_AMOUNTS + where.toString()
          + INVENTORY_CONSIGNMENT_STOCK_RETRUNS_AMOUNTS_GROUP_BY + " ORDER BY m.user_return_no ");

      if (fromDate != null && toDate != null) {
        ps.setTimestamp(1, fromDate);
        ps.setTimestamp(2, toDate);
        ps.setInt(3, accountGroup);
        if (centerId != 0)
          ps.setInt(4, centerId);
      } else {
        Iterator it = returnNos.iterator();
        int i = 1;
        while (it.hasNext()) {
          ps.setBigDecimal(i++, (BigDecimal) ((Map) it.next()).get("voucher_no"));
        }
      }
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  private static final String STOCK_TRANSFERS_BWN_ACCOUNT_GROUPS = " SELECT stm.transfer_no, agm_from.inter_comp_acc_name as inter_comp_acc_name_from, "
      + "	agm_to.inter_comp_acc_name as inter_comp_acc_name_to, sum(std.qty*(ssd.mrp/ssd.stock_pkg_size)) as total_amt, "
      + "	date(stm.date_time) as date_time, agm_from.account_group_name as account_group_from, "
      + "	agm_to.account_group_name as account_group_to " + " FROM store_transfer_main stm "
      + "	JOIN stores s_from ON (stm.store_from=s_from.dept_id) "
      + "	JOIN account_group_master agm_from ON (agm_from.account_group_id=s_from.account_group) "
      + "	JOIN stores s_to ON (stm.store_to=s_to.dept_id) "
      + "	JOIN account_group_master agm_to ON (agm_to.account_group_id=s_to.account_group) "
      + "	JOIN store_transfer_details std ON (stm.transfer_no=std.transfer_no) "
      + "	JOIN store_stock_details ssd ON "
      + "		(ssd.medicine_id=std.medicine_id AND std.batch_no=ssd.batch_no AND stm.store_from=ssd.dept_id) "
      + " WHERE agm_from.account_group_id!=agm_to.account_group_id ";

  private static final String STOCK_TRANSFERS_BWN_ACCOUNT_GROUPS_GROUP_BY = " GROUP BY stm.transfer_no, agm_from.inter_comp_acc_name, agm_to.inter_comp_acc_name, stm.date_time, "
      + "	agm_from.account_group_name, agm_to.account_group_name ";

  public static List getStockTransfersBwnAccountGroups(Connection con, java.sql.Timestamp fromDate,
      java.sql.Timestamp toDate, Integer accountGroup, List transferNos) throws SQLException {
    PreparedStatement ps = null;
    try {
      StringBuilder where = new StringBuilder();
      if (fromDate != null && toDate != null) {
        where.append(" AND stm.date_time BETWEEN ? AND ? AND agm_from.account_group_id=? ");
      } else {
        if (transferNos == null || transferNos.isEmpty())
          return Collections.EMPTY_LIST;

        DataBaseUtil.addWhereFieldInList(where, "stm.transfer_no", transferNos, true);
      }
      ps = con.prepareStatement(STOCK_TRANSFERS_BWN_ACCOUNT_GROUPS + where.toString()
          + STOCK_TRANSFERS_BWN_ACCOUNT_GROUPS_GROUP_BY + " ORDER BY stm.transfer_no");

      if (fromDate != null && toDate != null) {
        ps.setTimestamp(1, fromDate);
        ps.setTimestamp(2, toDate);
        ps.setInt(3, accountGroup);
      } else {
        Iterator it = transferNos.iterator();
        int i = 1;
        while (it.hasNext()) {
          ps.setInt(i++, (Integer) ((Map) it.next()).get("voucher_no"));
        }
      }

      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

}
