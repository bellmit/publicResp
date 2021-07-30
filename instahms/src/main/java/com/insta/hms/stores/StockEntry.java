package com.insta.hms.stores;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

public class StockEntry {

  private List mediList;

  private String username;
  private String change_source;

  private String supplier;
  private String suppCode;
  private String ponum;
  private String reference;
  private String invno;
  private Date invoiceDate;
  private Date grnDate;
  private BigDecimal invamt;
  private String vatType;
  private BigDecimal vatrate;
  private String mrptype;
  private String grnNo;
  private String ninv;
  private String einv;
  private Date dueDate;
  private BigDecimal billedamt;
  private BigDecimal disc;
  private BigDecimal tax;
  private BigDecimal roff;
  private String paymentId;
  private String status;
  private String meddisc;
  private String medDiscValue;
  private String invDisc;
  private BigDecimal discPer;

  private String mon;
  private String year;
  private String bonus;
  private String batchNo;

  private String invDate;
  private String invdueDate;

  private String hmedicineName;
  private String hmedicineId;
  private String hgenName;
  private String hgenCode;
  private String hpharma;
  private String controlTypeName;
  private float hpackUnit;
  private String hpackType;
  private String hmanufacturer;
  private String hmanufCode;
  private String hbatchNo;
  private int hcatId;
  private String hmedcatname;
  private boolean claimable;
  private Date hexpiry;
  private float hqty;
  private BigDecimal hrate;
  private BigDecimal htaxrate;
  private BigDecimal htax;
  private BigDecimal hmrp;
  private String delItem;
  private String deptId;
  private float bqty;
  private String hbin;
  private BigDecimal hamrp;
  private BigDecimal hdisc;
  private BigDecimal hamt;
  private BigDecimal totDisc;
  private BigDecimal totvat;
  private BigDecimal totAmt;

  private BigDecimal invdiscounts;
  private BigDecimal otherCharges;
  private String otherDescription;
  private String remarks;
  private String paymentRemarks;
  private BigDecimal roundAmt;
  private Date paidDate;
  private String strPaidDate;

  private String oldSupplier;
  private String oldInvoiceNo;

  private BigDecimal cessTaxRate;
  private BigDecimal cessAmt;

  private String taxName;
  private BigDecimal cstRate;

  private String invoiceType;

  private String consignmentStatus;
  private int issueId;

  public String getInvoiceType() {
    return invoiceType;
  }

  public void setInvoiceType(String invoiceType) {
    this.invoiceType = invoiceType;
  }

  public BigDecimal getCessAmt() {
    return cessAmt;
  }

  public void setCessAmt(BigDecimal cessAmt) {
    this.cessAmt = cessAmt;
  }

  public BigDecimal getCessTaxRate() {
    return cessTaxRate;
  }

  public void setCessTaxRate(BigDecimal cessTaxRate) {
    this.cessTaxRate = cessTaxRate;
  }

  public String getOldInvoiceNo() {
    return oldInvoiceNo;
  }

  public void setOldInvoiceNo(String oldInvoiceNo) {
    this.oldInvoiceNo = oldInvoiceNo;
  }

  public String getOldSupplier() {
    return oldSupplier;
  }

  public void setOldSupplier(String oldSupplier) {
    this.oldSupplier = oldSupplier;
  }

  public BigDecimal getInvamt() {
    return invamt;
  }

  public void setInvamt(BigDecimal invamt) {
    this.invamt = invamt;
  }

  public String getInvno() {
    return invno;
  }

  public void setInvno(String invno) {
    this.invno = invno;
  }

  public Date getInvoiceDate() {
    return invoiceDate;
  }

  public void setInvoiceDate(Date invoiceDate) {
    this.invoiceDate = invoiceDate;
  }

  public String getMrptype() {
    return mrptype;
  }

  public void setMrptype(String mrptype) {
    this.mrptype = mrptype;
  }

  public String getPonum() {
    return ponum;
  }

  public void setPonum(String ponum) {
    this.ponum = ponum;
  }

  public String getReference() {
    return reference;
  }

  public void setReference(String reference) {
    this.reference = reference;
  }

  public String getSupplier() {
    return supplier;
  }

  public void setSupplier(String supplier) {
    this.supplier = supplier;
  }

  public BigDecimal getVatrate() {
    return vatrate;
  }

  public void setVatrate(BigDecimal vatrate) {
    this.vatrate = vatrate;
  }

  public String getVatType() {
    return vatType;
  }

  public void setVatType(String vatType) {
    this.vatType = vatType;
  }

  public float getBqty() {
    return bqty;
  }

  public void setBqty(float bqty) {
    this.bqty = bqty;
  }

  public String getDelItem() {
    return delItem;
  }

  public void setDelItem(String delItem) {
    this.delItem = delItem;
  }

