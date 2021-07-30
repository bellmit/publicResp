package com.insta.hms.ipservices;

import org.apache.struts.action.ActionForm;

// TODO: Auto-generated Javadoc
/**
 * The Class IPDashBoardForm.
 */
public class IPDashBoardForm extends ActionForm {

  /** The searchmrno. */
  private String searchmrno;
  
  /** The first name. */
  private String firstName;
  
  /** The middle name. */
  private String middleName;
  
  /** The last name. */
  private String lastName;
  
  /** The searchdoctor. */
  private String searchdoctor;
  
  /** The mr no. */
  private String mrNo;
  
  /** The patientid. */
  private String patientid;
  
  /** The pat name. */
  private String patName;
  
  /** The doctor. */
  private String doctor;
  
  /** The patientorg. */
  private String patientorg;
  
  /** The ward. */
  private String ward;
  
  /** The bedtype. */
  private String bedtype;
  
  /** The bednumber. */
  private String bednumber;
  
  /** The estimateddays. */
  private String estimateddays;
  
  /** The daycare. */
  private String daycare;
  
  /** The patientbed. */
  private String patientbed;
  
  /** The hiddenmrno. */
  private String hiddenmrno;
  
  /** The patientname. */
  private String patientname;
  
  /** The admiteddate. */
  private String admiteddate;
  
  /** The admitteddoctor. */
  private String admitteddoctor;

  /** The date. */
  private String date;
  
  /** The time. */
  private String time;
  
  /** The gender. */
  private String gender;
  
  /** The age. */
  private String age;
  
  /** The agein. */
  private String agein;
  
  /** The date of birth. */
  private String dateOfBirth;
  
  /** The time of birth. */
  private String timeOfBirth;
  
  /** The admitward. */
  private String admitward;
  
  /** The admitbedtype. */
  private String admitbedtype;
  
  /** The admitbednumber. */
  private String admitbednumber;
  
  /** The admit pat dept id. */
  private String admitPatDeptId;
  
  /** The billtype. */
  private String billtype;
  
  /** The expecteddays. */
  private String expecteddays;
  
  /** The patient dept. */
  private String patientDept;
  
  /** The patient ward. */
  private String patientWard;
  
  /** The admitdaycare. */
  private String admitdaycare;

  /** The baby attending dept IP. */
  private String babyAttendingDeptIP;
  
  /** The baby attending doc IP. */
  private String babyAttendingDocIP;

  /** The department. */
  private String department;
  
  /** The wards. */
  private String[] wards;
  
  /** The doctors. */
  private String[] doctors;
  
  /** The fdate. */
  private String fdate;
  
  /** The tdate. */
  private String tdate;
  
  /** The sort order. */
  private String sortOrder;
  
  /** The sort reverse. */
  private boolean sortReverse;

  /** The shiftbedid. */
  private int shiftbedid;
  
  /** The admitbedid. */
  private int admitbedid;
  
  /** The admitwardid. */
  private String admitwardid;
  
  /** The shiftwardid. */
  private String shiftwardid;

  /** The startpage. */
  private String startpage;
  
  /** The endpage. */
  private String endpage;
  
  /** The page num. */
  private String pageNum;

  /** The shiftward. */
  private String shiftward;
  
  /** The bdate. */
  private String bdate;
  
  /** The btime. */
  private String btime;
  
  /** The dualoccupancy. */
  private String dualoccupancy;
  
  /** The extendeddays. */
  private String extendeddays;
  
  /** The shiftexpecteddays. */
  private String shiftexpecteddays;
  
  /** The shiftbedtype. */
  private String shiftbedtype;
  
  /** The wardname. */
  private String wardname;
  
  /** The changebasebed. */
  private String changebasebed;
  
  /** The child birth remarks. */
  private String childBirthRemarks;

  /** The nationality id. */
  private String nationalityId;
  
  /** The bill. */
  private String bill;
  
  private String deliveryType;

  private Integer caesareanIndicationId;

