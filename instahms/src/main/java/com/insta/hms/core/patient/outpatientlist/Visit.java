/**
 * 
 */
package com.insta.hms.core.patient.outpatientlist;

/**
 * @author krishnat
 *
 */
public class Visit {
	
	private String visit_id;
	private String visit_date;
	private String visit_time;
	private String visit_status;
	private String primary_insurance;
	private String secondary_insurance;
	private String dept_name;
	private String department;
	private String mr_no;
	
	public String getVisit_id() {
		return visit_id;
	}
	public void setVisit_id(String visit_id) {
		this.visit_id = visit_id;
	}
	public String getVisit_date() {
		return visit_date;
	}
	public void setVisit_date(String visit_date) {
		this.visit_date = visit_date;
	}
	public String getVisit_time() {
		return visit_time;
	}
	public void setVisit_time(String visit_time) {
		this.visit_time = visit_time;
	}
	public String getVisit_status() {
		return visit_status;
	}
	public void setVisit_status(String visit_status) {
		this.visit_status = visit_status;
	}
	public String getPrimary_insurance() {
		return primary_insurance;
	}
	public void setPrimary_insurance(String primary_insurance) {
		this.primary_insurance = primary_insurance;
	}
	public String getSecondary_insurance() {
		return secondary_insurance;
	}
	public void setSecondary_insurance(String secondary_insurance) {
		this.secondary_insurance = secondary_insurance;
	}
	public String getDepartment() {
		return department;
	}
	public void setDepartment(String department) {
		this.department = department;
	}
	public String getDept_name() {
		return dept_name;
	}
	public void setDept_name(String dept_name) {
		this.dept_name = dept_name;
	}
	
	public String getMr_no() {
		return mr_no;
	}
	public void setMr_no(String mr_no) {
		this.mr_no = mr_no;
	}
	@Override
	public int hashCode() {
		return this.visit_id.hashCode() * 20; 
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof Visit) {
			return ((Visit) obj).getVisit_id().equals(this.visit_id);
		}
		return false;
	}
	

}
