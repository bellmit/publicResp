/**
 *
 */
package com.insta.hms.pbmauthorization;

/**
 * @author lakshmi
 *
 */
public class PriorAuthorizationHeader {

	private String senderID;
	private String receiverID;
	private String transactionDate;
	private Integer recordCount;
	private String dispositionFlag;

	public String getDispositionFlag() {
		return dispositionFlag;
	}
	public void setDispositionFlag(String dispositionFlag) {
		this.dispositionFlag = dispositionFlag;
	}
	public String getReceiverID() {
		return receiverID;
	}
	public void setReceiverID(String receiverID) {
		this.receiverID = receiverID;
	}
	public Integer getRecordCount() {
		return recordCount;
	}
	public void setRecordCount(Integer recordCount) {
		this.recordCount = recordCount;
	}
	public String getSenderID() {
		return senderID;
	}
	public void setSenderID(String senderID) {
		this.senderID = senderID;
	}
	public String getTransactionDate() {
		return transactionDate;
	}
	public void setTransactionDate(String transactionDate) {
		this.transactionDate = transactionDate;
	}
}
