package com.insta.hms.stores;

/*
 * DTO to indicate a stock change (increment/decrement), for use in  passing a list
 * of changes to reduceStockEntries or addToStockEntries in MedicineStockDAO
 */

import java.math.BigDecimal;

public class StockChangeDTO {
  private String storeId;
  private String medicineId;
  private String batchNo;
  private BigDecimal changeQuantity;
  private int itemBatchId;
  private int itemLotId;

  private String username;
  private String change_source;

  public String getChange_source() {
    return change_source;
  }
  public void setChange_source(String change_source) {
    this.change_source = change_source;
  }
  public String getUsername() {
    return username;
  }
  public void setUsername(String username) {
    this.username = username;
  }
  public String getStoreId() {
    return storeId;
  }
  public void setStoreId(String v) {
    storeId = v;
  }

  public String getMedicineId() {
    return medicineId;
  }
  public void setMedicineId(String v) {
    medicineId = v;
  }

  public String getBatchNo() {
    return batchNo;
  }
  public void setBatchNo(String v) {
    batchNo = v;
  }

  public BigDecimal getChangeQuantity() {
    return changeQuantity;
  }
  public void setChangeQuantity(BigDecimal v) {
    changeQuantity = v;
  }
  public int getItemBatchId() {
    return itemBatchId;
  }
  public void setItemBatchId(int itemBatchId) {
    this.itemBatchId = itemBatchId;
  }
  public int getItemLotId() {
    return itemLotId;
  }
  public void setItemLotId(int itemLotId) {
    this.itemLotId = itemLotId;
  }

}
