package com.insta.hms.core.inventory.stockmgmt;

import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.inventory.URLRoute;
import com.insta.hms.exception.ValidationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

/**
 * The Class StockManagementController.
 */
@RestController
@RequestMapping(URLRoute.STOCK_MGMT_URL)
public class StockManagementController extends BaseRestController {

  /** The Constant logger. */
  static final Logger logger = LoggerFactory
      .getLogger(StockManagementController.class);

  /** The stock take service. */
  @LazyAutowired
  private StockTakeService stockTakeService;

  /**
   * get Stock Management Index Page.
   * 
   * @return ModelAndView
   */
  @IgnoreConfidentialFilters
  @GetMapping(URLRoute.STOCK_MGMT_INDEX_URL)
  public ModelAndView getStockManagementIndexPage() {
    return renderFlowUi(
        "Inventory", "inventory", "withFlow", "inventoryFlow", "stockmanagement", false);
  }

  /**
   * Updates count for any of the provided items and then marks the stock take
   * as completed.
   * 
   * @param modelMap
   *          map of post data. should specify the stock_take_id for which
   *          save and finalize is being attempted
   * @return Stock take bean that was finalized
   */
  @IgnoreConfidentialFilters
  @PostMapping(value = { URLRoute.STOCK_TAKE_COUNT_FINALIZE })
  public ResponseEntity<Map<String, Object>> finalizeCount(
      @RequestBody ModelMap modelMap) {
    if (null == modelMap || modelMap.isEmpty()
        || !modelMap.containsKey("stock_take_id")) {
      throw new ValidationException("exception.stock.take.number.required");
    }
    Map<String, Object> responseMap = stockTakeService
        .finalizeCount((String)modelMap.get("stock_take_id"), modelMap);
    return new ResponseEntity<>(responseMap, HttpStatus.OK);
  }

}
