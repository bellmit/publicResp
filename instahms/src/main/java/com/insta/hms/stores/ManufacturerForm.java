package com.insta.hms.stores;

import org.apache.struts.action.ActionForm;

public class ManufacturerForm extends ActionForm {

  private String manfu_code;
  private String manfu_name;
  private String manfu_address;
  private String manfu_address1;
  private String manfu_city;
  private String manfu_country;
  private String manfu_phone1;
  private String manfu_fax;
  private String manfu_email;
  private String manfu_state;
  private String manfu_pincode;
  private String manfu_phone2;
  private String manfu_website;
  private String operation;
  private String search_manfName;
  private String dactivate;
  private String manfu_mnemonic;
  private String statusAll;
  private String statusActive;
  private String statusInActive;
  private String search_manfcode;
  private String status;
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
  public String getManfu_address() {
    return manfu_address;
  }
  public void setManfu_address(String manfu_address) {
    this.manfu_address = manfu_address;
  }

  public String getManfu_address1() {
    return manfu_address1;
  }
  public void setManfu_address1(String manfu_address1) {
    this.manfu_address1 = manfu_address1;
  }

  public String getManfu_city() {
    return manfu_city;
  }
  public void setManfu_city(String manfu_city) {
    this.manfu_city = manfu_city;
  }

  public String getManfu_code() {
    return manfu_code;
  }
  public void setManfu_code(String manfu_code) {
    this.manfu_code = manfu_code;
  }

  public String getManfu_country() {
    return manfu_country;
  }
  public void setManfu_country(String manfu_country) {
    this.manfu_country = manfu_country;
  }

  public String getManfu_email() {
    return manfu_email;
  }
  public void setManfu_email(String manfu_email) {
    this.manfu_email = manfu_email;
  }

  public String getManfu_fax() {
    return manfu_fax;
  }
  public void setManfu_fax(String manfu_fax) {
    this.manfu_fax = manfu_fax;
  }

  public String getManfu_name() {
    return manfu_name;
  }
  public void setManfu_name(String manfu_name) {
    this.manfu_name = manfu_name;
  }

  public String getManfu_phone1() {
    return manfu_phone1;
  }
  public void setManfu_phone1(String manfu_phone1) {
    this.manfu_phone1 = manfu_phone1;
  }

  public String getManfu_phone2() {
    return manfu_phone2;
  }
  public void setManfu_phone2(String manfu_phone2) {
    this.manfu_phone2 = manfu_phone2;
  }

  public String getManfu_pincode() {
    return manfu_pincode;
  }
  public void setManfu_pincode(String manfu_pincode) {
    this.manfu_pincode = manfu_pincode;
  }

  public String getManfu_state() {
    return manfu_state;
  }
  public void setManfu_state(String manfu_state) {
    this.manfu_state = manfu_state;
  }

  public String getManfu_website() {
    return manfu_website;
  }
  public void setManfu_website(String manfu_website) {
    this.manfu_website = manfu_website;
  }

  public String getOperation() {
    return operation;
  }
  public void setOperation(String operation) {
    this.operation = operation;
  }

  public String getSearch_manfName() {
    return search_manfName;
  }
  public void setSearch_manfName(String search_manfName) {
    this.search_manfName = search_manfName;
  }

  public String getDactivate() {
    return dactivate;
  }
  public void setDactivate(String dactivate) {
    this.dactivate = dactivate;
  }
  public String getManfu_mnemonic() {
    return manfu_mnemonic;
  }
  public void setManfu_mnemonic(String manfu_mnemonic) {
    this.manfu_mnemonic = manfu_mnemonic;
  }
  public String getStatusActive() {
    return statusActive;
  }
  public void setStatusActive(String statusActive) {
    this.statusActive = statusActive;
  }
  public String getStatusAll() {
    return statusAll;
  }
  public void setStatusAll(String statusAll) {
    this.statusAll = statusAll;
  }
  public String getStatusInActive() {
    return statusInActive;
  }
  public void setStatusInActive(String statusInActive) {
    this.statusInActive = statusInActive;
  }
  public String getSearch_manfcode() {
    return search_manfcode;
  }
  public void setSearch_manfcode(String search_manfcode) {
    this.search_manfcode = search_manfcode;
  }
  public String getStatus() {
    return status;
  }
  public void setStatus(String status) {
    this.status = status;
  }

}
