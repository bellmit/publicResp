/*
 * 
 */

package com.insta.hms.core.insurance.claimhistory;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class ClaimSubmissionHistoryRepository.
 */
@Repository
public class ClaimSubmissionHistoryRepository extends GenericRepository {

  /**
   * Instantiates a new claim submission history repository.
   */
  public ClaimSubmissionHistoryRepository() {
    super("claim_submission_history");
  }
  
  /** The Constant UPDATE_CLAIM_RECEIVED_AMT. */
  private static final String UPDATE_CLAIM_RECEIVED_AMT = " UPDATE claim_activity_history cah "
      + " SET received_amt = irad.payment_amount , remittance_id = ird.remittance_id "
      + " FROM insurance_remittance_details ird "
      + " JOIN insurance_remittance_activity_details irad "
      + "   ON(ird.remittance_id = irad.remittance_id) "
      + " WHERE ird.claim_id = irad.claim_id "
      + " AND cah.claim_id = irad.claim_id "
      + " AND irad.activity_id = cah.activity_id "
      + " AND cah.claim_activity_id NOT LIKE 'ACT-%' "
      + " AND ird.remittance_id = ? AND cah.received_amt = 0 "
      + " AND cah.remittance_id = 0 AND irad.payment_amount >= 0 ";
  
  /**
   * Update claim received amount.
   *
   * @param remittanceId the remittance id
   * @return the boolean
   */
  public Boolean updateClaimReceivedAmount(Integer remittanceId) {
    return DatabaseHelper
        .update(UPDATE_CLAIM_RECEIVED_AMT, new Object[] {remittanceId}) >= 0;
  }

  /** The Constant UPDATE_CLAIM_RECOVERY_AMOUNT. */
  private static final String UPDATE_CLAIM_RECOVERY_AMOUNT = " UPDATE claim_activity_history cah "
      + " SET recovery_amt = irad.payment_amount , recovery_remittance_id = ird.remittance_id "
      + " FROM insurance_remittance_details ird "
      + " JOIN insurance_remittance_activity_details irad "
      + "   ON(ird.remittance_id = irad.remittance_id) "
      + " WHERE irad.claim_id = irad.claim_id "
      + " AND cah.claim_id = ird.claim_id "
      + " AND irad.activity_id = cah.activity_id "
      + " AND cah.claim_activity_id NOT LIKE 'ACT-%' "
      + " AND ird.remittance_id = ?  AND cah.received_amt != 0 "
      + " AND cah.remittance_id != 0 AND irad.payment_amount < 0 "
      + " AND cah.received_amt = -(irad.payment_amount) AND cah.recovery_remittance_id = 0 "
      + " AND cah.recovery_amt = 0 ";
  
  /**
   * Update claim recovery amount.
   *
   * @param remittanceId the remittance id
   * @return the boolean
   */
  public Boolean updateClaimRecoveryAmount(Integer remittanceId) {
    return DatabaseHelper.update(UPDATE_CLAIM_RECOVERY_AMOUNT, new Object[]{remittanceId}) >= 0;
  }
  
  /** The Constant UPDATE_COMBINED_ACTIVITY_RECEIVED_AMOUNT. */
  private static final String UPDATE_COMBINED_ACTIVITY_RECEIVED_AMOUNT = " UPDATE "
      + " claim_activity_history cah "
      + " SET received_amt = CASE WHEN foo.total_claim_amt > 0 "
      + " THEN (foo.claim_amount * foo.payment_amount)/foo.total_claim_amt ELSE 0 END,"
      + " remittance_id = foo.remittance_id "
      + " FROM (SELECT sum(ca.claim_amount) OVER (partition BY ca.claim_activity_id,"
      + " csh.submission_batch_id) "
      + "  AS total_claim_amt, ca.charge_id, ca.claim_amount, irad.payment_amount, "
      + "  ca.sale_item_id, ca.activity_id, irad.remittance_id, ca.claim_id "
      + " FROM claim_activity_history ca "
      + " JOIN claim_submission_history csh "
      + " ON(ca.claim_submission_hist_id = csh.claim_submission_hist_id) "
      + " JOIN insurance_remittance_activity_details irad "
      + " ON(ca.activity_id = irad.activity_id AND ca.claim_id = irad.claim_id) "
      + " WHERE irad.remittance_id = ? "
      + "   AND ca.claim_activity_id LIKE 'ACT-%') AS foo "
      + " WHERE cah.charge_id = foo.charge_id "
      + " AND cah.claim_id = foo.claim_id "
      + " AND cah.sale_item_id = foo.sale_item_id "
      + " AND cah.activity_id = foo.activity_id "
      + " AND cah.claim_activity_id LIKE 'ACT-%' "
      + " AND cah.remittance_id = 0 AND cah.received_amt = 0 ";

