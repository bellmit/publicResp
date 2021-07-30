package com.insta.hms.medicalrecorddepartment;

import org.apache.struts.action.ActionForm;

/**
 * @author lakshmi.p
 *
 */
public class ICDPatientSearchForm extends ActionForm {

  private String mrno;

  private String complaint;
  private String diagnosis;
  private String treatment;
  private String oldReg;

  private String regFromDate;
  private String regToDate;

  private String disFromDate;
  private String disToDate;

  private String sortOrder;
  private boolean sortReverse;

  public String getRegFromDate() {
    return regFromDate;
  }

  public void setRegFromDate(String regFromDate) {
    this.regFromDate = regFromDate;
  }

  public String getRegToDate() {
    return regToDate;
  }

  public void setRegToDate(String regToDate) {
    this.regToDate = regToDate;
  }

  public String getDisFromDate() {
    return disFromDate;
  }

  public void setDisFromDate(String disFromDate) {
    this.disFromDate = disFromDate;
  }

  public String getDisToDate() {
    return disToDate;
  }

  public void setDisToDate(String disToDate) {
    this.disToDate = disToDate;
  }

  public String getMrno() {
    return mrno;
  }

  public void setMrno(String mrno) {
    this.mrno = mrno;
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

  public String getComplaint() {
    return complaint;
  }

  public void setComplaint(String complaint) {
    this.complaint = complaint;
  }

  public String getDiagnosis() {
    return diagnosis;
  }

  public void setDiagnosis(String diagnosis) {
    this.diagnosis = diagnosis;
  }

  public String getTreatment() {
    return treatment;
  }

  public void setTreatment(String treatment) {
    this.treatment = treatment;
  }

  public String getOldReg() {
    return oldReg;
  }

  public void setOldReg(String oldReg) {
    this.oldReg = oldReg;
  }

}
