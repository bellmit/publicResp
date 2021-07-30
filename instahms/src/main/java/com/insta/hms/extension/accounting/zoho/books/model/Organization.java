/* $Id$ */

package com.insta.hms.extension.accounting.zoho.books.model;

import org.json.JSONObject;

/**
 * This class is used to make an object for organization.
 */

public class Organization {

  /** The organization id. */
  private String organizationId = "";

  /** The name. */
  private String name = "";

  /** The is default org. */
  private boolean isDefaultOrg = false;

  /** The account created date. */
  private String accountCreatedDate = "";

  /** The time zone. */
  private String timeZone = "";

  /** The language code. */
  private String languageCode = "en"; // No I18N

  /** The date format. */
  private String dateFormat = "";

  /** The field separator. */
  private String fieldSeparator = "";

  /** The fiscal year start month. */
  private String fiscalYearStartMonth = "";

  /** The contact name. */
  private String contactName = "";

  /** The industry type. */
  private String industryType = "";

  /** The industry size. */
  private String industrySize = "";

  /** The company id label. */
  private String companyIdLabel = "";

  /** The company id value. */
  private String companyIdValue = "";

  /** The tax id label. */
  private String taxIdLabel = "";

  /** The tax id value. */
  private String taxIdValue = "";

  /** The currency id. */
  private String currencyId = "";

  /** The currency code. */
  private String currencyCode = "";

  /** The currency symbol. */
  private String currencySymbol = "";

  /** The currency format. */
  private String currencyFormat = "";

  /** The price precision. */
  private int pricePrecision = 0;

  /** The org address. */
  private String orgAddress = "";

  /** The remit to address. */
  private String remitToAddress = "";

  /** The phone. */
  private String phone = "";

  /** The fax. */
  private String fax = "";

  /** The website. */
  private String website = "";

  /** The email. */
  private String email = "";

  /** The tax basis. */
  private String taxBasis = "";

  /** The is org active. */
  private boolean isOrgActive = false;

  /** The plan type. */
  private String planType = "";

  /** The tax group enabled. */
  private boolean taxGroupEnabled = false;

  /** The plan name. */
  private String planName = "";

  /** The plan period. */
  private String planPeriod = "";

  /** The account createddate formatted. */
  private String accountCreateddateFormatted = "";

  /** The is logo uploaded. */
  private boolean isLogoUploaded = false;

  /** The value. */
  private String value = "";

  /** The default value. */
  private String defaultValue = "";

  /** The address. */
  private Address address = new Address();

  /**
   * set the organization id.
   * 
   * @param organizationId
   *          ID of the organization.
   */

  public void setOrganizationId(String organizationId) {
    this.organizationId = organizationId;
  }

  /**
   * get the organization id.
   * 
   * @return Returns the ID of the organization.
   */

  public String getOrganizationId() {
    return organizationId;
  }

  /**
   * set the name.
   * 
   * @param name
   *          Name of the organization.
   */

  public void setName(String name) {
    this.name = name;
  }

  /**
   * get the name.
   * 
   * @return Returns the name of the organization.
   */

  public String getName() {
    return name;
  }

  /**
   * set is default org.
   * 
   * @param isDefaultOrg
   *          To determine whether the orgis default or not.
   */

  public void setIsDefaultOrg(boolean isDefaultOrg) {
    this.isDefaultOrg = isDefaultOrg;
  }

  /**
   * get is default org.
   * 
   * @return Returns true if the org is default else returns false.
   */

  public boolean isDefaultOrg() {
    return isDefaultOrg;
  }

  /**
   * set the account created date.
   * 
   * @param accountCreatedDate
   *          Account created date for the org.
   */

  public void setAccountCreatedDate(String accountCreatedDate) {
    this.accountCreatedDate = accountCreatedDate;
  }

  /**
   * get the account created date.
   * 
   * @return Returns the account created date for the org.
   */

  public String getAccountCreatedDate() {
    return accountCreatedDate;
  }

  /**
   * set the time zone.
   * 
   * @param timeZone
   *          Time zone in with the organization is located geographically.
   */

  public void setTimeZone(String timeZone) {
    this.timeZone = timeZone;
  }

  /**
   * get the time zone.
   * 
   * @return Returns the time zone in with the organization is located geographically.
   */

  public String getTimeZone() {
    return timeZone;
  }

  /**
   * set the language code.
   * 
   * @param languageCode
   *          language code for organization.For instance en represents english.
   */

