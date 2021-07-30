package com.insta.hms.mdm.insuranceplans;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.SearchQueryAssembler;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** The Class InsurancePlanRepository. */

@Repository("insurancePlanRepository")
public class InsurancePlanRepository extends MasterRepository<Integer> {

  /** Instantiates a new insurance plan repository. */
  public InsurancePlanRepository() {
    super("insurance_plan_main", "plan_id");
  }

  /** The get Sponsor from category_id. */
  private static String GET_SPONSORS = "SELECT sponsor_id FROM insurance_plan_main"
      + " where  category_id=? and status = 'A'";

  /**
   * Gets the SPONSERS.
   *
   * @param categoryId the category id
   *
   * @return sponsors
   */
  public List<BasicDynaBean> getSponserByCategory(Integer categoryId) {
    return DatabaseHelper.queryToDynaList(GET_SPONSORS, new Object[] { categoryId });
  }

  /** The get mapped plans. */
  private static String GET_MAPPED_PLANS =
      "select *,(select discount_plan_name " + " from discount_plan_main where "
          + " discount_plan_id = insurance_plan_main.discount_plan_id) "
          + " as discount_plan_name from insurance_plan_main  "
          + " where category_id = ? and (sponsor_id = ? or sponsor_id is null or sponsor_id = '')"
          + "  and op_applicable='Y' and (insurance_validity_start_date<now() "
          + " or insurance_validity_start_date is null) and (insurance_validity_end_date >now() "
          + " or insurance_validity_end_date is null) ";

  /**
   * Gets the mapped plans.
   *
   * @param categoryId the category id
   * @param tpaId the tpa id
   * @return the mapped plans
   */
  public List<BasicDynaBean> getMappedPlans(Integer categoryId, String tpaId) {
    return DatabaseHelper.queryToDynaList(GET_MAPPED_PLANS, new Object[] {categoryId, tpaId});
  }

  /** The get default rate plan. */
  private static String GET_DEFAULT_RATE_PLAN =
      " select org_name,org_id " + " from organization_details od "
          + " join insurance_plan_main ipm on ipm.default_rate_plan = od.org_id "
          + " where ipm.plan_id=? ";

  /**
   * Gets the plan default rate plan.
   *
   * @param planId the plan id
   * @return the plan default rate plan
   */
  public List<BasicDynaBean> getPlanDefaultRatePlan(Integer planId) {
    return DatabaseHelper.queryToDynaList(GET_DEFAULT_RATE_PLAN, new Object[] {planId});
  }

  /** The Constant SELECT_PLAN_TYPES_FOR_SPONSOR. */
  private static final String SELECT_PLAN_TYPES_FOR_SPONSOR =
      "SELECT distinct icatm.category_id," + " icatm.insurance_co_id, "
          + " icatm.category_name,icatm.status FROM insurance_category_master icatm "
          + " LEFT JOIN insurance_category_center_master iccm "
          + " ON(icatm.category_id=iccm.category_id) ";

  /** The Constant JOIN_MAPPED_INSURANCE_COMPANIES. */
  private static final String JOIN_MAPPED_INSURANCE_COMPANIES = "SELECT * FROM "
      + " ( select distinct icm.insurance_co_id FROM insurance_company_master icm "
      + " LEFT JOIN insurance_company_tpa_master ictm "
      + " ON ictm.insurance_co_id = icm.insurance_co_id "
      + " where icm.status = 'A' AND case when ( (select count(tpa_id) "
      + " as count from insurance_company_tpa_master ictm_t "
      + " JOIN insurance_company_master icm_t ON "
      + " (ictm_t.insurance_co_id = icm_t.insurance_co_id) "
      + " where ictm_t.tpa_id= ? and icm_t.status = 'A' limit 1) = 0  ) "
      + " THEN  true else ictm.tpa_id= ?end ) AS foo ";

  /** The join patient category mapped. */
  private static String JOIN_PATIENT_CATEGORY_MAPPED =
      " JOIN " + " (select regexp_split_to_table(op_allowed_insurance_co_ids, E',') "
          + " AS insurance_co_id_split "
          + " FROM patient_category_master WHERE category_id= ? ) AS foo1 "
          + " ON (foo1.insurance_co_id_split = foo.insurance_co_id "
          + " OR foo1.insurance_co_id_split = '*') ";

  /** The Constant WHERE_PLAN_TYPES. */
  private static final String WHERE_PLAN_TYPES =
      " WHERE icatm.status='A' AND iccm.status='A' ";

