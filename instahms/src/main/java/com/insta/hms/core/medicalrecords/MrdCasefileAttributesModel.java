package com.insta.hms.core.medicalrecords;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * The Class MrdCasefileAttributesModel.
 */
@Entity
@Table(name = "mrd_casefile_attributes")
public class MrdCasefileAttributesModel implements java.io.Serializable {

  /** The mr no. */
  private String mrNo;

  /** The case status. */
  private Character caseStatus;

  /** The file status. */
  private Character fileStatus;

  /** The issued id. */
  private Integer issuedId;

  /** The issued to dept. */
  private String issuedToDept;

  /** The remarks. */
  private String remarks;

  /** The recreated. */
  private Boolean recreated;

  /** The created date. */
  private Date createdDate;

  /** The indented. */
  private Character indented;

  /** The requested by. */
  private String requestedBy;

  /** The request date. */
  private Date requestDate;

  /** The issued on. */
  private Date issuedOn;

  /** The indent remarks. */
  private String indentRemarks;

  /** The requesting dept. */
  private String requestingDept;

  /** The issued to user. */
  private Integer issuedToUser;

  /** The returned on. */
  private Date returnedOn;

  /**
   * Instantiates a new mrd casefile attributes model.
   */
  public MrdCasefileAttributesModel() {
  }

  /**
   * Instantiates a new mrd casefile attributes model.
   *
   * @param mrNo
   *          the mr no
   */
  public MrdCasefileAttributesModel(String mrNo) {
    this.mrNo = mrNo;
  }

  /**
   * Instantiates a new mrd casefile attributes model.
   *
   * @param mrNo
   *          the mr no
   * @param caseStatus
   *          the case status
   * @param fileStatus
   *          the file status
   * @param issuedId
   *          the issued id
   * @param issuedToDept
   *          the issued to dept
   * @param remarks
   *          the remarks
   * @param recreated
   *          the recreated
   * @param createdDate
   *          the created date
   * @param indented
   *          the indented
   * @param requestedBy
   *          the requested by
   * @param requestDate
   *          the request date
   * @param issuedOn
   *          the issued on
   * @param indentRemarks
   *          the indent remarks
   * @param requestingDept
   *          the requesting dept
   * @param issuedToUser
   *          the issued to user
   * @param returnedOn
   *          the returned on
   */
  public MrdCasefileAttributesModel(String mrNo, Character caseStatus,
      Character fileStatus, Integer issuedId, String issuedToDept,
      String remarks, Boolean recreated, Date createdDate, Character indented,
      String requestedBy, Date requestDate, Date issuedOn, String indentRemarks,
      String requestingDept, Integer issuedToUser, Date returnedOn) {
    this.mrNo = mrNo;
    this.caseStatus = caseStatus;
    this.fileStatus = fileStatus;
    this.issuedId = issuedId;
    this.issuedToDept = issuedToDept;
    this.remarks = remarks;
    this.recreated = recreated;
    this.createdDate = createdDate;
    this.indented = indented;
    this.requestedBy = requestedBy;
    this.requestDate = requestDate;
    this.issuedOn = issuedOn;
    this.indentRemarks = indentRemarks;
    this.requestingDept = requestingDept;
    this.issuedToUser = issuedToUser;
    this.returnedOn = returnedOn;
  }

  /**
   * Gets the mr no.
   *
   * @return the mr no
   */
  @Id
  @Column(name = "mr_no", unique = true, nullable = false, length = 15)
  public String getMrNo() {
    return this.mrNo;
  }

  /**
   * Sets the mr no.
   *
   * @param mrNo
   *          the new mr no
   */
  public void setMrNo(String mrNo) {
    this.mrNo = mrNo;
  }

  /**
   * Gets the case status.
   *
   * @return the case status
   */
  @Column(name = "case_status", length = 1)
  public Character getCaseStatus() {
    return this.caseStatus;
  }

  /**
   * Sets the case status.
   *
   * @param caseStatus
   *          the new case status
   */
  public void setCaseStatus(Character caseStatus) {
    this.caseStatus = caseStatus;
  }

  /**
   * Gets the file status.
   *
   * @return the file status
   */
  @Column(name = "file_status", length = 1)
  public Character getFileStatus() {
    return this.fileStatus;
  }

  /**
   * Sets the file status.
   *
   * @param fileStatus
   *          the new file status
   */
  public void setFileStatus(Character fileStatus) {
    this.fileStatus = fileStatus;
  }

  /**
   * Gets the issued id.
   *
   * @return the issued id
   */
  @Column(name = "issued_id")
  public Integer getIssuedId() {
    return this.issuedId;
  }

  /**
   * Sets the issued id.
   *
   * @param issuedId
   *          the new issued id
   */
  public void setIssuedId(Integer issuedId) {
    this.issuedId = issuedId;
  }

