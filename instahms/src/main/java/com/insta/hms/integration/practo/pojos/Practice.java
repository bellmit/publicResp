package com.insta.hms.integration.practo.pojos;

public class Practice {
  private String summary;

  private String website;

  private Timings timings;

  private String mapped_practice_id;

  private String mapped_service;

  private String name;

  private Locality locality;

  private String longitude;

  private String latitude;

  private String new_slug;

  private String street_address;

  private String vn_active;

  private String tagline;

  private String locality_id;

  private String city_id;

  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }

  public String getWebsite() {
    return website;
  }

  public void setWebsite(String website) {
    this.website = website;
  }

  public Timings getTimings() {
    return timings;
  }

  public void setTimings(Timings timings) {
    this.timings = timings;
  }

  public String getMapped_practice_id() {
    return mapped_practice_id;
  }

  public void setMapped_practice_id(String mapped_practice_id) {
    this.mapped_practice_id = mapped_practice_id;
  }

  public String getMapped_service() {
    return mapped_service;
  }

  public void setMapped_service(String mapped_service) {
    this.mapped_service = mapped_service;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Locality getLocality() {
    return locality;
  }

  public void setLocality(Locality locality) {
    this.locality = locality;
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

  public String getNew_slug() {
    return new_slug;
  }

  public void setNew_slug(String new_slug) {
    this.new_slug = new_slug;
  }

  public String getStreet_address() {
    return street_address;
  }

  public void setStreet_address(String street_address) {
    this.street_address = street_address;
  }

  public String getVn_active() {
    return vn_active;
  }

  public void setVn_active(String vn_active) {
    this.vn_active = vn_active;
  }

  public String getTagline() {
    return tagline;
  }

  public void setTagline(String tagline) {
    this.tagline = tagline;
  }

  public String getLocality_id() {
    return locality_id;
  }

  public void setLocality_id(String locality_id) {
    this.locality_id = locality_id;
  }

  public String getCity_id() {
    return city_id;
  }

  public void setCity_id(String city_id) {
    this.city_id = city_id;
  }

  @Override
  public String toString() {
    return "ClassPojo [summary = " + summary + ", website = " + website + ", timings = " + timings
        + ", mapped_practice_id = " + mapped_practice_id + ", mapped_service = " + mapped_service
        + ", name = " + name + ", locality = " + locality + ", longitude = " + longitude
        + ", latitude = " + latitude + ", new_slug = " + new_slug + ", street_address = "
        + street_address + ", vn_active = " + vn_active + ", tagline = " + tagline + "]";
  }

}
