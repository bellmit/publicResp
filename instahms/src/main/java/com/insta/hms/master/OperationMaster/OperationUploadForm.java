package com.insta.hms.master.OperationMaster;

import org.apache.struts.action.ActionForm;
import org.apache.struts.upload.FormFile;

public class OperationUploadForm extends ActionForm {

	private FormFile csvFile;
	private String org_id;
	private FormFile xlsOperationFile;

	public FormFile getCsvFile() { return csvFile; }
	public void setCsvFile(FormFile v) { csvFile = v; }

	public String getOrg_id() { return org_id; }
	public void setOrg_id(String v) { org_id = v; }

	public FormFile getXlsOperationFile() {return xlsOperationFile;}
	public void setXlsOperationFile(FormFile v) {this.xlsOperationFile = v;}

}

