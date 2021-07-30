package com.insta.hms.master.SupplierRateContract;

import org.apache.struts.action.ActionForm;
import org.apache.struts.upload.FormFile;

import java.math.BigDecimal;

public class SupplierRateContractForm extends ActionForm {
	
	private String supplier_rate_contract_id;
	private String supplier_contract_name;
	private String supplier_id;
	private String validity_start_date;
	private String validity_end_date;
	private String status;
	
	private int[] medicine_id;
	private BigDecimal[] mrp;
	private BigDecimal[] supplier_rate;
	public BigDecimal[] getSupplier_rate() {
		return supplier_rate;
	}
	public void setSupplier_rate(BigDecimal[] supplier_rate) {
		this.supplier_rate = supplier_rate;
	}
	private BigDecimal[] discount;
	

	public int[] getMedicine_id() {
		return medicine_id;
	}
	public void setMedicine_id(int[] medicine_id) {
		this.medicine_id = medicine_id;
	}
	public BigDecimal[] getMrp() {
		return mrp;
	}
	public void setMrp(BigDecimal[] mrp) {
		this.mrp = mrp;
	}
	public String getSupplier_rate_contract_id() {
		return supplier_rate_contract_id;
	}
	public void setSupplier_rate_contract_id(String supplier_rate_contract_id) {
		this.supplier_rate_contract_id = supplier_rate_contract_id;
	}
	public String getSupplier_contract_name() {
		return supplier_contract_name;
	}
	public void setSupplier_contract_name(String supplier_contract_name) {
		this.supplier_contract_name = supplier_contract_name;
	}
	public String getSupplier_id() {
		return supplier_id;
	}
	public void setSupplier_id(String supplier_id) {
		this.supplier_id = supplier_id;
	}
	public String getValidity_start_date() {
		return validity_start_date;
	}
	public void setValidity_start_date(String validity_start_date) {
		this.validity_start_date = validity_start_date;
	}
	public String getValidity_end_date() {
		return validity_end_date;
	}
	public void setValidity_end_date(String validity_end_date) {
		this.validity_end_date = validity_end_date;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	private FormFile uploadFile;

	public FormFile getUploadFile() {
		return uploadFile;
	}
	public void setUploadFile(FormFile v) {
		this.uploadFile = v;
	}
	public BigDecimal[] getDiscount() {
		return discount;
	}
	public void setDiscount(BigDecimal[] discount) {
		this.discount = discount;
	}
	
}
