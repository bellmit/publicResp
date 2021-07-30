/**
 * 
 */

package com.insta.hms.core.patient.inpatientlist;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.DateHelper;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryAssembler;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.core.patient.PatientDetailsHelper;

import org.apache.commons.beanutils.BasicDynaBean;
import org.joda.time.DateTime;
import org.springframework.stereotype.Repository;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * The Class InPatientSearchRepository.
 *
 * @author anup vishwas
 */

@Repository
public class InPatientSearchRepository {

  /** The select field. */
  public static String SELECT_FIELD = " SELECT *";

  /** The select count. */
  public static String SELECT_COUNT = " SELECT COUNT(DISTINCT(mr_no))";

  /** The from table. */
  public static String FROM_TABLE = 
      " FROM (SELECT pr.mr_no, pr.center_id,"
      + " get_patient_full_name(sm.salutation, pd.patient_name,"
      + " pd.middle_name, pd.last_name) as full_name,"
      + " pd.patient_name, pd.middle_name, pd.last_name,"
      + " pd.patient_gender, pd.dateofbirth, pd.patient_phone, pd.patient_phone_country_code,"
      + " get_age_text(COALESCE(pd.dateofbirth, pd.expected_dob)) AS age_text, pd.oldmrno,"
      + " pd.government_identifier, cgm.abbreviation,"
      + " pr.patient_id, pr.patient_discharge_status,"
      + " pr.reg_date as visit_date, pr.status as visit_status,"
      + " bn.bed_name, wn.ward_no, wn.ward_name, pr.doctor_id "
      + " FROM patient_details pd "
      + " JOIN (SELECT pr.patient_id, pr.mr_no,"
      + " visit_type, pr.status, doctor, patient_discharge_status, pr.ward_id,"
      + " dept_name as dept_id, reg_date, center_id, #DOCTORID_SELECT# , pr.discharge_flag, "
      + " rank() over(partition by pr.mr_no ORDER BY pr.status, pr.reg_date DESC, pr.reg_time DESC)"
      + " FROM patient_details pd"
      + " JOIN patient_registration pr ON(pr.mr_no = pd.mr_no AND"
      + " ( patient_confidentiality_check(pd.patient_group,pd.mr_no) )) "
      + " @ ) AS pr ON (pr.rank = 1 AND pr.mr_no = pd.mr_no)"
      + " JOIN salutation_master sm ON (sm.salutation_id = pd.salutation)"
      + " JOIN confidentiality_grp_master cgm ON (cgm.confidentiality_grp_id = pd.patient_group)"
      + " LEFT JOIN admission adm ON (adm.patient_id = pr.patient_id)"
      + " LEFT JOIN bed_names bn ON (bn.bed_id = adm.bed_id)"
      + " LEFT JOIN ward_names wn ON (wn.ward_no = bn.ward_no)";

