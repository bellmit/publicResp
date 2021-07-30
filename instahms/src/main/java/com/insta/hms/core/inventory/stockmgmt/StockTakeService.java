package com.insta.hms.core.inventory.stockmgmt;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DateHelper;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.clinical.consultation.ValidationUtils;
import com.insta.hms.core.inventory.stocks.StockAdjustmentService;
import com.insta.hms.exception.AccessDeniedException;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.mdm.centers.CenterService;
import com.insta.hms.mdm.integration.storecategory.StoreCategoryIntegrationService;
import com.insta.hms.mdm.integration.stores.ManufacturerIntegrationService;
import com.insta.hms.mdm.servicegroup.ServiceGroupService;
import com.insta.hms.mdm.servicesubgroup.ServiceSubGroupService;
import com.insta.hms.mdm.stores.StoreService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class StockTakeService.
 */

@Service
public class StockTakeService {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(StockTakeService.class);

  /** The stock take repository. */
  @LazyAutowired
  StockTakeRepository stockTakeRepository;
  /** The store service. */
  @LazyAutowired
  StoreService storeService;
  
  @LazyAutowired
  CenterService centerService;

  @LazyAutowired
  StockTakeDetailRepository stockTakeDetailRepository;

  @LazyAutowired
  ManufacturerIntegrationService manfService;
  @LazyAutowired
  
  StoreCategoryIntegrationService categoryService;

  @LazyAutowired
  ServiceGroupService serviceGroupService;

  @LazyAutowired
  ServiceSubGroupService serviceSubGroupService;

  @LazyAutowired
  MessageUtil messageUtil;

  @LazyAutowired
  SessionService sessionService;

  @LazyAutowired 
  StockAdjustmentService storeAdjustmentService;

  /**
   * The Enum StockTakeStatus.
   */
  public enum StockTakeStatus {

    /** The initiated. */
    INITIATED('I', "ui.label.inventory.stock.take.status.initiated",
        StockTakeAction.COUNT),

    /** The completed. */
    COMPLETED('C', "ui.label.inventory.stock.take.status.completed",
        StockTakeAction.RECONCILE),

    /** The reconciled. */
    RECONCILED('R', "ui.label.inventory.stock.take.status.reconciled",
        StockTakeAction.APPROVE),

    /** The approved. */
    APPROVED('A', "ui.label.inventory.stock.take.status.approved", 
        StockTakeAction.VIEW),

    /** The cancelled. */
    CANCELLED('X', "ui.label.inventory.stock.take.status.cancelled");

    /** The code. */
    private final char code;
    private final String displayNameKey;
    private final List<StockTakeAction> allowedActions;

    /**
     * Instantiates a new stock take status.
     *
     * @param code the code
     */
    StockTakeStatus(char code, String displayNameKey,
        StockTakeAction... actions) {
      this.code = code;
      this.displayNameKey = displayNameKey;
      this.allowedActions = (null != actions) ? Arrays.asList(actions)
          : Collections.EMPTY_LIST;
    }

    /**
     * Gets the code.
     *
     * @return the code
     */
    public String getCode() {
      return Character.toString(this.code);
    }
    
    public String getDisplayNameKey() {
      return this.displayNameKey;
    }

    public List<StockTakeAction> getAllowedActions() {
      return allowedActions;
    }

  }

  public enum StockTakeAction {

    COUNT("store_stock_take_count"),
    RECONCILE("store_stock_take_reconcile"),
    APPROVE("store_stock_take_approve"),
    VIEW("store_stock_take_list");
    
    private final String actionId;

    StockTakeAction(String actionId) {
      this.actionId = actionId;
    }
    
    public String getActionId() {
      return this.actionId;
    }
  }
  
