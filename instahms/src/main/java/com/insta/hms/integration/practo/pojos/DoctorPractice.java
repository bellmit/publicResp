package com.insta.hms.integration.practo.pojos;

public class DoctorPractice {
  private String lead_days;

  private String profile_published;

  private String status;

  private String free_consultation;

  private String doctor_id;

  private String mapped_practice_id;

  private String practice_id;

  private String consultation_fee;

  private String mapped_service;

  private Abt_timings abt_timings;

  private String appointment_duration;

  private String resident_doctor;

  private Visit_timings visit_timings;

  private String published;

  private String on_call;

  private String mapped_doctor_id;

  public String getLead_days() {
    return lead_days;
  }

  public void setLead_days(String lead_days) {
    this.lead_days = lead_days;
  }

  public String getProfile_published() {
    return profile_published;
  }

  public void setProfile_published(String profile_published) {
    this.profile_published = profile_published;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getFree_consultation() {
    return free_consultation;
  }

  public void setFree_consultation(String free_consultation) {
    this.free_consultation = free_consultation;
  }

  public String getDoctor_id() {
    return doctor_id;
  }

  public void setDoctor_id(String doctor_id) {
    this.doctor_id = doctor_id;
  }

  public String getMapped_practice_id() {
    return mapped_practice_id;
  }

  public void setMapped_practice_id(String mapped_practice_id) {
    this.mapped_practice_id = mapped_practice_id;
  }

  public String getPractice_id() {
    return practice_id;
  }

  public void setPractice_id(String practice_id) {
    this.practice_id = practice_id;
  }

  public String getConsultation_fee() {
    return consultation_fee;
  }

  public void setConsultation_fee(String consultation_fee) {
    this.consultation_fee = consultation_fee;
  }

  public String getMapped_service() {
    return mapped_service;
  }

  public void setMapped_service(String mapped_service) {
    this.mapped_service = mapped_service;
  }

  public Abt_timings getAbt_timings() {
    return abt_timings;
  }

  public void setAbt_timings(Abt_timings abt_timings) {
    this.abt_timings = abt_timings;
  }

  public String getAppointment_duration() {
    return appointment_duration;
  }

  public void setAppointment_duration(String appointment_duration) {
    this.appointment_duration = appointment_duration;
  }

  public String getResident_doctor() {
    return resident_doctor;
  }

  public void setResident_doctor(String resident_doctor) {
    this.resident_doctor = resident_doctor;
  }

  public Visit_timings getVisit_timings() {
    return visit_timings;
  }

  public void setVisit_timings(Visit_timings visit_timings) {
    this.visit_timings = visit_timings;
  }

  public String getPublished() {
    return published;
  }

  public void setPublished(String published) {
    this.published = published;
  }

  public String getOn_call() {
    return on_call;
  }

  public void setOn_call(String on_call) {
    this.on_call = on_call;
  }

  public String getMapped_doctor_id() {
    return mapped_doctor_id;
  }

  public void setMapped_doctor_id(String mapped_doctor_id) {
    this.mapped_doctor_id = mapped_doctor_id;
  }

  @Override
  public String toString() {
    return "ClassPojo [lead_days = " + lead_days + ", profile_published = " + profile_published
        + ", status = " + status + ", free_consultation = " + free_consultation + ", doctor_id = "
        + doctor_id + ", mapped_practice_id = " + mapped_practice_id + ", practice_id = "
        + practice_id + ", consultation_fee = " + consultation_fee + ", mapped_service = "
        + mapped_service + ", abt_timings = " + abt_timings + ", appointment_duration = "
        + appointment_duration + ", resident_doctor = " + resident_doctor + ", visit_timings = "
        + visit_timings + ", published = " + published + ", on_call = " + on_call
        + ", mapped_doctor_id = " + mapped_doctor_id + "]";
  }

}
