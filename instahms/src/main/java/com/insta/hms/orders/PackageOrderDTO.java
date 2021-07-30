package com.insta.hms.orders;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class PackageOrderDTO.
 */
public class PackageOrderDTO {

  /** The package id. */
  private int packageId;

  /** The doctor id. */
  private String doctorId;

  /** The remarks. */
  private String remarks;

  /** The ordered time. */
  private Timestamp orderedTime;

  /** The op id. */
  private String opId;

  /** The start date time. */
  private Timestamp startDateTime;

  /** The to date time. */
  private Timestamp toDateTime;

  /** The theatre name. */
  private String theatreName;

  /** The units. */
  private String units;

  /** The surgeon. */
  private String surgeon;

  /** The anaesth. */
  private String anaesth;

  /** The status. */
  private String status;

  /** The need report. */
  private boolean needReport;

  /** The doc presc id. */
  private int docPrescId;

  /** The is standing. */
  private String isStanding;

  /** The pre auth ids. */
  private String[] preAuthIds;

  /** The pre auth mode ids. */
  private Integer[] preAuthModeIds;

  /** The pre auth id. */
  private String preAuthId;

  /** The pre auth mode id. */
  private Integer preAuthModeId;

  /** The conducting doctors. */
  private List<Map> conductingDoctors = new ArrayList<Map>();

  /** The test documents. */
  private List<TestDocumentDTO> testDocuments = new ArrayList<TestDocumentDTO>();

  /** The doctor visits. */
  private List<PackageDoctorVisit> doctorVisits = new ArrayList<PackageDoctorVisit>();

  /** The prescribedId id. */
  private int prescribedId;


  /**
   * Gets the pre auth mode ids.
   *
   * @return the pre auth mode ids
   */
  public Integer[] getPreAuthModeIds() {
    return preAuthModeIds;
  }

  /**
   * Sets the pre auth mode id.
   *
   * @param preAuthModeIds the new pre auth mode id
   */
  public void setPreAuthModeId(Integer[] preAuthModeIds) {
    this.preAuthModeIds = preAuthModeIds;
  }

  /**
   * Sets the pre auth mode id.
   *
   * @param preAuthModeId the new pre auth mode id
   */
  public void setPreAuthModeId(Integer preAuthModeId) {
    this.preAuthModeId = preAuthModeId;
  }

  /**
   * Gets the anaesth.
   *
   * @return the anaesth
   */
  public String getAnaesth() {
    return anaesth;
  }

  /**
   * Sets the anaesth.
   *
   * @param anaesth the new anaesth
   */
  public void setAnaesth(String anaesth) {
    this.anaesth = anaesth;
  }

  /**
   * Gets the doctor id.
   *
   * @return the doctor id
   */
  public String getDoctorId() {
    return doctorId;
  }

  /**
   * Sets the doctor id.
   *
   * @param doctorId the new doctor id
   */
  public void setDoctorId(String doctorId) {
    this.doctorId = doctorId;
  }

  /**
   * Gets the doctor visits.
   *
   * @return the doctor visits
   */
  public List<PackageDoctorVisit> getDoctorVisits() {
    return doctorVisits;
  }

  /**
   * Sets the doctor visits.
   *
   * @param doctorVisits the new doctor visits
   */
  public void setDoctorVisits(List<PackageDoctorVisit> doctorVisits) {
    this.doctorVisits = doctorVisits;
  }

  /**
   * Gets the conducting doctors.
   *
   * @return the conducting doctors
   */
  public List<Map> getConductingDoctors() {
    return conductingDoctors;
  }

  /**
   * Sets the conducting doctors.
   *
   * @param conductingDoctors the new conducting doctors
   */
  public void setConductingDoctors(List<Map> conductingDoctors) {
    this.conductingDoctors = conductingDoctors;
  }

  /**
   * Gets the need report.
   *
   * @return the need report
   */
  public boolean getNeedReport() {
    return needReport;
  }

  /**
   * Sets the need report.
   *
   * @param needReport the new need report
   */
  public void setNeedReport(boolean needReport) {
    this.needReport = needReport;
  }

