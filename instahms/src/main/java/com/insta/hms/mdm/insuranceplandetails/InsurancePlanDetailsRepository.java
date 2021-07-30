package com.insta.hms.mdm.insuranceplandetails;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class InsurancePlanDetailsRepository.
 */
@Repository
public class InsurancePlanDetailsRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new insurance plan details repository.
   */
  public InsurancePlanDetailsRepository() {
    super(new String[] { "insurance_category_id", "patient_type", "plan_id" }, null,
        "insurance_plan_details", "insurance_plan_details_id");
  }

  /** The get plan details. */
  private static final String GET_PLAN_DETAILS = "select ipd.plan_id,ipd.insurance_category_id,"
      + " ipd.patient_amount, ipd.patient_percent,ipd.patient_amount_cap,ipd.per_treatment_limit, "
      + " ipd.patient_type,ipd.patient_amount_per_category,ipd.username, iic.system_category, "
      + " iic.insurance_category_name,ipd.category_payable from insurance_plan_details ipd "
      + " LEFT JOIN item_insurance_categories iic "
      + " ON iic.insurance_category_id =ipd.insurance_category_id "
      + " where ipd.plan_id=? and ipd.patient_type=? ORDER BY iic.display_order ";

  /**
   * Gets the mapped plan details.
   *
   * @param object the object
   * @return the mapped plan details
   */
  public List<BasicDynaBean> getMappedPlanDetails(Object[] object) {
    return DatabaseHelper.queryToDynaList(GET_PLAN_DETAILS, object);
  }

  /** The deduction for plan. */
  public static final String DEDUCTION_FOR_PLAN = " SELECT ipd.plan_id, ipd.insurance_category_id,"
      + " ipd.patient_amount, CASE WHEN iic.insurance_payable = 'N' THEN 100"
      + " ELSE ipd.patient_percent END AS patient_percent, patient_amount_cap, "
      + "ipd.per_treatment_limit, ipd.patient_type, ipd.patient_amount_per_category, "
      + " foo.ip_applicable, foo.op_applicable, iic.insurance_category_name, "
      + " iic.insurance_payable, foo.is_copay_pc_on_post_discnt_amt, ipd.category_payable "
      + " FROM insurance_plan_details  ipd "
      + " LEFT JOIN (SELECT ip_applicable, op_applicable, plan_id, "
      + " is_copay_pc_on_post_discnt_amt FROM insurance_plan_main ) AS foo USING (plan_id) "
      + " LEFT JOIN item_insurance_categories iic "
      + " ON iic.insurance_category_id = ipd.insurance_category_id "
      + " WHERE plan_id=? AND ipd.insurance_category_id=? "
      + " AND patient_type=? AND ip_applicable = 'Y' ";

  /**
   * Gets the charge amt for plan.
   *
   * @param object the object
   * @return the charge amt for plan
   */
  public BasicDynaBean getChargeAmtForPlan(Object[] object) {
    return DatabaseHelper.queryToDynaBean(DEDUCTION_FOR_PLAN, object);
  }

  /** The Constant GET_PLAN_CHARGES_WITHOUT_ID. */
  private static final String GET_PLAN_CHARGES_WITHOUT_ID = " SELECT null as plan_id, "
      + " insurance_category_id, insurance_category_name, system_category, "
      + " 0::numeric AS patient_amount, "
      + " 0::numeric as patient_amount_per_category, 0::numeric AS patient_percent,"
      + " null AS patient_amount_cap, null AS per_treatment_limit , "
      + " 'i' AS patient_type,'Unknown' AS  username, current_timestamp AS mod_time,"
      + " 'Y' AS category_payable, null as category_prior_auth_required "
      + " FROM  (SELECT insurance_category_id, insurance_category_name, insurance_payable,"
      + " system_category, priority " + " FROM item_insurance_categories iic "
      + " JOIN insurance_company_category_mapping iccm using(insurance_category_id) "
      + " WHERE insurance_co_id=? AND insurance_payable = 'Y' ) AS foo " + " UNION "
      + " SELECT null as plan_id, insurance_category_id, insurance_category_name, system_category, "
      + " 0::numeric AS patient_amount, 0::numeric as patient_amount_per_category, "
      + " 0::numeric AS patient_percent,null AS patient_amount_cap, null AS per_treatment_limit ,"
      + " 'o' AS patient_type, 'Unknown' AS username, current_timestamp AS mod_time, "
      + " 'Y' AS category_payable, null as category_prior_auth_required "
      + " FROM  (SELECT insurance_category_id, insurance_category_name, insurance_payable,"
      + " system_category, priority " + " FROM item_insurance_categories iic "
      + " JOIN insurance_company_category_mapping iccm using(insurance_category_id) "
      + " WHERE insurance_co_id=? AND insurance_payable = 'Y') AS foo " + " UNION "
      + " SELECT null as plan_id, insurance_category_id, insurance_category_name, system_category, "
      + " 0::numeric AS patient_amount,0::numeric as patient_amount_per_category, "
      + " 0::numeric AS patient_percent, null AS patient_amount_cap, null AS per_treatment_limit , "
      + "'i' AS patient_type,'Unknown' AS  username, "
      + " current_timestamp AS mod_time,'N' AS category_payable, "
      + " null as category_prior_auth_required "
      + " FROM  (SELECT insurance_category_id, insurance_category_name, insurance_payable,"
      + " system_category, priority " + " FROM item_insurance_categories iic "
      + " JOIN insurance_company_category_mapping iccm using(insurance_category_id) "
      + " WHERE insurance_co_id=? AND insurance_payable = 'N') AS foo  " + " UNION "
      + " SELECT null as plan_id, insurance_category_id, insurance_category_name, system_category, "
      + " 0::numeric AS patient_amount, 0::numeric as patient_amount_per_category, "
      + " 0::numeric AS patient_percent,null AS patient_amount_cap, null AS per_treatment_limit ,"
      + " 'o' AS patient_type, 'Unknown' AS username, current_timestamp AS mod_time,"
      + " 'N' AS category_payable, null as category_prior_auth_required "
      + " FROM  (SELECT insurance_category_id, insurance_category_name, insurance_payable,"
      + " system_category, priority " + " FROM item_insurance_categories iic "
      + " JOIN insurance_company_category_mapping iccm using(insurance_category_id) "
      + " WHERE insurance_co_id=? AND insurance_payable = 'N')"
      + "  AS foo ORDER BY insurance_category_name ";

  /**
   * Gets the all plan charges.
   *
   * @return the all plan charges
   */
  public List<BasicDynaBean> getAllPlanCharges(String insuranceCompId) {
    return DatabaseHelper.queryToDynaList(GET_PLAN_CHARGES_WITHOUT_ID,
        new Object[] { insuranceCompId, insuranceCompId, insuranceCompId, insuranceCompId });
  }

  /** The Constant GET_PLAN_CHARGES_FOR_PLAN_ID. */
  private static final String GET_PLAN_CHARGES_FOR_PLAN_ID = " SELECT  plan_id, "
      + " ipd.insurance_category_id, insurance_category_name, patient_type, "
      + " patient_amount, patient_amount_per_category, patient_percent, system_category, "
      + " patient_amount_cap, per_treatment_limit , ch.insurance_payable , "
      + " foo.ip_applicable, foo.op_applicable, foo.is_copay_pc_on_post_discnt_amt,"
      + " ipd.category_payable, ipd.category_prior_auth_required,ipd.insurance_plan_details_id "
      + " FROM  insurance_plan_details ipd"
      + " JOIN item_insurance_categories ch on ipd.insurance_category_id=ch.insurance_category_id "
      + " LEFT JOIN ( SELECT ip_applicable, op_applicable, plan_id, is_copay_pc_on_post_discnt_amt "
      + " FROM insurance_plan_main) AS foo USING (plan_id)  " + " WHERE plan_id=? ";

  /**
   * Gets the all plan charges.
   *
   * @param planId the plan id
   * @return the all plan charges
   */
  public List<BasicDynaBean> getAllPlanCharges(int planId) {
    return DatabaseHelper.queryToDynaList(GET_PLAN_CHARGES_FOR_PLAN_ID
        + "ORDER BY system_category DESC, insurance_category_name", new Object[] { planId });
  }
}
