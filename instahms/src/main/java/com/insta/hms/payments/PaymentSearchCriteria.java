package com.insta.hms.payments;

public class PaymentSearchCriteria {


	public String payeeName;
	public String voucherNo;

	public java.util.ArrayList payType;
	public java.util.ArrayList chargeGroup;
	public java.util.ArrayList billStatus;
	public java.util.ArrayList patientType;
	public java.util.ArrayList tpaType;

	public String mrNo;

	public String doctorId;

	public String ohId;

	public int nonEligiblePayment;
	public String nonEligibleValue;

	public String doctorType;

	public java.sql.Date  fromDate;
	public java.sql.Date toDate;

	public String chargeHead;
	public String activityId;

	public int chargePageNo=1;
	public int chargePageSize=10;

	public int paymentPageNo=1;
	public int paymentPageSize=10;

	public String username;

	public int pageSize=0;
	public int pageNo=0;

	public String screenType;

	public String paymentCategory;

	public String dateType;

	public PaymentSearchCriteria(){
	}

}
