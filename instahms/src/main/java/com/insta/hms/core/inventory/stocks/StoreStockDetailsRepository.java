package com.insta.hms.core.inventory.stocks;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Repository
public class StoreStockDetailsRepository extends MasterRepository<Integer> {

  private static final String ITEM_DETAIL_LIST_QUERY = " SELECT s.medicine_id,c.identification,coalesce(sibd.mrp,0) as mrp,"
      + "  COALESCE(sir.tax_rate,COALESCE(ssir.tax_rate,p.tax_rate)) as tax_rate,"
      + "  COALESCE(sir.tax_type,COALESCE(ssir.tax_type,p.tax_type)) as tax_type,p.medicine_name,"
      + "   CASE" + "     WHEN issue_rate_expr IS NOT NULL THEN 'Y' ELSE 'N'"
      + "   END AS is_markup_rate," + "   (CASE"
      + "     WHEN c.issue_type='C'  THEN 'CONSUMABLE' WHEN c.issue_type='L'  THEN 'REUSABLE' "
      + "     WHEN c.issue_type='P'  THEN 'PERMANENT' WHEN c.issue_type='R' THEN 'RETAILABLE' "
      + "   END ) AS issue_type ,s.dept_id,sibd.batch_no,sibd.item_batch_id,"
      + "   COALESCE(sibd.exp_dt,NULL) AS exp_dt,c.billable,s.qty,p.package_type,"
      + "   p.issue_base_unit,c.category_id," + "   s.consignment_stock, p.issue_units,c.billable,"
      + "   COALESCE(c.discount,0) AS meddisc,p.tax_type, "
      + "   p.item_barcode_id,'' as patient_amount, '' as patient_amount_per_category,"
      + "   '' as patient_percent, '' as patient_amount_cap ,"
      + "   p.insurance_category_id, true AS first_of_category, "
      + "   p.package_uom,issue_rate_expr, p.control_type_id, sict.control_type_name, "
      + "   ssir.selling_price_expr as visit_selling_expr, sir.selling_price_expr as store_selling_expr ";

  private static final String ITEM_DETAIL_LIST_QUERY_TABLES =

      " FROM (SELECT medicine_id,sum(qty) as qty,item_batch_id,batch_no,sum(qty_in_use) as qty_in_use,sum(qty_maint) as qty_maint, "
          + "   sum(qty_retired) as qty_retired,sum(qty_lost) as qty_lost,sum(qty_kit) as qty_kit, "
          + "   sum(qty_unknown) as qty_unknown,consignment_stock,asset_approved,dept_id "
          + "   FROM store_stock_details  GROUP BY batch_no,item_batch_id,medicine_id,consignment_stock,asset_approved,dept_id "
          + "   ORDER BY medicine_id)  as s "
          + "   JOIN store_item_batch_details sibd USING(item_batch_id)"
          + "   JOIN store_item_details p ON (p.medicine_id = s.medicine_id)"
          + "   LEFT JOIN store_item_issue_rates siir ON (siir.medicine_id = s.medicine_id)"
          + "   JOIN store_category_master c ON(c.category_id =p.med_category_id) "
          + "   LEFT JOIN store_item_controltype sict ON(sict.control_type_id = p.control_type_id) "
          + "   LEFT JOIN store_item_rates ssir ON (p.medicine_id = ssir.medicine_id AND "
          + "     ssir.store_rate_plan_id = ?) ";

  private static final String STORE_ITEM_RATES_JOIN = "   LEFT JOIN store_item_rates sir ON( sir.medicine_id = s.medicine_id  AND sir.store_rate_plan_id = ?)";

  private static final String ITEM_DETAIL_LIST_QUERY_WHERE = "  WHERE s.asset_approved='Y' ";

  public StoreStockDetailsRepository() {
    super(new String[] { "dept_id", "medicine_id", "batch_no", "item_lot_id"	 }, null, "store_stock_details",
        "store_stock_id");
  }

