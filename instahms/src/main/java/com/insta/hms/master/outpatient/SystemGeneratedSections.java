/**
 *
 */
package com.insta.hms.master.outpatient;

/**
 * @author insta
 *
 */
public enum SystemGeneratedSections {

	Complaint 					("Complaint (Sys)", -1),
	Allergies 					("Allergies (Sys)", -2),
	TriageSummary 				("Triage Summary (Sys)", -3),
	Vitals 						("Vitals (Sys)", -4),
	ConsultationNotes 			("Consultation Notes (Sys)", -5),
	DiagnosisDetails 			("Diagnosis Details (Sys)", -6),
	Prescription 				("Prescriptions (Sys)", -7),
	HealthMaintenance			("Health Maintenance (Sys)", -15),
	PregnancyHistory			("Obstetric History (Sys)", -13),
	Antenatal					("Antenatal (Sys)", -14),
	PreAnaesthestheticCheckup	("Pre Anaesthesthetic Checkup (Sys)", -16),

	;
	String sectionName;
	int sectionId;

	SystemGeneratedSections (String sectionName, int sectionId) {
		this.sectionName = sectionName;
		this.sectionId = sectionId;
	}

	public String getSectionName() {
		return this.sectionName;
	}

	public int getSectionId() {
		return this.sectionId;
	}
}
