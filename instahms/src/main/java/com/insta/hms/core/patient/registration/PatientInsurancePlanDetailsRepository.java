package com.insta.hms.core.patient.registration;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class PatientInsurancePlanDetailsRepository.
 */
@Repository
public class PatientInsurancePlanDetailsRepository extends GenericRepository {

  /**
   * Instantiates a new patient insurance plan details repository.
   */
  public PatientInsurancePlanDetailsRepository() {
    super("patient_insurance_plan_details");
  }

  /** The Constant GET_VISIT_INS_DETAILS. */
  public static final String GET_VISIT_INS_DETAILS = "SELECT pipd.visit_id, pipd.plan_id, "
      + "pipd.insurance_category_id, "
      + "pipd.patient_amount, pipd.patient_percent, pipd.patient_amount_cap, "
      + "pipd.per_treatment_limit, pipd.patient_type, pipd.patient_amount_per_category, "
      + "ipm.is_copay_pc_on_post_discnt_amt,  pip.priority, CASE WHEN iic.insurance_payable='Y' "
      + "THEN true ELSE false END AS is_category_payable, pip.plan_limit, "
      + "CASE WHEN (ipm.limits_include_followup='Y' AND pr.visit_type='o') "
      + "THEN pip.episode_limit ELSE pip.visit_limit END AS visit_limit , "
      + "CASE WHEN (ipm.limits_include_followup='Y' AND pr.visit_type='o') "
      + "THEN pip.episode_deductible ELSE pip.visit_deductible END AS visit_deductible , "
      + "CASE WHEN (ipm.limits_include_followup='Y' AND pr.visit_type='o') "
      + "THEN pip.episode_copay_percentage ELSE pip.visit_copay_percentage "
      + "END AS visit_copay_percentage , CASE WHEN (ipm.limits_include_followup='Y' "
      + "AND pr.visit_type='o') THEN pip.episode_max_copay_percentage "
      + "ELSE pip.visit_max_copay_percentage END AS visit_max_copay_percentage , "
      + "pip.visit_per_day_limit, ipm.limits_include_followup, pr.reg_date, pr.visit_type, "
      + "pr.discharge_date, COALESCE(ipd.category_payable, iic.insurance_payable) "
      + "as plan_category_payable, ipm.limit_type, ipm.op_pre_authorized_amount, "
      + "ipm.enable_pre_authorized_limit, ipm.excluded_charge_groups "
      + "FROM patient_insurance_plan_details pipd "
      + "JOIN insurance_plan_main ipm ON(ipm.plan_id = pipd.plan_id) "
      + "JOIN patient_insurance_plans pip ON(pip.patient_id = pipd.visit_id "
      + "and pip.plan_id = pipd.plan_id) "
      + "JOIN patient_registration pr ON(pip.patient_id = pr.patient_id) "
      + "LEFT JOIN insurance_plan_details ipd ON(ipd.plan_id = pipd.plan_id "
      + "and ipd.insurance_category_id = pipd.insurance_category_id "
      + "AND ipd.patient_type = pipd.patient_type)  " + "JOIN item_insurance_categories iic "
      + "ON(iic.insurance_category_id = pipd.insurance_category_id) "
      + "WHERE pipd.visit_id=? ORDER BY pip.priority ";

  /**
   * Gets the visit ins details.
   *
   * @param object
   *          the object
   * @return the visit ins details
   */
  public List<BasicDynaBean> getVisitInsDetails(Object[] object) {
    return DatabaseHelper.queryToDynaList(GET_VISIT_INS_DETAILS, object);
  }

  /** The Constant GET_INSURANCE_PLAN_MASTER_DETAILS_FOR_VISIT. */
  private static final String GET_INSURANCE_PLAN_MASTER_DETAILS_FOR_VISIT = 
      " SELECT  pipd.plan_id, pipd.insurance_category_id, pipd.patient_amount, "
      + "pipd.patient_percent,  pipd.patient_amount_cap,  pipd.per_treatment_limit, "
      + "pipd.patient_type, pipd.patient_amount_per_category, "
      + "ipm.is_copay_pc_on_post_discnt_amt, "
      + "pip.priority, CASE WHEN iic.insurance_payable='Y' THEN true ELSE false "
      + "END AS is_category_payable, pip.plan_limit, "
      + "CASE WHEN (ipm.limits_include_followup='Y' AND pr.visit_type='o') "
      + "THEN  pip.episode_limit ELSE pip.visit_limit END AS visit_limit , "
      + "CASE WHEN (ipm.limits_include_followup='Y' AND pr.visit_type='o') "
      + "THEN  pip.episode_deductible ELSE pip.visit_deductible END AS visit_deductible, "
      + "CASE WHEN (ipm.limits_include_followup='Y' AND pr.visit_type='o') "
      + "THEN  pip.episode_copay_percentage ELSE pip.visit_copay_percentage "
      + "END AS visit_copay_percentage ,"
      + "CASE WHEN (ipm.limits_include_followup='Y' AND pr.visit_type='o') "
      + "THEN  pip.episode_max_copay_percentage ELSE pip.visit_max_copay_percentage "
      + "END AS visit_max_copay_percentage, pip.visit_per_day_limit, "
      + "ipm.limits_include_followup, "
      + "pr.reg_date, pr.visit_type, pr.discharge_date, "
      + "pipd.category_payable as plan_category_payable, "
      + "ipm.limit_type, ipm.op_pre_authorized_amount, ipm.enable_pre_authorized_limit, "
      + "ipm.excluded_charge_groups FROM insurance_plan_details pipd "
      + "JOIN insurance_plan_main ipm ON(ipm.plan_id = pipd.plan_id) "
      + "JOIN patient_insurance_plans pip ON(pip.patient_id = ? and pip.plan_id = pipd.plan_id) "
      + "JOIN patient_registration pr ON(pip.patient_id = pr.patient_id) "
      + "JOIN item_insurance_categories iic "
      + "ON(iic.insurance_category_id = pipd.insurance_category_id) "
      + "WHERE pipd.patient_type = ? ORDER BY pip.priority ";

