package com.insta.hms.mdm.patientcategories;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

// TODO: Auto-generated Javadoc
/** The Class PatientCategoryRepository. */
@Repository
public class PatientCategoryRepository extends MasterRepository<Integer> {
  /** Instantiates a new patient category repository. */
  public PatientCategoryRepository() {
    super("patient_category_master", "category_id");
  }

  /**
   * List by center.
   *
   * @param centerIds the center ids
   * @return the list
   */
  public List<BasicDynaBean> listByCenter(List<Integer> centerIds) {
    StringBuilder query =
        new StringBuilder("SELECT * from patient_category_master WHERE status='A' ");
    DataBaseUtil.addWhereFieldInList(query, "center_id", centerIds, true);
    query.append("ORDER BY category_name");
    return DatabaseHelper.queryToDynaList(query.toString(), centerIds.toArray());
  }

  /** The get default rate plan. */
  private static final String GET_DEFAULT_RATE_PLAN =
      " SELECT org_name, org_id from " + " organization_details od ";

  /** The filter condition. */
  private static final String FILTER_CONDITION =
      " WHERE od.status='A' AND ( "
          + "(od.has_date_validity AND current_date "
          + "BETWEEN od.valid_from_date AND od.valid_to_date ) "
          + "OR (NOT od.has_date_validity)) ";

  /** The center applicability. */
  private static final String CENTER_APPLICABILITY = " AND foo.center_id in (0,?) ";

  /** The order by. */
  private static final String ORDER_BY = " ORDER BY od.org_name ";

  /** The join patient category. */
  private static final String JOIN_PATIENT_CATEGORY =
      " join "
          + "(select regexp_split_to_table(op_allowed_rate_plans, E',') as rate_plan_id, center_id "
          + "from patient_category_master where category_id= ? ) as foo "
          + "ON(foo.rate_plan_id = od.org_id OR foo.rate_plan_id = '*') ";

  /**
   * Gets the pat category default rate plan.
   *
   * @param categoryId the category id
   * @param centerId the center id
   * @return the pat category default rate plan
   */
  public List<BasicDynaBean> getPatCategoryDefaultRatePlan(String categoryId, Integer centerId) {
    if (null != categoryId && !categoryId.equals("")) {
      return DatabaseHelper.queryToDynaList(
          GET_DEFAULT_RATE_PLAN
              + JOIN_PATIENT_CATEGORY
              + FILTER_CONDITION
              + CENTER_APPLICABILITY
              + ORDER_BY,
          new Object[] {Integer.parseInt(categoryId), centerId});
    } else {
      return DatabaseHelper.queryToDynaList(GET_DEFAULT_RATE_PLAN + FILTER_CONDITION + ORDER_BY);
    }
  }

  /** The Constant GET_CATEGORY_MASTER_INC_SUPER_DENTER. */
  private static final String GET_CATEGORY_MASTER_INC_SUPER_DENTER =
      "SELECT * from "
          + "patient_category_master WHERE status='A' "
          + "AND center_id in (?,0) order by category_name";
  
  /** The Constant GET_CATEGORY_MASTER. */
  private static final String GET_CATEGORY_MASTER = 
      "SELECT * from "
          + "patient_category_master WHERE status='A' "
          + " order by category_name" ;

  /**
   * Gets the all categories inc super center.
   *
   * @param centerId the center id
   * @return the all categories inc super center
   */
  public List<BasicDynaBean> getAllCategoriesIncSuperCenter(int centerId) {

    if (centerId == 0) {
      return DatabaseHelper.queryToDynaList(GET_CATEGORY_MASTER);      
    } else {
      return DatabaseHelper.queryToDynaList(
          GET_CATEGORY_MASTER_INC_SUPER_DENTER, new Object[] {centerId});
    }
  }

  /** The Constant IP_ALLOWED_INSURANCE_COMPANIES_QUERY. */
  private static final String IP_ALLOWED_INSURANCE_COMPANIES_QUERY =
      "SELECT ic.insurance_co_id, "
          + " ic.insurance_co_name, ic.status, ic.insurance_rules_doc_name "
          + " FROM insurance_company_master ic "
          + " JOIN (select * from patient_category_master where category_id=?) p "
          + " ON p.ip_allowed_insurance_co_ids='*' "
          + " OR p.ip_allowed_insurance_co_ids LIKE ic.insurance_co_id "
          + " OR p.ip_allowed_insurance_co_ids LIKE ic.insurance_co_id || ',%' "
          + " OR p.ip_allowed_insurance_co_ids LIKE '%,' || ic.insurance_co_id || ',%'  "
          + " OR p.ip_allowed_insurance_co_ids LIKE '%,' || ic.insurance_co_id WHERE ic.status='A'";

