package com.insta.hms.billing.payment;

import java.math.BigDecimal;

// TODO: Auto-generated Javadoc
/**
 * The Class ChargeBreakup.
 */
public class ChargeBreakup {

  /** The doctor. */
  private BigDecimal doctor = new BigDecimal(0);

  /** The referral. */
  private BigDecimal referral = new BigDecimal(0);

  /** The prescribed. */
  private BigDecimal prescribed = new BigDecimal(0);

  /** The hospital. */
  private BigDecimal hospital = new BigDecimal(0);

  /**
   * Instantiates a new charge breakup.
   *
   * @param doctor
   *          the doctor
   * @param referral
   *          the referral
   * @param prescribed
   *          the prescribed
   * @param hospital
   *          the hospital
   */
  public ChargeBreakup(BigDecimal doctor, BigDecimal referral, BigDecimal prescribed,
      BigDecimal hospital) {
    this.doctor = doctor;
    this.referral = referral;
    this.hospital = hospital;
    this.prescribed = prescribed;
  }

  /**
   * Gets the doctor component.
   *
   * @return the doctor component
   */
  public BigDecimal getDoctorComponent() {
    return doctor;
  }

  /**
   * Gets the referral component.
   *
   * @return the referral component
   */
  public BigDecimal getReferralComponent() {
    return referral;
  }

  /**
   * Gets the hospital component.
   *
   * @return the hospital component
   */
  public BigDecimal getHospitalComponent() {
    return hospital;
  }

  /**
   * Gets the prescribed component.
   *
   * @return the prescribed component
   */
  public BigDecimal getPrescribedComponent() {
    return prescribed;
  }

}
