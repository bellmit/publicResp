package com.insta.hms.core.inventory.stockmgmt;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.QueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Helper class for Stock operations.
 * 
 * @author irshadmohammed
 *
 */
@Repository
public class StockRepository {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(StockRepository.class);

  /** The Constant GET_BATCH_CP_DETAILS. */
  private static final String GET_BATCH_CP_DETAILS = "SELECT max(coalesce(il.package_cp,0)) as"
      + " max_package_cp, avg(coalesce(il.package_cp,0)) as "
      + " avg_package_cp,max(mrp) as mrp " + " FROM store_item_lot_details il "
      + "  JOIN store_stock_details ssd USING(item_lot_id)"
      + "  JOIN store_item_batch_details ibd ON (ssd.item_batch_id = ibd.item_batch_id) "
      + "  JOIN store_item_details sid ON (sid.medicine_id = ssd.medicine_id) "
      + " WHERE ssd.dept_id = ? AND ibd.item_batch_id = ?";

  /**
   * Gets the CP details.
   *
   * @param storeId the store id
   * @param itemBatchId the item batch id
   * @return the CP details
   */
  public BasicDynaBean getCPDetails(int storeId, int itemBatchId) {
    return DatabaseHelper.queryToDynaBean(GET_BATCH_CP_DETAILS,
        new Object[] {storeId, itemBatchId});
  }

  /** The Constant GET_BATCH_LOT_DETAILS. */
  private static final String GET_BATCH_LOT_DETAILS = "SELECT il.*, ssd.*,sid.issue_base_unit,"
      + " ibd.mrp FROM store_item_lot_details il JOIN store_stock_details ssd USING(item_lot_id)"
      + "  JOIN store_item_batch_details ibd ON (ssd.item_batch_id = ibd.item_batch_id) "
      + "  JOIN store_item_details sid ON (sid.medicine_id = ssd.medicine_id) "
      + " WHERE ssd.dept_id = ? AND ibd.item_batch_id = ?";

  /**
   * Gets the batch sorted lot details.
   *
   * @param storeId the store id
   * @param itemBatchId the item batch id
   * @return the batch sorted lot details
   */
  public List<BasicDynaBean> getBatchSortedLotDetails(int storeId, int itemBatchId) {
    return DatabaseHelper.queryToDynaList(GET_BATCH_LOT_DETAILS + " ORDER BY il.item_lot_id",
        new Object[] {storeId, itemBatchId});
  }

  /** The Constant GET_SELLING_PRICE. */
  private static final String GET_SELLING_PRICE = "SELECT COALESCE( sid.item_selling_price,"
      + " ibd.mrp) as selling_price FROM "
      + " store_item_batch_details ibd JOIN store_item_details sid "
      + " ON (sid.medicine_id = ibd.medicine_id) WHERE ibd.item_batch_id = ?";

  /**
   * Gets the selling price.
   *
   * @param itemBatchId the item batch id
   * @return the selling price
   */
  public BasicDynaBean getSellingPrice(int itemBatchId) {
    return DatabaseHelper.queryToDynaBean(GET_SELLING_PRICE, new Object[] {itemBatchId});
  }

  /** The Constant GET_MEDICINES_IN_STOCK_STORE_WISE. */
  private static final String GET_MEDICINES_IN_STOCK_STORE_WISE = " SELECT DISTINCT"
      + " m.medicine_name, m.medicine_id,m.cust_item_code, item_barcode_id, "
      + " issue_units,m.package_uom,issue_base_unit,sic.item_code, "
      + " CASE WHEN m.cust_item_code IS NOT NULL AND  TRIM(m.cust_item_code) != ''  "
      + " THEN m.medicine_name||' - '||m.cust_item_code ELSE m.medicine_name END as "
      + " cust_item_code_with_name, CASE WHEN m.cust_item_code IS NOT NULL AND "
      + " TRIM(m.cust_item_code) != '' AND item_barcode_id IS NOT NULL AND "
      + " TRIM(item_barcode_id) != '' THEN m.medicine_name||' - '||m.cust_item_code||'"
      + " - '||item_barcode_id WHEN m.cust_item_code IS NOT NULL AND "
      + " TRIM(m.cust_item_code) != ''  THEN m.medicine_name||' - '||m.cust_item_code "
      + " WHEN item_barcode_id IS NOT NULL AND  TRIM(item_barcode_id) != '' "
      + " THEN m.medicine_name||' - '||item_barcode_id ELSE m.medicine_name END "
      + " as cust_item_code_barcode_with_name, " + " m.insurance_category_id, m.billing_group_id "
      + " FROM store_stock_details msd JOIN store_item_details m USING(medicine_id) "
      + " JOIN store_category_master icm ON (icm.category_id = m.med_category_id)"
      + " LEFT JOIN ha_item_code_type hict "
      + " ON (hict.medicine_id = m.medicine_id AND hict.health_authority=?) "
      + " LEFT JOIN store_item_codes sic "
      + " ON (sic.medicine_id = m.medicine_id AND sic.code_type = hict.code_type) "
      + " WHERE msd.dept_id=? ";

