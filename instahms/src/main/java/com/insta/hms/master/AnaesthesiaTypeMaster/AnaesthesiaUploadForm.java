package com.insta.hms.master.AnaesthesiaTypeMaster;

import org.apache.struts.action.ActionForm;
import org.apache.struts.upload.FormFile;

public class AnaesthesiaUploadForm extends ActionForm {

	private FormFile xlsDetailsFile;
	private String org_id;
	private FormFile xlsAnaesthesiaFile;


	public FormFile getXlsDetailsFile() {
		return xlsDetailsFile;
	}
	public void setXlsDetailsFile(FormFile xlsDetailsFile) {
		this.xlsDetailsFile = xlsDetailsFile;
	}

	public String getOrg_id() { return org_id; }
	public void setOrg_id(String v) { org_id = v; }

	public FormFile getXlsAnaesthesiaFile() {return xlsAnaesthesiaFile;}
	public void setXlsAnaesthesiaFile(FormFile v) {this.xlsAnaesthesiaFile = v;}

}

