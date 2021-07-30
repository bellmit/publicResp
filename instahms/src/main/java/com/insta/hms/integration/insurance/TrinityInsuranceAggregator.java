package com.insta.hms.integration.insurance;

import com.insta.hms.common.annotations.Aggregator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class TrinityInsuranceAggregator.
 */
@Aggregator(value = "trinity", name = "Practo Trinity")
public class TrinityInsuranceAggregator implements GenericInsuranceAggregator {

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.integration.insurance.GenericInsuranceAggregator#getSupportedServices()
   */
  @Override
  public List<String> getSupportedServices() {
    // TODO Auto-generated method stub
    List<String> services = new ArrayList<String>();
    services.add("Member lookup by MR No");
    services.add("Member lookup by Mobile");
    services.add("Coverage Calculator");
    services.add("OP Claims");
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
    return false;
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
    return configListMap;
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
   * getDoctorConfigurationSchema()
   */
  @Override
  public Map<String, List<ConfigurationItem>> getDoctorConfigurationSchema() {
    // TODO Auto-generated method stub
    return null;
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
