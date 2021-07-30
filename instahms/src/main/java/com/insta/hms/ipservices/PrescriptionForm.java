package com.insta.hms.ipservices;

import org.apache.struts.action.ActionForm;

// TODO: Auto-generated Javadoc
/**
 * The Class PrescriptionForm.
 */
public class PrescriptionForm extends ActionForm {

  /** The htestconsult id. */
  private String[] htestconsultId;
  
  /** The hservice consult id. */
  private String[] hserviceConsultId;
  
  /** The htest dept. */
  private String[] htestDept;
  
  /** The htest id. */
  private String[] htestId;
  
  /** The htest name. */
  private String[] htestName;
  
  /** The htestpres id. */
  private String[] htestpresId;
  
  /** The hservicepres id. */
  private String[] hservicepresId;
  
  /** The catogery. */
  private String[] catogery;
  
  /** The servicedept. */
  private String[] servicedept;
  
  /** The serviceid. */
  private String[] serviceid;
  
  /** The servicename. */
  private String[] servicename;
  
  /** The no of times. */
  private String[] noOfTimes;
  
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
  
  /** The test pack. */
  private String[] test_pack;

  /** The usedate. */
  // added these for bug # 8113 (saving equipment usage time)
  private String[] usedate;
  
  /** The usetime. */
  private String[] usetime;
  
  /** The tilldate. */
  private String[] tilldate;
  
  /** The tilltime. */
  private String[] tilltime;

  /** The unitcharge. */
  private String[] unitcharge;
  
  /** The doctorname. */
  private String[] doctorname;
  
  /** The doctor id. */
  private String[] doctorId;
  
  /** The doctor consult charge. */
  private String[] doctorConsultCharge; // } added for getting the consultation charge for diff.
                                        
                                        /** The doctor pay percent. */
                                        // consultation types(Bug # 7832)
  private String[] doctorPayPercent; // }
  
  /** The doctor payment. */
  private String[] doctorPayment; // }
  
  /** The doctor consult discount. */
  private String[] doctorConsultDiscount;
  
  /** The medicine. */
  private String[] medicine;
  
  /** The medicinedosage. */
  private String[] medicinedosage;
  
  /** The mednoofdays. */
  private String[] mednoofdays;
  
  /** The medquantity. */
  private String[] medquantity;
  
  /** The patientid. */
  private String patientid;
  
  /** The orgid. */
  private String orgid;
  
  /** The doctor. */
  private String doctor;
  
  /** The mrno. */
  private String mrno;
  
  /** The otherservice. */
  private String[] otherservice;
  
  /** The otherservice group. */
  private String[] otherserviceGroup;
  
  /** The otherserviceqty. */
  private String[] otherserviceqty;
  
  /** The date. */
  private String date;
  
  /** The time. */
  private String time;
  
  /** The otherservicecharge. */
  private String[] otherservicecharge;
  
  /** The otherserviceremarks. */
  private String[] otherserviceremarks;
  
  /** The testremark. */
  private String[] testremark;
  
  /** The medremarks. */
  private String[] medremarks;
  
  /** The serviceremark. */
  private String[] serviceremark;
  
  /** The equipmentremark. */
  private String[] equipmentremark;
  
  /** The visitingdate. */
  private String[] visitingdate;
  
  /** The visitingtime. */
  private String[] visitingtime;
  
  /** The visitremarks. */
  private String[] visitremarks;
  
  /** The prescribeddoctor. */
  private String prescribeddoctor;
  
  /** The presdate. */
  private String presdate;
  
  /** The prestime. */
  private String prestime;
  
  /** The bill. */
  private String bill;
  
  /** The presdoctor. */
  private String presdoctor;
  
  /** The operationid. */
  private String[] operationid;
  
  /** The operationdeptid. */
  private String[] operationdeptid;
  
  /** The operationremarks. */
  private String[] operationremarks;
  
  /** The operationname. */
  private String[] operationname;

  /** The bedname. */
  private String[] bedname;
  
