package com.insta.hms.core.billing;

import com.insta.hms.mdm.taxsubgroups.ItemSubGroupsModel;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "receipt_tax")
public class ReceiptTaxModel implements Serializable {
  private Integer receiptTaxId;
  private ReceiptModel receiptId;
  private ItemSubGroupsModel taxSubGroupId;
  private BigDecimal taxRate;
  private BigDecimal taxAmount;

  public ReceiptTaxModel() {

  }

  @Id
  @Column(name = "receipt_tax_id", unique = true, nullable = false)
  public Integer getReceiptTaxId() {
    return receiptTaxId;
  }

  public void setReceiptTaxId(Integer receiptTaxId) {
    this.receiptTaxId = receiptTaxId;
  }

  @NotFound(action = NotFoundAction.IGNORE)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "receipt_id", nullable = false, insertable = false, updatable = false)
  public ReceiptModel getReceiptId() {
    return receiptId;
  }

  public void setReceiptId(ReceiptModel receiptId) {
    this.receiptId = receiptId;
  }

  @NotFound(action = NotFoundAction.IGNORE)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "tax_sub_group_id", nullable = false, insertable = false, updatable = false)
  public ItemSubGroupsModel getTaxSubGroupId() {
    return taxSubGroupId;
  }

  public void setTaxSubGroupId(ItemSubGroupsModel taxSubGroupId) {
    this.taxSubGroupId = taxSubGroupId;
  }

  @Column(name = "tax_rate", nullable = false, precision = 15, scale = 2)
  public BigDecimal getTaxRate() {
    return taxRate;
  }

  public void setTaxRate(BigDecimal taxRate) {
    this.taxRate = taxRate;
  }

  @Column(name = "tax_amount", nullable = false, precision = 15, scale = 2)
  public BigDecimal getTaxAmount() {
    return taxAmount;
  }

  public void setTaxAmount(BigDecimal taxAmount) {
    this.taxAmount = taxAmount;
  }
}
