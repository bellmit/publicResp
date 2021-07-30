package com.insta.hms.core.inventory.stocks;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.item.StoreItemDetailsRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author anandpatel
 *
 */
@Service("stockFifoService")
@Transactional
public class StockFifoService {
  @LazyAutowired
  StoreStockDetailsRepository storeStockDetailsRepository;

  @LazyAutowired
  StoreItemDetailsRepository storeItemDetailsRepository;
  
  @LazyAutowired
  StoreTransactionLotDetailsRepository storeTransactionLotDetailsRepository;

  public Map reduceStock(int storeid, int itembatchId, String transactionType, BigDecimal qty,
      BigDecimal kitQty, String username, String changeSource, int transactionId) {
    return reduceStock(storeid, itembatchId, transactionType, qty, kitQty, username, changeSource,
        transactionId, false);
  }

  public Map reduceStock(int storeid, int itembatchId, String transactionType, BigDecimal qty,
      BigDecimal kitQty, String username, String changeSource, int transactionId,
      boolean allowNegative) {
    return reduceStock(storeid, itembatchId, transactionType, qty, kitQty, username, changeSource,
        transactionId, allowNegative, null);
  }

  public Map reduceStock(int storeid, int itembatchId, String transactionType, BigDecimal qty,
      BigDecimal kitQty, String username, String changeSource, int transactionId,
      boolean allowNegative, String purchaseType) {
    return reduceStock(storeid, itembatchId, transactionType, qty, kitQty, username, changeSource,
        transactionId, allowNegative, purchaseType, null);
  }

  /**
   * To reduce stock on FIFO base we need item_batch_id,store_id Stock can be reduced
   */
  public Map reduceStock(int storeid, int itembatchId, String transactionType, BigDecimal qty,
      BigDecimal kitQty, String username, String changeSource, int transactionId,
      boolean allowNegative, String purchaseType, String grnNo) {

    HashMap<String, Object> lotKeys = new HashMap<>();
    lotKeys.put("dept_id", storeid);
    lotKeys.put("item_batch_id", itembatchId);
    List<BasicDynaBean> itemStock = null;
    // R.C HMS-7205.logic of fetching batches shd be similar in case of grnNo != null case as well
    if (grnNo != null) {
      itemStock = purchaseType != null
          ? getBatchLotDetails(storeid, itembatchId, purchaseType, grnNo)
          : storeStockDetailsRepository.findByCriteria(lotKeys, "item_lot_id");
    } else {
      itemStock = purchaseType != null ? getBatchLotDetails(storeid, itembatchId, purchaseType)
          : storeStockDetailsRepository.findByCriteria(lotKeys, "item_lot_id");
    }

    String qtyKey = (kitQty != null ? "qty_kit" : "qty");
    BigDecimal remainingQty = (kitQty != null ? kitQty : qty);
    BigDecimal totalCostValue = BigDecimal.ZERO;
    Map statusMap = new HashMap();
    int medicine_id = 0;
    for (int i = 0; i < itemStock.size(); i++) {

      BasicDynaBean stockDetails = itemStock.get(i);

      if (remainingQty.compareTo(BigDecimal.ZERO) <= 0)
        break;

      BigDecimal stockQty = (BigDecimal) stockDetails.get("qty");
      BigDecimal transactionQty = BigDecimal.ZERO;
      medicine_id = (Integer) stockDetails.get("medicine_id");
      if (stockQty.compareTo(remainingQty) > 0) {
        // no problem -- use up all what we need.
        transactionQty = remainingQty;
      } else {
        if (allowNegative && (i == itemStock.size() - 1)) {
          // last lot, and we allow negative. We must use up remaining qty here itself.
          transactionQty = remainingQty;
        } else {
          transactionQty = stockQty;
          // try next lot
        }
      }

      if (transactionQty.compareTo(BigDecimal.ZERO) <= 0) {
        // no luck in this lot. Try next one.
        continue;
      }

      remainingQty = remainingQty.subtract(transactionQty);

      // update the stock details: reduce stock.
      addQtyToStockDetails((Integer) stockDetails.get("store_stock_id"), transactionQty.negate(),
          username, changeSource);

      // insert transaction details
      insertTxnLot(transactionId, transactionType, (Integer) stockDetails.get("item_lot_id"),
          transactionQty);

      BasicDynaBean lot = getItemLotDetails((Integer) stockDetails.get("item_lot_id"));
      // cost is the whole transaction's actual cost, so package_cp needs divide by package size.
      BigDecimal cost = transactionQty.multiply((BigDecimal) lot.get("package_cp"));
      cost = ConversionUtils.divideHighPrecision(cost, (BigDecimal) lot.get("issue_base_unit"));
      totalCostValue = totalCostValue.add(cost);
    }

    if (remainingQty.compareTo(BigDecimal.ZERO) > 0 && !allowNegative) {
      statusMap.put("status", false);
      statusMap.put("statusReason", "Insufficient Quantity ");
      statusMap.put("left_qty", remainingQty);
      List<BasicDynaBean> medicineList = storeItemDetailsRepository.listAll(null, "medicine_id", medicine_id);
      if(!medicineList.isEmpty())
      statusMap.put("medicine_name",
          (medicineList).get(0)
              .get("medicine_name").toString());
    } else {
      statusMap.put("status", true);
      statusMap.put("costValue", totalCostValue);
    }

    return statusMap;
  }

