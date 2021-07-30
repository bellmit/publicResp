package com.insta.hms.core.inventory.stockmgmt;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.QueryAssembler;
import com.insta.hms.common.SearchQueryAssembler;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class StockTakeRepository extends GenericRepository {

  private static final Logger logger = LoggerFactory
      .getLogger(StockTakeRepository.class);

  public StockTakeRepository() {
    super("physical_stock_take");
  }

  private static final String LOOKUP_ORDER = " ORDER BY stock_take_id ";

  private static final String LOOKUP_QUERY = "SELECT "
      + " s.center_id, pst.store_id, s.dept_name as store_name, "
      + " pst.stock_take_id, pst.status "
      + " FROM physical_stock_take pst JOIN stores s "
      + "   ON (pst.store_id = s.dept_id) " ;

  private static final String FILTERED_LOOKUP_QUERY = LOOKUP_QUERY 
      + " WHERE stock_take_id ILIKE ? " ;

  /**
   * Gets a list of stock take records with the matching search text.
   * @param searchText search string to match the stock take number with.
   * @return List of beans matching the search string.
   */
  public List<BasicDynaBean> lookup(String searchText) {
    if (null != searchText && !searchText.isEmpty()) {
      return DatabaseHelper.queryToDynaList(
          FILTERED_LOOKUP_QUERY + LOOKUP_ORDER, new Object[] { "%" + searchText + "%" });
    } else {
      return DatabaseHelper.queryToDynaList(LOOKUP_QUERY + LOOKUP_ORDER);
    }
  }

  private static final String SEARCH_FIELDS = "SELECT "
      + " stock_take_id, center_id, store_id, store_name, user_name,"
      + " initiated_datetime, status, COUNT(item_batch_id) as total_count, "
      + " COUNT(counted_batch_id) as completed_count, "
      + " round(coalesce(SUM(abs(coalesce(physical_stock_qty, 0.00) - "
      + "   coalesce(system_stock_qty, 0.00))) * 100 / "
      + " SUM((NULLIF(system_stock_qty, 0.00))), 0.00), 2) as variance_perc, "
      + " round(coalesce(SUM(item_cost_value), 0.00), 2) as total_cost_value, "
      + " round(coalesce(SUM(item_cost_value * (coalesce(physical_stock_qty, 0.00) - "
      + "   coalesce(system_stock_qty, 0.00)) / NULLIF(item_qty, 0.00)), 0.00), 2) "
      + " as cost_value ";

  private static final String SEARCH_TABLES = " FROM ( SELECT "
      + " pst.stock_take_id, s.center_id, pst.store_id, "
      + " pst.inactive_item_excl_dt, "
      + " s.dept_name as store_name, "
      + " pst.user_name, pst.initiated_datetime::date, pst.status, "
      + " ssd.item_batch_id as item_batch_id, sibd.exp_dt, "
      + " pstd.item_batch_id as counted_batch_id,"
      + " pstd.physical_stock_qty, pstd.system_stock_qty,"
      + " SUM(ssd.qty) as item_qty, "
      + " SUM(ssd.qty * sild.package_cp / coalesce(sid.issue_base_unit, 1)) "
      + "   as item_cost_value"
      + " FROM physical_stock_take pst "
      + " JOIN stores s ON (pst.store_id = s.dept_id) "
      + " LEFT JOIN store_stock_details ssd USING (dept_id)"
      + " LEFT JOIN store_item_batch_details sibd USING (item_batch_id)"
      + " LEFT JOIN store_item_lot_details sild USING (item_lot_id)"
      + " LEFT JOIN store_item_details sid ON (sid.medicine_id = ssd.medicine_id)"
      + " LEFT JOIN physical_stock_take_detail pstd "
      + "     ON (pstd.stock_take_id = pst.stock_take_id "
      + "       AND pstd.item_batch_id = ssd.item_batch_id) "
      + " GROUP BY pst.stock_take_id, s.center_id, pst.store_id, "
      + "   pst.inactive_item_excl_dt, store_name, "
      + "   pst.user_name, pst.initiated_datetime::date, pst.status, "
      + "   ssd.item_batch_id, sibd.exp_dt, counted_batch_id, "
      + "   pstd.physical_stock_qty, "
      + "   pstd.system_stock_qty) as foo ";

  private static final String EXPIRY_WHERE_CLAUSE = " WHERE "
      + " (inactive_item_excl_dt IS NULL "
      + "   OR exp_dt IS NULL OR item_qty > 0 "
      + "   OR exp_dt >= inactive_item_excl_dt) ";

  private static final String OUTER_GROUP_BY = " stock_take_id, center_id, "
      + " store_id, store_name, user_name, initiated_datetime, status ";

  private static final String SEARCH_COUNT = " SELECT "
      + " COUNT(distinct stock_take_id) ";

  private static final int DEFAULT_PAGE_SIZE = 250;
  private static final int DEFAULT_PAGE_NUM = 1;

  /**
   * Searches stock take records matching the search filter criteria.
   * 
   * @param searchParams
   *          - search filter parameters
   * @return PagedList - search result
   */
  @Override
  public PagedList search(Map searchParams) {
    return search(searchParams, null);
  }

  /**
   * Searches stock take records matching the search filter criteria within a
   * list of stores. Useful when the search results have to be restricted to
   * user assigned stores.
   * 
   * @param searchParams
   *          - search filter parameters
   * @return PagedList - search result
   */
  public PagedList search(Map searchParams, List userStores) {
    SearchQueryAssembler queryAssembler = new SearchQueryAssembler(
        SEARCH_FIELDS, SEARCH_COUNT, SEARCH_TABLES, EXPIRY_WHERE_CLAUSE, OUTER_GROUP_BY,
        null, false, DEFAULT_PAGE_SIZE, DEFAULT_PAGE_NUM);
    queryAssembler.addFilterFromParamMap(searchParams);
    if (null != userStores) {
      queryAssembler.addFilter(QueryAssembler.INTEGER, "store_id", "IN", userStores);
    }
    queryAssembler.addSecondarySort("stock_take_id", true);
    queryAssembler.build();
    //logger.error(queryAssembler.getDataQueryString());
    return queryAssembler.getMappedPagedList();
  }

  // This outer query rolls up the totals of cost and variance for
  // all items for the stock take. The weighted average cost of an item
  // batch is applied to the difference in physical and stock quantity
  // to arrive at the cost value corresponding to the variance
  private static final String SUMMARY_FIELDS_FOR_APPROVAL = "SELECT "
      + " stock_take_id, store_name, COUNT(item_batch_id) as total_count, "
      + " COUNT(counted_batch_id) as completed_count, "
      + " round(coalesce(SUM(abs(coalesce(physical_stock_qty, 0.00) - "
      + "   coalesce(system_stock_qty, 0.00))) * 100 / "
      + "   SUM(NULLIF(system_stock_qty, 0.00)), 0.00), 2) as variance_perc, "
      + " round(coalesce(SUM(item_cost_value), 0.00), 2) as total_cost_value, "
      + " round(coalesce(SUM(item_cost_value * (coalesce(physical_stock_qty, 0.00) - "
      + "   coalesce(system_stock_qty, 0.00)) / NULLIF(item_qty, 0.00)), 0.00), 2) "
      + " as cost_value ";
  private static final String SUMMARY_FIELDS_FOR_COUNT = "SELECT "
      + " stock_take_id, store_name, COUNT(item_batch_id) as total_count, "
      + " COUNT(counted_batch_id) as completed_count, "
      + " 0.00 as variance_perc, "
      + " 0.00 as total_cost_value, "
      + " 0.00 as cost_value ";
  // This inner query aggregates the qty and cost per item batch
  // in the store_stock_details, along with the system  physical
  // stock quantities recorded as part of a given stock take
  private static final String SUMMARY_TABLES = " FROM ( SELECT "
      + " pst.stock_take_id, s.dept_name as store_name,"
      + " ssd.item_batch_id as item_batch_id,"
      + " pstd.item_batch_id as counted_batch_id,"
      + " pstd.physical_stock_qty, pstd.system_stock_qty,"
      + " SUM(ssd.qty) as item_qty, "
      + " SUM(ssd.qty * sild.package_cp) as item_cost_value"
      + " FROM physical_stock_take pst"
      + "   JOIN stores s ON (pst.store_id = s.dept_id)"
      + "   LEFT JOIN store_stock_details ssd USING (dept_id)"
      + "   LEFT JOIN store_item_lot_details sild USING (item_lot_id)"
      + "   LEFT JOIN physical_stock_take_detail pstd "
      + "     ON (pstd.stock_take_id = pst.stock_take_id "
      + "       AND pstd.item_batch_id = ssd.item_batch_id) "
      + " WHERE pst.stock_take_id = ? "
      + " GROUP BY pst.stock_take_id, store_name, "
      + "   ssd.item_batch_id, counted_batch_id, pstd.physical_stock_qty, "
      + "   pstd.system_stock_qty) AS foo GROUP BY stock_take_id, store_name ";


  private static final String COUNT_SUMMARY_QUERY = SUMMARY_FIELDS_FOR_COUNT
      + SUMMARY_TABLES ;

  private static final String APPROVE_SUMMARY_QUERY = SUMMARY_FIELDS_FOR_APPROVAL
      + SUMMARY_TABLES ;

  /**
   * Fetches stock take summary details.
   * 
   * @param stockTakeId
   *          the stock take id
   * @param includeCostDetails
   *          specifies whether the summary should include the cost value and
   *          variance details
   * @return BasicDynaBean summary for the given stock take
   */
  public BasicDynaBean getStockTakeSummary(String stockTakeId,
      boolean includeCostDetails) {
    return DatabaseHelper.queryToDynaBean(
        includeCostDetails ? APPROVE_SUMMARY_QUERY : COUNT_SUMMARY_QUERY,
        new Object[] { stockTakeId });
  }

  private static final String STOCK_TAKE_STOCK_QUANTITIES_QUERY = "SELECT "
      + " pst.store_id, pst.stock_take_id, ssd.item_batch_id, "
      + " pstd.physical_stock_qty, "
      + " pstd.system_stock_qty as recorded_system_stock_qty, "
      + " pstd.recorded_datetime, "
      + " SUM(ssd.qty)::numeric(10,4) as current_system_stock_qty "
      + " FROM store_stock_details ssd "
      + " JOIN physical_stock_take pst ON (pst.store_id = ssd.dept_id) "
      + " LEFT JOIN physical_stock_take_detail pstd "
      + "   ON (pstd.item_batch_id = ssd.item_batch_id "
      + "   AND pstd.stock_take_id = pst.stock_take_id) "
      + " WHERE pst.stock_take_id = ? "
      + " GROUP BY pst.store_id, pst.stock_take_id, ssd.item_batch_id, "
      + "   pstd.physical_stock_qty, pstd.system_stock_qty, pstd.recorded_datetime ";
  
  public List<BasicDynaBean> getStockQuantities(String stockTakeId) {
    return DatabaseHelper.queryToDynaList(STOCK_TAKE_STOCK_QUANTITIES_QUERY,
        new Object[] { stockTakeId });
  }

  private static final String STOCK_TAKE_UNRECONCILED_ITEMS_QUERY = "SELECT * "
      + " FROM physical_stock_take_detail pstd "
      + " WHERE stock_take_id = ? AND "
      + "   coalesce(physical_stock_qty, 0.00) != coalesce(system_stock_qty, 0.00) "
      + "   AND stock_adjustment_reason_id IS NULL ";

  /**
   * Get a list of items for which have a variance but reconciliation remarks
   * has not yet been entered.
   * 
   * @param stockTakeId
   *          Stock take Id
   * @return list of beans for which reconciliation remarks is not entered.
   */
  public List<BasicDynaBean> getUnreconciledItems(String stockTakeId) {
    return DatabaseHelper.queryToDynaList(STOCK_TAKE_UNRECONCILED_ITEMS_QUERY,
        new Object[] { stockTakeId });
  }

  private static final String UNCOUNTED_ITEMS_QUERY = " SELECT * "
      + " FROM (SELECT "
      + " pst.store_id, pst.stock_take_id, pst.inactive_item_excl_dt, "
      + " ssd.item_batch_id, sibd.exp_dt, "
      + " SUM(ssd.qty) as system_stock_qty " 
      + " FROM store_stock_details ssd "
      + " JOIN physical_stock_take pst ON (ssd.dept_id = pst.store_id) "
      + " JOIN store_item_batch_details sibd "
      + "   ON (sibd.item_batch_id = ssd.item_batch_id) "
      + " LEFT JOIN physical_stock_take_detail pstd "
      + " ON (pst.stock_take_id = pstd.stock_take_id AND "
      + "   pstd.item_batch_id = ssd.item_batch_id) "
      + " WHERE pst.stock_take_id = ? AND pstd.physical_stock_qty IS NULL "
      + " GROUP BY pst.store_id, pst.stock_take_id, pst.inactive_item_excl_dt, "
      + "   ssd.item_batch_id, sibd.exp_dt) AS FOO "
      + " WHERE (inactive_item_excl_dt IS NULL "
      + "   OR exp_dt IS NULL OR system_stock_qty > 0 "
      + "   OR exp_dt >= inactive_item_excl_dt) ";

  /**
   * Get a list of items for which physical stock quantity has not yet been
   * entered.
   * 
   * @param stockTakeId
   *          Stock take Id
   * @return list of beans for which physical stock qty is not entered.
   */
  public List<BasicDynaBean> getUncountedItems(String stockTakeId) {
    return DatabaseHelper.queryToDynaList(UNCOUNTED_ITEMS_QUERY,
        new Object[] { stockTakeId });
  }

  private static final String UNADJUSTED_ITEMS_QUERY = "SELECT sibd.batch_no, "
      + " round(pstd.physical_stock_qty - pstd.system_stock_qty, 0) as qty, "
      + " sid.medicine_id, "
      + " substring(sarm.adjustment_reason, 1, 100) as description,"
      + " pstd.item_batch_id " + " FROM physical_stock_take_detail pstd "
      + " JOIN store_item_batch_details sibd USING (item_batch_id) "
      + " JOIN store_item_details sid USING (medicine_id)   "
      + " JOIN stock_adjustment_reason_master sarm "
      + "   ON (sarm.adjustment_reason_id = pstd.stock_adjustment_reason_id) "
      + " WHERE pstd.physical_stock_qty != pstd.system_stock_qty "
      + "   AND pstd.stock_take_id = ? ";

  public List<BasicDynaBean> getUnadjustedItems(String stockTakeId) {
    return DatabaseHelper.queryToDynaList(UNADJUSTED_ITEMS_QUERY,
        new Object[] { stockTakeId });
  }
}
