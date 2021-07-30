package com.insta.hms.integration.insurance.remittance;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class InsuranceRemittanceActivityDetailsRepository.
 */
@Repository
public class InsuranceRemittanceActivityDetailsRepository extends GenericRepository {

  /**
   * Instantiates a new insurance remittance activity details repository.
   */
  public InsuranceRemittanceActivityDetailsRepository() {
    super("insurance_remittance_activity_details");
  }

  /** The get all claims with no act err. */
  // Returns only details where activity has errors and claim has no errors
  public final String getAllClaimsWithNoActError = "SELECT ird.claim_id, irad.activity_id, "
      + "ird.error AS claim_error, irad.error AS activity_error "
      + "FROM insurance_remittance_activity_details irad, insurance_remittance_details ird "
      + "WHERE ird.remittance_id = ? AND irad.remittance_id = ? "
      + " AND irad.claim_id = ird.claim_id AND irad.error != 0 " + "AND ird.error = 0";

  /**
   * Gets the claims with no act err.
   *
   * @param remittanceId the remittance id
   * @return the claims with no act err
   */
  public List<BasicDynaBean> getClaimsWithNoActErr(Integer remittanceId) {

    return DatabaseHelper.queryToDynaList(getAllClaimsWithNoActError,
        new Object[] {remittanceId, remittanceId});

  }

  /** The get all claims with act err. */
  // Returns details where activity has errors and claim has errors
  public final String getAllClaimsWithActError = "SELECT ird.claim_id, irad.activity_id, "
      + "ird.error AS claim_error, irad.error AS activity_error "
      + "FROM insurance_remittance_activity_details irad, insurance_remittance_details ird "
      + "WHERE ird.remittance_id = ? AND irad.remittance_id = ? "
      + " AND irad.claim_id = ird.claim_id AND irad.error != 0 " + "AND ird.error != 0";

  /**
   * Gets the claims with act err.
   *
   * @param remittanceId the remittance id
   * @return the claims with act err
   */
  public List<BasicDynaBean> getClaimsWithActErr(Integer remittanceId) {

    return DatabaseHelper.queryToDynaList(getAllClaimsWithActError,
        new Object[] {remittanceId, remittanceId});
  }


  /** The get all activities with err. */
  // Returns only activities with errors that belong to errorless claims
  private final String getAllActivitiesWithErr =
      "SELECT irad.claim_id, irad.activity_id, irad.error AS activity_error "
          + "FROM insurance_remittance_activity_details irad, insurance_remittance_details ird "
          + "WHERE irad.remittance_id = ? AND irad.error != 0 AND ird.claim_id = irad.claim_id "
          + "AND ird.error = 0 AND ird.remittance_id = ?";

  /**
   * Gets the acts with err.
   *
   * @param remitId the remit id
   * @return the acts with err
   */
  public List<BasicDynaBean> getActsWithErr(Integer remitId) {

    return DatabaseHelper.queryToDynaList(getAllActivitiesWithErr,
        new Object[] {remitId, remitId});
  }

  /** The update warning invalid activity. */
  private final String updateWarningInvalidActivity =
      "UPDATE insurance_remittance_activity_details "
          + "SET warning = (warning + ?) WHERE (activity_id NOT LIKE 'A-%' "
          + "AND activity_id NOT LIKE 'P-%' AND remittance_id = ?)";

  /** The update err combined hosp invalid batch. */
  private final String updateErrorCombinedInvalidHospitalBatch =
      "UPDATE insurance_remittance_activity_details "
          + "SET error = (error + ?) WHERE split_part(activity_id,'-',4) NOT IN ("
          + "SELECT submission_batch_id FROM insurance_remittance_activity_details irad "
          + "JOIN insurance_submission_batch isb "
          + "ON( split_part(activity_id,'-',4) = isb.submission_batch_id )) "
          + "AND activity_id LIKE 'A-ACT-%' AND remittance_id = ? "
          + "AND split_part(activity_id,'-',4) != ''";