  public void setLanguageCode(String languageCode) {
    this.languageCode = languageCode;
  }

  /**
   * get the language code.
   * 
   * @return Returns the language code for organization.
   */

  public String getLanguageCode() {
    return languageCode;
  }

  /**
   * set the date format.
   * 
   * @param dateFormat
   *          Format for representing the date.
   */

  public void setDateFormat(String dateFormat) {
    this.dateFormat = dateFormat;
  }

  /**
   * get the date format.
   * 
   * @return Returns the format for representing the date.
   */

  public String getDateFormat() {
    return dateFormat;
  }

  /**
   * set the field separator.
   * 
   * @param fieldSeparator
   *          Field separator for components in date.
   */

  public void setFieldSeparator(String fieldSeparator) {
    this.fieldSeparator = fieldSeparator;
  }

  /**
   * get the field separator.
   * 
   * @return Returns the field separator for components in date.
   */

  public String getFieldSeparator() {
    return fieldSeparator;
  }

  /**
   * set the fiscal year start month.
   * 
   * @param fiscalYearStartMonth
   *          Fiscal or financial starting year of your business.
   */

  public void setFiscalYearStartMonth(String fiscalYearStartMonth) {
    this.fiscalYearStartMonth = fiscalYearStartMonth;
  }

  /**
   * get the fiscal year start month.
   * 
   * @return Returns the fiscal or financial starting year of your business.
   */

  public String getFiscalYearStartMonth() {
    return fiscalYearStartMonth;
  }

  /**
   * set the contact name.
   * 
   * @param contactName
   *          Contact name of an organization.
   */

  public void setContactName(String contactName) {
    this.contactName = contactName;
  }

  /**
   * get the contact name.
   * 
   * @return Returns the Contact name of an organization.
   */

  public String getContactName() {
    return contactName;
  }

  /**
   * set the industry type.
   * 
   * @param industryType
   *          Type of the industry.
   */

  public void setIndustryType(String industryType) {
    this.industryType = industryType;
  }

  /**
   * get the industry type.
   * 
   * @return Returns the type of the industry.
   */

  public String getIndustryType() {
    return industryType;
  }

  /**
   * set the industry size.
   * 
   * @param industrySize
   *          Size of the industry.
   */

  public void setIndustrySize(String industrySize) {
    this.industrySize = industrySize;
  }

  /**
   * get the industry size.
   * 
   * @return Returns the size of the industry.
   */

  public String getIndustrySize() {
    return industrySize;
  }

  /**
   * set the company id label.
   * 
   * @param companyIdLabel
   *          Label of the company id.
   */

  public void setCompanyIdLabel(String companyIdLabel) {
    this.companyIdLabel = companyIdLabel;
  }

  /**
   * get the company id label.
   * 
   * @return Returns the label of the company id.
   */

  public String getCompanyIdLabel() {
    return companyIdLabel;
  }

  /**
   * set the company id value.
   * 
   * @param companyIdValue
   *          Value of the company id.
   */

  public void setCompanyIdValue(String companyIdValue) {
    this.companyIdValue = companyIdValue;
  }

  /**
   * get the company id value.
   * 
   * @return Returns the value of the company id.
   */

  public String getCompanyIdValue() {
    return companyIdValue;
  }

  /**
   * set the tax id label.
   * 
   * @param taxIdLabel
   *          Label of the tax id.
   */

  public void setTaxIdLabel(String taxIdLabel) {
    this.taxIdLabel = taxIdLabel;
  }

  /**
   * get the tax id label.
   * 
   * @return Returns the label of the tax id.
   */

  public String getTaxIdLabel() {
    return taxIdLabel;
  }

  /**
   * set the tax id value.
   * 
   * @param taxIdValue
   *          Value of the tax id.
   */

  public void setTaxIdValue(String taxIdValue) {
    this.taxIdValue = taxIdValue;
  }

  /**
   * get the tax id value.
   * 
   * @return Returns the value of the tax id.
   */

  public String getTaxIdValue() {
    return taxIdValue;
  }

  /**
   * set the currency id.
   * 
   * @param currencyId
   *          ID of the currency.
   */

  public void setCurrencyId(String currencyId) {
    this.currencyId = currencyId;
  }

  /**
   * get the currency id.
   * 
   * @return Returns the ID of the currency.
   */

  public String getCurrencyId() {
    return currencyId;
  }

  /**
   * set the currency code.
   * 
   * @param currencyCode
   *          Standard code for currency.
   */

