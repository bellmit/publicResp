package com.insta.hms.core.insurance;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

/**
 * The Class InsuranceSubmissionBatchRepository.
 */
@Repository
public class InsuranceSubmissionBatchRepository extends GenericRepository {

  /**
   * Instantiates a new insurance submission batch repository.
   */
  public InsuranceSubmissionBatchRepository() {
    super("insurance_submission_batch");
  }

  /** The Constant SUBMISSION_BATCH_DETAILS. */
  private static final String SUBMISSION_BATCH_DETAILS = " SELECT * from ( "
      + "   SELECT submission_batch_id,created_date,reference_number,submission_date,"
      + "   insurance_co_id,file_name,tpa_id,"
      + "   patient_type, is_resubmission, status,username, "
      + "   textcat_commacat(category_name) AS category_name, "
      + "   tpa_name,plan_id,plan_name,insurance_co_name,account_group, "
      + "   account_group_name,account_group_service_reg_no,"
      + "   center_name,hospital_center_service_reg_no  " + " FROM  "
      + "  (SELECT isb.submission_batch_id, created_date, reference_number, "
      + "  submission_date, isb.insurance_co_id, file_name, "
      + "  isb.tpa_id, instab.insurance_category_id, "
      + "  patient_type, is_resubmission, isb.status,isb.username, cat.category_name, "
      + "  tp.tpa_name,isb.plan_id,ipm.plan_name, icm.insurance_co_name,"
      + "  isb.account_group,ag.account_group_name, ag.account_group_service_reg_no, "
      + "  hc.center_name, hc.hospital_center_service_reg_no "
      + "    FROM insurance_submission_batch isb "
      + "   LEFT JOIN tpa_master tp ON(tp.tpa_id = isb.tpa_id) "
      + "   LEFT JOIN insurance_company_master icm "
      + "   ON(icm.insurance_co_id = isb.insurance_co_id) "
      + "   LEFT JOIN (SELECT submission_batch_id, "
      + "      regexp_split_to_table(insurance_category_id, E'\\,')::TEXT "
      + "      AS insurance_category_id " + "      FROM insurance_submission_batch ) AS instab "
      + "      ON (instab.submission_batch_id = isb.submission_batch_id) "
      + "   LEFT JOIN insurance_category_master cat "
      + "   ON (cat.category_id::text = instab.insurance_category_id) "
      + "   LEFT JOIN insurance_plan_main ipm ON(ipm.plan_id = isb.plan_id) "
      + "   LEFT JOIN account_group_master ag ON(ag.account_group_id = isb.account_group) "
      + "   LEFT JOIN hospital_center_master hc ON(hc.center_id = isb.center_id) " + "  ) as foo "
      + "  GROUP BY submission_batch_id, created_date,reference_number, submission_date, "
      + "   insurance_co_id, file_name, tpa_id, patient_type, is_resubmission, "
      + "   status,username,tpa_name,plan_id,plan_name, "
      + "   insurance_co_name,account_group,account_group_name,"
      + "   account_group_service_reg_no,center_name,hospital_center_service_reg_no "
      + " ) AS foo1 " + " WHERE submission_batch_id = ? ";

  /**
   * Find submission batch.
   *
   * @param submissionBatchId the submission batch id
   * @return the basic dyna bean
   */
  public BasicDynaBean findSubmissionBatch(String submissionBatchId) {
    return DatabaseHelper.queryToDynaBean(SUBMISSION_BATCH_DETAILS, submissionBatchId);
  }

}