  /** The update err combined phar invalid batch. */
  private final String updateErrorCombinedInvalidPharmacyBatch =
      "UPDATE insurance_remittance_activity_details "
          + "SET error = (error + ?) WHERE split_part(activity_id,'-',5) NOT IN ("
          + "SELECT submission_batch_id FROM insurance_remittance_activity_details irad "
          + "JOIN insurance_submission_batch isb "
          + "ON( split_part(activity_id,'-',5) = isb.submission_batch_id )) "
          + "AND activity_id LIKE 'P-ACT-%' AND remittance_id = ? "
          + "AND split_part(activity_id,'-',5) != ''";

  /** The update err hosp invalid batch. */
  private final String updateErrHospInvalidBatch =
      "UPDATE insurance_remittance_activity_details "
          + "SET error = (error + ?) WHERE split_part(activity_id,'-',3) NOT IN ("
          + "SELECT submission_batch_id FROM insurance_remittance_activity_details irad "
          + "JOIN insurance_submission_batch isb "
          + "ON( split_part(activity_id,'-',3) = isb.submission_batch_id )"
          + "WHERE irad.remittance_id = ?) "
          + "AND activity_id LIKE 'A-%' AND remittance_id = ?"
          + " AND activity_id NOT LIKE 'A-ACT-%' " + "AND split_part(activity_id,'-',3) != ''";

  /** The update err phar invalid batch. */
  private final String updateErrorPharmacyInvalidBatch =
      "UPDATE insurance_remittance_activity_details "
          + "SET error = (error + ?) WHERE split_part(activity_id,'-',4) NOT IN ("
          + "SELECT submission_batch_id FROM insurance_remittance_activity_details irad "
          + "JOIN insurance_submission_batch isb "
          + "ON( split_part(activity_id,'-',4) = isb.submission_batch_id )"
          + "WHERE irad.remittance_id = ?) "
          + "AND activity_id LIKE 'P-%' AND remittance_id = ? "
          + "AND activity_id NOT LIKE 'P-ACT-%' AND " + "split_part(activity_id,'-',4) != ''";

  /** The update warning hosp invalid act id. */
  private final String updateWarningHospInvalidActId =
      "UPDATE insurance_remittance_activity_details "
          + "SET warning = (warning + ?) WHERE activity_id NOT IN( "
          + "SELECT activity_id FROM insurance_remittance_activity_details irad "
          + "JOIN bill_charge bc " + "ON(split_part(activity_id,'-',2) = bc.charge_id ) "
          + "WHERE irad.remittance_id = ?) AND activity_id LIKE 'A-%' "
          + "AND remittance_id = ? AND activity_id NOT LIKE 'A-ACT-%'";

  /** The update warning phar invalid act id. */
  private final String updateWanringPharInvalidActId =
      "UPDATE insurance_remittance_activity_details " + "SET warning = (warning + ?) "
          + "WHERE activity_id NOT IN " + "(SELECT activity_id "
          + "FROM insurance_remittance_activity_details irad "
          + "JOIN store_sales_main ssm ON(split_part(activity_id,'-',2) = ssm.charge_id) "
          + "JOIN store_sales_details ssd ON"
          + "(split_part(activity_id, '-', 3) = ssd.sale_item_id::VARCHAR "
          + "AND ssm.sale_id = ssd.sale_id " + "AND remittance_id = ?)) "
          + " AND activity_id LIKE 'P-%' " + " AND remittance_id = ? "
          + " AND activity_id NOT LIKE 'P-ACT-%'";

  /** The update warning hosp combined invalid act id. */
  private final String updateWarningHospCombinedInvalidActId =
      "UPDATE insurance_remittance_activity_details "
          + "SET warning = (warning + ?) WHERE activity_id NOT IN "
          + "(SELECT activity_id FROM insurance_remittance_activity_details irad "
          + "JOIN bill_charge bc "
          + "ON( split_part(activity_id,'-',3) = bc.charge_id ) WHERE irad.remittance_id = ?) "
          + "AND activity_id LIKE 'A-ACT-%' AND remittance_id = ?";

