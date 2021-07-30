/**
 * 
 */

package com.insta.hms.core.patient.inpatientlist;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class Patient.
 *
 * @author anup vishwas
 */
public class PatientPojo {

  private String mr_no;
  private String full_name;
  private String patient_name;
  private String middle_name;
  private String last_name;
  private String patient_gender;
  private String patient_phone;
  private String country_code;

  private String age_text;
  private String dob;
  private String abbreviation;

  /** The visits. */
  private List<VisitPojo> visits = new ArrayList<VisitPojo>();

  /** The bills. */
  private List<BillPojo> bills = new ArrayList<BillPojo>();

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
  public List<BillPojo> getBills() {
    return bills;
  }
  public void setBills(List<BillPojo> bills) {
    this.bills = bills;
  }
  public List<VisitPojo> getVisits() {
    return visits;
  }
  public void setVisits(List<VisitPojo> visits) {
    this.visits = visits;
  }
  public String getDob() {
    return dob;
  }
  public void setDob(String dob) {
    this.dob = dob;
  }
  public String getMr_no() {
    return mr_no;
  }
  public void setMr_no(String mr_no) {
    this.mr_no = mr_no;
  }
  public String getFull_name() {
    return full_name;
  }
  public void setFull_name(String full_name) {
    this.full_name = full_name;
  }
  public String getPatient_gender() {
    return patient_gender;
  }
  public void setPatient_gender(String patient_gender) {
    this.patient_gender = patient_gender;
  }
  public String getPatient_phone() {
    return patient_phone;
  }
  public void setPatient_phone(String patient_phone) {
    this.patient_phone = patient_phone;
  }
  public String getCountry_code() {
    return country_code;
  }
  public void setCountry_code(String country_code) {
    this.country_code = country_code;
  }
  public String getAge_text() {
    return age_text;
  }
  public void setAge_text(String age_text) {
    this.age_text = age_text;
  } 
  public String getAbbreviation() {
    return abbreviation;
  }
  public void setAbbreviation(String abbreviation) {
    this.abbreviation = abbreviation;
  }

}