  /**
   * This method is used to get all medicines in stock.
   *
   * @param writer Writer Type
   * @param includeZeroStock boolean Type
   * @param retailable boolean Type
   * @param billable boolean Type
   * @param issueType String Array
   * @param includeConsignment boolean Type
   * @param includeUnapproved boolean Type
   * @param onlySalesStores boolean Type
   * @param singleStore boolean Type
   * @param storeId int Type
   * @param healthAuthority String type
   * @param grnNo String type
   * @param salesStoresList List type
   * @param storeList List type
   * @param medicineNameFilterText the medicine name filter text
   * @throws SQLException throws SQLException
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public void getMedicineNamesInStock(Writer writer, boolean includeZeroStock,
      Boolean retailable, Boolean billable, String[] issueType, boolean includeConsignment,
      boolean includeUnapproved, boolean onlySalesStores, boolean singleStore, int storeId,
      String healthAuthority, String grnNo, List<BasicDynaBean> salesStoresList,
      List<BasicDynaBean> storeList, String medicineNameFilterText)
      throws SQLException, IOException {
    int size = 2;
    StringBuilder query = new StringBuilder(GET_MEDICINES_IN_STOCK_STORE_WISE);

    constructGetStockQuery(includeZeroStock, retailable, billable, issueType,
        includeConsignment, includeUnapproved, medicineNameFilterText, query);

    if (grnNo != null) {
      query.append(" AND item_grn_no = ?");
      size++;
    }

    Object[] arr = new Object[size];
    arr[0] = healthAuthority;
    if (singleStore) {
      arr[1] = storeId;
      if (grnNo != null) {
        arr[2] = grnNo;
      }
      DatabaseHelper.queryToJsonStream(query.toString(), arr, writer);
    } else {
      List<BasicDynaBean> stores = onlySalesStores ? salesStoresList : storeList;
      Iterator<BasicDynaBean> storesIterator = stores.iterator();
      while (storesIterator.hasNext()) {
        BasicDynaBean storeBean = storesIterator.next();
        Integer deptId = (Integer) storeBean.get("dept_id");
        arr[1] = deptId;
        DatabaseHelper.queryToJsonStream(query.toString(), arr, writer);
      }
    }
  }



  /**
   * Gets the medicine names in stock.
   *
   * @param includeZeroStock the include zero stock
   * @param retailable the retailable
   * @param billable the billable
   * @param issueType the issue type
   * @param includeConsignment the include consignment
   * @param includeUnapproved the include unapproved
   * @param onlySalesStores the only sales stores
   * @param singleStore the single store
   * @param storeId the store id
   * @param healthAuthority the health authority
   * @param grnNo the grn no
   * @param salesStoresList the sales stores list
   * @param storeList the store list
   * @param medicineNameFilterText the medicine name filter text
   * @return the medicine names in stock
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public List<BasicDynaBean> getMedicineNamesInStock(boolean includeZeroStock,
      Boolean retailable, Boolean billable, String[] issueType, boolean includeConsignment,
      boolean includeUnapproved, boolean onlySalesStores, boolean singleStore, int storeId,
      String healthAuthority, String grnNo, List<BasicDynaBean> salesStoresList,
      List<BasicDynaBean> storeList, String medicineNameFilterText)
      throws SQLException, IOException {
    List<BasicDynaBean> medicineNamesInStock = new ArrayList<>();
    int size = 2;
    StringBuilder query = new StringBuilder(GET_MEDICINES_IN_STOCK_STORE_WISE);

    constructGetStockQuery(includeZeroStock, retailable, billable, issueType,
        includeConsignment, includeUnapproved, medicineNameFilterText, query);

    if (grnNo != null) {
      query.append(" AND item_grn_no = ?");
      size++;
    }

    Object[] arr = new Object[size];
    arr[0] = healthAuthority;
    if (singleStore) {
      arr[1] = storeId;
      if (grnNo != null) {
        arr[2] = grnNo;
      }
      medicineNamesInStock.addAll(DatabaseHelper.queryToDynaList(query.toString(), arr));
    } else {
      List<BasicDynaBean> stores = onlySalesStores ? salesStoresList : storeList;
      Iterator<BasicDynaBean> storesIterator = stores.iterator();
      while (storesIterator.hasNext()) {
        BasicDynaBean storeBean = storesIterator.next();
        Integer deptId = (Integer) storeBean.get("dept_id");
        arr[1] = deptId;
        medicineNamesInStock.addAll(DatabaseHelper.queryToDynaList(query.toString(), arr));
      }
    }
    return medicineNamesInStock;
  }



  /**
   * Construct get stock query.
   *
   * @param includeZeroStock the include zero stock
   * @param retailable the retailable
   * @param billable the billable
   * @param issueType the issue type
   * @param includeConsignment the include consignment
   * @param includeUnapproved the include unapproved
   * @param medicineNameFilterText the medicine name filter text
   * @param query the query
   */
  private void constructGetStockQuery(boolean includeZeroStock, Boolean retailable,
      Boolean billable, String[] issueType, boolean includeConsignment,
      boolean includeUnapproved, String medicineNameFilterText, StringBuilder query) {
    if (retailable != null) {
      query.append(" AND retailable = " + retailable);
    }

    if (billable != null) {
      query.append(" AND billable = " + billable);
    }

    if (issueType != null) {
      query.append(" AND icm.issue_type IN (");
      boolean first = true;
      for (String type : issueType) {
        if (!first) {
          query.append(",");
        }
        first = false;
        query.append("'").append(type).append("'");
      }
      query.append(")");
    }

    if (!includeConsignment) {
      query.append(" AND consignment_stock = false ");
    }
    if (!includeUnapproved) {
      query.append(" AND asset_approved = 'Y' ");
    }

    if (includeZeroStock) {
      /*
       * We have to include all medicines, except those that have zero stock AND are made
       * inactive. Thus, if we want to get rid of a medicine from the list, user has to make it
       * inactive, and till there is stock, it will keep showing. Only when stock becomes zero,
       * it will be excluded from the list.
       */
      query.append(" AND NOT (m.status='I' AND qty=0)");
    } else {
      // Disallow zero quantity selling, so we return only medicines where qty > 0
      query.append(" AND qty>0");
    }

    if (StringUtils.isNotBlank(medicineNameFilterText)) {
      query.append(" AND m.medicine_name ilike '%" + medicineNameFilterText + "%'");
    }
  }