  /** The bedqty. */
  private String[] bedqty;
  
  /** The bedrate. */
  private String[] bedrate;
  
  /** The bedamt. */
  private String[] bedamt;

  /** The otherserrate. */
  private String[] otherserrate;
  
  /** The otherseramt. */
  private String[] otherseramt;

  /** The testqty. */
  private String[] testqty;
  
  /** The testrate. */
  private String[] testrate;
  
  /** The testamt. */
  private String[] testamt;

  /** The opeqty. */
  private String[] opeqty;
  
  /** The operate. */
  private String[] operate;
  
  /** The opeamt. */
  private String[] opeamt;

  /** The servrate. */
  private String[] servrate;
  
  /** The servamt. */
  private String[] servamt;

  /** The equiprate. */
  private String[] equiprate;
  
  /** The equipamt. */
  private String[] equipamt;

  /** The chargetype. */
  private String[] chargetype;
  
  /** The docqty. */
  private String[] docqty;
  
  /** The docrate. */
  private String[] docrate;
  
  /** The docamt. */
  private String[] docamt;

  /** The medrate. */
  private String[] medrate;
  
  /** The medamt. */
  private String[] medamt;

  /** The total amt. */
  private String totalAmt;
  
  /** The bedtype. */
  private String bedtype;
  
  /** The organizations. */
  private String organizations;
  
  /** The mealname. */
  // for dietry
  private String[] mealname;
  
  /** The mealid. */
  private String[] mealid;
  
  /** The mealremark. */
  private String[] mealremark;
  
  /** The hmealpres id. */
  private String[] hmealpresId;
  
  /** The meal qty. */
  private String[] mealQty;
  
  /** The mealdate. */
  // private String[] mealtime;
  private String[] mealdate;
  
  /** The mealdelete. */
  private String[] mealdelete;
  
  /** The mealtiming. */
  private String[] mealtiming;
  
  /** The spl meal time. */
  private String[] splMealTime;

  /** The patient dept. */
  private String patientDept;

  /** The specialization. */
  private String[] specialization;

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
   * Gets the prescribeddoctor.
   *
   * @return the prescribeddoctor
   */
  public String getPrescribeddoctor() {
    return prescribeddoctor;
  }

  /**
   * Sets the prescribeddoctor.
   *
   * @param prescribeddoctor the new prescribeddoctor
   */
  public void setPrescribeddoctor(String prescribeddoctor) {
    this.prescribeddoctor = prescribeddoctor;
  }

  /**
   * Gets the presdate.
   *
   * @return the presdate
   */
  public String getPresdate() {
    return presdate;
  }

  /**
   * Sets the presdate.
   *
   * @param presdate the new presdate
   */
  public void setPresdate(String presdate) {
    this.presdate = presdate;
  }

  /**
   * Gets the prestime.
   *
   * @return the prestime
   */
  public String getPrestime() {
    return prestime;
  }

