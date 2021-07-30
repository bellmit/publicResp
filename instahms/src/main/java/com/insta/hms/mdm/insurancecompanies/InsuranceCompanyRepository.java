package com.insta.hms.mdm.insurancecompanies;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

// TODO: Auto-generated Javadoc
/** The Class InsuranceCompanyRepository. */
@Repository
public class InsuranceCompanyRepository extends MasterRepository<String> {

  /** Instantiates a new insurance company repository. */
  public InsuranceCompanyRepository() {
    super("insurance_company_master", "insurance_co_id", "insurance_co_name");
  }

  /** The get mapped insurance companies. */
  private static String GET_MAPPED_INSURANCE_COMPANIES =
      "select * from ( select distinct icm.insurance_co_id,icm.insurance_co_name "
          + "from insurance_company_master icm "
          + "LEFT JOIN insurance_company_tpa_master ictm ON "
          + "ictm.insurance_co_id = icm.insurance_co_id "
          + "where icm.status = 'A' AND case when ( (select count(tpa_id) as count "
          + "from insurance_company_tpa_master ictm_t "
          + "JOIN insurance_company_master icm_t ON "
          + "(ictm_t.insurance_co_id = icm_t.insurance_co_id) where "
          + "ictm_t.tpa_id= ? and icm_t.status = 'A' limit 1) = 0  ) "
          + "then true else ictm.tpa_id= ? end ) as foo ";

  /** The join patient category mapped. */
  private static String JOIN_PATIENT_CATEGORY_MAPPED =
      "join (select regexp_split_to_table(op_allowed_insurance_co_ids, E',') "
          + "as insurance_co_id_split "
          + "from patient_category_master where category_id= ? ) as "
          + "foo1 ON(foo1.insurance_co_id_split = foo.insurance_co_id "
          + "OR foo1.insurance_co_id_split = '*') ";

  /** The order by insurance companies. */
  private static String ORDER_BY_INSURANCE_COMPANIES = "order by insurance_co_name ";

  /**
   * Gets the mapped insurance companies.
   *
   * @param object the object
   * @return the mapped insurance companies
   */
  public List<BasicDynaBean> getMappedInsuranceCompanies(Object[] object) {
    return DatabaseHelper.queryToDynaList(GET_MAPPED_INSURANCE_COMPANIES, object);
  }

  /**
   * Gets the mapped insurance companies.
   *
   * @param tpaId the tpa id
   * @param categoryId the category id
   * @return the mapped insurance companies
   */
  public List<BasicDynaBean> getMappedInsuranceCompanies(String tpaId, String categoryId) {
    if (null != categoryId && !categoryId.equals("")) {
      return DatabaseHelper.queryToDynaList(
          GET_MAPPED_INSURANCE_COMPANIES
              + JOIN_PATIENT_CATEGORY_MAPPED
              + ORDER_BY_INSURANCE_COMPANIES,
          new Object[] {tpaId, tpaId, Integer.parseInt(categoryId)});
    } else {
      return DatabaseHelper.queryToDynaList(
          GET_MAPPED_INSURANCE_COMPANIES + ORDER_BY_INSURANCE_COMPANIES,
          new Object[] {tpaId, tpaId});
    }
  }

  /** The get default rate plan. */
  private static String GET_DEFAULT_RATE_PLAN =
      "select org_name,org_id from organization_details od "
          + "join insurance_company_master icm on icm.default_rate_plan = od.org_id ";

  /** The filter condition. */
  private static String FILTER_CONDITION = "where icm.insurance_co_id= ? ";

  /** The join patient category. */
  private static String JOIN_PATIENT_CATEGORY =
      "join (select regexp_split_to_table(op_allowed_insurance_co_ids, E',') "
          + "as insurance_co_id_split "
          + "from patient_category_master where category_id= ? ) as foo "
          + "ON(foo.insurance_co_id_split = icm.insurance_co_id "
          + "OR foo.insurance_co_id_split = '*') ";

  /**
   * Gets the insurance comp default rate plan.
   *
   * @param insCompId the ins comp id
   * @param categoryId the category id
   * @return the insurance comp default rate plan
   */
  public List<BasicDynaBean> getInsuranceCompDefaultRatePlan(String insCompId, String categoryId) {
    if (null != categoryId && !categoryId.equals("")) {
      return DatabaseHelper.queryToDynaList(
          GET_DEFAULT_RATE_PLAN + JOIN_PATIENT_CATEGORY + FILTER_CONDITION,
          new Object[] {Integer.parseInt(categoryId), insCompId});
    } else {
      return DatabaseHelper.queryToDynaList(
          GET_DEFAULT_RATE_PLAN + FILTER_CONDITION, new Object[] {insCompId});
    }
  }

  /** The get insurance company code. */
  private static String GET_INSURANCE_COMPANY_CODE =
      "SELECT hic.insurance_co_code, icm.insurance_co_name "
          + "FROM insurance_company_master icm "
          + "LEFT JOIN ha_ins_company_code hic ON(hic.insurance_co_id=icm.insurance_co_id "
          + "AND health_authority = ?) WHERE icm.insurance_co_id=? ";

  /**
   * Gets the insurance company code.
   *
   * @param healthAuthority the health authority
   * @param insuranceCoId the insurance co id
   * @return the insurance company code
   */
  public BasicDynaBean getInsuranceCompanyCode(String healthAuthority, String insuranceCoId) {
    return DatabaseHelper.queryToDynaBean(
        GET_INSURANCE_COMPANY_CODE, new Object[] {healthAuthority, insuranceCoId});
  }

