package com.insta.hms.master.PlanMaster;

import org.apache.struts.action.ActionForm;
import org.apache.struts.upload.FormFile;

public class PlanUploadForm extends ActionForm {

	private FormFile csvFile;
	private String org_id;
	private FormFile xlsServiceFile;

	public FormFile getCsvFile() { return csvFile; }
	public void setCsvFile(FormFile v) { csvFile = v; }

	public FormFile getXlsServiceFile() {return xlsServiceFile;}
	public void setXlsServiceFile(FormFile v) {this.xlsServiceFile = v;}

}
