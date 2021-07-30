/**
 * 
 */
package com.insta.hms.core.patient.outpatientlist;

import java.util.ArrayList;
import java.util.List;

/**
 * @author krishnat
 *
 */
public class Patient {
	
	private String mr_no;
	private String full_name;
	private String patient_name;
	private String middle_name;
	private String last_name;
	private String patient_gender;
	private String patient_gender_text;
	private int age;
	private String patient_phone;
	private String patient_phone_country_code;
	private String government_identifier;
	private String age_text;
	private String rawAgeText;
	private String date_of_birth;
	private String date_of_death;
	private String abbreviation;
	private Integer contactId;
	private String salutationName;
	private String emailId;
	public String getEmailId() {
    return emailId;
  }

  public void setEmailId(String emailId) {
    this.emailId = emailId;
  }

  public String getSalutationName() {
    return salutationName;
  }

  public void setSalutationName(String salutationName) {
    this.salutationName = salutationName;
  }


  private List<String> duplicate_mr_nos;
	
	private List<Visit> visits = new ArrayList<Visit>();
	private List<Bill> bills = new ArrayList<Bill>();
	private List<Order> orders = new ArrayList<Order>();
	private List<Appointment> appointments = new ArrayList<Appointment>();
	private OtherDetails other_details = new OtherDetails();
	

  public Integer getContactId() {
    return contactId;
  }

  public void setContactId(Integer contactId) {
    this.contactId = contactId;
  }

  public String getMr_no() {
		return mr_no;
	}

	public String getAbbreviation() {
		return abbreviation;
	}

