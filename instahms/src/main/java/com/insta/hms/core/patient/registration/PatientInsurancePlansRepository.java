package com.insta.hms.core.patient.registration;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class PatientInsurancePlansRepository.
 */
@Repository
public class PatientInsurancePlansRepository extends GenericRepository {

  /**
   * Instantiates a new patient insurance plans repository.
   */
  public PatientInsurancePlansRepository() {
    super("patient_insurance_plans");
  }

  /** The plan details fields. */
  private static String PLAN_DETAILS_FIELDS = " SELECT pip.patient_id, pip.plan_id, "
      + " pip.sponsor_id, tpa.tpa_name,tpa.sponsor_type,pip.priority ";

  /** The plan details tables. */
  private static String PLAN_DETAILS_TABLES = " FROM patient_insurance_plans pip "
      + " LEFT JOIN tpa_master tpa ON (tpa.tpa_id = pip.sponsor_id) ";

  /** The plan details where. */
  private static String PLAN_DETAILS_WHERE = 
      " WHERE pip.patient_id = ? AND pip.plan_id IS NOT NULL order by priority";

  /**
   * Gets the plan ids.
   *
   * @param patientId
   *          the patient id
   * @return the plan ids
   */
  public List<BasicDynaBean> getPlanIds(String patientId) {
    return DatabaseHelper.queryToDynaList(PLAN_DETAILS_FIELDS + PLAN_DETAILS_TABLES
        + PLAN_DETAILS_WHERE, patientId);
  }

  /** The plan details visit. */
  private static String PLAN_DETAILS_VISIT = 
      "SELECT pip.mr_no,pip.patient_id,pip.insurance_co,pip.sponsor_id,"
      + " pip.plan_id,pip.plan_type_id, "
      + " pip.insurance_approval,pip.codification_status,"
      + " pip.prior_auth_id,pip.codified_by,pip.patient_policy_id, "
      + " pip.prior_auth_mode_id,pip.codification_remarks,pip.use_drg,"
      + " pip.drg_code,pip.use_perdiem, "
      + " pip.per_diem_code,pip.priority,pip.plan_limit,pip.visit_limit,"
      + " pip.visit_deductible,pip.visit_copay_percentage, "
      + " pip.visit_max_copay_percentage,pip.visit_per_day_limit,"
      + " pip.episode_limit,pip.episode_deductible, "
      + " pip.episode_copay_percentage,pip.episode_max_copay_percentage,pip.utilization_amount,"
      + " ppd.member_id,ppd.policy_holder_name,ppd.employer_name,ppd.patient_relationship, "
      + " ppd.eligibility_reference_number, ppd.eligibility_authorization_status, "
      + " ppd.eligibility_authorization_remarks, "
      + " ppd.policy_validity_start,ppd.policy_validity_end,ppd.plan_id,ppd.patient_policy_id,"
      + " ppd.status,ppd.policy_number,ppd.visit_id, pr.visit_type, ipm.discount_plan_id,  "
      + " (select discount_plan_name from discount_plan_main where"
      + " discount_plan_id = ipm.discount_plan_id) as discount_plan_name,"
      + " pdd.doc_id, pip.insurance_co,"
      + " pip.patient_insurance_plans_id, icm.insurance_co_name,"
      + " ipm.limits_include_followup, tpa.tpa_name, tpa.sponsor_type,"
      + " incm.category_name as plan_type_name, ipm.plan_name"
      + " FROM patient_insurance_plans pip"
      + " LEFT JOIN insurance_plan_main ipm USING (plan_id)"
      + " LEFT JOIN tpa_master tpa ON (tpa.tpa_id = pip.sponsor_id)"
      + " LEFT JOIN insurance_category_master incm ON (incm.category_id = pip.plan_type_id)"
      + " LEFT JOIN patient_registration pr ON pr.patient_id = pip.patient_id"
      + " LEFT JOIN patient_policy_details ppd"
      + " ON (ppd.visit_id = pip.patient_id  and pip.plan_id = ppd.plan_id )"
      + " LEFT JOIN plan_docs_details pdd on pdd.patient_policy_id= ppd.patient_policy_id"
      + " LEFT JOIN insurance_company_master icm ON (icm.insurance_co_id = pip.insurance_co)"
      + " WHERE  pip.patient_id= ? order by pip.priority";

