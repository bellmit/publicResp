package com.insta.hms.core.billing;

import org.hibernate.annotations.Immutable;

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
@Immutable
@Table(name = "refund_reference_allocation_view")
public class RefundReferenceAllocationViewModel implements java.io.Serializable {
  private long id;
  private ReceiptModel refundReceipt;
  private ReceiptModel receipt;
  private BigDecimal amount;
  private BigDecimal allocatedAmount;
  private BillReceiptsModel billReceipt;
  private BillModel billNo;
  
  public RefundReferenceAllocationViewModel() {
    
  }
  
  @Id
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

  @Column(name = "allocated_amount", nullable = false, precision = 16, scale = 3)
  public BigDecimal getAllocatedAmount() {
    return allocatedAmount;
  }

  public void setAllocatedAmount(BigDecimal allocatedAmount) {
    this.allocatedAmount = allocatedAmount;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "bill_receipt_id")
  public BillReceiptsModel getBillReceipt() {
    return this.billReceipt;
  }

  public void setBillReceipt(BillReceiptsModel billReceipt) {
    this.billReceipt = billReceipt;
  }
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "bill_no", nullable = false)
  public BillModel getBillNo() {
    return this.billNo;
  }

  public void setBillNo(BillModel billNo) {
    this.billNo = billNo;
  }
}
