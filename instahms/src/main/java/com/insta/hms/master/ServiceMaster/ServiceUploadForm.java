package com.insta.hms.master.ServiceMaster;

import org.apache.struts.action.ActionForm;
import org.apache.struts.upload.FormFile;

public class ServiceUploadForm extends ActionForm {

	private FormFile csvFile;
	private String org_id;
	private FormFile xlsServiceFile;

	public FormFile getCsvFile() { return csvFile; }
	public void setCsvFile(FormFile v) { csvFile = v; }

	public String getOrg_id() { return org_id; }
	public void setOrg_id(String v) { org_id = v; }

	public FormFile getXlsServiceFile() {return xlsServiceFile;}
	public void setXlsServiceFile(FormFile v) {this.xlsServiceFile = v;}

}

