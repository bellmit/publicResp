package com.insta.hms.ipservices;

import java.sql.Timestamp;

// TODO: Auto-generated Javadoc
/**
 * The Class IpBedDetailsDTO.
 */
public class IpBedDetailsDTO {

  /** The mrno. */
  private String mrno;
  
  /** The patientid. */
  private String patientid;
  
  /** The patientname. */
  private String patientname;
  
  /** The doctor. */
  private String doctor;
  
  /** The date. */
  private String date;
  
  /** The bedname. */
  private String bedname;
  
  /** The prvbed. */
  private String prvbed;
  
  /** The prvward. */
  private String prvward;
  
  /** The curbed. */
  private String curbed;
  
  /** The curward. */
  private String curward;
  
  /** The curbedtype. */
  private String curbedtype;
  
  /** The time. */
  private String time;
  
  /** The extendeddays. */
  private String extendeddays;
  
  /** The shiftexpecteddays. */
  private int shiftexpecteddays;
  
  /** The orgid. */
  private String orgid;
  
  /** The ipconversion. */
  private String ipconversion;
  
  /** The userid. */
  private String userid;
  
  /** The convert. */
  private String convert;
  
  /** The patient dept. */
  private String patientDept;
  
  /** The bill. */
  private String bill;
  
  /** The startdate. */
  private String startdate;
  
  /** The enddate. */
  private String enddate;
  
  /** The finalise. */
  private boolean finalise;
  
  /** The admitdate. */
  private String admitdate;
  
  /** The admitid. */
  private int admitid;
  
  /** The shiftbedid. */
  private int shiftbedid;
  
  /** The shiftwardid. */
  private String shiftwardid;
  
  /** The curentbedid. */
  private int curentbedid;
  
  /** The from. */
  private String from = "";
  
  /** The days. */
  private int days;
  
  /** The unit. */
  private String unit;
  
  /** The changebasebed. */
  private String changebasebed;
  
  /** The last updated. */
  private Timestamp lastUpdated;

  /**
   * Gets the last updated.
   *
   * @return the last updated
   */
  public Timestamp getLastUpdated() {
    return lastUpdated;
  }

  /**
   * Sets the last updated.
   *
   * @param lastUpdated the new last updated
   */
  public void setLastUpdated(Timestamp lastUpdated) {
    this.lastUpdated = lastUpdated;
  }

  /**
   * Gets the days.
   *
   * @return the days
   */
  public int getDays() {
    return days;
  }

  /**
   * Sets the days.
   *
   * @param days the new days
   */
  public void setDays(int days) {
    this.days = days;
  }

  /**
   * Gets the from.
   *
   * @return the from
   */
  public String getFrom() {
    return from;
  }

  /**
   * Sets the from.
   *
   * @param from the new from
   */
  public void setFrom(String from) {
    this.from = from;
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
   * Gets the admitid.
   *
   * @return the admitid
   */
  public int getAdmitid() {
    return admitid;
  }

  /**
   * Sets the admitid.
   *
   * @param admitid the new admitid
   */
  public void setAdmitid(int admitid) {
    this.admitid = admitid;
  }

  /**
   * Checks if is finalise.
   *
   * @return true, if is finalise
   */
  public boolean isFinalise() {
    return finalise;
  }

  /**
   * Sets the finalise.
   *
   * @param finalise the new finalise
   */
  public void setFinalise(boolean finalise) {
    this.finalise = finalise;
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
   * Gets the convert.
   *
   * @return the convert
   */
  public String getConvert() {
    return convert;
  }

  /**
   * Sets the convert.
   *
   * @param convert the new convert
   */
  public void setConvert(String convert) {
    this.convert = convert;
  }

  /**
   * Gets the userid.
   *
   * @return the userid
   */
  public String getUserid() {
    return userid;
  }

  /**
   * Sets the userid.
   *
   * @param userid the new userid
   */
  public void setUserid(String userid) {
    this.userid = userid;
  }

  /**
   * Gets the ipconversion.
   *
   * @return the ipconversion
   */
  public String getIpconversion() {
    return ipconversion;
  }

  /**
   * Sets the ipconversion.
   *
   * @param ipconversion the new ipconversion
   */
  public void setIpconversion(String ipconversion) {
    this.ipconversion = ipconversion;
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
   * Gets the shiftexpecteddays.
   *
   * @return the shiftexpecteddays
   */
  public int getShiftexpecteddays() {
    return shiftexpecteddays;
  }

  /**
   * Sets the shiftexpecteddays.
   *
   * @param shiftexpecteddays the new shiftexpecteddays
   */
  public void setShiftexpecteddays(int shiftexpecteddays) {
    this.shiftexpecteddays = shiftexpecteddays;
  }

  /**
   * Gets the curbed.
   *
   * @return the curbed
   */
  public String getCurbed() {
    return curbed;
  }

  /**
   * Sets the curbed.
   *
   * @param curbed the new curbed
   */
  public void setCurbed(String curbed) {
    this.curbed = curbed;
  }

  /**
   * Gets the curward.
   *
   * @return the curward
   */
  public String getCurward() {
    return curward;
  }

  /**
   * Sets the curward.
   *
   * @param curward the new curward
   */
  public void setCurward(String curward) {
    this.curward = curward;
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
   * Gets the prvward.
   *
   * @return the prvward
   */
  public String getPrvward() {
    return prvward;
  }

  /**
   * Sets the prvward.
   *
   * @param prvward the new prvward
   */
  public void setPrvward(String prvward) {
    this.prvward = prvward;
  }

  /**
   * Gets the bedname.
   *
   * @return the bedname
   */
  public String getBedname() {
    return bedname;
  }

  /**
   * Sets the bedname.
   *
   * @param bedname the new bedname
   */
  public void setBedname(String bedname) {
    this.bedname = bedname;
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
   * Gets the curbedtype.
   *
   * @return the curbedtype
   */
  public String getCurbedtype() {
    return curbedtype;
  }

  /**
   * Sets the curbedtype.
   *
   * @param curbedtype the new curbedtype
   */
  public void setCurbedtype(String curbedtype) {
    this.curbedtype = curbedtype;
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
   * Gets the admitdate.
   *
   * @return the admitdate
   */
  public String getAdmitdate() {
    return admitdate;
  }

  /**
   * Sets the admitdate.
   *
   * @param admitdate the new admitdate
   */
  public void setAdmitdate(String admitdate) {
    this.admitdate = admitdate;
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
   * Gets the unit.
   *
   * @return the unit
   */
  public String getUnit() {
    return unit;
  }

  /**
   * Sets the unit.
   *
   * @param unit the new unit
   */
  public void setUnit(String unit) {
    this.unit = unit;
  }

}