  /** The Constant ORDER_BY_PLAN_TYPES. */
  private static final String ORDER_BY_PLAN_TYPES = " ORDER BY category_name ";

  /** The Constant WHERE_PLAN_NAMES. */
  private static final String WHERE_PLAN_NAMES =
      " WHERE op_applicable='Y' AND ipm.status = 'A' "
          + " AND (insurance_validity_start_date <= current_date "
          + " OR insurance_validity_start_date is null)"
          + " AND (insurance_validity_end_date >= current_date "
          + " OR insurance_validity_end_date is null) ";

  /** The Constant SELECT_PLAN_NAMES. */
  private static final String SELECT_PLAN_NAMES =
      " SELECT  ipm.*, " + " (select discount_plan_name FROM discount_plan_main WHERE "
          + " discount_plan_id = ipm.discount_plan_id) "
          + " AS discount_plan_name FROM insurance_plan_main AS ipm";

  /**
   * Gets the plan names for sponsor.
   *
   * @param tpaId the tpa id
   * @param categoryId the category id
   * @param centerId the center id
   * @return the plan names for sponsor
   */
  public List<BasicDynaBean> getPlanNamesForSponsor(String tpaId, Integer categoryId,
      Integer centerId) {
    List<Object> values = new ArrayList<Object>();
    StringBuilder joinQuery = new StringBuilder(SELECT_PLAN_TYPES_FOR_SPONSOR);
    {
      String tpaSelect = null;
      values.add(tpaId);
      values.add(tpaId);

      if (categoryId == null) {
        tpaSelect = JOIN_MAPPED_INSURANCE_COMPANIES;
      } else {
        values.add(categoryId);
        tpaSelect = JOIN_MAPPED_INSURANCE_COMPANIES + JOIN_PATIENT_CATEGORY_MAPPED;
      }

      joinQuery.append(" JOIN ( " + tpaSelect
          + " ) as foo3 ON (foo3.insurance_co_id = icatm.insurance_co_id) ");
      joinQuery.append(WHERE_PLAN_TYPES);
      if (centerId != null) {
        joinQuery.append(" AND ( iccm.center_id = ? or iccm.center_id = 0 ) ");
        values.add(centerId);
      }
      joinQuery.append(ORDER_BY_PLAN_TYPES);
    }
    StringBuilder query = new StringBuilder(SELECT_PLAN_NAMES);
    query.append(" JOIN ( " + joinQuery.toString() + " ) AS foo4 ON "
        + " ((? = ipm.sponsor_id OR ipm.sponsor_id is null OR ipm.sponsor_id = '')"
        + "  AND ipm.category_id = foo4.category_id)");
    values.add(tpaId);
    query.append(WHERE_PLAN_NAMES + " order by ipm.plan_name");
    return DatabaseHelper.queryToDynaList(query.toString(), values.toArray());
  }

  /** The plan detail filter select. */
  private static String PLAN_DETAIL_FILTER_SELECT = "SELECT plan_id ";

  /** The plan detail filter tables. */
  private static String PLAN_DETAIL_FILTER_TABLES = " FROM  insurance_plan_details ";

  /** The plan master query count. */
  private static String PLAN_MASTER_QUERY_COUNT = "SELECT COUNT(*) ";

  /** The plan master query select. */
  private static String PLAN_MASTER_QUERY_SELECT = "SELECT * ";

  /** The plan master query tables. */
  private static String PLAN_MASTER_QUERY_TABLES =
      "FROM (SELECT plan_id," + " plan_name, plan_notes, plan_exclusions, "
          + " icom.insurance_co_id, insurance_co_name, category_name, "
          + " icam.category_id, ipm.username, ipm.mod_time, "
          + " ipm.ip_applicable, ipm.op_applicable, overall_treatment_limit, "
          + " ipm.default_rate_plan , ipm.status, ipm.is_copay_pc_on_post_discnt_amt,"
          + " ipm.base_rate, ipm.gap_amount, ipm.marginal_percent,"
          + " ipm.perdiem_copay_per, ipm.perdiem_copay_amount, "
          + " ipm.op_visit_copay_limit, ipm.ip_visit_copay_limit, "
          + " ipm.insurance_validity_start_date, ipm.insurance_validity_end_date, "
          + " ipm.require_pbm_authorization , #centerDetails"
          + " op_plan_limit,op_episode_limit,op_visit_limit,"
          + " ip_plan_limit,ip_visit_limit,ip_per_day_limit,"
          + " op_visit_deductible ,ip_visit_deductible ,op_copay_percent,"
          + " ip_copay_percent,limits_include_followup,sponsor_id, "
          + " tpa_name, discount_plan_id, ipm.add_on_payment_factor "
          + " FROM insurance_plan_main ipm "
          + " LEFT JOIN insurance_company_master icom USING (insurance_co_id) "
          + " LEFT JOIN insurance_category_master icam USING (category_id)"
          + " #centerJoin"
          + " LEFT JOIN tpa_master tm ON(tm.tpa_id = ipm.sponsor_id)) AS rec";

