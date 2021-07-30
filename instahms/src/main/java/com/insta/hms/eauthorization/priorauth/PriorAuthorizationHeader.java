/**
 *
 */

package com.insta.hms.eauthorization.priorauth;

/**
 * The Class PriorAuthorizationHeader.
 *
 * @author lakshmi
 */
public class PriorAuthorizationHeader {

  /**
   * The sender ID.
   */
  private String senderID;

  /**
   * The receiver ID.
   */
  private String receiverID;

  /**
   * The transaction date.
   */
  private String transactionDate;

  /**
   * The record count.
   */
  private Integer recordCount;

  /**
   * The disposition flag.
   */
  private String dispositionFlag;

  /**
   * Gets the disposition flag.
   *
   * @return the disposition flag
   */
  public String getDispositionFlag() {
    return dispositionFlag;
  }

  /**
   * Sets the disposition flag.
   *
   * @param dispositionFlag the new disposition flag
   */
  public void setDispositionFlag(String dispositionFlag) {
    this.dispositionFlag = dispositionFlag;
  }

  /**
   * Gets the receiver ID.
   *
   * @return the receiver ID
   */
  public String getReceiverID() {
    return receiverID;
  }

  /**
   * Sets the receiver ID.
   *
   * @param receiverID the new receiver ID
   */
  public void setReceiverID(String receiverID) {
    this.receiverID = receiverID;
  }

  /**
   * Gets the record count.
   *
   * @return the record count
   */
  public Integer getRecordCount() {
    return recordCount;
  }

  /**
   * Sets the record count.
   *
   * @param recordCount the new record count
   */
  public void setRecordCount(Integer recordCount) {
    this.recordCount = recordCount;
  }

  /**
   * Gets the sender ID.
   *
   * @return the sender ID
   */
  public String getSenderID() {
    return senderID;
  }

  /**
   * Sets the sender ID.
   *
   * @param senderID the new sender ID
   */
  public void setSenderID(String senderID) {
    this.senderID = senderID;
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