  /**
   * Gets the issued to dept.
   *
   * @return the issued to dept
   */
  @Column(name = "issued_to_dept")
  public String getIssuedToDept() {
    return this.issuedToDept;
  }

  /**
   * Sets the issued to dept.
   *
   * @param issuedToDept
   *          the new issued to dept
   */
  public void setIssuedToDept(String issuedToDept) {
    this.issuedToDept = issuedToDept;
  }

  /**
   * Gets the remarks.
   *
   * @return the remarks
   */
  @Column(name = "remarks")
  public String getRemarks() {
    return this.remarks;
  }

  /**
   * Sets the remarks.
   *
   * @param remarks
   *          the new remarks
   */
  public void setRemarks(String remarks) {
    this.remarks = remarks;
  }

  /**
   * Gets the recreated.
   *
   * @return the recreated
   */
  @Column(name = "recreated")
  public Boolean getRecreated() {
    return this.recreated;
  }

  /**
   * Sets the recreated.
   *
   * @param recreated
   *          the new recreated
   */
  public void setRecreated(Boolean recreated) {
    this.recreated = recreated;
  }

  /**
   * Gets the created date.
   *
   * @return the created date
   */
  @Temporal(TemporalType.DATE)
  @Column(name = "created_date", length = 13)
  public Date getCreatedDate() {
    return this.createdDate;
  }

  /**
   * Sets the created date.
   *
   * @param createdDate
   *          the new created date
   */
  public void setCreatedDate(Date createdDate) {
    this.createdDate = createdDate;
  }

  /**
   * Gets the indented.
   *
   * @return the indented
   */
  @Column(name = "indented", length = 1)
  public Character getIndented() {
    return this.indented;
  }

  /**
   * Sets the indented.
   *
   * @param indented
   *          the new indented
   */
  public void setIndented(Character indented) {
    this.indented = indented;
  }

  /**
   * Gets the requested by.
   *
   * @return the requested by
   */
  @Column(name = "requested_by", length = 50)
  public String getRequestedBy() {
    return this.requestedBy;
  }

  /**
   * Sets the requested by.
   *
   * @param requestedBy
   *          the new requested by
   */
  public void setRequestedBy(String requestedBy) {
    this.requestedBy = requestedBy;
  }

  /**
   * Gets the request date.
   *
   * @return the request date
   */
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "request_date", length = 35)
  public Date getRequestDate() {
    return this.requestDate;
  }

  /**
   * Sets the request date.
   *
   * @param requestDate
   *          the new request date
   */
  public void setRequestDate(Date requestDate) {
    this.requestDate = requestDate;
  }

  /**
   * Gets the issued on.
   *
   * @return the issued on
   */
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "issued_on", length = 29)
  public Date getIssuedOn() {
    return this.issuedOn;
  }

  /**
   * Sets the issued on.
   *
   * @param issuedOn
   *          the new issued on
   */
  public void setIssuedOn(Date issuedOn) {
    this.issuedOn = issuedOn;
  }

  /**
   * Gets the indent remarks.
   *
   * @return the indent remarks
   */
  @Column(name = "indent_remarks", length = 100)
  public String getIndentRemarks() {
    return this.indentRemarks;
  }

  /**
   * Sets the indent remarks.
   *
   * @param indentRemarks
   *          the new indent remarks
   */
  public void setIndentRemarks(String indentRemarks) {
    this.indentRemarks = indentRemarks;
  }

  /**
   * Gets the requesting dept.
   *
   * @return the requesting dept
   */
  @Column(name = "requesting_dept", length = 50)
  public String getRequestingDept() {
    return this.requestingDept;
  }

  /**
   * Sets the requesting dept.
   *
   * @param requestingDept
   *          the new requesting dept
   */
  public void setRequestingDept(String requestingDept) {
    this.requestingDept = requestingDept;
  }

  /**
   * Gets the issued to user.
   *
   * @return the issued to user
   */
  @Column(name = "issued_to_user")
  public Integer getIssuedToUser() {
    return this.issuedToUser;
  }

  /**
   * Sets the issued to user.
   *
   * @param issuedToUser
   *          the new issued to user
   */
  public void setIssuedToUser(Integer issuedToUser) {
    this.issuedToUser = issuedToUser;
  }

  /**
   * Gets the returned on.
   *
   * @return the returned on
   */
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "returned_on", length = 29)
  public Date getReturnedOn() {
    return this.returnedOn;
  }

  /**
   * Sets the returned on.
   *
   * @param returnedOn
   *          the new returned on
   */
  public void setReturnedOn(Date returnedOn) {
    this.returnedOn = returnedOn;
  }

}
