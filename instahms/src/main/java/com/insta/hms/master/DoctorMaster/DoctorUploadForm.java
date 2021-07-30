package com.insta.hms.master.DoctorMaster;

import org.apache.struts.action.ActionForm;
import org.apache.struts.upload.FormFile;

public class DoctorUploadForm extends ActionForm {

	private FormFile xlsFile;
	private String org_id;
	private FormFile xlsDoctorFile;

	public FormFile getXlsFile() { return xlsFile; }
	public void setXlsFile(FormFile v) { xlsFile = v; }

	public String getOrg_id() { return org_id; }
	public void setOrg_id(String v) { org_id = v; }

	public FormFile getXlsDoctorFile() {return xlsDoctorFile;}
	public void setXlsDoctorFile(FormFile v) {this.xlsDoctorFile = v;}

}