  /**
   * Initiates a new stocktake.
   *
   * @param storeId      the store id
   * @param loggedInUser the logged in user
   * @return the int
   */
  public Map<String, Object> create(int storeId, String loggedInUser) {
    logger.debug("initiating stock take for store: {} by user: {}", storeId,
        loggedInUser);

    Integer roleId = RequestContext.getRoleId();
    if ((1 != roleId) && (2 != roleId)) {
      if (!isValidStore(storeId, loggedInUser)) {
        throw new ValidationException("exception.stock.take.invalid.store");
      }
    }
    // If a stock take is already in progress for this store
    if (isExist(storeId)) {
      throw new ValidationException("exception.stock.take.open");
    }
    
    // Request is valid. Continue.
    BasicDynaBean stockTakeBean = stockTakeRepository.getBean();
    stockTakeBean.set("stock_take_id", stockTakeRepository.getNextId());
    stockTakeBean.set("store_id", storeId);
    stockTakeBean.set("initiated_by", loggedInUser);
    stockTakeBean.set("user_name", loggedInUser);
    stockTakeBean.set("status", StockTakeStatus.INITIATED.getCode());
    java.sql.Timestamp currentTime = new Timestamp(
        DateHelper.getCurrentDate().toDateTime().getMillis());

    stockTakeBean.set("initiated_datetime", currentTime);
    stockTakeBean.set("mod_time", currentTime);
    
    BasicDynaBean storeBean = storeService.findByStore(storeId);
    if (null != storeBean) {
      Integer expMonths = (Integer) storeBean.get("stock_take_item_exp_months");
      if (null != expMonths && expMonths > 0) {
        DateTime now = DateHelper.getCurrentDate();
        DateTime refDate = now.minusMonths(expMonths).withDayOfMonth(1);
        java.sql.Date refExpDate = new java.sql.Date(
            refDate.toDate().getTime());
        stockTakeBean.set("inactive_item_excl_dt", refExpDate);
      }
    }
    
    int recordCount = stockTakeRepository.insert(stockTakeBean);
    
    HashMap<String, Object> response = new HashMap<>();
    response.put("recordCount", recordCount);
    response.put("stockTake", stockTakeBean.getMap());
    
    return response;
  }

  /**
   * Checks if is valid store.
   *
   * @param storeId the store id
   * @param userId  the user id
   * @return true, if is valid store
   */
  private boolean isValidStore(int storeId, String userId) {
    return (storeService.isStoreValid(storeId) 
        && storeService.userHasAccess(storeId, userId));
  }

  /**
   * Checks if an in-progress stock take exist.
   *
   * @param storeId the store id
   * @return true, if is exist
   */
  public boolean isExist(int storeId) {
    Map<String, Object> filterMap = new HashMap<>();
    filterMap.put("store_id", storeId);
    List<String> statusList = new ArrayList<>();
    statusList.add(StockTakeStatus.INITIATED.getCode());
    //statusList.add(StockTakeStatus.COUNTING_IN_PROGRESS.getCode());
    statusList.add(StockTakeStatus.COMPLETED.getCode());
    statusList.add(StockTakeStatus.RECONCILED.getCode());
    filterMap.put("status", statusList);
    List<BasicDynaBean> stockTakeBeans = stockTakeRepository
        .findByCriteria(filterMap);

    logger.debug("stockTakeBeans size: {}", stockTakeBeans.size());

    return (!stockTakeBeans.isEmpty());
  }

  /**
   * Searches stock take records matching the filter criteria.
   * 
   * @param searchParams
   *          - filter parameters
   * @return PagedList of matching records
   */
  public PagedList search(Map<String, String[]> searchParams) {

    List<Integer> storeList = null;

    // get user accessible stores
    List<BasicDynaBean> userStores = storeService.listByUserAccess();
    // If no store access, error out
    if (null == userStores || userStores.isEmpty()) {
      throw new AccessDeniedException(
          "js.stores.mgmt.noassignedstore.notaccessthisscreen");
    }
    storeList = new ArrayList<Integer>();
    Map storeMap = ConversionUtils.listBeanToMapBean(userStores, "dept_id");
    storeList.addAll(storeMap.keySet());

    return stockTakeRepository.search(searchParams, storeList);
  }

  /**
   * Gets the list of filter data options for stock take item search.
   * 
   * @return Map containing a list of filter parameters
   */
  public Map<String, Object> getItemSearchFilterData() {
    Map<String, Object> filterData = new HashMap<String, Object>();
    filterData.put("manufacturers",
        ConversionUtils.listBeanToListMap(manfService.lookup(true)));
    filterData.put("store_categories",
        ConversionUtils.listBeanToListMap(categoryService.lookup(true)));
    filterData.put("service_groups",
        ConversionUtils.listBeanToListMap(serviceGroupService.lookup(true)));
    filterData.put("service_subgroups",
        ConversionUtils.listBeanToListMap(serviceSubGroupService.lookup(true)));
    return filterData;
  }