  /**
   * Gets the details.
   *
   * @param patientId
   *          the patient id
   * @return the details
   */
  public List<BasicDynaBean> getDetails(String patientId) {
    return DatabaseHelper.queryToDynaList(PLAN_DETAILS_VISIT, patientId);
  }

  /** The Constant INSURANCE_CARD_DOC. */
  private static final String INSURANCE_CARD_DOC = " SELECT pdc.doc_content_bytea"
      + " FROM patient_insurance_plans pip"
      + " JOIN plan_docs_details pdd USING(patient_policy_id) "
      + " JOIN patient_documents pdc ON(pdc.doc_id = pdd.doc_id) "
      + " WHERE pip.patient_id=?  AND pip.patient_policy_id=? ";

  /**
   * Gets the insurance document.
   *
   * @param object
   *          the object
   * @return the insurance document
   */
  public BasicDynaBean getInsuranceDocument(Object[] object) {
    return DatabaseHelper.queryToDynaBean(INSURANCE_CARD_DOC, object);
  }

  /** The Constant GET_PLAN_DETAILS. */
  private static final String GET_PLAN_DETAILS = 
      " SELECT pip.patient_id, pip.plan_id, ipm.limits_include_followup,"
      + " pip.sponsor_id, tpa.tpa_name,tpa.sponsor_type,pip.priority,"
      + " tpa.claim_amount_includes_tax,"
      + " tpa.limit_includes_tax, pip.priority,"
      + " pip.prior_auth_id,pip.prior_auth_mode_id"
      + " FROM patient_insurance_plans pip"
      + " JOIN insurance_plan_main ipm"
      + " ON(pip.plan_id = ipm.plan_id AND pip.patient_id = ?)"
      + " LEFT JOIN tpa_master tpa ON (tpa.tpa_id = pip.sponsor_id) "
      + " WHERE pip.patient_id = ? order by priority ";

  /**
   * Gets the plan details.
   *
   * @param visitId
   *          the visit id
   * @return the plan details
   */
  public List<BasicDynaBean> getPlanDetails(String visitId) {
    return DatabaseHelper.queryToDynaList(GET_PLAN_DETAILS, new Object[] { visitId, visitId });
  }

  /** The get patient sponsor details. */
  private static String GET_PATIENT_SPONSOR_DETAILS = " SELECT od.org_name,"
      + " picm.insurance_co_name, sicm.insurance_co_name as sec_insurance_co_name,"
      + " ptpa.tpa_name ,stpa.tpa_name as sec_tpa_name,"
      + " picam.category_name, sicam.category_name as sec_category_name,"
      + " pipm.plan_name ,sipm.plan_name as sec_plan_name, pipm.plan_notes,"
      + " sipm.plan_notes as sec_plan_notes,"
      + " pipm.plan_exclusions, sipm.plan_exclusions as sec_plan_exclusions, "
      + " ppip.priority primary_priority, spip.priority secondary_priority,"
      + " ppdp.policy_validity_start AS insurance_validity_start_date, "
      + " ppdp.policy_validity_end AS insurance_validity_end_date,"
      + " ppds.policy_validity_start AS sec_ins_validity_start_date,"
      + " ppds.policy_validity_end as sec_ins_validity_end_date,"
      + " pipm.enable_pre_authorized_limit "
      + " FROM patient_registration pr"
      + " LEFT JOIN organization_details od ON(pr.org_id = od.org_id)"
      + " LEFT JOIN patient_insurance_plans ppip"
      + " ON(ppip.patient_id = pr.patient_id AND ppip.priority = 1)"
      + " LEFT JOIN patient_insurance_plans spip"
      + " ON(spip.patient_id = pr.patient_id AND spip.priority = 2)"
      + " LEFT JOIN insurance_plan_main pipm ON(pipm.plan_id = ppip.plan_id)"
      + " LEFT JOIN insurance_plan_main sipm ON(sipm.plan_id = spip.plan_id)"
      + " LEFT JOIN patient_policy_details ppdp ON ppdp.patient_policy_id = ppip.patient_policy_id"
      + " LEFT JOIN patient_policy_details ppds ON ppds.patient_policy_id = spip.patient_policy_id"
      + " LEFT JOIN insurance_company_master picm"
      + " ON(picm.insurance_co_id = pipm.insurance_co_id)"
      + " LEFT JOIN insurance_company_master sicm"
      + " ON(sicm.insurance_co_id = sipm.insurance_co_id)"
      + " LEFT JOIN tpa_master ptpa ON (ptpa.tpa_id = ppip.sponsor_id)"
      + " LEFT JOIN tpa_master stpa ON (stpa.tpa_id = spip.sponsor_id)"
      + " LEFT JOIN insurance_category_master picam" + " ON (picam.category_id=pipm.category_id)"
      + " LEFT JOIN insurance_category_master sicam" + " ON (sicam.category_id=sipm.category_id)"
      + " WHERE pr.patient_id =? ";