  /** The Constant GET_ITEM_DETAILS_FIELDS. */
  public static final String GET_ITEM_DETAILS_FIELDS =
      "SELECT sibd.batch_no, s.medicine_id, c.identification, p.medicine_name, " + " (CASE "
          + " WHEN c.issue_type='C' THEN 'CONSUMABLE' "
          + " WHEN c.issue_type='L' THEN 'REUSABLE' "
          + " WHEN c.issue_type='P' THEN 'PERMANENT' "
          + " WHEN c.issue_type='R' THEN 'RETAILABLE' "
          + " END) AS issue_type, s.dept_id, sibd.item_batch_id, "
          + " COALESCE(sibd.exp_dt, NULL) AS exp_dt, c.billable, "
          + " s.qty, p.package_type, p.issue_base_unit, c.category_id, "
          + " s.consignment_stock, p.issue_units, p.item_barcode_id, "
          + " p.package_uom, p.control_type_id, sict.control_type_name ";

  /** The Constant GET_ITEM_DETAILS_TABLES. */
  public static final String GET_ITEM_DETAILS_TABLES = " FROM "
      + " (SELECT medicine_id, SUM(qty) as qty, item_batch_id, batch_no, consignment_stock,"
      + " asset_approved, dept_id FROM store_stock_details "
      + " WHERE medicine_id = ? GROUP BY medicine_id, item_batch_id, batch_no, "
      + " consignment_stock, asset_approved, dept_id ORDER BY medicine_id) as s "
      + " JOIN store_item_batch_details sibd ON (sibd.item_batch_id = s.item_batch_id) "
      + " JOIN store_item_details p ON (p.medicine_id = s.medicine_id) "
      + " JOIN store_category_master c ON (c.category_id = p.med_category_id) "
      + " LEFT JOIN store_item_controltype sict ON (sict.control_type_id = p.control_type_id) ";

  /** The Constant GET_ITEM_DETAILS_WHERE. */
  public static final String GET_ITEM_DETAILS_WHERE = " WHERE s.asset_approved='Y' ";

  /**
   * This method is used to get stock details for medicine.
   *
   * @param storeBean             the store bean
   * @param medicineId            the medicine id
   * @param genericPreferanceBean the generic preferance bean
   * @return the item details
   */
  public List<BasicDynaBean> getItemDetails(BasicDynaBean storeBean, int medicineId,
      BasicDynaBean genericPreferanceBean) {
    StringBuilder query = new StringBuilder();

    query.append(GET_ITEM_DETAILS_FIELDS);
    query.append(GET_ITEM_DETAILS_TABLES);
    query.append(GET_ITEM_DETAILS_WHERE);

    if (genericPreferanceBean != null
        && genericPreferanceBean.get("stock_negative_sale") != null
        && ((String) genericPreferanceBean.get("stock_negative_sale")).equals("D")) {
      query.append(" AND S.qty>0 and s.medicine_id=? and dept_id=?  ");
    } else {
      query.append(" AND s.medicine_id=? AND dept_id=?  ");
    }

    return DatabaseHelper.queryToDynaList(query.toString(),
        new Object[] {medicineId, medicineId, (Integer) storeBean.get("dept_id")});
  }

  /** The Constant GET_SELLING_PRICE_EXPR. */
  private static final String GET_SELLING_PRICE_EXPR = "SELECT sid.medicine_id, ss.dept_id,"
      + " ss.use_batch_mrp, siir.issue_rate_expr as issue_rate_master_expr, visit_rate_plan_expr, "
      + " store_rate_plan_expr, sid.item_selling_price::text, ibd.mrp "
      + " FROM store_item_details sid "
      + " LEFT JOIN store_item_issue_rates siir ON (siir.medicine_id = sid.medicine_id) "
      + " LEFT JOIN ( "
      + "   SELECT visitsir.selling_price_expr as visit_rate_plan_expr,  visitsir.medicine_id "
      + "     FROM bill b "
      + "     LEFT JOIN organization_details od on (b.bill_rate_plan_id =  od.org_id) "
      + "     LEFT JOIN store_item_rates visitsir "
      + " ON (od.store_rate_plan_id = visitsir.store_rate_plan_id ) "
      + " WHERE b.bill_no = ? AND visitsir.medicine_id = ?) AS foo  ON"
      + " (foo.medicine_id = sid.medicine_id) " + " LEFT JOIN ( "
      + "      SELECT storesir.selling_price_expr as store_rate_plan_expr,"
      + " storesir.medicine_id, s.dept_id " + "   FROM stores s "
      + " JOIN store_item_rates storesir ON (storesir.store_rate_plan_id = s.store_rate_plan_id"
      + " AND storesir.medicine_id = ?)) AS foos "
      + " ON (foos.medicine_id = sid.medicine_id AND foos.dept_id = ?) "
      + " LEFT JOIN store_item_batch_details ibd on (ibd.medicine_id = sid.medicine_id) "
      + " CROSS JOIN stores ss "
      + " WHERE sid.medicine_id = ? AND ibd.item_batch_id = ? AND ss.dept_id = ? ";

