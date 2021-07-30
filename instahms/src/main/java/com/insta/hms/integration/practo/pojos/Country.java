package com.insta.hms.integration.practo.pojos;

public class Country {
  private String isd_code;

  private String id;

  private String tzdata_identifier;

  private String tz_offset;

  private String name;

  private String helpline_number;

  private String country_code;

  private String currency;

  private String created_at;

  public String getCreated_at() {
    return created_at;
  }

  public void setCreated_at(String created_at) {
    this.created_at = created_at;
  }

  public String getModified_at() {
    return modified_at;
  }

  public void setModified_at(String modified_at) {
    this.modified_at = modified_at;
  }

  private String modified_at;

  public String getIsd_code() {
    return isd_code;
  }

  public void setIsd_code(String isd_code) {
    this.isd_code = isd_code;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTzdata_identifier() {
    return tzdata_identifier;
  }

  public void setTzdata_identifier(String tzdata_identifier) {
    this.tzdata_identifier = tzdata_identifier;
  }

  public String getTz_offset() {
    return tz_offset;
  }

  public void setTz_offset(String tz_offset) {
    this.tz_offset = tz_offset;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getHelpline_number() {
    return helpline_number;
  }

  public void setHelpline_number(String helpline_number) {
    this.helpline_number = helpline_number;
  }

  public String getCountry_code() {
    return country_code;
  }

  public void setCountry_code(String country_code) {
    this.country_code = country_code;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  @Override
  public String toString() {
    return "ClassPojo [id = " + id + ", tzdata_identifier = " + tzdata_identifier + ", tz_offset = "
        + tz_offset + ", name = " + name + ", helpline_number = " + helpline_number
        + ", country_code = " + country_code + ", currency = " + currency + "]";
  }
}
