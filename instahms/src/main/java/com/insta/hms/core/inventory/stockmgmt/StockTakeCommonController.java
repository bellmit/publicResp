package com.insta.hms.core.inventory.stockmgmt;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.inventory.URLRoute;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * The Class StockManagementController.
 */
public abstract class StockTakeCommonController extends StockTakePrintController {

  /** The Constant logger. */
  static final Logger logger = LoggerFactory.getLogger(StockTakeCommonController.class);

  /** The stock take service. */
  @LazyAutowired
  private StockTakeService stockTakeService;

  /**
   * Get Stock Take Summary Data.
   * 
   * @param stockTakeId
   *          request parameter stock_take_id
   * @return JSON map containing the summary data
   */
  @IgnoreConfidentialFilters
  @GetMapping(value = { URLRoute.STOCK_TAKE_EXTENDED_SUMMARY })
  public ResponseEntity<Map<String, Object>> getApproveItemsSummary(
      @RequestParam(value = "stock_take_id",
          required = true) String stockTakeId) {
    Map<String, Object> responseMap = new HashMap<>();
    Map<String, Object> summary = stockTakeService
        .getApproveItemsSummary(stockTakeId);
    responseMap.put("stock_take_summary", summary);
    return new ResponseEntity<>(responseMap, HttpStatus.OK);
  }

  /**
   * Get list of items for reconciliation / approval.
   * 
   * @param request
   *          Http Request
   * @return JSON map of items
   */
  @IgnoreConfidentialFilters
  @GetMapping(value = { URLRoute.STOCK_TAKE_EXTENDED_LIST })
  public ResponseEntity<Map<String, Object>> getApproveItemsList(
      HttpServletRequest request) {
    Map<String, Object> responseMap = new HashMap<>();
    PagedList itemList = stockTakeService
        .searchApproveItems(request.getParameterMap());
    responseMap.put("physical_stock_take_detail", itemList);
    responseMap.put("filter_options",
        ConversionUtils.flatten(request.getParameterMap()));
    return new ResponseEntity<>(responseMap, HttpStatus.OK);
  }
  
}
