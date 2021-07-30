package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * @author mithun.saha
 *
 */

public class StoresItemBatchDetailsDAO extends GenericDAO{
	static Logger logger = LoggerFactory.getLogger(StoresItemBatchDetailsDAO.class);
	public StoresItemBatchDetailsDAO() {
		super("store_item_batch_details");
	}

	private static String STORE_ITEM_BATCH_FIELDS = " SELECT * ";

	private static String STORE_ITEM_BATCH_COUNT = " SELECT count(*) ";

	private static String STORE_ITEM_BATCH_TABLES = " FROM (" +
				" SELECT g.generic_name, pmd.medicine_name,pmd.cust_item_code, sibd.batch_no,sibd.item_batch_id, pmsd.dept_id, sibd.mrp, " +
				" pmd.issue_base_unit AS pkg_size, pmd.tax_rate, sum(pmsd.qty) as qty, " +
				" pmd.medicine_id, m.manf_name, m.manf_mnemonic, COALESCE(isld.bin,pmd.bin) as bin, sibd.exp_dt, " +
				" icm.category, icm.billable, icm.issue_type, pmsd.item_ced_amt, " +
				" textcat_commacat(distinct pmsd.item_supplier_name) AS supplier_name, " +
				" textcat_commacat(distinct pmsd.item_grn_no) AS grn_no, " +
				" textcat_commacat(distinct pmsd.item_invoice_no) AS invoice_no, pmd.med_category_id, " +
				" textcat_commacat(distinct pmsd.item_supplier_code) AS supplier_code, pmsd.asset_approved, " +
				" pmsd.consignment_stock, icm.expiry_date_val, " +
				" CASE " +
				" WHEN sum(pmsd.qty) > 0::numeric THEN 'g'::text " +
				" WHEN sum(pmsd.qty) < 0::numeric THEN 'l'::text " +
				" ELSE 'e'::text " +
				" END AS qtycond, icm.identification, pmsd.stock_pkg_size, " +
				" CASE " +
				" WHEN sibd.exp_dt < current_date::date THEN 'Y'::text " +
				" ELSE 'N'::text " +
				" END AS expired, pmd.issue_units,pmd.item_barcode_id, pmsd.package_uom,pmd.package_type,sum(pmsd.qty_in_transit) as transit_qty " +
				" FROM store_stock_details pmsd " +
				" JOIN store_item_batch_details sibd USING(item_batch_id) " +
				" JOIN store_item_details pmd ON pmsd.medicine_id = pmd.medicine_id " +
				" LEFT JOIN item_store_level_details  isld ON isld.medicine_id=pmsd.medicine_id AND isld.dept_id = pmsd.dept_id " +
				" JOIN manf_master m ON m.manf_code::text = pmd.manf_name::text " +
				" LEFT JOIN generic_name g ON g.generic_code::text = pmd.generic_name::text " +
				" JOIN store_category_master icm ON icm.category_id = pmd.med_category_id " +
				" GROUP BY g.generic_name, pmd.medicine_name,pmd.cust_item_code, sibd.batch_no,sibd.item_batch_id, pmsd.dept_id, sibd.mrp, " +
				" pmd.issue_base_unit, pmd.tax_rate, pmd.medicine_id, COALESCE(isld.bin,pmd.bin), m.manf_name, " +
				" m.manf_mnemonic, sibd.exp_dt, icm.category, icm.billable, " +
				" icm.issue_type, pmd.med_category_id, pmsd.asset_approved, pmsd.consignment_stock, pmsd.item_ced_amt, " +
				" icm.expiry_date_val, icm.identification, pmsd.stock_pkg_size, pmd.issue_units,pmd.item_barcode_id, pmsd.package_uom,pmd.package_type ) foo";


	public PagedList getStoreConsumptionDetailsList(Map map, Map pagingParams)
		throws Exception, ParseException {
		Connection con = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();

			SearchQueryBuilder qb = new SearchQueryBuilder(con, STORE_ITEM_BATCH_FIELDS,
					STORE_ITEM_BATCH_COUNT, STORE_ITEM_BATCH_TABLES, pagingParams);

			qb.addFilterFromParamMap(map);
			qb.addSecondarySort("item_batch_id");
			qb.build();

			PagedList l = qb.getMappedPagedList();
			return l;
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

	private static final String GET_ITEM_BATCH_DETAILS =
		"SELECT medicine_name,sid.cust_item_code,category,m.manf_name,sibd.batch_no,exp_dt,COALESCE(isld.bin,sid.bin) as bin,ssd.dept_id," +
		"	sid.issue_units,sid.package_uom,sid.issue_base_unit,mrp,sid.tax_rate," +
		"	sibd.item_batch_id,sum(qty) as qty,billable,max(ssd.package_cp) as package_cp " +
		" FROM store_item_batch_details sibd " +
		" JOIN store_stock_details ssd using(item_batch_id) " +
	 	" JOIN store_item_details sid ON(sid.medicine_id = sibd.medicine_id) " +
	 	" LEFT JOIN item_store_level_details  isld ON isld.medicine_id = sibd.medicine_id AND isld.dept_id = ssd.dept_id " +
		" JOIN manf_master m ON m.manf_code::text = sid.manf_name::text " +
	 	" JOIN store_category_master scm ON(scm.category_id = sid.med_category_id) " +
	 	" WHERE sibd.item_batch_id = ? AND ssd.dept_id = ? " +
	 	" GROUP BY medicine_name,sid.cust_item_code,category,m.manf_name,sibd.batch_no,exp_dt,COALESCE(isld.bin,sid.bin)," +
		"	issue_units,sid.package_uom,issue_base_unit,mrp,tax_rate,sibd.item_batch_id,ssd.dept_id,billable";

	 public static BasicDynaBean getItemBatchDetails(int itemBatchId,int storeId) throws SQLException {
		 Connection con = null;
		 PreparedStatement ps = null;
		 try {
			 con = DataBaseUtil.getConnection();
			 ps = con.prepareStatement(GET_ITEM_BATCH_DETAILS);
			 ps.setInt(1, itemBatchId);
			 ps.setInt(2, storeId);
			 
			 return DataBaseUtil.queryToDynaBean(ps);
		 } finally {
			 DataBaseUtil.closeConnections(con, ps);
		 }
	 }

	 private static final String GET_ITEM_DETAILS = "SELECT medicine_name as item_name FROM store_item_details sibd order by item_name";

	public static List<BasicDynaBean> getItemDetails() throws SQLException {
		 Connection con = null;
		 PreparedStatement ps = null;
		 try {
			 con = DataBaseUtil.getConnection();
			 ps = con.prepareStatement(GET_ITEM_DETAILS);
			 return DataBaseUtil.queryToDynaList(ps);
		 } finally {
			 DataBaseUtil.closeConnections(con, ps);
		 }
	}
}