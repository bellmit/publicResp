package com.insta.hms.integration.practo.pojos;

public class Locality {
  private String name;

  private City city;

  private String id;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public City getCity() {
    return city;
  }

  public void setCity(City city) {
    this.city = city;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Override
  public String toString() {
    return "ClassPojo [name = " + name + ", city = " + city + "]";
  }
}
