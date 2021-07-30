package com.insta.hms.core.inventory.stockmgmt;

import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.inventory.URLRoute;
import com.insta.hms.core.inventory.stocks.StoreStockDetailsService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * The Class StockManagementController.
 */
@RestController
@RequestMapping(URLRoute.STOCK_MGMT_URL)
public class StockTakeListController extends BaseRestController {

  /** The Constant logger. */
  static final Logger logger = LoggerFactory.getLogger(StockTakeListController.class);

  /** The store stock details service. */
  @LazyAutowired
  private StoreStockDetailsService storeStockDetailsService;

  /** The stock take service. */
  @LazyAutowired
  protected StockTakeService stockTakeService;

  /** The stock take print service. */
  @LazyAutowired
  private StockTakePrintService stockTakePrintService;

  /**
   * Retrieves the list of stock takes matching the search / filter criteria.
   *
   * @param request
   *          the HTTP Request
   * @return JSON map containing the paged list of stock take records
   */
  @IgnoreConfidentialFilters
  @GetMapping(value = { URLRoute.STOCK_TAKE_LIST })
  public ResponseEntity<Map<String, Object>> list(HttpServletRequest request) {
    Map<String, Object> responseMap = new HashMap<>();
    PagedList pagedList = stockTakeService.search(request.getParameterMap());
    responseMap.put("physical_stock_take", pagedList);
    return new ResponseEntity<>(responseMap, HttpStatus.OK);

  }

  /**
   * Gets the list of stores (id, name) that the user has access to including
   * the item batch count.
   *
   * @return the list
   */
  @IgnoreConfidentialFilters
  @GetMapping(URLRoute.GET_STORES_ITEM_BATCH_COUNT)
  public Map<String, Object> getItemBatchCount() {
    // String username = (String)
    // sessionService.getSessionAttributes().get("userId");

    return storeStockDetailsService.getStoresItemBatchCount();
  }

  /**
   * Get stock take search filter options.
   * 
   * @param request
   *          Http Request
   * @return JSON data for populating all filter options
   */
  @IgnoreConfidentialFilters
  @GetMapping(value = { URLRoute.STOCK_TAKE_FILTERDATA })
  public ResponseEntity<Map<String, Object>> getStockTakeFilterData(
      HttpServletRequest request) {
    Map<String, Object> responseMap = new HashMap<>();
    Map filterParams = stockTakeService
        .getSearchFilterData();
    responseMap.put("stock_take_filter_data", filterParams);
    return new ResponseEntity<>(responseMap, HttpStatus.OK);
  }

  /**
   * Get allowed actions for a stock take based on the current status of the
   * stock take and the user privileges.
   * 
   * @param request
   *          Http Request
   * @return JSON data mapping stock take status to a list of actions allowed
   *         for any stock take in the given state
   */
  @IgnoreConfidentialFilters
  @GetMapping(value = { URLRoute.STOCK_TAKE_ACTIONMAP })
  public ResponseEntity<Map<String, Object>> getStockTakeActionMap(
      HttpServletRequest request) {
    Map<String, Object> responseMap = new HashMap<>();
    Map actionMap = stockTakeService
        .getActionMapping();
    responseMap.put("stock_take_actions", actionMap);
    return new ResponseEntity<>(responseMap, HttpStatus.OK);
  }
  
  /**
   * Get a list of stock take numbers with store and center, matching the search text.
   * @param searchText search text to match the stock take number, with.
   * @return List of map of stock take beans with the matching stock take number
   */
  @IgnoreConfidentialFilters
  @GetMapping(value = { URLRoute.STOCK_TAKE_LOOKUP })
  public ResponseEntity<Map<String, Object>> lookup(
      @RequestParam(value = "search_text", required = true) String searchText) {
    Map<String, Object> responseMap = new HashMap<>();
    List<BasicDynaBean> result = stockTakeService.lookup(searchText);
    responseMap.put("stock_take_lookup",
        ConversionUtils.listBeanToListMap(result));
    return new ResponseEntity<>(responseMap, HttpStatus.OK);
  }

  /**
   * Get item search filter options.
   * 
   * @param request
   *          Http Request
   * @return JSON data for populating all filter options
   */
  @IgnoreConfidentialFilters
  @GetMapping(value = { URLRoute.STOCK_TAKE_ITEMS_FILTERDATA })
  public ResponseEntity<Map<String, Object>> getItemsFilterData(
      HttpServletRequest request) {
    Map<String, Object> responseMap = new HashMap<>();
    Map filterParams = stockTakeService
        .getItemSearchFilterData();
    responseMap.put("items_filter_data", filterParams);
    return new ResponseEntity<>(responseMap, HttpStatus.OK);
  }

  /**
   * Get a list of medicines that are in stock (including 0 stock) in the store
   * corresponding to the stock take.
   *
   * @param searchText
   *          search text to match the stock take number, with.
   * @return List of map of stock take beans with the matching stock take number
   */
  @IgnoreConfidentialFilters
  @GetMapping(value = { URLRoute.STOCK_TAKE_ITEMS_LOOKUP })
  public ResponseEntity<Map<String, Object>> lookupItems(
      @RequestParam(value = "stock_take_id",
          required = true) String stockTakeId,
      @RequestParam(value = "search_text", required = true) String searchText) {
    Map<String, Object> responseMap = new HashMap<>();
    List<BasicDynaBean> result = Collections.emptyList();
    List<BasicDynaBean> beans = stockTakeService.lookup(stockTakeId);
    if (CollectionUtils.isNotEmpty(beans)) {
      result = storeStockDetailsService
          .searchMedicine((Integer) beans.get(0).get("store_id"), searchText);
    }
    responseMap.put("stock_take_lookup",
        ConversionUtils.listBeanToListMap(result));
    return new ResponseEntity<>(responseMap, HttpStatus.OK);
  }

  /**
   * Get the Printer Settings and Printer Template Options.
   * 
   * @return Map - JSON map containing the printer settings and print templates
   */
  @IgnoreConfidentialFilters
  @GetMapping(value = URLRoute.STOCK_TAKE_PRINT_OPTIONS)
  public ModelAndView getStockTakePrintOptions() {
    ModelAndView view = new ModelAndView();
    List listMap = ConversionUtils
        .listBeanToListMap(stockTakePrintService.getPrinterSettings());
    view.addObject("printerSettings", listMap);
    view.addObject("printTemplates", stockTakePrintService.getTemplateNames());
    BasicDynaBean bean = stockTakePrintService.getDefaultPrinterSettings();
    if (null != bean) {
      view.addObject("defaultPrinterId", bean.getMap().get("printer_id"));
    }
    return view;
  }

}
