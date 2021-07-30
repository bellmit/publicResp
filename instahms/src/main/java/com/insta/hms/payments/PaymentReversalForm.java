package com.insta.hms.payments;


import org.apache.struts.action.ActionForm;

public class PaymentReversalForm extends ActionForm {

	private String payeeName;
	private String paymentType;
	private String payDate;
	private String counter;

	private String payeeId;

	private String allPayees;

	private String[] description;
	private String[] voucherNo;
	private String[] amount; //new  BigDecimal(0);

	private int paymentModeId;
	private int cardTypeId;
	private String paymentBank;
	private String paymentRefNum;
	private String paymentRemarks;
	private String tds;


	public String getPayeeName() { return payeeName; }
	public void setPayeeName(String v) { payeeName = v; }

	public String getPaymentType() { return paymentType; }
	public void setPaymentType(String v) { paymentType = v; }

	public String getPayDate() { return payDate; }
	public void setPayDate(String v) { payDate = v; }

	public String getCounter() { return counter; }
	public void setCounter(String v) { counter = v; }


	public String getPayeeId() { return payeeId; }
	public void setPayeeId(String v) { payeeId = v; }

	public String getAllPayees() { return allPayees; }
	public void setAllPayees(String v) { allPayees = v; }

	public String[] getDescription() { return description; }
	public void setDescription(String[] v) { description = v; }

	public String[] getVoucherNo() { return voucherNo; }
	public void setVoucherNo(String[] v) { voucherNo = v; }

	public String[] getAmount() { return amount; }
	public void setAmount(String[] v) { amount = v; }

	public int getPaymentModeId() { return paymentModeId; }
	public void setPaymentModeId(int v) { paymentModeId = v; }

	public int getCardTypeId() { return cardTypeId; }
	public void setCardTypeId(int v) { cardTypeId = v; }

	public String getPaymentRemarks() { return paymentRemarks; }
	public void setPaymentRemarks(String v) { paymentRemarks = v; }

	public String getPaymentBank() { return paymentBank; }
	public void setPaymentBank(String v) { paymentBank = v; }

	public String getPaymentRefNum() { return paymentRefNum; }
	public void setPaymentRefNum(String v) { paymentRefNum = v; }

	public String getTds() { return tds; }
	public void setTds(String v) { tds = v; }

}