  public String getDeptId() {
    return deptId;
  }

  public void setDeptId(String deptId) {
    this.deptId = deptId;
  }

  public BigDecimal getHamrp() {
    return hamrp;
  }

  public void setHamrp(BigDecimal hamrp) {
    this.hamrp = hamrp;
  }

  public BigDecimal getHamt() {
    return hamt;
  }

  public void setHamt(BigDecimal hamt) {
    this.hamt = hamt;
  }

  public String getHbatchNo() {
    return hbatchNo;
  }

  public void setHbatchNo(String hbatchNo) {
    this.hbatchNo = hbatchNo;
  }

  public String getHbin() {
    return hbin;
  }

  public void setHbin(String hbin) {
    this.hbin = hbin;
  }

  public BigDecimal getHdisc() {
    return hdisc;
  }

  public void setHdisc(BigDecimal hdisc) {
    this.hdisc = hdisc;
  }

  public Date getHexpiry() {
    return hexpiry;
  }

  public void setHexpiry(Date hexpiry) {
    this.hexpiry = hexpiry;
  }

  public String getHgenCode() {
    return hgenCode;
  }

  public void setHgenCode(String hgenCode) {
    this.hgenCode = hgenCode;
  }

  public String getHgenName() {
    return hgenName;
  }

  public void setHgenName(String hgenName) {
    this.hgenName = hgenName;
  }

  public String getHmanufacturer() {
    return hmanufacturer;
  }

  public void setHmanufacturer(String hmanufacturer) {
    this.hmanufacturer = hmanufacturer;
  }

  public String getHmanufCode() {
    return hmanufCode;
  }

  public void setHmanufCode(String hmanufCode) {
    this.hmanufCode = hmanufCode;
  }

  public String getHmedicineId() {
    return hmedicineId;
  }

  public void setHmedicineId(String hmedicineId) {
    this.hmedicineId = hmedicineId;
  }

  public String getHmedicineName() {
    return hmedicineName;
  }

  public void setHmedicineName(String hmedicineName) {
    this.hmedicineName = hmedicineName;
  }

  public BigDecimal getHmrp() {
    return hmrp;
  }

  public void setHmrp(BigDecimal hmrp) {
    this.hmrp = hmrp;
  }

  public float getHpackUnit() {
    return hpackUnit;
  }

  public void setHpackUnit(float hpackUnit) {
    this.hpackUnit = hpackUnit;
  }

  public String getHpharma() {
    return hpharma;
  }

  public void setHpharma(String hpharma) {
    this.hpharma = hpharma;
  }

  public float getHqty() {
    return hqty;
  }

  public void setHqty(float hqty) {
    this.hqty = hqty;
  }

  public BigDecimal getHrate() {
    return hrate;
  }

  public void setHrate(BigDecimal hrate) {
    this.hrate = hrate;
  }

  public BigDecimal getHtax() {
    return htax;
  }

  public void setHtax(BigDecimal htax) {
    this.htax = htax;
  }

  public BigDecimal getHtaxrate() {
    return htaxrate;
  }

  public void setHtaxrate(BigDecimal htaxrate) {
    this.htaxrate = htaxrate;
  }

  public BigDecimal getTotAmt() {
    return totAmt;
  }

  public void setTotAmt(BigDecimal totAmt) {
    this.totAmt = totAmt;
  }

  public BigDecimal getTotDisc() {
    return totDisc;
  }

  public void setTotDisc(BigDecimal totDisc) {
    this.totDisc = totDisc;
  }

  public BigDecimal getTotvat() {
    return totvat;
  }

  public void setTotvat(BigDecimal totvat) {
    this.totvat = totvat;
  }

  public String getGrnNo() {
    return grnNo;
  }

  public void setGrnNo(String grnNo) {
    this.grnNo = grnNo;
  }

  public String getSuppCode() {
    return suppCode;
  }

  public void setSuppCode(String suppCode) {
    this.suppCode = suppCode;
  }

  public List getMediList() {
    return mediList;
  }

  public void setMediList(List mediList) {
    this.mediList = mediList;
  }

  public Date getGrnDate() {
    return grnDate;
  }

  public void setGrnDate(Date grnDate) {
    this.grnDate = grnDate;
  }

  public String getPaymentId() {
    return paymentId;
  }

  public void setPaymentId(String v) {
    paymentId = v;
  }

  public String getHpackType() {
    return hpackType;
  }

  public void setHpackType(String hpackType) {
    this.hpackType = hpackType;
  }

  public BigDecimal getBilledamt() {
    return billedamt;
  }

  public void setBilledamt(BigDecimal billedamt) {
    this.billedamt = billedamt;
  }

  public BigDecimal getDisc() {
    return disc;
  }

