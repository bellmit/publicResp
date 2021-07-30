package com.insta.hms.master.ServiceGroup;

import org.apache.struts.action.ActionForm;
import org.apache.struts.upload.FormFile;

public class ServiceGroupUploadForm extends ActionForm {

	private FormFile xlsServiceGroupFile;

	public FormFile getXlsServiceGroupFile() {
		return xlsServiceGroupFile;
	}

	public void setXlsServiceGroupFile(FormFile xlsServiceGroupFile) {
		this.xlsServiceGroupFile = xlsServiceGroupFile;
	}

}