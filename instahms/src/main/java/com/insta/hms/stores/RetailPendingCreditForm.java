package com.insta.hms.stores;

import org.apache.struts.action.ActionForm;

public class RetailPendingCreditForm extends ActionForm {

  private String patname;
  private String pageNum;
  private String sortOrder;
  private boolean sortReverse;
  private String startPage;
  private String endPage;
  private String fdate;
  private String tdate;
  private String billNo;
  private String custName;
  private String counterId;
  private String netPay;
  private String amountdue;
  private String amtPay;
  private String receiptType;
  private int paymentModeId;
  private int cardTypeId;
  private String paymentBank;
  private String paymentRefNum;
  private String paymentRemarks;
  private String collectDate;
  private String collectTime;
  private String customerId;
  private boolean close;
  private String paymentType;
  private String billRemarks;

  public String getEndPage() {
    return endPage;
  }
  public void setEndPage(String v) {
    this.endPage = v;
  }

  public String getFdate() {
    return fdate;
  }
  public void setFdate(String v) {
    this.fdate = v;
  }

  public String getPageNum() {
    return pageNum;
  }
  public void setPageNum(String v) {
    this.pageNum = v;
  }

  public String getPatname() {
    return patname;
  }
  public void setPatname(String v) {
    this.patname = v;
  }

  public String getSortOrder() {
    return sortOrder;
  }
  public void setSortOrder(String v) {
    this.sortOrder = v;
  }

  public boolean getSortReverse() {
    return sortReverse;
  }
  public void setSortReverse(boolean v) {
    this.sortReverse = v;
  }

  public String getStartPage() {
    return startPage;
  }
  public void setStartPage(String v) {
    this.startPage = v;
  }

  public String getTdate() {
    return tdate;
  }
  public void setTdate(String v) {
    this.tdate = v;
  }

  public String getBillNo() {
    return billNo;
  }
  public void setBillNo(String v) {
    this.billNo = v;
  }

  public String getCustName() {
    return custName;
  }
  public void setCustName(String v) {
    this.custName = v;
  }

  public String getAmountdue() {
    return amountdue;
  }
  public void setAmountdue(String v) {
    this.amountdue = v;
  }

  public String getAmtPay() {
    return amtPay;
  }
  public void setAmtPay(String v) {
    this.amtPay = v;
  }

  public String getCounterId() {
    return counterId;
  }
  public void setCounterId(String v) {
    this.counterId = v;
  }

  public String getNetPay() {
    return netPay;
  }
  public void setNetPay(String v) {
    this.netPay = v;
  }

  public String getPaymentBank() {
    return paymentBank;
  }
  public void setPaymentBank(String v) {
    this.paymentBank = v;
  }

  public int getPaymentModeId() {
    return paymentModeId;
  }
  public void setPaymentModeId(int v) {
    paymentModeId = v;
  }

  public int getCardTypeId() {
    return cardTypeId;
  }
  public void setCardTypeId(int v) {
    cardTypeId = v;
  }

  public String getPaymentRefNum() {
    return paymentRefNum;
  }
  public void setPaymentRefNum(String v) {
    this.paymentRefNum = v;
  }

  public String getPaymentRemarks() {
    return paymentRemarks;
  }
  public void setPaymentRemarks(String v) {
    this.paymentRemarks = v;
  }

  public String getReceiptType() {
    return receiptType;
  }
  public void setReceiptType(String v) {
    this.receiptType = v;
  }

  public String getCollectDate() {
    return collectDate;
  }
  public void setCollectDate(String v) {
    this.collectDate = v;
  }

  public String getCollectTime() {
    return collectTime;
  }
  public void setCollectTime(String v) {
    collectTime = v;
  }

  public String getCustomerId() {
    return customerId;
  }
  public void setCustomerId(String v) {
    this.customerId = v;
  }

  public String getPaymentType() {
    return paymentType;
  }
  public void setPaymentType(String v) {
    this.paymentType = v;
  }

  public boolean isClose() {
    return close;
  }
  public void setClose(boolean v) {
    this.close = v;
  }

  public String getBillRemarks() {
    return billRemarks;
  }
  public void setBillRemarks(String billRemarks) {
    this.billRemarks = billRemarks;
  }
}
