package com.insta.hms.api.repositories;

import org.springframework.stereotype.Repository;

/**
 * The Class AccountingHeadsListRepository.
 */
@Repository
public class AccountingHeadsListRepository {

  /** The Constant BILL_ACCOUNT_HEADS_NAMES. */
  private static final String BILL_ACCOUNT_HEADS_NAMES = " FROM (  SELECT account_head_name AS"
      + " account_head_name FROM bill_account_heads ) AS FOO ";

  /**
   * Gets the bill account heads names query.
   *
   * @return the bill account heads names query
   */
  public String getBillAccountHeadsNamesQuery() {
    return BILL_ACCOUNT_HEADS_NAMES;
  }

  /** The Constant TPA_NAMES. */
  private static final String TPA_NAMES = " FROM ( SELECT tpa_name AS account_head_name "
      + " FROM tpa_master ) AS FOO ";

  /**
   * Gets the tpa names query.
   *
   * @return the tpa names query
   */
  public String getTpaNamesQuery() {
    return TPA_NAMES;
  }

  /** The Constant PAYMENT_MODE_NAMES. */
  private static final String PAYMENT_MODE_NAMES = " FROM (  SELECT payment_mode AS "
      + " account_head_name FROM payment_mode_master ) AS FOO ";

  /**
   * Gets the payment mode names query.
   *
   * @return the payment mode names query
   */
  public String getPaymentModeNamesQuery() {
    return PAYMENT_MODE_NAMES;
  }

  /** The Constant DOCTOR_NAMES. */
  private static final String DOCTOR_NAMES = " FROM ( SELECT doctor_name "
      + " AS account_head_name FROM doctors ) AS FOO ";

  /**
   * Gets the doctor names query.
   *
   * @return the doctor names query
   */
  public String getDoctorNamesQuery() {
    return DOCTOR_NAMES;
  }

  /** The Constant REFERRAL_NAMES. */
  private static final String REFERRAL_NAMES = " FROM ( SELECT referral_name "
      + " AS account_head_name FROM referral ) AS FOO ";

  /**
   * Gets the referral names query.
   *
   * @return the referral names query
   */
  public String getReferralNamesQuery() {
    return REFERRAL_NAMES;
  }

  /** The Constant SUPPLIER_NAMES. */
  private static final String SUPPLIER_NAMES = " FROM ( SELECT supplier_name "
      + " AS account_head_name FROM supplier_master ) AS FOO ";

  /**
   * Gets the supplier names query.
   *
   * @return the supplier names query
   */
  public String getSupplierNamesQuery() {
    return SUPPLIER_NAMES;
  }

  /** The Constant PAYMENTS_DETAILS_NAMES. */
  private static final String PAYMENTS_DETAILS_NAMES = " FROM ( SELECT payee_name "
      + " AS account_head_name FROM payments_details ) AS FOO ";

  /**
   * Gets the payments details names query.
   *
   * @return the payments details names query
   */
  public String getPaymentsDetailsNamesQuery() {
    return PAYMENTS_DETAILS_NAMES;
  }

  /** The Constant PAYMENTS_NAMES. */
  private static final String PAYMENTS_NAMES = " FROM ( SELECT payee_name "
      + " AS account_head_name FROM payments ) AS FOO ";

  /**
   * Gets the payments names query.
   *
   * @return the payments names query
   */
  public String getPaymentsNamesQuery() {
    return PAYMENTS_NAMES;
  }

  /** The Constant OUTHOUSE_NAMES. */
  private static final String OUTHOUSE_NAMES = " FROM ( SELECT oh_name "
      + " AS account_head_name FROM outhouse_master ) AS FOO ";

  /**
   * Gets the outhouse names query.
   *
   * @return the outhouse names query
   */
  public String getOuthouseNamesQuery() {
    return OUTHOUSE_NAMES;
  }

}
