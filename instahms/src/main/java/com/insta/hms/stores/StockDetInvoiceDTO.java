package com.insta.hms.stores;

public class StockDetInvoiceDTO {
  private String medicineid;
  private String batchno;
  private String deptid;
  private String invoiceno;
  private String grnno;
  private String supplierid;
  private String suppliername;
  private int storeId;

  public String getBatchno() {
    return batchno;
  }

  public void setBatchno(String batchno) {
    this.batchno = batchno;
  }

  public String getDeptid() {
    return deptid;
  }

  public void setDeptid(String deptid) {
    this.deptid = deptid;
  }

  public String getGrnno() {
    return grnno;
  }

  public void setGrnno(String grnno) {
    this.grnno = grnno;
  }

  public String getInvoiceno() {
    return invoiceno;
  }

  public void setInvoiceno(String invoiceno) {
    this.invoiceno = invoiceno;
  }

  public String getMedicineid() {
    return medicineid;
  }

  public void setMedicineid(String medicineid) {
    this.medicineid = medicineid;
  }

  public String getSupplierid() {
    return supplierid;
  }

  public void setSupplierid(String supplierid) {
    this.supplierid = supplierid;
  }

  public String getSuppliername() {
    return suppliername;
  }

  public void setSuppliername(String suppliername) {
    this.suppliername = suppliername;
  }

  public int getStoreId() {
    return storeId;
  }

  public void setStoreId(int storeId) {
    this.storeId = storeId;
  }

}
