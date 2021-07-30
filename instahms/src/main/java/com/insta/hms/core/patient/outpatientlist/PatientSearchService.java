/**
 * 
 */
package com.insta.hms.core.patient.outpatientlist;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.core.patient.PatientDetailsHelper;
import com.insta.hms.core.scheduler.PatientContactDetailsRepository;
import com.insta.hms.exception.EntityNotFoundException;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.mdm.departments.DepartmentService;
import com.insta.hms.mdm.diagdepartments.DiagDepartmentService;
import com.insta.hms.mdm.doctors.DoctorService;
import com.insta.hms.mdm.insurancecompanies.InsuranceCompanyService;
import com.insta.hms.mdm.savedsearches.SavedSearchService;
import com.insta.hms.mdm.servicedepartments.ServiceDepartmentsService;
import com.insta.hms.security.usermanager.UserService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author krishnat
 *
 */
@Service
public class PatientSearchService {
	
	@LazyAutowired SavedSearchService savedSearchService;
	@LazyAutowired DepartmentService departmentService;
	@LazyAutowired DiagDepartmentService diagDepartmentService;
	@LazyAutowired UserService userService;
	@LazyAutowired InsuranceCompanyService insuranceCompanyService;
	@LazyAutowired DoctorService doctorService;
	@LazyAutowired ServiceDepartmentsService serviceDepartmentsService;
	@LazyAutowired GenericPreferencesService genericPreferencesService;
	
	
	@LazyAutowired PatientSearchRepository repository;
	@LazyAutowired PatientContactDetailsRepository contactRepository;

	private boolean parseBool(String value) {
		return value != null && Arrays.asList("true", "y", "yes", "1").contains(value.toLowerCase());
	}
	
	public Map<String, Object> getPatientsList(Map<String, String[]> params, boolean advanced) throws ParseException, 
	UnsupportedEncodingException {
		String searchId = params.get("search_id") != null ? params.get("search_id")[0] : null;
		Map<String, String[]> savedParams = new HashMap<String,String[]>();
		boolean defaultSearch = params.get("use_default_search") != null ? parseBool(params.get("use_default_search")[0]) : false;
		if (searchId == null && defaultSearch) {
			savedParams = savedSearchService.getDefaultSearch("OP Flow");
			if (savedParams == null) {
				throw new EntityNotFoundException(new String[] {
						"exception.entity.not.found", "search_id", "default_search" });
			}
			savedParams.put("is_system_search", new String[]{ "true"});
		} else if (searchId != null) {
		  if(!NumberUtils.isDigits(searchId)){
	      throw new ValidationException("exception.invalid.parameter", new String[] { "search_id" });
	    }
			savedParams = savedSearchService.splitQuery(Integer.parseInt(searchId));
			if (savedParams == null) {
				throw new EntityNotFoundException(new String[] {
						"exception.entity.not.found", "search_id", searchId });
			}
			Map<String, Object> savedSearch = savedSearchService.getSavedSearch(Integer.parseInt(searchId));
			savedParams.put("is_system_search", new String[]{ savedSearch.get("search_type").toString().equalsIgnoreCase("system") ? "true" : "false"});
		}
		savedParams.putAll(params);
		String doctor = (String) userService.getLoggedUser().get("doctor_id");	
		
		if (doctor != null && !doctor.isEmpty()) {
			savedParams.put("doctor", new String[]{doctor});
			savedParams.put("user_is_doctor", new String[]{"Y"});
		}
		return repository.getPatientsForOpFlow(savedParams, advanced);
	}
		
	public Map<String, Object> getFilterData() {
		Map<String, Object> map = new HashMap<String, Object>();
		PatientDetailsHelper.getCommonFilterData(map);
		
		Map<String, Object> departments = new HashMap<String, Object>();
		
		List cons_depts = ConversionUtils.copyListDynaBeansToMap(departmentService.lookup(true));
		List test_depts = ConversionUtils.copyListDynaBeansToMap(diagDepartmentService.getActiveDiagDepartments());
		List service_depts = ConversionUtils.copyListDynaBeansToMap(serviceDepartmentsService.getActiveServiceDepts());
		
		departments.put("consultation", cons_depts);
		departments.put("test", test_depts);
		departments.put("service", service_depts);
		
		map.put("departments", departments);

		Map<String, String> patient_status = new LinkedHashMap<String, String>();
		patient_status.put("Consultation", "Consultation Pending");
		patient_status.put("Triage", "Triage Pending");
		map.put("patient_status", patient_status);
		
		String doctorId = (String) userService.getLoggedUser().get("doctor_id");
		String deptId = null;
		if (doctorId != null && !doctorId.equals("")) {
			Map doctorMap = doctorService.getDoctorDetails(doctorId);
			deptId = (String) doctorMap.get("dept_id");
		}
		map.put("logged_doctor", doctorId);
		map.put("logged_department", deptId);
		return map;
	}

