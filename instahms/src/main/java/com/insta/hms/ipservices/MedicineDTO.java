package com.insta.hms.ipservices;

import java.math.BigDecimal;
import java.sql.Timestamp;

// TODO: Auto-generated Javadoc
/**
 * The Class MedicineDTO.
 */
public class MedicineDTO {
  
  /** The mrno. */
  private String mrno;
  
  /** The patientid. */
  private String patientid;
  
  /** The doctor. */
  private String doctor;
  
  /** The medicine. */
  private String medicine;
  
  /** The medicinedosage. */
  private String medicinedosage;
  
  /** The mednoofdays. */
  private String mednoofdays;
  
  /** The medquantity. */
  private String medquantity;
  
  /** The medremarks. */
  private String medremarks;
  
  /** The presdate. */
  private Timestamp presdate;
  
  /** The hoperationid. */
  private String hoperationid;
  
  /** The medicineid. */
  private String medicineid;
  
  /** The mrp. */
  private BigDecimal mrp = BigDecimal.ZERO;
  
  /** The med prescription id. */
  private int medPrescriptionId;

  /**
   * Gets the med prescription id.
   *
   * @return the med prescription id
   */
  public int getMedPrescriptionId() {
    return medPrescriptionId;
  }

  /**
   * Sets the med prescription id.
   *
   * @param medPrescriptionId the new med prescription id
   */
  public void setMedPrescriptionId(int medPrescriptionId) {
    this.medPrescriptionId = medPrescriptionId;
  }

  /**
   * Gets the mrp.
   *
   * @return the mrp
   */
  public BigDecimal getMrp() {
    return mrp;
  }

  /**
   * Sets the mrp.
   *
   * @param mrp the new mrp
   */
  public void setMrp(BigDecimal mrp) {
    this.mrp = mrp;
  }

  /**
   * Gets the medicineid.
   *
   * @return the medicineid
   */
  public String getMedicineid() {
    return medicineid;
  }

  /**
   * Sets the medicineid.
   *
   * @param medicineid the new medicineid
   */
  public void setMedicineid(String medicineid) {
    this.medicineid = medicineid;
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
   * Gets the medicine.
   *
   * @return the medicine
   */
  public String getMedicine() {
    return medicine;
  }

  /**
   * Sets the medicine.
   *
   * @param medicine the new medicine
   */
  public void setMedicine(String medicine) {
    this.medicine = medicine;
  }

  /**
   * Gets the medicinedosage.
   *
   * @return the medicinedosage
   */
  public String getMedicinedosage() {
    return medicinedosage;
  }

  /**
   * Sets the medicinedosage.
   *
   * @param medicinedosage the new medicinedosage
   */
  public void setMedicinedosage(String medicinedosage) {
    this.medicinedosage = medicinedosage;
  }

  /**
   * Gets the mednoofdays.
   *
   * @return the mednoofdays
   */
  public String getMednoofdays() {
    return mednoofdays;
  }

  /**
   * Sets the mednoofdays.
   *
   * @param mednoofdays the new mednoofdays
   */
  public void setMednoofdays(String mednoofdays) {
    this.mednoofdays = mednoofdays;
  }

  /**
   * Gets the medquantity.
   *
   * @return the medquantity
   */
  public String getMedquantity() {
    return medquantity;
  }

  /**
   * Sets the medquantity.
   *
   * @param medquantity the new medquantity
   */
  public void setMedquantity(String medquantity) {
    this.medquantity = medquantity;
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
   * Gets the medremarks.
   *
   * @return the medremarks
   */
  public String getMedremarks() {
    return medremarks;
  }

  /**
   * Sets the medremarks.
   *
   * @param medremarks the new medremarks
   */
  public void setMedremarks(String medremarks) {
    this.medremarks = medremarks;
  }

}
