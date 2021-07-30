package com.insta.hms.patient;

import org.apache.struts.action.ActionForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PatientSearchForm extends ActionForm {
  static Logger logger = LoggerFactory.getLogger(PatientSearchForm.class);

  private String firstName;
  private String lastName;
  private String phone;
  private String mrno;
  private String patid;
  private String oldReg;
  private String caseFileNo;

  private String searchType;
  private String status;

  private String pageNum;
  private String sortOrder;
  private String condition;
  private String billno;
  private String activeCreditBillNo;

  private boolean statusAll;
  private boolean statusActive;
  private boolean statusInactive;
  private boolean statusNoVisit;
  private boolean typeAll;
  private boolean typeIP;
  private boolean typeOP;
  private boolean typeDiag;

  private boolean visitAll;
  private boolean visitNew;
  private boolean visitRevisit;

  private String fdate;
  private String tdate;

  private String[] department;
  private String[] doctor;
  private String[] refdoctor;

  private boolean sortReverse;

  private boolean disFinalizeAll;
  private boolean disFinalized;
  private boolean disNotFinalized;
  private String disFinalizedUser;
  private String disFinalizedFDate;
  private String disFinalizedTDate;

  /* Fields included as part of MRD Search */
  private String disfdate;
  private String distdate;

  /* Fields included as part of EMR Search */

  private String complaint;
  private String diagnosis;
  private String treatment;
  private String consultation;

  private String startPage;
  private String endPage;

  private String country;
  private String patientarea;
  private String countryid;
  private String patientstate;
  private String patientcity;
  private String stateid;
  private String cityid;

  private String regFieldName;
  private String regFieldValue;

  private String customRegFieldName;
  private String customRegFieldValue;

  public String getMrno() {
    return mrno;
  }

  public void setMrno(String mrno) {
    this.mrno = mrno;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getSearchType() {
    return searchType;
  }

  public void setSearchType(String searchType) {
    this.searchType = searchType;
  }

  public String getPageNum() {
    return pageNum;
  }

  public void setPageNum(String pageNum) {
    this.pageNum = pageNum;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getSortOrder() {
    return sortOrder;
  }

  public void setSortOrder(String sortOrder) {
    this.sortOrder = sortOrder;
  }

  public boolean getSortReverse() {
    return sortReverse;
  }

  public void setSortReverse(boolean sortReverse) {
    this.sortReverse = sortReverse;
  }

  public String getCondition() {
    return condition;
  }

  public void setCondition(String condition) {
    this.condition = condition;
  }

  public String getBillno() {
    return billno;
  }

  public void setBillno(String billno) {
    this.billno = billno;
  }

  public String getActiveCreditBillNo() {
    return activeCreditBillNo;
  }

  public void setActiveCreditBillNo(String activeCreditBillNo) {
    this.activeCreditBillNo = activeCreditBillNo;
  }

  public String getOldReg() {
    return oldReg;
  }

  public void setOldReg(String oldReg) {
    this.oldReg = oldReg;
  }

  public String getCaseFileNo() {
    return caseFileNo;
  }

  public void setCaseFileNo(String caseFileNo) {
    this.caseFileNo = caseFileNo;
  }

  public String getFdate() {
    return fdate;
  }

  public void setFdate(String fdate) {
    this.fdate = fdate;
  }

  public String getTdate() {
    return tdate;
  }

  public void setTdate(String tdate) {
    this.tdate = tdate;
  }

  public boolean getStatusAll() {
    return statusAll;
  }

  public void setStatusAll(boolean statusAll) {
    this.statusAll = statusAll;
  }

  public boolean getStatusActive() {
    return statusActive;
  }

  public void setStatusActive(boolean statusActive) {
    this.statusActive = statusActive;
  }

  public boolean getStatusInactive() {
    return statusInactive;
  }

  public void setStatusInactive(boolean statusInactive) {
    this.statusInactive = statusInactive;
  }

  public boolean getTypeAll() {
    return typeAll;
  }

  public void setTypeAll(boolean typeAll) {
    this.typeAll = typeAll;
  }

  public boolean getTypeIP() {
    return typeIP;
  }

  public void setTypeIP(boolean typeIP) {
    this.typeIP = typeIP;
  }

  public boolean getTypeOP() {
    return typeOP;
  }

  public void setTypeOP(boolean typeOP) {
    this.typeOP = typeOP;
  }

  public boolean getTypeDiag() {
    return typeDiag;
  }

  public void setTypeDiag(boolean typeDiag) {
    this.typeDiag = typeDiag;
  }

  public String[] getDepartment() {
    return department;
  }

  public void setDepartment(String[] department) {
    this.department = department;
  }

  public String[] getDoctor() {
    return doctor;
  }

  public void setDoctor(String[] doctor) {
    this.doctor = doctor;
  }

  public static Logger getLogger() {
    return logger;
  }

  public static void setLogger(Logger logger) {
    PatientSearchForm.logger = logger;
  }

  public String getComplaint() {
    return complaint;
  }

  public void setComplaint(String complaint) {
    this.complaint = complaint;
  }

  public String getConsultation() {
    return consultation;
  }

  public void setConsultation(String consultation) {
    this.consultation = consultation;
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

  public String getDisfdate() {
    return disfdate;
  }

  public void setDisfdate(String disfdate) {
    this.disfdate = disfdate;
  }

  public String getDistdate() {
    return distdate;
  }

  public void setDistdate(String distdate) {
    this.distdate = distdate;
  }

  public String getEndPage() {
    return endPage;
  }

  public void setEndPage(String endPage) {
    this.endPage = endPage;
  }

  public String getStartPage() {
    return startPage;
  }

  public void setStartPage(String startPage) {
    this.startPage = startPage;
  }

  public String getPatid() {
    return patid;
  }

  public void setPatid(String patid) {
    this.patid = patid;
  }

  public String[] getRefdoctor() {
    return refdoctor;
  }

  public void setRefdoctor(String[] refdoctor) {
    this.refdoctor = refdoctor;
  }

  public boolean getVisitAll() {
    return visitAll;
  }

  public void setVisitAll(boolean visitAll) {
    this.visitAll = visitAll;
  }

  public boolean getVisitNew() {
    return visitNew;
  }

  public void setVisitNew(boolean visitNew) {
    this.visitNew = visitNew;
  }

  public boolean getVisitRevisit() {
    return visitRevisit;
  }

  public void setVisitRevisit(boolean visitRevisit) {
    this.visitRevisit = visitRevisit;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public String getPatientarea() {
    return patientarea;
  }

  public void setPatientarea(String patientarea) {
    this.patientarea = patientarea;
  }

  public String getCountryid() {
    return countryid;
  }

  public void setCountryid(String countryid) {
    this.countryid = countryid;
  }

  public String getPatientcity() {
    return patientcity;
  }

  public void setPatientcity(String patientcity) {
    this.patientcity = patientcity;
  }

  public String getPatientstate() {
    return patientstate;
  }

  public void setPatientstate(String patientstate) {
    this.patientstate = patientstate;
  }

  public String getCityid() {
    return cityid;
  }

  public void setCityid(String cityid) {
    this.cityid = cityid;
  }

  public String getStateid() {
    return stateid;
  }

  public void setStateid(String stateid) {
    this.stateid = stateid;
  }

  public boolean isDisFinalizeAll() {
    return disFinalizeAll;
  }

  public void setDisFinalizeAll(boolean disFinalizeAll) {
    this.disFinalizeAll = disFinalizeAll;
  }

  public boolean isDisFinalized() {
    return disFinalized;
  }

  public void setDisFinalized(boolean disFinalized) {
    this.disFinalized = disFinalized;
  }

  public String getDisFinalizedUser() {
    return disFinalizedUser;
  }

  public void setDisFinalizedUser(String disFinalizedUser) {
    this.disFinalizedUser = disFinalizedUser;
  }

  public boolean isDisNotFinalized() {
    return disNotFinalized;
  }

  public void setDisNotFinalized(boolean disNotFinalized) {
    this.disNotFinalized = disNotFinalized;
  }

  public String getDisFinalizedFDate() {
    return disFinalizedFDate;
  }

  public void setDisFinalizedFDate(String disFinalizedFDate) {
    this.disFinalizedFDate = disFinalizedFDate;
  }

  public String getDisFinalizedTDate() {
    return disFinalizedTDate;
  }

  public void setDisFinalizedTDate(String disFinalizedTDate) {
    this.disFinalizedTDate = disFinalizedTDate;
  }

  public boolean getStatusNoVisit() {
    return statusNoVisit;
  }

  public void setStatusNoVisit(boolean statusNoVisit) {
    this.statusNoVisit = statusNoVisit;
  }

  public String getRegFieldName() {
    return regFieldName;
  }

  public void setRegFieldName(String regFieldName) {
    this.regFieldName = regFieldName;
  }

  public String getRegFieldValue() {
    return regFieldValue;
  }

  public void setRegFieldValue(String regFieldValue) {
    this.regFieldValue = regFieldValue;
  }

  public String getCustomRegFieldName() {
    return customRegFieldName;
  }

  public void setCustomRegFieldName(String customRegFieldName) {
    this.customRegFieldName = customRegFieldName;
  }

  public String getCustomRegFieldValue() {
    return customRegFieldValue;
  }

  public void setCustomRegFieldValue(String customRegFieldValue) {
    this.customRegFieldValue = customRegFieldValue;
  }

}
