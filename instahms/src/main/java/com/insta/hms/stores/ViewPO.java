package com.insta.hms.stores;

import java.math.BigDecimal;
import java.sql.Date;

public class ViewPO {
  private String suppName;
  private String qutNo;
  private String pono;
  private Date poDate;
  private int grnCount;

  // viewinvoices screen variables
  private String invoiceNo;
  private Date invdate;
  private Date duetdate;
  private String status;
  private BigDecimal amount;
  private String suppId;
  private String userName;

  public String getSuppId() {
    return suppId;
  }
  public void setSuppId(String suppId) {
    this.suppId = suppId;
  }
  public String getQutNo() {
    return qutNo;
  }
  public void setQutNo(String qutNo) {
    this.qutNo = qutNo;
  }
  public String getSuppName() {
    return suppName;
  }
  public void setSuppName(String suppName) {
    this.suppName = suppName;
  }
  public String getPono() {
    return pono;
  }
  public void setPono(String pono) {
    this.pono = pono;
  }

  // view invocie variables

  public int getGrnCount() {
    return grnCount;
  }
  public void setGrnCount(int grnCount) {
    this.grnCount = grnCount;
  }
  public Date getPoDate() {
    return poDate;
  }
  public void setPoDate(Date poDate) {
    this.poDate = poDate;
  }
  public BigDecimal getAmount() {
    return amount;
  }
  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public Date getDuetdate() {
    return duetdate;
  }
  public void setDuetdate(Date duetdate) {
    this.duetdate = duetdate;
  }
  public Date getInvdate() {
    return invdate;
  }
  public void setInvdate(Date invdate) {
    this.invdate = invdate;
  }
  public String getInvoiceNo() {
    return invoiceNo;
  }
  public void setInvoiceNo(String invoiceNo) {
    this.invoiceNo = invoiceNo;
  }
  public String getStatus() {
    return status;
  }
  public void setStatus(String status) {
    this.status = status;
  }
  public String getUserName() {
    return userName;
  }
  public void setUserName(String userName) {
    this.userName = userName;
  }

}
