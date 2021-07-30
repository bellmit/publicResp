package com.insta.hms.visitdetailssearch;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class VisitDetailsSearchDAO.
 *
 * @author krishna.t
 */
public class VisitDetailsSearchDAO {

  /** The Constant GET_BED_DETAILS. */
  private static final String GET_BED_DETAILS = " SELECT pr.patient_id, "
      + " textcat_commacat(coalesce(wn.ward_name,'') || '/' || coalesce(pr.bed_type, '')) "
      + " as reg_ward_bed, "
      + " TEXTCAT_COMMACAT(COALESCE(wn1.ward_name,'') || '/' || COALESCE(abn.bed_name, '')) "
      + " as alloc_ward_bed, "
      + " TEXTCAT_COMMACAT(CASE WHEN ibd.status IN ('P') THEN bn.bed_name ELSE '' END  ) "
      + " AS previous_bed, "
      + " TEXTCAT_COMMACAT(CASE WHEN ibd.status IN ('R') AND NOT is_bystander "
      + " THEN bn.bed_name ELSE '' END  ) AS retain_bed, "
      + " TEXTCAT_COMMACAT(distinct CASE WHEN bn.bed_type IN (SELECT DISTINCT "
      + " intensive_bed_type FROM icu_bed_charges) "
      + " THEN bn.bed_name ELSE '' END  ) AS icu_beds, "
      + " TEXTCAT_COMMACAT(CASE WHEN ibd.status IN ('R') AND is_bystander "
      + " THEN bn.bed_name ELSE '' END  ) AS bystander_bed "
      + " FROM patient_registration pr " + " LEFT JOIN admission a using(patient_id) "
      + " LEFT JOIN ip_bed_details ibd on (pr.patient_id = ibd.patient_id "
      + " AND a.bed_id != ibd.bed_id) "
      + " LEFT JOIN bed_names bn ON (bn.bed_id = ibd.bed_id ) "
      + " LEFT JOIN bed_names abn ON (abn.bed_id = a.bed_id ) "
      + " LEFT JOIN ward_names wn1 ON (wn1.ward_no = abn.ward_no) "
      + " LEFT JOIN ward_names wn ON (pr.ward_id=wn.ward_no) ";

  /** The Constant SEARCH_VISITS_DETAILS_FIELDS. */
  private static final String SEARCH_VISITS_DETAILS_FIELDS = "SELECT * ";

  /** The Constant SEARCH_VISITS_DETAILS_TABLES. */
  private static final String SEARCH_VISITS_DETAILS_TABLES = " FROM ( SELECT mv.mr_no, "
      + " patient_name, mv.patient_id, last_name, salutation_name, patient_full_name, "
      + " patient_phone, visit_type, patient_gender, age, age_in, mv.status, "
      + " patient_address, patient_area, "
      + " mv.patient_city, mv.patient_state, mv.country, mv.org_id, "
      + " reg_date, visit_reg_date, doctor_name, dept_name, previous_visit_id, "
      + " discharge_date, visit_id, relation, discharge_time, "
      + " mlc_status,vip_status, patient_care_oftext, op_type,abn.bed_name as current_bed , "
      + " doc_id::text, doc_format, doc_format AS format, template_id::text, "
      + " doc_type, specialized, "
      + " mv.primary_sponsor_id, mv.secondary_sponsor_id, "
      + " mv.discharge_flag, mv.discharge_finalized_date, mv.discharge_finalized_user, "
      + " mv.doctor, mv.dept_id, a.bed_id, wn1.ward_no as alloc_ward_no, "
      + " wn.ward_no as reg_ward_no,"
      + " mv.custom_field1, mv.custom_field2, mv.custom_field3, "
      + " mv.custom_field4, mv.custom_field5,"
      + " mv.custom_field6, mv.custom_field7, mv.custom_field8, "
      + " mv.custom_field9, mv.custom_field10, "
      + " mv.custom_field11, mv.custom_field12, mv.custom_field13,mv.custom_field14,"
      + " mv.custom_field15,mv.custom_field16,"
      + " mv.custom_field17,mv.custom_field18,mv.custom_field19,mv.custom_list1_value, "
      + " mv.custom_list2_value, mv.custom_list3_value,mv.custom_list4_value,"
      + " mv.custom_list5_value,mv.custom_list6_value, "
      + " mv.custom_list7_value,mv.custom_list8_value,mv.custom_list9_value,"
      + " mv.government_identifier,mv.identifier_id,"
      + " mv.category_name, mv.primary_plan_name,mv.secondry_plan_name, mv.policy_no,"
      + " mv.primary_policy_holder_name,mv.secondry_policy_holder_name, "
      + " mv.mlc_no, mv.mlc_type, mv.accident_place, mv.police_stn,  "
      + " mv.oldmrno, mv.original_mr_no, mv.patient_careof_address, "
      + " mv.patient_category_id, mv.center_id, discharge_doc_status, "
      + " mv.complaint, mv.family_id, mv.primary_member_id,mv.secondry_member_id, "
      + " dateofbirth, expected_dob, ''::text as age_text,  "
      +
      // below fields populated using another query.
      " '' as reg_ward_bed, '' as alloc_ward_bed, '' as previous_bed, "
      + " '' as retain_bed, '' as icu_beds, "
      + " '' as bystander_bed " + " FROM all_visits_view mv "
      + " LEFT JOIN admission a using(patient_id) "
      + " LEFT JOIN bed_names abn ON (abn.bed_id = a.bed_id ) "
      + " LEFT JOIN ward_names wn1 ON (wn1.ward_no = abn.ward_no) "
      + " LEFT JOIN ward_names wn ON (mv.ward_id=wn.ward_no) ) as foo";