  /**
   * Gets the in patients list.
   *
   * @param params
   *          the params
   * @param defaultFilterMap
   *          the default filter map
   * @param patientIds
   *          the patient ids
   * @param mrNos
   *          the mr nos
   * @param seachType
   *          the seach type
   * @return the in patients list
   * @throws ParseException
   *           the parse exception
   */
  public PagedList getInPatientsList(Map<String, String[]> params,
      Map<String, Object> defaultFilterMap, String patientIds, 
      StringBuilder mrNos, String seachType)
      throws ParseException {

    StringBuilder fromTableReplaced = null;
    String[] paramFindString = (String[]) params.get("find_string");
    Boolean isReqFromAdv = (params.get("is_request_from_advanced") != null);
    String findString = paramFindString == null || paramFindString[0] == null ? ""
        : paramFindString[0];
    boolean isDoctorLogin = (Boolean) defaultFilterMap.get("is_doctor_login");
    String doctorId = (String) defaultFilterMap.get("doctor_id");
    int centerId = (Integer) defaultFilterMap.get("center_id");
    String loggedUserName = (String) defaultFilterMap.get("user_name");
    String replaceQuery = "";
    String conditionBasedSelect = " pr.doctor as doctor_id ";
    if (!"".equals(findString)) {
      // useful when we search with input text field
      if (isDoctorLogin) { 
        // useful for multi visit, and loggedin doctor is not assign with all
        // visit
        replaceQuery = " LEFT JOIN visit_care_team vct ON (vct.patient_id = pr.patient_id) "
            + " WHERE pr.patient_id IN (" + patientIds
            + ") AND COALESCE(vct.care_doctor_id, pr.doctor) = " + "'" + doctorId + "'";
        conditionBasedSelect = " COALESCE(vct.care_doctor_id, pr.doctor) as doctor_id ";
      } else {
        replaceQuery = " WHERE pr.patient_id IN (" + patientIds + ")";
        fromTableReplaced = new StringBuilder(FROM_TABLE.replace("@", " WHERE pr.patient_id IN ("
            + patientIds + ")"));
      }
    } else if (isReqFromAdv || (!seachType.equals("") && (!seachType.equals("System")))) {
      // useful when we show the patient in the patient list after searching from advance search
      if (isDoctorLogin) { 
        // useful for multi visit, and loggedin doctor is not assign with all
        // visit
        replaceQuery = " JOIN visit_care_team vct ON (vct.patient_id = pr.patient_id "
            + " AND care_doctor_id = " + "'" + doctorId + "'" + ") ";
      }
      if (mrNos != null && mrNos.length() > 0) {
        replaceQuery = replaceQuery + " WHERE pr.mr_no IN (" + mrNos + ")";
      }
    } else {
      if (isDoctorLogin) { 
        // useful when we land from hamburger and logged in as docotr/care team
        // doctor
        replaceQuery = 
            " LEFT JOIN visit_care_team vct ON (vct.patient_id = pr.patient_id) "
            + " WHERE pr.visit_type = 'i' and pr.status='A'"
            + " AND COALESCE(vct.care_doctor_id, pr.doctor) = "
            + "'" + doctorId + "'";
        conditionBasedSelect = " COALESCE(vct.care_doctor_id, pr.doctor) as doctor_id ";
      } else { // useful when we land from hamburger and not logged in as doctor/care team doctor
        replaceQuery = " WHERE pr.visit_type = 'i' and pr.status='A'";
      }
    }
    if (centerId != 0) {
      replaceQuery = replaceQuery + " AND pr.center_id = " + centerId;
    }
    fromTableReplaced = new StringBuilder(FROM_TABLE.replace("@", replaceQuery).replace(
        "#DOCTORID_SELECT#", conditionBasedSelect));

    Map<LISTING, Object> listingParams = ConversionUtils.getListingParameter(params);
    int pageSize = (Integer) listingParams.get(LISTING.PAGESIZE);
    int pageNum = (Integer) listingParams.get(LISTING.PAGENUM);

    boolean applyNurseRules = (Boolean) defaultFilterMap.get("applyNurseRules");
    if (applyNurseRules) {
      String nurseFilterJoin = " JOIN LATERAL (SELECT (CASE "
          + " WHEN (pr.discharge_flag = 'D') THEN (SELECT 1 from admission adm1 "
          + " JOIN bed_names bn ON (bn.bed_id = adm1.bed_id) "
          + " JOIN nurse_ward_assignments nwa ON (bn.ward_no = nwa.ward_id and nwa.emp_username='"
          + loggedUserName + "') WHERE (adm1.patient_id = pr.patient_id)) "
          + " WHEN (bn.ward_no is not null) "
          + " THEN (SELECT count(1) FROM ip_bed_details ipd "
          + " JOIN bed_names bn ON (bn.bed_id = ipd.bed_id) "
          + " JOIN nurse_ward_assignments nwa ON(bn.ward_no = nwa.ward_id and nwa.emp_username='"
          + loggedUserName + "') "
          + " where ipd.patient_id=pr.patient_id and ipd.status in ('A','C','R')) ELSE ( "
          + " select 1 from nurse_ward_assignments WHERE pr.ward_id = ward_id and "
          + " emp_username='" + loggedUserName + "') END) as nurse) as foobar ON "
          + " (foobar.nurse != 0) ) as foo";
      fromTableReplaced.append(nurseFilterJoin);
    } else {
      fromTableReplaced.append(") as foo");
    }

    SearchQueryAssembler sqa = new SearchQueryAssembler(SELECT_FIELD, SELECT_COUNT,
        fromTableReplaced.toString(), null, "mr_no", false, pageSize, pageNum);

    if (isDoctorLogin) {
      sqa.addFilter(SearchQueryBuilder.STRING, "doctor_id", "=", doctorId);
    }
    // applyFilters(sqa, params, false);

    sqa.build();
    PagedList patientList = sqa.getMappedPagedList();
    return patientList;

  }

