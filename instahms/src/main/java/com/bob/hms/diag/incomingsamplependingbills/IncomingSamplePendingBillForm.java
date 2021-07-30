/**
 *
 */
package com.bob.hms.diag.incomingsamplependingbills;

import org.apache.struts.action.ActionForm;

/**
 * The Class IncomingSamplePendingBillForm.
 *
 * @author lakshmi.p
 */
public class IncomingSamplePendingBillForm extends ActionForm {

  /** The pat name. */
  private String patName;

  /** The patient id. */
  private String patientId;

  /** The page num. */
  private String pageNum;

  /** The sort order. */
  private String sortOrder;

  /** The sort reverse. */
  private boolean sortReverse;

  /** The start page. */
  private String startPage;

  /** The end page. */
  private String endPage;

  /** The fdate. */
  private String fdate;

  /** The tdate. */
  private String tdate;

  /** The bill no. */
  private String billNo;

  /** The counter id. */
  private String counterId;

  /** The net pay. */
  private String netPay;

  /** The amountdue. */
  private String amountdue;

  /** The amt pay. */
  private String amtPay;

  /** The receipt type. */
  private String receiptType;

  /** The payment mode id. */
  private int paymentModeId;

  /** The card type id. */
  private int cardTypeId;

  /** The payment bank. */
  private String paymentBank;

  /** The payment ref num. */
  private String paymentRefNum;

  /** The payment remarks. */
  private String paymentRemarks;

  /** The collect date. */
  private String collectDate;

  /** The collect time. */
  private String collectTime;

  /** The lab id. */
  private String labId;

  /** The lab name. */
  private String labName;

  /** The close. */
  private boolean close;

  /** The payment type. */
  private String paymentType;

  /** The pat other info. */
  private String patOtherInfo;

  /** The action. */
  private String action;

  /** The bill remarks. */
  private String billRemarks;

  /** The status all. */
  private boolean statusAll;

  /** The status open. */
  private boolean statusOpen;

  /** The status closed. */
  private boolean statusClosed;

  /** The type all. */
  private boolean typeAll;

  /** The type bill now. */
  private boolean typeBillNow;

  /** The type bill later. */
  private boolean typeBillLater;

  /** The bill type. */
  private String billType;

  /**
   * Gets the bill type.
   *
   * @return the bill type
   */
  public String getBillType() {
    return billType;
  }

  /**
   * Sets the bill type.
   *
   * @param billType the new bill type
   */
  public void setBillType(String billType) {
    this.billType = billType;
  }

  /**
   * Gets the end page.
   *
   * @return the end page
   */
  public String getEndPage() {
    return endPage;
  }

  /**
   * Sets the end page.
   *
   * @param v the new end page
   */
  public void setEndPage(String v) {
    this.endPage = v;
  }

  /**
   * Gets the fdate.
   *
   * @return the fdate
   */
  public String getFdate() {
    return fdate;
  }

  /**
   * Sets the fdate.
   *
   * @param v the new fdate
   */
  public void setFdate(String v) {
    this.fdate = v;
  }

  /**
   * Gets the page num.
   *
   * @return the page num
   */
  public String getPageNum() {
    return pageNum;
  }

  /**
   * Sets the page num.
   *
   * @param v the new page num
   */
  public void setPageNum(String v) {
    this.pageNum = v;
  }

  /**
   * Gets the sort order.
   *
   * @return the sort order
   */
  public String getSortOrder() {
    return sortOrder;
  }

  /**
   * Sets the sort order.
   *
   * @param v the new sort order
   */
  public void setSortOrder(String v) {
    this.sortOrder = v;
  }

  /**
   * Gets the sort reverse.
   *
   * @return the sort reverse
   */
  public boolean getSortReverse() {
    return sortReverse;
  }

  /**
   * Sets the sort reverse.
   *
   * @param v the new sort reverse
   */
  public void setSortReverse(boolean v) {
    this.sortReverse = v;
  }

  /**
   * Gets the start page.
   *
   * @return the start page
   */
  public String getStartPage() {
    return startPage;
  }

  /**
   * Sets the start page.
   *
   * @param v the new start page
   */
  public void setStartPage(String v) {
    this.startPage = v;
  }

  /**
   * Gets the tdate.
   *
   * @return the tdate
   */
  public String getTdate() {
    return tdate;
  }

