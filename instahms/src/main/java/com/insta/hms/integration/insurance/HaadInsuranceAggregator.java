package com.insta.hms.integration.insurance;

import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.annotations.Aggregator;
import com.insta.hms.common.annotations.LazyAutowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class HaadInsuranceAggregator.
 */
@Aggregator(value = "shafafiya", name = "Shafafiya (HAAD)")
public class HaadInsuranceAggregator implements GenericInsuranceAggregator {

  /** The message util. */
  @LazyAutowired
  MessageUtil messageUtil;

  /** The user name. */
  private String userName;

  /** The pass word. */
  private String passWord;

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.integration.insurance.GenericInsuranceAggregator#getSupportedServices()
   */
  @Override
  public List<String> getSupportedServices() {
    // TODO Auto-generated method stub
    List<String> services = new ArrayList<String>();
    services.add("OP Claims");
    services.add("IP Claims");
    services.add("Pharmacy Claims");
    services.add("Self pay reporting");
    services.add("OP Pre Authorization");
    services.add("IP Pre Authorization");
    services.add("Pharmacy Pre Authorization");
    services.add("Pharmacy Prescription Submission");
    services.add("Pharmacy Prescription Dispensing");
    services.add("Remittance Advices");
    return services;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.integration.insurance.GenericInsuranceAggregator#requiresConfiguration()
   */
  @Override
  public Boolean requiresConfiguration() {
    // TODO Auto-generated method stub
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.integration.insurance.GenericInsuranceAggregator#getPolicyDetailsByMrNo(java
   * .lang.String)
   */
  @Override
  public PolicyDetails getPolicyDetailsByMrNo(String mrNo) {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.integration.insurance.GenericInsuranceAggregator#getPolicyDetailsByMobile(
   * java.lang.String)
   */
  @Override
  public PolicyDetails getPolicyDetailsByMobile(String mobileNo) {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.integration.insurance.GenericInsuranceAggregator#
   * getPolicyDetailsByGovtIdentifier(java.lang.String)
   */
  @Override
  public PolicyDetails getPolicyDetailsByGovtIdentifier(String govtId) {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.integration.insurance.GenericInsuranceAggregator#sendClaimRequest(com.insta.
   * hms.integration.insurance.ClaimDocument)
   */
  @Override
  public ClaimRequestResults sendClaimRequest(ClaimDocument claimDoc) {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.integration.insurance.GenericInsuranceAggregator#syncClaimResponses(com.
   * insta.hms.integration.insurance.RemittanceFilter)
   */
  @Override
  public void syncClaimResponses(RemittanceFilter fiter) {
    // TODO Auto-generated method stub
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.integration.insurance.GenericInsuranceAggregator#sendPriorAuthRequest(com.
   * insta.hms.integration.insurance.PriorAuthDocument)
   */
  @Override
  public PriorAuthRequestResults sendPriorAuthRequest(PriorAuthDocument priorAuthDoc) {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.integration.insurance.GenericInsuranceAggregator#syncPriorAuthResponses(com.
   * insta.hms.integration.insurance.PriorAuthFilter)
   */
  @Override
  public void syncPriorAuthResponses(PriorAuthFilter filter) {
    // TODO Auto-generated method stub
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.integration.insurance.GenericInsuranceAggregator#
   * getCenterConfigurationSchema()
   */
  @Override
  public Map<String, List<ConfigurationItem>> getCenterConfigurationSchema() {
    Map<String, List<ConfigurationItem>> configListMap =
        new HashMap<String, List<ConfigurationItem>>();
    // prior auth example
    List<ConfigurationItem> priorAuthConfigs = new ArrayList<ConfigurationItem>();
    priorAuthConfigs.add(new ConfigurationItem("userName", "input",
        messageUtil.getMessage("ui.label.prior.auth.username", null)));
    priorAuthConfigs.add(new ConfigurationItem("password", "input",
        messageUtil.getMessage("ui.label.prior.auth.password", null)));
    configListMap.put("priorAuth", priorAuthConfigs);
    return configListMap;
  }


  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.integration.insurance.GenericInsuranceAggregator#
   * getDoctorConfigurationSchema()
   */
  @Override
  public Map<String, List<ConfigurationItem>> getDoctorConfigurationSchema() {
    Map<String, List<ConfigurationItem>> configListMap =
        new HashMap<String, List<ConfigurationItem>>();
    List<ConfigurationItem> doctorErxConfigs = new ArrayList<ConfigurationItem>();
    doctorErxConfigs
        .add(new ConfigurationItem("userName", "input", "DOCTOR_ERX_USERNAME_LABEL"));
    doctorErxConfigs
        .add(new ConfigurationItem("password", "input", "DOCTOR_ERX_PASSWORD_LABEL"));
    configListMap.put("erx", doctorErxConfigs);
    return configListMap;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.integration.insurance.GenericInsuranceAggregator#
   * getTpaInsCoConfigurationSchema()
   */
  @Override
  public Map<String, List<ConfigurationItem>> getTpaInsCoConfigurationSchema() {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.integration.insurance.GenericInsuranceAggregator#getStoreConfigurationSchema
   * ()
   */
  @Override
  public Map<String, List<ConfigurationItem>> getStoreConfigurationSchema() {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.integration.insurance.GenericInsuranceAggregator#getConfiguration()
   */
  @Override
  public List<ConfigurationItem> getConfiguration() {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.integration.insurance.GenericInsuranceAggregator#setConfiguration(java.util.
   * HashMap)
   */
  @Override
  public void setConfiguration(HashMap<String, String> configMap) {
    // TODO Auto-generated method stub

  }

}
