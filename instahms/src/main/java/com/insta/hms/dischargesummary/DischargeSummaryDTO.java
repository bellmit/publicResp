package com.insta.hms.dischargesummary;

import org.apache.struts.upload.FormFile;

import java.sql.Date;
import java.sql.Time;

// TODO: Auto-generated Javadoc
/**
 * The Class DischargeSummaryDTO.
 */
public class DischargeSummaryDTO {

  /** The template id. */
  private String templateId;
  
  /** The template name. */
  private String templateName;
  
  /** The template content. */
  private String templateContent;

  /** The mrno. */
  private String mrno;
  
  /** The pat id. */
  private String patId;
  
  /** The format. */
  private String format;
  
  /** The docid. */
  private int docid;
  
  /** The template type. */
  private String templateType;

  /** The dis date. */
  private String disDate;
  
  /** The dis time. */
  private String disTime;
  
  /** The dis type. */
  private String disType;
  
  /** The doctor id. */
  private String doctorId;
  
  /** The user name. */
  private String userName;

  /** The death date. */
  private String deathDate;
  
  /** The death time. */
  private String deathTime;
  
  /** The death reason. */
  private String deathReason;

  /** The file. */
  private FormFile theFile;
  
  /** The contenttype. */
  private String contenttype;
  
  /** The contentfilename. */
  private String contentfilename;

  /** The follow up date. */
  private String[] followUpDate;
  
  /** The follow up doctor id. */
  private String[] followUpDoctorId;
  
  /** The follow up remarks. */
  private String[] followUpRemarks;
  
  /** The follow up id. */
  private String[] followUpId;
  
  /** The delete follow up ids. */
  private String[] deleteFollowUpIds;

  /** The finalized time. */
  private Time finalizedTime;
  
  /** The finalized date. */
  private Date finalizedDate;
  
  /** The finalized user. */
  private String finalizedUser;
  
  /** The referred to. */
  private String referredTo;

  /** The disch date for disch summary. */
  private String dischDateForDischSummary;
  
  /** The disch time for disch summary. */
  private String dischTimeForDischSummary;

  /** The signatory username. */
  private String signatoryUsername;

  /**
   * Gets the template content.
   *
   * @return the template content
   */
  public String getTemplateContent() {
    return templateContent;
  }

  /**
   * Sets the template content.
   *
   * @param templateContent the new template content
   */
  public void setTemplateContent(String templateContent) {
    this.templateContent = templateContent;
  }

  /**
   * Gets the template id.
   *
   * @return the template id
   */
  public String getTemplateId() {
    return templateId;
  }

  /**
   * Sets the template id.
   *
   * @param templateId the new template id
   */
  public void setTemplateId(String templateId) {
    this.templateId = templateId;
  }

  /**
   * Gets the template name.
   *
   * @return the template name
   */
  public String getTemplateName() {
    return templateName;
  }

