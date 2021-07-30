package com.insta.hms.core.billing;

public class URLRoute {
	
	public static final String BILL  = "/billing";
	
	public static final String ITEM_TAX = "/itemtax";
	
	public static final String DO_TRANSACTION = "/dotransaction";

	public static final String CANCEL_TRANSACTION = "/canceltransaction";

	public static final String CHECK_TRANSACTION_STATUS = "/checktransactionstatus";
	
	public static final String CANCEL_PENDING_TRANSACTION = "/cancelpendingtransaction";

	public static final String SET_PRINTED_STATUS = "/setprintedstatus";
	
	public static final String GET_OPEN_FINALIZED_BILL_DETAILS = "/getopenfinalizedbilldetails";

	public static final String GET_IS_BILL_LOCKED = "/getisbilllocked";


  public static final String IP_URL = "/billing/ipflow";

  public static final String OP_URL = "/billing/opflow";

  public static final String VIEW_INDEX_URL = "/index";

	public static final String GET_NEW_BILL = "/newbill";
	public static final String GET_NEW_BILL_CREATION_PAGE 	= "/pages/billing/NewBill";
	
  /** The Constant RUN_ALLOCATION. */
  public static final String RUN_ALLOCATION = "/allocate";

}
