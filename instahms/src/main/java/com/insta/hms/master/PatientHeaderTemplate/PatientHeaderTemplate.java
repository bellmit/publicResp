/**
 *
 */
package com.insta.hms.master.PatientHeaderTemplate;

import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;

/**
 * @author krishna.t
 *
 */
public enum PatientHeaderTemplate {

	Lab				("L", "Laboratoy Patient Header", "Lab_PatientHeader", PrintConfigurationsDAO.PRINT_TYPE_DIAG),
	Rad 			("R", "Radiology Patient Header", "Lab_PatientHeader", PrintConfigurationsDAO.PRINT_TYPE_DIAG),
	Ser             ("S", "Services Patient Header", "Service_PatientHeader", PrintConfigurationsDAO.PRINT_TYPE_SERVICE),
	Dis 			("D", "Discharge Patient Header", "Discharge_PatientHeader", PrintConfigurationsDAO.PRINT_TYPE_DISCHARGE),
	Documents		("Documents", "Generic Documents Patient Header", "GenericDocs_PatientHeader",  PrintConfigurationsDAO.PRINT_TYPE_PATIENT),
	PatientWardActivities ("PWACT", "Patient Ward Activites Header", "PW_Activities_PatientHeader", PrintConfigurationsDAO.PRINT_TYPE_PATIENT),
	TreatmentSheet ("TSheet", "Treatement Sheet Patient Header", "Discharge_PatientHeader", PrintConfigurationsDAO.PRINT_TYPE_DISCHARGE),
	ClinicalInfo ("CI", "Clinical Info Patient Header", "ClinicalInfo_PatientHeader", PrintConfigurationsDAO.PRINT_TYPE_PATIENT),
	Triage 			("Triage", "Triage Patient Header", "Triage_PatientHeader", PrintConfigurationsDAO.PRINT_TYPE_PATIENT),
	Assessment ("Assessment", "Assessment Patient Header", "Assessment_PatientHeader", PrintConfigurationsDAO.PRINT_TYPE_PATIENT),
	WebBased ("WEB_LAB", "Web Based Patient Header", "WebLab_PatientHeader",PrintConfigurationsDAO.PRINT_TYPE_DIAG),
	Medication_Chart("Medication_Chart", "Medication Chart Report Header", "Medication_Chart_PatientHeader", PrintConfigurationsDAO.PRINT_TYPE_PATIENT),
	INSTA_GENERIC_FORM("INSTA_GENERIC_FORM", "Patient Generic Form Patient Header", "PatientGenericForm_PatientHeader", PrintConfigurationsDAO.PRINT_TYPE_PATIENT),
	APIBASED_LAB ("API_LAB", "API Based Lab Patient Header", "APILab_PatientHeader", PrintConfigurationsDAO.PRINT_TYPE_WEB_DIAG),
	APIBASED_RAD ("API_RAD", "API Based Rad Patient Header", "APIRad_PatientHeader", PrintConfigurationsDAO.PRINT_TYPE_WEB_DIAG),
	Discharge_Medication("Discharge_Medication", "Discharge Medication Patient Header" ,"DischargeMedicationPatientHeader",PrintConfigurationsDAO.PRINT_TYPE_DISCHARGE)
	;

	/* One patient header template "WebBased" is used for both laboratory and radiology web based report.. */

	String type = null;
	String ftlName = null;
	String printType = null;
	String title = null;

	private PatientHeaderTemplate(String type, String title, String ftlName, String printType) {
		this.type = type;
		this.ftlName = ftlName;
		this.printType = printType;
		this.title = title;
	}

	public String getType() {
		return type;
	}

	public String getTitle() {
		return title;
	}

	public String getFtlName() {
		return ftlName;
	}

	public String getPrintType() {
		return printType;
	}

	public PatientHeaderTemplate getTemplate(String type) {
		for (PatientHeaderTemplate template: PatientHeaderTemplate.values()) {
			if (template.getType().equals(type))
				return template;
		}
		return null;
	}

}
