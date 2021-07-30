package com.insta.hms.core.inventory.stockmgmt;

import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.inventory.URLRoute;
import com.insta.hms.exception.ValidationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(URLRoute.STOCK_MGMT_URL)
public class StockTakeCreateController extends BaseRestController {

  /** The Constant logger. */
  static final Logger logger = LoggerFactory
      .getLogger(StockTakeCreateController.class);

  /** The session service. */
  @LazyAutowired
  private SessionService sessionService;

  /** The stock take service. */
  @LazyAutowired
  private StockTakeService stockTakeService;

  /**
   * Initiates a stock take for the store.
   *
   * @param requestBody
   *          the request body
   * @return the response entity
   */
  @IgnoreConfidentialFilters
  @PostMapping(value = URLRoute.CREATE_STOCK_TAKE,
      consumes = "application/json")
  public ResponseEntity<Map<String, Object>> createStockTake(
      @RequestBody ModelMap requestBody) {
    if (requestBody == null || requestBody.isEmpty()
        || requestBody.get("storeid") == null) {
      throw new ValidationException("exception.stock.take.required.store",
          null);
    }
    // get store id
    Integer storeId = (Integer) requestBody.get("storeid");
    // Get logged in user
    String loggedInUser = (String) sessionService.getSessionAttributes()
        .get("userId");
    logger.debug("storeId = {}, user = {}", storeId, loggedInUser);

    Map createResponse = stockTakeService.create(storeId, loggedInUser);
    int recordCount = (int) createResponse.get("recordCount");
    Map<String, Object> stockTake = (HashMap<String, Object>) createResponse
        .get("stockTake");
    logger.debug("number of records created {} ", recordCount);
    if (recordCount > 0) {
      return new ResponseEntity<>(stockTake, HttpStatus.CREATED);
    } else {
      return new ResponseEntity<>(stockTake, HttpStatus.BAD_REQUEST);
    }
  }

}