  public List<BasicDynaBean> getBatchLotDetails(int storeId, int itemBatchId) {
    return storeStockDetailsRepository.getBatchLotDetails(storeId, itemBatchId);
  }

  public List<BasicDynaBean> getBatchLotDetails(int storeId, int itemBatchId, String purchaseType) {
    return storeStockDetailsRepository.getBatchLotDetails(storeId, itemBatchId, purchaseType);
  }

  public List<BasicDynaBean> getBatchLotDetails(int storeId, int itemBatchId, String purchaseType,
      String grnNo) {
    return storeStockDetailsRepository.getBatchLotDetails(storeId, itemBatchId, purchaseType,
        grnNo);
  }

  public int insertTxnLot(int txnId, String txnType, int itemLotId, BigDecimal qty) {
    return storeStockDetailsRepository.insertTxnLot(txnId, txnType, itemLotId, qty);
  }

  private int addQtyToStockDetails(int stockId, BigDecimal qtyToAdd, String userName,
      String changeSource, boolean transitOnly) {
    return storeStockDetailsRepository.addQtyToStockDetails(stockId, qtyToAdd, userName,
        changeSource, transitOnly);
  }

  private int addQtyToStockDetails(int stockId, BigDecimal qtyToAdd, String userName,
      String changeSource) {
    return addQtyToStockDetails(stockId, qtyToAdd, userName, changeSource, false);
  }

  public boolean updateStockTimeStamp() {
    return storeStockDetailsRepository.updateStockTimestamp() > 0;
  }

  public boolean updateStoresStockTimeStamp(int storeId) {
    return storeStockDetailsRepository.updateStoresStockTimeStamp(storeId) > 0;
  }

  public BasicDynaBean getItemLotDetails(int itemLotId) {
    return storeItemDetailsRepository.getItemLotDetails(itemLotId);
  }
  
  /**
   * Returning back  items to stock which requires item lot id which was used while reducing the stock.
   * Note: this works only if there is a transaction that is being reversed. For fresh stock addition,
   * this should not be used.
   *
   * If itemBatchId is given, it means that new lots will be inserted instead of updating the qty
   * of an existing lot. This is used for supplier replacements.
   */
  
  public Map<String, Object> addStock(int storeId, int transactionId, String transactionType, BigDecimal qty,
      String userName, String changeSource, Integer refStore) {
    return addStock(storeId, transactionId, transactionType, qty, userName, changeSource, refStore,
        null, 0);

  }
  
