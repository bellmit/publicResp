/**
 * 
 */
package com.insta.hms.core.patient;

import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.SearchQueryAssembler;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author anup vishwas
 *
 */

/* this class is basically used to represent common
 * data for multiple end points.
 * ex: advance filter option, search filters etc.
 */

public class PatientDetailsHelper {

    static MessageUtil messageUtil = ApplicationContextProvider.getBean(MessageUtil.class);
	
	public static void getCommonFilterData(Map<String, Object> map) {
		
		Map<String, String> visit_status = new LinkedHashMap<String, String>();
		visit_status.put("A", messageUtil.getMessage("ui.label.active"));
		visit_status.put("I", messageUtil.getMessage("ui.label.inactive"));
		map.put("visit_status", visit_status);
		
		Map<String, String> bill_status = new LinkedHashMap<String, String>();
		bill_status.put("A", messageUtil.getMessage("ui.label.open"));
		bill_status.put("F", messageUtil.getMessage("ui.label.finalized"));
		bill_status.put("C", messageUtil.getMessage("ui.label.closed"));
		map.put("bill_status", bill_status);

	}
	
	public static void appendCommonFilters(StringBuilder filterOn, String findString, SearchQueryAssembler sqa) {
		
		boolean first = true;
		for (String userInput: findString.split(" ")) {
			String reverseVal = new StringBuilder(userInput).reverse().toString();
			String lowerVal = userInput.toLowerCase();
			if (!first) { 
				filterOn.append(" AND ");
			}
			first = false;
    		// since phone number is allowing the special chars like (,),+,-and space we should check for them also.
    		if (userInput.matches("(\\d|\\+|-|\\(|\\)|\\s)*")) {    			
    			filterOn.append(" (REVERSE(pd.mr_no) LIKE ? "
    					+ " OR REVERSE(COALESCE(pd.original_mr_no,'')) LIKE ? "
    					+ " OR REVERSE(COALESCE(pd.oldmrno,'')) LIKE ? "
    					+ " OR patient_phone LIKE ? "
    					+ " OR REPLACE(pd.patient_phone, CASE WHEN pd.patient_phone_country_code IS NULL THEN '' ELSE pd.patient_phone_country_code END,'') LIKE ? "
    					+ " OR LOWER(government_identifier) LIKE ?) ");
    			sqa.addInitValue(SearchQueryAssembler.STRING, reverseVal + "%");
    			sqa.addInitValue(SearchQueryAssembler.STRING, reverseVal + "%");
    			sqa.addInitValue(SearchQueryAssembler.STRING, reverseVal + "%");
    			sqa.addInitValue(SearchQueryAssembler.STRING, userInput + "%");
    			sqa.addInitValue(SearchQueryAssembler.STRING, userInput + "%");
    			sqa.addInitValue(SearchQueryAssembler.STRING, lowerVal + "%");
    		} else if (userInput.matches("\\D+")) {
    			filterOn.append(" (LOWER(pd.patient_name) LIKE ? "
						+ " OR LOWER(pd.middle_name) LIKE ? "
						+ " OR LOWER(pd.last_name) LIKE ? "
						+ " OR LOWER(pd.patient_name) LIKE ? "
						+ " OR LOWER(pd.middle_name) LIKE ? "
						+ " OR LOWER(pd.last_name) LIKE ? "
						+ " OR LOWER(pd.government_identifier) LIKE ?) ");
    			sqa.addInitValue(SearchQueryAssembler.STRING, lowerVal + "%");
    			sqa.addInitValue(SearchQueryAssembler.STRING, lowerVal + "%");
     			sqa.addInitValue(SearchQueryAssembler.STRING, lowerVal + "%");
    			sqa.addInitValue(SearchQueryAssembler.STRING, "% " + lowerVal + "%");
    			sqa.addInitValue(SearchQueryAssembler.STRING, "% " + lowerVal + "%");
    			sqa.addInitValue(SearchQueryAssembler.STRING, "% " + lowerVal + "%");
    			sqa.addInitValue(SearchQueryAssembler.STRING, lowerVal + "%"); 
    		} else {
    			filterOn.append(" (REVERSE(pd.mr_no) LIKE ? "
    					+ " OR REVERSE(COALESCE(pd.original_mr_no,'')) LIKE ? "
    					+ " OR REVERSE(COALESCE(pd.oldmrno,'')) LIKE ? "
    					+ " OR LOWER(government_identifier) LIKE ?) ");
    			sqa.addInitValue(SearchQueryAssembler.STRING, reverseVal + "%");
    			sqa.addInitValue(SearchQueryAssembler.STRING, reverseVal + "%");
    			sqa.addInitValue(SearchQueryAssembler.STRING, reverseVal + "%");
    			sqa.addInitValue(SearchQueryAssembler.STRING, lowerVal + "%");
    		}
        }
		sqa.appendToQuery(filterOn.toString());		
	}
	
	public static void copyListingParams(Map<String, String[]> fromMap, Map<String, String[]> toMap) {
		
		if (fromMap == null || toMap == null)
			return;
		
		if (fromMap.get("page_num") != null)
			toMap.put("page_num", fromMap.get("page_num"));
		if (fromMap.get("page_size") != null)
			toMap.put("page_size", fromMap.get("page_size"));
		if (fromMap.get("sort_order") != null)
			toMap.put("sort_order", fromMap.get("sort_order"));
		if (fromMap.get("sort_reverse") != null)
			toMap.put("sort_reverse", fromMap.get("sort_reverse"));

	}
	
}