  /**
   * Gets the list of filter data options for stock take search.
   * 
   * @return Map containing a list of filter parameters
   */
  public Map<String, Object> getSearchFilterData() {
    Map<String, Object> filterData = new HashMap<>();
    Integer loggedInCenter = RequestContext.getCenterId();
    List<BasicDynaBean> centers = new ArrayList<>();
    if (0 == loggedInCenter) {
      centers = centerService.listAll(false);
    } else {
      BasicDynaBean center = centerService.getCenterDetails(loggedInCenter);
      centers.add(center);
    }
    
    if (centers != null) {
      List centerList = ConversionUtils.listBeanToListMap(centers);      
      filterData.put("centers", centerList);
    }
    
    // Stores that the user has access to
    // String userId = RequestContext.getUserName();
    List<BasicDynaBean> stores = storeService.listByUserAccess();

    if (stores != null) {
      List storesList = ConversionUtils.listBeanToListMap(stores);
      filterData.put("stores", storesList);
    }
    // All valid status
    List<Map<String,String>> allStatus = getAllStatus();
    filterData.put("status", allStatus);
    
    // All Stock Takes so far
    List<BasicDynaBean> stockTakeList = lookup(null);
    if (null != stockTakeList) {
      filterData.put("stock_take_lookup", ConversionUtils.listBeanToListMap(stockTakeList));
    }
    return filterData;
    
  }

  /**
   * Gets a list of stock take records with the matching search text.
   * @param searchText search string to match the stock take number with.
   * @return List of beans matching the search string.
   */
  public List<BasicDynaBean> lookup(String searchText) {
    return stockTakeRepository.lookup(searchText);
  }

  /**
   * Gets a map of actions allowed for the user, for different states of the
   * stock take.
   * 
   * @return Map of current state to next allowed actions.
   */
  public Map<String, Object> getActionMapping() {
    Map<String, Object> responseMap = new HashMap<String, Object>();

    Map urlRightsMap = (Map) sessionService
        .getSessionAttributes(new String[] { "urlRightsMap" })
        .get("urlRightsMap");

    for (StockTakeStatus status : StockTakeStatus.values()) {
      List<String> actions = new ArrayList<String>();
      for (StockTakeAction action : status.getAllowedActions()) {
        String allowed = (String) urlRightsMap.get(action.getActionId());
        if ((null != allowed) && (!allowed.equalsIgnoreCase("N"))) {
          actions.add(action.name().toLowerCase());
        }
      }
      responseMap.put(status.getCode(), actions);
    }
    return responseMap;
  }

  /**
   * Fetches stock take summary useful while counting, cost and variance is
   * always set to 0.
   * 
   * @param stockTakeId
   *          Stock Take Number
   * @return Map of summary data for the stock take
   */
  public Map<String, Object> getCountItemsSummary(String stockTakeId) {
    return getItemsSummary(stockTakeId, false);
  }

  /**
   * Searches stock take items, matching the filter criteria.
   * 
   * @param params Filter parameters
   * @return PagedList of matching records
   */
  public PagedList searchCountItems(Map<String, String[]> params) {
    return searchItems(params, false);
  }

  /**
   * Fetches stock take summary useful while reconciling / approving, including
   * the cost and variance.
   * 
   * @param stockTakeId
   *          Stock take number  
   * @return Map of summary data for the stock take
   */
  public Map<String, Object> getApproveItemsSummary(String stockTakeId) {
    return getItemsSummary(stockTakeId, true);
  }

  /**
   * Searches stock take items, matching the filter criteria.
   * 
   * @param params Filter parameters
   * @return PagedList of matching records
   */
  public PagedList searchApproveItems(Map<String, String[]> params) {
    return searchItems(params, true);
  }

  private Map<String, Object> getItemsSummary(String stockTakeId, boolean includeCostDetails) {

    BasicDynaBean bean = stockTakeRepository.getStockTakeSummary(stockTakeId,
        includeCostDetails);
    Map<String, Object> summary = Collections.emptyMap();
    if (null != bean) {
      summary = bean.getMap();
    }
    return summary;
  }

