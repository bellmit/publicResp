package com.insta.hms.diagnosticmodule.prescribetest;

import org.apache.struts.action.ActionForm;

public class DiagnoForm extends ActionForm{


	private String mrno;
	private String orgid;
	private String date;
	private String time;
	private String patid;
	private String username;

	private String doctor;
	private String billno;

	private String[] diagdepartment;
	private String[] priority;
	private String[] testname;
	private String[] testdate;
	private String[] testtime;
	private String[] diagdepartmentid;
	private String[] testid;
	private String[] testcharge;

	private String[] doctorid;
	private String patdeptid;
	private String patvisittype;
	private String[] print;
	private String module;
	private String category;


	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getModule() {
		return module;
	}
	public void setModule(String module) {
		this.module = module;
	}
	public String getPatdeptid() {
		return patdeptid;
	}
	public void setPatdeptid(String patdeptid) {
		this.patdeptid = patdeptid;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}

	public String getMrno() {
		return mrno;
	}
	public void setMrno(String mrno) {
		this.mrno = mrno;
	}
	public String getOrgid() {
		return orgid;
	}
	public void setOrgid(String orgid) {
		this.orgid = orgid;
	}
	public String getPatid() {
		return patid;
	}
	public void setPatid(String patid) {
		this.patid = patid;
	}

	public String[] getDiagdepartment() {
		return diagdepartment;
	}
	public void setDiagdepartment(String[] diagdepartment) {
		this.diagdepartment = diagdepartment;
	}
	public String[] getDiagdepartmentid() {
		return diagdepartmentid;
	}
	public void setDiagdepartmentid(String[] diagdepartmentid) {
		this.diagdepartmentid = diagdepartmentid;
	}
	public String[] getPriority() {
		return priority;
	}
	public void setPriority(String[] priority) {
		this.priority = priority;
	}
	public String[] getTestcharge() {
		return testcharge;
	}
	public void setTestcharge(String[] testcharge) {
		this.testcharge = testcharge;
	}
	public String[] getTestdate() {
		return testdate;
	}
	public void setTestdate(String[] testdate) {
		this.testdate = testdate;
	}
	public String[] getTestid() {
		return testid;
	}
	public void setTestid(String[] testid) {
		this.testid = testid;
	}
	public String[] getTestname() {
		return testname;
	}
	public void setTestname(String[] testname) {
		this.testname = testname;
	}
	public String[] getTesttime() {
		return testtime;
	}
	public void setTesttime(String[] testtime) {
		this.testtime = testtime;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getDoctor() {
		return doctor;
	}
	public void setDoctor(String doctor) {
		this.doctor = doctor;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String[] getDoctorid() {
		return doctorid;
	}
	public void setDoctorid(String[] doctorid) {
		this.doctorid = doctorid;
	}
	public String getPatvisittype() {
		return patvisittype;
	}
	public void setPatvisittype(String patvisittype) {
		this.patvisittype = patvisittype;
	}
	public String[] getPrint() {
		return print;
	}
	public void setPrint(String[] print) {
		this.print = print;
	}
	public String getBillno() {
		return billno;
	}
	public void setBillno(String billno) {
		this.billno = billno;
	}

}
