package com.bob.hms.adminmasters.doctor;

// TODO: Auto-generated Javadoc
/*
 * This class Represents DOCTORS Table in database
 *
 */

/**
 * The Class Doctor.
 */
public class Doctor {

  /** The doctor id. */
  private String doctorId;
  
  /** The doctor name. */
  private String doctorName;
  
  /** The dept id. */
  private String deptId;
  
  /** The specialization. */
  private String specialization;
  
  /** The doctor type. */
  private String doctorType;
  
  /** The doctor address. */
  private String doctorAddress;
  
  /** The doctor mobile. */
  private String doctorMobile;
  
  /** The doctor mail id. */
  private String doctorMailId;
  
  /** The validity. */
  private int validity;
  
  /** The allowed revisit count. */
  private int allowedRevisitCount;
  
  /** The is OT doctor. */
  private String isOTDoctor;
  
  /** The is consulting doctor. */
  private String isConsultingDoctor;
  
  /** The status. */
  private String status;
  
  /** The schedulable. */
  private boolean schedulable;

  /** The qualification. */
  private String qualification;
  
  /** The reg number. */
  private String regNumber;
  
  /** The clinic phone. */
  private String clinicPhone;
  
  /** The res phone. */
  private String resPhone;
  
  /** The payment cat id. */
  private int paymentCatId;
  
  /** The pay eligible. */
  private String payEligible;
  
  /** The license number. */
  private String licenseNumber;

  /** The doctor pay in per. */
  // payment varibles
  private String doctorPayInPer;
  
  /** The doctor pay for operation. */
  private Double doctorPayForOperation;
  
  /** The doctor pay for ip. */
  private Double doctorPayForIp;
  
  /** The doctor pay for op. */
  private Double doctorPayForOp;
  
  /** The ref pay in per. */
  private String refPayInPer;
  
  /** The ref pay for operation. */
  private Double refPayForOperation;
  
  /** The ref pay for ip. */
  private Double refPayForIp;
  
  /** The ref pay for op. */
  private Double refPayForOp;

  /** The doctor OP charge. */
  // Charges varying on Organization.
  private Double doctorOPCharge;
  
  /** The sub op charge. */
  private Double subOpCharge;

  /** The doctor priv OP charge. */
  private Double doctorPrivOPCharge;
  
  /** The sub priv op charge. */
  private Double subPrivOpCharge;


  /**
   * Gets the doctor OP charge.
   *
   * @return the doctor OP charge
   */
  public Double getDoctorOPCharge() {
    return doctorOPCharge;
  }

  /**
   * Sets the doctor OP charge.
   *
   * @param doctorOPCharge the new doctor OP charge
   */
  public void setDoctorOPCharge(Double doctorOPCharge) {
    this.doctorOPCharge = doctorOPCharge;
  }

  /**
   * Gets the sub op charge.
   *
   * @return the sub op charge
   */
  public Double getSubOpCharge() {
    return subOpCharge;
  }

  /**
   * Sets the sub op charge.
   *
   * @param subOpCharge the new sub op charge
   */
  public void setSubOpCharge(Double subOpCharge) {
    this.subOpCharge = subOpCharge;
  }

  /**
   * Gets the dept id.
   *
   * @return the dept id
   */
  public String getDeptId() {
    return deptId;
  }

  /**
   * Sets the dept id.
   *
   * @param deptId the new dept id
   */
  public void setDeptId(String deptId) {
    this.deptId = deptId;
  }

  /**
   * Gets the doctor address.
   *
   * @return the doctor address
   */
  public String getDoctorAddress() {
    return doctorAddress;
  }

  /**
   * Sets the doctor address.
   *
   * @param doctorAddress the new doctor address
   */
  public void setDoctorAddress(String doctorAddress) {
    this.doctorAddress = doctorAddress;
  }

  /**
   * Gets the doctor id.
   *
   * @return the doctor id
   */
  public String getDoctorId() {
    return doctorId;
  }

  /**
   * Sets the doctor id.
   *
   * @param doctorId the new doctor id
   */
  public void setDoctorId(String doctorId) {
    this.doctorId = doctorId;
  }

  /**
   * Gets the doctor mail id.
   *
   * @return the doctor mail id
   */
  public String getDoctorMailId() {
    return doctorMailId;
  }

  /**
   * Sets the doctor mail id.
   *
   * @param doctorMailId the new doctor mail id
   */
  public void setDoctorMailId(String doctorMailId) {
    this.doctorMailId = doctorMailId;
  }

  /**
   * Gets the doctor mobile.
   *
   * @return the doctor mobile
   */
  public String getDoctorMobile() {
    return doctorMobile;
  }

  /**
   * Sets the doctor mobile.
   *
   * @param doctorMobile the new doctor mobile
   */
  public void setDoctorMobile(String doctorMobile) {
    this.doctorMobile = doctorMobile;
  }

