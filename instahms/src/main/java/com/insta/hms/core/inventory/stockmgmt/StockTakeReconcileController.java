package com.insta.hms.core.inventory.stockmgmt;

import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.inventory.URLRoute;
import com.insta.hms.core.inventory.stockmgmt.StockTakeService.StockTakeAction;
import com.insta.hms.exception.ValidationException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(URLRoute.STOCK_TAKE_RECONCILE_BASE)
public class StockTakeReconcileController extends StockTakeCommonController {

  /** The Constant logger. */
  static final Logger logger = LoggerFactory
      .getLogger(StockTakeReconcileController.class);

  /** The stock take service. */
  @LazyAutowired
  private StockTakeService stockTakeService;

  @Override
  protected StockTakeAction getDelegateAction() {
    return StockTakeAction.RECONCILE;
  }

  /**
   * Re-opens a stock take, which has already been marked as recoiled, for
   * reconciliation.
   * 
   * @param modelMap
   *          stock take Id which has to be reopened for counting
   * @return Map of the stock take record, after the status update
   */
  @IgnoreConfidentialFilters
  @PostMapping(value = { URLRoute.STOCK_TAKE_COUNT_REOPEN })
  public ResponseEntity<Map<String, Object>> reopenCount(
      @RequestBody(required = true) ModelMap modelMap) {

    if (null == modelMap || modelMap.isEmpty()
        || !modelMap.containsKey("stock_take_id")) {
      throw new ValidationException("exception.stock.take.number.required");
    }

    Map<String, Object> responseMap = new HashMap<>();
    BasicDynaBean updatedBean = stockTakeService
        .reopenCount((String) modelMap.get("stock_take_id"));
    responseMap.put("physical_stock_take",
        (null != updatedBean) ? updatedBean.getMap() : Collections.EMPTY_MAP);
    return new ResponseEntity<>(responseMap, HttpStatus.OK);
  }

  /**
   * Updates count for any of the provided items and then marks the stock take
   * as completed.
   * 
   * @param modelMap
   *          stock take items with physical qty which have been submitted by
   *          the user
   * @return Map of the stock take record, after the status update
   */
  @IgnoreConfidentialFilters
  @PostMapping(value = { URLRoute.STOCK_TAKE_RECONCILE_FINALIZE })
  public ResponseEntity<Map<String, Object>> finalizeReconciliation(
      @RequestBody ModelMap modelMap) {
    if (null == modelMap || modelMap.isEmpty()
        || !modelMap.containsKey("stock_take_id")) {
      throw new ValidationException("exception.stock.take.number.required");
    }
    Map<String, Object> responseMap = stockTakeService
        .finalizeReconciliation((String)modelMap.get("stock_take_id"), modelMap);
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
  @PostMapping(value = { URLRoute.STOCK_TAKE_RECONCILE_UPDATE })
  public ResponseEntity<Map<String, Object>> updateReconciliation(
      @RequestBody ModelMap modelMap) {
    if (null == modelMap || modelMap.isEmpty()
        || !modelMap.containsKey("stock_take_id")) {
      throw new ValidationException("exception.stock.take.number.required");
    }

    Map<String, Object> responseMap = stockTakeService
        .updateReconciliation((String)modelMap.get("stock_take_id"), modelMap);
    return new ResponseEntity<>(responseMap, HttpStatus.OK);
  }

}
