package com.insta.hms.core.billing;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "receipt_refund_reference")
public class ReceiptRefundReferenceModel implements java.io.Serializable {
  private long id;
  private ReceiptModel refundReceipt;
  private ReceiptModel receipt;
  private BigDecimal amount;
  private BigDecimal taxAmount;
  private BigDecimal taxRate;
  
  public ReceiptRefundReferenceModel() {
  }
  
  public ReceiptRefundReferenceModel(ReceiptModel refundReceipt, ReceiptModel receipt, BigDecimal amount) {
    this.refundReceipt = refundReceipt;
    this.receipt = receipt;
    this.amount = amount;
  }
  
  public ReceiptRefundReferenceModel(long id, ReceiptModel refundReceipt, ReceiptModel receipt, BigDecimal amount) {
    this.id = id;
    this.refundReceipt = refundReceipt;
    this.receipt = receipt;
    this.amount = amount;
  }
  
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "receipt_refund_reference_generator")
  @SequenceGenerator(name = "receipt_refund_reference_generator", sequenceName = "receipt_refund_reference_sequence", allocationSize = 1, initialValue = 1)
  @Column(name = "id", unique = true, nullable = false)
  public long getId() {
    return this.id;
  }
  
  public void setId(long id) {
    this.id = id;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "refund_receipt_id", referencedColumnName = "receipt_id", nullable = false)
  public ReceiptModel getRefundReceipt() {
    return refundReceipt;
  }

  public void setRefundReceipt(ReceiptModel refundReceipt) {
    this.refundReceipt = refundReceipt;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "receipt_id", nullable = false)
  public ReceiptModel getReceipt() {
    return receipt;
  }

  public void setReceipt(ReceiptModel receipt) {
    this.receipt = receipt;
  }

  @Column(name = "amount", nullable = false, precision = 16, scale = 3)
  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  @Column(name = "tax_amount", nullable = true, precision = 16, scale = 3)
  public BigDecimal getTaxAmount() {
    return taxAmount;
  }

  public void setTaxAmount(BigDecimal taxAmount) {
    this.taxAmount = taxAmount;
  }

  @Column(name = "tax_rate", nullable = true, precision = 16, scale = 3)
  public BigDecimal getTaxRate() {
    return taxRate;
  }

  public void setTaxRate(BigDecimal taxRate) {
    this.taxRate = taxRate;
  }
}