  /** The Constant GET_PATIENT_DETAILS_SELECT. */
  private static final String GET_PATIENT_DETAILS_SELECT = 
      " SELECT pr.mr_no, pr.patient_id, pr.reg_date as visit_date,"
      + " get_patient_full_name(sm.salutation, pd.patient_name,"
      + " pd.middle_name, pd.last_name) as full_name,"
      + " pd.patient_gender, pd.dateofbirth, pd.patient_phone, pd.patient_phone_country_code,"
      + " get_age_text(COALESCE(pd.dateofbirth, pd.expected_dob)) AS age_text,"
      + " bn.bed_name, wn.ward_no, wn.ward_name,"
      + " pr.patient_discharge_status, pr.status as visit_status,"
      + " b.bill_no, b.status as bill_status, b.payment_status, d.doctor_id, d.doctor_name,"
      + " dep.dept_id, dep.dept_name";

  /** The Constant GET_PATIENT_DETAILS_FROM. */
  private static final String GET_PATIENT_DETAILS_FROM = " FROM patient_registration pr"
      + " JOIN patient_details pd ON (pd.mr_no"
      + " = pr.mr_no AND"
      + " (patient_confidentiality_check(pd.patient_group,pd.mr_no)))"
      + " JOIN salutation_master sm ON (sm.salutation_id = pd.salutation)"
      + " LEFT JOIN admission adm ON (adm.patient_id = pr.patient_id)"
      + " LEFT JOIN bed_names bn ON (bn.bed_id = adm.bed_id)"
      + " LEFT JOIN ward_names wn ON (wn.ward_no = bn.ward_no)"
      + " LEFT JOIN doctors d ON (d.doctor_id = pr.doctor)"
      + " LEFT JOIN department dep ON (dep.dept_id = pr.dept_name)"
      + " LEFT JOIN bill b ON (b.visit_id = pr.patient_id"
      + " AND b.status!='X' AND b.restriction_type!='P')"
      + " LEFT JOIN visit_care_team vct ON (vct.patient_id = pr.patient_id)";

  /**
   * Gets the patient details list.
   *
   * @param mrNoList
   *          the mr no list
   * @param params
   *          the params
   * @param defaultFilterMap
   *          the default filter map
   * @return the patient details list
   */
  // will give all records including no. of visits and bills, useful for advance search
  protected List<BasicDynaBean> getPatientDetailsList(List<String> mrNoList,
      Map<String, String[]> params, Map<String, Object> defaultFilterMap) {
    if (mrNoList == null || mrNoList.isEmpty()) {
      return Collections.EMPTY_LIST;
    }
    String doctorId = (String) defaultFilterMap.get("doctor_id");
    String loggedUserName = (String) defaultFilterMap.get("user_name");
    String fromQuery = GET_PATIENT_DETAILS_FROM;
    boolean applyNurseRules = (Boolean) defaultFilterMap.get("applyNurseRules");
    if (applyNurseRules) {
      String nurseFilterJoin = " JOIN LATERAL (SELECT (CASE "
          + " WHEN (pr.discharge_flag = 'D') THEN (SELECT 1 from admission adm1 "
          + " JOIN bed_names bn ON (bn.bed_id = adm1.bed_id) "
          + " JOIN nurse_ward_assignments nwa ON (bn.ward_no = nwa.ward_id and nwa.emp_username='"
          + loggedUserName + "') "
          + " WHERE (adm1.patient_id = pr.patient_id)) "
          + " WHEN (bn.ward_no is not null) "
          + " THEN (SELECT  count(1) FROM ip_bed_details ipd "
          + " JOIN bed_names bn ON (bn.bed_id = ipd.bed_id) "
          + " JOIN nurse_ward_assignments nwa ON (bn.ward_no = nwa.ward_id and nwa.emp_username='"
          + loggedUserName + "') "
          + " where ipd.patient_id=pr.patient_id and ipd.status in ('A','C','R')) ELSE ( "
          + " select 1 from nurse_ward_assignments WHERE pr.ward_id = ward_id and "
          + " emp_username='" + loggedUserName + "') END) as nurse) as foobar ON "
          + " (foobar.nurse != 0) ";
      fromQuery = fromQuery.concat(nurseFilterJoin);
    }
    SearchQueryAssembler qb = new SearchQueryAssembler(GET_PATIENT_DETAILS_SELECT, null,
        fromQuery, null, "pr.reg_date", false, 0, 0);
    qb.addSecondarySort("pr.reg_time", false);

    applyFilters(qb, params, true);

    boolean isDoctorLogin = (Boolean) defaultFilterMap.get("is_doctor_login");
    if (isDoctorLogin) {
      qb.addFilter(SearchQueryBuilder.STRING, "d.doctor_id", "=", doctorId);
    }

    qb.addFilter(SearchQueryBuilder.STRING, "pr.mr_no", "in", mrNoList);
    qb.build();

    Collection<Object> parameterValues = new ArrayList();
    parameterValues.addAll(qb.getfieldValues());
    String dataQuery = qb.getDataQueryString();
    List<BasicDynaBean> patientDetailsList = DatabaseHelper.queryToDynaList(dataQuery,
        parameterValues.toArray());

    return patientDetailsList;
  }

