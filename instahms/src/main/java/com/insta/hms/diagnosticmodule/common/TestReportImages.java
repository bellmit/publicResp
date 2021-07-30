package com.insta.hms.diagnosticmodule.common;

import org.apache.struts.upload.FormFile;

public class TestReportImages {

	private FormFile image;
	private String imageName;
	private String title;
	private int prescribedId;
	private String docType;


	public String getDocType() {
		return docType;
	}
	public void setDocType(String docType) {
		this.docType = docType;
	}
	public FormFile getImage() {
		return image;
	}
	public void setImage(FormFile image) {
		this.image = image;
	}
	public String getImageName() {
		return imageName;
	}
	public void setImageName(String imageName) {
		this.imageName = imageName;
	}
	public int getPrescribedId() {
		return prescribedId;
	}
	public void setPrescribedId(int prescribedId) {
		this.prescribedId = prescribedId;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
}