  /**
   * Gets the insurance plan main details.
   *
   * @param params the params
   * @param listingParams the listing params
   * @param centerId the center Id
   * @param totalCenters the total centers
   * @return the insurance plan main details
   */
  @SuppressWarnings("rawtypes")
  public SearchQueryAssembler getInsurancePlanMainDetails(Map params,
      Map<LISTING, Object> listingParams, int centerId, int totalCenters) {
    SearchQueryAssembler qa = null;
    ArrayList<Integer> planDetIdsList = new ArrayList<Integer>();
    List list = new ArrayList();

    String baseQuery = PLAN_MASTER_QUERY_TABLES;
    if (centerId != 0 && totalCenters > 1) {
      baseQuery = baseQuery.replace("#centerJoin",
          "LEFT JOIN insurance_category_center_master iccm "
              + "ON (icam.category_id = iccm.category_id)");
      baseQuery = baseQuery.replace("#centerDetails", "iccm.center_id,"
          + " iccm.status as insurance_category_center_status,");
    } else {
      baseQuery = baseQuery.replace("#centerJoin", "");
      baseQuery = baseQuery.replace("#centerDetails", "");

    }
    qa = new SearchQueryAssembler(PLAN_MASTER_QUERY_SELECT, PLAN_MASTER_QUERY_COUNT,
        baseQuery, listingParams);

    qa.addFilterFromString("string", "plan_name", "ilike",
        getStringFromParamObj(params.get("plan_name")), true);
    qa.addFilterFromString("string", "insurance_co_id", "eq",
        getStringFromParamObj(params.get("insurance_co_id")), true);
    qa.addFilterFromString("integer", "category_name", "ilike",
        getStringFromParamObj(params.get("category_name")), true);
    qa.addFilterFromString("numeric", "overall_treatment_limit",
        getStringFromParamObj(params.get("overall_treatment_limit@op")),
        getStringFromParamObj(params.get("overall_treatment_limit")), true);
    qa.addFilterFromString("string", "default_rate_plan", "eq",
        getStringFromParamObj(params.get("default_rate_plan")), true);
    qa.addFilterFromString("string", "ip_applicable", "eq",
        getStringFromParamObj(params.get("ip_applicable")), true);
    qa.addFilterFromString("string", "op_applicable", "eq",
        getStringFromParamObj(params.get("op_applicable")), true);
    qa.addFilterFromString("string", "status", "eq",
        getStringFromParamObj(params.get("status")), true);
    qa.addFilterFromString("string", "is_copay_pc_on_post_discnt_amt", "eq",
        getStringFromParamObj(params.get("is_copay_pc_on_post_discnt_amt")), true);
    qa.addFilterFromString("string", "require_pbm_authorization", "eq",
        getStringFromParamObj(params.get("require_pbm_authorization")), true);
    qa.addFilterFromString("string", "sponsor_id", "eq",
        getStringFromParamObj(params.get("sponsor_id")), true);
    qa.addFilterFromString("integer", "discount_plan_id", "eq",
        getStringFromParamObj(params.get("discount_plan_id")), true);

    if (params.get("insurance_category_id") != null && params.get("detailTypeAmt") != null
        && getStringFromParamObj(params.get("detailTypeAmt")) != null) {
      SearchQueryAssembler subQa = new SearchQueryAssembler(PLAN_DETAIL_FILTER_SELECT,
          PLAN_MASTER_QUERY_COUNT, PLAN_DETAIL_FILTER_TABLES);
      subQa.addFilterFromString("numeric", getStringFromParamObj(params.get("detailType")),
          getStringFromParamObj(params.get("detailType@op")),
          getStringFromParamObj(params.get("detailTypeAmt")), true);
      if (params.get("insurance_category_id") != null
          && getStringFromParamObj(params.get("insurance_category_id")) != null) {
        subQa.addFilterFromString("string", "insurance_category_id", "eq",
            getStringFromParamObj(params.get("insurance_category_id")), true);
      }
      subQa.build();
      List<BasicDynaBean> planDetIdsStrList =
          DatabaseHelper.queryToDynaList(subQa.getDataQueryString());
      if (planDetIdsStrList != null && !planDetIdsStrList.isEmpty()) {
        for (BasicDynaBean s : planDetIdsStrList) {
          if (s != null) {
            planDetIdsList.add(Integer.parseInt((String) s.get("plan_id")));
          }
        }
      }
      if (planDetIdsList.isEmpty()) {
        planDetIdsList.add(0);
      }
    }
    if (centerId != 0 && totalCenters > 1) {
      list.add(centerId);
      list.add(0);
      qa.addFilter(qa.INTEGER, "center_id", "IN", list);
      qa.addFilter(qa.STRING, "insurance_category_center_status", "=", "A");
    }
    qa.addFilter(qa.INTEGER, "plan_id", "in", planDetIdsList);
    qa.addSecondarySort("plan_id");
    qa.build();
    return qa;
  }

