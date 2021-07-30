package com.insta.hms.master.DynaPackage;

import org.apache.struts.action.ActionForm;
import org.apache.struts.upload.FormFile;

public class DynaPkgUploadForm extends ActionForm {

	private FormFile xlsDetailsForm;
	private FormFile xlsChargesForm;
	private FormFile csvLimitsFile;
	private String org_id;

	public FormFile getXlsChargesForm() {
		return xlsChargesForm;
	}
	public void setXlsChargesForm(FormFile xlsChargesForm) {
		this.xlsChargesForm = xlsChargesForm;
	}
	public FormFile getXlsDetailsForm() {
		return xlsDetailsForm;
	}
	public void setXlsDetailsForm(FormFile xlsDetailsForm) {
		this.xlsDetailsForm = xlsDetailsForm;
	}
	public String getOrg_id() {
		return org_id;
	}
	public void setOrg_id(String org_id) {
		this.org_id = org_id;
	}

	public FormFile getCsvLimitsFile() { return csvLimitsFile; }
	public void setCsvLimitsFile(FormFile v) { csvLimitsFile = v; }

}
