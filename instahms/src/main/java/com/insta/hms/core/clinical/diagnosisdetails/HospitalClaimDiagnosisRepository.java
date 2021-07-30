package com.insta.hms.core.clinical.diagnosisdetails;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

@Repository
public class HospitalClaimDiagnosisRepository extends GenericRepository {

  public HospitalClaimDiagnosisRepository() {
    super("hospital_claim_diagnosis");
  }

  private static final String GET_VISITS_NOT_COPIED_YET_INSURANCE_CLAIM = " SELECT "
      + " distinct(mrd.visit_id)"
      + " FROM insurance_submission_batch isb"
      + " JOIN claim_submissions cs ON (isb.submission_batch_id = cs.submission_batch_id)"
      + " JOIN insurance_claim ic ON (cs.claim_id = ic.claim_id)"
      + " JOIN mrd_diagnosis mrd ON (mrd.visit_id = ic.patient_id)"
      + " LEFT JOIN hospital_claim_diagnosis hcd ON (hcd.visit_id = ic.patient_id)"
      + " WHERE isb.submission_batch_id = ? AND hcd.visit_id IS NULL ";

  private static final String GET_VISITS_NOT_COPIED_YET_SELFPAY = " SELECT distinct(mrd.visit_id) "
      + "        FROM selfpay_submission_batch ssb "
      + "        JOIN bill b ON (ssb.selfpay_batch_id = b.selfpay_batch_id) "
      + "        JOIN mrd_diagnosis mrd ON (mrd.visit_id = b.visit_id) "
      + "        LEFT JOIN hospital_claim_diagnosis hcd ON (hcd.visit_id = b.visit_id) "
      + "        WHERE ssb.selfpay_batch_id = ? AND hcd.visit_id IS NULL ";

  public List<BasicDynaBean> getClaimVisitsNotCopied(String submissionBatchId) throws SQLException {
    return DataBaseUtil.queryToDynaList(GET_VISITS_NOT_COPIED_YET_INSURANCE_CLAIM,
        new Object[] { submissionBatchId });
  }

  public List<BasicDynaBean> getSelfpayVisitsNotCopied(Integer selfpayBatchId) throws SQLException {
    return DataBaseUtil.queryToDynaList(GET_VISITS_NOT_COPIED_YET_SELFPAY,
        new Object[] { selfpayBatchId });
  }

  public static final String FIND_CODER_DIAGNOSIS = " SELECT "
      + " (CASE WHEN diag_type = 'P' THEN 'Principal' "
      + " WHEN diag_type = 'A' THEN 'Admitting' " + " WHEN diag_type = 'V' THEN 'Reason For Visit' "
      + " ELSE 'Secondary' END) AS diag_type, md.diag_type as diagnosis_type, "
      + " md.code_type, icd_code, code_desc " + " FROM hospital_claim_diagnosis md "
      + " JOIN mrd_codes_master mcm ON (mcm.code_type = md.code_type AND mcm.code = md.icd_code) "
      + " WHERE visit_id = ?";

}
