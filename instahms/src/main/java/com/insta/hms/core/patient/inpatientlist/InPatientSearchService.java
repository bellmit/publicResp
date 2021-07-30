/**
 * 
 */

package com.insta.hms.core.patient.inpatientlist;

import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.clinicalpreferences.ClinicalPreferencesService;
import com.insta.hms.core.clinical.careteam.CareTeamService;
import com.insta.hms.core.patient.PatientDetailsHelper;
import com.insta.hms.core.wardassignment.UserWardAssignmentService;
import com.insta.hms.exception.EntityNotFoundException;
import com.insta.hms.mdm.centers.CenterService;
import com.insta.hms.mdm.departments.DepartmentService;
import com.insta.hms.mdm.doctors.DoctorService;
import com.insta.hms.mdm.savedsearches.SavedSearchService;
import com.insta.hms.mdm.ward.WardService;
import com.insta.hms.security.usermanager.UserService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * The Class InPatientSearchService.
 *
 * @author anup vishwas
 */

@Service
public class InPatientSearchService {

  /** The in patient search repository. */
  @LazyAutowired
  InPatientSearchRepository inPatientSearchRepository;

  /** The user service. */
  @LazyAutowired
  private UserService userService;

  /** The department service. */
  @LazyAutowired
  DepartmentService departmentService;

  /** The doctor service. */
  @LazyAutowired
  DoctorService doctorService;

  /** The ward service. */
  @LazyAutowired
  WardService wardService;

  /** The care team service. */
  @LazyAutowired
  CareTeamService careTeamService;

  /** The saved search service. */
  @LazyAutowired
  SavedSearchService savedSearchService;

  /** The user ward assignment service. */
  @LazyAutowired
  UserWardAssignmentService userWardAssignmentService;

  /** The center service. */
  @LazyAutowired
  CenterService centerService;

  /** The clinical preferences service. */
  @LazyAutowired
  ClinicalPreferencesService clinicalPreferencesService;

