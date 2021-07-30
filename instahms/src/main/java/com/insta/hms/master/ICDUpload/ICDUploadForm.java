/**
 *
 */
package com.insta.hms.master.ICDUpload;

import org.apache.struts.action.ActionForm;
import org.apache.struts.upload.FormFile;

/**
 * @author lakshmi.p
 *
 */
public class ICDUploadForm extends ActionForm {

	private String icd_type;
	private FormFile icd_upload_file_content;
	private String file_format;

	public String getIcd_type() {
		return icd_type;
	}
	public void setIcd_type(String icd_type) {
		this.icd_type = icd_type;
	}
	public FormFile getIcd_upload_file_content() {
		return icd_upload_file_content;
	}
	public void setIcd_upload_file_content(FormFile icd_upload_file_content) {
		this.icd_upload_file_content = icd_upload_file_content;
	}

	public String getFile_format() { return file_format; }
	public void setFile_format(String v) { file_format = v; }

}