  /**
   * Apply filters.
   *
   * @param qb
   *          the qb
   * @param params
   *          the params
   * @param withAlias
   *          the with alias
   */
  private void applyFilters(SearchQueryAssembler qb, 
      Map<String, String[]> params, boolean withAlias) {

    StringBuilder visitDateFilter = new StringBuilder();
    Date fromVisitDate = null;
    Date toVisitDate = null;
    boolean isVisitDateExists = false;

    // category 1 filters
    String[] visitDate = (String[]) params.get("visit_date");
    if (visitDate != null) {
      String visitDateVar = null;
      if (withAlias) {
        visitDateVar = " pr.reg_date";
      } else {
        visitDateVar = " visit_date";
      }
      if (visitDate[0] != null && visitDate[0].equals("week")) {
        DateTime[] dateRange = DateHelper.getPastDateRange(7);
        fromVisitDate = dateRange[0].toDate();
        toVisitDate = dateRange[1].toDate();
        qb.addInitValue(SearchQueryBuilder.DATE, fromVisitDate);
        qb.addInitValue(SearchQueryBuilder.DATE, toVisitDate);
        visitDateFilter.append(visitDateVar + " >= ? AND " + visitDateVar + " <= ?");
        isVisitDateExists = true;
      } else {

        if (visitDate[0] != null && !visitDate[0].equals("")) {
          fromVisitDate = DateHelper.parseDate(visitDate[0]).toDate();
          qb.addInitValue(SearchQueryBuilder.DATE, fromVisitDate);
          visitDateFilter.append(visitDateVar + " >= ?");
          isVisitDateExists = true;
        }
        if (visitDate.length > 1 && visitDate[1] != null && !visitDate[1].equals("")) {
          toVisitDate = DateHelper.parseDate(visitDate[1]).toDate();
          qb.addInitValue(SearchQueryBuilder.DATE, toVisitDate);
          if (isVisitDateExists) {
            visitDateFilter.append(" AND ");
          }
          visitDateFilter.append(visitDateVar + " <= ?");
          isVisitDateExists = true;
        }
      }
    }

    StringBuilder dateFilters = null;
    if (isVisitDateExists) {
      dateFilters = new StringBuilder("(");
      dateFilters.append("(").append(visitDateFilter.toString()).append(")");
      dateFilters.append(")");
    }
    if (dateFilters != null) {
      qb.appendToQuery(dateFilters.toString());
    }
    // category 2 filters
    String[] dischargeStatus = params.get("patient_discharge_status");
    if (!isEmptyorAll(dischargeStatus)) {
      String dischargeStatusVar = null;
      if (withAlias) {
        dischargeStatusVar = " pr.patient_discharge_status";
      } else {
        dischargeStatusVar = " patient_discharge_status";
      }
      qb.addFilter(SearchQueryAssembler.STRING, dischargeStatusVar, "in",
          Arrays.asList(dischargeStatus));
    }
    String[] billStatus = (String[]) params.get("bill_status");
    if (!isEmptyorAll(billStatus)) {
      String billStatusVar = null;
      if (withAlias) {
        billStatusVar = " b.status";
      } else {
        billStatusVar = " bill_status";
      }
      qb.addFilter(SearchQueryAssembler.STRING, billStatusVar, "in", Arrays.asList(billStatus));
    }
    String[] visitStatus = (String[]) params.get("visit_status");
    if (!isEmptyorAll(visitStatus)) {
      String visitStatusVar = null;
      if (withAlias) {
        visitStatusVar = " pr.status";
      } else {
        visitStatusVar = " visit_status";
      }
      qb.addFilter(SearchQueryAssembler.STRING, visitStatusVar, "in", Arrays.asList(visitStatus));
    }
    // category 3 filters
    String[] depts = (String[]) params.get("department");
    if (!isEmptyorAll(depts)) {
      String deptVar = null;
      if (withAlias) {
        deptVar = " dep.dept_id";
      } else {
        deptVar = " dept_id";
      }
      qb.addFilter(SearchQueryAssembler.STRING, deptVar, "in", Arrays.asList(depts));
    }
    if (selectAllInList(depts)) {
      qb.addFilter("dep.dept_id", "IS NOT NULL");
    }
    String[] doctors = (String[]) params.get("doctor");
    if (!isEmptyorAll(doctors)) {
      String doctorVar = null;
      if (withAlias) {
        doctorVar = " d.doctor_id";
      } else {
        doctorVar = " doctor_id";
      }
      qb.addFilter(SearchQueryAssembler.STRING, doctorVar, "in", Arrays.asList(doctors));
    }
    if (selectAllInList(doctors)) {
      qb.addFilter("d.doctor_id", "IS NOT NULL");
    }
    String[] wards = (String[]) params.get("ward");
    if (!isEmptyorAll(wards)) {
      String wardVar = null;
      if (withAlias) {
        wardVar = " wn.ward_no";
      } else {
        wardVar = " ward_no";
      }
      qb.addFilter(SearchQueryAssembler.STRING, wardVar, "in", Arrays.asList(wards));
    }
    if (selectAllInList(wards)) {
      qb.addFilter("wn.ward_no", "IS NOT NULL");
    }
  }