	public Map<String, Object> getFilterDoctorData() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("doctors", ConversionUtils.copyListDynaBeansToMap(doctorService.lookup(true)));
    return map;
	}

	public Map<String, Object> getFilterInsuranceData() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("insurance_companies", ConversionUtils.copyListDynaBeansToMap(insuranceCompanyService.lookup(true)));
		return map;
	}


	/** 
	 * Search within registered patients (patients with MR) on MR, Govt ID, Phone No 
	 * @param params Request Query Params
	 * 
	 * @return Map Page of results
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
  public Map<String, Object> lookup(MultiValueMap<String, String> params) {
	  List finalPatientList = new ArrayList();
	  
		Map patientMap = repository.searchRegisteredPatients(params);
		finalPatientList.addAll((Collection) patientMap.get("patients"));
		
		boolean withSchedulerDetails = params.getFirst("with_scheduler_details") != null ? parseBool(params.getFirst("with_scheduler_details")) : false;
		if (withSchedulerDetails) {
		  Map contactMap = contactRepository.searchAvailableContacts(params);
		  finalPatientList.addAll((Collection) contactMap.get("patients"));
		}
		patientMap.put("patients", finalPatientList);
		return patientMap;
	}
	
	public List<BasicDynaBean> findPatientMatch(String patientName, String phoneNo, boolean phoneMatch,
						int acceptableDiff) {
					
					List<BasicDynaBean> patientList = null;
					String patientNameStr = patientName.replaceAll("'","''");
					String[] strTokens = patientNameStr.split("\\s+");
					patientList = repository.getPatientList(strTokens,phoneNo,phoneMatch,acceptableDiff);
						return patientList;
				}
	
	/** 
	 * Check member_id is repeated or not 
	 * @param params Request Query Params
	 * 
	 * @return Map Page of results
	 */
	public Map<String, Object> tpaMemberCheck(MultiValueMap<String, String> params) throws ParseException, 
	UnsupportedEncodingException {
		String memberId = (params.getFirst("member_id") != null && !params.getFirst("member_id").isEmpty()) ? params.getFirst("member_id") : "0";
		String sponsorId = (params.getFirst("tpa_id") != null && !params.getFirst("tpa_id").isEmpty() ) ? params.getFirst("tpa_id") : "0";
		String excludeMrNumber = (params.getFirst("exclude_mr_no") != null && !params.getFirst("exclude_mr_no").isEmpty() ) ? params.getFirst("exclude_mr_no") : "0";
    	Map<String, Object> mapResults = new HashMap<String, Object>();
        List<Map<String, Object>> results = repository.searchUsedTpaMemberIdsMap(memberId, sponsorId, excludeMrNumber);
        if (results == null) {
        	results = new ArrayList<>();
        }
        List<String> mr_nos_list = new ArrayList<>();
        for(Map<String, Object> item : results) {
        	mr_nos_list.add((String) item.get("mrno"));
        }
    	mapResults.put("parent_child_mr_nos", results);
    	mapResults.put("mr_nos", mr_nos_list);
		return mapResults;
	}

	/**
	 *
	 * @param filterText
	 * @return a map with key and an list of objects which
	 * contains confidentiality_grp_name, mr_no and patient_username
	 */
  public Map<String, Object> lookupOnMrnoName(String filterText) {
    List<BasicDynaBean> results = null;
    if (filterText.matches("([a-zA-z\\s])*")) {
      MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
      params.add("q", filterText);
      params.add("with_count", "true");
      params.add("search_on", "name");
      params.add("page_size", "20");
      params.add("allow_confidential_patients", "true");
      return repository.searchRegisteredPatients(params);
    } else {
      results = repository.filterOnMrNo(filterText);
    }
    Map<String, Object> mapResults = new HashMap<String, Object>();
    if (results == null || results.isEmpty()) {
      mapResults.put("total_records", 0);
      mapResults.put("patients", new ArrayList<Object>());
      return mapResults;
    }
    Map<String, Map<String, Object>> patientMap = new HashMap<String, Map<String, Object>>();
    for (BasicDynaBean result : results) {
      @SuppressWarnings("unchecked")
      Map<String, Object> resultMap = new HashMap<String, Object>(result.getMap());
      patientMap.put((String) resultMap.get("mr_no"), resultMap);
    }
    mapResults.put("patients", patientMap.values());
    mapResults.put("total_records", results.size());
    return mapResults;
  }

}
