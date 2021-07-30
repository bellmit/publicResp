package com.insta.hms.medicalrecorddepartment;

import org.apache.struts.action.ActionForm;

public class MRDUpdateScreenForm extends ActionForm {

  private String bloodgroup;

  private String[] ailment;
  private String[] ailmentId;

  private String[] diagnosis;
  private String[] diagnosisId;

  private String[] treatment;
  private String[] treatmentId;

  private String[] ailmentIcdinput;
  private String[] diagnosisIcdinput;
  private String[] treatmentIcdinput;

  private String[] ailmentDelete;
  private String[] diagnosisDelete;
  private String[] treatmentDelete;

  private String patId;
  private String mrNo;

  private String disDate;
  private String disType;
  private String referredTo;

  public String getBloodgroup() {
    return bloodgroup;
  }

  public void setBloodgroup(String bloodgroup) {
    this.bloodgroup = bloodgroup;
  }

  public String getPatId() {
    return patId;
  }

  public void setPatId(String patId) {
    this.patId = patId;
  }

  public String getMrNo() {
    return mrNo;
  }

  public void setMrNo(String mrNo) {
    this.mrNo = mrNo;
  }

  public String[] getAilment() {
    return ailment;
  }

  public void setAilment(String[] ailment) {
    this.ailment = ailment;
  }

  public String[] getAilmentId() {
    return ailmentId;
  }

  public void setAilmentId(String[] ailmentId) {
    this.ailmentId = ailmentId;
  }

  public String[] getDiagnosis() {
    return diagnosis;
  }

  public void setDiagnosis(String[] diagnosis) {
    this.diagnosis = diagnosis;
  }

  public String[] getDiagnosisId() {
    return diagnosisId;
  }

  public void setDiagnosisId(String[] diagnosisId) {
    this.diagnosisId = diagnosisId;
  }

  public String[] getTreatment() {
    return treatment;
  }

  public void setTreatment(String[] treatment) {
    this.treatment = treatment;
  }

  public String[] getTreatmentId() {
    return treatmentId;
  }

  public void setTreatmentId(String[] treatmentId) {
    this.treatmentId = treatmentId;
  }

  public String[] getAilmentIcdinput() {
    return ailmentIcdinput;
  }

  public void setAilmentIcdinput(String[] ailmentIcdinput) {
    this.ailmentIcdinput = ailmentIcdinput;
  }

  public String[] getDiagnosisIcdinput() {
    return diagnosisIcdinput;
  }

  public void setDiagnosisIcdinput(String[] diagnosisIcdinput) {
    this.diagnosisIcdinput = diagnosisIcdinput;
  }

  public String[] getTreatmentIcdinput() {
    return treatmentIcdinput;
  }

  public void setTreatmentIcdinput(String[] treatmentIcdinput) {
    this.treatmentIcdinput = treatmentIcdinput;
  }

  public String getDisType() {
    return disType;
  }

  public void setDisType(String disType) {
    this.disType = disType;
  }

  public String getReferredTo() {
    return referredTo;
  }

  public void setReferredTo(String referredTo) {
    this.referredTo = referredTo;
  }

  public String getDisDate() {
    return disDate;
  }

  public void setDisDate(String disDate) {
    this.disDate = disDate;
  }

  public String[] getAilmentDelete() {
    return ailmentDelete;
  }

  public void setAilmentDelete(String[] ailmentDelete) {
    this.ailmentDelete = ailmentDelete;
  }

  public String[] getDiagnosisDelete() {
    return diagnosisDelete;
  }

  public void setDiagnosisDelete(String[] diagnosisDelete) {
    this.diagnosisDelete = diagnosisDelete;
  }

  public String[] getTreatmentDelete() {
    return treatmentDelete;
  }

  public void setTreatmentDelete(String[] treatmentDelete) {
    this.treatmentDelete = treatmentDelete;
  }

}