  private PagedList searchItems(Map<String, String[]> searchParams,
      boolean includeSystemStock) {
    return stockTakeDetailRepository.search(searchParams, includeSystemStock);
  }

  private List<Map<String, String>> getAllStatus() {

    List<Map<String, String>> statusList = new ArrayList<Map<String, String>>();
    for (StockTakeStatus s : StockTakeStatus.values()) {
      HashMap<String, String> statusMap = new HashMap<>();
      statusMap.put("status", s.getCode());
      statusMap.put("status_display",
          messageUtil.getMessage(s.getDisplayNameKey()));
      statusList.add(statusMap);
    }

    return statusList;
  }

  /**
   * Reopen a stock take for counting again.
   * 
   * @param stockTakeId
   *          the stock take id
   * @return stock take bean after reopening
   */
  @Transactional(rollbackFor = Exception.class)
  public BasicDynaBean reopenCount(String stockTakeId) {
    return updateStatus(stockTakeId, StockTakeStatus.COMPLETED,
        StockTakeStatus.INITIATED);
  }

  /**
   * Reopen the stock take for reconciliation again.
   * 
   * @param stockTakeId
   *          the stock take number
   * @return stock take bean after reopening
   */
  @Transactional(rollbackFor = Exception.class)
  public BasicDynaBean reopenReconciliation(String stockTakeId) {
    return updateStatus(stockTakeId, StockTakeStatus.RECONCILED,
        StockTakeStatus.COMPLETED);
  }

  /**
   * Updates the count for any items provided and then marks the stock take as
   * completed.
   * 
   * @param stockTakeId
   *          the stock take number
   * @param modelMap
   *          modelMap containing the items to be updated
   * @return response data, indicating success / failure for item updates
   */
  @Transactional(rollbackFor = Exception.class)
  public Map<String, Object> finalizeCount(String stockTakeId,
      Map<String, Object> modelMap) {
    Map<String, Object> errorMap = new HashMap<>();
    Map<String, Object> statusMap = new HashMap<>();
    Map<String, Object> responseData = updateStockTakeItems(
        StockTakeAction.COUNT, stockTakeId, modelMap, errorMap);
    if (isCountComplete(stockTakeId, true, statusMap) && errorMap.isEmpty()) {
      updateStatus(stockTakeId, StockTakeStatus.INITIATED,
          StockTakeStatus.COMPLETED);
      statusMap.put("completed", true);
    } else {
      statusMap.put("completed", false);
    }
    responseData.put("status", statusMap);
    return responseData;
  }

  /**
   * Marks the stock take as reconciled, after updating any of the item counts
   * and remarks provided.
   * 
   * @param stockTakeId
   *          the stock take number
   * @param modelMap
   *          modelMap containing the item counts and remarks
   * @return response data, indicating success / failure for item updates
   */
  @Transactional(rollbackFor = Exception.class)
  public Map<String, Object> finalizeReconciliation(String stockTakeId,
      Map<String, Object> modelMap) {
    Map<String, Object> errorMap = new HashMap<>();
    Map<String, Object> statusMap = new HashMap<>();
    Map<String, Object> responseData = updateStockTakeItems(
        StockTakeAction.RECONCILE, stockTakeId, modelMap, errorMap);
    if (isReconciliationComplete(stockTakeId, statusMap) && errorMap.isEmpty()) {
      updateStatus(stockTakeId, StockTakeStatus.COMPLETED,
          StockTakeStatus.RECONCILED);
      statusMap.put("completed", true);
    } else {
      statusMap.put("completed", false);
    }
    responseData.put("status", statusMap);
    return responseData;
  }

  /**
   * Marks the stock take as approved, and posts the variance as stock
   * adjustment.
   * 
   * @param stockTakeId
   *          the stock take number
   * @return the stock take bean after the status change
   */
  @Transactional(rollbackFor = Exception.class)
  public BasicDynaBean finalizeApproval(String stockTakeId) {
    BasicDynaBean stockTakeBean = stockTakeRepository.findByKey("stock_take_id",
        stockTakeId);
    List<BasicDynaBean> adjustments = stockTakeRepository
        .getUnadjustedItems(stockTakeId);
    storeAdjustmentService.postAdjustments(
        (Integer) stockTakeBean.get("store_id"), adjustments,
        "Stock Take / Reconciliation", (String)stockTakeBean.get("stock_take_id"));
    stockTakeBean = updateStatus(stockTakeId, StockTakeStatus.RECONCILED,
        StockTakeStatus.APPROVED);
    return stockTakeBean;
  }