  /** The update warning phar combined invalid act id. */
  private final String updateWarningPharCombinedInvalidActId =
      "UPDATE insurance_remittance_activity_details\n" + "SET warning = (warning + ?) "
          + "WHERE activity_id NOT IN " + "    (SELECT activity_id "
          + "     FROM insurance_remittance_activity_details irad "
          + "     JOIN store_sales_main ssm ON(split_part(activity_id,'-',3) = ssm.charge_id) "
          + "     JOIN store_sales_details ssd"
          + "      ON(split_part(activity_id, '-', 4) = ssd.sale_item_id::VARCHAR "
          + "                                     AND ssm.sale_id = ssd.sale_id "
          + "                                     AND remittance_id = ?)) "
          + "  AND activity_id LIKE 'P-ACT-%' " + "  AND remittance_id = ? ";

  /** The update err phar no sale item. */
  private final String updatePharNoSaleItem = "UPDATE insurance_remittance_activity_details "
      + "SET error = (error + ?) WHERE split_part(activity_id,'-',3) = '' "
      + "AND  remittance_id = ? AND activity_id LIKE 'P-%'";

  /** The update error invalid denial code. */
  private final String updateErrInvalidDenialCode =
      "UPDATE insurance_remittance_activity_details irad "
          + "SET error = (error + ?) WHERE irad.denial_code NOT IN "
          + "(SELECT idc.denial_code FROM insurance_denial_codes idc,"
          + " insurance_remittance_activity_details irad2 "
          + "WHERE idc.denial_code =  irad2.denial_code AND remittance_id = ?) "
          + "AND remittance_id = ? " + "AND denial_code IS NOT NULL";

  /** The update err act id not found. */
  private final String updateErrActIdNotFound = "UPDATE insurance_remittance_activity_details "
      + "SET error = (error + ?) WHERE (activity_id = '' AND remittance_id = ?)";

  /** The update err invalid start date. */
  private final String updateErrInvalidStartDate =
      "UPDATE insurance_remittance_activity_details irad "
          + "SET error = (error + ?) WHERE irad.start_date > now() "
          + "AND irad.remittance_id = ? OR start_date IS NULL";

  /** The update err invalid type. */
  private final String updateErrInvalidType =
      "UPDATE insurance_remittance_activity_details irad "
          + "SET error = (error + ?) WHERE (irad.code_type IS NULL OR irad.code_type = '') "
          + "AND irad.remittance_id = ?";

  /** The update err invalid code. */
  private final String updateErrInvalidCode =
      "UPDATE insurance_remittance_activity_details irad "
          + "SET error = (error + ?) WHERE (irad.code IS NULL OR irad.code = '' ) "
          + "AND irad.remittance_id = ?";

  /** The update err invalid quantity. */
  private final String updateErrInvalidQuantity =
      "UPDATE insurance_remittance_activity_details irad "
          + "SET error = (error + ?) WHERE (irad.quantity IS NULL) AND irad.remittance_id = ?";

  /** The update err invalid net. */
  private final String updateErrInvalidNet =
      "UPDATE insurance_remittance_activity_details irad "
          + "SET error = (error + ?) WHERE irad.net is null AND irad.remittance_id = ?";

  /** The update err invalid clinician. */
  private final String updateErrInvalidClinician =
      "UPDATE insurance_remittance_activity_details irad "
          + "SET error = (error + ?) WHERE irad.clinician is null AND irad.remittance_id = ?";

  /** The update err invalid payment. */
  private final String updateErrInvalidPayment =
      "UPDATE insurance_remittance_activity_details irad "
          + "SET error = (error + ?) WHERE irad.payment_amount is null "
          + "AND irad.remittance_id = ?";

