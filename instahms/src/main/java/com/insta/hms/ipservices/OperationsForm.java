package com.insta.hms.ipservices;

import org.apache.struts.action.ActionForm;

// TODO: Auto-generated Javadoc
/**
 * The Class OperationsForm.
 */
public class OperationsForm extends ActionForm {
  
  /** The mrno. */
  private String mrno;
  
  /** The patientid. */
  private String patientid;
  
  /** The doctor. */
  private String doctor;
  
  /** The orgid. */
  private String orgid;
  
  /** The hoperationid. */
  private String hoperationid;
  
  /** The opdept id. */
  private String[] opdeptId;
  
  /** The operation id. */
  private String[] operationId;
  
  /** The theater id. */
  private String[] theaterId;
  
  /** The hstarttime. */
  private String[] hstarttime;
  
  /** The hendtime. */
  private String[] hendtime;
  
  /** The date. */
  private String[] date;
  
  /** The enddate. */
  private String[] enddate;
  
  /** The startdate. */
  private String[] startdate;
  
  /** The hoperationremarks. */
  private String[] hoperationremarks;
  
  /** The equipmentname. */
  private String[] equipmentname;
  
  /** The equipment id. */
  private String[] equipmentId;
  
  /** The equipmentduration. */
  private String[] equipmentduration;
  
  /** The equipmentunit. */
  private String[] equipmentunit;
  
  /** The equipmentdepartment. */
  private String[] equipmentdepartment;
  
  /** The unitcharge. */
  private String[] unitcharge;
  
  /** The equipmentremark. */
  private String[] equipmentremark;
  
  /** The medicine. */
  private String[] medicine;
  
  /** The medicinedosage. */
  private String[] medicinedosage;
  
  /** The mednoofdays. */
  private String[] mednoofdays;
  
  /** The medquantity. */
  private String[] medquantity;
  
  /** The medremarks. */
  private String[] medremarks;
  
  /** The otherservice. */
  private String[] otherservice;
  
  /** The otherservice group. */
  private String[] otherserviceGroup;
  
  /** The otherserviceqty. */
  private String[] otherserviceqty;
  
  /** The otherservicecharge. */
  private String[] otherservicecharge;
  
  /** The otherserviceremarks. */
  private String[] otherserviceremarks;
  
  /** The operation. */
  private String[] operation;
  
  /** The hdate. */
  private String hdate;
  
  /** The htime. */
  private String htime;
  
  /** The patient dept. */
  private String patientDept;
  
  /** The operationname. */
  private String[] operationname;
  
  /** The theatername. */
  private String[] theatername;
  
  /** The otcharge. */
  private String[] otcharge;

  /** The operations 1. */
  private String[] operations1;
  
  /** The theaters 1. */
  private String[] theaters1;
  
  /** The otdoctor 1. */
  private String[] otdoctor1;
  
  /** The otdoctor. */
  private String[] otdoctor;
  
  /** The surgeon. */
  private String[] surgeon;
  
  /** The anesthetist. */
  private String[] anesthetist;
  
  /** The anesthetistid. */
  private String[] anesthetistid;
  
  /** The surgeonid. */
  private String[] surgeonid;

  /** The theatre. */
  private String theatre;
  
  /** The primarysurgeon. */
  private String primarysurgeon;
  
  /** The primaryanae. */
  private String primaryanae;

  /** The serviceid. */
  private String[] serviceid;
  
  /** The servicename. */
  private String[] servicename;
  
  /** The no of times. */
  private String[] noOfTimes;
  
  /** The serviceremark. */
  private String[] serviceremark;
  
  /** The servicedept. */
  private String[] servicedept;

  /** The otdoctorname. */
  private String[] otdoctorname;
  
  /** The opdoctortypename. */
  private String[] opdoctortypename;
  
  /** The bill. */
  private String bill;
  
  /** The bed. */
  private String bed;
  
  /** The otdoctortype. */
  private String[] otdoctortype;

  /**
   * Gets the otdoctortype.
   *
   * @return the otdoctortype
   */
  public String[] getOtdoctortype() {
    return otdoctortype;
  }

