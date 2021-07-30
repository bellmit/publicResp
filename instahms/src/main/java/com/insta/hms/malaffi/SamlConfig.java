package com.insta.hms.malaffi;

import java.util.Map;

public class SamlConfig {

  private String keyStorePath;
  private String samlResponseAudience;
  private String samlResponseRecipient;
  private String samlResponseOid;
  private Integer samlResponseNotBeforeSeconds;
  private Integer samlResponseNotOnOrAfterSeconds;
  private String ssoEndpointUrl;
  private String samlResponseOrganization;
  private String samlResponseIssuer;

  public void setConfig(Map<String, Object> config) {
    if (config == null || config.size() == 0) {
      return;
    }
    String env = ((boolean) config.get("production")) ? "prod" : "uat";
    this.setKeyStorePath((String) config.get("keystore_" + env));
    this.setSamlResponseAudience((String) config.get("audience"));
    this.setSamlResponseIssuer((String) config.get("issuer"));
    this.setSamlResponseRecipient((String) config.get("recipient"));
    this.setSamlResponseOid((String) config.get("oid"));
    this.setSamlResponseOrganization((String) config.get("organization"));
    this.setSamlResponseNotBeforeSeconds((Integer) config.get("condition_not_before"));
    this.setSamlResponseNotOnOrAfterSeconds((Integer) config.get("condition_not_on_or_after"));
    this.setSsoEndpointUrl((String) config.get("endpoint_" + env));
  }
  
  public String getKeyStorePath() {
    return keyStorePath;
  }

  public void setKeyStorePath(String keyStorePath) {
    this.keyStorePath = keyStorePath;
  }

  public String getSamlResponseAudience() {
    return samlResponseAudience;
  }

  public void setSamlResponseAudience(String samlResponseAudience) {
    this.samlResponseAudience = samlResponseAudience;
  }

  public String getSamlResponseRecipient() {
    return samlResponseRecipient;
  }

  public void setSamlResponseRecipient(String samlResponseRecipient) {
    this.samlResponseRecipient = samlResponseRecipient;
  }

  public String getSamlResponseOid() {
    return samlResponseOid;
  }

  public void setSamlResponseOid(String samlResponseOid) {
    this.samlResponseOid = samlResponseOid;
  }

  public Integer getSamlResponseNotBeforeSeconds() {
    return samlResponseNotBeforeSeconds;
  }

  public void setSamlResponseNotBeforeSeconds(Integer samlResponseNotBeforeSeconds) {
    this.samlResponseNotBeforeSeconds = samlResponseNotBeforeSeconds;
  }

  public Integer getSamlResponseNotOnOrAfterSeconds() {
    return samlResponseNotOnOrAfterSeconds;
  }

  public void setSamlResponseNotOnOrAfterSeconds(Integer samlResponseNotOnOrAfterSeconds) {
    this.samlResponseNotOnOrAfterSeconds = samlResponseNotOnOrAfterSeconds;
  }

  public String getSsoEndpointUrl() {
    return ssoEndpointUrl;
  }

  public void setSsoEndpointUrl(String ssoEndpointUrl) {
    this.ssoEndpointUrl = ssoEndpointUrl;
  }

  public String getSamlResponseOrganization() {
    return samlResponseOrganization;
  }

  public void setSamlResponseOrganization(String samlResponseOrganization) {
    this.samlResponseOrganization = samlResponseOrganization;
  }

  public String getSamlResponseIssuer() {
    return samlResponseIssuer;
  }

  public void setSamlResponseIssuer(String samlResponseIssuer) {
    this.samlResponseIssuer = samlResponseIssuer;
  }

  public boolean hasCompleteConfiguration() {
    return this.keyStorePath != null 
        && this.samlResponseAudience != null
        && this.samlResponseIssuer != null 
        && this.samlResponseNotBeforeSeconds != null
        && this.samlResponseNotOnOrAfterSeconds != null 
        && this.samlResponseOid != null
        && this.samlResponseOrganization != null
        && this.samlResponseRecipient != null
        && this.ssoEndpointUrl != null;
  }
}