  /** The Constant OP_ALLOWED_INSURANCE_COMPANIES_QUERY. */
  private static final String OP_ALLOWED_INSURANCE_COMPANIES_QUERY =
      "SELECT "
          + " ic.insurance_co_id, ic.insurance_co_name, "
          + " ic.status, ic.insurance_rules_doc_name "
          + " FROM insurance_company_master ic "
          + " JOIN (select * from patient_category_master where category_id=?) p "
          + " ON p.op_allowed_insurance_co_ids='*' "
          + " OR p.op_allowed_insurance_co_ids LIKE ic.insurance_co_id "
          + " OR p.op_allowed_insurance_co_ids LIKE ic.insurance_co_id || ',%' "
          + " OR p.op_allowed_insurance_co_ids LIKE "
          + " '%,' || ic.insurance_co_id || ',%'  "
          + " OR p.op_allowed_insurance_co_ids LIKE '%,' || ic.insurance_co_id "
          + " WHERE ic.status='A' ";

  /**
   * Get allowed insurance companies.
   *
   * @param categoryId the category id
   * @param visitType the visit type
   * @return the allowed ins companies
   */
  public List<BasicDynaBean> getAllowedInsCompanies(int categoryId, String visitType) {
    if (visitType.equals("i")) {
      return DatabaseHelper.queryToDynaList(
          IP_ALLOWED_INSURANCE_COMPANIES_QUERY, new Object[] {categoryId});
    } else {
      return DatabaseHelper.queryToDynaList(
          OP_ALLOWED_INSURANCE_COMPANIES_QUERY, new Object[] {categoryId});
    }
  }

  /** The Constant IP_ALLOWED_RATE_PLANS_QUERY. */
  private static final String IP_ALLOWED_RATE_PLANS_QUERY =
      "SELECT  o.org_id, o.org_name "
          + "FROM organization_details o  "
          + "JOIN (select * from patient_category_master where category_id=?) p  "
          + "ON (p.ip_allowed_rate_plans='*') OR p.ip_allowed_rate_plans LIKE o.org_id  "
          + "OR p.ip_allowed_rate_plans LIKE o.org_id || ',%'  "
          + "OR p.ip_allowed_rate_plans LIKE '%,' || o.org_id || ',%' "
          + "OR p.ip_allowed_rate_plans LIKE '%,' || o.org_id  "
          + "WHERE o.status = 'A' AND ( (o.has_date_validity "
          + "AND current_date BETWEEN o.valid_from_date AND o.valid_to_date ) "
          + "OR (NOT o.has_date_validity));";

  /** The Constant OP_ALLOWED_RATE_PLANS_QUERY. */
  private static final String OP_ALLOWED_RATE_PLANS_QUERY =
      "SELECT  o.org_id, o.org_name "
          + "FROM organization_details o  "
          + "JOIN (select * from patient_category_master where category_id=?) p  "
          + "ON (p.op_allowed_rate_plans='*') OR p.op_allowed_rate_plans LIKE o.org_id  "
          + "OR p.op_allowed_rate_plans LIKE o.org_id || ',%'  "
          + "OR p.op_allowed_rate_plans LIKE '%,' || o.org_id || ',%' "
          + "OR p.op_allowed_rate_plans LIKE '%,' || o.org_id  "
          + "WHERE o.status = 'A' AND ( (o.has_date_validity "
          + "AND current_date BETWEEN o.valid_from_date AND o.valid_to_date ) "
          + "OR (NOT o.has_date_validity));";

  /**
   * Gets the allowed rate plans.
   *
   * @param patientCategoryId the patient category id
   * @param visitType the visit type
   * @return the allowed rate plans
   */
  public List<BasicDynaBean> getAllowedRatePlans(int patientCategoryId, String visitType) {
    if (visitType.equals("i")) {
      return DatabaseHelper.queryToDynaList(
          IP_ALLOWED_RATE_PLANS_QUERY, new Object[] {patientCategoryId});
    } else {
      return DatabaseHelper.queryToDynaList(
          OP_ALLOWED_RATE_PLANS_QUERY, new Object[] {patientCategoryId});
    }
  }
  
  private static final String IP_DEFAULT_RATE_PLAN = " SELECT org.org_id, org.org_name "
      + "FROM organization_details org WHERE org_id IN " + "(SELECT ip_rate_plan_id "
      + "FROM patient_category_master WHERE category_id=?) and status='A'";

  private static final String OP_DEFAULT_RATE_PLAN = " SELECT org.org_id, org.org_name "
      + " FROM organization_details org WHERE org_id IN " + "(SELECT op_rate_plan_id "
      + " FROM patient_category_master WHERE category_id=?) and status='A' ";

  /**
   * Gets the default rate plan.
   *
   * @param categoryId the category id
   * @param visitType the visit type
   * @return the default rate plan
   */
  public List<BasicDynaBean> getDefaultRatePlan(int categoryId, String visitType) {
    if (visitType.equals("i")) {
      return DatabaseHelper.queryToDynaList(IP_DEFAULT_RATE_PLAN, new Object[] { categoryId });
    } else {
      return DatabaseHelper.queryToDynaList(OP_DEFAULT_RATE_PLAN, new Object[] { categoryId });
    }
  }
}
