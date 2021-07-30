package com.insta.hms.core.inventory.procurement;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * 
 * @author irshadmohammed
 *
 */
@Repository
public class PurchaseOrderMainRepository extends GenericRepository {
	
	public PurchaseOrderMainRepository() {
		super("store_po_main");
	}
	
	private static final String PO_SEQUENCE_PATTERN =
		" SELECT pattern_id FROM hosp_po_seq_prefs " +
		" WHERE priority = ( " +
			"  SELECT min(priority) FROM hosp_po_seq_prefs " +
			"  WHERE (store_id=? or store_id='*') " +
		" ) ";

	public static String getNextId(String storeId) throws SQLException {
		BasicDynaBean b = DataBaseUtil.queryToDynaBean(PO_SEQUENCE_PATTERN, storeId);
		String patternId = (String) b.get("pattern_id");
		return DatabaseHelper.getNextPatternId(patternId);
	}
	
	public static final String COPY_PO_DETAIL =
		"INSERT INTO store_po (po_no, qty_req, mrp, adj_mrp, cost_price, vat, discount, med_total, " +
		"  vat_rate, qty_received, vat_type, medicine_id, status, bonus_qty_req, " +
		"  bonus_qty_received, discount_per, item_remarks, po_pkg_size, item_ced_per, item_ced) " +
		"SELECT ?, qty_req, mrp, adj_mrp, cost_price, " +
		"		ROUND(( CASE WHEN vat_type = 'MB' THEN (adj_mrp * (qty_req + bonus_qty_req)/po_pkg_size*vat_rate/100)" +
		"        	  WHEN vat_type = 'M' THEN (adj_mrp * (qty_req)/po_pkg_size*vat_rate/100)" +
		"		 	  WHEN vat_type = 'C' THEN  (cost_price*qty_req/po_pkg_size -discount+item_ced) * vat_rate / 100" +
		"    		  ELSE (cost_price*(qty_req + bonus_qty_req)/po_pkg_size -discount+item_ced) * vat_rate / 100 END ), ?) as vat, " +
		"	discount, (foo.cost_price * foo.qty_req / foo.po_pkg_size " +
		"	- foo.discount + " +
		"		ROUND(( CASE WHEN vat_type = 'MB' THEN (adj_mrp * (qty_req + bonus_qty_req)/po_pkg_size*vat_rate/100)" +
		"        	  WHEN vat_type = 'M' THEN (adj_mrp * (qty_req)/po_pkg_size*vat_rate/100)" +
		"		 	  WHEN vat_type = 'C' THEN  (cost_price*qty_req/po_pkg_size -discount+item_ced) * vat_rate / 100" +
		"    		  ELSE (cost_price*(qty_req + bonus_qty_req)/po_pkg_size -discount+item_ced) * vat_rate / 100 END ), ?) + foo.item_ced) as med_total, " +
		"  vat_rate, 0, vat_type, medicine_id, status, bonus_qty_req, " +
		"  0, discount_per, item_remarks, po_pkg_size, item_ced_per, item_ced " +
		" FROM ( SELECT qty_req, mrp, adj_mrp, cost_price,  discount, med_total, " +
		"  			vat_rate, 0, vat_type,sp.medicine_id, sp.status, bonus_qty_req, " +
		"  			0,discount_per, item_remarks, po_pkg_size, item_ced_per, item_ced " +
		"FROM store_po sp " +
		"  JOIN stores s ON ( dept_id = ? ) " +
		"  LEFT JOIN store_item_rates ssir ON (sp.medicine_id = ssir.medicine_id AND " +
		"    ssir.store_rate_plan_id = s.store_rate_plan_id) " +
		"WHERE po_no=? AND sp.status !='R' ) as foo ";

	public static void copyPoDetail(String fromPo, String toPo, int storeId, int allowDecimalForQty) throws SQLException {
		DatabaseHelper.update(COPY_PO_DETAIL, new Object[]{toPo, allowDecimalForQty, allowDecimalForQty, storeId, fromPo});
	}
	
	public static final String COPY_PO_TAX_DETAIL =
			"INSERT INTO store_po_tax_details (medicine_id, po_no, item_subgroup_id, tax_rate, tax_amt) SELECT medicine_id, ?, item_subgroup_id, tax_rate, tax_amt "
			+ " FROM store_po_tax_details sptd WHERE sptd.po_no = ?";
		
	public static void copyPoTaxDetail(String fromPo, String toPo) throws SQLException {
		DatabaseHelper.update(COPY_PO_TAX_DETAIL, new Object[]{toPo, fromPo});
	}
}