  /**
   * Gets the string from param obj.
   *
   * @param obj the obj
   * @return the string from param obj
   */
  private static String getStringFromParamObj(Object obj) {
    if (obj == null || obj.equals("")) {
      return null;
    } else {
      return ((String[]) obj)[0] == null || ((String[]) obj)[0].equals("") ? null
          : ((String[]) obj)[0];
    }
  }

  /** The discount plan list. */
  private static String discount_plan_list = " select discount_plan_id,discount_plan_name "
      + " from discount_plan_main where coalesce(validity_start,current_date)<=current_date "
      + " and coalesce(validity_end,current_date) >= current_date and status='A' "
      + " order by discount_plan_name ";

  /**
   * Gets the default discount plan list.
   *
   * @return the default discount plan list
   */
  public static List<BasicDynaBean> getDefaultDiscountPlanList() {

    return DatabaseHelper.queryToDynaList(discount_plan_list);
  }

  /**
   * Find plan.
   *
   * @param keycolumn the keycolumn
   * @param identifier the identifier
   * @return the basic dyna bean
   */
  public BasicDynaBean findPlan(String keycolumn, Object identifier) {
    return findPlan(keycolumn, identifier, "=");
  }

  /**
   * Find plan.
   *
   * @param keycolumn the keycolumn
   * @param identifier the identifier
   * @param operator the operator
   * @return the basic dyna bean
   */
  public BasicDynaBean findPlan(String keycolumn, Object identifier, String operator) {

    keycolumn = DataBaseUtil.quoteIdent(keycolumn);
    String baseQuery = PLAN_MASTER_QUERY_TABLES;
    baseQuery = baseQuery.replace("#centerJoin", "");
    baseQuery = baseQuery.replace("#centerDetails", "");
    StringBuilder query = new StringBuilder();
    query.append("SELECT * ").append(baseQuery).append(" WHERE ")
        .append(keycolumn).append(" " + operator + " ?;");
    if (operator.equals("ilike")) {
      identifier = "%" + identifier + "%";
    }
    return DatabaseHelper.queryToDynaBean(query.toString(), identifier);
  }

  /** The deduction for plan. */
  public static String DEDUCTION_FOR_PLAN = " SELECT ipd.plan_id, ipd.insurance_category_id, "
      + " ipd.patient_amount, CASE WHEN iic.insurance_payable = 'N' THEN 100 ELSE "
      + " ipd.patient_percent END AS patient_percent, patient_amount_cap, "
      + " ipd.per_treatment_limit, ipd.patient_type, ipd.patient_amount_per_category, "
      + " foo.ip_applicable, foo.op_applicable, iic.insurance_category_name, "
      + " iic.insurance_payable, " + " foo.is_copay_pc_on_post_discnt_amt,  "
      + " ipd.category_payable " + " FROM insurance_plan_details  ipd "
      + " LEFT JOIN (SELECT ip_applicable, op_applicable, plan_id, "
      + " is_copay_pc_on_post_discnt_amt "
      + " FROM insurance_plan_main ) AS foo USING (plan_id) "
      + " LEFT JOIN item_insurance_categories iic ON "
      + " iic.insurance_category_id = ipd.insurance_category_id "
      + " WHERE plan_id=? AND ipd.insurance_category_id=? AND patient_type=? ";

  /** The is ip applicable. */
  public static String IS_IP_APPLICABLE = " AND ip_applicable = 'Y' ";