  public void setDisc(BigDecimal disc) {
    this.disc = disc;
  }

  public Date getDueDate() {
    return dueDate;
  }

  public void setDueDate(Date dueDate) {
    this.dueDate = dueDate;
  }

  public String getEinv() {
    return einv;
  }

  public void setEinv(String einv) {
    this.einv = einv;
  }

  public String getNinv() {
    return ninv;
  }

  public void setNinv(String ninv) {
    this.ninv = ninv;
  }

  public BigDecimal getRoff() {
    return roff;
  }

  public void setRoff(BigDecimal roff) {
    this.roff = roff;
  }

  public BigDecimal getTax() {
    return tax;
  }

  public void setTax(BigDecimal tax) {
    this.tax = tax;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getInvDate() {
    return invDate;
  }

  public void setInvDate(String invDate) {
    this.invDate = invDate;
  }

  public String getInvdueDate() {
    return invdueDate;
  }

  public void setInvdueDate(String invdueDate) {
    this.invdueDate = invdueDate;
  }

  public String getMeddisc() {
    return meddisc;
  }

  public void setMeddisc(String meddisc) {
    this.meddisc = meddisc;
  }

  public String getMedDiscValue() {
    return medDiscValue;
  }

  public void setMedDiscValue(String v) {
    medDiscValue = v;
  }

  public String getMon() {
    return mon;
  }

  public void setMon(String mon) {
    this.mon = mon;
  }

  public String getYear() {
    return year;
  }

  public void setYear(String year) {
    this.year = year;
  }

  public String getBonus() {
    return bonus;
  }

  public void setBonus(String bonus) {
    this.bonus = bonus;
  }

  public String getBatchNo() {
    return batchNo;
  }

  public void setBatchNo(String batchNo) {
    this.batchNo = batchNo;
  }

  public int getHcatId() {
    return hcatId;
  }

  public void setHcatId(int hcatId) {
    this.hcatId = hcatId;
  }

  public String getHmedcatname() {
    return hmedcatname;
  }

  public void setHmedcatname(String hmedcatname) {
    this.hmedcatname = hmedcatname;
  }

  public BigDecimal getDiscPer() {
    return discPer;
  }

  public void setDiscPer(BigDecimal discPer) {
    this.discPer = discPer;
  }

  public String getInvDisc() {
    return invDisc;
  }

  public void setInvDisc(String invDisc) {
    this.invDisc = invDisc;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getChange_source() {
    return change_source;
  }

  public void setChange_source(String change_source) {
    this.change_source = change_source;
  }

  public BigDecimal getInvdiscounts() {
    return invdiscounts;
  }

  public void setInvdiscounts(BigDecimal invdiscounts) {
    this.invdiscounts = invdiscounts;
  }

  public BigDecimal getOtherCharges() {
    return otherCharges;
  }

  public void setOtherCharges(BigDecimal otherCharges) {
    this.otherCharges = otherCharges;
  }

  public String getOtherDescription() {
    return otherDescription;
  }

  public void setOtherDescription(String otherDescription) {
    this.otherDescription = otherDescription;
  }

  public String getRemarks() {
    return remarks;
  }

  public void setRemarks(String remarks) {
    this.remarks = remarks;
  }

  public String getPaymentRemarks() {
    return paymentRemarks;
  }

  public void setPaymentRemarks(String v) {
    paymentRemarks = v;
  }

  public BigDecimal getRoundAmt() {
    return roundAmt;
  }

  public void setRoundAmt(BigDecimal roundAmt) {
    this.roundAmt = roundAmt;
  }

  public Date getPaidDate() {
    return paidDate;
  }

  public void setPaidDate(Date paidDate) {
    this.paidDate = paidDate;
  }

  public String getStrPaidDate() {
    return strPaidDate;
  }

  public void setStrPaidDate(String strPaidDate) {
    this.strPaidDate = strPaidDate;
  }

  public BigDecimal getCstRate() {
    return cstRate;
  }

  public void setCstRate(BigDecimal cstRate) {
    this.cstRate = cstRate;
  }

  public String getTaxName() {
    return taxName;
  }

  public void setTaxName(String taxName) {
    this.taxName = taxName;
  }

  public boolean isClaimable() {
    return claimable;
  }

  public void setClaimable(boolean claimable) {
    this.claimable = claimable;
  }

  public String getConsignmentStatus() {
    return consignmentStatus;
  }

  public void setConsignmentStatus(String v) {
    consignmentStatus = v;
  }

  public int getIssueId() {
    return issueId;
  }

  public void setIssueId(int v) {
    issueId = v;
  }

  public String getControlTypeName() {
    return controlTypeName;
  }

  public void setControlTypeName(String controlTypeName) {
    this.controlTypeName = controlTypeName;
  }

}
