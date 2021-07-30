package com.insta.hms.integration.insurance;

import javax.xml.ws.Holder;

/**
 * The Class PriorAuthRequestResults.
 */
public class PriorAuthRequestResults {

  /** The txn result. */
  private Holder<Integer> txnResult;
  
  /** The error message. */
  private Holder<String> errorMessage;
  
  /** The error report. */
  private Holder<byte[]> errorReport;
  
  /** The xml transactions. */
  private Holder<String> xmlTransactions;
  
  /** The file. */
  private Holder<byte[]> file;
  
  /** The file name. */
  private Holder<String> fileName;

  /**
   * Gets the txn result.
   *
   * @return the txn result
   */
  public Holder<Integer> getTxnResult() {
    return txnResult;
  }

  /**
   * Sets the txn result.
   *
   * @param txnResult the new txn result
   */
  public void setTxnResult(Holder<Integer> txnResult) {
    this.txnResult = txnResult;
  }

  /**
   * Gets the file.
   *
   * @return the file
   */
  public Holder<byte[]> getFile() {
    return file;
  }

  /**
   * Sets the file.
   *
   * @param file the new file
   */
  public void setFile(Holder<byte[]> file) {
    this.file = file;
  }

  /**
   * Gets the file name.
   *
   * @return the file name
   */
  public Holder<String> getFileName() {
    return fileName;
  }

  /**
   * Sets the file name.
   *
   * @param fileName the new file name
   */
  public void setFileName(Holder<String> fileName) {
    this.fileName = fileName;
  }

  /**
   * Gets the error message.
   *
   * @return the error message
   */
  public Holder<String> getErrorMessage() {
    return errorMessage;
  }

  /**
   * Sets the error message.
   *
   * @param errorMessage the new error message
   */
  public void setErrorMessage(Holder<String> errorMessage) {
    this.errorMessage = errorMessage;
  }

  /**
   * Gets the error report.
   *
   * @return the error report
   */
  public Holder<byte[]> getErrorReport() {
    return errorReport;
  }

  /**
   * Sets the error report.
   *
   * @param errorReport the new error report
   */
  public void setErrorReport(Holder<byte[]> errorReport) {
    this.errorReport = errorReport;
  }

  /**
   * Gets the xml transactions.
   *
   * @return the xml transactions
   */
  public Holder<String> getXmlTransactions() {
    return xmlTransactions;
  }

  /**
   * Sets the xml transactions.
   *
   * @param xmlTransactions the new xml transactions
   */
  public void setXmlTransactions(Holder<String> xmlTransactions) {
    this.xmlTransactions = xmlTransactions;
  }

}