  /** The is op applicable. */
  public static String IS_OP_APPLICABLE = " AND op_applicable = 'Y' ";

  /**
   * Gets the charge amt for plan.
   *
   * @param planId the plan id
   * @param categoryId the category id
   * @param visitType the visit type
   * @return the charge amt for plan
   */
  public BasicDynaBean getChargeAmtForPlan(int planId, int categoryId, String visitType) {
    if (visitType.equals("o")) {
      return DatabaseHelper.queryToDynaBean(DEDUCTION_FOR_PLAN + IS_OP_APPLICABLE,
          new Object[] {planId, categoryId, visitType});
    } else {
      return DatabaseHelper.queryToDynaBean(DEDUCTION_FOR_PLAN + IS_IP_APPLICABLE,
          new Object[] {planId, categoryId, visitType});
    }
  }

  /**
   * Get category id based on plan ids.
   *
   * @param query the query
   * @param itemIds the items ids
   * @param planIds the plan ids
   * @param visitType the visit type
   * @return category id list
   */
  public List<BasicDynaBean> getCatIdBasedOnPlanIds(String query, List<String> itemIds,
      Set<Integer> planIds, String visitType) {
    Object[] planId = planIds.toArray();
    String[] placeHolderArr = new String[itemIds.size()];
    Arrays.fill(placeHolderArr, "?");

    List<Object> args = new ArrayList<Object>();
    args.add((int) planId[0]);
    args.add(visitType);
    args.addAll(itemIds);
    if (planId.length > 1) {
      args.add((int) planId[1]);
    } else {
      args.add(-1); // return default
    }
    args.add(visitType);
    args.addAll(itemIds);
    args.addAll(itemIds);
    String placeHolders = StringUtils.arrayToCommaDelimitedString(placeHolderArr);
    String categoryQuery = query.replaceAll("#", placeHolders);

    return DatabaseHelper.queryToDynaList(categoryQuery, args.toArray());
  }

  private static final String CHECK_IS_GENERAL_CAT_EXISTS_FOR_REG_CHARGES =
      "SELECT * " + " FROM patient_insurance_plan_details ipd "
          + " WHERE ipd.visit_id = ? AND ipd.plan_id =? AND ipd.patient_type = ? "
          + " AND insurance_category_id = -1 ";

  /**
   * Check general cat existance.
   *
   * @param planId Plan Id
   * @param visitType the visit type
   * @return Boolean
   */
  public Boolean checkIsGeneralCategoryExistsForRegCharges(String visitId, int planId,
      String visitType) {

    BasicDynaBean catBean =
        DatabaseHelper.queryToDynaBean(CHECK_IS_GENERAL_CAT_EXISTS_FOR_REG_CHARGES,
            new Object[] {visitId, planId, visitType});
    Boolean isGeneralCatExists = false;
    if (null != catBean && catBean.get("insurance_category_id") != null) {
      isGeneralCatExists = true;
    }
    return isGeneralCatExists;
  }

  private static final String GET_PLANS_BY_CATEGORY =
      "SELECT plan_id, plan_name" + " FROM insurance_plan_main"
          + " WHERE category_id=? AND insurance_co_id=? AND limit_type = 'R' AND status = 'A' AND "
          + "((insurance_validity_start_date <= current_date "
          + " OR insurance_validity_start_date IS NULL)"
          + " AND (current_date <= insurance_validity_end_date"
          + " OR insurance_validity_end_date IS NULL))";

  public List<BasicDynaBean> getPlansByCategory(Integer categoryId,
      String insuranceCompanyId) {
    return DatabaseHelper.queryToDynaList(GET_PLANS_BY_CATEGORY,
        new Object[] {categoryId, insuranceCompanyId});
  }

  private static final String GET_PLANS_LIST_FOR_CAT_INSCO =
      "SELECT plan_id, plan_name, category_id, overall_treatment_limit, insurance_co_id, "
      + "plan_notes, plan_exclusions, default_rate_plan, username, mod_time, "
      + "ip_applicable, op_applicable, status, is_copay_pc_on_post_discnt_amt, "
      + "base_rate, gap_amount, marginal_percent, perdiem_copay_per, "
      + "perdiem_copay_amount, require_pbm_authorization, op_visit_copay_limit, "
      + "ip_visit_copay_limit, "
      + "to_char( insurance_validity_start_date, 'YYYY-MM-DD') "
      + "  as insurance_validity_start_date, "
      + "to_char( insurance_validity_end_date, 'YYYY-MM-DD') "
      + "  as insurance_validity_end_date, "
      + "op_plan_limit, op_episode_limit, op_visit_limit, ip_plan_limit, ip_visit_limit, "
      + "ip_per_day_limit, op_visit_deductible, ip_visit_deductible, op_copay_percent, "
      + "ip_copay_percent, limits_include_followup, sponsor_id, discount_plan_id, "
      + "add_on_payment_factor, interface_code  "
      + "from insurance_plan_main where  "
      + " insurance_co_id = (:inscoId) AND status ='A'";