  /**
   * Sets the tdate.
   *
   * @param v the new tdate
   */
  public void setTdate(String v) {
    this.tdate = v;
  }

  /**
   * Gets the bill no.
   *
   * @return the bill no
   */
  public String getBillNo() {
    return billNo;
  }

  /**
   * Sets the bill no.
   *
   * @param v the new bill no
   */
  public void setBillNo(String v) {
    this.billNo = v;
  }

  /**
   * Gets the amountdue.
   *
   * @return the amountdue
   */
  public String getAmountdue() {
    return amountdue;
  }

  /**
   * Sets the amountdue.
   *
   * @param v the new amountdue
   */
  public void setAmountdue(String v) {
    this.amountdue = v;
  }

  /**
   * Gets the amt pay.
   *
   * @return the amt pay
   */
  public String getAmtPay() {
    return amtPay;
  }

  /**
   * Sets the amt pay.
   *
   * @param v the new amt pay
   */
  public void setAmtPay(String v) {
    this.amtPay = v;
  }

  /**
   * Gets the counter id.
   *
   * @return the counter id
   */
  public String getCounterId() {
    return counterId;
  }

  /**
   * Sets the counter id.
   *
   * @param v the new counter id
   */
  public void setCounterId(String v) {
    this.counterId = v;
  }

  /**
   * Gets the net pay.
   *
   * @return the net pay
   */
  public String getNetPay() {
    return netPay;
  }

  /**
   * Sets the net pay.
   *
   * @param v the new net pay
   */
  public void setNetPay(String v) {
    this.netPay = v;
  }

  /**
   * Gets the payment bank.
   *
   * @return the payment bank
   */
  public String getPaymentBank() {
    return paymentBank;
  }

  /**
   * Sets the payment bank.
   *
   * @param v the new payment bank
   */
  public void setPaymentBank(String v) {
    this.paymentBank = v;
  }

  /**
   * Gets the payment mode id.
   *
   * @return the payment mode id
   */
  public int getPaymentModeId() {
    return paymentModeId;
  }

  /**
   * Sets the payment mode id.
   *
   * @param v the new payment mode id
   */
  public void setPaymentModeId(int v) {
    paymentModeId = v;
  }

  /**
   * Gets the card type id.
   *
   * @return the card type id
   */
  public int getCardTypeId() {
    return cardTypeId;
  }

  /**
   * Sets the card type id.
   *
   * @param v the new card type id
   */
  public void setCardTypeId(int v) {
    cardTypeId = v;
  }

  /**
   * Gets the payment ref num.
   *
   * @return the payment ref num
   */
  public String getPaymentRefNum() {
    return paymentRefNum;
  }

  /**
   * Sets the payment ref num.
   *
   * @param v the new payment ref num
   */
  public void setPaymentRefNum(String v) {
    this.paymentRefNum = v;
  }

  /**
   * Gets the payment remarks.
   *
   * @return the payment remarks
   */
  public String getPaymentRemarks() {
    return paymentRemarks;
  }

  /**
   * Sets the payment remarks.
   *
   * @param v the new payment remarks
   */
  public void setPaymentRemarks(String v) {
    this.paymentRemarks = v;
  }

  /**
   * Gets the receipt type.
   *
   * @return the receipt type
   */
  public String getReceiptType() {
    return receiptType;
  }

  /**
   * Sets the receipt type.
   *
   * @param v the new receipt type
   */
  public void setReceiptType(String v) {
    this.receiptType = v;
  }

  /**
   * Gets the collect date.
   *
   * @return the collect date
   */
  public String getCollectDate() {
    return collectDate;
  }

  /**
   * Sets the collect date.
   *
   * @param v the new collect date
   */
  public void setCollectDate(String v) {
    this.collectDate = v;
  }

  /**
   * Gets the collect time.
   *
   * @return the collect time
   */
  public String getCollectTime() {
    return collectTime;
  }

  /**
   * Sets the collect time.
   *
   * @param v the new collect time
   */
  public void setCollectTime(String v) {
    collectTime = v;
  }

  /**
   * Gets the payment type.
   *
   * @return the payment type
   */
  public String getPaymentType() {
    return paymentType;
  }

  /**
   * Sets the payment type.
   *
   * @param v the new payment type
   */
  public void setPaymentType(String v) {
    this.paymentType = v;
  }

