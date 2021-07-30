package com.insta.hms.ipservices;

import java.sql.Timestamp;

// TODO: Auto-generated Javadoc
/**
 * The Class OpPrescriptionDTO.
 */
public class OpPrescriptionDTO {

  /** The mrno. */
  private String mrno;
  
  /** The patid. */
  private String patid;
  
  /** The doctor. */
  private String doctor;
  
  /** The opdept id. */
  private String opdeptId;
  
  /** The operation id. */
  private String operationId;
  
  /** The theater id. */
  private String theaterId;
  
  /** The date. */
  private String date;
  
  /** The enddate. */
  private String enddate;
  
  /** The startdate. */
  private String startdate;
  
  /** The hstarttime. */
  private String hstarttime;
  
  /** The hendtime. */
  private String hendtime;
  
  /** The hoperationremarks. */
  private String hoperationremarks;
  
  /** The prescriptionid. */
  private int prescriptionid;
  
  /** The surgeon. */
  private String surgeon;
  
  /** The anaesthetist. */
  private String anaesthetist;
  
  /** The hrly. */
  private String hrly;
  
  /** The organisation. */
  private String organisation;
  
  /** The bed. */
  private String bed;
  
  /** The bill. */
  private String bill;
  
  /** The hdate. */
  private String hdate;
  
  /** The htime. */
  private String htime;
  
  /** The patientdept. */
  private String patientdept;
  
  /** The operationname. */
  private String operationname;
  
  /** The surgeonid. */
  private String surgeonid;
  
  /** The anaeid. */
  private String anaeid;
  
  /** The theatrename. */
  private String theatrename;
  
  /** The otchargetype. */
  private String otchargetype;
  
  /** The completed. */
  private String completed;
  
  /** The status. */
  private String status;
  
  /** The operation time. */
  private String operationTime;
  
  /** The expected end time. */
  private String expectedEndTime;
  
  /** The frompackage. */
  private boolean frompackage;
  
  /** The anesthesia type. */
  private String anesthesiaType;
  
  /** The prescribed time. */
  private Timestamp prescribedTime;

  /**
   * Gets the prescribed time.
   *
   * @return the prescribed time
   */
  public Timestamp getPrescribedTime() {
    return prescribedTime;
  }

  /**
   * Sets the prescribed time.
   *
   * @param prescribedTime the new prescribed time
   */
  public void setPrescribedTime(Timestamp prescribedTime) {
    this.prescribedTime = prescribedTime;
  }

  /**
   * Checks if is frompackage.
   *
   * @return true, if is frompackage
   */
  public boolean isFrompackage() {
    return frompackage;
  }

  /**
   * Sets the frompackage.
   *
   * @param frompackage the new frompackage
   */
  public void setFrompackage(boolean frompackage) {
    this.frompackage = frompackage;
  }

  /**
   * Gets the expected end time.
   *
   * @return the expected end time
   */
  public String getExpected_end_time() {
    return expectedEndTime;
  }

  /**
   * Sets the expected end time.
   *
   * @param expectedEndTime the new expected end time
   */
  public void setExpected_end_time(String expectedEndTime) {
    this.expectedEndTime = expectedEndTime;
  }

  /**
   * Gets the operation time.
   *
   * @return the operation time
   */
  public String getOperation_time() {
    return operationTime;
  }