  /**
   * Gets the patient list for adv search.
   *
   * @param params
   *          the params
   * @param defaultFilterMap
   *          the default filter map
   * @return the patient list for adv search
   * @throws ParseException
   *           the parse exception
   */
  // Will give list of mr no. Can be used from advanced search for paginations
  public PagedList getPatientListForAdvSearch(Map<String, String[]> params,
      Map<String, Object> defaultFilterMap) throws ParseException {

    boolean isDoctorLogin = (Boolean) defaultFilterMap.get("is_doctor_login");
    String doctorId = (String) defaultFilterMap.get("doctor_id");
    boolean applyNurseRules = (Boolean) defaultFilterMap.get("applyNurseRules");
    String loggedUserName = (String) defaultFilterMap.get("user_name");
    Map<LISTING, Object> listingParams = ConversionUtils.getListingParameter(params);
    int pageSize = (Integer) listingParams.get(LISTING.PAGESIZE);
    int pageNum = (Integer) listingParams.get(LISTING.PAGENUM);

    String fromQuery = GET_PATIENT_DETAILS_FROM;
    if (applyNurseRules) {
      String nurseFilterJoin = " JOIN LATERAL (SELECT (CASE "
          + " WHEN (pr.discharge_flag = 'D') THEN (SELECT 1 from admission adm1 "
          + " JOIN bed_names bn ON (bn.bed_id = adm1.bed_id) "
          + " JOIN nurse_ward_assignments nwa ON (bn.ward_no = nwa.ward_id and nwa.emp_username='"
          + loggedUserName + "') "
          + " WHERE (adm1.patient_id = pr.patient_id)) "
          + " WHEN (bn.ward_no is not null) "
          + " THEN (SELECT count(1) FROM ip_bed_details ipd "
          + " JOIN bed_names bn ON (bn.bed_id = ipd.bed_id) "
          + " JOIN nurse_ward_assignments nwa ON(bn.ward_no = nwa.ward_id and nwa.emp_username='"
          + loggedUserName + "') "
          + " where ipd.patient_id=pr.patient_id and ipd.status in ('A','C','R')) ELSE ( "
          + " select 1 from nurse_ward_assignments WHERE pr.ward_id = ward_id and "
          + " emp_username='" + loggedUserName + "') END) as nurse) as foobar ON "
          + " (foobar.nurse != 0) ";
      fromQuery = fromQuery.concat(nurseFilterJoin);
    }

    SearchQueryAssembler sqa = new SearchQueryAssembler("SELECT pr.mr_no ",
        "SELECT COUNT(DISTINCT(pr.mr_no))", fromQuery, null, "pr.mr_no", "pr.mr_no",
        false, pageSize, pageNum);
    applyFilters(sqa, params, true);
    if (isDoctorLogin) {
      sqa.addFilter(SearchQueryBuilder.STRING, "d.doctor_id", "=", doctorId);
    }
    int centerId = (Integer) defaultFilterMap.get("center_id");
    if (centerId != 0) {
      sqa.addFilter(SearchQueryBuilder.INTEGER, "pr.center_id", "=", centerId);
    }
    sqa.build();
    PagedList mrNoList = sqa.getMappedPagedList();

    return mrNoList;
  }

