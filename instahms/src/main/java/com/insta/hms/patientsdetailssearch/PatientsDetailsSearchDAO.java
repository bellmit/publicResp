/**
 *
 */

package com.insta.hms.patientsdetailssearch;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.DynaBeanMapDecorator;
import org.apache.commons.beanutils.RowSetDynaClass;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The Class PatientsDetailsSearchDAO.
 *
 * @author krishna.t
 */
public class PatientsDetailsSearchDAO {

  /** The Constant SEARCH_PATIENTS_DETAILS_FIELDS. */
  private static final String SEARCH_PATIENTS_DETAILS_FIELDS = 
      " SELECT  Distinct mr_no, patient_name, last_name, salutation_name, patient_phone, "
      + "patient_gender,patient_full_name, age, age_in,"
      + " status, first_visit_reg_date, last_visited_date, vip_status,"
      + " mlc_status, "
      + " patient_area, city_name, last_visited_center, government_identifier, identifier_id,"
      + " ''::text as age_text, dateofbirth, expected_dob";

  /** The Constant SEARCH_PATIENTS_DETAILS_TABLES. */
  private static final String SEARCH_PATIENTS_DETAILS_TABLES = " FROM all_mrnos_view mv";

  /** The Constant SEARCH_PATIENTS_DETAILS_COUNT. */
  private static final String SEARCH_PATIENTS_DETAILS_COUNT = " SELECT count(mr_no) ";

  /**
   * Search patients details.
   *
   * @param params
   *          the params
   * @param listingParams
   *          the listing params
   * @return the paged list
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   */
  public static PagedList searchPatientsDetails(Map params, Map<LISTING, Object> listingParams)
      throws SQLException, ParseException {
    // wrap in another map to modify the parameter map, request.getParamaeterMap() is a readonly
    // map.
    Map requestParams = new HashMap(params);
    String customRegFieldName = getValue("_customRegFieldName", requestParams);
    String customRegFieldValue = getValue("_customRegFieldValue", requestParams);
    String regFieldName = getValue("_regFieldName", requestParams);
    String regFieldValue = getValue("_regFieldValue", requestParams);
    String mobileNo = getValue("patient_phone", requestParams);
    String patientGovtId = getValue("government_identifier", requestParams);
    requestParams.put("patient_phone", new String[] { "" });

    PagedList pagedList = null;
    Connection con = DataBaseUtil.getConnection();
    SearchQueryBuilder qb = null;

    try {
      String countQuery = DataBaseUtil.isLargeDataset("patient_details") ? null
          : SEARCH_PATIENTS_DETAILS_COUNT;
      qb = new SearchQueryBuilder(con, SEARCH_PATIENTS_DETAILS_FIELDS, countQuery,
          SEARCH_PATIENTS_DETAILS_TABLES, null, listingParams);
      qb.addFilterFromParamMap(requestParams);

      if ((!customRegFieldName.equals(""))
          && (customRegFieldName.equals("custom_field14")
              || customRegFieldName.equals("custom_field15") || customRegFieldName
                .equals("custom_field16"))) {

        qb.addFilter(SearchQueryBuilder.DATE, "mv." + customRegFieldName, "=",
            DateUtil.parseDate(customRegFieldValue));

      } else if ((!customRegFieldName.equals(""))
          && (customRegFieldName.equals("custom_field17")
              || customRegFieldName.equals("custom_field18") || customRegFieldName
                .equals("custom_field19"))) {

        qb.addFilter(SearchQueryBuilder.NUMERIC, "mv." + customRegFieldName, "=", new BigDecimal(
            customRegFieldValue));

      } else if (!customRegFieldName.equals("")) {
        if (customRegFieldName.equals("member_id")) {
          String memberId = "(member_id ilike '%" + customRegFieldValue
              + "%')";
          qb.appendToQuery(memberId);
        } else {
          qb.addFilter(SearchQueryBuilder.STRING, "mv." + customRegFieldName, "ilike",
              customRegFieldValue);
        }
      }
      if (!regFieldName.equals("")) {
        qb.addFilter(SearchQueryBuilder.STRING, "mv." + regFieldName + "::text", "ilike",
            regFieldValue);
      }
      if (!mobileNo.equals("")) {
        qb.addFilter(SearchQueryBuilder.STRING, "mv.patient_phone", "ilike", mobileNo);
      }
      if (!patientGovtId.equals("")) {
        qb.addFilter(SearchQueryBuilder.STRING, "mv.government_identifier", "ilike", patientGovtId);
      }
      qb.addSecondarySort("mr_no");
      qb.build();
      // l = qb.getMappedPagedList();
      pagedList = qb.getDynaPagedList(true);

      for (BasicDynaBean bean : (List<BasicDynaBean>) pagedList.getDtoList()) {
        boolean prec = (bean.get("dateofbirth") != null);
        bean.set(
            "age_text",
            prec ? DateUtil.getAgeText((java.sql.Date) bean.get("dateofbirth"), prec) : DateUtil
                .getAgeText((java.sql.Date) bean.get("expected_dob"), prec));
      }
    } finally {
      DataBaseUtil.closeConnections(con, null);
      if (qb != null) {
        qb.close();
      }
    }
    return pagedList;
  }

