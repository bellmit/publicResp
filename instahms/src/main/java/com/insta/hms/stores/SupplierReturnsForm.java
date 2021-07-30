package com.insta.hms.stores;

import org.apache.struts.action.ActionForm;

public class SupplierReturnsForm extends ActionForm {

  private String supplier;
  private String remarks;
  private String returnType;
  private String username;
  private String invoiceNo;
  private String[] medicineId;
  private String[] batchNo;
  private String[] srQty;

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

  public String getRemarks() {
    return remarks;
  }
  public void setRemarks(String v) {
    this.remarks = v;
  }

  public String getReturnType() {
    return returnType;
  }
  public void setReturnType(String v) {
    this.returnType = v;
  }

  public String[] getSrQty() {
    return srQty;
  }
  public void setSrQty(String[] v) {
    this.srQty = v;
  }

  public String getSupplier() {
    return supplier;
  }
  public void setSupplier(String v) {
    this.supplier = v;
  }

  public String getUsername() {
    return username;
  }
  public void setUsername(String v) {
    this.username = v;
  }

  public String getInvoiceNo() {
    return invoiceNo;
  }
  public void setInvoiceNo(String v) {
    this.invoiceNo = v;
  }

}
