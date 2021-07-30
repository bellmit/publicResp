package com.insta.hms.messaging.providers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PhysicalDischargeDataProvider extends MapDataProvider {

  @Override
  public List<String> getTokens() throws SQLException {
    String[] tokens = new String[] { "patient_name", "discharge_date", "discharge_time",
        "admission_date", "admission_time", "receipient_id__", "visit_id", "doctor_name",
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
