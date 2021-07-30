package com.insta.hms.master.InsuranceCompMaster;

import org.apache.struts.upload.FormFile;

public class InsuCompMasterDTO {

	private FormFile insuruledoc;
	private String insurance_co_name;
	private String default_rate_plan;
	private String status;
	private String insurance_co_address;
	private String insurance_co_city;
	private String insurance_co_state;
	private String insurance_co_country;
	private String insurance_co_phone;
	private String insurance_co_email;
	private String docname;
	private String doctype;
	private String tin_number;
	private String interface_code;
	
	public String getInterface_code() {
    return interface_code;
  }

  public void setInterface_code(String interface_code) {
    this.interface_code = interface_code;
  }

  public String getDocname() {
		return docname;
	}

	public void setDocname(String docname) {
		this.docname = docname;
	}

	public String getDoctype() {
		return doctype;
	}

	public void setDoctype(String doctype) {
		this.doctype = doctype;
	}

	public String getDefault_rate_plan() {
		return default_rate_plan;
	}

	public void setDefault_rate_plan(String default_rate_plan) {
		this.default_rate_plan = default_rate_plan;
	}

	public String getInsurance_co_address() {
		return insurance_co_address;
	}

	public void setInsurance_co_address(String insurance_co_address) {
		this.insurance_co_address = insurance_co_address;
	}

	public String getInsurance_co_city() {
		return insurance_co_city;
	}

	public void setInsurance_co_city(String insurance_co_city) {
		this.insurance_co_city = insurance_co_city;
	}

	public String getInsurance_co_country() {
		return insurance_co_country;
	}

	public void setInsurance_co_country(String insurance_co_country) {
		this.insurance_co_country = insurance_co_country;
	}

	public String getInsurance_co_email() {
		return insurance_co_email;
	}

	public void setInsurance_co_email(String insurance_co_email) {
		this.insurance_co_email = insurance_co_email;
	}

	public String getInsurance_co_name() {
		return insurance_co_name;
	}

	public void setInsurance_co_name(String insurance_co_name) {
		this.insurance_co_name = insurance_co_name;
	}

	public String getInsurance_co_phone() {
		return insurance_co_phone;
	}

	public void setInsurance_co_phone(String insurance_co_phone) {
		this.insurance_co_phone = insurance_co_phone;
	}

	public String getInsurance_co_state() {
		return insurance_co_state;
	}

	public void setInsurance_co_state(String insurance_co_state) {
		this.insurance_co_state = insurance_co_state;
	}

	public FormFile getInsuruledoc() {
		return insuruledoc;
	}

	public void setInsuruledoc(FormFile insuruledoc) {
		this.insuruledoc = insuruledoc;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getTin_number() {
		return tin_number;
	}

	public void setTin_number(String tin_number) {
		this.tin_number = tin_number;
	}

}
