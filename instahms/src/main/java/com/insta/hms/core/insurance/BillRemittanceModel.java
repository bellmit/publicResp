package com.insta.hms.core.insurance;

import com.insta.hms.core.billing.BillModel;
import com.insta.hms.integration.insurance.remittance.InsuranceRemittanceModel;

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
@Table(name = "bill_remittance")
public class BillRemittanceModel implements Serializable {
  
  private BillRemittanceIdSequence id;
  private BillModel bill;
  private InsuranceRemittanceModel remittance;
  private String remarks;
  private boolean writeOff;
  private String status;
  
  public BillRemittanceModel() {}
  
  public BillRemittanceModel(BillRemittanceIdSequence id) {
    this.id = id;
  }
  
  public BillRemittanceModel(String billNo, int remittanceId) {
    this.id = new BillRemittanceIdSequence(billNo, remittanceId);
  }
  
  @EmbeddedId
  @AttributeOverrides({
      @AttributeOverride(name = "billNo", column = @Column(name = "bill_no", nullable = false, length=15)),
      @AttributeOverride(name = "remittanceId", column = @Column(name = "remittance_id", nullable = false, length = 15)) })
  public BillRemittanceIdSequence getId() {
    return id;
  }

  public void setId(BillRemittanceIdSequence id) {
    this.id = id;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "bill_no", nullable = false, insertable = false, updatable = false)
  public BillModel getBill() {
    return bill;
  }

  public void setBill(BillModel bill) {
    this.bill = bill;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "remittance_id", nullable = false, insertable = false, updatable = false)
  public InsuranceRemittanceModel getRemittance() {
    return remittance;
  }

  public void setRemittance(InsuranceRemittanceModel remittance) {
    this.remittance = remittance;
  }

  @Column(name = "remarks", length = 100)
  public String getRemarks() {
    return remarks;
  }

  public void setRemarks(String remarks) {
    this.remarks = remarks;
  }

  @Column(name = "writeOff")
  public boolean getWriteOff() {
    return writeOff;
  }

  public void setWriteOff(boolean writeOff) {
    this.writeOff = writeOff;
  }

  @Column(name = "status", length = 1)
  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

}
