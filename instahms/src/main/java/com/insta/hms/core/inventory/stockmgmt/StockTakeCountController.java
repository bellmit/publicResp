package com.insta.hms.core.inventory.stockmgmt;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.inventory.URLRoute;
import com.insta.hms.core.inventory.stockmgmt.StockTakeService.StockTakeAction;
import com.insta.hms.exception.ValidationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(URLRoute.STOCK_TAKE_COUNT_BASE)
public class StockTakeCountController extends StockTakePrintController {

  /** The stock take service. */
  @LazyAutowired
  private StockTakeService stockTakeService;

  @Override
  protected StockTakeAction getDelegateAction() {
    return StockTakeAction.COUNT;
  }

  /**
   * Get Stock Take Summary Data.
   * 
   * @param stockTakeId
   *          request parameter stock_take_id
   * @return JSON map containing the summary data
   */
  @IgnoreConfidentialFilters
  @GetMapping(value = { URLRoute.STOCK_TAKE_COUNT_SUMMARY })
  public ResponseEntity<Map<String, Object>> getCountItemsSummary(@RequestParam(
      value = "stock_take_id", required = true) String stockTakeId) {
    Map<String, Object> responseMap = new HashMap<>();
    Map<String, Object> summary = stockTakeService.getCountItemsSummary(stockTakeId);
    responseMap.put("stock_take_summary", summary);
    return new ResponseEntity<>(responseMap, HttpStatus.OK);
  }

  /**
   * Get list of items for updating the physical stock quantity.
   * 
   * @param request
   *          Http Request
   * @return JSON map of items
   */
  @IgnoreConfidentialFilters
  @GetMapping(value = { URLRoute.STOCK_TAKE_COUNT_LIST })
  public ResponseEntity<Map<String, Object>> getCountItemsList(
      HttpServletRequest request) {
    Map<String, Object> responseMap = new HashMap<>();
    PagedList itemList = stockTakeService
        .searchCountItems(request.getParameterMap());
    responseMap.put("physical_stock_take_detail", itemList);
    responseMap.put("filter_options",
        ConversionUtils.flatten(request.getParameterMap()));
    return new ResponseEntity<>(responseMap, HttpStatus.OK);
  }

  /**
   * Updates count and remarks for any of the provided items.
   * 
   * @param modelMap
   *          stock take items with physical qty which have been submitted by
   *          the user
   * @return map of all messages, warnings, errors, info generated during the
   *         bulk update
   */
  @IgnoreConfidentialFilters
  @PostMapping(value = { URLRoute.STOCK_TAKE_COUNT_UPDATE })
  public ResponseEntity<Map<String, Object>> updateCount(
      @RequestBody ModelMap modelMap) {
    if (null == modelMap || modelMap.isEmpty()
        || !modelMap.containsKey("stock_take_id")) {
      throw new ValidationException("exception.stock.take.number.required");
    }
    Map<String, Object> responseMap = stockTakeService
        .updateCount((String) modelMap.get("stock_take_id"), modelMap);
    return new ResponseEntity<>(responseMap, HttpStatus.OK);
  }
}
