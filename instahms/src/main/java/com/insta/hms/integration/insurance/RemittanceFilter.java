package com.insta.hms.integration.insurance;

/**
 * The Class RemittanceFilter.
 */
public class RemittanceFilter {
  
  /** The file id. */
  private String fileId;
  
  /** The transaction from date. */
  private String transactionFromDate;
  
  /** The transaction to date. */
  private String transactionToDate;

  /** Any part of the transaction file name to search for. */
  private String transactionFileName;

  /**
   * Gets the file id.
   *
   * @return the file id
   */
  public String getFileId() {
    return fileId;
  }

  /**
   * Sets the file id.
   *
   * @param fileId the new file id
   */
  public void setFileId(String fileId) {
    this.fileId = fileId;
  }

  /**
   * Gets the transaction from date.
   *
   * @return the transaction from date
   */
  public String getTransactionFromDate() {
    return transactionFromDate;
  }

  /**
   * Sets the transaction from date.
   *
   * @param transactionFromDate the new transaction from date
   */
  public void setTransactionFromDate(String transactionFromDate) {
    this.transactionFromDate = transactionFromDate;
  }

  /**
   * Gets the transaction to date.
   *
   * @return the transaction to date
   */
  public String getTransactionToDate() {
    return transactionToDate;
  }

  /**
   * Sets the transaction to date.
   *
   * @param transactionToDate the new transaction to date
   */
  public void setTransactionToDate(String transactionToDate) {
    this.transactionToDate = transactionToDate;
  }

  /**
   * Gets the transaction file name.
   *
   * @return the transaction file name
   */
  public String getTransactionFileName() {
    return transactionFileName;
  }

  /**
   * Sets the transaction file name.
   *
   * @param transactionFileName the new transaction file name
   */
  public void setTransactionFileName(String transactionFileName) {
    this.transactionFileName = transactionFileName;
  }
}
