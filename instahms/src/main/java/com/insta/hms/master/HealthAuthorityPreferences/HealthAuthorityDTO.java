/**
 * mithun.saha
 */
package com.insta.hms.master.HealthAuthorityPreferences;

/**
 * @author mithun.saha
 *
 */
public class HealthAuthorityDTO {

	private String healthAuthority = "";
	private String health_authority = "";
	private String diagnosis_code_type;
	private String prescriptions_by_generics;
	private String[] consultation_code_types;
	private String[] drug_code_type;

	private int default_gp_first_consultation;
	private int default_gp_revisit_consultation;
	private int default_sp_first_consultation;
	private int default_sp_revisit_consultation;
	private int child_mother_ins_member_validity_days;
	private String presc_doctor_as_ordering_clinician;
	private String base_rate_plan;
  private boolean isVisitClassificationReq; 

	public String getBase_rate_plan() {
		return base_rate_plan;
	}
	public void setBase_rate_plan(String base_rate_plan) {
		this.base_rate_plan = base_rate_plan;
	}
	public int getChild_mother_ins_member_validity_days() {
		return child_mother_ins_member_validity_days;
	}
	public void setChild_mother_ins_member_validity_days(
			int child_mother_ins_member_validity_days) {
		this.child_mother_ins_member_validity_days = child_mother_ins_member_validity_days;
	}
	public String[] getDrug_code_type() {
		return drug_code_type;
	}
	public void setDrug_code_type(String[] drug_code_type) {
		this.drug_code_type = drug_code_type;
	}
	public String getDiagnosis_code_type() {
		return diagnosis_code_type;
	}
	public void setDiagnosis_code_type(String diagnosis_code_type) {
		this.diagnosis_code_type = diagnosis_code_type;
	}
	public String getHealth_authority() {
		return health_authority;
	}
	public void setHealth_authority(String health_authority) {
		this.health_authority = health_authority;
	}
	public String getPrescriptions_by_generics() {
		return prescriptions_by_generics;
	}
	public void setPrescriptions_by_generics(String prescriptions_by_generics) {
		this.prescriptions_by_generics = prescriptions_by_generics;
	}
	public String getHealthAuthority() {
		return healthAuthority;
	}
	public void setHealthAuthority(String healthAuthority) {
		this.healthAuthority = healthAuthority;
	}
	public String[] getConsultation_code_types() {
		return consultation_code_types;
	}
	public void setConsultation_code_types(String[] consultation_code_types) {
		this.consultation_code_types = consultation_code_types;
	}
	public int getDefault_gp_first_consultation() {
		return default_gp_first_consultation;
	}
	public void setDefault_gp_first_consultation(int default_gp_first_consultation) {
		this.default_gp_first_consultation = default_gp_first_consultation;
	}
	public int getDefault_gp_revisit_consultation() {
		return default_gp_revisit_consultation;
	}
	public void setDefault_gp_revisit_consultation(
			int default_gp_revisit_consultation) {
		this.default_gp_revisit_consultation = default_gp_revisit_consultation;
	}
	public int getDefault_sp_first_consultation() {
		return default_sp_first_consultation;
	}
	public void setDefault_sp_first_consultation(int default_sp_first_consultation) {
		this.default_sp_first_consultation = default_sp_first_consultation;
	}
	public int getDefault_sp_revisit_consultation() {
		return default_sp_revisit_consultation;
	}
	public void setDefault_sp_revisit_consultation(
			int default_sp_revisit_consultation) {
		this.default_sp_revisit_consultation = default_sp_revisit_consultation;
	}
	public void setPresc_doctor_as_ordering_clinician(String presc_doctor_as_ordering_clinician) {
		this.presc_doctor_as_ordering_clinician = presc_doctor_as_ordering_clinician;
	}
	public String getPresc_doctor_as_ordering_clinician() {
		return presc_doctor_as_ordering_clinician;
	}
  public boolean getVisitClassificationReq() {
    return isVisitClassificationReq;
  }
  public void setVisitClassificationReq(boolean isVisitClassificationReq) {
    this.isVisitClassificationReq = isVisitClassificationReq;
  }
}
