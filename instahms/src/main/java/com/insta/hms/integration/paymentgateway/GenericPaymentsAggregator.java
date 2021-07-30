package com.insta.hms.integration.paymentgateway;

import java.util.List;
import java.util.Map;

public interface GenericPaymentsAggregator {

  public List<String> getSupportedServices();

  public Boolean requiresConfiguration();

  public Map<String, String> getConfigurationSchema();

  public Map<String, String> getConfiguration();

  public void setConfiguration(Map<String, String> configMap);

  public Map<String, String> getEndpoints();

  public void setEndpoints(Map<String, String> endpointMap);

  public Map<String, String> doTransaction(TransactionRequirements transactionReq);

  public Map<String, String> cancelTransaction(TransactionRequirements transactionReq);

  public void processResponse(String response);

  public Map<String, String> checkTransactionStatus();

}