  /**
   * Gets the insurance details from master.
   *
   * @param objects
   *          the objects
   * @return the insurance details from master
   */
  public List<BasicDynaBean> getInsuranceDetailsFromMaster(Object[] objects) {
    return DatabaseHelper.queryToDynaList(GET_INSURANCE_PLAN_MASTER_DETAILS_FOR_VISIT, objects);
  }

  /** The Constant GET_VISIT_PLAN_DETAILS. */
  public static final String GET_VISIT_PLAN_DETAILS = "select pd.*,"
      + " COALESCE(im.category_payable, iic.insurance_payable) as category_payable,"
      + " iic.insurance_category_name, iic.system_category "
      + " from patient_insurance_plan_details pd " + " left join insurance_plan_details im"
      + " on (im.plan_id =pd.plan_id and im.insurance_category_id = pd.insurance_category_id"
      + " AND pd.patient_type = im.patient_type) " + " join item_insurance_categories iic"
      + " on (iic.insurance_category_id = pd.insurance_category_id) "
      + " where pd.visit_id = ? and pd.plan_id=? and pd.patient_type=? "
      + " ORDER BY  iic.display_order ";

  /**
   * Gets the plan details.
   *
   * @param planId
   *          the plan id
   * @param visitType
   *          the visit type
   * @param visitId
   *          the visit id
   * @return the plan details
   */
  public List<BasicDynaBean> getPlanDetails(int planId, String visitType, String visitId) {
    return DatabaseHelper.queryToDynaList(GET_VISIT_PLAN_DETAILS, new Object[] { visitId, planId,
        visitType });
  }

  /** The Constant GET_PREVIOUS_VISIT_PLAN_DETAILS. */
  public static final String GET_PREVIOUS_VISIT_PLAN_DETAILS = "SELECT "
       + " pd.visit_id,COALESCE(pd.plan_id,im.plan_id)as plan_id, "
       + " COALESCE(pd.insurance_category_id,im.insurance_category_id) as insurance_category_id,"
       + " COALESCE(pd.patient_amount,im.patient_amount)  AS patient_amount, "
       + " COALESCE(pd.patient_percent,im.patient_percent) AS patient_percent, "
       + " COALESCE(pd.patient_amount_cap,im.patient_amount_cap) AS patient_amount_cap, "
       + " COALESCE(pd.per_treatment_limit,im.per_treatment_limit) AS per_treatment_limit, "
       + " COALESCE(pd.patient_type,im.patient_type) AS patient_type, "
       + " COALESCE(pd.patient_amount_per_category,"
       + " im.patient_amount_per_category) AS patient_amount_per_category, "
       + " pd.patient_insurance_plans_id, "
       + " COALESCE(im.category_payable, iic.insurance_payable) as category_payable, "
       + " iic.insurance_category_name, iic.system_category  "
       + " FROM insurance_plan_details im  "
       + " LEFT JOIN patient_insurance_plan_details pd "
       + " ON (im.plan_id =pd.plan_id and "
       + " im.insurance_category_id = pd.insurance_category_id AND visit_id = ?"
       + " AND pd.patient_type = im.patient_type)  "
       + " JOIN item_insurance_categories iic  "
       + " ON (iic.insurance_category_id = im.insurance_category_id)  "
       + " where im.plan_id = ? AND im.patient_type= ? "
       + " ORDER BY  iic.display_order ";
  /**
   * Gets the plan details.
   *
   * @param planId
   *          the plan id
   * @param visitType
   *          the visit type
   * @param visitId
   *          the visit id
   * @return the plan details
   */

  public List<BasicDynaBean> getPreviousPlanDetails(int planId, String visitType, 
      String visitId) {
    return DatabaseHelper.queryToDynaList(GET_PREVIOUS_VISIT_PLAN_DETAILS, new Object[] { visitId,
        planId, visitType });
  }



}