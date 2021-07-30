package com.insta.hms.erxprescription;

import com.insta.hms.erxprescription.generated.ERxValidateTransactionSoap;
import com.insta.hms.eservice.EResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import javax.xml.ws.Holder;

public class ERxApprovalsRetriever extends ERxWebService {

  static Logger log = LoggerFactory.getLogger(ERxApprovalsRetriever.class);

  public ERxApprovalsRetriever(String serviceUser, String servicePassword) {
    super(serviceUser, servicePassword);
  }

  public ERxApprovalsRetriever(String serviceUser, String servicePassword, String clinicianUser,
      String clinicianPassword) {
    super(serviceUser, servicePassword, clinicianUser, clinicianPassword);
  }

  public EResponse getApprovalFileList(int eRxReferenceNo, String memberID,
      String transactionFromDate, String transactionToDate) throws IOException {

    Holder<Integer> txnResult = new Holder<Integer>();
    Holder<String> errorMessage = new Holder<String>();
    Holder<String> xmlTransactions = new Holder<String>(); // Prior Auth Response XML Encoded String

    String user = getServiceUser();
    String password = getServicePassword();
    ERxValidateTransactionSoap erxSoap = getRemoteService();
    Integer txnStatus = 1;

    erxSoap.searchTransactions(user, password, 2, null, null, memberID, null, txnStatus,
        transactionFromDate, transactionToDate, 1, 100, txnResult, xmlTransactions, errorMessage);
    // (user, password, memberID, eRxReferenceNo,
    // txnResult, xmlTransactions, errorMessage);
    log.info("ERx Approval file receiving: memberID: " + memberID + " eRxReferenceNo: "
        + eRxReferenceNo + " xmlTransactions: " + xmlTransactions.value + " errorMessage: "
        + errorMessage.value);
    return new ERxResponse(txnResult.value, errorMessage.value, xmlTransactions.value);
  }

  public EResponse getApprovalFile(String fileId, boolean markAsDownloaded) throws IOException {
    Holder<Integer> downloadTxnResult = new Holder<Integer>();
    Holder<String> fileName = new Holder<String>();
    Holder<byte[]> file = new Holder<byte[]>();
    Holder<String> errorMessage = new Holder<String>();
    String user = getServiceUser();
    String password = getServicePassword();
    ERxValidateTransactionSoap erxSoap = getRemoteService();

    // Call ERx API to download transaction.
    erxSoap.downloadTransactionFile(user, password, fileId, downloadTxnResult, fileName, file,
        errorMessage);

    EResponse authXMLResponse = new ERxResponse(downloadTxnResult.value, errorMessage.value,
        file.value, new Object[] { fileName });

    log.info("ERx Request XML Content in getApprovalFile, File Content:" + new String(file.value)
        + " fileName: " + fileName.value + " errorMessage: " + errorMessage.value);

    if (!authXMLResponse.isError() && markAsDownloaded) {
      // After downloading the file using file id (errors) set as downloaded.
      Holder<Integer> txnDownloadedResult = new Holder<Integer>();
      errorMessage = new Holder<String>();
      erxSoap.setTransactionDownloaded(user, password, fileId, txnDownloadedResult, errorMessage);
      // we dont care too much about the response, here.
    }
    return authXMLResponse;
  }

}
