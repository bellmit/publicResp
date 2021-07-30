package com.insta.hms.ipservices;

import java.sql.Timestamp;

// TODO: Auto-generated Javadoc
/**
 * The Class DoctorVisitDTO.
 */
public class DoctorVisitDTO {
  
  /** The mrno. */
  private String mrno;
  
  /** The patientid. */
  private String patientid;
  
  /** The doctorname. */
  private String doctorname;
  
  /** The visitingdate. */
  private Timestamp visitingdate;
  
  /** The visitingtime. */
  private String visitingtime;
  
  /** The visitremarks. */
  private String visitremarks;
  
  /** The orgid. */
  private String orgid;
  
  /** The hoperationid. */
  private String hoperationid;
  
  /** The otdocrole. */
  private String otdocrole;
  
  /** The head. */
  private String head;
  
  /** The consultation token. */
  private int consultationToken;

  /**
   * Gets the consultation token.
   *
   * @return the consultation token
   */
  public int getConsultationToken() {
    return consultationToken;
  }

  /**
   * Sets the consultation token.
   *
   * @param consultationToken the new consultation token
   */
  public void setConsultationToken(int consultationToken) {
    this.consultationToken = consultationToken;
  }

  /**
   * Gets the head.
   *
   * @return the head
   */
  public String getHead() {
    return head;
  }

  /**
   * Sets the head.
   *
   * @param head the new head
   */
  public void setHead(String head) {
    this.head = head;
  }

  /**
   * Gets the otdocrole.
   *
   * @return the otdocrole
   */
  public String getOtdocrole() {
    return otdocrole;
  }

  /**
   * Sets the otdocrole.
   *
   * @param otdocrole the new otdocrole
   */
  public void setOtdocrole(String otdocrole) {
    this.otdocrole = otdocrole;
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
   * Gets the doctorname.
   *
   * @return the doctorname
   */
  public String getDoctorname() {
    return doctorname;
  }

  /**
   * Sets the doctorname.
   *
   * @param doctorname the new doctorname
   */
  public void setDoctorname(String doctorname) {
    this.doctorname = doctorname;
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
   * Gets the visitingdate.
   *
   * @return the visitingdate
   */
  public Timestamp getVisitingdate() {
    return visitingdate;
  }

  /**
   * Sets the visitingdate.
   *
   * @param visitingdate the new visitingdate
   */
  public void setVisitingdate(Timestamp visitingdate) {
    this.visitingdate = visitingdate;
  }

  /**
   * Gets the visitingtime.
   *
   * @return the visitingtime
   */
  public String getVisitingtime() {
    return visitingtime;
  }

  /**
   * Sets the visitingtime.
   *
   * @param visitingtime the new visitingtime
   */
  public void setVisitingtime(String visitingtime) {
    this.visitingtime = visitingtime;
  }

  /**
   * Gets the visitremarks.
   *
   * @return the visitremarks
   */
  public String getVisitremarks() {
    return visitremarks;
  }

  /**
   * Sets the visitremarks.
   *
   * @param visitremarks the new visitremarks
   */
  public void setVisitremarks(String visitremarks) {
    this.visitremarks = visitremarks;
  }

}
