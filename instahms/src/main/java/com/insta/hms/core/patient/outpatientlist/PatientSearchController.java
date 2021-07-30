/**
 * 
 */
package com.insta.hms.core.patient.outpatientlist;

import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.patient.URLRoute;

import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author krishnat
 *
 */
@RestController
@RequestMapping(URLRoute.PATIENT_INDEX_URL)
public class PatientSearchController extends BaseRestController {
	
	@LazyAutowired PatientSearchService patientSearchService;
	
	@IgnoreConfidentialFilters
    @RequestMapping(value = {"/list"} , method = RequestMethod.GET)
	public Map<String, Object> list(HttpServletRequest req, HttpServletResponse resp)
			throws ParseException, NumberFormatException, UnsupportedEncodingException {
        return patientSearchService.getPatientsList(req.getParameterMap(), false);
	}
	
	@IgnoreConfidentialFilters
    @RequestMapping(value = {"/advanceList"} , method = RequestMethod.GET)
	public Map<String, Object> advanceSearch(HttpServletRequest req, HttpServletResponse resp) 
		throws ParseException, NumberFormatException, UnsupportedEncodingException {
    	return patientSearchService.getPatientsList(req.getParameterMap(), true);
		//return patientSearchService.getPatientsAdvanced(req.getParameterMap());
	}
	
	@IgnoreConfidentialFilters
	@RequestMapping(value = {"/getFilterData"}, method = RequestMethod.GET)
	public Map<String, Object> getFilterData(HttpServletRequest req, HttpServletResponse resp) {
		return patientSearchService.getFilterData();
	}

	@IgnoreConfidentialFilters
	@RequestMapping(value = {"/getFilterDoctorData"}, method = RequestMethod.GET)
	public Map<String, Object> getFilterDoctorData(HttpServletRequest req, HttpServletResponse resp) {
		return patientSearchService.getFilterDoctorData();
	}

	@IgnoreConfidentialFilters
	@RequestMapping(value = {"/getFilterInsuranceData"}, method = RequestMethod.GET)
	public Map<String, Object> getFilterInsuranceData(HttpServletRequest req, HttpServletResponse resp) {
		return patientSearchService.getFilterInsuranceData();
	}

	@IgnoreConfidentialFilters
	@RequestMapping(value = {"/lookup"}, method = RequestMethod.GET)
	public Map<String, Object> doLookup(@RequestParam MultiValueMap<String, String> params) {
		return patientSearchService.lookup(params);
	}
	
    @IgnoreConfidentialFilters
	@RequestMapping(value = {"/tpaMemberCheck"}, method = RequestMethod.GET)
	public Map<String, Object> doTpaMemberCheck(@RequestParam MultiValueMap<String, String> params) 
		throws UnsupportedEncodingException, ParseException {
		return patientSearchService.tpaMemberCheck(params);
	}

  @IgnoreConfidentialFilters
  @RequestMapping(value = { "/lookupMrnoName" }, method = RequestMethod.GET)
  public Map<String, Object> lookupMrnoName(@RequestParam MultiValueMap<String, String> params) {
    if (params.get("filterText") == null) {
      Map<String, Object> mapResults = new HashMap<String, Object>();
      mapResults.put("total_records", 0);
      mapResults.put("patients", new ArrayList<Object>());
      return mapResults;
    }
    String filterText = (String) params.get("filterText").get(0);
    return patientSearchService.lookupOnMrnoName(filterText);
  }
}
