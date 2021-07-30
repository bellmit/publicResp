package com.insta.hms.messaging.providers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PatientAdmissionDataProvider extends MapDataProvider {

  @Override
  public List<String> getTokens() throws SQLException {
    String[] tokens = new String[] { "mr_no", "salutation_name", "patient_name", "admission_date",
        "admission_date_yyyy_mm_dd", "admission_time", "admission_time_12hr", "next_of_kin_contact",
        "next_of_kin_name", "center_name", "center_code", "complaint", "admitted_by", "department",
        "patient_phone", "doctor_name", "referal_doctor", "doctor_mobile", "referal_doctor_mobile",
        "department_id", "receipient_id__", "receipient_type__", "message_footer",
        "doctor_specialization", "lang_code" };
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