  /**
   * Gets the selling price expr.
   *
   * @param medicineId the medicine id
   * @param itemBatchId the item batch id
   * @param billNo the bill no
   * @param storeId the store id
   * @return the selling price expr
   */
  public BasicDynaBean getSellingPriceExpr(int medicineId, int itemBatchId, String billNo,
      int storeId) {
    return DatabaseHelper.queryToDynaBean(GET_SELLING_PRICE_EXPR, billNo, medicineId,
        medicineId, storeId, medicineId, itemBatchId, storeId);
  }

  /** The Constant GET_PACKAGE_CP_DETAILS. */
  private static final String GET_PACKAGE_CP_DETAILS =
      " SELECT ild.item_batch_id," + " ild.item_lot_id, ssd.dept_id, "
          + " coalesce(ild.reference_package_cp, ild.package_cp) as package_cp, "
          + " MAX(ild.package_cp) OVER (PARTITION BY ild.item_batch_id), "
          + " AVG (ild.package_cp) OVER (PARTITION BY ild.item_batch_id) "
          + " FROM store_item_lot_details ild "
          + " JOIN store_stock_details ssd USING(item_lot_id) "
          + " WHERE ssd.dept_id = ? AND ild.item_batch_id = ? " + " ORDER BY ild.item_lot_id ";

  /**
   * Gets the package CP details.
   *
   * @param storeId the store id
   * @param itemBatchId the item batch id
   * @return the package CP details
   */
  public List<BasicDynaBean> getPackageCPDetails(Object storeId, Integer itemBatchId) {
    return DatabaseHelper.queryToDynaList(GET_PACKAGE_CP_DETAILS, storeId, itemBatchId);
  }

  /** The Constant MED_STOCK_WITH_PLAN_QUERY_BASE_FIELDS. */
  private static final String MED_STOCK_WITH_PLAN_QUERY_BASE_FIELDS =

      "SELECT  '' as route_id, '' as route_name, msd.qty,COALESCE(sir.tax_rate,"
          + "  COALESCE(ssir.tax_rate,m.tax_rate)) as tax_rate, sico.item_code,"
          + "  COALESCE(sir.tax_type,COALESCE(ssir.tax_type,m.tax_type)) as tax_type,"
          + "  msd.medicine_id,msd.qty_in_use,msd.qty_maint,msd.qty_retired,"
          + "  msd.qty_lost,msd.qty_kit,msd.qty_unknown,"
          + "  msd.consignment_stock,msd.asset_approved,msd.dept_id,"
          + "  m.medicine_name, mf.manf_code, mf.manf_name, mf.manf_mnemonic, "
          + "  m.issue_base_unit, m.package_type,COALESCE(isld.bin,m.bin) as bin, "
          + "  COALESCE(m.issue_units,'') AS issue_units, COALESCE(m.package_uom,'')"
          + " AS master_package_uom, " + " COALESCE(mc.discount,0) AS meddisc, "
          + "  mc.category, mc.category_id, mc.identification,"
          + "  sic.control_type_name, g.generic_name, "
          + "  '' as indent_no, mc.billable, mc.retailable, mc.claimable, m.item_barcode_id, "
          + "  m.insurance_category_id, m.billing_group_id, m.prior_auth_required,"
          + "  cum.consumption_uom, iic.insurance_payable, "
          + "  ipm.is_copay_pc_on_post_discnt_amt, ipd.patient_amount as patient_amount, "
          + "  ipd.patient_amount_per_category AS patient_amount_per_category,"
          + "  ipd.category_payable AS category_payable, mc.identification, "
          + "  ipd.patient_percent as patient_percent,"
          + "  ipd.patient_amount_cap as patient_amount_cap "
          + "  ,issue_rate_expr,sibd.mrp,sibd.batch_no,sibd.exp_dt,sibd.item_batch_id,"
          + "  sibd.mrp,m.cust_item_code, sir.selling_price_expr as visit_selling_expr,"
          + "  ssir.selling_price_expr as store_selling_expr,"
          + "  CASE WHEN cust_item_code IS NOT NULL AND  TRIM(cust_item_code) != ''"
          + "  THEN medicine_name||' - '||cust_item_code ELSE medicine_name END"
          + "  as cust_item_code_with_name ";

  /** The Constant MED_STOCK_WITH_PLAN_QUERY_STOCK_TABLES. */
  private static final String MED_STOCK_WITH_PLAN_QUERY_STOCK_TABLES = " FROM ( "
      + " SELECT medicine_id,sum(qty) as qty,item_batch_id,batch_no,sum(qty_in_use)"
      + " as qty_in_use,sum(qty_maint) as qty_maint, "
      + " sum(qty_retired) as qty_retired,sum(qty_lost) as qty_lost,sum(qty_kit) as qty_kit, "
      + " sum(qty_unknown) as qty_unknown,consignment_stock,asset_approved,dept_id,"
      + " min(tax) as tax "
      + " FROM store_stock_details  GROUP BY batch_no,item_batch_id,medicine_id,"
      + " consignment_stock,asset_approved,dept_id " + "   ORDER BY medicine_id) as msd ";

