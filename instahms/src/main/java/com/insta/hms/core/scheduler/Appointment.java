package com.insta.hms.core.scheduler;

import java.io.Serializable;
import java.math.BigInteger;
import java.sql.Date;

// TODO: Auto-generated Javadoc
/**
 * The Class Appointment.
 */
public class Appointment implements Serializable {
  
  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 3447019975211530182L;
  
  /** The mr no. */
  private String mrNo;
  
  /** The visit id. */
  private String visitId;
  
  /** The patient name. */
  private String patientName;
  
  /** The phone no. */
  private String phoneNo;
  
  /** The phone country code. */
  private String phoneCountryCode;
  
  /** The remarks. */
  private String remarks;
  
  /** The complaint. */
  private String complaint;
  
  /** The complaint name. */
  private String complaintName;
  
  /** The appointment id. */
  private Integer appointmentId;
  
  /** The schedule id. */
  private Integer scheduleId;
  
  /** The schedule name. */
  private String scheduleName;
  
  /** The appointment time. */
  private java.sql.Timestamp appointmentTime;
  
  /** The appointment duration. */
  private Integer appointmentDuration;
  
  /** The appoint status. */
  private String appointStatus;
  
  /** The booked by. */
  private String bookedBy;
  
  /** The changed by. */
  private String changedBy;
  
  /** The changed time. */
  private java.sql.Timestamp changedTime;
  
  /** The booked time. */
  private java.sql.Timestamp bookedTime;
  
  /** The completed time. */
  private java.sql.Timestamp completedTime;
  
  /** The cancel reason. */
  private String cancelReason;
  
  /** The consultation type id. */
  private Integer consultationTypeId;
  
  /** The resource type. */
  private String resourceType;
  
  /** The scheduler visit type. */
  private String schedulerVisitType;
  
  /** The booked as secondary resource. */
  private boolean bookedAsSecondaryResource;
  
  /** The sch prior auth id. */
  private String schPriorAuthId;
  
  /** The sch prior auth mode id. */
  private Integer schPriorAuthModeId;
  
  /** The center id. */
  private Integer centerId;
  
  /** The center code. */
  private String centerCode;
  
  /** The center name. */
  private String centerName;
  
  /** The department name. */
  private String departmentName;
  
  /** The cond doc id. */
  private String condDocId;
  
  /** The waitlist. */
  private Integer waitlist;
  
  /** The visit mode. */
  private String visitMode;
  
  /** The teleconsult URL. */
  private String teleconsultURL;
  
  /**
   * Gets the teleconsult URL.
   *
   * @return the teleconsult URL
   */
  public String getTeleconsultURL() {
    return teleconsultURL;
  }

  /**
   * Sets the teleconsult URL.
   *
   * @param teleconsultURL the new teleconsult URL
   */
  public void setTeleconsultURL(String teleconsultURL) {
    this.teleconsultURL = teleconsultURL;
  }

  /**
   * Gets the visit mode.
   *
   * @return the visit mode
   */
  public String getVisitMode() {
    return visitMode;
  }

  /**
   * Sets the visit mode.
   *
   * @param visitMode the new visit mode
   */
  public void setVisitMode(String visitMode) {
    this.visitMode = visitMode;
  }

  /**
   * Gets the cond doc id.
   *
   * @return the cond doc id
   */
  public String getCondDocId() {
    return condDocId;
  }

  /**
   * Sets the cond doc id.
   *
   * @param condDocId the new cond doc id
   */
  public void setCondDocId(String condDocId) {
    this.condDocId = condDocId;
  }

  /** The presc doc id. */
  private String prescDocId;
  
  /** The payment status. */
  private String paymentStatus;
  
  /** The salutation name. */
  private String salutationName;
  
  /** The appt token. */
  private Integer apptToken;
  
  /** The paid at source. */
  private String paidAtSource;
  
  /** The bill type. */
  private String billType;
  
  /** The rescheduled. */
  private String rescheduled;
  
  /** The orig appt time. */
  private java.sql.Timestamp origApptTime;
  
  /** The unique appt ind. */
  private Integer uniqueApptInd;
  
  /** The prim res id. */
  private String primResId;
  
