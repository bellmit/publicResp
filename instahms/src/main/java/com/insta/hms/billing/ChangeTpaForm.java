package com.insta.hms.billing;

import org.apache.struts.action.ActionForm;
import org.apache.struts.upload.FormFile;

public class ChangeTpaForm extends ActionForm {
	private static final long serialVersionUID = 1L;

	private FormFile primary_insurance_doc_content_bytea1;
	private FormFile primary_corporate_doc_content_bytea1;
	private FormFile primary_national_doc_content_bytea1;
	private FormFile secondary_insurance_doc_content_bytea1;
	private FormFile secondary_corporate_doc_content_bytea1;
	private FormFile secondary_national_doc_content_bytea1;

	public FormFile getPrimary_corporate_doc_content_bytea1() {
		return primary_corporate_doc_content_bytea1;
	}
	public void setPrimary_corporate_doc_content_bytea1(
			FormFile primary_corporate_doc_content_bytea1) {
		this.primary_corporate_doc_content_bytea1 = primary_corporate_doc_content_bytea1;
	}
	public FormFile getPrimary_insurance_doc_content_bytea1() {
		return primary_insurance_doc_content_bytea1;
	}
	public void setPrimary_insurance_doc_content_bytea1(
			FormFile primary_insurance_doc_content_bytea1) {
		this.primary_insurance_doc_content_bytea1 = primary_insurance_doc_content_bytea1;
	}
	public FormFile getPrimary_national_doc_content_bytea1() {
		return primary_national_doc_content_bytea1;
	}
	public void setPrimary_national_doc_content_bytea1(
			FormFile primary_national_doc_content_bytea1) {
		this.primary_national_doc_content_bytea1 = primary_national_doc_content_bytea1;
	}
	public FormFile getSecondary_corporate_doc_content_bytea1() {
		return secondary_corporate_doc_content_bytea1;
	}
	public void setSecondary_corporate_doc_content_bytea1(
			FormFile secondary_corporate_doc_content_bytea1) {
		this.secondary_corporate_doc_content_bytea1 = secondary_corporate_doc_content_bytea1;
	}
	public FormFile getSecondary_insurance_doc_content_bytea1() {
		return secondary_insurance_doc_content_bytea1;
	}
	public void setSecondary_insurance_doc_content_bytea1(
			FormFile secondary_insurance_doc_content_bytea1) {
		this.secondary_insurance_doc_content_bytea1 = secondary_insurance_doc_content_bytea1;
	}
	public FormFile getSecondary_national_doc_content_bytea1() {
		return secondary_national_doc_content_bytea1;
	}
	public void setSecondary_national_doc_content_bytea1(
			FormFile secondary_national_doc_content_bytea1) {
		this.secondary_national_doc_content_bytea1 = secondary_national_doc_content_bytea1;
	}
}
