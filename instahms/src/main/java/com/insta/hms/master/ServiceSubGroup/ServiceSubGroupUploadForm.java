package com.insta.hms.master.ServiceSubGroup;

import org.apache.struts.action.ActionForm;
import org.apache.struts.upload.FormFile;

public class ServiceSubGroupUploadForm extends ActionForm {

	private FormFile xlsServiceSubGroupFile;

	public FormFile getXlsServiceSubGroupFile() {
		return xlsServiceSubGroupFile;
	}

	public void setXlsServiceSubGroupFile(FormFile xlsServiceSubGroupFile) {
		this.xlsServiceSubGroupFile = xlsServiceSubGroupFile;
	}

}