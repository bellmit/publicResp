package com.insta.hms.stores;

import org.apache.struts.action.ActionForm;

public class PharmacyMasterForm extends ActionForm {

  // category master variables

  private String category;

  // category details screen

  private String categoryId;
  private String operation;
  private String status;
  private String categoryName;

  // medicine master variabels

  private String searchMedName;
  private String searchManfName;
  private String searchGenName;
  private String searchMedCatName;
  private String medicineId;

  // medicine details screen

  private String medName;
  private String medShortName;
  private String manfName;
  private String genericName;
  private String composition;
  private String hdepartment[];
  private float hiddenMinLevel[];
  private float hiddenMaxLevel[];
  private float hiddenDangerLevel[];
  private float hiddenReorderLevel[];
  private boolean[] hdeleted;
  private String[] deptoldrnew;
  private String rflag[];
  private String delDepartment[];
  private String generic;
  private String therapatic;
  private String pharmaItem;
  private float issuePerBaseUnit;
  private boolean drug;
  private String packType;
  private String issueUnits;
  private String consumption_uom;
  private float consumption_capacity;
  private String updateMrpCp;
  private String originalPkgSize;

  // general variables

  private String pageNum;
  private String sortOrder;
  private boolean sortReverse;
  private boolean statusAll;
  private boolean statusActive;
  private boolean statusInActive;

  private boolean claimable;