  /** The app source id. */
  private Integer appSourceId;
  
  /** The practo appointment id. */
  private String practoAppointmentId;
  
  /** The primary sponsor id. */
  private String primarySponsorId;
  
  /** The primary sponsor co. */
  private String primarySponsorCo;
  
  /** The plan id. */
  private Integer planId;
  
  /** The plan type id. */
  private Integer planTypeId;
  
  /** The member id. */
  private String memberId;
  
  /** The patient dob. */
  private Date patientDob;
  
  /** The patient age. */
  private Integer patientAge;
  
  /** The patient age units. */
  private String patientAgeUnits;
  
  /** The patient gender. */
  private String patientGender;
  
  /** The patient category. */
  private Integer patientCategory;
  
  /** The patient address. */
  private String patientAddress;
  
  /** The patient area. */
  private String patientArea;
  
  /** The patient state. */
  private String patientState;
  
  /** The patient city. */
  private String patientCity;
  
  /** The patient country. */
  private String patientCountry;
  
  /** The patient nationality. */
  private String patientNationality;
  
  /** The patient email id. */
  private String patientEmailId;
  
  /** The patient citizen id. */
  private String patientCitizenId;
  
  /** The vip status. */
  private String vipStatus;
  
  /** The contact id. */
  private Integer contactId;
  
  /** The parent pack ob id. */
  private Integer parentPackObId;
  
  /** The package id. */
  private Integer packageId;
  
  /** The appointment pack group id. */
  private Integer appointmentPackGroupId;
  
  /** The patient presc id. */
  private Long patientPrescId;
  
  /** The followup id. */
  private String followupId;

  
  /**
   * Gets the followup id.
   *
   * @return the followup id
   */
  public String getFollowupId() {
    return followupId;
  }

  /**
   * Sets the followup id.
   *
   * @param followupId the new followup id
   */
  public void setFollowupId(String followupId) {
    this.followupId = followupId;
  }

  /**
   * Gets the patient presc id.
   *
   * @return the patient presc id
   */
  public Long getPatientPrescId() {
    return patientPrescId;
  }

  /**
   * Sets the patient presc id.
   *
   * @param patientPrescId the new patient presc id
   */
  public void setPatientPrescId(Long patientPrescId) {
    this.patientPrescId = patientPrescId;
  }

  /**
   * Gets the changed time.
   *
   * @return the changed time
   */
  public java.sql.Timestamp getChangedTime() {
    return changedTime;
  }

  /**
   * Sets the changed time.
   *
   * @param changedTime the new changed time
   */
  public void setChangedTime(java.sql.Timestamp changedTime) {
    this.changedTime = changedTime;
  }

  /**
   * Gets the patient citizen id.
   *
   * @return the patient citizen id
   */
  public String getPatientCitizenId() {
    return patientCitizenId;
  }

  /**
   * Sets the patient citizen id.
   *
   * @param patientCitizenId the new patient citizen id
   */
  public void setPatientCitizenId(String patientCitizenId) {
    this.patientCitizenId = patientCitizenId;
  }

  /**
   * Gets the patient dob.
   *
   * @return the patient dob
   */
  public Date getPatientDob() {
    return patientDob;
  }

  /**
   * Sets the patient dob.
   *
   * @param date the new patient dob
   */
  public void setPatientDob(Date date) {
    this.patientDob = date;
  }

  /**
   * Gets the patient age.
   *
   * @return the patient age
   */
  public Integer getPatientAge() {
    return patientAge;
  }

  /**
   * Sets the patient age.
   *
   * @param patientAge the new patient age
   */
  public void setPatientAge(Integer patientAge) {
    this.patientAge = patientAge;
  }

  /**
   * Gets the patient age units.
   *
   * @return the patient age units
   */
  public String getPatientAgeUnits() {
    return patientAgeUnits;
  }

  /**
   * Sets the patient age units.
   *
   * @param patientAgeUnits the new patient age units
   */
  public void setPatientAgeUnits(String patientAgeUnits) {
    this.patientAgeUnits = patientAgeUnits;
  }

  /**
   * Gets the patient gender.
   *
   * @return the patient gender
   */
  public String getPatientGender() {
    return patientGender;
  }

