package com.insta.hms.insurance;

import java.math.BigDecimal;
import java.util.Date;

/**
 * The Class EstimationDTO.
 *
 * @author lakshmi.p
 */
public class EstimationDTO {

  /** The estimate ID. */
  private String estimateID;

  /** The charge ID. */
  private String chargeID;

  /** The charge group. */
  private String chargeGroup;

  /** The charge head. */
  private String chargeHead;

  /** The act description. */
  private String actDescription;

  /** The act remarks. */
  private String actRemarks;

  /** The act rate. */
  private BigDecimal actRate = new BigDecimal(0);

  /** The act quantity. */
  private BigDecimal actQuantity = new BigDecimal(0);

  /** The amount. */
  private BigDecimal amount = new BigDecimal(0);

  /** The discount. */
  private BigDecimal discount = new BigDecimal(0);

  /** The approved amt. */
  private BigDecimal approvedAmt = new BigDecimal(0);

  /** The charge group name. */
  private String chargeGroupName;

  /** The charge head name. */
  private String chargeHeadName;

  /** The updated date. */
  private Date updatedDate;

  /** The charge ref. */
  private String chargeRef;

  /**
   * Gets the act description.
   *
   * @return the actDescription
   */
  public String getActDescription() {
    return actDescription;
  }

  /**
   * Sets the act description.
   *
   * @param actDescription the actDescription to set
   */
  public void setActDescription(String actDescription) {
    this.actDescription = actDescription;
  }

  /**
   * Gets the act quantity.
   *
   * @return the actQuantity
   */
  public BigDecimal getActQuantity() {
    return actQuantity;
  }

  /**
   * Sets the act quantity.
   *
   * @param actQuantity the actQuantity to set
   */
  public void setActQuantity(BigDecimal actQuantity) {
    this.actQuantity = actQuantity;
  }

  /**
   * Sets the act quantity.
   *
   * @param actQuantity the new act quantity
   */
  public void setActQuantity(String actQuantity) {
    setActQuantity(new BigDecimal(actQuantity));
  }

  /**
   * Gets the act rate.
   *
   * @return the actRate
   */
  public BigDecimal getActRate() {
    return actRate;
  }

  /**
   * Sets the act rate.
   *
   * @param actRate the actRate to set
   */
  public void setActRate(BigDecimal actRate) {
    this.actRate = actRate;
  }

  /**
   * Sets the act rate.
   *
   * @param actRate the new act rate
   */
  public void setActRate(String actRate) {
    setActRate(new BigDecimal(actRate));
  }

  /**
   * Gets the act remarks.
   *
   * @return the actRemarks
   */
  public String getActRemarks() {
    return actRemarks;
  }

  /**
   * Sets the act remarks.
   *
   * @param actRemarks the actRemarks to set
   */
  public void setActRemarks(String actRemarks) {
    this.actRemarks = actRemarks;
  }

  /**
   * Gets the amount.
   *
   * @return the amount
   */
  public BigDecimal getAmount() {
    return amount;
  }

  /**
   * Sets the amount.
   *
   * @param amount the amount to set
   */
  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  /**
   * Sets the amount.
   *
   * @param amount the new amount
   */
  public void setAmount(String amount) {
    setAmount(new BigDecimal(amount));
  }

  /**
   * Gets the approved amt.
   *
   * @return the approvedAmt
   */
  public BigDecimal getApprovedAmt() {
    return approvedAmt;
  }

  /**
   * Sets the approved amt.
   *
   * @param approvedAmt the approvedAmt to set
   */
  public void setApprovedAmt(BigDecimal approvedAmt) {
    this.approvedAmt = approvedAmt;
  }

  /**
   * Sets the approved amt.
   *
   * @param approvedAmt the new approved amt
   */
  public void setApprovedAmt(String approvedAmt) {
    setApprovedAmt(new BigDecimal(approvedAmt));
  }

  /**
   * Gets the charge group.
   *
   * @return the chargeGroup
   */
  public String getChargeGroup() {
    return chargeGroup;
  }

  /**
   * Sets the charge group.
   *
   * @param chargeGroup the chargeGroup to set
   */
  public void setChargeGroup(String chargeGroup) {
    this.chargeGroup = chargeGroup;
  }

  /**
   * Gets the charge group name.
   *
   * @return the chargeGroupName
   */
  public String getChargeGroupName() {
    return chargeGroupName;
  }

  /**
   * Sets the charge group name.
   *
   * @param chargeGroupName the chargeGroupName to set
   */
  public void setChargeGroupName(String chargeGroupName) {
    this.chargeGroupName = chargeGroupName;
  }

  /**
   * Gets the charge head.
   *
   * @return the chargeHead
   */
  public String getChargeHead() {
    return chargeHead;
  }

  /**
   * Sets the charge head.
   *
   * @param chargeHead the chargeHead to set
   */
  public void setChargeHead(String chargeHead) {
    this.chargeHead = chargeHead;
  }

  /**
   * Gets the charge head name.
   *
   * @return the chargeHeadName
   */
  public String getChargeHeadName() {
    return chargeHeadName;
  }

  /**
   * Sets the charge head name.
   *
   * @param chargeHeadName the chargeHeadName to set
   */
  public void setChargeHeadName(String chargeHeadName) {
    this.chargeHeadName = chargeHeadName;
  }

  /**
   * Gets the charge ID.
   *
   * @return the chargeID
   */
  public String getChargeID() {
    return chargeID;
  }

  /**
   * Sets the charge ID.
   *
   * @param chargeID the chargeID to set
   */
  public void setChargeID(String chargeID) {
    this.chargeID = chargeID;
  }

  /**
   * Gets the discount.
   *
   * @return the discount
   */
  public BigDecimal getDiscount() {
    return discount;
  }

  /**
   * Sets the discount.
   *
   * @param discount the discount to set
   */
  public void setDiscount(BigDecimal discount) {
    this.discount = discount;
  }

  /**
   * Sets the discount.
   *
   * @param discount the new discount
   */
  public void setDiscount(String discount) {
    setDiscount(new BigDecimal(discount));
  }

  /**
   * Gets the estimate ID.
   *
   * @return the estimateID
   */
  public String getEstimateID() {
    return estimateID;
  }

  /**
   * Sets the estimate ID.
   *
   * @param estimateID the estimateID to set
   */
  public void setEstimateID(String estimateID) {
    this.estimateID = estimateID;
  }

  /**
   * Gets the updated date.
   *
   * @return the updatedDate
   */
  public Date getUpdatedDate() {
    return updatedDate;
  }

  /**
   * Sets the updated date.
   *
   * @param updatedDate the updatedDate to set
   */
  public void setUpdatedDate(Date updatedDate) {
    this.updatedDate = updatedDate;
  }

  /**
   * Gets the charge ref.
   *
   * @return the chargeRef
   */
  public String getChargeRef() {
    return chargeRef;
  }

  /**
   * Sets the charge ref.
   *
   * @param chargeRef the chargeRef to set
   */
  public void setChargeRef(String chargeRef) {
    this.chargeRef = chargeRef;
  }

}
