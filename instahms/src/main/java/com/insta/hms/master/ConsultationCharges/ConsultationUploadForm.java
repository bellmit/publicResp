package com.insta.hms.master.ConsultationCharges;

import org.apache.struts.action.ActionForm;
import org.apache.struts.upload.FormFile;

public class ConsultationUploadForm extends ActionForm {

	private FormFile xlsConsultaionDetails;
	private FormFile xlsConsultaionCharges;
	private String org_id;

	public String getOrg_id() {
		return org_id;
	}
	public void setOrg_id(String org_id) {
		this.org_id = org_id;
	}
	public FormFile getXlsConsultaionDetails() {
		return xlsConsultaionDetails;
	}
	public void setXlsConsultaionDetails(FormFile xlsConsultaionDetails) {
		this.xlsConsultaionDetails = xlsConsultaionDetails;
	}
	public FormFile getXlsConsultaionCharges() {
		return xlsConsultaionCharges;
	}
	public void setXlsConsultaionCharges(FormFile xlsConsultaionCharges) {
		this.xlsConsultaionCharges = xlsConsultaionCharges;
	}



}