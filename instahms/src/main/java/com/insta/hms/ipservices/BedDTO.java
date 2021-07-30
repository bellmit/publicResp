package com.insta.hms.ipservices;

import java.sql.Timestamp;

// TODO: Auto-generated Javadoc
/**
 * The Class BedDTO.
 */
public class BedDTO {

  /** The Constant Bed_State_Prev_Bed. */
  public static final String Bed_State_Prev_Bed = "Finalized";

  /** The Constant Bed_State_Current_Bed. */
  public static final String Bed_State_Current_Bed = "Occupied";

  /** The Constant BED_DAY_UNITS. */
  public static final String BED_DAY_UNITS = "Days";

  /** The Constant BED_HR_UNITS. */
  public static final String BED_HR_UNITS = "Hrs";

  /** The mrno. */
  private String mrno;
  
  /** The patientid. */
  private String patientid;
  
  /** The bedname. */
  private String bedname;
  
  /** The wardname. */
  private String wardname;
  
  /** The bedtype. */
  private String bedtype;
  
  /** The admitdate. */
  private String admitdate;
  
  /** The admitid. */
  private int admitid;
  
  /** The estimateddays. */
  private float estimateddays;
  
  /** The daycare. */
  private String daycare;
  
  /** The baby. */
  private boolean baby;
  
  /** The startdate. */
  private String startdate;
  
  /** The status. */
  private String status;
  
  /** The enddate. */
  private String enddate;
  
  /** The parentid. */
  private String parentid;
  
  /** The bed id. */
  private int bedId;
  
  /** The units. */
  private String units;
  
  /** The graceperiod overrid. */
  private String graceperiodOverrid;
  
  /** The finaliseddate. */
  private Timestamp finaliseddate;
  
  /** The charge group. */
  private String chargeGroup;
  
  /** The occupancy. */
  private String occupancy;
  
  /** The last updated. */
  private Timestamp lastUpdated;
  
  /** The bed state. */
  private String bedState;
  
  /** The ward no. */
  private String wardNo;
  
  /** The bed ref id. */
  private int bedRefId;
  
  /** The finalized time. */
  private Timestamp finalizedTime;
  
  /** The duty doctor id. */
  private String dutyDoctorId;
  
  /** The is bystander. */
  private boolean isBystander;
  
  /** The bystander bed id. */
  private int bystanderBedId;

  /**
   * Checks if is checks if is bystander.
   *
   * @return true, if is checks if is bystander
   */
  public boolean isIs_bystander() {
    return isBystander;
  }

  /**
   * Sets the checks if is bystander.
   *
   * @param isBystander the new checks if is bystander
   */
  public void setIs_bystander(boolean isBystander) {
    this.isBystander = isBystander;
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
   * Gets the occupancy.
   *
   * @return the occupancy
   */
  public String getOccupancy() {
    return occupancy;
  }

  /**
   * Sets the occupancy.
   *
   * @param occupancy the new occupancy
   */
  public void setOccupancy(String occupancy) {
    this.occupancy = occupancy;
  }

  /**
   * Gets the finaliseddate.
   *
   * @return the finaliseddate
   */
  public Timestamp getFinaliseddate() {
    return finaliseddate;
  }

  /**
   * Sets the finaliseddate.
   *
   * @param finaliseddate the new finaliseddate
   */
  public void setFinaliseddate(Timestamp finaliseddate) {
    this.finaliseddate = finaliseddate;
  }

  /**
   * Gets the baby.
   *
   * @param baby the baby
   * @return the baby
   */
  public String getBaby(boolean baby) {
    if (baby) {
      return "Y";
    } else {
      return "N";
    }
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
   * Checks if is baby.
   *
   * @return true, if is baby
   */
  public boolean isBaby() {
    return baby;
  }

  /**
   * Sets the baby.
   *
   * @param baby the new baby
   */
  public void setBaby(boolean baby) {
    this.baby = baby;
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
   * Gets the estimateddays.
   *
   * @return the estimateddays
   */
  public float getEstimateddays() {
    return estimateddays;
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
   * Gets the parentid.
   *
   * @return the parentid
   */
  public String getParentid() {
    return parentid;
  }

  /**
   * Sets the parentid.
   *
   * @param parentid the new parentid
   */
  public void setParentid(String parentid) {
    this.parentid = parentid;
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
   * Gets the bed id.
   *
   * @return the bed id
   */
  public int getBed_id() {
    return bedId;
  }

  /**
   * Sets the bed id.
   *
   * @param bedId the new bed id
   */
  public void setBed_id(int bedId) {
    this.bedId = bedId;
  }

  /**
   * Gets the graceperiod overrid.
   *
   * @return the graceperiod overrid
   */
  public String getGraceperiod_overrid() {
    return graceperiodOverrid;
  }

  /**
   * Sets the graceperiod overrid.
   *
   * @param graceperiodOverrid the new graceperiod overrid
   */
  public void setGraceperiod_overrid(String graceperiodOverrid) {
    this.graceperiodOverrid = graceperiodOverrid;
  }

  /**
   * Gets the units.
   *
   * @return the units
   */
  public String getUnits() {
    return units;
  }

  /**
   * Sets the units.
   *
   * @param units the new units
   */
  public void setUnits(String units) {
    this.units = units;
  }

  /**
   * Gets the charge group.
   *
   * @return the charge group
   */
  public String getChargeGroup() {
    return chargeGroup;
  }

  /**
   * Sets the charge group.
   *
   * @param chargeGroup the new charge group
   */
  public void setChargeGroup(String chargeGroup) {
    this.chargeGroup = chargeGroup;
  }

  /**
   * Gets the bed state.
   *
   * @return the bed state
   */
  public String getBed_state() {
    return bedState;
  }

  /**
   * Sets the bed state.
   *
   * @param bedState the new bed state
   */
  public void setBed_state(String bedState) {
    this.bedState = bedState;
  }

  /**
   * Gets the ward no.
   *
   * @return the ward no
   */
  public String getWardNo() {
    return wardNo;
  }

  /**
   * Sets the ward no.
   *
   * @param wardNo the new ward no
   */
  public void setWardNo(String wardNo) {
    this.wardNo = wardNo;
  }

  /**
   * Gets the finalized time.
   *
   * @return the finalized time
   */
  public Timestamp getFinalizedTime() {
    return finalizedTime;
  }

  /**
   * Sets the finalized time.
   *
   * @param finalizedTime the new finalized time
   */
  public void setFinalizedTime(Timestamp finalizedTime) {
    this.finalizedTime = finalizedTime;
  }

  /**
   * Gets the bed ref id.
   *
   * @return the bed ref id
   */
  public int getBed_ref_id() {
    return bedRefId;
  }

  /**
   * Sets the bed ref id.
   *
   * @param bedRefId the new bed ref id
   */
  public void setBed_ref_id(int bedRefId) {
    this.bedRefId = bedRefId;
  }

  /**
   * Gets the bystander bed id.
   *
   * @return the bystander bed id
   */
  public int getBystander_bed_id() {
    return bystanderBedId;
  }

  /**
   * Sets the bystander bed id.
   *
   * @param bystanderBedId the new bystander bed id
   */
  public void setBystander_bed_id(int bystanderBedId) {
    this.bystanderBedId = bystanderBedId;
  }

}
