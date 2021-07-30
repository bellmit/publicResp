/*
 * Copyright (c) 2007-2009 Insta Health Solutions Pvt Ltd. All rights reserved.
 */

package com.insta.hms.usermanager;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserForm extends ActionForm {

  private static final long serialVersionUID = 1L;

  static Logger logger = LoggerFactory.getLogger(UserForm.class);

  private String op;
  private String name;
  private String password;
  private String roleId;
  private String remarks;
  private String counterId;
  private String specialization;
  private String labDepartment;
  private String masterUpdate;
  private String status;
  private String pharmacycounterId;
  private String pharmacyStoreId;
  private String[] multiStoreId;
  private String inventoryStoreId;
  private String fullname;
  /* Non persistent form fields */
  private String selUserName;
  private String confirmPassword;
  private String doctorId;
  private String schedulerDepartment;
  private String schedulerDefaultDoctor;
  private String prescriptionNoteTaker;
  private String bedViewDefaultWard;
  private int sampleCollectionCenter;
  private String poApprovalLimit;
  private String writeOffLimit;
  private String permissibleDiscountCap;
  private String emailId;
  private String mobileNo;
  private int userCenter;
  private String sharedLogin;
  private boolean ssoOnlyUser;
  private FormFile userSignature;
  private String allowSigUsageByOthers;
  private int discAuthorizer;
  private String[] serdeptid;
  private Integer reportCenter;
  private String loginControlsApplicable;
  private String firstName;
  private String middleName;
  private String lastName;
  private String gender;
  private String employeeId;
  private String profession;
  private String employeeCategory;
  private String employeeMajor;
  private boolean forcePasswordChange;
  private String allowBillFinalization;

  public String[] getSerdeptid() {
    return serdeptid;
  }

  public void setSerdeptid(String[] serdeptid) {
    this.serdeptid = serdeptid;
  }

  public String getSharedLogin() {
    return sharedLogin;
  }

  public void setSharedLogin(String sharedLogin) {
    this.sharedLogin = sharedLogin;
  }

  public String getPoApprovalLimit() {
    return poApprovalLimit;
  }

  public void setPoApprovalLimit(String poApprovalLimit) {
    this.poApprovalLimit = poApprovalLimit;
  }

  public int getSampleCollectionCenter() {
    return sampleCollectionCenter;
  }

  public void setSampleCollectionCenter(int sampleCollectionCenter) {
    this.sampleCollectionCenter = sampleCollectionCenter;
  }

  public String getBedViewDefaultWard() {
    return bedViewDefaultWard;
  }

  public void setBedViewDefaultWard(String bedViewDefaultWard) {
    this.bedViewDefaultWard = bedViewDefaultWard;
  }

  public String getDoctorId() {
    return doctorId;
  }

  public void setDoctorId(String doctorId) {
    this.doctorId = doctorId;
  }

  /*
   * Accessors
   */
  public String getOp() {
    return op;
  }

  public void setOp(String value) {
    op = value;
  }

  public String getName() {
    return name;
  }

  public void setName(String value) {
    name = value;
  }

  public String getSelUserName() {
    return selUserName;
  }

  public void setSelUserName(String value) {
    selUserName = value;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String value) {
    status = value;
  }

  public boolean isSsoOnlyUser() {
    return ssoOnlyUser;
  }

  public void setSsoOnlyUser(boolean ssoOnlyUser) {
    this.ssoOnlyUser = ssoOnlyUser;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String value) {
    password = value;
  }

  public String getConfirmPassword() {
    return confirmPassword;
  }

  public void setConfirmPassword(String value) {
    confirmPassword = value;
  }

  public String getRoleId() {
    return roleId;
  }

  public void setRoleId(String value) {
    roleId = value;
  }

  public String getRemarks() {
    return remarks;
  }

  public void setRemarks(String value) {
    remarks = value;
  }

  public String getCounterId() {
    return counterId;
  }

  public void setCounterId(String value) {
    counterId = value;
  }

  public String getSpecialization() {
    return specialization;
  }

  public void setSpecialization(String value) {
    specialization = value;
  }

  public String getLabDepartment() {
    return labDepartment;
  }

  public void setLabDepartment(String value) {
    labDepartment = value;
  }

  public String getMasterUpdate() {
    return masterUpdate;
  }

  public void setMasterUpdate(String value) {
    masterUpdate = value;
  }

  public String getPharmacycounterId() {
    return pharmacycounterId;
  }

  public void setPharmacycounterId(String value) {
    pharmacycounterId = value;
  }

  public String getPharmacyStoreId() {
    return pharmacyStoreId;
  }

  public void setPharmacyStoreId(String value) {
    pharmacyStoreId = value;
  }

  public String getInventoryStoreId() {
    return inventoryStoreId;
  }

  public void setInventoryStoreId(String value) {
    this.inventoryStoreId = value;
  }

  /*
   * Other methods
   */
  @Override
  public void reset(ActionMapping mapping, javax.servlet.http.HttpServletRequest request) {
    super.reset(mapping, request);
    this.name = null;
    this.selUserName = null;
    this.password = null;
    this.confirmPassword = null;
    this.remarks = null;
    this.op = null;
    this.roleId = null;
    this.counterId = null;
    this.specialization = null;
    this.masterUpdate = null;
    this.status = null;
    logger.debug("Resetting inside User form " + this);
  }

  public String getFullname() {
    return fullname;
  }

  public void setFullname(String fullname) {
    this.fullname = fullname;
  }

  public String getSchedulerDepartment() {
    return schedulerDepartment;
  }

  public void setSchedulerDepartment(String schedulerDepartment) {
    this.schedulerDepartment = schedulerDepartment;
  }

  public String getSchedulerDefaultDoctor() {
    return schedulerDefaultDoctor;
  }

  public void setSchedulerDefaultDoctor(String schedulerDefaultDoctor) {
    this.schedulerDefaultDoctor = schedulerDefaultDoctor;
  }

  public String getPrescription_note_taker() {
    return prescriptionNoteTaker;
  }

  public void setPrescription_note_taker(String prescriptionNoteTaker) {
    this.prescriptionNoteTaker = prescriptionNoteTaker;
  }

  public String[] getMultiStoreId() {
    return multiStoreId;
  }

  public void setMultiStoreId(String[] multiStoreId) {
    this.multiStoreId = multiStoreId;
  }

  public String getEmailId() {
    return emailId;
  }

  public void setEmailId(String emailId) {
    this.emailId = emailId;
  }

  public String getMobileNo() {
    return mobileNo;
  }

  public void setMobileNo(String mobileNo) {
    this.mobileNo = mobileNo;
  }

  public int getUserCenter() {
    return userCenter;
  }

  public void setUserCenter(int userCenter) {
    this.userCenter = userCenter;
  }

  public FormFile getUserSignature() {
    return userSignature;
  }

  public void setUserSignature(FormFile userSignature) {
    this.userSignature = userSignature;
  }

  public String getAllow_sig_usage_by_others() {
    return allowSigUsageByOthers;
  }

  public void setAllow_sig_usage_by_others(String allowSigUsageByOthers) {
    this.allowSigUsageByOthers = allowSigUsageByOthers;
  }

  public String getWriteOffLimit() {
    return writeOffLimit;
  }

  public void setWriteOffLimit(String writeOffLimit) {
    this.writeOffLimit = writeOffLimit;
  }

  public String getPermissibleDiscountCap() {
    return permissibleDiscountCap;
  }

  public void setPermissibleDiscountCap(String permissibleDiscountCap) {
    this.permissibleDiscountCap = permissibleDiscountCap;
  }

  public int getDiscAuthorizer() {
    return discAuthorizer;
  }

  public void setDiscAuthorizer(int discAuthorizer) {
    this.discAuthorizer = discAuthorizer;
  }

  public Integer getReportCenter() {
    return reportCenter;
  }

  public void setReportCenter(Integer reportCenter) {
    this.reportCenter = reportCenter;
  }

  public String getLoginControlsApplicable() {
    return loginControlsApplicable;
  }

  public void setLoginControlsApplicable(String loginControlsApplicable) {
    this.loginControlsApplicable = loginControlsApplicable;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getMiddleName() {
    return middleName;
  }

  public void setMiddleName(String middleName) {
    this.middleName = middleName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getGender() {
    return gender;
  }

  public void setGender(String gender) {
    this.gender = gender;
  }

  public String getEmployeeId() {
    return employeeId;
  }

  public void setEmployeeId(String employeeId) {
    this.employeeId = employeeId;
  }

  public String getProfession() {
    return profession;
  }

  public void setProfession(String profession) {
    this.profession = profession;
  }

  public String getEmployeeCategory() {
    return employeeCategory;
  }

  public void setEmployeeCategory(String employeeCategory) {
    this.employeeCategory = employeeCategory;
  }

  public String getEmployeeMajor() {
    return employeeMajor;
  }

  public void setEmployeeMajor(String employeeMajor) {
    this.employeeMajor = employeeMajor;
  }

  public boolean getForcePasswordChange() {
    return forcePasswordChange;
  }

  public void setForcePasswordChange(boolean forcePasswordChange) {
    this.forcePasswordChange = forcePasswordChange;
  }
  
  public String getAllowBillFinalization() {
    return allowBillFinalization;
  }

  public void setAllowBillFinalization(String allowBillFinalization) {
    this.allowBillFinalization = allowBillFinalization;
  }

}
