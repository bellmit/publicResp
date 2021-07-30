package com.insta.hms.stores;

import java.math.BigDecimal;
import java.sql.Date;

public class PO {

  private String supplier;
  private String quotationNo;
  private Date quotationDate;
  private String reference;
  private Date poDate;
  private String vatType;
  private String mrptype;
  private String supplierName;
  private BigDecimal vatRate;
  private String store;
  private String storeName;

  // po item screen variabels
  private String hdeleted;
  private String hmedicineName;
  private String hmedicineId;
  private String hgenName;
  private BigDecimal htaxrate;
  private String hpackType;
  private boolean hgdrug;
  private int hpackUnit;
  private int hcatId;
  private String hmanufacturer;
  private String hmanufMnemonic;
  private BigDecimal hmrp;
  private BigDecimal hamrp;
  private BigDecimal hrate;
  private float hqty;
  private BigDecimal hdisc;
  private BigDecimal hvat;
  private BigDecimal hamt;

  private BigDecimal totAmt;

  private String hgenCode;
  private String hmanufCode;
  private String suppterms;
  private String hospterms;
  private String userId;
  private String poNo;
  public String getMrptype() {
    return mrptype;
  }
  public void setMrptype(String mrptype) {
    this.mrptype = mrptype;
  }
  public Date getPoDate() {
    return poDate;
  }
  public void setPoDate(Date poDate) {
    this.poDate = poDate;
  }
  public Date getQuotationDate() {
    return quotationDate;
  }
  public void setQuotationDate(Date quotationDate) {
    this.quotationDate = quotationDate;
  }
  public String getQuotationNo() {
    return quotationNo;
  }
  public void setQuotationNo(String quotationNo) {
    this.quotationNo = quotationNo;
  }
  public String getReference() {
    return reference;
  }
  public void setReference(String reference) {
    this.reference = reference;
  }

  public String getSupplierName() {
    return supplierName;
  }
  public void setSupplierName(String supplierName) {
    this.supplierName = supplierName;
  }
  public String getVatType() {
    return vatType;
  }
  public void setVatType(String vatType) {
    this.vatType = vatType;
  }
  public BigDecimal getVatRate() {
    return vatRate;
  }
  public void setVatRate(BigDecimal vatRate) {
    this.vatRate = vatRate;
  }
  public String getSupplier() {
    return supplier;
  }
  public void setSupplier(String supplier) {
    this.supplier = supplier;
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
  public String getHdeleted() {
    return hdeleted;
  }
  public void setHdeleted(String hdeleted) {
    this.hdeleted = hdeleted;
  }
  public BigDecimal getHdisc() {
    return hdisc;
  }
  public void setHdisc(BigDecimal hdisc) {
    this.hdisc = hdisc;
  }
  public boolean isHgdrug() {
    return hgdrug;
  }
  public void setHgdrug(boolean hgdrug) {
    this.hgdrug = hgdrug;
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
  public String getHmanufMnemonic() {
    return hmanufMnemonic;
  }
  public void setHmanufMnemonic(String hmanufMnemonic) {
    this.hmanufMnemonic = hmanufMnemonic;
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
  public String getHpackType() {
    return hpackType;
  }
  public void setHpackType(String hpackType) {
    this.hpackType = hpackType;
  }
  public int getHpackUnit() {
    return hpackUnit;
  }
  public void setHpackUnit(int hpackUnit) {
    this.hpackUnit = hpackUnit;
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
  public BigDecimal getHtaxrate() {
    return htaxrate;
  }
  public void setHtaxrate(BigDecimal htaxrate) {
    this.htaxrate = htaxrate;
  }
  public BigDecimal getHvat() {
    return hvat;
  }
  public void setHvat(BigDecimal hvat) {
    this.hvat = hvat;
  }
  public BigDecimal getTotAmt() {
    return totAmt;
  }
  public void setTotAmt(BigDecimal totAmt) {
    this.totAmt = totAmt;
  }
  public String getHgenCode() {
    return hgenCode;
  }
  public void setHgenCode(String hgenCode) {
    this.hgenCode = hgenCode;
  }
  public String getHmanufCode() {
    return hmanufCode;
  }
  public void setHmanufCode(String hmanufCode) {
    this.hmanufCode = hmanufCode;
  }
  public String getHospterms() {
    return hospterms;
  }
  public void setHospterms(String hospterms) {
    this.hospterms = hospterms;
  }
  public String getSuppterms() {
    return suppterms;
  }
  public void setSuppterms(String suppterms) {
    this.suppterms = suppterms;
  }
  public String getUserId() {
    return userId;
  }
  public void setUserId(String userId) {
    this.userId = userId;
  }
  public String getPoNo() {
    return poNo;
  }
  public void setPoNo(String poNo) {
    this.poNo = poNo;
  }
  public int getHcatId() {
    return hcatId;
  }
  public void setHcatId(int hcatId) {
    this.hcatId = hcatId;
  }
  public String getStore() {
    return store;
  }
  public void setStore(String store) {
    this.store = store;
  }
  public String getStoreName() {
    return storeName;
  }
  public void setStoreName(String storeName) {
    this.storeName = storeName;
  }

}
