package com.insta.hms.payments;


import org.apache.struts.action.ActionForm;

public class PaymentVoucherForm extends ActionForm{


	private String payeeName;
	private String voucherNo;
	private String payDate;
	private String totalAmount;
	private String serviceTax;
	private String tds;
	private String netPayment;

	private int paymentModeId;
	private int cardTypeId;
	private String paymentRemarks;
	private String paymentBank;
	private String paymentRefNum;
	private String counter;

	private String pageType;
	private String pageNum;

	private String fdate;
	private String tdate;

	private String paymentType;
	private String payeeId;

	private String payType;

	private boolean payAll;
	private boolean typeCash;
	private boolean typeDoctor;
	private boolean typePresDr;
	private boolean typeReferral;
	private boolean typeOtTest;
	private boolean typeSupplier;

	private String roundOffAmt;

	private String screen;
	private String paymentCategory;

	public String getPayeeName() { return payeeName; }
	public void setPayeeName(String v) { payeeName = v; }

	public String getPaymentType() { return paymentType; }
	public void setPaymentType(String v) { paymentType = v; }

	public String getVoucherNo() { return voucherNo; }
	public void setVoucherNo(String v) { voucherNo = v; }

	public String getPayDate() { return payDate; }
	public void setPayDate(String v) { payDate = v; }

	public String getTotalAmount() { return totalAmount; }
	public void setTotalAmount(String v) { totalAmount = v; }

	public String getServiceTax() { return serviceTax; }
	public void setServiceTax(String v) { serviceTax = v; }

	public String getTds() { return tds; }
	public void setTds(String v) { tds = v; }

	public String getNetPayment() { return netPayment; }
	public void setNetPayment(String v) { netPayment = v; }

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

	public String getCounter() { return counter; }
	public void setCounter(String v) { counter = v; }

	public String getPageType() { return pageType; }
	public void setPageType(String v) { pageType = v; }

	public String getPageNum() { return pageNum; }
	public void setPageNum(String v) { pageNum = v; }

	public boolean getPayAll() { return payAll; }
	public void setPayAll(boolean v) { payAll = v; }

	public boolean getTypeCash() { return typeCash; }
	public void setTypeCash(boolean v) { typeCash = v; }

	public boolean getTypeDoctor() { return typeDoctor; }
	public void setTypeDoctor(boolean v) { typeDoctor = v; }

	public boolean getTypeReferral() { return typeReferral; }
	public void setTypeReferral(boolean v) { typeReferral = v; }

	public boolean getTypeOtTest() { return typeOtTest; }
	public void setTypeOtTest(boolean v) { typeOtTest = v; }

	public boolean getTypeSupplier() { return typeSupplier; }
	public void setTypeSupplier(boolean v) { typeSupplier = v; }

	public boolean getTypePresDr() { return typePresDr; }
    public void setTypePresDr(boolean v) { typePresDr = v; }

	public String getFdate() { return fdate; }
	public void setFdate(String v) { fdate = v; }

	public String getTdate() { return tdate; }
	public void setTdate(String v) { tdate = v; }

	public String getPayeeId() { return payeeId; }
	public void setPayeeId(String v) { payeeId = v; }

	public String getPayType() { return payType; }
	public void setPayType(String v) { payType = v; }

	public String getScreen() { return screen; }
	public void setScreen(String v) { screen = v; }

	public String getPaymentCategory() { return paymentCategory; }
	public void setPaymentCategory(String v) { paymentCategory = v; }

	public String getRoundOffAmt() { return roundOffAmt; }
	public void setRoundOffAmt(String v) { roundOffAmt = v; }

}
