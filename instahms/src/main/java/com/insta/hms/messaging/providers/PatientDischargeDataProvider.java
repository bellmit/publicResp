package com.insta.hms.messaging.providers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PatientDischargeDataProvider extends MapDataProvider {

  @Override
  public List<String> getTokens() throws SQLException {
    String[] tokens = new String[] { "patient_name", "discharge_date", "discharge_time", "visit_id",
        "initiate_discharging_time", "initiate_discharging_date", "expected_discharge_date",
        "expected_discharge_time", "financial_discharge_date", "financial_discharge_time",
        "billed_amount", "sponsor_amount", "sponsor_due", "patient_due", "patient_amount",
        "discharge_date", "admission_date", "admission_time", "receipient_id__", "doctor_name",
        "dept_name", "bed_name", "ward_name", "lang_code" };
    List<String> tokenList = super.getTokens();

    if (null == tokenList) {
      tokenList = new ArrayList<String>();
    }
    for (String token : tokens) {
      if (!tokenList.contains(token)) {
        tokenList.add(token);
      }
    }
    return tokenList;
  }

}