  public Map<String, Object> addStock(int storeId, int transactionId, String transactionType, BigDecimal qty,
      String userName, String changeSource, Integer refStore, String returntransactionType,
 int returnTransactionId) {
    Map<String, Object> statusMap = new HashMap<>();
    boolean status = true;
    BigDecimal totalCostValue = BigDecimal.ZERO;
    List<BasicDynaBean> usedLots = getUsedLots(transactionId, transactionType);
    BigDecimal transactionQty = qty;
    BigDecimal remainingQty = transactionQty;
    statusMap.put("transaction_lot_exists", (usedLots.isEmpty()));
    for (BasicDynaBean usedLot : usedLots) {
      int itemLotId = (Integer) usedLot.get("item_lot_id");
      BigDecimal lotQty = (BigDecimal) usedLot.get("qty");

      if (remainingQty.compareTo(BigDecimal.ZERO) == 0)
        break;

      if (lotQty.compareTo(BigDecimal.ZERO) == 0)
        continue;

      transactionQty = (remainingQty.compareTo(lotQty) > 0) ? lotQty : remainingQty;

      remainingQty = remainingQty.subtract(transactionQty);

      HashMap<String, Object> stockKeys = new HashMap<>();
      stockKeys.put("item_lot_id", itemLotId);
      stockKeys.put("dept_id", storeId);
      BasicDynaBean stockBean = storeStockDetailsRepository.findByKey(stockKeys);

      /**
       * possible in case transfer is happening and same batch exists in the to store but not this
       * lot.Need to insert it from ref store lot.
       */
      if (stockBean == null && refStore != null) {
        // get stock from reference store.
        stockKeys = new HashMap<>();
        stockKeys.put("item_lot_id", itemLotId);
        stockKeys.put("dept_id", refStore);
        stockBean = storeStockDetailsRepository.findByKey(stockKeys);

        transferStock(storeId, transactionQty, "StockTransfer", userName, stockBean);

      } else {
        // update the stock details: add stock.
        addQtyToStockDetails((Integer) stockBean.get("store_stock_id"), transactionQty, userName,
            changeSource);
      }

      // insert transaction lot (negative of original transaction)
      insertTxnLot(transactionId, transactionType, itemLotId, transactionQty.negate());
      if (returntransactionType != null) {
        // few transaction may do not need to track return transactions
        // insert transaction lot (negative of original transaction)
        insertTxnLot(returnTransactionId, returntransactionType, itemLotId,
            transactionQty.negate());
      }

      BasicDynaBean lot = getItemLotDetails(itemLotId);
      // cost is the whole transaction's actual cost, so package_cp needs divide by package size.
      BigDecimal cost = transactionQty.multiply((BigDecimal) lot.get("package_cp"));
      cost = ConversionUtils.divideHighPrecision(cost, (BigDecimal) lot.get("issue_base_unit"));
      totalCostValue = totalCostValue.add(cost);

    }

    statusMap.put("status", status);
    statusMap.put("costValue", totalCostValue);
    return statusMap;
  }
  
  private boolean transferStock(int storeId, BigDecimal qty, String chnageSource,
      String userName, BasicDynaBean existStock) {
    return transferStock(storeId, qty, chnageSource, userName, existStock, false);
  }
  
  private boolean transferStock(int storeId, BigDecimal qty, String chnageSource,
 String userName,
      BasicDynaBean existStock, boolean toTransit) {
    existStock.set("dept_id", storeId);
    if (toTransit) {
      existStock.set("qty_in_transit", qty);
      existStock.set("qty", BigDecimal.ZERO);
    } else {
      existStock.set("qty_in_transit", BigDecimal.ZERO);
      existStock.set("qty", qty);
    }
    existStock.set("change_source", chnageSource);
    existStock.set("username", userName);
    existStock.set("store_stock_id", DatabaseHelper.getNextSequence("store_stock_details"));

    return storeStockDetailsRepository.insert(existStock) > 0;
  }
  