  /**
   * Gets the pat insurance info.
   *
   * @param visitId
   *          the visit id
   * @return the pat insurance info
   */
  public BasicDynaBean getPatInsuranceInfo(String visitId) {
    return DatabaseHelper.queryToDynaBean(GET_PATIENT_SPONSOR_DETAILS, visitId);

  }

  /** The Constant GET_POLICY_DETAILS. */
  private static final String GET_POLICY_DETAILS = 
      "select pip.mr_no,pip.insurance_co,pip.sponsor_id,pip.plan_type_id,pip.plan_id,"
      + " ppd.member_id,pr.visit_type,pip.patient_id, pdd.doc_id,"
      + " ppd.policy_validity_start, ppd.policy_validity_end"
      + " from patient_insurance_plans pip"
      + " JOIN patient_policy_details  ppd ON ppd.mr_no= pip.mr_no AND"
      + " ppd.visit_id=pip.patient_id AND ppd.plan_id=pip.plan_id"
      + " LEFT JOIN plan_docs_details pdd on pdd.patient_policy_id= ppd.patient_policy_id"
      + " JOIN patient_registration pr ON pr.patient_id=pip.patient_id"
      + " WHERE # AND (ppd.policy_validity_end is null"
      + " OR ppd.policy_validity_end >= current_date)" + " order by pip.patient_id desc ";

  /**
   * Gets the policy details.
   *
   * @param mrNo
   *          the mr no
   * @param centerId
   *          the center id
   * @return the policy details
   */
  public List<BasicDynaBean> getPolicyDetails(String mrNo, Integer centerId) {
    if (centerId == null) {
      return DatabaseHelper.queryToDynaList(GET_POLICY_DETAILS.replaceAll("#", "pip.mr_no= ?"),
          mrNo);
    } else {
      return DatabaseHelper.queryToDynaList(
          GET_POLICY_DETAILS.replaceAll("#", "pip.mr_no= ? AND pr.center_id=?"), new Object[] {
              mrNo, centerId });
    }
  }

  /** The Constant GET_INSURANCE. */
  private static final String GET_INSURANCE = 
      "SELECT tpa.tpa_name, tpa.tpa_id, ic.insurance_co_name,ic.insurance_co_id, pr.op_type,"
      + " icm.category_name, icm.category_id, ip.plan_name, ip.plan_id,"
      + " ip.plan_notes, ip.plan_exclusions, ppd.member_id, pdd.doc_id,"
      + " pip.sponsor_id"
      + " FROM patient_insurance_plans pip "
      + " LEFT JOIN tpa_master tpa ON tpa.tpa_id = pip.sponsor_id"
      + " LEFT JOIN insurance_company_master ic ON ic.insurance_co_id = pip.insurance_co"
      + " LEFT JOIN insurance_category_master icm ON icm.category_id = pip.plan_type_id"
      + " LEFT JOIN insurance_plan_main ip ON ip.plan_id = pip.plan_id"
      + " LEFT JOIN patient_registration pr ON pr.patient_id = pip.patient_id"
      + " LEFT JOIN patient_policy_details ppd"
      + " ON (ppd.visit_id = pip.patient_id  and pip.plan_id = ppd.plan_id )"
      + " LEFT JOIN plan_docs_details pdd on pdd.patient_policy_id= ppd.patient_policy_id"
      + " WHERE pip.patient_id = ? AND pr.visit_type = ? order by pip.priority;";