  /**
   * Gets the plans by category, company id and insurance co id.
   *
   * @param categoryId the category id
   * @param insuranceCompanyId the insurance company id
   * @param sponsorId the sponsor id
   * @return the plans by category and company id
   */
  public List<BasicDynaBean> getPlansByCategoryAndCompanyId(Integer categoryId,
      String insuranceCompanyId, String sponsorId) {
    StringBuilder query = new StringBuilder(GET_PLANS_LIST_FOR_CAT_INSCO);
    MapSqlParameterSource params = new MapSqlParameterSource();

    if (org.apache.commons.lang.StringUtils.isNotEmpty(insuranceCompanyId)) {
      params.addValue("inscoId", insuranceCompanyId);
    }

    if (categoryId != null) {
      query.append(" AND category_id = (:categoryId) ");
      params.addValue("categoryId", categoryId);
    }

    if (org.apache.commons.lang.StringUtils.isNotEmpty(sponsorId)) {
      query.append(" AND (sponsor_id = (:sponsorId) OR sponsor_id = '' OR sponsor_id IS NULL) ");
      params.addValue("sponsorId", sponsorId);
    }
    return DatabaseHelper.queryToDynaList(query.toString(), params);
  }

  private static final String GET_PLANS_BY_INSURANCE_CATEGORY_MASTER_ID =
      "SELECT ipm.plan_id from insurance_plan_main ipm "
        + " WHERE icm.category_id = ipm.category_id";

  private static final String GET_CATEGORIES_BY_COMPANY_ID =
      "Select icm.category_id, icm.category_name" + " FROM insurance_category_master icm"
          + " LEFT JOIN insurance_category_center_master iccm "
          + "   ON (icm.category_id = iccm.category_id)"
          + " WHERE icm.insurance_co_id=:insuranceCompanyId AND icm.status = 'A'";

  /**
   * Used to Insurance categories by company Id.
   *
   * @param insuranceCompanyId - The Insurance company Id
   * @param sponsorId - The sponsor Id
   * @return list of insurance categories as DynaBeans
   */
  public List<BasicDynaBean> getCategoriesByCompanyId(String insuranceCompanyId, String sponsorId) {
    StringBuilder query = new StringBuilder(GET_CATEGORIES_BY_COMPANY_ID);
    query.append(" AND EXISTS (");
    query.append(GET_PLANS_BY_INSURANCE_CATEGORY_MASTER_ID);
    MapSqlParameterSource values = new MapSqlParameterSource();
    values.addValue("insuranceCompanyId", insuranceCompanyId);
    if (sponsorId != "" && sponsorId != null) {
      query.append(" AND (ipm.sponsor_id=:sponsorId OR ipm.sponsor_id='' "
              + " OR ipm.sponsor_id is null)");
      values.addValue("sponsorId", sponsorId);
    }
    query.append(")");
    return DatabaseHelper.queryToDynaList(query.toString(), values);
  }

  /**
   * Used to Insurance categories by company Id and center.
   *
   * @param insuranceCompanyId - The Insurance company Id
   * @param sponsorId - The sponsor Id
   * @param centerId - The center Id
   * @return list of insurance categories as DynaBeans
   */
  public List<BasicDynaBean> getCategoriesByCompanyId(String insuranceCompanyId,
      String sponsorId,int centerId) {
    StringBuilder query = new StringBuilder(GET_CATEGORIES_BY_COMPANY_ID);
    query.append(" AND EXISTS (");
    query.append(GET_PLANS_BY_INSURANCE_CATEGORY_MASTER_ID);
    MapSqlParameterSource values = new MapSqlParameterSource();
    values.addValue("insuranceCompanyId", insuranceCompanyId);
    if (sponsorId != "" && sponsorId != null) {
      query.append(" AND (ipm.sponsor_id=:sponsorId OR ipm.sponsor_id='' "
              + " OR ipm.sponsor_id is null)");
      values.addValue("sponsorId", sponsorId);
    }

    if (centerId > 0) {
      query.append(" AND ( iccm.center_id =:centerId or iccm.center_id = 0 ) ");
      values.addValue("centerId", centerId);
    }
    query.append(")");
    return DatabaseHelper.queryToDynaList(query.toString(), values);
  }

