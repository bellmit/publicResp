package com.insta.hms.integration.practo.pojos;

public class State {
  private String id;

  private String name;

  private Country country;

  private String modified_at;

  private String created_at;

  public String getCreated_at() {
    return created_at;
  }

  public void setCreated_at(String created_at) {
    this.created_at = created_at;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Country getCountry() {
    return country;
  }

  public void setCountry(Country country) {
    this.country = country;
  }

  public String getModified_at() {
    return modified_at;
  }

  public void setModified_at(String modified_at) {
    this.modified_at = modified_at;
  }

  @Override
  public String toString() {
    return "ClassPojo [id = " + id + ", name = " + name + ", country = " + country + "]";
  }
}
