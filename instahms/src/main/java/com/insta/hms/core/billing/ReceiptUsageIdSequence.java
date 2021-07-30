package com.insta.hms.core.billing;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class ReceiptUsageIdSequence implements java.io.Serializable {

  private String receiptId;
  private String entityType;
  private String entityId;

  public ReceiptUsageIdSequence() {
  }

  public ReceiptUsageIdSequence(String receiptId, String entityType, String entityId) {
    this.receiptId = receiptId;
    this.entityType = entityType;
    this.entityId = entityId;
  }

  @Column(name = "receipt_id", nullable = false, length = 15)
  public String getReceiptId() {
    return receiptId;
  }

  public void setReceiptId(String receiptId) {
    this.receiptId = receiptId;
  }

  @Column(name = "entity_type", nullable = false, length = 15)
  public String getEntityType() {
    return entityType;
  }

  public void setEntityType(String entityType) {
    this.entityType = entityType;
  }

  @Column(name = "entity_id", nullable = false, length = 15)
  public String getEntityId() {
    return entityId;
  }

  public void setEntityId(String entityId) {
    this.entityId = entityId;
  }

  public boolean equals(Object other) {
    if ((this == other))
      return true;
    if ((other == null))
      return false;
    if (!(other instanceof ReceiptUsageIdSequence))
      return false;
    ReceiptUsageIdSequence castOther = (ReceiptUsageIdSequence) other;

    return ((this.getReceiptId() == castOther.getReceiptId()) || this.getReceiptId() != null
        && castOther.getReceiptId() != null && this.getReceiptId().equals(castOther.getReceiptId()))
        && ((this.getEntityType() == castOther.getEntityType())
            || this.getEntityType() != null && castOther.getEntityType() != null
                && this.getEntityType().equals(castOther.getEntityType()))
        && ((this.getEntityId() == castOther.getEntityId())
            || this.getEntityId() != null && castOther.getEntityId() != null
                && this.getEntityId().equals(castOther.getEntityId()));
  }

  public int hashCode() {
    int result = 17;
    result = 37 * result + (getReceiptId() == null ? 0 : this.getReceiptId().hashCode());
    result = 37 * result + (getEntityType() == null ? 0 : this.getEntityType().hashCode());
    result = 37 * result + (getEntityId() == null ? 0 : this.getEntityId().hashCode());
    return result;
  }

}
