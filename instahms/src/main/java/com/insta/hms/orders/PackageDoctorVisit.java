package com.insta.hms.orders;

import java.sql.Timestamp;

// TODO: Auto-generated Javadoc
/**
 * The Class PackageDoctorVisit.
 */
public class PackageDoctorVisit {

  /** The v doctor. */
  private String vdoctor;

  /** The doctor head. */
  private String doctorHead;

  /** The doc visit date time. */
  private Timestamp docVisitDateTime;

  /**
   * Gets the doc visit date time.
   *
   * @return the doc visit date time
   */
  public Timestamp getDocVisitDateTime() {
    return docVisitDateTime;
  }

  /**
   * Sets the doc visit date time.
   *
   * @param docVisitDateTime the new doc visit date time
   */
  public void setDocVisitDateTime(Timestamp docVisitDateTime) {
    this.docVisitDateTime = docVisitDateTime;
  }

  /**
   * Gets the doctor head.
   *
   * @return the doctor head
   */
  public String getDoctorHead() {
    return doctorHead;
  }

  /**
   * Sets the doctor head.
   *
   * @param doctorHead the new doctor head
   */
  public void setDoctorHead(String doctorHead) {
    this.doctorHead = doctorHead;
  }

  /**
   * Gets the v doctor.
   *
   * @return the v doctor
   */
  public String getVDoctor() {
    return vdoctor;
  }

  /**
   * Sets the v doctor.
   *
   * @param doctor the new v doctor
   */
  public void setVDoctor(String doctor) {
    vdoctor = doctor;
  }
}
