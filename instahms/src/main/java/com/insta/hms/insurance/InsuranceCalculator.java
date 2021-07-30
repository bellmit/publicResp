package com.insta.hms.insurance;

import java.math.BigDecimal;

/*
 * InsuraceCalculator is an interface. In this Interface will declare all methods used for
 * insurance calculations. These methods will be defined in classes where we implements this
 * interface.
 *
 * Eg : 1. AdvanceInsuranceCalculator implements InsuraceCalculator. Here calculateClaim method
 * is defined to get a claim amount specific to advance insurance case. 2.
 * SimpleInsuranceCalculator implements InsuraceCalculator. Here calculateClaim method is
 * defined to get a claim amount specific to simple insurance case. 3.
 * PerdiemInsuranceCalculator implements InsuraceCalculator. Here calculateClaim method is
 * defined to get a claim amount specific to perdiem case.
 */

/**
 * The Interface InsuranceCalculator.
 */
public interface InsuranceCalculator {

  // TODO : Need to remove this method once we complete the new calculate claim method coding.

  /**
   * Calculate claim.
   *
   * @param amount          the amount
   * @param discount        the discount
   * @param billNo          the bill no
   * @param planId          the plan id
   * @param firstOfCategory the first of category
   * @param visitType       the visit type
   * @param categoryId      the category id
   * @return the big decimal
   * @throws Exception the exception
   */
  public BigDecimal calculateClaim(BigDecimal amount, BigDecimal discount, String billNo,
      int planId, Boolean firstOfCategory, String visitType, int categoryId) throws Exception;

  /**
   * Calculate claim.
   *
   * @param amount           the amount
   * @param discount         the discount
   * @param billNo           the bill no
   * @param planId           the plan id
   * @param firstOfCategory  the first of category
   * @param visitType        the visit type
   * @param categoryId       the category id
   * @param insurancePayable the insurance payable
   * @return the big decimal
   * @throws Exception the exception
   */
  // New calculate claim method
  public BigDecimal calculateClaim(BigDecimal amount, BigDecimal discount, String billNo,
      int planId, Boolean firstOfCategory, String visitType, int categoryId,
      boolean insurancePayable) throws Exception;

}