  /** The Constant GET_IP_CATEGORY_COMPANY_TPA_LIST. */
  private static final String GET_IP_CATEGORY_COMPANY_TPA_LIST =
      "SELECT ictm.insurance_co_id, ictm.tpa_id, "
          + " icm.insurance_co_name, tm.tpa_name, icm.status "
          + " AS ins_co_status, tm.status AS tpa_status"
          + " FROM insurance_company_tpa_master ictm "
          + " JOIN (SELECT regexp_split_to_table(foo.arp, E',') AS insurance_co_id "
          + " FROM ( "
          + " SELECT  "
          + "   CASE  "
          + "     WHEN ip_allowed_insurance_co_ids ='*' "
          + "     THEN ( "
          + "       SELECT array_to_string ( "
          + "         ARRAY(  "
          + "           SELECT insurance_co_id::text FROM insurance_company_master "
          + "           WHERE status='A'  "
          + "         ),  "
          + "       ',') "
          + "          ) "
          + "     ELSE ip_allowed_insurance_co_ids  "
          + "   END AS arp  "
          + "   FROM patient_category_master  "
          + "   WHERE category_id = ? ) AS foo) AS catins "
          + " ON (catins.insurance_co_id = ictm.insurance_co_id)"
          + " LEFT JOIN tpa_master tm ON (tm.tpa_id = ictm.tpa_id) "
          + " LEFT JOIN insurance_company_master icm "
          + " ON (icm.insurance_co_id = ictm.insurance_co_id) "
          + " WHERE icm.status = 'A'  AND tm.status='A' "
          + " ORDER BY icm.insurance_co_name, tm.tpa_name ";

  /** The Constant GET_OP_CATEGORY_COMPANY_TPA_LIST. */
  private static final String GET_OP_CATEGORY_COMPANY_TPA_LIST =
      "SELECT ictm.insurance_co_id, ictm.tpa_id, "
          + " icm.insurance_co_name, tm.tpa_name, icm.status "
          + " AS ins_co_status, tm.status AS tpa_status"
          + " FROM insurance_company_tpa_master ictm "
          + " JOIN (SELECT regexp_split_to_table(foo.arp, E',') AS insurance_co_id "
          + " FROM ( "
          + " SELECT  "
          + "   CASE  "
          + "     WHEN op_allowed_insurance_co_ids ='*' "
          + "     THEN ( "
          + "       SELECT array_to_string ( "
          + "         ARRAY(  "
          + "           SELECT insurance_co_id::text FROM insurance_company_master "
          + "           WHERE status='A'  "
          + "         ),  "
          + "       ',') "
          + "          ) "
          + "     ELSE op_allowed_insurance_co_ids  "
          + "   END AS arp  "
          + "   FROM patient_category_master  "
          + "   WHERE category_id = ? ) AS foo) AS catins "
          + " ON (catins.insurance_co_id = ictm.insurance_co_id)"
          + " LEFT JOIN tpa_master tm ON (tm.tpa_id = ictm.tpa_id) "
          + " LEFT JOIN insurance_company_master icm "
          + " ON (icm.insurance_co_id = ictm.insurance_co_id) "
          + " WHERE icm.status = 'A' AND tm.status='A' "
          + " ORDER BY icm.insurance_co_name, tm.tpa_name ";

  /**
   * Gets the category mapped tpa list.
   *
   * @param patientCategoryId the patient category id
   * @param visitType the visit type
   * @return the category mapped tpa list
   */
  public List<BasicDynaBean> getCategoryMappedTpaList(int patientCategoryId, String visitType) {

    if (visitType.equals("i")) {
      return DatabaseHelper.queryToDynaList(
          GET_IP_CATEGORY_COMPANY_TPA_LIST, new Object[] {patientCategoryId});
    } else {
      return DatabaseHelper.queryToDynaList(
          GET_OP_CATEGORY_COMPANY_TPA_LIST, new Object[] {patientCategoryId});
    }
  }

  /** The Constant GET_COMPANY_TPA_LIST. */
  private static final String GET_COMPANY_TPA_LIST = "SELECT ictm.insurance_co_id, ictm.tpa_id, "
      + " icm.insurance_co_name, tm.tpa_name, icm.status "
      + " AS ins_co_status, tm.status AS tpa_status" + " FROM insurance_company_tpa_master ictm "
      + " LEFT JOIN tpa_master tm ON (tm.tpa_id = ictm.tpa_id) "
      + " LEFT JOIN tpa_center_master tcm "
      + " ON (tcm.tpa_id = tm.tpa_id AND (tcm.center_id = ? OR tcm.center_id = -1))"
      + " LEFT JOIN insurance_company_master  icm "
      + " ON (icm.insurance_co_id = ictm.insurance_co_id) "
      + " WHERE icm.status = 'A'  AND tm.status='A' AND tcm.status = 'A' "
      + " ORDER BY icm.insurance_co_name, tm.tpa_name ";

  /**
   * Gets the company tpa list.
   *
   * @return the company tpa list
   */
  public List<BasicDynaBean> getCompanyTpaList(Integer centerId) {
    return DatabaseHelper.queryToDynaList(GET_COMPANY_TPA_LIST, new Object[] { centerId });
  }

  private static final String GET_ACTIVE_INSURANCE_COMPANY_LIST = "SELECT insurance_co_id,"
      + " insurance_co_name from insurance_company_master where status = 'A'";

  /**
   * Get insurance company list.
   *
   * @return list of insurance companies
   */
  public List<BasicDynaBean> getInsuranceCompanyList() {
    return DatabaseHelper.queryToDynaList(GET_ACTIVE_INSURANCE_COMPANY_LIST);
  }
}