  /**
   * Gets the patients list.
   *
   * @param params
   *          the params
   * @return the patients list
   * @throws ParseException
   *           the parse exception
   * @throws UnsupportedEncodingException
   *           the unsupported encoding exception
   */
  public Map<String, Object> getPatientsList(Map<String, String[]> params) throws ParseException,
      UnsupportedEncodingException {
    String[] paramSearchId = (String[]) params.get("search_id");
    params = this.getParamMap(params);
    Map defaultFilterMap = getDefaultFilterData();
    List<BasicDynaBean> patientDetailsBeanList = null;
    List<BasicDynaBean> mrNoList = null;
    PagedList allPatientsList = new PagedList();
    List<PatientPojo> patientList = new ArrayList<PatientPojo>();

    // This part of code is used for the input text filters
    String[] paramFindString = (String[]) params.get("find_string");
    String findString = paramFindString == null || paramFindString[0] == null ? ""
        : paramFindString[0].trim();
    StringBuilder patientIds = new StringBuilder();
    if (!"".equals(findString)) {
      patientDetailsBeanList = inPatientSearchRepository.getInputFilterData(findString);
      for (int j = 0; j < patientDetailsBeanList.size(); j++) {
        String patId = (String) patientDetailsBeanList.get(j).get("patient_id");
        patientIds.append("'" + patId + "'");
        if (j != (patientDetailsBeanList.size() - 1)) {
          patientIds.append(",");
        }
      }
    }

    // This part is used to show the results from advance search/custom saved search in patient list
    // card
    Boolean isReqFromAdv = (params.get("is_request_from_advanced") != null);
    String searchId = paramSearchId == null || paramSearchId[0] == null ? "" : paramSearchId[0];
    String seachType = "";
    BasicDynaBean bean = null;
    if (!searchId.equals("")) {
      bean = savedSearchService.findByKey(Integer.parseInt(searchId));
    }
    if (bean != null) {
      seachType = (String) bean.get("search_type");
    }
    StringBuilder mrNos = new StringBuilder();
    if (isReqFromAdv || (!seachType.equals("") && (!seachType.equals("System")))) {
      mrNoList = inPatientSearchRepository.getMrNoList(params, defaultFilterMap);
      for (int k = 0; k < mrNoList.size(); k++) {
        String mrNo = (String) mrNoList.get(k).get("mr_no");
        mrNos.append("'" + mrNo + "'");
        if (k != (mrNoList.size() - 1)) {
          mrNos.append(",");
        }
      }
    }
    // Useful when there is no records for given input text search creteria
    if (!"".equals(findString) && patientDetailsBeanList.size() == 0) {
      allPatientsList.setPageNumber(0);
      allPatientsList.setPageSize(0);
      allPatientsList.setTotalRecords(0);
    } else {
      allPatientsList = inPatientSearchRepository.getInPatientsList(params, defaultFilterMap,
          patientIds.toString(), mrNos, seachType);
      if (allPatientsList != null) {
        for (int i = 0; i < allPatientsList.getDtoList().size(); i++) {
          Map<String, Object> map = (Map<String, Object>) allPatientsList.getDtoList().get(i);
          PatientPojo patient = new PatientPojo();
          String mrNo = (String) map.get("mr_no");
          boolean isDocOrNurseWithNotLatestVisit = false;
          patient.setMr_no(mrNo);
          patient.setFull_name((String) map.get("full_name"));
          patient.setPatient_name((String) map.get("patient_name"));
          patient.setMiddle_name((String) map.get("middle_name"));
          patient.setLast_name((String) map.get("last_name"));
          patient.setAge_text((String) map.get("age_text"));
          patient.setDob(DateUtil.formatDate((java.sql.Date) map.get("dateofbirth")));
          patient.setPatient_gender((String) map.get("patient_gender"));
          patient.setPatient_phone((String) map.get("patient_phone"));
          patient.setCountry_code((String) map.get("patient_phone_country_code"));
          patient.setAbbreviation((String) map.get("abbreviation"));

          VisitPojo visit = new VisitPojo();
          visit.setVisit_id((String) map.get("patient_id"));
          visit.setDischarge_status((String) map.get("patient_discharge_status"));
          // useful, if logged in as doc or nurse and its not assigned with latest visit
          if ((Boolean) defaultFilterMap.get("is_doctor_login")
              || (Boolean) defaultFilterMap.get("applyNurseRules")) {
            isDocOrNurseWithNotLatestVisit = inPatientSearchRepository
                .multiVisitWithNotLatestDocOrNurse(mrNo, defaultFilterMap);
          }
          if (!isDocOrNurseWithNotLatestVisit) {
            visit.setBed_name((String) map.get("bed_name"));
            visit.setWard_name((String) map.get("ward_name"));
          } else {
            visit.setBed_name(null);
            visit.setWard_name(null);
          }
          patient.getVisits().add(visit);
          patientList.add(patient);

        }
      }
    }
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("patients", patientList);
    map.put("page_size", allPatientsList.getPageSize());
    map.put("page_num", allPatientsList.getPageNumber());
    map.put("total_records", allPatientsList.getTotalRecords());
    HttpServletRequest request = RequestContext.getHttpRequest();
    HttpSession session = (HttpSession) request.getSession(false);
    Integer centerId = (Integer) session.getAttribute("centerId");
    map.put("country_code", centerService.getCountryCode(centerId));

    return map;
  }

  /**
   * Gets the filter data.
   *
   * @return the filter data
   */
  public Map<String, Object> getFilterData() {
    Map<String, Object> map = new LinkedHashMap<String, Object>();
    Map defaultFilterMap = getDefaultFilterData();
    PatientDetailsHelper.getCommonFilterData(map);
    String doctorId = (String) defaultFilterMap.get("doctor_id");
    boolean isDoctorLogin = (Boolean) defaultFilterMap.get("is_doctor_login");
    boolean applyNurseRules = (Boolean) defaultFilterMap.get("applyNurseRules");
    int centerId = (Integer) defaultFilterMap.get("center_id");
    String userName = (String) defaultFilterMap.get("user_name");
    map.put("departments", ConversionUtils.copyListDynaBeansToMap(departmentService.lookup(true)));
    if (applyNurseRules) {
      map.put("wards", ConversionUtils.copyListDynaBeansToMap(userWardAssignmentService
          .getUserWardDetails(userName, centerId)));
    } else {
      map.put("wards", ConversionUtils.copyListDynaBeansToMap(wardService.lookup(true)));
    }
    if (isDoctorLogin) {
      map.put("doctors",
          ConversionUtils.copyListDynaBeansToMap(careTeamService.getAllVisitDetails(doctorId)));
    } else {
      map.put("doctors", ConversionUtils.copyListDynaBeansToMap(doctorService.lookup(true)));
    }
    Map<String, String> dischargeStatus = new LinkedHashMap<String, String>();
    dischargeStatus.put("N", "Discharge Pending");
    dischargeStatus.put("C", "Clinical Discharge Pending");
    dischargeStatus.put("F", "Financial Discharge Pending");
    dischargeStatus.put("P", "Physical Discharge Pending");
    dischargeStatus.put("D", "Discharge Completed");

    map.put("discharge_status", dischargeStatus);
    map.put("is_doctor_login", isDoctorLogin);

    return map;
  }

