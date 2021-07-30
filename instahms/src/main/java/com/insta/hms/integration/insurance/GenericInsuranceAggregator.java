package com.insta.hms.integration.insurance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Interface GenericInsuranceAggregator.
 */
public interface GenericInsuranceAggregator {

  /**
   * Gets the supported services.
   *
   * @return the supported services
   */
  public List<String> getSupportedServices();

  /**
   * Requires configuration.
   *
   * @return the boolean
   */
  public Boolean requiresConfiguration();

  /**
   * Gets the center configuration schema.
   *
   * @return the center configuration schema
   */
  public Map<String, List<ConfigurationItem>> getCenterConfigurationSchema();

  /**
   * Gets the doctor configuration schema.
   *
   * @return the doctor configuration schema
   */
  public Map<String, List<ConfigurationItem>> getDoctorConfigurationSchema();

  /**
   * Gets the tpa ins co configuration schema.
   *
   * @return the tpa ins co configuration schema
   */
  public Map<String, List<ConfigurationItem>> getTpaInsCoConfigurationSchema();

  /**
   * Gets the store configuration schema.
   *
   * @return the store configuration schema
   */
  public Map<String, List<ConfigurationItem>> getStoreConfigurationSchema();

  /**
   * Gets the configuration.
   *
   * @return the configuration
   */
  public List<ConfigurationItem> getConfiguration();

  /**
   * Sets the configuration.
   *
   * @param configMap the config map
   */
  public void setConfiguration(HashMap<String, String> configMap);

  /**
   * Gets the policy details by mr no.
   *
   * @param mrNo the mr no
   * @return the policy details by mr no
   */
  public PolicyDetails getPolicyDetailsByMrNo(String mrNo);

  /**
   * Gets the policy details by mobile.
   *
   * @param mobileNo the mobile no
   * @return the policy details by mobile
   */
  public PolicyDetails getPolicyDetailsByMobile(String mobileNo);

  /**
   * Gets the policy details by govt identifier.
   *
   * @param govtId the govt id
   * @return the policy details by govt identifier
   */
  public PolicyDetails getPolicyDetailsByGovtIdentifier(String govtId);

  /**
   * Send claim request.
   *
   * @param claimDoc the claim doc
   * @return the claim request results
   */
  public ClaimRequestResults sendClaimRequest(ClaimDocument claimDoc);

  /**
   * Sync claim responses.
   *
   * @param fiter the fiter
   */
  public void syncClaimResponses(RemittanceFilter fiter);

  /**
   * Send prior auth request.
   *
   * @param priorAuthDoc the prior auth doc
   * @return the prior auth request results
   */
  public PriorAuthRequestResults sendPriorAuthRequest(PriorAuthDocument priorAuthDoc);

  /**
   * Sync prior auth responses.
   *
   * @param filter the filter
   */
  public void syncPriorAuthResponses(PriorAuthFilter filter);

}