  /**
   * Sets the patient gender.
   *
   * @param patientGender the new patient gender
   */
  public void setPatientGender(String patientGender) {
    this.patientGender = patientGender;
  }

  /**
   * Gets the patient category.
   *
   * @return the patient category
   */
  public Integer getPatientCategory() {
    return patientCategory;
  }

  /**
   * Sets the patient category.
   *
   * @param patientCategory2 the new patient category
   */
  public void setPatientCategory(Integer patientCategory2) {
    this.patientCategory = patientCategory2;
  }

  /**
   * Gets the patient address.
   *
   * @return the patient address
   */
  public String getPatientAddress() {
    return patientAddress;
  }

  /**
   * Sets the patient address.
   *
   * @param patientAddress the new patient address
   */
  public void setPatientAddress(String patientAddress) {
    this.patientAddress = patientAddress;
  }

  /**
   * Gets the patient area.
   *
   * @return the patient area
   */
  public String getPatientArea() {
    return patientArea;
  }

  /**
   * Sets the patient area.
   *
   * @param patientArea the new patient area
   */
  public void setPatientArea(String patientArea) {
    this.patientArea = patientArea;
  }

  /**
   * Gets the patient state.
   *
   * @return the patient state
   */
  public String getPatientState() {
    return patientState;
  }

  /**
   * Sets the patient state.
   *
   * @param patientState the new patient state
   */
  public void setPatientState(String patientState) {
    this.patientState = patientState;
  }

  /**
   * Gets the patient city.
   *
   * @return the patient city
   */
  public String getPatientCity() {
    return patientCity;
  }

  /**
   * Sets the patient city.
   *
   * @param patientCity the new patient city
   */
  public void setPatientCity(String patientCity) {
    this.patientCity = patientCity;
  }

  /**
   * Gets the patient country.
   *
   * @return the patient country
   */
  public String getPatientCountry() {
    return patientCountry;
  }

  /**
   * Sets the patient country.
   *
   * @param patientCountry the new patient country
   */
  public void setPatientCountry(String patientCountry) {
    this.patientCountry = patientCountry;
  }

  /**
   * Gets the patient nationality.
   *
   * @return the patient nationality
   */
  public String getPatientNationality() {
    return patientNationality;
  }

  /**
   * Sets the patient nationality.
   *
   * @param patientNationality the new patient nationality
   */
  public void setPatientNationality(String patientNationality) {
    this.patientNationality = patientNationality;
  }

  /**
   * Gets the patient email id.
   *
   * @return the patient email id
   */
  public String getPatientEmailId() {
    return patientEmailId;
  }

  /**
   * Sets the patient email id.
   *
   * @param patientEmailId the new patient email id
   */
  public void setPatientEmailId(String patientEmailId) {
    this.patientEmailId = patientEmailId;
  }

  /**
   * Gets the vip status.
   *
   * @return the vip status
   */
  public String getVipStatus() {
    return vipStatus;
  }

  /**
   * Sets the vip status.
   *
   * @param vipStatus the new vip status
   */
  public void setVipStatus(String vipStatus) {
    this.vipStatus = vipStatus;
  }

  /**
   * Gets the prim res id.
   *
   * @return the prim res id
   */
  public String getPrim_res_id() {
    return primResId;
  }

  /**
   * Sets the prim res id.
   *
   * @param primResId the new prim res id
   */
  public void setPrim_res_id(String primResId) {
    this.primResId = primResId;
  }

  /**
   * Gets the unique appt ind.
   *
   * @return the unique appt ind
   */
  public Integer getUnique_appt_ind() {
    return uniqueApptInd;
  }

  /**
   * Gets the phone country code.
   *
   * @return the phone country code
   */
  public String getPhoneCountryCode() {
    return phoneCountryCode;
  }

  /**
   * Sets the phone country code.
   *
   * @param code the new phone country code
   */
  public void setPhoneCountryCode(String code) {
    phoneCountryCode = code;
  }

  /**
   * Sets the unique appt ind.
   *
   * @param uniqueApptInd the new unique appt ind
   */
  public void setUnique_appt_ind(Integer uniqueApptInd) {
    this.uniqueApptInd = uniqueApptInd;
  }