  /**
   * Gets the patients advanced.
   *
   * @param params
   *          the params
   * @return the patients advanced
   * @throws ParseException
   *           the parse exception
   * @throws UnsupportedEncodingException
   *           the unsupported encoding exception
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> getPatientsAdvanced(Map<String, String[]> params)
      throws ParseException, UnsupportedEncodingException {

    params = this.getParamMap(params);
    Map defaultFilterMap = getDefaultFilterData();
    PagedList patientsList = inPatientSearchRepository.getPatientListForAdvSearch(params,
        defaultFilterMap);
    List<String> mrNoList = getMrNoList(patientsList);
    List<BasicDynaBean> patientDetailsList = inPatientSearchRepository.getPatientDetailsList(
        mrNoList, params, defaultFilterMap);
    Map<String, List<BasicDynaBean>> visitDetailMap = new HashMap<String, List<BasicDynaBean>>();
    visitDetailMap = ConversionUtils.listBeanToMapListBean(patientDetailsList, "mr_no");
    LinkedList<PatientPojo> patientList = new LinkedList<PatientPojo>();
    Map<String, Object> mrnoMap = new HashMap<String, Object>();

    if (patientsList != null) {
      for (int i = 0; i < patientsList.getDtoList().size(); i++) {
        Map<String, Object> map = (Map<String, Object>) patientsList.getDtoList().get(i);
        PatientPojo patient = new PatientPojo();
        String mrNo = (String) map.get("mr_no");
        for (String key : visitDetailMap.keySet()) {
          if (key.equals(mrNo)) {
            List<BasicDynaBean> list = visitDetailMap.get(mrNo);
            for (int j = 0; j < list.size(); j++) {
              BasicDynaBean bean = list.get(j);
              String mrNoExist = (String) mrnoMap.get(mrNo);
              if (mrNoExist == null) {
                patient.setMr_no(mrNo);
                patient.setFull_name((String) bean.get("full_name"));
                patient.setAge_text((String) bean.get("age_text"));
                patient.setDob(DateUtil.formatDate((java.sql.Date) bean.get("dateofbirth")));
                patient.setPatient_gender((String) bean.get("patient_gender"));
                patient.setPatient_phone((String) bean.get("patient_phone"));
                patient.setCountry_code((String) bean.get("patient_phone_country_code"));
                mrnoMap.put("mrNo", mrNo);
              }

              VisitPojo visit = new VisitPojo();
              visit.setVisit_id((String) bean.get("patient_id"));
              visit.setDischarge_status((String) bean.get("patient_discharge_status"));
              visit.setBed_name((String) bean.get("bed_name"));
              visit.setWard_name((String) bean.get("ward_name"));
              visit.setDoctor_name((String) bean.get("doctor_name"));
              visit.setVisit_date(DateUtil.formatDate((java.sql.Date) bean.get("visit_date")));
              visit.setVisit_status((String) bean.get("visit_status"));
              visit.setDep_name((String) bean.get("dept_name"));

              BillPojo bill = new BillPojo();
              bill.setVisit_id((String) bean.get("patient_id"));
              bill.setBill_no((String) bean.get("bill_no"));
              bill.setBill_status((String) bean.get("bill_status"));
              bill.setPayment_status((String) bean.get("payment_status"));

              if (!patient.getVisits().contains(visit)) {
                patient.getVisits().add(visit);
              }
              if (!patient.getBills().contains(bill)) {
                patient.getBills().add(bill);
              }
            }
            patientList.add(patient);
          }
        }
      }
    }
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("patients", patientList);
    map.put("page_size", patientsList.getPageSize());
    map.put("page_num", patientsList.getPageNumber());
    map.put("total_records", patientsList.getTotalRecords());

    return map;
  }

  /**
   * Gets the mr no list.
   *
   * @param patientsList
   *          the patients list
   * @return the mr no list
   */
  public List<String> getMrNoList(PagedList patientsList) {
    List<String> mrNoList = new ArrayList<String>();
    if (patientsList != null) {
      for (int i = 0; i < patientsList.getDtoList().size(); i++) {
        Map<String, Object> map = (Map<String, Object>) patientsList.getDtoList().get(i);
        String mrNo = (String) map.get("mr_no");
        if (mrNo != null && !mrNo.equals("")) {
          mrNoList.add(mrNo);
        }
      }
    }
    return mrNoList;
  }

