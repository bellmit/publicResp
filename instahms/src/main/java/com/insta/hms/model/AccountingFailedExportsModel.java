package com.insta.hms.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "accounting_failed_exports")
public class AccountingFailedExportsModel implements Serializable {
  private Integer id;
  private String visitId;
  private String billNo;
  private String receiptId;
  private Date lastRun;
 
  public AccountingFailedExportsModel() {
	  
  }
  
  public AccountingFailedExportsModel(String billNo, String visitId) {
    this.billNo = billNo;
    this.visitId = visitId;
  }

  public AccountingFailedExportsModel(String receiptId) {
    this.receiptId = receiptId;
  }

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, 
  	generator = "accountingfailedexport_id_generator")
  @SequenceGenerator(name = "accountingfailedexport_id_generator", 
  	sequenceName = "accounting_failed_exports_id_seq", allocationSize = 1, initialValue = 1)
  @Column(name = "id", nullable = false)
  public Integer getId() {
    return this.id;
  }
  
  public void setId(Integer id) {
    this.id = id;
  }
  
  @Column(name = "visit_id", length = 15, nullable = true)
  public String getVisitId() {
    return this.visitId;
  }

  public void setVisitId(String visitId) {
    this.visitId = visitId;
  }

  @Column(name = "bill_no", length = 15, nullable = true)
  public String getBillNo() {
    return this.billNo;
  }

  public void setBillNo(String billNo) {
    this.billNo = billNo;
  }
  
  @Column(name = "receipt_id", length = 15, nullable = true)
  public String getReceiptId() {
    return this.receiptId;
  }

  public void setReceiptId(String receiptId) {
    this.receiptId = receiptId;
  }
  
  @Column(name = "last_run", nullable = false)
  public Date getlastRun() {
    return lastRun;
  }

  public void setLastRun(Date lastRun) {
    this.lastRun = lastRun;
  }
}
