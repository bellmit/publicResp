/**
 *
 */

package com.insta.hms.eauthorization.priorauth;

/**
 * The Class XMLFile.
 *
 * @author lakshmi
 *
 *         XML Format Parameter xml Transaction in the web service
 *         (getNewPriorAuthorizationTransactions) is expected to have the
 *         following format.
 */
public class XMLFile {
  /**
   * FileID – unique file id that will be used later to download the file; FileName – original file
   * name as it was uploaded by the sender; SenderID – e-claim file sender; ReceiverID – e-claim
   * file receiver; TransactionDate – value from e-claim file’s TransactionDate node; RecordCount –
   * value from e-claim file’s TransactionDate node. IsDownloaded – True if file has been already
   * downloaded from the Post Office; False otherwise. startDate - Indicates the date when the
   * transaction started endDate - Indicates the date when the transaction is due to end Note that
   * this attribute is provided only for the web service SearchTransactions.
   */

  private String fileId; // e86eb449-3e53-4ec1-a024-6b6acf76f7ef

  /**
   * The file name.
   */
  private String fileName; // C001_332ac5e1f59c.xml (or) MyFile1.xml

  /**
   * The sender id.
   */
  private String senderId; // C001 (or) MF2057

  /**
   * The receiver id.
   */
  private String receiverId; // MF2222 (or) A001

  /**
   * The transaction date.
   */
  private String transactionDate; // 11/04/2010 10:44:03

  /**
   * The record count.
   */
  private String recordCount; // 1

  /**
   * The is downloaded.
   */
  private String isDownloaded; // True

  /**
   * The start date.
   */
  private String startDate;

  /**
   * Gets the start date.
   *
   * @return the start date
   */
  public String getStartDate() {
    return startDate;
  }

  /**
   * Sets the start date.
   *
   * @param startDate the new start date
   */
  public void setStartDate(String startDate) {
    this.startDate = startDate;
  }

  /**
   * Gets the end date.
   *
   * @return the end date
   */
  public String getEndDate() {
    return endDate;
  }

  /**
   * Sets the end date.
   *
   * @param endDate the new end date
   */
  public void setEndDate(String endDate) {
    this.endDate = endDate;
  }

  /**
   * The end date.
   */
  private String endDate;

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
   * Gets the file name.
   *
   * @return the file name
   */
  public String getFileName() {
    return fileName;
  }

  /**
   * Sets the file name.
   *
   * @param fileName the new file name
   */
  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  /**
   * Gets the checks if is downloaded.
   *
   * @return the checks if is downloaded
   */
  public String getIsDownloaded() {
    return isDownloaded;
  }

  /**
   * Sets the checks if is downloaded.
   *
   * @param isDownloaded the new checks if is downloaded
   */
  public void setIsDownloaded(String isDownloaded) {
    this.isDownloaded = isDownloaded;
  }

  /**
   * Gets the receiver id.
   *
   * @return the receiver id
   */
  public String getReceiverId() {
    return receiverId;
  }

  /**
   * Sets the receiver id.
   *
   * @param receiverId the new receiver id
   */
  public void setReceiverId(String receiverId) {
    this.receiverId = receiverId;
  }

  /**
   * Gets the record count.
   *
   * @return the record count
   */
  public String getRecordCount() {
    return recordCount;
  }

  /**
   * Sets the record count.
   *
   * @param recordCount the new record count
   */
  public void setRecordCount(String recordCount) {
    this.recordCount = recordCount;
  }

  /**
   * Gets the sender id.
   *
   * @return the sender id
   */
  public String getSenderId() {
    return senderId;
  }

  /**
   * Sets the sender id.
   *
   * @param senderId the new sender id
   */
  public void setSenderId(String senderId) {
    this.senderId = senderId;
  }

  /**
   * Gets the transaction date.
   *
   * @return the transaction date
   */
  public String getTransactionDate() {
    return transactionDate;
  }

  /**
   * Sets the transaction date.
   *
   * @param transactionDate the new transaction date
   */
  public void setTransactionDate(String transactionDate) {
    this.transactionDate = transactionDate;
  }
}
