package com.insta.hms.core.medicalrecords;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * The Class MrdCasefileIssuelogModel.
 */
@Entity
@Table(name = "mrd_casefile_issuelog")
public class MrdCasefileIssuelogModel implements java.io.Serializable {

  /** The issue id. */
  private int issueId;

  /** The mr no. */
  private String mrNo;

  /** The issued on. */
  private Date issuedOn;

  /** The issued to dept. */
  private String issuedToDept;

  /** The purpose. */
  private String purpose;

  /** The issue user. */
  private String issueUser;

  /** The returned on. */
  private Date returnedOn;

  /** The return user. */
  private String returnUser;

  /** The issued to user. */
  private Integer issuedToUser;

  /**
   * Instantiates a new mrd casefile issuelog model.
   */
  public MrdCasefileIssuelogModel() {
  }

  /**
   * Instantiates a new mrd casefile issuelog model.
   *
   * @param issueId
   *          the issue id
   * @param mrNo
   *          the mr no
   */
  public MrdCasefileIssuelogModel(int issueId, String mrNo) {
    this.issueId = issueId;
    this.mrNo = mrNo;
  }

  /**
   * Instantiates a new mrd casefile issuelog model.
   *
   * @param issueId
   *          the issue id
   * @param mrNo
   *          the mr no
   * @param issuedOn
   *          the issued on
   * @param issuedToDept
   *          the issued to dept
   * @param purpose
   *          the purpose
   * @param issueUser
   *          the issue user
   * @param returnedOn
   *          the returned on
   * @param returnUser
   *          the return user
   * @param issuedToUser
   *          the issued to user
   */
  public MrdCasefileIssuelogModel(int issueId, String mrNo, Date issuedOn,
      String issuedToDept, String purpose, String issueUser, Date returnedOn,
      String returnUser, Integer issuedToUser) {
    this.issueId = issueId;
    this.mrNo = mrNo;
    this.issuedOn = issuedOn;
    this.issuedToDept = issuedToDept;
    this.purpose = purpose;
    this.issueUser = issueUser;
    this.returnedOn = returnedOn;
    this.returnUser = returnUser;
    this.issuedToUser = issuedToUser;
  }

  /**
   * Gets the issue id.
   *
   * @return the issue id
   */
  @Id
  @Column(name = "issue_id", unique = true, nullable = false)
  public int getIssueId() {
    return this.issueId;
  }

  /**
   * Sets the issue id.
   *
   * @param issueId
   *          the new issue id
   */
  public void setIssueId(int issueId) {
    this.issueId = issueId;
  }

  /**
   * Gets the mr no.
   *
   * @return the mr no
   */
  @Column(name = "mr_no", nullable = false, length = 15)
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
   * Gets the purpose.
   *
   * @return the purpose
   */
  @Column(name = "purpose")
  public String getPurpose() {
    return this.purpose;
  }

  /**
   * Sets the purpose.
   *
   * @param purpose
   *          the new purpose
   */
  public void setPurpose(String purpose) {
    this.purpose = purpose;
  }

  /**
   * Gets the issue user.
   *
   * @return the issue user
   */
  @Column(name = "issue_user")
  public String getIssueUser() {
    return this.issueUser;
  }

  /**
   * Sets the issue user.
   *
   * @param issueUser
   *          the new issue user
   */
  public void setIssueUser(String issueUser) {
    this.issueUser = issueUser;
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

  /**
   * Gets the return user.
   *
   * @return the return user
   */
  @Column(name = "return_user")
  public String getReturnUser() {
    return this.returnUser;
  }

  /**
   * Sets the return user.
   *
   * @param returnUser
   *          the new return user
   */
  public void setReturnUser(String returnUser) {
    this.returnUser = returnUser;
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

}