  /**
   * Sets the operation time.
   *
   * @param operationTime the new operation time
   */
  public void setOperation_time(String operationTime) {
    this.operationTime = operationTime;
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
   * Gets the completed.
   *
   * @return the completed
   */
  public String getCompleted() {
    return completed;
  }

  /**
   * Sets the completed.
   *
   * @param completed the new completed
   */
  public void setCompleted(String completed) {
    this.completed = completed;
  }

  /**
   * Gets the otchargetype.
   *
   * @return the otchargetype
   */
  public String getOtchargetype() {
    return otchargetype;
  }

  /**
   * Sets the otchargetype.
   *
   * @param otchargetype the new otchargetype
   */
  public void setOtchargetype(String otchargetype) {
    this.otchargetype = otchargetype;
  }

  /**
   * Gets the anaeid.
   *
   * @return the anaeid
   */
  public String getAnaeid() {
    return anaeid;
  }

  /**
   * Sets the anaeid.
   *
   * @param anaeid the new anaeid
   */
  public void setAnaeid(String anaeid) {
    this.anaeid = anaeid;
  }

  /**
   * Gets the surgeonid.
   *
   * @return the surgeonid
   */
  public String getSurgeonid() {
    return surgeonid;
  }

  /**
   * Sets the surgeonid.
   *
   * @param surgeonid the new surgeonid
   */
  public void setSurgeonid(String surgeonid) {
    this.surgeonid = surgeonid;
  }

  /**
   * Gets the theatrename.
   *
   * @return the theatrename
   */
  public String getTheatrename() {
    return theatrename;
  }

  /**
   * Sets the theatrename.
   *
   * @param theatrename the new theatrename
   */
  public void setTheatrename(String theatrename) {
    this.theatrename = theatrename;
  }

  /**
   * Gets the operationname.
   *
   * @return the operationname
   */
  public String getOperationname() {
    return operationname;
  }

  /**
   * Sets the operationname.
   *
   * @param operationname the new operationname
   */
  public void setOperationname(String operationname) {
    this.operationname = operationname;
  }

  /**
   * Gets the patientdept.
   *
   * @return the patientdept
   */
  public String getPatientdept() {
    return patientdept;
  }

  /**
   * Sets the patientdept.
   *
   * @param patientdept the new patientdept
   */
  public void setPatientdept(String patientdept) {
    this.patientdept = patientdept;
  }

  /**
   * Gets the hdate.
   *
   * @return the hdate
   */
  public String getHdate() {
    return hdate;
  }

  /**
   * Sets the hdate.
   *
   * @param hdate the new hdate
   */
  public void setHdate(String hdate) {
    this.hdate = hdate;
  }

  /**
   * Gets the htime.
   *
   * @return the htime
   */
  public String getHtime() {
    return htime;
  }

  /**
   * Sets the htime.
   *
   * @param htime the new htime
   */
  public void setHtime(String htime) {
    this.htime = htime;
  }

  /**
   * Gets the bill.
   *
   * @return the bill
   */
  public String getBill() {
    return bill;
  }

  /**
   * Sets the bill.
   *
   * @param bill the new bill
   */
  public void setBill(String bill) {
    this.bill = bill;
  }

  /**
   * Gets the bed.
   *
   * @return the bed
   */
  public String getBed() {
    return bed;
  }

  /**
   * Sets the bed.
   *
   * @param bed the new bed
   */
  public void setBed(String bed) {
    this.bed = bed;
  }

  /**
   * Gets the hrly.
   *
   * @return the hrly
   */
  public String getHrly() {
    return hrly;
  }

  /**
   * Sets the hrly.
   *
   * @param hrly the new hrly
   */
  public void setHrly(String hrly) {
    this.hrly = hrly;
  }

  /**
   * Gets the prescriptionid.
   *
   * @return the prescriptionid
   */
  public int getPrescriptionid() {
    return prescriptionid;
  }

  /**
   * Sets the prescriptionid.
   *
   * @param prescriptionid the new prescriptionid
   */
  public void setPrescriptionid(int prescriptionid) {
    this.prescriptionid = prescriptionid;
  }

  /**
   * Gets the date.
   *
   * @return the date
   */
  public String getDate() {
    return date;
  }

  /**
   * Sets the date.
   *
   * @param date the new date
   */
  public void setDate(String date) {
    this.date = date;
  }

  /**
   * Gets the doctor.
   *
   * @return the doctor
   */
  public String getDoctor() {
    return doctor;
  }

  /**
   * Sets the doctor.
   *
   * @param doctor the new doctor
   */
  public void setDoctor(String doctor) {
    this.doctor = doctor;
  }

  /**
   * Gets the hendtime.
   *
   * @return the hendtime
   */
  public String getHendtime() {
    return hendtime;
  }

  /**
   * Sets the hendtime.
   *
   * @param hendtime the new hendtime
   */
  public void setHendtime(String hendtime) {
    this.hendtime = hendtime;
  }

  /**
   * Gets the hoperationremarks.
   *
   * @return the hoperationremarks
   */
  public String getHoperationremarks() {
    return hoperationremarks;
  }

  /**
   * Sets the hoperationremarks.
   *
   * @param hoperationremarks the new hoperationremarks
   */
  public void setHoperationremarks(String hoperationremarks) {
    this.hoperationremarks = hoperationremarks;
  }

  /**
   * Gets the hstarttime.
   *
   * @return the hstarttime
   */
  public String getHstarttime() {
    return hstarttime;
  }

  /**
   * Sets the hstarttime.
   *
   * @param hstarttime the new hstarttime
   */
  public void setHstarttime(String hstarttime) {
    this.hstarttime = hstarttime;
  }

  /**
   * Gets the mrno.
   *
   * @return the mrno
   */
  public String getMrno() {
    return mrno;
  }

  /**
   * Sets the mrno.
   *
   * @param mrno the new mrno
   */
  public void setMrno(String mrno) {
    this.mrno = mrno;
  }

  /**
   * Gets the opdept id.
   *
   * @return the opdept id
   */
  public String getOpdeptId() {
    return opdeptId;
  }

  /**
   * Sets the opdept id.
   *
   * @param opdeptId the new opdept id
   */
  public void setOpdeptId(String opdeptId) {
    this.opdeptId = opdeptId;
  }

  /**
   * Gets the operation id.
   *
   * @return the operation id
   */
  public String getOperationId() {
    return operationId;
  }

  /**
   * Sets the operation id.
   *
   * @param operationId the new operation id
   */
  public void setOperationId(String operationId) {
    this.operationId = operationId;
  }

  /**
   * Gets the patid.
   *
   * @return the patid
   */
  public String getPatid() {
    return patid;
  }

  /**
   * Sets the patid.
   *
   * @param patid the new patid
   */
  public void setPatid(String patid) {
    this.patid = patid;
  }

  /**
   * Gets the theater id.
   *
   * @return the theater id
   */
  public String getTheaterId() {
    return theaterId;
  }

  /**
   * Sets the theater id.
   *
   * @param theaterId the new theater id
   */
  public void setTheaterId(String theaterId) {
    this.theaterId = theaterId;
  }

  /**
   * Gets the enddate.
   *
   * @return the enddate
   */
  public String getEnddate() {
    return enddate;
  }

  /**
   * Sets the enddate.
   *
   * @param enddate the new enddate
   */
  public void setEnddate(String enddate) {
    this.enddate = enddate;
  }

  /**
   * Gets the startdate.
   *
   * @return the startdate
   */
  public String getStartdate() {
    return startdate;
  }

  /**
   * Sets the startdate.
   *
   * @param startdate the new startdate
   */
  public void setStartdate(String startdate) {
    this.startdate = startdate;
  }

  /**
   * Gets the anaesthetist.
   *
   * @return the anaesthetist
   */
  public String getAnaesthetist() {
    return anaesthetist;
  }

  /**
   * Sets the anaesthetist.
   *
   * @param anaesthetist the new anaesthetist
   */
  public void setAnaesthetist(String anaesthetist) {
    this.anaesthetist = anaesthetist;
  }

  /**
   * Gets the surgeon.
   *
   * @return the surgeon
   */
  public String getSurgeon() {
    return surgeon;
  }

  /**
   * Sets the surgeon.
   *
   * @param surgeon the new surgeon
   */
  public void setSurgeon(String surgeon) {
    this.surgeon = surgeon;
  }

  /**
   * Gets the organisation.
   *
   * @return the organisation
   */
  public String getOrganisation() {
    return organisation;
  }

  /**
   * Sets the organisation.
   *
   * @param organisation the new organisation
   */
  public void setOrganisation(String organisation) {
    this.organisation = organisation;
  }

  /**
   * Gets the anesthesia type.
   *
   * @return the anesthesia type
   */
  public String getAnesthesia_type() {
    return anesthesiaType;
  }

  /**
   * Sets the anesthesia type.
   *
   * @param anesthesiaType the new anesthesia type
   */
  public void setAnesthesia_type(String anesthesiaType) {
    this.anesthesiaType = anesthesiaType;
  }

}
