package com.insta.hms.payments;


import org.apache.struts.action.ActionForm;

public class PaymentDashboardForm extends ActionForm {


	private String payeeName;
	private String fdate;
	private String tdate;

	
	private boolean payAll;
	private boolean typeCash;
	private boolean typeDoctor;
	private boolean typePresDr;
	private boolean typeReferral;
	private boolean typeOtTest;
	private boolean typeSupplier;

	private String pageNum;
	private String pagSize;

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

	public String getPayeeName() { return payeeName; }
	public void setPayeeName(String v) { payeeName = v; }

	public String getFdate() { return fdate; }
	public void setFdate(String v) { fdate = v; }

	public String getTdate() { return tdate; }
	public void setTdate(String v) { tdate = v; }

	public String getPageNum() { return pageNum; }
	public void setPageNum(String v) { pageNum = v; }

	public String getPagSize() { return pagSize; }
	public void setPagSize(String v) { pagSize = v; }

	public boolean getTypePresDr() { return typePresDr; }
	public void setTypePresDr(boolean v) { typePresDr = v; }


}