  /**
   * Gets the rescheduled.
   *
   * @return the rescheduled
   */
  public String getRescheduled() {
    return rescheduled;
  }

  /**
   * Sets the rescheduled.
   *
   * @param rescheduled the new rescheduled
   */
  public void setRescheduled(String rescheduled) {
    this.rescheduled = rescheduled;
  }

  /**
   * Gets the orig appt time.
   *
   * @return the orig appt time
   */
  public java.sql.Timestamp getOrigApptTime() {
    return origApptTime;
  }

  /**
   * Sets the orig appt time.
   *
   * @param origApptTime the new orig appt time
   */
  public void setOrigApptTime(java.sql.Timestamp origApptTime) {
    this.origApptTime = origApptTime;
  }

  /**
   * Gets the bill type.
   *
   * @return the bill type
   */
  public String getBillType() {
    return billType;
  }

  /**
   * Sets the bill type.
   *
   * @param billType the new bill type
   */
  public void setBillType(String billType) {
    this.billType = billType;
  }

  /**
   * Gets the paid at source.
   *
   * @return the paid at source
   */
  public String getPaidAtSource() {
    return paidAtSource;
  }

  /**
   * Sets the paid at source.
   *
   * @param paidAtSource the new paid at source
   */
  public void setPaidAtSource(String paidAtSource) {
    this.paidAtSource = paidAtSource;
  }

  /**
   * Gets the appt token.
   *
   * @return the appt token
   */
  public Integer getApptToken() {
    return apptToken;
  }

  /**
   * Sets the appt token.
   *
   * @param apptToken the new appt token
   */
  public void setApptToken(Integer apptToken) {
    this.apptToken = apptToken;
  }

  /**
   * Gets the payment status.
   *
   * @return the payment status
   */
  public String getPaymentStatus() {
    return paymentStatus;
  }

  /**
   * Sets the payment status.
   *
   * @param paymentStatus the new payment status
   */
  public void setPaymentStatus(String paymentStatus) {
    this.paymentStatus = paymentStatus;
  }

  /**
   * Gets the presc doc id.
   *
   * @return the presc doc id
   */
  public String getPrescDocId() {
    return prescDocId;
  }

  /**
   * Sets the presc doc id.
   *
   * @param prescDocId the new presc doc id
   */
  public void setPrescDocId(String prescDocId) {
    this.prescDocId = prescDocId;
  }

  /**
   * Gets the department name.
   *
   * @return the department name
   */
  public String getDepartmentName() {
    return departmentName;
  }

  /**
   * Sets the department name.
   *
   * @param departmentName the new department name
   */
  public void setDepartmentName(String departmentName) {
    this.departmentName = departmentName;
  }

  /**
   * Gets the center name.
   *
   * @return the center name
   */
  public String getCenterName() {
    return centerName;
  }

  /**
   * Sets the center name.
   *
   * @param centerName the new center name
   */
  public void setCenterName(String centerName) {
    this.centerName = centerName;
  }

  /**
   * Gets the center id.
   *
   * @return the center id
   */
  public Integer getCenterId() {
    return centerId;
  }

  /**
   * Sets the center id.
   *
   * @param centerId the new center id
   */
  public void setCenterId(Integer centerId) {
    this.centerId = centerId;
  }

  /**
   * Gets the sch prior auth id.
   *
   * @return the sch prior auth id
   */
  public String getSchPriorAuthId() {
    return schPriorAuthId;
  }

  /**
   * Sets the sch prior auth id.
   *
   * @param schPriorAuthId the new sch prior auth id
   */
  public void setSchPriorAuthId(String schPriorAuthId) {
    this.schPriorAuthId = schPriorAuthId;
  }

  /**
   * Gets the sch prior auth mode id.
   *
   * @return the sch prior auth mode id
   */
  public Integer getSchPriorAuthModeId() {
    return schPriorAuthModeId;
  }

  /**
   * Sets the sch prior auth mode id.
   *
   * @param schPriorAuthModeId the new sch prior auth mode id
   */
  public void setSchPriorAuthModeId(Integer schPriorAuthModeId) {
    this.schPriorAuthModeId = schPriorAuthModeId;
  }

