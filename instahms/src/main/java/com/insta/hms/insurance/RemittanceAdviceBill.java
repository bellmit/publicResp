package com.insta.hms.insurance;

import java.math.BigDecimal;

/**
 * The Class RemittanceAdviceBill.
 *
 * @author lakshmi.p
 */
public class RemittanceAdviceBill {

  /** The bill no. */
  private String billNo;

  /** The payment reference. */
  private String paymentReference;

  /** The payment amount. */
  private BigDecimal paymentAmount;

  /** The denial remarks. */
  private String denialRemarks;

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
   * @param billNo the new bill no
   */
  public void setBillNo(String billNo) {
    this.billNo = billNo;
  }

  /**
   * Gets the denial remarks.
   *
   * @return the denial remarks
   */
  public String getDenialRemarks() {
    return denialRemarks;
  }

  /**
   * Sets the denial remarks.
   *
   * @param denialRemarks the new denial remarks
   */
  public void setDenialRemarks(String denialRemarks) {
    this.denialRemarks = denialRemarks;
  }

  /**
   * Gets the payment amount.
   *
   * @return the payment amount
   */
  public BigDecimal getPaymentAmount() {
    return paymentAmount;
  }

  /**
   * Sets the payment amount.
   *
   * @param paymentAmount the new payment amount
   */
  public void setPaymentAmount(BigDecimal paymentAmount) {
    this.paymentAmount = paymentAmount;
  }

  /**
   * Gets the payment reference.
   *
   * @return the payment reference
   */
  public String getPaymentReference() {
    return paymentReference;
  }

  /**
   * Sets the payment reference.
   *
   * @param paymentReference the new payment reference
   */
  public void setPaymentReference(String paymentReference) {
    this.paymentReference = paymentReference;
  }
}
