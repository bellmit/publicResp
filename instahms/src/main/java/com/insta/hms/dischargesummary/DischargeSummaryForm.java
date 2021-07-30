package com.insta.hms.dischargesummary;

import org.apache.struts.action.ActionForm;
import org.apache.struts.upload.FormFile;

// TODO: Auto-generated Javadoc
/**
 * The Class DischargeSummaryForm.
 */
public class DischargeSummaryForm extends ActionForm {

  /** The scat. */
  private String scat;
  
  /** The pat name. */
  private String patName;
  
  /** The pat age. */
  private String patAge;
  
  /** The patient id. */
  private String patientId;
  
  /** The docid. */
  private String docid;
  
  /** The pat admission date. */
  private String patAdmissionDate;
  
  /** The discharge date. */
  private String dischargeDate;
  
  /** The discharge time. */
  private String dischargeTime;
  
  /** The death date. */
  private String deathDate;
  
  /** The death time. */
  private String deathTime;
  
  /** The death reason. */
  private String deathReason;
  
  /** The procedure. */
  private String procedure;
  
  /** The findings. */
  private String findings;
  
  /** The investigation. */
  private String investigation;
  
  /** The recommendation. */
  private String recommendation;

  /** The template content. */
  private String templateContent;

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
  
  /** The follow up doctor name. */
  private String[] followUpDoctorName;
  
  /** The follow up remarks. */
  private String[] followUpRemarks;
  
  /** The follow up id. */
  private String[] followUpId;
  
  /** The delete follow up ids. */
  private String[] deleteFollowUpIds;

  /** The finalized time. */
  private String finalizedTime;
  
  /** The finalized date. */
  private String finalizedDate;
  
  /** The finalized user. */
  private String finalizedUser;
  
  /** The finalized. */
  private boolean finalized;
  
  /** The referred to. */
  private String referredTo;
  
  /** The dis type. */
  private String disType;
  
  /** The doctor id. */
  private String doctorId;
  
  /** The form id. */
  private String formId;
  
  /** The template id. */
  private String templateId;
  
  /** The disch date for disch summary. */
  private String dischDateForDischSummary;
  
  /** The disch time for disch summary. */
  private String dischTimeForDischSummary;

  /**
   * Gets the disch date for disch summary.
   *
   * @return the disch date for disch summary
   */
  public String getDischDateForDischSummary() {
    return dischDateForDischSummary;
  }

  /**
   * Sets the disch date for disch summary.
   *
   * @param dischDateForDischSummary the new disch date for disch summary
   */
  public void setDischDateForDischSummary(String dischDateForDischSummary) {
    this.dischDateForDischSummary = dischDateForDischSummary;
  }

  /**
   * Gets the disch time for disch summary.
   *
   * @return the disch time for disch summary
   */
  public String getDischTimeForDischSummary() {
    return dischTimeForDischSummary;
  }

  /**
   * Sets the disch time for disch summary.
   *
   * @param dischTimeForDischSummary the new disch time for disch summary
   */
  public void setDischTimeForDischSummary(String dischTimeForDischSummary) {
    this.dischTimeForDischSummary = dischTimeForDischSummary;
  }

  /**
   * Gets the discharge date.
   *
   * @return the discharge date
   */
  public String getDischargeDate() {
    return dischargeDate;
  }

  /**
   * Sets the discharge date.
   *
   * @param dischargeDate the new discharge date
   */
  public void setDischargeDate(String dischargeDate) {
    this.dischargeDate = dischargeDate;
  }

  /**
   * Gets the discharge time.
   *
   * @return the discharge time
   */
  public String getDischargeTime() {
    return dischargeTime;
  }