  /** The Constant SELECT_INPUT_FILTER_DATA_FIELD. */
  private static final String SELECT_INPUT_FILTER_DATA_FIELD = " SELECT pr.patient_id";

  /** The Constant FROM_INPUT_FILTER_DATA. */
  private static final String FROM_INPUT_FILTER_DATA = " FROM patient_details pd"
      + " JOIN patient_registration pr ON (pd.mr_no = pr.mr_no)";

  /**
   * Gets the input filter data.
   *
   * @param findString
   *          the find string
   * @return the input filter data
   */
  public List<BasicDynaBean> getInputFilterData(String findString) {
    StringBuilder filterOn = new StringBuilder();
    SearchQueryAssembler sqa = new SearchQueryAssembler(SELECT_INPUT_FILTER_DATA_FIELD, null,
        FROM_INPUT_FILTER_DATA);
    PatientDetailsHelper.appendCommonFilters(filterOn, findString, sqa);
    sqa.build();
    Collection<Object> parameterValues = new ArrayList();
    parameterValues.addAll(sqa.getfieldValues());
    String dataQuery = sqa.getDataQueryString();

    return DatabaseHelper.queryToDynaList(dataQuery, parameterValues.toArray());
  }

  /**
   * Gets the mr no list.
   *
   * @param params
   *          the params
   * @param defaultFilterMap
   *          the default filter map
   * @return the mr no list
   */
  // get all mr no list based on applied filter creteria
  public List<BasicDynaBean> getMrNoList(Map<String, String[]> params,
      Map<String, Object> defaultFilterMap) {
    boolean isDoctorLogin = (Boolean) defaultFilterMap.get("is_doctor_login");
    String doctorId = (String) defaultFilterMap.get("doctor_id");
    boolean applyNurseRules = (Boolean) defaultFilterMap.get("applyNurseRules");
    String loggedUserName = (String) defaultFilterMap.get("user_name");
    String fromQuery = GET_PATIENT_DETAILS_FROM;
    if (applyNurseRules) {
      String nurseFilterJoin = " JOIN LATERAL (SELECT (CASE "
          + " WHEN (pr.discharge_flag = 'D') THEN (SELECT 1 from admission adm1 "
          + " JOIN bed_names bn ON (bn.bed_id = adm1.bed_id) "
          + " JOIN nurse_ward_assignments nwa ON (bn.ward_no = nwa.ward_id and nwa.emp_username='"
          + loggedUserName + "') "
          + " WHERE (adm1.patient_id = pr.patient_id)) "
          + " WHEN (bn.ward_no is not null) "
          + " THEN (SELECT count(1) FROM ip_bed_details ipd "
          + " JOIN bed_names bn ON (bn.bed_id = ipd.bed_id) "
          + " JOIN nurse_ward_assignments nwa ON(bn.ward_no = nwa.ward_id and nwa.emp_username='"
          + loggedUserName + "') "
          + " where ipd.patient_id=pr.patient_id and ipd.status in ('A','C','R')) ELSE ( "
          + " select 1 from nurse_ward_assignments WHERE pr.ward_id = ward_id and "
          + " emp_username='" + loggedUserName + "') END) as nurse) as foobar ON "
          + " (foobar.nurse != 0 ) ";
      fromQuery = fromQuery.concat(nurseFilterJoin);
    }
    
    SearchQueryAssembler sqa = new SearchQueryAssembler("SELECT pr.mr_no ",
        "SELECT COUNT(DISTINCT(pr.mr_no))", fromQuery, null, "pr.mr_no", null,
        false, 0, 0);
    applyFilters(sqa, params, true);
    if (isDoctorLogin) {
      sqa.addFilter(SearchQueryBuilder.STRING, "d.doctor_id", "=", doctorId);
    }
    sqa.build();
    Collection<Object> parameterValues = new ArrayList();
    parameterValues.addAll(sqa.getfieldValues());
    String dataQuery = sqa.getDataQueryString();

    return DatabaseHelper.queryToDynaList(dataQuery, parameterValues.toArray());
  }

