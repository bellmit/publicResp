/**
 *
 */
package com.insta.hms.master.ForeignCurrency;

import org.apache.struts.action.ActionForm;
import org.apache.struts.upload.FormFile;

/**
 * @author lakshmi.p
 *
 */
public class ForeignCurrencyUploadForm extends ActionForm {

	private FormFile csvFile;
	private FormFile xlsCurrencyFile;

	public FormFile getCsvFile() {
		return csvFile;
	}
	public void setCsvFile(FormFile csvFile) {
		this.csvFile = csvFile;
	}
	public FormFile getXlsCurrencyFile() {
		return xlsCurrencyFile;
	}
	public void setXlsCurrencyFile(FormFile xlsCurrencyFile) {
		this.xlsCurrencyFile = xlsCurrencyFile;
	}

}