  /** The Constant FIND_PATIENTS_DETAILS_FIELDS. */
  private static final String FIND_PATIENTS_DETAILS_FIELDS = " "
      + " SELECT  Distinct mr_no, patient_name, last_name, salutation, patient_phone, "
      + " patient_gender, to_char(dateofbirth, 'YYYY-MM-DD') as dateofbirth,"
      + " email_id,(salutation_name||' '||patient_full_name) as patient_full_name,age,"
      + " age_in, status, to_char(first_visit_reg_date , 'YYYY-MM-DD') "
      + " as first_visit_reg_date, to_char(expected_dob, 'YYYY-MM-DD') as expected_dob,"
      + " to_char(last_visited_date, 'YYYY-MM-DD') as last_visited_date, "
      + " vip_status, mlc_status, patient_address, "
      + " patient_area, patient_city, patient_state, country, patient_category_id,"
      + " last_visited_center ,government_identifier, identifier_id";

  /** The Constant FIND_PATIENTS_DETAILS_TABLES. */
  private static final String FIND_PATIENTS_DETAILS_TABLES = " FROM all_mrnos_view mv";

  /** The Constant FIND_PATIENTS_DETAILS_COUNT. */
  private static final String FIND_PATIENTS_DETAILS_COUNT = " SELECT count(mr_no) ";

  /**
   * Find patients details.
   *
   * @param con
   *          the con
   * @param params
   *          the params
   * @param listingParams
   *          the listing params
   * @return the paged list
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   */
  public static PagedList findPatientsDetails(Connection con, Map params,
      Map<LISTING, Object> listingParams) throws SQLException, ParseException {
    // wrap in another map to modify the parameter map, request.getParamaeterMap() is a readonly
    // map.
    Map requestParams = new HashMap(params);
    String mobileNo = getValue("patient_phone", requestParams);
    String patientGovtId = getValue("government_identifier", requestParams);
    requestParams.put("patient_phone", new String[] { "" });

    PagedList pagedList = null;
    SearchQueryBuilder qb = null;
    if (con == null) {
      con = DataBaseUtil.getConnection();
    }
    try {
      qb = new SearchQueryBuilder(con, FIND_PATIENTS_DETAILS_FIELDS, FIND_PATIENTS_DETAILS_COUNT,
          FIND_PATIENTS_DETAILS_TABLES, null, listingParams);
      qb.addFilterFromParamMap(requestParams);

      if (!mobileNo.equals("")) {
        qb.addFilter(SearchQueryBuilder.STRING,
            "regexp_replace(mv.patient_phone, E'-|\\\\+|\\\\(|\\\\)|\\\\s', '', 'g')", "ilike",
            mobileNo);
      }
      qb.addFilter(SearchQueryBuilder.STRING, "mv.government_identifier", "ilike", patientGovtId);
      qb.addSecondarySort("mr_no");
      qb.build();
      pagedList = qb.getMappedPagedList();
    } finally {
      if (qb != null) {
        qb.close();
      }
    }
    return pagedList;
  }

