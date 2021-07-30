package com.insta.hms.api.repositories;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ApiInsurancePlanRepository extends MasterRepository<String> {

  public ApiInsurancePlanRepository() {
    super("insurance_plan_main", "plan_id");
  }

  private static final String INSURANCE_PLAN_LOOKUP_QUERY =
      "SELECT * "
          + "FROM ( SELECT ipm.plan_id AS insurance_plan_id,icm.insurance_co_id,"
          + "icm.insurance_co_name,ipm.plan_name AS insurance_plan_name,"
          + "ipm.plan_code AS insurance_plan_code,ipm.interface_code AS insurance_interface_code,"
          + "ipm.default_rate_plan AS default_rate_plan_code, ipm.ip_applicable,ipm.op_applicable,"
          + "ipm.status,ipm.insurance_validity_start_date AS valid_from,"
          + "ipm.insurance_validity_end_date AS valid_to,ipm.sponsor_id,ipm.category_id "
          + "FROM insurance_plan_main ipm "
          + "JOIN insurance_company_master icm ON (ipm.insurance_co_id = icm.insurance_co_id) "
          + "WHERE icm.status = 'A' ORDER BY insurance_plan_name ) AS foo";

  @Override
  public String getLookupQuery() {
    return INSURANCE_PLAN_LOOKUP_QUERY;
  }

  /**
   * Get Insurance plan details using planId.
   *
   * @param planId  the plan id
   * @return the BasicDynaBean
   */
  public BasicDynaBean getInsurancePlanByPlanId(Integer planId) {
    return DatabaseHelper.queryToDynaBean(
        INSURANCE_PLAN_LOOKUP_QUERY + " WHERE insurance_plan_id = " + planId);
  }

  /**
   * Get Insurance plan details using planCode.
   *
   * @param planCode  the plan code
   * @return the BasicDynaBean
   */
  public BasicDynaBean getInsurancePlanByPlanCode(String planCode) {
    return DatabaseHelper.queryToDynaBean(
        INSURANCE_PLAN_LOOKUP_QUERY + " WHERE insurance_plan_code = ?", planCode);
  }

  private static final String GET_SPONSOR_DETAILS =
      "SELECT ipm.plan_id AS insurance_plan_id, tm.tpa_id AS sponsor_id,"
          + "tm.tpa_name AS sponsor_name FROM insurance_plan_main ipm "
          + "JOIN insurance_company_tpa_master ictm "
          + "ON (ipm.insurance_co_id = ictm.insurance_co_id) "
          + "JOIN tpa_master tm ON (ictm.tpa_id = tm.tpa_id) WHERE tm.status ='A' ";

  /**
   * Get Sponsor details from insurance_company_tpa_master  master.
   *
   * @param planIds  the plan id
   * @return the list
   */
  public List<BasicDynaBean> getSponsorDetails(String planIds) {
    String query = GET_SPONSOR_DETAILS + " AND ipm.plan_id IN (" + planIds + ")";
    return DatabaseHelper.queryToDynaList(query);
  }

  private static final String GET_SPONSOR_DETAILS_FROM_TPA_MASTER =
      "SELECT ipm.plan_id AS insurance_plan_id,"
          + "tm.tpa_id AS sponsor_id ,tm.tpa_name AS sponsor_name "
          + "FROM insurance_plan_main ipm "
          + "JOIN tpa_master tm ON (tm.tpa_id = ipm.sponsor_id) WHERE tm.status ='A' ";

  /**
   * Get Sponsor detail from tpa master.
   *
   * @param planIds  the plan id
   * @return the list
   */
  public List<BasicDynaBean> getSponsorDetailsFromTpaMaster(String planIds) {
    String query = GET_SPONSOR_DETAILS_FROM_TPA_MASTER
        + "AND ipm.plan_id IN (" + planIds + ")";
    return DatabaseHelper.queryToDynaList(query);
  }

  private static final String GET_PLAN_TYPE_DETAILS =
      "SELECT ipm.plan_id AS insurance_plan_id, icm.category_id AS plan_type_id,"
          + "icm.category_name AS plan_type_name "
          + "FROM insurance_plan_main ipm  "
          + "JOIN insurance_category_master icm ON (icm.insurance_co_id = ipm.insurance_co_id) "
          + "WHERE icm.status ='A'";

  /**
   * Get plan type details from  join using insurance_co_id.
   *
   * @param planIds  the plan id
   * @return the list
   */
  public List<BasicDynaBean> getPlanTypeDetails(String planIds) {
    String query = GET_PLAN_TYPE_DETAILS + " AND ipm.plan_id IN (" + planIds + ")";
    return DatabaseHelper.queryToDynaList(query);
  }

  private static final String GET_PLAN_TYPE_DETAILS_JOIN_USING_CATEGORY_ID =
      "SELECT ipm.plan_id AS insurance_plan_id, icm.category_id AS plan_type_id,"
          + "icm.category_name AS plan_type_name "
          + "FROM insurance_plan_main ipm "
          + "JOIN insurance_category_master icm ON (icm.category_id = ipm.category_id) "
          + "WHERE icm.status ='A' ";

  /**
   * Get plan type details from join using category_id.
   *
   * @param planIds  the plan id
   * @return the list
   */
  public List<BasicDynaBean> getPlanTypeDetailsJoinUsingCategoryId(String planIds) {
    String query = GET_PLAN_TYPE_DETAILS_JOIN_USING_CATEGORY_ID
        + "AND ipm.plan_id IN (" + planIds + ")";
    return DatabaseHelper.queryToDynaList(query);
  }
}
