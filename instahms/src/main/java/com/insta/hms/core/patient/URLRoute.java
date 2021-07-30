package com.insta.hms.core.patient;

public class URLRoute {
	/*
	 * Patient Index page
	 */
	public static final String VIEW_INDEX_URL       			= "/index";
	public static final String REGISTRATION_INDEX_PATH 		    = "/pages/Registration/index";
	
	public static final String PATIENT_HEADER_INDEX_URL         = "/patient/summaries";
	public static final String PATIENT_INDEX_URL 				= "/patients";
	
	/*
	 * In Patient Index page
	 */
	public static final String IN_PATIENT_INDEX_URL 				= "/inpatients";

	/**
     * OP Registration
     */
    
	public static final String OP_REGISTRATION_URL              = "/patients/opregistration";
    
	public static final String SAVED_SEARCHES_PATH			= "/savedsearches";
	public static final String QUICK_ESTIMATE  				= "/quickestimate";

	/*
	 * user list for ward assignment
	 */
	public static final String USER_WARD              			= "/wardassignment";
	public static final String USER_WARD_LIST		  			= "/pages/wardassignment/list";
	public static final String EDIT_WARD              			= "/editwardassignment";
	public static final String USER_WARD_SHOW		        	= "/pages/wardassignment/show";

	/** Mobile number validation **/
	public static final String MOBILE_NUMBER_VALIDATION = "/mobilenumber";
	
	public static final String PATIENT_COMMUNICATION_URL = "/patients/communication";
	
}
