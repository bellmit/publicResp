package com.insta.hms.ipservices;

import java.sql.Timestamp;

// TODO: Auto-generated Javadoc
/**
 * The Class PrescribedTestDTO.
 */
public class PrescribedTestDTO {
  
  /** The test dept. */
  private String testDept;
  
  /** The test id. */
  private String testId;
  
  /** The test name. */
  private String testName;
  
  /** The mrno. */
  private String mrno;
  
  /** The patientid. */
  private String patientid;
  
  /** The doctor. */
  private String doctor;
  
  /** The testremark. */
  private String testremark;
  
  /** The orgid. */
  private String orgid;
  
  /** The presdate. */
  private Timestamp presdate;
  
  /** The prestime. */
  private String prestime;
  
  /** The labno. */
  private String labno;
  
  /** The user name. */
  private String userName;
  
  /** The chargeable. */
  private boolean chargeable;
  
  /** The activity id. */
  private int activityId;
  
  /** The is sample collected. */
  private String isSampleCollected;

  /**
   * Gets the activity id.
   *
   * @return the activity id
   */
  public int getActivity_id() {
    return activityId;
  }

  /**
   * Sets the activity id.
   *
   * @param activityId the new activity id
   */
  public void setActivity_id(int activityId) {
    this.activityId = activityId;
  }

  /**
   * Gets the user name.
   *
   * @return the user name
   */
  public String getUserName() {
    return userName;
  }

  /**
   * Sets the user name.
   *
   * @param userName the new user name
   */
  public void setUserName(String userName) {
    this.userName = userName;
  }

  /**
   * Gets the labno.
   *
   * @return the labno
   */
  public String getLabno() {
    return labno;
  }

  /**
   * Sets the labno.
   *
   * @param labno the new labno
   */
  public void setLabno(String labno) {
    this.labno = labno;
  }

  /**
   * Gets the presdate.
   *
   * @return the presdate
   */
  public Timestamp getPresdate() {
    return presdate;
  }

  /**
   * Sets the presdate.
   *
   * @param presdate the new presdate
   */
  public void setPresdate(Timestamp presdate) {
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
   * Gets the testremark.
   *
   * @return the testremark
   */
  public String getTestremark() {
    return testremark;
  }

  /**
   * Sets the testremark.
   *
   * @param testremark the new testremark
   */
  public void setTestremark(String testremark) {
    this.testremark = testremark;
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
   * Gets the test dept.
   *
   * @return the test dept
   */
  public String getTestDept() {
    return testDept;
  }

  /**
   * Sets the test dept.
   *
   * @param testDept the new test dept
   */
  public void setTestDept(String testDept) {
    this.testDept = testDept;
  }

  /**
   * Gets the test id.
   *
   * @return the test id
   */
  public String getTestId() {
    return testId;
  }

  /**
   * Sets the test id.
   *
   * @param testId the new test id
   */
  public void setTestId(String testId) {
    this.testId = testId;
  }

  /**
   * Gets the test name.
   *
   * @return the test name
   */
  public String getTestName() {
    return testName;
  }

  /**
   * Sets the test name.
   *
   * @param testName the new test name
   */
  public void setTestName(String testName) {
    this.testName = testName;
  }

  /**
   * Checks if is chargeable.
   *
   * @return true, if is chargeable
   */
  public boolean isChargeable() {
    return chargeable;
  }

  /**
   * Sets the chargeable.
   *
   * @param chargeable the new chargeable
   */
  public void setChargeable(boolean chargeable) {
    this.chargeable = chargeable;
  }

  /**
   * Gets the checks if is sample collected.
   *
   * @return the checks if is sample collected
   */
  public String getIsSampleCollected() {
    return isSampleCollected;
  }

  /**
   * Sets the checks if is sample collected.
   *
   * @param isSampleCollected the new checks if is sample collected
   */
  public void setIsSampleCollected(String isSampleCollected) {
    this.isSampleCollected = isSampleCollected;
  }

}
