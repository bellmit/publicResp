package com.insta.hms.core.inventory.stocks;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.mdm.item.StoreItemDetailsRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StoreStockDetailsService {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(StoreStockDetailsService.class);

  @LazyAutowired
  StoreStockDetailsRepository storeStockDetailsRepository;

  @LazyAutowired
  StoreItemDetailsRepository storeItemDetailsRepository;

  /** The session service. */
  @LazyAutowired
  SessionService sessionService;

  public boolean isQuantityAvailable(Integer storeId, Integer itemId, int identifier,
      BigDecimal returnQuantity) {
    BasicDynaBean qtyAvailableBean = storeStockDetailsRepository.getQuantity(storeId, itemId, identifier,
        returnQuantity);
    return (qtyAvailableBean != null && qtyAvailableBean.get("qty") != null);
  }

  public BasicDynaBean getItemDetails(Integer visitStoreRatePlanId, Integer storeRatePlanId,
      String batchNo, Integer medicine_id) {
    return storeStockDetailsRepository.getItemDetails(visitStoreRatePlanId, storeRatePlanId,
        batchNo, medicine_id);
  }

  public BasicDynaBean getPackageMrpAndCP(Integer medicineId, String batchNo) {
    return storeStockDetailsRepository.getPackageMrpAndCP(medicineId, batchNo);
  }
  
  public List<BasicDynaBean> getItemQtyForBatch(int medicineId, int storeId, String batchNo) {
    return storeStockDetailsRepository.getItemQtyForBatch(medicineId, storeId, batchNo);
  }
  
  public BigDecimal getAvailableItemCountForBatchAndStore(int medicineId, int storeId,
      String batchNo) {
    return storeStockDetailsRepository.getAvailableItemCountForBatchAndStore(medicineId, storeId,
        batchNo);
  }
  
  public BasicDynaBean findByKey(Map<String, Object> filterMap) {
    return storeStockDetailsRepository.findByKey(filterMap);
  }
  
  public boolean updateStock(int deptIdTo, int deptIdFrom, String batchNo, int medicineId) {
    return storeStockDetailsRepository.updateStock(deptIdTo, deptIdFrom, batchNo, medicineId);
  }

  /*
   * Returns the store id, store name and count of item batches
   * for all stores for the user.
   * 
   * Returns map which has listSize and storesList
   */
  public Map<String, Object> getStoresItemBatchCount(/*String userName*/) {
    List<BasicDynaBean> userStores = storeStockDetailsRepository.getItemBatchCount();
    Map<String, Object> storesMap = new HashMap<>();
    storesMap.put("listSize", userStores.size());
    List<Map<String, Object>> userStoresList = new ArrayList<>();
    if (!userStores.isEmpty()) {
      userStoresList = ConversionUtils.listBeanToListMap(userStores);
    }
    storesMap.put("storesList", userStoresList);
    Map<String, Object> ret = sessionService.getSessionAttributes(new String[]{"pharmacyStoreId"});
    Object val = ret.get("pharmacyStoreId");
    Integer defaultStoreId = null;
    if (null != val) {
      try {
        defaultStoreId = Integer.parseInt(val.toString());
      } catch (NumberFormatException ne) {
        // we just ignore it
        logger.error("Invalid default store id {} for user", val.toString());
      }
    }
    storesMap.put("default_store_id", defaultStoreId);
    return storesMap; 
  }

  public List<BasicDynaBean> searchMedicine(Integer storeId, String searchText) {
    return storeStockDetailsRepository.searchMedicinesInStock(storeId, searchText);
  }

  public List<BasicDynaBean> getAvailableLots(Integer storeId,
      boolean includeZeroLots, boolean includeNegativeLots) {
    return storeStockDetailsRepository.getItemLots(storeId, includeZeroLots,
        includeNegativeLots);
  }
}
