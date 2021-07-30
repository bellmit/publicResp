package com.insta.hms.stores;

import java.math.BigDecimal;

public class SupplierReturnsDTO {

  private int return_no;
  private java.sql.Timestamp date_time;
  private String supplier_id;
  private String username;
  private String remarks;
  private String medicine_id;
  private String batch_no;
  private String return_type;
  private String invoiceNo;
  private BigDecimal qty;

  private String change_source;

  public String getChange_source() {
    return change_source;
  }
  public void setChange_source(String change_source) {
    this.change_source = change_source;
  }
  public String getBatch_no() {
    return batch_no;
  }
  public void setBatch_no(String v) {
    this.batch_no = v;
  }

  public java.sql.Timestamp getDate_time() {
    return date_time;
  }
  public void setDate_time(java.sql.Timestamp v) {
    this.date_time = v;
  }

  public String getMedicine_id() {
    return medicine_id;
  }
  public void setMedicine_id(String v) {
    this.medicine_id = v;
  }

  public BigDecimal getQty() {
    return qty;
  }
  public void setQty(BigDecimal v) {
    this.qty = v;
  }

  public String getRemarks() {
    return remarks;
  }
  public void setRemarks(String v) {
    this.remarks = v;
  }

  public int getReturn_no() {
    return return_no;
  }
  public void setReturn_no(int v) {
    this.return_no = v;
  }

  public String getReturn_type() {
    return return_type;
  }
  public void setReturn_type(String v) {
    this.return_type = v;
  }

  public String getSupplier_id() {
    return supplier_id;
  }
  public void setSupplier_id(String v) {
    this.supplier_id = v;
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
