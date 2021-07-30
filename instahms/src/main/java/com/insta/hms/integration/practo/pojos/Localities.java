package com.insta.hms.integration.practo.pojos;

public class Localities {
  private String id;

  private String zipcode;

  private String name;

  private String created_at;

  private String longitude;

  private String latitude;

  private String published;

  private String modified_at;

  private City city;

  public String getRanking() {
    return ranking;
  }

  public void setRanking(String ranking) {
    this.ranking = ranking;
  }

  public String getPromoted() {
    return promoted;
  }

  public void setPromoted(String promoted) {
    this.promoted = promoted;
  }

  private String ranking;

  private String promoted;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getZipcode() {
    return zipcode;
  }

  public void setZipcode(String zipcode) {
    this.zipcode = zipcode;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCreated_at() {
    return created_at;
  }

  public void setCreated_at(String created_at) {
    this.created_at = created_at;
  }

  public String getLongitude() {
    return longitude;
  }

  public void setLongitude(String longitude) {
    this.longitude = longitude;
  }

  public String getLatitude() {
    return latitude;
  }

  public void setLatitude(String latitude) {
    this.latitude = latitude;
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

  public City getCity() {
    return city;
  }

  public void setCity(City city) {
    this.city = city;
  }

  @Override
  public String toString() {
    return "ClassPojo [id = " + id + ", zipcode = " + zipcode + ", name = " + name
        + ", created_at = " + created_at + ", longitude = " + longitude + ", latitude = " + latitude
        + ", published = " + published + ", modified_at = " + modified_at + ", city = " + city
        + "]";
  }
}