	public void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
	}

	public void setMr_no(String mr_no) {
		this.mr_no = mr_no;
	}
	
	public String getGovernment_identifier() {
		return this.government_identifier;
	}
	
	public void setGovernment_identifier(String government_identifier) {
		this.government_identifier = government_identifier;
	}
	
	public String getFull_name() {
		return full_name;
	}

	public void setFull_name(String full_name) {
		this.full_name = full_name;
	}

	public String getPatient_name() {
		return patient_name;
	}

	public void setPatient_name(String patient_name) {
		this.patient_name = patient_name;
	}

	public String getMiddle_name() {
		return middle_name;
	}

	public void setMiddle_name(String middle_name) {
		this.middle_name = middle_name;
	}

	public String getLast_name() {
		return last_name;
	}

	public void setLast_name(String last_name) {
		this.last_name = last_name;
	}

	public String getPatient_gender() {
		return patient_gender;
	}

	public void setPatient_gender(String patient_gender) {
		this.patient_gender = patient_gender;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}
	
	public String getAge_text() {
		return age_text;
	}

	public void setAge_text(String age_text) {
		this.age_text = age_text;
	}


	public String getPatient_phone() {
		return patient_phone;
	}

	public void setPatient_phone(String patient_phone) {
		this.patient_phone = patient_phone;
	}
	
	public String getPatient_phone_country_code() {
		return this.patient_phone_country_code;
	}
	
	public void setPatient_phone_country_code(String patient_phone_country_code) {
		this.patient_phone_country_code = patient_phone_country_code;
	}

	public List<Visit> getVisits() {
		return visits;
	}

	public void setVisits(List<Visit> visits) {
		this.visits = visits;
	}

	public void addVisit(Visit visit) {
		if (this.visits == null) {
			this.visits = new ArrayList<Visit>();
		}
		this.visits.add(visit);
	}	


	public List<Bill> getBills() {
		return bills;
	}

	public void setBills(List<Bill> bills) {
		this.bills = bills;
	}

	public void addBill(Bill bill) {
		if (this.bills == null) {
			this.bills = new ArrayList<Bill>();
		}
		this.bills.add(bill);
	}	

	public List<Order> getOrders() {
		return orders;
	}

	public void setOrders(List<Order> orders) {
		this.orders = orders;
	}

	public void addOrder(Order order) {
		if (this.orders == null) {
			this.orders = new ArrayList<Order>();
		}
		this.orders.add(order);
	}	


	public List<Appointment> getAppointments() {
		return appointments;
	}

	public void setAppointments(List<Appointment> appointments) {
		this.appointments = appointments;
	}
	
	public void addAppointment(Appointment appointment) {
		if (this.appointments == null) {
			this.appointments = new ArrayList<Appointment>();
		}
		this.appointments.add(appointment);
	}
	
	public OtherDetails getOther_details() {
		return other_details;
	}

	public void setOther_details(OtherDetails other_details) {
		this.other_details = other_details;
	}

	public String getDate_of_birth() {
		return date_of_birth;
	}

	public void setDate_of_birth(String date_of_birth) {
		this.date_of_birth = date_of_birth;
	}

	public String getDate_of_death() {
		return date_of_death;
	}

	public void setDate_of_death(String date_of_death) {
		this.date_of_death = date_of_death;
	}

	public Boolean getAlive() {
		return this.date_of_death == null;
	}

	public String getPatient_gender_text() {
		return patient_gender_text;
	}

	public void setPatient_gender_text(String patient_gender_text) {
		this.patient_gender_text = patient_gender_text;
	}

	public List<String> getDuplicate_mr_nos() {
		return duplicate_mr_nos == null ? new ArrayList<String>() : duplicate_mr_nos;
	}

	public void setDuplicate_mr_nos(List<String> duplicate_mr_nos) {
		this.duplicate_mr_nos = duplicate_mr_nos;
	}

	public String getRawAgeText() {
		return rawAgeText;
	}

	public void setRawAgeText(String rawAgeText) {
		this.rawAgeText = rawAgeText;
	}

	public class OtherDetails {
		private String last_visited_date;
		private String todays_visit_time;
		private String todays_visit_type;
		private String todays_appointment_time;
		private String doctor_name;
		private int appointment_id;
		private String resource_name;
		private String dept_name;
		private long future_appointments_count;
		private long todays_appointments_count;
		private long past_appointments_count;
		private long todays_visit_count;
		private long todays_doctor_order_count;
		private String category;
		private boolean vip;
		private boolean mlc;
		private boolean er;
		private String sendSms;
		private String sendEmail;
		private String langCode;
		private int apptId;
		
		public String getCategory() {
			return category;
		}
		public void setCategory(String category) {
			this.category = category;
		}
		public String getDept_name() {
			return dept_name;
		}
		public void setDept_name(String dept_name) {
			this.dept_name = dept_name;
		}
		public String getTodays_visit_type() {
			return todays_visit_type;
		}
		public void setTodays_visit_type(String todays_visit_type) {
			this.todays_visit_type = todays_visit_type;
		}
		public String getTodays_visit_time() {
			return todays_visit_time;
		}
		public void setTodays_visit_time(String todays_visit_time) {
			this.todays_visit_time = todays_visit_time;
		}
		public String getResource_name() {
			return resource_name;
		}
		public void setResource_name(String resource_name) {
			this.resource_name = resource_name;
		}
		public long getPast_appointments_count() {
			return past_appointments_count;
		}
		public void setPast_appointments_count(long past_appointments_count) {
			this.past_appointments_count = past_appointments_count;
		}
		public int getAppointment_id() {
			return appointment_id;
		}
		public void setAppointment_id(int appointment_id) {
			this.appointment_id = appointment_id;
		}
		public String getLast_visited_date() {
			return last_visited_date;
		}
		public void setLast_visited_date(String last_visited_date) {
			this.last_visited_date = last_visited_date;
		}
		public String getDoctor_name() {
			return doctor_name;
		}
		public void setDoctor_name(String doctor_name) {
			this.doctor_name = doctor_name;
		}
		public long getFuture_appointments_count() {
			return future_appointments_count;
		}
		public void setFuture_appointments_count(long future_appointments_count) {
			this.future_appointments_count = future_appointments_count;
		}
		public long getTodays_appointments_count() {
			return todays_appointments_count;
		}
		public void setTodays_appointments_count(long todays_appointments_count) {
			this.todays_appointments_count = todays_appointments_count;
		}
		public long getTodays_visit_count() {
			return todays_visit_count;
		}
		public void setTodays_visit_count(long todays_visit_count) {
			this.todays_visit_count = todays_visit_count;
		}
		public long getTodays_doctor_order_count() {
			return todays_doctor_order_count;
		}
		public void setTodays_doctor_order_count(long todays_doctor_order_count) {
			this.todays_doctor_order_count = todays_doctor_order_count;
		}
		public String getTodays_appointment_time() {
			return todays_appointment_time;
		}
		public void setTodays_appointment_time(String todays_appointment_time) {
			this.todays_appointment_time = todays_appointment_time;
		}
		public boolean isVip() {
			return vip;
		}
		public void setVip(boolean vip) {
			this.vip = vip;
		}
		public boolean isMlc() {
			return mlc;
		}
		public void setMlc(boolean mlc) {
			this.mlc = mlc;
		}
		public boolean isEr() {
			return er;
		}
		public void setEr(boolean er) {
			this.er = er;
		}
    public String getSendSms() {
      return sendSms;
    }
    public void setSendSms(String sendSms) {
      this.sendSms = sendSms;
    }
    public String getSendEmail() {
      return sendEmail;
    }
    public void setSendEmail(String sendEmail) {
      this.sendEmail = sendEmail;
    }
    public String getLangCode() {
      return langCode;
    }
    public void setLangCode(String langCode) {
      this.langCode = langCode;
    }
    public int getApptId() {
      return apptId;
    }
    public void setApptId(int apptId) {
      this.apptId = apptId;
    }
    
		
	}
	
}
