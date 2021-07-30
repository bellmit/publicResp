package com.insta.hms.core.inventory.stockmgmt;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericRepository;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryAssembler;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public class StockTakeDetailRepository extends GenericRepository {

  private static final String TABLE_NAME = "physical_stock_take_detail";

  public StockTakeDetailRepository() {
    super(TABLE_NAME);
  }

  private static final String SEARCH_SELECT = "SELECT * ";

  private static final String SEARCH_COLUMNS_FOR_APPROVAL = " FROM (SELECT "
      + " pstd.stock_take_id, pst.store_id, s.dept_name as store_name, "
      + " pstd.stock_take_detail_id, sid.medicine_id, sid.medicine_name, "
      + " pstd.item_batch_id, "
      + " sid.cust_item_code, sid.bin, sid.med_category_id, "
      + " sid.manf_name as manf_code, "
      + " sg.service_group_id, sid.service_sub_group_id, sibd.batch_no, "
      + " pstd.stock_adjustment_reason_id, "
      + " sarm.adjustment_reason, pstd.physical_stock_qty, "
      + " pstd.physical_stock_qty as _current_physical_stock_qty, "
      + " pstd.system_stock_qty, "
      + " pstd.physical_stock_qty - pstd.system_stock_qty as variance "
      + " FROM physical_stock_take_detail pstd "
      + " JOIN physical_stock_take pst USING (stock_take_id) "
      + " JOIN store_item_batch_details sibd USING (item_batch_id) "
      + " JOIN store_item_details sid USING (medicine_id) "
      + " LEFT JOIN service_sub_groups ssg USING (service_sub_group_id) "
      + " LEFT JOIN service_groups sg USING (service_group_id)"
      + " JOIN stores s ON (pst.store_id = s.dept_id) "
      + " LEFT JOIN stock_adjustment_reason_master sarm "
      + "   ON (pstd.stock_adjustment_reason_id = sarm.adjustment_reason_id)) "
      + " AS foo ";

  private static final String SEARCH_COLUMNS_FOR_COUNTING = " FROM "
      + " ( SELECT DISTINCT "
      + " pst.stock_take_id, pst.store_id, s.dept_name as store_name, "
      + " pstd.stock_take_detail_id, sid.medicine_id, sid.medicine_name, "
      + " sibd.item_batch_id, "
      + " sid.cust_item_code, sid.bin, sid.med_category_id, "
      + " sid.manf_name as manf_code, "
      + " sg.service_group_id, sid.service_sub_group_id, sibd.batch_no, "
      + " pstd.stock_adjustment_reason_id, "
      + " sarm.adjustment_reason, pstd.physical_stock_qty, "
      + " pstd.physical_stock_qty as _current_physical_stock_qty "
      + " FROM store_stock_details ssd "
      + " JOIN store_item_batch_details sibd USING (item_batch_id) "
      + " JOIN store_item_details sid ON (sid.medicine_id = sibd.medicine_id) "
      + " JOIN physical_stock_take pst ON (ssd.dept_id = pst.store_id) "
      + " JOIN stores s ON (pst.store_id = s.dept_id) "
      + " LEFT JOIN physical_stock_take_detail pstd "
      + "   ON (pstd.stock_take_id = pst.stock_take_id "
      + "     AND pstd.item_batch_id = ssd.item_batch_id) "
      + " LEFT JOIN service_sub_groups ssg USING (service_sub_group_id) "
      + " LEFT JOIN service_groups sg USING (service_group_id)"
      + " LEFT JOIN stock_adjustment_reason_master sarm "
      + "   ON (pstd.stock_adjustment_reason_id = sarm.adjustment_reason_id) "
      + " WHERE (pst.inactive_item_excl_dt IS NULL OR sibd.exp_dt IS NULL "
      + "   OR ssd.qty > 0 OR sibd.exp_dt >= pst.inactive_item_excl_dt) ) "
      + " AS foo ";

  private static final String SEARCH_COUNT = "SELECT "
      + " COUNT( distinct stock_take_detail_id ) ";

  /**
   * Searches for records matching the filter criteria.
   * 
   * @param searchParams
   *          Filter criteria for the search
   * @param includeSystemStock
   *          flag to indicate whether system stock should be returned or not in
   *          the result set
   * @return PagedList of stock take item records
   */
  public PagedList search(Map searchParams, boolean includeSystemStock) {

    Map<LISTING, Object> listing = ConversionUtils
        .getListingParameter(searchParams);
    if (searchParams.containsKey("_records")) {
      listing.put(LISTING.PAGENUM, 0);
      listing.put(LISTING.PAGESIZE, 0);
    }

    SearchQueryAssembler queryAssembler = new SearchQueryAssembler(
        SEARCH_SELECT, SEARCH_COUNT,
        (includeSystemStock) ? SEARCH_COLUMNS_FOR_APPROVAL
            : SEARCH_COLUMNS_FOR_COUNTING, listing) {

      private Integer limit = 20;
      private Integer offset = 0;

      @Override
      public String getDataQueryString() {
        String dataQueryString = super.getDataQueryString();

        // super has not added limit, need one
        if (pageSize == 0 && limit != 0) {
          dataQueryString += " LIMIT " + limit;
        }

        // super has not added offset and we need one
        if (pageNum == 0 && offset >= 0) {
          dataQueryString += " OFFSET " + offset;
        }

        return dataQueryString;
      }

      @Override
      public void addFilterFromParamMap(Map map) {
        String[] recordParams = (String[]) map.get("_records");
        Integer first = 0;
        Integer last = 0;

        if (null != recordParams && recordParams.length > 0) {

          // From and To record numbers
          String strFrom = StringUtils.substringBefore(recordParams[0], "-");
          String strTo = StringUtils.substringAfter(recordParams[0], "-");
          try {
            first = StringUtils.isNumeric(strFrom)
                ? Math.abs(Integer.parseInt(strFrom) - 1) : 0;
          } catch (NumberFormatException nfe) {
            // ignore, we do not want to fail the query, if the user
            // provided meaningless inputs, we just assume meaningful defaults
            first = 0;
          }
          try {
            last = StringUtils.isNumeric(strTo)
                ? Math.abs(Integer.parseInt(strTo)) : 0;
          } catch (NumberFormatException nfe) {
            // ignore, we do not want to fail the query, if the user
            // provided meaningless inputs, we just assume meaningful defaults
            last = 0;
          }
          // Offset and Limit from the record numbers
          offset = Math.min(first, last);
          limit = Math.max(first, last) - offset;
        }

        super.addFilterFromParamMap(map);
      }

      @Override
      public PagedList getDynaPagedList() {
        PagedList list = super.getDynaPagedList();
        if (null != list && 0 == list.getPageSize()) {
          list.setPageSize(limit);
        }
        return list;
      }

    };

    queryAssembler.addFilterFromParamMap(searchParams);
    queryAssembler.addSecondarySort("stock_take_detail_id", false);
    queryAssembler.build();
    return queryAssembler.getMappedPagedList();
  }

}
