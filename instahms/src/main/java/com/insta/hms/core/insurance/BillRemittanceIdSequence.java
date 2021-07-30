package com.insta.hms.core.insurance;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class BillRemittanceIdSequence implements Serializable {

  private String billNo;
  private Integer remittanceId;

  public BillRemittanceIdSequence() {
  }

  public BillRemittanceIdSequence(String billNo, int remittanceId) {
    this.billNo = billNo;
    this.remittanceId = remittanceId;
  }

  @Column(name = "bill_no", length = 15, nullable = false)
  public String getBillNo() {
    return billNo;
  }

  public void setBillNo(String billNo) {
    this.billNo = billNo;
  }

  @Column(name = "remittance_id", nullable = false)
  public Integer getRemittanceId() {
    return remittanceId;
  }

  public void setRemittanceId(int remittanceId) {
    this.remittanceId = remittanceId;
  }

  public boolean equals(Object other) {
    if ((this == other)) {
      return true;
    }
    if ((other == null)) {
      return false;
    }
    if (!(other instanceof BillRemittanceIdSequence)) {
      return false;
    }
    BillRemittanceIdSequence castOther = (BillRemittanceIdSequence) other;

    return (((this.getBillNo() == castOther.getBillNo()) || (this.getBillNo() != null
        && castOther.getBillNo() != null && this.getBillNo().equals(castOther.getBillNo())))
        && (this.getRemittanceId() == castOther.getRemittanceId()));
  }

  public int hashCode() {
    int result = 17;

    result = 37 * result + (getBillNo() == null ? 0 : this.getBillNo().hashCode());
    result = 37 * result + (getRemittanceId() == null ? 0 : this.getRemittanceId().hashCode());
    return result;
  }

}
