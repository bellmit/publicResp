package com.insta.hms.stores;

import java.math.BigDecimal;

public class StockAdjustmentDTO {

  private int adjNo;
  private java.sql.Timestamp dateTime;
  private String storeId;
  private String username;
  private String reason;
  private String medicineId;
  private String batchNo;
  private String type;
  private BigDecimal qty;

  private String changeSource;

  public String getChangeSource() {
    return changeSource;
  }
  public void setChangeSource(String changeSource) {
    this.changeSource = changeSource;
  }
  public int getAdjNo() {
    return adjNo;
  }
  public void setAdjNo(int v) {
    this.adjNo = v;
  }

  public String getBatchNo() {
    return batchNo;
  }
  public void setBatchNo(String v) {
    this.batchNo = v;
  }

  public java.sql.Timestamp getDateTime() {
    return dateTime;
  }
  public void setDateTime(java.sql.Timestamp v) {
    this.dateTime = v;
  }

  public String getMedicineId() {
    return medicineId;
  }
  public void setMedicineId(String v) {
    this.medicineId = v;
  }

  public BigDecimal getQty() {
    return qty;
  }
  public void setQty(BigDecimal v) {
    this.qty = v;
  }

  public String getReason() {
    return reason;
  }
  public void setReason(String v) {
    this.reason = v;
  }

  public String getStoreId() {
    return storeId;
  }
  public void setStoreId(String v) {
    this.storeId = v;
  }

  public String getType() {
    return type;
  }
  public void setType(String v) {
    this.type = v;
  }

  public String getUsername() {
    return username;
  }
  public void setUsername(String v) {
    this.username = v;
  }

}
