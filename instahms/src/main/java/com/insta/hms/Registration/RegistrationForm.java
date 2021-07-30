package com.insta.hms.Registration;

import com.insta.hms.orders.CommonOrderForm;

import org.apache.struts.upload.FormFile;

public class RegistrationForm extends CommonOrderForm {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private FormFile primary_insurance_doc_content_bytea1;
	private FormFile primary_corporate_doc_content_bytea1;
	private FormFile primary_national_doc_content_bytea1;
	private FormFile secondary_insurance_doc_content_bytea1;
	private FormFile secondary_corporate_doc_content_bytea1;
	private FormFile secondary_national_doc_content_bytea1;

	private FormFile patPhoto;
	private FormFile doc_content_bytea1;
	private FormFile doc_content_bytea2;
	private FormFile doc_content_bytea3;
	private FormFile doc_content_bytea4;
	private FormFile doc_content_bytea5;
	private FormFile doc_content_bytea6;
	private FormFile doc_content_bytea7;
	
	private FormFile pastedPhoto;
	private FormFile primary_sponsor_pastedPhoto;
	private FormFile secondary_sponsor_pastedPhoto;
	


	public static final int DEFALUT_REFERRAL_CATEGORY = 1;
	public static final String DEFALUT_REFERRAL_PAYMENT_ELIGIBLE = "Y";

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
	public FormFile getPatPhoto() {
		return patPhoto;
	}
	public void setPatPhoto(FormFile patPhoto) {
		this.patPhoto = patPhoto;
	}
	public FormFile getDoc_content_bytea1() {
		return doc_content_bytea1;
	}
	public void setDoc_content_bytea1(FormFile doc_content_bytea1) {
		this.doc_content_bytea1 = doc_content_bytea1;
	}
	public FormFile getDoc_content_bytea2() {
		return doc_content_bytea2;
	}
	public void setDoc_content_bytea2(FormFile doc_content_bytea2) {
		this.doc_content_bytea2 = doc_content_bytea2;
	}
	public FormFile getDoc_content_bytea3() {
		return doc_content_bytea3;
	}
	public void setDoc_content_bytea3(FormFile doc_content_bytea3) {
		this.doc_content_bytea3 = doc_content_bytea3;
	}
	public FormFile getDoc_content_bytea4() {
		return doc_content_bytea4;
	}
	public void setDoc_content_bytea4(FormFile doc_content_bytea4) {
		this.doc_content_bytea4 = doc_content_bytea4;
	}
	public FormFile getDoc_content_bytea5() {
		return doc_content_bytea5;
	}
	public void setDoc_content_bytea5(FormFile doc_content_bytea5) {
		this.doc_content_bytea5 = doc_content_bytea5;
	}
	public FormFile getDoc_content_bytea6() {
		return doc_content_bytea6;
	}
	public void setDoc_content_bytea6(FormFile doc_content_bytea6) {
		this.doc_content_bytea6 = doc_content_bytea6;
	}
	public FormFile getDoc_content_bytea7() {
		return doc_content_bytea7;
	}
	public void setDoc_content_bytea7(FormFile doc_content_bytea7) {
		this.doc_content_bytea7 = doc_content_bytea7;
	}
	public FormFile getPastedPhoto() {
		return pastedPhoto;
	}
	public void setPastedPhoto(FormFile pastedPhoto) {
		this.pastedPhoto = pastedPhoto;
	}


	public FormFile getPrimary_sponsor_pastedPhoto() {
		return primary_sponsor_pastedPhoto;
	}
	
	public void setPrimary_sponsor_pastedPhoto(FormFile primary_sponsor_pastedPhoto) {
		this.primary_sponsor_pastedPhoto = primary_sponsor_pastedPhoto;
	}
	
	public FormFile getSecondary_sponsor_pastedPhoto() {
		return secondary_sponsor_pastedPhoto;
	}
	
	public void setSecondary_sponsor_pastedPhoto(FormFile secondary_sponsor_pastedPhoto) {
		this.secondary_sponsor_pastedPhoto = secondary_sponsor_pastedPhoto;
	}

}
