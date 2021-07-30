package com.insta.hms.payments;


import org.apache.struts.action.ActionForm;


public class MiscellaneousPaymentsForm extends ActionForm{



	private String counter;
	private String paydate;
	private String payTime;

	private String name;
	private String[] description;
	private String[] category;
	private String[] amount;
	private String[] accountHead;

	private boolean[] add;


	public String getName() { return name; }
	public void setName(String v) { name = v; }

	public String[] getDescription() { return description; }
	public void setDescription(String[] v) { description = v; }

	public String[] getCategory() { return category; }
	public void setCategory(String[] v) { category = v; }

	public String[] getAmount() { return amount; }
	public void setAmount(String[] v) { amount = v; }

	public String getCounter() { return counter; }
	public void setCounter(String v) { counter = v; }

	public String getPaydate() { return paydate; }
	public void setPaydate(String v) { paydate = v; }

	public boolean[] getAdd() { return add; }
	public void setAdd(boolean[] v) { add = v; }

	public String[] getAccountHead() { return accountHead; }
	public void setAccountHead(String[] v) { accountHead = v; }

	public String getPayTime() { return payTime; }
	public void setPayTime(String v) { payTime = v; }

}
