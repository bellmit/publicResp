/**
 *
 */

package com.insta.hms.eauthorization;

import com.insta.hms.eauthorization.generated_test.Webservices;
import com.insta.hms.eauthorization.generated_test.WebservicesSoap;
import com.insta.hms.eservice.EResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.xml.ws.Holder;

/**
 * The Class EAuthApprovalsRetriever.
 *
 * @author lakshmi
 */
public class EAuthApprovalsRetriever {

  /**
   * The log.
   */
  static Logger log = LoggerFactory.getLogger(EAuthApprovalsRetriever.class);

  /**
   * The time stamp formatter secs.
   */
  private SimpleDateFormat timeStampFormatterSecs = new SimpleDateFormat(
      "dd/MM/yyyy HH:mm:ss");

  /**
   * The service user.
   */
  private String serviceUser = null;

  /**
   * The service password.
   */
  private String servicePassword = null;

  /**
   * The service testing.
   */
  private boolean serviceTesting = false;

  /**
   * Instantiates a new e auth approvals retriever.
   *
   * @param serviceUser     the service user
   * @param servicePassword the service password
   * @param serviceTesting  the service testing
   */
  public EAuthApprovalsRetriever(String serviceUser, String servicePassword,
                                 boolean serviceTesting) {
    super();
    this.serviceUser = serviceUser;
    this.servicePassword = servicePassword;
    this.serviceTesting = serviceTesting;
  }

  /**
   * Gets the service user.
   *
   * @return the service user
   */
  public String getServiceUser() {
    return serviceUser;
  }

  /**
   * Gets the service password.
   *
   * @return the service password
   */
  public String getServicePassword() {
    return servicePassword;
  }

  /**
   * Gets the approval file list.
   *
   * @return the approval file list
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public EResponse getApprovalFileList() throws IOException {

    Holder<Integer> txnResult = new Holder<Integer>();
    Holder<String> errorMessage = new Holder<String>();
    Holder<String> xmlTransactions = new Holder<String>();

    String user = getServiceUser();
    String password = getServicePassword();

    WebservicesSoap eauthTestSoap = getTestWebService();
    com.insta.hms.pbmauthorization.generated.WebservicesSoap eauthSoap = getWebService();

    /*
     * if (serviceTesting)
     * eauthTestSoap.getNewPriorAuthorizationTransactions(user, password,
     * txnResult, xmlTransactions, errorMessage); else
     * eAuthSoap.getNewPriorAuthorizationTransactions(user, password,
     * txnResult, xmlTransactions, errorMessage);
     */

    Calendar calender = Calendar.getInstance();
    calender.setTime(new java.util.Date(calender.getTimeInMillis()));
    calender.add(Calendar.DATE, 1);
    Date td = new Date(calender.getTimeInMillis());

    Calendar cto = Calendar.getInstance();
    cto.setTime(new java.util.Date(cto.getTimeInMillis()));
    cto.add(Calendar.DATE, -2);
    Date fd = new Date(cto.getTimeInMillis());

    String transactionFromDate = timeStampFormatterSecs.format(fd);
    String transactionToDate = timeStampFormatterSecs.format(td);
    if (serviceTesting) {
      eauthSoap.getNewTransactions(user, password, txnResult,
          xmlTransactions, errorMessage);
    } else {
      /*
       * eAuthTestSoap.searchTransactions(user, password, 2, null, null,
       * 32, 1, null, transactionFromDate, transactionToDate, -1, -1,
       * txnResult, xmlTransactions, errorMessage);
       */
      eauthSoap.searchTransactions(user, password, 2, null, null, 32, 1,
          null, transactionFromDate, transactionToDate, -1, -1,
          txnResult, xmlTransactions, errorMessage);
    }
    // eAuthSoap.getNewTransactions(user, password, txnResult,
    // xmlTransactions,
    // errorMessage);

    return new EAuthResponse(txnResult.value, errorMessage.value,
        xmlTransactions.value);
  }

  /**
   * Gets the approval file.
   *
   * @param fileId           the file id
   * @param markAsDownloaded the mark as downloaded
   * @return the approval file
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public EResponse getApprovalFile(String fileId, boolean markAsDownloaded)
      throws IOException {
    Holder<Integer> downloadTxnResult = new Holder<Integer>();
    Holder<String> fileName = new Holder<String>();
    Holder<byte[]> file = new Holder<byte[]>();
    Holder<String> errorMessage = new Holder<String>();
    String user = getServiceUser();
    String password = getServicePassword();

    WebservicesSoap eauthTestSoap = getTestWebService();
    com.insta.hms.pbmauthorization.generated.WebservicesSoap eauthSoap = getWebService();

    // Call API to download transaction.
    if (serviceTesting) {
      eauthTestSoap.downloadTransactionFile(user, password, fileId,
          downloadTxnResult, fileName, file, errorMessage);
    } else {
      eauthSoap.downloadTransactionFile(user, password, fileId,
          downloadTxnResult, fileName, file, errorMessage);
    }

    EResponse authXMLResponse = new EAuthResponse(downloadTxnResult.value,
        errorMessage.value, file.value);
    if (!authXMLResponse.isError() && markAsDownloaded) {
      // After downloading the file using file id (errors) set as
      // downloaded.
      Holder<Integer> txnDownloadedResult = new Holder<Integer>();
      errorMessage = new Holder<String>();

      if (serviceTesting) {
        eauthTestSoap.setTransactionDownloaded(user, password, fileId,
            txnDownloadedResult, errorMessage);
      } else {
        eauthSoap.setTransactionDownloaded(user, password, fileId,
            txnDownloadedResult, errorMessage);
      }
      // we dont care too much about the response, here.
    }
    return authXMLResponse;
  }

  /**
   * Check new transactions.
   *
   * @return the e response
   */
  public EResponse checkNewTransactions() {
    Holder<Integer> txnResult = new Holder<Integer>();
    Holder<String> errorMessage = new Holder<String>();
    String user = getServiceUser();
    String password = getServicePassword();

    WebservicesSoap eauthTestSoap = getTestWebService();
    com.insta.hms.pbmauthorization.generated.WebservicesSoap eauthSoap = getWebService();

    // Call API to check for new transactions.
    if (serviceTesting) {
      eauthTestSoap.checkForNewPriorAuthorizationTransactions(user,
          password, txnResult, errorMessage);
    } else {
      eauthSoap.checkForNewPriorAuthorizationTransactions(user, password,
          txnResult, errorMessage);
    }

    return new EAuthResponse(txnResult.value, errorMessage.value, null);
  }

  /**
   * Gets the web service.
   *
   * @return the web service
   */
  public com.insta.hms.pbmauthorization.generated.WebservicesSoap getWebService() {
    com.insta.hms.pbmauthorization.generated.Webservices eauthws =
        new com.insta.hms.pbmauthorization.generated.Webservices();
    com.insta.hms.pbmauthorization.generated.WebservicesSoap eauthwsoap = eauthws
        .getWebservicesSoap();
    return eauthwsoap;
  }

  /**
   * Gets the test web service.
   *
   * @return the test web service
   */
  public WebservicesSoap getTestWebService() {
    Webservices eauthtestws = new Webservices();
    WebservicesSoap eauthtestwsoap = eauthtestws.getWebservicesSoap();
    return eauthtestwsoap;
  }

  /**
   * Gets the web service user.
   *
   * @return the web service user
   */
  public String getWebServiceUser() {
    return serviceUser;
  }

  /**
   * Gets the web service password.
   *
   * @return the web service password
   */
  public String getWebServicePassword() {
    return servicePassword;
  }
}
