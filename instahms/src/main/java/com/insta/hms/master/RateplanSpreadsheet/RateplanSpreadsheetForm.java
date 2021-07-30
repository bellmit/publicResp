package com.insta.hms.master.RateplanSpreadsheet;

import org.apache.struts.action.ActionForm;
import org.apache.struts.upload.FormFile;

public class RateplanSpreadsheetForm extends ActionForm {
	private String orgId;
	private FormFile xlsRateplanfile;

	public String getOrgId() {
		return orgId;
	}
	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}
	public FormFile getXlsRateplanfile() {
		return xlsRateplanfile;
	}
	public void setXlsRateplanfile(FormFile xlsRateplanfile) {
		this.xlsRateplanfile = xlsRateplanfile;
	}

}