  /**
   * Update combined activity received amount.
   *
   * @param remittanceId the remittance id
   * @return the boolean
   */
  public Boolean updateCombinedActivityReceivedAmount(int remittanceId) {
    return DatabaseHelper.update(UPDATE_COMBINED_ACTIVITY_RECEIVED_AMOUNT, 
        new Object[] {remittanceId}) >= 0;
  }

  /** The Constant UPDATE_COMBINED_ACTIVITY_RECOVERY_AMOUNT. */
  private static final String UPDATE_COMBINED_ACTIVITY_RECOVERY_AMOUNT = "UPDATE "
      + " claim_activity_history cah "
      + " SET recovery_amt = CASE WHEN foo.total_claim_amt > 0 "
      + " THEN (foo.claim_amount * foo.payment_amount)/foo.total_claim_amt ELSE 0 END, "
      + " recovery_remittance_id = foo.remittance_id "
      + " FROM (SELECT sum(ca.claim_amount) OVER (partition BY ca.claim_activity_id, "
      + " csh.submission_batch_id) "
      + "  AS total_claim_amt, irad.remittance_id, "
      + "  ca.charge_id, ca.claim_amount, irad.payment_amount, "
      + " ca.sale_item_id, ca.activity_id, ca.claim_id "
      + " FROM claim_activity_history ca "
      + " JOIN claim_submission_history csh "
      + " ON(ca.claim_submission_hist_id = csh.claim_submission_hist_id) "
      + " JOIN insurance_remittance_activity_details irad ON(ca.activity_id = irad.activity_id) "
      + " WHERE irad.remittance_id = ? AND ca.claim_activity_id LIKE 'ACT-%' "
      + "   AND ca.recovery_amt = 0 AND ca.recovery_remittance_id = 0 "
      + "   AND irad.payment_amount < 0) AS foo "
      + " WHERE cah.charge_id = foo.charge_id "
      + " AND cah.sale_item_id = foo.sale_item_id "
      + " AND cah.claim_id = foo.claim_id "
      + " AND cah.activity_id = foo.activity_id AND cah.claim_activity_id LIKE 'ACT-%' "
      + " AND cah.recovery_remittance_id = 0 and cah.recovery_amt = 0 "
      + " AND cah.received_amt = CASE WHEN foo.total_claim_amt > 0 "
      + " THEN -((foo.claim_amount * foo.payment_amount)/foo.total_claim_amt) ELSE 0 END "
      + " AND cah.remittance_id != 0 AND cah.received_amt != 0";
  
  /**
   * Update combined activity recovery amount.
   *
   * @param remittanceId the remittance id
   * @return the boolean
   */
  public Boolean updateCombinedActivityRecoveryAmount(int remittanceId) {
    return DatabaseHelper.update(UPDATE_COMBINED_ACTIVITY_RECOVERY_AMOUNT, 
        new Object[] {remittanceId}) >= 0;
  }

  /** The Constant UPDATE_ACTIVITY_STATUS_FOR_HOSPITAL. */
  private static final String UPDATE_ACTIVITY_STATUS_FOR_HOSPITAL = "UPDATE "
      + " claim_activity_history cah SET activity_status = bcc.claim_status "
      + " FROM bill_charge_claim bcc "
      + " WHERE cah.claim_id = bcc.claim_id AND "
      + " bcc.charge_id = cah.charge_id AND cah.activity_id  LIKE 'A-%'  "
      + " AND cah.remittance_id = ? ";
  
  /**
   * Update activity status for hospital.
   *
   * @param remittanceId the remittance id
   * @return the boolean
   */
  public Boolean updateActivityStatusForHospital(int remittanceId) {
    return DatabaseHelper.update(UPDATE_ACTIVITY_STATUS_FOR_HOSPITAL, 
        new Object[]{remittanceId}) >= 0;
  }
  
  /** The Constant UPDATE_ACTIVITY_STATUS_FOR_PHARMACY. */
  private static final String UPDATE_ACTIVITY_STATUS_FOR_PHARMACY = "UPDATE "
      + " claim_activity_history cah SET activity_status = scd.claim_status "
      + " FROM sales_claim_details scd "
      + " WHERE cah.claim_id = scd.claim_id AND "
      + " scd.sale_item_id = cah.sale_item_id AND cah.activity_id  LIKE 'P-%'  "
      + " AND cah.remittance_id = ? ";

