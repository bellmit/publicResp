package com.insta.hms.stores;

import org.apache.struts.action.ActionForm;

import java.math.BigDecimal;

public class AdjustmentForm extends ActionForm {

  private String store;
  private String reason;
  private String medicine;
  private String batch;
  private String adjtype;
  private String addAdjQty;
  private String[] medicineId;
  private String[] batchNo;
  private String[] adjustedtype;
  private BigDecimal[] adjustQty;
  private String currentStock[];

  public String getAddAdjQty() {
    return addAdjQty;
  }
  public void setAddAdjQty(String v) {
    this.addAdjQty = v;
  }

  public String getAdjtype() {
    return adjtype;
  }
  public void setAdjtype(String v) {
    this.adjtype = v;
  }

  public String getBatch() {
    return batch;
  }
  public void setBatch(String v) {
    this.batch = v;
  }

  public String getMedicine() {
    return medicine;
  }
  public void setMedicine(String v) {
    this.medicine = v;
  }

  public String getReason() {
    return reason;
  }
  public void setReason(String v) {
    this.reason = v;
  }

  public String getStore() {
    return store;
  }
  public void setStore(String v) {
    this.store = v;
  }

  public BigDecimal[] getAdjustQty() {
    return adjustQty;
  }
  public void setAdjustQty(BigDecimal[] v) {
    this.adjustQty = v;
  }

  public String[] getAdjustedtype() {
    return adjustedtype;
  }
  public void setAdjustedtype(String[] v) {
    this.adjustedtype = v;
  }

  public String[] getBatchNo() {
    return batchNo;
  }
  public void setBatchNo(String[] v) {
    this.batchNo = v;
  }

  public String[] getMedicineId() {
    return medicineId;
  }
  public void setMedicineId(String[] v) {
    this.medicineId = v;
  }

  public String[] getCurrentStock() {
    return currentStock;
  }
  public void setCurrentStock(String[] v) {
    this.currentStock = v;
  }

}