  /** The Constant IS_DOCORNURSE_FROM_LATEST_VISIT. */
  private static final String IS_DOCORNURSE_FROM_LATEST_VISIT = " SELECT pr.mr_no"
      + " FROM patient_details pd " + " JOIN (SELECT pr.patient_id, mr_no, ward_id,"
      + " rank() over(partition by mr_no ORDER BY pr.status, pr.reg_date, pr.reg_time DESC)"
      + " FROM patient_details pd JOIN patient_registration pr USING(mr_no)"
      + " WHERE pr.mr_no = ?) AS pr ON (pr.rank = 1 AND pr.mr_no = pd.mr_no)"
      + " LEFT JOIN admission adm ON (adm.patient_id = pr.patient_id)"
      + " LEFT JOIN bed_names bn ON (bn.bed_id = adm.bed_id)"
      + " LEFT JOIN ward_names wn ON (wn.ward_no = bn.ward_no)"
      + " LEFT JOIN visit_care_team vct ON (vct.patient_id = pr.patient_id)"
      + " LEFT JOIN nurse_ward_assignments nwa ON(COALESCE(bn.ward_no,pr.ward_id) = nwa.ward_id)";

  // for multivisit,if logged in Doc or Nurse is not assigned with latest visit, return true else
  /**
   * Multi visit with not latest doc or nurse.
   *
   * @param mrNo
   *          the mr no
   * @param defaultFilterMap
   *          the default filter map
   * @return true, if successful
   */
  // false
  public boolean multiVisitWithNotLatestDocOrNurse(
      String mrNo, Map<String, Object> defaultFilterMap) {
    boolean isDoctorLogin = (Boolean) defaultFilterMap.get("is_doctor_login");
    String doctorId = (String) defaultFilterMap.get("doctor_id");
    boolean applyNurseRules = (Boolean) defaultFilterMap.get("applyNurseRules");
    String loggedUserName = (String) defaultFilterMap.get("user_name");
    String filterParam = null;
    StringBuilder sb = new StringBuilder(IS_DOCORNURSE_FROM_LATEST_VISIT);
    if (isDoctorLogin) {
      sb.append(" WHERE vct.care_doctor_id = ?");
      filterParam = doctorId;
    } else if (applyNurseRules) {
      sb.append(" WHERE nwa.emp_username = ?");
      filterParam = loggedUserName;
    }
    return DatabaseHelper.queryToDynaBean(sb.toString(), mrNo, filterParam) == null;
  }

  /**
   * Checks if is emptyor all.
   *
   * @param array
   *          the array
   * @return true, if is emptyor all
   */
  private boolean isEmptyorAll(String[] array) {
    return array == null || array[0] == null || array[0].equals("") || array[0].equals("*");
  }

  /**
   * Select all in list.
   *
   * @param values
   *          the values
   * @return true, if successful
   */
  private boolean selectAllInList(String[] values) {
    return values != null && values.length == 1
        && (values[0].equalsIgnoreCase("*") || values[0].trim().isEmpty());
  }

}