  /**
   * Update activity status for pharmacy.
   *
   * @param remittanceId the remittance id
   * @return the boolean
   */
  public Boolean updateActivityStatusForPharmacy(int remittanceId) {
    return DatabaseHelper.update(UPDATE_ACTIVITY_STATUS_FOR_PHARMACY, 
        new Object[]{remittanceId}) >= 0;
  }
  
  /** The Constant GET_CLAIM_ACTIVITY_HISTORY. */
  private static final String GET_CLAIM_ACTIVITY_HISTORY = "SELECT * FROM ( "
      + " SELECT 'Submission' as txn_type, isb.submission_batch_id as id, "
      + " to_char(isb.submission_date, 'dd/mm/yyyy HH:MI') as sub_date, "
      + " cah.activity_id, cah.quantity, "
      + " cah.claim_amount, cah.activity_vat, cah.activity_vat_percent, "
      + " csh.resubmission_type, 0 as payment_amount, "
      + " CASE WHEN cah.activity_status = 'O' THEN 'Open' "
      + " WHEN cah.activity_status = 'D' THEN 'Denied'"
      + " WHEN cah.activity_status = 'C' THEN 'Closed' END AS activity_status, "
      + " '' as denial_code "
      + " FROM insurance_submission_batch isb "
      + " JOIN  claim_submission_history csh ON(isb.submission_batch_id = csh.submission_batch_id  "
      + "   AND csh.claim_id = ?)"
      + " JOIN claim_activity_history cah "
      + " ON(csh.claim_submission_hist_id = cah.claim_submission_hist_id  "
      + "   AND csh.claim_id = cah.claim_id AND cah.charge_id = ? AND cah.sale_item_id = ?) "
      + " UNION ALL"
      + " SELECT 'Remittance' as txn_type, ird.payment_reference as id, "
      + " to_char(ir.transaction_date, 'dd/mm/yyyy HH:MI') as sub_date, "
      + " irad.activity_id, cah.quantity,  "
      + " 0 as claim_amount, 0 as activity_vat, 0 as activity_vat_percent, "
      + " NULL as resubmission_type, cah.received_amt as payment_amount, "
      + " CASE WHEN cah.activity_status = 'O' THEN 'Open' "
      + " WHEN cah.activity_status = 'D' THEN 'Denied' "
      + " WHEN cah.activity_status = 'C' THEN 'Closed' END AS activity_status, "
      + " irad.denial_code "
      + " FROM insurance_remittance ir "
      + " JOIN insurance_remittance_details ird ON(ir.remittance_id = ird.remittance_id "
      + "   AND ird.claim_id = ?) "
      + " JOIN claim_activity_history cah ON(cah.remittance_id = ird.remittance_id "
      + "   AND cah.claim_id = ird.claim_id AND cah.charge_id = ? AND cah.sale_item_id = ?) "
      + " JOIN insurance_remittance_activity_details irad "
      + " ON(irad.remittance_id = cah.remittance_id "
      + "   AND irad.activity_id = cah.activity_id)"
      + " UNION ALL"
      + " SELECT 'Remittance' as txn_type, ird.payment_reference as id, "
      + " to_char(ir.transaction_date, 'dd/mm/yyyy HH:MI') as sub_date, "
      + " irad.activity_id, cah.quantity,  "
      + " 0 as claim_amount, 0 as activity_vat, 0 as activity_vat_percent, "
      + " NULL as resubmission_type, cah.recovery_amt as payment_amount, "
      + " 'Denied' AS activity_status, "
      + " irad.denial_code "
      + " FROM insurance_remittance ir "
      + " JOIN insurance_remittance_details ird ON(ir.remittance_id = ird.remittance_id "
      + "   AND ird.claim_id = ?) "
      + " JOIN claim_activity_history cah ON(cah.recovery_remittance_id = ird.remittance_id "
      + "   AND cah.claim_id = ird.claim_id AND cah.charge_id = ? AND cah.sale_item_id = ?) "
      + " JOIN insurance_remittance_activity_details irad "
      + " ON(irad.remittance_id = cah.recovery_remittance_id "
      + "   AND irad.activity_id = cah.activity_id)"
      + ") as foo "
      + " ORDER BY foo.sub_date ";

  /**
   * Gets the claim activity history.
   *
   * @param claimId the claim id
   * @param chargeId the charge id
   * @param saleItemId the sale item id
   * @return the claim activity history
   */
  public List<BasicDynaBean> getClaimActivityHistory(String claimId, String chargeId, 
      Integer saleItemId) {
    return DatabaseHelper.queryToDynaList(GET_CLAIM_ACTIVITY_HISTORY, 
        new Object[]{claimId, chargeId, saleItemId, claimId, chargeId, saleItemId, claimId, 
          chargeId, saleItemId});
  }