  private static final String CHECK_QUANTITY_AVAILABLE = "SELECT qty FROM (select coalesce(sum(qty),0) as qty from store_stock_details where medicine_id =? and item_batch_id=?"
      + " and dept_id=? and qty>0) as foo where qty >=?";

  public BasicDynaBean getQuantity(Integer storeId, Integer itemId, int identifier,
      BigDecimal returnQuantity) {
    return DatabaseHelper.queryToDynaBean(CHECK_QUANTITY_AVAILABLE,
        new Object[] { itemId, identifier, storeId, returnQuantity });
  }

  public BasicDynaBean getItemDetails(Integer visitStoreRatePlanId, Integer storeRatePlanId,
      String batchNo, Integer medicine_id) {
    return DatabaseHelper.queryToDynaBean(
        ITEM_DETAIL_LIST_QUERY + ITEM_DETAIL_LIST_QUERY_TABLES + STORE_ITEM_RATES_JOIN
            + ITEM_DETAIL_LIST_QUERY_WHERE + " and S.batch_no=? and S.medicine_id=? ",
        new Object[] { visitStoreRatePlanId, storeRatePlanId, batchNo, medicine_id });
  }

  private static final String GET_PACKAGE_MRP_AND_CP = " SELECT issue_base_unit, sibd.mrp, package_cp "
      + "   FROM store_stock_details ssd "
      + "     JOIN store_item_batch_details sibd USING(item_batch_id)"
      + "     JOIN store_item_details sid ON(sid.medicine_id = ssd.medicine_id) "
      + " WHERE ssd.medicine_id = ? AND sibd.batch_no = ?";

  public BasicDynaBean getPackageMrpAndCP(int medicineId, String batchNo) {
    return DatabaseHelper.queryToDynaBean(GET_PACKAGE_MRP_AND_CP,
        new Object[] { medicineId, batchNo });
  }

  private static final String GET_BATCH_LOT_DETAILS = "SELECT il.*, ssd.*,sid.issue_base_unit,ibd.mrp "
      + " FROM store_item_lot_details il " + "  JOIN store_stock_details ssd USING(item_lot_id)"
      + "  JOIN store_item_batch_details ibd ON (ssd.item_batch_id = ibd.item_batch_id) "
      + "  JOIN store_item_details sid ON (sid.medicine_id = ssd.medicine_id) "
      + " WHERE ssd.dept_id = ? AND ibd.item_batch_id = ?";

  public List<BasicDynaBean> getBatchLotDetails(int storeId, int itemBatchId) {
    return DatabaseHelper.queryToDynaList(GET_BATCH_LOT_DETAILS,
        new Object[] { storeId, itemBatchId });
  }

  public List<BasicDynaBean> getBatchLotDetails(int storeId, int itemBatchId, String purchaseType) {
    return DatabaseHelper.queryToDynaList(
        GET_BATCH_LOT_DETAILS + " AND purchase_type = ? ORDER BY il.item_lot_id ",
        new Object[] { storeId, itemBatchId, purchaseType });
  }

  public List<BasicDynaBean> getBatchLotDetails(int storeId, int itemBatchId, String purchaseType,
      String grnNo) {
    return DatabaseHelper.queryToDynaList(
        GET_BATCH_LOT_DETAILS + " AND purchase_type = ? AND grn_no = ? ORDER BY il.item_lot_id ",
        new Object[] { storeId, itemBatchId, purchaseType, grnNo });
  }

  private static final String ADD_QTY_TO_STOCK_DETAILS = " UPDATE store_stock_details SET qty=qty+?, qty_in_use=qty_in_use-?,qty_in_transit=qty_in_transit+?, "
      + " username=?, change_source=? " + " WHERE store_stock_id=? ";

