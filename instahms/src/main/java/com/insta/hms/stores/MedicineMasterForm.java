package com.insta.hms.stores;

import org.apache.struts.action.ActionForm;

import java.math.BigDecimal;

public class MedicineMasterForm extends ActionForm {

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
  private BigDecimal exciseDuty;
  private BigDecimal cess;
  private BigDecimal vat;
  private String generic;
  private String therapatic;
  private String pharmaItem;
  private int issuePerBaseUnit;
  private String medicineId;
  private String search_medName;
  private String search_manfName;
  private String search_genName;
  private boolean drug;
  private String status;
  private String statusAll;
  private String statusActive;
  private String statusInActive;
  private String packType;
  private String categoryName;

  public String getCategoryName() {
    return categoryName;
  }
  public void setCategoryName(String categoryName) {
    this.categoryName = categoryName;
  }
  public String getSearch_medName() {
    return search_medName;
  }
  public void setSearch_medName(String search_medName) {
    this.search_medName = search_medName;
  }

  public BigDecimal getCess() {
    return cess;
  }
  public void setCess(BigDecimal cess) {
    this.cess = cess;
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

  public BigDecimal getExciseDuty() {
    return exciseDuty;
  }
  public void setExciseDuty(BigDecimal exciseDuty) {
    this.exciseDuty = exciseDuty;
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

  public int getIssuePerBaseUnit() {
    return issuePerBaseUnit;
  }
  public void setIssuePerBaseUnit(int issuePerBaseUnit) {
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

  public BigDecimal getVat() {
    return vat;
  }
  public void setVat(BigDecimal vat) {
    this.vat = vat;
  }

  public String getMedicineId() {
    return medicineId;
  }
  public void setMedicineId(String medicineId) {
    this.medicineId = medicineId;
  }
  public boolean isDrug() {
    return drug;
  }
  public void setDrug(boolean drug) {
    this.drug = drug;
  }
  public String getSearch_genName() {
    return search_genName;
  }
  public void setSearch_genName(String search_genName) {
    this.search_genName = search_genName;
  }
  public String getSearch_manfName() {
    return search_manfName;
  }
  public void setSearch_manfName(String search_manfName) {
    this.search_manfName = search_manfName;
  }
  public String getStatusActive() {
    return statusActive;
  }
  public void setStatusActive(String statusActive) {
    this.statusActive = statusActive;
  }
  public String getStatusAll() {
    return statusAll;
  }
  public void setStatusAll(String statusAll) {
    this.statusAll = statusAll;
  }
  public String getStatusInActive() {
    return statusInActive;
  }
  public void setStatusInActive(String statusInActive) {
    this.statusInActive = statusInActive;
  }
  public String getStatus() {
    return status;
  }
  public void setStatus(String status) {
    this.status = status;
  }
  public String[] getDeptoldrnew() {
    return deptoldrnew;
  }
  public void setDeptoldrnew(String[] deptoldrnew) {
    this.deptoldrnew = deptoldrnew;
  }
  public boolean[] getHdeleted() {
    return hdeleted;
  }
  public void setHdeleted(boolean[] hdeleted) {
    this.hdeleted = hdeleted;
  }
  public String getPackType() {
    return packType;
  }
  public void setPackType(String packType) {
    this.packType = packType;
  }
  public String getMedShortName() {
    return medShortName;
  }
  public void setMedShortName(String medShortName) {
    this.medShortName = medShortName;
  }

}