  /**
   * Updates the physical stock count and remarks if any provided for the items.
   * 
   * @param stockTakeId
   *          stock take number
   * @param modelMap
   *          modelMap containing the item count and remarks, if any
   * @return response data, indicating success / failure for item updates
   */
  @Transactional(rollbackFor = Exception.class)
  public Map<String, Object> updateCount(String stockTakeId,
      Map<String, Object> modelMap) {
    Map<String, Object> errorMap = new HashMap<>();
    return updateStockTakeItems(StockTakeAction.COUNT, stockTakeId,
        modelMap, errorMap);
  }

  /**
   * Updates the reconciled physical stock count and reconciliation remarks for
   * the items.
   * 
   * @param stockTakeId
   *          the stock take number
   * @param modelMap
   *          modelMap containing items with reconciled quantities and remarks
   * @return response data, indicating success / failure for item updates
   */
  @Transactional(rollbackFor = Exception.class)
  public Map<String, Object> updateReconciliation(String stockTakeId,
      Map<String, Object> modelMap) {
    Map<String, Object> errorMap = new HashMap<>();
    return updateStockTakeItems(StockTakeAction.RECONCILE, stockTakeId,
        modelMap, errorMap);
  }

  /**
   * Reopen a stock take for counting again.
   * 
   * @param stockTakeId
   *          the stock take id
   * @return stock take bean after reopening
   */
  @Transactional(rollbackFor = Exception.class)
  public BasicDynaBean cancelStockTake(String stockTakeId) {
    return updateStatus(stockTakeId, null, // cancel can be initiated at any stage
        StockTakeStatus.CANCELLED);
  }

  
  private BasicDynaBean updateStatus(String stockTakeId,
      StockTakeStatus currentStatus, StockTakeStatus newStatus) {
    BasicDynaBean updateBean = stockTakeRepository.findByKey("stock_take_id",
        stockTakeId);
    if (null == updateBean) {
      throw new ValidationException("exception.stock.take.invalid");
    }

    if (null != currentStatus) {
      if (!((String) updateBean.get("status"))
          .equals(currentStatus.getCode())) {
        throw new ValidationException("exception.stock.take.action.invalid");
      }
    }

    if (null == newStatus) {
      throw new ValidationException("exception.stock.take.status.invalid");
    }

    final String user = RequestContext.getUserName();
    final java.sql.Timestamp currentTime = new java.sql.Timestamp(
        new java.util.Date().getTime());

    Map<String, Object> keys = new HashMap<>();
    keys.put("stock_take_id", stockTakeId);
    if (null != currentStatus) {
      keys.put("status", currentStatus.getCode());
    }

    updateBean.set("status", newStatus.getCode());
    updateBean.set("user_name", user);
    updateBean.set("mod_time", currentTime);

    if (newStatus == StockTakeStatus.APPROVED) {
      updateBean.set("approved_by", user);
      updateBean.set("approved_datetime", currentTime);
    }

    if (newStatus == StockTakeStatus.RECONCILED) {
      updateBean.set("reconciled_by", user);
      updateBean.set("reconciled_datetime", currentTime);
    }

    if (newStatus == StockTakeStatus.COMPLETED) {
      updateBean.set("completed_by", user);
      updateBean.set("completed_datetime", currentTime);
    }
    Integer updateCount = stockTakeRepository.update(updateBean, keys);

    if (0 == updateCount) {
      throw new ValidationException("exception.stock.take.action.failed");
    }

    return updateBean;
  }

