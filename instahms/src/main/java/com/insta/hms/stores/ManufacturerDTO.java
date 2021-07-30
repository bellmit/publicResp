package com.insta.hms.stores;

public class ManufacturerDTO {

  private String manufacturerCode;
  private String name;
  private String adress;
  private String city;
  private String state;
  private String country;
  private String pinNumber;
  private String phoneNumber1;
  private String phoneNumber2;
  private String faxNumber;
  private String mailId;
  private String website;
  private String operation;
  private String dactivate;
  private String manfuMnemonic;
  private boolean inventory;
  private boolean pharmacy;

  public boolean isInventory() {
    return inventory;
  }
  public void setInventory(boolean inventory) {
    this.inventory = inventory;
  }
  public boolean isPharmacy() {
    return pharmacy;
  }
  public void setPharmacy(boolean pharmacy) {
    this.pharmacy = pharmacy;
  }
  public String getDactivate() {
    return dactivate;
  }
  public void setDactivate(String dactivate) {
    this.dactivate = dactivate;
  }

  public String getAdress() {
    return adress;
  }
  public void setAdress(String adress) {
    this.adress = adress;
  }

  public String getCity() {
    return city;
  }
  public void setCity(String city) {
    this.city = city;
  }

  public String getCountry() {
    return country;
  }
  public void setCountry(String country) {
    this.country = country;
  }

  public String getFaxNumber() {
    return faxNumber;
  }
  public void setFaxNumber(String faxNumber) {
    this.faxNumber = faxNumber;
  }

  public String getMailId() {
    return mailId;
  }
  public void setMailId(String mailId) {
    this.mailId = mailId;
  }

  public String getManufacturerCode() {
    return manufacturerCode;
  }
  public void setManufacturerCode(String manufacturerCode) {
    this.manufacturerCode = manufacturerCode;
  }

  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  public String getPhoneNumber1() {
    return phoneNumber1;
  }
  public void setPhoneNumber1(String phoneNumber1) {
    this.phoneNumber1 = phoneNumber1;
  }

  public String getPhoneNumber2() {
    return phoneNumber2;
  }
  public void setPhoneNumber2(String phoneNumber2) {
    this.phoneNumber2 = phoneNumber2;
  }

  public String getPinNumber() {
    return pinNumber;
  }
  public void setPinNumber(String pinNumber) {
    this.pinNumber = pinNumber;
  }

  public String getState() {
    return state;
  }
  public void setState(String state) {
    this.state = state;
  }

  public String getWebsite() {
    return website;
  }
  public void setWebsite(String website) {
    this.website = website;
  }

  public String getOperation() {
    return operation;
  }
  public void setOperation(String operation) {
    this.operation = operation;
  }
  public String getManfuMnemonic() {
    return manfuMnemonic;
  }
  public void setManfuMnemonic(String manfuMnemonic) {
    this.manfuMnemonic = manfuMnemonic;
  }

}