  public static final String GET_TPA_DETAILS_FROM_PLAN_CODE_FOR_OP =
      "SELECT ipm.plan_id as insurance_plan_id, ipm.plan_code as insurance_plan_code,"
          + " ipm.plan_name as insurance_plan_name, "
          + " icom.insurance_co_name as insurance_company_name,"
          + " icom.insurance_co_id as insurance_company_id, "
          + " icm.category_name as plan_type_name,icm.category_id as plan_type_id, "
          + " tm.tpa_name as sponsor_name, tm.tpa_id as sponsor_id "
          + " FROM insurance_plan_main ipm " //
          + " JOIN (select regexp_split_to_table (op_allowed_sponsors, E',') " //
          + " as tpa_id from patient_category_master where category_id= ? ) as foo "
          + " ON (foo.tpa_id = ipm.sponsor_id OR foo.tpa_id = '*') "
          + " JOIN tpa_master tm ON (tm.tpa_id = ipm.sponsor_id and tm.status='A') "
          + " JOIN insurance_category_master icm ON "
          + " (icm.insurance_co_id = ipm.insurance_co_id and ipm.category_id=icm.category_id) "
          + " LEFT JOIN insurance_company_master icom "
          + " ON (icom.insurance_co_id = icm.insurance_co_id) "
          + " WHERE ipm.plan_code ilike ? AND ipm.sponsor_id IS NOT NULL "
          + " AND ipm.status = 'A' AND ipm.op_applicable='Y' "
          + " AND ((insurance_validity_start_date <= current_date "
          + " OR insurance_validity_start_date IS NULL)"
          + " AND (current_date <= insurance_validity_end_date "
          + " OR insurance_validity_end_date IS NULL)) LIMIT 10";

  public static final String GET_TPA_DETAILS_FROM_PLAN_CODE_FOR_IP =
      "SELECT ipm.plan_id as insurance_plan_id, ipm.plan_code as insurance_plan_code,"
          + " ipm.plan_name as insurance_plan_name, "
          + " icom.insurance_co_name as insurance_company_name, "
          + " icom.insurance_co_id as insurance_company_id, "
          + " icm.category_name as plan_type_name,icm.category_id as plan_type_id, "
          + " tm.tpa_name as sponsor_name, tm.tpa_id as sponsor_id "
          + " FROM insurance_plan_main ipm " //
          + " JOIN (select regexp_split_to_table (ip_allowed_sponsors, E',') " //
          + " as tpa_id from patient_category_master where category_id= ? ) as foo "
          + " ON (foo.tpa_id = ipm.sponsor_id OR foo.tpa_id = '*') "
          + " JOIN tpa_master tm ON (tm.tpa_id = ipm.sponsor_id and tm.status='A') "
          + " JOIN insurance_category_master icm ON "
          + " (icm.insurance_co_id = ipm.insurance_co_id and ipm.category_id=icm.category_id) "
          + " LEFT JOIN insurance_company_master icom "
          + " ON (icom.insurance_co_id = icm.insurance_co_id) "
          + " WHERE ipm.plan_code ilike ? AND ipm.sponsor_id IS NOT NULL "
          + " AND ipm.status = 'A' AND ipm.ip_applicable='Y' "
          + " AND ((insurance_validity_start_date <= current_date "
          + " OR insurance_validity_start_date IS NULL)"
          + " AND (current_date <= insurance_validity_end_date "
          + " OR insurance_validity_end_date IS NULL)) LIMIT 10";