  /**
   * Checks if is booked as secondary resource.
   *
   * @return true, if is booked as secondary resource
   */
  public boolean isBookedAsSecondaryResource() {
    return bookedAsSecondaryResource;
  }

  /**
   * Sets the booked as secondary resource.
   *
   * @param bookedAsSecondaryResource the new booked as secondary resource
   */
  public void setBookedAsSecondaryResource(boolean bookedAsSecondaryResource) {
    this.bookedAsSecondaryResource = bookedAsSecondaryResource;
  }

  /**
   * Gets the package id.
   *
   * @return the package id
   */
  public Integer getPackageId() {
    return packageId;
  }

  /**
   * Gets the parent pack ob id.
   *
   * @return the parent pack ob id
   */
  public Integer getParentPackObId() {
    return parentPackObId;
  }

  /**
   * Sets the parent pack ob id.
   *
   * @param parentPackObId the new parent pack ob id
   */
  public void setParentPackObId(Integer parentPackObId) {
    this.parentPackObId = parentPackObId;
  }

  /**
   * Gets the appointment pack group id.
   *
   * @return the appointment pack group id
   */
  public Integer getAppointmentPackGroupId() {
    return appointmentPackGroupId;
  }

  /**
   * Sets the appointment pack group id.
   *
   * @param appointmentPackGroupId the new appointment pack group id
   */
  public void setAppointmentPackGroupId(Integer appointmentPackGroupId) {
    this.appointmentPackGroupId = appointmentPackGroupId;
  }

  /**
   * Sets the package id.
   *
   * @param packageId the new package id
   */
  public void setPackageId(Integer packageId) {
    this.packageId = packageId;
  }

  /**
   * Gets the consultation type id.
   *
   * @return the consultation type id
   */
  public Integer getConsultationTypeId() {
    return consultationTypeId;
  }

  /**
   * Sets the consultation type id.
   *
   * @param consultationTypeId the new consultation type id
   */
  public void setConsultationTypeId(Integer consultationTypeId) {
    this.consultationTypeId = consultationTypeId;
  }

  /**
   * Gets the cancel reason.
   *
   * @return the cancel reason
   */
  public String getCancelReason() {
    return cancelReason;
  }

  /**
   * Sets the cancel reason.
   *
   * @param cancelReason the new cancel reason
   */
  public void setCancelReason(String cancelReason) {
    this.cancelReason = cancelReason;
  }

  /**
   * Instantiates a new appointment.
   *
   * @param appointmentId the appointment id
   */
  public Appointment(Integer appointmentId) {
    this.appointmentId = appointmentId;
  }

  /**
   * Gets the appointment duration.
   *
   * @return the appointment duration
   */
  public Integer getAppointmentDuration() {
    return appointmentDuration;
  }

  /**
   * Sets the appointment duration.
   *
   * @param appointmentDuration the new appointment duration
   */
  public void setAppointmentDuration(Integer appointmentDuration) {
    this.appointmentDuration = appointmentDuration;
  }

  /**
   * Gets the appointment id.
   *
   * @return the appointment id
   */
  public Integer getAppointmentId() {
    return appointmentId;
  }

  /**
   * Sets the appointment id.
   *
   * @param appointmentId the new appointment id
   */
  public void setAppointmentId(Integer appointmentId) {
    this.appointmentId = appointmentId;
  }

  /**
   * Gets the appointment time.
   *
   * @return the appointment time
   */
  public java.sql.Timestamp getAppointmentTime() {
    return appointmentTime;
  }

  /**
   * Sets the appointment time.
   *
   * @param appointmentTime the new appointment time
   */
  public void setAppointmentTime(java.sql.Timestamp appointmentTime) {
    this.appointmentTime = appointmentTime;
  }

  /**
   * Gets the appoint status.
   *
   * @return the appoint status
   */
  public String getAppointStatus() {
    return appointStatus;
  }

  /**
   * Sets the appoint status.
   *
   * @param appointStatus the new appoint status
   */
  public void setAppointStatus(String appointStatus) {
    this.appointStatus = appointStatus;
  }

  /**
   * Gets the booked by.
   *
   * @return the booked by
   */
  public String getBookedBy() {
    return bookedBy;
  }

