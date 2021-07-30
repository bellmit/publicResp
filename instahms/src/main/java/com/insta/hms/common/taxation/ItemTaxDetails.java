package com.insta.hms.common.taxation;

import java.math.BigDecimal;

// TODO: Auto-generated Javadoc
/**
 * The Class ItemTaxDetails.
 *
 * @author irshadmohammed
 */
public class ItemTaxDetails {

  /** The amount. */
  private BigDecimal costPrice;
  private BigDecimal mrp;
  private BigDecimal qty;
  private BigDecimal bonusQty;
  private BigDecimal pkgSize;
  private BigDecimal discount;
  private BigDecimal totalTaxAmount;
  private BigDecimal cedAmount;
  private BigDecimal adjMrp;
  private BigDecimal amount;

  /** The sugbroup id. */
  private int sugbroupId;

  /** The item id. */
  private Object itemId;

  /** The discount percent. */
  private BigDecimal discountPercent;

  /** The discount type. */
  private String discountType;

  /** The adj price. */
  private BigDecimal adjPrice;

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
   * @param amount
   *          the new amount
   */
  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  /** The tax basis. */
  private String taxBasis;

  /** The net amount. */
  private BigDecimal netAmount;

  /** The qty uom. */
  private String qtyUom;

  /** The original tax. */
  private BigDecimal originalTax;

  /** The agg tax rate. */
  private BigDecimal aggTaxRate;

  /**
   * Gets the cost price.
   *
   * @return the cost price
   */
  public BigDecimal getCostPrice() {
    return costPrice;
  }

  /**
   * Sets the cost price.
   *
   * @param costPrice
   *          the new cost price
   */
  public void setCostPrice(BigDecimal costPrice) {
    this.costPrice = costPrice;
  }

  /**
   * Gets the mrp.
   *
   * @return the mrp
   */
  public BigDecimal getMrp() {
    return mrp;
  }

  /**
   * Sets the mrp.
   *
   * @param mrp
   *          the new mrp
   */
  public void setMrp(BigDecimal mrp) {
    this.mrp = mrp;
  }

  /**
   * Gets the qty.
   *
   * @return the qty
   */
  public BigDecimal getQty() {
    return qty;
  }

  /**
   * Sets the qty.
   *
   * @param qty
   *          the new qty
   */
  public void setQty(BigDecimal qty) {
    this.qty = qty;
  }

  /**
   * Gets the bonus qty.
   *
   * @return the bonus qty
   */
  public BigDecimal getBonusQty() {
    return bonusQty;
  }

  /**
   * Sets the bonus qty.
   *
   * @param bonusQty
   *          the new bonus qty
   */
  public void setBonusQty(BigDecimal bonusQty) {
    this.bonusQty = bonusQty;
  }

  /**
   * Gets the pkg size.
   *
   * @return the pkg size
   */
  public BigDecimal getPkgSize() {
    return pkgSize;
  }

