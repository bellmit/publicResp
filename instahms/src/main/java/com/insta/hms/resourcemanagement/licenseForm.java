package com.insta.hms.resourcemanagement;

import org.apache.struts.action.ActionForm;
import org.apache.struts.upload.FormFile;

import java.math.BigDecimal;
import java.sql.Date;

@SuppressWarnings("serial")

public class licenseForm extends ActionForm {

	private transient FormFile licenseFile;
	private String license_desc;
	private String license_note;
	private int license_type_id;
	private BigDecimal license_value;
	private Date license_start_date;
	private Date license_end_date;
	private Date license_renewal_date;
	private String[] licenseTypeFilter;
	private String renewalFrom;
	private String renewalTo;
	private String expiryFrom;
	private String expiryTo;
	private String license_status;
	private int contractor_id;


	public String getLicense_status() {
		return license_status;
	}

	public void setLicense_status(String license_status) {
		this.license_status = license_status;
	}

	public String getExpiryTo() {
		return expiryTo;
	}

	public void setExpiryTo(String expiryTo) {
		this.expiryTo = expiryTo;
	}

	public String getExpiryFrom() {
		return expiryFrom;
	}

	public void setExpiryFrom(String expiryFrom) {
		this.expiryFrom = expiryFrom;
	}

	public String getRenewalFrom() {
		return renewalFrom;
	}

	public void setRenewalFrom(String renewalFrom) {
		this.renewalFrom = renewalFrom;
	}

	public String getRenewalTo() {
		return renewalTo;
	}

	public void setRenewalTo(String renewalTo) {
		this.renewalTo = renewalTo;
	}

	public FormFile getLicenseFile() {
		return licenseFile;
	}

	public void setLicenseFile(FormFile licenseFile) {
		this.licenseFile = licenseFile;
	}

	public Date getLicense_end_date() {
		return license_end_date;
	}

	public void setLicense_end_date(Date license_end_date) {
		this.license_end_date = license_end_date;
	}

	public Date getLicense_renewal_date() {
		return license_renewal_date;
	}

	public void setLicense_renewal_date(Date license_renewal_date) {
		this.license_renewal_date = license_renewal_date;
	}

	public Date getLicense_start_date() {
		return license_start_date;
	}

	public void setLicense_start_date(Date license_start_date) {
		this.license_start_date = license_start_date;
	}

	public int getLicense_type_id() {
		return license_type_id;
	}

	public void setLicense_type_id(int license_type_id) {
		this.license_type_id = license_type_id;
	}

	public BigDecimal getLicense_value() {
		return license_value;
	}

	public void setLicense_value(BigDecimal license_value) {
		this.license_value = license_value;
	}

	public String getLicense_desc() {
		return license_desc;
	}

	public void setLicense_desc(String license_desc) {
		this.license_desc = license_desc;
	}

	public String getLicense_note() {
		return license_note;
	}

	public void setLicense_note(String license_note) {
		this.license_note = license_note;
	}

	public String[] getLicenseTypeFilter() {
		return licenseTypeFilter;
	}

	public void setLicenseTypeFilter(String[] licenseTypeFilter) {
		this.licenseTypeFilter = licenseTypeFilter;
	}

	public int getContractor_id() {
		return contractor_id;
	}

	public void setContractor_id(int contractor_id) {
		this.contractor_id = contractor_id;
	}



}