  /**
   * Sets the booked by.
   *
   * @param bookedBy the new booked by
   */
  public void setBookedBy(String bookedBy) {
    this.bookedBy = bookedBy;
  }

  /**
   * Gets the booked time.
   *
   * @return the booked time
   */
  public java.sql.Timestamp getBookedTime() {
    return bookedTime;
  }

  /**
   * Sets the booked time.
   *
   * @param bookedTime the new booked time
   */
  public void setBookedTime(java.sql.Timestamp bookedTime) {
    this.bookedTime = bookedTime;
  }

  /**
   * Gets the complaint.
   *
   * @return the complaint
   */
  public String getComplaint() {
    return complaint;
  }

  /**
   * Sets the complaint.
   *
   * @param complaint the new complaint
   */
  public void setComplaint(String complaint) {
    this.complaint = complaint;
  }

  /**
   * Gets the mr no.
   *
   * @return the mr no
   */
  public String getMrNo() {
    return mrNo;
  }

  /**
   * Sets the mr no.
   *
   * @param mrNo the new mr no
   */
  public void setMrNo(String mrNo) {
    this.mrNo = mrNo;
  }

  /**
   * Gets the phone no.
   *
   * @return the phone no
   */
  public String getPhoneNo() {
    return phoneNo;
  }

  /**
   * Sets the phone no.
   *
   * @param phoneNo the new phone no
   */
  public void setPhoneNo(String phoneNo) {
    this.phoneNo = phoneNo;
  }

  /**
   * Gets the patient name.
   *
   * @return the patient name
   */
  public String getPatientName() {
    return patientName;
  }

  /**
   * Sets the patient name.
   *
   * @param patientName the new patient name
   */
  public void setPatientName(String patientName) {
    this.patientName = patientName;
  }

  /**
   * Gets the schedule id.
   *
   * @return the schedule id
   */
  public Integer getScheduleId() {
    return scheduleId;
  }

  /**
   * Sets the schedule id.
   *
   * @param scheduleId the new schedule id
   */
  public void setScheduleId(Integer scheduleId) {
    this.scheduleId = scheduleId;
  }

  /**
   * Gets the schedule name.
   *
   * @return the schedule name
   */
  public String getScheduleName() {
    return scheduleName;
  }

  /**
   * Sets the schedule name.
   *
   * @param scheduleName the new schedule name
   */
  public void setScheduleName(String scheduleName) {
    this.scheduleName = scheduleName;
  }

  /**
   * Gets the visit id.
   *
   * @return the visit id
   */
  public String getVisitId() {
    return visitId;
  }

  /**
   * Sets the visit id.
   *
   * @param visitId the new visit id
   */
  public void setVisitId(String visitId) {
    this.visitId = visitId;
  }

  /**
   * Gets the completed time.
   *
   * @return the completed time
   */
  public java.sql.Timestamp getCompletedTime() {
    return completedTime;
  }

  /**
   * Sets the completed time.
   *
   * @param completedTime the new completed time
   */
  public void setCompletedTime(java.sql.Timestamp completedTime) {
    this.completedTime = completedTime;
  }

  /**
   * Gets the complaint name.
   *
   * @return the complaint name
   */
  public String getComplaintName() {
    return complaintName;
  }

  /**
   * Sets the complaint name.
   *
   * @param complaintName the new complaint name
   */
  public void setComplaintName(String complaintName) {
    this.complaintName = complaintName;
  }

  /**
   * Gets the remarks.
   *
   * @return the remarks
   */
  public String getRemarks() {
    return remarks;
  }

  /**
   * Sets the remarks.
   *
   * @param remarks the new remarks
   */
  public void setRemarks(String remarks) {
    this.remarks = remarks;
  }

  /**
   * Gets the changed by.
   *
   * @return the changed by
   */
  public String getChangedBy() {
    return changedBy;
  }

  /**
   * Gets the salutation name.
   *
   * @return the salutation name
   */
  public String getSalutationName() {
    return salutationName;
  }

  /**
   * Sets the salutation name.
   *
   * @param salutationName the new salutation name
   */
  public void setSalutationName(String salutationName) {
    this.salutationName = salutationName;
  }