  /**
   * Gets the op id.
   *
   * @return the op id
   */
  public String getOpId() {
    return opId;
  }

  /**
   * Sets the op id.
   *
   * @param opId the new op id
   */
  public void setOpId(String opId) {
    this.opId = opId;
  }

  /**
   * Gets the ordered time.
   *
   * @return the ordered time
   */
  public Timestamp getOrderedTime() {
    return orderedTime;
  }

  /**
   * Sets the ordered time.
   *
   * @param orderedTime the new ordered time
   */
  public void setOrderedTime(Timestamp orderedTime) {
    this.orderedTime = orderedTime;
  }

  /**
   * Gets the package id.
   *
   * @return the package id
   */
  public int getPackageId() {
    return packageId;
  }

  /**
   * Sets the package id.
   *
   * @param packageId the new package id
   */
  public void setPackageId(int packageId) {
    this.packageId = packageId;
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

  /**
   * Gets the start date time.
   *
   * @return the start date time
   */
  public Timestamp getStartDateTime() {
    return startDateTime;
  }

  /**
   * Sets the start date time.
   *
   * @param startDateTime the new start date time
   */
  public void setStartDateTime(Timestamp startDateTime) {
    this.startDateTime = startDateTime;
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
   * Gets the surgeon.
   *
   * @return the surgeon
   */
  public String getSurgeon() {
    return surgeon;
  }

  /**
   * Sets the surgeon.
   *
   * @param surgeon the new surgeon
   */
  public void setSurgeon(String surgeon) {
    this.surgeon = surgeon;
  }

  /**
   * Gets the theatre name.
   *
   * @return the theatre name
   */
  public String getTheatreName() {
    return theatreName;
  }

  /**
   * Sets the theatre name.
   *
   * @param theatreName the new theatre name
   */
  public void setTheatreName(String theatreName) {
    this.theatreName = theatreName;
  }

  /**
   * Gets the to date time.
   *
   * @return the to date time
   */
  public Timestamp getToDateTime() {
    return toDateTime;
  }

  /**
   * Sets the to time.
   *
   * @param toDateTime the new to time
   */
  public void setToTime(Timestamp toDateTime) {
    this.toDateTime = toDateTime;
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
   * Gets the doc presc id.
   *
   * @return the doc presc id
   */
  public int getDocPrescId() {
    return docPrescId;
  }

  /**
   * Sets the doc presc id.
   *
   * @param docPrescId the new doc presc id
   */
  public void setDocPrescId(int docPrescId) {
    this.docPrescId = docPrescId;
  }

  /**
   * Gets the pre auth ids.
   *
   * @return the pre auth ids
   */
  public String[] getPreAuthIds() {
    return preAuthIds;
  }

  /**
   * Sets the pre auth id.
   *
   * @param preAuthIds the new pre auth id
   */
  public void setPreAuthId(String[] preAuthIds) {
    this.preAuthIds = preAuthIds;
  }

  /**
   * Sets the pre auth id.
   *
   * @param preAuthId the new pre auth id
   */
  public void setPreAuthId(String preAuthId) {
    this.preAuthId = preAuthId;
  }

  /**
   * Gets the pre auth id.
   *
   * @return the pre auth id
   */
  public String getPreAuthId() {
    return preAuthId;
  }

  /**
   * Gets the pre auth mode id.
   *
   * @return the pre auth mode id
   */
  public Integer getPreAuthModeId() {
    return preAuthModeId;
  }

  /**
   * Gets the test documents.
   *
   * @return the test documents
   */
  public List<TestDocumentDTO> getTestDocuments() {
    return testDocuments;
  }

  /**
   * Sets the test documents.
   *
   * @param testDocuments the new test documents
   */
  public void setTestDocuments(List<TestDocumentDTO> testDocuments) {
    this.testDocuments = testDocuments;
  }

  /**
   * Gets the prescribed id.
   *
   * @return the prescribed id
   */
  public int getPrescribedId() {
    return prescribedId;
  }

  /**
   * Sets the prescribed id.
   *
   * @parampPrescribedId the new prescribedId
   */
  public void setPrescribedId(int prescribedId) {
    this.prescribedId = prescribedId;
  }
}