  /** The Constant MED_STOCK_WITH_PLAN_QUERY_BASE_JOIN_TABLES. */
  private static final String MED_STOCK_WITH_PLAN_QUERY_BASE_JOIN_TABLES = "  JOIN"
      + "  store_item_batch_details sibd USING(item_batch_id) "
      + "  JOIN store_item_details m ON(m.medicine_id = msd.medicine_id) "
      + "  JOIN manf_master mf ON (mf.manf_code = m.manf_name) "
      + "  JOIN store_category_master mc ON mc.category_id = m.med_category_id "
      + "  LEFT JOIN item_store_level_details isld ON isld.medicine_id = m.medicine_id @"
      + "  LEFT JOIN ha_item_code_type hict ON (hict.medicine_id = m.medicine_id"
      + "  AND hict.health_authority=?) "
      + "  LEFT JOIN store_item_codes sico ON (sico.medicine_id = m.medicine_id"
      + "  AND sico.code_type = hict.code_type) "
      + "  LEFT JOIN store_item_controltype sic ON sic.control_type_id=m.control_type_id "
      + "  LEFT JOIN generic_name g ON m.generic_name = g.generic_code "
      + "  LEFT JOIN insurance_plan_details ipd on"
      + "  (m.insurance_category_id = ipd.insurance_category_id "
      + "    AND ipd.patient_type=? AND ipd.plan_id=?) "
      + "  LEFT JOIN insurance_plan_main ipm on (ipm.plan_id = ipd.plan_id) "
      + "  JOIN item_insurance_categories iic ON"
      + "  (iic.insurance_category_id = m.insurance_category_id) "
      + "  LEFT JOIN store_item_rates sir ON (msd.medicine_id = sir.medicine_id AND "
      + "    sir.store_rate_plan_id = ?) "
      + "  LEFT JOIN store_item_rates ssir ON (msd.medicine_id = ssir.medicine_id AND "
      + "    ssir.store_rate_plan_id = ?) "
      + "  LEFT JOIN store_item_issue_rates siir ON (siir.medicine_id = msd.medicine_id)         "
      + "  LEFT JOIN consumption_uom_master cum ON (cum.cons_uom_id = m.cons_uom_id)"
      + " WHERE consignment_stock=false AND asset_approved = 'Y' # ";

  /** The Constant MED_STOCK_WITH_PLAN_QUERY_BASE_JOIN_TABLES_WITH_CONSIGNMENT. */
  private static final String MED_STOCK_WITH_PLAN_QUERY_BASE_JOIN_TABLES_WITH_CONSIGNMENT = "  JOIN"
      + " store_item_batch_details sibd USING(item_batch_id) "
      + "  JOIN store_item_details m ON(m.medicine_id = msd.medicine_id) "
      + "  JOIN manf_master mf ON (mf.manf_code = m.manf_name) "
      + "  JOIN store_category_master mc ON mc.category_id = m.med_category_id "
      + "  LEFT JOIN item_store_level_details isld ON isld.medicine_id = m.medicine_id @"
      + "  LEFT JOIN ha_item_code_type hict ON (hict.medicine_id = m.medicine_id"
      + "  AND hict.health_authority=?) "
      + "  LEFT JOIN store_item_codes sico ON (sico.medicine_id = m.medicine_id"
      + "  AND sico.code_type = hict.code_type) "
      + "  LEFT JOIN store_item_controltype sic ON sic.control_type_id=m.control_type_id "
      + "  LEFT JOIN generic_name g ON m.generic_name = g.generic_code "
      + "  LEFT JOIN insurance_plan_details ipd on"
      + "  (m.insurance_category_id = ipd.insurance_category_id "
      + "    AND ipd.patient_type=? AND ipd.plan_id=?) "
      + "  LEFT JOIN insurance_plan_main ipm on (ipm.plan_id = ipd.plan_id) "
      + "  JOIN item_insurance_categories iic ON"
      + "  (iic.insurance_category_id = m.insurance_category_id) "
      + "  LEFT JOIN store_item_rates sir ON (msd.medicine_id = sir.medicine_id AND "
      + "    sir.store_rate_plan_id = ?) "
      + "  LEFT JOIN store_item_rates ssir ON (msd.medicine_id = ssir.medicine_id AND "
      + "    ssir.store_rate_plan_id = ?) "
      + "  LEFT JOIN store_item_issue_rates siir ON (siir.medicine_id = msd.medicine_id)         "
      + "  LEFT JOIN consumption_uom_master cum ON (cum.cons_uom_id = m.cons_uom_id)"
      + " WHERE asset_approved = 'Y' # ";

  /** The Constant MED_STOCK_SELLING_PRICE_QUERY_BATCH_MRP_NO. */
  private static final String MED_STOCK_SELLING_PRICE_QUERY_BATCH_MRP_NO = ",COALESCE("
      + " m.item_selling_price, sibd.mrp) as selling_price,"
      + " COALESCE( m.item_selling_price, sibd.mrp) as orig_selling_price ";

  /** The Constant MED_STOCK_SELLING_PRICE_QUERY_BATCH_MRP_YES. */
  private static final String MED_STOCK_SELLING_PRICE_QUERY_BATCH_MRP_YES = ",COALESCE("
      + " m.item_selling_price, sibd.mrp) as selling_price,"
      + "COALESCE( m.item_selling_price, sibd.mrp) as orig_selling_price ";

  /** The Constant ORDER_BY_WITH_ZEROS_INDENT. */
  // sort order is based on which items we should be selling first.
  private static final String ORDER_BY_WITH_ZEROS_INDENT =
      " ORDER BY sign(msd.qty) DESC,exp_dt";

  /** The Constant WHERE_CLAUSE_WITHOUT_ZEROS_INDENT. */
  private static final String WHERE_CLAUSE_WITHOUT_ZEROS_INDENT =
      "  AND msd.qty > 0 AND asset_approved='Y' ";