  public int addQtyToStockDetails(int stockId, BigDecimal qtyToAdd, String userName,
      String changeSource, boolean transitOnly) {
    if (transitOnly) {
      return DatabaseHelper.update(ADD_QTY_TO_STOCK_DETAILS,
          new Object[] { BigDecimal.ZERO, qtyToAdd, qtyToAdd, userName, changeSource, stockId });
    } else {
      return DatabaseHelper.update(ADD_QTY_TO_STOCK_DETAILS,
          new Object[] { qtyToAdd, qtyToAdd, BigDecimal.ZERO, userName, changeSource, stockId });
    }
  }

  private final String INSERT_TXN_LOT = " INSERT INTO store_transaction_lot_details (transaction_id, transaction_type, item_lot_id,qty) "
      + " VALUES (?, ?, ?, ?)";

  public int insertTxnLot(int txnId, String txnType, int itemLotId, BigDecimal qty) {
    return DatabaseHelper.insert(INSERT_TXN_LOT, new Object[] { txnId, txnType, itemLotId, qty });
  }

  public int updateStockTimestamp() {
    return DatabaseHelper
        .update("UPDATE store_main_stock_timestamp SET medicine_timestamp = medicine_timestamp+1");
  }

  public int updateStoresStockTimeStamp(int storeId) {
    return DatabaseHelper.update(
        "UPDATE stores SET stock_timestamp = stock_timestamp+1 WHERE dept_id = ?",
        new Object[] { storeId });
  }
  
  @Override
  public List<BasicDynaBean> findByCriteria(Map<String, Object> filterMap) {
    return super.findByCriteria(filterMap);
  }
  
  
  private static final String GET_USER_STORE_ITEM_BATCH_COUNT_QRY = "SELECT "
      + " user_stores.dept_id, s.dept_name, coalesce(ssd.item_batch_count, 0) "
      + "   as item_batch_count "
      + " FROM "
      + "   (SELECT regexp_split_to_table(multi_store, E'\\,')::integer "
      + "     as dept_id "
      + "     FROM u_user "
      + "     WHERE emp_username=?) as user_stores "
      + " JOIN stores s USING (dept_id) "
      + " LEFT JOIN "
      + "   (SELECT s.dept_id, s.dept_name, "
      + "         count(distinct item_batch_id) as item_batch_count "
      + "   FROM stores s "
      + "   JOIN store_stock_details ssd USING (dept_id) "
      + "   JOIN store_item_batch_details sibd USING(item_batch_id) "
      + "   WHERE s.status = 'A' AND (s.stock_take_item_exp_months IS NULL OR "
      + "     ssd.qty > 0 OR sibd.exp_dt >= date_trunc('month', "
      + "     now()::date - "
      + "     (s.stock_take_item_exp_months || ' months ')::interval)) "
      + "   GROUP BY s.dept_id, s.dept_name) as ssd "
      + " ON (ssd.dept_id = user_stores.dept_id) "
      + " ORDER BY s.dept_name ";
	  
  private static final String GET_ALL_STORES_ITEM_BATCH_COUNT_QRY = "SELECT "
      + " s.dept_id, s.dept_name, coalesce(ssd.item_batch_count, 0) "
      + "   as item_batch_count "
      + " FROM stores s "
      + " LEFT JOIN "
      + " (SELECT s.dept_id, count(distinct item_batch_id) as item_batch_count "
      + "   FROM stores s "
      + "   JOIN store_stock_details ssd USING (dept_id) "
      + "   JOIN store_item_batch_details sibd USING(item_batch_id) "
      + "   WHERE (s.stock_take_item_exp_months IS NULL OR "
      + "     sibd.exp_dt IS NULL OR "
      + "     ssd.qty > 0 OR sibd.exp_dt >= date_trunc('month', "
      + "         now()::date - "
      + "         (s.stock_take_item_exp_months || ' months ')::interval)) "
      + "   GROUP BY s.dept_id) as ssd "
      + " USING (dept_id) "
      + " WHERE s.status = 'A' AND ( 0 = ? OR s.center_id = ? ) "
      + " ORDER BY s.dept_name ";

