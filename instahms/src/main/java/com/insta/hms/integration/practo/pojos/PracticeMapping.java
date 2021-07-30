package com.insta.hms.integration.practo.pojos;

public class PracticeMapping {

  private String id;

  private String created_at;

  private String mapped_practice_id;

  private String practo_practice_id;

  private String modified_at;

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

  public String getMapped_practice_id() {
    return mapped_practice_id;
  }

  public void setMapped_practice_id(String mapped_practice_id) {
    this.mapped_practice_id = mapped_practice_id;
  }

  public String getPracto_practice_id() {
    return practo_practice_id;
  }

  public void setPracto_practice_id(String practo_practice_id) {
    this.practo_practice_id = practo_practice_id;
  }

  public String getModified_at() {
    return modified_at;
  }

  public void setModified_at(String modified_at) {
    this.modified_at = modified_at;
  }

  public String getMapped_service() {
    return mapped_service;
  }

  public void setMapped_service(String mapped_service) {
    this.mapped_service = mapped_service;
  }

  @Override
  public String toString() {
    return "ClassPojo [id = " + id + ", created_at = " + created_at + ", mapped_practice_id = "
        + mapped_practice_id + ", practo_practice_id = " + practo_practice_id + ", modified_at = "
        + modified_at + ", mapped_service = " + mapped_service + "]";
  }

}
