package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.QueryBuilder;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class MedicineStockDAO {

	static Logger log = LoggerFactory.getLogger(MedicineStockDAO.class);

	Connection con = null;

	public MedicineStockDAO(Connection con) {
		this.con = con;
	}

	/*
	 * Writes a (big) list of medicines available in the stock, grouped by all stores.
	 * into the given writer as JSON.
	 *
	 * This may include medicines with 0 stock depending on the includeZeroStock flag.
	 * But, if the medicine status is Inactive, and the stock is 0, we always exclude it.
	 *
	 * Note that includeZeros should be false only when selling medicines. For sales return
	 * or for adjustments, do not pass includeZeros as false, always pass it as true.
	 *
	 * Preferences have to be handled by the callee, this is because the cache depends on
	 * the preference, and the URL needs to be different for different preferences.
	 *
	 * We always return expired medicines so that the UI can alert the user saying there are
	 * items but expired. The preference only affects the UI alert, not this query.
	 */
	private static final String GET_MEDICINES_IN_STOCK_STORE_WISE =
		" SELECT DISTINCT m.medicine_name, m.medicine_id,m.cust_item_code, item_barcode_id,issue_units,m.package_uom,issue_base_unit,sic.item_code, " +
		" CASE WHEN m.cust_item_code IS NOT NULL AND  TRIM(m.cust_item_code) != ''  THEN m.medicine_name||' - '||m.cust_item_code ELSE m.medicine_name END as cust_item_code_with_name, "+
		" CASE WHEN m.cust_item_code IS NOT NULL AND  TRIM(m.cust_item_code) != '' AND item_barcode_id IS NOT NULL AND  TRIM(item_barcode_id) != '' " +
		" THEN m.medicine_name||' - '||m.cust_item_code||' - '||item_barcode_id  " +
		" WHEN m.cust_item_code IS NOT NULL AND  TRIM(m.cust_item_code) != ''  THEN m.medicine_name||' - '||m.cust_item_code " +
		" WHEN item_barcode_id IS NOT NULL AND  TRIM(item_barcode_id) != ''  THEN m.medicine_name||' - '||item_barcode_id " +
		" ELSE m.medicine_name END as cust_item_code_barcode_with_name " +
		" FROM store_stock_details msd " +
		" JOIN store_item_details m USING(medicine_id) " +
		" JOIN store_category_master icm ON (icm.category_id = m.med_category_id)" +
		" LEFT JOIN ha_item_code_type hict ON (hict.medicine_id = m.medicine_id AND hict.health_authority=?) " +
		" LEFT JOIN store_item_codes sic ON (sic.medicine_id = m.medicine_id AND sic.code_type = hict.code_type) " +
		" WHERE msd.dept_id=?   ";

	private static final String GET_STORE_LIST="SELECT DISTINCT dept_id from stores WHERE status='A'";
	private static final String GET_SALES_STORE_LIST =
		"SELECT DISTINCT dept_id FROM stores WHERE status='A' AND is_sales_store = 'Y'";

	public static void writeMedicineNamesInStock(java.io.Writer w, boolean includeZeroStock,
			Boolean retailable, Boolean billable, String[] issueType,
			boolean includeConsignment, boolean includeUnapproved, int storeId, String healthAuthority)
		throws SQLException, java.io.IOException {
		writeMedicineNamesInStock(w, includeZeroStock, retailable, billable, issueType,
				includeConsignment, includeUnapproved, false, true, storeId, healthAuthority, null);
	}
	
	public static void writeMedicineNamesInStock(java.io.Writer w, boolean includeZeroStock,
			Boolean retailable, Boolean billable, String[] issueType,
			boolean includeConsignment, boolean includeUnapproved, int storeId, String healthAuthority, String grnNo)
		throws SQLException, java.io.IOException {
		writeMedicineNamesInStock(w, includeZeroStock, retailable, billable, issueType,
				includeConsignment, includeUnapproved, false, true, storeId, healthAuthority, grnNo);
	}

	public static void writeMedicineNamesInStock(java.io.Writer w, boolean includeZeroStock,
			Boolean retailable, Boolean billable, String[] issueType,
			boolean includeConsignment, boolean includeUnapproved, boolean onlySalesStores, String healthAuthority)
		throws SQLException, java.io.IOException {
		writeMedicineNamesInStock(w, includeZeroStock, retailable, billable, issueType,
				includeConsignment, includeUnapproved, onlySalesStores, false, 0, healthAuthority, null);
	}

	public static void writeMedicineNamesInStock(java.io.Writer w, boolean includeZeroStock,
			Boolean retailable, Boolean billable, String[] issueType,
			boolean includeConsignment, boolean includeUnapproved, boolean onlySalesStores,
			boolean singleStore, int storeId, String healthAuthority, String grnNo)
		throws SQLException, java.io.IOException {

		List<Object> args = new ArrayList<>();
		List<Object> finalArgs = new ArrayList<>();
		StringBuilder query = new StringBuilder(GET_MEDICINES_IN_STOCK_STORE_WISE);
		
		if (retailable != null) {
			query.append(" AND retailable = ? ");
			args.add(retailable);
		}

		if (billable != null) {
			query.append(" AND billable = ? ");
			args.add(billable);
		}

		if (issueType != null) {
			query.append(" AND icm.issue_type IN (");
		    String[] placeholdersArr = new String[issueType.length];
		    Arrays.fill(placeholdersArr, "?");
		    query.append(StringUtils.arrayToCommaDelimitedString(placeholdersArr));
		    query.append(")");
			args.addAll(Arrays.asList(issueType));
		}

		if (!includeConsignment)
			query.append(" AND consignment_stock = false ");

		if (!includeUnapproved)
			query.append(" AND asset_approved = 'Y' ");

		if (includeZeroStock) {
			/*
			 * We have to include all medicines, except those that have zero stock AND
			 * are made inactive. Thus, if we want to get rid of a medicine from the list,
			 * user has to make it inactive, and till there is stock, it will keep showing.
			 * Only when stock becomes zero, it will be excluded from the list.
			 */
			query.append(" AND NOT (m.status='I' AND qty=0)");
		} else {
			// Disallow zero quantity selling, so we return only medicines where qty > 0
			query.append(" AND qty>0");
		}
		if (grnNo != null) {
			query.append(" AND item_grn_no = ?");
			args.add(grnNo);
		}	


		finalArgs.add(healthAuthority);
		if (singleStore) {
			finalArgs.add(storeId);
			finalArgs.addAll(args);
			DataBaseUtil.queryToJson(w, query.toString(), finalArgs.toArray());
		} else {
			List<Integer> stores = onlySalesStores ? DataBaseUtil.queryToList(GET_SALES_STORE_LIST) :
				DataBaseUtil.queryToList(GET_STORE_LIST);
			Iterator<Integer> storesIt = stores.iterator();

			w.write("{");
			while (storesIt.hasNext()) {
				List<Object> thisQueryArgs = new ArrayList<>();
				int store = (Integer) storesIt.next();
				w.write("\"" + store + "\":");
				thisQueryArgs.addAll(finalArgs);
				thisQueryArgs.add(store);
				thisQueryArgs.addAll(args);
				DataBaseUtil.queryToJson(w, query.toString(), thisQueryArgs.toArray());
				if (storesIt.hasNext())
					w.write(",");
			}
			w.write("}");
		}
	}

	private  static final String GET_CENTERAL_STORE_STOCK=GET_MEDICINES_IN_STOCK_STORE_WISE +"and msd.qty>0 ";

	public static HashMap getMedicineNamesInStockByStore(String storeId) throws SQLException {
		Connection con = null; PreparedStatement ps = null;
		try {
			int storeIdNum = -1;
			if ((storeId != null) && (storeId.trim().length() > 0)){
				storeIdNum = Integer.parseInt(storeId);
			}
			HashMap storeWiseMedMap = new HashMap();
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_CENTERAL_STORE_STOCK);
			ps.setInt(1, storeIdNum);
			storeWiseMedMap.put(storeId, DataBaseUtil.queryToArrayList(ps))	;

			return storeWiseMedMap;
		} finally {
			DataBaseUtil.closeConnections(con, ps);

		}
	}

	// TODO: this belongs in the medicine master DAO
	private static final String MEDICINE_NAME_TO_ID =
		"SELECT medicine_id FROM store_item_details WHERE medicine_name=?";

	public static String medicineNameToId(String medicineName) throws SQLException {
		Connection con = null; PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(MEDICINE_NAME_TO_ID);
			ps.setString(1, medicineName);

			return DataBaseUtil.getStringValueFromDb(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	// TODO: getMedicineNamesInDeptStock

	/*
	 * Returns the available stock for a given medicine ID in all departments
	 */
	private static final String GET_MEDICINE_STOCK_QUERY =
		"SELECT msd.*, m.medicine_name,m.cust_item_code, mf.manf_code, mf.manf_name, mf.manf_mnemonic, #," +
		"  m.issue_base_unit, m.package_type,m.bin, " +
		"  COALESCE(m.issue_units,'') AS issue_units, COALESCE(m.package_uom,'') AS master_package_uom, " +
		"  COALESCE(mc.discount,0) AS meddisc, " +
		"  mc.category, mc.identification, sic.control_type_name, g.generic_name, " +
		"  '' as indent_no, mc.billable, mc.retailable, mc.claimable, m.item_barcode_id, " +
		"  m.insurance_category_id, m.prior_auth_required,sibd.exp_dt,sibd.mrp," +
		"  COALESCE(sir.tax_rate,COALESCE(ssir.tax_rate,m.tax_rate)) as tax_rate," +
		"  COALESCE(sir.tax_type,COALESCE(ssir.tax_type,m.tax_type)) as tax_type," +
		"  (SELECT sum(qty)  FROM store_stock_details issd " +
		"		JOIN store_item_lot_details isild USING(item_lot_id) " +
		"		WHERE issd.item_batch_id=msd.item_batch_id AND purchase_type = 'B' AND dept_id = msd.dept_id ) as bonus_qty,sico.item_code " +
		"FROM " +
		"	(SELECT medicine_id,sum(qty) as qty,item_batch_id,batch_no,sum(qty_in_use) as qty_in_use,sum(qty_maint) as qty_maint, " +
		"	sum(qty_retired) as qty_retired,sum(qty_lost) as qty_lost,sum(qty_kit) as qty_kit, " +
		"	sum(qty_unknown) as qty_unknown,consignment_stock,asset_approved,dept_id " +
		"	FROM store_stock_details  GROUP BY batch_no,item_batch_id,medicine_id,consignment_stock,asset_approved,dept_id " +
		"	ORDER BY medicine_id) as msd " +
		"	JOIN store_item_batch_details sibd USING(item_batch_id)   " +
		"   JOIN store_item_details m ON(m.medicine_id = msd.medicine_id)   " +
		"   LEFT JOIN ha_item_code_type hict ON (hict.medicine_id = m.medicine_id AND hict.health_authority=?) " +
		"   LEFT JOIN store_item_codes sico ON (sico.medicine_id = m.medicine_id AND sico.code_type = hict.code_type) " +
		"   JOIN manf_master mf ON (mf.manf_code = m.manf_name)   " +
		"   JOIN store_category_master mc ON mc.category_id = m.med_category_id   " +
		"   LEFT JOIN store_item_controltype sic ON sic.control_type_id=m.control_type_id   " +
		"   LEFT JOIN generic_name g ON m.generic_name = g.generic_code  " +
		"   LEFT JOIN insurance_plan_details ipd on (m.insurance_category_id = ipd.insurance_category_id     " +
		"		AND ipd.patient_type='o' AND ipd.plan_id=0)   " +
		"   LEFT JOIN insurance_plan_main ipm on (ipm.plan_id = ipd.plan_id)   " +
		"   JOIN item_insurance_categories iic ON (iic.insurance_category_id = m.insurance_category_id)  " +
		"   LEFT JOIN store_item_rates sir ON (msd.medicine_id = sir.medicine_id AND     sir.store_rate_plan_id = 0)    " +
		"  LEFT JOIN store_item_rates ssir ON (msd.medicine_id = ssir.medicine_id AND " +
		"    ssir.store_rate_plan_id = ?) " +
		"   LEFT JOIN store_item_issue_rates siir ON (siir.medicine_id = msd.medicine_id)";


	/*public static ArrayList<MedicineStock> getMedicineStock(String medicineId) throws SQLException {
		Connection con = null; PreparedStatement ps = null; ResultSet rs = null;

		try {

			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_MEDICINE_STOCK_QUERY.replace("#", "''") + " WHERE sibd.medicine_id=? AND asset_approved='Y'  ORDER BY sibd.exp_dt, qty");
			ps.setInt(1, 0);
			ps.setInt(2, Integer.parseInt(medicineId));
			rs = ps.executeQuery();

			ArrayList<MedicineStock> l = new ArrayList();
			while (rs.next()) {
				MedicineStock stock = new MedicineStock();
				populateStockDTO(stock, rs);
				l.add(stock);
			}
			return l;
		} finally {
			DataBaseUtil.closeConnections(con, ps, rs);
		}
	}
*/


	/*
	 * Returns the available stock for a given medicine ID in all departments
	 */
	private static final String GET_STOCK_FOR_MEDICINES =
		"SELECT msd.*, m.medicine_name,m.cust_item_code, mf.manf_code, mf.manf_name, mf.manf_mnemonic, #," +
		"  m.issue_base_unit, m.package_type,m.bin, " +
		"  COALESCE(m.issue_units,'') AS issue_units, COALESCE(m.package_uom,'') AS master_package_uom, " +
		"  COALESCE(mc.discount,0) AS meddisc, " +
		"  mc.category, mc.identification, sic.control_type_name, g.generic_name, " +
		"  '' as indent_no, mc.billable, mc.retailable, mc.claimable, m.item_barcode_id, " +
		"  m.insurance_category_id, m.prior_auth_required,sibd.exp_dt,sibd.mrp," +
		"  COALESCE(sir.tax_rate,COALESCE(ssir.tax_rate,m.tax_rate)) as tax_rate," +
		"  COALESCE(sir.tax_type,COALESCE(ssir.tax_type,m.tax_type)) as tax_type " +

		" FROM " +
		"	(SELECT medicine_id,sum(qty) as qty,item_batch_id,batch_no,sum(qty_in_use) as qty_in_use,sum(qty_maint) as qty_maint, " +
		"	sum(qty_retired) as qty_retired,sum(qty_lost) as qty_lost,sum(qty_kit) as qty_kit, " +
		"	sum(qty_unknown) as qty_unknown,consignment_stock,asset_approved,dept_id " +
		"	FROM store_stock_details  GROUP BY batch_no,item_batch_id,medicine_id,consignment_stock,asset_approved,dept_id " +
		"	ORDER BY medicine_id) as msd " +
		"	JOIN store_item_batch_details sibd USING(item_batch_id)   " +
		"   JOIN store_item_details m ON(m.medicine_id = msd.medicine_id)   " +
		"   JOIN manf_master mf ON (mf.manf_code = m.manf_name)   " +
		"   JOIN store_category_master mc ON mc.category_id = m.med_category_id   " +
		"   LEFT JOIN store_item_controltype sic ON sic.control_type_id=m.control_type_id   " +
		"   LEFT JOIN generic_name g ON m.generic_name = g.generic_code  " +
		"   LEFT JOIN insurance_plan_details ipd on (m.insurance_category_id = ipd.insurance_category_id     " +
		"		AND ipd.patient_type='o' AND ipd.plan_id=0)   " +
		"   LEFT JOIN insurance_plan_main ipm on (ipm.plan_id = ipd.plan_id)   " +
		"   JOIN item_insurance_categories iic ON (iic.insurance_category_id = m.insurance_category_id)  " +
		"   LEFT JOIN store_item_rates sir ON (msd.medicine_id = sir.medicine_id AND     sir.store_rate_plan_id = 0)    " +
		"  LEFT JOIN store_item_rates ssir ON (msd.medicine_id = ssir.medicine_id AND " +
		"    ssir.store_rate_plan_id = ?) " +
		"   LEFT JOIN store_item_issue_rates siir ON (siir.medicine_id = msd.medicine_id)";


	public static ArrayList<MedicineStock> getStockForMedicines(List<Integer> medicineIds) throws SQLException {
		Connection con = null; PreparedStatement ps = null; ResultSet rs = null;

		try {
			String[] medicineIdsPlaceholdersArr = new String[medicineIds.size()];
			Arrays.fill(medicineIdsPlaceholdersArr, "?");
			String medicineIdsPlaceholders = StringUtils.arrayToCommaDelimitedString(medicineIdsPlaceholdersArr);
			
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_STOCK_FOR_MEDICINES.replace("#", "''") +
					" WHERE asset_approved='Y'  AND msd.medicine_id IN ("+medicineIdsPlaceholders+") ORDER BY sibd.exp_dt, qty");
			ps.setInt(1, 0);
			ListIterator<Integer> medicineIdIterator = medicineIds.listIterator();
			while (medicineIdIterator.hasNext()) {
				Object medicineId = medicineIdIterator.next();
				int idx = medicineIdIterator.nextIndex();
				ps.setObject(idx+1, medicineId);
			}			
			rs = ps.executeQuery();

			ArrayList<MedicineStock> l = new ArrayList();
			while (rs.next()) {
				MedicineStock stock = new MedicineStock();
				populateStockDTO(stock, rs);
				l.add(stock);
			}
			return l;
		} finally {
			DataBaseUtil.closeConnections(con, ps, rs);
		}
	}



	private static final String SUPP_ITEM_GRN_DET =
		" LEFT JOIN ("
		+" SELECT *,("
		+"	SELECT package_cp "
		+"		FROM store_item_lot_details "
		+"		WHERE item_batch_id = grn.item_batch_id AND purchase_type = 'S' "
		+"		ORDER BY lot_time DESC LIMIT 1) AS cost_price,"
		+"	(SELECT adj_mrp FROM store_grn_details isgd JOIN store_grn_main USING(grn_no) "
		+"                  WHERE isgd.item_batch_id = grn.item_batch_id AND isgd.billed_qty > 0 "
		+"                  ORDER BY grn_date DESC LIMIT 1) as adj_mrp, "
		+"	(SELECT isgd.discount/(isgd.billed_qty/grn_pkg_size) FROM store_grn_details isgd JOIN store_grn_main USING(grn_no) "
		+"                  WHERE isgd.item_batch_id = grn.item_batch_id AND isgd.billed_qty > 0 "
		+"                  ORDER BY grn_date DESC LIMIT 1) as discount, "
		+"	(SELECT item_ced FROM store_grn_details isgd JOIN store_grn_main USING(grn_no) "
		+"                  WHERE isgd.item_batch_id = grn.item_batch_id AND isgd.billed_qty > 0 "
		+"                  ORDER BY grn_date DESC LIMIT 1) as item_ced, "
		+"	(SELECT tax_rate FROM store_grn_details isgd JOIN store_grn_main USING(grn_no) "
		+"                  WHERE isgd.item_batch_id = grn.item_batch_id AND isgd.billed_qty > 0 "
		+"                  ORDER BY grn_date DESC LIMIT 1) as tax_rate, "
		+"	(SELECT billed_qty  FROM store_grn_details isgd JOIN store_grn_main USING(grn_no) "
		+"                  WHERE isgd.item_batch_id = grn.item_batch_id AND isgd.billed_qty > 0 "
		+"                  ORDER BY grn_date DESC LIMIT 1) as grn_qty "
		+"	FROM ("
		+" SELECT DISTINCT item_batch_id,sg.cost_price as grn_cost_price "
		+" FROM store_grn_details sg "
		+"  JOIN store_grn_main sgm USING (grn_no) "
		+"  LEFT JOIN store_invoice si ON (si.supplier_invoice_id = sgm.supplier_invoice_id "
		+"   AND supplier_id = ?) "
		+" WHERE sg.medicine_id=? AND sgm.debit_note_no IS NULL AND (billed_qty != 0 OR bonus_qty !=0)"
		+" GROUP BY item_batch_id,sg.cost_price,grn_pkg_size, sg.discount,sg.billed_qty,item_ced "
		+"ORDER BY item_batch_id,sg.cost_price DESC ) as grn"
		+") as price ON (price.item_batch_id = msd.item_batch_id)";

	public static ArrayList<MedicineStock> getMedicineStockInDeptWithSuppRates(String medicineId,
			String deptId, String supplierId, String healthAuthority)
		throws SQLException {

		Connection con = null; PreparedStatement ps = null; ResultSet rs = null;
		try {

			BasicDynaBean storeBean = StoreDAO.findByStore(Integer.parseInt(deptId));
			int storeRatePlanId = ( storeBean.get("store_rate_plan_id") == null ? 0 :(Integer)storeBean.get("store_rate_plan_id") );

			con = DataBaseUtil.getReadOnlyConnection();
			StringBuilder buildQuery = new StringBuilder(GET_MEDICINE_STOCK_QUERY.replace("#", "price.*")+SUPP_ITEM_GRN_DET);

			buildQuery.append(" WHERE msd.medicine_id=? AND dept_id=?  " );
			buildQuery.append(" and qty > 0 ");

			buildQuery.append(" ORDER BY sign(msd.qty) DESC, sibd.exp_dt, qty ");
			ps = con.prepareStatement(buildQuery.toString());
			ps = con.prepareStatement(buildQuery.toString());

			ps.setString(1, healthAuthority);
			ps.setInt(2, storeRatePlanId);
			ps.setString(3, supplierId);
			ps.setInt(4, Integer.parseInt(medicineId));
			ps.setInt(5, Integer.parseInt(medicineId));
			ps.setInt(6, Integer.parseInt(deptId));
			rs = ps.executeQuery();

			ArrayList<MedicineStock> l = new ArrayList();
			while (rs.next()) {
				MedicineStock stock = new MedicineStock();
				populateStockDTO(stock, rs);
				l.add(stock);
			}
			return l;
		}finally{
			DataBaseUtil.closeConnections(con, ps, rs);
		}
	}

	/*public static ArrayList<MedicineStock> getMedicineStockInDept(String medicineId, String deptId,
			String saleType)
		throws SQLException {

		if (medicineId == null)		// due to medicine ID int change, we need this check.
			return new ArrayList<MedicineStock>();

		Connection con = null; PreparedStatement ps = null; ResultSet rs = null;
		try {

			BasicDynaBean storeBean = StoreDAO.findByStore(Integer.parseInt(deptId));
			int storeRatePlanId = ( storeBean.get("store_rate_plan_id") == null ? 0 :(Integer)storeBean.get("store_rate_plan_id") );

			con = DataBaseUtil.getReadOnlyConnection();
			StringBuilder buildQuery = new StringBuilder(GET_MEDICINE_STOCK_QUERY.replace("#", "''"));

			if (saleType.equalsIgnoreCase("D")) {
				buildQuery.append(" WHERE msd.medicine_id=? AND dept_id=? AND msd.qty> 0 AND ((mc.identification = 'S' and msd.qty=1) or (mc.identification = 'B')) ");
				buildQuery.append(" AND ASSET_APPROVED = 'Y' and consignment_stock = false");
				// auto batch pick logic depends on this order.
				buildQuery.append(" ORDER BY sibd.exp_dt, qty ");
				ps = con.prepareStatement(buildQuery.toString());

			} else if (saleType.equalsIgnoreCase("return")) {

				buildQuery.append(" WHERE msd.medicine_id=? AND dept_id=?  AND ((mc.identification = 'S' and msd.qty=0) or (mc.identification = 'B')) ");
				buildQuery.append(" AND ASSET_APPROVED = 'Y' and consignment_stock = false");
				buildQuery.append(" ORDER BY sibd.exp_dt, qty ");
				ps = con.prepareStatement(buildQuery.toString());

			} else if (saleType.equalsIgnoreCase("estimate")){
					buildQuery.append(" WHERE msd.medicine_id=? AND dept_id=?  AND ((mc.identification = 'S' and msd.qty=1) or (mc.identification = 'B')) ");
					buildQuery.append(" and consignment_stock = false");
					buildQuery.append(" ORDER BY sibd.exp_dt, qty ");
					ps = con.prepareStatement(buildQuery.toString());
			} else {
				buildQuery.append(" WHERE msd.medicine_id=? AND dept_id=?  " );

				if (saleType.equals("supReturn")) {
					buildQuery.append(" and qty > 0 ");
				} else {
					buildQuery.append("AND ((mc.identification = 'S' and msd.qty=1) or (mc.identification = 'B')) ");
					buildQuery.append(" AND ASSET_APPROVED = 'Y' and consignment_stock = false");
				}

				// auto batch pick logic depends on this order. Positive qty always comes before negative
				buildQuery.append(" ORDER BY sign(msd.qty) DESC, sibd.exp_dt, qty ");
				ps = con.prepareStatement(buildQuery.toString());
			}

			ps.setInt(1, storeRatePlanId);
			ps.setInt(2, Integer.parseInt(medicineId));
			ps.setInt(3, Integer.parseInt(deptId));
			rs = ps.executeQuery();

			ArrayList<MedicineStock> l = new ArrayList();
			while (rs.next()) {
				MedicineStock stock = new MedicineStock();
				populateStockDTO(stock, rs);
				l.add(stock);
			}
			return l;
		} finally {
			DataBaseUtil.closeConnections(con, ps, rs);
		}
	}
*/
	/*
	 * Query used by sales/issues/returns/issue returns: this includes the patient insurance
	 * amounts required for calculations. Other screens such as stock entry/supplier returns/transfers
	 * don't require this information.
	 */
	private static final String MED_STOCK_WITH_PLAN_QUERY_BASE_FIELDS =

		"SELECT  '' as route_id, '' as route_name, msd.qty,COALESCE(sir.tax_rate,COALESCE(ssir.tax_rate,m.tax_rate)) as tax_rate, sico.item_code," +
		"  COALESCE(sir.tax_type,COALESCE(ssir.tax_type,m.tax_type)) as tax_type," +
		"  msd.medicine_id,msd.qty_in_use,msd.qty_maint,msd.qty_retired,msd.qty_lost,msd.qty_kit,msd.qty_unknown," +
		"  msd.consignment_stock,msd.asset_approved,msd.dept_id," +
		"  m.medicine_name, m.item_selling_price, mf.manf_code, mf.manf_name, mf.manf_mnemonic, " +
		"  m.issue_base_unit, m.package_type,COALESCE(isld.bin,m.bin) as bin, " +
		"  COALESCE(m.issue_units,'') AS issue_units, COALESCE(m.package_uom,'') AS master_package_uom, " +
		"	COALESCE(mc.discount,0) AS meddisc, " +
		"  mc.category, mc.category_id, mc.identification, sic.control_type_name, g.generic_name, " +
		"  '' as indent_no, mc.billable, mc.retailable, mc.claimable, m.item_barcode_id, " +
		"  m.insurance_category_id, m.billing_group_id, m.prior_auth_required, cum.consumption_uom, " +
		"  m.insurance_category_id, m.prior_auth_required, m.cons_uom_id, m.med_category_id, " +
		" iic.insurance_payable, ipm.is_copay_pc_on_post_discnt_amt, ipd.patient_amount as patient_amount, " +
		"  ipd.patient_amount_per_category AS patient_amount_per_category," +
		"  ipd.category_payable AS category_payable," +
		"  ipd.patient_percent as patient_percent, ipd.patient_amount_cap as patient_amount_cap " +
//		"  COALESCE(sir.selling_price,COALESCE(ssir.selling_price, COALESCE(m.item_selling_price, sibd.mrp))) as selling_price," +
//		" COALESCE(sir.selling_price,COALESCE(ssir.selling_price, COALESCE(m.item_selling_price, sibd.mrp))) as orig_selling_price" +
		"  ,issue_rate_expr,sibd.mrp,sibd.batch_no,sibd.exp_dt,sibd.item_batch_id,sibd.mrp,m.cust_item_code, sir.selling_price_expr as visit_selling_expr, ssir.selling_price_expr as store_selling_expr," +
		"  CASE WHEN cust_item_code IS NOT NULL AND  TRIM(cust_item_code) != ''  THEN medicine_name||' - '||cust_item_code ELSE medicine_name END as cust_item_code_with_name ";

		private static final String MED_STOCK_WITH_PLAN_QUERY_STOCK_TABLES =
		" FROM ( " +
		" SELECT medicine_id,sum(qty) as qty,item_batch_id,batch_no,sum(qty_in_use) as qty_in_use,sum(qty_maint) as qty_maint, " +
		" sum(qty_retired) as qty_retired,sum(qty_lost) as qty_lost,sum(qty_kit) as qty_kit, " +
	    " sum(qty_unknown) as qty_unknown,consignment_stock,asset_approved,dept_id,min(tax) as tax " +
	    " FROM store_stock_details  GROUP BY batch_no,item_batch_id,medicine_id,consignment_stock,asset_approved,dept_id " +
		"   ORDER BY medicine_id) as msd " ;

	private static final String MED_STOCK_WITH_PLAN_QUERY_BASE_JOIN_TABLES =
		"  JOIN store_item_batch_details sibd USING(item_batch_id) " +
		"  JOIN store_item_details m ON(m.medicine_id = msd.medicine_id) " +
		"  JOIN manf_master mf ON (mf.manf_code = m.manf_name) " +
		"  JOIN store_category_master mc ON mc.category_id = m.med_category_id " +
		"  LEFT JOIN item_store_level_details isld ON isld.medicine_id = m.medicine_id @" +
		"  LEFT JOIN ha_item_code_type hict ON (hict.medicine_id = m.medicine_id AND hict.health_authority=?) " +
		"  LEFT JOIN store_item_codes sico ON (sico.medicine_id = m.medicine_id AND sico.code_type = hict.code_type) " +
		"  LEFT JOIN store_item_controltype sic ON sic.control_type_id=m.control_type_id "+
		"  LEFT JOIN generic_name g ON m.generic_name = g.generic_code "+
		"  LEFT JOIN insurance_plan_details ipd on (m.insurance_category_id = ipd.insurance_category_id " +
		"    AND ipd.patient_type=? AND ipd.plan_id=?) " +
		"  LEFT JOIN insurance_plan_main ipm on (ipm.plan_id = ipd.plan_id) " +
		"  JOIN item_insurance_categories iic ON (iic.insurance_category_id = m.insurance_category_id) " +
		"  LEFT JOIN store_item_rates sir ON (msd.medicine_id = sir.medicine_id AND " +
		"    sir.store_rate_plan_id = ?) " +
		"  LEFT JOIN store_item_rates ssir ON (msd.medicine_id = ssir.medicine_id AND " +
		"    ssir.store_rate_plan_id = ?) " +
		"  LEFT JOIN store_item_issue_rates siir ON (siir.medicine_id = msd.medicine_id)         " +
		" LEFT JOIN consumption_uom_master cum ON (cum.cons_uom_id = m.cons_uom_id)" +
		" WHERE consignment_stock=false AND asset_approved = 'Y' # ";
	
	private static final String MED_STOCK_WITH_PLAN_QUERY_BASE_JOIN_TABLES_WITH_CONSIGNMENT =
			"  JOIN store_item_batch_details sibd USING(item_batch_id) " +
			"  JOIN store_item_details m ON(m.medicine_id = msd.medicine_id) " +
			"  JOIN manf_master mf ON (mf.manf_code = m.manf_name) " +
			"  JOIN store_category_master mc ON mc.category_id = m.med_category_id " +
			"  LEFT JOIN item_store_level_details isld ON isld.medicine_id = m.medicine_id @" +
			"  LEFT JOIN ha_item_code_type hict ON (hict.medicine_id = m.medicine_id AND hict.health_authority=?) " +
			"  LEFT JOIN store_item_codes sico ON (sico.medicine_id = m.medicine_id AND sico.code_type = hict.code_type) " +
			"  LEFT JOIN store_item_controltype sic ON sic.control_type_id=m.control_type_id "+
			"  LEFT JOIN generic_name g ON m.generic_name = g.generic_code "+
			"  LEFT JOIN insurance_plan_details ipd on (m.insurance_category_id = ipd.insurance_category_id " +
			"    AND ipd.patient_type=? AND ipd.plan_id=?) " +
			"  LEFT JOIN insurance_plan_main ipm on (ipm.plan_id = ipd.plan_id) " +
			"  JOIN item_insurance_categories iic ON (iic.insurance_category_id = m.insurance_category_id) " +
			"  LEFT JOIN store_item_rates sir ON (msd.medicine_id = sir.medicine_id AND " +
			"    sir.store_rate_plan_id = ?) " +
			"  LEFT JOIN store_item_rates ssir ON (msd.medicine_id = ssir.medicine_id AND " +
			"    ssir.store_rate_plan_id = ?) " +
			"  LEFT JOIN store_item_issue_rates siir ON (siir.medicine_id = msd.medicine_id)         " +
			" WHERE asset_approved = 'Y' # ";

	private static final String MED_STOCK_SELLING_PRICE_QUERY_BATCH_MRP_NO =
		",COALESCE( m.item_selling_price, sibd.mrp) as selling_price," +
		" COALESCE( m.item_selling_price, sibd.mrp) as orig_selling_price ";

	private static final String MED_STOCK_SELLING_PRICE_QUERY_BATCH_MRP_YES =
		",COALESCE( m.item_selling_price, sibd.mrp) as selling_price," +
		"COALESCE( m.item_selling_price, sibd.mrp) as orig_selling_price " ;

	// sort order is based on which items we should be selling first.
	private static final String ORDER_BY_WITH_ZEROS =
		" ORDER BY sign(msd.qty) DESC,exp_dt";

	private static final String WHERE_CLAUSE_WITHOUT_ZEROS = "  AND msd.qty > 0 AND asset_approved='Y' " ;
	private static final String ORDER_BY_WITHOUT_ZEROS =
		" ORDER BY exp_dt";

	public static List getMedicineStockWithPatAmtsInDept(List<Integer> medicineIds, int deptId, int planId,
			String visitType, boolean includeZeroStock, int visitStoreRatePlanId, String healthAuthority) throws SQLException {

		if (medicineIds == null || medicineIds.size() == 0)
			throw new IllegalArgumentException("Stock query requires at least one item");
		BasicDynaBean storeBean = StoreDAO.findByStore(deptId);
		int storeRatePlanId = ( storeBean.get("store_rate_plan_id") == null ? 0 :(Integer)storeBean.get("store_rate_plan_id") );
		boolean useBatchMRP = ((String)storeBean.get("use_batch_mrp")).equals("Y");

		StringBuilder query = new StringBuilder(MED_STOCK_WITH_PLAN_QUERY_BASE_FIELDS);

		query.append(useBatchMRP ? MED_STOCK_SELLING_PRICE_QUERY_BATCH_MRP_YES : MED_STOCK_SELLING_PRICE_QUERY_BATCH_MRP_NO);
		query.append(MED_STOCK_WITH_PLAN_QUERY_STOCK_TABLES);
		query.append((MED_STOCK_WITH_PLAN_QUERY_BASE_JOIN_TABLES.replace("#", " AND msd.dept_id=? "))
				.replace("@", " AND isld.dept_id="+deptId+" "));
		
		QueryBuilder.addWhereFieldOpValue(true, query, "msd.medicine_id", "IN", medicineIds);
		if (includeZeroStock)
			query.append(ORDER_BY_WITH_ZEROS);
		else
			query.append(WHERE_CLAUSE_WITHOUT_ZEROS+ORDER_BY_WITHOUT_ZEROS);

		log.debug("Stock query: " + query.toString() + " size: " + medicineIds.size() );
		Object params[] = new Object[6 + medicineIds.size()];
		int i=0;
		params[i++] = healthAuthority;
		params[i++] = visitType;
		params[i++] = planId;
		params[i++] = visitStoreRatePlanId;
		params[i++] = storeRatePlanId;       
		params[i++] = deptId;
		
		for (int medId : medicineIds) {
			params[i++] = medId;
		}
		return DataBaseUtil.queryToDynaList(query.toString(), params);
	}
	
	public static List getOrderKitMedicineStockWithPatAmtsInDept(List<Integer> medicineIds, int deptId, int planId,
			String visitType, boolean includeZeroStock, int visitStoreRatePlanId, String healthAuthority) throws SQLException {

		if (medicineIds == null || medicineIds.size() == 0)
			throw new IllegalArgumentException("Stock query requires at least one item");
		BasicDynaBean storeBean = StoreDAO.findByStore(deptId);
		int storeRatePlanId = ( storeBean.get("store_rate_plan_id") == null ? 0 :(Integer)storeBean.get("store_rate_plan_id") );
		boolean useBatchMRP = ((String)storeBean.get("use_batch_mrp")).equals("Y");

		StringBuilder query = new StringBuilder(MED_STOCK_WITH_PLAN_QUERY_BASE_FIELDS);

		query.append(useBatchMRP ? MED_STOCK_SELLING_PRICE_QUERY_BATCH_MRP_YES : MED_STOCK_SELLING_PRICE_QUERY_BATCH_MRP_NO);
		query.append(MED_STOCK_WITH_PLAN_QUERY_STOCK_TABLES);
		query.append((MED_STOCK_WITH_PLAN_QUERY_BASE_JOIN_TABLES_WITH_CONSIGNMENT.replace("#", " AND msd.dept_id=? "))
		.replace("@", " AND isld.dept_id="+deptId+" "));

		QueryBuilder.addWhereFieldOpValue(true, query, "msd.medicine_id", "IN", medicineIds);
		if (includeZeroStock)
			query.append(ORDER_BY_WITH_ZEROS);
		else
			query.append(WHERE_CLAUSE_WITHOUT_ZEROS+ORDER_BY_WITHOUT_ZEROS);

		log.debug("Stock query: " + query.toString() + " size: " + medicineIds.size() );
		Object params[] = new Object[6 + medicineIds.size()];
		int i=0;
		params[i++] = healthAuthority;
		params[i++] = visitType;
		params[i++] = planId;
		params[i++] = visitStoreRatePlanId;
		params[i++] = storeRatePlanId;       
		params[i++] = deptId;

		for (int medId : medicineIds) {
			params[i++] = medId;
		}
		return DataBaseUtil.queryToDynaList(query.toString(), params);
	}

	public static List<BasicDynaBean> getAllStoreMedicineStockWithPatAmtsInDept(List<Integer> medicineIds, int planId,
			String visitType, boolean includeZeroStock, int visitStoreRatePlanId,
			String healthAuthority,boolean useBatchMRP) throws SQLException {

		if (medicineIds == null || medicineIds.size() == 0)
			throw new IllegalArgumentException("Stock query requires at least one item");

		StringBuilder query = new StringBuilder(MED_STOCK_WITH_PLAN_QUERY_BASE_FIELDS);

		query.append(useBatchMRP ? MED_STOCK_SELLING_PRICE_QUERY_BATCH_MRP_YES : MED_STOCK_SELLING_PRICE_QUERY_BATCH_MRP_NO);
		query.append(MED_STOCK_WITH_PLAN_QUERY_STOCK_TABLES);
		query.append((MED_STOCK_WITH_PLAN_QUERY_BASE_JOIN_TABLES.replace("#", ""))
				.replace("@", ""));

		QueryBuilder.addWhereFieldOpValue(true, query, "msd.medicine_id", "IN", medicineIds);
		if (includeZeroStock)
			query.append(ORDER_BY_WITH_ZEROS);
		else
			query.append(ORDER_BY_WITHOUT_ZEROS);

		log.debug("Stock query: " + query.toString() + " size: " + medicineIds.size() );
		Object params[] = new Object[5 + medicineIds.size()];
		int i=0;
		params[i++] = healthAuthority; params[i++] = visitType; params[i++] = planId; params[i++] = visitStoreRatePlanId;params[i++] = 0;
		for (int medId : medicineIds) {
			params[i++] = medId;
		}
		return DataBaseUtil.queryToDynaList(query.toString(), params);
	}

	/*
	 * Add extra quantity to existing stock entries: the quantities given in the list are
	 * to be treated as quantity to be removed from existing stock.
	 */
	private static final String REDUCE_STOCK =
		"UPDATE store_stock_details  SET qty=(qty-?), username=?, change_source=? " +
		" WHERE medicine_id=? AND batch_no=? AND dept_id=?";

	private static final String ADD_TO_STOCK =
		"UPDATE store_stock_details   SET qty=(qty+?), username=?, change_source=? " +
		" WHERE medicine_id=? AND batch_no=? AND dept_id=?";

	public  boolean updateStockEntries(Connection con,BasicDynaBean bean,String stockChangeType)
		throws SQLException {
		PreparedStatement ps = null;

		try {
				if(stockChangeType.equals("increase"))
					ps = con.prepareStatement(ADD_TO_STOCK);
				else
					ps = con.prepareStatement(REDUCE_STOCK);

				ps.setBigDecimal(1,(BigDecimal) bean.get("qty"));
				ps.setString(2, bean.get("username").toString());
				ps.setString(3, bean.get("change_source").toString());
				ps.setString(4, bean.get("medicine_id").toString());
				ps.setString(5, bean.get("batch_no").toString());
				ps.setString(6, bean.get("dept_id").toString());
			int p = ps.executeUpdate();

			return p > 0;
		} finally {
		  if (ps != null) {
		    ps.close();
		  }
		}
	}

	public static final String INSERT_STOCKISSUE_MAIN="INSERT INTO stock_issue_main (issue_no,date_time,dept_from,issued_to, username,reason) VALUES (?,?,?,?,?,?)";

	public boolean insertStockIssueMain(BasicDynaBean bean ,int issueNo,String toStore,String fromStore,String reason) throws SQLException {

		PreparedStatement ps=null;
		int i=0;
		boolean target=false;
		try{
			ps = con.prepareStatement(INSERT_STOCKISSUE_MAIN);
			ps.setInt(1, issueNo);
			ps.setTimestamp(2, (java.sql.Timestamp)bean.get("stock_time"));
			ps.setString(3, fromStore);
			ps.setString(4,toStore);
			ps.setString(5, bean.get("username").toString());
			ps.setString(6, reason);
            i=ps.executeUpdate();
            if(i>0)
            target=true;
		}finally{
		  if (ps != null) {
		    ps.close();
		  }
		}
		return target;
	}

	/*
	 * Update existing stock with new mrp and quantities
	 */
	public boolean updateStockEntries(ArrayList<MedicineStock> medicines) {
		// TODO for edit stock details
		return true;
	}

	/*
	 * Add new stock rows. Will fail if same medicine ID with batch number already exists.
	 * All attributes of the medicine DTO will be used in this case.
	 */
	public boolean insertStockEntries(ArrayList<MedicineStock> medicines) {
		// TODO for direct stock entry
		return true;
	}

	private static void populateStockDTO(MedicineStock stock, ResultSet rs) throws SQLException {
		stock.setMedicineId(rs.getInt("medicine_id"));
		stock.setBatchNo(rs.getString("batch_no"));
		stock.setQty(rs.getBigDecimal("qty"));
		stock.setExpDt(rs.getDate("exp_dt"));
		stock.setMrp(rs.getBigDecimal("mrp"));
		stock.setTaxPercent(rs.getBigDecimal("tax_rate"));
		stock.setDeptId(rs.getInt("dept_id"));
		stock.setMedicineName(rs.getString("medicine_name"));
		stock.setManfCode(rs.getString("manf_code"));
		stock.setManfName(rs.getString("manf_name"));
		stock.setManfMnemonic(rs.getString("manf_mnemonic"));
		stock.setPackageUnit(rs.getBigDecimal("issue_base_unit"));
		stock.setBin(rs.getString("bin"));
		stock.setPackage_type(rs.getString("package_type"));
		stock.setMedDisc(rs.getBigDecimal("meddisc"));
		stock.setTaxType(rs.getString("tax_type"));
		stock.setMedCategory(rs.getString("category"));
		stock.setControlType(rs.getString("control_type_name"));
		stock.setIssueUnits(rs.getString("issue_units"));
		stock.setGenericName(rs.getString("generic_name"));
		stock.setPreAuthRequired(rs.getString("prior_auth_required"));
		stock.setPackageUOM(rs.getString("master_package_uom"));
		if (rs.getString("identification") != null){
			stock.setIdentification(rs.getString("identification"));
		}
		stock.setRetailable(rs.getString("retailable"));
		stock.setBillable(rs.getString("billable"));
		stock.setClaimable(rs.getString("claimable"));
		stock.setItemBarcode(rs.getString("item_barcode_id"));
		stock.setItemBatchId(rs.getInt("item_batch_id"));
		
		if (doesColumnExist(rs, "grn_qty") && rs.getString("grn_qty") != null)
			stock.setGrn_qty(rs.getBigDecimal("grn_qty"));

		if (doesColumnExist(rs, "item_ced") && rs.getString("item_ced") != null){
			stock.setCedTaxAmt(rs.getBigDecimal("item_ced"));
		}

		if( doesColumnExist(rs, "cost_price")) {
			stock.setPackage_cp(rs.getBigDecimal("cost_price") == null ? BigDecimal.ZERO : rs.getBigDecimal("cost_price"));
		}

		if( doesColumnExist(rs, "bonus_qty")) {
			stock.setBonusQty(rs.getBigDecimal("bonus_qty") == null ? BigDecimal.ZERO : rs.getBigDecimal("bonus_qty"));
		}

		if( doesColumnExist(rs, "discount") && rs.getBigDecimal("discount") != null) {
			stock.setMedDisc(rs.getBigDecimal("discount"));
		}

		if( doesColumnExist(rs, "adj_mrp")) {
			stock.setPackageSp(rs.getBigDecimal("adj_mrp"));
		}

		if ((doesColumnExist(rs, "insurance_category_id"))){
			stock.setInsuranceCategoryId(rs.getInt("insurance_category_id"));
		}
		//Next 3 fields are for getting patient copay amounts in case those exist i.e an insurance patient.
		if ((doesColumnExist(rs, "patient_amount")) && (rs.getBigDecimal("patient_amount") != null)){
			stock.setPatAmt(rs.getBigDecimal("patient_amount"));
		}
		if ((doesColumnExist(rs, "patient_percent")) && (rs.getBigDecimal("patient_percent") != null)){
			stock.setPatPer(rs.getBigDecimal("patient_percent"));
		}
		if ((doesColumnExist(rs, "patient_amount_cap")) && (rs.getBigDecimal("patient_amount_cap") != null)){
			stock.setPatCap(rs.getBigDecimal("patient_amount_cap"));
		}
		if ((doesColumnExist(rs, "patient_amount_per_category")) && (rs.getBigDecimal("patient_amount_per_category") != null)){
			stock.setPatCatAmt(rs.getBigDecimal("patient_amount_per_category"));
		}
		if ((doesColumnExist(rs, "grn_cost_price")) && (rs.getBigDecimal("grn_cost_price") != null)){
			stock.setGrn_cp(rs.getBigDecimal("grn_cost_price"));
		}	
		if ((doesColumnExist(rs, "item_code")) && (rs.getString("item_code") != null)){
			stock.setItemcode(rs.getString("item_code"));
		}
	}

	/*
	 * Getting a new sequenceId for stock adjustment.
	 */
	public int getNextId() throws SQLException {
		PreparedStatement ps=con.prepareStatement("select nextval('stockadjust_sequence')");
		return DataBaseUtil.getIntValueFromDb(ps);
	}

	public int getIssueNextId() throws SQLException {

		PreparedStatement ps=con.prepareStatement("select nextval('stockissue_sequence')");
		return DataBaseUtil.getIntValueFromDb(ps);
	}

	public static final String INSERT_STOCKISSUE="INSERT INTO stock_issue_details (issue_no,medicine_id,batch_no,qty) VALUES (?,?,?,?)";

	public boolean insertStockIssue(List<BasicDynaBean> stockIssue ,int issueNo) throws SQLException {

		PreparedStatement ps = con.prepareStatement(INSERT_STOCKISSUE);
		boolean target=false;
		try {
			for(BasicDynaBean bean : stockIssue){
				ps.setInt(1, issueNo);
				ps.setString(2, bean.get("medicine_id").toString());
				ps.setString(3, bean.get("batch_no").toString());
				ps.setBigDecimal(4,(BigDecimal) bean.get("qty"));
				int p = ps.executeUpdate();
				if(p>0)
		            target=true;
				else
					break;
			}
	} finally {
		ps.close();
	}
     return target;
}


	public int getSupplierReturnsNextId() throws SQLException {
		PreparedStatement ps=con.prepareStatement("select nextval('supplier_return_sequence')");
		return DataBaseUtil.getIntValueFromDb(ps);
	}

	public static final String INSERT_SUPPLIER_RETURNS_MAIN="INSERT INTO store_supplier_returns_main(return_no,date_time,user_name,supplier_id," +
			" return_type,remarks,invoice_no) VALUES(?,?,?,?,?,?,?)";

	public boolean insertSupplierReturnsMain(SupplierReturnsDTO supplierRMainDTO, ArrayList<SupplierReturnsDTO> supplierRList) throws SQLException {

		PreparedStatement ps=null;
		int i=0;
		boolean target=false;
		try{
			ps = con.prepareStatement(INSERT_SUPPLIER_RETURNS_MAIN);
			ps.setInt(1, supplierRMainDTO.getReturn_no());
			ps.setTimestamp(2, supplierRMainDTO.getDate_time());
			ps.setString(3, supplierRMainDTO.getUsername());
			ps.setString(4, supplierRMainDTO.getSupplier_id());
			ps.setString(5, supplierRMainDTO.getReturn_type());
			ps.setString(6, supplierRMainDTO.getRemarks());
			ps.setString(7, supplierRMainDTO.getInvoiceNo());

			i=ps.executeUpdate();
			if(i>0)
				target=true;

		}finally{
			if(ps!=null)ps.close();
		}
		return target;
	}

   public static final String INSERT_SUPPLIER_RETURNS="INSERT INTO store_supplier_returns(return_no,medicine_id,batch_no,qty) VALUES(?,?,?,?)";


   public boolean insertSupplierReturns(SupplierReturnsDTO supplierRMainDTO, ArrayList<SupplierReturnsDTO> supplierRList) throws SQLException {

	   PreparedStatement ps = con.prepareStatement(INSERT_SUPPLIER_RETURNS);
	   boolean target=true;
	   try {
		   Iterator<SupplierReturnsDTO> it = supplierRList.iterator();
		   while (it.hasNext()) {
			   SupplierReturnsDTO sr = it.next();
			   ps.setInt(1, supplierRMainDTO.getReturn_no());
			   ps.setString(2, sr.getMedicine_id());
			   ps.setString(3, sr.getBatch_no());
			   ps.setBigDecimal(4, sr.getQty());
			   ps.addBatch();
		   }

		   int[] p = ps.executeBatch();
		   for(int i=0;i<p.length;i++){
			   if(p[i] <=0){
				   target = false;
				   break;
			   }
		   }
	   } finally {
		   ps.close();
	   }
	   return target;
   }

   public static final String STOCK_REORDER_DETAILS="SELECT dept_id,medicine_name,qty,reorder_level,danger_level FROM " +
   		"(SELECT pmsd.dept_id,pmd.medicine_name,sum(pmsd.qty) AS qty,pds.reorder_level,pds.danger_level,pmd.cust_item_code " +
   		 "FROM store_item_details pmd,store_stock_details pmsd,store_reorder_levels pds " +
   		"WHERE pds.dept_id=pmsd.dept_id AND pmd.medicine_id=pds.medicine_id AND pmsd.medicine_id=pds.medicine_id " +
   		"GROUP BY pmsd.dept_id,pmd.medicine_name,pds.reorder_level,pds.danger_level ) AS reorder_items " +
   		"WHERE qty <= reorder_level AND dept_id=? ";

   public static ArrayList getStockReorderScreen(int offsetNum, String storeId) throws SQLException {
	   Connection con = null; PreparedStatement ps = null;
	   try {
		   ArrayList stockReorderList=null;
		   con = DataBaseUtil.getReadOnlyConnection();

		   ps = con.prepareStatement(STOCK_REORDER_DETAILS+"ORDER BY medicine_name ASC LIMIT 15 OFFSET ?");
		   ps.setString(1, storeId);
		   ps.setInt(2, offsetNum);

		   stockReorderList=DataBaseUtil.queryToArrayList(ps);
		   return stockReorderList;
	   } finally {
		   DataBaseUtil.closeConnections(con, ps);

	   }
   }


   public static int getStockReorderArraySize(String storeId) throws SQLException {

	   Connection con = null; PreparedStatement ps = null;
	   try{
		   con = DataBaseUtil.getReadOnlyConnection();
		   ps = con.prepareStatement(STOCK_REORDER_DETAILS+" ORDER BY medicine_name");
		   ps.setString(1, storeId);

		   ArrayList stockReorderDetails = DataBaseUtil.queryToArrayList(ps);
		   int stockReorderSize = stockReorderDetails.size() / 15;
		   if(stockReorderSize==0)
			   stockReorderSize=1;
		   int count = stockReorderDetails.size() % 15;

		   if ((count > 1)&&(stockReorderSize>1)){
			   ++stockReorderSize;
		   }
		   ps.close();
		   return stockReorderSize;
	   } finally {
		   DataBaseUtil.closeConnections(con, ps);

	   }

   }

   public static final String MEDICINE_TIMESTAMP="SELECT * FROM store_main_stock_timestamp";
   /* This method  for getting  count  for number of medicnes for cache related thing*/
   public static int getMedicineTimestamp() throws SQLException {

	   Connection con = null; PreparedStatement ps = null;ResultSet rs=null;int count=0;
	   try {
		   con = DataBaseUtil.getReadOnlyConnection();
		   ps = con.prepareStatement(MEDICINE_TIMESTAMP);
		   rs=ps.executeQuery();
		   if(rs.next()){
			   count=rs.getInt(1);
		   }
	   } finally {
		   DataBaseUtil.closeConnections(con, ps, rs);
	   }
	   return count;
   }

	private static final String STORE_STOCK_TIMESTAMP = "SELECT stock_timestamp FROM stores WHERE dept_id=?";
	public static int getStoreStockTimestamp(int storeId) throws SQLException {
		BasicDynaBean b = DataBaseUtil.queryToDynaBean(STORE_STOCK_TIMESTAMP, storeId);
		return (Integer) b.get("stock_timestamp");
	}

   private static final String GET_EQUI_MEDICINES_FROM_STOCK =
	    "SELECT DISTINCT m.medicine_name,m.cust_item_code, " +
   		"m.medicine_name || ' ('||SUM(qty)||')' AS display_name, SUM(qty) AS qty " +
   		"	FROM store_stock_details msd " +
   		"   JOIN store_item_batch_details sibd USING(item_batch_id) " +
   		"	JOIN store_item_details m ON(m.medicine_id=msd.medicine_id) " +
   		"	JOIN generic_name g ON g.generic_code = m.generic_name " +
   		" 	JOIN store_category_master scm ON (scm.category_id = m.med_category_id) ";

	public static List<BasicDynaBean> getEquivalentMedicinesList(String medicineName, String genericName,
			String storeId, Boolean allStores,String saleType) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		List<BasicDynaBean> l = null;
		String saleExpiredMedicine = GenericPreferencesDAO.getGenericPreferences().getSaleOfExpiredItems();
		try {
			HashMap storeWiseMedMap = new HashMap();

			StringBuilder buildQuery = new StringBuilder(GET_EQUI_MEDICINES_FROM_STOCK);
			buildQuery.append(" WHERE ");
			buildQuery.append(" g.status = 'A' AND ");
			if (!allStores) {
				buildQuery.append(" msd.dept_id=?  AND ASSET_APPROVED = 'Y' ");
				buildQuery.append(" AND ");
			}
			buildQuery.append(" ( medicine_name = ? OR g.generic_name = ? )");
			if ("sale".equals(saleType) || "return".equals(saleType))
				buildQuery.append(" AND scm.issue_type IN ('C','R') AND billable=true AND retailable=true ");
			if (!"Y".equalsIgnoreCase(saleExpiredMedicine)) buildQuery.append("AND (sibd.exp_dt is null or sibd.exp_dt >= current_date) ");
			buildQuery.append("GROUP BY medicine_name,cust_item_code ");

			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(buildQuery.toString());
			int index = 1;
			if (!allStores)
				ps.setInt(index++, Integer.parseInt(storeId));
			ps.setString(index++, medicineName);
			ps.setString(index++, genericName);
			l = DataBaseUtil.queryToDynaList(ps);
			return l;
		} finally {
			DataBaseUtil.closeConnections(con, ps);

		}
	}
	public static List getGroupMedDetails(String saleType,String deptId,List medIds,List batchNos) throws SQLException {
		PreparedStatement ps = null;Connection con = null;
		try {

			BasicDynaBean storeBean = StoreDAO.findByStore(Integer.parseInt(deptId));
			int storeRatePlanId = ( storeBean.get("store_rate_plan_id") == null ? 0 :(Integer)storeBean.get("store_rate_plan_id") );

			con = DataBaseUtil.getReadOnlyConnection();
			int deptIdNum = -1;

			deptIdNum = Integer.parseInt(deptId);

			StringBuilder where = new StringBuilder();
			DataBaseUtil.addWhereFieldInList(where, "batch_no", batchNos);
			DataBaseUtil.addWhereFieldInList(where, "medicine_id", medIds);
			//[b12.5, b14.5, b44]
			// [MD21, MD22, MD19]

			StringBuilder query = new StringBuilder(GET_MEDICINE_STOCK_QUERY.replace("#", "''"));
			query.append(where);
			if(saleType.equalsIgnoreCase("D"))
				query.append(" AND dept_id=? AND msd.qty>0 ");
			else
				query.append(" AND dept_id=?  ");

			ps =con.prepareStatement(query+"");

			int i = 2;
			ps.setInt(1, storeRatePlanId);

			if (batchNos != null) {
				Iterator it = batchNos.iterator();
				while (it.hasNext()) {
					ps.setString(i, (String) it.next());
					i++;
				}
			}
			if (medIds != null) {
				Iterator it = medIds.iterator();
				while (it.hasNext()) {
					int medIdNum = -1;
					String medId = (String)it.next();
					if ((medId != null) && (medId.trim().length() > 0)){
						medIdNum = Integer.parseInt(medId);
					}
					if (medIdNum > 0){
						ps.setInt(i, medIdNum);
					}
					i++;
				}
			}

			ps.setInt(i, deptIdNum);

			return DataBaseUtil.queryToDynaList(ps);
	    }finally {
		  DataBaseUtil.closeConnections(con, ps);
	    }
    }

	public static boolean checkQtyAvailable(String storeId,String medicineId,String batchNo,BigDecimal returnqty) throws SQLException{
		boolean qtyAvailable = false;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection con = null;

		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement("select qty from store_stock_details where medicine_id =? and batch_no=?" +
					" and dept_id=? and qty>0 and qty >=?");
			ps.setString(1, medicineId);
			ps.setString(2, batchNo);
			ps.setString(3, storeId);
			ps.setBigDecimal(4, returnqty);

			rs = ps.executeQuery();

			if(rs.next())
				qtyAvailable = true;
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}

		return qtyAvailable;

	}
	/*
	 * Add extra quantity to existing stock entries: the quantities given in the list are
	 * to be treated as quantity to be removed from existing stock.
	 */
	private static final String REDUCE_STOCK_ADJUST =
		"UPDATE store_stock_details  SET qty=(qty-?), username=?, change_source=? " +
		" WHERE item_batch_id = ? AND dept_id=?";

	public boolean reduceStockEntries(ArrayList<StockChangeDTO> medicines)
		throws SQLException {
		PreparedStatement ps = con.prepareStatement(REDUCE_STOCK_ADJUST);
		try {
			Iterator<StockChangeDTO> it = medicines.iterator();
			while (it.hasNext()) {
				StockChangeDTO m = it.next();
				ps.setBigDecimal(1, m.getChangeQuantity());
				ps.setString(2, m.getUsername());
				ps.setString(3, m.getChange_source());
				ps.setInt(4, m.getItemBatchId());
				ps.setInt(5, Integer.parseInt(m.getStoreId()));
				ps.addBatch();
			}
			int[] p = ps.executeBatch();
			return DataBaseUtil.checkBatchUpdates(p);
		} finally {
			ps.close();
		}
	}

	/*
	 * Add extra quantity to existing stock entries: the quantities given in the list of medicines are
	 * to be treated as additional quantity to be added to existing stock.
	 */
	private static final String ADD_TO_STOCK_ADJUST =
		"UPDATE store_stock_details   SET qty=(qty+?), username=?, change_source=? " +
		" WHERE item_batch_id = ? AND dept_id = ?";

	public boolean addToStockEntries(ArrayList<StockChangeDTO> medicines)
		throws SQLException {
		PreparedStatement ps = con.prepareStatement(ADD_TO_STOCK_ADJUST);
		try {
			Iterator<StockChangeDTO> it = medicines.iterator();
			while (it.hasNext()) {
				StockChangeDTO m = it.next();
				ps.setBigDecimal(1, m.getChangeQuantity());
				ps.setString(2, m.getUsername());
				ps.setString(3, m.getChange_source());
				ps.setInt(4, m.getItemBatchId());
				ps.setInt(5, Integer.parseInt(m.getStoreId()));
				ps.addBatch();
			}
			int[] p = ps.executeBatch();
			return DataBaseUtil.checkBatchUpdates(p);
		} finally {
			ps.close();
		}
	}

	private static final String GET_SEL_SUPP_MEDICINE_STOCK_QUERY =
		 "SELECT distinct msd.batch_no as batch_no_spl,msd.*,price.*,m.bin,m.medicine_name,m.cust_item_code, mf.manf_code, mf.manf_name, mf.manf_mnemonic, m.issue_base_unit,"
		+" m.package_type, COALESCE(m.issue_units, '') AS issue_units, COALESCE(mc.discount,0) AS meddisc,"
		+" mc.category ,mc.identification, sic.control_type_name,mc.claimable,m.item_barcode_id, m.prior_auth_required,"
		+" CASE WHEN char_length(g.generic_name) >= 21 THEN substr(g.generic_name,0,20) ELSE g.generic_name END as generic_name,mc.billable, "
		+" mc.retailable,m.package_uom as master_package_uom,sibd.exp_dt,sibd.mrp,"
		+"  COALESCE(sir.tax_rate,COALESCE(ssir.tax_rate,m.tax_rate)) as tax_rate,"
		+"  COALESCE(sir.tax_type,COALESCE(ssir.tax_type,m.tax_type)) as tax_type,sibd.batch_no,sibd.item_batch_id, "
		+"  (SELECT sum(qty) FROM store_stock_details issd "
		+"		JOIN store_item_lot_details isild USING(item_lot_id) "
		+"      WHERE issd.item_batch_id=msd.item_batch_id AND purchase_type = 'B' AND item_supplier_code = msd.item_supplier_code AND issd.dept_id=msd.dept_id %) as bonus_qty,sico.item_code"
		+" FROM (SELECT item_supplier_code,medicine_id,sum(qty) as qty,item_batch_id,batch_no,sum(qty_in_use) as qty_in_use,sum(qty_maint) as qty_maint, "
		+"	sum(qty_retired) as qty_retired,sum(qty_lost) as qty_lost,sum(qty_kit) as qty_kit, "
		+"	sum(qty_unknown) as qty_unknown,consignment_stock,asset_approved,dept_id $"
		+"	FROM store_stock_details  WHERE consignment_stock = false "
		+" GROUP BY item_supplier_code,batch_no,item_batch_id,medicine_id,consignment_stock,asset_approved,dept_id # "
		+"	ORDER BY medicine_id) as msd"
		+" JOIN store_item_details m USING(medicine_id)"
		+" LEFT JOIN ha_item_code_type hict ON (hict.medicine_id = m.medicine_id AND hict.health_authority=?) " 
		+" LEFT JOIN store_item_codes sico ON (sico.medicine_id = m.medicine_id AND sico.code_type = hict.code_type) " 
		+" JOIN store_item_batch_details sibd ON (sibd.item_batch_id = msd.item_batch_id) "
		+" JOIN manf_master mf ON (mf.manf_code = m.manf_name)"
		+" JOIN store_category_master mc ON mc.category_id = m.med_category_id"
		+ SUPP_ITEM_GRN_DET
		+" LEFT OUTER JOIN generic_name g ON m.generic_name = g.generic_code "
		+" LEFT JOIN store_item_controltype sic ON sic.control_type_id = m.control_type_id "
		+"  LEFT JOIN store_item_rates sir ON (msd.medicine_id = sir.medicine_id AND "
		+"    sir.store_rate_plan_id = 0) "
		+"  LEFT JOIN store_item_rates ssir ON (msd.medicine_id = ssir.medicine_id AND "
		+"    ssir.store_rate_plan_id = ?) "
		+" where item_supplier_code=? and dept_id=? and m.medicine_id=? and qty > 0 @";

	public static ArrayList<MedicineStock> getSelSuppMedicineStockInDept(String medicineId, String deptId,
			String supp, String grnNo,String healthAuthority)
		throws SQLException {
		Connection con = null;PreparedStatement ps = null;ResultSet rs = null;
		try {

			BasicDynaBean storeBean = StoreDAO.findByStore(Integer.parseInt(deptId));
			int storeRatePlanId = ( storeBean.get("store_rate_plan_id") == null ? 0 :(Integer)storeBean.get("store_rate_plan_id") );

			con = DataBaseUtil.getReadOnlyConnection();
			String query = GET_SEL_SUPP_MEDICINE_STOCK_QUERY;
			if (grnNo != null && !grnNo.isEmpty()){
				query = query.replace("%", "AND item_grn_no =?");
				query = query.replace("$", ",item_grn_no");
				query = query.replace("#", ",item_grn_no");
				query = query.replace("@", "AND msd.item_grn_no =?");
			} else {
				query = query.replace("%", "");
				query = query.replace("$", "");
				query = query.replace("#", "");
				query = query.replace("@", "");
			}
			ps = con.prepareStatement(query);
			if (grnNo != null && !grnNo.isEmpty()){
				
				ps.setString(1, grnNo);
				ps.setString(2, healthAuthority);
				ps.setString(3, supp);
				ps.setInt(4, Integer.parseInt(medicineId));
				ps.setInt(5, storeRatePlanId);
				ps.setString(6, supp);
				ps.setInt(7, Integer.parseInt(deptId));
				ps.setInt(8, Integer.parseInt(medicineId));
				ps.setString(9, grnNo);
			} else {
				ps.setString(1, healthAuthority);
				ps.setString(2, supp);
				ps.setInt(3, Integer.parseInt(medicineId));
				ps.setInt(4, storeRatePlanId);
				ps.setString(5, supp);
				ps.setInt(6, Integer.parseInt(deptId));
				ps.setInt(7, Integer.parseInt(medicineId));
			}
			rs = ps.executeQuery();

			ArrayList<MedicineStock> l = new ArrayList();
			while (rs.next()) {
				MedicineStock stock = new MedicineStock();
				populateStockDTO(stock, rs);
				l.add(stock);
			}
			return l;
		} finally {
			DataBaseUtil.closeConnections(con, ps, rs);
		}
	}

	public static boolean doesColumnExist(ResultSet crs, String name) throws SQLException{
		ResultSetMetaData meta = crs.getMetaData();

		int numCol = meta.getColumnCount();

		for (int i = 1; i < numCol+1; i++) {
			if(meta.getColumnLabel(i).equals(name)){
				return true;
			}

		}
		return false;
	}

	private static final String PAT_AMT_QRY =
	"SELECT ipd.patient_amount, ipd.patient_amount_per_category, ipd.patient_percent, ipd.patient_amount_cap, ipd.insurance_category_id "+
	" FROM store_item_details m " +
	"   LEFT OUTER JOIN insurance_plan_details ipd on ipd.insurance_category_id = m.insurance_category_id "+
	" where m.medicine_id = ? AND ipd.plan_id = ? and ipd.patient_type = ? ";

	public static BasicDynaBean getPatientAmounts(String medicineId, String planId, String patientType)
		throws SQLException {
		Connection con = null;PreparedStatement ps = null;ResultSet rs = null;
		String thisMedId = medicineId;
		String thisPlanId = planId;
		String thisPatientType = patientType;
		BasicDynaBean patAmt = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(PAT_AMT_QRY);
			ps.setInt(1, Integer.parseInt(thisMedId));
			ps.setInt(2, Integer.parseInt(thisPlanId));
			ps.setString(3, thisPatientType);
			patAmt = DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps, rs);
		}
		return patAmt;
	}

	private static final String ITEM_BATCH_COUNT = "SELECT count(*) as count FROM store_stock_details " +
		" WHERE medicine_id=? AND batch_no=?";

	public static int getBatchEntriesCount(int medicineId, String batchNo) throws SQLException {
		BasicDynaBean b = DataBaseUtil.queryToDynaBean(ITEM_BATCH_COUNT, new Object[]{medicineId, batchNo});
		long count = (Long) b.get("count");
		return (int) count;
	}

	private static final String PROPOGATE_BATCH_ATTR =
		" UPDATE store_stock_details ssd SET tax = main.tax, " +
		"  item_ced_amt = main.item_ced_amt, " +
		"  last_cp_grn = main.last_cp_grn, max_cp_grn = main.max_cp_grn, package_uom = main.package_uom " +
		" FROM (SELECT * FROM store_stock_details issd WHERE issd.item_batch_id = ? AND issd.dept_id=?) main " +
		" WHERE ssd.item_batch_id = ? AND ssd.dept_id != ? ";

	public static void propogateBatchAttributes(Connection con, int storeId, int itemBatchId)
	throws SQLException {
	  try (PreparedStatement ps = con.prepareStatement(PROPOGATE_BATCH_ATTR);) {
    		int i=1;
    		ps.setInt(i++, itemBatchId);
    		ps.setInt(i++, storeId);
    
    		ps.setInt(i++, itemBatchId);
    		ps.setInt(i++, storeId);
    		ps.executeUpdate();
	  }
	}
	
	/*
	 * Query used by sales/issues/returns/issue returns: this includes the patient insurance
	 * amounts required for calculations. Other screens such as stock entry/supplier returns/transfers
	 * don't require this information.
	 */
	private static final String MED_STOCK_WITH_PLAN_QUERY_BASE_FIELDS_INDENT =

		"SELECT  '' as route_id, '' as route_name, msd.qty,COALESCE(sir.tax_rate,COALESCE(ssir.tax_rate,m.tax_rate)) as tax_rate, sico.item_code," +
		"  COALESCE(sir.tax_type,COALESCE(ssir.tax_type,m.tax_type)) as tax_type," +
		"  msd.medicine_id,msd.qty_in_use,msd.qty_maint,msd.qty_retired,msd.qty_lost,msd.qty_kit,msd.qty_unknown," +
		"  msd.consignment_stock,msd.asset_approved,msd.dept_id," +
		"  m.medicine_name, mf.manf_code, mf.manf_name, mf.manf_mnemonic, " +
		"  m.issue_base_unit, m.package_type,m.bin, " +
		"  COALESCE(m.issue_units,'') AS issue_units, COALESCE(m.package_uom,'') AS master_package_uom, " +
		"	COALESCE(mc.discount,0) AS meddisc, " +
		"  mc.category, mc.category_id, mc.identification, sic.control_type_name, g.generic_name, " +
		"  '' as indent_no, mc.billable, mc.retailable, mc.claimable, m.item_barcode_id, " +
		"  m.insurance_category_id, m.prior_auth_required, cum.consumption_uom, iic.insurance_payable, " +
		"  ipm.is_copay_pc_on_post_discnt_amt, ipd.patient_amount as patient_amount, m.cons_uom_id," +
		"  ipd.patient_amount_per_category AS patient_amount_per_category," +
		"  ipd.category_payable AS category_payable," +
		"  ipd.patient_percent as patient_percent, ipd.patient_amount_cap as patient_amount_cap " +
		"  ,issue_rate_expr,sibd.mrp,sibd.batch_no,sibd.exp_dt,sibd.item_batch_id,sibd.mrp,m.cust_item_code, " +
		"  CASE WHEN cust_item_code IS NOT NULL AND  TRIM(cust_item_code) != ''  THEN medicine_name||' - '||cust_item_code ELSE medicine_name END as cust_item_code_with_name " +
		"  ,sir.selling_price_expr as visit_selling_expr, ssir.selling_price_expr as store_selling_expr ";

		private static final String MED_STOCK_WITH_PLAN_QUERY_STOCK_TABLES_INDENT =
		" FROM ( " +
		" SELECT medicine_id,sum(qty) as qty,item_batch_id,batch_no,sum(qty_in_use) as qty_in_use,sum(qty_maint) as qty_maint, " +
		" sum(qty_retired) as qty_retired,sum(qty_lost) as qty_lost,sum(qty_kit) as qty_kit, " +
	    " sum(qty_unknown) as qty_unknown,consignment_stock,asset_approved,dept_id,min(tax) as tax " +
	    " FROM store_stock_details  GROUP BY batch_no,item_batch_id,medicine_id,consignment_stock,asset_approved,dept_id " +
		"   ORDER BY medicine_id) as msd " ;

	private static final String MED_STOCK_WITH_PLAN_QUERY_BASE_JOIN_TABLES_INDENT =
		"  JOIN store_item_batch_details sibd USING(item_batch_id) " +
		"  JOIN store_item_details m ON(m.medicine_id = msd.medicine_id) " +
		"  JOIN manf_master mf ON (mf.manf_code = m.manf_name) " +
		"  JOIN store_category_master mc ON mc.category_id = m.med_category_id " +
		"  LEFT JOIN ha_item_code_type hict ON (hict.medicine_id = m.medicine_id AND hict.health_authority=?) " +
		"  LEFT JOIN store_item_codes sico ON (sico.medicine_id = m.medicine_id AND sico.code_type = hict.code_type) " +
		"  LEFT JOIN store_item_controltype sic ON sic.control_type_id=m.control_type_id "+
		"  LEFT JOIN generic_name g ON m.generic_name = g.generic_code "+
		"  LEFT JOIN insurance_plan_details ipd on (m.insurance_category_id = ipd.insurance_category_id " +
		"    AND ipd.patient_type=? AND ipd.plan_id=?) " +
		"  LEFT JOIN insurance_plan_main ipm on (ipm.plan_id = ipd.plan_id) " +
		"  JOIN item_insurance_categories iic ON (iic.insurance_category_id = m.insurance_category_id) " +
		"  LEFT JOIN store_item_rates sir ON (msd.medicine_id = sir.medicine_id AND " +
		"    sir.store_rate_plan_id = ?) " +
		"  LEFT JOIN store_item_rates ssir ON (msd.medicine_id = ssir.medicine_id AND " +
		"    ssir.store_rate_plan_id = ?) " +
		"  LEFT JOIN store_item_issue_rates siir ON (siir.medicine_id = msd.medicine_id)         " +
		" LEFT JOIN consumption_uom_master cum ON (cum.cons_uom_id = m.cons_uom_id)" +
		" WHERE consignment_stock=false AND asset_approved = 'Y' # ";
	
	private static final String MED_STOCK_SELLING_PRICE_QUERY_BATCH_MRP_NO_INDENT =
			",COALESCE( m.item_selling_price, sibd.mrp) as selling_price," +
			" COALESCE( m.item_selling_price, sibd.mrp) as orig_selling_price ";
	
	private static final String MED_STOCK_SELLING_PRICE_QUERY_BATCH_MRP_YES_INDENT =
			",COALESCE( m.item_selling_price, sibd.mrp) as selling_price," +
			"COALESCE( m.item_selling_price, sibd.mrp) as orig_selling_price " ;

	// sort order is based on which items we should be selling first.
	private static final String ORDER_BY_WITH_ZEROS_INDENT =
		" ORDER BY sign(msd.qty) DESC,exp_dt";

	private static final String WHERE_CLAUSE_WITHOUT_ZEROS_INDENT = "  AND msd.qty > 0 AND asset_approved='Y' " ;
	private static final String ORDER_BY_WITHOUT_ZEROS_INDENT =
		" ORDER BY exp_dt";

	/**
	 * TODO - Remove this method once we are doing store tariff in issues screen.
	 * 
	 * This method is used to server for issues via indent.
	 * 
	 * @param medicineIds
	 * @param deptId
	 * @param planId
	 * @param visitType
	 * @param includeZeroStock
	 * @param visitStoreRatePlanId
	 * @param healthAuthority
	 * @return
	 * @throws SQLException
	 */
	public static List getMedicineStockWithPatAmtsInDeptIndent(List<Integer> medicineIds, int deptId, int planId,
			String visitType, boolean includeZeroStock, int visitStoreRatePlanId, String healthAuthority) throws SQLException {

		if (medicineIds == null || medicineIds.size() == 0)
			throw new IllegalArgumentException("Stock query requires at least one item");
		BasicDynaBean storeBean = StoreDAO.findByStore(deptId);
		int storeRatePlanId = ( storeBean.get("store_rate_plan_id") == null ? 0 :(Integer)storeBean.get("store_rate_plan_id") );
		boolean useBatchMRP = ((String)storeBean.get("use_batch_mrp")).equals("Y");

		StringBuilder query = new StringBuilder(MED_STOCK_WITH_PLAN_QUERY_BASE_FIELDS_INDENT);

		query.append(useBatchMRP ? MED_STOCK_SELLING_PRICE_QUERY_BATCH_MRP_YES_INDENT : MED_STOCK_SELLING_PRICE_QUERY_BATCH_MRP_NO_INDENT);
		query.append(MED_STOCK_WITH_PLAN_QUERY_STOCK_TABLES_INDENT);
		query.append(MED_STOCK_WITH_PLAN_QUERY_BASE_JOIN_TABLES_INDENT.replace("#", " AND dept_id=? "));

		QueryBuilder.addWhereFieldOpValue(true, query, "msd.medicine_id", "IN", medicineIds);
		if (includeZeroStock)
			query.append(ORDER_BY_WITH_ZEROS_INDENT);
		else
			query.append(WHERE_CLAUSE_WITHOUT_ZEROS_INDENT+ORDER_BY_WITHOUT_ZEROS_INDENT);

		log.debug("Stock query: " + query.toString() + " size: " + medicineIds.size() );
		Object params[] = new Object[6 + medicineIds.size()];
		int i=0;
		params[i++] = healthAuthority;
		params[i++] = visitType;
		params[i++] = planId;
		params[i++] = visitStoreRatePlanId;
		params[i++] = storeRatePlanId;       
		params[i++] = deptId;

		for (int medId : medicineIds) {
			params[i++] = medId;
		}
		return DataBaseUtil.queryToDynaList(query.toString(), params);
	}
}