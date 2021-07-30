package com.insta.hms.stores;

import org.apache.struts.action.ActionForm;

public class StockIssueForm extends ActionForm {

  private String from_store;
  private String to_store;
  private String reason;
  private String batch;
  private String issuQty;
  private String[] medicineId;
  private String[] medicineName;
  private String[] batchNo;
  private String[] issuedQty;
  private String[] packSp;
  private String[] taX;
  private String[] taxPer;
  private String[] expiry;
  private String[] mrp;
  private String[] packCp;
  private String[] taxType;
  private boolean[] deleted;

  public boolean[] getDeleted() {
    return deleted;
  }
  public void setDeleted(boolean[] deleted) {
    this.deleted = deleted;
  }
  public String getBatch() {
    return batch;
  }
  public void setBatch(String v) {
    this.batch = v;
  }

  public String[] getBatchNo() {
    return batchNo;
  }
  public void setBatchNo(String[] v) {
    this.batchNo = v;
  }

  public String getFrom_store() {
    return from_store;
  }
  public void setFrom_store(String v) {
    this.from_store = v;
  }

  public String[] getIssuedQty() {
    return issuedQty;
  }
  public void setIssuedQty(String[] v) {
    this.issuedQty = v;
  }

  public String getIssuQty() {
    return issuQty;
  }
  public void setIssuQty(String v) {
    this.issuQty = v;
  }

  public String[] getMedicineId() {
    return medicineId;
  }
  public void setMedicineId(String[] v) {
    this.medicineId = v;
  }

  public String getReason() {
    return reason;
  }
  public void setReason(String v) {
    this.reason = v;
  }

  public String getTo_store() {
    return to_store;
  }
  public void setTo_store(String v) {
    this.to_store = v;
  }

  public String[] getPackSp() {
    return packSp;
  }
  public void setPackSp(String[] v) {
    this.packSp = v;
  }

  public String[] getTaX() {
    return taX;
  }
  public void setTaX(String[] v) {
    this.taX = v;
  }

  public String[] getTaxPer() {
    return taxPer;
  }
  public void setTaxPer(String[] v) {
    this.taxPer = v;
  }

  public String[] getExpiry() {
    return expiry;
  }
  public void setExpiry(String[] v) {
    this.expiry = v;
  }

  public String[] getMrp() {
    return mrp;
  }
  public void setMrp(String[] v) {
    this.mrp = v;
  }

  public String[] getPackCp() {
    return packCp;
  }
  public void setPackCp(String[] v) {
    this.packCp = v;
  }

  public String[] getTaxType() {
    return taxType;
  }
  public void setTaxType(String[] v) {
    taxType = v;
  }
  public String[] getMedicineName() {
    return medicineName;
  }
  public void setMedicineName(String[] medicineName) {
    this.medicineName = medicineName;
  }

}
