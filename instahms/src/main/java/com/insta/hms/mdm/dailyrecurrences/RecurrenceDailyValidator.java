package com.insta.hms.mdm.dailyrecurrences;

import com.insta.hms.mdm.MasterValidator;

import org.springframework.stereotype.Component;

/**
 * The validator for recurrence daily master.
 * @author sainathbatthala
 */
@Component
public class RecurrenceDailyValidator extends MasterValidator {
  public boolean validateDelete() {
    // Need to implement validations for delete
    return true;
  }
}