  /** The Constant ORDER_BY_WITHOUT_ZEROS_INDENT. */
  private static final String ORDER_BY_WITHOUT_ZEROS_INDENT = " ORDER BY exp_dt";

  /** The Constant ORDER_BY_WITH_ZEROS. */
  // sort order is based on which items we should be selling first.
  private static final String ORDER_BY_WITH_ZEROS = " ORDER BY sign(msd.qty) DESC,exp_dt";

  /** The Constant WHERE_CLAUSE_WITHOUT_ZEROS. */
  private static final String WHERE_CLAUSE_WITHOUT_ZEROS = "  AND msd.qty > 0 AND"
      + " asset_approved='Y' ";

  /** The Constant ORDER_BY_WITHOUT_ZEROS. */
  private static final String ORDER_BY_WITHOUT_ZEROS = " ORDER BY exp_dt";

  /**
   * Gets the medicine stock with pat amts in dept.
   *
   * @param medicineIds          the medicine ids
   * @param deptId               the dept id
   * @param planId               the plan id
   * @param visitType            the visit type
   * @param includeZeroStock     the include zero stock
   * @param visitStoreRatePlanId the visit store rate plan id
   * @param healthAuthority      the health authority
   * @param storeRatePlanId      the store rate plan id
   * @param useBatchMRP          the use batch MRP
   * @return the medicine stock with pat amts in dept
   */
  public List<BasicDynaBean> getMedicineStockWithPatAmtsInDept(List<Integer> medicineIds,
      int deptId, int planId, String visitType, boolean includeZeroStock,
      int visitStoreRatePlanId, String healthAuthority, int storeRatePlanId,
      boolean useBatchMRP) {
    StringBuilder query = new StringBuilder(MED_STOCK_WITH_PLAN_QUERY_BASE_FIELDS);

    query.append(useBatchMRP ? MED_STOCK_SELLING_PRICE_QUERY_BATCH_MRP_YES
        : MED_STOCK_SELLING_PRICE_QUERY_BATCH_MRP_NO);
    query.append(MED_STOCK_WITH_PLAN_QUERY_STOCK_TABLES);
    query.append(
        (MED_STOCK_WITH_PLAN_QUERY_BASE_JOIN_TABLES.replace("#", " AND msd.dept_id=? "))
            .replace("@", " AND isld.dept_id=" + deptId + " "));

    QueryBuilder.addWhereFieldOpValue(true, query, "msd.medicine_id", "IN", medicineIds);
    if (includeZeroStock) {
      query.append(ORDER_BY_WITH_ZEROS);
    } else {
      query.append(WHERE_CLAUSE_WITHOUT_ZEROS + ORDER_BY_WITHOUT_ZEROS);
    }

    logger.debug("Stock query: " + query.toString() + " size: " + medicineIds.size());
    Object[] params = new Object[6 + medicineIds.size()];
    int paramIndex = 0;
    params[paramIndex++] = healthAuthority;
    params[paramIndex++] = visitType;
    params[paramIndex++] = planId;
    params[paramIndex++] = visitStoreRatePlanId;
    params[paramIndex++] = storeRatePlanId;
    params[paramIndex++] = deptId;

    for (int medId : medicineIds) {
      params[paramIndex++] = medId;
    }
    return DatabaseHelper.queryToDynaList(query.toString(), params);
  }

  /**
   * Gets the order kit medicine stock with pat amts in dept.
   *
   * @param medicineIds          the medicine ids
   * @param deptId               the dept id
   * @param planId               the plan id
   * @param visitType            the visit type
   * @param includeZeroStock     the include zero stock
   * @param visitStoreRatePlanId the visit store rate plan id
   * @param healthAuthority      the health authority
   * @param storeRatePlanId      the store rate plan id
   * @param useBatchMRP          the use batch MRP
   * @return the order kit medicine stock with pat amts in dept
   */
  public List<BasicDynaBean> getOrderKitMedicineStockWithPatAmtsInDept(
      List<Integer> medicineIds, int deptId, int planId, String visitType,
      boolean includeZeroStock, int visitStoreRatePlanId, String healthAuthority,
      int storeRatePlanId, boolean useBatchMRP) {

    StringBuilder query = new StringBuilder(MED_STOCK_WITH_PLAN_QUERY_BASE_FIELDS);

    query.append(useBatchMRP ? MED_STOCK_SELLING_PRICE_QUERY_BATCH_MRP_YES
        : MED_STOCK_SELLING_PRICE_QUERY_BATCH_MRP_NO);
    query.append(MED_STOCK_WITH_PLAN_QUERY_STOCK_TABLES);
    query.append((MED_STOCK_WITH_PLAN_QUERY_BASE_JOIN_TABLES_WITH_CONSIGNMENT.replace("#",
        " AND msd.dept_id=? ")).replace("@", " AND isld.dept_id=" + deptId + " "));

    QueryBuilder.addWhereFieldOpValue(true, query, "msd.medicine_id", "IN", medicineIds);
    if (includeZeroStock) {
      query.append(ORDER_BY_WITH_ZEROS);
    } else {
      query.append(WHERE_CLAUSE_WITHOUT_ZEROS + ORDER_BY_WITHOUT_ZEROS);
    }
    logger.debug("Stock query: " + query.toString() + " size: " + medicineIds.size());
    Object[] params = new Object[6 + medicineIds.size()];
    int paramIndex = 0;
    params[paramIndex++] = healthAuthority;
    params[paramIndex++] = visitType;
    params[paramIndex++] = planId;
    params[paramIndex++] = visitStoreRatePlanId;
    params[paramIndex++] = storeRatePlanId;
    params[paramIndex++] = deptId;

    for (int medId : medicineIds) {
      params[paramIndex++] = medId;
    }
    return DatabaseHelper.queryToDynaList(query.toString(), params);
  }