  /**
   * Gets the doctor name.
   *
   * @return the doctor name
   */
  public String getDoctorName() {
    return doctorName;
  }

  /**
   * Sets the doctor name.
   *
   * @param doctorName the new doctor name
   */
  public void setDoctorName(String doctorName) {
    this.doctorName = doctorName;
  }

  /**
   * Gets the doctor pay for ip.
   *
   * @return the doctor pay for ip
   */
  public Double getDoctorPayForIp() {
    return doctorPayForIp;
  }

  /**
   * Sets the doctor pay for ip.
   *
   * @param doctorPayForIp the new doctor pay for ip
   */
  public void setDoctorPayForIp(Double doctorPayForIp) {
    this.doctorPayForIp = doctorPayForIp;
  }

  /**
   * Gets the doctor pay for op.
   *
   * @return the doctor pay for op
   */
  public Double getDoctorPayForOp() {
    return doctorPayForOp;
  }

  /**
   * Sets the doctor pay for op.
   *
   * @param doctorPayForOp the new doctor pay for op
   */
  public void setDoctorPayForOp(Double doctorPayForOp) {
    this.doctorPayForOp = doctorPayForOp;
  }

  /**
   * Gets the doctor pay for operation.
   *
   * @return the doctor pay for operation
   */
  public Double getDoctorPayForOperation() {
    return doctorPayForOperation;
  }

  /**
   * Sets the doctor pay for operation.
   *
   * @param doctorPayForOperation the new doctor pay for operation
   */
  public void setDoctorPayForOperation(Double doctorPayForOperation) {
    this.doctorPayForOperation = doctorPayForOperation;
  }

  /**
   * Gets the doctor pay in per.
   *
   * @return the doctor pay in per
   */
  public String getDoctorPayInPer() {
    return doctorPayInPer;
  }

  /**
   * Sets the doctor pay in per.
   *
   * @param doctorPayInPer the new doctor pay in per
   */
  public void setDoctorPayInPer(String doctorPayInPer) {
    this.doctorPayInPer = doctorPayInPer;
  }

  /**
   * Gets the doctor type.
   *
   * @return the doctor type
   */
  public String getDoctorType() {
    return doctorType;
  }

  /**
   * Sets the doctor type.
   *
   * @param doctorType the new doctor type
   */
  public void setDoctorType(String doctorType) {
    this.doctorType = doctorType;
  }

  /**
   * Gets the checks if is consulting doctor.
   *
   * @return the checks if is consulting doctor
   */
  public String getIsConsultingDoctor() {
    return isConsultingDoctor;
  }

  /**
   * Sets the checks if is consulting doctor.
   *
   * @param isConsultingDoctor the new checks if is consulting doctor
   */
  public void setIsConsultingDoctor(String isConsultingDoctor) {
    this.isConsultingDoctor = isConsultingDoctor;
  }

  /**
   * Gets the checks if is OT doctor.
   *
   * @return the checks if is OT doctor
   */
  public String getIsOTDoctor() {
    return isOTDoctor;
  }

  /**
   * Sets the checks if is OT doctor.
   *
   * @param isOTDoctor the new checks if is OT doctor
   */
  public void setIsOTDoctor(String isOTDoctor) {
    this.isOTDoctor = isOTDoctor;
  }

  /**
   * Gets the ref pay for ip.
   *
   * @return the ref pay for ip
   */
  public Double getRefPayForIp() {
    return refPayForIp;
  }

  /**
   * Sets the ref pay for ip.
   *
   * @param refPayForIp the new ref pay for ip
   */
  public void setRefPayForIp(Double refPayForIp) {
    this.refPayForIp = refPayForIp;
  }

  /**
   * Gets the ref pay for op.
   *
   * @return the ref pay for op
   */
  public Double getRefPayForOp() {
    return refPayForOp;
  }

  /**
   * Sets the ref pay for op.
   *
   * @param refPayForOp the new ref pay for op
   */
  public void setRefPayForOp(Double refPayForOp) {
    this.refPayForOp = refPayForOp;
  }

  /**
   * Gets the ref pay for operation.
   *
   * @return the ref pay for operation
   */
  public Double getRefPayForOperation() {
    return refPayForOperation;
  }

  /**
   * Sets the ref pay for operation.
   *
   * @param refPayForOperation the new ref pay for operation
   */
  public void setRefPayForOperation(Double refPayForOperation) {
    this.refPayForOperation = refPayForOperation;
  }

  /**
   * Gets the ref pay in per.
   *
   * @return the ref pay in per
   */
  public String getRefPayInPer() {
    return refPayInPer;
  }

  /**
   * Sets the ref pay in per.
   *
   * @param refPayInPer the new ref pay in per
   */
  public void setRefPayInPer(String refPayInPer) {
    this.refPayInPer = refPayInPer;
  }