  /**
   * Gets the insurance details.
   *
   * @param visitId
   *          the visit id
   * @param visitType
   *          the visit type
   * @return the insurance details
   */
  public List getInsuranceDetails(String visitId, String visitType) {
    return DatabaseHelper.queryToDynaList(GET_INSURANCE, new Object[] { visitId, visitType });
  }

  /** The Constant GET_DISCOUNT_PLAN_DETAILS. */
  private static final String GET_DISCOUNT_PLAN_DETAILS = 
      "SELECT dpm.discount_plan_id, discount_plan_name, applicable_type, applicable_to_id,"
      + " discount_value, discount_type, priority"
      + " FROM bill b "
      + " LEFT JOIN discount_plan_main dpm on (b.discount_category_id = dpm.discount_plan_id)"
      + " LEFT JOIN discount_plan_details dpd ON (dpm.discount_plan_id = dpd.discount_plan_id)"
      + " WHERE b.bill_no=? ORDER BY dpd.priority";

  /**
   * Gets the discount plan details.
   *
   * @param billNo
   *          the bill no
   * @return the discount plan details
   */
  public List<BasicDynaBean> getDiscountPlanDetails(String billNo) {
    return DatabaseHelper.queryToDynaList(GET_DISCOUNT_PLAN_DETAILS, billNo);
  }

  /** The Constant GET_ALL_PATIENT_PLANS_DETAILS. */
  public static final String GET_ALL_PATIENT_PLANS_DETAILS = 
      "SELECT DISTINCT pip.insurance_co, pip.sponsor_id, pip.plan_id, "
      + "ipm.limits_include_followup, "
      + "pip.plan_type_id, ppd.member_id, ppd.policy_number,ppd.policy_validity_start, "
      + "ppd.policy_validity_end, "
      + "ppd.policy_holder_name, ppd.patient_relationship, pip.patient_policy_id, "
      + "pip.prior_auth_id, pip.prior_auth_mode_id, "
      + "ppd.mr_no, pip.priority, icm.insurance_co_id, icm.insurance_co_name,"
      + "pip.use_drg, pip.use_perdiem, pip.per_diem_code, pip.insurance_approval,ppd.status, "
      + "pip.plan_limit,pip.visit_limit,pip.visit_deductible,pip.visit_copay_percentage,"
      + "pip.visit_max_copay_percentage, "
      + "pip.visit_per_day_limit,pip.episode_limit,pip.episode_deductible,"
      + "pip.episode_copay_percentage,pip.episode_max_copay_percentage, "
      + "pip.utilization_amount FROM patient_insurance_plans pip "
      + "LEFT JOIN insurance_plan_main ipm USING (plan_id) "
      + "LEFT JOIN patient_policy_details ppd ON (ppd.patient_policy_id = pip.patient_policy_id) "
      + "LEFT JOIN insurance_company_master icm USING (insurance_co_id) "
      + "WHERE pip.mr_no= ? "
      + "AND pip.use_drg = 'N' ";

  /**
   * Gets the all patient plan details.
   *
   * @param mrNo
   *          the mr no
   * @return the all patient plan details
   */
  public List<BasicDynaBean> getAllPatientPlanDetails(String mrNo) {
    return DatabaseHelper.queryToDynaList(GET_ALL_PATIENT_PLANS_DETAILS, new Object[] { mrNo });
  }