  /** The Constant GET_ISSUE_ITEMS. */
  public static final String GET_ISSUE_ITEMS = " select sim.user_issue_no,"
      + " sim.username as user_name, sim.date_time::date as issue_date, s.dept_name, "
      + " medicine_name, sibd.batch_no, sid.qty, reference as remarks, sim.gatepass_id,"
      + " sim.user_type, sim.issued_to, "
      + " coalesce(sm.salutation||' '||pd.patient_name||' '||pd.middle_name||' '||pd.last_name,'')"
      + " as patient_name, " + " round((sid.amount*sid.qty),2) as amount, m.issue_units, "
      + " case when scm.billable='t' then 'true' else 'false' end as billable, "
      + " case when issue_type='P' then 'Permanent' when issue_type='C' then 'Consumable' "
      + " when issue_type='R'  then 'Retailable' else 'Reusable' end as issue_type, "
      + " case when consignment_stock='t' then 'Consignment' else 'Normal' end as stocktype,"
      + " sid.indent_no, " + " date(indent.date_time) as indent_date,m.cust_item_code "
      + " from stock_issue_main " + " sim  "
      + " left join stock_issue_details sid on sid.user_issue_no = sim.user_issue_no "
      + " LEFT JOIN store_item_batch_details sibd USING(item_batch_id)"
      + " left outer join patient_details pd on pd.mr_no=sim.issued_to  "
      + " left join store_item_details m on m.medicine_id = sid.medicine_id "
      + " join store_category_master scm ON (scm.category_id = m.med_category_id) "
      + " left join stores s on s.dept_id = sim.dept_from  "
      + " left join store_transaction_lot_details stld "
      + " on (stld.transaction_type = 'U' and stld.transaction_id = sid.item_issue_no) "
      + " join store_stock_details ssd on (ssd.dept_id=dept_from and"
      + " ssd.medicine_id=sid.medicine_id and sid.batch_no=ssd.batch_no"
      + " and stld.item_lot_id = ssd.item_lot_id) "
      + " left join patient_registration pr on (pd.mr_no=pr.mr_no and pr.status='A')  "
      + " left join salutation_master sm on (sm.salutation_id = pd.salutation)  "
      + " left join store_indent_main indent on (indent.indent_no= sid.indent_no) "
      + " WHERE sim.user_issue_no=? AND"
      + " ( patient_confidentiality_check(pd.patient_group,pd.mr_no) )";

  /**
   * Gets the issued item list.
   *
   * @param issueNo the issue no
   * @return the issued item list
   */
  public List<BasicDynaBean> getIssuedItemList(Integer issueNo) {
    return DatabaseHelper.queryToDynaList(GET_ISSUE_ITEMS, new Object[] {issueNo});
  }

  private static final String GET_INV_DETAILS = " SELECT supplier_id, invoice_no, invoice_date,"
      + " due_date, po_no, po_reference, discount, "
      + " round_off, si.status, payment_id, discount_type, discount_per, date_time, other_charges, "
      + " other_charges_remarks, remarks, paid_date, payment_remarks, cess_tax_rate, cess_tax_amt, "
      + " tax_name, cst_rate, account_group, supplier_invoice_id, consignment_stock, debit_amt, "
      + " cash_purchase, invoice_file_name, invoice_contenttype, "
      + " sm.supplier_name,sm.cust_supplier_code, sm.supplier_address, company_name,"
      + " means_of_transport, " + " consignment_no, consignment_date,si.transportation_charges "
      + " FROM store_invoice si "
      + " JOIN supplier_master sm ON (sm.supplier_code = si.supplier_id) "
      + " WHERE supplier_invoice_id=?";

  public static BasicDynaBean getInvDetails(int suppInvId) {
    return DatabaseHelper.queryToDynaBean(GET_INV_DETAILS, new Object[] { suppInvId });
  }

  private static final String GRN_SEQUENCE_PATTERN = " SELECT pattern_id FROM hosp_grn_seq_prefs "
      + " WHERE priority = ( " + "  SELECT min(priority) FROM hosp_grn_seq_prefs "
      + "  WHERE (store_id=? or store_id='*')  " + " ) #";

  /**
   * Gets the next id.
   *
   * @param storeId the store id
   * @param cashPurchase the cash purchase
   * @return the next id
   */
  public String getNextId(String storeId, String cashPurchase) {
    BasicDynaBean bean = null;
    if (cashPurchase != null) {
      bean = DatabaseHelper.queryToDynaBean(
          GRN_SEQUENCE_PATTERN.replace("#", " AND pattern_type = 'cash'"), storeId);
    }

    if (bean == null) {
      // it means that seperate sequence is not needed for cash purchase,lets use existing pattern
      bean = DatabaseHelper
          .queryToDynaBean(GRN_SEQUENCE_PATTERN.replace("#", " AND pattern_type IS NULL"), storeId);
    }
    String patternId = (String) bean.get("pattern_id");
    return DatabaseHelper.getNextPatternId(patternId);
  }