  /**
   * Can can transafer stock fromStore to toStore on FIFO.
   * @param medicineId the medicineId
   * @param itemBatchId the itemBatchId
   * @param fromStore the fromStore
   * @param toStore the toStore
   * @param qty the qty
   * @param userName the userName
   * @param transactionId the transactionId
   * @param receiveprocess the receiveprocess
   * @return map
   */
  public Map<String, Object> transferStock(int medicineId, int itemBatchId, int fromStore,
      int toStore, BigDecimal qty, String userName, int transactionId, String receiveprocess) {
    boolean status = true;
    BasicDynaBean lotTxnBean = null;
    Map statusMap = new HashMap();

    List<BasicDynaBean> fromStockList = null;
    BigDecimal transactionQty = qty;
    BigDecimal remainingQty = transactionQty;
    BigDecimal totalCostValue = BigDecimal.ZERO;

    Map<String, Object> stockKeys = new HashMap<>();
    stockKeys.put("dept_id", fromStore);
    stockKeys.put("medicine_id", medicineId);
    stockKeys.put("item_batch_id", itemBatchId);
    fromStockList = storeStockDetailsRepository.listAll(null,stockKeys,"item_lot_id");
    for(BasicDynaBean fromStock : fromStockList ){
      if ( (BigDecimal)fromStock.get("qty") == BigDecimal.ZERO || remainingQty == BigDecimal.ZERO )
        continue;
      transactionQty = ( remainingQty.compareTo(BigDecimal.ZERO) > 0 && remainingQty.compareTo((BigDecimal)fromStock.get("qty")) > 0 )
                    ? (BigDecimal)fromStock.get("qty") : remainingQty;
      remainingQty = remainingQty.subtract(transactionQty);
      Map reduceKeys = new HashMap();
      reduceKeys.put("store_stock_id", fromStock.get("store_stock_id"));

      fromStock.set("qty", ((BigDecimal)fromStock.get("qty")).subtract(transactionQty));
      status &= storeStockDetailsRepository.update(fromStock,reduceKeys) > 0;//reduce from from store


      //lot details of reducing item
      lotTxnBean = storeTransactionLotDetailsRepository.getBean();
      lotTxnBean.set("transaction_id", transactionId);
      lotTxnBean.set("transaction_type", "T");
      lotTxnBean.set("item_lot_id", fromStock.get("item_lot_id"));
      lotTxnBean.set("qty", transactionQty);

      //record transaction
      status = storeTransactionLotDetailsRepository.insert(lotTxnBean)>0;
      
      fromStock.set("dept_id", toStore);

      if (receiveprocess.equalsIgnoreCase("N")){
        fromStock.set("qty", transactionQty);
      } else{
        fromStock.set("qty", BigDecimal.ZERO);
        fromStock.set("qty_in_transit", transactionQty);
      }
      fromStock.set("change_source","StockTransfer");
      fromStock.set("username", userName);
      fromStock.set("store_stock_id", DatabaseHelper.getNextSequence("store_stock_details"));

      status &= storeStockDetailsRepository.insert(fromStock)>0;//transfer
      BasicDynaBean lot = getItemLotDetails((Integer)fromStock.get("item_lot_id"));
      // cost is the whole transaction's actual cost, so package_cp needs divide by package size.
      BigDecimal cost = transactionQty.multiply((BigDecimal)lot.get("package_cp"));
      cost = ConversionUtils.divideHighPrecision(cost, (BigDecimal)lot.get("issue_base_unit"));
      totalCostValue = totalCostValue.add(cost);     
      
    }
    if (remainingQty.compareTo(BigDecimal.ZERO) > 0) {
      statusMap.put("status", false);
      statusMap.put("statusReason", "Insufficient Quantity ");
      statusMap.put("left_qty", remainingQty);
    } else {
      statusMap.put("status", true);
      statusMap.put("costValue", totalCostValue);
    }

    return statusMap;
  }
  
