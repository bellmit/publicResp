package com.insta.hms.integration.insurance.remittance;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class InsuranceRemittanceDetailsRepository.
 */
@Repository
public class InsuranceRemittanceDetailsRepository extends GenericRepository {

  /**
   * Instantiates a new insurance remittance details repository.
   */
  public InsuranceRemittanceDetailsRepository() {
    super("insurance_remittance_details");
  }

  /** The get claim errors. */
  // returns claims that have errors but dont contain any activities with errors
  private final String getClaimErrors = "SELECT ird.claim_id, ird.error as claim_error "
      + "FROM insurance_remittance_details ird WHERE remittance_id = ? "
      + "AND error != 0 AND NOT EXISTS (SELECT * FROM insurance_remittance_activity_details irad,"
      + "insurance_remittance_details ird WHERE ird.claim_id = irad.claim_id AND "
      + "ird.remittance_id = ? AND irad.error != 0 AND irad.remittance_id = ?)";

  /**
   * Gets the error claims.
   *
   * @param remitId the remit id
   * @return the error claims
   */
  public List<BasicDynaBean> getErrorClaims(Integer remitId) {

    return DatabaseHelper.queryToDynaList(getClaimErrors,
        new Object[] {remitId, remitId, remitId});
  }



  /** The update error no claim elem found. */
  private final String updateErrorNoClaimElemFound = "UPDATE insurance_remittance_details"
      + " ird SET error = (error + ?) WHERE (ird.claim_id = '' OR ird.claim_id IS NULL)"
      + " AND ird.remittance_id = ?";

  /** The update error invalid claim id found. */
  // validation no longer used removed as part of Operation Kill Green Rain v2
  private final String updateErrorInvalidClaimIdFound =
      "UPDATE insurance_remittance_details ird SET error = (error + ?)"
          + "WHERE claim_id NOT IN (SELECT ic.claim_id FROM insurance_claim ic "
          + "JOIN insurance_remittance_details ird ON"
          + "(ird.claim_id = ic.claim_id AND remittance_id = ?)) AND remittance_id = ?";

  /** The update warning invalid claim id found. */
  private final String updateWarningInvalidClaimIdFound =
      "UPDATE insurance_remittance_details ird SET warning = (warning + ?)"
          + "WHERE claim_id NOT IN (SELECT ic.claim_id FROM insurance_claim ic "
          + "JOIN insurance_remittance_details ird ON"
          + " (ird.claim_id = ic.claim_id AND remittance_id = ?)) AND remittance_id = ?";


  /** The update error invalid batch id. */
  private final String updateErrorInvalidBatchId =
      "UPDATE insurance_remittance_details ird SET error = (error + ?) "
          + "FROM insurance_claim ic " + "WHERE ic.claim_id = ird.claim_id AND "
          + "(ic.last_submission_batch_id = '' OR ic.last_submission_batch_id IS NULL)"
          + " AND ird.remittance_id = ?";

  /** The update error batch not sent. */
  private final String updateErrorBatchNotSent =
      "UPDATE insurance_remittance_details ird SET error = (error + ?) "
          + "FROM claim_submission_batch_view csb WHERE csb.claim_id = ird.claim_id AND "
          + "csb.status != 'S' AND ird.remittance_id = ?";

  /** The update error no payer id. */
  private final String updateErrorNoPayerId = "UPDATE insurance_remittance_details "
      + "ird SET error = (error + ?) WHERE (ird.payer_id = '' OR ird.payer_id IS NULL)"
      + "AND ird.remittance_id = ?";

  /** The update error no provider id. */
  private final String updateErrorNoProviderId = "UPDATE insurance_remittance_details ird "
      + "SET error = (error + ?) WHERE (ird.provider_id = '' OR ird.provider_id IS NULL)"
      + " AND ird.remittance_id = ?";

  /** The update error no payment ref. */
  private final String updateErrorNoPaymentRef =
      "UPDATE insurance_remittance_details ird " + "SET error = (error + ?) WHERE "
          + "(ird.payment_reference IS NULL OR ird.payment_reference = '')"
          + " AND ird.remittance_id = ?";

  /** The update error no activity. */
  private final String updateErrorNoActivity =
      "UPDATE insurance_remittance_details ird " + "SET error = (error + ?) WHERE NOT EXISTS "
          + "(SELECT * FROM insurance_remittance_activity_details WHERE remittance_id = ?) "
          + "AND remittance_id = ?";

  /** The update error duplicate transaction. */
  private final String updateErrorDuplicateTransaction =
      "UPDATE insurance_remittance_details ird " + "SET error = (ird.error + ?) "
          + "FROM insurance_remittance ir2," + "     insurance_remittance_details ird2,"
          + "     insurance_remittance ir " + "WHERE ird.remittance_id = ?"
          + "  AND ird2.claim_id = ird.claim_id"
          + "  AND ird2.remittance_id != ird.remittance_id"
          + "  AND ir.remittance_id = ird.remittance_id"
          + "  AND ird2.remittance_id = ir2.remittance_id"
          + "  AND ir.transaction_date = ir2.transaction_date"
          + "  AND ir2.processing_status = 'C' ";