  /**
   * Used to get TPA details from Plan Code associated with that Insurance plan. More than one plan
   * can have same Plan Code (Non-Unique).
   *
   * @param planCode - Plan code associated with the Insurance plan
   * @param categoryId - Patient Category ID
   * @param visitType - OP/IP
   * @return list of tpa details as DynaBeans
   */
  public List<BasicDynaBean> getTpaDetailsFromPlanCode(String planCode, Integer categoryId,
      String visitType) {
    String queryPlanCode = "%" + planCode + "%";
    List<BasicDynaBean> tpaList = new ArrayList<>();
    if (org.apache.commons.lang3.StringUtils.equalsIgnoreCase(visitType, "op")) {
      tpaList.addAll(DatabaseHelper.queryToDynaList(GET_TPA_DETAILS_FROM_PLAN_CODE_FOR_OP,
          new Object[] {categoryId, queryPlanCode}));
    }
    if (org.apache.commons.lang3.StringUtils.equalsIgnoreCase(visitType, "ip")) {
      tpaList.addAll(DatabaseHelper.queryToDynaList(GET_TPA_DETAILS_FROM_PLAN_CODE_FOR_IP,
          new Object[] {categoryId, queryPlanCode}));
    }
    return tpaList;
  }

  /** The Constant GET_PLANS_BY_CATEGORYIDS. */
  private static final String GET_PLANS_BY_CATEGORYIDS = "SELECT plan_id, plan_name, "
      + " category_id, insurance_co_id "
      + " FROM insurance_plan_main ipm WHERE ipm.category_id in(:categoryIds) ";

  /**
   * Gets the plans by category ids.
   *
   * @param categoryIds
   *          the category ids
   * @return the plans by category ids
   */
  public List<BasicDynaBean> getPlansByCategoryIds(List<Integer> categoryIds, String sponsorId) {
    return getPlansByCategoryIds(categoryIds, sponsorId, null, null);
  }


  /**
   * Gets plans by category ids.
   *
   * @param categoryIds the category ids
   * @param sponsorId   the sponsor id
   * @param searchQuery the search query
   * @param limit       the limit
   * @return the plans by category ids
   */
  public List<BasicDynaBean> getPlansByCategoryIds(List<Integer> categoryIds, String sponsorId,
      String searchQuery, Integer limit) {
    StringBuilder query = new StringBuilder(GET_PLANS_BY_CATEGORYIDS);
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    if (categoryIds.size() == 0) {
      return new ArrayList<>();
    }
    parameters.addValue("categoryIds", categoryIds);
    if (sponsorId != "" && sponsorId != null) {
      query.append("and (ipm.sponsor_id=:sponsorId OR ipm.sponsor_id='' "
          + " OR ipm.sponsor_id is null)");
      parameters.addValue("sponsorId", sponsorId);
    }

    if (!StringUtils.isEmpty(searchQuery)) {
      query.append(" and lower(plan_name) like :searchQuery");
      searchQuery =
          org.apache.commons.lang3.StringUtils.join(searchQuery.toLowerCase().split("\\s+"), '%')
              + '%';
      parameters.addValue("searchQuery", searchQuery);
    }

    if (limit != null && limit != 0) {
      query.append(" limit :limit");
      parameters.addValue("limit", limit);
    }

    return DatabaseHelper.queryToDynaList(query.toString(), parameters);
  }

  /** The Constant GET_PLAN_TYPE_LIST_BY_SPONSOR. */
  private static final String GET_PLAN_TYPE_LIST_BY_SPONSOR = "SELECT * "
      + " FROM insurance_category_master icm "
      + " LEFT JOIN insurance_category_center_master iccm "
      + "   ON (icm.category_id = iccm.category_id)"
      + " WHERE icm.insurance_co_id IN (SELECT insurance_co_id "
      + "   FROM insurance_company_tpa_master WHERE tpa_id = ?) "
      + " AND EXISTS (SELECT ipm.plan_id from insurance_plan_main ipm"
      + " WHERE icm.category_id=ipm.category_id"
      + " AND (ipm.sponsor_id=? OR ipm.sponsor_id='' OR ipm.sponsor_id is NULL)) ";

  /**
   * Gets the plan type list by sponsor.
   *
   * @param sponsorId
   *          the sponsor id
   * @return the plan type list by sponsor
   */
  public List<BasicDynaBean> getPlanTypeListBySponsor(String sponsorId) {
    return DatabaseHelper.queryToDynaList(GET_PLAN_TYPE_LIST_BY_SPONSOR, sponsorId, sponsorId);
  }

  /**
   * Gets the plan type list by sponsor.
   *
   * @param sponsorId
   *          the sponsor id
   * @return the plan type list by sponsor
   */
  public List<BasicDynaBean> getPlanTypeListBySponsor(String sponsorId,int centerId) {
    return DatabaseHelper.queryToDynaList(GET_PLAN_TYPE_LIST_BY_SPONSOR
        + " AND ( iccm.center_id = ? or iccm.center_id = 0 ) ", sponsorId, sponsorId,centerId);
  }
}