  public Map<String, Object> addStockByLot(int storeId, int itemLotId, String transactionType,
      BigDecimal qty, String userName, String changeSource, int transactionId, String remarks) {
    Map<String, Object> statusMap = new HashMap<>();
    boolean status = true;
    BigDecimal totalCostValue = BigDecimal.ZERO;

    HashMap<String, Object> stockKeys = new HashMap<>();
    stockKeys.put("item_lot_id", itemLotId);
    stockKeys.put("dept_id", storeId);
    // this is actual stock of that item batch,store,lot
    BasicDynaBean stockBean = storeStockDetailsRepository.findByKey(stockKeys);
    stockBean.set("qty", ((BigDecimal) stockBean.get("qty")).add(qty));
    stockBean.set("username", userName);
    stockBean.set("change_source", changeSource);
    stockBean.set("item_lot_id", itemLotId);
    stockBean.set("remarks_avbl", remarks);
    status = storeStockDetailsRepository.update(stockBean, stockKeys) > 0;

    BasicDynaBean lotTxnBean = storeTransactionLotDetailsRepository.getBean();
    lotTxnBean.set("transaction_id", transactionId);
    lotTxnBean.set("transaction_type", transactionType);
    lotTxnBean.set("item_lot_id", itemLotId);
    lotTxnBean.set("qty", qty.negate());
    status &= storeTransactionLotDetailsRepository.insert(lotTxnBean) > 0;

    BasicDynaBean lot = getItemLotDetails(itemLotId);

    // cost is the whole transaction's actual cost, so package_cp needs divide by package size.
    BigDecimal cost = qty.multiply((BigDecimal) lot.get("package_cp"));
    cost = ConversionUtils.divideHighPrecision(cost, (BigDecimal) lot.get("issue_base_unit"));
    totalCostValue = totalCostValue.add(cost);

    statusMap.put("status", status);
    statusMap.put("costValue", totalCostValue.negate());

    return statusMap;
  }

  public Map<String, Object> reduceStockByLot(int storeid, int itemLotId, String transactionType,
      BigDecimal qty, String username, String changeSource, int transactionId, String remarks) {
    BasicDynaBean lotTxnBean = null;

    Map<String, Object> lotKeys = new HashMap<>();
    lotKeys.put("dept_id", storeid);
    lotKeys.put("item_lot_id", itemLotId);
    BasicDynaBean stockBean = storeStockDetailsRepository.findByKey(lotKeys);

    boolean status = true;
    BigDecimal totalCostValue = BigDecimal.ZERO;
    Map<String, Object> statusMap = new HashMap<>();
    BigDecimal txnQty = ((BigDecimal) stockBean.get("qty")).subtract(qty);

    if (txnQty.compareTo(BigDecimal.ZERO) < 0) {
      statusMap.put("status", false);
      statusMap.put("costValue", BigDecimal.ZERO);
      statusMap.put("statusReason", "Insufficient Quantity ");

      return statusMap;
    }

    stockBean.set("qty", txnQty);
    stockBean.set("username", username);
    stockBean.set("change_source", changeSource);
    stockBean.set("remarks_avbl", remarks);

    // reduce stock
    HashMap<String, Object> stockKeys = new HashMap<>();
    stockKeys.put("store_stock_id", stockBean.get("store_stock_id"));
    status = storeStockDetailsRepository.update(stockBean, stockKeys) > 0;

    lotTxnBean = storeTransactionLotDetailsRepository.getBean();
    lotTxnBean.set("transaction_id", transactionId);
    lotTxnBean.set("transaction_type", transactionType);
    lotTxnBean.set("item_lot_id", stockBean.get("item_lot_id"));
    lotTxnBean.set("qty", qty);
    status &= storeTransactionLotDetailsRepository.insert(lotTxnBean) > 0;

    BasicDynaBean lot = getItemLotDetails((Integer) stockBean.get("item_lot_id"));
    // cost is the whole transaction's actual cost, so package_cp needs divide by package size.
    BigDecimal cost = qty.multiply((BigDecimal) lot.get("package_cp"));
    cost = ConversionUtils.divideHighPrecision(cost, (BigDecimal) lot.get("issue_base_unit"));
    totalCostValue = totalCostValue.add(cost);

    statusMap.put("status", status);
    statusMap.put("costValue", totalCostValue);

    return statusMap;
  }
  
  private static final String STOCK_REDUCED_LOTS = " SELECT transaction_id, "
      + " transaction_type, sum(qty) as qty, item_lot_id" 
      + " FROM store_transaction_lot_details "
      + " WHERE transaction_id=? AND transaction_type=? "
      + " GROUP BY transaction_id, transaction_type, item_lot_id" 
      + " ORDER BY item_lot_id DESC ";

  public List<BasicDynaBean> getUsedLots(int txnId, String txnType) {
    return DatabaseHelper.queryToDynaList(STOCK_REDUCED_LOTS, new Object[] { txnId, txnType });
  }
}