  /** The Constant FIND_PATIENT_DETAIL_FIELDS_FOR_LVD. */
  private static final String FIND_PATIENT_DETAIL_FIELDS_FOR_LVD = 
      "WITH foo AS(SELECT coalesce(pd.mr_no,prvs_pr.mr_no) "
      + "as mr_no,pd.patient_name,pd.last_name,"
      + "pd.salutation,pd.patient_phone,pd.patient_gender,"
      + "pd.dateofbirth,pd.email_id,sal.salutation||' '"
      + "||patient_full_name as patient_full_name,pd.age,"
      + "pd.age_in,pd.status,pd.first_visit_reg_date,"
      + "pd.expected_dob,to_char(coalesce(pr.reg_date, prvs_pr.reg_date), "
      + "'YYYY-MM-DD') as last_visited_date,"
      + "pd.vip_status,pr.mlc_status,pd.patient_address,pd.patient_area,"
      + "pd.patient_city,pd.patient_state,pd.country,pd.patient_category_id,"
      + "coalesce(pr.center_id, prvs_pr.center_id) as last_visited_center,"
      + "pd.government_identifier,pd.identifier_id "
      + "FROM (SELECT mr_no,to_char(first_visit_reg_date , 'YYYY-MM-DD')  "
      + "as first_visit_reg_date,patient_name,middle_name,"
      + "last_name,CASE when visit_id is null and previous_visit_id  is null "
      + "then 'N' when visit_id is not null then 'A'when "
      + "visit_id is null then 'I' end as status,visit_id,patient_phone,"
      + "patient_gender,email_id,patient_name|| ' ' ||coalesce(middle_name, '')|| "
      + "case when coalesce(middle_name, '')!='' then ' '  else '' end || "
      + "coalesce(last_name, '') AS patient_full_name,to_char(dateofbirth,'YYYY-MM-DD') "
      + "as dateofbirth,to_char(expected_dob, 'YYYY-MM-DD') as expected_dob,"
      + "get_patient_age(dateofbirth, expected_dob) as age,"
      + "get_patient_age_in(dateofbirth,expected_dob) as age_in,salutation,"
      + "vip_status,patient_address,patient_area, patient_city, patient_state,"
      + " country, patient_category_id,government_identifier,identifier_id,"
      + "previous_visit_id FROM patient_details) pd LEFT JOIN patient_registration pr "
      + "ON pd.mr_no::text = pr.mr_no::text AND pr.patient_id = CASE WHEN "
      + "pd.visit_id != '' THEN pd.visit_id ELSE pd.previous_visit_id END "
      + "LEFT JOIN patient_registration prvs_pr ON (pd.previous_visit_id=prvs_pr.patient_id) "
      + "LEFT JOIN salutation_master sal ON (sal.salutation_id = pd.salutation)) "
      + "select * from foo where foo.last_visited_date = ANY (?)";

  /** The Constant FIND_PATIENT_DETAIL_FIELDS. */
  private static final String FIND_PATIENT_DETAIL_FIELDS = 
      "SELECT coalesce(pd.mr_no,prvs_pr.mr_no) as mr_no,"
      + "pd.patient_name,pd.last_name,pd.salutation,"
      + "pd.patient_phone,pd.patient_gender,pd.dateofbirth,"
      + "pd.email_id,sal.salutation||' '||patient_full_name as patient_full_name,"
      + "pd.patient_phone2, pd.relation as next_of_kin_name, "
      + "pd.next_of_kin_relation, pd.patient_care_oftext as next_of_kin_phone,"
      + "pd.patient_careof_address as next_of_kin_address, "
      + "pd.remarks as patient_remarks, pd.oldmrno, pd.nationality_id,"
      + "pd.custom_list1_value, pd.custom_list2_value, pd.custom_list3_value, "
      + "pd.custom_list4_value, pd.custom_list5_value,"
      + "pd.custom_list6_value, pd.custom_list7_value, pd.custom_list8_value, "
      + "pd.custom_list9_value, "
      + "coalesce(pr.reference_docto_id, prvs_pr.reference_docto_id) as "
      + "last_visit_referred_by, "
      + "coalesce(pr.dept_name, prvs_pr.dept_name) as last_visit_dept_id, "
      + "coalesce(pr.doctor, prvs_pr.doctor) as last_visit_admission_doctor_id, "
      + "coalesce(pr.transfer_source, prvs_pr.transfer_source) as "
      + "last_visit_transferred_from, "
      + "coalesce(pr.transfer_destination, prvs_pr.transfer_destination) "
      + "as last_visit_transferred_to, "
      + "pd.age,pd.age_in,pd.status,pd.first_visit_reg_date,pd.expected_dob,"
      + "to_char(coalesce(pr.reg_date, prvs_pr.reg_date), 'YYYY-MM-DD') "
      + "as last_visited_date,pd.vip_status,pr.mlc_status,pd.patient_address,"
      + "pd.patient_area,pd.patient_city,pd.patient_state,pd.country,"
      + "pd.patient_category_id,coalesce(pr.center_id, prvs_pr.center_id) as "
      + "last_visited_center,pd.government_identifier,pd.identifier_id";

