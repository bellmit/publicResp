/**
 *
 */
package com.insta.hms.master.DRGCodesMaster;

import org.apache.struts.action.ActionForm;
import org.apache.struts.upload.FormFile;

/**
 * @author lakshmi
 *
 */
public class DRGCodesUploadForm extends ActionForm {

	private static final long serialVersionUID = 1L;
	private FormFile csvFile;
	private FormFile xlsDRGCodesFile;

	public FormFile getCsvFile() {
		return csvFile;
	}
	public void setCsvFile(FormFile csvFile) {
		this.csvFile = csvFile;
	}
	public FormFile getXlsDRGCodesFile() {
		return xlsDRGCodesFile;
	}
	public void setXlsDRGCodesFile(FormFile xlsDRGCodesFile) {
		this.xlsDRGCodesFile = xlsDRGCodesFile;
	}

}