  /**
   * Checks if is close.
   *
   * @return true, if is close
   */
  public boolean isClose() {
    return close;
  }

  /**
   * Sets the close.
   *
   * @param v the new close
   */
  public void setClose(boolean v) {
    this.close = v;
  }

  /**
   * Gets the lab id.
   *
   * @return the lab id
   */
  public String getLabId() {
    return labId;
  }

  /**
   * Sets the lab id.
   *
   * @param labId the new lab id
   */
  public void setLabId(String labId) {
    this.labId = labId;
  }

  /**
   * Gets the lab name.
   *
   * @return the lab name
   */
  public String getLabName() {
    return labName;
  }

  /**
   * Sets the lab name.
   *
   * @param labName the new lab name
   */
  public void setLabName(String labName) {
    this.labName = labName;
  }

  /**
   * Gets the patient id.
   *
   * @return the patient id
   */
  public String getPatientId() {
    return patientId;
  }

  /**
   * Sets the patient id.
   *
   * @param patientId the new patient id
   */
  public void setPatientId(String patientId) {
    this.patientId = patientId;
  }

  /**
   * Gets the pat name.
   *
   * @return the pat name
   */
  public String getPatName() {
    return patName;
  }

  /**
   * Sets the pat name.
   *
   * @param patName the new pat name
   */
  public void setPatName(String patName) {
    this.patName = patName;
  }

  /**
   * Checks if is status all.
   *
   * @return true, if is status all
   */
  public boolean isStatusAll() {
    return statusAll;
  }

  /**
   * Sets the status all.
   *
   * @param statusAll the new status all
   */
  public void setStatusAll(boolean statusAll) {
    this.statusAll = statusAll;
  }

  /**
   * Checks if is status closed.
   *
   * @return true, if is status closed
   */
  public boolean isStatusClosed() {
    return statusClosed;
  }

  /**
   * Sets the status closed.
   *
   * @param statusClosed the new status closed
   */
  public void setStatusClosed(boolean statusClosed) {
    this.statusClosed = statusClosed;
  }

  /**
   * Checks if is status open.
   *
   * @return true, if is status open
   */
  public boolean isStatusOpen() {
    return statusOpen;
  }

  /**
   * Sets the status open.
   *
   * @param statusOpen the new status open
   */
  public void setStatusOpen(boolean statusOpen) {
    this.statusOpen = statusOpen;
  }

  /**
   * Checks if is type all.
   *
   * @return true, if is type all
   */
  public boolean isTypeAll() {
    return typeAll;
  }

  /**
   * Sets the type all.
   *
   * @param typeAll the new type all
   */
  public void setTypeAll(boolean typeAll) {
    this.typeAll = typeAll;
  }

  /**
   * Checks if is type bill later.
   *
   * @return true, if is type bill later
   */
  public boolean isTypeBillLater() {
    return typeBillLater;
  }

  /**
   * Sets the type bill later.
   *
   * @param typeBillLater the new type bill later
   */
  public void setTypeBillLater(boolean typeBillLater) {
    this.typeBillLater = typeBillLater;
  }

  /**
   * Checks if is type bill now.
   *
   * @return true, if is type bill now
   */
  public boolean isTypeBillNow() {
    return typeBillNow;
  }

  /**
   * Sets the type bill now.
   *
   * @param typeBillNow the new type bill now
   */
  public void setTypeBillNow(boolean typeBillNow) {
    this.typeBillNow = typeBillNow;
  }

  /**
   * Gets the action.
   *
   * @return the action
   */
  public String getAction() {
    return action;
  }

  /**
   * Sets the action.
   *
   * @param action the new action
   */
  public void setAction(String action) {
    this.action = action;
  }

  /**
   * Gets the bill remarks.
   *
   * @return the bill remarks
   */
  public String getBillRemarks() {
    return billRemarks;
  }

  /**
   * Sets the bill remarks.
   *
   * @param billRemarks the new bill remarks
   */
  public void setBillRemarks(String billRemarks) {
    this.billRemarks = billRemarks;
  }

  /**
   * Gets the pat other info.
   *
   * @return the pat other info
   */
  public String getPatOtherInfo() {
    return patOtherInfo;
  }

  /**
   * Sets the pat other info.
   *
   * @param patOtherInfo the new pat other info
   */
  public void setPatOtherInfo(String patOtherInfo) {
    this.patOtherInfo = patOtherInfo;
  }

}
