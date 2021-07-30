package com.insta.hms.master.DiagnosisCodeFavourites;

import org.apache.struts.action.ActionForm;
import org.apache.struts.upload.FormFile;
/**
 *
 * @author Anil N
 *
 */
public class ICDFavForm extends ActionForm {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private FormFile xlsICDFile;

	public FormFile getXlsICDFile() {
		return xlsICDFile;
	}

	public void setXlsICDFile(FormFile xlsICDFile) {
		this.xlsICDFile = xlsICDFile;
	}
}
