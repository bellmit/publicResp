package com.insta.hms.integration.insurance.dha;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.insurance.ClaimValidationHelper;
import com.insta.hms.dhaeclaimprescription.generated.ValidateTransactions;
import com.insta.hms.dhaeclaimprescription.generated.ValidateTransactionsSoap;
import com.insta.hms.integration.insurance.ClaimContext;
import com.insta.hms.integration.insurance.ClaimDocument;
import com.insta.hms.integration.insurance.ClaimReference;
import com.insta.hms.integration.insurance.ClaimRemittance;
import com.insta.hms.integration.insurance.ClaimSubmissionResult;
import com.insta.hms.integration.insurance.InsurancePlugin;
import com.insta.hms.integration.insurance.MemberEligibility;
import com.insta.hms.integration.insurance.MembershipDetails;
import com.insta.hms.integration.insurance.PatientIdentity;
import com.insta.hms.integration.insurance.PayerDetails;
import com.insta.hms.integration.insurance.PluginMatcher;
import com.insta.hms.integration.insurance.PriorAuthDocument;
import com.insta.hms.integration.insurance.PriorAuthRequestResults;
import com.insta.hms.integration.insurance.RemittanceFilter;
import com.insta.hms.integration.insurance.ServiceCredential;
import com.insta.hms.mdm.hospitalcenters.HospitalCenterService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.ConnectException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceClient;

// TODO: Auto-generated Javadoc
/**
 * The Class DhaInsurancePlugin.
 */
@Component
public class DhaInsurancePlugin implements InsurancePlugin {

  /** The log. */
  static Logger log = LoggerFactory.getLogger(DhaInsurancePlugin.class);

  /** The disposition. */
  private String disposition; // production/test

  /** The user name. */
  private String userName;

  /** The pass word. */
  private String passWord;

  @LazyAutowired
  HospitalCenterService hospitalCenterService;

