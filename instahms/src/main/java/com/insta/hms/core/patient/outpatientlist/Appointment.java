/**
 * 
 */
package com.insta.hms.core.patient.outpatientlist;

/**
 * @author krishnat
 *
 */
public class Appointment {
	
	private int appointment_id;
	private String patient_name;
	private String mr_no;
	private String visit_id;
	private String appointment_resource;
	private String appointment_date;
	private String appointment_time;
	private String department;
	private String department_id;
	private String category;
	private String status;
    private String age_text;
    private int age;
    private String patient_gender;
    private String patient_gender_text;

    
	public int getAge() {
      return age;
    }
    public void setAge(int age) {
      this.age = age;
    }
    public String getPatient_gender() {
      return patient_gender;
    }
    public void setPatient_gender(String patient_gender) {
      this.patient_gender = patient_gender;
    }
    public String getPatient_gender_text() {
      return patient_gender_text;
    }
    public void setPatient_gender_text(String patient_gender_text) {
      this.patient_gender_text = patient_gender_text;
    }
    public String getAge_text() {
      return age_text;
    }
    public void setAge_text(String age_text) {
      this.age_text = age_text;
    }
    public String getAppointment_date() {
		return appointment_date;
	}
	public void setAppointment_date(String appointment_date) {
		this.appointment_date = appointment_date;
	}
	private String patient_phone;
	public int getAppointment_id() {
		return appointment_id;
	}
	public void setAppointment_id(int appointment_id) {
		this.appointment_id = appointment_id;
	}
	public String getPatient_name() {
		return patient_name;
	}
	public void setPatient_name(String patient_name) {
		this.patient_name = patient_name;
	}
	public String getMr_no() {
		return mr_no;
	}
	public void setMr_no(String mr_no) {
		this.mr_no = mr_no;
	}
	public String getVisit_id() {
		return visit_id;
	}
	public void setVisit_id(String visit_id) {
		this.visit_id = visit_id;
	}
	public String getAppointment_resource() {
		return appointment_resource;
	}
	public void setAppointment_resource(String appointment_resource) {
		this.appointment_resource = appointment_resource;
	}
	public String getAppointment_time() {
		return appointment_time;
	}
	public void setAppointment_time(String appointment_time) {
		this.appointment_time = appointment_time;
	}
	public String getPatient_phone() {
		return patient_phone;
	}
	public void setPatient_phone(String patient_phone) {
		this.patient_phone = patient_phone;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	
	@Override
	public int hashCode() {
		return this.category.hashCode() * this.appointment_id * 20; 
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
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof Appointment) {
			Appointment apt = (Appointment) obj;
			if (apt.getCategory().equals(this.category) && apt.getAppointment_id() == this.appointment_id) {
				return true;
			}
		}
		return false;
	}
}
