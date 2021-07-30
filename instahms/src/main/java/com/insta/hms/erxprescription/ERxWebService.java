package com.insta.hms.erxprescription;

import com.insta.hms.erxprescription.generated.ERxValidateTransaction;
import com.insta.hms.erxprescription.generated.ERxValidateTransactionSoap;
import com.insta.hms.eservice.EService;
import com.sun.xml.ws.client.BindingProviderProperties;
import javax.xml.ws.BindingProvider;
import java.util.Map;

public class ERxWebService extends EService<ERxValidateTransactionSoap> {

  public ERxWebService() {
  }

  public ERxWebService(String serviceUser, String servicePassword) {
    super(serviceUser, servicePassword);
  }

  public ERxWebService(String serviceUser, String servicePassword, String clinicianUser,
      String clinicianPassword) {
    super(serviceUser, servicePassword, clinicianUser, clinicianPassword);
  }

  @Override
  public ERxValidateTransactionSoap getRemoteService() {
    ERxValidateTransaction erxWS = new ERxValidateTransaction();
    ERxValidateTransactionSoap erxValidateTransactionSoap = erxWS.getERxValidateTransactionSoap();; 
    ((BindingProvider) erxValidateTransactionSoap).getRequestContext()
        .put(BindingProviderProperties.CONNECT_TIMEOUT, 15000);
    ((BindingProvider) erxValidateTransactionSoap).getRequestContext()
        .put(BindingProviderProperties.REQUEST_TIMEOUT, 15000);
    return erxValidateTransactionSoap;
  }
  
  public ERxValidateTransactionSoap getRemoteServiceWithTimeOut(int timeOutInMilliSec) {
    ERxValidateTransaction erxWS = new ERxValidateTransaction();
    ERxValidateTransactionSoap erxValidateTransactionSoap = erxWS.getERxValidateTransactionSoap();; 
    ((BindingProvider) erxValidateTransactionSoap).getRequestContext()
        .put(BindingProviderProperties.CONNECT_TIMEOUT, timeOutInMilliSec);
    ((BindingProvider) erxValidateTransactionSoap).getRequestContext()
        .put(BindingProviderProperties.REQUEST_TIMEOUT, timeOutInMilliSec);
    return erxValidateTransactionSoap;
  }
}