  public boolean getClaimable() {
    return claimable;
  }
  public void setClaimable(boolean claimable) {
    this.claimable = claimable;
  }
  public String getCategory() {
    return category;
  }
  public void setCategory(String category) {
    this.category = category;
  }
  public boolean isStatusActive() {
    return statusActive;
  }
  public void setStatusActive(boolean statusActive) {
    this.statusActive = statusActive;
  }
  public boolean isStatusAll() {
    return statusAll;
  }
  public void setStatusAll(boolean statusAll) {
    this.statusAll = statusAll;
  }
  public boolean isStatusInActive() {
    return statusInActive;
  }
  public void setStatusInActive(boolean statusInActive) {
    this.statusInActive = statusInActive;
  }
  public String getPageNum() {
    return pageNum;
  }
  public void setPageNum(String pageNum) {
    this.pageNum = pageNum;
  }
  public String getSortOrder() {
    return sortOrder;
  }
  public void setSortOrder(String sortOrder) {
    this.sortOrder = sortOrder;
  }
  public boolean isSortReverse() {
    return sortReverse;
  }
  public void setSortReverse(boolean sortReverse) {
    this.sortReverse = sortReverse;
  }
  public String getCategoryId() {
    return categoryId;
  }
  public void setCategoryId(String categoryId) {
    this.categoryId = categoryId;
  }
  public String getOperation() {
    return operation;
  }
  public void setOperation(String operation) {
    this.operation = operation;
  }
  public String getStatus() {
    return status;
  }
  public void setStatus(String status) {
    this.status = status;
  }
  public String getCategoryName() {
    return categoryName;
  }
  public void setCategoryName(String categoryName) {
    this.categoryName = categoryName;
  }
  public String getSearchGenName() {
    return searchGenName;
  }
  public void setSearchGenName(String searchGenName) {
    this.searchGenName = searchGenName;
  }
  public String getSearchManfName() {
    return searchManfName;
  }
  public void setSearchManfName(String searchManfName) {
    this.searchManfName = searchManfName;
  }
  public String getSearchMedCatName() {
    return searchMedCatName;
  }
  public void setSearchMedCatName(String searchMedCatName) {
    this.searchMedCatName = searchMedCatName;
  }
  public String getSearchMedName() {
    return searchMedName;
  }
  public void setSearchMedName(String searchMedName) {
    this.searchMedName = searchMedName;
  }
  public String getMedicineId() {
    return medicineId;
  }
  public void setMedicineId(String medicineId) {
    this.medicineId = medicineId;
  }
  public String getComposition() {
    return composition;
  }
  public void setComposition(String composition) {
    this.composition = composition;
  }
  public String[] getDelDepartment() {
    return delDepartment;
  }
  public void setDelDepartment(String[] delDepartment) {
    this.delDepartment = delDepartment;
  }
  public String[] getDeptoldrnew() {
    return deptoldrnew;
  }
  public void setDeptoldrnew(String[] deptoldrnew) {
    this.deptoldrnew = deptoldrnew;
  }
  public boolean isDrug() {
    return drug;
  }
  public void setDrug(boolean drug) {
    this.drug = drug;
  }
  public String getGeneric() {
    return generic;
  }
  public void setGeneric(String generic) {
    this.generic = generic;
  }
  public String getGenericName() {
    return genericName;
  }
  public void setGenericName(String genericName) {
    this.genericName = genericName;
  }
  public boolean[] getHdeleted() {
    return hdeleted;
  }
  public void setHdeleted(boolean[] hdeleted) {
    this.hdeleted = hdeleted;
  }
  public String[] getHdepartment() {
    return hdepartment;
  }
  public void setHdepartment(String[] hdepartment) {
    this.hdepartment = hdepartment;
  }
  public float[] getHiddenDangerLevel() {
    return hiddenDangerLevel;
  }
  public void setHiddenDangerLevel(float[] hiddenDangerLevel) {
    this.hiddenDangerLevel = hiddenDangerLevel;
  }
  public float[] getHiddenMaxLevel() {
    return hiddenMaxLevel;
  }
  public void setHiddenMaxLevel(float[] hiddenMaxLevel) {
    this.hiddenMaxLevel = hiddenMaxLevel;
  }
  public float[] getHiddenMinLevel() {
    return hiddenMinLevel;
  }
  public void setHiddenMinLevel(float[] hiddenMinLevel) {
    this.hiddenMinLevel = hiddenMinLevel;
  }
  public float[] getHiddenReorderLevel() {
    return hiddenReorderLevel;
  }
  public void setHiddenReorderLevel(float[] hiddenReorderLevel) {
    this.hiddenReorderLevel = hiddenReorderLevel;
  }
  public float getIssuePerBaseUnit() {
    return issuePerBaseUnit;
  }
  public void setIssuePerBaseUnit(float issuePerBaseUnit) {
    this.issuePerBaseUnit = issuePerBaseUnit;
  }
  public String getManfName() {
    return manfName;
  }
  public void setManfName(String manfName) {
    this.manfName = manfName;
  }
  public String getMedName() {
    return medName;
  }
  public void setMedName(String medName) {
    this.medName = medName;
  }
  public String getMedShortName() {
    return medShortName;
  }
  public void setMedShortName(String medShortName) {
    this.medShortName = medShortName;
  }
  public String getPackType() {
    return packType;
  }
  public void setPackType(String packType) {
    this.packType = packType;
  }
  public String getPharmaItem() {
    return pharmaItem;
  }
  public void setPharmaItem(String pharmaItem) {
    this.pharmaItem = pharmaItem;
  }
  public String[] getRflag() {
    return rflag;
  }
  public void setRflag(String[] rflag) {
    this.rflag = rflag;
  }
  public String getTherapatic() {
    return therapatic;
  }
  public void setTherapatic(String therapatic) {
    this.therapatic = therapatic;
  }
  public String getUpdateMrpCp() {
    return updateMrpCp;
  }
  public void setUpdateMrpCp(String updateMrpCp) {
    this.updateMrpCp = updateMrpCp;
  }
  public String getOriginalPkgSize() {
    return originalPkgSize;
  }
  public void setOriginalPkgSize(String originalPkgSize) {
    this.originalPkgSize = originalPkgSize;
  }
  public String getIssueUnits() {
    return issueUnits;
  }
  public void setIssueUnits(String issueUnits) {
    this.issueUnits = issueUnits;
  }
  public float getConsumption_capacity() {
    return consumption_capacity;
  }
  public void setConsumption_capacity(float consumption_capacity) {
    this.consumption_capacity = consumption_capacity;
  }
  public String getConsumption_uom() {
    return consumption_uom;
  }
  public void setConsumption_uom(String consumption_uom) {
    this.consumption_uom = consumption_uom;
  }

}
