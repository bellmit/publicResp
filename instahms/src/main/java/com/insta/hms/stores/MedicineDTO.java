package com.insta.hms.stores;

import java.math.BigDecimal;

public class MedicineDTO {

  private String medicineName;
  private String medShortName;
  private String manufacturerName;
  private String genericName;
  private String composition;
  private float exciseDuty;
  private float cess;
  private BigDecimal vat;
  private String therapaticUse;
  private String pharmaItem;
  private float issuPerBaseUnit;
  private String medicineId;
  private String packageType;
  private String hDrugStatus;
  private String status;
  private String drugst;
  private int categoryId;
  private String categoryName;
  private String updatemrp;
  private float originalPkgSize;
  private String username;
  private String change_source;
  private String issueUnits;
  private boolean claimable;

  public boolean getClaimable() {
    return claimable;
  }
  public void setClaimable(boolean claimable) {
    this.claimable = claimable;
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
  public float getOriginalPkgSize() {
    return originalPkgSize;
  }
  public void setOriginalPkgSize(float originalPkgSize) {
    this.originalPkgSize = originalPkgSize;
  }
  public String getUpdatemrp() {
    return updatemrp;
  }
  public void setUpdatemrp(String updatemrp) {
    this.updatemrp = updatemrp;
  }
  public String getCategoryName() {
    return categoryName;
  }
  public void setCategoryName(String categoryName) {
    this.categoryName = categoryName;
  }
  public String getMedicineId() {
    return medicineId;
  }
  public void setMedicineId(String medicineId) {
    this.medicineId = medicineId;
  }

  public float getCess() {
    return cess;
  }
  public void setCess(float cess) {
    this.cess = cess;
  }

  public String getComposition() {
    return composition;
  }
  public void setComposition(String composition) {
    this.composition = composition;
  }

  public float getExciseDuty() {
    return exciseDuty;
  }
  public void setExciseDuty(float exciseDuty) {
    this.exciseDuty = exciseDuty;
  }

  public String getGenericName() {
    if (genericName.trim().equals("")) {
      genericName = null;
    }
    return genericName;
  }
  public void setGenericName(String genericName) {
    this.genericName = genericName;
  }

  public float getIssuPerBaseUnit() {
    return issuPerBaseUnit;
  }
  public void setIssuPerBaseUnit(float issuPerBaseUnit) {
    this.issuPerBaseUnit = issuPerBaseUnit;
  }

  public String getManufacturerName() {
    return manufacturerName;
  }
  public void setManufacturerName(String manufacturerName) {
    this.manufacturerName = manufacturerName;
  }

  public String getMedicineName() {
    return medicineName;
  }
  public void setMedicineName(String medicineName) {
    this.medicineName = medicineName;
  }

  public String getPharmaItem() {
    return pharmaItem;
  }
  public void setPharmaItem(String pharmaItem) {
    this.pharmaItem = pharmaItem;
  }

  public String getTherapaticUse() {
    return therapaticUse;
  }
  public void setTherapaticUse(String therapaticUse) {
    this.therapaticUse = therapaticUse;
  }

  public BigDecimal getVat() {
    return vat;
  }
  public void setVat(BigDecimal vat) {
    this.vat = vat;
  }

  public String getPackageType() {
    return packageType;
  }
  public void setPackageType(String packageType) {
    this.packageType = packageType;
  }

  public String getStatus() {
    return status;
  }
  public void setStatus(String status) {
    this.status = status;
  }
  public String getHDrugStatus() {
    return hDrugStatus;
  }
  public void setHDrugStatus(String drugStatus) {
    hDrugStatus = drugStatus;
  }
  public String getDrugst() {
    return drugst;
  }
  public void setDrugst(String drugst) {
    this.drugst = drugst;
  }
  public String getMedShortName() {
    return medShortName;
  }
  public void setMedShortName(String medShortName) {
    this.medShortName = medShortName;
  }
  public int getCategoryId() {
    return categoryId;
  }
  public void setCategoryId(int categoryId) {
    this.categoryId = categoryId;
  }

}