  @LazyAutowired
  private ClaimValidationHelper claimValidationHelper;

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.integration.insurance.InsurancePlugin#getClaimContext()
   */
  public ClaimContext getClaimContext() {
    return new DhaClaimContext();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.integration.insurance.InsurancePlugin#getMatcher()
   */
  @Override
  public PluginMatcher getMatcher() {

    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.integration.insurance.InsurancePlugin#getMembershipDetails(com.insta.hms.
   * integration.insurance.PatientIdentity)
   */
  @Override
  public MembershipDetails getMembershipDetails(PatientIdentity pi) {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.integration.insurance.InsurancePlugin#downloadFile(com.insta.hms.integration.
   * insurance.RemittanceFilter, com.insta.hms.integration.insurance.ClaimContext, boolean)
   */
  @Override
  public ClaimRemittance downloadFile(RemittanceFilter filter, ClaimContext context,
      boolean markAsDownloaded) throws ConnectException {
    try {
      ValidateTransactions erxws = new ValidateTransactions();
      ValidateTransactionsSoap erxwsoap = erxws.getValidateTransactionsSoap();

      Holder<String> errorMessage = new Holder<String>();
      Holder<Integer> downloadTxnResult = new Holder<Integer>();
      Holder<String> fileName = new Holder<String>();
      Holder<byte[]> file = new Holder<byte[]>();

      ServiceCredential serviceCredential = ((DhaClaimContext) context).getServiceCredentials();
      ClaimRemittance claimRemittance = new ClaimRemittance();

      // downloads the file from webservice
      erxwsoap.downloadTransactionFile(serviceCredential.getServiceUser(),
          serviceCredential.getServicePassword(), filter.getFileId(), downloadTxnResult, fileName,
          file, errorMessage);

      if (downloadTxnResult.value >= 0 && markAsDownloaded) {
        // After downloading the details, mark the fileId as downloaded.
        Holder<Integer> txnDownloadedResult = new Holder<Integer>();
        Holder<String> errorMessage1 = new Holder<String>();
        erxwsoap.setTransactionDownloaded(serviceCredential.getServiceUser(),
            serviceCredential.getServicePassword(), filter.getFileId(), txnDownloadedResult,
            errorMessage1);
        // we dont care too much about the response, here.
      }
      claimRemittance.setErrorMessage(errorMessage);
      claimRemittance.setTxnResult(downloadTxnResult);
      claimRemittance.setFile(file);
      claimRemittance.setFileName(fileName);
      return claimRemittance;
    } catch (Exception exception) {
      StringWriter stack = new StringWriter();
      exception.printStackTrace(new PrintWriter(stack));
      log.error(stack.toString());
      throw new ConnectException(exception.getMessage());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.integration.insurance.InsurancePlugin#getEligibility(com.insta.hms.integration.
   * insurance.MembershipDetails)
   */
  @Override
  public MemberEligibility getEligibility(MembershipDetails memberDetails) {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.integration.insurance.InsurancePlugin#sendPriorAuthRequest(com.insta.hms.
   * integration.insurance.PriorAuthDocument)
   */
  @Override
  public PriorAuthRequestResults sendPriorAuthRequest(PriorAuthDocument priorAuthDocument)
      throws ConnectException {
    try {
      ValidateTransactions erxws = new ValidateTransactions();
      ValidateTransactionsSoap erxwsoap = erxws.getValidateTransactionsSoap();

      byte[] fileContent = priorAuthDocument.getContent();
      String fileName = priorAuthDocument.getFileName();
      Holder<Integer> uploadTxnResult = new Holder<Integer>();
      Holder<String> errorMessage = new Holder<String>();
      Holder<byte[]> errorReport = new Holder<byte[]>();

      String user = this.userName;
      String password = this.passWord;

      erxwsoap.uploadTransaction(user, password, fileContent, fileName, uploadTxnResult,
          errorMessage, errorReport);

      PriorAuthRequestResults csResult = new PriorAuthRequestResults();
      csResult.setErrorMessage(errorMessage);
      csResult.setErrorReport(errorReport);
      csResult.setTxnResult(uploadTxnResult);
      return csResult;
    } catch (Exception exception) {
      StringWriter stack = new StringWriter();
      exception.printStackTrace(new PrintWriter(stack));
      log.error(stack.toString());
      throw new ConnectException();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.integration.insurance.InsurancePlugin#submitClaim(com.insta.hms.integration.
   * insurance.ClaimDocument, com.insta.hms.integration.insurance.ClaimContext)
   */
  @Override
  public ClaimSubmissionResult submitClaim(ClaimDocument claimDocument, ClaimContext context)
      throws ConnectException {
    try {
      ValidateTransactions erxws = new ValidateTransactions();
      ValidateTransactionsSoap erxwsoap = erxws.getValidateTransactionsSoap();
      byte[] fileContent = claimDocument.getContent();
      String fileName = claimDocument.getFileName();
      Holder<Integer> uploadTxnResult = new Holder<Integer>();
      Holder<String> errorMessage = new Holder<String>();
      Holder<byte[]> errorReport = new Holder<byte[]>();
      ServiceCredential serviceCredential = ((DhaClaimContext) context).getServiceCredentials();
      erxwsoap.uploadTransaction(serviceCredential.getServiceUser(),
          serviceCredential.getServicePassword(), fileContent, fileName, uploadTxnResult,
          errorMessage, errorReport);
      ClaimSubmissionResult csResult = new ClaimSubmissionResult();
      csResult.setErrorMessage(errorMessage);
      csResult.setErrorReport(errorReport);
      csResult.setUploadTxnResult(uploadTxnResult);
      return csResult;
    } catch (Exception exception) {
      StringWriter stack = new StringWriter();
      exception.printStackTrace(new PrintWriter(stack));
      log.error(stack.toString());
      throw new ConnectException();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.integration.insurance.InsurancePlugin#getRemittance(com.insta.hms.integration.
   * insurance.PayerDetails, com.insta.hms.integration.insurance.RemittanceFilter,
   * com.insta.hms.integration.insurance.ClaimReference,
   * com.insta.hms.integration.insurance.ClaimContext, boolean)
   */
  @Override
  public ClaimRemittance getRemittance(PayerDetails payerInfo, RemittanceFilter filter,
      ClaimReference reference, ClaimContext context, boolean newTransactionsOnly)
      throws ConnectException {
    try {
      ValidateTransactions erxws = new ValidateTransactions();
      ValidateTransactionsSoap erxwsoap = erxws.getValidateTransactionsSoap();

      Holder<Integer> txnResult = new Holder<Integer>();
      Holder<String> errorMessage = new Holder<String>();
      Holder<String> xmlTransactions = new Holder<String>();

      ServiceCredential serviceCredential = ((DhaClaimContext) context).getServiceCredentials();
      ClaimRemittance claimRemittance = new ClaimRemittance();

      // if newTransactionsOnly is true, bring a list of only new files
      // (that haven't been marked as downloaded before)

      if (newTransactionsOnly) {
        erxwsoap.searchTransactions(serviceCredential.getServiceUser(),
            serviceCredential.getServicePassword(), 2, null, null, 8, 1,
            filter.getTransactionFileName(), filter.getTransactionFromDate(),
            filter.getTransactionToDate(), -1, -1, txnResult, xmlTransactions, errorMessage);
        claimRemittance.setErrorMessage(errorMessage);
        claimRemittance.setTxnResult(txnResult);
        claimRemittance.setXmlTransactions(xmlTransactions);
      } else if (!newTransactionsOnly) {
        /*
         * if not newTransactionsOnly, bring a list of only all files. already downloaded only.
         */
        erxwsoap.searchTransactions(serviceCredential.getServiceUser(),
            serviceCredential.getServicePassword(), 2, null, null, 8, 2,
            filter.getTransactionFileName(), filter.getTransactionFromDate(),
            filter.getTransactionToDate(), -1, -1, txnResult, xmlTransactions, errorMessage);
        claimRemittance.setErrorMessage(errorMessage);
        claimRemittance.setTxnResult(txnResult);
        claimRemittance.setXmlTransactions(xmlTransactions);
      }
      return claimRemittance;
    } catch (Exception exception) {
      StringWriter stack = new StringWriter();
      exception.printStackTrace(new PrintWriter(stack));
      log.error(stack.toString());
      throw new ConnectException(exception.getMessage());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.integration.insurance.InsurancePlugin#getConfiguration()
   */
  @Override
  public HashMap<String, String> getConfiguration() {
    HashMap<String, String> configMap = new HashMap<String, String>();
    configMap.put("userName", this.userName);
    configMap.put("passWord", this.passWord);
    configMap.put("disposition", this.disposition);
    return configMap;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.integration.insurance.InsurancePlugin#setConfiguration(java.util.HashMap)
   */
  @Override
  public void setConfiguration(HashMap<String, String> configMap) {
    this.userName = configMap.get("userName");
    this.passWord = configMap.get("passWord");
    this.disposition = configMap.get("disposition");

  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.integration.insurance.InsurancePlugin#priorAuthApprovalFileList()
   */
  @Override
  public PriorAuthRequestResults priorAuthApprovalFileList() throws ConnectException {
    try {
      Calendar calender = Calendar.getInstance();
      calender.setTime(new java.util.Date(calender.getTimeInMillis()));
      calender.add(Calendar.DATE, 1);
      Date td = new Date(calender.getTimeInMillis());

      Calendar cto = Calendar.getInstance();
      cto.setTime(new java.util.Date(cto.getTimeInMillis()));
      cto.add(Calendar.DATE, -2);
      Date fd = new Date(cto.getTimeInMillis());
      SimpleDateFormat timeStampFormatterSecs = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
      String transactionFromDate = timeStampFormatterSecs.format(fd);
      String transactionToDate = timeStampFormatterSecs.format(td);
      Holder<Integer> txnResult = new Holder<Integer>();
      Holder<String> errorMessage = new Holder<String>();
      Holder<String> xmlTransactions = new Holder<String>();
      String password = this.passWord;
      ValidateTransactions erxws = new ValidateTransactions();
      ValidateTransactionsSoap erxwsoap = erxws.getValidateTransactionsSoap();
      String user = this.userName;

      log.info("Using endpoint URL: " + ((BindingProvider) erxwsoap).getRequestContext()
          .get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY));
      erxwsoap.searchTransactions(user, password, 2, null, null, 32, 1, null, transactionFromDate,
          transactionToDate, -1, -1, txnResult, xmlTransactions, errorMessage);

      PriorAuthRequestResults csResult = new PriorAuthRequestResults();
      csResult.setTxnResult(txnResult);
      csResult.setErrorMessage(errorMessage);
      csResult.setXmlTransactions(xmlTransactions);
      return csResult;
    } catch (Exception exception) {
      StringWriter stack = new StringWriter();
      exception.printStackTrace(new PrintWriter(stack));
      log.error(stack.toString());
      throw new ConnectException();
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.integration.insurance.InsurancePlugin#priorAuthApprovalFile(java.lang.String,
   * boolean)
   */
  @Override
  public PriorAuthRequestResults priorAuthApprovalFile(String fileId, boolean markAsDownloaded)
      throws ConnectException {
    try {
      Holder<Integer> downloadTxnResult = new Holder<Integer>();
      Holder<String> fileName = new Holder<String>();
      Holder<byte[]> file = new Holder<byte[]>();
      Holder<String> errorMessage = new Holder<String>();
      String user = this.userName;
      String password = this.passWord;

      ValidateTransactions erxws = new ValidateTransactions();
      ValidateTransactionsSoap erxwsoap = erxws.getValidateTransactionsSoap();

      log.info("Using endpoint URL: " + ((BindingProvider) erxwsoap).getRequestContext()
          .get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY));

      // Call API to download transaction.
      erxwsoap.downloadTransactionFile(user, password, fileId, downloadTxnResult, fileName, file,
          errorMessage);

      PriorAuthRequestResults csResult = new PriorAuthRequestResults();
      csResult.setTxnResult(downloadTxnResult);
      csResult.setErrorMessage(errorMessage);
      csResult.setFile(file);
      csResult.setFileName(fileName);
      if (downloadTxnResult.value >= 0 && markAsDownloaded) {
        // After downloading the file using file id (errors) set as downloaded.
        Holder<Integer> txnDownloadedResult = new Holder<Integer>();
        errorMessage = new Holder<String>();
        erxwsoap.setTransactionDownloaded(user, password, fileId, txnDownloadedResult,
            errorMessage);
        // we dont care too much about the response, here.
      }
      return csResult;
    } catch (Exception exception) {
      StringWriter stack = new StringWriter();
      exception.printStackTrace(new PrintWriter(stack));
      log.error(stack.toString());
      throw new ConnectException();
    }

  }

  @Override
  public String getWebservicesHost() {
    return ValidateTransactions.class.getAnnotation(WebServiceClient.class).targetNamespace();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.integration.insurance.InsurancePlugin#getPriorAuthApprovalFile(java.lang.String)
   */
  @Override
  public PriorAuthRequestResults getPriorAuthApprovalFile(String fileId) throws ConnectException {
    // call the priorAuthApprovalFile method with markAsDownloaded argument to false.
    return priorAuthApprovalFile(fileId, false);
  }
  
}