  /** The Constant FIND_PATIENT_DETAIL_TABLES. */
  private static final String FIND_PATIENT_DETAIL_TABLES = 
      " FROM (SELECT mr_no,to_char(first_visit_reg_date , 'YYYY-MM-DD')  "
      + "as first_visit_reg_date,"
      + "patient_name,middle_name,last_name,CASE when visit_id is null and "
      + "previous_visit_id  is null then 'N' when visit_id is not null then 'A' "
      + "when visit_id is null then 'I' end as status,visit_id,patient_phone,"
      + "patient_gender,email_id,patient_name|| ' ' ||coalesce(middle_name, '')"
      + " || case when coalesce(middle_name, '')!='' then ' '  else '' end || "
      + "coalesce(last_name, '') AS patient_full_name,to_char(dateofbirth, "
      + "'YYYY-MM-DD') as dateofbirth,to_char(expected_dob, 'YYYY-MM-DD') as "
      + "expected_dob,get_patient_age(dateofbirth, expected_dob) as age,"
      + "get_patient_age_in(dateofbirth, expected_dob) as age_in,salutation,"
      + "vip_status,patient_address,patient_area, patient_city, patient_state,"
      + " country, patient_category_id,government_identifier, identifier_id,"
      + "previous_visit_id, patient_phone2, remarks, oldmrno, nationality_id, "
      + " custom_list1_value, custom_list2_value, custom_list3_value, "
      + "custom_list4_value, custom_list5_value, custom_list6_value, custom_list7_value,"
      + " custom_list8_value, custom_list9_value, patient_careof_address, "
      + "patient_care_oftext, next_of_kin_relation, relation FROM patient_details";

  /** The Constant FIND_PATIENT_DETAIL_JOIN_TABLES. */
  private static final String FIND_PATIENT_DETAIL_JOIN_TABLES = ") pd "
      + "LEFT JOIN patient_registration pr ON pd.mr_no::text = pr.mr_no::text "
      + "AND pr.patient_id = " + "CASE WHEN pd.visit_id != '' THEN " + "pd.visit_id " + "ELSE "
      + "pd.previous_visit_id " + "END "
      + "LEFT JOIN patient_registration prvs_pr ON (pd.previous_visit_id=prvs_pr.patient_id) "
      + "LEFT JOIN salutation_master sal ON (sal.salutation_id = pd.salutation)";

  /** The Constant SEARCH_PATIENT_DETAIL_COUNT. */
  private static final String SEARCH_PATIENT_DETAIL_COUNT = " SELECT count(pd.mr_no) ";