  private static final String PACKAGE_STOCK_IN_STORE_BASE_FIELDS =
      "SELECT  '' as route_id, '' as route_name, msd.qty, m.tax_rate as tax_rate, sico.item_code,"
      + " m.tax_type as tax_type,  msd.medicine_id,msd.qty_in_use,msd.qty_maint,msd.qty_retired,"
      + " msd.qty_lost,msd.qty_kit,msd.qty_unknown,  msd.consignment_stock,msd.asset_approved,"
      + " msd.dept_id,  m.medicine_name, mf.manf_code, mf.manf_name, mf.manf_mnemonic,"
      + " m.issue_base_unit, m.package_type, m.bin as bin, COALESCE(m.issue_units,'') AS"
      + " issue_units, COALESCE(m.package_uom,'') AS master_package_uom, COALESCE(mc.discount,0)"
      + " AS meddisc, mc.category, mc.category_id, mc.identification, sic.control_type_name,"
      + " g.generic_name, mc.billable, mc.retailable, mc.claimable, m.item_barcode_id,"
      + " m.insurance_category_id, m.billing_group_id, m.prior_auth_required,  cum.consumption_uom,"
      + " iic.insurance_payable, ipm.is_copay_pc_on_post_discnt_amt, ipd.patient_amount as"
      + " patient_amount, ipd.patient_amount_per_category AS patient_amount_per_category,"
      + " ipd.category_payable AS category_payable, mc.identification,"
      + " ipd.patient_percent as patient_percent, ipd.patient_amount_cap as patient_amount_cap,"
      + " issue_rate_expr, sibd.mrp, sibd.batch_no, sibd.exp_dt, sibd.item_batch_id,"
      + " m.cust_item_code, CASE WHEN cust_item_code IS NOT NULL AND  TRIM(cust_item_code) != ''"
      + " THEN medicine_name||' - '||cust_item_code ELSE medicine_name END "
      + " as cust_item_code_with_name";

  private static final String PACKAGE_STOCK_IN_STORE_BASE_JOIN_TABLES = "  JOIN"
      + "  store_item_batch_details sibd USING(item_batch_id) "
      + "  JOIN store_item_details m ON(m.medicine_id = msd.medicine_id) "
      + "  JOIN manf_master mf ON (mf.manf_code = m.manf_name) "
      + "  JOIN store_category_master mc ON mc.category_id = m.med_category_id "
      + "  LEFT JOIN ha_item_code_type hict ON (hict.medicine_id = m.medicine_id"
      + "  AND hict.health_authority=?) "
      + "  LEFT JOIN store_item_codes sico ON (sico.medicine_id = m.medicine_id"
      + "  AND sico.code_type = hict.code_type) "
      + "  LEFT JOIN store_item_controltype sic ON sic.control_type_id=m.control_type_id "
      + "  LEFT JOIN generic_name g ON m.generic_name = g.generic_code "
      + "  LEFT JOIN insurance_plan_details ipd on"
      + "  (m.insurance_category_id = ipd.insurance_category_id "
      + "    AND ipd.patient_type=? AND ipd.plan_id=?) "
      + "  LEFT JOIN insurance_plan_main ipm on (ipm.plan_id = ipd.plan_id) "
      + "  JOIN item_insurance_categories iic ON"
      + "  (iic.insurance_category_id = m.insurance_category_id) "
      + "  LEFT JOIN store_item_issue_rates siir ON (siir.medicine_id = msd.medicine_id) "
      + "  LEFT JOIN consumption_uom_master cum ON (cum.cons_uom_id = m.cons_uom_id)"
      + " WHERE consignment_stock=false AND asset_approved = 'Y'  AND msd.dept_id=? ";

  /**
   * Gets the package medicine stock in store.
   *
   * @param medicineIds the medicine ids
   * @param storeId the store id
   * @param planId the plan id
   * @param visitType the visit type
   * @param healthAuthority the health authority
   * @return the package medicine stock in store
   */
  public List<BasicDynaBean> getPackageMedicineStockInStore(List<Integer> medicineIds,
      int storeId, int planId, String visitType, String healthAuthority) {
    StringBuilder query = new StringBuilder(PACKAGE_STOCK_IN_STORE_BASE_FIELDS);

    query.append(MED_STOCK_SELLING_PRICE_QUERY_BATCH_MRP_NO);
    query.append(MED_STOCK_WITH_PLAN_QUERY_STOCK_TABLES);
    query.append(PACKAGE_STOCK_IN_STORE_BASE_JOIN_TABLES);


    QueryBuilder.addWhereFieldOpValue(true, query, "msd.medicine_id", "IN", medicineIds);
    query.append(WHERE_CLAUSE_WITHOUT_ZEROS + ORDER_BY_WITHOUT_ZEROS);

    logger.debug("Package Stock query: " + query.toString() + " size: " + medicineIds.size());
    Object[] params = new Object[4 + medicineIds.size()];
    int paramIndex = 0;
    params[paramIndex++] = healthAuthority;
    params[paramIndex++] = visitType;
    params[paramIndex++] = planId;
    params[paramIndex++] = storeId;

    for (int medId : medicineIds) {
      params[paramIndex++] = medId;
    }
    return DatabaseHelper.queryToDynaList(query.toString(), params);

  }

  private static final String GET_MEDICINE_NAME = "SELECT medicine_id, medicine_name FROM "
      + "store_item_details WHERE medicine_id IN (:medicineIds)";

  /**
   * Get medicine names by medicine ids.
   * @param medicineIds medicine ids
   * @return List of BasicDynaBean
   */
  public List<BasicDynaBean> getMedicineNamesByMedicineIds(List<Integer> medicineIds) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("medicineIds", medicineIds);
    return DatabaseHelper.queryToDynaList(GET_MEDICINE_NAME, parameters);
  }
}
