package com.insta.hms.integration.insurance.submission;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ClaimSubmissionsRepository extends GenericRepository {

  public ClaimSubmissionsRepository() {
    super("claim_submissions");
  }

  public void updateStatusRecvd(Integer remittanceId) {
    DatabaseHelper.update(UPDATE_STATUS_RECEIVED, new Object[] {remittanceId});
  }

  public void updateStatusDenied(Integer remittanceId) {
    DatabaseHelper.update(UPDATE_STATUS_DENIED, new Object[] {remittanceId, remittanceId});
  }

  private static final String UPDATE_STATUS_RECEIVED =
      "UPDATE claim_submissions cs SET status = 'R' "
          + "FROM insurance_remittance_details ird "
          + "WHERE ird.remittance_id = ? "
          + "   AND ird.claim_id = cs.claim_id AND cs.status = 'S' ";

  private static final String UPDATE_STATUS_DENIED =
      "UPDATE claim_submissions cs  SET    status = 'D' "
          + "FROM   bill_charge_claim bcc  JOIN   insurance_remittance_details ird  ON     ( "
          + "              ird.claim_id = bcc.claim_id  AND    ird.remittance_id = ?) "
          + "WHERE  ird.remittance_id = ?  AND    bcc.claim_status = 'D' "
          + "AND    bcc.claim_id = cs.claim_id  AND    cs.status ='R' ";

  private static final String UPDATE_CLAIM_BATCH_STATUS_TO_SENT = "UPDATE claim_submissions "
      + " SET status='S' WHERE submission_batch_id= ?";

  public void updateClaimBatchStatusToSent(String submissionBatchID) {
    DatabaseHelper.update(UPDATE_CLAIM_BATCH_STATUS_TO_SENT, new Object[] {submissionBatchID});
  }

  private static final String GET_CLAIMS_IN_SUBMISSION_BATCH = "SELECT "
      + " cs.claim_id, pip.insurance_co, pip.sponsor_id "
      + " FROM claim_submissions cs"
      + " JOIN insurance_claim icl ON(icl.claim_id = cs.claim_id)"
      + " JOIN patient_insurance_plans pip "
      + " ON(icl.patient_id = pip.patient_id AND icl.plan_id = pip.plan_id)"
      + " WHERE cs.submission_batch_id = ? ";

  public List<BasicDynaBean> getClaimsInSubmissionBatch(String submissionBatchID) {
    return DatabaseHelper.queryToDynaList(GET_CLAIMS_IN_SUBMISSION_BATCH, submissionBatchID);
  }

  private static final String GET_CLAIM_SUBMISSION_COUNT_LIST = "SELECT"
      + " cs.claim_id, count(cs.claim_id)::integer as submissioncount "
      + " FROM claim_submissions cs "
      + " WHERE cs.claim_id in (SELECT claim_id "
      + " FROM claim_submissions WHERE submission_batch_id = ?) "
      + " GROUP BY cs.claim_id ";

  public List<BasicDynaBean> getClaimSubmissionCountList(String submissionBatchID) {
    return DatabaseHelper.queryToDynaList(GET_CLAIM_SUBMISSION_COUNT_LIST, submissionBatchID);
  }


  private static final String GET_DISTINCT_ACCOUNT_GROUPS =

      " SELECT DISTINCT ac_id, id, ac_name, accounting_company_name, ser_reg_no, type "
          + " FROM accountgrp_and_center_view WHERE type='C' ##acid## ##query##"
          + " UNION "
          + " SELECT DISTINCT ac_id, id, ac_name, accounting_company_name, ser_reg_no, type "
          + " FROM accountgrp_and_center_view WHERE type='A' ##storecenter##"
          + " ##query## ";

  /**
   * Gets accounting groups for account and center filter.
   *
   * @param userCenterId the user center id
   * @param query        the query
   * @return the accounting groups
   */
  public List<BasicDynaBean> getAccountingGroups(Integer userCenterId, String query) {
    String qry = null;
    if (null != userCenterId && userCenterId != 0) {
      qry = GET_DISTINCT_ACCOUNT_GROUPS.replace("##acid##",
          "and ac_id=? ").replace("##storecenter##",
          "and (store_center_id = ? or store_center_id is null)");

      if (StringUtils.isBlank(query)) {
        qry = qry.replace("##query##", "");
        return DatabaseHelper.queryToDynaList(qry, new Object[] {
            userCenterId, userCenterId});
      } else {
        qry = qry.replace("##query##", "and ac_name ilike ?");
        String searchString = StringUtils.join(query.split("\\s+"), '%') + '%';
        return DatabaseHelper.queryToDynaList(qry, new Object[] {
            userCenterId, searchString, userCenterId, searchString});
      }


    } else {
      qry = GET_DISTINCT_ACCOUNT_GROUPS.replace("##acid##", "").replace(
          "##storecenter##", "");
      if (StringUtils.isBlank(query)) {
        qry = qry.replace("##query##", "");
        return DatabaseHelper.queryToDynaList(qry);

      } else {
        qry = qry.replace("##query##", "and ac_name ilike ?");
        String searchString = StringUtils.join(query.split("\\s+"), '%') + '%';
        return DatabaseHelper.queryToDynaList(qry, searchString, searchString);
      }
    }
  }
}
