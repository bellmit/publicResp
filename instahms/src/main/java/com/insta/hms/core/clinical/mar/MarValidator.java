package com.insta.hms.core.clinical.mar;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.mdm.MasterValidator;
import com.insta.hms.mdm.servingfrequency.ServingFrequencyService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;

@Component
public class MarValidator extends MasterValidator {

  @LazyAutowired
  private ServingFrequencyService servingFrequencyService;

  /**
   * Validates if a MAR setup is applicable.
   * @param prescription prescription bean
   * @param errMap mutable map for appending error message encountered during validation
   * @return true if validation passed
   */
  public boolean isMarSetupApplicable(BasicDynaBean prescription, ValidationErrorMap errMap) {

    if (prescription.get("discontinued").equals("Y")) {
      errMap.addError("discontinued", "ui.alert.prescription.discontinued.msg");
      return false;
    }
    Integer recurrenceDailyId = (Integer) prescription.get("recurrence_daily_id");
    String freqType = (String) prescription.get("freq_type");
    if (recurrenceDailyId != null && recurrenceDailyId == -2) {
      errMap.addError("others", "exception.mar.setup.not.available");
      return false;
    } else if (recurrenceDailyId != null && "F".equals(freqType)) {
      BasicDynaBean servingFrequencyBean = servingFrequencyService
          .findByRecurrenceDailyId((Integer) prescription.get("recurrence_daily_id"));
      if (servingFrequencyBean == null) {
        errMap.addError("others", "exception.mar.setup.serving.ferquency.notexists");
        return false;
      }
    }

    return true;
  }

}
