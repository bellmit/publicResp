package com.insta.hms.ipservices;

// TODO: Auto-generated Javadoc
/**
 * The Class BedAdmissionDTO.
 */
public class BedAdmissionDTO {

  /**
   * Instantiates a new bed admission DTO.
   */
  public BedAdmissionDTO() {
    this.isBystanderBed = false;
  }

  /** The searchmrno. */
  private String searchmrno;
  
  /** The first name. */
  private String firstName;
  
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
  private float estimateddays;
  
  /** The daycare. */
  private String daycare;
  
  /** The prvbed. */
  private String prvbed;
  
  /** The is baby. */
  private String isBaby;
  
  /** The parent id. */
  private String parentId;
  
  /** The covert. */
  private String covert;

  /** The admitward. */
  private String admitward;
  
  /** The admitbedtype. */
  private String admitbedtype;
  
  /** The admitbednumber. */
  private String admitbednumber;
  
  /** The admit pat dept id. */
  private String admitPatDeptId;

  /** The billno. */
  private String billno;
  
  /** The noofdays. */
  private int noofdays;
  
  /** The state. */
  private String state = "NORMAL";
  
  /** The admitdaycare. */
  private String admitdaycare;
  
  /** The group. */
  private String group;

  /** The shiftbedid. */
  private int shiftbedid;
  
  /** The admitbedid. */
  private int admitbedid;
  
  /** The admitwardid. */
  private String admitwardid;
  
  /** The shiftwardid. */
  private String shiftwardid;
  
  /** The curentbedid. */
  private int curentbedid;

  /** The bill bedtype. */
  private String billBedtype;

  /** The basebedtype. */
  private String basebedtype;

  /** The bill. */
  private String bill;
  
  /** The changebasebed. */
  private boolean changebasebed;

  /** The duty doctor id. */
  private String dutyDoctorId;
  
  /** The is bystander bed. */
  private boolean isBystanderBed;
  
  /** The remarks. */
  private String remarks;

  /**
   * Checks if is bystander bed.
   *
   * @return true, if is bystander bed
   */
  public boolean isBystanderBed() {
    return isBystanderBed;
  }

  /**
   * Sets the bystander bed.
   *
   * @param isBystanderBed the new bystander bed
   */
  public void setBystanderBed(boolean isBystanderBed) {
    this.isBystanderBed = isBystanderBed;
  }

  /**
   * Gets the duty doctor id.
   *
   * @return the duty doctor id
   */
  public String getDuty_doctor_id() {
    return dutyDoctorId;
  }

  /**
   * Sets the duty doctor id.
   *
   * @param dutyDoctorId the new duty doctor id
   */
  public void setDuty_doctor_id(String dutyDoctorId) {
    this.dutyDoctorId = dutyDoctorId;
  }

  /**
   * Checks if is changebasebed.
   *
   * @return true, if is changebasebed
   */
  public boolean isChangebasebed() {
    return changebasebed;
  }

  /**
   * Sets the changebasebed.
   *
   * @param changebasebed the new changebasebed
   */
  public void setChangebasebed(boolean changebasebed) {
    this.changebasebed = changebasebed;
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
   * Gets the noofdays.
   *
   * @return the noofdays
   */
  public int getNoofdays() {
    return noofdays;
  }

  /**
   * Sets the noofdays.
   *
   * @param noofdays the new noofdays
   */
  public void setNoofdays(int noofdays) {
    this.noofdays = noofdays;
  }

  /**
   * Gets the state.
   *
   * @return the state
   */
  public String getState() {
    return state;
  }

  /**
   * Sets the state.
   *
   * @param state the new state
   */
  public void setState(String state) {
    this.state = state;
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
   * Gets the prvbed.
   *
   * @return the prvbed
   */
  public String getPrvbed() {
    return prvbed;
  }

  /**
   * Sets the prvbed.
   *
   * @param prvbed the new prvbed
   */
  public void setPrvbed(String prvbed) {
    this.prvbed = prvbed;
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
   * Gets the checks if is baby.
   *
   * @return the checks if is baby
   */
  public String getIsBaby() {
    return isBaby;
  }

  /**
   * Sets the checks if is baby.
   *
   * @param isBaby the new checks if is baby
   */
  public void setIsBaby(String isBaby) {
    this.isBaby = isBaby;
  }

  /**
   * Gets the parent id.
   *
   * @return the parent id
   */
  public String getParentId() {
    return parentId;
  }

  /**
   * Sets the parent id.
   *
   * @param parentId the new parent id
   */
  public void setParentId(String parentId) {
    this.parentId = parentId;
  }

  /**
   * Gets the billno.
   *
   * @return the billno
   */
  public String getBillno() {
    return billno;
  }

  /**
   * Sets the billno.
   *
   * @param billno the new billno
   */
  public void setBillno(String billno) {
    this.billno = billno;
  }

  /**
   * Gets the covert.
   *
   * @return the covert
   */
  public String getCovert() {
    return covert;
  }

  /**
   * Sets the covert.
   *
   * @param covert the new covert
   */
  public void setCovert(String covert) {
    this.covert = covert;
  }

  /**
   * Gets the group.
   *
   * @return the group
   */
  public String getGroup() {
    return group;
  }

  /**
   * Sets the group.
   *
   * @param group the new group
   */
  public void setGroup(String group) {
    this.group = group;
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
   * Gets the curentbedid.
   *
   * @return the curentbedid
   */
  public int getCurentbedid() {
    return curentbedid;
  }

  /**
   * Sets the curentbedid.
   *
   * @param curentbedid the new curentbedid
   */
  public void setCurentbedid(int curentbedid) {
    this.curentbedid = curentbedid;
  }

  /**
   * Gets the basebedtype.
   *
   * @return the basebedtype
   */
  public String getBasebedtype() {
    return basebedtype;
  }

  /**
   * Sets the basebedtype.
   *
   * @param basebedtype the new basebedtype
   */
  public void setBasebedtype(String basebedtype) {
    this.basebedtype = basebedtype;
  }

  /**
   * Sets the estimateddays.
   *
   * @param estimateddays the new estimateddays
   */
  public void setEstimateddays(float estimateddays) {
    this.estimateddays = estimateddays;
  }

  /**
   * Gets the estimateddays.
   *
   * @return the estimateddays
   */
  public float getEstimateddays() {
    return estimateddays;
  }

  /**
   * Gets the bill bedtype.
   *
   * @return the bill bedtype
   */
  public String getBillBedtype() {
    return billBedtype;
  }

  /**
   * Sets the bill bedtype.
   *
   * @param billBedtype the new bill bedtype
   */
  public void setBillBedtype(String billBedtype) {
    this.billBedtype = billBedtype;
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

}
