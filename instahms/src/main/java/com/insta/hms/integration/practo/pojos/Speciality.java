package com.insta.hms.integration.practo.pojos;

public class Speciality {
  private String id;

  private String speciality;

  private String promoted;

  private String clinic_label;

  private String valid_countries;

  private String doctor_label;

  private String created_at;

  private String published;

  private String practice_label;

  private String patient_label;

  private String ranking;

  private String modified_at;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getSpeciality() {
    return speciality;
  }

  public void setSpeciality(String speciality) {
    this.speciality = speciality;
  }

  public String getPromoted() {
    return promoted;
  }

  public void setPromoted(String promoted) {
    this.promoted = promoted;
  }

  public String getClinic_label() {
    return clinic_label;
  }

  public void setClinic_label(String clinic_label) {
    this.clinic_label = clinic_label;
  }

  public String getValid_countries() {
    return valid_countries;
  }

  public void setValid_countries(String valid_countries) {
    this.valid_countries = valid_countries;
  }

  public String getDoctor_label() {
    return doctor_label;
  }

  public void setDoctor_label(String doctor_label) {
    this.doctor_label = doctor_label;
  }

  public String getCreated_at() {
    return created_at;
  }

  public void setCreated_at(String created_at) {
    this.created_at = created_at;
  }

  public String getPublished() {
    return published;
  }

  public void setPublished(String published) {
    this.published = published;
  }

  public String getPractice_label() {
    return practice_label;
  }

  public void setPractice_label(String practice_label) {
    this.practice_label = practice_label;
  }

  public String getPatient_label() {
    return patient_label;
  }

  public void setPatient_label(String patient_label) {
    this.patient_label = patient_label;
  }

  public String getRanking() {
    return ranking;
  }

  public void setRanking(String ranking) {
    this.ranking = ranking;
  }

  public String getModified_at() {
    return modified_at;
  }

  public void setModified_at(String modified_at) {
    this.modified_at = modified_at;
  }

  @Override
  public String toString() {
    return "ClassPojo [id = " + id + ", speciality = " + speciality + ", promoted = " + promoted
        + ", clinic_label = " + clinic_label + ", valid_countries = " + valid_countries
        + ", doctor_label = " + doctor_label + ", created_at = " + created_at + ", published = "
        + published + ", practice_label = " + practice_label + ", patient_label = " + patient_label
        + ", ranking = " + ranking + ", modified_at = " + modified_at + "]";
  }

}