  private Map<String, Object> updateStockTakeItems(StockTakeAction action,
      String stockTakeId, Map<String, Object> modelMap,
      Map<String, Object> errorMap) {

    boolean isValid = true;

    // Get a map of all item batches with their current stock qty,
    // previously recorded system stock qty if any and recorded time, so that
    // the
    // incoming data can be compared against these to timestamp and update the
    // system stock quantities appropriately.
    List<BasicDynaBean> stockQtyList = stockTakeRepository
        .getStockQuantities(stockTakeId);
    Map stockQtyMap = ConversionUtils.listBeanToMapMap(stockQtyList,
        "item_batch_id");
    BasicDynaBean stockTakeBean = stockTakeRepository.findByKey("stock_take_id",
        stockTakeId);

    List<BasicDynaBean> insertBeans = Collections.emptyList();
    List<BasicDynaBean> updateBeans = Collections.emptyList();
    Map<String, Object> responseData = new HashMap<>();
    if (modelMap.get("insert") != null) {
      // logger.error("Inside Insert");
      responseData.put("insert", new HashMap<String, Object>());
      List<Map<String, Object>> insertList = (List<Map<String, Object>>) modelMap
          .get("insert");
      insertBeans = processInserts(stockQtyMap, stockTakeBean, insertList,
          responseData, errorMap);
    }
    Map<String, Object> updateKeysMap = new HashMap<>();
    List<Object> updateKeys = new ArrayList<>();
    if (modelMap.get("update") != null) {
      // logger.error("Inside Update");
      responseData.put("update", new HashMap<String, Object>());
      List<Map<String, Object>> updateList = (List<Map<String, Object>>) modelMap
          .get("update");
      updateBeans = processUpdates(action, stockQtyMap, stockTakeBean,
          updateList, updateKeys, responseData, errorMap);
    }

    if (!insertBeans.isEmpty()) {
      stockTakeDetailRepository.batchInsert(insertBeans);
    }
    if (!updateBeans.isEmpty()) {
      updateKeysMap.put("stock_take_detail_id", updateKeys);
      stockTakeDetailRepository.batchUpdate(updateBeans, updateKeysMap);
    }
    return responseData;
  }

  private List<BasicDynaBean> processInserts(
      Map<Integer, Map<String, Object>> stockQtyMap,
      BasicDynaBean stockTakeBean, List<Map<String, Object>> insertList,
      Map<String, Object> responseData, Map<String, Object> errorMap) {

    List<BasicDynaBean> insertBeans = new ArrayList<>();
    Integer recordIndex = 0;

    for (Map<String, Object> dataRow : insertList) {
      ValidationErrorMap errMap = new ValidationErrorMap();
      BasicDynaBean itemBean = stockTakeDetailRepository.getBean();
      if (populateInsertBean(stockQtyMap, stockTakeBean, itemBean, dataRow,
          errMap) && validateBean(itemBean, errMap)) {
        insertBeans.add(itemBean);
        updateResponseData(responseData, recordIndex, "insert", itemBean);
      } else {
        processInsertErrors(errorMap, recordIndex, errMap);
      }
      recordIndex++;
    }
    return insertBeans;

  }

  private List<BasicDynaBean> processUpdates(StockTakeAction action,
      Map<Integer, Map<String, Object>> stockQtyMap,
      BasicDynaBean stockTakeBean, List<Map<String, Object>> updateList,
      List<Object> updateKeys, Map<String, Object> responseData,
      Map<String, Object> errorMap) {
    List<BasicDynaBean> updateBeans = new ArrayList<>();
    Integer recordIndex = 0;
    for (Map<String, Object> dataRow : updateList) {
      ValidationErrorMap errMap = new ValidationErrorMap();
      BasicDynaBean itemBean = stockTakeDetailRepository.getBean();
      if (populateUpdateBean(stockQtyMap, stockTakeBean, itemBean, dataRow,
          errMap) && validateBean(itemBean, errMap)) {
        updateBeans.add(itemBean);
        updateKeys.add(itemBean.get("stock_take_detail_id"));
        updateResponseData(responseData, recordIndex, "update", itemBean);
      } else {
        processUpdateErrors(errorMap, recordIndex, errMap);
      }
      recordIndex++;
    }
    return updateBeans;
  }

