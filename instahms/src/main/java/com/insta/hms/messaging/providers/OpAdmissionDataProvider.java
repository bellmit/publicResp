package com.insta.hms.messaging.providers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class OpAdmissionDataProvider extends MapDataProvider {

  @Override
  public List<String> getTokens() throws SQLException {
    String[] tokens = new String[] { "salutation_name", "patient_name", "admission_date",
        "admission_time", "admission_time_12hr", "admission_date_yyyy_mm_dd", "center_name",
        "center_code", "complaint", "admitted_by", "department", "patient_phone", "doctor_name",
        "referal_doctor", "doctor_mobile", "referal_doctor_mobile", "department_id",
        "receipient_id__", "receipient_type__", "message_footer" };

    List<String> tokenList = super.getTokens();

    if (tokenList == null) {
      tokenList = new ArrayList<String>();
    }

    tokenList.addAll(Arrays.asList(tokens));
    Collections.sort(tokenList);
    return tokenList;
  }

}
