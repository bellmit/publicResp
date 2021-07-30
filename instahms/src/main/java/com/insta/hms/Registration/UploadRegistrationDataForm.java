package com.insta.hms.Registration;

import org.apache.struts.action.ActionForm;
import org.apache.struts.upload.FormFile;

public class UploadRegistrationDataForm extends ActionForm{

	private FormFile csvFile;

	public FormFile getCsvFile() { return csvFile; }
	public void setCsvFile(FormFile v) { csvFile = v; }
}
