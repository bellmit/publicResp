package com.insta.hms.core.billing;

import java.io.Serializable;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "receipt_usage")
public class ReceiptUsageModel implements Serializable{
  
  ReceiptUsageIdSequence id;
  ReceiptModel receipt;
  
  public ReceiptUsageModel() {}
  
  public ReceiptUsageModel(String receiptId, String entityType, String entityId) {
    this.id = new ReceiptUsageIdSequence(receiptId, entityType, entityId);
  }
  
  @EmbeddedId
  @AttributeOverrides({
      @AttributeOverride(name = "receiptId", column = @Column(name = "receipt_id", nullable = false, length = 15)),
      @AttributeOverride(name = "entityType", column = @Column(name = "entity_type", nullable = false, length = 15)),
      @AttributeOverride(name = "entityId", column = @Column(name = "entity_id", nullable = false, length = 15)) })
  public ReceiptUsageIdSequence getId() {
    return id;
  }
  public void setId(ReceiptUsageIdSequence id) {
    this.id = id;
  }
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "receipt_id", nullable = false, insertable = false, updatable = false)
  public ReceiptModel getReceipt() {
    return receipt;
  }
  public void setReceipt(ReceiptModel receipt) {
    this.receipt = receipt;
  }
  

}