  /**
   * Gets the specialization.
   *
   * @return the specialization
   */
  public String getSpecialization() {
    return specialization;
  }

  /**
   * Sets the specialization.
   *
   * @param specialization the new specialization
   */
  public void setSpecialization(String specialization) {
    this.specialization = specialization;
  }

  /**
   * Gets the status.
   *
   * @return the status
   */
  public String getStatus() {
    return status;
  }

  /**
   * Sets the status.
   *
   * @param status the new status
   */
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   * Gets the validity.
   *
   * @return the validity
   */
  public int getValidity() {
    return validity;
  }

  /**
   * Sets the validity.
   *
   * @param validity the new validity
   */
  public void setValidity(int validity) {
    this.validity = validity;
  }

  /**
   * Gets the doctor priv OP charge.
   *
   * @return the doctor priv OP charge
   */
  public Double getDoctorPrivOPCharge() {
    return doctorPrivOPCharge;
  }

  /**
   * Sets the doctor priv OP charge.
   *
   * @param doctorPrivOPCharge the new doctor priv OP charge
   */
  public void setDoctorPrivOPCharge(Double doctorPrivOPCharge) {
    this.doctorPrivOPCharge = doctorPrivOPCharge;
  }

  /**
   * Gets the sub priv op charge.
   *
   * @return the sub priv op charge
   */
  public Double getSubPrivOpCharge() {
    return subPrivOpCharge;
  }

  /**
   * Sets the sub priv op charge.
   *
   * @param subPrivOpCharge the new sub priv op charge
   */
  public void setSubPrivOpCharge(Double subPrivOpCharge) {
    this.subPrivOpCharge = subPrivOpCharge;
  }

  /**
   * Checks if is schedulable.
   *
   * @return true, if is schedulable
   */
  public boolean isSchedulable() {
    return schedulable;
  }

  /**
   * Sets the schedulable.
   *
   * @param schedulable the new schedulable
   */
  public void setSchedulable(boolean schedulable) {
    this.schedulable = schedulable;
  }

  /**
   * Gets the clinic phone.
   *
   * @return the clinic phone
   */
  public String getClinicPhone() {
    return clinicPhone;
  }

  /**
   * Sets the clinic phone.
   *
   * @param clinicPhone the new clinic phone
   */
  public void setClinicPhone(String clinicPhone) {
    this.clinicPhone = clinicPhone;
  }

  /**
   * Gets the qualification.
   *
   * @return the qualification
   */
  public String getQualification() {
    return qualification;
  }

  /**
   * Sets the qualification.
   *
   * @param qualification the new qualification
   */
  public void setQualification(String qualification) {
    this.qualification = qualification;
  }

  /**
   * Gets the reg number.
   *
   * @return the reg number
   */
  public String getRegNumber() {
    return regNumber;
  }

  /**
   * Sets the reg number.
   *
   * @param regNumber the new reg number
   */
  public void setRegNumber(String regNumber) {
    this.regNumber = regNumber;
  }

  /**
   * Gets the res phone.
   *
   * @return the res phone
   */
  public String getResPhone() {
    return resPhone;
  }

  /**
   * Sets the res phone.
   *
   * @param resPhone the new res phone
   */
  public void setResPhone(String resPhone) {
    this.resPhone = resPhone;
  }

  /**
   * Gets the pay eligible.
   *
   * @return the pay eligible
   */
  public String getPayEligible() {
    return payEligible;
  }

  /**
   * Sets the pay eligible.
   *
   * @param payEligible the new pay eligible
   */
  public void setPayEligible(String payEligible) {
    this.payEligible = payEligible;
  }

  /**
   * Gets the payment cat id.
   *
   * @return the payment cat id
   */
  public int getPaymentCatId() {
    return paymentCatId;
  }

  /**
   * Sets the payment cat id.
   *
   * @param paymentCatId the new payment cat id
   */
  public void setPaymentCatId(int paymentCatId) {
    this.paymentCatId = paymentCatId;
  }

  /**
   * Gets the license number.
   *
   * @return the license number
   */
  public String getLicenseNumber() {
    return licenseNumber;
  }

  /**
   * Sets the license number.
   *
   * @param licenseNumber the new license number
   */
  public void setLicenseNumber(String licenseNumber) {
    this.licenseNumber = licenseNumber;
  }

  /**
   * Gets the allowed revisit count.
   *
   * @return the allowed revisit count
   */
  public int getAllowedRevisitCount() {
    return allowedRevisitCount;
  }

  /**
   * Sets the allowed revisit count.
   *
   * @param allowedRevisitCount the new allowed revisit count
   */
  public void setAllowedRevisitCount(int allowedRevisitCount) {
    this.allowedRevisitCount = allowedRevisitCount;
  }

}