  public void setCurrencyCode(String currencyCode) {
    this.currencyCode = currencyCode;
  }

  /**
   * get the currency code.
   * 
   * @return Returns the standard code for the currency.
   */

  public String getCurrencyCode() {
    return currencyCode;
  }

  /**
   * set the currency symbol.
   * 
   * @param currencySymbol
   *          Symbol of the currency.
   */

  public void setCurrencySymbol(String currencySymbol) {
    this.currencySymbol = currencySymbol;
  }

  /**
   * get the currency symbol.
   * 
   * @return Returns the symbol for the currency.
   */

  public String getCurrencySymbol() {
    return currencySymbol;
  }

  /**
   * set the currency format.
   * 
   * @param currencyFormat
   *          Format of the currency.
   */

  public void setCurrencyFormat(String currencyFormat) {
    this.currencyFormat = currencyFormat;
  }

  /**
   * get the currency format.
   * 
   * @return Returns the format of the currency.
   */

  public String getCurrencyFormat() {
    return currencyFormat;
  }

  /**
   * set the price precision.
   * 
   * @param pricePrecision
   *          Price precision of the organization.
   */

  public void setPricePrecision(int pricePrecision) {
    this.pricePrecision = pricePrecision;
  }

  /**
   * get the price precision.
   * 
   * @return Returns the price precision of the organization.
   */

  public int getPricePrecision() {
    return pricePrecision;
  }

  /**
   * set the org address.
   * 
   * @param orgAddress
   *          Address of the organization.
   */

  public void setOrgAddress(String orgAddress) {
    this.orgAddress = orgAddress;
  }

  /**
   * get the org address.
   * 
   * @return Returns the address of the organization.
   */

  public String getOrgAddress() {
    return orgAddress;
  }

  /**
   * set the remit to address.
   * 
   * @param remitToAddress
   *          Remit address of the organization.
   */

  public void setRemitToAddress(String remitToAddress) {
    this.remitToAddress = remitToAddress;
  }

  /**
   * get the remit to address.
   * 
   * @return Returns the remit address of the organization.
   */

  public String getRemitToAddress() {
    return remitToAddress;
  }

  /**
   * set the phone number.
   * 
   * @param phone
   *          Phone number of the organization.
   */

  public void setPhone(String phone) {
    this.phone = phone;
  }

  /**
   * get the phone.
   * 
   * @return Returns the phone number of the organization.
   */

  public String getPhone() {
    return phone;
  }

  /**
   * set the fax.
   * 
   * @param fax
   *          Fax of the organization.
   */

  public void setFax(String fax) {
    this.fax = fax;
  }

  /**
   * get the fax.
   * 
   * @return Returns the fax of the organization.
   */

  public String getFax() {
    return fax;
  }

  /**
   * set the website.
   * 
   * @param website
   *          Website of the organization.
   */

  public void setWebsite(String website) {
    this.website = website;
  }

  /**
   * get the website.
   * 
   * @return Returns the website of the organization.
   */

  public String getWebsite() {
    return website;
  }

  /**
   * set the email.
   * 
   * @param email
   *          Email of the organization.
   */

  public void setEmail(String email) {
    this.email = email;
  }

  /**
   * get the email.
   * 
   * @return Returns the email of the organization.
   */

  public String getEmail() {
    return email;
  }

  /**
   * set the tax basis.
   * 
   * @param taxBasis
   *          Tax basis based on accrual(tax as of invoice date) or cash(tax upon payment receipt).
   */

  public void setTaxBasis(String taxBasis) {
    this.taxBasis = taxBasis;
  }

  /**
   * get the tax basis.
   * 
   * @return Returns the tax basis based on accrual or cash.
   */

  public String getTaxBasis() {
    return taxBasis;
  }

  /**
   * set is org active.
   * 
   * @param isOrgActive
   *          To determine whether the org is enable or disable.
   */

  public void setIsOrgActive(boolean isOrgActive) {
    this.isOrgActive = isOrgActive;
  }

  /**
   * get is org active.
   * 
   * @return Returns true if the org is enable else return false.
   */

  public boolean isOrgActive() {
    return isOrgActive;
  }

  /**
   * set the plan type.
   * 
   * @param planType
   *          Plan type of the organization.
   */

  public void setPlanType(String planType) {
    this.planType = planType;
  }

  /**
   * get the plan type.
   * 
   * @return Returns the plan type of the organization.
   */

  public String getPlanType() {
    return planType;
  }

