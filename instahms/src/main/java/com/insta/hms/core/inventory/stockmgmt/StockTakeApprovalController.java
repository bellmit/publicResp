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
@RequestMapping(URLRoute.STOCK_TAKE_APPROVAL_BASE)
public class StockTakeApprovalController extends StockTakeCommonController {

  /** The Constant logger. */
  private static final Logger logger = LoggerFactory
      .getLogger(StockTakeApprovalController.class);

  /** The stock take service. */
  @LazyAutowired
  private StockTakeService stockTakeService;

  @Override
  protected StockTakeAction getDelegateAction() {
    return StockTakeAction.APPROVE;
  }

  /**
   * Re-opens a stock take, which has already been marked as recoiled, for
   * reconciliation.
   * 
   * @param modelMap
   *          json payload map containing stock_take_id
   * @return Map of the stock take record, after the status update
   */
  @IgnoreConfidentialFilters
  @PostMapping(value = { URLRoute.STOCK_TAKE_RECONCILE_REOPEN })
  public ResponseEntity<Map<String, Object>> reopenReconciliation(
      @RequestBody(required = true) ModelMap modelMap) {

    if (null == modelMap || modelMap.isEmpty()
        || !modelMap.containsKey("stock_take_id")) {
      throw new ValidationException("exception.stock.take.number.required");
    }
    Map<String, Object> responseMap = new HashMap<>();
    BasicDynaBean updatedBean = stockTakeService
        .reopenReconciliation((String) modelMap.get("stock_take_id"));
    responseMap.put("physical_stock_take",
        (null != updatedBean) ? updatedBean.getMap() : Collections.EMPTY_MAP);
    return new ResponseEntity<>(responseMap, HttpStatus.OK);
  }

  /**
   * Approves and posts adjustments against an already reconciled stock take.
   * 
   * @param modelMap
   *          json payload map containing stock_take_id
   * @return map of the bean after its status is updated.
   */
  @IgnoreConfidentialFilters
  @PostMapping(value = { URLRoute.STOCK_TAKE_APPROVE_ADJUST })
  public ResponseEntity<Map<String, Object>> approveStockTake(
      @RequestBody(required = true) ModelMap modelMap) {
    if (null == modelMap || modelMap.isEmpty()
        || !modelMap.containsKey("stock_take_id")) {
      throw new ValidationException("exception.stock.take.number.required");
    }
    Map<String, Object> responseMap = new HashMap<>();
    BasicDynaBean updatedBean = stockTakeService
        .finalizeApproval((String) modelMap.get("stock_take_id"));
    responseMap.put("physical_stock_take",
        (null != updatedBean) ? updatedBean.getMap() : Collections.EMPTY_MAP);
    return new ResponseEntity<>(responseMap, HttpStatus.OK);
  }

}
