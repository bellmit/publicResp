package com.insta.hms.integration.insurance.remittance;

import com.insta.hms.core.insurance.InsuranceClaimModel;
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
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name="reconciliation_activity_details")
public class ReconciliationActivityDetailsModel implements java.io.Serializable { 
  
  private Long reconciliationActivityDetailsId;
  private ReconciliationModel reconciliationId;
  private String activityId;
  private InsuranceClaimModel claimId;
  private BigDecimal allocatedAmount;
  private String denialRemarks;
  private String status;
  private Date createdAt;
  private String createdBy;
  
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "reconciliation_activity_details_id_generator")
    @SequenceGenerator(name = "reconciliation_activity_details_id_generator", sequenceName = "reconciliation_activity_details_id_sequence", allocationSize = 1, initialValue = 1)
    @Column(name = "reconciliation_activity_details_id", unique = true, nullable = false)
    public Long getReconciliationActivityDetailsId() {
        return this.reconciliationActivityDetailsId;
    }

    public void setReconciliationActivityDetailsId(Long reconciliationActivityDetailsId) {
        this.reconciliationActivityDetailsId = reconciliationActivityDetailsId;
    }
    
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reconciliation_id", nullable = false)
  public ReconciliationModel getReconciliationId() {
    return reconciliationId;
  }

  public void setReconciliationId(
      ReconciliationModel reconciliationId) {
    this.reconciliationId = reconciliationId;
  }

  @Column(name = "activity_id")
  public String getActivityId() {
        return activityId; 
  }
    
  public void setActivityId(String activityId) { 
        this.activityId = activityId; 
  }


  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "claim_id", nullable = false)
  public InsuranceClaimModel getClaimId() {
    return claimId;
  }

  public void setClaimId(InsuranceClaimModel claimId) {
    this.claimId = claimId;
  }

  @Column(name = "allocated_amount", precision = 15)
  public BigDecimal getAllocatedAmount() {
    return allocatedAmount;
  }

  public void setAllocatedAmount(BigDecimal allocatedAmount) {
    this.allocatedAmount = allocatedAmount;
  }

  @Column(name =  "denial_remarks")
  public String getDenialRemarks() {
    return denialRemarks;
  }

  public void setDenialRemarks(String denialRemarks) {
    this.denialRemarks = denialRemarks;
  }

  @Column(name = "status")
  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  @Column(name = "created_at")
  public Date getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }

  @Column(name = "created_by")
  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }
}