  /**
   * Sets the changed by.
   *
   * @param changedBy the new changed by
   */
  public void setChangedBy(String changedBy) {
    this.changedBy = changedBy;
  }

  /**
   * Gets the resource type.
   *
   * @return the resource type
   */
  public String getResourceType() {
    return resourceType;
  }

  /**
   * Sets the resource type.
   *
   * @param resourceType the new resource type
   */
  public void setResourceType(String resourceType) {
    this.resourceType = resourceType;
  }

  /**
   * Gets the scheduler visit type.
   *
   * @return the scheduler visit type
   */
  public String getSchedulerVisitType() {
    return schedulerVisitType;
  }

  /**
   * Sets the scheduler visit type.
   *
   * @param schedulerVisitType the new scheduler visit type
   */
  public void setSchedulerVisitType(String schedulerVisitType) {
    this.schedulerVisitType = schedulerVisitType;
  }

  /**
   * Gets the center code.
   *
   * @return the center code
   */
  public String getCenterCode() {
    return centerCode;
  }

  /**
   * Sets the center code.
   *
   * @param centerCode the new center code
   */
  public void setCenterCode(String centerCode) {
    this.centerCode = centerCode;
  }

  /**
   * Gets the practo appointment id.
   *
   * @return the practo appointment id
   */
  public String getPractoAppointmentId() {
    return practoAppointmentId;
  }

  /**
   * Sets the practo appointment id.
   *
   * @param practoAppointmentId the new practo appointment id
   */
  public void setPractoAppointmentId(String practoAppointmentId) {
    this.practoAppointmentId = practoAppointmentId;
  }

  /**
   * Gets the app source id.
   *
   * @return the app source id
   */
  public Integer getApp_source_id() {
    return appSourceId;
  }

  /**
   * Sets the app source id.
   *
   * @param appSourceId the new app source id
   */
  public void setApp_source_id(Integer appSourceId) {
    this.appSourceId = appSourceId;
  }

  /**
   * Gets the primary sponsor id.
   *
   * @return the primary sponsor id
   */
  public String getPrimarySponsorId() {
    return primarySponsorId;
  }

  /**
   * Sets the primary sponsor id.
   *
   * @param primarySponsorId the new primary sponsor id
   */
  public void setPrimarySponsorId(String primarySponsorId) {
    this.primarySponsorId = primarySponsorId;
  }

  /**
   * Gets the primary sponsor co.
   *
   * @return the primary sponsor co
   */
  public String getPrimarySponsorCo() {
    return primarySponsorCo;
  }

  /**
   * Sets the primary sponsor co.
   *
   * @param primarySponsorCo the new primary sponsor co
   */
  public void setPrimarySponsorCo(String primarySponsorCo) {
    this.primarySponsorCo = primarySponsorCo;
  }

  /**
   * Gets the plan id.
   *
   * @return the plan id
   */
  public Integer getPlanId() {
    return planId;
  }

  /**
   * Sets the plan id.
   *
   * @param planId the new plan id
   */
  public void setPlanId(Integer planId) {
    this.planId = planId;
  }

  /**
   * Gets the plan type id.
   *
   * @return the plan type id
   */
  public Integer getPlanTypeId() {
    return planTypeId;
  }

  /**
   * Sets the plan type id.
   *
   * @param planTypeId the new plan type id
   */
  public void setPlanTypeId(Integer planTypeId) {
    this.planTypeId = planTypeId;
  }

  /**
   * Gets the member id.
   *
   * @return the member id
   */
  public String getMemberId() {
    return memberId;
  }

  /**
   * Sets the member id.
   *
   * @param memberId the new member id
   */
  public void setMemberId(String memberId) {
    this.memberId = memberId;
  }

  /**
   * Gets the contact id.
   *
   * @return the contact id
   */
  public Integer getContactId() {
    return contactId;
  }

  /**
   * Sets the contact id.
   *
   * @param contactId the new contact id
   */
  public void setContactId(Integer contactId) {
    this.contactId = contactId;
  }

  public Integer getWaitlist() {
    return waitlist;
  }

  public void setWaitlist(Integer waitlist) {
    this.waitlist = waitlist;
  }
  
  

}
