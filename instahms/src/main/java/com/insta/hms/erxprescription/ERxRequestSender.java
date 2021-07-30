package com.insta.hms.erxprescription;

import com.insta.hms.erxprescription.generated.ERxValidateTransactionSoap;
import com.sun.xml.ws.client.ClientTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.SocketTimeoutException;
import javax.xml.ws.Holder;

public class ERxRequestSender extends ERxWebService {

  static Logger log = LoggerFactory.getLogger(ERxRequestSender.class);

  public ERxRequestSender(String serviceUser, String servicePassword) {
    super(serviceUser, servicePassword);
  }

  public ERxRequestSender(String serviceUser, String servicePassword, String clinicianUser,
      String clinicianPassword) {
    super(serviceUser, servicePassword, clinicianUser, clinicianPassword);
  }

  public ERxResponse sendErxRequest(String xml, String xmlFileName) throws SocketTimeoutException, ClientTransportException {
    byte[] fileContent = xml.getBytes();
    Holder<Integer> uploadTxnResult = new Holder<Integer>();
    Holder<Integer> eRxReferenceNo = new Holder<Integer>();
    Holder<String> errorMessage = new Holder<String>();
    Holder<byte[]> errorReport = new Holder<byte[]>();

    String serviceUserLogin = getServiceUser();
    String serviceUserPwd = getServicePassword();

    String clinicianLogin = getClinicianUser();
    String clinicianPwd = getClinicianPassword();

    ERxValidateTransactionSoap erxSoap = getRemoteService();

    erxSoap.uploadERxRequest(serviceUserLogin, serviceUserPwd, clinicianLogin, clinicianPwd,
        fileContent, xmlFileName, uploadTxnResult, eRxReferenceNo, errorMessage, errorReport);

    log.info("ERx Request Sent with details: xmlFileName : " + xmlFileName + " eRxReferenceNo: "
        + eRxReferenceNo.value + "  errorMessage: " + errorMessage.value + "  errorReport: "
        + errorReport.value);
    return new ERxResponse(uploadTxnResult.value, errorMessage.value, errorReport.value,
        new Object[] { eRxReferenceNo.value });
  }
  
  /**
   * This method sends erx request/cancel with timeout.
   * @param xml
   * @param xmlFileName
   * @param timeOutInMilliSec
   * @return
   * @throws SocketTimeoutException
   * @throws ClientTransportException
   */
  public ERxResponse sendERxRequestWithTimeOut(String xml, String xmlFileName,
      int timeOutInMilliSec) throws SocketTimeoutException, ClientTransportException {
    byte[] fileContent = xml.getBytes();
    Holder<Integer> uploadTxnResult = new Holder<Integer>();
    Holder<Integer> eRxReferenceNo = new Holder<Integer>();
    Holder<String> errorMessage = new Holder<String>();
    Holder<byte[]> errorReport = new Holder<byte[]>();

    String serviceUserLogin = getServiceUser();
    String serviceUserPwd = getServicePassword();

    String clinicianLogin = getClinicianUser();
    String clinicianPwd = getClinicianPassword();

    ERxValidateTransactionSoap erxSoap = getRemoteServiceWithTimeOut(timeOutInMilliSec);

    erxSoap.uploadERxRequest(serviceUserLogin, serviceUserPwd, clinicianLogin, clinicianPwd,
        fileContent, xmlFileName, uploadTxnResult, eRxReferenceNo, errorMessage, errorReport);

    log.info("ERx Request Sent with details: xmlFileName : " + xmlFileName + " eRxReferenceNo: "
        + eRxReferenceNo.value + "  errorMessage: " + errorMessage.value + "  errorReport: "
        + errorReport.value);
    return new ERxResponse(uploadTxnResult.value, errorMessage.value, errorReport.value,
        new Object[] { eRxReferenceNo.value });
  }
  
}