  /**
   * Sets the template name.
   *
   * @param templateName the new template name
   */
  public void setTemplateName(String templateName) {
    this.templateName = templateName;
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
   * Gets the pat id.
   *
   * @return the pat id
   */
  public String getPatId() {
    return patId;
  }

  /**
   * Sets the pat id.
   *
   * @param patId the new pat id
   */
  public void setPatId(String patId) {
    this.patId = patId;
  }

  /**
   * Gets the format.
   *
   * @return the format
   */
  public String getFormat() {
    return this.format;
  }

  /**
   * Sets the format.
   *
   * @param format the new format
   */
  public void setFormat(String format) {
    this.format = format;
  }

  /**
   * Gets the docid.
   *
   * @return the docid
   */
  public int getDocid() {
    return docid;
  }

  /**
   * Sets the docid.
   *
   * @param docId the new docid
   */
  public void setDocid(int docId) {
    this.docid = docId;
  }

  /**
   * Gets the template type.
   *
   * @return the template type
   */
  public String getTemplateType() {
    return templateType;
  }

  /**
   * Sets the template type.
   *
   * @param templateType the new template type
   */
  public void setTemplateType(String templateType) {
    this.templateType = templateType;
  }

  /**
   * Gets the dis date.
   *
   * @return the dis date
   */
  public String getDisDate() {
    return disDate;
  }

  /**
   * Sets the dis date.
   *
   * @param disDate the new dis date
   */
  public void setDisDate(String disDate) {
    this.disDate = disDate;
  }

  /**
   * Gets the dis time.
   *
   * @return the dis time
   */
  public String getDisTime() {
    return disTime;
  }

  /**
   * Sets the dis time.
   *
   * @param disTime the new dis time
   */
  public void setDisTime(String disTime) {
    this.disTime = disTime;
  }

  /**
   * Gets the dis type.
   *
   * @return the dis type
   */
  public String getDisType() {
    return disType;
  }

  /**
   * Sets the dis type.
   *
   * @param disType the new dis type
   */
  public void setDisType(String disType) {
    this.disType = disType;
  }

  /**
   * Gets the death date.
   *
   * @return the death date
   */
  public String getDeathDate() {
    return deathDate;
  }

  /**
   * Sets the death date.
   *
   * @param deathDate the new death date
   */
  public void setDeathDate(String deathDate) {
    this.deathDate = deathDate;
  }

  /**
   * Gets the death time.
   *
   * @return the death time
   */
  public String getDeathTime() {
    return deathTime;
  }

  /**
   * Sets the death time.
   *
   * @param deathTime the new death time
   */
  public void setDeathTime(String deathTime) {
    this.deathTime = deathTime;
  }

  /**
   * Gets the death reason.
   *
   * @return the death reason
   */
  public String getDeathReason() {
    return deathReason;
  }

  /**
   * Sets the death reason.
   *
   * @param deathReason the new death reason
   */
  public void setDeathReason(String deathReason) {
    this.deathReason = deathReason;
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
   * Gets the the file.
   *
   * @return the the file
   */
  public FormFile getTheFile() {
    return theFile;
  }

  /**
   * Sets the the file.
   *
   * @param theFile the new the file
   */
  public void setTheFile(FormFile theFile) {
    this.theFile = theFile;
  }

  /**
   * Gets the contentfilename.
   *
   * @return the contentfilename
   */
  public String getContentfilename() {
    return contentfilename;
  }

  /**
   * Sets the contentfilename.
   *
   * @param contentfilename the new contentfilename
   */
  public void setContentfilename(String contentfilename) {
    this.contentfilename = contentfilename;
  }

  /**
   * Gets the contenttype.
   *
   * @return the contenttype
   */
  public String getContenttype() {
    return contenttype;
  }

  /**
   * Sets the contenttype.
   *
   * @param contenttype the new contenttype
   */
  public void setContenttype(String contenttype) {
    this.contenttype = contenttype;
  }

  /**
   * Gets the follow up date.
   *
   * @return the follow up date
   */
  public String[] getFollowUpDate() {
    return followUpDate;
  }

  /**
   * Sets the follow up date.
   *
   * @param followUpDate the new follow up date
   */
  public void setFollowUpDate(String[] followUpDate) {
    this.followUpDate = followUpDate;
  }

  /**
   * Gets the follow up remarks.
   *
   * @return the follow up remarks
   */
  public String[] getFollowUpRemarks() {
    return followUpRemarks;
  }

  /**
   * Sets the follow up remarks.
   *
   * @param followUpRemarks the new follow up remarks
   */
  public void setFollowUpRemarks(String[] followUpRemarks) {
    this.followUpRemarks = followUpRemarks;
  }

  /**
   * Gets the follow up id.
   *
   * @return the follow up id
   */
  public String[] getFollowUpId() {
    return followUpId;
  }

  /**
   * Sets the follow up id.
   *
   * @param followUpId the new follow up id
   */
  public void setFollowUpId(String[] followUpId) {
    this.followUpId = followUpId;
  }

  /**
   * Gets the follow up doctor id.
   *
   * @return the follow up doctor id
   */
  public String[] getFollowUpDoctorId() {
    return followUpDoctorId;
  }

  /**
   * Sets the follow up doctor id.
   *
   * @param followUpDoctorId the new follow up doctor id
   */
  public void setFollowUpDoctorId(String[] followUpDoctorId) {
    this.followUpDoctorId = followUpDoctorId;
  }

  /**
   * Gets the delete follow up ids.
   *
   * @return the delete follow up ids
   */
  public String[] getDeleteFollowUpIds() {
    return deleteFollowUpIds;
  }

  /**
   * Sets the delete follow up ids.
   *
   * @param deleteFollowUpIds the new delete follow up ids
   */
  public void setDeleteFollowUpIds(String[] deleteFollowUpIds) {
    this.deleteFollowUpIds = deleteFollowUpIds;
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
   * Gets the finalized date.
   *
   * @return the finalized date
   */
  public Date getFinalizedDate() {
    return finalizedDate;
  }

  /**
   * Sets the finalized date.
   *
   * @param finalizedDate the new finalized date
   */
  public void setFinalizedDate(Date finalizedDate) {
    this.finalizedDate = finalizedDate;
  }

  /**
   * Gets the finalized time.
   *
   * @return the finalized time
   */
  public Time getFinalizedTime() {
    return finalizedTime;
  }

  /**
   * Sets the finalized time.
   *
   * @param finalizedTime the new finalized time
   */
  public void setFinalizedTime(Time finalizedTime) {
    this.finalizedTime = finalizedTime;
  }

  /**
   * Gets the finalized user.
   *
   * @return the finalized user
   */
  public String getFinalizedUser() {
    return finalizedUser;
  }

  /**
   * Sets the finalized user.
   *
   * @param finalizedUser the new finalized user
   */
  public void setFinalizedUser(String finalizedUser) {
    this.finalizedUser = finalizedUser;
  }

  /**
   * Gets the referred to.
   *
   * @return the referred to
   */
  public String getReferredTo() {
    return referredTo;
  }

  /**
   * Sets the referred to.
   *
   * @param referredTo the new referred to
   */
  public void setReferredTo(String referredTo) {
    this.referredTo = referredTo;
  }

  /**
   * Gets the disch date for disch summary.
   *
   * @return the disch date for disch summary
   */
  public String getDisch_date_for_disch_summary() {
    return dischDateForDischSummary;
  }

  /**
   * Sets the disch date for disch summary.
   *
   * @param dischDateForDischSummary the new disch date for disch summary
   */
  public void setDisch_date_for_disch_summary(String dischDateForDischSummary) {
    this.dischDateForDischSummary = dischDateForDischSummary;
  }

  /**
   * Gets the disch time for disch summary.
   *
   * @return the disch time for disch summary
   */
  public String getDisch_time_for_disch_summary() {
    return dischTimeForDischSummary;
  }

  /**
   * Sets the disch time for disch summary.
   *
   * @param dischTimeForDischSummary the new disch time for disch summary
   */
  public void setDisch_time_for_disch_summary(String dischTimeForDischSummary) {
    this.dischTimeForDischSummary = dischTimeForDischSummary;
  }

  /**
   * Gets the signatory username.
   *
   * @return the signatory username
   */
  public String getSignatory_username() {
    return signatoryUsername;
  }

  /**
   * Sets the signatory username.
   *
   * @param signatoryUsername the new signatory username
   */
  public void setSignatory_username(String signatoryUsername) {
    this.signatoryUsername = signatoryUsername;
  }
}