  public List<BasicDynaBean> getItemBatchCount(/*String username*/) {
    Integer roleId = RequestContext.getRoleId();
    if (1 == roleId || 2 == roleId) {
      Integer loggedInCenter = RequestContext.getCenterId();
      return DatabaseHelper.queryToDynaList(GET_ALL_STORES_ITEM_BATCH_COUNT_QRY,
          new Object[] { loggedInCenter, loggedInCenter });
    }
    String username = RequestContext.getUserName();
	  return DatabaseHelper.queryToDynaList(GET_USER_STORE_ITEM_BATCH_COUNT_QRY, username);
  }

  private static final String MEDICINE_LOOKUP_QUERY = " SELECT * FROM (SELECT DISTINCT"
      + " m.medicine_name, m.medicine_id, m.cust_item_code, m.item_barcode_id, "
      + " m.issue_units,m.package_uom,m.issue_base_unit, "
      + " CONCAT_WS ('##', m.item_barcode_id, m.cust_item_code, m.medicine_name) "
      + " as search_field "
      + " FROM store_stock_details msd JOIN store_item_details m USING(medicine_id) "
      + " WHERE msd.dept_id=?) FOO WHERE search_field ILIKE ? ";

  /**
   * Simple lookup matching the search text anywhere in the medicine name, 
   * item code or barcode id
   * @param storeId
   * @param searchText
   * @return List containing the list of medicines, matching the provided text
   */
  public List<BasicDynaBean> searchMedicinesInStock(Integer storeId,
      String searchText) {
    if (null != storeId && null != searchText) {
      return DatabaseHelper.queryToDynaList(MEDICINE_LOOKUP_QUERY,
          new Object[] { storeId, "%" + searchText + "%" });
    } else {
      return Collections.emptyList();
    }
  }
  
  private static final String STORE_ITEM_LOTS_QUERY = "SELECT "
      + " dept_id, item_batch_id, item_lot_id, qty "
      + " FROM store_stock_details WHERE dept_id = ? ";
  private static final String STORE_ITEM_LOTS_NON_ZERO_CLAUSE = " AND "
      + " qty != 0 ";
  private static final String STORE_ITEM_NON_NEGATIVE_CLAUSE = " AND "
      + " qty >= 0 ";

  public List<BasicDynaBean> getItemLots(Integer storeId,
      boolean includeZeroLots, boolean includeNegativeLots) {
    return DatabaseHelper.queryToDynaList(
        STORE_ITEM_LOTS_QUERY
            + ((!includeZeroLots) ? STORE_ITEM_LOTS_NON_ZERO_CLAUSE : "")
            + ((!includeNegativeLots) ? STORE_ITEM_NON_NEGATIVE_CLAUSE : ""),
        new Object[] { storeId });
  }

  private static final String GET_AVAILABLE_QTY_FOR_A_BTACH = "SELECT sum(qty) AS qty"
      + " FROM store_stock_details "
      + " WHERE asset_approved='Y' AND medicine_id= ? AND dept_id = ? AND batch_no = ? ";

  public BigDecimal getAvailableItemCountForBatchAndStore(int medicineId, int storeId,
      String batchNo) {
    return DatabaseHelper.getBigDecimal(GET_AVAILABLE_QTY_FOR_A_BTACH, medicineId, storeId,
        batchNo);
  }
  
  private static final String GET_AVAILABLE_QTY_DETAILS_FOR_A_BTACH = "SELECT store_stock_id, "
      + " il.item_lot_id, il.lot_time,qty, purchase_type, ssd.medicine_id, sibd.batch_no "
      + " FROM store_stock_details ssd " 
      + " JOIN store_item_lot_details il USING(item_lot_id) "
      + " JOIN store_item_batch_details sibd ON(sibd.item_batch_id = ssd.item_batch_id) "
      + " WHERE ssd.medicine_id= ? AND ssd.dept_id = ? and sibd.batch_no = ? and ssd.qty >0 "
      + " ORDER BY store_stock_id desc ";

