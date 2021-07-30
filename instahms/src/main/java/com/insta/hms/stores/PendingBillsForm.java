package com.insta.hms.stores;

import org.apache.struts.action.ActionForm;

import java.math.BigDecimal;

public class PendingBillsForm extends ActionForm {

  private String fdate;
  private String tdate;
  private String mrno;
  private String billNo;
  private String patname;
  private String pageNum;
  private String sortOrder;
  private boolean sortReverse;
  private String startPage;
  private String endPage;
  private String counterId;
  private String saleId;
  private int paymentModeId;
  private int cardTypeId;
  private String paymentBank;
  private String paymentRefNum;
  private String paymentRemarks;
  private String payDate;
  private String payTime;
  private String grandtotal;
  private String depositSetOff;
  private int rewardPointsRedeemed;
  private BigDecimal rewardPointsRedeemedAmount;

  public String getBillNo() {
    return billNo;
  }
  public void setBillNo(String v) {
    this.billNo = v;
  }

  public String getFdate() {
    return fdate;
  }
  public void setFdate(String v) {
    this.fdate = v;
  }

  public String getMrno() {
    return mrno;
  }
  public void setMrno(String v) {
    this.mrno = v;
  }

  public String getPatname() {
    return patname;
  }
  public void setPatname(String v) {
    this.patname = v;
  }

  public String getTdate() {
    return tdate;
  }
  public void setTdate(String v) {
    this.tdate = v;
  }

  public String getEndPage() {
    return endPage;
  }
  public void setEndPage(String v) {
    this.endPage = v;
  }

  public String getPageNum() {
    return pageNum;
  }
  public void setPageNum(String v) {
    this.pageNum = v;
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

  public String getCounterId() {
    return counterId;
  }
  public void setCounterId(String v) {
    this.counterId = v;
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

  public String getSaleId() {
    return saleId;
  }
  public void setSaleId(String v) {
    this.saleId = v;
  }

  public String getGrandtotal() {
    return grandtotal;
  }
  public void setGrandtotal(String v) {
    this.grandtotal = v;
  }

  public String getDepositSetOff() {
    return depositSetOff;
  }
  public void setDepositSetOff(String depositSetOff) {
    this.depositSetOff = depositSetOff;
  }
  public String getPayDate() {
    return payDate;
  }
  public void setPayDate(String payDate) {
    this.payDate = payDate;
  }
  public String getPayTime() {
    return payTime;
  }
  public void setPayTime(String payTime) {
    this.payTime = payTime;
  }
  public int getRewardPointsRedeemed() {
    return rewardPointsRedeemed;
  }
  public void setRewardPointsRedeemed(int rewardPointsRedeemed) {
    this.rewardPointsRedeemed = rewardPointsRedeemed;
  }
  public BigDecimal getRewardPointsRedeemedAmount() {
    return rewardPointsRedeemedAmount;
  }
  public void setRewardPointsRedeemedAmount(BigDecimal rewardPointsRedeemedAmount) {
    this.rewardPointsRedeemedAmount = rewardPointsRedeemedAmount;
  }
}
