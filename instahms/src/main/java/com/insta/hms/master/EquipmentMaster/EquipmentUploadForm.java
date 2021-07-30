package com.insta.hms.master.EquipmentMaster;

import org.apache.struts.action.ActionForm;
import org.apache.struts.upload.FormFile;

public class EquipmentUploadForm extends ActionForm {

	private FormFile csvFile;
	private String org_id;
	private FormFile xlsEquipmentFile;

	public FormFile getCsvFile() { return csvFile; }
	public void setCsvFile(FormFile v) { csvFile = v; }

	public String getOrg_id() { return org_id; }
	public void setOrg_id(String v) { org_id = v; }

	public FormFile getXlsEquipmentFile() {return xlsEquipmentFile;}
	public void setXlsEquipmentFile(FormFile v) {this.xlsEquipmentFile = v;}

}

