package com.insta.hms.master.RegistrationCharges;

import org.apache.struts.action.ActionForm;
import org.apache.struts.upload.FormFile;

public class RegistrationChargesForm extends ActionForm {

	private FormFile xlsRegistrationFile;
	private String org_id;

	public String getOrg_id() {
		return org_id;
	}
	public void setOrg_id(String org_id) {
		this.org_id = org_id;
	}
	public FormFile getXlsRegistrationFile() {
		return xlsRegistrationFile;
	}
	public void setXlsRegistrationFile(FormFile xlsRegistrationFile) {
		this.xlsRegistrationFile = xlsRegistrationFile;
	}

}