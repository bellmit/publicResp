/**
 *
 */
package com.insta.hms.pbmauthorization;

/**
 * @author lakshmi
 *
 *         XML Format Parameter xml Transaction in the web service
 *         (getNewPriorAuthorizationTransactions) is expected to have the
 *         following format.
 *
 */
public class XMLFile {
	/**
	 *  FileID – unique file id that will be used later to download the file;
		FileName – original file name as it was uploaded by the sender;
		SenderID – e-claim file sender;
		ReceiverID – e-claim file receiver;
		TransactionDate – value from e-claim file’s TransactionDate node;
		RecordCount – value from e-claim file’s TransactionDate node.
		IsDownloaded – True if file has been already downloaded from the Post Office; False otherwise.
		Note that this attribute is provided only for the web service SearchTransactions.
	 */

	private String fileId; // e86eb449-3e53-4ec1-a024-6b6acf76f7ef
	private String fileName; // C001_332ac5e1f59c.xml (or) MyFile1.xml
	private String senderId; // C001 (or) MF2057
	private String receiverId; // PF1506 (or) A001
	private String transactionDate; // 11/04/2010 10:44:03
	private String recordCount; // 1
	private String isDownloaded; // True

	public String getFileId() {
		return fileId;
	}
	public void setFileId(String fileId) {
		this.fileId = fileId;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getIsDownloaded() {
		return isDownloaded;
	}
	public void setIsDownloaded(String isDownloaded) {
		this.isDownloaded = isDownloaded;
	}
	public String getReceiverId() {
		return receiverId;
	}
	public void setReceiverId(String receiverId) {
		this.receiverId = receiverId;
	}
	public String getRecordCount() {
		return recordCount;
	}
	public void setRecordCount(String recordCount) {
		this.recordCount = recordCount;
	}
	public String getSenderId() {
		return senderId;
	}
	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}
	public String getTransactionDate() {
		return transactionDate;
	}
	public void setTransactionDate(String transactionDate) {
		this.transactionDate = transactionDate;
	}
}
