package com.insta.hms.billing;

/*
 * DTO representing Bill Details: includes the following sub-objects:
 *   - The Bill object itself
 *   - List of all charges in the bill
 *   - List of all Receipts associated with the bill
 *   - List of all refunds made for the bill
 *   - List of all third party payments made against this bill
 */
import java.util.List;

public class BillDetails {

	private Bill bill;
	private List charges;	// ChargeDTO objects
	private List receipts;	// Receipt objects
	private List refunds;	// Refund objects
	private List thirdPartyReceipts;	// ThirdPartyPayment objects

	public Bill getBill() { return bill; }
	public void setBill(Bill v) { bill = v; }

	public List getCharges() { return charges; }
	public void setCharges(List v) { charges = v; }

	public List getReceipts() { return receipts; }
	public void setReceipts(List v) { receipts = v; }

	public List getRefunds() { return refunds; }
	public void setRefunds(List v) { refunds = v; }

	public List getThirdPartyReceipts() { return thirdPartyReceipts; }
	public void setThirdPartyReceipts(List v) { thirdPartyReceipts = v; }

	public float getExistingCredits() {
		// TODO: use this method instead of getPatExistingCredits in the BO.
		float credits = 0;
		return credits;
	}
}

