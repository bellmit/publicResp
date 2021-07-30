package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class StockEntryDAO {

	Connection con = null;
	public StockEntryDAO (){}

	public StockEntryDAO (Connection con){
		this.con = con;
	}

	static Logger log = LoggerFactory.getLogger(StockEntryDAO.class);

	private static final String GET_BATCH_DETAILS =
		" SELECT DISTINCT ON (ssd.medicine_id, sibd.batch_no) " +
		"   ssd.medicine_id, sibd.batch_no, to_char(sibd.exp_dt, 'DD-MM-YYYY') as exp_dt, " +
		"   sibd.mrp, package_sp,package_cp, " +
		" consignment_stock, COALESCE(sir.tax_rate,sid.tax_rate) as tax_rate," +
		"  COALESCE(sir.tax_type,sid.tax_type) as tax_type,sibd.item_batch_id " +
		" FROM store_stock_details ssd" +
		" JOIN store_item_details sid USING(medicine_id)" +
		" LEFT JOIN store_item_rates sir ON (sid.medicine_id = sir.medicine_id AND " +
		"    sir.store_rate_plan_id = ?) " +
		" JOIN store_item_batch_details sibd USING(item_batch_id)" +
		" WHERE ssd.medicine_id=? ";

	public static List getItemBatchesDetails(int itemId) throws SQLException {
		return DataBaseUtil.queryToDynaList(GET_BATCH_DETAILS, itemId);
	}

	public static List getItemBatchesDetails(int storeId,int itemId) throws SQLException {

		BasicDynaBean storeBean = StoreDAO.findByStore(storeId);
		int storeRatePlanId = ( storeBean.get("store_rate_plan_id") == null ? 0 :(Integer)storeBean.get("store_rate_plan_id") );
		return DataBaseUtil.queryToDynaList(GET_BATCH_DETAILS, new Object[]{storeRatePlanId,itemId});
	}

	private static final String GET_BATCH_LOT_DETAILS =
		" SELECT ssd.medicine_id, sibd.batch_no,sibd.item_batch_id, " +
		"   ssd.medicine_id, sibd.batch_no,sibd.exp_dt, sibd.mrp, package_sp, " +
		"   consignment_stock, sid.tax_type, sid.tax_rate,sibd.item_batch_id," +
		"   ssd.stock_time,ssd.qty_maint,ssd.qty_retired,qty_lost,qty_kit,qty_unknown,qty_in_transit,ssd.qty," +
		"   qty_in_use,il.item_lot_id,il.lot_time,il.package_cp," +
		"   (CASE " +
		"		WHEN purchase_type = 'S' THEN 'Billed Quantity'" +
		"		ELSE 'Bonus Quantity' " +
		"	 END) AS purchase_type," +
		"   (CASE " +
		"		WHEN il.lot_source = 'S' THEN 'Stock Entry'  " +
		"       WHEN il.lot_source = 'E' THEN 'Edit GRN'" +
		"       ELSE 'Supplier Returns' " +
		"	END) AS lot_source,COALESCE(il.grn_no,'') as grn_no" +
		" FROM store_stock_details ssd" +
		" JOIN store_item_lot_details il USING(item_lot_id)" +
		" JOIN store_item_details sid ON(sid.medicine_id = ssd.medicine_id)" +
		" JOIN store_item_batch_details sibd ON(sibd.item_batch_id = ssd.item_batch_id)" +
		" WHERE ssd.medicine_id=? AND ssd.dept_id = ? order by store_stock_id desc";

	public static List getItemBatcheLotDetails(int storeId,int itemId) throws SQLException {
		return DataBaseUtil.queryToDynaList(GET_BATCH_LOT_DETAILS, new Object[]{itemId,storeId});
	}

	private static final String GET_PURCHASE_DET =
		"SELECT pi.supplier_id, sm.supplier_name,sm.cust_supplier_code, grn_no, " +
		" TO_CHAR(pngm.grn_date,'DD-MM-YYYY') AS grn_date, png.cost_price, png.batch_no, png.mrp, " +
		" png.discount_per, png.discount, " +
		" TO_CHAR(pi.invoice_date,'dd-MM-yyyy') AS invoice_date, " +
		" round(billed_qty/grn_pkg_size,2) as billed_qty ," +
		" round(bonus_qty/grn_pkg_size,2) as bonus_qty, invoice_no, grn_pkg_size,"
		+ "CASE WHEN tax_type = 'MB' THEN 'MRP Based(with bonus)' "
		+ "     WHEN tax_type = 'M' THEN 'MRP Based(without bonus)'"
		+ "     WHEN tax_type = 'CB' THEN 'CP Based(with bonus)' "
		+ "     ELSE 'CP Based(without bonus)' END as tax_type,tax_rate " +
		" FROM store_grn_details png " +
		"  JOIN store_grn_main pngm USING (grn_no) " +
		"  JOIN store_invoice pi USING (supplier_invoice_id) " +
		"  JOIN supplier_master sm ON (sm.supplier_code = pi.supplier_id) " +
		" WHERE png.medicine_id=? ";
		
	public static List<BasicDynaBean> getPurchaseDetails(int medId, List<Integer> storeList) throws SQLException {
		
		Connection con = null;
		PreparedStatement ps = null;
		List<BasicDynaBean> l = null;
	
		
		
		try {
			int i = 1;
			StringBuilder query = new StringBuilder(GET_PURCHASE_DET);
	    
	    DataBaseUtil.addWhereFieldInList(query, "store_id", storeList, true);
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(query.toString()+" ORDER BY pngm.grn_date ASC");
			ps.setInt(i, medId);
			i++;
			for(Integer store : storeList){
				ps.setInt(i, store);
				i++;
			}
			
			return DataBaseUtil.queryToDynaList(ps);
		} finally{
			DataBaseUtil.closeConnections(con, ps);
		}
		
	}

	// using LEFT JOIN on (true) instead of CROSS JOIN to avoid returning 0 rows in case
	// price details are not there.
	public static final String GET_ITEM_DETAILS =
		" SELECT pmd.medicine_name, pmd.medicine_id, pmd.item_barcode_id, 'false' as supplier_rate_validation," +
		"  pmd.package_uom, pmd.issue_units, pmd.issue_base_unit,  " +
		"  ic.identification, ic.expiry_date_val, ic.billable, " +
		"  pmd.max_cost_price, COALESCE(isld.bin,pmd.bin)  as bin," +
		"  price.cost_price, price.mrp,price.grn_pkg_size,COALESCE(sir.tax_rate,pmd.tax_rate) as tax_rate," +
		"  COALESCE(sir.tax_type,pmd.tax_type) as tax_type, " +
		"  batch_no_applicable,pmd.cust_item_code,sic.code_type,sic.item_code " +
		" FROM store_item_details pmd " +
		" LEFT JOIN ha_item_code_type hict ON (hict.medicine_id = pmd.medicine_id AND hict.health_authority=?) " +
		" LEFT JOIN store_item_codes sic ON (sic.medicine_id = pmd.medicine_id AND sic.code_type = hict.code_type) " +
		"  LEFT JOIN item_store_level_details  isld ON isld.medicine_id = pmd.medicine_id AND isld.dept_id = ? " +
		"   JOIN store_category_master ic ON (pmd.med_category_id=ic.category_id) " +
		" LEFT JOIN store_item_rates sir ON (pmd.medicine_id = sir.medicine_id AND " +
		"    sir.store_rate_plan_id = ?) " +
		"   LEFT JOIN ( " +
		"     SELECT cost_price, mrp,grn_pkg_size FROM store_grn_details sg " +
		"       JOIN store_grn_main sgm USING (grn_no) " +
		"       LEFT JOIN store_invoice si ON (si.supplier_invoice_id = sgm.supplier_invoice_id " +
		"         AND supplier_id = ?) " +
		"     WHERE sg.medicine_id=? AND sgm.debit_note_no IS NULL # " +
		"     ORDER BY supplier_id, grn_date DESC LIMIT 1 " +
		"   ) as price ON (true) " +
		" WHERE pmd.medicine_id=?";

	public static BasicDynaBean getItemDetails(int itemId, int storeId, String supplierId, List<Integer> storeList,String healthAuthority)
		throws SQLException {

		BasicDynaBean storeBean = StoreDAO.findByStore(storeId);
		int storeRatePlanId = ( storeBean.get("store_rate_plan_id") == null ? 0 :(Integer)storeBean.get("store_rate_plan_id") );

		StringBuilder query = new StringBuilder("");
		DataBaseUtil.addWhereFieldInList(query, "store_id", storeList, true);
		Connection con = null;
		PreparedStatement ps = null;
		
		try {
			int i =6;
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_ITEM_DETAILS.replace("#",query.toString()));
			ps.setString(1, healthAuthority);
			ps.setInt(2, storeId);
			ps.setInt(3,storeRatePlanId);
			ps.setString(4,supplierId);
			ps.setInt(5,itemId);
			for(Integer storeNo :storeList) {
				ps.setInt(i, storeNo);
				i++;
			}
			ps.setInt(i, itemId);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
    }

	private static final String GET_PO_ITEMS =
		"SELECT po.*, " +
		"   pom.discount_type as po_discount_type, pom.discount as po_discount, " +
		"   pom.discount as po_discount, pom.po_qty_unit, pom.store_id, " +
		"   itd.medicine_name,itd.cust_item_code, itd.item_barcode_id, itd.package_uom, itd.max_cost_price, itd.issue_units, " +
		"   itd.issue_base_unit,pom.vat_type as item_vat_type,po.vat_rate as item_vat_rate,0 as cst_rate, " +
		"   COALESCE(isld.bin,itd.bin)  as bin, " +
		"  CASE WHEN cust_item_code IS NOT NULL AND  TRIM(cust_item_code) != ''  THEN medicine_name||' - '||cust_item_code ELSE medicine_name END as cust_item_code_with_name, " +
		"   icm.billable, icm.identification, icm.expiry_date_val, batch_no_applicable, " +
		"	coalesce(po.vat_rate, sir.tax_rate, itd.tax_rate) as tax_rate,coalesce(po.vat_type, sir.tax_type, itd.tax_type) as tax_type,sic.item_code,pom.transportation_charges " +
		" FROM store_po po" +
		"   JOIN store_po_main pom using(po_no)" +
		"   JOIN store_item_details itd using(medicine_id)" +
		" LEFT JOIN ha_item_code_type hict ON (hict.medicine_id = itd.medicine_id AND hict.health_authority=?) " +
		" LEFT JOIN store_item_codes sic ON (sic.medicine_id = itd.medicine_id AND sic.code_type = hict.code_type) " +
		"   JOIN store_category_master icm on category_id=med_category_id" +
		" 	JOIN stores s ON (s.dept_id = pom.store_id) " +
		"  	LEFT JOIN item_store_level_details  isld ON isld.medicine_id = po.medicine_id AND isld.dept_id = pom.store_id " +
		"	LEFT JOIN store_item_rates sir ON (sir.medicine_id = itd.medicine_id AND sir.store_rate_plan_id = s.store_rate_plan_id) " +
		" WHERE po.po_no=? and po.status != 'R' " +
		" ORDER BY po.item_order";

	public static List<BasicDynaBean> getPOItems(String poNo,String healthAuthority) throws SQLException {
		return DataBaseUtil.queryToDynaList(GET_PO_ITEMS, healthAuthority, poNo);
	}

	public static final String GET_ITEMS_IN_PO_BATCHES =
		" SELECT DISTINCT ON (ssd.medicine_id, sibd.batch_no) " +
		"  ssd.medicine_id, sibd.batch_no, to_char(sibd.exp_dt, 'DD-MM-YYYY') as exp_dt, " +
		"  sibd.mrp, ssd.package_sp, sid.tax_type, sid.tax_rate, " +
		"  ssd.consignment_stock, ssd.stock_pkg_size, ssd.package_uom " +
		" FROM store_stock_details ssd " +
		"   JOIN store_item_details sid USING(medicine_id) " +
		"   JOIN store_item_batch_details sibd ON(sibd.item_batch_id = ssd.item_batch_id)" +
		"   JOIN store_po po ON (ssd.medicine_id = po.medicine_id) " +
		" WHERE po.po_no = ?";

	public static List<BasicDynaBean> getItemsInPoBatches(String poNo) throws SQLException {
		return DataBaseUtil.queryToDynaList(GET_ITEMS_IN_PO_BATCHES, poNo);
	}
	
	
	public static final String GET_CENTER_SUPPLIERS = "SELECT SM.*,center_id, " +
			"	CASE WHEN supplier_city IS NOT NULL AND  TRIM(supplier_city) != ''  THEN supplier_city||' - '||supplier_name ELSE supplier_name END as supplier_name_with_city FROM SUPPLIER_MASTER SM JOIN SUPPLIER_CENTER_MASTER SCM ON(SCM.SUPPLIER_CODE=SM.SUPPLIER_CODE) WHERE SM.STATUS='A' and (scm.center_id = ? OR scm.center_id = 0) ORDER BY SUPPLIER_NAME ";
	
	public static ArrayList getCenterMaterSupplierMaster(Integer centerId) throws SQLException {
		Connection con = null; PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_CENTER_SUPPLIERS);
			ps.setInt(1,centerId);
			return DataBaseUtil.queryToArrayList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	private static final String GET_GRN_DET=
		"SELECT sm.supplier_name,sm.cust_supplier_code, sm.supplier_address, pinv.supplier_id, pinv.round_off, pinv.invoice_no," +
		"  TO_CHAR(pinv.due_date,'DD-MM-YYYY') AS due_date, sm.tcs_applicable, " +
		"  TO_CHAR(pinv.invoice_date,'DD-MM-YYYY') AS invoice_date," +
		"  pinv.discount, pinv.cess_tax_amt, pngm.grn_no, pinv.discount_type, pinv.discount_per, " +
		"  pinv.tcs_type, pinv.tcs_per, pinv.tcs_amount, " +
		"  pngm.store_id, pinv.cess_tax_rate,coalesce(other_charges,0) AS other_charges, " +
		"  pinv.tax_name, pinv.cst_rate, pngm.consignment_stock, pinv.debit_amt, pngm.po_no, " +
		"  pinv.supplier_invoice_id, pinv.status, pinv.remarks, pinv.cash_purchase, " +
		"  pngm.grn_qty_unit, pinv.po_reference, pinv.payment_remarks, company_name, means_of_transport, consignment_no, TO_CHAR(consignment_date,'DD-MM-YYYY') as consignment_date,  " +
		"  CASE WHEN pngm.consignment_stock = true then 't' else 'f' END AS stock_type, invoice_file_name, purpose_of_purchase, c_form, form_8h,transportation_charges "+
		" FROM store_grn_main pngm " +
		"  JOIN store_invoice pinv USING (supplier_invoice_id) " +
		"  JOIN supplier_master sm ON pinv.supplier_id=supplier_code " +
		" WHERE grn_no = ? and debit_note_no is null ";

	public static BasicDynaBean getGrnDetails (String grnNo) throws SQLException {
		return DataBaseUtil.queryToDynaBean(GET_GRN_DET, grnNo);
	}

	public static BasicDynaBean getGrnDetails (String grnNo, List<Integer> storeList) throws SQLException {

		int i = 2;
		Connection con = null; 
		PreparedStatement ps = null;
		StringBuilder query = new StringBuilder(GET_GRN_DET);
		DataBaseUtil.addWhereFieldInList(query, "store_id", storeList, true);
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(query.toString());
			ps.setString(1,grnNo);
			for(Integer store : storeList){
				ps.setInt(i, store);
				i++;
			}
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	private static final String GET_GRN_MED =
		"SELECT gd.medicine_id, pmd.medicine_name, issue_units, pmd.item_barcode_id, pmd.max_cost_price as item_max_cost_price, " +
		" mc.expiry_date_val, mc.billable, mc.identification, pmd.issue_base_unit, " +
		" gd.grn_pkg_size, gd.grn_package_uom, gd.grn_package_uom as package_uom, " +
		" sibd.batch_no, to_char(sibd.exp_dt, 'DD-MM-YYYY') as exp_dt, " +
		" gd.mrp, gd.adj_mrp,gd.cost_price, gd.tax_rate, gd.tax_type, " +
		" gd.billed_qty, gd.bonus_qty, gd.discount,gd.scheme_discount,outgoing_tax_rate, " +
		" gd.tax, gd.item_ced_per, gd.item_ced, " +
		" CASE WHEN gd.medicine_id=po.medicine_id THEN 'Y' ELSE 'N' END AS pomed, " +
		" po.qty_req, po.qty_received, po.bonus_qty_req, po.bonus_qty_received, " +
		" po.cost_price as pomedrate, " +
		" pmd.package_uom AS stock_package_uom, sibd.item_batch_id, pmd.batch_no_applicable,pmd.cust_item_code,sic.item_code " +
		"FROM store_grn_details gd " +
		" JOIN store_item_batch_details sibd USING(item_batch_id)" +
		" JOIN store_item_details pmd on(pmd.medicine_id = gd.medicine_id) " +
		" LEFT JOIN ha_item_code_type hict ON (hict.medicine_id = pmd.medicine_id AND hict.health_authority=?) " +
		" LEFT JOIN store_item_codes sic ON (sic.medicine_id = pmd.medicine_id AND sic.code_type = hict.code_type) " +
		" JOIN store_grn_main gm using(grn_no) " +
		" LEFT JOIN store_po po ON (gm.po_no=po.po_no AND gd.medicine_id=po.medicine_id) " +
		" JOIN store_category_master mc ON (pmd.med_category_id = mc.category_id) " +
		"WHERE gd.grn_no=? AND gm.debit_note_no is null ";


	public static List<BasicDynaBean> getGrnItems(String grnNo,String healthAuthority) throws SQLException {
		return DataBaseUtil.queryToDynaList(GET_GRN_MED,healthAuthority, grnNo);
	}

	public static final String GET_ITEMS_IN_GRN_BATCHES =
		" SELECT DISTINCT ON (ssd.medicine_id, batch_no) " +
		"  ssd.medicine_id, sibd.batch_no, to_char(sibd.exp_dt, 'DD-MM-YYYY') as exp_dt, " +
		"  sibd.mrp, ssd.package_sp, sid.tax_type, sid.tax_rate, " +
		"  ssd.consignment_stock, ssd.stock_pkg_size, ssd.package_uom,ssd.asset_approved " +
		" FROM store_stock_details ssd " +
		"   JOIN store_item_details sid USING(medicine_id) " +
		"   JOIN store_grn_details grn ON (ssd.medicine_id = grn.medicine_id) " +
		"   JOIN store_item_batch_details sibd ON( sibd.item_batch_id = ssd.item_batch_id )" +
		" WHERE grn.grn_no = ?";

	public static List<BasicDynaBean> getItemsInGrnBatches(String grnNo) throws SQLException {
		return DataBaseUtil.queryToDynaList(GET_ITEMS_IN_GRN_BATCHES, grnNo);
	}

	private static final String GET_INV_DETAILS =
		" SELECT supplier_id, invoice_no, invoice_date, due_date, po_no, po_reference, discount, " +
		" round_off, si.status, payment_id, discount_type, discount_per, date_time, other_charges, " +
		" other_charges_remarks, remarks, paid_date, payment_remarks, cess_tax_rate, cess_tax_amt, " +
		" tax_name, cst_rate, account_group, supplier_invoice_id, consignment_stock, debit_amt, " +
		" cash_purchase, invoice_file_name, invoice_contenttype, " +
		" sm.supplier_name,sm.cust_supplier_code, sm.supplier_address, company_name, means_of_transport, " +
		" consignment_no, consignment_date,si.transportation_charges, " +
		" tcs_type, tcs_per, tcs_amount " +
		" FROM store_invoice si " +
		" JOIN supplier_master sm ON (sm.supplier_code = si.supplier_id) " +
		" WHERE supplier_invoice_id=?";

	public static BasicDynaBean getInvDetails(int suppInvId) throws SQLException {
		// not using findByKey due to attachment in this table.
		return DataBaseUtil.queryToDynaBean(GET_INV_DETAILS, suppInvId);
	}

	public static final String GRN_ITEM_QUERY = "SELECT *, " +
		" (billed_qty*cost_price/grn_pkg_size) - (discount + scheme_discount) + tax + item_ced AS med_total " +
		" FROM store_grn_details WHERE grn_no=? AND medicine_id=? AND batch_no=?";

	public static BasicDynaBean getRow(String grnNo, int medId, String batch) throws SQLException {
		// easier than findByKey when multiple keys exist
		return DataBaseUtil.queryToDynaBean(GRN_ITEM_QUERY, new Object[]{grnNo, medId, batch});
	}

	private static final String UPDATE_INV_STATUS =
		" UPDATE store_invoice SET status=?,payment_remarks=? WHERE supplier_invoice_id=?";

	public static int updateInvoiceStatus(Connection con, int suppInvId, String status, String paymentRemarks) throws SQLException {
	  PreparedStatement ps = null;
	  Integer result  = null;
	  try {
  	  ps = con.prepareStatement(UPDATE_INV_STATUS);
  		ps.setString(1, status);
  		ps.setString(2, paymentRemarks);
  		ps.setInt(3, suppInvId);
  		result = ps.executeUpdate();
	  } finally {
	    if (ps != null) ps.close();
	  }
	  return result;
	}

	private static final String GRN_SEQUENCE_PATTERN =
		" SELECT pattern_id FROM hosp_grn_seq_prefs " +
		" WHERE priority = ( " +
		"  SELECT min(priority) FROM hosp_grn_seq_prefs " +
		"  WHERE (store_id=? or store_id='*')  " +
		" ) #";

	public static String getNextId(String storeId,String cashPurchase) throws SQLException {
		BasicDynaBean b = null;
		if ( cashPurchase != null )
			b = DataBaseUtil.queryToDynaBean( GRN_SEQUENCE_PATTERN.replace("#", " AND pattern_type = 'cash'"), storeId);

		if ( b == null ){
			//it means that seperate sequence is not needed for cash purchase,lets use existing pattern
			 b = DataBaseUtil.queryToDynaBean( GRN_SEQUENCE_PATTERN.replace("#", " AND pattern_type IS NULL") , storeId);
		}
		String patternId = (String) b.get("pattern_id");
		return DataBaseUtil.getNextPatternId(patternId);
	}

	public static String getBarCode() throws SQLException {
		//BasicDynaBean b = DataBaseUtil.queryToDynaBean(GRN_SEQUENCE_PATTERN, storeId);
		//String patternId = (String) b.get("pattern_id");
		return DataBaseUtil.getNextPatternId("ITEM_BAR_CODE");
	}
	
	private static final String ITEM_NO_SEQUENCE_PATTERN =
			" SELECT pattern_id FROM hosp_item_seq_prefs " +
			" WHERE priority = ( " +
			"  SELECT min(priority) FROM hosp_item_seq_prefs " +
			"  WHERE (category_id = ? OR category_id = 0) AND " +
			"        (center_id = ? OR center_id = 0)  " +
			" ) ";
	public static final String getItemNoPattern(int medCategoryId,int centerId) throws SQLException {

		Connection con = null;
		PreparedStatement stmt = null;

		try {
			con = DataBaseUtil.getConnection();
			stmt = con.prepareStatement(ITEM_NO_SEQUENCE_PATTERN);
			stmt.setInt(1, medCategoryId);
			stmt.setInt(2, centerId);

			List<BasicDynaBean> l = DataBaseUtil.queryToDynaList(stmt);
			BasicDynaBean b = l.get(0);
			return (String) b.get("pattern_id");
		} finally {
			DataBaseUtil.closeConnections(con, stmt);
		}
	}
	
	public static final String getNextItemNo(int medCategoryId,int centerId) throws SQLException {

		String patternId = getItemNoPattern(medCategoryId , centerId);
		return DataBaseUtil.getNextPatternId(patternId);
	}
	

		private static final String GET_BATCHES = "SELECT distinct batch_no FROM store_stock_details WHERE medicine_ID=? AND dept_id=?";

		public static String[] getBatches (int itemId,int storeId) throws SQLException{
			int i=0;
			int centralId = storeId;
			String countquery = "select count(*) from store_stock_details where medicine_ID='"+itemId+"' and dept_id='"+centralId+"'";
			int count = Integer.parseInt(DataBaseUtil.getStringValueFromDb(countquery));
			String[] batchs = new String[count];
			PreparedStatement ps = null;Connection con = null;ResultSet rs = null;

			try{
				con = DataBaseUtil.getReadOnlyConnection();
				ps = con.prepareStatement(GET_BATCHES);
	            ps.setInt(1, itemId);
	            ps.setInt(2,centralId);
	            rs = ps.executeQuery();

	            while (rs.next()) {
	            	batchs[i] = rs.getString("batch_no");
	            	i++;
	            }
			    return batchs;
		   }finally{
				DataBaseUtil.closeConnections(con, ps, rs);
			}
		}

		private static final String CHECK_SUPP_INV = "SELECT COUNT(*) FROM  store_invoice WHERE SUPPLIER_ID=? AND INVOICE_NO=?";

		public static int checkSuppInv(String suppId,String invNo) throws SQLException{
			Connection con = null; PreparedStatement ps = null;ResultSet rs = null;
			int count = 0;
			try {
				con = DataBaseUtil.getReadOnlyConnection();
				ps = con.prepareStatement(CHECK_SUPP_INV);
				ps.setString(1, suppId);
				ps.setString(2, invNo);
				rs = ps.executeQuery();
				if (rs.next()){
					count = rs.getInt(1);
				}
			}finally {
				DataBaseUtil.closeConnections(con, ps, rs);
			}
			return count;
		}

		private static final String GET_INVOICE = "SELECT supplier_invoice_id FROM store_invoice where supplier_id = ? AND invoice_no = ? ";

		public static int getSupplierInvoice(String supplier, String invoice) throws SQLException {
			Connection con = null;
			PreparedStatement ps = null;
			List<BasicDynaBean> l = null;
			try {
				con = DataBaseUtil.getReadOnlyConnection();
				ps = con.prepareStatement(GET_INVOICE);
				ps.setString(1, supplier);
				ps.setString(2, invoice);
				l  = DataBaseUtil.queryToDynaList(ps);
				if (l!= null && l.size() > 0 )
					return (Integer)(l.get(0).get("supplier_invoice_id"));
				else return 0;
			} finally {
				DataBaseUtil.closeConnections(con, ps);
			}
		}



		private static final String PO_ITEMS = "select medicine_id,"
			+" case when po_qty_unit = 'P' then trunc((qty_req-qty_received)/issue_base_unit,2) else (qty_req-qty_received) end as billed_qty,"
			+" case when po_qty_unit = 'P' then trunc((bonus_qty_req-bonus_qty_received)/issue_base_unit,2) else (bonus_qty_req-bonus_qty_received) end as bonus_qty,"
			+" medicine_name from store_po join store_po_main using(po_no)"
			+" join store_item_details itd using(medicine_id) where po_no=?";


		public static String getPOItemsInJSON(String poNo) throws SQLException {
			Connection con = null; PreparedStatement ps = null;	ArrayList<String> data = null;

			try {
				con = DataBaseUtil.getReadOnlyConnection();
				ps = con.prepareStatement(PO_ITEMS);
				ps.setString(1, poNo);
				JSONSerializer js = new JSONSerializer().exclude("class");
				data = DataBaseUtil.queryToArrayList(ps);
				return js.serialize(data);
			} finally {
				DataBaseUtil.closeConnections(con, ps);
			}
		}


		private static final String GET_GRNS_WITH_INV = "SELECT TO_CHAR(PNGM.GRN_DATE,'DD-MM-YYYY') AS GRNDATE,PNGM.GRN_NO, "
            + " SUM(((PNG.BILLED_QTY/png.grn_pkg_size * PNG.COST_PRICE) - PNG.DISCOUNT + PNG.TAX + png.item_ced)) AS AMT," +
            		"COUNT(png.grn_no) AS total_items,TRUNC(SUM(png.billed_qty/png.grn_pkg_size),2) AS total_qty, " +
            		"SUM((png.billed_qty/png.grn_pkg_size * png.cost_price) ) AS tot_amt, SUM(png.discount) AS discount ,sum(png.item_ced) as ced,sum(png.tax) AS tax"
            + " FROM store_grn_main PNGM JOIN store_grn_details PNG USING(GRN_NO) JOIN store_item_details sid using(medicine_id)"
            + " WHERE pngm.supplier_invoice_id = ? GROUP BY PNGM.GRN_NO,PNGM.GRN_DATE,debit_note_no";

		public static ArrayList getGrnsAccInvNo(int supplierInvoiceId) throws SQLException {
			Connection con = null; PreparedStatement ps = null;
			try {
				con = DataBaseUtil.getReadOnlyConnection();
				ps = con.prepareStatement(GET_GRNS_WITH_INV);
				ps.setInt(1, supplierInvoiceId);
				//	ps.setString(2, suppCode);
				return DataBaseUtil.queryToArrayList(ps);
			} finally {
				DataBaseUtil.closeConnections(con, ps);
			}
        }

		private static final String CHECK_DUPLICATE = "SELECT COUNT(*) FROM store_invoice where invoice_no = ? AND supplier_id = ? ";
		private static final String GET_SUPP_INV_ID = "select supplier_invoice_id from store_invoice where invoice_no = ? AND supplier_id = ?";

		public static int checkDuplicateInvoice (String invNo,String suppId) throws SQLException{
			Connection con = null; 
			PreparedStatement ps = null;
			try {
				con = DataBaseUtil.getReadOnlyConnection();
				ps = con.prepareStatement(CHECK_DUPLICATE);
				ps.setString(1, invNo);
				ps.setString(2, suppId);
				int count = DataBaseUtil.getIntValueFromDb(ps);
				return count;
			} finally {
				DataBaseUtil.closeConnections(con, ps);
			}
		}

		private static final String UPDATE_INVOICE = "UPDATE store_invoice SET INVOICE_DATE=?,DUE_DATE=?,PO_NO=?,PO_REFERENCE=?,"
	        + " DISCOUNT_PER=?,DISCOUNT=?,DISCOUNT_TYPE=?,ROUND_OFF=?,OTHER_CHARGES=?,STATUS=?,OTHER_CHARGES_REMARKS=?, date_time=current_timestamp,"
	        + " PAID_DATE=?,REMARKS=?,SUPPLIER_ID=?,INVOICE_NO=?,cess_tax_rate=?,cess_tax_amt=?, debit_amt=?, cash_purchase=? ";

		public static boolean updateInvoice (Map<String,Object> item,int id,boolean file) throws SQLException,IOException{

			Connection con = DataBaseUtil.getReadOnlyConnection();
			int count = 0;
			String query = UPDATE_INVOICE;
			try (PreparedStatement ps = con.prepareStatement(query + " WHERE SUPPLIER_INVOICE_ID = ?")) {
				if (file) query = query + " ,invoice_file_name=?,supplier_invoice_attachment=?,invoice_contenttype=? ";
				
				con.setAutoCommit(false);
				for (Map.Entry e : (Collection<Map.Entry<String, Object>>) item.entrySet()) {
					Object object = e.getValue();
					if (object instanceof java.io.InputStream) {
						java.io.InputStream stream = (java.io.InputStream) object;
						ps.setBinaryStream(Integer.parseInt((e.getKey()).toString()), stream, stream.available());
					} else {
						ps.setObject(Integer.parseInt((e.getKey()).toString()), object);
					}
				}
				ps.setInt(item.size()+1, id);
				count = ps.executeUpdate();
				return count > 0;
			} finally {
				if (count > 0) {
					con.commit();
				} else {
					con.rollback();
  			}
  			con.close();
			}
    }

		private static final String  store_stock_details_QUERY = "SELECT * FROM store_stock_details ";
		public BasicDynaBean getStock(Connection con,int  store,int med_id,int itemBatchId)throws SQLException{
			PreparedStatement ps = null;
			ResultSet rs = null;
			BasicDynaBean bean = null;
			try{
				ps = con.prepareStatement(store_stock_details_QUERY+" WHERE DEPT_ID=? AND MEDICINE_ID=? AND item_batch_id=?");
				ps.setInt(1, store);
				ps.setInt(2, med_id);
				ps.setInt(3, itemBatchId);
				rs = ps.executeQuery();
                GenericDAO storeStockDetailsDAO = new GenericDAO("store_stock_details");
				while(rs.next()){
					bean  = storeStockDetailsDAO.getBean();
					bean.set("dept_id", rs.getInt("dept_id"));
					bean.set("medicine_id", rs.getInt("medicine_id"));
					bean.set("batch_no", rs.getString("batch_no"));
					bean.set("package_cp", rs.getBigDecimal("package_cp"));
					bean.set("package_sp",rs.getBigDecimal("package_sp"));
					bean.set("stock_time", rs.getTimestamp("stock_time"));
					bean.set("username", rs.getString("username"));
					bean.set("qty",rs.getBigDecimal("qty"));
					bean.set("tax", rs.getBigDecimal("tax"));
					bean.set("stock_pkg_size", rs.getBigDecimal("stock_pkg_size"));
					bean.set("package_uom", rs.getString("package_uom"));
					bean.set("item_batch_id", rs.getInt("item_batch_id"));
					bean.set("item_lot_id", rs.getInt("item_lot_id"));
				}
				return bean;
			}finally{
				DataBaseUtil.closeConnections(null, ps,rs);
			}
		}

		private static final String VAL_NAME_EXISTS = "select validateName(?,?,?,?,?) ;";

		public static boolean valNameExists (String colname,int deptid,int medicineid,String batchno,String expvalue) throws SQLException{
			Connection con = null; PreparedStatement ps = null;boolean flag;BasicDynaBean bean = null;
			try {
				con = DataBaseUtil.getReadOnlyConnection();
				ps = con.prepareStatement(VAL_NAME_EXISTS);
				ps.setString(1, colname);
				ps.setInt(2, deptid);
				ps.setInt(3, medicineid);
				ps.setString(4, batchno);
				ps.setString(5, expvalue);
				bean = (BasicDynaBean)DataBaseUtil.queryToDynaList(ps).get(0);
				String val = bean.get("validatename").toString();
				flag = val.equalsIgnoreCase("t") || val.equalsIgnoreCase("true");
			} finally {
				DataBaseUtil.closeConnections(con, ps);
			}
			return  flag;
		}


		public static final String GET_INV_FIELDS = "select gd.medicine_id,gd.batch_no, sinv.supplier_id,sinv.invoice_no, s.supplier_name,s.cust_supplier_code,gd.grn_no, "
			+" gm.store_id from store_invoice sinv join store_grn_main gm using (supplier_invoice_id) join store_grn_details gd using (grn_no) "
			+" join supplier_master s on sinv.supplier_id = s.supplier_code "
			+" where sinv.invoice_no::text = ? and debit_note_no is null " ;

		public static boolean setStkDetWithInvoiceInfo(String invoiceno, String supplier, String oldinv, String oldsupplier) throws Exception{
			Connection con = DataBaseUtil.getConnection();
			boolean status = true;
			ResultSet rs = null;
			String myinv = invoiceno;
			try (PreparedStatement ps = con.prepareStatement(GET_INV_FIELDS)) {
				ps.setString(1, myinv);
				rs = ps.executeQuery();
				while (rs.next()){
					StockDetInvoiceDTO stockDet = new StockDetInvoiceDTO();
					stockDet.setMedicineid(rs.getString("medicine_id"));
					stockDet.setBatchno(rs.getString("batch_no"));
					stockDet.setSupplierid(rs.getString("supplier_id"));
					stockDet.setSuppliername(rs.getString("supplier_name"));
					stockDet.setGrnno(rs.getString("grn_no"));
					stockDet.setStoreId(rs.getInt("store_id"));
					stockDet.setInvoiceno(myinv);
					status = updateStkDetWithInvoiceInfo(stockDet, oldinv, oldsupplier);
				}
			}finally {
			  if (rs != null) {
			    rs.close();
			  }
			  DataBaseUtil.closeConnections(con, null);
		  }

			return status;



		}



		public static boolean updateStkDetWithInvoiceInfo(StockDetInvoiceDTO stockDetail,String oldinv, String oldsupplier) throws Exception{
			String myoldsupplier = oldsupplier;
			String myoldinv = oldinv;
			boolean success = false;
			Connection con = null;
			StockDetInvoiceDTO myStockDetail = stockDetail;
			Map<Integer, Object> keys = new HashMap<Integer, Object>();
			boolean exists = false;
			boolean oldexists = false;
			int j = 1;
			try{
				con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);
				int storeId = myStockDetail.getStoreId();
				int medicineid = Integer.parseInt(myStockDetail.getMedicineid());
				String batchno = myStockDetail.getBatchno();
				String oldsuppName = StoresDBTablesUtil.suppIdToName(myoldsupplier);
				String suppname =  myStockDetail.getSuppliername();
				StringBuilder UPDATE_STOCK = new StringBuilder("UPDATE store_stock_details set ");
				exists = StockEntryDAO.valNameExists("item_supplier_name", storeId,
						medicineid, batchno, suppname);
				if (!exists) {


					/**The old supplier is already a part of commacat string, so just need to replace the old supplier ref. with the new supplier ref.
					 * We should not be appending this supplier again
					 */
					if (!(myStockDetail.getSupplierid().equalsIgnoreCase(myoldsupplier))){
						keys.put(j++, oldsuppName);
						keys.put(j++, suppname);
						UPDATE_STOCK.append(" item_supplier_name=regexp_replace(item_supplier_name,?,?),");

						//supplier code
						keys.put(j++, myoldsupplier);
						keys.put(j++, myStockDetail.getSupplierid());
						UPDATE_STOCK.append(" item_supplier_code=regexp_replace(item_supplier_code,?,?),");
					}

				}
				exists = StockEntryDAO.valNameExists("item_invoice_no", storeId,
						medicineid, batchno, myStockDetail.getInvoiceno());
				if (!exists) {

					/**The old invoice is already a part of commacat string, so just need to replace the old invoice no. with the new invoice no.
					 * We should not be appending this invoice again
					 */
					if (!(myStockDetail.getInvoiceno().equalsIgnoreCase(myoldinv))){
						keys.put(j++, myoldinv);
						keys.put(j++, myStockDetail.getInvoiceno());
						UPDATE_STOCK.append(" item_invoice_no=regexp_replace(item_invoice_no,?,?),");
					}

				}

				keys.put(j++,"StockEntry");
				keys.put(j++,Integer.parseInt(myStockDetail.getMedicineid()));
				keys.put(j++,myStockDetail.getBatchno());
				keys.put(j++,myStockDetail.getStoreId());
				UPDATE_STOCK.append(" CHANGE_SOURCE=? WHERE MEDICINE_ID=? AND BATCH_NO=? AND dept_id=? ");
				success = StoresDBTablesUtil.updateTableWithDynamicQuery(con,keys,UPDATE_STOCK);
				if (!success) log.error("Update Stock failed in Store Stock Entry");
				if (success) {
					con.commit();
					if (con!=null) con.close();
				} else {
					log.error("Error in transaction, rolling back");
					con.rollback();
					if (con!=null) con.close();
				}

			} catch(Exception e) {
				success = false;
				log.error("Caught exception, rolling back", e);
				if (con!=null) con.rollback();
			}
			return success;

		}

	public static final String GET_GRN_ITEMS=
		" SELECT sitd.medicine_name, sitd.issue_base_unit, sgd.item_order, " +
		"  sgm.grn_no, sgm.po_no, TO_CHAR(sgm.grn_date,'DD-MON-YY') AS grn_date, sgd.batch_no, " +
		"  grn_pkg_size, TO_CHAR(sibd.exp_dt,'MM-YYYY') AS exp_date, sm.supplier_name,sm.cust_supplier_code, " +
		"  sitd.service_sub_group_id, ssg.service_group_id, " +
		"  REPLACE(sm.supplier_address,'~','') AS supplier_address, " +
		"  COALESCE(supplier_city,'') AS city_name, COALESCE(supplier_country,'') AS country_name, " +
		"  COALESCE(supplier_state,'') AS state_name, sinv.invoice_no, " +
		"  ((SELECT SUM ((((sgd.billed_qty/sgd.grn_pkg_size) * sgd.cost_price) " +
		"     - (sgd.discount+sgd.scheme_discount) + sgd.tax+sgd.item_ced)) AS amt " +
		"    FROM store_grn_details sgd,store_grn_main sgm " +
		"    WHERE sgd.grn_no=sgm.grn_no AND sgm.supplier_invoice_id=sinv.supplier_invoice_id " +
		"    GROUP BY sgm.supplier_invoice_id) - sinv.discount + sinv.round_off ) AS final_amt, " +
		"  COALESCE(sm.supplier_tin_no,'') AS supplier_tin,sgd.discount,sgd.scheme_discount,sgd.bonus_qty, " +
		"  TO_CHAR(sinv.invoice_date,'DD-MON-YY') AS invoice_date,sgd.item_ced_per,sgd.item_ced, " +
		"  sgd.billed_qty, sgd.bonus_qty, sgd.mrp, sgd.cost_price, sgd.discount,sgd.scheme_discount, " +
		"  sgd.outgoing_tax_rate as tax_rate,sgd.tax, " +
		"  sgd.adj_mrp, sinv.po_reference, COALESCE(sinv.discount,0) AS invdisc, " +
		"  COALESCE(sinv.round_off,0) AS round_off, " +
		"  (sgd.billed_qty/sgd.grn_pkg_size*sgd.cost_price) - (sgd.discount+sgd.scheme_discount) + sgd.tax + sgd.item_ced AS amt, "+
		"  sgd.tax_type, sgm.user_name, s.dept_name as store_name, " +
		"  (sgd.tax * sinv.cess_tax_rate)/100 as cess_tax_amount, sgm.supplier_invoice_id, sp.po_pkg_size," +
		"  CASE WHEN spm.po_date IS NULL THEN '' ELSE TO_CHAR(spm.po_date,'DD-MON-YY') END as po_date," +
		"  sp.qty_req AS pobillqty, sp.bonus_qty_req AS pobonusqty, sgm.grn_qty_unit, sitd.package_uom, " +
		"  issue_units, scm.category as item_category, sm.cust_supplier_code, sitd.cust_item_code, sm.drug_license_no, sm.pan_no, sm.cin_no, "+
		"  s.pharmacy_tin_no, hcms.tin_number "+
		" FROM store_grn_details sgd " +
		"  JOIN store_grn_main sgm ON (sgm.grn_no = sgd.grn_no) " +
		"  JOIN store_invoice sinv USING (supplier_invoice_id) " +
		"  JOIN store_item_batch_details sibd ON (sibd.item_batch_id = sgd.item_batch_id) " +
		"  JOIN supplier_master sm on sm.supplier_code = sinv.supplier_id " +
		"  JOIN store_item_details sitd ON sgd.medicine_id = sitd.medicine_id " +
		"  JOIN stores s on (s.dept_id=sgm.store_id) " +
		"  LEFT JOIN hospital_center_master hcms ON (hcms.center_id=s.center_id) "+
		"  JOIN service_sub_groups ssg ON (ssg.service_sub_group_id = sitd.service_sub_group_id) " +
		"  LEFT JOIN store_po_main spm on (spm.po_no = sgm.po_no) "+
		"  LEFT JOIN store_po SP on sp.po_no=spm.po_no and sp.medicine_id=sgd.medicine_id " +
		"  LEFT JOIN store_category_master scm on (scm.category_id = sitd.med_category_id) " +
		" WHERE sgm.grn_no=? ORDER BY sgd.item_order";

	// used for print GRN.
	public static List<BasicDynaBean> getGRNitemsList(String grnNo)
			throws SQLException {

		PreparedStatement ps = null;
		Connection con = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_GRN_ITEMS);
			ps.setString(1, grnNo);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
			if (ps != null) {
			  ps.close();
			}
		}
	}

	public boolean updateIndentItems (List<BasicDynaBean> beans, int medicineId,String poNo,BigDecimal qtyplus,String userName) throws SQLException,IOException{
		StoresIndentProcessDAO dao = new StoresIndentProcessDAO (con);
		boolean success = true;
		for (BasicDynaBean bean : beans) {
			int indentNo = (int) bean.get("indent_no");
			String indentType = DataBaseUtil.getStringValueFromDb("select indent_type from store_indent_main where indent_no="+indentNo);
			if (success && indentType.equalsIgnoreCase("R")) {
				success = dao.updateIndentItem(con, indentNo, medicineId, qtyplus);     //indent details item status
				if(success) success = dao.updateIndentStatus(con,indentNo,userName);    //indent main status
			}
		}
		return success;
	}

	public static boolean isCategoryAssetTracking (int catId){
		String query = "select asset_tracking from store_category_master where category_id=" + catId;
		String result = DataBaseUtil.getStringValueFromDb(query);
		return result.equalsIgnoreCase("Y");
	}

	private static final String CHECK_ASSET_EXIST = "SELECT * from fixed_asset_master where asset_dept=? and asset_id=? and asset_serial_no=?";

	public static boolean checkFixedAsset (int dept,int assetId,String serialNo) throws SQLException {
		Connection con = DataBaseUtil.getReadOnlyConnection();ResultSet rs = null;boolean count = true;
		try (PreparedStatement ps = con.prepareStatement(CHECK_ASSET_EXIST)) {
			
			ps.setInt(1, dept);
			ps.setInt(2, assetId);
			ps.setString(3, serialNo);
			rs = ps.executeQuery();
			while (rs.next()){
				count = false ;
			}
			return count;
		} finally {
		  if (rs != null) {
		    rs.close();
		  }
			DataBaseUtil.closeConnections(con, null);
		}
	}

	public static final String GET_LINEN_ITEMS_FOR_WARD = " select distinct(medicine_name) " +
			" from store_stock_details_view s join ward_names ward on (s.dept_id = ward.store_id) where category_name ilike 'Linen' and ward.ward_no = ?" ;

     public static List getLinenItemsForWard(String wardId) throws SQLException{
         List l =null;
         Connection con = null;
         PreparedStatement ps = null;
         con = DataBaseUtil.getReadOnlyConnection();
         ps = con.prepareStatement(GET_LINEN_ITEMS_FOR_WARD);
         try {
        	 ps.setString(1, wardId);
        	 l = DataBaseUtil.queryToArrayList(ps);
         } finally {
             DataBaseUtil.closeConnections(con, ps);
         }

         return l;
     }

     public static final String GET_LINEN_ITEMS = " select distinct(medicine_name) " +
		" from store_stock_details_view where category_name ilike 'Linen' " ;

	public static List getLinenItems() throws SQLException{
	  List l =null;
	  Connection con = null;
	  PreparedStatement ps = null;
	  con = DataBaseUtil.getReadOnlyConnection();
	  ps = con.prepareStatement(GET_LINEN_ITEMS);
	  try {
	 	 l = DataBaseUtil.queryToArrayList(ps);
	  } finally {
	      DataBaseUtil.closeConnections(con, ps);
	  }

	  return l;
	}

     public static BigDecimal getMRP (int itemBatchId) throws SQLException{
    	 Connection con = null;
    	 PreparedStatement ps =null;BigDecimal mrp = null;
    	 try {
    		 con = DataBaseUtil.getReadOnlyConnection();
    		 ps = con.prepareStatement("select mrp from store_item_batch_details where item_batch_id = ?");
    		 ps.setInt(1, itemBatchId);
    		 mrp = DataBaseUtil.getBigDecimalValueFromDb(ps);
    	 } finally{
    		 DataBaseUtil.closeConnections(con, ps);
    	 }
    	 return mrp;
     }

     public static BigDecimal getCP (int storeId,int medicineId,String batchNo) throws SQLException{
    	 Connection con = null;
    	 PreparedStatement ps =null;BigDecimal cp = null;
    	 try {
    		 con = DataBaseUtil.getReadOnlyConnection();
    		 ps = con.prepareStatement("select package_cp from store_stock_details where dept_id=? and medicine_id=? and batch_no=?");
    		 ps.setInt(1, storeId);
    		 ps.setInt(2, medicineId);
    		 ps.setString(3, batchNo);
    		 cp = DataBaseUtil.getBigDecimalValueFromDb(ps);
    	 } finally{
    		 DataBaseUtil.closeConnections(con, ps);
    	 }
    	 return cp;
     }



     public static Map getItemBarcodeDetails (String grnno) throws SQLException{
    	 Connection con = null;
    	 PreparedStatement ps =null;
    	 HashMap ItemDetails = new HashMap();
    	 String query = "SELECT g.medicine_id,medicine_name,item_barcode_id,mrp,(billed_qty+bonus_qty)/grn_pkg_size as qty,"
    		 +" (medicine_name || batch_no) as comb,sm.supplier_name,sm.cust_supplier_code,batch_no,exp_dt,dept_name, identification " +
    		 		"FROM store_grn_details as g "
    		 +" join store_grn_main gm using(grn_no)"
             +" join store_item_details i using(medicine_id)"
             +" join store_invoice si using(supplier_invoice_id)"
             +" join supplier_master sm on sm.supplier_code=si.supplier_id "
             +" join stores on store_id=dept_id"
             +" join store_category_master scm ON(scm.category_id = i.med_category_id) "
    		 +" where grn_no=? and item_barcode_id != ''";
    	 try {
    		 con = DataBaseUtil.getReadOnlyConnection();
    		 ps = con.prepareStatement(query);
    		 ps.setString(1, grnno);
    		 List list = DataBaseUtil.queryToDynaList(ps);
    		 ItemDetails = DataBaseUtil.mapDynaRowSet(list, "comb");
    	 } finally{
    		 DataBaseUtil.closeConnections(con, ps);
    	 }
    	 return ItemDetails;
     }

     public static Map getItemBarcodeDetails (int itemId,String noOfPrint,String item_batch_id) throws SQLException{
    	 Connection con = null;
    	 PreparedStatement ps =null;
    	 HashMap ItemDetails = new HashMap();
    	 String selectquery = "select medicine_id,medicine_name,item_barcode_id, *noOfPrint* as qty, *mrp* as mrp,  "
        		 +" medicine_name as comb,'' as supplier_name, *batch_no* as batch_no, *exp_dt* as exp_dt,'' as dept_name from store_item_details sid "
        		 +" *join*   where medicine_id = ? and item_barcode_id != ''  *item_batch_id* ";
    	 try {
    		 con = DataBaseUtil.getReadOnlyConnection();
    		 String finalquery = "";
    		 String query = "";
    		 if(noOfPrint == null || "".equals(noOfPrint)){
    			 query = selectquery.replace("*noOfPrint*", ""+1);
    		 }else{
    			 query = selectquery.replace("*noOfPrint*", ""+noOfPrint);
    		 }
    		 
    		 if(item_batch_id == null || item_batch_id.equals("")){
    			 finalquery =  query.replace("*mrp*", ""+0).replace("*batch_no*", "''").replace("*exp_dt*", ""+null).replace("*join*", "").replace("*item_batch_id*", "");
    		 }else{
    			 finalquery = query.replace("*mrp*", "sibd.mrp").
    					 replace("*batch_no*", "sibd.batch_no").replace("*exp_dt*", "sibd.exp_dt").
    					 replace("*join*", "join store_item_batch_details sibd using(medicine_id) ").
    					 replace("*item_batch_id*", "and item_batch_id=? "); 
    		 }
    		 ps = con.prepareStatement(finalquery);
    		 ps.setInt(1, itemId);
    		 if(item_batch_id != null && !"".equals(item_batch_id)){
    			 ps.setInt(2, Integer.parseInt(item_batch_id));
    		 }
    		 List list = DataBaseUtil.queryToDynaList(ps);
    		 ItemDetails = DataBaseUtil.mapDynaRowSet(list, "comb");
    	 } finally{
    		 DataBaseUtil.closeConnections(con, ps);
    	 }
    	 return ItemDetails;
     }

     public static BasicDynaBean getInvoiceRow(int invoiceId)
		throws SQLException {
    	 Connection con = null;
    	 try{
    		 con = DataBaseUtil.getReadOnlyConnection();
    		 return getInvoiceRow(con,invoiceId);
    	 }finally{
    		 DataBaseUtil.closeConnections(con, null);
    	 }
     }

     public static BasicDynaBean getInvoiceRow(Connection con,int invoiceId)
		throws SQLException {

		PreparedStatement ps = null;
		String grnbquery = "select supplier_id, invoice_no, invoice_date, due_date, po_no, po_reference, "
           +" discount, round_off, status, payment_id, discount_type, discount_per,"
           +" date_time, other_charges, other_charges_remarks, remarks, paid_date, "
           +" payment_remarks, cess_tax_rate, cess_tax_amt, tax_name, cst_rate, "
           +" account_group, supplier_invoice_id, consignment_stock, debit_amt, "
           +" cash_purchase, invoice_file_name, transportation_charges, tcs_amount "
           +" from store_invoice where supplier_invoice_id=?";
		try {
			ps = con.prepareStatement(grnbquery);
			ps.setInt(1, invoiceId);
			List list = DataBaseUtil.queryToDynaList(ps);
			if (list.size() > 0) {
				return (BasicDynaBean) list.get(0);
			} else {
				return null;
			}
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
     }

     public static String getMaxCPGRN (Connection con,int store,int medid,String batch)throws SQLException {
    	 PreparedStatement ps = null;
 			String grnquery = "select grn_no from store_grn_main "
 				+ " join store_grn_details using(grn_no)"
 				+ " where store_id=? and medicine_id=? and"
 				+ " batch_no=? and debit_note_no is null order by cost_price desc limit 1";
 			try {
 				ps = con.prepareStatement(grnquery);
 				ps.setInt(1, store);
 				ps.setInt(2, medid);
 				ps.setString(3, batch);
 				return DataBaseUtil.getStringValueFromDb(ps);
 			} finally {
 				DataBaseUtil.closeConnections(null, ps);
 			}
     }

     public static String getLastCPGRN (Connection con,int store,int medid,String batch)throws SQLException {
    	 PreparedStatement ps = null;
 			String grnquery = "select grn_no from store_grn_main "
 				+ " join store_grn_details using(grn_no)"
 				+ " where store_id=? and medicine_id=? and"
 				+ " batch_no=? and debit_note_no is null order by grn_date desc limit 1";
 			try {
 				ps = con.prepareStatement(grnquery);
 				ps.setInt(1, store);
 				ps.setInt(2, medid);
 				ps.setString(3, batch);
 				return DataBaseUtil.getStringValueFromDb(ps);
 			} finally {
 				DataBaseUtil.closeConnections(null, ps);
 			}
     }

     public static BasicDynaBean chkItemProp(int medid) throws SQLException {
    	 PreparedStatement ps = null;
 		String query = "select medicine_id,mf.status AS manf_status, scm.status AS cat_status,gn.status AS gen_status "
 			+" ,std.status as itemstatus,mf.manf_name,gn.generic_name,scm.category FROM store_item_details std "
 			+" JOIN manf_master mf ON ( mf.manf_code = std.manf_name ) "
 			+" JOIN  store_category_master scm ON (category_id = med_category_id ) "
 			+" LEFT JOIN generic_name gn ON ( std.generic_name = generic_code) "
 			+" where medicine_id in (?)";
 		Connection con = DataBaseUtil.getReadOnlyConnection();
 		try {
			ps = con.prepareStatement(query);
			ps.setInt(1, medid);
			List list = DataBaseUtil.queryToDynaList(ps);
			if (list.size() > 0) {
				return (BasicDynaBean) list.get(0);
			} else {
				return null;
			}	
		} 
 		finally {
			DataBaseUtil.closeConnections(con, ps);
		}
     }

    public static final String GET_INVOICE_NOS = " SELECT invoice_no FROM store_invoice"
    +" WHERE supplier_id = ? and invoice_date::text >= ? and invoice_date::text <= ?";

	public static List getInvoiceNos(String suppId, String fromDate, String toDate) throws SQLException {
		List l =null;
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_INVOICE_NOS);
			ps.setString(1, suppId);
			ps.setString(2, fromDate);
			ps.setString(3, toDate);
			l = DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return l;
	}


	private static final String GET_PREV_COST_AVG =
		" SELECT  AVG(st.package_cp) AS curr_package_cp, SUM(qty) AS cur_qty "+
		" FROM store_stock_details st JOIN store_item_details it USING (medicine_id) " +
		" WHERE medicine_ID=? and batch_no=? " +
		" LIMIT 1";

	public static BasicDynaBean getPreviousCostAvg(int itemId, String batchNo) throws SQLException {
		PreparedStatement ps = null;Connection con = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_PREV_COST_AVG);
			ps.setInt(1, itemId);
			ps.setString(2, batchNo);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_UPLOADED_DOC =
		" SELECT invoice_file_name, supplier_invoice_attachment, invoice_contenttype " +
		" FROM store_invoice " +
		" WHERE supplier_invoice_id=?";

	public static Map getUploadedDocInfo(int supplierInvoiceId) throws SQLException {
    	Map<String,Object> upload = new HashMap<String,Object>();
    	Connection con = null;
    	PreparedStatement ps = null;
    	ResultSet rs = null;
    	try {
    		con = DataBaseUtil.getReadOnlyConnection();
    		ps = con.prepareStatement(GET_UPLOADED_DOC);
    		ps.setInt(1, supplierInvoiceId);
    		rs = ps.executeQuery();
    		if (rs.next()) {
    			String filename = rs.getString(1);
    			upload.put("filename", filename);
    			InputStream uploadfile = rs.getBinaryStream(2);
    			upload.put("uploadfile", uploadfile);
    			String contenttype = rs.getString(3);
    			upload.put("contenttype", contenttype);
    		}
    	} finally {
    		DataBaseUtil.closeConnections(con, ps, rs);
    	}
    	return upload;
    }

	private static final String ITEM_DETAILS =
		   "SELECT pmd.medicine_id,issue_type,identification,package_type,"
	 	 + " coalesce(" +
	 	 "				(SELECT sum(qty) " +
	 	 "					FROM store_stock_details ssd" +
	 	 "					JOIN store_item_batch_details sibd USING(item_batch_id) " +
	 	 "					WHERE dept_id = ? and ssd.medicine_id= ? AND ( exp_dt is null or sibd.exp_dt >= current_date ))" +
	 	 "				,0) as qty_avbl, "
	 	 + " issue_base_unit, issue_units,category,medicine_name,package_uom,m.manf_name,package_uom,sic.item_code, sic.code_type,pmd.cust_item_code "
		 + " FROM store_item_details pmd "
		 + " LEFT JOIN ha_item_code_type hict ON (hict.medicine_id = pmd.medicine_id AND hict.health_authority=?) "
		 + " LEFT JOIN store_item_codes sic ON (sic.medicine_id = pmd.medicine_id AND sic.code_type = hict.code_type) "
		 + " JOIN manf_master m ON ( pmd.manf_name = m.manf_code )"
		 + " join store_category_master on med_category_id=category_id WHERE pmd.medicine_id= ? ";

	public static List<BasicDynaBean> getItemDetails(String item_name,int store_id, String healthAuthority) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		List l = null;
		try {
			con = DataBaseUtil.getConnection();
			String itemid = StoresDBTablesUtil.itemNameToId(item_name);
			if( itemid != null && !itemid.equals("")) {
				ps = con.prepareStatement(ITEM_DETAILS);
				ps.setInt(1, store_id);
				ps.setInt(2, Integer.parseInt(itemid));
				ps.setString(3, healthAuthority);
				ps.setInt(4, Integer.parseInt(itemid));
				l = DataBaseUtil.queryToDynaList(ps);
			}
		}finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return l;
	}

	public static final String GET_STORE_ITEM_DETAILS =
		" SELECT pmd.medicine_name, pmd.medicine_id, pmd.item_barcode_id, " +
		"  pmd.package_uom, pmd.issue_units, pmd.issue_base_unit,  " +
		"  ic.identification, ic.expiry_date_val, ic.billable, " +
		"  pmd.max_cost_price,COALESCE(isld.bin,pmd.bin) as bin, " +
		"  pmd.tax_type,pmd.tax_rate,ic.category,pmd.cust_item_code " +
		"  FROM store_item_details pmd " +
		"  JOIN store_category_master ic ON (pmd.med_category_id=ic.category_id) " +
		"  LEFT JOIN item_store_level_details  isld ON isld.medicine_id = pmd.medicine_id AND isld.dept_id = ? " +
		"  WHERE pmd.medicine_id=?";

	public static BasicDynaBean getItemDetails(int itemId, int storeId)
		throws SQLException {
		return DataBaseUtil.queryToDynaBean(GET_STORE_ITEM_DETAILS, new Object[]{storeId, itemId});
    }
	private static final String GET_REORDER_LEVEL=" SELECT sid.medicine_id,S.dept_id,sid.medicine_name,S.dept_name,srl.bin,srl.danger_level,srl.min_level,srl.reorder_level,srl.max_level,"
			+ " srl.danger_uom "
			+ " FROM item_store_level_details  as srl"
			+ " left join store_item_details sid on(sid.medicine_id=srl.medicine_id)" 
			+ " left join stores S on(S.dept_id=srl.dept_id)"
			+ " order by srl.medicine_id ";
			
	public static List<BasicDynaBean> reorderLevelDetails()throws SQLException {

        List list = null;
        PreparedStatement ps = null;
        Connection con = null;
        try {
	        con = DataBaseUtil.getConnection();
	        	ps = con.prepareStatement(GET_REORDER_LEVEL);
	        list = DataBaseUtil.queryToDynaList(ps);
        }finally{
        	DataBaseUtil.closeConnections(con, ps);
        }

        return list;
    }
	
	private static final String GET_CODE_TYPE_STORE_ITEM_DETAILS =" SELECT  sid.medicine_id,sid.medicine_name,sid.medicine_short_name,sid.item_barcode_id,"
			+ " sid.status,sid.batch_no_applicable,sid.item_strength,sid.item_strength_units,"
			+ " ifm.item_form_name,mm.manf_name,gn.generic_name,sid.tax_rate,sid.tax_type ,"
			+ " sid.bin,sid.package_type,sid.package_uom,sid.issue_units as issue_uom,c.category,"
			+ " ssg.service_sub_group_name,sict.control_type_name,sid.max_cost_price,sid.value,"
			+ " iic.insurance_category_name,sid.prior_auth_required,sic.code_type,sic.item_code,"
			+ " sid.item_selling_price, sid.high_cost_consumable, sid.cust_item_code "
			+ " FROM store_item_details as sid "
			+ " LEFT JOIN store_item_codes sic ON(sic.medicine_id = sid.medicine_id) "
			+ " left join item_form_master ifm on(ifm.item_form_id=sid.item_form_id) "			
			+ " left join store_category_master as c on(c.category_id=sid.med_category_id)"
			+ " left join service_sub_groups as ssg on(ssg.service_sub_group_id=sid.service_sub_group_id)"
			+ " left join store_item_controltype  as sict on(sict.control_type_id=sid. control_type_id) "
			+ " left join item_insurance_categories as iic on(iic.insurance_category_id=sid.insurance_category_id)"
			+ " left join manf_master mm on(mm.manf_code=sid.manf_name)"
			+ " left join generic_name gn on(gn.generic_code=sid.generic_name) "
			+ " WHERE (sic.code_type = ? OR (code_type is null OR code_type = '')) ";
	
	private static final String GET_CODE_TYPE_STORE_ITEM_DETAILS_ORDER_BY = " ORDER BY medicine_id";
	
	public static List<BasicDynaBean> storeItemDetails(String codeTypes)throws SQLException {

        List list = null;
        PreparedStatement ps = null;
        Connection con = null;
        try {
	        con = DataBaseUtil.getConnection();
	        String query = GET_CODE_TYPE_STORE_ITEM_DETAILS + GET_CODE_TYPE_STORE_ITEM_DETAILS_ORDER_BY;
	        	ps = con.prepareStatement(query);
	        	ps.setString(1, codeTypes);
	        	list = DataBaseUtil.queryToDynaList(ps);
        }finally{
        	DataBaseUtil.closeConnections(con, ps);
        }

        return list;
    }
	
	public int getGrnCount(String poNo) throws SQLException{

		int grnCount = 0;
		BasicDynaBean grnCountBean = null;
		try{

			grnCountBean = DataBaseUtil.queryToDynaBean("SELECT COUNT(*) as grn_count FROM store_grn_main WHERE PO_NO=?", poNo);
			if ( grnCountBean != null ){
				grnCount = Integer.parseInt(grnCountBean.get("grn_count").toString());
			}
		
		}catch(Exception e){
			log.error(Arrays.toString(e.getStackTrace()));
		}
		
		return grnCount;
	}
	//OverLoaded method to filter based on Active/Inactive to download

	public static List<BasicDynaBean> storeItemDetails(String codeTypes, String codeStatus)throws SQLException {

        List list = null;
        Connection con = DataBaseUtil.getConnection();
        String activeInactive = (!"".equals(codeStatus) && !codeStatus.equals("All") && codeStatus.equals("A")) ? "A" : "I";

        String query = GET_CODE_TYPE_STORE_ITEM_DETAILS;
        if (codeStatus != null && !codeStatus.equals("") && !codeStatus.equals("All")) {
          query = query + "AND sid.status = ?";
        }
        int ival = 1;
        query = query + GET_CODE_TYPE_STORE_ITEM_DETAILS_ORDER_BY ;
        try (PreparedStatement ps = con.prepareStatement(query)) {
	        
	       	ps.setString(ival, codeTypes);
	       	if (codeStatus != null && !codeStatus.equals("") && !codeStatus.equals("All")) {
	       	  ival++;
	       		ps.setString(ival, activeInactive);
	       	}
	       	list = DataBaseUtil.queryToDynaList(ps);
        }finally{
        	DataBaseUtil.closeConnections(con, null);
        }
        return list;
    }
	
	public static List getGRNTaxDetails(String grnNo) throws SQLException {
		return DataBaseUtil.queryToDynaList("SELECT sptd.*, UPPER(isg.item_subgroup_name) as item_subgroup_name,ig.* "
				+ " FROM store_grn_tax_details sptd JOIN item_sub_groups isg ON (sptd.item_subgroup_id = isg.item_subgroup_id) "
				+ " JOIN item_groups ig ON (isg.item_group_id = ig.item_group_id)"
				+ " WHERE isg.status='A' AND sptd.grn_no = ? ", grnNo);
	}
	
	public static final String GET_DEBIT_TAX_DETAILS = "SELECT sptd.*, UPPER(isg.item_subgroup_name) as item_subgroup_name,ig.* "
				+ " FROM store_grn_main sgm "
				+ " JOIN store_grn_tax_details sptd ON (sptd.grn_no = sgm.grn_no)"
				+ " JOIN item_sub_groups isg ON (sptd.item_subgroup_id = isg.item_subgroup_id) "
				+ " JOIN item_groups ig ON (isg.item_group_id = ig.item_group_id)"
				+ " WHERE isg.status='A' AND sgm.debit_note_no = ?";
	public static List getDebitNoteTaxDetails(String debitNoteNo) throws SQLException {
		return DataBaseUtil.queryToDynaList(GET_DEBIT_TAX_DETAILS, debitNoteNo);
	}
	
	private static final String GET_TAX_DETAILS="SELECT sgtd.medicine_id,sgtd.grn_no,sgtd.item_batch_id,sgm.po_no, sgd.batch_no,sgd.item_order,"+  
			" ig.item_group_name,isb.item_subgroup_name,sgtd.tax_rate,sgtd.tax_amt "+
			" FROM store_grn_details sgd "+  
			" JOIN store_grn_main sgm ON (sgm.grn_no = sgd.grn_no) "+ 
			" JOIN store_grn_tax_details sgtd ON (sgtd.grn_no = sgd.grn_no AND sgtd.medicine_id = sgd.medicine_id AND sgtd.item_batch_id = sgd.item_batch_id) "+
			" JOIN item_sub_groups isb ON isb.item_subgroup_id = sgtd.item_subgroup_id "+
			" JOIN item_groups ig ON ig.item_group_id = isb.item_group_id "+
			" WHERE sgm.grn_no=? ORDER BY sgd.item_order";

	public static List<BasicDynaBean> getTaxDetails(String grnNo) throws SQLException {

		PreparedStatement ps = null;
		Connection con = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_TAX_DETAILS);
			ps.setString(1, grnNo);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	public static final String GET_LATEST_GRN_DETAILS = "SELECT sgm.grn_no FROM store_grn_main sgm "
			+ " JOIN store_invoice si ON (sgm.supplier_invoice_id = si.supplier_invoice_id) "
			+ " JOIN store_grn_details sgd ON (sgm.grn_no = sgd.grn_no) "
			+ " WHERE sgm.store_id = ? AND si.supplier_id = ? AND sgd.medicine_id = ? AND sgd.item_batch_id =? "
			+ " ORDER BY sgm.grn_date DESC limit 1 ";
	public static BasicDynaBean getLatestGrnDetails(String deptIdStr, String supplierId, String medicineIdStr, String batchIdStr) throws SQLException {
		int medicineId = Integer.parseInt(medicineIdStr);
		int deptId = Integer.parseInt(deptIdStr);
		int itemBatchId = Integer.parseInt(batchIdStr);
		return DataBaseUtil.queryToDynaBean(GET_LATEST_GRN_DETAILS, new Object[] { deptId, supplierId, medicineId, itemBatchId});
	}
	
	public static final String GET_MED_GRN_TAX_DETAILS = "SELECT * FROM store_grn_tax_details WHERE medicine_id = ? AND grn_no = ? AND item_batch_id =?";
	public static List getMedGrnTaxDetails(String medicineIdStr, String batchIdStr, String grnNo) throws SQLException {
		int itemBatchId = Integer.parseInt(batchIdStr);
		int medicineId = Integer.parseInt(medicineIdStr);
		return DataBaseUtil.queryToDynaList(GET_MED_GRN_TAX_DETAILS, new Object[] { medicineId, grnNo, itemBatchId});
	}

}
