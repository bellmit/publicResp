package com.insta.hms.integration.practo.pojos;

public class Specializations {
  private String id;

  private Sub_specialities[] sub_specialities;

  private String speciality;

  private String modified_at;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Sub_specialities[] getSub_specialities() {
    return sub_specialities;
  }

  public void setSub_specialities(Sub_specialities[] sub_specialities) {
    this.sub_specialities = sub_specialities;
  }

  public String getSpeciality() {
    return speciality;
  }

  public void setSpeciality(String speciality) {
    this.speciality = speciality;
  }

  public String getModified_at() {
    return modified_at;
  }

  public void setModified_at(String modified_at) {
    this.modified_at = modified_at;
  }

  @Override
  public String toString() {
    return "ClassPojo [id = " + id + ", sub_specialities = " + sub_specialities + ", speciality = "
        + speciality + ", modified_at = " + modified_at + "]";
  }

}