  /**
   * Validate activities.
   *
   * @param remitId the remit id
   * @return the integer
   */
  public Integer validateActivities(Integer remitId) {

    Map<String, Object[]> activityValidationQueryMap = new HashMap<String, Object[]>();
    Integer errorCount = 0;

    activityValidationQueryMap.put(updateErrorCombinedInvalidHospitalBatch,
        new Object[] {ActivityErrorType.INVALID_RESUB_BATCH_ID.getCode(), remitId});
    activityValidationQueryMap.put(updateErrorCombinedInvalidPharmacyBatch,
        new Object[] {ActivityErrorType.INVALID_RESUB_BATCH_ID.getCode(), remitId});
    activityValidationQueryMap.put(updateErrHospInvalidBatch,
        new Object[] {ActivityErrorType.INVALID_RESUB_BATCH_ID.getCode(), remitId, remitId});
    activityValidationQueryMap.put(updateErrorPharmacyInvalidBatch,
        new Object[] {ActivityErrorType.INVALID_RESUB_BATCH_ID.getCode(), remitId, remitId});
    activityValidationQueryMap.put(updatePharNoSaleItem,
        new Object[] {ActivityErrorType.INVALID_SALE_ITEM_ID.getCode(), remitId});
    activityValidationQueryMap.put(updateErrActIdNotFound,
        new Object[] {ActivityErrorType.ACTIVITY_ID_NOT_FOUND.getCode(), remitId});
    activityValidationQueryMap.put(updateErrInvalidStartDate,
        new Object[] {ActivityErrorType.INVALID_START_DATE.getCode(), remitId});
    activityValidationQueryMap.put(updateErrInvalidType,
        new Object[] {ActivityErrorType.TYPE_VALUE_NOT_FOUND.getCode(), remitId});
    activityValidationQueryMap.put(updateErrInvalidCode,
        new Object[] {ActivityErrorType.CODE_VALUE_NOT_FOUND.getCode(), remitId});
    activityValidationQueryMap.put(updateErrInvalidQuantity,
        new Object[] {ActivityErrorType.QUANTITY_VALUE_NOT_FOUND.getCode(), remitId});
    activityValidationQueryMap.put(updateErrInvalidPayment,
        new Object[] {ActivityErrorType.PAYMENT_VALUE_NOT_FOUND.getCode(), remitId});
    activityValidationQueryMap.put(updateErrInvalidDenialCode,
        new Object[] {ActivityErrorType.INVALID_DENIAL_CODE.getCode(), remitId, remitId});

    // Run through queries and update error codes
    for (String query : activityValidationQueryMap.keySet()) {
      errorCount += DatabaseHelper.update(query, activityValidationQueryMap.get(query));
    }

    // do warning checks and update warning codes accordingly
    DatabaseHelper.update(updateWarningInvalidActivity,
        new Object[] {ActivityWarningType.ACTIVITY_ID_NOT_FOUND_SKIPPED.getCode(), remitId});
    DatabaseHelper.update(updateWarningHospInvalidActId, new Object[] {
        ActivityWarningType.ACTIVITY_ID_NOT_FOUND_SKIPPED.getCode(), remitId, remitId});
    DatabaseHelper.update(updateWanringPharInvalidActId, new Object[] {
        ActivityWarningType.ACTIVITY_ID_NOT_FOUND_SKIPPED.getCode(), remitId, remitId});
    DatabaseHelper.update(updateWarningHospCombinedInvalidActId, new Object[] {
        ActivityWarningType.ACTIVITY_ID_NOT_FOUND_SKIPPED.getCode(), remitId, remitId});
    DatabaseHelper.update(updateWarningPharCombinedInvalidActId, new Object[] {
        ActivityWarningType.ACTIVITY_ID_NOT_FOUND_SKIPPED.getCode(), remitId, remitId});

    return errorCount;

  }

  /** The get warning count. */
  private final String getWarningCountQuery =
      "SELECT count(*) FROM insurance_remittance_activity_details irad "
          + "WHERE irad.warning !=0 AND irad.remittance_id = ?";

  /**
   * Do warnings exist.
   *
   * @param remitId the remit id
   * @return true, if successful
   */
  public boolean doWarningsExist(Integer remitId) {
    int warningCount = DatabaseHelper.getInteger(getWarningCountQuery, new Object[] {remitId});
    return warningCount > 0;
  }

  /** The get all activities with warning. */
  // Returns details where activity has warnings
  public final String getAllActivitiesWithWarningQuery =
      "SELECT irad.activity_id, irad.claim_id, irad.warning AS activity_warning "
          + "FROM insurance_remittance_activity_details irad "
          + "WHERE irad.remittance_id = ? AND irad.warning != 0";

  /**
   * Gets the activities with warning codes > 0.
   *
   * @param remittanceId the remittance id
   * @return the activities with warning
   */
  public List<BasicDynaBean> getActivitiesWithWarning(Integer remittanceId) {
    return DatabaseHelper.queryToDynaList(getAllActivitiesWithWarningQuery,
        new Object[] {remittanceId});
  }
}