  /** The Constant UPDATE_RECEIVED_AMT_AND_REMITTANCE_ID. */
  private static final String UPDATE_RECEIVED_AMT_AND_REMITTANCE_ID = " UPDATE "
      + " claim_submission_history csh SET remittance_id = foo.remittance_id,"
      + " received_amt = foo.received_amt "
      + " FROM (SELECT cah.claim_submission_hist_id, cah.claim_id, cah.remittance_id,"
      + " sum(received_amt) as received_amt "
      + " FROM claim_activity_history cah WHERE cah.remittance_id = ? "
      + " GROUP by cah.claim_id, cah.claim_submission_hist_id, cah.remittance_id) as foo "
      + " WHERE csh.claim_submission_hist_id = foo.claim_submission_hist_id "
      + " AND csh.claim_id = foo.claim_id AND csh.remittance_id = 0 ";
  
  /**
   * Update recieved amt and remittance id.
   *
   * @param remitId the remit id
   * @return the boolean
   */
  public Boolean updateRecievedAmtAndRemittanceId(int remitId) {
    return DatabaseHelper.update(UPDATE_RECEIVED_AMT_AND_REMITTANCE_ID, new Object[]{remitId}) >= 0;
  }
  
  /** The Constant UPDATE_RECOVERY_AMT_AND_REMITTANCE_ID. */
  private static final String UPDATE_RECOVERY_AMT_AND_REMITTANCE_ID = " UPDATE "
      + " claim_submission_history csh "
      + " SET recovery_remittance_id = foo.recovery_remittance_id, recovery_amt = foo.recovery_amt "
      + " FROM (SELECT cah.claim_submission_hist_id, cah.claim_id, cah.recovery_remittance_id, "
      + " sum(recovery_amt) as recovery_amt "
      + " FROM claim_activity_history cah WHERE cah.recovery_remittance_id = ? "
      + " GROUP by cah.claim_id, cah.claim_submission_hist_id, cah.recovery_remittance_id) as foo "
      + " WHERE csh.claim_submission_hist_id = foo.claim_submission_hist_id "
      + " AND csh.claim_id = foo.claim_id AND csh.recovery_remittance_id = 0 ";

  /**
   * Update recovery amt and remittance id.
   *
   * @param remitId the remit id
   * @return the boolean
   */
  public Boolean updateRecoveryAmtAndRemittanceId(int remitId) {
    return DatabaseHelper.update(UPDATE_RECOVERY_AMT_AND_REMITTANCE_ID, new Object[]{remitId}) >= 0;
  }
  
  /** The Constant GET_BILL_LEVEL_CLAIM_HISTORY. */
  private static final String GET_BILL_LEVEL_CLAIM_HISTORY = " SELECT bc.bill_no, pr.center_id, "
      + " csh.resubmission_type, tpa.tpa_name, "
      + " sum(cah.claim_amount) as net_amount, isb.submission_date "
      + " FROM claim_submission_history csh "
      + " JOIN claim_activity_history cah "
      + " ON(csh.claim_submission_hist_id = cah.claim_submission_hist_id)"
      + " JOIN bill_charge bc ON(cah.charge_id = bc.charge_id) "
      + " JOIN insurance_claim icl ON(icl.claim_id = csh.claim_id) "
      + " JOIN patient_registration pr ON(pr.patient_id = icl.patient_id) "
      + " JOIN insurance_submission_batch isb "
      + " ON(isb.submission_batch_id = csh.submission_batch_id) "
      + " JOIN tpa_master tpa ON(tpa.tpa_id = csh.tpa_id) "
      + " WHERE isb.submission_date::date >= ? AND isb.submission_date::date <= ? "
      + " GROUP BY bc.bill_no, pr.center_id, csh.resubmission_type, "
      + " tpa.tpa_name, isb.submission_date ";

  /**
   * Gets the bill level claim history.
   *
   * @param fromSubmissionDate the from submission date
   * @param toSubmissionDate the to submission date
   * @return the bill level claim history
   */
  public Map<String, Object> getBillLevelClaimHistory(java.util.Date fromSubmissionDate, 
      java.util.Date toSubmissionDate) {
    List<BasicDynaBean> claimHistList = DatabaseHelper.queryToDynaList(
        GET_BILL_LEVEL_CLAIM_HISTORY, new Object[] { fromSubmissionDate, toSubmissionDate });
    Map<String, Object> claimHistMap = new HashMap();
    claimHistMap.put("billLevelClaimHistory", ConversionUtils.listBeanToListMap(claimHistList));

    return claimHistMap;
  }

}