  /**
   * Instantiates a new IP dash board form.
   */
  public IPDashBoardForm() {
    this.startpage = "";
    this.endpage = "";
    this.pageNum = "";
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
   * Gets the expecteddays.
   *
   * @return the expecteddays
   */
  public String getExpecteddays() {
    return expecteddays;
  }

  /**
   * Sets the expecteddays.
   *
   * @param expecteddays the new expecteddays
   */
  public void setExpecteddays(String expecteddays) {
    this.expecteddays = expecteddays;
  }

  /**
   * Gets the billtype.
   *
   * @return the billtype
   */
  public String getBilltype() {
    return billtype;
  }

  /**
   * Sets the billtype.
   *
   * @param billtype the new billtype
   */
  public void setBilltype(String billtype) {
    this.billtype = billtype;
  }

  /**
   * Gets the agein.
   *
   * @return the agein
   */
  public String getAgein() {
    return agein;
  }

  /**
   * Sets the agein.
   *
   * @param agein the new agein
   */
  public void setAgein(String agein) {
    this.agein = agein;
  }

  /**
   * Gets the gender.
   *
   * @return the gender
   */
  public String getGender() {
    return gender;
  }

  /**
   * Sets the gender.
   *
   * @param gender the new gender
   */
  public void setGender(String gender) {
    this.gender = gender;
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
   * Gets the admitteddoctor.
   *
   * @return the admitteddoctor
   */
  public String getAdmitteddoctor() {
    return admitteddoctor;
  }

  /**
   * Sets the admitteddoctor.
   *
   * @param admitteddoctor the new admitteddoctor
   */
  public void setAdmitteddoctor(String admitteddoctor) {
    this.admitteddoctor = admitteddoctor;
  }

  /**
   * Gets the admiteddate.
   *
   * @return the admiteddate
   */
  public String getAdmiteddate() {
    return admiteddate;
  }

  /**
   * Sets the admiteddate.
   *
   * @param admiteddate the new admiteddate
   */
  public void setAdmiteddate(String admiteddate) {
    this.admiteddate = admiteddate;
  }

  /**
   * Gets the patientname.
   *
   * @return the patientname
   */
  public String getPatientname() {
    return patientname;
  }

  /**
   * Sets the patientname.
   *
   * @param patientname the new patientname
   */
  public void setPatientname(String patientname) {
    this.patientname = patientname;
  }

  /**
   * Gets the hiddenmrno.
   *
   * @return the hiddenmrno
   */
  public String getHiddenmrno() {
    return hiddenmrno;
  }

  /**
   * Sets the hiddenmrno.
   *
   * @param hiddenmrno the new hiddenmrno
   */
  public void setHiddenmrno(String hiddenmrno) {
    this.hiddenmrno = hiddenmrno;
  }

  /**
   * Gets the patientbed.
   *
   * @return the patientbed
   */
  public String getPatientbed() {
    return patientbed;
  }

  /**
   * Sets the patientbed.
   *
   * @param patientbed the new patientbed
   */
  public void setPatientbed(String patientbed) {
    this.patientbed = patientbed;
  }

  /**
   * Gets the bednumber.
   *
   * @return the bednumber
   */
  public String getBednumber() {
    return bednumber;
  }

  /**
   * Sets the bednumber.
   *
   * @param bednumber the new bednumber
   */
  public void setBednumber(String bednumber) {
    this.bednumber = bednumber;
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
   * Gets the daycare.
   *
   * @return the daycare
   */
  public String getDaycare() {
    return daycare;
  }

  /**
   * Sets the daycare.
   *
   * @param daycare the new daycare
   */
  public void setDaycare(String daycare) {
    this.daycare = daycare;
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
   * Gets the estimateddays.
   *
   * @return the estimateddays
   */
  public String getEstimateddays() {
    return estimateddays;
  }

  /**
   * Sets the estimateddays.
   *
   * @param estimateddays the new estimateddays
   */
  public void setEstimateddays(String estimateddays) {
    this.estimateddays = estimateddays;
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
   * Gets the patientorg.
   *
   * @return the patientorg
   */
  public String getPatientorg() {
    return patientorg;
  }

  /**
   * Sets the patientorg.
   *
   * @param patientorg the new patientorg
   */
  public void setPatientorg(String patientorg) {
    this.patientorg = patientorg;
  }

  /**
   * Gets the pat name.
   *
   * @return the pat name
   */
  public String getPatName() {
    return patName;
  }

  /**
   * Sets the pat name.
   *
   * @param patName the new pat name
   */
  public void setPatName(String patName) {
    this.patName = patName;
  }

  /**
   * Gets the ward.
   *
   * @return the ward
   */
  public String getWard() {
    return ward;
  }

  /**
   * Sets the ward.
   *
   * @param ward the new ward
   */
  public void setWard(String ward) {
    this.ward = ward;
  }

  /**
   * Gets the first name.
   *
   * @return the first name
   */
  public String getFirstName() {
    return firstName;
  }

  /**
   * Sets the first name.
   *
   * @param firstName the new first name
   */
  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  /**
   * Gets the last name.
   *
   * @return the last name
   */
  public String getLastName() {
    return lastName;
  }

  /**
   * Sets the last name.
   *
   * @param lastName the new last name
   */
  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  /**
   * Gets the searchdoctor.
   *
   * @return the searchdoctor
   */
  public String getSearchdoctor() {
    return searchdoctor;
  }

  /**
   * Sets the searchdoctor.
   *
   * @param searchdoctor the new searchdoctor
   */
  public void setSearchdoctor(String searchdoctor) {
    this.searchdoctor = searchdoctor;
  }

  /**
   * Gets the searchmrno.
   *
   * @return the searchmrno
   */
  public String getSearchmrno() {
    return searchmrno;
  }

  /**
   * Sets the searchmrno.
   *
   * @param searchmrno the new searchmrno
   */
  public void setSearchmrno(String searchmrno) {
    this.searchmrno = searchmrno;
  }

  /**
   * Gets the age.
   *
   * @return the age
   */
  public String getAge() {
    return age;
  }

  /**
   * Sets the age.
   *
   * @param age the new age
   */
  public void setAge(String age) {
    this.age = age;
  }

  /**
   * Gets the date of birth.
   *
   * @return the date of birth
   */
  public String getDateOfBirth() {
    return dateOfBirth;
  }

  /**
   * Sets the date of birth.
   *
   * @param dateOfBirth the new date of birth
   */
  public void setDateOfBirth(String dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
  }

  /**
   * Gets the admitbednumber.
   *
   * @return the admitbednumber
   */
  public String getAdmitbednumber() {
    return admitbednumber;
  }

  /**
   * Sets the admitbednumber.
   *
   * @param admitbednumber the new admitbednumber
   */
  public void setAdmitbednumber(String admitbednumber) {
    this.admitbednumber = admitbednumber;
  }

  /**
   * Gets the admitbedtype.
   *
   * @return the admitbedtype
   */
  public String getAdmitbedtype() {
    return admitbedtype;
  }

  /**
   * Sets the admitbedtype.
   *
   * @param admitbedtype the new admitbedtype
   */
  public void setAdmitbedtype(String admitbedtype) {
    this.admitbedtype = admitbedtype;
  }

  /**
   * Gets the admitward.
   *
   * @return the admitward
   */
  public String getAdmitward() {
    return admitward;
  }

  /**
   * Sets the admitward.
   *
   * @param admitward the new admitward
   */
  public void setAdmitward(String admitward) {
    this.admitward = admitward;
  }

  /**
   * Gets the admit pat dept id.
   *
   * @return the admit pat dept id
   */
  public String getAdmitPatDeptId() {
    return admitPatDeptId;
  }

  /**
   * Sets the admit pat dept id.
   *
   * @param admitPatDeptId the new admit pat dept id
   */
  public void setAdmitPatDeptId(String admitPatDeptId) {
    this.admitPatDeptId = admitPatDeptId;
  }

  /**
   * Gets the patient ward.
   *
   * @return the patient ward
   */
  public String getPatientWard() {
    return patientWard;
  }

  /**
   * Sets the patient ward.
   *
   * @param patientWard the new patient ward
   */
  public void setPatientWard(String patientWard) {
    this.patientWard = patientWard;
  }

  /**
   * Gets the baby attending dept IP.
   *
   * @return the baby attending dept IP
   */
  public String getBabyAttendingDeptIP() {
    return babyAttendingDeptIP;
  }

  /**
   * Sets the baby attending dept IP.
   *
   * @param babyAttendingDeptIP the new baby attending dept IP
   */
  public void setBabyAttendingDeptIP(String babyAttendingDeptIP) {
    this.babyAttendingDeptIP = babyAttendingDeptIP;
  }

  /**
   * Gets the baby attending doc IP.
   *
   * @return the baby attending doc IP
   */
  public String getBabyAttendingDocIP() {
    return babyAttendingDocIP;
  }

  /**
   * Sets the baby attending doc IP.
   *
   * @param babyAttendingDocIP the new baby attending doc IP
   */
  public void setBabyAttendingDocIP(String babyAttendingDocIP) {
    this.babyAttendingDocIP = babyAttendingDocIP;
  }

  /**
   * Gets the admitdaycare.
   *
   * @return the admitdaycare
   */
  public String getAdmitdaycare() {
    return admitdaycare;
  }

  /**
   * Sets the admitdaycare.
   *
   * @param admitdaycare the new admitdaycare
   */
  public void setAdmitdaycare(String admitdaycare) {
    this.admitdaycare = admitdaycare;
  }

  /**
   * Gets the department.
   *
   * @return the department
   */
  public String getDepartment() {
    return department;
  }

  /**
   * Sets the department.
   *
   * @param department the new department
   */
  public void setDepartment(String department) {
    this.department = department;
  }

  /**
   * Gets the wards.
   *
   * @return the wards
   */
  public String[] getWards() {
    return wards;
  }

  /**
   * Sets the wards.
   *
   * @param wards the new wards
   */
  public void setWards(String[] wards) {
    this.wards = wards;
  }

  /**
   * Gets the doctors.
   *
   * @return the doctors
   */
  public String[] getDoctors() {
    return doctors;
  }

  /**
   * Sets the doctors.
   *
   * @param doctors the new doctors
   */
  public void setDoctors(String[] doctors) {
    this.doctors = doctors;
  }

  /**
   * Gets the fdate.
   *
   * @return the fdate
   */
  public String getFdate() {
    return fdate;
  }

  /**
   * Sets the fdate.
   *
   * @param fdate the new fdate
   */
  public void setFdate(String fdate) {
    this.fdate = fdate;
  }

  /**
   * Gets the tdate.
   *
   * @return the tdate
   */
  public String getTdate() {
    return tdate;
  }

  /**
   * Sets the tdate.
   *
   * @param tdate the new tdate
   */
  public void setTdate(String tdate) {
    this.tdate = tdate;
  }

  /**
   * Gets the sort order.
   *
   * @return the sort order
   */
  public String getSortOrder() {
    return sortOrder;
  }

  /**
   * Sets the sort order.
   *
   * @param sortOrder the new sort order
   */
  public void setSortOrder(String sortOrder) {
    this.sortOrder = sortOrder;
  }

  /**
   * Gets the sort reverse.
   *
   * @return the sort reverse
   */
  public boolean getSortReverse() {
    return sortReverse;
  }

  /**
   * Sets the sort reverse.
   *
   * @param sortReverse the new sort reverse
   */
  public void setSortReverse(boolean sortReverse) {
    this.sortReverse = sortReverse;
  }

  /**
   * Gets the endpage.
   *
   * @return the endpage
   */
  public String getEndpage() {
    return endpage;
  }

  /**
   * Sets the endpage.
   *
   * @param endpage the new endpage
   */
  public void setEndpage(String endpage) {
    this.endpage = endpage;
  }

  /**
   * Gets the startpage.
   *
   * @return the startpage
   */
  public String getStartpage() {
    return startpage;
  }

  /**
   * Sets the startpage.
   *
   * @param startpage the new startpage
   */
  public void setStartpage(String startpage) {
    this.startpage = startpage;
  }

  /**
   * Gets the page num.
   *
   * @return the page num
   */
  public String getPageNum() {
    return pageNum;
  }

  /**
   * Sets the page num.
   *
   * @param pageNum the new page num
   */
  public void setPageNum(String pageNum) {
    this.pageNum = pageNum;
  }

  /**
   * Gets the time of birth.
   *
   * @return the time of birth
   */
  public String getTimeOfBirth() {
    return timeOfBirth;
  }

  /**
   * Sets the time of birth.
   *
   * @param timeOfBirth the new time of birth
   */
  public void setTimeOfBirth(String timeOfBirth) {
    this.timeOfBirth = timeOfBirth;
  }

  /**
   * Gets the admitbedid.
   *
   * @return the admitbedid
   */
  public int getAdmitbedid() {
    return admitbedid;
  }

  /**
   * Sets the admitbedid.
   *
   * @param admitbedid the new admitbedid
   */
  public void setAdmitbedid(int admitbedid) {
    this.admitbedid = admitbedid;
  }

  /**
   * Gets the shiftbedid.
   *
   * @return the shiftbedid
   */
  public int getShiftbedid() {
    return shiftbedid;
  }

  /**
   * Sets the shiftbedid.
   *
   * @param shiftbedid the new shiftbedid
   */
  public void setShiftbedid(int shiftbedid) {
    this.shiftbedid = shiftbedid;
  }

  /**
   * Gets the admitwardid.
   *
   * @return the admitwardid
   */
  public String getAdmitwardid() {
    return admitwardid;
  }

  /**
   * Sets the admitwardid.
   *
   * @param admitwardid the new admitwardid
   */
  public void setAdmitwardid(String admitwardid) {
    this.admitwardid = admitwardid;
  }

  /**
   * Gets the shiftwardid.
   *
   * @return the shiftwardid
   */
  public String getShiftwardid() {
    return shiftwardid;
  }

  /**
   * Sets the shiftwardid.
   *
   * @param shiftwardid the new shiftwardid
   */
  public void setShiftwardid(String shiftwardid) {
    this.shiftwardid = shiftwardid;
  }

  /**
   * Gets the bdate.
   *
   * @return the bdate
   */
  public String getBdate() {
    return bdate;
  }

  /**
   * Sets the bdate.
   *
   * @param bdate the new bdate
   */
  public void setBdate(String bdate) {
    this.bdate = bdate;
  }

  /**
   * Gets the btime.
   *
   * @return the btime
   */
  public String getBtime() {
    return btime;
  }

  /**
   * Sets the btime.
   *
   * @param btime the new btime
   */
  public void setBtime(String btime) {
    this.btime = btime;
  }

  /**
   * Gets the dualoccupancy.
   *
   * @return the dualoccupancy
   */
  public String getDualoccupancy() {
    return dualoccupancy;
  }

  /**
   * Sets the dualoccupancy.
   *
   * @param dualoccupancy the new dualoccupancy
   */
  public void setDualoccupancy(String dualoccupancy) {
    this.dualoccupancy = dualoccupancy;
  }

  /**
   * Gets the extendeddays.
   *
   * @return the extendeddays
   */
  public String getExtendeddays() {
    return extendeddays;
  }

  /**
   * Sets the extendeddays.
   *
   * @param extendeddays the new extendeddays
   */
  public void setExtendeddays(String extendeddays) {
    this.extendeddays = extendeddays;
  }

  /**
   * Gets the shiftbedtype.
   *
   * @return the shiftbedtype
   */
  public String getShiftbedtype() {
    return shiftbedtype;
  }

  /**
   * Sets the shiftbedtype.
   *
   * @param shiftbedtype the new shiftbedtype
   */
  public void setShiftbedtype(String shiftbedtype) {
    this.shiftbedtype = shiftbedtype;
  }

  /**
   * Gets the shiftexpecteddays.
   *
   * @return the shiftexpecteddays
   */
  public String getShiftexpecteddays() {
    return shiftexpecteddays;
  }

  /**
   * Sets the shiftexpecteddays.
   *
   * @param shiftexpecteddays the new shiftexpecteddays
   */
  public void setShiftexpecteddays(String shiftexpecteddays) {
    this.shiftexpecteddays = shiftexpecteddays;
  }

  /**
   * Gets the shiftward.
   *
   * @return the shiftward
   */
  public String getShiftward() {
    return shiftward;
  }

  /**
   * Sets the shiftward.
   *
   * @param shiftward the new shiftward
   */
  public void setShiftward(String shiftward) {
    this.shiftward = shiftward;
  }

  /**
   * Gets the wardname.
   *
   * @return the wardname
   */
  public String getWardname() {
    return wardname;
  }

  /**
   * Sets the wardname.
   *
   * @param wardname the new wardname
   */
  public void setWardname(String wardname) {
    this.wardname = wardname;
  }

  /**
   * Gets the changebasebed.
   *
   * @return the changebasebed
   */
  public String getChangebasebed() {
    return changebasebed;
  }

  /**
   * Sets the changebasebed.
   *
   * @param changebasebed the new changebasebed
   */
  public void setChangebasebed(String changebasebed) {
    this.changebasebed = changebasebed;
  }

  /**
   * Gets the child birth remarks.
   *
   * @return the child birth remarks
   */
  public String getChildBirthRemarks() {
    return childBirthRemarks;
  }

  /**
   * Sets the child birth remarks.
   *
   * @param childBirthRemarks the new child birth remarks
   */
  public void setChildBirthRemarks(String childBirthRemarks) {
    this.childBirthRemarks = childBirthRemarks;
  }

  /**
   * Gets the middle name.
   *
   * @return the middle name
   */
  public String getMiddleName() {
    return middleName;
  }

  /**
   * Sets the middle name.
   *
   * @param middleName the new middle name
   */
  public void setMiddleName(String middleName) {
    this.middleName = middleName;
  }

  /**
   * Gets the nationality id.
   *
   * @return the nationality id
   */
  public String getNationalityId() {
    return nationalityId;
  }

  /**
   * Sets the nationality id.
   *
   * @param nationalityId the new nationality id
   */
  public void setNationalityId(String nationalityId) {
    this.nationalityId = nationalityId;
  }

  public String getDeliveryType() {
    return deliveryType;
  }

  public void setDeliveryType(String deliveryType) {
    this.deliveryType = deliveryType;
  }

  public Integer getCaesareanIndicationId() {
    return caesareanIndicationId;
  }

  public void setCaesareanIndicationId(Integer caesareanIndicationId) {
    this.caesareanIndicationId = caesareanIndicationId;
  } 
}