  /**
   * Gets the default filter data.
   *
   * @return the default filter data
   */
  public Map<String, Object> getDefaultFilterData() {
    Map<String, Object> map = new HashMap<String, Object>();
    HttpServletRequest request = RequestContext.getHttpRequest();
    HttpSession session = (HttpSession) request.getSession(false);
    Integer centerId = (Integer) session.getAttribute("centerId");
    BasicDynaBean clinicalPreferenceBean = clinicalPreferencesService.getClinicalPreferences();

    BasicDynaBean userBean = userService.getLoggedUser();
    String doctorId = (String) userBean.get("doctor_id");
    boolean isDoctorLogin = false;
    boolean applyNurseRules = false;
    if (clinicalPreferenceBean.get("ip_cases_across_doctors").equals("N") && doctorId != null
        && !doctorId.equals("")) {
      // if the doctor logged into the application, show patients of that doctor in IP list.
      // params.put("doctor_id", new String[]{doctorId+""});
      isDoctorLogin = true;
    }
    if (!isDoctorLogin) {
      applyNurseRules = clinicalPreferenceBean.get("nurse_staff_ward_assignments_applicable")
          .equals("Y");
    }
    map.put("center_id", centerId);
    map.put("doctor_id", doctorId);
    map.put("is_doctor_login", isDoctorLogin);
    int roleId = (Integer) request.getSession().getAttribute("roleId");
    map.put("applyNurseRules", applyNurseRules && roleId > 2);
    String loggedUserName = (String) userBean.get("emp_username");
    map.put("user_name", loggedUserName);
    return map;
  }

  /**
   * Gets the param map.
   *
   * @param map
   *          the map
   * @return the param map
   * @throws UnsupportedEncodingException
   *           the unsupported encoding exception
   */
  public Map<String, String[]> getParamMap(Map<String, String[]> map)
      throws UnsupportedEncodingException {
    Map<String, String[]> params = new HashMap<String, String[]>(map);
    String[] searchId = (String[]) map.get("search_id");
    String[] findString = (String[]) map.get("find_string");
    String[] defaultSearch = (String[]) map.get("use_default_search");

    if (defaultSearch != null && defaultSearch[0] != null && new Boolean(defaultSearch[0])) {
      params = savedSearchService.getDefaultSearch("IP Flow");
      if (params == null) {
        throw new EntityNotFoundException(new String[] { "exception.entity.not.found", "id",
            "default search" });
      }
      PatientDetailsHelper.copyListingParams(map, params);
    } else if (findString != null) {
      String doctor = (String) userService.getLoggedUser().get("doctor_id");
      params.put("doctor", new String[] { doctor == null ? "" : doctor });
    } else if (searchId != null && searchId[0] != null && !searchId[0].equals("")) {
      params = this.splitQuery(Integer.parseInt(searchId[0]));
      params = savedSearchService.splitQuery(Integer.parseInt(searchId[0]));

      PatientDetailsHelper.copyListingParams(map, params);
      if (params == null) {
        throw new EntityNotFoundException(new String[] { "exception.entity.not.found", "search_id",
            searchId[0] });
      }
    }

    return params;
  }

  /**
   * Split query.
   *
   * @param searchId
   *          the search id
   * @return the map
   * @throws UnsupportedEncodingException
   *           the unsupported encoding exception
   */
  public Map<String, String[]> splitQuery(int searchId) throws UnsupportedEncodingException {
    return savedSearchService.splitQuery(searchId);
  }

}
