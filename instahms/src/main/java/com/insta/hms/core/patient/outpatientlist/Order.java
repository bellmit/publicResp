/**
 * 
 */
package com.insta.hms.core.patient.outpatientlist;

/**
 * @author krishnat
 *
 */
public class Order {
	
	private String visit_id;
	private String prescribed_id;
	private String order_type;
	private String item_name;
	private String department;
	private String department_id;
	private String mr_no;

	public String getVisit_id() {
		return visit_id;
	}
	public void setVisit_id(String visit_id) {
		this.visit_id = visit_id;
	}
	public String getPrescribed_id() {
		return prescribed_id;
	}
	public void setPrescribed_id(String prescribed_id) {
		this.prescribed_id = prescribed_id;
	}
	public String getOrder_type() {
		return order_type;
	}
	public void setOrder_type(String order_type) {
		this.order_type = order_type;
	}
	public String getItem_name() {
		return item_name;
	}
	public void setItem_name(String item_name) {
		this.item_name = item_name;
	}
	public String getDepartment() {
		return department;
	}
	public void setDepartment(String department) {
		this.department = department;
	}
	public String getDepartment_id() {
		return department_id;
	}
	public void setDepartment_id(String department_id) {
		this.department_id = department_id;
	}
	public String getMr_no() {
		return mr_no;
	}
	public void setMr_no(String mr_no) {
		this.mr_no = mr_no;
	}
}
