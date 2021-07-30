package com.insta.hms.integration.practo.pojos;

public class Sub_specialities {
  private String id;

  private Speciality speciality;

  private String clinic_label;

  private String subspecialization;

  private String created_at;

  private String published;

  private String modified_at;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Speciality getSpeciality() {
    return speciality;
  }

  public void setSpeciality(Speciality speciality) {
    this.speciality = speciality;
  }

  public String getClinic_label() {
    return clinic_label;
  }

  public void setClinic_label(String clinic_label) {
    this.clinic_label = clinic_label;
  }

  public String getSubspecialization() {
    return subspecialization;
  }

  public void setSubspecialization(String subspecialization) {
    this.subspecialization = subspecialization;
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

  public String getModified_at() {
    return modified_at;
  }

  public void setModified_at(String modified_at) {
    this.modified_at = modified_at;
  }

  @Override
  public String toString() {
    return "ClassPojo [id = " + id + ", speciality = " + speciality + ", clinic_label = "
        + clinic_label + ", subspecialization = " + subspecialization + ", created_at = "
        + created_at + ", published = " + published + ", modified_at = " + modified_at + "]";
  }

}
