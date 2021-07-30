package com.insta.hms.integration.insurance.remittance;

import com.fasterxml.jackson.annotation.JsonView;
import com.insta.hms.billing.Receipt;
import com.insta.hms.core.billing.ReceiptModel;
import com.insta.hms.security.usermanager.UUserModel;
import com.insta.hms.util.ViewProfiles;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "reconciliation")
public class ReconciliationModel implements Serializable {
  @JsonView(ViewProfiles.Public.class)
  private int reconciliationId;
  @JsonView(ViewProfiles.Public.class)
  private BigDecimal allocatedAmount;
  @JsonView(ViewProfiles.Public.class)
  private ReceiptModel receiptId;
  @JsonView(ViewProfiles.Public.class)
  private UUserModel createdBy;
  @JsonView(ViewProfiles.Public.class)
  private UUserModel modifiedBy;
  @JsonView(ViewProfiles.Public.class)
  private Date createdAt;
  @JsonView(ViewProfiles.Public.class)
  private Date modifiedAt;


  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "reconciliation_id_generator")
  @SequenceGenerator(name = "reconciliation_id_generator", sequenceName = "reconciliation_seq",
      allocationSize = 1, initialValue = 1)
  @Column(name = "reconciliation_id", unique = true, nullable = false)
  public int getReconciliationId() {
    return reconciliationId;
  }

  public void setReconciliationId(int reconciliationId) {
    this.reconciliationId = reconciliationId;
  }

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "receipt_id")
  public ReceiptModel getReceiptId() {
    return receiptId;
  }

  @Column(name = "allocated_amount", nullable = false)
  public BigDecimal getAllocatedAmount() {
    return allocatedAmount;
  }

  public void setAllocatedAmount(BigDecimal allocatedAmount) {
    this.allocatedAmount = allocatedAmount;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by", referencedColumnName = "emp_username", nullable = false)
  public UUserModel getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(UUserModel createdBy) {
    this.createdBy = createdBy;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "modified_by", referencedColumnName = "emp_username", nullable = false)
  public UUserModel getModifiedBy() {
    return modifiedBy;
  }

  public void setModifiedBy(UUserModel modifiedBy) {
    this.modifiedBy = modifiedBy;
  }

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "created_at", length = 29)
  public Date getCreatedAt() {
    return this.createdAt;
  }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "modified_at", length = 29)
  public Date getModifiedAt() {
    return this.modifiedAt;
  }

  public void setModifiedAt(Date modifiedAt) {
    this.modifiedAt = modifiedAt;
  }

  public void setReceiptId(ReceiptModel receiptId) {
    this.receiptId = receiptId;
  }
}
