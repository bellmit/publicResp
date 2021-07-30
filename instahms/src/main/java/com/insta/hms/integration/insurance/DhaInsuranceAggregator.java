package com.insta.hms.integration.insurance;

import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.annotations.Aggregator;
import com.insta.hms.common.annotations.LazyAutowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class DhaInsuranceAggregator.
 */
@Aggregator(value = "eclaimLink", name = "EclaimLink (DHA)")
public class DhaInsuranceAggregator implements GenericInsuranceAggregator {

  /** The message util. */
  @LazyAutowired
  MessageUtil messageUtil;

  /** The user name. */
  private String userName;
  
  /** The pass word. */
  private String passWord;

  /**
   * Gets the supported services.
   *
   * @return the supported services
   */
  @Override
  public List<String> getSupportedServices() {
    // TODO Auto-generated method stub
    List<String> services = new ArrayList<String>();
    services.add("Member lookup by Govt ID");
    services.add("OP Claims");
    services.add("IP Claims");
    services.add("Pharmacy Claims");
    services.add("OP Pre Authorization");
    services.add("IP Pre Authorization");
    services.add("Pharmacy Pre Authorization");
    services.add("Pharmacy Prescription Submission");
    services.add("Pharmacy Prescription Dispensing");
    services.add("Remittance Advices");
    return services;
  }

  /**
   * Requires configuration.
   *
   * @return the boolean
   */
  @Override
  public Boolean requiresConfiguration() {
    // TODO Auto-generated method stub
    return true;
  }

  /**
   * Gets the center configuration schema.
   *
   * @return the center configuration schema
   */
  @Override
  public Map<String, List<ConfigurationItem>> getCenterConfigurationSchema() {
    // TODO Auto-generated method stub
    Map<String, List<ConfigurationItem>> configListMap =
        new HashMap<String, List<ConfigurationItem>>();
    // List<Map<String, ConfigurationItem>> configList = new ArrayList<Map<String,
    // ConfigurationItem>>();
    List<ConfigurationItem> eclaimConfigs = new ArrayList<ConfigurationItem>();
    eclaimConfigs.add(new ConfigurationItem("userName", "checkbox", "E_CLAIM_USERNAME_LABEL"));
    eclaimConfigs.add(new ConfigurationItem("password", "input", "E_CLAIM_PASSWORD_LABEL"));
    configListMap.put("eclaim", eclaimConfigs);
    return configListMap;
  }

  /**
   * Gets the policy details by mr no.
   *
   * @param mrNo the mr no
   * @return the policy details by mr no
   */
  @Override
  public PolicyDetails getPolicyDetailsByMrNo(String mrNo) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Gets the policy details by mobile.
   *
   * @param mobileNo the mobile no
   * @return the policy details by mobile
   */
  @Override
  public PolicyDetails getPolicyDetailsByMobile(String mobileNo) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Gets the policy details by govt identifier.
   *
   * @param govtId the govt id
   * @return the policy details by govt identifier
   */
  @Override
  public PolicyDetails getPolicyDetailsByGovtIdentifier(String govtId) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Send claim request.
   *
   * @param claimDoc the claim doc
   * @return the claim request results
   */
  @Override
  public ClaimRequestResults sendClaimRequest(ClaimDocument claimDoc) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Sync claim responses.
   *
   * @param fiter the fiter
   */
  @Override
  public void syncClaimResponses(RemittanceFilter fiter) {
    // TODO Auto-generated method stub
  }

  /**
   * Send prior auth request.
   *
   * @param priorAuthDoc the prior auth doc
   * @return the prior auth request results
   */
  @Override
  public PriorAuthRequestResults sendPriorAuthRequest(PriorAuthDocument priorAuthDoc) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Sync prior auth responses.
   *
   * @param filter the filter
   */
  @Override
  public void syncPriorAuthResponses(PriorAuthFilter filter) {
    // TODO Auto-generated method stub
  }

  /**
   * Gets the doctor configuration schema.
   *
   * @return the doctor configuration schema
   */
  @Override
  public Map<String, List<ConfigurationItem>> getDoctorConfigurationSchema() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Gets the tpa ins co configuration schema.
   *
   * @return the tpa ins co configuration schema
   */
  @Override
  public Map<String, List<ConfigurationItem>> getTpaInsCoConfigurationSchema() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Gets the store configuration schema.
   *
   * @return the store configuration schema
   */
  @Override
  public Map<String, List<ConfigurationItem>> getStoreConfigurationSchema() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Gets the configuration.
   *
   * @return the configuration
   */
  @Override
  public List<ConfigurationItem> getConfiguration() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Sets the configuration.
   *
   * @param configMap the config map
   */
  @Override
  public void setConfiguration(HashMap<String, String> configMap) {
    // TODO Auto-generated method stub

  }
}
