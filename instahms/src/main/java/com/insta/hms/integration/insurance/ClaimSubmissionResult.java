package com.insta.hms.integration.insurance;

import javax.xml.ws.Holder;

/**
 * The Class ClaimSubmissionResult.
 */
public class ClaimSubmissionResult {

  /** The upload txn result. */
  private Holder<Integer> uploadTxnResult;

  /** The error message. */
  private Holder<String> errorMessage;

  /** The error report. */
  private Holder<byte[]> errorReport;

  /**
   * Gets the upload txn result.
   *
   * @return the upload txn result
   */
  public Holder<Integer> getUploadTxnResult() {
    return uploadTxnResult;
  }

  /**
   * Sets the upload txn result.
   *
   * @param uploadTxnResult the new upload txn result
   */
  public void setUploadTxnResult(Holder<Integer> uploadTxnResult) {
    this.uploadTxnResult = uploadTxnResult;
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

}
