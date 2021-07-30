package com.insta.hms.stores;

/*
 * DTO class to hold the variables of store_stock_details
 */
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

public class MedicineStock {

  private int deptId; // the store for which this stock exists
  private int medicineId;
  private String batchNo;
  private String bin;
  private Date expDt;
  private BigDecimal qty; // quantity avlbl in stock, in issue units.
  private BigDecimal bonusQty; // bonus quantity avlbl in stock, in issue units.
  private BigDecimal mrp; // the MRP printed on the strip/package/bottle/item
  private BigDecimal tax; // Amount of tax for the package (in Rs.)
  private BigDecimal taxPercent; // amount of tax in percentage. Use only when rate is changed.
  private BigDecimal packageSp; // rate at which the package (excluding tax) is to be sold
  private BigDecimal packageUnit; // number of items in a package
  private Date receivedDate; // when the stock was received last
  private Timestamp stockTime; // when the stock was updated due to a sale/return
  private BigDecimal package_cp;
  private String username;
  private String change_source;
  public BigDecimal getGrn_qty() {
    return grn_qty;
  }
  public void setGrn_qty(BigDecimal grn_qty) {
    this.grn_qty = grn_qty;
  }
  private String taxType;
  private String medCategory;
  private String controlType;
  private BigDecimal cedTaxAmt; // Central Excise Tax
  private BigDecimal patAmt; // Patient Amt -- used in insurance
  private BigDecimal patPer; // Patient Percentage for copay -- used in insurance
  private BigDecimal patCap; // Patient Amount Cap -- used in insurance
  private BigDecimal patCatAmt;// Patient Amt per category -- used in insurance
  private int insuranceCategoryId;
  private BigDecimal grn_qty;
  private BigDecimal grn_cp;// to hold cost price with out any discount,tax

  public BigDecimal getGrn_cp() {
    return grn_cp;
  }
  public void setGrn_cp(BigDecimal grn_cp) {
    this.grn_cp = grn_cp;
  }
  /*
   * Extended attributes available via a join, not used in insert/updates
   */
  private String medicineName;
  private String package_type;
  private String manfCode;
  private String manfName;
  private String manfMnemonic;
  private BigDecimal medDisc;
  private String issueUnits;
  private String genericName;
  private String identification;
  private String retailable;
  private String billable;
  private String claimable;
  private String itemBarcode;
  private String preAuthRequired;
  private String packageUOM;
  private String itemcode;
  private List<Map> stockTaxDetails;

  public String getItemcode() {
    return itemcode;
  }
  public void setItemcode(String itemcode) {
    this.itemcode = itemcode;
  }
  private int itemBatchId;

  public int getItemBatchId() {
    return itemBatchId;
  }
  public void setItemBatchId(int itemBatchId) {
    this.itemBatchId = itemBatchId;
  }
  public String getPackageUOM() {
    return packageUOM;
  }
  public void setPackageUOM(String packageUOM) {
    this.packageUOM = packageUOM;
  }
  public String getPreAuthRequired() {
    return preAuthRequired;
  }
  public void setPreAuthRequired(String preAuthRequired) {
    this.preAuthRequired = preAuthRequired;
  }
  public String getItemBarcode() {
    return itemBarcode;
  }
  public void setItemBarcode(String itemBarcode) {
    this.itemBarcode = itemBarcode;
  }
  public String getIssueUnits() {
    return issueUnits;
  }
  public void setIssueUnits(String issueUnits) {
    this.issueUnits = issueUnits;
  }
  public String getChange_source() {
    return change_source;
  }
  public void setChange_source(String change_source) {
    this.change_source = change_source;
  }
  public String getUsername() {
    return username;
  }
  public void setUsername(String username) {
    this.username = username;
  }
  /*
   * Accessors
   */
  public int getMedicineId() {
    return medicineId;
  }
  public void setMedicineId(int v) {
    medicineId = v;
  }

  public String getBatchNo() {
    return batchNo;
  }
  public void setBatchNo(String v) {
    batchNo = v;
  }

  public BigDecimal getQty() {
    return qty;
  }
  public void setQty(BigDecimal v) {
    qty = v;
  }

  public Date getExpDt() {
    return expDt;
  }
  public void setExpDt(Date v) {
    expDt = v;
  }

  public BigDecimal getMrp() {
    return mrp;
  }
  public void setMrp(BigDecimal v) {
    mrp = v;
  }