  /**
   * set tax group enabled.
   * 
   * @param taxGroupEnabled
   *          To determine wether the tax group is enable or disable.
   */

  public void setTaxGroupEnabled(boolean taxGroupEnabled) {
    this.taxGroupEnabled = taxGroupEnabled;
  }

  /**
   * get tax group enabled.
   * 
   * @return Returns true if tax group is enabled else returns false.
   */

  public boolean isTaxGroupEnabled() {
    return taxGroupEnabled;
  }

  /**
   * set the plan name.
   * 
   * @param planName
   *          Plan name of the organization.
   */

  public void setPlanName(String planName) {
    this.planName = planName;
  }

  /**
   * get the plan name.
   * 
   * @return Returns the plan name of the organization.
   */

  public String getPlanName() {
    return planName;
  }

  /**
   * set the plan period.
   * 
   * @param planPeriod
   *          Plan period of the organization.
   */

  public void setPlanPeriod(String planPeriod) {
    this.planPeriod = planPeriod;
  }

  /**
   * get the plan period.
   * 
   * @return Returns the plan period of the organization.
   */

  public String getPlanPeriod() {
    return planPeriod;
  }

  /**
   * set the account created date format.
   * 
   * @param accountCreateddateFormatted
   *          Format of the account created date.
   */

  public void setAccountCreatedDateFormatted(String accountCreateddateFormatted) {
    this.accountCreateddateFormatted = accountCreateddateFormatted;
  }

  /**
   * get the account created date format.
   * 
   * @return Returns the format of the account created date.
   */

  public String getAccountCreatedDateFormatted() {
    return accountCreateddateFormatted;
  }

  /**
   * set is logo uploaded.
   * 
   * @param isLogoUploaded
   *          To determine whether the logo is uploaded or not.
   */

  public void setIsLogoUploaded(boolean isLogoUploaded) {
    this.isLogoUploaded = isLogoUploaded;
  }

  /**
   * get is logo uploaded.
   * 
   * @return Returns true if the logo is uploaded else returns false.
   */

  public boolean isLogoUploaded() {
    return isLogoUploaded;
  }

  /**
   * set the value.
   * 
   * @param value
   *          Value for the organization.
   */

  public void setValue(String value) {
    this.value = value;
  }

  /**
   * get the value.
   * 
   * @return Returns the value for the organization.
   */

  public String getValue() {
    return value;
  }

  /**
   * set the default value.
   * 
   * @param defaultValue
   *          Default value for the organization.
   */

  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }

  /**
   * get the default value.
   * 
   * @return Returns the default value for the organization.
   */

  public String getDefaultValue() {
    return defaultValue;
  }

  /**
   * set the address.
   *
   * @param address
   *          Address for the organization.
   * @throws Exception
   *           the exception
   */

  public void setAddress(Address address) throws Exception {
    this.address = address;
  }

  /**
   * get the address.
   * 
   * @return Returns an Address object.
   */

  public Address getAddress() {
    return address;
  }

  /**
   * Convert Organization object into JSONObject.
   *
   * @return Returns a JSONObject.
   * @throws Exception
   *           the exception
   */

  public JSONObject toJSON() throws Exception {
    JSONObject jsonObject = new JSONObject();

    jsonObject.put("name", name);
    jsonObject.put("is_logo_uploaded", isLogoUploaded);
    jsonObject.put("industry_type", industryType);
    jsonObject.put("industry_size", industrySize);

    if (address != null) {
      jsonObject.put("address", address.toJSON());
    }

    jsonObject.put("org_address", orgAddress);
    jsonObject.put("remit_to_address", remitToAddress);
    jsonObject.put("contact_name", contactName);
    jsonObject.put("phone", phone);
    jsonObject.put("fax", fax);
    jsonObject.put("website", website);
    jsonObject.put("email", email);
    jsonObject.put("fiscal_year_start_month", fiscalYearStartMonth);
    jsonObject.put("currency_code", currencyCode);
    jsonObject.put("time_zone", timeZone);
    jsonObject.put("date_format", dateFormat);
    jsonObject.put("field_separator", fieldSeparator);
    jsonObject.put("language_code", languageCode);
    jsonObject.put("company_id_label", companyIdLabel);
    jsonObject.put("company_id_value", companyIdValue);
    jsonObject.put("tax_id_label", taxIdLabel);
    jsonObject.put("tax_id_value", taxIdValue);
    jsonObject.put("tax_basis", taxBasis);

    return jsonObject;
  }

}
