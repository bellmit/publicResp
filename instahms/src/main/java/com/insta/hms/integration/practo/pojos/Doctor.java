package com.insta.hms.integration.practo.pojos;

public class Doctor {

  private String id;

  private String summary;

  private String[] qualifications;

  private String practicing_start_year;

  private String primary_email_address;

  private String primary_contact_number;

  private DoctorSpecializations specializations;

  private String locality_id;

  private Phones phones;

  private String city_id;

  private String name;

  private String source_description;

  private String gender;

  private String[] registrations;

  private String published;

  private String[] services;

  private String[] organizations;

  private String date_of_birth;

  private Emails emails;

  private String mapped_service;

  private String[] photos;

  private String source;

  private String[] relations;

  private String[] awards;

  private String verification_status;

  private String[] memberships;

  private String notes;

  private String street_address;

  private String mapped_doctor_id;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }

  public String[] getQualifications() {
    return qualifications;
  }

  public void setQualifications(String[] qualifications) {
    this.qualifications = qualifications;
  }

  public String getPracticing_start_year() {
    return practicing_start_year;
  }

  public void setPracticing_start_year(String practicing_start_year) {
    this.practicing_start_year = practicing_start_year;
  }

  public String getPrimary_email_address() {
    return primary_email_address;
  }

  public void setPrimary_email_address(String primary_email_address) {
    this.primary_email_address = primary_email_address;
  }

  public String getPrimary_contact_number() {
    return primary_contact_number;
  }

  public void setPrimary_contact_number(String primary_contact_number) {
    this.primary_contact_number = primary_contact_number;
  }

  public DoctorSpecializations getSpecializations() {
    return specializations;
  }

  public void setSpecializations(DoctorSpecializations specializations) {
    this.specializations = specializations;
  }

  public String getLocality_id() {
    return locality_id;
  }

  public void setLocality_id(String locality_id) {
    this.locality_id = locality_id;
  }

  public Phones getPhones() {
    return phones;
  }

  public void setPhones(Phones phones) {
    this.phones = phones;
  }

  public String getCity_id() {
    return city_id;
  }

  public void setCity_id(String city_id) {
    this.city_id = city_id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSource_description() {
    return source_description;
  }

  public void setSource_description(String source_description) {
    this.source_description = source_description;
  }

  public String getGender() {
    return gender;
  }

  public void setGender(String gender) {
    this.gender = gender;
  }

  public String[] getRegistrations() {
    return registrations;
  }

  public void setRegistrations(String[] registrations) {
    this.registrations = registrations;
  }

  public String getPublished() {
    return published;
  }

  public void setPublished(String published) {
    this.published = published;
  }

  public String[] getServices() {
    return services;
  }

  public void setServices(String[] services) {
    this.services = services;
  }

  public String[] getOrganizations() {
    return organizations;
  }

  public void setOrganizations(String[] organizations) {
    this.organizations = organizations;
  }

  public String getDate_of_birth() {
    return date_of_birth;
  }

  public void setDate_of_birth(String date_of_birth) {
    this.date_of_birth = date_of_birth;
  }

  public Emails getEmails() {
    return emails;
  }

  public void setEmails(Emails emails) {
    this.emails = emails;
  }

  public String getMapped_service() {
    return mapped_service;
  }

  public void setMapped_service(String mapped_service) {
    this.mapped_service = mapped_service;
  }

  public String[] getPhotos() {
    return photos;
  }

  public void setPhotos(String[] photos) {
    this.photos = photos;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String[] getRelations() {
    return relations;
  }

  public void setRelations(String[] relations) {
    this.relations = relations;
  }

  public String[] getAwards() {
    return awards;
  }

  public void setAwards(String[] awards) {
    this.awards = awards;
  }

  public String getVerification_status() {
    return verification_status;
  }

  public void setVerification_status(String verification_status) {
    this.verification_status = verification_status;
  }

  public String[] getMemberships() {
    return memberships;
  }

  public void setMemberships(String[] memberships) {
    this.memberships = memberships;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public String getStreet_address() {
    return street_address;
  }

  public void setStreet_address(String street_address) {
    this.street_address = street_address;
  }

  public String getMapped_doctor_id() {
    return mapped_doctor_id;
  }

  public void setMapped_doctor_id(String mapped_doctor_id) {
    this.mapped_doctor_id = mapped_doctor_id;
  }

  @Override
  public String toString() {
    return "ClassPojo [summary = " + summary + ", qualifications = " + qualifications
        + ", practicing_start_year = " + practicing_start_year + ", primary_email_address = "
        + primary_email_address + ", primary_contact_number = " + primary_contact_number
        + ", specializations = " + specializations + ", locality_id = " + locality_id
        + ", phones = " + phones + ", city_id = " + city_id + ", name = " + name
        + ", source_description = " + source_description + ", gender = " + gender
        + ", registrations = " + registrations + ", published = " + published + ", services = "
        + services + ", organizations = " + organizations + ", date_of_birth = " + date_of_birth
        + ", emails = " + emails + ", mapped_service = " + mapped_service + ", photos = " + photos
        + ", source = " + source + ", relations = " + relations + ", awards = " + awards
        + ", verification_status = " + verification_status + ", memberships = " + memberships
        + ", notes = " + notes + ", street_address = " + street_address + ", mapped_doctor_id = "
        + mapped_doctor_id + "]";
  }

}