  /**
   * Sets the pkg size.
   *
   * @param pkgSize
   *          the new pkg size
   */
  public void setPkgSize(BigDecimal pkgSize) {
    this.pkgSize = pkgSize;
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
   * @param discount
   *          the new discount
   */
  public void setDiscount(BigDecimal discount) {
    this.discount = discount;
  }

  /**
   * Gets the total tax amount.
   *
   * @return the total tax amount
   */
  public BigDecimal getTotalTaxAmount() {
    return totalTaxAmount;
  }

  /**
   * Sets the total tax amount.
   *
   * @param totalTaxAmount
   *          the new total tax amount
   */
  public void setTotalTaxAmount(BigDecimal totalTaxAmount) {
    this.totalTaxAmount = totalTaxAmount;
  }

  /**
   * Gets the ced amount.
   *
   * @return the ced amount
   */
  public BigDecimal getCedAmount() {
    return cedAmount;
  }

  /**
   * Sets the ced amount.
   *
   * @param cedAmount
   *          the new ced amount
   */
  public void setCedAmount(BigDecimal cedAmount) {
    this.cedAmount = cedAmount;
  }

  /**
   * Gets the tax basis.
   *
   * @return the tax basis
   */
  public String getTaxBasis() {
    return taxBasis;
  }

  /**
   * Sets the tax basis.
   *
   * @param taxBasis
   *          the new tax basis
   */
  public void setTaxBasis(String taxBasis) {
    this.taxBasis = taxBasis;
  }

  /**
   * Gets the adj mrp.
   *
   * @return the adj mrp
   */
  public BigDecimal getAdjMrp() {
    return adjMrp;
  }

  /**
   * Sets the adj mrp.
   *
   * @param adjMrp
   *          the new adj mrp
   */
  public void setAdjMrp(BigDecimal adjMrp) {
    this.adjMrp = adjMrp;
  }

  /**
   * Gets the sugbroup id.
   *
   * @return the sugbroup id
   */
  public int getSugbroupId() {
    return sugbroupId;
  }

  /**
   * Sets the sugbroup id.
   *
   * @param sugbroupId
   *          the new sugbroup id
   */
  public void setSugbroupId(int sugbroupId) {
    this.sugbroupId = sugbroupId;
  }

  /**
   * Gets the item id.
   *
   * @return the item id
   */
  public Object getItemId() {
    return itemId;
  }

  /**
   * Sets the item id.
   *
   * @param itemId
   *          the new item id
   */
  public void setItemId(Object itemId) {
    this.itemId = itemId;
  }

  /**
   * Gets the net amount.
   *
   * @return the net amount
   */
  public BigDecimal getNetAmount() {
    return netAmount;
  }

  /**
   * Sets the net amount.
   *
   * @param netAmount
   *          the new net amount
   */
  public void setNetAmount(BigDecimal netAmount) {
    this.netAmount = netAmount;
  }

  /**
   * Gets the discount percent.
   *
   * @return the discount percent
   */
  public BigDecimal getDiscountPercent() {
    return discountPercent;
  }

  /**
   * Sets the discount percent.
   *
   * @param discountPercent
   *          the new discount percent
   */
  public void setDiscountPercent(BigDecimal discountPercent) {
    this.discountPercent = discountPercent;
  }

  /**
   * Gets the discount type.
   *
   * @return the discount type
   */
  public String getDiscountType() {
    return discountType;
  }

  /**
   * Sets the discount type.
   *
   * @param discountType
   *          the new discount type
   */
  public void setDiscountType(String discountType) {
    this.discountType = discountType;
  }

  /**
   * Sets the adj price.
   *
   * @param adjPrice
   *          the new adj price
   */
  public void setAdjPrice(BigDecimal adjPrice) {
    this.adjPrice = adjPrice;
  }

  /**
   * Gets the adj price.
   *
   * @return the adj price
   */
  public BigDecimal getAdjPrice() {
    return adjPrice;
  }

  /**
   * Gets the qty uom.
   *
   * @return the qty uom
   */
  public String getQtyUom() {
    return qtyUom;
  }

  /**
   * Sets the qty uom.
   *
   * @param qtyUom
   *          the new qty uom
   */
  public void setQtyUom(String qtyUom) {
    this.qtyUom = qtyUom;
  }

  /**
   * Gets the original tax.
   *
   * @return the original tax
   */
  public BigDecimal getOriginalTax() {
    return originalTax;
  }

  /**
   * Sets the original tax.
   *
   * @param originalTax
   *          the new original tax
   */
  public void setOriginalTax(BigDecimal originalTax) {
    this.originalTax = originalTax;
  }

  /**
   * Gets the agg tax rate.
   *
   * @return the agg tax rate
   */
  public BigDecimal getAggTaxRate() {
    return aggTaxRate;
  }

  /**
   * Sets the aggregate rate.
   *
   * @param aggTaxRate
   *          the new aggregate rate
   */
  public void setAggregateRate(BigDecimal aggTaxRate) {
    this.aggTaxRate = aggTaxRate; // This is already divided by 100 (%)
  }

}