  /** The Constant SEARCH_VISITS_DETAILS_COUNT. */
  private static final String SEARCH_VISITS_DETAILS_COUNT = " SELECT count(*) ";

  /**
   * Search visits details.
   *
   * @param params
   *          the params
   * @param listingParams
   *          the listing params
   * @param centerId
   *          the center id
   * @return the paged list
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   */
  public static PagedList searchVisitsDetails(Map params, Map<LISTING, Object> listingParams,
      int centerId) throws SQLException, ParseException {
    // wrap in another map to modify the parameter map, request.getParamaeterMap() is a readonly
    // map.
    Map requestParams = new HashMap(params);
    listingParams.put(LISTING.PAGESIZE, 10); // display 10 patients per page.
    String customRegFieldName = getValue("_customRegFieldName", requestParams);
    String customRegFieldValue = getValue("_customRegFieldValue", requestParams);
    String regFieldName = getValue("_regFieldName", requestParams);
    String regFieldValue = getValue("_regFieldValue", requestParams);
    String allocWardNo = getValue("exclude_in_qb_alloc_ward_no", requestParams);
    String allocBedNo = getValue("exclude_in_qb_alloc_bed_no", requestParams);
    String regWardNo = getValue("exclude_in_qb_reg_ward_no", requestParams);
    String regBedType = getValue("exclude_in_qb_reg_bed_type", requestParams);
    String patientGovtId = getValue("government_identifier", requestParams);
    String mobileNo = getValue("patient_phone", requestParams);
    requestParams.put("patient_phone", new String[] { "" });

    PagedList pagedList = null;
    Connection con = DataBaseUtil.getConnection();
    SearchQueryBuilder qb = null;
    PreparedStatement ps = null;
    List<String> validColumns = Arrays.asList(
        new String[]{ "mr_no", "patient_id", "visit_reg_date", "dept_name", "reg_date" });
    try {
      String countQuery = DataBaseUtil.isLargeDataset("patient_registration") ? null
          : SEARCH_VISITS_DETAILS_COUNT;
      qb = new SearchQueryBuilder(con, SEARCH_VISITS_DETAILS_FIELDS, countQuery,
          SEARCH_VISITS_DETAILS_TABLES, null, null, listingParams, validColumns);
      qb.addFilterFromParamMap(requestParams);

      if ((!customRegFieldName.equals("")) && (customRegFieldName.equals("custom_field14")
          || customRegFieldName.equals("custom_field15")
          || customRegFieldName.equals("custom_field16"))) {

        qb.addFilter(SearchQueryBuilder.DATE, customRegFieldName, "=",
            DateUtil.parseDate(customRegFieldValue));
      } else if ((!customRegFieldName.equals("")) && (customRegFieldName.equals("custom_field17")
          || customRegFieldName.equals("custom_field18")
          || customRegFieldName.equals("custom_field19"))) {

        qb.addFilter(SearchQueryBuilder.NUMERIC, customRegFieldName, "=",
            new BigDecimal(customRegFieldValue));
      } else if (!customRegFieldName.equals("")) {
        if (customRegFieldName.equals("member_id")) {
          String memberId = "(primary_member_id ilike '%" + customRegFieldValue
              + "%' OR secondry_member_id ilike '%" + customRegFieldValue + "%')";
          qb.appendToQuery(memberId);
        } else if (customRegFieldName.equals("plan_name")) {
          String planName = "(primary_plan_name ilike '%" + customRegFieldValue
              + "%' OR secondry_plan_name ilike '%" + customRegFieldValue + "%')";
          qb.appendToQuery(planName);

        } else if (customRegFieldName.equals("policy_holder_name")) {
          String planName = "(primary_policy_holder_name ilike '%" + customRegFieldValue
              + "%' OR secondry_policy_holder_name ilike '%" + customRegFieldValue + "%')";
          qb.appendToQuery(planName);
        } else {
          qb.addFilter(SearchQueryBuilder.STRING, customRegFieldName, "ilike", customRegFieldValue);
        }
      }

      if (!mobileNo.equals("")) {
        qb.addFilter(SearchQueryBuilder.STRING, "patient_phone", "ilike", mobileNo);
      }

      if (!regFieldName.equals("")) {
        // field name externally to text is required since patient category id is integer and others
        // are strings.
        qb.addFilter(SearchQueryBuilder.STRING, regFieldName + "::text", "ilike", regFieldValue);
      }
      qb.addFilter(SearchQueryBuilder.STRING, "government_identifier", "ilike", patientGovtId);
      Object[] finalized = (Object[]) requestParams.get("exclude_in_qb_finalized");
      if (finalized != null && finalized[0] != null) {
        String disDocStatus = (String) finalized[0];
        if (!disDocStatus.equals("")) {
          qb.addFilter(SearchQueryBuilder.STRING, "discharge_doc_status", "in",
              Arrays.asList(finalized));
        }  
      }
      qb.addFilter(SearchQueryBuilder.STRING, "reg_ward_no", "=", regWardNo);
      qb.addFilter(SearchQueryBuilder.STRING, "alloc_ward_no", "=", allocWardNo);
      qb.addFilter(SearchQueryBuilder.STRING, "bed_type", "=", regBedType);
      qb.addFilter(SearchQueryBuilder.INTEGER, "bed_id", "=",
          allocBedNo.equals("") ? null : Integer.parseInt(allocBedNo));

      if (centerId != 0) {
        qb.addFilter(SearchQueryBuilder.INTEGER, "center_id", "=", centerId);
      } 

      qb.addSecondarySort("visit_reg_date");
      qb.build();
      pagedList = qb.getDynaPagedList(true);

      Map map = null;
      if (!pagedList.getDtoList().isEmpty()) {
        StringBuilder query = new StringBuilder(GET_BED_DETAILS);
        DataBaseUtil.addWhereFieldInList(query, "pr.patient_id", pagedList.getDtoList(), false);
        query.append(" GROUP BY pr.patient_id ");
        ps = con.prepareStatement(query.toString());
        int count = 1;
        for (BasicDynaBean b : (List<BasicDynaBean>) pagedList.getDtoList()) {
          ps.setObject(count++, b.get("patient_id"));
        }
        // get the columns which are having textcat function.
        List dataList = DataBaseUtil.queryToDynaList(ps);
        map = ConversionUtils.listBeanToMapBean(dataList, "patient_id");
      }

      for (BasicDynaBean bean : (List<BasicDynaBean>) pagedList.getDtoList()) {
        boolean prec = (bean.get("dateofbirth") != null);
        if (prec) {
          bean.set("age_text", DateUtil.getAgeText((java.sql.Date) bean.get("dateofbirth"), prec));
        } else if (bean.get("expected_dob") != null) {
          bean.set("age_text", DateUtil.getAgeText((java.sql.Date) bean.get("expected_dob"), prec));
        } 

        BasicDynaBean beans = (BasicDynaBean) map.get((String) bean.get("patient_id"));
        bean.set("reg_ward_bed", beans.get("reg_ward_bed"));
        bean.set("alloc_ward_bed", beans.get("alloc_ward_bed"));
        bean.set("previous_bed", beans.get("previous_bed"));
        bean.set("retain_bed", beans.get("retain_bed"));
        bean.set("icu_beds", beans.get("icu_beds"));
        bean.set("bystander_bed", beans.get("bystander_bed"));
      }

    } finally {
      DataBaseUtil.closeConnections(con, null);
      if (qb != null) {
        qb.close();
      } 
    }
    return pagedList;
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

  /**
   * Checks if is bed type in use.
   *
   * @param bedType
   *          the bed type
   * @return true, if is bed type in use
   * @throws SQLException
   *           the SQL exception
   */
  public boolean isBedTypeInUse(String bedType) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;

    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(
          " SELECT * FROM patient_registration WHERE bed_type = ? AND status = 'A' LIMIT 1 ");
      ps.setString(1, bedType);

      return (DataBaseUtil.queryToDynaBean(ps) != null);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }
}
