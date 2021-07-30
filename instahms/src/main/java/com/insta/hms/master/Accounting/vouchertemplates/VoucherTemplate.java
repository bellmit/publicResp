package com.insta.hms.master.Accounting.vouchertemplates;

public enum VoucherTemplate {

	CSRECEIPT		("CSRECEIPT", "CSRECEIPT_voucher"),
	DEPOSIT			("DEPOSIT", "DEPOSIT_voucher"),
	HOSPBILL		("HOSPBILL", "HOSPBILL_voucher"),
	PAYMENTDUES		("PAYMENTDUES", "PAYMENTDUES_voucher"),
	PHARMACYBILL	("PHARMACYBILL", "PHARMACYBILL_voucher"),
	PP				("PP", "PP_voucher"),
	PR				("PR", "PR_voucher"),
	PURCHASE		("PURCHASE", "PURCHASE_voucher"),
	RECEIPT			("RECEIPT", "RECEIPT_voucher"),
	SRWDEBITNOTE	("SRWDEBITNOTE", "SRWDEBITNOTE_voucher"),
	CSRETURNED		("CSRETURNED", "CSRETURNED_voucher"),
	CSISSUED		("CSISSUED", "CSISSUED_voucher");


	private String ftlType=null;
	private String ftlFileName=null;

	private VoucherTemplate(final String ftlType,final String ftlFileName){
		this.ftlType=ftlType;
		this.ftlFileName=ftlFileName;
	}

	public String getFtlType(){
		return ftlType;
	}

	public String getFtlFileName(){
		return ftlFileName;
	}

}