  public BigDecimal getTax() {
    return tax;
  }
  public void setTax(BigDecimal v) {
    tax = v;
  }

  public BigDecimal getTaxPercent() {
    return taxPercent;
  }
  public void setTaxPercent(BigDecimal v) {
    taxPercent = v;
  }

  public Date getReceivedDate() {
    return receivedDate;
  }
  public void setReceivedDate(Date v) {
    receivedDate = v;
  }

  public int getDeptId() {
    return deptId;
  }
  public void setDeptId(int v) {
    deptId = v;
  }

  public Timestamp getStockTime() {
    return stockTime;
  }
  public void setStockTime(Timestamp v) {
    stockTime = v;
  }

  public String getMedicineName() {
    return medicineName;
  }
  public void setMedicineName(String v) {
    medicineName = v;
  }

  public String getManfCode() {
    return manfCode;
  }
  public void setManfCode(String v) {
    manfCode = v;
  }

  public String getManfName() {
    return manfName;
  }
  public void setManfName(String v) {
    manfName = v;
  }

  public String getManfMnemonic() {
    return manfMnemonic;
  }
  public void setManfMnemonic(String v) {
    manfMnemonic = v;
  }

  public BigDecimal getPackageUnit() {
    return packageUnit;
  }
  public void setPackageUnit(BigDecimal v) {
    packageUnit = v;
  }

  public BigDecimal getPackageSp() {
    return packageSp;
  }
  public void setPackageSp(BigDecimal v) {
    packageSp = v;
  }

  public String getBin() {
    return bin;
  }
  public void setBin(String v) {
    this.bin = v;
  }

  public BigDecimal getPackage_cp() {
    return package_cp;
  }
  public void setPackage_cp(BigDecimal v) {
    this.package_cp = v;
  }

  public String getPackage_type() {
    return package_type;
  }
  public void setPackage_type(String v) {
    this.package_type = v;
  }
  public BigDecimal getMedDisc() {
    return medDisc;
  }
  public void setMedDisc(BigDecimal medDisc) {
    this.medDisc = medDisc;
  }
  public String getTaxType() {
    return taxType;
  }
  public void setTaxType(String v) {
    taxType = v;
  }
  public String getMedCategory() {
    return medCategory;
  }
  public void setMedCategory(String medCategory) {
    this.medCategory = medCategory;
  }
  public String getGenericName() {
    return genericName;
  }
  public void setGenericName(String genericName) {
    this.genericName = genericName;
  }
  public String getIdentification() {
    return identification;
  }
  public void setIdentification(String identification) {
    this.identification = identification;
  }
  public String getBillable() {
    return billable;
  }
  public void setBillable(String billable) {
    this.billable = billable;
  }
  public String getRetailable() {
    return retailable;
  }
  public void setRetailable(String retailable) {
    this.retailable = retailable;
  }
  public BigDecimal getCedTaxAmt() {
    return cedTaxAmt;
  }
  public void setCedTaxAmt(BigDecimal cedTaxAmt) {
    this.cedTaxAmt = cedTaxAmt;
  }
  public String getClaimable() {
    return claimable;
  }
  public void setClaimable(String v) {
    claimable = v;
  }
  public BigDecimal getPatAmt() {
    return patAmt;
  }
  public void setPatAmt(BigDecimal patAmt) {
    this.patAmt = patAmt;
  }
  public BigDecimal getPatCap() {
    return patCap;
  }
  public void setPatCap(BigDecimal patCap) {
    this.patCap = patCap;
  }
  public BigDecimal getPatPer() {
    return patPer;
  }
  public void setPatPer(BigDecimal patPer) {
    this.patPer = patPer;
  }
  public BigDecimal getPatCatAmt() {
    return patCatAmt;
  }
  public void setPatCatAmt(BigDecimal patCatAmt) {
    this.patCatAmt = patCatAmt;
  }
  public int getInsuranceCategoryId() {
    return insuranceCategoryId;
  }
  public void setInsuranceCategoryId(int v) {
    insuranceCategoryId = v;
  }

  public String getControlType() {
    return controlType;
  }
  public void setControlType(String controlType) {
    this.controlType = controlType;
  }
  public BigDecimal getBonusQty() {
    return bonusQty;
  }
  public void setBonusQty(BigDecimal bonusQty) {
    this.bonusQty = bonusQty;
  }

}
