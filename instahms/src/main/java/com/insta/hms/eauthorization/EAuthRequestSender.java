/**
 *
 */

package com.insta.hms.eauthorization;

import com.insta.hms.eauthorization.generated_test.Webservices;
import com.insta.hms.eauthorization.generated_test.WebservicesSoap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;

/**
 * The Class EAuthRequestSender.
 *
 * @author lakshmi
 */
public class EAuthRequestSender {

  /**
   * The log.
   */
  static Logger log = LoggerFactory.getLogger(EAuthRequestSender.class);

  /**
   * The service user.
   */
  private String serviceUser = null;

  /**
   * The service password.
   */
  private String servicePassword = null;

  /**
   * The test mode.
   */
  private boolean testMode = false;

  /**
   * Instantiates a new e auth request sender.
   *
   * @param serviceUser     the service user
   * @param servicePassword the service password
   * @param testMode        the test mode
   */
  public EAuthRequestSender(String serviceUser, String servicePassword,
                            boolean testMode) {
    super();
    this.serviceUser = serviceUser;
    this.servicePassword = servicePassword;
    this.testMode = testMode;
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
   * Send E auth request.
   *
   * @param requestXML the request XML
   * @param fileName   the file name
   * @return the e auth response
   */
  public EAuthResponse sendEAuthRequest(String requestXML, String fileName) {

    byte[] fileContent = requestXML.getBytes();
    Holder<Integer> uploadTxnResult = new Holder<Integer>();
    Holder<String> errorMessage = new Holder<String>();
    Holder<byte[]> errorReport = new Holder<byte[]>();

    String user = getWebServiceUser();
    String password = getWebServicePassword();

    WebservicesSoap eauthTestSoap = getTestWebService();
    com.insta.hms.pbmauthorization.generated.WebservicesSoap eauthSoap = getWebService();
    if (testMode) {
      log.info("Using endpoint URL: "
          + ((BindingProvider) eauthTestSoap).getRequestContext()
          .get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY));
      eauthTestSoap.uploadTransaction(user, password, fileContent,
          fileName, uploadTxnResult, errorMessage, errorReport);
    } else {
      log.info("Using endpoint URL: "
          + ((BindingProvider) eauthSoap).getRequestContext()
          .get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY));
      eauthSoap.uploadTransaction(user, password, fileContent, fileName,
          uploadTxnResult, errorMessage, errorReport);
    }
    return new EAuthResponse(uploadTxnResult.value, errorMessage.value,
        errorReport.value);
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
    WebservicesSoap eauthtestwsoap = eauthtestws
        .getWebservicesSoap();
    return eauthtestwsoap;
  }

  /**
   * Cancel E auth request.
   *
   * @param requestXML the request XML
   * @param fileName   the file name
   * @return the e auth response
   */
  public EAuthResponse cancelEAuthRequest(String requestXML,
                                          String fileName) {
    return sendEAuthRequest(requestXML, fileName);
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