  /**
   * Sets the prestime.
   *
   * @param prestime the new prestime
   */
  public void setPrestime(String prestime) {
    this.prestime = prestime;
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

  /**
   * Gets the testremark.
   *
   * @return the testremark
   */
  public String[] getTestremark() {
    return testremark;
  }

  /**
   * Sets the testremark.
   *
   * @param testremark the new testremark
   */
  public void setTestremark(String[] testremark) {
    this.testremark = testremark;
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
   * Gets the doctorname.
   *
   * @return the doctorname
   */
  public String[] getDoctorname() {
    return doctorname;
  }

  /**
   * Sets the doctorname.
   *
   * @param doctorname the new doctorname
   */
  public void setDoctorname(String[] doctorname) {
    this.doctorname = doctorname;
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
   * Gets the htest dept.
   *
   * @return the htest dept
   */
  public String[] getHtestDept() {
    return htestDept;
  }

  /**
   * Sets the htest dept.
   *
   * @param htestDept the new htest dept
   */
  public void setHtestDept(String[] htestDept) {
    this.htestDept = htestDept;
  }

  /**
   * Gets the htest id.
   *
   * @return the htest id
   */
  public String[] getHtestId() {
    return htestId;
  }

  /**
   * Sets the htest id.
   *
   * @param htestId the new htest id
   */
  public void setHtestId(String[] htestId) {
    this.htestId = htestId;
  }

  /**
   * Gets the htest name.
   *
   * @return the htest name
   */
  public String[] getHtestName() {
    return htestName;
  }

  /**
   * Sets the htest name.
   *
   * @param htestName the new htest name
   */
  public void setHtestName(String[] htestName) {
    this.htestName = htestName;
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
   * Gets the time.
   *
   * @return the time
   */
  public String getTime() {
    return time;
  }

  /**
   * Sets the time.
   *
   * @param time the new time
   */
  public void setTime(String time) {
    this.time = time;
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
   * Gets the visitingdate.
   *
   * @return the visitingdate
   */
  public String[] getVisitingdate() {
    return visitingdate;
  }

  /**
   * Sets the visitingdate.
   *
   * @param visitingdate the new visitingdate
   */
  public void setVisitingdate(String[] visitingdate) {
    this.visitingdate = visitingdate;
  }

  /**
   * Gets the visitingtime.
   *
   * @return the visitingtime
   */
  public String[] getVisitingtime() {
    return visitingtime;
  }

  /**
   * Sets the visitingtime.
   *
   * @param visitingtime the new visitingtime
   */
  public void setVisitingtime(String[] visitingtime) {
    this.visitingtime = visitingtime;
  }

  /**
   * Gets the visitremarks.
   *
   * @return the visitremarks
   */
  public String[] getVisitremarks() {
    return visitremarks;
  }

  /**
   * Sets the visitremarks.
   *
   * @param visitremarks the new visitremarks
   */
  public void setVisitremarks(String[] visitremarks) {
    this.visitremarks = visitremarks;
  }

  /**
   * Gets the doctor id.
   *
   * @return the doctor id
   */
  public String[] getDoctorId() {
    return doctorId;
  }

  /**
   * Sets the doctor id.
   *
   * @param doctorId the new doctor id
   */
  public void setDoctorId(String[] doctorId) {
    this.doctorId = doctorId;
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
   * Gets the presdoctor.
   *
   * @return the presdoctor
   */
  public String getPresdoctor() {
    return presdoctor;
  }

  /**
   * Sets the presdoctor.
   *
   * @param presdoctor the new presdoctor
   */
  public void setPresdoctor(String presdoctor) {
    this.presdoctor = presdoctor;
  }

  /**
   * Gets the operationdeptid.
   *
   * @return the operationdeptid
   */
  public String[] getOperationdeptid() {
    return operationdeptid;
  }

  /**
   * Sets the operationdeptid.
   *
   * @param operationdeptid the new operationdeptid
   */
  public void setOperationdeptid(String[] operationdeptid) {
    this.operationdeptid = operationdeptid;
  }

  /**
   * Gets the operationid.
   *
   * @return the operationid
   */
  public String[] getOperationid() {
    return operationid;
  }

  /**
   * Sets the operationid.
   *
   * @param operationid the new operationid
   */
  public void setOperationid(String[] operationid) {
    this.operationid = operationid;
  }

  /**
   * Gets the operationremarks.
   *
   * @return the operationremarks
   */
  public String[] getOperationremarks() {
    return operationremarks;
  }

  /**
   * Sets the operationremarks.
   *
   * @param operationremarks the new operationremarks
   */
  public void setOperationremarks(String[] operationremarks) {
    this.operationremarks = operationremarks;
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
   * Gets the catogery.
   *
   * @return the catogery
   */
  public String[] getCatogery() {
    return catogery;
  }

  /**
   * Sets the catogery.
   *
   * @param catogery the new catogery
   */
  public void setCatogery(String[] catogery) {
    this.catogery = catogery;
  }

  /**
   * Gets the doctor consult charge.
   *
   * @return the doctor consult charge
   */
  public String[] getDoctorConsultCharge() {
    return doctorConsultCharge;
  }

  /**
   * Sets the doctor consult charge.
   *
   * @param doctorConsultCharge the new doctor consult charge
   */
  public void setDoctorConsultCharge(String[] doctorConsultCharge) {
    this.doctorConsultCharge = doctorConsultCharge;
  }

  /**
   * Gets the doctor payment.
   *
   * @return the doctor payment
   */
  public String[] getDoctorPayment() {
    return doctorPayment;
  }

  /**
   * Sets the doctor payment.
   *
   * @param doctorPayment the new doctor payment
   */
  public void setDoctorPayment(String[] doctorPayment) {
    this.doctorPayment = doctorPayment;
  }

  /**
   * Gets the doctor pay percent.
   *
   * @return the doctor pay percent
   */
  public String[] getDoctorPayPercent() {
    return doctorPayPercent;
  }

  /**
   * Sets the doctor pay percent.
   *
   * @param doctorPayPercent the new doctor pay percent
   */
  public void setDoctorPayPercent(String[] doctorPayPercent) {
    this.doctorPayPercent = doctorPayPercent;
  }

  /**
   * Gets the doctor consult discount.
   *
   * @return the doctor consult discount
   */
  public String[] getDoctorConsultDiscount() {
    return doctorConsultDiscount;
  }

  /**
   * Sets the doctor consult discount.
   *
   * @param doctorConsultDiscount the new doctor consult discount
   */
  public void setDoctorConsultDiscount(String[] doctorConsultDiscount) {
    this.doctorConsultDiscount = doctorConsultDiscount;
  }

  /**
   * Gets the tilldate.
   *
   * @return the tilldate
   */
  public String[] getTilldate() {
    return tilldate;
  }

  /**
   * Sets the tilldate.
   *
   * @param tilldate the new tilldate
   */
  public void setTilldate(String[] tilldate) {
    this.tilldate = tilldate;
  }

  /**
   * Gets the tilltime.
   *
   * @return the tilltime
   */
  public String[] getTilltime() {
    return tilltime;
  }

  /**
   * Sets the tilltime.
   *
   * @param tilltime the new tilltime
   */
  public void setTilltime(String[] tilltime) {
    this.tilltime = tilltime;
  }

  /**
   * Gets the usedate.
   *
   * @return the usedate
   */
  public String[] getUsedate() {
    return usedate;
  }

  /**
   * Sets the usedate.
   *
   * @param usedate the new usedate
   */
  public void setUsedate(String[] usedate) {
    this.usedate = usedate;
  }

  /**
   * Gets the usetime.
   *
   * @return the usetime
   */
  public String[] getUsetime() {
    return usetime;
  }

  /**
   * Sets the usetime.
   *
   * @param usetime the new usetime
   */
  public void setUsetime(String[] usetime) {
    this.usetime = usetime;
  }

  /**
   * Gets the bedamt.
   *
   * @return the bedamt
   */
  public String[] getBedamt() {
    return bedamt;
  }

  /**
   * Sets the bedamt.
   *
   * @param bedamt the new bedamt
   */
  public void setBedamt(String[] bedamt) {
    this.bedamt = bedamt;
  }

  /**
   * Gets the bedname.
   *
   * @return the bedname
   */
  public String[] getBedname() {
    return bedname;
  }

  /**
   * Sets the bedname.
   *
   * @param bedname the new bedname
   */
  public void setBedname(String[] bedname) {
    this.bedname = bedname;
  }

  /**
   * Gets the bedqty.
   *
   * @return the bedqty
   */
  public String[] getBedqty() {
    return bedqty;
  }

  /**
   * Sets the bedqty.
   *
   * @param bedqty the new bedqty
   */
  public void setBedqty(String[] bedqty) {
    this.bedqty = bedqty;
  }

  /**
   * Gets the bedrate.
   *
   * @return the bedrate
   */
  public String[] getBedrate() {
    return bedrate;
  }

  /**
   * Sets the bedrate.
   *
   * @param bedrate the new bedrate
   */
  public void setBedrate(String[] bedrate) {
    this.bedrate = bedrate;
  }

  /**
   * Gets the docamt.
   *
   * @return the docamt
   */
  public String[] getDocamt() {
    return docamt;
  }

  /**
   * Sets the docamt.
   *
   * @param docamt the new docamt
   */
  public void setDocamt(String[] docamt) {
    this.docamt = docamt;
  }

  /**
   * Gets the docqty.
   *
   * @return the docqty
   */
  public String[] getDocqty() {
    return docqty;
  }

  /**
   * Sets the docqty.
   *
   * @param docqty the new docqty
   */
  public void setDocqty(String[] docqty) {
    this.docqty = docqty;
  }

  /**
   * Gets the docrate.
   *
   * @return the docrate
   */
  public String[] getDocrate() {
    return docrate;
  }

  /**
   * Sets the docrate.
   *
   * @param docrate the new docrate
   */
  public void setDocrate(String[] docrate) {
    this.docrate = docrate;
  }

  /**
   * Gets the equipamt.
   *
   * @return the equipamt
   */
  public String[] getEquipamt() {
    return equipamt;
  }

  /**
   * Sets the equipamt.
   *
   * @param equipamt the new equipamt
   */
  public void setEquipamt(String[] equipamt) {
    this.equipamt = equipamt;
  }

  /**
   * Gets the equiprate.
   *
   * @return the equiprate
   */
  public String[] getEquiprate() {
    return equiprate;
  }

  /**
   * Sets the equiprate.
   *
   * @param equiprate the new equiprate
   */
  public void setEquiprate(String[] equiprate) {
    this.equiprate = equiprate;
  }

  /**
   * Gets the medamt.
   *
   * @return the medamt
   */
  public String[] getMedamt() {
    return medamt;
  }

  /**
   * Sets the medamt.
   *
   * @param medamt the new medamt
   */
  public void setMedamt(String[] medamt) {
    this.medamt = medamt;
  }

  /**
   * Gets the medrate.
   *
   * @return the medrate
   */
  public String[] getMedrate() {
    return medrate;
  }

  /**
   * Sets the medrate.
   *
   * @param medrate the new medrate
   */
  public void setMedrate(String[] medrate) {
    this.medrate = medrate;
  }

  /**
   * Gets the opeamt.
   *
   * @return the opeamt
   */
  public String[] getOpeamt() {
    return opeamt;
  }

  /**
   * Sets the opeamt.
   *
   * @param opeamt the new opeamt
   */
  public void setOpeamt(String[] opeamt) {
    this.opeamt = opeamt;
  }

  /**
   * Gets the opeqty.
   *
   * @return the opeqty
   */
  public String[] getOpeqty() {
    return opeqty;
  }

  /**
   * Sets the opeqty.
   *
   * @param opeqty the new opeqty
   */
  public void setOpeqty(String[] opeqty) {
    this.opeqty = opeqty;
  }

  /**
   * Gets the operate.
   *
   * @return the operate
   */
  public String[] getOperate() {
    return operate;
  }

  /**
   * Sets the operate.
   *
   * @param operate the new operate
   */
  public void setOperate(String[] operate) {
    this.operate = operate;
  }

  /**
   * Gets the otherseramt.
   *
   * @return the otherseramt
   */
  public String[] getOtherseramt() {
    return otherseramt;
  }

  /**
   * Sets the otherseramt.
   *
   * @param otherseramt the new otherseramt
   */
  public void setOtherseramt(String[] otherseramt) {
    this.otherseramt = otherseramt;
  }

  /**
   * Gets the otherserrate.
   *
   * @return the otherserrate
   */
  public String[] getOtherserrate() {
    return otherserrate;
  }

  /**
   * Sets the otherserrate.
   *
   * @param otherserrate the new otherserrate
   */
  public void setOtherserrate(String[] otherserrate) {
    this.otherserrate = otherserrate;
  }

  /**
   * Gets the servamt.
   *
   * @return the servamt
   */
  public String[] getServamt() {
    return servamt;
  }

  /**
   * Sets the servamt.
   *
   * @param servamt the new servamt
   */
  public void setServamt(String[] servamt) {
    this.servamt = servamt;
  }

  /**
   * Gets the servrate.
   *
   * @return the servrate
   */
  public String[] getServrate() {
    return servrate;
  }

  /**
   * Sets the servrate.
   *
   * @param servrate the new servrate
   */
  public void setServrate(String[] servrate) {
    this.servrate = servrate;
  }

  /**
   * Gets the testamt.
   *
   * @return the testamt
   */
  public String[] getTestamt() {
    return testamt;
  }

  /**
   * Sets the testamt.
   *
   * @param testamt the new testamt
   */
  public void setTestamt(String[] testamt) {
    this.testamt = testamt;
  }

  /**
   * Gets the testqty.
   *
   * @return the testqty
   */
  public String[] getTestqty() {
    return testqty;
  }

  /**
   * Sets the testqty.
   *
   * @param testqty the new testqty
   */
  public void setTestqty(String[] testqty) {
    this.testqty = testqty;
  }

  /**
   * Gets the testrate.
   *
   * @return the testrate
   */
  public String[] getTestrate() {
    return testrate;
  }

  /**
   * Sets the testrate.
   *
   * @param testrate the new testrate
   */
  public void setTestrate(String[] testrate) {
    this.testrate = testrate;
  }

  /**
   * Gets the chargetype.
   *
   * @return the chargetype
   */
  public String[] getChargetype() {
    return chargetype;
  }

  /**
   * Sets the chargetype.
   *
   * @param chargetype the new chargetype
   */
  public void setChargetype(String[] chargetype) {
    this.chargetype = chargetype;
  }

  /**
   * Gets the bedtype.
   *
   * @return the bedtype
   */
  public String getBedtype() {
    return bedtype;
  }

  /**
   * Sets the bedtype.
   *
   * @param bedtype the new bedtype
   */
  public void setBedtype(String bedtype) {
    this.bedtype = bedtype;
  }

  /**
   * Gets the organizations.
   *
   * @return the organizations
   */
  public String getOrganizations() {
    return organizations;
  }

  /**
   * Sets the organizations.
   *
   * @param organizations the new organizations
   */
  public void setOrganizations(String organizations) {
    this.organizations = organizations;
  }

  /**
   * Gets the total amt.
   *
   * @return the total amt
   */
  public String getTotalAmt() {
    return totalAmt;
  }

  /**
   * Sets the total amt.
   *
   * @param totalAmt the new total amt
   */
  public void setTotalAmt(String totalAmt) {
    this.totalAmt = totalAmt;
  }

  /**
   * Gets the htestconsult id.
   *
   * @return the htestconsult id
   */
  public String[] getHtestconsultId() {
    return htestconsultId;
  }

  /**
   * Sets the htestconsult id.
   *
   * @param htestconsultId the new htestconsult id
   */
  public void setHtestconsultId(String[] htestconsultId) {
    this.htestconsultId = htestconsultId;
  }

  /**
   * Gets the hservice consult id.
   *
   * @return the hservice consult id
   */
  public String[] getHserviceConsultId() {
    return hserviceConsultId;
  }

  /**
   * Sets the hservice consult id.
   *
   * @param hserviceConsultId the new hservice consult id
   */
  public void setHserviceConsultId(String[] hserviceConsultId) {
    this.hserviceConsultId = hserviceConsultId;
  }

  /**
   * Gets the hservicepres id.
   *
   * @return the hservicepres id
   */
  public String[] getHservicepresId() {
    return hservicepresId;
  }

  /**
   * Sets the hservicepres id.
   *
   * @param hservicepresId the new hservicepres id
   */
  public void setHservicepresId(String[] hservicepresId) {
    this.hservicepresId = hservicepresId;
  }

  /**
   * Gets the htestpres id.
   *
   * @return the htestpres id
   */
  public String[] getHtestpresId() {
    return htestpresId;
  }

  /**
   * Sets the htestpres id.
   *
   * @param htestpresId the new htestpres id
   */
  public void setHtestpresId(String[] htestpresId) {
    this.htestpresId = htestpresId;
  }

  /**
   * Gets the mealid.
   *
   * @return the mealid
   */
  public String[] getMealid() {
    return mealid;
  }

  /**
   * Sets the mealid.
   *
   * @param mealid the new mealid
   */
  public void setMealid(String[] mealid) {
    this.mealid = mealid;
  }

  /**
   * Gets the mealname.
   *
   * @return the mealname
   */
  public String[] getMealname() {
    return mealname;
  }

  /**
   * Sets the mealname.
   *
   * @param mealname the new mealname
   */
  public void setMealname(String[] mealname) {
    this.mealname = mealname;
  }

  /**
   * Gets the mealremark.
   *
   * @return the mealremark
   */
  public String[] getMealremark() {
    return mealremark;
  }

  /**
   * Sets the mealremark.
   *
   * @param mealremark the new mealremark
   */
  public void setMealremark(String[] mealremark) {
    this.mealremark = mealremark;
  }

  /**
   * Gets the hmealpres id.
   *
   * @return the hmealpres id
   */
  public String[] getHmealpresId() {
    return hmealpresId;
  }

  /**
   * Sets the hmealpres id.
   *
   * @param hmealpresId the new hmealpres id
   */
  public void setHmealpresId(String[] hmealpresId) {
    this.hmealpresId = hmealpresId;
  }

  /**
   * Gets the meal qty.
   *
   * @return the meal qty
   */
  public String[] getMealQty() {
    return mealQty;
  }

  /**
   * Sets the meal qty.
   *
   * @param mealQty the new meal qty
   */
  public void setMealQty(String[] mealQty) {
    this.mealQty = mealQty;
  }

  /**
   * Gets the mealdate.
   *
   * @return the mealdate
   */
  public String[] getMealdate() {
    return mealdate;
  }

  /**
   * Sets the mealdate.
   *
   * @param mealdate the new mealdate
   */
  public void setMealdate(String[] mealdate) {
    this.mealdate = mealdate;
  }

  /**
   * Gets the mealdelete.
   *
   * @return the mealdelete
   */
  /*
   * public String[] getMealtime() { return mealtime; } public void setMealtime(String[] mealtime) {
   * this.mealtime = mealtime; }
   */public String[] getMealdelete() {
    return mealdelete;
  }

  /**
   * Sets the mealdelete.
   *
   * @param mealdelete the new mealdelete
   */
  public void setMealdelete(String[] mealdelete) {
    this.mealdelete = mealdelete;
  }

  /**
   * Gets the mealtiming.
   *
   * @return the mealtiming
   */
  public String[] getMealtiming() {
    return mealtiming;
  }

  /**
   * Sets the mealtiming.
   *
   * @param mealtiming the new mealtiming
   */
  public void setMealtiming(String[] mealtiming) {
    this.mealtiming = mealtiming;
  }

  /**
   * Gets the spl meal time.
   *
   * @return the spl meal time
   */
  public String[] getSplMealTime() {
    return splMealTime;
  }

  /**
   * Sets the spl meal time.
   *
   * @param splMealTime the new spl meal time
   */
  public void setSplMealTime(String[] splMealTime) {
    this.splMealTime = splMealTime;
  }

  /**
   * Gets the test pack.
   *
   * @return the test pack
   */
  public String[] getTest_pack() {
    return test_pack;
  }

  /**
   * Sets the test pack.
   *
   * @param test_pack the new test pack
   */
  public void setTest_pack(String[] test_pack) {
    this.test_pack = test_pack;
  }

  /**
   * Gets the specialization.
   *
   * @return the specialization
   */
  public String[] getSpecialization() {
    return specialization;
  }

  /**
   * Sets the specialization.
   *
   * @param specialization the new specialization
   */
  public void setSpecialization(String[] specialization) {
    this.specialization = specialization;
  }
}