  public List<BasicDynaBean> getItemQtyForBatch(int medicineId, int storeId, String batchNo) {
    return DatabaseHelper.queryToDynaList(GET_AVAILABLE_QTY_DETAILS_FOR_A_BTACH,
        new Object[] { medicineId, storeId, batchNo });
  }
  
  private static final String UPDATE_STOCK_CED_AMT = "UPDATE store_stock_details SET item_ced_amt = ? "
      + " WHERE medicine_id = ?AND batch_no=? AND dept_id=?";

  private static final String GET_ITEM_CED_AMT = "SELECT  item_ced_amt FROM store_stock_details "
      + " WHERE dept_id=? AND medicine_id=? AND batch_no=? limit 1 ";

  public boolean updateStock(int deptIdTo, int deptIdFrom, String batchNo, int medicineId) {
    BigDecimal itemCedAmt = DatabaseHelper.getBigDecimal(GET_ITEM_CED_AMT, deptIdFrom, medicineId,
        batchNo);
    int result = DatabaseHelper.update(UPDATE_STOCK_CED_AMT, itemCedAmt, medicineId, batchNo,
        deptIdTo);
    return result != 0;
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
          " LEFT JOIN ha_item_code_type hict ON (hict.medicine_id = pmd.medicine_id) " +
          " LEFT JOIN store_item_codes sic ON (sic.medicine_id = pmd.medicine_id AND sic.code_type = hict.code_type) " +
          " JOIN store_grn_main gm using(grn_no) " +
          " LEFT JOIN store_po po ON (gm.po_no=po.po_no AND gd.medicine_id=po.medicine_id) " +
          " JOIN store_category_master mc ON (pmd.med_category_id = mc.category_id) " +
          "WHERE gd.grn_no=? AND gm.debit_note_no is null ";

  private static final String GET_GRN_DET =
      "SELECT s.dept_name as store_name,sm.supplier_name,sm.cust_supplier_code, sm.supplier_address, pinv.supplier_id, pinv.round_off, pinv.invoice_no," +
          "  TO_CHAR(pinv.due_date,'DD-MM-YYYY') AS due_date, sm.tcs_applicable, " +
          "  TO_CHAR(pinv.invoice_date,'DD-MM-YYYY') AS invoice_date," +
          "  pinv.discount, pinv.cess_tax_amt, pngm.grn_no, pinv.discount_type, pinv.discount_per, " +
          "  pinv.tcs_type, pinv.tcs_per, pinv.tcs_amount, " +
          "  pngm.store_id, pinv.cess_tax_rate,coalesce(other_charges,0) AS other_charges, " +
          "  pinv.tax_name, pinv.cst_rate, pngm.consignment_stock, pinv.debit_amt, pngm.po_no, " +
          "  pinv.supplier_invoice_id, pinv.status, pinv.remarks, pinv.cash_purchase, " +
          "  pngm.grn_qty_unit, pinv.po_reference, pinv.payment_remarks, company_name, means_of_transport, consignment_no, " +
          "  TO_CHAR(consignment_date,'DD-MM-YYYY') as consignment_date,  " +
          "  CASE WHEN pngm.consignment_stock = true then 't' else 'f' END AS stock_type, invoice_file_name, purpose_of_purchase, " +
          "  c_form, form_8h,transportation_charges " +
          " FROM store_grn_main pngm " +
          "  JOIN store_invoice pinv USING (supplier_invoice_id) " +
          "  JOIN supplier_master sm ON pinv.supplier_id=supplier_code " +
          "  JOIN stores s ON (s.dept_id = pngm.store_id) " +
          " WHERE grn_no = ? and debit_note_no is null ";


  public List<BasicDynaBean> getGrnItems(String grnNo) {
    return DatabaseHelper.queryToDynaList(GET_GRN_MED, grnNo);
  }

  public BasicDynaBean getGrnDetails(String grnNo) {
    return DatabaseHelper.queryToDynaBean(GET_GRN_DET, grnNo);
  }

}