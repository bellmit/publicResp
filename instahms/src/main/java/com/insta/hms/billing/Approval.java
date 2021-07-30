package com.insta.hms.billing;
import java.sql.Date;

public class Approval {
	private String approvalId;
	private Date approvalDate;
	private String username;
	private String exported;
	private int numTransactions;
	private Date minTxnDate;
	private Date maxTxnDate;

	/*
	 * Constants
	 */
	public static final String APPROVAL_EXPORTED = "Y";
	public static final String APPROVAL_NOT_EXPORTED = "N";

	/*
	 * Default constructor
	 */
	public Approval() {
		approvalDate = new Date(new java.util.Date().getTime());
		exported = APPROVAL_NOT_EXPORTED; 
	}


	/*
	 * Accessors
	 */
	public String getApprovalId() { return approvalId; }
	public void setApprovalId(String v) { approvalId = v; }

	public Date getApprovalDate() { return approvalDate; }
	public void setApprovalDate(Date v) { approvalDate = v; }

	public String getUsername() { return username; }
	public void setUsername(String v) { username = v; }

	public String getExported() { return exported; }
	public void setExported(String v) { exported = v; }

	public int getNumTransactions() { return numTransactions; }
	public void setNumTransactions(int v) { numTransactions = v; }

	public Date getMinTxnDate() { return minTxnDate; }
	public void setMinTxnDate(Date v) { minTxnDate = v; }

	public Date getMaxTxnDate() { return maxTxnDate; }
	public void setMaxTxnDate(Date v) { maxTxnDate = v; }

}

