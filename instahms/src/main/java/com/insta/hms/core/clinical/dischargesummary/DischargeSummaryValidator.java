package com.insta.hms.core.clinical.dischargesummary;

import com.insta.hms.exception.ValidationErrorMap;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author anup vishwas.
 *
 */

@Component
public class DischargeSummaryValidator {

  /**
   * Validates follow up details.
   * 
   * @param followUpDetailsList the list
   * @param errors the errors
   * @return boolean
   */
  @SuppressWarnings("rawtypes")
  public boolean validateFollowUpDetails(List<Map> followUpDetailsList, ValidationErrorMap errors) {
    boolean success = true;
    for (int i = 0; i < followUpDetailsList.size(); i++) {
      Map map = followUpDetailsList.get(i);
      String isAddedDeletedModified = (String) map.get("followup_status");
      if (isAddedDeletedModified.equals("newadded") || isAddedDeletedModified.equals("exists")) {
        if (map.get("followup_doctor_id") != null) {
          if (map.get("followup_date") == null || map.get("followup_date").equals("")) {
            errors.addError("followup_date",
                "js.registration.dischargesummary.details.followupdate");
            return false;
          }
          if (map.get("followup_remarks") == null || map.get("followup_remarks").equals("")) {
            errors.addError("followup_remarks",
                "js.registration.dischargesummary.details.followupremarks");
            return false;
          }
        }
      }
    }
    return success;
  }

}
