/**
 *
 */
package com.insta.hms.master.CommonPrintTemplates;

import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;

/**
 * @author krishna
 *
 */
public enum PrintTemplate {

	/*
	 * listed template types will be displayed while adding the print template using one of modes(html/text).
	 */
	DentalConsultation (
			"DentalConsultation",
			"DentalTreatmentDetails",
			"DentalTreatmentDetailsText",
			PrintConfigurationsDAO.PRINT_TYPE_PATIENT,
			"Dental Consultation Print Templates"),

	OtRecord (
			"OtRecord",
			"OtRecordDetails",
			"OtRecordDetailsText",
			PrintConfigurationsDAO.PRINT_TYPE_PATIENT,
			"OT Record Print Templates"),

	PbmPrescription ("PbmPrescription",
					 "PbmPrescription",
					 "PbmPrescriptionText",
					 PrintConfigurationsDAO.PRINT_TYPE_PATIENT,
					 "PBM Prescription Print Templates"),

	QuickEstimateBillPrint ("QuickEstimateBillPrint",
					   		"QuickEstimateBillPrint",
					   		"QuickEstimateBillPrintText",
					   		PrintConfigurationsDAO.PRINT_TYPE_BILL,
					   		"Quick Estimate Print Templates"),
	InstaGenericForm ("InstaGenericForm",
			  "PatientGenericForm",
			  "PatientGenericFormText",
			   PrintConfigurationsDAO.PRINT_TYPE_PATIENT,
			  "Generic Form Print Templates")

	;
	String hmFtlName = null; // html mode ftl file name
	String tmFtlName = null; // html mode ftl file name
	String printType = null;
	String title = null;
	String type = null;

	private PrintTemplate(String type, String hmFtlName, String tmFtlName, String printType, String title) {
		this.hmFtlName = hmFtlName;
		this.tmFtlName = tmFtlName;
		this.printType = printType;
		this.title = title;
		this.type = type;
	}

	public String getHmFtlName() {
		return hmFtlName;
	}

	public String getTmFtlName() {
		return tmFtlName;
	}

	public String getPrintType() {
		return printType;
	}

	public String getTitle() {
		return title;
	}

	public String getType() {
		return type;
	}

}
