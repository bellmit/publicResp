package com.insta.hms.followupdashboard;

/**
 * The Class FollowUpDashboardSearch.
 */
public class FollowUpDashboardSearch {

  /**
   * The mrno.
   */
  private String mrno;

  /**
   * The patient name.
   */
  private String patientName;

  /**
   * The phone.
   */
  private String phone;

  /**
   * The follow up doc id.
   */
  private String followUpDocId;

  /**
   * The follow up doc name.
   */
  private String followUpDocName;

  /**
   * The follow up dept id.
   */
  private String followUpDeptId;

  /**
   * The follow up dept name.
   */
  private String followUpDeptName;

  /**
   * The follow up remarks.
   */
  private String followUpRemarks;

  /**
   * The follow up date.
   */
  private java.sql.Date followUpDate;

  /**
   * Gets the follow up date.
   *
   * @return the follow up date
   */
  public java.sql.Date getFollowUpDate() {
    return followUpDate;
  }

  /**
   * Sets the follow up date.
   *
   * @param followUpDate the new follow up date
   */
  public void setFollowUpDate(java.sql.Date followUpDate) {
    this.followUpDate = followUpDate;
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
   * Gets the phone.
   *
   * @return the phone
   */
  public String getPhone() {
    return phone;
  }

  /**
   * Sets the phone.
   *
   * @param phone the new phone
   */
  public void setPhone(String phone) {
    this.phone = phone;
  }

  /**
   * Gets the follow up dept id.
   *
   * @return the follow up dept id
   */
  public String getFollowUpDeptId() {
    return followUpDeptId;
  }

  /**
   * Sets the follow up dept id.
   *
   * @param followUpDeptId the new follow up dept id
   */
  public void setFollowUpDeptId(String followUpDeptId) {
    this.followUpDeptId = followUpDeptId;
  }

  /**
   * Gets the follow up dept name.
   *
   * @return the follow up dept name
   */
  public String getFollowUpDeptName() {
    return followUpDeptName;
  }

  /**
   * Sets the follow up dept name.
   *
   * @param followUpDeptName the new follow up dept name
   */
  public void setFollowUpDeptName(String followUpDeptName) {
    this.followUpDeptName = followUpDeptName;
  }

  /**
   * Gets the follow up doc id.
   *
   * @return the follow up doc id
   */
  public String getFollowUpDocId() {
    return followUpDocId;
  }

  /**
   * Sets the follow up doc id.
   *
   * @param followUpDocId the new follow up doc id
   */
  public void setFollowUpDocId(String followUpDocId) {
    this.followUpDocId = followUpDocId;
  }

  /**
   * Gets the follow up doc name.
   *
   * @return the follow up doc name
   */
  public String getFollowUpDocName() {
    return followUpDocName;
  }

  /**
   * Sets the follow up doc name.
   *
   * @param followUpDocName the new follow up doc name
   */
  public void setFollowUpDocName(String followUpDocName) {
    this.followUpDocName = followUpDocName;
  }

  /**
   * Gets the follow up remarks.
   *
   * @return the follow up remarks
   */
  public String getFollowUpRemarks() {
    return followUpRemarks;
  }

  /**
   * Sets the follow up remarks.
   *
   * @param followUpRemarks the new follow up remarks
   */
  public void setFollowUpRemarks(String followUpRemarks) {
    this.followUpRemarks = followUpRemarks;
  }
}