  /** The Constant GET_INSURANCE_PLANS. */
  private static final String GET_INSURANCE_PLANS = 
      "select case when pipd.category_payable='Y' then 't'"
      + " when sipd.category_payable ='Y' then 't' else 'f'"
      + " end as plan_category_payable,pipd.category_payable as pri_cat_payable "
      + " from patient_registration pr "
      + " left join patient_insurance_plans pip"
      + " on (pip.patient_id = pr.patient_id and pip.priority=1)"
      + " left join patient_insurance_plans sip"
      + " on (sip.patient_id = pr.patient_id and sip.priority=2)"
      + " left join insurance_plan_main pipm"
      + " on(pipm.plan_id = pip.plan_id and pip.priority=1)"
      + " left join insurance_plan_main sipm"
      + " on(sipm.plan_id = sip.plan_id and sip.priority=2)"
      + " left join insurance_plan_details pipd"
      + " on(pipm.plan_id = pipd.plan_id"
      + " and pipd.insurance_category_id =? and pipd.patient_type=?)"
      + " left join insurance_plan_details sipd"
      + " on(sipm.plan_id = sipd.plan_id"
      + " and sipd.insurance_category_id =? and sipd.patient_type=?)" + " where pr.patient_id=?";

  /**
   * Gets the insurance category payable status.
   *
   * @param visitId
   *          the visit id
   * @param insuranceCategoryId
   *          the insurance category id
   * @param patientType
   *          the patient type
   * @return the insurance category payable status
   */
  public BasicDynaBean getInsuranceCategoryPayableStatus(String visitId, int insuranceCategoryId,
      String patientType) {
    return DatabaseHelper.queryToDynaBean(GET_INSURANCE_PLANS, new Object[] { insuranceCategoryId,
        patientType, insuranceCategoryId, patientType, visitId });
  }

  /** The Constant UPDATE_PRI_CASE_RATE_LIMITS. */
  private static final String UPDATE_PRI_CASE_RATE_LIMITS = 
      " UPDATE patient_insurance_plan_details pipd"
      + " SET per_treatment_limit = crd.amount"
      + " FROM patient_registration pr"
      + " JOIN patient_insurance_plans pip ON(pr.patient_id = pip.patient_id AND pip.priority=1)"
      + " JOIN case_rate_detail crd ON(pr.primary_case_rate_id = crd.case_rate_id)"
      + " WHERE pr.patient_id = pipd.visit_id AND pipd.plan_id = pip.plan_id"
      + " AND pipd.insurance_category_id = crd.insurance_category_id"
      + " AND pr.patient_id = ?  AND crd.case_rate_id = ? ";

  /** The Constant UPDATE_SEC_CASE_RATE_LIMITS. */
  private static final String UPDATE_SEC_CASE_RATE_LIMITS = 
      " UPDATE patient_insurance_plan_details pipd"
      + " SET per_treatment_limit = (COALESCE(per_treatment_limit,0) + crd.amount)"
      + " FROM patient_registration pr"
      + " JOIN patient_insurance_plans pip ON(pr.patient_id = pip.patient_id AND pip.priority=1)"
      + " JOIN case_rate_detail crd ON(pr.secondary_case_rate_id = crd.case_rate_id)"
      + " WHERE pr.patient_id = pipd.visit_id AND pipd.plan_id = pip.plan_id"
      + " AND pipd.insurance_category_id = crd.insurance_category_id"
      + " AND pr.patient_id = ?  AND crd.case_rate_id = ? ";

  /**
   * Update case rate limits.
   *
   * @param caseRateId
   *          the case rate id
   * @param visitId
   *          the visit id
   * @param caseRateNo
   *          the case rate no
   */
  public void updateCaseRateLimits(Integer caseRateId, String visitId, Integer caseRateNo) {
    String query = null;
    if (caseRateNo == 1) {
      query = UPDATE_PRI_CASE_RATE_LIMITS;
    } else if (caseRateNo == 2) {
      query = UPDATE_SEC_CASE_RATE_LIMITS;
    }
    DatabaseHelper.update(query, new Object[] { visitId, caseRateId });
  }

}