  // query no longer used, needs to be cleaned up at a later stage. Removed as part of
  /** The update error duplicate payment ref. */
  // Operation Kill Green Rain v2
  private final String updateErrorDuplicatePaymentRef =
      "UPDATE insurance_remittance_details ird " + "SET error = ( ird.error + ? ) "
          + "FROM insurance_remittance_details ird2 " + "WHERE ird.claim_id = ird2.claim_id "
          + " AND ird.remittance_id = ? "
          + " AND ird.payment_reference = ird2.payment_reference " + " AND ird2.error = 0 "
          + " AND ird2.remittance_id NOT IN ( " + " SELECT irad.remittance_id "
          + " FROM insurance_remittance_activity_details irad " + " WHERE irad.error != 0 "
          + " ) " + "AND ird2.remittance_id != ird.remittance_id ";

  /** The update error invalid provider id. */
  private final String updateErrorInvalidProviderId =
      "UPDATE insurance_remittance_details ird SET error = (error + ?) "
          + " WHERE ird.provider_id NOT IN " + " ( "
          + " SELECT distinct(acv.ser_reg_no) FROM accountgrp_and_center_view acv " + " ) "
          + " AND ird.remittance_id = ?";


  /**
   * Validate claims.
   *
   * @param remitId the remit id
   * @return the integer
   */
  public Integer validateClaims(Integer remitId) {
    int errorCount = 0;
    errorCount += DatabaseHelper.update(updateErrorNoClaimElemFound,
        new Object[] {ClaimErrorType.NO_ID_CLAIM_FOUND.getCode(), remitId});
    errorCount += DatabaseHelper.update(updateErrorInvalidBatchId,
        new Object[] {ClaimErrorType.INVALID_BATCH_ID.getCode(), remitId});
    errorCount += DatabaseHelper.update(updateErrorBatchNotSent,
        new Object[] {ClaimErrorType.INVALID_BATCH_NOT_SENT.getCode(), remitId});
    errorCount += DatabaseHelper.update(updateErrorNoPayerId,
        new Object[] {ClaimErrorType.ID_PAYER_NOT_FOUND.getCode(), remitId});
    errorCount += DatabaseHelper.update(updateErrorNoProviderId,
        new Object[] {ClaimErrorType.ID_PROVIDER_NOT_FOUND.getCode(), remitId});
    errorCount += DatabaseHelper.update(updateErrorNoPaymentRef,
        new Object[] {ClaimErrorType.PAYMENT_REF_NOT_FOUND.getCode(), remitId});
    errorCount += DatabaseHelper.update(updateErrorNoActivity,
        new Object[] {ClaimErrorType.ACTIVITY_NOT_FOUND.getCode(), remitId, remitId});
    errorCount += DatabaseHelper.update(updateErrorInvalidProviderId,
        new Object[] {ClaimErrorType.INVALID_PROVIDER_ID.getCode(), remitId});
    errorCount += DatabaseHelper.update(updateErrorDuplicateTransaction,
        new Object[] {ClaimErrorType.DUPLICATE_REMITTANCE_FOR_CLAIM.getCode(), remitId});

    // do warning checks and update warning codes accordingly
    DatabaseHelper.update(updateWarningInvalidClaimIdFound, new Object[] {
        ClaimWarningType.CLAIM_ID_NOT_FOUND_SKIPPED.getCode(), remitId, remitId});

    return errorCount;

  }

  /** The get warning count. */
  private final String getWarningCount =
      "SELECT count(*) FROM insurance_remittance_details ird "
          + "WHERE ird.warning !=0 AND ird.remittance_id = ?";


  /**
   * Do warnings exist.
   *
   * @param remitId the remit id
   * @return true, if successful
   */
  public boolean doWarningsExist(Integer remitId) {
    int warningCount = DatabaseHelper.getInteger(getWarningCount, new Object[] {remitId});
    return warningCount > 0;
  }

  /** The get all claims with warning. */
  // Returns details where claim has warnings
  public final String getAllClaimsWithWarning = "SELECT ird.claim_id, "
      + "ird.warning AS claim_warning " + "FROM insurance_remittance_details ird "
      + "WHERE ird.remittance_id = ? " + "AND ird.warning != 0";

  /**
   * Gets the claims with warning codes > 0.
   *
   * @param remittanceId the remittance id
   * @return the claims with warning
   */
  public List<BasicDynaBean> getClaimsWithWarning(Integer remittanceId) {
    return DatabaseHelper.queryToDynaList(getAllClaimsWithWarning,
        new Object[] {remittanceId});
  }
}
