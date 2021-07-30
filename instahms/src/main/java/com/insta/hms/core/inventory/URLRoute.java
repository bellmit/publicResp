package com.insta.hms.core.inventory;

/**
 * 
 * @author irshadmohammed
 *
 */
public class URLRoute {
	/**
	 * Sales Related URLS
	 */
	
	// TODO (Inventory cleanup) : cleanup the URL
	public static final String SALES  = "/sales";
	public static final String GET_SELLING_PRICE = "/getsellingprice";
	public static final String GET_TAX_DETAILS = "/gettaxdetails";
	public static final String GET_ALL_ITEMS_TAX_DETAILS = "/getallitemstaxdetails";
	public static final String GET_SUPPORTED_TAX_GROUPS = "/taxgroups";
	public static final String GET_BASE_TAX_DETAILS = "/getbasetaxdetails";
	
	/**
	 * Issues Related URLS
	 */
	public static final String ISSUES  = "/issues";
	public static final String ISSUES_GET_MARKUP_RATE = "/getmarkuprate";
	
	/**
	 * Patient Issues Related URLS
	 */
	public static final String PATIENT_ISSUES  = "/patientissues";
	public static final String PATIENT_ISSUES_SCREEN  = "/add";
	public static final String GET_PATIENT_DETAILS  = "/getpatientdetails";
	public static final String GET_ITEM_BATCH_DETAILS  = "/getitembatchdetails";
	public static final String GET_ITEM_AMOUNT_DETAILS  = "/getitemamountdetails";
	public static final String GET_BULK_ITEM_AMOUNT_DETAILS  = "/getbulkitemamountdetails";
	public static final String SAVE_ISSUE_DETAILS  = "/saveissuedetails";
  public static final String GET_INSURANCE_CATEGORY_PAYABLE_STATUS = "/getinsurancecategorypaystatus";
  public static final String GET_ORDERKIT_ITEMS = "/getorderkititems";
  public static final String GENERATE_GATE_PASS = "/generategatepass";
  public static final String GET_ISSUES_CHARGE_CLAIMS = "/getissueschargeclaims";
  public static final String GET_CLAIM_AMOUNT = "/getclaimamount";
	
	/**
	 * Indent Related URLS
	 */
	public static final String PATIENTINDENT  = "/indent";
	public static final String GET_ITEM_LIST  = "/getItemList";
	
	/**
	 * Procurement Related URLS
	 */
	public static final String PO  = "/purchaseorder";
	public static final String PO_TAX  = "/potaxdetails";
	public static final String PO_ITEM_TAX  = "/itemtaxdetails";
	public static final String PO_TAX_CHANGE  = "/changepotaxdetails";
	
	public static final String STOCK = "/stocks";
	public static final String STOCK_ENTRY_TAX = "/grntaxdetails";
	public static final String STOCK_DEBIT_TAX = "/debitnotetaxdetails";
	public static final String STOCK_ENTRY_TAX_CHANGE = "/changetaxdetails";
	public static final String GET_GRN_ITEMS_AND_DETAILS = "/getgrnitemsanddetails";

	
	/**
	 * Medicine stock urls
	 */
	public static final String STOCK_DETAILS = "/stockdetails";
	public static final String GET_STOCK_IN_STORE = "/getstockinstore";
	
	/**
	 * Stock Management URLs
	 */
	public static final String STOCK_MGMT_URL = "/inventory/stockmanagement";
	public static final String STOCK_MGMT_INDEX_URL = "/index";
	public static final String GET_STORES_ITEM_BATCH_COUNT = "/stocktake/storeslist";
	public static final String CREATE_STOCK_TAKE = "/stocktake/create";
	
  public static final String STOCK_TAKE_PRINT_OPTIONS = "/stocktake/printoptions";

  public static final String STOCK_TAKE_LIST = "/stocktake/list";
  public static final String STOCK_TAKE_LOOKUP = "/stocktake/lookup";
  
  
  public static final String STOCK_TAKE_FILTERDATA = "/stocktake/filterdata";
  public static final String STOCK_TAKE_ACTIONMAP = "/stocktake/actions";

  public static final String STOCK_TAKE_ITEMS_FILTERDATA = "/stocktake/items/filterdata";
  public static final String STOCK_TAKE_ITEMS_LOOKUP = "/stocktake/items/lookup";
  
  public static final String STOCK_TAKE_COUNT_BASE = STOCK_MGMT_URL + "/stocktake/count";
  public static final String STOCK_TAKE_COUNT_SUMMARY = "/summary";
  public static final String STOCK_TAKE_COUNT_LIST = "/list";
  public static final String STOCK_TAKE_COUNT_UPDATE = "/update";
  public static final String STOCK_TAKE_COUNT_FINALIZE = "/stocktake/count/finalize";

  public static final String STOCK_TAKE_RECONCILE_BASE = STOCK_MGMT_URL + "/stocktake/reconcile";
  public static final String STOCK_TAKE_RECONCILE_UPDATE = "/update";
  public static final String STOCK_TAKE_RECONCILE_FINALIZE = "/finalize";
  public static final String STOCK_TAKE_COUNT_REOPEN = "/reopen";
  
  public static final String STOCK_TAKE_APPROVAL_BASE = STOCK_MGMT_URL + "/stocktake/approve";
  public static final String STOCK_TAKE_RECONCILE_REOPEN = "/reopen";
  public static final String STOCK_TAKE_APPROVE_ADJUST = "/adjust";

  public static final String STOCK_TAKE_VIEW_BASE = STOCK_MGMT_URL + "/stocktake/view";

  public static final String STOCK_TAKE_EXTENDED_SUMMARY = "/summary";
  public static final String STOCK_TAKE_EXTENDED_LIST = "/list";
  public static final String STOCK_TAKE_PRINT = "/print";

  public static final String STOCK_TAKE_CANCEL = "/stocktake/cancel";

  /**
   * Package details URLS
   */
  public static final String GET_PKG_DETAILS = "/packagedetails";

}

