package com.insta.hms.core.insurance;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.exception.ValidationException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ReconciliationValidator {

  private ReconciliationValidator() {
  }

  /**
   * Validate remittance request.
   *
   * @param bills
   *          the bills
   * @return the string
   */
  public static String validateRemittanceRequest(Map<String, Map<String, Object>> bills) {
    String errorMessage = "";
    Set<String> allocationKeys = bills.keySet();
    for (String allocationKey : allocationKeys) {
      BigDecimal sum = new BigDecimal(0);
      Map remittanceDetails = (Map<String, Object>) bills.get(allocationKey);
      String amountString = String.valueOf(remittanceDetails.get("remittance_amount"));
      if (null == amountString) {
        errorMessage = "No amount entered for bill number " + remittanceDetails.get("bill_no");
        return errorMessage;
      }
      BigDecimal amount = new BigDecimal(amountString);
      Map<String, Object> allocations = (Map<String, Object>) remittanceDetails.get("allocation");
      boolean autoAllocate = (boolean) remittanceDetails.get("auto_allocate");
      if (null == allocations) {
        if (autoAllocate) {
          continue;
        }
        errorMessage = "No allocation entered for bill number " + remittanceDetails.get("bill_no");
        return errorMessage;
      }
      Set<String> charges = allocations.keySet();
      for (String chargeId : charges) {
        Map<String, Object> allocationData = (Map<String, Object>) allocations.get(chargeId);
        String chargeAmountString = String.valueOf(allocationData.get("amount"));
        if (null == chargeAmountString) {
          errorMessage =
              "No amount entered for chargeId " + chargeId + " of bill number " + remittanceDetails
                  .get("bill_no");
          return errorMessage;
        }
        BigDecimal chargeAmount = new BigDecimal(chargeAmountString);
        sum = sum.add(chargeAmount);
      }

      if (sum.compareTo(amount) != 0) {
        errorMessage = "Mismatch in amount allocated for bill " + remittanceDetails.get("bill_no")
            + " and the sum of amount allocated to the bill charges.";
        return errorMessage;
      }

    }
    return errorMessage;
  }

  private static final long MAX_DATE_DIFF = 30L * 24 * 60 * 60 * 1000;

  /**
   * Validate date range.
   *
   * @param from
   *          the from date
   * @param to
   *          the to date
   * @return the error string
   */
  public static String validateDateRange(Date from, Date to) {
    Date now = new Date();
    if (now.before(from)) {
      return "From date cannot be in the future";
    }
    if (now.before(to)) {
      return "To date cannot be in the future";
    }
    if (from.compareTo(to) > 0) {
      return "From date cannot be greater than To date.";
    }
    long diff = to.getTime() - from.getTime();
    if (diff > MAX_DATE_DIFF) {
      return "Date range cannot exceed 30 Days";
    }
    return "";
  }


}