  private boolean populateInsertBean(Map<Integer, Map<String, Object>> stockQtyMap, 
      BasicDynaBean stockTakeBean,
      BasicDynaBean bean, Map<String, Object> data, ValidationErrorMap errMap) {
    List<String> conversionErrorList = new ArrayList<>();
    String userName = (String) RequestContext.getUserName();
    // This should set the fields, item_batch_id, physical_qtock_qty,
    // stock_adjustment_reason_id
    ConversionUtils.copyJsonToDynaBean(data, bean, conversionErrorList, false);
    // The remaining parameters ...
    if (!conversionErrorList.isEmpty()) {
      ValidationUtils.copyCoversionErrors(errMap, conversionErrorList);
      return false;
    } else {
      bean.set("stock_take_detail_id",
          stockTakeDetailRepository.getNextSequence());
      bean.set("stock_take_id", stockTakeBean.get("stock_take_id"));
      bean.set("system_stock_qty",
          getCurrentSystemStock(stockQtyMap, (Integer) bean.get("item_batch_id")));
      bean.set("recorded_datetime",
          new java.sql.Timestamp(new java.util.Date().getTime()));
      bean.set("user_name", userName);
      bean.set("mod_time",
          new java.sql.Timestamp(new java.util.Date().getTime()));
      return true;
    }
  }

  private boolean populateUpdateBean(
      Map<Integer, Map<String, Object>> stockQtyMap,
      BasicDynaBean stockTakeBean, BasicDynaBean bean, Map<String, Object> data,
      ValidationErrorMap errMap) {
    List<String> conversionErrorList = new ArrayList<>();
    String userName = (String) RequestContext.getUserName();
    // This should set the fields, item_batch_id, physical_qtock_qty,
    // stock_adjustment_reason_id
    ConversionUtils.copyJsonToDynaBean(data, bean, conversionErrorList, false);
    // The remaining parameters ...
    if (!conversionErrorList.isEmpty()) {
      ValidationUtils.copyCoversionErrors(errMap, conversionErrorList);
      return false;
    } else {
      Integer itemBatchId = (Integer) bean.get("item_batch_id");
      Map stockBeanMap = (null != itemBatchId && null != stockQtyMap)
          ? stockQtyMap.get(itemBatchId) : null;
      BigDecimal currentQty = BigDecimal.ZERO;
      BigDecimal recordedQty = null;
      java.sql.Timestamp recordedTime = null;

      // Check if the user has modified the physical stock qty
      if (null != bean.get("physical_stock_qty")
          && null != data.get("_current_physical_stock_qty")
          && (0 != ((BigDecimal) bean.get("physical_stock_qty"))
              .compareTo(new BigDecimal(
                  (data.get("_current_physical_stock_qty")).toString())))) {
        
        // If so, set the system stock to current stock, recorded time to
        // current time
        // if system stock is available, bail out to 0 otherwise.
        if (null != stockBeanMap) {
          currentQty = (BigDecimal) stockBeanMap
              .get("current_system_stock_qty");
        }
        bean.set("system_stock_qty",
            (null != currentQty) ? currentQty : BigDecimal.ZERO);
        bean.set("recorded_datetime",
            new java.sql.Timestamp(new java.util.Date().getTime()));
      } else {
        // Else, set the system stock to the same value as what is recorded
        // previously
        // along with the previous recorded time.
        if (null != stockBeanMap) {
          recordedQty = (BigDecimal) stockBeanMap
              .get("recorded_system_stock_qty");
          recordedTime = (java.sql.Timestamp) stockBeanMap
              .get("recorded_datetime");
        }
        bean.set("system_stock_qty",
            (null != recordedQty) ? recordedQty : BigDecimal.ZERO);
        bean.set("recorded_datetime", (null != recordedTime) ? recordedTime
            : new java.sql.Timestamp(new java.util.Date().getTime()));
      }
      bean.set("user_name", userName);
      bean.set("mod_time",
          new java.sql.Timestamp(new java.util.Date().getTime()));
      return true;
    }
  }

  private BigDecimal getCurrentSystemStock(
      Map<Integer, Map<String, Object>> stockQtyMap, Integer itemBatchId) {
    Map<String, Object> stockBeanMap = stockQtyMap.get(itemBatchId);
    if (null != stockBeanMap
        && stockBeanMap.containsKey("current_system_stock_qty")) {
      return (BigDecimal) stockBeanMap.get("current_system_stock_qty");
    }
    return null;
  }

