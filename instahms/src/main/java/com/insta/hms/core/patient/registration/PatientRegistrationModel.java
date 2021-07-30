package com.insta.hms.core.patient.registration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.insta.hms.core.billing.BillModel;
import com.insta.hms.core.patient.PatientDetailsModel;
import com.insta.hms.mdm.centers.HospitalCenterMasterModel;
import com.insta.hms.mdm.departments.DepartmentModel;
import com.insta.hms.mdm.doctors.DoctorsModel;
import com.insta.hms.mdm.insurancecompanies.InsuranceCompanyMasterModel;
import com.insta.hms.mdm.referraldoctors.ReferralModel;
import com.insta.hms.mdm.tpas.TpaMasterModel;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * PatientRegistrationModel
 */
@Entity
@Table(name = "patient_registration")
public class PatientRegistrationModel implements java.io.Serializable {

  private String patientId;
  private Date regDate;
  private Date regTime;
  private String status;
  private BigDecimal cflag;
  private Character visitType;
  private Character revisit;
  private char readyToDischarge;
  private TpaMasterModel primarySponsorId;
  private Integer insuranceId;
  private BigDecimal primaryInsuranceApproval;
  private String patientCareOftext;
  private String patientCareofAddress;
  private String relation;
  private String originalVisitId;
  private Integer docId;
  private String regChargeAccepted;
  private String referenceDoctoId;
  private String bedType;
  private String wardId;
  private String mlcStatus;
  private DepartmentModel deptName;
  private String orgId;
  private Date dischargeDate;
  private Date dischargeTime;
  private String dischargeFlag;
  private String dischargeDoctorId;
  private String obsoleteDischargeType;
  private Character dischargeFormat;
  private DoctorsModel doctor;
  private String wardName;
  private Integer dischargeDocId;
  private String dischargeFinalizedUser;
  private Date dischargeFinalizedDate;
  private Date dischargeFinalizedTime;
  private Integer unitId;
  private String referredTo;
  private Integer patientCategoryId;
  private String userName;
  private String primaryInsuranceCo;
  private int planId;
  private Character codificationStatus;
  private String mlcNo;
  private String mlcType;
  private String accidentPlace;
  private String policeStn;
  private String mlcRemarks;
  private String certificateStatus;
  private String admittedDept;
  private Integer categoryId;
  private char opType;
  private String mainVisitId;
  private Integer encounterType;
  private Integer encounterStartType;
  private Integer encounterEndType;
  private String analysisOfComplaint;
  private String establishedType;
  private String priorAuthId;
  private String codifiedBy;
  private String childBirthRemarks;
  private Integer patientPolicyId;
  private Integer priorAuthModeId;
  private String codificationRemarks;
  private HospitalCenterMasterModel centerId;
  private Date modTime;
  private String dischargedBy;
  private Date dischDateForDischSummary;
  private Date dischTimeForDischSummary;
  private String useDrg;
  private String drgCode;
  private int docsDownloadPasscode;
  private TpaMasterModel secondarySponsorId;
  private String secondaryInsuranceCo;
  private BigDecimal secondaryInsuranceApproval;
  private Integer patientCorporateId;
  private Integer patientNationalSponsorId;
  private String signatoryUsername;
  private Integer secondaryPatientCorporateId;
  private Integer secondaryPatientNationalSponsorId;
  private String visitCustomField1;
  private String visitCustomField2;
  private String visitCustomField3;
  private String visitCustomList1;
  private String visitCustomList2;
  private int collectionCenterId;
  private Integer dischargeTypeId;
  private String complaint;
  private String transferSource;
  private String transferDestination;
  private char usePerdiem;
  private String perDiemCode;
  private Date visitCustomField4;
  private Date visitCustomField5;
  private Date visitCustomField6;
  private BigDecimal visitCustomField7;
  private BigDecimal visitCustomField8;
  private BigDecimal visitCustomField9;
  private String dischargeRemarks;
  private String patientCareOftextCountryCode;
  private Character patientDischargeStatus;
  @JsonIgnore
  private Set<BillModel> bills;
  @JsonIgnore
  private String mrNo;
  private ReferralModel referralDoctorReferral;
  private DoctorsModel referralDoctorDoctors;
  private InsuranceCompanyMasterModel primaryInsuranceCoModel;
  private InsuranceCompanyMasterModel secondaryInsuranceCoModel;
  private PatientDetailsModel patientDetails;