  /**
   * Find patient detail.
   *
   * @param con
   *          the con
   * @param params
   *          the params
   * @param listingParams
   *          the listing params
   * @return the list
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   */
  public static List findPatientDetail(Connection con, Map params,
      Map<LISTING, Object> listingParams) throws SQLException, ParseException {
    // wrap in another map to modify the parameter map,
    // request.getParamaeterMap() is a readonly map.
    Map requestParams = new HashMap(params);
    String mobileNo = getValue("patient_phone", requestParams);
    String patientGovtId = getValue("government_identifier", requestParams);
    requestParams.put("patient_phone", new String[] { "" });

    String[] lastVisitedDate = (String[]) params.get("last_visited_date");
    String[] mrNo = (String[]) params.get("mr_no");
    String[] patientName = (String[]) params.get("patient_name");
    String[] lastName = (String[]) params.get("last_name");
    String[] patientGender = (String[]) params.get("patient_gender");
    String[] patientPhone = (String[]) params.get("patient_phone");
    String[] emailId = (String[]) params.get("email_id");
    String[] dateofbirth = (String[]) params.get("dateofbirth");
    String[] governmentIdentifier = (String[]) params.get("government_identifier");
    String[] oldMrno = (String[]) params.get("oldmrno");

    List lis = null;
    SearchQueryBuilder qb = null;
    if (con == null) {
      con = DataBaseUtil.getConnection();
    }
    if (lastVisitedDate == null) {
      try {
        qb = new SearchQueryBuilder(con, FIND_PATIENT_DETAIL_FIELDS, SEARCH_PATIENT_DETAIL_COUNT,
            FIND_PATIENT_DETAIL_TABLES, listingParams);
        qb.addFilterFromParamMap(requestParams);
        if (!mobileNo.equals("")) {
          qb.addFilter(SearchQueryBuilder.STRING,
              "regexp_replace(patient_phone, E'-|\\\\+|\\\\(|\\\\)|\\\\s', '', 'g')", "ilike",
              mobileNo);
        }
        qb.appendExpression(FIND_PATIENT_DETAIL_JOIN_TABLES, new ArrayList(), new ArrayList());

        qb.addFilter(SearchQueryBuilder.STRING, "pd.government_identifier", "ilike", patientGovtId);
        qb.build();

        lis = qb.getMappedPagedList().getDtoList();
      } finally {
        if (qb != null) {
          qb.close();
        }
      }
      return lis;
    } else if (lastVisitedDate != null
        && (mrNo != null || patientGender != null 
        || lastName != null || patientName != null
            || patientPhone != null || emailId != null 
            || dateofbirth != null || governmentIdentifier != null 
            || oldMrno != null)) {
      PreparedStatement ps = null;
      try {

        requestParams.remove("last_visited_date");

        qb = new SearchQueryBuilder(con, FIND_PATIENT_DETAIL_FIELDS, SEARCH_PATIENT_DETAIL_COUNT,
            FIND_PATIENT_DETAIL_TABLES, listingParams);
        qb.addFilterFromParamMap(requestParams);
        if (!mobileNo.equals("")) {
          qb.addFilter(SearchQueryBuilder.STRING,
              "regexp_replace(patient_phone, E'-|\\\\+|\\\\(|\\\\)|\\\\s', '', 'g')", "ilike",
              mobileNo);
        }
        qb.appendExpression(FIND_PATIENT_DETAIL_JOIN_TABLES, new ArrayList(), new ArrayList());
        qb.addFilter(SearchQueryBuilder.STRING, "pd.government_identifier", "ilike", patientGovtId);

        qb.build();
        lis = qb.getMappedPagedList().getDtoList();

        for (Iterator iterator = lis.iterator(); iterator.hasNext();) {
          DynaBeanMapDecorator bean = (DynaBeanMapDecorator) iterator.next();
          String lvd = (String) bean.getDynaBean().get("last_visited_date");
          List uniqueLvds = Arrays.asList(lastVisitedDate);
          if (!uniqueLvds.contains(lvd)) {
            iterator.remove();
          }

        }

        return lis;
      } finally {
        if (qb != null) {
          qb.close();
        }
      }
    } else if (lastVisitedDate != null
        && (mrNo == null && patientGender == null 
        && lastName == null && patientName == null
            && patientPhone == null && emailId == null 
            && dateofbirth == null && governmentIdentifier == null
            && oldMrno == null)) {
      PreparedStatement ps = null;

      try {
        ps = con.prepareStatement(FIND_PATIENT_DETAIL_FIELDS_FOR_LVD);
        ps.setArray(1, con.createArrayOf("text", lastVisitedDate));

        RowSetDynaClass rsd = new RowSetDynaClass(ps.executeQuery());
        List list = rsd.getRows();
        lis = ConversionUtils.copyListDynaBeansToMap(list);

        return lis;

      } finally {
        DataBaseUtil.closeConnections(con, ps);
      }
    }
    return Collections.emptyList();
  }

  /**
   * Gets the value.
   *
   * @param key
   *          the key
   * @param params
   *          the params
   * @return the value
   */
  private static String getValue(String key, Map params) {
    Object[] obj = (Object[]) params.get(key);
    if (obj != null && obj[0] != null) {
      return obj[0].toString();
    }
    return "";
  }

}