  private boolean validateBean(BasicDynaBean bean, ValidationErrorMap errMap) {
    String[] nonNullFields = new String[] { "stock_take_id", "item_batch_id",
        "stock_take_detail_id" };
    boolean isValid = true;
    if (null != bean) {
      for (String field : nonNullFields) {
        if (null == bean.get(field)) {
          errMap.addError(field, "exception.stock.take.not.null");
          isValid = false;
        }
      }
    }
    return isValid;
  }

  private void updateResponseData(Map<String, Object> responseData,
      Integer recordIndex, String opKey, BasicDynaBean itemBean) {
    Map<String, Object> record = new HashMap<>();
    record.put("stock_take_detail_id", itemBean.get("stock_take_detail_id"));
    ((Map<String, Object>) responseData.get(opKey))
        .put(recordIndex.toString(), record);
  }

  private void processUpdateErrors(Map<String, Object> errorMap,
      Integer recordIndex, ValidationErrorMap errMap) {
    processErrors("update", errorMap, recordIndex, errMap);
  }

  private void processInsertErrors(Map<String, Object> errorMap,
      Integer recordIndex, ValidationErrorMap errMap) {
    processErrors("insert", errorMap, recordIndex, errMap);
  }

  private void processErrors(String errorKey, Map<String, Object> errorMap,
      Integer recordIndex, ValidationErrorMap errMap) {
    if (!errMap.getErrorMap().isEmpty()) {
      if (!errorMap.containsKey(errorKey)) {
        errorMap.put(errorKey, new HashMap<String, Object>());
      }
      ((Map<String, Object>) errorMap.get(errorKey)).put(
          (recordIndex).toString(),
          (new ValidationException(errMap)).getErrors());
    }
  }

  private boolean isCountComplete(String stockTakeId,
      boolean forceCompleteZeroStock, Map<String, Object> errorMap) {
    List<BasicDynaBean> uncountedBeans = stockTakeRepository
        .getUncountedItems(stockTakeId);
    if (null == uncountedBeans || uncountedBeans.isEmpty()) {
      return true;
    }
    errorMap.put("incomplete_items",
        ConversionUtils.listBeanToListMap(uncountedBeans));
    return false;
  }

  private boolean isReconciliationComplete(String stockTakeId,
      Map<String, Object> errorMap) {
    List<BasicDynaBean> unreconciledBeans = stockTakeRepository
        .getUnreconciledItems(stockTakeId);
    if (null == unreconciledBeans || unreconciledBeans.isEmpty()) {
      return true;
    }
    errorMap.put("incomplete_items",
        ConversionUtils.listBeanToListMap(unreconciledBeans));
    return false;
  }

  /**
   * Fetch the data to be used in the FTL template while printing.
   * 
   * @param stockTakeId
   *          the Stock Take ID
   * @param filterMap
   *          filter parameters used for filtering the items to be printed
   * @param action
   *          differentiator which indicates the parent action which initiated
   *          the print. Valid values are those corresponding to
   *          {@link StockTakeAction} enumeration strings as returned by the
   *          corresponding toString() methods.
   * @return Map containing the stock take data used in FTL processing
   */
  public Map<String, Object> getPrintData(String stockTakeId,
      Map<String, String[]> filterMap, String action) {

    Map<String, Object> templateDataMap = new HashMap<String, Object>();
    BasicDynaBean stockTakeBean = null;
    boolean extendedData = false;
    if (null != stockTakeId && !stockTakeId.isEmpty()) {
      stockTakeBean = stockTakeRepository.findByKey("stock_take_id",
          stockTakeId);
      templateDataMap.put("stock_take", stockTakeBean);
      if (null != stockTakeBean) {
        BasicDynaBean storeBean = storeService
            .findByStore((Integer) stockTakeBean.get("store_id"));
        templateDataMap.put("store", storeBean);
      }

      if (null != action && !action.isEmpty()) {
        extendedData = StockTakeAction
            .valueOf(action.toUpperCase()) != StockTakeAction.COUNT;
        templateDataMap.put("extended_columns", extendedData ? "Y" : "N");
      }
      PagedList stockTakeItems = searchItems(filterMap, extendedData);
      templateDataMap.put("stock_take_details", stockTakeItems.getDtoList());
    }
    return templateDataMap;
  }

}