  public PatientRegistrationModel() {
  }

  public PatientRegistrationModel(String patientId) {
    this.patientId = patientId;
  }

  @Id

  @Column(name = "patient_id", unique = true, nullable = false, length = 15)
  public String getPatientId() {
    return this.patientId;
  }

  public void setPatientId(String patientId) {
    this.patientId = patientId;
  }

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "mr_no")
  public PatientDetailsModel getPatientDetails() {
    return this.patientDetails;
  }

  public void setPatientDetails(PatientDetailsModel patientDetails) {
    this.patientDetails = patientDetails;
  }

  @Column(name = "mr_no", nullable = false, insertable = false, updatable = false)
  public String getMrNo() {
    return this.mrNo;
  }

  public void setMrNo(String mrNo) {
    this.mrNo = mrNo;
  }
  
  @Temporal(TemporalType.DATE)
  @Column(name = "reg_date", nullable = false, length = 13)
  public Date getRegDate() {
    return this.regDate;
  }

  public void setRegDate(Date regDate) {
    this.regDate = regDate;
  }

  @Temporal(TemporalType.TIME)
  @Column(name = "reg_time", nullable = false, length = 15)
  public Date getRegTime() {
    return this.regTime;
  }

  public void setRegTime(Date regTime) {
    this.regTime = regTime;
  }

  @Column(name = "status", nullable = false, length = 15)
  public String getStatus() {
    return this.status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  @Column(name = "cflag", precision = 131089, scale = 0)
  public BigDecimal getCflag() {
    return this.cflag;
  }

  public void setCflag(BigDecimal cflag) {
    this.cflag = cflag;
  }

  @Column(name = "visit_type", length = 1)
  public Character getVisitType() {
    return this.visitType;
  }

  public void setVisitType(Character visitType) {
    this.visitType = visitType;
  }

  @Column(name = "revisit", length = 1)
  public Character getRevisit() {
    return this.revisit;
  }

  public void setRevisit(Character revisit) {
    this.revisit = revisit;
  }

  @Column(name = "ready_to_discharge", nullable = false, length = 1)
  public char getReadyToDischarge() {
    return this.readyToDischarge;
  }

  public void setReadyToDischarge(char readyToDischarge) {
    this.readyToDischarge = readyToDischarge;
  }

  @NotFound(action = NotFoundAction.IGNORE)
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "primary_sponsor_id", insertable = false, updatable = false)
  public TpaMasterModel getPrimarySponsorId() {
    return this.primarySponsorId;
  }

  public void setPrimarySponsorId(TpaMasterModel primarySponsorId) {
    this.primarySponsorId = primarySponsorId;
  }

  @Column(name = "insurance_id")
  public Integer getInsuranceId() {
    return this.insuranceId;
  }

  public void setInsuranceId(Integer insuranceId) {
    this.insuranceId = insuranceId;
  }

  @Column(name = "primary_insurance_approval", precision = 15)
  public BigDecimal getPrimaryInsuranceApproval() {
    return this.primaryInsuranceApproval;
  }

  public void setPrimaryInsuranceApproval(BigDecimal primaryInsuranceApproval) {
    this.primaryInsuranceApproval = primaryInsuranceApproval;
  }

  @Column(name = "patient_care_oftext", length = 50)
  public String getPatientCareOftext() {
    return this.patientCareOftext;
  }

  public void setPatientCareOftext(String patientCareOftext) {
    this.patientCareOftext = patientCareOftext;
  }

  @Column(name = "patient_careof_address", length = 250)
  public String getPatientCareofAddress() {
    return this.patientCareofAddress;
  }

  public void setPatientCareofAddress(String patientCareofAddress) {
    this.patientCareofAddress = patientCareofAddress;
  }

  @Column(name = "relation", length = 100)
  public String getRelation() {
    return this.relation;
  }

  public void setRelation(String relation) {
    this.relation = relation;
  }

  @Column(name = "original_visit_id", length = 15)
  public String getOriginalVisitId() {
    return this.originalVisitId;
  }

  public void setOriginalVisitId(String originalVisitId) {
    this.originalVisitId = originalVisitId;
  }

  @Column(name = "doc_id")
  public Integer getDocId() {
    return this.docId;
  }

  public void setDocId(Integer docId) {
    this.docId = docId;
  }

  @Column(name = "reg_charge_accepted", length = 1)
  public String getRegChargeAccepted() {
    return this.regChargeAccepted;
  }

  public void setRegChargeAccepted(String regChargeAccepted) {
    this.regChargeAccepted = regChargeAccepted;
  }

  @Column(name = "reference_docto_id", length = 10)
  public String getReferenceDoctoId() {
    return this.referenceDoctoId;
  }

  public void setReferenceDoctoId(String referenceDoctoId) {
    this.referenceDoctoId = referenceDoctoId;
  }

  @Column(name = "bed_type", nullable = false, length = 50)
  public String getBedType() {
    return this.bedType;
  }

  public void setBedType(String bedType) {
    this.bedType = bedType;
  }

  @Column(name = "ward_id", length = 15)
  public String getWardId() {
    return this.wardId;
  }

  public void setWardId(String wardId) {
    this.wardId = wardId;
  }

  @Column(name = "mlc_status", length = 3)
  public String getMlcStatus() {
    return this.mlcStatus;
  }

  public void setMlcStatus(String mlcStatus) {
    this.mlcStatus = mlcStatus;
  }

  @NotFound(action = NotFoundAction.IGNORE)
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "dept_name", insertable = false, updatable = false)
  public DepartmentModel getDeptName() {
    return this.deptName;
  }

  public void setDeptName(DepartmentModel deptName) {
    this.deptName = deptName;
  }

  @Column(name = "org_id", nullable = false, length = 10)
  public String getOrgId() {
    return this.orgId;
  }

  public void setOrgId(String orgId) {
    this.orgId = orgId;
  }

  @Temporal(TemporalType.DATE)
  @Column(name = "discharge_date", length = 13)
  public Date getDischargeDate() {
    return this.dischargeDate;
  }

  public void setDischargeDate(Date dischargeDate) {
    this.dischargeDate = dischargeDate;
  }

  @Temporal(TemporalType.TIME)
  @Column(name = "discharge_time", length = 15)
  public Date getDischargeTime() {
    return this.dischargeTime;
  }

  public void setDischargeTime(Date dischargeTime) {
    this.dischargeTime = dischargeTime;
  }

  @Column(name = "discharge_flag", length = 15)
  public String getDischargeFlag() {
    return this.dischargeFlag;
  }

  public void setDischargeFlag(String dischargeFlag) {
    this.dischargeFlag = dischargeFlag;
  }

  @Column(name = "discharge_doctor_id", length = 20)
  public String getDischargeDoctorId() {
    return this.dischargeDoctorId;
  }

  public void setDischargeDoctorId(String dischargeDoctorId) {
    this.dischargeDoctorId = dischargeDoctorId;
  }

  @Column(name = "obsolete_discharge_type", length = 50)
  public String getObsoleteDischargeType() {
    return this.obsoleteDischargeType;
  }

  public void setObsoleteDischargeType(String obsoleteDischargeType) {
    this.obsoleteDischargeType = obsoleteDischargeType;
  }

  @Column(name = "discharge_format", length = 1)
  public Character getDischargeFormat() {
    return this.dischargeFormat;
  }

  public void setDischargeFormat(Character dischargeFormat) {
    this.dischargeFormat = dischargeFormat;
  }

  @NotFound(action = NotFoundAction.IGNORE)
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "doctor", insertable = false, updatable = false)
  public DoctorsModel getDoctor() {
    return this.doctor;
  }

  public void setDoctor(DoctorsModel doctor) {
    this.doctor = doctor;
  }

  @Column(name = "ward_name", length = 50)
  public String getWardName() {
    return this.wardName;
  }

  public void setWardName(String wardName) {
    this.wardName = wardName;
  }

  @Column(name = "discharge_doc_id")
  public Integer getDischargeDocId() {
    return this.dischargeDocId;
  }

  public void setDischargeDocId(Integer dischargeDocId) {
    this.dischargeDocId = dischargeDocId;
  }

  @Column(name = "discharge_finalized_user", length = 50)
  public String getDischargeFinalizedUser() {
    return this.dischargeFinalizedUser;
  }

  public void setDischargeFinalizedUser(String dischargeFinalizedUser) {
    this.dischargeFinalizedUser = dischargeFinalizedUser;
  }

  @Temporal(TemporalType.DATE)
  @Column(name = "discharge_finalized_date", length = 13)
  public Date getDischargeFinalizedDate() {
    return this.dischargeFinalizedDate;
  }

  public void setDischargeFinalizedDate(Date dischargeFinalizedDate) {
    this.dischargeFinalizedDate = dischargeFinalizedDate;
  }

  @Temporal(TemporalType.TIME)
  @Column(name = "discharge_finalized_time", length = 15)
  public Date getDischargeFinalizedTime() {
    return this.dischargeFinalizedTime;
  }

  public void setDischargeFinalizedTime(Date dischargeFinalizedTime) {
    this.dischargeFinalizedTime = dischargeFinalizedTime;
  }

  @Column(name = "unit_id")
  public Integer getUnitId() {
    return this.unitId;
  }

  public void setUnitId(Integer unitId) {
    this.unitId = unitId;
  }

  @Column(name = "referred_to", length = 100)
  public String getReferredTo() {
    return this.referredTo;
  }

  public void setReferredTo(String referredTo) {
    this.referredTo = referredTo;
  }

  @Column(name = "patient_category_id")
  public Integer getPatientCategoryId() {
    return this.patientCategoryId;
  }

  public void setPatientCategoryId(Integer patientCategoryId) {
    this.patientCategoryId = patientCategoryId;
  }

  @Column(name = "user_name", length = 30)
  public String getUserName() {
    return this.userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  @Column(name = "primary_insurance_co", length = 15)
  public String getPrimaryInsuranceCo() {
    return this.primaryInsuranceCo;
  }

  public void setPrimaryInsuranceCo(String primaryInsuranceCo) {
    this.primaryInsuranceCo = primaryInsuranceCo;
  }

  @Column(name = "plan_id", nullable = false)
  public int getPlanId() {
    return this.planId;
  }

  public void setPlanId(int planId) {
    this.planId = planId;
  }

  @Column(name = "codification_status", length = 1)
  public Character getCodificationStatus() {
    return this.codificationStatus;
  }

  public void setCodificationStatus(Character codificationStatus) {
    this.codificationStatus = codificationStatus;
  }

  @Column(name = "mlc_no", length = 15)
  public String getMlcNo() {
    return this.mlcNo;
  }

  public void setMlcNo(String mlcNo) {
    this.mlcNo = mlcNo;
  }

  @Column(name = "mlc_type")
  public String getMlcType() {
    return this.mlcType;
  }

  public void setMlcType(String mlcType) {
    this.mlcType = mlcType;
  }

  @Column(name = "accident_place")
  public String getAccidentPlace() {
    return this.accidentPlace;
  }

  public void setAccidentPlace(String accidentPlace) {
    this.accidentPlace = accidentPlace;
  }

  @Column(name = "police_stn")
  public String getPoliceStn() {
    return this.policeStn;
  }

  public void setPoliceStn(String policeStn) {
    this.policeStn = policeStn;
  }

  @Column(name = "mlc_remarks")
  public String getMlcRemarks() {
    return this.mlcRemarks;
  }

  public void setMlcRemarks(String mlcRemarks) {
    this.mlcRemarks = mlcRemarks;
  }

  @Column(name = "certificate_status")
  public String getCertificateStatus() {
    return this.certificateStatus;
  }

  public void setCertificateStatus(String certificateStatus) {
    this.certificateStatus = certificateStatus;
  }

  @Column(name = "admitted_dept", length = 100)
  public String getAdmittedDept() {
    return this.admittedDept;
  }

  public void setAdmittedDept(String admittedDept) {
    this.admittedDept = admittedDept;
  }

  @Column(name = "category_id")
  public Integer getCategoryId() {
    return this.categoryId;
  }

  public void setCategoryId(Integer categoryId) {
    this.categoryId = categoryId;
  }

  @Column(name = "op_type", nullable = false, length = 1)
  public char getOpType() {
    return this.opType;
  }

  public void setOpType(char opType) {
    this.opType = opType;
  }

  @Column(name = "main_visit_id", length = 15)
  public String getMainVisitId() {
    return this.mainVisitId;
  }

  public void setMainVisitId(String mainVisitId) {
    this.mainVisitId = mainVisitId;
  }

  @Column(name = "encounter_type")
  public Integer getEncounterType() {
    return this.encounterType;
  }

  public void setEncounterType(Integer encounterType) {
    this.encounterType = encounterType;
  }

  @Column(name = "encounter_start_type")
  public Integer getEncounterStartType() {
    return this.encounterStartType;
  }

  public void setEncounterStartType(Integer encounterStartType) {
    this.encounterStartType = encounterStartType;
  }

  @Column(name = "encounter_end_type")
  public Integer getEncounterEndType() {
    return this.encounterEndType;
  }

  public void setEncounterEndType(Integer encounterEndType) {
    this.encounterEndType = encounterEndType;
  }

  @Column(name = "analysis_of_complaint", nullable = false, length = 2000)
  public String getAnalysisOfComplaint() {
    return this.analysisOfComplaint;
  }

  public void setAnalysisOfComplaint(String analysisOfComplaint) {
    this.analysisOfComplaint = analysisOfComplaint;
  }

  @Column(name = "established_type", nullable = false, length = 1)
  public String getEstablishedType() {
    return this.establishedType;
  }

  public void setEstablishedType(String establishedType) {
    this.establishedType = establishedType;
  }

  @Column(name = "prior_auth_id", length = 25)
  public String getPriorAuthId() {
    return this.priorAuthId;
  }

  public void setPriorAuthId(String priorAuthId) {
    this.priorAuthId = priorAuthId;
  }

  @Column(name = "codified_by", length = 30)
  public String getCodifiedBy() {
    return this.codifiedBy;
  }

  public void setCodifiedBy(String codifiedBy) {
    this.codifiedBy = codifiedBy;
  }

  @Column(name = "child_birth_remarks")
  public String getChildBirthRemarks() {
    return this.childBirthRemarks;
  }

  public void setChildBirthRemarks(String childBirthRemarks) {
    this.childBirthRemarks = childBirthRemarks;
  }

  @Column(name = "patient_policy_id")
  public Integer getPatientPolicyId() {
    return this.patientPolicyId;
  }

  public void setPatientPolicyId(Integer patientPolicyId) {
    this.patientPolicyId = patientPolicyId;
  }

  @Column(name = "prior_auth_mode_id")
  public Integer getPriorAuthModeId() {
    return this.priorAuthModeId;
  }

  public void setPriorAuthModeId(Integer priorAuthModeId) {
    this.priorAuthModeId = priorAuthModeId;
  }

  @Column(name = "codification_remarks")
  public String getCodificationRemarks() {
    return this.codificationRemarks;
  }

  public void setCodificationRemarks(String codificationRemarks) {
    this.codificationRemarks = codificationRemarks;
  }

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "center_id", nullable = false, insertable = false, updatable = false)
  public HospitalCenterMasterModel getCenterId() {
    return this.centerId;
  }

  public void setCenterId(HospitalCenterMasterModel centerId) {
    this.centerId = centerId;
  }

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "mod_time", length = 29)
  public Date getModTime() {
    return this.modTime;
  }

  public void setModTime(Date modTime) {
    this.modTime = modTime;
  }

  @Column(name = "discharged_by", length = 100)
  public String getDischargedBy() {
    return this.dischargedBy;
  }

  public void setDischargedBy(String dischargedBy) {
    this.dischargedBy = dischargedBy;
  }

  @Temporal(TemporalType.DATE)
  @Column(name = "disch_date_for_disch_summary", length = 13)
  public Date getDischDateForDischSummary() {
    return this.dischDateForDischSummary;
  }

  public void setDischDateForDischSummary(Date dischDateForDischSummary) {
    this.dischDateForDischSummary = dischDateForDischSummary;
  }

  @Temporal(TemporalType.TIME)
  @Column(name = "disch_time_for_disch_summary", length = 15)
  public Date getDischTimeForDischSummary() {
    return this.dischTimeForDischSummary;
  }

  public void setDischTimeForDischSummary(Date dischTimeForDischSummary) {
    this.dischTimeForDischSummary = dischTimeForDischSummary;
  }

  @Column(name = "use_drg", nullable = false, length = 1)
  public String getUseDrg() {
    return this.useDrg;
  }

  public void setUseDrg(String useDrg) {
    this.useDrg = useDrg;
  }

  @Column(name = "drg_code", length = 15)
  public String getDrgCode() {
    return this.drgCode;
  }

  public void setDrgCode(String drgCode) {
    this.drgCode = drgCode;
  }

  @Column(name = "docs_download_passcode", nullable = false)
  public int getDocsDownloadPasscode() {
    return this.docsDownloadPasscode;
  }

  public void setDocsDownloadPasscode(int docsDownloadPasscode) {
    this.docsDownloadPasscode = docsDownloadPasscode;
  }

  @NotFound(action = NotFoundAction.IGNORE)
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "secondary_sponsor_id", insertable = false, updatable = false)
  public TpaMasterModel getSecondarySponsorId() {
    return this.secondarySponsorId;
  }

  public void setSecondarySponsorId(TpaMasterModel secondarySponsorId) {
    this.secondarySponsorId = secondarySponsorId;
  }

  @Column(name = "secondary_insurance_co", length = 15)
  public String getSecondaryInsuranceCo() {
    return this.secondaryInsuranceCo;
  }

  public void setSecondaryInsuranceCo(String secondaryInsuranceCo) {
    this.secondaryInsuranceCo = secondaryInsuranceCo;
  }

  @Column(name = "secondary_insurance_approval", precision = 15)
  public BigDecimal getSecondaryInsuranceApproval() {
    return this.secondaryInsuranceApproval;
  }

  public void setSecondaryInsuranceApproval(BigDecimal secondaryInsuranceApproval) {
    this.secondaryInsuranceApproval = secondaryInsuranceApproval;
  }

  @Column(name = "patient_corporate_id")
  public Integer getPatientCorporateId() {
    return this.patientCorporateId;
  }

  public void setPatientCorporateId(Integer patientCorporateId) {
    this.patientCorporateId = patientCorporateId;
  }

  @Column(name = "patient_national_sponsor_id")
  public Integer getPatientNationalSponsorId() {
    return this.patientNationalSponsorId;
  }

  public void setPatientNationalSponsorId(Integer patientNationalSponsorId) {
    this.patientNationalSponsorId = patientNationalSponsorId;
  }

  @Column(name = "signatory_username", length = 30)
  public String getSignatoryUsername() {
    return this.signatoryUsername;
  }

  public void setSignatoryUsername(String signatoryUsername) {
    this.signatoryUsername = signatoryUsername;
  }

  @Column(name = "secondary_patient_corporate_id")
  public Integer getSecondaryPatientCorporateId() {
    return this.secondaryPatientCorporateId;
  }

  public void setSecondaryPatientCorporateId(Integer secondaryPatientCorporateId) {
    this.secondaryPatientCorporateId = secondaryPatientCorporateId;
  }

  @Column(name = "secondary_patient_national_sponsor_id")
  public Integer getSecondaryPatientNationalSponsorId() {
    return this.secondaryPatientNationalSponsorId;
  }

  public void setSecondaryPatientNationalSponsorId(Integer secondaryPatientNationalSponsorId) {
    this.secondaryPatientNationalSponsorId = secondaryPatientNationalSponsorId;
  }

  @Column(name = "visit_custom_field1")
  public String getVisitCustomField1() {
    return this.visitCustomField1;
  }

  public void setVisitCustomField1(String visitCustomField1) {
    this.visitCustomField1 = visitCustomField1;
  }

  @Column(name = "visit_custom_field2")
  public String getVisitCustomField2() {
    return this.visitCustomField2;
  }

  public void setVisitCustomField2(String visitCustomField2) {
    this.visitCustomField2 = visitCustomField2;
  }

  @Column(name = "visit_custom_field3")
  public String getVisitCustomField3() {
    return this.visitCustomField3;
  }

  public void setVisitCustomField3(String visitCustomField3) {
    this.visitCustomField3 = visitCustomField3;
  }

  @Column(name = "visit_custom_list1")
  public String getVisitCustomList1() {
    return this.visitCustomList1;
  }

  public void setVisitCustomList1(String visitCustomList1) {
    this.visitCustomList1 = visitCustomList1;
  }

  @Column(name = "visit_custom_list2")
  public String getVisitCustomList2() {
    return this.visitCustomList2;
  }

  public void setVisitCustomList2(String visitCustomList2) {
    this.visitCustomList2 = visitCustomList2;
  }

  @Column(name = "collection_center_id", nullable = false)
  public int getCollectionCenterId() {
    return this.collectionCenterId;
  }

  public void setCollectionCenterId(int collectionCenterId) {
    this.collectionCenterId = collectionCenterId;
  }

  @Column(name = "discharge_type_id")
  public Integer getDischargeTypeId() {
    return this.dischargeTypeId;
  }

  public void setDischargeTypeId(Integer dischargeTypeId) {
    this.dischargeTypeId = dischargeTypeId;
  }

  @Column(name = "complaint")
  public String getComplaint() {
    return this.complaint;
  }

  public void setComplaint(String complaint) {
    this.complaint = complaint;
  }

  @Column(name = "transfer_source", length = 15)
  public String getTransferSource() {
    return this.transferSource;
  }

  public void setTransferSource(String transferSource) {
    this.transferSource = transferSource;
  }

  @Column(name = "transfer_destination", length = 15)
  public String getTransferDestination() {
    return this.transferDestination;
  }

  public void setTransferDestination(String transferDestination) {
    this.transferDestination = transferDestination;
  }

  @Column(name = "use_perdiem", nullable = false, length = 1)
  public char getUsePerdiem() {
    return this.usePerdiem;
  }

  public void setUsePerdiem(char usePerdiem) {
    this.usePerdiem = usePerdiem;
  }

  @Column(name = "per_diem_code", length = 15)
  public String getPerDiemCode() {
    return this.perDiemCode;
  }

  public void setPerDiemCode(String perDiemCode) {
    this.perDiemCode = perDiemCode;
  }

  @Temporal(TemporalType.DATE)
  @Column(name = "visit_custom_field4", length = 13)
  public Date getVisitCustomField4() {
    return this.visitCustomField4;
  }

  public void setVisitCustomField4(Date visitCustomField4) {
    this.visitCustomField4 = visitCustomField4;
  }

  @Temporal(TemporalType.DATE)
  @Column(name = "visit_custom_field5", length = 13)
  public Date getVisitCustomField5() {
    return this.visitCustomField5;
  }

  public void setVisitCustomField5(Date visitCustomField5) {
    this.visitCustomField5 = visitCustomField5;
  }

  @Temporal(TemporalType.DATE)
  @Column(name = "visit_custom_field6", length = 13)
  public Date getVisitCustomField6() {
    return this.visitCustomField6;
  }

  public void setVisitCustomField6(Date visitCustomField6) {
    this.visitCustomField6 = visitCustomField6;
  }

  @Column(name = "visit_custom_field7", precision = 15)
  public BigDecimal getVisitCustomField7() {
    return this.visitCustomField7;
  }

  public void setVisitCustomField7(BigDecimal visitCustomField7) {
    this.visitCustomField7 = visitCustomField7;
  }

  @Column(name = "visit_custom_field8", precision = 15)
  public BigDecimal getVisitCustomField8() {
    return this.visitCustomField8;
  }

  public void setVisitCustomField8(BigDecimal visitCustomField8) {
    this.visitCustomField8 = visitCustomField8;
  }

  @Column(name = "visit_custom_field9", precision = 15)
  public BigDecimal getVisitCustomField9() {
    return this.visitCustomField9;
  }

  public void setVisitCustomField9(BigDecimal visitCustomField9) {
    this.visitCustomField9 = visitCustomField9;
  }

  @Column(name = "discharge_remarks")
  public String getDischargeRemarks() {
    return this.dischargeRemarks;
  }

  public void setDischargeRemarks(String dischargeRemarks) {
    this.dischargeRemarks = dischargeRemarks;
  }

  @Column(name = "patient_care_oftext_country_code", length = 5)
  public String getPatientCareOftextCountryCode() {
    return this.patientCareOftextCountryCode;
  }

  public void setPatientCareOftextCountryCode(String patientCareOftextCountryCode) {
    this.patientCareOftextCountryCode = patientCareOftextCountryCode;
  }

  @Column(name = "patient_discharge_status", length = 1)
  public Character getPatientDischargeStatus() {
    return this.patientDischargeStatus;
  }

  public void setPatientDischargeStatus(Character patientDischargeStatus) {
    this.patientDischargeStatus = patientDischargeStatus;
  }

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "visitId")
  public Set<BillModel> getBills() {
    return bills;
  }

  public void setBills(Set<BillModel> bills) {
    this.bills = bills;
  }

  // To be added when below entities are added to hbm configuration

  // @OneToMany(fetch = FetchType.LAZY, mappedBy = "patientRegistration")
  // public Set<FpLogs> getFpLogses() {
  // return this.fpLogses;
  // }
  //
  // public void setFpLogses(Set<FpLogs> fpLogses) {
  // this.fpLogses = fpLogses;
  // }
  //
  // @OneToMany(fetch = FetchType.LAZY, mappedBy = "patientRegistration")
  // public Set<OpthalTestMain> getOpthalTestMains() {
  // return this.opthalTestMains;
  // }
  //
  // public void setOpthalTestMains(Set<OpthalTestMain> opthalTestMains) {
  // this.opthalTestMains = opthalTestMains;
  // }
  //
  // @OneToMany(fetch = FetchType.LAZY, mappedBy = "patientRegistration")
  // public Set<OpthalDoctorExamMain> getOpthalDoctorExamMains() {
  // return this.opthalDoctorExamMains;
  // }
  //
  // public void setOpthalDoctorExamMains(Set<OpthalDoctorExamMain> opthalDoctorExamMains) {
  // this.opthalDoctorExamMains = opthalDoctorExamMains;
  // }

  @NotFound(action = NotFoundAction.IGNORE)
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "reference_docto_id", referencedColumnName = "referal_no", insertable = false, updatable = false)
  public ReferralModel getReferralDoctorReferral() {
    return referralDoctorReferral;
  }

  public void setReferralDoctorReferral(ReferralModel referralDoctorReferral) {
    this.referralDoctorReferral = referralDoctorReferral;
  }

  @NotFound(action = NotFoundAction.IGNORE)
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "reference_docto_id", referencedColumnName = "doctor_id", insertable = false, updatable = false)
  public DoctorsModel getReferralDoctorDoctors() {
    return referralDoctorDoctors;
  }

  public void setReferralDoctorDoctors(DoctorsModel referralDoctorDoctors) {
    this.referralDoctorDoctors = referralDoctorDoctors;
  }

  @NotFound(action = NotFoundAction.IGNORE)
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "primary_insurance_co", referencedColumnName = "insurance_co_id", insertable = false, updatable = false)
  public InsuranceCompanyMasterModel getPrimaryInsuranceCoModel() {
    return primaryInsuranceCoModel;
  }

  public void setPrimaryInsuranceCoModel(InsuranceCompanyMasterModel primaryInsuranceCoModel) {
    this.primaryInsuranceCoModel = primaryInsuranceCoModel;
  }

  @NotFound(action = NotFoundAction.IGNORE)
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "secondary_insurance_co", referencedColumnName = "insurance_co_id", insertable = false, updatable = false)
  public InsuranceCompanyMasterModel getSecondaryInsuranceCoModel() {
    return secondaryInsuranceCoModel;
  }

  public void setSecondaryInsuranceCoModel(InsuranceCompanyMasterModel secondaryInsuranceCoModel) {
    this.secondaryInsuranceCoModel = secondaryInsuranceCoModel;
  }

}
