package com.insta.mhms.patient.scheduler;

/**
 * @author mithun.saha
 */
public class Appointments {
  public String mrNo;
  public String visitId;
  public String patientName;
  public String phoneNo;
  public String remarks;
  public String complaint;
  public String complaintName;
  public int appointmentId;
  public int scheduleId;
  public String scheduleName;
  public java.sql.Timestamp appointmentTime;
  public int appointmentDuration;
  public String appointStatus;
  public String bookedBy;
  public String changedBy;
  public java.sql.Timestamp bookedTime;
  public java.sql.Timestamp completedTime;
  public String cancelReason;
  public int consultationTypeId;
  public String resourceType;
  public String schedulerVisitType;
  public boolean bookedAsSecondaryResource;
  public String schPriorAuthId;
  public int schPriorAuthModeId;
  public int centerId;
  public String centerCode;
  public String centerName;
  public String departmentName;
  
  public int getAppointmentDuration() {
    return appointmentDuration;
  }

  public void setAppointmentDuration(int appointmentDuration) {
    this.appointmentDuration = appointmentDuration;
  }

  public int getAppointmentId() {
    return appointmentId;
  }

  public void setAppointmentId(int appointmentId) {
    this.appointmentId = appointmentId;
  }

  public java.sql.Timestamp getAppointmentTime() {
    return appointmentTime;
  }

  public void setAppointmentTime(java.sql.Timestamp appointmentTime) {
    this.appointmentTime = appointmentTime;
  }

  public String getAppointStatus() {
    return appointStatus;
  }

  public void setAppointStatus(String appointStatus) {
    this.appointStatus = appointStatus;
  }

  public boolean isBookedAsSecondaryResource() {
    return bookedAsSecondaryResource;
  }

  public void setBookedAsSecondaryResource(boolean bookedAsSecondaryResource) {
    this.bookedAsSecondaryResource = bookedAsSecondaryResource;
  }

  public String getBookedBy() {
    return bookedBy;
  }

  public void setBookedBy(String bookedBy) {
    this.bookedBy = bookedBy;
  }

  public java.sql.Timestamp getBookedTime() {
    return bookedTime;
  }

  public void setBookedTime(java.sql.Timestamp bookedTime) {
    this.bookedTime = bookedTime;
  }

  public String getCancelReason() {
    return cancelReason;
  }

  public void setCancelReason(String cancelReason) {
    this.cancelReason = cancelReason;
  }

  public String getCenterCode() {
    return centerCode;
  }

  public void setCenterCode(String centerCode) {
    this.centerCode = centerCode;
  }

  public int getCenterId() {
    return centerId;
  }

  public void setCenterId(int centerId) {
    this.centerId = centerId;
  }

  public String getCenterName() {
    return centerName;
  }

  public void setCenterName(String centerName) {
    this.centerName = centerName;
  }

  public String getChangedBy() {
    return changedBy;
  }

  public void setChangedBy(String changedBy) {
    this.changedBy = changedBy;
  }

  public String getComplaint() {
    return complaint;
  }

  public void setComplaint(String complaint) {
    this.complaint = complaint;
  }

  public String getComplaintName() {
    return complaintName;
  }

  public void setComplaintName(String complaintName) {
    this.complaintName = complaintName;
  }

  public java.sql.Timestamp getCompletedTime() {
    return completedTime;
  }

  public void setCompletedTime(java.sql.Timestamp completedTime) {
    this.completedTime = completedTime;
  }

  public int getConsultationTypeId() {
    return consultationTypeId;
  }

  public void setConsultationTypeId(int consultationTypeId) {
    this.consultationTypeId = consultationTypeId;
  }

  public String getDepartmentName() {
    return departmentName;
  }

  public void setDepartmentName(String departmentName) {
    this.departmentName = departmentName;
  }

  public String getMrNo() {
    return mrNo;
  }

  public void setMrNo(String mrNo) {
    this.mrNo = mrNo;
  }

  public String getPatientName() {
    return patientName;
  }

  public void setPatientName(String patientName) {
    this.patientName = patientName;
  }

  public String getPhoneNo() {
    return phoneNo;
  }

  public void setPhoneNo(String phoneNo) {
    this.phoneNo = phoneNo;
  }

  public String getRemarks() {
    return remarks;
  }

  public void setRemarks(String remarks) {
    this.remarks = remarks;
  }

  public String getResourceType() {
    return resourceType;
  }

  public void setResourceType(String resourceType) {
    this.resourceType = resourceType;
  }

  public int getScheduleId() {
    return scheduleId;
  }

  public void setScheduleId(int scheduleId) {
    this.scheduleId = scheduleId;
  }

  public String getScheduleName() {
    return scheduleName;
  }

  public void setScheduleName(String scheduleName) {
    this.scheduleName = scheduleName;
  }

  public String getSchedulerVisitType() {
    return schedulerVisitType;
  }

  public void setSchedulerVisitType(String schedulerVisitType) {
    this.schedulerVisitType = schedulerVisitType;
  }

  public String getSchPriorAuthId() {
    return schPriorAuthId;
  }

  public void setSchPriorAuthId(String schPriorAuthId) {
    this.schPriorAuthId = schPriorAuthId;
  }

  public int getSchPriorAuthModeId() {
    return schPriorAuthModeId;
  }

  public void setSchPriorAuthModeId(int schPriorAuthModeId) {
    this.schPriorAuthModeId = schPriorAuthModeId;
  }

  public String getVisitId() {
    return visitId;
  }

  public void setVisitId(String visitId) {
    this.visitId = visitId;
  }
}