  /**
   * Sets the discharge time.
   *
   * @param dischargeTime the new discharge time
   */
  public void setDischargeTime(String dischargeTime) {
    this.dischargeTime = dischargeTime;
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
   * Gets the findings.
   *
   * @return the findings
   */
  public String getFindings() {
    return findings;
  }

  /**
   * Sets the findings.
   *
   * @param findings the new findings
   */
  public void setFindings(String findings) {
    this.findings = findings;
  }

  /**
   * Gets the investigation.
   *
   * @return the investigation
   */
  public String getInvestigation() {
    return investigation;
  }

  /**
   * Sets the investigation.
   *
   * @param investigation the new investigation
   */
  public void setInvestigation(String investigation) {
    this.investigation = investigation;
  }

  /**
   * Gets the pat admission date.
   *
   * @return the pat admission date
   */
  public String getPatAdmissionDate() {
    return patAdmissionDate;
  }

  /**
   * Sets the pat admission date.
   *
   * @param patAdmissionDate the new pat admission date
   */
  public void setPatAdmissionDate(String patAdmissionDate) {
    this.patAdmissionDate = patAdmissionDate;
  }

  /**
   * Gets the pat age.
   *
   * @return the pat age
   */
  public String getPatAge() {
    return patAge;
  }

  /**
   * Sets the pat age.
   *
   * @param patAge the new pat age
   */
  public void setPatAge(String patAge) {
    this.patAge = patAge;
  }

  /**
   * Gets the patient id.
   *
   * @return the patient id
   */
  public String getPatient_id() {
    return patientId;
  }

  /**
   * Sets the patient id.
   *
   * @param patId the new patient id
   */
  public void setPatient_id(String patId) {
    this.patientId = patId;
  }

  /**
   * Gets the docid.
   *
   * @return the docid
   */
  public String getDocid() {
    return docid;
  }

  /**
   * Sets the docid.
   *
   * @param docId the new docid
   */
  public void setDocid(String docId) {
    this.docid = docId;
  }

  /**
   * Gets the pat name.
   *
   * @return the pat name
   */
  public String getPatName() {
    return patName;
  }

  /**
   * Sets the pat name.
   *
   * @param patName the new pat name
   */
  public void setPatName(String patName) {
    this.patName = patName;
  }

  /**
   * Gets the procedure.
   *
   * @return the procedure
   */
  public String getProcedure() {
    return procedure;
  }

  /**
   * Sets the procedure.
   *
   * @param procedure the new procedure
   */
  public void setProcedure(String procedure) {
    this.procedure = procedure;
  }

  /**
   * Gets the recommendation.
   *
   * @return the recommendation
   */
  public String getRecommendation() {
    return recommendation;
  }

  /**
   * Sets the recommendation.
   *
   * @param recommendation the new recommendation
   */
  public void setRecommendation(String recommendation) {
    this.recommendation = recommendation;
  }

  /**
   * Gets the scat.
   *
   * @return the scat
   */
  public String getScat() {
    return scat;
  }

  /**
   * Sets the scat.
   *
   * @param scat the new scat
   */
  public void setScat(String scat) {
    this.scat = scat;
  }

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
   * Checks if is finalized.
   *
   * @return true, if is finalized
   */
  public boolean isFinalized() {
    return finalized;
  }

  /**
   * Sets the finalized.
   *
   * @param finalized the new finalized
   */
  public void setFinalized(boolean finalized) {
    this.finalized = finalized;
  }

  /**
   * Gets the finalized date.
   *
   * @return the finalized date
   */
  public String getFinalizedDate() {
    return finalizedDate;
  }

  /**
   * Sets the finalized date.
   *
   * @param finalizedDate the new finalized date
   */
  public void setFinalizedDate(String finalizedDate) {
    this.finalizedDate = finalizedDate;
  }

  /**
   * Gets the finalized time.
   *
   * @return the finalized time
   */
  public String getFinalizedTime() {
    return finalizedTime;
  }

  /**
   * Sets the finalized time.
   *
   * @param finalizedTime the new finalized time
   */
  public void setFinalizedTime(String finalizedTime) {
    this.finalizedTime = finalizedTime;
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
   * Gets the follow up doctor name.
   *
   * @return the follow up doctor name
   */
  public String[] getFollowUpDoctorName() {
    return followUpDoctorName;
  }

  /**
   * Sets the follow up doctor name.
   *
   * @param followUpDoctorName the new follow up doctor name
   */
  public void setFollowUpDoctorName(String[] followUpDoctorName) {
    this.followUpDoctorName = followUpDoctorName;
  }

  /**
   * Gets the form id.
   *
   * @return the form id
   */
  public String getFormId() {
    return formId;
  }

  /**
   * Sets the form id.
   *
   * @param formId the new form id
   */
  public void setFormId(String formId) {
    this.formId = formId;
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
}
