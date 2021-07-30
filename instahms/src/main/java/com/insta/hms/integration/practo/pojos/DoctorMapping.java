package com.insta.hms.integration.practo.pojos;

public class DoctorMapping {
  private String id;

  private String created_at;

  private String practo_doctor_id;

  private String modified_at;

  private String mapped_doctor_id;

  private String mapped_service;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getCreated_at() {
    return created_at;
  }

  public void setCreated_at(String created_at) {
    this.created_at = created_at;
  }

  public String getPracto_doctor_id() {
    return practo_doctor_id;
  }

  public void setPracto_doctor_id(String practo_doctor_id) {
    this.practo_doctor_id = practo_doctor_id;
  }

  public String getModified_at() {
    return modified_at;
  }

  public void setModified_at(String modified_at) {
    this.modified_at = modified_at;
  }

  public String getMapped_doctor_id() {
    return mapped_doctor_id;
  }

  public void setMapped_doctor_id(String mapped_doctor_id) {
    this.mapped_doctor_id = mapped_doctor_id;
  }

  public String getMapped_service() {
    return mapped_service;
  }

  public void setMapped_service(String mapped_service) {
    this.mapped_service = mapped_service;
  }

  @Override
  public String toString() {
    return "ClassPojo [id = " + id + ", created_at = " + created_at + ", practo_doctor_id = "
        + practo_doctor_id + ", modified_at = " + modified_at + ", mapped_doctor_id = "
        + mapped_doctor_id + ", mapped_service = " + mapped_service + "]";
  }

}