  /**
   * Sets the otdoctortype.
   *
   * @param otdoctortype the new otdoctortype
   */
  public void setOtdoctortype(String[] otdoctortype) {
    this.otdoctortype = otdoctortype;
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
   * Gets the opdoctortypename.
   *
   * @return the opdoctortypename
   */
  public String[] getOpdoctortypename() {
    return opdoctortypename;
  }

  /**
   * Sets the opdoctortypename.
   *
   * @param opdoctortypename the new opdoctortypename
   */
  public void setOpdoctortypename(String[] opdoctortypename) {
    this.opdoctortypename = opdoctortypename;
  }

  /**
   * Gets the otdoctorname.
   *
   * @return the otdoctorname
   */
  public String[] getOtdoctorname() {
    return otdoctorname;
  }

  /**
   * Sets the otdoctorname.
   *
   * @param otdoctorname the new otdoctorname
   */
  public void setOtdoctorname(String[] otdoctorname) {
    this.otdoctorname = otdoctorname;
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
   * Gets the hendtime.
   *
   * @return the hendtime
   */
  public String[] getHendtime() {
    return hendtime;
  }

  /**
   * Sets the hendtime.
   *
   * @param hendtime the new hendtime
   */
  public void setHendtime(String[] hendtime) {
    this.hendtime = hendtime;
  }

  /**
   * Gets the hoperationremarks.
   *
   * @return the hoperationremarks
   */
  public String[] getHoperationremarks() {
    return hoperationremarks;
  }

  /**
   * Sets the hoperationremarks.
   *
   * @param hoperationremarks the new hoperationremarks
   */
  public void setHoperationremarks(String[] hoperationremarks) {
    this.hoperationremarks = hoperationremarks;
  }

  /**
   * Gets the hstarttime.
   *
   * @return the hstarttime
   */
  public String[] getHstarttime() {
    return hstarttime;
  }

  /**
   * Sets the hstarttime.
   *
   * @param hstarttime the new hstarttime
   */
  public void setHstarttime(String[] hstarttime) {
    this.hstarttime = hstarttime;
  }

  /**
   * Gets the opdept id.
   *
   * @return the opdept id
   */
  public String[] getOpdeptId() {
    return opdeptId;
  }

  /**
   * Sets the opdept id.
   *
   * @param opdeptId the new opdept id
   */
  public void setOpdeptId(String[] opdeptId) {
    this.opdeptId = opdeptId;
  }

  /**
   * Gets the operation id.
   *
   * @return the operation id
   */
  public String[] getOperationId() {
    return operationId;
  }

  /**
   * Sets the operation id.
   *
   * @param operationId the new operation id
   */
  public void setOperationId(String[] operationId) {
    this.operationId = operationId;
  }

  /**
   * Gets the theater id.
   *
   * @return the theater id
   */
  public String[] getTheaterId() {
    return theaterId;
  }

  /**
   * Sets the theater id.
   *
   * @param theaterId the new theater id
   */
  public void setTheaterId(String[] theaterId) {
    this.theaterId = theaterId;
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
   * Gets the patientid.
   *
   * @return the patientid
   */
  public String getPatientid() {
    return patientid;
  }

  /**
   * Sets the patientid.
   *
   * @param patientid the new patientid
   */
  public void setPatientid(String patientid) {
    this.patientid = patientid;
  }

  /**
   * Gets the date.
   *
   * @return the date
   */
  public String[] getDate() {
    return date;
  }

  /**
   * Sets the date.
   *
   * @param date the new date
   */
  public void setDate(String[] date) {
    this.date = date;
  }

  /**
   * Gets the equipmentdepartment.
   *
   * @return the equipmentdepartment
   */
  public String[] getEquipmentdepartment() {
    return equipmentdepartment;
  }

  /**
   * Sets the equipmentdepartment.
   *
   * @param equipmentdepartment the new equipmentdepartment
   */
  public void setEquipmentdepartment(String[] equipmentdepartment) {
    this.equipmentdepartment = equipmentdepartment;
  }

  /**
   * Gets the equipmentduration.
   *
   * @return the equipmentduration
   */
  public String[] getEquipmentduration() {
    return equipmentduration;
  }

  /**
   * Sets the equipmentduration.
   *
   * @param equipmentduration the new equipmentduration
   */
  public void setEquipmentduration(String[] equipmentduration) {
    this.equipmentduration = equipmentduration;
  }

  /**
   * Gets the equipment id.
   *
   * @return the equipment id
   */
  public String[] getEquipmentId() {
    return equipmentId;
  }

  /**
   * Sets the equipment id.
   *
   * @param equipmentId the new equipment id
   */
  public void setEquipmentId(String[] equipmentId) {
    this.equipmentId = equipmentId;
  }

  /**
   * Gets the equipmentname.
   *
   * @return the equipmentname
   */
  public String[] getEquipmentname() {
    return equipmentname;
  }

  /**
   * Sets the equipmentname.
   *
   * @param equipmentname the new equipmentname
   */
  public void setEquipmentname(String[] equipmentname) {
    this.equipmentname = equipmentname;
  }

  /**
   * Gets the equipmentremark.
   *
   * @return the equipmentremark
   */
  public String[] getEquipmentremark() {
    return equipmentremark;
  }

  /**
   * Sets the equipmentremark.
   *
   * @param equipmentremark the new equipmentremark
   */
  public void setEquipmentremark(String[] equipmentremark) {
    this.equipmentremark = equipmentremark;
  }

  /**
   * Gets the equipmentunit.
   *
   * @return the equipmentunit
   */
  public String[] getEquipmentunit() {
    return equipmentunit;
  }

  /**
   * Sets the equipmentunit.
   *
   * @param equipmentunit the new equipmentunit
   */
  public void setEquipmentunit(String[] equipmentunit) {
    this.equipmentunit = equipmentunit;
  }

  /**
   * Gets the medicine.
   *
   * @return the medicine
   */
  public String[] getMedicine() {
    return medicine;
  }

  /**
   * Sets the medicine.
   *
   * @param medicine the new medicine
   */
  public void setMedicine(String[] medicine) {
    this.medicine = medicine;
  }

  /**
   * Gets the medicinedosage.
   *
   * @return the medicinedosage
   */
  public String[] getMedicinedosage() {
    return medicinedosage;
  }

  /**
   * Sets the medicinedosage.
   *
   * @param medicinedosage the new medicinedosage
   */
  public void setMedicinedosage(String[] medicinedosage) {
    this.medicinedosage = medicinedosage;
  }

  /**
   * Gets the mednoofdays.
   *
   * @return the mednoofdays
   */
  public String[] getMednoofdays() {
    return mednoofdays;
  }

  /**
   * Sets the mednoofdays.
   *
   * @param mednoofdays the new mednoofdays
   */
  public void setMednoofdays(String[] mednoofdays) {
    this.mednoofdays = mednoofdays;
  }

  /**
   * Gets the medquantity.
   *
   * @return the medquantity
   */
  public String[] getMedquantity() {
    return medquantity;
  }

  /**
   * Sets the medquantity.
   *
   * @param medquantity the new medquantity
   */
  public void setMedquantity(String[] medquantity) {
    this.medquantity = medquantity;
  }

  /**
   * Gets the medremarks.
   *
   * @return the medremarks
   */
  public String[] getMedremarks() {
    return medremarks;
  }

  /**
   * Sets the medremarks.
   *
   * @param medremarks the new medremarks
   */
  public void setMedremarks(String[] medremarks) {
    this.medremarks = medremarks;
  }

  /**
   * Gets the operation.
   *
   * @return the operation
   */
  public String[] getOperation() {
    return operation;
  }

  /**
   * Sets the operation.
   *
   * @param operation the new operation
   */
  public void setOperation(String[] operation) {
    this.operation = operation;
  }

  /**
   * Gets the otherservice.
   *
   * @return the otherservice
   */
  public String[] getOtherservice() {
    return otherservice;
  }

  /**
   * Sets the otherservice.
   *
   * @param otherservice the new otherservice
   */
  public void setOtherservice(String[] otherservice) {
    this.otherservice = otherservice;
  }

  /**
   * Gets the otherservicecharge.
   *
   * @return the otherservicecharge
   */
  public String[] getOtherservicecharge() {
    return otherservicecharge;
  }

  /**
   * Sets the otherservicecharge.
   *
   * @param otherservicecharge the new otherservicecharge
   */
  public void setOtherservicecharge(String[] otherservicecharge) {
    this.otherservicecharge = otherservicecharge;
  }

  /**
   * Gets the otherservice group.
   *
   * @return the otherservice group
   */
  public String[] getOtherserviceGroup() {
    return otherserviceGroup;
  }

  /**
   * Sets the otherservice group.
   *
   * @param otherserviceGroup the new otherservice group
   */
  public void setOtherserviceGroup(String[] otherserviceGroup) {
    this.otherserviceGroup = otherserviceGroup;
  }

  /**
   * Gets the otherserviceqty.
   *
   * @return the otherserviceqty
   */
  public String[] getOtherserviceqty() {
    return otherserviceqty;
  }

  /**
   * Sets the otherserviceqty.
   *
   * @param otherserviceqty the new otherserviceqty
   */
  public void setOtherserviceqty(String[] otherserviceqty) {
    this.otherserviceqty = otherserviceqty;
  }

  /**
   * Gets the otherserviceremarks.
   *
   * @return the otherserviceremarks
   */
  public String[] getOtherserviceremarks() {
    return otherserviceremarks;
  }

  /**
   * Sets the otherserviceremarks.
   *
   * @param otherserviceremarks the new otherserviceremarks
   */
  public void setOtherserviceremarks(String[] otherserviceremarks) {
    this.otherserviceremarks = otherserviceremarks;
  }

  /**
   * Gets the unitcharge.
   *
   * @return the unitcharge
   */
  public String[] getUnitcharge() {
    return unitcharge;
  }

  /**
   * Sets the unitcharge.
   *
   * @param unitcharge the new unitcharge
   */
  public void setUnitcharge(String[] unitcharge) {
    this.unitcharge = unitcharge;
  }

  /**
   * Gets the orgid.
   *
   * @return the orgid
   */
  public String getOrgid() {
    return orgid;
  }

  /**
   * Sets the orgid.
   *
   * @param orgid the new orgid
   */
  public void setOrgid(String orgid) {
    this.orgid = orgid;
  }

  /**
   * Gets the hoperationid.
   *
   * @return the hoperationid
   */
  public String getHoperationid() {
    return hoperationid;
  }

  /**
   * Sets the hoperationid.
   *
   * @param hoperationid the new hoperationid
   */
  public void setHoperationid(String hoperationid) {
    this.hoperationid = hoperationid;
  }

  /**
   * Gets the patient dept.
   *
   * @return the patient dept
   */
  public String getPatientDept() {
    return patientDept;
  }

  /**
   * Sets the patient dept.
   *
   * @param patientDept the new patient dept
   */
  public void setPatientDept(String patientDept) {
    this.patientDept = patientDept;
  }

  /**
   * Gets the operations 1.
   *
   * @return the operations 1
   */
  public String[] getOperations1() {
    return operations1;
  }

  /**
   * Sets the operations 1.
   *
   * @param operations1 the new operations 1
   */
  public void setOperations1(String[] operations1) {
    this.operations1 = operations1;
  }

  /**
   * Gets the theaters 1.
   *
   * @return the theaters 1
   */
  public String[] getTheaters1() {
    return theaters1;
  }

  /**
   * Sets the theaters 1.
   *
   * @param theaters1 the new theaters 1
   */
  public void setTheaters1(String[] theaters1) {
    this.theaters1 = theaters1;
  }

  /**
   * Gets the otdoctor 1.
   *
   * @return the otdoctor 1
   */
  public String[] getOtdoctor1() {
    return otdoctor1;
  }

  /**
   * Sets the otdoctor 1.
   *
   * @param otdoctor1 the new otdoctor 1
   */
  public void setOtdoctor1(String[] otdoctor1) {
    this.otdoctor1 = otdoctor1;
  }

  /**
   * Gets the enddate.
   *
   * @return the enddate
   */
  public String[] getEnddate() {
    return enddate;
  }

  /**
   * Sets the enddate.
   *
   * @param enddate the new enddate
   */
  public void setEnddate(String[] enddate) {
    this.enddate = enddate;
  }

  /**
   * Gets the startdate.
   *
   * @return the startdate
   */
  public String[] getStartdate() {
    return startdate;
  }

  /**
   * Sets the startdate.
   *
   * @param startdate the new startdate
   */
  public void setStartdate(String[] startdate) {
    this.startdate = startdate;
  }

  /**
   * Gets the operationname.
   *
   * @return the operationname
   */
  public String[] getOperationname() {
    return operationname;
  }

  /**
   * Sets the operationname.
   *
   * @param operationname the new operationname
   */
  public void setOperationname(String[] operationname) {
    this.operationname = operationname;
  }

  /**
   * Gets the theatername.
   *
   * @return the theatername
   */
  public String[] getTheatername() {
    return theatername;
  }

  /**
   * Sets the theatername.
   *
   * @param theatername the new theatername
   */
  public void setTheatername(String[] theatername) {
    this.theatername = theatername;
  }

  /**
   * Gets the otdoctor.
   *
   * @return the otdoctor
   */
  public String[] getOtdoctor() {
    return otdoctor;
  }

  /**
   * Sets the otdoctor.
   *
   * @param otdoctor the new otdoctor
   */
  public void setOtdoctor(String[] otdoctor) {
    this.otdoctor = otdoctor;
  }

  /**
   * Gets the otcharge.
   *
   * @return the otcharge
   */
  public String[] getOtcharge() {
    return otcharge;
  }

  /**
   * Sets the otcharge.
   *
   * @param otcharge the new otcharge
   */
  public void setOtcharge(String[] otcharge) {
    this.otcharge = otcharge;
  }

  /**
   * Gets the anesthetist.
   *
   * @return the anesthetist
   */
  public String[] getAnesthetist() {
    return anesthetist;
  }

  /**
   * Sets the anesthetist.
   *
   * @param anesthetist the new anesthetist
   */
  public void setAnesthetist(String[] anesthetist) {
    this.anesthetist = anesthetist;
  }

  /**
   * Gets the surgeon.
   *
   * @return the surgeon
   */
  public String[] getSurgeon() {
    return surgeon;
  }

  /**
   * Sets the surgeon.
   *
   * @param surgeon the new surgeon
   */
  public void setSurgeon(String[] surgeon) {
    this.surgeon = surgeon;
  }

  /**
   * Gets the anesthetistid.
   *
   * @return the anesthetistid
   */
  public String[] getAnesthetistid() {
    return anesthetistid;
  }

  /**
   * Sets the anesthetistid.
   *
   * @param anesthetistid the new anesthetistid
   */
  public void setAnesthetistid(String[] anesthetistid) {
    this.anesthetistid = anesthetistid;
  }

  /**
   * Gets the surgeonid.
   *
   * @return the surgeonid
   */
  public String[] getSurgeonid() {
    return surgeonid;
  }

  /**
   * Sets the surgeonid.
   *
   * @param surgeonid the new surgeonid
   */
  public void setSurgeonid(String[] surgeonid) {
    this.surgeonid = surgeonid;
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
   * Gets the primaryanae.
   *
   * @return the primaryanae
   */
  public String getPrimaryanae() {
    return primaryanae;
  }

  /**
   * Sets the primaryanae.
   *
   * @param primaryanae the new primaryanae
   */
  public void setPrimaryanae(String primaryanae) {
    this.primaryanae = primaryanae;
  }

  /**
   * Gets the primarysurgeon.
   *
   * @return the primarysurgeon
   */
  public String getPrimarysurgeon() {
    return primarysurgeon;
  }

  /**
   * Sets the primarysurgeon.
   *
   * @param primarysurgeon the new primarysurgeon
   */
  public void setPrimarysurgeon(String primarysurgeon) {
    this.primarysurgeon = primarysurgeon;
  }

  /**
   * Gets the theatre.
   *
   * @return the theatre
   */
  public String getTheatre() {
    return theatre;
  }

  /**
   * Sets the theatre.
   *
   * @param theatre the new theatre
   */
  public void setTheatre(String theatre) {
    this.theatre = theatre;
  }

  /**
   * Gets the no of times.
   *
   * @return the no of times
   */
  public String[] getNoOfTimes() {
    return noOfTimes;
  }

  /**
   * Sets the no of times.
   *
   * @param noOfTimes the new no of times
   */
  public void setNoOfTimes(String[] noOfTimes) {
    this.noOfTimes = noOfTimes;
  }

  /**
   * Gets the servicedept.
   *
   * @return the servicedept
   */
  public String[] getServicedept() {
    return servicedept;
  }

  /**
   * Sets the servicedept.
   *
   * @param servicedept the new servicedept
   */
  public void setServicedept(String[] servicedept) {
    this.servicedept = servicedept;
  }

  /**
   * Gets the serviceid.
   *
   * @return the serviceid
   */
  public String[] getServiceid() {
    return serviceid;
  }

  /**
   * Sets the serviceid.
   *
   * @param serviceid the new serviceid
   */
  public void setServiceid(String[] serviceid) {
    this.serviceid = serviceid;
  }

  /**
   * Gets the servicename.
   *
   * @return the servicename
   */
  public String[] getServicename() {
    return servicename;
  }

  /**
   * Sets the servicename.
   *
   * @param servicename the new servicename
   */
  public void setServicename(String[] servicename) {
    this.servicename = servicename;
  }

  /**
   * Gets the serviceremark.
   *
   * @return the serviceremark
   */
  public String[] getServiceremark() {
    return serviceremark;
  }

  /**
   * Sets the serviceremark.
   *
   * @param serviceremark the new serviceremark
   */
  public void setServiceremark(String[] serviceremark) {
    this.serviceremark = serviceremark;
  }

}
